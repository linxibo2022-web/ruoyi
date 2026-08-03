package plus.ruoyi.generator.domain;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import cn.hutool.core.lang.Dict;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.json.utils.JsonUtils;
import plus.ruoyi.common.mybatis.core.domain.BaseEntity;
import plus.ruoyi.generator.config.GenConfig;
import plus.ruoyi.generator.constant.GenConstants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 代码生成业务表实体类
 *
 * <p>用于存储代码生成器的表配置信息，包括：</p>
 * <ul>
 *   <li>数据库表基本信息（表名、注释等）</li>
 *   <li>代码生成配置（包名、模块名、作者等）</li>
 *   <li>模板选择（单表、树表、主子表）</li>
 *   <li>关联表配置（用于主子表场景）</li>
 *   <li>树形结构配置（用于树表场景）</li>
 * </ul>
 *
 * <p>对应数据库表：sys_gen_table</p>
 *
 * @author Lion Li
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_gen_table")
public class GenTable extends BaseEntity {

    // ========== 基础信息字段 ==========

    /**
     * 表主键ID
     * <p>代码生成表的唯一标识</p>
     */
    @TableId(value = "table_id")
    private Long tableId;

    /**
     * 数据源名称
     * <p>指定代码生成使用的数据源，支持多数据源场景</p>
     */
    @NotBlank(message = "数据源名称不能为空")
    private String dataName;

    /**
     * 数据库表名
     * <p>要生成代码的目标数据库表名</p>
     */
    @NotBlank(message = "表名称不能为空")
    private String tableName;

    /**
     * 表注释描述
     * <p>数据库表的注释信息，用于生成代码注释和文档</p>
     */
    @NotBlank(message = "表描述不能为空")
    private String tableComment;

    // ========== 主子表关联配置 ==========

    /**
     * 子表表名
     * <p>当模板类型为主子表(sub)时，指定关联的子表名称</p>
     */
    private String subTableName;

    /**
     * 子表外键字段名
     * <p>子表中关联主表的外键字段名称</p>
     */
    private String subTableFkName;

    // ========== 代码生成配置 ==========

    /**
     * Java实体类名
     * <p>生成的Java实体类名称，遵循帕斯卡命名法（首字母大写）</p>
     */
    @NotBlank(message = "实体类名称不能为空")
    private String className;

    /**
     * 模板类型
     * <p>选择代码生成使用的模板：</p>
     * <ul>
     *   <li>crud - 单表操作模板</li>
     *   <li>tree - 树表操作模板</li>
     *   <li>sub - 主子表操作模板</li>
     * </ul>
     */
    private String tplCategory;

    /**
     * 生成代码的包路径
     * <p>指定生成代码的Java包路径，如：com.example.system</p>
     */
    @NotBlank(message = "生成包路径不能为空")
    private String packageName;

    /**
     * 生成模块名
     * <p>用于区分不同功能模块，影响生成的目录结构和包名</p>
     */
    @NotBlank(message = "生成模块名不能为空")
    private String moduleName;

    /**
     * 业务名称
     * <p>用于生成类名、方法名、URL路径等，通常为表名去除前缀后的驼峰形式</p>
     */
    @NotBlank(message = "生成业务名不能为空")
    private String businessName;

    /**
     * 功能名称
     * <p>用于生成代码注释和页面标题，通常为表注释的简化形式</p>
     */
    @NotBlank(message = "生成功能名不能为空")
    private String functionName;

    /**
     * 代码作者
     * <p>生成代码的作者信息，用于类注释中的@author标签</p>
     */
    @NotBlank(message = "作者不能为空")
    private String functionAuthor;

    // ========== 生成方式配置 ==========

    /**
     * 代码生成方式
     * <p>指定代码的输出方式：</p>
     * <ul>
     *   <li>0 - 生成zip压缩包下载</li>
     *   <li>1 - 生成到自定义路径</li>
     * </ul>
     */
    private String genType;

    /**
     * 自定义生成路径
     * <p>当genType为1时，指定代码生成的目标路径；为空时使用项目默认路径</p>
     */
    @TableField(updateStrategy = FieldStrategy.NOT_EMPTY)
    private String genPath;

    // ========== 关联对象（不存储到数据库） ==========

    /**
     * 主键列信息
     * <p>表的主键列配置，用于生成主键相关的代码逻辑</p>
     */
    @TableField(exist = false)
    private GenTableColumn pkColumn;

    /**
     * 关联子表信息
     * <p>当前表作为主表时，关联的子表配置信息</p>
     */
    @TableField(exist = false)
    private GenTable subTable;

    /**
     * 表列配置列表
     * <p>表中所有列的详细配置信息，包括字段类型、显示方式、权限等</p>
     */
    @Valid
    @TableField(exist = false)
    private List<GenTableColumn> columns;

    /**
     * 是否为子表
     */
    @TableField(exist = false)
    private Boolean isSubTable;

    /**
     * 父表信息
     */
    @TableField(exist = false)
    private GenTable parentTable;

    // ========== 其他配置 ==========

    /**
     * 其他生成选项
     * <p>JSON格式存储的额外配置选项</p>
     */
    private String options;

    /**
     * 备注信息
     * <p>用户自定义的备注说明</p>
     */
    private String remark;

    // ========== 树表配置（不存储到数据库） ==========

    /**
     * 树编码字段名
     * <p>树表结构中标识节点编码的字段名，用于构建树形结构</p>
     */
    @TableField(exist = false)
    private String treeCode;

    /**
     * 树父编码字段名
     * <p>树表结构中标识父节点编码的字段名，用于建立父子关系</p>
     */
    @TableField(exist = false)
    private String treeParentCode;

    /**
     * 树名称字段名
     * <p>树表结构中用于显示节点名称的字段名</p>
     */
    @TableField(exist = false)
    private String treeName;

    // ========== 菜单配置（不存储到数据库） ==========

    /**
     * 关联菜单ID列表
     * <p>生成代码时关联的系统菜单ID集合</p>
     */
    @TableField(exist = false)
    private List<Long> menuIds;

    /**
     * 上级菜单ID
     * <p>生成菜单时的父级菜单ID</p>
     */
    @TableField(exist = false)
    private Long parentMenuId;

    /**
     * 上级菜单名称
     * <p>生成菜单时的父级菜单名称</p>
     */
    @TableField(exist = false)
    private String parentMenuName;

    /**
     * 菜单图标
     * <p>生成菜单时使用的图标名称</p>
     */
    @TableField(exist = false)
    private String menuIcon;

    /**
     * 菜单顺序
     * <p>生成菜单时的显示顺序，数值越小越靠前</p>
     */
    @TableField(exist = false)
    private Integer menuOrder;

    /**
     * 是否自动导入菜单
     * <p>生成代码时是否自动导入菜单SQL到数据库（0-否，1-是）</p>
     */
    @TableField(exist = false)
    private String autoImportMenu;

    // ========== 生成路径配置（存储在 options JSON 中） ==========

    /**
     * 后端模块名称
     * <p>代码生成到哪个后端模块（如：ruoyi-business、ruoyi-system、ruoyi-mall）</p>
     */
    @TableField(exist = false)
    private String backendModuleName;

    /**
     * 前端项目根目录
     * <p>代码生成到哪个前端项目（如：plus-ui、plus-uniapp、plus-app）</p>
     */
    @TableField(exist = false)
    private String frontendRootDir;

    // ========== 业务逻辑方法 ==========

    /**
     * 判断当前表是否为主子表模式
     *
     * @return true如果是主子表模式
     */
    public boolean isSub() {
        return isSub(this.tplCategory);
    }

    /**
     * 静态方法：判断指定模板类型是否为主子表模式
     *
     * @param tplCategory 模板类型
     * @return true如果是主子表模式
     */
    public static boolean isSub(String tplCategory) {
        return tplCategory != null && StringUtils.equals(GenConstants.TPL_SUB, tplCategory);
    }

    /**
     * 判断当前表是否为树表模式
     *
     * @return true如果是树表模式
     */
    public boolean isTree() {
        return isTree(this.tplCategory);
    }

    /**
     * 静态方法：判断指定模板类型是否为树表模式
     *
     * @param tplCategory 模板类型
     * @return true如果是树表模式
     */
    public static boolean isTree(String tplCategory) {
        return tplCategory != null && StringUtils.equals(GenConstants.TPL_TREE, tplCategory);
    }

    /**
     * 判断当前表是否为单表CRUD模式
     *
     * @return true如果是单表CRUD模式
     */
    public boolean isCrud() {
        return isCrud(this.tplCategory);
    }

    /**
     * 静态方法：判断指定模板类型是否为单表CRUD模式
     *
     * @param tplCategory 模板类型
     * @return true如果是单表CRUD模式
     */
    public static boolean isCrud(String tplCategory) {
        return tplCategory != null && StringUtils.equals(GenConstants.TPL_CRUD, tplCategory);
    }

    /**
     * 判断指定字段是否为父类字段
     *
     * @param javaField Java字段名
     * @return true如果是父类字段
     */
    public boolean isSuperColumn(String javaField) {
        return isSuperColumn(this.tplCategory, javaField);
    }

    /**
     * 静态方法：判断指定字段是否为Entity基类字段
     * <p>基类字段通常包括创建时间、创建人、更新时间、更新人等公共字段，
     * 这些字段在代码生成时需要特殊处理，不需要在前端表单中显示</p>
     *
     * @param tplCategory 模板类型（此参数目前未使用，保留用于扩展）
     * @param javaField   Java字段名
     * @return true如果是父类字段
     */
    public static boolean isSuperColumn(String tplCategory, String javaField) {
        return GenConstants.BASE_ENTITY.contains(javaField);
    }

    // ========== 自定义 Getter（从 options 解析，无配置则返回全局默认值） ==========

    /**
     * 获取后端模块名称
     * <p>优先级：字段值 > options配置 > 全局默认值</p>
     *
     * @return 后端模块名称
     */
    public String getBackendModuleName() {
        if (StringUtils.isNotBlank(backendModuleName)) {
            return backendModuleName;
        }
        // 尝试从 options 解析
        if (StringUtils.isNotBlank(options)) {
            try {
                Dict paramsObj = JsonUtils.parseMap(options);
                String value = paramsObj.getStr("backendModuleName");
                if (StringUtils.isNotBlank(value)) {
                    return value;
                }
            } catch (Exception ignored) {
            }
        }
        return GenConfig.getBackendModuleName();
    }

    /**
     * 获取前端项目根目录
     * <p>优先级：字段值 > options配置 > 全局默认值</p>
     *
     * @return 前端项目根目录
     */
    public String getFrontendRootDir() {
        if (StringUtils.isNotBlank(frontendRootDir)) {
            return frontendRootDir;
        }
        if (StringUtils.isNotBlank(options)) {
            try {
                Dict paramsObj = JsonUtils.parseMap(options);
                String value = paramsObj.getStr("frontendRootDir");
                if (StringUtils.isNotBlank(value)) {
                    return value;
                }
            } catch (Exception ignored) {
            }
        }
        return GenConfig.getFrontendRootDir();
    }
}
