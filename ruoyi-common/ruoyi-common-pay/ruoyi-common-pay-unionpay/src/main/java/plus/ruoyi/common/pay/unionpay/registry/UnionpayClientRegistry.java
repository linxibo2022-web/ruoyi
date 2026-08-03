package plus.ruoyi.common.pay.unionpay.registry;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 银联客户端注册表
 *
 * 用于管理不同appid对应的配置信息
 * (银联开放平台使用HTTP API，不需要像微信/支付宝那样维护SDK客户端实例)
 *
 * @author 抓蛙师
 */
@Slf4j
public class UnionpayClientRegistry {

    /**
     * 配置缓存
     * Key: appid
     * Value: 配置信息（预留，当前版本暂未使用）
     */
    private final Map<String, Object> configCache = new ConcurrentHashMap<>();

    /**
     * 注册配置
     *
     * @param appid  应用ID
     * @param config 配置信息
     */
    public void register(String appid, Object config) {
        configCache.put(appid, config);
        log.info("银联配置已注册: appid={}", appid);
    }

    /**
     * 注销配置
     *
     * @param appid 应用ID
     */
    public void unregister(String appid) {
        configCache.remove(appid);
        log.info("银联配置已注销: appid={}", appid);
    }

    /**
     * 获取配置
     *
     * @param appid 应用ID
     * @return 配置信息
     */
    public Object getConfig(String appid) {
        return configCache.get(appid);
    }

    /**
     * 获取已注册实例数量
     *
     * @return 数量
     */
    public int size() {
        return configCache.size();
    }

    /**
     * 清空所有配置
     */
    public void clear() {
        configCache.clear();
        log.info("银联配置已清空");
    }
}
