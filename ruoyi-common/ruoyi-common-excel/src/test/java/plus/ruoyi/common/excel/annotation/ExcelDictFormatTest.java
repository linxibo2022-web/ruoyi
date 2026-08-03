package plus.ruoyi.common.excel.annotation;

import plus.ruoyi.common.core.utils.StringUtils;
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
 * ExcelDictFormat 字典格式化注解测试
 *
 * @author 抓蛙师
 */
@DisplayName("ExcelDictFormat字典格式化注解测试")
public class ExcelDictFormatTest extends BaseUnitTest {

    // ==================== 注解元信息测试 ====================

    @Test
    @DisplayName("测试注解-Target应为FIELD")
    public void testAnnotationTarget() {
        Target target = ExcelDictFormat.class.getAnnotation(Target.class);

        assertNotNull(target, "应有Target注解");
        assertEquals(1, target.value().length, "应只有1个目标");
        assertEquals(ElementType.FIELD, target.value()[0], "目标应为FIELD");
    }

    @Test
    @DisplayName("测试注解-Retention应为RUNTIME")
    public void testAnnotationRetention() {
        Retention retention = ExcelDictFormat.class.getAnnotation(Retention.class);

        assertNotNull(retention, "应有Retention注解");
        assertEquals(RetentionPolicy.RUNTIME, retention.value(), "保留策略应为RUNTIME");
    }

    @Test
    @DisplayName("测试注解-应可被继承")
    public void testAnnotationInherited() {
        assertTrue(ExcelDictFormat.class.isAnnotationPresent(java.lang.annotation.Inherited.class),
            "应有Inherited注解");
    }

    // ==================== 默认值测试 ====================

    @Test
    @DisplayName("测试默认值-dictType默认为空")
    public void testDefaultDictType() throws Exception {
        Field field = TestClass.class.getDeclaredField("defaultField");
        ExcelDictFormat annotation = field.getAnnotation(ExcelDictFormat.class);

        assertEquals("", annotation.dictType(), "dictType默认应为空字符串");
    }

    @Test
    @DisplayName("测试默认值-readConverterExp默认为空")
    public void testDefaultReadConverterExp() throws Exception {
        Field field = TestClass.class.getDeclaredField("defaultField");
        ExcelDictFormat annotation = field.getAnnotation(ExcelDictFormat.class);

        assertEquals("", annotation.readConverterExp(), "readConverterExp默认应为空字符串");
    }

    @Test
    @DisplayName("测试默认值-separator默认为系统分隔符")
    public void testDefaultSeparator() throws Exception {
        Field field = TestClass.class.getDeclaredField("defaultField");
        ExcelDictFormat annotation = field.getAnnotation(ExcelDictFormat.class);

        assertEquals(StringUtils.SEPARATOR, annotation.separator(), "separator默认应为系统分隔符");
    }

    // ==================== dictType测试 ====================

    @Test
    @DisplayName("测试dictType-用户性别字典")
    public void testDictTypeGender() throws Exception {
        Field field = TestClass.class.getDeclaredField("genderField");
        ExcelDictFormat annotation = field.getAnnotation(ExcelDictFormat.class);

        assertEquals("sys_user_gender", annotation.dictType(), "dictType应为sys_user_gender");
    }

    @Test
    @DisplayName("测试dictType-用户状态字典")
    public void testDictTypeStatus() throws Exception {
        Field field = TestClass.class.getDeclaredField("statusField");
        ExcelDictFormat annotation = field.getAnnotation(ExcelDictFormat.class);

        assertEquals("sys_user_status", annotation.dictType(), "dictType应为sys_user_status");
    }

    // ==================== readConverterExp测试 ====================

    @Test
    @DisplayName("测试readConverterExp-简单表达式")
    public void testReadConverterExpSimple() throws Exception {
        Field field = TestClass.class.getDeclaredField("simpleExpField");
        ExcelDictFormat annotation = field.getAnnotation(ExcelDictFormat.class);

        assertEquals("0=男,1=女", annotation.readConverterExp(), "转换表达式应正确");
    }

    @Test
    @DisplayName("测试readConverterExp-多值表达式")
    public void testReadConverterExpMultiple() throws Exception {
        Field field = TestClass.class.getDeclaredField("multiExpField");
        ExcelDictFormat annotation = field.getAnnotation(ExcelDictFormat.class);

        String exp = annotation.readConverterExp();
        assertTrue(exp.contains("0="), "应包含0=");
        assertTrue(exp.contains("1="), "应包含1=");
        assertTrue(exp.contains("2="), "应包含2=");
    }

    // ==================== separator测试 ====================

    @Test
    @DisplayName("测试separator-自定义分隔符逗号")
    public void testSeparatorComma() throws Exception {
        Field field = TestClass.class.getDeclaredField("commaSeparatorField");
        ExcelDictFormat annotation = field.getAnnotation(ExcelDictFormat.class);

        assertEquals(",", annotation.separator(), "分隔符应为逗号");
    }

    @Test
    @DisplayName("测试separator-自定义分隔符分号")
    public void testSeparatorSemicolon() throws Exception {
        Field field = TestClass.class.getDeclaredField("semicolonSeparatorField");
        ExcelDictFormat annotation = field.getAnnotation(ExcelDictFormat.class);

        assertEquals(";", annotation.separator(), "分隔符应为分号");
    }

    // ==================== 组合使用测试 ====================

    @Test
    @DisplayName("测试组合-dictType和separator")
    public void testCombineDictTypeAndSeparator() throws Exception {
        Field field = TestClass.class.getDeclaredField("roleField");
        ExcelDictFormat annotation = field.getAnnotation(ExcelDictFormat.class);

        assertEquals("sys_role", annotation.dictType(), "dictType应为sys_role");
        assertEquals(",", annotation.separator(), "separator应为逗号");
    }

    @Test
    @DisplayName("测试组合-readConverterExp和separator")
    public void testCombineExpAndSeparator() throws Exception {
        Field field = TestClass.class.getDeclaredField("permissionField");
        ExcelDictFormat annotation = field.getAnnotation(ExcelDictFormat.class);

        assertNotEquals("", annotation.readConverterExp(), "readConverterExp不应为空");
        assertEquals("|", annotation.separator(), "separator应为竖线");
    }

    // ==================== 业务场景测试 ====================

    @Test
    @DisplayName("测试业务场景-用户实体类")
    public void testUserEntityScenario() throws Exception {
        // 性别字段
        Field genderField = UserVo.class.getDeclaredField("gender");
        ExcelDictFormat genderAnnotation = genderField.getAnnotation(ExcelDictFormat.class);
        assertNotNull(genderAnnotation, "gender应有ExcelDictFormat注解");
        assertEquals("sys_user_gender", genderAnnotation.dictType(), "gender应使用sys_user_gender字典");

        // 状态字段
        Field statusField = UserVo.class.getDeclaredField("status");
        ExcelDictFormat statusAnnotation = statusField.getAnnotation(ExcelDictFormat.class);
        assertNotNull(statusAnnotation, "status应有ExcelDictFormat注解");
    }

    @Test
    @DisplayName("测试业务场景-无字典使用表达式")
    public void testNoDictUseExpression() throws Exception {
        Field field = UserVo.class.getDeclaredField("isDeleted");
        ExcelDictFormat annotation = field.getAnnotation(ExcelDictFormat.class);

        assertEquals("", annotation.dictType(), "无字典时dictType应为空");
        assertNotEquals("", annotation.readConverterExp(), "应使用转换表达式");
        assertTrue(annotation.readConverterExp().contains("0="), "表达式应包含0=");
    }

    // ==================== 反射获取测试 ====================

    @Test
    @DisplayName("测试反射-获取所有带注解字段")
    public void testGetAllAnnotatedFields() {
        Field[] fields = TestClass.class.getDeclaredFields();
        int annotatedCount = 0;

        for (Field field : fields) {
            if (field.isAnnotationPresent(ExcelDictFormat.class)) {
                annotatedCount++;
            }
        }

        assertTrue(annotatedCount > 0, "应有带ExcelDictFormat注解的字段");
    }

    @Test
    @DisplayName("测试反射-无注解字段")
    public void testFieldWithoutAnnotation() throws Exception {
        Field field = TestClass.class.getDeclaredField("noAnnotationField");
        ExcelDictFormat annotation = field.getAnnotation(ExcelDictFormat.class);

        assertNull(annotation, "无注解字段应返回null");
    }

    // ==================== 边界值测试 ====================

    @Test
    @DisplayName("测试边界值-空dictType")
    public void testEmptyDictType() throws Exception {
        Field field = TestClass.class.getDeclaredField("emptyDictTypeField");
        ExcelDictFormat annotation = field.getAnnotation(ExcelDictFormat.class);

        assertEquals("", annotation.dictType(), "dictType应为空");
    }

    @Test
    @DisplayName("测试边界值-复杂转换表达式")
    public void testComplexExpression() throws Exception {
        Field field = TestClass.class.getDeclaredField("complexExpField");
        ExcelDictFormat annotation = field.getAnnotation(ExcelDictFormat.class);

        String exp = annotation.readConverterExp();
        assertNotNull(exp, "表达式不应为null");
        assertTrue(exp.length() > 0, "表达式不应为空");
    }

    // ==================== 测试用类 ====================

    /**
     * 测试用类 - 包含各种ExcelDictFormat配置
     */
    private static class TestClass {
        @ExcelDictFormat
        private String defaultField;

        @ExcelDictFormat(dictType = "sys_user_gender")
        private String genderField;

        @ExcelDictFormat(dictType = "sys_user_status")
        private String statusField;

        @ExcelDictFormat(readConverterExp = "0=男,1=女")
        private String simpleExpField;

        @ExcelDictFormat(readConverterExp = "0=待处理,1=处理中,2=已完成")
        private String multiExpField;

        @ExcelDictFormat(separator = ",")
        private String commaSeparatorField;

        @ExcelDictFormat(separator = ";")
        private String semicolonSeparatorField;

        @ExcelDictFormat(dictType = "sys_role", separator = ",")
        private String roleField;

        @ExcelDictFormat(readConverterExp = "1=读,2=写,4=执行", separator = "|")
        private String permissionField;

        @ExcelDictFormat(dictType = "")
        private String emptyDictTypeField;

        @ExcelDictFormat(readConverterExp = "0=全部数据权限,1=自定数据权限,2=本部门数据权限,3=本部门及以下数据权限,4=仅本人数据权限")
        private String complexExpField;

        private String noAnnotationField;
    }

    /**
     * 用户VO - 业务场景测试
     */
    private static class UserVo {
        private Long id;
        private String userName;

        @ExcelDictFormat(dictType = "sys_user_gender")
        private String gender;

        @ExcelDictFormat(dictType = "sys_enable_status")
        private String status;

        @ExcelDictFormat(readConverterExp = "0=正常,1=已删除")
        private String isDeleted;
    }
}
