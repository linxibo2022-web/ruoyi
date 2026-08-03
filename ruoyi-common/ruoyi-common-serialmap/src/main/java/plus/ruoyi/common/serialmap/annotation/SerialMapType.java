package plus.ruoyi.common.serialmap.annotation;

import java.lang.annotation.*;
import plus.ruoyi.common.serialmap.core.SerialMapInterface;

/**
 * 序列化映射转换器类型注解
 *
 * <p>用于标识{@link SerialMapInterface}实现类的转换器类型，建立转换器与类型标识的映射关系。
 *
 * <p>使用场景：
 * <ul>
 *   <li>标注在转换器实现类上，声明该转换器的类型标识</li>
 *   <li>配合{@link SerialMap#converter()}使用，建立转换器的查找索引</li>
 *   <li>支持转换器的自动注册和管理</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>
 * // 用户ID转用户名转换器
 * {@code @SerialMapType(type = "user_id_to_name")}
 * {@code @Component}
 * public class UserIdToNameConverter implements SerialMapInterface&lt;String&gt; {
 *     {@code @Override}
 *     public String convert(Object source, String param, Class&lt;?&gt; entityClass, String targetField) {
 *         // 转换逻辑实现
 *         return convertUserIdToName(source);
 *     }
 * }
 *
 * // 字典转换器
 * {@code @SerialMapType(type = "dict_type_to_label")}
 * {@code @Component}
 * public class DictConverter implements SerialMapInterface&lt;String&gt; {
 *     // 转换实现...
 * }
 * </pre>
 *
 * <p>注意：type值必须在全局范围内唯一，建议使用有意义的命名规范，如：模块_源_to_目标。
 *
 * @author 抓蛙师
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface SerialMapType {

    /**
     * 转换器类型标识
     *
     * <p>定义转换器的唯一标识，与{@link SerialMap#converter()}的值对应。
     * 系统在初始化时会扫描所有标注此注解的转换器，建立类型标识与转换器实例的映射关系。
     *
     * <p>命名建议：
     * <ul>
     *   <li>使用下划线分隔的小写字母：user_id_to_name</li>
     *   <li>遵循"源_to_目标"的格式：dept_id_to_name</li>
     *   <li>包含业务含义：dict_type_to_label</li>
     * </ul>
     *
     * @return 转换器类型标识字符串
     */
    String type();
}
