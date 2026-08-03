package plus.ruoyi.common.media.builder;

import net.coobird.thumbnailator.Thumbnails;
import plus.ruoyi.common.media.enums.OutputFormat;
import plus.ruoyi.common.media.enums.ResizeMode;
import plus.ruoyi.common.media.exception.MediaException;
import plus.ruoyi.common.media.options.FilterOptions;
import plus.ruoyi.common.media.options.WatermarkOptions;
import plus.ruoyi.common.media.utils.ImageUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 图片处理建造者，支持缩放、裁剪、旋转、水印、滤镜等功能。
 *
 * @author 抓蛙师
 */
public class ImageBuilder {

    private BufferedImage sourceImage;
    private BufferedImage currentImage;
    private OutputFormat outputFormat = OutputFormat.PNG;

    /**
     * 通过 BufferedImage 初始化。
     *
     * @param image 原始图像
     */
    public ImageBuilder(BufferedImage image) {
        this.sourceImage = image;
        this.currentImage = copyImage(image);
    }

    /**
     * 通过本地路径或 URL 加载图像。
     *
     * @param imagePath 图片路径或 URL
     * @throws MediaException 如果加载失败
     */
    public ImageBuilder(String imagePath) {
        try {
            BufferedImage image;
            if (imagePath.startsWith("http")) {
                image = ImageUtils.loadFromUrl(imagePath);
            } else {
                image = ImageIO.read(new File(imagePath));
            }
            this.sourceImage = image;
            this.currentImage = copyImage(image);
        } catch (IOException e) {
            throw new MediaException("加载图片失败: " + imagePath, e);
        }
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 通过 BufferedImage 创建 ImageBuilder 实例
     *
     * @param image 原始图像
     * @return ImageBuilder实例
     */
    public static ImageBuilder of(BufferedImage image) {
        return new ImageBuilder(image);
    }

    /**
     * 通过本地路径或 URL 创建 ImageBuilder 实例
     *
     * @param imagePath 图片路径或 URL
     * @return ImageBuilder实例
     */
    public static ImageBuilder of(String imagePath) {
        return new ImageBuilder(imagePath);
    }

    /**
     * 通过字节数组创建 ImageBuilder 实例
     *
     * @param imageBytes 图片字节数组
     * @return ImageBuilder实例
     * @throws MediaException 如果解析字节数组失败
     */
    public static ImageBuilder of(byte[] imageBytes) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes)) {
            BufferedImage image = ImageIO.read(bais);
            if (image == null) {
                throw new MediaException("无法解析图片字节数组");
            }
            return new ImageBuilder(image);
        } catch (IOException e) {
            throw new MediaException("解析图片字节数组失败", e);
        }
    }

    /**
     * 通过输入流创建 ImageBuilder 实例
     *
     * @param inputStream 图片输入流
     * @return ImageBuilder实例
     * @throws MediaException 如果读取输入流失败
     */
    public static ImageBuilder of(InputStream inputStream) {
        try {
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                throw new MediaException("无法读取输入流中的图片");
            }
            return new ImageBuilder(image);
        } catch (IOException e) {
            throw new MediaException("读取图片输入流失败", e);
        }
    }

    // ==================== 链式配置方法 ====================

    /**
     * 重置为原始图像。
     *
     * @return 当前构建器实例
     */
    public ImageBuilder reset() {
        this.currentImage = copyImage(sourceImage);
        return this;
    }

    /**
     * 按指定尺寸调整图像大小，默认使用 FIT 模式。
     *
     * @param width  目标宽度
     * @param height 目标高度
     * @return 当前构建器实例
     */
    public ImageBuilder resize(int width, int height) {
        return resize(width, height, ResizeMode.FIT);
    }

    /**
     * 按指定模式和尺寸调整图像大小。
     *
     * @param width  目标宽度
     * @param height 目标高度
     * @param mode   缩放模式
     * @return 当前构建器实例
     */
    public ImageBuilder resize(int width, int height, ResizeMode mode) {
        currentImage = ImageUtils.resize(currentImage, width, height, mode);
        return this;
    }

    /**
     * 按比例缩放图像。
     *
     * @param scale 缩放比例
     * @return 当前构建器实例
     */
    public ImageBuilder scale(double scale) {
        int newWidth = (int) (currentImage.getWidth() * scale);
        int newHeight = (int) (currentImage.getHeight() * scale);
        return resize(newWidth, newHeight, ResizeMode.STRETCH);
    }

    /**
     * 裁剪图像。
     *
     * @param x      起始 x 坐标
     * @param y      起始 y 坐标
     * @param width  裁剪宽度
     * @param height 裁剪高度
     * @return 当前构建器实例
     */
    public ImageBuilder crop(int x, int y, int width, int height) {
        try {
            currentImage = currentImage.getSubimage(x, y, width, height);
        } catch (Exception e) {
            throw new MediaException("裁剪图片失败", e);
        }
        return this;
    }

    /**
     * 旋转图像。
     *
     * @param angle 旋转角度（单位：度）
     * @return 当前构建器实例
     */
    public ImageBuilder rotate(double angle) {
        int width = currentImage.getWidth();
        int height = currentImage.getHeight();

        BufferedImage rotated = new BufferedImage(width, height, currentImage.getType());
        Graphics2D g2d = rotated.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.rotate(Math.toRadians(angle), width / 2.0, height / 2.0);
        g2d.drawImage(currentImage, 0, 0, null);
        g2d.dispose();

        currentImage = rotated;
        return this;
    }

    /**
     * 水平翻转图像。
     *
     * @return 当前构建器实例
     */
    public ImageBuilder flipHorizontal() {
        int width = currentImage.getWidth();
        int height = currentImage.getHeight();

        BufferedImage flipped = new BufferedImage(width, height, currentImage.getType());
        Graphics2D g2d = flipped.createGraphics();
        g2d.drawImage(currentImage, width, 0, -width, height, null);
        g2d.dispose();

        currentImage = flipped;
        return this;
    }

    /**
     * 垂直翻转图像。
     *
     * @return 当前构建器实例
     */
    public ImageBuilder flipVertical() {
        int width = currentImage.getWidth();
        int height = currentImage.getHeight();

        BufferedImage flipped = new BufferedImage(width, height, currentImage.getType());
        Graphics2D g2d = flipped.createGraphics();
        g2d.drawImage(currentImage, 0, height, width, -height, null);
        g2d.dispose();

        currentImage = flipped;
        return this;
    }

    /**
     * 添加水印（文字或图片）。
     *
     * @param options 水印配置
     * @return 当前构建器实例
     */
    public ImageBuilder addWatermark(WatermarkOptions options) {
        if (options.getText() != null) {
            addTextWatermark(options);
        } else if (options.getImagePath() != null) {
            addImageWatermark(options);
        }
        return this;
    }

    /**
     * 应用滤镜（灰度、亮度、对比度等）。
     *
     * @param options 滤镜配置
     * @return 当前构建器实例
     */
    public ImageBuilder applyFilter(FilterOptions options) {
        if (options.isGrayscale()) {
            toGrayscale();
        }

        if (options.getBrightness() != 0 || options.getContrast() != 1.0f) {
            adjustBrightnessContrast(options.getBrightness(), options.getContrast());
        }

        return this;
    }

    /**
     * 设置输出格式。
     *
     * @param format 输出格式
     * @return 当前构建器实例
     */
    public ImageBuilder format(OutputFormat format) {
        this.outputFormat = format;
        return this;
    }

    /**
     * 使用 Thumbnailator 进行高质量处理。
     *
     * @return 当前构建器实例
     */
    public ImageBuilder withThumbnailator() {
        try {
            currentImage = Thumbnails.of(currentImage)
                .scale(1.0)
                .asBufferedImage();
        } catch (IOException e) {
            throw new MediaException("Thumbnailator处理失败", e);
        }
        return this;
    }

    // ==================== 构建与输出方法 ====================

    /**
     * 构建处理后的图像。
     *
     * @return 处理后的 BufferedImage
     */
    public BufferedImage build() {
        return currentImage;
    }

    /**
     * 转换为字节数组。
     *
     * @return 图像字节数据
     */
    public byte[] toBytes() {
        return ImageUtils.toBytes(currentImage, outputFormat.getExtension());
    }

    /**
     * 构建图像并返回输入流。
     * <p>
     * 该方法适用于需要将图像数据作为流处理的场景，比如：
     * <ul>
     *   <li>上传到OSS等云存储服务</li>
     *   <li>通过HTTP响应返回</li>
     *   <li>写入到其他输出流</li>
     * </ul>
     * <p>
     * 注意：返回的输入流使用完毕后请记得关闭。
     *
     * @return 图像数据的输入流
     * @throws MediaException 如果生成失败
     */
    public InputStream toInputStream() {
        return new ByteArrayInputStream(toBytes());
    }

    /**
     * 构建图像并写入到指定的输出流。
     * <p>
     * 该方法适用于直接将图像数据写入到目标流的场景，避免了中间字节数组的创建，
     * 对于大型图像更加内存友好。
     * <p>
     * 注意：该方法不会关闭传入的输出流，调用者需要自行管理流的生命周期。
     *
     * @param outputStream 目标输出流
     * @throws MediaException 如果写入失败
     * @throws IllegalArgumentException 如果输出流为null
     */
    public void writeTo(OutputStream outputStream) {
        if (outputStream == null) {
            throw new IllegalArgumentException("输出流不能为null");
        }

        try {
            if (outputFormat == OutputFormat.JPG || outputFormat == OutputFormat.JPEG) {
                // JPEG需要转换为RGB格式
                BufferedImage rgbImage = new BufferedImage(
                    currentImage.getWidth(), currentImage.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = rgbImage.createGraphics();
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, currentImage.getWidth(), currentImage.getHeight());
                g2d.drawImage(currentImage, 0, 0, null);
                g2d.dispose();
                ImageIO.write(rgbImage, outputFormat.getExtension(), outputStream);
            } else {
                ImageIO.write(currentImage, outputFormat.getExtension(), outputStream);
            }
        } catch (IOException e) {
            throw new MediaException("写入图像数据失败", e);
        }
    }

    /**
     * 保存图像到指定路径。
     *
     * @param filePath 文件路径
     * @throws MediaException 如果保存失败
     */
    public void save(String filePath) {
        try {
            File file = new File(filePath);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            if (outputFormat == OutputFormat.JPG || outputFormat == OutputFormat.JPEG) {
                // JPEG需要转换为RGB格式
                BufferedImage rgbImage = new BufferedImage(
                    currentImage.getWidth(), currentImage.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = rgbImage.createGraphics();
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, currentImage.getWidth(), currentImage.getHeight());
                g2d.drawImage(currentImage, 0, 0, null);
                g2d.dispose();
                ImageIO.write(rgbImage, outputFormat.getExtension(), file);
            } else {
                ImageIO.write(currentImage, outputFormat.getExtension(), file);
            }
        } catch (IOException e) {
            throw new MediaException("保存图片失败", e);
        }
    }

    // ==================== 便利方法 ====================

    /**
     * 获取当前图像的宽度
     *
     * @return 图像宽度（像素）
     */
    public int getWidth() {
        return currentImage.getWidth();
    }

    /**
     * 获取当前图像的高度
     *
     * @return 图像高度（像素）
     */
    public int getHeight() {
        return currentImage.getHeight();
    }

    /**
     * 获取当前图像的尺寸信息
     *
     * @return Dimension对象，包含宽度和高度
     */
    public Dimension getSize() {
        return new Dimension(currentImage.getWidth(), currentImage.getHeight());
    }

    /**
     * 获取构建后的图像数据大小（字节数）
     * <p>
     * 注意：该方法会触发图像构建过程，如果之后还需要使用构建结果，
     * 建议先调用toBytes()方法保存结果，避免重复构建。
     *
     * @return 图像数据的字节数
     */
    public long getDataSize() {
        return toBytes().length;
    }

    // ==================== 私有辅助方法 ====================

    private BufferedImage copyImage(BufferedImage original) {
        BufferedImage copy = new BufferedImage(
            original.getWidth(), original.getHeight(), original.getType());
        Graphics2D g2d = copy.createGraphics();
        g2d.drawImage(original, 0, 0, null);
        g2d.dispose();
        return copy;
    }

    private void addTextWatermark(WatermarkOptions options) {
        Graphics2D g2d = currentImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2d.setFont(options.getFont());
        Color watermarkColor = new Color(
            options.getTextColor().getRed(),
            options.getTextColor().getGreen(),
            options.getTextColor().getBlue(),
            (int) (255 * options.getOpacity())
        );
        g2d.setColor(watermarkColor);

        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(options.getText());
        int textHeight = fm.getHeight();

        Point position = calculatePosition(options.getPosition(),
            currentImage.getWidth(), currentImage.getHeight(),
            textWidth, textHeight, options.getOffsetX(), options.getOffsetY());

        g2d.drawString(options.getText(), position.x, position.y + fm.getAscent());
        g2d.dispose();
    }

    private void addImageWatermark(WatermarkOptions options) {
        try {
            BufferedImage watermark;
            if (options.getImagePath().startsWith("http")) {
                watermark = ImageUtils.loadFromUrl(options.getImagePath());
            } else {
                watermark = ImageIO.read(new File(options.getImagePath()));
            }

            if (options.isAutoSize()) {
                int maxSize = Math.min(currentImage.getWidth(), currentImage.getHeight()) / 4;
                if (watermark.getWidth() > maxSize || watermark.getHeight() > maxSize) {
                    watermark = ImageUtils.resize(watermark, maxSize, maxSize, ResizeMode.FIT);
                }
            } else {
                watermark = ImageUtils.resize(watermark, options.getFixedWidth(), options.getFixedHeight(), ResizeMode.FIT);
            }

            Point position = calculatePosition(options.getPosition(),
                currentImage.getWidth(), currentImage.getHeight(),
                watermark.getWidth(), watermark.getHeight(),
                options.getOffsetX(), options.getOffsetY());

            Graphics2D g2d = currentImage.createGraphics();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, options.getOpacity()));
            g2d.drawImage(watermark, position.x, position.y, null);
            g2d.dispose();

        } catch (IOException e) {
            throw new MediaException("添加图片水印失败", e);
        }
    }

    private Point calculatePosition(WatermarkOptions.Position position,
                                  int canvasWidth, int canvasHeight,
                                  int itemWidth, int itemHeight,
                                  int offsetX, int offsetY) {
        int x, y;

        switch (position) {
            case TOP_LEFT:
                x = offsetX;
                y = offsetY;
                break;
            case TOP_CENTER:
                x = (canvasWidth - itemWidth) / 2 + offsetX;
                y = offsetY;
                break;
            case TOP_RIGHT:
                x = canvasWidth - itemWidth - offsetX;
                y = offsetY;
                break;
            case CENTER_LEFT:
                x = offsetX;
                y = (canvasHeight - itemHeight) / 2 + offsetY;
                break;
            case CENTER:
                x = (canvasWidth - itemWidth) / 2 + offsetX;
                y = (canvasHeight - itemHeight) / 2 + offsetY;
                break;
            case CENTER_RIGHT:
                x = canvasWidth - itemWidth - offsetX;
                y = (canvasHeight - itemHeight) / 2 + offsetY;
                break;
            case BOTTOM_LEFT:
                x = offsetX;
                y = canvasHeight - itemHeight - offsetY;
                break;
            case BOTTOM_CENTER:
                x = (canvasWidth - itemWidth) / 2 + offsetX;
                y = canvasHeight - itemHeight - offsetY;
                break;
            case BOTTOM_RIGHT:
            default:
                x = canvasWidth - itemWidth - offsetX;
                y = canvasHeight - itemHeight - offsetY;
                break;
        }

        return new Point(x, y);
    }

    private void toGrayscale() {
        ColorConvertOp op = new ColorConvertOp(
            currentImage.getColorModel().getColorSpace(),
            ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        currentImage = op.filter(currentImage, null);
    }

    private void adjustBrightnessContrast(int brightness, float contrast) {
        int width = currentImage.getWidth();
        int height = currentImage.getHeight();

        BufferedImage adjusted = new BufferedImage(width, height, currentImage.getType());

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color color = new Color(currentImage.getRGB(x, y));

                int r = adjustChannel(color.getRed(), brightness, contrast);
                int g = adjustChannel(color.getGreen(), brightness, contrast);
                int b = adjustChannel(color.getBlue(), brightness, contrast);

                adjusted.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }

        currentImage = adjusted;
    }

    private int adjustChannel(int channel, int brightness, float contrast) {
        int adjusted = (int) ((channel - 128) * contrast + 128 + brightness);
        return Math.max(0, Math.min(255, adjusted));
    }
}
