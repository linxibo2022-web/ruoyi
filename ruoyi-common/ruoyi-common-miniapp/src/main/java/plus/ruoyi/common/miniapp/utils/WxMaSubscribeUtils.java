package plus.ruoyi.common.miniapp.utils;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaSubscribeMessage;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.core.utils.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 微信小程序订阅消息工具类
 * <p>
 * 提供小程序订阅消息发送的基础能力，仅负责调用微信API发送消息
 * 业务逻辑（如模板匹配、数据构建等）应在业务层实现
 * </p>
 *
 * <p>使用示例：</p>
 * <pre>
 * // 基础发送
 * Map&lt;String, String&gt; data = new HashMap&lt;&gt;();
 * data.put("thing1", "订单编号123456");
 * data.put("time2", "2025-01-01 10:00:00");
 * data.put("thing3", "您的订单已发货");
 * WxMaSubscribeUtils.send("appid", "openid", "templateId", data);
 *
 * // 带跳转页面
 * WxMaSubscribeUtils.send("appid", "openid", "templateId", data, "pages/order/detail?id=123");
 *
 * // 批量发送
 * List&lt;String&gt; openids = Arrays.asList("openid1", "openid2", "openid3");
 * int successCount = WxMaSubscribeUtils.batchSend("appid", openids, "templateId", data);
 * </pre>
 *
 * @author bkywksj
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WxMaSubscribeUtils {

    /**
     * 发送订阅消息
     *
     * @param appid      小程序appid
     * @param openid     用户openid
     * @param templateId 模板ID
     * @param data       模板数据（key为字段名，value为字段值）
     * @return 是否发送成功
     */
    public static boolean send(String appid, String openid, String templateId, Map<String, String> data) {
        return send(appid, openid, templateId, data, null, null);
    }

    /**
     * 发送订阅消息（带跳转页面）
     *
     * @param appid      小程序appid
     * @param openid     用户openid
     * @param templateId 模板ID
     * @param data       模板数据（key为字段名，value为字段值）
     * @param page       跳转页面路径（如：pages/index/index?id=123）
     * @return 是否发送成功
     */
    public static boolean send(String appid, String openid, String templateId,
                               Map<String, String> data, String page) {
        return send(appid, openid, templateId, data, page, null);
    }

    /**
     * 发送订阅消息（完整参数）
     *
     * @param appid            小程序appid
     * @param openid           用户openid
     * @param templateId       模板ID
     * @param data             模板数据（key为字段名，value为字段值）
     * @param page             跳转页面路径（如：pages/index/index?id=123）
     * @param miniprogramState 跳转小程序类型：developer(开发版)、trial(体验版)、formal(正式版，默认根据环境自动选择)
     * @return 是否发送成功
     */
    public static boolean send(String appid, String openid, String templateId,
                               Map<String, String> data, String page, String miniprogramState) {
        // 参数校验
        if (StringUtils.isBlank(appid)) {
            throw ServiceException.of("小程序appid不能为空");
        }
        if (StringUtils.isBlank(openid)) {
            throw ServiceException.of("用户openid不能为空");
        }
        if (StringUtils.isBlank(templateId)) {
            throw ServiceException.of("模板ID不能为空");
        }
        if (data == null || data.isEmpty()) {
            throw ServiceException.of("模板数据不能为空");
        }

        try {
            WxMaService wxMaService = SpringUtils.getBean(WxMaService.class);
            // 切换到指定的小程序配置
            wxMaService.switchover(appid);

            // 转换数据格式
            List<WxMaSubscribeMessage.MsgData> msgDataList = Lists.newArrayList();
            data.forEach((key, value) -> msgDataList.add(new WxMaSubscribeMessage.MsgData(key, value)));

            // 构建订阅消息
            WxMaSubscribeMessage.WxMaSubscribeMessageBuilder builder = WxMaSubscribeMessage.builder()
                .toUser(openid)
                .templateId(templateId)
                .data(msgDataList);

            // 设置跳转页面
            if (StringUtils.isNotBlank(page)) {
                builder.page(page);
            }

            // 设置小程序版本（默认根据环境自动选择）
            if (StringUtils.isNotBlank(miniprogramState)) {
                builder.miniprogramState(miniprogramState);
            } else {
                // 根据环境自动选择版本
                String env = SpringUtils.getActiveProfile();
                String envVersion = "dev".equals(env) ? "developer" : "formal";
                builder.miniprogramState(envVersion);
            }

            WxMaSubscribeMessage message = builder.build();

            log.debug("发送订阅消息 - appid:{}, openid:{}, templateId:{}, page:{}, data:{}",
                appid, openid, templateId, page, data);

            // 发送订阅消息
            wxMaService.getMsgService().sendSubscribeMsg(message);

            log.info("订阅消息发送成功 - openid:{}, templateId:{}", openid, templateId);
            return true;

        } catch (WxErrorException e) {
            // 常见错误码说明：
            // 43101: 用户拒绝接受消息
            // 47003: 模板参数不准确
            // 41030: page路径不正确
            log.error("发送订阅消息失败 - appid:{}, openid:{}, templateId:{}, errCode:{}, errMsg:{}",
                appid, openid, templateId, e.getError().getErrorCode(), e.getError().getErrorMsg(), e);

            // 用户拒绝或其他错误不抛异常，返回false
            return false;
        } catch (Exception e) {
            log.error("发送订阅消息异常 - appid:{}, openid:{}, templateId:{}",
                appid, openid, templateId, e);
            return false;
        }
    }

    /**
     * 批量发送订阅消息
     * <p>
     * 向多个用户发送相同模板的订阅消息
     * </p>
     *
     * @param appid      小程序appid
     * @param openids    用户openid列表
     * @param templateId 模板ID
     * @param data       模板数据（key为字段名，value为字段值）
     * @return 成功发送的数量
     */
    public static int batchSend(String appid, List<String> openids, String templateId,
                                Map<String, String> data) {
        return batchSend(appid, openids, templateId, data, null);
    }

    /**
     * 批量发送订阅消息（带跳转页面）
     *
     * @param appid      小程序appid
     * @param openids    用户openid列表
     * @param templateId 模板ID
     * @param data       模板数据（key为字段名，value为字段值）
     * @param page       跳转页面路径
     * @return 成功发送的数量
     */
    public static int batchSend(String appid, List<String> openids, String templateId,
                                Map<String, String> data, String page) {
        if (openids == null || openids.isEmpty()) {
            log.warn("批量发送订阅消息 - openid列表为空");
            return 0;
        }

        int successCount = 0;
        for (String openid : openids) {
            try {
                boolean success = send(appid, openid, templateId, data, page);
                if (success) {
                    successCount++;
                }
            } catch (Exception e) {
                log.error("批量发送订阅消息失败 - openid:{}", openid, e);
            }
        }

        log.info("批量发送订阅消息完成 - 总数:{}, 成功:{}, 失败:{}",
            openids.size(), successCount, openids.size() - successCount);

        return successCount;
    }

    /**
     * 发送订阅消息（使用MsgData列表）
     * <p>
     * 适用于需要更灵活控制数据格式的场景
     * </p>
     *
     * @param appid       小程序appid
     * @param openid      用户openid
     * @param templateId  模板ID
     * @param msgDataList 消息数据列表
     * @return 是否发送成功
     */
    public static boolean sendWithMsgData(String appid, String openid, String templateId,
                                          List<WxMaSubscribeMessage.MsgData> msgDataList) {
        return sendWithMsgData(appid, openid, templateId, msgDataList, null);
    }

    /**
     * 发送订阅消息（使用MsgData列表，带跳转页面）
     *
     * @param appid       小程序appid
     * @param openid      用户openid
     * @param templateId  模板ID
     * @param msgDataList 消息数据列表
     * @param page        跳转页面路径
     * @return 是否发送成功
     */
    public static boolean sendWithMsgData(String appid, String openid, String templateId,
                                          List<WxMaSubscribeMessage.MsgData> msgDataList, String page) {
        if (StringUtils.isBlank(appid)) {
            throw ServiceException.of("小程序appid不能为空");
        }
        if (StringUtils.isBlank(openid)) {
            throw ServiceException.of("用户openid不能为空");
        }
        if (StringUtils.isBlank(templateId)) {
            throw ServiceException.of("模板ID不能为空");
        }
        if (msgDataList == null || msgDataList.isEmpty()) {
            throw ServiceException.of("模板数据不能为空");
        }

        try {
            WxMaService wxMaService = SpringUtils.getBean(WxMaService.class);
            wxMaService.switchover(appid);

            WxMaSubscribeMessage.WxMaSubscribeMessageBuilder builder = WxMaSubscribeMessage.builder()
                .toUser(openid)
                .templateId(templateId)
                .data(msgDataList);

            if (StringUtils.isNotBlank(page)) {
                builder.page(page);
            }

            // 根据环境自动选择版本
            String env = SpringUtils.getActiveProfile();
            String envVersion = "dev".equals(env) ? "developer" : "formal";
            builder.miniprogramState(envVersion);

            wxMaService.getMsgService().sendSubscribeMsg(builder.build());

            log.info("订阅消息发送成功 - openid:{}, templateId:{}", openid, templateId);
            return true;

        } catch (WxErrorException e) {
            log.error("发送订阅消息失败 - appid:{}, openid:{}, templateId:{}, errCode:{}, errMsg:{}",
                appid, openid, templateId, e.getError().getErrorCode(), e.getError().getErrorMsg(), e);
            return false;
        } catch (Exception e) {
            log.error("发送订阅消息异常 - appid:{}, openid:{}, templateId:{}",
                appid, openid, templateId, e);
            return false;
        }
    }
}
