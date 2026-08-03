package plus.ruoyi.business.base.service;

import plus.ruoyi.business.base.domain.bo.PaymentBo;
import plus.ruoyi.business.base.domain.vo.PaymentVo;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;

import java.util.Collection;
import java.util.List;

/**
 * 支付配置服务接口
 *
 * @author 抓蛙师
 */
public interface IPaymentService {

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return 视图对象
     */
    PaymentVo get(Long id);

    /**
     * 查询列表
     *
     * @param bo 查询参数
     * @return 列表数据
     */
    List<PaymentVo> list(PaymentBo bo);

    /**
     * 分页查询
     *
     * @param bo        查询参数
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    PageResult<PaymentVo> page(PaymentBo bo, PageQuery pageQuery);

    /**
     * 新增
     *
     * @param bo 业务对象
     * @return 主键ID
     */
    Long add(PaymentBo bo);

    /**
     * 修改
     *
     * @param bo 业务对象
     * @return 影响行数
     */
    int update(PaymentBo bo);

    /**
     * 批量删除
     *
     * @param ids ID集合
     * @return 影响行数
     */
    int batchDelete(Collection<Long> ids);

    /**
     * 批量保存
     *
     * @param boList 业务对象集合
     * @return 影响行数
     */
    int batchSave(List<PaymentBo> boList);

    /**
     * 获取所有启用的支付配置
     *
     * @return 启用的支付配置列表
     */
    List<PaymentVo> listEnabled();

    /**
     * 查询选项列表
     * 用于下拉选择、关联查询等场景,只返回必要字段
     *
     * @return 选项列表
     */
    List<PaymentVo> listForOption();
}
