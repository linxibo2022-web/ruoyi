package plus.ruoyi.system.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.digest.BCrypt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.constant.GlobalConstants;
import plus.ruoyi.common.core.constant.I18nKeys;
import plus.ruoyi.common.core.dict.DictEnableStatus;
import plus.ruoyi.common.core.dict.DictOperResult;
import plus.ruoyi.common.core.domain.model.LoginUser;
import plus.ruoyi.common.core.domain.model.PasswordLoginBody;
import plus.ruoyi.common.core.enums.AuthType;
import plus.ruoyi.common.core.enums.UserType;
import plus.ruoyi.common.core.exception.user.CaptchaException;
import plus.ruoyi.common.core.exception.user.CaptchaExpireException;
import plus.ruoyi.common.core.exception.user.UserException;
import plus.ruoyi.common.core.service.ConfigService;
import plus.ruoyi.common.core.utils.MessageUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.core.utils.ValidatorUtils;
import plus.ruoyi.common.json.utils.JsonUtils;
import plus.ruoyi.common.log.publisher.LoginLogPublisher;
import plus.ruoyi.common.redis.utils.RedisUtils;
import plus.ruoyi.common.satoken.utils.LoginHelper;
import plus.ruoyi.common.tenant.helper.TenantHelper;
import plus.ruoyi.common.web.config.properties.CaptchaProperties;
import plus.ruoyi.system.auth.domain.vo.AuthTokenVo;
import plus.ruoyi.system.core.domain.vo.SysUserVo;
import plus.ruoyi.system.auth.service.IAuthStrategy;
import plus.ruoyi.system.auth.service.SysLoginService;
import plus.ruoyi.system.core.service.ISysUserService;
import org.springframework.stereotype.Service;

/**
 * 密码认证策略
 * 实现基于用户名密码的标准认证流程，包括验证码校验、密码验证和令牌生成
 *
 * @author 抓蛙师
 */
@Slf4j
@Service("password" + IAuthStrategy.BASE_NAME)
@RequiredArgsConstructor
public class PasswordAuthStrategy implements IAuthStrategy {

    private final ConfigService configService;
    private final SysLoginService loginService;
    private final ISysUserService userService;
    private final LoginLogPublisher loginLogPublisher;

    /**
     * 密码登录实现
     * 完成用户名密码认证的完整流程：解析请求、验证码校验、加载用户、验证密码、生成令牌
     *
     * @param body 登录请求体JSON字符串
     * @return 登录结果，包含访问令牌等信息
     */
    @Override
    public AuthTokenVo login(String body) {
        // 1. 解析请求体，将JSON转换为PasswordLoginBody对象
        PasswordLoginBody loginBody = JsonUtils.parseObject(body, PasswordLoginBody.class);
        // 使用验证框架校验必填字段
        ValidatorUtils.validate(loginBody);

        // 2. 提取认证信息
        String tenantId = TenantHelper.getTenantId();
        String userName = loginBody.getUserName();
        String password = loginBody.getPassword();

        // 设置用户类型 - 默认使用PC用户类型
        UserType userType = UserType.PC_USER;

        // 3. 验证码校验 - 只有在验证码功能启用时才校验
        boolean captchaEnabled = configService.getBooleanValue(
            CaptchaProperties.CAPTCHA_ENABLED_KEY, true);
        if (captchaEnabled) {
            validateImageCaptcha(tenantId, userName,
                loginBody.getCode(), loginBody.getUuid(), userType.getDeviceType());
        }

        // 4. 认证用户 - 使用租户工具进行隔离
        LoginUser loginUser = TenantHelper.dynamic(tenantId, () -> {
            // 4.1 根据用户名加载用户信息
            SysUserVo user = loadUserByUserName(userName);

            // 4.2 验证密码 - 使用loginService的checkLogin方法，该方法封装了密码错误次数限制逻辑
            // 传入的Supplier返回true表示密码校验失败
            loginService.checkLogin(AuthType.PASSWORD, tenantId, user.getUserId(), userName, userType.getDeviceType(),
                () -> !BCrypt.checkpw(password, user.getPassword()));

            // 4.3 创建登录用户对象 - 包括用户基本信息、权限信息等
            return loginService.createLoginUser(user);
        });

        // 5. 设置用户类型和生成令牌
        // 根据用户类型设置不同的会话超时时间
        loginUser.setDeviceType(userType.getDeviceType());

        // 构建登录模型，设置令牌参数
        SaLoginParameter loginParameter = new SaLoginParameter();
        loginParameter.setDeviceType(userType.getDeviceType());
        // 为不同用户类型设置不同的令牌超时时间
        loginParameter.setTimeout(userType.getTimeout());
        loginParameter.setActiveTimeout(userType.getActiveTimeout());

        // 调用LoginHelper生成令牌并完成登录
        LoginHelper.login(loginUser, loginParameter);

        // 6. 构建并返回登录结果
        AuthTokenVo authTokenVo = new AuthTokenVo();
        authTokenVo.setAccessToken(StpUtil.getTokenValue());
        authTokenVo.setExpireIn(StpUtil.getTokenTimeout());
        return authTokenVo;
    }

    /**
     * 校验图片验证码
     * 从Redis中获取验证码并与用户提供的验证码进行比对，校验失败则抛出异常
     *
     * @param tenantId   租户ID
     * @param userName   用户名
     * @param code       用户提供的验证码
     * @param uuid       验证码唯一标识
     * @param deviceType 设备类型
     */
    private void validateImageCaptcha(String tenantId, String userName, String code, String uuid, String deviceType) {
        // 构建验证码在Redis中的键名
        String verifyKey = GlobalConstants.CAPTCHA_CODE_KEY + StringUtils.blankToDefault(uuid, "");
        // 从Redis获取存储的验证码
        String captcha = RedisUtils.getCacheObject(verifyKey);
        // 无论验证是否通过，都删除Redis中的验证码，确保验证码只能使用一次
        RedisUtils.deleteObject(verifyKey);

        // 验证码不存在或已过期
        if (captcha == null) {
            // 记录登录失败日志
            loginLogPublisher.publishLoginLog(userName, DictOperResult.FAIL.getValue(),
                MessageUtils.message(I18nKeys.VerifyCode.CAPTCHA_EXPIRED), tenantId);
            // 抛出验证码过期异常
            throw new CaptchaExpireException();
        }

        // 验证码不匹配（不区分大小写）
        if (!code.equalsIgnoreCase(captcha)) {
            // 记录登录失败日志
            loginLogPublisher.publishLoginLog(userName, DictOperResult.FAIL.getValue(),
                MessageUtils.message(I18nKeys.VerifyCode.CAPTCHA_INVALID), tenantId);
            // 抛出验证码错误异常
            throw new CaptchaException();
        }
        // 验证通过则继续执行后续逻辑
    }

    /**
     * 根据用户名加载用户信息
     * 查询数据库获取对应用户名的用户，并校验用户状态
     *
     * @param userName 用户名
     * @return 用户信息对象
     * @throws UserException 如果用户不存在或被禁用
     */
    private SysUserVo loadUserByUserName(String userName) {
        // 构建查询条件 - 按用户名精确匹配
        SysUserVo user = userService.getUserByUserName(userName);

        // 用户不存在
        if (ObjectUtil.isNull(user)) {
            log.info("登录用户：{} 不存在.", userName);
            // 抛出用户不存在异常，使用国际化消息
            throw UserException.of(I18nKeys.User.ACCOUNT_NOT_EXISTS, userName);
        }
        // 用户被禁用
        else if (DictEnableStatus.DISABLED.getValue().equals(user.getStatus())) {
            log.info("登录用户：{} 已被停用.", userName);
            // 抛出用户被禁用异常，使用国际化消息
            throw UserException.of(I18nKeys.User.ACCOUNT_DISABLED, userName);
        }

        // 用户存在且状态正常，返回用户信息
        return user;
    }
}
