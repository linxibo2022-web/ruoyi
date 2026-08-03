package plus.ruoyi.common.excel.annotation;

import plus.ruoyi.common.test.base.BaseUnitTest;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ExcelRequired 必填字段标识注解测试
 *
 * @author 抓蛙师
 */
@DisplayName("ExcelRequired必填字段标识注解测试")
public class ExcelRequiredTest extends BaseUnitTest {

    // ==================== 注解元信息测试 ====================

    @Test
    @DisplayName("测试注解-Target应为FIELD")
    public void testAnnotationTarget() {
        Target target = ExcelRequired.class.getAnnotation(Target.class);

        assertNotNull(target, "应有Target注解");
        assertEquals(1, target.value().length, "应只有1个目标");
        assertEquals(ElementType.FIELD, target.value()[0], "目标应为FIELD");
    }

    @Test
    @DisplayName("测试注解-Retention应为RUNTIME")
    public void testAnnotationRetention() {
        Retention retention = ExcelRequired.class.getAnnotation(Retention.class);

        assertNotNull(retention, "应有Retention注解");
        assertEquals(RetentionPolicy.RUNTIME, retention.value(), "保留策略应为RUNTIME");
    }

    // ==================== 默认值测试 ====================

    @Test
    @DisplayName("测试默认值-fontColor默认为红色")
    public void testDefaultFontColor() throws Exception {
        Field field = TestClass.class.getDeclaredField("defaultField");
        ExcelRequired annotation = field.getAnnotation(ExcelRequired.class);

        assertEquals(IndexedColors.RED, annotation.fontColor(), "fontColor默认应为RED");
    }

    // ==================== 自定义值测试 ====================

    @Test
    @DisplayName("测试自定义值-蓝色字体")
    public void testBlueFontColor() throws Exception {
        Field field = TestClass.class.getDeclaredField("blueField");
        ExcelRequired annotation = field.getAnnotation(ExcelRequired.class);

        assertEquals(IndexedColors.BLUE, annotation.fontColor(), "fontColor应为BLUE");
    }

    @Test
    @DisplayName("测试自定义值-橙色字体")
    public void testOrangeFontColor() throws Exception {
        Field field = TestClass.class.getDeclaredField("orangeField");
        ExcelRequired annotation = field.getAnnotation(ExcelRequired.class);

        assertEquals(IndexedColors.ORANGE, annotation.fontColor(), "fontColor应为ORANGE");
    }

    @Test
    @DisplayName("测试自定义值-深红色字体")
    public void testDarkRedFontColor() throws Exception {
        Field field = TestClass.class.getDeclaredField("darkRedField");
        ExcelRequired annotation = field.getAnnotation(ExcelRequired.class);

        assertEquals(IndexedColors.DARK_RED, annotation.fontColor(), "fontColor应为DARK_RED");
    }

    // ==================== 业务场景测试 ====================

    @Test
    @DisplayName("测试业务场景-用户名必填")
    public void testUserNameRequired() throws Exception {
        Field field = UserVo.class.getDeclaredField("userName");
        ExcelRequired annotation = field.getAnnotation(ExcelRequired.class);

        assertNotNull(annotation, "userName应有ExcelRequired注解");
        assertEquals(IndexedColors.RED, annotation.fontColor(), "用户名必填应为红色");
    }

    @Test
    @DisplayName("测试业务场景-邮箱必填")
    public void testEmailRequired() throws Exception {
        Field field = UserVo.class.getDeclaredField("email");
        ExcelRequired annotation = field.getAnnotation(ExcelRequired.class);

        assertNotNull(annotation, "email应有ExcelRequired注解");
    }

    @Test
    @DisplayName("测试业务场景-订单号必填")
    public void testOrderNoRequired() throws Exception {
        Field field = OrderVo.class.getDeclaredField("orderNo");
        ExcelRequired annotation = field.getAnnotation(ExcelRequired.class);

        assertNotNull(annotation, "orderNo应有ExcelRequired注解");
    }

    @Test
    @DisplayName("测试业务场景-非必填字段无注解")
    public void testOptionalFieldNoAnnotation() throws Exception {
        Field field = UserVo.class.getDeclaredField("remark");
        ExcelRequired annotation = field.getAnnotation(ExcelRequired.class);

        assertNull(annotation, "remark不应有ExcelRequired注解");
    }

    // ==================== 反射获取测试 ====================

    @Test
    @DisplayName("测试反射-获取所有必填字段")
    public void testGetAllRequiredFields() {
        Field[] fields = UserVo.class.getDeclaredFields();
        int requiredCount = 0;

        for (Field field : fields) {
            if (field.isAnnotationPresent(ExcelRequired.class)) {
                requiredCount++;
            }
        }

        assertEquals(2, requiredCount, "UserVo应有2个必填字段");
    }

    @Test
    @DisplayName("测试反射-无注解字段")
    public void testFieldWithoutAnnotation() throws Exception {
        Field field = TestClass.class.getDeclaredField("noAnnotationField");
        ExcelRequired annotation = field.getAnnotation(ExcelRequired.class);

        assertNull(annotation, "无注解字段应返回null");
    }

    // ==================== IndexedColors颜色值测试 ====================

    @Test
    @DisplayName("测试颜色-RED颜色索引")
    public void testRedColorIndex() {
        IndexedColors red = IndexedColors.RED;

        assertTrue(red.getIndex() >= 0, "RED颜色索引应为正数");
        assertNotNull(red.name(), "RED名称不应为null");
    }

    @Test
    @DisplayName("测试颜色-常用颜色列表")
    public void testCommonColors() {
        // 验证常用颜色都是有效的
        assertNotNull(IndexedColors.RED, "RED应存在");
        assertNotNull(IndexedColors.BLUE, "BLUE应存在");
        assertNotNull(IndexedColors.GREEN, "GREEN应存在");
        assertNotNull(IndexedColors.ORANGE, "ORANGE应存在");
        assertNotNull(IndexedColors.YELLOW, "YELLOW应存在");
        assertNotNull(IndexedColors.BLACK, "BLACK应存在");
        assertNotNull(IndexedColors.WHITE, "WHITE应存在");
    }

    // ==================== 组合注解测试 ====================

    @Test
    @DisplayName("测试组合-ExcelRequired和ExcelNotation组合")
    public void testCombineWithNotation() throws Exception {
        Field field = CombinedVo.class.getDeclaredField("requiredWithNotation");

        ExcelRequired required = field.getAnnotation(ExcelRequired.class);
        ExcelNotation notation = field.getAnnotation(ExcelNotation.class);

        assertNotNull(required, "应有ExcelRequired注解");
        assertNotNull(notation, "应有ExcelNotation注解");
        assertEquals(IndexedColors.RED, required.fontColor(), "必填颜色应为红色");
        assertTrue(notation.value().length() > 0, "批注不应为空");
    }

    // ==================== 边界值测试 ====================

    @Test
    @DisplayName("测试边界值-使用所有IndexedColors颜色")
    public void testAllIndexedColors() throws Exception {
        // 测试IndexedColors枚举的有效性
        IndexedColors[] allColors = IndexedColors.values();
        assertTrue(allColors.length > 0, "IndexedColors应有颜色值");

        // 验证每个颜色都有有效索引
        for (IndexedColors color : allColors) {
            assertTrue(color.getIndex() >= -1, color.name() + "颜色索引应有效");
        }
    }

    // ==================== 测试用类 ====================

    /**
     * 测试用类 - 包含各种ExcelRequired配置
     */
    private static class TestClass {
        @ExcelRequired
        private String defaultField;

        @ExcelRequired(fontColor = IndexedColors.BLUE)
        private String blueField;

        @ExcelRequired(fontColor = IndexedColors.ORANGE)
        private String orangeField;

        @ExcelRequired(fontColor = IndexedColors.DARK_RED)
        private String darkRedField;

        private String noAnnotationField;
    }

    /**
     * 用户VO - 业务场景测试
     */
    private static class UserVo {
        private Long id;

        @ExcelRequired
        private String userName;

        @ExcelRequired
        private String email;

        private String phone;

        private String remark;
    }

    /**
     * 订单VO - 业务场景测试
     */
    private static class OrderVo {
        private Long id;

        @ExcelRequired
        private String orderNo;

        @ExcelRequired(fontColor = IndexedColors.ORANGE)
        private String customerName;

        private Double amount;
    }

    /**
     * 组合注解VO - 测试多注解组合
     */
    private static class CombinedVo {
        @ExcelRequired
        @ExcelNotation(value = "必填项，请填写有效数据")
        private String requiredWithNotation;
    }
}
