package plus.ruoyi.common.oss.constant;

import plus.ruoyi.common.test.base.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OssConstant 常量类测试
 *
 * @author 抓蛙师
 */
@DisplayName("OssConstant常量类测试")
public class OssConstantTest extends BaseUnitTest {

    // ==================== 目录常量测试 ====================

    @Test
    @DisplayName("测试常量-ALL全部目录")
    public void testAllConstant() {
        assertEquals(9999999999999999L, OssConstant.ALL, "ALL应为9999999999999999");
        assertTrue(OssConstant.ALL > 0, "ALL应为正数");
    }

    @Test
    @DisplayName("测试常量-UNCATEGORIZED未分类目录")
    public void testUncategorizedConstant() {
        assertEquals(10000000000000000L, OssConstant.UNCATEGORIZED, "UNCATEGORIZED应为10000000000000000");
        assertTrue(OssConstant.UNCATEGORIZED > OssConstant.ALL, "UNCATEGORIZED应大于ALL");
    }

    // ==================== 路径常量测试 ====================

    @Test
    @DisplayName("测试常量-UPLOAD_PATH上传接口路径")
    public void testUploadPathConstant() {
        assertEquals("/resource/oss/upload", OssConstant.UPLOAD_PATH);
        assertTrue(OssConstant.UPLOAD_PATH.startsWith("/"), "上传路径应以/开头");
    }

    @Test
    @DisplayName("测试常量-RESOURCE_PATH资源路径")
    public void testResourcePathConstant() {
        assertEquals("resources", OssConstant.RESOURCE_PATH);
        assertFalse(OssConstant.RESOURCE_PATH.startsWith("/"), "资源路径不应以/开头");
    }

    @Test
    @DisplayName("测试常量-RESOURCE_PREFIX资源前缀")
    public void testResourcePrefixConstant() {
        assertEquals("/resources", OssConstant.RESOURCE_PREFIX);
        assertTrue(OssConstant.RESOURCE_PREFIX.startsWith("/"), "资源前缀应以/开头");
        assertEquals("/" + OssConstant.RESOURCE_PATH, OssConstant.RESOURCE_PREFIX,
            "RESOURCE_PREFIX应等于/加RESOURCE_PATH");
    }

    // ==================== 配置Key常量测试 ====================

    @Test
    @DisplayName("测试常量-DEFAULT_CONFIG_KEY默认配置Key")
    public void testDefaultConfigKeyConstant() {
        assertNotNull(OssConstant.DEFAULT_CONFIG_KEY, "默认配置Key不应为null");
        assertTrue(OssConstant.DEFAULT_CONFIG_KEY.contains("sys_oss"), "应包含sys_oss");
        assertTrue(OssConstant.DEFAULT_CONFIG_KEY.contains("default_config"), "应包含default_config");
    }

    @Test
    @DisplayName("测试常量-PREVIEW_LIST_RESOURCE_KEY预览开关Key")
    public void testPreviewListResourceKeyConstant() {
        assertEquals("system.oss.preview-enabled", OssConstant.PREVIEW_LIST_RESOURCE_KEY);
        assertTrue(OssConstant.PREVIEW_LIST_RESOURCE_KEY.contains("oss"), "应包含oss");
        assertTrue(OssConstant.PREVIEW_LIST_RESOURCE_KEY.contains("preview"), "应包含preview");
    }

    // ==================== 系统数据ID常量测试 ====================

    @Test
    @DisplayName("测试常量-SYSTEM_DATA_IDS系统数据ID")
    public void testSystemDataIdsConstant() {
        List<Long> ids = OssConstant.SYSTEM_DATA_IDS;

        assertNotNull(ids, "系统数据ID列表不应为null");
        assertEquals(4, ids.size(), "应有4个系统数据ID");
        assertTrue(ids.contains(1L), "应包含ID 1");
        assertTrue(ids.contains(2L), "应包含ID 2");
        assertTrue(ids.contains(3L), "应包含ID 3");
        assertTrue(ids.contains(4L), "应包含ID 4");
    }

    @Test
    @DisplayName("测试常量-SYSTEM_DATA_IDS不可变性")
    public void testSystemDataIdsImmutability() {
        List<Long> ids = OssConstant.SYSTEM_DATA_IDS;

        // List.of 创建的列表是不可变的
        assertThrows(UnsupportedOperationException.class, () -> ids.add(5L),
            "系统数据ID列表应不可变");
    }

    // ==================== 云服务商常量测试 ====================

    @Test
    @DisplayName("测试常量-CLOUD_SERVICE云服务商列表")
    public void testCloudServiceConstant() {
        String[] services = OssConstant.CLOUD_SERVICE;

        assertNotNull(services, "云服务商列表不应为null");
        assertEquals(4, services.length, "应有4个云服务商");
    }

    @Test
    @DisplayName("测试常量-CLOUD_SERVICE包含aliyun")
    public void testCloudServiceContainsAliyun() {
        assertTrue(Arrays.asList(OssConstant.CLOUD_SERVICE).contains("aliyun"),
            "应包含aliyun");
    }

    @Test
    @DisplayName("测试常量-CLOUD_SERVICE包含qcloud")
    public void testCloudServiceContainsQcloud() {
        assertTrue(Arrays.asList(OssConstant.CLOUD_SERVICE).contains("qcloud"),
            "应包含qcloud");
    }

    @Test
    @DisplayName("测试常量-CLOUD_SERVICE包含qiniu")
    public void testCloudServiceContainsQiniu() {
        assertTrue(Arrays.asList(OssConstant.CLOUD_SERVICE).contains("qiniu"),
            "应包含qiniu");
    }

    @Test
    @DisplayName("测试常量-CLOUD_SERVICE包含obs")
    public void testCloudServiceContainsObs() {
        assertTrue(Arrays.asList(OssConstant.CLOUD_SERVICE).contains("obs"),
            "应包含obs");
    }

    @Test
    @DisplayName("测试常量-CLOUD_SERVICE不包含local")
    public void testCloudServiceNotContainsLocal() {
        assertFalse(Arrays.asList(OssConstant.CLOUD_SERVICE).contains("local"),
            "云服务商列表不应包含local");
    }

    @Test
    @DisplayName("测试常量-CLOUD_SERVICE不包含minio")
    public void testCloudServiceNotContainsMinio() {
        // MinIO是私有部署，不是云服务商
        assertFalse(Arrays.asList(OssConstant.CLOUD_SERVICE).contains("minio"),
            "云服务商列表不应包含minio");
    }

    // ==================== HTTPS状态常量测试 ====================

    @Test
    @DisplayName("测试常量-IS_HTTPS")
    public void testIsHttpsConstant() {
        assertEquals("1", OssConstant.IS_HTTPS, "IS_HTTPS应为字符串1");
    }

    // ==================== 业务场景测试 ====================

    @Test
    @DisplayName("测试业务场景-判断是否为云服务商")
    public void testIsCloudServiceProvider() {
        String[] cloudServices = OssConstant.CLOUD_SERVICE;

        // 阿里云endpoint
        String aliyunEndpoint = "oss-cn-hangzhou.aliyuncs.com";
        boolean isCloud = Arrays.stream(cloudServices)
            .anyMatch(service -> aliyunEndpoint.contains(service));
        assertTrue(isCloud, "阿里云endpoint应被识别为云服务商");

        // 腾讯云endpoint
        String qcloudEndpoint = "cos.ap-guangzhou.myqcloud.com";
        isCloud = Arrays.stream(cloudServices)
            .anyMatch(service -> qcloudEndpoint.contains(service));
        assertTrue(isCloud, "腾讯云endpoint应被识别为云服务商");

        // MinIO endpoint
        String minioEndpoint = "minio.example.com:9000";
        isCloud = Arrays.stream(cloudServices)
            .anyMatch(service -> minioEndpoint.contains(service));
        assertFalse(isCloud, "MinIO endpoint不应被识别为云服务商");

        // 本地存储
        String localEndpoint = "localhost";
        isCloud = Arrays.stream(cloudServices)
            .anyMatch(service -> localEndpoint.contains(service));
        assertFalse(isCloud, "本地存储不应被识别为云服务商");
    }

    @Test
    @DisplayName("测试业务场景-判断是否为系统数据")
    public void testIsSystemData() {
        List<Long> systemIds = OssConstant.SYSTEM_DATA_IDS;

        assertTrue(systemIds.contains(1L), "ID 1应为系统数据");
        assertTrue(systemIds.contains(2L), "ID 2应为系统数据");
        assertTrue(systemIds.contains(3L), "ID 3应为系统数据");
        assertTrue(systemIds.contains(4L), "ID 4应为系统数据");
        assertFalse(systemIds.contains(5L), "ID 5不应为系统数据");
        assertFalse(systemIds.contains(100L), "ID 100不应为系统数据");
    }

    @Test
    @DisplayName("测试业务场景-构建资源访问URL")
    public void testBuildResourceUrl() {
        String baseUrl = "http://localhost:8080";
        String fileName = "2024/05/27/abc123.jpg";

        String resourceUrl = baseUrl + OssConstant.RESOURCE_PREFIX + "/" + fileName;

        assertTrue(resourceUrl.contains("/resources/"), "资源URL应包含resources路径");
        assertEquals("http://localhost:8080/resources/2024/05/27/abc123.jpg", resourceUrl);
    }

    @Test
    @DisplayName("测试业务场景-HTTPS判断")
    public void testHttpsCheck() {
        String httpsEnabled = "1";
        String httpsDisabled = "0";

        assertEquals(OssConstant.IS_HTTPS, httpsEnabled, "1表示启用HTTPS");
        assertNotEquals(OssConstant.IS_HTTPS, httpsDisabled, "0表示禁用HTTPS");

        boolean isHttps = OssConstant.IS_HTTPS.equals(httpsEnabled);
        assertTrue(isHttps, "应判断为HTTPS");
    }

    @Test
    @DisplayName("测试业务场景-特殊目录ID判断")
    public void testSpecialDirectoryIds() {
        Long allDirId = OssConstant.ALL;
        Long uncategorizedDirId = OssConstant.UNCATEGORIZED;

        // 假设文件目录ID
        Long normalDirId = 1001L;

        // 判断是否为特殊目录
        boolean isAll = allDirId.equals(normalDirId);
        boolean isUncategorized = uncategorizedDirId.equals(normalDirId);

        assertFalse(isAll, "普通目录不应等于ALL");
        assertFalse(isUncategorized, "普通目录不应等于UNCATEGORIZED");

        // 特殊目录判断
        assertTrue(allDirId.equals(OssConstant.ALL));
        assertTrue(uncategorizedDirId.equals(OssConstant.UNCATEGORIZED));
    }

    // ==================== 常量一致性测试 ====================

    @Test
    @DisplayName("测试一致性-RESOURCE_PREFIX与RESOURCE_PATH")
    public void testResourcePrefixConsistency() {
        String expected = "/" + OssConstant.RESOURCE_PATH;
        assertEquals(expected, OssConstant.RESOURCE_PREFIX,
            "RESOURCE_PREFIX应该等于斜杠加RESOURCE_PATH");
    }

    @Test
    @DisplayName("测试一致性-系统数据ID顺序")
    public void testSystemDataIdsOrder() {
        List<Long> ids = OssConstant.SYSTEM_DATA_IDS;

        assertEquals(1L, ids.get(0), "第一个ID应为1");
        assertEquals(2L, ids.get(1), "第二个ID应为2");
        assertEquals(3L, ids.get(2), "第三个ID应为3");
        assertEquals(4L, ids.get(3), "第四个ID应为4");
    }
}
