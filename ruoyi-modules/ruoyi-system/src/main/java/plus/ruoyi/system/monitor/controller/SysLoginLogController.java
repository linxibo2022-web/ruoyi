package plus.ruoyi.system.monitor.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.lock.annotation.Lock4j;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

import plus.ruoyi.common.core.constant.CacheConstants;
import plus.ruoyi.common.core.constant.I18nKeys;
import plus.ruoyi.common.core.dict.DictOperType;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.excel.utils.ExcelUtil;
import plus.ruoyi.common.log.annotation.Log;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.redis.utils.RedisUtils;
import plus.ruoyi.system.monitor.domain.bo.SysLoginLogBo;
import plus.ruoyi.system.monitor.domain.vo.SysLoginLogVo;
import plus.ruoyi.system.monitor.service.ISysLoginLogService;

/**
 * 登录日志
 *
 * @author Lion Li
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/monitor/loginLog")
public class SysLoginLogController {

    private final ISysLoginLogService loginLogService;

    /**
     * 查询登录日志列表
     */
    @SaCheckPermission("monitor:loginLog:query")
    @GetMapping("/pageLoginLogs")
    public R<PageResult<SysLoginLogVo>> pageLoginLogs(SysLoginLogBo loginLog, PageQuery pageQuery) {
        return R.ok(loginLogService.page(loginLog, pageQuery));
    }

    /**
     * 批量删除登录日志
     *
     * @param infoIds 日志ids
     */
    @SaCheckPermission("monitor:loginLog:delete")
    @Log(title = "登录日志", operType = DictOperType.DELETE)
    @DeleteMapping("/deleteLoginLogs/{infoIds}")
    public R<Void> deleteLoginLogs(@NotEmpty(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long[] infoIds) {
        return R.status(loginLogService.batchDelete(List.of(infoIds)));
    }

    /**
     * 清理登录日志
     */
    @SaCheckPermission("monitor:loginLog:delete")
    @Log(title = "登录日志", operType = DictOperType.CLEAN)
    @Lock4j
    @DeleteMapping("/clearLoginLogs")
    public R<Void> clearLoginLogs() {
        loginLogService.clearLoginLogs();
        return R.ok();
    }

    /**
     * 解锁用户登录状态
     *
     * @param userName 用户名
     */
    @SaCheckPermission("monitor:loginLog:unlock")
    @Log(title = "账户解锁", operType = DictOperType.OTHER)
    @GetMapping("/unlockLoginLog/{userName}")
    public R<Void> unlockLoginLog(@NotBlank(message = "用户名不能为空") @PathVariable("userName") String userName) {
        String errorKey = CacheConstants.PWD_ERR_CNT_KEY + userName;
        if (RedisUtils.hasKey(errorKey)) {
            RedisUtils.deleteObject(errorKey);
        }
        return R.ok();
    }


    /**
     * 导出登录日志列表
     */
    @Log(title = "登录日志", operType = DictOperType.EXPORT)
    @SaCheckPermission("monitor:loginLog:export")
    @PostMapping("/exportLoginLogs")
    public void exportLoginLogs(SysLoginLogBo loginLogBo, PageQuery pageQuery, HttpServletResponse response) {
        PageResult<SysLoginLogVo> pageResult = loginLogService.page(loginLogBo, pageQuery);
        ExcelUtil.exportExcel(pageResult.getRecords(), "登录日志", SysLoginLogVo.class, response);
    }

}
