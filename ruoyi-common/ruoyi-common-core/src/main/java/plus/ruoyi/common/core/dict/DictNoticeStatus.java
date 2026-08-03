package plus.ruoyi.common.core.dict;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 字典枚举-通知发送状态
 * <p>
 * 用于表示通知的发送状态，包括立即发送和保存为草稿两种状态。
 *
 * @author 抓蛙师
 */
@Getter
@AllArgsConstructor
public enum DictNoticeStatus {
    /**
     * 立即发送
     */
    SEND_IMMEDIATELY("1", "立即发送"),

    /**
     * 保存为草稿
     */
    SAVE_AS_DRAFT("0", "保存为草稿");

    /**
     * 字典值
     */
    private final String value;

    /**
     * 字典标签
     */
    private final String label;

    /**
     * 通知状态字典类型
     */
    public static final String DICT_TYPE = "sys_notice_status";

    /**
     * 根据字典值获取枚举
     *
     * @param value 字典值
     * @return 对应的枚举实例，如果未找到则返回草稿状态
     */
    public static DictNoticeStatus getByValue(String value) {
        for (DictNoticeStatus status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        // 默认返回草稿状态
        return SAVE_AS_DRAFT;
    }

    /**
     * 根据字典标签获取枚举
     *
     * @param label 字典标签
     * @return 对应的枚举实例，如果未找到则返回草稿状态
     */
    public static DictNoticeStatus getByLabel(String label) {
        for (DictNoticeStatus status : values()) {
            if (status.getLabel().equals(label)) {
                return status;
            }
        }
        // 默认返回草稿状态
        return SAVE_AS_DRAFT;
    }

    /**
     * 判断指定值是否为立即发送状态
     *
     * @param value 字典值
     * @return true-立即发送状态，false-其他状态
     */
    public static boolean isSendImmediately(String value) {
        return SEND_IMMEDIATELY.getValue().equals(value);
    }

    /**
     * 判断指定值是否为草稿状态
     *
     * @param value 字典值
     * @return true-草稿状态，false-其他状态
     */
    public static boolean isDraft(String value) {
        return SAVE_AS_DRAFT.getValue().equals(value);
    }
}
