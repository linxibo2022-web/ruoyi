package plus.ruoyi.system.auth.listener;

import cn.dev33.satoken.listener.SaTokenListener;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.core.convert.Convert;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.constant.CacheConstants;
import plus.ruoyi.common.core.constant.I18nKeys;
import plus.ruoyi.common.core.dict.DictOperResult;
import plus.ruoyi.common.core.domain.dto.UserOnlineDTO;
import plus.ruoyi.common.core.utils.MessageUtils;
import plus.ruoyi.common.core.utils.ServletUtils;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.core.utils.ip.AddressUtils;
import plus.ruoyi.common.log.event.LoginLogEvent;
import plus.ruoyi.common.redis.utils.RedisUtils;
import plus.ruoyi.common.satoken.utils.LoginHelper;
import plus.ruoyi.common.tenant.helper.TenantHelper;
import plus.ruoyi.system.auth.service.SysLoginService;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Objects;

/**
 * 用户行为监听器
 * 实现SaToken框架的侦听器接口，用于监听用户的登录、注销等操作，并记录相关日志
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Component
@Slf4j
public class UserActionListener implements SaTokenListener {

    private final SysLoginService loginService;

    /**
     * 用户登录事件处理
     * 记录用户登录信息，包括IP、设备、浏览器等，并更新用户登录记录
     *
     * @param authType       认证类型
     * @param loginId        登录ID
     * @param tokenValue     令牌值
     * @param loginParameter 登录模型，包含额外的登录信息
     */
    @Override
    public void doLogin(String authType, Object loginId, String tokenValue, SaLoginParameter loginParameter) {
        // 解析用户代理信息
        UserAgent userAgent = UserAgentUtil.parse(ServletUtils.getRequest().getHeader("User-Agent"));
        // 获取客户端IP
        String ip = ServletUtils.getClientIP();

        // 构建在线用户信息
        UserOnlineDTO dto = new UserOnlineDTO();
        dto.setIpaddr(ip);
        dto.setLoginLocation(AddressUtils.getRealAddressByIp(ip));
        dto.setBrowser(Objects.nonNull(userAgent.getBrowser()) ? userAgent.getBrowser().getName() : "Unknown");
        dto.setOs(Objects.nonNull(userAgent.getOs()) ? userAgent.getOs().getName() : "Unknown");
        dto.setLoginTime(System.currentTimeMillis());
        dto.setTokenId(tokenValue);

        // 从登录模型中获取额外信息
        String tenantId = (String) loginParameter.getExtra(LoginHelper.TENANT_ID);
        Long userId = (Long) loginParameter.getExtra(LoginHelper.USER_ID);
        String userName = (String) loginParameter.getExtra(LoginHelper.USER_NAME);
        dto.setUserName(userName);
        dto.setDeviceType(loginParameter.getDeviceType());
        dto.setDeptName((String) loginParameter.getExtra(LoginHelper.DEPT_NAME));

        // 使用租户工具进行隔离，将在线用户信息存入Redis
        TenantHelper.dynamic(tenantId, () -> {
            if (loginParameter.getTimeout() == -1) {
                RedisUtils.setCacheObject(CacheConstants.ONLINE_TOKEN_KEY + tokenValue, dto);
            } else {
                RedisUtils.setCacheObject(CacheConstants.ONLINE_TOKEN_KEY + tokenValue, dto, Duration.ofSeconds(loginParameter.getTimeout()));
            }
        });

        // 记录登录日志
        LoginLogEvent loginLogEvent = new LoginLogEvent();
        loginLogEvent.setTenantId(tenantId);
        loginLogEvent.setUserId(userId);
        loginLogEvent.setUserName(userName);
        loginLogEvent.setDeviceType(loginParameter.getDeviceType());
        loginLogEvent.setStatus(DictOperResult.SUCCESS.getValue());
        loginLogEvent.setMessage(MessageUtils.message(I18nKeys.User.LOGIN_SUCCESS));
        loginLogEvent.setRequest(ServletUtils.getRequest());
        SpringUtils.context().publishEvent(loginLogEvent);

        // 更新用户登录信息
        loginService.updateUserLoginInfo(userId, ip);
        log.info("user doLogin, userId:{}, token:{}***", loginId, tokenValue.substring(0, Math.min(8, tokenValue.length())));
    }

    /**
     * 用户注销事件处理
     * 清除用户在线状态缓存
     *
     * @param authType   认证类型
     * @param loginId    登录ID
     * @param tokenValue 令牌值
     */
    @Override
    public void doLogout(String authType, Object loginId, String tokenValue) {
        // 从令牌中获取租户ID
        String tenantId = Convert.toStr(StpUtil.getExtra(tokenValue, LoginHelper.TENANT_ID));

        // 使用租户工具进行隔离，删除在线用户缓存
        TenantHelper.dynamic(tenantId, () -> {
            RedisUtils.deleteObject(CacheConstants.ONLINE_TOKEN_KEY + tokenValue);
        });

        log.info("user doLogout, userId:{}, token:{}***", loginId, tokenValue.substring(0, Math.min(8, tokenValue.length())));
    }

    /**
     * 用户被踢下线事件处理
     * 当用户在其他地方登录导致当前会话被踢出时触发
     * 清除用户在线状态缓存
     *
     * @param authType   认证类型
     * @param loginId    登录ID
     * @param tokenValue 令牌值
     */
    @Override
    public void doKickout(String authType, Object loginId, String tokenValue) {
        // 从令牌中获取租户ID
        String tenantId = Convert.toStr(StpUtil.getExtra(tokenValue, LoginHelper.TENANT_ID));

        // 使用租户工具进行隔离，删除在线用户缓存
        TenantHelper.dynamic(tenantId, () -> {
            RedisUtils.deleteObject(CacheConstants.ONLINE_TOKEN_KEY + tokenValue);
        });

        log.info("user doKickout, userId:{}, token:{}***", loginId, tokenValue.substring(0, Math.min(8, tokenValue.length())));
    }

    /**
     * 用户被顶下线事件处理
     * 当同一账号超过允许的同时在线数量时，早期登录的会话被顶下线
     * 清除用户在线状态缓存
     *
     * @param authType   认证类型
     * @param loginId    登录ID
     * @param tokenValue 令牌值
     */
    @Override
    public void doReplaced(String authType, Object loginId, String tokenValue) {
        // 从令牌中获取租户ID
        String tenantId = Convert.toStr(StpUtil.getExtra(tokenValue, LoginHelper.TENANT_ID));

        // 使用租户工具进行隔离，删除在线用户缓存
        TenantHelper.dynamic(tenantId, () -> {
            RedisUtils.deleteObject(CacheConstants.ONLINE_TOKEN_KEY + tokenValue);
        });

        log.info("user doReplaced, userId:{}, token:{}***", loginId, tokenValue.substring(0, Math.min(8, tokenValue.length())));
    }

    /**
     * 用户被封禁事件处理
     * 当用户账号被封禁时触发
     *
     * @param authType    认证类型
     * @param loginId     登录ID
     * @param service     封禁服务
     * @param level       封禁等级
     * @param disableTime 封禁时长
     */
    @Override
    public void doDisable(String authType, Object loginId, String service, int level, long disableTime) {
        // 此处可以添加封禁事件的处理逻辑
    }

    /**
     * 用户被解封事件处理
     * 当用户账号被解除封禁时触发
     *
     * @param authType 认证类型
     * @param loginId  登录ID
     * @param service  封禁服务
     */
    @Override
    public void doUntieDisable(String authType, Object loginId, String service) {
        // 此处可以添加解封事件的处理逻辑
    }

    /**
     * 二级认证开启事件处理
     * 当用户开启二级认证时触发
     *
     * @param authType   认证类型
     * @param tokenValue 令牌值
     * @param service    二级认证服务
     * @param safeTime   安全时间
     */
    @Override
    public void doOpenSafe(String authType, String tokenValue, String service, long safeTime) {
        // 此处可以添加二级认证开启事件的处理逻辑
    }

    /**
     * 二级认证关闭事件处理
     * 当用户关闭二级认证时触发
     *
     * @param authType   认证类型
     * @param tokenValue 令牌值
     * @param service    二级认证服务
     */
    @Override
    public void doCloseSafe(String authType, String tokenValue, String service) {
        // 此处可以添加二级认证关闭事件的处理逻辑
    }

    /**
     * 创建Session事件处理
     * 当创建新的会话时触发
     *
     * @param id 会话ID
     */
    @Override
    public void doCreateSession(String id) {
        // 此处可以添加会话创建事件的处理逻辑
    }

    /**
     * 注销Session事件处理
     * 当会话被注销时触发
     *
     * @param id 会话ID
     */
    @Override
    public void doLogoutSession(String id) {
        // 此处可以添加会话注销事件的处理逻辑
    }

    /**
     * 令牌续期事件处理
     * 当令牌被延长有效期时触发
     *
     * @param authType   认证类型
     * @param tokenValue 令牌值
     * @param loginId    登录ID
     * @param timeout    新的超时时间
     */
    @Override
    public void doRenewTimeout(String authType, Object loginId, String tokenValue, long timeout) {
        // 此处可以添加令牌续期事件的处理逻辑
    }
}
