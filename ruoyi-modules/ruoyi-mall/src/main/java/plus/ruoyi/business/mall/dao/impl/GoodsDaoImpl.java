package plus.ruoyi.business.mall.dao.impl;

import org.springframework.stereotype.Repository;
import plus.ruoyi.business.mall.dao.IGoodsDao;
import plus.ruoyi.business.mall.domain.Goods;
import plus.ruoyi.business.mall.domain.bo.GoodsBo;
import plus.ruoyi.business.mall.mapper.GoodsMapper;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.mybatis.core.dao.impl.BaseDaoImpl;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;

import java.util.Map;

/**
 * 商品数据访问实现
 *
 * @author 抓蛙师
 */
@Repository
public class GoodsDaoImpl extends BaseDaoImpl<GoodsMapper, Goods> implements IGoodsDao {

    /**
     * 构建查询条件
     *
     * @param bo 查询参数
     * @return 查询条件
     */
    @Override
    public PlusLambdaQuery<Goods> buildQueryWrapper(GoodsBo bo) {
        Map<String, Object> params = bo.getParams();
        PlusLambdaQuery<Goods> lqw = PlusLambdaQuery.of();

        // 精确匹配查询条件
        lqw.eq(Goods::getId, bo.getId());
        lqw.eq(Goods::getCategory, bo.getCategory());
        lqw.eq(Goods::getCode, bo.getCode());
        lqw.eq(Goods::getName, bo.getName());
        lqw.eq(Goods::getImg, bo.getImg());
        lqw.eq(Goods::getOriginalPrice, bo.getOriginalPrice());
        lqw.eq(Goods::getDiscount, bo.getDiscount());
        lqw.eq(Goods::getPrice, bo.getPrice());
        lqw.eq(Goods::getDescription, bo.getDescription());
        lqw.eq(Goods::getStock, bo.getStock());
        lqw.eq(Goods::getSalesCount, bo.getSalesCount());
        lqw.eq(Goods::getStatus, bo.getStatus());
        lqw.eq(Goods::getSortOrder, bo.getSortOrder());
        lqw.eq(Goods::getCreateTime, bo.getCreateTime());
        lqw.between(Goods::getCreateTime, params.get("beginCreateTime"), params.get("endCreateTime"));

        // 模糊查询（对非字符串字段使用 likeCast 确保跨数据库兼容）
        String searchValue = bo.getSearchValue();
        if (StringUtils.isNotBlank(searchValue)) {
            lqw.and(w -> w
                .likeCast(Goods::getId, searchValue)                  // Long 类型
                .or().like(Goods::getCategory, searchValue)           // String 类型
                .or().like(Goods::getCode, searchValue)               // String 类型
                .or().like(Goods::getName, searchValue)               // String 类型
                .or().likeCast(Goods::getOriginalPrice, searchValue)  // BigDecimal 类型
                .or().likeCast(Goods::getDiscount, searchValue)       // BigDecimal 类型
                .or().likeCast(Goods::getPrice, searchValue)          // BigDecimal 类型
                .or().like(Goods::getDescription, searchValue)        // String 类型
                .or().likeCast(Goods::getStock, searchValue)          // Long 类型
                .or().likeCast(Goods::getSalesCount, searchValue)     // Long 类型
                .or().like(Goods::getStatus, searchValue)             // String 类型
                .or().likeCast(Goods::getSortOrder, searchValue)      // Long 类型
                .or().likeCast(Goods::getCreateTime, searchValue)     // DateTime 类型
            );
        }
        return lqw;
    }
}
