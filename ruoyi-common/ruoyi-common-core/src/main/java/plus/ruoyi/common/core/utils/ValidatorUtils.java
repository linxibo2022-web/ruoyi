package plus.ruoyi.common.core.utils;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 参数校验工具类
 * <p>基于Jakarta Bean Validation提供参数校验功能</p>
 * <p>主要用于手动触发对象的参数校验，支持分组校验和校验结果处理</p>
 *
 * @author 抓蛙师
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidatorUtils {

    /**
     * 校验器实例，从Spring容器获取
     */
    private static final Validator VALIDATOR = SpringUtils.getBean(Validator.class);

    /**
     * 校验对象参数（抛出异常）
     * <p>示例：{@code ValidatorUtils.validate(user)} // 使用默认校验组</p>
     * <p>如果校验失败，将抛出ConstraintViolationException异常</p>
     *
     * @param <T>    校验对象类型
     * @param object 要校验的对象，不能为null
     * @param groups 校验组，可以为空（使用默认校验组）
     * @throws ConstraintViolationException 校验失败时抛出，包含所有校验错误信息
     * @throws IllegalArgumentException     当校验对象为null时抛出
     */
    public static <T> void validate(T object, Class<?>... groups) {
        if (object == null) {
            throw new IllegalArgumentException("校验对象不能为null");
        }

        Set<ConstraintViolation<T>> violations = VALIDATOR.validate(object, groups);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));

            throw new ConstraintViolationException("参数校验失败 - " + errorMessage, violations);
        }
    }

    /**
     * 校验对象参数（返回校验结果）
     * <p>示例：{@code Set<ConstraintViolation<User>> errors = ValidatorUtils.validateAndReturn(user)}</p>
     * <p>不抛出异常，返回校验结果集合，可用于自定义处理校验错误</p>
     *
     * @param <T>    校验对象类型
     * @param object 要校验的对象，不能为null
     * @param groups 校验组，可以为空（使用默认校验组）
     * @return 校验结果集合，如果校验通过则返回空集合
     * @throws IllegalArgumentException 当校验对象为null时抛出
     */
    public static <T> Set<ConstraintViolation<T>> validateAndReturn(T object, Class<?>... groups) {
        if (object == null) {
            throw new IllegalArgumentException("校验对象不能为null");
        }

        return VALIDATOR.validate(object, groups);
    }

    /**
     * 校验对象的指定属性
     * <p>示例：{@code ValidatorUtils.validateProperty(user, "email")}</p>
     * <p>只校验对象的单个属性，适用于实时校验场景</p>
     *
     * @param <T>          校验对象类型
     * @param object       要校验的对象，不能为null
     * @param propertyName 属性名称，不能为空
     * @param groups       校验组，可以为空（使用默认校验组）
     * @throws ConstraintViolationException 校验失败时抛出
     * @throws IllegalArgumentException     当参数无效时抛出
     */
    public static <T> void validateProperty(T object, String propertyName, Class<?>... groups) {
        if (object == null) {
            throw new IllegalArgumentException("校验对象不能为null");
        }
        if (StringUtils.isBlank(propertyName)) {
            throw new IllegalArgumentException("属性名称不能为空");
        }

        Set<ConstraintViolation<T>> violations = VALIDATOR.validateProperty(object, propertyName, groups);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException("属性校验失败", violations);
        }
    }

    /**
     * 校验属性值
     * <p>示例：{@code ValidatorUtils.validateValue(User.class, "email", "invalid-email")}</p>
     * <p>校验指定值是否符合类的某个属性的校验规则，无需实例化对象</p>
     *
     * @param <T>          校验对象类型
     * @param beanType     对象类型
     * @param propertyName 属性名称，不能为空
     * @param value        要校验的值
     * @param groups       校验组，可以为空（使用默认校验组）
     * @throws ConstraintViolationException 校验失败时抛出
     * @throws IllegalArgumentException     当参数无效时抛出
     */
    public static <T> void validateValue(Class<T> beanType, String propertyName, Object value, Class<?>... groups) {
        if (beanType == null) {
            throw new IllegalArgumentException("对象类型不能为null");
        }
        if (StringUtils.isBlank(propertyName)) {
            throw new IllegalArgumentException("属性名称不能为空");
        }

        Set<ConstraintViolation<T>> violations = VALIDATOR.validateValue(beanType, propertyName, value, groups);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException("属性值校验失败", violations);
        }
    }

    /**
     * 检查对象是否校验通过
     * <p>示例：{@code boolean isValid = ValidatorUtils.isValid(user)}</p>
     * <p>只返回是否校验通过的布尔值，不抛出异常</p>
     *
     * @param <T>    校验对象类型
     * @param object 要校验的对象
     * @param groups 校验组，可以为空（使用默认校验组）
     * @return true表示校验通过，false表示校验失败或对象为null
     */
    public static <T> boolean isValid(T object, Class<?>... groups) {
        if (object == null) {
            return false;
        }

        Set<ConstraintViolation<T>> violations = VALIDATOR.validate(object, groups);
        return violations.isEmpty();
    }

    /**
     * 获取校验错误信息字符串
     * <p>示例：{@code String errors = ValidatorUtils.getValidationErrors(user)}</p>
     * <p>返回格式："字段名: 错误信息; 字段名: 错误信息"</p>
     *
     * @param <T>    校验对象类型
     * @param object 要校验的对象
     * @param groups 校验组，可以为空（使用默认校验组）
     * @return 校验错误信息字符串，如果校验通过则返回空字符串
     */
    public static <T> String getValidationErrors(T object, Class<?>... groups) {
        if (object == null) {
            return "校验对象为null";
        }

        Set<ConstraintViolation<T>> violations = VALIDATOR.validate(object, groups);
        if (violations.isEmpty()) {
            return "";
        }

        return violations.stream()
            .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
            .collect(Collectors.joining("; "));
    }

}
