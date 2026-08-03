<!--
活动卡片组件 AActivityCard

使用示例:

1. 基本用法
<el-row :gutter="20">
  <el-col :span="24">
    <AActivityCard
      title="最近活动"
      :activities="[
        { id: 1, user: '张三', action: '创建了', target: '新项目', time: '5分钟前' },
        { id: 2, user: '李四', action: '更新了', target: '文档', time: '10分钟前' }
      ]"
    />
  </el-col>
</el-row>

2. 带用户头像
<el-row :gutter="20">
  <el-col :span="24">
    <AActivityCard
      title="团队动态"
      :activities="[
        { id: 1, user: '张三', avatar: 'https://i.pravatar.cc/100?img=1', action: '评论了', target: '你的文章', content: '写得不错!', time: '10分钟前' },
        { id: 2, user: '李四', avatar: 'https://i.pravatar.cc/100?img=2', action: '分享了', target: '一篇文章', time: '1小时前' }
      ]"
      show-avatar
    />
  </el-col>
</el-row>

3. 带活动类型图标
<el-row :gutter="20">
  <el-col :xs="24" :md="16">
    <AActivityCard
      title="系统日志"
      :activities="[
        { id: 1, user: '系统', action: '执行了', target: '数据备份', type: 'system', time: '1小时前' },
        { id: 2, user: '李四', action: '上传了', target: '10个文件', type: 'upload', time: '2小时前' },
        { id: 3, user: '王五', action: '删除了', target: '旧数据', type: 'delete', time: '3小时前' }
      ]"
      show-type-icon
    />
  </el-col>
</el-row>

4. 带查看更多按钮
<el-row :gutter="20">
  <el-col :span="24">
    <AActivityCard
      title="项目动态"
      subtitle="最近7天"
      :activities="[
        { id: 1, user: '张三', action: '创建了', target: '任务 #123', type: 'create', time: '刚刚' },
        { id: 2, user: '李四', action: '完成了', target: '需求评审', type: 'update', time: '5分钟前' },
        { id: 3, user: '王五', action: '上传了', target: '设计稿', type: 'upload', time: '30分钟前' }
      ]"
      :max-count="3"
      :show-more-button="true"
      show-type-icon
    />
  </el-col>
</el-row>

5. 完整功能展示
<el-row :gutter="20">
  <el-col :span="24">
    <AActivityCard
      title="全部活动"
      :activities="[
        { id: 1, user: '张三', avatar: 'https://i.pravatar.cc/100?img=1', action: '创建了', target: '新项目', content: '启动了新的产品开发项目', type: 'create', time: '5分钟前', attachments: ['需求文档.pdf'] },
        { id: 2, user: '李四', avatar: 'https://i.pravatar.cc/100?img=2', action: '更新了', target: '代码库', type: 'update', time: '1小时前' },
        { id: 3, user: '王五', avatar: 'https://i.pravatar.cc/100?img=3', action: '下载了', target: '数据报表', type: 'download', time: '2小时前' }
      ]"
      :max-count="5"
      show-avatar
      :show-more-button="true"
      item-clickable
    />
  </el-col>
</el-row>
-->
<template>
  <div class="activity-card">
    <!-- 卡片头部 -->
    <div class="card-header">
      <div class="header-content">
        <h3 class="card-title">{{ title }}</h3>
        <p v-if="subtitle" class="card-subtitle">{{ subtitle }}</p>
      </div>
      <div v-if="showHeaderAction || $slots['header-action']" class="header-action">
        <slot name="header-action">
          <el-button text @click="handleHeaderAction">
            <Icon code="more" :size="16" />
          </el-button>
        </slot>
      </div>
    </div>

    <!-- 活动列表 -->
    <div v-if="displayActivities.length > 0" class="card-body">
      <div class="activity-timeline">
        <div
          v-for="(activity, index) in displayActivities"
          :key="activity.id"
          class="activity-item"
          :class="{ 'is-clickable': itemClickable, 'is-last': index === displayActivities.length - 1 }"
          @click="handleItemClick(activity)"
        >
          <!-- 时间轴节点 -->
          <div class="activity-node">
            <div v-if="showTypeIcon" class="node-icon" :class="`type-${activity.type || 'default'}`">
              <Icon :code="getTypeIcon(activity.type)" :size="14" />
            </div>
            <div v-else-if="showAvatar && activity.avatar" class="node-avatar">
              <el-avatar :size="32" :src="activity.avatar">
                <Icon code="user" :size="16" />
              </el-avatar>
            </div>
            <div v-else class="node-dot"></div>
          </div>

          <!-- 时间轴线 -->
          <div v-if="index !== displayActivities.length - 1" class="activity-line"></div>

          <!-- 活动内容 -->
          <div class="activity-content">
            <div class="content-header">
              <span class="activity-text">
                <strong class="user-name">{{ activity.user }}</strong>
                <span class="action-text">{{ activity.action }}</span>
                <strong v-if="activity.target" class="target-text">{{ activity.target }}</strong>
              </span>
              <span class="activity-time">{{ activity.time }}</span>
            </div>

            <!-- 活动详情 -->
            <p v-if="activity.content" class="activity-description">
              {{ activity.content }}
            </p>

            <!-- 附加信息 -->
            <div v-if="activity.extra || $slots.extra" class="activity-extra">
              <slot name="extra" :activity="activity">
                <span v-if="activity.extra">{{ activity.extra }}</span>
              </slot>
            </div>

            <!-- 附件或图片 -->
            <div v-if="activity.attachments && activity.attachments.length > 0" class="activity-attachments">
              <div v-for="(attachment, idx) in activity.attachments" :key="idx" class="attachment-item">
                <Icon code="file" :size="14" />
                <span>{{ attachment }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 查看更多 -->
      <div v-if="showMoreButton && hasMore" class="card-footer">
        <el-button text @click="handleMore">
          {{ t('card.activity.viewMore') }}
          <Icon code="right" :size="14" />
        </el-button>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="card-empty">
      <Icon code="history" :size="48" />
      <span class="empty-text">{{ t('card.activity.noData') }}</span>
    </div>
  </div>
</template>

<script setup lang="ts" name="AActivityCard">
const { t } = useI18n()

/**活动项接口*/
interface ActivityItem {
  /**唯一标识*/
  id: number | string
  /**用户名*/
  user: string
  /**用户头像*/
  avatar?: string
  /**动作*/
  action: string
  /**目标对象*/
  target?: string
  /**详细内容*/
  content?: string
  /**时间*/
  time: string
  /**活动类型*/
  type?: 'default' | 'create' | 'update' | 'delete' | 'upload' | 'download' | 'system'
  /**附加信息*/
  extra?: string
  /**附件列表*/
  attachments?: string[]
  /**自定义数据*/
  [key: string]: any
}

/**活动卡片属性接口*/
interface AActivityCardProps {
  /**标题*/
  title: string
  /**副标题*/
  subtitle?: string
  /**活动列表*/
  activities: ActivityItem[]
  /**最大显示数量*/
  maxCount?: number
  /**是否显示更多按钮*/
  showMoreButton?: boolean
  /**是否显示用户头像*/
  showAvatar?: boolean
  /**是否显示类型图标*/
  showTypeIcon?: boolean
  /**是否显示头部操作*/
  showHeaderAction?: boolean
  /**列表项是否可点击*/
  itemClickable?: boolean
  /**是否按日期分组*/
  groupByDate?: boolean
}

/**组件属性定义*/
const props = withDefaults(defineProps<AActivityCardProps>(), {
  subtitle: '',
  maxCount: 0,
  showMoreButton: false,
  showAvatar: false,
  showTypeIcon: false,
  showHeaderAction: false,
  itemClickable: false,
  groupByDate: false
})

/**组件事件定义*/
const emit = defineEmits<{
  /**查看更多*/
  more: []
  /**活动项点击*/
  itemClick: [activity: ActivityItem]
  /**头部操作点击*/
  headerAction: []
}>()

/**显示的活动列表*/
const displayActivities = computed(() => {
  if (props.maxCount > 0 && props.activities.length > props.maxCount) {
    return props.activities.slice(0, props.maxCount)
  }
  return props.activities
})

/**是否有更多*/
const hasMore = computed(() => {
  return props.maxCount > 0 && props.activities.length > props.maxCount
})

/**类型图标映射*/
const typeIconMap: Record<string, IconCode> = {
  'default': 'circle',
  'create': 'add',
  'update': 'edit',
  'delete': 'delete',
  'upload': 'upload',
  'download': 'download',
  'system': 'setting'
}

/**获取类型图标*/
const getTypeIcon = (type?: string): IconCode => {
  return typeIconMap[type || 'default'] || 'circle'
}

/**处理查看更多*/
const handleMore = () => {
  emit('more')
}

/**处理活动项点击*/
const handleItemClick = (activity: ActivityItem) => {
  if (props.itemClickable) {
    emit('itemClick', activity)
  }
}

/**处理头部操作*/
const handleHeaderAction = () => {
  emit('headerAction')
}

/**暴露方法*/
defineExpose({
  /**活动数量*/
  totalCount: computed(() => props.activities.length),
  /**显示数量*/
  displayCount: computed(() => displayActivities.value.length)
})
</script>

<style lang="scss" scoped>
.activity-card {
  background-color: var(--bg-level-1);
  border-radius: var(--radius-md);
  border: 1px solid var(--el-border-color);

  .card-header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    padding: 20px;
    border-bottom: 1px solid var(--el-border-color);

    .header-content {
      flex: 1;
    }

    .card-title {
      margin: 0 0 4px;
      font-size: 16px;
      font-weight: 500;
      color: var(--el-text-color-primary);
    }

    .card-subtitle {
      margin: 0;
      font-size: 13px;
      color: var(--el-text-color-secondary);
    }

    .header-action {
      margin-left: 16px;

      :deep(.el-button) {
        color: var(--el-text-color-regular);

        &:hover {
          color: var(--el-color-primary);
        }
      }
    }
  }

  .card-body {
    padding: 20px;
  }

  .activity-timeline {
    position: relative;
  }

  .activity-item {
    position: relative;
    display: flex;
    gap: 16px;
    padding-bottom: 20px;
    transition: background-color 0.2s ease;

    &.is-last {
      padding-bottom: 0;
    }

    &.is-clickable {
      cursor: pointer;

      &:hover .activity-content {
        background-color: var(--el-fill-color-light);
      }
    }

    .activity-node {
      position: relative;
      z-index: 2;
      display: flex;
      flex-shrink: 0;
      align-items: center;
      justify-content: center;
      margin-top: 4px;
      width: 32px;
      .node-dot {
        width: 10px;
        height: 10px;
        background-color: var(--el-color-primary);
        border: 2px solid var(--bg-level-1);
        border-radius: 50%;
        box-shadow: 0 0 0 2px var(--el-color-primary-light-9);
      }

      .node-icon {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 24px;
        height: 24px;
        border-radius: 50%;
        color: #fff;

        &.type-default {
          background-color: var(--el-color-primary);
        }

        &.type-create {
          background-color: var(--el-color-success);
        }

        &.type-update {
          background-color: var(--el-color-warning);
        }

        &.type-delete {
          background-color: var(--el-color-danger);
        }

        &.type-upload,
        &.type-download {
          background-color: var(--el-color-info);
        }

        &.type-system {
          background-color: #909399;
        }
      }

      .node-avatar {
        :deep(.el-avatar) {
          border: 2px solid var(--bg-level-1);
          box-shadow: 0 0 0 2px var(--el-border-color);
        }
      }
    }

    .activity-line {
      position: absolute;
      top: 36px;
      left: 16px;
      transform: translateX(-50%);
      width: 1px;
      height: calc(100% - 34px);
      background-color: var(--el-border-color);
    }

    .activity-content {
      flex: 1;
      min-width: 0;
      padding: 6px 12px;
      border-radius: var(--radius-md);
      transition: background-color 0.2s ease;

      .content-header {
        display: flex;
        align-items: flex-start;
        justify-content: space-between;
        gap: 12px;
        margin-bottom: 4px;
      }

      .activity-text {
        font-size: 14px;
        line-height: 1.5;

        .user-name {
          color: var(--el-text-color-primary);
          font-weight: 500;
        }

        .action-text {
          margin: 0 4px;
          color: var(--el-text-color-regular);
        }

        .target-text {
          color: var(--el-color-primary);
          font-weight: 500;
        }
      }

      .activity-time {
        flex-shrink: 0;
        font-size: 12px;
        color: var(--el-text-color-placeholder);
      }

      .activity-description {
        margin: 6px 0 0;
        font-size: 13px;
        color: var(--el-text-color-regular);
        line-height: 1.6;
      }

      .activity-extra {
        margin-top: 6px;
        font-size: 12px;
        color: var(--el-text-color-secondary);
      }

      .activity-attachments {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        margin-top: 8px;

        .attachment-item {
          display: flex;
          align-items: center;
          gap: 4px;
          padding: 4px 8px;
          font-size: 12px;
          color: var(--el-text-color-regular);
          background-color: var(--el-fill-color-light);
          border-radius: 4px;
        }
      }
    }
  }

  .card-footer {
    display: flex;
    justify-content: center;
    padding: 12px 20px 20px;
    border-top: 1px solid var(--el-border-color);
    margin-top: 8px;

    :deep(.el-button) {
      color: var(--el-color-primary);
    }
  }

  .card-empty {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 60px 20px;
    color: var(--el-text-color-placeholder);

    .empty-text {
      margin-top: 12px;
      font-size: 14px;
    }
  }
}
</style>
