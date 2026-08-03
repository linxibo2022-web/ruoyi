package plus.ruoyi.common.core.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.AntPathMatcher;

import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 字符串工具类
 * <p>基于Apache Commons Lang3和HuTool的字符串工具进行扩展</p>
 * <p>提供字符串格式化、分割、转换、校验等常用字符串操作功能</p>
 *
 * @author Lion Li
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE, onConstructor_ = @SuppressWarnings("deprecation"))
public class StringUtils extends org.apache.commons.lang3.StringUtils {

    /**
     * 默认分隔符：逗号
     */
    public static final String SEPARATOR = ",";

    /**
     * 斜杠分隔符
     */
    public static final String SLASH = "/";

    /**
     * 获取非空白字符串，如果为空白则返回默认值
     * <p>示例：{@code String result = StringUtils.blankToDefault("", "默认值")}</p>
     *
     * @param str          待判断的字符串
     * @param defaultValue 默认值
     * @return 非空白的原字符串或默认值
     */
    public static String blankToDefault(String str, String defaultValue) {
        return StrUtil.blankToDefault(str, defaultValue);
    }

    /**
     * 判断字符串是否为空（null或长度为0）
     * <p>示例：{@code boolean empty = StringUtils.isEmpty("")} // true</p>
     *
     * @param str 待判断的字符串
     * @return true表示为空，false表示非空
     */
    public static boolean isEmpty(String str) {
        return StrUtil.isEmpty(str);
    }

    /**
     * 判断字符串是否为非空
     * <p>示例：{@code boolean notEmpty = StringUtils.isNotEmpty("hello")} // true</p>
     *
     * @param str 待判断的字符串
     * @return true表示非空，false表示为空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 去除字符串首尾空白字符
     * <p>示例：{@code String result = StringUtils.trim("  hello  ")} // "hello"</p>
     *
     * @param str 待处理的字符串
     * @return 去除首尾空白后的字符串
     */
    public static String trim(String str) {
        return StrUtil.trim(str);
    }

    /**
     * 从指定位置开始截取字符串到末尾
     * <p>示例：{@code String result = StringUtils.substring("hello", 1)} // "ello"</p>
     *
     * @param str   源字符串
     * @param start 开始位置（包含）
     * @return 截取后的字符串
     */
    public static String substring(final String str, int start) {
        return substring(str, start, str.length());
    }

    /**
     * 截取指定范围的字符串
     * <p>示例：{@code String result = StringUtils.substring("hello", 1, 3)} // "el"</p>
     *
     * @param str   源字符串
     * @param start 开始位置（包含）
     * @param end   结束位置（不包含）
     * @return 截取后的字符串
     */
    public static String substring(final String str, int start, int end) {
        return StrUtil.sub(str, start, end);
    }

    /**
     * 格式化文本，{}表示占位符
     * <p>示例：{@code String result = StringUtils.format("Hello {}, age {}", "Tom", 25)} // "Hello Tom, age 25"</p>
     * <p>转义用法：</p>
     * <ul>
     *     <li>转义{}：format("this is \\{} for {}", "a", "b") → this is {} for a</li>
     *     <li>转义\：format("this is \\\\{} for {}", "a", "b") → this is \a for b</li>
     * </ul>
     *
     * @param template 文本模板，被替换的部分用{}表示
     * @param params   参数值
     * @return 格式化后的文本
     */
    public static String format(String template, Object... params) {
        return StrUtil.format(template, params);
    }

    /**
     * 判断字符串是否为HTTP(S)链接
     * <p>示例：{@code boolean isUrl = StringUtils.isHttpUrl("https://www.example.com")} // true</p>
     *
     * @param link 待判断的链接字符串
     * @return true表示是有效的HTTP(S)链接，false表示不是
     */
    public static boolean isUrl(String link) {
        return Validator.isUrl(link);
    }

    /**
     * 字符串转Set集合
     * <p>示例：{@code Set<String> set = StringUtils.stringToSet("a,b,c", ",")} // {"a", "b", "c"}</p>
     *
     * @param str       源字符串
     * @param separator 分隔符
     * @return Set集合，去重后的结果
     */
    public static Set<String> stringToSet(String str, String separator) {
        return new HashSet<>(stringToList(str, separator, true, false));
    }

    /**
     * 字符串转List集合
     * <p>示例：{@code List<String> list = StringUtils.stringToList("a, b, c", ",", true, true)} // ["a", "b", "c"]</p>
     *
     * @param str         源字符串
     * @param separator   分隔符
     * @param filterBlank 是否过滤空白字符串
     * @param trim        是否去除每个元素的首尾空白
     * @return List集合
     */
    public static List<String> stringToList(String str, String separator, boolean filterBlank, boolean trim) {
        List<String> list = new ArrayList<>();
        if (isEmpty(str)) {
            return list;
        }

        // 过滤空白字符串
        if (filterBlank && isBlank(str)) {
            return list;
        }
        String[] split = str.split(separator);
        for (String string : split) {
            if (filterBlank && isBlank(string)) {
                continue;
            }
            if (trim) {
                string = trim(string);
            }
            list.add(string);
        }

        return list;
    }

    /**
     * 检查字符串是否包含指定字符串列表中的任意一个（忽略大小写）
     * <p>示例：{@code boolean contains = StringUtils.containsAnyIgnoreCase("Hello", "hello", "world")} // true</p>
     *
     * @param cs                  指定字符串
     * @param searchCharSequences 需要检查的字符串数组
     * @return true表示包含任意一个字符串，false表示都不包含
     */
    public static boolean containsAnyIgnoreCase(CharSequence cs, CharSequence... searchCharSequences) {
        return StrUtil.containsAnyIgnoreCase(cs, searchCharSequences);
    }

    /**
     * 驼峰命名转下划线命名
     * <p>示例：{@code String result = StringUtils.camelToUnderscore("userName")} // "user_name"</p>
     *
     * @param str 驼峰命名的字符串
     * @return 下划线命名的字符串
     */
    public static String camelToUnderscore(String str) {
        return StrUtil.toUnderlineCase(str);
    }

    /**
     * 检查字符串是否等于指定字符串数组中的任意一个（忽略大小写）
     * <p>示例：{@code boolean equals = StringUtils.equalsAnyIgnoreCase("Hello", "hello", "world")} // true</p>
     *
     * @param str  待验证字符串
     * @param strs 字符串数组
     * @return true表示相等，false表示不相等
     */
    public static boolean equalsAnyIgnoreCase(String str, String... strs) {
        return StrUtil.equalsAnyIgnoreCase(str, strs);
    }

    /**
     * 下划线命名转大驼峰命名（首字母大写）
     * <p>示例：{@code String result = StringUtils.underscoreToPascalCase("user_name")} // "UserName"</p>
     *
     * @param name 下划线命名的字符串
     * @return 大驼峰命名的字符串
     */
    public static String underscoreToPascalCase(String name) {
        return StrUtil.upperFirst(StrUtil.toCamelCase(name));
    }

    /**
     * 下划线命名转小驼峰命名
     * <p>示例：{@code String result = StringUtils.underscoreToCamelCase("user_name")} // "userName"</p>
     *
     * @param str 下划线命名的字符串
     * @return 小驼峰命名的字符串
     */
    public static String underscoreToCamelCase(String str) {
        return StrUtil.toCamelCase(str);
    }

    /**
     * 检查字符串是否匹配指定模式列表中的任意一个
     * <p>示例：{@code boolean match = StringUtils.matchesAny("/api/user", Arrays.asList("/api/*", "/admin/*"))} // true</p>
     *
     * @param str      待匹配的字符串
     * @param patterns 模式列表
     * @return true表示匹配，false表示不匹配
     */
    public static boolean matchesAny(String str, List<String> patterns) {
        if (isEmpty(str) || CollUtil.isEmpty(patterns)) {
            return false;
        }
        for (String pattern : patterns) {
            if (isMatch(pattern, str)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 使用Ant路径匹配器判断URL是否匹配规则
     * <p>示例：{@code boolean match = StringUtils.isMatch("/api/*", "/api/user")} // true</p>
     * <p>匹配规则：</p>
     * <ul>
     *     <li>? 表示单个字符</li>
     *     <li>* 表示一层路径内的任意字符串，不可跨层级</li>
     *     <li>** 表示任意层路径</li>
     * </ul>
     *
     * @param pattern 匹配规则
     * @param url     需要匹配的URL
     * @return true表示匹配，false表示不匹配
     */
    public static boolean isMatch(String pattern, String url) {
        AntPathMatcher matcher = new AntPathMatcher();
        return matcher.match(pattern, url);
    }

    /**
     * 数字左边补零到指定长度
     * <p>示例：{@code String result = StringUtils.leftPadWithZero(123, 5)} // "00123"</p>
     *
     * @param num  数字对象
     * @param size 目标字符串长度
     * @return 左补零后的字符串
     */
    public static String leftPadWithZero(final Number num, final int size) {
        return leftPad(num.toString(), size, '0');
    }

    /**
     * 字符串左补齐到指定长度
     * <p>示例：{@code String result = StringUtils.leftPad("abc", 5, '*')} // "**abc"</p>
     *
     * @param str     原始字符串
     * @param size    目标字符串长度
     * @param padChar 用于补齐的字符
     * @return 左补齐后的字符串，如果原字符串长度大于目标长度则截取后面部分
     */
    public static String leftPad(final String str, final int size, final char padChar) {
        final StringBuilder sb = new StringBuilder(size);
        if (str != null) {
            final int len = str.length();
            if (str.length() <= size) {
                sb.append(String.valueOf(padChar).repeat(size - len));
                sb.append(str);
            } else {
                return str.substring(len - size, len);
            }
        } else {
            sb.append(String.valueOf(padChar).repeat(Math.max(0, size)));
        }
        return sb.toString();
    }

    /**
     * 分割字符串为字符串列表（默认逗号分隔）
     * <p>示例：{@code List<String> list = StringUtils.splitToList("a,b,c")} // ["a", "b", "c"]</p>
     *
     * @param str 被分割的字符串
     * @return 分割后的字符串列表
     */
    public static List<String> splitToList(String str) {
        return splitToList(str, Convert::toStr);
    }

    /**
     * 分割字符串为字符串列表
     * <p>示例：{@code List<String> list = StringUtils.splitToList("a;b;c", ";")} // ["a", "b", "c"]</p>
     *
     * @param str       被分割的字符串
     * @param separator 分隔符
     * @return 分割后的字符串列表
     */
    public static List<String> splitToList(String str, String separator) {
        return splitToList(str, separator, Convert::toStr);
    }

    /**
     * 分割字符串并自定义转换（默认逗号分隔）
     * <p>示例：{@code List<Integer> list = StringUtils.splitToList("1,2,3", Integer::valueOf)} // [1, 2, 3]</p>
     *
     * @param <T>    转换后的类型
     * @param str    被分割的字符串
     * @param mapper 自定义转换函数
     * @return 分割并转换后的列表
     */
    public static <T> List<T> splitToList(String str, Function<? super Object, T> mapper) {
        return splitToList(str, SEPARATOR, mapper);
    }

    /**
     * 分割字符串并自定义转换
     * <p>示例：{@code List<Integer> list = StringUtils.splitToList("1;2;3", ";", Integer::valueOf)} // [1, 2, 3]</p>
     *
     * @param <T>       转换后的类型
     * @param str       被分割的字符串
     * @param separator 分隔符
     * @param mapper    自定义转换函数
     * @return 分割并转换后的列表
     */
    public static <T> List<T> splitToList(String str, String separator, Function<? super Object, T> mapper) {
        if (isBlank(str)) {
            return new ArrayList<>(0);
        }
        return StrUtil.split(str, separator)
            .stream()
            .filter(Objects::nonNull)
            .map(mapper)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * 检查字符串是否以指定前缀数组中的任意一个开头（忽略大小写）
     * <p>示例：{@code boolean starts = StringUtils.startsWithAnyIgnoreCase("Hello", "he", "hi")} // true</p>
     *
     * @param str      要检查的字符串
     * @param prefixes 前缀数组
     * @return true表示以任意前缀开头，false表示都不匹配
     */
    public static boolean startsWithAnyIgnoreCase(CharSequence str, CharSequence... prefixes) {
        for (CharSequence prefix : prefixes) {
            if (StringUtils.startsWithIgnoreCase(str, prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 字符串字符集转换
     * <p>示例：{@code String result = StringUtils.convertCharset("中文", StandardCharsets.UTF_8, StandardCharsets.ISO_8859_1)}</p>
     *
     * @param input       原始字符串
     * @param fromCharset 源字符集
     * @param toCharset   目标字符集
     * @return 转换后的字符串，转换失败时返回原字符串
     */
    public static String convertCharset(String input, Charset fromCharset, Charset toCharset) {
        if (isBlank(input)) {
            return input;
        }
        try {
            // 从源字符集获取字节
            byte[] bytes = input.getBytes(fromCharset);
            // 使用目标字符集解码
            return new String(bytes, toCharset);
        } catch (Exception e) {
            return input;
        }
    }

    /**
     * 以逗号拼接元素
     *
     * @param iterable 可迭代对象
     * @return 拼接后的字符串
     */
    public static String joinComma(Iterable<?> iterable) {
        return StringUtils.join(iterable, SEPARATOR);
    }

    /**
     * 将数组中的元素使用逗号拼接成字符串
     *
     * @param array 任意类型的数组
     * @return 拼接后的字符串
     */
    public static String joinComma(Object[] array) {
        return StringUtils.join(array, SEPARATOR);
    }

    /**
     * 使用映射关系转换字符串（默认逗号分隔）
     * <p>示例：{@code String result = StringUtils.convertWithMapping("1,2", statusMap)} // "启用,禁用"</p>
     *
     * @param inputValue 输入值（单个值或多个值用分隔符分割）
     * @param mapping    映射关系（key为输入值，value为输出值）
     * @return 转换后的值，无效值返回空字符串
     */
    public static String convertWithMapping(String inputValue, Map<String, String> mapping) {
        return convertWithMapping(inputValue, SEPARATOR, mapping);
    }

    /**
     * 使用映射关系转换字符串
     * <p>支持单值和多值（分隔符分割）的批量转换</p>
     * <p>示例：</p>
     * <ul>
     *     <li>单值：{@code convertWithMapping("1", ",", statusMap) → "启用"}</li>
     *     <li>多值：{@code convertWithMapping("1,2", ",", statusMap) → "启用,禁用"}</li>
     *     <li>ID转名称：{@code convertWithMapping("101,102", ",", userMap) → "张三,李四"}</li>
     * </ul>
     *
     * @param inputValue 输入值（单个值或多个值用分隔符分割）
     * @param separator  分隔符
     * @param mapping    映射关系（key为输入值，value为输出值）
     * @return 转换后的值（单个值或多个值用分隔符连接），无效值返回空字符串
     */
    public static String convertWithMapping(String inputValue, String separator, Map<String, String> mapping) {
        if (isBlank(inputValue) || mapping == null || mapping.isEmpty()) {
            return EMPTY;
        }

        // 处理单值情况
        if (!inputValue.contains(separator)) {
            return mapping.getOrDefault(inputValue.trim(), EMPTY);
        }

        // 处理多值情况
        return Arrays.stream(inputValue.split(separator))
            .map(String::trim)
            .filter(StringUtils::isNotBlank)
            .map(value -> mapping.getOrDefault(value, EMPTY))
            .collect(Collectors.joining(separator));
    }

    /**
     * 批量转换多个字符串
     * <p>示例：{@code List<String> results = StringUtils.convertBatchWithMapping(Arrays.asList("1", "2"), statusMap)}</p>
     * <p>返回格式：["启用", "禁用"]</p>
     *
     * @param inputValues 输入值列表
     * @param mapping     映射关系
     * @return 转换后的值列表，保持原有顺序
     */
    public static List<String> convertBatchWithMapping(List<String> inputValues, Map<String, String> mapping) {
        if (inputValues == null || inputValues.isEmpty() || mapping == null || mapping.isEmpty()) {
            return new ArrayList<>();
        }

        return inputValues.stream()
            .map(value -> isBlank(value) ? EMPTY : mapping.getOrDefault(value.trim(), EMPTY))
            .collect(Collectors.toList());
    }

    /**
     * 反向映射转换（值转键）
     * <p>示例：{@code String result = StringUtils.convertWithReverseMapping("启用,禁用", ",", statusMap)} // "1,2"</p>
     * <p>根据映射的value查找对应的key进行转换</p>
     *
     * @param inputValue 输入值（映射的value值）
     * @param separator  分隔符
     * @param mapping    映射关系（key为原始值，value为目标值）
     * @return 转换后的键值，无效值返回空字符串
     */
    public static String convertWithReverseMapping(String inputValue, String separator, Map<String, String> mapping) {
        if (isBlank(inputValue) || mapping == null || mapping.isEmpty()) {
            return EMPTY;
        }

        // 构建反向映射（value -> key）
        Map<String, String> reverseMapping = mapping.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getValue,
                Map.Entry::getKey,
                // 如果有重复value，保留第一个
                (existing, replacement) -> existing
            ));

        return convertWithMapping(inputValue, separator, reverseMapping);
    }

    /**
     * 向逗号分隔的字符串中添加新元素
     * <p>
     * 如果元素已存在则不重复添加，自动去重
     *
     * @param commaSeparatedStr 原始逗号分隔字符串，可以为null或空
     * @param newElement 要添加的新元素
     * @return 添加元素后的逗号分隔字符串
     */
    public static String addToCommaString(String commaSeparatedStr, String newElement) {
        if (isBlank(newElement)) {
            return commaSeparatedStr == null ? "" : commaSeparatedStr;
        }

        Set<String> set = new HashSet<>();

        // 添加原有元素
        if (isNotBlank(commaSeparatedStr)) {
            Arrays.stream(commaSeparatedStr.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .forEach(set::add);
        }

        // 添加新元素
        set.add(newElement.trim());

        return String.join(",", set);
    }

}
