package plus.ruoyi.system.tenant.dao.impl;

import org.springframework.stereotype.Repository;
import plus.ruoyi.common.mybatis.core.dao.impl.BaseDaoImpl;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.system.tenant.dao.ISysTenantPackageDao;
import plus.ruoyi.system.tenant.domain.SysTenantPackage;
import plus.ruoyi.system.tenant.domain.bo.SysTenantPackageBo;
import plus.ruoyi.system.tenant.mapper.SysTenantPackageMapper;


/**
 * 租户套餐数据访问实现
 *
 * @author Michelle.Chung
 */
@Repository
public class SysTenantPackageDaoImpl extends BaseDaoImpl<SysTenantPackageMapper, SysTenantPackage> implements ISysTenantPackageDao {

    /**
     * 构建查询条件
     *
     * @param bo 查询参数
     * @return 查询条件
     */
    @Override
    public PlusLambdaQuery<SysTenantPackage> buildQueryWrapper(SysTenantPackageBo bo) {
        PlusLambdaQuery<SysTenantPackage> lqw = PlusLambdaQuery.of(SysTenantPackage.class);
        lqw.like(SysTenantPackage::getPackageName, bo.getPackageName());
        lqw.eq(SysTenantPackage::getStatus, bo.getStatus());
        return lqw;
    }

    /**
     * 检查套餐名称是否唯一
     */
    @Override
    public boolean checkPackageNameUnique(String packageName, Long packageId) {
        PlusLambdaQuery<SysTenantPackage> lqw = PlusLambdaQuery.of(SysTenantPackage.class)
            .eq(SysTenantPackage::getPackageName, packageName)
            .ne(packageId != null, SysTenantPackage::getPackageId, packageId);
        return !exists(lqw);
    }

}
