<!-- 生成信息配置 -->
<template>
  <el-form ref="genInfoFormRef" :model="infoForm" :rules="rules" label-width="auto">
    <el-row>
      <AFormSelect
        label="生成模板"
        v-model="infoForm.tplCategory"
        prop="tplCategory"
        :options="templateOptions"
        :span="12"
        @change="tplSelectChange"
      ></AFormSelect>

      <AFormInput
        label="生成包路径"
        v-model="infoForm.packageName"
        prop="packageName"
        placeholder="例如: com.ruoyi.system"
        tooltip="生成在哪个java包下，例如 com.ruoyi.system"
        :span="12"
      ></AFormInput>

      <AFormInput
        label="生成模块名"
        v-model="infoForm.moduleName"
        prop="moduleName"
        placeholder="例如: system"
        tooltip="可理解为子系统名，例如 system"
        :span="12"
      ></AFormInput>

      <AFormInput
        label="生成业务名"
        v-model="infoForm.businessName"
        prop="businessName"
        placeholder="例如: user"
        tooltip="可理解为功能英文名，例如 user"
        :span="12"
      ></AFormInput>

      <AFormInput
        label="生成功能名"
        v-model="infoForm.functionName"
        prop="functionName"
        placeholder="例如: 广告配置"
        tooltip="用作类描述，菜单名称等，例如 广告配置"
        :span="12"
      ></AFormInput>

      <AFormTreeSelect
        label="上级菜单"
        v-model="infoForm.parentMenuId"
        :data="menuOptions"
        :props="{ value: 'menuId', label: 'menuName', children: 'children' }"
        tooltip="分配到指定菜单下，例如 系统管理"
        :span="12"
      ></AFormTreeSelect>

      <el-col :span="12">
        <el-form-item :label="t('Menu Icon', '菜单图标')">
          <IconSelect v-model="infoForm.menuIcon" />
        </el-form-item>
      </el-col>

      <AFormInput
        label="菜单顺序"
        v-model="infoForm.menuOrder"
        type="number"
        :min="0"
        :step="10"
        width="200px"
        prop="menuOrder"
        placeholder="请输入菜单顺序"
        tooltip="数值越小越靠前，默认为1"
        :span="12"
      ></AFormInput>

      <AFormRadio
        :label="t('Gen Code Type', '生成代码方式')"
        v-model="infoForm.genType"
        :options="genTypeOptions"
        :tooltip="t('Default is zip download, can also use custom path', '默认为zip压缩包下载，也可以自定义生成路径')"
        :span="24"
      ></AFormRadio>

      <!-- 只有自定义路径生成时才显示自动导入菜单选项 -->
      <AFormRadio
        v-if="infoForm.genType == '1'"
        :label="t('Auto Import Menu', '自动导入菜单')"
        v-model="infoForm.autoImportMenu"
        :options="autoImportMenuOptions"
        :tooltip="t('Auto import menu SQL when generating (only works for current project, existing menus will be skipped)', '生成代码时自动将菜单SQL导入数据库（仅当路径为当前项目时生效，已存在的菜单会自动跳过）')"
        :span="24"
      ></AFormRadio>

      <AFormInput
        v-if="infoForm.genType == '1'"
        :label="t('Backend Module', '后端模块')"
        v-model="infoForm.backendModuleName"
        :placeholder="t('e.g. ruoyi-business, ruoyi-system', '如: ruoyi-business、ruoyi-system')"
        :tooltip="t('Target backend module directory, leave empty for global default', '代码生成到哪个后端模块目录，留空使用全局默认配置')"
        :span="12"
      ></AFormInput>

      <AFormInput
        v-if="infoForm.genType == '1'"
        :label="t('Frontend Project', '前端项目')"
        v-model="infoForm.frontendRootDir"
        :placeholder="t('e.g. plus-ui, plus-uniapp, plus-app', '如: plus-ui、plus-uniapp、plus-app')"
        :tooltip="t('Target frontend project directory, leave empty for global default', '代码生成到哪个前端项目目录，留空使用全局默认配置')"
        :span="12"
      ></AFormInput>

      <AFormInput
        v-if="infoForm.genType == '1'"
        label="自定义路径"
        v-model="infoForm.genPath"
        prop="genPath"
        placeholder="填写项目根目录绝对路径，如: D:/workspace/ruoyi-plus-uniapp"
        tooltip="填写项目根目录，代码将自动分发到: 后端(ruoyi-modules/ruoyi-business/)、前端(plus-ui/src/)、SQL(script/menu/)"
        :span="24"
      >
        <template #label>
          <span>{{ t('Custom Path', '自定义路径') }}</span>
          <el-tag type="warning" size="small" style="margin-left: 10px">⚠️ {{ t('Commit code before overwrite!', '覆盖前请先提交代码!') }}</el-tag>
        </template>
        <template #append>
          <el-dropdown>
            <el-button type="primary">
              {{ t('Quick Path Selection', '最近路径快速选择') }}
              <el-icon class="el-icon--right">
                <arrow-down />
              </el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="infoForm.genPath = '/'">{{ t('Reset to default path', '恢复默认的生成基础路径') }}</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </AFormInput>

      <!-- 路径预览提示 -->
      <el-col v-if="infoForm.genType == '1'" :span="24">
        <el-alert type="info" :closable="false" style="margin-bottom: 20px">
          <template #title>
            <strong>📂 {{ t('Path Preview', '生成路径预览') }}</strong>
          </template>
          <div style="font-size: 13px; line-height: 2.2; padding: 5px 0">
            <div>
              • <strong>{{ t('Backend Code', '后端代码') }}</strong>:
              <code style="font-size: 13px">{{ getFullPath() }}ruoyi-modules/{{ effectiveBackendModule }}/src/main/java/...</code>
            </div>
            <div>
              • <strong>{{ t('Frontend API', '前端API') }}</strong>:
              <code style="font-size: 13px"
                >{{ getFullPath() }}{{ effectiveFrontendDir }}/src/api/{{ getFrontendPath() }}/{{ infoForm.businessName }}/</code
              >
            </div>
            <div>
              • <strong>{{ t('Frontend Pages', '前端页面') }}</strong>:
              <code style="font-size: 13px"
                >{{ getFullPath() }}{{ effectiveFrontendDir }}/src/views/{{ getFrontendPath() }}/{{ infoForm.businessName }}/</code
              >
            </div>
            <div>
              • <strong>{{ t('Menu SQL', '菜单SQL') }}</strong>:
              <code style="font-size: 13px">{{ getFullPath() }}script/menu/{{ getSqlFileName() }}</code>
            </div>
          </div>
        </el-alert>
      </el-col>
    </el-row>

    <!-- 树表配置 -->
    <template v-if="infoForm.tplCategory == 'tree'">
      <h4 class="form-header">{{ t('Other Info', '其他信息') }}</h4>
      <el-row>
        <AFormSelect
          label="树编码字段"
          v-model="infoForm.treeCode"
          :options="columnOptions"
          placeholder="请选择树编码字段"
          tooltip="树显示的编码字段名， 如：dept_id"
          :span="12"
        ></AFormSelect>

        <AFormSelect
          label="树父编码字段"
          v-model="infoForm.treeParentCode"
          :options="columnOptions"
          placeholder="请选择树父编码字段"
          tooltip="树显示的父编码字段名， 如：parent_Id"
          :span="12"
        ></AFormSelect>

        <AFormSelect
          label="树名称字段"
          v-model="infoForm.treeName"
          :options="columnOptions"
          placeholder="请选择树名称字段"
          tooltip="树节点的显示名称字段名， 如：dept_name"
          :span="12"
        ></AFormSelect>
      </el-row>
    </template>

    <!-- 关联表配置 -->
    <template v-if="infoForm.tplCategory == 'sub'">
      <h4 class="form-header">{{ t('Relation Info', '关联信息') }}</h4>
      <el-row>
        <AFormSelect
          :label="t('Sub Table Name', '关联子表的表名')"
          v-model="infoForm.subTableName"
          :options="tableOptions"
          :placeholder="t('Please select sub table', '请选择关联子表')"
          :tooltip="t('Sub table name, e.g. sys_user', '关联子表的表名， 如：sys_user')"
          :span="12"
          @change="subSelectChange"
        ></AFormSelect>

        <AFormSelect
          :label="t('Sub Table FK Name', '子表关联的外键名')"
          v-model="infoForm.subTableFkName"
          :options="subColumnOptions"
          :placeholder="t('Please select FK field', '请选择外键字段')"
          :tooltip="t('Sub table FK name, e.g. user_id', '子表关联的外键名， 如：user_id')"
          :span="12"
        ></AFormSelect>
      </el-row>
    </template>
  </el-form>
</template>

<script setup lang="ts">
import { listMenus } from '@/api/system/core/menu/menuApi'
import { getGenConfig } from '@/api/tool/gen/genApi'
import type { GenConfigVo } from '@/api/tool/gen/genTypes'
import { buildTree } from '@/utils/tree'
import IconSelect from '@/components/Icon/IconSelect.vue'
const { t } = useI18n()

// =========== 类型定义 ===========
/**
 * 菜单选项类型定义
 */
interface MenuOptionsType {
  /** 菜单ID */
  menuId: number | string
  /** 菜单名称 */
  menuName: string
  /** 子菜单 */
  children?: MenuOptionsType[]
}

/**
 * 代码生成表单Props接口
 */
interface GenInfoFormProps {
  /** 代码生成信息对象 */
  info: Partial<{
    tplCategory: string
    packageName: string
    moduleName: string
    businessName: string
    functionName: string
    parentMenuId: string | number
    genType: string
    genPath: string
    treeCode: string
    treeParentCode: string
    treeName: string
    subTableName: string
    subTableFkName: string
    columns: any[]
    [key: string]: any
  }>
  /** 可用于生成的表列表 */
  tables: any[]
}

// =========== Props 定义 ===========
const props = withDefaults(defineProps<GenInfoFormProps>(), {
  info: () => ({}),
  tables: () => []
})

// =========== 表单相关 ===========
/**表单引用*/
const genInfoFormRef = ref<ElFormInstance>()

/**响应式表单数据*/
const infoForm = computed(() => props.info || {})

/**可用表格列表*/
const table = computed(() => props.tables)

/**表单校验规则*/
const rules = computed<ElFormRules>(() => ({
  tplCategory: [{ required: true, message: t('Please select template', '请选择生成模板'), trigger: 'change' }],
  packageName: [{ required: true, message: t('Package name is required', '生成包路径不能为空'), trigger: 'blur' }],
  moduleName: [{ required: true, message: t('Module name is required', '生成模块名不能为空'), trigger: 'blur' }],
  businessName: [{ required: true, message: t('Business name is required', '生成业务名不能为空'), trigger: 'blur' }],
  functionName: [{ required: true, message: t('Function name is required', '生成功能名不能为空'), trigger: 'blur' }]
}))

// =========== 选项数据 ===========
/**子表列信息*/
const subColumns = ref<any[]>([])
/**菜单选项*/
const menuOptions = ref<MenuOptionsType[]>([])

/**模板类型选项*/
const templateOptions = computed(() => [
  { label: t('Single Table (CRUD)', '单表（增删改查）'), value: 'crud' },
  { label: t('Tree Table (CRUD)', '树表（增删改查）'), value: 'tree' },
  { label: t('Master-Detail Table (CRUD)', '主子表（增删改查）'), value: 'sub' }
])

/**自动导入菜单选项*/
const autoImportMenuOptions = computed(() => [
  { label: t('No', '否'), value: '0' },
  { label: t('Yes', '是'), value: '1' }
])

/**生成方式选项*/
const genTypeOptions = computed(() => [
  { label: t('Zip Package', 'zip压缩包'), value: '0' },
  { label: t('Custom Path', '自定义路径'), value: '1' }
])

// =========== 计算属性 ===========
/**字段选项*/
const columnOptions = computed(() => {
  const columns = infoForm.value.columns || []
  return columns.map((column: any) => ({
    label: `${column.columnName}：${column.columnComment}`,
    value: column.columnName
  }))
})

/**表选项*/
const tableOptions = computed(() => {
  return table.value.map((t: any) => ({
    label: `${t.tableName}：${t.tableComment}`,
    value: t.tableName
  }))
})

/**子表字段选项*/
const subColumnOptions = computed(() => {
  return subColumns.value.map((column: any) => ({
    label: `${column.columnName}：${column.columnComment}`,
    value: column.columnName
  }))
})

/**有效的后端模块名（优先使用表级配置，否则使用全局配置）*/
const effectiveBackendModule = computed(() => {
  return infoForm.value.backendModuleName || genConfig.value.backendModuleName
})

/**有效的前端项目目录（优先使用表级配置，否则使用全局配置）*/
const effectiveFrontendDir = computed(() => {
  return infoForm.value.frontendRootDir || genConfig.value.frontendRootDir
})

// =========== 事件处理 ===========
/**
 * 子表选择变更事件处理
 * 清空子表外键名
 */
const subSelectChange = (value: string) => {
  infoForm.value.subTableFkName = ''
  if (value) {
    setSubTableColumns(value)
  }
}

/**
 * 模板类型变更事件处理
 * @param value 选中的模板类型
 */
const tplSelectChange = (value: string) => {
  if (value !== 'sub') {
    infoForm.value.subTableName = ''
    infoForm.value.subTableFkName = ''
  }
}

/**
 * 设置子表列信息
 * @param value 表名
 */
const setSubTableColumns = (value: string) => {
  const matchedTable = table.value.find((item: any) => item.tableName === value)
  if (matchedTable) {
    subColumns.value = matchedTable.columns || []
  }
}

/**
 * 获取菜单树选择数据
 */
const getMenuTreeselect = async () => {
  const [err, data] = await listMenus()
  if (!err && data) {
    menuOptions.value = []
    const menu: MenuOptionsType = { menuId: 0, menuName: t('Root Menu', '主类目'), children: [] }
    menu.children = buildTree<MenuOptionsType>(data, { id: 'menuId' })
    menuOptions.value.push(menu)
  }
}

/**
 * 代码生成器配置信息
 */
const genConfig = ref<GenConfigVo>({
  defaultGenType: '0',
  backendModuleName: 'ruoyi-business',
  frontendRootDir: 'plus-ui',
  author: '抓蛙师'
})

/**
 * 获取代码生成器配置
 */
const getGeneratorConfig = async () => {
  const [err, data] = await getGenConfig()
  if (!err && data) {
    genConfig.value = data
  }
}

/**
 * 获取完整路径前缀
 * 如果配置了具体路径（非 /），则返回该路径，否则返回空
 */
const getFullPath = () => {
  const genPath = infoForm.value.genPath || '/'
  if (genPath === '/') {
    return ''
  }
  // 确保路径以分隔符结尾
  return genPath.endsWith('/') || genPath.endsWith('\\') ? genPath : genPath + '/'
}

/**
 * 获取前端路径预览
 * 从包名提取前端路径，跳过前两级域名
 */
const getFrontendPath = () => {
  const packageName = infoForm.value.packageName || ''
  if (!packageName) return 'business'

  const parts = packageName.split('.')
  if (parts.length > 2) {
    return parts.slice(2).join('/')
  }
  return infoForm.value.moduleName || 'business'
}

/**
 * 获取SQL文件名预览
 * 格式: {moduleName}_{subModule}_{businessName}_menu.sql
 */
const getSqlFileName = () => {
  const packageName = infoForm.value.packageName || ''
  const businessName = infoForm.value.businessName || 'xxx'
  const nameParts: string[] = []

  if (packageName) {
    const parts = packageName.split('.')
    // 跳过前两级域名，提取剩余部分
    if (parts.length > 2) {
      for (let i = 2; i < parts.length; i++) {
        nameParts.push(parts[i])
      }
    }
  }

  nameParts.push(businessName)
  nameParts.push('menu')

  return nameParts.join('_') + '.sql'
}

// =========== 监听器 ===========
/**监听子表名变化*/
watch(
  () => props.info.subTableName,
  (val) => {
    if (val) {
      setSubTableColumns(val)
    }
  }
)

// =========== 生命周期 ===========
onMounted(() => {
  getMenuTreeselect()
  getGeneratorConfig()
})

// =========== 暴露方法 ===========
defineExpose({
  $refs: {
    genInfoForm: genInfoFormRef
  }
})
</script>

<style lang="scss" scoped>
/* 表单头部样式 */
.form-header {
  font-size: 15px; // 字体大小
  color: #6379bb; // 文字颜色
  border-bottom: 1px solid #ddd; // 底部边框
  margin: 8px 10px 25px 10px; // 外边距
  padding-bottom: 5px; // 底部内边距
}
</style>
