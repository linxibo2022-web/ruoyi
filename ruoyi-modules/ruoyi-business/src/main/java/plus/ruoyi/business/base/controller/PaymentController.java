package plus.ruoyi.business.base.controller;

import java.util.List;
import java.util.ArrayList;

import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import plus.ruoyi.common.core.constant.I18nKeys;
import plus.ruoyi.common.core.utils.MapstructUtils;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.excel.core.ExcelResult;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import plus.ruoyi.common.idempotent.annotation.RepeatSubmit;
import plus.ruoyi.common.log.annotation.Log;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.core.validate.AddGroup;
import plus.ruoyi.common.core.validate.EditGroup;
import plus.ruoyi.common.core.dict.DictOperType;
import plus.ruoyi.common.excel.utils.ExcelUtil;
import plus.ruoyi.business.base.domain.vo.PaymentVo;
import plus.ruoyi.business.base.domain.bo.PaymentBo;
import plus.ruoyi.business.base.service.IPaymentService;
import plus.ruoyi.common.core.domain.vo.DictItemVo;
import plus.ruoyi.common.core.utils.StreamUtils;
import plus.ruoyi.common.pay.initializer.PayConfigInitializer;
import plus.ruoyi.common.tenant.helper.TenantHelper;
import plus.ruoyi.business.base.listener.PlatformConfigSyncListener;

/**
 * 支付配置
 * <p>
 * 只有当支付模块启用时才加载此控制器
 *
 * @author 抓蛙师
 * @date 2025-05-14
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/base/payment")
@ConditionalOnProperty(prefix = "module", name = "pay-enabled", havingValue = "true", matchIfMissing = true)
public class PaymentController {

    private final IPaymentService paymentService;

    /**
     * 查询支付配置列表
     */
    @SaCheckPermission("base:payment:query")
    @GetMapping("/pagePayments")
    public R<PageResult<PaymentVo>> pagePayments(PaymentBo bo, PageQuery pageQuery) {
        return R.ok(paymentService.page(bo, pageQuery));
    }

    /**
     * 获取支付配置详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("base:payment:query")
    @GetMapping("/getPayment/{id}")
    public R<PaymentVo> getPayment(@NotNull(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long id) {
        return R.ok(paymentService.get(id));
    }

    /**
     * 新增支付配置
     */
    @SaCheckPermission("base:payment:add")
    @Log(title = "支付配置", operType = DictOperType.INSERT)
    @RepeatSubmit()
    @PostMapping("/addPayment")
    public R<Long> addPayment(@Validated(AddGroup.class) @RequestBody PaymentBo bo) {
        Long id = paymentService.add(bo);
        reloadPaymentConfig();
        return R.ok(id);
    }

    /**
     * 修改支付配置
     */
    @SaCheckPermission("base:payment:update")
    @Log(title = "支付配置", operType = DictOperType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/updatePayment")
    public R<Void> updatePayment(@Validated(EditGroup.class) @RequestBody PaymentBo bo) {
        boolean result = paymentService.update(bo) > 0;
        if (result) {
            reloadPaymentConfig();
        }
        return R.status(result);
    }

    /**
     * 删除支付配置
     *
     * @param ids 主键串
     */
    @SaCheckPermission("base:payment:delete")
    @Log(title = "支付配置", operType = DictOperType.DELETE)
    @DeleteMapping("/deletePayments/{ids}")
    public R<Void> deletePayments(@NotEmpty(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long[] ids) {
        boolean result = paymentService.batchDelete(List.of(ids)) > 0;
        if (result) {
            reloadPaymentConfig();
        }
        return R.status(result);
    }

    /**
     * 导出支付配置列表
     */
    @SaCheckPermission("base:payment:export")
    @Log(title = "支付配置", operType = DictOperType.EXPORT)
    @PostMapping("/exportPayments")
    public void exportPayments(PaymentBo bo, PageQuery pageQuery, HttpServletResponse response) {
        PageResult<PaymentVo> pageResult = paymentService.page(bo, pageQuery);
        ExcelUtil.exportExcel(pageResult.getRecords(), "支付配置", PaymentVo.class, response);
    }

    /**
     * 获取支付配置导入模板
     */
    @PostMapping("/templatePayments")
    public void templatePayments(HttpServletResponse response) {
        ExcelUtil.exportExcel(new ArrayList<>(), "支付配置模板", PaymentVo.class, response);
    }

    /**
     * 导入支付配置
     *
     * @param file 导入文件
     */
    @Log(title = "支付配置", operType = DictOperType.IMPORT)
    @SaCheckPermission("base:payment:import")
    @PostMapping(value = "/importPayments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<Void> importPayments(MultipartFile file) throws Exception {
        ExcelResult<PaymentVo> excelResult = ExcelUtil.importExcel(file.getInputStream(), PaymentVo.class, true);
        List<PaymentBo> boList = MapstructUtils.convert(excelResult.getList(), PaymentBo.class);
        paymentService.batchSave(boList);
        reloadPaymentConfig();
        return R.ok(excelResult.getAnalysis());
    }

    /**
     * 获取支付配置选项列表
     * 用于下拉选择、关联查询等场景
     */
    @SaCheckPermission("base:payment:query")
    @GetMapping("/optionPayments")
    public R<List<DictItemVo>> optionPayments() {
        List<PaymentVo> list = paymentService.listForOption();
        List<DictItemVo> options = StreamUtils.toList(list, item ->
            DictItemVo.of(item.getMchName(), String.valueOf(item.getId()), "success")
        );
        return R.ok(options);
    }

    /**
     * 刷新当前租户的支付配置，并广播到集群其他节点
     */
    private void reloadPaymentConfig() {
        String tenantId = TenantHelper.getTenantId();

        // 刷新当前节点的支付配置
        SpringUtils.getBean(PayConfigInitializer.class).initByTenant(tenantId);

        // 广播到其他集群节点
        PlatformConfigSyncListener.publishRefreshPay(tenantId);
    }
}
