package plus.ruoyi.common.mybatis.helper;

import cn.hutool.core.convert.Convert;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.mybatis.enums.DataBaseType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 数据库操作助手类
 * <p>
 * 提供数据库类型检测、跨数据库SQL兼容性处理、数据源管理等功能。
 * 支持多种主流数据库：MySQL、Oracle、PostgreSQL、SQL Server等。
 * <p>
 * 主要功能：
 * <ul>
 *   <li>动态检测当前使用的数据库类型</li>
 *   <li>提供各种数据库类型的快速判断方法</li>
 *   <li>生成跨数据库兼容的SQL语句（如字符串查找函数）</li>
 *   <li>管理和查询可用的数据源列表</li>
 * </ul>
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 检测数据库类型
 * if (DataBaseHelper.isMySql()) {
 *     // MySQL特定逻辑
 * }
 *
 * // 生成兼容的字符串查找SQL
 * String sql = DataBaseHelper.findInSet("100", "user_roles");
 * }</pre>
 *
 * @author Lion Li
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DataBaseHelper {

    /**
     * 动态路由数据源实例
     * <p>
     * 通过Spring容器获取，用于访问当前激活的数据源
     */
    private static final DynamicRoutingDataSource DS = SpringUtils.getBean(DynamicRoutingDataSource.class);

    /**
     * 数据库类型缓存，避免重复获取数据库连接
     * <p>
     * Key: 数据源标识，Value: 数据库类型
     */
    private static final ConcurrentMap<String, DataBaseType> DATABASE_TYPE_CACHE = new ConcurrentHashMap<>();

    /**
     * 获取当前激活数据源的数据库类型
     * <p>
     * 通过获取数据库连接的元数据信息来确定数据库类型。
     * 此方法会自动处理连接的获取和释放，并使用缓存提高性能。
     *
     * @return 当前数据库类型枚举值
     * @throws ServiceException 当无法获取数据库连接或读取元数据时抛出
     * @see DataBaseType#find(String) 数据库类型识别逻辑
     */
    public static DataBaseType getDataBaseType() {
        DataSource dataSource = DS.determineDataSource();
        String dataSourceKey = getDataSourceKey(dataSource);

        // 尝试从缓存获取
        DataBaseType cachedType = DATABASE_TYPE_CACHE.get(dataSourceKey);
        if (cachedType != null) {
            return cachedType;
        }

        // 缓存未命中，从数据库获取
        DataBaseType databaseType = detectDatabaseTypeFromConnection(dataSource);

        // 放入缓存
        DATABASE_TYPE_CACHE.put(dataSourceKey, databaseType);

        return databaseType;
    }

    /**
     * 获取指定数据源对应的数据库类型
     *
     * @param dsName 数据源名称
     * @return 指定数据库对应的 DataBaseType 枚举
     * @throws ServiceException 当获取数据库连接或元数据出现异常时抛出
     */
    public static DataBaseType getDataBaseType(String dsName) {
        DataSource dataSource = DS.getDataSource(dsName);
        String dataSourceKey = getDataSourceKey(dataSource);

        DataBaseType cachedType = DATABASE_TYPE_CACHE.get(dataSourceKey);
        if (cachedType != null) {
            return cachedType;
        }

        DataBaseType databaseType = detectDatabaseTypeFromConnection(dataSource);
        DATABASE_TYPE_CACHE.put(dataSourceKey, databaseType);
        return databaseType;
    }

    /**
     * 从数据库连接中检测数据库类型
     *
     * @param dataSource 数据源
     * @return 数据库类型
     * @throws ServiceException 当无法获取数据库连接或读取元数据时抛出
     */
    private static DataBaseType detectDatabaseTypeFromConnection(DataSource dataSource) {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            String databaseProductName = metaData.getDatabaseProductName();
            return DataBaseType.find(databaseProductName);
        } catch (SQLException e) {
            log.error("获取数据库类型失败", e);
            throw ServiceException.of("获取数据库类型失败: " + e.getMessage());
        }
    }

    /**
     * 获取数据源的唯一标识
     *
     * @param dataSource 数据源
     * @return 数据源标识
     */
    private static String getDataSourceKey(DataSource dataSource) {
        // 这里可以根据实际情况调整标识生成策略
        return dataSource.toString();
    }

    /**
     * 清除数据库类型缓存
     * <p>
     * 当数据源配置发生变化时可以调用此方法清除缓存
     */
    public static void clearCache() {
        DATABASE_TYPE_CACHE.clear();
        log.debug("数据库类型缓存已清除");
    }

    /**
     * 判断当前数据库是否为 MySQL
     *
     * @return true-当前使用MySQL数据库，false-使用其他类型数据库
     */
    public static boolean isMySql() {
        return DataBaseType.MY_SQL == getDataBaseType();
    }

    /**
     * 判断当前数据库是否为 Oracle
     *
     * @return true-当前使用Oracle数据库，false-使用其他类型数据库
     */
    public static boolean isOracle() {
        return DataBaseType.ORACLE == getDataBaseType();
    }

    /**
     * 判断当前数据库是否为 PostgreSQL
     *
     * @return true-当前使用PostgreSQL数据库，false-使用其他类型数据库
     */
    public static boolean isPostgreSql() {
        return DataBaseType.POSTGRE_SQL == getDataBaseType();
    }

    /**
     * 判断当前数据库是否为 SQL Server
     *
     * @return true-当前使用SQL Server数据库，false-使用其他类型数据库
     */
    public static boolean isSqlServer() {
        return DataBaseType.SQL_SERVER == getDataBaseType();
    }

    /**
     * 生成跨数据库兼容的字符串查找SQL表达式
     * <p>
     * 用于检查指定值是否存在于逗号分隔的字符串列表中，类似于MySQL的FIND_IN_SET函数。
     * 该方法会根据不同的数据库类型生成对应的SQL表达式。
     * <p>
     * 各数据库实现方式：
     * <ul>
     *   <li><b>MySQL</b>: 使用 FIND_IN_SET 函数</li>
     *   <li><b>SQL Server</b>: 使用 CHARINDEX 函数</li>
     *   <li><b>PostgreSQL</b>: 使用 STRPOS 函数</li>
     *   <li><b>Oracle</b>: 使用 INSTR 函数</li>
     * </ul>
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 检查用户ID "100" 是否在角色列表 "0,100,101" 中
     * String condition = DataBaseHelper.findInSet("100", "user_roles");
     * String sql = "SELECT * FROM users WHERE " + condition;
     *
     * // 生成的SQL（MySQL）: SELECT * FROM users WHERE find_in_set('100' , user_roles) <> 0
     * // 生成的SQL（Oracle）: SELECT * FROM users WHERE instr(','||user_roles||',' , ',100,') <> 0
     * }</pre>
     *
     * @param searchValue 要查找的目标值，不能为null
     * @param fieldName   包含逗号分隔值的字段名或表达式，不能为null或空字符串
     * @return 适合当前数据库类型的SQL条件表达式，返回结果不为0表示找到匹配项
     * @throws IllegalArgumentException 当参数为null或空时抛出
     */
    public static String findInSet(Object searchValue, String fieldName) {
        return buildFindInSetCondition(searchValue, fieldName, false);
    }

    /**
     * 生成跨数据库兼容的字符串排除查找SQL表达式
     * <p>
     * 用于检查指定值是否不存在于逗号分隔的字符串列表中，与findInSet相反。
     * 该方法会根据不同的数据库类型生成对应的SQL表达式。
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 检查用户ID "100" 是否不在角色列表 "0,100,101" 中
     * String condition = DataBaseHelper.findNotInSet("100", "user_roles");
     * String sql = "SELECT * FROM users WHERE " + condition;
     *
     * // 生成的SQL（MySQL）: SELECT * FROM users WHERE find_in_set('100' , user_roles) = 0
     * // 生成的SQL（Oracle）: SELECT * FROM users WHERE instr(','||user_roles||',' , ',100,') = 0
     * }</pre>
     *
     * @param searchValue 要查找的目标值，不能为null
     * @param fieldName   包含逗号分隔值的字段名或表达式，不能为null或空字符串
     * @return 适合当前数据库类型的SQL条件表达式，返回结果为0表示未找到匹配项
     * @throws IllegalArgumentException 当参数为null或空时抛出
     */
    public static String findNotInSet(Object searchValue, String fieldName) {
        return buildFindInSetCondition(searchValue, fieldName, true);
    }

    /**
     * 构建字符串查找条件的通用方法
     *
     * @param searchValue 要查找的目标值
     * @param fieldName   字段名
     * @param isNegative  是否为否定查询（true=不在集合中，false=在集合中）
     * @return SQL条件表达式
     */
    private static String buildFindInSetCondition(Object searchValue, String fieldName, boolean isNegative) {
        if (searchValue == null) {
            throw new IllegalArgumentException("搜索值不能为null");
        }
        if (fieldName == null || fieldName.trim().isEmpty()) {
            throw new IllegalArgumentException("字段名不能为null或空字符串");
        }

        DataBaseType databaseType = getDataBaseType();
        String value = Convert.toStr(searchValue);

        return switch (databaseType) {
            case SQL_SERVER -> buildSqlServerFindInSet(value, fieldName, isNegative);
            case POSTGRE_SQL -> buildPostgreSqlFindInSet(value, fieldName, isNegative);
            case ORACLE -> buildOracleFindInSet(value, fieldName, isNegative);
            default -> buildMySqlFindInSet(value, fieldName, isNegative);
        };
    }

    /**
     * 构建SQL Server的字符串查找表达式
     */
    private static String buildSqlServerFindInSet(String value, String fieldName, boolean isNegative) {
        // SQL Server: 使用 CHARINDEX 函数
        if (isNegative) {
            // 不在集合中：字段为NULL 或者 查找结果为0
            return "(%s IS NULL OR charindex(',%s,' , ','+%s+',') = 0)".formatted(fieldName, value, fieldName);
        } else {
            // 在集合中：字段不为NULL 且 查找结果不为0
            return "(%s IS NOT NULL AND charindex(',%s,' , ','+%s+',') <> 0)".formatted(fieldName, value, fieldName);
        }
    }

    /**
     * 构建PostgreSQL的字符串查找表达式
     */
    private static String buildPostgreSqlFindInSet(String value, String fieldName, boolean isNegative) {
        // PostgreSQL: 使用 STRPOS 函数
        if (isNegative) {
            // 不在集合中：字段为NULL 或者 查找结果为0
            return "(%s IS NULL OR (select strpos(','||%s||',' , ',%s,')) = 0)".formatted(fieldName, fieldName, value);
        } else {
            // 在集合中：字段不为NULL 且 查找结果不为0
            return "(%s IS NOT NULL AND (select strpos(','||%s||',' , ',%s,')) <> 0)".formatted(fieldName, fieldName, value);
        }
    }

    /**
     * 构建Oracle的字符串查找表达式
     */
    private static String buildOracleFindInSet(String value, String fieldName, boolean isNegative) {
        // Oracle: 使用 INSTR 函数
        if (isNegative) {
            // 不在集合中：字段为NULL 或者 查找结果为0
            return "(%s IS NULL OR instr(','||%s||',' , ',%s,') = 0)".formatted(fieldName, fieldName, value);
        } else {
            // 在集合中：字段不为NULL 且 查找结果不为0
            return "(%s IS NOT NULL AND instr(','||%s||',' , ',%s,') <> 0)".formatted(fieldName, fieldName, value);
        }
    }

    /**
     * 构建MySQL的字符串查找表达式
     */
    private static String buildMySqlFindInSet(String value, String fieldName, boolean isNegative) {
        // MySQL: 使用 FIND_IN_SET 函数
        if (isNegative) {
            // 不在集合中：字段为NULL 或者 查找结果为0
            // 使用 COALESCE 处理 NULL 值
            return "COALESCE(find_in_set('%s' , %s), 0) = 0".formatted(value, fieldName);
        } else {
            // 在集合中：字段不为NULL 且 查找结果不为0
            return "find_in_set('%s' , %s) <> 0".formatted(value, fieldName);
        }
    }

    /**
     * 生成跨数据库兼容的 LIKE 表达式（将字段转换为字符串）
     * <p>
     * 用于非字符串字段（如 bigint、numeric、timestamp 等）的模糊查询。
     * PostgreSQL 等数据库不支持对非字符串类型直接使用 LIKE，需要先转换为字符串。
     * <p>
     * 各数据库实现方式：
     * <ul>
     *   <li><b>MySQL</b>: 直接使用 LIKE（MySQL 支持隐式转换）</li>
     *   <li><b>PostgreSQL</b>: CAST(column AS VARCHAR)</li>
     *   <li><b>Oracle</b>: TO_CHAR(column)</li>
     *   <li><b>SQL Server</b>: CAST(column AS NVARCHAR(MAX))</li>
     * </ul>
     * <p>
     * 使用示例：
     * <pre>{@code
     * // 对 bigint 类型字段进行模糊查询
     * String expr = DataBaseHelper.castToVarchar("id");
     * // MySQL: id
     * // PostgreSQL: CAST(id AS VARCHAR)
     * // Oracle: TO_CHAR(id)
     * // SQL Server: CAST(id AS NVARCHAR(MAX))
     * }</pre>
     *
     * @param columnName 字段名
     * @return 转换后的表达式
     */
    public static String castToVarchar(String columnName) {
        if (columnName == null || columnName.trim().isEmpty()) {
            throw new IllegalArgumentException("字段名不能为null或空字符串");
        }

        DataBaseType databaseType = getDataBaseType();

        return switch (databaseType) {
            case POSTGRE_SQL -> "CAST(" + columnName + " AS VARCHAR)";
            case ORACLE -> "TO_CHAR(" + columnName + ")";
            case SQL_SERVER -> "CAST(" + columnName + " AS NVARCHAR(MAX))";
            default -> columnName; // MySQL 支持隐式转换，无需处理
        };
    }

    /**
     * 判断当前数据库是否需要对非字符串字段进行 LIKE 查询时显式转换类型
     * <p>
     * MySQL 支持对 bigint 等类型直接使用 LIKE（隐式转换），
     * 而 PostgreSQL、Oracle、SQL Server 需要显式转换。
     *
     * @return true-需要显式转换，false-不需要
     */
    public static boolean needCastForLike() {
        return !isMySql();
    }

    /**
     * 获取当前系统中所有已加载的数据源名称列表
     * <p>
     * 返回动态数据源中已配置的所有数据源的名称标识。
     * 这些名称通常对应配置文件中定义的数据源键值。
     * <p>
     * 使用场景：
     * <ul>
     *   <li>系统监控：展示可用的数据源列表</li>
     *   <li>动态切换：为用户提供数据源选择选项</li>
     *   <li>配置验证：检查特定数据源是否已正确加载</li>
     * </ul>
     *
     * @return 数据源名称列表，列表中的每个元素对应一个已配置的数据源标识
     */
    public static List<String> getDataSourceNameList() {
        return new ArrayList<>(DS.getDataSources().keySet());
    }
}
