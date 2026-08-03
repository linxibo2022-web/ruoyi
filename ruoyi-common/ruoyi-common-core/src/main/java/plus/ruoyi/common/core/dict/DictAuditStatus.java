package plus.ruoyi.common.core.dict;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 字典枚举-审核状态
 *
 * @author 抓蛙师
 */
@Getter
@AllArgsConstructor
public enum DictAuditStatus {
    /**
     * 待审核
     */
    PENDING("0", "待审核"),

    /**
     * 通过
     */
    APPROVED("1", "通过"),

    /**
     * 驳回
     */
    REJECTED("2", "驳回"),

    /**
     * 拒绝
     */
    DENIED("3", "拒绝");

    /**
     * 审核状态字典类型
     */
    public static final String DICT_TYPE = "sys_audit_status";

    /**
     * 字典值
     */
    private final String value;

    /**
     * 字典标签
     */
    private final String label;

    /**
     * 根据字典值获取枚举
     */
    public static DictAuditStatus getByValue(String value) {
        for (DictAuditStatus status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        // 默认返回待审核
        return PENDING;
    }

    /**
     * 根据字典标签获取枚举
     */
    public static DictAuditStatus getByLabel(String label) {
        for (DictAuditStatus status : values()) {
            if (status.getLabel().equals(label)) {
                return status;
            }
        }
        // 默认返回待审核
        return PENDING;
    }

    /**
     * 是否已审核通过
     */
    public static boolean isApproved(String value) {
        return APPROVED.getValue().equals(value);
    }

    /**
     * 是否待审核
     */
    public static boolean isPending(String value) {
        return PENDING.getValue().equals(value);
    }
}
