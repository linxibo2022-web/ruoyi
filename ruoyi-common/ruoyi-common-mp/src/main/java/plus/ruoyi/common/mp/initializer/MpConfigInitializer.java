package plus.ruoyi.common.mp.initializer;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.redis.WxRedisOps;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import me.chanjar.weixin.mp.config.impl.WxMpRedisConfigImpl;
import plus.ruoyi.common.core.dict.DictPlatformType;
import plus.ruoyi.common.core.domain.dto.PlatformDTO;
import plus.ruoyi.common.core.service.PlatformService;
import plus.ruoyi.common.core.utils.StreamUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.util.List;

/**
 * 公众号配置初始化
 * @author bkywksj
 */
@Slf4j
public class MpConfigInitializer implements ApplicationRunner {

    private final PlatformService platformService;
    private final WxMpService wxMpService;
    private final WxRedisOps wxRedisOps;

    /**
     * 构造函数
     *
     * @param platformService 平台配置服务
     * @param wxMpService     微信公众号服务
     * @param wxRedisOps      微信配置缓存操作
     */
    public MpConfigInitializer(PlatformService platformService,
                               WxMpService wxMpService,
                               WxRedisOps wxRedisOps) {
        this.platformService = platformService;
        this.wxMpService = wxMpService;
        this.wxRedisOps = wxRedisOps;
    }

    /**
     * 应用启动后初始化公众号配置
     */
    @Override
    public void run(ApplicationArguments args) {
        init();
    }

    /**
     * 初始化微信公众号配置
     * <p>
     * 从数据库加载所有微信公众号平台配置并注册到 WxMpService
     * </p>
     */
    public void init() {
        List<PlatformDTO> platformDTOList = platformService.listPlatformsByType(
            DictPlatformType.MP_OFFICIAL_ACCOUNT.getValue(), null);

        if (CollUtil.isEmpty(platformDTOList)) {
            log.info("未找到微信公众号配置，跳过初始化");
            return;
        }

        for (PlatformDTO platformDTO : platformDTOList) {
            String appid = platformDTO.getAppid();
            WxMpDefaultConfigImpl config = buildWxMpConfigImpl(platformDTO);
            wxMpService.addConfigStorage(appid, config);
        }

        log.info("初始化微信公众号配置成功: {}", StreamUtils.join(platformDTOList,
            platformDTO -> StringUtils.format("{}({})", platformDTO.getName(), platformDTO.getAppid()), ", "));
    }

    /**
     * 动态添加单个公众号配置
     *
     * @param platform 平台配置
     */
    public void addConfig(PlatformDTO platform) {
        if (platform == null || StringUtils.isBlank(platform.getAppid())) {
            log.warn("平台配置为空或appid为空，跳过添加");
            return;
        }

        // 检查是否为微信公众号配置
        if (!DictPlatformType.MP_OFFICIAL_ACCOUNT.getValue().equals(platform.getType())) {
            log.debug("平台类型[{}]不是微信公众号，跳过添加", platform.getType());
            return;
        }

        try {
            WxMpDefaultConfigImpl config = buildWxMpConfigImpl(platform);
            wxMpService.addConfigStorage(platform.getAppid(), config);
            log.info("成功添加微信公众号配置: appid={}, name={}", platform.getAppid(), platform.getName());
        } catch (Exception e) {
            log.error("添加微信公众号配置失败: appid={}, name={}",
                platform.getAppid(), platform.getName(), e);
        }
    }

    /**
     * 动态移除单个公众号配置
     *
     * @param appid 应用ID
     */
    public void removeConfig(String appid) {
        if (StringUtils.isBlank(appid)) {
            return;
        }

        try {
            wxMpService.removeConfigStorage(appid);
            log.info("成功移除微信公众号配置: appid={}", appid);
        } catch (Exception e) {
            log.debug("移除微信公众号配置失败或配置不存在: appid={}", appid);
        }
    }

    /**
     * 构建微信公众号配置对象
     * <p>
     * 使用 Redis 缓存配置信息，提高多实例部署时的配置共享能力
     * </p>
     *
     * @param platformDTO 平台配置DTO
     * @return 微信公众号配置
     */
    private WxMpDefaultConfigImpl buildWxMpConfigImpl(PlatformDTO platformDTO) {
        WxMpRedisConfigImpl wxMpRedisConfig = new WxMpRedisConfigImpl(wxRedisOps, "wx:mp");
        wxMpRedisConfig.setAppId(platformDTO.getAppid());
        wxMpRedisConfig.setSecret(platformDTO.getSecret());
        wxMpRedisConfig.setToken(platformDTO.getToken());
        wxMpRedisConfig.setAesKey(platformDTO.getAeskey());
        return wxMpRedisConfig;
    }
}
