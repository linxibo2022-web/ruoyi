package plus.ruoyi.common.core.validate.message;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;

import jakarta.validation.MessageInterpolator;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.core.utils.regex.RegexValidator;

import java.util.Locale;

/**
 * 国际化消息拦截器
 * <p>
 * 该拦截器扩展了Hibernate Validator的默认消息插值行为
 * 2. 简化格式：key - 直接使用国际化键，无需花括号包围
 *
 * @author 抓蛙师
 */
public class I18nMessageInterceptor implements MessageInterpolator {

    private final MessageSource messageSource;
    private final MessageInterpolator defaultInterpolator;

    /**
     * 构造函数
     *
     * @param messageSource Spring的消息源，用于国际化消息查找
     */
    public I18nMessageInterceptor(MessageSource messageSource) {
        this.messageSource = messageSource;
        this.defaultInterpolator = new ResourceBundleMessageInterpolator();
    }

    /**
     * 消息插值处理（使用当前线程的区域设置）
     *
     * @param messageTemplate 消息模板
     * @param context         约束验证上下文
     * @return 插值后的消息
     */
    @Override
    public String interpolate(String messageTemplate, Context context) {
        return interpolate(messageTemplate, context, LocaleContextHolder.getLocale());
    }

    /**
     * 消息插值处理（指定区域设置）
     * <p>
     * 处理逻辑：
     * 1. 检查消息模板是否符合国际化键格式（不包含花括号的简单键）
     * 2. 如果是简化格式，直接从MessageSource查找对应的国际化消息
     * 3. 如果是标准格式或其他复杂格式，委托给默认的插值器处理
     * 4. 查找失败时，返回原始消息模板作为降级处理
     * </p>
     *
     * @param messageTemplate 消息模板，可能是国际化键或标准的{key}格式
     * @param context         约束验证上下文，包含约束相关信息
     * @param locale          区域设置，用于确定返回哪种语言的消息
     * @return 插值后的最终消息
     */
    @Override
    public String interpolate(String messageTemplate, Context context, Locale locale) {
        if (StringUtils.isBlank(messageTemplate)) {
            return messageTemplate;
        }
        String trimmedTemplate = messageTemplate.trim();
        // 判断是否为简化的国际化键格式（不包含花括号）
        if (RegexValidator.isValidI18nKey(trimmedTemplate)) {
            try {
                // 第一步：获取国际化消息
                String i18nMessage = messageSource.getMessage(trimmedTemplate, null, locale);

                // 第二步：将国际化消息交给默认插值器处理约束属性等
                return defaultInterpolator.interpolate(i18nMessage, context, locale);

            } catch (Exception e) {
                // 找不到国际化消息时，返回原始键
                return trimmedTemplate;
            }
        }

        // 其他情况直接委托给默认插值器处理
        return defaultInterpolator.interpolate(messageTemplate, context, locale);
    }

}
