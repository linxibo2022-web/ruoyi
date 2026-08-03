package plus.ruoyi.common.pay.wechat.registry;

import com.github.binarywang.wxpay.service.WxPayService;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.pay.core.registry.PayServiceRegistry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 微信支付服务注册中心
 *
 * 管理所有 WxPayService 实例
 * 支持多租户、多商户场景
 *
 * 注意: 此类不需要 @Component 注解,已在 PayAutoConfiguration 中通过 @Bean 注册
 *
 * @author 抓蛙师
 */
@Slf4j
public class WxPayServiceRegistry implements PayServiceRegistry<WxPayService> {

    /**
     * 服务实例缓存
     * Key: appid
     * Value: WxPayService实例
     */
    private final Map<String, WxPayService> serviceMap = new ConcurrentHashMap<>();

    @Override
    public void register(String appid, WxPayService service) {
        if (service == null) {
            log.warn("注册的WxPayService为空,忽略: appid={}", appid);
            return;
        }

        serviceMap.put(appid, service);
        log.debug("WxPayService注册成功: appid={}", appid);
    }

    @Override
    public WxPayService getService(String appid) {
        WxPayService service = serviceMap.get(appid);
        if (service == null) {
            log.warn("未找到WxPayService实例: appid={}", appid);
        }
        return service;
    }

    @Override
    public void remove(String appid) {
        serviceMap.remove(appid);
        log.debug("WxPayService已移除: appid={}", appid);
    }

    @Override
    public boolean contains(String appid) {
        return serviceMap.containsKey(appid);
    }

    @Override
    public void clear() {
        int size = serviceMap.size();
        serviceMap.clear();
        log.info("WxPayService注册中心已清空,共移除{}个服务实例", size);
    }

    /**
     * 获取已注册的服务数量
     */
    public int size() {
        return serviceMap.size();
    }
}
