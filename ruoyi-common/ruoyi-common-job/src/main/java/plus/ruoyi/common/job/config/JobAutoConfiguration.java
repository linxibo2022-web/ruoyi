package plus.ruoyi.common.job.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.aizuda.snailjob.client.common.appender.SnailLogbackAppender;
import com.aizuda.snailjob.client.common.event.SnailClientStartingEvent;
import com.aizuda.snailjob.client.starter.EnableSnailJob;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * SnailJob 定时任务自动配置类
 * <p>
 * 集成 SnailJob 分布式任务调度框架，提供以下功能：
 * <ul>
 * <li>启用 Spring 定时任务支持</li>
 * <li>启用 SnailJob 客户端</li>
 * <li>配置日志收集器，用于任务执行日志的统一管理</li>
 * </ul>
 * <p>
 * 配置启用条件：需要在配置文件中设置 snail-job.enabled=true
 *
 * @author opensnail
 * @date 2024-05-17
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "snail-job", name = "enabled", havingValue = "true")
@EnableScheduling
@EnableSnailJob
public class JobAutoConfiguration {

    /**
     * SnailJob 客户端启动事件监听器
     * <p>
     * 在 SnailJob 客户端启动时自动配置日志收集器，
     * 将应用的日志输出发送到 SnailJob 服务端进行统一管理和查看
     *
     * @param event SnailJob 客户端启动事件
     */
    @EventListener(SnailClientStartingEvent.class)
    public void onStarting(SnailClientStartingEvent event) {
        // 获取 Logback 日志上下文
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

        // 创建 SnailJob 专用的日志追加器
        SnailLogbackAppender<ILoggingEvent> ca = new SnailLogbackAppender<>();
        ca.setName("snail_log_appender");
        ca.start();

        // 将日志追加器添加到根日志记录器，实现日志统一收集
        Logger rootLogger = lc.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(ca);
    }

}
