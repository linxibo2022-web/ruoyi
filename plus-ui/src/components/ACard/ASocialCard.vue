<!--
社交卡片组件 ASocialCard

使用示例:

1. 基本用法
<el-row :gutter="20">
  <el-col :span="24">
    <ASocialCard
      :content="{
        user: '张三',
        avatar: 'https://i.pravatar.cc/100?img=1',
        text: '今天天气真好,适合出去走走!',
        time: '5分钟前'
      }"
    />
  </el-col>
</el-row>

2. 带图片发布
<el-row :gutter="20">
  <el-col :span="24">
    <ASocialCard
      :content="{
        user: '李四',
        avatar: 'https://i.pravatar.cc/100?img=2',
        text: '分享几张美图',
        images: ['https://picsum.photos/400/300?random=1'],
        time: '1小时前'
      }"
    />
  </el-col>
</el-row>

3. 带位置和互动数据
<el-row :gutter="20">
  <el-col :span="24">
    <ASocialCard
      :content="{
        user: '王五',
        avatar: 'https://i.pravatar.cc/100?img=3',
        text: '今天在这里拍到的美景',
        images: ['https://picsum.photos/400/300?random=2', 'https://picsum.photos/400/300?random=3'],
        time: '2小时前',
        location: '北京'
      }"
      :stats="{ likes: 128, comments: 45, shares: 12 }"
      show-actions
    />
  </el-col>
</el-row>

4. 转发动态
<el-row :gutter="20">
  <el-col :span="24">
    <ASocialCard
      :content="{
        user: '赵六',
        avatar: 'https://i.pravatar.cc/100?img=4',
        text: '转发一条精彩的内容',
        time: '3小时前'
      }"
      :forward-content="{
        user: '原作者',
        text: '这是原始内容,包含了很多有价值的信息',
        images: ['https://picsum.photos/400/300?random=4'],
        time: '1天前'
      }"
      :stats="{ likes: 256, comments: 89 }"
      show-actions
    />
  </el-col>
</el-row>

5. 完整功能展示
<el-row :gutter="20">
  <el-col :span="24">
    <ASocialCard
      :content="{
        user: '孙七',
        avatar: 'https://i.pravatar.cc/100?img=5',
        text: '分享我的最新作品,欢迎大家提意见',
        images: [
          'https://picsum.photos/400/300?random=5',
          'https://picsum.photos/400/300?random=6',
          'https://picsum.photos/400/300?random=7'
        ],
        time: '刚刚',
        location: '上海'
      }"
      :stats="{ likes: 520, comments: 168, shares: 88 }"
      show-menu
      show-actions
    />
  </el-col>
</el-row>
-->
<template>
  <div class="social-card">
    <!-- 用户信息 -->
    <div class="card-header">
      <div class="user-info">
        <el-avatar :size="48" :src="content.avatar">
          <Icon code="user" :size="24" />
        </el-avatar>
        <div class="user-detail">
          <h4 class="user-name">{{ content.user }}</h4>
          <div class="post-meta">
            <span class="post-time">{{ content.time }}</span>
            <span v-if="content.location" class="post-location">
              <Icon code="location" :size="12" />
              {{ content.location }}
            </span>
          </div>
        </div>
      </div>
      <div v-if="showMenu" class="card-menu">
        <el-dropdown @command="handleCommand">
          <el-button text>
            <Icon code="more" :size="16" />
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="edit">{{ t('card.social.edit') }}</el-dropdown-item>
              <el-dropdown-item command="delete">{{ t('card.social.delete') }}</el-dropdown-item>
              <el-dropdown-item command="report">{{ t('card.social.report') }}</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 内容 -->
    <div class="card-body">
      <!-- 文本内容 -->
      <div v-if="content.text" class="post-text">
        {{ content.text }}
      </div>

      <!-- 图片 -->
      <div v-if="content.images && content.images.length > 0" class="post-images" :class="`images-${getImageGridClass()}`">
        <div v-for="(image, index) in content.images.slice(0, maxImages)" :key="index" class="image-item" @click="handleImageClick(image, index)">
          <el-image :src="image" fit="cover" lazy>
            <template #error>
              <div class="image-error">
                <Icon code="image-error" :size="32" />
              </div>
            </template>
          </el-image>
          <div v-if="index === maxImages - 1 && content.images.length > maxImages" class="image-more">+{{ content.images.length - maxImages }}</div>
        </div>
      </div>

      <!-- 转发内容 -->
      <div v-if="forwardContent" class="forward-content">
        <div class="forward-header">
          <Icon code="forward" :size="14" />
          <span class="forward-user">@{{ forwardContent.user }}</span>
        </div>
        <div class="forward-text">{{ forwardContent.text }}</div>
        <div v-if="forwardContent.images && forwardContent.images.length > 0" class="forward-images">
          <el-image v-for="(image, index) in forwardContent.images.slice(0, 3)" :key="index" :src="image" fit="cover" class="forward-image" />
        </div>
      </div>

      <!-- 链接预览 -->
      <div v-if="linkPreview" class="link-preview" @click="handleLinkClick">
        <div v-if="linkPreview.image" class="preview-image">
          <el-image :src="linkPreview.image" fit="cover" />
        </div>
        <div class="preview-content">
          <h5 class="preview-title">{{ linkPreview.title }}</h5>
          <p class="preview-description">{{ linkPreview.description }}</p>
          <span class="preview-url">{{ linkPreview.url }}</span>
        </div>
      </div>
    </div>

    <!-- 统计和操作 -->
    <div v-if="stats || showActions" class="card-footer">
      <!-- 统计数据 -->
      <div v-if="stats" class="post-stats">
        <span v-if="stats.likes" class="stat-item">
          <Icon code="heart" :size="14" />
          {{ formatNumber(stats.likes) }}
        </span>
        <span v-if="stats.comments" class="stat-item">
          <Icon code="comment" :size="14" />
          {{ formatNumber(stats.comments) }}
        </span>
        <span v-if="stats.shares" class="stat-item">
          <Icon code="share" :size="14" />
          {{ formatNumber(stats.shares) }}
        </span>
      </div>

      <!-- 操作按钮 -->
      <div v-if="showActions" class="post-actions">
        <el-button text :class="{ 'is-liked': isLiked }" @click="handleLike">
          <Icon :code="isLiked ? 'heart-fill' : 'heart'" :size="16" />
          {{ t('card.social.like') }}
        </el-button>
        <el-button text @click="handleComment">
          <Icon code="comment" :size="16" />
          {{ t('card.social.comment') }}
        </el-button>
        <el-button text @click="handleShare">
          <Icon code="share" :size="16" />
          {{ t('card.social.share') }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts" name="ASocialCard">
const { t } = useI18n()

/**社交内容接口*/
interface SocialContent {
  /**用户名*/
  user: string
  /**头像*/
  avatar?: string
  /**文本内容*/
  text?: string
  /**图片列表*/
  images?: string[]
  /**时间*/
  time: string
  /**位置*/
  location?: string
}

/**统计数据接口*/
interface SocialStats {
  /**点赞数*/
  likes?: number
  /**评论数*/
  comments?: number
  /**分享数*/
  shares?: number
}

/**链接预览接口*/
interface LinkPreview {
  /**标题*/
  title: string
  /**描述*/
  description?: string
  /**图片*/
  image?: string
  /**链接*/
  url: string
}

/**社交卡片属性接口*/
interface ASocialCardProps {
  /**内容*/
  content: SocialContent
  /**转发内容*/
  forwardContent?: SocialContent
  /**统计数据*/
  stats?: SocialStats
  /**链接预览*/
  linkPreview?: LinkPreview
  /**是否显示菜单*/
  showMenu?: boolean
  /**是否显示操作按钮*/
  showActions?: boolean
  /**是否已点赞*/
  liked?: boolean
  /**最大图片数量*/
  maxImages?: number
}

/**组件属性定义*/
const props = withDefaults(defineProps<ASocialCardProps>(), {
  showMenu: false,
  showActions: false,
  liked: false,
  maxImages: 9
})

/**组件事件定义*/
const emit = defineEmits<{
  /**点赞*/
  like: []
  /**评论*/
  comment: []
  /**分享*/
  share: []
  /**图片点击*/
  imageClick: [image: string, index: number]
  /**链接点击*/
  linkClick: []
  /**菜单命令*/
  command: [command: string]
}>()

/**是否已点赞*/
const isLiked = ref(props.liked)

/**获取图片网格类名*/
const getImageGridClass = (): string => {
  const count = Math.min(props.content.images?.length || 0, props.maxImages)
  if (count === 1) return 'one'
  if (count === 2) return 'two'
  if (count === 3) return 'three'
  if (count === 4) return 'four'
  return 'grid'
}

/**格式化数字*/
const formatNumber = (num: number): string => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num.toString()
}

/**处理点赞*/
const handleLike = () => {
  isLiked.value = !isLiked.value
  emit('like')
}

/**处理评论*/
const handleComment = () => {
  emit('comment')
}

/**处理分享*/
const handleShare = () => {
  emit('share')
}

/**处理图片点击*/
const handleImageClick = (image: string, index: number) => {
  emit('imageClick', image, index)
}

/**处理链接点击*/
const handleLinkClick = () => {
  emit('linkClick')
}

/**处理菜单命令*/
const handleCommand = (command: string) => {
  emit('command', command)
}

/**监听 liked 属性变化*/
watch(
  () => props.liked,
  (newValue) => {
    isLiked.value = newValue
  }
)

/**暴露方法*/
defineExpose({
  /**是否已点赞*/
  isLiked: computed(() => isLiked.value)
})
</script>

<style lang="scss" scoped>
.social-card {
  background-color: var(--bg-level-1);
  border-radius: var(--radius-md);
  border: 1px solid var(--el-border-color);

  .card-header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    padding: 16px 20px;

    .user-info {
      display: flex;
      gap: 12px;
      flex: 1;
    }

    .user-detail {
      flex: 1;
      min-width: 0;
    }

    .user-name {
      margin: 0 0 4px;
      font-size: 15px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }

    .post-meta {
      display: flex;
      align-items: center;
      gap: 12px;
      font-size: 13px;
      color: var(--el-text-color-secondary);

      .post-location {
        display: flex;
        align-items: center;
        gap: 2px;
      }
    }

    .card-menu {
      margin-left: 12px;
    }
  }

  .card-body {
    padding: 0 20px 16px;

    .post-text {
      margin-bottom: 12px;
      font-size: 15px;
      color: var(--el-text-color-primary);
      line-height: 1.6;
      word-wrap: break-word;
    }

    .post-images {
      display: grid;
      gap: 4px;
      margin-bottom: 12px;

      &.images-one {
        grid-template-columns: 1fr;
        max-height: 400px;
      }

      &.images-two {
        grid-template-columns: repeat(2, 1fr);
        max-height: 250px;
      }

      &.images-three {
        grid-template-columns: repeat(3, 1fr);
        max-height: 200px;
      }

      &.images-four {
        grid-template-columns: repeat(2, 1fr);
        max-height: 300px;
      }

      &.images-grid {
        grid-template-columns: repeat(3, 1fr);
        max-height: 300px;
      }

      .image-item {
        position: relative;
        overflow: hidden;
        border-radius: var(--radius-md);
        cursor: pointer;
        aspect-ratio: 1;

        :deep(.el-image) {
          width: 100%;
          height: 100%;
        }

        .image-error {
          display: flex;
          align-items: center;
          justify-content: center;
          width: 100%;
          height: 100%;
          background-color: var(--el-fill-color-light);
          color: var(--el-text-color-placeholder);
        }

        .image-more {
          position: absolute;
          inset: 0;
          display: flex;
          align-items: center;
          justify-content: center;
          background-color: rgba(0, 0, 0, 0.6);
          color: #fff;
          font-size: 24px;
          font-weight: 600;
        }

        &:hover {
          opacity: 0.9;
        }
      }
    }

    .forward-content {
      padding: 12px;
      background-color: var(--el-fill-color-light);
      border-radius: var(--radius-md);
      border-left: 3px solid var(--el-color-primary);

      .forward-header {
        display: flex;
        align-items: center;
        gap: 6px;
        margin-bottom: 8px;
        color: var(--el-text-color-secondary);
        font-size: 13px;

        .forward-user {
          font-weight: 500;
          color: var(--el-color-primary);
        }
      }

      .forward-text {
        margin-bottom: 8px;
        font-size: 14px;
        color: var(--el-text-color-regular);
        line-height: 1.5;
      }

      .forward-images {
        display: flex;
        gap: 4px;

        .forward-image {
          width: 80px;
          height: 80px;
          border-radius: var(--radius-sm);
        }
      }
    }

    .link-preview {
      display: flex;
      overflow: hidden;
      border: 1px solid var(--el-border-color);
      border-radius: var(--radius-md);
      cursor: pointer;
      transition: all 0.2s ease;

      &:hover {
        border-color: var(--el-color-primary);
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      }

      .preview-image {
        flex-shrink: 0;
        width: 120px;
        height: 120px;

        :deep(.el-image) {
          width: 100%;
          height: 100%;
        }
      }

      .preview-content {
        flex: 1;
        padding: 12px;
        min-width: 0;
      }

      .preview-title {
        margin: 0 0 6px;
        font-size: 14px;
        font-weight: 500;
        color: var(--el-text-color-primary);
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .preview-description {
        margin: 0 0 6px;
        font-size: 13px;
        color: var(--el-text-color-regular);
        line-height: 1.5;
        display: -webkit-box;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 2;
        overflow: hidden;
      }

      .preview-url {
        font-size: 12px;
        color: var(--el-text-color-placeholder);
      }
    }
  }

  .card-footer {
    padding: 12px 20px;
    border-top: 1px solid var(--el-border-color-lighter);

    .post-stats {
      display: flex;
      gap: 20px;
      padding-bottom: 12px;
      font-size: 13px;
      color: var(--el-text-color-secondary);

      .stat-item {
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }

    .post-actions {
      display: flex;
      justify-content: space-around;
      gap: 8px;

      :deep(.el-button) {
        flex: 1;
        color: var(--el-text-color-regular);

        &:hover {
          color: var(--el-color-primary);
          background-color: var(--el-color-primary-light-9);
        }

        &.is-liked {
          color: var(--el-color-danger);

          &:hover {
            color: var(--el-color-danger);
            background-color: var(--el-color-danger-light-9);
          }
        }
      }
    }
  }
}
</style>
