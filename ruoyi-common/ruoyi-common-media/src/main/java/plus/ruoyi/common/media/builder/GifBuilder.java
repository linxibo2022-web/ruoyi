package plus.ruoyi.common.media.builder;

import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.media.exception.MediaException;
import plus.ruoyi.common.media.model.GifFrame;
import plus.ruoyi.common.media.options.AnimationOptions;
import plus.ruoyi.common.media.utils.ImageUtils;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * GIF 动图构建器，支持添加帧、设置尺寸、延迟、循环等参数。
 *
 * @author 抓蛙师
 */
@Slf4j
public class GifBuilder {

    private List<GifFrame> frames;
    private AnimationOptions options;

    /**
     * 默认构造函数，初始化帧列表与默认配置。
     */
    public GifBuilder() {
        this.frames = new ArrayList<>();
        this.options = AnimationOptions.defaults();
    }

    /**
     * 带尺寸初始化构造函数。
     *
     * @param width  动图宽度
     * @param height 动图高度
     */
    public GifBuilder(int width, int height) {
        this();
        this.options.size(width, height);
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 创建默认的GifBuilder实例
     *
     * @return GifBuilder实例
     */
    public static GifBuilder of() {
        return new GifBuilder();
    }

    /**
     * 创建指定尺寸的GifBuilder实例
     *
     * @param width  动图宽度
     * @param height 动图高度
     * @return GifBuilder实例
     */
    public static GifBuilder of(int width, int height) {
        return new GifBuilder(width, height);
    }

    /**
     * 使用动画选项创建GifBuilder实例
     *
     * @param options 动画选项配置
     * @return GifBuilder实例
     */
    public static GifBuilder of(AnimationOptions options) {
        GifBuilder builder = new GifBuilder();
        builder.options = options;
        return builder;
    }

    /**
     * 快速创建带基本配置的GifBuilder实例
     *
     * @param width  动图宽度
     * @param height 动图高度
     * @param delay  帧延迟时间（毫秒）
     * @return GifBuilder实例
     */
    public static GifBuilder of(int width, int height, int delay) {
        return new GifBuilder(width, height).delay(delay);
    }

    /**
     * 快速创建带完整配置的GifBuilder实例
     *
     * @param width  动图宽度
     * @param height 动图高度
     * @param delay  帧延迟时间（毫秒）
     * @param loop   是否循环播放
     * @return GifBuilder实例
     */
    public static GifBuilder of(int width, int height, int delay, boolean loop) {
        return new GifBuilder(width, height).delay(delay).loop(loop);
    }

    // ==================== 链式配置方法 ====================

    /**
     * 设置动图尺寸。
     *
     * @param width  宽度
     * @param height 高度
     * @return 当前构建器实例
     */
    public GifBuilder size(int width, int height) {
        options.size(width, height);
        return this;
    }

    /**
     * 设置帧延迟时间。
     *
     * @param delay 延迟时间（毫秒）
     * @return 当前构建器实例
     */
    public GifBuilder delay(int delay) {
        options.delay(delay);
        return this;
    }

    /**
     * 设置是否循环播放。
     *
     * @param loop 是否循环
     * @return 当前构建器实例
     */
    public GifBuilder loop(boolean loop) {
        options.loop(loop);
        return this;
    }

    /**
     * 设置背景颜色。
     *
     * @param color 背景色
     * @return 当前构建器实例
     */
    public GifBuilder backgroundColor(Color color) {
        options.backgroundColor(color);
        return this;
    }

    /**
     * 设置图像质量。
     *
     * @param quality 质量值
     * @return 当前构建器实例
     */
    public GifBuilder quality(int quality) {
        options.quality(quality);
        return this;
    }

    // ==================== 帧添加方法 ====================

    /**
     * 添加一帧图像，使用默认延迟。
     *
     * @param image 图像
     * @return 当前构建器实例
     */
    public GifBuilder addFrame(BufferedImage image) {
        frames.add(new GifFrame(image, options.getDefaultDelay()));
        return this;
    }

    /**
     * 添加一帧图像，并指定延迟。
     *
     * @param image 图像
     * @param delay 延迟时间（毫秒）
     * @return 当前构建器实例
     */
    public GifBuilder addFrame(BufferedImage image, int delay) {
        frames.add(new GifFrame(image, delay));
        return this;
    }

    /**
     * 从 URL 添加一帧或多帧图像，使用默认延迟。
     * <p>
     * 支持单个URL或逗号分隔的多个URL
     * <p>
     * 示例：
     * <pre>
     * // 单个URL
     * addFrame("https://example.com/image1.jpg");
     *
     * // 多个URL（逗号分隔）
     * addFrame("https://example.com/image1.jpg,https://example.com/image2.jpg,https://example.com/image3.jpg");
     * </pre>
     *
     * @param imageUrls 图片地址，单个URL或逗号分隔的多个URL
     * @return 当前构建器实例
     */
    public GifBuilder addFrame(String imageUrls) {
        if (StringUtils.isBlank(imageUrls)) {
            return this;
        }

        List<String> urls = StringUtils.splitToList(imageUrls, StringUtils.SEPARATOR);
        for (String url : urls) {
            try {
                BufferedImage image = ImageUtils.loadFromUrl(url);
                addFrame(image);
            } catch (Exception e) {
                log.warn("加载图片失败，跳过URL: {} - {}", url, e.getMessage());
            }
        }

        return this;
    }

    /**
     * 从 URL 添加一帧或多帧图像，并指定延迟。
     * <p>
     * 支持单个URL或逗号分隔的多个URL，所有帧使用相同延迟
     *
     * @param imageUrls 图片地址，单个URL或逗号分隔的多个URL
     * @param delay     延迟时间（毫秒）
     * @return 当前构建器实例
     */
    public GifBuilder addFrame(String imageUrls, int delay) {
        if (StringUtils.isBlank(imageUrls) || delay < 0) {
            return this;
        }

        List<String> urls = StringUtils.splitToList(imageUrls, StringUtils.SEPARATOR);
        for (String url : urls) {
            try {
                BufferedImage image = ImageUtils.loadFromUrl(url);
                addFrame(image, delay);
            } catch (Exception e) {
                log.warn("加载图片失败，跳过URL: {} - {}", url, e.getMessage());
            }
        }

        return this;
    }

    /**
     * 批量添加帧。
     *
     * @param images 图像列表
     * @return 当前构建器实例
     */
    public GifBuilder addFrames(List<BufferedImage> images) {
        for (BufferedImage image : images) {
            addFrame(image);
        }
        return this;
    }

    // ==================== 构建与输出方法 ====================

    /**
     * 构建 GIF 动图并返回字节数组。
     *
     * @return GIF 字节数据
     * @throws MediaException 如果没有帧或生成失败
     */
    public byte[] build() {
        if (frames.isEmpty()) {
            throw new MediaException("没有添加任何帧");
        }

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            writeGif(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new MediaException("GIF生成失败", e);
        }
    }

    /**
     * 构建 GIF 动图并返回字节数组（build方法的别名）。
     *
     * @return GIF 字节数据
     */
    public byte[] toBytes() {
        return build();
    }

    /**
     * 构建 GIF 动图并返回输入流。
     * <p>
     * 该方法适用于需要将GIF数据作为流处理的场景，比如：
     * <ul>
     *   <li>上传到OSS等云存储服务</li>
     *   <li>通过HTTP响应返回</li>
     *   <li>写入到其他输出流</li>
     * </ul>
     * <p>
     * 注意：返回的输入流使用完毕后请记得关闭。
     *
     * @return GIF数据的输入流
     * @throws MediaException 如果没有帧或生成失败
     */
    public InputStream toInputStream() {
        return new ByteArrayInputStream(build());
    }

    /**
     * 构建 GIF 动图并写入到指定的输出流。
     * <p>
     * 该方法适用于直接将GIF数据写入到目标流的场景，避免了中间字节数组的创建，
     * 对于大型GIF更加内存友好。
     * <p>
     * 注意：该方法不会关闭传入的输出流，调用者需要自行管理流的生命周期。
     *
     * @param outputStream 目标输出流
     * @throws MediaException           如果没有帧或写入失败
     * @throws IllegalArgumentException 如果输出流为null
     */
    public void writeTo(OutputStream outputStream) {
        if (outputStream == null) {
            throw new IllegalArgumentException("输出流不能为null");
        }

        if (frames.isEmpty()) {
            throw new MediaException("没有添加任何帧");
        }

        try {
            writeGif(outputStream);
        } catch (IOException e) {
            throw new MediaException("写入GIF数据失败", e);
        }
    }

    /**
     * 保存 GIF 到指定路径。
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

            try (FileOutputStream fos = new FileOutputStream(file)) {
                writeGif(fos);
            }
        } catch (IOException e) {
            throw new MediaException("保存GIF失败", e);
        }
    }

    // ==================== 便利方法 ====================

    /**
     * 获取构建后的GIF数据大小（字节数）
     * <p>
     * 注意：该方法会触发GIF构建过程，如果之后还需要使用构建结果，
     * 建议先调用build()方法保存结果，避免重复构建。
     *
     * @return GIF数据的字节数
     */
    public long getDataSize() {
        return build().length;
    }

    /**
     * 获取帧数量
     *
     * @return 当前已添加的帧数量
     */
    public int getFrameCount() {
        return frames.size();
    }

    /**
     * 获取动画总时长（毫秒）
     * <p>
     * 计算所有帧的延迟时间总和
     *
     * @return 动画总时长
     */
    public long getTotalDuration() {
        return frames.stream()
            .mapToLong(GifFrame::getDelay)
            .sum();
    }

    /**
     * 清除所有帧
     *
     * @return 当前构建器实例
     */
    public GifBuilder clearFrames() {
        frames.clear();
        return this;
    }

    // ==================== 内部方法 ====================

    /**
     * 将 GIF 写入输出流。
     *
     * @param outputStream 输出流
     * @throws IOException 如果写入失败
     */
    private void writeGif(OutputStream outputStream) throws IOException {
        ImageWriter writer = getGifWriter();
        if (writer == null) {
            throw new MediaException("找不到GIF编码器");
        }

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream)) {
            writer.setOutput(ios);
            writer.prepareWriteSequence(null);

            for (int i = 0; i < frames.size(); i++) {
                GifFrame frame = frames.get(i);
                BufferedImage processedImage = processFrame(frame.getImage());

                ImageWriteParam writeParam = writer.getDefaultWriteParam();
                IIOMetadata metadata = writer.getDefaultImageMetadata(
                    new ImageTypeSpecifier(processedImage), writeParam);

                configureFrameMetadata(metadata, frame.getDelay(), i == 0 && options.isLoop());

                writer.writeToSequence(new IIOImage(processedImage, null, metadata), writeParam);
            }

            writer.endWriteSequence();
        } finally {
            writer.dispose();
        }
    }

    private BufferedImage processFrame(BufferedImage original) {
        return ImageUtils.resize(original, options.getWidth(), options.getHeight(), options.getResizeMode());
    }

    /**
     * 配置帧元数据（延迟、循环等）。
     *
     * @param metadata            元数据对象
     * @param delay               延迟时间
     * @param isFirstFrameAndLoop 是否为第一帧且启用循环
     */
    private void configureFrameMetadata(IIOMetadata metadata, int delay, boolean isFirstFrameAndLoop) {
        try {
            String metadataFormat = metadata.getNativeMetadataFormatName();
            IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metadataFormat);

            IIOMetadataNode graphicsControlExtension = getNode(root, "GraphicControlExtension");
            if (graphicsControlExtension == null) {
                graphicsControlExtension = new IIOMetadataNode("GraphicControlExtension");
                root.appendChild(graphicsControlExtension);
            }

            graphicsControlExtension.setAttribute("disposalMethod", "none");
            graphicsControlExtension.setAttribute("userInputFlag", "FALSE");
            graphicsControlExtension.setAttribute("transparentColorFlag", "FALSE");
            graphicsControlExtension.setAttribute("delayTime", String.valueOf(Math.max(delay / 10, 1)));
            graphicsControlExtension.setAttribute("transparentColorIndex", "0");

            if (isFirstFrameAndLoop) {
                IIOMetadataNode appExtensions = getNode(root, "ApplicationExtensions");
                if (appExtensions == null) {
                    appExtensions = new IIOMetadataNode("ApplicationExtensions");
                    root.appendChild(appExtensions);
                }

                IIOMetadataNode appExtension = new IIOMetadataNode("ApplicationExtension");
                appExtension.setAttribute("applicationID", "NETSCAPE");
                appExtension.setAttribute("authenticationCode", "2.0");

                int loopContinuously = 0;
                appExtension.setUserObject(new byte[]{0x1,
                    (byte) (loopContinuously & 0xFF),
                    (byte) ((loopContinuously >> 8) & 0xFF)});
                appExtensions.appendChild(appExtension);
            }

            metadata.setFromTree(metadataFormat, root);
        } catch (Exception e) {
            log.warn("配置GIF元数据失败: {}", e.getMessage());
        }
    }

    /**
     * 获取指定名称的元数据节点。
     *
     * @param rootNode 根节点
     * @param nodeName 节点名称
     * @return 对应节点或 null
     */
    private IIOMetadataNode getNode(IIOMetadataNode rootNode, String nodeName) {
        int nodeCount = rootNode.getLength();
        for (int i = 0; i < nodeCount; i++) {
            if (rootNode.item(i).getNodeName().equalsIgnoreCase(nodeName)) {
                return (IIOMetadataNode) rootNode.item(i);
            }
        }
        return null;
    }

    /**
     * 获取 GIF 图像写入器。
     *
     * @return GIF 写入器或 null
     */
    private ImageWriter getGifWriter() {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("gif");
        if (!writers.hasNext()) {
            return null;
        }
        return writers.next();
    }
}
