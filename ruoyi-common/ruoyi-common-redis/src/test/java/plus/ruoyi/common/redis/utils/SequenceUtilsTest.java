package plus.ruoyi.common.redis.utils;

import plus.ruoyi.common.test.base.BaseSpringTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SequenceUtils 分布式ID发号器工具类测试
 *
 * 需要启动 Spring 容器和 Redis 连接
 *
 * @author 抓蛙师
 */
@DisplayName("SequenceUtils分布式ID发号器测试")
public class SequenceUtilsTest extends BaseSpringTest {

    /**
     * 测试使用的key前缀
     */
    private static final String TEST_KEY_PREFIX = "test:seq:";

    /**
     * 测试使用的key列表，用于清理
     */
    private final List<String> testKeys = new ArrayList<>();

    @BeforeEach
    public void setUp() {
        testKeys.clear();
    }

    @AfterEach
    public void tearDown() {
        // 清理测试数据
        for (String key : testKeys) {
            try {
                RedisUtils.deleteObject(key);
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 生成测试key并记录
     */
    private String genTestKey(String suffix) {
        String key = TEST_KEY_PREFIX + suffix + ":" + System.currentTimeMillis();
        testKeys.add(key);
        return key;
    }

    // ==================== getNextId 测试 ====================

    @Test
    @DisplayName("测试getNextId-基本递增功能")
    public void testGetNextIdBasic() {
        String key = genTestKey("basic");

        long id1 = SequenceUtils.getNextId(key, Duration.ofMinutes(5));
        long id2 = SequenceUtils.getNextId(key, Duration.ofMinutes(5));
        long id3 = SequenceUtils.getNextId(key, Duration.ofMinutes(5));

        assertEquals(1, id1, "第一个ID应该是1");
        assertEquals(2, id2, "第二个ID应该是2");
        assertEquals(3, id3, "第三个ID应该是3");
    }

    @Test
    @DisplayName("测试getNextId-自定义初始值")
    public void testGetNextIdWithCustomParams() {
        String key = genTestKey("custom");

        // 参数说明：initValue=100（初始值），allocationSize=5（预分配大小）
        // allocationSize 是分布式环境下的性能优化参数，用于批量预分配 ID
        // 在单客户端测试中，ID 通常递增 1
        long id1 = SequenceUtils.getNextId(key, Duration.ofMinutes(5), 100, 5);
        long id2 = SequenceUtils.getNextId(key, Duration.ofMinutes(5), 100, 5);
        long id3 = SequenceUtils.getNextId(key, Duration.ofMinutes(5), 100, 5);

        // 验证初始值正确
        assertEquals(100, id1, "第一个ID应该是100");
        // 验证ID递增且唯一
        assertTrue(id2 > id1, "第二个ID应该大于第一个");
        assertTrue(id3 > id2, "第三个ID应该大于第二个");
    }

    @Test
    @DisplayName("测试getNextId-ID唯一性")
    public void testGetNextIdUniqueness() {
        String key = genTestKey("unique");
        Set<Long> ids = new HashSet<>();

        // 生成1000个ID
        for (int i = 0; i < 1000; i++) {
            long id = SequenceUtils.getNextId(key, Duration.ofMinutes(5));
            assertTrue(ids.add(id), "ID应该唯一，重复ID: " + id);
        }

        assertEquals(1000, ids.size(), "应该生成1000个唯一ID");
    }

    @Test
    @DisplayName("测试getNextIdString-返回字符串ID")
    public void testGetNextIdString() {
        String key = genTestKey("string");

        String id1 = SequenceUtils.getNextIdString(key, Duration.ofMinutes(5));
        String id2 = SequenceUtils.getNextIdString(key, Duration.ofMinutes(5));

        assertEquals("1", id1);
        assertEquals("2", id2);
    }

    @Test
    @DisplayName("测试getPaddedNextIdString-补零ID")
    public void testGetPaddedNextIdString() {
        String key = genTestKey("padded");

        String id1 = SequenceUtils.getPaddedNextIdString(key, Duration.ofMinutes(5), 6);
        String id2 = SequenceUtils.getPaddedNextIdString(key, Duration.ofMinutes(5), 6);

        assertEquals("000001", id1, "应该补零到6位");
        assertEquals("000002", id2, "应该补零到6位");
        assertEquals(6, id1.length());
        assertEquals(6, id2.length());
    }

    // ==================== getDateId 测试 ====================

    @Test
    @DisplayName("测试getDateId-带前缀的日期ID")
    public void testGetDateIdWithPrefix() {
        String prefix = "ORD";

        String id1 = SequenceUtils.getDateId(prefix);
        String id2 = SequenceUtils.getDateId(prefix);

        // 验证格式：ORD + yyyyMMdd + 序号
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        assertTrue(id1.startsWith(prefix + today), "ID应该以前缀+日期开头");
        assertTrue(id2.startsWith(prefix + today), "ID应该以前缀+日期开头");

        // 验证序号递增
        assertNotEquals(id1, id2, "两个ID应该不同");
    }

    @Test
    @DisplayName("测试getDateId-不带前缀的日期ID")
    public void testGetDateIdWithoutPrefix() {
        String prefix = "TEST" + System.currentTimeMillis();

        String id = SequenceUtils.getDateId(prefix, false);

        // 验证格式：yyyyMMdd + 序号（不包含业务前缀）
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        assertTrue(id.startsWith(today), "ID应该以日期开头，不包含业务前缀");
        assertFalse(id.startsWith(prefix), "ID不应该包含业务前缀");
    }

    @Test
    @DisplayName("测试getDateId-带补零的日期ID")
    public void testGetDateIdWithPadding() {
        String prefix = "PAD" + System.currentTimeMillis();

        String id = SequenceUtils.getDateId(prefix, true, 8);

        // 验证格式：前缀 + yyyyMMdd + 8位序号
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        assertTrue(id.startsWith(prefix + today), "ID应该以前缀+日期开头");

        // 提取序号部分验证补零
        String seqPart = id.substring((prefix + today).length());
        assertEquals(8, seqPart.length(), "序号部分应该是8位");
        assertTrue(seqPart.matches("\\d{8}"), "序号应该是8位数字");
    }

    @Test
    @DisplayName("测试getPaddedDateId-默认补零日期ID")
    public void testGetPaddedDateId() {
        String prefix = "DEF" + System.currentTimeMillis();

        String id = SequenceUtils.getPaddedDateId(prefix, true);

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        assertTrue(id.startsWith(prefix + today));

        // 验证使用默认6位补零
        String seqPart = id.substring((prefix + today).length());
        assertEquals(6, seqPart.length(), "默认应该补零到6位");
    }

    @Test
    @DisplayName("测试getDateId-指定日期")
    public void testGetDateIdWithSpecificDate() {
        String prefix = "HIST" + System.currentTimeMillis();
        LocalDate specificDate = LocalDate.of(2024, 1, 15);

        String id = SequenceUtils.getDateId(prefix, true, 6, specificDate);

        assertTrue(id.startsWith(prefix + "20240115"), "ID应该使用指定日期");
    }

    // ==================== getDateTimeId 测试 ====================

    @Test
    @DisplayName("测试getDateTimeId-带前缀的日期时间ID")
    public void testGetDateTimeIdWithPrefix() {
        String prefix = "SN" + System.currentTimeMillis();

        String id = SequenceUtils.getDateTimeId(prefix);

        // 验证格式：SN + yyyyMMddHHmmss + 序号
        assertTrue(id.startsWith(prefix), "ID应该以前缀开头");
        assertTrue(id.length() > prefix.length() + 14, "ID长度应该大于前缀+14位时间");
    }

    @Test
    @DisplayName("测试getDateTimeId-不带前缀的日期时间ID")
    public void testGetDateTimeIdWithoutPrefix() {
        String prefix = "TIME" + System.currentTimeMillis();

        String id = SequenceUtils.getDateTimeId(prefix, false);

        // 验证格式：yyyyMMddHHmmss + 序号
        assertFalse(id.startsWith(prefix), "ID不应该包含业务前缀");

        // 验证时间格式
        String timePart = id.substring(0, 14);
        assertTrue(timePart.matches("\\d{14}"), "应该以14位时间开头");
    }

    @Test
    @DisplayName("测试getDateTimeId-带补零的日期时间ID")
    public void testGetDateTimeIdWithPadding() {
        String prefix = "PADTIME" + System.currentTimeMillis();

        String id = SequenceUtils.getDateTimeId(prefix, true, 6);

        assertTrue(id.startsWith(prefix), "ID应该以前缀开头");

        // 验证时间部分后面是6位序号
        String afterPrefix = id.substring(prefix.length());
        String timePart = afterPrefix.substring(0, 14);
        String seqPart = afterPrefix.substring(14);

        assertTrue(timePart.matches("\\d{14}"), "时间部分应该是14位数字");
        assertEquals(6, seqPart.length(), "序号部分应该是6位");
    }

    @Test
    @DisplayName("测试getPaddedDateTimeId-默认补零日期时间ID")
    public void testGetPaddedDateTimeId() {
        String prefix = "DEFDT" + System.currentTimeMillis();

        String id = SequenceUtils.getPaddedDateTimeId(prefix, true);

        assertTrue(id.startsWith(prefix));

        String afterPrefix = id.substring(prefix.length());
        String seqPart = afterPrefix.substring(14);
        assertEquals(6, seqPart.length(), "默认应该补零到6位");
    }

    @Test
    @DisplayName("测试getDateTimeId-指定时间")
    public void testGetDateTimeIdWithSpecificTime() {
        String prefix = "SPEC" + System.currentTimeMillis();
        LocalDateTime specificTime = LocalDateTime.of(2024, 6, 15, 10, 30, 45);

        String id = SequenceUtils.getDateTimeId(prefix, true, 6, specificTime);

        assertTrue(id.contains("20240615103045"), "ID应该包含指定时间");
    }

    // ==================== ID生成器测试 ====================

    @Test
    @DisplayName("测试getIdGenerator-获取ID生成器")
    public void testGetIdGenerator() {
        String key = genTestKey("generator");

        var generator = SequenceUtils.getIdGenerator(key, Duration.ofMinutes(5));

        assertNotNull(generator, "ID生成器不应为null");

        long id1 = generator.nextId();
        long id2 = generator.nextId();

        assertEquals(1, id1);
        assertEquals(2, id2);
    }

    @Test
    @DisplayName("测试getIdGenerator-自定义初始值")
    public void testGetIdGeneratorWithParams() {
        String key = genTestKey("generatorparams");

        // 参数说明：initValue=1000（初始值），allocationSize=10（预分配大小）
        // 注意：直接使用 generator.nextId() 时，ID 始终递增 1
        // allocationSize 是性能优化参数，影响批量预分配，不影响单实例内的递增步长
        var generator = SequenceUtils.getIdGenerator(key, Duration.ofMinutes(5), 1000, 10);

        long id1 = generator.nextId();
        long id2 = generator.nextId();
        long id3 = generator.nextId();

        assertEquals(1000, id1, "初始值应该是1000");
        assertEquals(1001, id2, "第二个ID应该是1001");
        assertEquals(1002, id3, "第三个ID应该是1002");
    }

    // ==================== 并发测试 ====================

    @Test
    @DisplayName("测试ID生成-并发唯一性")
    public void testConcurrentIdUniqueness() throws InterruptedException {
        String key = genTestKey("concurrent");
        int threadCount = 10;
        int idsPerThread = 100;
        Set<Long> allIds = java.util.Collections.synchronizedSet(new HashSet<>());
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    for (int j = 0; j < idsPerThread; j++) {
                        long id = SequenceUtils.getNextId(key, Duration.ofMinutes(5));
                        allIds.add(id);
                    }
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await(60, TimeUnit.SECONDS);

        assertEquals(threadCount * idsPerThread, allIds.size(),
            "并发生成的ID应该全部唯一");
    }

    @Test
    @DisplayName("测试日期ID生成-并发唯一性")
    public void testConcurrentDateIdUniqueness() throws InterruptedException {
        String prefix = "CONC" + System.currentTimeMillis();
        int threadCount = 5;
        int idsPerThread = 50;
        Set<String> allIds = java.util.Collections.synchronizedSet(new HashSet<>());
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    for (int j = 0; j < idsPerThread; j++) {
                        String id = SequenceUtils.getDateId(prefix);
                        allIds.add(id);
                    }
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await(60, TimeUnit.SECONDS);

        assertEquals(threadCount * idsPerThread, allIds.size(),
            "并发生成的日期ID应该全部唯一");
    }

    // ==================== 边界条件测试 ====================

    @Test
    @DisplayName("测试getNextId-初始值为0时使用默认值")
    public void testGetNextIdWithZeroInit() {
        String key = genTestKey("zeroinit");

        // 初始值0应该使用默认值1
        long id = SequenceUtils.getNextId(key, Duration.ofMinutes(5), 0, 1);

        assertEquals(1, id, "初始值0时应该使用默认值1");
    }

    @Test
    @DisplayName("测试getNextId-allocationSize为0时使用默认值")
    public void testGetNextIdWithZeroStep() {
        String key = genTestKey("zerostep");

        // allocationSize 为0时应该使用默认值1（见 SequenceUtils 源码第98行）
        long id1 = SequenceUtils.getNextId(key, Duration.ofMinutes(5), 1, 0);
        long id2 = SequenceUtils.getNextId(key, Duration.ofMinutes(5), 1, 0);

        // 验证ID递增
        assertTrue(id2 > id1, "ID应该递增");
    }

    @Test
    @DisplayName("测试getNextId-负数初始值使用默认值")
    public void testGetNextIdWithNegativeInit() {
        String key = genTestKey("neginit");

        long id = SequenceUtils.getNextId(key, Duration.ofMinutes(5), -10, 1);

        assertEquals(1, id, "负数初始值应该使用默认值1");
    }

    @Test
    @DisplayName("测试getDateId-空前缀")
    public void testGetDateIdWithEmptyPrefix() {
        String id = SequenceUtils.getDateId("");

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        assertTrue(id.startsWith(today), "空前缀时ID应该直接以日期开头");
    }

    @Test
    @DisplayName("测试常量值")
    public void testConstants() {
        assertEquals(1L, SequenceUtils.DEFAULT_INIT_VALUE, "默认初始值应该是1");
        assertEquals(1L, SequenceUtils.DEFAULT_STEP_VALUE, "默认allocationSize应该是1");
        assertEquals(Duration.ofDays(1), SequenceUtils.DEFAULT_EXPIRE_TIME_DAY, "默认天过期时间");
        assertEquals(Duration.ofMinutes(1), SequenceUtils.DEFAULT_EXPIRE_TIME_MINUTE, "默认分钟过期时间");
        assertEquals(6, SequenceUtils.DEFAULT_MIN_ID_CAPACITY_BITS, "默认最小ID位数应该是6");
    }
}
