package plus.ruoyi.common.redis.config;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.CompositeCodec;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.redis.config.properties.RedissonProperties;
import plus.ruoyi.common.redis.handler.KeyPrefixHandler;
import plus.ruoyi.common.redis.handler.RedisExceptionHandler;
import plus.ruoyi.common.redis.manager.PlusSpringCacheManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Redis 模块自动配置
 * <p>
 * 负责配置 Redis 相关组件，包括：
 * - Redisson 客户端配置（序列化、连接池等）
 * - 本地缓存配置（Caffeine）
 * - 缓存管理器配置（多级缓存）
 * - Redis 异常处理器
 * </p>
 *
 * @author Lion Li
 */
@Slf4j
@AutoConfiguration
@EnableCaching
@EnableConfigurationProperties(RedissonProperties.class)
public class RedisAutoConfiguration {

    // ==================== Redisson 配置 ====================

    /**
     * 自定义 Redisson 配置
     * <p>
     * 配置序列化方式、连接参数、线程池等核心参数
     * 支持单机和集群两种模式
     * </p>
     *
     * @param redissonProperties Redisson 配置属性
     * @return Redisson 配置定制器
     */
    @Bean
    public RedissonAutoConfigurationCustomizer redissonCustomizer(RedissonProperties redissonProperties) {
        return config -> {
            // 配置 LocalDateTime 的序列化格式
            JavaTimeModule javaTimeModule = new JavaTimeModule();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
            javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));

            // 配置 Jackson ObjectMapper
            ObjectMapper om = new ObjectMapper();
            om.registerModule(javaTimeModule);
            om.setTimeZone(TimeZone.getDefault());
            om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
            // 启用默认类型信息，序列化时保存对象类型
            om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);

            // 使用组合编解码器：key 用 String，value 用 JSON
            TypedJsonJacksonCodec jsonCodec = new TypedJsonJacksonCodec(Object.class, om);
            CompositeCodec codec = new CompositeCodec(StringCodec.INSTANCE, jsonCodec, jsonCodec);

            // 基础配置
            config.setThreads(redissonProperties.getThreads())
                .setNettyThreads(redissonProperties.getNettyThreads())
                // 缓存 Lua 脚本，减少网络传输（redisson 大部分功能都基于 Lua 脚本实现）
                .setUseScriptCache(true)
                .setCodec(codec);

            // 虚拟线程支持
            // netty 对虚拟线程适配存在缺陷，长时间使用会导致 redisson 卡死，故此暂时禁用
            // 参考上游修复：plus-admin/dev d259cac6a（2026-04-21）
            // if (SpringUtils.isVirtual()) {
            //     config.setNettyExecutor(new VirtualThreadTaskExecutor("redisson-"));
            // }

            // 单机模式配置
            RedissonProperties.SingleServerConfig singleServerConfig = redissonProperties.getSingleServerConfig();
            if (ObjectUtil.isNotNull(singleServerConfig)) {
                // 使用单机模式
                config.useSingleServer()
                    // 设置 redis key 前缀
                    .setNameMapper(new KeyPrefixHandler(redissonProperties.getKeyPrefix()))
                    .setTimeout(singleServerConfig.getTimeout())
                    .setClientName(singleServerConfig.getClientName())
                    .setIdleConnectionTimeout(singleServerConfig.getIdleConnectionTimeout())
                    .setSubscriptionConnectionPoolSize(singleServerConfig.getSubscriptionConnectionPoolSize())
                    .setConnectionMinimumIdleSize(singleServerConfig.getConnectionMinimumIdleSize())
                    .setConnectionPoolSize(singleServerConfig.getConnectionPoolSize());
            }

            // 集群模式配置
            RedissonProperties.ClusterServersConfig clusterServersConfig = redissonProperties.getClusterServersConfig();
            if (ObjectUtil.isNotNull(clusterServersConfig)) {
                config.useClusterServers()
                    // 设置 redis key 前缀
                    .setNameMapper(new KeyPrefixHandler(redissonProperties.getKeyPrefix()))
                    .setTimeout(clusterServersConfig.getTimeout())
                    .setClientName(clusterServersConfig.getClientName())
                    .setIdleConnectionTimeout(clusterServersConfig.getIdleConnectionTimeout())
                    .setSubscriptionConnectionPoolSize(clusterServersConfig.getSubscriptionConnectionPoolSize())
                    .setMasterConnectionMinimumIdleSize(clusterServersConfig.getMasterConnectionMinimumIdleSize())
                    .setMasterConnectionPoolSize(clusterServersConfig.getMasterConnectionPoolSize())
                    .setSlaveConnectionMinimumIdleSize(clusterServersConfig.getSlaveConnectionMinimumIdleSize())
                    .setSlaveConnectionPoolSize(clusterServersConfig.getSlaveConnectionPoolSize())
                    .setReadMode(clusterServersConfig.getReadMode())
                    .setSubscriptionMode(clusterServersConfig.getSubscriptionMode());
            }

            log.info("初始化 Redis 配置完成");
        };
    }

    /**
     * 注册 Redis 异常处理器
     * <p>
     * 统一处理 Redis 操作过程中的异常，提供友好的错误提示
     * </p>
     *
     * @return 异常处理器实例
     */
    @Bean
    public RedisExceptionHandler redisExceptionHandler() {
        return new RedisExceptionHandler();
    }

    // ==================== 缓存配置 ====================

    /**
     * 创建 Caffeine 本地缓存实例
     * <p>
     * 用于提供高性能的 JVM 内存缓存，减少对 Redis 等远程缓存的访问
     * 特性：
     * - 写入后 30 秒过期
     * - 初始容量 100 个
     * - 最大容量 1000 个
     * </p>
     *
     * @return 配置好的本地缓存实例
     */
    @Bean
    public Cache<Object, Object> caffeine() {
        log.info("创建 Caffeine 本地缓存");
        return Caffeine.newBuilder()
            // 写入或访问后 30 秒过期
            .expireAfterWrite(30, TimeUnit.SECONDS)
            // 初始容量 100 个条目
            .initialCapacity(100)
            // 最大容量 1000 个条目
            .maximumSize(1000)
            .build();
    }

    /**
     * 创建自定义缓存管理器
     * <p>
     * 整合 Spring Cache 抽象层，支持多级缓存策略
     * 优先使用本地 Caffeine 缓存，穿透后访问 Redis
     * </p>
     *
     * @return 自定义缓存管理器实例
     */
    @Bean
    public CacheManager cacheManager() {
        return new PlusSpringCacheManager();
    }

    /**
     * Redis集群配置示例
     *
     * <pre>
     * # Redis集群配置(单机与集群只能开启一个)
     * spring.data:
     *   redis:
     *     cluster:
     *       nodes:
     *         - 192.168.0.100:6379
     *         - 192.168.0.101:6379
     *         - 192.168.0.102:6379
     *     # 密码
     *     password:
     *     # 连接超时时间
     *     timeout: 10s
     *     # 是否开启ssl
     *     ssl.enabled: false
     *
     * redisson:
     *   # 线程池数量
     *   threads: 16
     *   # Netty线程池数量
     *   nettyThreads: 32
     *   # 集群配置
     *   clusterServersConfig:
     *     # 客户端名称
     *     clientName: ${app.id}
     *     # master最小空闲连接数
     *     masterConnectionMinimumIdleSize: 32
     *     # master连接池大小
     *     masterConnectionPoolSize: 64
     *     # slave最小空闲连接数
     *     slaveConnectionMinimumIdleSize: 32
     *     # slave连接池大小
     *     slaveConnectionPoolSize: 64
     *     # 连接空闲超时，单位：毫秒
     *     idleConnectionTimeout: 10000
     *     # 命令等待超时，单位：毫秒
     *     timeout: 3000
     *     # 发布和订阅连接池大小
     *     subscriptionConnectionPoolSize: 50
     *     # 读取模式
     *     readMode: "SLAVE"
     *     # 订阅模式
     *     subscriptionMode: "MASTER"
     */
}
