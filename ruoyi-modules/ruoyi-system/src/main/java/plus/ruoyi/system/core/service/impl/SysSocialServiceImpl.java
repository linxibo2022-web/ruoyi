package plus.ruoyi.system.core.service.impl;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.utils.MapstructUtils;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.system.core.dao.ISysSocialDao;
import plus.ruoyi.system.core.domain.SysSocial;
import plus.ruoyi.system.core.domain.bo.SysSocialBo;
import plus.ruoyi.system.core.domain.vo.SysSocialVo;
import plus.ruoyi.system.core.service.ISysSocialService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 社会化关系Service业务层处理
 *
 * @author thiszhc
 * @date 2023-06-12
 */
@RequiredArgsConstructor
@Service
public class SysSocialServiceImpl implements ISysSocialService {

    private final ISysSocialDao socialDao;

    /**
     * 根据ID查询
     */
    @Override
    public SysSocialVo get(Long id) {
        SysSocial entity = socialDao.getById(id);
        return MapstructUtils.convert(entity, SysSocialVo.class);
    }

    /**
     * 查询列表
     */
    @Override
    public List<SysSocialVo> list(SysSocialBo bo) {
        PlusLambdaQuery<SysSocial> wrapper = socialDao.buildQueryWrapper(bo);
        List<SysSocial> entities = socialDao.list(wrapper);
        return MapstructUtils.convert(entities, SysSocialVo.class);
    }

    /**
     * 分页查询
     */
    @Override
    public PageResult<SysSocialVo> page(SysSocialBo bo, PageQuery pageQuery) {
        PlusLambdaQuery<SysSocial> wrapper = socialDao.buildQueryWrapper(bo);
        PageResult<SysSocial> entityPage = socialDao.page(wrapper, pageQuery);
        return entityPage.convert(SysSocialVo.class);
    }

    /**
     * 新增
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(SysSocialBo bo) {
        SysSocial entity = MapstructUtils.convert(bo, SysSocial.class);
        beforeSave(entity);
        socialDao.insert(entity);
        return entity.getId();
    }

    /**
     * 修改
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(SysSocialBo bo) {
        if (bo.getId() == null) {
            throw ServiceException.of("主键ID不能为空");
        }
        if (!socialDao.exists(bo.getId())) {
            throw ServiceException.of("社会化关系不存在");
        }
        SysSocial entity = MapstructUtils.convert(bo, SysSocial.class);
        beforeSave(entity);
        return socialDao.updateById(entity);
    }

    /**
     * 批量删除
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            throw ServiceException.of("ID集合不能为空");
        }
        beforeDelete(ids);
        return socialDao.deleteByIds(ids);
    }

    /**
     * 批量保存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSave(List<SysSocialBo> boList) {
        if (CollUtil.isEmpty(boList)) {
            return 0;
        }
        List<SysSocial> entities = new ArrayList<>(boList.size());
        for (SysSocialBo bo : boList) {
            SysSocial entity = MapstructUtils.convert(bo, SysSocial.class);
            beforeSave(entity);
            entities.add(entity);
        }
        return socialDao.batchSave(entities);
    }

    /**
     * 保存前的数据校验
     */
    protected void beforeSave(SysSocial entity) {
        // 业务校验逻辑
    }

    /**
     * 删除前的业务规则校验
     */
    protected void beforeDelete(Collection<Long> ids) {
        // 业务校验逻辑
    }

    /**
     * 根据用户ID查询社会化账号绑定列表
     */
    @Override
    public List<SysSocialVo> listSocialsByUserId(Long userId) {
        List<SysSocial> entities = socialDao.listByUserId(userId);
        return MapstructUtils.convert(entities, SysSocialVo.class);
    }

    /**
     * 根据第三方平台认证ID查询社会化关系列表
     */
    @Override
    public List<SysSocialVo> listSocialsByAuthId(String authId) {
        List<SysSocial> entities = socialDao.listByAuthId(authId);
        return MapstructUtils.convert(entities, SysSocialVo.class);
    }

}
