package plus.ruoyi.system.core.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import plus.ruoyi.common.mybatis.core.dao.IBaseDao;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.system.core.domain.SysPost;
import plus.ruoyi.system.core.domain.bo.SysPostBo;
import plus.ruoyi.system.core.domain.vo.SysPostVo;

import java.util.List;

/**
 * 岗位数据访问层
 *
 * @author Lion Li
 */
public interface ISysPostDao extends IBaseDao<SysPost> {

    /**
     * 构建查询条件
     *
     * @param bo 查询条件
     * @return 查询构造器
     */
    PlusLambdaQuery<SysPost> buildQueryWrapper(SysPostBo bo);

    /**
     * 分页查询(带数据权限)
     *
     * @param page 分页对象
     * @param wrapper 查询条件
     * @return 分页结果
     */
    IPage<SysPost> pageWithPermission(IPage<SysPost> page, Wrapper<SysPost> wrapper);

    /**
     * 根据用户ID查询岗位列表
     *
     * @param userId 用户ID
     * @return 岗位列表
     */
    List<SysPost> listPostsByUserId(Long userId);

    /**
     * 根据岗位ID列表查询正常状态的岗位
     *
     * @param postIds 岗位ID列表
     * @return 岗位列表
     */
    List<SysPost> listNormalPostsByIds(List<Long> postIds);

    /**
     * 校验岗位名称在同一部门内的唯一性
     *
     * @param postName 岗位名称
     * @param deptId 部门ID
     * @param postId 岗位ID(排除自己)
     * @return true表示唯一
     */
    boolean checkPostNameUnique(String postName, Long deptId, Long postId);

    /**
     * 校验岗位编码的唯一性
     *
     * @param postCode 岗位编码
     * @param postId 岗位ID(排除自己)
     * @return true表示唯一
     */
    boolean checkPostCodeUnique(String postCode, Long postId);

    /**
     * 统计部门下的岗位数量
     *
     * @param deptId 部门ID
     * @return 岗位数量
     */
    long countPostsByDeptId(Long deptId);

    /**
     * 根据岗位ID列表查询岗位
     *
     * @param postIds 岗位ID列表
     * @return 岗位列表
     */
    List<SysPost> listByPostIds(List<Long> postIds);
}
