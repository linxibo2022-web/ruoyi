package plus.ruoyi.common.media.builder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import plus.ruoyi.common.test.config.TestConfig;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * QrCodeBuilder 单元测试
 */
class QrCodeBuilderTest {

    private String tempDir;

    @BeforeEach
    void setUp() {
        // 使用统一的测试配置目录
        tempDir = TestConfig.createTestSubDir("qrcode");
    }

    @Test
    void testBasicQrCode() {
        QrCodeBuilder builder = new QrCodeBuilder("https://example.com");
        BufferedImage result = builder.build();

        assertNotNull(result);
        assertEquals(300, result.getWidth()); // 默认尺寸
        assertEquals(300, result.getHeight());
    }

    @Test
    void testCustomSize() {
        QrCodeBuilder builder = new QrCodeBuilder("test content");
        BufferedImage result = builder.size(200).build();

        assertNotNull(result);
        assertEquals(200, result.getWidth());
        assertEquals(200, result.getHeight());
    }

    @Test
    void testCustomColors() {
        QrCodeBuilder builder = new QrCodeBuilder("color test");
        BufferedImage result = builder
                .colors(Color.BLUE, Color.YELLOW)
                .build();

        assertNotNull(result);
        assertEquals(300, result.getWidth());
        assertEquals(300, result.getHeight());
    }

    @Test
    void testMargin() {
        QrCodeBuilder builder = new QrCodeBuilder("margin test");
        BufferedImage result = builder
                .margin(5)
                .build();

        assertNotNull(result);
    }

    @Test
    void testChainedConfiguration() {
        QrCodeBuilder builder = new QrCodeBuilder("https://github.com");
        BufferedImage result = builder
                .size(250)
                .margin(2)
                .colors(Color.BLACK, Color.WHITE)
                .build();

        assertNotNull(result);
        assertEquals(250, result.getWidth());
        assertEquals(250, result.getHeight());
    }

    @Test
    void testToBytes() {
        QrCodeBuilder builder = new QrCodeBuilder("byte test");
        byte[] result = builder.toBytes();

        assertNotNull(result);
        assertTrue(result.length > 0);

        // 验证是PNG格式（简单检查文件头）
        assertEquals((byte) 0x89, result[0]); // PNG signature
        assertEquals('P', result[1]);
        assertEquals('N', result[2]);
        assertEquals('G', result[3]);
    }

    @Test
    void testSave() {
        QrCodeBuilder builder = new QrCodeBuilder("save test");
        String filePath = tempDir + File.separator + "qrcode.png";

        assertDoesNotThrow(() -> builder.save(filePath));
    }

    @Test
    void testLongContent() {
        String longContent = "这是一个很长的测试内容，包含中文字符和特殊符号！@#$%^&*()_+{}|:<>?[]\\;'\",./ " +
                "This is a long test content with Chinese characters and special symbols!";
        QrCodeBuilder builder = new QrCodeBuilder(longContent);
        BufferedImage result = builder.size(400).build();

        assertNotNull(result);
        assertEquals(400, result.getWidth());
        assertEquals(400, result.getHeight());
    }

    @Test
    void testSpecialCharacters() {
        QrCodeBuilder builder = new QrCodeBuilder("特殊字符测试 !@#$%^&*()");
        BufferedImage result = builder.build();

        assertNotNull(result);
    }
}
