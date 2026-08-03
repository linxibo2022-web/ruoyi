package plus.ruoyi.system.monitor.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

import plus.ruoyi.common.core.constant.I18nKeys;
import plus.ruoyi.common.core.dict.DictOperType;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.excel.utils.ExcelUtil;
import plus.ruoyi.common.log.annotation.Log;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.system.monitor.domain.bo.SysOperLogBo;
import plus.ruoyi.system.monitor.domain.vo.SysOperLogVo;
import plus.ruoyi.system.monitor.service.ISysOperLogService;

/**
 * 操作日志
 *
 * @author Lion Li
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/monitor/operLog")
public class SysOperlogController {

    private final ISysOperLogService operLogService;

    /**
     * 获取操作日志列表
     */
    @SaCheckPermission("monitor:operLog:query")
    @GetMapping("/pageOperLogs")
    public R<PageResult<SysOperLogVo>> pageOperLogs(SysOperLogBo operLog, PageQuery pageQuery) {
        return R.ok(operLogService.page(operLog, pageQuery));
    }

    /**
     * 批量删除操作日志
     *
     * @param operIds 日志ids
     */
    @Log(title = "操作日志", operType = DictOperType.DELETE)
    @SaCheckPermission("monitor:operLog:delete")
    @DeleteMapping("/deleteOperLogs/{operIds}")
    public R<Void> deleteOperLogs(@NotNull(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long[] operIds) {
        return R.status(operLogService.batchDelete(List.of(operIds)));
    }

    /**
     * 清理操作日志
     */
    @Log(title = "操作日志", operType = DictOperType.CLEAN)
    @SaCheckPermission("monitor:operLog:delete")
    @DeleteMapping("/clearOperLogs")
    public R<Void> clearOperLogs() {
        operLogService.clearOperLogs();
        return R.ok();
    }

    /**
     * 导出操作日志列表
     */
    @Log(title = "操作日志", operType = DictOperType.EXPORT)
    @SaCheckPermission("monitor:operLog:export")
    @PostMapping("/exportOperLogs")
    public void exportOperLogs(SysOperLogBo bo, PageQuery pageQuery, HttpServletResponse response) {
        PageResult<SysOperLogVo> pageResult = operLogService.page(bo, pageQuery);
        ExcelUtil.exportExcel(pageResult.getRecords(), "操作日志", SysOperLogVo.class, response);
    }
}
