package plus.ruoyi.system.oss.dao;

import plus.ruoyi.common.mybatis.core.dao.IBaseDao;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.system.oss.domain.SysOss;
import plus.ruoyi.system.oss.domain.bo.SysOssBo;

import java.util.List;

/**
 * 文件上传DAO接口
 *
 * @author 抓蛙师
 */
public interface ISysOssDao extends IBaseDao<SysOss> {

    /**
     * 根据业务对象构建查询条件
     */
    PlusLambdaQuery<SysOss> buildQueryWrapper(SysOssBo bo);

    /**
     * 根据URL查询文件
     *
     * @param url 文件URL
     * @return 文件实体
     */
    SysOss getByUrl(String url);

    /**
     * 根据URLs批量查询文件
     *
     * @param urls URL列表
     * @return 文件实体列表
     */
    List<SysOss> listByUrls(List<String> urls);

    /**
     * 统计指定目录ID列表下的文件数量
     *
     * @param directoryIds 目录ID列表
     * @return 文件数量
     */
    long countByDirectoryIds(List<Long> directoryIds);
}
