package plus.ruoyi.common.pay.unionpay.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.pay.exception.PayException;

import java.util.Map;

/**
 * 银联HTTP工具类
 *
 * 使用Hutool的HttpUtil进行HTTP请求
 *
 * @author 抓蛙师
 */
@Slf4j
public class UnionpayHttpUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 请求超时时间（毫秒）
     */
    private static final int TIMEOUT = 30000;

    /**
     * POST请求 - JSON格式
     *
     * @param url     请求地址
     * @param params  请求参数
     * @param headers 请求头
     * @return 响应结果
     */
    public static String postJson(String url, Map<String, Object> params, Map<String, String> headers) {
        try {
            log.debug("银联POST请求(JSON): url={}, params={}", url, params);

            // 构建请求体
            String jsonBody = OBJECT_MAPPER.writeValueAsString(params);

            // 发送请求
            HttpRequest request = HttpRequest.post(url)
                .body(jsonBody)
                .contentType("application/json; charset=utf-8")
                .timeout(TIMEOUT);

            // 添加请求头
            if (headers != null) {
                headers.forEach(request::header);
            }

            // 执行请求
            try (HttpResponse response = request.execute()) {
                if (!response.isOk()) {
                    throw new PayException("银联请求失败: HTTP " + response.getStatus());
                }

                String result = response.body();
                log.debug("银联响应结果: {}", result);

                return result;
            }

        } catch (Exception e) {
            log.error("银联HTTP请求异常: url={}", url, e);
            throw new PayException("银联HTTP请求异常: " + e.getMessage());
        }
    }

    /**
     * POST请求 - 表单格式
     *
     * @param url     请求地址
     * @param params  请求参数
     * @param headers 请求头
     * @return 响应结果
     */
    public static String postForm(String url, Map<String, Object> params, Map<String, String> headers) {
        try {
            log.debug("银联POST请求(Form): url={}, params={}", url, params);

            // 发送请求
            HttpRequest request = HttpRequest.post(url)
                .form(params)
                .timeout(TIMEOUT);

            // 添加请求头
            if (headers != null) {
                headers.forEach(request::header);
            }

            // 执行请求
            try (HttpResponse response = request.execute()) {
                if (!response.isOk()) {
                    throw new PayException("银联请求失败: HTTP " + response.getStatus());
                }

                String result = response.body();
                log.debug("银联响应结果: {}", result);

                return result;
            }

        } catch (Exception e) {
            log.error("银联HTTP请求异常: url={}", url, e);
            throw new PayException("银联HTTP请求异常: " + e.getMessage());
        }
    }

    /**
     * GET请求
     *
     * @param url     请求地址
     * @param params  请求参数
     * @param headers 请求头
     * @return 响应结果
     */
    public static String get(String url, Map<String, Object> params, Map<String, String> headers) {
        try {
            log.debug("银联GET请求: url={}, params={}", url, params);

            // 发送请求
            HttpRequest request = HttpRequest.get(url)
                .form(params)
                .timeout(TIMEOUT);

            // 添加请求头
            if (headers != null) {
                headers.forEach(request::header);
            }

            // 执行请求
            try (HttpResponse response = request.execute()) {
                if (!response.isOk()) {
                    throw new PayException("银联请求失败: HTTP " + response.getStatus());
                }

                String result = response.body();
                log.debug("银联响应结果: {}", result);

                return result;
            }

        } catch (Exception e) {
            log.error("银联HTTP请求异常: url={}", url, e);
            throw new PayException("银联HTTP请求异常: " + e.getMessage());
        }
    }
}
