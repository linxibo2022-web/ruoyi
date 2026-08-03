package plus.ruoyi.common.media.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import plus.ruoyi.common.media.options.AnimationOptions;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * GifUtils 单元测试
 */
class GifUtilsTest {

    private List<BufferedImage> testFrames;

    @BeforeEach
    void setUp() {
        BufferedImage frame1 = createColoredFrame(Color.RED, 100, 100);
        BufferedImage frame2 = createColoredFrame(Color.GREEN, 100, 100);
        BufferedImage frame3 = createColoredFrame(Color.BLUE, 100, 100);
        testFrames = Arrays.asList(frame1, frame2, frame3);
    }

    private BufferedImage createColoredFrame(Color color, int width, int height) {
        BufferedImage frame = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = frame.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();
        return frame;
    }

    @Test
    void testCreateGifWithDelay() {
        byte[] result = GifUtils.createGif(testFrames, 500);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testCreateGifWithSize() {
        byte[] result = GifUtils.createGif(testFrames, 200, 150, 800);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testCreateGifWithOptions() {
        AnimationOptions options = AnimationOptions.defaults()
                .size(180, 180)
                .delay(600)
                .loop(false)
                .backgroundColor(Color.BLACK)
                .quality(95);

        byte[] result = GifUtils.createGif(testFrames, options);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testCreateGifWithSingleFrame() {
        List<BufferedImage> singleFrame = Arrays.asList(testFrames.get(0));
        byte[] result = GifUtils.createGif(singleFrame, 1000);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testCreateGifWithManyFrames() {
        // 创建更多帧
        List<BufferedImage> manyFrames = Arrays.asList(
                createColoredFrame(Color.RED, 50, 50),
                createColoredFrame(Color.GREEN, 50, 50),
                createColoredFrame(Color.BLUE, 50, 50),
                createColoredFrame(Color.YELLOW, 50, 50),
                createColoredFrame(Color.MAGENTA, 50, 50),
                createColoredFrame(Color.CYAN, 50, 50)
        );

        byte[] result = GifUtils.createGif(manyFrames, 300);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }
}
