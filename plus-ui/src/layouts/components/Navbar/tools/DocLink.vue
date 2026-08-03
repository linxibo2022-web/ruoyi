<!-- 文档链接 -->
<template>
  <el-tooltip :content="t('navbar.document')" effect="dark" placement="bottom" v-if="SystemConfig.services.docUrl">
    <div class="flex-center h-full px-1">
      <div class="navbar-tool-item flex-center w-9 h-9 rounded-2 cursor-pointer" @click="gotoDoc">
        <Icon code="documentation" size="md" />
      </div>
    </div>
  </el-tooltip>
</template>

<script setup lang="ts" name="DocLink">
import { SystemConfig } from '@/systemConfig'
// 初始化国际化
const { t } = useI18n()

// 前往文档
const gotoDoc = () => {
  window.open(SystemConfig.services.docUrl)
}
</script>

<style lang="scss" scoped></style>
