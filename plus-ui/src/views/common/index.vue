<!-- 首页工作台 -->
<template>
  <div>
    <!-- 欢迎卡片 -->
    <el-row v-if="welcome.visible" :gutter="16" class="mb-4">
      <el-col :span="24">
        <el-card shadow="hover">
          <div class="flex justify-between items-center flex-wrap gap-5 p-2">
            <div class="flex-1 min-w-200px">
              <h2 class="text-6 font-bold m-0 mb-2">{{ welcome.title }}</h2>
              <p class="text-3.5 opacity-90 m-0 mb-1">{{ welcome.subtitle }}</p>
              <p class="text-3.5 opacity-80 m-0">{{ welcome.message }}</p>
            </div>
            <div v-if="welcome.quickActions && welcome.quickActions.length > 0" class="flex gap-3 flex-wrap">
              <el-button
                v-for="action in welcome.quickActions"
                :key="action.path"
                :icon="action.icon"
                class="border-none! border-white/30 backdrop-blur-10px!"
                @click="router.push(action.path)"
              >
                {{ action.name }}
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 核心统计卡片 -->
    <el-row v-if="coreStats.visible" :gutter="16">
      <el-col class="mb-4" :xs="24" :sm="12" :md="8" :lg="6">
        <AStatsCard
          title="总用户数"
          :value="coreStats.totalUsers"
          description="较昨日"
          icon="user"
          :trend="{ value: coreStats.userGrowthRate, isUp: coreStats.userGrowthRate > 0 }"
          showAction
          action-icon="recharge"
          actionIconColor="#6D93FF"
          @action="() => (showRecharge = true)"
          clickable
          @click="goToUserManagement()"
        />
      </el-col>
      <el-col class="mb-4" :xs="24" :sm="12" :md="8" :lg="6">
        <AStatsCard
          title="今日活跃"
          :value="coreStats.todayActive"
          icon="hot"
          :trend="{ value: coreStats.activeGrowthRate, isUp: coreStats.activeGrowthRate > 0 }"
          clickable
          @click="goToUserManagement('today', 'loginDate')"
        />
      </el-col>
      <el-col class="mb-4" :xs="24" :sm="12" :md="8" :lg="6">
        <AStatsCard
          title="今日订单"
          :value="coreStats.todayOrders"
          icon="order"
          description="较昨日"
          :trend="{ value: coreStats.orderGrowthRate, isUp: coreStats.orderGrowthRate > 0 }"
          clickable
          @click="goToOrderManagement('today')"
        />
      </el-col>
      <el-col class="mb-4" :xs="24" :sm="12" :md="8" :lg="6">
        <AStatsCard
          title="今日收入"
          :value="coreStats.todayRevenue"
          :decimals="2"
          icon="money"
          description="较昨日"
          :trend="{ value: coreStats.revenueGrowthRate, isUp: coreStats.revenueGrowthRate > 0 }"
          clickable
          @click="goToOrderManagement('today', 'paid,delivered,completed')"
        />
      </el-col>
    </el-row>

    <!-- 图表统计 -->
    <el-row :gutter="16">
      <el-col v-if="userGrowth.visible" class="mb-4" :xs="24" :md="12" :lg="8">
        <ABarStatsCard
          title="用户增长"
          :y-axis-min-interval="1"
          :description="userGrowth.description"
          :subtitle="userGrowth.subtitle"
          :chart-data="userGrowth.chartData"
          :x-axis-data="userGrowth.xAxisData"
          :stats="userGrowth.stats"
          stats-clickable
          @stat-click="handleUserStatClick"
        />
      </el-col>
      <el-col v-if="orderConversion.visible" class="mb-4" :xs="24" :md="12" :lg="8">
        <ABarStatsCard
          title="订单转化"
          :description="orderConversion.description"
          :subtitle="orderConversion.subtitle"
          :chart-data="orderConversion.chartData"
          :x-axis-data="orderConversion.xAxisData"
          :stats="orderConversion.stats"
          stats-clickable
          @stat-click="handleOrderConversionClick"
        />
      </el-col>
      <el-col v-if="orderStats.visible" class="mb-4" :xs="24" :md="12" :lg="8">
        <ALineStatsCard
          title="订单统计"
          :description="orderStats.description"
          :subtitle="orderStats.subtitle"
          :chart-data="orderStats.chartData"
          :x-axis-data="orderStats.xAxisData"
          :stats="orderStats.stats"
          stats-clickable
          show-area-color
          @stat-click="handleOrderStatsClick"
        />
      </el-col>
    </el-row>

    <ARecharge v-model="showRecharge" />
  </div>
</template>

<script setup lang="ts" name="Index">
import { getHomeStatistics } from '@/api/common/base/statistics/statisticsApi'
import type { CoreStatsVo, ChartStatsVo, WelcomeVo } from '@/api/common/base/statistics/statisticsTypes'
import type { StatItem } from '@/components/ACard/ABarStatsCard.vue'

const router = useRouter()

/**是否显示充值弹窗*/
const showRecharge = ref(false)

/**欢迎信息*/
const welcome = ref<WelcomeVo>({
  visible: false,
  title: '',
  subtitle: '',
  message: '',
  quickActions: []
})

/**核心统计数据*/
const coreStats = ref<CoreStatsVo>({
  visible: false,
  totalUsers: 0,
  userGrowthRate: 0,
  todayActive: 0,
  activeGrowthRate: 0,
  todayOrders: 0,
  orderGrowthRate: 0,
  todayRevenue: '0',
  revenueGrowthRate: 0
})

/**用户增长数据*/
const userGrowth = ref<ChartStatsVo>({
  visible: false,
  description: '',
  subtitle: '',
  chartData: [],
  xAxisData: [],
  stats: []
})

/**订单转化数据*/
const orderConversion = ref<ChartStatsVo>({
  visible: false,
  description: '',
  subtitle: '',
  chartData: [],
  xAxisData: [],
  stats: []
})

/**订单统计数据*/
const orderStats = ref<ChartStatsVo>({
  visible: false,
  description: '',
  subtitle: '',
  chartData: [],
  xAxisData: [],
  stats: []
})

/**
 * 日期类型映射表
 * 用于统一管理统计标签到日期类型的映射关系
 */
const DATE_TYPE_MAP: Record<string, string> = {
  '今日': 'today',
  '昨日': 'yesterday',
  '本周': 'week',
  '本月': 'month'
}

/**
 * 从统计标签中提取日期类型
 * @param label 统计标签
 * @returns 日期类型或undefined
 */
const extractDateType = (label: string): string | undefined => {
  for (const [key, value] of Object.entries(DATE_TYPE_MAP)) {
    if (label.includes(key)) {
      return value
    }
  }
  return undefined
}

/**
 * 跳转到用户管理页面
 * @param dateType 日期类型: 'today' | 'yesterday' | 'week' | 'month'
 * @param field 排序字段: 'createTime' - 按创建时间排序
 */
const goToUserManagement = (dateType?: string, field?: string) => {
  const query: Record<string, string> = {}

  if (dateType) {
    query.dateType = dateType
  }

  query.field = field || 'createTime'
  router.push({
    path: '/system/user',
    query
  })
}

/**
 * 跳转到订单管理页面
 * @param dateType 日期类型: 'today' | 'yesterday' | 'week' | 'month'
 * @param orderStatus 订单状态
 */
const goToOrderManagement = (dateType?: string, orderStatus?: string) => {
  const query: Record<string, string> = {}

  if (dateType) {
    query.dateType = dateType
  }

  if (orderStatus) {
    query.orderStatus = orderStatus
  }

  router.push({
    path: '/mallManage/order',
    query
  })
}

/**
 * 处理用户增长统计项点击
 * @param stat 统计项数据
 * @param index 统计项索引
 */
const handleUserStatClick = (stat: StatItem, index: number) => {
  const dateType = extractDateType(stat.label)
  goToUserManagement(dateType)
}

/**
 * 处理订单转化统计项点击
 * 根据统计项标签跳转到对应订单状态的列表
 * @param stat 统计项数据
 * @param index 统计项索引
 */
const handleOrderConversionClick = (stat: StatItem, index: number) => {
  const orderStatusMap: Record<string, string> = {
    '待支付': 'pending',
    '已支付': 'paid',
    '已发货': 'delivered',
    '已完成': 'completed',
    '转化率': 'paid,delivered,completed'
  }

  // 查找匹配的订单状态
  const orderStatus = Object.entries(orderStatusMap).find(([key]) => stat.label.includes(key))?.[1]

  router.push({
    path: '/mallManage/order',
    query: orderStatus ? { orderStatus } : {}
  })
}

/**
 * 处理订单统计项点击
 * @param stat 统计项数据
 * @param index 统计项索引
 */
const handleOrderStatsClick = (stat: StatItem, index: number) => {
  const dateType = extractDateType(stat.label)
  goToOrderManagement(dateType)
}

/**获取首页统计数据*/
const fetchHomeStatistics = async (): Promise<void> => {
  const [err, data] = await getHomeStatistics()

  if (!err && data) {
    // 更新欢迎信息
    if (data.welcome) {
      welcome.value = data.welcome
    }

    // 更新核心统计
    if (data.coreStats) {
      coreStats.value = data.coreStats
    }

    // 更新图表数据
    if (data.userGrowth) {
      userGrowth.value = data.userGrowth
    }

    if (data.orderConversion) {
      orderConversion.value = data.orderConversion
    }

    if (data.orderStats) {
      orderStats.value = data.orderStats
    }
  }
}

/**组件挂载*/
onMounted(() => {
  fetchHomeStatistics()
})
</script>

<style lang="scss" scoped></style>
