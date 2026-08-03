package plus.ruoyi.common.oss.dto;

import plus.ruoyi.common.test.base.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OssFileMetadata 文件元数据DTO测试
 *
 * @author 抓蛙师
 */
@DisplayName("OssFileMetadata文件元数据DTO测试")
public class OssFileMetadataTest extends BaseUnitTest {

    // ==================== Builder模式测试 ====================

    @Test
    @DisplayName("测试Builder-完整构建")
    public void testBuilderComplete() {
        Date createTime = new Date();
        Date updateTime = new Date();
        Map<String, String> userMetadata = new HashMap<>();
        userMetadata.put("author", "张三");
        userMetadata.put("department", "研发部");

        OssFileMetadata metadata = OssFileMetadata.builder()
            .fileName("report.pdf")
            .fileSize(4096L)
            .filePath("/tenant123/docs/report.pdf")
            .contentType("application/pdf")
            .eTag("\"abc123def456\"")
            .createTime(createTime)
            .updateTime(updateTime)
            .userMetadata(userMetadata)
            .build();

        assertNotNull(metadata, "构建的对象不应为null");
        assertEquals("report.pdf", metadata.getFileName());
        assertEquals(4096L, metadata.getFileSize());
        assertEquals("/tenant123/docs/report.pdf", metadata.getFilePath());
        assertEquals("application/pdf", metadata.getContentType());
        assertEquals("\"abc123def456\"", metadata.getETag());
        assertEquals(createTime, metadata.getCreateTime());
        assertEquals(updateTime, metadata.getUpdateTime());
        assertEquals(userMetadata, metadata.getUserMetadata());
        assertEquals("张三", metadata.getUserMetadata().get("author"));
    }

    @Test
    @DisplayName("测试Builder-空构建")
    public void testBuilderEmpty() {
        OssFileMetadata metadata = OssFileMetadata.builder().build();

        assertNotNull(metadata, "空构建也应返回有效对象");
        assertNull(metadata.getFileName());
        assertNull(metadata.getFileSize());
        assertNull(metadata.getFilePath());
        assertNull(metadata.getContentType());
        assertNull(metadata.getETag());
        assertNull(metadata.getCreateTime());
        assertNull(metadata.getUpdateTime());
        assertNull(metadata.getUserMetadata());
    }

    @Test
    @DisplayName("测试Builder-部分字段")
    public void testBuilderPartial() {
        OssFileMetadata metadata = OssFileMetadata.builder()
            .fileName("image.jpg")
            .fileSize(2048L)
            .contentType("image/jpeg")
            .build();

        assertNotNull(metadata);
        assertEquals("image.jpg", metadata.getFileName());
        assertEquals(2048L, metadata.getFileSize());
        assertEquals("image/jpeg", metadata.getContentType());
        assertNull(metadata.getFilePath());
        assertNull(metadata.getETag());
    }

    // ==================== Setter/Getter测试 ====================

    @Test
    @DisplayName("测试Setter/Getter-fileName")
    public void testSetGetFileName() {
        OssFileMetadata metadata = OssFileMetadata.builder().build();
        metadata.setFileName("document.docx");

        assertEquals("document.docx", metadata.getFileName());
    }

    @Test
    @DisplayName("测试Setter/Getter-fileSize")
    public void testSetGetFileSize() {
        OssFileMetadata metadata = OssFileMetadata.builder().build();
        metadata.setFileSize(8192L);

        assertEquals(8192L, metadata.getFileSize());
    }

    @Test
    @DisplayName("测试Setter/Getter-filePath")
    public void testSetGetFilePath() {
        OssFileMetadata metadata = OssFileMetadata.builder().build();
        metadata.setFilePath("/tenant/2024/05/file.txt");

        assertEquals("/tenant/2024/05/file.txt", metadata.getFilePath());
    }

    @Test
    @DisplayName("测试Setter/Getter-contentType")
    public void testSetGetContentType() {
        OssFileMetadata metadata = OssFileMetadata.builder().build();
        metadata.setContentType("text/html");

        assertEquals("text/html", metadata.getContentType());
    }

    @Test
    @DisplayName("测试Setter/Getter-eTag")
    public void testSetGetETag() {
        OssFileMetadata metadata = OssFileMetadata.builder().build();
        metadata.setETag("\"etag-hash-value\"");

        assertEquals("\"etag-hash-value\"", metadata.getETag());
    }

    @Test
    @DisplayName("测试Setter/Getter-createTime")
    public void testSetGetCreateTime() {
        OssFileMetadata metadata = OssFileMetadata.builder().build();
        Date createTime = new Date();
        metadata.setCreateTime(createTime);

        assertEquals(createTime, metadata.getCreateTime());
    }

    @Test
    @DisplayName("测试Setter/Getter-updateTime")
    public void testSetGetUpdateTime() {
        OssFileMetadata metadata = OssFileMetadata.builder().build();
        Date updateTime = new Date();
        metadata.setUpdateTime(updateTime);

        assertEquals(updateTime, metadata.getUpdateTime());
    }

    @Test
    @DisplayName("测试Setter/Getter-userMetadata")
    public void testSetGetUserMetadata() {
        OssFileMetadata metadata = OssFileMetadata.builder().build();
        Map<String, String> userMeta = new HashMap<>();
        userMeta.put("key1", "value1");
        userMeta.put("key2", "value2");
        metadata.setUserMetadata(userMeta);

        assertEquals(userMeta, metadata.getUserMetadata());
        assertEquals("value1", metadata.getUserMetadata().get("key1"));
        assertEquals("value2", metadata.getUserMetadata().get("key2"));
    }

    // ==================== ETag格式测试 ====================

    @Test
    @DisplayName("测试ETag格式-带引号")
    public void testETagWithQuotes() {
        OssFileMetadata metadata = OssFileMetadata.builder()
            .eTag("\"d41d8cd98f00b204e9800998ecf8427e\"")
            .build();

        String eTag = metadata.getETag();
        assertTrue(eTag.startsWith("\""), "ETag应以引号开头");
        assertTrue(eTag.endsWith("\""), "ETag应以引号结尾");
    }

    @Test
    @DisplayName("测试ETag格式-不带引号")
    public void testETagWithoutQuotes() {
        OssFileMetadata metadata = OssFileMetadata.builder()
            .eTag("d41d8cd98f00b204e9800998ecf8427e")
            .build();

        assertNotNull(metadata.getETag());
        assertEquals(32, metadata.getETag().length(), "MD5 ETag长度应为32");
    }

    @Test
    @DisplayName("测试ETag格式-S3分片上传")
    public void testETagMultipartUpload() {
        // S3分片上传的ETag格式为: "hash-partCount"
        OssFileMetadata metadata = OssFileMetadata.builder()
            .eTag("\"abc123def456-3\"")
            .build();

        assertTrue(metadata.getETag().contains("-"), "分片上传ETag应包含分片数");
    }

    // ==================== userMetadata测试 ====================

    @Test
    @DisplayName("测试userMetadata-空Map")
    public void testUserMetadataEmpty() {
        OssFileMetadata metadata = OssFileMetadata.builder()
            .userMetadata(new HashMap<>())
            .build();

        assertNotNull(metadata.getUserMetadata());
        assertTrue(metadata.getUserMetadata().isEmpty(), "空Map应正常处理");
    }

    @Test
    @DisplayName("测试userMetadata-多个元数据")
    public void testUserMetadataMultiple() {
        Map<String, String> userMeta = new HashMap<>();
        userMeta.put("x-amz-meta-author", "admin");
        userMeta.put("x-amz-meta-category", "document");
        userMeta.put("x-amz-meta-version", "1.0");

        OssFileMetadata metadata = OssFileMetadata.builder()
            .userMetadata(userMeta)
            .build();

        assertEquals(3, metadata.getUserMetadata().size());
        assertEquals("admin", metadata.getUserMetadata().get("x-amz-meta-author"));
        assertEquals("document", metadata.getUserMetadata().get("x-amz-meta-category"));
        assertEquals("1.0", metadata.getUserMetadata().get("x-amz-meta-version"));
    }

    @Test
    @DisplayName("测试userMetadata-中文值")
    public void testUserMetadataChineseValue() {
        Map<String, String> userMeta = new HashMap<>();
        userMeta.put("author", "张三");
        userMeta.put("description", "这是一份测试文档");

        OssFileMetadata metadata = OssFileMetadata.builder()
            .userMetadata(userMeta)
            .build();

        assertEquals("张三", metadata.getUserMetadata().get("author"));
        assertEquals("这是一份测试文档", metadata.getUserMetadata().get("description"));
    }

    // ==================== 时间字段测试 ====================

    @Test
    @DisplayName("测试时间字段-createTime早于updateTime")
    public void testTimeFieldsOrder() {
        Date createTime = new Date(System.currentTimeMillis() - 86400000); // 昨天
        Date updateTime = new Date(); // 今天

        OssFileMetadata metadata = OssFileMetadata.builder()
            .createTime(createTime)
            .updateTime(updateTime)
            .build();

        assertTrue(metadata.getCreateTime().before(metadata.getUpdateTime()),
            "创建时间应早于更新时间");
    }

    @Test
    @DisplayName("测试时间字段-相同时间")
    public void testTimeFieldsSame() {
        Date sameTime = new Date();

        OssFileMetadata metadata = OssFileMetadata.builder()
            .createTime(sameTime)
            .updateTime(sameTime)
            .build();

        assertEquals(metadata.getCreateTime(), metadata.getUpdateTime(),
            "新文件的创建时间和更新时间可能相同");
    }

    // ==================== 边界值测试 ====================

    @Test
    @DisplayName("测试边界值-超大文件大小")
    public void testFileSizeLarge() {
        Long largeSize = 100L * 1024 * 1024 * 1024; // 100GB
        OssFileMetadata metadata = OssFileMetadata.builder()
            .fileSize(largeSize)
            .build();

        assertEquals(largeSize, metadata.getFileSize(), "超大文件大小应正常处理");
    }

    @Test
    @DisplayName("测试边界值-文件大小为0")
    public void testFileSizeZero() {
        OssFileMetadata metadata = OssFileMetadata.builder()
            .fileSize(0L)
            .build();

        assertEquals(0L, metadata.getFileSize(), "空文件大小应正常处理");
    }

    @Test
    @DisplayName("测试边界值-特殊字符文件名")
    public void testSpecialCharFileName() {
        String specialName = "文档(副本)[测试]#1.pdf";
        OssFileMetadata metadata = OssFileMetadata.builder()
            .fileName(specialName)
            .build();

        assertEquals(specialName, metadata.getFileName(), "特殊字符文件名应正常处理");
    }

    // ==================== equals/hashCode测试 ====================

    @Test
    @DisplayName("测试equals-相同属性的对象应相等")
    public void testEqualsWithSameProperties() {
        Date time = new Date();
        Map<String, String> meta = new HashMap<>();
        meta.put("key", "value");

        OssFileMetadata metadata1 = OssFileMetadata.builder()
            .fileName("test.txt")
            .fileSize(100L)
            .eTag("\"etag\"")
            .createTime(time)
            .updateTime(time)
            .userMetadata(meta)
            .build();

        OssFileMetadata metadata2 = OssFileMetadata.builder()
            .fileName("test.txt")
            .fileSize(100L)
            .eTag("\"etag\"")
            .createTime(time)
            .updateTime(time)
            .userMetadata(meta)
            .build();

        assertEquals(metadata1, metadata2, "相同属性的对象应相等");
        assertEquals(metadata1.hashCode(), metadata2.hashCode(), "相等对象的hashCode应相同");
    }

    @Test
    @DisplayName("测试equals-不同属性的对象应不相等")
    public void testEqualsWithDifferentProperties() {
        OssFileMetadata metadata1 = OssFileMetadata.builder()
            .fileName("file1.txt")
            .build();

        OssFileMetadata metadata2 = OssFileMetadata.builder()
            .fileName("file2.txt")
            .build();

        assertNotEquals(metadata1, metadata2, "不同属性的对象应不相等");
    }

    // ==================== toString测试 ====================

    @Test
    @DisplayName("测试toString-应包含所有字段")
    public void testToString() {
        Map<String, String> meta = new HashMap<>();
        meta.put("key", "value");

        OssFileMetadata metadata = OssFileMetadata.builder()
            .fileName("test.pdf")
            .fileSize(1024L)
            .filePath("/path/test.pdf")
            .contentType("application/pdf")
            .eTag("\"etag\"")
            .createTime(new Date())
            .updateTime(new Date())
            .userMetadata(meta)
            .build();

        String str = metadata.toString();

        assertNotNull(str, "toString不应返回null");
        assertTrue(str.contains("fileName"), "toString应包含fileName字段");
        assertTrue(str.contains("fileSize"), "toString应包含fileSize字段");
        assertTrue(str.contains("filePath"), "toString应包含filePath字段");
        assertTrue(str.contains("contentType"), "toString应包含contentType字段");
        assertTrue(str.contains("eTag"), "toString应包含eTag字段");
        assertTrue(str.contains("userMetadata"), "toString应包含userMetadata字段");
    }
}
