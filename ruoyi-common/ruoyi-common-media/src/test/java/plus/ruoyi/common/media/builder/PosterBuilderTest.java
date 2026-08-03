package plus.ruoyi.common.media.builder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import plus.ruoyi.common.media.enums.OutputFormat;
import plus.ruoyi.common.test.config.TestConfig;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PosterBuilder 单元测试
 */
class PosterBuilderTest {

    private String tempDir;

    @BeforeEach
    void setUp() {
        // 使用统一的测试配置目录
        tempDir = TestConfig.createTestSubDir("poster");
    }

    @Test
    void testBasicConstructor() {
        PosterBuilder builder = new PosterBuilder(400, 300);
        assertNotNull(builder);

        BufferedImage result = builder.build();
        assertNotNull(result);
        assertEquals(400, result.getWidth());
        assertEquals(300, result.getHeight());
    }

    @Test
    void testConstructorWithBackground() {
        PosterBuilder builder = new PosterBuilder(Color.BLUE, 400, 300);
        assertNotNull(builder);

        BufferedImage result = builder.build();
        assertNotNull(result);
        assertEquals(400, result.getWidth());
        assertEquals(300, result.getHeight());
    }

    @Test
    void testAddText() {
        PosterBuilder builder = new PosterBuilder(400, 300);
        BufferedImage result = builder
                .addText("Test Text", 24, "#FF0000", 50, 50)
                .build();

        assertNotNull(result);
        assertEquals(400, result.getWidth());
        assertEquals(300, result.getHeight());
    }

    @Test
    void testAddTextWithFont() {
        PosterBuilder builder = new PosterBuilder(400, 300);
        Font font = new Font("Arial", Font.BOLD, 32);
        BufferedImage result = builder
                .addText("Bold Text", font, Color.BLACK, 50, 50)
                .build();

        assertNotNull(result);
    }

    @Test
    void testAddTextWithFontName() {
        PosterBuilder builder = new PosterBuilder(400, 300);
        BufferedImage result = builder
                .addText("Custom Font", "Arial", 28, "#0000FF", 50, 100)
                .build();

        assertNotNull(result);
    }

    @Test
    void testAddQrCode() {
        PosterBuilder builder = new PosterBuilder(400, 300);
        BufferedImage result = builder
                .addQrCode("https://example.com", 300, 200, 80)
                .build();

        assertNotNull(result);
    }

    @Test
    void testChainedOperations() {
        PosterBuilder builder = new PosterBuilder(500, 400);
        BufferedImage result = builder
                .background(Color.WHITE)
                .addText("标题", "微软雅黑", 36, "#333333", 50, 80)
                .addText("副标题", 24, "#666666", 50, 120)
                .addQrCode("https://example.com", 350, 250, 100)
                .format(OutputFormat.PNG)
                .build();

        assertNotNull(result);
        assertEquals(500, result.getWidth());
        assertEquals(400, result.getHeight());
    }

    @Test
    void testBackgroundMethods() {
        PosterBuilder builder = new PosterBuilder(400, 300);

        // 测试颜色背景
        builder.background(Color.YELLOW);
        BufferedImage result1 = builder.build();
        assertNotNull(result1);

        // 测试URL背景
        builder.background("https://example.com/bg.jpg");
        // 注意：这里可能因为URL无效而失败，在实际测试中需要有效的图片URL
    }

    @Test
    void testFormatSetting() {
        PosterBuilder builder = new PosterBuilder(400, 300);
        builder.format(OutputFormat.JPEG);

        BufferedImage result = builder.build();
        assertNotNull(result);
    }

    @Test
    void testToBytes() {
        PosterBuilder builder = new PosterBuilder(200, 200);
        builder.addText("Test", 16, "#000000", 10, 30);

        byte[] result = builder.toBytes();
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testSave() {
        PosterBuilder builder = new PosterBuilder(200, 200);
        builder.addText("Save Test", 16, "#000000", 10, 30);
        String filePath = tempDir + File.separator + "poster.png";

        assertDoesNotThrow(() -> builder.save(filePath));
    }

    @Test
    void testMultipleElements() {
        PosterBuilder builder = new PosterBuilder(600, 500);
        BufferedImage result = builder
                .addText("主标题", 48, "#FF0000", 100, 80)
                .addText("副标题", 32, "#0000FF", 100, 140)
                .addText("描述文本", 24, "#666666", 100, 180)
                .addQrCode("content1", 450, 300, 80)
                .addQrCode("content2", 450, 400, 80)
                .build();

        assertNotNull(result);
        assertEquals(600, result.getWidth());
        assertEquals(500, result.getHeight());
    }
}
