package plus.ruoyi.common.sms.handler;

import cn.hutool.http.HttpStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.domain.R;
import org.dromara.sms4j.comm.exception.SmsBlendException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 短信异常处理器
 *
 * 全局捕获和处理短信相关异常，统一返回格式和错误信息
 *
 * @author AprilWind
 */
@Slf4j
@RestControllerAdvice
public class SmsExceptionHandler {

    /**
     * 处理短信混合异常
     *
     * 捕获SMS4J框架抛出的短信发送异常，记录错误日志并返回友好提示
     *
     * @param e       短信混合异常对象
     * @param request HTTP请求对象，用于获取请求信息
     * @return 统一响应结果，包含错误信息
     */
    @ExceptionHandler(SmsBlendException.class)
    public R<Void> handleSmsBlendException(SmsBlendException e, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        log.error("请求地址'{}',发生短信发送异常.", requestUri, e);
        return R.fail(HttpStatus.HTTP_INTERNAL_ERROR, "短信发送失败，请稍后再试...");
    }

}
