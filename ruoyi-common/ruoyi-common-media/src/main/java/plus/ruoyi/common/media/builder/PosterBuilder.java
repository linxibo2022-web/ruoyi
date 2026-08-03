package plus.ruoyi.common.media.builder;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.media.enums.OutputFormat;
import plus.ruoyi.common.media.enums.PosterItemType;
import plus.ruoyi.common.media.exception.MediaException;
import plus.ruoyi.common.media.model.PosterItem;
import plus.ruoyi.common.media.utils.ColorUtils;
import plus.ruoyi.common.media.utils.FontUtils;
import plus.ruoyi.common.media.utils.ImageUtils;
import plus.ruoyi.common.media.utils.QrCodeUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 海报构建器，用于动态生成海报图像。
 * 支持添加文本、图片、二维码等元素，并可将结果输出为字节数组、保存为文件或直接写入HTTP响应。
 *
 * @author 抓蛙师
 */
public class PosterBuilder {
    private String bgUrl;
    private Color bgColor;
    private int width;
    private int height;
    private List<PosterItem> posterItems;
    private OutputFormat outputFormat;
    private BufferedImage result;

    /**
     * 构造一个指定尺寸的海报构建器，默认背景为白色，输出格式为PNG。
     *
     * @param width  海报宽度（像素）
     * @param height 海报高度（像素）
     */
    public PosterBuilder(int width, int height) {
        this(null, Color.WHITE, width, height, OutputFormat.PNG);
    }

    /**
     * 构造一个指定背景图片URL和尺寸的海报构建器，默认输出格式为PNG。
     *
     * @param bgUrl  背景图片URL
     * @param width  海报宽度（像素）
     * @param height 海报高度（像素）
     */
    public PosterBuilder(String bgUrl, int width, int height) {
        this(bgUrl, null, width, height, OutputFormat.PNG);
    }

    /**
     * 构造一个指定背景颜色和尺寸的海报构建器，默认输出格式为PNG。
     *
     * @param bgColor 背景颜色
     * @param width   海报宽度（像素）
     * @param height  海报高度（像素）
     */
    public PosterBuilder(Color bgColor, int width, int height) {
        this(null, bgColor, width, height, OutputFormat.PNG);
    }

    /**
     * 构造一个完整的海报构建器实例。
     *
     * @param bgUrl        背景图片URL（可选）
     * @param bgColor      背景颜色（当bgUrl为空时使用）
     * @param width        海报宽度（像素）
     * @param height       海报高度（像素）
     * @param outputFormat 输出格式（如PNG、JPEG）
     */
    public PosterBuilder(String bgUrl, Color bgColor, int width, int height, OutputFormat outputFormat) {
        this.bgUrl = bgUrl;
        this.bgColor = bgColor == null ? Color.WHITE : bgColor;
        this.width = width;
        this.height = height;
        this.outputFormat = outputFormat == null ? OutputFormat.PNG : outputFormat;
        this.posterItems = new ArrayList<>();
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 创建指定尺寸的海报构建器，默认白色背景
     *
     * @param width  海报宽度（像素）
     * @param height 海报高度（像素）
     * @return PosterBuilder实例
     */
    public static PosterBuilder of(int width, int height) {
        return new PosterBuilder(width, height);
    }

    /**
     * 创建带背景图片的海报构建器
     *
     * @param bgUrl  背景图片URL
     * @param width  海报宽度（像素）
     * @param height 海报高度（像素）
     * @return PosterBuilder实例
     */
    public static PosterBuilder of(String bgUrl, int width, int height) {
        return new PosterBuilder(bgUrl, width, height);
    }

    /**
     * 创建带背景颜色的海报构建器
     *
     * @param bgColor 背景颜色
     * @param width   海报宽度（像素）
     * @param height  海报高度（像素）
     * @return PosterBuilder实例
     */
    public static PosterBuilder of(Color bgColor, int width, int height) {
        return new PosterBuilder(bgColor, width, height);
    }

    /**
     * 创建完整配置的海报构建器
     *
     * @param bgUrl        背景图片URL（可选）
     * @param bgColor      背景颜色（当bgUrl为空时使用）
     * @param width        海报宽度（像素）
     * @param height       海报高度（像素）
     * @param outputFormat 输出格式（如PNG、JPEG）
     * @return PosterBuilder实例
     */
    public static PosterBuilder of(String bgUrl, Color bgColor, int width, int height, OutputFormat outputFormat) {
        return new PosterBuilder(bgUrl, bgColor, width, height, outputFormat);
    }

    /**
     * 快速创建常用尺寸的海报构建器 - A4纸张比例 (210:297)
     *
     * @param scale 缩放倍数（1.0表示600x848像素）
     * @return PosterBuilder实例
     */
    public static PosterBuilder ofA4(double scale) {
        int width = (int) (600 * scale);
        int height = (int) (848 * scale);
        return new PosterBuilder(width, height);
    }

    /**
     * 快速创建常用尺寸的海报构建器 - 正方形
     *
     * @param size 正方形边长（像素）
     * @return PosterBuilder实例
     */
    public static PosterBuilder ofSquare(int size) {
        return new PosterBuilder(size, size);
    }

    /**
     * 快速创建常用尺寸的海报构建器 - 16:9宽屏比例
     *
     * @param width 宽度（像素）
     * @return PosterBuilder实例
     */
    public static PosterBuilder of16x9(int width) {
        int height = width * 9 / 16;
        return new PosterBuilder(width, height);
    }

    /**
     * 快速创建常用尺寸的海报构建器 - 4:3经典比例
     *
     * @param width 宽度（像素）
     * @return PosterBuilder实例
     */
    public static PosterBuilder of4x3(int width) {
        int height = width * 3 / 4;
        return new PosterBuilder(width, height);
    }

    /**
     * 快速创建移动端海报构建器 - 9:16竖屏比例
     *
     * @param width 宽度（像素）
     * @return PosterBuilder实例
     */
    public static PosterBuilder ofMobile(int width) {
        int height = width * 16 / 9;
        return new PosterBuilder(width, height);
    }

    // ==================== 链式配置方法 ====================

    /**
     * 设置背景图片URL。
     *
     * @param bgUrl 背景图片URL
     * @return 当前PosterBuilder实例，支持链式调用
     */
    public PosterBuilder background(String bgUrl) {
        this.bgUrl = bgUrl;
        return this;
    }

    /**
     * 设置背景颜色。
     *
     * @param bgColor 背景颜色
     * @return 当前PosterBuilder实例，支持链式调用
     */
    public PosterBuilder background(Color bgColor) {
        this.bgColor = bgColor;
        return this;
    }

    /**
     * 设置输出格式。
     *
     * @param format 输出格式（如PNG、JPEG）
     * @return 当前PosterBuilder实例，支持链式调用
     */
    public PosterBuilder format(OutputFormat format) {
        this.outputFormat = format;
        return this;
    }

    // ==================== 文本元素添加方法 ====================

    /**
     * 添加文本元素到海报中。
     *
     * @param text  文本内容
     * @param font  字体样式
     * @param color 文本颜色
     * @param x     文本左上角X坐标
     * @param y     文本基线Y坐标
     * @return 当前PosterBuilder实例，支持链式调用
     */
    public PosterBuilder addText(String text, Font font, Color color, int x, int y) {
        posterItems.add(new PosterItem(PosterItemType.TEXT, font, color, x, y, text));
        return this;
    }

    /**
     * 使用默认字体添加文本元素。
     *
     * @param text     文本内容
     * @param fontSize 字体大小
     * @param colorStr 颜色字符串（如 "#FF0000"）
     * @param x        文本左上角X坐标
     * @param y        文本基线Y坐标
     * @return 当前PosterBuilder实例，支持链式调用
     */
    public PosterBuilder addText(String text, int fontSize, String colorStr, int x, int y) {
        Font font = FontUtils.createDefaultFont(fontSize);
        Color color = ColorUtils.parseColor(colorStr);
        return addText(text, font, color, x, y);
    }

    /**
     * 使用自定义字体名称添加文本元素。
     *
     * @param text     文本内容
     * @param fontName 字体名称
     * @param fontSize 字体大小
     * @param colorStr 颜色字符串（如 "#FF0000"）
     * @param x        文本左上角X坐标
     * @param y        文本基线Y坐标
     * @return 当前PosterBuilder实例，支持链式调用
     */
    public PosterBuilder addText(String text, String fontName, int fontSize, String colorStr, int x, int y) {
        Font font = FontUtils.createFont(fontName, Font.PLAIN, fontSize);
        Color color = ColorUtils.parseColor(colorStr);
        return addText(text, font, color, x, y);
    }

    /**
     * 添加居中文本
     *
     * @param text     文本内容
     * @param fontSize 字体大小
     * @param colorStr 颜色字符串
     * @param y        文本基线Y坐标
     * @return 当前PosterBuilder实例，支持链式调用
     */
    public PosterBuilder addCenterText(String text, int fontSize, String colorStr, int y) {
        Font font = FontUtils.createDefaultFont(fontSize);
        Color color = ColorUtils.parseColor(colorStr);

        // 创建临时Graphics2D来计算文本宽度
        BufferedImage tempImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D tempG2d = tempImage.createGraphics();
        tempG2d.setFont(font);
        FontMetrics fm = tempG2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        tempG2d.dispose();

        int x = (width - textWidth) / 2;
        return addText(text, font, color, x, y);
    }

    /**
     * 添加多行文本（自动换行）
     *
     * @param text       文本内容
     * @param fontSize   字体大小
     * @param colorStr   颜色字符串
     * @param x          起始X坐标
     * @param y          起始Y坐标
     * @param maxWidth   最大宽度
     * @param lineHeight 行高
     * @return 当前PosterBuilder实例，支持链式调用
     */
    public PosterBuilder addMultiLineText(String text, int fontSize, String colorStr, int x, int y, int maxWidth, int lineHeight) {
        Font font = FontUtils.createDefaultFont(fontSize);
        Color color = ColorUtils.parseColor(colorStr);

        // 简单的文本换行处理
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        int currentY = y;

        BufferedImage tempImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D tempG2d = tempImage.createGraphics();
        tempG2d.setFont(font);
        FontMetrics fm = tempG2d.getFontMetrics();

        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            if (fm.stringWidth(testLine) > maxWidth && currentLine.length() > 0) {
                addText(currentLine.toString(), font, color, x, currentY);
                currentLine = new StringBuilder(word);
                currentY += lineHeight;
            } else {
                currentLine = new StringBuilder(testLine);
            }
        }

        if (currentLine.length() > 0) {
            addText(currentLine.toString(), font, color, x, currentY);
        }

        tempG2d.dispose();
        return this;
    }

    // ==================== 图片元素添加方法 ====================

    /**
     * 添加矩形图片元素到海报中。
     *
     * @param imageUrl 图片URL
     * @param x        图片左上角X坐标
     * @param y        图片左上角Y坐标
     * @param width    图片宽度
     * @param height   图片高度
     * @return 当前PosterBuilder实例，支持链式调用
     */
    public PosterBuilder addImage(String imageUrl, int x, int y, int width, int height) {
        posterItems.add(new PosterItem(PosterItemType.IMAGE, x, y, width, height, false, imageUrl));
        return this;
    }

    /**
     * 添加圆形图片元素到海报中。
     *
     * @param imageUrl 图片URL
     * @param x        图片中心X坐标
     * @param y        图片中心Y坐标
     * @param diameter 圆形直径
     * @return 当前PosterBuilder实例，支持链式调用
     */
    public PosterBuilder addCircleImage(String imageUrl, int x, int y, int diameter) {
        posterItems.add(new PosterItem(PosterItemType.IMAGE, x, y, diameter, diameter, true, imageUrl));
        return this;
    }

    /**
     * 添加居中图片
     *
     * @param imageUrl 图片URL
     * @param y        图片顶部Y坐标
     * @param width    图片宽度
     * @param height   图片高度
     * @return 当前PosterBuilder实例，支持链式调用
     */
    public PosterBuilder addCenterImage(String imageUrl, int y, int width, int height) {
        int x = (this.width - width) / 2;
        return addImage(imageUrl, x, y, width, height);
    }

    // ==================== 二维码元素添加方法 ====================

    /**
     * 添加二维码元素到海报中。
     *
     * @param content 二维码内容（通常是URL）
     * @param x       二维码左上角X坐标
     * @param y       二维码左上角Y坐标
     * @param size    二维码尺寸（正方形）
     * @return 当前PosterBuilder实例，支持链式调用
     */
    public PosterBuilder addQrCode(String content, int x, int y, int size) {
        posterItems.add(new PosterItem(PosterItemType.QRCODE, x, y, size, size, content));
        return this;
    }

    /**
     * 添加居中二维码
     *
     * @param content 二维码内容
     * @param y       二维码顶部Y坐标
     * @param size    二维码尺寸
     * @return 当前PosterBuilder实例，支持链式调用
     */
    public PosterBuilder addCenterQrCode(String content, int y, int size) {
        int x = (width - size) / 2;
        return addQrCode(content, x, y, size);
    }

    // ==================== 几何图形添加方法 ====================

    /**
     * 添加矩形
     *
     * @param x        矩形左上角X坐标
     * @param y        矩形左上角Y坐标
     * @param width    矩形宽度
     * @param height   矩形高度
     * @param colorStr 填充颜色字符串
     * @return 当前PosterBuilder实例，支持链式调用
     */
    public PosterBuilder addRectangle(int x, int y, int width, int height, String colorStr) {
        posterItems.add(new PosterItem(PosterItemType.RECTANGLE, x, y, width, height, colorStr));
        return this;
    }

    /**
     * 添加圆形
     *
     * @param centerX  圆心X坐标
     * @param centerY  圆心Y坐标
     * @param diameter 直径
     * @param colorStr 填充颜色字符串
     * @return 当前PosterBuilder实例，支持链式调用
     */
    public PosterBuilder addCircle(int centerX, int centerY, int diameter, String colorStr) {
        int x = centerX - diameter / 2;
        int y = centerY - diameter / 2;
        posterItems.add(new PosterItem(PosterItemType.CIRCLE, x, y, diameter, diameter, colorStr));
        return this;
    }

    // ==================== 便利方法 ====================

    /**
     * 清除所有元素
     *
     * @return 当前PosterBuilder实例，支持链式调用
     */
    public PosterBuilder clear() {
        posterItems.clear();
        return this;
    }

    /**
     * 获取海报尺寸信息
     *
     * @return 包含宽度和高度的Dimension对象
     */
    public Dimension getSize() {
        return new Dimension(width, height);
    }

    /**
     * 获取元素数量
     *
     * @return 当前添加的元素数量
     */
    public int getElementCount() {
        return posterItems.size();
    }

    // ==================== 构建与输出方法 ====================

    /**
     * 构建并返回最终的海报图像。
     *
     * @return 构建完成的BufferedImage对象
     * @throws MediaException 如果构建过程中发生异常
     */
    public BufferedImage build() {
        try {
            result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = result.createGraphics();

            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (StringUtils.isBlank(bgUrl)) {
                g2d.setColor(bgColor);
                g2d.fillRect(0, 0, width, height);
            } else {
                BufferedImage bgImage = ImageUtils.loadFromUrl(bgUrl);
                g2d.drawImage(bgImage, 0, 0, width, height, null);
            }

            for (PosterItem item : posterItems) {
                drawItem(g2d, item);
            }

            g2d.dispose();
            return result;
        } catch (Exception e) {
            throw new MediaException("海报生成失败", e);
        }
    }

    /**
     * 将构建好的海报转换为字节数组。
     *
     * @return 海报图像的字节数组表示
     * @throws MediaException 如果转换过程中发生异常
     */
    public byte[] toBytes() {
        if (result == null) {
            build();
        }
        return ImageUtils.toBytes(result, outputFormat.getExtension());
    }

    /**
     * 构建海报并返回输入流
     * <p>
     * 适用于需要将海报数据作为流处理的场景，比如上传到OSS等云存储服务
     *
     * @return 海报数据的输入流
     * @throws MediaException 如果构建失败
     */
    public InputStream toInputStream() {
        return new ByteArrayInputStream(toBytes());
    }

    /**
     * 构建海报并写入到指定的输出流
     * <p>
     * 该方法避免了中间字节数组的创建，对于大型海报更加内存友好
     *
     * @param outputStream 目标输出流
     * @throws MediaException           如果写入失败
     * @throws IllegalArgumentException 如果输出流为null
     */
    public void writeTo(OutputStream outputStream) {
        if (outputStream == null) {
            throw new IllegalArgumentException("输出流不能为null");
        }

        try {
            if (result == null) {
                build();
            }
            ImageIO.write(result, outputFormat.getExtension(), outputStream);
        } catch (IOException e) {
            throw new MediaException("写入海报数据失败", e);
        }
    }

    /**
     * 将构建好的海报保存为本地文件。
     *
     * @param filePath 文件保存路径
     * @throws MediaException 如果保存过程中发生异常
     */
    public void save(String filePath) {
        if (result == null) {
            build();
        }
        try {
            File file = new File(filePath);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            ImageIO.write(result, outputFormat.getExtension(), file);
        } catch (IOException e) {
            throw new MediaException("保存海报失败", e);
        }
    }

    /**
     * 输出到HTTP响应
     *
     * @param response HTTP响应对象
     * @throws MediaException 如果输出失败
     */
    public void toResponse(HttpServletResponse response) {
        if (result == null) {
            build();
        }
        try {
            response.setContentType(outputFormat.getMimeType());
            response.setHeader("Cache-Control", "no-cache");
            ServletOutputStream out = response.getOutputStream();
            ImageIO.write(result, outputFormat.getExtension(), out);
            out.flush();
        } catch (IOException e) {
            throw new MediaException("输出海报到响应失败", e);
        }
    }

    /**
     * 获取构建后的海报数据大小（字节数）
     *
     * @return 海报数据的字节数
     */
    public long getDataSize() {
        return toBytes().length;
    }

    // ==================== 内部方法 ====================

    /**
     * 根据类型绘制不同的海报元素。
     *
     * @param g2d  Graphics2D绘图上下文
     * @param item 要绘制的海报元素对象
     * @throws MediaException 如果绘制过程中发生异常
     */
    private void drawItem(Graphics2D g2d, PosterItem item) {
        try {
            switch (item.getType()) {
                case TEXT:
                    String text = item.getContent() != null ? item.getContent() : "";
                    g2d.setColor(item.getFontColor());
                    g2d.setFont(item.getFont());
                    g2d.drawString(text, item.getX(), item.getY());
                    break;

                case IMAGE:
                    BufferedImage image;
                    if (StringUtils.isBlank(item.getContent())) {
                        // 创建默认占位图片
                        image = new BufferedImage(item.getWidth(), item.getHeight(), BufferedImage.TYPE_INT_RGB);
                        Graphics2D imgGraphics = image.createGraphics();
                        imgGraphics.setColor(Color.LIGHT_GRAY);
                        imgGraphics.fillRect(0, 0, item.getWidth(), item.getHeight());
                        imgGraphics.setColor(Color.DARK_GRAY);
                        imgGraphics.drawString("No Image", 10, item.getHeight() / 2);
                        imgGraphics.dispose();
                    } else {
                        image = ImageUtils.loadFromUrl(item.getContent());
                    }
                    if (item.getIsCircle()) {
                        image = ImageUtils.createCircleImage(image, item.getWidth());
                    }
                    g2d.drawImage(image, item.getX(), item.getY(), item.getWidth(), item.getHeight(), null);
                    break;

                case QRCODE:
                    BufferedImage qrCode = QrCodeUtils.generate(item.getContent(), item.getWidth());
                    g2d.drawImage(qrCode, item.getX(), item.getY(), null);
                    break;

                case RECTANGLE:
                    Color rectColor = ColorUtils.parseColor(item.getContent());
                    g2d.setColor(rectColor);
                    g2d.fillRect(item.getX(), item.getY(), item.getWidth(), item.getHeight());
                    break;

                case CIRCLE:
                    Color circleColor = ColorUtils.parseColor(item.getContent());
                    g2d.setColor(circleColor);
                    g2d.fillOval(item.getX(), item.getY(), item.getWidth(), item.getHeight());
                    break;
            }
        } catch (Exception e) {
            throw new MediaException("绘制元素失败: " + item.getType(), e);
        }
    }
}
