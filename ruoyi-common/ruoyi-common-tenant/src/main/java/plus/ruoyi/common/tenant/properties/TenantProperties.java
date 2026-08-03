package plus.ruoyi.common.tenant.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

/**
 * 多租户配置属性类
 * <p>
 * 从配置文件中读取tenant前缀的配置项，
 * 用于控制多租户功能的开启和排除表设置
 *
 * @author Lion Li
 */
@Data
@ConfigurationProperties(prefix = "tenant")
public class TenantProperties {

    /**
     * 是否启用多租户功能
     * <p>
     * 设置为true时启用多租户功能，false时禁用
     */
    private Boolean enable;

    /**
     * 全局多租户排除表列表
     * <p>
     * 配置中指定的表名不会应用租户过滤条件，
     * 通常用于系统级别的公共表
     * <p>
     * 该配置应用到所有数据源，如果需要为特定数据源配置排除表，
     * 请使用 datasourceExcludes
     */
    private List<String> excludes;

    /**
     * 数据源级别的多租户排除表配置
     * <p>
     * Key: 数据源名称（如 master、slave 等）
     * Value: 该数据源需要排除的表名列表
     * <p>
     * 优先级：数据源级别配置 > 全局配置
     * <p>
     * 使用场景：当切换数据源后，同名表需要不同的租户隔离策略时使用
     * <p>
     * 示例配置：
     * <pre>
     * tenant:
     *   excludes:
     *     - sys_tenant
     *     - sys_menu
     *   datasource-excludes:
     *     master:
     *       - sys_config
     *       - sys_dict
     *     third-party:
     *       - log_table
     * </pre>
     */
    private Map<String, List<String>> datasourceExcludes;
}
