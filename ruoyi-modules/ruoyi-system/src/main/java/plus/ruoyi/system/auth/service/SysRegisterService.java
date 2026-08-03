package plus.ruoyi.system.auth.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.digest.BCrypt;
import lombok.RequiredArgsConstructor;
import plus.ruoyi.common.core.constant.GlobalConstants;
import plus.ruoyi.common.core.constant.I18nKeys;
import plus.ruoyi.common.core.dict.DictEnableStatus;
import plus.ruoyi.common.core.dict.DictOperResult;
import plus.ruoyi.common.core.domain.model.RegisterBody;
import plus.ruoyi.common.core.enums.UserType;
import plus.ruoyi.common.core.exception.user.CaptchaException;
import plus.ruoyi.common.core.exception.user.CaptchaExpireException;
import plus.ruoyi.common.core.exception.user.UserException;
import plus.ruoyi.common.core.service.ConfigService;
import plus.ruoyi.common.core.utils.MapstructUtils;
import plus.ruoyi.common.core.utils.MessageUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.log.publisher.LoginLogPublisher;
import plus.ruoyi.common.mybatis.helper.DataPermissionHelper;
import plus.ruoyi.common.redis.utils.RedisUtils;
import plus.ruoyi.common.tenant.helper.TenantHelper;
import plus.ruoyi.common.web.config.properties.CaptchaProperties;
import plus.ruoyi.system.core.dao.ISysRoleDao;
import plus.ruoyi.system.core.dao.ISysUserDao;
import plus.ruoyi.system.core.domain.SysRole;
import plus.ruoyi.system.core.domain.SysUser;
import plus.ruoyi.system.core.domain.bo.SysUserBo;
import plus.ruoyi.system.core.domain.vo.RoleInviteVo;
import plus.ruoyi.system.core.domain.vo.SysUserVo;
import plus.ruoyi.system.core.service.ISysRoleInviteService;
import plus.ruoyi.system.core.service.ISysUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * 注册服务
 * 提供用户注册相关的业务逻辑，包括验证码校验、用户创建等
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Service
public class SysRegisterService {

    private final ISysUserService userService;
    private final ISysUserDao userDao;
    private final ConfigService configService;
    private final LoginLogPublisher loginLogPublisher;
    private final ISysRoleInviteService roleInviteService;
    private final ISysRoleDao roleDao;

    /**
     * 用户注册(支持邀请码)
     * 验证注册信息，创建用户账号
     *
     * @param registerBody 注册信息
     * @throws UserException 如果注册失败
     */
    @Transactional(rollbackFor = Exception.class)
    public void registerPcUser(RegisterBody registerBody) {
        // 提取注册信息
        String tenantId = TenantHelper.getTenantId();
        String userName = registerBody.getUserName();
        String password = registerBody.getPassword();
        // 校验用户类型是否存在
        String userType = UserType.getUserType(registerBody.getUserType()).getUserType();

        // 邀请码处理
        RoleInviteVo inviteInfo = null;
        if (StringUtils.isNotBlank(registerBody.getInviteCode())) {
            // 验证邀请码
            try {
                inviteInfo = roleInviteService.validateRoleInvite(registerBody.getInviteCode());
            } catch (Exception e) {
                throw UserException.of("邀请码无效: " + e.getMessage());
            }
        }

        // 验证码校验 - 如果启用了验证码功能
        boolean captchaEnabled = configService.getBooleanValue(
            CaptchaProperties.CAPTCHA_ENABLED_KEY, true);
        if (captchaEnabled) {
            validateCaptcha(tenantId, userName, registerBody.getCode(), registerBody.getUuid());
        }

        // 构建用户对象
        SysUserBo sysUser = new SysUserBo();
        sysUser.setUserName(userName);
        sysUser.setNickName(userName);
        sysUser.setPassword(BCrypt.hashpw(password));
        sysUser.setUserType(userType);

        // 如果有邀请码，设置部门和状态
        if (ObjectUtil.isNotNull(inviteInfo)) {
            sysUser.setDeptId(inviteInfo.getDeptId());
            // 根据是否需要审核设置用户状态 0-待审核 1-正常
            sysUser.setStatus(inviteInfo.getNeedApproval() ? DictEnableStatus.DISABLED.getValue() : DictEnableStatus.ENABLE.getValue());
        } else {
            // 普通注册默认为正常状态
            sysUser.setStatus(DictEnableStatus.ENABLE.getValue());
        }

        // 使用租户工具进行隔离，检查用户名是否已存在
        boolean exist = TenantHelper.dynamic(tenantId, () -> userDao.existsByUserName(sysUser.getUserName()));

        // 用户名已存在
        if (exist) {
            throw UserException.of(I18nKeys.User.REGISTER_ACCOUNT_EXISTS, userName);
        }

        // 调用用户服务创建用户
        boolean regFlag = DataPermissionHelper.ignore(() -> userService.registerPcUser(sysUser, tenantId));
        if (!regFlag) {
            throw UserException.of(I18nKeys.User.REGISTER_FAILED);
        }

        // 如果有邀请码，分配角色并更新邀请码使用次数
        if (ObjectUtil.isNotNull(inviteInfo)) {
            if (!tenantId.equals(inviteInfo.getTenantId())) {
                throw UserException.of("租户信息不匹配");
            }
            // 分配角色
            Long roleId = inviteInfo.getRoleId();
            DataPermissionHelper.ignore(() -> {
                userService.assignUserRoles(sysUser.getUserId(), List.of(roleId));
            });

            // 更新邀请码使用次数
            roleInviteService.useRoleInvite(registerBody.getInviteCode());
            // 记录通过邀请码注册的日志
            loginLogPublisher.publishLoginLog(userName, DictOperResult.SUCCESS.getValue(),
                MessageUtils.message(I18nKeys.User.REGISTER_SUCCESS) +
                    " (通过邀请码: " + inviteInfo.getRoleName() + " -> " + inviteInfo.getDeptName() + ")",
                tenantId, sysUser.getUserId());
        } else {
            // 记录注册日志
            loginLogPublisher.publishLoginLog(userName, DictOperResult.SUCCESS.getValue(),
                MessageUtils.message(I18nKeys.User.REGISTER_SUCCESS), tenantId, sysUser.getUserId());
        }
    }

    /**
     * 校验验证码
     * 从Redis中获取验证码并与用户提供的验证码进行比对
     *
     * @param tenantId 租户ID，用于记录日志
     * @param userName 用户名，用于记录日志
     * @param code     用户提供的验证码
     * @param uuid     验证码唯一标识，用于从Redis获取对应的验证码
     * @throws CaptchaException       如果验证码校验失败
     * @throws CaptchaExpireException 如果验证码不存在或已过期
     */
    public void validateCaptcha(String tenantId, String userName, String code, String uuid) {
        // 构建验证码在Redis中的键名
        String verifyKey = GlobalConstants.CAPTCHA_CODE_KEY + StringUtils.blankToDefault(uuid, "");
        // 从Redis获取存储的验证码
        String captcha = RedisUtils.getCacheObject(verifyKey);
        // 无论验证是否通过，都删除Redis中的验证码，确保验证码只能使用一次
        RedisUtils.deleteObject(verifyKey);

        // 验证码不存在或已过期
        if (captcha == null) {
            // 记录失败日志
            loginLogPublisher.publishLoginLog(userName, DictOperResult.FAIL.getValue(),
                MessageUtils.message(I18nKeys.VerifyCode.CAPTCHA_EXPIRED), tenantId);
            // 抛出验证码过期异常
            throw new CaptchaExpireException();
        }

        // 验证码不匹配（不区分大小写）
        if (!code.equalsIgnoreCase(captcha)) {
            // 记录失败日志
            loginLogPublisher.publishLoginLog(userName, DictOperResult.FAIL.getValue(),
                MessageUtils.message(I18nKeys.VerifyCode.CAPTCHA_INVALID), tenantId);
            // 抛出验证码错误异常
            throw new CaptchaException();
        }
    }

    /**
     * 移动端用户注册
     */
    @Transactional(rollbackFor = Exception.class)
    public SysUserVo registerAppUser(String userName, String password, String phone, String nickName, String avatar) {
        SysUser sysUser = new SysUser();
        sysUser.setTenantId(TenantHelper.getTenantId());
        sysUser.setUserType(UserType.APP_USER.getUserType());
        if (StringUtils.isNotBlank(userName)) {
            // 检查用户名是否唯一
            if (!userService.isUserNameUnique(userName, null)) {
                throw UserException.of(I18nKeys.User.REGISTER_ACCOUNT_EXISTS, userName);
            }
            sysUser.setUserName(userName);
        }
        // 检查密码是否为空
        if (StringUtils.isNotBlank(password)) {
            sysUser.setPassword(BCrypt.hashpw(password));
        }
        //暂不检测手机号
//        if(StringUtils.isNotBlank(phone)){
//            // 检查手机号码是否唯一
//            if (userService.isPhoneUnique(phone, null)) {
//                throw UserException.of(I18nKeys.User.REGISTER_ACCOUNT_EXISTS, phone);
//            }
//        }
        sysUser.setPhone(phone);
        sysUser.setNickName(nickName);
        sysUser.setAvatar(avatar);
        // 密码加密
        userDao.insert(sysUser);
        //查询移动端默认角色
        SysRole role = roleDao.getByRoleKey(UserType.APP_USER.getUserType());
        if (ObjectUtil.isNotNull(role)) {
            DataPermissionHelper.ignore(() -> {
                userService.assignUserRoles(sysUser.getUserId(), List.of(role.getRoleId()));
            });
        }
        // 记录注册日志
        loginLogPublisher.publishLoginLog(sysUser.getUserName(), DictOperResult.SUCCESS.getValue(),
            MessageUtils.message(I18nKeys.User.REGISTER_SUCCESS), sysUser.getTenantId(), sysUser.getUserId());
        // 返回新创建的用户信息
        return MapstructUtils.convert(sysUser, SysUserVo.class);
    }
}
