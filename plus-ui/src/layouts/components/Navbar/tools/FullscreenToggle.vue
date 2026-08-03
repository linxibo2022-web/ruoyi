<!-- 全屏切换 -->
<template>
  <el-tooltip :content="isFullscreen ? t('navbar.exitFull') : t('navbar.full')" effect="dark" placement="bottom">
    <div class="flex-center h-full px-1">
      <div class="navbar-tool-item flex-center w-9 h-9 rounded-2 cursor-pointer" @click="toggle">
        <Icon :code="isFullscreen ? 'exit-fullscreen' : 'expand'" size="md" :animate="isFullscreen ? 'shrink' : 'expand'" />
      </div>
    </div>
  </el-tooltip>
</template>

<script setup lang="ts" name="FullscreenToggle">
// 初始化全屏API和国际化
const { isFullscreen, toggle } = useFullscreen()
const { t } = useI18n()
</script>

<style lang="scss" scoped></style>
