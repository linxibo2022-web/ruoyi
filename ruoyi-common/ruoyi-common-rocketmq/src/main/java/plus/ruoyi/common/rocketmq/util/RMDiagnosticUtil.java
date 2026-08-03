package plus.ruoyi.common.rocketmq.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import plus.ruoyi.common.rocketmq.config.RocketMQProperties;

import java.util.List;

/**
 * RocketMQ 诊断工具（RocketMQ 5.x 兼容版本）
 * <p>
 * 用于排查 NameServer 连接问题
 *
 * @author 路北
 * @date 2025-11-03
 */
@Slf4j
public class RMDiagnosticUtil {

    /**
     * NameServer 地址（Spring 环境下自动从配置文件读取）
     */
    private static String nameServer = "127.0.0.1:9876";

    /**
     * 私有构造器，防止实例化
     */
    private RMDiagnosticUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * 初始化工具类
     * <p>由 RocketMQAutoConfiguration 在 Spring 容器启动时调用</p>
     * <p>将配置注入到静态字段中</p>
     *
     * @param properties RocketMQ 配置
     */
    public static void init(RocketMQProperties properties) {
        nameServer = properties.getNameServer();
        log.debug("RMDiagnosticUtil 初始化完成");
    }


    /**
     * 快速诊断 NameServer 连接状态
     * <p>通过创建临时 Producer 测试 NameServer 是否可连接</p>
     * <p>此方法仅测试网络连通性，不检查 Broker 注册状态</p>
     * <p>示例：{@code RocketMQDiagnosticUtil.quickDiagnose("127.0.0.1:9876")}</p>
     *
     * @param namesrvAddr NameServer 地址（格式：host:port，多个地址用分号分隔）
     */
    public static void quickDiagnose(String namesrvAddr) {
        log.info("🔍 正在测试 NameServer 连接: {}", namesrvAddr);

        DefaultMQProducer testProducer = new DefaultMQProducer("diagnostic-test-group");
        testProducer.setNamesrvAddr(namesrvAddr);
        testProducer.setInstanceName("diagnostic-" + System.currentTimeMillis());

        try {
            testProducer.start();
            log.info("✅ 成功连接到 NameServer: {}", namesrvAddr);
            log.info("💡 提示：Broker 注册需要约 10-30 秒，请耐心等待");

        } catch (Exception e) {
            log.error("❌ 无法连接到 NameServer: {}", namesrvAddr);
            log.error("   错误信息: {}", e.getMessage());
            log.error("请检查：");
            log.error("  1. RocketMQ Server 是否正常运行");
            log.error("  2. NameServer 地址是否正确");
            log.error("  3. 网络连接是否正常");
            log.error("  4. 防火墙是否阻止了 9876 端口");

        } finally {
            testProducer.shutdown();
        }
    }

    /**
     * 完整诊断 RocketMQ 连接状态
     * <p>执行完整的诊断流程，包括：NameServer 连接测试 + Broker 注册状态检查</p>
     * <p>推荐在遇到 "No route info" 错误时使用此方法进行排查</p>
     * <p>示例：{@code RocketMQDiagnosticUtil.diagnose("127.0.0.1:9876")}</p>
     */
    public static void diagnose() {
        log.info("========================================");
        log.info("🔍 开始诊断 RocketMQ 连接状态...");
        log.info("========================================");

        quickDiagnose(nameServer);

        log.info("========================================");
        log.info("🔍 检查 Broker 注册状态...");
        log.info("========================================");

        checkBrokerRegistration(nameServer);

        log.info("========================================");
        log.info("✅ 诊断完成！");
        log.info("========================================");
    }

    /**
     * 检查 Broker 是否已注册到 NameServer
     * <p>通过查询默认 Topic (TBW102) 的路由信息来检查 Broker 注册状态</p>
     * <p>如果 Broker 未注册，会输出详细的排查建议</p>
     * <p>示例：{@code RocketMQDiagnosticUtil.checkBrokerRegistration("127.0.0.1:9876")}</p>
     *
     * @param namesrvAddr NameServer 地址（格式：host:port，多个地址用分号分隔）
     */
    public static void checkBrokerRegistration(String namesrvAddr) {
        DefaultMQAdminExt adminExt = new DefaultMQAdminExt();
        adminExt.setNamesrvAddr(namesrvAddr);
        adminExt.setInstanceName("diagnostic-admin-" + System.currentTimeMillis());

        try {
            adminExt.start();
            log.info("🔍 查询已注册的 Broker...");

            // 尝试查询集群信息
            try {
                // 通过查询默认Topic的路由信息来检查Broker注册状态
                TopicRouteData routeData = adminExt.examineTopicRouteInfo("TBW102");

                if (routeData != null && routeData.getBrokerDatas() != null) {
                    List<BrokerData> brokerDatas = routeData.getBrokerDatas();

                    if (brokerDatas.isEmpty()) {
                        log.error("❌ 没有发现任何已注册的 Broker！");
                        log.error("   可能原因：");
                        log.error("   1. Broker 启动失败");
                        log.error("   2. Broker 配置的 NameServer 地址不正确");
                        log.error("   3. Broker 还在启动中，尚未完成注册");
                        log.error("   4. 默认topic (TBW102) 不存在,或者未开启自动创建topic配置:autoCreateTopicEnable");
                        log.error("   建议：检查 RocketMQ Server 的启动日志");
                    } else {
                        log.info("✅ 发现 {} 个已注册的 Broker", brokerDatas.size());
                        for (BrokerData brokerData : brokerDatas) {
                            log.info("  - Broker 名称: {}", brokerData.getBrokerName());
                            log.info("    集群名称: {}", brokerData.getCluster());
                            log.info("    Broker 地址: {}", brokerData.getBrokerAddrs());
                        }
                    }
                } else {
                    log.warn("⚠️ 无法获取 Broker 路由信息");
                }

            } catch (Exception e) {
                log.error("❌ 查询 Broker 注册状态失败");
                log.error("   错误信息: {}", e.getMessage());
                log.error("   可能原因：");
                log.error("   1. 默认Topic (TBW102) 不存在");
                log.error("   2. Broker 未正确注册到 NameServer");
                log.error("   3. NameServer 和 Broker 之间网络不通");
            }

        } catch (MQClientException e) {
            log.error("❌ 无法启动 Admin 客户端", e);
        } finally {
            adminExt.shutdown();
        }
    }
}
