package plus.ruoyi.system.core.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.service.PostService;
import plus.ruoyi.common.core.utils.MapstructUtils;
import plus.ruoyi.common.core.utils.StreamUtils;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.system.core.dao.ISysDeptDao;
import plus.ruoyi.system.core.dao.ISysPostDao;
import plus.ruoyi.system.core.dao.ISysUserPostDao;
import plus.ruoyi.system.core.domain.SysPost;
import plus.ruoyi.system.core.domain.bo.SysPostBo;
import plus.ruoyi.system.core.domain.vo.SysPostVo;
import plus.ruoyi.system.core.service.ISysPostService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 岗位管理业务层处理
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Service
public class SysPostServiceImpl implements ISysPostService, PostService {

    private final ISysPostDao postDao;
    private final ISysUserPostDao userPostDao;
    private final ISysDeptDao deptDao;

    // ================ 基础CRUD实现 =================

    /**
     * 根据ID查询
     */
    @Override
    public SysPostVo get(Long postId) {
        SysPost entity = postDao.getById(postId);
        return MapstructUtils.convert(entity, SysPostVo.class);
    }

    /**
     * 查询列表
     */
    @Override
    public List<SysPostVo> list(SysPostBo bo) {
        processDeptIds(bo);
        PlusLambdaQuery<SysPost> wrapper = postDao.buildQueryWrapper(bo);
        List<SysPost> entities = postDao.list(wrapper);
        return MapstructUtils.convert(entities, SysPostVo.class);
    }

    /**
     * 分页查询(带数据权限)
     */
    @Override
    public PageResult<SysPostVo> page(SysPostBo bo, PageQuery pageQuery) {
        processDeptIds(bo);
        PlusLambdaQuery<SysPost> wrapper = postDao.buildQueryWrapper(bo);
        IPage<SysPost> page = postDao.pageWithPermission(pageQuery.build(), wrapper);
        PageResult<SysPost> entityPage = PageResult.of(page);
        return entityPage.convert(SysPostVo.class);
    }

    /**
     * 新增
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(SysPostBo bo) {
        SysPost entity = MapstructUtils.convert(bo, SysPost.class);
        beforeSave(entity);
        postDao.insert(entity);
        return entity.getPostId();
    }

    /**
     * 修改
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(SysPostBo bo) {
        SysPost entity = MapstructUtils.convert(bo, SysPost.class);
        beforeSave(entity);
        return postDao.updateById(entity);
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
        return postDao.deleteByIds(ids);
    }

    /**
     * 批量保存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSave(List<SysPostBo> boList) {
        if (CollUtil.isEmpty(boList)) {
            return 0;
        }
        List<SysPost> entities = MapstructUtils.convert(boList, SysPost.class);
        return postDao.batchSave(entities);
    }

    // ================ 钩子方法 =================

    /**
     * 保存前的数据校验
     */
    protected void beforeSave(SysPost entity) {
        // 保存前数据校验，可根据业务需求扩展
    }

    /**
     * 删除前的业务校验
     * <p>
     * 检查岗位是否已分配给用户，已分配的岗位不允许删除
     *
     * @param ids 待删除的岗位ID集合
     */
    protected void beforeDelete(Collection<Long> ids) {
        for (Long postId : ids) {
            SysPost post = postDao.getById(postId);
            if (ObjectUtil.isNotNull(post) && countUsersByPostId(postId) > 0) {
                throw ServiceException.of("{}已分配，不能删除!", post.getPostName());
            }
        }
    }

    // ================ 用户岗位关联查询 =================

    /**
     * 查询用户所属岗位组
     *
     * @param userId 用户ID
     * @return 岗位ID
     */
    @Override
    public List<SysPostVo> listPostsByUserId(Long userId) {
        List<SysPost> posts = postDao.listPostsByUserId(userId);
        return MapstructUtils.convert(posts, SysPostVo.class);
    }

    /**
     * 根据用户ID获取岗位id列表
     *
     * @param userId 用户ID
     * @return 选中岗位ID列表
     */
    @Override
    public List<Long> listPostIdsByUserId(Long userId) {
        List<SysPost> list = postDao.listPostsByUserId(userId);
        return StreamUtils.toList(list, SysPost::getPostId);
    }

    /**
     * 通过岗位ID串查询岗位
     *
     * @param postIds 岗位ID列表
     * @return 岗位列表信息
     */
    @Override
    public List<SysPostVo> listPostsByIds(List<Long> postIds) {
        List<SysPost> entities = postDao.listNormalPostsByIds(postIds);
        return MapstructUtils.convert(entities, SysPostVo.class);
    }

    // ================ 业务校验方法 =================

    /**
     * 校验岗位名称是否唯一
     * <p>
     * 在同一部门内，岗位名称必须唯一
     *
     * @param post 岗位信息
     * @return true表示唯一，false表示重复
     */
    @Override
    public boolean checkPostNameUnique(SysPostBo post) {
        return postDao.checkPostNameUnique(
            post.getPostName(),
            post.getDeptId(),
            ObjectUtil.defaultIfNull(post.getPostId(), -1L)
        );
    }

    /**
     * 校验岗位编码是否唯一
     * <p>
     * 岗位编码在全系统范围内必须唯一
     *
     * @param post 岗位信息
     * @return true表示唯一，false表示重复
     */
    @Override
    public boolean checkPostCodeUnique(SysPostBo post) {
        return postDao.checkPostCodeUnique(
            post.getPostCode(),
            ObjectUtil.defaultIfNull(post.getPostId(), -1L)
        );
    }

    // ================ 统计查询方法 =================

    /**
     * 通过岗位ID查询岗位使用数量
     *
     * @param postId 岗位ID
     * @return 使用该岗位的用户数量
     */
    @Override
    public long countUsersByPostId(Long postId) {
        return userPostDao.countByPostId(postId);
    }

    /**
     * 通过部门ID查询岗位数量
     *
     * @param deptId 部门ID
     * @return 该部门的岗位数量
     */
    @Override
    public long countPostsByDeptId(Long deptId) {
        return postDao.countPostsByDeptId(deptId);
    }

    // ================ 辅助方法 =================

    /**
     * Service层辅助方法：处理部门树查询
     */
    private void processDeptIds(SysPostBo bo) {
        if (ObjectUtil.isNotNull(bo.getBelongDeptId()) && ObjectUtil.isNull(bo.getDeptId())) {
            List<Long> deptIds = deptDao.listChildrenDeptIds(bo.getBelongDeptId());
            if (!deptIds.contains(bo.getBelongDeptId())) {
                deptIds.add(bo.getBelongDeptId());
            }
            bo.setDeptIds(deptIds);
        }
    }

    // ================ PostService 通用接口实现 =================

    /**
     * 根据岗位ID列表查询岗位名称映射关系
     *
     * @param postIds 岗位ID列表
     * @return Map，其中key为岗位ID，value为对应的岗位名称
     */
    @Override
    public Map<Long, String> mapPostNames(List<Long> postIds) {
        if (CollUtil.isEmpty(postIds)) {
            return Collections.emptyMap();
        }

        return postDao.listByPostIds(postIds)
            .stream()
            .collect(Collectors.toMap(SysPost::getPostId, SysPost::getPostName));
    }
}
