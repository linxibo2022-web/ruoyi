package plus.ruoyi.system.config.dao;

import plus.ruoyi.common.mybatis.core.dao.IBaseDao;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.system.config.domain.SysConfig;
import plus.ruoyi.system.config.domain.bo.SysConfigBo;

import java.util.List;

/**
 * 参数配置DAO接口
 *
 * @author 抓蛙师
 */
public interface ISysConfigDao extends IBaseDao<SysConfig> {

    /**
     * 根据业务对象构建查询条件
     */
    PlusLambdaQuery<SysConfig> buildQueryWrapper(SysConfigBo bo);

    /**
     * 根据参数键名查询配置
     *
     * @param configKey 参数键名
     * @return 参数配置
     */
    SysConfig getByConfigKey(String configKey);

    /**
     * 根据参数键名更新配置
     *
     * @param config    配置对象
     * @param configKey 参数键名
     * @return 是否成功
     */
    boolean updateByConfigKey(SysConfig config, String configKey);

    /**
     * 根据租户ID查询所有配置
     *
     * @param tenantId 租户ID
     * @return 配置列表
     */
    List<SysConfig> listByTenantId(String tenantId);
}
