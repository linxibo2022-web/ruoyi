package plus.ruoyi.common.media.utils;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import plus.ruoyi.common.media.builder.GifBuilder;
import plus.ruoyi.common.test.config.TestConfig;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * GIF多图片生成专项测试
 */
class GifMultiImageTest {

    private static String TEST_OUTPUT_DIR;
    private List<BufferedImage> testImages;

    @BeforeAll
    static void setupTestDir() {
        // 使用统一的测试配置目录
        TEST_OUTPUT_DIR = TestConfig.createTestSubDir("gif");
    }

    @BeforeEach
    void setUp() {
        testImages = new ArrayList<>();

        // 创建多个不同颜色的测试图片
        Color[] colors = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW,
                Color.MAGENTA, Color.CYAN, Color.ORANGE, Color.PINK};

        for (int i = 0; i < colors.length; i++) {
            BufferedImage image = createColoredFrame(colors[i], 400, 300, "帧 " + (i + 1));
            testImages.add(image);
        }
    }

    @Test
    void testCreateGifFromOnlineUrls() {
        System.out.println("=== 测试：从在线图片地址生成GIF ===");

        // 六个在线图片地址示例
        String[] imageUrls = {
                "https://oss.mydazy.cn/ailx/000000/2025/08/13/d87a1a2763234578a89e2fe1b97efa7d.gif",
                "https://oss.mydazy.cn/ailx/000000/2025/08/13/4c814827a72a459ea6c7af8f680933bf.gif",
                "https://oss.mydazy.cn/ailx/000000/2025/08/13/05ea19330a26439ab72f19b14157097d.gif",
                "https://oss.mydazy.cn/ailx/000000/2025/08/13/01749b2aa5cd4d2584e77b4086cc12e8.gif",
                "https://oss.mydazy.cn/ailx/000000/2025/08/13/f56397775e8744b59b0534a023db6d62.gif",
                "https://oss.mydazy.cn/ailx/000000/2025/08/13/1b500974e9f449abb3147e63670aa052.gif"
        };

        try {
            // 创建GIF构建器
            GifBuilder gifBuilder = new GifBuilder(302, 255)
                    .delay(1000)    // 每帧1秒
                    .loop(true)     // 循环播放
                    .quality(100);   // 80%质量

            // 添加每个图片URL作为帧
            for (int i = 0; i < imageUrls.length; i++) {
                System.out.println("添加第 " + (i + 1) + " 张图片...");
                gifBuilder.addFrame(imageUrls[i]);
            }

            // 保存GIF
            String gifPath = TEST_OUTPUT_DIR + File.separator + "six-images-example.gif";
            gifBuilder.save(gifPath);

            System.out.println("✓ GIF生成成功！");
            System.out.println("保存位置: " + gifPath);


            // 生成并保存GIF
            File outputFile = new File(TEST_OUTPUT_DIR, "online-images.gif");

            long startTime = System.currentTimeMillis();
            gifBuilder.save(outputFile.getAbsolutePath());
            long endTime = System.currentTimeMillis();

            assertTrue(outputFile.exists());
            assertTrue(outputFile.length() > 0);

            System.out.println("=== 生成结果 ===");
            System.out.println("GIF生成耗时: " + (endTime - startTime) + "ms");
            System.out.println("文件大小: " + String.format("%.2f KB", outputFile.length() / 1024.0));
            System.out.println("保存路径: " + outputFile.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("生成GIF失败: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * 测试：从多张BufferedImage生成GIF
     */
    @Test
    void testCreateGifFromMultipleImages() {
        System.out.println("=== 测试：从多张BufferedImage生成GIF ===");

        GifBuilder gifBuilder = new GifBuilder(400, 300)
                .delay(800)  // 每帧800ms
                .loop(true)
                .quality(85);

        // 添加所有测试图片
        for (BufferedImage image : testImages) {
            gifBuilder.addFrame(image);
        }

        // 生成并保存GIF
        File outputFile = new File(TEST_OUTPUT_DIR, "multi-images-test.gif");
        gifBuilder.save(outputFile.getAbsolutePath());

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);

        System.out.println("GIF已保存: " + outputFile.getAbsolutePath());
        System.out.println("文件大小: " + outputFile.length() + " bytes");
        System.out.println("包含帧数: " + testImages.size());
    }

    /**
     * 测试：从本地图片文件路径生成GIF
     */
    @Test
    void testCreateGifFromImagePaths() {
        System.out.println("=== 测试：从本地图片文件路径生成GIF ===");

        // 首先保存测试图片到本地
        List<String> imagePaths = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            File imageFile = new File(TEST_OUTPUT_DIR, "frame_" + i + ".png");
            try {
                javax.imageio.ImageIO.write(testImages.get(i), "PNG", imageFile);
                imagePaths.add(imageFile.getAbsolutePath());
                System.out.println("已保存图片: " + imageFile.getName());
            } catch (Exception e) {
                fail("保存测试图片失败: " + e.getMessage());
            }
        }

        // 从图片路径创建GIF
        GifBuilder gifBuilder = new GifBuilder(400, 300)
                .delay(1000)
                .loop(true);

        // 注意：这里需要实现从路径加载的功能
        for (String path : imagePaths) {
            try {
                BufferedImage image = javax.imageio.ImageIO.read(new File(path));
                gifBuilder.addFrame(image);
            } catch (Exception e) {
                fail("加载图片失败: " + e.getMessage());
            }
        }

        File outputFile = new File(TEST_OUTPUT_DIR, "from-paths-test.gif");
        gifBuilder.save(outputFile.getAbsolutePath());

        assertTrue(outputFile.exists());
        System.out.println("从路径生成的GIF已保存: " + outputFile.getAbsolutePath());
    }

    /**
     * 测试：不同延迟时间的GIF
     */
    @Test
    void testGifWithDifferentDelays() {
        System.out.println("=== 测试：不同延迟时间的GIF ===");

        GifBuilder gifBuilder = new GifBuilder(300, 250)
                .loop(true);

        // 添加不同延迟的帧
        int[] delays = {500, 1000, 1500, 800, 600};
        for (int i = 0; i < Math.min(testImages.size(), delays.length); i++) {
            gifBuilder.addFrame(testImages.get(i), delays[i]);
            System.out.println("添加帧 " + (i + 1) + "，延迟: " + delays[i] + "ms");
        }

        File outputFile = new File(TEST_OUTPUT_DIR, "different-delays.gif");
        gifBuilder.save(outputFile.getAbsolutePath());

        assertTrue(outputFile.exists());
        System.out.println("不同延迟GIF已保存: " + outputFile.getAbsolutePath());
    }

    /**
     * 测试：大量图片生成GIF（压力测试）
     */
    @Test
    void testGifWithManyImages() {
        System.out.println("=== 测试：大量图片生成GIF ===");

        // 创建更多测试图片
        List<BufferedImage> manyImages = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Color color = new Color(
                    (int) (Math.random() * 255),
                    (int) (Math.random() * 255),
                    (int) (Math.random() * 255)
            );
            BufferedImage image = createColoredFrame(color, 200, 150, "帧" + (i + 1));
            manyImages.add(image);
        }

        long startTime = System.currentTimeMillis();

        GifBuilder gifBuilder = new GifBuilder(200, 150)
                .delay(200)  // 快速播放
                .loop(true)
                .quality(70); // 较低质量以减小文件大小

        for (BufferedImage image : manyImages) {
            gifBuilder.addFrame(image);
        }

        File outputFile = new File(TEST_OUTPUT_DIR, "many-frames.gif");
        gifBuilder.save(outputFile.getAbsolutePath());

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertTrue(outputFile.exists());
        System.out.println("20帧GIF生成完成");
        System.out.println("用时: " + duration + "ms");
        System.out.println("文件大小: " + outputFile.length() + " bytes");
        System.out.println("已保存: " + outputFile.getAbsolutePath());
    }


    /**
     * 测试：文字滚动效果GIF
     */
    @Test
    void testScrollingTextGif() {
        System.out.println("=== 测试：文字滚动效果GIF ===");

        String scrollText = "RuoYi框架 - 让开发更简单！";
        int canvasWidth = 400;
        int canvasHeight = 80;

        GifBuilder gifBuilder = new GifBuilder(canvasWidth, canvasHeight)
                .delay(50)  // 流畅滚动
                .loop(true);

        Font font = FontUtils.createBoldFont("微软雅黑", 24);

        // 计算文字宽度
        BufferedImage tempImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D tempG2d = tempImg.createGraphics();
        FontMetrics fm = tempG2d.getFontMetrics(font);
        int textWidth = fm.stringWidth(scrollText);
        tempG2d.dispose();

        // 从右到左滚动
        for (int x = canvasWidth; x > -textWidth; x -= 8) {
            BufferedImage frame = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = frame.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // 深色背景
            g2d.setColor(new Color(30, 30, 30));
            g2d.fillRect(0, 0, canvasWidth, canvasHeight);

            // 绘制文字
            g2d.setColor(Color.YELLOW);
            g2d.setFont(font);
            g2d.drawString(scrollText, x, canvasHeight / 2 + 10);

            g2d.dispose();
            gifBuilder.addFrame(frame);
        }

        File outputFile = new File(TEST_OUTPUT_DIR, "scrolling-text.gif");
        gifBuilder.save(outputFile.getAbsolutePath());

        assertTrue(outputFile.exists());
        System.out.println("文字滚动GIF已保存: " + outputFile.getAbsolutePath());
        System.out.println("总帧数: " + ((canvasWidth + textWidth) / 8));
    }

    /**
     * 测试：图片缩放适配
     */
    @Test
    void testGifWithImageResize() {
        System.out.println("=== 测试：图片缩放适配 ===");

        // 创建不同尺寸的图片
        List<BufferedImage> differentSizeImages = new ArrayList<>();
        int[][] sizes = {{100, 100}, {200, 150}, {300, 200}, {150, 300}, {400, 100}};

        for (int i = 0; i < sizes.length; i++) {
            BufferedImage img = createColoredFrame(
                    testImages.get(i % testImages.size()).getColorModel().getColorSpace().equals(
                            testImages.get(i).getColorModel().getColorSpace()) ? Color.BLUE : Color.RED,
                    sizes[i][0], sizes[i][1],
                    sizes[i][0] + "×" + sizes[i][1]
            );
            differentSizeImages.add(img);
        }

        // 统一缩放到300x200
        GifBuilder gifBuilder = new GifBuilder(300, 200)
                .delay(1000)
                .loop(true)
                .backgroundColor(Color.WHITE);

        for (BufferedImage img : differentSizeImages) {
            // 手动调整图片尺寸以适配
            BufferedImage resized = new BufferedImage(300, 200, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = resized.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            // 白色背景
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, 300, 200);

            // 居中绘制原图（等比缩放）
            double scaleX = 300.0 / img.getWidth();
            double scaleY = 200.0 / img.getHeight();
            double scale = Math.min(scaleX, scaleY);

            int scaledWidth = (int) (img.getWidth() * scale);
            int scaledHeight = (int) (img.getHeight() * scale);
            int x = (300 - scaledWidth) / 2;
            int y = (200 - scaledHeight) / 2;

            g2d.drawImage(img, x, y, scaledWidth, scaledHeight, null);
            g2d.dispose();

            gifBuilder.addFrame(resized);
        }

        File outputFile = new File(TEST_OUTPUT_DIR, "resized-frames.gif");
        gifBuilder.save(outputFile.getAbsolutePath());

        assertTrue(outputFile.exists());
        System.out.println("缩放适配GIF已保存: " + outputFile.getAbsolutePath());
    }

    /**
     * 创建带颜色和文字的测试帧
     */
    private BufferedImage createColoredFrame(Color bgColor, int width, int height, String text) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 背景
        g2d.setColor(bgColor);
        g2d.fillRect(0, 0, width, height);

        // 边框
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRect(5, 5, width - 10, height - 10);

        // 文字
        g2d.setColor(Color.WHITE);
        g2d.setFont(FontUtils.createBoldFont("微软雅黑", Math.min(width, height) / 10));
        FontMetrics fm = g2d.getFontMetrics();
        int textX = (width - fm.stringWidth(text)) / 2;
        int textY = height / 2 + fm.getAscent() / 2;
        g2d.drawString(text, textX, textY);

        // 添加一些装饰
        g2d.setColor(new Color(255, 255, 255, 128));
        for (int i = 0; i < 5; i++) {
            int x = (int) (Math.random() * width);
            int y = (int) (Math.random() * height);
            g2d.fillOval(x, y, 10, 10);
        }

        g2d.dispose();
        return image;
    }
}
