package plus.ruoyi.common.web.config.properties;

import plus.ruoyi.common.web.enums.CaptchaCategory;
import plus.ruoyi.common.web.enums.CaptchaType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 验证码配置属性类
 * <p>
 * 用于配置系统中验证码的相关参数，包括验证码类型、类别、长度等。
 * 配置前缀：captcha
 * </p>
 *
 * @author Lion Li
 */
@Data
@ConfigurationProperties(prefix = "captcha")
public class CaptchaProperties {

    /**
     * 验证码类型
     * <p>定义验证码的展现形式，如图片验证码、滑块验证码等</p>
     */
    private CaptchaType type;

    /**
     * 验证码类别
     * <p>定义验证码的内容类型，如纯数字、字母、混合等</p>
     */
    private CaptchaCategory category;

    /**
     * 数字验证码位数
     * <p>当验证码类别为数字时，指定生成的数字位数</p>
     */
    private Integer numberLength;

    /**
     * 字符验证码长度
     * <p>当验证码类别为字符或混合时，指定生成的字符长度</p>
     */
    private Integer charLength;

    /**
     * 验证码开关配置键名
     * <p>用于在系统配置中启用或禁用验证码功能的配置键</p>
     */
    public static final String CAPTCHA_ENABLED_KEY = "system.security.captcha-enabled";
}
