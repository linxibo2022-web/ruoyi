package plus.ruoyi.system.auth.controller;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.dromara.sms4j.api.SmsBlend;
import org.dromara.sms4j.api.entity.SmsResponse;
import org.dromara.sms4j.core.factory.SmsFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.captcha.AbstractCaptcha;
import cn.hutool.captcha.generator.CodeGenerator;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import plus.ruoyi.common.core.constant.Constants;
import plus.ruoyi.common.core.constant.GlobalConstants;
import plus.ruoyi.common.core.constant.I18nKeys;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.service.ConfigService;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.core.utils.reflect.ReflectUtils;
import plus.ruoyi.common.mail.config.properties.MailProperties;
import plus.ruoyi.common.mail.utils.MailUtils;
import plus.ruoyi.common.ratelimiter.annotation.RateLimiter;
import plus.ruoyi.common.ratelimiter.enums.LimitType;
import plus.ruoyi.common.redis.utils.RedisUtils;
import plus.ruoyi.common.social.config.properties.SocialLoginConfigProperties;
import plus.ruoyi.common.social.config.properties.SocialProperties;
import plus.ruoyi.common.tenant.helper.TenantHelper;
import plus.ruoyi.common.web.config.properties.CaptchaProperties;
import plus.ruoyi.common.web.enums.CaptchaType;
import plus.ruoyi.system.auth.domain.vo.CaptchaVo;
import plus.ruoyi.system.tenant.service.ISysTenantService;

/**
 * 验证码控制器
 * 提供各种类型验证码的生成和校验功能，包括图片验证码、短信验证码和邮箱验证码
 *
 * @author Lion Li
 */
@SaIgnore
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class CaptchaController {

    // 业务服务
    private final ConfigService configService;
    private final ISysTenantService tenantService;

    // 配置属性
    private final CaptchaProperties captchaProperties;
    private final MailProperties mailProperties;
    private final SocialProperties socialProperties;

    /**
     * 生成图片验证码
     * 根据配置生成对应类型的图片验证码
     *
     * @return 验证码信息，包括验证码图片的Base64编码和UUID
     */
    @GetMapping("/imgCode")
    public R<CaptchaVo> imgCode() {
        // 检查验证码功能是否启用
        boolean captchaEnabled = configService.getBooleanValue(
            CaptchaProperties.CAPTCHA_ENABLED_KEY, true);
        if (!captchaEnabled) {
            // 验证码未启用，返回空结果
            CaptchaVo captchaVo = new CaptchaVo();
            captchaVo.setTenantEnabled(TenantHelper.isEnable());
            if (captchaVo.getTenantEnabled()) {
                captchaVo.setTenantId(TenantHelper.getTenantId());
                captchaVo.setTenantTitle(tenantService.getTenantTitle());
            }
            //设置是否开启了注册
            boolean registerEnabled = configService.getBooleanValue("system.account.register-enabled");
            captchaVo.setRegisterEnabled(registerEnabled);
            captchaVo.setCaptchaEnabled(false);
            // 设置已配置的社交登录类型
            captchaVo.setSocialTypes(getConfiguredSocialTypes());
            // 设置社交登录自动注册开关（只有注册开关开启时才有效）
            captchaVo.setSocialAutoRegisterEnabled(
                registerEnabled && configService.getBooleanValue("system.social.auto-register-enabled", false)
            );
            return R.ok(captchaVo);
        }

        // 调用实际的验证码生成实现方法（使用AOP代理以支持限流）
        return R.ok(SpringUtils.getAopProxy(this).getCodeImpl());
    }

    /**
     * 图片验证码生成实现方法
     * 独立方法避免验证码关闭之后仍然走限流逻辑，限制每IP每60秒最多请求10次
     *
     * @return 验证码信息对象
     */
    @RateLimiter(time = 60, count = 20, limitType = LimitType.IP)
    public CaptchaVo getCodeImpl() {
        // 生成唯一标识
        String uuid = IdUtil.simpleUUID();
        // 构建验证码缓存键
        String verifyKey = GlobalConstants.CAPTCHA_CODE_KEY + uuid;

        // 获取验证码类型配置（数学计算或字符验证码）
        CaptchaType captchaType = captchaProperties.getType();
        boolean isMath = CaptchaType.MATH == captchaType;

        // 根据配置确定验证码长度
        Integer length = isMath ? captchaProperties.getNumberLength() : captchaProperties.getCharLength();

        // 创建验证码生成器
        CodeGenerator codeGenerator = ReflectUtils.newInstance(captchaType.getClazz(), length);

        // 创建验证码实例
        AbstractCaptcha captcha = SpringUtils.getBean(captchaProperties.getCategory().getClazz());
        captcha.setGenerator(codeGenerator);

        // 生成验证码
        captcha.createCode();

        // 如果是数学验证码，使用SpEL表达式计算结果
        String code = captcha.getCode();
        if (isMath) {
            ExpressionParser parser = new SpelExpressionParser();
            Expression exp = parser.parseExpression(StringUtils.remove(code, "="));
            code = exp.getValue(String.class);
        }

        // 将验证码存入Redis，有效期为配置的验证码过期时间
        RedisUtils.setCacheObject(verifyKey, code, Duration.ofMinutes(Constants.CAPTCHA_EXPIRATION));

        // 构建并返回验证码信息
        CaptchaVo captchaVo = new CaptchaVo();
        captchaVo.setTenantEnabled(TenantHelper.isEnable());
        if (captchaVo.getTenantEnabled()) {
            captchaVo.setTenantId(TenantHelper.getTenantId());
            captchaVo.setTenantTitle(tenantService.getTenantTitle());
        }
        //设置是否开启了注册
        boolean registerEnabled = configService.getBooleanValue("system.account.register-enabled");
        captchaVo.setRegisterEnabled(registerEnabled);
        captchaVo.setUuid(uuid);
        captchaVo.setImg("data:image/gif;base64," + captcha.getImageBase64());
        // 设置已配置的社交登录类型
        captchaVo.setSocialTypes(getConfiguredSocialTypes());
        // 设置社交登录自动注册开关（只有注册开关开启时才有效）
        captchaVo.setSocialAutoRegisterEnabled(
            registerEnabled && configService.getBooleanValue("system.social.auto-register-enabled", false)
        );
        return captchaVo;
    }

    /**
     * 获取短信验证码
     * 生成随机验证码并发送到指定手机号，限制每60秒只能发送1次
     *
     * @param phone 用户手机号
     * @return 发送结果
     */
    @RateLimiter(key = "#phone", time = 60, count = 1)
    @GetMapping("/smsCode")
    public R<Void> smsCode(@NotBlank(message = I18nKeys.User.PHONE_REQUIRED) String phone) {
        // 构建缓存键，用于存储验证码
        String key = GlobalConstants.CAPTCHA_CODE_KEY + phone;
        // 生成4位随机数字验证码
        String code = RandomUtil.randomNumbers(4);
        // 将验证码存入Redis，有效期为配置的过期时间（默认为2分钟）
        RedisUtils.setCacheObject(key, code, Duration.ofMinutes(Constants.CAPTCHA_EXPIRATION));

        // 验证码模板ID，实际项目中应从配置或数据库中获取
        String templateId = "";
        // 构建短信模板参数，将验证码放入map中
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("code", code);

        // 获取短信发送服务实例
        SmsBlend smsBlend = SmsFactory.getSmsBlend("config1");
        // 发送短信
        SmsResponse smsResponse = smsBlend.sendMessage(phone, templateId, map);

        // 检查短信发送结果
        if (!smsResponse.isSuccess()) {
            // 发送失败时记录日志
            log.error("验证码短信发送异常 => {}", smsResponse);
            // 返回发送失败信息
            return R.fail(smsResponse.getData().toString());
        }

        // 发送成功返回
        return R.ok();
    }

    /**
     * 获取邮箱验证码
     * 生成随机验证码并发送到指定邮箱
     *
     * @param email 邮箱地址
     * @return 发送结果
     */
    @GetMapping("/emailCode")
    public R<Void> emailCode(@NotBlank(message = I18nKeys.User.EMAIL_REQUIRED) String email) {
        // 检查邮箱功能是否启用
        if (!mailProperties.getEnabled()) {
            return R.fail("当前系统没有开启邮箱功能！");
        }

        // 调用实际的邮箱验证码实现方法（使用AOP代理以支持限流）
        SpringUtils.getAopProxy(this).emailCodeImpl(email);
        return R.ok();
    }

    /**
     * 邮箱验证码实现方法
     * 独立方法避免验证码关闭之后仍然走限流逻辑，限制每60秒只能发送1次
     *
     * @param email 邮箱地址
     */
    @RateLimiter(key = "#email", time = 60, count = 1)
    public void emailCodeImpl(String email) {
        // 构建验证码缓存键
        String key = GlobalConstants.CAPTCHA_CODE_KEY + email;
        // 生成四位数字验证码
        String code = RandomUtil.randomNumbers(4);
        // 将验证码存入Redis，有效期为配置的验证码过期时间
        RedisUtils.setCacheObject(key, code, Duration.ofMinutes(Constants.CAPTCHA_EXPIRATION));

        try {
            // 发送包含验证码的邮件
            MailUtils.sendText(email, "登录验证码",
                "您本次验证码为：" + code + "，有效性为" + Constants.CAPTCHA_EXPIRATION + "分钟，请尽快填写。");
        } catch (Exception e) {
            log.error("验证码短信发送异常 => {}", e.getMessage());
            throw ServiceException.of(e.getMessage());
        }
    }

    /**
     * 获取已配置的社交登录类型
     * 遍历社交登录配置，筛选出已配置的类型（clientId和clientSecret不包含*）
     *
     * @return 逗号分隔的社交登录类型字符串
     */
    private String getConfiguredSocialTypes() {
        if (socialProperties == null || socialProperties.getType() == null) {
            return "";
        }

        // 筛选出已配置的社交登录类型
        List<String> configuredTypes = socialProperties.getType().entrySet().stream()
            .filter(entry -> {
                SocialLoginConfigProperties config = entry.getValue();
                // 判断clientId和clientSecret是否包含*（未配置的标志）
                boolean clientIdConfigured = StrUtil.isNotBlank(config.getClientId())
                    && !config.getClientId().contains("*");
                boolean clientSecretConfigured = StrUtil.isNotBlank(config.getClientSecret())
                    && !config.getClientSecret().contains("*");
                return clientIdConfigured && clientSecretConfigured;
            })
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        // 返回逗号分隔的字符串
        return String.join(",", configuredTypes);
    }

}
