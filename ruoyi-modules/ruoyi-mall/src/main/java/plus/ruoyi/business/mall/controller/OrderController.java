package plus.ruoyi.business.mall.controller;

import java.util.List;
import java.util.ArrayList;

import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import plus.ruoyi.common.core.constant.I18nKeys;
import plus.ruoyi.common.core.utils.MapstructUtils;
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
import plus.ruoyi.business.mall.domain.vo.OrderVo;
import plus.ruoyi.business.mall.domain.bo.OrderBo;
import plus.ruoyi.business.mall.service.IOrderService;

/**
 * 订单
 *
 * @author 抓蛙师
 * @date 2025-07-17
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/mall/order")
public class OrderController {

    private final IOrderService orderService;

    /**
     * 查询订单列表
     */
    @SaCheckPermission("mall:order:query")
    @GetMapping("/pageOrders")
    public R<PageResult<OrderVo>> pageOrders(OrderBo bo, PageQuery pageQuery) {
        return R.ok(orderService.page(bo, pageQuery));
    }

    /**
     * 获取订单详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("mall:order:query")
    @GetMapping("/getOrder/{id}")
    public R<OrderVo> getOrder(@NotNull(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long id) {
        return R.ok(orderService.get(id));
    }

    /**
     * 新增订单
     */
    @SaCheckPermission("mall:order:add")
    @Log(title = "订单", operType = DictOperType.INSERT)
    @RepeatSubmit()
    @PostMapping("/addOrder")
    public R<Long> addOrder(@Validated(AddGroup.class) @RequestBody OrderBo bo) {
        return R.ok(orderService.add(bo));
    }

    /**
     * 修改订单
     */
    @SaCheckPermission("mall:order:update")
    @Log(title = "订单", operType = DictOperType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/updateOrder")
    public R<Void> updateOrder(@Validated(EditGroup.class) @RequestBody OrderBo bo) {
        return R.status(orderService.update(bo));
    }

    /**
     * 删除订单
     *
     * @param ids 主键串
     */
    @SaCheckPermission("mall:order:delete")
    @Log(title = "订单", operType = DictOperType.DELETE)
    @DeleteMapping("/deleteOrders/{ids}")
    public R<Void> deleteOrders(@NotEmpty(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long[] ids) {
        return R.status(orderService.batchDelete(List.of(ids)));
    }

    /**
     * 订单发货
     *
     * @param bo 发货信息（包含订单ID和物流信息）
     */
    @SaCheckPermission("mall:order:update")
    @Log(title = "订单发货", operType = DictOperType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/deliverOrder")
    public R<Void> deliverOrder(@Validated @RequestBody OrderBo bo) {
        return R.status(orderService.deliverOrder(bo.getId(), bo.getShippingInfo()));
    }

    /**
     * 导出订单列表
     */
    @SaCheckPermission("mall:order:export")
    @Log(title = "订单", operType = DictOperType.EXPORT)
    @PostMapping("/exportOrders")
    public void exportOrders(OrderBo bo, PageQuery pageQuery, HttpServletResponse response) {
        PageResult<OrderVo> pageResult = orderService.page(bo, pageQuery);
        ExcelUtil.exportExcel(pageResult.getRecords(), "订单", OrderVo.class, response);
    }

    /**
     * 获取订单导入模板
     */
    @PostMapping("/templateOrders")
    public void templateOrders(HttpServletResponse response) {
        ExcelUtil.exportExcel(new ArrayList<>(), "订单模板", OrderVo.class, response);
    }

    /**
     * 导入订单
     *
     * @param file 导入文件
     */
    @Log(title = "订单", operType = DictOperType.IMPORT)
    @SaCheckPermission("mall:order:import")
    @PostMapping(value = "/importOrders", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<Void> importOrders(MultipartFile file) throws Exception {
        ExcelResult<OrderVo> excelResult = ExcelUtil.importExcel(file.getInputStream(), OrderVo.class, true);
        List<OrderBo> boList = MapstructUtils.convert(excelResult.getList(), OrderBo.class);
        orderService.batchSave(boList);
        return R.ok(excelResult.getAnalysis());
    }
}
