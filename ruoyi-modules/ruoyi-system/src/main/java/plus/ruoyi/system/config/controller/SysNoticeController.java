package plus.ruoyi.system.config.controller;

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
import plus.ruoyi.common.satoken.utils.LoginHelper;

import plus.ruoyi.system.config.domain.bo.SysNoticeBo;
import plus.ruoyi.system.config.domain.vo.SysNoticeVo;
import plus.ruoyi.system.config.domain.vo.UserNoticeVo;
import plus.ruoyi.system.config.service.ISysNoticeService;

/**
 * 公告 信息操作处理
 *
 * @author Lion Li
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/notice")
public class SysNoticeController {

    // 业务服务
    private final ISysNoticeService noticeService;

    /**
     * 获取通知公告列表
     */
    @SaCheckPermission("system:notice:query")
    @GetMapping("/pageNotices")
    public R<PageResult<SysNoticeVo>> pageNotices(SysNoticeBo bo, PageQuery pageQuery) {
        return R.ok(noticeService.page(bo, pageQuery));
    }

    /**
     * 根据通知公告编号获取详细信息
     *
     * @param noticeId 公告ID
     */
    @SaCheckPermission("system:notice:query")
    @GetMapping("/getNotice/{noticeId}")
    public R<SysNoticeVo> getNotice(@NotNull(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long noticeId) {
        return R.ok(noticeService.get(noticeId));
    }

    /**
     * 新增通知公告
     */
    @SaCheckPermission("system:notice:add")
    @Log(title = "通知公告", operType = DictOperType.INSERT)
    @RepeatSubmit()
    @PostMapping("/addNotice")
    public R<Void> addNotice(@Validated @RequestBody SysNoticeBo bo) {
        noticeService.processTargetConfig(bo);
        noticeService.add(bo);
        noticeService.sendNoticeNotification(bo);
        return R.ok();
    }

    /**
     * 修改通知公告
     */
    @SaCheckPermission("system:notice:update")
    @Log(title = "通知公告", operType = DictOperType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/updateNotice")
    public R<Void> updateNotice(@Validated @RequestBody SysNoticeBo bo) {
        noticeService.processTargetConfig(bo);
        noticeService.update(bo);
        noticeService.sendNoticeNotification(bo);
        return R.ok();
    }

    /**
     * 删除通知公告
     *
     * @param noticeIds 公告ID串
     */
    @SaCheckPermission("system:notice:delete")
    @Log(title = "通知公告", operType = DictOperType.DELETE)
    @DeleteMapping("/deleteNotices/{noticeIds}")
    public R<Void> deleteNotices(@NotEmpty(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long[] noticeIds) {
        return R.status(noticeService.batchDelete(List.of(noticeIds)));
    }

    /**
     * 导出通知公告列表
     */
    @SaCheckPermission("system:notice:export")
    @Log(title = "通知公告", operType = DictOperType.EXPORT)
    @PostMapping("/exportNotices")
    public void exportNotices(SysNoticeBo bo, PageQuery pageQuery, HttpServletResponse response) {
        PageResult<SysNoticeVo> pageResult = noticeService.page(bo, pageQuery);
        ExcelUtil.exportExcel(pageResult.getRecords(), "通知公告", SysNoticeVo.class, response);
    }

    /**
     * 获取通知公告导入模板
     */
    @PostMapping("/templateNotices")
    public void templateNotices(HttpServletResponse response) {
        ExcelUtil.exportExcel(new ArrayList<>(), "通知公告模板", SysNoticeVo.class, response);
    }

    /**
     * 导入通知公告
     *
     * @param file 导入文件
     */
    @Log(title = "通知公告", operType = DictOperType.IMPORT)
    @SaCheckPermission("system:notice:import")
    @PostMapping(value = "/importNotices", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<Void> importNotices(MultipartFile file) throws Exception {
        ExcelResult<SysNoticeVo> excelResult = ExcelUtil.importExcel(file.getInputStream(), SysNoticeVo.class, true);
        List<SysNoticeBo> boList = MapstructUtils.convert(excelResult.getList(), SysNoticeBo.class);
        noticeService.batchSave(boList);
        return R.ok(excelResult.getAnalysis());
    }

    /**
     * 获取当前用户的通知公告列表（用户端）
     */
    @GetMapping("/pageUserNotices")
    public R<PageResult<UserNoticeVo>> pageUserNotices(PageQuery pageQuery) {
        return R.ok(noticeService.pageNoticesByUserId(LoginHelper.getUserId(), pageQuery));
    }

    /**
     * 获取当前用户的未读通知数量
     */
    @GetMapping("/getNoticeUnreadCount")
    public R<Long> getNoticeUnreadCount() {
        return R.ok(noticeService.getUnreadCountByUserId(LoginHelper.getUserId()));
    }

    /**
     * 获取用户公告详情（包含已读状态）
     *
     * @param noticeId 公告ID
     */
    @GetMapping("/getUserNotice/{noticeId}")
    public R<UserNoticeVo> getUserNotice(@PathVariable Long noticeId) {
        return R.ok(noticeService.getUserNoticeDetail(noticeId, LoginHelper.getUserId()));
    }

    /**
     * 标记公告为已读
     */
    @PostMapping("/markNoticeAsRead/{noticeId}")
    public R<Void> markNoticeAsRead(@PathVariable Long noticeId) {
        boolean success = noticeService.markAsRead(noticeId, LoginHelper.getUserId());
        return R.status(success);
    }

    /**
     * 标记所有公告为已读
     */
    @PostMapping("/markAllNoticesAsRead")
    public R<Void> markAllNoticesAsRead() {
        boolean success = noticeService.markAllAsRead(LoginHelper.getUserId());
        return R.status(success);
    }

}
