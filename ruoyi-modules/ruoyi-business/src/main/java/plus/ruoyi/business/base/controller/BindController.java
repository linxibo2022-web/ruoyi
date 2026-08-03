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
import plus.ruoyi.business.base.domain.Bind;
import plus.ruoyi.business.base.domain.vo.BindVo;
import plus.ruoyi.business.base.domain.bo.BindBo;
import plus.ruoyi.business.base.service.IBindService;

/**
 * 账号绑定
 *
 * @author 抓蛙师
 * @date 2025-06-22
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/base/bind")
public class BindController {

    private final IBindService bindService;

    /**
     * 查询账号绑定列表
     */
    @SaCheckPermission("base:bind:query")
    @GetMapping("/pageBinds")
    public R<PageResult<BindVo>> pageBinds(BindBo bo, PageQuery pageQuery) {
        return R.ok(bindService.page(bo, pageQuery));
    }

    /**
     * 查询指定用户的账号绑定列表（管理员）
     *
     * @param userId 用户ID
     */
    @SaCheckPermission("system:user:query")
    @GetMapping("/listBindsByUserId/{userId}")
    public R<List<BindVo>> listBindsByUserId(
        @NotNull(message = "用户ID不能为空") @PathVariable Long userId) {
        BindBo bo = new BindBo();
        bo.setUserId(userId);
        return R.ok(bindService.list(bo));
    }

    /**
     * 获取账号绑定详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("base:bind:query")
    @GetMapping("/getBind/{id}")
    public R<BindVo> getBind(@NotNull(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long id) {
        return R.ok(bindService.get(id));
    }

    /**
     * 新增账号绑定
     */
    @SaCheckPermission("base:bind:add")
    @Log(title = "账号绑定", operType = DictOperType.INSERT)
    @RepeatSubmit()
    @PostMapping("/addBind")
    public R<Long> addBind(@Validated(AddGroup.class) @RequestBody BindBo bo) {
        return R.ok(bindService.add(bo));
    }

    /**
     * 修改账号绑定
     */
    @SaCheckPermission("base:bind:update")
    @Log(title = "账号绑定", operType = DictOperType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/updateBind")
    public R<Void> updateBind(@Validated(EditGroup.class) @RequestBody BindBo bo) {
        return R.status(bindService.update(bo));
    }

    /**
     * 删除账号绑定
     *
     * @param ids 主键串
     */
    @SaCheckPermission("base:bind:delete")
    @Log(title = "账号绑定", operType = DictOperType.DELETE)
    @DeleteMapping("/deleteBinds/{ids}")
    public R<Void> deleteBinds(@NotEmpty(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long[] ids) {
        return R.status(bindService.batchDelete(List.of(ids)));
    }

    /**
     * 导出账号绑定列表
     */
    @SaCheckPermission("base:bind:export")
    @Log(title = "账号绑定", operType = DictOperType.EXPORT)
    @PostMapping("/exportBinds")
    public void exportBinds(BindBo bo, PageQuery pageQuery, HttpServletResponse response) {
        PageResult<BindVo> pageResult = bindService.page(bo, pageQuery);
        ExcelUtil.exportExcel(pageResult.getRecords(), "账号绑定", BindVo.class, response);
    }

    /**
     * 获取账号绑定导入模板
     */
    @PostMapping("/templateBinds")
    public void templateBinds(HttpServletResponse response) {
        ExcelUtil.exportExcel(new ArrayList<>(), "账号绑定模板", BindVo.class, response);
    }

    /**
     * 导入账号绑定
     *
     * @param file 导入文件
     */
    @Log(title = "账号绑定", operType = DictOperType.IMPORT)
    @SaCheckPermission("base:bind:import")
    @PostMapping(value = "/importBinds", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<Void> importBinds(MultipartFile file) throws Exception {
        ExcelResult<BindVo> excelResult = ExcelUtil.importExcel(file.getInputStream(), BindVo.class, true);
        List<BindBo> boList = MapstructUtils.convert(excelResult.getList(), BindBo.class);
        bindService.batchSave(boList);
        return R.ok(excelResult.getAnalysis());
    }
}
