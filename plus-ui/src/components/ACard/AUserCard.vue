<!--
用户卡片组件 AUserCard

使用示例:

1. 基本用法
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="6">
    <AUserCard
      avatar="https://i.pravatar.cc/150?img=1"
      name="张三"
      role="产品经理"
    />
  </el-col>
</el-row>

2. 带统计数据
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="6">
    <AUserCard
      avatar="https://i.pravatar.cc/150?img=2"
      name="李四"
      role="前端工程师"
      :stats="{ posts: 128, followers: 1234, following: 567 }"
    />
  </el-col>
</el-row>

3. 带描述和在线状态
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="6">
    <AUserCard
      avatar="https://i.pravatar.cc/150?img=3"
      name="王五"
      role="UI设计师"
      description="专注于用户体验设计,热爱创造美好的产品"
      :stats="{ posts: 89, followers: 2340 }"
      :show-status="true"
      status="online"
    />
  </el-col>
</el-row>

4. 完整功能展示
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="6">
    <AUserCard
      avatar="https://i.pravatar.cc/150?img=4"
      name="赵六"
      role="技术总监"
      description="10年+技术经验,带领团队创造价值"
      location="北京"
      email="zhaoliu@example.com"
      :stats="{ posts: 256, followers: 5678, following: 234 }"
      :tags="['Vue', 'React', 'Node.js']"
      :show-status="true"
      status="online"
      show-actions
    />
  </el-col>
</el-row>

5. 多用户卡片布局
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="6">
    <AUserCard
      avatar="https://i.pravatar.cc/150?img=5"
      name="孙七"
      role="后端工程师"
      :stats="{ posts: 156, followers: 890 }"
      :tags="['Java', 'Spring']"
    />
  </el-col>
  <el-col :xs="24" :sm="12" :md="6">
    <AUserCard
      avatar="https://i.pravatar.cc/150?img=6"
      name="周八"
      role="测试工程师"
      :stats="{ posts: 78, followers: 456 }"
      :show-status="true"
      status="busy"
    />
  </el-col>
  <el-col :xs="24" :sm="12" :md="6">
    <AUserCard
      avatar="https://i.pravatar.cc/150?img=7"
      name="吴九"
      role="运维工程师"
      :stats="{ posts: 123, followers: 678 }"
      show-actions
    />
  </el-col>
</el-row>
-->
<template>
  <div class="user-card">
    <!-- 用户头像 -->
    <div class="card-avatar">
      <el-avatar :size="avatarSize" :src="avatar">
        <Icon code="user" :size="avatarSize / 2" />
      </el-avatar>
      <div v-if="showStatus" class="avatar-status" :class="`status-${status}`"></div>
    </div>

    <!-- 用户信息 -->
    <div class="card-info">
      <!-- 姓名和角色 -->
      <h3 class="user-name">{{ name }}</h3>
      <p v-if="role" class="user-role">{{ role }}</p>

      <!-- 描述 -->
      <p v-if="description" class="user-description">{{ description }}</p>

      <!-- 扩展信息 -->
      <div v-if="location || email" class="user-meta">
        <span v-if="location" class="meta-item">
          <Icon code="location" :size="14" />
          {{ location }}
        </span>
        <span v-if="email" class="meta-item">
          <Icon code="email" :size="14" />
          {{ email }}
        </span>
      </div>

      <!-- 标签 -->
      <div v-if="tags && tags.length > 0" class="user-tags">
        <el-tag v-for="(tag, index) in tags" :key="index" size="small" type="info">
          {{ tag }}
        </el-tag>
      </div>

      <!-- 统计数据 -->
      <div v-if="stats" class="user-stats">
        <div v-if="stats.posts !== undefined" class="stat-item">
          <span class="stat-value">{{ formatNumber(stats.posts) }}</span>
          <span class="stat-label">{{ computedStatsLabels.posts }}</span>
        </div>
        <div v-if="stats.followers !== undefined" class="stat-item">
          <span class="stat-value">{{ formatNumber(stats.followers) }}</span>
          <span class="stat-label">{{ computedStatsLabels.followers }}</span>
        </div>
        <div v-if="stats.following !== undefined" class="stat-item">
          <span class="stat-value">{{ formatNumber(stats.following) }}</span>
          <span class="stat-label">{{ computedStatsLabels.following }}</span>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div v-if="showActions || $slots.actions" class="user-actions">
        <slot name="actions">
          <el-button type="primary" size="small" @click="handleFollow">
            {{ computedFollowText }}
          </el-button>
          <el-button size="small" @click="handleMessage"> {{ t('card.user.message') }} </el-button>
        </slot>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts" name="AUserCard">
const { t } = useI18n()

/**用户统计数据接口*/
interface UserStats {
  /**文章数*/
  posts?: number
  /**粉丝数*/
  followers?: number
  /**关注数*/
  following?: number
}

/**统计标签接口*/
interface StatsLabels {
  /**文章标签*/
  posts: string
  /**粉丝标签*/
  followers: string
  /**关注标签*/
  following: string
}

/**用户卡片属性接口*/
interface AUserCardProps {
  /**头像地址*/
  avatar: string
  /**用户名*/
  name: string
  /**角色/职位*/
  role?: string
  /**描述*/
  description?: string
  /**位置*/
  location?: string
  /**邮箱*/
  email?: string
  /**标签*/
  tags?: string[]
  /**统计数据*/
  stats?: UserStats
  /**统计标签*/
  statsLabels?: StatsLabels
  /**头像大小*/
  avatarSize?: number
  /**是否显示状态*/
  showStatus?: boolean
  /**在线状态*/
  status?: 'online' | 'offline' | 'busy'
  /**是否显示操作按钮*/
  showActions?: boolean
  /**关注按钮文本*/
  followText?: string
}

/**组件属性定义*/
const props = withDefaults(defineProps<AUserCardProps>(), {
  role: '',
  description: '',
  location: '',
  email: '',
  tags: () => [],
  statsLabels: () => ({
    posts: '文章',
    followers: '粉丝',
    following: '关注'
  }),
  avatarSize: 80,
  showStatus: false,
  status: 'offline',
  showActions: false,
  followText: '关注'
})

/**组件事件定义*/
const emit = defineEmits<{
  /**关注*/
  follow: []
  /**发消息*/
  message: []
}>()

/**计算统计标签*/
const computedStatsLabels = computed(() => ({
  posts: props.statsLabels?.posts || t('card.user.posts'),
  followers: props.statsLabels?.followers || t('card.user.followers'),
  following: props.statsLabels?.following || t('card.user.following')
}))

/**计算关注按钮文本*/
const computedFollowText = computed(() => props.followText || t('card.user.follow'))

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

/**处理关注*/
const handleFollow = () => {
  emit('follow')
}

/**处理发消息*/
const handleMessage = () => {
  emit('message')
}

/**暴露方法*/
defineExpose({
  /**用户名*/
  userName: computed(() => props.name)
})
</script>

<style lang="scss" scoped>
.user-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px;
  background-color: var(--bg-level-1);
  border-radius: var(--radius-md);
  border: 1px solid var(--el-border-color);

  .card-avatar {
    position: relative;
    margin-bottom: 16px;

    .avatar-status {
      position: absolute;
      right: 2px;
      bottom: 2px;
      width: 16px;
      height: 16px;
      border: 2px solid var(--bg-level-1);
      border-radius: 50%;

      &.status-online {
        background-color: var(--el-color-success);
      }

      &.status-offline {
        background-color: var(--el-text-color-disabled);
      }

      &.status-busy {
        background-color: var(--el-color-warning);
      }
    }
  }

  .card-info {
    width: 100%;
    text-align: center;
  }

  .user-name {
    margin: 0 0 4px;
    font-size: 18px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  .user-role {
    margin: 0 0 12px;
    font-size: 14px;
    color: var(--el-text-color-secondary);
  }

  .user-description {
    margin: 0 0 12px;
    font-size: 13px;
    color: var(--el-text-color-regular);
    line-height: 1.6;
  }

  .user-meta {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    gap: 16px;
    margin-bottom: 12px;

    .meta-item {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 13px;
      color: var(--el-text-color-secondary);
    }
  }

  .user-tags {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    gap: 8px;
    margin-bottom: 16px;
  }

  .user-stats {
    display: flex;
    justify-content: center;
    gap: 24px;
    padding: 16px 0;
    border-top: 1px solid var(--el-border-color-lighter);
    border-bottom: 1px solid var(--el-border-color-lighter);

    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 4px;
    }

    .stat-value {
      font-size: 18px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }

    .stat-label {
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }
  }

  .user-actions {
    display: flex;
    justify-content: center;
    gap: 12px;
    margin-top: 16px;
  }
}
</style>
