package plus.ruoyi.common.mp.config;

import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.redis.WxRedisOps;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import plus.ruoyi.common.core.service.PlatformService;
import plus.ruoyi.common.mp.channel.MpMessageChannel;
import plus.ruoyi.common.mp.initializer.MpConfigInitializer;

/**
 * 微信公众号自动配置
 * <p>
 * 负责注册微信公众号相关组件，包括：
 * - WxMpService：微信公众号核心服务
 * - WxRedisOps：公众号配置缓存实现
 * - MpConfigInitializer：配置初始化器
 * <p>
 * 只有当公众号模块启用时才加载此配置
 * 通过 application.yml 中的 module.mp-enabled 配置控制
 * </p>
 *
 * @author bkywksj
 */
@Slf4j
@AutoConfiguration
@ConditionalOnProperty(prefix = "module", name = "mp-enabled", havingValue = "true", matchIfMissing = true)
public class MpAutoConfiguration {

    /**
     * 注册微信公众号核心服务
     * <p>
     * 提供微信公众号API调用能力，支持：
     * - 用户管理
     * - 菜单管理
     * - 消息推送
     * - 素材管理等功能
     * </p>
     */
    @Bean
    public WxMpService wxMpService() {
        WxMpServiceImpl wxMpService = new WxMpServiceImpl();
        // 设置最大重试次数，提高接口调用的容错性
        wxMpService.setMaxRetryTimes(3);
        return wxMpService;
    }

    /**
     * 注册微信公众号配置缓存实现
     * <p>
     * 使用 Redis + Caffeine 二级缓存，提高配置访问性能
     * - 一级缓存（Caffeine）：本地内存缓存，5秒过期
     * - 二级缓存（Redis）：分布式缓存，支持多实例共享
     * </p>
     */
    @Bean
    public WxRedisOps wxRedisOps() {
        return new PlusWxRedisOps();
    }

    /**
     * 注册微信公众号配置初始化器
     * <p>
     * 应用启动时自动从数据库加载微信公众号配置
     * 支持多公众号配置的动态管理
     * </p>
     *
     * @param platformService 平台配置服务
     * @param wxMpService     微信公众号服务
     * @param wxRedisOps      微信配置缓存操作
     */
    @Bean
    public MpConfigInitializer mpConfigInitializer(PlatformService platformService,
                                                    WxMpService wxMpService,
                                                    WxRedisOps wxRedisOps) {
        return new MpConfigInitializer(platformService, wxMpService, wxRedisOps);
    }

    /**
     * 注册微信公众号模板消息通道
     * <p>
     * 实现统一消息接口，支持通过 MessagePushService 发送公众号模板消息
     * </p>
     */
    @Bean
    public MpMessageChannel mpMessageChannel() {
        return new MpMessageChannel();
    }
}
