package plus.ruoyi.business.base.dao;

import plus.ruoyi.business.base.domain.Ad;
import plus.ruoyi.business.base.domain.bo.AdBo;
import plus.ruoyi.common.mybatis.core.dao.IBaseDao;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;

/**
 * 广告配置DAO接口
 *
 * @author 抓蛙师
 */
public interface IAdDao extends IBaseDao<Ad> {

    /**
     * 根据业务对象构建查询条件
     */
    PlusLambdaQuery<Ad> buildQueryWrapper(AdBo bo);
}
