package plus.ruoyi.common.mybatis.core.dao;

import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaUpdate;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * 基础DAO接口
 * 提供通用的CRUD操作方法
 *
 * @param <T> 实体类型
 * @author 抓蛙师
 */
public interface IBaseDao<T> {

    // ==================== 查询操作 ====================

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return 实体对象
     */
    T getById(Serializable id);

    /**
     * 根据ID集合批量查询
     *
     * @param ids 主键ID集合
     * @return 实体列表
     */
    List<T> listByIds(Collection<? extends Serializable> ids);

    /**
     * 根据条件查询单个对象
     * 如果查询结果超过1条,默认抛出异常
     *
     * @param wrapper 查询条件
     * @return 实体对象,不存在则返回null
     */
    T getOne(PlusLambdaQuery<T> wrapper);

    /**
     * 根据条件查询单个对象
     *
     * @param wrapper 查询条件
     * @param throwEx 是否在结果不唯一时抛出异常
     * @return 实体对象,不存在则返回null
     */
    T getOne(PlusLambdaQuery<T> wrapper, boolean throwEx);

    /**
     * 条件查询列表
     *
     * @param wrapper 查询条件
     * @return 实体列表
     */
    List<T> list(PlusLambdaQuery<T> wrapper);

    /**
     * 查询所有数据
     *
     * @return 全部实体列表
     */
    List<T> listAll();

    /**
     * 分页查询
     *
     * @param wrapper   查询条件
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    PageResult<T> page(PlusLambdaQuery<T> wrapper, PageQuery pageQuery);

    /**
     * 查询单列并转换为目标类型列表
     * <p>
     * 注意：此方法基于 selectObjs，返回的是 select 的第一列值，而非实体对象。
     * mapper 接收的是 Object 类型（单列值），需要自行转换为目标类型。
     * <p>
     * 示例：
     * <pre>{@code
     * // 获取所有 SKU 的 ID 列表
     * PlusLambdaQuery<GoodsSku> query = PlusLambdaQuery.of()
     *     .select(GoodsSku::getId)
     *     .eq(GoodsSku::getGoodsId, goodsId);
     * List<Long> skuIds = goodsSkuDao.mapList(query, obj -> (Long) obj);
     * }</pre>
     *
     * @param wrapper 查询条件（需要通过 select() 指定查询列）
     * @param mapper  转换函数，接收单列值（Object），返回目标类型
     * @param <C>     目标对象类型参数
     * @return 转换后的对象列表
     */
    <C> List<C> mapList(PlusLambdaQuery<T> wrapper, Function<? super Object, C> mapper);

    // ==================== 统计与判断 ====================

    /**
     * 统计数量
     *
     * @param wrapper 查询条件
     * @return 记录数
     */
    Long count(PlusLambdaQuery<T> wrapper);

    /**
     * 根据ID判断是否存在
     *
     * @param id 主键ID
     * @return 是否存在
     */
    boolean exists(Serializable id);

    /**
     * 根据条件判断是否存在
     *
     * @param wrapper 查询条件
     * @return 是否存在
     */
    boolean exists(PlusLambdaQuery<T> wrapper);

    // ==================== 插入操作 ====================

    /**
     * 插入实体
     *
     * @param entity 实体对象
     * @return 影响行数
     */
    int insert(T entity);

    /**
     * 批量插入（纯插入，不进行主键判断）
     * 适用于无主键或不需要更新的批量插入场景
     *
     * @param entities 实体集合
     * @return 影响行数
     */
    int batchInsert(Collection<T> entities);

    // ==================== 保存操作(插入或更新) ====================

    /**
     * 保存实体(根据主键判断插入或更新)
     *
     * @param entity 实体对象
     * @return 影响行数
     */
    int save(T entity);

    /**
     * 批量保存(根据主键判断插入或更新)
     *
     * @param entities 实体集合
     * @return 影响行数
     */
    int batchSave(Collection<T> entities);

    // ==================== 更新操作 ====================

    /**
     * 根据ID更新
     *
     * @param entity 实体对象
     * @return 影响行数
     */
    int updateById(T entity);

    /**
     * 条件更新
     *
     * @param entity  实体对象
     * @param wrapper 更新条件
     * @return 影响行数
     */
    int update(T entity, PlusLambdaQuery<T> wrapper);

    /**
     * 创建Lambda更新条件构建器
     * 用于构建灵活的更新条件和更新字段
     * <p>
     * 示例：
     * <pre>{@code
     * // 条件更新
     * xxxDao.lambdaUpdate()
     *     .set(Xxx::getName, "新名称")           // 不忽略 null，可以设置字段为 null
     *     .setIfNotNull(Xxx::getRemark, remark)  // 忽略 null 和空字符串
     *     .eq(Xxx::getId, id)
     *     .update();
     * }</pre>
     *
     * @return Lambda更新条件构建器
     */
    PlusLambdaUpdate<T> lambdaUpdate();

    // ==================== 删除操作 ====================

    /**
     * 根据ID删除
     *
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteById(Serializable id);

    /**
     * 根据ID集合批量删除
     *
     * @param ids 主键ID集合
     * @return 影响行数
     */
    int deleteByIds(Collection<? extends Serializable> ids);

    /**
     * 条件删除
     *
     * @param wrapper 删除条件
     * @return 影响行数
     */
    int delete(PlusLambdaQuery<T> wrapper);
}
