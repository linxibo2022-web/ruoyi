package plus.ruoyi.system.oss.controller;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.core.lang.tree.Tree;
import lombok.RequiredArgsConstructor;

import plus.ruoyi.common.core.constant.I18nKeys;
import plus.ruoyi.common.core.dict.DictOperType;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.core.utils.MapstructUtils;
import plus.ruoyi.common.core.validate.AddGroup;
import plus.ruoyi.common.core.validate.EditGroup;
import plus.ruoyi.common.excel.core.ExcelResult;
import plus.ruoyi.common.excel.utils.ExcelUtil;
import plus.ruoyi.common.idempotent.annotation.RepeatSubmit;
import plus.ruoyi.common.log.annotation.Log;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;

import plus.ruoyi.system.oss.domain.SysOssDirectory;
import plus.ruoyi.system.oss.domain.bo.SysOssDirectoryBo;
import plus.ruoyi.system.oss.domain.vo.SysOssDirectoryVo;
import plus.ruoyi.system.oss.service.ISysOssDirectoryService;

/**
 * OSS目录
 *
 * @author 抓蛙师
 * @date 2025-04-14
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/resource/ossDirectory")
public class SysOssDirectoryController {

    // 业务服务
    private final ISysOssDirectoryService ossDirectoryService;

    /**
     * 查询OSS目录列表
     */
    @SaCheckPermission("system:ossDirectory:query")
    @GetMapping("/pageOssDirectorys")
    public R<PageResult<SysOssDirectoryVo>> pageOssDirectorys(SysOssDirectoryBo bo, PageQuery pageQuery) {
        return R.ok(ossDirectoryService.page(bo, pageQuery));
    }

    /**
     * 获取OSS目录详细信息
     *
     * @param directoryId 主键
     */
    @SaCheckPermission("system:ossDirectory:query")
    @GetMapping("/getOssDirectory/{directoryId}")
    public R<SysOssDirectoryVo> getOssDirectory(@NotNull(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long directoryId) {
        return R.ok(ossDirectoryService.get(directoryId));
    }

    /**
     * 新增OSS目录
     */
    @SaCheckPermission("system:ossDirectory:add")
    @Log(title = "OSS目录", operType = DictOperType.INSERT)
    @RepeatSubmit()
    @PostMapping("/addOssDirectory")
    public R<Long> addOssDirectory(@Validated(AddGroup.class) @RequestBody SysOssDirectoryBo bo) {
        return R.ok(ossDirectoryService.add(bo));
    }

    /**
     * 修改OSS目录
     */
    @SaCheckPermission("system:ossDirectory:update")
    @Log(title = "OSS目录", operType = DictOperType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/updateOssDirectory")
    public R<Void> updateOssDirectory(@Validated(EditGroup.class) @RequestBody SysOssDirectoryBo bo) {
        return R.status(ossDirectoryService.update(bo));
    }

    /**
     * 移动OSS文件到指定目录
     *
     * @param ossIds      OSS文件ID字符串，多个以逗号分隔
     * @param directoryId 目标目录ID
     */
    @SaCheckPermission("system:ossDirectory:update")
    @Log(title = "OSS目录", operType = DictOperType.UPDATE)
    @PutMapping("/moveOssDirectory/{ossIds}")
    public R<Void> moveOssDirectory(@NotEmpty(message = "文件ID不能为空") @PathVariable Long[] ossIds, Long directoryId) {
        return R.status(ossDirectoryService.moveOss(
            (directoryId == 9999999999999999L || directoryId == 10000000000000000L) ? null :
                directoryId, List.of(ossIds)));
    }

    /**
     * 删除OSS目录
     *
     * @param directoryIds 主键串
     */
    @SaCheckPermission("system:ossDirectory:delete")
    @Log(title = "OSS目录", operType = DictOperType.DELETE)
    @DeleteMapping("/deleteOssDirectorys/{directoryIds}")
    public R<Void> deleteOssDirectorys(@NotEmpty(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long[] directoryIds) {
        return R.status(ossDirectoryService.batchDelete(List.of(directoryIds)));
    }

    /**
     * 导出OSS目录列表
     */
    @SaCheckPermission("system:ossDirectory:export")
    @Log(title = "OSS目录", operType = DictOperType.EXPORT)
    @PostMapping("/exportOssDirectorys")
    public void exportOssDirectorys(SysOssDirectoryBo bo, PageQuery pageQuery, HttpServletResponse response) {
        PageResult<SysOssDirectoryVo> pageResult = ossDirectoryService.page(bo, pageQuery);
        ExcelUtil.exportExcel(pageResult.getRecords(), "OSS目录", SysOssDirectoryVo.class, response);
    }

    /**
     * 获取OSS目录导入模板
     */
    @PostMapping("/templateOssDirectorys")
    public void templateOssDirectorys(HttpServletResponse response) {
        ExcelUtil.exportExcel(new ArrayList<>(), "OSS目录模板", SysOssDirectoryVo.class, response);
    }

    /**
     * 导入OSS目录
     *
     * @param file 导入文件
     */
    @Log(title = "OSS目录", operType = DictOperType.IMPORT)
    @SaCheckPermission("system:ossDirectory:import")
    @PostMapping(value = "/importOssDirectorys", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<Void> importOssDirectorys(MultipartFile file) throws Exception {
        ExcelResult<SysOssDirectoryVo> excelResult = ExcelUtil.importExcel(file.getInputStream(), SysOssDirectoryVo.class, true);
        List<SysOssDirectoryBo> boList = MapstructUtils.convert(excelResult.getList(), SysOssDirectoryBo.class);
        ossDirectoryService.batchSave(boList);
        return R.ok(excelResult.getAnalysis());
    }

    /**
     * 查询OSS目录树列表
     */
    @SaCheckPermission(value = {"system:ossDirectory:query", "system:oss:query"}, mode = SaMode.OR)
    @GetMapping("/getOssDirectoryTreeOptions")
    public R<List<Tree<Long>>> getOssDirectoryTreeOptions(SysOssDirectoryBo bo) {
        return R.ok(ossDirectoryService.getOssDirectoryTreeOptions(bo));
    }

}
