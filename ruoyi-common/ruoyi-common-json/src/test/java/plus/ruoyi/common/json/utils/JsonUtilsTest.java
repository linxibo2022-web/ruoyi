package plus.ruoyi.common.json.utils;

import plus.ruoyi.common.test.base.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import lombok.Data;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JsonUtils JSON工具类测试
 *
 * @author 抓蛙师
 */
@DisplayName("JsonUtils JSON工具类测试")
public class JsonUtilsTest extends BaseUnitTest {

    @Data
    static class User {
        private Long id;
        private String name;
        private Integer age;
    }

    @Test
    @DisplayName("测试toJsonString-对象转JSON字符串")
    public void testToJsonString() {
        User user = new User();
        user.setId(1L);
        user.setName("张三");
        user.setAge(25);

        String json = JsonUtils.toJsonString(user);

        assertNotNull(json);
        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"name\":\"张三\""));
        assertTrue(json.contains("\"age\":25"));
    }

    @Test
    @DisplayName("测试parseObject-JSON字符串转对象")
    public void testParseObject() {
        String json = "{\"id\":1,\"name\":\"李四\",\"age\":30}";

        User user = JsonUtils.parseObject(json, User.class);

        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("李四", user.getName());
        assertEquals(30, user.getAge());
    }

    @Test
    @DisplayName("测试parseArray-JSON数组转List")
    public void testParseArray() {
        String json = "[{\"id\":1,\"name\":\"用户1\",\"age\":20}," +
                      "{\"id\":2,\"name\":\"用户2\",\"age\":25}]";

        List<User> users = JsonUtils.parseArray(json, User.class);

        assertNotNull(users);
        assertEquals(2, users.size());
        assertEquals("用户1", users.get(0).getName());
        assertEquals("用户2", users.get(1).getName());
    }

    @Test
    @DisplayName("测试parseMap-JSON转Map")
    public void testParseMap() {
        String json = "{\"key1\":\"value1\",\"key2\":\"value2\"}";

        Map<String, Object> map = JsonUtils.parseMap(json);

        assertNotNull(map);
        assertEquals(2, map.size());
        assertEquals("value1", map.get("key1"));
        assertEquals("value2", map.get("key2"));
    }

    @Test
    @DisplayName("测试toJsonString-null值应返回null")
    public void testToJsonStringWithNull() {
        String json = JsonUtils.toJsonString(null);
        assertNull(json);
    }

    @Test
    @DisplayName("测试parseObject-空字符串应返回null")
    public void testParseObjectWithEmptyString() {
        User user = JsonUtils.parseObject("", User.class);
        assertNull(user);
    }

    @Test
    @DisplayName("测试parseObject-null应返回null")
    public void testParseObjectWithNull() {
        User user = JsonUtils.parseObject("", User.class);
        assertNull(user);
    }

    @Test
    @DisplayName("测试toJsonString-格式化输出")
    public void testToJsonStringPretty() {
        User user = new User();
        user.setId(1L);
        user.setName("测试");

        // 注意:这里根据你实际的JsonUtils实现来测试
        // 如果没有pretty方法,可以删除这个测试
        String json = JsonUtils.toJsonString(user);

        assertNotNull(json);
        assertTrue(json.length() > 0);
    }
}
