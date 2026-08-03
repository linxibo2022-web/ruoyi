package plus.ruoyi.common.media.options;

import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.Data;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 二维码选项配置类，用于设置生成二维码时的各项参数。
 */
@Data
public class QrCodeOptions {
    /**
     * 二维码尺寸（单位：像素）
     */
    private int size = 300;

    /**
     * 二维码边距大小（单位：模块数）
     */
    private int margin = 1;

    /**
     * 二维码前景色（默认为黑色）
     */
    private Color foregroundColor = Color.BLACK;

    /**
     * 二维码背景色（默认为白色）
     */
    private Color backgroundColor = Color.WHITE;

    /**
     * 纠错级别（默认为M级，可纠正约15%的错误）
     */
    private ErrorCorrectionLevel errorCorrectionLevel = ErrorCorrectionLevel.M;

    /**
     * 编码字符集（默认为UTF-8）
     */
    private String charset = "UTF-8";

    /**
     * Logo图片路径（可选）
     */
    private String logoPath;

    /**
     * Logo大小比例，取值范围为0.1到0.3之间（相对于二维码尺寸）
     */
    private float logoSizeRatio = 0.2f;

    /**
     * 创建一个使用默认配置的二维码选项实例
     *
     * @return 返回一个新的QrCodeOptions对象，包含默认配置
     */
    public static QrCodeOptions defaults() {
        return new QrCodeOptions();
    }

    /**
     * 设置二维码尺寸
     *
     * @param size 二维码尺寸（单位：像素）
     * @return 返回当前QrCodeOptions对象，支持链式调用
     */
    public QrCodeOptions size(int size) {
        this.size = size;
        return this;
    }

    /**
     * 设置二维码边距
     *
     * @param margin 边距大小（单位：模块数）
     * @return 返回当前QrCodeOptions对象，支持链式调用
     */
    public QrCodeOptions margin(int margin) {
        this.margin = margin;
        return this;
    }

    /**
     * 设置二维码前景色和背景色
     *
     * @param foreground 前景色
     * @param background 背景色
     * @return 返回当前QrCodeOptions对象，支持链式调用
     */
    public QrCodeOptions colors(Color foreground, Color background) {
        this.foregroundColor = foreground;
        this.backgroundColor = background;
        return this;
    }

    /**
     * 设置二维码纠错级别
     *
     * @param level 纠错级别（L/M/Q/H）
     * @return 返回当前QrCodeOptions对象，支持链式调用
     */
    public QrCodeOptions errorCorrection(ErrorCorrectionLevel level) {
        this.errorCorrectionLevel = level;
        return this;
    }

    /**
     * 设置Logo图片路径
     *
     * @param logoPath Logo图片文件路径
     * @return 返回当前QrCodeOptions对象，支持链式调用
     */
    public QrCodeOptions logo(String logoPath) {
        this.logoPath = logoPath;
        return this;
    }

    /**
     * 设置Logo图片路径及大小比例
     *
     * @param logoPath  Logo图片文件路径
     * @param sizeRatio Logo大小比例（限制在0.1到0.3之间）
     * @return 返回当前QrCodeOptions对象，支持链式调用
     */
    public QrCodeOptions logo(String logoPath, float sizeRatio) {
        this.logoPath = logoPath;
        this.logoSizeRatio = Math.max(0.1f, Math.min(0.3f, sizeRatio));
        return this;
    }

    /**
     * 获取ZXing编码提示信息，用于二维码生成过程中的参数配置
     *
     * @return 包含纠错级别、字符集和边距设置的编码提示映射表
     */
    public Map<EncodeHintType, Object> getEncodeHints() {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, errorCorrectionLevel);
        hints.put(EncodeHintType.CHARACTER_SET, charset);
        hints.put(EncodeHintType.MARGIN, margin);
        return hints;
    }
}
