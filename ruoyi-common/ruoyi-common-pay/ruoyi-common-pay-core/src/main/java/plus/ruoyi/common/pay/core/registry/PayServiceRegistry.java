package plus.ruoyi.common.pay.core.registry;

/**
 * 支付服务注册中心接口
 *
 * 用于管理各支付渠道的服务实例
 * 支持多租户、多商户场景
 *
 * @param <T> 服务类型
 * @author 抓蛙师
 */
public interface PayServiceRegistry<T> {

    /**
     * 注册服务实例
     *
     * @param appid   应用ID(唯一标识)
     * @param service 服务实例
     */
    void register(String appid, T service);

    /**
     * 获取服务实例
     *
     * @param appid 应用ID
     * @return 服务实例,如果不存在返回null
     */
    T getService(String appid);

    /**
     * 移除服务实例
     *
     * @param appid 应用ID
     */
    void remove(String appid);

    /**
     * 检查服务是否存在
     *
     * @param appid 应用ID
     * @return 是否存在
     */
    boolean contains(String appid);

    /**
     * 清空所有服务
     */
    void clear();
}
