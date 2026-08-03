package plus.ruoyi.common.core.xss;

import cn.hutool.core.util.ReUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.utils.StringUtils;

import java.util.regex.Pattern;

/**
 * XSS攻击防护校验器
 * <p>实现Xss注解的校验逻辑，检测和防止跨站脚本攻击</p>
 * <p>支持多种检测模式，提供不同级别的安全防护</p>
 *
 * @author Lion Li
 */
@Slf4j
public class XssValidator implements ConstraintValidator<Xss, String> {

    /**
     * XSS检测模式
     */
    private Xss.Mode mode;

    /**
     * 基础模式：检测常见HTML标签和基本脚本
     */
    private static final Pattern BASIC_XSS_PATTERN = Pattern.compile("""
        (?i)(
        <[^>]*script[^>]*>|
        <[^>]*on\\w+\\s*=|
        <[^>]*style\\s*=.*expression\\s*\\(|
        javascript\\s*:|
        vbscript\\s*:|
        <[^>]*src\\s*=\\s*["']?\\s*javascript\\s*:|
        <\\s*/?.*(script|object|applet|embed|form|iframe|frameset|frame)\\s*[^>]*>
        )
        """);

    /**
     * 严格模式：检测更多XSS攻击向量
     */
    private static final Pattern STRICT_XSS_PATTERN = Pattern.compile("""
        (?i)(
        <[^>]*script[^>]*>|
        <[^>]*on\\w+\\s*=|
        <[^>]*style\\s*=.*expression\\s*\\(|
        (javascript|vbscript)\\s*:|
        data\\s*:.*text/html|
        <[^>]*src\\s*=\\s*["']?\\s*(javascript|vbscript|data)\\s*:|
        (&#\\d+;?)|(&#x[0-9a-f]+;?)|
        (%3c|%3e|%22|%27|%2f|%5c)|
        (\\\\x[0-9a-f]{2})|(\\\\u[0-9a-f]{4})|
        (eval|expression)\\s*\\(|
        String\\s*\\.\\s*fromCharCode|
        (document|window)\\s*\\.|
        (alert|confirm|prompt)\\s*\\(|
        <\\s*/?.*(script|object|applet|embed|form|iframe|frameset|frame|meta|link|style|base)\\s*[^>]*>
        )
        """);

    /**
     * 宽松模式：仅检测明显的脚本标签
     */
    private static final Pattern LENIENT_XSS_PATTERN = Pattern.compile("""
            (?i)(
            <\\s*script[^>]*>.*?</\\s*script\\s*>|
            (javascript|vbscript)\\s*:|
            <\\s*(iframe|object|embed)[^>]*>
            )
            """);

    /**
     * 初始化校验器
     * <p>从注解中提取配置参数</p>
     *
     * @param annotation Xss注解实例
     */
    @Override
    public void initialize(Xss annotation) {
        this.mode = annotation.mode();
        log.debug("初始化XSS校验器: mode={}", mode);
    }

    /**
     * 执行XSS攻击检测
     * <p>校验逻辑：</p>
     * <ol>
     *     <li>空值检查：空值直接通过校验（空字符串不可能是XSS攻击）</li>
     *     <li>模式选择：根据配置的检测模式选择对应的正则表达式</li>
     *     <li>模式检测：使用正则表达式检测潜在的XSS攻击代码</li>
     * </ol>
     *
     * @param value   待校验的字符串
     * @param context 校验上下文，用于构建自定义错误信息
     * @return true表示安全（无XSS攻击代码），false表示存在安全风险
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 空值直接通过校验（空字符串不可能是XSS攻击）
        if (StringUtils.isBlank(value)) {
            return true;
        }

        try {
            // 根据模式选择检测策略
            boolean containsXss = switch (mode) {
                case BASIC -> detectBasicXss(value);
                case STRICT -> detectStrictXss(value);
                case LENIENT -> detectLenientXss(value);
            };

            if (containsXss) {
                log.warn("检测到XSS攻击代码: mode={}, value={}", mode,
                    value.length() > 100 ? value.substring(0, 100) + "..." : value);
                buildCustomErrorMessage(context, value);
                return false;
            }

            return true;

        } catch (Exception e) {
            log.error("XSS检测异常: mode={}, value={}", mode, value, e);
            // 出现异常时为了安全起见，认为检测失败
            return false;
        }
    }

    /**
     * 基础模式XSS检测
     * <p>检测常见的HTML标签和基本脚本攻击，适用于大多数业务场景</p>
     * <p>检测内容：script标签、事件处理器、伪协议、危险标签等</p>
     *
     * @param value 待检测的字符串
     * @return true表示检测到XSS，false表示安全
     */
    private boolean detectBasicXss(String value) {
        return ReUtil.contains(BASIC_XSS_PATTERN, value);
    }

    /**
     * 严格模式XSS检测
     * <p>检测更多的XSS攻击向量，包括编码绕过、函数调用等高级攻击手段</p>
     * <p>检测内容：基础检测 + HTML实体编码、URL编码、Unicode编码、危险函数等</p>
     *
     * @param value 待检测的字符串
     * @return true表示检测到XSS，false表示安全
     */
    private boolean detectStrictXss(String value) {
        return ReUtil.contains(STRICT_XSS_PATTERN, value);
    }

    /**
     * 宽松模式XSS检测
     * <p>仅检测明显的脚本标签和危险协议，适用于需要允许部分HTML的场景</p>
     * <p>检测内容：明显的script标签、伪协议、iframe/object/embed标签</p>
     *
     * @param value 待检测的字符串
     * @return true表示检测到XSS，false表示安全
     */
    private boolean detectLenientXss(String value) {
        return ReUtil.contains(LENIENT_XSS_PATTERN, value);
    }

    /**
     * 构建自定义错误消息
     *
     * @param context        校验上下文
     * @param dangerousValue 包含危险代码的值
     */
    private void buildCustomErrorMessage(ConstraintValidatorContext context, String dangerousValue) {
        // 禁用默认错误消息
        context.disableDefaultConstraintViolation();

        // 构建详细的错误消息
        String customMessage = String.format(
            "输入内容包含潜在的XSS攻击代码（检测模式：%s），请移除脚本标签后重试",
            mode.name()
        );

        context.buildConstraintViolationWithTemplate(customMessage)
            .addConstraintViolation();
    }

    /**
     * 手动检测XSS攻击（工具方法）
     * <p>提供给其他组件使用的静态检测方法</p>
     *
     * @param value 待检测的字符串
     * @param mode  检测模式
     * @return true表示检测到XSS，false表示安全
     */
    public static boolean containsXss(String value, Xss.Mode mode) {
        if (StringUtils.isBlank(value)) {
            return false;
        }

        XssValidator validator = new XssValidator();
        return switch (mode) {
            case BASIC -> validator.detectBasicXss(value);
            case STRICT -> validator.detectStrictXss(value);
            case LENIENT -> validator.detectLenientXss(value);
        };
    }

    /**
     * 简化的XSS检测方法（使用基础模式）
     *
     * @param value 待检测的字符串
     * @return true表示检测到XSS，false表示安全
     */
    public static boolean containsXss(String value) {
        return containsXss(value, Xss.Mode.BASIC);
    }

}
