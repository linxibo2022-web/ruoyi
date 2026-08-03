package plus.ruoyi.common.pay.utils;

import cn.hutool.core.net.NetUtil;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.exception.ServiceException;

/**
 * 支付回调地址构建工具
 *
 * @author 抓蛙师
 */
@Slf4j
public class PayNotifyUrlBuilder {

    /**
     * 回调地址基础路径
     */
    private static final String NOTIFY_BASE_PATH = "/payment/notify";

    /**
     * 构建微信支付回调地址
     *
     * @param baseApi 基础API地址
     * @param appid 微信APPID
     * @return 完整的回调地址
     */
    public static String buildWechatNotifyUrl(String baseApi, String appid) {
        validateUrlSecurity(baseApi);

        // 移除末尾的斜杠
        if (baseApi.endsWith("/")) {
            baseApi = baseApi.substring(0, baseApi.length() - 1);
        }

        return baseApi + NOTIFY_BASE_PATH + "/wechat/" + appid;
    }

    /**
     * 构建支付宝回调地址
     *
     * @param baseApi 基础API地址
     * @param appid 支付宝APPID
     * @return 完整的回调地址
     */
    public static String buildAlipayNotifyUrl(String baseApi, String appid) {
        validateUrlSecurity(baseApi);

        // 移除末尾的斜杠
        if (baseApi.endsWith("/")) {
            baseApi = baseApi.substring(0, baseApi.length() - 1);
        }

        return baseApi + NOTIFY_BASE_PATH + "/alipay/" + appid;
    }

    /**
     * 验证URL安全性，检查是否为内网地址
     *
     * @param url 要验证的URL
     * @throws ServiceException 如果URL不安全
     */
    private static void validateUrlSecurity(String url) {
        if (StrUtil.isBlank(url)) {
            log.error("回调地址构建失败: 未配置 app.base-api");
            throw new ServiceException(
                "支付配置错误: 请在 application.yml 中配置 app.base-api\n" +
                "示例: http://yourdomain.com 或 https://yourdomain.com"
            );
        }

        // 验证URL格式
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            log.error("回调地址构建失败: app.base-api 格式不正确,必须以 http:// 或 https:// 开头, 当前值: {}", url);
            throw new ServiceException(
                "支付配置错误: app.base-api 必须以 http:// 或 https:// 开头\n" +
                "当前值: " + url + "\n" +
                "正确示例: http://yourdomain.com 或 https://yourdomain.com\n" +
                "错误示例: 127.0.0.1:5500 (缺少协议前缀)"
            );
        }

        try {
            UrlBuilder urlBuilder = UrlBuilder.of(url);
            String host = urlBuilder.getHost();

            if (StrUtil.isBlank(host)) {
                throw new ServiceException("无效的URL格式: " + url);
            }

            // 使用 Hutool 获取IP并检查是否为内网（包括回环地址）
            String ip = NetUtil.getIpByHost(host);
            if (StrUtil.isNotBlank(ip) && NetUtil.isInnerIP(ip)) {
                log.error("回调地址不能使用内网IP: url={}, host={}, ip={}", url, host, ip);
                throw new ServiceException(
                    "支付配置错误: 回调地址不能使用内网IP\n" +
                    "当前地址: " + url + "\n" +
                    "解析IP: " + ip + "\n" +
                    "原因: 第三方支付平台(微信/支付宝)无法访问内网地址进行支付回调\n" +
                    "解决方案:\n" +
                    "1. 生产环境: 配置公网域名 (如 https://yourdomain.com)\n" +
                    "2. 开发环境: 使用内网穿透工具 (如 natapp、ngrok、cpolar)\n" +
                    "3. 测试环境: 配置测试服务器的公网地址"
                );
            }

        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("URL解析失败: url={}, error={}", url, e.getMessage(), e);
            throw new ServiceException("URL解析失败: " + e.getMessage());
        }
    }

    /**
     * 解析回调地址，提取支付类型和商户标识
     *
     * @param notifyUrl 回调地址
     * @return 包含支付类型和商户标识的数组 [paymentType, merchantId]，解析失败返回null
     */
    public static String[] parseNotifyUrl(String notifyUrl) {
        if (notifyUrl == null || !notifyUrl.contains(NOTIFY_BASE_PATH)) {
            return null;
        }

        try {
            // 提取路径部分
            String path = notifyUrl;
            int pathIndex = notifyUrl.indexOf(NOTIFY_BASE_PATH);
            if (pathIndex != -1) {
                path = notifyUrl.substring(pathIndex);
            }

            // 移除查询参数
            int queryIndex = path.indexOf('?');
            if (queryIndex != -1) {
                path = path.substring(0, queryIndex);
            }

            // 分割路径
            String[] parts = path.split("/");
            if (parts.length >= 4) {
                String paymentType = parts[parts.length - 2];
                String merchantId = parts[parts.length - 1];
                return new String[]{paymentType, merchantId};
            }
        } catch (Exception e) {
            log.warn("解析回调地址失败: {}", notifyUrl, e);
        }

        return null;
    }

    /**
     * 验证回调地址格式是否正确
     *
     * @param notifyUrl 回调地址
     * @return true-格式正确，false-格式错误
     */
    public static boolean isValidNotifyUrl(String notifyUrl) {
        String[] parsed = parseNotifyUrl(notifyUrl);
        return parsed != null && parsed.length == 2 &&
               parsed[0] != null && !parsed[0].trim().isEmpty() &&
               parsed[1] != null && !parsed[1].trim().isEmpty();
    }
}
