package plus.ruoyi.common.media.utils;

import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FontUtils 单元测试
 */
class FontUtilsTest {

    @Test
    void testCreateFont() {
        Font result = FontUtils.createFont("Arial", Font.PLAIN, 24);

        assertNotNull(result);
        assertEquals("Arial", result.getName());
        assertEquals(Font.PLAIN, result.getStyle());
        assertEquals(24, result.getSize());
    }

    @Test
    void testCreateDefaultFont() {
        Font result = FontUtils.createDefaultFont(32);

        assertNotNull(result);
        assertEquals("微软雅黑", result.getName());
        assertEquals(Font.PLAIN, result.getStyle());
        assertEquals(32, result.getSize());
    }

    @Test
    void testCreateBoldFont() {
        Font result = FontUtils.createBoldFont("Times New Roman", 18);

        assertNotNull(result);
        assertEquals("Times New Roman", result.getName());
        assertEquals(Font.BOLD, result.getStyle());
        assertEquals(18, result.getSize());
    }

    @Test
    void testGetTextSize() {
        Font testFont = new Font("Arial", Font.PLAIN, 20);
        Dimension result = FontUtils.getTextSize("Hello World", testFont);

        assertNotNull(result);
        assertTrue(result.width > 0);
        assertTrue(result.height > 0);
    }

    @Test
    void testGetTextSizeEmptyString() {
        Font testFont = new Font("Arial", Font.PLAIN, 16);
        Dimension result = FontUtils.getTextSize("", testFont);

        assertNotNull(result);
        assertEquals(0, result.width);
        assertTrue(result.height > 0); // 字体高度应该大于0
    }

    @Test
    void testGetTextSizeDifferentFonts() {
        Font smallFont = new Font("Arial", Font.PLAIN, 12);
        Font largeFont = new Font("Arial", Font.PLAIN, 36);
        String text = "Test Text";

        Dimension smallSize = FontUtils.getTextSize(text, smallFont);
        Dimension largeSize = FontUtils.getTextSize(text, largeFont);

        assertNotNull(smallSize);
        assertNotNull(largeSize);
        assertTrue(largeSize.width > smallSize.width);
        assertTrue(largeSize.height > smallSize.height);
    }

    @Test
    void testGetTextSizeWithSpecialCharacters() {
        Font testFont = new Font("微软雅黑", Font.PLAIN, 24);
        Dimension result = FontUtils.getTextSize("中文测试!@#$", testFont);

        assertNotNull(result);
        assertTrue(result.width > 0);
        assertTrue(result.height > 0);
    }

    @Test
    void testCreateFontWithDifferentStyles() {
        Font plain = FontUtils.createFont("Arial", Font.PLAIN, 16);
        Font bold = FontUtils.createFont("Arial", Font.BOLD, 16);
        Font italic = FontUtils.createFont("Arial", Font.ITALIC, 16);
        Font boldItalic = FontUtils.createFont("Arial", Font.BOLD | Font.ITALIC, 16);

        assertEquals(Font.PLAIN, plain.getStyle());
        assertEquals(Font.BOLD, bold.getStyle());
        assertEquals(Font.ITALIC, italic.getStyle());
        assertEquals(Font.BOLD | Font.ITALIC, boldItalic.getStyle());
    }
}
