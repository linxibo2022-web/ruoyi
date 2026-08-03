package plus.ruoyi.business.base.service.impl;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plus.ruoyi.business.base.dao.IPaymentDao;
import plus.ruoyi.business.base.domain.Payment;
import plus.ruoyi.business.base.domain.bo.PaymentBo;
import plus.ruoyi.business.base.domain.vo.PaymentVo;
import plus.ruoyi.business.base.service.IPaymentService;
import plus.ruoyi.common.core.dict.DictEnableStatus;
import plus.ruoyi.common.core.domain.dto.PaymentDTO;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.service.PaymentService;
import plus.ruoyi.common.core.utils.MapstructUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.common.tenant.helper.TenantHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 支付配置服务实现
 *
 * @author 抓蛙师
 */
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements IPaymentService, PaymentService {

    private final IPaymentDao paymentDao;

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return 视图对象
     */
    @Override
    public PaymentVo get(Long id) {
        Payment entity = paymentDao.getById(id);
        return MapstructUtils.convert(entity, PaymentVo.class);
    }

    /**
     * 查询列表
     *
     * @param bo 查询参数
     * @return 列表数据
     */
    @Override
    public List<PaymentVo> list(PaymentBo bo) {
        PlusLambdaQuery<Payment> wrapper = paymentDao.buildQueryWrapper(bo);
        List<Payment> entities = paymentDao.list(wrapper);
        return MapstructUtils.convert(entities, PaymentVo.class);
    }

    /**
     * 分页查询
     *
     * @param bo        查询参数
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    @Override
    public PageResult<PaymentVo> page(PaymentBo bo, PageQuery pageQuery) {
        PlusLambdaQuery<Payment> wrapper = paymentDao.buildQueryWrapper(bo);
        PageResult<Payment> entityPage = paymentDao.page(wrapper, pageQuery);
        return entityPage.convert(PaymentVo.class);
    }

    /**
     * 新增
     *
     * @param bo 业务对象
     * @return 主键ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(PaymentBo bo) {
        Payment entity = MapstructUtils.convert(bo, Payment.class);
        beforeSave(entity);
        paymentDao.insert(entity);
        return entity.getId();
    }

    /**
     * 修改
     *
     * @param bo 业务对象
     * @return 影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(PaymentBo bo) {
        if (bo.getId() == null) {
            throw ServiceException.of("支付配置ID不能为空");
        }
        if (!paymentDao.exists(bo.getId())) {
            throw ServiceException.of("支付配置不存在");
        }
        Payment entity = MapstructUtils.convert(bo, Payment.class);
        beforeSave(entity);
        return paymentDao.updateById(entity);
    }

    /**
     * 批量删除
     *
     * @param ids ID集合
     * @return 影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            throw ServiceException.of("ID集合不能为空");
        }
        beforeDelete(ids);
        return paymentDao.deleteByIds(ids);
    }

    /**
     * 批量保存
     *
     * @param boList 业务对象集合
     * @return 影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSave(List<PaymentBo> boList) {
        if (CollUtil.isEmpty(boList)) {
            return 0;
        }
        List<Payment> entities = new ArrayList<>(boList.size());
        for (PaymentBo bo : boList) {
            Payment entity = MapstructUtils.convert(bo, Payment.class);
            beforeSave(entity);
            entities.add(entity);
        }
        return paymentDao.batchSave(entities);
    }

    /**
     * 保存前钩子方法
     * 子类可重写此方法实现数据校验、默认值设置等逻辑
     *
     * @param entity 实体对象
     */
    protected void beforeSave(Payment entity) {
        // 校验商户号是否重复
        if (StringUtils.isNotBlank(entity.getMchId())) {
            if (paymentDao.existsByMchId(entity.getMchId(), entity.getId())) {
                throw ServiceException.of("商户号已存在: " + entity.getMchId());
            }
        }
    }

    /**
     * 删除前钩子方法
     * 子类可重写此方法实现关联数据校验、清理等逻辑
     *
     * @param ids 待删除的ID集合
     */
    protected void beforeDelete(Collection<Long> ids) {
        // 默认实现为空,子类按需重写
    }

    /**
     * 根据支付类型获取支付配置列表
     *
     * @param type     支付类型
     * @param tenantId 租户id
     * @return 支付配置列表
     */
    @Override
    public List<PaymentDTO> listPaymentByType(String type, String tenantId) {
        List<Payment> list;
        if (StringUtils.isBlank(tenantId)) {
            list = TenantHelper.ignore(() ->
                paymentDao.listByTypeAndStatus(type, DictEnableStatus.ENABLE.getValue())
            );
        } else {
            list = TenantHelper.dynamic(tenantId, () ->
                paymentDao.listByTypeAndStatus(type, DictEnableStatus.ENABLE.getValue())
            );
        }
        return MapstructUtils.convert(list, PaymentDTO.class);
    }

    /**
     * 根据商户号获取支付配置
     *
     * @param mchId 商户号
     * @return 支付配置
     */
    @Override
    public PaymentDTO getByMchId(String mchId) {
        Payment payment = paymentDao.getByMchId(mchId, DictEnableStatus.ENABLE.getValue());
        return MapstructUtils.convert(payment, PaymentDTO.class);
    }

    /**
     * 根据支付配置ID获取支付配置
     *
     * @param paymentId 支付配置ID
     * @return 支付配置
     */
    @Override
    public PaymentDTO getById(Long paymentId) {
        Payment payment = paymentDao.getById(paymentId);
        return MapstructUtils.convert(payment, PaymentDTO.class);
    }

    /**
     * 检查商户号是否存在且有效
     *
     * @param mchId 商户号
     * @return true-存在且有效,false-不存在或无效
     */
    @Override
    public boolean existsValidMchId(String mchId) {
        return paymentDao.existsByMchIdAndStatus(mchId, DictEnableStatus.ENABLE.getValue());
    }

    /**
     * 获取支付配置总数
     *
     * @return 支付配置总数
     */
    @Override
    public long countPayments() {
        return paymentDao.countByStatus(DictEnableStatus.ENABLE.getValue());
    }

    /**
     * 获取所有启用的支付配置
     */
    @Override
    public List<PaymentVo> listEnabled() {
        // 使用DAO层封装的方法
        List<Payment> list = paymentDao.listByStatus(DictEnableStatus.ENABLE.getValue());
        return MapstructUtils.convert(list, PaymentVo.class);
    }

    /**
     * 查询选项列表
     * 用于下拉选择、关联查询等场景,只返回必要字段
     */
    @Override
    public List<PaymentVo> listForOption() {
        PlusLambdaQuery<Payment> wrapper = PlusLambdaQuery.of();
        // 只查询启用状态的数据
        wrapper.eq(Payment::getStatus, DictEnableStatus.ENABLE.getValue());
        // 只选择必要的字段:ID和显示字段
        wrapper.select(Payment::getId, Payment::getMchName);
        // 排序
        wrapper.orderByDesc(Payment::getId);
        // 使用分页限制返回数量,防止数据过多
        PageQuery pageQuery = new PageQuery(1000, 1);
        PageResult<Payment> entityPage = paymentDao.page(wrapper, pageQuery);
        return MapstructUtils.convert(entityPage.getRecords(), PaymentVo.class);
    }
}
