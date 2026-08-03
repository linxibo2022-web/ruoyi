package plus.ruoyi.common.core.utils.sql;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import plus.ruoyi.common.core.utils.StringUtils;

/**
 * SQL操作工具类
 * <p>提供SQL注入防护、SQL语法校验等安全相关功能</p>
 * <p>主要用于防止SQL注入攻击，确保动态SQL的安全性</p>
 *
 * @author ruoyi
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SqlUtil {

    /**
     * 危险的SQL关键字正则表达式
     * <p>包含常见的SQL注入攻击关键字，用于检测潜在的SQL注入风险</p>
     * <p>注意：\u000B 表示垂直制表符，用于检测特殊字符绕过</p>
     */
    public static String SQL_REGEX = "\u000B|and |extractvalue|updatexml|sleep|exec |insert |select |delete |update |drop |count |chr |mid |master |truncate |char |declare |or |union |like |+|/*|user()";

    /**
     * ORDER BY子句的安全字符正则表达式
     * <p>仅允许字母、数字、下划线、空格、逗号、小数点</p>
     * <p>支持多字段排序，如：name asc, create_time desc</p>
     */
    public static final String SQL_PATTERN = "[a-zA-Z0-9_\\ \\,\\.]+";

    /**
     * 校验并转义ORDER BY语句，防止SQL注入
     * <p>检查ORDER BY子句是否包含危险字符，确保排序参数的安全性</p>
     *
     * @param value ORDER BY子句内容
     * @return 校验通过后的原始值
     * @throws IllegalArgumentException 当参数包含非法字符时抛出
     */
    public static String escapeOrderBySql(String value) {
        if (StringUtils.isNotEmpty(value) && !isValidOrderBySql(value)) {
            throw new IllegalArgumentException("参数不符合规范，不能进行查询");
        }
        return value;
    }

    /**
     * 验证ORDER BY语句是否符合安全规范
     * <p>检查是否只包含允许的字符（字母、数字、下划线、空格、逗号、小数点）</p>
     *
     * @param value 待验证的ORDER BY子句
     * @return true表示符合规范，false表示包含非法字符
     */
    public static boolean isValidOrderBySql(String value) {
        return value.matches(SQL_PATTERN);
    }

    /**
     * 过滤SQL关键字，防止SQL注入攻击
     * <p>检查输入字符串是否包含危险的SQL关键字</p>
     * <p>采用忽略大小写的方式进行匹配，提高安全检测的准确性</p>
     *
     * @param value 待检查的字符串
     * @throws IllegalArgumentException 当发现SQL关键字时抛出，提示存在注入风险
     */
    public static void filterKeyword(String value) {
        if (StringUtils.isEmpty(value)) {
            return;
        }
        // 将关键字字符串按分隔符拆分成数组
        String[] sqlKeywords = StringUtils.split(SQL_REGEX, "\\|");
        for (String sqlKeyword : sqlKeywords) {
            // 忽略大小写检查是否包含危险关键字
            if (StringUtils.indexOfIgnoreCase(value, sqlKeyword) > -1) {
                throw new IllegalArgumentException("参数存在SQL注入风险");
            }
        }
    }
}
