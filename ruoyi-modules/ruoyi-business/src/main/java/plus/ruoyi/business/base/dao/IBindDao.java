package plus.ruoyi.business.base.dao;

import plus.ruoyi.business.base.domain.Bind;
import plus.ruoyi.business.base.domain.bo.BindBo;
import plus.ruoyi.common.mybatis.core.dao.IBaseDao;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;

/**
 * 账号绑定DAO接口
 *
 * @author 抓蛙师
 */
public interface IBindDao extends IBaseDao<Bind> {

    /**
     * 根据业务对象构建查询条件
     */
    PlusLambdaQuery<Bind> buildQueryWrapper(BindBo bo);

    /**
     * 根据平台类型和openid查询绑定信息
     *
     * @param platformType 平台类型
     * @param openid       openid
     * @return 绑定信息
     */
    Bind getByPlatformAndOpenid(String platformType, String openid);
}
