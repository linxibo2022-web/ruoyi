package plus.ruoyi.system.core.controller;

import java.util.List;

import jakarta.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;

import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.satoken.utils.LoginHelper;

import plus.ruoyi.system.core.domain.vo.SysSocialVo;
import plus.ruoyi.system.core.service.ISysSocialService;

/**
 * 社会化关系
 *
 * @author thiszhc
 * @date 2023-06-16
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/social")
public class SysSocialController {

    /* 业务服务 */
    private final ISysSocialService socialService;

    /**
     * 查询社会化关系列表
     */
    @GetMapping("/getSocialBindingList")
    public R<List<SysSocialVo>> getSocialBindingList() {
        return R.ok(socialService.listSocialsByUserId(LoginHelper.getUserId()));
    }

    /**
     * 查询指定用户的社会化关系列表（管理员）
     */
    @SaCheckPermission("system:user:query")
    @GetMapping("/listSocialsByUserId/{userId}")
    public R<List<SysSocialVo>> listSocialsByUserId(
        @NotNull(message = "用户ID不能为空") @PathVariable Long userId) {
        return R.ok(socialService.listSocialsByUserId(userId));
    }

}
