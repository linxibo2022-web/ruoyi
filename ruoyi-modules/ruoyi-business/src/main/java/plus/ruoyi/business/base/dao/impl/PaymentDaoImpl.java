package plus.ruoyi.business.base.dao.impl;

import org.springframework.stereotype.Repository;
import plus.ruoyi.business.base.dao.IPaymentDao;
import plus.ruoyi.business.base.domain.Payment;
import plus.ruoyi.business.base.domain.bo.PaymentBo;
import plus.ruoyi.business.base.mapper.PaymentMapper;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.mybatis.core.dao.impl.BaseDaoImpl;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;

import java.util.List;
import java.util.Map;

/**
 * 支付配置数据访问实现
 *
 * @author 抓蛙师
 */
@Repository
public class PaymentDaoImpl extends BaseDaoImpl<PaymentMapper, Payment> implements IPaymentDao {

    /**
     * 构建查询条件
     *
     * @param bo 查询参数
     * @return 查询条件
     */
    @Override
    public PlusLambdaQuery<Payment> buildQueryWrapper(PaymentBo bo) {
        Map<String, Object> params = bo.getParams();
        PlusLambdaQuery<Payment> lqw = PlusLambdaQuery.of();

        // 精确匹配查询条件
        lqw.eq(Payment::getId, bo.getId());
        lqw.eq(Payment::getType, bo.getType());
        lqw.eq(Payment::getMchName, bo.getMchName());
        lqw.eq(Payment::getMchId, bo.getMchId());
        lqw.eq(Payment::getMchKey, bo.getMchKey());
        lqw.eq(Payment::getApiV3Key, bo.getApiV3Key());
        lqw.eq(Payment::getCertPath, bo.getCertPath());
        lqw.eq(Payment::getKeyPath, bo.getKeyPath());
        lqw.eq(Payment::getPlatformCertPath, bo.getPlatformCertPath());
        lqw.eq(Payment::getP12CertPath, bo.getP12CertPath());
        lqw.eq(Payment::getCertSerialNo, bo.getCertSerialNo());
        lqw.eq(Payment::getStatus, bo.getStatus());
        lqw.eq(Payment::getCreateTime, bo.getCreateTime());
        lqw.between(Payment::getCreateTime, params.get("beginCreateTime"), params.get("endCreateTime"));

        // 模糊查询（对非字符串字段使用 likeCast 确保跨数据库兼容）
        String searchValue = bo.getSearchValue();
        if (StringUtils.isNotBlank(searchValue)) {
            lqw.and(w -> w
                .likeCast(Payment::getId, searchValue)             // Long 类型
                .or().like(Payment::getType, searchValue)          // String 类型
                .or().like(Payment::getMchName, searchValue)       // String 类型
                .or().like(Payment::getMchId, searchValue)         // String 类型
                .or().like(Payment::getMchKey, searchValue)        // String 类型
                .or().like(Payment::getApiV3Key, searchValue)      // String 类型
                .or().like(Payment::getCertPath, searchValue)      // String 类型
                .or().like(Payment::getKeyPath, searchValue)       // String 类型
                .or().like(Payment::getPlatformCertPath, searchValue) // String 类型
                .or().like(Payment::getP12CertPath, searchValue)   // String 类型
                .or().like(Payment::getCertSerialNo, searchValue)  // String 类型
            );
        }
        return lqw;
    }

    /**
     * 根据商户号查询支付配置
     *
     * @param mchId  商户号
     * @param status 状态
     * @return 支付配置
     */
    @Override
    public Payment getByMchId(String mchId, String status) {
        PlusLambdaQuery<Payment> lqw = PlusLambdaQuery.of(Payment.class)
            .eq(Payment::getMchId, mchId)
            .eq(Payment::getStatus, status);
        return getOne(lqw);
    }

    /**
     * 根据支付类型和状态查询配置列表
     *
     * @param type   支付类型
     * @param status 状态
     * @return 配置列表
     */
    @Override
    public List<Payment> listByTypeAndStatus(String type, String status) {
        PlusLambdaQuery<Payment> lqw = PlusLambdaQuery.of(Payment.class)
            .eq(Payment::getType, type)
            .eq(Payment::getStatus, status);
        return list(lqw);
    }

    /**
     * 根据状态统计配置数量
     *
     * @param status 状态
     * @return 配置数量
     */
    @Override
    public long countByStatus(String status) {
        PlusLambdaQuery<Payment> lqw = PlusLambdaQuery.of(Payment.class)
            .eq(Payment::getStatus, status);
        return count(lqw);
    }

    /**
     * 根据商户号查询是否存在(排除指定ID)
     *
     * @param mchId     商户号
     * @param excludeId 排除的ID(可为null)
     * @return 是否存在
     */
    @Override
    public boolean existsByMchId(String mchId, Long excludeId) {
        PlusLambdaQuery<Payment> lqw = PlusLambdaQuery.of(Payment.class)
            .eq(Payment::getMchId, mchId);
        if (excludeId != null) {
            lqw.ne(Payment::getId, excludeId);
        }
        return exists(lqw);
    }

    /**
     * 根据商户号和状态判断是否存在
     *
     * @param mchId  商户号
     * @param status 状态
     * @return 是否存在
     */
    @Override
    public boolean existsByMchIdAndStatus(String mchId, String status) {
        PlusLambdaQuery<Payment> lqw = PlusLambdaQuery.of(Payment.class)
            .eq(Payment::getMchId, mchId)
            .eq(Payment::getStatus, status);
        return exists(lqw);
    }

    /**
     * 根据状态查询配置列表
     *
     * @param status 状态
     * @return 配置列表
     */
    @Override
    public List<Payment> listByStatus(String status) {
        PlusLambdaQuery<Payment> lqw = PlusLambdaQuery.of(Payment.class)
            .eq(Payment::getStatus, status);
        return list(lqw);
    }
}
