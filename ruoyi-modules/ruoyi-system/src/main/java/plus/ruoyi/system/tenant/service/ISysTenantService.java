package plus.ruoyi.system.tenant.service;

import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.system.tenant.domain.bo.SysTenantBo;
import plus.ruoyi.system.tenant.domain.vo.SysTenantVo;

import java.util.Collection;
import java.util.List;

/**
 * 租户Service接口
 *
 * @author Michelle.Chung
 */
public interface ISysTenantService {

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return 视图对象
     */
    SysTenantVo get(Long id);

    /**
     * 查询列表
     *
     * @param bo 查询参数
     * @return 列表数据
     */
    List<SysTenantVo> list(SysTenantBo bo);

    /**
     * 分页查询
     *
     * @param bo        查询参数
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    PageResult<SysTenantVo> page(SysTenantBo bo, PageQuery pageQuery);

    /**
     * 新增
     *
     * @param bo 业务对象
     * @return 主键ID
     */
    Long add(SysTenantBo bo);

    /**
     * 修改
     *
     * @param bo 业务对象
     * @return 影响行数
     */
    int update(SysTenantBo bo);

    /**
     * 批量删除
     *
     * @param ids ID集合
     * @return 影响行数
     */
    int batchDelete(Collection<Long> ids);

    /**
     * 批量保存
     *
     * @param boList 业务对象集合
     * @return 影响行数
     */
    int batchSave(List<SysTenantBo> boList);

    /**
     * 基于租户ID查询租户
     *
     * @param tenantId 租户ID
     * @return 租户信息
     */
    SysTenantVo getTenantByTenantId(String tenantId);

    /**
     * 新增租户
     *
     * @param bo 租户业务对象
     * @return 是否成功
     */
    boolean insertTenant(SysTenantBo bo);

    /**
     * 修改租户
     *
     * @param bo 租户业务对象
     * @return 是否成功
     */
    boolean updateTenant(SysTenantBo bo);

    /**
     * 修改租户状态
     *
     * @param bo 租户业务对象
     * @return 是否成功
     */
    boolean updateTenantStatus(SysTenantBo bo);

    /**
     * 校验租户是否允许操作
     *
     * @param tenantId 租户ID
     */
    void checkTenantAllowed(String tenantId);

    /**
     * 校验企业名称是否唯一
     *
     * @param bo 租户业务对象
     * @return 是否唯一
     */
    boolean checkCompanyNameUnique(SysTenantBo bo);

    /**
     * 校验账号余额(注册用户有限)
     *
     * @param tenantId 租户ID
     * @return 是否有足够的用户余额
     */
    boolean checkAccountBalance(String tenantId);

    /**
     * 校验有效期
     *
     * @param tenantId 租户ID
     * @return 是否过期
     */
    boolean checkExpireTime(String tenantId);

    /**
     * 同步租户套餐
     *
     * @param tenantId  租户ID
     * @param packageId 套餐ID
     * @return 是否成功
     */
    boolean syncTenantPackage(String tenantId, Long packageId);

    /**
     * 同步租户角色
     */
    void syncTenantRoles();

    /**
     * 同步租户字典
     */
    void syncTenantDicts();

    /**
     * 获取租户标题
     *
     * @return 租户标题
     */
    String getTenantTitle();

    /**
     * 同步租户参数配置
     */
    void syncTenantConfigs();
}
