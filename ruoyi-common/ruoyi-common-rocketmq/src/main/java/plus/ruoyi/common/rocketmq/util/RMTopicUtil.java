package plus.ruoyi.common.rocketmq.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.common.TopicConfig;
import plus.ruoyi.common.rocketmq.config.RocketMQProperties;

import java.util.Set;

/**
 * RocketMQ Topic 静态工具类
 * <p>
 * 用于手动创建、删除、查询Topic，完全独立于 Spring 容器，可在任何地方使用
 * </p>
 * <p>
 * 在 Spring 环境下会自动从配置文件读取配置，非 Spring 环境使用硬编码默认值
 * </p>
 *
 * @author 路北
 * @date 2025-11-03
 */
@Slf4j
public class RMTopicUtil {

    /**
     * NameServer 地址（Spring 环境下自动从配置文件读取）
     */
    private static String nameServer = "127.0.0.1:9876";

    /**
     * 集群名称（Spring 环境下自动从配置文件读取）
     */
    private static String clusterName = "RuoYiCluster";

    /**
     * Broker 地址（Spring 环境下自动从配置文件读取）
     */
    private static String brokerAddr = "127.0.0.1:10911";

    /**
     * 私有构造器，防止实例化
     */
    private RMTopicUtil() {
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
        clusterName = properties.getClusterName();
        brokerAddr = properties.getBrokerAddr();
        log.debug("RMTopicUtil 初始化完成");
    }

    /**
     * 创建Topic（如果不存在）- 完整参数版本
     * <p>如果Topic已存在则跳过创建，自动等待路由信息同步到NameServer</p>
     * <p>示例：{@code RMTopicUtil.createTopicIfNotExists("127.0.0.1:9876", "127.0.0.1:10911", "RuoYiCluster", "order-topic", 16)}</p>
     *
     * @param nameServer  NameServer地址
     * @param brokerAddr  Broker地址
     * @param clusterName 集群名称
     * @param topicName   Topic名称
     * @param queueNum    队列数量（读写队列数相同）
     */
    public static void createTopicIfNotExists(String nameServer, String brokerAddr, String clusterName,
                                              String topicName, int queueNum) {
        DefaultMQAdminExt adminExt = new DefaultMQAdminExt();
        adminExt.setNamesrvAddr(nameServer);
        adminExt.setInstanceName("admin-" + System.currentTimeMillis());

        try {
            adminExt.start();
            log.info("🔍 检查Topic是否存在: {}", topicName);

            // 检查Topic是否已存在
            if (topicExists(adminExt, topicName)) {
                log.info("✅ Topic已存在，跳过创建: {}", topicName);
                return;
            }

            // 创建Topic配置
            TopicConfig topicConfig = new TopicConfig();
            topicConfig.setTopicName(topicName);
            topicConfig.setReadQueueNums(queueNum);
            topicConfig.setWriteQueueNums(queueNum);
            topicConfig.setPerm(6);  // 读写权限
            topicConfig.setOrder(false);

            // 在Broker上创建Topic（使用配置中的Broker地址）
            log.info("📍 使用Broker地址: {}", brokerAddr);
            adminExt.createAndUpdateTopicConfig(brokerAddr, topicConfig);

            log.info("✅ Topic创建成功: {}", topicName);
            log.info("   - 读队列数: {}", queueNum);
            log.info("   - 写队列数: {}", queueNum);
            log.info("   - 集群名称: {}", clusterName);

            // 等待Topic路由信息同步到NameServer（增加等待时间）
            log.info("⏳ 等待路由信息同步到NameServer...");

            // 多次检查路由信息是否同步成功（最多等待30秒）
            boolean routeReady = false;
            for (int i = 0; i < 15; i++) {
                Thread.sleep(2000);
                try {
                    adminExt.examineTopicRouteInfo(topicName);
                    routeReady = true;
                    log.info("✅ 路由信息已同步到NameServer (耗时: {}秒)", (i + 1) * 2);
                    break;
                } catch (Exception e) {
                    log.debug("路由信息尚未同步，继续等待... ({}/15)", i + 1);
                }
            }

            if (!routeReady) {
                log.warn("⚠️ 路由信息同步超时，但Topic已创建成功");
                log.warn("   建议：等待30秒后再发送消息，或重启Broker服务");
            }

        } catch (MQClientException | RemotingException | MQBrokerException e) {
            log.error("❌ Topic创建失败: {}", topicName, e);
            throw new RuntimeException("Topic创建失败: " + topicName, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("❌ Topic创建被中断: {}", topicName, e);
            throw new RuntimeException("Topic创建被中断: " + topicName, e);
        } finally {
            adminExt.shutdown();
        }
    }

    /**
     * 检查Topic是否存在
     */
    private static boolean topicExists(DefaultMQAdminExt adminExt, String topicName) {
        try {
            // 尝试获取Topic路由信息
            adminExt.examineTopicRouteInfo(topicName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 创建Topic（使用配置文件的配置和默认8个队列）
     * <p>自动读取配置：rocketmq.name-server、rocketmq.broker-addr、rocketmq.cluster-name</p>
     * <p>示例：{@code RMTopicUtil.createTopic("order-topic")}</p>
     *
     * @param topicName Topic名称
     */
    public static void createTopic(String topicName) {
        createTopicIfNotExists(nameServer, brokerAddr, clusterName, topicName, 8);
    }

    /**
     * 创建Topic（使用配置文件的配置，指定队列数）
     * <p>自动读取配置：rocketmq.name-server、rocketmq.broker-addr、rocketmq.cluster-name</p>
     * <p>示例：{@code RMTopicUtil.createTopic("order-topic", 16)}</p>
     *
     * @param topicName Topic名称
     * @param queueNum  队列数量
     */
    public static void createTopic(String topicName, int queueNum) {
        createTopicIfNotExists(nameServer, brokerAddr, clusterName, topicName, queueNum);
    }

    /**
     * 删除Topic - 完整参数版本
     * <p>从指定集群中删除Topic，包括所有相关的路由信息</p>
     * <p>示例：{@code RMTopicUtil.deleteTopic("127.0.0.1:9876", "RuoYiCluster", "order-topic")}</p>
     *
     * @param nameServer  NameServer地址
     * @param clusterName 集群名称
     * @param topicName   Topic名称
     */
    public static void deleteTopic(String nameServer, String clusterName, String topicName) {
        DefaultMQAdminExt adminExt = new DefaultMQAdminExt();
        adminExt.setNamesrvAddr(nameServer);
        adminExt.setInstanceName("admin-" + System.currentTimeMillis());

        try {
            adminExt.start();
            log.info("🗑️  正在删除Topic: {}", topicName);
            log.info("📍 使用集群: {}", clusterName);

            adminExt.deleteTopic(topicName, clusterName);

            log.info("✅ Topic删除成功: {}", topicName);

        } catch (Exception e) {
            log.error("❌ Topic删除失败: {}", topicName, e);
            throw new RuntimeException("Topic删除失败: " + topicName, e);
        } finally {
            adminExt.shutdown();
        }
    }

    /**
     * 删除Topic（使用配置文件的配置）
     * <p>自动读取配置：rocketmq.name-server、rocketmq.cluster-name</p>
     * <p>示例：{@code RMTopicUtil.deleteTopic("order-topic")}</p>
     *
     * @param topicName Topic名称
     */
    public static void deleteTopic(String topicName) {
        deleteTopic(nameServer, clusterName, topicName);
    }

    /**
     * 查询所有Topic - 完整参数版本
     * <p>从NameServer获取所有已注册的Topic列表，包括系统Topic</p>
     * <p>示例：{@code Set<String> topics = RMTopicUtil.listTopics("127.0.0.1:9876")}</p>
     *
     * @param nameServer NameServer地址
     * @return Topic名称列表
     */
    public static Set<String> listTopics(String nameServer) {
        DefaultMQAdminExt adminExt = new DefaultMQAdminExt();
        adminExt.setNamesrvAddr(nameServer);
        adminExt.setInstanceName("admin-" + System.currentTimeMillis());

        try {
            adminExt.start();
            log.info("📋 查询Topic列表...");
            log.info("📍 使用NameServer: {}", nameServer);
            return adminExt.fetchAllTopicList().getTopicList();
        } catch (Exception e) {
            log.error("❌ 查询Topic列表失败", e);
            throw new RuntimeException("查询Topic列表失败", e);
        } finally {
            adminExt.shutdown();
        }
    }

    /**
     * 查询所有Topic（使用配置文件的配置）
     * <p>自动读取配置：rocketmq.name-server</p>
     * <p>示例：{@code Set<String> topics = RMTopicUtil.listTopics()}</p>
     *
     * @return Topic名称列表
     */
    public static Set<String> listTopics() {
        return listTopics(nameServer);
    }

    /**
     * 验证Topic路由信息是否可用 - 完整参数版本
     * <p>检查Topic是否已在NameServer中注册，路由信息是否可查询</p>
     * <p>示例：{@code boolean ok = RMTopicUtil.verifyTopicRoute("127.0.0.1:9876", "order-topic")}</p>
     *
     * @param nameServer NameServer地址
     * @param topicName  Topic名称
     * @return true表示路由信息可用，false表示不可用
     */
    public static boolean verifyTopicRoute(String nameServer, String topicName) {
        DefaultMQAdminExt adminExt = new DefaultMQAdminExt();
        adminExt.setNamesrvAddr(nameServer);
        adminExt.setInstanceName("admin-" + System.currentTimeMillis());

        try {
            adminExt.start();
            adminExt.examineTopicRouteInfo(topicName);
            log.info("✅ Topic路由信息验证成功: {}", topicName);
            log.info("📍 使用NameServer: {}", nameServer);
            return true;
        } catch (Exception e) {
            log.warn("❌ Topic路由信息不可用: {}", topicName);
            log.warn("   错误信息: {}", e.getMessage());
            return false;
        } finally {
            adminExt.shutdown();
        }
    }

    /**
     * 验证Topic路由信息是否可用（使用配置文件的配置）
     * <p>自动读取配置：rocketmq.name-server</p>
     * <p>示例：{@code boolean ok = RMTopicUtil.verifyTopicRoute("order-topic")}</p>
     *
     * @param topicName Topic名称
     * @return true表示路由信息可用，false表示不可用
     */
    public static boolean verifyTopicRoute(String topicName) {
        return verifyTopicRoute(nameServer, topicName);
    }
}
