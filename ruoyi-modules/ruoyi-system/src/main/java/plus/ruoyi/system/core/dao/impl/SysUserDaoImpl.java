package plus.ruoyi.system.core.dao.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Repository;
import plus.ruoyi.common.core.constant.SystemConstants;
import plus.ruoyi.common.core.dict.DictBooleanFlag;
import plus.ruoyi.common.core.dict.DictEnableStatus;
import plus.ruoyi.common.core.utils.ObjectUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.mybatis.annotation.DataColumn;
import plus.ruoyi.common.mybatis.annotation.DataPermission;
import plus.ruoyi.common.mybatis.core.dao.impl.BaseDaoImpl;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.common.mybatis.core.query.PlusQuery;
import plus.ruoyi.system.core.dao.ISysUserDao;
import plus.ruoyi.system.core.domain.SysUser;
import plus.ruoyi.system.core.domain.bo.SysUserBo;
import plus.ruoyi.system.core.domain.vo.SysUserExportVo;
import plus.ruoyi.system.core.domain.vo.SysUserVo;
import plus.ruoyi.system.core.mapper.SysUserMapper;

import java.util.List;
import java.util.Map;

/**
 * 用户数据访问实现
 *
 * @author Lion Li
 */
@Repository
public class SysUserDaoImpl extends BaseDaoImpl<SysUserMapper, SysUser> implements ISysUserDao {

    /**
     * 创建PlusQuery查询构造器(支持表别名)
     */
    protected PlusQuery<SysUser> query() {
        return PlusQuery.of(SysUser.class);
    }

    /**
     * 构建查询条件
     */
    @Override
    public PlusLambdaQuery<SysUser> buildQueryWrapper(SysUserBo bo) {
        Map<String, Object> params = bo.getParams();
        PlusQuery<SysUser> wrapper = query();

        wrapper.eq("u.is_deleted", DictBooleanFlag.NO.getValue())
            .eq("u.user_id", bo.getUserId())
            .in("u.user_id", StringUtils.splitToList(bo.getUserIds(), Convert::toLong))
            .like("u.user_name", bo.getUserName())
            .eq("u.status", bo.getStatus())
            .like("u.phone", bo.getPhone())
            .between("u.create_time", params.get("beginTime"), params.get("endTime"))
            .between("u.login_date", params.get("beginLoginDate"), params.get("endLoginDate"))
            // 部门树查询（Service层已设置deptIds）
            .in("u.dept_id", bo.getDeptIds())
            .orderByAsc("u.user_id");

        if (StringUtils.isNotBlank(bo.getExcludeUserIds())) {
            wrapper.notIn("u.user_id", StringUtils.splitToList(bo.getExcludeUserIds(), Convert::toLong));
        }

        String searchValue = bo.getSearchValue();
        if (StringUtils.isNotBlank(searchValue)) {
            wrapper.and(w -> w.like("u.user_name", searchValue)
                .or().like("u.nick_name", searchValue)
                .or().like("u.phone", searchValue)
                .or().like("u.email", searchValue)
                .or().like("u.user_id", searchValue));
        }

        return wrapper.lambda();
    }

    /**
     * 分页查询用户列表(带数据权限)
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "u.dept_id"),
        @DataColumn(key = "userName", value = "u.user_id")
    })
    @Override
    public PageResult<SysUserVo> pageWithPermission(SysUserBo bo, IPage<SysUser> page) {
        PlusLambdaQuery<SysUser> wrapper = buildQueryWrapper(bo);
        Page<SysUserVo> pageVo = baseMapper.selectPageUserList((Page<SysUser>) page, wrapper);
        return PageResult.of(pageVo);
    }

    /**
     * 查询用户列表(带数据权限)
     * SQL使用别名: from sys_user u
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "u.dept_id"),
        @DataColumn(key = "userName", value = "u.user_id")
    })
    @Override
    public List<SysUserVo> listWithPermission(PlusLambdaQuery<SysUser> wrapper) {
        return baseMapper.selectUserList(wrapper);
    }

    /**
     * 导出用户列表(带数据权限)
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "d.dept_id"),
        @DataColumn(key = "userName", value = "u.user_id")
    })
    @Override
    public List<SysUserExportVo> listExportWithPermission(PlusLambdaQuery<SysUser> wrapper) {
        Page<SysUser> page = new Page<>(1, Integer.MAX_VALUE);
        Page<SysUserExportVo> exportVoPage = baseMapper.selectUserExports(page, wrapper);
        return exportVoPage.getRecords();
    }

    /**
     * 分页查询角色已授权用户列表(带数据权限)
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "d.dept_id"),
        @DataColumn(key = "userName", value = "u.user_id")
    })
    @Override
    public PageResult<SysUserVo> pageRoleAuthorizedUsers(SysUserBo bo, IPage<SysUser> page) {
        PlusQuery<SysUser> wrapper = query();
        wrapper.eq("u.is_deleted", DictBooleanFlag.NO.getValue())
            .ne("u.user_id", SystemConstants.SUPER_ADMIN_ID)
            .eq("r.role_id", bo.getRoleId())
            .like("u.user_name", bo.getUserName())
            .eq("u.status", bo.getStatus())
            .like("u.phone", bo.getPhone())
            .orderByAsc("u.user_id");

        Page<SysUserVo> pageVo = baseMapper.selectRoleAuthorizedUsers((Page<SysUser>) page, wrapper);
        return PageResult.of(pageVo);
    }

    /**
     * 分页查询角色未授权用户列表(带数据权限)
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "d.dept_id"),
        @DataColumn(key = "userName", value = "u.user_id")
    })
    @Override
    public PageResult<SysUserVo> pageRoleUnauthorizedUsers(SysUserBo bo, List<Long> excludeUserIds, IPage<SysUser> page) {
        PlusQuery<SysUser> wrapper = query();
        wrapper.eq("u.is_deleted", DictBooleanFlag.NO.getValue())
            .ne("u.user_id", SystemConstants.SUPER_ADMIN_ID)
            .eq("u.dept_id", bo.getDeptId())
            .and(w -> w.ne("r.role_id", bo.getRoleId()).or().isNull("r.role_id"))
            .notIn("u.user_id", excludeUserIds)
            .like("u.user_name", bo.getUserName())
            .like("u.phone", bo.getPhone())
            .orderByAsc("u.user_id");

        Page<SysUserVo> pageVo = baseMapper.selectRoleUnauthorizedUsers((Page<SysUser>) page, wrapper);
        return PageResult.of(pageVo);
    }

    /**
     * 统计用户数量(带数据权限)
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "dept_id"),
        @DataColumn(key = "userName", value = "user_id")
    })
    @Override
    public long countUserById(Long userId) {
        return baseMapper.countUserById(userId);
    }

    /**
     * 根据用户ID列表和部门ID查询用户列表
     */
    @Override
    public List<SysUser> listUsersByIdsAndDeptId(List<Long> userIds, Long deptId) {
        PlusLambdaQuery<SysUser> lqw = PlusLambdaQuery.of(SysUser.class)
            .select(SysUser::getUserId, SysUser::getUserName, SysUser::getNickName)
            .eq(SysUser::getStatus, plus.ruoyi.common.core.dict.DictEnableStatus.ENABLE.getValue())
            .eq(SysUser::getDeptId, deptId)
            .in(SysUser::getUserId, userIds);
        return list(lqw);
    }

    /**
     * 根据部门ID查询用户列表
     */
    @Override
    public List<SysUser> listUsersByDeptId(Long deptId) {
        PlusLambdaQuery<SysUser> lqw = PlusLambdaQuery.of(SysUser.class)
            .eq(SysUser::getDeptId, deptId)
            .orderByAsc(SysUser::getUserId);
        return list(lqw);
    }

    /**
     * 根据用户名查询用户
     */
    @Override
    public SysUser getByUserName(String userName) {
        PlusLambdaQuery<SysUser> lqw = PlusLambdaQuery.of(SysUser.class)
            .eq(SysUser::getUserName, userName);
        return getOne(lqw);
    }

    /**
     * 根据手机号查询用户
     */
    @Override
    public SysUser getByPhone(String phone) {
        PlusLambdaQuery<SysUser> lqw = PlusLambdaQuery.of(SysUser.class)
            .eq(SysUser::getPhone, phone);
        return getOne(lqw);
    }

    /**
     * 根据邮箱查询用户
     */
    @Override
    public SysUser getByEmail(String email) {
        PlusLambdaQuery<SysUser> lqw = PlusLambdaQuery.of(SysUser.class)
            .eq(SysUser::getEmail, email);
        return getOne(lqw);
    }

    /**
     * 根据ID查询用户
     */
    @Override
    public SysUser getById(Long userId) {
        return baseMapper.selectById(userId);
    }

    /**
     * 更新用户昵称和头像
     */
    @Override
    public boolean updateUserNickNameAvatar(Long userId, String nickName, String avatar) {
        return lambdaUpdate()
            .set(SysUser::getNickName, nickName)
            .set(SysUser::getAvatar, avatar)
            .eq(SysUser::getUserId, userId)
            .update() > 0;
    }

    /**
     * 校验用户名唯一性
     */
    @Override
    public boolean checkUserNameUnique(String userName, Long userId) {
        PlusLambdaQuery<SysUser> lqw = PlusLambdaQuery.of(SysUser.class)
            .eq(SysUser::getUserName, userName)
            .ne(SysUser::getUserId, userId);
        return count(lqw) == 0;
    }

    /**
     * 校验手机号唯一性
     */
    @Override
    public boolean checkPhoneUnique(String phone, Long userId) {
        PlusLambdaQuery<SysUser> lqw = PlusLambdaQuery.of(SysUser.class)
            .eq(SysUser::getPhone, phone)
            .ne(SysUser::getUserId, userId);
        return count(lqw) == 0;
    }

    /**
     * 校验邮箱唯一性
     */
    @Override
    public boolean checkEmailUnique(String email, Long userId) {
        PlusLambdaQuery<SysUser> lqw = PlusLambdaQuery.of(SysUser.class)
            .eq(SysUser::getEmail, email)
            .ne(SysUser::getUserId, userId);
        return count(lqw) == 0;
    }

    /**
     * 新增用户
     */
    @Override
    public Long add(SysUser user) {
        insert(user);
        return user.getUserId();
    }

    /**
     * 更新用户状态
     */
    @Override
    public boolean updateUserStatus(Long userId, String status) {
        return lambdaUpdate()
            .set(SysUser::getStatus, status)
            .eq(SysUser::getUserId, userId)
            .update() > 0;
    }

    /**
     * 更新用户基本信息
     */
    @Override
    public boolean updateUserProfile(SysUserBo user) {
        return lambdaUpdate()
            .set(ObjectUtil.isNotNull(user.getNickName()), SysUser::getNickName, user.getNickName())
            .set(ObjectUtil.isNotNull(user.getPhone()), SysUser::getPhone, user.getPhone())
            .set(ObjectUtil.isNotNull(user.getEmail()), SysUser::getEmail, user.getEmail())
            .set(ObjectUtil.isNotNull(user.getGender()), SysUser::getGender, user.getGender())
            .eq(SysUser::getUserId, user.getUserId())
            .update() > 0;
    }

    /**
     * 更新用户头像
     */
    @Override
    public boolean updateUserAvatar(Long userId, String avatar) {
        return lambdaUpdate()
            .set(SysUser::getAvatar, avatar)
            .eq(SysUser::getUserId, userId)
            .update() > 0;
    }

    /**
     * 重置用户密码
     */
    @Override
    public boolean resetUserPwd(Long userId, String password) {
        return lambdaUpdate()
            .set(SysUser::getPassword, password)
            .eq(SysUser::getUserId, userId)
            .update() > 0;
    }

    /**
     * 删除用户
     */
    @Override
    public boolean deleteById(Long userId) {
        return baseMapper.deleteById(userId) > 0;
    }

    /**
     * 批量删除用户
     */
    @Override
    public boolean deleteByIds(List<Long> userIds) {
        return baseMapper.deleteByIds(userIds) > 0;
    }

    /**
     * 根据用户ID查询用户名
     */
    @Override
    public String getUserNameById(Long userId) {
        SysUser sysUser = baseMapper.selectOne(PlusLambdaQuery.of(SysUser.class)
            .select(SysUser::getUserName)
            .eq(SysUser::getUserId, userId));
        return ObjectUtils.getIfNotNull(sysUser, SysUser::getUserName);
    }

    /**
     * 根据状态查询用户列表（带数据权限）
     * <p>
     * 默认带数据权限过滤，需要忽略权限时请使用 DataPermissionHelper.ignore() 包装调用
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "dept_id"),
        @DataColumn(key = "userName", value = "user_id")
    })
    @Override
    public List<SysUser> listByStatus(String status) {
        PlusLambdaQuery<SysUser> lqw = PlusLambdaQuery.of(SysUser.class)
            .eq(SysUser::getStatus, status);
        return baseMapper.selectList(lqw);
    }

    /**
     * 根据部门ID列表和状态查询用户列表（带数据权限）
     * <p>
     * 默认带数据权限过滤，需要忽略权限时请使用 DataPermissionHelper.ignore() 包装调用
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "dept_id"),
        @DataColumn(key = "userName", value = "user_id")
    })
    @Override
    public List<SysUser> listByDeptIdsAndStatus(List<Long> deptIds, String status) {
        PlusLambdaQuery<SysUser> lqw = PlusLambdaQuery.of(SysUser.class)
            .in(SysUser::getDeptId, deptIds)
            .eq(SysUser::getStatus, status);
        return baseMapper.selectList(lqw);
    }

    /**
     * 根据用户ID列表和状态查询用户列表（带数据权限）
     * <p>
     * 默认带数据权限过滤，需要忽略权限时请使用 DataPermissionHelper.ignore() 包装调用
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "dept_id"),
        @DataColumn(key = "userName", value = "user_id")
    })
    @Override
    public List<SysUser> listByUserIdsAndStatus(List<Long> userIds, String status) {
        PlusLambdaQuery<SysUser> lqw = PlusLambdaQuery.of(SysUser.class)
            .in(SysUser::getUserId, userIds)
            .eq(SysUser::getStatus, status);
        return baseMapper.selectList(lqw);
    }

    /**
     * 根据时间范围统计用户数量
     */
    @Override
    public long countByTimeRange(java.util.Date startTime, java.util.Date endTime) {
        PlusLambdaQuery<SysUser> lqw = PlusLambdaQuery.of(SysUser.class)
            .ge(startTime != null, SysUser::getCreateTime, startTime)
            .le(endTime != null, SysUser::getCreateTime, endTime);
        return count(lqw);
    }

    /**
     * 根据登录时间范围统计活跃用户数量
     */
    @Override
    public long countActiveByLoginDateRange(java.util.Date startTime, java.util.Date endTime) {
        PlusLambdaQuery<SysUser> lqw = PlusLambdaQuery.of(SysUser.class)
            .ge(SysUser::getLoginDate, startTime)
            .le(SysUser::getLoginDate, endTime);
        return count(lqw);
    }

    /**
     * 根据用户ID查询昵称
     */
    @Override
    public String getNickNameById(Long userId) {
        SysUser sysUser = baseMapper.selectOne(PlusLambdaQuery.of(SysUser.class)
            .select(SysUser::getNickName)
            .eq(SysUser::getUserId, userId));
        return ObjectUtils.getIfNotNull(sysUser, SysUser::getNickName);
    }

    /**
     * 根据用户ID查询头像
     */
    @Override
    public String getAvatarById(Long userId) {
        SysUser sysUser = baseMapper.selectOne(PlusLambdaQuery.of(SysUser.class)
            .select(SysUser::getAvatar)
            .eq(SysUser::getUserId, userId));
        return ObjectUtils.getIfNotNull(sysUser, SysUser::getAvatar);
    }

    /**
     * 根据用户ID查询手机号
     */
    @Override
    public String getPhoneById(Long userId) {
        SysUser sysUser = baseMapper.selectOne(PlusLambdaQuery.of(SysUser.class)
            .select(SysUser::getPhone)
            .eq(SysUser::getUserId, userId));
        return ObjectUtils.getIfNotNull(sysUser, SysUser::getPhone);
    }

    /**
     * 根据用户ID查询邮箱
     */
    @Override
    public String getEmailById(Long userId) {
        SysUser sysUser = baseMapper.selectOne(PlusLambdaQuery.of(SysUser.class)
            .select(SysUser::getEmail)
            .eq(SysUser::getUserId, userId));
        return ObjectUtils.getIfNotNull(sysUser, SysUser::getEmail);
    }

    /**
     * 根据用户ID列表查询用户列表
     */
    @Override
    public List<SysUser> listByIds(List<Long> userIds) {
        PlusLambdaQuery<SysUser> lqw = PlusLambdaQuery.of(SysUser.class)
            .select(SysUser::getUserId, SysUser::getUserName, SysUser::getNickName,
                SysUser::getEmail, SysUser::getPhone)
            .eq(SysUser::getStatus, DictEnableStatus.ENABLE.getValue())
            .in(SysUser::getUserId, userIds);
        return list(lqw);
    }

    /**
     * 根据部门ID列表查询用户列表
     */
    @Override
    public List<SysUser> listByDeptIds(List<Long> deptIds) {
        PlusLambdaQuery<SysUser> lqw = PlusLambdaQuery.of(SysUser.class)
            .select(SysUser::getUserId, SysUser::getUserName, SysUser::getNickName,
                SysUser::getEmail, SysUser::getPhone)
            .eq(SysUser::getStatus, DictEnableStatus.ENABLE.getValue())
            .in(SysUser::getDeptId, deptIds);
        return list(lqw);
    }

    /**
     * 根据用户ID列表查询用户名称映射
     */
    @Override
    public List<SysUser> listUserNamesById(List<Long> userIds) {
        PlusLambdaQuery<SysUser> lqw = PlusLambdaQuery.of(SysUser.class)
            .select(SysUser::getUserId, SysUser::getNickName)
            .in(SysUser::getUserId, userIds);
        return list(lqw);
    }

    /**
     * 根据用户名或昵称模糊查询用户
     */
    @Override
    public SysUser getByNameKeyword(String nameKeyword) {
        PlusLambdaQuery<SysUser> lqw = PlusLambdaQuery.of(SysUser.class)
            .and(w -> w.like(SysUser::getUserName, nameKeyword)
                .or().like(SysUser::getNickName, nameKeyword));
        return getOne(lqw, false);
    }

    /**
     * 检查部门下是否存在用户
     */
    @Override
    public boolean existsByDeptId(Long deptId) {
        PlusLambdaQuery<SysUser> lqw = PlusLambdaQuery.of(SysUser.class)
            .eq(SysUser::getDeptId, deptId);
        return exists(lqw);
    }

    /**
     * 检查用户名是否存在
     */
    @Override
    public boolean existsByUserName(String userName) {
        PlusLambdaQuery<SysUser> lqw = PlusLambdaQuery.of(SysUser.class)
            .eq(SysUser::getUserName, userName);
        return exists(lqw);
    }

}
