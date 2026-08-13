package plus.ruoyi.common.test.base;

import plus.ruoyi.common.test.TestApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Spring 测试基类 - 启动轻量级 Spring 上下文
 * <p>
 * 适用于需要依赖注入但不需要 Web 环境的测试:
 * <ul>
 *   <li>需要 @Autowired 注入 Bean 的测试</li>
 *   <li>需要 Spring 配置的测试</li>
 *   <li>集成测试 (但不测试 HTTP 接口)</li>
 * </ul>
 *
 * <p>性能优势:
 * <ul>
 *   <li>webEnvironment = NONE: 不启动 Web 容器 (Tomcat/Jetty)</li>
 *   <li>启动时间: 比完整 SpringBootTest 快 30%-50%</li>
 *   <li>资源占用: 减少 Web 容器相关资源</li>
 * </ul>
 *
 * <p>使用示例:
 * <pre>
 * // ✅ 直接继承,无需指定启动类
 * public class MqttClientUsageTest extends BaseSpringTest {
 *
 *     {@code @Autowired(required = false)}
 *     private MqttClientTemplate mqttClient;
 *
 *     {@code @Test}
 *     void testPublish() {
 *         if (mqttClient == null) {
 *             log.warn("⏭️ MQTT 未启用,跳过测试");
 *             return;
 *         }
 *         // 测试逻辑
 *     }
 * }
 * </pre>
 * <p>
 * ⚠️ 本基类激活 "test" profile（见 application-test.yml），其中配置了测试数据源与 Redis 连接，
 * 依赖 MySQL/Redis 等中间件（CI backend-test 已配套启动 mysql/redis service）。
 * 需要完整 dev 环境（Redis 等）的集成测试请使用 {@code @Tag("integration")} + {@code @ActiveProfiles("dev")}。
 * </p>
 *
 * @author 抓蛙师
 */
@ActiveProfiles("test")
@SpringBootTest(
    classes = TestApplication.class,  // ✅ 统一使用 test 模块的启动类
    webEnvironment = SpringBootTest.WebEnvironment.NONE  // 不启动 Web 容器
)
public abstract class BaseSpringTest extends BaseUnitTest {
    // 继承 BaseUnitTest 的性能监控能力
}
