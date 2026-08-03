package plus.ruoyi.system.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.BCrypt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import plus.ruoyi.common.core.constant.I18nKeys;
import plus.ruoyi.common.core.dict.DictEnableStatus;
import plus.ruoyi.common.core.dict.DictOperResult;
import plus.ruoyi.common.core.domain.model.LoginUser;
import plus.ruoyi.common.core.domain.model.SocialLoginBody;
import plus.ruoyi.common.core.enums.UserType;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.exception.user.UserException;
import plus.ruoyi.common.core.service.ConfigService;
import plus.ruoyi.common.core.utils.MapstructUtils;
import plus.ruoyi.common.core.utils.StreamUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.core.utils.ValidatorUtils;
import plus.ruoyi.common.json.utils.JsonUtils;
import plus.ruoyi.common.log.publisher.LoginLogPublisher;
import plus.ruoyi.common.mybatis.helper.DataPermissionHelper;
import plus.ruoyi.common.satoken.utils.LoginHelper;
import plus.ruoyi.common.social.config.properties.SocialProperties;
import plus.ruoyi.common.social.utils.SocialUtils;
import plus.ruoyi.common.tenant.helper.TenantHelper;
import plus.ruoyi.system.auth.domain.vo.AuthTokenVo;
import plus.ruoyi.system.core.domain.bo.SysSocialBo;
import plus.ruoyi.system.core.domain.bo.SysUserBo;
import plus.ruoyi.system.core.domain.vo.RoleInviteVo;
import plus.ruoyi.system.core.domain.vo.SysSocialVo;
import plus.ruoyi.system.core.domain.vo.SysUserVo;
import plus.ruoyi.system.core.service.ISysRoleInviteService;
import plus.ruoyi.system.core.service.ISysSocialService;
import plus.ruoyi.system.core.service.ISysUserService;
import plus.ruoyi.system.auth.service.IAuthStrategy;
import plus.ruoyi.system.auth.service.SysLoginService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/**
 * 第三方授权策略
 * 实现基于第三方平台（如GitHub、Gitee等）的用户认证流程
 *
 * @author thiszhc is 三三
 */
@Slf4j
@Service("social" + IAuthStrategy.BASE_NAME)
@RequiredArgsConstructor
public class SocialAuthStrategy implements IAuthStrategy {

    private final SocialProperties socialProperties;
    private final ISysSocialService sysSocialService;
    private final ISysUserService userService;
    private final SysLoginService loginService;
    private final ConfigService configService;
    private final ISysRoleInviteService roleInviteService;
    private final LoginLogPublisher loginLogPublisher;

    /**
     * 第三方平台登录实现
     * 完成第三方认证的完整流程：解析请求、获取第三方用户信息、查询绑定关系、生成令牌
     *
     * @param body 登录请求体JSON字符串
     * @return 登录结果，包含访问令牌等信息
     */
    @Override
    public AuthTokenVo login(String body) {
        // 1. 解析请求体，将JSON转换为SocialLoginBody对象
        SocialLoginBody loginBody = JsonUtils.parseObject(body, SocialLoginBody.class);
        // 使用验证框架校验必填字段
        ValidatorUtils.validate(loginBody);

        //获取租户id
        String tenantId = TenantHelper.getTenantId();

        // 2. 获取第三方登录信息
        // 调用第三方授权工具获取用户信息
        AuthResponse<AuthUser> response = SocialUtils.loginAuth(
            loginBody.getSource(), loginBody.getSocialCode(),
            loginBody.getSocialState(), socialProperties);

        // 检查授权响应状态
        if (!response.ok()) {
            throw ServiceException.of(response.getMsg());
        }

        // 3. 处理第三方用户信息
        AuthUser authUserData = response.getData();

        // 4. 查询绑定关系
        List<SysSocialVo> list = sysSocialService.listSocialsByAuthId(authUserData.getSource() + authUserData.getUuid());

        // 5. 解析socialState获取邀请码(如果有)
        String inviteCode = null;
        try {
            String stateJson = Base64.decodeStr(loginBody.getSocialState(), StandardCharsets.UTF_8);
            Dict stateMap = JsonUtils.parseMap(stateJson);
            inviteCode = stateMap.get("inviteCode").toString();
        } catch (Exception e) {
            log.warn("解析socialState失败: {}", e.getMessage());
        }

        // 6. 处理未绑定用户
        SysSocialVo social;
        if (CollUtil.isEmpty(list)) {
            // 检查是否开启自动注册
            boolean autoRegisterEnabled = configService.getBooleanValue("system.social.auto-register-enabled", false);

            if (!autoRegisterEnabled) {
                // 未开启自动注册，提示需要先绑定
                throw ServiceException.of("你还没有绑定第三方账号，绑定后才可以登录！");
            }

            // 开启了自动注册，自动创建用户并绑定
            log.info("社交登录自动注册: source={}, uuid={}, inviteCode={}", authUserData.getSource(), authUserData.getUuid(), inviteCode);
            social = autoRegisterAndBind(authUserData, tenantId, inviteCode);
        } else {
            // 6. 已有绑定关系，处理租户
            if (TenantHelper.isEnable()) {
                // 查找与当前租户匹配的绑定记录
                Optional<SysSocialVo> opt = StreamUtils.findAny(list, x -> x.getTenantId().equals(tenantId));
                // 如果没有找到匹配的租户绑定，提示无权登录
                if (opt.isEmpty()) {
                    throw ServiceException.of("对不起，你没有权限登录当前租户！");
                }
                social = opt.get();
            } else {
                // 未启用租户功能，直接使用第一个绑定记录
                social = list.get(0);
            }
        }

        // 6. 构建登录用户
        LoginUser loginUser = TenantHelper.dynamic(social.getTenantId(), () -> {
            // 加载用户信息
            SysUserVo user = loadUser(social.getUserId());
            // 创建登录用户对象
            return loginService.createLoginUser(user);
        });

        // 设置用户类型和生成令牌
        UserType userType = UserType.PC_USER;
        loginUser.setDeviceType(userType.getDeviceType());

        // 构建登录模型，设置令牌参数
        SaLoginParameter loginParameter = new SaLoginParameter();
        loginParameter.setDeviceType(userType.getDeviceType());
        // 为不同用户类型设置不同的令牌超时时间
        loginParameter.setTimeout(userType.getTimeout());
        loginParameter.setActiveTimeout(userType.getActiveTimeout());

        // 调用LoginHelper生成令牌并完成登录
        LoginHelper.login(loginUser, loginParameter);

        // 构建并返回登录结果
        AuthTokenVo authTokenVo = new AuthTokenVo();
        authTokenVo.setAccessToken(StpUtil.getTokenValue());
        authTokenVo.setExpireIn(StpUtil.getTokenTimeout());
        return authTokenVo;
    }

    /**
     * 根据用户ID加载用户信息
     * 查询数据库获取指定ID的用户，并校验用户状态
     *
     * @param userId 用户ID
     * @return 用户信息对象
     * @throws UserException 如果用户不存在或被禁用
     */
    private SysUserVo loadUser(Long userId) {
        // 根据用户ID查询用户信息
        SysUserVo user = userService.getUserById(userId);

        // 用户不存在
        if (ObjectUtil.isNull(user)) {
            log.info("登录用户：{} 不存在.", "");
            // 抛出用户不存在异常，使用国际化消息
            throw UserException.of(I18nKeys.User.ACCOUNT_NOT_EXISTS, "");
        }
        // 用户被禁用
        else if (DictEnableStatus.DISABLED.getValue().equals(user.getStatus())) {
            log.info("登录用户：{} 已被停用.", "");
            // 抛出用户被禁用异常，使用国际化消息
            throw UserException.of(I18nKeys.User.ACCOUNT_DISABLED, "");
        }

        // 用户存在且状态正常，返回用户信息
        return user;
    }

    /**
     * 自动注册用户并绑定社交账号
     * 当开启自动注册开关时，第一次使用社交账号登录会自动创建PC用户并绑定
     *
     * @param authUser 第三方用户信息
     * @param tenantId 租户ID
     * @param inviteCode 邀请码(可选，用于绑定角色和部门)
     * @return 社交绑定信息
     */
    private SysSocialVo autoRegisterAndBind(AuthUser authUser, String tenantId, String inviteCode) {
        // 构建第三方用户唯一标识
        String authId = authUser.getSource() + authUser.getUuid();

        // 双重检查：防止并发情况下重复创建
        List<SysSocialVo> checkList = sysSocialService.listSocialsByAuthId(authId);
        if (CollUtil.isNotEmpty(checkList)) {
            // 已经被其他线程创建并绑定了，直接返回
            log.info("社交账号已被绑定，无需自动注册: authId={}", authId);
            return checkList.get(0);
        }

        // 1. 处理邀请码
        RoleInviteVo inviteInfo = null;
        if (StringUtils.isNotBlank(inviteCode)) {
            try {
                inviteInfo = roleInviteService.validateRoleInvite(inviteCode);
                // 验证租户ID是否匹配
                if (!tenantId.equals(inviteInfo.getTenantId())) {
                    throw ServiceException.of("邀请码租户信息不匹配");
                }
                log.info("社交登录使用邀请码: inviteCode={}, roleId={}, deptId={}",
                    inviteCode, inviteInfo.getRoleId(), inviteInfo.getDeptId());
            } catch (Exception e) {
                log.error("邀请码验证失败: inviteCode={}, error={}", inviteCode, e.getMessage());
                throw ServiceException.of("邀请码无效: " + e.getMessage());
            }
        }

        // 2. 生成用户名 - 使用第三方平台名称+随机数，确保唯一性
        String userName = authUser.getSource() + "_" + RandomUtil.randomString(8);

        // 3. 生成随机密码
        String randomPassword = RandomUtil.randomString(16);

        // 4. 提取用户信息
        String nickName = StringUtils.isNotBlank(authUser.getNickname())
            ? authUser.getNickname()
            : userName;
        String avatar = StringUtils.isNotBlank(authUser.getAvatar())
            ? authUser.getAvatar()
            : "";

        // 5. 构建用户对象，创建PC用户
        SysUserBo sysUserBo = new SysUserBo();
        sysUserBo.setUserName(userName);
        sysUserBo.setNickName(nickName);
        sysUserBo.setPassword(BCrypt.hashpw(randomPassword));
        sysUserBo.setUserType(UserType.PC_USER.getUserType());

        // 如果有邀请码，设置部门和状态
        if (ObjectUtil.isNotNull(inviteInfo)) {
            sysUserBo.setDeptId(inviteInfo.getDeptId());
            // 根据是否需要审核设置用户状态 0-待审核 1-正常
            sysUserBo.setStatus(inviteInfo.getNeedApproval() ? DictEnableStatus.DISABLED.getValue() : DictEnableStatus.ENABLE.getValue());
        } else {
            // 普通注册默认为正常状态
            sysUserBo.setStatus(DictEnableStatus.ENABLE.getValue());
        }

        // 6. 调用用户服务创建PC用户
        boolean regFlag = TenantHelper.dynamic(tenantId, () ->
            DataPermissionHelper.ignore(() -> userService.registerPcUser(sysUserBo, tenantId)));

        if (!regFlag) {
            throw ServiceException.of("社交登录自动注册失败");
        }

        log.info("社交登录自动注册成功: userId={}, userName={}, source={}, inviteCode={}",
            sysUserBo.getUserId(), userName, authUser.getSource(), inviteCode);

        // 7. 如果有邀请码，分配角色并更新邀请码使用次数
        if (ObjectUtil.isNotNull(inviteInfo)) {
            Long roleId = inviteInfo.getRoleId();
            TenantHelper.dynamic(tenantId, () -> {
                // 分配角色
                DataPermissionHelper.ignore(() -> {
                    userService.assignUserRoles(sysUserBo.getUserId(), List.of(roleId));
                });
                return null;
            });

            // 更新邀请码使用次数
            roleInviteService.useRoleInvite(inviteCode);

            // 记录通过邀请码注册的日志
            loginLogPublisher.publishLoginLog(userName, DictOperResult.SUCCESS.getValue(),
                "社交登录自动注册成功 (通过邀请码: " + inviteInfo.getRoleName() + " -> " + inviteInfo.getDeptName() + ")",
                tenantId, sysUserBo.getUserId());
        }

        // 8. 转换第三方用户信息为系统社交用户对象（复用bindSocialAccount的逻辑）
        SysSocialBo bo = BeanUtil.toBean(authUser, SysSocialBo.class);
        // 复制令牌信息
        BeanUtil.copyProperties(authUser.getToken(), bo);

        // 设置关联信息
        bo.setUserId(sysUserBo.getUserId());
        bo.setAuthId(authId);
        bo.setOpenId(authUser.getUuid());
        bo.setUserName(authUser.getUsername());
        bo.setNickName(nickName);

        // 9. 保存绑定关系
        TenantHelper.dynamic(tenantId, () -> {
            sysSocialService.add(bo);
            return null;
        });

        log.info("社交账号绑定成功: userId={}, authId={}", sysUserBo.getUserId(), authId);

        // 10. 返回绑定信息
        return MapstructUtils.convert(bo, SysSocialVo.class);
    }
}
