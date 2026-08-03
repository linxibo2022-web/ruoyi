package plus.ruoyi.common.openapi;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import plus.ruoyi.common.test.base.BaseUnitTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * OpenAPI 广告接口测试
 * <p>
 * 【重要】此测试不启动 Spring 容器，需要手动启动 ruoyi-admin 项目后运行
 * <p>
 * 测试步骤：
 * 1. 启动 ruoyi-admin 项目（默认端口 5500）
 * 2. 在 AdController 的 addAd 接口上添加 @OpenApi 注解
 * 3. 运行此测试类
 * <p>
 * 测试内容：
 * - 请求头方式签名验证
 * - URL 参数方式签名验证
 *
 * @author 抓蛙师
 */
@Slf4j
@DisplayName("OpenAPI 接口测试")
public class OpenApiTest extends BaseUnitTest {

    // 测试服务器地址（需要手动启动 ruoyi-admin）
    private static final String BASE_URL = "http://127.0.0.1:5500";

    private static final String APP_KEY = "d4c0ed4bc5b049c8a144109f60c8abb9";
    private static final String APP_SECRET = "fcfe7ade592c4fcb9e6b8ec9e7c3134d";

    @BeforeAll
    static void beforeAll() {
        log.info("========================================");
        log.info("OpenAPI 接口测试");
        log.info("请确保 ruoyi-admin 项目已启动（端口: 5500）");
        log.info("请确保 AdController.addAd 接口已添加 @OpenApi 注解");
        log.info("========================================");
    }

    /**
     * 测试添加广告接口 - 请求头方式
     */
    @Test
    @DisplayName("测试添加广告 - 请求头方式")
    public void testAddAdWithHeader() {
        // 1. 构建请求数据
        String requestBody = """
            {
                "appid": "wx1234567890",
                "adUnitId": "adunit-12345",
                "adName": "测试广告位-请求头方式",
                "adType": "banner",
                "position": "home_top",
                "img": "https://example.com/ad.jpg",
                "description": "这是一个测试广告（请求头方式）",
                "sortOrder": 1,
                "status": "1",
                "remark": "OpenAPI测试数据-Header方式"
            }
            """;

        // 2. 生成签名
        long timestamp = System.currentTimeMillis();
        String sign = DigestUtil.md5Hex(APP_KEY + timestamp + APP_SECRET);

        log.info("========== 测试方式：请求头（Header）==========");
        log.info("请求URL: {}/base/ad/addAd", BASE_URL);
        log.info("AppKey: {}", APP_KEY);
        log.info("Timestamp: {}", timestamp);
        log.info("Sign: {}", sign);
        log.info("请求体: {}", requestBody);

        // 3. 发送 POST 请求
        HttpResponse response = HttpRequest.post(BASE_URL + "/base/ad/addAd")
            .header("Content-Type", "application/json")
            .header("X-App-Key", APP_KEY)
            .header("X-Timestamp", String.valueOf(timestamp))
            .header("X-Sign", sign)
            .body(requestBody)
            .execute();

        // 4. 输出结果
        log.info("响应状态码: {}", response.getStatus());
        log.info("响应内容: {}", response.body());

        // 5. 断言验证
        assertEquals(200, response.getStatus(), "HTTP 状态码应该是 200");
        assertEquals(200, JSONUtil.parseObj(response.body()).getInt("code"), "业务码应该是 200");
    }

    /**
     * 测试添加广告接口 - URL 参数方式
     * <p>
     * 需要在 AdController 中 addAd 接口开启 @OpenApi 注解方可测试
     */
    @Test
    @DisplayName("测试添加广告 - URL 参数方式")
    public void testAddAdWithUrlParams() {
        // 1. 构建请求数据
        String requestBody = """
            {
                "appid": "wx1234567890",
                "adUnitId": "adunit-67890",
                "adName": "测试广告位-URL参数方式",
                "adType": "banner",
                "position": "home_bottom",
                "img": "https://example.com/ad2.jpg",
                "description": "这是一个测试广告（URL参数方式）",
                "sortOrder": 2,
                "status": "1",
                "remark": "OpenAPI测试数据-URL参数方式"
            }
            """;

        // 2. 生成签名
        long timestamp = System.currentTimeMillis();
        String sign = DigestUtil.md5Hex(APP_KEY + timestamp + APP_SECRET);

        log.info("========== 测试方式：URL 参数 ==========");
        log.info("AppKey: {}", APP_KEY);
        log.info("Timestamp: {}", timestamp);
        log.info("Sign: {}", sign);
        log.info("请求体: {}", requestBody);

        // 3. 构建带查询参数的完整URL
        String fullUrl = String.format("%s/base/ad/addAd?appKey=%s&timestamp=%d&sign=%s",
            BASE_URL, APP_KEY, timestamp, sign);

        log.info("完整URL: {}", fullUrl);

        // 4. 发送 POST 请求（URL 参数已在 URL 中）
        HttpResponse response = HttpRequest.post(fullUrl)
            .header("Content-Type", "application/json")
            .body(requestBody)
            .execute();

        // 5. 输出结果
        log.info("响应状态码: {}", response.getStatus());
        log.info("响应内容: {}", response.body());

        // 6. 断言验证
        assertEquals(200, response.getStatus(), "HTTP 状态码应该是 200");
        assertEquals(200, JSONUtil.parseObj(response.body()).getInt("code"), "业务码应该是 200");
    }

    /**
     * 测试查询广告接口 - GET 请求使用 URL 参数
     * <p>
     * 这种方式最适合 URL 参数，可以直接在浏览器中测试
     */
    @Test
    @DisplayName("测试查询广告 - GET 请求 + URL 参数")
    public void testGetAdWithUrlParams() {
        Long adId = 1L; // 假设要查询的广告ID

        // 1. 生成签名
        long timestamp = System.currentTimeMillis();
        String sign = DigestUtil.md5Hex(APP_KEY + timestamp + APP_SECRET);

        log.info("========== 测试方式：GET 请求 + URL 参数 ==========");
        log.info("AppKey: {}", APP_KEY);
        log.info("Timestamp: {}", timestamp);
        log.info("Sign: {}", sign);

        // 2. 构建完整的URL（包含所有查询参数）
        String fullUrl = String.format(
            "%s/base/ad/getAd/%d?appKey=%s&timestamp=%d&sign=%s",
            BASE_URL, adId, APP_KEY, timestamp, sign
        );

        log.info("完整请求URL: {}", fullUrl);
        log.info("提示：可以直接复制上面的 URL 在浏览器中访问测试");

        // 3. 发送 GET 请求
        HttpResponse response = HttpRequest.get(fullUrl)
            .header("Content-Type", "application/json")
            .execute();

        // 4. 输出结果
        log.info("响应状态码: {}", response.getStatus());
        log.info("响应内容: {}", response.body());

        // 5. 断言验证
        assertEquals(200, response.getStatus(), "HTTP 状态码应该是 200");
    }
}
