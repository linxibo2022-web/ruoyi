package plus.ruoyi.common.miniapp.channel;

import cn.binarywang.wx.miniapp.api.WxMaService;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.message.MessageChannel;
import plus.ruoyi.common.core.message.MessageContext;
import plus.ruoyi.common.core.message.MessageResult;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.miniapp.utils.WxMaSubscribeUtils;

import java.util.Map;

/**
 * 微信小程序订阅消息通道实现
 * <p>
 * 基于框架现有的 WxMaSubscribeUtils 实现统一消息接口
 * 支持微信小程序订阅消息推送
 * </p>
 * <p>
 * 特点：
 * - 官方推送通道，送达率高
 * - 需要用户授权订阅
 * - 支持模板消息
 * - 可直接跳转小程序页面
 * </p>
 * <p>
 * 适用场景：
 * - 订单状态通知
 * - 物流信息通知
 * - 预约提醒
 * - 活动通知
 * </p>
 * <p>
 * 使用要求：
 * - params.appid: 小程序appid (必填)
 * - params.openid: 用户openid (必填)
 * - params.templateId: 模板ID (必填)
 * - params.data: 模板数据 Map&lt;String,String&gt; (必填)
 * - params.page: 跳转页面路径 (可选)
 * - params.miniprogramState: 跳转版本(developer/trial/formal, 可选)
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * MessageContext context = MessageContext.ofParams(userId, Map.of(
 *     "appid", "wx1234567890",
 *     "openid", "oABC123",
 *     "templateId", "tpl_order_status",
 *     "data", Map.of(
 *         "thing1", "订单编号123456",
 *         "time2", "2025-01-01 10:00:00",
 *         "thing3", "您的订单已发货"
 *     ),
 *     "page", "pages/order/detail?id=123"
 * ));
 * </pre>
 *
 * @author YourName
 */
@Slf4j
public class MiniappMessageChannel implements MessageChannel {

    @Override
    public String getChannelType() {
        return "miniapp";
    }

    @Override
    public String getChannelName() {
        return "微信小程序订阅消息";
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
        String page = (String) params.get("page");
        String miniprogramState = (String) params.get("miniprogramState");

        // 调用框架现有的 WxMaSubscribeUtils 发送订阅消息
        // WxMaSubscribeUtils 内部已处理所有异常，只返回 true/false，不会抛出异常
        boolean success;
        if (StringUtils.isNotBlank(miniprogramState)) {
            success = WxMaSubscribeUtils.send(appid, openid, templateId, data, page, miniprogramState);
        } else {
            success = WxMaSubscribeUtils.send(appid, openid, templateId, data, page);
        }

        if (success) {
            MessageResult result = MessageResult.success(
                context.getMessageId(),
                getChannelType(),
                context.getUserIds() != null && !context.getUserIds().isEmpty()
                    ? context.getUserIds().get(0) : null
            );
            result.setCostTime(System.currentTimeMillis() - startTime);

            log.info("小程序订阅消息发送成功: appid={}, openid={}, templateId={}, costTime={}ms",
                appid, openid, templateId, result.getCostTime());

            return result;

        } else {
            // WxMaSubscribeUtils 已记录详细错误日志（包含错误码和错误信息）
            log.warn("小程序订阅消息发送失败: appid={}, openid={}, templateId={}",
                appid, openid, templateId);

            return MessageResult.fail(
                context.getMessageId(),
                getChannelType(),
                context.getUserIds() != null && !context.getUserIds().isEmpty()
                    ? context.getUserIds().get(0) : null,
                "SEND_FAIL",
                "小程序订阅消息发送失败，详细原因见上方日志（常见：用户未授权、模板参数错误、页面路径错误）"
            );
        }
    }

    @Override
    public boolean isEnabled() {
        // 小程序通道始终启用
        // 具体的小程序配置由业务层控制（通过 appid 参数）
        return true;
    }

    @Override
    public int getPriority() {
        // 小程序订阅消息优先级较高，官方通道
        return 4;
    }

    @Override
    public boolean healthCheck() {
        try {
            // 检查 WxMaService 是否可用
            // 如果 Spring 容器中没有 WxMaService，说明小程序模块未正确配置
            return SpringUtils.getBean(WxMaService.class) != null;
        } catch (Exception e) {
            log.warn("小程序通道健康检查失败: WxMaService 未找到，请检查小程序配置", e);
            return false;
        }
    }

    @Override
    public boolean supportTenant(String tenantId) {
        // 小程序支持所有租户
        // 如果需要租户级别的小程序配置隔离，可以在这里实现
        return true;
    }
}
