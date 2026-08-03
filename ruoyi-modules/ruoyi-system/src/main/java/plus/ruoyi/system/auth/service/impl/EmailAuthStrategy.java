package plus.ruoyi.system.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.constant.GlobalConstants;
import plus.ruoyi.common.core.constant.I18nKeys;
import plus.ruoyi.common.core.dict.DictEnableStatus;
import plus.ruoyi.common.core.dict.DictOperResult;
import plus.ruoyi.common.core.domain.model.EmailLoginBody;
import plus.ruoyi.common.core.domain.model.LoginUser;
import plus.ruoyi.common.core.enums.AuthType;
import plus.ruoyi.common.core.enums.UserType;
import plus.ruoyi.common.core.exception.user.CaptchaExpireException;
import plus.ruoyi.common.core.exception.user.UserException;
import plus.ruoyi.common.core.utils.MessageUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.core.utils.ValidatorUtils;
import plus.ruoyi.common.json.utils.JsonUtils;
import plus.ruoyi.common.log.publisher.LoginLogPublisher;
import plus.ruoyi.common.redis.utils.RedisUtils;
import plus.ruoyi.common.satoken.utils.LoginHelper;
import plus.ruoyi.common.tenant.helper.TenantHelper;
import plus.ruoyi.system.auth.domain.vo.AuthTokenVo;
import plus.ruoyi.system.core.domain.vo.SysUserVo;
import plus.ruoyi.system.auth.service.IAuthStrategy;
import plus.ruoyi.system.auth.service.SysLoginService;
import plus.ruoyi.system.core.service.ISysUserService;
import org.springframework.stereotype.Service;

/**
 * 邮件认证策略
 * 实现基于邮箱验证码的用户认证流程，适用于邮箱登录场景
 *
 * @author Michelle.Chung
 */
@Slf4j
@Service("email" + IAuthStrategy.BASE_NAME)
@RequiredArgsConstructor
public class EmailAuthStrategy implements IAuthStrategy {

    private final SysLoginService loginService;
    private final ISysUserService userService;
    private final LoginLogPublisher loginLogPublisher;

    /**
     * 邮箱验证码登录实现
     * 完成邮箱+验证码认证的完整流程：解析请求、加载用户、验证邮箱码、生成令牌
     *
     * @param body 登录请求体JSON字符串
     * @return 登录结果，包含访问令牌等信息
     */
    @Override
    public AuthTokenVo login(String body) {
        // 解析登录请求体，将JSON转换为EmailLoginBody对象
        EmailLoginBody loginBody = JsonUtils.parseObject(body, EmailLoginBody.class);
        // 使用验证框架校验必填字段
        ValidatorUtils.validate(loginBody);

        // 提取认证信息
        String tenantId = TenantHelper.getTenantId();
        String email = loginBody.getEmail();
        String emailCode = loginBody.getEmailCode();

        // 设置用户类型 - 默认使用PC用户类型
        UserType userType = UserType.PC_USER;

        // 认证用户 - 使用租户工具进行隔离
        LoginUser loginUser = TenantHelper.dynamic(tenantId, () -> {
            // 1. 根据邮箱加载用户信息
            SysUserVo user = loadUserByEmail(email);

            // 3. 验证邮箱验证码 - 使用loginService的checkLogin方法，封装了验证失败次数限制逻辑
            // lambda表达式返回true表示验证失败
            loginService.checkLogin(AuthType.EMAIL, tenantId, user.getUserId(), user.getUserName(), userType.getDeviceType(),
                () -> !(validateEmailCode(tenantId, user.getUserId(), email, emailCode, userType.getDeviceType())));

            // 4. 创建登录用户对象 - 包括用户基本信息、权限信息等
            return loginService.createLoginUser(user);
        });

        // 设置用户类型和生成令牌
        loginUser.setDeviceType(userType.getDeviceType());

        // 构建登录模型，设置令牌参数
        SaLoginParameter loginParameter = new SaLoginParameter();
        loginParameter.setDeviceType(userType.getDeviceType());
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
     * 校验邮箱验证码
     * 从Redis中获取验证码并与用户提供的验证码进行比对
     *
     * @param tenantId   租户ID
     * @param userId     用户ID
     * @param email      邮箱地址
     * @param emailCode  用户提供的验证码
     * @param deviceType 设备类型
     * @return 验证结果，true表示验证通过
     * @throws CaptchaExpireException 如果验证码不存在或已过期
     */
    private boolean validateEmailCode(String tenantId, Long userId, String email, String emailCode, String deviceType) {
        // 构建验证码在Redis中的键名
        String code = RedisUtils.getCacheObject(GlobalConstants.CAPTCHA_CODE_KEY + email);

        // 验证码不存在或已过期
        if (StringUtils.isBlank(code)) {
            // 记录登录失败日志
            loginLogPublisher.publishLoginLog(email, DictOperResult.FAIL.getValue(),
                MessageUtils.message(I18nKeys.VerifyCode.CAPTCHA_EXPIRED), tenantId, userId, deviceType);
            // 抛出验证码过期异常
            throw new CaptchaExpireException();
        }

        // 返回验证结果 - 验证码完全匹配才算通过
        return code.equals(emailCode);
    }

    /**
     * 根据邮箱加载用户信息
     * 查询数据库获取对应邮箱的用户，并校验用户状态
     *
     * @param email 邮箱地址
     * @return 用户信息对象
     * @throws UserException 如果用户不存在或被禁用
     */
    private SysUserVo loadUserByEmail(String email) {
        // 构建查询条件 - 按邮箱精确匹配
        SysUserVo user = userService.getUserByEmail(email);

        // 用户不存在
        if (ObjectUtil.isNull(user)) {
            log.info("登录用户：{} 不存在.", email);
            // 抛出用户不存在异常，使用国际化消息
            throw UserException.of(I18nKeys.User.ACCOUNT_NOT_EXISTS, email);
        }
        // 用户被禁用
        else if (DictEnableStatus.DISABLED.getValue().equals(user.getStatus())) {
            log.info("登录用户：{} 已被停用.", email);
            // 抛出用户被禁用异常，使用国际化消息
            throw UserException.of(I18nKeys.User.ACCOUNT_DISABLED, email);
        }

        // 用户存在且状态正常，返回用户信息
        return user;
    }
}
