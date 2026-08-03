package plus.ruoyi.generator.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.anyline.metadata.Column;
import org.anyline.metadata.Table;
import org.anyline.proxy.ServiceProxy;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import plus.ruoyi.common.core.constant.Constants;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.core.utils.StreamUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.core.utils.file.FileUtils;
import plus.ruoyi.common.json.utils.JsonUtils;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.generator.config.GenConfig;
import plus.ruoyi.generator.constant.GenConstants;
import plus.ruoyi.generator.dao.IGenTableColumnDao;
import plus.ruoyi.generator.dao.IGenTableDao;
import plus.ruoyi.generator.domain.GenTable;
import plus.ruoyi.generator.domain.GenTableColumn;
import plus.ruoyi.generator.domain.vo.CodeGenResult;
import plus.ruoyi.generator.util.GenUtils;
import plus.ruoyi.generator.util.VelocityInitializer;
import plus.ruoyi.generator.util.VelocityUtils;
import plus.ruoyi.common.core.service.MenuService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成业务表服务实现类
 * 负责处理代码生成相关的业务逻辑，包括表导入、代码生成、预览等功能
 *
 * @author Lion Li
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class GenTableServiceImpl implements IGenTableService {

    private final IGenTableDao genTableDao;
    private final IGenTableColumnDao genTableColumnDao;
    private final IdentifierGenerator identifierGenerator;
    private final MenuService menuService;
    private final JdbcTemplate jdbcTemplate;

    /**
     * 需要忽略的表前缀
     */
    private static final String[] TABLE_IGNORE = new String[]{"sj_", "flow_", "gen_"};

    /**
     * 分页查询
     *
     * @param genTable  查询参数
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    @Override
    public PageResult<GenTable> page(GenTable genTable, PageQuery pageQuery) {
        PlusLambdaQuery<GenTable> wrapper = genTableDao.buildQueryWrapper(genTable);
        return genTableDao.page(wrapper, pageQuery);
    }

    /**
     * 根据表ID查询业务字段列表
     *
     * @param tableId 业务表ID
     * @return 业务字段集合
     */
    @Override
    public List<GenTableColumn> listGenTableColumnsByTableId(Long tableId) {
        return genTableColumnDao.listByTableId(tableId);
    }

    /**
     * 根据ID查询业务表信息
     *
     * @param id 业务表ID
     * @return 业务表信息
     */
    @Override
    public GenTable getGenTableById(Long id) {
        GenTable genTable = genTableDao.getGenTableById(id);
        // 设置表的扩展选项信息
        setTableFromOptions(genTable);
        return genTable;
    }


    /**
     * 分页查询数据库表列表
     * 从指定数据源中查询可用于导入的数据库表
     *
     * @param genTable  包含查询条件的GenTable对象
     * @param pageQuery 包含分页信息的PageQuery对象
     * @return 包含分页结果的数据库表列表
     */
    @DS("#genTable.dataName")
    @Override
    public PageResult<GenTable> pageDbTables(GenTable genTable, PageQuery pageQuery) {
        // 获取查询条件
        String tableName = genTable.getTableName();
        String tableComment = genTable.getTableComment();
        String searchValue = genTable.getSearchValue();
        // 获取数据库中所有表的元数据信息
        LinkedHashMap<String, Table<?>> tablesMap = ServiceProxy.metadata().tables();
        if (CollUtil.isEmpty(tablesMap)) {
            return PageResult.of();
        }

        // 获取已经导入的表名列表，避免重复导入
        List<String> tableNames = genTableDao.listTableNames(genTable.getDataName());
        String[] tableArrays;
        if (CollUtil.isNotEmpty(tableNames)) {
            tableArrays = tableNames.toArray(new String[0]);
        } else {
            tableArrays = new String[0];
        }

        // 过滤并转换表格数据
        List<GenTable> tables = tablesMap.values().stream()
            // 过滤掉系统表（以指定前缀开头的表）
            .filter(x -> !StringUtils.startsWithAnyIgnoreCase(x.getName(), TABLE_IGNORE))
            // 过滤掉已经导入的表
            .filter(x -> {
                if (CollUtil.isEmpty(tableNames)) {
                    return true;
                }
                return !StringUtils.equalsAnyIgnoreCase(x.getName(), tableArrays);
            })
            // 根据查询条件过滤表
            .filter(x -> {
                boolean nameMatches = true;
                boolean commentMatches = true;
                // 进行表名称的模糊查询
                if (StringUtils.isNotBlank(tableName)) {
                    nameMatches = StringUtils.containsIgnoreCase(x.getName(), tableName);
                }
                // 进行表描述的模糊查询
                if (StringUtils.isNotBlank(tableComment)) {
                    commentMatches = StringUtils.containsIgnoreCase(x.getComment(), tableComment);
                }
                if (StringUtils.isNotBlank(searchValue)) {
                    commentMatches = StringUtils.containsIgnoreCase(x.getName(), searchValue) ||
                        StringUtils.containsIgnoreCase(x.getComment(), searchValue);
                }
                // 同时匹配名称和描述
                return nameMatches && commentMatches;
            })
            // 转换为GenTable对象
            .map(x -> {
                GenTable gen = new GenTable();
                gen.setTableName(x.getName());
                gen.setTableComment(x.getComment());
                // postgresql的表元数据没有创建时间，使用当前时间代替
                gen.setCreateTime(ObjectUtil.defaultIfNull(x.getCreateTime(), new Date()));
                gen.setUpdateTime(x.getUpdateTime());
                return gen;
            }).sorted(Comparator.comparing(GenTable::getCreateTime).reversed())
            .toList();

        // 手动分页处理
        IPage<GenTable> page = pageQuery.build();
        page.setTotal(tables.size());
        // 根据分页参数截取数据
        page.setRecords(CollUtil.page((int) page.getCurrent() - 1, (int) page.getSize(), tables));
        return PageResult.of(page);
    }

    /**
     * 根据表名数组查询数据库表信息
     *
     * @param tableNames 表名称数组
     * @param dataName   数据源名称
     * @return 数据库表集合
     */
    @DS("#dataName")
    @Override
    public List<GenTable> listDbTablesByNames(String[] tableNames, String dataName) {
        Set<String> tableNameSet = new HashSet<>(List.of(tableNames));
        LinkedHashMap<String, Table<?>> tablesMap = ServiceProxy.metadata().tables();

        if (CollUtil.isEmpty(tablesMap)) {
            return new ArrayList<>();
        }

        // 过滤出指定名称的表
        List<Table<?>> tableList = tablesMap.values().stream()
            .filter(x -> !StringUtils.startsWithAnyIgnoreCase(x.getName(), TABLE_IGNORE))
            .filter(x -> tableNameSet.contains(x.getName())).toList();

        if (CollUtil.isEmpty(tableList)) {
            return new ArrayList<>();
        }

        // 转换为GenTable对象
        return tableList.stream().map(x -> {
            GenTable gen = new GenTable();
            gen.setDataName(dataName);
            gen.setTableName(x.getName());
            gen.setTableComment(x.getComment());
            gen.setCreateTime(x.getCreateTime());
            gen.setUpdateTime(x.getUpdateTime());
            return gen;
        }).toList();
    }

    /**
     * 查询所有业务表信息
     *
     * @return 业务表集合
     */
    @Override
    public List<GenTable> listAllGenTables() {
        return genTableDao.listAllGenTables();
    }

    /**
     * 修改业务表信息
     * 更新表配置和列配置信息
     *
     * @param genTable 业务表信息
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateGenTable(GenTable genTable) {
        // 将扩展参数转换为JSON字符串存储
        String options = JsonUtils.toJsonString(genTable.getParams());
        genTable.setOptions(options);

        // 更新表信息
        boolean result = genTableDao.updateById(genTable) > 0;
        if (result) {
            // 更新列信息
            for (GenTableColumn cenTableColumn : genTable.getColumns()) {
                genTableColumnDao.updateById(cenTableColumn);
            }
        }
    }

    /**
     * 批量删除业务表
     *
     * @param tableIds 需要删除的表ID数组
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteGenTablesByIds(Long[] tableIds) {
        List<Long> ids = List.of(tableIds);
        // 删除表信息
        genTableDao.deleteByIds(ids);
        // 删除对应的列信息
        genTableColumnDao.deleteByTableIds(ids);
    }

    /**
     * 导入数据库表结构
     * 将数据库表导入到代码生成器中进行配置
     *
     * @param tableList 要导入的表列表
     * @param dataName  数据源名称
     */
    @DSTransactional
    @Override
    public void importGenTables(List<GenTable> tableList, String dataName) {
        try {
            for (GenTable table : tableList) {
                String tableName = table.getTableName();
                // 初始化表的基本信息
                GenUtils.initTable(table);
                table.setDataName(dataName);
                table.setCreateTime(new Date());

                // 将扩展参数序列化为JSON存储（包括菜单图标、自动导入菜单等）
                String options = JsonUtils.toJsonString(table.getParams());
                table.setOptions(options);

                // 保存表信息
                boolean result = genTableDao.insert(table) > 0;
                if (result) {
                    // 查询并保存列信息
                    List<GenTableColumn> genTableColumns = SpringUtils.getAopProxy(this).listDbTableColumnsByName(tableName, dataName);
                    List<GenTableColumn> saveColumns = new ArrayList<>();
                    for (GenTableColumn column : genTableColumns) {
                        // 初始化列的配置信息
                        GenUtils.initColumnField(column, table);
                        saveColumns.add(column);
                    }
                    if (CollUtil.isNotEmpty(saveColumns)) {
                        genTableColumnDao.batchSave(saveColumns);
                    }
                }
            }
        } catch (Exception e) {
            throw ServiceException.of("导入失败：" + e.getMessage());
        }
    }

    /**
     * 根据表名查询数据库列信息
     *
     * @param tableName 表名称
     * @param dataName  数据源名称
     * @return 列信息列表
     */
    @DS("#dataName")
    @Override
    public List<GenTableColumn> listDbTableColumnsByName(String tableName, String dataName) {
        // 获取表的元数据信息
        Table<?> table = ServiceProxy.metadata().table(tableName);
        if (ObjectUtil.isNull(table)) {
            return new ArrayList<>();
        }

        // 获取表的所有列信息
        LinkedHashMap<String, Column> columns = table.getColumns();
        List<GenTableColumn> tableColumns = new ArrayList<>();

        // 转换列信息
        columns.forEach((columnName, column) -> {
            GenTableColumn tableColumn = new GenTableColumn();
            tableColumn.setIsPk(column.isPrimaryKey() ? "1" : "0");
            tableColumn.setColumnName(column.getName());
            tableColumn.setColumnComment(column.getComment());
            tableColumn.setColumnType(column.getOriginType().toLowerCase());
            tableColumn.setSort(column.getPosition());
            tableColumn.setIsRequired(column.isNullable() ? "0" : "1");
            tableColumn.setIsIncrement(column.isAutoIncrement() ? "1" : "0");

            // 获取字段默认值
            tableColumn.setColumnDefault(String.valueOf(column.getDefaultValue()));

            tableColumns.add(tableColumn);
        });
        return tableColumns;
    }

    /**
     * 预览生成的代码
     * 生成代码内容但不保存到文件，用于预览
     *
     * @param tableId 表ID
     * @return 模板文件名和生成内容的映射
     */
    @Override
    public Map<String, String> previewCode(Long tableId) {
        Map<String, String> dataMap = new LinkedHashMap<>();

        // 查询表信息
        GenTable table = genTableDao.getGenTableById(tableId);
        // 设置扩展选项信息（包括菜单图标等）
        setTableFromOptions(table);

        // 生成菜单ID（用于SQL脚本）
        List<Long> menuIds = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            menuIds.add(identifierGenerator.nextId(null).longValue());
        }
        table.setMenuIds(menuIds);
        // 设置主子表信息
        setSubTable(table);
        // 设置父表信息
        setParentTable(table);
        // 设置主键列信息
        setPkColumn(table);
        // 初始化Velocity模板引擎
        VelocityInitializer.initVelocity();

        // 准备模板上下文
        VelocityContext context = VelocityUtils.prepareContext(table);

        // 获取模板列表并逐个渲染
        List<String> templates = VelocityUtils.getTemplateList(table.getTplCategory(), table.getDataName());
        for (String template : templates) {
            // 渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, Constants.UTF8);
            tpl.merge(context, sw);
            // 使用完整的预览文件路径作为key
            String previewFileName = VelocityUtils.getPreviewFileName(template, table);
            dataMap.put(previewFileName, sw.toString());
        }
        return dataMap;
    }

    /**
     * 生成代码并打包下载
     *
     * @param tableId 表ID
     * @return 生成的代码压缩包字节数组
     */
    @Override
    public byte[] downloadCode(Long tableId) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(outputStream);
        generatorCode(tableId, zip);
        IoUtil.close(zip);
        return outputStream.toByteArray();
    }

    /**
     * 自动导入菜单SQL
     * <p>根据配置判断是否需要导入菜单SQL，支持增量导入（已存在的菜单不重复导入）</p>
     * <p>⚠️ 注意：此方法已添加事务管理，SQL执行失败会自动回滚</p>
     * <p>⚠️ 限制：仅当生成路径为当前项目根目录时才执行SQL导入，防止影响其他项目</p>
     *
     * @param table 业务表信息
     * @return 导入状态消息（成功/跳过/失败）
     */
    @Transactional(rollbackFor = Exception.class)
    protected String autoImportMenuSql(GenTable table) {
        // 检查是否开启自动导入
        if (!"1".equals(table.getAutoImportMenu())) {
            return null;
        }

        // 检查生成方式：只有自定义路径（genType=1）才需要导入
        if (!"1".equals(table.getGenType())) {
            log.info("生成方式为zip下载，跳过菜单导入");
            return null;
        }

        // 检查生成路径：只有当前项目才执行SQL导入
        if (!isCurrentProject(table.getGenPath())) {
            log.info("生成路径非当前项目，跳过菜单导入: {}", table.getGenPath());
            return null;
        }

        try {
            // 提取主菜单权限字符串：{moduleName}:{businessName}:view
            String mainPerms = extractMainPermission(table);

            // 检查菜单是否已存在
            if (menuService.existsByPerms(mainPerms)) {
                String message = String.format("⏭️ 菜单已存在，跳过: %s", mainPerms);
                log.info(message);
                return "菜单已存在，跳过导入";
            }

            // 渲染菜单SQL
            String sqlContent = renderMenuSql(table);
            if (StringUtils.isBlank(sqlContent)) {
                log.warn("⚠️ 菜单SQL内容为空");
                return "菜单SQL内容为空";
            }

            // 执行SQL语句
            executeSqlStatements(sqlContent);

            String message = String.format("✅ 自动导入菜单成功: %s", mainPerms);
            log.info(message);
            return "菜单导入成功";

        } catch (Exception e) {
            String errorMessage = String.format("❌ 自动导入菜单失败: %s", e.getMessage());
            log.error(errorMessage, e);
            return "菜单导入失败: " + e.getMessage();
        }
    }

    /**
     * 提取主菜单权限字符串
     * <p>格式：{moduleName}:{businessName}:view</p>
     *
     * @param table 业务表信息
     * @return 权限字符串
     */
    private String extractMainPermission(GenTable table) {
        // 从包名提取模块名（最后一段）
        String packageName = table.getPackageName();
        String moduleName = packageName.substring(packageName.lastIndexOf('.') + 1);
        String businessName = table.getBusinessName();
        return String.format("%s:%s:view", moduleName, businessName);
    }

    /**
     * 渲染菜单SQL
     * <p>使用Velocity模板引擎生成SQL内容</p>
     *
     * @param table 业务表信息
     * @return SQL内容
     */
    private String renderMenuSql(GenTable table) {
        // 生成菜单ID（用于SQL脚本）
        List<Long> menuIds = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            menuIds.add(identifierGenerator.nextId(null).longValue());
        }
        table.setMenuIds(menuIds);

        // 准备Velocity上下文
        VelocityContext context = VelocityUtils.prepareContext(table);

        // 渲染SQL模板
        StringWriter sw = new StringWriter();
        Template tpl = Velocity.getTemplate("vm/sql/sql.vm", Constants.UTF8);
        tpl.merge(context, sw);

        return sw.toString();
    }

    /**
     * 判断生成路径是否为当前项目
     * <p>通过对比配置路径与当前项目根目录来判断</p>
     *
     * @param genPath 生成路径（"/" 表示默认路径，其他为自定义路径）
     * @return true-当前项目，false-其他项目
     */
    private boolean isCurrentProject(String genPath) {
        // 默认路径 "/" 表示当前项目
        if (StringUtils.equals(genPath, "/")) {
            return true;
        }

        // 获取当前项目根目录
        String currentProjectRoot = getProjectRoot();

        // 规范化路径（去除末尾分隔符）
        String normalizedGenPath = genPath.endsWith("/") || genPath.endsWith("\\")
            ? genPath.substring(0, genPath.length() - 1)
            : genPath;
        String normalizedCurrentRoot = currentProjectRoot.endsWith("/") || currentProjectRoot.endsWith("\\")
            ? currentProjectRoot.substring(0, currentProjectRoot.length() - 1)
            : currentProjectRoot;

        // 对比路径（忽略大小写，Windows路径不区分大小写）
        boolean isSameProject = normalizedGenPath.equalsIgnoreCase(normalizedCurrentRoot);

        log.debug("路径判断 - 配置路径: {}, 当前项目: {}, 是否相同: {}",
            normalizedGenPath, normalizedCurrentRoot, isSameProject);

        return isSameProject;
    }

    /**
     * 执行SQL语句
     * <p>解析并执行多条INSERT语句</p>
     * <p>注意：依赖 JdbcTemplate 的事务管理，需在外层方法添加事务注解</p>
     *
     * @param sqlContent SQL内容
     */
    private void executeSqlStatements(String sqlContent) {
        // 提取所有INSERT语句（使用正则表达式）
        Pattern pattern = Pattern.compile("INSERT INTO.*?;", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(sqlContent);

        int executedCount = 0;
        while (matcher.find()) {
            String sql = matcher.group().trim();
            if (StringUtils.isNotBlank(sql)) {
                jdbcTemplate.execute(sql);
                executedCount++;
            }
        }

        if (executedCount == 0) {
            throw ServiceException.of("未找到有效的SQL语句");
        }

        log.info("成功执行 {} 条SQL语句", executedCount);
    }

    /**
     * 生成代码到指定路径
     * <p>支持前后端代码同时生成，自动覆盖到项目目录</p>
     *
     * @param tableId 表ID
     * @return 生成结果（包含菜单导入状态）
     */
    @Override
    public CodeGenResult generatorCode(Long tableId) {
        // 查询表信息
        GenTable table = genTableDao.getGenTableById(tableId);
        // 设置扩展选项信息（包括菜单图标等）
        setTableFromOptions(table);

        // 生成菜单ID（用于SQL脚本）
        List<Long> menuIds = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            menuIds.add(identifierGenerator.nextId(null).longValue());
        }
        table.setMenuIds(menuIds);

        // 设置表的关联信息
        setSubTable(table);
        // 设置父表信息
        setParentTable(table);
        // 设置主键列信息
        setPkColumn(table);

        // 初始化Velocity模板引擎
        VelocityInitializer.initVelocity();
        VelocityContext context = VelocityUtils.prepareContext(table);

        // 获取模板列表
        List<String> templates = VelocityUtils.getTemplateList(table.getTplCategory(), table.getDataName());

        // 代码生成开始
        log.info("========== 代码生成开始 ==========");
        log.info("表名: {}", table.getTableName());
        log.info("包名: {}", table.getPackageName());
        log.info("业务名: {}", table.getBusinessName());

        // 统计覆盖文件
        List<String> overwriteFiles = new ArrayList<>();

        for (String template : templates) {
            // 渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, Constants.UTF8);
            tpl.merge(context, sw);
            try {
                // 获取生成文件的路径
                String path = getGenPath(table, template);
                File targetFile = new File(path);

                // 检查文件是否存在（覆盖提醒）
                if (targetFile.exists()) {
                    overwriteFiles.add(targetFile.getName());
                    log.warn("⚠️  文件将被覆盖: {}", path);
                } else {
                    log.info("✅ 新建文件: {}", path);
                }

                // 写入文件
                FileUtils.writeUtf8String(sw.toString(), path);
            } catch (Exception e) {
                log.error("渲染模板失败，表名：{}, 模板：{}", table.getTableName(), template, e);
                throw ServiceException.of("渲染模板失败，表名：" + table.getTableName());
            }
        }

        // 日志：代码生成完成
        log.info("========== 代码生成完成 ==========");
        if (!overwriteFiles.isEmpty()) {
            log.warn("⚠️  共覆盖 {} 个文件: {}", overwriteFiles.size(), String.join(", ", overwriteFiles));
        }

        // 自动导入菜单SQL（如果开启）
        String importResult = autoImportMenuSql(table);
        if (StringUtils.isNotBlank(importResult)) {
            log.info("菜单导入结果: {}", importResult);
        }

        // 返回生成结果
        return CodeGenResult.success(templates.size(), overwriteFiles.size(), importResult);
    }

    /**
     * 设置主子表信息
     * 如果配置了子表，则查询子表信息
     *
     * @param table 业务表信息
     */
    public void setSubTable(GenTable table) {
        String subTableName = table.getSubTableName();
        if (StringUtils.isNotEmpty(subTableName)) {
            table.setSubTable(genTableDao.getGenTableByName(subTableName));
        }
    }

    /**
     * 设置父表信息
     * 查询当前表是否为其他表的子表，如果是则设置父表信息
     *
     * @param table 业务表信息
     */
    public void setParentTable(GenTable table) {
        // 查询是否有其他表将当前表作为子表
        GenTable parentTable = genTableDao.getParentTableBySubTableName(table.getTableName());
        if (parentTable != null) {
            // 设置当前表为子表标识
            table.setIsSubTable(true);
            table.setParentTable(parentTable);
        } else {
            table.setIsSubTable(false);
        }
    }

    /**
     * 同步数据库表结构
     * 将数据库中的最新表结构同步到代码生成器中
     *
     * @param tableId 表ID
     */
    @DSTransactional
    @Override
    public void syncDatabase(Long tableId) {
        GenTable table = genTableDao.getGenTableById(tableId);
        List<GenTableColumn> tableColumns = table.getColumns();
        // 构建现有列的映射，用于保留用户配置
        Map<String, GenTableColumn> tableColumnMap = StreamUtils.toIdentityMap(tableColumns, GenTableColumn::getColumnName);

        // 从数据库重新获取列信息
        List<GenTableColumn> dbTableColumns = SpringUtils.getAopProxy(this).listDbTableColumnsByName(table.getTableName(), table.getDataName());
        if (CollUtil.isEmpty(dbTableColumns)) {
            throw ServiceException.of("同步数据失败，原表结构不存在");
        }
        List<String> dbTableColumnNames = StreamUtils.toList(dbTableColumns, GenTableColumn::getColumnName);

        List<GenTableColumn> saveColumns = new ArrayList<>();
        dbTableColumns.forEach(column -> {
            // 初始化列的基本信息
            GenUtils.initColumnField(column, table);

            // 如果列已存在，保留用户的配置
            if (tableColumnMap.containsKey(column.getColumnName())) {
                GenTableColumn prevColumn = tableColumnMap.get(column.getColumnName());
                column.setColumnId(prevColumn.getColumnId());

                // 保留增删改查配置
                column.setIsInsert(prevColumn.getIsInsert());
                column.setIsEdit(prevColumn.getIsEdit());
                column.setIsList(prevColumn.getIsList());
                column.setIsQuery(prevColumn.getIsQuery());

                // 保留查询方式和必填验证
                if (StringUtils.isNotEmpty(prevColumn.getQueryType())) {
                    column.setQueryType(prevColumn.getQueryType());
                }
                if (StringUtils.isNotEmpty(prevColumn.getIsRequired())) {
                    column.setIsRequired(prevColumn.getIsRequired());
                }

                // 保留显示类型配置
                if (StringUtils.isNotEmpty(prevColumn.getHtmlType())) {
                    column.setHtmlType(prevColumn.getHtmlType());
                }

                // 保留字典类型配置
                if (StringUtils.isNotEmpty(prevColumn.getDictType())) {
                    column.setDictType(prevColumn.getDictType());
                }

                // 处理字段标签：只有备注发生变化时才重新生成标签
                if (!StringUtils.equals(prevColumn.getColumnComment(), column.getColumnComment())) {
                    // 备注变化了，重新生成标签
                    String newLabel = GenUtils.removeBrackets(column.getColumnComment());
                    column.setColumnLabel(StringUtils.isBlank(newLabel) ? column.getJavaField() : newLabel);
                } else {
                    // 备注未变化，保留原标签（用户可能手动修改过）
                    column.setColumnLabel(prevColumn.getColumnLabel());
                }

            }
            saveColumns.add(column);
        });

        // 批量保存更新后的列信息
        if (CollUtil.isNotEmpty(saveColumns)) {
            genTableColumnDao.batchSave(saveColumns);
        }

        // 删除数据库中已不存在的列
        List<GenTableColumn> delColumns = StreamUtils.filter(tableColumns, column -> !dbTableColumnNames.contains(column.getColumnName()));
        if (CollUtil.isNotEmpty(delColumns)) {
            List<Long> ids = StreamUtils.toList(delColumns, GenTableColumn::getColumnId);
            if (CollUtil.isNotEmpty(ids)) {
                genTableColumnDao.deleteByIds(ids);
            }
        }
    }

    /**
     * 批量生成代码并打包下载
     *
     * @param tableIds 表ID数组
     * @return 生成的代码压缩包字节数组
     */
    @Override
    public byte[] downloadCode(String[] tableIds) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(outputStream);
        for (String tableId : tableIds) {
            generatorCode(Long.parseLong(tableId), zip);
        }
        IoUtil.close(zip);
        return outputStream.toByteArray();
    }

    /**
     * 生成代码并添加到ZIP压缩包
     *
     * @param tableId 表ID
     * @param zip     ZIP输出流
     */
    private void generatorCode(Long tableId, ZipOutputStream zip) {
        // 查询表信息
        GenTable table = genTableDao.getGenTableById(tableId);
        // 设置扩展选项信息（包括菜单图标等）
        setTableFromOptions(table);

        // 生成菜单ID（用于SQL脚本）
        List<Long> menuIds = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            menuIds.add(identifierGenerator.nextId(null).longValue());
        }
        table.setMenuIds(menuIds);

        // 设置表的关联信息
        setSubTable(table);
        // 设置父表信息
        setParentTable(table);
        // 设置主键列信息
        setPkColumn(table);

        // 初始化Velocity模板引擎
        VelocityInitializer.initVelocity();
        VelocityContext context = VelocityUtils.prepareContext(table);

        // 获取模板列表并逐个渲染
        List<String> templates = VelocityUtils.getTemplateList(table.getTplCategory(), table.getDataName());
        for (String template : templates) {
            // 渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, Constants.UTF8);
            tpl.merge(context, sw);
            try {
                // 添加到ZIP压缩包
                zip.putNextEntry(new ZipEntry(VelocityUtils.getFileName(template, table)));
                IoUtil.write(zip, StandardCharsets.UTF_8, false, sw.toString());
                IoUtil.close(sw);
                zip.flush();
                zip.closeEntry();
            } catch (IOException e) {
                log.error("渲染模板失败，表名：" + table.getTableName(), e);
            }
        }
    }

    /**
     * 修改保存参数校验
     * 验证业务表配置的有效性
     *
     * @param genTable 业务表信息
     */
    @Override
    public void validateEdit(GenTable genTable) {
        // 树形表模板参数验证
        if (GenConstants.TPL_TREE.equals(genTable.getTplCategory())) {
            String options = JsonUtils.toJsonString(genTable.getParams());
            Dict paramsObj = JsonUtils.parseMap(options);
            if (StringUtils.isEmpty(paramsObj.getStr(GenConstants.TREE_CODE))) {
                throw ServiceException.of("树编码字段不能为空");
            } else if (StringUtils.isEmpty(paramsObj.getStr(GenConstants.TREE_PARENT_CODE))) {
                throw ServiceException.of("树父编码字段不能为空");
            } else if (StringUtils.isEmpty(paramsObj.getStr(GenConstants.TREE_NAME))) {
                throw ServiceException.of("树名称字段不能为空");
            }
        }
        // 主子表模板参数验证
        else if (GenConstants.TPL_SUB.equals(genTable.getTplCategory())) {
            if (StringUtils.isEmpty(genTable.getSubTableName())) {
                throw ServiceException.of("关联子表的表名不能为空");
            } else if (StringUtils.isEmpty(genTable.getSubTableFkName())) {
                throw ServiceException.of("子表关联的外键名不能为空");
            } else if (ObjectUtil.isNull(genTableDao.getGenTableByName(genTable.getSubTableName()))) {
                throw ServiceException.of(StringUtils.format("请先导入子表:{}进行配置", genTable.getSubTableName()));
            }
        }
    }

    /**
     * 设置主键列信息
     * 找到表的主键列，如果没有主键则智能选择合适的列作为主键
     *
     * @param table 业务表信息
     */
    public void setPkColumn(GenTable table) {
        // 检查主表是否有字段
        if (CollUtil.isEmpty(table.getColumns())) {
            throw ServiceException.of(String.format("表 [%s] 没有任何字段，无法生成代码，请检查表结构", table.getTableName()));
        }

        // 查找主表的主键列
        for (GenTableColumn column : table.getColumns()) {
            if (column.isPk()) {
                table.setPkColumn(column);
                break;
            }
        }

        // 如果没有找到主键，智能选择合适的列
        if (ObjectUtil.isNull(table.getPkColumn())) {
            GenTableColumn pkColumn = selectBestPkColumn(table.getColumns(), table.getTableName());
            table.setPkColumn(pkColumn);
        }

        // 如果是主子表，也要设置子表的主键列
        if (GenConstants.TPL_SUB.equals(table.getTplCategory())) {
            GenTable subTable = table.getSubTable();

            // 检查子表是否有字段
            if (CollUtil.isEmpty(subTable.getColumns())) {
                throw ServiceException.of(String.format("子表 [%s] 没有任何字段，无法生成代码，请检查表结构", subTable.getTableName()));
            }

            // 查找子表的主键列
            for (GenTableColumn column : subTable.getColumns()) {
                if (column.isPk()) {
                    subTable.setPkColumn(column);
                    break;
                }
            }

            // 如果没有找到主键，智能选择合适的列
            if (ObjectUtil.isNull(subTable.getPkColumn())) {
                GenTableColumn pkColumn = selectBestPkColumn(subTable.getColumns(), subTable.getTableName());
                subTable.setPkColumn(pkColumn);
            }
        }
    }

    /**
     * 智能选择最佳的主键列
     * 优先级：id列 > 第一个Long类型列 > 第一列
     *
     * @param columns   字段列表
     * @param tableName 表名（用于日志）
     * @return 选中的主键列
     */
    private GenTableColumn selectBestPkColumn(List<GenTableColumn> columns, String tableName) {
        // 优先查找名为 "id" 的列
        for (GenTableColumn column : columns) {
            if ("id".equalsIgnoreCase(column.getColumnName())) {
                log.warn("⚠️ 表 [{}] 没有设置主键，已自动选择 [id] 列作为主键，建议在数据库中设置主键约束", tableName);
                return column;
            }
        }

        // 其次查找第一个 Long 类型的列
        for (GenTableColumn column : columns) {
            if ("Long".equals(column.getJavaType())) {
                log.warn("⚠️ 表 [{}] 没有设置主键，已自动选择 [{}] 列作为主键，建议在数据库中设置主键约束",
                    tableName, column.getColumnName());
                return column;
            }
        }

        // 最后使用第一列
        GenTableColumn firstColumn = columns.get(0);
        log.warn("⚠️ 表 [{}] 没有设置主键，已自动选择第一列 [{}] 作为主键，建议在数据库中设置主键约束",
            tableName, firstColumn.getColumnName());
        return firstColumn;
    }

    /**
     * 设置代码生成其他选项值
     * 从JSON配置中解析扩展参数并设置到表对象中
     *
     * @param genTable 业务表对象
     */
    public void setTableFromOptions(GenTable genTable) {
        Dict paramsObj = JsonUtils.parseMap(genTable.getOptions());
        if (ObjectUtil.isNotNull(paramsObj)) {
            String treeCode = paramsObj.getStr(GenConstants.TREE_CODE);
            String treeParentCode = paramsObj.getStr(GenConstants.TREE_PARENT_CODE);
            String treeName = paramsObj.getStr(GenConstants.TREE_NAME);
            Long parentMenuId = paramsObj.getLong(GenConstants.PARENT_MENU_ID);
            String parentMenuName = paramsObj.getStr(GenConstants.PARENT_MENU_NAME);
            String menuIcon = paramsObj.getStr(GenConstants.MENU_ICON);
            Integer menuOrder = paramsObj.getInt(GenConstants.MENU_ORDER);
            String autoImportMenu = paramsObj.getStr(GenConstants.AUTO_IMPORT_MENU);

            genTable.setTreeCode(treeCode);
            genTable.setTreeParentCode(treeParentCode);
            genTable.setTreeName(treeName);
            genTable.setParentMenuId(parentMenuId);
            genTable.setParentMenuName(parentMenuName);
            genTable.setMenuIcon(menuIcon);
            genTable.setMenuOrder(menuOrder);
            genTable.setAutoImportMenu(autoImportMenu);
        }
    }

    /**
     * 获取代码生成路径
     * 根据表配置的生成路径和模板类型确定最终的文件路径
     * <p>支持智能路径分发：</p>
     * <ul>
     *   <li>后端代码 → {根目录}/ruoyi-modules/ruoyi-business/src/</li>
     *   <li>前端代码 → {根目录}/plus-ui/</li>
     *   <li>SQL文件 → {根目录}/script/sql/menu/</li>
     * </ul>
     *
     * @param table    业务表信息
     * @param template 模板文件路径
     * @return 生成文件的完整路径
     */
    public static String getGenPath(GenTable table, String template) {
        String genPath = table.getGenPath();

        // 判断模板类型
        boolean isFrontend = StringUtils.containsAny(template,
            "api.ts.vm", "types.ts.vm", "index.vue.vm", "index-tree.vue.vm", "child.vue.vm");
        boolean isSql = template.contains("sql.vm");

        // 获取项目根目录
        String projectRoot;
        if (StringUtils.equals(genPath, "/")) {
            // 默认路径：获取项目真正的根目录
            projectRoot = getProjectRoot();
        } else {
            // 自定义路径：使用用户配置的路径
            projectRoot = genPath;
        }

        // 获取文件相对路径（VelocityUtils返回的是相对路径）
        String fileName = VelocityUtils.getFileName(template, table);

        // 从表级配置读取模块名称和前端目录（优先使用表级配置，未设置则使用全局默认值）
        String backendModule = table.getBackendModuleName();
        String frontendDir = table.getFrontendRootDir();

        // 根据模板类型智能分发到不同目录
        if (isFrontend) {
            // 前端代码 → projectRoot/{frontendDir}/{fileName}
            return projectRoot + File.separator + frontendDir
                   + File.separator + fileName;

        } else if (isSql) {
            // SQL菜单 → projectRoot/script/sql/menu/{fileName}
            return projectRoot + File.separator + "script"
                   + File.separator + "sql"
                   + File.separator + "menu"
                   + File.separator + fileName;

        } else {
            // 后端代码 → projectRoot/ruoyi-modules/{backendModule}/src/{fileName}
            // 注意：fileName 已经包含了 main/java/... 或 main/resources/...，所以这里需要加 src/
            return projectRoot + File.separator + "ruoyi-modules"
                   + File.separator + backendModule
                   + File.separator + "src"
                   + File.separator + fileName;
        }
    }

    /**
     * 获取项目根目录
     * <p>兼容各种运行环境，自动识别项目根路径</p>
     * <p>判断依据：项目根目录应该包含 ruoyi-modules 和 plus-ui 子目录</p>
     *
     * @return 项目根目录路径
     */
    private static String getProjectRoot() {
        String userDir = System.getProperty("user.dir");
        File currentDir = new File(userDir);

        // 判断当前目录是否为项目根目录
        // 标准：同时存在 ruoyi-modules 和 plus-ui 子目录
        if (isProjectRoot(currentDir)) {
            return userDir;
        }

        // 如果不是，向上查找（最多查找3层）
        File parent = currentDir.getParentFile();
        int maxDepth = 3;
        while (parent != null && maxDepth-- > 0) {
            if (isProjectRoot(parent)) {
                return parent.getAbsolutePath();
            }
            parent = parent.getParentFile();
        }

        // 如果都找不到，返回当前目录（兼容处理）
        return userDir;
    }

    /**
     * 判断是否为项目根目录
     * <p>标准：同时存在 ruoyi-modules 和 plus-ui（或plus-uniapp）子目录</p>
     *
     * @param dir 待判断的目录
     * @return true 如果是项目根目录
     */
    private static boolean isProjectRoot(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return false;
        }

        // 检查是否存在关键子目录
        File modulesDir = new File(dir, "ruoyi-modules");
        File uiDir = new File(dir, "plus-ui");
        File uniappDir = new File(dir, "plus-uniapp");

        // 必须有 ruoyi-modules，并且至少有一个前端目录
        return modulesDir.exists() && modulesDir.isDirectory()
            && (uiDir.exists() || uniappDir.exists());
    }
}
