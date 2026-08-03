package plus.ruoyi.system.core.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import plus.ruoyi.common.mybatis.core.dao.impl.BaseDaoImpl;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.system.core.dao.ISysUserPostDao;
import plus.ruoyi.system.core.domain.SysUserPost;
import plus.ruoyi.system.core.mapper.SysUserPostMapper;

import java.util.List;

/**
 * 用户岗位关联数据访问实现
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Repository
public class SysUserPostDaoImpl extends BaseDaoImpl<SysUserPostMapper, SysUserPost> implements ISysUserPostDao {

    /**
     * 根据用户ID删除用户岗位关联
     */
    @Override
    public int deleteByUserId(Long userId) {
        PlusLambdaQuery<SysUserPost> lqw = PlusLambdaQuery.of(SysUserPost.class)
            .eq(SysUserPost::getUserId, userId);
        return baseMapper.delete(lqw);
    }

    /**
     * 根据用户ID列表批量删除用户岗位关联
     */
    @Override
    public int batchDeleteByUserIds(List<Long> userIds) {
        PlusLambdaQuery<SysUserPost> lqw = PlusLambdaQuery.of(SysUserPost.class)
            .in(SysUserPost::getUserId, userIds);
        return baseMapper.delete(lqw);
    }

    /**
     * 根据岗位ID列表查询用户岗位关联
     */
    @Override
    public List<SysUserPost> listByPostIds(List<Long> postIds) {
        PlusLambdaQuery<SysUserPost> lqw = PlusLambdaQuery.of(SysUserPost.class)
            .in(SysUserPost::getPostId, postIds);
        return list(lqw);
    }

    /**
     * 批量插入用户岗位关联
     * 使用batchInsert而不是batchSave，避免因为没有主键导致的插入变更新问题
     */
    @Override
    public boolean batchInsertUserPosts(List<SysUserPost> list) {
        return batchInsert(list) > 0;
    }

    /**
     * 根据岗位ID统计用户数量
     */
    @Override
    public long countByPostId(Long postId) {
        PlusLambdaQuery<SysUserPost> lqw = PlusLambdaQuery.of(SysUserPost.class)
            .eq(SysUserPost::getPostId, postId);
        return count(lqw);
    }
}
