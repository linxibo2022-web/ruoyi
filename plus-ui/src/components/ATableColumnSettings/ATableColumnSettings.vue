<!-- 表格列设置 -->
<template>
  <el-popover ref="popoverRef" placement="bottom-end" :width="280" trigger="click" popper-class="table-column-settings-popover">
    <template #reference>
      <slot>
        <el-button circle icon="Setting" />
      </slot>
    </template>

    <div class="column-settings">
      <div class="column-settings__header">
        <span class="column-settings__title">{{ t('columnSettings.title') }}</span>
        <el-button link type="primary" @click="handleReset">
          {{ t('columnSettings.reset') }}
        </el-button>
      </div>

      <div class="column-settings__actions">
        <span class="column-settings__hint">{{ t('columnSettings.dragHint') }}</span>
        <span class="column-settings__count">{{ visibleCount }}/{{ totalCount }}</span>
      </div>

      <div class="column-settings__content">
        <VueDraggable v-model="localColumns" :animation="150" class="column-list" @end="handleDragEnd">
          <div v-for="item in localColumns" :key="item.field" class="column-item">
            <el-checkbox
              v-model="item.visible"
              :disabled="item.visible && visibleCount <= 1"
              @change="handleVisibleChange"
              @click.stop
            />
            <el-icon class="drag-icon">
              <Rank />
            </el-icon>
            <span class="column-item__label">{{ item.label }}</span>
          </div>
        </VueDraggable>
      </div>
    </div>
  </el-popover>
</template>

<script setup lang="ts" name="ATableColumnSettings">
import { VueDraggable } from 'vue-draggable-plus'
import { showMsgSuccess } from '@/utils/modal'

const { t } = useI18n()
const route = useRoute()

/**
 * 列配置接口
 */
export interface ColumnConfig {
  field: string
  label: string
  visible: boolean
  width?: number | string
  minWidth?: number | string
  align?: 'left' | 'center' | 'right'
}

interface Props {
  /** 列配置 */
  columns: ColumnConfig[]
  /** 缓存 key，不传则自动基于路由生成 */
  cacheKey?: string
}

const props = defineProps<Props>()

const emit = defineEmits<{
  (e: 'update:columns', columns: ColumnConfig[]): void
}>()

/** 自动生成缓存 key */
const autoCacheKey = computed(() => props.cacheKey || `table-columns-${route.path}`)

const popoverRef = ref()

/** 保存初始列配置（用于重置） */
const initialColumns = ref<ColumnConfig[]>([])

/** 从缓存加载列配置（会话级缓存） */
const loadFromCache = (): ColumnConfig[] => {
  // 保存初始配置（深拷贝，保持原始顺序）
  initialColumns.value = props.columns.map((col) => ({ ...col }))

  try {
    const cached = sessionStorage.getItem(autoCacheKey.value)
    if (cached) {
      const { order, visibility } = JSON.parse(cached)
      const result: ColumnConfig[] = []

      order.forEach((field: string) => {
        const col = props.columns.find((c) => c.field === field)
        if (col) {
          result.push({ ...col, visible: visibility[field] ?? col.visible })
        }
      })

      // 添加缓存中没有的新列
      props.columns.forEach((col) => {
        if (!result.find((c) => c.field === col.field)) {
          result.push({ ...col })
        }
      })

      return result
    }
  } catch (e) {
    console.warn('加载列配置缓存失败:', e)
  }

  return props.columns.map((col) => ({ ...col }))
}

// 同步初始化（立即从缓存加载）
const localColumns = ref<ColumnConfig[]>(loadFromCache())

/** 获取可见列 */
const visibleColumns = computed(() => localColumns.value.filter((col) => col.visible))

// 组件挂载后 emit 初始可见列
onMounted(() => {
  emit('update:columns', visibleColumns.value)
})

/** 保存列配置到缓存（会话级缓存） */
const saveToCache = () => {
  try {
    const data = {
      order: localColumns.value.map((col) => col.field),
      visibility: localColumns.value.reduce(
        (acc, col) => {
          acc[col.field] = col.visible
          return acc
        },
        {} as Record<string, boolean>
      )
    }
    sessionStorage.setItem(autoCacheKey.value, JSON.stringify(data))
  } catch (e) {
    console.warn('保存列配置缓存失败:', e)
  }
}

// 计算属性
const visibleCount = computed(() => localColumns.value.filter((col) => col.visible).length)
const totalCount = computed(() => localColumns.value.length)

/** 触发变更 */
const emitChange = () => {
  saveToCache()
  emit('update:columns', visibleColumns.value)
}

const handleDragEnd = () => emitChange()
const handleVisibleChange = () => emitChange()

const handleReset = () => {
  sessionStorage.removeItem(autoCacheKey.value)
  // 使用初始配置恢复（保持原始顺序和可见性）
  localColumns.value = initialColumns.value.map((col) => ({ ...col }))
  emitChange()
  popoverRef.value?.hide()
  showMsgSuccess(t('columnSettings.resetSuccess'))
}

// 暴露给父组件
defineExpose({
  columns: localColumns,
  visibleColumns
})
</script>

<style lang="scss" scoped>
.column-settings {
  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 12px;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  &__title {
    font-weight: 500;
    font-size: 14px;
    color: var(--el-text-color-primary);
  }

  &__actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 0;
    border-bottom: 1px solid var(--el-border-color-lighter);
    margin-bottom: 8px;
  }

  &__count {
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }

  &__hint {
    font-size: 12px;
    color: var(--el-text-color-placeholder);
  }

  &__content {
    max-height: 280px;
    overflow-y: auto;
  }
}

.column-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.column-item {
  display: flex;
  align-items: center;
  padding: 8px 4px;
  border-radius: 6px;
  transition: background-color 0.2s;
  gap: 8px;
  cursor: move;

  &:hover {
    background-color: var(--el-fill-color-light);
  }

  .drag-icon {
    color: var(--el-text-color-placeholder);
    font-size: 16px;
  }

  &__label {
    flex: 1;
    font-size: 13px;
    color: var(--el-text-color-regular);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

:deep(.sortable-ghost) {
  opacity: 0.5;
  background-color: var(--el-color-primary-light-9);
}

:deep(.sortable-chosen) {
  background-color: var(--el-color-primary-light-8);
}
</style>

<style lang="scss">
.table-column-settings-popover {
  padding: 12px !important;
}
</style>
