package plus.ruoyi.system.config.dao.impl;

import org.springframework.stereotype.Repository;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.mybatis.core.dao.impl.BaseDaoImpl;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.system.config.dao.ISysConfigDao;
import plus.ruoyi.system.config.domain.SysConfig;
import plus.ruoyi.system.config.domain.bo.SysConfigBo;
import plus.ruoyi.system.config.mapper.SysConfigMapper;

import java.util.List;
import java.util.Map;

/**
 * 参数配置数据访问实现
 *
 * @author 抓蛙师
 */
@Repository
public class SysConfigDaoImpl extends BaseDaoImpl<SysConfigMapper, SysConfig> implements ISysConfigDao {

    /**
     * 构建查询条件
     *
     * @param bo 查询参数
     * @return 查询条件
     */
    @Override
    public PlusLambdaQuery<SysConfig> buildQueryWrapper(SysConfigBo bo) {
        Map<String, Object> params = bo.getParams();
        PlusLambdaQuery<SysConfig> lqw = PlusLambdaQuery.of(SysConfig.class);

        // 精确匹配查询条件
        lqw.like(SysConfig::getConfigName, bo.getConfigName());
        lqw.eq(SysConfig::getConfigType, bo.getConfigType());
        lqw.like(SysConfig::getConfigKey, bo.getConfigKey());
        lqw.between(SysConfig::getCreateTime, params.get("beginTime"), params.get("endTime"));
        lqw.orderByAsc(SysConfig::getConfigId);

        // 模糊查询（跨数据库兼容：String 类型用 like，非 String 类型用 likeCast）
        String searchValue = bo.getSearchValue();
        if (StringUtils.isNotBlank(searchValue)) {
            lqw.and(w -> w
                .like(SysConfig::getConfigName, searchValue)          // String
                .or().like(SysConfig::getConfigKey, searchValue)      // String
                .or().like(SysConfig::getConfigValue, searchValue)    // String
                .or().like(SysConfig::getRemark, searchValue)         // String
                .or().likeCast(SysConfig::getConfigId, searchValue)   // Long
            );
        }
        return lqw;
    }

    /**
     * 根据参数键名查询配置
     *
     * @param configKey 参数键名
     * @return 参数配置
     */
    @Override
    public SysConfig getByConfigKey(String configKey) {
        PlusLambdaQuery<SysConfig> lqw = PlusLambdaQuery.of(SysConfig.class)
            .eq(SysConfig::getConfigKey, configKey);
        return getOne(lqw);
    }

    /**
     * 根据参数键名更新配置
     */
    @Override
    public boolean updateByConfigKey(SysConfig config, String configKey) {
        PlusLambdaQuery<SysConfig> lqw = PlusLambdaQuery.of(SysConfig.class)
            .eq(SysConfig::getConfigKey, configKey);
        return update(config, lqw) > 0;
    }

    /**
     * 根据租户ID查询所有配置
     *
     * @param tenantId 租户ID
     * @return 配置列表
     */
    @Override
    public List<SysConfig> listByTenantId(String tenantId) {
        PlusLambdaQuery<SysConfig> lqw = PlusLambdaQuery.of(SysConfig.class)
            .eq(SysConfig::getTenantId, tenantId);
        return list(lqw);
    }
}
