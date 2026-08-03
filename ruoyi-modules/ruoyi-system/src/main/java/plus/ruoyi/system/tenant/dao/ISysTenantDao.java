package plus.ruoyi.system.tenant.dao;

import plus.ruoyi.common.mybatis.core.dao.IBaseDao;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.system.tenant.domain.SysTenant;
import plus.ruoyi.system.tenant.domain.bo.SysTenantBo;

import java.util.List;

/**
 * 租户DAO接口
 *
 * @author Michelle.Chung
 */
public interface ISysTenantDao extends IBaseDao<SysTenant> {

    /**
     * 根据业务对象构建查询条件
     *
     * @param bo 查询参数
     * @return 查询条件
     */
    PlusLambdaQuery<SysTenant> buildQueryWrapper(SysTenantBo bo);

    /**
     * 根据租户ID查询租户
     *
     * @param tenantId 租户ID
     * @return 租户实体
     */
    SysTenant getByTenantId(String tenantId);

    /**
     * 查询所有租户ID列表
     *
     * @return 租户ID列表
     */
    List<String> listAllTenantIds();

    /**
     * 检查企业名称是否已存在
     *
     * @param companyName 企业名称
     * @param tenantId    租户ID(用于排除自身)
     * @return 是否存在
     */
    boolean existsByCompanyName(String companyName, String tenantId);

    /**
     * 根据租户ID查询套餐ID
     *
     * @param tenantId 租户ID
     * @return 租户实体(仅包含packageId)
     */
    SysTenant getPackageIdByTenantId(String tenantId);

    /**
     * 查询所有启用的租户ID(排除默认租户)
     *
     * @param defaultTenantId 默认租户ID
     * @return 启用的租户ID列表
     */
    List<String> listEnabledTenantIds(String defaultTenantId);

    /**
     * 根据域名和状态查询租户
     *
     * @param domain 域名
     * @param status 状态
     * @param throwEx 是否在结果不唯一时抛出异常
     * @return 租户实体
     */
    SysTenant getByDomainAndStatus(String domain, String status, boolean throwEx);

    /**
     * 检查套餐下是否存在租户
     *
     * @param packageIds 套餐ID列表
     * @return 是否存在
     */
    boolean existsByPackageIds(List<Long> packageIds);
}
