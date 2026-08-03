<!-- 数据大屏 -->
<template>
  <div class="datav-container">
    <!-- 页面头部 -->
    <div class="flex justify-between items-center mb-4">
      <h3>DataV 大屏装饰器示例</h3>
      <el-button type="primary" @click="openFullScreen">
        <Icon code="monitor" class="mr-2" />
        打开大屏预览
      </el-button>
    </div>

    <el-divider />

    <!-- 统一示例展示 -->
    <div class="examples-container">
      <!-- 1. BorderBox 边框装饰器 -->
      <h2 class="section-title">1. BorderBox 边框装饰器</h2>
      <div class="examples-grid">
        <div class="example-item" v-for="item in borderBoxExamples" :key="item.name">
          <div class="example-header">
            <span class="example-title">{{ item.name }}</span>
            <div class="copy-btn" @click="copy(item.code, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <component :is="item.component" v-bind="item.props" class="datav-demo-box">
            <div class="demo-content">{{ item.name }}</div>
          </component>
        </div>
      </div>

      <!-- 2. Decoration 装饰组件 -->
      <h2 class="section-title">2. Decoration 装饰组件</h2>
      <div class="examples-grid">
        <div class="example-item" v-for="item in decorationExamples" :key="item.name">
          <div class="example-header">
            <span class="example-title">{{ item.name }}</span>
            <div class="copy-btn" @click="copy(item.code, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <div class="decoration-demo">
            <component :is="item.component" :style="item.style" v-bind="item.props">
              <div v-if="item.content" class="text-white mx-2">{{ item.content }}</div>
            </component>
          </div>
        </div>
      </div>

      <!-- 3. 综合应用示例 -->
      <h2 class="section-title">3. 综合应用示例</h2>
      <div class="comprehensive-example">
        <div class="example-header">
          <span class="example-title">边框 + 装饰 + 图表组合</span>
          <div class="copy-btn" @click="copy(codeExamples.comprehensive, '复制成功!')">
            <Icon code="copy" size="md" />
          </div>
        </div>
        <div class="comprehensive-demo">
          <border-box-8 class="demo-panel">
            <div class="panel-header">
              <decoration-7 :color="['#4fd2dd', '#235fa7']"><div class="text-white mx-2">销售数据统计</div></decoration-7>
            </div>
            <AChart :config="demoChartOption" height="300px" @chartReady="handleChartReady" @chartClick="handleChartClick" />
          </border-box-8>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useClipboard } from '@vueuse/core'
import { ElMessage } from 'element-plus'
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
import { showMsg, showMsgSuccess } from '@/utils/modal'

// BorderBox 示例配置
const borderBoxExamples = [
  {
    name: 'BorderBox1',
    component: BorderBox1,
    props: {},
    code: `<border-box-1>
  <div>内容区域</div>
</border-box-1>`
  },
  {
    name: 'BorderBox5',
    component: BorderBox5,
    props: {},
    code: `<border-box-5>
  <div>内容区域</div>
</border-box-5>`
  },
  {
    name: 'BorderBox6',
    component: BorderBox6,
    props: {},
    code: `<border-box-6>
  <div>内容区域</div>
</border-box-6>`
  },
  {
    name: 'BorderBox8',
    component: BorderBox8,
    props: { dur: 3 },
    code: `<border-box-8 :dur="3">
  <div>内容区域</div>
</border-box-8>`
  },
  {
    name: 'BorderBox9',
    component: BorderBox9,
    props: {},
    code: `<border-box-9>
  <div>内容区域</div>
</border-box-9>`
  },
  {
    name: 'BorderBox10',
    component: BorderBox10,
    props: {},
    code: `<border-box-10>
  <div>内容区域</div>
</border-box-10>`
  },
  {
    name: 'BorderBox11',
    component: BorderBox11,
    props: { title: '标题示例', titleWidth: 250 },
    code: `<border-box-11 title="标题" :title-width="250">
  <div>内容区域</div>
</border-box-11>`
  },
  {
    name: 'BorderBox12',
    component: BorderBox12,
    props: {},
    code: `<border-box-12>
  <div>内容区域</div>
</border-box-12>`
  },
  {
    name: 'BorderBox13',
    component: BorderBox13,
    props: {},
    code: `<border-box-13>
  <div>内容区域</div>
</border-box-13>`
  }
]

// Decoration 示例配置
const decorationExamples = [
  {
    name: 'Decoration1',
    component: Decoration1,
    style: { width: '100%', height: '30px' },
    code: `<decoration-1 />`
  },
  {
    name: 'Decoration2',
    component: Decoration2,
    style: { width: '100%', height: '6px' },
    code: `<decoration-2 />`
  },
  {
    name: 'Decoration3',
    component: Decoration3,
    style: { width: '200px', height: '30px' },
    code: `<decoration-3 />`
  },
  {
    name: 'Decoration4',
    component: Decoration4,
    style: { width: '100%', height: '60px' },
    code: `<decoration-4 />`
  },
  {
    name: 'Decoration5',
    component: Decoration5,
    style: { width: '300px', height: '40px' },
    code: `<decoration-5 />`
  },
  {
    name: 'Decoration6',
    component: Decoration6,
    style: { width: '100%', height: '30px' },
    code: `<decoration-6 />`
  },
  {
    name: 'Decoration7',
    component: Decoration7,
    style: { width: '200px', height: '30px' },
    props: { color: ['#4fd2dd', '#235fa7'] }, // 设置醒目的颜色
    content: '标题文字',
    code: `<decoration-7 style="width: 200px; height: 30px;" :color="['#4fd2dd', '#235fa7']">
  <div class="text-white mx-2">标题文字</div>
</decoration-7>`
  },
  {
    name: 'Decoration8',
    component: Decoration8,
    style: { width: '200px', height: '50px' },
    code: `<decoration-8 />`
  }
]

// 代码示例
const codeExamples = {
  comprehensive: `<template>
  <border-box-8 class="panel">
    <div class="panel-header">
      <decoration-7>销售数据统计</decoration-7>
    </div>
    <AChart :config="chartOption" height="300px" />
  </border-box-8>
</template>

<script setup lang="ts">
import { BorderBox8, Decoration7 } from '@kjgl77/datav-vue3'

const chartOption = computed(() => ({
  xAxis: {
    type: 'category',
    data: ['周一', '周二', '周三', '周四', '周五']
  },
  yAxis: {
    type: 'value'
  },
  series: [{
    data: [120, 200, 150, 80, 70],
    type: 'bar'
  }]
}))
<\/script>`
}

// 演示图表配置
const demoChartOption = computed<any>(() => ({
  grid: {
    left: '10%',
    right: '5%',
    top: '15%',
    bottom: '10%'
  },
  xAxis: {
    type: 'category',
    data: ['周一', '周二', '周三', '周四', '周五']
  },
  yAxis: {
    type: 'value'
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
      }
    }
  ]
}))

// 复制功能
const { copy: clipboardCopy } = useClipboard()
const copy = (text: string, message: string) => {
  clipboardCopy(text)
  showMsgSuccess(message)
}

// 打开全屏大屏
const openFullScreen = () => {
  window.open('/datav/fullscreen', '_blank')
}

// 图表准备就绪事件
const handleChartReady = (chart: any) => {
  // 图表初始化完成,可以在这里进行额外配置
}

// 图表点击事件处理
const handleChartClick = (params: any) => {
  showMsg(`点击了: ${params.name} - 值: ${params.value}`, 'success')
}
</script>

<style scoped lang="scss">
.datav-container {
  padding: 20px;
  background: var(--bg-level-0);
  min-height: calc(100vh - 84px);
}

// 示例容器
.examples-container {
  .section-title {
    font-size: 18px;
    font-weight: bold;
    margin: 30px 0 20px;
    padding-left: 12px;
    border-left: 4px solid var(--el-color-primary);
  }

  .examples-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 20px;
    margin-bottom: 30px;
  }

  .example-item {
    background: var(--bg-level-1);
    border-radius: 8px;
    padding: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    transition: all 0.3s;
    border: 1px solid var(--el-border-color);

    &:hover {
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
      transform: translateY(-2px);
    }
  }

  .example-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    padding-bottom: 12px;
    border-bottom: 1px solid var(--el-border-color);

    .example-title {
      font-weight: 500;
      color: var(--el-text-color-primary);
    }

    .copy-btn {
      cursor: pointer;
      color: #909399;
      transition: color 0.3s;

      &:hover {
        color: var(--el-color-primary);
      }
    }
  }

  .datav-demo-box {
    height: 200px;
    background: linear-gradient(135deg, #0b1c2c 0%, #1a3a52 100%);

    .demo-content {
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #4fd2dd;
      font-size: 18px;
      font-weight: bold;
    }
  }

  .decoration-demo {
    height: 100px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #0b1c2c 0%, #1a3a52 100%);
    border-radius: 4px;
    padding: 20px;
  }

  .comprehensive-example {
    background: var(--bg-level-1);
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    margin-bottom: 30px;
    border: 1px solid var(--el-border-color);

    .comprehensive-demo {
      margin-top: 16px;

      .demo-panel {
        height: 400px;
        background: linear-gradient(135deg, #0b1c2c 0%, #1a3a52 100%);
        padding: 20px;

        .panel-header {
          margin-bottom: 15px;

          :deep(.dv-decoration-7) {
            width: 100%;
            height: 30px;
          }
        }
      }
    }
  }
}

// 响应式
@media (max-width: 768px) {
  .examples-grid {
    grid-template-columns: 1fr !important;
  }
}
</style>
