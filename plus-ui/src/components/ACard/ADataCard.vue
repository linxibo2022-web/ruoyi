<!--
数据卡片组件
基础使用方式示例：

1. 基本用法 - 显示单个数据
<ADataCard
  title="总用户数"
  icon-code="user"
  :data-list="[{ label: '当前用户', value: '12,345', percent: 5.2 }]"
/>

2. 对比数据 - 主数据 + 多个对比项
<ADataCard
  title="销售数据"
  icon-code="chart"
  :data-list="[
    { label: '今日销售', value: '¥8,520', percent: 12.5 },
    { label: '昨日', value: '¥7,580', percent: -2.1 },
    { label: '本周', value: '¥45,600', percent: 8.9 },
    { label: '上周', value: '¥41,200', percent: 3.2 }
  ]"
  @item-click="handleItemClick"
/>

3. 自定义响应式布局
<ADataCard
  title="访问统计"
  icon-code="eye"
  :data-list="[{ label: '今日访问', value: '1,234' }]"
  :col-config="{ xs: 24, sm: 12, md: 8, lg: 6, xl: 4 }"
/>

4. 带操作按钮和底部内容
<ADataCard
  title="订单数据"
  icon-code="shopping-cart"
  :data-list="[{ label: '待处理订单', value: '28', percent: -5.5 }]"
>
  <template #actions>
    <el-button size="small" type="primary">查看详情</el-button>
  </template>
  <template #footer>
    <div class="text-xs text-gray-500">最后更新：2分钟前</div>
  </template>
</ADataCard>

5. 无百分比变化的数据
<ADataCard
  title="库存状态"
  icon-code="package"
  :data-list="[
    { label: '总库存', value: '2,580件' },
    { label: '可用', value: '2,100件' },
    { label: '预定', value: '380件' },
    { label: '缺货', value: '100件' }
  ]"
/>
-->
<template>
  <el-col :xs="colConfig.xs" :sm="colConfig.sm" :md="colConfig.md" :lg="colConfig.lg" :xl="colConfig.xl" class="mb-4">
    <el-card class="h-full transition-all duration-300 ease hover:shadow-lg hover:-translate-y-1" shadow="hover">
      <!-- 卡片头部 -->
      <template #header>
        <div class="flex justify-between items-center">
          <div class="flex items-center gap-2">
            <Icon :code="iconCode" :value="iconValue" class="text-base text-blue-500 dark:text-blue-400" />
            <span class="text-sm font-medium">{{ title }}</span>
          </div>
          <slot name="actions"></slot>
        </div>
      </template>

      <!-- 主数据展示区域 - 取数组第一项 -->
      <div v-if="dataList.length > 0" class="flex-1 flex justify-between items-center mb-3">
        <div class="flex-1">
          <div class="mb-1 text-xl font-bold leading-none">{{ dataList[0].value }}</div>
          <div class="mb-1 text-xs text-gray-500 dark:text-gray-400">{{ dataList[0].label }}</div>
          <div class="flex items-center gap-2" v-if="dataList[0].percent !== undefined">
            <el-tag :type="getTrendType(dataList[0].percent)" size="small" class="text-xs font-medium rounded">
              {{ getTrendIcon(dataList[0].percent) }} {{ formatPercent(dataList[0].percent) }}
            </el-tag>
          </div>
        </div>
      </div>

      <!-- 对比数据网格 - 显示除第一项外的其他数据 -->
      <el-row :gutter="4" v-if="compareData.length > 0">
        <el-col :span="6" v-for="(item, index) in compareData" :key="index" class="mb-1">
          <div
            class="h-full p-2 rounded text-center cursor-pointer transition-all duration-300 hover:shadow-sm hover:-translate-y-0.5"
            :style="{
              backgroundColor: 'var(--bg-level-2)',
              '--hover-bg': 'var(--bg-level-3)'
            }"
            @click="onItemClick(item, index + 1)"
          >
            <div class="mb-0.5 text-xs text-gray-500 dark:text-gray-400 truncate">{{ item.label }}</div>
            <div class="mb-0.5 text-sm font-medium text-gray-800 dark:text-gray-200 truncate">{{ item.value }}</div>
            <el-tag v-if="item.percent !== undefined" :type="getTrendType(item.percent)" size="small" class="text-xs font-medium rounded">
              {{ formatPercent(item.percent) }}
            </el-tag>
          </div>
        </el-col>
      </el-row>

      <!-- 底部插槽 -->
      <div v-if="$slots.footer" class="mt-3 pt-2 border-t border-gray-100 dark:border-gray-700">
        <slot name="footer"></slot>
      </div>
    </el-card>
  </el-col>
</template>

<script setup lang="ts" name="ADataCard">
/**定义数据项接口*/
export interface DataItem {
  /**数据标签，如："今日新增"、"昨日"、"本周"等*/
  label: string
  /**数值*/
  value: number | string
  /**变化百分比*/
  percent?: number
}

/**响应式配置接口*/
interface ColConfig {
  /**超小屏幕 <768px*/
  xs?: number
  /**小屏幕 ≥768px*/
  sm?: number
  /**中等屏幕 ≥992px*/
  md?: number
  /**大屏幕 ≥1200px*/
  lg?: number
  /**超大屏幕 ≥1920px*/
  xl?: number
}

interface ADataCardProps {
  /**卡片标题*/
  title: string
  /**图标代码，优先使用*/
  iconCode?: IconCode
  /**图标值，备用*/
  iconValue?: string
  /**数据列表（第一项作为主数据展示）*/
  dataList: DataItem[]
  /**响应式列配置*/
  colConfig?: ColConfig
  /**是否可点击*/
  clickable?: boolean
}

/**定义组件属性*/
const props = withDefaults(defineProps<ADataCardProps>(), {
  iconValue: '',
  colConfig: () => ({
    /**更小屏幕*/
    xs: 24,
    /**小屏幕*/
    sm: 12,
    /**中等屏幕*/
    md: 12,
    /**大屏幕*/
    lg: 6,
    /**超大屏幕*/
    xl: 6
  }),
  clickable: true
})

/**定义事件*/
const emit = defineEmits<{
  itemClick: [item: DataItem, index: number]
}>()

/**对比数据（除第一项外的其他数据）*/
const compareData = computed(() => {
  return props.dataList.slice(1)
})

/**百分比格式化*/
const formatPercent = (percent: number | null | undefined): string => {
  if (percent == null) return '-'
  const sign = percent > 0 ? '+' : ''
  return `${sign}${percent.toFixed(1)}%`
}

/**获取趋势图标*/
const getTrendIcon = (percent: number | null | undefined): string => {
  if (percent == null) return '-'
  return percent > 0 ? '↗' : percent < 0 ? '↘' : '→'
}

/**获取趋势类型*/
const getTrendType = (percent: number | null | undefined): ElTagType => {
  if (percent == null) return 'info'
  if (percent > 0) return 'success'
  if (percent < 0) return 'danger'
  return 'info'
}

/**项目点击事件*/
const onItemClick = (item: DataItem, index: number) => {
  if (props.clickable) {
    emit('itemClick', item, index)
  }
}
</script>
