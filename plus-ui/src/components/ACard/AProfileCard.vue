<!--
个人资料卡片组件 AProfileCard

使用示例:

1. 基本用法
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="8">
    <AProfileCard
      :profile="{
        name: '张三',
        avatar: 'https://i.pravatar.cc/150?img=1',
        title: '高级工程师',
        company: '某某科技公司'
      }"
    />
  </el-col>
</el-row>

2. 带联系方式
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="8">
    <AProfileCard
      :profile="{
        name: '李四',
        avatar: 'https://i.pravatar.cc/150?img=2',
        title: '高级前端工程师',
        company: '某某科技公司',
        department: '技术部',
        email: 'lisi@example.com',
        phone: '138****8888',
        location: '北京市朝阳区'
      }"
      show-contact
    />
  </el-col>
</el-row>

3. 带统计数据
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="8">
    <AProfileCard
      :profile="{
        name: '王五',
        avatar: 'https://i.pravatar.cc/150?img=3',
        title: 'UI设计师',
        company: '创意设计公司',
        bio: '热爱设计,专注用户体验'
      }"
      :stats="[
        { label: '项目', value: 28 },
        { label: '粉丝', value: 1234 },
        { label: '关注', value: 567 }
      ]"
    />
  </el-col>
</el-row>

4. 带社交链接
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="8">
    <AProfileCard
      :profile="{
        name: '赵六',
        avatar: 'https://i.pravatar.cc/150?img=4',
        title: '全栈工程师',
        bio: '10年+开发经验,热爱开源'
      }"
      :social-links="[
        { type: 'github', url: 'https://github.com' },
        { type: 'twitter', url: 'https://twitter.com' },
        { type: 'linkedin', url: 'https://linkedin.com' }
      ]"
      show-social
      show-actions
    />
  </el-col>
</el-row>

5. 完整功能展示
<el-row :gutter="20">
  <el-col :xs="24" :md="12" :lg="8">
    <AProfileCard
      :profile="{
        name: '孙七',
        avatar: 'https://i.pravatar.cc/150?img=5',
        title: '技术总监',
        company: '互联网公司',
        department: '研发中心',
        email: 'sunqi@example.com',
        phone: '186****6666',
        location: '上海市浦东新区',
        bio: '专注技术创新,带领团队创造价值'
      }"
      :stats="[
        { label: '项目', value: 156 },
        { label: '团队', value: 28 },
        { label: '粉丝', value: 5678 }
      ]"
      :social-links="[
        { type: 'github', url: 'https://github.com' },
        { type: 'wechat', url: '#' }
      ]"
      cover-image="https://picsum.photos/400/200"
      :show-status="true"
      status="online"
      show-contact
      show-social
      show-actions
    />
  </el-col>
</el-row>
-->
<template>
  <div class="profile-card">
    <!-- 背景图 -->
    <div v-if="coverImage" class="card-cover" :style="{ backgroundImage: `url(${coverImage})` }">
      <div class="cover-overlay"></div>
    </div>

    <!-- 个人信息 -->
    <div class="card-body">
      <!-- 头像 -->
      <div class="profile-avatar" :class="{ 'has-cover': coverImage }">
        <el-avatar :size="avatarSize" :src="profile.avatar">
          <Icon code="user" :size="avatarSize / 2" />
        </el-avatar>
        <div v-if="showStatus" class="avatar-status" :class="`status-${status}`"></div>
      </div>

      <!-- 基本信息 -->
      <div class="profile-info">
        <h3 class="profile-name">{{ profile.name }}</h3>
        <p v-if="profile.title" class="profile-title">{{ profile.title }}</p>

        <div v-if="profile.company || profile.department" class="profile-meta">
          <span v-if="profile.company" class="meta-item">
            <Icon code="building" :size="14" />
            {{ profile.company }}
          </span>
          <span v-if="profile.department" class="meta-item">
            <Icon code="team" :size="14" />
            {{ profile.department }}
          </span>
        </div>

        <!-- 个人简介 -->
        <p v-if="profile.bio" class="profile-bio">{{ profile.bio }}</p>

        <!-- 联系方式 -->
        <div v-if="showContact" class="profile-contact">
          <div v-if="profile.email" class="contact-item">
            <Icon code="email" :size="14" />
            <span>{{ profile.email }}</span>
          </div>
          <div v-if="profile.phone" class="contact-item">
            <Icon code="phone" :size="14" />
            <span>{{ profile.phone }}</span>
          </div>
          <div v-if="profile.location" class="contact-item">
            <Icon code="location" :size="14" />
            <span>{{ profile.location }}</span>
          </div>
        </div>

        <!-- 统计数据 -->
        <div v-if="stats && stats.length > 0" class="profile-stats">
          <div v-for="(stat, index) in stats" :key="index" class="stat-item">
            <span class="stat-value">{{ formatNumber(stat.value) }}</span>
            <span class="stat-label">{{ stat.label }}</span>
          </div>
        </div>

        <!-- 社交链接 -->
        <div v-if="showSocial && socialLinks && socialLinks.length > 0" class="profile-social">
          <a v-for="(link, index) in socialLinks" :key="index" :href="link.url" target="_blank" class="social-link" :title="link.type">
            <Icon :code="getSocialIcon(link.type)" :size="18" />
          </a>
        </div>

        <!-- 操作按钮 -->
        <div v-if="showActions || $slots.actions" class="profile-actions">
          <slot name="actions">
            <el-button type="primary" @click="handleEdit">
              <Icon code="edit" :size="14" />
              {{ t('card.profile.edit') }}
            </el-button>
            <el-button @click="handleFollow">
              <Icon code="like" :size="14" />
              {{ t('card.user.follow') }}
            </el-button>
          </slot>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts" name="AProfileCard">
const { t } = useI18n()

/**统计项接口*/
interface StatItem {
  /**标签*/
  label: string
  /**数值*/
  value: number
}

/**社交链接接口*/
interface SocialLink {
  /**类型*/
  type: string
  /**链接*/
  url: string
}

/**个人资料接口*/
interface ProfileData {
  /**姓名*/
  name: string
  /**头像*/
  avatar?: string
  /**职位*/
  title?: string
  /**公司*/
  company?: string
  /**部门*/
  department?: string
  /**邮箱*/
  email?: string
  /**电话*/
  phone?: string
  /**位置*/
  location?: string
  /**个人简介*/
  bio?: string
}

/**个人资料卡片属性接口*/
interface AProfileCardProps {
  /**个人资料*/
  profile: ProfileData
  /**统计数据*/
  stats?: StatItem[]
  /**社交链接*/
  socialLinks?: SocialLink[]
  /**封面图*/
  coverImage?: string
  /**头像大小*/
  avatarSize?: number
  /**是否显示状态*/
  showStatus?: boolean
  /**在线状态*/
  status?: 'online' | 'offline' | 'busy'
  /**是否显示联系方式*/
  showContact?: boolean
  /**是否显示社交链接*/
  showSocial?: boolean
  /**是否显示操作按钮*/
  showActions?: boolean
}

/**组件属性定义*/
const props = withDefaults(defineProps<AProfileCardProps>(), {
  stats: () => [],
  socialLinks: () => [],
  coverImage: '',
  avatarSize: 100,
  showStatus: false,
  status: 'offline',
  showContact: false,
  showSocial: false,
  showActions: false
})

/**组件事件定义*/
const emit = defineEmits<{
  /**编辑资料*/
  edit: []
  /**关注*/
  follow: []
}>()

/**社交图标映射*/
const socialIconMap: Record<string, IconCode> = {
  'github': 'github',
  'twitter': 'twitter',
  'linkedin': 'linkedin',
  'facebook': 'facebook',
  'instagram': 'instagram',
  'wechat': 'wechat',
  'weibo': 'weibo',
  'qq': 'qq'
}

/**获取社交图标*/
const getSocialIcon = (type: string): IconCode => {
  return socialIconMap[type.toLowerCase()] || 'link'
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

/**处理编辑*/
const handleEdit = () => {
  emit('edit')
}

/**处理关注*/
const handleFollow = () => {
  emit('follow')
}

/**暴露方法*/
defineExpose({
  /**用户名*/
  userName: computed(() => props.profile.name)
})
</script>

<style lang="scss" scoped>
.profile-card {
  position: relative;
  background-color: var(--bg-level-1);
  border-radius: var(--radius-md);
  border: 1px solid var(--el-border-color);
  overflow: hidden;

  .card-cover {
    height: 120px;
    background-size: cover;
    background-position: center;
    position: relative;

    .cover-overlay {
      position: absolute;
      inset: 0;
      background: linear-gradient(180deg, transparent 0%, rgba(0, 0, 0, 0.3) 100%);
    }
  }

  .card-body {
    padding: 24px;
  }

  .profile-avatar {
    display: flex;
    justify-content: center;
    margin-bottom: 16px;
    position: relative;

    &.has-cover {
      margin-top: -50px;
      margin-bottom: 20px;
    }

    :deep(.el-avatar) {
      border: 4px solid var(--bg-level-1);
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    }

    .avatar-status {
      position: absolute;
      right: calc(50% - 50px);
      bottom: 8px;
      width: 16px;
      height: 16px;
      border: 3px solid var(--bg-level-1);
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

  .profile-info {
    text-align: center;
  }

  .profile-name {
    margin: 0 0 4px;
    font-size: 22px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  .profile-title {
    margin: 0 0 12px;
    font-size: 15px;
    color: var(--el-text-color-regular);
  }

  .profile-meta {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    gap: 16px;
    margin-bottom: 16px;

    .meta-item {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 13px;
      color: var(--el-text-color-secondary);
    }
  }

  .profile-bio {
    margin: 0 0 20px;
    padding: 0 16px;
    font-size: 14px;
    color: var(--el-text-color-regular);
    line-height: 1.6;
  }

  .profile-contact {
    display: flex;
    flex-direction: column;
    gap: 8px;
    margin-bottom: 20px;
    padding: 16px;
    background-color: var(--el-fill-color-light);
    border-radius: var(--radius-md);

    .contact-item {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 13px;
      color: var(--el-text-color-regular);
    }
  }

  .profile-stats {
    display: flex;
    justify-content: center;
    gap: 32px;
    padding: 20px 0;
    border-top: 1px solid var(--el-border-color-lighter);
    border-bottom: 1px solid var(--el-border-color-lighter);
    margin-bottom: 20px;

    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 4px;
    }

    .stat-value {
      font-size: 20px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }

    .stat-label {
      font-size: 13px;
      color: var(--el-text-color-secondary);
    }
  }

  .profile-social {
    display: flex;
    justify-content: center;
    gap: 12px;
    margin-bottom: 20px;

    .social-link {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 36px;
      height: 36px;
      color: var(--el-text-color-regular);
      background-color: var(--el-fill-color-light);
      border-radius: 50%;
      transition: all 0.2s ease;

      &:hover {
        color: var(--el-color-primary);
        background-color: var(--el-color-primary-light-9);
        transform: translateY(-2px);
      }
    }
  }

  .profile-actions {
    display: flex;
    gap: 12px;
    justify-content: center;

    :deep(.el-button) {
      flex: 1;
      max-width: 160px;
    }
  }
}
</style>
