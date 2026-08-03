package plus.ruoyi.common.pay.alipay.registry;

import com.alipay.api.AlipayClient;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.pay.core.registry.PayServiceRegistry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AlipayClient 注册器
 *
 * 管理多个appid对应的AlipayClient实例
 *
 * @author 抓蛙师
 */
@Slf4j
public class AlipayClientRegistry implements PayServiceRegistry<AlipayClient> {

    /**
     * AlipayClient 缓存
     * Key: appid
     * Value: AlipayClient实例
     */
    private final Map<String, AlipayClient> clientMap = new ConcurrentHashMap<>();

    @Override
    public void register(String appid, AlipayClient client) {
        if (client == null) {
            log.warn("尝试注册空的AlipayClient: appid={}", appid);
            return;
        }

        AlipayClient existing = clientMap.put(appid, client);
        if (existing != null) {
            log.debug("替换已存在的AlipayClient: appid={}", appid);
        } else {
            log.debug("注册AlipayClient成功: appid={}", appid);
        }
    }

    @Override
    public AlipayClient getService(String appid) {
        return clientMap.get(appid);
    }

    /**
     * 获取 AlipayClient (别名方法)
     */
    public AlipayClient getClient(String appid) {
        return getService(appid);
    }

    @Override
    public void remove(String appid) {
        AlipayClient removed = clientMap.remove(appid);
        if (removed != null) {
            log.debug("移除AlipayClient: appid={}", appid);
        }
    }

    /**
     * 移除客户端 (别名方法,兼容性)
     */
    public void unregister(String appid) {
        remove(appid);
    }

    @Override
    public void clear() {
        int size = clientMap.size();
        clientMap.clear();
        log.debug("清空AlipayClient注册器,共移除{}个实例", size);
    }

    @Override
    public boolean contains(String appid) {
        return clientMap.containsKey(appid);
    }

    /**
     * 获取注册客户端数量
     */
    public int size() {
        return clientMap.size();
    }
}
