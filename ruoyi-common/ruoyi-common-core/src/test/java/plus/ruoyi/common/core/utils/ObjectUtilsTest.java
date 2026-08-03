package plus.ruoyi.common.core.utils;

import plus.ruoyi.common.test.base.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import lombok.Data;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ObjectUtils 对象工具测试
 *
 * @author 抓蛙师
 */
@DisplayName("ObjectUtils对象工具测试")
public class ObjectUtilsTest extends BaseUnitTest {

    // ==================== 测试用实体类 ====================

    /**
     * 用户类
     */
    @Data
    static class User {
        private Long id;
        private String username;
        private String email;
        private Integer age;
        private Address address;
    }

    /**
     * 地址类
     */
    @Data
    static class Address {
        private String province;
        private String city;
        private String detail;
    }

    // ==================== getIfNotNull (无默认值) 测试 ====================

    @Test
    @DisplayName("测试getIfNotNull-正常获取对象字段值")
    public void testGetIfNotNullNormalCase() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setAge(25);

        // 获取各种字段
        Long id = ObjectUtils.getIfNotNull(user, User::getId);
        assertEquals(1L, id);

        String username = ObjectUtils.getIfNotNull(user, User::getUsername);
        assertEquals("testuser", username);

        String email = ObjectUtils.getIfNotNull(user, User::getEmail);
        assertEquals("test@example.com", email);

        Integer age = ObjectUtils.getIfNotNull(user, User::getAge);
        assertEquals(25, age);
    }

    @Test
    @DisplayName("测试getIfNotNull-对象为null应返回null")
    public void testGetIfNotNullWithNullObject() {
        User user = null;

        String username = ObjectUtils.getIfNotNull(user, User::getUsername);
        assertNull(username, "对象为null时应返回null");

        Long id = ObjectUtils.getIfNotNull(user, User::getId);
        assertNull(id, "对象为null时应返回null");
    }

    @Test
    @DisplayName("测试getIfNotNull-getter为null应返回null")
    public void testGetIfNotNullWithNullGetter() {
        User user = new User();
        user.setUsername("testuser");

        String result = ObjectUtils.getIfNotNull(user, null);
        assertNull(result, "getter为null时应返回null");
    }

    @Test
    @DisplayName("测试getIfNotNull-字段值为null")
    public void testGetIfNotNullWithNullFieldValue() {
        User user = new User();
        user.setId(1L);
        // username 未设置，为null

        String username = ObjectUtils.getIfNotNull(user, User::getUsername);
        assertNull(username, "字段值为null时应返回null");
    }

    @Test
    @DisplayName("测试getIfNotNull-获取嵌套对象")
    public void testGetIfNotNullWithNestedObject() {
        User user = new User();
        Address address = new Address();
        address.setCity("深圳市");
        user.setAddress(address);

        // 获取嵌套对象
        Address addr = ObjectUtils.getIfNotNull(user, User::getAddress);
        assertNotNull(addr);
        assertEquals("深圳市", addr.getCity());
    }

    @Test
    @DisplayName("测试getIfNotNull-嵌套对象为null")
    public void testGetIfNotNullWithNullNestedObject() {
        User user = new User();
        user.setId(1L);
        // address 未设置，为null

        Address address = ObjectUtils.getIfNotNull(user, User::getAddress);
        assertNull(address, "嵌套对象为null时应返回null");
    }

    @Test
    @DisplayName("测试getIfNotNull-链式安全访问")
    public void testGetIfNotNullChainSafeAccess() {
        User user = new User();
        Address address = new Address();
        address.setCity("深圳市");
        user.setAddress(address);

        // 链式安全访问嵌套属性
        String city = ObjectUtils.getIfNotNull(
            ObjectUtils.getIfNotNull(user, User::getAddress),
            Address::getCity
        );

        assertEquals("深圳市", city);
    }

    @Test
    @DisplayName("测试getIfNotNull-链式访问中间对象为null")
    public void testGetIfNotNullChainWithNullMiddleObject() {
        User user = new User();
        user.setId(1L);
        // address 为null

        // 链式访问，中间对象为null
        String city = ObjectUtils.getIfNotNull(
            ObjectUtils.getIfNotNull(user, User::getAddress),
            Address::getCity
        );

        assertNull(city, "中间对象为null时，链式访问应返回null");
    }

    // ==================== getIfNotNull (有默认值) 测试 ====================

    @Test
    @DisplayName("测试getIfNotNull-正常获取字段值，不使用默认值")
    public void testGetIfNotNullWithDefaultNormalCase() {
        User user = new User();
        user.setUsername("testuser");
        user.setAge(25);

        String username = ObjectUtils.getIfNotNull(user, User::getUsername, "默认用户");
        assertEquals("testuser", username, "有值时应返回实际值而不是默认值");

        Integer age = ObjectUtils.getIfNotNull(user, User::getAge, 18);
        assertEquals(25, age, "有值时应返回实际值而不是默认值");
    }

    @Test
    @DisplayName("测试getIfNotNull-对象为null应返回默认值")
    public void testGetIfNotNullWithDefaultNullObject() {
        User user = null;

        String username = ObjectUtils.getIfNotNull(user, User::getUsername, "默认用户");
        assertEquals("默认用户", username, "对象为null时应返回默认值");

        Integer age = ObjectUtils.getIfNotNull(user, User::getAge, 18);
        assertEquals(18, age, "对象为null时应返回默认值");
    }

    @Test
    @DisplayName("测试getIfNotNull-getter为null应返回默认值")
    public void testGetIfNotNullWithDefaultNullGetter() {
        User user = new User();
        user.setUsername("testuser");

        String result = ObjectUtils.getIfNotNull(user, null, "默认值");
        assertEquals("默认值", result, "getter为null时应返回默认值");
    }

    @Test
    @DisplayName("测试getIfNotNull-字段值为null时返回字段值null而非默认值")
    public void testGetIfNotNullWithDefaultNullFieldValue() {
        User user = new User();
        user.setId(1L);
        // username 未设置，为null

        // 注意：字段值为null时，方法返回null而不是defaultValue
        // 因为对象和getter都不为null，会执行getter.apply(obj)
        String username = ObjectUtils.getIfNotNull(user, User::getUsername, "默认用户");
        assertNull(username, "字段值为null时返回null（不是默认值）");
    }

    @Test
    @DisplayName("测试getIfNotNull-默认值为null")
    public void testGetIfNotNullWithNullDefaultValue() {
        User user = null;

        String username = ObjectUtils.getIfNotNull(user, User::getUsername, null);
        assertNull(username, "默认值为null时应返回null");
    }

    @Test
    @DisplayName("测试getIfNotNull-多种类型的默认值")
    public void testGetIfNotNullWithVariousDefaultValues() {
        User user = null;

        // String 默认值
        String str = ObjectUtils.getIfNotNull(user, User::getUsername, "未知");
        assertEquals("未知", str);

        // Integer 默认值
        Integer num = ObjectUtils.getIfNotNull(user, User::getAge, 0);
        assertEquals(0, num);

        // Long 默认值
        Long id = ObjectUtils.getIfNotNull(user, User::getId, -1L);
        assertEquals(-1L, id);

        // 对象默认值
        Address defaultAddress = new Address();
        defaultAddress.setCity("默认城市");
        Address address = ObjectUtils.getIfNotNull(user, User::getAddress, defaultAddress);
        assertEquals("默认城市", address.getCity());
    }

    @Test
    @DisplayName("测试getIfNotNull-实际业务场景示例")
    public void testGetIfNotNullRealWorldScenario() {
        // 场景1：用户可能为null，需要显示默认名称
        User user1 = null;
        String displayName1 = ObjectUtils.getIfNotNull(user1, User::getUsername, "游客");
        assertEquals("游客", displayName1);

        // 场景2：用户存在但未设置年龄，使用默认年龄
        User user2 = new User();
        user2.setUsername("张三");
        Integer displayAge = ObjectUtils.getIfNotNull(user2, User::getAge, 18);
        assertNull(displayAge, "字段值为null时返回null");

        // 场景3：正常用户，使用实际值
        User user3 = new User();
        user3.setUsername("李四");
        user3.setAge(25);
        String displayName3 = ObjectUtils.getIfNotNull(user3, User::getUsername, "游客");
        assertEquals("李四", displayName3);
    }

    // ==================== 对比测试：有默认值 vs 无默认值 ====================

    @Test
    @DisplayName("测试对比-有默认值vs无默认值")
    public void testComparisonWithAndWithoutDefault() {
        User user = null;

        // 无默认值版本
        String result1 = ObjectUtils.getIfNotNull(user, User::getUsername);
        assertNull(result1);

        // 有默认值版本
        String result2 = ObjectUtils.getIfNotNull(user, User::getUsername, "默认用户");
        assertEquals("默认用户", result2);

        // 两者在对象不为null时的行为一致
        User validUser = new User();
        validUser.setUsername("testuser");

        String result3 = ObjectUtils.getIfNotNull(validUser, User::getUsername);
        String result4 = ObjectUtils.getIfNotNull(validUser, User::getUsername, "默认用户");
        assertEquals(result3, result4);
        assertEquals("testuser", result3);
    }

    // ==================== 边界测试 ====================

    @Test
    @DisplayName("测试边界情况-空字符串vs null")
    public void testBoundaryEmptyStringVsNull() {
        User user1 = new User();
        user1.setUsername("");  // 空字符串

        User user2 = new User();
        user2.setUsername(null);  // null

        // 空字符串不是null
        String result1 = ObjectUtils.getIfNotNull(user1, User::getUsername, "默认");
        assertEquals("", result1, "空字符串应被视为有效值");

        // null字段
        String result2 = ObjectUtils.getIfNotNull(user2, User::getUsername, "默认");
        assertNull(result2, "null字段返回null");
    }

    @Test
    @DisplayName("测试边界情况-数字0vs null")
    public void testBoundaryZeroVsNull() {
        User user1 = new User();
        user1.setAge(0);  // 0

        User user2 = new User();
        user2.setAge(null);  // null

        // 0不是null
        Integer result1 = ObjectUtils.getIfNotNull(user1, User::getAge, 18);
        assertEquals(0, result1, "0应被视为有效值");

        // null字段
        Integer result2 = ObjectUtils.getIfNotNull(user2, User::getAge, 18);
        assertNull(result2, "null字段返回null");
    }

    @Test
    @DisplayName("测试边界情况-复杂Lambda表达式")
    public void testBoundaryComplexLambda() {
        User user = new User();
        user.setUsername("testuser");
        user.setAge(25);

        // 使用复杂的Lambda表达式
        String result = ObjectUtils.getIfNotNull(user, u -> {
            if (u.getAge() != null && u.getAge() > 18) {
                return u.getUsername() + "(成年)";
            }
            return u.getUsername();
        });

        assertEquals("testuser(成年)", result);
    }
}
