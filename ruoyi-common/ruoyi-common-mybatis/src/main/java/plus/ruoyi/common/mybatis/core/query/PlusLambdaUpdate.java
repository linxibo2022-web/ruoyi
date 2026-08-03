package plus.ruoyi.common.mybatis.core.query;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.AbstractLambdaWrapper;
import com.baomidou.mybatisplus.core.conditions.SharedString;
import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments;
import com.baomidou.mybatisplus.core.conditions.update.Update;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import plus.ruoyi.common.mybatis.helper.DataBaseHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * MyBatis-Plus Lambda更新增强器 (PlusLambdaUpdate)
 * <p>
 * 本类扩展了AbstractLambdaWrapper，提供类型安全的Lambda更新功能：
 * <p>
 * 1. SET 方法
 * - set: set(column, value) - 设置字段值，不忽略null（可以将字段设为null）
 * - setIfNotNull: setIfNotNull(column, value) - 设置字段值，当值为null或空字符串时忽略
 * <p>
 * 2. 静态工厂方法
 * - of(): 创建空更新实例
 * - of(Class<E>): 基于实体类创建更新实例
 * - of(E): 基于实体对象创建更新实例
 * <p>
 * 3. 条件方法增强（与PlusLambdaQuery一致，自动处理无效值）
 * - 比较操作: eq, ne, gt, ge, lt, le
 * - 范围操作: between, notBetween
 * - 集合操作: in, notIn
 * - 模糊匹配: like, likeLeft, likeRight
 * - 跨数据库模糊匹配: likeCast, likeLeftCast, likeRightCast
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 更新用户状态
 * PlusLambdaUpdate<User> update = PlusLambdaUpdate.of(User.class)
 *     .set(User::getStatus, "1")           // 设置状态，即使值为null也会更新
 *     .setIfNotNull(User::getName, name)   // 只有name不为null时才更新
 *     .eq(User::getId, userId);
 *
 * userDao.update(null, update);
 *
 * // 跨数据库兼容的模糊匹配更新（适用于非字符串字段）
 * xxxDao.lambdaUpdate()
 *     .set(Xxx::getStatus, "0")
 *     .likeCast(Xxx::getId, "100")         // 更新ID包含"100"的记录（PostgreSQL兼容）
 *     .update();
 * }</pre>
 *
 * @param <T> 实体类型
 * @author 抓蛙师
 */
@SuppressWarnings("serial")
public class PlusLambdaUpdate<T> extends AbstractLambdaWrapper<T, PlusLambdaUpdate<T>>
    implements Update<PlusLambdaUpdate<T>, SFunction<T, ?>> {

    /**
     * SQL 更新字段内容，例如：name='张三', age=18
     */
    private final List<String> sqlSet;

    /**
     * BaseMapper 引用，用于执行更新操作
     */
    private BaseMapper<T> baseMapper;

    /**
     * 无参构造方法
     */
    public PlusLambdaUpdate() {
        this((T) null);
    }

    /**
     * 带实体对象的构造方法
     *
     * @param entity 实体对象
     */
    public PlusLambdaUpdate(T entity) {
        super.setEntity(entity);
        super.initNeed();
        this.sqlSet = new ArrayList<>();
    }

    /**
     * 带实体类的构造方法
     *
     * @param entityClass 实体类
     */
    public PlusLambdaUpdate(Class<T> entityClass) {
        super.setEntityClass(entityClass);
        super.initNeed();
        this.sqlSet = new ArrayList<>();
    }

    /**
     * 完整构造方法
     */
    PlusLambdaUpdate(T entity, Class<T> entityClass, List<String> sqlSet, AtomicInteger paramNameSeq,
                     Map<String, Object> paramNameValuePairs, MergeSegments mergeSegments, SharedString paramAlias,
                     SharedString lastSql, SharedString sqlComment, SharedString sqlFirst) {
        super.setEntity(entity);
        super.setEntityClass(entityClass);
        this.paramNameSeq = paramNameSeq;
        this.paramNameValuePairs = paramNameValuePairs;
        this.expression = mergeSegments;
        this.sqlSet = sqlSet;
        this.paramAlias = paramAlias;
        this.lastSql = lastSql;
        this.sqlComment = sqlComment;
        this.sqlFirst = sqlFirst;
    }

    // ================ 静态工厂方法 =================

    /**
     * 创建LambdaUpdate实例（推荐）
     *
     * <p>示例：
     * <pre>{@code
     * // 创建一个更新
     * PlusLambdaUpdate.of(User.class)
     *     .set(User::getStatus, "1")
     *     .eq(User::getId, userId)
     * }</pre>
     *
     * @param <E>         实体类型
     * @param entityClass 实体类
     * @return LambdaUpdate实例
     */
    public static <E> PlusLambdaUpdate<E> of(Class<E> entityClass) {
        return new PlusLambdaUpdate<>(entityClass);
    }

    /**
     * 创建LambdaUpdate实例
     *
     * <p>示例：
     * <pre>{@code
     * // 创建一个空更新
     * PlusLambdaUpdate.of()
     * }</pre>
     *
     * @param <E> 实体类型
     * @return LambdaUpdate实例
     */
    public static <E> PlusLambdaUpdate<E> of() {
        return new PlusLambdaUpdate<>();
    }

    /**
     * 创建包含实体对象的LambdaUpdate实例
     *
     * <p>示例：
     * <pre>{@code
     * // 通过实体创建更新
     * User user = new User();
     * user.setName("张三");
     * PlusLambdaUpdate.of(user)
     * }</pre>
     *
     * @param <E>    实体类型
     * @param entity 实体对象
     * @return LambdaUpdate实例
     */
    public static <E> PlusLambdaUpdate<E> of(E entity) {
        return new PlusLambdaUpdate<>(entity);
    }

    /**
     * 创建包含Mapper的LambdaUpdate实例
     * <p>
     * 用于支持链式调用后直接执行update()方法
     *
     * <p>示例：
     * <pre>{@code
     * // 通过Mapper创建更新，支持链式调用后直接执行
     * xxxDao.lambdaUpdate()
     *     .set(Xxx::getName, "新名称")
     *     .eq(Xxx::getId, id)
     *     .update();
     * }</pre>
     *
     * @param <E>    实体类型
     * @param mapper BaseMapper实例
     * @return LambdaUpdate实例
     */
    public static <E> PlusLambdaUpdate<E> of(BaseMapper<E> mapper) {
        PlusLambdaUpdate<E> update = new PlusLambdaUpdate<>();
        update.baseMapper = mapper;
        return update;
    }

    // ================ 执行方法 =================

    /**
     * 执行更新操作
     * <p>
     * 注意：使用此方法前必须通过 {@link #of(BaseMapper)} 工厂方法创建实例，
     * 或通过 DAO 层的 lambdaUpdate() 方法获取。
     *
     * <p>示例：
     * <pre>{@code
     * // 通过 DAO 层使用
     * xxxDao.lambdaUpdate()
     *     .set(Xxx::getName, "新名称")
     *     .setIfNotNull(Xxx::getRemark, remark)
     *     .eq(Xxx::getId, id)
     *     .update();
     * }</pre>
     *
     * @return 影响的行数
     * @throws IllegalStateException 如果 BaseMapper 未设置
     */
    public int update() {
        if (baseMapper == null) {
            throw new IllegalStateException("BaseMapper not set. Use of(BaseMapper) factory method or obtain instance from DAO.lambdaUpdate().");
        }
        return baseMapper.update(null, this);
    }

    /**
     * 执行带实体的更新操作
     * <p>
     * 实体对象的非null字段会被更新，同时应用 SET 子句中的设置
     *
     * <p>示例：
     * <pre>{@code
     * Xxx entity = new Xxx();
     * entity.setStatus("1");
     *
     * xxxDao.lambdaUpdate()
     *     .set(Xxx::getUpdateTime, new Date())
     *     .eq(Xxx::getId, id)
     *     .update(entity);
     * }</pre>
     *
     * @param entity 实体对象
     * @return 影响的行数
     * @throws IllegalStateException 如果 BaseMapper 未设置
     */
    public int update(T entity) {
        if (baseMapper == null) {
            throw new IllegalStateException("BaseMapper not set. Use of(BaseMapper) factory method or obtain instance from DAO.lambdaUpdate().");
        }
        return baseMapper.update(entity, this);
    }

    // ================ SET 方法 =================

    /**
     * 设置字段值（不忽略null值）
     * <p>
     * 可以将字段设置为null，适用于需要清空字段值的场景
     *
     * @param column 字段
     * @param val    值（可以为null）
     * @return this
     */
    @Override
    public PlusLambdaUpdate<T> set(SFunction<T, ?> column, Object val) {
        return set(true, column, val);
    }

    /**
     * 设置字段值（不忽略null值）
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值（可以为null）
     * @return this
     */
    @Override
    public PlusLambdaUpdate<T> set(boolean condition, SFunction<T, ?> column, Object val) {
        return maybeDo(condition, () -> {
            String columnName = columnToString(column);
            sqlSet.add(columnName + Constants.EQUALS + formatParam(null, val));
        });
    }

    /**
     * 设置字段值（带 TypeHandler/JdbcType 映射，不忽略null值）
     *
     * @param column  字段
     * @param val     值（可以为null）
     * @param mapping TypeHandler 或 JdbcType 映射
     * @return this
     */
    @Override
    public PlusLambdaUpdate<T> set(SFunction<T, ?> column, Object val, String mapping) {
        return set(true, column, val, mapping);
    }

    /**
     * 设置字段值（带 TypeHandler/JdbcType 映射，不忽略null值）
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值（可以为null）
     * @param mapping   TypeHandler 或 JdbcType 映射
     * @return this
     */
    @Override
    public PlusLambdaUpdate<T> set(boolean condition, SFunction<T, ?> column, Object val, String mapping) {
        return maybeDo(condition, () -> {
            String columnName = columnToString(column);
            sqlSet.add(columnName + Constants.EQUALS + formatParam(mapping, val));
        });
    }

    /**
     * 设置字段值（忽略null值和空字符串）
     * <p>
     * 只有当值有效（不为null且字符串不为空）时才会设置
     *
     * @param column 字段
     * @param val    值
     * @return this
     */
    public PlusLambdaUpdate<T> setIfNotNull(SFunction<T, ?> column, Object val) {
        return setIfNotNull(true, column, val);
    }

    /**
     * 设置字段值（忽略null值和空字符串）
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @return this
     */
    public PlusLambdaUpdate<T> setIfNotNull(boolean condition, SFunction<T, ?> column, Object val) {
        if (checkValueEffective(val)) {
            return set(condition, column, val);
        }
        return typedThis;
    }

    /**
     * 设置SQL片段（直接添加到SET子句）
     *
     * @param setSql SQL片段，如 "name = '张三'"
     * @param params 参数（用于格式化SQL）
     * @return this
     */
    @Override
    public PlusLambdaUpdate<T> setSql(boolean condition, String setSql, Object... params) {
        return maybeDo(condition, () -> {
            if (StringUtils.isNotBlank(setSql)) {
                if (params != null && params.length > 0) {
                    sqlSet.add(String.format(setSql.replace("%", "%%").replace("{0}", "%s"), params));
                } else {
                    sqlSet.add(setSql);
                }
            }
        });
    }

    /**
     * 设置字段自增（适用于数字类型字段）
     * <p>
     * 示例：setIncrBy(User::getAge, 1) 生成 SQL: age = age + 1
     *
     * @param column 字段
     * @param val    增加的值
     * @return this
     */
    @Override
    public PlusLambdaUpdate<T> setIncrBy(boolean condition, SFunction<T, ?> column, Number val) {
        return maybeDo(condition, () -> {
            String columnName = columnToString(column);
            sqlSet.add(columnName + Constants.EQUALS + columnName + " + " + val);
        });
    }

    /**
     * 设置字段自减（适用于数字类型字段）
     * <p>
     * 示例：setDecrBy(User::getAge, 1) 生成 SQL: age = age - 1
     *
     * @param column 字段
     * @param val    减少的值
     * @return this
     */
    @Override
    public PlusLambdaUpdate<T> setDecrBy(boolean condition, SFunction<T, ?> column, Number val) {
        return maybeDo(condition, () -> {
            String columnName = columnToString(column);
            sqlSet.add(columnName + Constants.EQUALS + columnName + " - " + val);
        });
    }

    /**
     * Lambda方式设置SQL片段
     * <p>
     * 示例：setSql(User::getName, "'张三'") 生成 SQL: name = '张三'
     *
     * @param column 字段
     * @param val    SQL表达式
     * @return this
     */
    public PlusLambdaUpdate<T> setSql(SFunction<T, ?> column, String val) {
        return setSql(true, column, val);
    }

    /**
     * Lambda方式设置SQL片段
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       SQL表达式
     * @return this
     */
    public PlusLambdaUpdate<T> setSql(boolean condition, SFunction<T, ?> column, String val) {
        return maybeDo(condition, () -> {
            String columnName = columnToString(column);
            sqlSet.add(columnName + Constants.EQUALS + val);
        });
    }

    /**
     * 获取SET SQL片段
     *
     * @return SET SQL片段
     */
    @Override
    public String getSqlSet() {
        if (CollectionUtils.isEmpty(sqlSet)) {
            return null;
        }
        return String.join(StringPool.COMMA, sqlSet);
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
    public PlusLambdaUpdate<T> eq(SFunction<T, ?> column, Object val) {
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
    public PlusLambdaUpdate<T> ne(SFunction<T, ?> column, Object val) {
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
    public PlusLambdaUpdate<T> gt(SFunction<T, ?> column, Object val) {
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
    public PlusLambdaUpdate<T> ge(SFunction<T, ?> column, Object val) {
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
    public PlusLambdaUpdate<T> lt(SFunction<T, ?> column, Object val) {
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
    public PlusLambdaUpdate<T> le(SFunction<T, ?> column, Object val) {
        if (checkValueEffective(val)) {
            super.le(column, val);
        }
        return typedThis;
    }

    /**
     * BETWEEN 值1 AND 值2，智能处理两个值的null和空字符串情况
     *
     * @param column 字段
     * @param val1   值1
     * @param val2   值2
     * @return this
     */
    @Override
    public PlusLambdaUpdate<T> between(SFunction<T, ?> column, Object val1, Object val2) {
        // Oracle 兼容：将日期格式字符串转为 Date，避免字符串隐式转换触发 ORA-01861
        val1 = tryParseDateStr(val1);
        val2 = tryParseDateStr(val2);
        boolean val1Effective = checkValueEffective(val1);
        boolean val2Effective = checkValueEffective(val2);

        if (val1Effective && val2Effective) {
            super.between(column, val1, val2);
        } else if (val1Effective) {
            super.ge(column, val1);
        } else if (val2Effective) {
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
    public PlusLambdaUpdate<T> notBetween(SFunction<T, ?> column, Object val1, Object val2) {
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
    public PlusLambdaUpdate<T> in(SFunction<T, ?> column, Collection<?> coll) {
        if (CollectionUtils.isNotEmpty(coll)) {
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
    public PlusLambdaUpdate<T> notIn(SFunction<T, ?> column, Collection<?> coll) {
        if (CollectionUtils.isNotEmpty(coll)) {
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
    public PlusLambdaUpdate<T> like(SFunction<T, ?> column, Object val) {
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
    public PlusLambdaUpdate<T> likeRight(SFunction<T, ?> column, Object val) {
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
    public PlusLambdaUpdate<T> likeLeft(SFunction<T, ?> column, Object val) {
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
    public PlusLambdaUpdate<T> likeCast(SFunction<T, ?> column, Object val) {
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
    public PlusLambdaUpdate<T> likeRightCast(SFunction<T, ?> column, Object val) {
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
    public PlusLambdaUpdate<T> likeLeftCast(SFunction<T, ?> column, Object val) {
        if (checkValueEffective(val)) {
            String columnName = columnToString(column);
            String castColumn = DataBaseHelper.castToVarchar(columnName);
            super.apply(castColumn + " LIKE {0}", "%" + val);
        }
        return typedThis;
    }

    // ================ 内部方法 =================

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
     * 为避免误伤数字范围更新（如 between(age, 18, 60)）与非日期字符串（如脏数据 "100"）：
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
                    // 解析失败保底原样返回，绝不影响原有更新行为
                    return val;
                }
            }
        }
        return val;
    }

    /**
     * 用于生成嵌套 sql
     */
    @Override
    protected PlusLambdaUpdate<T> instance() {
        return new PlusLambdaUpdate<>(getEntity(), getEntityClass(), new ArrayList<>(), paramNameSeq, paramNameValuePairs,
            new MergeSegments(), paramAlias, SharedString.emptyString(), SharedString.emptyString(), SharedString.emptyString());
    }

    @Override
    public void clear() {
        super.clear();
        sqlSet.clear();
    }
}
