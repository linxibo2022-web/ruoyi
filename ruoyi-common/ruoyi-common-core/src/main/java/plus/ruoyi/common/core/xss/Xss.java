package plus.ruoyi.common.core.xss;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * XSS攻击防护校验注解
 * <p>用于检测和防止跨站脚本攻击（Cross-Site Scripting）的恶意代码注入</p>
 * <p>通过正则表达式检测HTML标签、JavaScript代码等潜在的XSS攻击载荷</p>
 *  富文本场景可以使用宽松模式,或者使用别的方式,比如自定义富文本校验器通过Jsoup实现,纯正则表达式的XSS检测存在局限性
 * <p>使用示例：</p>
 * <pre>{@code
 * public class UserBo {
 *     @Xss(message = "用户名不能包含脚本代码")
 *     private String userName;
 *
 *     @Xss(mode = Xss.Mode.STRICT, message = "评论内容存在安全风险")
 *     private String comment;
 *
 *     @Xss(mode = Xss.Mode.LENIENT, message = "富文本内容包含危险脚本")
 *     private String richText;
 * }
 * }</pre>
 *
 * @author Lion Li
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.PARAMETER})
@Constraint(validatedBy = {XssValidator.class})
public @interface Xss {

    /**
     * XSS检测模式
     */
    enum Mode {
        /** 基础模式：检测常见的HTML标签和脚本 */
        BASIC,
        /** 严格模式：检测更多潜在的XSS攻击向量 */
        STRICT,
        /** 宽松模式：仅检测明显的脚本标签 */
        LENIENT
    }

    /**
     * XSS检测模式
     * <p>不同模式对应不同的检测严格程度：</p>
     * <ul>
     *     <li>BASIC：检测常见HTML标签，适用于大多数场景</li>
     *     <li>STRICT：检测更多攻击向量，适用于安全要求高的场景</li>
     *     <li>LENIENT：仅检测明显脚本，适用于需要部分HTML的场景</li>
     * </ul>
     *
     * @return XSS检测模式，默认为BASIC
     */
    Mode mode() default Mode.BASIC;

    /**
     * 校验失败时的错误信息
     *
     * @return 错误信息，支持占位符{mode}表示检测模式
     */
    String message() default "输入内容包含潜在的XSS攻击代码，请检查并移除脚本标签";

    /**
     * 校验分组
     *
     * @return 校验分组数组
     */
    Class<?>[] groups() default {};

    /**
     * 负载信息
     *
     * @return 负载信息数组
     */
    Class<? extends Payload>[] payload() default {};

}
