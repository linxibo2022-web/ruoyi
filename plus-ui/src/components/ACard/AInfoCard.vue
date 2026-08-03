<!--
信息卡片组件 AInfoCard

使用示例:

1. 基本用法
<el-row :gutter="20">
  <el-col :span="24">
    <AInfoCard title="系统通知" type="info">
      这是一条重要的系统通知消息,请及时查看处理
    </AInfoCard>
  </el-col>
</el-row>

2. 不同类型提示
<el-row :gutter="20">
  <el-col :xs="24" :sm="12">
    <AInfoCard title="成功提示" type="success">
      操作已成功完成!数据已保存
    </AInfoCard>
  </el-col>
  <el-col :xs="24" :sm="12">
    <AInfoCard title="警告信息" type="warning">
      请注意检查数据的完整性和准确性
    </AInfoCard>
  </el-col>
</el-row>

3. 带可关闭功能
<el-row :gutter="20">
  <el-col :span="24">
    <AInfoCard
      title="温馨提示"
      type="info"
      icon="info-circle"
      closable
    >
      <p>这是提示内容的第一段,包含重要信息...</p>
      <p>这是提示内容的第二段,请仔细阅读...</p>
    </AInfoCard>
  </el-col>
</el-row>

4. 带自定义操作按钮
<el-row :gutter="20">
  <el-col :span="24">
    <AInfoCard
      title="更新通知"
      type="warning"
      icon="bell"
    >
      <p>发现新版本 v2.0.0,包含以下更新:</p>
      <p>• 修复若干已知问题</p>
      <p>• 优化系统性能</p>
      <template #actions>
        <el-button size="small" type="primary">立即更新</el-button>
        <el-button size="small">稍后提醒</el-button>
      </template>
    </AInfoCard>
  </el-col>
</el-row>

5. 简洁模式
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="6">
    <AInfoCard type="success" simple>操作成功!</AInfoCard>
  </el-col>
  <el-col :xs="24" :sm="12" :md="6">
    <AInfoCard type="error" simple>操作失败!</AInfoCard>
  </el-col>
</el-row>
-->
<template>
  <div v-if="visible" class="info-card" :class="[`type-${type}`, { 'is-simple': simple }]">
    <!-- 图标 -->
    <div v-if="showIcon" class="card-icon">
      <Icon :code="iconCode" :size="iconSize" />
    </div>

    <!-- 内容区域 -->
    <div class="card-content">
      <!-- 标题 -->
      <h3 v-if="title" class="card-title">{{ title }}</h3>

      <!-- 主体内容 -->
      <div class="card-body">
        <slot></slot>
      </div>

      <!-- 操作按钮 -->
      <div v-if="$slots.actions" class="card-actions">
        <slot name="actions"></slot>
      </div>
    </div>

    <!-- 关闭按钮 -->
    <div v-if="closable" class="card-close">
      <el-button text @click="handleClose">
        <Icon code="close" :size="16" />
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts" name="AInfoCard">
/**信息卡片类型*/
type InfoCardType = 'info' | 'success' | 'warning' | 'error'

/**信息卡片属性接口*/
interface AInfoCardProps {
  /**标题*/
  title?: string
  /**类型*/
  type?: InfoCardType
  /**自定义图标*/
  icon?: IconCode
  /**图标大小*/
  iconSize?: number
  /**是否显示图标*/
  showIcon?: boolean
  /**是否可关闭*/
  closable?: boolean
  /**简洁模式*/
  simple?: boolean
}

/**组件属性定义*/
const props = withDefaults(defineProps<AInfoCardProps>(), {
  title: '',
  type: 'info',
  iconSize: 20,
  showIcon: true,
  closable: false,
  simple: false
})

/**组件事件定义*/
const emit = defineEmits<{
  /**关闭事件*/
  close: []
}>()

/**是否可见*/
const visible = ref(true)

/**图标代码映射*/
const iconMap: Record<InfoCardType, IconCode> = {
  'info': 'info-circle',
  'success': 'check-circle',
  'warning': 'warning-circle',
  'error': 'close-circle'
}

/**计算图标代码*/
const iconCode = computed(() => {
  return props.icon || iconMap[props.type]
})

/**处理关闭*/
const handleClose = () => {
  visible.value = false
  emit('close')
}

/**暴露方法*/
defineExpose({
  /**显示*/
  show: () => {
    visible.value = true
  },
  /**隐藏*/
  hide: () => {
    visible.value = false
  }
})
</script>

<style lang="scss" scoped>
.info-card {
  display: flex;
  gap: 12px;
  padding: 16px;
  background-color: var(--bg-level-1);
  border-radius: var(--radius-md);
  border: 1px solid;

  &.type-info {
    border-color: var(--el-color-info-light-7);
    background-color: var(--el-color-info-light-9);

    .card-icon {
      color: var(--el-color-info);
    }

    .card-title {
      color: var(--el-color-info);
    }
  }

  &.type-success {
    border-color: var(--el-color-success-light-7);
    background-color: var(--el-color-success-light-9);

    .card-icon {
      color: var(--el-color-success);
    }

    .card-title {
      color: var(--el-color-success);
    }
  }

  &.type-warning {
    border-color: var(--el-color-warning-light-7);
    background-color: var(--el-color-warning-light-9);

    .card-icon {
      color: var(--el-color-warning);
    }

    .card-title {
      color: var(--el-color-warning);
    }
  }

  &.type-error {
    border-color: var(--el-color-danger-light-7);
    background-color: var(--el-color-danger-light-9);

    .card-icon {
      color: var(--el-color-danger);
    }

    .card-title {
      color: var(--el-color-danger);
    }
  }

  &.is-simple {
    padding: 12px 16px;

    .card-title {
      margin-bottom: 0;
      font-size: 14px;
    }

    .card-body {
      font-size: 14px;
    }
  }

  .card-icon {
    display: flex;
    flex-shrink: 0;
    align-items: flex-start;
    padding-top: 2px;
  }

  .card-content {
    flex: 1;
    min-width: 0;
  }

  .card-title {
    margin: 0 0 8px;
    font-size: 15px;
    font-weight: 500;
    line-height: 1.4;
  }

  .card-body {
    font-size: 14px;
    color: var(--el-text-color-regular);
    line-height: 1.6;

    :deep(p) {
      margin: 0 0 8px;

      &:last-child {
        margin-bottom: 0;
      }
    }
  }

  .card-actions {
    display: flex;
    gap: 8px;
    margin-top: 12px;
  }

  .card-close {
    flex-shrink: 0;
    margin-left: 8px;

    :deep(.el-button) {
      color: var(--el-text-color-secondary);

      &:hover {
        color: var(--el-text-color-primary);
      }
    }
  }
}
</style>
