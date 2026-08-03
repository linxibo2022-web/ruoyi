package plus.ruoyi.common.mybatis.core.query;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.AbstractLambdaWrapper;
import com.baomidou.mybatisplus.core.conditions.SharedString;
import com.baomidou.mybatisplus.core.conditions.query.Query;
import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import plus.ruoyi.common.mybatis.helper.DataBaseHelper;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * MyBatis-Plus Lambda查询增强器 (PlusLambdaQuery)
 * <p>
 * 本类扩展了AbstractLambdaWrapper，提供比原生LambdaQueryWrapper更强大的查询功能：
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
 * 使用本类可以显著简化查询构建代码，避免手动判断条件有效性，提高代码可读性和安全性。
 * 所有条件构建方法都保持链式调用风格，与MyBatis-Plus原生API保持一致。
 *
 * @param <T> 实体类型
 * @author 抓蛙师
 */
@SuppressWarnings("serial")
public class PlusLambdaQuery<T> extends AbstractLambdaWrapper<T, PlusLambdaQuery<T>>
    implements Query<PlusLambdaQuery<T>, T, SFunction<T, ?>> {

    /**
     * 查询字段
     */
    private SharedString sqlSelect = new SharedString();

    /**
     * 无参构造方法
     */
    public PlusLambdaQuery() {
        this((T) null);
    }

    /**
     * 带实体对象的构造方法
     *
     * @param entity 实体对象
     */
    public PlusLambdaQuery(T entity) {
        super.setEntity(entity);
        super.initNeed();
    }

    /**
     * 带实体类的构造方法
     *
     * @param entityClass 实体类
     */
    public PlusLambdaQuery(Class<T> entityClass) {
        super.setEntityClass(entityClass);
        super.initNeed();
    }

    /**
     * 完整构造方法
     */
    PlusLambdaQuery(T entity, Class<T> entityClass, SharedString sqlSelect, AtomicInteger paramNameSeq,
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

    // ================ 静态工厂方法 =================

    /**
     * 创建LambdaQuery实例（推荐）
     *
     * <p>示例：
     * <pre>{@code
     * // 创建一个查询
     * PlusLambdaQuery.of(User.class)
     *     .eq(User::getStatus, 1)
     *     .like(User::getName, keyword)
     * }</pre>
     *
     * @param <E>         实体类型
     * @param entityClass 实体类
     * @return LambdaQuery实例
     */
    public static <E> PlusLambdaQuery<E> of(Class<E> entityClass) {
        return new PlusLambdaQuery<>(entityClass);
    }

    /**
     * 创建LambdaQuery实例
     *
     * <p>示例：
     * <pre>{@code
     * // 创建一个空查询
     * PlusLambdaQuery.of()
     * }</pre>
     *
     * @param <E> 实体类型
     * @return LambdaQuery实例
     */
    public static <E> PlusLambdaQuery<E> of() {
        return new PlusLambdaQuery<>();
    }

    /**
     * 创建包含实体对象的LambdaQuery实例
     *
     * <p>示例：
     * <pre>{@code
     * // 通过实体创建查询
     * User user = new User();
     * user.setName("张三");
     * PlusLambdaQuery.of(user)
     * }</pre>
     *
     * @param <E>    实体类型
     * @param entity 实体对象
     * @return LambdaQuery实例
     */
    public static <E> PlusLambdaQuery<E> of(E entity) {
        return new PlusLambdaQuery<>(entity);
    }

    /**
     * 转换为使用字符串列名的QueryPlus
     *
     * @return QueryPlus实例（包含当前查询的所有条件）
     */
    public PlusQuery<T> toQuery() {
        // 创建一个新的Query实例，传递所有查询状态
        return new PlusQuery<>(getEntity(), getEntityClass(), sqlSelect, paramNameSeq, paramNameValuePairs,
            expression, paramAlias, lastSql, sqlComment, sqlFirst);
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
    public PlusLambdaQuery<T> eq(SFunction<T, ?> column, Object val) {
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
    public PlusLambdaQuery<T> ne(SFunction<T, ?> column, Object val) {
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
    public PlusLambdaQuery<T> gt(SFunction<T, ?> column, Object val) {
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
    public PlusLambdaQuery<T> ge(SFunction<T, ?> column, Object val) {
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
    public PlusLambdaQuery<T> lt(SFunction<T, ?> column, Object val) {
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
    public PlusLambdaQuery<T> le(SFunction<T, ?> column, Object val) {
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
    public PlusLambdaQuery<T> between(SFunction<T, ?> column, Object val1, Object val2) {
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
    public PlusLambdaQuery<T> notBetween(SFunction<T, ?> column, Object val1, Object val2) {
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
    public PlusLambdaQuery<T> in(SFunction<T, ?> column, Collection<?> coll) {
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
    public PlusLambdaQuery<T> notIn(SFunction<T, ?> column, Collection<?> coll) {
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
    public PlusLambdaQuery<T> like(SFunction<T, ?> column, Object val) {
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
    public PlusLambdaQuery<T> likeRight(SFunction<T, ?> column, Object val) {
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
    public PlusLambdaQuery<T> likeLeft(SFunction<T, ?> column, Object val) {
        if (checkValueEffective(val)) {
            super.likeLeft(column, val);
        }
        return typedThis;
    }

    /**
     * 跨数据库兼容的 LIKE 查询（将字段转换为字符串后再进行模糊匹配）
     * <p>
     * 用于非字符串字段（如 bigint、numeric、timestamp 等）的模糊查询。
     * PostgreSQL 等数据库不支持对非字符串类型直接使用 LIKE，需要先转换为字符串。
     * <p>
     * 各数据库生成的 SQL：
     * <ul>
     *   <li>MySQL: column LIKE '%value%'（直接使用，支持隐式转换）</li>
     *   <li>PostgreSQL: CAST(column AS VARCHAR) LIKE '%value%'</li>
     *   <li>Oracle: TO_CHAR(column) LIKE '%value%'</li>
     *   <li>SQL Server: CAST(column AS NVARCHAR(MAX)) LIKE '%value%'</li>
     * </ul>
     *
     * @param column 字段
     * @param val    值
     * @return this
     */
    public PlusLambdaQuery<T> likeCast(SFunction<T, ?> column, Object val) {
        if (checkValueEffective(val)) {
            String columnName = columnToString(column);
            String castColumn = DataBaseHelper.castToVarchar(columnName);
            super.apply(castColumn + " LIKE {0}", "%" + val + "%");
        }
        return typedThis;
    }

    /**
     * 跨数据库兼容的 LIKE 右匹配查询（将字段转换为字符串后再进行模糊匹配）
     * <p>
     * 各数据库生成的 SQL：
     * <ul>
     *   <li>MySQL: column LIKE 'value%'</li>
     *   <li>PostgreSQL: CAST(column AS VARCHAR) LIKE 'value%'</li>
     *   <li>Oracle: TO_CHAR(column) LIKE 'value%'</li>
     *   <li>SQL Server: CAST(column AS NVARCHAR(MAX)) LIKE 'value%'</li>
     * </ul>
     *
     * @param column 字段
     * @param val    值
     * @return this
     */
    public PlusLambdaQuery<T> likeRightCast(SFunction<T, ?> column, Object val) {
        if (checkValueEffective(val)) {
            String columnName = columnToString(column);
            String castColumn = DataBaseHelper.castToVarchar(columnName);
            super.apply(castColumn + " LIKE {0}", val + "%");
        }
        return typedThis;
    }

    /**
     * 跨数据库兼容的 LIKE 左匹配查询（将字段转换为字符串后再进行模糊匹配）
     * <p>
     * 各数据库生成的 SQL：
     * <ul>
     *   <li>MySQL: column LIKE '%value'</li>
     *   <li>PostgreSQL: CAST(column AS VARCHAR) LIKE '%value'</li>
     *   <li>Oracle: TO_CHAR(column) LIKE '%value'</li>
     *   <li>SQL Server: CAST(column AS NVARCHAR(MAX)) LIKE '%value'</li>
     * </ul>
     *
     * @param column 字段
     * @param val    值
     * @return this
     */
    public PlusLambdaQuery<T> likeLeftCast(SFunction<T, ?> column, Object val) {
        if (checkValueEffective(val)) {
            String columnName = columnToString(column);
            String castColumn = DataBaseHelper.castToVarchar(columnName);
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
    @SafeVarargs
    @Override
    public final PlusLambdaQuery<T> select(SFunction<T, ?>... columns) {
        return select(Arrays.asList(columns));
    }

    @Override
    public PlusLambdaQuery<T> select(List<SFunction<T, ?>> columns) {
        if (CollectionUtils.isNotEmpty(columns)) {
            this.sqlSelect.setStringValue(columnsToString(false, columns));
        }
        return typedThis;
    }

    @Override
    public PlusLambdaQuery<T> select(boolean condition, List<SFunction<T, ?>> columns) {
        if (condition && CollectionUtils.isNotEmpty(columns)) {
            this.sqlSelect.setStringValue(columnsToString(false, columns));
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
    public PlusLambdaQuery<T> select(Class<T> entityClass, Predicate<TableFieldInfo> predicate) {
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

    // ================ 聚合函数部分 =================

    /**
     * 聚合函数 sum，对指定字段求和
     *
     * @param source 字段
     * @return this
     */
    public PlusLambdaQuery<T> sum(SFunction<T, ?> source) {
        aggfunc("sum", source, null);
        return typedThis;
    }

    /**
     * 聚合函数 sum，对指定字段求和并指定别名
     *
     * @param source 源字段
     * @param func   别名字段
     * @return this
     */
    public PlusLambdaQuery<T> sum(SFunction<T, ?> source, SFunction<T, ?> func) {
        aggfunc("sum", source, func);
        return typedThis;
    }

    /**
     * 聚合函数 min，获取指定字段的最小值
     *
     * @param source 字段
     * @return this
     */
    public PlusLambdaQuery<T> min(SFunction<T, ?> source) {
        aggfunc("min", source, null);
        return typedThis;
    }

    /**
     * 聚合函数 min，获取指定字段的最小值并指定别名
     *
     * @param source 源字段
     * @param func   别名字段
     * @return this
     */
    public PlusLambdaQuery<T> min(SFunction<T, ?> source, SFunction<T, ?> func) {
        aggfunc("min", source, func);
        return typedThis;
    }

    /**
     * 聚合函数 max，获取指定字段的最大值
     *
     * @param source 字段
     * @return this
     */
    public PlusLambdaQuery<T> max(SFunction<T, ?> source) {
        aggfunc("max", source, null);
        return typedThis;
    }

    /**
     * 聚合函数 max，获取指定字段的最大值并指定别名
     *
     * @param source 源字段
     * @param func   别名字段
     * @return this
     */
    public PlusLambdaQuery<T> max(SFunction<T, ?> source, SFunction<T, ?> func) {
        aggfunc("max", source, func);
        return typedThis;
    }

    /**
     * 聚合函数 count，计算指定字段的记录数
     *
     * @param source 字段
     * @return this
     */
    public PlusLambdaQuery<T> count(SFunction<T, ?> source) {
        aggfunc("count", source, null);
        return typedThis;
    }

    /**
     * 聚合函数 count，计算指定字段的记录数并指定别名
     *
     * @param source 源字段
     * @param func   别名字段
     * @return this
     */
    public PlusLambdaQuery<T> count(SFunction<T, ?> source, SFunction<T, ?> func) {
        aggfunc("count", source, func);
        return typedThis;
    }

    /**
     * 聚合函数 avg，计算指定字段的平均值并指定别名
     *
     * @param source 源字段
     * @param func   别名字段
     * @return this
     */
    public PlusLambdaQuery<T> avg(SFunction<T, ?> source, SFunction<T, ?> func) {
        aggfunc("avg", source, func);
        return typedThis;
    }

    /**
     * 通用聚合函数构建器
     *
     * @param name   聚合函数名称
     * @param source 源字段
     * @param func   别名字段，为null时使用默认别名规则
     * @return this
     */
    public PlusLambdaQuery<T> aggfunc(String name, SFunction<T, ?> source, SFunction<T, ?> func) {
        String agg = (StringUtils.isBlank(this.sqlSelect.getStringValue()) ?
            "" : this.sqlSelect.getStringValue() + ", ") +
            name + "(" + columnToString(source) + ") as " +
            (ObjectUtil.isNull(func) ? columnToString(source) + "_" + name : columnToString(func));
        this.sqlSelect.setStringValue(agg);
        return typedThis;
    }

    // ================ 内部方法 =================

    /**
     * 用于生成嵌套 sql
     * <p>故 sqlSelect 不向下传递</p>
     */
    @Override
    protected PlusLambdaQuery<T> instance() {
        return new PlusLambdaQuery<>(getEntity(), getEntityClass(), null, paramNameSeq, paramNameValuePairs,
            new MergeSegments(), paramAlias, SharedString.emptyString(), SharedString.emptyString(), SharedString.emptyString());
    }

    @Override
    public void clear() {
        super.clear();
        sqlSelect.toNull();
    }
}
