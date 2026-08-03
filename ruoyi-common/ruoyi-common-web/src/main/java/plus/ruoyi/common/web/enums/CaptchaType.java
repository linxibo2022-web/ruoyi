package plus.ruoyi.common.web.enums;

import cn.hutool.captcha.generator.CodeGenerator;
import cn.hutool.captcha.generator.RandomGenerator;
import plus.ruoyi.common.web.utils.UnsignedMathGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 验证码类型枚举
 * 定义支持的验证码生成方式
 *
 * @author Lion Li
 */
@Getter
@AllArgsConstructor
public enum CaptchaType {

    /**
     * 数学运算验证码 (如: 3+2=?)
     */
    MATH(UnsignedMathGenerator.class),

    /**
     * 随机字符验证码 (如: ABC123)
     */
    CHAR(RandomGenerator.class);

    /**
     * 验证码生成器类
     */
    private final Class<? extends CodeGenerator> clazz;
}
