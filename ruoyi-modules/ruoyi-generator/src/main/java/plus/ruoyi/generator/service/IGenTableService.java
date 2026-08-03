package plus.ruoyi.generator.service;

import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.generator.domain.GenTable;
import plus.ruoyi.generator.domain.GenTableColumn;

import java.util.List;
import java.util.Map;

/**
 * 代码生成表服务接口
 *
 * @author Lion Li
 */
public interface IGenTableService {

    /**
     * 分页查询
     *
     * @param genTable  查询参数
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    PageResult<GenTable> page(GenTable genTable, PageQuery pageQuery);

    /**
     * 查询业务字段列表
     *
     * @param tableId 业务字段编号
     * @return 业务字段集合
     */
    List<GenTableColumn> listGenTableColumnsByTableId(Long tableId);

    /**
     * 查询据库列表
     *
     * @param genTable 业务信息
     * @param pageQuery 分页参数
     * @return 数据库表集合
     */
    PageResult<GenTable> pageDbTables(GenTable genTable, PageQuery pageQuery);

    /**
     * 查询据库列表
     *
     * @param tableNames 表名称组
     * @param dataName   数据源名称
     * @return 数据库表集合
     */
    List<GenTable> listDbTablesByNames(String[] tableNames, String dataName);

    /**
     * 查询所有表信息
     *
     * @return 表信息集合
     */
    List<GenTable> listAllGenTables();

    /**
     * 查询业务信息
     *
     * @param id 业务ID
     * @return 业务信息
     */
    GenTable getGenTableById(Long id);

    /**
     * 修改业务
     *
     * @param genTable 业务信息
     */
    void updateGenTable(GenTable genTable);

    /**
     * 删除业务信息
     *
     * @param tableIds 需要删除的表数据ID
     */
    void deleteGenTablesByIds(Long[] tableIds);

    /**
     * 导入表结构
     *
     * @param tableList 导入表列表
     * @param dataName  数据源名称
     */
    void importGenTables(List<GenTable> tableList, String dataName);

    /**
     * 根据表名称查询列信息
     *
     * @param tableName 表名称
     * @param dataName  数据源名称
     * @return 列信息
     */
    List<GenTableColumn> listDbTableColumnsByName(String tableName, String dataName);

    /**
     * 预览代码
     *
     * @param tableId 表编号
     * @return 预览数据列表
     */
    Map<String, String> previewCode(Long tableId);

    /**
     * 生成代码（下载方式）
     *
     * @param tableId 表名称
     * @return 数据
     */
    byte[] downloadCode(Long tableId);

    /**
     * 生成代码（自定义路径）
     *
     * @param tableId 表名称
     * @return 生成结果（包含菜单导入状态）
     */
    plus.ruoyi.generator.domain.vo.CodeGenResult generatorCode(Long tableId);

    /**
     * 同步数据库
     *
     * @param tableId 表名称
     */
    void syncDatabase(Long tableId);

    /**
     * 批量生成代码（下载方式）
     *
     * @param tableIds 表ID数组
     * @return 数据
     */
    byte[] downloadCode(String[] tableIds);

    /**
     * 修改保存参数校验
     *
     * @param genTable 业务信息
     */
    void validateEdit(GenTable genTable);
}
