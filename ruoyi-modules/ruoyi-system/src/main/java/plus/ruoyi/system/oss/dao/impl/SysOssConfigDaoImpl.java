package plus.ruoyi.system.oss.dao.impl;

import org.springframework.stereotype.Repository;
import plus.ruoyi.common.core.dict.DictEnableStatus;
import plus.ruoyi.common.mybatis.core.dao.impl.BaseDaoImpl;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.system.oss.dao.ISysOssConfigDao;
import plus.ruoyi.system.oss.domain.SysOssConfig;
import plus.ruoyi.system.oss.domain.bo.SysOssConfigBo;
import plus.ruoyi.system.oss.mapper.SysOssConfigMapper;


/**
 * 对象存储配置数据访问实现
 *
 * @author 抓蛙师
 */
@Repository
public class SysOssConfigDaoImpl extends BaseDaoImpl<SysOssConfigMapper, SysOssConfig> implements ISysOssConfigDao {

    /**
     * 构建查询条件
     *
     * @param bo 查询参数
     * @return 查询条件
     */
    @Override
    public PlusLambdaQuery<SysOssConfig> buildQueryWrapper(SysOssConfigBo bo) {
        PlusLambdaQuery<SysOssConfig> lqw = PlusLambdaQuery.of(SysOssConfig.class);
        lqw.eq(SysOssConfig::getConfigKey, bo.getConfigKey());
        lqw.like(SysOssConfig::getBucketName, bo.getBucketName());
        lqw.eq(SysOssConfig::getStatus, bo.getStatus());
        lqw.orderByAsc(SysOssConfig::getOssConfigId);
        return lqw;
    }

    /**
     * 根据配置key查询配置
     *
     * @param configKey 配置key
     * @return 配置实体
     */
    @Override
    public SysOssConfig getByConfigKey(String configKey) {
        PlusLambdaQuery<SysOssConfig> lqw = PlusLambdaQuery.of(SysOssConfig.class)
            .select(SysOssConfig::getOssConfigId, SysOssConfig::getConfigKey)
            .eq(SysOssConfig::getConfigKey, configKey);
        return getOne(lqw);
    }

    /**
     * 校验配置key是否唯一
     *
     * @param configKey 配置key
     * @param ossConfigId 排除的配置ID
     * @return 是否唯一
     */
    @Override
    public boolean checkConfigKeyUnique(String configKey, Long ossConfigId) {
        PlusLambdaQuery<SysOssConfig> lqw = PlusLambdaQuery.of(SysOssConfig.class)
            .eq(SysOssConfig::getConfigKey, configKey)
            .ne(SysOssConfig::getOssConfigId, ossConfigId);
        return !exists(lqw);
    }

    /**
     * 批量禁用所有配置
     *
     * @return 更新数量
     */
    @Override
    public int disableAllConfigs() {
        return lambdaUpdate()
            .set(SysOssConfig::getStatus, DictEnableStatus.DISABLED.getValue())
            .update();
    }

}
