package plus.ruoyi.common.redis.handler;

import cn.hutool.http.HttpStatus;
import com.baomidou.lock.exception.LockFailureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.domain.R;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Redis相关异常处理器
 * <p>
 * 统一处理Redis相关的异常，包括分布式锁获取失败等场景
 * 提供友好的错误响应和详细的日志记录
 *
 * @author AprilWind
 */
@Slf4j
@RestControllerAdvice
public class RedisExceptionHandler {

    /**
     * 处理分布式锁获取失败异常
     * <p>
     * 当使用Lock4j分布式锁时，如果锁获取失败会抛出LockFailureException
     * 通常发生在高并发场景下，多个请求同时竞争同一个锁资源
     *
     * @param e 分布式锁失败异常
     * @param request HTTP请求对象，用于获取请求路径信息
     * @return 统一响应对象，提示用户稍后重试
     */
    @ExceptionHandler(LockFailureException.class)
    public R<Void> handleLockFailureException(LockFailureException e, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        log.error("获取锁失败了'{}',发生Lock4j异常.", requestUri, e);
        return R.fail(HttpStatus.HTTP_UNAVAILABLE, "业务处理中，请稍后再试...");
    }

}
