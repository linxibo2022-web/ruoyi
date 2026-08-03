package plus.ruoyi.common.excel.convert;

import cn.idev.excel.enums.CellDataTypeEnum;
import cn.idev.excel.metadata.data.WriteCellData;
import plus.ruoyi.common.test.base.BaseUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ExcelBigNumberConvert 大数值转换器测试
 *
 * @author 抓蛙师
 */
@DisplayName("ExcelBigNumberConvert大数值转换器测试")
public class ExcelBigNumberConvertTest extends BaseUnitTest {

    private ExcelBigNumberConvert converter;

    @BeforeEach
    public void setUp() {
        converter = new ExcelBigNumberConvert();
    }

    // ==================== supportJavaTypeKey测试 ====================

    @Test
    @DisplayName("测试supportJavaTypeKey-应返回Long类型")
    public void testSupportJavaTypeKey() {
        Class<Long> result = converter.supportJavaTypeKey();

        assertEquals(Long.class, result, "应支持Long类型");
    }

    // ==================== supportExcelTypeKey测试 ====================

    @Test
    @DisplayName("测试supportExcelTypeKey-应返回null")
    public void testSupportExcelTypeKey() {
        CellDataTypeEnum result = converter.supportExcelTypeKey();

        assertNull(result, "应返回null以支持所有Excel类型");
    }

    // ==================== convertToExcelData测试 - 数值范围 ====================

    @Test
    @DisplayName("测试convertToExcelData-15位以内数值应保持数值格式")
    public void testConvertToExcelDataSmallNumber() {
        Long smallNumber = 123456789012345L; // 15位

        WriteCellData<Object> result = converter.convertToExcelData(smallNumber, null, null);

        assertNotNull(result, "结果不应为null");
        assertEquals(CellDataTypeEnum.NUMBER, result.getType(), "15位以内应为NUMBER类型");
        assertNotNull(result.getNumberValue(), "数值不应为null");
        assertEquals(new BigDecimal(smallNumber), result.getNumberValue(), "数值应正确");
    }

    @Test
    @DisplayName("测试convertToExcelData-超过15位数值应转为字符串")
    public void testConvertToExcelDataLargeNumber() {
        Long largeNumber = 1234567890123456789L; // 19位

        WriteCellData<Object> result = converter.convertToExcelData(largeNumber, null, null);

        assertNotNull(result, "结果不应为null");
        assertEquals("1234567890123456789", result.getStringValue(), "超过15位应转为字符串");
    }

    @Test
    @DisplayName("测试convertToExcelData-正好15位数值")
    public void testConvertToExcelDataExact15Digits() {
        Long exact15 = 999999999999999L; // 15位最大值

        WriteCellData<Object> result = converter.convertToExcelData(exact15, null, null);

        assertEquals(CellDataTypeEnum.NUMBER, result.getType(), "正好15位应为NUMBER类型");
    }

    @Test
    @DisplayName("测试convertToExcelData-正好16位数值")
    public void testConvertToExcelDataExact16Digits() {
        Long exact16 = 1000000000000000L; // 16位

        WriteCellData<Object> result = converter.convertToExcelData(exact16, null, null);

        assertNotNull(result.getStringValue(), "16位应转为字符串");
        assertEquals("1000000000000000", result.getStringValue(), "16位数值应正确转换");
    }

    // ==================== convertToExcelData测试 - 边界值 ====================

    @Test
    @DisplayName("测试convertToExcelData-null值")
    public void testConvertToExcelDataNull() {
        WriteCellData<Object> result = converter.convertToExcelData(null, null, null);

        assertNotNull(result, "结果不应为null");
        assertEquals("", result.getStringValue(), "null应转为空字符串");
    }

    @Test
    @DisplayName("测试convertToExcelData-0值")
    public void testConvertToExcelDataZero() {
        WriteCellData<Object> result = converter.convertToExcelData(0L, null, null);

        assertEquals(CellDataTypeEnum.NUMBER, result.getType(), "0应为NUMBER类型");
        assertEquals(BigDecimal.ZERO, result.getNumberValue(), "0值应正确");
    }

    @Test
    @DisplayName("测试convertToExcelData-负数")
    public void testConvertToExcelDataNegative() {
        Long negative = -12345678901234L;

        WriteCellData<Object> result = converter.convertToExcelData(negative, null, null);

        assertEquals(CellDataTypeEnum.NUMBER, result.getType(), "负数应为NUMBER类型");
    }

    @Test
    @DisplayName("测试convertToExcelData-Long最大值")
    public void testConvertToExcelDataMaxLong() {
        Long maxLong = Long.MAX_VALUE; // 9223372036854775807 (19位)

        WriteCellData<Object> result = converter.convertToExcelData(maxLong, null, null);

        assertEquals(String.valueOf(Long.MAX_VALUE), result.getStringValue(), "Long最大值应转为字符串");
    }

    @Test
    @DisplayName("测试convertToExcelData-Long最小值")
    public void testConvertToExcelDataMinLong() {
        Long minLong = Long.MIN_VALUE; // -9223372036854775808 (20位含负号)

        WriteCellData<Object> result = converter.convertToExcelData(minLong, null, null);

        assertEquals(String.valueOf(Long.MIN_VALUE), result.getStringValue(), "Long最小值应转为字符串");
    }

    @Test
    @DisplayName("测试convertToExcelData-1")
    public void testConvertToExcelDataOne() {
        WriteCellData<Object> result = converter.convertToExcelData(1L, null, null);

        assertEquals(CellDataTypeEnum.NUMBER, result.getType(), "1应为NUMBER类型");
        assertEquals(BigDecimal.ONE, result.getNumberValue(), "1值应正确");
    }

    // ==================== 业务场景测试 ====================

    @Test
    @DisplayName("测试业务场景-用户ID(雪花算法)")
    public void testSnowflakeUserId() {
        // 雪花算法生成的ID通常是19位
        Long snowflakeId = 1234567890123456789L;

        WriteCellData<Object> result = converter.convertToExcelData(snowflakeId, null, null);

        assertEquals("1234567890123456789", result.getStringValue(), "雪花ID应转为字符串保留精度");
    }

    @Test
    @DisplayName("测试业务场景-订单号(数字型)")
    public void testNumericOrderNo() {
        Long orderNo = 202401011234567L; // 15位订单号

        WriteCellData<Object> result = converter.convertToExcelData(orderNo, null, null);

        assertEquals(CellDataTypeEnum.NUMBER, result.getType(), "15位订单号应为NUMBER类型");
    }

    @Test
    @DisplayName("测试业务场景-手机号")
    public void testPhoneNumber() {
        Long phone = 13812345678L; // 11位手机号

        WriteCellData<Object> result = converter.convertToExcelData(phone, null, null);

        assertEquals(CellDataTypeEnum.NUMBER, result.getType(), "11位手机号应为NUMBER类型");
    }

    @Test
    @DisplayName("测试业务场景-身份证号(数字部分)")
    public void testIdCardNumber() {
        // 身份证号18位（实际身份证最后一位可能是X，这里测试纯数字场景）
        Long idCard = 110101199001011234L;

        WriteCellData<Object> result = converter.convertToExcelData(idCard, null, null);

        assertEquals("110101199001011234", result.getStringValue(), "18位身份证号应转为字符串");
    }

    @Test
    @DisplayName("测试业务场景-银行卡号")
    public void testBankCardNumber() {
        // 银行卡号通常16-19位
        Long bankCard = 6222021234567890123L; // 19位

        WriteCellData<Object> result = converter.convertToExcelData(bankCard, null, null);

        assertEquals("6222021234567890123", result.getStringValue(), "19位银行卡号应转为字符串");
    }

    @Test
    @DisplayName("测试业务场景-金额(分)")
    public void testAmountInCents() {
        Long amountCents = 9999999999L; // 约100亿分

        WriteCellData<Object> result = converter.convertToExcelData(amountCents, null, null);

        assertEquals(CellDataTypeEnum.NUMBER, result.getType(), "金额应为NUMBER类型");
    }

    // ==================== 精度测试 ====================

    @Test
    @DisplayName("测试精度-Excel精度限制验证")
    public void testExcelPrecisionLimit() {
        // Excel数值精度限制为15位
        // 超过15位的数值会丢失精度

        Long within15 = 123456789012345L;
        Long over15 = 1234567890123456L;

        WriteCellData<Object> resultWithin = converter.convertToExcelData(within15, null, null);
        WriteCellData<Object> resultOver = converter.convertToExcelData(over15, null, null);

        assertEquals(CellDataTypeEnum.NUMBER, resultWithin.getType(), "15位以内应为数值类型");
        assertNotNull(resultOver.getStringValue(), "超过15位应为字符串类型");
    }

    @Test
    @DisplayName("测试精度-连续数值边界")
    public void testConsecutiveNumberBoundary() {
        // 测试从15位到16位的边界
        Long digits15 = 100000000000000L; // 15位 (1后面14个0)
        Long digits16 = 1000000000000000L; // 16位 (1后面15个0)

        WriteCellData<Object> result15 = converter.convertToExcelData(digits15, null, null);
        WriteCellData<Object> result16 = converter.convertToExcelData(digits16, null, null);

        assertEquals(CellDataTypeEnum.NUMBER, result15.getType(), "15位应为NUMBER");
        assertEquals("1000000000000000", result16.getStringValue(), "16位应为STRING");
    }

    // ==================== 类型转换一致性测试 ====================

    @Test
    @DisplayName("测试一致性-数值转换后可还原")
    public void testNumberConversionReversible() {
        Long original = 12345678901234L;

        WriteCellData<Object> cellData = converter.convertToExcelData(original, null, null);
        Long restored = cellData.getNumberValue().longValue();

        assertEquals(original, restored, "数值转换后应可还原");
    }

    @Test
    @DisplayName("测试一致性-字符串转换后可还原")
    public void testStringConversionReversible() {
        Long original = 1234567890123456789L;

        WriteCellData<Object> cellData = converter.convertToExcelData(original, null, null);
        Long restored = Long.parseLong(cellData.getStringValue());

        assertEquals(original, restored, "字符串转换后应可还原");
    }
}
