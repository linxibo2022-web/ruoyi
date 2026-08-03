package plus.ruoyi.common.mybatis.core.query;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.SharedString;
import com.baomidou.mybatisplus.core.conditions.query.Query;
import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlInjectionUtils;
import plus.ruoyi.common.mybatis.helper.DataBaseHelper;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * MyBatis-Plus 字符串列名查询增强器 (Query)
 * <p>
 * 本类扩展了AbstractWrapper，提供比原生QueryWrapper更强大的查询功能：
 * <p>
 * 1. 智能条件处理
 * - 自动处理null值和空字符串，避免无效条件
 * - BETWEEN条件的智能降级处理（当只有一个边界值有效时自动转为大于/小于条件）
 * - 集合条件(IN/NOT IN)的元素过滤，排除无效元素
 * <p>
 * 2. 聚合函数支持
 * - sum: sum(column), sum(column, alias) - 求和函数
 * - min: min(column), min(column, alias) - 最小值函数
 * - max: max(column), max(column, alias) - 最大值函数
 * - count: count(column), count(column, alias) - 计数函数
 * - avg: avg(column, alias) - 平均值函数
 * - aggfunc: aggfunc(name, column, alias) - 通用聚合函数构建器
 * <p>
 * 3. 静态工厂方法
 * - of(): 创建空查询实例
 * - of(Class<E>): 基于实体类创建查询实例
 * - of(E): 基于实体对象创建查询实例
 * <p>
 * 4. 条件方法增强（所有方法自动处理无效值）
 * - 比较操作: eq, ne, gt, ge, lt, le
 * - 范围操作: between, notBetween
 * - 集合操作: in, notIn
 * - 模糊匹配: like, likeLeft, likeRight
 * - 字段选择: select
 * <p>
 * 与LambdaQuery不同，本类直接使用字符串形式的列名，适合动态列名场景。
 * 所有条件构建方法都保持链式调用风格，与MyBatis-Plus原生API保持一致。
 *
 * @param <T> 实体类型
 * @author 抓蛙师
 */
@SuppressWarnings("serial")
public class PlusQuery<T> extends AbstractWrapper<T, String, PlusQuery<T>>
    implements Query<PlusQuery<T>, T, String> {

    /**
     * 查询字段
     */
    private SharedString sqlSelect = new SharedString();

    /**
     * SQL注入检查开关
     */
    private boolean checkSqlInjection = false;

    /**
     * 无参构造方法
     */
    public PlusQuery() {
        this((T) null);
    }

    /**
     * 带实体对象的构造方法
     *
     * @param entity 实体对象
     */
    public PlusQuery(T entity) {
        super.setEntity(entity);
        super.initNeed();
    }

    /**
     * 带实体类的构造方法
     *
     * @param entityClass 实体类
     */
    public PlusQuery(Class<T> entityClass) {
        super.setEntityClass(entityClass);
        super.initNeed();
    }

    /**
     * 带实体对象与字段的构造方法
     *
     * @param entity  实体对象
     * @param columns 查询字段
     */
    public PlusQuery(T entity, String... columns) {
        super.setEntity(entity);
        super.initNeed();
        this.select(columns);
    }

    /**
     * 完整构造方法
     */
    PlusQuery(T entity, Class<T> entityClass, SharedString sqlSelect, AtomicInteger paramNameSeq,
              Map<String, Object> paramNameValuePairs, MergeSegments mergeSegments, SharedString paramAlias,
              SharedString lastSql, SharedString sqlComment, SharedString sqlFirst) {
        super.setEntity(entity);
        super.setEntityClass(entityClass);
        this.paramNameSeq = paramNameSeq;
        this.paramNameValuePairs = paramNameValuePairs;
        this.expression = mergeSegments;
        this.sqlSelect = sqlSelect;
        this.paramAlias = paramAlias;
        this.lastSql = lastSql;
        this.sqlComment = sqlComment;
        this.sqlFirst = sqlFirst;
    }

    /**
     * 开启SQL注入检查
     *
     * @return this
     */
    public PlusQuery<T> checkSqlInjection() {
        this.checkSqlInjection = true;
        return this;
    }

    /**
     * 字段转换为字符串
     *
     * @param column 字段
     * @return 字段字符串
     */
    @Override
    protected String columnToString(String column) {
        if (this.checkSqlInjection && SqlInjectionUtils.check(column)) {
            throw new MybatisPlusException("发现SQL注入攻击字段: " + column);
        }
        return column;
    }

    // ================ 静态工厂方法 =================

    /**
     * 创建QueryPlus实例（推荐）
     *
     * <p>示例：
     * <pre>{@code
     * // 创建一个查询
     * QueryPlus.of(User.class)
     *     .eq("status", 1)
     *     .like("name", keyword)
     * }</pre>
     *
     * @param <E>         实体类型
     * @param entityClass 实体类
     * @return QueryPlus实例
     */
    public static <E> PlusQuery<E> of(Class<E> entityClass) {
        return new PlusQuery<>(entityClass);
    }

    /**
     * 创建QueryPlus实例
     *
     * <p>示例：
     * <pre>{@code
     * // 创建一个空查询
     * QueryPlus.of()
     * }</pre>
     *
     * @param <E> 实体类型
     * @return QueryPlus实例
     */
    public static <E> PlusQuery<E> of() {
        return new PlusQuery<>();
    }

    /**
     * 创建包含实体对象的QueryPlus实例
     *
     * <p>示例：
     * <pre>{@code
     * // 通过实体创建查询
     * User user = new User();
     * user.setName("张三");
     * QueryPlus.of(user)
     * }</pre>
     *
     * @param <E>    实体类型
     * @param entity 实体对象
     * @return QueryPlus实例
     */
    public static <E> PlusQuery<E> of(E entity) {
        return new PlusQuery<>(entity);
    }

    // ================ 条件方法增强部分（智能处理null值和空字符串）=================

    /**
     * 等于(=)，当值不为null且字符串不为空时才会加入条件
     *
     * @param column 字段
     * @param val    值
     * @return this
     */
    @Override
    public PlusQuery<T> eq(String column, Object val) {
        if (checkValueEffective(val)) {
            super.eq(column, val);
        }
        return typedThis;
    }

    /**
     * 不等于(<>)，当值不为null且字符串不为空时才会加入条件
     *
     * @param column 字段
     * @param val    值
     * @return this
     */
    @Override
    public PlusQuery<T> ne(String column, Object val) {
        if (checkValueEffective(val)) {
            super.ne(column, val);
        }
        return typedThis;
    }

    /**
     * 大于(>)，当值不为null且字符串不为空时才会加入条件
     *
     * @param column 字段
     * @param val    值
     * @return this
     */
    @Override
    public PlusQuery<T> gt(String column, Object val) {
        if (checkValueEffective(val)) {
            super.gt(column, val);
        }
        return typedThis;
    }

    /**
     * 大于等于(>=)，当值不为null且字符串不为空时才会加入条件
     *
     * @param column 字段
     * @param val    值
     * @return this
     */
    @Override
    public PlusQuery<T> ge(String column, Object val) {
        if (checkValueEffective(val)) {
            super.ge(column, val);
        }
        return typedThis;
    }

    /**
     * 小于(<)，当值不为null且字符串不为空时才会加入条件
     *
     * @param column 字段
     * @param val    值
     * @return this
     */
    @Override
    public PlusQuery<T> lt(String column, Object val) {
        if (checkValueEffective(val)) {
            super.lt(column, val);
        }
        return typedThis;
    }

    /**
     * 小于等于(<=)，当值不为null且字符串不为空时才会加入条件
     *
     * @param column 字段
     * @param val    值
     * @return this
     */
    @Override
    public PlusQuery<T> le(String column, Object val) {
        if (checkValueEffective(val)) {
            super.le(column, val);
        }
        return typedThis;
    }

    /**
     * BETWEEN 值1 AND 值2，智能处理两个值的null和空字符串情况
     * 1. 当两个值都有效时执行BETWEEN条件
     * 2. 当只有第一个值有效时，转为大于等于(>=)条件
     * 3. 当只有第二个值有效时，转为小于等于(<=)条件
     * 4. 当两个值都无效时，不添加任何条件
     *
     * @param column 字段
     * @param val1   值1
     * @param val2   值2
     * @return this
     */
    @Override
    public PlusQuery<T> between(String column, Object val1, Object val2) {
        // Oracle 兼容：将日期格式字符串转为 Date，避免字符串隐式转换触发 ORA-01861
        val1 = tryParseDateStr(val1);
        val2 = tryParseDateStr(val2);
        boolean val1Effective = checkValueEffective(val1);
        boolean val2Effective = checkValueEffective(val2);

        if (val1Effective && val2Effective) {
            super.between(column, val1, val2);
        } else if (val1Effective) {
            // 只有val1有效，则改为大于等于
            super.ge(column, val1);
        } else if (val2Effective) {
            // 只有val2有效，则改为小于等于
            super.le(column, val2);
        }
        return typedThis;
    }

    /**
     * NOT BETWEEN 值1 AND 值2，当两个值都有效时才会加入条件
     *
     * @param column 字段
     * @param val1   值1
     * @param val2   值2
     * @return this
     */
    @Override
    public PlusQuery<T> notBetween(String column, Object val1, Object val2) {
        // Oracle 兼容：将日期格式字符串转为 Date，避免字符串隐式转换触发 ORA-01861
        val1 = tryParseDateStr(val1);
        val2 = tryParseDateStr(val2);
        if (checkValueEffective(val1) && checkValueEffective(val2)) {
            super.notBetween(column, val1, val2);
        }
        return typedThis;
    }

    /**
     * IN (值1, 值2, ...)，当集合不为空且其中的元素都有效时才会加入条件
     *
     * @param column 字段
     * @param coll   值集合
     * @return this
     */
    @Override
    public PlusQuery<T> in(String column, Collection<?> coll) {
        if (CollectionUtils.isNotEmpty(coll)) {
            // 过滤无效值
            Collection<?> filteredColl = coll.stream()
                .filter(this::checkValueEffective)
                .collect(Collectors.toList());

            if (CollectionUtils.isNotEmpty(filteredColl)) {
                super.in(column, filteredColl);
            }
        }
        return typedThis;
    }

    /**
     * NOT IN (值1, 值2, ...)，当集合不为空且其中的元素都有效时才会加入条件
     *
     * @param column 字段
     * @param coll   值集合
     * @return this
     */
    @Override
    public PlusQuery<T> notIn(String column, Collection<?> coll) {
        if (CollectionUtils.isNotEmpty(coll)) {
            // 过滤无效值
            Collection<?> filteredColl = coll.stream()
                .filter(this::checkValueEffective)
                .collect(Collectors.toList());

            if (CollectionUtils.isNotEmpty(filteredColl)) {
                super.notIn(column, filteredColl);
            }
        }
        return typedThis;
    }

    /**
     * LIKE '%值%'，当值不为null且不为空字符串时才会加入条件
     *
     * @param column 字段
     * @param val    值
     * @return this
     */
    @Override
    public PlusQuery<T> like(String column, Object val) {
        if (checkValueEffective(val)) {
            super.like(column, val);
        }
        return typedThis;
    }

    /**
     * LIKE '值%'，当值不为null且不为空字符串时才会加入条件
     *
     * @param column 字段
     * @param val    值
     * @return this
     */
    @Override
    public PlusQuery<T> likeRight(String column, Object val) {
        if (checkValueEffective(val)) {
            super.likeRight(column, val);
        }
        return typedThis;
    }

    /**
     * LIKE '%值'，当值不为null且不为空字符串时才会加入条件
     *
     * @param column 字段
     * @param val    值
     * @return this
     */
    @Override
    public PlusQuery<T> likeLeft(String column, Object val) {
        if (checkValueEffective(val)) {
            super.likeLeft(column, val);
        }
        return typedThis;
    }

    /**
     * LIKE '%值%'（带类型转换），用于非字符串类型字段的模糊查询
     * <p>
     * 跨数据库兼容：
     * - MySQL: 隐式转换，无需特殊处理
     * - PostgreSQL: CAST(column AS VARCHAR)
     * - Oracle: TO_CHAR(column)
     * - SQL Server: CAST(column AS NVARCHAR(MAX))
     *
     * @param column 字段名
     * @param val    值
     * @return this
     */
    public PlusQuery<T> likeCast(String column, Object val) {
        if (checkValueEffective(val)) {
            String castColumn = DataBaseHelper.castToVarchar(columnToString(column));
            super.apply(castColumn + " LIKE {0}", "%" + val + "%");
        }
        return typedThis;
    }

    /**
     * LIKE '值%'（带类型转换），用于非字符串类型字段的右模糊查询
     *
     * @param column 字段名
     * @param val    值
     * @return this
     */
    public PlusQuery<T> likeRightCast(String column, Object val) {
        if (checkValueEffective(val)) {
            String castColumn = DataBaseHelper.castToVarchar(columnToString(column));
            super.apply(castColumn + " LIKE {0}", val + "%");
        }
        return typedThis;
    }

    /**
     * LIKE '%值'（带类型转换），用于非字符串类型字段的左模糊查询
     *
     * @param column 字段名
     * @param val    值
     * @return this
     */
    public PlusQuery<T> likeLeftCast(String column, Object val) {
        if (checkValueEffective(val)) {
            String castColumn = DataBaseHelper.castToVarchar(columnToString(column));
            super.apply(castColumn + " LIKE {0}", "%" + val);
        }
        return typedThis;
    }

    /**
     * 检查值是否有效（不为null且字符串不为空）
     *
     * @param val 值
     * @return 是否有效
     */
    private boolean checkValueEffective(Object val) {
        if (val == null) {
            return false;
        }
        if (val instanceof CharSequence) {
            return StringUtils.isNotBlank(val.toString());
        }
        return true;
    }

    /**
     * Oracle 兼容处理：仅将 "yyyy-MM-dd" 或 "yyyy-MM-dd HH:mm:ss" 格式的日期字符串转为 Date。
     * <p>
     * 为什么这样做：Oracle 对 DATE 列与字符串比较时，会按会话 NLS_DATE_FORMAT 隐式转换，
     * 前端传入的 "2026-07-01" 等格式与其默认格式不符会抛 ORA-01861；转为 Date 后由 JDBC
     * 以 TIMESTAMP 绑定，Oracle / MySQL / PostgreSQL 均兼容，且不会因隐式转换导致索引失效。
     * <p>
     * 为避免误伤数字范围查询（如 between(age, 18, 60)）与非日期字符串（如脏数据 "100"）：
     * 仅对匹配日期格式正则的字符串做转换；数字、其他类型、以及解析失败一律原样返回，绝不抛异常。
     *
     * @param val 原始边界值
     * @return 命中日期格式则返回转换后的 Date，其余原样返回
     */
    private static Object tryParseDateStr(Object val) {
        if (val instanceof CharSequence) {
            String str = val.toString().trim();
            // 仅匹配 yyyy-MM-dd 开头的日期格式，数字（如 "100"）、非日期串不会命中
            if (str.matches("\\d{4}-\\d{2}-\\d{2}([ T].*)?")) {
                try {
                    return DateUtil.parse(str);
                } catch (Exception e) {
                    // 解析失败保底原样返回，绝不影响原有查询行为
                    return val;
                }
            }
        }
        return val;
    }

    // ================ select相关方法 =================

    /**
     * SELECT 部分 SQL 设置
     *
     * @param columns 查询字段
     */
    @Override
    public PlusQuery<T> select(String... columns) {
        if (columns != null && columns.length > 0) {
            this.sqlSelect.setStringValue(String.join(",", columns));
        }
        return typedThis;
    }

    @Override
    public PlusQuery<T> select(boolean condition, List<String> columns) {
        if (condition && CollectionUtils.isNotEmpty(columns)) {
            this.sqlSelect.setStringValue(String.join(",", columns));
        }
        return typedThis;
    }

    /**
     * 过滤查询的字段信息(主键除外!)
     * <p>例1: 只要 java 字段名以 "test" 开头的             -> select(i -&gt; i.getProperty().startsWith("test"))</p>
     * <p>例2: 只要 java 字段属性是 CharSequence 类型的     -> select(TableFieldInfo::isCharSequence)</p>
     * <p>例3: 只要 java 字段没有填充策略的                 -> select(i -&gt; i.getFieldFill() == FieldFill.DEFAULT)</p>
     * <p>例4: 要全部字段                                   -> select(i -&gt; true)</p>
     * <p>例5: 只要主键字段                                 -> select(i -&gt; false)</p>
     *
     * @param predicate 过滤方式
     * @return this
     */
    @Override
    public PlusQuery<T> select(Class<T> entityClass, Predicate<TableFieldInfo> predicate) {
        if (entityClass == null) {
            entityClass = getEntityClass();
        } else {
            setEntityClass(entityClass);
        }
        Assert.notNull(entityClass, "entityClass can not be null");
        this.sqlSelect.setStringValue(TableInfoHelper.getTableInfo(entityClass).chooseSelect(predicate));
        return typedThis;
    }

    @Override
    public String getSqlSelect() {
        return sqlSelect.getStringValue();
    }

    /**
     * 返回一个支持Lambda函数式接口的LambdaQueryWrapper
     *
     * @return LambdaQueryWrapper
     */
    public PlusLambdaQuery<T> lambda() {
        // 创建一个新的LambdaQuery实例，传递所有查询状态
        return new PlusLambdaQuery<>(getEntity(), getEntityClass(), sqlSelect, paramNameSeq, paramNameValuePairs,
            expression, paramAlias, lastSql, sqlComment, sqlFirst);
    }

    // ================ 聚合函数部分 =================

    /**
     * 聚合函数 sum，对指定字段求和
     *
     * @param column 字段
     * @return this
     */
    public PlusQuery<T> sum(String column) {
        aggfunc("sum", column, null);
        return typedThis;
    }

    /**
     * 聚合函数 sum，对指定字段求和并指定别名
     *
     * @param column 源字段
     * @param alias  别名
     * @return this
     */
    public PlusQuery<T> sum(String column, String alias) {
        aggfunc("sum", column, alias);
        return typedThis;
    }

    /**
     * 聚合函数 min，获取指定字段的最小值
     *
     * @param column 字段
     * @return this
     */
    public PlusQuery<T> min(String column) {
        aggfunc("min", column, null);
        return typedThis;
    }

    /**
     * 聚合函数 min，获取指定字段的最小值并指定别名
     *
     * @param column 源字段
     * @param alias  别名
     * @return this
     */
    public PlusQuery<T> min(String column, String alias) {
        aggfunc("min", column, alias);
        return typedThis;
    }

    /**
     * 聚合函数 max，获取指定字段的最大值
     *
     * @param column 字段
     * @return this
     */
    public PlusQuery<T> max(String column) {
        aggfunc("max", column, null);
        return typedThis;
    }

    /**
     * 聚合函数 max，获取指定字段的最大值并指定别名
     *
     * @param column 源字段
     * @param alias  别名
     * @return this
     */
    public PlusQuery<T> max(String column, String alias) {
        aggfunc("max", column, alias);
        return typedThis;
    }

    /**
     * 聚合函数 count，计算指定字段的记录数
     *
     * @param column 字段
     * @return this
     */
    public PlusQuery<T> count(String column) {
        aggfunc("count", column, null);
        return typedThis;
    }

    /**
     * 聚合函数 count，计算指定字段的记录数并指定别名
     *
     * @param column 源字段
     * @param alias  别名
     * @return this
     */
    public PlusQuery<T> count(String column, String alias) {
        aggfunc("count", column, alias);
        return typedThis;
    }

    /**
     * 聚合函数 avg，计算指定字段的平均值并指定别名
     *
     * @param column 源字段
     * @param alias  别名
     * @return this
     */
    public PlusQuery<T> avg(String column, String alias) {
        aggfunc("avg", column, alias);
        return typedThis;
    }

    /**
     * 通用聚合函数构建器
     *
     * @param name   聚合函数名称
     * @param column 源字段
     * @param alias  别名，为null时使用默认别名规则
     * @return this
     */
    public PlusQuery<T> aggfunc(String name, String column, String alias) {
        String agg = (StringUtils.isBlank(this.sqlSelect.getStringValue()) ?
            "" : this.sqlSelect.getStringValue() + ", ") +
            name + " (" + columnToString(column) + ") as " +
            (ObjectUtil.isNull(alias) ? column + "_" + name : alias);
        this.sqlSelect.setStringValue(agg);
        return typedThis;
    }

    // ================ 内部方法 =================

    /**
     * 用于生成嵌套 sql
     * <p>故 sqlSelect 不向下传递</p>
     */
    @Override
    protected PlusQuery<T> instance() {
        return new PlusQuery<>(getEntity(), getEntityClass(), null, paramNameSeq, paramNameValuePairs,
            new MergeSegments(), paramAlias, SharedString.emptyString(), SharedString.emptyString(), SharedString.emptyString());
    }

    @Override
    public void clear() {
        super.clear();
        sqlSelect.toNull();
    }
}
