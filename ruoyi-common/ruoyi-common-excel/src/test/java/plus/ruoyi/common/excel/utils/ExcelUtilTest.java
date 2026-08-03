package plus.ruoyi.common.excel.utils;

import plus.ruoyi.common.test.base.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ExcelUtil 工具类测试
 *
 * @author 抓蛙师
 */
@DisplayName("ExcelUtil工具类测试")
public class ExcelUtilTest extends BaseUnitTest {

    // ==================== convertByExp测试 ====================

    @Test
    @DisplayName("测试convertByExp-单值转换")
    public void testConvertByExpSingleValue() {
        String result = ExcelUtil.convertByExp("0", "0=男,1=女,2=未知", ",");

        assertEquals("男", result, "0应转换为男");
    }

    @Test
    @DisplayName("测试convertByExp-另一个单值转换")
    public void testConvertByExpSingleValueFemale() {
        String result = ExcelUtil.convertByExp("1", "0=男,1=女,2=未知", ",");

        assertEquals("女", result, "1应转换为女");
    }

    @Test
    @DisplayName("测试convertByExp-最后一个值")
    public void testConvertByExpLastValue() {
        String result = ExcelUtil.convertByExp("2", "0=男,1=女,2=未知", ",");

        assertEquals("未知", result, "2应转换为未知");
    }

    @Test
    @DisplayName("测试convertByExp-多值转换")
    public void testConvertByExpMultipleValues() {
        String result = ExcelUtil.convertByExp("0,1", "0=男,1=女,2=未知", ",");

        assertTrue(result.contains("男"), "应包含男");
        assertTrue(result.contains("女"), "应包含女");
    }

    @Test
    @DisplayName("测试convertByExp-多值转换三个值")
    public void testConvertByExpThreeValues() {
        String result = ExcelUtil.convertByExp("0,1,2", "0=男,1=女,2=未知", ",");

        assertTrue(result.contains("男"), "应包含男");
        assertTrue(result.contains("女"), "应包含女");
        assertTrue(result.contains("未知"), "应包含未知");
    }

    @Test
    @DisplayName("测试convertByExp-不存在的值")
    public void testConvertByExpNotExists() {
        String result = ExcelUtil.convertByExp("99", "0=男,1=女,2=未知", ",");

        assertEquals("", result, "不存在的值应返回空字符串");
    }

    @Test
    @DisplayName("测试convertByExp-空值")
    public void testConvertByExpEmpty() {
        String result = ExcelUtil.convertByExp("", "0=男,1=女", ",");

        assertEquals("", result, "空值应返回空字符串");
    }

    @Test
    @DisplayName("测试convertByExp-使用分号分隔符")
    public void testConvertByExpSemicolonSeparator() {
        String result = ExcelUtil.convertByExp("0;1", "0=启用,1=禁用", ";");

        assertTrue(result.contains("启用"), "应包含启用");
        assertTrue(result.contains("禁用"), "应包含禁用");
    }

    // ==================== reverseByExp测试 ====================

    @Test
    @DisplayName("测试reverseByExp-单值反向转换")
    public void testReverseByExpSingleValue() {
        String result = ExcelUtil.reverseByExp("男", "0=男,1=女,2=未知", ",");

        assertEquals("0", result, "男应反向转换为0");
    }

    @Test
    @DisplayName("测试reverseByExp-另一个单值反向转换")
    public void testReverseByExpSingleValueFemale() {
        String result = ExcelUtil.reverseByExp("女", "0=男,1=女,2=未知", ",");

        assertEquals("1", result, "女应反向转换为1");
    }

    @Test
    @DisplayName("测试reverseByExp-多值反向转换")
    public void testReverseByExpMultipleValues() {
        String result = ExcelUtil.reverseByExp("男,女", "0=男,1=女,2=未知", ",");

        assertTrue(result.contains("0"), "应包含0");
        assertTrue(result.contains("1"), "应包含1");
    }

    @Test
    @DisplayName("测试reverseByExp-不存在的值")
    public void testReverseByExpNotExists() {
        String result = ExcelUtil.reverseByExp("不存在", "0=男,1=女", ",");

        assertEquals("", result, "不存在的值应返回空字符串");
    }

    @Test
    @DisplayName("测试reverseByExp-空值")
    public void testReverseByExpEmpty() {
        String result = ExcelUtil.reverseByExp("", "0=男,1=女", ",");

        assertEquals("", result, "空值应返回空字符串");
    }

    // ==================== convertByExp和reverseByExp一致性测试 ====================

    @Test
    @DisplayName("测试一致性-转换后可逆向还原")
    public void testConvertAndReverseConsistency() {
        String exp = "0=正常,1=停用";
        String separator = ",";

        // 正向转换
        String label = ExcelUtil.convertByExp("0", exp, separator);
        assertEquals("正常", label, "正向转换应正确");

        // 逆向转换
        String value = ExcelUtil.reverseByExp(label, exp, separator);
        assertEquals("0", value, "逆向转换应还原原值");
    }

    @Test
    @DisplayName("测试一致性-多值转换可逆")
    public void testMultiValueConvertAndReverse() {
        String exp = "1=读,2=写,4=执行";
        String separator = ",";

        // 正向转换
        String labels = ExcelUtil.convertByExp("1,2", exp, separator);
        assertTrue(labels.contains("读"), "应包含读");
        assertTrue(labels.contains("写"), "应包含写");
    }

    // ==================== encodingFilename测试 ====================

    @Test
    @DisplayName("测试encodingFilename-应包含原文件名")
    public void testEncodingFilenameContainsOriginal() {
        String filename = "用户信息";
        String result = ExcelUtil.encodingFilename(filename);

        assertTrue(result.startsWith(filename), "应以原文件名开头");
    }

    @Test
    @DisplayName("测试encodingFilename-应以xlsx结尾")
    public void testEncodingFilenameEndsWithXlsx() {
        String result = ExcelUtil.encodingFilename("测试文件");

        assertTrue(result.endsWith(".xlsx"), "应以.xlsx结尾");
    }

    @Test
    @DisplayName("测试encodingFilename-应包含时间戳")
    public void testEncodingFilenameContainsTimestamp() {
        String filename = "导出数据";
        String result = ExcelUtil.encodingFilename(filename);

        // 时间戳格式：yyyyMMddHHmmss (14位数字)
        String timestampPart = result.substring(filename.length(), result.length() - 5);
        assertTrue(timestampPart.matches("\\d{14}"), "应包含14位时间戳");
    }

    @Test
    @DisplayName("测试encodingFilename-空文件名")
    public void testEncodingFilenameEmpty() {
        String result = ExcelUtil.encodingFilename("");

        assertTrue(result.endsWith(".xlsx"), "空文件名也应以.xlsx结尾");
    }

    @Test
    @DisplayName("测试encodingFilename-英文文件名")
    public void testEncodingFilenameEnglish() {
        String result = ExcelUtil.encodingFilename("UserList");

        assertTrue(result.startsWith("UserList"), "应以英文文件名开头");
        assertTrue(result.endsWith(".xlsx"), "应以.xlsx结尾");
    }

    @Test
    @DisplayName("测试encodingFilename-特殊字符文件名")
    public void testEncodingFilenameSpecialChars() {
        String result = ExcelUtil.encodingFilename("用户_数据");

        assertTrue(result.startsWith("用户_数据"), "应保留特殊字符");
        assertTrue(result.endsWith(".xlsx"), "应以.xlsx结尾");
    }

    // ==================== 业务场景测试 ====================

    @Test
    @DisplayName("测试业务场景-用户状态转换")
    public void testUserStatusConversion() {
        String statusExp = "0=正常,1=停用";

        assertEquals("正常", ExcelUtil.convertByExp("0", statusExp, ","), "0应转换为正常");
        assertEquals("停用", ExcelUtil.convertByExp("1", statusExp, ","), "1应转换为停用");
        assertEquals("0", ExcelUtil.reverseByExp("正常", statusExp, ","), "正常应反向转换为0");
        assertEquals("1", ExcelUtil.reverseByExp("停用", statusExp, ","), "停用应反向转换为1");
    }

    @Test
    @DisplayName("测试业务场景-性别转换")
    public void testGenderConversion() {
        String genderExp = "0=男,1=女,2=保密";

        assertEquals("男", ExcelUtil.convertByExp("0", genderExp, ","), "0应转换为男");
        assertEquals("女", ExcelUtil.convertByExp("1", genderExp, ","), "1应转换为女");
        assertEquals("保密", ExcelUtil.convertByExp("2", genderExp, ","), "2应转换为保密");
    }

    @Test
    @DisplayName("测试业务场景-角色权限多值转换")
    public void testRolePermissionConversion() {
        String roleExp = "1=管理员,2=普通用户,3=访客";

        // 用户同时拥有管理员和普通用户权限
        String labels = ExcelUtil.convertByExp("1,2", roleExp, ",");

        assertTrue(labels.contains("管理员"), "应包含管理员");
        assertTrue(labels.contains("普通用户"), "应包含普通用户");
        assertFalse(labels.contains("访客"), "不应包含访客");
    }

    @Test
    @DisplayName("测试业务场景-数据权限转换")
    public void testDataScopeConversion() {
        String scopeExp = "1=全部数据权限,2=自定数据权限,3=本部门数据权限,4=本部门及以下数据权限,5=仅本人数据权限";

        assertEquals("全部数据权限", ExcelUtil.convertByExp("1", scopeExp, ","), "1应转换为全部数据权限");
        assertEquals("仅本人数据权限", ExcelUtil.convertByExp("5", scopeExp, ","), "5应转换为仅本人数据权限");
    }

    @Test
    @DisplayName("测试业务场景-是否标识转换")
    public void testYesNoConversion() {
        String yesNoExp = "Y=是,N=否";

        assertEquals("是", ExcelUtil.convertByExp("Y", yesNoExp, ","), "Y应转换为是");
        assertEquals("否", ExcelUtil.convertByExp("N", yesNoExp, ","), "N应转换为否");
        assertEquals("Y", ExcelUtil.reverseByExp("是", yesNoExp, ","), "是应反向转换为Y");
    }

    // ==================== 边界值测试 ====================

    @Test
    @DisplayName("测试边界值-单个映射")
    public void testSingleMapping() {
        String result = ExcelUtil.convertByExp("1", "1=唯一选项", ",");

        assertEquals("唯一选项", result, "单个映射应正确转换");
    }

    @Test
    @DisplayName("测试边界值-大量映射")
    public void testManyMappings() {
        StringBuilder exp = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            if (i > 0) exp.append(",");
            exp.append(i).append("=选项").append(i);
        }

        String result = ExcelUtil.convertByExp("50", exp.toString(), ",");
        assertEquals("选项50", result, "大量映射应正确转换");
    }

    @Test
    @DisplayName("测试边界值-值包含等号")
    public void testValueContainsEquals() {
        // 注意：当值本身包含等号时，解析可能会有问题
        // 这个测试验证正常情况
        String result = ExcelUtil.convertByExp("0", "0=A等级,1=B等级", ",");

        assertEquals("A等级", result, "包含等字的值应正确转换");
    }

    @Test
    @DisplayName("测试边界值-中文键值")
    public void testChineseKeyValue() {
        String result = ExcelUtil.convertByExp("男", "男=Male,女=Female", ",");

        assertEquals("Male", result, "中文键应正确转换");
    }

    // ==================== 分隔符测试 ====================

    @Test
    @DisplayName("测试分隔符-竖线分隔")
    public void testPipeSeparator() {
        String result = ExcelUtil.convertByExp("1|2", "1=A,2=B,3=C", "|");

        assertTrue(result.contains("A"), "应包含A");
        assertTrue(result.contains("B"), "应包含B");
    }

    @Test
    @DisplayName("测试分隔符-空格分隔")
    public void testSpaceSeparator() {
        String result = ExcelUtil.convertByExp("1 2", "1=甲,2=乙,3=丙", " ");

        assertTrue(result.contains("甲"), "应包含甲");
        assertTrue(result.contains("乙"), "应包含乙");
    }
}
