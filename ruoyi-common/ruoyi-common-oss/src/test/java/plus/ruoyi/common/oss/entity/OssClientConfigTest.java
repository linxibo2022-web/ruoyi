package plus.ruoyi.common.oss.entity;

import plus.ruoyi.common.test.base.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OssClientConfig 客户端配置类测试
 *
 * @author 抓蛙师
 */
@DisplayName("OssClientConfig客户端配置类测试")
public class OssClientConfigTest extends BaseUnitTest {

    // ==================== 基础Setter/Getter测试 ====================

    @Test
    @DisplayName("测试Setter/Getter-tenantId")
    public void testSetGetTenantId() {
        OssClientConfig config = new OssClientConfig();
        config.setTenantId("tenant123");

        assertEquals("tenant123", config.getTenantId());
    }

    @Test
    @DisplayName("测试Setter/Getter-endpoint")
    public void testSetGetEndpoint() {
        OssClientConfig config = new OssClientConfig();
        config.setEndpoint("s3.amazonaws.com");

        assertEquals("s3.amazonaws.com", config.getEndpoint());
    }

    @Test
    @DisplayName("测试Setter/Getter-domain")
    public void testSetGetDomain() {
        OssClientConfig config = new OssClientConfig();
        config.setDomain("cdn.example.com");

        assertEquals("cdn.example.com", config.getDomain());
    }

    @Test
    @DisplayName("测试Setter/Getter-prefix")
    public void testSetGetPrefix() {
        OssClientConfig config = new OssClientConfig();
        config.setPrefix("avatar");

        assertEquals("avatar", config.getPrefix());
    }

    @Test
    @DisplayName("测试Setter/Getter-accessKey")
    public void testSetGetAccessKey() {
        OssClientConfig config = new OssClientConfig();
        config.setAccessKey("AKIAIOSFODNN7EXAMPLE");

        assertEquals("AKIAIOSFODNN7EXAMPLE", config.getAccessKey());
    }

    @Test
    @DisplayName("测试Setter/Getter-secretKey")
    public void testSetGetSecretKey() {
        OssClientConfig config = new OssClientConfig();
        config.setSecretKey("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");

        assertEquals("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY", config.getSecretKey());
    }

    @Test
    @DisplayName("测试Setter/Getter-bucketName")
    public void testSetGetBucketName() {
        OssClientConfig config = new OssClientConfig();
        config.setBucketName("my-test-bucket");

        assertEquals("my-test-bucket", config.getBucketName());
    }

    @Test
    @DisplayName("测试Setter/Getter-region")
    public void testSetGetRegion() {
        OssClientConfig config = new OssClientConfig();
        config.setRegion("us-east-1");

        assertEquals("us-east-1", config.getRegion());
    }

    @Test
    @DisplayName("测试Setter/Getter-isHttps")
    public void testSetGetIsHttps() {
        OssClientConfig config = new OssClientConfig();

        config.setIsHttps("1");
        assertEquals("1", config.getIsHttps());

        config.setIsHttps("0");
        assertEquals("0", config.getIsHttps());
    }

    @Test
    @DisplayName("测试Setter/Getter-accessPolicy")
    public void testSetGetAccessPolicy() {
        OssClientConfig config = new OssClientConfig();

        config.setAccessPolicy("0");
        assertEquals("0", config.getAccessPolicy());

        config.setAccessPolicy("1");
        assertEquals("1", config.getAccessPolicy());

        config.setAccessPolicy("2");
        assertEquals("2", config.getAccessPolicy());
    }

    // ==================== 完整配置测试 ====================

    @Test
    @DisplayName("测试完整配置-阿里云OSS")
    public void testAliyunOssConfig() {
        OssClientConfig config = new OssClientConfig();
        config.setTenantId("tenant001");
        config.setEndpoint("oss-cn-hangzhou.aliyuncs.com");
        config.setDomain("oss.example.com");
        config.setPrefix("upload");
        config.setAccessKey("LTAI5tTestAccessKey");
        config.setSecretKey("TestSecretKeyValue");
        config.setBucketName("my-aliyun-bucket");
        config.setRegion("cn-hangzhou");
        config.setIsHttps("1");
        config.setAccessPolicy("2");

        assertEquals("tenant001", config.getTenantId());
        assertEquals("oss-cn-hangzhou.aliyuncs.com", config.getEndpoint());
        assertEquals("oss.example.com", config.getDomain());
        assertEquals("upload", config.getPrefix());
        assertEquals("LTAI5tTestAccessKey", config.getAccessKey());
        assertEquals("TestSecretKeyValue", config.getSecretKey());
        assertEquals("my-aliyun-bucket", config.getBucketName());
        assertEquals("cn-hangzhou", config.getRegion());
        assertEquals("1", config.getIsHttps());
        assertEquals("2", config.getAccessPolicy());
    }

    @Test
    @DisplayName("测试完整配置-腾讯云COS")
    public void testQcloudCosConfig() {
        OssClientConfig config = new OssClientConfig();
        config.setEndpoint("cos.ap-guangzhou.myqcloud.com");
        config.setAccessKey("AKIDTestSecretId");
        config.setSecretKey("TestSecretKey");
        config.setBucketName("my-qcloud-bucket-1250000000");
        config.setRegion("ap-guangzhou");
        config.setIsHttps("1");
        config.setAccessPolicy("0");

        assertEquals("cos.ap-guangzhou.myqcloud.com", config.getEndpoint());
        assertTrue(config.getBucketName().contains("-1250000000"), "腾讯云bucket应包含APPID");
    }

    @Test
    @DisplayName("测试完整配置-MinIO")
    public void testMinioConfig() {
        OssClientConfig config = new OssClientConfig();
        config.setEndpoint("minio.example.com:9000");
        config.setAccessKey("minioadmin");
        config.setSecretKey("minioadmin");
        config.setBucketName("test-bucket");
        config.setIsHttps("0");
        config.setAccessPolicy("1");

        assertEquals("minio.example.com:9000", config.getEndpoint());
        assertEquals("minioadmin", config.getAccessKey());
        assertEquals("0", config.getIsHttps(), "MinIO本地通常使用HTTP");
    }

    @Test
    @DisplayName("测试完整配置-本地存储")
    public void testLocalStorageConfig() {
        OssClientConfig config = new OssClientConfig();
        config.setEndpoint("localhost");
        config.setDomain("");
        config.setPrefix("");
        config.setAccessPolicy("0");

        assertEquals("localhost", config.getEndpoint());
        assertEquals("", config.getDomain());
        assertEquals("", config.getPrefix());
    }

    // ==================== 边界值测试 ====================

    @Test
    @DisplayName("测试边界值-所有字段为null")
    public void testAllFieldsNull() {
        OssClientConfig config = new OssClientConfig();

        assertNull(config.getTenantId());
        assertNull(config.getEndpoint());
        assertNull(config.getDomain());
        assertNull(config.getPrefix());
        assertNull(config.getAccessKey());
        assertNull(config.getSecretKey());
        assertNull(config.getBucketName());
        assertNull(config.getRegion());
        assertNull(config.getIsHttps());
        assertNull(config.getAccessPolicy());
    }

    @Test
    @DisplayName("测试边界值-空字符串")
    public void testEmptyStrings() {
        OssClientConfig config = new OssClientConfig();
        config.setTenantId("");
        config.setEndpoint("");
        config.setDomain("");
        config.setPrefix("");
        config.setAccessKey("");
        config.setSecretKey("");
        config.setBucketName("");
        config.setRegion("");

        assertEquals("", config.getTenantId());
        assertEquals("", config.getEndpoint());
        assertEquals("", config.getDomain());
        assertEquals("", config.getPrefix());
        assertEquals("", config.getAccessKey());
        assertEquals("", config.getSecretKey());
        assertEquals("", config.getBucketName());
        assertEquals("", config.getRegion());
    }

    @Test
    @DisplayName("测试边界值-特殊字符")
    public void testSpecialCharacters() {
        OssClientConfig config = new OssClientConfig();
        config.setPrefix("upload/2024/images");
        config.setBucketName("my-bucket-123");

        assertEquals("upload/2024/images", config.getPrefix());
        assertEquals("my-bucket-123", config.getBucketName());
    }

    @Test
    @DisplayName("测试边界值-超长字符串")
    public void testLongStrings() {
        OssClientConfig config = new OssClientConfig();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("a");
        }
        String longString = sb.toString();

        config.setAccessKey(longString);
        config.setSecretKey(longString);

        assertEquals(longString, config.getAccessKey());
        assertEquals(longString, config.getSecretKey());
    }

    // ==================== equals/hashCode测试 ====================

    @Test
    @DisplayName("测试equals-相同配置应相等")
    public void testEqualsWithSameConfig() {
        OssClientConfig config1 = createTestConfig();
        OssClientConfig config2 = createTestConfig();

        assertEquals(config1, config2, "相同配置的对象应相等");
        assertEquals(config1.hashCode(), config2.hashCode(), "相等对象的hashCode应相同");
    }

    @Test
    @DisplayName("测试equals-不同配置应不相等")
    public void testEqualsWithDifferentConfig() {
        OssClientConfig config1 = createTestConfig();
        OssClientConfig config2 = createTestConfig();
        config2.setBucketName("different-bucket");

        assertNotEquals(config1, config2, "不同配置的对象应不相等");
    }

    @Test
    @DisplayName("测试equals-与null比较")
    public void testEqualsWithNull() {
        OssClientConfig config = createTestConfig();

        assertNotEquals(null, config, "与null比较应返回false");
    }

    @Test
    @DisplayName("测试equals-与自身比较")
    public void testEqualsWithSelf() {
        OssClientConfig config = createTestConfig();

        assertEquals(config, config, "与自身比较应返回true");
    }

    // ==================== toString测试 ====================

    @Test
    @DisplayName("测试toString-应包含所有字段")
    public void testToString() {
        OssClientConfig config = createTestConfig();
        String str = config.toString();

        assertNotNull(str, "toString不应返回null");
        assertTrue(str.contains("tenantId"), "应包含tenantId字段");
        assertTrue(str.contains("endpoint"), "应包含endpoint字段");
        assertTrue(str.contains("bucketName"), "应包含bucketName字段");
        assertTrue(str.contains("accessKey"), "应包含accessKey字段");
    }

    // ==================== 业务场景测试 ====================

    @Test
    @DisplayName("测试业务场景-HTTPS配置")
    public void testHttpsConfiguration() {
        OssClientConfig httpsConfig = new OssClientConfig();
        httpsConfig.setIsHttps("1");

        OssClientConfig httpConfig = new OssClientConfig();
        httpConfig.setIsHttps("0");

        assertEquals("1", httpsConfig.getIsHttps(), "HTTPS应为1");
        assertEquals("0", httpConfig.getIsHttps(), "HTTP应为0");
    }

    @Test
    @DisplayName("测试业务场景-访问策略配置")
    public void testAccessPolicyConfiguration() {
        OssClientConfig privateConfig = new OssClientConfig();
        privateConfig.setAccessPolicy("0");

        OssClientConfig publicConfig = new OssClientConfig();
        publicConfig.setAccessPolicy("1");

        OssClientConfig customConfig = new OssClientConfig();
        customConfig.setAccessPolicy("2");

        assertEquals("0", privateConfig.getAccessPolicy(), "私有策略应为0");
        assertEquals("1", publicConfig.getAccessPolicy(), "公共策略应为1");
        assertEquals("2", customConfig.getAccessPolicy(), "自定义策略应为2");
    }

    @Test
    @DisplayName("测试业务场景-多租户配置")
    public void testMultiTenantConfiguration() {
        OssClientConfig tenant1Config = new OssClientConfig();
        tenant1Config.setTenantId("tenant001");
        tenant1Config.setBucketName("tenant001-bucket");

        OssClientConfig tenant2Config = new OssClientConfig();
        tenant2Config.setTenantId("tenant002");
        tenant2Config.setBucketName("tenant002-bucket");

        assertNotEquals(tenant1Config.getTenantId(), tenant2Config.getTenantId());
        assertNotEquals(tenant1Config.getBucketName(), tenant2Config.getBucketName());
    }

    @Test
    @DisplayName("测试业务场景-自定义域名CDN配置")
    public void testCustomDomainCdnConfiguration() {
        OssClientConfig config = new OssClientConfig();
        config.setEndpoint("oss-cn-hangzhou.aliyuncs.com");
        config.setDomain("cdn.example.com");
        config.setBucketName("my-bucket");

        // 有自定义域名时，访问URL应使用域名
        assertNotNull(config.getDomain());
        assertNotEquals(config.getEndpoint(), config.getDomain());
    }

    @Test
    @DisplayName("测试业务场景-路径前缀配置")
    public void testPathPrefixConfiguration() {
        OssClientConfig config = new OssClientConfig();
        config.setPrefix("avatar");

        assertEquals("avatar", config.getPrefix(), "前缀应正确设置");
    }

    @Test
    @DisplayName("测试业务场景-区域配置")
    public void testRegionConfiguration() {
        // AWS区域
        OssClientConfig awsConfig = new OssClientConfig();
        awsConfig.setRegion("us-east-1");
        assertEquals("us-east-1", awsConfig.getRegion());

        // 阿里云区域
        OssClientConfig aliyunConfig = new OssClientConfig();
        aliyunConfig.setRegion("cn-hangzhou");
        assertEquals("cn-hangzhou", aliyunConfig.getRegion());

        // 腾讯云区域
        OssClientConfig qcloudConfig = new OssClientConfig();
        qcloudConfig.setRegion("ap-guangzhou");
        assertEquals("ap-guangzhou", qcloudConfig.getRegion());
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建测试用配置对象
     */
    private OssClientConfig createTestConfig() {
        OssClientConfig config = new OssClientConfig();
        config.setTenantId("tenant001");
        config.setEndpoint("s3.amazonaws.com");
        config.setDomain("cdn.example.com");
        config.setPrefix("upload");
        config.setAccessKey("AKIAIOSFODNN7EXAMPLE");
        config.setSecretKey("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
        config.setBucketName("my-test-bucket");
        config.setRegion("us-east-1");
        config.setIsHttps("1");
        config.setAccessPolicy("0");
        return config;
    }
}
