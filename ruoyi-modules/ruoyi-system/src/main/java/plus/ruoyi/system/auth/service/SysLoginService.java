package plus.ruoyi.system.auth.service;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.lock.annotation.Lock4j;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.model.AuthUser;
import plus.ruoyi.common.core.constant.CacheConstants;
import plus.ruoyi.common.core.constant.I18nKeys;
import plus.ruoyi.common.core.constant.TenantConstants;
import plus.ruoyi.common.core.dict.DictEnableStatus;
import plus.ruoyi.common.core.dict.DictOperResult;
import plus.ruoyi.common.core.domain.dto.PostDTO;
import plus.ruoyi.common.core.domain.dto.RoleDTO;
import plus.ruoyi.common.core.domain.model.LoginUser;
import plus.ruoyi.common.core.enums.AuthType;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.exception.user.UserException;
import plus.ruoyi.common.core.utils.DateUtils;
import plus.ruoyi.common.core.utils.MessageUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.log.publisher.LoginLogPublisher;
import plus.ruoyi.common.mybatis.helper.DataPermissionHelper;
import plus.ruoyi.common.redis.utils.RedisUtils;
import plus.ruoyi.common.satoken.utils.LoginHelper;
import plus.ruoyi.common.tenant.exception.TenantException;
import plus.ruoyi.common.tenant.helper.TenantHelper;
import plus.ruoyi.system.core.domain.SysUser;
import plus.ruoyi.system.core.domain.bo.SysSocialBo;
import plus.ruoyi.system.core.domain.vo.*;
import plus.ruoyi.system.core.mapper.SysUserMapper;
import plus.ruoyi.system.core.service.*;
import plus.ruoyi.system.tenant.domain.vo.SysTenantVo;
import plus.ruoyi.system.tenant.service.ISysTenantService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

/**
 * 登录服务
 * 提供用户登录相关的核心业务逻辑，包括用户验证、登录记录、令牌管理等
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class SysLoginService {

    /**
     * 最大密码重试次数
     */
    @Value("${user.password.maxRetryCount}")
    private Integer maxRetryCount;

    /**
     * 账户锁定时间（分钟）
     */
    @Value("${user.password.lockTime}")
    private Integer lockTime;

    private final ISysTenantService tenantService;
    private final ISysPermissionService permissionService;
    private final ISysSocialService sysSocialService;
    private final ISysRoleService roleService;
    private final ISysDeptService deptService;
    private final ISysPostService postService;
    private final SysUserMapper userMapper;
    private final LoginLogPublisher loginLogPublisher;

    /**
     * 绑定第三方用户
     * 将第三方平台的用户信息与系统用户绑定
     *
     * @param authUserData 第三方授权用户数据
     */
    @Lock4j(keys = {"#authUserData.source", "#authUserData.uuid"}, expire = 10000)
    public void bindSocialAccount(AuthUser authUserData) {
        // 构建第三方用户唯一标识 - 平台类型 + 平台用户ID
        String authId = authUserData.getSource() + authUserData.getUuid();

        // 转换第三方用户信息为系统社交用户对象
        SysSocialBo bo = BeanUtil.toBean(authUserData, SysSocialBo.class);
        // 复制令牌信息
        BeanUtil.copyProperties(authUserData.getToken(), bo);

        // 设置关联信息
        Long userId = LoginHelper.getUserId();
        bo.setUserId(userId);
        bo.setAuthId(authId);
        bo.setOpenId(authUserData.getUuid());
        bo.setUserName(authUserData.getUsername());
        bo.setNickName(authUserData.getNickname());

        // 检查该第三方账号是否已被其他用户绑定
        List<SysSocialVo> checkList = sysSocialService.listSocialsByAuthId(authId);
        if (CollUtil.isNotEmpty(checkList)) {
            throw ServiceException.of("此三方账号已经被绑定!");
        }

        // 查询当前用户是否已绑定同平台的其他账号
        SysSocialBo params = new SysSocialBo();
        params.setUserId(userId);
        params.setSource(bo.getSource());
        List<SysSocialVo> list = sysSocialService.list(params);

        if (CollUtil.isEmpty(list)) {
            // 没有绑定用户, 新增用户信息
            sysSocialService.add(bo);
        } else {
            // 更新用户信息
            bo.setId(list.get(0).getId());
            sysSocialService.update(bo);
            // 如果要绑定的平台账号已经被绑定过了 是否抛异常自行决断
            // throw new ServiceException("此平台账号已经被绑定!");
        }
    }


    /**
     * 退出登录
     * 清除用户登录状态，记录登出日志
     */
    public void logout() {
        try {
            // 获取当前登录用户
            LoginUser loginUser = LoginHelper.getLoginUser();
            if (ObjectUtil.isNull(loginUser)) {
                return;
            }

            // 如果是超级管理员且启用了租户功能，清除动态租户
            if (TenantHelper.isEnable() && LoginHelper.isSuperAdmin()) {
                // 超级管理员登出清除动态租户
                TenantHelper.clearDynamic();
            }

            // 记录登出日志
            loginLogPublisher.publishLoginLog(loginUser.getUserName(), DictOperResult.SUCCESS.getValue(),
                MessageUtils.message(I18nKeys.User.LOGOUT_SUCCESS), loginUser.getTenantId(), loginUser.getUserId(), loginUser.getDeviceType());
        } catch (NotLoginException ignored) {
            // 已经退出登录的情况，忽略异常
        } finally {
            try {
                // 调用SaToken清除会话
                StpUtil.logout();
            } catch (NotLoginException ignored) {
                // 已经退出登录的情况，忽略异常
            }
        }
    }

    /**
     * 创建登录用户对象
     * 组装用户基本信息、权限信息、角色信息等
     *
     * @param user 用户基本信息
     * @return 完整的登录用户对象
     */
    public LoginUser createLoginUser(SysUserVo user) {
        // 创建登录用户对象
        LoginUser loginUser = new LoginUser();
        Long userId = user.getUserId();

        // 设置基本信息
        loginUser.setTenantId(user.getTenantId());
        loginUser.setUserId(userId);
        loginUser.setDeptId(user.getDeptId());
        loginUser.setUserName(user.getUserName());
        loginUser.setNickName(user.getNickName());
        loginUser.setUserType(user.getUserType());

        // 设置权限信息
        loginUser.setMenuPermission(permissionService.listMenuPermissions(userId));
        loginUser.setRolePermission(permissionService.listRolePermissions(userId));

        // 设置部门信息
        if (ObjectUtil.isNotNull(user.getDeptId())) {
            Opt<SysDeptVo> deptOpt = Opt.of(user.getDeptId()).map(deptService::get);
            loginUser.setDeptName(deptOpt.map(SysDeptVo::getDeptName).orElse(StringUtils.EMPTY));
            loginUser.setDeptCategory(deptOpt.map(SysDeptVo::getDeptCategory).orElse(StringUtils.EMPTY));
        }

        // 设置角色和岗位信息
        List<SysRoleVo> roles = roleService.listRolesByUserId(userId);
        List<SysPostVo> posts = postService.listPostsByUserId(userId);
        loginUser.setRoles(BeanUtil.copyToList(roles, RoleDTO.class));
        loginUser.setPosts(BeanUtil.copyToList(posts, PostDTO.class));

        return loginUser;
    }

    /**
     * 更新用户登录信息
     * 记录最后登录IP和时间
     *
     * @param userId 用户ID
     * @param ip     登录IP
     */
    public void updateUserLoginInfo(Long userId, String ip) {
        // 创建用户对象并设置更新信息
        SysUser sysUser = new SysUser();
        sysUser.setUserId(userId);
        sysUser.setLoginIp(ip);
        sysUser.setLoginDate(DateUtils.getNowDate());
        sysUser.setUpdateBy(userId);

        // 忽略数据权限限制，更新用户登录信息
        DataPermissionHelper.ignore(() -> userMapper.updateById(sysUser));
    }

    /**
     * 登录校验
     * 验证用户登录信息，处理登录错误次数和账户锁定
     *
     * @param authType   认证类型
     * @param tenantId   租户ID
     * @param userId     用户ID
     * @param userName   用户名
     * @param deviceType 设备类型
     * @param supplier   校验逻辑供应商，返回true表示校验失败
     * @throws UserException 如果验证失败或账户被锁定
     */
    public void checkLogin(AuthType authType, String tenantId, Long userId, String userName, String deviceType, Supplier<Boolean> supplier) {
        // 构建Redis中的错误计数键（使用用户名，便于解锁时通过用户名查找）
        String errorKey = CacheConstants.PWD_ERR_CNT_KEY + userName;

        // 获取用户登录错误次数，默认为0
        int errorNumber = ObjectUtil.defaultIfNull(RedisUtils.getCacheObject(errorKey), 0);

        // 检查是否已经超过最大重试次数
        if (errorNumber >= maxRetryCount) {
            // 已锁定，记录日志并抛出异常
            loginLogPublisher.publishLoginLog(userName, DictOperResult.FAIL.getValue(),
                MessageUtils.message(authType.getRetryLimitExceed(), maxRetryCount, lockTime), tenantId, userId, deviceType);
            throw UserException.of(authType.getRetryLimitExceed(), maxRetryCount, lockTime);
        }

        // 执行认证逻辑
        if (supplier.get()) {
            // 验证失败，错误次数递增
            errorNumber++;
            // 更新Redis中的错误计数，设置锁定时间
            RedisUtils.setCacheObject(errorKey, errorNumber, Duration.ofMinutes(lockTime));

            // 检查是否达到最大重试次数
            if (errorNumber >= maxRetryCount) {
                // 达到锁定阈值，记录日志并抛出异常
                loginLogPublisher.publishLoginLog(userName, DictOperResult.FAIL.getValue(),
                    MessageUtils.message(authType.getRetryLimitExceed(), maxRetryCount, lockTime), tenantId, userId, deviceType);
                throw UserException.of(authType.getRetryLimitExceed(), maxRetryCount, lockTime);
            } else {
                // 未达到锁定阈值，提示剩余次数
                loginLogPublisher.publishLoginLog(userName, DictOperResult.FAIL.getValue(),
                    MessageUtils.message(authType.getRetryLimitCount(), errorNumber), tenantId, userId, deviceType);
                throw UserException.of(authType.getRetryLimitCount(), errorNumber);
            }
        }

        // 认证成功，清除错误计数
        RedisUtils.deleteObject(errorKey);
    }

    /**
     * 校验租户
     * 验证租户是否存在、是否启用、是否过期
     *
     * @param tenantId 租户ID
     * @throws TenantException 如果租户校验失败
     */
    public void checkTenant(String tenantId) {
        // 如果租户功能未启用，直接返回
        if (!TenantHelper.isEnable()) {
            return;
        }

        // 验证租户ID不能为空
        if (StringUtils.isBlank(tenantId)) {
            throw new TenantException(I18nKeys.Tenant.ID_REQUIRED);
        }
        if (TenantConstants.DEFAULT_TENANT_ID.equals(tenantId)) {
            return;
        }

        // 查询租户信息
        SysTenantVo tenant = tenantService.getTenantByTenantId(tenantId);

        // 租户不存在
        if (ObjectUtil.isNull(tenant)) {
            log.info("登录租户：{} 不存在.", tenantId);
            throw new TenantException(I18nKeys.Tenant.NOT_EXISTS);
        }
        // 租户被停用
        else if (DictEnableStatus.DISABLED.getValue().equals(tenant.getStatus())) {
            log.info("登录租户：{} 已被停用.", tenantId);
            throw new TenantException(I18nKeys.Tenant.DISABLED);
        }
        // 租户已过期
        else if (ObjectUtil.isNotNull(tenant.getExpireTime())
            && new Date().after(tenant.getExpireTime())) {
            log.info("登录租户：{} 已超过有效期.", tenantId);
            throw new TenantException(I18nKeys.Tenant.EXPIRED);
        }
    }
}
