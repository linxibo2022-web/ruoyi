package plus.ruoyi.system.auth.controller;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;

import plus.ruoyi.common.core.constant.CacheNames;
import plus.ruoyi.common.core.constant.I18nKeys;
import plus.ruoyi.common.core.constant.TenantConstants;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.core.domain.model.LoginBody;
import plus.ruoyi.common.core.domain.model.LoginUser;
import plus.ruoyi.common.core.domain.model.RegisterBody;
import plus.ruoyi.common.core.domain.model.SocialLoginBody;
import plus.ruoyi.common.core.utils.MapstructUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.core.utils.ValidatorUtils;
import plus.ruoyi.common.encrypt.annotation.ApiEncrypt;
import plus.ruoyi.common.json.utils.JsonUtils;
import plus.ruoyi.common.redis.utils.CacheUtils;
import plus.ruoyi.common.satoken.utils.LoginHelper;
import plus.ruoyi.common.social.config.properties.SocialLoginConfigProperties;
import plus.ruoyi.common.social.config.properties.SocialProperties;
import plus.ruoyi.common.social.utils.SocialUtils;
import plus.ruoyi.common.tenant.helper.TenantHelper;
import plus.ruoyi.system.auth.domain.bo.UserProfileUpdateBo;
import plus.ruoyi.system.auth.service.IAuthStrategy;
import plus.ruoyi.system.auth.service.SysLoginService;
import plus.ruoyi.system.auth.service.SysRegisterService;
import plus.ruoyi.system.auth.domain.vo.AuthTokenVo;
import plus.ruoyi.system.config.service.ISysConfigService;
import plus.ruoyi.system.core.domain.vo.SysUserVo;
import plus.ruoyi.system.core.service.ISysSocialService;
import plus.ruoyi.system.core.service.ISysUserService;
import plus.ruoyi.system.tenant.domain.bo.SysTenantBo;
import plus.ruoyi.system.tenant.domain.vo.SysTenantVo;
import plus.ruoyi.system.tenant.domain.vo.TenantConfigVo;
import plus.ruoyi.system.tenant.domain.vo.TenantOptionVo;
import plus.ruoyi.system.tenant.service.ISysTenantService;

import static plus.ruoyi.common.satoken.utils.LoginHelper.getLoginUser;

/**
 * 认证控制器
 * 提供用户认证相关的API接口，包括登录、注销、注册、社交账号绑定等功能
 *
 * @author Lion Li
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    // 认证服务
    private final SysLoginService loginService;
    private final SysRegisterService registerService;

    // 业务服务
    private final ISysUserService sysUserService;
    private final ISysSocialService socialUserService;
    private final ISysConfigService configService;
    private final ISysTenantService tenantService;

    // 配置属性
    private final SocialProperties socialProperties;

    // ==================== 基础认证相关接口 ====================

    /**
     * 用户登录方法
     * 根据不同的认证类型调用相应的认证策略进行用户登录
     *
     * @param body 包含登录信息的JSON字符串，将被解析为LoginBody对象
     * @return 返回登录结果，包括访问令牌等信息
     */
    @SaIgnore
    @ApiEncrypt
    @PostMapping("/userLogin")
    public R<AuthTokenVo> userLogin(@RequestBody String body) {
        // 解析JSON字符串为LoginBody对象，包含认证所需的信息
        LoginBody loginBody = JsonUtils.parseObject(body, LoginBody.class);
        // 验证LoginBody对象的必填字段
        ValidatorUtils.validate(loginBody);
        // 检查租户是否有效（存在且可用）
        loginService.checkTenant(TenantHelper.getTenantId());
        // 根据认证类型（密码、短信、社交账号等）选择对应的认证策略进行登录
        return R.ok(IAuthStrategy.login(body, loginBody.getAuthType()));
    }

    /**
     * 用户退出登录
     * 清除用户的登录状态和相关缓存
     *
     * @return 退出结果
     */
    @SaIgnore
    @PostMapping("/userLogout")
    public R<Void> userLogout() {
        // 调用登录服务的登出方法
        loginService.logout();
        return R.ok(I18nKeys.User.LOGOUT_SUCCESS);
    }

    /**
     * 用户注册
     * 验证注册信息并创建新用户
     *
     * @param user 注册信息
     * @return 注册结果
     */
    @SaIgnore
    @ApiEncrypt
    @PostMapping("/userRegister")
    public R<Void> userRegister(@Validated @RequestBody RegisterBody user) {
        // 检查是否有邀请码
        boolean hasInviteCode = StringUtils.isNotBlank(user.getInviteCode());

        // 如果没有邀请码，检查系统是否开启公开注册功能
        if (!hasInviteCode && !configService.getRegisterEnabled(TenantHelper.getTenantId())) {
            return R.fail("当前系统未开放公开注册，请使用邀请码注册！");
        }

        // 调用注册服务创建用户
        registerService.registerPcUser(user);
        return R.ok();
    }

    /**
     * 更新用户头像和昵称
     * 用于用户授权后更新个人资料信息
     *
     * @param userProfileUpdateBo 用户资料更新信息
     * @return 更新结果
     */
    @PostMapping("/updateUserProfile")
    public R<Void> updateUserProfile(@Validated @RequestBody UserProfileUpdateBo userProfileUpdateBo) {
        // 获取当前登录用户
        LoginUser loginUser = getLoginUser();
        if (ObjectUtil.isNull(loginUser)) {
            return R.fail("请先登录！");
        }
        // 查询用户信息进行验证
        SysUserVo sysUser = sysUserService.getUserById(loginUser.getUserId());
        if (ObjectUtil.isNull(sysUser)) {
            return R.fail("用户信息不存在，请重新登录！");
        }

        // 更新用户头像和昵称
        sysUserService.updateUserNickNameAvatar(
            loginUser.getUserId(),
            userProfileUpdateBo.getNickName(),
            userProfileUpdateBo.getAvatar()
        );

        // 清除用户头像昵称相关缓存
        CacheUtils.evict(CacheNames.SYS_NICKNAME, loginUser.getUserId());
        CacheUtils.evict(CacheNames.SYS_AVATAR, loginUser.getUserId());

        // 更新登录用户会话中的昵称信息
        loginUser.setNickName(userProfileUpdateBo.getNickName());

        // 更新缓存中的用户信息
        StpUtil.getTokenSession().set(LoginHelper.LOGIN_USER, loginUser);

        return R.ok("用户资料更新成功");
    }

    // ==================== 社交认证相关接口 ====================

    /**
     * 获取社交认证跳转URL
     * 用于第三方账号认证的第一步，获取重定向到第三方认证页面的URL
     *
     * @param source 认证来源（如：gitee、github等）
     * @param domain 域名
     * @param inviteCode 邀请码(可选，用于注册时绑定角色和部门)
     * @return 包含授权URL的结果
     */
    @SaIgnore
    @GetMapping("/socialBindUrl/{source}")
    public R<String> socialBindUrl(@NotBlank(message = "source不能为空") @PathVariable("source") String source,
                                     @RequestParam String domain,
                                     @RequestParam(required = false) String inviteCode) {
        // 获取指定平台的配置信息
        SocialLoginConfigProperties obj = socialProperties.getType().get(source);
        // 检查平台配置是否存在
        if (ObjectUtil.isNull(obj)) {
            return R.fail(source + "平台账号暂不支持");
        }
        AuthRequest authRequest = SocialUtils.getAuthRequest(source, socialProperties);
        HashMap<String, String> map = MapUtil.newHashMap();
        map.put("tenantId", TenantHelper.getTenantId());
        map.put("domain", domain);
        // 创建唯一状态码，防止CSRF攻击
        map.put("state", AuthStateUtils.createState());
        // 传递邀请码（如果有）
        if (StringUtils.isNotBlank(inviteCode)) {
            map.put("inviteCode", inviteCode);
        }

        // 生成授权URL，并将参数Base64编码
        String authorizeUrl = authRequest.authorize(Base64.encode(JsonUtils.toJsonString(map), StandardCharsets.UTF_8));
        return R.ok("操作成功", authorizeUrl);
    }

    /**
     * 前端回调绑定授权(需要token)
     * 完成第三方账号与系统用户的绑定
     *
     * @param loginBody 社交登录信息
     * @return 绑定结果
     */
    @PostMapping("/socialBind")
    public R<Void> socialBind(@RequestBody SocialLoginBody loginBody) {
        // 获取第三方登录信息
        AuthResponse<AuthUser> response = SocialUtils.loginAuth(
            loginBody.getSource(), loginBody.getSocialCode(),
            loginBody.getSocialState(), socialProperties);
        // 获取授权用户数据
        AuthUser authUserData = response.getData();

        // 判断授权响应是否成功
        if (!response.ok()) {
            return R.fail(response.getMsg());
        }

        // 调用登录服务绑定社交账号
        loginService.bindSocialAccount(authUserData);
        return R.ok();
    }

    /**
     * 取消社交授权(需要token)
     * 解除用户与第三方账号的绑定关系
     *
     * @param socialId 社交绑定ID
     * @return 解绑结果
     */
    @DeleteMapping(value = "/socialUnbind/{socialId}")
    public R<Void> socialUnbind(@NotNull(message = "社交绑定id不能为空") @PathVariable Long socialId) {
        // 调用社交用户服务删除绑定关系
        int rows = socialUserService.batchDelete(List.of(socialId));
        return rows > 0 ? R.ok() : R.fail("取消授权失败");
    }

    /**
     * 获取租户开关和页面可用的租户列表
     * 根据当前环境和用户状态返回可用的租户信息
     *
     * @return 租户启用状态和租户列表
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @GetMapping("/getTenantConfig")
    public R<TenantConfigVo> getTenantConfig() {
        // 返回对象
        TenantConfigVo tenantConfigVo = new TenantConfigVo();
        // 设置租户功能是否启用
        tenantConfigVo.setTenantEnabled(TenantHelper.isEnable());
        // 如果未开启租户这直接返回
        if (!tenantConfigVo.getTenantEnabled()) {
            return R.ok(tenantConfigVo);
        }

        // 查询所有租户列表
        List<SysTenantVo> tenantList = tenantService.list(new SysTenantBo());
        // 转换为前端展示对象
        List<TenantOptionVo> voList = MapstructUtils.convert(tenantList, TenantOptionVo.class);
        // 设置租户列表
        tenantConfigVo.setVoList(voList);
        return R.ok(tenantConfigVo);
    }
}
