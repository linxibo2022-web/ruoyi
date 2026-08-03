package plus.ruoyi.common.core.utils.reflect;

import plus.ruoyi.common.test.base.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import lombok.Data;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ReflectUtils 反射工具测试
 *
 * @author 抓蛙师
 */
@DisplayName("ReflectUtils反射工具测试")
public class ReflectUtilsTest extends BaseUnitTest {

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
        private Profile profile;  // 嵌套对象
    }

    /**
     * 用户档案类 - 嵌套对象
     */
    @Data
    static class Profile {
        private String nickname;
        private String phone;
        private Address address;  // 多级嵌套
    }

    /**
     * 地址类 - 多级嵌套对象
     */
    @Data
    static class Address {
        private String province;
        private String city;
        private String detail;
    }

    // ==================== 测试方法 ====================

    @Test
    @DisplayName("测试invokeGetter-简单属性访问")
    public void testInvokeGetterSimpleProperty() {
        User user = createTestUser();

        // 测试简单属性
        Long id = ReflectUtils.invokeGetter(user, "id");
        assertEquals(1L, id);

        String username = ReflectUtils.invokeGetter(user, "username");
        assertEquals("testuser", username);

        Integer age = ReflectUtils.invokeGetter(user, "age");
        assertEquals(25, age);
    }

    @Test
    @DisplayName("测试invokeGetter-多级属性访问")
    public void testInvokeGetterNestedProperty() {
        User user = createTestUser();

        // 测试二级属性
        String nickname = ReflectUtils.invokeGetter(user, "profile.nickname");
        assertEquals("测试昵称", nickname);

        String phone = ReflectUtils.invokeGetter(user, "profile.phone");
        assertEquals("13800138000", phone);

        // 测试三级属性
        String province = ReflectUtils.invokeGetter(user, "profile.address.province");
        assertEquals("广东省", province);

        String city = ReflectUtils.invokeGetter(user, "profile.address.city");
        assertEquals("深圳市", city);
    }

    @Test
    @DisplayName("测试invokeGetter-null对象应抛出异常")
    public void testInvokeGetterNullObject() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ReflectUtils.invokeGetter(null, "username")
        );

        assertEquals("目标对象不能为null", exception.getMessage());
    }

    @Test
    @DisplayName("测试invokeGetter-空属性名应抛出异常")
    public void testInvokeGetterBlankPropertyName() {
        User user = createTestUser();

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ReflectUtils.invokeGetter(user, "")
        );

        assertEquals("属性名称不能为空", exception.getMessage());
    }

    @Test
    @DisplayName("测试invokeGetter-中间对象为null应返回null")
    public void testInvokeGetterMiddleObjectIsNull() {
        User user = new User();
        user.setId(1L);
        user.setProfile(null);  // profile为null

        // 访问profile.nickname应该返回null而不抛出异常
        String nickname = ReflectUtils.invokeGetter(user, "profile.nickname");
        assertNull(nickname, "中间对象为null时应返回null");
    }

    @Test
    @DisplayName("测试invokeSetter-简单属性设置")
    public void testInvokeSetterSimpleProperty() {
        User user = new User();

        // 设置简单属性
        ReflectUtils.invokeSetter(user, "id", 100L);
        assertEquals(100L, user.getId());

        ReflectUtils.invokeSetter(user, "username", "newuser");
        assertEquals("newuser", user.getUsername());

        ReflectUtils.invokeSetter(user, "age", 30);
        assertEquals(30, user.getAge());
    }

    @Test
    @DisplayName("测试invokeSetter-多级属性设置")
    public void testInvokeSetterNestedProperty() {
        User user = createTestUser();

        // 设置二级属性
        ReflectUtils.invokeSetter(user, "profile.nickname", "新昵称");
        assertEquals("新昵称", user.getProfile().getNickname());

        ReflectUtils.invokeSetter(user, "profile.phone", "13900139000");
        assertEquals("13900139000", user.getProfile().getPhone());

        // 设置三级属性
        ReflectUtils.invokeSetter(user, "profile.address.city", "广州市");
        assertEquals("广州市", user.getProfile().getAddress().getCity());
    }

    @Test
    @DisplayName("测试invokeSetter-null对象应抛出异常")
    public void testInvokeSetterNullObject() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ReflectUtils.invokeSetter(null, "username", "test")
        );

        assertEquals("目标对象不能为null", exception.getMessage());
    }

    @Test
    @DisplayName("测试invokeSetter-空属性名应抛出异常")
    public void testInvokeSetterBlankPropertyName() {
        User user = new User();

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ReflectUtils.invokeSetter(user, "", "test")
        );

        assertEquals("属性名称不能为空", exception.getMessage());
    }

    @Test
    @DisplayName("测试invokeSetter-中间对象为null应抛出异常")
    public void testInvokeSetterMiddleObjectIsNull() {
        User user = new User();
        user.setProfile(null);  // profile为null

        // 尝试设置profile.nickname应该抛出异常
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> ReflectUtils.invokeSetter(user, "profile.nickname", "测试")
        );

        assertTrue(exception.getMessage().contains("中间对象为null"));
    }

    @Test
    @DisplayName("测试getPropertySafely-简单属性安全访问")
    public void testGetPropertySafelySimpleProperty() {
        User user = createTestUser();

        String username = ReflectUtils.getPropertySafely(user, "username");
        assertEquals("testuser", username);

        Integer age = ReflectUtils.getPropertySafely(user, "age");
        assertEquals(25, age);
    }

    @Test
    @DisplayName("测试getPropertySafely-多级属性安全访问")
    public void testGetPropertySafelyNestedProperty() {
        User user = createTestUser();

        String nickname = ReflectUtils.getPropertySafely(user, "profile.nickname");
        assertEquals("测试昵称", nickname);

        String city = ReflectUtils.getPropertySafely(user, "profile.address.city");
        assertEquals("深圳市", city);
    }

    @Test
    @DisplayName("测试getPropertySafely-null对象应返回null")
    public void testGetPropertySafelyNullObject() {
        String result = ReflectUtils.getPropertySafely(null, "username");
        assertNull(result, "null对象应返回null而不抛异常");
    }

    @Test
    @DisplayName("测试getPropertySafely-空属性名应返回null")
    public void testGetPropertySafelyBlankPropertyName() {
        User user = createTestUser();

        String result = ReflectUtils.getPropertySafely(user, "");
        assertNull(result, "空属性名应返回null");
    }

    @Test
    @DisplayName("测试getPropertySafely-中间对象为null应返回null")
    public void testGetPropertySafelyMiddleObjectIsNull() {
        User user = new User();
        user.setId(1L);
        user.setProfile(null);  // profile为null

        String nickname = ReflectUtils.getPropertySafely(user, "profile.nickname");
        assertNull(nickname, "中间对象为null时应返回null");
    }

    @Test
    @DisplayName("测试getPropertySafely-不存在的属性应返回null")
    public void testGetPropertySafelyNonExistentProperty() {
        User user = createTestUser();

        // 访问不存在的属性
        Object result = ReflectUtils.getPropertySafely(user, "nonExistentProperty");
        assertNull(result, "不存在的属性应返回null");
    }

    @Test
    @DisplayName("测试hasProperty-简单属性存在性检查")
    public void testHasPropertySimpleProperty() {
        User user = new User();

        // 存在的属性
        assertTrue(ReflectUtils.hasProperty(user, "id"));
        assertTrue(ReflectUtils.hasProperty(user, "username"));
        assertTrue(ReflectUtils.hasProperty(user, "age"));

        // 不存在的属性
        assertFalse(ReflectUtils.hasProperty(user, "nonExistentProperty"));
    }

    @Test
    @DisplayName("测试hasProperty-多级属性存在性检查")
    public void testHasPropertyNestedProperty() {
        User user = createTestUser();

        // 存在的嵌套属性
        assertTrue(ReflectUtils.hasProperty(user, "profile.nickname"));
        assertTrue(ReflectUtils.hasProperty(user, "profile.phone"));
        assertTrue(ReflectUtils.hasProperty(user, "profile.address.province"));
        assertTrue(ReflectUtils.hasProperty(user, "profile.address.city"));

        // 不存在的嵌套属性
        assertFalse(ReflectUtils.hasProperty(user, "profile.nonExistent"));
        assertFalse(ReflectUtils.hasProperty(user, "profile.address.nonExistent"));
    }

    @Test
    @DisplayName("测试hasProperty-null对象应返回false")
    public void testHasPropertyNullObject() {
        boolean result = ReflectUtils.hasProperty(null, "username");
        assertFalse(result, "null对象应返回false");
    }

    @Test
    @DisplayName("测试hasProperty-空属性名应返回false")
    public void testHasPropertyBlankPropertyName() {
        User user = new User();

        boolean result = ReflectUtils.hasProperty(user, "");
        assertFalse(result, "空属性名应返回false");
    }

    @Test
    @DisplayName("测试组合场景-先检查属性再获取")
    public void testCombinedScenario() {
        User user = createTestUser();

        // 检查属性存在
        if (ReflectUtils.hasProperty(user, "profile.address.city")) {
            // 安全获取属性值
            String city = ReflectUtils.getPropertySafely(user, "profile.address.city");
            assertEquals("深圳市", city);
        }

        // 检查不存在的属性
        if (!ReflectUtils.hasProperty(user, "profile.nonExistent")) {
            // 安全获取返回null
            Object value = ReflectUtils.getPropertySafely(user, "profile.nonExistent");
            assertNull(value);
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建完整的测试用户对象
     */
    private User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setAge(25);

        Profile profile = new Profile();
        profile.setNickname("测试昵称");
        profile.setPhone("13800138000");

        Address address = new Address();
        address.setProvince("广东省");
        address.setCity("深圳市");
        address.setDetail("南山区科技园");

        profile.setAddress(address);
        user.setProfile(profile);

        return user;
    }
}
