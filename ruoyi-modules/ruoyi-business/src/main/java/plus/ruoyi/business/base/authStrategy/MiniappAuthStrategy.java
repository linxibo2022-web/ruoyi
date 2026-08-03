package plus.ruoyi.business.base.authStrategy;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import plus.ruoyi.business.base.domain.bo.BindBo;
import plus.ruoyi.business.base.domain.vo.BindVo;
import plus.ruoyi.business.base.service.IBindService;
import plus.ruoyi.common.core.dict.DictEnableStatus;
import plus.ruoyi.common.core.dict.DictPlatformType;
import plus.ruoyi.common.core.domain.dto.PlatformDTO;
import plus.ruoyi.common.core.domain.model.LoginUser;
import plus.ruoyi.common.core.domain.model.PlatformLoginBody;
import plus.ruoyi.common.core.enums.UserType;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.service.PlatformService;
import plus.ruoyi.common.core.utils.MapstructUtils;
import plus.ruoyi.common.core.utils.ValidatorUtils;
import plus.ruoyi.common.json.utils.JsonUtils;
import plus.ruoyi.common.satoken.utils.LoginHelper;
import plus.ruoyi.system.auth.service.SysRegisterService;
import plus.ruoyi.system.auth.domain.vo.AuthTokenVo;
import plus.ruoyi.business.base.domain.vo.PlatformUserInfoVo;
import plus.ruoyi.system.core.domain.vo.SysUserVo;
import plus.ruoyi.system.auth.service.IAuthStrategy;
import plus.ruoyi.system.auth.service.SysLoginService;
import plus.ruoyi.system.core.service.ISysUserService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


/**
 * 小程序认证策略
 * 实现基于小程序的用户认证流程，主要用于微信小程序等场景
 * <p>
 * 只有当小程序模块启用时才加载此策略
 *
 * @author Michelle.Chung 抓蛙师
 */
@Slf4j
@Service("miniapp" + IAuthStrategy.BASE_NAME)
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "module", name = "miniapp-enabled", havingValue = "true", matchIfMissing = true)
public class MiniappAuthStrategy implements IAuthStrategy {

    private final SysLoginService loginService;
    private final SysRegisterService registerService;
    private final ISysUserService userService;
    private final IBindService bindService;
    private final PlatformService platformService;
    private final WxMaService wxMaService;

    /**
     * 多平台小程序登录实现
     * 完成小程序认证的完整流程：解析请求、获取openid、加载用户、生成令牌
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

        // 2. 获取用户信息
        PlatformUserInfoVo platformUserInfoVo = getMiniappUserIdentity(loginBody);

        // 3. 加载或创建用户
        SysUserVo user = loadOrCreateUser(platformUserInfoVo);

        // 4. 构建小程序登录用户
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
     * 根据平台获取用户信息
     */
    private PlatformUserInfoVo getMiniappUserIdentity(PlatformLoginBody loginBody) {
        DictPlatformType platformType = DictPlatformType.getByValue(loginBody.getPlatform());
        if (platformType == null) {
            throw new ServiceException("不支持的小程序平台: " + loginBody.getPlatform());
        }

        // 获取用户信息
        PlatformUserInfoVo platformUserInfoVo = switch (platformType) {
            case MP_WEIXIN -> getWechatUserIdentity(loginBody);
            case MP_QQ -> getQQUserIdentity(loginBody);
            case MP_ALIPAY -> getAlipayUserIdentity(loginBody);
            case MP_JD -> getJDUserIdentity(loginBody);
            case MP_KUAISHOU -> getKuaishouUserIdentity(loginBody);
            case MP_LARK -> getLarkUserIdentity(loginBody);
            case MP_BAIDU -> getBaiduUserIdentity(loginBody);
            case MP_TOUTIAO -> getToutiaoUserIdentity(loginBody);
            case MP_XHS -> getXHSUserIdentity(loginBody);
            default -> throw new ServiceException("暂不支持的小程序平台: " + platformType.getLabel());
        };
        // 统一设置appid和平台信息
        platformUserInfoVo.setAppid(loginBody.getAppid());
        platformUserInfoVo.setPlatform(loginBody.getPlatform());
        return platformUserInfoVo;
    }

    /**
     * 微信小程序用户信息获取
     */
    private PlatformUserInfoVo getWechatUserIdentity(PlatformLoginBody loginBody) {
        try {
            // 切换到对应的小程序服务
            wxMaService.switchover(loginBody.getAppid());
            WxMaJscode2SessionResult session = wxMaService.getUserService()
                .getSessionInfo(loginBody.getPlatformCode());

            PlatformUserInfoVo platformUserInfoVo = new PlatformUserInfoVo();
            platformUserInfoVo.setOpenid(session.getOpenid());
            platformUserInfoVo.setUnionid(session.getUnionid());
            return platformUserInfoVo;
        } catch (NullPointerException nullPointerException) {
            throw new ServiceException("平台微信小程序未配置或禁用, 请联系管理员");
        } catch (Exception e) {
            log.error("获取微信小程序用户信息失败", e);
            throw new ServiceException("获取微信用户信息失败: " + e.getMessage());
        }
    }

    /**
     * QQ小程序用户信息获取
     */
    private PlatformUserInfoVo getQQUserIdentity(PlatformLoginBody loginBody) {
        try {
            // QQ小程序API调用
            String url = "https://api.q.qq.com/sns/jscode2session";
            Map<String, Object> params = new HashMap<>();
            params.put("appid", loginBody.getAppid());
            params.put("secret", getAppSecret(loginBody.getAppid(), DictPlatformType.MP_QQ));
            params.put("js_code", loginBody.getPlatformCode());
            params.put("grant_type", "authorization_code");

            // 调用QQ API
            // String response = restTemplate.postForObject(url, params, String.class);
            // 解析响应获取openid

            PlatformUserInfoVo userInfo = new PlatformUserInfoVo();
            // userInfo.setOpenid(parseOpenidFromQQResponse(response));

            return userInfo;
        } catch (Exception e) {
            log.error("获取QQ小程序用户信息失败", e);
            throw new ServiceException("获取QQ用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 支付宝小程序用户信息获取
     */
    private PlatformUserInfoVo getAlipayUserIdentity(PlatformLoginBody loginBody) {
        try {
            // 支付宝小程序API调用
            String url = "https://openapi.alipay.com/gateway.do";

            PlatformUserInfoVo userInfo = new PlatformUserInfoVo();
            // 实现支付宝小程序用户信息获取逻辑

            return userInfo;
        } catch (Exception e) {
            log.error("获取支付宝小程序用户信息失败", e);
            throw new ServiceException("获取支付宝用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 京东小程序用户信息获取
     */
    private PlatformUserInfoVo getJDUserIdentity(PlatformLoginBody loginBody) {
        try {
            PlatformUserInfoVo userInfo = new PlatformUserInfoVo();
            // 实现京东小程序用户信息获取逻辑

            return userInfo;
        } catch (Exception e) {
            log.error("获取京东小程序用户信息失败", e);
            throw new ServiceException("获取京东用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 快手小程序用户信息获取
     */
    private PlatformUserInfoVo getKuaishouUserIdentity(PlatformLoginBody loginBody) {
        try {
            PlatformUserInfoVo userInfo = new PlatformUserInfoVo();
            // 实现快手小程序用户信息获取逻辑

            return userInfo;
        } catch (Exception e) {
            log.error("获取快手小程序用户信息失败", e);
            throw new ServiceException("获取快手用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 飞书小程序用户信息获取
     */
    private PlatformUserInfoVo getLarkUserIdentity(PlatformLoginBody loginBody) {
        try {
            PlatformUserInfoVo userInfo = new PlatformUserInfoVo();
            // 实现飞书小程序用户信息获取逻辑

            return userInfo;
        } catch (Exception e) {
            log.error("获取飞书小程序用户信息失败", e);
            throw new ServiceException("获取飞书用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 百度小程序用户信息获取
     */
    private PlatformUserInfoVo getBaiduUserIdentity(PlatformLoginBody loginBody) {
        try {
            String url = "https://spapi.baidu.com/oauth/jscode2sessionkey";
            Map<String, Object> params = new HashMap<>();
            params.put("code", loginBody.getPlatformCode());
            params.put("client_id", loginBody.getAppid());
            params.put("sk", getAppSecret(loginBody.getAppid(), DictPlatformType.MP_BAIDU));

            PlatformUserInfoVo userInfo = new PlatformUserInfoVo();
            // 调用百度API获取用户信息

            return userInfo;
        } catch (Exception e) {
            log.error("获取百度小程序用户信息失败", e);
            throw new ServiceException("获取百度用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 头条小程序用户信息获取
     */
    private PlatformUserInfoVo getToutiaoUserIdentity(PlatformLoginBody loginBody) {
        try {
            String url = "https://developer.toutiao.com/api/apps/jscode2session";
            Map<String, Object> params = new HashMap<>();
            params.put("appid", loginBody.getAppid());
            params.put("secret", getAppSecret(loginBody.getAppid(), DictPlatformType.MP_TOUTIAO));
            params.put("code", loginBody.getCode());

            PlatformUserInfoVo userInfo = new PlatformUserInfoVo();
            // 调用头条API获取用户信息

            return userInfo;
        } catch (Exception e) {
            log.error("获取头条小程序用户信息失败", e);
            throw new ServiceException("获取头条用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 小红书小程序用户信息获取
     */
    private PlatformUserInfoVo getXHSUserIdentity(PlatformLoginBody loginBody) {
        try {
            PlatformUserInfoVo userInfo = new PlatformUserInfoVo();
            // 实现小红书小程序用户信息获取逻辑

            return userInfo;
        } catch (Exception e) {
            log.error("获取小红书小程序用户信息失败", e);
            throw new ServiceException("获取小红书用户信息失败: " + e.getMessage());
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
            // 创建新用户
            sysUser = registerService.registerAppUser(null, null, null, null, null);
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
     * 获取小程序密钥
     * 根据appid和平台类型获取对应的密钥
     */
    private String getAppSecret(String appid, DictPlatformType platformType) {
        // 这里应该从配置文件或数据库中获取对应的密钥
        // 可以根据 appid + platformType 作为key来获取
        PlatformDTO platformDTO = platformService.getPlatformByAppidAndType(appid, platformType.getValue());
        if (ObjectUtil.isNull(platformDTO)) {
            throw new ServiceException("未配置该平台的密钥: " + platformType.getLabel());
        }
        return platformDTO.getSecret();
    }
}
