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
 * CellMerge 单元格合并注解测试
 *
 * @author 抓蛙师
 */
@DisplayName("CellMerge单元格合并注解测试")
public class CellMergeTest extends BaseUnitTest {

    // ==================== 注解元信息测试 ====================

    @Test
    @DisplayName("测试注解-Target应为FIELD")
    public void testAnnotationTarget() {
        Target target = CellMerge.class.getAnnotation(Target.class);

        assertNotNull(target, "应有Target注解");
        assertEquals(1, target.value().length, "应只有1个目标");
        assertEquals(ElementType.FIELD, target.value()[0], "目标应为FIELD");
    }

    @Test
    @DisplayName("测试注解-Retention应为RUNTIME")
    public void testAnnotationRetention() {
        Retention retention = CellMerge.class.getAnnotation(Retention.class);

        assertNotNull(retention, "应有Retention注解");
        assertEquals(RetentionPolicy.RUNTIME, retention.value(), "保留策略应为RUNTIME");
    }

    @Test
    @DisplayName("测试注解-应可被继承")
    public void testAnnotationInherited() {
        assertTrue(CellMerge.class.isAnnotationPresent(java.lang.annotation.Inherited.class),
            "应有Inherited注解");
    }

    // ==================== 默认值测试 ====================

    @Test
    @DisplayName("测试默认值-index默认为-1")
    public void testDefaultIndex() throws Exception {
        Field field = TestClass.class.getDeclaredField("defaultField");
        CellMerge annotation = field.getAnnotation(CellMerge.class);

        assertEquals(-1, annotation.index(), "index默认值应为-1");
    }

    @Test
    @DisplayName("测试默认值-mergeBy默认为空数组")
    public void testDefaultMergeBy() throws Exception {
        Field field = TestClass.class.getDeclaredField("defaultField");
        CellMerge annotation = field.getAnnotation(CellMerge.class);

        assertNotNull(annotation.mergeBy(), "mergeBy不应为null");
        assertEquals(0, annotation.mergeBy().length, "mergeBy默认应为空数组");
    }

    // ==================== 自定义值测试 ====================

    @Test
    @DisplayName("测试自定义值-index")
    public void testCustomIndex() throws Exception {
        Field field = TestClass.class.getDeclaredField("customIndexField");
        CellMerge annotation = field.getAnnotation(CellMerge.class);

        assertEquals(5, annotation.index(), "index应为设置的值5");
    }

    @Test
    @DisplayName("测试自定义值-mergeBy单个依赖")
    public void testCustomMergeBySingle() throws Exception {
        Field field = TestClass.class.getDeclaredField("singleMergeByField");
        CellMerge annotation = field.getAnnotation(CellMerge.class);

        assertEquals(1, annotation.mergeBy().length, "mergeBy应有1个元素");
        assertEquals("department", annotation.mergeBy()[0], "依赖字段应为department");
    }

    @Test
    @DisplayName("测试自定义值-mergeBy多个依赖")
    public void testCustomMergeByMultiple() throws Exception {
        Field field = TestClass.class.getDeclaredField("multipleMergeByField");
        CellMerge annotation = field.getAnnotation(CellMerge.class);

        assertEquals(2, annotation.mergeBy().length, "mergeBy应有2个元素");
        assertEquals("department", annotation.mergeBy()[0], "第一个依赖应为department");
        assertEquals("team", annotation.mergeBy()[1], "第二个依赖应为team");
    }

    @Test
    @DisplayName("测试自定义值-index和mergeBy同时设置")
    public void testCustomBothValues() throws Exception {
        Field field = TestClass.class.getDeclaredField("fullConfigField");
        CellMerge annotation = field.getAnnotation(CellMerge.class);

        assertEquals(3, annotation.index(), "index应为3");
        assertEquals(1, annotation.mergeBy().length, "mergeBy应有1个元素");
    }

    // ==================== 业务场景测试 ====================

    @Test
    @DisplayName("测试业务场景-部门人员列表合并")
    public void testDepartmentUserMerge() throws Exception {
        // 部门字段 - 相同部门合并
        Field deptField = DepartmentUserVo.class.getDeclaredField("department");
        CellMerge deptAnnotation = deptField.getAnnotation(CellMerge.class);

        assertNotNull(deptAnnotation, "department应有CellMerge注解");
        assertEquals(0, deptAnnotation.mergeBy().length, "部门字段不依赖其他字段");

        // 团队字段 - 依赖部门合并
        Field teamField = DepartmentUserVo.class.getDeclaredField("team");
        CellMerge teamAnnotation = teamField.getAnnotation(CellMerge.class);

        assertNotNull(teamAnnotation, "team应有CellMerge注解");
        assertEquals(1, teamAnnotation.index(), "team的index应为1");
        assertEquals(1, teamAnnotation.mergeBy().length, "team应依赖1个字段");
        assertEquals("department", teamAnnotation.mergeBy()[0], "team应依赖department");
    }

    @Test
    @DisplayName("测试业务场景-订单明细合并")
    public void testOrderDetailMerge() throws Exception {
        // 订单号字段 - 相同订单号合并
        Field orderNoField = OrderDetailVo.class.getDeclaredField("orderNo");
        CellMerge orderNoAnnotation = orderNoField.getAnnotation(CellMerge.class);

        assertNotNull(orderNoAnnotation, "orderNo应有CellMerge注解");

        // 商品分类 - 依赖订单号
        Field categoryField = OrderDetailVo.class.getDeclaredField("category");
        CellMerge categoryAnnotation = categoryField.getAnnotation(CellMerge.class);

        assertEquals(1, categoryAnnotation.mergeBy().length, "category应依赖orderNo");
    }

    // ==================== 反射获取测试 ====================

    @Test
    @DisplayName("测试反射-获取所有带注解字段")
    public void testGetAllAnnotatedFields() {
        Field[] fields = TestClass.class.getDeclaredFields();
        int annotatedCount = 0;

        for (Field field : fields) {
            if (field.isAnnotationPresent(CellMerge.class)) {
                annotatedCount++;
            }
        }

        assertEquals(6, annotatedCount, "应有6个带CellMerge注解的字段");
    }

    @Test
    @DisplayName("测试反射-无注解字段")
    public void testFieldWithoutAnnotation() throws Exception {
        Field field = TestClass.class.getDeclaredField("noAnnotationField");
        CellMerge annotation = field.getAnnotation(CellMerge.class);

        assertNull(annotation, "无注解字段应返回null");
    }

    // ==================== 边界值测试 ====================

    @Test
    @DisplayName("测试边界值-index为0")
    public void testIndexZero() throws Exception {
        Field field = TestClass.class.getDeclaredField("zeroIndexField");
        CellMerge annotation = field.getAnnotation(CellMerge.class);

        assertEquals(0, annotation.index(), "index应为0");
    }

    @Test
    @DisplayName("测试边界值-空mergeBy数组")
    public void testEmptyMergeBy() throws Exception {
        Field field = TestClass.class.getDeclaredField("defaultField");
        CellMerge annotation = field.getAnnotation(CellMerge.class);

        assertEquals(0, annotation.mergeBy().length, "mergeBy应为空数组");
    }

    // ==================== 测试用类 ====================

    /**
     * 测试用类 - 包含各种CellMerge配置
     */
    private static class TestClass {
        @CellMerge
        private String defaultField;

        @CellMerge(index = 5)
        private String customIndexField;

        @CellMerge(mergeBy = {"department"})
        private String singleMergeByField;

        @CellMerge(mergeBy = {"department", "team"})
        private String multipleMergeByField;

        @CellMerge(index = 3, mergeBy = {"parent"})
        private String fullConfigField;

        @CellMerge(index = 0)
        private String zeroIndexField;

        private String noAnnotationField;
    }

    /**
     * 部门人员VO - 业务场景测试
     */
    private static class DepartmentUserVo {
        @CellMerge
        private String department;

        @CellMerge(index = 1, mergeBy = {"department"})
        private String team;

        private String userName;
    }

    /**
     * 订单明细VO - 业务场景测试
     */
    private static class OrderDetailVo {
        @CellMerge
        private String orderNo;

        @CellMerge(mergeBy = {"orderNo"})
        private String category;

        private String productName;
        private Integer quantity;
    }
}
