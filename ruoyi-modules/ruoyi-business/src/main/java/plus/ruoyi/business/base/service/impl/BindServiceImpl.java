package plus.ruoyi.business.base.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.lock.annotation.Lock4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plus.ruoyi.business.base.dao.IBindDao;
import plus.ruoyi.business.base.domain.Bind;
import plus.ruoyi.business.base.domain.bo.BindBo;
import plus.ruoyi.business.base.domain.vo.BindVo;
import plus.ruoyi.business.base.domain.vo.PlatformUserInfoVo;
import plus.ruoyi.business.base.service.IBindService;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.utils.MapstructUtils;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 账号绑定服务实现
 *
 * @author 抓蛙师
 */
@Service
@RequiredArgsConstructor
public class BindServiceImpl implements IBindService {

    private final IBindDao bindDao;

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return 视图对象
     */
    @Override
    public BindVo get(Long id) {
        Bind entity = bindDao.getById(id);
        return MapstructUtils.convert(entity, BindVo.class);
    }

    /**
     * 查询列表
     *
     * @param bo 查询参数
     * @return 列表数据
     */
    @Override
    public List<BindVo> list(BindBo bo) {
        PlusLambdaQuery<Bind> wrapper = bindDao.buildQueryWrapper(bo);
        List<Bind> entities = bindDao.list(wrapper);
        return MapstructUtils.convert(entities, BindVo.class);
    }

    /**
     * 分页查询
     *
     * @param bo        查询参数
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    @Override
    public PageResult<BindVo> page(BindBo bo, PageQuery pageQuery) {
        PlusLambdaQuery<Bind> wrapper = bindDao.buildQueryWrapper(bo);
        PageResult<Bind> entityPage = bindDao.page(wrapper, pageQuery);
        return entityPage.convert(BindVo.class);
    }

    /**
     * 新增
     *
     * @param bo 业务对象
     * @return 主键ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(BindBo bo) {
        Bind entity = MapstructUtils.convert(bo, Bind.class);
        beforeSave(entity);
        bindDao.insert(entity);
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
    public int update(BindBo bo) {
        if (bo.getId() == null) {
            throw ServiceException.of("账号绑定ID不能为空");
        }
        if (!bindDao.exists(bo.getId())) {
            throw ServiceException.of("账号绑定不存在");
        }
        Bind entity = MapstructUtils.convert(bo, Bind.class);
        beforeSave(entity);
        return bindDao.updateById(entity);
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
        return bindDao.deleteByIds(ids);
    }

    /**
     * 批量保存
     *
     * @param boList 业务对象集合
     * @return 影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSave(List<BindBo> boList) {
        if (CollUtil.isEmpty(boList)) {
            return 0;
        }
        List<Bind> entities = new ArrayList<>(boList.size());
        for (BindBo bo : boList) {
            Bind entity = MapstructUtils.convert(bo, Bind.class);
            beforeSave(entity);
            entities.add(entity);
        }
        return bindDao.batchSave(entities);
    }

    /**
     * 保存前钩子方法
     * 子类可重写此方法实现数据校验、默认值设置等逻辑
     *
     * @param entity 实体对象
     */
    protected void beforeSave(Bind entity) {
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

    /**
     * 根据小程序用户信息获取或者新建绑定信息
     * 使用 @Lock4j 分布式锁防止并发插入重复数据
     *
     * @param platformUserInfoVo 小程序用户信息
     * @return 绑定信息
     */
    @Override
    @Lock4j(keys = {"#platformUserInfoVo.platform", "#platformUserInfoVo.openid"}, expire = 10000)
    @Transactional(rollbackFor = Exception.class)
    public BindVo getOrCreateBind(PlatformUserInfoVo platformUserInfoVo) {
        String platform = platformUserInfoVo.getPlatform();
        String openid = platformUserInfoVo.getOpenid();

        // 查询是否已存在绑定信息
        Bind bind = bindDao.getByPlatformAndOpenid(platform, openid);
        if (ObjectUtil.isNotNull(bind)) {
            return MapstructUtils.convert(bind, BindVo.class);
        }

        // 如果没有绑定信息，则创建新的绑定信息
        Bind newBind = new Bind();
        newBind.setPlatformType(platform);
        newBind.setUnionid(platformUserInfoVo.getUnionid());
        newBind.setOpenid(openid);
        newBind.setAppid(platformUserInfoVo.getAppid());
        bindDao.insert(newBind);

        return MapstructUtils.convert(newBind, BindVo.class);
    }

    /**
     * 更新绑定信息
     *
     * @param bindBo 绑定信息
     * @param userId 用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBindUserId(BindBo bindBo, Long userId) {
        Bind bind = MapstructUtils.convert(bindBo, Bind.class);
        bind.setUserId(userId);
        bindDao.updateById(bind);
    }
}
