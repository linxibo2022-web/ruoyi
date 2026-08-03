package plus.ruoyi.common.core.service;

/**
 * 通用 租户服务
 *
 * @author 抓蛙师
 */
public interface TenantService {

    /**
     * 获取当前租户id
     *
     * @return 租户id
     */
    String getTenantId();


    /**
     * 根据请求获取租户ID
     * 专门处理从请求中提取租户信息的逻辑：域名识别 + 请求头获取
     *
     * @return 租户ID，如果获取不到返回null
     */
    String getTenantIdByRequest();
}
