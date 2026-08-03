package plus.ruoyi.common.mybatis.config;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.anyline.data.datasource.DataSourceMonitor;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.util.ConfigTable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.Map;

/**
 * MyBatis 动态数据源监控器 - 用于适配 Anyline 框架与动态数据源
 * <p>
 * 本类实现了 Anyline 框架的 DataSourceMonitor 接口，专门用于处理动态数据源场景。
 * 主要功能包括：
 * <ul>
 *   <li>识别数据源特征信息，用于确定适合的数据库适配器</li>
 *   <li>为动态数据源提供唯一标识</li>
 *   <li>管理适配器的生命周期和绑定策略</li>
 * </ul>
 *
 * @author Lion Li
 */
@Slf4j
public class MyBatisDataSourceMonitor implements DataSourceMonitor {

    /**
     * 数据源特征信息缓存
     * <p>
     * Key: 数据源标识符（由 DynamicDataSourceContextHolder 提供）
     * Value: 数据源特征字符串（格式：数据库产品名_连接URL）
     */
    private final Map<String, String> features = new HashMap<>();

    /**
     * 构造函数 - 初始化 Anyline 配置
     * <p>
     * 配置说明：
     * <ul>
     *   <li>KEEP_ADAPTER = 2: 启用自定义适配器保持策略</li>
     *   <li>METADATA_CACHE_SCOPE = 0: 禁用元数据缓存，确保动态数据源切换时数据的准确性</li>
     * </ul>
     */
    public MyBatisDataSourceMonitor() {
        // 设置为自定义适配器保持模式，允许根据 keepAdapter() 方法返回值决定是否复用适配器
        ConfigTable.KEEP_ADAPTER = 2;
        // 禁用元数据缓存，避免动态数据源切换时出现缓存数据不一致问题
        ConfigTable.METADATA_CACHE_SCOPE = 0;
    }

    /**
     * 获取数据源特征信息
     * <p>
     * 数据源特征用于帮助 Anyline 框架选择合适的数据库适配器。
     * 特征字符串格式：{数据库产品名}_{连接URL}
     * <p>
     * 处理逻辑：
     * <ol>
     *   <li>检查数据源是否为 JdbcTemplate 类型</li>
     *   <li>判断是否为动态路由数据源</li>
     *   <li>获取当前数据源上下文标识</li>
     *   <li>从缓存中查找或通过数据库连接获取特征信息</li>
     * </ol>
     *
     * @param runtime    数据运行时上下文
     * @param datasource 目标数据源对象
     * @return 数据源特征字符串，格式为 "数据库产品名_连接URL"，如果无法获取则返回 null
     */
    @Override
    public String feature(DataRuntime runtime, Object datasource) {
        String feature = null;

        // 检查数据源类型是否为 JdbcTemplate
        if (datasource instanceof JdbcTemplate jdbc) {
            DataSource ds = jdbc.getDataSource();

            // 仅处理动态路由数据源
            if (ds instanceof DynamicRoutingDataSource) {
                // 获取当前数据源上下文的标识符
                String key = DynamicDataSourceContextHolder.peek();

                // 尝试从缓存中获取特征信息
                feature = features.get(key);

                if (null == feature) {
                    // 缓存中不存在，需要通过数据库连接获取
                    Connection con = null;
                    try {
                        // 获取数据库连接
                        con = DataSourceUtils.getConnection(ds);
                        DatabaseMetaData meta = con.getMetaData();
                        String url = meta.getURL();

                        // 构造特征字符串：数据库产品名（小写，去空格）+ "_" + 连接URL
                        feature = meta.getDatabaseProductName().toLowerCase().replace(" ", "") + "_" + url;

                        // 将特征信息缓存起来，避免重复获取
                        features.put(key, feature);

                        log.debug("数据源 [{}] 特征信息已缓存: {}", key, feature);
                    } catch (Exception e) {
                        log.error("获取数据源 [{}] 特征信息失败: {}", key, e.getMessage(), e);
                    } finally {
                        // 确保连接资源得到正确释放
                        if (null != con && !DataSourceUtils.isConnectionTransactional(con, ds)) {
                            DataSourceUtils.releaseConnection(con, ds);
                        }
                    }
                }
            }
        }
        return feature;
    }

    /**
     * 获取数据源唯一标识符
     * <p>
     * 用于标识不同的数据源实例，Anyline 框架使用此标识符来管理数据源相关的配置和缓存。
     * <p>
     * 对于动态数据源，返回当前上下文中的数据源键名；
     * 对于普通数据源，使用运行时默认的键名。
     *
     * @param runtime    数据运行时上下文
     * @param datasource 目标数据源对象
     * @return 数据源唯一标识符
     */
    @Override
    public String key(DataRuntime runtime, Object datasource) {
        if (datasource instanceof JdbcTemplate jdbc) {
            DataSource ds = jdbc.getDataSource();

            // 对于动态路由数据源，使用当前上下文的数据源键作为标识符
            if (ds instanceof DynamicRoutingDataSource) {
                String currentKey = DynamicDataSourceContextHolder.peek();
                log.debug("使用动态数据源标识符: {}", currentKey);
                return currentKey;
            }
        }

        // 对于非动态数据源，使用运行时默认键
        return runtime.getKey();
    }

    /**
     * 判断是否保持数据源与适配器的绑定关系
     * <p>
     * 当 ConfigTable.KEEP_ADAPTER = 2 时，此方法的返回值决定了适配器的复用策略：
     * <ul>
     *   <li>返回 true：同一个数据源实例将始终绑定同一个适配器实例</li>
     *   <li>返回 false：每次访问都可能创建新的适配器实例</li>
     * </ul>
     * <p>
     * 设计原理：
     * <ul>
     *   <li>对于动态路由数据源：由于同一个 DynamicRoutingDataSource 对象可能路由到不同类型的数据库，
     *       不同的数据库需要不同的适配器，因此返回 false</li>
     *   <li>对于普通数据源：一个数据源对应一种数据库类型，可以安全地复用适配器，因此返回 true</li>
     * </ul>
     *
     * @param runtime    数据运行时上下文
     * @param datasource 目标数据源对象
     * @return true-保持绑定关系，false-不保持绑定关系
     */
    @Override
    public boolean keepAdapter(DataRuntime runtime, Object datasource) {
        if (datasource instanceof JdbcTemplate jdbc) {
            DataSource ds = jdbc.getDataSource();

            // 动态路由数据源不保持适配器绑定，因为它可能路由到不同类型的数据库
            boolean isKeep = !(ds instanceof DynamicRoutingDataSource);
            log.debug("数据源适配器保持策略 - 数据源类型: {}, 是否保持: {}",
                     ds.getClass().getSimpleName(), isKeep);
            return isKeep;
        }

        // 默认保持绑定关系
        return true;
    }
}
