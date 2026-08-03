package plus.ruoyi.common.mqtt.config;

import lombok.extern.slf4j.Slf4j;
import org.dromara.mica.mqtt.core.client.IMqttClientConnectListener;
import org.dromara.mica.mqtt.core.client.IMqttClientGlobalMessageListener;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import plus.ruoyi.common.mqtt.listener.DefaultMqttClientConnectListener;
import plus.ruoyi.common.mqtt.listener.DefaultMqttClientMessageListener;

/**
 * MQTT 自动配置类
 * <p>基于 mica-mqtt 2.5.7 版本，支持集群部署</p>
 * <p>配置由官方 mica-mqtt-client-spring-boot-starter 提供</p>
 * <p>
 * 📌 集群部署方案：
 * <ul>
 *   <li>✅ 使用共享订阅（$share/GroupName/Topic）实现集群负载均衡</li>
 *   <li>✅ 共享订阅由 MQTT Broker 原生支持，零代码、高性能</li>
 *   <li>✅ 支持 EMQX、Mosquitto 2.0+、HiveMQ 等主流 Broker</li>
 *   <li>💡 配置前缀: mqtt.client</li>
 * </ul>
 * </p>
 *
 * @author 抓蛙师
 * @date 2025-11-24
 */
@Slf4j
@AutoConfiguration
@ConditionalOnProperty(prefix = "mqtt.client", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MqttAutoConfiguration {

    /**
     * 构造函数：打印初始化信息
     */
    public MqttAutoConfiguration() {
        log.info("========================================");
        log.info("MQTT 模块初始化");
        log.info("使用 mica-mqtt 2.5.7 官方配置");
        log.info("集群部署推荐: 使用共享订阅 $share/GroupName/Topic");
        log.info("配置前缀: mqtt.client");
        log.info("========================================");
    }

    /**
     * 注册默认连接监听器
     * <p>用户可通过自定义 Bean 覆盖此默认实现</p>
     */
    @Bean
    @ConditionalOnMissingBean
    public IMqttClientConnectListener mqttClientConnectListener() {
        log.info("✅ 注册默认 MQTT 连接监听器");
        return new DefaultMqttClientConnectListener();
    }

    /**
     * 注册默认全局消息监听器
     * <p>用户可通过自定义 Bean 覆盖此默认实现</p>
     */
    @Bean
    @ConditionalOnMissingBean
    public IMqttClientGlobalMessageListener mqttClientGlobalMessageListener() {
        log.info("✅ 注册默认 MQTT 全局消息监听器");
        log.info("💡 集群部署推荐：使用共享订阅（$share/GroupName/Topic）");
        return new DefaultMqttClientMessageListener();
    }
}
