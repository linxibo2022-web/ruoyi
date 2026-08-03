package plus.ruoyi.common.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import plus.ruoyi.common.core.utils.TypeParameterResolver;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TypeParameterResolver使用示例和测试用例
 * <p>
 * 展示了各种复杂泛型场景的解析能力，包括：
 * 1. 基本泛型参数解析
 * 2. 多层继承场景
 * 3. 接口泛型参数解析
 * 4. 复杂类型支持（数组、通配符、边界类型等）
 * 5. 缓存机制验证
 * 6. 便捷方法使用
 *
 * @author 抓蛙师
 */
public class TypeParameterResolverTest {

    @BeforeEach
    void setUp() {
        // 每个测试前清空缓存
        TypeParameterResolver.clearAllCache();
    }

    @AfterEach
    void tearDown() {
        // 每个测试后清空缓存
        TypeParameterResolver.clearAllCache();
    }

    // === 测试用例 ===

    /**
     * 测试基本泛型参数解析
     */
    @Test
    void testBasicTypeParameterResolution() {
        // 测试基本的三层泛型结构
        Map<String, Class<?>> typeMap = TypeParameterResolver.resolveTypeParameters(UserService.class);

        assertEquals(User.class, typeMap.get("T"));
        assertEquals(UserBo.class, typeMap.get("B"));
        assertEquals(UserVo.class, typeMap.get("V"));

        System.out.println("基本泛型解析结果: " + typeMap);
    }

    /**
     * 测试多层继承场景
     */
    @Test
    void testMultiLevelInheritance() {
        Map<String, Class<?>> typeMap = TypeParameterResolver.resolveTypeParameters(OrderService.class);

        // 验证所有层次的泛型参数都被正确解析
        assertEquals(Order.class, typeMap.get("T"));
        assertEquals(OrderBo.class, typeMap.get("B"));
        assertEquals(OrderVo.class, typeMap.get("V"));
        assertEquals(OrderExt.class, typeMap.get("E"));
        assertEquals(Long.class, typeMap.get("ID"));
        assertEquals(OrderVo.class, typeMap.get("DTO"));

        System.out.println("多层继承解析结果: " + typeMap);
    }

    /**
     * 测试数组类型支持
     */
    @Test
    void testArrayTypeSupport() {
        Map<String, Class<?>> typeMap = TypeParameterResolver.resolveTypeParameters(StringArrayService.class);

        // 根据类定义，T 应该是 String，T_ARRAY 应该是 StringVo
        assertEquals(String.class, typeMap.get("T"));
        assertEquals(StringVo.class, typeMap.get("T_ARRAY"));

        // 如果需要验证父类的数组类型参数，可以添加这些检查
        // 注意：父类的参数名是 AbstractService 中定义的，不是 T 和 T_ARRAY
        // 例如，如果 AbstractService 定义为 AbstractService<T_ENTITY, T_BO, T_VO>
        // 那么应该检查这些参数名

        System.out.println("数组类型解析结果: " + typeMap);
        System.out.println("T 类型: " + typeMap.get("T"));
        System.out.println("T_ARRAY 类型: " + typeMap.get("T_ARRAY"));
    }
    /**
     * 测试便捷方法
     */
    @Test
    void testConvenienceMethods() {
        // 测试单个参数解析
        Class<?> entityClass = TypeParameterResolver.resolveTypeParameter(UserService.class, "T");
        assertEquals(User.class, entityClass);

        // 测试批量参数解析
        Map<String, Class<?>> selectedTypes = TypeParameterResolver.resolveTypeParameters(
            OrderService.class, "T", "B", "V"
        );

        assertEquals(3, selectedTypes.size());
        assertEquals(Order.class, selectedTypes.get("T"));
        assertEquals(OrderBo.class, selectedTypes.get("B"));
        assertEquals(OrderVo.class, selectedTypes.get("V"));

        System.out.println("便捷方法解析结果: " + selectedTypes);
    }

    /**
     * 测试缓存机制
     */
    @Test
    void testCacheEfficiency() {
        assertEquals(0, TypeParameterResolver.getCacheSize());

        // 第一次解析 - 应该缓存结果
        long startTime = System.nanoTime();
        Map<String, Class<?>> result1 = TypeParameterResolver.resolveTypeParameters(UserService.class);
        long firstCallTime = System.nanoTime() - startTime;

        assertEquals(1, TypeParameterResolver.getCacheSize());

        // 第二次解析 - 应该从缓存获取
        startTime = System.nanoTime();
        Map<String, Class<?>> result2 = TypeParameterResolver.resolveTypeParameters(UserService.class);
        long secondCallTime = System.nanoTime() - startTime;

        // 验证结果一致性
        assertEquals(result1, result2);

        // 验证缓存效果（第二次调用应该更快）
        assertTrue(secondCallTime < firstCallTime,
            String.format("缓存未生效！第一次: %d ns, 第二次: %d ns", firstCallTime, secondCallTime));

        System.out.println("缓存效果验证:");
        System.out.println("  第一次解析时间: " + firstCallTime + " ns");
        System.out.println("  第二次解析时间: " + secondCallTime + " ns");
        System.out.println("  性能提升: " + (firstCallTime - secondCallTime) + " ns");
        System.out.println("  缓存大小: " + TypeParameterResolver.getCacheSize());
    }

    /**
     * 测试线程安全性
     */
    @Test
    void testThreadSafety() throws InterruptedException {
        final int THREAD_COUNT = 10;
        final int OPERATIONS_PER_THREAD = 100;
        Thread[] threads = new Thread[THREAD_COUNT];

        // 创建多个线程同时访问解析器
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                    Map<String, Class<?>> result = TypeParameterResolver.resolveTypeParameters(UserService.class);
                    assertNotNull(result);
                    assertEquals(User.class, result.get("T"));

                    // 测试便捷方法的线程安全
                    Class<?> entityClass = TypeParameterResolver.resolveTypeParameter(OrderService.class, "T");
                    assertEquals(Order.class, entityClass);
                }
            });
        }

        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }

        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }

        // 验证缓存状态正常
        assertTrue(TypeParameterResolver.getCacheSize() > 0);
        System.out.println("线程安全测试完成，最终缓存大小: " + TypeParameterResolver.getCacheSize());
    }

    /**
     * 测试异常边界情况
     */
    @Test
    void testEdgeCases() {
        // 测试null输入
        Map<String, Class<?>> nullResult = TypeParameterResolver.resolveTypeParameters(null);
        assertTrue(nullResult.isEmpty());

        Class<?> nullParam = TypeParameterResolver.resolveTypeParameter(null, "T");
        assertNull(nullParam);

        Class<?> emptyParam = TypeParameterResolver.resolveTypeParameter(UserService.class, "");
        assertNull(emptyParam);

        // 测试不存在的泛型参数
        Class<?> nonExistentParam = TypeParameterResolver.resolveTypeParameter(UserService.class, "NONEXISTENT");
        assertNull(nonExistentParam);

        System.out.println("边界情况测试通过");
    }

    /**
     * 测试复杂嵌套泛型
     */
    @Test
    void testComplexNestedGenerics() {
        // 定义复杂的嵌套泛型类
        class ComplexService<T extends List<? extends Number>,
            U extends Map<String, ? super Integer>,
            V extends Set<T>> {
        }

        class ConcreteComplexService extends ComplexService<List<Double>, Map<String, Number>, Set<List<Double>>> {
        }

        Map<String, Class<?>> typeMap = TypeParameterResolver.resolveTypeParameters(ConcreteComplexService.class);

        // 验证复杂类型解析（由于类型擦除，这里主要验证基础类型）
        assertNotNull(typeMap.get("T"));
        assertNotNull(typeMap.get("U"));
        assertNotNull(typeMap.get("V"));

        System.out.println("复杂嵌套泛型解析结果: " + typeMap);
    }

    /**
     * 测试泛型数组的各种形式
     */
    @Test
    void testGenericArrayVariations() {
        // 一维数组
        class ArrayService1<T> {
            T[] array;
        }
        class StringArrayService1 extends ArrayService1<String> {
        }

        // 多维数组
        class ArrayService2<T> {
            T[][] matrix;
        }
        class IntegerMatrixService extends ArrayService2<Integer> {
        }

        Map<String, Class<?>> result1 = TypeParameterResolver.resolveTypeParameters(StringArrayService1.class);
        Map<String, Class<?>> result2 = TypeParameterResolver.resolveTypeParameters(IntegerMatrixService.class);

        assertEquals(String.class, result1.get("T"));
        assertEquals(Integer.class, result2.get("T"));

        System.out.println("数组变体解析结果:");
        System.out.println("  一维数组: " + result1);
        System.out.println("  多维数组: " + result2);
    }

    /**
     * 测试缓存管理功能
     */
    @Test
    void testCacheManagement() {
        // 添加一些缓存条目
        TypeParameterResolver.resolveTypeParameters(UserService.class);
        TypeParameterResolver.resolveTypeParameters(OrderService.class);

        assertEquals(2, TypeParameterResolver.getCacheSize());

        // 测试清除单个缓存
        TypeParameterResolver.clearCache(UserService.class);
        assertEquals(1, TypeParameterResolver.getCacheSize());

        // 测试获取缓存统计
        String stats = TypeParameterResolver.getCacheStats();
        assertNotNull(stats);
        assertTrue(stats.contains("OrderService"));

        // 测试清除所有缓存
        TypeParameterResolver.clearAllCache();
        assertEquals(0, TypeParameterResolver.getCacheSize());

        System.out.println("缓存管理测试完成");
        System.out.println("缓存统计信息示例:\n" + stats);
    }

    /**
     * 性能基准测试
     */
    @Test
    void testPerformanceBenchmark() {
        final int WARM_UP_ITERATIONS = 1000;
        final int BENCHMARK_ITERATIONS = 10000;

        // 预热JVM
        for (int i = 0; i < WARM_UP_ITERATIONS; i++) {
            TypeParameterResolver.clearAllCache();
            TypeParameterResolver.resolveTypeParameters(UserService.class);
        }

        // 基准测试 - 无缓存场景
        TypeParameterResolver.clearAllCache();
        long startTime = System.nanoTime();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            TypeParameterResolver.clearAllCache();
            TypeParameterResolver.resolveTypeParameters(UserService.class);
        }
        long noCacheTime = System.nanoTime() - startTime;

        // 基准测试 - 有缓存场景
        TypeParameterResolver.clearAllCache();
        // 预先缓存
        TypeParameterResolver.resolveTypeParameters(UserService.class);

        startTime = System.nanoTime();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            TypeParameterResolver.resolveTypeParameters(UserService.class);
        }
        long cachedTime = System.nanoTime() - startTime;

        double speedupRatio = (double) noCacheTime / cachedTime;

        System.out.println("性能基准测试结果:");
        System.out.println("  无缓存总时间: " + noCacheTime / 1_000_000 + " ms");
        System.out.println("  有缓存总时间: " + cachedTime / 1_000_000 + " ms");
        System.out.println("  缓存加速比: " + String.format("%.2fx", speedupRatio));
        System.out.println("  平均每次解析时间（无缓存）: " + noCacheTime / BENCHMARK_ITERATIONS + " ns");
        System.out.println("  平均每次解析时间（有缓存）: " + cachedTime / BENCHMARK_ITERATIONS + " ns");

        // 验证缓存确实提升了性能
        assertTrue(speedupRatio > 2, "缓存应该显著提升性能，实际加速比: " + speedupRatio);
    }

    /**
     * 使用示例演示
     */
    @Test
    void testUsageExamples() {
        System.out.println("=== TypeParameterResolver 使用示例 ===\n");

        // 示例1: 基本使用
        System.out.println("1. 基本使用:");
        Map<String, Class<?>> basicTypes = TypeParameterResolver.resolveTypeParameters(UserService.class);
        basicTypes.forEach((name, clazz) ->
            System.out.println("   " + name + " -> " + clazz.getSimpleName()));

        // 示例2: 便捷方法
        System.out.println("\n2. 便捷方法:");
        Class<?> entityType = TypeParameterResolver.resolveTypeParameter(UserService.class, "T");
        System.out.println("   实体类型: " + entityType.getSimpleName());

        // 示例3: 批量解析
        System.out.println("\n3. 批量解析:");
        Map<String, Class<?>> selectedTypes = TypeParameterResolver.resolveTypeParameters(
            OrderService.class, "T", "B", "V");
        selectedTypes.forEach((name, clazz) ->
            System.out.println("   " + name + " -> " + clazz.getSimpleName()));

        // 示例4: 复杂继承结构
        System.out.println("\n4. 复杂继承结构:");
        Map<String, Class<?>> complexTypes = TypeParameterResolver.resolveTypeParameters(OrderService.class);
        complexTypes.forEach((name, clazz) ->
            System.out.println("   " + name + " -> " + clazz.getSimpleName()));

        // 示例5: 缓存状态
        System.out.println("\n5. 缓存状态:");
        System.out.println("   " + TypeParameterResolver.getCacheStats());

        System.out.println("\n=== 示例演示完成 ===");
    }

    // === 测试用的类层次结构 ===

    /**
     * 基础泛型接口
     */
    interface BaseRepository<T, ID> {
        T findById(ID id);

        List<T> findAll();
    }

    /**
     * 扩展的泛型接口
     */
    interface ExtendedRepository<T, ID, DTO> extends BaseRepository<T, ID> {
        DTO convertToDto(T entity);

        List<DTO> findAllAsDto();
    }

    /**
     * 泛型基类
     */
    static abstract class AbstractService<T, B, V> {
        abstract T getEntity(Long id);

        abstract V getVo(Long id);

        abstract Long save(B bo);
    }

    /**
     * 中间继承层
     */
    static abstract class MiddleService<T, B, V, E> extends AbstractService<T, B, V> {
        abstract E getExtended(Long id);
    }

    /**
     * 具体实现类 - 基本场景
     */
    static class UserService extends AbstractService<User, UserBo, UserVo> {
        @Override
        User getEntity(Long id) {
            return new User();
        }

        @Override
        UserVo getVo(Long id) {
            return new UserVo();
        }

        @Override
        Long save(UserBo bo) {
            return 1L;
        }
    }

    /**
     * 具体实现类 - 多层继承场景
     */
    static class OrderService extends MiddleService<Order, OrderBo, OrderVo, OrderExt>
        implements ExtendedRepository<Order, Long, OrderVo> {

        @Override
        Order getEntity(Long id) {
            return new Order();
        }

        @Override
        OrderVo getVo(Long id) {
            return new OrderVo();
        }

        @Override
        Long save(OrderBo bo) {
            return 1L;
        }

        @Override
        OrderExt getExtended(Long id) {
            return new OrderExt();
        }

        @Override
        public Order findById(Long id) {
            return new Order();
        }

        @Override
        public List<Order> findAll() {
            return List.of();
        }

        @Override
        public OrderVo convertToDto(Order entity) {
            return new OrderVo();
        }

        @Override
        public List<OrderVo> findAllAsDto() {
            return List.of();
        }
    }

    /**
     * 复杂泛型场景 - 数组类型
     */
    static class ArrayService<T, T_ARRAY> extends AbstractService<T[], T, T_ARRAY[]> {
        @Override
        T[] getEntity(Long id) {
            return null;
        }

        @Override
        T_ARRAY[] getVo(Long id) {
            return null;
        }

        @Override
        Long save(T bo) {
            return 1L;
        }
    }

    /**
     * 具体的数组服务实现
     */
    static class StringArrayService extends ArrayService<String, StringVo> {
    }


    // 测试用的简单类
    static class User {
    }

    static class UserBo {
    }

    static class UserVo {
    }

    static class Order {
    }

    static class OrderBo {
    }

    static class OrderVo {
    }

    static class OrderExt {
    }

    static class StringVo {
    }


}
