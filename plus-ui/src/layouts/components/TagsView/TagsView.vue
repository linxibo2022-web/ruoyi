<!-- 主页签组件 -->
<!--
  标签页视图组件
  提供多标签页的管理和展示功能，支持标签页的增删改查、右键菜单操作等
  主要功能：
  1. 标签页的显示和切换
  2. 标签页的关闭操作（支持关闭当前、其他、左侧、右侧、全部）
  3. 右键上下文菜单
  4. 标签页自动滚动定位
  5. 深色模式支持
  6. 国际化支持
-->
<template>
  <div id="tags-view-container" class="tags-view-container" ref="containerRef">
    <!-- 标签页滚动容器 -->
    <ScrollPane ref="scrollPaneRef" class="tags-view-wrapper" @scroll="handleScroll">
      <!-- 遍历所有访问过的视图标签 -->
      <router-link
        v-for="tag in visitedViews"
        :key="tag.path"
        :data-path="tag.path"
        :class="['tags-view-item', { 'active': isActive(tag) }]"
        :to="{ path: tag.path ? tag.path : '', query: tag.query }"
        @click.middle="!isAffix(tag) ? closeSelectedTag(tag) : ''"
        @contextmenu.prevent="openMenu(tag, $event)"
      >
        <!-- 标签图标 -->
        <div class="tag-icon">
          <Icon :code="tag.meta?.icon || 'nested'" size="sm" />
        </div>

        <!-- 标签文本内容 -->
        <div class="tag-text">
          {{ getTagTitle(tag) }}
        </div>

        <!-- 关闭按钮 - 仅非固定标签显示 -->
        <span v-if="!isAffix(tag)" class="close-btn" @click.prevent.stop="closeSelectedTag(tag)">
          <svg class="close-icon" viewBox="0 0 24 24" fill="currentColor">
            <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z" />
          </svg>
        </span>
      </router-link>
    </ScrollPane>

    <!-- 右键上下文菜单 -->
    <ul v-show="visible" :style="{ left: left + 'px', top: top + 'px' }" class="contextmenu">
      <!-- 刷新当前标签页 -->
      <li @click="refreshSelectedTag(selectedTag)" class="menu-item">
        <div class="menu-icon">
          <Icon code="refresh" size="sm" />
        </div>
        <span>{{ t('tagsView.refresh') }}</span>
      </li>

      <!-- 关闭当前标签页 - 仅非固定标签显示 -->
      <li v-if="!isAffix(selectedTag)" @click="closeSelectedTag(selectedTag)" class="menu-item">
        <div class="menu-icon">
          <Icon code="close" size="sm" />
        </div>
        <span>{{ t('tagsView.closeCurrent') }}</span>
      </li>

      <!-- 关闭其他标签页 -->
      <li @click="closeOthersTags" class="menu-item">
        <div class="menu-icon">
          <Icon code="table-remove" size="sm" />
        </div>
        <span>{{ t('tagsView.closeOthers') }}</span>
      </li>

      <!-- 关闭左侧标签页 - 非第一个标签时显示 -->
      <li v-if="!isFirstView()" @click="closeLeftTags" class="menu-item">
        <div class="menu-icon">
          <Icon code="left" size="sm" />
        </div>
        <span>{{ t('tagsView.closeLeft') }}</span>
      </li>

      <!-- 关闭右侧标签页 - 非最后一个标签时显示 -->
      <li v-if="!isLastView()" @click="closeRightTags" class="menu-item">
        <div class="menu-icon">
          <Icon code="right2" size="sm" />
        </div>
        <span>{{ t('tagsView.closeRight') }}</span>
      </li>

      <!-- 关闭所有标签页 -->
      <li @click="closeAllTags(selectedTag)" class="menu-item">
        <div class="menu-icon">
          <Icon code="clear" size="sm" />
        </div>
        <span>{{ t('tagsView.closeAll') }}</span>
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts" name="TagsView">
import ScrollPane from './ScrollPane.vue'
import { useRoute, useRouter, type RouteRecordRaw, type RouteLocationNormalized } from 'vue-router'
import { normalizePath } from '@/utils/string'
import { closeAllPage, closeLeftPage, closeOtherPage, closePage, closeRightPage, refreshPage } from '@/utils/tab'

// ==================== 基础工具初始化 ====================
const { t } = useI18n()
const route = useRoute()
const router = useRouter()

// 获取布局状态管理
const layout = useLayout()

// ==================== DOM 引用 ====================
const scrollPaneRef = ref<InstanceType<typeof ScrollPane>>()
const containerRef = ref<HTMLElement | null>(null)

// ==================== 右键菜单状态 ====================
const visible = ref(false) // 右键菜单显示状态
const top = ref(0) // 右键菜单垂直位置
const left = ref(0) // 右键菜单水平位置
const selectedTag = ref<RouteLocationNormalized>() // 当前选中的标签

// ==================== 标签数据状态 ====================
const affixTags = ref<RouteLocationNormalized[]>([]) // 固定标签列表

// ==================== 计算属性 ====================
/** 获取访问过的视图列表 */
const visitedViews = layout.visitedViews as any

/** 获取路由配置 */
const routes = computed(() => usePermissionStore().getRoutes())

// ==================== 标签状态判断方法 ====================
/**
 * 检查指定标签是否为当前激活状态
 */
const isActive = (r: RouteLocationNormalized): boolean => {
  return r.path === route.path
}

/**
 * 检查标签是否为固定标签（不可关闭）
 */
const isAffix = (tag: RouteLocationNormalized) => {
  return tag?.meta && tag?.meta?.affix
}

/**
 * 检查当前选中标签是否为第一个可操作的视图
 */
const isFirstView = () => {
  try {
    return selectedTag.value?.fullPath === '/index' || selectedTag.value?.fullPath === visitedViews.value[1].fullPath
  } catch (err) {
    return false
  }
}

/**
 * 检查当前选中标签是否为最后一个视图
 */
const isLastView = () => {
  try {
    return selectedTag.value?.fullPath === visitedViews.value[visitedViews.value.length - 1].fullPath
  } catch (err) {
    return false
  }
}

// ==================== 标签显示相关方法 ====================
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
 * 获取标签页标题
 * 支持国际化处理和动态参数，添加兜底方案
 */
const getTagTitle = (tag: RouteLocationNormalized) => {
  const meta = tag?.meta
  const name = tag?.name

  // 优先使用国际化键进行翻译
  if (meta?.i18nKey) {
    const i18nTitle = t(meta.i18nKey)
    // 如果翻译成功（返回值不等于键名），使用翻译结果
    if (i18nTitle !== meta.i18nKey) {
      return i18nTitle
    }
  }

  // 处理包含动态参数的标题模板
  if (meta?.title?.includes('{')) {
    try {
      return t(meta.title, meta.titleParams || {})
    } catch {
      // 解析失败时返回原始标题
      return meta.title
    }
  }

  // 降级处理：使用使用名称转换的标题或者原始标题
  return t(nameToTitle(name.toString()), meta?.title)
}

// ==================== 固定标签处理方法 ====================
/**
 * 递归筛选出所有固定标签
 */
const filterAffixTags = (routes: RouteRecordRaw[], basePath = '') => {
  let tags: RouteLocationNormalized[] = []

  routes.forEach((route) => {
    // 检查当前路由是否为固定标签
    if (route.meta && route.meta.affix) {
      const tagPath = normalizePath(basePath + '/' + route.path)
      tags.push({
        hash: '',
        matched: [],
        params: undefined,
        query: undefined,
        redirectedFrom: undefined,
        fullPath: tagPath,
        path: tagPath,
        name: route.name as string,
        meta: { ...route.meta }
      })
    }

    // 递归处理子路由
    if (route.children) {
      const tempTags = filterAffixTags(route.children, route.path)
      if (tempTags.length >= 1) {
        tags = [...tags, ...tempTags]
      }
    }
  })

  return tags
}

/**
 * 初始化固定标签
 * 在组件加载时执行，将所有固定标签添加到标签栏
 */
const initTags = () => {
  const res = filterAffixTags(routes.value)
  affixTags.value = res

  // 将固定标签添加到访问视图列表
  for (const tag of res) {
    if (tag.name) {
      layout.addVisitedView(tag)
    }
  }
}

// ==================== 标签操作核心方法 ====================
/**
 * 添加当前路由为新标签
 * 根据路由信息创建新的标签页
 */
const addTags = () => {
  const { name } = route

  // 如果查询参数中包含标题，更新路由元信息
  if (route.query.title) {
    route.meta.title = route.query.title as string
  }

  // 有效路由名称时添加到视图存储
  if (name) {
    layout.addView(route as any)
  }
}

/**
 * 滚动到当前激活的标签位置
 * 确保当前标签在可视区域内
 */
const moveToCurrentTag = async () => {
  await nextTick()
  for (const r of visitedViews.value) {
    if (r.path === route.path) {
      // 滚动到目标标签
      scrollPaneRef.value?.moveToTarget(r)
      // 如果完整路径不同则更新视图信息
      if (r.fullPath !== route.fullPath) {
        layout.updateVisitedView(route)
      }
    }
  }
}

/**
 * 跳转到最后一个可用的视图
 */
const toLastView = (visitedViews: RouteLocationNormalized[], view?: RouteLocationNormalized) => {
  const latestView = visitedViews.slice(-1)[0]

  if (latestView) {
    // 跳转到最后一个标签
    router.push(latestView.fullPath as string)
  } else {
    // 没有可用标签时的默认处理
    if (view?.name === 'Dashboard') {
      // 重载首页
      router.replace({ path: '/redirect' + view?.fullPath })
    } else {
      // 跳转到根路径
      router.push('/')
    }
  }
}

// ==================== 单个标签操作方法 ====================
/**
 * 刷新指定标签页
 */
const refreshSelectedTag = (view: RouteLocationNormalized) => {
  refreshPage(view)
  // 如果是外部链接，清除iframe视图缓存
  if (route.meta.link) {
    layout.delIframeView(route)
  }
}

/**
 * 关闭指定标签页
 */
const closeSelectedTag = (view: RouteLocationNormalized) => {
  closePage(view).then(({ visitedViews }: any) => {
    // 如果关闭的是当前激活标签，跳转到最后一个标签
    if (isActive(view)) {
      toLastView(visitedViews, view)
    }
  })
}

// ==================== 批量标签操作方法 ====================
/**
 * 关闭选中标签右侧的所有标签
 */
const closeRightTags = () => {
  closeRightPage(selectedTag.value).then((visitedViews: RouteLocationNormalized[]) => {
    // 如果当前路由被关闭，跳转到最后一个可用标签
    if (!visitedViews.find((i: RouteLocationNormalized) => i.fullPath === route.fullPath)) {
      toLastView(visitedViews)
    }
  })
}

/**
 * 关闭选中标签左侧的所有标签
 */
const closeLeftTags = () => {
  closeLeftPage(selectedTag.value).then((visitedViews: RouteLocationNormalized[]) => {
    // 如果当前路由被关闭，跳转到最后一个可用标签
    if (!visitedViews.find((i: RouteLocationNormalized) => i.fullPath === route.fullPath)) {
      toLastView(visitedViews)
    }
  })
}

/**
 * 关闭除选中标签外的其他所有标签
 */
const closeOthersTags = () => {
  // 先跳转到选中的标签
  if (selectedTag.value) {
    router
      .push({
        path: selectedTag.value.path,
        query: selectedTag.value.query
      })
      .catch(() => {})
  }

  // 执行关闭其他标签操作
  closeOtherPage(selectedTag.value).then(() => {
    moveToCurrentTag()
  })
}

/**
 * 关闭所有标签（保留固定标签）
 */
const closeAllTags = (view: RouteLocationNormalized) => {
  closeAllPage().then(({ visitedViews }) => {
    // 如果当前路由是固定标签，则不进行跳转
    if (affixTags.value.some((tag) => tag.path === route.path)) {
      return
    }
    toLastView(visitedViews, view)
  })
}

// ==================== 右键菜单相关方法 ====================
/**
 * 打开右键上下文菜单
 */
const openMenu = (tag: RouteLocationNormalized, e: MouseEvent) => {
  const menuMinWidth = 105 // 菜单最小宽度
  const container = containerRef.value
  if (!container) return

  // 计算菜单位置
  const offsetLeft = container.getBoundingClientRect().left
  const offsetWidth = container.offsetWidth
  const maxLeft = offsetWidth - menuMinWidth // 左边界限制
  const l = e.clientX - offsetLeft + 15 // 15px 右边距

  // 防止菜单超出容器边界
  if (l > maxLeft) {
    left.value = maxLeft
  } else {
    left.value = l
  }

  // 设置菜单位置和状态
  top.value = e.clientY
  visible.value = true
  selectedTag.value = tag
}

/**
 * 关闭右键上下文菜单
 */
const closeMenu = () => {
  visible.value = false
}

/**
 * 处理滚动事件
 * 滚动时自动关闭右键菜单
 */
const handleScroll = () => {
  closeMenu()
}

// ==================== 监听器设置 ====================
// 监听路由变化，自动添加和定位到当前标签
watch(route, () => {
  addTags()
  moveToCurrentTag()
})

// 监听右键菜单显示状态，动态绑定/解绑全局点击事件
watch(visible, (value) => {
  if (value) {
    document.body.addEventListener('click', closeMenu)
  } else {
    document.body.removeEventListener('click', closeMenu)
  }
})

// ==================== 生命周期钩子 ====================
// 组件挂载时的初始化操作
onMounted(() => {
  initTags() // 初始化固定标签
  addTags() // 添加当前路由标签
})
</script>

<style lang="scss" scoped>
/* ==================== 页签视图容器 ==================== */
.tags-view-container {
  height: 40px;
  width: 100%;
  background-color: var(--header-bg);
  transition: all var(--duration-normal) ease;
  padding: 0 16px; /* 与 AppMain 的 p-4 (16px) 保持一致 */

  /* ==================== 标签包装器 ==================== */
  .tags-view-wrapper {
    .tags-view-item {
      vertical-align: middle; /* 居中对齐 */
      display: inline-flex;
      align-items: center;
      position: relative;
      cursor: pointer;
      height: 32px; /* 更高的标签 */
      line-height: 32px;
      background-color: var(--el-fill-color-blank); /* 默认半透明填充背景 */
      color: var(--el-text-color-regular);
      padding: 0 12px; /* 更大的内边距 */
      font-size: 13px; /* 稍大的字体 */
      margin-left: 6px;
      margin-top: 4px;
      border-radius: 6px; /* art-design-pro 特色：较大的圆角 */
      /* 添加边框，与侧边栏边框保持一致 */
      border: 1px solid var(--el-border-color);
      transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1); /* 更流畅的过渡 */
      overflow: hidden;
      font-weight: 450; /* 稍微加粗 */

      /* ==================== 激活状态 ==================== */
      &.active {
        color: var(--el-color-primary);
        transform: translateY(-1px); /* 轻微上浮 */
        font-weight: 500;

        /* 激活状态下的图标和关闭按钮也使用主题色 */
        .tag-icon,
        .close-btn {
          color: var(--el-color-primary);
          opacity: 1;
        }

        /* 激活状态下的关闭按钮悬停效果 */
        .close-btn:hover {
          background: var(--el-fill-color); /* 淡色背景圈 */
        }
      }

      /* ==================== 标签图标 ==================== */
      .tag-icon {
        display: flex;
        align-items: center;
        margin-right: 6px;
        flex-shrink: 0;
        opacity: 0.8;
        transition: opacity 0.2s ease;

        .icon-svg {
          width: 14px;
          height: 14px;
        }
      }

      /* ==================== 标签文本 ==================== */
      .tag-text {
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        max-width: 140px; /* 更宽的文本区域 */
      }

      /* ==================== 关闭按钮 ==================== */
      .close-btn {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        margin-left: 8px;
        width: 18px;
        height: 18px;
        border-radius: 50%;
        background: transparent; /* 默认无背景 */
        opacity: 0.6; /* 图标始终可见 */
        transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
        flex-shrink: 0;
        color: currentColor;

        .close-icon {
          width: 12px;
          height: 12px;
        }
      }

      /* ==================== 悬停效果 ==================== */
      &:hover {
        /* 背景不变化，只改变文字和图标颜色 */
        color: var(--el-color-primary);
        transform: translateY(-1px);

        .tag-icon {
          opacity: 1;
          color: var(--el-color-primary);
        }

        .close-btn {
          opacity: 0.8; /* 悬停标签时图标稍微明显一点 */
        }

        /* 激活状态悬停时保持原样 */
        &.active {
          /* 激活状态悬停时背景也不变化 */
          color: var(--el-color-primary);

          .tag-icon {
            color: var(--el-color-primary);
          }

          .close-btn {
            opacity: 1;
          }
        }
      }

      /* 关闭按钮悬停 */
      .close-btn:hover {
        opacity: 1 !important;
        background: var(--el-fill-color); /* 淡色背景圈 */
        transform: scale(1.1); /* 轻微放大 */
      }

      /* ==================== 边距优化 ==================== */
      &:first-of-type {
        margin-left: 0; /* 第一个标签无左边距 */
      }

      &:last-of-type {
        margin-right: 12px;
      }
    }
  }

  /* ==================== 右键上下文菜单 ==================== */
  .contextmenu {
    margin: 0;
    background: var(--el-bg-color-overlay);
    z-index: 1000;
    position: absolute;
    list-style-type: none;
    padding: 8px 6px; /* art-design-pro 风格的内边距 */
    border-radius: 8px; /* 更大的圆角 */
    font-size: 13px;
    font-weight: 400;
    border: 1px solid var(--el-border-color-light);
    box-shadow:
      0 6px 16px 0 rgba(0, 0, 0, 0.08),
      0 3px 6px -4px rgba(0, 0, 0, 0.12),
      0 9px 28px 8px rgba(0, 0, 0, 0.05); /* art-design-pro 特色阴影 */
    backdrop-filter: blur(12px); /* 毛玻璃效果 */
    min-width: 140px;

    /* ==================== 菜单项 ==================== */
    .menu-item {
      margin: 2px 0;
      padding: 0 12px; /* 移除上下内边距，使用固定高度 */
      height: 32px; /* 与页签高度保持一致 */
      cursor: pointer;
      display: flex;
      align-items: center;
      transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
      color: var(--el-text-color-regular);
      border-radius: 6px;
      background: transparent;

      /* ==================== 菜单图标 ==================== */
      .menu-icon {
        margin-right: 10px;
        display: flex;
        align-items: center;
        flex-shrink: 0;
        color: currentColor;
        opacity: 0.75;
        transition: opacity 0.2s ease;
      }

      /* ==================== 悬停效果 ==================== */
      &:hover {
        background: var(--el-fill-color-light);
        color: var(--el-color-primary);
        transform: translateX(2px);

        .menu-icon {
          opacity: 1;
          color: var(--el-color-primary);
        }
      }

      /* ==================== 激活效果 ==================== */
      &:active {
        transform: scale(0.98);
      }
    }
  }
}

/* ==================== 深色模式优化 ==================== */
@media (prefers-color-scheme: dark) {
  .tags-view-container {
    .tags-view-wrapper {
      .tags-view-item {
        &.active {
          box-shadow:
            0 2px 8px rgba(64, 158, 255, 0.3),
            0 1px 3px rgba(64, 158, 255, 0.2);
        }
      }
    }

    .contextmenu {
      background: var(--el-bg-color-overlay);
      box-shadow:
        0 6px 16px 0 rgba(0, 0, 0, 0.3),
        0 3px 6px -4px rgba(0, 0, 0, 0.4),
        0 9px 28px 8px rgba(0, 0, 0, 0.2);
    }
  }
}

/* ==================== 移动端响应式 ==================== */
@media (max-width: 768px) {
  .tags-view-container {
    height: 36px;
    padding: 0 16px; /* 移动端也保持 16px */

    .tags-view-wrapper {
      .tags-view-item {
        height: 28px;
        line-height: 28px;
        padding: 0 10px;
        font-size: 12px;
        margin-left: 4px;

        .tag-text {
          max-width: 80px;
        }

        .close-btn {
          width: 16px;
          height: 16px;

          .close-icon {
            width: 10px;
            height: 10px;
          }
        }
      }
    }

    .contextmenu {
      min-width: 120px;
      padding: 6px 4px;

      .menu-item {
        padding: 0 10px; /* 移除上下内边距 */
        height: 28px; /* 与移动端页签高度保持一致 */
        font-size: 12px;
      }
    }
  }
}

/* ==================== 平板响应式 ==================== */
@media (min-width: 769px) and (max-width: 1024px) {
  .tags-view-container {
    .tags-view-wrapper {
      .tags-view-item {
        .tag-text {
          max-width: 100px;
        }
      }
    }
  }
}
</style>
