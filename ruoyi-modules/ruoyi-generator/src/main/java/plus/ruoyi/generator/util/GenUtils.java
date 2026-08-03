package plus.ruoyi.generator.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import plus.ruoyi.common.core.dict.DictAuditStatus;
import plus.ruoyi.common.core.dict.DictBooleanFlag;
import plus.ruoyi.common.core.dict.DictEnableStatus;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.generator.config.GenConfig;
import plus.ruoyi.generator.constant.GenConstants;
import plus.ruoyi.generator.domain.GenTable;
import plus.ruoyi.generator.domain.GenTableColumn;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 代码生成器工具类
 *
 * <p>提供代码生成相关的工具方法，主要功能包括：</p>
 * <ul>
 *   <li>表信息初始化</li>
 *   <li>列属性字段初始化</li>
 *   <li>数据类型转换</li>
 *   <li>字段规则设置</li>
 *   <li>字符串处理工具</li>
 *   <li>默认值处理</li>
 * </ul>
 *
 * @author ruoyi
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GenUtils {

    /**
     * 预编译的表名/注释替换正则表达式
     * <p>用于提高字符串替换性能</p>
     */
    private static final Pattern REPLACEMENT_PATTERN = Pattern.compile(GenConstants.REPLACEMENT_REGEX);

    /**
     * 预编译的列类型长度提取正则表达式
     * <p>用于从数据库列类型中提取长度信息，如 varchar(50) -> 50</p>
     */
    private static final Pattern COLUMN_TYPE_PATTERN = Pattern.compile("\\((\\d+)(?:,\\d+)?\\)");

    /**
     * 初始化表信息
     * <p>根据数据库表信息设置代码生成所需的基本属性</p>
     *
     * @param genTable 代码生成表对象
     */
    public static void initTable(GenTable genTable) {
        if (genTable == null) {
            return;
        }

        // 设置生成代码方式（0-zip压缩包 1-自定义路径）
        genTable.setGenType(GenConfig.getDefaultGenType());
        // 设置模块名
        genTable.setModuleName(getModuleName(GenConfig.getPackageName()));
        // 设置包名
        genTable.setPackageName(GenConfig.getPackageName());
        // 设置Java类名（驼峰命名）
        genTable.setClassName(convertClassName(genTable.getTableName()));
        // 设置业务名（用于生成路径和变量名）
        genTable.setBusinessName(getBusinessName(genTable.getTableName()));
        // 设置功能名（清理后的表注释）
        genTable.setFunctionName(replaceText(genTable.getTableComment()));
        // 设置作者
        genTable.setFunctionAuthor(GenConfig.getAuthor());

        // 初始化扩展参数（菜单图标、自动导入菜单等）
        initTableParams(genTable);
    }

    /**
     * 初始化表的扩展参数
     * <p>从配置文件读取默认值并设置到 params 中</p>
     *
     * @param genTable 代码生成表对象
     */
    private static void initTableParams(GenTable genTable) {
        Map<String, Object> params = new HashMap<>();
        // 从配置文件读取默认菜单图标
        params.put(GenConstants.MENU_ICON, GenConfig.getMenuIcon());
        // 从配置文件读取默认菜单顺序
        params.put(GenConstants.MENU_ORDER, GenConfig.getMenuOrder());
        // 从配置文件读取默认自动导入菜单设置
        params.put(GenConstants.AUTO_IMPORT_MENU, GenConfig.getAutoImportMenu());
        genTable.setParams(params);
    }

    /**
     * 初始化列属性字段
     * <p>根据数据库列信息自动推断并设置代码生成所需的各种属性</p>
     *
     * @param column 数据库列对象
     * @param table  所属表对象
     */
    public static void initColumnField(GenTableColumn column, GenTable table) {
        if (column == null || table == null) {
            return;
        }

        // 获取数据库类型（去掉长度信息）
        String dataType = getDbType(column.getColumnType());
        // 统一转小写，避免数据库大小写问题
        String columnName = StringUtils.lowerCase(column.getColumnName());

        // 设置基础属性
        initBasicColumnProperties(column, table, columnName);

        // 根据数据类型设置字段属性
        setColumnPropertiesByDataType(column, dataType);

        // 设置默认的增删改查权限
        setDefaultPermissions(column, columnName);

        // 根据字段名设置特殊属性
        setSpecialPropertiesByColumnName(column, columnName);

        // 处理默认值 - 新增
        processColumnDefault(column);
    }

    /**
     * 处理列默认值，转换为前端代码中合适的格式
     * <p>将数据库中的字符串默认值转换为对应Java类型的前端表示</p>
     *
     * @param column 数据库列对象
     */
    private static void processColumnDefault(GenTableColumn column) {
        String defaultValue = column.getColumnDefault();

        // 如果没有默认值或为null，设置为undefined
        if (StringUtils.isBlank(defaultValue) || "null".equalsIgnoreCase(defaultValue)) {
            column.setColumnDefault("undefined");
            return;
        }

        // 清理 PostgreSQL 类型转换后缀（如 '000000'::varchar -> 000000）
        defaultValue = cleanPostgresqlDefault(defaultValue);

        // 清理后再次检查是否为空或null
        if (StringUtils.isBlank(defaultValue) || "null".equalsIgnoreCase(defaultValue)) {
            column.setColumnDefault("undefined");
            return;
        }

        String javaType = column.getJavaType();
        String processedValue;

        switch (javaType) {
            case GenConstants.TYPE_STRING:
                // 字符串类型，添加引号并转义
                processedValue = "'" + defaultValue.replace("'", "\\'") + "'";
                break;

            case GenConstants.TYPE_INTEGER:
            case GenConstants.TYPE_LONG:
            case GenConstants.TYPE_BIGDECIMAL:
                // 数字类型，验证并直接使用
                if (isValidNumber(defaultValue)) {
                    processedValue = defaultValue;
                } else {
                    processedValue = "undefined";
                }
                break;

            case "Boolean":
                // 布尔类型转换
                if ("1".equals(defaultValue) || "true".equalsIgnoreCase(defaultValue)) {
                    processedValue = "true";
                } else if ("0".equals(defaultValue) || "false".equalsIgnoreCase(defaultValue)) {
                    processedValue = "false";
                } else {
                    processedValue = "undefined";
                }
                break;

            case GenConstants.TYPE_DATE:
                // 日期类型的函数默认值不处理
                if (isSqlFunction(defaultValue)) {
                    processedValue = "undefined";
                } else {
                    // 具体日期值，保持引号
                    processedValue = "'" + defaultValue + "'";
                }
                break;

            default:
                // 其他类型默认当作字符串处理
                processedValue = "'" + defaultValue.replace("'", "\\'") + "'";
                break;
        }

        // 更新默认值为处理后的值
        column.setColumnDefault(processedValue);
    }

    /**
     * 验证是否为有效数字
     * <p>使用BigDecimal验证，可以处理整数和小数</p>
     *
     * @param value 待验证的字符串
     * @return true如果是有效数字
     */
    private static boolean isValidNumber(String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }

        try {
            new BigDecimal(value.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 判断是否为SQL函数
     * <p>识别常见的SQL函数，如CURRENT_TIMESTAMP等</p>
     *
     * @param value 待判断的值
     * @return true如果是SQL函数
     */
    private static boolean isSqlFunction(String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }

        String upperValue = value.toUpperCase().trim();
        return upperValue.contains("CURRENT_TIMESTAMP") ||
               upperValue.contains("NOW()") ||
               upperValue.contains("SYSDATE") ||
               upperValue.contains("GETDATE()") ||
               upperValue.contains("CURRENT_DATE") ||
               upperValue.contains("CURRENT_TIME") ||
               upperValue.startsWith("NEXTVAL(") ||
               upperValue.startsWith("UUID()");
    }

    /**
     * 清理 PostgreSQL 默认值格式
     * <p>PostgreSQL 默认值可能包含类型转换后缀，需要清理后才能正确处理</p>
     * <p>处理规则：</p>
     * <ul>
     *   <li>'000000'::character varying -> 000000</li>
     *   <li>'1'::bpchar -> 1</li>
     *   <li>'value'::text -> value</li>
     *   <li>0 -> 0（数字不变）</li>
     *   <li>true -> true（布尔不变）</li>
     *   <li>nextval('seq'::regclass) -> 保持不变（SQL函数）</li>
     * </ul>
     *
     * @param value 原始默认值
     * @return 清理后的默认值
     */
    private static String cleanPostgresqlDefault(String value) {
        if (StringUtils.isBlank(value)) {
            return value;
        }

        String cleaned = value.trim();

        // 如果是 SQL 函数，不做处理
        if (isSqlFunction(cleaned)) {
            return cleaned;
        }

        // 去除 PostgreSQL 类型转换后缀 ::type（如 ::varchar, ::character varying, ::bpchar）
        int castIndex = cleaned.indexOf("::");
        if (castIndex > 0) {
            cleaned = cleaned.substring(0, castIndex).trim();
        }

        // 去除外层单引号（如果有）
        if (cleaned.startsWith("'") && cleaned.endsWith("'") && cleaned.length() >= 2) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }

        return cleaned;
    }

    /**
     * 初始化列的基础属性
     * <p>设置列对象的基本信息，如所属表、Java字段名等</p>
     *
     * @param column     列对象
     * @param table      所属表对象
     * @param columnName 列名（小写）
     */
    private static void initBasicColumnProperties(GenTableColumn column, GenTable table, String columnName) {
        column.setTableId(table.getTableId());
        column.setCreateBy(table.getCreateBy());
        // 设置Java字段名（驼峰命名）
        String javaField = StringUtils.underscoreToCamelCase(columnName);
        column.setJavaField(javaField);
        // 默认设置为字符串类型
        column.setJavaType(GenConstants.TYPE_STRING);
        // 默认设置为等值查询
        column.setQueryType(GenConstants.QUERY_EQ);

        // 初始化字段标签：如果备注为空则使用字段名，否则去除括号后的备注
        String columnComment = column.getColumnComment();
        if (StringUtils.isBlank(columnComment)) {
            column.setColumnLabel(javaField);
        } else {
            String label = removeBrackets(columnComment);
            column.setColumnLabel(StringUtils.isBlank(label) ? javaField : label);
        }
    }

    /**
     * 根据数据库类型设置字段属性
     * <p>根据数据库字段类型自动推断Java类型和HTML控件类型</p>
     *
     * @param column   列对象
     * @param dataType 数据库类型
     */
    private static void setColumnPropertiesByDataType(GenTableColumn column, String dataType) {
        if (isStringOrTextType(dataType)) {
            setStringTypeProperties(column, dataType);
        } else if (isTimeType(dataType)) {
            setTimeTypeProperties(column);
        } else if (isNumberType(dataType)) {
            setNumberTypeProperties(column);
        }
    }

    /**
     * 设置字符串类型字段属性
     * <p>根据字段长度决定使用文本框还是文本域</p>
     *
     * @param column   列对象
     * @param dataType 数据库类型
     */
    private static void setStringTypeProperties(GenTableColumn column, String dataType) {
        Integer columnLength = getColumnLength(column.getColumnType());
        // 长度超过阈值或者是文本类型，使用文本域
        boolean isTextArea = columnLength >= GenConstants.TEXTAREA_LENGTH_THRESHOLD || isTextType(dataType);
        column.setHtmlType(isTextArea ? GenConstants.HTML_TEXTAREA : GenConstants.HTML_INPUT);
    }

    /**
     * 设置时间类型字段属性
     * <p>设置为Date类型，使用日期时间控件，支持范围查询</p>
     *
     * @param column 列对象
     */
    private static void setTimeTypeProperties(GenTableColumn column) {
        column.setJavaType(GenConstants.TYPE_DATE);
        column.setHtmlType(GenConstants.HTML_DATETIME);

        String columnName = StringUtils.lowerCase(column.getColumnName());
        // 非排除字段设置为范围查询
        if (!GenConstants.COLUMNNAME_NOT_QUERY.contains(columnName)) {
            column.setQueryType(GenConstants.BETWEEN);
        }
    }

    /**
     * 设置数字类型字段属性
     * <p>根据数字精度和长度设置对应的Java类型</p>
     *
     * @param column 列对象
     */
    private static void setNumberTypeProperties(GenTableColumn column) {
        column.setHtmlType(GenConstants.HTML_INPUT);

        // 解析列类型中的长度和精度信息
        String columnTypeInfo = StringUtils.substringBetween(column.getColumnType(), "(", ")");
        if (StringUtils.isNotBlank(columnTypeInfo)) {
            String[] parts = StringUtils.split(columnTypeInfo, ",");

            if (parts.length == 2 && parseIntSafely(parts[1]) > 0) {
                // 有小数位，使用BigDecimal（用于精确计算）
                column.setJavaType(GenConstants.TYPE_BIGDECIMAL);
            } else if (parts.length == 1 && parseIntSafely(parts[0]) <= GenConstants.INTEGER_LENGTH_THRESHOLD) {
                // 长度较小的整数，使用Integer
                column.setJavaType(GenConstants.TYPE_INTEGER);
            } else {
                // 长度较大的整数，使用Long
                column.setJavaType(GenConstants.TYPE_LONG);
            }
        } else {
            // 无长度信息，默认使用Long
            column.setJavaType(GenConstants.TYPE_LONG);
        }
    }

    /**
     * 设置字段的默认增删改查权限
     * <p>根据字段名和字段属性设置默认的操作权限</p>
     *
     * @param column     列对象
     * @param columnName 列名（小写）
     */
    private static void setDefaultPermissions(GenTableColumn column, String columnName) {
        // 非系统字段且非主键，默认允许插入
        if (!GenConstants.COLUMNNAME_NOT_ADD.contains(columnName) && !column.isPk()) {
            column.setIsInsert(GenConstants.REQUIRE);
        }

        // 非系统字段，默认允许编辑
        if (!GenConstants.COLUMNNAME_NOT_EDIT.contains(columnName)) {
            column.setIsEdit(GenConstants.REQUIRE);
        }

        // 主键字段必填
        if (column.isPk()) {
            column.setIsRequired(GenConstants.REQUIRE);
        }

        // 非内部字段，默认在列表中显示
        if (!GenConstants.COLUMNNAME_NOT_LIST.contains(columnName)) {
            column.setIsList(GenConstants.REQUIRE);
        }

        // 非排除字段，默认可查询
        if (!GenConstants.COLUMNNAME_NOT_QUERY.contains(columnName)) {
            column.setIsQuery(GenConstants.REQUIRE);
        }
    }

    /**
     * 根据字段名设置特殊属性
     * <p>基于字段命名规范自动推断字段的展示方式和查询方式</p>
     *
     * @param column     列对象
     * @param columnName 列名（小写）
     */
    private static void setSpecialPropertiesByColumnName(GenTableColumn column, String columnName) {
        // name结尾的字段，默认使用模糊查询
        if (isNameField(columnName) && StringUtils.isBlank(column.getQueryType())) {
            column.setQueryType(GenConstants.QUERY_LIKE);
        }

        if (GenConstants.REMARK_FIELD_NAME.equals(columnName)) {
            column.setHtmlType(GenConstants.HTML_TEXTAREA);
            return;
        }

        // 标准status字段特殊处理
        if (GenConstants.STATUS_FIELD_NAME.equals(columnName)) {
            column.setHtmlType(GenConstants.HTML_RADIO);
            column.setDictType(DictEnableStatus.DICT_TYPE);
            return;
        }

        // 数字输入框字段，直接设置为numberInput类型
        if (isNumberInputField(columnName)) {
            column.setHtmlType(GenConstants.HTML_NUMBER_INPUT);
            return;
        }

        // 其他status字段设置单选框
        if (isStatusField(columnName) && StringUtils.isBlank(column.getHtmlType())) {
            column.setHtmlType(GenConstants.HTML_RADIO);
        }
        // type和gender字段设置下拉框
        else if (isTypeOrGenderField(columnName) && StringUtils.isBlank(column.getHtmlType())) {
            column.setHtmlType(GenConstants.HTML_SELECT);
        }
        // 布尔字段设置下拉框
        else if (isBooleanField(column, columnName)) {
            setBooleanFieldProperties(column);
        }
        // 图片字段设置图片上传控件
        else if (isImageField(columnName)) {
            column.setHtmlType(GenConstants.HTML_IMAGE_UPLOAD);
        }
        // 文件字段设置文件上传控件
        else if (isFileField(columnName)) {
            column.setHtmlType(GenConstants.HTML_FILE_UPLOAD);
        }
        // 内容字段设置富文本编辑器
        else if (isContentField(columnName) && StringUtils.isBlank(column.getHtmlType())) {
            column.setHtmlType(GenConstants.HTML_EDITOR);
        }
    }

    /**
     * 设置布尔字段属性
     * <p>为布尔类型字段设置下拉框控件和对应的字典类型</p>
     *
     * @param column 列对象
     */
    private static void setBooleanFieldProperties(GenTableColumn column) {
        column.setHtmlType(GenConstants.HTML_SELECT);
        if (GenConstants.COMMENT_AUDIT_STATUS.equals(column.getColumnComment())) {
            // 审核状态使用专用字典
            column.setDictType(DictAuditStatus.DICT_TYPE);
        } else {
            // 普通布尔字段使用通用字典
            column.setDictType(DictBooleanFlag.DICT_TYPE);
        }
    }

    // ========== 数据类型判断方法 ==========

    /**
     * 判断是否为字符串或文本类型
     *
     * @param dataType 数据库类型
     * @return true如果是字符串或文本类型
     */
    private static boolean isStringOrTextType(String dataType) {
        return GenConstants.COLUMNTYPE_STR.contains(dataType) ||
               GenConstants.COLUMNTYPE_TEXT.contains(dataType);
    }

    /**
     * 判断是否为文本类型
     *
     * @param dataType 数据库类型
     * @return true如果是文本类型
     */
    private static boolean isTextType(String dataType) {
        return GenConstants.COLUMNTYPE_TEXT.contains(dataType);
    }

    /**
     * 判断是否为时间类型
     *
     * @param dataType 数据库类型
     * @return true如果是时间类型
     */
    private static boolean isTimeType(String dataType) {
        return GenConstants.COLUMNTYPE_TIME.contains(dataType);
    }

    /**
     * 判断是否为数字类型
     *
     * @param dataType 数据库类型
     * @return true如果是数字类型
     */
    private static boolean isNumberType(String dataType) {
        return GenConstants.COLUMNTYPE_NUMBER.contains(dataType);
    }

    // ========== 字段名判断方法 ==========

    /**
     * 判断是否为名称字段
     * <p>检查字段名是否以name结尾</p>
     *
     * @param columnName 字段名
     * @return true如果是名称字段
     */
    private static boolean isNameField(String columnName) {
        return GenConstants.NAME_FIELD_SUFFIXES.stream()
            .anyMatch(suffix -> StringUtils.endsWithIgnoreCase(columnName, suffix));
    }

    /**
     * 判断是否为状态字段
     * <p>检查字段名是否以status结尾</p>
     *
     * @param columnName 字段名
     * @return true如果是状态字段
     */
    private static boolean isStatusField(String columnName) {
        return GenConstants.STATUS_FIELD_SUFFIXES.stream()
            .anyMatch(suffix -> StringUtils.endsWithIgnoreCase(columnName, suffix));
    }

    /**
     * 判断是否为类型或性别字段
     * <p>检查字段名是否以type或gender结尾</p>
     *
     * @param columnName 字段名
     * @return true如果是类型或性别字段
     */
    private static boolean isTypeOrGenderField(String columnName) {
        return GenConstants.TYPE_FIELD_SUFFIXES.stream()
            .anyMatch(suffix -> StringUtils.endsWithIgnoreCase(columnName, suffix)) ||
               GenConstants.GENDER_FIELD_SUFFIXES.stream()
            .anyMatch(suffix -> StringUtils.endsWithIgnoreCase(columnName, suffix));
    }

    /**
     * 判断是否为布尔字段
     * <p>根据字段名前缀(is)或字段注释(是否)判断是否为布尔类型</p>
     * <p>排除特殊字段如is_deleted等系统字段</p>
     *
     * @param column     列对象
     * @param columnName 字段名
     * @return true如果是布尔字段
     */
    private static boolean isBooleanField(GenTableColumn column, String columnName) {
        // 排除特殊的系统字段
        if (GenConstants.BOOLEAN_FIELD_EXCLUDES.contains(columnName)) {
            return false;
        }

        // 检查字段名是否以is开头
        boolean hasIsPrefix = GenConstants.BOOLEAN_FIELD_PREFIXES.stream()
            .anyMatch(columnName::startsWith);
        // 检查字段注释是否包含"是否"
        boolean hasBooleanComment = StringUtils.contains(column.getColumnComment(), GenConstants.COMMENT_BOOLEAN_KEYWORD);
        // 必须没有设置字典类型才能被识别为布尔字段
        return (hasIsPrefix || hasBooleanComment) && StringUtils.isBlank(column.getDictType());
    }

    /**
     * 判断是否为图片字段
     * <p>检查字段名是否以image、img、avatar结尾</p>
     *
     * @param columnName 字段名
     * @return true如果是图片字段
     */
    private static boolean isImageField(String columnName) {
        return GenConstants.IMAGE_FIELD_SUFFIXES.stream()
            .anyMatch(suffix -> StringUtils.endsWithIgnoreCase(columnName, suffix));
    }

    /**
     * 判断是否为文件字段
     * <p>检查字段名是否以file结尾</p>
     *
     * @param columnName 字段名
     * @return true如果是文件字段
     */
    private static boolean isFileField(String columnName) {
        return GenConstants.FILE_FIELD_SUFFIXES.stream()
            .anyMatch(suffix -> StringUtils.endsWithIgnoreCase(columnName, suffix));
    }

    /**
     * 判断是否为内容字段
     * <p>检查字段名是否以content结尾</p>
     *
     * @param columnName 字段名
     * @return true如果是内容字段
     */
    private static boolean isContentField(String columnName) {
        return GenConstants.CONTENT_FIELD_SUFFIXES.stream()
            .anyMatch(suffix -> StringUtils.endsWithIgnoreCase(columnName, suffix));
    }

    /**
     * 判断是否为数字输入框字段
     * <p>检查字段名是否以num、count、number、amount等后缀结尾</p>
     * <p>这类字段会自动使用type=number的数字输入框</p>
     *
     * @param columnName 字段名
     * @return true如果是数字输入框字段
     */
    private static boolean isNumberInputField(String columnName) {
        return GenConstants.NUMBER_INPUT_FIELD_SUFFIXES.stream()
            .anyMatch(suffix -> StringUtils.endsWithIgnoreCase(columnName, suffix));
    }

    // ========== 工具方法 ==========

    /**
     * 去除文本中的括号及其内容
     * <p>用于从字段备注中提取简洁的显示标签</p>
     * <p>处理规则：</p>
     * <ul>
     *   <li>去除英文括号 () 及内容</li>
     *   <li>去除中文括号 （）【】 及内容</li>
     *   <li>支持多组括号嵌套</li>
     *   <li>如果处理后为空，返回空字符串</li>
     * </ul>
     *
     * @param text 待处理的文本，如 "状态（0停用 1正常）"
     * @return 去除括号后的文本，如 "状态"
     */
    public static String removeBrackets(String text) {
        if (StringUtils.isBlank(text)) {
            return StringUtils.EMPTY;
        }

        String result = text;
        // 去除英文括号及内容
        result = result.replaceAll("\\([^)]*\\)", "");
        // 去除中文圆括号及内容
        result = result.replaceAll("[（][^）]*[）]", "");
        // 去除中文方括号及内容
        result = result.replaceAll("[【][^】]*[】]", "");

        // 去除多余空格并返回
        return StringUtils.trim(result);
    }

    /**
     * 安全的字符串转整数
     * <p>转换失败时返回0而不是抛出异常</p>
     *
     * @param str 待转换的字符串
     * @return 转换后的整数，失败时返回0
     */
    private static int parseIntSafely(String str) {
        try {
            return Integer.parseInt(StringUtils.trim(str));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 获取模块名
     * <p>从包名中提取最后一段作为模块名</p>
     *
     * @param packageName 完整包名，如 com.example.system
     * @return 模块名，如 system
     */
    public static String getModuleName(String packageName) {
        if (StringUtils.isBlank(packageName)) {
            return StringUtils.EMPTY;
        }

        int lastIndex = packageName.lastIndexOf(".");
        return lastIndex >= 0 ?
            packageName.substring(lastIndex + 1) :
            packageName;
    }

    /**
     * 获取业务名
     * <p>从表名中提取业务名称，去掉表前缀</p>
     *
     * @param tableName 数据库表名，如 sys_user
     * @return 业务名，如 user（驼峰命名）
     */
    public static String getBusinessName(String tableName) {
        if (StringUtils.isBlank(tableName)) {
            return StringUtils.EMPTY;
        }

        int firstIndex = tableName.indexOf("_");
        if (firstIndex < 0) {
            // 没有下划线，直接转驼峰
            return StringUtils.underscoreToCamelCase(tableName);
        }

        // 取第一个下划线后的部分作为业务名
        String businessName = tableName.substring(firstIndex + 1);
        return StringUtils.underscoreToCamelCase(businessName);
    }

    /**
     * 表名转换成Java类名
     * <p>将数据库表名转换为Java类名（帕斯卡命名法）</p>
     *
     * @param tableName 数据库表名
     * @return Java类名
     */
    public static String convertClassName(String tableName) {
        if (StringUtils.isBlank(tableName)) {
            return StringUtils.EMPTY;
        }

        String processedTableName = tableName;
        boolean autoRemovePre = GenConfig.getAutoRemovePre();
        String tablePrefix = GenConfig.getTablePrefix();

        // 如果配置了自动删除前缀，则处理表前缀
        if (autoRemovePre && StringUtils.isNotEmpty(tablePrefix)) {
            String[] searchList = StringUtils.split(tablePrefix, StringUtils.SEPARATOR);
            processedTableName = replaceFirst(tableName, searchList);
        }

        return StringUtils.underscoreToPascalCase(processedTableName);
    }

    /**
     * 批量替换前缀
     * <p>从文本开头移除指定的前缀之一</p>
     *
     * @param text       待处理文本
     * @param searchList 前缀数组
     * @return 移除前缀后的文本
     */
    public static String replaceFirst(String text, String[] searchList) {
        if (StringUtils.isBlank(text) || searchList == null || searchList.length == 0) {
            return StringUtils.defaultString(text);
        }

        // 遍历前缀列表，找到匹配的前缀并移除
        for (String prefix : searchList) {
            if (StringUtils.isNotEmpty(prefix) && text.startsWith(prefix)) {
                return text.substring(prefix.length());
            }
        }

        return text;
    }

    /**
     * 关键字替换
     * <p>使用预编译正则表达式清理文本中的特定关键字</p>
     *
     * @param text 需要被替换的文本
     * @return 清理后的文本
     */
    public static String replaceText(String text) {
        if (StringUtils.isBlank(text)) {
            return StringUtils.EMPTY;
        }
        return REPLACEMENT_PATTERN.matcher(text).replaceAll("");
    }

    /**
     * 获取数据库类型字段
     * <p>从完整的列类型中提取基础类型名，去掉长度等信息</p>
     *
     * @param columnType 完整列类型，如 varchar(50)
     * @return 基础类型名，如 varchar
     */
    public static String getDbType(String columnType) {
        if (StringUtils.isBlank(columnType)) {
            return StringUtils.EMPTY;
        }

        int parenthesesIndex = columnType.indexOf("(");
        return parenthesesIndex > 0 ?
            columnType.substring(0, parenthesesIndex) :
            columnType;
    }

    /**
     * 获取字段长度
     * <p>从列类型中提取长度信息</p>
     *
     * @param columnType 完整列类型，如 varchar(50) 或 decimal(10,2)
     * @return 字段长度，如 50；如果无法解析则返回0
     */
    public static Integer getColumnLength(String columnType) {
        if (StringUtils.isBlank(columnType)) {
            return 0;
        }

        java.util.regex.Matcher matcher = COLUMN_TYPE_PATTERN.matcher(columnType);
        if (matcher.find()) {
            try {
                return Integer.valueOf(matcher.group(1));
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        return 0;
    }
}
