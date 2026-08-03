package plus.ruoyi.common.core.validate.dicts;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字典值校验注解
 * <p>用于校验字段值是否为指定字典类型中的有效值</p>
 * <p>支持单个值校验和多个值（分隔符分割）校验</p>
 * <p>使用示例：</p>
 * <pre>{@code
 * @DictPattern(dictType = "sys_user_gender", message = "性别字典值无效")
 * private String gender;
 *
 * @DictPattern(dictType = "sys_user_status", separator = ";", message = "状态值无效")
 * private String statusList;
 * }</pre>
 *
 * @author AprilWind
 */
@Documented
@Constraint(validatedBy = DictPatternValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DictPattern {

    /**
     * 字典类型编码
     * <p>示例："sys_user_gender"、"sys_user_status"</p>
     *
     * @return 字典类型编码，不能为空
     */
    String dictType();

    /**
     * 多值分隔符
     * <p>当字段包含多个字典值时使用的分隔符</p>
     * <p>默认为逗号","，可以自定义为";"、"|"等</p>
     *
     * @return 分隔符字符串，默认为逗号
     */
    String separator() default ",";

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
     *
     * @return 错误信息模板，支持占位符{dictType}
     */
    String message() default "字典值无效，不在[{dictType}]字典范围内";

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
