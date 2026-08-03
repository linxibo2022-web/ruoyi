package plus.ruoyi.common.web.interceptor;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.core.utils.RequestIdUtils;
import plus.ruoyi.common.json.utils.JsonUtils;
import plus.ruoyi.common.web.filter.RepeatedlyRequestWrapper;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Web请求耗时统计拦截器
 * 记录每个请求的执行时间和参数信息
 *
 * @author Lion Li
 * @since 3.3.0
 */
@Slf4j
public class PlusWebInvokeTimeInterceptor implements HandlerInterceptor {

    /**
     * 存储每个请求的计时器
     */
    private final static ThreadLocal<StopWatch> KEY_CACHE = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 提取请求ID并设置到MDC
        String requestId = request.getHeader(RequestIdUtils.REQUEST_ID_HEADER);
        RequestIdUtils.setRequestId(requestId);

        String url = request.getMethod() + " " + request.getRequestURI();

        // 根据请求类型打印不同格式的参数信息
        if (isJsonRequest(request)) {
            String jsonParam = "";
            // 如果是可重复读取的包装器，则读取JSON参数
            // 直接读 InputStream + UTF-8 解码，避免容器字符集判定不准导致日志中文乱码
            if (request instanceof RepeatedlyRequestWrapper) {
                try (InputStream is = request.getInputStream();
                     ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                    is.transferTo(bos);
                    jsonParam = bos.toString(StandardCharsets.UTF_8);
                }
            }
            log.info("[PLUS]开始请求 => URL[{}],参数类型[json],参数:[{}]", url, jsonParam);
        } else {
            // 处理表单参数
            Map<String, String[]> parameterMap = request.getParameterMap();
            if (MapUtil.isNotEmpty(parameterMap)) {
                String parameters = JsonUtils.toJsonString(parameterMap);
                log.info("[PLUS]开始请求 => URL[{}],参数类型[param],参数:[{}]", url, parameters);
            } else {
                log.info("[PLUS]开始请求 => URL[{}],无参数", url);
            }
        }

        // 启动计时器
        StopWatch stopWatch = new StopWatch();
        KEY_CACHE.set(stopWatch);
        stopWatch.start();

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 请求处理完成后的回调，暂无逻辑
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 停止计时并记录耗时
        StopWatch stopWatch = KEY_CACHE.get();
        if (ObjectUtil.isNotNull(stopWatch)) {
            stopWatch.stop();
            log.info("[PLUS]结束请求 => URL[{}],耗时:[{}]毫秒",
                request.getMethod() + " " + request.getRequestURI(),
                stopWatch.getDuration().toMillis());
            // 清理ThreadLocal避免内存泄漏
            KEY_CACHE.remove();
        }
        // 清理请求ID
        RequestIdUtils.clearRequestId();
    }

    /**
     * 判断请求是否为JSON类型
     *
     * @param request HTTP请求
     * @return true-JSON请求, false-其他类型
     */
    private boolean isJsonRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        if (contentType != null) {
            return StringUtils.startsWithIgnoreCase(contentType, MediaType.APPLICATION_JSON_VALUE);
        }
        return false;
    }
}
