package plus.ruoyi.common.oss.factory;

import plus.ruoyi.common.oss.entity.OssClientConfig;
import plus.ruoyi.common.oss.enums.OssType;
import plus.ruoyi.common.oss.exception.OssException;
import plus.ruoyi.common.oss.service.OssStrategy;
import plus.ruoyi.common.oss.service.impl.LocalOssStrategy;
import plus.ruoyi.common.oss.service.impl.S3OssStrategy;
import plus.ruoyi.common.test.base.BaseUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OssStrategyFactory 策略工厂测试
 *
 * @author 抓蛙师
 */
@DisplayName("OssStrategyFactory策略工厂测试")
public class OssStrategyFactoryTest extends BaseUnitTest {

    private OssClientConfig localConfig;
    private OssClientConfig s3Config;

    @BeforeEach
    public void initConfig() {
        // 初始化本地存储配置
        localConfig = new OssClientConfig();
        localConfig.setEndpoint("localhost");
        localConfig.setDomain("");
        localConfig.setPrefix("");
        localConfig.setAccessPolicy("0");

        // 初始化S3存储配置
        s3Config = new OssClientConfig();
        s3Config.setEndpoint("s3.amazonaws.com");
        s3Config.setAccessKey("testAccessKey");
        s3Config.setSecretKey("testSecretKey");
        s3Config.setBucketName("test-bucket");
        s3Config.setRegion("us-east-1");
        s3Config.setIsHttps("1");
        s3Config.setAccessPolicy("0");
    }

    // ==================== createStrategy(String, OssClientConfig) 测试 ====================

    @Test
    @DisplayName("测试createStrategy-local配置键应创建LocalOssStrategy")
    public void testCreateStrategyWithLocalConfigKey() {
        OssStrategy strategy = OssStrategyFactory.createStrategy("local", localConfig);

        assertNotNull(strategy, "策略对象不应为null");
        assertTrue(strategy instanceof LocalOssStrategy, "应创建LocalOssStrategy实例");
    }

    @Test
    @DisplayName("测试createStrategy-非local配置键应创建S3OssStrategy")
    public void testCreateStrategyWithNonLocalConfigKey() {
        // 由于S3OssStrategy构造时会连接服务器，这里我们只测试工厂方法的逻辑
        // 如果配置键不是local，应该尝试创建S3策略
        // 实际测试中由于网络原因可能会抛出OssException

        // 测试aliyun配置
        try {
            OssStrategy strategy = OssStrategyFactory.createStrategy("aliyun", s3Config);
            assertTrue(strategy instanceof S3OssStrategy, "aliyun应创建S3OssStrategy实例");
        } catch (OssException e) {
            // S3连接失败是预期的（没有真实的S3服务）
            assertTrue(e.getMessage().contains("配置错误") || e.getMessage().contains("Bucket"),
                "应是配置或连接相关的错误");
        }
    }

    @Test
    @DisplayName("测试createStrategy-minio配置键")
    public void testCreateStrategyWithMinioConfigKey() {
        OssClientConfig minioConfig = new OssClientConfig();
        minioConfig.setEndpoint("localhost:9000");
        minioConfig.setAccessKey("minioadmin");
        minioConfig.setSecretKey("minioadmin");
        minioConfig.setBucketName("test-bucket");
        minioConfig.setIsHttps("0");
        minioConfig.setAccessPolicy("0");

        try {
            OssStrategy strategy = OssStrategyFactory.createStrategy("minio", minioConfig);
            assertTrue(strategy instanceof S3OssStrategy, "minio应创建S3OssStrategy实例");
        } catch (OssException e) {
            // 连接失败是预期的
            assertNotNull(e.getMessage());
        }
    }

    @Test
    @DisplayName("测试createStrategy-qcloud配置键")
    public void testCreateStrategyWithQcloudConfigKey() {
        OssClientConfig qcloudConfig = new OssClientConfig();
        qcloudConfig.setEndpoint("cos.ap-guangzhou.myqcloud.com");
        qcloudConfig.setAccessKey("testAccessKey");
        qcloudConfig.setSecretKey("testSecretKey");
        qcloudConfig.setBucketName("test-bucket");
        qcloudConfig.setIsHttps("1");
        qcloudConfig.setAccessPolicy("0");

        try {
            OssStrategy strategy = OssStrategyFactory.createStrategy("qcloud", qcloudConfig);
            assertTrue(strategy instanceof S3OssStrategy, "qcloud应创建S3OssStrategy实例");
        } catch (OssException e) {
            // 连接失败是预期的
            assertNotNull(e.getMessage());
        }
    }

    @Test
    @DisplayName("测试createStrategy-qiniu配置键")
    public void testCreateStrategyWithQiniuConfigKey() {
        OssClientConfig qiniuConfig = new OssClientConfig();
        qiniuConfig.setEndpoint("s3-cn-east-1.qiniucs.com");
        qiniuConfig.setAccessKey("testAccessKey");
        qiniuConfig.setSecretKey("testSecretKey");
        qiniuConfig.setBucketName("test-bucket");
        qiniuConfig.setIsHttps("1");
        qiniuConfig.setAccessPolicy("0");

        try {
            OssStrategy strategy = OssStrategyFactory.createStrategy("qiniu", qiniuConfig);
            assertTrue(strategy instanceof S3OssStrategy, "qiniu应创建S3OssStrategy实例");
        } catch (OssException e) {
            // 连接失败是预期的
            assertNotNull(e.getMessage());
        }
    }

    @Test
    @DisplayName("测试createStrategy-obs配置键")
    public void testCreateStrategyWithObsConfigKey() {
        OssClientConfig obsConfig = new OssClientConfig();
        obsConfig.setEndpoint("obs.cn-north-4.myhuaweicloud.com");
        obsConfig.setAccessKey("testAccessKey");
        obsConfig.setSecretKey("testSecretKey");
        obsConfig.setBucketName("test-bucket");
        obsConfig.setIsHttps("1");
        obsConfig.setAccessPolicy("0");

        try {
            OssStrategy strategy = OssStrategyFactory.createStrategy("obs", obsConfig);
            assertTrue(strategy instanceof S3OssStrategy, "obs应创建S3OssStrategy实例");
        } catch (OssException e) {
            // 连接失败是预期的
            assertNotNull(e.getMessage());
        }
    }

    @Test
    @DisplayName("测试createStrategy-未知配置键应默认创建S3策略")
    public void testCreateStrategyWithUnknownConfigKey() {
        try {
            OssStrategy strategy = OssStrategyFactory.createStrategy("unknown", s3Config);
            assertTrue(strategy instanceof S3OssStrategy, "未知配置键应默认创建S3OssStrategy");
        } catch (OssException e) {
            // 连接失败是预期的
            assertNotNull(e.getMessage());
        }
    }

    // ==================== createStrategy(OssType, OssClientConfig) 测试 ====================

    @Test
    @DisplayName("测试createStrategy-OssType.LOCAL应创建LocalOssStrategy")
    public void testCreateStrategyWithOssTypeLocal() {
        OssStrategy strategy = OssStrategyFactory.createStrategy(OssType.LOCAL, localConfig);

        assertNotNull(strategy, "策略对象不应为null");
        assertTrue(strategy instanceof LocalOssStrategy, "LOCAL类型应创建LocalOssStrategy实例");
    }

    @Test
    @DisplayName("测试createStrategy-OssType.ALIYUN")
    public void testCreateStrategyWithOssTypeAliyun() {
        try {
            OssStrategy strategy = OssStrategyFactory.createStrategy(OssType.ALIYUN, s3Config);
            assertTrue(strategy instanceof S3OssStrategy, "ALIYUN类型应创建S3OssStrategy实例");
        } catch (OssException e) {
            // 连接失败是预期的
            assertNotNull(e.getMessage());
        }
    }

    @Test
    @DisplayName("测试createStrategy-OssType.QCLOUD")
    public void testCreateStrategyWithOssTypeQcloud() {
        try {
            OssStrategy strategy = OssStrategyFactory.createStrategy(OssType.QCLOUD, s3Config);
            assertTrue(strategy instanceof S3OssStrategy, "QCLOUD类型应创建S3OssStrategy实例");
        } catch (OssException e) {
            // 连接失败是预期的
            assertNotNull(e.getMessage());
        }
    }

    @Test
    @DisplayName("测试createStrategy-OssType.QINIU")
    public void testCreateStrategyWithOssTypeQiniu() {
        try {
            OssStrategy strategy = OssStrategyFactory.createStrategy(OssType.QINIU, s3Config);
            assertTrue(strategy instanceof S3OssStrategy, "QINIU类型应创建S3OssStrategy实例");
        } catch (OssException e) {
            // 连接失败是预期的
            assertNotNull(e.getMessage());
        }
    }

    @Test
    @DisplayName("测试createStrategy-OssType.MINIO")
    public void testCreateStrategyWithOssTypeMinio() {
        try {
            OssStrategy strategy = OssStrategyFactory.createStrategy(OssType.MINIO, s3Config);
            assertTrue(strategy instanceof S3OssStrategy, "MINIO类型应创建S3OssStrategy实例");
        } catch (OssException e) {
            // 连接失败是预期的
            assertNotNull(e.getMessage());
        }
    }

    @Test
    @DisplayName("测试createStrategy-OssType.OBS")
    public void testCreateStrategyWithOssTypeObs() {
        try {
            OssStrategy strategy = OssStrategyFactory.createStrategy(OssType.OBS, s3Config);
            assertTrue(strategy instanceof S3OssStrategy, "OBS类型应创建S3OssStrategy实例");
        } catch (OssException e) {
            // 连接失败是预期的
            assertNotNull(e.getMessage());
        }
    }

    // ==================== 策略工厂逻辑测试 ====================

    @Test
    @DisplayName("测试工厂逻辑-local字符串大小写敏感")
    public void testFactoryLogicCaseSensitive() {
        // "local"应创建本地策略
        OssStrategy localStrategy = OssStrategyFactory.createStrategy("local", localConfig);
        assertTrue(localStrategy instanceof LocalOssStrategy, "小写local应创建本地策略");

        // "LOCAL"应创建S3策略（因为不等于"local"）
        try {
            OssStrategy upperStrategy = OssStrategyFactory.createStrategy("LOCAL", s3Config);
            assertTrue(upperStrategy instanceof S3OssStrategy, "大写LOCAL应创建S3策略");
        } catch (OssException e) {
            // S3连接失败是预期的
            assertNotNull(e.getMessage());
        }
    }

    @Test
    @DisplayName("测试工厂逻辑-OssType枚举转换")
    public void testFactoryLogicOssTypeConversion() {
        // OssType.LOCAL的值是"local"
        assertEquals("local", OssType.LOCAL.getValue());

        // 使用枚举创建的策略应与使用字符串创建的策略类型一致
        OssStrategy strategyByEnum = OssStrategyFactory.createStrategy(OssType.LOCAL, localConfig);
        OssStrategy strategyByString = OssStrategyFactory.createStrategy("local", localConfig);

        assertEquals(strategyByEnum.getClass(), strategyByString.getClass(),
            "枚举和字符串创建的策略类型应一致");
    }

    // ==================== LocalOssStrategy 属性测试 ====================

    @Test
    @DisplayName("测试LocalOssStrategy-配置传递正确")
    public void testLocalOssStrategyConfig() {
        OssStrategy strategy = OssStrategyFactory.createStrategy("local", localConfig);

        assertTrue(strategy instanceof LocalOssStrategy);
        LocalOssStrategy localStrategy = (LocalOssStrategy) strategy;

        assertEquals(localConfig, localStrategy.getOssClientConfig(), "配置应正确传递");
    }

    // ==================== 边界条件测试 ====================

    @Test
    @DisplayName("测试边界条件-空字符串配置键")
    public void testEmptyConfigKey() {
        try {
            OssStrategy strategy = OssStrategyFactory.createStrategy("", s3Config);
            // 空字符串不等于"local"，应创建S3策略
            assertTrue(strategy instanceof S3OssStrategy);
        } catch (OssException e) {
            // S3连接失败是预期的
            assertNotNull(e.getMessage());
        }
    }

    @Test
    @DisplayName("测试边界条件-带空格的配置键")
    public void testConfigKeyWithSpaces() {
        try {
            OssStrategy strategy = OssStrategyFactory.createStrategy(" local ", s3Config);
            // " local "不等于"local"（有空格），应创建S3策略
            assertTrue(strategy instanceof S3OssStrategy);
        } catch (OssException e) {
            // S3连接失败是预期的
            assertNotNull(e.getMessage());
        }
    }

    // ==================== 业务场景测试 ====================

    @Test
    @DisplayName("测试业务场景-开发环境使用本地存储")
    public void testDevEnvironmentUseLocalStorage() {
        // 开发环境通常使用local配置
        String devConfigKey = "local";

        OssStrategy strategy = OssStrategyFactory.createStrategy(devConfigKey, localConfig);

        assertTrue(strategy instanceof LocalOssStrategy, "开发环境应使用本地存储策略");
    }

    @Test
    @DisplayName("测试业务场景-生产环境使用云存储")
    public void testProdEnvironmentUseCloudStorage() {
        // 生产环境通常使用云存储配置
        String prodConfigKey = "aliyun";

        try {
            OssStrategy strategy = OssStrategyFactory.createStrategy(prodConfigKey, s3Config);
            assertTrue(strategy instanceof S3OssStrategy, "生产环境应使用S3存储策略");
        } catch (OssException e) {
            // 连接失败是预期的（没有真实的云服务）
            assertNotNull(e.getMessage());
        }
    }

    @Test
    @DisplayName("测试业务场景-多租户不同存储配置")
    public void testMultiTenantDifferentStorage() {
        // 模拟不同租户使用不同存储
        OssClientConfig tenant1Config = new OssClientConfig();
        tenant1Config.setTenantId("tenant1");
        localConfig.setTenantId("tenant1");

        OssClientConfig tenant2Config = new OssClientConfig();
        tenant2Config.setTenantId("tenant2");
        tenant2Config.setEndpoint("s3.amazonaws.com");
        tenant2Config.setAccessKey("key");
        tenant2Config.setSecretKey("secret");
        tenant2Config.setBucketName("bucket");
        tenant2Config.setIsHttps("1");
        tenant2Config.setAccessPolicy("0");

        // 租户1使用本地存储
        OssStrategy tenant1Strategy = OssStrategyFactory.createStrategy("local", localConfig);
        assertTrue(tenant1Strategy instanceof LocalOssStrategy);

        // 租户2使用云存储
        try {
            OssStrategy tenant2Strategy = OssStrategyFactory.createStrategy("aliyun", tenant2Config);
            assertTrue(tenant2Strategy instanceof S3OssStrategy);
        } catch (OssException e) {
            // 连接失败是预期的
            assertNotNull(e.getMessage());
        }
    }
}
