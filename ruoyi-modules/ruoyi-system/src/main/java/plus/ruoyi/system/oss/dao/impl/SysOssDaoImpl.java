package plus.ruoyi.system.oss.dao.impl;

import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Repository;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.mybatis.core.dao.impl.BaseDaoImpl;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.common.oss.constant.OssConstant;
import plus.ruoyi.system.oss.dao.ISysOssDao;
import plus.ruoyi.system.oss.domain.SysOss;
import plus.ruoyi.system.oss.domain.bo.SysOssBo;
import plus.ruoyi.system.oss.mapper.SysOssMapper;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 文件上传数据访问实现
 *
 * @author 抓蛙师
 */
@Repository
public class SysOssDaoImpl extends BaseDaoImpl<SysOssMapper, SysOss> implements ISysOssDao {

    /**
     * 构建查询条件
     *
     * @param bo 查询参数
     * @return 查询条件
     */
    @Override
    public PlusLambdaQuery<SysOss> buildQueryWrapper(SysOssBo bo) {
        Map<String, Object> params = bo.getParams();
        PlusLambdaQuery<SysOss> lqw = PlusLambdaQuery.of(SysOss.class);

        // 目录查询逻辑
        if (Objects.nonNull(bo.getDirectoryId())) {
            //UNCATEGORIZED代表未分类
            if (OssConstant.UNCATEGORIZED.equals(bo.getDirectoryId())) {
                lqw.isNull(SysOss::getDirectoryId);
            } else if (!OssConstant.ALL.equals(bo.getDirectoryId())) {
                //不等于ALL则根据对应的目录id查询
                lqw.eq(SysOss::getDirectoryId, bo.getDirectoryId());
            }
            //不拼接目录id时查询全部
        }

        lqw.like(SysOss::getFileName, bo.getFileName());
        lqw.like(SysOss::getOriginalName, bo.getOriginalName());
        if (StringUtils.isNotBlank(bo.getFileSuffix())) {
            lqw.in(SysOss::getFileSuffix, StringUtils.splitToList(bo.getFileSuffix()));
        }
        lqw.eq(SysOss::getUrl, bo.getUrl());
        lqw.between(SysOss::getCreateTime, params.get("beginCreateTime"), params.get("endCreateTime"));
        lqw.eq(SysOss::getCreateBy, bo.getCreateBy());
        lqw.eq(SysOss::getService, bo.getService());

        // 模糊搜索（跨数据库兼容：String 类型用 like，非 String 类型用 likeCast）
        String searchValue = bo.getSearchValue();
        if (ObjectUtil.isNotEmpty(searchValue)) {
            lqw.and(w -> w
                .like(SysOss::getFileName, searchValue)             // String
                .or().like(SysOss::getOriginalName, searchValue)    // String
                .or().like(SysOss::getFileSuffix, searchValue)      // String
                .or().like(SysOss::getUrl, searchValue)             // String
                .or().like(SysOss::getService, searchValue)         // String
                .or().likeCast(SysOss::getDirectoryId, searchValue) // Long
                .or().likeCast(SysOss::getCreateBy, searchValue)    // Long
                .or().likeCast(SysOss::getOssId, searchValue));     // Long
        }

        return lqw;
    }

    /**
     * 根据URL查询文件
     *
     * @param url 文件URL
     * @return 文件实体
     */
    @Override
    public SysOss getByUrl(String url) {
        PlusLambdaQuery<SysOss> lqw = PlusLambdaQuery.of(SysOss.class)
            .eq(SysOss::getUrl, url);
        return getOne(lqw);
    }

    /**
     * 根据URLs批量查询文件
     *
     * @param urls URL列表
     * @return 文件实体列表
     */
    @Override
    public List<SysOss> listByUrls(List<String> urls) {
        PlusLambdaQuery<SysOss> lqw = PlusLambdaQuery.of(SysOss.class)
            .in(SysOss::getUrl, urls);
        return list(lqw);
    }

    /**
     * 统计指定目录ID列表下的文件数量
     */
    @Override
    public long countByDirectoryIds(List<Long> directoryIds) {
        PlusLambdaQuery<SysOss> lqw = PlusLambdaQuery.of(SysOss.class)
            .in(SysOss::getDirectoryId, directoryIds);
        return count(lqw);
    }

}
