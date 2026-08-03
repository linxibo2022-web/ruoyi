package plus.ruoyi.common.mqtt;

import plus.ruoyi.common.test.base.BaseUnitTest;
import org.dromara.mica.mqtt.codec.MqttQoS;
import org.dromara.mica.mqtt.codec.MqttVersion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MQTT Codec 单元测试
 * <p>测试 mica-mqtt 2.5.7 的核心枚举和常量</p>
 * <p>✅ 无需 Spring 容器,启动速度极快</p>
 *
 * @author 抓蛙师
 * @date 2025-11-24
 */
@DisplayName("MQTT 编解码测试")
public class MqttCodecTest extends BaseUnitTest {

    @Test
    @DisplayName("测试 MqttQoS 枚举")
    void testMqttQoS() {
        // 测试 QoS 0
        assertEquals(0, MqttQoS.QOS0.value());

        // 测试 QoS 1
        assertEquals(1, MqttQoS.QOS1.value());

        // 测试 QoS 2
        assertEquals(2, MqttQoS.QOS2.value());

        // 测试 FAILURE
        assertEquals(0x80, MqttQoS.FAILURE.value());

        // 测试 valueOf
        assertEquals(MqttQoS.QOS0, MqttQoS.valueOf(0));
        assertEquals(MqttQoS.QOS1, MqttQoS.valueOf(1));
        assertEquals(MqttQoS.QOS2, MqttQoS.valueOf(2));
        assertEquals(MqttQoS.FAILURE, MqttQoS.valueOf(0x80));
    }

    @Test
    @DisplayName("测试 MqttVersion 枚举")
    void testMqttVersion() {
        // 测试 MQTT 3.1
        assertEquals("MQIsdp", MqttVersion.MQTT_3_1.protocolName());
        assertEquals((byte) 3, MqttVersion.MQTT_3_1.protocolLevel());

        // 测试 MQTT 3.1.1
        assertEquals("MQTT", MqttVersion.MQTT_3_1_1.protocolName());
        assertEquals((byte) 4, MqttVersion.MQTT_3_1_1.protocolLevel());

        // 测试 MQTT 5.0
        assertEquals("MQTT", MqttVersion.MQTT_5.protocolName());
        assertEquals((byte) 5, MqttVersion.MQTT_5.protocolLevel());
    }

    @Test
    @DisplayName("测试 MqttQoS valueOf 无效值")
    void testMqttQoSValueOfInvalid() {
        assertThrows(IllegalArgumentException.class, () -> MqttQoS.valueOf(-1));
        assertThrows(IllegalArgumentException.class, () -> MqttQoS.valueOf(3));
        assertThrows(IllegalArgumentException.class, () -> MqttQoS.valueOf(999));
    }

    @Test
    @DisplayName("测试 MqttVersion fromProtocolNameAndLevel")
    void testMqttVersionFromProtocolNameAndLevel() {
        assertEquals(MqttVersion.MQTT_3_1, MqttVersion.fromProtocolNameAndLevel("MQIsdp", (byte) 3));
        assertEquals(MqttVersion.MQTT_3_1_1, MqttVersion.fromProtocolNameAndLevel("MQTT", (byte) 4));
        assertEquals(MqttVersion.MQTT_5, MqttVersion.fromProtocolNameAndLevel("MQTT", (byte) 5));
    }

    @Test
    @DisplayName("测试 MqttVersion 协议名称和级别不匹配")
    void testMqttVersionMismatch() {
        assertThrows(Exception.class, () ->
            MqttVersion.fromProtocolNameAndLevel("WRONG", (byte) 3));
        assertThrows(Exception.class, () ->
            MqttVersion.fromProtocolNameAndLevel("MQIsdp", (byte) 4));
    }
}
