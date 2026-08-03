package plus.ruoyi.business.mall.dao;

import plus.ruoyi.business.mall.domain.Goods;
import plus.ruoyi.business.mall.domain.bo.GoodsBo;
import plus.ruoyi.common.mybatis.core.dao.IBaseDao;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;

/**
 * 商品DAO接口
 *
 * @author 抓蛙师
 */
public interface IGoodsDao extends IBaseDao<Goods> {

    /**
     * 根据业务对象构建查询条件
     */
    PlusLambdaQuery<Goods> buildQueryWrapper(GoodsBo bo);
}
