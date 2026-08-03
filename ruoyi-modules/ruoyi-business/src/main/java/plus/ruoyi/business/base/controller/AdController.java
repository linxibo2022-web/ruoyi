package plus.ruoyi.business.base.controller;

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
import plus.ruoyi.business.base.domain.Ad;
import plus.ruoyi.business.base.domain.vo.AdVo;
import plus.ruoyi.business.base.domain.bo.AdBo;
import plus.ruoyi.business.base.service.IAdService;
import plus.ruoyi.common.openapi.annotation.OpenApi;

/**
 * 广告配置
 *
 * @author 抓蛙师
 * @date 2025-07-17
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/base/ad")
public class AdController {

    private final IAdService adService;

    /**
     * 查询广告配置列表
     */
    @SaCheckPermission("base:ad:query")
    @GetMapping("/pageAds")
    @OpenApi(value = "查询广告配置列表")
    public R<PageResult<AdVo>> pageAds(AdBo bo, PageQuery pageQuery) {
        return R.ok(adService.page(bo, pageQuery));
    }

    /**
     * 获取广告配置详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("base:ad:query")
    @GetMapping("/getAd/{id}")
    @OpenApi(value = "获取广告配置详细信息")
    public R<AdVo> getAd(@NotNull(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long id) {
        return R.ok(adService.get(id));
    }

    /**
     * 新增广告配置
     */
    @SaCheckPermission("base:ad:add")
    @Log(title = "广告配置", operType = DictOperType.INSERT)
    @RepeatSubmit()
    @PostMapping("/addAd")
    @OpenApi(value = "添加广告配置") // 开启此注解可以在OpenApiTest中测试开放平台
    public R<Long> addAd(@Validated(AddGroup.class) @RequestBody AdBo bo) {
        return R.ok(adService.add(bo));
    }

    /**
     * 修改广告配置
     */
    @SaCheckPermission("base:ad:update")
    @Log(title = "广告配置", operType = DictOperType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/updateAd")
    public R<Void> updateAd(@Validated(EditGroup.class) @RequestBody AdBo bo) {
        return R.status(adService.update(bo));
    }

    /**
     * 删除广告配置
     *
     * @param ids 主键串
     */
    @SaCheckPermission("base:ad:delete")
    @Log(title = "广告配置", operType = DictOperType.DELETE)
    @DeleteMapping("/deleteAds/{ids}")
    public R<Void> deleteAds(@NotEmpty(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long[] ids) {
        return R.status(adService.batchDelete(List.of(ids)));
    }

    /**
     * 导出广告配置列表
     */
    @SaCheckPermission("base:ad:export")
    @Log(title = "广告配置", operType = DictOperType.EXPORT)
    @PostMapping("/exportAds")
    public void exportAds(AdBo bo, PageQuery pageQuery, HttpServletResponse response) {
        PageResult<AdVo> pageResult = adService.page(bo, pageQuery);
        ExcelUtil.exportExcel(pageResult.getRecords(), "广告配置", AdVo.class, response);
    }

    /**
     * 获取广告配置导入模板
     */
    @PostMapping("/templateAds")
    public void templateAds(HttpServletResponse response) {
        ExcelUtil.exportExcel(new ArrayList<>(), "广告配置模板", AdVo.class, response);
    }

    /**
     * 导入广告配置
     *
     * @param file 导入文件
     */
    @Log(title = "广告配置", operType = DictOperType.IMPORT)
    @SaCheckPermission("base:ad:import")
    @PostMapping(value = "/importAds", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<Void> importAds(MultipartFile file) throws Exception {
        ExcelResult<AdVo> excelResult = ExcelUtil.importExcel(file.getInputStream(), AdVo.class, true);
        List<AdBo> boList = MapstructUtils.convert(excelResult.getList(), AdBo.class);
        adService.batchSave(boList);
        return R.ok(excelResult.getAnalysis());
    }
}
