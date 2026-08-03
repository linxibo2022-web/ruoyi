package plus.ruoyi.common.web.enums;

import cn.hutool.captcha.AbstractCaptcha;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.ShearCaptcha;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 验证码类别枚举
 * <p>
 * 定义验证码的干扰样式类型，每种类别对应不同的干扰效果。
 * 用于配置验证码的视觉干扰方式，提高安全性。
 * </p>
 *
 * @author Lion Li
 */
@Getter
@AllArgsConstructor
public enum CaptchaCategory {

    /**
     * 线段干扰验证码
     * <p>使用随机线段作为干扰元素</p>
     */
    LINE(LineCaptcha.class),

    /**
     * 圆圈干扰验证码
     * <p>使用随机圆圈作为干扰元素</p>
     */
    CIRCLE(CircleCaptcha.class),

    /**
     * 扭曲干扰验证码
     * <p>对验证码进行扭曲变形处理，增加识别难度</p>
     */
    SHEAR(ShearCaptcha.class);

    /**
     * 验证码实现类
     */
    private final Class<? extends AbstractCaptcha> clazz;
}
