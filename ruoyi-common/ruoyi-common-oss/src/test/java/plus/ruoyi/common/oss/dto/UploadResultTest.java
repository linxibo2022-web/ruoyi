package plus.ruoyi.common.oss.dto;

import plus.ruoyi.common.test.base.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UploadResult 上传结果DTO测试
 *
 * @author 抓蛙师
 */
@DisplayName("UploadResult上传结果DTO测试")
public class UploadResultTest extends BaseUnitTest {

    // ==================== Builder模式测试 ====================

    @Test
    @DisplayName("测试Builder-完整构建")
    public void testBuilderComplete() {
        UploadResult result = UploadResult.builder()
            .url("http://example.com/files/test.jpg")
            .fileName("test.jpg")
            .fileSize(1024L)
            .eTag("abc123def456")
            .build();

        assertNotNull(result, "构建的对象不应为null");
        assertEquals("http://example.com/files/test.jpg", result.getUrl());
        assertEquals("test.jpg", result.getFileName());
        assertEquals(1024L, result.getFileSize());
        assertEquals("abc123def456", result.getETag());
    }

    @Test
    @DisplayName("测试Builder-部分字段")
    public void testBuilderPartial() {
        UploadResult result = UploadResult.builder()
            .url("http://example.com/files/doc.pdf")
            .fileName("doc.pdf")
            .build();

        assertNotNull(result);
        assertEquals("http://example.com/files/doc.pdf", result.getUrl());
        assertEquals("doc.pdf", result.getFileName());
        assertNull(result.getFileSize(), "未设置的字段应为null");
        assertNull(result.getETag(), "未设置的字段应为null");
    }

    @Test
    @DisplayName("测试Builder-空构建")
    public void testBuilderEmpty() {
        UploadResult result = UploadResult.builder().build();

        assertNotNull(result, "空构建也应返回有效对象");
        assertNull(result.getUrl());
        assertNull(result.getFileName());
        assertNull(result.getFileSize());
        assertNull(result.getETag());
    }

    // ==================== Setter/Getter测试 ====================

    @Test
    @DisplayName("测试Setter/Getter-url")
    public void testSetGetUrl() {
        UploadResult result = UploadResult.builder().build();

        result.setUrl("http://test.com/image.png");

        assertEquals("http://test.com/image.png", result.getUrl());
    }

    @Test
    @DisplayName("测试Setter/Getter-fileName")
    public void testSetGetFileName() {
        UploadResult result = UploadResult.builder().build();

        result.setFileName("document.pdf");

        assertEquals("document.pdf", result.getFileName());
    }

    @Test
    @DisplayName("测试Setter/Getter-fileSize")
    public void testSetGetFileSize() {
        UploadResult result = UploadResult.builder().build();

        result.setFileSize(2048L);

        assertEquals(2048L, result.getFileSize());
    }

    @Test
    @DisplayName("测试Setter/Getter-eTag")
    public void testSetGetETag() {
        UploadResult result = UploadResult.builder().build();

        result.setETag("etag-value-123");

        assertEquals("etag-value-123", result.getETag());
    }

    // ==================== 边界值测试 ====================

    @Test
    @DisplayName("测试边界值-fileSize为0")
    public void testFileSizeZero() {
        UploadResult result = UploadResult.builder()
            .fileSize(0L)
            .build();

        assertEquals(0L, result.getFileSize(), "文件大小为0应正常处理");
    }

    @Test
    @DisplayName("测试边界值-超大fileSize")
    public void testFileSizeLarge() {
        Long largeSize = Long.MAX_VALUE;
        UploadResult result = UploadResult.builder()
            .fileSize(largeSize)
            .build();

        assertEquals(largeSize, result.getFileSize(), "超大文件大小应正常处理");
    }

    @Test
    @DisplayName("测试边界值-空字符串url")
    public void testEmptyUrl() {
        UploadResult result = UploadResult.builder()
            .url("")
            .build();

        assertEquals("", result.getUrl(), "空字符串URL应正常处理");
    }

    @Test
    @DisplayName("测试边界值-特殊字符文件名")
    public void testSpecialCharFileName() {
        String specialFileName = "文档(1)-副本【测试】.pdf";
        UploadResult result = UploadResult.builder()
            .fileName(specialFileName)
            .build();

        assertEquals(specialFileName, result.getFileName(), "特殊字符文件名应正常处理");
    }

    // ==================== 路径格式测试 ====================

    @Test
    @DisplayName("测试路径格式-带日期路径")
    public void testDatePathFormat() {
        UploadResult result = UploadResult.builder()
            .url("http://minio.example.com/bucket/2024/05/27/abc123.jpg")
            .fileName("2024/05/27/abc123.jpg")
            .fileSize(5120L)
            .build();

        assertTrue(result.getUrl().contains("2024/05/27"), "URL应包含日期路径");
        assertTrue(result.getFileName().contains("2024/05/27"), "文件名应包含日期路径");
    }

    @Test
    @DisplayName("测试路径格式-带租户路径")
    public void testTenantPathFormat() {
        UploadResult result = UploadResult.builder()
            .url("http://oss.example.com/tenant123/2024/05/27/doc.pdf")
            .fileName("tenant123/2024/05/27/doc.pdf")
            .build();

        assertTrue(result.getUrl().contains("tenant123"), "URL应包含租户ID");
        assertTrue(result.getFileName().contains("tenant123"), "文件名应包含租户ID");
    }

    // ==================== equals/hashCode测试 ====================

    @Test
    @DisplayName("测试equals-相同属性的对象应相等")
    public void testEqualsWithSameProperties() {
        UploadResult result1 = UploadResult.builder()
            .url("http://example.com/test.jpg")
            .fileName("test.jpg")
            .fileSize(1024L)
            .eTag("etag123")
            .build();

        UploadResult result2 = UploadResult.builder()
            .url("http://example.com/test.jpg")
            .fileName("test.jpg")
            .fileSize(1024L)
            .eTag("etag123")
            .build();

        assertEquals(result1, result2, "相同属性的对象应相等");
        assertEquals(result1.hashCode(), result2.hashCode(), "相等对象的hashCode应相同");
    }

    @Test
    @DisplayName("测试equals-不同属性的对象应不相等")
    public void testEqualsWithDifferentProperties() {
        UploadResult result1 = UploadResult.builder()
            .url("http://example.com/test1.jpg")
            .build();

        UploadResult result2 = UploadResult.builder()
            .url("http://example.com/test2.jpg")
            .build();

        assertNotEquals(result1, result2, "不同属性的对象应不相等");
    }

    // ==================== toString测试 ====================

    @Test
    @DisplayName("测试toString-应包含所有字段")
    public void testToString() {
        UploadResult result = UploadResult.builder()
            .url("http://example.com/file.jpg")
            .fileName("file.jpg")
            .fileSize(2048L)
            .eTag("etag-value")
            .build();

        String str = result.toString();

        assertNotNull(str, "toString不应返回null");
        assertTrue(str.contains("url"), "toString应包含url字段");
        assertTrue(str.contains("fileName"), "toString应包含fileName字段");
        assertTrue(str.contains("fileSize"), "toString应包含fileSize字段");
        assertTrue(str.contains("eTag"), "toString应包含eTag字段");
    }
}
