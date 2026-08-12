package plus.ruoyi.common.core.utils.sql;

import plus.ruoyi.common.test.base.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SqlUtil SQL安全工具测试
 *
 * @author 抓蛙师
 */
@Tag("dev")
@DisplayName("SqlUtil SQL安全工具测试")
public class SqlUtilTest extends BaseUnitTest {

    // ==================== escapeOrderBySql 测试 ====================

    @Test
    @DisplayName("测试escapeOrderBySql-合法的单字段排序")
    public void testEscapeOrderBySqlValidSingleField() {
        // 简单字段名
        assertEquals("name", SqlUtil.escapeOrderBySql("name"));
        assertEquals("create_time", SqlUtil.escapeOrderBySql("create_time"));
        assertEquals("userId", SqlUtil.escapeOrderBySql("userId"));

        // 带排序方向
        assertEquals("name asc", SqlUtil.escapeOrderBySql("name asc"));
        assertEquals("create_time desc", SqlUtil.escapeOrderBySql("create_time desc"));
    }

    @Test
    @DisplayName("测试escapeOrderBySql-合法的多字段排序")
    public void testEscapeOrderBySqlValidMultipleFields() {
        // 多字段排序
        assertEquals("name, age", SqlUtil.escapeOrderBySql("name, age"));
        assertEquals("name asc, create_time desc", SqlUtil.escapeOrderBySql("name asc, create_time desc"));
        assertEquals("user_id, dept_id, create_time", SqlUtil.escapeOrderBySql("user_id, dept_id, create_time"));
    }

    @Test
    @DisplayName("测试escapeOrderBySql-包含小数点的字段名")
    public void testEscapeOrderBySqlWithDot() {
        // 表名.字段名格式
        assertEquals("u.name", SqlUtil.escapeOrderBySql("u.name"));
        assertEquals("user.create_time desc", SqlUtil.escapeOrderBySql("user.create_time desc"));
        assertEquals("t1.id, t2.name", SqlUtil.escapeOrderBySql("t1.id, t2.name"));
    }

    @Test
    @DisplayName("测试escapeOrderBySql-空字符串和null应返回原值")
    public void testEscapeOrderBySqlEmptyOrNull() {
        assertNull(SqlUtil.escapeOrderBySql(null));
        assertEquals("", SqlUtil.escapeOrderBySql(""));
    }

    @Test
    @DisplayName("测试escapeOrderBySql-包含非法字符应抛出异常")
    public void testEscapeOrderBySqlWithSqlKeywords() {
        // 包含分号等非法字符
        assertThrows(IllegalArgumentException.class,
            () -> SqlUtil.escapeOrderBySql("name; DROP TABLE users"));

        assertThrows(IllegalArgumentException.class,
            () -> SqlUtil.escapeOrderBySql("name WHERE 1=1"));
        // 注意: escapeOrderBySql 只校验字符级白名单[a-zA-Z0-9_ \,\.]
        // "name FROM users" 全为合法字符，不会抛异常
        // 如需检测SQL关键字，请使用 filterKeyword() 方法
    }

    @Test
    @DisplayName("测试escapeOrderBySql-包含特殊字符应抛出异常")
    public void testEscapeOrderBySqlWithSpecialChars() {
        // 包含分号
        assertThrows(IllegalArgumentException.class,
            () -> SqlUtil.escapeOrderBySql("name;"));

        // 包含括号
        assertThrows(IllegalArgumentException.class,
            () -> SqlUtil.escapeOrderBySql("count(*)"));

        // 包含单引号
        assertThrows(IllegalArgumentException.class,
            () -> SqlUtil.escapeOrderBySql("name'"));

        // 包含等号
        assertThrows(IllegalArgumentException.class,
            () -> SqlUtil.escapeOrderBySql("id=1"));
    }

    // ==================== isValidOrderBySql 测试 ====================

    @Test
    @DisplayName("测试isValidOrderBySql-合法的ORDER BY语句")
    public void testIsValidOrderBySqlValid() {
        // 简单字段
        assertTrue(SqlUtil.isValidOrderBySql("name"));
        assertTrue(SqlUtil.isValidOrderBySql("create_time"));
        assertTrue(SqlUtil.isValidOrderBySql("userId"));

        // 带排序方向
        assertTrue(SqlUtil.isValidOrderBySql("name asc"));
        assertTrue(SqlUtil.isValidOrderBySql("create_time desc"));

        // 多字段
        assertTrue(SqlUtil.isValidOrderBySql("name, age"));
        assertTrue(SqlUtil.isValidOrderBySql("name asc, create_time desc"));

        // 表名.字段名
        assertTrue(SqlUtil.isValidOrderBySql("u.name"));
        assertTrue(SqlUtil.isValidOrderBySql("user.create_time desc"));
    }

    @Test
    @DisplayName("测试isValidOrderBySql-非法的ORDER BY语句")
    public void testIsValidOrderBySqlInvalid() {
        // 包含特殊字符
        assertFalse(SqlUtil.isValidOrderBySql("name;"));
        assertFalse(SqlUtil.isValidOrderBySql("count(*)"));
        assertFalse(SqlUtil.isValidOrderBySql("name'"));
        assertFalse(SqlUtil.isValidOrderBySql("id=1"));

        // 包含SQL关键字
        assertFalse(SqlUtil.isValidOrderBySql("name; DROP TABLE users"));
        assertFalse(SqlUtil.isValidOrderBySql("name WHERE 1=1"));
    }

    // ==================== filterKeyword 测试 ====================

    @Test
    @DisplayName("测试filterKeyword-安全的输入不应抛出异常")
    public void testFilterKeywordSafeInput() {
        // 正常字符串
        assertDoesNotThrow(() -> SqlUtil.filterKeyword("张三"));
        assertDoesNotThrow(() -> SqlUtil.filterKeyword("test@example.com"));
        assertDoesNotThrow(() -> SqlUtil.filterKeyword("13800138000"));
        assertDoesNotThrow(() -> SqlUtil.filterKeyword("正常的业务数据"));
    }

    @Test
    @DisplayName("测试filterKeyword-空字符串和null不应抛出异常")
    public void testFilterKeywordEmptyOrNull() {
        assertDoesNotThrow(() -> SqlUtil.filterKeyword(null));
        assertDoesNotThrow(() -> SqlUtil.filterKeyword(""));
    }

    @Test
    @DisplayName("测试filterKeyword-包含SQL注入关键字应抛出异常-SELECT")
    public void testFilterKeywordWithSelect() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SqlUtil.filterKeyword("test' OR '1'='1' SELECT * FROM users")
        );
        assertEquals("参数存在SQL注入风险", exception.getMessage());

        // 大小写混合
        assertThrows(IllegalArgumentException.class,
            () -> SqlUtil.filterKeyword("SeLeCt * from users"));
    }

    @Test
    @DisplayName("测试filterKeyword-包含SQL注入关键字应抛出异常-DELETE")
    public void testFilterKeywordWithDelete() {
        assertThrows(IllegalArgumentException.class,
            () -> SqlUtil.filterKeyword("'; DELETE FROM users WHERE '1'='1"));

        assertThrows(IllegalArgumentException.class,
            () -> SqlUtil.filterKeyword("DeLeTe from users"));
    }

    @Test
    @DisplayName("测试filterKeyword-包含SQL注入关键字应抛出异常-UPDATE")
    public void testFilterKeywordWithUpdate() {
        assertThrows(IllegalArgumentException.class,
            () -> SqlUtil.filterKeyword("'; UPDATE users SET password='hacked'"));

        assertThrows(IllegalArgumentException.class,
            () -> SqlUtil.filterKeyword("UpDaTe users set name='test'"));
    }

    @Test
    @DisplayName("测试filterKeyword-包含SQL注入关键字应抛出异常-DROP")
    public void testFilterKeywordWithDrop() {
        assertThrows(IllegalArgumentException.class,
            () -> SqlUtil.filterKeyword("'; DROP TABLE users"));

        assertThrows(IllegalArgumentException.class,
            () -> SqlUtil.filterKeyword("DrOp database test"));
    }

    @Test
    @DisplayName("测试filterKeyword-包含SQL注入关键字应抛出异常-UNION")
    public void testFilterKeywordWithUnion() {
        assertThrows(IllegalArgumentException.class,
            () -> SqlUtil.filterKeyword("test' UNION SELECT password FROM users"));

        assertThrows(IllegalArgumentException.class,
            () -> SqlUtil.filterKeyword("UnIoN all select * from admin"));
    }

    @Test
    @DisplayName("测试filterKeyword-包含SQL注入关键字应抛出异常-OR")
    public void testFilterKeywordWithOr() {
        assertThrows(IllegalArgumentException.class,
            () -> SqlUtil.filterKeyword("admin' OR '1'='1"));

        assertThrows(IllegalArgumentException.class,
            () -> SqlUtil.filterKeyword("test' Or 1=1 --"));
    }

    @Test
    @DisplayName("测试filterKeyword-包含SQL注入关键字应抛出异常-AND")
    public void testFilterKeywordWithAnd() {
        assertThrows(IllegalArgumentException.class,
            () -> SqlUtil.filterKeyword("test' AND 1=1"));

        assertThrows(IllegalArgumentException.class,
            () -> SqlUtil.filterKeyword("admin' AnD '1'='1"));
    }

    @Test
    @DisplayName("测试filterKeyword-包含SQL注入关键字应抛出异常-LIKE")
    public void testFilterKeywordWithLike() {
        assertThrows(IllegalArgumentException.class,
            () -> SqlUtil.filterKeyword("test' LIKE '%admin%"));
    }

    @Test
    @DisplayName("测试filterKeyword-包含SQL注入关键字应抛出异常-EXEC")
    public void testFilterKeywordWithExec() {
        assertThrows(IllegalArgumentException.class,
            () -> SqlUtil.filterKeyword("test'; EXEC sp_executesql"));
    }

    @Test
    @DisplayName("测试filterKeyword-包含SQL注入关键字应抛出异常-INSERT")
    public void testFilterKeywordWithInsert() {
        assertThrows(IllegalArgumentException.class,
            () -> SqlUtil.filterKeyword("test'; INSERT INTO users VALUES('hacker')"));
    }

    @Test
    @DisplayName("测试filterKeyword-包含SQL函数应抛出异常")
    public void testFilterKeywordWithSqlFunctions() {
        // extractvalue (XML函数,常用于报错注入)
        assertThrows(IllegalArgumentException.class,
            () -> SqlUtil.filterKeyword("test' AND extractvalue(1, concat(0x7e, database()))"));

        // updatexml (XML函数,常用于报错注入)
        assertThrows(IllegalArgumentException.class,
            () -> SqlUtil.filterKeyword("test' AND updatexml(1, concat(0x7e, user()), 1)"));

        // sleep (时间盲注)
        assertThrows(IllegalArgumentException.class,
            () -> SqlUtil.filterKeyword("test' AND sleep(5)"));
    }

    @Test
    @DisplayName("测试filterKeyword-包含注释符应抛出异常")
    public void testFilterKeywordWithComments() {
        // SQL注释符 /*
        assertThrows(IllegalArgumentException.class,
            () -> SqlUtil.filterKeyword("test' /* comment */"));
    }

    @Test
    @DisplayName("测试filterKeyword-包含加号应抛出异常")
    public void testFilterKeywordWithPlus() {
        // 加号可能用于字符串拼接绕过
        assertThrows(IllegalArgumentException.class,
            () -> SqlUtil.filterKeyword("test' + 'injection"));
    }

    @Test
    @DisplayName("测试filterKeyword-包含垂直制表符应抛出异常")
    public void testFilterKeywordWithVerticalTab() {
        // 垂直制表符(\u000B)用于绕过某些过滤
        assertThrows(IllegalArgumentException.class,
            () -> SqlUtil.filterKeyword("test\u000Binjection"));
    }

    @Test
    @DisplayName("测试filterKeyword-组合注入攻击场景")
    public void testFilterKeywordCombinedInjectionScenarios() {
        // 经典万能密码
        assertThrows(IllegalArgumentException.class,
            () -> SqlUtil.filterKeyword("admin' OR '1'='1"));

        // 联合查询注入
        assertThrows(IllegalArgumentException.class,
            () -> SqlUtil.filterKeyword("1' UNION SELECT username, password FROM admin_users --"));

        // 堆叠查询注入
        assertThrows(IllegalArgumentException.class,
            () -> SqlUtil.filterKeyword("1'; DROP TABLE users; --"));

        // 报错注入
        assertThrows(IllegalArgumentException.class,
            () -> SqlUtil.filterKeyword("1' AND extractvalue(1, concat(0x7e, (SELECT database())))"));
    }

    // ==================== 边界测试 ====================

    @Test
    @DisplayName("测试边界情况-超长字符串")
    public void testBoundaryLongString() {
        // 生成一个很长的安全字符串
        String longSafeString = "正常数据".repeat(1000);
        assertDoesNotThrow(() -> SqlUtil.filterKeyword(longSafeString));

        // 超长字符串包含危险关键字
        String longDangerousString = "正常数据".repeat(1000) + " SELECT * FROM users";
        assertThrows(IllegalArgumentException.class,
            () -> SqlUtil.filterKeyword(longDangerousString));
    }

    @Test
    @DisplayName("测试边界情况-特殊Unicode字符")
    public void testBoundaryUnicodeChars() {
        // 包含emoji等Unicode字符的安全输入
        assertDoesNotThrow(() -> SqlUtil.filterKeyword("测试😀数据🎉"));

        // Unicode字符 + 危险关键字
        assertThrows(IllegalArgumentException.class,
            () -> SqlUtil.filterKeyword("😀 SELECT * FROM users"));
    }
}
