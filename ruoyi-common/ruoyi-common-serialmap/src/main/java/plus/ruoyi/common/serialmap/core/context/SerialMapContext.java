package plus.ruoyi.common.serialmap.core.context;

import lombok.Builder;
import lombok.Getter;
import plus.ruoyi.common.serialmap.annotation.SerialMap;

import java.lang.reflect.Type;

/**
 * 序列化映射上下文
 *
 * <p>基于ThreadLocal实现的上下文管理器，用于在序列化过程中传递当前处理字段的相关信息。
 *
 * <p>主要功能：
 * <ul>
 *   <li>存储当前序列化字段的元数据信息</li>
 *   <li>在转换器中提供字段上下文信息</li>
 *   <li>支持复杂转换场景中的上下文感知</li>
 *   <li>确保线程安全的上下文传递</li>
 * </ul>
 *
 * <p>注意：上下文在使用完毕后会自动清理，避免内存泄漏。
 *
 * @author 抓蛙师
 */
@Getter
@Builder
public class SerialMapContext {

    /** 当前线程的上下文存储 */
    private static final ThreadLocal<SerialMapContext> CURRENT = new ThreadLocal<>();

    /** 当前处理的属性名 */
    private final String propertyName;

    /** 当前处理的字段类型 */
    private final Class<?> fieldType;

    /** 当前处理的泛型类型 */
    private final Type genericType;

    /** 当前处理的SerialMap注解 */
    private final SerialMap annotation;

    /**
     * 获取当前线程的上下文
     *
     * @return 当前上下文实例，如果未设置则返回null
     */
    public static SerialMapContext current() {
        return CURRENT.get();
    }

    /**
     * 设置当前线程的上下文
     *
     * @param context 要设置的上下文实例
     */
    public static void setCurrent(SerialMapContext context) {
        CURRENT.set(context);
    }

    /**
     * 清除当前线程的上下文
     *
     * <p>重要：必须在处理完成后调用，避免ThreadLocal内存泄漏
     */
    public static void clear() {
        CURRENT.remove();
    }
}
