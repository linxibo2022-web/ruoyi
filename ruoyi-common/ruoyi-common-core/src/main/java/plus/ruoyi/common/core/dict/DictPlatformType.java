package plus.ruoyi.common.core.dict;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 字典枚举-平台类型
 *
 * @author 抓蛙师
 */
@Getter
@AllArgsConstructor
public enum DictPlatformType {
    /**
     * 微信小程序
     */
    MP_WEIXIN("mp-weixin", "微信小程序"),

    /**
     * 微信公众号
     */
    MP_OFFICIAL_ACCOUNT("mp-official-account", "微信公众号"),

    /**
     * QQ小程序
     */
    MP_QQ("mp-qq", "QQ小程序"),

    /**
     * 支付宝小程序
     */
    MP_ALIPAY("mp-alipay", "支付宝小程序"),

    /**
     * 京东小程序
     */
    MP_JD("mp-jd", "京东小程序"),

    /**
     * 快手小程序
     */
    MP_KUAISHOU("mp-kuaishou", "快手小程序"),

    /**
     * 飞书小程序
     */
    MP_LARK("mp-lark", "飞书小程序"),

    /**
     * 百度小程序
     */
    MP_BAIDU("mp-baidu", "百度小程序"),

    /**
     * 头条小程序
     */
    MP_TOUTIAO("mp-toutiao", "头条小程序"),

    /**
     * 小红书小程序
     */
    MP_XHS("mp-xhs", "小红书小程序");

    /**
     * 平台类型字典类型
     */
    public static final String DICT_TYPE = "sys_platform_type";

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
    public static DictPlatformType getByValue(String value) {
        for (DictPlatformType type : values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据字典标签获取枚举
     */
    public static DictPlatformType getByLabel(String label) {
        for (DictPlatformType type : values()) {
            if (type.getLabel().equals(label)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 是否为微信相关平台
     */
    public static boolean isWechatPlatform(String value) {
        return MP_WEIXIN.getValue().equals(value) ||
            MP_OFFICIAL_ACCOUNT.getValue().equals(value);
    }
}
