package plus.ruoyi.common.core.utils;

import org.slf4j.MDC;

/**
 * 请求ID工具类
 * 简单管理MDC中的请求ID，用于日志链路追踪
 *
 * @author Claude Code
 * @since 2025-09-25
 */
public class RequestIdUtils {

    /**
     * 请求ID在MDC中的键名
     */
    public static final String REQUEST_ID_KEY = "requestId";

    /**
     * 请求ID请求头名称
     */
    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    /**
     * 设置请求ID到MDC
     *
     * @param requestId 请求ID
     */
    public static void setRequestId(String requestId) {
        if (requestId != null && !requestId.trim().isEmpty()) {
            MDC.put(REQUEST_ID_KEY, requestId);
        }
    }

    /**
     * 获取当前请求ID
     *
     * @return 当前请求ID，如果不存在返回null
     */
    public static String getRequestId() {
        return MDC.get(REQUEST_ID_KEY);
    }

    /**
     * 清除请求ID
     */
    public static void clearRequestId() {
        MDC.remove(REQUEST_ID_KEY);
    }
}