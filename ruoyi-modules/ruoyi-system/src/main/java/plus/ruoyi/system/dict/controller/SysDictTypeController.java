package plus.ruoyi.system.dict.controller;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;

import plus.ruoyi.common.core.constant.I18nKeys;
import plus.ruoyi.common.core.dict.DictOperType;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.core.utils.MapstructUtils;
import plus.ruoyi.common.excel.core.ExcelResult;
import plus.ruoyi.common.excel.utils.ExcelUtil;
import plus.ruoyi.common.idempotent.annotation.RepeatSubmit;
import plus.ruoyi.common.log.annotation.Log;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;

import plus.ruoyi.system.dict.domain.bo.SysDictTypeBo;
import plus.ruoyi.system.dict.domain.vo.SysDictTypeVo;
import plus.ruoyi.system.dict.service.ISysDictTypeService;

/**
 * 数据字典信息
 *
 * @author Lion Li
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/dictType")
public class SysDictTypeController {

    // 业务服务
    private final ISysDictTypeService dictTypeService;

    /**
     * 查询字典类型列表
     */
    @SaCheckPermission("system:dict:query")
    @GetMapping("/pageDictTypes")
    public R<PageResult<SysDictTypeVo>> pageDictTypes(SysDictTypeBo dictType, PageQuery pageQuery) {
        return R.ok(dictTypeService.page(dictType, pageQuery));
    }

    /**
     * 查询字典类型详细
     *
     * @param dictId 字典ID
     */
    @SaCheckPermission("system:dict:query")
    @GetMapping("/getDictType/{dictId}")
    public R<SysDictTypeVo> getDictType(@NotNull(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long dictId) {
        return R.ok(dictTypeService.get(dictId));
    }

    /**
     * 新增字典类型
     */
    @SaCheckPermission("system:dict:add")
    @Log(title = "字典类型", operType = DictOperType.INSERT)
    @RepeatSubmit()
    @PostMapping("/addDictType")
    public R<Long> addDictType(@Validated @RequestBody SysDictTypeBo dict) {
        if (!dictTypeService.checkDictTypeUnique(dict)) {
            return R.fail("新增字典'" + dict.getDictName() + "'失败，字典类型已存在");
        }
        dictTypeService.insertDictType(dict);
        return R.ok("新增成功", dict.getDictId());
    }

    /**
     * 修改字典类型
     */
    @SaCheckPermission("system:dict:update")
    @Log(title = "字典类型", operType = DictOperType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/updateDictType")
    public R<Void> updateDictType(@Validated @RequestBody SysDictTypeBo dict) {
        if (!dictTypeService.checkDictTypeUnique(dict)) {
            return R.fail("修改字典'" + dict.getDictName() + "'失败，字典类型已存在");
        }
        dictTypeService.updateDictType(dict);
        return R.ok();
    }

    /**
     * 删除字典类型
     *
     * @param dictIds 字典ID串
     */
    @SaCheckPermission("system:dict:delete")
    @Log(title = "字典类型", operType = DictOperType.DELETE)
    @DeleteMapping("/deleteDictTypes/{dictIds}")
    public R<Void> deleteDictTypes(@NotEmpty(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long[] dictIds) {
        dictTypeService.deleteDictTypeByIds(List.of(dictIds));
        return R.ok();
    }

    /**
     * 刷新字典缓存
     */
    @SaCheckPermission("system:dict:delete")
    @Log(title = "字典类型", operType = DictOperType.CLEAN)
    @DeleteMapping("/refreshDictCache")
    public R<Void> refreshDictCache() {
        dictTypeService.resetDictCache();
        return R.ok();
    }

    /**
     * 获取字典选择框列表
     */
    @GetMapping("/getDictTypeOptions")
    public R<List<SysDictTypeVo>> getDictTypeOptions() {
        List<SysDictTypeVo> dictTypes = dictTypeService.list(new SysDictTypeBo());
        return R.ok(dictTypes);
    }


    /**
     * 导出字典类型列表
     */
    @Log(title = "字典类型", operType = DictOperType.EXPORT)
    @SaCheckPermission("system:dict:export")
    @PostMapping("/exportDictTypes")
    public void exportDictTypes(SysDictTypeBo dictType, PageQuery pageQuery, HttpServletResponse response) {
        PageResult<SysDictTypeVo> pageResult = dictTypeService.page(dictType, pageQuery);
        ExcelUtil.exportExcel(pageResult.getRecords(), "字典类型", SysDictTypeVo.class, response);
    }

    /**
     * 下载字典模板
     */
    @PostMapping("/templateDictTypes")
    public void templateDictTypes(HttpServletResponse response) {
        ExcelUtil.exportExcel(new ArrayList<>(), "字典类型模板", SysDictTypeVo.class, response);
    }

    /**
     * 导入字典类型
     */
    @Log(title = "字典类型", operType = DictOperType.IMPORT)
    @SaCheckPermission("system:dict:add")
    @PostMapping(value = "/importDictTypes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<Void> importDictTypes(MultipartFile file) throws Exception {
        ExcelResult<SysDictTypeVo> excelResult = ExcelUtil.importExcel(file.getInputStream(), SysDictTypeVo.class, true);
        // 转换为Bo再保存
        List<SysDictTypeBo> boList = MapstructUtils.convert(excelResult.getList(), SysDictTypeBo.class);
        dictTypeService.batchSave(boList);
        return R.ok(excelResult.getAnalysis());
    }

}
