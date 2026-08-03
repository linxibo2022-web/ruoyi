<!-- 图标选择测试 -->
<template>
  <div class="p-5 min-h-screen bg-gray-50">
    <!-- 顶部搜索栏 -->
    <el-card shadow="never" class="mb-5">
      <div class="flex justify-between items-center mb-4">
        <h2 class="m-0 text-6 font-600 text-gray-800">图标库</h2>
        <el-tag type="info">共 {{ filteredIcons.length }} 个图标 (已加载 {{ displayedIcons.length }})</el-tag>
      </div>
      <el-input v-model="searchKeyword" placeholder="搜索图标名称或代码..." clearable class="max-w-100" @input="handleSearchInput">
        <template #prefix>
          <Icon code="search" />
        </template>
      </el-input>
    </el-card>

    <!-- 图标网格 - 懒加载 -->
    <div v-if="filteredIcons.length > 0" class="grid grid-cols-[repeat(auto-fill,minmax(140px,1fr))] gap-4">
      <el-card
        v-for="icon in displayedIcons"
        :key="icon.code"
        v-memo="[icon.code, hoveredIcon === icon.code]"
        shadow="hover"
        class="icon-card relative cursor-pointer rd-2"
        @mouseenter="hoveredIcon = icon.code"
        @mouseleave="hoveredIcon = null"
      >
        <template #default>
          <!-- 复制按钮组 -->
          <div
            class="absolute top-2 right-2 flex gap-1 z-10 transition-opacity duration-200"
            :class="hoveredIcon === icon.code ? 'opacity-100' : 'opacity-0'"
          >
            <el-tooltip content="复制图标代码" placement="top">
              <el-button size="small" circle class="w-7 h-7 p-0 text-3.5!" @click="copyIconCode(icon.code)">
                <Icon code="copy" />
              </el-button>
            </el-tooltip>
            <el-tooltip content="复制组件代码" placement="top">
              <el-button size="small" circle type="primary" class="w-7 h-7 p-0 text-3.5!" @click="copyComponentCode(icon.code)">
                <Icon code="code" />
              </el-button>
            </el-tooltip>
          </div>

          <!-- 图标显示 -->
          <div
            class="flex justify-center items-center h-15 mb-3 text-gray-600 transition-colors duration-200"
            :class="{ 'text-primary!': hoveredIcon === icon.code }"
          >
            <Icon :code="icon.code as IconCode" size="28px" />
          </div>

          <!-- 图标信息 -->
          <div class="text-center">
            <div class="text-3.5 font-500 text-gray-800 mb-1 truncate" :title="icon.name">{{ icon.name }}</div>
            <div class="text-3 text-gray-500 font-mono truncate" :title="icon.code">{{ icon.code }}</div>
          </div>
        </template>
      </el-card>
    </div>

    <!-- 加载更多触发器 -->
    <div v-if="hasMore" ref="loadMoreRef" class="flex justify-center items-center h-20 mt-5">
      <el-icon class="is-loading" size="24">
        <Loading />
      </el-icon>
      <span class="ml-2 text-gray-500">加载中...</span>
    </div>

    <!-- 全部加载完成提示 -->
    <div v-else-if="filteredIcons.length > 0" class="text-center text-gray-400 py-5">已加载全部图标</div>

    <!-- 空状态 -->
    <el-empty v-if="filteredIcons.length === 0" description="未找到匹配的图标" class="mt-15" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, nextTick } from 'vue'
import { useDebounceFn, useIntersectionObserver } from '@vueuse/core'
import { Loading } from '@element-plus/icons-vue'
import Icon from '@/components/Icon/Icon.vue'
import { ALL_ICONS } from '@/types/icons.d'
import { copy } from '@/utils/function'

// 搜索关键词
const searchKeyword = ref('')

// 防抖搜索
const debouncedSearch = ref('')
const updateSearch = useDebounceFn((value: string) => {
  debouncedSearch.value = value
}, 300)

// 监听搜索输入
const handleSearchInput = (value: string) => {
  updateSearch(value)
}

// 当前悬停的图标
const hoveredIcon = ref<string | null>(null)

// 懒加载配置
const PAGE_SIZE = 100 // 每页加载数量
const currentPage = ref(1)

/**
 * 过滤后的图标列表
 */
const filteredIcons = computed(() => {
  const keyword = debouncedSearch.value.toLowerCase()
  if (!keyword) {
    return ALL_ICONS
  }
  return ALL_ICONS.filter((icon) => icon.code.toLowerCase().includes(keyword) || icon.name.toLowerCase().includes(keyword))
})

/**
 * 当前显示的图标列表
 */
const displayedIcons = computed(() => {
  return filteredIcons.value.slice(0, currentPage.value * PAGE_SIZE)
})

/**
 * 是否还有更多数据
 */
const hasMore = computed(() => {
  return displayedIcons.value.length < filteredIcons.value.length
})

/**
 * 加载更多
 */
const loadMore = () => {
  if (hasMore.value) {
    currentPage.value++
  }
}

// 加载更多触发器
const loadMoreRef = ref<HTMLElement>()

// 监听滚动到底部
useIntersectionObserver(
  loadMoreRef,
  ([{ isIntersecting }]) => {
    if (isIntersecting && hasMore.value) {
      loadMore()
    }
  },
  {
    threshold: 0.1
  }
)

/**
 * 重置分页
 */
const resetPagination = () => {
  currentPage.value = 1
}

// 监听搜索变化,重置分页
watch(debouncedSearch, () => {
  resetPagination()
})

/**
 * 复制图标代码到剪贴板
 */
const copyIconCode = (code: string) => {
  copy(code, `已复制图标代码: ${code}`)
}

/**
 * 复制组件代码到剪贴板
 */
const copyComponentCode = (code: string) => {
  const componentCode = `<Icon code="${code}" />`
  copy(componentCode, '已复制组件代码')
}

// 初始化
onMounted(() => {
  // 预加载第一页
  nextTick(() => {
    resetPagination()
  })
})
</script>

<style scoped lang="scss">
.icon-card {
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
}
</style>
