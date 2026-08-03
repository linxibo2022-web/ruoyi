package plus.ruoyi.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import plus.ruoyi.common.core.utils.StringUtils;

/**
 * 日期时间格式枚举
 * <p>
 * 日期格式说明：
 * <ul>
 * <li>"yyyy"：4位数的年份，例如：2023年表示为"2023"</li>
 * <li>"yy"：2位数的年份，例如：2023年表示为"23"</li>
 * <li>"MM"：2位数的月份，取值范围为01到12，例如：7月表示为"07"</li>
 * <li>"M"：不带前导零的月份，取值范围为1到12，例如：7月表示为"7"</li>
 * <li>"dd"：2位数的日期，取值范围为01到31，例如：22日表示为"22"</li>
 * <li>"d"：不带前导零的日期，取值范围为1到31，例如：22日表示为"22"</li>
 * <li>"EEEE"：星期的全名，例如：星期三表示为"Wednesday"</li>
 * <li>"E"：星期的缩写，例如：星期三表示为"Wed"</li>
 * <li>"DDD" 或 "D"：一年中的第几天，取值范围为001到366，例如：第200天表示为"200"</li>
 * </ul>
 * 时间格式说明：
 * <ul>
 * <li>"HH"：24小时制的小时数，取值范围为00到23，例如：下午5点表示为"17"</li>
 * <li>"hh"：12小时制的小时数，取值范围为01到12，例如：下午5点表示为"05"</li>
 * <li>"mm"：分钟数，取值范围为00到59，例如：30分钟表示为"30"</li>
 * <li>"ss"：秒数，取值范围为00到59，例如：45秒表示为"45"</li>
 * <li>"SSS"：毫秒数，取值范围为000到999，例如：123毫秒表示为"123"</li>
 * </ul>
 *
 * @author 抓蛙师
 */
@Getter
@AllArgsConstructor
public enum DateTimeFormat {

    /**
     * 2位数年份格式
     * 例如：2023年表示为"23"
     */
    YEAR_2("yy"),

    /**
     * 4位数年份格式
     * 例如：2023年表示为"2023"
     */
    YEAR_4("yyyy"),

    /**
     * 年月格式（横线分隔）
     * 例如：2023年7月表示为"2023-07"
     */
    YEAR_MONTH("yyyy-MM"),

    /**
     * 标准日期格式（横线分隔）
     * 例如：2023年7月22日表示为"2023-07-22"
     */
    DATE("yyyy-MM-dd"),

    /**
     * 日期时间格式（精确到分钟）
     * 例如：2023年7月22日下午3点30分表示为"2023-07-22 15:30"
     */
    DATETIME_MINUTE("yyyy-MM-dd HH:mm"),

    /**
     * 标准日期时间格式（精确到秒）
     * 例如：2023年7月22日下午3点30分45秒表示为"2023-07-22 15:30:45"
     */
    DATETIME("yyyy-MM-dd HH:mm:ss"),

    /**
     * 时间格式
     * 例如：下午3点30分45秒表示为"15:30:45"
     */
    TIME("HH:mm:ss"),

    /**
     * 年月格式（斜杠分隔）
     * 例如：2023年7月表示为"2023/07"
     */
    YEAR_MONTH_SLASH("yyyy/MM"),

    /**
     * 日期格式（斜杠分隔）
     * 例如：2023年7月22日表示为"2023/07/22"
     */
    DATE_SLASH("yyyy/MM/dd"),

    /**
     * 日期时间格式（斜杠分隔，精确到分钟）
     * 例如：2023年7月22日下午3点30分表示为"2023/07/22 15:30"
     */
    DATETIME_MINUTE_SLASH("yyyy/MM/dd HH:mm"),

    /**
     * 日期时间格式（斜杠分隔，精确到秒）
     * 例如：2023年7月22日下午3点30分45秒表示为"2023/07/22 15:30:45"
     */
    DATETIME_SLASH("yyyy/MM/dd HH:mm:ss"),

    /**
     * 年月格式（点分隔）
     * 例如：2023年7月表示为"2023.07"
     */
    YEAR_MONTH_DOT("yyyy.MM"),

    /**
     * 日期格式（点分隔）
     * 例如：2023年7月22日表示为"2023.07.22"
     */
    DATE_DOT("yyyy.MM.dd"),

    /**
     * 日期时间格式（点分隔，精确到分钟）
     * 例如：2023年7月22日下午3点30分表示为"2023.07.22 15:30"
     */
    DATETIME_MINUTE_DOT("yyyy.MM.dd HH:mm"),

    /**
     * 日期时间格式（点分隔，精确到秒）
     * 例如：2023年7月22日下午3点30分45秒表示为"2023.07.22 15:30:45"
     */
    DATETIME_DOT("yyyy.MM.dd HH:mm:ss"),

    /**
     * 紧凑年月格式
     * 例如：2023年7月表示为"202307"
     */
    YEAR_MONTH_COMPACT("yyyyMM"),

    /**
     * 紧凑日期格式
     * 例如：2023年7月22日表示为"20230722"
     */
    DATE_COMPACT("yyyyMMdd"),

    /**
     * 紧凑日期时间格式（精确到小时）
     * 例如：2023年7月22日下午3点表示为"2023072215"
     */
    DATETIME_HOUR_COMPACT("yyyyMMddHH"),

    /**
     * 紧凑日期时间格式（精确到分钟）
     * 例如：2023年7月22日下午3点30分表示为"202307221530"
     */
    DATETIME_MINUTE_COMPACT("yyyyMMddHHmm"),

    /**
     * 紧凑日期时间格式（精确到秒）
     * 例如：2023年7月22日下午3点30分45秒表示为"20230722153045"
     */
    DATETIME_COMPACT("yyyyMMddHHmmss");

    /**
     * 日期时间格式模式字符串
     */
    private final String pattern;

    /**
     * 根据模式字符串查找匹配的日期时间格式
     *
     * @param str 包含日期时间模式的字符串
     * @return 匹配的日期时间格式枚举
     * @throws IllegalArgumentException 如果找不到匹配的格式
     */
    public static DateTimeFormat findByPattern(String str) {
        for (DateTimeFormat format : values()) {
            if (StringUtils.contains(str, format.getPattern())) {
                return format;
            }
        }
        throw new IllegalArgumentException("No DateTimeFormat found for pattern: " + str);
    }
}
