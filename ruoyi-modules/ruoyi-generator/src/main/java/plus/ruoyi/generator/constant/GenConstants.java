package plus.ruoyi.generator.constant;

import java.util.Set;

/**
 * 代码生成通用常量
 *
 * <p>包含代码生成器所需的所有常量定义，包括：</p>
 * <ul>
 *   <li>模板类型常量</li>
 *   <li>数据库字段类型映射</li>
 *   <li>HTML控件类型</li>
 *   <li>Java数据类型</li>
 *   <li>查询类型</li>
 *   <li>字段规则配置</li>
 * </ul>
 *
 * @author ruoyi
 */
public interface GenConstants {

    // ========== 模板类型常量 ==========

    /**
     * 单表（增删改查）模板
     */
    String TPL_CRUD = "crud";

    /**
     * 树表（增删改查）模板
     */
    String TPL_TREE = "tree";

    /**
     * 主子表（增删改查）模板
     */
    String TPL_SUB = "sub";

    // ========== 树形结构字段常量 ==========

    /**
     * 树编码字段名
     */
    String TREE_CODE = "treeCode";

    /**
     * 树父编码字段名
     */
    String TREE_PARENT_CODE = "treeParentCode";

    /**
     * 树名称字段名
     */
    String TREE_NAME = "treeName";

    // ========== 菜单相关字段常量 ==========

    /**
     * 上级菜单ID字段名
     */
    String PARENT_MENU_ID = "parentMenuId";

    /**
     * 上级菜单名称字段名
     */
    String PARENT_MENU_NAME = "parentMenuName";

    /**
     * 菜单图标字段名
     */
    String MENU_ICON = "menuIcon";

    /**
     * 菜单顺序字段名
     */
    String MENU_ORDER = "menuOrder";

    /**
     * 自动导入菜单字段名
     */
    String AUTO_IMPORT_MENU = "autoImportMenu";

    // ========== 数据库字段类型集合 ==========

    /**
     * 数据库字符串类型集合
     * <p>包含各种数据库的字符串类型，用于判断字段是否为字符串类型</p>
     */
    Set<String> COLUMNTYPE_STR = Set.of(
        "char", "varchar", "enum", "set",
        "nchar", "nvarchar", "varchar2", "nvarchar2"
    );

    /**
     * 数据库文本类型集合
     * <p>包含各种数据库的大文本类型，通常用于存储长文本内容</p>
     */
    Set<String> COLUMNTYPE_TEXT = Set.of(
        "tinytext", "text", "mediumtext", "longtext",
        "binary", "varbinary", "blob", "ntext", "image", "bytea"
    );

    /**
     * 数据库时间类型集合
     * <p>包含各种数据库的日期时间类型</p>
     */
    Set<String> COLUMNTYPE_TIME = Set.of(
        "datetime", "time", "date", "timestamp", "year", "interval",
        "smalldatetime", "datetime2", "datetimeoffset", "timestamptz"
    );

    /**
     * 数据库数字类型集合
     * <p>包含各种数据库的数值类型，包括整数、浮点数、货币等</p>
     */
    Set<String> COLUMNTYPE_NUMBER = Set.of(
        "tinyint", "smallint", "mediumint", "int", "int2", "int4", "int8",
        "number", "integer", "bit", "bigint", "float", "float4", "float8",
        "double", "decimal", "numeric", "real", "double precision",
        "smallserial", "serial", "bigserial", "money", "smallmoney"
    );

    // ========== 字段权限控制集合 ==========

    /**
     * BO对象不需要添加的字段集合
     * <p>这些字段通常由系统自动维护，不需要在新增时手动填写</p>
     */
    Set<String> COLUMNNAME_NOT_ADD = Set.of(
        "create_dept", "create_by", "create_time", "is_deleted",
        "update_by", "update_time", "version", "tenant_id"
    );

    /**
     * BO对象不需要编辑的字段集合
     * <p>这些字段创建后不允许修改，或由系统自动维护</p>
     */
    Set<String> COLUMNNAME_NOT_EDIT = Set.of(
        "create_dept", "create_by", "create_time", "is_deleted",
        "update_by", "update_time", "version", "tenant_id"
    );

    /**
     * VO对象不需要返回的字段集合
     * <p>这些字段属于内部字段，不需要返回给前端</p>
     */
    Set<String> COLUMNNAME_NOT_LIST = Set.of(
        "create_dept", "create_by", "is_deleted",
        "update_by", "version", "tenant_id"
    );

    /**
     * BO对象不需要查询的字段集合
     * <p>这些字段通常不作为查询条件使用</p>
     */
    Set<String> COLUMNNAME_NOT_QUERY = Set.of(
        "create_dept", "create_by", "is_deleted", "update_by",
        "update_time", "remark", "version", "tenant_id"
    );

    /**
     * Entity基类字段集合
     * <p>继承自基类的公共字段</p>
     */
    Set<String> BASE_ENTITY = Set.of(
        "createDept", "createBy", "createTime",
        "updateBy", "updateTime", "tenantId"
    );

    // ========== HTML控件类型常量 ==========

    /**
     * 文本框控件
     * <p>用于输入单行文本</p>
     */
    String HTML_INPUT = "input";

    /**
     * 文本域控件
     * <p>用于输入多行文本</p>
     */
    String HTML_TEXTAREA = "textarea";

    /**
     * 下拉框控件
     * <p>用于从预定义选项中选择</p>
     */
    String HTML_SELECT = "select";

    /**
     * 单选框控件
     * <p>用于从多个选项中选择一个</p>
     */
    String HTML_RADIO = "radio";

    /**
     * 复选框控件
     * <p>用于选择多个选项</p>
     */
    String HTML_CHECKBOX = "checkbox";

    /**
     * 日期时间控件
     * <p>用于选择日期和时间</p>
     */
    String HTML_DATETIME = "datetime";

    /**
     * 图片上传控件
     * <p>用于上传图片文件</p>
     */
    String HTML_IMAGE_UPLOAD = "imageUpload";

    /**
     * 文件上传控件
     * <p>用于上传各种类型的文件</p>
     */
    String HTML_FILE_UPLOAD = "fileUpload";

    /**
     * 富文本编辑器控件
     * <p>用于编辑富文本内容</p>
     */
    String HTML_EDITOR = "editor";

    /**
     * 数字输入框控件
     * <p>用于输入数字，带有type=number属性</p>
     */
    String HTML_NUMBER_INPUT = "numberInput";

    // ========== Java数据类型常量 ==========

    /**
     * 字符串类型
     */
    String TYPE_STRING = "String";

    /**
     * 整型
     * <p>用于表示较小的整数值</p>
     */
    String TYPE_INTEGER = "Integer";

    /**
     * 长整型
     * <p>用于表示较大的整数值</p>
     */
    String TYPE_LONG = "Long";

    /**
     * 双精度浮点型
     */
    String TYPE_DOUBLE = "Double";

    /**
     * 高精度计算类型
     * <p>用于需要精确计算的场景，如金额</p>
     */
    String TYPE_BIGDECIMAL = "BigDecimal";

    /**
     * 日期时间类型
     */
    String TYPE_DATE = "Date";

    // ========== 查询类型常量 ==========

    /**
     * 模糊查询
     * <p>使用LIKE进行模糊匹配</p>
     */
    String QUERY_LIKE = "LIKE";

    /**
     * 相等查询
     * <p>使用等号进行精确匹配</p>
     */
    String QUERY_EQ = "EQ";

    /**
     * 范围查询
     * <p>在两个值之间进行查询</p>
     */
    String BETWEEN = "BETWEEN";

    /**
     * 必填标识
     * <p>表示该字段为必填项</p>
     */
    String REQUIRE = "1";

    // ========== 字段后缀识别集合 ==========

    /**
     * 图片字段后缀集合
     * <p>以这些后缀结尾的字段将自动设置为图片上传控件</p>
     */
    Set<String> IMAGE_FIELD_SUFFIXES = Set.of("image", "img", "avatar");

    /**
     * 文件字段后缀集合
     * <p>以这些后缀结尾的字段将自动设置为文件上传控件</p>
     */
    Set<String> FILE_FIELD_SUFFIXES = Set.of("file");

    /**
     * 内容字段后缀集合
     * <p>以这些后缀结尾的字段将自动设置为富文本编辑器</p>
     */
    Set<String> CONTENT_FIELD_SUFFIXES = Set.of("content", "detail");

    /**
     * 状态字段后缀集合
     * <p>以这些后缀结尾的字段将自动设置为单选框控件</p>
     */
    Set<String> STATUS_FIELD_SUFFIXES = Set.of("status");

    /**
     * 类型字段后缀集合
     * <p>以这些后缀结尾的字段将自动设置为下拉框控件</p>
     */
    Set<String> TYPE_FIELD_SUFFIXES = Set.of("type");

    /**
     * 性别字段后缀集合
     * <p>以这些后缀结尾的字段将自动设置为下拉框控件</p>
     */
    Set<String> GENDER_FIELD_SUFFIXES = Set.of("gender");

    /**
     * 名称字段后缀集合
     * <p>以这些后缀结尾的字段将自动设置为模糊查询</p>
     */
    Set<String> NAME_FIELD_SUFFIXES = Set.of("name");

    /**
     * 数字输入框字段后缀集合
     * <p>以这些后缀结尾的字段将自动设置为type=number的数字输入框</p>
     */
    Set<String> NUMBER_INPUT_FIELD_SUFFIXES = Set.of("num", "count", "number", "amount", "order");

    // ========== 字段前缀识别集合 ==========

    /**
     * 布尔字段前缀集合
     * <p>以这些前缀开头的字段将自动识别为布尔类型</p>
     */
    Set<String> BOOLEAN_FIELD_PREFIXES = Set.of("is");

    /**
     * 布尔字段排除集合
     * <p>这些字段虽然以is开头，但不应被识别为布尔字段</p>
     */
    Set<String> BOOLEAN_FIELD_EXCLUDES = Set.of("is_deleted");

    // ========== 特殊字段名常量 ==========

    /**
     * 标准状态字段名
     * <p>名为"status"的字段会有特殊处理逻辑</p>
     */
    String STATUS_FIELD_NAME = "status";

    /**
     * 标准备注字段名
     * <p>名为"remark"的字段会有特殊处理逻辑</p>
     */
    String REMARK_FIELD_NAME = "remark";

    // ========== 数值阈值常量 ==========

    /**
     * 文本域长度阈值
     * <p>字符串长度超过此值将使用文本域而非文本框</p>
     */
    int TEXTAREA_LENGTH_THRESHOLD = 500;

    /**
     * 整型字段长度阈值
     * <p>数字长度小于等于此值将使用Integer类型，否则使用Long类型</p>
     */
    int INTEGER_LENGTH_THRESHOLD = 10;


    // ========== 特殊注释关键字常量 ==========

    /**
     * 审核状态注释关键字
     * <p>字段注释为此值时将使用审核状态字典</p>
     */
    String COMMENT_AUDIT_STATUS = "审核状态";

    /**
     * 布尔类型注释关键字
     * <p>字段注释包含此关键字时将识别为布尔类型</p>
     */
    String COMMENT_BOOLEAN_KEYWORD = "是否";

    // ========== 正则表达式常量 ==========

    /**
     * 表名/注释替换正则表达式
     * <p>用于清理表名和注释中的特定关键字</p>
     */
    String REPLACEMENT_REGEX = "(?:表|若依)";
}
