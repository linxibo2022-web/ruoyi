package plus.ruoyi.business.base.authStrategy;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.beans.factory.annotation.Autowired;
import plus.ruoyi.business.base.domain.bo.BindBo;
import plus.ruoyi.business.base.domain.vo.BindVo;
import plus.ruoyi.business.base.domain.vo.PlatformUserInfoVo;
import plus.ruoyi.business.base.service.IBindService;
import plus.ruoyi.common.core.dict.DictEnableStatus;
import plus.ruoyi.common.core.dict.DictPlatformType;
import plus.ruoyi.common.core.domain.model.LoginUser;
import plus.ruoyi.common.core.domain.model.PlatformLoginBody;
import plus.ruoyi.common.core.enums.UserType;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.utils.MapstructUtils;
import plus.ruoyi.common.core.utils.ValidatorUtils;
import plus.ruoyi.common.json.utils.JsonUtils;
import plus.ruoyi.common.satoken.utils.LoginHelper;
import plus.ruoyi.system.auth.service.IAuthStrategy;
import plus.ruoyi.system.auth.service.SysLoginService;
import plus.ruoyi.system.auth.service.SysRegisterService;
import plus.ruoyi.system.auth.domain.vo.AuthTokenVo;
import plus.ruoyi.system.core.domain.vo.SysUserVo;
import plus.ruoyi.system.core.service.ISysUserService;
import plus.ruoyi.system.oss.domain.vo.SysOssVo;
import plus.ruoyi.system.oss.service.ISysOssService;
import org.springframework.stereotype.Service;

/**
 * 公众号认证策略
 * 实现基于公众号的用户认证流程，支持微信公众号等场景
 *
 * @author Michelle.Chung 抓蛙师
 */
@Slf4j
@Service("mp" + IAuthStrategy.BASE_NAME)
@RequiredArgsConstructor
public class MpAuthStrategy implements IAuthStrategy {

    private final SysLoginService loginService;
    private final SysRegisterService registerService;
    private final ISysUserService userService;
    private final IBindService bindService;
    private final ISysOssService sysOssService;
    @Autowired(required = false)
    private WxMpService wxMpService;

    /**
     * 多平台公众号登录实现
     * 完成公众号认证的完整流程：解析请求、获取openid、加载用户、生成令牌
     *
     * @param body 登录请求体JSON字符串
     * @return 登录结果，包含访问令牌等信息
     */
    @Override
    public AuthTokenVo login(String body) {
        // 1. 解析请求体，将JSON转换为PlatformLoginBody对象
        PlatformLoginBody loginBody = JsonUtils.parseObject(body, PlatformLoginBody.class);
        // 使用验证框架校验必填字段
        ValidatorUtils.validate(loginBody);

        // 2. 根据code获取openid和unionid
        PlatformUserInfoVo platformUserInfoVo = getMpUserIdentity(loginBody);

        // 3. 加载或创建用户
        SysUserVo user = loadOrCreateUser(platformUserInfoVo);

        // 4. 构建公众号登录用户
        LoginUser loginUser = loginService.createLoginUser(user);
        // 设置用户设备类型为移动端
        UserType userType = UserType.APP_USER;
        loginUser.setDeviceType(userType.getDeviceType());
        //设置登录用户的所在平台 appid openid等(可用于识别平台 智能调用对应平台的支付)
        loginUser.setPlatform(platformUserInfoVo.getPlatform());
        loginUser.setAppid(platformUserInfoVo.getAppid());
        loginUser.setUnionid(platformUserInfoVo.getUnionid());
        loginUser.setOpenid(platformUserInfoVo.getOpenid());

        // 构建登录模型，设置令牌参数
        SaLoginParameter loginParameter = new SaLoginParameter();
        loginParameter.setDeviceType(userType.getDeviceType());
        loginParameter.setTimeout(userType.getTimeout());
        loginParameter.setActiveTimeout(userType.getActiveTimeout());

        // 调用LoginHelper生成令牌并完成登录
        LoginHelper.login(loginUser, loginParameter);

        // 构建并返回登录结果
        AuthTokenVo authTokenVo = new AuthTokenVo();
        authTokenVo.setAccessToken(StpUtil.getTokenValue());
        authTokenVo.setExpireIn(StpUtil.getTokenTimeout());
        return authTokenVo;
    }

    /**
     * 获取公众号用户身份标识
     * 通过OAuth 2.0流程获取用户的openid、unionid等标识信息
     */
    private PlatformUserInfoVo getMpUserIdentity(PlatformLoginBody loginBody) {
        DictPlatformType platformType = DictPlatformType.getByValue(loginBody.getPlatform());
        if (platformType == null) {
            throw new ServiceException("不支持的公众号平台: " + loginBody.getPlatform());
        }

        // 获取OAuth信息
        PlatformUserInfoVo platformUserInfo = switch (platformType) {
            case MP_OFFICIAL_ACCOUNT -> getWechatMpUserIdentity(loginBody);
            default -> throw new ServiceException("暂不支持的公众号平台: " + platformType.getLabel());
        };
        // 统一设置appid和平台信息
        platformUserInfo.setAppid(loginBody.getAppid());
        platformUserInfo.setPlatform(loginBody.getPlatform());
        return platformUserInfo;
    }

    /**
     * 微信公众号用户身份标识获取
     * 通过OAuth 2.0认证流程获取openid和unionid
     */
    private PlatformUserInfoVo getWechatMpUserIdentity(PlatformLoginBody loginBody) {
        try {
            // 切换到对应的公众号服务
            wxMpService.switchoverTo(loginBody.getAppid());

            // 使用code换取access_token
            WxOAuth2AccessToken accessToken = wxMpService.getOAuth2Service()
                .getAccessToken(loginBody.getPlatformCode());

            if (ObjectUtil.isNull(accessToken)) {
                throw new ServiceException("获取微信访问令牌失败");
            }

            // 创建平台用户信息对象
            PlatformUserInfoVo platformUserInfoVo = new PlatformUserInfoVo();
            platformUserInfoVo.setOpenid(accessToken.getOpenId());
            platformUserInfoVo.setUnionid(accessToken.getUnionId());
            // 保存accessToken，后续获取用户详细信息时使用
            platformUserInfoVo.setCredential(accessToken);

            return platformUserInfoVo;
        } catch (WxErrorException e) {
            log.error("微信公众号OAuth认证失败，错误码：{}，错误信息：{}",
                e.getError().getErrorCode(), e.getError().getErrorMsg());
            throw new ServiceException("微信OAuth认证失败: " + e.getMessage());
        }
    }

    /**
     * 加载或创建用户
     */
    private SysUserVo loadOrCreateUser(PlatformUserInfoVo platformUserInfoVo) {
        //根据平台类型和openid获取账号绑定信息
        BindVo bindVo = bindService.getOrCreateBind(platformUserInfoVo);

        SysUserVo sysUser = null;
        if (ObjectUtil.isNotNull(bindVo.getUserId())) {
            sysUser = userService.getUserById(bindVo.getUserId());
        }

        // 如果用户不存在，创建新用户
        if (ObjectUtil.isNull(sysUser)) {
            log.info("登录用户：{} 不存在，创建新用户", platformUserInfoVo.getOpenid());

            // 获取用户详细信息
            fetchUserProfile(platformUserInfoVo);

            // 上传用户头像
            String avatarUrl = null;
            if (ObjectUtil.isNotEmpty(platformUserInfoVo.getAvatar())) {
                try {
                    SysOssVo sysOssVo = sysOssService.saveRemoteImageToOss("avatar", null,"/头像", platformUserInfoVo.getAvatar());
                    avatarUrl = sysOssVo.getUrl();
                } catch (Exception e) {
                    log.error("上传用户头像失败", e);
                }
            }

            // 创建新用户
            sysUser = registerService.registerAppUser(null, null, null,
                platformUserInfoVo.getNickName(), avatarUrl);
            // 更新账号关联的用户ID
            bindService.updateBindUserId(MapstructUtils.convert(bindVo, BindBo.class), sysUser.getUserId());
        } else {
            // 检查用户状态
            if (DictEnableStatus.DISABLED.getValue().equals(sysUser.getStatus())) {
                log.info("登录用户：{} 已被停用.", platformUserInfoVo.getOpenid());
                throw new ServiceException("用户已被停用");
            }
        }

        return sysUser;
    }

    /**
     * 为新用户获取详细信息（昵称、头像等）
     * 注意：这个方法只在创建新用户时调用
     */
    private void fetchUserProfile(PlatformUserInfoVo userInfoVo) {
        DictPlatformType platformType = DictPlatformType.getByValue(userInfoVo.getPlatform());
        if (platformType == null) {
            return;
        }

        switch (platformType) {
            case MP_OFFICIAL_ACCOUNT -> {
                wxMpService.switchoverTo(userInfoVo.getAppid());
                if (ObjectUtil.isNull(userInfoVo.getCredential())) {
                    throw ServiceException.of("微信公众号缺失访问令牌");
                }
                WxOAuth2AccessToken accessToken = (WxOAuth2AccessToken) userInfoVo.getCredential();
                try {
                    WxOAuth2UserInfo userInfo = wxMpService.getOAuth2Service().getUserInfo(accessToken, null);
                    if (ObjectUtil.isNull(userInfo)) {
                        throw ServiceException.of("微信公众号获取用户信息失败");
                    }
                    // 设置用户昵称
                    userInfoVo.setNickName(userInfo.getNickname());
                    // 设置用户头像
                    userInfoVo.setAvatar(userInfo.getHeadImgUrl());
                } catch (NullPointerException nullPointerException) {
                    throw new ServiceException("平台微信公众号未配置或禁用, 请联系管理员");
                } catch (WxErrorException e) {
                    throw ServiceException.of("微信公众号获取用户信息服务异常");
                }
            }
            default -> throw ServiceException.of("不支持的公众号平台: " + platformType.getLabel());
        }
    }

}
