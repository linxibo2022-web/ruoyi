package plus.ruoyi.generator.dao;

import plus.ruoyi.common.mybatis.core.dao.IBaseDao;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.generator.domain.GenTable;

import java.util.List;

/**
 * 代码生成表DAO接口
 *
 * @author Lion Li
 */
public interface IGenTableDao extends IBaseDao<GenTable> {

    /**
     * 根据业务对象构建查询条件
     *
     * @param genTable 查询参数
     * @return 查询条件
     */
    PlusLambdaQuery<GenTable> buildQueryWrapper(GenTable genTable);

    /**
     * 查询所有表信息
     *
     * @return 表信息集合
     */
    List<GenTable> listAllGenTables();

    /**
     * 根据ID查询业务表信息
     *
     * @param id 业务表ID
     * @return 业务表信息
     */
    GenTable getGenTableById(Long id);

    /**
     * 根据表名查询业务表信息
     *
     * @param tableName 表名称
     * @return 业务表信息
     */
    GenTable getGenTableByName(String tableName);

    /**
     * 查询指定数据源下的所有表名列表
     *
     * @param dataName 数据源名称
     * @return 当前数据库中的表名列表
     */
    List<String> listTableNames(String dataName);

    /**
     * 根据子表名称查询父表信息
     *
     * @param subTableName 子表名称
     * @return 父表信息
     */
    GenTable getParentTableBySubTableName(String subTableName);
}
