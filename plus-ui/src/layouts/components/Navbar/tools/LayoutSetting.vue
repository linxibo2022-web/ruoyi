<!-- 布局设置 -->
<template>
  <el-tooltip :content="t('navbar.layoutSetting')" effect="dark" placement="bottom">
    <div class="flex-center h-full px-1">
      <div class="navbar-tool-item flex-center w-9 h-9 rounded-2 cursor-pointer" @click="openLayoutSetting">
        <Icon code="settings" size="md" animate="rotate180" />
      </div>
    </div>
  </el-tooltip>
</template>

<script setup lang="ts" name="LayoutSetting">
// 初始化国际化
const { t } = useI18n()

// 定义事件
const emit = defineEmits<{
  /** 设置布局事件 */
  'setLayout': []
}>()

// 打开布局设置
const openLayoutSetting = () => {
  emit('setLayout')
}
</script>

<style lang="scss" scoped></style>
