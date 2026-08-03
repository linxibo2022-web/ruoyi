package plus.ruoyi.system.core.service;

import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.system.core.domain.bo.SysPostBo;
import plus.ruoyi.system.core.domain.vo.SysPostVo;

import java.util.Collection;
import java.util.List;

/**
 * 岗位信息 服务层
 *
 * @author Lion Li
 */
public interface ISysPostService {

    /**
     * 根据ID查询
     *
     * @param postId 主键ID
     * @return 岗位VO
     */
    SysPostVo get(Long postId);

    /**
     * 查询列表
     *
     * @param bo 业务对象
     * @return 岗位VO列表
     */
    List<SysPostVo> list(SysPostBo bo);

    /**
     * 分页查询
     *
     * @param bo 业务对象
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    PageResult<SysPostVo> page(SysPostBo bo, PageQuery pageQuery);

    /**
     * 新增
     *
     * @param bo 业务对象
     * @return 主键ID
     */
    Long add(SysPostBo bo);

    /**
     * 修改
     *
     * @param bo 业务对象
     * @return 影响行数
     */
    int update(SysPostBo bo);

    /**
     * 批量删除
     *
     * @param ids 主键ID集合
     * @return 影响行数
     */
    int batchDelete(Collection<Long> ids);

    /**
     * 批量保存
     *
     * @param boList 业务对象列表
     * @return 影响行数
     */
    int batchSave(List<SysPostBo> boList);

    /**
     * 查询用户所属岗位组
     *
     * @param userId 用户ID
     * @return 岗位列表
     */
    List<SysPostVo> listPostsByUserId(Long userId);

    /**
     * 根据用户ID获取岗位id列表
     *
     * @param userId 用户ID
     * @return 选中岗位ID列表
     */
    List<Long> listPostIdsByUserId(Long userId);

    /**
     * 通过岗位ID串查询岗位
     *
     * @param postIds 岗位id串
     * @return 岗位列表信息
     */
    List<SysPostVo> listPostsByIds(List<Long> postIds);

    /**
     * 校验岗位名称
     *
     * @param post 岗位信息
     * @return 结果
     */
    boolean checkPostNameUnique(SysPostBo post);

    /**
     * 校验岗位编码
     *
     * @param post 岗位信息
     * @return 结果
     */
    boolean checkPostCodeUnique(SysPostBo post);

    /**
     * 通过岗位ID查询岗位使用数量
     *
     * @param postId 岗位ID
     * @return 结果
     */
    long countUsersByPostId(Long postId);

    /**
     * 通过部门ID查询岗位使用数量
     *
     * @param deptId 部门id
     * @return 结果
     */
    long countPostsByDeptId(Long deptId);
}
