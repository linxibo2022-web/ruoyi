package plus.ruoyi.common.mqtt;

import plus.ruoyi.common.test.base.BaseUnitTest;
import org.dromara.mica.mqtt.codec.MqttQoS;
import org.dromara.mica.mqtt.codec.MqttVersion;
import org.dromara.mica.mqtt.spring.client.config.MqttClientProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.util.unit.DataSize;
import org.tio.client.task.HeartbeatTimeoutStrategy;
import org.tio.core.task.HeartbeatMode;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MqttClientProperties 配置属性单元测试
 * <p>测试 mica-mqtt 官方配置类</p>
 * <p>✅ 无需 Spring 容器,纯 POJO 测试</p>
 *
 * @author 抓蛙师
 * @date 2025-11-24
 */
@DisplayName("MQTT 客户端配置属性测试")
public class MqttClientPropertiesTest extends BaseUnitTest {

    @Test
    @DisplayName("测试默认配置值")
    void testDefaultValues() {
        MqttClientProperties properties = new MqttClientProperties();

        // 默认值测试
        assertTrue(properties.isEnabled(), "默认应启用 MQTT 客户端");
        assertEquals("Mica-Mqtt-Client", properties.getName());
        assertEquals("127.0.0.1", properties.getIp());
        assertEquals(1883, properties.getPort());
        assertEquals(60, properties.getKeepAliveSecs());
        assertEquals(HeartbeatMode.LAST_REQ, properties.getHeartbeatMode());
        assertEquals(HeartbeatTimeoutStrategy.PING, properties.getHeartbeatTimeoutStrategy());
        assertTrue(properties.isReconnect());
        assertEquals(5000L, properties.getReInterval());
        assertEquals(0, properties.getRetryCount());
        assertEquals(20, properties.getReSubscribeBatchSize());
        assertEquals(MqttVersion.MQTT_5, properties.getVersion());
        assertTrue(properties.isCleanStart());
        assertEquals(0, properties.getSessionExpiryIntervalSecs());
        assertFalse(properties.isStatEnable());
        assertFalse(properties.isDebug());
        assertTrue(properties.isDisconnectBeforeStop());
    }

    @Test
    @DisplayName("测试自定义配置值")
    void testCustomValues() {
        MqttClientProperties properties = new MqttClientProperties();

        properties.setEnabled(false);
        properties.setName("Custom-Client");
        properties.setIp("192.168.1.100");
        properties.setPort(1884);
        properties.setUsername("test_user");
        properties.setPassword("test_pass");
        properties.setClientId("test-client-123");
        properties.setTimeout(10);
        properties.setKeepAliveSecs(120);
        properties.setReconnect(false);
        properties.setCleanStart(false);
        properties.setVersion(MqttVersion.MQTT_3_1_1);

        assertFalse(properties.isEnabled());
        assertEquals("Custom-Client", properties.getName());
        assertEquals("192.168.1.100", properties.getIp());
        assertEquals(1884, properties.getPort());
        assertEquals("test_user", properties.getUsername());
        assertEquals("test_pass", properties.getPassword());
        assertEquals("test-client-123", properties.getClientId());
        assertEquals(10, properties.getTimeout());
        assertEquals(120, properties.getKeepAliveSecs());
        assertFalse(properties.isReconnect());
        assertFalse(properties.isCleanStart());
        assertEquals(MqttVersion.MQTT_3_1_1, properties.getVersion());
    }

    @Test
    @DisplayName("测试遗嘱消息配置")
    void testWillConfig() {
        MqttClientProperties.WillMessage will = new MqttClientProperties.WillMessage();
        will.setTopic("/test/offline");
        will.setMessage("client offline");
        will.setQos(MqttQoS.QOS2);
        will.setRetain(true);

        assertEquals("/test/offline", will.getTopic());
        assertEquals("client offline", will.getMessage());
        assertEquals(MqttQoS.QOS2, will.getQos());
        assertTrue(will.isRetain());
    }

    @Test
    @DisplayName("测试 SSL 配置")
    void testSslConfig() {
        MqttClientProperties.Ssl ssl = new MqttClientProperties.Ssl();
        ssl.setEnabled(true);
        ssl.setKeystorePath("classpath:/certs/keystore.jks");
        ssl.setKeystorePass("keystore_password");
        ssl.setTruststorePath("classpath:/certs/truststore.jks");
        ssl.setTruststorePass("truststore_password");

        assertTrue(ssl.isEnabled());
        assertEquals("classpath:/certs/keystore.jks", ssl.getKeystorePath());
        assertEquals("keystore_password", ssl.getKeystorePass());
        assertEquals("classpath:/certs/truststore.jks", ssl.getTruststorePath());
        assertEquals("truststore_password", ssl.getTruststorePass());
    }

    @Test
    @DisplayName("测试心跳模式和超时策略")
    void testHeartbeatConfig() {
        MqttClientProperties properties = new MqttClientProperties();

        // 测试默认值
        assertEquals(HeartbeatMode.LAST_REQ, properties.getHeartbeatMode());
        assertEquals(HeartbeatTimeoutStrategy.PING, properties.getHeartbeatTimeoutStrategy());

        // 修改配置
        properties.setHeartbeatMode(HeartbeatMode.LAST_RESP);
        properties.setHeartbeatTimeoutStrategy(HeartbeatTimeoutStrategy.CLOSE);

        assertEquals(HeartbeatMode.LAST_RESP, properties.getHeartbeatMode());
        assertEquals(HeartbeatTimeoutStrategy.CLOSE, properties.getHeartbeatTimeoutStrategy());
    }

    @Test
    @DisplayName("测试数据大小配置")
    void testDataSizeConfig() {
        MqttClientProperties properties = new MqttClientProperties();

        // 测试默认值
        assertEquals(DataSize.ofBytes(8192), properties.getReadBufferSize());
        assertEquals(DataSize.ofBytes(10 * 1024 * 1024), properties.getMaxBytesInMessage());

        // 修改配置
        properties.setReadBufferSize(DataSize.ofKilobytes(16));
        properties.setMaxBytesInMessage(DataSize.ofMegabytes(20));

        assertEquals(DataSize.ofKilobytes(16), properties.getReadBufferSize());
        assertEquals(DataSize.ofMegabytes(20), properties.getMaxBytesInMessage());
    }
}
