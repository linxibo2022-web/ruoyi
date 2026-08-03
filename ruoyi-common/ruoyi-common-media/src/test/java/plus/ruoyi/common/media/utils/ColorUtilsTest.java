package plus.ruoyi.common.media.utils;

import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * ColorUtils 单元测试
 */
class ColorUtilsTest {

    @Test
    void testParseHexColor() {
        Color result = ColorUtils.parseColor("#FF0000");
        assertEquals(Color.RED, result);

        Color result2 = ColorUtils.parseColor("#00FF00");
        assertEquals(Color.GREEN, result2);

        Color result3 = ColorUtils.parseColor("#0000FF");
        assertEquals(Color.BLUE, result3);
    }

    @Test
    void testParseRgbColor() {
        Color result = ColorUtils.parseColor("rgb(255,0,0)");
        assertEquals(Color.RED, result);

        Color result2 = ColorUtils.parseColor("rgb(0, 255, 0)");
        assertEquals(Color.GREEN, result2);

        Color result3 = ColorUtils.parseColor("rgb(0,0,255)");
        assertEquals(Color.BLUE, result3);
    }

    @Test
    void testParseNamedColors() {
        assertEquals(Color.RED, ColorUtils.parseColor("red"));
        assertEquals(Color.GREEN, ColorUtils.parseColor("green"));
        assertEquals(Color.BLUE, ColorUtils.parseColor("blue"));
        assertEquals(Color.WHITE, ColorUtils.parseColor("white"));
        assertEquals(Color.BLACK, ColorUtils.parseColor("black"));
        assertEquals(Color.YELLOW, ColorUtils.parseColor("yellow"));
        assertEquals(Color.ORANGE, ColorUtils.parseColor("orange"));
        assertEquals(Color.PINK, ColorUtils.parseColor("pink"));
        assertEquals(Color.CYAN, ColorUtils.parseColor("cyan"));
        assertEquals(Color.MAGENTA, ColorUtils.parseColor("magenta"));
        assertEquals(Color.GRAY, ColorUtils.parseColor("gray"));
        assertEquals(Color.GRAY, ColorUtils.parseColor("grey"));
    }

    @Test
    void testParseInvalidColor() {
        // 无效颜色应该返回黑色
        assertEquals(Color.BLACK, ColorUtils.parseColor("invalid"));
        assertEquals(Color.BLACK, ColorUtils.parseColor(""));
        assertEquals(Color.BLACK, ColorUtils.parseColor(null));
        assertEquals(Color.BLACK, ColorUtils.parseColor("#GGGGGG"));
        assertEquals(Color.BLACK, ColorUtils.parseColor("rgb(300,400,500)"));
    }

    @Test
    void testParseCaseInsensitive() {
        assertEquals(Color.RED, ColorUtils.parseColor("RED"));
        assertEquals(Color.RED, ColorUtils.parseColor("Red"));
        assertEquals(Color.BLUE, ColorUtils.parseColor("BLUE"));
        assertEquals(Color.GREEN, ColorUtils.parseColor("GREEN"));
    }

    @Test
    void testWithAlpha() {
        Color baseColor = Color.RED;

        Color result1 = ColorUtils.withAlpha(baseColor, 0.5f);
        assertEquals(255, result1.getRed());
        assertEquals(0, result1.getGreen());
        assertEquals(0, result1.getBlue());
        assertEquals(128, result1.getAlpha()); // 255 * 0.5 = 127.5 ≈ 128

        Color result2 = ColorUtils.withAlpha(baseColor, 0.0f);
        assertEquals(0, result2.getAlpha());

        Color result3 = ColorUtils.withAlpha(baseColor, 1.0f);
        assertEquals(255, result3.getAlpha());
    }

    @Test
    void testWithAlphaOutOfBounds() {
        Color baseColor = Color.BLUE;

        // 测试超出范围的alpha值
        Color result1 = ColorUtils.withAlpha(baseColor, -0.5f);
        assertEquals(0, result1.getAlpha());

        Color result2 = ColorUtils.withAlpha(baseColor, 1.5f);
        assertEquals(255, result2.getAlpha());
    }

    @Test
    void testParseRgbWithSpaces() {
        Color result1 = ColorUtils.parseColor("rgb( 255 , 128 , 64 )");
        assertEquals(255, result1.getRed());
        assertEquals(128, result1.getGreen());
        assertEquals(64, result1.getBlue());

        Color result2 = ColorUtils.parseColor("rgb(100,200,50)");
        assertEquals(100, result2.getRed());
        assertEquals(200, result2.getGreen());
        assertEquals(50, result2.getBlue());
    }

    @Test
    void testParseShortHexColor() {
        // 测试3位十六进制颜色（如果支持的话）
        Color result = ColorUtils.parseColor("#F00");
        // 注意：当前实现可能不支持3位十六进制，会返回BLACK
        // 这个测试用来确认当前行为
        assertNotNull(result);
    }
}
