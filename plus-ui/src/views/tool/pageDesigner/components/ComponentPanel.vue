<!-- 组件面板 -->
<template>
  <div class="component-panel">
    <div class="panel-header">
      <span class="panel-title">组件库</span>
    </div>
    <div class="panel-body">
      <el-collapse v-model="activeCategories" class="component-collapse">
        <el-collapse-item v-for="category in categories" :key="category" :title="category" :name="category">
          <div class="component-list">
            <div
              v-for="component in componentsByCategory[category]"
              :key="component.type"
              class="component-item"
              draggable="true"
              @dragstart="handleDragStart($event, component.type)"
              @dragend="handleDragEnd"
              @click="handleClick(component.type)"
            >
              <div class="component-icon">
                <Icon :code="component.icon" />
              </div>
              <span class="component-name">{{ component.name }}</span>
            </div>
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { FormItemType } from '../types'
import { getComponentsByCategory, getCategories } from '../config/componentConfig'

defineOptions({ name: 'ComponentPanel' })

const emit = defineEmits<{
  (e: 'add', type: FormItemType): void
  (e: 'drag-start', type: FormItemType): void
  (e: 'drag-end'): void
}>()

// 获取分类和组件
const categories = getCategories()
const componentsByCategory = getComponentsByCategory()

// 默认展开所有分类
const activeCategories = ref<string[]>([...categories])

// 拖拽开始
function handleDragStart(event: DragEvent, type: FormItemType) {
  if (event.dataTransfer) {
    event.dataTransfer.setData('componentType', type)
    event.dataTransfer.effectAllowed = 'copy'
  }
  emit('drag-start', type)
}

// 拖拽结束
function handleDragEnd() {
  emit('drag-end')
}

// 点击添加
function handleClick(type: FormItemType) {
  emit('add', type)
}
</script>

<style scoped lang="scss">
.component-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--el-bg-color);
  border-right: 1px solid var(--el-border-color-light);

  .panel-header {
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
    overflow-y: auto;
    padding: 8px;
  }

  .component-collapse {
    border: none;

    :deep(.el-collapse-item__header) {
      padding: 0 8px;
      font-size: 13px;
      font-weight: 500;
      background: transparent;
      border: none;
    }

    :deep(.el-collapse-item__wrap) {
      border: none;
    }

    :deep(.el-collapse-item__content) {
      padding-bottom: 8px;
    }
  }

  .component-list {
    display: flex;
    flex-direction: column;
    gap: 4px;
    padding: 4px 0;
  }

  .component-item {
    display: flex;
    flex-direction: row;
    align-items: center;
    gap: 8px;
    padding: 6px 8px;
    border: 1px solid transparent;
    border-radius: 4px;
    background: transparent;
    cursor: move;
    transition: all 0.15s;

    &:hover {
      border-color: var(--el-color-primary-light-5);
      background: var(--el-fill-color-light);
    }

    &:active {
      background: var(--el-fill-color);
    }

    .component-icon {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 24px;
      height: 24px;
      flex-shrink: 0;
      font-size: 14px;
      color: var(--el-color-primary);
      background: var(--el-fill-color-light);
      border-radius: 4px;
    }

    .component-name {
      flex: 1;
      font-size: 13px;
      color: var(--el-text-color-regular);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
}
</style>
