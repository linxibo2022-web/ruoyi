<!-- 导航搜索 -->
<template>
  <!-- 头部搜索组件容器 -->
  <div class="inline-block">
    <!-- 搜索包装器：包含搜索图标和输入框 -->
    <div class="flex items-center h-50px px-1" :class="{ 'expanded': show }">
      <!-- 搜索触发按钮：点击显示/隐藏搜索框 -->
      <el-tooltip :content="t('Search Menu', '搜索菜单')" effect="dark" placement="bottom">
        <div
          class="navbar-tool-item flex items-center justify-center w-9 h-9 cursor-pointer rounded-2 transition-all duration-300"
          @click.stop="toggleSearch"
        >
          <!-- 搜索图标：根据展开状态改变颜色 -->
          <Icon code="search" size="md" animate="shake" />
        </div>
      </el-tooltip>

      <!-- 搜索下拉选择器：仅在展开时显示 -->
      <div v-show="show">
        <el-select
          class="w-50! ml-2"
          ref="headerSearchSelectRef"
          v-model="search"
          :placeholder="t('Search Menu', '搜索菜单')"
          filterable
          default-first-option
          remote
          :remote-method="querySearch"
          @change="handleSelect"
          @blur="handleBlur"
        >
          <!-- 搜索结果选项：显示菜单图标和层级标题 -->
          <el-option v-for="option in options" :key="option.item.path" :value="option.item" :label="option.item.title.join(' > ')">
            <div class="flex items-center text-#777 font-normal text-13px">
              <!-- 菜单图标：优先显示路由图标，否则显示文件夹图标 -->
              <Icon v-if="option.item.icon" :code="option.item.icon" />
              <Icon v-else code="folder" />
              <!-- 菜单层级标题：用 > 分隔显示完整路径 -->
              <span class="ml-1">{{ option.item.title.join(' > ') }}</span>
            </div>
          </el-option>
        </el-select>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts" name="NavbarSearch">
import { useRouter } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import Fuse from 'fuse.js'
import { normalizePath } from '@/utils/string'
import { isHttp } from '@/utils/validators'

// 初始化国际化
const { t } = useI18n()

/**
 * 路由搜索项接口定义
 * @interface RouterItem
 * @property {string} path - 路由路径
 * @property {string[]} title - 层级标题数组，用于显示面包屑导航
 * @property {string} [icon] - 可选的图标代码
 * @property {string} [query] - 可选的查询参数JSON字符串
 */
interface RouterItem {
  path: string
  title: string[]
  icon?: IconCode
  query?: string
}

/**
 * Fuse.js搜索结果接口定义
 * @interface FuseResult
 * @property {RouterItem} item - 匹配的路由项
 * @property {number} refIndex - 结果在原数组中的索引
 */
interface FuseResult {
  item: RouterItem
  refIndex: number
}

// ====================== 响应式数据定义 ======================
/** 当前搜索关键词 */
const search = ref('')

/** 搜索结果选项列表 */
const options = ref<FuseResult[]>([])

/** 可搜索的路由池 - 扁平化处理后的所有路由数据 */
const searchPool = ref<RouterItem[]>([])

/** 搜索框显示状态 */
const show = ref(false)

/** Fuse.js搜索引擎实例 */
const fuse = ref<Fuse<RouterItem>>()

/** 搜索选择器组件引用 */
const headerSearchSelectRef = ref()

// ====================== Composables ======================
const router = useRouter()
/** 获取权限过滤后的路由列表 */
const routes = computed(() => usePermissionStore().getDefaultRoutes())

// ====================== 搜索框状态控制 ======================
/**
 * 切换搜索框显示状态
 * 显示时自动聚焦输入框，隐藏时重置搜索状态
 */
const toggleSearch = async () => {
  show.value = !show.value

  if (show.value) {
    // 显示时聚焦输入框 - 使用nextTick确保DOM更新完成
    await nextTick()
    headerSearchSelectRef.value?.focus()
  } else {
    // 隐藏时重置状态
    resetSearch()
  }
}

/**
 * 重置搜索状态
 * 清空搜索词、结果列表并失焦输入框
 */
const resetSearch = () => {
  search.value = ''
  options.value = []
  headerSearchSelectRef.value?.blur()
}

/**
 * 关闭搜索框
 * 隐藏搜索框并重置所有状态
 */
const closeSearch = () => {
  show.value = false
  resetSearch()
}

/**
 * 处理输入框失焦事件
 * 延迟关闭以避免在选择选项时立即关闭搜索框
 */
const handleBlur = () => {
  // 150ms延迟确保用户有足够时间点击选项
  setTimeout(() => {
    closeSearch()
  }, 150)
}

// ====================== 路由导航处理 ======================
/**
 * 处理菜单项选择事件
 * @param {RouterItem} val - 选中的路由项
 */
const handleSelect = (val: RouterItem) => {
  if (!val) return

  const path = val.path
  const query = val.query

  try {
    // 判断是否为外部链接
    if (isHttp(path)) {
      // 外部链接在新窗口打开
      const httpIndex = path.indexOf('http')
      window.open(path.substring(httpIndex), '_blank')
    } else {
      // 内部路由导航
      if (query) {
        // 带查询参数的路由跳转
        router.push({ path: path, query: JSON.parse(query) })
      } else {
        // 普通路由跳转
        router.push(path)
      }
    }
  } catch (error) {
    // 错误处理：如果JSON解析失败等，回退到简单路由跳转
    console.error('路由跳转失败:', error)
    router.push(path)
  }

  // 成功导航后关闭搜索框
  closeSearch()
}

// ====================== 搜索引擎配置 ======================
/**
 * 初始化Fuse.js搜索引擎
 * @param {RouterItem[]} list - 可搜索的路由列表
 */
const initFuse = (list: RouterItem[]) => {
  fuse.value = new Fuse(list, {
    shouldSort: true, // 对结果进行排序
    threshold: 0.4, // 匹配阈值：0完全匹配，1匹配任何内容
    location: 0, // 预期匹配位置
    distance: 100, // 匹配位置的最大距离
    minMatchCharLength: 1, // 最小匹配字符长度
    keys: [
      {
        name: 'title', // 搜索路由标题，权重更高
        weight: 0.7
      },
      {
        name: 'path', // 搜索路由路径，权重较低
        weight: 0.3
      }
    ]
  })
}

// ====================== 路由数据处理 ======================
/**
 * 递归生成可搜索的路由列表
 * 将嵌套的路由结构扁平化为包含完整路径信息的搜索项
 *
 * @param {RouteRecordRaw[]} routes - 路由配置数组
 * @param {string} basePath - 基础路径，用于构建完整路径
 * @param {string[]} prefixTitle - 父级标题数组，用于构建面包屑
 * @param {string} [parentIcon] - 父级图标，子路由可继承
 * @returns {RouterItem[]} 扁平化的路由搜索项数组
 */
const generateRoutes = (routes: RouteRecordRaw[], basePath = '', prefixTitle: string[] = [], parentIcon?: string): RouterItem[] => {
  let res: RouterItem[] = []

  routes.forEach((r) => {
    // 跳过隐藏的路由（hidden: true）
    if (r.hidden) return

    // 构建完整路径：处理相对路径和绝对路径
    const p = r.path.length > 0 && r.path[0] === '/' ? r.path : '/' + r.path
    const data: RouterItem = {
      path: !isHttp(r.path) ? normalizePath(basePath + p) : r.path,
      title: [...prefixTitle] // 复制父级标题数组
    }

    // 处理路由元信息
    if (r.meta?.title) {
      // 添加当前路由标题到面包屑
      data.title = [...data.title, r.meta.title]

      // 图标优先级：当前路由图标 > 父级图标
      if (r.meta.icon) {
        data.icon = r.meta.icon
      } else if (parentIcon) {
        data.icon = parentIcon as IconCode
      }

      // 只添加有标题且不是重定向占位符的路由
      if (r.redirect !== 'noRedirect') {
        res.push(data)
      }
    }

    // 添加查询参数（如果存在）
    if (r.query) {
      data.query = r.query
    }

    // 递归处理子路由
    if (r.children && r.children.length > 0) {
      // 传递当前图标给子路由作为父级图标
      const currentIcon = r.meta?.icon || parentIcon
      const tempRoutes = generateRoutes(r.children, data.path, data.title, currentIcon)
      if (tempRoutes.length >= 1) {
        res = [...res, ...tempRoutes]
      }
    }
  })

  return res
}

// ====================== 搜索功能 ======================
/**
 * 执行搜索查询
 * @param {string} query - 搜索关键词
 */
const querySearch = (query: string) => {
  if (query !== '' && fuse.value) {
    // 使用Fuse.js执行模糊搜索
    options.value = fuse.value.search(query)
  } else {
    // 清空搜索结果
    options.value = []
  }
}

// ====================== 全局事件处理 ======================
/**
 * 处理全局点击事件
 * 点击组件外部时关闭搜索框
 * @param {MouseEvent} event - 点击事件对象
 */
const handleGlobalClick = (event: MouseEvent) => {
  const target = event.target as HTMLElement
  const searchElement = document.querySelector('.header-search')

  // 如果点击在搜索组件外部，则关闭搜索框
  if (searchElement && !searchElement.contains(target)) {
    closeSearch()
  }
}

// ====================== 响应式监听 ======================
/**
 * 监听搜索框显示状态
 * 显示时添加全局点击监听，隐藏时移除监听
 */
watch(show, (value) => {
  if (value) {
    document.addEventListener('click', handleGlobalClick)
  } else {
    document.removeEventListener('click', handleGlobalClick)
  }
})

/**
 * 监听搜索池变化
 * 当路由数据更新时重新初始化搜索引擎
 */
watch(
  searchPool,
  (list: RouterItem[]) => {
    initFuse(list)
  },
  { deep: true } // 深度监听数组内容变化
)

/**
 * 监听路由配置变化
 * 当权限路由更新时重新生成搜索池
 */
watch(
  routes,
  (newRoutes) => {
    searchPool.value = generateRoutes(newRoutes)
  },
  { deep: true } // 深度监听路由配置变化
)

// ====================== 生命周期钩子 ======================
/**
 * 组件挂载时初始化
 * 生成搜索池并初始化搜索引擎
 */
onMounted(() => {
  searchPool.value = generateRoutes(routes.value)
  initFuse(searchPool.value)
})

/**
 * 组件卸载时清理
 * 移除全局事件监听器，防止内存泄漏
 */
onUnmounted(() => {
  document.removeEventListener('click', handleGlobalClick)
})
</script>
