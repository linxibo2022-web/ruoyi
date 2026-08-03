package plus.ruoyi.common.excel.core;

import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.test.base.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DropDownOptions 下拉选项类测试
 *
 * @author 抓蛙师
 */
@DisplayName("DropDownOptions下拉选项类测试")
public class DropDownOptionsTest extends BaseUnitTest {

    // ==================== 构造方法测试 ====================

    @Test
    @DisplayName("测试构造方法-默认构造")
    public void testDefaultConstructor() {
        DropDownOptions options = new DropDownOptions();

        assertEquals(0, options.getIndex(), "默认index应为0");
        assertEquals(0, options.getNextIndex(), "默认nextIndex应为0");
        assertNotNull(options.getOptions(), "options不应为null");
        assertNotNull(options.getNextOptions(), "nextOptions不应为null");
        assertTrue(options.getOptions().isEmpty(), "options应为空");
        assertTrue(options.getNextOptions().isEmpty(), "nextOptions应为空");
    }

    @Test
    @DisplayName("测试构造方法-一级下拉构造")
    public void testSingleLevelConstructor() {
        List<String> optionList = Arrays.asList("选项1", "选项2", "选项3");
        DropDownOptions options = new DropDownOptions(2, optionList);

        assertEquals(2, options.getIndex(), "index应为2");
        assertEquals(3, options.getOptions().size(), "应有3个选项");
        assertTrue(options.getOptions().contains("选项1"), "应包含选项1");
    }

    @Test
    @DisplayName("测试构造方法-全参数构造")
    public void testAllArgsConstructor() {
        List<String> optionList = Arrays.asList("父选项1", "父选项2");
        Map<String, List<String>> nextOptionsMap = new HashMap<>();
        nextOptionsMap.put("父选项1", Arrays.asList("子选项1", "子选项2"));

        DropDownOptions options = new DropDownOptions(1, 2, optionList, nextOptionsMap);

        assertEquals(1, options.getIndex(), "index应为1");
        assertEquals(2, options.getNextIndex(), "nextIndex应为2");
        assertEquals(2, options.getOptions().size(), "应有2个父选项");
        assertEquals(1, options.getNextOptions().size(), "应有1组子选项");
    }

    // ==================== Setter/Getter测试 ====================

    @Test
    @DisplayName("测试Setter/Getter-index")
    public void testSetGetIndex() {
        DropDownOptions options = new DropDownOptions();
        options.setIndex(5);

        assertEquals(5, options.getIndex(), "index应为5");
    }

    @Test
    @DisplayName("测试Setter/Getter-nextIndex")
    public void testSetGetNextIndex() {
        DropDownOptions options = new DropDownOptions();
        options.setNextIndex(3);

        assertEquals(3, options.getNextIndex(), "nextIndex应为3");
    }

    @Test
    @DisplayName("测试Setter/Getter-options")
    public void testSetGetOptions() {
        DropDownOptions options = new DropDownOptions();
        List<String> optionList = Arrays.asList("A", "B", "C");
        options.setOptions(optionList);

        assertEquals(3, options.getOptions().size(), "应有3个选项");
        assertEquals("A", options.getOptions().get(0), "第一个选项应为A");
    }

    @Test
    @DisplayName("测试Setter/Getter-nextOptions")
    public void testSetGetNextOptions() {
        DropDownOptions options = new DropDownOptions();
        Map<String, List<String>> nextOptionsMap = new HashMap<>();
        nextOptionsMap.put("key1", Arrays.asList("v1", "v2"));
        options.setNextOptions(nextOptionsMap);

        assertEquals(1, options.getNextOptions().size(), "应有1组子选项");
        assertTrue(options.getNextOptions().containsKey("key1"), "应包含key1");
    }

    // ==================== createOptionValue测试 ====================

    @Test
    @DisplayName("测试createOptionValue-单个参数")
    public void testCreateOptionValueSingleParam() {
        String result = DropDownOptions.createOptionValue("选项");

        assertEquals("选项", result, "单个参数应直接返回");
    }

    @Test
    @DisplayName("测试createOptionValue-多个参数")
    public void testCreateOptionValueMultipleParams() {
        String result = DropDownOptions.createOptionValue("北京", "朝阳区");

        assertEquals("北京_朝阳区", result, "多个参数应用下划线连接");
    }

    @Test
    @DisplayName("测试createOptionValue-数字参数")
    public void testCreateOptionValueWithNumber() {
        String result = DropDownOptions.createOptionValue("用户", "123");

        assertEquals("用户_123", result, "应支持数字");
    }

    @Test
    @DisplayName("测试createOptionValue-中英文混合")
    public void testCreateOptionValueMixedChinaEnglish() {
        String result = DropDownOptions.createOptionValue("部门A", "Team1");

        assertEquals("部门A_Team1", result, "应支持中英文混合");
    }

    @Test
    @DisplayName("测试createOptionValue-空白字符应抛出异常")
    public void testCreateOptionValueWhitespaceThrowsException() {
        // 包含空格的字符串应抛出异常（正则要求非空白字符）
        assertThrows(ServiceException.class, () -> {
            DropDownOptions.createOptionValue("选项 1");
        }, "包含空格应抛出异常");
    }

    @Test
    @DisplayName("测试createOptionValue-单个数字应抛出异常")
    public void testCreateOptionValueSingleDigitThrowsException() {
        // 根据源码正则 ^\\d_*$，只有单个数字或单个数字后跟下划线会抛出异常
        assertThrows(ServiceException.class, () -> {
            DropDownOptions.createOptionValue("1");
        }, "单个数字应抛出异常");
    }

    @Test
    @DisplayName("测试createOptionValue-三个参数")
    public void testCreateOptionValueThreeParams() {
        String result = DropDownOptions.createOptionValue("省", "市", "区");

        assertEquals("省_市_区", result, "三个参数应用下划线连接");
    }

    // ==================== analyzeOptionValue测试 ====================

    @Test
    @DisplayName("测试analyzeOptionValue-单个值")
    public void testAnalyzeOptionValueSingleValue() {
        List<String> result = DropDownOptions.analyzeOptionValue("选项");

        assertEquals(1, result.size(), "应解析出1个值");
        assertEquals("选项", result.get(0), "值应为选项");
    }

    @Test
    @DisplayName("测试analyzeOptionValue-多个值")
    public void testAnalyzeOptionValueMultipleValues() {
        List<String> result = DropDownOptions.analyzeOptionValue("北京_朝阳区");

        assertEquals(2, result.size(), "应解析出2个值");
        assertEquals("北京", result.get(0), "第一个值应为北京");
        assertEquals("朝阳区", result.get(1), "第二个值应为朝阳区");
    }

    @Test
    @DisplayName("测试analyzeOptionValue-三级值")
    public void testAnalyzeOptionValueThreeLevel() {
        List<String> result = DropDownOptions.analyzeOptionValue("中国_北京_朝阳");

        assertEquals(3, result.size(), "应解析出3个值");
        assertEquals("中国", result.get(0), "第一个值应为中国");
        assertEquals("北京", result.get(1), "第二个值应为北京");
        assertEquals("朝阳", result.get(2), "第三个值应为朝阳");
    }

    @Test
    @DisplayName("测试analyzeOptionValue-空字符串")
    public void testAnalyzeOptionValueEmpty() {
        List<String> result = DropDownOptions.analyzeOptionValue("");

        assertTrue(result.isEmpty(), "空字符串应返回空列表");
    }

    // ==================== buildLinkedOptions测试 ====================

    @Test
    @DisplayName("测试buildLinkedOptions-父子级联")
    public void testBuildLinkedOptions() {
        // 创建父数据
        List<TestEntity> parentList = new ArrayList<>();
        parentList.add(new TestEntity(1L, null, "北京"));
        parentList.add(new TestEntity(2L, null, "上海"));

        // 创建子数据
        List<TestEntity> sonList = new ArrayList<>();
        sonList.add(new TestEntity(11L, 1L, "朝阳区"));
        sonList.add(new TestEntity(12L, 1L, "海淀区"));
        sonList.add(new TestEntity(21L, 2L, "浦东新区"));

        DropDownOptions result = DropDownOptions.buildLinkedOptions(
            parentList, 0,
            sonList, 1,
            TestEntity::getId,
            TestEntity::getParentId,
            TestEntity::getName
        );

        assertEquals(0, result.getIndex(), "父级index应为0");
        assertEquals(1, result.getNextIndex(), "子级nextIndex应为1");
        assertEquals(2, result.getOptions().size(), "应有2个父选项");
        assertTrue(result.getOptions().contains("北京"), "应包含北京");
        assertTrue(result.getOptions().contains("上海"), "应包含上海");
        assertEquals(2, result.getNextOptions().size(), "应有2组子选项");
        assertTrue(result.getNextOptions().containsKey("北京"), "应包含北京的子选项");
        assertEquals(2, result.getNextOptions().get("北京").size(), "北京应有2个区");
    }

    @Test
    @DisplayName("测试buildLinkedOptions-空父列表")
    public void testBuildLinkedOptionsEmptyParent() {
        List<TestEntity> parentList = new ArrayList<>();
        List<TestEntity> sonList = new ArrayList<>();
        sonList.add(new TestEntity(11L, 1L, "子项"));

        DropDownOptions result = DropDownOptions.buildLinkedOptions(
            parentList, 0,
            sonList, 1,
            TestEntity::getId,
            TestEntity::getParentId,
            TestEntity::getName
        );

        assertTrue(result.getOptions().isEmpty(), "父选项应为空");
        assertTrue(result.getNextOptions().isEmpty(), "子选项应为空");
    }

    @Test
    @DisplayName("测试buildLinkedOptions-空子列表")
    public void testBuildLinkedOptionsEmptySon() {
        List<TestEntity> parentList = new ArrayList<>();
        parentList.add(new TestEntity(1L, null, "北京"));

        List<TestEntity> sonList = new ArrayList<>();

        DropDownOptions result = DropDownOptions.buildLinkedOptions(
            parentList, 0,
            sonList, 1,
            TestEntity::getId,
            TestEntity::getParentId,
            TestEntity::getName
        );

        assertEquals(1, result.getOptions().size(), "应有1个父选项");
        assertTrue(result.getNextOptions().isEmpty(), "子选项应为空");
    }

    // ==================== 业务场景测试 ====================

    @Test
    @DisplayName("测试业务场景-性别下拉")
    public void testGenderDropdown() {
        List<String> genders = Arrays.asList("男", "女", "未知");
        DropDownOptions options = new DropDownOptions(2, genders);

        assertEquals(2, options.getIndex(), "性别列应在第3列(index=2)");
        assertEquals(3, options.getOptions().size(), "应有3个性别选项");
    }

    @Test
    @DisplayName("测试业务场景-状态下拉")
    public void testStatusDropdown() {
        List<String> statuses = Arrays.asList("启用", "禁用");
        DropDownOptions options = new DropDownOptions(5, statuses);

        assertEquals(5, options.getIndex(), "状态列应在第6列(index=5)");
        assertEquals(2, options.getOptions().size(), "应有2个状态选项");
    }

    @Test
    @DisplayName("测试业务场景-省市区级联")
    public void testProvinceCityDistrictCascade() {
        DropDownOptions options = new DropDownOptions();
        options.setIndex(0);
        options.setNextIndex(1);

        List<String> provinces = Arrays.asList("北京市", "上海市", "广东省");
        options.setOptions(provinces);

        Map<String, List<String>> cities = new HashMap<>();
        cities.put("北京市", Arrays.asList("东城区", "西城区", "朝阳区"));
        cities.put("上海市", Arrays.asList("黄浦区", "徐汇区", "浦东新区"));
        cities.put("广东省", Arrays.asList("广州市", "深圳市", "东莞市"));
        options.setNextOptions(cities);

        assertEquals(3, options.getOptions().size(), "应有3个省份选项");
        assertEquals(3, options.getNextOptions().size(), "应有3组城市选项");
        assertEquals(3, options.getNextOptions().get("北京市").size(), "北京应有3个区");
    }

    @Test
    @DisplayName("测试业务场景-部门人员级联")
    public void testDepartmentUserCascade() {
        // 部门数据
        List<TestEntity> departments = new ArrayList<>();
        departments.add(new TestEntity(1L, null, "技术部"));
        departments.add(new TestEntity(2L, null, "市场部"));

        // 人员数据
        List<TestEntity> users = new ArrayList<>();
        users.add(new TestEntity(101L, 1L, "张三"));
        users.add(new TestEntity(102L, 1L, "李四"));
        users.add(new TestEntity(201L, 2L, "王五"));

        DropDownOptions result = DropDownOptions.buildLinkedOptions(
            departments, 0,
            users, 1,
            TestEntity::getId,
            TestEntity::getParentId,
            TestEntity::getName
        );

        assertTrue(result.getOptions().contains("技术部"), "应包含技术部");
        assertTrue(result.getOptions().contains("市场部"), "应包含市场部");
        assertEquals(2, result.getNextOptions().get("技术部").size(), "技术部应有2人");
        assertEquals(1, result.getNextOptions().get("市场部").size(), "市场部应有1人");
    }

    // ==================== 边界值测试 ====================

    @Test
    @DisplayName("测试边界值-index为0")
    public void testIndexZero() {
        DropDownOptions options = new DropDownOptions(0, Arrays.asList("A"));

        assertEquals(0, options.getIndex(), "index应为0");
    }

    @Test
    @DisplayName("测试边界值-大index值")
    public void testLargeIndex() {
        DropDownOptions options = new DropDownOptions();
        options.setIndex(100);

        assertEquals(100, options.getIndex(), "应支持大index值");
    }

    @Test
    @DisplayName("测试边界值-单个选项")
    public void testSingleOption() {
        DropDownOptions options = new DropDownOptions(0, Arrays.asList("唯一选项"));

        assertEquals(1, options.getOptions().size(), "应有1个选项");
    }

    @Test
    @DisplayName("测试边界值-大量选项")
    public void testManyOptions() {
        List<String> manyOptions = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            manyOptions.add("选项" + i);
        }

        DropDownOptions options = new DropDownOptions(0, manyOptions);

        assertEquals(1000, options.getOptions().size(), "应支持1000个选项");
    }

    // ==================== createOptionValue和analyzeOptionValue一致性测试 ====================

    @Test
    @DisplayName("测试一致性-创建和解析应可逆")
    public void testCreateAndAnalyzeConsistency() {
        String created = DropDownOptions.createOptionValue("省", "市", "区");
        List<String> analyzed = DropDownOptions.analyzeOptionValue(created);

        assertEquals(3, analyzed.size(), "应解析出3个值");
        assertEquals("省", analyzed.get(0), "第一个值应为省");
        assertEquals("市", analyzed.get(1), "第二个值应为市");
        assertEquals("区", analyzed.get(2), "第三个值应为区");
    }

    @Test
    @DisplayName("测试一致性-中文创建和解析")
    public void testChineseCreateAndAnalyze() {
        String created = DropDownOptions.createOptionValue("北京市", "朝阳区");
        List<String> analyzed = DropDownOptions.analyzeOptionValue(created);

        assertEquals(2, analyzed.size(), "应解析出2个值");
        assertEquals("北京市", analyzed.get(0), "第一个值应为北京市");
        assertEquals("朝阳区", analyzed.get(1), "第二个值应为朝阳区");
    }

    // ==================== 辅助类 ====================

    /**
     * 测试用实体类
     */
    private static class TestEntity {
        private Long id;
        private Long parentId;
        private String name;

        TestEntity(Long id, Long parentId, String name) {
            this.id = id;
            this.parentId = parentId;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public Long getParentId() {
            return parentId;
        }

        public String getName() {
            return name;
        }
    }
}
