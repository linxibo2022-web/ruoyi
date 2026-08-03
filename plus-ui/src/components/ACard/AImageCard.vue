<!--
图片卡片组件 AImageCard

使用示例:

1. 基本用法
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="8">
    <AImageCard
      image-url="https://picsum.photos/400/300"
      title="精选文章标题"
      description="这是一篇关于前端开发的精彩文章,介绍了最新的技术趋势..."
    />
  </el-col>
</el-row>

2. 带分类和元信息
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="8">
    <AImageCard
      image-url="https://picsum.photos/400/300"
      title="Vue 3 深度解析"
      description="深入探讨Vue 3的响应式原理和组合式API的使用技巧"
      category="前端开发"
      category-type="success"
      author="张三"
      date="2024-03-20"
      read-time="5分钟"
    />
  </el-col>
</el-row>

3. 带操作按钮
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="8">
    <AImageCard
      image-url="https://picsum.photos/400/300"
      title="产品介绍"
      description="了解更多关于我们的创新产品和服务"
      show-actions
    />
  </el-col>
</el-row>

4. 水平布局
<el-row :gutter="20">
  <el-col :span="24">
    <AImageCard
      image-url="https://picsum.photos/400/300"
      title="最新资讯"
      description="实时更新的行业动态和技术新闻,帮助您掌握前沿信息"
      layout="horizontal"
      image-height="120px"
      category="新闻"
    />
  </el-col>
</el-row>

5. 多列响应式布局
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="8">
    <AImageCard
      image-url="https://picsum.photos/400/300"
      title="技术博客"
      description="分享技术心得和实战经验"
      category="技术"
      author="李四"
      date="2024-03-21"
    />
  </el-col>
  <el-col :xs="24" :sm="12" :md="8">
    <AImageCard
      image-url="https://picsum.photos/400/300"
      title="设计理念"
      description="探索优秀的设计思路和方法"
      category="设计"
      category-type="warning"
      author="王五"
      date="2024-03-22"
    />
  </el-col>
  <el-col :xs="24" :sm="12" :md="8">
    <AImageCard
      image-url="https://picsum.photos/400/300"
      title="项目实战"
      description="真实项目的开发经验总结"
      category="实战"
      category-type="danger"
      show-actions
    />
  </el-col>
</el-row>
-->
<template>
  <div class="image-card" :class="[`layout-${layout}`, { 'is-clickable': clickable }]" @click="handleClick">
    <!-- 图片区域 -->
    <div class="card-image" :style="imageContainerStyle">
      <el-image :src="imageUrl" :fit="imageFit" :lazy="lazyLoad" :style="imageStyle">
        <template #placeholder>
          <div class="image-placeholder">
            <Icon code="image" :size="32" />
          </div>
        </template>
        <template #error>
          <div class="image-error">
            <Icon code="image-error" :size="32" />
          </div>
        </template>
      </el-image>

      <!-- 图片上的标签 -->
      <div v-if="category" class="image-badge">
        <el-tag :type="categoryType" size="small">{{ category }}</el-tag>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="card-content">
      <!-- 标题 -->
      <h3 class="card-title" :class="{ 'ellipsis-2': titleClamp }">
        {{ title }}
      </h3>

      <!-- 描述 -->
      <p v-if="description" class="card-description" :class="{ 'ellipsis-3': descriptionClamp }">
        {{ description }}
      </p>

      <!-- 元信息 -->
      <div v-if="author || date || readTime" class="card-meta">
        <span v-if="author" class="meta-item">
          <Icon code="user" :size="14" />
          {{ author }}
        </span>
        <span v-if="date" class="meta-item">
          <Icon code="calendar" :size="14" />
          {{ date }}
        </span>
        <span v-if="readTime" class="meta-item">
          <Icon code="time" :size="14" />
          {{ readTime }}
        </span>
      </div>

      <!-- 操作按钮 -->
      <div v-if="showActions || $slots.actions" class="card-actions">
        <slot name="actions">
          <el-button size="small" type="primary" @click.stop="handleAction('view')"> {{ t('card.image.view') }} </el-button>
        </slot>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts" name="AImageCard">
const { t } = useI18n()

/**图片卡片属性接口*/
interface AImageCardProps {
  /**图片地址*/
  imageUrl: string
  /**标题*/
  title: string
  /**描述*/
  description?: string
  /**分类*/
  category?: string
  /**分类标签类型*/
  categoryType?: 'success' | 'warning' | 'danger' | 'info' | ''
  /**作者*/
  author?: string
  /**日期*/
  date?: string
  /**阅读时间*/
  readTime?: string
  /**布局方式*/
  layout?: 'vertical' | 'horizontal'
  /**图片高度*/
  imageHeight?: string
  /**图片填充方式*/
  imageFit?: 'fill' | 'contain' | 'cover' | 'none' | 'scale-down'
  /**是否懒加载*/
  lazyLoad?: boolean
  /**标题是否截断*/
  titleClamp?: boolean
  /**描述是否截断*/
  descriptionClamp?: boolean
  /**是否显示操作按钮*/
  showActions?: boolean
  /**是否可点击*/
  clickable?: boolean
}

/**组件属性定义*/
const props = withDefaults(defineProps<AImageCardProps>(), {
  description: '',
  category: '',
  categoryType: '',
  author: '',
  date: '',
  readTime: '',
  layout: 'vertical',
  imageHeight: '200px',
  imageFit: 'cover',
  lazyLoad: true,
  titleClamp: true,
  descriptionClamp: true,
  showActions: false,
  clickable: true
})

/**组件事件定义*/
const emit = defineEmits<{
  /**卡片点击*/
  click: []
  /**操作按钮点击*/
  action: [actionType: string]
}>()

/**图片容器样式*/
const imageContainerStyle = computed(() => {
  if (props.layout === 'horizontal') {
    return {
      width: props.imageHeight,
      height: props.imageHeight
    }
  }
  return {
    height: props.imageHeight
  }
})

/**图片样式*/
const imageStyle = computed(() => ({
  width: '100%',
  height: '100%'
}))

/**处理卡片点击*/
const handleClick = () => {
  if (props.clickable) {
    emit('click')
  }
}

/**处理操作按钮点击*/
const handleAction = (actionType: string) => {
  emit('action', actionType)
}

/**暴露方法*/
defineExpose({
  /**标题*/
  title: computed(() => props.title)
})
</script>

<style lang="scss" scoped>
.image-card {
  display: flex;
  overflow: hidden;
  background-color: var(--bg-level-1);
  border-radius: var(--radius-md);
  border: 1px solid var(--el-border-color);
  transition: all 0.3s ease;

  &.is-clickable {
    cursor: pointer;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);

      .card-title {
        color: var(--el-color-primary);
      }
    }
  }

  &.layout-vertical {
    flex-direction: column;
  }

  &.layout-horizontal {
    flex-direction: row;

    .card-content {
      padding: 16px;
    }
  }

  .card-image {
    position: relative;
    flex-shrink: 0;
    overflow: hidden;
    background-color: var(--el-fill-color-light);

    :deep(.el-image) {
      display: block;
    }

    .image-placeholder,
    .image-error {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 100%;
      height: 100%;
      color: var(--el-text-color-placeholder);
      background-color: var(--el-fill-color-light);
    }

    .image-badge {
      position: absolute;
      top: 12px;
      left: 12px;
      z-index: 1;
    }
  }

  .card-content {
    display: flex;
    flex: 1;
    flex-direction: column;
    padding: 20px;
  }

  .card-title {
    margin: 0 0 8px;
    font-size: 16px;
    font-weight: 500;
    color: var(--el-text-color-primary);
    line-height: 1.5;
    transition: color 0.2s ease;

    &.ellipsis-2 {
      display: -webkit-box;
      overflow: hidden;
      -webkit-box-orient: vertical;
      -webkit-line-clamp: 2;
    }
  }

  .card-description {
    margin: 0 0 12px;
    font-size: 14px;
    color: var(--el-text-color-regular);
    line-height: 1.6;

    &.ellipsis-3 {
      display: -webkit-box;
      overflow: hidden;
      -webkit-box-orient: vertical;
      -webkit-line-clamp: 3;
    }
  }

  .card-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
    margin-top: auto;
    padding-top: 12px;
    border-top: 1px solid var(--el-border-color-lighter);

    .meta-item {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 13px;
      color: var(--el-text-color-secondary);
    }
  }

  .card-actions {
    display: flex;
    gap: 8px;
    margin-top: 16px;
    padding-top: 12px;
    border-top: 1px solid var(--el-border-color-lighter);
  }
}
</style>
