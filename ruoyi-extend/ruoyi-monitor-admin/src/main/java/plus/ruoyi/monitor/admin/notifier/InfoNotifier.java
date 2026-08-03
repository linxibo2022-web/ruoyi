package plus.ruoyi.monitor.admin.notifier;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.net.URLEncodeUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.utils.DateUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.mail.utils.MailUtils;

import plus.ruoyi.monitor.admin.event.NotifierEvent;
import plus.ruoyi.monitor.admin.properties.NotifyProperties;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * 信息通知服务
 * 负责处理服务状态变更通知，支持邮件和WebHook两种通知方式
 *
 * @author AprilWind
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class InfoNotifier {

    private final NotifyProperties notifyProperties;

    /**
     * 处理服务通知事件
     * 监听所有服务状态变更事件，发送WebHook通知
     *
     * @param notifier 通知事件对象
     */
    @Async
    @EventListener
    public void infoNotification(NotifierEvent notifier) {
        sendWebHook(notifier);
    }

    /**
     * 处理服务异常通知事件
     * 目前监听所有事件，可通过@EventListener的condition属性来过滤特定状态
     * 例如：@EventListener(condition = "#notifier.status == 'DOWN'")
     *
     * @param notifier 通知事件对象
     */
    @Async
    @EventListener
    public void errNotification(NotifierEvent notifier) {
        sendMail(notifier);
    }

    /**
     * 发送邮件通知
     *
     * @param notifier 包含通知信息的对象
     */
    public void sendMail(NotifierEvent notifier) {
        NotifyProperties.Mail mail = notifyProperties.getMail();
        if (!mail.getEnabled()) {
            return;
        }

        // 格式化邮件内容
        String message = StringUtils.format(mail.getTemplate(),
            notifier.getRegisterName(),
            notifier.getInstanceId(),
            notifier.getStatusName(),
            notifier.getStatus(),
            notifier.getServiceUrl(),
            DateUtils.getTime());

        try {
            if (StringUtils.isBlank(mail.getTo())) {
                log.error("请设置收件人");
                return;
            }

            // 发送HTML格式邮件
            MailUtils.sendHtml(mail.getTo(), notifier.getRegisterName() + notifier.getStatusName(), message);
            log.info("邮件已发送至: {}", mail.getTo());
        } catch (Exception e) {
            log.error("邮件发送失败: ", e);
        }
    }

    /**
     * 发送WebHook通知
     *
     * @param notifier 包含通知信息的对象
     */
    public void sendWebHook(NotifierEvent notifier) {
        NotifyProperties.WebHook webHook = notifyProperties.getWebHook();
        if (!webHook.getEnabled()) {
            return;
        }

        String title = notifier.getRegisterName() + notifier.getStatusName();
        // 格式化WebHook消息内容
        String message = StringUtils.format(webHook.getTemplate(),
            title,
            notifier.getRegisterName(),
            notifier.getInstanceId(),
            notifier.getStatusName(),
            notifier.getStatus(),
            notifier.getServiceUrl(),
            DateUtils.getTime());

        try {
            sendWebHookMessage(webHook, title, message);
            log.info("WebHook消息已发送至: {}", webHook.getUrl());
        } catch (Exception e) {
            log.error("WebHook消息发送失败: ", e);
        }
    }

    /**
     * 发送WebHook消息到指定地址
     * 支持不同的认证方式：无认证、签名认证、密码认证
     *
     * @param webHook         WebHook配置
     * @param title           消息标题
     * @param markdownMessage 消息内容（Markdown格式）
     */
    private void sendWebHookMessage(NotifyProperties.WebHook webHook, String title, String markdownMessage) throws Exception {
        String url = webHook.getUrl();
        if (StringUtils.isBlank(url)) {
            throw ServiceException.of("请设置WebHook地址");
        }

        // 根据认证类型处理URL
        switch (webHook.getType()) {
            //默认
            case "0":
                break;
            //密钥
            case "1":
                if (StringUtils.isBlank(webHook.getSecret())) {
                    throw ServiceException.of("请设置密钥");
                }
                url = url + generateSign(webHook.getSecret());
                break;
            //密码
            case "2":
                if (StringUtils.isBlank(webHook.getSecret())) {
                    throw ServiceException.of("请设置密码");
                }
                //待实现
                url = url + generateSign(webHook.getSecret());
                break;
            default:
                throw new RuntimeException("认证类型有误");
        }

        // 添加关键词前缀（用于某些webhook过滤）
        if (StringUtils.isNotBlank(webHook.getKeywords())) {
            title = "(" + webHook.getKeywords() + ")" + title;
        }

        // 构造钉钉/企业微信格式的消息体
        Map<String, Object> messageBody = new HashMap<>();
        messageBody.put("msgtype", "markdown");

        Map<String, String> markdownContent = new HashMap<>();
        markdownContent.put("title", title);
        markdownContent.put("text", markdownMessage);
        messageBody.put("markdown", markdownContent);

        // 发送HTTP请求
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonMessage = objectMapper.writeValueAsString(messageBody);

        // 创建HTTP客户端
        HttpRequest request = HttpUtil.createPost(url)
            .body(jsonMessage)
            .contentType("application/json");

        // 发送请求并获取响应
        HttpResponse response = request.execute();

        // 处理响应
        if (response.isOk()) {
            JsonNode jsonNode = objectMapper.readTree(response.body());
            int errcode = jsonNode.has("errcode") ? jsonNode.get("errcode").asInt() : -1;
            String errmsg = jsonNode.has("errmsg") ? jsonNode.get("errmsg").asText() : "Unknown error";

            if (errcode == 0) {
                log.info("WebHook消息发送成功");
            } else {
                log.error("WebHook消息发送失败: errcode={}, errmsg={}", errcode, errmsg);
            }
        } else {
            log.error("WebHook消息发送失败: {}", response.body());
        }
    }

    /**
     * 生成签名认证参数
     * 用于钉钉等需要签名验证的WebHook
     *
     * @param secret 密钥
     * @return 签名参数字符串
     */
    private String generateSign(String secret) {
        Long timestamp = System.currentTimeMillis();
        String stringToSign = timestamp + "\n" + secret;
        String sign;

        try {
            // 使用HmacSHA256算法生成签名
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            sign = URLEncodeUtil.encode(Base64.encode(signData), StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("签名生成失败", e);
        }

        return StringUtils.format("&timestamp={}&sign={}", timestamp, sign);
    }
}
