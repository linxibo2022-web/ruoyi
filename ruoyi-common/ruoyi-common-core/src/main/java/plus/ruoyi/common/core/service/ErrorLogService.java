package plus.ruoyi.common.core.service;

import jakarta.servlet.http.HttpServletRequest;
import plus.ruoyi.common.core.domain.model.ErrorLogContext;

/**
 * 通用 错误日志服务
 *
 * @author Lion Li
 */
public interface ErrorLogService {

    /**
     * 构建错误日志上下文快照
     *
     * @param exception 异常对象
     * @param request   请求对象
     * @return 错误日志上下文快照
     */
    ErrorLogContext buildContext(Exception exception, HttpServletRequest request);

    /**
     * 异步记录错误日志
     *
     * @param context 错误日志上下文快照
     */
    void recordErrorAsync(ErrorLogContext context);
}
