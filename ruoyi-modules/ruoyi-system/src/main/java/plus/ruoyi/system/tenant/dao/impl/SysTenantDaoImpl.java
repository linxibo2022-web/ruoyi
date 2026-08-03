package plus.ruoyi.system.tenant.dao.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Repository;
import plus.ruoyi.common.core.dict.DictEnableStatus;
import plus.ruoyi.common.core.utils.StreamUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.mybatis.core.dao.impl.BaseDaoImpl;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.system.tenant.dao.ISysTenantDao;
import plus.ruoyi.system.tenant.domain.SysTenant;
import plus.ruoyi.system.tenant.domain.bo.SysTenantBo;
import plus.ruoyi.system.tenant.mapper.SysTenantMapper;

import java.util.List;

/**
 * 租户数据访问实现
 *
 * @author Michelle.Chung
 */
@Repository
public class SysTenantDaoImpl extends BaseDaoImpl<SysTenantMapper, SysTenant> implements ISysTenantDao {

    /**
     * 构建查询条件
     *
     * @param bo 查询参数
     * @return 查询条件
     */
    @Override
    public PlusLambdaQuery<SysTenant> buildQueryWrapper(SysTenantBo bo) {
        PlusLambdaQuery<SysTenant> lqw = PlusLambdaQuery.of(SysTenant.class);
        lqw.eq(SysTenant::getTenantId, bo.getTenantId());
        lqw.like(SysTenant::getContactUserName, bo.getContactUserName());
        lqw.eq(SysTenant::getContactPhone, bo.getContactPhone());
        lqw.like(SysTenant::getCompanyName, bo.getCompanyName());
        lqw.eq(SysTenant::getLicenseNumber, bo.getLicenseNumber());
        lqw.eq(SysTenant::getAddress, bo.getAddress());
        lqw.eq(SysTenant::getIntro, bo.getIntro());
        lqw.like(SysTenant::getDomain, bo.getDomain());
        lqw.eq(SysTenant::getPackageId, bo.getPackageId());
        lqw.eq(SysTenant::getExpireTime, bo.getExpireTime());
        lqw.eq(SysTenant::getAccountCount, bo.getAccountCount());
        lqw.eq(SysTenant::getStatus, bo.getStatus());

        // 模糊搜索（跨数据库兼容：String 类型用 like，非 String 类型用 likeCast）
        String searchValue = bo.getSearchValue();
        if (ObjectUtil.isNotEmpty(searchValue)) {
            lqw.and(w -> w
                .like(SysTenant::getTenantId, searchValue)              // String
                .or().like(SysTenant::getContactUserName, searchValue)  // String
                .or().like(SysTenant::getContactPhone, searchValue)     // String
                .or().like(SysTenant::getCompanyName, searchValue)      // String
                .or().like(SysTenant::getLicenseNumber, searchValue)    // String
                .or().like(SysTenant::getAddress, searchValue)          // String
                .or().like(SysTenant::getIntro, searchValue)            // String
                .or().like(SysTenant::getDomain, searchValue)           // String
                .or().likeCast(SysTenant::getPackageId, searchValue)    // Long
                .or().likeCast(SysTenant::getAccountCount, searchValue)); // Long
        }
        return lqw;
    }

    /**
     * 根据租户ID查询租户
     *
     * @param tenantId 租户ID
     * @return 租户实体
     */
    @Override
    public SysTenant getByTenantId(String tenantId) {
        PlusLambdaQuery<SysTenant> wrapper = PlusLambdaQuery.of(SysTenant.class)
            .eq(SysTenant::getTenantId, tenantId);
        return getOne(wrapper);
    }

    /**
     * 查询所有租户ID列表
     *
     * @return 租户ID列表
     */
    @Override
    public List<String> listAllTenantIds() {
        PlusLambdaQuery<SysTenant> wrapper = PlusLambdaQuery.of(SysTenant.class)
            .select(SysTenant::getTenantId);
        return mapList(wrapper, Convert::toStr);
    }

    /**
     * 检查企业名称是否已存在
     *
     * @param companyName 企业名称
     * @param tenantId    租户ID(用于排除自身)
     * @return 是否存在
     */
    @Override
    public boolean existsByCompanyName(String companyName, String tenantId) {
        PlusLambdaQuery<SysTenant> wrapper = PlusLambdaQuery.of(SysTenant.class)
            .eq(SysTenant::getCompanyName, companyName)
            .ne(ObjectUtil.isNotNull(tenantId), SysTenant::getTenantId, tenantId);
        return exists(wrapper);
    }

    /**
     * 根据租户ID查询套餐ID
     *
     * @param tenantId 租户ID
     * @return 租户实体(仅包含packageId)
     */
    @Override
    public SysTenant getPackageIdByTenantId(String tenantId) {
        PlusLambdaQuery<SysTenant> wrapper = PlusLambdaQuery.of(SysTenant.class)
            .eq(SysTenant::getTenantId, tenantId)
            .select(SysTenant::getPackageId);
        return getOne(wrapper);
    }

    /**
     * 查询所有启用的租户ID(排除默认租户)
     *
     * @param defaultTenantId 默认租户ID
     * @return 启用的租户ID列表
     */
    @Override
    public List<String> listEnabledTenantIds(String defaultTenantId) {
        PlusLambdaQuery<SysTenant> wrapper = PlusLambdaQuery.of(SysTenant.class)
            .select(SysTenant::getTenantId)
            .eq(SysTenant::getStatus, DictEnableStatus.ENABLE.getValue())  // 启用状态
            .ne(SysTenant::getTenantId, defaultTenantId);
        return mapList(wrapper, Convert::toStr);
    }

    /**
     * 根据域名和状态查询租户
     *
     * @param domain 域名
     * @param status 状态
     * @param throwEx 是否在结果不唯一时抛出异常
     * @return 租户实体
     */
    @Override
    public SysTenant getByDomainAndStatus(String domain, String status, boolean throwEx) {
        PlusLambdaQuery<SysTenant> wrapper = PlusLambdaQuery.of(SysTenant.class)
            .eq(SysTenant::getDomain, StringUtils.splitToList(domain, ":").get(0))
            .eq(SysTenant::getStatus, status);
        return getOne(wrapper, throwEx);
    }

    /**
     * 检查套餐下是否存在租户
     */
    @Override
    public boolean existsByPackageIds(List<Long> packageIds) {
        PlusLambdaQuery<SysTenant> wrapper = PlusLambdaQuery.of(SysTenant.class)
            .in(SysTenant::getPackageId, packageIds);
        return exists(wrapper);
    }

}
