<!-- 定时任务 -->
<template>
  <div>
    <!-- 内链打开：使用 iframe -->
    <IFrameContainer v-if="currentOpenMode === 'internal'" :src="url" />

    <!-- 外链打开：显示跳转提示 -->
    <div v-else class="flex items-start justify-center min-h-screen pt-50">
      <div class="text-center">
        <div class="mb-6">
          <div class="text-6xl mb-4">🐌</div>
          <h2 class="text-xl font-semibold text-gray-700 mb-2">
            {{ displayText.title }}
          </h2>
          <p class="text-gray-500">
            {{ displayText.description }}
          </p>
        </div>

        <div class="text-sm text-gray-400 mb-8 cursor-pointer" @click="copyUrl()">
          <span>{{ url }}</span>
          <span class="ml-1" :class="{ 'copied': isCopied }">
            {{ isCopied ? '✓' : '📋' }}
          </span>
        </div>

        <el-button @click="openExternal"> 立即打开</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { SystemConfig } from '@/systemConfig'
import { copy } from '@/utils/function'

// 是否启用自动检测模式
const autoDetect = ref<boolean>(true)

// 控制开发环境打开方式的 ref 变量
// 'internal' = 内链打开(iframe), 'external' = 外链打开
const devOpenMode = ref<'internal' | 'external'>('internal')

// 控制生产环境打开方式的 ref 变量
// 'internal' = 内链打开(iframe), 'external' = 外链打开
const prodOpenMode = ref<'internal' | 'external'>('external')

// 控制跳转状态的文案
const isPreparing = ref<boolean>(true)
// 是否已复制
const isCopied = ref(false)
// 访问地址
const url = computed(() => SystemConfig.services.snailJob)

const isDevelopment = computed(() => {
  return SystemConfig.app.env === 'development'
})

// 检测父页面是否为 https
const isParentHttps = computed(() => {
  return window.location.protocol === 'https:'
})

// 计算当前环境应该使用的打开方式
const currentOpenMode = computed(() => {
  if (autoDetect.value) {
    // 自动检测模式：父页面是 https 用外链，http 用 iframe (不过对于snailjob 统一使用内链即可 如果需要可以在此处修改)
    return isParentHttps.value ? 'internal' : 'internal'
  } else {
    // 手动模式：根据环境设置判断
    return isDevelopment.value ? devOpenMode.value : prodOpenMode.value
  }
})

// 计算显示的标题和描述
const displayText = computed(() => {
  if (isPreparing.value) {
    return {
      title: '正在跳转...',
      description: '即将为您打开Snailjob控制台'
    }
  } else {
    return {
      title: 'Snailjob控制台链接已准备就绪',
      description: '请点击下方按钮打开，或复制链接手动访问'
    }
  }
})

// 打开外部链接
const openExternal = () => {
  if (!url.value) return
  // 尝试新标签页打开
  window.open(url.value, '_blank')
}

/** 拷贝访问链接 */
const copyUrl = () => {
  copy(url.value)
  isCopied.value = true
}

// 自动跳转逻辑
onMounted(() => {
  let shouldAutoOpen = false

  if (autoDetect.value) {
    // 自动检测模式：父页面是 https 才自动跳转
    shouldAutoOpen = isParentHttps.value
  } else {
    // 手动模式：根据环境设置判断
    shouldAutoOpen = isDevelopment.value ? devOpenMode.value === 'external' : prodOpenMode.value === 'external'
  }

  if (shouldAutoOpen) {
    // 延迟一点时间让用户看到跳转提示
    setTimeout(() => {
      openExternal()
      isPreparing.value = false
    }, 1500)
  }
})
</script>
