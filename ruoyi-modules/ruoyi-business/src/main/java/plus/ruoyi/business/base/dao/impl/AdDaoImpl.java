package plus.ruoyi.business.base.dao.impl;

import org.springframework.stereotype.Repository;
import plus.ruoyi.business.base.dao.IAdDao;
import plus.ruoyi.business.base.domain.Ad;
import plus.ruoyi.business.base.domain.bo.AdBo;
import plus.ruoyi.business.base.mapper.AdMapper;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.mybatis.core.dao.impl.BaseDaoImpl;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;

import java.util.Map;

/**
 * 广告配置数据访问实现
 *
 * @author 抓蛙师
 */
@Repository
public class AdDaoImpl extends BaseDaoImpl<AdMapper, Ad> implements IAdDao {

    /**
     * 构建查询条件
     *
     * @param bo 查询参数
     * @return 查询条件
     */
    @Override
    public PlusLambdaQuery<Ad> buildQueryWrapper(AdBo bo) {
        Map<String, Object> params = bo.getParams();
        PlusLambdaQuery<Ad> lqw = PlusLambdaQuery.of();

        // 精确匹配查询条件
        lqw.eq(Ad::getId, bo.getId());
        lqw.eq(Ad::getAppid, bo.getAppid());
        lqw.eq(Ad::getAdUnitId, bo.getAdUnitId());
        lqw.eq(Ad::getAdName, bo.getAdName());
        lqw.eq(Ad::getAdType, bo.getAdType());
        lqw.eq(Ad::getPosition, bo.getPosition());
        lqw.eq(Ad::getImg, bo.getImg());
        lqw.eq(Ad::getDescription, bo.getDescription());
        lqw.eq(Ad::getJumpAppid, bo.getJumpAppid());
        lqw.eq(Ad::getJumpPath, bo.getJumpPath());
        lqw.eq(Ad::getStyleConfig, bo.getStyleConfig());
        lqw.eq(Ad::getSortOrder, bo.getSortOrder());
        lqw.eq(Ad::getStatus, bo.getStatus());
        lqw.eq(Ad::getCreateTime, bo.getCreateTime());
        lqw.between(Ad::getCreateTime, params.get("beginCreateTime"), params.get("endCreateTime"));

        // 模糊查询（对非字符串字段使用 likeCast 确保跨数据库兼容）
        String searchValue = bo.getSearchValue();
        if (StringUtils.isNotBlank(searchValue)) {
            lqw.and(w -> w
                .likeCast(Ad::getId, searchValue)              // Long 类型
                .or().like(Ad::getAppid, searchValue)          // String 类型
                .or().like(Ad::getAdUnitId, searchValue)       // String 类型
                .or().like(Ad::getAdName, searchValue)         // String 类型
                .or().like(Ad::getAdType, searchValue)         // String 类型
                .or().like(Ad::getPosition, searchValue)       // String 类型
                .or().like(Ad::getImg, searchValue)            // String 类型
                .or().like(Ad::getDescription, searchValue)    // String 类型
                .or().like(Ad::getJumpAppid, searchValue)      // String 类型
                .or().like(Ad::getJumpPath, searchValue)       // String 类型
                .or().like(Ad::getStyleConfig, searchValue)    // String 类型
                .or().likeCast(Ad::getSortOrder, searchValue)  // Long 类型
                .or().like(Ad::getStatus, searchValue)         // String 类型
                .or().likeCast(Ad::getCreateTime, searchValue) // DateTime 类型
            );
        }
        return lqw;
    }
}
