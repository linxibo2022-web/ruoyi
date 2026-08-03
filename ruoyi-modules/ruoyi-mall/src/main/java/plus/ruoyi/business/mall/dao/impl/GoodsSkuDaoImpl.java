package plus.ruoyi.business.mall.dao.impl;

import org.springframework.stereotype.Repository;
import plus.ruoyi.business.mall.dao.IGoodsSkuDao;
import plus.ruoyi.business.mall.domain.GoodsSku;
import plus.ruoyi.business.mall.domain.bo.GoodsSkuBo;
import plus.ruoyi.business.mall.mapper.GoodsSkuMapper;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.mybatis.core.dao.impl.BaseDaoImpl;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;

import java.util.Map;

/**
 * 商品SKU数据访问实现
 *
 * @author 抓蛙师
 * @date 2025-11-01
 */
@Repository
public class GoodsSkuDaoImpl extends BaseDaoImpl<GoodsSkuMapper, GoodsSku> implements IGoodsSkuDao {

    /**
     * 构建查询条件
     */
    @Override
    public PlusLambdaQuery<GoodsSku> buildQueryWrapper(GoodsSkuBo bo) {
        Map<String, Object> params = bo.getParams();
        PlusLambdaQuery<GoodsSku> lqw = PlusLambdaQuery.of();

        // 精确匹配查询条件
        lqw.eq(GoodsSku::getId, bo.getId());
        lqw.eq(GoodsSku::getGoodsId, bo.getGoodsId());
        lqw.eq(GoodsSku::getSkuCode, bo.getSkuCode());
        lqw.eq(GoodsSku::getStatus, bo.getStatus());

        // 模糊搜索
        String searchValue = bo.getSearchValue();
        if (StringUtils.isNotBlank(searchValue)) {
            lqw.and(w -> w
                .like(GoodsSku::getSkuName, searchValue)
                .or().like(GoodsSku::getSkuCode, searchValue)
            );
        }

        // 时间范围查询
        lqw.between(GoodsSku::getCreateTime, params.get("beginCreateTime"), params.get("endCreateTime"));

        return lqw;
    }
}
