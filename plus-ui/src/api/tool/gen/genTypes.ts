// 代码生成类型
export interface GenTableQuery extends PageQuery {
  /** 数据源名称 */
  dataName?: string
  /** 数据库表名 */
  tableName: string
  /** 表注释描述 */
  tableComment: string
}

export interface GenTable {
  /** 表主键ID */
  tableId?: string | number
  /** 数据源名称 */
  dataName?: string
  /** 数据库表名 */
  tableName: string
  /** 表注释描述 */
  tableComment: string
  /** 子表表名 */
  subTableName?: string
  /** 子表外键字段名 */
  subTableFkName?: string
  /** Java实体类名 */
  className?: string
  /** 模板类型(crud/tree/sub) */
  tplCategory?: string
  /** 生成代码的包路径 */
  packageName?: string
  /** 生成模块名 */
  moduleName?: string
  /** 业务名称 */
  businessName?: string
  /** 功能名称 */
  functionName?: string
  /** 代码作者 */
  functionAuthor?: string
  /** 代码生成方式(0-zip下载 1-自定义路径) */
  genType?: string
  /** 自定义生成路径 */
  genPath?: string
  /** 主键列信息 */
  pkColumn?: GenTableColumn
  /** 关联子表信息 */
  subTable?: GenTable
  /** 表列配置列表 */
  columns?: GenTableColumn[]
  /** 是否为子表 */
  isSubTable?: boolean
  /** 父表信息 */
  parentTable?: GenTable
  /** 其他生成选项 */
  options?: string
  /** 备注信息 */
  remark?: string
  /** 树编码字段名 */
  treeCode?: string
  /** 树父编码字段名 */
  treeParentCode?: string
  /** 树名称字段名 */
  treeName?: string
  /** 关联菜单ID列表 */
  menuIds?: number[]
  /** 上级菜单ID */
  parentMenuId?: number | string
  /** 上级菜单名称 */
  parentMenuName?: string
  /** 菜单图标 */
  menuIcon?: string
  /** 菜单顺序 */
  menuOrder?: number
  /** 是否自动导入菜单(0-否 1-是) */
  autoImportMenu?: string
  /** 后端模块名称 */
  backendModuleName?: string
  /** 前端项目根目录 */
  frontendRootDir?: string
  /** 创建部门 */
  createDept?: number | string
  /** 是否为树表模式 */
  tree?: boolean
  /** 是否为CRUD模式 */
  crud?: boolean
  /** 是否为主子表模式 */
  sub?: boolean
  /** 参数配置 */
  params?: GenTableParams
  /** 创建者 */
  createBy?: string
  /** 创建时间 */
  createTime?: string
  /** 更新者 */
  updateBy?: string
  /** 更新时间 */
  updateTime?: string
}

/** 代码生成表参数配置 */
export interface GenTableParams {
  /** 树编码字段名 */
  treeCode?: string
  /** 树名称字段名 */
  treeName?: string
  /** 树父编码字段名 */
  treeParentCode?: string
  /** 上级菜单ID */
  parentMenuId?: number
  /** 菜单图标 */
  menuIcon?: string
  /** 菜单顺序 */
  menuOrder?: number
  /** 是否自动导入菜单 */
  autoImportMenu?: string
  /** 后端模块名称 */
  backendModuleName?: string
  /** 前端项目根目录 */
  frontendRootDir?: string
}

export interface GenTableColumn {
  /** 列ID */
  columnId?: string | number
  /** 表ID */
  tableId?: string | number
  /** 列名 */
  columnName?: string
  /** 列注释 */
  columnComment?: string
  /** 列类型 */
  columnType?: string
  /** Java类型 */
  javaType?: string
  /** Java字段名 */
  javaField?: string
  /** 是否主键(字符串形式: "1"是) */
  isPk?: string
  /** 是否自增(字符串形式: "1"是) */
  isIncrement?: string
  /** 是否必填(字符串形式: "1"是) */
  isRequired?: string
  /** 是否插入(字符串形式: "1"是) */
  isInsert?: string
  /** 是否编辑(字符串形式: "1"是) */
  isEdit?: string
  /** 是否列表(字符串形式: "1"是) */
  isList?: string
  /** 是否查询(字符串形式: "1"是) */
  isQuery?: string
  /** 查询方式(EQ等于、NE不等于、GT大于、LT小于、LIKE模糊、BETWEEN范围) */
  queryType?: string
  /** 显示类型(input文本框、textarea文本域、select下拉框等) */
  htmlType?: string
  /** 字典类型 */
  dictType?: string
  /** 默认值 */
  columnDefault?: string
  /** 排序 */
  sort?: number
  /** 创建部门 */
  createDept?: number
  /** 首字母大写的Java字段名 */
  capJavaField?: string
  /** 是否为主键(布尔值) */
  pk?: boolean
  /** 是否自增(布尔值) */
  increment?: boolean
  /** 是否必填(布尔值) */
  required?: boolean
  /** 是否插入(布尔值) */
  insert?: boolean
  /** 是否编辑(布尔值) */
  edit?: boolean
  /** 是否列表(布尔值) */
  list?: boolean
  /** 是否查询(布尔值) */
  query?: boolean
  /** 是否为父类字段 */
  superColumn?: boolean
  /** 是否为可用字段 */
  usableColumn?: boolean
  /** 创建者 */
  createBy?: string
  /** 创建时间 */
  createTime?: string
  /** 更新者 */
  updateBy?: string
  /** 更新时间 */
  updateTime?: string
}

/** 代码生成详情响应对象 */
export interface GenTableDetailVo {
  /** 当前表信息 */
  info: GenTable
  /** 表列配置列表 */
  rows: GenTableColumn[]
  /** 所有表列表 */
  tables: GenTable[]
}

/** 代码生成器配置信息 */
export interface GenConfigVo {
  /** 默认生成方式：0-zip压缩包，1-自定义路径 */
  defaultGenType: string
  /** 后端模块名称 */
  backendModuleName: string
  /** 前端项目根目录 */
  frontendRootDir: string
  /** 作者 */
  author: string
}

/** 代码生成结果 */
export interface CodeGenResult {
  /** 生成是否成功 */
  success: boolean
  /** 生成的文件数量 */
  fileCount?: number
  /** 覆盖的文件数量 */
  overwriteCount?: number
  /** 菜单导入状态 */
  menuImportResult?: string
  /** 错误信息 */
  errorMessage?: string
}
