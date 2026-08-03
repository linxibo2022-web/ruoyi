package plus.ruoyi.common.sensitive.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import plus.ruoyi.common.sensitive.core.SensitiveStrategy;
import plus.ruoyi.common.sensitive.handler.SensitiveHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据脱敏注解
 *
 * <p>用于标记需要进行数据脱敏的字段，在JSON序列化时根据用户权限自动处理敏感数据。
 *
 * <p>支持特性：
 * <ul>
 *   <li>多种内置脱敏策略：手机号、身份证、邮箱、地址等</li>
 *   <li>基于角色的权限控制：指定角色可查看原始数据</li>
 *   <li>基于权限的访问控制：指定权限可查看原始数据</li>
 *   <li>自动化处理：集成Jackson序列化，无需手动调用</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>
 * public class User {
 *     // 手机号脱敏，admin角色可查看原数据
 *     {@code @Sensitive(strategy = SensitiveStrategy.PHONE, roleKey = {"admin"})}
 *     private String phone;
 *
 *     // 身份证脱敏，需要用户查询权限
 *     {@code @Sensitive(strategy = SensitiveStrategy.ID_CARD, perms = {"user:query"})}
 *     private String idCard;
 *
 *     // 邮箱脱敏，满足任一角色或权限即可查看
 *     {@code @Sensitive(strategy = SensitiveStrategy.EMAIL, roleKey = {"admin"}, perms = {"user:detail"})}
 *     private String email;
 * }
 * </pre>
 *
 * @author zhujie
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@JacksonAnnotationsInside
@JsonSerialize(using = SensitiveHandler.class)
public @interface Sensitive {

    /**
     * 脱敏策略
     *
     * <p>指定使用的脱敏算法，如手机号、身份证、邮箱等
     *
     * @return 脱敏策略枚举
     */
    SensitiveStrategy strategy();

    /**
     * 角色标识符数组
     *
     * <p>拥有指定角色的用户可以查看原始数据，多个角色之间为OR关系(满足其中一个即可)
     *
     * @return 角色标识符数组，默认为空数组
     */
    String[] roleKey() default {};

    /**
     * 权限标识符数组
     *
     * <p>拥有指定权限的用户可以查看原始数据，多个权限之间为OR关系(满足其中一个即可)
     *
     * @return 权限标识符数组，默认为空数组
     */
    String[] perms() default {};
}
