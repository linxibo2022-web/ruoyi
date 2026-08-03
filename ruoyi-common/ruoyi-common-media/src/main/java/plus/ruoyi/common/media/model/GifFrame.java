package plus.ruoyi.common.media.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.awt.image.BufferedImage;

/**
 * GIF帧
 * <p>
 * 用于表示GIF动画中的单个帧，包含帧图像和显示延迟时间
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GifFrame {
    /**
     * 帧图像
     * <p>
     * 存储当前帧的图像数据
     */
    private BufferedImage image;

    /**
     * 帧延迟时间(毫秒)
     * <p>
     * 表示当前帧显示的持续时间，单位为毫秒
     */
    private int delay;

    /**
     * 构造函数，使用指定图像创建GIF帧
     *
     * @param image 帧图像，不能为空
     *              默认延迟时间设置为500毫秒
     */
    public GifFrame(BufferedImage image) {
        this.image = image;
        this.delay = 500; // 默认500ms
    }
}

