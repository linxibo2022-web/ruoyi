package plus.ruoyi.common.core.dict;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 字典枚举-订单状态
 *
 * @author 抓蛙师
 */
@Getter
@AllArgsConstructor
public enum DictOrderStatus {
    /**
     * 待支付
     */
    PENDING("pending", "待支付"),

    /**
     * 已支付
     */
    PAID("paid", "已支付"),

    /**
     * 已发货
     */
    DELIVERED("delivered", "已发货"),

    /**
     * 已完成
     */
    COMPLETED("completed", "已完成"),

    /**
     * 已取消
     */
    CANCELLED("cancelled", "已取消"),

    /**
     * 已退款
     */
    REFUNDED("refunded", "已退款");

    /**
     * 订单状态字典类型
     */
    public static final String DICT_TYPE = "order_status";

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
    public static DictOrderStatus getByValue(String value) {
        for (DictOrderStatus status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        // 默认返回待支付
        return PENDING;
    }

    /**
     * 根据字典标签获取枚举
     */
    public static DictOrderStatus getByLabel(String label) {
        for (DictOrderStatus status : values()) {
            if (status.getLabel().equals(label)) {
                return status;
            }
        }
        // 默认返回待支付
        return PENDING;
    }

    /**
     * 是否可以支付
     */
    public boolean canPay() {
        return this == PENDING;
    }

    /**
     * 是否可以取消
     */
    public boolean canCancel() {
        return this == PENDING;
    }

    /**
     * 是否可以发货
     */
    public boolean canDeliver() {
        return this == PAID;
    }

    /**
     * 是否可以完成
     */
    public boolean canComplete() {
        return this == DELIVERED;
    }

    /**
     * 是否可以退款
     */
    public boolean canRefund() {
        return this == PAID || this == DELIVERED;
    }

    /**
     * 是否为终态
     */
    public boolean isFinished() {
        return this == COMPLETED || this == CANCELLED || this == REFUNDED;
    }

    /**
     * 是否已支付
     */
    public static boolean isPaid(String value) {
        return PAID.getValue().equals(value);
    }

    /**
     * 是否待支付
     */
    public static boolean isPending(String value) {
        return PENDING.getValue().equals(value);
    }

    /**
     * 是否已完成
     */
    public static boolean isCompleted(String value) {
        return COMPLETED.getValue().equals(value);
    }

    /**
     * 是否已取消
     */
    public static boolean isCancelled(String value) {
        return CANCELLED.getValue().equals(value);
    }
}
