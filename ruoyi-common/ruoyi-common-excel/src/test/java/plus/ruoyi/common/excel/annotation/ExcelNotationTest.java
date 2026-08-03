package plus.ruoyi.common.excel.annotation;

import plus.ruoyi.common.test.base.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ExcelNotation 单元格批注注解测试
 *
 * @author 抓蛙师
 */
@DisplayName("ExcelNotation单元格批注注解测试")
public class ExcelNotationTest extends BaseUnitTest {

    // ==================== 注解元信息测试 ====================

    @Test
    @DisplayName("测试注解-Target应为FIELD")
    public void testAnnotationTarget() {
        Target target = ExcelNotation.class.getAnnotation(Target.class);

        assertNotNull(target, "应有Target注解");
        assertEquals(1, target.value().length, "应只有1个目标");
        assertEquals(ElementType.FIELD, target.value()[0], "目标应为FIELD");
    }

    @Test
    @DisplayName("测试注解-Retention应为RUNTIME")
    public void testAnnotationRetention() {
        Retention retention = ExcelNotation.class.getAnnotation(Retention.class);

        assertNotNull(retention, "应有Retention注解");
        assertEquals(RetentionPolicy.RUNTIME, retention.value(), "保留策略应为RUNTIME");
    }

    // ==================== 默认值测试 ====================

    @Test
    @DisplayName("测试默认值-value默认为空")
    public void testDefaultValue() throws Exception {
        Field field = TestClass.class.getDeclaredField("defaultField");
        ExcelNotation annotation = field.getAnnotation(ExcelNotation.class);

        assertEquals("", annotation.value(), "value默认应为空字符串");
    }

    // ==================== 自定义值测试 ====================

    @Test
    @DisplayName("测试自定义值-简单批注")
    public void testSimpleNotation() throws Exception {
        Field field = TestClass.class.getDeclaredField("simpleField");
        ExcelNotation annotation = field.getAnnotation(ExcelNotation.class);

        assertEquals("请填写真实姓名", annotation.value(), "批注内容应正确");
    }

    @Test
    @DisplayName("测试自定义值-长批注")
    public void testLongNotation() throws Exception {
        Field field = TestClass.class.getDeclaredField("longNotationField");
        ExcelNotation annotation = field.getAnnotation(ExcelNotation.class);

        String value = annotation.value();
        assertTrue(value.length() > 20, "长批注内容应超过20字符");
        assertTrue(value.contains("格式"), "应包含格式说明");
    }

    @Test
    @DisplayName("测试自定义值-多行批注")
    public void testMultiLineNotation() throws Exception {
        Field field = TestClass.class.getDeclaredField("multiLineField");
        ExcelNotation annotation = field.getAnnotation(ExcelNotation.class);

        String value = annotation.value();
        assertTrue(value.contains("\n"), "多行批注应包含换行符");
    }

    // ==================== 业务场景测试 ====================

    @Test
    @DisplayName("测试业务场景-用户名批注")
    public void testUserNameNotation() throws Exception {
        Field field = UserVo.class.getDeclaredField("userName");
        ExcelNotation annotation = field.getAnnotation(ExcelNotation.class);

        assertNotNull(annotation, "userName应有ExcelNotation注解");
        assertTrue(annotation.value().contains("真实姓名"), "批注应包含真实姓名说明");
    }

    @Test
    @DisplayName("测试业务场景-邮箱批注")
    public void testEmailNotation() throws Exception {
        Field field = UserVo.class.getDeclaredField("email");
        ExcelNotation annotation = field.getAnnotation(ExcelNotation.class);

        assertNotNull(annotation, "email应有ExcelNotation注解");
        String value = annotation.value();
        assertTrue(value.contains("邮箱") || value.contains("@"), "批注应包含邮箱相关说明");
    }

    @Test
    @DisplayName("测试业务场景-手机号批注")
    public void testPhoneNotation() throws Exception {
        Field field = UserVo.class.getDeclaredField("phone");
        ExcelNotation annotation = field.getAnnotation(ExcelNotation.class);

        assertNotNull(annotation, "phone应有ExcelNotation注解");
        assertTrue(annotation.value().contains("11位"), "批注应包含手机号格式说明");
    }

    @Test
    @DisplayName("测试业务场景-金额批注")
    public void testAmountNotation() throws Exception {
        Field field = OrderVo.class.getDeclaredField("amount");
        ExcelNotation annotation = field.getAnnotation(ExcelNotation.class);

        assertNotNull(annotation, "amount应有ExcelNotation注解");
        assertTrue(annotation.value().contains("小数"), "批注应包含小数说明");
    }

    // ==================== 反射获取测试 ====================

    @Test
    @DisplayName("测试反射-获取所有带注解字段")
    public void testGetAllAnnotatedFields() {
        Field[] fields = TestClass.class.getDeclaredFields();
        int annotatedCount = 0;

        for (Field field : fields) {
            if (field.isAnnotationPresent(ExcelNotation.class)) {
                annotatedCount++;
            }
        }

        assertTrue(annotatedCount > 0, "应有带ExcelNotation注解的字段");
    }

    @Test
    @DisplayName("测试反射-无注解字段")
    public void testFieldWithoutAnnotation() throws Exception {
        Field field = TestClass.class.getDeclaredField("noAnnotationField");
        ExcelNotation annotation = field.getAnnotation(ExcelNotation.class);

        assertNull(annotation, "无注解字段应返回null");
    }

    // ==================== 特殊字符测试 ====================

    @Test
    @DisplayName("测试特殊字符-包含引号")
    public void testNotationWithQuotes() throws Exception {
        Field field = TestClass.class.getDeclaredField("quotesField");
        ExcelNotation annotation = field.getAnnotation(ExcelNotation.class);

        String value = annotation.value();
        assertTrue(value.contains("\""), "批注应包含引号");
    }

    @Test
    @DisplayName("测试特殊字符-包含冒号")
    public void testNotationWithColon() throws Exception {
        Field field = TestClass.class.getDeclaredField("colonField");
        ExcelNotation annotation = field.getAnnotation(ExcelNotation.class);

        String value = annotation.value();
        assertTrue(value.contains(":") || value.contains("："), "批注应包含冒号");
    }

    // ==================== 边界值测试 ====================

    @Test
    @DisplayName("测试边界值-空字符串批注")
    public void testEmptyNotation() throws Exception {
        Field field = TestClass.class.getDeclaredField("defaultField");
        ExcelNotation annotation = field.getAnnotation(ExcelNotation.class);

        assertEquals("", annotation.value(), "空字符串批注应正确");
    }

    @Test
    @DisplayName("测试边界值-单个字符批注")
    public void testSingleCharNotation() throws Exception {
        Field field = TestClass.class.getDeclaredField("singleCharField");
        ExcelNotation annotation = field.getAnnotation(ExcelNotation.class);

        assertEquals("*", annotation.value(), "单个字符批注应正确");
    }

    // ==================== 测试用类 ====================

    /**
     * 测试用类 - 包含各种ExcelNotation配置
     */
    private static class TestClass {
        @ExcelNotation
        private String defaultField;

        @ExcelNotation(value = "请填写真实姓名")
        private String simpleField;

        @ExcelNotation(value = "请按以下格式填写：yyyy-MM-dd，例如：2024-01-01")
        private String longNotationField;

        @ExcelNotation(value = "注意事项：\n1. 必须填写\n2. 格式正确")
        private String multiLineField;

        @ExcelNotation(value = "请填写\"有效\"或\"无效\"")
        private String quotesField;

        @ExcelNotation(value = "格式：类型:值")
        private String colonField;

        @ExcelNotation(value = "*")
        private String singleCharField;

        private String noAnnotationField;
    }

    /**
     * 用户VO - 业务场景测试
     */
    private static class UserVo {
        private Long id;

        @ExcelNotation(value = "请填写真实姓名，长度2-20个字符")
        private String userName;

        @ExcelNotation(value = "请填写有效邮箱地址，如：example@company.com")
        private String email;

        @ExcelNotation(value = "请填写11位手机号码")
        private String phone;

        private String remark;
    }

    /**
     * 订单VO - 业务场景测试
     */
    private static class OrderVo {
        private Long id;
        private String orderNo;

        @ExcelNotation(value = "订单金额，保留2位小数，单位：元")
        private Double amount;
    }
}
