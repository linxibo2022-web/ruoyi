package plus.ruoyi.common.redis.utils;

import plus.ruoyi.common.test.base.BaseSpringTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.redisson.api.RateType;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RedisUtils Redis工具类测试
 *
 * 需要启动 Spring 容器和 Redis 连接
 *
 * @author 抓蛙师
 */
@DisplayName("RedisUtils工具类测试")
public class RedisUtilsTest extends BaseSpringTest {

    /**
     * 测试使用的key前缀，用于清理测试数据
     */
    private static final String TEST_KEY_PREFIX = "test:redis:utils:";

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

    // ==================== 基础缓存操作测试 ====================

    @Test
    @DisplayName("测试setCacheObject/getCacheObject-字符串类型")
    public void testSetGetCacheObjectString() {
        String key = genTestKey("string");
        String value = "测试字符串值";

        // 设置缓存
        RedisUtils.setCacheObject(key, value);

        // 获取缓存
        String result = RedisUtils.getCacheObject(key);

        assertEquals(value, result, "获取的值应与设置的值一致");
    }

    @Test
    @DisplayName("测试setCacheObject/getCacheObject-对象类型")
    public void testSetGetCacheObjectPojo() {
        String key = genTestKey("object");
        TestUser user = new TestUser(1L, "张三", 25);

        // 设置缓存
        RedisUtils.setCacheObject(key, user);

        // 获取缓存
        TestUser result = RedisUtils.getCacheObject(key);

        assertNotNull(result, "获取的对象不应为null");
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getName(), result.getName());
        assertEquals(user.getAge(), result.getAge());
    }

    @Test
    @DisplayName("测试setCacheObject-带过期时间")
    public void testSetCacheObjectWithExpire() throws InterruptedException {
        String key = genTestKey("expire");
        String value = "即将过期的值";

        // 设置2秒过期
        RedisUtils.setCacheObject(key, value, Duration.ofSeconds(2));

        // 立即获取应该存在
        assertNotNull(RedisUtils.getCacheObject(key), "设置后立即获取应该存在");

        // 等待3秒后应该过期
        TimeUnit.SECONDS.sleep(3);
        assertNull(RedisUtils.getCacheObject(key), "过期后应该返回null");
    }

    @Test
    @DisplayName("测试getCacheObject-获取不存在的key应返回null")
    public void testGetCacheObjectNotExists() {
        String key = genTestKey("notexists");

        Object result = RedisUtils.getCacheObject(key);

        assertNull(result, "不存在的key应该返回null");
    }

    @Test
    @DisplayName("测试setObjectIfAbsent-key不存在时设置成功")
    public void testSetObjectIfAbsentSuccess() {
        String key = genTestKey("ifabsent");
        String value = "条件设置的值";

        boolean result = RedisUtils.setObjectIfAbsent(key, value, Duration.ofMinutes(1));

        assertTrue(result, "key不存在时设置应该成功");
        assertEquals(value, RedisUtils.getCacheObject(key));
    }

    @Test
    @DisplayName("测试setObjectIfAbsent-key已存在时设置失败")
    public void testSetObjectIfAbsentFail() {
        String key = genTestKey("ifabsent2");

        // 先设置一个值
        RedisUtils.setCacheObject(key, "原始值");

        // 尝试条件设置
        boolean result = RedisUtils.setObjectIfAbsent(key, "新值", Duration.ofMinutes(1));

        assertFalse(result, "key已存在时设置应该失败");
        assertEquals("原始值", RedisUtils.getCacheObject(key), "原值应该保持不变");
    }

    @Test
    @DisplayName("测试setObjectIfExists-key存在时设置成功")
    public void testSetObjectIfExistsSuccess() {
        String key = genTestKey("ifexists");

        // 先设置一个值
        RedisUtils.setCacheObject(key, "原始值");

        // 条件设置
        boolean result = RedisUtils.setObjectIfExists(key, "新值", Duration.ofMinutes(1));

        assertTrue(result, "key存在时设置应该成功");
        assertEquals("新值", RedisUtils.getCacheObject(key));
    }

    @Test
    @DisplayName("测试setObjectIfExists-key不存在时设置失败")
    public void testSetObjectIfExistsFail() {
        String key = genTestKey("ifexists2");

        boolean result = RedisUtils.setObjectIfExists(key, "新值", Duration.ofMinutes(1));

        assertFalse(result, "key不存在时设置应该失败");
        assertNull(RedisUtils.getCacheObject(key));
    }

    @Test
    @DisplayName("测试deleteObject-删除存在的key")
    public void testDeleteObjectExists() {
        String key = genTestKey("delete");
        RedisUtils.setCacheObject(key, "待删除的值");

        boolean result = RedisUtils.deleteObject(key);

        assertTrue(result, "删除存在的key应该返回true");
        assertNull(RedisUtils.getCacheObject(key), "删除后应该获取不到");
    }

    @Test
    @DisplayName("测试deleteObject-删除不存在的key")
    public void testDeleteObjectNotExists() {
        String key = genTestKey("deletenotexists");

        boolean result = RedisUtils.deleteObject(key);

        assertFalse(result, "删除不存在的key应该返回false");
    }

    @Test
    @DisplayName("测试isExistsObject-key存在")
    public void testIsExistsObjectTrue() {
        String key = genTestKey("exists");
        RedisUtils.setCacheObject(key, "存在的值");

        boolean result = RedisUtils.isExistsObject(key);

        assertTrue(result, "存在的key应该返回true");
    }

    @Test
    @DisplayName("测试isExistsObject-key不存在")
    public void testIsExistsObjectFalse() {
        String key = genTestKey("notexists2");

        boolean result = RedisUtils.isExistsObject(key);

        assertFalse(result, "不存在的key应该返回false");
    }

    @Test
    @DisplayName("测试hasKey-key存在")
    public void testHasKeyTrue() {
        String key = genTestKey("haskey");
        RedisUtils.setCacheObject(key, "存在的值");

        Boolean result = RedisUtils.hasKey(key);

        assertTrue(result, "存在的key应该返回true");
    }

    @Test
    @DisplayName("测试hasKey-key不存在")
    public void testHasKeyFalse() {
        String key = genTestKey("haskey2");

        Boolean result = RedisUtils.hasKey(key);

        assertFalse(result, "不存在的key应该返回false");
    }

    @Test
    @DisplayName("测试expire-设置过期时间")
    public void testExpire() {
        String key = genTestKey("setexpire");
        RedisUtils.setCacheObject(key, "测试值");

        // 设置10秒过期
        boolean result = RedisUtils.expire(key, 10);

        assertTrue(result, "设置过期时间应该成功");

        // 验证TTL
        long ttl = RedisUtils.getTimeToLive(key);
        assertTrue(ttl > 0 && ttl <= 10000, "TTL应该在0-10000毫秒之间");
    }

    @Test
    @DisplayName("测试getTimeToLive-获取TTL")
    public void testGetTimeToLive() {
        String key = genTestKey("ttl");
        RedisUtils.setCacheObject(key, "测试值", Duration.ofSeconds(60));

        long ttl = RedisUtils.getTimeToLive(key);

        assertTrue(ttl > 50000 && ttl <= 60000, "TTL应该在50-60秒之间");
    }

    @Test
    @DisplayName("测试getTimeToLive-永不过期的key")
    public void testGetTimeToLiveNoExpire() {
        String key = genTestKey("ttlnoexpire");
        RedisUtils.setCacheObject(key, "测试值");

        long ttl = RedisUtils.getTimeToLive(key);

        assertEquals(-1, ttl, "永不过期的key TTL应该为-1");
    }

    // ==================== List操作测试 ====================

    @Test
    @DisplayName("测试setCacheList/getCacheList-存取List")
    public void testSetGetCacheList() {
        String key = genTestKey("list");
        List<String> dataList = Arrays.asList("item1", "item2", "item3");

        // 设置List
        boolean result = RedisUtils.setCacheList(key, dataList);
        assertTrue(result, "设置List应该成功");

        // 获取List
        List<String> resultList = RedisUtils.getCacheList(key);

        assertNotNull(resultList, "获取的List不应为null");
        assertEquals(3, resultList.size(), "List大小应该为3");
        assertTrue(resultList.containsAll(dataList), "应该包含所有元素");
    }

    @Test
    @DisplayName("测试setCacheList-带过期时间")
    public void testSetCacheListWithExpire() throws InterruptedException {
        String key = genTestKey("listexpire");
        List<String> dataList = Arrays.asList("a", "b", "c");

        // 设置2秒过期
        RedisUtils.setCacheList(key, dataList, Duration.ofSeconds(2));

        // 立即获取应该存在
        assertFalse(RedisUtils.getCacheList(key).isEmpty(), "设置后立即获取应该存在");

        // 等待3秒后应该过期
        TimeUnit.SECONDS.sleep(3);
        assertTrue(RedisUtils.getCacheList(key).isEmpty(), "过期后应该为空");
    }

    @Test
    @DisplayName("测试addCacheList-向List追加元素")
    public void testAddCacheList() {
        String key = genTestKey("listadd");
        RedisUtils.setCacheList(key, Arrays.asList("item1", "item2"));

        // 追加元素
        boolean result = RedisUtils.addCacheList(key, "item3");

        assertTrue(result, "追加元素应该成功");
        List<String> list = RedisUtils.getCacheList(key);
        assertEquals(3, list.size());
        assertTrue(list.contains("item3"));
    }

    @Test
    @DisplayName("测试getCacheListRange-获取List范围")
    public void testGetCacheListRange() {
        String key = genTestKey("listrange");
        RedisUtils.setCacheList(key, Arrays.asList("a", "b", "c", "d", "e"));

        // 获取范围 [1, 3]
        List<String> range = RedisUtils.getCacheListRange(key, 1, 3);

        assertEquals(3, range.size(), "范围应该包含3个元素");
        assertEquals("b", range.get(0));
        assertEquals("c", range.get(1));
        assertEquals("d", range.get(2));
    }

    // ==================== Set操作测试 ====================

    @Test
    @DisplayName("测试setCacheSet/getCacheSet-存取Set")
    public void testSetGetCacheSet() {
        String key = genTestKey("set");
        Set<String> dataSet = new HashSet<>(Arrays.asList("a", "b", "c"));

        // 设置Set
        boolean result = RedisUtils.setCacheSet(key, dataSet);
        assertTrue(result, "设置Set应该成功");

        // 获取Set
        Set<String> resultSet = RedisUtils.getCacheSet(key);

        assertNotNull(resultSet, "获取的Set不应为null");
        assertEquals(3, resultSet.size(), "Set大小应该为3");
        assertTrue(resultSet.containsAll(dataSet), "应该包含所有元素");
    }

    @Test
    @DisplayName("测试addCacheSet-向Set追加元素")
    public void testAddCacheSet() {
        String key = genTestKey("setadd");
        RedisUtils.setCacheSet(key, new HashSet<>(Arrays.asList("a", "b")));

        // 追加新元素
        boolean result = RedisUtils.addCacheSet(key, "c");
        assertTrue(result, "追加新元素应该成功");

        // 追加已存在的元素
        boolean result2 = RedisUtils.addCacheSet(key, "a");
        assertFalse(result2, "追加已存在的元素应该返回false");

        Set<String> set = RedisUtils.getCacheSet(key);
        assertEquals(3, set.size());
    }

    // ==================== Map操作测试 ====================

    @Test
    @DisplayName("测试setCacheMap/getCacheMap-存取Map")
    public void testSetGetCacheMap() {
        String key = genTestKey("map");
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("name", "张三");
        dataMap.put("age", "25");
        dataMap.put("city", "北京");

        // 设置Map
        RedisUtils.setCacheMap(key, dataMap);

        // 获取Map
        Map<String, String> resultMap = RedisUtils.getCacheMap(key);

        assertNotNull(resultMap, "获取的Map不应为null");
        assertEquals(3, resultMap.size(), "Map大小应该为3");
        assertEquals("张三", resultMap.get("name"));
        assertEquals("25", resultMap.get("age"));
        assertEquals("北京", resultMap.get("city"));
    }

    @Test
    @DisplayName("测试setCacheMapValue/getCacheMapValue-存取Map单个值")
    public void testSetGetCacheMapValue() {
        String key = genTestKey("mapvalue");

        // 设置单个值
        RedisUtils.setCacheMapValue(key, "field1", "value1");
        RedisUtils.setCacheMapValue(key, "field2", "value2");

        // 获取单个值
        String value1 = RedisUtils.getCacheMapValue(key, "field1");
        String value2 = RedisUtils.getCacheMapValue(key, "field2");

        assertEquals("value1", value1);
        assertEquals("value2", value2);
    }

    @Test
    @DisplayName("测试delCacheMapValue-删除Map单个值")
    public void testDelCacheMapValue() {
        String key = genTestKey("mapdel");
        RedisUtils.setCacheMapValue(key, "field1", "value1");
        RedisUtils.setCacheMapValue(key, "field2", "value2");

        // 删除field1
        String deleted = RedisUtils.delCacheMapValue(key, "field1");

        assertEquals("value1", deleted, "删除应该返回被删除的值");
        assertNull(RedisUtils.getCacheMapValue(key, "field1"), "删除后应该获取不到");
        assertNotNull(RedisUtils.getCacheMapValue(key, "field2"), "其他字段应该还在");
    }

    @Test
    @DisplayName("测试getCacheMapKeySet-获取Map所有key")
    public void testGetCacheMapKeySet() {
        String key = genTestKey("mapkeys");
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("k1", "v1");
        dataMap.put("k2", "v2");
        dataMap.put("k3", "v3");
        RedisUtils.setCacheMap(key, dataMap);

        Set<String> keySet = RedisUtils.getCacheMapKeySet(key);

        assertEquals(3, keySet.size());
        assertTrue(keySet.contains("k1"));
        assertTrue(keySet.contains("k2"));
        assertTrue(keySet.contains("k3"));
    }

    @Test
    @DisplayName("测试getMultiCacheMapValue-批量获取Map值")
    public void testGetMultiCacheMapValue() {
        String key = genTestKey("mapmulti");
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("k1", "v1");
        dataMap.put("k2", "v2");
        dataMap.put("k3", "v3");
        RedisUtils.setCacheMap(key, dataMap);

        // 批量获取
        Set<String> hKeys = new HashSet<>(Arrays.asList("k1", "k3"));
        Map<String, String> result = RedisUtils.getMultiCacheMapValue(key, hKeys);

        assertEquals(2, result.size());
        assertEquals("v1", result.get("k1"));
        assertEquals("v3", result.get("k3"));
    }

    // ==================== 原子操作测试 ====================

    @Test
    @DisplayName("测试setAtomicValue/getAtomicValue-原子值操作")
    public void testSetGetAtomicValue() {
        String key = genTestKey("atomic");

        RedisUtils.setAtomicValue(key, 100);
        long value = RedisUtils.getAtomicValue(key);

        assertEquals(100, value, "原子值应该为100");
    }

    @Test
    @DisplayName("测试incrAtomicValue-原子递增")
    public void testIncrAtomicValue() {
        String key = genTestKey("atomicincr");
        RedisUtils.setAtomicValue(key, 10);

        long result1 = RedisUtils.incrAtomicValue(key);
        long result2 = RedisUtils.incrAtomicValue(key);
        long result3 = RedisUtils.incrAtomicValue(key);

        assertEquals(11, result1);
        assertEquals(12, result2);
        assertEquals(13, result3);
    }

    @Test
    @DisplayName("测试decrAtomicValue-原子递减")
    public void testDecrAtomicValue() {
        String key = genTestKey("atomicdecr");
        RedisUtils.setAtomicValue(key, 10);

        long result1 = RedisUtils.decrAtomicValue(key);
        long result2 = RedisUtils.decrAtomicValue(key);

        assertEquals(9, result1);
        assertEquals(8, result2);
    }

    @Test
    @DisplayName("测试原子操作-并发安全性")
    public void testAtomicConcurrency() throws InterruptedException {
        String key = genTestKey("atomicconcurrent");
        RedisUtils.setAtomicValue(key, 0);

        int threadCount = 10;
        int incrPerThread = 100;
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    for (int j = 0; j < incrPerThread; j++) {
                        RedisUtils.incrAtomicValue(key);
                    }
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await(30, TimeUnit.SECONDS);
        long finalValue = RedisUtils.getAtomicValue(key);

        assertEquals(threadCount * incrPerThread, finalValue, "并发递增后的值应该正确");
    }

    // ==================== 发布订阅测试 ====================

    @Test
    @DisplayName("测试publish/subscribe-发布订阅")
    public void testPubSub() throws InterruptedException {
        String channel = genTestKey("pubsub");
        AtomicReference<String> receivedMsg = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        // 订阅
        RedisUtils.subscribe(channel, String.class, msg -> {
            receivedMsg.set(msg);
            latch.countDown();
        });

        // 等待订阅生效
        TimeUnit.MILLISECONDS.sleep(500);

        // 发布
        RedisUtils.publish(channel, "Hello Redis!");

        // 等待接收
        boolean received = latch.await(5, TimeUnit.SECONDS);

        assertTrue(received, "应该在5秒内收到消息");
        assertEquals("Hello Redis!", receivedMsg.get(), "收到的消息应该正确");
    }

    // ==================== 限流测试 ====================

    @Test
    @DisplayName("测试rateLimiter-基本限流功能")
    public void testRateLimiter() {
        String key = genTestKey("ratelimit");

        // 设置限流：每1秒允许3个请求
        long result1 = RedisUtils.rateLimiter(key, RateType.OVERALL, 3, 1);
        long result2 = RedisUtils.rateLimiter(key, RateType.OVERALL, 3, 1);
        long result3 = RedisUtils.rateLimiter(key, RateType.OVERALL, 3, 1);
        long result4 = RedisUtils.rateLimiter(key, RateType.OVERALL, 3, 1);

        // 前3个应该成功
        assertTrue(result1 >= 0, "第1个请求应该成功");
        assertTrue(result2 >= 0, "第2个请求应该成功");
        assertTrue(result3 >= 0, "第3个请求应该成功");
        // 第4个应该被限流
        assertEquals(-1, result4, "第4个请求应该被限流");
    }

    // ==================== Key操作测试 ====================

    @Test
    @DisplayName("测试keys-按模式匹配key")
    public void testKeys() {
        String prefix = genTestKey("keyspattern");
        // 创建多个key
        RedisUtils.setCacheObject(prefix + ":1", "v1");
        RedisUtils.setCacheObject(prefix + ":2", "v2");
        RedisUtils.setCacheObject(prefix + ":3", "v3");
        testKeys.add(prefix + ":1");
        testKeys.add(prefix + ":2");
        testKeys.add(prefix + ":3");

        // 按模式查找
        Collection<String> keys = RedisUtils.keys(prefix + ":*");

        assertTrue(keys.size() >= 3, "应该至少找到3个匹配的key");
    }

    @Test
    @DisplayName("测试deleteKeys-按模式删除key")
    public void testDeleteKeys() {
        String prefix = genTestKey("deletekeyspattern");
        // 创建多个key
        RedisUtils.setCacheObject(prefix + ":1", "v1");
        RedisUtils.setCacheObject(prefix + ":2", "v2");
        RedisUtils.setCacheObject(prefix + ":3", "v3");

        // 按模式删除
        RedisUtils.deleteKeys(prefix + ":*");

        // 验证已删除
        assertNull(RedisUtils.getCacheObject(prefix + ":1"));
        assertNull(RedisUtils.getCacheObject(prefix + ":2"));
        assertNull(RedisUtils.getCacheObject(prefix + ":3"));
    }

    @Test
    @DisplayName("测试getClient-获取Redisson客户端")
    public void testGetClient() {
        assertNotNull(RedisUtils.getClient(), "应该能获取到Redisson客户端");
    }

    @Test
    @DisplayName("测试getLock-获取分布式锁")
    public void testGetLock() {
        String key = genTestKey("lock");

        var lock = RedisUtils.getLock(key);

        assertNotNull(lock, "应该能获取到锁对象");
    }

    // ==================== 批量删除测试 ====================

    @Test
    @DisplayName("测试deleteObject-批量删除")
    public void testBatchDeleteObject() {
        String key1 = genTestKey("batch1");
        String key2 = genTestKey("batch2");
        String key3 = genTestKey("batch3");

        RedisUtils.setCacheObject(key1, "v1");
        RedisUtils.setCacheObject(key2, "v2");
        RedisUtils.setCacheObject(key3, "v3");

        // 批量删除
        RedisUtils.deleteObject(Arrays.asList(key1, key2, key3));

        // 验证已删除
        assertNull(RedisUtils.getCacheObject(key1));
        assertNull(RedisUtils.getCacheObject(key2));
        assertNull(RedisUtils.getCacheObject(key3));
    }

    // ==================== 测试辅助类 ====================

    /**
     * 测试用的用户对象
     */
    public static class TestUser implements java.io.Serializable {
        private static final long serialVersionUID = 1L;

        private Long id;
        private String name;
        private Integer age;

        public TestUser() {
        }

        public TestUser(Long id, String name, Integer age) {
            this.id = id;
            this.name = name;
            this.age = age;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }
    }
}
