package plus.ruoyi.common.rocketmq.enums;

import lombok.Getter;

/**
 * RocketMQ 延迟消息级别枚举
 * <p>
 * RocketMQ 支持 18 个固定的延迟级别，不支持自定义延迟时间
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * RMProducerUtil.sendDelay("topic", message, DelayLevel.TEN_SECONDS);
 * </pre>
 * </p>
 *
 * @author 路北
 * @date 2025-11-03
 */
@Getter
public enum DelayLevel {

    /**
     * 延迟 1 秒
     */
    ONE_SECOND(1, "1s", "1秒"),

    /**
     * 延迟 5 秒
     */
    FIVE_SECONDS(2, "5s", "5秒"),

    /**
     * 延迟 10 秒
     */
    TEN_SECONDS(3, "10s", "10秒"),

    /**
     * 延迟 30 秒
     */
    THIRTY_SECONDS(4, "30s", "30秒"),

    /**
     * 延迟 1 分钟
     */
    ONE_MINUTE(5, "1m", "1分钟"),

    /**
     * 延迟 2 分钟
     */
    TWO_MINUTES(6, "2m", "2分钟"),

    /**
     * 延迟 3 分钟
     */
    THREE_MINUTES(7, "3m", "3分钟"),

    /**
     * 延迟 4 分钟
     */
    FOUR_MINUTES(8, "4m", "4分钟"),

    /**
     * 延迟 5 分钟
     */
    FIVE_MINUTES(9, "5m", "5分钟"),

    /**
     * 延迟 6 分钟
     */
    SIX_MINUTES(10, "6m", "6分钟"),

    /**
     * 延迟 7 分钟
     */
    SEVEN_MINUTES(11, "7m", "7分钟"),

    /**
     * 延迟 8 分钟
     */
    EIGHT_MINUTES(12, "8m", "8分钟"),

    /**
     * 延迟 9 分钟
     */
    NINE_MINUTES(13, "9m", "9分钟"),

    /**
     * 延迟 10 分钟
     */
    TEN_MINUTES(14, "10m", "10分钟"),

    /**
     * 延迟 20 分钟
     */
    TWENTY_MINUTES(15, "20m", "20分钟"),

    /**
     * 延迟 30 分钟
     */
    THIRTY_MINUTES(16, "30m", "30分钟"),

    /**
     * 延迟 1 小时
     */
    ONE_HOUR(17, "1h", "1小时"),

    /**
     * 延迟 2 小时
     */
    TWO_HOURS(18, "2h", "2小时");

    /**
     * 延迟级别（1-18）
     */
    private final int level;

    /**
     * 简短描述（英文）
     */
    private final String code;

    /**
     * 中文描述
     */
    private final String description;

    DelayLevel(int level, String code, String description) {
        this.level = level;
        this.code = code;
        this.description = description;
    }

    /**
     * 根据级别获取枚举
     *
     * @param level 延迟级别（1-18）
     * @return DelayLevel 枚举
     */
    public static DelayLevel fromLevel(int level) {
        for (DelayLevel delayLevel : values()) {
            if (delayLevel.level == level) {
                return delayLevel;
            }
        }
        throw new IllegalArgumentException("不支持的延迟级别: " + level + "，有效范围: 1-18");
    }

    /**
     * 根据 code 获取枚举
     *
     * @param code 延迟代码（如 "1s", "5m", "1h"）
     * @return DelayLevel 枚举
     */
    public static DelayLevel fromCode(String code) {
        for (DelayLevel delayLevel : values()) {
            if (delayLevel.code.equalsIgnoreCase(code)) {
                return delayLevel;
            }
        }
        throw new IllegalArgumentException("不支持的延迟代码: " + code);
    }

    @Override
    public String toString() {
        return description + " (level=" + level + ")";
    }
}
