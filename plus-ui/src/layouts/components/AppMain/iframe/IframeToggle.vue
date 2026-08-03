<!-- 内嵌页面切换 -->
<template>
  <!--
    iframe 视图切换组件
    根据当前路由显示对应的 iframe 页面，支持多标签页切换
  -->
  <InnerLink
    v-for="(item, index) in layout.iframeViews.value"
    v-show="route.path === item.path"
    :key="item.path"
    :iframe-id="`iframe${index}`"
    :src="buildIframeUrl(item.meta?.link, item.query)"
  />
</template>

<script setup lang="ts" name="IframeToggle">
import InnerLink from './InnerLink.vue'
import { objectToQuery } from '@/utils/object'

/**
 * iframe 视图切换组件
 *
 * 功能说明：
 * - 管理多个 iframe 标签页的显示和隐藏
 * - 根据当前路由路径匹配对应的 iframe 视图
 * - 支持 URL 参数拼接，将路由查询参数传递给 iframe
 *
 * 使用场景：
 * - 内嵌第三方系统页面
 * - 多标签页管理外部链接
 * - 保持 iframe 状态，避免重复加载
 */

// 当前路由信息
const route = useRoute()
// 布局状态管理
const layout = useLayout()

/**
 * 构建 iframe 的完整 URL
 *
 * @param baseUrl - 基础 URL，来自路由 meta.link
 * @param queryParams - 路由查询参数对象
 * @returns 拼接查询参数后的完整 URL
 *
 * @example
 * ```typescript
 * buildIframeUrl('https://example.com', { id: 1, type: 'user' })
 * // 返回: 'https://example.com?id=1&type=user'
 *
 * buildIframeUrl('https://example.com', {})
 * // 返回: 'https://example.com'
 * ```
 */
const buildIframeUrl = (baseUrl: string | undefined, queryParams: Record<string, any>): string | undefined => {
  // 如果基础 URL 为空，直接返回
  if (!baseUrl) {
    return baseUrl
  }

  // 使用工具类方法生成查询字符串
  const queryString = objectToQuery(queryParams || {})

  if (!queryString) {
    return baseUrl
  }

  // 检查原 URL 是否已包含查询参数
  const separator = baseUrl.includes('?') ? '&' : '?'

  return `${baseUrl}${separator}${queryString}`
}
</script>
