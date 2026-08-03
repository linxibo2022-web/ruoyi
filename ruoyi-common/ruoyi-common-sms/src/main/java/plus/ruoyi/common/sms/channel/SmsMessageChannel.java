package plus.ruoyi.common.sms.channel;

import lombok.extern.slf4j.Slf4j;
import org.dromara.sms4j.api.SmsBlend;
import org.dromara.sms4j.api.entity.SmsResponse;
import org.dromara.sms4j.core.factory.SmsFactory;
import plus.ruoyi.common.core.message.MessageChannel;
import plus.ruoyi.common.core.message.MessageContext;
import plus.ruoyi.common.core.message.MessageResult;
import plus.ruoyi.common.core.utils.StringUtils;

/**
 * 短信消息通道实现
 * <p>
 * 基于 SMS4J 框架实现统一消息接口
 * SMS4J 支持多家短信服务商：阿里云、腾讯云、华为云、网易云等
 * </p>
 * <p>
 * 特点：
 * - 多平台支持(阿里云/腾讯云/华为云等)
 * - 自动重试机制
 * - 发送记录缓存
 * - 支持模板短信和普通短信
 * </p>
 * <p>
 * 适用场景：
 * - 验证码发送
 * - 订单通知
 * - 营销短信
 * - 重要通知(需要确保送达)
 * </p>
 * <p>
 * 使用要求：
 * - params 中必须包含 phone(手机号)
 * - content 为短信内容
 * - 可选: templateId(模板ID), templateParams(模板参数)
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * // 方式1: 发送纯文本短信
 * MessageContext context = MessageContext.of(userId, "您的验证码是123456")
 *     .setParams(Map.of("phone", "13800138000"));
 *
 * // 方式2: 发送模板短信 (推荐)
 * MessageContext context = MessageContext.ofParams(userId, Map.of(
 *     "phone", "13800138000",
 *     "templateId", "SMS_123456",
 *     "templateParams", Map.of("code", "123456")
 * ));
 * </pre>
 *
 * @author YourName
 */
@Slf4j
public class SmsMessageChannel implements MessageChannel {

    @Override
    public String getChannelType() {
        return "sms";
    }

    @Override
    public String getChannelName() {
        return "短信推送";
    }

    @Override
    public MessageResult send(MessageContext context) {
        long startTime = System.currentTimeMillis();

        // 参数校验
        if (context == null || context.getParams() == null) {
            return MessageResult.fail(
                context != null ? context.getMessageId() : null,
                getChannelType(),
                null,
                "PARAM_ERROR",
                "消息上下文或扩展参数不能为空"
            );
        }

        // 获取手机号
        String phone = (String) context.getParams().get("phone");
        if (StringUtils.isBlank(phone)) {
            return MessageResult.fail(
                context.getMessageId(),
                getChannelType(),
                context.getUserIds() != null && !context.getUserIds().isEmpty()
                    ? context.getUserIds().get(0) : null,
                "PARAM_ERROR",
                "缺少必填参数: params.phone"
            );
        }

        // 获取 SMS4J 实例 (参考 CaptchaController 的实现)
        // 默认使用 "config1" 配置，业务层可以通过 params.configId 指定其他配置
        String configId = (String) context.getParams().getOrDefault("configId", "config1");
        SmsBlend smsBlend = SmsFactory.getSmsBlend(configId);

        // 发送短信
        SmsResponse response;
        String templateId = (String) context.getParams().get("templateId");

        if (StringUtils.isNotBlank(templateId)) {
            // 发送模板短信 (推荐)
            log.debug("发送模板短信: phone={}, templateId={}, configId={}", phone, templateId, configId);
            response = smsBlend.sendMessage(phone, templateId);
        } else {
            // 发送普通短信
            if (StringUtils.isBlank(context.getContent())) {
                return MessageResult.fail(
                    context.getMessageId(),
                    getChannelType(),
                    context.getUserIds() != null && !context.getUserIds().isEmpty()
                        ? context.getUserIds().get(0) : null,
                    "PARAM_ERROR",
                    "消息内容不能为空"
                );
            }
            log.debug("发送普通短信: phone={}, content={}, configId={}", phone, context.getContent(), configId);
            response = smsBlend.sendMessage(phone, context.getContent());
        }

        // 判断发送结果
        if (response != null && response.isSuccess()) {
            MessageResult result = MessageResult.success(
                context.getMessageId(),
                getChannelType(),
                context.getUserIds() != null && !context.getUserIds().isEmpty()
                    ? context.getUserIds().get(0) : null
            );
            result.setCostTime(System.currentTimeMillis() - startTime);
            // SMS4J 的 SmsResponse 使用 getData() 获取厂商原返回体
            if (response.getData() != null) {
                result.setThirdPartyMsgId(response.getData().toString());
                result.setExtra(response.getData().toString());
            }

            log.info("短信发送成功: phone={}, messageId={}, configId={}, costTime={}ms",
                phone, context.getMessageId(), response.getConfigId(), result.getCostTime());

            return result;

        } else {
            String errorMsg = "短信发送失败";
            String errorCode = "SMS_SEND_FAIL";

            // SMS4J 失败时，data 字段可能包含错误信息
            if (response != null && response.getData() != null) {
                errorMsg = "短信发送失败: " + response.getData().toString();
            } else if (response == null) {
                errorMsg = "短信发送失败，未返回响应";
                errorCode = "NO_RESPONSE";
            }

            log.error("短信发送失败: phone={}, messageId={}, errorCode={}, errorMsg={}",
                phone, context.getMessageId(), errorCode, errorMsg);

            MessageResult failResult = MessageResult.fail(
                context.getMessageId(),
                getChannelType(),
                context.getUserIds() != null && !context.getUserIds().isEmpty()
                    ? context.getUserIds().get(0) : null,
                errorCode,
                errorMsg
            );

            // 记录厂商返回的原始数据
            if (response != null && response.getData() != null) {
                failResult.setExtra(response.getData().toString());
            }

            return failResult;
        }
    }

    @Override
    public boolean isEnabled() {
        // 短信通道始终启用
        // SMS4J 通过配置文件管理，不需要额外的开关控制
        return true;
    }

    @Override
    public int getPriority() {
        // 短信成本较高，优先级中等
        return 3;
    }

    @Override
    public boolean healthCheck() {
        try {
            // 检查 SMS4J 配置是否正确
            // 尝试获取默认配置，如果配置不存在会抛出异常
            SmsBlend smsBlend = SmsFactory.getSmsBlend("config1");
            return smsBlend != null;
        } catch (Exception e) {
            log.warn("短信通道健康检查失败: SMS4J 配置未找到或无效", e);
            return false;
        }
    }

    @Override
    public boolean supportTenant(String tenantId) {
        // 短信支持所有租户
        // 如果需要租户级别的短信配置隔离，可以在这里实现
        return true;
    }
}
