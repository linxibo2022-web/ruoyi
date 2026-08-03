package plus.ruoyi.generator.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IoUtil;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import plus.ruoyi.common.core.constant.I18nKeys;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.mybatis.helper.DataBaseHelper;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.log.annotation.Log;
import plus.ruoyi.common.core.dict.DictOperType;
import plus.ruoyi.generator.config.GenConfig;
import plus.ruoyi.generator.domain.GenTable;
import plus.ruoyi.generator.domain.GenTableColumn;
import plus.ruoyi.generator.service.IGenTableService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 代码生成 操作处理
 *
 * @author Lion Li
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/tool/gen")
public class GenController {

    private final IGenTableService genTableService;

    /**
     * 查询代码生成列表
     */
    @SaCheckPermission("tool:gen:query")
    @GetMapping("/pageGens")
    public R<PageResult<GenTable>> pageGens(GenTable genTable, PageQuery pageQuery) {
        return R.ok(genTableService.page(genTable, pageQuery));
    }

    /**
     * 修改代码生成业务
     *
     * @param tableId 表ID
     */
    @SaCheckPermission("tool:gen:query")
    @GetMapping(value = "/getGen/{tableId}")
    public R<Map<String, Object>> getGen(@NotNull(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long tableId) {
        GenTable table = genTableService.getGenTableById(tableId);
        List<GenTable> tables = genTableService.listAllGenTables();
        List<GenTableColumn> list = genTableService.listGenTableColumnsByTableId(tableId);
        Map<String, Object> map = new HashMap<>(3);
        map.put("info", table);
        map.put("rows", list);
        map.put("tables", tables);
        return R.ok(map);
    }

    /**
     * 查询数据库列表
     */
    @SaCheckPermission("tool:gen:query")
    @GetMapping("/pageGenDbs")
    public R<PageResult<GenTable>> pageGenDbs(GenTable genTable, PageQuery pageQuery) {
        return R.ok(genTableService.pageDbTables(genTable, pageQuery));
    }

    /**
     * 查询数据表字段列表
     *
     * @param tableId 表ID
     */
    @SaCheckPermission("tool:gen:query")
    @GetMapping(value = "/listGenColumns/{tableId}")
    public R<PageResult<GenTableColumn>> listGenColumns(@NotNull(message = I18nKeys.Common.ID_REQUIRED) @PathVariable("tableId") Long tableId) {
        PageResult<GenTableColumn> pageResult = PageResult.of();
        List<GenTableColumn> list = genTableService.listGenTableColumnsByTableId(tableId);
        pageResult.setRecords(list);
        pageResult.setTotal(list.size());
        return R.ok(pageResult);
    }

    /**
     * 导入表结构（保存）
     *
     * @param tables 表名串
     */
    @SaCheckPermission("tool:gen:import")
    @Log(title = "代码生成", operType = DictOperType.IMPORT)
    @PostMapping("/importGens")
    public R<Void> importGens(String tables, String dataName) {
        String[] tableNames = Convert.toStrArray(tables);
        // 查询表信息
        List<GenTable> tableList = genTableService.listDbTablesByNames(tableNames, dataName);
        genTableService.importGenTables(tableList, dataName);
        return R.ok("导入成功");
    }

    /**
     * 修改保存代码生成业务
     */
    @SaCheckPermission("tool:gen:update")
    @Log(title = "代码生成", operType = DictOperType.UPDATE)
    @PutMapping("/updateGen")
    public R<Void> updateGen(@Validated @RequestBody GenTable genTable) {
        genTableService.validateEdit(genTable);
        genTableService.updateGenTable(genTable);
        return R.ok();
    }

    /**
     * 删除代码生成
     *
     * @param tableIds 表ID串
     */
    @SaCheckPermission("tool:gen:delete")
    @Log(title = "代码生成", operType = DictOperType.DELETE)
    @DeleteMapping("/deleteGens/{tableIds}")
    public R<Void> deleteGens(@NotEmpty(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long[] tableIds) {
        genTableService.deleteGenTablesByIds(tableIds);
        return R.ok();
    }

    /**
     * 预览代码
     *
     * @param tableId 表ID
     */
    @SaCheckPermission("tool:gen:preview")
    @GetMapping("/previewGen/{tableId}")
    public R<Map<String, String>> previewGen(@NotNull(message = I18nKeys.Common.ID_REQUIRED) @PathVariable("tableId") Long tableId) throws IOException {
        Map<String, String> dataMap = genTableService.previewCode(tableId);
        return R.ok(dataMap);
    }

    /**
     * 生成代码（下载方式）
     *
     * @param tableId 表ID
     */
    @SaCheckPermission("tool:gen:code")
    @Log(title = "代码生成", operType = DictOperType.GENCODE)
    @GetMapping("/downloadGens/{tableId}")
    public void downloadGens(HttpServletResponse response, @NotNull(message = I18nKeys.Common.ID_REQUIRED) @PathVariable("tableId") Long tableId) throws IOException {
        byte[] data = genTableService.downloadCode(tableId);
        genCode(response, data);
    }

    /**
     * 生成代码（自定义路径）
     *
     * @param tableId 表ID
     * @return 生成结果（包含菜单导入状态）
     */
    @SaCheckPermission("tool:gen:code")
    @Log(title = "代码生成", operType = DictOperType.GENCODE)
    @GetMapping("/generateCodes/{tableId}")
    public R<plus.ruoyi.generator.domain.vo.CodeGenResult> generateCodes(@NotNull(message = I18nKeys.Common.ID_REQUIRED) @PathVariable("tableId") Long tableId) {
        plus.ruoyi.generator.domain.vo.CodeGenResult result = genTableService.generatorCode(tableId);
        return R.ok(result);
    }

    /**
     * 同步数据库
     *
     * @param tableId 表ID
     */
    @SaCheckPermission("tool:gen:update")
    @Log(title = "代码生成", operType = DictOperType.UPDATE)
    @GetMapping("/syncGenDb/{tableId}")
    public R<Void> syncGenDb(@NotNull(message = I18nKeys.Common.ID_REQUIRED) @PathVariable("tableId") Long tableId) {
        genTableService.syncDatabase(tableId);
        return R.ok("同步成功");
    }

    /**
     * 批量生成代码
     *
     * @param tableIdStr 表ID串
     */
    @SaCheckPermission("tool:gen:code")
    @Log(title = "代码生成", operType = DictOperType.GENCODE)
    @GetMapping("/batchGenerateCodes")
    public void batchGenerateCodes(HttpServletResponse response, String tableIdStr) throws IOException {
        String[] tableIds = Convert.toStrArray(tableIdStr);
        byte[] data = genTableService.downloadCode(tableIds);
        genCode(response, data);
    }

    /**
     * 生成zip文件
     */
    private void genCode(HttpServletResponse response, byte[] data) throws IOException {
        response.reset();
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Expose-Headers", "Content-Disposition");
        String fileName = "ruoyi_" + DateUtil.format(new Date(), "yyyyMMddHHmmss") + ".zip";
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        response.addHeader("Content-Length", "" + data.length);
        response.setContentType("application/octet-stream; charset=UTF-8");
        IoUtil.write(response.getOutputStream(), false, data);
    }

    /**
     * 查询数据源名称列表
     */
    @SaCheckPermission("tool:gen:query")
    @GetMapping(value = "/getDataSourceNames")
    public R<List<String>> getDataSourceNames() {
        return R.ok(DataBaseHelper.getDataSourceNameList());
    }

    /**
     * 获取代码生成器配置信息
     * <p>用于前端动态显示配置的模块名称和目录</p>
     */
    @SaCheckPermission("tool:gen:query")
    @GetMapping(value = "/getGenConfig")
    public R<GenConfigVo> getGenConfig() {
        GenConfigVo config = new GenConfigVo(
            GenConfig.getDefaultGenType(),
            GenConfig.getBackendModuleName(),
            GenConfig.getFrontendRootDir(),
            GenConfig.getAuthor()
        );
        return R.ok(config);
    }

    /**
     * 代码生成配置
     *
     */
    public record GenConfigVo(
        String defaultGenType,
        String backendModuleName,
        String frontendRootDir,
        String author) {
    }
}
