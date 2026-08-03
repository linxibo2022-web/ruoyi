<!-- 数据大屏全屏 -->
<template>
  <div class="datav-fullscreen">
    <!-- 顶部标题栏 -->
    <div class="datav-header">
      <decoration-5 class="header-left-decoration" />
      <div class="header-title">
        <decoration-8 class="title-decoration" :reverse="true" />
        <span>数据可视化大屏</span>
        <decoration-8 class="title-decoration" />
      </div>
      <decoration-5 class="header-right-decoration" :reverse="true" />
    </div>

    <!-- 主体内容 -->
    <div class="datav-main">
      <!-- 左侧面板 -->
      <div class="datav-left">
        <!-- 左上 - 实时数据统计 -->
        <border-box-1 class="panel-item">
          <div class="panel-header">
            <decoration-3 />
            <span>实时数据统计</span>
          </div>
          <div class="stats-grid">
            <div v-for="(stat, index) in statsData" :key="index" class="stat-item">
              <decoration-6 class="stat-decoration" />
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-label">{{ stat.label }}</div>
            </div>
          </div>
        </border-box-1>

        <!-- 左中 - 柱状图 -->
        <border-box-8 class="panel-item" :dur="3">
          <div class="panel-header">
            <decoration-7>区域销售排行</decoration-7>
          </div>
          <AChart :config="barChartOption" height="280px" />
        </border-box-8>

        <!-- 左下 - 饼图 -->
        <border-box-13 class="panel-item">
          <div class="panel-header">
            <decoration-3 />
            <span>产品销售占比</span>
          </div>
          <AChart :config="pieChartOption" height="280px" />
        </border-box-13>
      </div>

      <!-- 中间面板 -->
      <div class="datav-center">
        <!-- 中心数据 -->
        <border-box-11 title="核心指标" class="center-panel">
          <div class="center-content">
            <decoration-4 class="center-top-decoration" />

            <div class="center-main">
              <border-box-12 class="center-box">
                <div class="center-stat">
                  <div class="center-value">125,680</div>
                  <div class="center-label">今日销售额 (元)</div>
                  <decoration-2 class="center-decoration" />
                </div>
              </border-box-12>

              <border-box-12 class="center-box">
                <div class="center-stat">
                  <div class="center-value">98.5%</div>
                  <div class="center-label">目标完成率</div>
                  <decoration-2 class="center-decoration" :reverse="true" />
                </div>
              </border-box-12>
            </div>

            <decoration-4 class="center-bottom-decoration" :reverse="true" />
          </div>
        </border-box-11>

        <!-- 中下 - 折线图 -->
        <border-box-10 class="center-chart">
          <div class="panel-header">
            <decoration-7>30天销售趋势</decoration-7>
          </div>
          <AChart :config="lineChartOption" height="240px" />
        </border-box-10>
      </div>

      <!-- 右侧面板 -->
      <div class="datav-right">
        <!-- 右上 - 雷达图 -->
        <border-box-6 class="panel-item">
          <div class="panel-header">
            <decoration-3 />
            <span>能力评估</span>
          </div>
          <AChart :config="radarChartOption" height="280px" />
        </border-box-6>

        <!-- 右中 - 数据排名 -->
        <border-box-9 class="panel-item rank-panel">
          <div class="panel-header">
            <decoration-7>销售排名</decoration-7>
          </div>
          <div class="rank-list">
            <div v-for="(item, index) in rankData" :key="index" class="rank-item">
              <decoration-1 class="rank-decoration" />
              <div class="rank-index">{{ index + 1 }}</div>
              <div class="rank-info">
                <div class="rank-name">{{ item.name }}</div>
                <div class="rank-value">{{ item.value }}</div>
              </div>
            </div>
          </div>
        </border-box-9>

        <!-- 右下 - 地图 -->
        <border-box-5 class="panel-item" :reverse="true">
          <div class="panel-header">
            <decoration-3 />
            <span>地区分布</span>
          </div>
          <AMapChart :map-data="mapData" :show-scatter="true" :show-labels="false" height="280px" />
        </border-box-5>
      </div>
    </div>

    <!-- 底部装饰 -->
    <div class="datav-footer">
      <decoration-1 />
    </div>
  </div>
</template>

<script setup lang="ts">
import AMapChart from '@/components/AChart/AMapChart.vue'
import type { MapDataItem } from '@/components/AChart/AMapChart.vue'
import {
  BorderBox1,
  BorderBox5,
  BorderBox6,
  BorderBox8,
  BorderBox9,
  BorderBox10,
  BorderBox11,
  BorderBox12,
  BorderBox13,
  Decoration1,
  Decoration2,
  Decoration3,
  Decoration4,
  Decoration5,
  Decoration6,
  Decoration7,
  Decoration8
} from '@kjgl77/datav-vue3'

// 统计数据
const statsData = ref([
  { label: '今日访问', value: '12,345' },
  { label: '总订单', value: '8,234' },
  { label: '总用户', value: '56,789' },
  { label: '转化率', value: '68.5%' }
])

// 排名数据
const rankData = ref([
  { name: '华东区', value: '¥125,680' },
  { name: '华北区', value: '¥98,450' },
  { name: '华南区', value: '¥87,230' },
  { name: '西南区', value: '¥76,540' },
  { name: '东北区', value: '¥65,890' }
])

// 地图数据
const mapData = ref<MapDataItem[]>([
  { name: '北京', value: 320, adcode: '110000', level: 'province' },
  { name: '上海', value: 280, adcode: '310000', level: 'province' },
  { name: '广东', value: 450, adcode: '440000', level: 'province' },
  { name: '浙江', value: 380, adcode: '330000', level: 'province' },
  { name: '江苏', value: 420, adcode: '320000', level: 'province' },
  { name: '四川', value: 240, adcode: '510000', level: 'province' },
  { name: '山东', value: 360, adcode: '370000', level: 'province' }
])

// 柱状图配置
const barChartOption = computed<any>(() => ({
  grid: {
    left: '15%',
    right: '15%',
    top: '15%',
    bottom: '15%'
  },
  xAxis: {
    type: 'category',
    data: ['华北', '华东', '华南', '西南', '东北'],
    axisLine: { lineStyle: { color: '#4fd2dd' } },
    axisLabel: { color: '#fff' }
  },
  yAxis: {
    type: 'value',
    axisLine: { lineStyle: { color: '#4fd2dd' } },
    axisLabel: { color: '#fff' },
    splitLine: { lineStyle: { color: '#2e5266', type: 'dashed' } }
  },
  series: [
    {
      data: [120, 200, 150, 80, 70],
      type: 'bar',
      itemStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: '#4fd2dd' },
            { offset: 1, color: '#235fa7' }
          ]
        }
      },
      barWidth: '40%'
    }
  ]
}))

// 饼图配置
const pieChartOption = computed<any>(() => ({
  legend: {
    orient: 'vertical',
    right: '5%',
    top: 'center',
    textStyle: { color: '#fff' }
  },
  series: [
    {
      name: '销售占比',
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['40%', '50%'],
      data: [
        { value: 335, name: '产品A' },
        { value: 310, name: '产品B' },
        { value: 234, name: '产品C' },
        { value: 135, name: '产品D' }
      ],
      itemStyle: {
        borderRadius: 10,
        borderColor: '#0b1c2c',
        borderWidth: 2
      },
      label: {
        color: '#fff'
      }
    }
  ]
}))

// 折线图配置
const lineChartOption = computed<any>(() => ({
  grid: {
    left: '8%',
    right: '5%',
    top: '15%',
    bottom: '10%'
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: Array.from({ length: 30 }, (_, i) => `${i + 1}日`),
    axisLine: { lineStyle: { color: '#4fd2dd' } },
    axisLabel: { color: '#fff', interval: 4 }
  },
  yAxis: {
    type: 'value',
    axisLine: { lineStyle: { color: '#4fd2dd' } },
    axisLabel: { color: '#fff' },
    splitLine: { lineStyle: { color: '#2e5266', type: 'dashed' } }
  },
  series: [
    {
      data: Array.from({ length: 30 }, () => Math.floor(Math.random() * 100) + 50),
      type: 'line',
      smooth: true,
      itemStyle: { color: '#4fd2dd' },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(79, 210, 221, 0.3)' },
            { offset: 1, color: 'rgba(79, 210, 221, 0.05)' }
          ]
        }
      }
    }
  ]
}))

// 雷达图配置
const radarChartOption = computed<any>(() => ({
  radar: {
    indicator: [
      { name: '销售', max: 100 },
      { name: '管理', max: 100 },
      { name: '技术', max: 100 },
      { name: '客服', max: 100 },
      { name: '研发', max: 100 }
    ],
    splitArea: {
      areaStyle: {
        color: ['rgba(79, 210, 221, 0.1)', 'rgba(79, 210, 221, 0.05)']
      }
    },
    axisLine: { lineStyle: { color: '#4fd2dd' } },
    splitLine: { lineStyle: { color: '#4fd2dd' } }
  },
  series: [
    {
      type: 'radar',
      data: [
        {
          value: [80, 90, 85, 75, 95],
          areaStyle: {
            color: 'rgba(79, 210, 221, 0.3)'
          }
        }
      ]
    }
  ]
}))
</script>

<style scoped lang="scss">
.datav-fullscreen {
  width: 100vw;
  min-height: 100vh;
  background: #0b1c2c;
  background-image:
    radial-gradient(circle at 20% 50%, rgba(79, 210, 221, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 80%, rgba(35, 95, 167, 0.1) 0%, transparent 50%);
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  color: #fff;
}

// 顶部标题
.datav-header {
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 40px;
  position: relative;

  .header-left-decoration,
  .header-right-decoration {
    width: 300px;
    height: 40px;
  }

  .header-title {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 32px;
    font-weight: bold;
    letter-spacing: 4px;
    color: #4fd2dd;
    text-shadow: 0 0 10px rgba(79, 210, 221, 0.5);

    .title-decoration {
      width: 200px;
      height: 50px;
      margin: 0 20px;
    }
  }
}

// 主体内容
.datav-main {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr 1.2fr 1fr;
  gap: 20px;
  padding: 0 20px 20px;
  min-height: 0;
}

.datav-left,
.datav-right {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.datav-center {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

// 面板通用样式
.panel-item {
  flex: 1;
  padding: 15px;
  min-height: 0;
}

.center-panel {
  flex: 0.6;
  padding: 20px;
}

.center-chart {
  flex: 1;
  padding: 15px;
}

.panel-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
  font-size: 16px;
  font-weight: bold;
  color: #4fd2dd;

  :deep(.dv-decoration-3) {
    width: 150px;
    height: 20px;
  }

  :deep(.dv-decoration-7) {
    width: 100%;
    height: 25px;
  }
}

// 统计数据网格
.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
  padding: 10px;
}

.stat-item {
  text-align: center;
  padding: 15px;
  background: rgba(79, 210, 221, 0.05);
  border-radius: 8px;
  border: 1px solid rgba(79, 210, 221, 0.2);

  .stat-decoration {
    width: 100%;
    height: 20px;
    margin-bottom: 10px;
  }

  .stat-value {
    font-size: 24px;
    font-weight: bold;
    color: #4fd2dd;
    margin: 10px 0;
  }

  .stat-label {
    font-size: 14px;
    color: #8b9eb5;
  }
}

// 中心内容
.center-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 20px;
}

.center-top-decoration,
.center-bottom-decoration {
  width: 100%;
  height: 60px;
}

.center-main {
  flex: 1;
  display: flex;
  gap: 30px;
  align-items: center;
  justify-content: center;
}

.center-box {
  flex: 1;
  height: 180px;
  padding: 20px;
}

.center-stat {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  .center-value {
    font-size: 48px;
    font-weight: bold;
    color: #4fd2dd;
    text-shadow: 0 0 20px rgba(79, 210, 221, 0.6);
  }

  .center-label {
    font-size: 16px;
    color: #8b9eb5;
    margin-top: 15px;
    margin-bottom: 20px;
  }

  .center-decoration {
    width: 80%;
    height: 6px;
  }
}

// 排名面板
.rank-panel {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

// 排名列表
.rank-list {
  padding: 10px 20px;
  overflow-y: auto;
  flex: 1;
}

.rank-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 12px;
  margin-bottom: 10px;
  background: rgba(79, 210, 221, 0.05);
  border-radius: 6px;
  border-left: 3px solid #4fd2dd;

  .rank-decoration {
    width: 80px;
    height: 30px;
  }

  .rank-index {
    width: 30px;
    height: 30px;
    border-radius: 50%;
    background: linear-gradient(135deg, #4fd2dd, #235fa7);
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: bold;
    font-size: 16px;
  }

  .rank-info {
    flex: 1;

    .rank-name {
      font-size: 14px;
      color: #fff;
    }

    .rank-value {
      font-size: 16px;
      color: #4fd2dd;
      font-weight: bold;
      margin-top: 5px;
    }
  }
}

// 底部装饰
.datav-footer {
  height: 30px;
  padding: 0 20px;

  :deep(.dv-decoration-1) {
    width: 100%;
    height: 30px;
  }
}

// 响应式适配
@media (max-width: 1600px) {
  .datav-main {
    grid-template-columns: 1fr 1fr 1fr;
  }

  .header-title {
    font-size: 28px !important;
  }
}

@media (max-width: 1200px) {
  .datav-main {
    grid-template-columns: 1fr;
    grid-template-rows: auto auto auto;
  }
}
</style>
