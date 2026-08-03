<!--
图标选择器组件
提供图标选择功能，支持筛选和实时预览

<IconSelect v-model="selectedIcon" width="300px" />
-->
<template>
  <div class="relative icon-select-wrapper" :style="{ 'width': width }">
    <el-input v-model="currentValue" readonly :placeholder="t('iconSelect.placeholder')" @click="handleInputClick">
      <template v-if="hasValidIcon" #prepend>
        <Icon :code="currentValue as IconCode" />
      </template>
      <template #suffix>
        <span class="suffix-icons">
          <el-icon v-if="hasValidIcon" class="clear-icon" @click.stop="handleClear">
            <CircleClose />
          </el-icon>
          <el-icon class="arrow-icon" @click.stop="visible = !visible">
            <ArrowUp v-if="visible" />
            <ArrowDown v-else />
          </el-icon>
        </span>
      </template>
    </el-input>

    <el-popover shadow="none" :visible="visible" placement="bottom-end" trigger="click" :width="450">
      <template #reference>
        <div class="popover-reference"></div>
      </template>

      <!-- 搜索 -->
      <div class="p-2 border-b">
        <el-input v-model="filterValue" :placeholder="t('iconSelect.searchPlaceholder')" clearable class="mb-2" />
      </div>

      <!-- 图标信息显示区域 -->
      <div class="icon-info-bar">
        <div v-if="displayIcon" class="flex items-center gap-2 text-sm">
          <Icon :code="displayIcon.code" class="text-lg" />
          <span class="font-medium">{{ displayIcon.name }}</span>
          <span class="text-gray-500">({{ displayIcon.code }})</span>
        </div>
        <div v-else class="text-sm text-gray-400">{{ t('iconSelect.totalIcons', { count: ALL_ICONS.length }) }}</div>
      </div>

      <!-- 图标列表 -->
      <el-scrollbar>
        <ul class="icon-list-class">
          <li
            v-for="icon in filteredIcons"
            :key="icon.code"
            :class="['icon-item-class', { active: currentValue === icon.code }]"
            @click="selectedIcon(icon.code)"
            @mouseenter="hoveredIcon = icon"
            @mouseleave="hoveredIcon = null"
          >
            <Icon :code="icon.code as IconCode" />
          </li>
        </ul>
      </el-scrollbar>
    </el-popover>
  </div>
</template>

<script setup lang="ts" name="IconSelect">
import { computed, ref, watch } from 'vue'
import { CircleClose, ArrowUp, ArrowDown } from '@element-plus/icons-vue'
import Icon from './Icon.vue'
import { ALL_ICONS, searchIcons, getIconName, type IconItem } from '@/types/icons.d'

const { t } = useI18n()

/**
 * 图标选择器组件的属性接口
 */
interface IconSelectProps {
  /**
   * 当前选中的图标代码
   * @required
   */
  modelValue: string

  /**
   * 组件宽度
   * @default '400px'
   */
  width?: string

  /**
   * 清空时的默认值
   * 用于某些场景需要保留占位符，如菜单图标需要 '#'
   * @default ''
   */
  emptyValue?: string
}

/**
 * 图标选择器组件
 * 提供图标选择功能，支持筛选和实时预览
 */
const props = withDefaults(defineProps<IconSelectProps>(), {
  modelValue: '',
  width: '400px',
  emptyValue: ''
})

/**
 * 定义组件事件
 */
const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

// 控制图标选择器可见性
const visible = ref(false)

// 筛选关键词
const filterValue = ref('')

// 当前悬停的图标
const hoveredIcon = ref(null)

/**
 * 双向绑定的计算属性，避免直接修改props
 */
const currentValue = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

/**
 * 是否有有效的图标（非空且不等于 emptyValue）
 */
const hasValidIcon = computed(() => {
  return currentValue.value && currentValue.value !== props.emptyValue
})

/**
 * 根据筛选条件过滤图标
 */
const filteredIcons = computed(() => {
  if (filterValue.value) {
    const keyword = filterValue.value.toLowerCase()
    return ALL_ICONS.filter((icon) => icon.code.toLowerCase().includes(keyword) || icon.name.toLowerCase().includes(keyword))
  }
  return ALL_ICONS
})

/**
 * 当前选中图标的信息
 */
const currentSelectedInfo = computed(() => {
  if (!currentValue.value) return null
  return ALL_ICONS.find((icon) => icon.code === currentValue.value)
})

/**
 * 要显示的图标信息（悬停优先，其次是选中的）
 */
const displayIcon = computed(() => {
  return hoveredIcon.value || currentSelectedInfo.value
})

/**
 * 选择图标
 * @param iconCode 选择的图标ID
 */
const selectedIcon = (iconCode: string) => {
  emit('update:modelValue', iconCode)
  visible.value = false
  hoveredIcon.value = null
}

/**
 * 处理输入框点击
 */
const handleInputClick = (event: MouseEvent) => {
  // 如果点击的是清除按钮，不打开弹窗
  const target = event.target as HTMLElement
  if (target.closest('.el-input__clear')) {
    return
  }
  visible.value = !visible.value
}

/**
 * 清空选中的图标
 */
const handleClear = () => {
  emit('update:modelValue', props.emptyValue)
  visible.value = false
}
</script>

<style lang="scss" scoped>
.icon-select-wrapper {
  .popover-reference {
    position: absolute;
    top: 0;
    right: 0;
    width: 100%;
    height: 100%;
    pointer-events: none;
  }

  .suffix-icons {
    display: inline-flex;
    align-items: center;
    gap: 4px;
  }

  .clear-icon,
  .arrow-icon {
    cursor: pointer;
    color: var(--el-text-color-placeholder);
    transition: color 0.2s;

    &:hover {
      color: var(--el-text-color-regular);
    }
  }
}

.el-scrollbar {
  max-height: calc(50vh - 140px) !important; // 为信息栏留出空间
  overflow-y: auto;
}

.icon-info-bar {
  padding: 4px 12px;
  margin: 0 7px;
  background-color: #fafafa;
  min-height: 36px;
  display: flex;
  align-items: center;
  border-radius: 4px;
}

.icon-list-class {
  display: flex;
  flex-wrap: wrap;
  padding: 8px;
  margin: 0;

  .icon-item-class {
    cursor: pointer;
    width: 32px;
    height: 32px;
    margin: 4px;
    display: flex;
    justify-content: center;
    align-items: center;
    border: 1px solid #eee;
    border-radius: 4px;
    transition: all 0.2s;

    &:hover {
      border-color: var(--el-color-primary);
      color: var(--el-color-primary);
      transform: scale(1.05); // 减小缩放效果
    }
  }

  .active {
    border-color: var(--el-color-primary);
    color: var(--el-color-primary);
    background-color: var(--el-color-primary-light-9);
  }
}
</style>
