package plus.ruoyi.common.mybatis.core.dao.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import org.springframework.beans.factory.annotation.Autowired;
import plus.ruoyi.common.core.utils.StreamUtils;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaUpdate;
import plus.ruoyi.common.mybatis.core.dao.IBaseDao;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * 基础DAO实现
 * 封装MyBatis-Plus的通用操作
 *
 * @param <M> Mapper类型
 * @param <T> 实体类型
 * @author 抓蛙师
 */
public abstract class BaseDaoImpl<M extends BaseMapper<T>, T> implements IBaseDao<T> {

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    protected M baseMapper;

    // ==================== 查询操作 ====================

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return 实体对象
     */
    @Override
    public T getById(Serializable id) {
        return baseMapper.selectById(id);
    }

    /**
     * 根据ID集合批量查询
     *
     * @param ids 主键ID集合
     * @return 实体列表
     */
    @Override
    public List<T> listByIds(Collection<? extends Serializable> ids) {
        return baseMapper.selectByIds(ids);
    }

    /**
     * 根据条件查询单个对象
     * 如果查询结果超过1条,默认抛出异常
     *
     * @param wrapper 查询条件
     * @return 实体对象
     */
    @Override
    public T getOne(PlusLambdaQuery<T> wrapper) {
        return baseMapper.selectOne(wrapper, true);
    }

    /**
     * 根据条件查询单个对象
     *
     * @param wrapper 查询条件
     * @param throwEx 是否在结果不唯一时抛出异常
     * @return 实体对象
     */
    @Override
    public T getOne(PlusLambdaQuery<T> wrapper, boolean throwEx) {
        return baseMapper.selectOne(wrapper, throwEx);
    }

    /**
     * 条件查询列表
     *
     * @param wrapper 查询条件
     * @return 实体列表
     */
    @Override
    public List<T> list(PlusLambdaQuery<T> wrapper) {
        return baseMapper.selectList(wrapper);
    }

    /**
     * 查询所有数据
     *
     * @return 全部实体列表
     */
    @Override
    public List<T> listAll() {
        return baseMapper.selectList(PlusLambdaQuery.of());
    }

    /**
     * 分页查询
     *
     * @param wrapper   查询条件
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    @Override
    public PageResult<T> page(PlusLambdaQuery<T> wrapper, PageQuery pageQuery) {
        Page<T> page = baseMapper.selectPage(pageQuery.build(), wrapper);
        return PageResult.of(page);
    }

    /**
     * 查询单列并转换为目标类型列表
     *
     * @param wrapper 查询条件
     * @param mapper  转换函数
     * @param <C>     目标对象类型参数
     * @return 转换后的对象列表
     */
    @Override
    public <C> List<C> mapList(PlusLambdaQuery<T> wrapper, Function<? super Object, C> mapper) {
        return StreamUtils.toList(baseMapper.selectObjs(wrapper), mapper);
    }

    // ==================== 统计与判断 ====================

    /**
     * 统计数量
     *
     * @param wrapper 查询条件
     * @return 记录数
     */
    @Override
    public Long count(PlusLambdaQuery<T> wrapper) {
        return baseMapper.selectCount(wrapper);
    }

    /**
     * 根据ID判断是否存在
     *
     * @param id 主键id
     * @return 是否存在
     */
    @Override
    public boolean exists(Serializable id) {
        return baseMapper.selectById(id) != null;
    }

    /**
     * 判断是否存在
     *
     * @param wrapper 查询条件
     * @return 是否存在
     */
    @Override
    public boolean exists(PlusLambdaQuery<T> wrapper) {
        return baseMapper.exists(wrapper);
    }

    // ==================== 插入操作 ====================

    /**
     * 插入实体
     *
     * @param entity 实体对象
     * @return 影响行数
     */
    @Override
    public int insert(T entity) {
        return baseMapper.insert(entity);
    }

    /**
     * 批量插入（纯插入，不进行主键判断）
     * 适用于无主键或不需要更新的批量插入场景
     *
     * @param entities 实体集合
     * @return 影响行数
     */
    @Override
    public int batchInsert(Collection<T> entities) {
        if (CollUtil.isEmpty(entities)) {
            return 0;
        }
        return Db.saveBatch(entities) ? entities.size() : 0;
    }

    // ==================== 保存操作(插入或更新) ====================

    /**
     * 保存实体(根据主键判断插入或更新)
     *
     * @param entity 实体对象
     * @return 影响行数
     */
    @Override
    public int save(T entity) {
        return Db.saveOrUpdate(entity) ? 1 : 0;
    }

    /**
     * 批量保存(根据主键判断插入或更新)
     *
     * @param entities 实体集合
     * @return 影响行数
     */
    @Override
    public int batchSave(Collection<T> entities) {
        if (CollUtil.isEmpty(entities)) {
            return 0;
        }
        return Db.saveOrUpdateBatch(entities) ? entities.size() : 0;
    }

    // ==================== 更新操作 ====================

    /**
     * 根据ID更新
     *
     * @param entity 实体对象
     * @return 影响行数
     */
    @Override
    public int updateById(T entity) {
        return baseMapper.updateById(entity);
    }

    /**
     * 条件更新
     *
     * @param entity  实体对象
     * @param wrapper 更新条件
     * @return 影响行数
     */
    @Override
    public int update(T entity, PlusLambdaQuery<T> wrapper) {
        return baseMapper.update(entity, wrapper);
    }

    /**
     * 创建Lambda更新条件构建器
     *
     * @return Lambda更新条件构建器
     */
    @Override
    public PlusLambdaUpdate<T> lambdaUpdate() {
        return PlusLambdaUpdate.of(baseMapper);
    }

    // ==================== 删除操作 ====================

    /**
     * 根据ID删除
     *
     * @param id 主键ID
     * @return 影响行数
     */
    @Override
    public int deleteById(Serializable id) {
        return baseMapper.deleteById(id);
    }

    /**
     * 根据ID集合批量删除
     *
     * @param ids 主键ID集合
     * @return 影响行数
     */
    @Override
    public int deleteByIds(Collection<? extends Serializable> ids) {
        return baseMapper.deleteByIds(ids);
    }

    /**
     * 条件删除
     *
     * @param wrapper 删除条件
     * @return 影响行数
     */
    @Override
    public int delete(PlusLambdaQuery<T> wrapper) {
        return baseMapper.delete(wrapper);
    }
}
