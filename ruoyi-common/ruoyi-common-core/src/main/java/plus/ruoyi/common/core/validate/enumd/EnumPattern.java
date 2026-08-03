package plus.ruoyi.common.core.validate.enumd;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 枚举值校验注解
 * <p>用于校验字段值是否为指定枚举类型中某个字段的有效值</p>
 * <p>支持校验枚举的任意字段值，如code、value、name等</p>
 * <p>使用示例：</p>
 * <pre>{@code
 * // 校验用户状态枚举的code字段
 * @EnumPattern(type = UserStatusEnum.class, fieldName = "code", message = "用户状态无效")
 * private String status;
 *
 * // 校验性别枚举的value字段
 * @EnumPattern(type = GenderEnum.class, fieldName = "value")
 * private Integer gender;
 *
 * // 多重校验（同时校验多个枚举）
 * @EnumPattern(type = StatusEnum.class, fieldName = "code")
 * @EnumPattern(type = TypeEnum.class, fieldName = "value")
 * private String statusOrType;
 * }</pre>
 *
 * @author 秋辞未寒
 * @date 2024-12-09
 */
@Documented
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
// 允许在同一元素上多次使用该注解
@Repeatable(EnumPattern.List.class)
@Constraint(validatedBy = {EnumPatternValidator.class})
public @interface EnumPattern {

    /**
     * 需要校验的枚举类型
     * <p>示例：UserStatusEnum.class、GenderEnum.class</p>
     *
     * @return 枚举类型，必须继承自Enum
     */
    Class<? extends Enum<?>> type();

    /**
     * 枚举类型中用于校验的字段名称
     * <p>常用字段名：code、value、name、status等</p>
     * <p>注意：该字段必须有对应的getter方法</p>
     *
     * @return 字段名称，不能为空
     */
    String fieldName();

    /**
     * 是否允许空值
     * <p>true表示允许null或空字符串通过校验</p>
     * <p>false表示null或空字符串将校验失败</p>
     *
     * @return 是否允许空值，默认为true
     */
    boolean allowEmpty() default true;

    /**
     * 校验失败时的错误信息
     * <p>支持占位符：{type}表示枚举类名，{fieldName}表示字段名</p>
     *
     * @return 错误信息模板
     */
    String message() default "输入值不在枚举[{type}.{fieldName}]范围内";

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

    /**
     * 多重注解容器
     * <p>支持在同一字段上使用多个@EnumPattern注解</p>
     */
    @Documented
    @Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
    @Retention(RUNTIME)
    @interface List {
        /**
         * 多个EnumPattern注解
         *
         * @return EnumPattern注解数组
         */
        EnumPattern[] value();
    }

}
