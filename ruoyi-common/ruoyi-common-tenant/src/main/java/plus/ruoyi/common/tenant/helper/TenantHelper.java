package plus.ruoyi.common.tenant.helper;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.context.model.SaStorage;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.plugins.IgnoreStrategy;
import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.constant.GlobalConstants;
import plus.ruoyi.common.core.constant.TenantConstants;
import plus.ruoyi.common.core.service.TenantService;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.core.utils.reflect.ReflectUtils;
import plus.ruoyi.common.redis.utils.RedisUtils;
import plus.ruoyi.common.satoken.utils.LoginHelper;

import java.time.Duration;
import java.util.Stack;
import java.util.function.Supplier;

/**
 * 多租户助手工具类
 * <p>
 * 提供租户相关的操作方法，包括租户开关控制、动态租户设置、
 * 租户忽略控制等功能
 *
 * @author Lion Li
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TenantHelper {

    /**
     * 动态租户Redis键前缀
     */
    private static final String DYNAMIC_TENANT_KEY = GlobalConstants.GLOBAL_REDIS_KEY + "dynamicTenant";

    /**
     * 线程本地动态租户存储
     */
    private static final ThreadLocal<String> TEMP_DYNAMIC_TENANT = new ThreadLocal<>();

    /**
     * 租户忽略重入计数器
     */
    private static final ThreadLocal<Stack<Integer>> REENTRANT_IGNORE = ThreadLocal.withInitial(Stack::new);

    /**
     * 检查多租户功能是否启用
     *
     * @return true：已启用，false：未启用
     */
    public static boolean isEnable() {
        return Convert.toBool(SpringUtils.getProperty("tenant.enable"), false);
    }

    /**
     * 获取当前的忽略策略
     *
     * @return 忽略策略对象
     */
    private static IgnoreStrategy getIgnoreStrategy() {
        Object ignoreStrategyLocal = ReflectUtils.getStaticFieldValue(ReflectUtils.getField(InterceptorIgnoreHelper.class, "IGNORE_STRATEGY_LOCAL"));
        if (ignoreStrategyLocal instanceof ThreadLocal<?> IGNORE_STRATEGY_LOCAL) {
            if (IGNORE_STRATEGY_LOCAL.get() instanceof IgnoreStrategy ignoreStrategy) {
                return ignoreStrategy;
            }
        }
        return null;
    }

    /**
     * 开启租户忽略模式
     * <p>
     * 开启后需手动调用disableIgnore()关闭，支持重入
     */
    private static void enableIgnore() {
        IgnoreStrategy ignoreStrategy = getIgnoreStrategy();
        if (ObjectUtil.isNull(ignoreStrategy)) {
            InterceptorIgnoreHelper.handle(IgnoreStrategy.builder().tenantLine(true).build());
        } else {
            ignoreStrategy.setTenantLine(true);
        }

        // 重入计数
        Stack<Integer> reentrantStack = REENTRANT_IGNORE.get();
        reentrantStack.push(reentrantStack.size() + 1);
    }

    /**
     * 关闭租户忽略模式
     * <p>
     * 支持重入，只有在最外层调用时才真正关闭
     */
    private static void disableIgnore() {
        IgnoreStrategy ignoreStrategy = getIgnoreStrategy();
        if (ObjectUtil.isNotNull(ignoreStrategy)) {
            // 检查是否还有其他忽略策略
            boolean noOtherIgnoreStrategy = !Boolean.TRUE.equals(ignoreStrategy.getDynamicTableName())
                && !Boolean.TRUE.equals(ignoreStrategy.getBlockAttack())
                && !Boolean.TRUE.equals(ignoreStrategy.getIllegalSql())
                && !Boolean.TRUE.equals(ignoreStrategy.getDataPermission())
                && CollectionUtil.isEmpty(ignoreStrategy.getOthers());

            Stack<Integer> reentrantStack = REENTRANT_IGNORE.get();
            boolean empty = reentrantStack.isEmpty() || reentrantStack.pop() == 1;

            if (noOtherIgnoreStrategy && empty) {
                // 清除所有忽略策略
                InterceptorIgnoreHelper.clearIgnoreStrategy();
            } else if (empty) {
                // 只关闭租户忽略
                ignoreStrategy.setTenantLine(false);
            }
        }
    }

    /**
     * 在忽略租户模式下执行代码块
     *
     * @param handle 要执行的代码块
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
     * 在忽略租户模式下执行代码块并返回结果
     *
     * @param handle 要执行的代码块
     * @return 执行结果
     */
    public static <T> T ignore(Supplier<T> handle) {
        enableIgnore();
        try {
            return handle.get();
        } finally {
            disableIgnore();
        }
    }

    /**
     * 设置动态租户（线程级别）
     *
     * @param tenantId 租户ID
     */
    public static void setDynamic(String tenantId) {
        setDynamic(tenantId, false);
    }

    /**
     * 设置动态租户
     *
     * @param tenantId 租户ID
     * @param global   是否全局生效（存储到Redis）
     */
    public static void setDynamic(String tenantId, boolean global) {
        if (!isEnable()) {
            return;
        }

        // 未登录或非全局模式，只在线程内生效
        if (!LoginHelper.isLogin() || !global) {
            TEMP_DYNAMIC_TENANT.set(tenantId);
            return;
        }

        // 存储到Redis，跨请求生效（绑定到具体的Token，实现会话级别隔离）
        String cacheKey = DYNAMIC_TENANT_KEY + ":" + LoginHelper.getUserId() + ":" + StpUtil.getTokenValue();
        RedisUtils.setCacheObject(cacheKey, tenantId, Duration.ofDays(10));
        SaHolder.getStorage().set(cacheKey, tenantId);
    }

    /**
     * 获取动态租户ID
     *
     * @return 动态租户ID
     */
    public static String getDynamic() {
        if (!isEnable()) {
            return null;
        }

        // 未登录时从线程本地获取
        if (!LoginHelper.isLogin()) {
            return TEMP_DYNAMIC_TENANT.get();
        }

        // 优先从线程本地获取
        String tenantId = TEMP_DYNAMIC_TENANT.get();
        if (StringUtils.isNotBlank(tenantId)) {
            return tenantId;
        }

        // 从会话存储或Redis获取（绑定到具体的Token，实现会话级别隔离）
        SaStorage storage = SaHolder.getStorage();
        String cacheKey = DYNAMIC_TENANT_KEY + ":" + LoginHelper.getUserId() + ":" + StpUtil.getTokenValue();
        tenantId = storage.getString(cacheKey);

        // -1表示Redis中不存在
        if (StringUtils.isNotBlank(tenantId)) {
            return tenantId.equals("-1") ? null : tenantId;
        }

        // 从Redis获取并缓存到会话存储
        tenantId = RedisUtils.getCacheObject(cacheKey);
        storage.set(cacheKey, StringUtils.isBlank(tenantId) ? "-1" : tenantId);
        return tenantId;
    }

    /**
     * 清除动态租户设置
     */
    public static void clearDynamic() {
        if (!isEnable()) {
            return;
        }

        if (!LoginHelper.isLogin()) {
            TEMP_DYNAMIC_TENANT.remove();
            return;
        }

        // 清除线程本地和Redis存储（绑定到具体的Token，实现会话级别隔离）
        TEMP_DYNAMIC_TENANT.remove();
        String cacheKey = DYNAMIC_TENANT_KEY + ":" + LoginHelper.getUserId() + ":" + StpUtil.getTokenValue();
        RedisUtils.deleteObject(cacheKey);
        SaHolder.getStorage().delete(cacheKey);
    }

    /**
     * 清除线程本地动态租户（不影响全局动态租户）
     */
    public static void clearDynamicLocal() {
        if (!isEnable()) {
            return;
        }
        TEMP_DYNAMIC_TENANT.remove();
    }

    /**
     * 在指定租户下执行代码块
     *
     * @param tenantId 租户ID
     * @param handle   要执行的代码块
     */
    public static void dynamic(String tenantId, Runnable handle) {
        if (!isEnable()) {
            handle.run();
            return;
        }

        String previousTenant = TEMP_DYNAMIC_TENANT.get();
        TEMP_DYNAMIC_TENANT.set(tenantId);
        try {
            handle.run();
        } finally {
            if (previousTenant == null) {
                TEMP_DYNAMIC_TENANT.remove();
            } else {
                TEMP_DYNAMIC_TENANT.set(previousTenant);
            }
        }
    }

    /**
     * 在指定租户下执行代码块并返回结果
     *
     * @param tenantId 租户ID
     * @param handle   要执行的代码块
     * @return 执行结果
     */
    public static <T> T dynamic(String tenantId, Supplier<T> handle) {
        if (!isEnable()) {
            return handle.get();
        }

        String previousTenant = TEMP_DYNAMIC_TENANT.get();
        TEMP_DYNAMIC_TENANT.set(tenantId);
        try {
            return handle.get();
        } finally {
            if (previousTenant == null) {
                TEMP_DYNAMIC_TENANT.remove();
            } else {
                TEMP_DYNAMIC_TENANT.set(previousTenant);
            }
        }
    }

    /**
     * 获取当前有效的租户ID
     * <p>
     * 优先级：动态租户 > 登录用户租户 > 请求头租户
     *
     * @return 当前租户ID
     */
    public static String getTenantId() {
        // 如果租户功能未开启，直接返回默认租户 保证数据隔离
        if (!isEnable()) {
            return TenantConstants.DEFAULT_TENANT_ID;
        }

        // 优先获取动态租户
        String tenantId = getDynamic();
        if (StringUtils.isBlank(tenantId)) {
            // 从登录用户信息获取
            tenantId = LoginHelper.getTenantId();
        }
        if (StringUtils.isBlank(tenantId)) {
            // 从请求中获取（包含域名识别和请求头获取）
            try {
                tenantId = SpringUtils.getBean(TenantService.class).getTenantIdByRequest();
            } catch (Exception ignored) {
                // 忽略获取请求失败的异常（比如非Web环境）
            }
        }
        return StringUtils.isNotBlank(tenantId) ? tenantId : TenantConstants.DEFAULT_TENANT_ID;
    }

}
