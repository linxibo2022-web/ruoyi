package plus.ruoyi.common.core.utils.regex;

import plus.ruoyi.common.test.base.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RegexUtils 正则工具测试
 *
 * @author 抓蛙师
 */
@DisplayName("RegexUtils正则工具测试")
public class RegexUtilsTest extends BaseUnitTest {

    // ==================== extractFromString 测试 ====================

    @Test
    @DisplayName("测试extractFromString-提取邮箱")
    public void testExtractFromStringEmail() {
        String input = "联系我：test@example.com 或者 admin@test.com";

        // 使用简单的邮箱正则提取第一个邮箱
        String email = RegexUtils.extractFromString(input, "([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})", "");

        assertEquals("test@example.com", email);
    }

    @Test
    @DisplayName("测试extractFromString-提取手机号")
    public void testExtractFromStringPhone() {
        String input = "我的手机号是13800138000，请联系";

        // 提取手机号
        String phone = RegexUtils.extractFromString(input, "(1[3-9]\\d{9})", "");

        assertEquals("13800138000", phone);
    }

    @Test
    @DisplayName("测试extractFromString-提取数字")
    public void testExtractFromStringNumber() {
        String input = "订单号：20240115001，金额：1234.56元";

        // 提取第一个数字
        String orderNo = RegexUtils.extractFromString(input, "(\\d+)", "");

        assertEquals("20240115001", orderNo);
    }

    @Test
    @DisplayName("测试extractFromString-未匹配时返回默认值")
    public void testExtractFromStringNoMatch() {
        String input = "这是一段普通文本，没有邮箱";

        // 尝试提取邮箱，未匹配时返回默认值
        String email = RegexUtils.extractFromString(input, "([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})", "无邮箱");

        assertEquals("无邮箱", email);
    }

    @Test
    @DisplayName("测试extractFromString-空字符串输入")
    public void testExtractFromStringEmptyInput() {
        String input = "";

        String result = RegexUtils.extractFromString(input, "(\\d+)", "默认值");

        assertEquals("默认值", result);
    }

    @Test
    @DisplayName("测试extractFromString-null输入")
    public void testExtractFromStringNullInput() {
        String input = null;

        // null输入应该返回默认值
        String result = RegexUtils.extractFromString(input, "(\\d+)", "默认值");

        assertEquals("默认值", result);
    }

    @Test
    @DisplayName("测试extractFromString-错误的正则表达式")
    public void testExtractFromStringInvalidRegex() {
        String input = "test 123";

        // 错误的正则表达式应该返回默认值
        String result = RegexUtils.extractFromString(input, "[", "默认值");

        assertEquals("默认值", result);
    }

    @Test
    @DisplayName("测试extractFromString-提取括号内容")
    public void testExtractFromStringBrackets() {
        String input = "用户名(admin)登录成功";

        // 提取括号内的内容
        String username = RegexUtils.extractFromString(input, "\\(([^)]+)\\)", "");

        assertEquals("admin", username);
    }

    @Test
    @DisplayName("测试extractFromString-提取URL")
    public void testExtractFromStringUrl() {
        String input = "访问网站：https://www.example.com/path?query=test 了解更多";

        // 提取URL
        String url = RegexUtils.extractFromString(input, "(https?://[^\\s]+)", "");

        assertEquals("https://www.example.com/path?query=test", url);
    }

    @Test
    @DisplayName("测试extractFromString-提取IP地址")
    public void testExtractFromStringIpAddress() {
        String input = "服务器IP：192.168.1.1，端口：8080";

        // 提取IP地址
        String ip = RegexUtils.extractFromString(input, "(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})", "");

        assertEquals("192.168.1.1", ip);
    }

    @Test
    @DisplayName("测试extractFromString-默认值为null")
    public void testExtractFromStringNullDefault() {
        String input = "没有数字的文本";

        String result = RegexUtils.extractFromString(input, "(\\d+)", null);

        assertNull(result);
    }

    @Test
    @DisplayName("测试extractFromString-默认值为空字符串")
    public void testExtractFromStringEmptyDefault() {
        String input = "没有数字的文本";

        String result = RegexUtils.extractFromString(input, "(\\d+)", "");

        assertEquals("", result);
    }

    // ==================== 配合 RegexPatterns 使用测试 ====================

    @Test
    @DisplayName("测试extractFromString-配合I18N_KEY模式")
    public void testExtractFromStringWithI18nKeyPattern() {
        String input = "国际化键：user.profile.name，类型：string";

        // 提取国际化键
        // 注意：RegexPatterns.I18N_KEY 包含 ^ 和 $,需要去掉才能在字符串中间匹配
        String pattern = RegexPatterns.I18N_KEY.replace("^", "").replace("$", "");
        String key = RegexUtils.extractFromString(input, "(" + pattern + ")", "");

        assertEquals("user.profile.name", key);
    }

    @Test
    @DisplayName("测试extractFromString-配合QQ模式")
    public void testExtractFromStringWithQQPattern() {
        String input = "我的QQ：1234567890，欢迎添加";

        // 提取QQ号
        // 注意：RegexPatterns.QQ 包含 ^ 和 $,需要去掉才能在字符串中间匹配
        String pattern = RegexPatterns.QQ.replace("^", "").replace("$", "");
        String qq = RegexUtils.extractFromString(input, "(" + pattern + ")", "");

        assertEquals("1234567890", qq);
    }

    @Test
    @DisplayName("测试extractFromString-配合邮政编码模式")
    public void testExtractFromStringWithPostalCodePattern() {
        String input = "邮编：518000，地址：深圳市南山区";

        // 提取邮政编码
        // 注意：RegexPatterns.POSTAL_CODE 包含 ^ 和 $,需要去掉才能在字符串中间匹配
        String pattern = RegexPatterns.POSTAL_CODE.replace("^", "").replace("$", "");
        String code = RegexUtils.extractFromString(input, "(" + pattern + ")", "");

        assertEquals("518000", code);
    }

    @Test
    @DisplayName("测试extractFromString-配合账号模式")
    public void testExtractFromStringWithAccountPattern() {
        String input = "用户账号：admin123，已激活";

        // 提取账号
        // 注意：RegexPatterns.ACCOUNT 包含 ^ 和 $,需要去掉才能在字符串中间匹配
        String pattern = RegexPatterns.ACCOUNT.replace("^", "").replace("$", "");
        String account = RegexUtils.extractFromString(input, "(" + pattern + ")", "");

        assertEquals("admin123", account);
    }

    // ==================== 实际业务场景测试 ====================

    @Test
    @DisplayName("测试业务场景-从错误信息中提取错误代码")
    public void testBusinessScenarioExtractErrorCode() {
        String errorMessage = "操作失败，错误代码：E1001，请联系管理员";

        // 提取错误代码
        String errorCode = RegexUtils.extractFromString(errorMessage, "错误代码：([A-Z]\\d+)", "未知错误");

        assertEquals("E1001", errorCode);
    }

    @Test
    @DisplayName("测试业务场景-从日志中提取时间戳")
    public void testBusinessScenarioExtractTimestamp() {
        String log = "[2024-01-15 14:30:25] INFO: 用户登录成功";

        // 提取时间戳
        String timestamp = RegexUtils.extractFromString(log, "\\[(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})\\]", "");

        assertEquals("2024-01-15 14:30:25", timestamp);
    }

    @Test
    @DisplayName("测试业务场景-从响应中提取token")
    public void testBusinessScenarioExtractToken() {
        String response = "{\"code\":200,\"token\":\"abc123xyz456\",\"message\":\"success\"}";

        // 提取token
        String token = RegexUtils.extractFromString(response, "\"token\":\"([^\"]+)\"", "");

        assertEquals("abc123xyz456", token);
    }

    @Test
    @DisplayName("测试业务场景-从文件名中提取日期")
    public void testBusinessScenarioExtractDateFromFilename() {
        String filename = "export_data_20240115_final.xlsx";

        // 提取日期
        String date = RegexUtils.extractFromString(filename, "(\\d{8})", "");

        assertEquals("20240115", date);
    }

    @Test
    @DisplayName("测试业务场景-从描述中提取版本号")
    public void testBusinessScenarioExtractVersion() {
        String description = "系统版本：v5.5.0，发布日期：2024-01-15";

        // 提取版本号
        String version = RegexUtils.extractFromString(description, "v(\\d+\\.\\d+\\.\\d+)", "");

        assertEquals("5.5.0", version);
    }

    // ==================== 边界测试 ====================

    @Test
    @DisplayName("测试边界情况-超长字符串")
    public void testBoundaryLongString() {
        String input = "正常文本".repeat(10000) + "关键信息：KEY12345" + "更多文本".repeat(1000);

        String result = RegexUtils.extractFromString(input, "关键信息：([A-Z0-9]+)", "");

        assertEquals("KEY12345", result);
    }

    @Test
    @DisplayName("测试边界情况-特殊字符")
    public void testBoundarySpecialChars() {
        String input = "特殊字符：!@#$%^&*()，数字：12345";

        String result = RegexUtils.extractFromString(input, "数字：(\\d+)", "");

        assertEquals("12345", result);
    }

    @Test
    @DisplayName("测试边界情况-多行文本")
    public void testBoundaryMultilineText() {
        String input = "第一行：abc\n第二行：123\n第三行：xyz";

        // 提取第二行的数字
        String result = RegexUtils.extractFromString(input, "第二行：(\\d+)", "");

        assertEquals("123", result);
    }

    @Test
    @DisplayName("测试边界情况-Unicode字符")
    public void testBoundaryUnicodeChars() {
        String input = "用户名：张三😀，年龄：25";

        String result = RegexUtils.extractFromString(input, "年龄：(\\d+)", "");

        assertEquals("25", result);
    }
}
