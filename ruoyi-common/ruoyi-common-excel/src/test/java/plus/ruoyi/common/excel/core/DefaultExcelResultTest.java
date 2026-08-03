package plus.ruoyi.common.excel.core;

import plus.ruoyi.common.test.base.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DefaultExcelResult 默认Excel结果类测试
 *
 * @author 抓蛙师
 */
@DisplayName("DefaultExcelResult默认Excel结果类测试")
public class DefaultExcelResultTest extends BaseUnitTest {

    // ==================== 构造方法测试 ====================

    @Test
    @DisplayName("测试构造方法-默认构造")
    public void testDefaultConstructor() {
        DefaultExcelResult<String> result = new DefaultExcelResult<>();

        assertNotNull(result.getHead(), "表头不应为null");
        assertNotNull(result.getList(), "数据列表不应为null");
        assertNotNull(result.getErrorList(), "错误列表不应为null");
        assertTrue(result.getHead().isEmpty(), "表头应为空");
        assertTrue(result.getList().isEmpty(), "数据列表应为空");
        assertTrue(result.getErrorList().isEmpty(), "错误列表应为空");
    }

    @Test
    @DisplayName("测试构造方法-带参数构造")
    public void testConstructorWithParams() {
        List<String> dataList = new ArrayList<>();
        dataList.add("data1");
        dataList.add("data2");

        List<String> errorList = new ArrayList<>();
        errorList.add("error1");

        DefaultExcelResult<String> result = new DefaultExcelResult<>(dataList, errorList);

        assertEquals(2, result.getList().size(), "数据列表应有2条");
        assertEquals(1, result.getErrorList().size(), "错误列表应有1条");
        assertNotNull(result.getHead(), "表头不应为null");
    }

    @Test
    @DisplayName("测试构造方法-复制构造")
    public void testCopyConstructor() {
        // 创建原始结果
        DefaultExcelResult<String> original = new DefaultExcelResult<>();
        original.getHead().put(0, "姓名");
        original.getHead().put(1, "年龄");
        original.getList().add("张三");
        original.getErrorList().add("第3行数据错误");

        // 复制构造
        DefaultExcelResult<String> copy = new DefaultExcelResult<>(original);

        assertEquals(original.getHead(), copy.getHead(), "表头应相同");
        assertEquals(original.getList(), copy.getList(), "数据列表应相同");
        assertEquals(original.getErrorList(), copy.getErrorList(), "错误列表应相同");
    }

    // ==================== Setter/Getter测试 ====================

    @Test
    @DisplayName("测试Setter/Getter-head")
    public void testSetGetHead() {
        DefaultExcelResult<String> result = new DefaultExcelResult<>();
        Map<Integer, String> head = new HashMap<>();
        head.put(0, "列1");
        head.put(1, "列2");

        result.setHead(head);

        assertEquals(head, result.getHead(), "表头应正确设置");
        assertEquals("列1", result.getHead().get(0), "第一列标题应为列1");
        assertEquals("列2", result.getHead().get(1), "第二列标题应为列2");
    }

    @Test
    @DisplayName("测试Setter/Getter-list")
    public void testSetGetList() {
        DefaultExcelResult<String> result = new DefaultExcelResult<>();
        List<String> list = new ArrayList<>();
        list.add("数据1");
        list.add("数据2");

        result.setList(list);

        assertEquals(list, result.getList(), "数据列表应正确设置");
        assertEquals(2, result.getList().size(), "数据列表应有2条");
    }

    @Test
    @DisplayName("测试Setter/Getter-errorList")
    public void testSetGetErrorList() {
        DefaultExcelResult<String> result = new DefaultExcelResult<>();
        List<String> errorList = new ArrayList<>();
        errorList.add("错误1");
        errorList.add("错误2");

        result.setErrorList(errorList);

        assertEquals(errorList, result.getErrorList(), "错误列表应正确设置");
        assertEquals(2, result.getErrorList().size(), "错误列表应有2条");
    }

    // ==================== getAnalysis测试 ====================

    @Test
    @DisplayName("测试getAnalysis-全部成功")
    public void testGetAnalysisAllSuccess() {
        DefaultExcelResult<String> result = new DefaultExcelResult<>();
        result.getList().add("数据1");
        result.getList().add("数据2");
        result.getList().add("数据3");

        String analysis = result.getAnalysis();

        assertTrue(analysis.contains("全部读取成功"), "应包含全部读取成功");
        assertTrue(analysis.contains("3"), "应包含成功数量3");
    }

    @Test
    @DisplayName("测试getAnalysis-部分成功")
    public void testGetAnalysisPartialSuccess() {
        DefaultExcelResult<String> result = new DefaultExcelResult<>();
        result.getList().add("数据1");
        result.getList().add("数据2");
        result.getErrorList().add("错误1");

        String analysis = result.getAnalysis();

        assertTrue(analysis.contains("成功") && analysis.contains("2"), "应包含成功2条");
        assertTrue(analysis.contains("失败") && analysis.contains("1"), "应包含失败1条");
    }

    @Test
    @DisplayName("测试getAnalysis-全部失败")
    public void testGetAnalysisAllFailed() {
        DefaultExcelResult<String> result = new DefaultExcelResult<>();
        // 没有成功数据，只有错误
        result.getErrorList().add("错误1");

        String analysis = result.getAnalysis();

        assertTrue(analysis.contains("读取失败"), "应包含读取失败");
        assertTrue(analysis.contains("未解析到数据"), "应包含未解析到数据");
    }

    @Test
    @DisplayName("测试getAnalysis-空数据")
    public void testGetAnalysisEmpty() {
        DefaultExcelResult<String> result = new DefaultExcelResult<>();

        String analysis = result.getAnalysis();

        assertTrue(analysis.contains("读取失败"), "应包含读取失败");
    }

    // ==================== 接口实现测试 ====================

    @Test
    @DisplayName("测试接口实现-ExcelResult")
    public void testExcelResultInterface() {
        DefaultExcelResult<String> result = new DefaultExcelResult<>();

        assertTrue(result instanceof ExcelResult, "应实现ExcelResult接口");
    }

    // ==================== 泛型测试 ====================

    @Test
    @DisplayName("测试泛型-Integer类型")
    public void testGenericInteger() {
        DefaultExcelResult<Integer> result = new DefaultExcelResult<>();
        result.getList().add(1);
        result.getList().add(2);
        result.getList().add(3);

        assertEquals(3, result.getList().size(), "应有3条Integer数据");
        assertEquals(Integer.valueOf(1), result.getList().get(0), "第一条数据应为1");
    }

    @Test
    @DisplayName("测试泛型-自定义对象类型")
    public void testGenericCustomObject() {
        DefaultExcelResult<TestData> result = new DefaultExcelResult<>();
        TestData data = new TestData("测试", 100);
        result.getList().add(data);

        assertEquals(1, result.getList().size(), "应有1条数据");
        assertEquals("测试", result.getList().get(0).name, "名称应为测试");
        assertEquals(100, result.getList().get(0).value, "值应为100");
    }

    // ==================== 业务场景测试 ====================

    @Test
    @DisplayName("测试业务场景-模拟Excel导入结果")
    public void testExcelImportScenario() {
        DefaultExcelResult<Map<String, Object>> result = new DefaultExcelResult<>();

        // 设置表头
        result.getHead().put(0, "姓名");
        result.getHead().put(1, "年龄");
        result.getHead().put(2, "邮箱");

        // 添加成功数据
        Map<String, Object> row1 = new HashMap<>();
        row1.put("name", "张三");
        row1.put("age", 25);
        row1.put("email", "zhangsan@example.com");
        result.getList().add(row1);

        Map<String, Object> row2 = new HashMap<>();
        row2.put("name", "李四");
        row2.put("age", 30);
        row2.put("email", "lisi@example.com");
        result.getList().add(row2);

        // 添加错误信息
        result.getErrorList().add("第5行-年龄格式错误");

        // 验证
        assertEquals(3, result.getHead().size(), "表头应有3列");
        assertEquals(2, result.getList().size(), "成功数据应有2条");
        assertEquals(1, result.getErrorList().size(), "错误信息应有1条");

        String analysis = result.getAnalysis();
        assertTrue(analysis.contains("成功") && analysis.contains("2"), "分析应显示成功2条");
        assertTrue(analysis.contains("失败") && analysis.contains("1"), "分析应显示失败1条");
    }

    @Test
    @DisplayName("测试业务场景-大量数据导入")
    public void testLargeDataImport() {
        DefaultExcelResult<String> result = new DefaultExcelResult<>();

        // 模拟1000条成功数据
        for (int i = 0; i < 1000; i++) {
            result.getList().add("数据" + i);
        }

        // 模拟50条错误
        for (int i = 0; i < 50; i++) {
            result.getErrorList().add("第" + (i + 1001) + "行错误");
        }

        assertEquals(1000, result.getList().size(), "成功数据应有1000条");
        assertEquals(50, result.getErrorList().size(), "错误数据应有50条");

        String analysis = result.getAnalysis();
        assertTrue(analysis.contains("1000"), "分析应包含1000");
        assertTrue(analysis.contains("50"), "分析应包含50");
    }

    @Test
    @DisplayName("测试业务场景-只有表头无数据")
    public void testOnlyHeaderNoData() {
        DefaultExcelResult<String> result = new DefaultExcelResult<>();
        result.getHead().put(0, "列1");
        result.getHead().put(1, "列2");

        assertEquals(2, result.getHead().size(), "表头应有2列");
        assertTrue(result.getList().isEmpty(), "数据列表应为空");
        assertTrue(result.getAnalysis().contains("读取失败"), "分析应显示读取失败");
    }

    // ==================== 边界值测试 ====================

    @Test
    @DisplayName("测试边界值-null参数构造")
    public void testNullParamsConstructor() {
        DefaultExcelResult<String> result = new DefaultExcelResult<>(null, null);

        assertNull(result.getList(), "list应为null");
        assertNull(result.getErrorList(), "errorList应为null");
    }

    @Test
    @DisplayName("测试边界值-空列表参数构造")
    public void testEmptyListConstructor() {
        List<String> emptyList = new ArrayList<>();
        List<String> emptyErrorList = new ArrayList<>();

        DefaultExcelResult<String> result = new DefaultExcelResult<>(emptyList, emptyErrorList);

        assertTrue(result.getList().isEmpty(), "数据列表应为空");
        assertTrue(result.getErrorList().isEmpty(), "错误列表应为空");
    }

    @Test
    @DisplayName("测试边界值-单条成功数据")
    public void testSingleSuccessData() {
        DefaultExcelResult<String> result = new DefaultExcelResult<>();
        result.getList().add("唯一数据");

        String analysis = result.getAnalysis();
        assertTrue(analysis.contains("1"), "应包含数量1");
        assertTrue(analysis.contains("全部读取成功"), "应显示全部成功");
    }

    @Test
    @DisplayName("测试边界值-单条错误数据")
    public void testSingleErrorData() {
        DefaultExcelResult<String> result = new DefaultExcelResult<>();
        result.getList().add("数据");
        result.getErrorList().add("错误");

        String analysis = result.getAnalysis();
        assertTrue(analysis.contains("成功") && analysis.contains("1"), "应显示成功1条");
        assertTrue(analysis.contains("失败") && analysis.contains("1"), "应显示失败1条");
    }

    // ==================== 辅助类 ====================

    /**
     * 测试用数据类
     */
    private static class TestData {
        String name;
        int value;

        TestData(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }
}
