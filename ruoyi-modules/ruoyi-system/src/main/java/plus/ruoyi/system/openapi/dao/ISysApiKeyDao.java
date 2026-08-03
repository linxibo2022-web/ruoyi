package plus.ruoyi.system.openapi.dao;

import plus.ruoyi.common.mybatis.core.dao.IBaseDao;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.system.openapi.domain.SysApiKey;
import plus.ruoyi.system.openapi.domain.bo.SysApiKeyBo;

/**
 * API密钥DAO接口
 *
 * @author 抓蛙师
 */
public interface ISysApiKeyDao extends IBaseDao<SysApiKey> {

    /**
     * 根据业务对象构建查询条件
     */
    PlusLambdaQuery<SysApiKey> buildQueryWrapper(SysApiKeyBo bo);

    /**
     * 根据AppKey查询API密钥
     *
     * @param appKey AppKey
     * @return API密钥实体
     */
    SysApiKey getByAppKey(String appKey);

    /**
     * 根据AppKey查询启用状态的API密钥
     *
     * @param appKey AppKey
     * @param status 状态
     * @return API密钥实体
     */
    SysApiKey getByAppKeyAndStatus(String appKey, String status);

    /**
     * 校验AppKey是否唯一
     *
     * @param appKey AppKey
     * @param id     排除的ID
     * @return 是否唯一
     */
    boolean checkAppKeyUnique(String appKey, Long id);
}
