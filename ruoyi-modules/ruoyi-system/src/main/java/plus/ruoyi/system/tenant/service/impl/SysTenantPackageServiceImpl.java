package plus.ruoyi.system.tenant.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.utils.MapstructUtils;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.system.tenant.dao.ISysTenantDao;
import plus.ruoyi.system.tenant.dao.ISysTenantPackageDao;
import plus.ruoyi.system.tenant.domain.SysTenant;
import plus.ruoyi.system.tenant.domain.SysTenantPackage;
import plus.ruoyi.system.tenant.domain.bo.SysTenantPackageBo;
import plus.ruoyi.system.tenant.domain.vo.SysTenantPackageVo;
import plus.ruoyi.system.tenant.service.ISysTenantPackageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 租户套餐Service业务层处理
 *
 * @author Michelle.Chung
 */
@RequiredArgsConstructor
@Service
public class SysTenantPackageServiceImpl implements ISysTenantPackageService {

    private final ISysTenantPackageDao packageDao;
    private final ISysTenantDao tenantDao;

    /**
     * 根据ID查询
     *
     * @param packageId 主键ID
     * @return 视图对象
     */
    @Override
    public SysTenantPackageVo get(Long packageId) {
        SysTenantPackage entity = packageDao.getById(packageId);
        return MapstructUtils.convert(entity, SysTenantPackageVo.class);
    }

    /**
     * 查询列表
     *
     * @param bo 查询参数
     * @return 列表数据
     */
    @Override
    public List<SysTenantPackageVo> list(SysTenantPackageBo bo) {
        PlusLambdaQuery<SysTenantPackage> wrapper = packageDao.buildQueryWrapper(bo);
        List<SysTenantPackage> entities = packageDao.list(wrapper);
        return MapstructUtils.convert(entities, SysTenantPackageVo.class);
    }

    /**
     * 分页查询
     *
     * @param bo        查询参数
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    @Override
    public PageResult<SysTenantPackageVo> page(SysTenantPackageBo bo, PageQuery pageQuery) {
        PlusLambdaQuery<SysTenantPackage> wrapper = packageDao.buildQueryWrapper(bo);
        PageResult<SysTenantPackage> entityPage = packageDao.page(wrapper, pageQuery);
        return entityPage.convert(SysTenantPackageVo.class);
    }

    /**
     * 新增
     *
     * @param bo 业务对象
     * @return 主键ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(SysTenantPackageBo bo) {
        SysTenantPackage entity = MapstructUtils.convert(bo, SysTenantPackage.class);
        beforeSave(entity);
        packageDao.insert(entity);
        return entity.getPackageId();
    }

    /**
     * 修改
     *
     * @param bo 业务对象
     * @return 影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(SysTenantPackageBo bo) {
        if (bo.getPackageId() == null) {
            throw ServiceException.of("套餐ID不能为空");
        }
        if (!packageDao.exists(bo.getPackageId())) {
            throw ServiceException.of("套餐不存在");
        }
        SysTenantPackage entity = MapstructUtils.convert(bo, SysTenantPackage.class);
        beforeSave(entity);
        return packageDao.updateById(entity);
    }

    /**
     * 批量删除
     *
     * @param ids ID集合
     * @return 影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            throw ServiceException.of("ID集合不能为空");
        }
        beforeDelete(ids);
        return packageDao.deleteByIds(ids);
    }

    /**
     * 批量保存
     *
     * @param boList 业务对象集合
     * @return 影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSave(List<SysTenantPackageBo> boList) {
        if (CollUtil.isEmpty(boList)) {
            return 0;
        }
        List<SysTenantPackage> entities = new ArrayList<>(boList.size());
        for (SysTenantPackageBo bo : boList) {
            SysTenantPackage entity = MapstructUtils.convert(bo, SysTenantPackage.class);
            beforeSave(entity);
            entities.add(entity);
        }
        return packageDao.batchSave(entities);
    }

    /**
     * 保存前的数据校验
     *
     * @param entity 待保存实体
     */
    protected void beforeSave(SysTenantPackage entity) {
        // 可以在这里添加保存前的通用校验逻辑
    }

    /**
     * 删除前的业务规则校验
     *
     * @param ids 待删除数据ID集合
     */
    protected void beforeDelete(Collection<Long> ids) {
        List<Long> idList = new ArrayList<>(ids);
        boolean exists = tenantDao.existsByPackageIds(idList);
        if (exists) {
            throw ServiceException.of("租户套餐已被使用");
        }
    }

    /**
     * 校验套餐名称是否唯一
     */
    @Override
    public boolean checkPackageNameUnique(SysTenantPackageBo bo) {
        return packageDao.checkPackageNameUnique(bo.getPackageName(), bo.getPackageId());
    }

    /**
     * 修改套餐状态
     *
     * @param bo 套餐信息
     * @return 结果
     */
    @Override
    public boolean updatePackageStatus(SysTenantPackageBo bo) {
        SysTenantPackage tenantPackage = MapstructUtils.convert(bo, SysTenantPackage.class);
        return packageDao.updateById(tenantPackage) > 0;
    }
}
