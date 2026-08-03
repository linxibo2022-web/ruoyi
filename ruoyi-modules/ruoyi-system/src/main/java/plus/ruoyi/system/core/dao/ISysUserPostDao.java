package plus.ruoyi.system.core.dao;

import plus.ruoyi.common.mybatis.core.dao.IBaseDao;
import plus.ruoyi.system.core.domain.SysUserPost;

import java.util.List;

/**
 * 用户岗位关联数据访问接口
 *
 * @author Lion Li
 */
public interface ISysUserPostDao extends IBaseDao<SysUserPost> {

    /**
     * 根据用户ID删除用户岗位关联
     */
    int deleteByUserId(Long userId);

    /**
     * 根据用户ID列表批量删除用户岗位关联
     */
    int batchDeleteByUserIds(List<Long> userIds);

    /**
     * 根据岗位ID列表查询用户岗位关联
     */
    List<SysUserPost> listByPostIds(List<Long> postIds);

    /**
     * 批量插入用户岗位关联
     */
    boolean batchInsertUserPosts(List<SysUserPost> list);

    /**
     * 根据岗位ID统计用户数量
     *
     * @param postId 岗位ID
     * @return 用户数量
     */
    long countByPostId(Long postId);
}
