package plus.ruoyi.common.core.utils.file;

import plus.ruoyi.common.test.base.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FileUtils 文件工具测试
 *
 * @author 抓蛙师
 */
@DisplayName("FileUtils文件工具测试")
public class FileUtilsTest extends BaseUnitTest {

    // ==================== percentEncode 测试 ====================

    @Test
    @DisplayName("测试percentEncode-英文文件名")
    public void testPercentEncodeEnglishFileName() {
        String fileName = "test.txt";
        String encoded = FileUtils.percentEncode(fileName);

        assertEquals("test.txt", encoded, "纯英文文件名应保持不变");
    }

    @Test
    @DisplayName("测试percentEncode-中文文件名")
    public void testPercentEncodeChineseFileName() {
        String fileName = "测试文件.txt";
        String encoded = FileUtils.percentEncode(fileName);

        // 中文应被编码
        assertNotEquals(fileName, encoded);
        assertTrue(encoded.contains("%"), "中文应该被百分号编码");
        assertTrue(encoded.contains(".txt"), "文件扩展名应被编码");
    }

    @Test
    @DisplayName("测试percentEncode-包含空格的文件名")
    public void testPercentEncodeFileNameWithSpaces() {
        String fileName = "test file.txt";
        String encoded = FileUtils.percentEncode(fileName);

        // 空格应该被编码为 %20 而不是 +
        assertTrue(encoded.contains("%20"), "空格应该被编码为 %20");
        assertFalse(encoded.contains("+"), "不应该包含加号");
        assertEquals("test%20file.txt", encoded);
    }

    @Test
    @DisplayName("测试percentEncode-包含特殊字符的文件名")
    public void testPercentEncodeFileNameWithSpecialChars() {
        // 包含括号
        String fileName1 = "file(1).txt";
        String encoded1 = FileUtils.percentEncode(fileName1);
        assertTrue(encoded1.contains("%28") && encoded1.contains("%29"),
            "括号应该被编码");

        // 包含中文括号
        String fileName2 = "文件（1）.txt";
        String encoded2 = FileUtils.percentEncode(fileName2);
        assertTrue(encoded2.contains("%"), "中文括号应该被编码");
    }

    @Test
    @DisplayName("测试percentEncode-空字符串")
    public void testPercentEncodeEmptyString() {
        String encoded = FileUtils.percentEncode("");
        assertEquals("", encoded, "空字符串应返回空字符串");
    }

    @Test
    @DisplayName("测试percentEncode-复杂文件名")
    public void testPercentEncodeComplexFileName() {
        String fileName = "用户导出数据 2024-01-15 (副本).xlsx";
        String encoded = FileUtils.percentEncode(fileName);

        // 应该被编码且不包含 +
        assertNotEquals(fileName, encoded);
        assertFalse(encoded.contains("+"), "不应该包含加号");
        assertTrue(encoded.contains("%20"), "空格应该被编码为 %20");
    }

    @Test
    @DisplayName("测试percentEncode-已编码的字符串")
    public void testPercentEncodeAlreadyEncoded() {
        // 测试重复编码
        String fileName = "test file.txt";
        String encoded1 = FileUtils.percentEncode(fileName);
        String encoded2 = FileUtils.percentEncode(encoded1);

        // 重复编码会导致 % 被再次编码
        assertNotEquals(encoded1, encoded2);
        assertTrue(encoded2.contains("%25"), "百分号应该被编码为 %25");
    }

    // ==================== setAttachmentResponseHeader 测试 ====================

    @Test
    @DisplayName("测试setAttachmentResponseHeader-设置响应头")
    public void testSetAttachmentResponseHeaderBasic() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        String fileName = "test.txt";

        FileUtils.setAttachmentResponseHeader(response, fileName);

        // 验证 Content-Disposition 头
        String contentDisposition = response.getHeader("Content-disposition");
        assertNotNull(contentDisposition, "Content-disposition头不应为null");
        assertTrue(contentDisposition.startsWith("attachment;"),
            "Content-disposition应以attachment开头");
        assertTrue(contentDisposition.contains("filename="),
            "Content-disposition应包含filename");
        assertTrue(contentDisposition.contains("filename*=utf-8''"),
            "Content-disposition应包含filename*参数");
    }

    @Test
    @DisplayName("测试setAttachmentResponseHeader-中文文件名")
    public void testSetAttachmentResponseHeaderChineseFileName() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        String fileName = "测试文件.txt";

        FileUtils.setAttachmentResponseHeader(response, fileName);

        // 验证 Content-Disposition 头
        String contentDisposition = response.getHeader("Content-disposition");
        assertNotNull(contentDisposition);
        assertTrue(contentDisposition.contains("attachment;"));

        // 验证中文被正确编码
        assertFalse(contentDisposition.contains("测试文件"),
            "中文应该被编码而不是直接出现");
        assertTrue(contentDisposition.contains("%"),
            "中文应该被百分号编码");
    }

    @Test
    @DisplayName("测试setAttachmentResponseHeader-包含空格的文件名")
    public void testSetAttachmentResponseHeaderFileNameWithSpaces() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        String fileName = "test file name.txt";

        FileUtils.setAttachmentResponseHeader(response, fileName);

        // 验证空格被正确编码
        String contentDisposition = response.getHeader("Content-disposition");
        assertTrue(contentDisposition.contains("%20"),
            "空格应该被编码为 %20");
        assertFalse(contentDisposition.contains("+"),
            "不应该包含加号");
    }

    @Test
    @DisplayName("测试setAttachmentResponseHeader-验证所有响应头")
    public void testSetAttachmentResponseHeaderAllHeaders() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        String fileName = "测试文件.xlsx";

        FileUtils.setAttachmentResponseHeader(response, fileName);

        // 验证 Content-disposition 头
        String contentDisposition = response.getHeader("Content-disposition");
        assertNotNull(contentDisposition, "Content-disposition不应为null");

        // 验证 download-filename 头
        String downloadFilename = response.getHeader("download-filename");
        assertNotNull(downloadFilename, "download-filename不应为null");
        assertTrue(downloadFilename.contains("%"), "文件名应被编码");

        // 验证 Access-Control-Expose-Headers 头
        String exposeHeaders = response.getHeader("Access-Control-Expose-Headers");
        assertNotNull(exposeHeaders, "Access-Control-Expose-Headers不应为null");
        assertTrue(exposeHeaders.contains("Content-Disposition"),
            "应暴露Content-Disposition头");
        assertTrue(exposeHeaders.contains("download-filename"),
            "应暴露download-filename头");
    }

    @Test
    @DisplayName("测试setAttachmentResponseHeader-特殊字符文件名")
    public void testSetAttachmentResponseHeaderSpecialChars() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        String fileName = "订单导出(2024-01-15).csv";

        FileUtils.setAttachmentResponseHeader(response, fileName);

        String contentDisposition = response.getHeader("Content-disposition");
        assertNotNull(contentDisposition);

        // 验证特殊字符被编码
        assertTrue(contentDisposition.contains("%28"), "左括号应被编码为 %28");
        assertTrue(contentDisposition.contains("%29"), "右括号应被编码为 %29");
    }

    @Test
    @DisplayName("测试setAttachmentResponseHeader-长文件名")
    public void testSetAttachmentResponseHeaderLongFileName() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        String fileName = "这是一个非常长的文件名包含很多中文字符用于测试文件下载功能是否正常工作.xlsx";

        FileUtils.setAttachmentResponseHeader(response, fileName);

        // 验证响应头设置成功
        String contentDisposition = response.getHeader("Content-disposition");
        assertNotNull(contentDisposition);
        assertTrue(contentDisposition.length() > 0);

        String downloadFilename = response.getHeader("download-filename");
        assertNotNull(downloadFilename);
        assertTrue(downloadFilename.length() > 0);
    }

    @Test
    @DisplayName("测试setAttachmentResponseHeader-验证RFC3986标准")
    public void testSetAttachmentResponseHeaderRFC3986Compliance() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        String fileName = "test file.txt";

        FileUtils.setAttachmentResponseHeader(response, fileName);

        String contentDisposition = response.getHeader("Content-disposition");
        String downloadFilename = response.getHeader("download-filename");

        // RFC 3986 标准：空格应编码为 %20 而不是 +
        assertTrue(contentDisposition.contains("%20"),
            "根据RFC 3986，空格应编码为 %20");
        assertTrue(downloadFilename.contains("%20"),
            "根据RFC 3986，空格应编码为 %20");

        assertFalse(contentDisposition.contains(" test "),
            "不应包含未编码的空格");
        assertFalse(downloadFilename.contains("+"),
            "不应使用加号代替空格");
    }

    @Test
    @DisplayName("测试setAttachmentResponseHeader-多次调用")
    public void testSetAttachmentResponseHeaderMultipleCalls() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        // 第一次设置
        FileUtils.setAttachmentResponseHeader(response, "file1.txt");
        String header1 = response.getHeader("download-filename");

        // 第二次设置（覆盖）
        FileUtils.setAttachmentResponseHeader(response, "file2.txt");
        String header2 = response.getHeader("download-filename");

        // 验证后续设置覆盖了之前的值
        assertNotEquals(header1, header2);
        assertTrue(header2.contains("file2.txt"));
    }

    // ==================== 集成测试 ====================

    @Test
    @DisplayName("测试集成场景-完整文件下载流程")
    public void testIntegrationCompleteDownloadFlow() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        // 模拟实际文件下载场景
        String originalFileName = "用户数据导出 2024-01-15 (最终版).xlsx";

        // 1. 编码文件名
        String encodedFileName = FileUtils.percentEncode(originalFileName);
        assertNotNull(encodedFileName);
        assertFalse(encodedFileName.contains("+"));

        // 2. 设置响应头
        FileUtils.setAttachmentResponseHeader(response, originalFileName);

        // 3. 验证响应头正确设置
        assertNotNull(response.getHeader("Content-disposition"));
        assertNotNull(response.getHeader("download-filename"));
        assertNotNull(response.getHeader("Access-Control-Expose-Headers"));

        // 4. 验证编码一致性
        String downloadFilename = response.getHeader("download-filename");
        assertEquals(encodedFileName, downloadFilename,
            "download-filename应与percentEncode结果一致");
    }

    @Test
    @DisplayName("测试边界情况-各种文件扩展名")
    public void testBoundaryVariousFileExtensions() {
        String[] fileNames = {
            "文档.doc",
            "表格.xls",
            "图片.jpg",
            "压缩包.zip",
            "视频.mp4",
            "音频.mp3",
            "文本.txt",
            "代码.java"
        };

        for (String fileName : fileNames) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            FileUtils.setAttachmentResponseHeader(response, fileName);

            assertNotNull(response.getHeader("Content-disposition"),
                fileName + " 的Content-disposition不应为null");
            assertNotNull(response.getHeader("download-filename"),
                fileName + " 的download-filename不应为null");
        }
    }
}
