package plus.ruoyi.system.core.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import plus.ruoyi.common.mybatis.core.dao.IBaseDao;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.system.core.domain.SysUser;
import plus.ruoyi.system.core.domain.bo.SysUserBo;
import plus.ruoyi.system.core.domain.vo.SysUserExportVo;
import plus.ruoyi.system.core.domain.vo.SysUserVo;

import java.util.List;

/**
 * 用户数据访问接口
 *
 * @author Lion Li
 */
public interface ISysUserDao extends IBaseDao<SysUser> {

    /**
     * 构建查询条件
     */
    PlusLambdaQuery<SysUser> buildQueryWrapper(SysUserBo bo);

    /**
     * 分页查询用户列表(带数据权限)
     */
    PageResult<SysUserVo> pageWithPermission(SysUserBo bo, IPage<SysUser> page);

    /**
     * 查询用户列表(带数据权限)
     */
    List<SysUserVo> listWithPermission(PlusLambdaQuery<SysUser> wrapper);

    /**
     * 导出用户列表(带数据权限)
     */
    List<SysUserExportVo> listExportWithPermission(PlusLambdaQuery<SysUser> wrapper);

    /**
     * 分页查询角色已授权用户列表(带数据权限)
     */
    PageResult<SysUserVo> pageRoleAuthorizedUsers(SysUserBo bo, IPage<SysUser> page);

    /**
     * 分页查询角色未授权用户列表(带数据权限)
     */
    PageResult<SysUserVo> pageRoleUnauthorizedUsers(SysUserBo bo, List<Long> excludeUserIds, IPage<SysUser> page);

    /**
     * 统计用户数量(带数据权限)
     */
    long countUserById(Long userId);

    /**
     * 根据用户ID列表和部门ID查询用户列表
     */
    List<SysUser> listUsersByIdsAndDeptId(List<Long> userIds, Long deptId);

    /**
     * 根据部门ID查询用户列表
     */
    List<SysUser> listUsersByDeptId(Long deptId);

    /**
     * 根据用户名查询用户
     */
    SysUser getByUserName(String userName);

    /**
     * 根据手机号查询用户
     */
    SysUser getByPhone(String phone);

    /**
     * 根据邮箱查询用户
     */
    SysUser getByEmail(String email);

    /**
     * 根据ID查询用户
     */
    SysUser getById(Long userId);

    /**
     * 更新用户昵称和头像
     */
    boolean updateUserNickNameAvatar(Long userId, String nickName, String avatar);

    /**
     * 校验用户名唯一性
     */
    boolean checkUserNameUnique(String userName, Long userId);

    /**
     * 校验手机号唯一性
     */
    boolean checkPhoneUnique(String phone, Long userId);

    /**
     * 校验邮箱唯一性
     */
    boolean checkEmailUnique(String email, Long userId);

    /**
     * 新增用户
     */
    Long add(SysUser user);

    /**
     * 更新用户状态
     */
    boolean updateUserStatus(Long userId, String status);

    /**
     * 更新用户基本信息
     */
    boolean updateUserProfile(SysUserBo user);

    /**
     * 更新用户头像
     */
    boolean updateUserAvatar(Long userId, String avatar);

    /**
     * 重置用户密码
     */
    boolean resetUserPwd(Long userId, String password);

    /**
     * 删除用户
     */
    boolean deleteById(Long userId);

    /**
     * 批量删除用户
     */
    boolean deleteByIds(List<Long> userIds);

    /**
     * 根据用户ID查询用户名
     */
    String getUserNameById(Long userId);

    /**
     * 根据状态查询用户列表（带数据权限）
     * <p>
     * 默认带数据权限过滤，需要忽略权限时请使用 DataPermissionHelper.ignore() 包装调用
     *
     * @param status 用户状态
     * @return 用户列表
     */
    List<SysUser> listByStatus(String status);

    /**
     * 根据部门ID列表和状态查询用户列表（带数据权限）
     * <p>
     * 默认带数据权限过滤，需要忽略权限时请使用 DataPermissionHelper.ignore() 包装调用
     *
     * @param deptIds 部门ID列表
     * @param status  用户状态
     * @return 用户列表
     */
    List<SysUser> listByDeptIdsAndStatus(List<Long> deptIds, String status);

    /**
     * 根据用户ID列表和状态查询用户列表（带数据权限）
     * <p>
     * 默认带数据权限过滤，需要忽略权限时请使用 DataPermissionHelper.ignore() 包装调用
     *
     * @param userIds 用户ID列表
     * @param status  用户状态
     * @return 用户列表
     */
    List<SysUser> listByUserIdsAndStatus(List<Long> userIds, String status);

    /**
     * 根据时间范围统计用户数量
     *
     * @param startTime 开始时间(可为null)
     * @param endTime   结束时间(可为null)
     * @return 用户数量
     */
    long countByTimeRange(java.util.Date startTime, java.util.Date endTime);

    /**
     * 根据登录时间范围统计活跃用户数量
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 活跃用户数量
     */
    long countActiveByLoginDateRange(java.util.Date startTime, java.util.Date endTime);

    /**
     * 根据用户ID查询昵称
     */
    String getNickNameById(Long userId);

    /**
     * 根据用户ID查询头像
     */
    String getAvatarById(Long userId);

    /**
     * 根据用户ID查询手机号
     */
    String getPhoneById(Long userId);

    /**
     * 根据用户ID查询邮箱
     */
    String getEmailById(Long userId);

    /**
     * 根据用户ID列表查询用户列表
     */
    List<SysUser> listByIds(List<Long> userIds);

    /**
     * 根据部门ID列表查询用户列表
     */
    List<SysUser> listByDeptIds(List<Long> deptIds);

    /**
     * 根据用户ID列表查询用户名称映射
     */
    List<SysUser> listUserNamesById(List<Long> userIds);

    /**
     * 根据用户名或昵称模糊查询用户
     *
     * @param nameKeyword 用户名或昵称关键字
     * @return 用户对象
     */
    SysUser getByNameKeyword(String nameKeyword);

    /**
     * 检查部门下是否存在用户
     *
     * @param deptId 部门ID
     * @return 是否存在
     */
    boolean existsByDeptId(Long deptId);

    /**
     * 检查用户名是否存在
     *
     * @param userName 用户名
     * @return 是否存在
     */
    boolean existsByUserName(String userName);
}
