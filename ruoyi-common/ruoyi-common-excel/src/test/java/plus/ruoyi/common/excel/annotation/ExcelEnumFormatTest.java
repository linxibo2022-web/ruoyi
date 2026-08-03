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
 * ExcelEnumFormat 枚举格式化注解测试
 *
 * @author 抓蛙师
 */
@DisplayName("ExcelEnumFormat枚举格式化注解测试")
public class ExcelEnumFormatTest extends BaseUnitTest {

    // ==================== 注解元信息测试 ====================

    @Test
    @DisplayName("测试注解-Target应为FIELD")
    public void testAnnotationTarget() {
        Target target = ExcelEnumFormat.class.getAnnotation(Target.class);

        assertNotNull(target, "应有Target注解");
        assertEquals(1, target.value().length, "应只有1个目标");
        assertEquals(ElementType.FIELD, target.value()[0], "目标应为FIELD");
    }

    @Test
    @DisplayName("测试注解-Retention应为RUNTIME")
    public void testAnnotationRetention() {
        Retention retention = ExcelEnumFormat.class.getAnnotation(Retention.class);

        assertNotNull(retention, "应有Retention注解");
        assertEquals(RetentionPolicy.RUNTIME, retention.value(), "保留策略应为RUNTIME");
    }

    @Test
    @DisplayName("测试注解-应可被继承")
    public void testAnnotationInherited() {
        assertTrue(ExcelEnumFormat.class.isAnnotationPresent(java.lang.annotation.Inherited.class),
            "应有Inherited注解");
    }

    // ==================== 默认值测试 ====================

    @Test
    @DisplayName("测试默认值-valueField默认为value")
    public void testDefaultValueField() throws Exception {
        Field field = TestClass.class.getDeclaredField("defaultField");
        ExcelEnumFormat annotation = field.getAnnotation(ExcelEnumFormat.class);

        assertEquals("value", annotation.valueField(), "valueField默认应为value");
    }

    @Test
    @DisplayName("测试默认值-labelField默认为label")
    public void testDefaultLabelField() throws Exception {
        Field field = TestClass.class.getDeclaredField("defaultField");
        ExcelEnumFormat annotation = field.getAnnotation(ExcelEnumFormat.class);

        assertEquals("label", annotation.labelField(), "labelField默认应为label");
    }

    // ==================== enumClass测试 ====================

    @Test
    @DisplayName("测试enumClass-用户状态枚举")
    public void testEnumClassUserStatus() throws Exception {
        Field field = TestClass.class.getDeclaredField("statusField");
        ExcelEnumFormat annotation = field.getAnnotation(ExcelEnumFormat.class);

        assertEquals(UserStatus.class, annotation.enumClass(), "enumClass应为UserStatus");
    }

    @Test
    @DisplayName("测试enumClass-订单状态枚举")
    public void testEnumClassOrderStatus() throws Exception {
        Field field = TestClass.class.getDeclaredField("orderStatusField");
        ExcelEnumFormat annotation = field.getAnnotation(ExcelEnumFormat.class);

        assertEquals(OrderStatus.class, annotation.enumClass(), "enumClass应为OrderStatus");
    }

    // ==================== valueField测试 ====================

    @Test
    @DisplayName("测试valueField-自定义code字段")
    public void testCustomValueFieldCode() throws Exception {
        Field field = TestClass.class.getDeclaredField("codeValueField");
        ExcelEnumFormat annotation = field.getAnnotation(ExcelEnumFormat.class);

        assertEquals("code", annotation.valueField(), "valueField应为code");
    }

    @Test
    @DisplayName("测试valueField-自定义status字段")
    public void testCustomValueFieldStatus() throws Exception {
        Field field = TestClass.class.getDeclaredField("statusValueField");
        ExcelEnumFormat annotation = field.getAnnotation(ExcelEnumFormat.class);

        assertEquals("status", annotation.valueField(), "valueField应为status");
    }

    // ==================== labelField测试 ====================

    @Test
    @DisplayName("测试labelField-自定义name字段")
    public void testCustomLabelFieldName() throws Exception {
        Field field = TestClass.class.getDeclaredField("nameLabelField");
        ExcelEnumFormat annotation = field.getAnnotation(ExcelEnumFormat.class);

        assertEquals("name", annotation.labelField(), "labelField应为name");
    }

    @Test
    @DisplayName("测试labelField-自定义desc字段")
    public void testCustomLabelFieldDesc() throws Exception {
        Field field = TestClass.class.getDeclaredField("descLabelField");
        ExcelEnumFormat annotation = field.getAnnotation(ExcelEnumFormat.class);

        assertEquals("desc", annotation.labelField(), "labelField应为desc");
    }

    // ==================== 组合使用测试 ====================

    @Test
    @DisplayName("测试组合-所有自定义字段")
    public void testAllCustomFields() throws Exception {
        Field field = TestClass.class.getDeclaredField("fullConfigField");
        ExcelEnumFormat annotation = field.getAnnotation(ExcelEnumFormat.class);

        assertEquals(GenderEnum.class, annotation.enumClass(), "enumClass应为GenderEnum");
        assertEquals("code", annotation.valueField(), "valueField应为code");
        assertEquals("name", annotation.labelField(), "labelField应为name");
    }

    // ==================== 业务场景测试 ====================

    @Test
    @DisplayName("测试业务场景-用户状态转换")
    public void testUserStatusScenario() throws Exception {
        Field field = UserVo.class.getDeclaredField("status");
        ExcelEnumFormat annotation = field.getAnnotation(ExcelEnumFormat.class);

        assertNotNull(annotation, "status应有ExcelEnumFormat注解");
        assertEquals(UserStatus.class, annotation.enumClass(), "应使用UserStatus枚举");

        // 验证枚举类有对应的字段
        UserStatus[] statuses = UserStatus.values();
        assertTrue(statuses.length > 0, "枚举应有值");
    }

    @Test
    @DisplayName("测试业务场景-订单状态转换")
    public void testOrderStatusScenario() throws Exception {
        Field field = OrderVo.class.getDeclaredField("status");
        ExcelEnumFormat annotation = field.getAnnotation(ExcelEnumFormat.class);

        assertNotNull(annotation, "status应有ExcelEnumFormat注解");
        assertEquals(OrderStatus.class, annotation.enumClass(), "应使用OrderStatus枚举");
    }

    @Test
    @DisplayName("测试业务场景-性别转换")
    public void testGenderScenario() throws Exception {
        Field field = UserVo.class.getDeclaredField("gender");
        ExcelEnumFormat annotation = field.getAnnotation(ExcelEnumFormat.class);

        assertNotNull(annotation, "gender应有ExcelEnumFormat注解");
        assertEquals(GenderEnum.class, annotation.enumClass(), "应使用GenderEnum枚举");
        assertEquals("code", annotation.valueField(), "valueField应为code");
        assertEquals("name", annotation.labelField(), "labelField应为name");
    }

    // ==================== 反射获取测试 ====================

    @Test
    @DisplayName("测试反射-获取所有带注解字段")
    public void testGetAllAnnotatedFields() {
        Field[] fields = TestClass.class.getDeclaredFields();
        int annotatedCount = 0;

        for (Field field : fields) {
            if (field.isAnnotationPresent(ExcelEnumFormat.class)) {
                annotatedCount++;
            }
        }

        assertTrue(annotatedCount > 0, "应有带ExcelEnumFormat注解的字段");
    }

    @Test
    @DisplayName("测试反射-无注解字段")
    public void testFieldWithoutAnnotation() throws Exception {
        Field field = TestClass.class.getDeclaredField("noAnnotationField");
        ExcelEnumFormat annotation = field.getAnnotation(ExcelEnumFormat.class);

        assertNull(annotation, "无注解字段应返回null");
    }

    // ==================== 枚举类型验证测试 ====================

    @Test
    @DisplayName("测试枚举-UserStatus枚举值")
    public void testUserStatusEnumValues() {
        UserStatus[] values = UserStatus.values();

        assertEquals(2, values.length, "UserStatus应有2个值");
        assertEquals(UserStatus.ACTIVE, UserStatus.valueOf("ACTIVE"), "应包含ACTIVE");
        assertEquals(UserStatus.INACTIVE, UserStatus.valueOf("INACTIVE"), "应包含INACTIVE");
    }

    @Test
    @DisplayName("测试枚举-OrderStatus枚举值")
    public void testOrderStatusEnumValues() {
        OrderStatus[] values = OrderStatus.values();

        assertEquals(4, values.length, "OrderStatus应有4个值");
    }

    @Test
    @DisplayName("测试枚举-GenderEnum枚举值")
    public void testGenderEnumValues() {
        GenderEnum[] values = GenderEnum.values();

        assertEquals(3, values.length, "GenderEnum应有3个值");
    }

    // ==================== 边界值测试 ====================

    @Test
    @DisplayName("测试边界值-枚举类属性访问")
    public void testEnumFieldAccess() {
        UserStatus active = UserStatus.ACTIVE;

        assertEquals(1, active.getValue(), "ACTIVE的value应为1");
        assertEquals("激活", active.getLabel(), "ACTIVE的label应为激活");
    }

    // ==================== 测试用枚举 ====================

    /**
     * 用户状态枚举
     */
    public enum UserStatus {
        ACTIVE(1, "激活"),
        INACTIVE(0, "禁用");

        private final Integer value;
        private final String label;

        UserStatus(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        public Integer getValue() {
            return value;
        }

        public String getLabel() {
            return label;
        }
    }

    /**
     * 订单状态枚举
     */
    public enum OrderStatus {
        PENDING(0, "待处理"),
        PROCESSING(1, "处理中"),
        COMPLETED(2, "已完成"),
        CANCELLED(3, "已取消");

        private final Integer value;
        private final String label;

        OrderStatus(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        public Integer getValue() {
            return value;
        }

        public String getLabel() {
            return label;
        }
    }

    /**
     * 性别枚举 - 使用不同字段名
     */
    public enum GenderEnum {
        MALE(0, "男"),
        FEMALE(1, "女"),
        UNKNOWN(2, "未知");

        private final Integer code;
        private final String name;

        GenderEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        public Integer getCode() {
            return code;
        }

        public String getName() {
            return name;
        }
    }

    // ==================== 测试用类 ====================

    /**
     * 测试用类 - 包含各种ExcelEnumFormat配置
     */
    private static class TestClass {
        @ExcelEnumFormat(enumClass = UserStatus.class)
        private Integer defaultField;

        @ExcelEnumFormat(enumClass = UserStatus.class)
        private Integer statusField;

        @ExcelEnumFormat(enumClass = OrderStatus.class)
        private Integer orderStatusField;

        @ExcelEnumFormat(enumClass = UserStatus.class, valueField = "code")
        private Integer codeValueField;

        @ExcelEnumFormat(enumClass = UserStatus.class, valueField = "status")
        private Integer statusValueField;

        @ExcelEnumFormat(enumClass = UserStatus.class, labelField = "name")
        private Integer nameLabelField;

        @ExcelEnumFormat(enumClass = UserStatus.class, labelField = "desc")
        private Integer descLabelField;

        @ExcelEnumFormat(enumClass = GenderEnum.class, valueField = "code", labelField = "name")
        private Integer fullConfigField;

        private String noAnnotationField;
    }

    /**
     * 用户VO - 业务场景测试
     */
    private static class UserVo {
        private Long id;
        private String userName;

        @ExcelEnumFormat(enumClass = UserStatus.class)
        private Integer status;

        @ExcelEnumFormat(enumClass = GenderEnum.class, valueField = "code", labelField = "name")
        private Integer gender;
    }

    /**
     * 订单VO - 业务场景测试
     */
    private static class OrderVo {
        private Long id;
        private String orderNo;

        @ExcelEnumFormat(enumClass = OrderStatus.class)
        private Integer status;
    }
}
