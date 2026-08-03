package plus.ruoyi.business.mall.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import plus.ruoyi.common.core.constant.I18nKeys;
import plus.ruoyi.common.core.utils.MapstructUtils;
import plus.ruoyi.common.excel.core.ExcelResult;
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
import plus.ruoyi.business.mall.domain.GoodsSku;
import plus.ruoyi.business.mall.domain.vo.GoodsSkuVo;
import plus.ruoyi.business.mall.domain.bo.GoodsSkuBo;
import plus.ruoyi.business.mall.service.IGoodsSkuService;
import plus.ruoyi.common.openapi.annotation.OpenApi;

/**
 * 商品SKU
 *
 * @author 抓蛙师
 * @date 2025-11-01
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/mall/goodsSku")
public class GoodsSkuController {

    private final IGoodsSkuService goodsSkuService;

    /**
     * 查询商品SKU列表
     */
    @SaCheckPermission("mall:goods:query")
    @GetMapping("/pageGoodsSkus")
    public R<PageResult<GoodsSkuVo>> pageGoodsSkus(GoodsSkuBo bo, PageQuery pageQuery) {
        return R.ok(goodsSkuService.page(bo, pageQuery));
    }

    /**
     * 根据商品ID查询SKU列表
     */
    @SaCheckPermission("mall:goods:query")
    @GetMapping("/listByGoodsId/{goodsId}")
    public R<List<GoodsSkuVo>> listByGoodsId(@PathVariable Long goodsId) {
        return R.ok(goodsSkuService.listByGoodsId(goodsId));
    }

    /**
     * 获取商品SKU详细信息
     */
    @SaCheckPermission("mall:goods:query")
    @GetMapping("/getGoodsSku/{id}")
    public R<GoodsSkuVo> getGoodsSku(@NotNull(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long id) {
        return R.ok(goodsSkuService.get(id));
    }

    /**
     * 新增商品SKU
     */
    @SaCheckPermission("mall:goods:add")
    @Log(title = "商品SKU", operType = DictOperType.INSERT)
    @RepeatSubmit()
    @PostMapping("/addGoodsSku")
    public R<Long> addGoodsSku(@Validated(AddGroup.class) @RequestBody GoodsSkuBo bo) {
        return R.ok(goodsSkuService.add(bo));
    }

    /**
     * 修改商品SKU
     */
    @SaCheckPermission("mall:goods:update")
    @Log(title = "商品SKU", operType = DictOperType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/updateGoodsSku")
    public R<Void> updateGoodsSku(@Validated(EditGroup.class) @RequestBody GoodsSkuBo bo) {
        return R.status(goodsSkuService.update(bo));
    }

    /**
     * 删除商品SKU
     */
    @SaCheckPermission("mall:goods:delete")
    @Log(title = "商品SKU", operType = DictOperType.DELETE)
    @DeleteMapping("/deleteGoodsSkus/{ids}")
    public R<Void> deleteGoodsSkus(@NotEmpty(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long[] ids) {
        return R.status(goodsSkuService.batchDelete(List.of(ids)));
    }

    /**
     * 批量保存商品SKU(先删除商品的所有SKU，再批量新增)
     */
    @SaCheckPermission("mall:goods:update")
    @Log(title = "商品SKU", operType = DictOperType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/batchSaveByGoodsId")
    public R<Void> batchSaveByGoodsId(@RequestParam Long goodsId, @RequestBody List<GoodsSkuBo> boList) {
        return R.status(goodsSkuService.batchSaveByGoodsId(goodsId, boList));
    }

    /**
     * 导出商品SKU列表
     */
    @SaCheckPermission("mall:goods:export")
    @Log(title = "商品SKU", operType = DictOperType.EXPORT)
    @PostMapping("/exportGoodsSkus")
    public void exportGoodsSkus(GoodsSkuBo bo, PageQuery pageQuery, HttpServletResponse response) {
        PageResult<GoodsSkuVo> pageResult = goodsSkuService.page(bo, pageQuery);
        ExcelUtil.exportExcel(pageResult.getRecords(), "商品SKU", GoodsSkuVo.class, response);
    }
}
