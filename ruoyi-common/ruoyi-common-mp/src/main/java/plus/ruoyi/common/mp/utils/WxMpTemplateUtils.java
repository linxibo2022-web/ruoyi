package plus.ruoyi.common.mp.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.core.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 微信公众号模板消息工具类
 * <p>
 * 提供公众号模板消息发送的基础能力，仅负责调用微信API发送消息
 * 业务逻辑（如模板匹配、数据构建等）应在业务层实现
 * </p>
 *
 * <p>使用示例：</p>
 * <pre>
 * // 基础发送
 * Map&lt;String, String&gt; data = new HashMap&lt;&gt;();
 * data.put("first", "您的订单已发货");
 * data.put("keyword1", "订单编号123456");
 * data.put("keyword2", "2025-01-01 10:00:00");
 * data.put("remark", "感谢您的购买");
 * WxMpTemplateUtils.send("appid", "openid", "templateId", data);
 *
 * // 带跳转H5链接
 * WxMpTemplateUtils.send("appid", "openid", "templateId", data, "https://example.com/order/123");
 *
 * // 跳转小程序
 * WxMpTemplateUtils.sendToMiniProgram("appid", "openid", "templateId", data, "miniAppid", "pages/index/index");
 *
 * // 自定义统一颜色
 * WxMpTemplateUtils.sendWithCustomColor("appid", "openid", "templateId", data, url, "#FF0000");
 *
 * // 每个字段单独设置颜色
 * Map&lt;String, TemplateData&gt; colorData = new HashMap&lt;&gt;();
 * colorData.put("first", new TemplateData("您的订单已发货", "#173177"));
 * colorData.put("keyword1", new TemplateData("订单123456", "#FF0000"));
 * WxMpTemplateUtils.sendWithColor("appid", "openid", "templateId", colorData);
 *
 * // 批量发送
 * List&lt;String&gt; openids = Arrays.asList("openid1", "openid2", "openid3");
 * int successCount = WxMpTemplateUtils.batchSend("appid", openids, "templateId", data);
 * </pre>
 *
 * @author bkywksj
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WxMpTemplateUtils {

    /**
     * 默认字体颜色（黑色）
     */
    private static final String DEFAULT_COLOR = "#173177";

    /**
     * 发送模板消息
     *
     * @param appid      公众号appid
     * @param openid     用户openid
     * @param templateId 模板ID
     * @param data       模板数据（key为字段名，value为字段值）
     * @return 消息ID（发送成功返回msgid，失败返回null）
     */
    public static String send(String appid, String openid, String templateId, Map<String, String> data) {
        return send(appid, openid, templateId, data, null);
    }

    /**
     * 发送模板消息（带跳转链接）
     *
     * @param appid      公众号appid
     * @param openid     用户openid
     * @param templateId 模板ID
     * @param data       模板数据（key为字段名，value为字段值）
     * @param url        跳转链接（如：https://example.com/order/123）
     * @return 消息ID
     */
    public static String send(String appid, String openid, String templateId,
                              Map<String, String> data, String url) {
        // 转换为带颜色的数据（使用默认颜色）
        List<WxMpTemplateData> templateDataList = new ArrayList<>();
        data.forEach((key, value) ->
            templateDataList.add(new WxMpTemplateData(key, value, DEFAULT_COLOR))
        );

        return sendInternal(appid, openid, templateId, templateDataList, url, null, null);
    }

    /**
     * 发送模板消息（带跳转小程序）
     *
     * @param appid            公众号appid
     * @param openid           用户openid
     * @param templateId       模板ID
     * @param data             模板数据（key为字段名，value为字段值）
     * @param miniProgramAppid 小程序appid
     * @param miniProgramPath  小程序页面路径
     * @return 消息ID
     */
    public static String sendToMiniProgram(String appid, String openid, String templateId,
                                           Map<String, String> data, String miniProgramAppid, String miniProgramPath) {
        // 转换为带颜色的数据（使用默认颜色）
        List<WxMpTemplateData> templateDataList = new ArrayList<>();
        data.forEach((key, value) ->
            templateDataList.add(new WxMpTemplateData(key, value, DEFAULT_COLOR))
        );

        return sendInternal(appid, openid, templateId, templateDataList, null, miniProgramAppid, miniProgramPath);
    }

    /**
     * 发送模板消息（带跳转链接和自定义颜色）
     *
     * @param appid      公众号appid
     * @param openid     用户openid
     * @param templateId 模板ID
     * @param data       模板数据（key为字段名，value为字段值）
     * @param url        跳转链接
     * @param color      字体颜色（如：#173177，不填默认为黑色）
     * @return 消息ID
     */
    public static String sendWithCustomColor(String appid, String openid, String templateId,
                                             Map<String, String> data, String url, String color) {
        // 转换为带颜色的数据
        String finalColor = StringUtils.isNotBlank(color) ? color : DEFAULT_COLOR;
        List<WxMpTemplateData> templateDataList = new ArrayList<>();
        data.forEach((key, value) ->
            templateDataList.add(new WxMpTemplateData(key, value, finalColor))
        );

        return sendInternal(appid, openid, templateId, templateDataList, url, null, null);
    }

    /**
     * 发送模板消息（支持每个字段单独设置颜色）
     *
     * @param appid      公众号appid
     * @param openid     用户openid
     * @param templateId 模板ID
     * @param data       模板数据（TemplateData包含value和color）
     * @return 消息ID
     */
    public static String sendWithColor(String appid, String openid, String templateId,
                                       Map<String, TemplateData> data) {
        return sendWithColor(appid, openid, templateId, data, null);
    }

    /**
     * 发送模板消息（支持每个字段单独设置颜色，带跳转链接）
     *
     * @param appid      公众号appid
     * @param openid     用户openid
     * @param templateId 模板ID
     * @param data       模板数据（TemplateData包含value和color）
     * @param url        跳转链接
     * @return 消息ID
     */
    public static String sendWithColor(String appid, String openid, String templateId,
                                       Map<String, TemplateData> data, String url) {
        // 转换为WxMpTemplateData
        List<WxMpTemplateData> templateDataList = new ArrayList<>();
        data.forEach((key, templateData) ->
            templateDataList.add(new WxMpTemplateData(
                key,
                templateData.getValue(),
                StringUtils.isNotBlank(templateData.getColor()) ? templateData.getColor() : DEFAULT_COLOR
            ))
        );

        return sendInternal(appid, openid, templateId, templateDataList, url, null, null);
    }

    /**
     * 批量发送模板消息
     * <p>
     * 向多个用户发送相同模板的消息
     * </p>
     *
     * @param appid      公众号appid
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
     * 批量发送模板消息（带跳转链接）
     *
     * @param appid      公众号appid
     * @param openids    用户openid列表
     * @param templateId 模板ID
     * @param data       模板数据（key为字段名，value为字段值）
     * @param url        跳转链接
     * @return 成功发送的数量
     */
    public static int batchSend(String appid, List<String> openids, String templateId,
                                Map<String, String> data, String url) {
        if (openids == null || openids.isEmpty()) {
            log.warn("批量发送模板消息 - openid列表为空");
            return 0;
        }

        int successCount = 0;
        for (String openid : openids) {
            try {
                String msgId = send(appid, openid, templateId, data, url);
                if (StringUtils.isNotBlank(msgId)) {
                    successCount++;
                }
            } catch (Exception e) {
                log.error("批量发送模板消息失败 - openid:{}", openid, e);
            }
        }

        log.info("批量发送模板消息完成 - 总数:{}, 成功:{}, 失败:{}",
            openids.size(), successCount, openids.size() - successCount);

        return successCount;
    }

    /**
     * 发送模板消息（使用WxMpTemplateData列表）
     * <p>
     * 适用于需要更灵活控制数据格式和颜色的场景
     * </p>
     *
     * @param appid            公众号appid
     * @param openid           用户openid
     * @param templateId       模板ID
     * @param templateDataList 模板数据列表
     * @return 消息ID
     */
    public static String sendWithTemplateData(String appid, String openid, String templateId,
                                              List<WxMpTemplateData> templateDataList) {
        return sendInternal(appid, openid, templateId, templateDataList, null, null, null);
    }

    /**
     * 发送模板消息（使用WxMpTemplateData列表，带跳转链接）
     *
     * @param appid            公众号appid
     * @param openid           用户openid
     * @param templateId       模板ID
     * @param templateDataList 模板数据列表
     * @param url              跳转链接
     * @return 消息ID
     */
    public static String sendWithTemplateData(String appid, String openid, String templateId,
                                              List<WxMpTemplateData> templateDataList, String url) {
        return sendInternal(appid, openid, templateId, templateDataList, url, null, null);
    }

    /**
     * 内部发送方法（统一处理）
     *
     * @param appid            公众号appid
     * @param openid           用户openid
     * @param templateId       模板ID
     * @param templateDataList 模板数据列表
     * @param url              跳转链接
     * @param miniAppid        小程序appid
     * @param miniPath         小程序页面路径
     * @return 消息ID
     */
    private static String sendInternal(String appid, String openid, String templateId,
                                       List<WxMpTemplateData> templateDataList,
                                       String url, String miniAppid, String miniPath) {
        // 参数校验
        if (StringUtils.isBlank(appid)) {
            throw ServiceException.of("公众号appid不能为空");
        }
        if (StringUtils.isBlank(openid)) {
            throw ServiceException.of("用户openid不能为空");
        }
        if (StringUtils.isBlank(templateId)) {
            throw ServiceException.of("模板ID不能为空");
        }
        if (templateDataList == null || templateDataList.isEmpty()) {
            throw ServiceException.of("模板数据不能为空");
        }

        try {
            WxMpService wxMpService = SpringUtils.getBean(WxMpService.class);
            // 切换到指定的公众号配置
            wxMpService.switchover(appid);

            // 构建模板消息
            WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                .toUser(openid)
                .templateId(templateId)
                .data(templateDataList)
                .build();

            // 设置跳转链接
            if (StringUtils.isNotBlank(url)) {
                templateMessage.setUrl(url);
            }

            // 设置跳转小程序
            if (StringUtils.isNotBlank(miniAppid) && StringUtils.isNotBlank(miniPath)) {
                WxMpTemplateMessage.MiniProgram miniProgram = new WxMpTemplateMessage.MiniProgram();
                miniProgram.setAppid(miniAppid);
                miniProgram.setPagePath(miniPath);
                templateMessage.setMiniProgram(miniProgram);
            }

            log.debug("发送模板消息 - appid:{}, openid:{}, templateId:{}, url:{}, data:{}",
                appid, openid, templateId, url, templateDataList);

            // 发送模板消息
            String msgId = wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);

            log.info("模板消息发送成功 - openid:{}, templateId:{}, msgId:{}", openid, templateId, msgId);
            return msgId;

        } catch (WxErrorException e) {
            // 常见错误码说明：
            // 40001: access_token无效或过期
            // 43004: 用户拒收消息
            // 47003: 模板参数不准确
            // 41028: form_id已被使用或已过期
            log.error("发送模板消息失败 - appid:{}, openid:{}, templateId:{}, errCode:{}, errMsg:{}",
                appid, openid, templateId, e.getError().getErrorCode(), e.getError().getErrorMsg(), e);

            // 用户拒收或其他错误不抛异常，返回null
            return null;
        } catch (Exception e) {
            log.error("发送模板消息异常 - appid:{}, openid:{}, templateId:{}",
                appid, openid, templateId, e);
            return null;
        }
    }

    /**
     * 模板数据（包含值和颜色）
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class TemplateData {
        /**
         * 字段值
         */
        private String value;

        /**
         * 字体颜色（如：#173177）
         */
        private String color;

        /**
         * 构造方法（使用默认颜色）
         *
         * @param value 字段值
         */
        public TemplateData(String value) {
            this.value = value;
            this.color = DEFAULT_COLOR;
        }
    }
}
