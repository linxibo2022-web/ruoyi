package plus.ruoyi.common.test.base;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.dtflys.forest.config.ForestConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import plus.ruoyi.common.test.TestApplication;

import java.util.Map;

/**
 * Controller测试基类 - 基于真实 HTTP 请求的集成测试
 * <p>
 * 提供真实 HTTP 请求支持,用于测试完整的请求流程（包括拦截器、过滤器、签名验证等）
 *
 * <p>特性:
 * <ul>
 *   <li>继承 BaseSpringTest (Spring容器 + 性能监控 + 测试目录管理)</li>
 *   <li>启动完整 Web 容器 (RANDOM_PORT)</li>
 *   <li>使用 Hutool HttpRequest 发送真实 HTTP 请求</li>
 *   <li>提供便捷的 doGet/doPost/doPut/doDelete 方法</li>
 *   <li>支持自定义请求头（如 OpenAPI 签名）</li>
 * </ul>
 *
 * <p>使用示例:
 * <pre>
 * {@code @DisplayName("OpenAPI接口测试")}
 * public class OpenApiTest extends BaseControllerTest {
 *
 *     {@code @Test}
 *     {@code @DisplayName("测试添加广告")}
 *     public void testAddAd() {
 *         // 构建请求体
 *         String requestBody = "{\"adName\":\"测试广告\"}";
 *
 *         // 自定义请求头
 *         Map<String, String> headers = Map.of(
 *             "X-App-Key", "your-app-key",
 *             "X-Sign", "your-signature"
 *         );
 *
 *         // 发送 POST 请求
 *         HttpResponse response = doPost("/base/ad/addAd", requestBody, headers);
 *
 *         // 断言
 *         assertThat(response.getStatus()).isEqualTo(200);
 *         assertThat(JSONUtil.parseObj(response.body()).getInt("code")).isEqualTo(200);
 *     }
 * }
 * </pre>
 *
 * @author 抓蛙师
 */
@SpringBootTest(
    classes = TestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public abstract class BaseControllerTest extends BaseSpringTest {

    @LocalServerPort  // 获取随机端口
    protected int port;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected ForestConfiguration forestConfiguration;

    /**
     * 初始化 Forest 客户端 baseUrl 变量
     * <p>由于使用 RANDOM_PORT，需要在每个测试方法执行前动态设置
     */
    @BeforeEach
    public void initForestBaseUrl() {
        forestConfiguration.setVariable("baseUrl", getBaseUrl());
    }

    /**
     * 获取基础URL
     *
     * @return 基础URL，例如: http://127.0.0.1:8080
     */
    protected String getBaseUrl() {
        return "http://127.0.0.1:" + port;
    }

    // ==================== GET 请求 ====================

    /**
     * 执行 GET 请求
     *
     * @param path 请求路径（不含域名和端口），例如: /base/ad/getAd/1
     * @return HTTP 响应
     */
    protected HttpResponse doGet(String path) {
        return HttpRequest.get(getBaseUrl() + path)
            .header("Content-Type", "application/json")
            .execute();
    }

    /**
     * 执行 GET 请求（带自定义请求头）
     *
     * @param path    请求路径
     * @param headers 自定义请求头
     * @return HTTP 响应
     */
    protected HttpResponse doGet(String path, Map<String, String> headers) {
        HttpRequest request = HttpRequest.get(getBaseUrl() + path)
            .header("Content-Type", "application/json");
        headers.forEach(request::header);
        return request.execute();
    }

    // ==================== POST 请求 ====================

    /**
     * 执行 POST 请求（对象自动序列化为 JSON）
     *
     * @param path 请求路径
     * @param body 请求体对象
     * @return HTTP 响应
     */
    protected HttpResponse doPost(String path, Object body) {
        return HttpRequest.post(getBaseUrl() + path)
            .header("Content-Type", "application/json")
            .body(JSONUtil.toJsonStr(body))
            .execute();
    }

    /**
     * 执行 POST 请求（直接传 JSON 字符串）
     *
     * @param path     请求路径
     * @param jsonBody JSON 字符串
     * @return HTTP 响应
     */
    protected HttpResponse doPost(String path, String jsonBody) {
        return HttpRequest.post(getBaseUrl() + path)
            .header("Content-Type", "application/json")
            .body(jsonBody)
            .execute();
    }

    /**
     * 执行 POST 请求（带自定义请求头）
     *
     * @param path     请求路径
     * @param jsonBody JSON 字符串
     * @param headers  自定义请求头
     * @return HTTP 响应
     */
    protected HttpResponse doPost(String path, String jsonBody, Map<String, String> headers) {
        HttpRequest request = HttpRequest.post(getBaseUrl() + path)
            .header("Content-Type", "application/json");
        headers.forEach(request::header);
        request.body(jsonBody);
        return request.execute();
    }

    /**
     * 执行 POST 请求（无请求体）
     *
     * @param path 请求路径
     * @return HTTP 响应
     */
    protected HttpResponse doPost(String path) {
        return HttpRequest.post(getBaseUrl() + path)
            .header("Content-Type", "application/json")
            .execute();
    }

    // ==================== PUT 请求 ====================

    /**
     * 执行 PUT 请求（对象自动序列化为 JSON）
     *
     * @param path 请求路径
     * @param body 请求体对象
     * @return HTTP 响应
     */
    protected HttpResponse doPut(String path, Object body) {
        return HttpRequest.put(getBaseUrl() + path)
            .header("Content-Type", "application/json")
            .body(JSONUtil.toJsonStr(body))
            .execute();
    }

    /**
     * 执行 PUT 请求（直接传 JSON 字符串）
     *
     * @param path     请求路径
     * @param jsonBody JSON 字符串
     * @return HTTP 响应
     */
    protected HttpResponse doPut(String path, String jsonBody) {
        return HttpRequest.put(getBaseUrl() + path)
            .header("Content-Type", "application/json")
            .body(jsonBody)
            .execute();
    }

    // ==================== DELETE 请求 ====================

    /**
     * 执行 DELETE 请求
     *
     * @param path 请求路径
     * @return HTTP 响应
     */
    protected HttpResponse doDelete(String path) {
        return HttpRequest.delete(getBaseUrl() + path)
            .header("Content-Type", "application/json")
            .execute();
    }

    /**
     * 执行 DELETE 请求（带自定义请求头）
     *
     * @param path    请求路径
     * @param headers 自定义请求头
     * @return HTTP 响应
     */
    protected HttpResponse doDelete(String path, Map<String, String> headers) {
        HttpRequest request = HttpRequest.delete(getBaseUrl() + path)
            .header("Content-Type", "application/json");
        headers.forEach(request::header);
        return request.execute();
    }

    // ==================== 工具方法 ====================

    /**
     * 将对象转换为 JSON 字符串
     *
     * @param obj 对象
     * @return JSON 字符串
     */
    protected String toJson(Object obj) {
        return JSONUtil.toJsonStr(obj);
    }

    /**
     * 将 JSON 字符串转换为对象
     *
     * @param json  JSON 字符串
     * @param clazz 目标类型
     * @return 对象
     */
    protected <T> T fromJson(String json, Class<T> clazz) {
        return JSONUtil.toBean(json, clazz);
    }
}
