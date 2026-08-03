package plus.ruoyi.business.base.listener;

import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.dict.DictPlatformType;
import plus.ruoyi.common.core.domain.dto.PlatformConfigChangeDTO;
import plus.ruoyi.common.core.domain.dto.PlatformDTO;
import plus.ruoyi.common.core.service.PlatformService;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.miniapp.initializer.MiniappConfigInitializer;
import plus.ruoyi.common.mp.initializer.MpConfigInitializer;
import plus.ruoyi.common.pay.initializer.PayConfigInitializer;
import plus.ruoyi.common.redis.utils.RedisUtils;
import plus.ruoyi.common.tenant.helper.TenantHelper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 平台配置集群同步监听器
 * <p>
 * 通过 Redis 发布订阅机制，在集群部署时同步平台配置变更到所有节点。
 * 当某个节点修改了平台配置后，会通过 Redis 广播通知其他节点更新内存中的配置。
 *
 * @author 抓蛙师
 */
@Slf4j
@Component
public class PlatformConfigSyncListener implements ApplicationRunner {

    /**
     * Redis 发布订阅频道名称
     */
    public static final String PLATFORM_CONFIG_TOPIC = "global:platform:config";

    /**
     * 当前节点唯一标识（用于防止自身重复处理）
     */
    public static final String NODE_ID = UUID.randomUUID().toString();

    private final PlatformService platformService;

    public PlatformConfigSyncListener(PlatformService platformService) {
        this.platformService = platformService;
    }

    @Override
    public void run(ApplicationArguments args) {
        RedisUtils.subscribe(PLATFORM_CONFIG_TOPIC, PlatformConfigChangeDTO.class, this::handleMessage);
        log.info("平台配置集群同步监听器已启动, nodeId={}", NODE_ID);
    }

    /**
     * 处理收到的配置变更消息
     */
    private void handleMessage(PlatformConfigChangeDTO message) {
        // 跳过本节点发出的消息（本节点已在发布前处理过）
        if (NODE_ID.equals(message.getSourceNodeId())) {
            return;
        }

        String action = message.getAction();
        String appid = message.getAppid();
        String tenantId = message.getTenantId();

        log.info("收到平台配置变更通知: action={}, appid={}, tenantId={}, sourceNode={}",
            action, appid, tenantId, message.getSourceNodeId());

        try {
            switch (action) {
                case PlatformConfigChangeDTO.ACTION_ADD -> handleAddConfig(appid, tenantId);
                case PlatformConfigChangeDTO.ACTION_REMOVE -> handleRemoveConfig(appid, tenantId);
                case PlatformConfigChangeDTO.ACTION_REFRESH_PAY -> handleRefreshPay(tenantId);
                default -> log.warn("未知的配置变更操作: {}", action);
            }
        } catch (Exception e) {
            log.error("处理平台配置变更失败: action={}, appid={}, error={}", action, appid, e.getMessage(), e);
        }
    }

    /**
     * 处理添加配置
     */
    private void handleAddConfig(String appid, String tenantId) {
        if (StringUtils.isBlank(appid)) {
            return;
        }

        PlatformDTO platform = TenantHelper.dynamic(tenantId, () ->
            platformService.getPlatformByAppid(appid, tenantId));

        if (platform == null) {
            log.warn("集群同步: 未找到appid={}的平台配置", appid);
            return;
        }

        addConfigByType(platform);
    }

    /**
     * 处理移除配置
     */
    private void handleRemoveConfig(String appid, String tenantId) {
        if (StringUtils.isBlank(appid)) {
            return;
        }

        // 移除时可能配置已从数据库删除，需要先查询再按类型移除
        // 如果查不到，则尝试同时从小程序和公众号中移除
        PlatformDTO platform = TenantHelper.dynamic(tenantId, () ->
            platformService.getPlatformByAppid(appid, tenantId));

        if (platform != null) {
            removeConfigByType(platform.getType(), appid);
        } else {
            // 配置已删除，尝试从所有类型中移除
            removeFromAll(appid);
        }
    }

    /**
     * 处理刷新支付配置
     */
    private void handleRefreshPay(String tenantId) {
        if (StringUtils.isBlank(tenantId)) {
            return;
        }

        TenantHelper.dynamic(tenantId, () -> {
            SpringUtils.getBean(PayConfigInitializer.class).initByTenant(tenantId);
            log.info("集群同步: 重新初始化租户[{}]的支付配置", tenantId);
            return null;
        });
    }

    /**
     * 根据平台类型添加配置
     */
    private void addConfigByType(PlatformDTO platform) {
        String type = platform.getType();
        if (DictPlatformType.MP_WEIXIN.getValue().equals(type)) {
            SpringUtils.getBean(MiniappConfigInitializer.class).addConfig(platform);
            log.info("集群同步: 添加小程序配置 appid={}", platform.getAppid());
        } else if (DictPlatformType.MP_OFFICIAL_ACCOUNT.getValue().equals(type)) {
            SpringUtils.getBean(MpConfigInitializer.class).addConfig(platform);
            log.info("集群同步: 添加公众号配置 appid={}", platform.getAppid());
        }
    }

    /**
     * 根据平台类型移除配置
     */
    private void removeConfigByType(String type, String appid) {
        if (DictPlatformType.MP_WEIXIN.getValue().equals(type)) {
            SpringUtils.getBean(MiniappConfigInitializer.class).removeConfig(appid);
            log.info("集群同步: 移除小程序配置 appid={}", appid);
        } else if (DictPlatformType.MP_OFFICIAL_ACCOUNT.getValue().equals(type)) {
            SpringUtils.getBean(MpConfigInitializer.class).removeConfig(appid);
            log.info("集群同步: 移除公众号配置 appid={}", appid);
        }
    }

    /**
     * 从所有配置类型中移除（删除场景，无法判断类型时使用）
     */
    private void removeFromAll(String appid) {
        SpringUtils.getBean(MiniappConfigInitializer.class).removeConfig(appid);
        SpringUtils.getBean(MpConfigInitializer.class).removeConfig(appid);
        log.info("集群同步: 从所有配置中移除 appid={}", appid);
    }

    // ============== 静态发布方法（供 Controller 调用） ==============

    /**
     * 发布配置添加消息
     */
    public static void publishAdd(String appid, String tenantId) {
        publish(PlatformConfigChangeDTO.ACTION_ADD, appid, tenantId);
    }

    /**
     * 发布配置移除消息
     */
    public static void publishRemove(String appid, String tenantId) {
        publish(PlatformConfigChangeDTO.ACTION_REMOVE, appid, tenantId);
    }

    /**
     * 发布支付配置刷新消息
     */
    public static void publishRefreshPay(String tenantId) {
        publish(PlatformConfigChangeDTO.ACTION_REFRESH_PAY, null, tenantId);
    }

    /**
     * 发布配置变更消息到 Redis
     */
    private static void publish(String action, String appid, String tenantId) {
        PlatformConfigChangeDTO message = new PlatformConfigChangeDTO();
        message.setAction(action);
        message.setAppid(appid);
        message.setTenantId(tenantId);
        message.setSourceNodeId(NODE_ID);

        RedisUtils.publish(PLATFORM_CONFIG_TOPIC, message);
        log.debug("发布平台配置变更消息: action={}, appid={}, tenantId={}", action, appid, tenantId);
    }
}
