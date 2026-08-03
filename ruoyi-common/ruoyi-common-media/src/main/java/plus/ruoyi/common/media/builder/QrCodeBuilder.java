package plus.ruoyi.common.media.builder;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.media.exception.MediaException;
import plus.ruoyi.common.media.options.QrCodeOptions;
import plus.ruoyi.common.media.utils.ImageUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * 二维码构建器
 * <p>
 * 提供二维码的生成、保存和转换为字节数组的功能，支持设置尺寸、边距、颜色、Logo等选项。
 *
 * @author 抓蛙师
 */
@Slf4j
public class QrCodeBuilder{

    /**
     * 二维码内容
     */
    private String content;

    /**
     * 二维码配置选项
     */
    private QrCodeOptions options;

    /**
     * 构造函数，初始化二维码内容并使用默认配置
     *
     * @param content 二维码内容
     */
    public QrCodeBuilder(String content) {
        this.content = content;
        this.options = QrCodeOptions.defaults();
    }

    /**
     * 设置二维码尺寸
     *
     * @param size 尺寸大小（单位：像素）
     * @return 当前构建器实例，用于链式调用
     */
    public QrCodeBuilder size(int size) {
        options.size(size);
        return this;
    }

    /**
     * 设置二维码边距
     *
     * @param margin 边距大小（单位：像素）
     * @return 当前构建器实例，用于链式调用
     */
    public QrCodeBuilder margin(int margin) {
        options.margin(margin);
        return this;
    }

    /**
     * 设置前景色和背景色
     *
     * @param foreground 前景色（二维码图案颜色）
     * @param background 背景色（二维码背景颜色）
     * @return 当前构建器实例，用于链式调用
     */
    public QrCodeBuilder colors(Color foreground, Color background) {
        options.colors(foreground, background);
        return this;
    }

    /**
     * 设置Logo图片路径
     *
     * @param logoPath Logo图片路径（本地路径或URL）
     * @return 当前构建器实例，用于链式调用
     */
    public QrCodeBuilder logo(String logoPath) {
        options.logo(logoPath);
        return this;
    }

    /**
     * 设置Logo图片路径及尺寸比例
     *
     * @param logoPath   Logo图片路径（本地路径或URL）
     * @param sizeRatio  Logo相对于二维码的尺寸比例（0~1之间）
     * @return 当前构建器实例，用于链式调用
     */
    public QrCodeBuilder logo(String logoPath, float sizeRatio) {
        options.logo(logoPath, sizeRatio);
        return this;
    }

    /**
     * 构建二维码图像
     *
     * @return 生成的二维码图像对象
     * @throws MediaException 生成失败时抛出异常
     */
    public BufferedImage build() {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE,
                options.getSize(), options.getSize(), options.getEncodeHints());

            BufferedImage qrImage = createQrImage(bitMatrix, options);

            // 如果有Logo，添加Logo
            if (options.getLogoPath() != null && !options.getLogoPath().trim().isEmpty()) {
                qrImage = addLogo(qrImage, options);
            }

            return qrImage;
        } catch (WriterException e) {
            throw new MediaException("生成二维码失败", e);
        }
    }

    /**
     * 将二维码图像转换为字节数组（PNG格式）
     *
     * @return 二维码图像的字节数组表示
     */
    public byte[] toBytes() {
        return ImageUtils.toBytes(build(), "PNG");
    }

    /**
     * 将二维码图像保存到指定文件路径
     *
     * @param filePath 文件保存路径
     * @throws MediaException 保存失败时抛出异常
     */
    public void save(String filePath) {
        try {
            BufferedImage qrImage = build();
            File file = new File(filePath);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            ImageIO.write(qrImage, "PNG", file);
        } catch (IOException e) {
            throw new MediaException("保存二维码失败", e);
        }
    }

    /**
     * 根据位矩阵创建二维码图像
     *
     * @param bitMatrix 二维码位矩阵
     * @param options   二维码配置选项
     * @return 生成的二维码图像
     */
    private BufferedImage createQrImage(BitMatrix bitMatrix, QrCodeOptions options) {
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int foregroundRGB = options.getForegroundColor().getRGB();
        int backgroundRGB = options.getBackgroundColor().getRGB();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? foregroundRGB : backgroundRGB);
            }
        }

        return image;
    }

    /**
     * 在二维码中央添加Logo图像
     *
     * @param qrImage 二维码图像
     * @param options 二维码配置选项
     * @return 添加Logo后的二维码图像
     */
    private BufferedImage addLogo(BufferedImage qrImage, QrCodeOptions options) {
        try {
            BufferedImage logo;
            if (options.getLogoPath().startsWith("http")) {
                logo = ImageUtils.loadFromUrl(options.getLogoPath());
            } else {
                logo = ImageIO.read(new File(options.getLogoPath()));
            }

            int qrSize = qrImage.getWidth();
            int logoSize = (int) (qrSize * options.getLogoSizeRatio());

            // 缩放Logo
            BufferedImage scaledLogo = new BufferedImage(logoSize, logoSize, BufferedImage.TYPE_INT_ARGB);
            Graphics2D logoG2d = scaledLogo.createGraphics();
            logoG2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            logoG2d.drawImage(logo, 0, 0, logoSize, logoSize, null);
            logoG2d.dispose();

            // 创建圆角Logo
            BufferedImage roundedLogo = createRoundedLogo(scaledLogo, logoSize);

            // 合并到二维码
            Graphics2D qrG2d = qrImage.createGraphics();
            qrG2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int logoX = (qrSize - logoSize) / 2;
            int logoY = (qrSize - logoSize) / 2;

            // 绘制白色背景圆角矩形
            int padding = logoSize / 8;
            qrG2d.setColor(Color.WHITE);
            qrG2d.fill(new RoundRectangle2D.Float(logoX - padding, logoY - padding,
                logoSize + 2 * padding, logoSize + 2 * padding, padding, padding));

            // 绘制Logo
            qrG2d.drawImage(roundedLogo, logoX, logoY, null);
            qrG2d.dispose();

            return qrImage;
        } catch (Exception e) {
            log.warn("添加Logo失败: {}", e.getMessage());
            return qrImage; // 返回原二维码
        }
    }

    /**
     * 创建圆角Logo图像
     *
     * @param logo Logo原始图像
     * @param size Logo尺寸
     * @return 圆角处理后的Logo图像
     */
    private BufferedImage createRoundedLogo(BufferedImage logo, int size) {
        BufferedImage rounded = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rounded.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int cornerRadius = size / 8;
        RoundRectangle2D.Float roundRect = new RoundRectangle2D.Float(0, 0, size, size,
            cornerRadius, cornerRadius);

        g2d.setClip(roundRect);
        g2d.drawImage(logo, 0, 0, null);
        g2d.dispose();

        return rounded;
    }
}
