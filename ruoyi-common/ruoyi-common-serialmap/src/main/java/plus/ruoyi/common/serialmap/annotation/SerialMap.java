package plus.ruoyi.common.serialmap.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import plus.ruoyi.common.serialmap.core.SerialMapInterface;
import plus.ruoyi.common.serialmap.core.handler.SerialMapHandler;

import java.lang.annotation.*;

/**
 * 序列化映射注解
 *
 * <p>用于在JSON序列化过程中将字段值映射/转换为其他值或对象，支持各种数据转换需求：
 * <ul>
 *   <li>ID转名称：用户ID → 用户名、部门ID → 部门名称</li>
 *   <li>字典映射：字典类型 → 字典标签</li>
 *   <li>资源转换：OSS文件ID → 访问URL</li>
 *   <li>自定义转换：基于业务需求的任意数据转换</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>
 * public class UserVo {
 *     // 用户ID转用户名
 *     {@code @SerialMap(converter = "user_id_to_name")}
 *     private Long userId;
 *
 *     // 从其他字段获取值进行转换
 *     {@code @SerialMap(converter = "dept_id_to_name", source = "deptId")}
 *     private String deptName;
 *
 *     // 字典转换，带参数
 *     {@code @SerialMap(converter = "dict_type_to_label", param = "sys_user_gender")}
 *     private String sex;
 *
 *     // 实体映射
 *     {@code @SerialMap(converter = "field_map", entityClass = SysUser.class, source = "userId", targetField = "nickName")}
 *     private String userNickName;
 *
 *     //方法上也可以使用
 *     {@code @SerialMap(converter = "user_id_to_name")}
 *     public Long getUserId() { // getter方法上使用
 *         return userId;
 *     }
 * }
 * </pre>
 *
 * <p>注意：需要对应的转换器实现{@link SerialMapInterface}接口并标注{@link SerialMapType}注解。
 *
 * @author 抓蛙师
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Documented
@JacksonAnnotationsInside
@JsonSerialize(using = SerialMapHandler.class)
public @interface SerialMap {

    /**
     * 转换器类型标识
     *
     * <p>指定要使用的转换器，对应实现类上{@link SerialMapType}注解的type值。
     *
     * <p>常用转换器类型：
     * <ul>
     *   <li>field_map：通用字段映射</li>
     *   <li>user_id_to_name：用户ID转账号</li>
     *   <li>user_id_to_nickname：用户ID转昵称</li>
     *   <li>dept_id_to_name：部门ID转名称</li>
     *   <li>dict_type_to_label：字典转换</li>
     *   <li>oss_id_to_url：OSS文件ID转URL</li>
     * </ul>
     *
     * @return 转换器类型标识
     */
    String converter();

    /**
     * 源字段名称
     *
     * <p>指定转换数据的来源字段，默认使用当前注解字段的值。
     * 当需要使用其他字段的值进行转换时，可指定该参数。
     *
     * @return 源字段名称，默认为空字符串(使用当前字段)
     */
    String source() default "";

    /**
     * 转换器额外参数
     *
     * <p>提供给转换器的附加信息，用于传递转换所需的配置参数。
     * 例如：字典转换时传递字典类型，查询转换时传递查询条件等。
     *
     * @return 额外参数，默认为空字符串
     */
    String param() default "";

    /**
     * 数据源实体类
     *
     * <p>指定转换数据的来源实体类，用于字段映射等场景。
     * 转换器可基于此实体类进行数据查询或字段映射。
     *
     * @return 实体类Class对象，默认为void.class(未指定)
     */
    Class<?> entityClass() default void.class;

    /**
     * 目标映射字段
     *
     * <p>指定实体类中要映射的目标字段名，用于单字段映射场景。
     * 与entityClass配合使用，实现精确的字段映射。
     *
     * @return 目标字段名，默认为空字符串
     */
    String targetField() default "";
}
