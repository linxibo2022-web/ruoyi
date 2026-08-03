package plus.ruoyi.system.core.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.tree.Tree;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

import plus.ruoyi.common.core.constant.I18nKeys;
import plus.ruoyi.common.core.dict.DictEnableStatus;
import plus.ruoyi.common.core.dict.DictOperType;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.idempotent.annotation.RepeatSubmit;
import plus.ruoyi.common.log.annotation.Log;

import plus.ruoyi.system.core.domain.bo.SysDeptBo;
import plus.ruoyi.system.core.domain.vo.SysDeptVo;
import plus.ruoyi.system.core.service.ISysDeptService;
import plus.ruoyi.system.core.service.ISysPostService;

/**
 * 部门信息
 *
 * @author Lion Li
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/dept")
public class SysDeptController {

    /* 业务服务 */
    private final ISysDeptService deptService;
    private final ISysPostService postService;

    /**
     * 获取部门列表
     */
    @SaCheckPermission("system:dept:query")
    @GetMapping("/listDepts")
    public R<List<SysDeptVo>> listDepts(SysDeptBo dept) {
        return R.ok(deptService.list(dept));
    }

    /**
     * 查询部门列表（排除节点）
     *
     * @param deptId 部门ID
     */
    @SaCheckPermission("system:dept:query")
    @GetMapping("/listDeptsExcludeChild/{deptId}")
    public R<List<SysDeptVo>> listDeptsExcludeChild(@PathVariable(value = "deptId", required = false) Long deptId) {
        List<SysDeptVo> depts = deptService.list(new SysDeptBo());
        depts.removeIf(d -> d.getDeptId().equals(deptId)
            || StringUtils.splitToList(d.getAncestors()).contains(Convert.toStr(deptId)));
        return R.ok(depts);
    }

    /**
     * 根据部门编号获取详细信息
     *
     * @param deptId 部门ID
     */
    @SaCheckPermission("system:dept:query")
    @GetMapping(value = "/getDept/{deptId}")
    public R<SysDeptVo> getDept(@NotNull(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long deptId) {
        deptService.checkDeptDataScope(deptId);
        return R.ok(deptService.get(deptId));
    }

    /**
     * 新增部门
     */
    @SaCheckPermission("system:dept:add")
    @Log(title = "部门管理", operType = DictOperType.INSERT)
    @RepeatSubmit()
    @PostMapping("/addDept")
    public R<Long> addDept(@Validated @RequestBody SysDeptBo dept) {
        if (!deptService.checkDeptNameUnique(dept)) {
            return R.fail("新增部门'" + dept.getDeptName() + "'失败，部门名称已存在");
        }
        return R.ok(deptService.insertDept(dept));
    }

    /**
     * 修改部门
     */
    @SaCheckPermission("system:dept:update")
    @Log(title = "部门管理", operType = DictOperType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/updateDept")
    public R<Void> updateDept(@Validated @RequestBody SysDeptBo dept) {
        Long deptId = dept.getDeptId();
        deptService.checkDeptDataScope(deptId);
        if (!deptService.checkDeptNameUnique(dept)) {
            return R.fail("修改部门'" + dept.getDeptName() + "'失败，部门名称已存在");
        } else if (dept.getParentId().equals(deptId)) {
            return R.fail("修改部门'" + dept.getDeptName() + "'失败，上级部门不能是自己");
        } else if (StringUtils.equals(DictEnableStatus.DISABLED.getValue(), dept.getStatus())) {
            if (deptService.getNormalChildrenDeptById(deptId) > 0) {
                return R.fail("该部门包含未停用的子部门!");
            } else if (deptService.checkDeptExistUser(deptId)) {
                return R.fail("该部门下存在已分配用户，不能禁用!");
            }
        }
        return R.status(deptService.updateDept(dept));
    }

    /**
     * 删除部门
     *
     * @param deptId 部门ID
     */
    @SaCheckPermission("system:dept:delete")
    @Log(title = "部门管理", operType = DictOperType.DELETE)
    @DeleteMapping("/deleteDept/{deptId}")
    public R<Void> deleteDept(@NotNull(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long deptId) {
        if (deptService.hasChildByDeptId(deptId)) {
            return R.warn("存在下级部门,不允许删除");
        }
        if (deptService.checkDeptExistUser(deptId)) {
            return R.warn("部门存在用户,不允许删除");
        }
        if (postService.countPostsByDeptId(deptId) > 0) {
            return R.warn("部门存在岗位,不允许删除");
        }
        deptService.checkDeptDataScope(deptId);
        return R.status(deptService.deleteDeptById(deptId));
    }

    /**
     * 获取部门选择框列表
     *
     * @param deptIds 部门ID串
     */
    @SaCheckPermission("system:dept:query")
    @GetMapping("/listNormalDeptsByIds")
    public R<List<SysDeptVo>> listNormalDeptsByIds(@RequestParam(required = false) Long[] deptIds) {
        return R.ok(deptService.listNormalDeptsByIds(deptIds == null ? null : List.of(deptIds)));
    }

    /**
     * 获取部门树列表
     */
    @SaCheckPermission(value = {"system:user:query", "system:dept:query",
        "system:notice:add", "system:notice:update", "system:role:query"}, mode = SaMode.OR)
    @GetMapping("/getDeptTreeOptions")
    public R<List<Tree<Long>>> getDeptTreeOptions(SysDeptBo dept) {
        return R.ok(deptService.getDeptTree(dept));
    }

}
