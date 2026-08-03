package plus.ruoyi.system.oss.dao;

import plus.ruoyi.common.mybatis.core.dao.IBaseDao;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.system.oss.domain.SysOssDirectory;
import plus.ruoyi.system.oss.domain.bo.SysOssDirectoryBo;

import java.util.List;

/**
 * OSS目录DAO接口
 *
 * @author 抓蛙师
 */
public interface ISysOssDirectoryDao extends IBaseDao<SysOssDirectory> {

    /**
     * 根据业务对象构建查询条件
     *
     * @param bo 查询参数
     * @return 查询条件
     */
    PlusLambdaQuery<SysOssDirectory> buildQueryWrapper(SysOssDirectoryBo bo);

    /**
     * 根据目录名称和父目录ID查询目录
     *
     * @param directoryName 目录名称
     * @param parentId      父目录ID
     * @return 目录实体
     */
    SysOssDirectory getByNameAndParentId(String directoryName, Long parentId);

    /**
     * 根据祖先ID查询所有子目录
     *
     * @param ancestorId 祖先ID
     * @return 子目录列表
     */
    List<SysOssDirectory> listChildrenByAncestors(String ancestorId);

    /**
     * 批量更新目录
     *
     * @param directories 目录列表
     * @return 是否成功
     */
    boolean batchUpdateById(List<SysOssDirectory> directories);

    /**
     * 统计指定父目录ID列表下的子目录数量
     *
     * @param parentIds 父目录ID列表
     * @return 子目录数量
     */
    long countByParentIds(List<Long> parentIds);

    /**
     * 根据目录路径查询目录
     *
     * @param directoryPath 目录路径
     * @return 目录实体
     */
    SysOssDirectory getByDirectoryPath(String directoryPath);
}
