package plus.ruoyi.common.rocketmq.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * RocketMQ 配置属性
 *
 * @author 路北
 * @date 2025-11-02
 */
@Data
@ConfigurationProperties(prefix = "rocketmq")
public class RocketMQProperties {

    /**
     * 是否启用 RocketMQ
     */
    private Boolean enabled = false;

    /**
     * NameServer 地址（多个用分号分隔）
     */
    private String nameServer = "127.0.0.1:9876";

    /**
     * 集群名称
     */
    private String clusterName = "RuoYiCluster";

    /**
     * Broker 地址（用于Topic管理）
     */
    private String brokerAddr = "127.0.0.1:10911";

    /**
     * 生产者配置
     */
    private Producer producer = new Producer();

    /**
     * 消费者配置
     */
    private Consumer consumer = new Consumer();

    @Data
    public static class Producer {
        /**
         * 生产者组名
         */
        private String group = "default-producer-group";

        /**
         * 发送消息超时时间（毫秒）
         */
        private Integer sendMsgTimeout = 3000;

        /**
         * 消息最大大小（字节）
         */
        private Integer maxMessageSize = 4194304; // 4MB

        /**
         * 发送失败重试次数
         */
        private Integer retryTimesWhenSendFailed = 2;

        /**
         * 异步发送失败重试次数
         */
        private Integer retryTimesWhenSendAsyncFailed = 2;

        /**
         * 是否自动创建 Topic
         * <p>为 true 时，发送消息前会自动检查并创建 Topic</p>
         */
        private Boolean autoCreateTopic = true;

        /**
         * 批量发送消息的最大数量
         */
        private Integer batchSize = 100;

        /**
         * 是否启用消息发送日志
         */
        private Boolean enableLog = true;
    }

    @Data
    public static class Consumer {
        /**
         * 消费者线程池最小线程数
         */
        private Integer consumeThreadMin = 20;

        /**
         * 消费者线程池最大线程数
         */
        private Integer consumeThreadMax = 64;

        /**
         * 消息拉取批次大小
         */
        private Integer pullBatchSize = 32;

        /**
         * 消费失败最大重试次数（-1 表示无限重试）
         */
        private Integer maxReconsumeTimes = 16;
    }
}
