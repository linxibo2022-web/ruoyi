package plus.ruoyi.generator.dao.impl;

import org.springframework.stereotype.Repository;
import plus.ruoyi.common.mybatis.core.dao.impl.BaseDaoImpl;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.generator.dao.IGenTableColumnDao;
import plus.ruoyi.generator.domain.GenTableColumn;
import plus.ruoyi.generator.mapper.GenTableColumnMapper;

import java.util.List;

/**
 * 代码生成表字段DAO实现
 *
 * @author Lion Li
 */
@Repository
public class GenTableColumnDaoImpl extends BaseDaoImpl<GenTableColumnMapper, GenTableColumn> implements IGenTableColumnDao {

    /**
     * 根据表ID查询列信息列表
     *
     * @param tableId 表ID
     * @return 列信息列表
     */
    @Override
    public List<GenTableColumn> listByTableId(Long tableId) {
        PlusLambdaQuery<GenTableColumn> query = PlusLambdaQuery.of(GenTableColumn.class)
            .eq(GenTableColumn::getTableId, tableId)
            .orderByAsc(GenTableColumn::getSort);
        return list(query);
    }

    /**
     * 根据表ID删除列信息
     *
     * @param tableIds 表ID列表
     * @return 是否删除成功
     */
    @Override
    public boolean deleteByTableIds(List<Long> tableIds) {
        PlusLambdaQuery<GenTableColumn> query = PlusLambdaQuery.of(GenTableColumn.class)
            .in(GenTableColumn::getTableId, tableIds);
        return baseMapper.delete(query) > 0;
    }

}
