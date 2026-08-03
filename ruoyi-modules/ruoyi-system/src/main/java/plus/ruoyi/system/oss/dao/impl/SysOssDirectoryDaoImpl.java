package plus.ruoyi.system.oss.dao.impl;

import org.springframework.stereotype.Repository;
import plus.ruoyi.common.mybatis.core.dao.impl.BaseDaoImpl;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.system.oss.dao.ISysOssDirectoryDao;
import plus.ruoyi.system.oss.domain.SysOssDirectory;
import plus.ruoyi.system.oss.domain.bo.SysOssDirectoryBo;
import plus.ruoyi.system.oss.mapper.SysOssDirectoryMapper;

import java.util.List;
import java.util.Map;

/**
 * OSS目录数据访问实现
 *
 * @author 抓蛙师
 */
@Repository
public class SysOssDirectoryDaoImpl extends BaseDaoImpl<SysOssDirectoryMapper, SysOssDirectory> implements ISysOssDirectoryDao {

    /**
     * 构建查询条件
     *
     * @param bo 查询参数
     * @return 查询条件
     */
    @Override
    public PlusLambdaQuery<SysOssDirectory> buildQueryWrapper(SysOssDirectoryBo bo) {
        Map<String, Object> params = bo.getParams();
        PlusLambdaQuery<SysOssDirectory> lqw = PlusLambdaQuery.of(SysOssDirectory.class);
        lqw.eq(SysOssDirectory::getDirectoryId, bo.getDirectoryId());
        lqw.eq(SysOssDirectory::getParentId, bo.getParentId());
        lqw.eq(SysOssDirectory::getAncestors, bo.getAncestors());
        lqw.eq(SysOssDirectory::getDirectoryName, bo.getDirectoryName());
        lqw.eq(SysOssDirectory::getDirectoryPath, bo.getDirectoryPath());
        lqw.eq(SysOssDirectory::getOrderNum, bo.getOrderNum());
        lqw.eq(SysOssDirectory::getStatus, bo.getStatus());
        lqw.eq(SysOssDirectory::getIsDefault, bo.getIsDefault());
        lqw.eq(SysOssDirectory::getCreateTime, bo.getCreateTime());
        lqw.between(SysOssDirectory::getCreateTime, params.get("beginCreateTime"), params.get("endCreateTime"));
        return lqw;
    }

    /**
     * 根据目录名称和父目录ID查询目录
     *
     * @param directoryName 目录名称
     * @param parentId      父目录ID
     * @return 目录实体
     */
    @Override
    public SysOssDirectory getByNameAndParentId(String directoryName, Long parentId) {
        PlusLambdaQuery<SysOssDirectory> lqw = PlusLambdaQuery.of(SysOssDirectory.class)
            .eq(SysOssDirectory::getDirectoryName, directoryName)
            .eq(SysOssDirectory::getParentId, parentId);
        return getOne(lqw);
    }

    /**
     * 根据祖先ID查询所有子目录
     *
     * @param ancestorId 祖先ID
     * @return 子目录列表
     */
    @Override
    public List<SysOssDirectory> listChildrenByAncestors(String ancestorId) {
        return baseMapper.selectChildrenByAncestors(ancestorId);
    }

    /**
     * 批量更新目录
     *
     * @param directories 目录列表
     * @return 是否成功
     */
    @Override
    public boolean batchUpdateById(List<SysOssDirectory> directories) {
        return batchSave(directories) > 0;
    }

    /**
     * 统计指定父目录ID列表下的子目录数量
     */
    @Override
    public long countByParentIds(List<Long> parentIds) {
        PlusLambdaQuery<SysOssDirectory> lqw = PlusLambdaQuery.of(SysOssDirectory.class)
            .in(SysOssDirectory::getParentId, parentIds);
        return count(lqw);
    }

    /**
     * 根据目录路径查询目录
     */
    @Override
    public SysOssDirectory getByDirectoryPath(String directoryPath) {
        PlusLambdaQuery<SysOssDirectory> lqw = PlusLambdaQuery.of(SysOssDirectory.class)
            .eq(SysOssDirectory::getDirectoryPath, directoryPath);
        return getOne(lqw);
    }

}
