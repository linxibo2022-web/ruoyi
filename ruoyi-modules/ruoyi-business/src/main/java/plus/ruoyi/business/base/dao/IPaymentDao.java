package plus.ruoyi.business.base.dao;

import plus.ruoyi.business.base.domain.Payment;
import plus.ruoyi.business.base.domain.bo.PaymentBo;
import plus.ruoyi.common.mybatis.core.dao.IBaseDao;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;

import java.util.List;

/**
 * 支付配置DAO接口
 *
 * @author 抓蛙师
 */
public interface IPaymentDao extends IBaseDao<Payment> {

    /**
     * 根据业务对象构建查询条件
     */
    PlusLambdaQuery<Payment> buildQueryWrapper(PaymentBo bo);

    /**
     * 根据商户号查询支付配置
     *
     * @param mchId  商户号
     * @param status 状态
     * @return 支付配置
     */
    Payment getByMchId(String mchId, String status);

    /**
     * 根据支付类型和状态查询配置列表
     *
     * @param type   支付类型
     * @param status 状态
     * @return 配置列表
     */
    List<Payment> listByTypeAndStatus(String type, String status);

    /**
     * 根据状态统计配置数量
     *
     * @param status 状态
     * @return 配置数量
     */
    long countByStatus(String status);

    /**
     * 根据商户号查询是否存在(排除指定ID)
     *
     * @param mchId     商户号
     * @param excludeId 排除的ID(可为null)
     * @return 是否存在
     */
    boolean existsByMchId(String mchId, Long excludeId);

    /**
     * 根据商户号和状态判断是否存在
     *
     * @param mchId  商户号
     * @param status 状态
     * @return 是否存在
     */
    boolean existsByMchIdAndStatus(String mchId, String status);

    /**
     * 根据状态查询配置列表
     *
     * @param status 状态
     * @return 配置列表
     */
    List<Payment> listByStatus(String status);
}
