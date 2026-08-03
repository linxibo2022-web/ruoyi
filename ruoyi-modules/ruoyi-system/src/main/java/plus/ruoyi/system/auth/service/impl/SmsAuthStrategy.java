package plus.ruoyi.system.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.constant.GlobalConstants;
import plus.ruoyi.common.core.constant.I18nKeys;
import plus.ruoyi.common.core.dict.DictEnableStatus;
import plus.ruoyi.common.core.dict.DictOperResult;
import plus.ruoyi.common.core.domain.model.LoginUser;
import plus.ruoyi.common.core.domain.model.SmsLoginBody;
import plus.ruoyi.common.core.enums.UserType;
import plus.ruoyi.common.core.exception.user.CaptchaException;
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
import plus.ruoyi.system.auth.service.SysRegisterService;
import plus.ruoyi.system.core.service.ISysUserService;
import org.springframework.stereotype.Service;

/**
 * 短信认证策略
 * 实现基于短信验证码的用户认证流程，适用于手机号登录场景
 * 支持自动注册：当用户不存在时自动创建新账户
 *
 * @author Michelle.Chung
 */
@Slf4j
@Service("sms" + IAuthStrategy.BASE_NAME)
@RequiredArgsConstructor
public class SmsAuthStrategy implements IAuthStrategy {

    private final SysLoginService loginService;
    private final ISysUserService userService;
    private final LoginLogPublisher loginLogPublisher;
    private final SysRegisterService registerService;

    /**
     * 短信验证码登录实现
     * 完成手机号+验证码认证的完整流程：解析请求、验证短信码、加载用户、生成令牌
     * 如果用户不存在，将自动创建新账户
     *
     * @param body 登录请求体JSON字符串
     * @return 登录结果，包含访问令牌等信息
     */
    @Override
    public AuthTokenVo login(String body) {
        // 1. 解析登录请求体，将JSON转换为SmsLoginBody对象
        SmsLoginBody loginBody = JsonUtils.parseObject(body, SmsLoginBody.class);
        // 使用验证框架校验必填字段
        ValidatorUtils.validate(loginBody);

        // 2. 提取认证信息
        String tenantId = TenantHelper.getTenantId();
        String phone = loginBody.getPhone();
        String smsCode = loginBody.getSmsCode();

        // 设置用户类型 - 默认使用PC用户类型
        UserType userType = UserType.PC_USER;

        // 3. 先验证短信验证码，确保验证码正确后再进行用户操作
        validateSmsCode(tenantId, phone, smsCode, userType.getDeviceType());

        // 4. 认证用户 - 使用租户工具进行隔离
        LoginUser loginUser = TenantHelper.dynamic(tenantId, () -> {
            // 4.1 根据手机号加载用户信息，如果不存在则自动注册
            SysUserVo user = loadUserByPhone(phone);

            // 4.2 由于已经验证过短信验证码，这里直接创建登录用户对象
            return loginService.createLoginUser(user);
        });

        // 5. 设置用户类型和生成令牌
        loginUser.setDeviceType(userType.getDeviceType());

        // 构建登录模型，设置令牌参数
        SaLoginParameter loginParameter = new SaLoginParameter();
        loginParameter.setDeviceType(userType.getDeviceType());
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
     * 先验证短信验证码
     * 在进行用户操作之前，先验证短信验证码的有效性
     * 这样可以避免恶意用户通过无效验证码触发自动注册
     *
     * @param tenantId   租户ID
     * @param phone      手机号
     * @param smsCode    用户提供的验证码
     * @param deviceType 设备类型
     * @throws CaptchaExpireException 如果验证码过期或不存在
     * @throws CaptchaException       如果验证码错误
     */
    private void validateSmsCode(String tenantId,  String phone, String smsCode, String deviceType) {
        // 构建验证码在Redis中的键名
        String code = RedisUtils.getCacheObject(GlobalConstants.CAPTCHA_CODE_KEY + phone);

        // 验证码不存在或已过期
        if (StringUtils.isBlank(code)) {
            // 记录登录失败日志
            loginLogPublisher.publishLoginLog(phone, DictOperResult.FAIL.getValue(),
                MessageUtils.message(I18nKeys.VerifyCode.CAPTCHA_EXPIRED), tenantId, null, deviceType);
            // 抛出验证码过期异常
            throw new CaptchaExpireException();
        }

        // 验证码不匹配
        if (!code.equals(smsCode)) {
            // 记录登录失败日志（此时还没有用户ID，传null）
            loginLogPublisher.publishLoginLog(phone, DictOperResult.FAIL.getValue(),
                MessageUtils.message(I18nKeys.VerifyCode.CAPTCHA_INVALID), tenantId, null, deviceType);
            // 删除Redis中的验证码，防止重复尝试
            RedisUtils.deleteObject(GlobalConstants.CAPTCHA_CODE_KEY + phone);
            // 抛出验证码错误异常
            throw new CaptchaException();
        }

        // 验证通过，删除Redis中的验证码，确保验证码只能使用一次
        RedisUtils.deleteObject(GlobalConstants.CAPTCHA_CODE_KEY + phone);
    }

    /**
     * 根据手机号加载用户信息
     * 查询数据库获取对应手机号的用户，如果用户不存在则自动创建新账户
     * 如果用户存在但被禁用，则抛出异常
     *
     * @param phone 手机号
     * @return 用户信息对象
     * @throws UserException 如果用户被禁用
     */
    private SysUserVo loadUserByPhone(String phone) {
        // 构建查询条件 - 按手机号精确匹配
        SysUserVo user = userService.getUserByPhone(phone);

        // 如果用户不存在，isAutoRegister标志为true，表示需要自动注册新用户
        boolean isAutoRegister = true;
        // 用户不存在 - 自动注册新用户
        if (ObjectUtil.isNull(user)) {
            if (!isAutoRegister) {
                // 如果用户不存在，抛出用户不存在异常
                log.info("手机号用户：{} 不存在", phone);
                throw UserException.of(I18nKeys.User.ACCOUNT_NOT_EXISTS, phone);
            }
            log.info("手机号用户：{} 不存在，正在自动注册...", phone);

            try {
                // 生成用户名：使用手机号作为用户名
                String userName = phone;
                // 生成昵称：使用手机号的脱敏显示
                String nickName = DesensitizedUtil.mobilePhone(phone);
                // 密码为空，后续可通过其他方式设置
                String password = null;
                // 头像为空
                String avatar = null;

                // 调用注册服务的移动端用户注册方法
                user = registerService.registerAppUser(userName, password, phone, nickName, avatar);

                log.info("手机号用户：{} 自动注册成功，用户ID：{}", phone, user.getUserId());

            } catch (Exception e) {
                log.error("手机号用户：{} 自动注册失败", phone, e);
                // 如果自动注册失败，抛出用户不存在异常
                throw UserException.of(I18nKeys.User.ACCOUNT_NOT_EXISTS, phone);
            }
        }
        // 用户被禁用
        else if (DictEnableStatus.DISABLED.getValue().equals(user.getStatus())) {
            log.info("手机号用户：{} 已被停用.", phone);
            // 抛出用户被禁用异常，使用国际化消息
            throw UserException.of(I18nKeys.User.ACCOUNT_DISABLED, phone);
        }

        // 用户存在且状态正常，返回用户信息
        return user;
    }

}
