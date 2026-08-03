package plus.ruoyi.common.oss.dto;

import plus.ruoyi.common.test.base.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OssFileInfo 文件信息DTO测试
 *
 * @author 抓蛙师
 */
@DisplayName("OssFileInfo文件信息DTO测试")
public class OssFileInfoTest extends BaseUnitTest {

    // ==================== Builder模式测试 ====================

    @Test
    @DisplayName("测试Builder-完整构建")
    public void testBuilderComplete() {
        Date updateTime = new Date();

        OssFileInfo fileInfo = OssFileInfo.builder()
            .fileName("document.pdf")
            .fileSize(2048L)
            .filePath("/tenant123/2024/05/27/document.pdf")
            .contentType("application/pdf")
            .isDirectory(false)
            .updateTime(updateTime)
            .url("http://example.com/tenant123/2024/05/27/document.pdf")
            .build();

        assertNotNull(fileInfo, "构建的对象不应为null");
        assertEquals("document.pdf", fileInfo.getFileName());
        assertEquals(2048L, fileInfo.getFileSize());
        assertEquals("/tenant123/2024/05/27/document.pdf", fileInfo.getFilePath());
        assertEquals("application/pdf", fileInfo.getContentType());
        assertFalse(fileInfo.getIsDirectory());
        assertEquals(updateTime, fileInfo.getUpdateTime());
        assertEquals("http://example.com/tenant123/2024/05/27/document.pdf", fileInfo.getUrl());
    }

    @Test
    @DisplayName("测试Builder-目录类型")
    public void testBuilderDirectory() {
        OssFileInfo fileInfo = OssFileInfo.builder()
            .fileName("images")
            .filePath("/tenant123/images/")
            .isDirectory(true)
            .build();

        assertNotNull(fileInfo);
        assertEquals("images", fileInfo.getFileName());
        assertTrue(fileInfo.getIsDirectory(), "应为目录类型");
    }

    @Test
    @DisplayName("测试Builder-空构建")
    public void testBuilderEmpty() {
        OssFileInfo fileInfo = OssFileInfo.builder().build();

        assertNotNull(fileInfo, "空构建也应返回有效对象");
        assertNull(fileInfo.getFileName());
        assertNull(fileInfo.getFileSize());
        assertNull(fileInfo.getFilePath());
        assertNull(fileInfo.getContentType());
        assertNull(fileInfo.getIsDirectory());
        assertNull(fileInfo.getUpdateTime());
        assertNull(fileInfo.getUrl());
    }

    // ==================== Setter/Getter测试 ====================

    @Test
    @DisplayName("测试Setter/Getter-fileName")
    public void testSetGetFileName() {
        OssFileInfo fileInfo = OssFileInfo.builder().build();
        fileInfo.setFileName("test.jpg");

        assertEquals("test.jpg", fileInfo.getFileName());
    }

    @Test
    @DisplayName("测试Setter/Getter-fileSize")
    public void testSetGetFileSize() {
        OssFileInfo fileInfo = OssFileInfo.builder().build();
        fileInfo.setFileSize(4096L);

        assertEquals(4096L, fileInfo.getFileSize());
    }

    @Test
    @DisplayName("测试Setter/Getter-filePath")
    public void testSetGetFilePath() {
        OssFileInfo fileInfo = OssFileInfo.builder().build();
        fileInfo.setFilePath("/path/to/file.txt");

        assertEquals("/path/to/file.txt", fileInfo.getFilePath());
    }

    @Test
    @DisplayName("测试Setter/Getter-contentType")
    public void testSetGetContentType() {
        OssFileInfo fileInfo = OssFileInfo.builder().build();
        fileInfo.setContentType("image/png");

        assertEquals("image/png", fileInfo.getContentType());
    }

    @Test
    @DisplayName("测试Setter/Getter-isDirectory")
    public void testSetGetIsDirectory() {
        OssFileInfo fileInfo = OssFileInfo.builder().build();

        fileInfo.setIsDirectory(true);
        assertTrue(fileInfo.getIsDirectory());

        fileInfo.setIsDirectory(false);
        assertFalse(fileInfo.getIsDirectory());
    }

    @Test
    @DisplayName("测试Setter/Getter-updateTime")
    public void testSetGetUpdateTime() {
        OssFileInfo fileInfo = OssFileInfo.builder().build();
        Date now = new Date();
        fileInfo.setUpdateTime(now);

        assertEquals(now, fileInfo.getUpdateTime());
    }

    @Test
    @DisplayName("测试Setter/Getter-url")
    public void testSetGetUrl() {
        OssFileInfo fileInfo = OssFileInfo.builder().build();
        fileInfo.setUrl("http://cdn.example.com/images/photo.jpg");

        assertEquals("http://cdn.example.com/images/photo.jpg", fileInfo.getUrl());
    }

    // ==================== ContentType测试 ====================

    @Test
    @DisplayName("测试ContentType-图片类型")
    public void testContentTypeImage() {
        OssFileInfo fileInfo = OssFileInfo.builder()
            .fileName("photo.jpg")
            .contentType("image/jpeg")
            .build();

        assertTrue(fileInfo.getContentType().startsWith("image/"), "应为图片类型");
    }

    @Test
    @DisplayName("测试ContentType-文档类型")
    public void testContentTypeDocument() {
        OssFileInfo fileInfo = OssFileInfo.builder()
            .fileName("report.pdf")
            .contentType("application/pdf")
            .build();

        assertEquals("application/pdf", fileInfo.getContentType());
    }

    @Test
    @DisplayName("测试ContentType-默认类型")
    public void testContentTypeDefault() {
        OssFileInfo fileInfo = OssFileInfo.builder()
            .fileName("unknown.xyz")
            .contentType("application/octet-stream")
            .build();

        assertEquals("application/octet-stream", fileInfo.getContentType(), "未知类型应使用默认值");
    }

    @Test
    @DisplayName("测试ContentType-常见类型")
    public void testCommonContentTypes() {
        // PNG图片
        OssFileInfo png = OssFileInfo.builder().contentType("image/png").build();
        assertEquals("image/png", png.getContentType());

        // GIF图片
        OssFileInfo gif = OssFileInfo.builder().contentType("image/gif").build();
        assertEquals("image/gif", gif.getContentType());

        // Word文档
        OssFileInfo word = OssFileInfo.builder()
            .contentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
            .build();
        assertNotNull(word.getContentType());

        // Excel文档
        OssFileInfo excel = OssFileInfo.builder()
            .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            .build();
        assertNotNull(excel.getContentType());

        // JSON
        OssFileInfo json = OssFileInfo.builder().contentType("application/json").build();
        assertEquals("application/json", json.getContentType());

        // 文本
        OssFileInfo text = OssFileInfo.builder().contentType("text/plain").build();
        assertEquals("text/plain", text.getContentType());
    }

    // ==================== 路径格式测试 ====================

    @Test
    @DisplayName("测试路径格式-Unix风格")
    public void testUnixPathStyle() {
        OssFileInfo fileInfo = OssFileInfo.builder()
            .filePath("/tenant/2024/05/27/file.txt")
            .build();

        assertTrue(fileInfo.getFilePath().startsWith("/"), "应以/开头");
        assertFalse(fileInfo.getFilePath().contains("\\"), "不应包含Windows路径分隔符");
    }

    @Test
    @DisplayName("测试路径格式-带租户和日期")
    public void testPathWithTenantAndDate() {
        OssFileInfo fileInfo = OssFileInfo.builder()
            .filePath("/tenant123/avatar/2024/05/27/photo.jpg")
            .build();

        String path = fileInfo.getFilePath();
        assertTrue(path.contains("tenant123"), "应包含租户ID");
        assertTrue(path.contains("2024/05/27"), "应包含日期路径");
    }

    // ==================== 边界值测试 ====================

    @Test
    @DisplayName("测试边界值-文件大小为0")
    public void testFileSizeZero() {
        OssFileInfo fileInfo = OssFileInfo.builder()
            .fileSize(0L)
            .build();

        assertEquals(0L, fileInfo.getFileSize(), "文件大小为0应正常处理");
    }

    @Test
    @DisplayName("测试边界值-超大文件")
    public void testFileSizeLarge() {
        Long largeSize = 10L * 1024 * 1024 * 1024; // 10GB
        OssFileInfo fileInfo = OssFileInfo.builder()
            .fileSize(largeSize)
            .build();

        assertEquals(largeSize, fileInfo.getFileSize(), "超大文件大小应正常处理");
    }

    @Test
    @DisplayName("测试边界值-中文文件名")
    public void testChineseFileName() {
        String chineseFileName = "报告文档【2024】.docx";
        OssFileInfo fileInfo = OssFileInfo.builder()
            .fileName(chineseFileName)
            .build();

        assertEquals(chineseFileName, fileInfo.getFileName(), "中文文件名应正常处理");
    }

    @Test
    @DisplayName("测试边界值-超长文件名")
    public void testLongFileName() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("a");
        }
        sb.append(".txt");
        String longFileName = sb.toString();

        OssFileInfo fileInfo = OssFileInfo.builder()
            .fileName(longFileName)
            .build();

        assertEquals(longFileName, fileInfo.getFileName(), "超长文件名应正常处理");
    }

    // ==================== equals/hashCode测试 ====================

    @Test
    @DisplayName("测试equals-相同属性的对象应相等")
    public void testEqualsWithSameProperties() {
        Date updateTime = new Date();

        OssFileInfo info1 = OssFileInfo.builder()
            .fileName("test.jpg")
            .fileSize(1024L)
            .filePath("/path/test.jpg")
            .isDirectory(false)
            .updateTime(updateTime)
            .build();

        OssFileInfo info2 = OssFileInfo.builder()
            .fileName("test.jpg")
            .fileSize(1024L)
            .filePath("/path/test.jpg")
            .isDirectory(false)
            .updateTime(updateTime)
            .build();

        assertEquals(info1, info2, "相同属性的对象应相等");
        assertEquals(info1.hashCode(), info2.hashCode(), "相等对象的hashCode应相同");
    }

    @Test
    @DisplayName("测试equals-不同属性的对象应不相等")
    public void testEqualsWithDifferentProperties() {
        OssFileInfo info1 = OssFileInfo.builder()
            .fileName("test1.jpg")
            .build();

        OssFileInfo info2 = OssFileInfo.builder()
            .fileName("test2.jpg")
            .build();

        assertNotEquals(info1, info2, "不同属性的对象应不相等");
    }

    // ==================== toString测试 ====================

    @Test
    @DisplayName("测试toString-应包含所有字段")
    public void testToString() {
        OssFileInfo fileInfo = OssFileInfo.builder()
            .fileName("doc.pdf")
            .fileSize(2048L)
            .filePath("/path/doc.pdf")
            .contentType("application/pdf")
            .isDirectory(false)
            .url("http://example.com/doc.pdf")
            .build();

        String str = fileInfo.toString();

        assertNotNull(str, "toString不应返回null");
        assertTrue(str.contains("fileName"), "toString应包含fileName字段");
        assertTrue(str.contains("fileSize"), "toString应包含fileSize字段");
        assertTrue(str.contains("filePath"), "toString应包含filePath字段");
        assertTrue(str.contains("contentType"), "toString应包含contentType字段");
        assertTrue(str.contains("isDirectory"), "toString应包含isDirectory字段");
    }
}
