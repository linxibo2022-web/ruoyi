package plus.ruoyi.common.core.utils;

import plus.ruoyi.common.test.base.BaseUnitTest;
import plus.ruoyi.common.core.enums.DateTimeFormat;
import plus.ruoyi.common.core.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DateUtils 日期工具类测试
 *
 * @author 抓蛙师
 */
@DisplayName("日期工具类测试")
public class DateUtilsTest extends BaseUnitTest {

    @Test
    @DisplayName("测试getNowDate-获取当前日期时间")
    public void testGetNowDate() {
        Date now = DateUtils.getNowDate();

        assertNotNull(now);
        assertTrue(now.getTime() <= System.currentTimeMillis());
    }

    @Test
    @DisplayName("测试getDate-获取当前日期字符串")
    public void testGetDate() {
        String date = DateUtils.getDate();

        assertNotNull(date);
        assertTrue(date.matches("\\d{4}-\\d{2}-\\d{2}"), "日期格式应该是yyyy-MM-dd");
    }

    @Test
    @DisplayName("测试getCurrentDate-获取紧凑格式日期")
    public void testGetCurrentDate() {
        String date = DateUtils.getCurrentDate();

        assertNotNull(date);
        assertTrue(date.matches("\\d{8}"), "日期格式应该是yyyyMMdd");
    }

    @Test
    @DisplayName("测试datePath-获取路径格式日期")
    public void testDatePath() {
        String datePath = DateUtils.datePath();

        assertNotNull(datePath);
        assertTrue(datePath.matches("\\d{4}/\\d{2}/\\d{2}"), "日期格式应该是yyyy/MM/dd");
    }

    @Test
    @DisplayName("测试getTime-获取当前时间字符串")
    public void testGetTime() {
        String time = DateUtils.getTime();

        assertNotNull(time);
        assertTrue(time.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"),
            "时间格式应该是yyyy-MM-dd HH:mm:ss");
    }

    @Test
    @DisplayName("测试formatDate-格式化日期")
    public void testFormatDate() {
        Date date = new Date();
        String formatted = DateUtils.formatDate(date);

        assertNotNull(formatted);
        assertTrue(formatted.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    @Test
    @DisplayName("测试formatDateTime-格式化日期时间")
    public void testFormatDateTime() {
        Date date = new Date();
        String formatted = DateUtils.formatDateTime(date);

        assertNotNull(formatted);
        assertTrue(formatted.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    @DisplayName("测试parseDate-解析日期字符串")
    public void testParseDate() {
        // 测试多种日期格式
        Date date1 = DateUtils.parseDate("2024-01-15");
        Date date2 = DateUtils.parseDate("2024-01-15 10:30:00");
        Date date3 = DateUtils.parseDate("2024/01/15");
        Date date4 = DateUtils.parseDate(null);

        assertNotNull(date1);
        assertNotNull(date2);
        assertNotNull(date3);
        assertNull(date4, "null应该返回null");
    }

    @Test
    @DisplayName("测试parseDateTime-按指定格式解析日期")
    public void testParseDateTime() {
        String dateStr = "2024-01-15 10:30:00";
        Date date = DateUtils.parseDateTime(DateTimeFormat.DATETIME, dateStr);

        assertNotNull(date);
        String formatted = DateUtils.formatDateTime(date);
        assertEquals(dateStr, formatted);
    }

    @Test
    @DisplayName("测试getServerStartDate-获取服务器启动时间")
    public void testGetServerStartDate() {
        Date startDate = DateUtils.getServerStartDate();

        assertNotNull(startDate);
        assertTrue(startDate.getTime() <= System.currentTimeMillis());
    }

    @Test
    @DisplayName("测试difference-计算时间差")
    public void testDifference() {
        Date start = new Date();
        Date end = new Date(start.getTime() + TimeUnit.HOURS.toMillis(2) + TimeUnit.MINUTES.toMillis(30));

        long hours = DateUtils.difference(start, end, TimeUnit.HOURS);
        long minutes = DateUtils.difference(start, end, TimeUnit.MINUTES);

        assertEquals(2, hours);
        assertEquals(150, minutes);
    }

    @Test
    @DisplayName("测试getDatePoor-获取时间差描述")
    public void testGetDatePoor() {
        Date start = new Date();
        Date end = new Date(start.getTime() + TimeUnit.DAYS.toMillis(2) + TimeUnit.HOURS.toMillis(3) + TimeUnit.MINUTES.toMillis(15));

        String diff = DateUtils.getDatePoor(end, start);

        assertNotNull(diff);
        assertTrue(diff.contains("2天"));
        assertTrue(diff.contains("3小时"));
        assertTrue(diff.contains("15分钟"));
    }

    @Test
    @DisplayName("测试getTimeDifference-获取时间差")
    public void testGetTimeDifference() {
        Date start = new Date();
        Date end = new Date(start.getTime() + TimeUnit.HOURS.toMillis(1) + TimeUnit.MINUTES.toMillis(30) + TimeUnit.SECONDS.toMillis(45));

        String diff = DateUtils.getTimeDifference(end, start);

        assertNotNull(diff);
        assertTrue(diff.contains("1小时"));
        assertTrue(diff.contains("30分钟"));
        assertTrue(diff.contains("45秒"));
    }

    @Test
    @DisplayName("测试toDate-LocalDateTime转Date")
    public void testToDateFromLocalDateTime() {
        LocalDateTime localDateTime = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        Date date = DateUtils.toDate(localDateTime);

        assertNotNull(date);
    }

    @Test
    @DisplayName("测试toDate-LocalDate转Date")
    public void testToDateFromLocalDate() {
        LocalDate localDate = LocalDate.of(2024, 1, 15);
        Date date = DateUtils.toDate(localDate);

        assertNotNull(date);
    }

    @Test
    @DisplayName("测试validateDateRange-校验日期范围正常")
    public void testValidateDateRangeSuccess() {
        Date start = new Date();
        Date end = new Date(start.getTime() + TimeUnit.DAYS.toMillis(5));

        // 最大7天,应该通过
        assertDoesNotThrow(() ->
            DateUtils.validateDateRange(start, end, 7, TimeUnit.DAYS)
        );
    }

    @Test
    @DisplayName("测试validateDateRange-结束日期早于开始日期")
    public void testValidateDateRangeEndBeforeStart() {
        Date start = new Date();
        Date end = new Date(start.getTime() - TimeUnit.DAYS.toMillis(1));

        assertThrows(ServiceException.class, () ->
            DateUtils.validateDateRange(start, end, 7, TimeUnit.DAYS)
        );
    }

    @Test
    @DisplayName("测试validateDateRange-超过最大时间跨度")
    public void testValidateDateRangeExceedsMax() {
        Date start = new Date();
        Date end = new Date(start.getTime() + TimeUnit.DAYS.toMillis(10));

        // 最大7天,应该抛异常
        assertThrows(ServiceException.class, () ->
            DateUtils.validateDateRange(start, end, 7, TimeUnit.DAYS)
        );
    }

    /**
     * 自定义性能阈值
     */
    @Override
    protected long getPerformanceThreshold() {
        return 1000L; // 1秒
    }
}
