package plus.ruoyi.common.tenant.handle;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import plus.ruoyi.common.tenant.properties.TenantProperties;
import plus.ruoyi.common.test.base.BaseUnitTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PlusTenantLineHandler 租户拦截器测试
 * <p>
 * 测试数据源级别的租户隔离功能:
 * <ul>
 *   <li>全局配置的表忽略</li>
 *   <li>数据源级别配置的表忽略</li>
 *   <li>优先级规则（数据源级别 > 全局级别）</li>
 *   <li>切换数据源后的行为</li>
 * </ul>
 *
 * @author Claude Code
 */
@DisplayName("PlusTenantLineHandler 租户拦截器测试")
class PlusTenantLineHandlerTest extends BaseUnitTest {

    // ==================== 全局配置测试 ====================

    @Nested
    @DisplayName("全局配置测试")
    class GlobalExcludesTests {

        @Test
        @DisplayName("ignoreTable - 全局配置的表应被忽略")
        void testIgnoreTableWithGlobalExcludes() {
            // 准备配置
            TenantProperties properties = new TenantProperties();
            properties.setExcludes(List.of("sys_config", "sys_dict"));

            // 创建拦截器
            PlusTenantLineHandler handler = new PlusTenantLineHandler(properties);

            // 验证全局配置的表被忽略
            assertTrue(handler.ignoreTable("sys_config"), "全局配置的表应被忽略");
            assertTrue(handler.ignoreTable("sys_dict"), "全局配置的表应被忽略");
            assertTrue(handler.ignoreTable("SYS_CONFIG"), "表名大小写不敏感");
        }

        @Test
        @DisplayName("ignoreTable - 未配置的表不应被忽略")
        void testIgnoreTableWithoutConfig() {
            // 准备配置
            TenantProperties properties = new TenantProperties();
            properties.setExcludes(List.of("sys_config"));

            // 创建拦截器
            PlusTenantLineHandler handler = new PlusTenantLineHandler(properties);

            // 验证未配置的表不被忽略
            assertFalse(handler.ignoreTable("b_ad"), "未配置的表不应被忽略");
            assertFalse(handler.ignoreTable("m_goods"), "未配置的表不应被忽略");
        }

        @Test
        @DisplayName("ignoreTable - 系统固定排除表始终被忽略")
        void testIgnoreTableWithSystemExcludes() {
            // 准备配置（不配置任何排除表）
            TenantProperties properties = new TenantProperties();

            // 创建拦截器
            PlusTenantLineHandler handler = new PlusTenantLineHandler(properties);

            // 验证系统固定排除表被忽略
            assertTrue(handler.ignoreTable("sys_tenant"), "系统固定排除表应被忽略");
            assertTrue(handler.ignoreTable("sys_menu"), "系统固定排除表应被忽略");
            assertTrue(handler.ignoreTable("sys_oss_config"), "系统固定排除表应被忽略");
        }
    }

    // ==================== 数据源级别配置测试 ====================

    @Nested
    @DisplayName("数据源级别配置测试")
    class DataSourceExcludesTests {

        @Test
        @DisplayName("ignoreTable - 数据源级别配置的表应被忽略")
        void testIgnoreTableWithDataSourceExcludes() {
            try (MockedStatic<DynamicDataSourceContextHolder> contextHolderMock =
                     Mockito.mockStatic(DynamicDataSourceContextHolder.class)) {

                // Mock 当前数据源为 master
                contextHolderMock.when(DynamicDataSourceContextHolder::peek)
                    .thenReturn("master");

                // 准备配置
                TenantProperties properties = new TenantProperties();
                Map<String, List<String>> dsExcludes = new HashMap<>();
                dsExcludes.put("master", List.of("sys_config", "sys_dict"));
                properties.setDatasourceExcludes(dsExcludes);

                // 创建拦截器
                PlusTenantLineHandler handler = new PlusTenantLineHandler(properties);

                // 验证数据源级别配置的表被忽略
                assertTrue(handler.ignoreTable("sys_config"),
                    "master 数据源配置的表应被忽略");
                assertTrue(handler.ignoreTable("sys_dict"),
                    "master 数据源配置的表应被忽略");
            }
        }

        @Test
        @DisplayName("ignoreTable - 切换数据源后同名表不应被忽略")
        void testIgnoreTableAfterSwitchDataSource() {
            try (MockedStatic<DynamicDataSourceContextHolder> contextHolderMock =
                     Mockito.mockStatic(DynamicDataSourceContextHolder.class)) {

                // 准备配置：只在 master 数据源忽略 sys_config
                TenantProperties properties = new TenantProperties();
                Map<String, List<String>> dsExcludes = new HashMap<>();
                dsExcludes.put("master", List.of("sys_config"));
                properties.setDatasourceExcludes(dsExcludes);

                // 创建拦截器
                PlusTenantLineHandler handler = new PlusTenantLineHandler(properties);

                // 场景1：在 master 数据源，sys_config 应被忽略
                contextHolderMock.when(DynamicDataSourceContextHolder::peek)
                    .thenReturn("master");
                assertTrue(handler.ignoreTable("sys_config"),
                    "master 数据源的 sys_config 应被忽略");

                // 场景2：切换到 third-party 数据源，sys_config 不应被忽略
                contextHolderMock.when(DynamicDataSourceContextHolder::peek)
                    .thenReturn("third-party");
                assertFalse(handler.ignoreTable("sys_config"),
                    "third-party 数据源的 sys_config 不应被忽略");
            }
        }
    }

    // ==================== 配置规则测试 ====================

    @Nested
    @DisplayName("配置规则测试")
    class ConfigurationRulesTests {

        @Test
        @DisplayName("ignoreTable - 数据源级别配置累加到全局配置")
        void testDataSourceExcludesAdditive() {
            try (MockedStatic<DynamicDataSourceContextHolder> contextHolderMock =
                     Mockito.mockStatic(DynamicDataSourceContextHolder.class)) {

                // Mock 当前数据源为 master
                contextHolderMock.when(DynamicDataSourceContextHolder::peek)
                    .thenReturn("master");

                // 准备配置：全局配置忽略 sys_config 和 sys_dict，master 数据源额外忽略 b_ad
                TenantProperties properties = new TenantProperties();
                properties.setExcludes(List.of("sys_config", "sys_dict"));

                Map<String, List<String>> dsExcludes = new HashMap<>();
                dsExcludes.put("master", List.of("b_ad")); // master 额外忽略 b_ad
                properties.setDatasourceExcludes(dsExcludes);

                // 创建拦截器
                PlusTenantLineHandler handler = new PlusTenantLineHandler(properties);

                // 验证：累加模式 - 全局配置 + 数据源配置都生效
                assertTrue(handler.ignoreTable("b_ad"),
                    "master 数据源配置的表应被忽略");
                assertTrue(handler.ignoreTable("sys_config"),
                    "全局配置的表应被忽略（即使 master 未单独配置）");
                assertTrue(handler.ignoreTable("sys_dict"),
                    "全局配置的表应被忽略（即使 master 未单独配置）");
                assertFalse(handler.ignoreTable("m_goods"),
                    "未在任何配置中的表不应被忽略");
            }
        }

        @Test
        @DisplayName("ignoreTable - 未配置数据源使用全局配置")
        void testFallbackToGlobalExcludes() {
            try (MockedStatic<DynamicDataSourceContextHolder> contextHolderMock =
                     Mockito.mockStatic(DynamicDataSourceContextHolder.class)) {

                // Mock 当前数据源为 slave（未配置数据源级别）
                contextHolderMock.when(DynamicDataSourceContextHolder::peek)
                    .thenReturn("slave");

                // 准备配置
                TenantProperties properties = new TenantProperties();
                properties.setExcludes(List.of("sys_config", "sys_dict"));

                Map<String, List<String>> dsExcludes = new HashMap<>();
                dsExcludes.put("master", List.of("b_ad")); // 只配置 master
                properties.setDatasourceExcludes(dsExcludes);

                // 创建拦截器
                PlusTenantLineHandler handler = new PlusTenantLineHandler(properties);

                // 验证：slave 数据源使用全局配置
                assertTrue(handler.ignoreTable("sys_config"),
                    "未配置的数据源应使用全局配置");
                assertTrue(handler.ignoreTable("sys_dict"),
                    "未配置的数据源应使用全局配置");
                assertFalse(handler.ignoreTable("b_ad"),
                    "未配置的数据源不应使用其他数据源的配置");
            }
        }
    }

    // ==================== 边界条件测试 ====================

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryTests {

        @Test
        @DisplayName("ignoreTable - 表名为null时返回true")
        void testIgnoreTableWithNullTableName() {
            TenantProperties properties = new TenantProperties();
            PlusTenantLineHandler handler = new PlusTenantLineHandler(properties);

            assertTrue(handler.ignoreTable(null), "表名为null时应返回true");
        }

        @Test
        @DisplayName("ignoreTable - 表名为空字符串时返回true")
        void testIgnoreTableWithEmptyTableName() {
            TenantProperties properties = new TenantProperties();
            PlusTenantLineHandler handler = new PlusTenantLineHandler(properties);

            assertTrue(handler.ignoreTable(""), "表名为空字符串时应返回true");
            assertTrue(handler.ignoreTable("   "), "表名为空白字符时应返回true");
        }

        @Test
        @DisplayName("ignoreTable - 数据源名称为null时视为master数据源(累加模式)")
        void testIgnoreTableWithNullDataSource() {
            try (MockedStatic<DynamicDataSourceContextHolder> contextHolderMock =
                     Mockito.mockStatic(DynamicDataSourceContextHolder.class)) {

                // Mock 数据源为 null（表示默认数据源 master）
                contextHolderMock.when(DynamicDataSourceContextHolder::peek)
                    .thenReturn(null);

                // 准备配置：master 数据源配置忽略 sys_config
                TenantProperties properties = new TenantProperties();
                properties.setExcludes(List.of("sys_dict"));  // 全局配置

                Map<String, List<String>> dsExcludes = new HashMap<>();
                dsExcludes.put("master", List.of("sys_config"));  // master 数据源配置
                properties.setDatasourceExcludes(dsExcludes);

                // 创建拦截器
                PlusTenantLineHandler handler = new PlusTenantLineHandler(properties);

                // 验证：累加模式 — 全局配置 + 数据源配置都生效
                assertTrue(handler.ignoreTable("sys_config"),
                    "数据源级别的表应被忽略");
                assertTrue(handler.ignoreTable("sys_dict"),
                    "累加模式下全局配置的表也应被忽略（即使master有独立配置）");
            }
        }

        @Test
        @DisplayName("ignoreTable - 配置为空时只忽略系统固定表")
        void testIgnoreTableWithEmptyConfig() {
            TenantProperties properties = new TenantProperties();
            PlusTenantLineHandler handler = new PlusTenantLineHandler(properties);

            // 系统固定表应被忽略
            assertTrue(handler.ignoreTable("sys_tenant"), "系统固定表应被忽略");

            // 业务表不应被忽略
            assertFalse(handler.ignoreTable("b_ad"), "业务表不应被忽略");
            assertFalse(handler.ignoreTable("sys_config"), "业务表不应被忽略");
        }
    }
}
