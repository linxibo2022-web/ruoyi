package plus.ruoyi.system.core.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.constant.CacheNames;
import plus.ruoyi.common.core.constant.SystemConstants;
import plus.ruoyi.common.core.domain.dto.UserDTO;
import plus.ruoyi.common.core.enums.UserType;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.service.UserService;
import plus.ruoyi.common.core.utils.*;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.common.satoken.utils.LoginHelper;
import plus.ruoyi.system.core.dao.*;
import plus.ruoyi.system.core.domain.*;
import plus.ruoyi.system.core.domain.bo.SysRoleBo;
import plus.ruoyi.system.core.domain.bo.SysUserBo;
import plus.ruoyi.system.core.domain.vo.SysRoleVo;
import plus.ruoyi.system.core.domain.vo.SysUserExportVo;
import plus.ruoyi.system.core.domain.vo.SysUserVo;
import plus.ruoyi.system.core.service.ISysRoleService;
import plus.ruoyi.system.core.service.ISysUserService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户 业务层处理
 *
 * @author Lion Li
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SysUserServiceImpl implements ISysUserService, UserService {

    private final ISysDeptDao deptDao;
    private final ISysRoleDao roleDao;
    private final ISysPostDao postDao;
    private final ISysUserDao userDao;
    private final ISysUserRoleDao userRoleDao;
    private final ISysUserPostDao userPostDao;
    private final ISysRoleService roleService;

    // ================ 基础CRUD实现 =================


    // ================ 分页查询相关方法 =================

    /**
     * 根据条件分页查询用户列表
     *
     * @param user      用户查询条件
     * @param pageQuery 分页参数
     * @return 用户信息集合
     */
    @Override
    public PageResult<SysUserVo> pageUsers(SysUserBo user, PageQuery pageQuery) {
        // Service层处理：根据部门树查询子部门ID列表
        processDeptIds(user);
        return userDao.pageWithPermission(user, pageQuery.build());
    }

    /**
     * 根据条件分页查询用户导出列表
     *
     * @param user      用户查询条件
     * @param pageQuery 分页参数
     * @return 用户导出信息集合
     */
    @Override
    public PageResult<SysUserExportVo> pageUserExports(SysUserBo user, PageQuery pageQuery) {
        // Service层处理：根据部门树查询子部门ID列表
        processDeptIds(user);
        List<SysUserExportVo> list = userDao.listExportWithPermission(userDao.buildQueryWrapper(user));
        return PageResult.of(list);
    }

    /**
     * Service层辅助方法：处理部门树查询
     */
    private void processDeptIds(SysUserBo user) {
        if (ObjectUtil.isNotNull(user.getDeptId())) {
            List<Long> deptIds = deptDao.listChildrenDeptIds(user.getDeptId());
            deptIds.add(user.getDeptId());
            user.setDeptIds(deptIds);
        }
    }

    /**
     * 根据条件分页查询已分配角色的用户列表
     *
     * @param user      用户信息
     * @param pageQuery 分页参数
     * @return 用户信息集合
     */
    @Override
    public PageResult<SysUserVo> pageRoleAuthorizedUsers(SysUserBo user, PageQuery pageQuery) {
        return userDao.pageRoleAuthorizedUsers(user, pageQuery.build());
    }

    /**
     * 根据条件分页查询未分配用户角色列表
     *
     * @param user      用户信息
     * @param pageQuery 分页参数
     * @return 用户信息集合
     */
    @Override
    public PageResult<SysUserVo> pageRoleUnauthorizedUsers(SysUserBo user, PageQuery pageQuery) {
        List<Long> userIds = userRoleDao.listUserIdsByRoleId(user.getRoleId());
        return userDao.pageRoleUnauthorizedUsers(user, userIds, pageQuery.build());
    }

    // ================ 用户查询相关方法 =================

    /**
     * 通过用户名查询用户
     *
     * @param userName 用户名
     * @return 用户对象信息
     */
    @Override
    public SysUserVo getUserByUserName(String userName) {
        SysUser user = userDao.getByUserName(userName);
        return MapstructUtils.convert(user, SysUserVo.class);
    }

    /**
     * 通过手机号查询用户
     *
     * @param phone 手机号
     * @return 用户对象信息
     */
    @Override
    public SysUserVo getUserByPhone(String phone) {
        SysUser user = userDao.getByPhone(phone);
        return MapstructUtils.convert(user, SysUserVo.class);
    }

    /**
     * 通过邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户对象信息
     */
    @Override
    public SysUserVo getUserByEmail(String email) {
        SysUser user = userDao.getByEmail(email);
        return MapstructUtils.convert(user, SysUserVo.class);
    }

    /**
     * 通过用户ID查询用户
     *
     * @param userId 用户ID
     * @return 用户对象信息
     */
    @Override
    public SysUserVo getUserById(Long userId) {
        SysUser entity = userDao.getById(userId);
        return MapstructUtils.convert(entity, SysUserVo.class);
    }

    /**
     * 根据用户名或昵称模糊查询用户
     *
     * @param nameKeyword 用户名或昵称关键字
     * @return 用户对象信息
     */
    @Override
    public SysUserVo getUserByNameKeyword(String nameKeyword) {
        SysUser entity = userDao.getByNameKeyword(nameKeyword);
        return MapstructUtils.convert(entity, SysUserVo.class);
    }

    /**
     * 通过用户ID查询用户(带角色信息)
     *
     * @param userId 用户ID
     * @return 用户对象信息
     */
    @Override
    public SysUserVo getUserWithRolesById(Long userId) {
        SysUser entity = userDao.getById(userId);
        if (ObjectUtil.isNull(entity)) {
            return null;
        }
        SysUserVo user = MapstructUtils.convert(entity, SysUserVo.class);
        user.setRoles(MapstructUtils.convert(roleDao.listRolesByUserId(user.getUserId()), SysRoleVo.class));
        return user;
    }

    /**
     * 通过用户ID串查询用户
     *
     * @param userIds 用户ID列表
     * @param deptId  部门ID
     * @return 用户列表信息
     */
    @Override
    public List<SysUserVo> listUsersByIdsAndDeptId(List<Long> userIds, Long deptId) {
        List<SysUser> users = userDao.listUsersByIdsAndDeptId(userIds, deptId);
        return MapstructUtils.convert(users, SysUserVo.class);
    }

    /**
     * 查询用户所属角色组
     *
     * @param userId 用户ID
     * @return 角色组字符串
     */
    @Override
    public String getUserRoleGroup(Long userId) {
        List<SysRole> list = roleDao.listRolesByUserId(userId);
        if (CollUtil.isEmpty(list)) {
            return StringUtils.EMPTY;
        }
        return StreamUtils.join(list, SysRole::getRoleName);
    }

    /**
     * 查询用户所属岗位组
     *
     * @param userId 用户ID
     * @return 岗位组字符串
     */
    @Override
    public String getUserPostGroup(Long userId) {
        List<SysPost> list = postDao.listPostsByUserId(userId);
        if (CollUtil.isEmpty(list)) {
            return StringUtils.EMPTY;
        }
        return StreamUtils.join(list, SysPost::getPostName);
    }

    /**
     * 通过部门ID查询当前部门所有用户
     *
     * @param deptId 部门ID
     * @return 用户信息集合
     */
    @Override
    public List<SysUserVo> listUsersByDeptId(Long deptId) {
        List<SysUser> users = userDao.listUsersByDeptId(deptId);
        return MapstructUtils.convert(users, SysUserVo.class);
    }


    /**
     * 更新用户昵称和头像
     *
     * @param userId   用户ID
     * @param nickName 用户昵称
     * @param avatar   头像地址
     */
    @Override
    public void updateUserNickNameAvatar(Long userId, String nickName, String avatar) {
        userDao.updateUserNickNameAvatar(userId, nickName, avatar);
    }

    // ================ 业务校验相关方法 =================

    /**
     * 判断用户名称是否唯一
     *
     * @param userName 用户名
     * @param userId   用户ID（可为null，新增时传null）
     * @return true表示唯一，false表示重复
     */
    @Override
    public boolean isUserNameUnique(String userName, Long userId) {
        return userDao.checkUserNameUnique(userName, userId);
    }

    /**
     * 判断手机号码是否唯一
     *
     * @param phone  手机号码
     * @param userId 用户ID（可为null，新增时传null）
     * @return true表示唯一，false表示重复
     */
    @Override
    public boolean isPhoneUnique(String phone, Long userId) {
        return userDao.checkPhoneUnique(phone, userId);
    }

    /**
     * 判断email是否唯一
     *
     * @param email  邮箱地址
     * @param userId 用户ID（可为null，新增时传null）
     * @return true表示唯一，false表示重复
     */
    @Override
    public boolean isEmailUnique(String email, Long userId) {
        return userDao.checkEmailUnique(email, userId);
    }

    /**
     * 校验用户是否允许操作
     *
     * @param userId 用户ID
     */
    @Override
    public void checkUserAllowed(Long userId) {
        if (ObjectUtil.isNotNull(userId) && LoginHelper.isSuperAdmin(userId)) {
            throw ServiceException.of("不允许操作超级管理员用户");
        }
    }

    /**
     * 校验用户是否有数据权限
     *
     * @param userId 用户ID
     */
    @Override
    public void checkUserDataScope(Long userId) {
        if (ObjectUtil.isNull(userId)) {
            return;
        }
        if (LoginHelper.isSuperAdmin()) {
            return;
        }
        if (userDao.countUserById(userId) == 0) {
            throw ServiceException.of("没有权限访问用户数据！");
        }
    }

    // ================ 用户增删改相关方法 =================

    /**
     * 新增保存用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long insertUser(SysUserBo user) {
        SysUser sysUser = MapstructUtils.convert(user, SysUser.class);
        if (StringUtils.isBlank(sysUser.getUserType())) {
            sysUser.setUserType(UserType.PC_USER.getUserType());
        }
        // 新增用户信息
        userDao.insert(sysUser);
        Long userId = sysUser.getUserId();
        user.setUserId(userId);
        // 新增用户岗位关联
        insertUserPosts(user, false);
        // 新增用户与角色管理
        insertUserRoles(user, false);
        return sysUser.getUserId();
    }

    /**
     * 注册用户信息
     *
     * @param user     用户信息
     * @param tenantId 租户ID
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean registerPcUser(SysUserBo user, String tenantId) {
        user.setCreateBy(0L);
        user.setUpdateBy(0L);
        SysUser sysUser = MapstructUtils.convert(user, SysUser.class);
        sysUser.setTenantId(tenantId);
        userDao.insert(sysUser);
        Long userId = sysUser.getUserId();
        if (userId != null) {
            user.setUserId(userId);
        }
        //查询pc端默认角色
        SysRole role = roleDao.getByRoleKey(UserType.PC_USER.getUserType());
        if (ObjectUtil.isNotNull(role)) {
            assignUserRoles(sysUser.getUserId(), List.of(role.getRoleId()));
        }
        return userId != null;
    }

    /**
     * 修改保存用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    @CacheEvict(cacheNames = CacheNames.SYS_NICKNAME, key = "#user.userId")
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(SysUserBo user) {
        // 判断是否修改了角色
        boolean roleChanged = ArrayUtil.isNotEmpty(user.getRoleIds());
        // 新增用户与角色管理
        insertUserRoles(user, true);
        // 新增用户与岗位管理
        insertUserPosts(user, true);
        SysUser sysUser = MapstructUtils.convert(user, SysUser.class);
        // 防止错误更新后导致的数据误删除
        boolean success = userDao.updateById(sysUser) > 0;
        if (!success) {
            throw ServiceException.of("修改用户{}信息失败", user.getUserName());
        }
        // 如果修改了角色，踢用户下线使其重新登录获取新权限
        if (roleChanged) {
            roleService.cleanOnlineUser(List.of(user.getUserId()));
        }
        return true;
    }

    /**
     * 分配用户角色
     *
     * @param userId  用户ID
     * @param roleIds 角色组
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignUserRoles(Long userId, List<Long> roleIds) {
        insertUserRoles(userId, roleIds, true);
        // 踢用户下线使其重新登录获取新权限
        roleService.cleanOnlineUser(List.of(userId));
    }

    /**
     * 修改用户状态
     *
     * @param userId 用户ID
     * @param status 帐号状态
     * @return 结果
     */
    @Override
    public boolean updateUserStatus(Long userId, String status) {
        return userDao.updateUserStatus(userId, status);
    }

    /**
     * 修改用户基本信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @CacheEvict(cacheNames = CacheNames.SYS_NICKNAME, key = "#user.userId")
    @Override
    public boolean updateUserProfile(SysUserBo user) {
        return userDao.updateUserProfile(user);
    }

    /**
     * 修改用户头像
     *
     * @param userId 用户ID
     * @param avatar 头像地址
     * @return 结果
     */
    @Override
    public boolean updateUserAvatar(Long userId, String avatar) {
        return userDao.updateUserAvatar(userId, avatar);
    }

    /**
     * 重置用户密码
     *
     * @param userId   用户ID
     * @param password 密码
     * @return 结果
     */
    @Override
    public boolean resetUserPwd(Long userId, String password) {
        return userDao.resetUserPwd(userId, password);
    }

    /**
     * 通过用户ID删除用户
     *
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUserById(Long userId) {
        // 删除用户与角色关联
        userRoleDao.deleteByUserId(userId);

        // 删除用户与岗位表
        userPostDao.deleteByUserId(userId);

        // 防止更新失败导致的数据删除
        boolean success = userDao.deleteById(userId);
        if (!success) {
            throw ServiceException.of("删除用户失败!");
        }
        return true;
    }

    /**
     * 批量删除用户信息
     *
     * @param userIds 需要删除的用户ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUserByIds(Long[] userIds) {
        for (Long userId : userIds) {
            checkUserAllowed(userId);
            checkUserDataScope(userId);
        }

        List<Long> ids = List.of(userIds);

        // 删除用户与角色关联
        userRoleDao.batchDeleteByUserIds(ids);

        // 删除用户与岗位表
        userPostDao.batchDeleteByUserIds(ids);

        // 防止更新失败导致的数据删除
        boolean success = userDao.deleteByIds(ids);
        if (!success) {
            throw ServiceException.of("删除用户失败!");
        }
        return true;
    }

    // ================ 缓存查询相关方法 =================

    /**
     * 通过用户ID查询用户账户
     *
     * @param userId 用户ID
     * @return 用户账户
     */
    @Cacheable(cacheNames = CacheNames.SYS_USER_NAME, key = "#userId")
    @Override
    public String getUserNameById(Long userId) {
        return userDao.getUserNameById(userId);
    }

    /**
     * 通过用户ID查询用户昵称
     *
     * @param userId 用户ID
     * @return 用户昵称
     */
    @Override
    @Cacheable(cacheNames = CacheNames.SYS_NICKNAME, key = "#userId")
    public String getNickNameById(Long userId) {
        return userDao.getNickNameById(userId);
    }

    /**
     * 通过用户ID查询用户昵称
     *
     * @param userId 用户ID
     * @return 用户昵称
     */
    @Override
    @Cacheable(cacheNames = CacheNames.SYS_AVATAR, key = "#userId")
    public String getAvatarById(Long userId) {
        return userDao.getAvatarById(userId);
    }

    /**
     * 通过用户ID串查询用户昵称
     *
     * @param userIds 用户ID串，多个用逗号隔开
     * @return 用户昵称串，逗号分隔
     */
    @Override
    public String getNickNamesByIds(String userIds) {
        List<String> list = new ArrayList<>();
        for (Long id : StringUtils.splitToList(userIds, Convert::toLong)) {
            String nickname = SpringUtils.getAopProxy(this).getNickNameById(id);
            if (StringUtils.isNotBlank(nickname)) {
                list.add(nickname);
            }
        }
        return StringUtils.joinComma(list);
    }


    @Override
    public String getAvatarsByIds(String userIds) {
        List<String> list = new ArrayList<>();
        for (Long id : StringUtils.splitToList(userIds, Convert::toLong)) {
            String avatar = SpringUtils.getAopProxy(this).getAvatarById(id);
            if (StringUtils.isNotBlank(avatar)) {
                list.add(avatar);
            }
        }
        return StringUtils.joinComma(list);
    }

    /**
     * 通过用户ID查询用户手机号
     *
     * @param userId 用户ID
     * @return 用户手机号
     */
    @Override
    public String getPhoneById(Long userId) {
        return userDao.getPhoneById(userId);
    }

    /**
     * 通过用户ID查询用户邮箱
     *
     * @param userId 用户ID
     * @return 用户邮箱
     */
    @Override
    public String getEmailById(Long userId) {
        return userDao.getEmailById(userId);
    }

    // ================ 批量查询相关方法 =================

    /**
     * 通过用户ID列表查询用户列表
     *
     * @param userIds 用户ID列表
     * @return 用户列表
     */
    @Override
    public List<UserDTO> listUsersByIds(List<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return List.of();
        }

        List<SysUser> list = userDao.listByIds(userIds);
        List<SysUserVo> voList = MapstructUtils.convert(list, SysUserVo.class);

        return BeanUtil.copyToList(voList, UserDTO.class);
    }

    /**
     * 通过角色ID查询用户ID
     *
     * @param roleIds 角色ID列表
     * @return 用户ID列表
     */
    @Override
    public List<Long> listUserIdsByRoleIds(List<Long> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return List.of();
        }

        List<SysUserRole> userRoles = userRoleDao.listByRoleIds(roleIds);
        return StreamUtils.toList(userRoles, SysUserRole::getUserId);
    }

    /**
     * 通过角色ID查询用户
     *
     * @param roleIds 角色ID列表
     * @return 用户列表
     */
    @Override
    public List<UserDTO> listUsersByRoleIds(List<Long> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return List.of();
        }

        // 通过角色ID获取用户角色信息
        List<SysUserRole> userRoles = userRoleDao.listByRoleIds(roleIds);

        // 获取用户ID列表
        Set<Long> userIds = StreamUtils.toSet(userRoles, SysUserRole::getUserId);

        return listUsersByIds(new ArrayList<>(userIds));
    }

    /**
     * 通过部门ID查询用户
     *
     * @param deptIds 部门ID列表
     * @return 用户列表
     */
    @Override
    public List<UserDTO> listUsersByDeptIds(List<Long> deptIds) {
        if (CollUtil.isEmpty(deptIds)) {
            return List.of();
        }

        List<SysUser> list = userDao.listByDeptIds(deptIds);
        List<SysUserVo> voList = MapstructUtils.convert(list, SysUserVo.class);

        return BeanUtil.copyToList(voList, UserDTO.class);
    }

    /**
     * 通过岗位ID查询用户
     *
     * @param postIds 岗位ID列表
     * @return 用户列表
     */
    @Override
    public List<UserDTO> listUsersByPostIds(List<Long> postIds) {
        if (CollUtil.isEmpty(postIds)) {
            return List.of();
        }

        // 通过岗位ID获取用户岗位信息
        List<SysUserPost> userPosts = userPostDao.listByPostIds(postIds);

        // 获取用户ID列表
        Set<Long> userIds = StreamUtils.toSet(userPosts, SysUserPost::getUserId);

        return listUsersByIds(new ArrayList<>(userIds));
    }

    // ================ 名称映射查询相关方法 =================

    /**
     * 根据用户ID列表查询用户名称映射关系
     *
     * @param userIds 用户ID列表
     * @return Map，其中key为用户ID，value为对应的用户名称
     */
    @Override
    public Map<Long, String> mapUserNames(List<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyMap();
        }

        return userDao.listUserNamesById(userIds)
            .stream()
            .collect(Collectors.toMap(SysUser::getUserId, SysUser::getNickName));
    }

    // ================ 私有辅助方法 =================

    /**
     * 插入用户角色关联关系
     *
     * @param user          用户对象
     * @param clearExisting 插入前是否清除已存在的关联数据
     */
    private void insertUserRoles(SysUserBo user, boolean clearExisting) {
        Long[] roleIds = user.getRoleIds();
        if (ArrayUtil.isNotEmpty(roleIds)) {
            this.insertUserRoles(user.getUserId(), List.of(roleIds), clearExisting);
        }
    }

    /**
     * 插入用户岗位关联关系
     *
     * @param user          用户对象
     * @param clearExisting 插入前是否清除已存在的关联数据
     */
    private void insertUserPosts(SysUserBo user, boolean clearExisting) {
        Long[] posts = user.getPostIds();
        if (ArrayUtil.isNotEmpty(posts)) {
            if (clearExisting) {
                // 删除用户与岗位关联
                userPostDao.deleteByUserId(user.getUserId());
            }

            // 新增用户与岗位管理
            List<SysUserPost> list = StreamUtils.toList(List.of(posts), postId -> {
                SysUserPost up = new SysUserPost();
                up.setUserId(user.getUserId());
                up.setPostId(postId);
                return up;
            });
            userPostDao.batchInsertUserPosts(list);
        }
    }

    /**
     * 插入用户角色关联关系
     *
     * @param userId        用户ID
     * @param roleIds       角色组
     * @param clearExisting 插入前是否清除已存在的关联数据
     */
    private void insertUserRoles(Long userId, List<Long> roleIds, boolean clearExisting) {
        // 过滤超级管理员角色（非超级管理员用户不能分配超管角色）
        if (!LoginHelper.isSuperAdmin(userId) && ArrayUtil.isNotEmpty(roleIds)) {
            roleIds = StreamUtils.filter(roleIds,
                roleId -> !roleId.equals(SystemConstants.SUPER_ADMIN_ID));
            // 移除超管角色后若无剩余角色，说明仅选了超管角色且不允许分配，显式报错
            if (roleIds.isEmpty()) {
                throw ServiceException.of("不允许为普通用户分配超级管理员角色，请至少选择一个其他角色");
            }
        }

        // 查询当前操作者有权限分配的所有角色
        PlusLambdaQuery<SysRole> wrapper = roleDao.buildQueryWrapper(new SysRoleBo());
        List<SysRole> permissionRoles = roleDao.list(wrapper);
        Set<Long> permissionRoleIds = StreamUtils.toSet(permissionRoles, SysRole::getRoleId);

        if (clearExisting) {
            // 只删除当前操作者有权限的角色关联，保留无权限的角色
            // 这样可以防止越权删除用户原有的角色
            if (!permissionRoleIds.isEmpty()) {
                userRoleDao.deleteByUserIdAndRoleIds(userId, permissionRoleIds);
            }
        }

        // 过滤出有权限分配的角色
        if (ArrayUtil.isNotEmpty(roleIds)) {
            List<Long> validRoleIds = roleIds.stream()
                .filter(permissionRoleIds::contains)
                .toList();

            if (validRoleIds.isEmpty()) {
                // 如果提交的角色都不在权限范围内，不做任何操作（保留原有角色）
                return;
            }

            // 新增用户与角色管理
            List<SysUserRole> list = StreamUtils.toList(validRoleIds, roleId -> {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                return ur;
            });
            userRoleDao.batchInsertUserRoles(list);
        }
    }
}
