package plus.ruoyi.business.api;

import cn.dev33.satoken.annotation.SaIgnore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;
import plus.ruoyi.common.core.dict.DictPaymentMethod;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.utils.ServletUtils;
import plus.ruoyi.common.pay.domain.request.NotifyRequest;
import plus.ruoyi.common.pay.domain.response.NotifyResponse;
import plus.ruoyi.common.pay.service.PayService;

import jakarta.servlet.http.HttpServletRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 统一支付回调控制器
 * <p>
 * 职责：
 * 1. 接收各种支付方式的回调通知
 * 2. 解析不同格式的回调数据（XML、表单参数）
 * 3. 构建标准的回调请求对象
 * 4. 委托给PaymentManagerService进行业务处理
 * 5. 返回符合各支付平台要求的响应格式
 * <p>
 * 回调地址格式：/payment/notify/{paymentType}/{merchantId}
 * <p>
 * 支持的支付类型：
 * - wechat: 微信支付（XML格式数据）
 * - alipay: 支付宝支付（表单参数）
 * - unionpay: 银联支付（表单参数）
 * - balance: 余额支付（通常不需要回调）
 * <p>
 * 设计原则：
 * - 控制器只负责HTTP层面的处理，不涉及具体业务逻辑
 * - 统一的错误处理和日志记录
 * - 根据支付类型返回对应格式的响应
 * <p>
 * 只有当支付模块启用时才加载此控制器
 *
 * @author 抓蛙师
 */
@SaIgnore
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/payment/notify")
@ConditionalOnProperty(prefix = "module", name = "pay-enabled", havingValue = "true", matchIfMissing = true)
public class PayNotifyController {

    /** 支付服务 */
    private final PayService payService;

    /**
     * 统一支付回调入口
     * <p>
     * 这是所有支付平台回调的统一入口，通过路径参数区分不同的支付方式
     *
     * @param paymentType 支付类型 (wechat/alipay/unionpay/balance)
     * @param merchantId  商户标识 (微信商户号/支付宝AppId/银联商户号等)
     * @param request     HTTP请求对象
     * @return 响应字符串（格式根据支付类型而定）
     */
    @PostMapping("/{paymentType}/{merchantId}")
    public String unifiedNotify(@PathVariable String paymentType,
                                @PathVariable String merchantId,
                                HttpServletRequest request) {
        long startTime = System.currentTimeMillis();
        String clientIp = ServletUtils.getClientIP();

        try {
            log.info("接收到{}支付回调: merchantId={}, clientIp={}, userAgent={}",
                paymentType, merchantId, clientIp, request.getHeader("User-Agent"));

            // 1. 解析支付方式枚举
            DictPaymentMethod paymentMethod = DictPaymentMethod.getByValue(paymentType);

            // 2. 根据支付方式构建标准的回调请求对象
            NotifyRequest notifyRequest = buildNotifyRequest(paymentMethod, merchantId, request);

            // 3. 委托给PayService进行统一处理
            NotifyResponse response = payService.handleNotify(paymentMethod, notifyRequest);

            long costTime = System.currentTimeMillis() - startTime;
            log.info("{}支付回调处理完成: merchantId={}, result={}, costTime={}ms",
                paymentType, merchantId, response.getReturnCode(), costTime);

            // 4. 根据支付类型返回对应格式的响应
            return formatResponseByPaymentType(paymentType, response);

        } catch (IllegalArgumentException e) {
            // 参数错误（如不支持的支付类型）
            long costTime = System.currentTimeMillis() - startTime;
            log.warn("支付回调参数错误: paymentType={}, merchantId={}, costTime={}ms, error={}",
                paymentType, merchantId, costTime, e.getMessage());

            NotifyResponse failResponse = NotifyResponse.fail("参数错误: " + e.getMessage());
            return formatResponseByPaymentType(paymentType, failResponse);

        } catch (UnsupportedOperationException e) {
            // 不支持的操作（如某支付方式不支持回调）
            long costTime = System.currentTimeMillis() - startTime;
            log.warn("不支持的回调操作: paymentType={}, merchantId={}, costTime={}ms, error={}",
                paymentType, merchantId, costTime, e.getMessage());

            NotifyResponse failResponse = NotifyResponse.success("该支付方式不需要回调处理");
            return formatResponseByPaymentType(paymentType, failResponse);

        } catch (Exception e) {
            // 系统异常
            long costTime = System.currentTimeMillis() - startTime;
            log.error("处理{}支付回调异常: merchantId={}, costTime={}ms, error={}",
                paymentType, merchantId, costTime, e.getMessage(), e);

            NotifyResponse failResponse = NotifyResponse.fail("系统异常");
            return formatResponseByPaymentType(paymentType, failResponse);
        }
    }

    /**
     * 根据支付方式构建标准的回调请求对象
     * <p>
     * 不同的支付平台使用不同的数据格式：
     * - 微信支付：XML格式
     * - 支付宝：表单参数
     * - 银联：表单参数
     * - 余额支付：通常不需要回调数据
     *
     * @param paymentMethod 支付方式枚举
     * @param merchantId    商户标识
     * @param request       HTTP请求对象
     * @return 标准化的回调请求对象
     * @throws IOException 读取请求数据失败
     */
    private NotifyRequest buildNotifyRequest(DictPaymentMethod paymentMethod, String merchantId,
                                             HttpServletRequest request) throws IOException {
        String clientIp = ServletUtils.getClientIP();

        switch (paymentMethod) {
            case WECHAT:
                return buildWechatNotifyRequest(merchantId, request, clientIp);
            case ALIPAY:
                return buildAlipayNotifyRequest(merchantId, request, clientIp);
            case UNIONPAY:
                return buildUnionpayNotifyRequest(merchantId, request, clientIp);
            case BALANCE:
                return buildBalanceNotifyRequest(merchantId, request, clientIp);
            default:
                throw new ServiceException("不支持的支付方式: " + paymentMethod.getLabel());
        }
    }

    /**
     * 构建微信支付回调请求对象
     * <p>
     * 微信支付回调数据格式：
     * - v2: XML格式数据
     * - v3: JSON格式数据 + 签名请求头
     * 根据Content-Type自动判断版本并构建相应的请求对象
     *
     * @param mchId    微信商户号
     * @param request  HTTP请求对象
     * @param clientIp 客户端IP
     * @return 微信支付回调请求对象
     * @throws IOException 读取回调数据失败
     */
    private NotifyRequest buildWechatNotifyRequest(String mchId, HttpServletRequest request, String clientIp)
        throws IOException {
        // 读取请求体数据
        String bodyData = readRequestBody(request);
        String contentType = request.getContentType();

        if (bodyData == null || bodyData.trim().isEmpty()) {
            log.warn("微信支付回调数据为空: mchId={}, contentType={}", mchId, contentType);
            return NotifyRequest.createWxPayNotifyRequest("", mchId, clientIp);
        }

        // 根据数据格式判断版本
        boolean isJsonFormat = isJsonContent(contentType, bodyData);

        if (isJsonFormat) {
            // v3回调：JSON格式 + 签名请求头
            log.debug("微信支付v3回调详情: mchId={}, dataLength={}, contentType={}",
                mchId, bodyData.length(), contentType);

            // 提取微信v3回调的签名请求头
            Map<String, String> headers = extractWxV3NotifyHeaders(request);

            return NotifyRequest.createWxPayV3NotifyRequest(bodyData, mchId, clientIp, headers);
        } else {
            // v2回调：XML格式
            log.debug("微信支付v2回调详情: mchId={}, dataLength={}, contentType={}",
                mchId, bodyData.length(), contentType);
            return NotifyRequest.createWxPayNotifyRequest(bodyData, mchId, clientIp);
        }
    }

    /**
     * 提取微信v3回调的签名请求头
     * <p>
     * 微信v3回调需要验证签名,签名信息在HTTP请求头中:
     * - Wechatpay-Timestamp: 时间戳
     * - Wechatpay-Nonce: 随机字符串
     * - Wechatpay-Serial: 证书序列号
     * - Wechatpay-Signature: 签名值
     *
     * @param request HTTP请求对象
     * @return 包含微信v3签名信息的请求头Map
     */
    private Map<String, String> extractWxV3NotifyHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();

        headers.put("Wechatpay-Timestamp", request.getHeader("Wechatpay-Timestamp"));
        headers.put("Wechatpay-Nonce", request.getHeader("Wechatpay-Nonce"));
        headers.put("Wechatpay-Serial", request.getHeader("Wechatpay-Serial"));
        headers.put("Wechatpay-Signature", request.getHeader("Wechatpay-Signature"));

        log.debug("微信v3回调请求头: Timestamp={}, Nonce={}, Serial={}, Signature=***",
            headers.get("Wechatpay-Timestamp"),
            headers.get("Wechatpay-Nonce"),
            headers.get("Wechatpay-Serial"));

        return headers;
    }

    /**
     * 构建支付宝回调请求对象
     * <p>
     * 支付宝使用表单参数传递回调数据
     *
     * @param appId    支付宝应用ID
     * @param request  HTTP请求对象
     * @param clientIp 客户端IP
     * @return 支付宝回调请求对象
     */
    private NotifyRequest buildAlipayNotifyRequest(String appId, HttpServletRequest request, String clientIp) {
        // 读取表单参数
        Map<String, String> params = readRequestParams(request);

        log.debug("支付宝回调详情: appId={}, paramsSize={}, contentType={}",
            appId, params.size(), request.getContentType());

        return NotifyRequest.createAlipayNotifyRequest(params, appId, clientIp);
    }

    /**
     * 构建银联支付回调请求对象
     * <p>
     * 银联支付使用表单参数传递回调数据
     *
     * @param merId    银联商户号
     * @param request  HTTP请求对象
     * @param clientIp 客户端IP
     * @return 银联支付回调请求对象
     */
    private NotifyRequest buildUnionpayNotifyRequest(String merId, HttpServletRequest request, String clientIp) {
        // 读取表单参数
        Map<String, String> params = readRequestParams(request);

        log.debug("银联支付回调详情: merId={}, paramsSize={}, contentType={}",
            merId, params.size(), request.getContentType());

        return NotifyRequest.builder()
            .formData(params)
            .mchId(merId)
            .clientIp(clientIp)
            .requestTime(System.currentTimeMillis())
            .notifyType("PAY")
            .build();
    }

    /**
     * 构建余额支付回调请求对象
     * <p>
     * 余额支付通常是同步的，一般不需要异步回调
     * 这里提供接口主要是为了架构的完整性
     *
     * @param merchantId 商户标识
     * @param request    HTTP请求对象
     * @param clientIp   客户端IP
     * @return 余额支付回调请求对象
     */
    private NotifyRequest buildBalanceNotifyRequest(String merchantId, HttpServletRequest request, String clientIp) {
        log.info("余额支付回调: merchantId={}, note=余额支付通常不需要回调", merchantId);

        return NotifyRequest.builder()
            .mchId(merchantId)
            .clientIp(clientIp)
            .requestTime(System.currentTimeMillis())
            .notifyType("BALANCE")
            .build();
    }

    // ===================== 私有方法 - 响应格式化 =====================

    /**
     * 根据支付类型格式化响应内容
     * <p>
     * 不同的支付平台要求不同的响应格式：
     * - 微信支付：XML格式
     * - 支付宝：纯文本（success/failure）
     * - 银联：纯文本
     * - 余额支付：纯文本
     *
     * @param paymentType 支付类型字符串
     * @param response    统一的回调响应对象
     * @return 符合支付平台要求的响应字符串
     */
    private String formatResponseByPaymentType(String paymentType, NotifyResponse response) {
        String normalizedPaymentType = paymentType.toLowerCase();

        switch (normalizedPaymentType) {
            case "wechat":
                return response.toWxXml();

            case "alipay":
                return response.toAliResponse();

            case "unionpay":
            case "balance":
                return response.getReturnMsg();

            default:
                // 默认返回简单的成功/失败信息
                return "SUCCESS".equals(response.getReturnCode()) ? "success" : "fail";
        }
    }


    // ============== 工具方法 ==============

    /**
     * 读取HTTP请求体内容
     * <p>
     * 主要用于读取微信支付的XML数据
     * 注意：请求体只能读取一次，所以要一次性读取完整内容
     * <p>
     * 为什么用 getInputStream() 而不是 getReader()：
     * 微信/支付宝/银联回调的 Content-Type 通常不带 charset（如 application/xml、text/xml），
     * 此时 Servlet 容器（Undertow/Tomcat）按 HTTP 规范默认用 ISO-8859-1 解码 getReader()，
     * 导致 XML 中的中文字段（如商品描述、附加数据）乱码，进而签名校验失败、回调处理失败。
     * 这里直接拿原始字节，自己用 UTF-8 解码，绕开容器的字符集判定。
     *
     * @param request HTTP请求对象
     * @return 请求体内容字符串
     * @throws IOException 读取失败
     */
    private String readRequestBody(HttpServletRequest request) throws IOException {
        try (InputStream is = request.getInputStream();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            is.transferTo(bos);
            return bos.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("读取请求体失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 读取HTTP请求参数
     * <p>
     * 主要用于读取支付宝、银联的表单参数
     * 同时会过滤敏感参数，避免在日志中泄露
     *
     * @param request HTTP请求对象
     * @return 参数映射表
     */
    private Map<String, String> readRequestParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();

        // 获取所有参数名
        Enumeration<String> parameterNames = request.getParameterNames();

        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String paramValue = request.getParameter(paramName);

            // 存储参数
            params.put(paramName, paramValue);

            // 记录日志（敏感信息不记录具体值）
            if (isSensitiveParam(paramName)) {
                log.debug("请求参数: {}=***敏感信息已隐藏***", paramName);
            } else {
                log.debug("请求参数: {}={}", paramName, paramValue);
            }
        }

        return params;
    }

    /**
     * 判断是否为敏感参数
     * <p>
     * 识别包含签名、密钥等敏感信息的参数名，避免在日志中记录其值
     *
     * @param paramName 参数名
     * @return 是否为敏感参数
     */
    private boolean isSensitiveParam(String paramName) {
        if (paramName == null) {
            return false;
        }

        String lowerParamName = paramName.toLowerCase();
        return lowerParamName.contains("sign") ||      // 签名相关
            lowerParamName.contains("key") ||       // 密钥相关
            lowerParamName.contains("password") ||  // 密码相关
            lowerParamName.contains("secret") ||    // 秘钥相关
            lowerParamName.contains("token");       // 令牌相关
    }

    /**
     * 判断回调数据是否为JSON格式
     * <p>
     * 通过Content-Type和数据内容特征判断：
     * - v2回调：XML格式，Content-Type通常为 text/xml 或 application/xml
     * - v3回调：JSON格式，Content-Type通常为 application/json
     *
     * @param contentType HTTP Content-Type头
     * @param bodyData    请求体数据
     * @return true=JSON格式(v3), false=XML格式(v2)
     */
    private boolean isJsonContent(String contentType, String bodyData) {
        // 1. 首先检查Content-Type
        if (contentType != null) {
            String lowerContentType = contentType.toLowerCase();
            if (lowerContentType.contains("application/json")) {
                return true;
            }
            if (lowerContentType.contains("xml")) {
                return false;
            }
        }

        // 2. 如果Content-Type不明确，通过数据内容特征判断
        if (bodyData != null && !bodyData.trim().isEmpty()) {
            String trimmedData = bodyData.trim();
            // JSON通常以 { 开头
            if (trimmedData.startsWith("{")) {
                return true;
            }
            // XML通常以 < 开头
            if (trimmedData.startsWith("<")) {
                return false;
            }
        }

        // 3. 默认认为是XML格式（保持向后兼容）
        return false;
    }
}
