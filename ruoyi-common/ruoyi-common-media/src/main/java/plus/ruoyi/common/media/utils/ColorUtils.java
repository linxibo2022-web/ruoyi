package plus.ruoyi.common.media.utils;

import java.awt.*;

/**
 * 颜色工具类
 * 提供颜色解析和处理的相关工具方法
 */
public class ColorUtils {

    /**
     * 解析颜色字符串，支持多种格式的颜色表示
     * 支持格式：#FF0000, rgb(255,0,0), red
     *
     * @param colorStr 颜色字符串，可以是十六进制、RGB格式或预定义颜色名称
     * @return 解析后的Color对象，如果解析失败则返回黑色
     */
    public static Color parseColor(String colorStr) {
        if (colorStr == null || colorStr.trim().isEmpty()) {
            return Color.BLACK;
        }

        colorStr = colorStr.trim();

        // 十六进制颜色
        if (colorStr.startsWith("#")) {
            try {
                return Color.decode(colorStr);
            } catch (NumberFormatException e) {
                return Color.BLACK;
            }
        }

        // RGB格式 rgb(255,0,0)
        if (colorStr.startsWith("rgb(") && colorStr.endsWith(")")) {
            String rgb = colorStr.substring(4, colorStr.length() - 1);
            String[] parts = rgb.split(",");
            if (parts.length == 3) {
                try {
                    int r = Integer.parseInt(parts[0].trim());
                    int g = Integer.parseInt(parts[1].trim());
                    int b = Integer.parseInt(parts[2].trim());
                    return new Color(r, g, b);
                } catch (IllegalArgumentException e) {
                    return Color.BLACK;
                }
            }
        }

        // 预定义颜色名称
        return switch (colorStr.toLowerCase()) {
            case "red" -> Color.RED;
            case "green" -> Color.GREEN;
            case "blue" -> Color.BLUE;
            case "yellow" -> Color.YELLOW;
            case "orange" -> Color.ORANGE;
            case "pink" -> Color.PINK;
            case "cyan" -> Color.CYAN;
            case "magenta" -> Color.MAGENTA;
            case "white" -> Color.WHITE;
            case "black" -> Color.BLACK;
            case "gray", "grey" -> Color.GRAY;
            default -> Color.BLACK;
        };
    }

    /**
     * 创建带透明度的颜色
     *
     * @param color 原始颜色
     * @param alpha 透明度值，范围0.0-1.0，0表示完全透明，1表示完全不透明
     * @return 带透明度的新颜色对象
     */
    public static Color withAlpha(Color color, float alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(),
                        Math.round(255 * Math.max(0, Math.min(1, alpha))));
    }
}
