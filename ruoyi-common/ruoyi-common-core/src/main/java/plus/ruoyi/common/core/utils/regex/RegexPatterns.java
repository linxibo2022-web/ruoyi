package plus.ruoyi.common.core.utils.regex;

import cn.hutool.core.lang.RegexPool;

/**
 * 正则表达式模式常量
 * <p>定义系统中常用的正则表达式模式字符串</p>
 * <p>更多正则表达式参考: https://any86.github.io/any-rule/</p>
 *
 * @author Feng
 */
public interface RegexPatterns extends RegexPool {

    /**
     * 国际化键格式：英文段.英文段.英文段...（至少2段）
     * 英文段规则：字母开头，可包含字母、数字、下划线
     * 示例：user.profile.name, system.error.message
     */
    String I18N_KEY = "^[a-zA-Z][a-zA-Z0-9_]*(?:\\.[a-zA-Z][a-zA-Z0-9_]*)+$";

    /**
     * 字典类型：小写字母开头，只能包含小写字母、数字、下划线
     * 示例：user_type, status_dict, sys_config
     */
    String DICT_TYPE = "^[a-z][a-z0-9_]*$";

    /**
     * 权限标识格式：模块:操作:资源 或 空字符串
     * 第一部分（模块）：只能包含字母、数字、下划线
     * 第二、三部分（操作、资源）：可包含字母、数字、下划线、通配符*
     * 示例：user:edit:profile, system:*:*, ""
     */
    String PERMISSION = "^$|^[a-zA-Z0-9_]+:[a-zA-Z0-9_*]+:[a-zA-Z0-9_*]+$";

    /**
     * 身份证号码后6位：DDMMNN格式
     * DD：日期(01-31)，MM：月份(01-12)，NN：顺序码+校验码
     * 示例：150389, 281234
     */
    String ID_CARD_SUFFIX = "^(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$";

    /**
     * QQ号码：5-11位数字，不能以0开头
     * 示例：12345, 1234567890
     */
    String QQ = "^[1-9][0-9]\\d{4,9}$";

    /**
     * 中国邮政编码：6位数字，不能以0开头
     * 示例：100000, 518000
     */
    String POSTAL_CODE = "^[1-9]\\d{5}$";

    /**
     * 用户账号：字母开头，5-16位，可包含字母、数字、下划线
     * 示例：admin, user123, test_user
     */
    String ACCOUNT = "^[a-zA-Z][a-zA-Z0-9_]{4,15}$";

    /**
     * 强密码：至少8位，必须包含大小写字母、数字、特殊字符
     * 特殊字符范围：@$!%*?&
     * 示例：Password123!, Admin@2024
     */
    String STRONG_PASSWORD = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";

    /**
     * 通用状态：0(正常) 或 1(停用)
     * 示例：0, 1
     */
    String BINARY_STATUS = "^[01]$";
}
