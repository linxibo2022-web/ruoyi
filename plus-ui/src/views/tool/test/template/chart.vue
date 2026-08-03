<!-- 图表模板 -->
<template>
  <div>
    <!-- 图表示例头部 -->
    <div class="flex justify-between items-center">
      <div class="flex items-center">
        <h3>图表示例</h3>
        <el-divider direction="vertical" />
        <el-button-group>
          <el-button :type="chartShowType === '基础示例' ? 'primary' : 'default'" @click="handleChartShowData('基础示例')">基础示例</el-button>
          <el-button :type="chartShowType === '详细示例' ? 'primary' : 'default'" @click="handleChartShowData('详细示例')">详细示例</el-button>
        </el-button-group>
      </div>
      <el-button-group v-if="chartShowType === '详细示例'">
        <el-button
          v-for="(item, index) in chartButtonGroupData"
          :key="index"
          :type="chartShowData.type === item.type ? 'primary' : 'default'"
          @click="handleChartTypeButton(item)"
          >{{ item.name }}</el-button
        >
      </el-button-group>
    </div>

    <el-divider />

    <!-- 基础示例 -->
    <div v-if="chartShowType === '基础示例'" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-2 xl:grid-cols-3 gap-3">
      <el-card shadow="never">
        <template #header>
          <div class="flex justify-between items-center">
            <span>柱状图</span>
            <div class="cursor-pointer hover:opacity-80 transition-opacity" @click="copy(chartShowConfigData.bar.data[0].copy, '复制成功！')">
              <Icon code="copy" size="lg" color="var(--el-color-primary)" />
            </div>
          </div>
        </template>
        <ABarChart :data="[120, 200, 150, 80, 70, 110, 130]" :x-axis-data="['周一', '周二', '周三', '周四', '周五', '周六', '周日']" />
      </el-card>

      <el-card shadow="never">
        <template #header>
          <div class="flex justify-between items-center">
            <span>折线图</span>
            <div class="cursor-pointer hover:opacity-80 transition-opacity" @click="copy(chartShowConfigData.line.data[0].copy, '复制成功！')">
              <Icon code="copy" size="lg" color="var(--el-color-primary)" />
            </div>
          </div>
        </template>
        <ALineChart
          @chart-click="handleLineChartClick"
          :data="[120, 200, 150, 80, 70, 110, 130]"
          :x-axis-data="['周一', '周二', '周三', '周四', '周五', '周六', '周日']"
          symbol="circle"
          :symbol-size="8"
        />
      </el-card>

      <el-card shadow="never">
        <template #header>
          <div class="flex justify-between items-center">
            <span>饼图</span>
            <div class="cursor-pointer hover:opacity-80 transition-opacity" @click="copy(chartShowConfigData.pie.data[0].copy, '复制成功！')">
              <Icon code="copy" size="lg" color="var(--el-color-primary)" />
            </div>
          </div>
        </template>
        <APieChart
          :data="[
            { name: '直接访问', value: 335 },
            { name: '邮件营销', value: 310 },
            { name: '联盟广告', value: 234 },
            { name: '视频广告', value: 135 }
          ]"
        />
      </el-card>

      <el-card shadow="never">
        <template #header>
          <div class="flex justify-between items-center">
            <span>双向柱状图</span>
            <div
              class="cursor-pointer hover:opacity-80 transition-opacity"
              @click="copy(chartShowConfigData['bar-bidirectional'].data[0].copy, '复制成功！')"
            >
              <Icon code="copy" size="lg" color="var(--el-color-primary)" />
            </div>
          </div>
        </template>
        <ABarBidirectionalChart
          :positive-data="[20, 25, 30, 18, 22, 28, 35]"
          :negative-data="[15, 20, 25, 12, 18, 24, 30]"
          :x-axis-data="['周一', '周二', '周三', '周四', '周五', '周六', '周日']"
          positive-name="收入"
          negative-name="支出"
        />
      </el-card>

      <el-card shadow="never">
        <template #header>
          <div class="flex justify-between items-center">
            <span>横向柱状图</span>
            <div
              class="cursor-pointer hover:opacity-80 transition-opacity"
              @click="copy(chartShowConfigData['bar-horizontal'].data[0].copy, '复制成功！')"
            >
              <Icon code="copy" size="lg" color="var(--el-color-primary)" />
            </div>
          </div>
        </template>
        <ABarHorizontalChart
          :data="[120, 200, 150, 80, 70, 110, 130]"
          :y-axis-data="['产品A', '产品B', '产品C', '产品D', '产品E', '产品F', '产品G']"
        />
      </el-card>

      <el-card shadow="never">
        <template #header>
          <div class="flex justify-between items-center">
            <span>K线图</span>
            <div class="cursor-pointer hover:opacity-80 transition-opacity" @click="copy(chartShowConfigData.candlestick.data[0].copy, '复制成功！')">
              <Icon code="copy" size="lg" color="var(--el-color-primary)" />
            </div>
          </div>
        </template>
        <ACandlestickChart
          :data="[
            { time: '2023-01-01', open: 100, close: 102, high: 105, low: 99 },
            { time: '2023-01-02', open: 102, close: 98, high: 103, low: 97 },
            { time: '2023-01-03', open: 98, close: 104, high: 106, low: 96 },
            { time: '2023-01-04', open: 104, close: 107, high: 109, low: 103 }
          ]"
        />
      </el-card>

      <el-card shadow="never">
        <template #header>
          <div class="flex justify-between items-center">
            <span>地图</span>
            <div class="cursor-pointer hover:opacity-80 transition-opacity" @click="copy(chartShowConfigData.map.data[0].copy, '复制成功！')">
              <Icon code="copy" size="lg" color="var(--el-color-primary)" />
            </div>
          </div>
        </template>
        <AMapChart
          :map-data="[
            { name: '北京', value: 100, adcode: '110000', level: 'province' },
            { name: '上海', value: 200, adcode: '310000', level: 'province' },
            { name: '广东', value: 300, adcode: '440000', level: 'province' }
          ]"
        />
      </el-card>

      <el-card shadow="never">
        <template #header>
          <div class="flex justify-between items-center">
            <span>雷达图</span>
            <div class="cursor-pointer hover:opacity-80 transition-opacity" @click="copy(chartShowConfigData.radar.data[0].copy, '复制成功！')">
              <Icon code="copy" size="lg" color="var(--el-color-primary)" />
            </div>
          </div>
        </template>
        <ARadarChart
          :indicator="[
            { name: '销售', max: 100 },
            { name: '管理', max: 100 },
            { name: '信息技术', max: 100 },
            { name: '客服', max: 100 },
            { name: '研发', max: 100 },
            { name: '市场', max: 100 }
          ]"
          :data="[{ name: '预算分配', value: [43, 72, 65, 53, 99, 70] }]"
        />
      </el-card>

      <el-card shadow="never">
        <template #header>
          <div class="flex justify-between items-center">
            <span>散点图</span>
            <div class="cursor-pointer hover:opacity-80 transition-opacity" @click="copy(chartShowConfigData.scatter.data[0].copy, '复制成功！')">
              <Icon code="copy" size="lg" color="var(--el-color-primary)" />
            </div>
          </div>
        </template>
        <AScatterChart :data="[{ value: [10, 20] }, { value: [15, 25] }, { value: [20, 18] }, { value: [25, 30] }, { value: [30, 22] }]" />
      </el-card>
    </div>

    <!-- 详细示例 -->
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-2 xl:grid-cols-3 gap-3">
      <el-card shadow="never" v-for="(item, index) in chartShowData.data" :key="index">
        <template #header>
          <div class="flex justify-between items-center">
            <span>{{ item.title }}</span>
            <div class="cursor-pointer hover:opacity-80 transition-opacity" @click="copy(item.copy, '示例复制成功！')">
              <Icon code="copy" size="lg" color="var(--el-color-primary)" />
            </div>
          </div>
        </template>
        <!-- 柱状图 -->
        <ABarChart
          v-if="chartShowData.type === 'bar'"
          :data="item.data"
          :x-axis-data="item['x-axis-data']"
          :bar-width="item['bar-width']"
          :border-radius="item['border-radius']"
          :stack="item.stack"
          :show-legend="item['show-legend']"
          :colors="item.colors"
        />
        <!-- 折线图 -->
        <ALineChart
          v-if="chartShowData.type === 'line'"
          :data="item.data"
          :x-axis-data="item['x-axis-data']"
          :show-legend="item['show-legend']"
          :show-area-color="item['show-area-color']"
          :smooth="item.smooth"
          :line-width="item['line-width']"
          :symbol="item.symbol"
          :symbol-size="item['symbol-size']"
          :colors="item.colors"
        />
        <!-- 饼图 -->
        <APieChart
          v-if="chartShowData.type === 'pie'"
          :data="item.data"
          :radius="item.radius"
          :center-text="item['center-text']"
          :show-label="item['show-label']"
          :border-radius="item['border-radius']"
          :show-legend="item['show-legend']"
          :legend-position="item['legend-position']"
          :colors="item.colors"
        />
        <!-- 双向柱状图 -->
        <ABarBidirectionalChart
          v-if="chartShowData.type === 'bar-bidirectional'"
          :positive-data="item['positive-data']"
          :negative-data="item['negative-data']"
          :x-axis-data="item['x-axis-data']"
          :positive-name="item['positive-name']"
          :negative-name="item['negative-name']"
          :show-legend="item['show-legend']"
          :show-data-label="item['show-data-label']"
          :bar-width="item['bar-width']"
          :y-axis-min="item['y-axis-min']"
          :y-axis-max="item['y-axis-max']"
          :positive-border-radius="item['positive-border-radius']"
          :negative-border-radius="item['negative-border-radius']"
          :colors="item.colors"
        />
        <!-- 横向柱状图 -->
        <ABarHorizontalChart
          v-if="chartShowData.type === 'bar-horizontal'"
          :data="item.data"
          :y-axis-data="item['y-axis-data']"
          :bar-width="item['bar-width']"
          :stack="item.stack"
          :show-legend="item['show-legend']"
          :colors="item.colors"
        />
        <!-- K线图 -->
        <ACandlestickChart
          v-if="chartShowData.type === 'candlestick'"
          :data="item.data"
          :show-data-zoom="item['show-data-zoom']"
          :data-zoom-start="item['data-zoom-start']"
          :data-zoom-end="item['data-zoom-end']"
          :height="item.height"
          :colors="item.colors"
        />
        <!-- 地图 -->
        <AMapChart
          v-if="chartShowData.type === 'map'"
          :map-data="item['map-data']"
          :show-scatter="item['show-scatter']"
          :show-labels="item['show-labels']"
          :height="item.height"
        />
        <!-- 雷达图 -->
        <ARadarChart
          v-if="chartShowData.type === 'radar'"
          :indicator="item.indicator"
          :data="item.data"
          :show-legend="item['show-legend']"
          :legend-position="item['legend-position']"
          :colors="item.colors"
        />
        <!-- 散点图 -->
        <AScatterChart
          v-if="chartShowData.type === 'scatter'"
          :data="item.data"
          :symbol-size="item['symbol-size']"
          :show-axis-line="item['show-axis-line']"
          :show-split-line="item['show-split-line']"
          :colors="item.colors"
        />
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { copy } from '@/utils/function'

const chartShowConfigData: Record<string, any> = {
  bar: {
    type: 'bar',
    data: [
      {
        title: '基本用法 - 简单数值数组',
        data: [120, 200, 150, 80, 70, 110, 130],
        'x-axis-data': ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
        copy: `<ABarChart
  :data="[120, 200, 150, 80, 70, 110, 130]"
  :x-axis-data="['周一', '周二', '周三', '周四', '周五', '周六', '周日']"
/>`
      },
      {
        title: '自定义单个柱子颜色',
        data: [
          120,
          150,
          80,
          70,
          110,
          130,
          {
            value: 200,
            itemStyle: {
              color: '#505372'
            }
          }
        ],
        'x-axis-data': ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
        copy: `
        <ABarChart
          :data= "[
          120,
          150,
          80,
          70,
          110,
          130,
          {
            value: 200,
            itemStyle: {
              color: '#505372'
            }
          }
          ]"
          :x-axis-data="['周一', '周二', '周三', '周四', '周五', '周六', '周日']"
        />`
      },
      {
        title: '多系列数据 - 对比展示',
        data: [
          { name: '销售额', data: [120, 200, 150, 80, 70, 110, 130] },
          { name: '利润', data: [80, 120, 100, 60, 50, 80, 90] }
        ],
        'x-axis-data': ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
        copy: `
        <ABarChart
          :data="[120, 200, 150, 80, 70, 110, 130]"
          :x-axis-data="['周一', '周二', '周三', '周四', '周五', '周六', '周日']"
        />`
      },
      {
        title: '堆叠柱状图',
        data: [
          {
            name: '数据1',
            type: 'bar',
            stack: 'stack1',
            emphasis: {
              focus: 'series'
            },
            data: [120, 132, 101, 134, 290, 230, 220]
          },
          {
            name: '数据2',
            type: 'bar',
            stack: 'stack1',
            emphasis: {
              focus: 'series'
            },
            data: [60, 72, 71, 74, 190, 130, 110]
          },
          {
            name: '数据3',
            type: 'bar',
            stack: 'stack1',
            emphasis: {
              focus: 'series'
            },
            data: [62, 82, 91, 84, 109, 110, 120]
          }
        ],
        'x-axis-data': ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
        stack: true,
        'show-legend': true,
        copy: `
        <ABarChart
          :data="[120, 200, 150, 80, 70, 110, 130]"
          :x-axis-data="['周一', '周二', '周三', '周四', '周五', '周六', '周日']"
          :stack="true"
          :show-legend="true"
        />`
      },
      {
        title: '自定义样式',
        data: [120, 200, 150, 80, 70, 110, 130],
        'x-axis-data': ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
        'bar-width': '60%',
        'border-radius': 8,
        colors: ['#b48df3', '#ffffff', '#f9901f'],
        copy: `
        <ABarChart
          :data="[120, 200, 150, 80, 70, 110, 130]"
          :x-axis-data="['周一', '周二', '周三', '周四', '周五', '周六', '周日']"
          bar-width="60%"
          :border-radius="8"
          :is-custom-theme-color="true"
          :colors="['#b48df3', '#ffffff', '#f9901f']"
        />`
      }
    ]
  },
  line: {
    type: 'line',
    data: [
      {
        title: '基本用法 - 简单数值数组',
        data: [120, 200, 150, 80, 70, 110, 130],
        'x-axis-data': ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
        copy: `
        <ALineChart
          :data="[120, 200, 150, 80, 70, 110, 130]"
          :x-axis-data="['周一', '周二', '周三', '周四', '周五', '周六', '周日']"
        />`
      },
      {
        title: '多系列数据 - 对比展示',
        data: [
          { name: '访问量', data: [120, 200, 150, 80, 70, 110, 130] },
          { name: '用户数', data: [80, 120, 100, 60, 50, 80, 90] }
        ],
        'x-axis-data': ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
        'show-legend': true,
        copy: `
        <ALineChart
          :data="[
            { name: '访问量', data: [120, 200, 150, 80, 70, 110, 130] },
            { name: '用户数', data: [80, 120, 100, 60, 50, 80, 90] }
          ]"
          :x-axis-data="['周一', '周二', '周三', '周四', '周五', '周六', '周日']"
          :show-legend="true"
        />`
      },
      {
        title: '区域填充',
        data: [120, 200, 150, 80, 70, 110, 130],
        'x-axis-data': ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
        'show-area-color': true,
        smooth: true,
        copy: `
        <ALineChart
          :data="[120, 200, 150, 80, 70, 110, 130]"
          :x-axis-data="['周一', '周二', '周三', '周四', '周五', '周六', '周日']"
          :show-area-color="true"
          :smooth="true"
        />`
      },
      {
        title: '自定义样式',
        data: [120, 200, 150, 80, 70, 110, 130],
        'x-axis-data': ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
        'line-width': 3,
        symbol: 'circle',
        'symbol-size': 8,
        colors: ['#5470c6', '#91cc75', '#fac858'],
        copy: `
        <ALineChart
          :data="[120, 200, 150, 80, 70, 110, 130]"
          :x-axis-data="['周一', '周二', '周三', '周四', '周五', '周六', '周日']"
          :line-width="3"
          symbol="circle"
          :symbol-size="8"
          :colors="['#5470c6', '#91cc75', '#fac858']"
        />`
      }
    ]
  },
  pie: {
    type: 'pie',
    data: [
      {
        title: '基本饼图',
        data: [
          { name: '直接访问', value: 335 },
          { name: '邮件营销', value: 310 },
          { name: '联盟广告', value: 234 },
          { name: '视频广告', value: 135 },
          { name: '搜索引擎', value: 186 }
        ],
        copy: `
        <APieChart
          :data="[
            { name: '直接访问', value: 335 },
            { name: '邮件营销', value: 310 },
            { name: '联盟广告', value: 234 },
            { name: '视频广告', value: 135 },
            { name: '搜索引擎', value: 186 }
          ]"
        />`
      },
      {
        title: '环形图（甜甜圈图）',
        data: [
          { name: '直接访问', value: 335 },
          { name: '邮件营销', value: 310 },
          { name: '联盟广告', value: 234 },
          { name: '视频广告', value: 135 }
        ],
        radius: ['40%', '70%'],
        'center-text': '总销售额',
        copy: `
        <APieChart
          :data="pieData"
          :radius="['40%', '70%']"
          center-text="总销售额"
        />`
      },
      {
        title: '带图例的饼图',
        data: [
          { name: '直接访问', value: 335 },
          { name: '邮件营销', value: 310 },
          { name: '联盟广告', value: 234 },
          { name: '视频广告', value: 135 }
        ],
        'show-legend': true,
        'legend-position': 'right',
        copy: `
        <APieChart
          :data="pieData"
          :show-legend="true"
          legend-position="right"
        />`
      },
      {
        title: '带标签的饼图',
        data: [
          { name: '直接访问', value: 335 },
          { name: '邮件营销', value: 310 },
          { name: '联盟广告', value: 234 },
          { name: '视频广告', value: 135 }
        ],
        'show-label': true,
        'border-radius': 5,
        copy: `
        <APieChart
          :data="pieData"
          :show-label="true"
          :border-radius="5"
        />`
      },
      {
        title: '自定义颜色',
        data: [
          { name: '直接访问', value: 335 },
          { name: '邮件营销', value: 310 },
          { name: '联盟广告', value: 234 },
          { name: '视频广告', value: 135 }
        ],
        colors: ['#5470c6', '#91cc75', '#fac858', '#ee6666'],
        copy: `
        <APieChart
          :data="pieData"
          :colors="['#5470c6', '#91cc75', '#fac858', '#ee6666']"
        />`
      }
    ]
  },
  'bar-bidirectional': {
    type: 'bar-bidirectional',
    data: [
      {
        title: '基本用法 - 正负向数据对比',
        'positive-data': [20, 25, 30, 18, 22, 28, 35],
        'negative-data': [15, 20, 25, 12, 18, 24, 30],
        'x-axis-data': ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
        'positive-name': '收入',
        'negative-name': '支出',
        copy: `
        <ABarBidirectionalChart
          :positive-data="[20, 25, 30, 18, 22, 28, 35]"
          :negative-data="[15, 20, 25, 12, 18, 24, 30]"
          :x-axis-data="['周一', '周二', '周三', '周四', '周五', '周六', '周日']"
          positive-name="收入"
          negative-name="支出"
        />`
      },
      {
        title: '带图例显示',
        'positive-data': [30, 35, 40, 28, 32, 38, 45],
        'negative-data': [25, 30, 35, 22, 28, 34, 40],
        'x-axis-data': ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
        'positive-name': '男性用户',
        'negative-name': '女性用户',
        'show-legend': true,
        copy: `
        <ABarBidirectionalChart
          :positive-data="positiveData"
          :negative-data="negativeData"
          :x-axis-data="categories"
          positive-name="男性用户"
          negative-name="女性用户"
          :show-legend="true"
        />`
      },
      {
        title: '显示数据标签',
        'positive-data': [20, 25, 30, 18, 22, 28, 35],
        'negative-data': [15, 20, 25, 12, 18, 24, 30],
        'x-axis-data': ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
        'positive-name': '收入',
        'negative-name': '支出',
        'show-data-label': true,
        'y-axis-min': -50,
        'y-axis-max': 50,
        copy: `
        <ABarBidirectionalChart
          :positive-data="positiveData"
          :negative-data="negativeData"
          :x-axis-data="categories"
          :show-data-label="true"
          :y-axis-min="-50"
          :y-axis-max="50"
        />`
      },
      {
        title: '自定义样式',
        'positive-data': [20, 25, 30, 18, 22, 28, 35],
        'negative-data': [15, 20, 25, 12, 18, 24, 30],
        'x-axis-data': ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
        'positive-name': '收入',
        'negative-name': '支出',
        'bar-width': 20,
        'positive-border-radius': [8, 8, 0, 0],
        'negative-border-radius': [0, 0, 8, 8],
        colors: ['#5470c6', '#91cc75'],
        copy: `
        <ABarBidirectionalChart
          :positive-data="positiveData"
          :negative-data="negativeData"
          :x-axis-data="categories"
          :bar-width="20"
          :positive-border-radius="[8, 8, 0, 0]"
          :negative-border-radius="[0, 0, 8, 8]"
          :colors="['#5470c6', '#91cc75']"
        />`
      }
    ]
  },
  'bar-horizontal': {
    type: 'bar-horizontal',
    data: [
      {
        title: '基本用法 - 简单数值数组',
        data: [120, 200, 150, 80, 70, 110, 130],
        'y-axis-data': ['产品A', '产品B', '产品C', '产品D', '产品E', '产品F', '产品G'],
        copy: `
        <ABarHorizontalChart
          :data="[120, 200, 150, 80, 70, 110, 130]"
          :y-axis-data="['产品A', '产品B', '产品C', '产品D', '产品E', '产品F', '产品G']"
        />`
      },
      {
        title: '多系列数据 - 对比展示',
        data: [
          { name: '销售额', data: [120, 200, 150, 80, 70, 110, 130] },
          { name: '目标值', data: [100, 180, 140, 90, 80, 120, 140] }
        ],
        'y-axis-data': ['产品A', '产品B', '产品C', '产品D', '产品E', '产品F', '产品G'],
        'show-legend': true,
        copy: `
        <ABarHorizontalChart
          :data="[
            { name: '销售额', data: [120, 200, 150, 80, 70, 110, 130] },
            { name: '目标值', data: [100, 180, 140, 90, 80, 120, 140] }
          ]"
          :y-axis-data="['产品A', '产品B', '产品C', '产品D', '产品E', '产品F', '产品G']"
          :show-legend="true"
        />`
      },
      {
        title: '堆叠水平柱状图',
        data: [
          { name: '数据1', type: 'bar', stack: 'stack1', data: [120, 132, 101, 134, 90, 230, 210] },
          { name: '数据2', type: 'bar', stack: 'stack1', data: [60, 72, 71, 74, 190, 130, 110] },
          { name: '数据3', type: 'bar', stack: 'stack1', data: [62, 82, 91, 84, 109, 110, 120] }
        ],
        'y-axis-data': ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
        stack: true,
        'show-legend': true,
        copy: `
        <ABarHorizontalChart
          :data="stackData"
          :y-axis-data="categories"
          :stack="true"
          :show-legend="true"
        />`
      },
      {
        title: '自定义样式',
        data: [120, 200, 150, 80, 70, 110, 130],
        'y-axis-data': ['产品A', '产品B', '产品C', '产品D', '产品E', '产品F', '产品G'],
        'bar-width': '50%',
        colors: ['#5470c6', '#91cc75', '#fac858'],
        copy: `
        <ABarHorizontalChart
          :data="data"
          :y-axis-data="categories"
          bar-width="50%"
          :colors="['#5470c6', '#91cc75', '#fac858']"
        />`
      }
    ]
  },
  candlestick: {
    type: 'candlestick',
    data: [
      {
        title: '基本用法 - 股票K线图',
        data: [
          { time: '2023-01-01', open: 100, close: 102, high: 105, low: 99 },
          { time: '2023-01-02', open: 102, close: 98, high: 103, low: 97 },
          { time: '2023-01-03', open: 98, close: 104, high: 106, low: 96 },
          { time: '2023-01-04', open: 104, close: 107, high: 109, low: 103 },
          { time: '2023-01-05', open: 107, close: 105, high: 110, low: 104 }
        ],
        copy: `
        <ACandlestickChart
          :data="[
            { time: '2023-01-01', open: 100, close: 102, high: 105, low: 99 },
            { time: '2023-01-02', open: 102, close: 98, high: 103, low: 97 },
            { time: '2023-01-03', open: 98, close: 104, high: 106, low: 96 },
            { time: '2023-01-04', open: 104, close: 107, high: 109, low: 103 }
          ]"
        />`
      },
      {
        title: '带数据缩放功能',
        data: [
          { time: '2023-01-01', open: 100, close: 102, high: 105, low: 99 },
          { time: '2023-01-02', open: 102, close: 98, high: 103, low: 97 },
          { time: '2023-01-03', open: 98, close: 104, high: 106, low: 96 },
          { time: '2023-01-04', open: 104, close: 107, high: 109, low: 103 },
          { time: '2023-01-05', open: 107, close: 105, high: 110, low: 104 },
          { time: '2023-01-06', open: 105, close: 108, high: 112, low: 103 }
        ],
        'show-data-zoom': true,
        'data-zoom-start': 20,
        'data-zoom-end': 80,
        copy: `
        <ACandlestickChart
          :data="klineData"
          :show-data-zoom="true"
          :data-zoom-start="20"
          :data-zoom-end="80"
        />`
      },
      {
        title: '自定义颜色',
        data: [
          { time: '2023-01-01', open: 100, close: 102, high: 105, low: 99 },
          { time: '2023-01-02', open: 102, close: 98, high: 103, low: 97 },
          { time: '2023-01-03', open: 98, close: 104, high: 106, low: 96 },
          { time: '2023-01-04', open: 104, close: 107, high: 109, low: 103 }
        ],
        colors: ['#ef5350', '#26a69a'],
        'show-data-zoom': true,
        copy: `
        <ACandlestickChart
          :data="klineData"
          :colors="['#ef5350', '#26a69a']"
          :show-data-zoom="true"
        />`
      }
    ]
  },
  map: {
    type: 'map',
    data: [
      {
        title: '基本用法 - 中国地图',
        'map-data': [
          { name: '北京', value: 100, adcode: '110000', level: 'province' },
          { name: '上海', value: 200, adcode: '310000', level: 'province' },
          { name: '广东', value: 300, adcode: '440000', level: 'province' },
          { name: '浙江', value: 250, adcode: '330000', level: 'province' }
        ],
        copy: `
        <AMapChart
          :map-data="[
            { name: '北京', value: 100, adcode: '110000', level: 'province' },
            { name: '上海', value: 200, adcode: '310000', level: 'province' },
            { name: '广东', value: 300, adcode: '440000', level: 'province' }
          ]"
        />`
      },
      {
        title: '带散点标记',
        'map-data': [
          { name: '北京', value: 100, adcode: '110000', level: 'province' },
          { name: '上海', value: 200, adcode: '310000', level: 'province' },
          { name: '广东', value: 300, adcode: '440000', level: 'province' }
        ],
        'show-scatter': true,
        'show-labels': true,
        copy: `
        <AMapChart
          :map-data="provinceData"
          :show-scatter="true"
          :show-labels="true"
        />`
      },
      {
        title: '隐藏标签的简洁地图',
        'map-data': [
          { name: '北京', value: 100, adcode: '110000', level: 'province' },
          { name: '上海', value: 200, adcode: '310000', level: 'province' },
          { name: '广东', value: 300, adcode: '440000', level: 'province' }
        ],
        'show-labels': false,
        'show-scatter': false,
        copy: `
        <AMapChart
          :map-data="mapData"
          :show-labels="false"
          :show-scatter="false"
        />`
      }
    ]
  },
  radar: {
    type: 'radar',
    data: [
      {
        title: '基本用法 - 单个数据系列',
        indicator: [
          { name: '销售', max: 100 },
          { name: '管理', max: 100 },
          { name: '信息技术', max: 100 },
          { name: '客服', max: 100 },
          { name: '研发', max: 100 },
          { name: '市场', max: 100 }
        ],
        data: [{ name: '预算分配', value: [43, 72, 65, 53, 99, 70] }],
        copy: `
        <ARadarChart
          :indicator="[
            { name: '销售', max: 100 },
            { name: '管理', max: 100 },
            { name: '信息技术', max: 100 },
            { name: '客服', max: 100 },
            { name: '研发', max: 100 },
            { name: '市场', max: 100 }
          ]"
          :data="[
            { name: '预算分配', value: [43, 72, 65, 53, 99, 70] }
          ]"
        />`
      },
      {
        title: '多个数据系列对比',
        indicator: [
          { name: '语文', max: 100 },
          { name: '数学', max: 100 },
          { name: '英语', max: 100 },
          { name: '物理', max: 100 },
          { name: '化学', max: 100 },
          { name: '生物', max: 100 }
        ],
        data: [
          { name: '张三', value: [80, 90, 85, 75, 95, 88] },
          { name: '李四', value: [70, 85, 90, 80, 85, 92] },
          { name: '王五', value: [90, 80, 75, 85, 80, 85] }
        ],
        'show-legend': true,
        copy: `
        <ARadarChart
          :indicator="indicators"
          :data="[
            { name: '张三', value: [80, 90, 85, 75, 95, 88] },
            { name: '李四', value: [70, 85, 90, 80, 85, 92] },
            { name: '王五', value: [90, 80, 75, 85, 80, 85] }
          ]"
          :show-legend="true"
        />`
      },
      {
        title: '自定义颜色和样式',
        indicator: [
          { name: '销售', max: 100 },
          { name: '管理', max: 100 },
          { name: '信息技术', max: 100 },
          { name: '客服', max: 100 },
          { name: '研发', max: 100 }
        ],
        data: [
          { name: '部门A', value: [80, 90, 85, 75, 95] },
          { name: '部门B', value: [70, 85, 90, 80, 85] }
        ],
        colors: ['#5470c6', '#91cc75', '#fac858'],
        'show-legend': true,
        'legend-position': 'bottom',
        copy: `
        <ARadarChart
          :indicator="indicators"
          :data="data"
          :colors="['#5470c6', '#91cc75', '#fac858']"
          :show-legend="true"
          legend-position="bottom"
        />`
      }
    ]
  },
  scatter: {
    type: 'scatter',
    data: [
      {
        title: '基本用法 - 简单散点分布',
        data: [
          { value: [10, 20] },
          { value: [15, 25] },
          { value: [20, 18] },
          { value: [25, 30] },
          { value: [30, 22] },
          { value: [35, 28] },
          { value: [40, 35] }
        ],
        copy: `
        <AScatterChart
          :data="[
            { value: [10, 20] },
            { value: [15, 25] },
            { value: [20, 18] },
            { value: [25, 30] },
            { value: [30, 22] }
          ]"
        />`
      },
      {
        title: '带名称的散点',
        data: [
          { value: [10, 20], name: '数据点1' },
          { value: [15, 25], name: '数据点2' },
          { value: [20, 18], name: '数据点3' },
          { value: [25, 30], name: '数据点4' },
          { value: [30, 22], name: '数据点5' }
        ],
        copy: `
        <AScatterChart
          :data="[
            { value: [10, 20], name: '数据点1' },
            { value: [15, 25], name: '数据点2' },
            { value: [20, 18], name: '数据点3' },
            { value: [25, 30], name: '数据点4' },
            { value: [30, 22], name: '数据点5' }
          ]"
        />`
      },
      {
        title: '自定义样式',
        data: [{ value: [10, 20] }, { value: [15, 25] }, { value: [20, 18] }, { value: [25, 30] }, { value: [30, 22] }],
        'symbol-size': 20,
        colors: ['#5470c6'],
        'show-axis-line': true,
        'show-split-line': true,
        copy: `
        <AScatterChart
          :data="scatterData"
          :symbol-size="20"
          :colors="['#5470c6']"
          :show-axis-line="true"
          :show-split-line="true"
        />`
      }
    ]
  }
}

const chartShowType = ref<string>('基础示例')

const chartShowData = ref<{ type: string; data: any[] }>({
  type: '柱状图',
  data: []
})

const chartButtonGroupData = ref<any[]>([
  { name: '柱状图', type: 'bar' },
  { name: '折线图', type: 'line' },
  { name: '饼图', type: 'pie' },
  { name: '双向柱状图', type: 'bar-bidirectional' },
  { name: '横向柱状图', type: 'bar-horizontal' },
  { name: 'K线图', type: 'candlestick' },
  { name: '地图', type: 'map' },
  { name: '雷达图', type: 'radar' },
  { name: '散点图', type: 'scatter' }
])

const handleChartShowData = (type: string) => {
  chartShowType.value = type
  chartShowData.value = chartShowConfigData.bar
}

/* 点击图表类型按钮 */
const handleChartTypeButton = (value: any) => {
  console.log(value)
  switch (value.type) {
    case 'bar':
      chartShowData.value = chartShowConfigData.bar
      break
    case 'line':
      chartShowData.value = chartShowConfigData.line
      break
    case 'pie':
      chartShowData.value = chartShowConfigData.pie
      break
    case 'bar-bidirectional':
      chartShowData.value = chartShowConfigData['bar-bidirectional']
      break
    case 'bar-horizontal':
      chartShowData.value = chartShowConfigData['bar-horizontal']
      break
    case 'candlestick':
      chartShowData.value = chartShowConfigData.candlestick
      break
    case 'map':
      chartShowData.value = chartShowConfigData.map
      break
    case 'radar':
      chartShowData.value = chartShowConfigData.radar
      break
    case 'scatter':
      chartShowData.value = chartShowConfigData.scatter
      break
    default:
      chartShowData.value = chartShowConfigData.bar
      break
  }
}

/**
 * 处理折线图点击事件
 */
const handleLineChartClick = (params: any) => {
  console.log('折线图点击事件:', params)
  if (params.componentType === 'series') {
    const message = `点击了: ${params.name || '数据点'} - 值: ${params.value || params.data}`
    copy(message, '已复制图表数据!')
  }
}
</script>

<style scoped lang="scss"></style>
