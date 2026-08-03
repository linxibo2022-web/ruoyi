package plus.ruoyi.common.core.utils;

import cn.hutool.core.util.ObjectUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.function.Function;

/**
 * 对象工具类
 * <p>基于HuTool的ObjectUtil进行扩展，提供安全的对象字段访问和默认值处理功能</p>
 * <p>主要用于避免空指针异常，简化对象字段的安全访问</p>
 *
 * @author 秋辞未寒
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ObjectUtils extends ObjectUtil {

    /**
     * 安全获取对象的指定字段值
     * <p>当对象或获取函数为null时，返回null而不抛出异常</p>
     * <p>使用示例：{@code ObjectUtils.getIfNotNull(user, User::getName)}</p>
     *
     * @param <T> 对象类型
     * @param <R> 返回值类型
     * @param obj 目标对象
     * @param getter 字段获取函数（通常使用方法引用）
     * @return 字段值，如果对象或函数为null则返回null
     */
    public static <T, R> R getIfNotNull(T obj, Function<T, R> getter) {
        if (isNotNull(obj) && isNotNull(getter)) {
            return getter.apply(obj);
        }
        return null;
    }

    /**
     * 安全获取对象的指定字段值，支持默认值
     * <p>当对象或获取函数为null时，返回指定的默认值</p>
     * <p>使用示例：{@code ObjectUtils.getIfNotNull(user, User::getName, "未知用户")}</p>
     *
     * @param <T> 对象类型
     * @param <R> 返回值类型
     * @param obj 目标对象
     * @param getter 字段获取函数（通常使用方法引用）
     * @param defaultValue 默认值，当对象或函数为null时返回
     * @return 字段值或默认值
     */
    public static <T, R> R getIfNotNull(T obj, Function<T, R> getter, R defaultValue) {
        if (isNotNull(obj) && isNotNull(getter)) {
            return getter.apply(obj);
        }
        return defaultValue;
    }

}
