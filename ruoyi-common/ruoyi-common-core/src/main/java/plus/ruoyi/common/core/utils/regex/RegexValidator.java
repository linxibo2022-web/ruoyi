package plus.ruoyi.common.core.utils.regex;

import cn.hutool.core.exceptions.ValidateException;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

/**
 * 正则表达式字段验证器
 * <p>提供基于正则表达式的字段格式验证功能</p>
 *
 * @author Feng
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class RegexValidator extends Validator {

    // ==================== 国际化键验证 ====================

    /**
     * 验证国际化键格式是否正确
     * <p>格式要求：英文段.英文段.英文段...（至少2段）</p>
     * <p>英文段规则：字母开头，可包含字母、数字、下划线</p>
     *
     * @param i18nKey 待验证的国际化键
     * @return 是否符合国际化键格式要求
     */
    public static boolean isValidI18nKey(CharSequence i18nKey) {
        return isMatchPattern(RegexPatternPool.I18N_KEY, i18nKey);
    }

    /**
     * 验证国际化键格式，不符合则抛出异常
     *
     * @param i18nKey    待验证的国际化键
     * @param errorMsg   验证失败时的异常消息
     * @param <T>        CharSequence 的子类型
     * @return 验证通过的国际化键
     * @throws ValidateException 验证失败时抛出
     */
    public static <T extends CharSequence> T validateI18nKey(T i18nKey, String errorMsg) {
        if (!isValidI18nKey(i18nKey)) {
            throw new ValidateException(StrUtil.isNotBlank(errorMsg) ? errorMsg :
                "国际化键格式不正确，应为：段.段.段...格式，每段以字母开头");
        }
        return i18nKey;
    }

    // ==================== 账号相关验证 ====================

    /**
     * 验证用户账号格式是否正确
     *
     * @param account 待验证的账号
     * @return 是否符合账号格式要求
     */
    public static boolean isValidAccount(CharSequence account) {
        return isMatchPattern(RegexPatternPool.ACCOUNT, account);
    }

    /**
     * 验证用户账号格式，不符合则抛出异常
     *
     * @param account    待验证的账号
     * @param errorMsg   验证失败时的异常消息
     * @param <T>        CharSequence 的子类型
     * @return 验证通过的账号
     * @throws ValidateException 验证失败时抛出
     */
    public static <T extends CharSequence> T validateAccount(T account, String errorMsg) {
        if (!isValidAccount(account)) {
            throw new ValidateException(StrUtil.isNotBlank(errorMsg) ? errorMsg : "账号格式不正确");
        }
        return account;
    }

    // ==================== 状态相关验证 ====================

    /**
     * 验证状态值格式是否正确（0或1）
     *
     * @param status 待验证的状态值
     * @return 是否符合状态格式要求
     */
    public static boolean isValidStatus(CharSequence status) {
        return isMatchPattern(RegexPatternPool.BINARY_STATUS, status);
    }

    /**
     * 验证状态值格式，不符合则抛出异常
     *
     * @param status     待验证的状态值
     * @param errorMsg   验证失败时的异常消息
     * @param <T>        CharSequence 的子类型
     * @return 验证通过的状态值
     * @throws ValidateException 验证失败时抛出
     */
    public static <T extends CharSequence> T validateStatus(T status, String errorMsg) {
        if (!isValidStatus(status)) {
            throw new ValidateException(StrUtil.isNotBlank(errorMsg) ? errorMsg : "状态值格式不正确，只能为0或1");
        }
        return status;
    }

    // ==================== 字典类型验证 ====================

    /**
     * 验证字典类型格式是否正确
     *
     * @param dictType 待验证的字典类型
     * @return 是否符合字典类型格式要求
     */
    public static boolean isValidDictType(CharSequence dictType) {
        return isMatchPattern(RegexPatternPool.DICT_TYPE, dictType);
    }

    /**
     * 验证字典类型格式，不符合则抛出异常
     *
     * @param dictType   待验证的字典类型
     * @param errorMsg   验证失败时的异常消息
     * @param <T>        CharSequence 的子类型
     * @return 验证通过的字典类型
     * @throws ValidateException 验证失败时抛出
     */
    public static <T extends CharSequence> T validateDictType(T dictType, String errorMsg) {
        if (!isValidDictType(dictType)) {
            throw new ValidateException(StrUtil.isNotBlank(errorMsg) ? errorMsg : "字典类型格式不正确");
        }
        return dictType;
    }

    // ==================== 密码验证 ====================

    /**
     * 验证密码强度是否符合要求
     *
     * @param password 待验证的密码
     * @return 是否符合强密码要求
     */
    public static boolean isStrongPassword(CharSequence password) {
        return isMatchPattern(RegexPatternPool.STRONG_PASSWORD, password);
    }

    /**
     * 验证密码强度，不符合则抛出异常
     *
     * @param password   待验证的密码
     * @param errorMsg   验证失败时的异常消息
     * @param <T>        CharSequence 的子类型
     * @return 验证通过的密码
     * @throws ValidateException 验证失败时抛出
     */
    public static <T extends CharSequence> T validatePassword(T password, String errorMsg) {
        if (!isStrongPassword(password)) {
            throw new ValidateException(StrUtil.isNotBlank(errorMsg) ? errorMsg :
                "密码强度不足，需包含至少8位字符，包括大小写字母、数字和特殊字符");
        }
        return password;
    }

    // ==================== 权限标识验证 ====================

    /**
     * 验证权限标识格式是否正确
     *
     * @param permission 待验证的权限标识
     * @return 是否符合权限标识格式要求
     */
    public static boolean isValidPermission(CharSequence permission) {
        return isMatchPattern(RegexPatternPool.PERMISSION, permission);
    }

    /**
     * 验证权限标识格式，不符合则抛出异常
     *
     * @param permission 待验证的权限标识
     * @param errorMsg   验证失败时的异常消息
     * @param <T>        CharSequence 的子类型
     * @return 验证通过的权限标识
     * @throws ValidateException 验证失败时抛出
     */
    public static <T extends CharSequence> T validatePermission(T permission, String errorMsg) {
        if (!isValidPermission(permission)) {
            throw new ValidateException(StrUtil.isNotBlank(errorMsg) ? errorMsg : "权限标识格式不正确");
        }
        return permission;
    }

    // ==================== 通用验证方法 ====================

    /**
     * 检查字符序列是否匹配指定的正则表达式模式
     *
     * @param pattern 预编译的正则表达式模式
     * @param input   待检查的字符序列
     * @return 是否匹配
     */
    private static boolean isMatchPattern(Pattern pattern, CharSequence input) {
        return input != null && pattern.matcher(input).matches();
    }
}
