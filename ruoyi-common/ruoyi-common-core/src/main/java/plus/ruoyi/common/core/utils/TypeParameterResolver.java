package plus.ruoyi.common.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.HashMap;

/**
 * 泛型参数解析工具类 (TypeParameterResolver)
 * <p>
 * 参考Netty的TypeParameterMatcher实现，提供安全、高效的泛型参数解析能力。
 * 解决了硬编码数组索引在继承扩展时导致的索引错位问题。
 * <p>
 * 核心特性：
 * 1. 缓存机制：避免重复解析，提高性能
 * 2. 多层继承支持：递归查找泛型参数定义
 * 3. 防御性编程：完善的边界条件检查
 * 4. 独立组件：可在项目中任意位置复用
 * 5. 类型支持全面：支持Class、ParameterizedType、GenericArrayType、TypeVariable、WildcardType
 * 6. 线程安全：使用读写锁保证高并发场景下的安全性
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 解析当前类的泛型参数
 * Map<String, Class<?>> typeMap = TypeParameterResolver.resolveTypeParameters(this.getClass());
 * Class<?> entityClass = typeMap.get("T");
 * Class<?> boClass = typeMap.get("B");
 *
 * // 便捷方法：直接获取特定泛型参数
 * Class<?> entityClass = TypeParameterResolver.resolveTypeParameter(MyService.class, "T");
 * }</pre>
 *
 * @author 抓蛙师
 */
public class TypeParameterResolver {

    private static final Logger log = LoggerFactory.getLogger(TypeParameterResolver.class);

    /**
     * 泛型参数解析结果缓存
     * Key: 目标类的Class对象
     * Value: 泛型变量名 -> 实际类型的映射
     */
    private static final Map<Class<?>, Map<String, Class<?>>> TYPE_CACHE = new ConcurrentHashMap<>();

    /**
     * 读写锁，保证缓存操作的线程安全
     * 读操作（缓存命中）使用读锁，写操作（缓存更新）使用写锁
     */
    private static final ReentrantReadWriteLock CACHE_LOCK = new ReentrantReadWriteLock();

    /**
     * 解析指定类的泛型参数映射（带缓存）
     * <p>
     * 核心入口方法，优先从缓存获取结果，缓存未命中时执行解析并缓存结果
     *
     * @param targetClass 目标类
     * @return 泛型变量名 -> 实际类型的映射，解析失败返回空Map
     */
    public static Map<String, Class<?>> resolveTypeParameters(Class<?> targetClass) {
        if (targetClass == null) {
            return new HashMap<>();
        }

        // 读锁：优先从缓存获取
        CACHE_LOCK.readLock().lock();
        try {
            Map<String, Class<?>> cachedResult = TYPE_CACHE.get(targetClass);
            if (cachedResult != null) {
                return new HashMap<>(cachedResult); // 返回副本，避免外部修改缓存
            }
        } finally {
            CACHE_LOCK.readLock().unlock();
        }

        // 写锁：缓存未命中，执行解析
        CACHE_LOCK.writeLock().lock();
        try {
            // 双重检查：避免重复解析
            Map<String, Class<?>> cachedResult = TYPE_CACHE.get(targetClass);
            if (cachedResult != null) {
                return new HashMap<>(cachedResult);
            }

            // 执行解析
            Map<String, Class<?>> typeMap = doResolveTypeParameters(targetClass);

            // 缓存解析结果（即使为空也缓存，避免重复解析）
            TYPE_CACHE.put(targetClass, new HashMap<>(typeMap));

            return typeMap;
        } finally {
            CACHE_LOCK.writeLock().unlock();
        }
    }

    /**
     * 便捷方法：直接解析指定类的特定泛型参数
     *
     * @param targetClass   目标类
     * @param typeParamName 泛型参数名称
     * @return 对应的Class对象，解析失败返回null
     */
    public static Class<?> resolveTypeParameter(Class<?> targetClass, String typeParamName) {
        if (targetClass == null || typeParamName == null || typeParamName.isEmpty()) {
            return null;
        }

        Map<String, Class<?>> typeMap = resolveTypeParameters(targetClass);
        return typeMap.get(typeParamName);
    }

    /**
     * 便捷方法：自动获取调用者类的特定泛型参数
     * <p>
     * 注意：此方法会通过堆栈信息自动获取调用者类，性能相对较低，
     * 建议在性能敏感场景使用 {@link #resolveTypeParameter(Class, String)} 方法
     *
     * @param typeParamName 泛型参数名称
     * @return 对应的Class对象，解析失败返回null
     */
    public static Class<?> resolveTypeParameter(String typeParamName) {
        if (typeParamName == null || typeParamName.isEmpty()) {
            return null;
        }

        // 获取调用此方法的类的Class对象
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length < 3) {
            log.warn("无法获取调用者类信息，堆栈深度不足");
            return null;
        }

        try {
            Class<?> callerClass = Class.forName(stackTrace[2].getClassName());
            return resolveTypeParameter(callerClass, typeParamName);
        } catch (ClassNotFoundException e) {
            log.warn("无法加载调用者类: {}", stackTrace[2].getClassName(), e);
            return null;
        }
    }

    /**
     * 批量解析多个泛型参数
     *
     * @param targetClass    目标类
     * @param typeParamNames 泛型参数名称数组
     * @return 泛型参数名 -> Class对象的映射
     */
    public static Map<String, Class<?>> resolveTypeParameters(Class<?> targetClass, String... typeParamNames) {
        Map<String, Class<?>> result = new HashMap<>();
        if (targetClass == null || typeParamNames == null || typeParamNames.length == 0) {
            return result;
        }

        Map<String, Class<?>> allTypeMap = resolveTypeParameters(targetClass);
        for (String typeParamName : typeParamNames) {
            if (typeParamName != null && !typeParamName.isEmpty()) {
                Class<?> clazz = allTypeMap.get(typeParamName);
                if (clazz != null) {
                    result.put(typeParamName, clazz);
                }
            }
        }
        return result;
    }

    /**
     * 执行泛型参数解析的核心逻辑
     * <p>
     * 1. 从目标类开始，向上遍历继承链
     * 2. 对每个泛型父类/接口，建立参数名到具体类型的映射
     * 3. 只处理能够确定具体类型的参数，跳过类型变量
     * 4. 优先保留最底层（最具体）的类型映射
     *
     * @param targetClass 目标类
     * @return 泛型变量名 -> 实际类型的映射
     */
    private static Map<String, Class<?>> doResolveTypeParameters(Class<?> targetClass) {
        Map<String, Class<?>> result = new HashMap<>();

        try {
            log.debug("开始解析类: {}", targetClass.getName());

            // 从当前类开始，向上遍历继承链
            Class<?> currentClass = targetClass;

            while (currentClass != null && currentClass != Object.class) {
                log.debug("当前处理类: {}", currentClass.getName());

                // 处理父类的泛型参数
                Type genericSuperclass = currentClass.getGenericSuperclass();
                if (genericSuperclass instanceof ParameterizedType pt) {
                    Class<?> rawSuperclass = (Class<?>) pt.getRawType();
                    log.debug("发现泛型父类: {} -> {}", currentClass.getSimpleName(), rawSuperclass.getSimpleName());

                    // 解析父类的泛型参数
                    extractTypeParameters(rawSuperclass, pt, result);

                    // 继续处理父类
                    currentClass = rawSuperclass;
                } else {
                    // 非泛型父类，继续向上
                    currentClass = currentClass.getSuperclass();
                }
            }

            // 重新从目标类开始处理接口
            processAllInterfaces(targetClass, result);

            log.debug("解析完成，结果: {}", result);

        } catch (Exception e) {
            log.warn("解析泛型参数失败: {}", targetClass.getName(), e);
        }

        return result;
    }

    /**
     * 从指定的参数化类型中提取泛型参数映射
     */
    private static void extractTypeParameters(Class<?> rawType, ParameterizedType parameterizedType, Map<String, Class<?>> result) {
        TypeVariable<?>[] typeParams = rawType.getTypeParameters();
        Type[] actualTypes = parameterizedType.getActualTypeArguments();

        for (int i = 0; i < Math.min(typeParams.length, actualTypes.length); i++) {
            String paramName = typeParams[i].getName();
            Type actualType = actualTypes[i];

            log.debug("处理泛型参数: {} -> {} ({})", paramName, actualType, actualType.getClass().getSimpleName());

            Class<?> actualClass = extractActualClass(actualType);
            if (actualClass != null) {
                // 只有当还没有这个参数的映射时才添加（保持最底层的映射）
                if (!result.containsKey(paramName)) {
                    result.put(paramName, actualClass);
                    log.debug("成功映射: {} -> {}", paramName, actualClass.getName());
                } else {
                    log.debug("跳过已存在的映射: {} -> {}", paramName, result.get(paramName).getName());
                }
            } else {
                log.debug("无法解析类型: {} -> {}", paramName, actualType);
            }
        }
    }

    /**
     * 处理所有层次的接口
     */
    private static void processAllInterfaces(Class<?> targetClass, Map<String, Class<?>> result) {
        Class<?> currentClass = targetClass;

        while (currentClass != null && currentClass != Object.class) {
            processDirectInterfaces(currentClass, result);
            currentClass = currentClass.getSuperclass();
        }
    }

    /**
     * 处理当前类直接实现的接口
     */
    private static void processDirectInterfaces(Class<?> currentClass, Map<String, Class<?>> result) {
        Type[] genericInterfaces = currentClass.getGenericInterfaces();
        if (genericInterfaces == null) {
            return;
        }

        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType pt) {
                Class<?> rawInterface = (Class<?>) pt.getRawType();
                log.debug("发现泛型接口: {} -> {}", currentClass.getSimpleName(), rawInterface.getSimpleName());

                extractTypeParameters(rawInterface, pt, result);
            }
        }
    }

    /**
     * 从Type对象中提取实际的Class对象
     * <p>
     * 支持多种Type类型的解析：
     * 1. Class：直接返回
     * 2. ParameterizedType：返回原始类型
     * 3. GenericArrayType：处理泛型数组类型
     * 4. TypeVariable：尝试解析边界，无法解析时返回null
     * 5. WildcardType：处理通配符类型
     *
     * @param type Type对象
     * @return 对应的Class对象，无法解析返回null
     */
    private static Class<?> extractActualClass(Type type) {
        if (type == null) {
            return null;
        }

        log.debug("解析Type: {}", type);

        // 1. 普通Class类型
        if (type instanceof Class) {
            log.debug("Class类型: {}", type);
            return (Class<?>) type;
        }

        // 2. 参数化类型（如 List<String>）
        if (type instanceof ParameterizedType parameterizedType) {
            Type rawType = parameterizedType.getRawType();
            log.debug("参数化类型: {} -> rawType: {}", type, rawType);
            return rawType instanceof Class ? (Class<?>) rawType : null;
        }

        // 3. 泛型数组类型（如 T[]）
        if (type instanceof GenericArrayType genericArrayType) {
            Type componentType = genericArrayType.getGenericComponentType();
            log.debug("泛型数组类型: {} -> componentType: {}", type, componentType);
            Class<?> componentClass = extractActualClass(componentType);
            if (componentClass != null) {
                // 创建对应的数组类型
                try {
                    Class<?> arrayClass = Array.newInstance(componentClass, 0).getClass();
                    log.debug("创建数组类型成功: {}", arrayClass);
                    return arrayClass;
                } catch (Exception e) {
                    log.debug("创建数组类型失败: {}", componentClass.getName(), e);
                    return null;
                }
            }
        }

        // 4. 类型变量（如 T, E, K, V）
        if (type instanceof TypeVariable<?> typeVariable) {
            log.debug("类型变量: {}", typeVariable.getName());

            // 尝试解析边界类型
            Type[] bounds = typeVariable.getBounds();
            if (bounds != null && bounds.length > 0) {
                // 取第一个边界作为实际类型
                Class<?> boundClass = extractActualClass(bounds[0]);
                log.debug("类型变量边界: {} -> {}", typeVariable.getName(), boundClass);
                if (boundClass != null && boundClass != Object.class) {
                    return boundClass;
                }
            }

            // 关键修复：类型变量无法解析时返回null，而不是Object.class
            // 这样可以避免将泛型参数错误地映射为Object
            // 调用者会跳过null值，不会添加到最终的映射中
            log.debug("类型变量无法解析，返回null: {}", typeVariable.getName());
            return null;
        }

        // 5. 通配符类型（如 ? extends Number, ? super Integer）
        if (type instanceof WildcardType wildcardType) {
            log.debug("通配符类型: {}", type);

            // 优先处理上界
            Type[] upperBounds = wildcardType.getUpperBounds();
            if (upperBounds != null && upperBounds.length > 0) {
                Class<?> upperBoundClass = extractActualClass(upperBounds[0]);
                if (upperBoundClass != null) {
                    log.debug("通配符上界: {}", upperBoundClass);
                    return upperBoundClass;
                }
            }

            // 处理下界
            Type[] lowerBounds = wildcardType.getLowerBounds();
            if (lowerBounds != null && lowerBounds.length > 0) {
                Class<?> lowerBoundClass = extractActualClass(lowerBounds[0]);
                if (lowerBoundClass != null) {
                    log.debug("通配符下界: {}", lowerBoundClass);
                    return lowerBoundClass;
                }
            }

            // 对于无法确定的通配符，返回Object.class作为兜底
            return Object.class;
        }

        // 未知类型，返回null
        log.debug("未知的Type类型: {}", type.getClass().getName());
        return null;
    }

    // === 缓存管理方法 ===

    /**
     * 清除指定类的缓存
     *
     * @param targetClass 目标类
     */
    public static void clearCache(Class<?> targetClass) {
        if (targetClass == null) {
            return;
        }

        CACHE_LOCK.writeLock().lock();
        try {
            TYPE_CACHE.remove(targetClass);
            log.debug("已清除类 {} 的泛型参数缓存", targetClass.getName());
        } finally {
            CACHE_LOCK.writeLock().unlock();
        }
    }

    /**
     * 清除所有缓存（用于测试或内存清理）
     * <p>
     * 在单元测试或需要重新加载类定义时使用
     */
    public static void clearAllCache() {
        CACHE_LOCK.writeLock().lock();
        try {
            int cacheSize = TYPE_CACHE.size();
            TYPE_CACHE.clear();
            log.info("已清除所有泛型参数缓存，共 {} 个条目", cacheSize);
        } finally {
            CACHE_LOCK.writeLock().unlock();
        }
    }

    /**
     * 获取缓存大小（用于监控）
     *
     * @return 当前缓存的类数量
     */
    public static int getCacheSize() {
        CACHE_LOCK.readLock().lock();
        try {
            return TYPE_CACHE.size();
        } finally {
            CACHE_LOCK.readLock().unlock();
        }
    }

    /**
     * 获取缓存统计信息（用于监控和调试）
     *
     * @return 缓存统计信息的字符串表示
     */
    public static String getCacheStats() {
        CACHE_LOCK.readLock().lock();
        try {
            StringBuilder stats = new StringBuilder();
            stats.append("TypeParameterResolver缓存统计:\n");
            stats.append("  - 缓存条目数: ").append(TYPE_CACHE.size()).append("\n");
            stats.append("  - 缓存类列表: ");
            TYPE_CACHE.keySet().forEach(clazz ->
                stats.append(clazz.getSimpleName()).append(" ")
            );
            return stats.toString();
        } finally {
            CACHE_LOCK.readLock().unlock();
        }
    }
}
