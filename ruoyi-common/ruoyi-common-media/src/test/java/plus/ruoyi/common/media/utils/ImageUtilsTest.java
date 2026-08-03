package plus.ruoyi.common.media.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import plus.ruoyi.common.media.enums.ResizeMode;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ImageUtils 单元测试
 */
class ImageUtilsTest {

    private BufferedImage testImage;

    @BeforeEach
    void setUp() {
        testImage = new BufferedImage(200, 150, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = testImage.createGraphics();
        g2d.setColor(Color.GREEN);
        g2d.fillRect(0, 0, 200, 150);
        g2d.dispose();
    }

    @Test
    void testLoadFromBytes() {
        byte[] imageBytes = ImageUtils.toBytes(testImage, "PNG");
        BufferedImage result = ImageUtils.loadFromBytes(imageBytes);

        assertNotNull(result);
        assertEquals(200, result.getWidth());
        assertEquals(150, result.getHeight());
    }

    @Test
    void testToBytes() {
        byte[] result = ImageUtils.toBytes(testImage, "PNG");

        assertNotNull(result);
        assertTrue(result.length > 0);

        // 验证PNG文件头
        assertEquals((byte) 0x89, result[0]);
        assertEquals('P', result[1]);
        assertEquals('N', result[2]);
        assertEquals('G', result[3]);
    }

    @Test
    void testResizeFit() {
        BufferedImage result = ImageUtils.resize(testImage, 100, 100, ResizeMode.FIT);

        assertNotNull(result);
        assertEquals(100, result.getWidth());
        assertEquals(100, result.getHeight());
    }

    @Test
    void testResizeFill() {
        BufferedImage result = ImageUtils.resize(testImage, 100, 200, ResizeMode.FILL);

        assertNotNull(result);
        assertEquals(100, result.getWidth());
        assertEquals(200, result.getHeight());
    }

    @Test
    void testResizeStretch() {
        BufferedImage result = ImageUtils.resize(testImage, 300, 100, ResizeMode.STRETCH);

        assertNotNull(result);
        assertEquals(300, result.getWidth());
        assertEquals(100, result.getHeight());
    }

    @Test
    void testCreateCircleImage() {
        BufferedImage result = ImageUtils.createCircleImage(testImage, 80);

        assertNotNull(result);
        assertEquals(80, result.getWidth());
        assertEquals(80, result.getHeight());
        assertEquals(BufferedImage.TYPE_INT_ARGB, result.getType()); // 圆形图片应该支持透明
    }

    @Test
    void testToBytesJPEG() {
        byte[] result = ImageUtils.toBytes(testImage, "JPEG");

        assertNotNull(result);
        assertTrue(result.length > 0);

        // 验证JPEG文件头
        assertEquals((byte) 0xFF, result[0]);
        assertEquals((byte) 0xD8, result[1]);
    }

    @Test
    void testResizeToSameSize() {
        BufferedImage result = ImageUtils.resize(testImage, 200, 150, ResizeMode.FIT);

        assertNotNull(result);
        assertEquals(200, result.getWidth());
        assertEquals(150, result.getHeight());
    }

    @Test
    void testCreateCircleImageLargerThanOriginal() {
        BufferedImage result = ImageUtils.createCircleImage(testImage, 300);

        assertNotNull(result);
        assertEquals(300, result.getWidth());
        assertEquals(300, result.getHeight());
    }
}
