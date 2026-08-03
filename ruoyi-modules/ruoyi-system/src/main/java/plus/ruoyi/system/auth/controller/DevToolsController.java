package plus.ruoyi.system.auth.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;

import plus.ruoyi.common.core.constant.I18nKeys;
import plus.ruoyi.common.core.domain.model.LoginUser;
import plus.ruoyi.common.core.enums.UserType;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.satoken.utils.LoginHelper;
import plus.ruoyi.common.tenant.helper.TenantHelper;
import plus.ruoyi.system.auth.service.SysLoginService;
import plus.ruoyi.system.core.domain.vo.SysUserVo;
import plus.ruoyi.system.core.service.ISysUserService;

/**
 * 开发工具控制器
 * 提供开发和调试相关的辅助接口
 * 仅在开发环境可用,开发/测试环境不注册
 *
 * @author 抓蛙师
 */
@Profile("dev")  // 只在开发环境注册此Controller
@SaIgnore
@RequiredArgsConstructor
@RestController
@RequestMapping("")
public class DevToolsController {

    private final SysLoginService loginService;
    private final ISysUserService userService;

    /**
     * 快速获取Token
     * 用于单元测试和开发调试,仅在开发环境可用
     *
     * @param userId 用户ID(可选,不传则默认为超级管理员ID=1)
     * @return Bearer token字符串
     */
    @GetMapping("/getToken")
    public String getToken(@RequestParam(required = false) Long userId) {
        // 默认使用超级管理员ID
        Long targetUserId = ObjectUtil.defaultIfNull(userId, 1L);

        // 忽略租户限制查询用户
        return TenantHelper.ignore(() -> {
            // 查询用户信息
            SysUserVo user = userService.getUserById(targetUserId);
            if (ObjectUtil.isNull(user)) {
                throw ServiceException.of(I18nKeys.User.ACCOUNT_NOT_EXISTS, targetUserId);
            }

            // 使用用户所属租户进行后续操作
            return TenantHelper.dynamic(user.getTenantId(), () -> {
                // 创建登录用户对象
                LoginUser loginUser = loginService.createLoginUser(user);

                // 设置用户类型为PC用户
                UserType userType = UserType.PC_USER;
                loginUser.setDeviceType(userType.getDeviceType());

                // 构建登录参数
                SaLoginParameter loginParameter = new SaLoginParameter();
                loginParameter.setDeviceType(userType.getDeviceType());
                loginParameter.setTimeout(userType.getTimeout());
                loginParameter.setActiveTimeout(userType.getActiveTimeout());

                // 执行登录
                LoginHelper.login(loginUser, loginParameter);

                // 返回 Bearer token
                return SaManager.getConfig().getTokenPrefix() + " " + StpUtil.getTokenValue();
            });
        });
    }
}
