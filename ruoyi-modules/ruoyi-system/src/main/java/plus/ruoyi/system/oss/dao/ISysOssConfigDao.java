package plus.ruoyi.system.oss.dao;

import plus.ruoyi.common.mybatis.core.dao.IBaseDao;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.system.oss.domain.SysOssConfig;
import plus.ruoyi.system.oss.domain.bo.SysOssConfigBo;

/**
 * 对象存储配置DAO接口
 *
 * @author 抓蛙师
 */
public interface ISysOssConfigDao extends IBaseDao<SysOssConfig> {

    /**
     * 根据业务对象构建查询条件
     */
    PlusLambdaQuery<SysOssConfig> buildQueryWrapper(SysOssConfigBo bo);

    /**
     * 根据配置key查询配置
     *
     * @param configKey 配置key
     * @return 配置实体
     */
    SysOssConfig getByConfigKey(String configKey);

    /**
     * 校验配置key是否唯一
     *
     * @param configKey 配置key
     * @param ossConfigId 排除的配置ID
     * @return 是否唯一
     */
    boolean checkConfigKeyUnique(String configKey, Long ossConfigId);

    /**
     * 批量禁用所有配置
     *
     * @return 更新数量
     */
    int disableAllConfigs();
}
