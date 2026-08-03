package plus.ruoyi.generator.dao.impl;

import org.springframework.stereotype.Repository;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.mybatis.core.dao.impl.BaseDaoImpl;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.common.mybatis.core.query.PlusQuery;
import plus.ruoyi.generator.dao.IGenTableDao;
import plus.ruoyi.generator.domain.GenTable;
import plus.ruoyi.generator.mapper.GenTableMapper;

import java.util.List;
import java.util.Map;

/**
 * 代码生成表DAO实现
 *
 * @author Lion Li
 */
@Repository
public class GenTableDaoImpl extends BaseDaoImpl<GenTableMapper, GenTable> implements IGenTableDao {

    /**
     * 构建查询条件
     *
     * @param genTable 查询参数
     * @return 查询条件
     */
    @Override
    public PlusLambdaQuery<GenTable> buildQueryWrapper(GenTable genTable) {
        Map<String, Object> params = genTable.getParams();
        PlusQuery<GenTable> query = PlusQuery.of(GenTable.class);

        query
            .eq("data_name", genTable.getDataName())
            .like("lower(table_name)", StringUtils.lowerCase(genTable.getTableName()))
            .like("lower(table_comment)", StringUtils.lowerCase(genTable.getTableComment()))
            .between("create_time", params.get("beginTime"), params.get("endTime"))
            .orderByDesc("table_id");

        // 模糊搜索（跨数据库兼容：String 类型用 like，非 String 类型用 likeCast）
        String searchValue = genTable.getSearchValue();
        if (StringUtils.isNotBlank(searchValue)) {
            query.and(w -> w
                .like("table_name", searchValue)      // String
                .or().like("table_comment", searchValue) // String
                .or().like("data_name", searchValue)  // String
                .or().likeCast("table_id", searchValue)); // Long
        }

        return query.lambda();
    }

    /**
     * 查询所有表信息
     *
     * @return 表信息集合
     */
    @Override
    public List<GenTable> listAllGenTables() {
        return baseMapper.selectGenTableAll();
    }

    /**
     * 根据ID查询业务表信息
     *
     * @param id 业务表ID
     * @return 业务表信息
     */
    @Override
    public GenTable getGenTableById(Long id) {
        return baseMapper.selectGenTableById(id);
    }

    /**
     * 根据表名查询业务表信息
     *
     * @param tableName 表名称
     * @return 业务表信息
     */
    @Override
    public GenTable getGenTableByName(String tableName) {
        return baseMapper.selectGenTableByName(tableName);
    }

    /**
     * 查询指定数据源下的所有表名列表
     *
     * @param dataName 数据源名称
     * @return 当前数据库中的表名列表
     */
    @Override
    public List<String> listTableNames(String dataName) {
        return baseMapper.selectTableNameList(dataName);
    }

    /**
     * 根据子表名称查询父表信息
     *
     * @param subTableName 子表名称
     * @return 父表信息
     */
    @Override
    public GenTable getParentTableBySubTableName(String subTableName) {
        PlusLambdaQuery<GenTable> query = PlusLambdaQuery.of();
        query.eq(GenTable::getSubTableName, subTableName);
        return getOne(query);
    }

}
