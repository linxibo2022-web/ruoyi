package plus.ruoyi.system.tenant.service;

import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.system.tenant.domain.bo.SysTenantPackageBo;
import plus.ruoyi.system.tenant.domain.vo.SysTenantPackageVo;

import java.util.Collection;
import java.util.List;


/**
 * 租户套餐Service接口
 *
 * @author Michelle.Chung
 */
public interface ISysTenantPackageService {

    /**
     * 根据ID查询
     *
     * @param packageId 主键ID
     * @return 视图对象
     */
    SysTenantPackageVo get(Long packageId);

    /**
     * 查询列表
     *
     * @param bo 查询参数
     * @return 列表数据
     */
    List<SysTenantPackageVo> list(SysTenantPackageBo bo);

    /**
     * 分页查询
     *
     * @param bo        查询参数
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    PageResult<SysTenantPackageVo> page(SysTenantPackageBo bo, PageQuery pageQuery);

    /**
     * 新增
     *
     * @param bo 业务对象
     * @return 主键ID
     */
    Long add(SysTenantPackageBo bo);

    /**
     * 修改
     *
     * @param bo 业务对象
     * @return 影响行数
     */
    int update(SysTenantPackageBo bo);

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
    int batchSave(List<SysTenantPackageBo> boList);

    /**
     * 校验套餐名称是否唯一
     */
    boolean checkPackageNameUnique(SysTenantPackageBo bo);

    /**
     * 修改套餐状态
     */
    boolean updatePackageStatus(SysTenantPackageBo bo);

}
