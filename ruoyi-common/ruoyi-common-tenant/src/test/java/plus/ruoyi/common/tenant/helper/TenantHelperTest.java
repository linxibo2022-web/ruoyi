package plus.ruoyi.common.tenant.helper;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.context.model.SaStorage;
import cn.hutool.extra.spring.SpringUtil;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import plus.ruoyi.common.core.constant.TenantConstants;
import plus.ruoyi.common.redis.utils.RedisUtils;
import plus.ruoyi.common.satoken.utils.LoginHelper;
import plus.ruoyi.common.test.base.BaseUnitTest;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * TenantHelper 租户助手工具类测试
 * <p>
 * 测试多租户管理工具的核心功能:
 * <ul>
 *   <li>租户功能开关检测</li>
 *   <li>租户忽略模式控制</li>
 *   <li>动态租户设置与获取</li>
 *   <li>租户ID获取优先级</li>
 * </ul>
 *
 * @author 抓蛙师
 */
@DisplayName("TenantHelper 租户助手工具类测试")
class TenantHelperTest extends BaseUnitTest {

    // ==================== 租户功能开关测试 ====================

    @Nested
    @DisplayName("租户功能开关测试")
    class EnableTests {

        @Test
        @DisplayName("isEnable - 租户功能启用时返回true")
        void testIsEnableWhenEnabled() {
            try (MockedStatic<SpringUtil> springUtilMock = Mockito.mockStatic(SpringUtil.class)) {
                springUtilMock.when(() -> SpringUtil.getProperty("tenant.enable"))
                    .thenReturn("true");

                assertTrue(TenantHelper.isEnable(), "租户功能启用时应返回true");
            }
        }

        @Test
        @DisplayName("isEnable - 租户功能禁用时返回false")
        void testIsEnableWhenDisabled() {
            try (MockedStatic<SpringUtil> springUtilMock = Mockito.mockStatic(SpringUtil.class)) {
                springUtilMock.when(() -> SpringUtil.getProperty("tenant.enable"))
                    .thenReturn("false");

                assertFalse(TenantHelper.isEnable(), "租户功能禁用时应返回false");
            }
        }

        @Test
        @DisplayName("isEnable - 配置为空时默认返回false")
        void testIsEnableWhenPropertyNull() {
            try (MockedStatic<SpringUtil> springUtilMock = Mockito.mockStatic(SpringUtil.class)) {
                springUtilMock.when(() -> SpringUtil.getProperty("tenant.enable"))
                    .thenReturn(null);

                assertFalse(TenantHelper.isEnable(), "配置为空时应默认返回false");
            }
        }

        @Test
        @DisplayName("isEnable - 配置为非布尔值时返回false")
        void testIsEnableWhenPropertyInvalid() {
            try (MockedStatic<SpringUtil> springUtilMock = Mockito.mockStatic(SpringUtil.class)) {
                springUtilMock.when(() -> SpringUtil.getProperty("tenant.enable"))
                    .thenReturn("invalid");

                assertFalse(TenantHelper.isEnable(), "非布尔值配置应返回false");
            }
        }
    }

    // ==================== 租户忽略模式测试 ====================

    @Nested
    @DisplayName("租户忽略模式测试")
    class IgnoreTests {

        @Test
        @DisplayName("ignore(Runnable) - 执行代码块")
        void testIgnoreRunnable() {
            AtomicBoolean executed = new AtomicBoolean(false);

            TenantHelper.ignore(() -> {
                executed.set(true);
            });

            assertTrue(executed.get(), "代码块应被执行");
        }

        @Test
        @DisplayName("ignore(Supplier) - 执行代码块并返回结果")
        void testIgnoreSupplier() {
            String result = TenantHelper.ignore(() -> "test-result");

            assertEquals("test-result", result, "应返回正确的执行结果");
        }

        @Test
        @DisplayName("ignore(Supplier) - 执行过程中发生异常时应正常抛出")
        void testIgnoreSupplierWithException() {
            assertThrows(RuntimeException.class, () -> {
                TenantHelper.ignore(() -> {
                    throw new RuntimeException("Test exception");
                });
            }, "异常应正常抛出");
        }

        @Test
        @DisplayName("ignore(Runnable) - 执行过程中发生异常时应正常抛出")
        void testIgnoreRunnableWithException() {
            assertThrows(RuntimeException.class, () -> {
                TenantHelper.ignore((Runnable) () -> {
                    throw new RuntimeException("Test exception");
                });
            }, "异常应正常抛出");
        }

        @Test
        @DisplayName("ignore - 支持嵌套调用（重入）")
        void testIgnoreReentrant() {
            // 测试嵌套调用 ignore() 方法的重入能力
            TenantHelper.ignore(() -> {
                // 外层忽略
                TenantHelper.ignore(() -> {
                    // 内层忽略（重入）
                    // 应该能正常执行完毕，不抛出异常
                });
            });
        }
    }

    // ==================== 动态租户测试 (租户功能禁用) ====================

    @Nested
    @DisplayName("动态租户测试 - 租户功能禁用")
    class DynamicTenantDisabledTests {

        @Test
        @DisplayName("setDynamic - 租户功能禁用时不执行任何操作")
        void testSetDynamicWhenDisabled() {
            try (MockedStatic<SpringUtil> springUtilMock = Mockito.mockStatic(SpringUtil.class)) {
                springUtilMock.when(() -> SpringUtil.getProperty("tenant.enable"))
                    .thenReturn("false");

                // 不应抛出异常
                TenantHelper.setDynamic("test-tenant");
            }
        }

        @Test
        @DisplayName("getDynamic - 租户功能禁用时返回null")
        void testGetDynamicWhenDisabled() {
            try (MockedStatic<SpringUtil> springUtilMock = Mockito.mockStatic(SpringUtil.class)) {
                springUtilMock.when(() -> SpringUtil.getProperty("tenant.enable"))
                    .thenReturn("false");

                assertNull(TenantHelper.getDynamic(), "租户功能禁用时应返回null");
            }
        }

        @Test
        @DisplayName("clearDynamic - 租户功能禁用时不执行任何操作")
        void testClearDynamicWhenDisabled() {
            try (MockedStatic<SpringUtil> springUtilMock = Mockito.mockStatic(SpringUtil.class)) {
                springUtilMock.when(() -> SpringUtil.getProperty("tenant.enable"))
                    .thenReturn("false");

                // 不应抛出异常
                TenantHelper.clearDynamic();
            }
        }
    }

    // ==================== 动态租户测试 (租户功能启用，未登录) ====================

    @Nested
    @DisplayName("动态租户测试 - 租户功能启用，未登录")
    class DynamicTenantEnabledNotLoggedInTests {

        @Test
        @DisplayName("setDynamic - 未登录时存储到线程本地")
        void testSetDynamicWhenNotLoggedIn() {
            try (MockedStatic<SpringUtil> springUtilMock = Mockito.mockStatic(SpringUtil.class);
                 MockedStatic<LoginHelper> loginHelperMock = Mockito.mockStatic(LoginHelper.class)) {

                springUtilMock.when(() -> SpringUtil.getProperty("tenant.enable"))
                    .thenReturn("true");
                loginHelperMock.when(LoginHelper::isLogin).thenReturn(false);

                TenantHelper.setDynamic("test-tenant-id");

                // 获取动态租户验证
                String tenantId = TenantHelper.getDynamic();
                assertEquals("test-tenant-id", tenantId, "应从线程本地获取租户ID");

                // 清理
                TenantHelper.clearDynamic();
            }
        }

        @Test
        @DisplayName("getDynamic - 未登录且未设置时返回null")
        void testGetDynamicWhenNotLoggedInAndNotSet() {
            try (MockedStatic<SpringUtil> springUtilMock = Mockito.mockStatic(SpringUtil.class);
                 MockedStatic<LoginHelper> loginHelperMock = Mockito.mockStatic(LoginHelper.class)) {

                springUtilMock.when(() -> SpringUtil.getProperty("tenant.enable"))
                    .thenReturn("true");
                loginHelperMock.when(LoginHelper::isLogin).thenReturn(false);

                // 确保清理状态
                TenantHelper.clearDynamic();

                assertNull(TenantHelper.getDynamic(), "未设置时应返回null");
            }
        }

        @Test
        @DisplayName("clearDynamic - 未登录时清除线程本地存储")
        void testClearDynamicWhenNotLoggedIn() {
            try (MockedStatic<SpringUtil> springUtilMock = Mockito.mockStatic(SpringUtil.class);
                 MockedStatic<LoginHelper> loginHelperMock = Mockito.mockStatic(LoginHelper.class)) {

                springUtilMock.when(() -> SpringUtil.getProperty("tenant.enable"))
                    .thenReturn("true");
                loginHelperMock.when(LoginHelper::isLogin).thenReturn(false);

                // 先设置
                TenantHelper.setDynamic("test-tenant-id");
                assertEquals("test-tenant-id", TenantHelper.getDynamic());

                // 清除
                TenantHelper.clearDynamic();
                assertNull(TenantHelper.getDynamic(), "清除后应返回null");
            }
        }
    }

    // ==================== dynamic方法测试 ====================

    @Nested
    @DisplayName("dynamic 方法测试")
    class DynamicMethodTests {

        @Test
        @DisplayName("dynamic(tenantId, Runnable) - 在指定租户下执行代码块")
        void testDynamicRunnable() {
            try (MockedStatic<SpringUtil> springUtilMock = Mockito.mockStatic(SpringUtil.class);
                 MockedStatic<LoginHelper> loginHelperMock = Mockito.mockStatic(LoginHelper.class)) {

                springUtilMock.when(() -> SpringUtil.getProperty("tenant.enable"))
                    .thenReturn("true");
                loginHelperMock.when(LoginHelper::isLogin).thenReturn(false);

                AtomicReference<String> capturedTenantId = new AtomicReference<>();

                TenantHelper.dynamic("dynamic-tenant", () -> {
                    capturedTenantId.set(TenantHelper.getDynamic());
                });

                assertEquals("dynamic-tenant", capturedTenantId.get(), "执行时应使用指定租户");
                assertNull(TenantHelper.getDynamic(), "执行后应清除动态租户");
            }
        }

        @Test
        @DisplayName("dynamic(tenantId, Supplier) - 在指定租户下执行代码块并返回结果")
        void testDynamicSupplier() {
            try (MockedStatic<SpringUtil> springUtilMock = Mockito.mockStatic(SpringUtil.class);
                 MockedStatic<LoginHelper> loginHelperMock = Mockito.mockStatic(LoginHelper.class)) {

                springUtilMock.when(() -> SpringUtil.getProperty("tenant.enable"))
                    .thenReturn("true");
                loginHelperMock.when(LoginHelper::isLogin).thenReturn(false);

                String result = TenantHelper.dynamic("dynamic-tenant", () -> {
                    return "result-from-" + TenantHelper.getDynamic();
                });

                assertEquals("result-from-dynamic-tenant", result, "应返回正确结果");
                assertNull(TenantHelper.getDynamic(), "执行后应清除动态租户");
            }
        }

        @Test
        @DisplayName("dynamic - 执行过程中发生异常时应清除动态租户")
        void testDynamicWithException() {
            try (MockedStatic<SpringUtil> springUtilMock = Mockito.mockStatic(SpringUtil.class);
                 MockedStatic<LoginHelper> loginHelperMock = Mockito.mockStatic(LoginHelper.class)) {

                springUtilMock.when(() -> SpringUtil.getProperty("tenant.enable"))
                    .thenReturn("true");
                loginHelperMock.when(LoginHelper::isLogin).thenReturn(false);

                assertThrows(RuntimeException.class, () -> {
                    TenantHelper.dynamic("dynamic-tenant", () -> {
                        throw new RuntimeException("Test exception");
                    });
                });

                // 异常后也应清除动态租户
                assertNull(TenantHelper.getDynamic(), "异常后也应清除动态租户");
            }
        }
    }

    // ==================== getTenantId方法测试 ====================

    @Nested
    @DisplayName("getTenantId 方法测试")
    class GetTenantIdTests {

        @Test
        @DisplayName("getTenantId - 租户功能禁用时返回默认租户ID")
        void testGetTenantIdWhenDisabled() {
            try (MockedStatic<SpringUtil> springUtilMock = Mockito.mockStatic(SpringUtil.class)) {
                springUtilMock.when(() -> SpringUtil.getProperty("tenant.enable"))
                    .thenReturn("false");

                assertEquals(TenantConstants.DEFAULT_TENANT_ID, TenantHelper.getTenantId(),
                    "租户功能禁用时应返回默认租户ID");
            }
        }

        @Test
        @DisplayName("getTenantId - 优先返回动态租户")
        void testGetTenantIdPriorityDynamic() {
            try (MockedStatic<SpringUtil> springUtilMock = Mockito.mockStatic(SpringUtil.class);
                 MockedStatic<LoginHelper> loginHelperMock = Mockito.mockStatic(LoginHelper.class)) {

                springUtilMock.when(() -> SpringUtil.getProperty("tenant.enable"))
                    .thenReturn("true");
                loginHelperMock.when(LoginHelper::isLogin).thenReturn(false);
                loginHelperMock.when(LoginHelper::getTenantId).thenReturn("user-tenant");

                // 设置动态租户
                TenantHelper.setDynamic("dynamic-tenant");

                assertEquals("dynamic-tenant", TenantHelper.getTenantId(),
                    "应优先返回动态租户");

                // 清理
                TenantHelper.clearDynamic();
            }
        }

        @Test
        @DisplayName("getTenantId - 动态租户为空时从登录用户获取")
        void testGetTenantIdFromLoginUser() {
            try (MockedStatic<SpringUtil> springUtilMock = Mockito.mockStatic(SpringUtil.class);
                 MockedStatic<LoginHelper> loginHelperMock = Mockito.mockStatic(LoginHelper.class)) {

                springUtilMock.when(() -> SpringUtil.getProperty("tenant.enable"))
                    .thenReturn("true");
                // 先清理再配置 mock
                loginHelperMock.when(LoginHelper::isLogin).thenReturn(false);

                // 确保动态租户为空
                TenantHelper.clearDynamic();

                // 重新配置 mock 返回租户ID
                loginHelperMock.when(LoginHelper::getTenantId).thenReturn("user-tenant");

                assertEquals("user-tenant", TenantHelper.getTenantId(),
                    "动态租户为空时应从登录用户获取");
            }
        }

        @Test
        @DisplayName("getTenantId - 全部为空时返回默认租户ID")
        @SuppressWarnings("unchecked")
        void testGetTenantIdReturnDefault() {
            try (MockedStatic<SpringUtil> springUtilMock = Mockito.mockStatic(SpringUtil.class);
                 MockedStatic<LoginHelper> loginHelperMock = Mockito.mockStatic(LoginHelper.class)) {

                springUtilMock.when(() -> SpringUtil.getProperty("tenant.enable"))
                    .thenReturn("true");
                springUtilMock.when(() -> SpringUtil.getBean(any(Class.class)))
                    .thenThrow(new RuntimeException("No TenantService"));
                loginHelperMock.when(LoginHelper::isLogin).thenReturn(false);
                loginHelperMock.when(LoginHelper::getTenantId).thenReturn(null);

                // 确保动态租户为空
                TenantHelper.clearDynamic();

                assertEquals(TenantConstants.DEFAULT_TENANT_ID, TenantHelper.getTenantId(),
                    "全部为空时应返回默认租户ID");
            }
        }
    }

    // ==================== 边界条件测试 ====================

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryTests {

        @Test
        @DisplayName("setDynamic - 设置空字符串")
        void testSetDynamicWithEmptyString() {
            try (MockedStatic<SpringUtil> springUtilMock = Mockito.mockStatic(SpringUtil.class);
                 MockedStatic<LoginHelper> loginHelperMock = Mockito.mockStatic(LoginHelper.class)) {

                springUtilMock.when(() -> SpringUtil.getProperty("tenant.enable"))
                    .thenReturn("true");
                loginHelperMock.when(LoginHelper::isLogin).thenReturn(false);

                TenantHelper.setDynamic("");

                assertEquals("", TenantHelper.getDynamic(), "应能设置空字符串");

                TenantHelper.clearDynamic();
            }
        }

        @Test
        @DisplayName("setDynamic - 设置null")
        void testSetDynamicWithNull() {
            try (MockedStatic<SpringUtil> springUtilMock = Mockito.mockStatic(SpringUtil.class);
                 MockedStatic<LoginHelper> loginHelperMock = Mockito.mockStatic(LoginHelper.class)) {

                springUtilMock.when(() -> SpringUtil.getProperty("tenant.enable"))
                    .thenReturn("true");
                loginHelperMock.when(LoginHelper::isLogin).thenReturn(false);

                TenantHelper.setDynamic(null);

                assertNull(TenantHelper.getDynamic(), "设置null后获取应返回null");

                TenantHelper.clearDynamic();
            }
        }

        @Test
        @DisplayName("多次setDynamic - 后设置的覆盖前设置的")
        void testMultipleSetDynamic() {
            try (MockedStatic<SpringUtil> springUtilMock = Mockito.mockStatic(SpringUtil.class);
                 MockedStatic<LoginHelper> loginHelperMock = Mockito.mockStatic(LoginHelper.class)) {

                springUtilMock.when(() -> SpringUtil.getProperty("tenant.enable"))
                    .thenReturn("true");
                loginHelperMock.when(LoginHelper::isLogin).thenReturn(false);

                TenantHelper.setDynamic("tenant-1");
                TenantHelper.setDynamic("tenant-2");
                TenantHelper.setDynamic("tenant-3");

                assertEquals("tenant-3", TenantHelper.getDynamic(), "后设置的应覆盖前设置的");

                TenantHelper.clearDynamic();
            }
        }
    }

    // ==================== 动态租户测试 (租户功能启用，已登录) ====================

    @Nested
    @DisplayName("动态租户测试 - 租户功能启用，已登录")
    class DynamicTenantEnabledLoggedInTests {

        @Test
        @DisplayName("setDynamic(tenantId, false) - 已登录非全局模式存储到线程本地")
        void testSetDynamicNotGlobalWhenLoggedIn() {
            try (MockedStatic<SpringUtil> springUtilMock = Mockito.mockStatic(SpringUtil.class);
                 MockedStatic<LoginHelper> loginHelperMock = Mockito.mockStatic(LoginHelper.class);
                 MockedStatic<RedisUtils> redisUtilsMock = Mockito.mockStatic(RedisUtils.class);
                 MockedStatic<SaHolder> saHolderMock = Mockito.mockStatic(SaHolder.class)) {

                springUtilMock.when(() -> SpringUtil.getProperty("tenant.enable"))
                    .thenReturn("true");
                loginHelperMock.when(LoginHelper::isLogin).thenReturn(true);
                loginHelperMock.when(LoginHelper::getUserId).thenReturn(100L);

                // Mock SaHolder.getStorage()
                SaStorage mockStorage = mock(SaStorage.class);
                saHolderMock.when(SaHolder::getStorage).thenReturn(mockStorage);

                // 非全局模式
                TenantHelper.setDynamic("local-tenant", false);

                // 验证只存储到线程本地，因此 getDynamic() 可以获取到
                String tenantId = TenantHelper.getDynamic();
                assertEquals("local-tenant", tenantId, "非全局模式应存储到线程本地");

                TenantHelper.clearDynamic();
            }
        }

        @Test
        @DisplayName("setDynamic(tenantId, true) - 已登录全局模式存储到Redis")
        void testSetDynamicGlobalWhenLoggedIn() {
            try (MockedStatic<SpringUtil> springUtilMock = Mockito.mockStatic(SpringUtil.class);
                 MockedStatic<LoginHelper> loginHelperMock = Mockito.mockStatic(LoginHelper.class);
                 MockedStatic<RedisUtils> redisUtilsMock = Mockito.mockStatic(RedisUtils.class);
                 MockedStatic<SaHolder> saHolderMock = Mockito.mockStatic(SaHolder.class)) {

                springUtilMock.when(() -> SpringUtil.getProperty("tenant.enable"))
                    .thenReturn("true");
                loginHelperMock.when(LoginHelper::isLogin).thenReturn(true);
                loginHelperMock.when(LoginHelper::getUserId).thenReturn(100L);

                SaStorage mockStorage = mock(SaStorage.class);
                saHolderMock.when(SaHolder::getStorage).thenReturn(mockStorage);

                // 全局模式
                TenantHelper.setDynamic("global-tenant", true);

                // 验证Redis调用
                redisUtilsMock.verify(() -> RedisUtils.setCacheObject(
                    contains("dynamicTenant:100"),
                    eq("global-tenant"),
                    any()
                ), times(1));
            }
        }
    }
}
