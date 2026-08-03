<!-- 预览对话框 -->
<template>
  <!-- 内部预览弹窗（放在最外层避免嵌套问题） -->
  <AModal
    v-model="showInnerDialog"
    :title="dialogSettings.title || (hasFormItems ? '表单弹窗' : '页面弹窗')"
    :mode="dialogSettings.type"
    :size="dialogSettings.size"
    :show-footer="hasFormItems && dialogSettings.showFooter"
    @confirm="handleDialogConfirm"
  >
    <!-- 表单模式弹窗内容 -->
    <el-form
      v-if="hasFormItems"
      ref="innerFormRef"
      :model="formData"
      :rules="rules"
      :label-width="schema.labelWidth"
      :label-position="schema.labelPosition"
    >
      <el-row :gutter="schema.gutter || 20">
        <template v-for="item in schema.items" :key="item.id">
          <!-- 行容器特殊处理 -->
          <el-col v-if="item.type === 'row'" :span="24">
            <el-row :gutter="item.props?.gutter || 20">
              <template v-for="child in item.children" :key="child.id">
                <el-col :span="child.type === 'col' ? (child.props?.span || 12) : getSpan(child)">
                  <ComponentRenderer :item="child" :form-data="formData" :preview-mode="true" />
                </el-col>
              </template>
            </el-row>
          </el-col>
          <!-- 普通组件 -->
          <el-col v-else :span="getSpan(item)">
            <ComponentRenderer :item="item" :form-data="formData" :preview-mode="true" />
          </el-col>
        </template>
      </el-row>
    </el-form>

    <!-- 非表单模式弹窗内容（卡片、图表等） -->
    <div v-else class="dialog-canvas-preview">
      <template v-for="item in schema.items" :key="item.id">
        <!-- 行容器：使用 el-row/el-col 栅格系统 -->
        <el-row v-if="item.type === 'row'" :gutter="item.props?.gutter || 16" class="preview-row">
          <template v-for="child in item.children" :key="child.id">
            <el-col :span="child.type === 'col' ? (child.props?.span || 12) : 24">
              <!-- col 容器内部渲染子组件 -->
              <template v-if="child.type === 'col' && child.children?.length">
                <ComponentRenderer
                  v-for="grandChild in child.children"
                  :key="grandChild.id"
                  :item="grandChild"
                  :form-data="formData"
                  :preview-mode="true"
                />
              </template>
              <!-- 非 col 组件直接渲染 -->
              <ComponentRenderer v-else :item="child" :form-data="formData" :preview-mode="true" />
            </el-col>
          </template>
        </el-row>
        <!-- 普通组件：使用 el-row 包裹 -->
        <el-row v-else :gutter="16" class="preview-row">
          <el-col :span="getSpan(item)">
            <ComponentRenderer :item="item" :form-data="formData" :preview-mode="true" />
          </el-col>
        </el-row>
      </template>
    </div>
  </AModal>

  <!-- 主预览弹窗 -->
  <AModal v-model="visible" :title="previewTitle" size="xl" :show-footer="false" :destroy-on-close="false">
    <div class="preview-wrapper">
      <!-- 左侧设置面板 -->
      <div class="preview-settings">
        <div class="settings-title">预览模式</div>
        <el-radio-group v-model="previewMode" class="settings-radio">
          <el-radio value="page">页面</el-radio>
          <el-radio value="dialog">弹窗</el-radio>
        </el-radio-group>

        <template v-if="previewMode === 'dialog'">
          <el-divider />
          <div class="settings-title">弹窗设置</div>
          <el-form label-position="top" size="small">
            <el-form-item label="弹窗标题">
              <el-input v-model="dialogSettings.title" :placeholder="hasFormItems ? '表单弹窗' : '页面弹窗'" />
            </el-form-item>
            <el-form-item label="弹窗类型">
              <el-radio-group v-model="dialogSettings.type">
                <el-radio value="dialog">对话框</el-radio>
                <el-radio value="drawer">抽屉</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="弹窗尺寸">
              <el-select v-model="dialogSettings.size" class="w-full">
                <el-option label="小" value="small" />
                <el-option label="中" value="medium" />
                <el-option label="大" value="large" />
                <el-option label="超大" value="xl" />
              </el-select>
            </el-form-item>
            <el-form-item v-if="hasFormItems" label="显示底部">
              <el-switch v-model="dialogSettings.showFooter" />
            </el-form-item>
          </el-form>
        </template>
      </div>

      <!-- 右侧预览区域 -->
      <div class="preview-content">
        <el-alert
          :title="hasFormItems ? '预览模式：表单仅供预览，数据不会提交' : '预览模式：页面组件预览'"
          type="info"
          :closable="false"
          show-icon
          class="mb-4"
        />

        <!-- 表单模式 - 页面预览 -->
        <template v-if="hasFormItems && previewMode === 'page'">
          <div class="page-preview">
            <el-form
              ref="formRef"
              :model="formData"
              :rules="rules"
              :label-width="schema.labelWidth"
              :label-position="schema.labelPosition"
            >
              <el-row :gutter="schema.gutter || 20">
                <template v-for="item in schema.items" :key="item.id">
                  <!-- 行容器特殊处理 -->
                  <el-col v-if="item.type === 'row'" :span="24">
                    <el-row :gutter="item.props?.gutter || 20">
                      <template v-for="child in item.children" :key="child.id">
                        <el-col :span="child.type === 'col' ? (child.props?.span || 12) : getSpan(child)">
                          <ComponentRenderer :item="child" :form-data="formData" :preview-mode="true" />
                        </el-col>
                      </template>
                    </el-row>
                  </el-col>
                  <!-- 普通组件 -->
                  <el-col v-else :span="getSpan(item)">
                    <ComponentRenderer :item="item" :form-data="formData" :preview-mode="true" />
                  </el-col>
                </template>
              </el-row>
            </el-form>
          </div>
        </template>

        <!-- 弹窗预览触发区域（表单和非表单通用） -->
        <template v-else-if="previewMode === 'dialog'">
          <div class="dialog-preview-trigger">
            <el-button type="primary" @click="openInnerDialog">
              点击打开{{ dialogSettings.type === 'drawer' ? '抽屉' : '弹窗' }}
            </el-button>
            <p class="trigger-tip">{{ hasFormItems ? '表单将在弹窗中展示' : '页面组件将在弹窗中展示' }}</p>
          </div>
        </template>

        <!-- 非表单模式 - 页面组件预览（卡片、图表等） -->
        <template v-else-if="!hasFormItems && previewMode === 'page'">
          <div class="page-preview canvas-preview">
            <template v-for="item in schema.items" :key="item.id">
              <!-- 行容器：使用 el-row/el-col 栅格系统 -->
              <el-row v-if="item.type === 'row'" :gutter="item.props?.gutter || 16" class="preview-row">
                <template v-for="child in item.children" :key="child.id">
                  <el-col :span="child.type === 'col' ? (child.props?.span || 12) : 24">
                    <!-- col 容器内部渲染子组件 -->
                    <template v-if="child.type === 'col' && child.children?.length">
                      <ComponentRenderer
                        v-for="grandChild in child.children"
                        :key="grandChild.id"
                        :item="grandChild"
                        :form-data="formData"
                        :preview-mode="true"
                      />
                    </template>
                    <!-- 非 col 组件直接渲染 -->
                    <ComponentRenderer v-else :item="child" :form-data="formData" :preview-mode="true" />
                  </el-col>
                </template>
              </el-row>
              <!-- 普通组件：使用 el-row 包裹 -->
              <el-row v-else :gutter="16" class="preview-row">
                <el-col :span="getSpan(item)">
                  <ComponentRenderer :item="item" :form-data="formData" :preview-mode="true" />
                </el-col>
              </el-row>
            </template>
          </div>
        </template>

        <el-divider />

        <!-- 功能按钮 -->
        <div class="preview-actions">
          <!-- 表单功能按钮（仅表单模式显示） -->
          <template v-if="hasFormItems">
            <el-button type="primary" @click="handleValidate">验证表单</el-button>
            <el-button @click="handleReset">重置表单</el-button>
            <el-button @click="handleShowData">查看数据</el-button>
          </template>
          <!-- 生成代码按钮（始终显示） -->
          <el-button type="success" @click="handleGenerateCode">生成代码</el-button>
        </div>

        <!-- 数据展示（仅表单模式显示） -->
        <el-collapse v-if="hasFormItems" v-model="activeCollapse" class="mt-4">
          <el-collapse-item title="表单数据" name="data">
            <pre class="data-preview">{{ JSON.stringify(formData, null, 2) }}</pre>
          </el-collapse-item>
        </el-collapse>
      </div>
    </div>
  </AModal>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import type { FormSchema, FormItemSchema, FormItemType } from '../types'
import { hasFormComponents, getItemSpan, getItemWidth } from '../types'
import ComponentRenderer from './ComponentRenderer.vue'

defineOptions({ name: 'PreviewDialog' })

const props = defineProps<{
  modelValue: boolean
  schema: FormSchema
}>()

export interface CodeSettings {
  mode: 'page' | 'dialog' | 'drawer'
  size: 'small' | 'medium' | 'large' | 'xl'
  title?: string
}

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'code', settings: CodeSettings): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 是否包含表单组件
const hasFormItems = computed(() => hasFormComponents(props.schema.items))

// 预览标题
const previewTitle = computed(() => hasFormItems.value ? '表单预览' : '页面预览')

const formRef = ref<FormInstance>()
const innerFormRef = ref<FormInstance>()
const formData = ref<Record<string, any>>({})
const activeCollapse = ref<string[]>([])

// 预览模式：page-页面 / dialog-弹窗
const previewMode = ref<'page' | 'dialog'>('page')

// 内部弹窗显示状态
const showInnerDialog = ref(false)

// 打开内部弹窗
function openInnerDialog() {
  showInnerDialog.value = true
}

// 弹窗设置
const dialogSettings = reactive({
  title: '表单弹窗',
  type: 'dialog' as 'dialog' | 'drawer',
  size: 'xl' as 'small' | 'medium' | 'large' | 'xl',
  showFooter: true
})

// 容器组件类型
const containerTypes: FormItemType[] = ['row', 'col']

// 递归收集所有表单项（排除容器组件）
function collectFormItems(items: FormItemSchema[]): FormItemSchema[] {
  const result: FormItemSchema[] = []
  for (const item of items) {
    if (containerTypes.includes(item.type)) {
      // 容器组件递归收集子组件
      if (item.children && item.children.length > 0) {
        result.push(...collectFormItems(item.children))
      }
    } else {
      result.push(item)
    }
  }
  return result
}

// 生成校验规则（递归处理嵌套组件）
const rules = computed<FormRules>(() => {
  const result: FormRules = {}
  const allItems = collectFormItems(props.schema.items)
  allItems
    .filter((item) => item.required)
    .forEach((item) => {
      const trigger = isSelectType(item.type) ? 'change' : 'blur'
      result[item.prop] = [
        {
          required: true,
          message: `请${getActionText(item.type)}${item.label}`,
          trigger
        }
      ]
    })
  return result
})

// 监听弹窗打开，初始化表单数据
watch(visible, (val) => {
  if (val) {
    initFormData()
  }
})

// 初始化表单数据（递归处理嵌套组件）
function initFormData() {
  const data: Record<string, any> = {}
  const allItems = collectFormItems(props.schema.items)
  allItems.forEach((item) => {
    data[item.prop] = item.defaultValue ?? getDefaultValue(item.type)
  })
  formData.value = data
}

// 获取默认值
function getDefaultValue(type: FormItemType): any {
  switch (type) {
    case 'checkbox':
    case 'daterange':
    case 'datetimerange':
      return []
    case 'switch':
      return false
    case 'number':
      return null
    default:
      return ''
  }
}

// 使用公共函数的别名，保持模板兼容
const getSpan = getItemSpan

// 是否是选择类型
function isSelectType(type: FormItemType): boolean {
  return ['select', 'radio', 'checkbox', 'date', 'datetime', 'daterange', 'datetimerange', 'time', 'cascader', 'treeSelect'].includes(type)
}

// 获取操作文本
function getActionText(type: FormItemType): string {
  return isSelectType(type) ? '选择' : '输入'
}

// 验证表单
async function handleValidate() {
  try {
    await formRef.value?.validate()
    ElMessage.success('表单验证通过')
  } catch {
    ElMessage.warning('请检查表单填写是否正确')
  }
}

// 重置表单
function handleReset() {
  formRef.value?.resetFields()
  initFormData()
  ElMessage.success('表单已重置')
}

// 查看数据
function handleShowData() {
  activeCollapse.value = ['data']
}

// 弹窗确认
async function handleDialogConfirm() {
  try {
    await innerFormRef.value?.validate()
    ElMessage.success('表单验证通过，确认提交')
    showInnerDialog.value = false
  } catch {
    ElMessage.warning('请检查表单填写是否正确')
  }
}

// 生成代码
function handleGenerateCode() {
  visible.value = false
  // 传递预览设置到代码生成
  const settings: CodeSettings = {
    mode: previewMode.value === 'dialog'
      ? (dialogSettings.type === 'drawer' ? 'drawer' : 'dialog')
      : 'page',
    size: dialogSettings.size,
    title: dialogSettings.title
  }
  emit('code', settings)
}
</script>

<style scoped lang="scss">
.preview-wrapper {
  display: flex;
  gap: 20px;
  min-height: 400px;
}

.preview-settings {
  width: 200px;
  flex-shrink: 0;
  padding: 16px;
  background: var(--el-fill-color-lighter);
  border-radius: 8px;

  .settings-title {
    font-size: 14px;
    font-weight: 600;
    color: var(--el-text-color-primary);
    margin-bottom: 12px;
  }

  .settings-radio {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .w-full {
    width: 100%;
  }

  :deep(.el-form-item) {
    margin-bottom: 12px;
  }

  :deep(.el-divider) {
    margin: 16px 0;
  }
}

.preview-content {
  flex: 1;
  min-width: 0;
}

.page-preview {
  padding: 20px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;

  &.canvas-preview {
    .preview-row {
      margin-bottom: 16px;

      &:last-child {
        margin-bottom: 0;
      }
    }
  }
}

.dialog-preview-trigger {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 60px 20px;
  background: var(--el-fill-color-lighter);
  border: 2px dashed var(--el-border-color);
  border-radius: 8px;

  .trigger-tip {
    margin: 0;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }
}

.dialog-canvas-preview {
  .preview-row {
    margin-bottom: 16px;

    &:last-child {
      margin-bottom: 0;
    }
  }
}

.preview-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.data-preview {
  margin: 0;
  padding: 12px;
  font-size: 12px;
  line-height: 1.6;
  background: var(--el-fill-color-light);
  border-radius: 4px;
  overflow-x: auto;
}

.mb-4 {
  margin-bottom: 16px;
}

.mt-4 {
  margin-top: 16px;
}
</style>
