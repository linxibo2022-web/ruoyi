package plus.ruoyi.system.oss.service;

import cn.hutool.core.lang.tree.Tree;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.system.oss.domain.bo.SysOssDirectoryBo;
import plus.ruoyi.system.oss.domain.vo.SysOssDirectoryVo;

import java.util.Collection;
import java.util.List;

/**
 * OSS目录Service接口
 *
 * @author 抓蛙师
 * @date 2025-04-14
 */
public interface ISysOssDirectoryService {

    /**
     * 根据ID查询
     *
     * @param directoryId 主键ID
     * @return 视图对象
     */
    SysOssDirectoryVo get(Long directoryId);

    /**
     * 查询列表
     *
     * @param bo 查询参数
     * @return 列表数据
     */
    List<SysOssDirectoryVo> list(SysOssDirectoryBo bo);

    /**
     * 分页查询
     *
     * @param bo        查询参数
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    PageResult<SysOssDirectoryVo> page(SysOssDirectoryBo bo, PageQuery pageQuery);

    /**
     * 新增
     *
     * @param bo 业务对象
     * @return 主键ID
     */
    Long add(SysOssDirectoryBo bo);

    /**
     * 修改
     *
     * @param bo 业务对象
     * @return 影响行数
     */
    int update(SysOssDirectoryBo bo);

    /**
     * 批量删除
     *
     * @param ids ID集合
     * @return 影响行数
     */
    int batchDelete(Collection<Long> ids);

    /**
     * 批量保存
     *
     * @param boList 业务对象集合
     * @return 影响行数
     */
    int batchSave(List<SysOssDirectoryBo> boList);

    /**
     * 查询OSS目录树结构信息
     *
     * @param bo OSS目录信息
     * @return OSS目录树信息集合
     */
    List<Tree<Long>> getOssDirectoryTreeOptions(SysOssDirectoryBo bo);

    /**
     * 移动文件到指定目录
     *
     * @param directoryId 目标目录ID
     * @param ossIds      OSS文件ID列表
     * @return 结果
     */
    boolean moveOss(Long directoryId, List<Long> ossIds);

    /**
     * 根据目录路径获取目录ID，不存在则创建
     * 支持多级目录自动创建，如：/文档/办公/图书
     *
     * @param directoryPath 目录路径，如：/文档/办公/图书
     * @return 目录ID
     */
    Long getOrCreateDirectoryByPath(String directoryPath);

    /**
     * 根据目录ID获取目录名称
     *
     * @param directoryId 目录ID
     * @return 目录名称（带/前缀），如果不存在返回null
     */
    String getDirectoryNameById(Long directoryId);
}
