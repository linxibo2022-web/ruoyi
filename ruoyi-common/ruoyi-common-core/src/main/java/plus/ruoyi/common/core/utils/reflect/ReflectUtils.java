package plus.ruoyi.common.core.utils.reflect;

import cn.hutool.core.util.ReflectUtil;
import plus.ruoyi.common.core.utils.StringUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;

/**
 * 反射工具类
 * <p>提供调用getter/setter方法、访问私有变量、调用私有方法、获取泛型类型Class、被AOP过的真实类等工具函数</p>
 * <p>基于HuTool的ReflectUtil进行扩展，增加了对多级属性访问的支持</p>
 *
 * @author Lion Li
 */
@SuppressWarnings("rawtypes")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReflectUtils extends ReflectUtil {

    /**
     * Setter方法前缀
     */
    private static final String SETTER_PREFIX = "set";

    /**
     * Getter方法前缀
     */
    private static final String GETTER_PREFIX = "get";

    /**
     * 属性分隔符
     */
    private static final String PROPERTY_SEPARATOR = ".";

    /**
     * 调用Getter方法获取属性值
     * <p>示例：{@code String name = ReflectUtils.invokeGetter(user, "profile.name")}</p>
     * <p>支持多级属性访问，使用点号分隔，如：user.profile.name</p>
     *
     * @param <E>          返回值类型
     * @param obj          目标对象，不能为null
     * @param propertyName 属性名称，支持多级访问（如：user.profile.name）
     * @return 属性值，可能为null
     * @throws RuntimeException 当对象为null、属性不存在或访问失败时抛出
     */
    @SuppressWarnings("unchecked")
    public static <E> E invokeGetter(Object obj, String propertyName) {
        if (obj == null) {
            throw new IllegalArgumentException("目标对象不能为null");
        }
        if (StringUtils.isBlank(propertyName)) {
            throw new IllegalArgumentException("属性名称不能为空");
        }

        Object currentObject = obj;
        for (String name : StringUtils.split(propertyName, PROPERTY_SEPARATOR)) {
            if (currentObject == null) {
                return null;
            }
            String getterMethodName = GETTER_PREFIX + StringUtils.capitalize(name);
            currentObject = invoke(currentObject, getterMethodName);
        }
        return (E) currentObject;
    }

    /**
     * 调用Setter方法设置属性值
     * <p>示例：{@code ReflectUtils.invokeSetter(user, "profile.name", "张三")}</p>
     * <p>支持多级属性访问，使用点号分隔，如：user.profile.name</p>
     * <p>注意：仅根据方法名匹配setter方法，不进行参数类型校验</p>
     *
     * @param <E>          属性值类型
     * @param obj          目标对象，不能为null
     * @param propertyName 属性名称，支持多级访问（如：user.profile.name）
     * @param value        要设置的属性值
     * @throws RuntimeException 当对象为null、属性不存在或设置失败时抛出
     */
    public static <E> void invokeSetter(Object obj, String propertyName, E value) {
        if (obj == null) {
            throw new IllegalArgumentException("目标对象不能为null");
        }
        if (StringUtils.isBlank(propertyName)) {
            throw new IllegalArgumentException("属性名称不能为空");
        }

        Object currentObject = obj;
        String[] names = StringUtils.split(propertyName, PROPERTY_SEPARATOR);

        for (int i = 0; i < names.length; i++) {
            if (currentObject == null) {
                throw new IllegalStateException("中间对象为null，无法继续设置属性：" + names[i]);
            }

            if (i < names.length - 1) {
                // 获取中间对象
                String getterMethodName = GETTER_PREFIX + StringUtils.capitalize(names[i]);
                currentObject = invoke(currentObject, getterMethodName);
            } else {
                // 设置最终属性值
                String setterMethodName = SETTER_PREFIX + StringUtils.capitalize(names[i]);
                Method method = getMethodByName(currentObject.getClass(), setterMethodName);
                invoke(currentObject, method, value);
            }
        }
    }

    /**
     * 安全调用Getter方法获取属性值
     * <p>示例：{@code String name = ReflectUtils.getPropertySafely(user, "profile.name")}</p>
     * <p>与invokeGetter的区别：遇到null对象时返回null而不抛出异常</p>
     *
     * @param <E>          返回值类型
     * @param obj          目标对象，可以为null
     * @param propertyName 属性名称，支持多级访问
     * @return 属性值，如果任何中间对象为null则返回null
     */
    @SuppressWarnings("unchecked")
    public static <E> E getPropertySafely(Object obj, String propertyName) {
        if (obj == null || StringUtils.isBlank(propertyName)) {
            return null;
        }

        try {
            Object currentObject = obj;
            for (String name : StringUtils.split(propertyName, PROPERTY_SEPARATOR)) {
                if (currentObject == null) {
                    return null;
                }
                String getterMethodName = GETTER_PREFIX + StringUtils.capitalize(name);
                currentObject = invoke(currentObject, getterMethodName);
            }
            return (E) currentObject;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 检查对象是否具有指定的属性路径
     * <p>示例：{@code boolean hasProperty = ReflectUtils.hasProperty(user, "profile.name")}</p>
     *
     * @param obj          目标对象
     * @param propertyName 属性名称，支持多级访问
     * @return true表示具有该属性路径，false表示不具有
     */
    public static boolean hasProperty(Object obj, String propertyName) {
        if (obj == null || StringUtils.isBlank(propertyName)) {
            return false;
        }

        try {
            Class<?> currentClass = obj.getClass();
            String[] names = StringUtils.split(propertyName, PROPERTY_SEPARATOR);

            for (String name : names) {
                String getterMethodName = GETTER_PREFIX + StringUtils.capitalize(name);
                Method method = getMethodByName(currentClass, getterMethodName);
                if (method == null) {
                    return false;
                }
                currentClass = method.getReturnType();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
