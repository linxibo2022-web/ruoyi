package plus.ruoyi.common.redis.utils;

import plus.ruoyi.common.test.base.BaseSpringTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CacheUtils 缓存操作工具类测试
 *
 * 需要启动 Spring 容器和缓存配置
 *
 * @author 抓蛙师
 */
@DisplayName("CacheUtils缓存工具类测试")
public class CacheUtilsTest extends BaseSpringTest {

    /**
     * 测试使用的缓存组名称
     */
    private static final String TEST_CACHE_NAME = "testCache";

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
                CacheUtils.evict(TEST_CACHE_NAME, key);
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 生成测试key并记录
     */
    private String genTestKey(String suffix) {
        String key = "test:" + suffix + ":" + System.currentTimeMillis();
        testKeys.add(key);
        return key;
    }

    // ==================== put/get 测试 ====================

    @Test
    @DisplayName("测试put/get-字符串类型")
    public void testPutGetString() {
        String key = genTestKey("string");
        String value = "测试字符串值";

        // 存储
        CacheUtils.put(TEST_CACHE_NAME, key, value);

        // 获取
        String result = CacheUtils.get(TEST_CACHE_NAME, key);

        assertEquals(value, result, "获取的值应与存储的值一致");
    }

    @Test
    @DisplayName("测试put/get-整数类型")
    public void testPutGetInteger() {
        String key = genTestKey("integer");
        Integer value = 12345;

        CacheUtils.put(TEST_CACHE_NAME, key, value);
        Integer result = CacheUtils.get(TEST_CACHE_NAME, key);

        assertEquals(value, result, "获取的整数应与存储的值一致");
    }

    @Test
    @DisplayName("测试put/get-对象类型")
    public void testPutGetObject() {
        String key = genTestKey("object");
        TestCacheUser user = new TestCacheUser(1L, "张三", 25);

        CacheUtils.put(TEST_CACHE_NAME, key, user);
        TestCacheUser result = CacheUtils.get(TEST_CACHE_NAME, key);

        assertNotNull(result, "获取的对象不应为null");
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getName(), result.getName());
        assertEquals(user.getAge(), result.getAge());
    }

    @Test
    @DisplayName("测试get-获取不存在的key应返回null")
    public void testGetNotExists() {
        String key = genTestKey("notexists");

        Object result = CacheUtils.get(TEST_CACHE_NAME, key);

        assertNull(result, "不存在的key应该返回null");
    }

    @Test
    @DisplayName("测试put-覆盖已存在的值")
    public void testPutOverwrite() {
        String key = genTestKey("overwrite");

        // 第一次存储
        CacheUtils.put(TEST_CACHE_NAME, key, "原始值");
        assertEquals("原始值", CacheUtils.get(TEST_CACHE_NAME, key));

        // 覆盖存储
        CacheUtils.put(TEST_CACHE_NAME, key, "新值");
        assertEquals("新值", CacheUtils.get(TEST_CACHE_NAME, key), "应该被新值覆盖");
    }

    @Test
    @DisplayName("测试put-存储null值")
    public void testPutNullValue() {
        String key = genTestKey("nullvalue");

        // 先存储一个值
        CacheUtils.put(TEST_CACHE_NAME, key, "原始值");
        assertNotNull(CacheUtils.get(TEST_CACHE_NAME, key));

        // 存储null
        CacheUtils.put(TEST_CACHE_NAME, key, null);

        // 获取应该返回null
        assertNull(CacheUtils.get(TEST_CACHE_NAME, key), "存储null后获取应该返回null");
    }

    // ==================== evict 测试 ====================

    @Test
    @DisplayName("测试evict-删除存在的缓存项")
    public void testEvictExists() {
        String key = genTestKey("evict");
        CacheUtils.put(TEST_CACHE_NAME, key, "待删除的值");

        // 验证存在
        assertNotNull(CacheUtils.get(TEST_CACHE_NAME, key));

        // 删除
        CacheUtils.evict(TEST_CACHE_NAME, key);

        // 验证已删除
        assertNull(CacheUtils.get(TEST_CACHE_NAME, key), "删除后应该获取不到");
    }

    @Test
    @DisplayName("测试evict-删除不存在的缓存项不报错")
    public void testEvictNotExists() {
        String key = genTestKey("evictnotexists");

        // 删除不存在的key，不应抛出异常
        assertDoesNotThrow(() -> CacheUtils.evict(TEST_CACHE_NAME, key),
            "删除不存在的key不应抛出异常");
    }

    // ==================== clear 测试 ====================

    @Test
    @DisplayName("测试clear-清空缓存组")
    public void testClear() {
        String key1 = genTestKey("clear1");
        String key2 = genTestKey("clear2");
        String key3 = genTestKey("clear3");

        // 存储多个值
        CacheUtils.put(TEST_CACHE_NAME, key1, "value1");
        CacheUtils.put(TEST_CACHE_NAME, key2, "value2");
        CacheUtils.put(TEST_CACHE_NAME, key3, "value3");

        // 清空缓存组
        CacheUtils.clear(TEST_CACHE_NAME);

        // 验证全部清空
        assertNull(CacheUtils.get(TEST_CACHE_NAME, key1), "清空后key1应该为null");
        assertNull(CacheUtils.get(TEST_CACHE_NAME, key2), "清空后key2应该为null");
        assertNull(CacheUtils.get(TEST_CACHE_NAME, key3), "清空后key3应该为null");
    }

    // ==================== 不同缓存组隔离测试 ====================

    @Test
    @DisplayName("测试缓存组隔离-不同缓存组数据互不影响")
    public void testCacheNameIsolation() {
        String anotherCacheName = "anotherTestCache";
        String key = genTestKey("isolation");

        // 在testCache中存储
        CacheUtils.put(TEST_CACHE_NAME, key, "testCache的值");

        // 在anotherTestCache中存储
        CacheUtils.put(anotherCacheName, key, "anotherTestCache的值");

        // 验证两个缓存组的值不同
        assertEquals("testCache的值", CacheUtils.get(TEST_CACHE_NAME, key));
        assertEquals("anotherTestCache的值", CacheUtils.get(anotherCacheName, key));

        // 清理
        CacheUtils.evict(anotherCacheName, key);
    }

    // ==================== 边界条件测试 ====================

    @Test
    @DisplayName("测试put/get-使用整数作为key")
    public void testIntegerKey() {
        Integer key = 12345;
        String value = "整数key的值";
        testKeys.add(String.valueOf(key));

        CacheUtils.put(TEST_CACHE_NAME, key, value);
        String result = CacheUtils.get(TEST_CACHE_NAME, key);

        assertEquals(value, result, "使用整数key应该正常工作");
    }

    @Test
    @DisplayName("测试put/get-使用Long作为key")
    public void testLongKey() {
        Long key = 9999999999L;
        String value = "Long类型key的值";
        testKeys.add(String.valueOf(key));

        CacheUtils.put(TEST_CACHE_NAME, key, value);
        String result = CacheUtils.get(TEST_CACHE_NAME, key);

        assertEquals(value, result, "使用Long类型key应该正常工作");
    }

    @Test
    @DisplayName("测试put/get-key包含特殊字符")
    public void testSpecialCharKey() {
        String key = genTestKey("special:key:with:colons");

        CacheUtils.put(TEST_CACHE_NAME, key, "特殊字符key的值");
        String result = CacheUtils.get(TEST_CACHE_NAME, key);

        assertEquals("特殊字符key的值", result, "包含特殊字符的key应该正常工作");
    }

    @Test
    @DisplayName("测试put/get-存储空字符串")
    public void testEmptyStringValue() {
        String key = genTestKey("emptystring");

        CacheUtils.put(TEST_CACHE_NAME, key, "");
        String result = CacheUtils.get(TEST_CACHE_NAME, key);

        assertEquals("", result, "空字符串应该正常存取");
    }

    @Test
    @DisplayName("测试put/get-存储对象List")
    public void testListValue() {
        String key = genTestKey("list");

        // 使用 ArrayList 存储可序列化对象，避免 Jackson 类型解析问题
        List<TestCacheUser> value = new ArrayList<>();
        value.add(new TestCacheUser(1L, "用户1", 20));
        value.add(new TestCacheUser(2L, "用户2", 25));
        value.add(new TestCacheUser(3L, "用户3", 30));

        CacheUtils.put(TEST_CACHE_NAME, key, value);
        List<TestCacheUser> result = CacheUtils.get(TEST_CACHE_NAME, key);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("用户1", result.get(0).getName());
        assertEquals(2L, result.get(1).getId());
        assertEquals(3L, result.get(2).getId());
    }

    // ==================== 测试辅助类 ====================

    /**
     * 测试用的用户对象
     */
    public static class TestCacheUser implements java.io.Serializable {
        private static final long serialVersionUID = 1L;

        private Long id;
        private String name;
        private Integer age;

        public TestCacheUser() {
        }

        public TestCacheUser(Long id, String name, Integer age) {
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
