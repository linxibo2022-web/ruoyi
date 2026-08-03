package plus.ruoyi.common.log.publisher;

import plus.ruoyi.common.core.enums.UserType;
import plus.ruoyi.common.core.utils.ServletUtils;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.log.event.LoginLogEvent;

/**
 * 登录日志事件发布器
 * <p>
 * 统一处理用户登录、登出、注册等操作的日志事件发布，
 * 避免在各个服务中重复编写日志发布逻辑
 * </p>
 *
 * @author 抓蛙师
 */
public class LoginLogPublisher {

    /**
     * 发布登录日志事件（完整参数版本）
     * <p>
     * 适用于需要指定设备类型的场景，如移动端登录、PC端登录等
     * </p>
     *
     * @param userName   用户名，用于标识操作用户
     * @param status     操作状态，如成功、失败等（来自 DictOperResult）
     * @param message    详细消息，描述具体的操作结果
     * @param tenantId   租户ID，多租户环境下的租户标识
     * @param userId     用户ID，可为null（如注册失败时）
     * @param deviceType 设备类型，如PC、APP等
     */
    public void publishLoginLog(String userName, String status, String message,
                                String tenantId, Long userId, String deviceType) {
        LoginLogEvent event = new LoginLogEvent();
        event.setUserName(userName);
        event.setStatus(status);
        event.setMessage(message);
        event.setTenantId(tenantId);
        event.setUserId(userId);
        event.setDeviceType(deviceType);
        event.setRequest(ServletUtils.getRequest());

        SpringUtils.context().publishEvent(event);
    }

    /**
     * 发布登录日志事件（默认PC设备类型）
     * <p>
     * 适用于PC端或默认设备类型的场景，简化调用方式
     * </p>
     *
     * @param userName 用户名，用于标识操作用户
     * @param status   操作状态，如成功、失败等（来自 DictOperResult）
     * @param message  详细消息，描述具体的操作结果
     * @param tenantId 租户ID，多租户环境下的租户标识
     * @param userId   用户ID，可为null（如注册失败时）
     */
    public void publishLoginLog(String userName, String status, String message,
                                String tenantId, Long userId) {
        publishLoginLog(userName, status, message, tenantId, userId,
            UserType.APP_USER.getDeviceType());
    }

    /**
     * 发布登录日志事件（最简版本）
     * <p>
     * 适用于简单场景，用户ID为null，设备类型为PC
     * 主要用于注册失败、验证码错误等场景
     * </p>
     *
     * @param userName 用户名，用于标识操作用户
     * @param status   操作状态，如成功、失败等（来自 DictOperResult）
     * @param message  详细消息，描述具体的操作结果
     * @param tenantId 租户ID，多租户环境下的租户标识
     */
    public void publishLoginLog(String userName, String status, String message,
                                String tenantId) {
        publishLoginLog(userName, status, message, tenantId, null);
    }
}
