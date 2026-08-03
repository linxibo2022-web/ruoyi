package plus.ruoyi.common.miniapp.initializer;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.dict.DictPlatformType;
import plus.ruoyi.common.core.domain.dto.PlatformDTO;
import plus.ruoyi.common.core.service.PlatformService;
import plus.ruoyi.common.core.utils.StreamUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 小程序配置初始化
 *
 * @author bkywksj
 */
@Slf4j
public class MiniappConfigInitializer implements ApplicationRunner {

    private final PlatformService platformService;
    private final WxMaService wxMaService;

    public MiniappConfigInitializer(PlatformService platformService, WxMaService wxMaService) {
        this.platformService = platformService;
        this.wxMaService = wxMaService;
    }

    /**
     * 启动应用后初始化公众号配置
     */
    @Override
    public void run(ApplicationArguments args) {
        init();
    }

    /**
     * 初始化小程序配置
     */
    public void init() {
        List<PlatformDTO> platformDTOList = platformService.listPlatformsByType(DictPlatformType.MP_WEIXIN.getValue(), null);
        if (CollUtil.isEmpty(platformDTOList)) {
            log.info("未找到微信小程序配置，跳过初始化");
            return;
        }

        wxMaService.setMultiConfigs(
            platformDTOList.stream()
                .map(this::buildWxMaConfig)
                .collect(Collectors.toMap(WxMaDefaultConfigImpl::getAppid,
                    wxMaDefaultConfig -> wxMaDefaultConfig,
                    // 合并函数，处理重复的appid，保留第一个
                    (first, second) -> {
                        log.warn("发现重复的appid配置: {}, 保留第一个配置", first.getAppid());
                        return first;
                    })));

        log.info("初始化微信小程序配置成功:{}", StreamUtils.join(platformDTOList,
            platformDTO -> StringUtils.format("{}({})", platformDTO.getName(), platformDTO.getAppid()), ", "));
    }

    /**
     * 添加单个配置
     *
     * @param platform 平台配置
     */
    public void addConfig(PlatformDTO platform) {
        if (platform == null || StringUtils.isBlank(platform.getAppid())) {
            log.warn("平台配置为空或appid为空，跳过添加");
            return;
        }

        // 检查是否为微信小程序配置
        if (!DictPlatformType.MP_WEIXIN.getValue().equals(platform.getType())) {
            log.debug("平台类型[{}]不是微信小程序，跳过添加", platform.getType());
            return;
        }

        try {
            WxMaDefaultConfigImpl config = buildWxMaConfig(platform);
            wxMaService.addConfig(platform.getAppid(), config);
            log.info("成功添加微信小程序配置: appid={}, name={}", platform.getAppid(), platform.getName());
        } catch (Exception e) {
            log.error("添加微信小程序配置失败: appid={}, name={}, error={}",
                platform.getAppid(), platform.getName(), e.getMessage(), e);
        }
    }

    /**
     * 移除单个配置
     *
     * @param appid 应用ID
     */
    public void removeConfig(String appid) {
        if (StringUtils.isBlank(appid)) {
            return;
        }

        try {
            wxMaService.removeConfig(appid);
            log.info("成功移除微信小程序配置: appid={}", appid);
        } catch (Exception e) {
            log.debug("移除微信小程序配置失败或配置不存在: appid={}", appid);
        }
    }

    /**
     * 构建微信小程序配置
     *
     * @param platform 平台配置DTO
     * @return 微信小程序配置
     */
    private WxMaDefaultConfigImpl buildWxMaConfig(PlatformDTO platform) {
        WxMaDefaultConfigImpl config = new WxMaDefaultConfigImpl();
        config.setAppid(platform.getAppid());
        config.setSecret(platform.getSecret());
        config.setToken(platform.getToken());
        config.setAesKey(platform.getAeskey());
        return config;
    }
}
