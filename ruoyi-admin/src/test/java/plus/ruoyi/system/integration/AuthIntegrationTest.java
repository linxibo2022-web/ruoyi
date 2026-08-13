package plus.ruoyi.system.integration;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import plus.ruoyi.common.core.constant.GlobalConstants;
import plus.ruoyi.common.encrypt.utils.EncryptUtils;
import plus.ruoyi.common.redis.utils.RedisUtils;
import plus.ruoyi.common.test.base.BaseControllerTest;
import plus.ruoyi.helper.TestLoginHelper;

import java.time.Duration;


import static org.junit.jupiter.api.Assertions.*;

/**
 * 认证接口集成测试
 * <p>
 * 测试策略说明:
 * - 通过预设 Redis 验证码绕过验证码校验
 * - 测试完整的登录/登出流程
 * - 测试 token 有效性验证
 *
 * @author 抓蛙师
 */
@Slf4j
@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@DisplayName("认证接口集成测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthIntegrationTest extends BaseControllerTest {

    @Autowired
    private TestLoginHelper testLoginHelper;

    /**
     * RSA 公钥 (用于加密 AES 密钥)
     */
    private static final String RSA_PUBLIC_KEY = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAK9s1Pbnn5W+l1hx3ukHLtevayF5nOFIb140zvIPksw8h7bZWY05zyWdsQ2I51ypiC53KnyfhibPk3AJ2yr+qnkCAwEAAQ==";

    /**
     * 加密标识头
     */
    private static final String ENCRYPT_HEADER = "encrypt-key";

    /**
     * 固定的测试验证码
     */
    private static final String TEST_CAPTCHA_CODE = "1234";

    /**
     * 登录获取的token (用于后续测试)
     */
    private static String loginToken;

    /**
     * 生成 32 位随机 AES 密钥
     */
    private String generateAesKey() {
        return RandomUtil.randomString(32);
    }

    /**
     * 预设验证码到 Redis
     *
     * @param uuid 验证码唯一标识
     * @param code 验证码值
     */
    private void setCaptchaToRedis(String uuid, String code) {
        String key = GlobalConstants.CAPTCHA_CODE_KEY + uuid;
        RedisUtils.setCacheObject(key, code, Duration.ofMinutes(5));
        log.info("预设验证码到 Redis: key={}, code={}", key, code);
    }

    /**
     * 发送加密的 POST 请求
     */
    private HttpResponse doEncryptedPost(String path, String jsonBody) {
        String aesKey = generateAesKey();
        String encryptedBody = EncryptUtils.encryptByAes(jsonBody, aesKey);
        String aesKeyBase64 = EncryptUtils.encryptByBase64(aesKey);
        String encryptedAesKey = EncryptUtils.encryptByRsa(aesKeyBase64, RSA_PUBLIC_KEY);

        return HttpRequest.post(getBaseUrl() + path)
            .header("Content-Type", "application/json")
            .header(ENCRYPT_HEADER, encryptedAesKey)
            .body(encryptedBody)
            .execute();
    }

    @BeforeAll
    public static void beforeAll() {
        log.info("========== 认证接口测试开始 ==========");
    }

    @Test
    @Order(1)
    @DisplayName("测试用户登录 - 使用正确的用户名密码")
    public void testLogin() {
        log.info("测试用户登录 (加密传输 + 预设验证码)");

        // 预设验证码到 Redis
        String uuid = "test-uuid-" + System.currentTimeMillis();
        setCaptchaToRedis(uuid, TEST_CAPTCHA_CODE);

        // 验证验证码已写入 Redis
        String verifyKey = GlobalConstants.CAPTCHA_CODE_KEY + uuid;
        String savedCaptcha = RedisUtils.getCacheObject(verifyKey);
        log.info("验证 Redis 验证码: key={}, value={}", verifyKey, savedCaptcha);
        assertNotNull(savedCaptcha, "验证码应该已写入 Redis");
        assertEquals(TEST_CAPTCHA_CODE, savedCaptcha, "验证码值应该匹配");

        // 构造登录请求体 (包含验证码)
        String loginBody = String.format("""
            {
                "authType": "password",
                "userName": "superadmin",
                "password": "admin123",
                "code": "%s",
                "uuid": "%s"
            }
            """, TEST_CAPTCHA_CODE, uuid);

        // 发起加密的登录请求
        HttpResponse response = doEncryptedPost("/auth/userLogin", loginBody);
        log.info("登录响应: status={}, body={}", response.getStatus(), response.body());

        // 验证HTTP响应状态
        assertEquals(200, response.getStatus(), "HTTP状态码应该是200");

        // 解析响应
        JSONObject result = JSONUtil.parseObj(response.body());
        assertNotNull(result, "响应结果不应为null");
        assertEquals(200, result.getInt("code"), "业务状态码应该是200, 实际返回: " + result);

        // 验证Token数据
        JSONObject data = result.getJSONObject("data");
        assertNotNull(data, "Token信息不应为null");
        // 注意：返回字段是 access_token (下划线格式)
        String accessToken = data.getStr("access_token");
        assertNotNull(accessToken, "访问令牌不应为null");
        assertTrue(accessToken.length() > 0, "访问令牌不应为空");

        // 保存token用于后续测试
        loginToken = "Bearer " + accessToken;

        log.info("登录成功: access_token={}", accessToken.substring(0, Math.min(30, accessToken.length())) + "...");
    }

    @Test
    @Order(2)
    @DisplayName("测试用户登录 - 使用错误的密码")
    public void testLoginWithWrongPassword() {
        log.info("测试使用错误密码登录");

        // 预设验证码
        String uuid = "test-uuid-wrong-" + System.currentTimeMillis();
        setCaptchaToRedis(uuid, TEST_CAPTCHA_CODE);

        String loginBody = String.format("""
            {
                "authType": "password",
                "userName": "superadmin",
                "password": "wrongPassword123",
                "code": "%s",
                "uuid": "%s"
            }
            """, TEST_CAPTCHA_CODE, uuid);

        HttpResponse response = doEncryptedPost("/auth/userLogin", loginBody);

        assertEquals(200, response.getStatus(), "HTTP请求应该成功");

        JSONObject result = JSONUtil.parseObj(response.body());
        assertNotNull(result, "响应结果不应为null");
        assertNotEquals(200, result.getInt("code"), "使用错误密码登录,业务状态码不应该是200");

        log.info("验证成功: 错误密码登录返回错误, code={}, msg={}", result.getInt("code"), result.getStr("msg"));
    }

    @Test
    @Order(3)
    @DisplayName("测试用户登录 - 使用不存在的用户名")
    public void testLoginWithNonExistentUser() {
        log.info("测试使用不存在的用户名登录");

        // 预设验证码
        String uuid = "test-uuid-nouser-" + System.currentTimeMillis();
        setCaptchaToRedis(uuid, TEST_CAPTCHA_CODE);

        String loginBody = String.format("""
            {
                "authType": "password",
                "userName": "nonExistentUser_12345",
                "password": "somePassword",
                "code": "%s",
                "uuid": "%s"
            }
            """, TEST_CAPTCHA_CODE, uuid);

        HttpResponse response = doEncryptedPost("/auth/userLogin", loginBody);

        assertEquals(200, response.getStatus(), "HTTP请求应该成功");

        JSONObject result = JSONUtil.parseObj(response.body());
        assertNotNull(result, "响应结果不应为null");
        assertNotEquals(200, result.getInt("code"), "使用不存在用户登录,业务状态码不应该是200");

        log.info("验证成功: 不存在用户登录返回错误, code={}, msg={}", result.getInt("code"), result.getStr("msg"));
    }

    @Test
    @Order(4)
    @DisplayName("测试用户登录 - 验证码错误")
    public void testLoginWithWrongCaptcha() {
        log.info("测试使用错误验证码登录");

        // 预设验证码
        String uuid = "test-uuid-captcha-" + System.currentTimeMillis();
        setCaptchaToRedis(uuid, TEST_CAPTCHA_CODE);

        // 使用错误的验证码
        String loginBody = String.format("""
            {
                "authType": "password",
                "userName": "superadmin",
                "password": "admin123",
                "code": "9999",
                "uuid": "%s"
            }
            """, uuid);

        HttpResponse response = doEncryptedPost("/auth/userLogin", loginBody);

        assertEquals(200, response.getStatus(), "HTTP请求应该成功");

        JSONObject result = JSONUtil.parseObj(response.body());
        assertNotNull(result, "响应结果不应为null");
        assertNotEquals(200, result.getInt("code"), "使用错误验证码登录,业务状态码不应该是200");

        log.info("验证成功: 错误验证码登录返回错误, code={}, msg={}", result.getInt("code"), result.getStr("msg"));
    }

    @Test
    @Order(5)
    @DisplayName("测试使用有效Token访问受保护接口")
    public void testAccessWithValidToken() {
        log.info("测试使用有效 Token 访问受保护接口");

        // 确保有 token
        if (loginToken == null || loginToken.isEmpty()) {
            loginToken = testLoginHelper.loginAsSuperAdmin();
        }

        HttpResponse response = HttpRequest.get(getBaseUrl() + "/system/user/getUserInfo")
            .header("Content-Type", "application/json")
            .header("Authorization", loginToken)
            .execute();

        assertEquals(200, response.getStatus(), "HTTP 状态码应该是 200");

        JSONObject result = JSONUtil.parseObj(response.body());
        assertNotNull(result, "响应结果不应为 null");
        assertEquals(200, result.getInt("code"), "业务状态码应该是 200, 实际返回: " + result);

        JSONObject data = result.getJSONObject("data");
        assertNotNull(data, "用户信息不应为 null");

        log.info("使用有效 Token 访问成功");
    }

    @Test
    @Order(6)
    @DisplayName("测试无Token访问受保护接口 - 应失败")
    public void testAccessWithoutToken() {
        log.info("测试无 Token 访问受保护接口");

        HttpResponse response = HttpRequest.get(getBaseUrl() + "/system/user/getUserInfo")
            .header("Content-Type", "application/json")
            .execute();

        JSONObject result = JSONUtil.parseObj(response.body());
        assertNotNull(result, "响应结果不应为 null");
        assertNotEquals(200, result.getInt("code"), "无 Token 访问应返回错误");

        log.info("验证成功: 无 Token 访问返回错误, code={}, msg={}", result.getInt("code"), result.getStr("msg"));
    }

    @Test
    @Order(7)
    @DisplayName("测试使用无效Token访问受保护接口 - 应失败")
    public void testAccessWithInvalidToken() {
        log.info("测试使用无效 Token 访问受保护接口");

        String invalidToken = "Bearer invalid_token_12345";

        HttpResponse response = HttpRequest.get(getBaseUrl() + "/system/user/getUserInfo")
            .header("Content-Type", "application/json")
            .header("Authorization", invalidToken)
            .execute();

        JSONObject result = JSONUtil.parseObj(response.body());
        assertNotNull(result, "响应结果不应为 null");
        assertNotEquals(200, result.getInt("code"), "无效 Token 访问应返回错误");

        log.info("验证成功: 无效 Token 访问返回错误, code={}, msg={}", result.getInt("code"), result.getStr("msg"));
    }

    @Test
    @Order(8)
    @DisplayName("测试用户登出")
    public void testLogout() {
        log.info("测试用户登出");

        // 确保有 token
        if (loginToken == null || loginToken.isEmpty()) {
            loginToken = testLoginHelper.loginAsSuperAdmin();
        }

        HttpResponse response = HttpRequest.post(getBaseUrl() + "/auth/userLogout")
            .header("Content-Type", "application/json")
            .header("Authorization", loginToken)
            .body("")
            .execute();

        assertEquals(200, response.getStatus(), "HTTP 状态码应该是 200");

        JSONObject result = JSONUtil.parseObj(response.body());
        assertNotNull(result, "响应结果不应为 null");
        assertEquals(200, result.getInt("code"), "业务状态码应该是 200, 实际返回: " + result);

        log.info("登出成功");
    }

    @Test
    @Order(9)
    @DisplayName("测试使用已登出的Token访问接口 - 应失败")
    public void testAccessWithLoggedOutToken() {
        log.info("测试使用已登出的 Token 访问接口");

        if (loginToken == null || loginToken.isEmpty()) {
            log.warn("没有可用的已登出 token, 跳过此测试");
            return;
        }

        HttpResponse response = HttpRequest.get(getBaseUrl() + "/system/user/getUserInfo")
            .header("Content-Type", "application/json")
            .header("Authorization", loginToken)
            .execute();

        JSONObject result = JSONUtil.parseObj(response.body());
        assertNotNull(result, "响应结果不应为 null");
        assertNotEquals(200, result.getInt("code"), "已登出 Token 访问应返回错误");

        log.info("验证成功: 已登出 Token 访问返回错误, code={}, msg={}", result.getInt("code"), result.getStr("msg"));
    }

    @AfterAll
    public static void afterAll() {
        log.info("========== 认证接口测试结束 ==========");
    }
}
