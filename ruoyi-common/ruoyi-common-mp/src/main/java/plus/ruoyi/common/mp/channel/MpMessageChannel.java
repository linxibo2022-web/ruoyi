package plus.ruoyi.common.mp.channel;

import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import plus.ruoyi.common.core.message.MessageChannel;
import plus.ruoyi.common.core.message.MessageContext;
import plus.ruoyi.common.core.message.MessageResult;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.mp.utils.WxMpTemplateUtils;

import java.util.Map;

/**
 * 微信公众号模板消息通道实现
 * <p>
 * 基于框架现有的 WxMpTemplateUtils 实现统一消息接口
 * 支持微信公众号模板消息推送
 * </p>
 * <p>
 * 特点：
 * - 官方推送通道，送达率高
 * - 无需用户授权(用户关注公众号即可)
 * - 支持模板消息
 * - 可跳转H5页面或小程序
 * - 支持自定义字体颜色
 * </p>
 * <p>
 * 适用场景：
 * - 订单状态通知
 * - 支付成功通知
 * - 预约提醒
 * - 活动通知
 * - 重要业务通知
 * </p>
 * <p>
 * 使用要求：
 * - params.appid: 公众号appid (必填)
 * - params.openid: 用户openid (必填)
 * - params.templateId: 模板ID (必填)
 * - params.data: 模板数据 Map&lt;String,String&gt; (必填)
 * - params.url: 跳转H5链接 (可选)
 * - params.miniAppid: 跳转小程序appid (可选)
 * - params.miniPath: 跳转小程序路径 (可选)
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * // 示例1: 发送模板消息(跳转H5)
 * MessageContext context = MessageContext.ofParams(userId, Map.of(
 *     "appid", "wx9876543210",
 *     "openid", "oXYZ456",
 *     "templateId", "tpl_order_success",
 *     "data", Map.of(
 *         "first", "您的订单已发货",
 *         "keyword1", "订单编号123456",
 *         "keyword2", "2025-01-01 10:00:00",
 *         "remark", "感谢您的购买"
 *     ),
 *     "url", "https://example.com/order/123"
 * ));
 *
 * // 示例2: 跳转小程序
 * MessageContext context = MessageContext.ofParams(userId, Map.of(
 *     "appid", "wx9876543210",
 *     "openid", "oXYZ456",
 *     "templateId", "tpl_order_success",
 *     "data", Map.of(...),
 *     "miniAppid", "wx1234567890",
 *     "miniPath", "pages/order/detail?id=123"
 * ));
 * </pre>
 *
 * @author YourName
 */
@Slf4j
public class MpMessageChannel implements MessageChannel {

    @Override
    public String getChannelType() {
        return "mp";
    }

    @Override
    public String getChannelName() {
        return "微信公众号模板消息";
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

        Map<String, Object> params = context.getParams();

        // 必填参数校验
        String appid = (String) params.get("appid");
        String openid = (String) params.get("openid");
        String templateId = (String) params.get("templateId");
        @SuppressWarnings("unchecked")
        Map<String, String> data = (Map<String, String>) params.get("data");

        if (StringUtils.isBlank(appid)) {
            return MessageResult.fail(
                context.getMessageId(),
                getChannelType(),
                context.getUserIds() != null && !context.getUserIds().isEmpty()
                    ? context.getUserIds().get(0) : null,
                "PARAM_ERROR",
                "缺少必填参数: params.appid"
            );
        }

        if (StringUtils.isBlank(openid)) {
            return MessageResult.fail(
                context.getMessageId(),
                getChannelType(),
                context.getUserIds() != null && !context.getUserIds().isEmpty()
                    ? context.getUserIds().get(0) : null,
                "PARAM_ERROR",
                "缺少必填参数: params.openid"
            );
        }

        if (StringUtils.isBlank(templateId)) {
            return MessageResult.fail(
                context.getMessageId(),
                getChannelType(),
                context.getUserIds() != null && !context.getUserIds().isEmpty()
                    ? context.getUserIds().get(0) : null,
                "PARAM_ERROR",
                "缺少必填参数: params.templateId"
            );
        }

        if (data == null || data.isEmpty()) {
            return MessageResult.fail(
                context.getMessageId(),
                getChannelType(),
                context.getUserIds() != null && !context.getUserIds().isEmpty()
                    ? context.getUserIds().get(0) : null,
                "PARAM_ERROR",
                "缺少必填参数: params.data"
            );
        }

        // 可选参数
        String url = (String) params.get("url");
        String miniAppid = (String) params.get("miniAppid");
        String miniPath = (String) params.get("miniPath");

        // 调用框架现有的 WxMpTemplateUtils 发送模板消息
        // WxMpTemplateUtils 内部已处理所有异常，发送成功返回 msgId，失败返回 null
        String msgId;

        if (StringUtils.isNotBlank(miniAppid) && StringUtils.isNotBlank(miniPath)) {
            // 跳转小程序
            msgId = WxMpTemplateUtils.sendToMiniProgram(appid, openid, templateId, data, miniAppid, miniPath);
        } else {
            // 跳转H5或不跳转
            msgId = WxMpTemplateUtils.send(appid, openid, templateId, data, url);
        }

        if (StringUtils.isNotBlank(msgId)) {
            MessageResult result = MessageResult.success(
                context.getMessageId(),
                getChannelType(),
                context.getUserIds() != null && !context.getUserIds().isEmpty()
                    ? context.getUserIds().get(0) : null
            );
            result.setCostTime(System.currentTimeMillis() - startTime);
            result.setThirdPartyMsgId(msgId);

            log.info("公众号模板消息发送成功: appid={}, openid={}, templateId={}, msgId={}, costTime={}ms",
                appid, openid, templateId, msgId, result.getCostTime());

            return result;

        } else {
            // WxMpTemplateUtils 已记录详细错误日志（包含错误码和错误信息）
            log.warn("公众号模板消息发送失败: appid={}, openid={}, templateId={}",
                appid, openid, templateId);

            return MessageResult.fail(
                context.getMessageId(),
                getChannelType(),
                context.getUserIds() != null && !context.getUserIds().isEmpty()
                    ? context.getUserIds().get(0) : null,
                "SEND_FAIL",
                "公众号模板消息发送失败，详细原因见上方日志（常见：用户未关注、模板参数错误、access_token失效）"
            );
        }
    }

    @Override
    public boolean isEnabled() {
        // 公众号通道始终启用
        // 具体的公众号配置由业务层控制（通过 appid 参数）
        return true;
    }

    @Override
    public int getPriority() {
        // 公众号模板消息优先级中等，官方通道
        return 5;
    }

    @Override
    public boolean healthCheck() {
        try {
            // 检查 WxMpService 是否可用
            // 如果 Spring 容器中没有 WxMpService，说明公众号模块未正确配置
            return SpringUtils.getBean(WxMpService.class) != null;
        } catch (Exception e) {
            log.warn("公众号通道健康检查失败: WxMpService 未找到，请检查公众号配置", e);
            return false;
        }
    }

    @Override
    public boolean supportTenant(String tenantId) {
        // 公众号支持所有租户
        // 如果需要租户级别的公众号配置隔离，可以在这里实现
        return true;
    }
}
