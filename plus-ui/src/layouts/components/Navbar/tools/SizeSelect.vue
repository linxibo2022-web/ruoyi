<!-- 尺寸切换 -->
<template>
  <el-tooltip :content="t('navbar.layoutSize')" effect="dark" placement="bottom" :offset="6">
    <div class="flex-center h-full px-1">
      <el-dropdown trigger="click" @command="handleSetSize">
        <div class="navbar-tool-item flex-center w-9 h-9 rounded-2 cursor-pointer">
          <Icon code="size" size="md" animate="shake" />
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item v-for="item of sizeOptions" :key="item.value" :disabled="size === item.value" :command="item.value">
              {{ item.label }}
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </el-tooltip>
</template>

<script setup lang="ts" name="SizeSelect">
import { showMsgSuccess } from '@/utils/modal'

// 初始化布局状态管理和国际化
const layout = useLayout()
const { t } = useI18n()

// 获取当前应用尺寸设置
const size = layout.size

// 定义尺寸选项，使用国际化标签
const sizeOptions = computed(() => [
  {
    label: t('label.large'),
    value: 'large'
  },
  {
    label: t('label.default'),
    value: 'default'
  },
  {
    label: t('label.small'),
    value: 'small'
  }
])

/**
 * 处理尺寸设置
 * @param size - 选择的新尺寸
 */
const handleSetSize = (size: ElSize) => {
  layout.setSize(size)

  // 添加成功提示消息
  showMsgSuccess(t('navbar.sizeChangeSuccess'))
}
</script>

<style lang="scss" scoped></style>
