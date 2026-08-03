package plus.ruoyi.common.mybatis.helper;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.context.model.SaStorage;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.plugins.IgnoreStrategy;
import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import plus.ruoyi.common.core.utils.reflect.ReflectUtils;
import plus.ruoyi.common.mybatis.annotation.DataPermission;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.function.Supplier;

/**
 * 数据权限助手类
 * <p>
 * 提供数据权限相关的工具方法，用于管理和控制SQL查询的数据权限过滤。
 * 主要功能包括权限注解管理、上下文变量存储、权限忽略控制等。
 * <p>
 * 核心功能：
 * <ul>
 *   <li><b>权限注解管理</b>：存储和获取当前执行方法的数据权限注解信息</li>
 *   <li><b>上下文变量</b>：提供线程安全的变量存储，支持权限过滤时的参数传递</li>
 *   <li><b>权限忽略控制</b>：支持临时禁用数据权限过滤，适用于特殊业务场景</li>
 *   <li><b>可重入支持</b>：支持嵌套调用的权限忽略操作</li>
 * </ul>
 * <p>
 * 典型使用场景：
 * <pre>{@code
 * // 场景1：临时忽略数据权限执行查询
 * List<User> allUsers = DataPermissionHelper.ignore(() -> {
 *     return userMapper.selectList(null);
 * });
 *
 * // 场景2：设置权限过滤参数
 * DataPermissionHelper.setVariable("deptId", currentUser.getDeptId());
 * List<User> deptUsers = userMapper.selectList(null);
 *
 * // 场景3：手动控制权限忽略
 * DataPermissionHelper.enableIgnore();
 * try {
 *     // 执行不需要数据权限过滤的操作
 *     doSomething();
 * } finally {
 *     DataPermissionHelper.disableIgnore();
 * }
 * }</pre>
 *
 * @author Lion Li
 * @version 3.5.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("unchecked")
public class DataPermissionHelper {

    /**
     * 数据权限上下文在SaStorage中的存储键
     * <p>
     * 用于在Sa-Token的存储容器中标识数据权限相关的上下文信息
     */
    private static final String DATA_PERMISSION_KEY = "data:permission";

    /**
     * 可重入忽略栈的线程本地存储
     * <p>
     * 使用栈结构支持嵌套的权限忽略操作，确保在复杂调用链中正确管理忽略状态。
     * 每个线程维护独立的栈，避免多线程间的相互影响。
     */
    private static final ThreadLocal<Stack<Integer>> REENTRANT_IGNORE = ThreadLocal.withInitial(Stack::new);

    /**
     * 当前执行方法权限注解的线程本地缓存
     * <p>
     * 存储当前正在执行的Mapper方法上的 {@link DataPermission} 注解信息，
     * 供数据权限拦截器使用以确定具体的权限过滤规则。
     */
    private static final ThreadLocal<DataPermission> PERMISSION_CACHE = new ThreadLocal<>();

    /**
     * 获取当前执行Mapper方法的权限注解
     * <p>
     * 在数据权限拦截器中调用，用于获取当前方法上定义的权限规则。
     * 如果方法上没有 {@link DataPermission} 注解，则返回 null。
     *
     * @return 当前方法的数据权限注解，如果未设置则返回 null
     */
    public static DataPermission getPermission() {
        return PERMISSION_CACHE.get();
    }

    /**
     * 设置当前执行Mapper方法的权限注解
     * <p>
     * 通常在数据权限拦截器中调用，用于缓存从方法上解析出的权限注解信息，
     * 以便后续的权限过滤逻辑可以访问这些配置。
     *
     * @param dataPermission 数据权限注解对象
     */
    public static void setPermission(DataPermission dataPermission) {
        PERMISSION_CACHE.set(dataPermission);
    }

    /**
     * 清除当前线程的权限注解缓存
     * <p>
     * 在方法执行完成后调用，避免线程复用时出现权限信息泄露。
     * 通常在数据权限拦截器的finally块中调用。
     */
    public static void removePermission() {
        PERMISSION_CACHE.remove();
    }

    /**
     * 从权限上下文中获取指定键的变量值
     * <p>
     * 支持在数据权限过滤过程中传递参数，如当前用户的部门ID、角色信息等。
     * 这些变量可以在权限SQL构建时使用。
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 获取当前用户部门ID
     * Long deptId = DataPermissionHelper.getVariable("deptId");
     *
     * // 获取用户角色列表
     * List<String> roles = DataPermissionHelper.getVariable("userRoles");
     * }</pre>
     *
     * @param key 变量的键名
     * @param <T> 变量值的类型，支持泛型自动转换
     * @return 指定键的变量值，如果不存在则返回 null
     */
    public static <T> T getVariable(String key) {
        Map<String, Object> context = getContext();
        return (T) context.get(key);
    }

    /**
     * 向权限上下文中设置变量值
     * <p>
     * 用于在权限过滤执行前设置必要的参数，这些参数可以在权限SQL构建时使用。
     * 常见的变量包括：用户ID、部门ID、角色信息、数据范围等。
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 设置当前用户信息
     * DataPermissionHelper.setVariable("userId", currentUser.getId());
     * DataPermissionHelper.setVariable("deptId", currentUser.getDeptId());
     * DataPermissionHelper.setVariable("dataScope", currentUser.getDataScope());
     * }</pre>
     *
     * @param key   变量的键名
     * @param value 变量值，可以是任意类型的对象
     */
    public static void setVariable(String key, Object value) {
        Map<String, Object> context = getContext();
        context.put(key, value);
    }

    /**
     * 获取数据权限上下文容器
     * <p>
     * 返回用于存储权限相关变量的Map容器。该容器基于Sa-Token的存储机制，
     * 确保在同一个请求生命周期内数据的一致性和线程安全性。
     * <p>
     * 如果上下文不存在，会自动创建一个新的HashMap并存储到SaStorage中。
     *
     * @return 数据权限上下文Map容器，永不为null
     * @throws NullPointerException 如果从SaStorage中获取的数据类型不是Map时抛出
     */
    public static Map<String, Object> getContext() {
        SaStorage saStorage = SaHolder.getStorage();
        Object attribute = saStorage.get(DATA_PERMISSION_KEY);

        // 如果上下文不存在，创建新的Map容器
        if (ObjectUtil.isNull(attribute)) {
            saStorage.set(DATA_PERMISSION_KEY, new HashMap<>());
            attribute = saStorage.get(DATA_PERMISSION_KEY);
        }

        // 类型检查，确保数据完整性
        if (attribute instanceof Map map) {
            return map;
        }
        throw new NullPointerException("数据权限上下文类型异常，期望Map类型但实际为: " + attribute.getClass().getName());
    }

    /**
     * 获取MyBatis-Plus拦截器的忽略策略对象
     * <p>
     * 通过反射访问InterceptorIgnoreHelper中的线程本地变量，
     * 获取当前线程的拦截器忽略策略配置。
     * <p>
     * 此方法为内部使用，用于支持权限忽略功能的实现。
     *
     * @return 当前线程的忽略策略对象，如果不存在则返回 null
     */
    private static IgnoreStrategy getIgnoreStrategy() {
        // 使用反射获取InterceptorIgnoreHelper中的IGNORE_STRATEGY_LOCAL字段
        Object ignoreStrategyLocal = ReflectUtils.getStaticFieldValue(
            ReflectUtils.getField(InterceptorIgnoreHelper.class, "IGNORE_STRATEGY_LOCAL")
        );

        if (ignoreStrategyLocal instanceof ThreadLocal<?> IGNORE_STRATEGY_LOCAL) {
            if (IGNORE_STRATEGY_LOCAL.get() instanceof IgnoreStrategy ignoreStrategy) {
                return ignoreStrategy;
            }
        }
        return null;
    }

    /**
     * 开启数据权限忽略模式
     * <p>
     * 调用此方法后，后续的数据库操作将跳过数据权限过滤，直到调用 {@link #disableIgnore()} 方法。
     * 支持可重入调用，即可以在已开启忽略的情况下再次调用此方法。
     * <p>
     * <b>重要提醒</b>：使用此方法后必须确保在适当的时机调用 {@link #disableIgnore()} 方法，
     * 建议使用try-finally结构或者使用 {@link #ignore(Runnable)} 方法。
     * <p>
     * 适用场景：
     * <ul>
     *   <li>系统管理员查看全量数据</li>
     *   <li>数据统计和报表生成</li>
     *   <li>数据迁移和批量处理</li>
     *   <li>特殊业务逻辑需要跨越权限边界</li>
     * </ul>
     */
    private static void enableIgnore() {
        IgnoreStrategy ignoreStrategy = getIgnoreStrategy();

        if (ObjectUtil.isNull(ignoreStrategy)) {
            // 如果当前没有忽略策略，创建一个新的并设置数据权限忽略
            InterceptorIgnoreHelper.handle(IgnoreStrategy.builder().dataPermission(true).build());
        } else {
            // 如果已存在忽略策略，只需要设置数据权限忽略标志
            ignoreStrategy.setDataPermission(true);
        }

        // 维护可重入栈，支持嵌套调用
        Stack<Integer> reentrantStack = REENTRANT_IGNORE.get();
        reentrantStack.push(reentrantStack.size() + 1);
    }

    /**
     * 关闭数据权限忽略模式
     * <p>
     * 与 {@link #enableIgnore()} 方法成对使用，用于恢复数据权限过滤。
     * 支持可重入调用，只有当所有的enableIgnore调用都对应调用了disableIgnore后，
     * 才会真正关闭权限忽略。
     * <p>
     * 方法会智能判断是否需要完全清除忽略策略：
     * <ul>
     *   <li>如果没有其他类型的忽略策略且是最后一层调用，则完全清除忽略策略</li>
     *   <li>如果存在其他忽略策略或还有嵌套调用，则只关闭数据权限忽略</li>
     * </ul>
     */
    private static void disableIgnore() {
        IgnoreStrategy ignoreStrategy = getIgnoreStrategy();

        if (ObjectUtil.isNotNull(ignoreStrategy)) {
            // 检查是否还有其他类型的忽略策略
            boolean noOtherIgnoreStrategy = !Boolean.TRUE.equals(ignoreStrategy.getDynamicTableName())
                && !Boolean.TRUE.equals(ignoreStrategy.getBlockAttack())
                && !Boolean.TRUE.equals(ignoreStrategy.getIllegalSql())
                && !Boolean.TRUE.equals(ignoreStrategy.getTenantLine())
                && CollectionUtil.isEmpty(ignoreStrategy.getOthers());

            // 处理可重入栈
            Stack<Integer> reentrantStack = REENTRANT_IGNORE.get();
            boolean isLastLevel = reentrantStack.isEmpty() || reentrantStack.pop() == 1;

            if (noOtherIgnoreStrategy && isLastLevel) {
                // 没有其他忽略策略且是最后一层调用，完全清除忽略策略
                InterceptorIgnoreHelper.clearIgnoreStrategy();
            } else if (isLastLevel) {
                // 还有其他忽略策略但是最后一层数据权限调用，只关闭数据权限忽略
                ignoreStrategy.setDataPermission(false);
            }
            // 如果不是最后一层调用，则什么都不做，等待后续的disableIgnore调用
        }
    }

    /**
     * 在忽略数据权限的模式下执行指定操作
     * <p>
     * 这是推荐的使用方式，能够确保权限忽略状态的正确管理，避免忘记调用disableIgnore()。
     * 方法会自动处理异常情况，确保在任何情况下都能正确恢复权限状态。
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 忽略权限删除所有测试数据
     * DataPermissionHelper.ignore(() -> {
     *     userMapper.delete(Wrappers.lambdaQuery(User.class)
     *         .like(User::getUserName, "test_"));
     * });
     *
     * // 忽略权限执行数据统计
     * DataPermissionHelper.ignore(() -> {
     *     statisticsService.generateDailyReport();
     * });
     * }</pre>
     *
     * @param handle 需要在忽略权限模式下执行的操作
     */
    public static void ignore(Runnable handle) {
        enableIgnore();
        try {
            handle.run();
        } finally {
            disableIgnore();
        }
    }

    /**
     * 在忽略数据权限的模式下执行指定操作并返回结果
     * <p>
     * 带返回值的权限忽略执行方法，适用于需要获取执行结果的场景。
     * 同样确保权限状态的正确管理和异常安全性。
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 获取所有用户数量（忽略数据权限）
     * Long totalCount = DataPermissionHelper.ignore(() -> {
     *     return userMapper.selectCount(null);
     * });
     *
     * // 获取全量数据进行分析
     * List<User> allUsers = DataPermissionHelper.ignore(() -> {
     *     return userMapper.selectList(null);
     * });
     *
     * // 执行复杂的跨权限数据处理
     * ReportData report = DataPermissionHelper.ignore(() -> {
     *     return reportService.generateFullReport();
     * });
     * }</pre>
     *
     * @param handle 需要在忽略权限模式下执行的操作
     * @param <T>    返回值类型
     * @return 操作执行的结果
     */
    public static <T> T ignore(Supplier<T> handle) {
        enableIgnore();
        try {
            return handle.get();
        } finally {
            disableIgnore();
        }
    }
}
