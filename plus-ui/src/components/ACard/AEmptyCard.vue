<!--
空状态卡片组件 AEmptyCard

使用示例:

1. 基本用法
<el-row :gutter="20">
  <el-col :span="24">
    <AEmptyCard
      title="暂无数据"
      description="还没有任何内容,快来创建第一条吧"
    />
  </el-col>
</el-row>

2. 带图标提示
<el-row :gutter="20">
  <el-col :xs="24" :md="12">
    <AEmptyCard
      icon="empty"
      title="空空如也"
      description="试试创建第一个项目吧"
    />
  </el-col>
  <el-col :xs="24" :md="12">
    <AEmptyCard
      icon="folder-open"
      title="暂无文件"
      description="还没有上传任何文件"
    />
  </el-col>
</el-row>

3. 带操作按钮
<el-row :gutter="20">
  <el-col :span="24">
    <AEmptyCard
      icon="inbox"
      title="没有订单"
      description="您还没有任何订单记录"
      :actions="[
        { label: '开始购物', type: 'primary', handler: () => {} },
        { label: '浏览商品', handler: () => {} }
      ]"
    />
  </el-col>
</el-row>

4. 搜索无结果
<el-row :gutter="20">
  <el-col :span="24">
    <AEmptyCard
      icon="search"
      title="没有找到结果"
      description="尝试调整筛选条件或使用其他关键词"
      :actions="[
        { label: '重置筛选', type: 'primary', handler: () => {} }
      ]"
    />
  </el-col>
</el-row>

5. 自定义图片
<el-row :gutter="20">
  <el-col :span="24">
    <AEmptyCard
      image="https://picsum.photos/200/200"
      title="暂无收藏"
      description="收藏的内容会显示在这里"
      :actions="[
        { label: '去逛逛', type: 'primary', handler: () => {} }
      ]"
    />
  </el-col>
</el-row>
-->
<template>
  <div class="empty-card" :style="{ height: height }">
    <!-- 图片/图标 -->
    <div class="empty-visual">
      <el-image v-if="image" :src="image" :style="{ width: imageSize, height: imageSize }" fit="contain" />
      <Icon v-else-if="icon" :code="icon" :size="iconSize" />
      <Icon v-else code="empty" :size="iconSize" />
    </div>
    <!-- 标题 -->
    <h3 class="empty-title" :class="{ 'is-empty': !title }">
      {{ title || '\u00A0' }}
    </h3>

    <!-- 描述 -->
    <p v-if="description" class="empty-description">{{ description }}</p>

    <!-- 操作按钮 -->
    <div v-if="actions.length > 0 || $slots.actions" class="empty-actions">
      <slot name="actions">
        <el-button
          v-for="(action, index) in actions"
          :key="index"
          :type="action.type || ''"
          :size="action.size || 'default'"
          @click="handleAction(action)"
        >
          {{ action.label }}
        </el-button>
      </slot>
    </div>
  </div>
</template>

<script setup lang="ts" name="AEmptyCard">
/**操作按钮接口*/
interface ActionButton {
  /**按钮文本*/
  label: string
  /**按钮类型*/
  type?: 'primary' | 'success' | 'warning' | 'danger' | 'info' | ''
  /**按钮尺寸*/
  size?: 'large' | 'default' | 'small'
  /**点击处理函数*/
  handler?: () => void
}

/**空状态卡片属性接口*/
interface AEmptyCardProps {
  /**图标*/
  icon?: IconCode
  /**图标大小*/
  iconSize?: number
  /**自定义图片*/
  image?: string
  /**图片大小*/
  imageSize?: string
  /**标题*/
  title?: string
  /**描述*/
  description?: string
  /**操作按钮列表*/
  actions?: ActionButton[]
  /**卡片高度*/
  height?: string
}

/**组件属性定义*/
const props = withDefaults(defineProps<AEmptyCardProps>(), {
  iconSize: 80,
  imageSize: '160px',
  title: '',
  description: '',
  actions: () => [],
  height: 'auto'
})

/**处理操作按钮点击*/
const handleAction = (action: ActionButton) => {
  if (action.handler) {
    action.handler()
  }
}

/**暴露方法*/
defineExpose({
  /**标题*/
  title: computed(() => props.title)
})
</script>

<style lang="scss" scoped>
.empty-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  background-color: var(--bg-level-1);
  border-radius: var(--radius-md);
  border: 1px solid var(--el-border-color);

  .empty-visual {
    margin-bottom: 16px;
    color: var(--el-text-color-placeholder);
    height: 80px;
    display: flex;
    align-items: center;
    justify-content: center;

    :deep(.el-image) {
      display: block;
    }
  }

  .empty-title {
    margin: 0 0 4px;
    font-size: 16px;
    font-weight: 500;
    color: var(--el-text-color-primary);
    text-align: center;
    min-height: 24px;
  }

  .empty-description {
    margin: 0 0 24px;
    font-size: 14px;
    color: var(--el-text-color-secondary);
    text-align: center;
    line-height: 1.6;
  }

  .empty-actions {
    display: flex;
    gap: 12px;
    flex-wrap: wrap;
    justify-content: center;
  }
}
</style>
