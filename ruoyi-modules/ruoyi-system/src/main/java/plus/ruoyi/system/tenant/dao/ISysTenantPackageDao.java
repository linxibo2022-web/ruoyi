package plus.ruoyi.system.tenant.dao;

import plus.ruoyi.common.mybatis.core.dao.IBaseDao;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.system.tenant.domain.SysTenantPackage;
import plus.ruoyi.system.tenant.domain.bo.SysTenantPackageBo;

/**
 * 租户套餐DAO接口
 *
 * @author Michelle.Chung
 */
public interface ISysTenantPackageDao extends IBaseDao<SysTenantPackage> {

    /**
     * 根据业务对象构建查询条件
     *
     * @param bo 查询参数
     * @return 查询条件
     */
    PlusLambdaQuery<SysTenantPackage> buildQueryWrapper(SysTenantPackageBo bo);

    /**
     * 检查套餐名称是否唯一
     *
     * @param packageName 套餐名称
     * @param packageId   套餐ID(用于排除自身)
     * @return 是否唯一
     */
    boolean checkPackageNameUnique(String packageName, Long packageId);
}
