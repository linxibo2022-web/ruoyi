package plus.ruoyi.common.core.utils.regex;

import cn.hutool.core.lang.PatternPool;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

/**
 * 正则表达式模式池
 * <p>预编译常用正则表达式模式，提高运行时匹配性能</p>
 * <p>基于 Hutool 的 PatternPool 实现模式缓存和复用</p>
 *
 * @author 21001
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class RegexPatternPool extends PatternPool {

    /**
     * 国际化键格式的预编译模式
     */
    public static final Pattern I18N_KEY = get(RegexPatterns.I18N_KEY);

    /**
     * 字典类型的预编译模式
     */
    public static final Pattern DICT_TYPE = get(RegexPatterns.DICT_TYPE);

    /**
     * 权限标识的预编译模式
     */
    public static final Pattern PERMISSION = get(RegexPatterns.PERMISSION);

    /**
     * 身份证号码后6位的预编译模式
     */
    public static final Pattern ID_CARD_SUFFIX = get(RegexPatterns.ID_CARD_SUFFIX);

    /**
     * QQ号码的预编译模式
     */
    public static final Pattern QQ = get(RegexPatterns.QQ);

    /**
     * 邮政编码的预编译模式
     */
    public static final Pattern POSTAL_CODE = get(RegexPatterns.POSTAL_CODE);

    /**
     * 用户账号的预编译模式
     */
    public static final Pattern ACCOUNT = get(RegexPatterns.ACCOUNT);

    /**
     * 强密码的预编译模式
     */
    public static final Pattern STRONG_PASSWORD = get(RegexPatterns.STRONG_PASSWORD);

    /**
     * 通用状态的预编译模式
     */
    public static final Pattern BINARY_STATUS = get(RegexPatterns.BINARY_STATUS);

}
