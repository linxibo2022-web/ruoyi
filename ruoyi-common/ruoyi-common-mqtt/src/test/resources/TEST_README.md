# MQTT 模块测试说明

## 📖 测试位置说明

### ✅ 正确的测试位置

**MQTT 客户端使用示例测试** 应该放在 `ruoyi-admin` 模块中：

```
ruoyi-admin/
  └── src/test/java/plus/ruoyi/common/mqtt/
      └── MqttClientUsageTest.java  ✅ 正确位置
```

**原因**：
- MQTT 客户端依赖 Spring Boot 自动配置
- 测试需要完整的 Spring 上下文
- `ruoyi-admin` 模块包含 `@SpringBootApplication` 主类

**错误位置**（不要这样做）：
```
ruoyi-common/ruoyi-common-mqtt/
  └── src/test/java/  ❌ 不要在这里写集成测试
```

---

## 📋 测试类型

### 1. 使用示例测试 (推荐) ⭐

**位置**: `ruoyi-admin/src/test/java/plus/ruoyi/common/mqtt/MqttClientUsageTest.java`

**特点**:
- 包含 12 个完整使用示例
- 展示发布、订阅、QoS 等所有功能
- 演示最佳实践
- 可作为开发参考

**运行方式**:
```java
@SpringBootTest  // ✅ 自动找到 RuoyiPlus 主类
@DisplayName("MQTT 客户端使用示例")
class MqttClientUsageTest {
    @Autowired(required = false)
    private MqttClientTemplate mqttClientTemplate;

    @Test
    void example1_PublishSimpleMessage() {
        // 示例代码...
    }
}
```

### 2. 单元测试 (可选)

如果需要测试配置属性等纯逻辑功能，可以在 `ruoyi-common-mqtt` 模块编写：

```java
// ruoyi-common/ruoyi-common-mqtt/src/test/java/
@DisplayName("MQTT 配置属性测试")
class MqttPropertiesTest {
    @Test
    void testDefaultValues() {
        // 不依赖 Spring 上下文的纯逻辑测试
    }
}
```

---

## 🚀 运行测试前提

### 1. 启动外部 MQTT Broker

**选项1: EMQX (推荐)**
```bash
docker run -d --name emqx -p 1883:1883 -p 18083:18083 emqx/emqx:latest
```

访问管理界面: http://localhost:18083 (默认账号: admin / public)

**选项2: Mosquitto**
```bash
docker run -d --name mosquitto -p 1883:1883 eclipse-mosquitto:latest
```

**选项3: HiveMQ**
```bash
docker run -d --name hivemq -p 1883:1883 -p 8080:8080 hivemq/hivemq4
```

### 2. 配置 application-dev.yml

```yaml
mqtt:
  client:
    enabled: true  # ✅ 改为 true
    ip: 127.0.0.1
    port: 1883
```

### 3. 运行测试

**IDEA 中运行**:
- 右键 `MqttClientUsageTest.java` → Run
- 或运行单个测试方法

**Maven 命令**:
```bash
# 运行所有测试
mvn test -pl ruoyi-admin -Dtest=MqttClientUsageTest

# 运行单个测试方法
mvn test -pl ruoyi-admin -Dtest=MqttClientUsageTest#example1_PublishSimpleMessage
```

---

## 📚 测试示例说明

### 基础发送示例

```java
@Test
@DisplayName("示例1: 发送普通消息 (默认 QoS 0)")
void example1_PublishSimpleMessage() {
    String topic = "/test/simple";
    String message = "Hello MQTT!";
    boolean success = mqttClientTemplate.publish(topic, message.getBytes(StandardCharsets.UTF_8));
    log.info("✅ 发送消息: Topic={}, Message={}, Success={}", topic, message, success);
}

@Test
@DisplayName("示例2: 发送 JSON 数据 (QoS 1)")
void example2_PublishJsonWithQoS1() {
    String topic = "/device/001/data";
    String jsonData = "{\"deviceId\":\"001\",\"temperature\":25.5,\"humidity\":60}";
    boolean success = mqttClientTemplate.publish(
        topic,
        jsonData.getBytes(StandardCharsets.UTF_8),
        MqttQoS.QOS1
    );
}
```

### 订阅示例

**推荐方式：配置文件订阅**
```yaml
mqtt:
  client:
    global-subscribe:
      - topic: /device/+/status
        qos: QOS1
      - topic: /device/#
        qos: QOS1
```

**动态订阅（特殊场景）**:
```java
IMqttClientMessageListener listener = (context, topic, message, payload) -> {
    log.info("收到消息: {}", new String(payload, StandardCharsets.UTF_8));
};
mqttClientTemplate.subQos1("/device/+/status", listener);
```

---

## 🔧 手动测试工具

### 1. MQTTX (推荐)
- 下载: https://mqttx.app/
- 现代化界面，支持 MQTT 5.0
- 跨平台 (Windows/macOS/Linux)

### 2. MQTT.fx
- 下载: https://mqttfx.jensd.de/
- 经典桌面客户端

### 3. 在线工具
- EMQX 公共测试服务器: https://www.emqx.com/zh/mqtt/public-mqtt5-broker

---

## 📝 注意事项

### ✅ 推荐做法

1. **集成测试放在 ruoyi-admin 模块**
   - 拥有完整 Spring 上下文
   - 可以自动找到主配置类

2. **使用 @Autowired(required = false)**
   ```java
   @Autowired(required = false)  // ✅ MQTT 未启用时不报错
   private MqttClientTemplate mqttClientTemplate;
   ```

3. **测试前检查连接**
   ```java
   @BeforeEach
   void checkConnection() {
       if (mqttClientTemplate == null || !mqttClientTemplate.isConnected()) {
           log.info("⏭️ 跳过测试 - MQTT 未连接");
           return;
       }
   }
   ```

### ❌ 避免的做法

1. ❌ 不要在 `ruoyi-common-mqtt` 模块编写集成测试
2. ❌ 不要在测试中硬编码 Broker 地址
3. ❌ 不要在没有 Broker 的情况下强制运行测试

---

## 🎯 最佳实践

### 1. 测试结构

```
ruoyi-admin/src/test/java/plus/ruoyi/common/mqtt/
├── MqttClientUsageTest.java        # ✅ 使用示例 (12个示例)
├── MqttPerformanceTest.java        # 性能测试 (可选)
└── MqttIntegrationTest.java        # 集成测试 (可选)
```

### 2. 测试命名规范

```java
@DisplayName("示例1: 发送普通消息 (默认 QoS 0)")
void example1_PublishSimpleMessage() { }

@DisplayName("示例2: 发送 JSON 数据 (QoS 1)")
void example2_PublishJsonWithQoS1() { }
```

### 3. 日志输出

```java
log.info("✅ 发送消息: Topic={}, Message={}, Success={}", topic, message, success);
log.info("📤 QoS 1: 至少一次，可能重复，推荐使用");
log.info("💡 提示: 动态订阅需要提供监听器");
```

---

## 🆘 常见问题

### Q1: 测试报错 "Unable to find a @SpringBootConfiguration"

**原因**: 测试类不在正确的模块

**解决**: 将测试移动到 `ruoyi-admin/src/test/java/` 目录下

### Q2: MqttClientTemplate 注入失败

**原因**: MQTT 未启用或配置错误

**解决**:
```yaml
mqtt:
  client:
    enabled: true  # ✅ 确保为 true
```

### Q3: 连接 Broker 失败

**原因**: Broker 未启动或端口错误

**解决**:
```bash
# 检查 Broker 是否运行
docker ps | grep emqx

# 启动 Broker
docker run -d --name emqx -p 1883:1883 emqx/emqx:latest
```

### Q4: 订阅方法编译错误 "Expected 2 arguments but found 1"

**原因**: 使用了错误的方法签名

**解决**: 参考 `METHOD_SIGNATURES.md`，订阅方法需要提供监听器参数，推荐使用配置文件订阅

---

## 📖 相关文档

- **完整示例**: `ruoyi-admin/src/test/java/plus/ruoyi/common/mqtt/MqttClientUsageTest.java`
- **使用指南**: `USAGE.md`
- **方法签名**: `METHOD_SIGNATURES.md`
- **详细文档**: `README.md`
