package plus.ruoyi.system.core.dao.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Repository;
import plus.ruoyi.common.core.dict.DictEnableStatus;
import plus.ruoyi.common.mybatis.annotation.DataColumn;
import plus.ruoyi.common.mybatis.annotation.DataPermission;
import plus.ruoyi.common.mybatis.core.dao.impl.BaseDaoImpl;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.system.core.dao.ISysPostDao;
import plus.ruoyi.system.core.domain.SysPost;
import plus.ruoyi.system.core.domain.bo.SysPostBo;
import plus.ruoyi.system.core.mapper.SysPostMapper;

import java.util.List;

/**
 * 岗位数据访问实现
 *
 * @author Lion Li
 */
@Repository
public class SysPostDaoImpl extends BaseDaoImpl<SysPostMapper, SysPost> implements ISysPostDao {

    /**
     * 构建查询条件
     */
    @Override
    public PlusLambdaQuery<SysPost> buildQueryWrapper(SysPostBo bo) {
        PlusLambdaQuery<SysPost> lqw = PlusLambdaQuery.of(SysPost.class)
            .like(SysPost::getPostCode, bo.getPostCode())
            .like(SysPost::getPostName, bo.getPostName())
            .eq(SysPost::getStatus, bo.getStatus())
            .eq(SysPost::getDeptId, bo.getDeptId())
            .orderByAsc(SysPost::getPostSort);

        // 部门树搜索（Service层已设置deptIds）
        if (CollUtil.isNotEmpty(bo.getDeptIds())) {
            lqw.in(SysPost::getDeptId, bo.getDeptIds());
        }

        return lqw;
    }

    /**
     * 分页查询(带数据权限)
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "dept_id"),
        @DataColumn(key = "userName", value = "create_by")
    })
    @Override
    public IPage<SysPost> pageWithPermission(IPage<SysPost> page, Wrapper<SysPost> wrapper) {
        return baseMapper.selectPage(page, wrapper);
    }

    /**
     * 根据用户ID查询岗位列表
     */
    @Override
    public List<SysPost> listPostsByUserId(Long userId) {
        return baseMapper.selectPostsByUserId(userId);
    }

    /**
     * 根据岗位ID列表查询正常状态的岗位
     */
    @Override
    public List<SysPost> listNormalPostsByIds(List<Long> postIds) {
        PlusLambdaQuery<SysPost> lqw = PlusLambdaQuery.of(SysPost.class)
            .select(SysPost::getPostId, SysPost::getPostName, SysPost::getPostCode)
            .eq(SysPost::getStatus, DictEnableStatus.ENABLE.getValue())
            .in(SysPost::getPostId, postIds);
        return list(lqw);
    }

    /**
     * 校验岗位名称在同一部门内的唯一性
     */
    @Override
    public boolean checkPostNameUnique(String postName, Long deptId, Long postId) {
        PlusLambdaQuery<SysPost> lqw = PlusLambdaQuery.of(SysPost.class)
            .eq(SysPost::getPostName, postName)
            .eq(SysPost::getDeptId, deptId)
            .ne(SysPost::getPostId, postId);
        return !exists(lqw);
    }

    /**
     * 校验岗位编码的唯一性
     */
    @Override
    public boolean checkPostCodeUnique(String postCode, Long postId) {
        PlusLambdaQuery<SysPost> lqw = PlusLambdaQuery.of(SysPost.class)
            .eq(SysPost::getPostCode, postCode)
            .ne(SysPost::getPostId, postId);
        return !exists(lqw);
    }

    /**
     * 统计部门下的岗位数量
     */
    @Override
    public long countPostsByDeptId(Long deptId) {
        PlusLambdaQuery<SysPost> lqw = PlusLambdaQuery.of(SysPost.class)
            .eq(SysPost::getDeptId, deptId);
        return count(lqw);
    }

    /**
     * 根据岗位ID列表查询岗位
     */
    @Override
    public List<SysPost> listByPostIds(List<Long> postIds) {
        PlusLambdaQuery<SysPost> lqw = PlusLambdaQuery.of(SysPost.class)
            .select(SysPost::getPostId, SysPost::getPostName)
            .in(SysPost::getPostId, postIds);
        return list(lqw);
    }

}
