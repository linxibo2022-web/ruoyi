package plus.ruoyi.common.media.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import plus.ruoyi.common.media.options.QrCodeOptions;
import plus.ruoyi.common.test.config.TestConfig;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * QrCodeUtils 单元测试
 */
class QrCodeUtilsTest {

    private String tempDir;

    @BeforeEach
    void setUp() {
        // 使用统一的测试配置目录
        tempDir = TestConfig.createTestSubDir("qrcode");
    }

    @Test
    void testGenerate() {
        BufferedImage result = QrCodeUtils.generate("https://example.com");

        assertNotNull(result);
        assertEquals(300, result.getWidth()); // 默认尺寸
        assertEquals(300, result.getHeight());
    }

    @Test
    void testGenerateWithSize() {
        BufferedImage result = QrCodeUtils.generate("test content", 250);

        assertNotNull(result);
        assertEquals(250, result.getWidth());
        assertEquals(250, result.getHeight());
    }

    @Test
    void testGenerateWithLogo() {
        // 注意: Logo加载失败时,方法会捕获异常并返回不带Logo的二维码
        // 这里测试异常处理逻辑是否正常工作
        BufferedImage result = QrCodeUtils.generateWithLogo(
                "https://github.com", 300, "https://example.com/logo.png");

        // 即使Logo加载失败,也应该返回正常的二维码
        assertNotNull(result);
        assertEquals(300, result.getWidth());
        assertEquals(300, result.getHeight());
    }

    @Test
    void testGenerateColorful() {
        BufferedImage result = QrCodeUtils.generateColorful(
                "colorful qr", 200, Color.BLUE, Color.YELLOW);

        assertNotNull(result);
        assertEquals(200, result.getWidth());
        assertEquals(200, result.getHeight());
    }

    @Test
    void testSaveQrCode() {
        QrCodeOptions options = QrCodeOptions.defaults()
                .size(250)
                .margin(2)
                .colors(Color.BLACK, Color.WHITE);

        String filePath = tempDir + File.separator + "saved_qr.png";

        assertDoesNotThrow(() -> QrCodeUtils.saveQrCode(
                "save test", options, filePath));
    }

    @Test
    void testGenerateWithChineseContent() {
        BufferedImage result = QrCodeUtils.generate("中文测试内容");

        assertNotNull(result);
        assertEquals(300, result.getWidth());
        assertEquals(300, result.getHeight());
    }

    @Test
    void testGenerateWithSpecialCharacters() {
        BufferedImage result = QrCodeUtils.generate("!@#$%^&*()_+{}|:<>?");

        assertNotNull(result);
        assertEquals(300, result.getWidth());
        assertEquals(300, result.getHeight());
    }

    @Test
    void testGenerateWithUrl() {
        BufferedImage result = QrCodeUtils.generate("https://www.example.com/path?param=value&other=123");

        assertNotNull(result);
        assertEquals(300, result.getWidth());
        assertEquals(300, result.getHeight());
    }
}
