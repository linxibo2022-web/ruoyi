package plus.ruoyi.common.media.builder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import plus.ruoyi.common.media.exception.MediaException;
import plus.ruoyi.common.test.config.TestConfig;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GifBuilder 单元测试
 */
class GifBuilderTest {

    private BufferedImage frame1, frame2, frame3;
    private String tempDir;

    @BeforeEach
    void setUp() {
        // 使用统一的测试配置目录
        tempDir = TestConfig.createTestSubDir("gif");
        // 创建测试帧
        frame1 = createColoredFrame(Color.RED);
        frame2 = createColoredFrame(Color.GREEN);
        frame3 = createColoredFrame(Color.BLUE);
    }

    private BufferedImage createColoredFrame(Color color) {
        BufferedImage frame = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = frame.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(0, 0, 100, 100);
        g2d.dispose();
        return frame;
    }

    @Test
    void testDefaultConstructor() {
        GifBuilder builder = new GifBuilder();
        assertNotNull(builder);
    }

    @Test
    void testConstructorWithSize() {
        GifBuilder builder = new GifBuilder(200, 150);
        assertNotNull(builder);
    }

    @Test
    void testAddFrame() {
        GifBuilder builder = new GifBuilder();
        builder.addFrame(frame1);

        byte[] result = builder.build();
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testAddFrameWithDelay() {
        GifBuilder builder = new GifBuilder();
        builder.addFrame(frame1, 1000);

        byte[] result = builder.build();
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testAddFrames() {
        GifBuilder builder = new GifBuilder();
        List<BufferedImage> frames = Arrays.asList(frame1, frame2, frame3);
        builder.addFrames(frames);

        byte[] result = builder.build();
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testChainedConfiguration() {
        GifBuilder builder = new GifBuilder();
        byte[] result = builder
                .size(150, 150)
                .delay(800)
                .loop(true)
                .backgroundColor(Color.WHITE)
                .quality(90)
                .addFrame(frame1)
                .addFrame(frame2)
                .build();

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testEmptyFramesThrowsException() {
        GifBuilder builder = new GifBuilder();

        assertThrows(MediaException.class, builder::build);
    }

    @Test
    void testToBytes() {
        GifBuilder builder = new GifBuilder();
        builder.addFrame(frame1);

        byte[] result1 = builder.build();
        byte[] result2 = builder.toBytes();

        assertArrayEquals(result1, result2);
    }

    @Test
    void testSave() {
        GifBuilder builder = new GifBuilder();
        builder.addFrame(frame1).addFrame(frame2);
        String filePath = tempDir + File.separator + "test.gif";

        assertDoesNotThrow(() -> builder.save(filePath));
    }

    @Test
    void testMultipleFramesWithDifferentDelays() {
        GifBuilder builder = new GifBuilder();
        builder.addFrame(frame1, 500)
                .addFrame(frame2, 1000)
                .addFrame(frame3, 300);

        byte[] result = builder.build();
        assertNotNull(result);
        assertTrue(result.length > 0);
    }
}
