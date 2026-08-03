package plus.ruoyi.common.redis.utils;

import cn.hutool.core.date.DatePattern;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import org.redisson.api.RIdGenerator;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

/**
 * 分布式ID发号器工具类
 *
 * <p>基于Redisson的RIdGenerator实现分布式唯一ID生成，支持：
 * <ul>
 *   <li>自定义初始值和步长的ID生成</li>
 *   <li>带时间前缀的ID生成(日期/日期时间)</li>
 *   <li>补零填充的ID生成</li>
 *   <li>ID过期时间控制</li>
 *   <li>可选的业务前缀控制</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>
 * // 基础ID生成
 * long id = SequenceUtils.getNextId("order", Duration.ofDays(1));
 *
 * // 带日期前缀的订单号(含业务前缀)
 * String orderId = SequenceUtils.getDateId("ORD"); // ORD20241210000001
 *
 * // 带时间前缀的流水号(不含业务前缀)
 * String serialNo = SequenceUtils.getDateTimeId("SN", false); // 20241210103000001
 *
 * // 补零的序列号
 * String seqNo = SequenceUtils.getPaddedNextIdString("seq", Duration.ofHours(1), 6); // 000001
 *
 * // 指定时间的ID生成
 * String historyId = SequenceUtils.getDateId("HIST", true, 8, LocalDate.of(2024, 1, 1)); // HIST2024010100000001
 * </pre>
 *
 * @author 秋辞未寒
 * @date 2024-12-10
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SequenceUtils {

    /**
     * 默认初始值
     */
    public static final long DEFAULT_INIT_VALUE = 1L;

    /**
     * 默认步长
     */
    public static final long DEFAULT_STEP_VALUE = 1L;

    /**
     * 默认过期时间-天(用于日期前缀ID)
     */
    public static final Duration DEFAULT_EXPIRE_TIME_DAY = Duration.ofDays(1);

    /**
     * 默认过期时间-分钟(用于时间前缀ID)
     */
    public static final Duration DEFAULT_EXPIRE_TIME_MINUTE = Duration.ofMinutes(1);

    /**
     * 默认最小ID容量位数 - 6位数（即至少可以生成的ID为999999个）
     */
    public static final int DEFAULT_MIN_ID_CAPACITY_BITS = 6;

    /**
     * Redisson客户端实例
     */
    private static final RedissonClient REDISSON_CLIENT = SpringUtils.getBean(RedissonClient.class);

    /**
     * 获取ID生成器实例
     *
     * <p>创建或获取一个Redisson的ID生成器，并设置初始值、步长和过期时间
     *
     * @param key        业务标识key，用于区分不同业务的ID序列
     * @param expireTime 过期时间，超过此时间后ID计数器会重置
     * @param initValue  ID初始值，小于等于0时使用默认值1
     * @param stepValue  ID步长，小于等于0时使用默认值1
     * @return ID生成器实例
     */
    public static RIdGenerator getIdGenerator(String key, Duration expireTime, long initValue, long stepValue) {
        RIdGenerator idGenerator = REDISSON_CLIENT.getIdGenerator(key);
        // 初始值和步长不能小于等于0
        initValue = initValue <= 0 ? DEFAULT_INIT_VALUE : initValue;
        stepValue = stepValue <= 0 ? DEFAULT_STEP_VALUE : stepValue;
        // 设置初始值和步长(仅在首次创建时生效)
        idGenerator.tryInit(initValue, stepValue);
        // 设置过期时间
        idGenerator.expire(expireTime);
        return idGenerator;
    }

    /**
     * 获取ID生成器实例(使用默认初始值1和步长1)
     *
     * @param key        业务标识key
     * @param expireTime 过期时间
     * @return ID生成器实例
     */
    public static RIdGenerator getIdGenerator(String key, Duration expireTime) {
        return getIdGenerator(key, expireTime, DEFAULT_INIT_VALUE, DEFAULT_STEP_VALUE);
    }

    /**
     * 生成指定业务key的唯一ID
     *
     * @param key        业务标识key
     * @param expireTime 过期时间
     * @param initValue  ID初始值
     * @param stepValue  ID步长
     * @return 唯一ID数值
     */
    public static long getNextId(String key, Duration expireTime, long initValue, long stepValue) {
        return getIdGenerator(key, expireTime, initValue, stepValue).nextId();
    }

    /**
     * 生成指定业务key的唯一ID (使用默认初始值1和步长1)
     *
     * @param key        业务标识key
     * @param expireTime 过期时间
     * @return 唯一ID数值
     */
    public static long getNextId(String key, Duration expireTime) {
        return getIdGenerator(key, expireTime).nextId();
    }

    /**
     * 生成指定业务key的唯一ID字符串
     *
     * @param key        业务标识key
     * @param expireTime 过期时间
     * @param initValue  ID初始值
     * @param stepValue  ID步长
     * @return 唯一ID字符串
     */
    public static String getNextIdString(String key, Duration expireTime, long initValue, long stepValue) {
        return String.valueOf(getNextId(key, expireTime, initValue, stepValue));
    }

    /**
     * 生成指定业务key的唯一ID字符串 (使用默认初始值1和步长1)
     *
     * @param key        业务标识key
     * @param expireTime 过期时间
     * @return 唯一ID字符串
     */
    public static String getNextIdString(String key, Duration expireTime) {
        return String.valueOf(getNextId(key, expireTime));
    }

    /**
     * 生成补零的唯一ID字符串 (使用默认初始值1和步长1)
     *
     * <p>生成指定位数的ID字符串，不足位数时左侧补0
     *
     * @param key        业务标识key
     * @param expireTime 过期时间
     * @param width      总位数，不足时左侧补0
     * @return 补零后的唯一ID字符串，如width=6时返回"000001"
     */
    public static String getPaddedNextIdString(String key, Duration expireTime, Integer width) {
        return StringUtils.leftPad(getNextIdString(key, expireTime), width, '0');
    }

    /**
     * 生成yyyyMMdd格式的唯一ID
     *
     * <p>格式：yyyyMMdd + 序号，如：20241210000001
     *
     * @return 带当前日期前缀的唯一ID
     * @deprecated 请使用 {@link #getDateId(String)} 或 {@link #getDateId(String, boolean)}、{@link #getDateId(String, boolean, int)}，确保不同业务的ID连续性
     */
    @Deprecated
    public static String getDateId() {
        return getDateId("");
    }

    /**
     * 生成带业务前缀的yyyyMMdd格式唯一ID
     *
     * <p>格式：prefix + yyyyMMdd + 序号，如：ORD20241210000001
     *
     * @param prefix 业务前缀，可以为空
     * @return 带前缀和当前日期的唯一ID
     */
    public static String getDateId(String prefix) {
        return getDateId(prefix, true);
    }

    /**
     * 生成yyyyMMdd格式的唯一ID，可选择是否包含业务前缀
     *
     * <p>格式示例：
     * <ul>
     *   <li>isWithPrefix=true: ORD20241210000001</li>
     *   <li>isWithPrefix=false: 20241210000001</li>
     * </ul>
     *
     * @param prefix       业务前缀
     * @param isWithPrefix 是否在最终ID中包含业务前缀
     * @return 唯一ID字符串
     */
    public static String getDateId(String prefix, boolean isWithPrefix) {
        return getDateId(prefix, isWithPrefix, -1);
    }

    /**
     * 生成带补零的yyyyMMdd格式唯一ID
     *
     * <p>启用ID补位，补位长度使用默认值 {@link #DEFAULT_MIN_ID_CAPACITY_BITS}
     *
     * @param prefix       业务前缀
     * @param isWithPrefix 是否在最终ID中包含业务前缀
     * @return 补零后的唯一ID，如：ORD20241210000001（序号部分至少6位）
     */
    public static String getPaddedDateId(String prefix, boolean isWithPrefix) {
        return getDateId(prefix, isWithPrefix, DEFAULT_MIN_ID_CAPACITY_BITS);
    }

    /**
     * 生成yyyyMMdd格式的唯一ID（完整参数版本）
     *
     * @param prefix            业务前缀
     * @param isWithPrefix      是否在最终ID中包含业务前缀
     * @param minIdCapacityBits 最小ID容量位数，小于该位数的ID序号部分会左补0（小于等于0表示不启用补位）
     * @return 唯一ID字符串
     */
    public static String getDateId(String prefix, boolean isWithPrefix, int minIdCapacityBits) {
        return getDateId(prefix, isWithPrefix, minIdCapacityBits, LocalDate.now());
    }

    /**
     * 生成指定日期的yyyyMMdd格式唯一ID
     *
     * @param prefix            业务前缀
     * @param isWithPrefix      是否在最终ID中包含业务前缀
     * @param minIdCapacityBits 最小ID容量位数
     * @param time              指定的日期
     * @return 唯一ID字符串
     */
    public static String getDateId(String prefix, boolean isWithPrefix, int minIdCapacityBits, LocalDate time) {
        return getDateId(prefix, isWithPrefix, minIdCapacityBits, time, DEFAULT_INIT_VALUE, DEFAULT_STEP_VALUE);
    }

    /**
     * 生成指定日期的yyyyMMdd格式唯一ID（全参数版本）
     *
     * @param prefix            业务前缀
     * @param isWithPrefix      是否在最终ID中包含业务前缀
     * @param minIdCapacityBits 最小ID容量位数
     * @param time              指定的日期
     * @param initValue         ID初始值
     * @param stepValue         ID步长
     * @return 唯一ID字符串
     */
    public static String getDateId(String prefix, boolean isWithPrefix, int minIdCapacityBits, LocalDate time, long initValue, long stepValue) {
        return getDatePatternId(prefix, isWithPrefix, minIdCapacityBits, time, DatePattern.PURE_DATE_FORMATTER, DEFAULT_EXPIRE_TIME_DAY, initValue, stepValue);
    }

    /**
     * 生成yyyyMMddHHmmss格式的唯一ID
     *
     * <p>格式：yyyyMMddHHmmss + 序号，如：20241210103000001
     *
     * @return 带当前日期时间前缀的唯一ID
     * @deprecated 请使用 {@link #getDateTimeId(String)} 或 {@link #getDateTimeId(String, boolean)}、{@link #getDateTimeId(String, boolean, int)}，确保不同业务的ID连续性
     */
    @Deprecated
    public static String getDateTimeId() {
        return getDateTimeId("", false);
    }

    /**
     * 生成带业务前缀的yyyyMMddHHmmss格式唯一ID
     *
     * <p>格式：prefix + yyyyMMddHHmmss + 序号，如：SN20241210103000001
     *
     * @param prefix 业务前缀
     * @return 带前缀和当前日期时间的唯一ID
     */
    public static String getDateTimeId(String prefix) {
        return getDateTimeId(prefix, true);
    }

    /**
     * 生成yyyyMMddHHmmss格式的唯一ID，可选择是否包含业务前缀
     *
     * <p>格式示例：
     * <ul>
     *   <li>isWithPrefix=true: SN20241210103000001</li>
     *   <li>isWithPrefix=false: 20241210103000001</li>
     * </ul>
     *
     * @param prefix       业务前缀
     * @param isWithPrefix 是否在最终ID中包含业务前缀
     * @return 唯一ID字符串
     */
    public static String getDateTimeId(String prefix, boolean isWithPrefix) {
        return getDateTimeId(prefix, isWithPrefix, -1);
    }

    /**
     * 生成带补零的yyyyMMddHHmmss格式唯一ID
     *
     * <p>启用ID补位，补位长度使用默认值 {@link #DEFAULT_MIN_ID_CAPACITY_BITS}
     *
     * @param prefix       业务前缀
     * @param isWithPrefix 是否在最终ID中包含业务前缀
     * @return 补零后的唯一ID，如：SN20241210103000001（序号部分至少6位）
     */
    public static String getPaddedDateTimeId(String prefix, boolean isWithPrefix) {
        return getDateTimeId(prefix, isWithPrefix, DEFAULT_MIN_ID_CAPACITY_BITS);
    }

    /**
     * 生成yyyyMMddHHmmss格式的唯一ID（完整参数版本）
     *
     * @param prefix            业务前缀
     * @param isWithPrefix      是否在最终ID中包含业务前缀
     * @param minIdCapacityBits 最小ID容量位数，小于该位数的ID序号部分会左补0（小于等于0表示不启用补位）
     * @return 唯一ID字符串
     */
    public static String getDateTimeId(String prefix, boolean isWithPrefix, int minIdCapacityBits) {
        return getDateTimeId(prefix, isWithPrefix, minIdCapacityBits, LocalDateTime.now());
    }

    /**
     * 生成指定时间的yyyyMMddHHmmss格式唯一ID
     *
     * @param prefix            业务前缀
     * @param isWithPrefix      是否在最终ID中包含业务前缀
     * @param minIdCapacityBits 最小ID容量位数
     * @param time              指定的日期时间
     * @return 唯一ID字符串
     */
    public static String getDateTimeId(String prefix, boolean isWithPrefix, int minIdCapacityBits, LocalDateTime time) {
        return getDateTimeId(prefix, isWithPrefix, minIdCapacityBits, time, DEFAULT_INIT_VALUE, DEFAULT_STEP_VALUE);
    }

    /**
     * 生成指定时间的yyyyMMddHHmmss格式唯一ID（全参数版本）
     *
     * @param prefix            业务前缀
     * @param isWithPrefix      是否在最终ID中包含业务前缀
     * @param minIdCapacityBits 最小ID容量位数
     * @param time              指定的日期时间
     * @param initValue         ID初始值
     * @param stepValue         ID步长
     * @return 唯一ID字符串
     */
    public static String getDateTimeId(String prefix, boolean isWithPrefix, int minIdCapacityBits, LocalDateTime time, long initValue, long stepValue) {
        return getDatePatternId(prefix, isWithPrefix, minIdCapacityBits, time, DatePattern.PURE_DATETIME_FORMATTER, DEFAULT_EXPIRE_TIME_MINUTE, initValue, stepValue);
    }

    /**
     * 获取指定业务key的指定时间格式的ID（核心实现方法）
     *
     * <p>这是所有时间格式ID生成方法的底层实现，负责：
     * <ul>
     *   <li>构建时间前缀</li>
     *   <li>生成递增序号</li>
     *   <li>处理补零逻辑</li>
     *   <li>组装最终ID</li>
     * </ul>
     *
     * @param prefix            业务前缀
     * @param isWithPrefix      是否在最终ID中包含业务前缀
     * @param minIdCapacityBits 最小ID容量位数，小于该位数的ID序号部分会左补0（小于等于0表示不启用补位）
     * @param temporalAccessor  时间访问器（LocalDate或LocalDateTime等）
     * @param timeFormatter     时间格式化器
     * @param expireTime        过期时间
     * @param initValue         ID初始值
     * @param stepValue         ID步长
     * @return 格式化的唯一ID字符串
     */
    private static String getDatePatternId(String prefix, boolean isWithPrefix, int minIdCapacityBits, TemporalAccessor temporalAccessor, DateTimeFormatter timeFormatter, Duration expireTime, long initValue, long stepValue) {
        // 格式化时间前缀，如：20241210 或 20241210103000
        String timePrefix = timeFormatter.format(temporalAccessor);
        // 构建完整的Redis key：业务前缀 + 时间前缀
        String prefixKey = StringUtils.format("{}{}", StringUtils.blankToDefault(prefix, ""), timePrefix);

        // 生成递增序号
        String nextId = getNextIdString(prefixKey, expireTime, initValue, stepValue);

        // 如果启用补零且序号位数不足，则左补0
        if (minIdCapacityBits > 0 && nextId.length() < minIdCapacityBits) {
            nextId = StringUtils.leftPad(nextId, minIdCapacityBits, '0');
        }

        // 根据isWithPrefix决定返回格式
        if (isWithPrefix) {
            // 返回：业务前缀 + 时间前缀 + 序号，如：ORD20241210000001
            return StringUtils.format("{}{}", prefixKey, nextId);
        }
        // 返回：时间前缀 + 序号，如：20241210000001
        return StringUtils.format("{}{}", timePrefix, nextId);
    }
}
