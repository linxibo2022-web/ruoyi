package plus.ruoyi.common.rocketmq.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import plus.ruoyi.common.rocketmq.util.RMDiagnosticUtil;
import plus.ruoyi.common.rocketmq.util.RMSendUtil;
import plus.ruoyi.common.rocketmq.util.RMTopicUtil;

/**
 * RocketMQ 自动配置类
 * <p>
 * 集成 RocketMQ 消息队列框架，提供以下功能：
 * <ul>
 * <li>自动配置生产者和消费者</li>
 * <li>提供 RocketMQTemplate 工具类</li>
 * <li>支持注解式消费者 @RocketMQMessageListener</li>
 * <li>自动初始化静态工具类（RMSendUtil、RMTopicUtil、RMDiagnosticUtil）</li>
 * </ul>
 * <p>
 * 配置启用条件：需要在配置文件中设置 rocketmq.enabled=true
 * <p>
 * ⚠️ 注意：RocketMQ Spring 2.3.0+ 官方 starter 已包含自动配置
 *
 * @author 路北
 * @date 2025-11-02
 */
@Slf4j
@AutoConfiguration
@ConditionalOnProperty(prefix = "rocketmq", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(RocketMQProperties.class)
public class RocketMQAutoConfiguration {

    private final RocketMQProperties properties;

    /**
     * 构造函数：打印 RocketMQ 配置信息
     * <p>在配置类实例化时执行，打印配置参数，方便排查配置问题</p>
     *
     * @param properties RocketMQ 配置属性对象，由 Spring 自动注入
     */
    public RocketMQAutoConfiguration(RocketMQProperties properties) {
        this.properties = properties;
        log.info("========================================");
        log.info("🚀 RocketMQ 模块开始初始化");
        log.info("  - NameServer: {}", properties.getNameServer());
        log.info("  - 集群名称: {}", properties.getClusterName());
        log.info("  - Broker地址: {}", properties.getBrokerAddr());
        log.info("  - 生产者组: {}", properties.getProducer().getGroup());
        log.info("  - 发送超时: {}ms", properties.getProducer().getSendMsgTimeout());
        log.info("  - 自动创建Topic: {}", properties.getProducer().getAutoCreateTopic());
        log.info("  - 消费线程: {}-{}",
            properties.getConsumer().getConsumeThreadMin(),
            properties.getConsumer().getConsumeThreadMax());
        log.info("========================================");
    }

    /**
     * 初始化 RMSendUtil 工具类
     * <p>将 RocketMQTemplate 和配置注入到静态工具类中</p>
     */
    @Bean
    public Object rmSendUtilInitializer(RocketMQTemplate rocketMQTemplate) {
        RMSendUtil.init(rocketMQTemplate, properties);
        return new Object();
    }

    /**
     * 初始化 RMTopicUtil 工具类
     * <p>将配置注入到静态工具类中</p>
     */
    @Bean
    public Object rmTopicUtilInitializer() {
        RMTopicUtil.init(properties);
        return new Object();
    }

    /**
     * 初始化 RMDiagnosticUtil 工具类
     * <p>将配置注入到静态工具类中</p>
     */
    @Bean
    public Object rmDiagnosticUtilInitializer() {
        RMDiagnosticUtil.init(properties);
        return new Object();
    }

    /**
     * RocketMQ 启动完成日志 + 自动诊断
     * <p>依赖所有初始化器完成后再执行</p>
     * <p>打印启动完成信息并自动诊断 Broker 连接状态</p>
     */
    @Bean
    public Object rocketMQStartupLogger() {
        log.info("========================================");
        log.info("✅ RocketMQ 客户端启动完成！");
        log.info("========================================");

        // 自动诊断 Broker 注册状态
        try {
            RMDiagnosticUtil.diagnose();
        } catch (Exception e) {
            log.warn("⚠️ Broker 状态检查失败: {}", e.getMessage());
        }

        return new Object();
    }
}
