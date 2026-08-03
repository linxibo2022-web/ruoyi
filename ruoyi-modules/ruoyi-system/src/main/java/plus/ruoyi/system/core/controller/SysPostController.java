package plus.ruoyi.system.core.controller;

import java.util.ArrayList;
import java.util.List;

import cn.dev33.satoken.annotation.SaMode;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.util.ObjectUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

import plus.ruoyi.common.core.constant.I18nKeys;
import plus.ruoyi.common.core.dict.DictEnableStatus;
import plus.ruoyi.common.core.dict.DictOperType;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.excel.utils.ExcelUtil;
import plus.ruoyi.common.idempotent.annotation.RepeatSubmit;
import plus.ruoyi.common.log.annotation.Log;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;

import plus.ruoyi.system.core.domain.bo.SysPostBo;
import plus.ruoyi.system.core.domain.vo.SysPostVo;
import plus.ruoyi.system.core.service.ISysPostService;

/**
 * 岗位信息操作处理
 *
 * @author Lion Li
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/post")
public class SysPostController {

    /* 业务服务 */
    private final ISysPostService postService;

    /**
     * 获取岗位列表
     */
    @SaCheckPermission("system:post:query")
    @GetMapping("/pagePosts")
    public R<PageResult<SysPostVo>> pagePosts(SysPostBo post, PageQuery pageQuery) {
        return R.ok(postService.page(post, pageQuery));
    }

    /**
     * 根据岗位编号获取详细信息
     *
     * @param postId 岗位ID
     */
    @SaCheckPermission("system:post:query")
    @GetMapping("/getPost/{postId}")
    public R<SysPostVo> getPost(@NotNull(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long postId) {
        return R.ok(postService.get(postId));
    }

    /**
     * 新增岗位
     */
    @SaCheckPermission("system:post:add")
    @Log(title = "岗位管理", operType = DictOperType.INSERT)
    @RepeatSubmit()
    @PostMapping("/addPost")
    public R<Long> addPost(@Validated @RequestBody SysPostBo post) {
        if (!postService.checkPostNameUnique(post)) {
            return R.fail("新增岗位'" + post.getPostName() + "'失败，岗位名称已存在");
        } else if (!postService.checkPostCodeUnique(post)) {
            return R.fail("新增岗位'" + post.getPostName() + "'失败，岗位编码已存在");
        }
        return R.ok(postService.add(post));
    }

    /**
     * 修改岗位
     */
    @SaCheckPermission("system:post:update")
    @Log(title = "岗位管理", operType = DictOperType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/updatePost")
    public R<Void> updatePost(@Validated @RequestBody SysPostBo post) {
        if (!postService.checkPostNameUnique(post)) {
            return R.fail("修改岗位'" + post.getPostName() + "'失败，岗位名称已存在");
        } else if (!postService.checkPostCodeUnique(post)) {
            return R.fail("修改岗位'" + post.getPostName() + "'失败，岗位编码已存在");
        } else if (DictEnableStatus.DISABLED.getValue().equals(post.getStatus())
            && postService.countUsersByPostId(post.getPostId()) > 0) {
            return R.fail("该岗位下存在已分配用户，不能禁用!");
        }
        return R.status(postService.update(post));
    }

    /**
     * 删除岗位
     *
     * @param postIds 岗位ID串
     */
    @SaCheckPermission("system:post:delete")
    @Log(title = "岗位管理", operType = DictOperType.DELETE)
    @DeleteMapping("/deletePosts/{postIds}")
    public R<Void> deletePosts(@NotEmpty(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long[] postIds) {
        return R.status(postService.batchDelete(List.of(postIds)));
    }

    /**
     * 获取岗位选择框列表
     *
     * @param postIds 岗位ID串
     * @param deptId  部门id
     */
    @SaCheckPermission(value = {"system:post:query", "system:user:add", "system:user:update"}, mode = SaMode.OR)
    @GetMapping("/getPostOptions")
    public R<List<SysPostVo>> getPostOptions(@RequestParam(required = false) Long[] postIds, @RequestParam(required = false) Long deptId) {
        List<SysPostVo> postVoList = new ArrayList<>();
        if (ObjectUtil.isNotNull(deptId)) {
            SysPostBo post = new SysPostBo();
            post.setDeptId(deptId);
            postVoList = postService.list(post);
        } else if (postIds != null) {
            postVoList = postService.listPostsByIds(List.of(postIds));
        }
        return R.ok(postVoList);
    }

    /**
     * 导出岗位列表
     */
    @Log(title = "岗位管理", operType = DictOperType.EXPORT)
    @SaCheckPermission("system:post:export")
    @PostMapping("/exportPosts")
    public void exportPosts(SysPostBo post, PageQuery pageQuery, HttpServletResponse response) {
        PageResult<SysPostVo> pageResult = postService.page(post, pageQuery);
        ExcelUtil.exportExcel(pageResult.getRecords(), "岗位数据", SysPostVo.class, response);
    }
}
