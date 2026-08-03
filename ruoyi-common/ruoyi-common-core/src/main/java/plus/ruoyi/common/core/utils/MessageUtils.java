package plus.ruoyi.common.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * 国际化消息工具类
 *
 * <p>提供便捷的方法来获取i18n资源文件中的消息内容，
 * 基于Spring的MessageSource实现多语言支持</p>
 *
 * @author Lion Li
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageUtils {

    /**
     * Spring消息源，用于获取国际化消息
     */
    private static final MessageSource MESSAGE_SOURCE = SpringUtils.getBean(MessageSource.class);

    /**
     * 根据消息键和参数获取国际化消息
     *
     * <p>委托给Spring MessageSource处理，根据当前线程的Locale自动选择语言</p>
     *
     * @param code 消息键，对应资源文件中的key
     * @param args 消息参数，用于替换消息模板中的占位符
     * @return 国际化翻译后的消息内容，如果消息键不存在则返回消息键本身
     */
    public static String message(String code, Object... args) {
        try {
            return MESSAGE_SOURCE.getMessage(code, args, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException e) {
            return code;
        }
    }
}
