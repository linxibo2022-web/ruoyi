<!-- 面包屑导航 -->
<template>
  <el-breadcrumb class="app-breadcrumb" separator="/">
    <transition-group name="breadcrumb">
      <el-breadcrumb-item v-for="(item, index) in levelList" :key="item.path">
        <!-- 使用计算好的标题，避免重复调用getMenuTitle -->
        <span v-if="item.redirect === 'noRedirect' || index === levelList.length - 1" class="no-redirect">
          {{ menuTitles[index] }}
        </span>
        <a v-else @click.prevent="handleLink(item)">
          {{ menuTitles[index] }}
        </a>
      </el-breadcrumb-item>
    </transition-group>
  </el-breadcrumb>
</template>

<script setup lang="ts" name="Breadcrumb">
import { type RouteLocationMatched } from 'vue-router'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const permissionStore = usePermissionStore()

/**
 * 将name转换为英文标题
 * @param name 路由name属性
 * @returns 转换后的英文标题
 */
const nameToTitle = (name: string) => {
  if (!name) return ''
  // 去掉末尾的数字，保留英文部分
  const cleanName = name.replace(/\d+$/, '')
  // 首字母大写
  return cleanName.charAt(0).toUpperCase() + cleanName.slice(1)
}

/**
 * 获取国际化菜单标题
 * @param item 路由项对象
 * @returns 国际化后的菜单标题
 */
const getMenuTitle = (item: any) => {
  const meta = item?.meta
  const name = item?.name

  if (!meta && !name) return ''

  // 优先使用国际化键
  if (meta?.i18nKey) {
    const i18nTitle = t(meta.i18nKey)
    if (i18nTitle !== meta.i18nKey) {
      return i18nTitle
    }
  }
  // 使用使用名称转换的标题或者原始标题
  return t(nameToTitle(name), meta.title)
}

/**
 * 计算当前路径中的斜杠数量，用于判断路由嵌套深度
 * @param str 路径字符串
 * @param char 分隔符，默认为斜杠
 * @returns 分隔符出现的次数
 */
const findPathNum = (str: string, char = '/') => {
  // 使用正则表达式计数，更简洁高效
  return typeof str === 'string' ? (str.match(new RegExp(char, 'g')) || []).length : 0
}

/**
 * 判断路由是否为仪表盘/首页路由
 * @param route 路由对象
 * @returns 是否为首页路由
 */
const isDashboard = (route?: RouteLocationMatched) => {
  return route?.name?.toString().trim() === 'Index'
}

/**
 * 迭代查找匹配的路由
 * @param pathList 路径段列表
 * @param routeList 路由列表
 * @param matched 已匹配的路由数组
 */
const getMatched = (pathList: string[], routeList: any[], matched: any[]) => {
  // 使用迭代代替递归，提高性能并避免潜在的栈溢出
  const currentList = [...pathList] // 创建副本以避免修改原数组
  let currentRoutes = routeList

  while (currentList.length > 0) {
    const currentPath = currentList[0]
    // 避免修改原始路由对象，正确处理name属性
    const data = currentRoutes.find((item) => item.path == currentPath || (item.name && item.name.toString().toLowerCase() == currentPath))

    if (!data) break

    matched.push(data)
    if (data.children && data.children.length > 0) {
      currentRoutes = data.children
      currentList.shift()
    } else {
      break
    }
  }
}

/**
 * 处理面包屑项点击，执行路由跳转
 * @param item 路由项
 */
const handleLink = (item: any) => {
  const target = item.redirect || item.path
  target && router.push(target)
}

/**
 * 计算面包屑数据，使用computed代替watchEffect和onMounted
 * 当路由变化时自动重新计算
 */
const breadcrumbData = computed(() => {
  if (route.path.startsWith('/redirect/')) return []

  let matched: any[] = []
  const pathNum = findPathNum(route.path)

  // 处理多级菜单路径
  if (pathNum > 2) {
    const pathList = route.path.match(/\/\w+/gi)?.map((item, index) => (index !== 0 ? item.slice(1) : item)) || []
    getMatched(pathList, permissionStore.defaultRoutes, matched)
  } else {
    // 使用常规路由匹配
    matched = route.matched.filter((item) => item.meta?.title)
  }

  // 判断是否为首页
  if (!isDashboard(matched[0])) {
    matched = [{ path: '/index', meta: { title: '首页', i18nKey: 'menu.index' } }].concat(matched)
  }

  return matched.filter((item) => item.meta && item.meta.title && item.meta.breadcrumb !== false)
})

// 将breadcrumbData赋值给levelList，保持原API兼容
const levelList = computed(() => breadcrumbData.value)

/**
 * 预计算所有面包屑标题，避免模板中重复调用getMenuTitle
 */
const menuTitles = computed(() => {
  return levelList.value.map((item) => getMenuTitle(item))
})
</script>

<style lang="scss" scoped>
.app-breadcrumb.el-breadcrumb {
  display: inline-block;
  font-size: 14px;
  margin-left: 8px;

  .no-redirect {
    color: #97a8be;
    cursor: text;
  }
}
</style>
