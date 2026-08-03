package plus.ruoyi.business.mall.dao;

import plus.ruoyi.business.mall.domain.GoodsSku;
import plus.ruoyi.business.mall.domain.bo.GoodsSkuBo;
import plus.ruoyi.common.mybatis.core.dao.IBaseDao;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;

/**
 * 商品SKUDAO接口
 *
 * @author 抓蛙师
 * @date 2025-11-01
 */
public interface IGoodsSkuDao extends IBaseDao<GoodsSku> {

    /**
     * 根据业务对象构建查询条件
     */
    PlusLambdaQuery<GoodsSku> buildQueryWrapper(GoodsSkuBo bo);
}
