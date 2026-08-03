package plus.ruoyi.common.media.utils;

import plus.ruoyi.common.media.enums.ResizeMode;
import plus.ruoyi.common.media.exception.MediaException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

/**
 * 图片工具类
 * 提供图片加载、转换、缩放和圆形裁剪等常用操作的工具方法。
 */
public class ImageUtils {

    /**
     * 从URL加载图片
     *
     * @param imageUrl 图片的网络地址
     * @return 加载成功的 BufferedImage 对象
     * @throws MediaException 当无法从指定URL加载图片时抛出异常
     */
    public static BufferedImage loadFromUrl(String imageUrl) {
        try {
            URI uri = URI.create(imageUrl);
            // 设置连接和读取超时(各5秒)
            java.net.URLConnection connection = uri.toURL().openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            return ImageIO.read(connection.getInputStream());
        } catch (IOException e) {
            throw new MediaException("无法加载图片: " + imageUrl, e);
        }
    }

    /**
     * 从字节数组加载图片
     *
     * @param imageBytes 图片的字节数组
     * @return 加载成功的 BufferedImage 对象
     * @throws MediaException 当无法从字节数组中加载图片时抛出异常
     */
    public static BufferedImage loadFromBytes(byte[] imageBytes) {
        try {
            return ImageIO.read(new ByteArrayInputStream(imageBytes));
        } catch (IOException e) {
            throw new MediaException("无法从字节数组加载图片", e);
        }
    }

    /**
     * 将 BufferedImage 转换为字节数组
     *
     * @param image  BufferedImage 对象
     * @param format 图片格式（如 "jpg", "png"）
     * @return 图片对应的字节数组
     * @throws MediaException 当图片转换失败时抛出异常
     */
    public static byte[] toBytes(BufferedImage image, String format) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, format, baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new MediaException("图片转换为字节数组失败", e);
        }
    }

    /**
     * 根据指定模式调整图片尺寸
     *
     * @param original      原始图片
     * @param targetWidth   目标宽度
     * @param targetHeight  目标高度
     * @param mode          缩放模式（拉伸、填充、等比）
     * @return 调整后的图片
     */
    public static BufferedImage resize(BufferedImage original, int targetWidth, int targetHeight, ResizeMode mode) {
        if (mode == ResizeMode.STRETCH) {
            return resizeStretch(original, targetWidth, targetHeight);
        } else if (mode == ResizeMode.FILL) {
            return resizeFill(original, targetWidth, targetHeight);
        } else {
            return resizeFit(original, targetWidth, targetHeight);
        }
    }

    /**
     * 等比缩放：保持原图宽高比例，居中放置在目标尺寸画布上，多余部分填充背景色
     *
     * @param original      原始图片
     * @param targetWidth   目标宽度
     * @param targetHeight  目标高度
     * @return 等比缩放后的图片
     */
    private static BufferedImage resizeFit(BufferedImage original, int targetWidth, int targetHeight) {
        double scaleX = (double) targetWidth / original.getWidth();
        double scaleY = (double) targetHeight / original.getHeight();
        double scale = Math.min(scaleX, scaleY);

        int scaledWidth = (int) (original.getWidth() * scale);
        int scaledHeight = (int) (original.getHeight() * scale);

        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, targetWidth, targetHeight);

        int x = (targetWidth - scaledWidth) / 2;
        int y = (targetHeight - scaledHeight) / 2;
        g2d.drawImage(original, x, y, scaledWidth, scaledHeight, null);
        g2d.dispose();

        return resized;
    }

    /**
     * 填充裁剪：按比例缩放至刚好填满目标区域，超出部分被裁剪掉
     *
     * @param original      原始图片
     * @param targetWidth   目标宽度
     * @param targetHeight  目标高度
     * @return 填充裁剪后的图片
     */
    private static BufferedImage resizeFill(BufferedImage original, int targetWidth, int targetHeight) {
        double scaleX = (double) targetWidth / original.getWidth();
        double scaleY = (double) targetHeight / original.getHeight();
        double scale = Math.max(scaleX, scaleY);

        int scaledWidth = (int) (original.getWidth() * scale);
        int scaledHeight = (int) (original.getHeight() * scale);

        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int x = (targetWidth - scaledWidth) / 2;
        int y = (targetHeight - scaledHeight) / 2;
        g2d.drawImage(original, x, y, scaledWidth, scaledHeight, null);
        g2d.dispose();

        return resized;
    }

    /**
     * 拉伸缩放：直接将原始图片拉伸到目标尺寸，可能造成变形
     *
     * @param original      原始图片
     * @param targetWidth   目标宽度
     * @param targetHeight  目标高度
     * @return 拉伸后的图片
     */
    private static BufferedImage resizeStretch(BufferedImage original, int targetWidth, int targetHeight) {
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(original, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();
        return resized;
    }

    /**
     * 创建圆形图片：将原始图片绘制在一个圆形区域内，多余部分被裁剪
     *
     * @param original  原始图片
     * @param diameter  圆形直径
     * @return 圆形图片
     */
    public static BufferedImage createCircleImage(BufferedImage original, int diameter) {
        BufferedImage circleImage = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = circleImage.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setComposite(AlphaComposite.Clear);
        g2d.fill(new Rectangle(diameter, diameter));
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 1.0f));

        Ellipse2D.Double shape = new Ellipse2D.Double(0, 0, diameter, diameter);
        g2d.setClip(shape);
        g2d.drawImage(original, 0, 0, diameter, diameter, null);
        g2d.dispose();

        return circleImage;
    }
}
