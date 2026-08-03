package plus.ruoyi.common.miniapp.config;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.bean.WxMaKefuMessage;
import cn.binarywang.wx.miniapp.bean.WxMaSubscribeMessage;
import cn.binarywang.wx.miniapp.message.WxMaMessageHandler;
import cn.binarywang.wx.miniapp.message.WxMaMessageRouter;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.result.WxMediaUploadResult;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import plus.ruoyi.common.core.service.PlatformService;
import plus.ruoyi.common.miniapp.channel.MiniappMessageChannel;
import plus.ruoyi.common.miniapp.initializer.MiniappConfigInitializer;

import java.io.File;

/**
 * 微信小程序自动配置
 * <p>
 * 负责注册微信小程序相关组件，包括：
 * - WxMaService：微信小程序核心服务
 * - WxMaMessageRouter：消息路由器
 * - MiniappConfigInitializer：配置初始化启动器
 * <p>
 * 只有当小程序模块启用时才加载此配置
 * 通过 application.yml 中的 module.miniapp-enabled 配置控制
 * </p>
 *
 * @author bkywksj
 */
@Slf4j
@AutoConfiguration
@ConditionalOnProperty(prefix = "module", name = "miniapp-enabled", havingValue = "true", matchIfMissing = true)
public class MiniappAutoConfiguration {

    /**
     * 注册微信小程序核心服务
     * <p>
     * 提供微信小程序API调用能力，支持：
     * - 用户管理
     * - 消息推送
     * - 二维码生成
     * - 素材管理等功能
     * </p>
     */
    @Bean
    public WxMaService wxMaService() {
        WxMaServiceImpl wxMaService = new WxMaServiceImpl();
        // 设置最大重试次数，提高接口调用的容错性
        wxMaService.setMaxRetryTimes(3);
        return wxMaService;
    }

    /**
     * 注册消息路由器
     * <p>
     * 用于处理微信小程序推送的各类消息和事件
     * 支持文本、图片、二维码、订阅消息等多种消息类型
     * </p>
     *
     * @param wxMaService 微信小程序服务
     */
    @Bean
    public WxMaMessageRouter wxMaMessageRouter(WxMaService wxMaService) {
        final WxMaMessageRouter router = new WxMaMessageRouter(wxMaService);
        router
            // 所有消息都先记录日志
            .rule().handler(logHandler).next()
            // 订阅消息处理
            .rule().async(false).content("订阅消息").handler(subscribeMsgHandler).end()
            // 文本消息处理
            .rule().async(false).content("文本").handler(textHandler).end()
            // 图片消息处理
            .rule().async(false).content("图片").handler(picHandler).end()
            // 二维码消息处理
            .rule().async(false).content("二维码").handler(qrcodeHandler).end();
        return router;
    }

    /**
     * 注册微信小程序配置初始化器
     * <p>
     * 应用启动时自动从数据库加载微信小程序配置
     * 支持多小程序配置的动态管理
     * </p>
     *
     * @param platformService 平台配置服务
     * @param wxMaService     微信小程序服务
     */
    @Bean
    public MiniappConfigInitializer wxMaApplicationRunner(PlatformService platformService,
                                                          WxMaService wxMaService) {
        return new MiniappConfigInitializer(platformService, wxMaService);
    }

    /**
     * 注册微信小程序订阅消息通道
     * <p>
     * 实现统一消息接口，支持通过 MessagePushService 发送小程序订阅消息
     * </p>
     */
    @Bean
    public MiniappMessageChannel miniappMessageChannel() {
        return new MiniappMessageChannel();
    }

    // ==================== 消息处理器 ====================

    /**
     * 订阅消息处理器
     */
    private final WxMaMessageHandler subscribeMsgHandler = (wxMessage, context, service, sessionManager) -> {
        service.getMsgService().sendSubscribeMsg(WxMaSubscribeMessage.builder()
            .templateId("此处更换为自己的模板id")
            .data(Lists.newArrayList(
                new WxMaSubscribeMessage.MsgData("keyword1", "339208499")))
            .toUser(wxMessage.getFromUser())
            .build());
        return null;
    };

    /**
     * 日志记录处理器
     * 记录所有接收到的消息，并发送确认回复
     */
    private final WxMaMessageHandler logHandler = (wxMessage, context, service, sessionManager) -> {
        log.info("收到消息：{}", wxMessage.toString());
        service.getMsgService().sendKefuMsg(WxMaKefuMessage.newTextBuilder()
            .content("收到信息为：" + wxMessage.toJson())
            .toUser(wxMessage.getFromUser()).build());
        return null;
    };

    /**
     * 文本消息处理器
     */
    private final WxMaMessageHandler textHandler = (wxMessage, context, service, sessionManager) -> {
        service.getMsgService().sendKefuMsg(WxMaKefuMessage.newTextBuilder()
            .content("回复文本消息")
            .toUser(wxMessage.getFromUser()).build());
        return null;
    };

    /**
     * 图片消息处理器
     */
    private final WxMaMessageHandler picHandler = (wxMessage, context, service, sessionManager) -> {
        try {
            WxMediaUploadResult uploadResult = service.getMediaService()
                .uploadMedia("image", "png",
                    ClassLoader.getSystemResourceAsStream("tmp.png"));
            service.getMsgService().sendKefuMsg(
                WxMaKefuMessage.newImageBuilder()
                    .mediaId(uploadResult.getMediaId())
                    .toUser(wxMessage.getFromUser())
                    .build());
        } catch (WxErrorException e) {
            log.error("发送图片消息失败", e);
        }
        return null;
    };

    /**
     * 二维码消息处理器
     */
    private final WxMaMessageHandler qrcodeHandler = (wxMessage, context, service, sessionManager) -> {
        try {
            final File file = service.getQrcodeService().createQrcode("123", 430);
            WxMediaUploadResult uploadResult = service.getMediaService().uploadMedia("image", file);
            service.getMsgService().sendKefuMsg(
                WxMaKefuMessage.newImageBuilder()
                    .mediaId(uploadResult.getMediaId())
                    .toUser(wxMessage.getFromUser())
                    .build());
        } catch (WxErrorException e) {
            log.error("发送二维码消息失败", e);
        }
        return null;
    };
}
