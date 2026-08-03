package plus.ruoyi.system.core.service;

import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.system.core.domain.bo.SysUserBo;
import plus.ruoyi.system.core.domain.vo.SysUserExportVo;
import plus.ruoyi.system.core.domain.vo.SysUserVo;

import java.util.List;

/**
 * 用户 业务层
 *
 * @author Lion Li
 */
public interface ISysUserService {


    /**
     * 根据条件分页查询用户列表
     *
     * @param user      用户信息
     * @param pageQuery 发呢也
     * @return 用户信息
     */
    PageResult<SysUserVo> pageUsers(SysUserBo user, PageQuery pageQuery);

    /**
     * 根据条件分页查询用户导出列表
     *
     * @param user      用户信息
     * @param pageQuery 分页参数
     * @return 用户信息集合信息
     */
    PageResult<SysUserExportVo> pageUserExports(SysUserBo user, PageQuery pageQuery);

    /**
     * 根据条件分页查询已分配角色的用户列表
     *
     * @param user      用户信息
     * @param pageQuery 分页参数
     * @return 用户信息集合信息
     */
    PageResult<SysUserVo> pageRoleAuthorizedUsers(SysUserBo user, PageQuery pageQuery);

    /**
     * 根据条件分页查询未分配用户角色列表
     *
     * @param user      用户信息
     * @param pageQuery 分页
     * @return 用户信息集合信息
     */
    PageResult<SysUserVo> pageRoleUnauthorizedUsers(SysUserBo user, PageQuery pageQuery);

    /**
     * 通过用户名查询用户
     *
     * @param userName 用户名
     * @return 用户对象信息
     */
    SysUserVo getUserByUserName(String userName);

    /**
     * 通过手机号查询用户
     *
     * @param phone 手机号
     * @return 用户对象信息
     */
    SysUserVo getUserByPhone(String phone);

    /**
     * 通过邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户对象信息
     */
    SysUserVo getUserByEmail(String email);

    /**
     * 通过用户ID查询用户
     *
     * @param userId 用户ID
     * @return 用户对象信息
     */
    SysUserVo getUserById(Long userId);

    /**
     * 根据用户名或昵称模糊查询用户
     *
     * @param nameKeyword 用户名或昵称关键字
     * @return 用户对象信息
     */
    SysUserVo getUserByNameKeyword(String nameKeyword);

    /**
     * 通过用户ID查询用户(带角色信息)
     *
     * @param userId 用户ID
     * @return 用户对象信息
     */
    SysUserVo getUserWithRolesById(Long userId);

    /**
     * 通过用户ID串查询用户
     *
     * @param userIds 用户ID串
     * @param deptId  部门id
     * @return 用户列表信息
     */
    List<SysUserVo> listUsersByIdsAndDeptId(List<Long> userIds, Long deptId);

    /**
     * 根据用户ID查询用户所属角色组
     *
     * @param userId 用户ID
     * @return 结果
     */
    String getUserRoleGroup(Long userId);

    /**
     * 根据用户ID查询用户所属岗位组
     *
     * @param userId 用户ID
     * @return 结果
     */
    String getUserPostGroup(Long userId);

    /**
     * 判断用户名称是否唯一
     *
     * @param userName 用户名
     * @param userId   用户ID（可为null，新增时传null）
     * @return 结果
     */
    boolean isUserNameUnique(String userName, Long userId);

    /**
     * 判断手机号码是否唯一
     *
     * @param phone  手机号码
     * @param userId 用户ID（可为null，新增时传null）
     * @return 返回结果为假则唯一
     */
    boolean isPhoneUnique(String phone, Long userId);

    /**
     * 判断email是否唯一
     *
     * @param email  邮箱地址
     * @param userId 用户ID（可为null，新增时传null）
     * @return 返回结果为假则唯一
     */
    boolean isEmailUnique(String email, Long userId);

    /**
     * 判断用户是否允许操作
     *
     * @param userId 用户ID
     */
    void checkUserAllowed(Long userId);

    /**
     * 校验用户是否有数据权限
     *
     * @param userId 用户id
     */
    void checkUserDataScope(Long userId);

    /**
     * 新增用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    Long insertUser(SysUserBo user);

    /**
     * 注册用户信息
     *
     * @param user     用户信息
     * @param tenantId 租户id
     * @return 结果
     */
    boolean registerPcUser(SysUserBo user, String tenantId);

    /**
     * 修改用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    boolean updateUser(SysUserBo user);

    /**
     * 分配用户角色
     *
     * @param userId  用户ID
     * @param roleIds 角色组
     */
    void assignUserRoles(Long userId, List<Long> roleIds);

    /**
     * 修改用户状态
     *
     * @param userId 用户ID
     * @param status 帐号状态
     * @return 结果
     */
    boolean updateUserStatus(Long userId, String status);

    /**
     * 修改用户基本信息
     *
     * @param user 用户信息
     * @return 结果
     */
    boolean updateUserProfile(SysUserBo user);

    /**
     * 修改用户头像
     *
     * @param userId 用户ID
     * @param avatar 头像地址
     * @return 结果
     */
    boolean updateUserAvatar(Long userId, String avatar);

    /**
     * 重置用户密码
     *
     * @param userId   用户ID
     * @param password 密码
     * @return 结果
     */
    boolean resetUserPwd(Long userId, String password);

    /**
     * 通过用户ID删除用户
     *
     * @param userId 用户ID
     * @return 结果
     */
    boolean deleteUserById(Long userId);

    /**
     * 批量删除用户信息
     *
     * @param userIds 需要删除的用户ID
     * @return 结果
     */
    boolean deleteUserByIds(Long[] userIds);

    /**
     * 通过部门id查询当前部门所有用户
     *
     * @param deptId 部门id
     * @return 结果
     */
    List<SysUserVo> listUsersByDeptId(Long deptId);

    /**
     * 更新用户昵称和头像
     *
     * @param userId   用户ID
     * @param nickName 用户昵称
     * @param avatar   头像地址
     */
    void updateUserNickNameAvatar(Long userId, String nickName, String avatar);
}
