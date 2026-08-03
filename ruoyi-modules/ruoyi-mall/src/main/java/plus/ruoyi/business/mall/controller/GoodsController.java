package plus.ruoyi.business.mall.controller;

import java.util.List;
import java.util.ArrayList;

import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import plus.ruoyi.business.mall.domain.bo.GoodsBo;
import plus.ruoyi.business.mall.domain.vo.GoodsVo;
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
import plus.ruoyi.business.mall.domain.Goods;
import plus.ruoyi.business.mall.service.IGoodsService;

/**
 * 商品
 *
 * @author 抓蛙师
 * @date 2025-07-17
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/mall/goods")
public class GoodsController {

    private final IGoodsService goodsService;

    /**
     * 查询商品列表
     */
    @SaCheckPermission("mall:goods:query")
    @GetMapping("/pageGoods")
    public R<PageResult<GoodsVo>> pageGoods(GoodsBo bo, PageQuery pageQuery) {
        return R.ok(goodsService.page(bo, pageQuery));
    }

    /**
     * 获取商品详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("mall:goods:query")
    @GetMapping("/getGoods/{id}")
    public R<GoodsVo> getGoods(@NotNull(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long id) {
        return R.ok(goodsService.get(id));
    }

    /**
     * 新增商品
     */
    @SaCheckPermission("mall:goods:add")
    @Log(title = "商品", operType = DictOperType.INSERT)
    @RepeatSubmit()
    @PostMapping("/addGoods")
    public R<Long> addGoods(@Validated(AddGroup.class) @RequestBody GoodsBo bo) {
        return R.ok(goodsService.add(bo));
    }

    /**
     * 修改商品
     */
    @SaCheckPermission("mall:goods:update")
    @Log(title = "商品", operType = DictOperType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/updateGoods")
    public R<Void> updateGoods(@Validated(EditGroup.class) @RequestBody GoodsBo bo) {
        return R.status(goodsService.update(bo));
    }

    /**
     * 删除商品
     *
     * @param ids 主键串
     */
    @SaCheckPermission("mall:goods:delete")
    @Log(title = "商品", operType = DictOperType.DELETE)
    @DeleteMapping("/deleteGoods/{ids}")
    public R<Void> deleteGoods(@NotEmpty(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long[] ids) {
        return R.status(goodsService.batchDelete(List.of(ids)));
    }

    /**
     * 导出商品列表
     */
    @SaCheckPermission("mall:goods:export")
    @Log(title = "商品", operType = DictOperType.EXPORT)
    @PostMapping("/exportGoods")
    public void exportGoods(GoodsBo bo, PageQuery pageQuery, HttpServletResponse response) {
        PageResult<GoodsVo> pageResult = goodsService.page(bo, pageQuery);
        ExcelUtil.exportExcel(pageResult.getRecords(), "商品", GoodsVo.class, response);
    }

    /**
     * 获取商品导入模板
     */
    @PostMapping("/templateGoods")
    public void templateGoods(HttpServletResponse response) {
        ExcelUtil.exportExcel(new ArrayList<>(), "商品模板", GoodsVo.class, response);
    }

    /**
     * 导入商品
     *
     * @param file 导入文件
     */
    @Log(title = "商品", operType = DictOperType.IMPORT)
    @SaCheckPermission("mall:goods:import")
    @PostMapping(value = "/importGoods", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<Void> importGoods(MultipartFile file) throws Exception {
        ExcelResult<GoodsVo> excelResult = ExcelUtil.importExcel(file.getInputStream(), GoodsVo.class, true);
        List<GoodsBo> boList = MapstructUtils.convert(excelResult.getList(), GoodsBo.class);
        goodsService.batchSave(boList);
        return R.ok(excelResult.getAnalysis());
    }

    /**
     * 同步商品主表数据(根据SKU重新计算价格、库存、销量)
     * 适用场景: SKU变更后需要更新商品主表的统计数据
     *
     * @param goodsId 商品ID
     */
    @SaCheckPermission("mall:goods:update")
    @PostMapping("/syncGoodsDataFromSku/{goodsId}")
    public R<Void> syncGoodsDataFromSku(@NotNull(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long goodsId) {
        goodsService.syncGoodsDataFromSku(goodsId);
        return R.ok();
    }
}
