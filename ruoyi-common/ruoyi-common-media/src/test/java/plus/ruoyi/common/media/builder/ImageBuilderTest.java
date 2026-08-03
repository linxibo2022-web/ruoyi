package plus.ruoyi.common.media.builder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import plus.ruoyi.common.media.enums.OutputFormat;
import plus.ruoyi.common.media.enums.ResizeMode;
import plus.ruoyi.common.media.options.FilterOptions;
import plus.ruoyi.common.media.options.WatermarkOptions;
import plus.ruoyi.common.test.config.TestConfig;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ImageBuilder 单元测试
 */
class ImageBuilderTest {

    private BufferedImage testImage;

    private String tempDir;

    @BeforeEach
    void setUp() {
        // 使用统一的测试配置目录
        tempDir = TestConfig.createTestSubDir("image");
        testImage = new BufferedImage(300, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = testImage.createGraphics();
        g2d.setColor(Color.RED);
        g2d.fillRect(0, 0, 300, 200);
        g2d.dispose();
    }

    @Test
    void testResize() {
        ImageBuilder builder = new ImageBuilder(testImage);
        BufferedImage result = builder.resize(150, 100).build();

        assertNotNull(result);
        assertEquals(150, result.getWidth());
        assertEquals(100, result.getHeight());
    }

    @Test
    void testResizeWithMode() {
        ImageBuilder builder = new ImageBuilder(testImage);
        BufferedImage result = builder.resize(100, 100, ResizeMode.STRETCH).build();

        assertNotNull(result);
        assertEquals(100, result.getWidth());
        assertEquals(100, result.getHeight());
    }

    @Test
    void testScale() {
        ImageBuilder builder = new ImageBuilder(testImage);
        BufferedImage result = builder.scale(0.5).build();

        assertNotNull(result);
        assertEquals(150, result.getWidth()); // 300 * 0.5
        assertEquals(100, result.getHeight()); // 200 * 0.5
    }

    @Test
    void testCrop() {
        ImageBuilder builder = new ImageBuilder(testImage);
        BufferedImage result = builder.crop(50, 50, 100, 100).build();

        assertNotNull(result);
        assertEquals(100, result.getWidth());
        assertEquals(100, result.getHeight());
    }

    @Test
    void testRotate() {
        ImageBuilder builder = new ImageBuilder(testImage);
        BufferedImage result = builder.rotate(90).build();

        assertNotNull(result);
        // 旋转后尺寸应该保持不变（在这个简单的实现中）
        assertEquals(300, result.getWidth());
        assertEquals(200, result.getHeight());
    }

    @Test
    void testFlipHorizontal() {
        ImageBuilder builder = new ImageBuilder(testImage);
        BufferedImage result = builder.flipHorizontal().build();

        assertNotNull(result);
        assertEquals(300, result.getWidth());
        assertEquals(200, result.getHeight());
    }

    @Test
    void testFlipVertical() {
        ImageBuilder builder = new ImageBuilder(testImage);
        BufferedImage result = builder.flipVertical().build();

        assertNotNull(result);
        assertEquals(300, result.getWidth());
        assertEquals(200, result.getHeight());
    }

    @Test
    void testAddTextWatermark() {
        ImageBuilder builder = new ImageBuilder(testImage);
        WatermarkOptions watermark = WatermarkOptions.text("Test Watermark")
                .position(WatermarkOptions.Position.CENTER)
                .opacity(0.5f);

        BufferedImage result = builder.addWatermark(watermark).build();

        assertNotNull(result);
        assertEquals(300, result.getWidth());
        assertEquals(200, result.getHeight());
    }

    @Test
    void testApplyFilter() {
        ImageBuilder builder = new ImageBuilder(testImage);
        FilterOptions filter = FilterOptions.defaults()
                .brightness(20)
                .contrast(1.2f)
                .grayscale();

        BufferedImage result = builder.applyFilter(filter).build();

        assertNotNull(result);
        assertEquals(300, result.getWidth());
        assertEquals(200, result.getHeight());
    }

    @Test
    void testChainedOperations() {
        ImageBuilder builder = new ImageBuilder(testImage);
        BufferedImage result = builder
                .resize(400, 300)
                .rotate(45)
                .addWatermark(WatermarkOptions.text("Chained"))
                .applyFilter(FilterOptions.defaults().brightness(10))
                .build();

        assertNotNull(result);
        assertEquals(400, result.getWidth());
        assertEquals(300, result.getHeight());
    }

    @Test
    void testReset() {
        ImageBuilder builder = new ImageBuilder(testImage);
        builder.resize(100, 100);

        BufferedImage result1 = builder.build();
        assertEquals(100, result1.getWidth());

        BufferedImage result2 = builder.reset().build();
        assertEquals(300, result2.getWidth()); // 恢复原始尺寸
    }

    @Test
    void testToBytes() {
        ImageBuilder builder = new ImageBuilder(testImage);
        byte[] result = builder.format(OutputFormat.PNG).toBytes();

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testSave() {
        ImageBuilder builder = new ImageBuilder(testImage);
        String filePath = tempDir + File.separator + "test.png";

        assertDoesNotThrow(() -> builder.save(filePath));
    }
}
