<!-- 页面设计器 -->
<template>
  <div ref="pageDesignerRef" class="page-designer" tabindex="0">
    <!-- 左侧组件面板 -->
    <div class="designer-left">
      <ComponentPanel @add="handleAddItem" @drag-start="handleDragStart" @drag-end="handleDragEnd" />
    </div>

    <!-- 中间设计画布 -->
    <div class="designer-center">
      <DesignCanvas
        :schema="schema"
        :items="schema.items"
        :selected-id="selectedId"
        :can-undo="canUndo"
        :can-redo="canRedo"
        :has-items="hasItems"
        :dragging-item-type="draggingItemType"
        @select="selectItem"
        @remove="removeItem"
        @copy="copyItem"
        @move="moveItem"
        @add="handleAddItem"
        @add-to-container="handleAddToContainer"
        @undo="undo"
        @redo="redo"
        @clear="clearItems"
        @preview="showPreview = true"
        @code="handleOpenCode()"
        @ai-generate="showAiGenerate = true"
        @ai-optimize="showAiOptimize = true"
      />
    </div>

    <!-- 右侧属性面板 -->
    <div class="designer-right">
      <PropertyPanel :schema="schema" :selected-item="selectedItem" @update="handlePropertyUpdate" @deselect="selectItem(null)" />
    </div>

    <!-- 预览弹窗 -->
    <PreviewDialog v-model="showPreview" :schema="schema" @code="handleOpenCode" />

    <!-- 代码弹窗 -->
    <CodeDialog v-model="showCode" :schema="schema" :initial-settings="codeInitialSettings" @import="handleImport" />

    <!-- AI 生成弹窗 -->
    <AiGenerateDialog v-model="showAiGenerate" @generate="handleAiGenerate" />

    <!-- AI 优化弹窗 -->
    <AiOptimizeDialog v-model="showAiOptimize" :items="schema.items" @optimize="handleAiOptimize" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import type { FormItemType, FormItemSchema, FormSchema } from './types'
import { useFormSchema } from './composables/useFormSchema'
import ComponentPanel from './components/ComponentPanel.vue'
import DesignCanvas from './components/DesignCanvas.vue'
import PropertyPanel from './components/PropertyPanel.vue'
import PreviewDialog from './components/PreviewDialog.vue'
import CodeDialog from './components/CodeDialog.vue'
import AiGenerateDialog from './components/AiGenerateDialog.vue'
import AiOptimizeDialog from './components/AiOptimizeDialog.vue'
import { showMsgError, showMsgSuccess } from '@/utils/modal'

defineOptions({ name: 'PageDesigner' })

const pageDesignerRef = ref<HTMLDivElement | null>(null)

// 页面设计器状态管理
const {
  schema,
  selectedId,
  selectedItem,
  canUndo,
  canRedo,
  hasItems,
  draggingItemType,
  setDraggingItemType,
  clearDraggingItemType,
  addItem,
  addItemToContainer,
  batchAddItems,
  batchUpdateItems,
  removeItem,
  copyItem,
  moveItem,
  selectItem,
  clearItems,
  undo,
  redo,
  saveHistory,
  importSchema
} = useFormSchema()

// 弹窗状态
const showPreview = ref(false)
const showCode = ref(false)
const showAiGenerate = ref(false)
const showAiOptimize = ref(false)

// 代码生成初始设置（从预览传递过来）
const codeInitialSettings = ref<{
  mode: 'page' | 'dialog' | 'drawer'
  size: 'small' | 'medium' | 'large' | 'xl'
  title?: string
} | null>(null)

// 打开代码弹窗
function handleOpenCode(settings?: { mode: 'page' | 'dialog' | 'drawer'; size: 'small' | 'medium' | 'large' | 'xl'; title?: string }) {
  codeInitialSettings.value = settings || null
  showCode.value = true
}

// 添加组件
function handleAddItem(type: FormItemType, index?: number) {
  addItem(type, index)
}

// 向容器添加组件
function handleAddToContainer(containerId: string, type: FormItemType) {
  addItemToContainer(containerId, type)
}

// 拖拽状态
function handleDragStart(type: FormItemType) {
  setDraggingItemType(type)
}

function handleDragEnd() {
  clearDraggingItemType()
}

// 属性更新
function handlePropertyUpdate() {
  saveHistory('更新属性')
}

// 导入配置
function handleImport(newSchema: FormSchema) {
  try {
    // 基础校验
    if (typeof newSchema !== 'object' || newSchema === null || !Array.isArray(newSchema.items)) {
      throw new Error('无效的配置格式，请检查后重试')
    }
    importSchema(newSchema)
  } catch (error: any) {
    showMsgError(error.message || '导入失败')
  }
}

// AI 生成组件
function handleAiGenerate(items: FormItemSchema[], mode: 'append' | 'replace') {
  if (items.length === 0) return
  batchAddItems(items, mode)
  showMsgSuccess(`已${mode === 'replace' ? '替换生成' : '追加'} ${items.length} 个组件`)
}

// AI 优化组件
function handleAiOptimize(updates: Array<{ id: string; updates: Partial<FormItemSchema> }>) {
  if (updates.length === 0) return
  batchUpdateItems(updates)
  showMsgSuccess(`已优化 ${updates.length} 个组件`)
}

// 键盘快捷键
function handleKeydown(e: KeyboardEvent) {
  // Ctrl+Z 撤销
  if (e.ctrlKey && e.key === 'z' && !e.shiftKey) {
    e.preventDefault()
    undo()
  }
  // Ctrl+Y 或 Ctrl+Shift+Z 重做
  if ((e.ctrlKey && e.key === 'y') || (e.ctrlKey && e.shiftKey && e.key === 'z')) {
    e.preventDefault()
    redo()
  }
  // Delete 删除选中组件
  if (e.key === 'Delete' && selectedId.value) {
    e.preventDefault()
    removeItem(selectedId.value)
  }
  // Escape 取消选中
  if (e.key === 'Escape') {
    selectItem(null)
  }
}

onMounted(() => {
  pageDesignerRef.value?.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  pageDesignerRef.value?.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped lang="scss">
.page-designer {
  display: flex;
  // 84px = 顶部导航栏(50px) + 标签页(34px)
  // 32px = AppMain 的 padding (16px * 2)
  height: calc(100vh - 84px - 32px);
  background: var(--el-bg-color-page);
  overflow: hidden;
  border-radius: 4px;

  .designer-left {
    width: 200px;
    flex-shrink: 0;
    height: 100%;
    overflow: hidden;
  }

  .designer-center {
    flex: 1;
    min-width: 0;
    height: 100%;
    overflow: hidden;
  }

  .designer-right {
    width: 320px;
    flex-shrink: 0;
    height: 100%;
    overflow: hidden;
  }
}
</style>
