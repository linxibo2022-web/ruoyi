<!-- 属性面板 -->
<template>
  <div class="property-panel">
    <div class="panel-header">
      <span class="panel-title">{{ selectedItem ? '组件配置' : '页面配置' }}</span>
      <el-button
        v-if="selectedItem"
        size="small"
        text
        type="primary"
        @click="$emit('deselect')"
      >
        返回页面配置
      </el-button>
    </div>

    <div class="panel-body">
      <el-scrollbar>
        <!-- 未选中组件时显示页面配置 -->
        <template v-if="!selectedItem">
          <el-form label-position="top" size="small" class="config-form">
            <el-form-item label="页面名称">
              <el-input v-model="schema.name" placeholder="请输入页面名称" />
            </el-form-item>

            <el-form-item label="页面描述">
              <el-input v-model="schema.description" type="textarea" :rows="2" placeholder="请输入页面描述" />
            </el-form-item>

            <!-- 表单布局配置（仅有表单组件时显示） -->
            <template v-if="hasFormItems">
              <el-divider content-position="left">表单配置</el-divider>

              <el-form-item label="标签宽度">
                <el-input v-model="schema.labelWidth" placeholder="如: 100px">
                  <template #append>px</template>
                </el-input>
              </el-form-item>

              <el-form-item label="标签位置">
                <el-radio-group v-model="schema.labelPosition">
                  <el-radio-button value="left">左对齐</el-radio-button>
                  <el-radio-button value="right">右对齐</el-radio-button>
                  <el-radio-button value="top">顶部</el-radio-button>
                </el-radio-group>
              </el-form-item>

              <el-form-item label="栅格间距">
                <el-input-number v-model="schema.gutter" :min="0" :max="40" :step="4" controls-position="" class="w-full" />
              </el-form-item>
            </template>

            <el-divider content-position="left">生成配置</el-divider>

            <el-form-item label="生成类型">
              <el-radio-group v-model="schema.layout">
                <el-radio-button value="dialog">弹窗</el-radio-button>
                <el-radio-button value="drawer">抽屉</el-radio-button>
                <el-radio-button value="page">页面</el-radio-button>
              </el-radio-group>
            </el-form-item>

            <el-form-item v-if="schema.layout === 'dialog'" label="弹窗尺寸">
              <el-select v-model="schema.dialogSize" class="w-full">
                <el-option label="小 (600px)" value="small" />
                <el-option label="中 (800px)" value="medium" />
                <el-option label="大 (1000px)" value="large" />
                <el-option label="超大 (1200px)" value="xl" />
              </el-select>
            </el-form-item>

            <el-form-item v-if="schema.layout === 'drawer'" label="抽屉方向">
              <el-select v-model="schema.drawerDirection" class="w-full">
                <el-option label="从右侧弹出" value="rtl" />
                <el-option label="从左侧弹出" value="ltr" />
                <el-option label="从顶部弹出" value="ttb" />
                <el-option label="从底部弹出" value="btt" />
              </el-select>
            </el-form-item>
          </el-form>
        </template>

        <!-- 选中组件时显示组件配置 -->
        <template v-else>
          <el-form label-position="top" size="small" class="config-form">
            <!-- 表单组件配置 -->
            <template v-if="isSelectedFormComponent">
              <el-form-item label="字段标识">
                <el-input v-model="selectedItem.prop" placeholder="字段标识" clearable @change="handleUpdate" />
              </el-form-item>

              <el-form-item label="标签名称">
                <el-input v-model="selectedItem.label" placeholder="标签名称" clearable @change="handleUpdate" />
              </el-form-item>

              <el-form-item label="占位提示">
                <el-input v-model="selectedItem.placeholder" placeholder="请输入占位提示" clearable @change="handleUpdate" />
              </el-form-item>

              <el-form-item label="提示信息">
                <el-input v-model="selectedItem.tooltip" placeholder="鼠标悬浮提示" clearable @change="handleUpdate" />
              </el-form-item>

              <el-form-item label="栅格占比">
                <el-select v-model="selectedItem.span" class="w-full" clearable @change="handleUpdate">
                  <el-option label="自动" value="auto" />
                  <el-option label="1/4 (6列)" :value="6" />
                  <el-option label="1/3 (8列)" :value="8" />
                  <el-option label="1/2 (12列)" :value="12" />
                  <el-option label="2/3 (16列)" :value="16" />
                  <el-option label="3/4 (18列)" :value="18" />
                  <el-option label="整行 (24列)" :value="24" />
                </el-select>
              </el-form-item>

              <el-form-item label="是否必填">
                <el-switch v-model="selectedItem.required" @change="handleUpdate" />
              </el-form-item>
            </template>

            <!-- 非表单组件配置（卡片、图表等） -->
            <template v-else>
              <el-form-item label="组件名称">
                <el-input v-model="selectedItem.label" placeholder="组件名称" clearable @change="handleUpdate" />
              </el-form-item>

              <!-- 宽度占比（布局组件不显示，它们有自己的 props.span 配置） -->
              <el-form-item v-if="!isLayoutComponent" label="宽度占比">
                <el-select v-model="selectedItem.span" class="w-full" clearable @change="handleUpdate">
                  <el-option label="自动 (50%)" value="auto" />
                  <el-option label="1/4 (25%)" :value="6" />
                  <el-option label="1/3 (33%)" :value="8" />
                  <el-option label="1/2 (50%)" :value="12" />
                  <el-option label="2/3 (66%)" :value="16" />
                  <el-option label="3/4 (75%)" :value="18" />
                  <el-option label="整行 (100%)" :value="24" />
                </el-select>
              </el-form-item>
            </template>

            <el-divider content-position="left">组件属性</el-divider>

            <!-- 动态属性配置 -->
            <template v-for="propConfig in componentPropsConfig" :key="propConfig.prop">
              <el-form-item
                v-if="!propConfig.showCondition || propConfig.showCondition(selectedItem)"
                :label="propConfig.label"
              >
                <!-- 输入框 -->
                <el-input
                  v-if="propConfig.type === 'input'"
                  v-model="selectedItem.props![propConfig.prop]"
                  :placeholder="propConfig.placeholder"
                  clearable
                  @change="handleUpdate"
                />

                <!-- 数字输入 -->
                <el-input-number
                  v-else-if="propConfig.type === 'number'"
                  v-model="selectedItem.props![propConfig.prop]"
                  :min="propConfig.min"
                  :max="propConfig.max"
                  :step="propConfig.step || 1"
                  controls-position=""
                  class="w-full"
                  @change="handleUpdate"
                />

                <!-- 开关 -->
                <el-switch
                  v-else-if="propConfig.type === 'switch'"
                  v-model="selectedItem.props![propConfig.prop]"
                  @change="handleUpdate"
                />

                <!-- 下拉选择 -->
                <el-select
                  v-else-if="propConfig.type === 'select'"
                  v-model="selectedItem.props![propConfig.prop]"
                  class="w-full"
                  clearable
                  @change="handleUpdate"
                >
                  <el-option
                    v-for="opt in propConfig.options"
                    :key="opt.value"
                    :label="opt.label"
                    :value="opt.value"
                  />
                </el-select>

                <!-- 单选 -->
                <el-radio-group
                  v-else-if="propConfig.type === 'radio'"
                  v-model="selectedItem.props![propConfig.prop]"
                  @change="handleUpdate"
                >
                  <el-radio-button v-for="opt in propConfig.options" :key="opt.value" :value="opt.value">
                    {{ opt.label }}
                  </el-radio-button>
                </el-radio-group>

                <!-- 选项编辑器 -->
                <OptionsEditor
                  v-else-if="propConfig.type === 'options-editor'"
                  v-model="selectedItem.options"
                  @change="handleUpdate"
                />

                <!-- 字典类型选择 -->
                <el-select
                  v-else-if="propConfig.type === 'dict-select'"
                  v-model="selectedItem.props![propConfig.prop]"
                  class="w-full"
                  clearable
                  filterable
                  placeholder="请选择字典类型"
                  value-on-clear=""
                  @change="handleUpdate"
                >
                  <el-option
                    v-for="dict in dictTypeOptions"
                    :key="dict.dictType"
                    :label="dict.dictName"
                    :value="dict.dictType"
                  >
                    <span style="float: left">{{ dict.dictName }}</span>
                    <span style="float: right; color: #8492a6; font-size: 13px">{{ dict.dictType }}</span>
                  </el-option>
                </el-select>

                <!-- 图标选择器 -->
                <IconSelect
                  v-else-if="propConfig.type === 'icon-select'"
                  v-model="selectedItem.props![propConfig.prop]"
                  width="100%"
                  @update:model-value="handleUpdate"
                />
              </el-form-item>
            </template>
          </el-form>
        </template>
      </el-scrollbar>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import type { FormSchema, FormItemSchema, PropConfig } from '../types'
import { hasFormComponents, isFormComponent, CONTAINER_TYPES } from '../types'
import { getComponentConfig } from '../config/componentConfig'
import { getDictTypeOptions } from '@/api/system/dict/dictType/dictTypeApi'
import type { SysDictTypeVo } from '@/api/system/dict/dictType/dictTypeTypes'
import OptionsEditor from './OptionsEditor.vue'

defineOptions({ name: 'PropertyPanel' })

const props = defineProps<{
  schema: FormSchema
  selectedItem: FormItemSchema | null
}>()

// 字典类型列表
const dictTypeOptions = ref<SysDictTypeVo[]>([])

// 加载字典类型列表
async function loadDictTypeOptions() {
  const [err, data] = await getDictTypeOptions()
  if (!err) {
    dictTypeOptions.value = data
  }
}

// 组件挂载时加载字典列表
onMounted(() => {
  loadDictTypeOptions()
})

// 是否包含表单组件
const hasFormItems = computed(() => hasFormComponents(props.schema.items))

// 当前选中的组件是否为表单组件
const isSelectedFormComponent = computed(() => {
  if (!props.selectedItem) return false
  return isFormComponent(props.selectedItem.type)
})

// 当前选中的组件是否为布局组件（row, col）
const isLayoutComponent = computed(() => {
  if (!props.selectedItem) return false
  return CONTAINER_TYPES.includes(props.selectedItem.type)
})

const emit = defineEmits<{
  (e: 'update'): void
  (e: 'deselect'): void
}>()

// 获取当前选中组件的属性配置
const componentPropsConfig = computed<PropConfig[]>(() => {
  if (!props.selectedItem) return []
  const config = getComponentConfig(props.selectedItem.type)
  return config?.propsConfig || []
})

// 属性更新
function handleUpdate() {
  emit('update')
}
</script>

<style scoped lang="scss">
.property-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--el-bg-color);
  border-left: 1px solid var(--el-border-color-light);

  .panel-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px;
    border-bottom: 1px solid var(--el-border-color-light);

    .panel-title {
      font-size: 14px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }
  }

  .panel-body {
    flex: 1;
    overflow: hidden;

    :deep(.el-scrollbar) {
      height: 100%;
    }

    :deep(.el-scrollbar__view) {
      padding: 16px;
    }
  }

  .config-form {
    :deep(.el-form-item) {
      margin-bottom: 16px;
    }

    :deep(.el-form-item__label) {
      font-size: 12px;
      color: var(--el-text-color-secondary);
      padding-bottom: 4px;
    }

    :deep(.el-divider__text) {
      font-size: 12px;
      color: var(--el-text-color-secondary);
      background: var(--el-bg-color);
    }
  }

  .w-full {
    width: 100%;
  }
}
</style>
