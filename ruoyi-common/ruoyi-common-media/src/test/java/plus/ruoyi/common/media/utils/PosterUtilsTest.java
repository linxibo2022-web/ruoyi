package plus.ruoyi.common.media.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import plus.ruoyi.common.media.builder.PosterBuilder;
import plus.ruoyi.common.media.enums.OutputFormat;
import plus.ruoyi.common.test.config.TestConfig;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author 抓蛙师
 * @date 2025/9/10
 */
class PosterUtilsTest {

    private String tempDir;

    @BeforeEach
    void setUp() {
        // 使用统一的测试配置目录
        tempDir = TestConfig.createTestSubDir("poster");
    }

    @Test
    void testCreateTextPoster() {
        BufferedImage result = PosterUtils.createTextPoster("测试文本", 400, 300);

        assertNotNull(result);
        assertEquals(400, result.getWidth());
        assertEquals(300, result.getHeight());
    }

    @Test
    void testCreateTextPosterWithBackground() {
        // 测试不带背景URL的情况（传入null）
        BufferedImage result = PosterUtils.createTextPoster("带背景的文本", null, 500, 400);

        assertNotNull(result);
        assertEquals(500, result.getWidth());
        assertEquals(400, result.getHeight());
    }

    @Test
    void testCreateProductPoster() {
        BufferedImage result = PosterUtils.createProductPoster(
                "测试商品",
                null, // 使用null避免网络依赖
                "¥199.00",
                600,
                800
        );

        assertNotNull(result);
        assertEquals(600, result.getWidth());
        assertEquals(800, result.getHeight());
    }

    @Test
    void testPosterBuilderBasicConstruction() {
        // 测试基本构造函数
        PosterBuilder builder = new PosterBuilder(400, 300);
        BufferedImage result = builder.build();

        assertNotNull(result);
        assertEquals(400, result.getWidth());
        assertEquals(300, result.getHeight());
    }

    @Test
    void testPosterBuilderWithColorBackground() {
        PosterBuilder builder = new PosterBuilder(400, 300);
        BufferedImage result = builder
                .background(Color.YELLOW)
                .build();

        assertNotNull(result);
        assertEquals(400, result.getWidth());
        assertEquals(300, result.getHeight());
    }

    @Test
    void testPosterBuilderWithUrlBackground() {
        PosterBuilder builder = new PosterBuilder(400, 300);
        // 测试设置背景URL（即使URL无效也不应该崩溃）
        BufferedImage result = builder
                .background("https://cloudcache.tencent-cloud.com/qcloud/portal/kit/images/presale.a4955999.jpeg")
                .build();

        assertNotNull(result);
        assertEquals(400, result.getWidth());
        assertEquals(300, result.getHeight());
    }

    @Test
    void testAddTextWithDefaultFont() {
        PosterBuilder builder = new PosterBuilder(400, 300);
        BufferedImage result = builder
                .addText("测试文本", 24, "#FF0000", 50, 100)
                .build();

        assertNotNull(result);
    }

    @Test
    void testAddTextWithCustomFont() {
        PosterBuilder builder = new PosterBuilder(400, 300);
        BufferedImage result = builder
                .addText("测试文本", "微软雅黑", 24, "#FF0000", 50, 100)
                .build();

        assertNotNull(result);
    }

    @Test
    void testAddTextWithFontObject() {
        PosterBuilder builder = new PosterBuilder(400, 300);
        Font customFont = new Font("Arial", Font.BOLD, 20);
        BufferedImage result = builder
                .addText("测试文本", customFont, Color.BLUE, 50, 100)
                .build();

        assertNotNull(result);
    }

    @Test
    void testAddImage() {
        PosterBuilder builder = new PosterBuilder(500, 400);
        // 使用null URL测试，避免网络依赖
        BufferedImage result = builder
                .addImage(null, 100, 100, 200, 150)
                .build();

        assertNotNull(result);
    }

    @Test
    void testAddCircleImage() {
        PosterBuilder builder = new PosterBuilder(500, 400);
        BufferedImage result = builder
                .addCircleImage(null, 100, 100, 80)
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
    void testFormatSetting() {
        PosterBuilder builder = new PosterBuilder(400, 300);
        BufferedImage result = builder
                .format(OutputFormat.JPEG)
                .addText("JPEG格式测试", 16, "#000000", 10, 30)
                .build();

        assertNotNull(result);
    }

    @Test
    void testToBytesWithPNG() {
        PosterBuilder builder = new PosterBuilder(200, 200);
        byte[] result = builder
                .format(OutputFormat.PNG)
                .addText("PNG测试", 16, "#000000", 10, 30)
                .toBytes();

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testToBytesWithJPEG() {
        PosterBuilder builder = new PosterBuilder(200, 200);
        byte[] result = builder
                .format(OutputFormat.JPEG)
                .addText("JPEG测试", 16, "#000000", 10, 30)
                .toBytes();

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testSavePNG() {
        PosterBuilder builder = new PosterBuilder(200, 200);
        String filePath = tempDir + File.separator + "poster_test.png";

        assertDoesNotThrow(() -> {
            builder.format(OutputFormat.PNG)
                    .addText("保存测试", 16, "#000000", 10, 30)
                    .save(filePath);
        });

        // 验证文件是否创建
        File savedFile = new File(filePath);
        assertTrue(savedFile.exists());
        assertTrue(savedFile.length() > 0);

    }

    @Test
    void testSaveJPEG() {
        PosterBuilder builder = new PosterBuilder(200, 200);
        String filePath = tempDir + File.separator + "poster_test.jpg";

        assertDoesNotThrow(() -> {
            builder.format(OutputFormat.JPEG)
                    .addText("JPEG保存测试", 16, "#000000", 10, 30)
                    .save(filePath);
        });

        // 验证文件是否创建
        File savedFile = new File(filePath);
        assertTrue(savedFile.exists());
        assertTrue(savedFile.length() > 0);

    }

    @Test
    void testChainedMethodCalls() {
        PosterBuilder builder = new PosterBuilder(600, 500);
        BufferedImage result = builder
                .background(Color.WHITE)
                .format(OutputFormat.PNG)
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

    @Test
    void testComplexPosterWithAllElements() {
        PosterBuilder builder = new PosterBuilder(800, 600);
        String filePath = tempDir + File.separator + "complex_poster.png";

        BufferedImage result = builder
                .background(Color.WHITE)
                .addText("复杂海报测试", "微软雅黑", 36, "#333333", 50, 60)
                .addText("这是一个包含多种元素的测试海报", 18, "#666666", 50, 100)
                .addImage(null, 50, 150, 300, 200) // 使用null避免网络依赖
                .addCircleImage(null, 400, 150, 100)
                .addQrCode("https://example.com", 650, 450, 100)
                .format(OutputFormat.PNG)
                .build();

        assertNotNull(result);
        assertEquals(800, result.getWidth());
        assertEquals(600, result.getHeight());

        // 保存并验证文件
        assertDoesNotThrow(() -> builder.save(filePath));
        File savedFile = new File(filePath);
        assertTrue(savedFile.exists());
    }

    @Test
    void testMultipleBuildCalls() {
        // 测试多次调用build方法
        PosterBuilder builder = new PosterBuilder(300, 200);
        builder.addText("测试", 16, "#000000", 10, 30);

        BufferedImage result1 = builder.build();
        BufferedImage result2 = builder.build();

        assertNotNull(result1);
        assertNotNull(result2);
        // 两次build应该返回相同的结果
        assertEquals(result1.getWidth(), result2.getWidth());
        assertEquals(result1.getHeight(), result2.getHeight());
    }

    @Test
    void testEmptyTextHandling() {
        // 测试空文本的处理
        PosterBuilder builder = new PosterBuilder(300, 200);

        assertDoesNotThrow(() -> {
            BufferedImage result = builder
                    .addText("", 16, "#000000", 10, 30)
                    .addText(null, 16, "#000000", 10, 60) // 测试null文本
                    .build();
            assertNotNull(result);
        });
    }

    @Test
    void testColorParsing() {
        // 测试不同的颜色格式
        PosterBuilder builder = new PosterBuilder(400, 300);

        assertDoesNotThrow(() -> {
            BufferedImage result = builder
                    .addText("红色文本", 16, "#FF0000", 10, 30)
                    .addText("蓝色文本", 16, "#0000FF", 10, 60)
                    .addText("绿色文本", 16, "#00FF00", 10, 90)
                    .addText("白色文本", 16, "#FFFFFF", 10, 120)
                    .addText("黑色文本", 16, "#000000", 10, 150)
                    .build();
            assertNotNull(result);
        });
    }

    @Test
    void testQrCodeGeneration() {
        // 测试二维码生成
        PosterBuilder builder = new PosterBuilder(400, 400);

        BufferedImage result = builder
                .addQrCode("简单文本", 50, 50, 80)
                .addQrCode("https://www.example.com", 150, 50, 80)
                .addQrCode("中文内容测试", 250, 50, 80)
                .build();

        assertNotNull(result);
    }

    @Test
    void testBuildAndSaveSequence() {
        // 测试构建后保存的完整流程
        PosterBuilder builder = new PosterBuilder(500, 300);
        String filePath = tempDir + File.separator + "sequence_test.png";

        // 先构建
        BufferedImage image = builder
                .background(Color.LIGHT_GRAY)
                .addText("序列测试", 24, "#000000", 50, 100)
                .addQrCode("test content", 350, 150, 80)
                .build();

        assertNotNull(image);

        // 再保存
        assertDoesNotThrow(() -> builder.save(filePath));

        // 验证文件
        File savedFile = new File(filePath);
        assertTrue(savedFile.exists());
    }
}
