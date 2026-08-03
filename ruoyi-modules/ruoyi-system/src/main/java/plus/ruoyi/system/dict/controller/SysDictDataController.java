package plus.ruoyi.system.dict.controller;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;

import plus.ruoyi.common.core.constant.I18nKeys;
import plus.ruoyi.common.core.dict.DictEnableStatus;
import plus.ruoyi.common.core.dict.DictOperType;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.core.utils.MapstructUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.excel.core.ExcelResult;
import plus.ruoyi.common.excel.utils.ExcelUtil;
import plus.ruoyi.common.idempotent.annotation.RepeatSubmit;
import plus.ruoyi.common.log.annotation.Log;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;

import plus.ruoyi.system.dict.domain.SysDictData;
import plus.ruoyi.system.dict.domain.bo.SysDictDataBo;
import plus.ruoyi.system.dict.domain.vo.SysDictDataVo;
import plus.ruoyi.system.dict.domain.vo.SysDictTypeVo;
import plus.ruoyi.system.dict.service.ISysDictDataService;
import plus.ruoyi.system.dict.service.ISysDictTypeService;

/**
 * 数据字典信息
 *
 * @author Lion Li
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/dictData")
public class SysDictDataController {

    // 业务服务
    private final ISysDictDataService dictDataService;
    private final ISysDictTypeService dictTypeService;

    /**
     * 查询字典数据列表
     */
    @SaCheckPermission("system:dict:query")
    @GetMapping("/pageDictDatas")
    public R<PageResult<SysDictDataVo>> pageDictDatas(SysDictDataBo dictData, PageQuery pageQuery) {
        return R.ok(dictDataService.page(dictData, pageQuery));
    }


    /**
     * 获取字典数据详细信息
     *
     * @param dictDataId 主键
     */
    @SaCheckPermission("system:dict:query")
    @GetMapping("/getDictData/{dictDataId}")
    public R<SysDictDataVo> getDictData(@NotNull(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long dictDataId) {
        return R.ok(dictDataService.get(dictDataId));
    }

    /**
     * 根据字典类型查询字典数据信息
     *
     * @param dictType 字典类型
     */
    @GetMapping(value = "/listDictDatasByDictType/{dictType}")
    public R<List<SysDictDataVo>> listDictDatasByDictType(@NotBlank(message = "字典类型不为空") @PathVariable String dictType) {
        SysDictTypeVo vo = dictTypeService.getDictTypeByType(dictType);
        if (ObjectUtil.isNull(vo) || DictEnableStatus.isDisabled(vo.getStatus())) {
            return R.ok(List.of());
        }
        List<SysDictDataVo> data = dictTypeService.listDictDataByType(dictType);
        if (ObjectUtil.isNull(data)) {
            data = new ArrayList<>();
        }
        return R.ok(data);
    }

    /**
     * 新增字典类型
     */
    @SaCheckPermission("system:dict:add")
    @Log(title = "字典数据", operType = DictOperType.INSERT)
    @RepeatSubmit()
    @PostMapping("/addDictData")
    public R<Void> addDictData(@Validated @RequestBody SysDictDataBo dict) {
        if (!dictDataService.checkDictDataUnique(dict)) {
            return R.fail("新增字典数据'" + dict.getDictValue() + "'失败，字典键值已存在");
        }
        dictDataService.insertDictData(dict);
        return R.ok();
    }

    /**
     * 修改保存字典类型
     */
    @SaCheckPermission("system:dict:update")
    @Log(title = "字典数据", operType = DictOperType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/updateDictData")
    public R<Void> updateDictData(@Validated @RequestBody SysDictDataBo dict) {
        if (!dictDataService.checkDictDataUnique(dict)) {
            return R.fail("修改字典数据'" + dict.getDictValue() + "'失败，字典键值已存在");
        }
        dictDataService.updateDictData(dict);
        return R.ok();
    }

    /**
     * 删除字典类型
     *
     * @param dictDataIds 字典code串
     */
    @SaCheckPermission("system:dict:delete")
    @Log(title = "字典类型", operType = DictOperType.DELETE)
    @DeleteMapping("/deleteDictDatas/{dictDataIds}")
    public R<Void> deleteDictDatas(@NotEmpty(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long[] dictDataIds) {
        dictDataService.deleteDictDataByIds(List.of(dictDataIds));
        return R.ok();
    }

    /**
     * 导出字典数据列表
     */
    @SaCheckPermission("system:dict:export")
    @Log(title = "字典数据", operType = DictOperType.EXPORT)
    @PostMapping("/exportDictDatas")
    public void exportDictDatas(SysDictDataBo bo, PageQuery pageQuery, HttpServletResponse response) {
        PageResult<SysDictDataVo> pageResult = dictDataService.page(bo, pageQuery);
        ExcelUtil.exportExcel(pageResult.getRecords(), "字典数据", SysDictDataVo.class, response);
    }

    /**
     * 获取字典数据导入模板
     */
    @PostMapping("/templateDictDatas")
    public void templateDictDatas(HttpServletResponse response) {
        ExcelUtil.exportExcel(new ArrayList<>(), "字典数据模板", SysDictDataVo.class, response);
    }

    /**
     * 导入字典数据
     *
     * @param file 导入文件
     * @param dictType 字典类型
     */
    @Log(title = "字典数据", operType = DictOperType.IMPORT)
    @SaCheckPermission("system:dict:add")
    @PostMapping(value = "/importDictDatas", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<Void> importDictDatas(MultipartFile file, String dictType) throws Exception {
        ExcelResult<SysDictDataVo> excelResult = ExcelUtil.importExcel(file.getInputStream(), SysDictDataVo.class, true);
        List<SysDictDataVo> dictDataVoList = excelResult.getList();
        // 设置字典类型
        if (StringUtils.isNotBlank(dictType)) {
            dictDataVoList.forEach(vo -> vo.setDictType(dictType));
        }
        // 转换为Bo再保存
        List<SysDictDataBo> boList = MapstructUtils.convert(dictDataVoList, SysDictDataBo.class);
        dictDataService.batchSave(boList);
        return R.ok(excelResult.getAnalysis());
    }

}
