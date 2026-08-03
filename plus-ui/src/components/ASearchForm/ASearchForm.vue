<!--
通用搜索表单组件

// 1. 基础用法
<ASearchForm v-model="queryParams" title="搜索条件">
  <el-form-item label="用户名" prop="userName">
    <el-input v-model="queryParams.userName" placeholder="请输入用户名" />
  </el-form-item>
</ASearchForm>

// 2. 带展开/收起功能（大于等于2行自动显示，收起时只显示第1行）
<ASearchForm v-model="queryParams" title="搜索条件" :collapsible="true">
  <el-form-item label="用户名" prop="userName">
    <el-input v-model="queryParams.userName" placeholder="请输入用户名" />
  </el-form-item>
  <el-form-item label="手机号" prop="phone">
    <el-input v-model="queryParams.phone" placeholder="请输入手机号" />
  </el-form-item>
  <el-form-item label="状态" prop="status">
    <el-select v-model="queryParams.status" placeholder="请选择状态">
      <el-option label="正常" value="0" />
      <el-option label="停用" value="1" />
    </el-select>
  </el-form-item>
</ASearchForm>

// 3. 默认展开
<ASearchForm v-model="queryParams" :default-expanded="true">
</ASearchForm>

// 4. 禁用展开/收起功能
<ASearchForm v-model="queryParams" :collapsible="false">
</ASearchForm>
-->
<template>
  <transition :enter-active-class="searchAnimate.enter" :leave-active-class="searchAnimate.leave">
    <div v-show="visible" class="mb-[10px]">
      <el-card shadow="hover">
        <!-- 自定义卡片头部插槽 -->
        <template v-if="$slots.header" #header>
          <slot name="header"></slot>
        </template>
        <template v-else-if="title" #header>
          <div class="flex items-center justify-between">
            <h5 class="m-0">{{ title }}</h5>
          </div>
        </template>
        <div ref="formContainerRef" class="search-form-container" :class="{ 'is-collapsed': !isExpanded && showCollapseButton }">
          <el-form ref="formRef" :model="formModel" :inline="inline" :label-width="labelWidth" :label-position="labelPosition">
            <!-- 表单内容插槽 -->
            <slot></slot>
          </el-form>
          <!-- 展开/收起按钮 -->
          <div v-if="showCollapseButton" class="collapse-button" @click="toggleExpanded">
            <span class="toggle-text">{{ isExpanded ? t('button.collapse') : t('button.expand') }}</span>
            <el-icon class="toggle-icon" :class="{ 'is-expanded': isExpanded }">
              <component :is="isExpanded ? 'ArrowUp' : 'ArrowDown'" />
            </el-icon>
          </div>
        </div>
      </el-card>
    </div>
  </transition>
</template>

<script setup lang="ts" name="ASearchForm">
/**
 * SearchForm 通用搜索表单组件
 *
 * 该组件封装了带有动画效果的搜索表单容器，支持v-model双向绑定和表单显示/隐藏控制
 * @example
 * // 设置自定义样式和布局
 * <ASearchForm
 *   v-model="queryParams"
 *   :visible="showSearch"
 *   :inline="false"
 *   label-width="auto"
 *   label-position="left"
 *   title="搜索条件"
 * >
 *   <!-- 表单项 -->
 * </ASearchForm>
 */
import { searchAnimate } from '@/composables/useAnimation'
import type { FormInstance } from 'element-plus'

const { t } = useI18n()

/**
 * 组件属性定义
 */
interface Props {
  /**
   * 表单数据模型，通过v-model绑定
   * @default {}
   */
  modelValue: Record<string, any>

  /**
   * 控制表单显示/隐藏
   * @default true
   */
  visible?: boolean

  /**
   * 是否行内表单
   * @default true
   */
  inline?: boolean

  /**
   * 标签宽度
   * @default 'auto'
   */
  labelWidth?: string

  /**
   * 标签位置
   * @default 'right'
   * label-position="right" 表示标签文本右对齐（标签区域内靠右）
   * label-position="left" 表示标签文本左对齐（标签区域内靠左）
   */
  labelPosition?: 'left' | 'right' | 'top'

  /**
   * 卡片标题（当没有使用header插槽时显示）
   * @default ''
   */
  title?: string

  /**
   * 是否启用展开/收起功能（当表单项超过2行时显示）
   * @default true
   */
  collapsible?: boolean

  /**
   * 默认是否展开
   * @default false
   */
  defaultExpanded?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: () => ({}),
  visible: true,
  inline: true,
  labelWidth: 'auto',
  labelPosition: 'right',
  title: '',
  collapsible: true,
  defaultExpanded: false
})

/**
 * 定义组件事件
 */
const emit = defineEmits(['update:modelValue', 'search', 'reset'])

/**
 * 表单引用，用于调用表单方法如resetFields
 */
const formRef = ref<FormInstance>()

/**
 * 表单容器引用，用于计算行数
 */
const formContainerRef = ref<HTMLElement>()

/**
 * 使用computed替代双向绑定，优化性能
 */
const formModel = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// ========== 展开/收起功能 ==========

/**
 * 是否展开
 */
const isExpanded = ref(props.defaultExpanded)

/**
 * 表单行数
 */
const formRows = ref(0)

/**
 * 是否显示展开/收起按钮
 * 当表单行数大于等于2行时显示
 */
const showCollapseButton = computed(() => {
  return props.collapsible && formRows.value >= 2
})

/**
 * 计算表单行数
 */
const calculateFormRows = () => {
  if (!props.inline || !formContainerRef.value) {
    formRows.value = 0
    return
  }

  nextTick(() => {
    const formElement = formContainerRef.value?.querySelector('.el-form') as HTMLElement
    if (!formElement) return

    const formItems = formElement.querySelectorAll('.el-form-item')
    if (formItems.length === 0) {
      formRows.value = 0
      return
    }

    // 使用 Map 来统计不同 top 值的数量（即行数）
    const topValues = new Set<number>()

    formItems.forEach((item) => {
      const itemTop = Math.round((item as HTMLElement).offsetTop)
      topValues.add(itemTop)
    })

    const currentRow = topValues.size
    formRows.value = currentRow
  })
}

/**
 * 切换展开/收起状态
 */
const toggleExpanded = () => {
  isExpanded.value = !isExpanded.value
}

// 监听表单项变化，重新计算行数
watch(
  () => props.visible,
  (newVal) => {
    if (newVal) {
      // 延迟一下确保DOM完全渲染
      setTimeout(() => {
        calculateFormRows()
      }, 100)
    }
  },
  { immediate: true }
)

// 监听插槽内容变化（通过 MutationObserver）
let observer: MutationObserver | null = null

// 窗口大小改变时重新计算行数
onMounted(() => {
  calculateFormRows()
  window.addEventListener('resize', calculateFormRows)

  // 监听表单内容变化
  nextTick(() => {
    const formElement = formContainerRef.value?.querySelector('.el-form') as HTMLElement
    if (formElement) {
      observer = new MutationObserver(() => {
        calculateFormRows()
      })
      observer.observe(formElement, {
        childList: true,
        subtree: true
      })
    }
  })
})

onUnmounted(() => {
  window.removeEventListener('resize', calculateFormRows)
  observer?.disconnect()
})

/**
 * 暴露组件方法供父组件调用
 */
defineExpose({
  /**
   * 重置表单字段到初始值
   */
  resetFields: () => {
    formRef.value?.resetFields()
    emit('reset')
  },

  /**
   * 重新计算表单行数
   */
  calculateFormRows,

  /**
   * 展开表单
   */
  expand: () => {
    isExpanded.value = true
  },

  /**
   * 收起表单
   */
  collapse: () => {
    isExpanded.value = false
  },

  /**
   * 表单引用，可用于直接操作el-form实例
   */
  formRef
})
</script>

<style lang="scss" scoped>
:deep(.el-card__body) {
  padding: 10px 20px 0 20px !important;
}

.search-form-container {
  position: relative;

  // 收起状态：只显示第一行
  &.is-collapsed {
    :deep(.el-form) {
      max-height: calc(1 * 40px + 10px); // 1行表单项的高度 + 底部间距
      overflow: hidden;
      position: relative;

      // 添加渐变遮罩效果
      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        height: 30px;
        background: linear-gradient(to bottom, transparent, var(--el-bg-color));
        pointer-events: none;
      }
    }
  }

  // 展开/收起按钮样式
  .collapse-button {
    position: absolute;
    right: -20px;
    bottom: 8px;
    z-index: 1;
    display: flex;
    align-items: center;
    padding: 4px 8px;
    color: var(--el-color-primary);
    cursor: pointer;
    user-select: none;
    transition: all 0.2s ease;

    &:hover {
      color: var(--el-color-primary-light-3);
    }

    .toggle-text {
      font-size: 14px;
      margin-right: 4px;
    }

    .toggle-icon {
      font-size: 14px;
      transition: transform 0.3s ease;
    }
  }
}
</style>
