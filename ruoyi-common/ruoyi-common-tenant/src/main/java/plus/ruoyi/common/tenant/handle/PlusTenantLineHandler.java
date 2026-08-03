package plus.ruoyi.common.tenant.handle;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.tenant.helper.TenantHelper;
import plus.ruoyi.common.tenant.properties.TenantProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 自定义多租户SQL处理器（始终启用）
 * <p>
 * 始终为SQL添加租户条件，永远不返回NullValue
 * 这样可以避免开关租户功能导致的数据混乱
 *
 * @author Lion Li
 */
@Slf4j
public class PlusTenantLineHandler implements TenantLineHandler {

    /**
     * 系统固定排除表（硬编码，防止用户误配置）
     */
    private static final Set<String> SYSTEM_EXCLUDE_TABLES = Set.of(
        "sys_tenant",
        "sys_tenant_package",
        "sys_gen_table",
        "sys_gen_table_column",
        "sys_menu",
        "sys_role_menu",
        "sys_role_dept",
        "sys_user_role",
        "sys_user_post",
        "sys_oss_config"
    );

    /**
     * 全局排除表集合（系统表 + 全局配置表）
     * 应用到所有数据源
     */
    private final Set<String> globalExcludes;

    /**
     * 数据源级别排除表配置
     * Key: 数据源名称
     * Value: 该数据源的排除表集合
     */
    private final Map<String, Set<String>> datasourceExcludes;

    public PlusTenantLineHandler(TenantProperties tenantProperties) {
        // 构建全局排除表集合（系统表 + 全局配置表）
        this.globalExcludes = Stream.concat(
                // 系统固定排除表
                SYSTEM_EXCLUDE_TABLES.stream(),
                // 全局配置的排除表
                tenantProperties.getExcludes() != null ?
                    tenantProperties.getExcludes().stream() : Stream.empty()
            )
            .map(String::toLowerCase)
            .collect(Collectors.toSet());

        // 构建数据源级别排除表配置
        this.datasourceExcludes = new HashMap<>();
        if (tenantProperties.getDatasourceExcludes() != null) {
            tenantProperties.getDatasourceExcludes().forEach((datasource, tables) -> {
                Set<String> tableSet = tables.stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());
                datasourceExcludes.put(datasource, tableSet);
            });
        }

        log.info("多租户拦截器已启用（始终生效），全局排除表: {}, 数据源级别配置: {}",
            globalExcludes, datasourceExcludes);
    }

    /**
     * 获取租户ID表达式（始终返回有效值）
     * <p>
     * 无论租户功能是否开启，都返回有效的租户ID
     * 这样可以确保所有数据都有明确的租户归属
     *
     * @return 租户ID的SQL表达式，永远不为null
     */
    @Override
    public Expression getTenantId() {
        // 直接使用TenantHelper.getTenantId()，它已经保证返回有效值
        return new StringValue(TenantHelper.getTenantId());
    }

    /**
     * 判断是否忽略指定表的租户过滤（支持数据源级别配置）
     * <p>
     * 配置规则（累加模式）：
     * 1. 全局配置的排除表始终生效（应用到所有数据源）
     * 2. 数据源级别配置是在全局配置基础上的补充（不是替换）
     * 3. 最终结果：表在全局配置中 OR 表在当前数据源配置中 → 忽略租户过滤
     *
     * @param tableName 表名
     * @return true：忽略租户过滤，false：应用租户过滤
     */
    @Override
    public boolean ignoreTable(String tableName) {
        if (StringUtils.isBlank(tableName)) {
            return true;
        }

        String lowerTableName = tableName.toLowerCase();

        // 获取当前数据源名称（null 表示默认数据源 master）
        String currentDataSource = DynamicDataSourceContextHolder.peek();
        if (currentDataSource == null) {
            currentDataSource = "master";
        }

        // 检查全局配置（始终生效）
        boolean inGlobalExcludes = globalExcludes.contains(lowerTableName);

        // 检查数据源级别配置（如果存在）
        boolean inDataSourceExcludes = datasourceExcludes.containsKey(currentDataSource)
            && datasourceExcludes.get(currentDataSource).contains(lowerTableName);

        // 累加模式：在全局配置中 OR 在数据源配置中 → 忽略租户过滤
        boolean shouldIgnore = inGlobalExcludes || inDataSourceExcludes;

        if (log.isDebugEnabled()) {
            log.debug("数据源 [{}] 表 [{}] 租户过滤: {} (全局: {}, 数据源: {})",
                currentDataSource, tableName,
                shouldIgnore ? "忽略" : "应用",
                inGlobalExcludes, inDataSourceExcludes);
        }

        return shouldIgnore;
    }
}
