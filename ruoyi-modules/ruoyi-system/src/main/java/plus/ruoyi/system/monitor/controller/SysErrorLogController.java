package plus.ruoyi.system.monitor.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import plus.ruoyi.common.core.constant.I18nKeys;
import plus.ruoyi.common.core.dict.DictOperType;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.core.validate.EditGroup;
import plus.ruoyi.common.excel.utils.ExcelUtil;
import plus.ruoyi.common.idempotent.annotation.RepeatSubmit;
import plus.ruoyi.common.log.annotation.Log;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.system.monitor.domain.bo.SysErrorLogBo;
import plus.ruoyi.system.monitor.domain.vo.SysErrorLogVo;
import plus.ruoyi.system.monitor.service.ISysErrorLogService;

import java.util.List;

/**
 * 错误日志管理
 *
 * @author 抓蛙师
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/monitor/errorLog")
public class SysErrorLogController {

    private final ISysErrorLogService errorLogService;

    /**
     * 查询错误日志列表
     */
    @SaCheckPermission("monitor:errorLog:query")
    @GetMapping("/pageErrorLogs")
    public R<PageResult<SysErrorLogVo>> pageErrorLogs(SysErrorLogBo bo, PageQuery pageQuery) {
        return R.ok(errorLogService.page(bo, pageQuery));
    }

    /**
     * 获取错误日志详细信息
     */
    @SaCheckPermission("monitor:errorLog:query")
    @GetMapping("/getErrorLog/{id}")
    public R<SysErrorLogVo> getErrorLog(@NotNull(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long id) {
        return R.ok(errorLogService.get(id));
    }

    /**
     * 更新错误日志处理状态
     */
    @SaCheckPermission("monitor:errorLog:update")
    @Log(title = "错误日志管理", operType = DictOperType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/updateHandleStatus")
    public R<Void> updateHandleStatus(@Validated(EditGroup.class) @RequestBody SysErrorLogBo bo) {
        return R.status(errorLogService.updateHandleStatus(bo));
    }

    /**
     * 按ID集合批量更新处理状态
     */
    @SaCheckPermission("monitor:errorLog:update")
    @Log(title = "错误日志管理", operType = DictOperType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/updateHandleStatusBatch")
    public R<Void> updateHandleStatusBatch(@Validated @RequestBody SysErrorLogBo bo) {
        errorLogService.updateHandleStatusBatch(bo);
        return R.ok();
    }

    /**
     * 按相同错误批量更新处理状态
     */
    @SaCheckPermission("monitor:errorLog:update")
    @Log(title = "错误日志管理", operType = DictOperType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/updateSameErrorLogs")
    public R<Void> updateSameErrorLogs(@Validated(EditGroup.class) @RequestBody SysErrorLogBo bo) {
        errorLogService.updateHandleStatusBySameError(bo);
        return R.ok();
    }

    /**
     * 按相同接口批量更新处理状态
     */
    @SaCheckPermission("monitor:errorLog:update")
    @Log(title = "错误日志管理", operType = DictOperType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/updateSameRequestLogs")
    public R<Void> updateSameRequestLogs(@Validated(EditGroup.class) @RequestBody SysErrorLogBo bo) {
        errorLogService.updateHandleStatusBySameRequest(bo);
        return R.ok();
    }

    /**
     * 删除错误日志
     */
    @SaCheckPermission("monitor:errorLog:delete")
    @Log(title = "错误日志管理", operType = DictOperType.DELETE)
    @DeleteMapping("/deleteErrorLogs/{ids}")
    public R<Void> deleteErrorLogs(@NotEmpty(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long[] ids) {
        return R.status(errorLogService.batchDelete(List.of(ids)));
    }

    /**
     * 清空错误日志
     */
    @SaCheckPermission("monitor:errorLog:delete")
    @Log(title = "错误日志管理", operType = DictOperType.DELETE)
    @DeleteMapping("/clearErrorLogs")
    public R<Void> clearErrorLogs() {
        errorLogService.clearErrorLogs();
        return R.ok();
    }

    /**
     * 导出错误日志列表
     */
    @SaCheckPermission("monitor:errorLog:export")
    @Log(title = "错误日志管理", operType = DictOperType.EXPORT)
    @PostMapping("/exportErrorLogs")
    public void exportErrorLogs(SysErrorLogBo bo, PageQuery pageQuery, HttpServletResponse response) {
        PageResult<SysErrorLogVo> pageResult = errorLogService.page(bo, pageQuery);
        ExcelUtil.exportExcel(pageResult.getRecords(), "错误日志数据", SysErrorLogVo.class, response);
    }
}
