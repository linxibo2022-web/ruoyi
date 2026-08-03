<!-- 代码预览对话框 -->
<template>
  <AModal v-model="visible" title="生成代码" size="xl" :show-footer="false">
    <div class="code-dialog">
      <div class="code-wrapper">
        <!-- 左侧设置面板 -->
        <div class="code-settings">
          <div class="settings-title">生成模式</div>
          <el-radio-group v-model="generateMode" class="settings-radio">
            <el-radio value="page">页面</el-radio>
            <el-radio value="dialog">弹窗</el-radio>
            <el-radio value="drawer">抽屉</el-radio>
          </el-radio-group>

          <template v-if="generateMode === 'dialog' || generateMode === 'drawer'">
            <el-divider />
            <div class="settings-title">弹窗设置</div>
            <el-form label-position="top" size="small">
              <el-form-item label="弹窗尺寸">
                <el-select v-model="codeSettings.size" class="w-full">
                  <el-option label="小" value="small" />
                  <el-option label="中" value="medium" />
                  <el-option label="大" value="large" />
                  <el-option label="超大" value="xl" />
                </el-select>
              </el-form-item>
              <el-form-item v-if="generateMode === 'drawer'" label="抽屉方向">
                <el-select v-model="codeSettings.direction" class="w-full">
                  <el-option label="从右侧弹出" value="rtl" />
                  <el-option label="从左侧弹出" value="ltr" />
                  <el-option label="从上方弹出" value="ttb" />
                  <el-option label="从下方弹出" value="btt" />
                </el-select>
              </el-form-item>
            </el-form>
          </template>

          <!-- 表单设置（仅有表单组件时显示） -->
          <template v-if="hasFormItems">
            <el-divider />
            <div class="settings-title">表单设置</div>
            <el-form label-position="top" size="small">
              <el-form-item label="表单名称">
                <el-input v-model="codeSettings.name" placeholder="如：用户信息" />
              </el-form-item>
              <el-form-item label="标签宽度">
                <el-input v-model="codeSettings.labelWidth" placeholder="如：100px" />
              </el-form-item>
            </el-form>
          </template>

          <!-- 页面设置（无表单组件时显示） -->
          <template v-else>
            <el-divider />
            <div class="settings-title">页面设置</div>
            <el-form label-position="top" size="small">
              <el-form-item label="页面名称">
                <el-input v-model="codeSettings.name" placeholder="如：数据统计" />
              </el-form-item>
            </el-form>
          </template>
        </div>

        <!-- 右侧代码区域 -->
        <div class="code-main">
          <!-- 标签页 -->
          <el-tabs v-model="activeTab" class="code-tabs">
            <el-tab-pane label="完整代码" name="full">
              <div class="code-header">
                <span class="code-title">Vue 单文件组件</span>
                <div class="code-actions">
                  <el-button size="small" :icon="DocumentCopy" @click="handleCopy('full')">复制代码</el-button>
                  <el-button size="small" :icon="Download" @click="handleDownload">下载文件</el-button>
                </div>
              </div>
              <div class="code-content">
                <pre><code class="language-vue">{{ fullCode }}</code></pre>
              </div>
            </el-tab-pane>

            <el-tab-pane label="模板" name="template">
              <div class="code-header">
                <span class="code-title">Template 模板代码</span>
                <div class="code-actions">
                  <el-button size="small" :icon="DocumentCopy" @click="handleCopy('template')">复制代码</el-button>
                </div>
              </div>
              <div class="code-content">
                <pre><code class="language-html">{{ templateCode }}</code></pre>
              </div>
            </el-tab-pane>

            <el-tab-pane label="脚本" name="script">
              <div class="code-header">
                <span class="code-title">Script 脚本代码</span>
                <div class="code-actions">
                  <el-button size="small" :icon="DocumentCopy" @click="handleCopy('script')">复制代码</el-button>
                </div>
              </div>
              <div class="code-content">
                <pre><code class="language-typescript">{{ scriptCode }}</code></pre>
              </div>
            </el-tab-pane>

            <el-tab-pane label="类型定义" name="types">
              <div class="code-header">
                <span class="code-title">TypeScript 类型定义</span>
                <div class="code-actions">
                  <el-button size="small" :icon="DocumentCopy" @click="handleCopy('types')">复制代码</el-button>
                </div>
              </div>
              <div class="code-content">
                <pre><code class="language-typescript">{{ typesCode }}</code></pre>
              </div>
            </el-tab-pane>

            <el-tab-pane label="JSON配置" name="json">
              <div class="code-header">
                <span class="code-title">JSON 配置（可导入导出）</span>
                <div class="code-actions">
                  <el-button size="small" :icon="Upload" @click="triggerImport">导入JSON</el-button>
                  <el-button size="small" :icon="DocumentCopy" @click="handleCopy('json')">复制配置</el-button>
                  <el-button size="small" :icon="Download" @click="handleDownloadJson">下载JSON</el-button>
                </div>
              </div>
              <div class="code-content">
                <pre><code class="language-json">{{ jsonCode }}</code></pre>
              </div>
              <!-- 隐藏的文件输入 -->
              <input ref="fileInputRef" type="file" accept=".json" style="display: none" @change="handleFileImport" />
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </div>
  </AModal>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { DocumentCopy, Download, Upload } from '@element-plus/icons-vue'
import { useClipboard } from '@vueuse/core'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormSchema } from '../types'
import { hasFormComponents } from '../types'
import { useCodeGenerator } from '../composables/useCodeGenerator'

defineOptions({ name: 'CodeDialog' })

export interface InitialSettings {
  mode: 'page' | 'dialog' | 'drawer'
  size: 'small' | 'medium' | 'large' | 'xl'
  title?: string
}

const props = defineProps<{
  modelValue: boolean
  schema: FormSchema
  initialSettings?: InitialSettings | null
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'import', schema: FormSchema): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 是否包含表单组件
const hasFormItems = computed(() => hasFormComponents(props.schema.items))

const { copy } = useClipboard()
const { generateTemplate, generateScript, generateTypes, generateFullCode, generateJsonConfig } = useCodeGenerator()

const activeTab = ref('full')

// 生成模式（默认页面）
const generateMode = ref<'page' | 'dialog' | 'drawer'>('page')

// 代码生成设置
const codeSettings = reactive({
  name: '',
  size: 'medium' as 'small' | 'medium' | 'large' | 'xl',
  direction: 'rtl' as 'rtl' | 'ltr' | 'ttb' | 'btt',
  labelWidth: '100px'
})

// 监听弹窗打开，应用初始设置
watch(visible, (val) => {
  if (val && props.initialSettings) {
    generateMode.value = props.initialSettings.mode
    codeSettings.size = props.initialSettings.size
    if (props.initialSettings.title) {
      codeSettings.name = props.initialSettings.title
    }
  }
})

// 合并配置，生成最终的 schema
const mergedSchema = computed<FormSchema>(() => {
  return {
    ...props.schema,
    name: codeSettings.name || props.schema.name || '表单',
    layout: generateMode.value,
    labelWidth: codeSettings.labelWidth || props.schema.labelWidth,
    dialogSize: codeSettings.size,
    drawerDirection: codeSettings.direction
  }
})

// 生成各类代码
const templateCode = computed(() => generateTemplate(mergedSchema.value))
const scriptCode = computed(() => generateScript(mergedSchema.value))
const typesCode = computed(() => generateTypes(mergedSchema.value))
const fullCode = computed(() => generateFullCode(mergedSchema.value))
const jsonCode = computed(() => generateJsonConfig(mergedSchema.value))

// 复制代码
async function handleCopy(type: 'full' | 'template' | 'script' | 'types' | 'json') {
  const codeMap = {
    full: fullCode.value,
    template: templateCode.value,
    script: scriptCode.value,
    types: typesCode.value,
    json: jsonCode.value
  }

  try {
    await copy(codeMap[type])
    ElMessage.success('代码已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败，请手动复制')
  }
}

// 下载 Vue 文件
function handleDownload() {
  // 保持原始文件名，不做大小写转换
  const name = mergedSchema.value.name || 'index'
  const filename = `${name}.vue`
  downloadFile(fullCode.value, filename, 'text/plain')
  ElMessage.success(`文件 ${filename} 已下载`)
}

// 下载 JSON 文件
function handleDownloadJson() {
  const filename = `${mergedSchema.value.name || 'form'}-config.json`
  downloadFile(jsonCode.value, filename, 'application/json')
  ElMessage.success(`文件 ${filename} 已下载`)
}

// 下载文件
function downloadFile(content: string, filename: string, type: string) {
  const blob = new Blob([content], { type })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

// 文件输入引用
const fileInputRef = ref<HTMLInputElement>()

// 触发文件选择
function triggerImport() {
  fileInputRef.value?.click()
}

// 处理文件导入
async function handleFileImport(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  try {
    const content = await file.text()
    const importedSchema = JSON.parse(content) as FormSchema

    // 基本校验
    if (!importedSchema.items || !Array.isArray(importedSchema.items)) {
      throw new Error('无效的配置格式：缺少 items 数组')
    }

    // 确认是否覆盖
    await ElMessageBox.confirm('导入将覆盖当前设计内容，是否继续？', '确认导入', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 触发导入事件
    emit('import', importedSchema)
    visible.value = false
    ElMessage.success('配置导入成功')
  } catch (error: any) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error?.message || 'JSON 格式错误，导入失败')
    }
  } finally {
    // 清空文件输入，允许再次选择同一文件
    input.value = ''
  }
}
</script>

<style scoped lang="scss">
.code-dialog {
  .code-wrapper {
    display: flex;
    gap: 20px;
    min-height: 500px;
  }

  .code-settings {
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

  .code-main {
    flex: 1;
    min-width: 0;
  }

  .code-tabs {
    :deep(.el-tabs__content) {
      padding: 0;
    }
  }

  .code-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 16px;
    background: var(--el-fill-color-light);
    border-bottom: 1px solid var(--el-border-color-light);

    .code-title {
      font-size: 14px;
      font-weight: 500;
      color: var(--el-text-color-primary);
    }

    .code-actions {
      display: flex;
      gap: 8px;
    }
  }

  .code-content {
    max-height: 450px;
    overflow: auto;
    background: #1e1e1e;

    pre {
      margin: 0;
      padding: 16px;

      code {
        font-family: 'Fira Code', 'Consolas', monospace;
        font-size: 13px;
        line-height: 1.6;
        color: #d4d4d4;
        white-space: pre;
      }
    }
  }
}
</style>
