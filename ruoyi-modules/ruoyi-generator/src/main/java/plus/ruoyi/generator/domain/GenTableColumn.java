package plus.ruoyi.generator.domain;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;

import jakarta.validation.constraints.NotBlank;

/**
 * 代码生成业务字段表 sys_gen_table_column
 *
 * @author Lion Li
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_gen_table_column")
public class
GenTableColumn extends BaseEntity {

    /**
     * 编号
     */
    @TableId(value = "column_id")
    private Long columnId;

    /**
     * 归属表编号
     */
    private Long tableId;

    /**
     * 列名称
     */
    private String columnName;

    /**
     * 列描述
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS, jdbcType = JdbcType.VARCHAR)
    private String columnComment;

    /**
     * 字段标签（用于显示）
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS, jdbcType = JdbcType.VARCHAR)
    private String columnLabel;

    /**
     * 列类型
     */
    private String columnType;

    /**
     * JAVA类型
     */
    private String javaType;

    /**
     * JAVA字段名
     */
    @NotBlank(message = "Java属性不能为空")
    private String javaField;

    /**
     * 是否主键（1是）
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS, jdbcType = JdbcType.VARCHAR)
    private String isPk;

    /**
     * 是否自增（1是）
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS, jdbcType = JdbcType.VARCHAR)
    private String isIncrement;

    /**
     * 是否必填（1是）
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS, jdbcType = JdbcType.VARCHAR)
    private String isRequired;

    /**
     * 是否为插入字段（1是）
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS, jdbcType = JdbcType.VARCHAR)
    private String isInsert;

    /**
     * 是否编辑字段（1是）
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS, jdbcType = JdbcType.VARCHAR)
    private String isEdit;

    /**
     * 是否列表字段（1是）
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS, jdbcType = JdbcType.VARCHAR)
    private String isList;

    /**
     * 是否查询字段（1是）
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS, jdbcType = JdbcType.VARCHAR)
    private String isQuery;

    /**
     * 查询方式（EQ等于、NE不等于、GT大于、LT小于、LIKE模糊、BETWEEN范围）
     */
    private String queryType;

    /**
     * 显示类型（input文本框、textarea文本域、select下拉框、checkbox复选框、radio单选框、datetime日期控件、image图片上传控件、upload文件上传控件、editor富文本控件）
     */
    private String htmlType;

    /**
     * 字典类型
     */
    private String dictType;

    /**
     * 默认值
     */
    private String columnDefault;

    /**
     * 排序
     */
    private Integer sort;

    public String getCapJavaField() {
        return StringUtils.capitalize(javaField);
    }

    public boolean isPk() {
        return isPk(this.isPk);
    }

    public boolean isPk(String isPk) {
        return isPk != null && StringUtils.equals("1", isPk);
    }

    public boolean isIncrement() {
        return isIncrement(this.isIncrement);
    }

    public boolean isIncrement(String isIncrement) {
        return isIncrement != null && StringUtils.equals("1", isIncrement);
    }

    public boolean isRequired() {
        return isRequired(this.isRequired);
    }

    public boolean isRequired(String isRequired) {
        return isRequired != null && StringUtils.equals("1", isRequired);
    }

    public boolean isInsert() {
        return isInsert(this.isInsert);
    }

    public boolean isInsert(String isInsert) {
        return isInsert != null && StringUtils.equals("1", isInsert);
    }

    public boolean isEdit() {
        return isEdit(this.isEdit);
    }

    public boolean isEdit(String isEdit) {
        return isEdit != null && StringUtils.equals("1", isEdit);
    }

    public boolean isList() {
        return isList(this.isList);
    }

    public boolean isList(String isList) {
        return isList != null && StringUtils.equals("1", isList);
    }

    public boolean isQuery() {
        return isQuery(this.isQuery);
    }

    public boolean isQuery(String isQuery) {
        return isQuery != null && StringUtils.equals("1", isQuery);
    }

    public boolean isSuperColumn() {
        return isSuperColumn(this.javaField);
    }

    public static boolean isSuperColumn(String javaField) {
        return StringUtils.equalsAnyIgnoreCase(javaField,
            // BaseEntity
            "createBy", "createTime", "updateBy", "updateTime",
            // TreeEntity
            "parentName", "parentId");
    }

    public boolean isUsableColumn() {
        return isUsableColumn(javaField);
    }

    public static boolean isUsableColumn(String javaField) {
        // isSuperColumn()中的名单用于避免生成多余Domain属性，若某些属性在生成页面时需要用到不能忽略，则放在此处白名单
        return StringUtils.equalsAnyIgnoreCase(javaField, "parentId", "orderNum", "remark");
    }

    /**
     * 解析字段注释中的字典转换表达式
     *
     * <p>从字段注释的中文括号（）或英文括号()中提取字典映射关系</p>
     * <p>仅当括号内容符合字典格式时才生成表达式，否则返回空字符串</p>
     *
     * <p>字典格式要求：空格分隔的多个条目，每个条目以数字开头</p>
     * <ul>
     *   <li>✅ 状态（0停用 1正常） → 0=停用,1=正常</li>
     *   <li>✅ 性别（0男 1女 2未知） → 0=男,1=女,2=未知</li>
     *   <li>❌ 订单ID（雪花ID） → 空（非字典格式）</li>
     *   <li>❌ 客户ID（关联 crm_customer.cus_id） → 空（非字典格式）</li>
     * </ul>
     */
    public String readConverterExp() {
        // 优先匹配中文括号，再匹配英文括号
        String remarks = StringUtils.substringBetween(this.columnComment, "（", "）");
        if (StringUtils.isEmpty(remarks)) {
            remarks = StringUtils.substringBetween(this.columnComment, "(", ")");
        }
        if (StringUtils.isEmpty(remarks)) {
            return "";
        }
        // 校验是否为字典格式：每个空格分隔的条目都必须以数字开头且长度>=2
        String[] entries = remarks.split(" ");
        for (String entry : entries) {
            if (StringUtils.isEmpty(entry)) {
                continue;
            }
            if (entry.length() < 2 || !Character.isDigit(entry.charAt(0))) {
                // 非字典格式（如"雪花ID"、"关联 crm_customer.cus_id"），跳过
                return "";
            }
        }
        // 解析字典表达式
        StringBuilder sb = new StringBuilder();
        for (String entry : entries) {
            if (StringUtils.isNotEmpty(entry)) {
                String key = entry.substring(0, 1);
                String value = entry.substring(1);
                sb.append(key).append("=").append(value).append(StringUtils.SEPARATOR);
            }
        }
        return sb.deleteCharAt(sb.length() - 1).toString();
    }

    /**
     * 获取字段长度
     * 从 columnType 中解析长度值，如 varchar(100) -> 100
     *
     * @return 字段长度，如果无法解析则返回 null
     */
    public Integer getColumnLength() {
        if (StringUtils.isBlank(this.columnType)) {
            return null;
        }
        // 匹配括号中的数字，如 varchar(100), int(11), decimal(10,2)
        String length = StringUtils.substringBetween(this.columnType, "(", ")");
        if (StringUtils.isBlank(length)) {
            return null;
        }
        // 处理 decimal(10,2) 这种情况，取第一个数字
        if (length.contains(",")) {
            length = length.split(",")[0];
        }
        try {
            return Integer.parseInt(length.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
