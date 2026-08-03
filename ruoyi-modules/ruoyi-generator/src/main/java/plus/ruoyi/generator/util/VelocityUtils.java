package plus.ruoyi.generator.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ObjectUtil;
import plus.ruoyi.generator.config.GenConfig;
import plus.ruoyi.generator.constant.GenConstants;
import plus.ruoyi.common.core.utils.DateUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.json.utils.JsonUtils;
import plus.ruoyi.common.mybatis.enums.DataBaseType;
import plus.ruoyi.common.mybatis.helper.DataBaseHelper;
import plus.ruoyi.generator.domain.GenTable;
import plus.ruoyi.generator.domain.GenTableColumn;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.velocity.VelocityContext;
import plus.ruoyi.common.core.service.DictService;
import plus.ruoyi.common.core.domain.dto.DictTypeDTO;
import plus.ruoyi.common.core.config.properties.AppProperties;

import java.util.*;

/**
 * 模板处理工具类
 * 负责为Velocity模板引擎准备上下文变量，处理代码生成相关的配置
 *
 * @author ruoyi
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VelocityUtils {

    /**
     * 项目空间路径
     */
    private static final String PROJECT_PATH = "main/java";

    /**
     * mybatis空间路径
     */
    private static final String MYBATIS_PATH = "main/resources/mapper";

    /**
     * 默认上级菜单，系统工具
     */
    private static final String DEFAULT_PARENT_MENU_ID = "3";

    /**
     * 设置模板变量信息
     * 构建Velocity模板引擎所需的上下文变量
     *
     * @param genTable 生成表信息
     * @return Velocity上下文对象
     */
    public static VelocityContext prepareContext(GenTable genTable) {
        // 获取基本配置信息
        String moduleName = genTable.getModuleName();
        String businessName = genTable.getBusinessName();
        String packageName = genTable.getPackageName();
        String tplCategory = genTable.getTplCategory();
        String functionName = genTable.getFunctionName();

        // 创建Velocity上下文并设置基础变量
        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("tplCategory", genTable.getTplCategory());
        velocityContext.put("tableName", genTable.getTableName());
        velocityContext.put("functionName", StringUtils.isNotEmpty(functionName) ? functionName : "【请填写功能名称】");
        velocityContext.put("ClassName", genTable.getClassName());
        velocityContext.put("className", StringUtils.uncapitalize(genTable.getClassName()));
        velocityContext.put("moduleName", genTable.getModuleName());
        velocityContext.put("BusinessName", StringUtils.capitalize(genTable.getBusinessName()));
        velocityContext.put("businessName", genTable.getBusinessName());
        velocityContext.put("basePackage", getPackagePrefix(packageName));
        velocityContext.put("packageName", packageName);
        velocityContext.put("author", genTable.getFunctionAuthor());
        velocityContext.put("datetime", DateUtils.getDate());
        velocityContext.put("pkColumn", genTable.getPkColumn());
        velocityContext.put("importList", getImportList(genTable));
        velocityContext.put("permissionPrefix", getPermissionPrefix(moduleName, businessName));
        velocityContext.put("frontendPath", getFrontendPath(packageName, moduleName));
        velocityContext.put("componentPath", getComponentPath(packageName, moduleName, businessName, tplCategory));
        velocityContext.put("columns", genTable.getColumns());
        velocityContext.put("table", genTable);
        velocityContext.put("dicts", getDicts(genTable));
        velocityContext.put("dictComments", getDictComments(genTable));

        // 添加复数后缀变量
        velocityContext.put("pluralSuffix", getPluralSuffix(businessName));

        // 推断并设置显示字段
        velocityContext.put("displayField", inferDisplayField(genTable));

        // 设置菜单相关变量
        setMenuVelocityContext(velocityContext, genTable);
        if (GenConstants.TPL_TREE.equals(tplCategory)) {
            // 根据模板类型设置特定变量
            setTreeVelocityContext(velocityContext, genTable);
        } else if (GenConstants.TPL_SUB.equals(tplCategory)) {
            setSubVelocityContext(velocityContext, genTable);
        }
        // 设置父表相关变量（如果当前表是子表）
        setParentVelocityContext(velocityContext, genTable);
        return velocityContext;
    }

    /**
     * 设置菜单相关的模板变量
     *
     * @param context  Velocity上下文
     * @param genTable 生成表信息
     */
    public static void setMenuVelocityContext(VelocityContext context, GenTable genTable) {
        String options = genTable.getOptions();
        Dict paramsObj = JsonUtils.parseMap(options);
        String parentMenuId = getParentMenuId(paramsObj);
        context.put("parentMenuId", parentMenuId);
    }

    /**
     * 设置树形结构相关的模板变量
     *
     * @param context  Velocity上下文
     * @param genTable 生成表信息
     */
    public static void setTreeVelocityContext(VelocityContext context, GenTable genTable) {
        String options = genTable.getOptions();
        Dict paramsObj = JsonUtils.parseMap(options);

        // 获取树形结构的关键字段
        String treeCode = getTreecode(paramsObj);
        String treeParentCode = getTreeParentCode(paramsObj);
        String treeName = getTreeName(paramsObj);

        context.put("treeCode", treeCode);
        context.put("treeParentCode", treeParentCode);
        context.put("treeName", treeName);
        context.put("expandColumn", getExpandColumn(genTable));

        // 设置原始字段名（用于某些特殊场景）
        if (paramsObj.containsKey(GenConstants.TREE_PARENT_CODE)) {
            context.put("tree_parent_code", paramsObj.get(GenConstants.TREE_PARENT_CODE));
        }
        if (paramsObj.containsKey(GenConstants.TREE_NAME)) {
            context.put("tree_name", paramsObj.get(GenConstants.TREE_NAME));
        }
    }

    /**
     * 设置主子表相关的模板变量
     *
     * @param context  Velocity上下文
     * @param genTable 生成表信息（主表）
     */
    public static void setSubVelocityContext(VelocityContext context, GenTable genTable) {
        GenTable subTable = genTable.getSubTable();
        String subTableName = genTable.getSubTableName();
        String subTableFkName = genTable.getSubTableFkName();
        String subClassName = subTable.getClassName();
        String subTableFkClassName = StringUtils.underscoreToPascalCase(subTableFkName);
        String subBusinessName = subTable.getBusinessName();

        // 设置子表相关变量
        context.put("subTable", subTable);
        context.put("subTableName", subTableName);
        context.put("subTableFkName", subTableFkName);
        context.put("subTableFkClassName", subTableFkClassName);
        context.put("subTableFkclassName", StringUtils.uncapitalize(subTableFkClassName));
        context.put("subClassName", subClassName);
        context.put("subclassName", StringUtils.uncapitalize(subClassName));
        context.put("subBusinessName", StringUtils.capitalize(subBusinessName));
        context.put("subbusinessName", subBusinessName);
        context.put("subImportList", getImportList(subTable));
        context.put("subDicts", getDicts(subTable));
        context.put("subFrontendPath", getFrontendPath(subTable.getPackageName(), subTable.getModuleName()));

        // 子表的复数后缀变量
        context.put("subPluralSuffix", getPluralSuffix(subBusinessName));
    }

    /**
     * 设置父表上下文信息
     * 用于处理当前表作为子表时的相关变量
     *
     * @param context  Velocity上下文
     * @param genTable 生成表信息
     */
    public static void setParentVelocityContext(VelocityContext context, GenTable genTable) {
        context.put("isSubTable", genTable.getIsSubTable());

        if (genTable.getIsSubTable()) {
            GenTable parentTable = genTable.getParentTable();
            context.put("parentTable", parentTable);

            // 设置父表外键相关的变量名
            context.put("parentTableSubTableFkclassName", StringUtils.underscoreToCamelCase(parentTable.getSubTableFkName()));
            context.put("parentTableSubTableFkClassName", StringUtils.capitalize(StringUtils.underscoreToCamelCase(parentTable.getSubTableFkName())));

            // 查找外键列的Java类型和注释
            String fkJavaType = "Long";
            String fkComment = "";
            for (GenTableColumn column : genTable.getColumns()) {
                if (column.getColumnName().equals(parentTable.getSubTableFkName())) {
                    fkJavaType = column.getJavaType();
                    fkComment = column.getColumnComment();
                    break;
                }
            }
            context.put("parentTableSubTableFkJavaType", fkJavaType);
            context.put("parentTableSubTableFkComment", fkComment);

            // 设置父表的前端路径和业务名称相关变量
            String parentBusinessName = parentTable.getBusinessName();
            String parentClassName = parentTable.getClassName();
            context.put("parentBusinessName", parentBusinessName);
            context.put("parentclassName", StringUtils.uncapitalize(parentClassName));
            context.put("ParentBusinessName", StringUtils.capitalize(parentBusinessName));
            context.put("ParentClassName", parentClassName);

            // 设置父表的前端路径
            context.put("parentFrontendPath", getFrontendPath(parentTable.getPackageName(), parentTable.getModuleName()));

            // 设置父表的复数后缀
            context.put("parentPluralSuffix", getPluralSuffix(parentBusinessName));
        }
    }

    /**
     * 获取模板文件列表
     * 根据模板类型返回需要生成的模板文件路径
     *
     * @param tplCategory 模板类型
     * @return 模板文件路径列表
     */
    public static List<String> getTemplateList(String tplCategory, String dsName) {
        List<String> templates = new ArrayList<>();

        // 基础模板（所有类型都需要）
        templates.add("vm/java/domain.java.vm");
        templates.add("vm/java/bo.java.vm");
        templates.add("vm/java/vo.java.vm");
        templates.add("vm/java/controller.java.vm");
        templates.add("vm/java/service.java.vm");
        templates.add("vm/java/serviceImpl.java.vm");
        templates.add("vm/java/dao.java.vm");
        templates.add("vm/java/daoImpl.java.vm");
        templates.add("vm/java/mapper.java.vm");
        templates.add("vm/xml/mapper.xml.vm");

        // 根据指定数据源的数据库类型选择SQL模板
        DataBaseType dataBaseType = DataBaseHelper.getDataBaseType(dsName);
        if (dataBaseType.isOracle()) {
            templates.add("vm/sql/oracle/sql.vm");
        } else if (dataBaseType.isPostgreSql()) {
            templates.add("vm/sql/postgres/sql.vm");
        } else if (dataBaseType.isSqlServer()) {
            templates.add("vm/sql/sqlserver/sql.vm");
        } else {
            templates.add("vm/sql/sql.vm");
        }

        // TypeScript相关模板
        templates.add("vm/ts/types.ts.vm");
        templates.add("vm/ts/api.ts.vm");

        // 根据模板类型添加前端页面模板
        if (GenConstants.TPL_CRUD.equals(tplCategory)) {
            templates.add("vm/vue/index.vue.vm");
        } else if (GenConstants.TPL_TREE.equals(tplCategory)) {
            templates.add("vm/vue/index-tree.vue.vm");
        } else if (GenConstants.TPL_SUB.equals(tplCategory)) {
            templates.add("vm/vue/index.vue.vm");
            //子表
            templates.add("vm/vue/child.vue.vm");
        }
        return templates;
    }

    /**
     * 获取生成文件的完整路径和文件名
     *
     * @param template 模板文件路径
     * @param genTable 生成表信息
     * @return 生成文件的完整路径
     */
    public static String getFileName(String template, GenTable genTable) {
        // 文件名称
        String fileName = "";
        // 包路径
        String packageName = genTable.getPackageName();
        // 模块名
        String moduleName = genTable.getModuleName();
        // 大写类名
        String className = genTable.getClassName();
        // 业务名称
        String businessName = genTable.getBusinessName();

        // 构建Java文件路径
        String javaPath = PROJECT_PATH + "/" + StringUtils.replace(packageName, ".", "/");
        // 构建MyBatis映射文件路径
        String mybatisPath = MYBATIS_PATH + "/" + moduleName;
        // 前端文件路径 - 根据包名生成层次结构
        String vuePath = "src";
        String frontendPath = getFrontendPath(packageName, moduleName);

        // 根据模板类型生成对应的文件路径
        if (template.contains("domain.java.vm")) {
            fileName = StringUtils.format("{}/domain/{}.java", javaPath, className);
        }
        if (template.contains("vo.java.vm")) {
            fileName = StringUtils.format("{}/domain/vo/{}Vo.java", javaPath, className);
        }
        if (template.contains("bo.java.vm")) {
            fileName = StringUtils.format("{}/domain/bo/{}Bo.java", javaPath, className);
        }
        if (template.contains("controller.java.vm")) {
            fileName = StringUtils.format("{}/controller/{}Controller.java", javaPath, className);
        } else if (template.contains("service.java.vm")) {
            fileName = StringUtils.format("{}/service/I{}Service.java", javaPath, className);
        } else if (template.contains("serviceImpl.java.vm")) {
            fileName = StringUtils.format("{}/service/impl/{}ServiceImpl.java", javaPath, className);
        } else if (template.contains("dao.java.vm")) {
            fileName = StringUtils.format("{}/dao/I{}Dao.java", javaPath, className);
        } else if (template.contains("daoImpl.java.vm")) {
            fileName = StringUtils.format("{}/dao/impl/{}DaoImpl.java", javaPath, className);
        } else if (template.contains("mapper.java.vm")) {
            fileName = StringUtils.format("{}/mapper/{}Mapper.java", javaPath, className);
        } else if (template.contains("mapper.xml.vm")) {
            fileName = StringUtils.format("{}/{}Mapper.xml", mybatisPath, className);
        } else if (template.contains("sql.vm")) {
            // SQL文件命名规则: {moduleName}_{subModule}_{businessName}_menu.sql
            fileName = getSqlFileName(genTable);
        } else if (template.contains("api.ts.vm")) {
            fileName = StringUtils.format("{}/api/{}/{}/{}Api.ts", vuePath, frontendPath, businessName, businessName);
        } else if (template.contains("types.ts.vm")) {
            fileName = StringUtils.format("{}/api/{}/{}/{}Types.ts", vuePath, frontendPath, businessName, businessName);
        } else if (template.contains("index.vue.vm")) {
            fileName = StringUtils.format("{}/views/{}/{}/{}.vue", vuePath, frontendPath, businessName, businessName);
        } else if (template.contains("index-tree.vue.vm")) {
            fileName = StringUtils.format("{}/views/{}/{}/{}Tree.vue", vuePath, frontendPath, businessName, businessName);
        } else if (template.contains("child.vue.vm")) {
            // 子表页面使用子表的类名
            fileName = StringUtils.format("{}/views/{}/{}/{}Child.vue", vuePath, frontendPath, businessName, genTable.getSubTable().getClassName());
        }
        return fileName;
    }

    /**
     * 获取代码预览用的完整文件路径
     * 包含项目根目录和模块路径的完整层级结构
     * 对于 Java 文件,将包路径部分合并显示 (如 plus.ruoyi.business.base)
     *
     * @param template 模板文件路径
     * @param genTable 生成表信息
     * @return 预览用的完整文件路径
     */
    public static String getPreviewFileName(String template, GenTable genTable) {
        // 获取基础相对路径
        String relativeFileName = getFileName(template, genTable);

        // 通过 SpringUtils 获取 AppProperties
        AppProperties appProperties = SpringUtils.getBean(AppProperties.class);
        String appId = appProperties.getId();

        // 从表级配置获取模块名称和前端目录（自动fallback到全局配置）
        String backendModuleName = genTable.getBackendModuleName();
        String frontendRootDir = genTable.getFrontendRootDir();

        // 根据文件类型构建完整路径
        if (template.contains(".java.vm") || template.contains(".xml.vm")) {
            // 后端文件: 需要优化包路径显示
            // relativeFileName 格式: main/java/plus/ruoyi/business/base/controller/AdController.java

            // 提取包路径和文件路径
            String packageName = genTable.getPackageName(); // 如: plus.ruoyi.business.base
            String packagePath = StringUtils.replace(packageName, ".", "/"); // 转为: plus/ruoyi/business/base

            // 找到包路径在 relativeFileName 中的位置
            int packageIndex = relativeFileName.indexOf(packagePath);
            if (packageIndex != -1) {
                // 分割为: 前缀部分 + 包路径 + 后缀部分
                String prefix = relativeFileName.substring(0, packageIndex); // main/java/
                String suffix = relativeFileName.substring(packageIndex + packagePath.length()); // /controller/AdController.java

                // 重新组合,使用点号形式的包名
                relativeFileName = prefix + packageName + suffix;
            }

            // 后端文件: appId/ruoyi-modules/ruoyi-business/src/...
            return StringUtils.format("{}/ruoyi-modules/{}/src/{}", appId, backendModuleName, relativeFileName);
        } else if (template.contains(".ts.vm") || template.contains(".vue.vm")) {
            // 前端文件: appId/plus-ui/src/...
            return StringUtils.format("{}/{}/{}", appId, frontendRootDir, relativeFileName);
        } else if (template.contains("sql.vm")) {
            // SQL文件: appId/script/menu/xxx_menu.sql
            return StringUtils.format("{}/script/menu/{}", appId, relativeFileName);
        }

        // 默认返回相对路径
        return relativeFileName;
    }

    /**
     * 获取复数后缀
     * 如果业务名称已经以s结尾，则返回空字符串，否则返回"s"
     *
     * @param businessName 业务名称
     * @return 复数后缀，"s" 或空字符串
     */
    private static String getPluralSuffix(String businessName) {
        if (StringUtils.isEmpty(businessName)) {
            return "s";
        }

        // 如果业务名称以s结尾（不区分大小写），则不添加s
        return businessName.toLowerCase().endsWith("s") ? "" : "s";
    }

    /**
     * 根据包名生成前端文件的目录路径
     * 跳过前两级域名（如plus.ruoyi, com.ruoyi等），将剩余部分转换为目录路径
     *
     * @param packageName 完整包名，如：plus.ruoyi.business.base
     * @param moduleName  模块名，如：base
     * @return 前端目录路径，如：business/base
     */
    private static String getFrontendPath(String packageName, String moduleName) {
        if (StringUtils.isEmpty(packageName)) {
            return moduleName;
        }
        List<String> parts = StringUtils.splitToList(packageName, ".");
        return parts.size() > 2
            ? String.join("/", parts.subList(2, parts.size()))
            : moduleName;
    }

    /**
     * 获取前端组件路径
     * 根据模板类型决定是否添加特殊后缀
     *
     * @param packageName  包名
     * @param moduleName   模块名
     * @param businessName 业务名
     * @param tplCategory  模板类型
     * @return 前端组件路径
     */
    public static String getComponentPath(String packageName, String moduleName, String businessName, String tplCategory) {
        String frontendPath = getFrontendPath(packageName, moduleName);
        String basePath = frontendPath + "/" + businessName + "/" + businessName;

        // 如果是树表模板，添加Tree后缀
        if (GenConstants.TPL_TREE.equals(tplCategory)) {
            return basePath + "Tree";
        }

        return basePath;
    }

    /**
     * 获取包前缀
     * 从完整包名中提取基础包路径
     *
     * @param packageName 完整包名
     * @return 基础包路径
     */
    public static String getPackagePrefix(String packageName) {
        int lastIndex = packageName.lastIndexOf(".");
        return StringUtils.substring(packageName, 0, lastIndex);
    }

    /**
     * 根据列类型获取需要导入的包列表
     *
     * @param genTable 业务表对象
     * @return 需要导入的包列表
     */
    public static HashSet<String> getImportList(GenTable genTable) {
        List<GenTableColumn> columns = genTable.getColumns();
        HashSet<String> importList = new HashSet<>();

        for (GenTableColumn column : columns) {
            // 日期类型需要导入Date和JsonFormat
            if (GenConstants.TYPE_DATE.equals(column.getJavaType())) {
                importList.add("java.util.Date");
                importList.add("com.fasterxml.jackson.annotation.JsonFormat");
            } else if (GenConstants.TYPE_BIGDECIMAL.equals(column.getJavaType())) {
                importList.add("java.math.BigDecimal");
            }
        }
        return importList;
    }

    /**
     * 获取表中所有字典类型
     * 用于前端页面的字典数据加载
     *
     * @param genTable 业务表对象
     * @return 字典类型字符串，用逗号分隔
     */
    public static String getDicts(GenTable genTable) {
        List<GenTableColumn> columns = genTable.getColumns();
        Set<String> dicts = new HashSet<>();

        // 添加主表的字典
        addDicts(dicts, columns);

        // 如果有子表，也添加子表的字典
        if (ObjectUtil.isNotNull(genTable.getSubTable())) {
            List<GenTableColumn> subColumns = genTable.getSubTable().getColumns();
            addDicts(dicts, subColumns);
        }
        return StringUtils.join(dicts, ", ");
    }

    /**
     * 添加字典列表
     * 从列集合中提取字典类型配置
     *
     * @param dicts   字典集合
     * @param columns 列集合
     */
    public static void addDicts(Set<String> dicts, List<GenTableColumn> columns) {
        for (GenTableColumn column : columns) {
            // 非系统字段且配置了字典类型，且HTML类型为下拉框、单选或复选框
            if (!column.isSuperColumn() && StringUtils.isNotEmpty(column.getDictType()) && StringUtils.equalsAny(
                column.getHtmlType(),
                new String[]{GenConstants.HTML_SELECT, GenConstants.HTML_RADIO, GenConstants.HTML_CHECKBOX})) {
                dicts.add("'" + column.getDictType() + "'");
            }
        }
    }

    /**
     * 获取字典注释映射
     * 通过 DictService 查询字典类型,获取字典类型对应的字典名称(注释)
     *
     * @param genTable 业务表对象
     * @return 字典类型到注释的映射 Map<字典类型, 字典名称>
     */
    public static Map<String, String> getDictComments(GenTable genTable) {
        List<GenTableColumn> columns = genTable.getColumns();
        Set<String> dictTypes = new HashSet<>();

        // 收集当前表的所有字典类型(不带引号)
        for (GenTableColumn column : columns) {
            if (!column.isSuperColumn() && StringUtils.isNotEmpty(column.getDictType()) && StringUtils.equalsAny(
                column.getHtmlType(),
                new String[]{GenConstants.HTML_SELECT, GenConstants.HTML_RADIO, GenConstants.HTML_CHECKBOX})) {
                dictTypes.add(column.getDictType());
            }
        }

        // 如果没有字典类型,返回空Map
        if (dictTypes.isEmpty()) {
            return new HashMap<>();
        }

        // 通过 DictService 查询字典类型获取注释
        Map<String, String> dictComments = new HashMap<>();
        try {
            DictService dictService = SpringUtils.getBean(DictService.class);
            for (String dictType : dictTypes) {
                DictTypeDTO dictTypeDTO = dictService.getDictType(dictType);
                if (ObjectUtil.isNotNull(dictTypeDTO) && StringUtils.isNotEmpty(dictTypeDTO.getDictName())) {
                    dictComments.put(dictType, dictTypeDTO.getDictName());
                }
            }
        } catch (Exception e) {
            // 如果查询失败,返回空Map(避免影响代码生成)
            return new HashMap<>();
        }

        return dictComments;
    }

    /**
     * 获取权限前缀
     * 用于生成权限标识符
     *
     * @param moduleName   模块名称
     * @param businessName 业务名称
     * @return 权限前缀，格式：模块名:业务名
     */
    public static String getPermissionPrefix(String moduleName, String businessName) {
        return StringUtils.format("{}:{}", moduleName, businessName);
    }

    /**
     * 获取上级菜单ID
     * 从配置参数中获取，如果未配置则使用默认值
     *
     * @param paramsObj 配置参数对象
     * @return 上级菜单ID
     */
    public static String getParentMenuId(Dict paramsObj) {
        if (CollUtil.isNotEmpty(paramsObj) && paramsObj.containsKey(GenConstants.PARENT_MENU_ID)
            && StringUtils.isNotEmpty(paramsObj.getStr(GenConstants.PARENT_MENU_ID))) {
            return paramsObj.getStr(GenConstants.PARENT_MENU_ID);
        }
        return DEFAULT_PARENT_MENU_ID;
    }

    /**
     * 获取树编码字段
     * 将下划线命名转换为驼峰命名
     *
     * @param paramsObj 配置参数对象
     * @return 树编码字段（驼峰命名）
     */
    public static String getTreecode(Map<String, Object> paramsObj) {
        if (CollUtil.isNotEmpty(paramsObj) && paramsObj.containsKey(GenConstants.TREE_CODE)) {
            return StringUtils.underscoreToCamelCase(Convert.toStr(paramsObj.get(GenConstants.TREE_CODE)));
        }
        return StringUtils.EMPTY;
    }

    /**
     * 获取树父编码字段
     * 将下划线命名转换为驼峰命名
     *
     * @param paramsObj 配置参数对象
     * @return 树父编码字段（驼峰命名）
     */
    public static String getTreeParentCode(Dict paramsObj) {
        if (CollUtil.isNotEmpty(paramsObj) && paramsObj.containsKey(GenConstants.TREE_PARENT_CODE)) {
            return StringUtils.underscoreToCamelCase(paramsObj.getStr(GenConstants.TREE_PARENT_CODE));
        }
        return StringUtils.EMPTY;
    }

    /**
     * 获取树名称字段
     * 将下划线命名转换为驼峰命名
     *
     * @param paramsObj 配置参数对象
     * @return 树名称字段（驼峰命名）
     */
    public static String getTreeName(Dict paramsObj) {
        if (CollUtil.isNotEmpty(paramsObj) && paramsObj.containsKey(GenConstants.TREE_NAME)) {
            return StringUtils.underscoreToCamelCase(paramsObj.getStr(GenConstants.TREE_NAME));
        }
        return StringUtils.EMPTY;
    }

    /**
     * 获取树形表格中展开按钮显示的列序号
     * 计算树名称字段在可显示列中的位置
     *
     * @param genTable 业务表对象
     * @return 展开按钮列序号（从1开始）
     */
    public static int getExpandColumn(GenTable genTable) {
        String options = genTable.getOptions();
        Dict paramsObj = JsonUtils.parseMap(options);
        String treeName = paramsObj.getStr(GenConstants.TREE_NAME);
        int num = 0;
        // 遍历所有需要在列表中显示的列
        for (GenTableColumn column : genTable.getColumns()) {
            if (column.isList()) {
                num++;
                String columnName = column.getColumnName();
                if (columnName.equals(treeName)) {
                    break;
                }
            }
        }
        return num;
    }

    /**
     * 获取SQL菜单文件名
     * <p>格式: {moduleName}_{subModule}_{businessName}_menu.sql</p>
     * <p>示例: plus.ruoyi.business.base + ad → business_base_ad_menu.sql</p>
     *
     * @param genTable 生成表信息
     * @return SQL文件名
     */
    public static String getSqlFileName(GenTable genTable) {
        String packageName = genTable.getPackageName();  // 如: plus.ruoyi.business.base
        String businessName = genTable.getBusinessName(); // 如: ad

        List<String> nameParts = new ArrayList<>();

        // 从包名提取模块信息
        if (StringUtils.isNotEmpty(packageName)) {
            String[] parts = packageName.split("\\.");

            // 跳过前两级域名 (plus.ruoyi)，提取剩余部分
            if (parts.length > 2) {
                for (int i = 2; i < parts.length; i++) {
                    nameParts.add(parts[i]);  // 例如: [business, base]
                }
            }
        }

        // 添加业务名
        nameParts.add(businessName);  // 例如: [business, base, ad]

        // 添加后缀
        nameParts.add("menu");  // 例如: [business, base, ad, menu]

        // 用下划线连接
        return String.join("_", nameParts) + ".sql";
        // 最终结果: business_base_ad_menu.sql
    }

    /**
     * 推断显示字段
     * 按优先级查找合适的字符串类型显示字段: name → title → userName → nickName → no → code → 第二个非ID字符串字段
     *
     * @param genTable 生成表信息
     * @return 显示字段名(首字母大写的getter方法形式)，如 "Name"、"Title" 等
     */
    public static String inferDisplayField(GenTable genTable) {
        List<GenTableColumn> columns = genTable.getColumns();

        // 优先级字段列表
        String[] priorityFields = {"name", "title", "userName", "nickName", "no", "code"};

        // 按优先级查找(只选择字符串类型)
        for (String field : priorityFields) {
            for (GenTableColumn column : columns) {
                String javaField = column.getJavaField();
                String javaType = column.getJavaType();
                // 不区分大小写匹配（兼容 Name、name、NAME 等），且必须是String类型
                if (field.equalsIgnoreCase(javaField) && !column.isPk() && "String".equals(javaType)) {
                    // 返回首字母大写的字段名
                    return StringUtils.capitalize(javaField);
                }
            }
        }

        // 如果没有找到优先级字段，使用第二个非主键字符串字段
        int nonPkStringCount = 0;
        for (GenTableColumn column : columns) {
            if (!column.isPk() && !column.getJavaField().equals("id") && "String".equals(column.getJavaType())) {
                nonPkStringCount++;
                if (nonPkStringCount == 2) {
                    return StringUtils.capitalize(column.getJavaField());
                }
            }
        }

        // 如果还是找不到，使用第一个非主键字符串字段
        for (GenTableColumn column : columns) {
            if (!column.isPk() && "String".equals(column.getJavaType())) {
                return StringUtils.capitalize(column.getJavaField());
            }
        }

        // 兜底：返回 "Id"
        return "Id";
    }
}
