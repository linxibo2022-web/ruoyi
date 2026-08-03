/**
 * 代码生成器
 */
import type { FormSchema, FormItemSchema, FormItemType } from '../types'
import { hasFormComponents, CONTAINER_TYPES, NON_FORM_COMPONENT_TYPES } from '../types'

/** 组件映射 */
const componentMap: Record<FormItemType, string> = {
  // 表单组件
  input: 'AFormInput',
  textarea: 'AFormInput',
  password: 'AFormInput',
  number: 'AFormInput',
  select: 'AFormSelect',
  radio: 'AFormRadio',
  checkbox: 'AFormCheckbox',
  switch: 'AFormSwitch',
  date: 'AFormDate',
  datetime: 'AFormDate',
  daterange: 'AFormDate',
  datetimerange: 'AFormDate',
  time: 'AFormDate',
  cascader: 'AFormCascader',
  treeSelect: 'AFormTreeSelect',
  imgUpload: 'AFormImgUpload',
  fileUpload: 'AFormFileUpload',
  editor: 'AFormEditor',
  // 卡片组件 - 统计类
  statsCard: 'AStatsCard',
  lineStatsCard: 'ALineStatsCard',
  barStatsCard: 'ABarStatsCard',
  // 卡片组件 - 图表类
  pieChartCard: 'APieChartCard',
  barChartCard: 'ABarChartCard',
  lineChartCard: 'ALineChartCard',
  radarChartCard: 'ARadarChartCard',
  mapChartCard: 'AMapChartCard',
  // 卡片组件 - 数据展示类
  dataCard: 'ADataCard',
  tableCard: 'ATableCard',
  dataListCard: 'ADataListCard',
  activityCard: 'AActivityCard',
  timelineListCard: 'ATimelineListCard',
  // 卡片组件 - 用户信息类
  userCard: 'AUserCard',
  profileCard: 'AProfileCard',
  socialCard: 'ASocialCard',
  // 卡片组件 - 特殊功能类
  formCard: 'AFormCard',
  pricingCard: 'APricingCard',
  imageCard: 'AImageCard',
  infoCard: 'AInfoCard',
  weatherCard: 'AWeatherCard',
  notificationCard: 'ANotificationCard',
  emptyCard: 'AEmptyCard',
  // 图表组件 (独立)
  lineChart: 'ALineChart',
  barChart: 'ABarChart',
  pieChart: 'APieChart',
  radarChart: 'ARadarChart',
  scatterChart: 'AScatterChart',
  mapChart: 'AMapChart',
  // 布局组件
  row: 'el-row',
  col: 'el-col',
  divider: 'el-divider',
  alert: 'el-alert',
  collapse: 'el-collapse'
}

/** 非表单组件类型（从 types 导入） */
const nonFormTypes = NON_FORM_COMPONENT_TYPES

/** 容器组件类型（从 types 导入） */
const containerTypes = CONTAINER_TYPES

/** 代码生成器 */
export function useCodeGenerator() {
  /**
   * 生成表单项代码
   */
  function generateFormItem(item: FormItemSchema, indent: string = '        '): string {
    const component = componentMap[item.type]
    const isNonFormType = nonFormTypes.includes(item.type)
    const isContainer = containerTypes.includes(item.type)

    // 容器组件特殊处理
    if (isContainer) {
      return generateContainerItem(item, indent)
    }

    const props: string[] = []

    // 表单组件需要 v-model 和 label
    if (!isNonFormType) {
      // v-model
      props.push(`v-model="form.${item.prop}"`)

      // 基础属性
      props.push(`label="${item.label}"`)
      props.push(`prop="${item.prop}"`)

      // span
      if (item.span) {
        if (item.span === 'auto') {
          props.push(`span="auto"`)
        } else if (typeof item.span === 'number') {
          props.push(`:span="${item.span}"`)
        }
      }

      // 占位符
      if (item.placeholder) {
        props.push(`placeholder="${item.placeholder}"`)
      }

      // 提示信息
      if (item.tooltip) {
        props.push(`tooltip="${item.tooltip}"`)
      }

      // 类型特殊处理
      if (['textarea', 'password', 'number'].includes(item.type)) {
        props.push(`type="${item.type}"`)
      }

      if (['datetime', 'daterange', 'datetimerange', 'time'].includes(item.type)) {
        props.push(`type="${item.type}"`)
      }
    }

    // 组件特有属性
    if (item.props) {
      // 需要排除的内部属性（不是组件实际支持的 props）
      const excludeProps = ['dictType']
      for (const [key, value] of Object.entries(item.props)) {
        if (value === undefined || value === null) continue
        if (excludeProps.includes(key)) continue // 跳过内部属性
        if (typeof value === 'boolean') {
          if (value) {
            props.push(key)
          }
        } else if (typeof value === 'number') {
          props.push(`:${key}="${value}"`)
        } else if (typeof value === 'string' && value) {
          props.push(`${key}="${value}"`)
        } else if (Array.isArray(value)) {
          props.push(`:${key}="${JSON.stringify(value)}"`)
        }
      }
    }

    // 选项数据（字典类型优先）
    if (['select', 'radio', 'checkbox'].includes(item.type)) {
      if (item.props?.dictType) {
        // 字典类型：使用 useDict 获取的字典数据
        props.push(`:options="${item.props.dictType}"`)
      } else if (item.options) {
        props.push(`:options="${item.prop}Options"`)
      }
    }

    // 必填
    if (item.required && !isNonFormType) {
      // 通过 rules 实现，不需要单独属性
    }

    // 禁用
    if (item.disabled === true) {
      props.push('disabled')
    }

    const propsStr = props.join('\n' + indent + '  ')
    return `${indent}<${component}\n${indent}  ${propsStr}\n${indent}/>`
  }

  /**
   * 生成容器组件代码 (el-row / el-col)
   */
  function generateContainerItem(item: FormItemSchema, indent: string = '        '): string {
    const isRow = item.type === 'row'
    const component = isRow ? 'el-row' : 'el-col'
    const props: string[] = []

    if (isRow) {
      // el-row 属性
      if (item.props?.gutter) {
        props.push(`:gutter="${item.props.gutter}"`)
      }
      if (item.props?.justify && item.props.justify !== 'start') {
        props.push(`justify="${item.props.justify}"`)
      }
      if (item.props?.align && item.props.align !== 'top') {
        props.push(`align="${item.props.align}"`)
      }
    } else {
      // el-col 属性
      const span = item.props?.span || 12
      props.push(`:span="${span}"`)
      if (item.props?.offset) {
        props.push(`:offset="${item.props.offset}"`)
      }
      if (item.props?.push) {
        props.push(`:push="${item.props.push}"`)
      }
      if (item.props?.pull) {
        props.push(`:pull="${item.props.pull}"`)
      }
    }

    // 生成子组件代码
    const childIndent = indent + '  '
    let childrenCode = ''

    if (item.children && item.children.length > 0) {
      if (isRow) {
        // el-row 里面包 el-col
        childrenCode = item.children
          .map((child) => {
            // 如果子组件是 col，直接生成；否则包一层 el-col
            if (child.type === 'col') {
              return generateContainerItem(child, childIndent)
            } else {
              // 普通组件包一层 el-col
              const childSpan = typeof child.span === 'number' ? child.span : 12
              const innerContent = generateFormItem(child, childIndent + '  ')
              return `${childIndent}<el-col :span="${childSpan}">\n${innerContent}\n${childIndent}</el-col>`
            }
          })
          .join('\n')
      } else {
        // el-col 直接渲染子组件
        childrenCode = item.children.map((child) => generateFormItem(child, childIndent)).join('\n')
      }
    } else {
      // 空容器注释
      childrenCode = `${childIndent}<!-- ${isRow ? '行容器' : '列容器'}内容 -->`
    }

    const propsStr = props.length > 0 ? ' ' + props.join(' ') : ''
    return `${indent}<${component}${propsStr}>\n${childrenCode}\n${indent}</${component}>`
  }

  /**
   * 生成模板代码
   */
  function generateTemplate(schema: FormSchema): string {
    const hasForm = hasFormComponents(schema.items)
    const items = schema.items.map((item) => generateFormItem(item, hasForm ? '        ' : '      ')).join('\n')

    if (schema.layout === 'page') {
      // 有表单组件 - 使用 el-form 包裹
      if (hasForm) {
        return `<template>
  <div class="app-container">
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="${schema.labelWidth}"
      label-position="${schema.labelPosition}"
    >
      <el-row :gutter="${schema.gutter || 20}">
${items}
      </el-row>
      <el-form-item>
        <el-button type="primary" @click="handleSubmit">提交</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>`
      }
      // 无表单组件 - 直接渲染卡片/图表等
      return `<template>
  <div class="app-container">
    <div class="page-grid">
${items}
    </div>
  </div>
</template>`
    }

    const modalPropsLines =
      schema.layout === 'drawer'
        ? `mode="drawer"\n    direction="${schema.drawerDirection || 'rtl'}"`
        : `size="${schema.dialogSize || 'medium'}"`

    // 有表单组件 - 使用 el-form 包裹
    if (hasForm) {
      return `<template>
  <AModal
    v-model="dialog.visible"
    :title="dialog.title"
    ${modalPropsLines}
    :loading="buttonLoading"
    @confirm="submitForm"
    @cancel="cancel"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="${schema.labelWidth}"
    >
      <el-row :gutter="${schema.gutter || 20}">
${items}
      </el-row>
    </el-form>
  </AModal>
</template>`
    }

    // 无表单组件 - 直接渲染卡片/图表等
    return `<template>
  <AModal
    v-model="dialog.visible"
    :title="dialog.title"
    ${modalPropsLines}
    :show-footer="false"
  >
    <div class="modal-grid">
${items}
    </div>
  </AModal>
</template>`
  }

  /**
   * 生成脚本代码
   */
  function generateScript(schema: FormSchema): string {
    const hasForm = hasFormComponents(schema.items)
    const pascalName = toPascalCase(schema.name)
    const camelName = pascalName.charAt(0).toLowerCase() + pascalName.slice(1)

    // 递归收集所有表单项
    const formItems = collectFormItems(schema.items)

    const isModal = schema.layout !== 'page'

    // 无表单组件时，生成简化的脚本
    if (!hasForm) {
      if (isModal) {
        return `<script setup lang="ts">
// =========== 弹窗相关 ===========

/**对话框配置对象*/
const dialog = ref<DialogState>({
  visible: false,
  title: ''
})

/** 打开弹窗 */
const handleOpen = (title: string = '${schema.name}') => {
  dialog.value.visible = true
  dialog.value.title = title
}

/** 关闭弹窗 */
const handleClose = () => {
  dialog.value.visible = false
}

// 暴露方法给父组件
defineExpose({
  handleOpen,
  handleClose
})
</script>`
      }
      // 页面模式 - 无表单组件
      return `<script setup lang="ts">
// =========== 页面组件 ===========
// 此页面仅包含展示组件，无表单逻辑
</script>`
    }

    // 有表单组件时，生成完整的表单脚本
    // 生成初始表单数据
    const initFormFields = formItems
      .map((item) => {
        const defaultValue = getDefaultValue(item)
        return `  ${item.prop}: ${defaultValue}`
      })
      .join(',\n')

    // 生成校验规则
    const rules = formItems
      .filter((item) => item.required)
      .map((item) => {
        const trigger = ['select', 'radio', 'checkbox', 'date', 'datetime', 'daterange', 'datetimerange', 'time', 'cascader', 'treeSelect'].includes(item.type)
          ? 'change'
          : 'blur'
        return `  ${item.prop}: [{ required: true, message: '请${getActionText(item.type)}${item.label}', trigger: '${trigger}' }]`
      })
      .join(',\n')

    // 生成选项数据（排除使用字典类型的组件）
    const optionsCode = formItems
      .filter((item) => ['select', 'radio', 'checkbox'].includes(item.type) && item.options?.length && !item.props?.dictType)
      .map((item) => {
        const options = JSON.stringify(item.options, null, 2)
          .split('\n')
          .map((line, index) => (index === 0 ? line : '  ' + line))
          .join('\n')
        return `const ${item.prop}Options = ref(${options})`
      })
      .join('\n\n')

    // 收集使用字典的组件
    const dictItems = formItems.filter((item) => ['select', 'radio', 'checkbox'].includes(item.type) && item.props?.dictType)
    const dictTypes = [...new Set(dictItems.map((item) => item.props!.dictType))]

    // 生成字典导入代码
    const dictImport = dictTypes.length > 0 ? `import { useDict } from '@/composables/useDict'` : ''

    // 生成字典解构代码（直接使用字典类型字符串，不依赖 DictTypes 枚举）
    const dictDestructure =
      dictTypes.length > 0 ? `const { ${dictTypes.join(', ')} } = useDict(${dictTypes.map((t) => `'${t}'`).join(', ')})` : ''

    if (isModal) {
      return `<script setup lang="ts">
import { toValidate } from '@/utils/to'
import { showMsgSuccess } from '@/utils/modal'
${dictImport}
// import { add${pascalName}, update${pascalName} } from '@/api/xxx/${camelName}Api'
// import type { ${pascalName}Bo } from '@/api/xxx/${camelName}Types'

// =========== 表单相关 ===========
${dictDestructure ? `\n// 字典数据\n${dictDestructure}\n` : ''}

/**初始表单数据*/
const initFormData = {
${initFormFields}
}

/**表单引用*/
const formRef = ref<ElFormInstance>()
/**表单提交按钮加载状态*/
const buttonLoading = ref(false)
/**对话框配置对象*/
const dialog = ref<DialogState>({
  visible: false,
  title: ''
})
/**表单数据对象*/
const form = ref({ ...initFormData })
/**表单校验规则*/
const rules = ref<ElFormRules>({
${rules}
})
${optionsCode ? `\n// 选项数据\n${optionsCode}` : ''}

/** 表单重置 */
const reset = () => {
  form.value = { ...initFormData }
  formRef.value?.resetFields()
}

/** 取消编辑 */
const cancel = () => {
  reset()
  dialog.value.visible = false
}

/** 新增操作 */
const handleAdd = () => {
  reset()
  dialog.value.visible = true
  dialog.value.title = '新增${schema.name}'
}

/** 修改操作 */
const handleUpdate = async (row: any) => {
  reset()
  // TODO: 调用接口获取详情
  // const [err, data] = await get${pascalName}(row.id)
  // if (!err) {
  //   Object.assign(form.value, data)
  //   dialog.value.visible = true
  //   dialog.value.title = '修改${schema.name}'
  // }
  Object.assign(form.value, row)
  dialog.value.visible = true
  dialog.value.title = '修改${schema.name}'
}

/** 提交表单 */
const submitForm = async () => {
  const [validateErr] = await toValidate(formRef)
  if (validateErr) return

  buttonLoading.value = true
  // TODO: 调用接口保存数据
  // let err: Error | null
  // if (form.value.id) {
  //   ;[err] = await update${pascalName}(form.value)
  // } else {
  //   ;[err] = await add${pascalName}(form.value)
  // }
  // if (!err) {
  //   showMsgSuccess(form.value.id ? '修改成功' : '新增成功')
  //   dialog.value.visible = false
  //   // 刷新列表
  // }
  console.log('表单数据:', form.value)
  showMsgSuccess('保存成功')
  dialog.value.visible = false
  buttonLoading.value = false
}

// 暴露方法给父组件
defineExpose({
  handleAdd,
  handleUpdate
})
</script>`
    }

    // 页面模式
    return `<script setup lang="ts">
import { toValidate } from '@/utils/to'
import { showMsgSuccess } from '@/utils/modal'
${dictImport}
// import { add${pascalName} } from '@/api/xxx/${camelName}Api'
// import type { ${pascalName}Bo } from '@/api/xxx/${camelName}Types'

// =========== 表单相关 ===========
${dictDestructure ? `\n// 字典数据\n${dictDestructure}\n` : ''}
/**初始表单数据*/
const initFormData = {
${initFormFields}
}

/**表单引用*/
const formRef = ref<ElFormInstance>()
/**表单提交按钮加载状态*/
const isLoading = ref(false)
/**表单数据对象*/
const form = ref({ ...initFormData })
/**表单校验规则*/
const rules = ref<ElFormRules>({
${rules}
})
${optionsCode ? `\n// 选项数据\n${optionsCode}` : ''}

/** 重置表单 */
const handleReset = () => {
  form.value = { ...initFormData }
  formRef.value?.resetFields()
}

/** 提交表单 */
const handleSubmit = async () => {
  const [validateErr] = await toValidate(formRef)
  if (validateErr) return

  isLoading.value = true
  // TODO: 调用接口保存数据
  // const [err] = await add${pascalName}(form.value)
  // if (!err) {
  //   showMsgSuccess('保存成功')
  //   handleReset()
  // }
  console.log('表单数据:', form.value)
  showMsgSuccess('保存成功')
  isLoading.value = false
}
</script>`
  }

  /**
   * 生成样式代码
   */
  function generateStyle(schema: FormSchema): string {
    const hasForm = hasFormComponents(schema.items)

    if (schema.layout === 'page') {
      if (hasForm) {
        return `<style scoped lang="scss">
.app-container {
  padding: 20px;
}
</style>`
      }
      // 无表单组件 - 添加网格布局样式
      return `<style scoped lang="scss">
.app-container {
  padding: 20px;

  .page-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
  }
}
</style>`
    }

    // 弹窗模式，无表单组件时添加网格样式
    if (!hasForm) {
      return `<style scoped lang="scss">
.modal-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}
</style>`
    }

    return ''
  }

  /**
   * 生成类型定义
   */
  function generateTypes(schema: FormSchema): string {
    const hasForm = hasFormComponents(schema.items)

    // 无表单组件时不生成类型定义
    if (!hasForm) {
      return `// 此页面仅包含展示组件，无需表单类型定义`
    }

    // 递归收集所有表单项
    const formItems = collectFormItems(schema.items)

    const fields = formItems
      .map((item) => {
        const type = getTypeScriptType(item)
        const optional = item.required ? '' : '?'
        return `  ${item.prop}${optional}: ${type}`
      })
      .join('\n')

    return `/** ${schema.name}表单类型 */
export interface ${toPascalCase(schema.name)}Form {
${fields}
}`
  }

  /**
   * 生成完整代码
   */
  function generateFullCode(schema: FormSchema): string {
    const template = generateTemplate(schema)
    const script = generateScript(schema)
    const style = generateStyle(schema)

    return [template, script, style].filter(Boolean).join('\n\n')
  }

  /**
   * 生成 JSON 配置
   */
  function generateJsonConfig(schema: FormSchema): string {
    return JSON.stringify(schema, null, 2)
  }

  return {
    generateTemplate,
    generateScript,
    generateStyle,
    generateTypes,
    generateFullCode,
    generateJsonConfig
  }
}

// ==================== 辅助函数 ====================

/** 递归收集所有表单项（排除容器组件） */
function collectFormItems(items: FormItemSchema[]): FormItemSchema[] {
  const result: FormItemSchema[] = []
  for (const item of items) {
    // 容器组件不加入表单项，但递归收集其子组件
    if (containerTypes.includes(item.type)) {
      if (item.children && item.children.length > 0) {
        result.push(...collectFormItems(item.children))
      }
    } else if (!nonFormTypes.includes(item.type)) {
      // 只收集表单组件
      result.push(item)
    }
  }
  return result
}

/** 获取默认值 */
function getDefaultValue(item: FormItemSchema): string {
  if (item.defaultValue !== undefined) {
    if (typeof item.defaultValue === 'string') {
      return `'${item.defaultValue}'`
    }
    return JSON.stringify(item.defaultValue)
  }

  switch (item.type) {
    case 'checkbox':
    case 'daterange':
    case 'datetimerange':
      return '[]'
    case 'switch':
      return 'false'
    case 'number':
      return 'null'
    default:
      return "''"
  }
}

/** 获取操作文本 */
function getActionText(type: FormItemType): string {
  const selectTypes = ['select', 'radio', 'checkbox', 'date', 'datetime', 'daterange', 'datetimerange', 'time', 'cascader', 'treeSelect']
  return selectTypes.includes(type) ? '选择' : '输入'
}

/** 获取 TypeScript 类型 */
function getTypeScriptType(item: FormItemSchema): string {
  switch (item.type) {
    case 'number':
      return 'number | null'
    case 'switch':
      return 'boolean'
    case 'checkbox':
      return 'string[]'
    case 'daterange':
    case 'datetimerange':
      return '[string, string] | []'
    case 'imgUpload':
    case 'fileUpload':
      return 'string | string[]'
    default:
      return 'string'
  }
}

/** 转换为 PascalCase */
function toPascalCase(str: string): string {
  return str
    .replace(/[^a-zA-Z0-9\u4e00-\u9fa5]/g, ' ')
    .split(' ')
    .filter(Boolean)
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join('')
}
