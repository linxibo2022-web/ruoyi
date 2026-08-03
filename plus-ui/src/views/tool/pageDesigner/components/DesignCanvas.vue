<!-- 设计画布 -->
<template>
  <div class="design-canvas">
    <!-- 工具栏 -->
    <div class="canvas-toolbar">
      <div class="toolbar-left">
        <el-button :icon="RefreshLeft" :disabled="!canUndo" title="撤销 (Ctrl+Z)" @click="$emit('undo')" />
        <el-button :icon="RefreshRight" :disabled="!canRedo" title="重做 (Ctrl+Y)" @click="$emit('redo')" />
        <el-divider direction="vertical" />
        <el-button :icon="Delete" :disabled="!hasItems" title="清空" @click="handleClear" />
      </div>
      <div class="toolbar-right">
        <el-button v-if="aiEnabled" :icon="MagicStick" type="warning" plain @click="$emit('ai-generate')">AI 生成</el-button>
        <el-button :icon="View" type="primary" plain @click="$emit('preview')">预览</el-button>
        <el-button :icon="Document" type="success" plain @click="$emit('code')">生成代码</el-button>
      </div>
    </div>

    <!-- 画布区域 -->
    <div
      class="canvas-body"
      :class="{ 'is-drag-over': isDragOver }"
      @dragover.prevent="handleDragOver"
      @dragleave="handleDragLeave"
      @drop="handleDrop"
      @click="handleCanvasClick"
    >
      <!-- 空状态 -->
      <div v-if="items.length === 0" class="canvas-empty">
        <el-empty description="从左侧拖拽组件到此处" :image-size="120">
          <template #image>
            <Icon name="folder-add" class="empty-icon" />
          </template>
        </el-empty>
      </div>

      <!-- 有表单组件时使用 el-form 包裹 -->
      <el-form
        v-else-if="hasFormItems"
        ref="formRef"
        :model="formData"
        :rules="rules"
        :label-width="schema.labelWidth"
        :label-position="schema.labelPosition"
        class="design-form"
      >
        <el-row :gutter="schema.gutter || 20">
          <TransitionGroup name="list">
            <template v-for="(item, index) in items" :key="item.id">
              <!-- 行容器特殊处理：渲染为 el-row -->
              <el-col v-if="item.type === 'row'" :span="24">
                <div
                  class="form-item-wrapper row-wrapper"
                  :class="{
                    'is-selected': selectedId === item.id,
                    'is-drag-source': dragSourceId === item.id,
                    'is-drop-target': rowDropTargetId === item.id
                  }"
                  draggable="true"
                  @click.stop="$emit('select', item.id)"
                  @dragstart="handleItemDragStart($event, item.id, index)"
                  @dragend="handleItemDragEnd"
                  @dragover.prevent="handleRowDragOver($event, item.id)"
                  @dragleave="handleRowDragLeave"
                  @drop.stop="handleRowDrop($event, item.id)"
                >
                  <el-row :gutter="item.props?.gutter || 20">
                    <!-- 行容器内的列 -->
                    <template v-if="item.children && item.children.length > 0">
                      <el-col
                        v-for="child in item.children"
                        :key="child.id"
                        :span="child.type === 'col' ? (child.props?.span || 12) : getSpan(child)"
                      >
                        <div
                          class="form-item-wrapper"
                          :class="{ 'is-selected': selectedId === child.id }"
                          @click.stop="$emit('select', child.id)"
                        >
                          <ComponentRenderer
                            :item="child"
                            :form-data="formData"
                            :selected-id="selectedId"
                            @select-child="handleSelectChild"
                            @remove-child="handleRemoveChild"
                            @add-to-container="handleAddToContainer"
                          />
                          <!-- 子组件操作按钮 -->
                          <div v-if="selectedId === child.id" class="item-actions">
                            <el-button-group size="small">
                              <el-button :icon="DocumentCopy" title="复制" @click.stop="$emit('copy', child.id)" />
                              <el-button :icon="Delete" title="删除" @click.stop="$emit('remove', child.id)" />
                            </el-button-group>
                          </div>
                          <div v-if="selectedId === child.id" class="item-indicator">
                            <span class="indicator-label">{{ child.label }}</span>
                          </div>
                        </div>
                      </el-col>
                    </template>
                    <!-- 空行容器提示 -->
                    <el-col v-else :span="24">
                      <div class="empty-container">拖拽组件到此行容器</div>
                    </el-col>
                  </el-row>

                  <!-- 行容器操作遮罩 -->
                  <div v-if="selectedId === item.id" class="item-actions">
                    <el-button-group size="small">
                      <el-button :icon="DocumentCopy" title="复制" @click.stop="$emit('copy', item.id)" />
                      <el-button :icon="Delete" title="删除" @click.stop="$emit('remove', item.id)" />
                    </el-button-group>
                  </div>
                  <div v-if="selectedId === item.id" class="item-indicator">
                    <span class="indicator-label">{{ item.label }}</span>
                  </div>
                </div>
              </el-col>

              <!-- 普通组件 -->
              <el-col v-else :span="getSpan(item)">
                <div
                  class="form-item-wrapper"
                  :class="{
                    'is-selected': selectedId === item.id,
                    'is-drag-source': dragSourceId === item.id
                  }"
                  draggable="true"
                  @click.stop="$emit('select', item.id)"
                  @dragstart="handleItemDragStart($event, item.id, index)"
                  @dragend="handleItemDragEnd"
                  @dragover.prevent="handleItemDragOver($event, index)"
                  @drop.stop="handleItemDrop($event, index)"
                >
                  <!-- 组件渲染 -->
                  <ComponentRenderer
                    :item="item"
                    :form-data="formData"
                    :selected-id="selectedId"
                    @select-child="handleSelectChild"
                    @remove-child="handleRemoveChild"
                    @add-to-container="handleAddToContainer"
                  />

                  <!-- 操作遮罩 -->
                  <div v-if="selectedId === item.id" class="item-actions">
                    <el-button-group size="small">
                      <el-button :icon="DocumentCopy" title="复制" @click.stop="$emit('copy', item.id)" />
                      <el-button :icon="Delete" title="删除" @click.stop="$emit('remove', item.id)" />
                    </el-button-group>
                  </div>

                  <!-- 选中指示器 -->
                  <div v-if="selectedId === item.id" class="item-indicator">
                    <span class="indicator-label">{{ item.label }}</span>
                  </div>

                  <!-- 拖拽排序指示线 -->
                  <div v-if="dropIndex === index && (dragSourceId || draggingItemType)" class="drop-indicator drop-indicator-before" />
                  <div
                    v-if="dropIndex === index + 1 && (dragSourceId || draggingItemType) && index === items.length - 1"
                    class="drop-indicator drop-indicator-after"
                  />
                </div>
              </el-col>
            </template>
          </TransitionGroup>
        </el-row>
      </el-form>

      <!-- 无表单组件时直接渲染（卡片、图表等） -->
      <div v-else class="design-canvas-content">
        <TransitionGroup name="list" tag="div" class="canvas-grid">
          <div
            v-for="(item, index) in items"
            :key="item.id"
            class="canvas-item-wrapper"
            :class="{
              'is-selected': selectedId === item.id,
              'is-drag-source': dragSourceId === item.id
            }"
            :style="{ width: getItemWidth(item) }"
            draggable="true"
            @click.stop="$emit('select', item.id)"
            @dragstart="handleItemDragStart($event, item.id, index)"
            @dragend="handleItemDragEnd"
            @dragover.prevent="handleItemDragOver($event, index)"
            @drop.stop="handleItemDrop($event, index)"
          >
            <!-- 组件渲染 -->
            <ComponentRenderer
              :item="item"
              :form-data="formData"
              :selected-id="selectedId"
              @select-child="handleSelectChild"
              @remove-child="handleRemoveChild"
              @add-to-container="handleAddToContainer"
            />

            <!-- 操作遮罩 -->
            <div v-if="selectedId === item.id" class="item-actions">
              <el-button-group size="small">
                <el-button :icon="DocumentCopy" title="复制" @click.stop="$emit('copy', item.id)" />
                <el-button :icon="Delete" title="删除" @click.stop="$emit('remove', item.id)" />
              </el-button-group>
            </div>

            <!-- 选中指示器 -->
            <div v-if="selectedId === item.id" class="item-indicator">
              <span class="indicator-label">{{ item.label }}</span>
            </div>

            <!-- 拖拽排序指示线 -->
            <div v-if="dropIndex === index && (dragSourceId || draggingItemType)" class="drop-indicator drop-indicator-before" />
            <div
              v-if="dropIndex === index + 1 && (dragSourceId || draggingItemType) && index === items.length - 1"
              class="drop-indicator drop-indicator-after"
            />
          </div>
        </TransitionGroup>
      </div>

      <!-- 悬浮优化按钮 -->
      <div v-if="aiEnabled && hasItems" class="floating-optimize-btn">
        <el-tooltip content="AI 一键优化" placement="left">
          <el-button type="warning" circle size="large" @click="$emit('ai-optimize')">
            <el-icon :size="20"><MagicStick /></el-icon>
          </el-button>
        </el-tooltip>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { RefreshLeft, RefreshRight, Delete, View, Document, DocumentCopy, MagicStick } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import type { FormRules } from 'element-plus'
import type { FormItemSchema, FormSchema, FormItemType } from '../types'
import { hasFormComponents, getItemSpan, getItemWidth, CONTAINER_TYPES } from '../types'
import ComponentRenderer from './ComponentRenderer.vue'
import { useFeatureStore } from '@/stores/modules/feature'

defineOptions({ name: 'DesignCanvas' })

// 获取功能配置 Store
const featureStore = useFeatureStore()

// AI 功能是否启用
const aiEnabled = computed(() => featureStore.features.langchain4jEnabled)

const props = defineProps<{
  schema: FormSchema
  items: FormItemSchema[]
  selectedId: string | null
  canUndo: boolean
  canRedo: boolean
  hasItems: boolean
  draggingItemType: FormItemType | null
}>()

const emit = defineEmits<{
  (e: 'select', id: string | null): void
  (e: 'remove', id: string): void
  (e: 'copy', id: string): void
  (e: 'move', oldIndex: number, newIndex: number): void
  (e: 'add', type: FormItemType, index?: number): void
  (e: 'add-to-container', containerId: string, type: FormItemType): void
  (e: 'undo'): void
  (e: 'redo'): void
  (e: 'clear'): void
  (e: 'preview'): void
  (e: 'code'): void
  (e: 'ai-generate'): void
  (e: 'ai-optimize'): void
}>()

// 表单数据（用于预览）
const formData = ref<Record<string, any>>({})

// 拖拽状态
const isDragOver = ref(false)
const dragSourceId = ref<string | null>(null)
const dropIndex = ref<number | null>(null)
const rowDropTargetId = ref<string | null>(null) // 行容器拖放目标

// 是否包含表单组件
const hasFormItems = computed(() => hasFormComponents(props.items))

// 递归收集所有表单项（排除容器组件）
function collectFormItems(items: FormItemSchema[]): FormItemSchema[] {
  const result: FormItemSchema[] = []
  for (const item of items) {
    if (CONTAINER_TYPES.includes(item.type)) {
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

// 是否是选择类型
function isSelectType(type: FormItemType): boolean {
  return ['select', 'radio', 'checkbox', 'date', 'datetime', 'daterange', 'datetimerange', 'time', 'cascader', 'treeSelect'].includes(type)
}

// 获取操作文本
function getActionText(type: FormItemType): string {
  return isSelectType(type) ? '选择' : '输入'
}

// 生成校验规则（递归处理嵌套组件）
const rules = computed<FormRules>(() => {
  const result: FormRules = {}
  const allItems = collectFormItems(props.items)
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

// 监听 items 变化，初始化表单数据
watch(
  () => props.items,
  (items) => {
    items.forEach((item) => {
      if (!(item.prop in formData.value)) {
        formData.value[item.prop] = item.defaultValue ?? getDefaultValue(item.type)
      }
    })
  },
  { immediate: true, deep: true }
)

// 获取默认值
function getDefaultValue(type: FormItemType): any {
  switch (type) {
    case 'checkbox':
      return []
    case 'switch':
      return false
    case 'number':
      return null
    case 'daterange':
    case 'datetimerange':
      return []
    default:
      return ''
  }
}

// 使用公共函数的别名，保持模板兼容
const getSpan = getItemSpan

// 从组件面板拖入
function handleDragOver(event: DragEvent) {
  event.preventDefault()
  isDragOver.value = true
}

function handleDragLeave() {
  isDragOver.value = false
  dropIndex.value = null
}

function handleDrop(event: DragEvent) {
  isDragOver.value = false
  const type = event.dataTransfer?.getData('componentType') as FormItemType
  if (type) {
    emit('add', type, dropIndex.value ?? undefined)
  }
  dropIndex.value = null
}

// 处理向容器添加组件
function handleAddToContainer(containerId: string, type: FormItemType) {
  emit('add-to-container', containerId, type)
}

// 处理选中子组件
function handleSelectChild(id: string) {
  emit('select', id)
}

// 处理删除子组件
function handleRemoveChild(id: string) {
  emit('remove', id)
}

// 画布内排序拖拽
function handleItemDragStart(event: DragEvent, id: string, index: number) {
  dragSourceId.value = id
  if (event.dataTransfer) {
    event.dataTransfer.setData('itemId', id)
    event.dataTransfer.setData('itemIndex', String(index))
    event.dataTransfer.effectAllowed = 'move'
  }
}

function handleItemDragEnd() {
  dragSourceId.value = null
  dropIndex.value = null
}

function handleItemDragOver(event: DragEvent, index: number) {
  // 仅在内部拖拽或从外部拖入时才计算
  if (!dragSourceId.value && !props.draggingItemType) return

  event.preventDefault()
  event.stopPropagation()

  const rect = (event.currentTarget as HTMLElement).getBoundingClientRect()
  const midY = rect.top + rect.height / 2

  if (event.clientY < midY) {
    dropIndex.value = index
  } else {
    dropIndex.value = index + 1
  }
}

function handleItemDrop(event: DragEvent, index: number) {
  event.stopPropagation()

  // 从外部拖入
  const type = event.dataTransfer?.getData('componentType') as FormItemType
  if (type) {
    const dropTargetItem = props.items[index]
    // 特殊处理：当 “列” 被拖到 “行” 上时，视为添加到行容器内部
    if (dropTargetItem?.type === 'row' && type === 'col') {
      emit('add-to-container', dropTargetItem.id, type)
    } else if (dropIndex.value !== null) {
      // 其他情况，添加到顶层列表
      emit('add', type, dropIndex.value)
    }
    dragSourceId.value = null
    dropIndex.value = null
    return
  }

  // 内部排序
  const itemIndex = event.dataTransfer?.getData('itemIndex')
  if (itemIndex !== undefined && dropIndex.value !== null) {
    const oldIndex = parseInt(itemIndex)
    let newIndex = dropIndex.value

    if (oldIndex < newIndex) {
      newIndex--
    }

    if (oldIndex !== newIndex) {
      emit('move', oldIndex, newIndex)
    }
  }

  dragSourceId.value = null
  dropIndex.value = null
}

// 行容器拖放处理
function handleRowDragOver(event: DragEvent, rowId: string) {
  // 只有从组件面板拖入时才处理
  if (!props.draggingItemType) return

  event.preventDefault()
  event.stopPropagation()
  rowDropTargetId.value = rowId
}

function handleRowDragLeave() {
  rowDropTargetId.value = null
}

function handleRowDrop(event: DragEvent, rowId: string) {
  event.stopPropagation()
  rowDropTargetId.value = null

  // 从组件面板拖入
  const type = event.dataTransfer?.getData('componentType') as FormItemType
  if (type) {
    emit('add-to-container', rowId, type)
  }
}

// 点击画布空白区域取消选择
function handleCanvasClick(event: MouseEvent) {
  // 只有点击画布本身时才取消选择，点击组件时不触发（组件有 @click.stop）
  const target = event.target as HTMLElement
  if (
    target.classList.contains('canvas-body') ||
    target.classList.contains('design-form') ||
    target.classList.contains('design-canvas-content') ||
    target.classList.contains('canvas-grid') ||
    target.classList.contains('canvas-empty')
  ) {
    emit('select', null)
  }
}

// 清空确认
async function handleClear() {
  try {
    await ElMessageBox.confirm('确定要清空所有组件吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    emit('clear')
  } catch {
    // 取消操作
  }
}
</script>

<style scoped lang="scss">
.design-canvas {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--el-fill-color-light);

  .canvas-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 16px;
    background: var(--el-bg-color);
    border-bottom: 1px solid var(--el-border-color-light);

    .toolbar-left,
    .toolbar-right {
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }

  .canvas-body {
    position: relative;
    flex: 1;
    padding: 20px;
    overflow-y: auto;
    transition: background-color 0.2s;

    &.is-drag-over {
      background: var(--el-fill-color);
    }
  }

  .canvas-empty {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
    min-height: 400px;

    .empty-icon {
      font-size: 80px;
      color: var(--el-text-color-placeholder);
    }
  }

  .design-form {
    padding: 20px;
    background: var(--el-bg-color);
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
    min-height: 400px;
  }

  // 非表单组件布局（卡片、图表等）
  .design-canvas-content {
    padding: 20px;
    background: var(--el-bg-color);
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
    min-height: 400px;

    .canvas-grid {
      display: flex;
      flex-wrap: wrap;
      gap: 16px;
    }
  }

  .canvas-item-wrapper {
    position: relative;
    padding: 8px;
    border: 1px dashed transparent;
    border-radius: 4px;
    transition: all 0.2s;
    cursor: move;
    box-sizing: border-box;

    &:hover {
      border-color: var(--el-border-color);
      background: var(--el-fill-color-lighter);
    }

    &.is-selected {
      border-color: var(--el-color-primary);
      background: var(--el-fill-color);
    }

    &.is-drag-source {
      opacity: 0.5;
    }
  }

  .form-item-wrapper {
    position: relative;
    padding: 8px;
    margin-bottom: 8px;
    border: 1px dashed transparent;
    border-radius: 4px;
    transition: all 0.2s;
    cursor: move;

    &:hover {
      border-color: var(--el-border-color);
      background: var(--el-fill-color-lighter);
    }

    &.is-selected {
      border-color: var(--el-color-primary);
      background: var(--el-fill-color);
    }

    &.is-drag-source {
      opacity: 0.5;
    }

    // 行容器样式
    &.row-wrapper {
      padding: 12px;
      background: var(--el-fill-color-lighter);
      border: 1px dashed var(--el-border-color);

      &:hover {
        border-color: var(--el-color-primary-light-5);
      }

      &.is-selected {
        border-color: var(--el-color-primary);
        background: var(--el-fill-color);
      }

      &.is-drop-target {
        border-color: var(--el-color-success);
        background: var(--el-color-success-light-9);
      }
    }
  }

  .empty-container {
    padding: 20px;
    text-align: center;
    color: var(--el-text-color-placeholder);
    background: var(--el-fill-color-light);
    border: 1px dashed var(--el-border-color-light);
    border-radius: 4px;
  }

  .item-actions {
    position: absolute;
    top: -32px;
    right: 0;
    z-index: 10;
  }

  .item-indicator {
    position: absolute;
    top: -1px;
    left: -1px;
    padding: 2px 8px;
    font-size: 12px;
    color: #fff;
    background: var(--el-color-primary);
    border-radius: 4px 0 4px 0;

    .indicator-label {
      max-width: 120px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .drop-indicator {
    position: absolute;
    left: 0;
    right: 0;
    height: 2px;
    background: var(--el-color-primary);
    z-index: 100;

    &::before {
      content: '';
      position: absolute;
      left: 0;
      width: 6px;
      height: 6px;
      background: var(--el-color-primary);
      border-radius: 50%;
      transform: translateY(-2px);
    }

    &-before {
      top: 0;
    }

    &-after {
      bottom: 0;
    }
  }

  // 列表过渡动画
  .list-enter-active,
  .list-leave-active {
    transition: all 0.3s ease;
  }

  .list-enter-from,
  .list-leave-to {
    opacity: 0;
    transform: translateX(-30px);
  }

  // 悬浮优化按钮
  .floating-optimize-btn {
    position: absolute;
    right: 24px;
    bottom: 24px;
    z-index: 100;

    .el-button {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      transition: all 0.3s ease;

      &:hover {
        transform: scale(1.1);
        box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
      }
    }
  }
}
</style>
