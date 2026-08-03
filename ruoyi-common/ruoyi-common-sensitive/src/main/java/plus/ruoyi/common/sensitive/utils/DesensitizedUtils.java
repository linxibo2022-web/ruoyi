package plus.ruoyi.common.sensitive.utils;

import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 脱敏工具类
 *
 * <p>继承自Hutool的DesensitizedUtil，扩展了灵活配置的脱敏方法。
 *
 * <p>使用示例：
 * <pre>{@code
 * // 灵活脱敏：前4位可见，后4位可见，中间4个*
 * String result = DesensitizedUtils.mask("1234567890", 4, 4, 4);
 * // 结果: "1234**7890"
 *
 * // 短字符串会自动缩减掩码
 * String result2 = DesensitizedUtils.mask("12345", 2, 2, 4);
 * // 结果: "12*45"
 * }</pre>
 *
 * @author AprilWind
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DesensitizedUtils extends DesensitizedUtil {

    /**
     * 灵活脱敏方法
     *
     * <p>支持自定义前后可见长度和中间掩码长度，适用于各种长度的字符串脱敏需求。
     *
     * @param value         原始字符串
     * @param prefixVisible 前面可见长度
     * @param suffixVisible 后面可见长度
     * @param maskLength    中间掩码长度（固定显示多少 *，如果总长度不足则自动缩减）
     * @return 脱敏后字符串
     */
    public static String mask(String value, int prefixVisible, int suffixVisible, int maskLength) {
        if (StrUtil.isBlank(value)) {
            return value;
        }

        int len = value.length();

        // 总长度小于等于前后可见长度 → 全部掩码
        if (len <= prefixVisible + suffixVisible) {
            return StrUtil.repeat('*', len);
        }

        // 可用长度 = 总长度 - 前后可见长度
        int available = len - prefixVisible - suffixVisible;

        // 中间掩码长度不能超过可用长度
        int actualMaskLength = Math.min(maskLength, available);

        // 剩余字符尽量显示在中间掩码旁
        int remaining = available - actualMaskLength;
        String middleChars = remaining > 0 ? value.substring(prefixVisible, prefixVisible + remaining) : "";
        String middleMask = StrUtil.repeat('*', actualMaskLength);

        String prefix = value.substring(0, prefixVisible);
        String suffix = value.substring(len - suffixVisible);

        return prefix + middleChars + middleMask + suffix;
    }

}
