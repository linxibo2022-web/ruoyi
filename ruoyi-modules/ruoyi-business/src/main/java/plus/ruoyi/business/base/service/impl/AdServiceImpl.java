package plus.ruoyi.business.base.service.impl;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plus.ruoyi.business.base.dao.IAdDao;
import plus.ruoyi.business.base.domain.Ad;
import plus.ruoyi.business.base.domain.bo.AdBo;
import plus.ruoyi.business.base.domain.vo.AdVo;
import plus.ruoyi.business.base.service.IAdService;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.utils.MapstructUtils;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 广告配置服务实现
 *
 * @author 抓蛙师
 * @date 2025-10-07
 */
@Service
@RequiredArgsConstructor
public class AdServiceImpl implements IAdService {

    private final IAdDao adDao;

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return 视图对象
     */
    @Override
    public AdVo get(Long id) {
        Ad entity = adDao.getById(id);
        return MapstructUtils.convert(entity, AdVo.class);
    }

    /**
     * 查询列表
     *
     * @param bo 查询参数
     * @return 列表数据
     */
    @Override
    public List<AdVo> list(AdBo bo) {
        PlusLambdaQuery<Ad> wrapper = adDao.buildQueryWrapper(bo);
        List<Ad> entities = adDao.list(wrapper);
        return MapstructUtils.convert(entities, AdVo.class);
    }

    /**
     * 分页查询
     *
     * @param bo        查询参数
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    @Override
    public PageResult<AdVo> page(AdBo bo, PageQuery pageQuery) {
        PlusLambdaQuery<Ad> wrapper = adDao.buildQueryWrapper(bo);
        PageResult<Ad> entityPage = adDao.page(wrapper, pageQuery);
        return entityPage.convert(AdVo.class);
    }

    /**
     * 新增
     *
     * @param bo 业务对象
     * @return 主键ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(AdBo bo) {
        Ad entity = MapstructUtils.convert(bo, Ad.class);
        beforeSave(entity);
        adDao.insert(entity);
        return entity.getId();
    }

    /**
     * 修改
     *
     * @param bo 业务对象
     * @return 影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(AdBo bo) {
        if (bo.getId() == null) {
            throw ServiceException.of("广告配置ID不能为空");
        }
        if (!adDao.exists(bo.getId())) {
            throw ServiceException.of("广告配置不存在");
        }
        Ad entity = MapstructUtils.convert(bo, Ad.class);
        beforeSave(entity);
        return adDao.updateById(entity);
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
        return adDao.deleteByIds(ids);
    }

    /**
     * 批量保存
     *
     * @param boList 业务对象集合
     * @return 影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSave(List<AdBo> boList) {
        if (CollUtil.isEmpty(boList)) {
            return 0;
        }
        List<Ad> entities = new ArrayList<>(boList.size());
        for (AdBo bo : boList) {
            Ad entity = MapstructUtils.convert(bo, Ad.class);
            beforeSave(entity);
            entities.add(entity);
        }
        return adDao.batchSave(entities);
    }

    /**
     * 保存前钩子方法
     * 子类可重写此方法实现数据校验、默认值设置等逻辑
     *
     * @param entity 实体对象
     */
    protected void beforeSave(Ad entity) {
        // 默认实现为空,子类按需重写
    }

    /**
     * 删除前钩子方法
     * 子类可重写此方法实现关联数据校验、清理等逻辑
     *
     * @param ids 待删除的ID集合
     */
    protected void beforeDelete(Collection<Long> ids) {
        // 默认实现为空,子类按需重写
    }
}
