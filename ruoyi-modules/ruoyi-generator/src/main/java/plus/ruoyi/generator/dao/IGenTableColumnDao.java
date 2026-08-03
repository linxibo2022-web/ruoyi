package plus.ruoyi.generator.dao;

import plus.ruoyi.common.mybatis.core.dao.IBaseDao;
import plus.ruoyi.generator.domain.GenTableColumn;

import java.util.List;

/**
 * 代码生成表字段DAO接口
 *
 * @author Lion Li
 */
public interface IGenTableColumnDao extends IBaseDao<GenTableColumn> {

    /**
     * 根据表ID查询列信息列表
     *
     * @param tableId 表ID
     * @return 列信息列表
     */
    List<GenTableColumn> listByTableId(Long tableId);

    /**
     * 根据表ID删除列信息
     *
     * @param tableIds 表ID列表
     * @return 是否删除成功
     */
    boolean deleteByTableIds(List<Long> tableIds);
}
