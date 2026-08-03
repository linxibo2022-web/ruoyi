package plus.ruoyi.common.core.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 线程池配置属性
 * <p>
 * 用于从配置文件中读取自定义线程池的相关属性
 * 配置前缀: thread-pool
 *
 * @author Lion Li
 */
@Data
@ConfigurationProperties(prefix = "thread-pool")
public class ThreadPoolProperties {

    /**
     * 是否开启线程池
     * <p>
     * 可在配置文件中设置: thread-pool.enabled=true|false
     * 默认值取决于Spring配置
     */
    private boolean enabled;

    /**
     * 队列最大长度
     * <p>
     * 当核心线程数已满时，新任务会放入队列等待
     * 此值设置队列能够存储的最大任务数
     * 可在配置文件中设置: thread-pool.queue-capacity=xxx
     */
    private int queueCapacity;

    /**
     * 线程池维护线程所允许的空闲时间(秒)
     * <p>
     * 当线程空闲时间超过此值且线程数大于核心线程数时，会销毁多余线程
     * 可在配置文件中设置: thread-pool.keep-alive-seconds=xxx
     */
    private int keepAliveSeconds;

}
