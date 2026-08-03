package plus.ruoyi.common.core.dict;

import lombok.AllArgsConstructor;
import lombok.Getter;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.utils.StringUtils;

/**
 * 字典枚举-支付方式
 *
 * @author 抓蛙师
 */
@Getter
@AllArgsConstructor
public enum DictPaymentMethod {
    /**
     * 微信支付
     */
    WECHAT("wechat", "微信支付"),

    /**
     * 支付宝
     */
    ALIPAY("alipay", "支付宝"),

    /**
     * 银联
     */
    UNIONPAY("unionpay", "银联"),

    /**
     * 余额支付
     */
    BALANCE("balance", "余额支付"),

    /**
     * 积分抵扣
     */
    POINTS("points", "积分抵扣");

    /**
     * 支付方式字典类型
     */
    public static final String DICT_TYPE = "sys_payment_method";

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
    public static DictPaymentMethod getByValue(String value) {
        for (DictPaymentMethod method : values()) {
            if (method.getValue().equals(value)) {
                return method;
            }
        }
        throw new ServiceException(StringUtils.format("不支持的支付类型:{}", value));
    }

    /**
     * 根据字典标签获取枚举
     */
    public static DictPaymentMethod getByLabel(String label) {
        for (DictPaymentMethod method : values()) {
            if (method.getLabel().equals(label)) {
                return method;
            }
        }
        throw new ServiceException(StringUtils.format("不支持的支付类型:{}", label));
    }

    /**
     * 是否为在线支付方式
     */
    public static boolean isOnlinePayment(String value) {
        return WECHAT.getValue().equals(value) ||
            ALIPAY.getValue().equals(value) ||
            UNIONPAY.getValue().equals(value);
    }
}
