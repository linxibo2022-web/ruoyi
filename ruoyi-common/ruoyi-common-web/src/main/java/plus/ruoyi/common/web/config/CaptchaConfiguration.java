package plus.ruoyi.common.web.config;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.ShearCaptcha;
import org.springframework.context.annotation.Configuration;
import plus.ruoyi.common.web.config.properties.CaptchaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

import java.awt.*;

/**
 * 验证码配置类
 * <p>
 * 配置不同类型的验证码生成器，包括圆圈干扰、线段干扰和扭曲干扰验证码。
 * 所有验证码使用统一的尺寸、背景色和字体样式。
 * </p>
 *
 * @author Lion Li
 */
@Configuration
@EnableConfigurationProperties(CaptchaProperties.class)
public class CaptchaConfiguration {

    /**
     * 验证码图片宽度
     */
    private static final int WIDTH = 160;

    /**
     * 验证码图片高度
     */
    private static final int HEIGHT = 60;

    /**
     * 验证码背景色
     */
    private static final Color BACKGROUND = Color.WHITE;

    /**
     * 验证码字体样式
     */
    private static final Font FONT = new Font("Arial", Font.BOLD, 48);

    /**
     * 圆圈干扰验证码
     * <p>生成带有圆圈干扰线的验证码</p>
     *
     * @return 圆圈干扰验证码实例
     */
    @Lazy
    @Bean
    public CircleCaptcha circleCaptcha() {
        CircleCaptcha captcha = CaptchaUtil.createCircleCaptcha(WIDTH, HEIGHT);
        captcha.setBackground(BACKGROUND);
        captcha.setFont(FONT);
        return captcha;
    }

    /**
     * 线段干扰验证码
     * <p>生成带有线段干扰线的验证码</p>
     *
     * @return 线段干扰验证码实例
     */
    @Lazy
    @Bean
    public LineCaptcha lineCaptcha() {
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(WIDTH, HEIGHT);
        captcha.setBackground(BACKGROUND);
        captcha.setFont(FONT);
        return captcha;
    }

    /**
     * 扭曲干扰验证码
     * <p>生成带有扭曲效果的验证码，增加识别难度</p>
     *
     * @return 扭曲干扰验证码实例
     */
    @Lazy
    @Bean
    public ShearCaptcha shearCaptcha() {
        ShearCaptcha captcha = CaptchaUtil.createShearCaptcha(WIDTH, HEIGHT);
        captcha.setBackground(BACKGROUND);
        captcha.setFont(FONT);
        return captcha;
    }

}
