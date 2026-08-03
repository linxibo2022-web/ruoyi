package plus.ruoyi.common.core.validate.enumd;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.core.utils.reflect.ReflectUtils;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.Arrays;

/**
 * 枚举值校验器
 * <p>实现EnumPattern注解的校验逻辑，验证字段值是否为指定枚举类型中某个字段的有效值</p>
 * <p>支持缓存机制，提高重复校验的性能</p>
 *
 * @author 秋辞未寒
 * @date 2024-12-09
 */
@Slf4j
public class EnumPatternValidator implements ConstraintValidator<EnumPattern, Object> {

    /**
     * 枚举类型
     */
    private Class<? extends Enum<?>> enumType;

    /**
     * 校验字段名
     */
    private String fieldName;

    /**
     * 是否允许空值
     */
    private boolean allowEmpty;

    /**
     * 枚举值缓存：key为"enumType-fieldName"，value为有效值集合
     */
    private static final ConcurrentHashMap<String, Set<Object>> ENUM_VALUE_CACHE = new ConcurrentHashMap<>();

    /**
     * 初始化校验器
     * <p>从注解中提取配置参数并预加载枚举值</p>
     *
     * @param annotation EnumPattern注解实例
     */
    @Override
    public void initialize(EnumPattern annotation) {
        this.enumType = annotation.type();
        this.fieldName = annotation.fieldName();
        this.allowEmpty = annotation.allowEmpty();

        log.debug("初始化枚举校验器: enumType={}, fieldName={}, allowEmpty={}",
            enumType.getSimpleName(), fieldName, allowEmpty);

        // 预加载枚举值到缓存
        preloadEnumValues();
    }

    /**
     * 执行枚举值校验
     * <p>校验逻辑：</p>
     * <ol>
     *     <li>空值检查：根据allowEmpty配置决定是否允许空值</li>
     *     <li>缓存查找：从缓存中获取有效枚举值集合</li>
     *     <li>值校验：检查输入值是否在有效值集合中</li>
     * </ol>
     *
     * @param value   待校验的字段值
     * @param context 校验上下文，用于构建自定义错误信息
     * @return true表示校验通过，false表示校验失败
     */
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        // 空值检查
        if (isEmptyValue(value)) {
            return allowEmpty;
        }

        try {
            // 从缓存获取有效枚举值集合
            Set<Object> validValues = getValidEnumValues();

            // 校验输入值是否在有效值集合中
            boolean isValid = validValues.contains(value);

            if (!isValid) {
                log.debug("枚举值校验失败: enumType={}, fieldName={}, value={}, validValues={}",
                    enumType.getSimpleName(), fieldName, value, validValues);
                buildCustomErrorMessage(context, value, validValues);
            }

            return isValid;

        } catch (Exception e) {
            log.error("枚举值校验异常: enumType={}, fieldName={}, value={}",
                enumType.getSimpleName(), fieldName, value, e);
            return false;
        }
    }

    /**
     * 预加载枚举值到缓存
     */
    private void preloadEnumValues() {
        String cacheKey = getCacheKey();
        if (!ENUM_VALUE_CACHE.containsKey(cacheKey)) {
            try {
                Set<Object> validValues = extractEnumValues();
                ENUM_VALUE_CACHE.put(cacheKey, validValues);
                log.debug("枚举值缓存加载完成: key={}, values={}", cacheKey, validValues);
            } catch (Exception e) {
                log.error("枚举值缓存加载失败: enumType={}, fieldName={}",
                    enumType.getSimpleName(), fieldName, e);
            }
        }
    }

    /**
     * 从枚举类型中提取指定字段的所有值
     *
     * @return 有效枚举值集合
     */
    private Set<Object> extractEnumValues() {
        return Arrays.stream(enumType.getEnumConstants())
            .map(enumConstant -> {
                try {
                    return ReflectUtils.invokeGetter(enumConstant, fieldName);
                } catch (Exception e) {
                    log.warn("获取枚举字段值失败: enum={}, fieldName={}",
                        enumConstant, fieldName, e);
                    return null;
                }
            })
            .filter(value -> value != null)
            .collect(Collectors.toSet());
    }

    /**
     * 获取有效枚举值集合（优先从缓存获取）
     *
     * @return 有效枚举值集合
     */
    private Set<Object> getValidEnumValues() {
        String cacheKey = getCacheKey();
        return ENUM_VALUE_CACHE.computeIfAbsent(cacheKey, k -> extractEnumValues());
    }

    /**
     * 生成缓存键
     *
     * @return 缓存键，格式为"enumType-fieldName"
     */
    private String getCacheKey() {
        return enumType.getName() + "-" + fieldName;
    }

    /**
     * 判断值是否为空
     *
     * @param value 待判断的值
     * @return true表示为空，false表示非空
     */
    private boolean isEmptyValue(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String) {
            return StringUtils.isBlank((String) value);
        }
        return false;
    }

    /**
     * 构建自定义错误消息
     *
     * @param context      校验上下文
     * @param invalidValue 无效的输入值
     * @param validValues  有效值集合
     */
    private void buildCustomErrorMessage(ConstraintValidatorContext context, Object invalidValue, Set<Object> validValues) {
        // 禁用默认错误消息
        context.disableDefaultConstraintViolation();

        // 构建详细的错误消息
        String customMessage = String.format(
            "输入值[%s]无效，不在枚举[%s.%s]的有效范围内，有效值：%s",
            invalidValue,
            enumType.getSimpleName(),
            fieldName,
            validValues
        );

        context.buildConstraintViolationWithTemplate(customMessage)
            .addConstraintViolation();
    }

    /**
     * 清理缓存（用于测试或内存管理）
     */
    public static void clearCache() {
        ENUM_VALUE_CACHE.clear();
        log.debug("枚举值缓存已清理");
    }

    /**
     * 获取缓存大小（用于监控）
     *
     * @return 当前缓存的大小
     */
    public static int getCacheSize() {
        return ENUM_VALUE_CACHE.size();
    }

}
