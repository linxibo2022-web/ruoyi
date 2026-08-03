<!-- 卡片模板 -->
<template>
  <div class="card-container">
    <!-- 卡片示例头部 -->
    <div class="flex justify-between items-center">
      <h3>卡片示例</h3>
    </div>

    <el-divider />

    <!-- 统一示例展示 -->
    <div class="examples-container">
      <!-- 1. 统计卡片 -->
      <h2 class="section-title">1. 统计卡片 AStatsCard</h2>
      <div class="examples-grid">
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">基本用法</span>
            <div class="copy-btn" @click="copy(codeExamples.stats1, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <AStatsCard title="总用户数" :value="8520" description="较昨日" icon="user" />
        </div>

        <div class="example-item">
          <div class="example-header">
            <span class="example-title">带趋势指标</span>
            <div class="copy-btn" @click="copy(codeExamples.stats2, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <AStatsCard title="活跃用户" :value="1234" icon="hot" :trend="{ value: 12.5, isUp: true }" />
        </div>

        <div class="example-item">
          <div class="example-header">
            <span class="example-title">带进度条</span>
            <div class="copy-btn" @click="copy(codeExamples.stats3, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <AStatsCard title="月度目标" :value="85000" :target="100000" unit="元" icon="goal" :show-progress="true" />
        </div>

        <div class="example-item">
          <div class="example-header">
            <span class="example-title">自定义颜色</span>
            <div class="copy-btn" @click="copy(codeExamples.stats4, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <AStatsCard title="总收入" :value="98000" unit="元" icon="money" icon-color="#f56c6c" icon-bg-color="rgba(245, 108, 108, 0.1)" />
        </div>
      </div>

      <!-- 2. 柱状图卡片 -->
      <h2 class="section-title">2. 柱状图卡片 ABarChartCard</h2>
      <div class="examples-grid">
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">基本用法</span>
            <div class="copy-btn" @click="copy(codeExamples.bar1, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <ABarChartCard
            title="本周销售趋势"
            :data="[120, 200, 150, 80, 70, 110, 130]"
            :x-axis-data="['周一', '周二', '周三', '周四', '周五', '周六', '周日']"
          />
        </div>

        <div class="example-item">
          <div class="example-header">
            <span class="example-title">带指标显示</span>
            <div class="copy-btn" @click="copy(codeExamples.bar2, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <ABarChartCard
            title="月度销售额"
            :value="125000"
            unit="元"
            :trend="{ value: 12.5, isUp: true }"
            description="较上月"
            :data="[120, 200, 150, 180]"
            :x-axis-data="['1周', '2周', '3周', '4周']"
          />
        </div>

        <div class="example-item">
          <div class="example-header">
            <span class="example-title">多系列对比</span>
            <div class="copy-btn" @click="copy(codeExamples.bar3, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <ABarChartCard
            title="季度对比"
            :data="[
              { name: 'Q1', data: [150, 180, 200] },
              { name: 'Q2', data: [180, 220, 190] }
            ]"
            :x-axis-data="['1月', '2月', '3月']"
            :show-legend="true"
          />
        </div>

        <div class="example-item">
          <div class="example-header">
            <span class="example-title">年度增长</span>
            <div class="copy-btn" @click="copy(codeExamples.bar4, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <ABarChartCard
            title="年度销售额"
            :value="986000"
            unit="元"
            :trend="{ value: 28.3, isUp: true }"
            description="同比增长"
            :data="[65, 80, 92, 110, 95, 105, 98, 115, 120, 105, 110, 89]"
            :x-axis-data="['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']"
          />
        </div>
      </div>

      <!-- 3. 折线图卡片 -->
      <h2 class="section-title">3. 折线图卡片 ALineChartCard</h2>
      <div class="examples-grid">
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">基本用法</span>
            <div class="copy-btn" @click="copy(codeExamples.line1, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <ALineChartCard
            title="用户增长趋势"
            :data="[120, 200, 150, 180, 220, 190, 240]"
            :x-axis-data="['周一', '周二', '周三', '周四', '周五', '周六', '周日']"
          />
        </div>

        <div class="example-item">
          <div class="example-header">
            <span class="example-title">多系列对比</span>
            <div class="copy-btn" @click="copy(codeExamples.line2, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <ALineChartCard
            title="流量对比"
            :data="[
              { name: '本月', data: [120, 200, 150, 180] },
              { name: '上月', data: [100, 180, 140, 160] }
            ]"
            :x-axis-data="['第1周', '第2周', '第3周', '第4周']"
            :show-legend="true"
          />
        </div>

        <div class="example-item">
          <div class="example-header">
            <span class="example-title">带指标显示</span>
            <div class="copy-btn" @click="copy(codeExamples.line3, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <ALineChartCard
            title="访问量统计"
            :value="52800"
            description="本周总计"
            :data="[6200, 7800, 7500, 8200, 8800, 7300, 6700]"
            :x-axis-data="['周一', '周二', '周三', '周四', '周五', '周六', '周日']"
          />
        </div>

        <div class="example-item">
          <div class="example-header">
            <span class="example-title">多维度分析</span>
            <div class="copy-btn" @click="copy(codeExamples.line4, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <ALineChartCard
            title="业绩趋势分析"
            :data="[
              { name: '团队A', data: [320, 332, 301, 334, 390] },
              { name: '团队B', data: [220, 282, 191, 234, 290] },
              { name: '团队C', data: [150, 232, 201, 154, 190] }
            ]"
            :x-axis-data="['第1季度', '第2季度', '第3季度', '第4季度', '第5季度']"
            :show-legend="true"
          />
        </div>
      </div>

      <!-- 4. 饼图卡片 -->
      <h2 class="section-title">4. 饼图卡片 APieChartCard</h2>
      <div class="examples-grid">
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">基本用法</span>
            <div class="copy-btn" @click="copy(codeExamples.pie1, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <APieChartCard
            title="销售分布"
            :data="[
              { name: '直接访问', value: 335 },
              { name: '邮件营销', value: 310 },
              { name: '联盟广告', value: 234 },
              { name: '视频广告', value: 135 }
            ]"
          />
        </div>

        <div class="example-item">
          <div class="example-header">
            <span class="example-title">环形图</span>
            <div class="copy-btn" @click="copy(codeExamples.pie2, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <APieChartCard
            title="用户分布"
            :data="[
              { name: 'VIP用户', value: 450 },
              { name: '普通用户', value: 800 }
            ]"
            chart-type="ring"
          />
        </div>

        <div class="example-item">
          <div class="example-header">
            <span class="example-title">产品占比</span>
            <div class="copy-btn" @click="copy(codeExamples.pie3, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <APieChartCard
            title="产品销售占比"
            :data="[
              { name: '电子产品', value: 520 },
              { name: '服装鞋帽', value: 380 },
              { name: '食品饮料', value: 280 },
              { name: '家居用品', value: 220 },
              { name: '其他', value: 100 }
            ]"
          />
        </div>

        <div class="example-item">
          <div class="example-header">
            <span class="example-title">区域分布环形图</span>
            <div class="copy-btn" @click="copy(codeExamples.pie4, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <APieChartCard
            title="区域销售分布"
            :data="[
              { name: '华东地区', value: 680 },
              { name: '华南地区', value: 520 },
              { name: '华北地区', value: 450 },
              { name: '西南地区', value: 350 }
            ]"
            chart-type="ring"
          />
        </div>
      </div>

      <!-- 5. 数据列表卡片 -->
      <h2 class="section-title">5. 数据列表卡片 ADataListCard</h2>
      <div class="examples-grid">
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">基本用法</span>
            <div class="copy-btn" @click="copy(codeExamples.dataList1, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <ADataListCard
            title="待办事项"
            :list="[
              { title: '完成项目报告', status: '进行中', time: '2小时前', icon: 'file' },
              { title: '客户会议', status: '待处理', time: '今天 14:00', icon: 'calendar' }
            ]"
          />
        </div>

        <div class="example-item">
          <div class="example-header">
            <span class="example-title">自定义图标颜色</span>
            <div class="copy-btn" @click="copy(codeExamples.dataList2, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <ADataListCard
            title="最近活动"
            :list="[
              { title: '新用户注册', status: '成功', time: '5分钟前', icon: 'user', iconColor: '#67c23a' },
              { title: '订单支付', status: '完成', time: '10分钟前', icon: 'money', iconColor: '#409eff' }
            ]"
          />
        </div>

        <div class="example-item">
          <div class="example-header">
            <span class="example-title">系统通知列表</span>
            <div class="copy-btn" @click="copy(codeExamples.dataList3, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <ADataListCard
            title="系统通知"
            :list="[
              { title: '系统升级通知', status: '重要', time: '1小时前', icon: 'activity', iconColor: '#e6a23c' },
              { title: '维护公告', status: '已读', time: '3小时前', icon: 'activity', iconColor: '#909399' },
              { title: '功能更新', status: '未读', time: '昨天', icon: 'activity', iconColor: '#409eff' }
            ]"
          />
        </div>

        <div class="example-item">
          <div class="example-header">
            <span class="example-title">项目任务</span>
            <div class="copy-btn" @click="copy(codeExamples.dataList4, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <ADataListCard
            title="项目任务列表"
            :list="[
              { title: '前端开发', status: '进行中', time: '2天前', icon: 'code', iconColor: '#409eff' },
              { title: '后端接口', status: '已完成', time: '3天前', icon: 'server', iconColor: '#67c23a' },
              { title: '测试验收', status: '待开始', time: '明天', icon: 'test', iconColor: '#909399' }
            ]"
          />
        </div>
      </div>

      <!-- 6. 雷达图卡片 -->
      <h2 class="section-title">6. 雷达图卡片 ARadarChartCard</h2>
      <div class="examples-grid">
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">能力评估</span>
            <div class="copy-btn" @click="copy(codeExamples.radar1, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <ARadarChartCard
            title="能力评估"
            :indicator="[
              { name: '销售', max: 100 },
              { name: '管理', max: 100 },
              { name: '技术', max: 100 },
              { name: '客服', max: 100 },
              { name: '研发', max: 100 }
            ]"
            :data="[{ name: '当前', value: [80, 90, 85, 75, 95] }]"
          />
        </div>
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">团队对比</span>
            <div class="copy-btn" @click="copy(codeExamples.radar2, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <ARadarChartCard
            title="团队对比"
            :indicator="[
              { name: '执行力', max: 100 },
              { name: '创新力', max: 100 },
              { name: '协作力', max: 100 },
              { name: '学习力', max: 100 }
            ]"
            :data="[
              { name: 'A组', value: [90, 85, 88, 92] },
              { name: 'B组', value: [75, 90, 82, 85] }
            ]"
          />
        </div>
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">产品指标</span>
            <div class="copy-btn" @click="copy(codeExamples.radar3, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <ARadarChartCard
            title="产品指标"
            :indicator="[
              { name: '性能', max: 100 },
              { name: '功能', max: 100 },
              { name: '体验', max: 100 },
              { name: '稳定', max: 100 },
              { name: '安全', max: 100 },
              { name: '成本', max: 100 }
            ]"
            :data="[{ name: '产品A', value: [88, 92, 85, 90, 95, 78] }]"
          />
        </div>
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">员工素质</span>
            <div class="copy-btn" @click="copy(codeExamples.radar4, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <ARadarChartCard
            title="员工素质"
            :indicator="[
              { name: '专业能力', max: 100 },
              { name: '沟通能力', max: 100 },
              { name: '团队协作', max: 100 },
              { name: '解决问题', max: 100 },
              { name: '责任心', max: 100 }
            ]"
            :data="[
              { name: '张三', value: [85, 80, 90, 88, 95] },
              { name: '李四', value: [92, 88, 85, 90, 87] }
            ]"
          />
        </div>
      </div>

      <!-- 7. 地图卡片 -->
      <h2 class="section-title">7. 地图卡片 AMapChartCard</h2>
      <div class="examples-grid">
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">全国销售分布</span>
            <div class="copy-btn" @click="copy(codeExamples.map1, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <AMapChartCard
            title="全国销售分布"
            :map-data="[
              { name: '北京', value: 100, adcode: '110000', level: 'province' },
              { name: '上海', value: 200, adcode: '310000', level: 'province' },
              { name: '广东', value: 300, adcode: '440000', level: 'province' }
            ]"
          />
        </div>
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">区域用户分布</span>
            <div class="copy-btn" @click="copy(codeExamples.map2, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <AMapChartCard
            title="区域用户分布"
            :map-data="[
              { name: '浙江', value: 250, adcode: '330000', level: 'province' },
              { name: '江苏', value: 180, adcode: '320000', level: 'province' },
              { name: '四川', value: 150, adcode: '510000', level: 'province' },
              { name: '山东', value: 200, adcode: '370000', level: 'province' }
            ]"
          />
        </div>
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">订单热力分布</span>
            <div class="copy-btn" @click="copy(codeExamples.map3, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <AMapChartCard
            title="订单热力分布"
            :map-data="[
              { name: '河北', value: 120, adcode: '130000', level: 'province' },
              { name: '河南', value: 190, adcode: '410000', level: 'province' },
              { name: '湖北', value: 160, adcode: '420000', level: 'province' },
              { name: '湖南', value: 140, adcode: '430000', level: 'province' }
            ]"
          />
        </div>
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">营收区域排名</span>
            <div class="copy-btn" @click="copy(codeExamples.map4, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <AMapChartCard
            title="营收区域排名"
            :map-data="[
              { name: '福建', value: 220, adcode: '350000', level: 'province' },
              { name: '安徽', value: 135, adcode: '340000', level: 'province' },
              { name: '江西', value: 110, adcode: '360000', level: 'province' },
              { name: '辽宁', value: 175, adcode: '210000', level: 'province' }
            ]"
          />
        </div>
      </div>

      <!-- 8. 时间轴列表卡片 -->
      <h2 class="section-title">8. 时间轴列表卡片 ATimelineListCard</h2>
      <div class="examples-grid">
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">项目进度</span>
            <div class="copy-btn" @click="copy(codeExamples.timeline1, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <ATimelineListCard
            title="项目进度"
            :list="[
              { time: '2024-01-15', content: '项目启动', status: 'success' },
              { time: '2024-01-20', content: '需求评审', status: 'info' },
              { time: '2024-01-25', content: '开发中', status: 'warning' }
            ]"
          />
        </div>
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">订单状态</span>
            <div class="copy-btn" @click="copy(codeExamples.timeline2, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <ATimelineListCard
            title="订单状态"
            :list="[
              { time: '2024-10-12 10:20', content: '订单已创建', status: 'info' },
              { time: '2024-10-12 10:25', content: '支付成功', status: 'success' },
              { time: '2024-10-12 11:00', content: '商品出库', status: 'success' }
            ]"
          />
        </div>
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">物流跟踪</span>
            <div class="copy-btn" @click="copy(codeExamples.timeline3, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <ATimelineListCard
            title="物流跟踪"
            :list="[
              { time: '2024-10-12 14:00', content: '快递已揽收', status: 'success' },
              { time: '2024-10-12 18:30', content: '运输中', status: 'info' },
              { time: '2024-10-13 09:15', content: '到达本市', status: 'success' },
              { time: '2024-10-13 12:00', content: '派送中', status: 'warning' }
            ]"
          />
        </div>
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">审批流程</span>
            <div class="copy-btn" @click="copy(codeExamples.timeline4, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <ATimelineListCard
            title="审批流程"
            :list="[
              { time: '2024-10-10 09:00', content: '提交申请', status: 'success' },
              { time: '2024-10-10 14:30', content: '部门审批通过', status: 'success' },
              { time: '2024-10-11 10:00', content: '财务审批中', status: 'warning' }
            ]"
          />
        </div>
      </div>

      <!-- 9. 图片卡片 -->
      <h2 class="section-title">9. 图片卡片 AImageCard</h2>
      <div class="examples-grid">
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">基本用法</span>
            <div class="copy-btn" @click="copy(codeExamples.image1, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <AImageCard image-url="https://picsum.photos/400/300" title="精美风景" description="这是一张精美的风景图片" />
        </div>
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">城市夜景</span>
            <div class="copy-btn" @click="copy(codeExamples.image2, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <AImageCard image-url="https://picsum.photos/400/301" title="城市夜景" description="繁华都市的璀璨夜色" />
        </div>
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">自然景观</span>
            <div class="copy-btn" @click="copy(codeExamples.image3, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <AImageCard image-url="https://picsum.photos/400/302" title="自然景观" description="大自然的鬼斧神工" />
        </div>
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">海滨风光</span>
            <div class="copy-btn" @click="copy(codeExamples.image4, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <AImageCard image-url="https://picsum.photos/400/303" title="海滨风光" description="碧海蓝天,令人心旷神怡" />
        </div>
      </div>

      <!-- 10. 用户信息卡片 -->
      <h2 class="section-title">10. 用户信息卡片 (AUserCard / AProfileCard / ASocialCard)</h2>
      <div class="examples-grid">
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">基础用户卡片</span>
            <div class="copy-btn" @click="copy(codeExamples.user1, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <AUserCard name="张三" avatar="https://i.pravatar.cc/150?img=1" title="产品经理" description="负责产品规划与设计" />
        </div>

        <div class="example-item">
          <div class="example-header">
            <span class="example-title">个人资料卡片</span>
            <div class="copy-btn" @click="copy(codeExamples.user2, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <AProfileCard
            :profile="{
              name: '李四',
              avatar: 'https://i.pravatar.cc/150?img=2',
              title: '技术总监',
              company: '互联网公司',
              email: 'lisi@example.com',
              phone: '138****8888'
            }"
            :stats="[
              { label: '项目', value: 28 },
              { label: '粉丝', value: 1234 }
            ]"
            show-contact
          />
        </div>

        <div class="example-item">
          <div class="example-header">
            <span class="example-title">社交动态卡片</span>
            <div class="copy-btn" @click="copy(codeExamples.user3, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <ASocialCard
            :content="{
              user: '王五',
              avatar: 'https://i.pravatar.cc/100?img=3',
              text: '今天天气真好,适合出去走走!',
              time: '5分钟前'
            }"
          />
        </div>

        <div class="example-item">
          <div class="example-header">
            <span class="example-title">团队成员卡片</span>

            <div class="copy-btn" @click="copy(codeExamples.user4, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <AUserCard name="赵六" avatar="https://i.pravatar.cc/150?img=4" title="前端工程师" description="专注于Vue和React开发" />
        </div>
      </div>

      <!-- 11. 表格卡片 -->
      <h2 class="section-title">11. 表格卡片 ATableCard</h2>
      <div class="examples-grid">
        <div class="example-item" style="grid-column: 1 / -1">
          <div class="example-header">
            <span class="example-title">订单列表示例</span>
            <div class="copy-btn" @click="copy(codeExamples.table1, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <ATableCard
            title="最新订单"
            :columns="[
              { prop: 'id', label: '订单号', width: 120 },
              { prop: 'name', label: '商品名称' },
              { prop: 'price', label: '价格' },
              { prop: 'status', label: '状态' }
            ]"
            :data="[
              { id: 'ORD001', name: '商品A', price: '¥199', status: '已完成' },
              { id: 'ORD002', name: '商品B', price: '¥299', status: '进行中' }
            ]"
          />
        </div>
      </div>

      <!-- 12. 信息卡片 -->
      <h2 class="section-title">12. 信息卡片 AInfoCard</h2>
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="6">
          <div class="example-item">
            <div class="example-header">
              <span class="example-title">成功提示</span>
              <div class="copy-btn" @click="copy(codeExamples.info1, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AInfoCard title="成功提示" type="success">操作已成功完成!</AInfoCard>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="example-item">
            <div class="example-header">
              <span class="example-title">警告信息</span>
              <div class="copy-btn" @click="copy(codeExamples.info2, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AInfoCard title="警告信息" type="warning">请注意检查数据!</AInfoCard>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="example-item">
            <div class="example-header">
              <span class="example-title">错误提示</span>
              <div class="copy-btn" @click="copy(codeExamples.info3, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AInfoCard title="错误提示" type="error">操作失败,请重试!</AInfoCard>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="example-item">
            <div class="example-header">
              <span class="example-title">普通信息</span>
              <div class="copy-btn" @click="copy(codeExamples.info4, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <AInfoCard title="普通信息" type="info">这是一条普通消息</AInfoCard>
          </div>
        </el-col>
      </el-row>

      <!-- 13. 空状态卡片 -->
      <h2 class="section-title">13. 空状态卡片 AEmptyCard</h2>
      <div class="examples-grid">
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">基本用法</span>
            <div class="copy-btn" @click="copy(codeExamples.empty1, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <AEmptyCard title="内容" description="暂无数据" />
        </div>
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">自定义图标</span>
            <div class="copy-btn" @click="copy(codeExamples.empty2, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <AEmptyCard description="暂无订单记录" icon="order" />
        </div>
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">搜索无结果</span>
            <div class="copy-btn" @click="copy(codeExamples.empty3, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <AEmptyCard title="搜索结果" description="未找到相关内容" icon="search" />
        </div>
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">收藏夹为空</span>
            <div class="copy-btn" @click="copy(codeExamples.empty4, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <AEmptyCard title="收藏夹" description="还没有收藏任何内容" icon="activity" />
        </div>
      </div>

      <!-- 14. 天气卡片 -->
      <h2 class="section-title">14. 天气卡片 AWeatherCard</h2>
      <div class="examples-grid">
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">基本用法</span>
            <div class="copy-btn" @click="copy(codeExamples.weather1, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <AWeatherCard :weather="{ city: '北京', temperature: 25, condition: '晴', icon: 'sunny' }" />
        </div>
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">带湿度信息</span>
            <div class="copy-btn" @click="copy(codeExamples.weather2, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <AWeatherCard :weather="{ city: '上海', temperature: 28, condition: '多云', icon: 'cloudy', humidity: 65 }" />
        </div>
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">雨天天气</span>
            <div class="copy-btn" @click="copy(codeExamples.weather3, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <AWeatherCard :weather="{ city: '广州', temperature: 22, condition: '小雨', icon: 'rainy', humidity: 78 }" />
        </div>
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">低温天气</span>
            <div class="copy-btn" @click="copy(codeExamples.weather4, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <AWeatherCard :weather="{ city: '哈尔滨', temperature: -5, condition: '雪', icon: 'snowy', humidity: 45 }" />
        </div>
      </div>

      <!-- 15. 价格卡片 -->
      <h2 class="section-title">15. 价格卡片 APricingCard</h2>
      <div class="examples-grid">
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">基础版</span>
            <div class="copy-btn" @click="copy(codeExamples.pricing1, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <APricingCard plan="基础版" :price="99" period="月" :features="['功能A', '功能B', '功能C']" />
        </div>
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">专业版(推荐)</span>
            <div class="copy-btn" @click="copy(codeExamples.pricing2, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <APricingCard plan="专业版" :price="299" period="月" :features="['所有基础功能', '高级功能A', '高级功能B', '优先支持']" recommended />
        </div>
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">免费版</span>
            <div class="copy-btn" @click="copy(codeExamples.pricing3, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <APricingCard plan="免费版" price="免费" :features="['基础功能', '1个项目', '社区支持']" />
        </div>
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">企业版</span>
            <div class="copy-btn" @click="copy(codeExamples.pricing4, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <APricingCard plan="企业版" :price="999" period="月" :features="['所有专业功能', '无限项目', '专属客服', '定制开发', 'SLA保障']" />
        </div>
      </div>

      <!-- 16. 通知卡片 -->
      <h2 class="section-title">16. 通知卡片 ANotificationCard</h2>
      <div class="examples-grid">
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">系统通知</span>
            <div class="copy-btn" @click="copy(codeExamples.notification1, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <ANotificationCard
            title="系统通知"
            :notifications="[
              { id: 1, title: '系统更新', content: '系统将于今晚12点进行维护升级', time: '10分钟前', type: 'info', read: false },
              { id: 2, title: '维护完成', content: '系统维护已完成', time: '1小时前', type: 'success', read: true }
            ]"
            @item-click="(notification) => showMsgSuccess(`点击了${notification.title}`)"
          />
        </div>
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">消息通知</span>
            <div class="copy-btn" @click="copy(codeExamples.notification2, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <ANotificationCard
            title="消息通知"
            :notifications="[
              { id: 1, title: '新订单', content: '您有一个新订单待处理', time: '刚刚', type: 'warning', read: false },
              { id: 2, title: '评论回复', content: '有人回复了您的评论', time: '5分钟前', type: 'info', read: false },
              { id: 3, title: '系统消息', content: '您的账户余额不足', time: '30分钟前', type: 'error', read: true }
            ]"
            @item-click="(notification) => showMsgSuccess(`点击了${notification.title}`)"
          />
        </div>
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">待办提醒</span>
            <div class="copy-btn" @click="copy(codeExamples.notification3, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <ANotificationCard
            title="待办提醒"
            :notifications="[
              { id: 1, title: '会议提醒', content: '下午3点有项目评审会议', time: '2小时前', type: 'warning', read: false },
              { id: 2, title: '报告截止', content: '周报需在今天下班前提交', time: '4小时前', type: 'error', read: false }
            ]"
            @item-click="(notification) => showMsgSuccess(`点击了${notification.title}`)"
          />
        </div>
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">安全通知</span>
            <div class="copy-btn" @click="copy(codeExamples.notification4, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <ANotificationCard
            title="安全通知"
            :notifications="[
              { id: 1, title: '登录异常', content: '检测到异地登录，请确认是否本人操作', time: '15分钟前', type: 'error', read: false },
              { id: 2, title: '密码即将过期', content: '您的密码将在7天后过期，请及时修改', time: '1天前', type: 'warning', read: false },
              { id: 3, title: '账号已锁定', content: '多次登录失败，账号已被锁定', time: '2天前', type: 'error', read: true }
            ]"
            @item-click="(notification) => showMsgSuccess(`点击了${notification.title}`)"
          />
        </div>
      </div>

      <!-- 17. 活动卡片 -->
      <h2 class="section-title">17. 活动卡片 AActivityCard</h2>
      <div class="examples-grid">
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">最近活动</span>
            <div class="copy-btn" @click="copy(codeExamples.activity1, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <AActivityCard
            title="最近活动"
            :activities="[
              { id: 1, user: '张三', action: '创建了', target: '新项目', time: '5分钟前' },
              { id: 2, user: '李四', action: '更新了', target: '文档', time: '10分钟前' }
            ]"
          />
        </div>
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">系统日志</span>
            <div class="copy-btn" @click="copy(codeExamples.activity2, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <AActivityCard
            title="系统日志"
            :activities="[
              { id: 1, user: '王五', action: '登录了', target: '系统', time: '1分钟前' },
              { id: 2, user: '赵六', action: '删除了', target: '订单数据', time: '3分钟前' },
              { id: 3, user: '孙七', action: '导出了', target: '报表', time: '8分钟前' }
            ]"
          />
        </div>
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">团队协作</span>
            <div class="copy-btn" @click="copy(codeExamples.activity3, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <AActivityCard
            title="团队协作"
            :activities="[
              { id: 1, user: '周八', action: '评论了', target: '需求文档', time: '12分钟前' },
              { id: 2, user: '吴九', action: '分享了', target: '设计稿', time: '20分钟前' },
              { id: 3, user: '郑十', action: '完成了', target: '代码审查', time: '35分钟前' }
            ]"
          />
        </div>
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">操作记录</span>
            <div class="copy-btn" @click="copy(codeExamples.activity4, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <AActivityCard
            title="操作记录"
            :activities="[
              { id: 1, user: '管理员', action: '修改了', target: '系统配置', time: '2分钟前' },
              { id: 2, user: '管理员', action: '添加了', target: '新用户', time: '5分钟前' },
              { id: 3, user: '管理员', action: '备份了', target: '数据库', time: '15分钟前' },
              { id: 4, user: '管理员', action: '清理了', target: '缓存', time: '30分钟前' }
            ]"
          />
        </div>
      </div>

      <!-- 18. 数据对比卡片 -->
      <h2 class="section-title">18. 数据对比卡片 ADataCard</h2>
      <el-row :gutter="20">
        <el-col :xs="6" :sm="6" :md="6">
          <div class="example-item">
            <div class="example-header">
              <span class="example-title">单项数据</span>
              <div class="copy-btn" @click="copy(codeExamples.dataCard1, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <ADataCard
              :colConfig="{ xs: 24, sm: 24, md: 24 }"
              title="总用户数"
              icon-code="user"
              :data-list="[{ label: '当前用户', value: '12,345', percent: 5.2 }]"
            />
          </div>
        </el-col>
        <el-col :xs="6" :sm="6" :md="6">
          <div class="example-item">
            <div class="example-header">
              <span class="example-title">多项对比</span>
              <div class="copy-btn" @click="copy(codeExamples.dataCard2, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <ADataCard
              :colConfig="{ xs: 24, sm: 24, md: 24 }"
              title="销售数据"
              icon-code="chart"
              :data-list="[
                { label: '今日销售', value: '¥8,520', percent: 12.5 },
                { label: '昨日', value: '¥7,580', percent: -2.1 }
              ]"
            />
          </div>
        </el-col>
        <el-col :xs="6" :sm="6" :md="6">
          <div class="example-item">
            <div class="example-header">
              <span class="example-title">订单统计</span>
              <div class="copy-btn" @click="copy(codeExamples.dataCard3, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <ADataCard
              :colConfig="{ xs: 24, sm: 24, md: 24 }"
              title="订单数据"
              icon-code="order"
              :data-list="[
                { label: '待处理', value: '45', percent: 8.3 },
                { label: '已完成', value: '328', percent: 15.2 }
              ]"
            />
          </div>
        </el-col>
        <el-col :xs="6" :sm="6" :md="6">
          <div class="example-item">
            <div class="example-header">
              <span class="example-title">访问统计</span>
              <div class="copy-btn" @click="copy(codeExamples.dataCard4, '复制成功!')">
                <Icon code="copy" size="md" />
              </div>
            </div>
            <ADataCard
              :colConfig="{ xs: 24, sm: 24, md: 24 }"
              title="网站流量"
              icon-code="view"
              :data-list="[
                { label: '今日PV', value: '25,680', percent: 22.6 },
                { label: '今日UV', value: '8,520', percent: 18.4 }
              ]"
            />
          </div>
        </el-col>
      </el-row>

      <!-- 19. 柱状图统计卡片 -->
      <h2 class="section-title">19. 柱状图统计卡片 ABarStatsCard</h2>
      <div class="examples-grid">
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">用户增长统计</span>
            <div class="copy-btn" @click="copy(codeExamples.barStats1, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <ABarStatsCard
            title="用户增长"
            description="比上周 +23%"
            :chart-data="[160, 100, 150, 80, 190, 100, 175]"
            :x-axis-data="['1', '2', '3', '4', '5', '6', '7']"
            :stats="[
              { label: '今日新增', value: '1.2k' },
              { label: '本周新增', value: '8.5k' }
            ]"
          />
        </div>
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">订单统计</span>
            <div class="copy-btn" @click="copy(codeExamples.barStats2, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <ABarStatsCard
            title="订单统计"
            description="环比增长 +15%"
            :chart-data="[85, 120, 95, 140, 110, 130, 150]"
            :x-axis-data="['周一', '周二', '周三', '周四', '周五', '周六', '周日']"
            :stats="[
              { label: '今日订单', value: '150' },
              { label: '本周订单', value: '830' }
            ]"
          />
        </div>
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">销售额分析</span>
            <div class="copy-btn" @click="copy(codeExamples.barStats3, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <ABarStatsCard
            title="销售额分析"
            description="同比增长 +35%"
            :chart-data="[180, 220, 195, 250, 235, 270, 290]"
            :x-axis-data="['1', '2', '3', '4', '5', '6', '7']"
            :stats="[
              { label: '今日销售', value: '¥29万' },
              { label: '本周销售', value: '¥164万' }
            ]"
          />
        </div>
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">访问统计</span>
            <div class="copy-btn" @click="copy(codeExamples.barStats4, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <ABarStatsCard
            title="访问统计"
            description="较上月 +18%"
            :chart-data="[320, 280, 350, 310, 390, 360, 410]"
            :x-axis-data="['周一', '周二', '周三', '周四', '周五', '周六', '周日']"
            :stats="[
              { label: '今日访问', value: '4.1万' },
              { label: '本周访问', value: '24.2万' }
            ]"
          />
        </div>
      </div>

      <!-- 20. 折线图统计卡片 -->
      <h2 class="section-title">20. 折线图统计卡片 ALineStatsCard</h2>
      <div class="examples-grid">
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">访问量趋势</span>
            <div class="copy-btn" @click="copy(codeExamples.lineStats1, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <ALineStatsCard
            title="访问量趋势"
            description="较上周 +18.5%"
            :chart-data="[120, 150, 180, 160, 200, 190, 220]"
            :x-axis-data="['1', '2', '3', '4', '5', '6', '7']"
            :stats="[
              { label: '今日访问', value: '8.2k' },
              { label: '本周访问', value: '52k' }
            ]"
          />
        </div>
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">销售趋势</span>
            <div class="copy-btn" @click="copy(codeExamples.lineStats2, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <ALineStatsCard
            title="销售趋势"
            description="同比增长 +28%"
            :chart-data="[220, 180, 250, 230, 280, 260, 310]"
            :x-axis-data="['周一', '周二', '周三', '周四', '周五', '周六', '周日']"
            :stats="[
              { label: '今日销售', value: '¥31k' },
              { label: '本周销售', value: '¥173k' }
            ]"
          />
        </div>
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">营收趋势</span>
            <div class="copy-btn" @click="copy(codeExamples.lineStats3, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <ALineStatsCard
            title="营收趋势"
            description="环比增长 +22%"
            :chart-data="[150, 165, 158, 175, 182, 190, 205]"
            :x-axis-data="['1', '2', '3', '4', '5', '6', '7']"
            :stats="[
              { label: '今日营收', value: '¥20.5k' },
              { label: '本周营收', value: '¥122.5k' }
            ]"
          />
        </div>
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">转化率趋势</span>
            <div class="copy-btn" @click="copy(codeExamples.lineStats4, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <ALineStatsCard
            title="转化率趋势"
            description="提升 +5.2%"
            :chart-data="[12, 15, 14, 18, 17, 20, 22]"
            :x-axis-data="['周一', '周二', '周三', '周四', '周五', '周六', '周日']"
            :stats="[
              { label: '今日转化', value: '22%' },
              { label: '周均转化', value: '16.9%' }
            ]"
          />
        </div>
      </div>

      <!-- 21. 表单卡片 -->
      <h2 class="section-title">21. 表单卡片 AFormCard</h2>
      <div class="examples-grid">
        <div class="example-item">
          <div class="example-header">
            <span class="example-title">基本表单</span>
            <div class="copy-btn" @click="copy(codeExamples.formCard1, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <AFormCard title="基本信息" subtitle="请填写您的基本信息" :model="{ name: '', email: '' }">
            <el-form-item label="姓名" prop="name">
              <el-input placeholder="请输入姓名" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input placeholder="请输入邮箱" />
            </el-form-item>
          </AFormCard>
        </div>

        <div class="example-item">
          <div class="example-header">
            <span class="example-title">联系方式表单</span>
            <div class="copy-btn" @click="copy(codeExamples.formCard2, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <AFormCard title="联系方式" subtitle="请填写您的联系方式" :model="{ phone: '', address: '' }">
            <el-form-item label="电话" prop="phone">
              <el-input placeholder="请输入电话" />
            </el-form-item>
            <el-form-item label="地址" prop="address">
              <el-input placeholder="请输入地址" />
            </el-form-item>
          </AFormCard>
        </div>

        <div class="example-item">
          <div class="example-header">
            <span class="example-title">账户设置</span>
            <div class="copy-btn" @click="copy(codeExamples.formCard3, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <AFormCard title="账户设置" subtitle="修改您的账户信息" :model="{ username: '', password: '' }">
            <el-form-item label="用户名" prop="username">
              <el-input placeholder="请输入用户名" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input type="password" placeholder="请输入密码" />
            </el-form-item>
          </AFormCard>
        </div>

        <div class="example-item">
          <div class="example-header">
            <span class="example-title">个人资料</span>
            <div class="copy-btn" @click="copy(codeExamples.formCard4, '复制成功!')">
              <Icon code="copy" size="md" />
            </div>
          </div>
          <AFormCard title="个人资料" subtitle="完善您的个人资料" :model="{ nickname: '', bio: '' }">
            <el-form-item label="昵称" prop="nickname">
              <el-input placeholder="请输入昵称" />
            </el-form-item>
            <el-form-item label="简介" prop="bio">
              <el-input type="textarea" placeholder="请输入个人简介" :rows="3" />
            </el-form-item>
          </AFormCard>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { showMsgSuccess } from '@/utils/modal'
import { copy } from '@/utils/function'

// 代码示例文本
const codeExamples = {
  stats1: `<AStatsCard title="总用户数" :value="8520" description="较昨日" icon="user" />`,
  stats2: `<AStatsCard title="活跃用户" :value="1234" icon="hot" :trend="{ value: 12.5, isUp: true }" />`,
  stats3: `<AStatsCard title="月度目标" :value="85000" :target="100000" unit="元" icon="goal" :show-progress="true" />`,
  stats4: `<AStatsCard title="总收入" :value="98000" unit="元" icon="money" icon-color="#f56c6c" icon-bg-color="rgba(245, 108, 108, 0.1)" />`,

  bar1: `<ABarChartCard title="本周销售趋势" :data="[120, 200, 150, 80, 70, 110, 130]" :x-axis-data="['周一', '周二', '周三', '周四', '周五', '周六', '周日']" />`,
  bar2: `<ABarChartCard title="月度销售额" :value="125000" unit="元" :trend="{ value: 12.5, isUp: true }" description="较上月" :data="[120, 200, 150, 180]" :x-axis-data="['1周', '2周', '3周', '4周']" />`,
  bar3: `<ABarChartCard title="季度对比" :data="[{ name: 'Q1', data: [150, 180, 200] }, { name: 'Q2', data: [180, 220, 190] }]" :x-axis-data="['1月', '2月', '3月']" :show-legend="true" />`,
  bar4: `<ABarChartCard title="年度销售额" :value="986000" unit="元" :trend="{ value: 28.3, isUp: true }" description="同比增长" :data="[65, 80, 92, 110, 95, 105, 98, 115, 120, 105, 110, 89]" :x-axis-data="['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']" />`,

  line1: `<ALineChartCard title="用户增长趋势" :data="[120, 200, 150, 180, 220, 190, 240]" :x-axis-data="['周一', '周二', '周三', '周四', '周五', '周六', '周日']" />`,
  line2: `<ALineChartCard title="流量对比" :data="[{ name: '本月', data: [120, 200, 150, 180] }, { name: '上月', data: [100, 180, 140, 160] }]" :x-axis-data="['第1周', '第2周', '第3周', '第4周']" :show-legend="true" />`,
  line3: `<ALineChartCard title="访问量统计" :value="52800" description="本周总计" :data="[6200, 7800, 7500, 8200, 8800, 7300, 6700]" :x-axis-data="['周一', '周二', '周三', '周四', '周五', '周六', '周日']" />`,
  line4: `<ALineChartCard title="业绩趋势分析" :data="[{ name: '团队A', data: [320, 332, 301, 334, 390] }, { name: '团队B', data: [220, 282, 191, 234, 290] }, { name: '团队C', data: [150, 232, 201, 154, 190] }]" :x-axis-data="['第1季度', '第2季度', '第3季度', '第4季度', '第5季度']" :show-legend="true" />`,

  pie1: `<APieChartCard title="销售分布" :data="[{ name: '直接访问', value: 335 }, { name: '邮件营销', value: 310 }, { name: '联盟广告', value: 234 }, { name: '视频广告', value: 135 }]" />`,
  pie2: `<APieChartCard title="用户分布" :data="[{ name: 'VIP用户', value: 450 }, { name: '普通用户', value: 800 }]" chart-type="ring" />`,
  pie3: `<APieChartCard title="产品销售占比" :data="[{ name: '电子产品', value: 520 }, { name: '服装鞋帽', value: 380 }, { name: '食品饮料', value: 280 }, { name: '家居用品', value: 220 }, { name: '其他', value: 100 }]" />`,
  pie4: `<APieChartCard title="区域销售分布" :data="[{ name: '华东地区', value: 680 }, { name: '华南地区', value: 520 }, { name: '华北地区', value: 450 }, { name: '西南地区', value: 350 }]" chart-type="ring" />`,

  dataList1: `<ADataListCard title="待办事项" :list="[{ title: '完成项目报告', status: '进行中', time: '2小时前', icon: 'file' }, { title: '客户会议', status: '待处理', time: '今天 14:00', icon: 'calendar' }]" />`,
  dataList2: `<ADataListCard title="最近活动" :list="[{ title: '新用户注册', status: '成功', time: '5分钟前', icon: 'user', iconColor: '#67c23a' }, { title: '订单支付', status: '完成', time: '10分钟前', icon: 'money', iconColor: '#409eff' }]" />`,
  dataList3: `<ADataListCard title="系统通知" :list="[{ title: '系统升级通知', status: '重要', time: '1小时前', icon: 'bell', iconColor: '#e6a23c' }, { title: '维护公告', status: '已读', time: '3小时前', icon: 'info', iconColor: '#909399' }, { title: '功能更新', status: '未读', time: '昨天', icon: 'update', iconColor: '#409eff' }]" />`,
  dataList4: `<ADataListCard title="项目任务列表" :list="[{ title: '前端开发', status: '进行中', time: '2天前', icon: 'code', iconColor: '#409eff' }, { title: '后端接口', status: '已完成', time: '3天前', icon: 'server', iconColor: '#67c23a' }, { title: '测试验收', status: '待开始', time: '明天', icon: 'test', iconColor: '#909399' }]" />`,

  radar1: `<ARadarChartCard title="能力评估" :indicator="[{ name: '销售', max: 100 }, { name: '管理', max: 100 }, { name: '技术', max: 100 }, { name: '客服', max: 100 }, { name: '研发', max: 100 }]" :data="[{ name: '当前', value: [80, 90, 85, 75, 95] }]" />`,
  radar2: `<ARadarChartCard title="团队对比" :indicator="[{ name: '执行力', max: 100 }, { name: '创新力', max: 100 }, { name: '协作力', max: 100 }, { name: '学习力', max: 100 }]" :data="[{ name: 'A组', value: [90, 85, 88, 92] }, { name: 'B组', value: [75, 90, 82, 85] }]" />`,
  radar3: `<ARadarChartCard title="产品指标" :indicator="[{ name: '性能', max: 100 }, { name: '功能', max: 100 }, { name: '体验', max: 100 }, { name: '稳定', max: 100 }, { name: '安全', max: 100 }, { name: '成本', max: 100 }]" :data="[{ name: '产品A', value: [88, 92, 85, 90, 95, 78] }]" />`,
  radar4: `<ARadarChartCard title="员工素质" :indicator="[{ name: '专业能力', max: 100 }, { name: '沟通能力', max: 100 }, { name: '团队协作', max: 100 }, { name: '解决问题', max: 100 }, { name: '责任心', max: 100 }]" :data="[{ name: '张三', value: [85, 80, 90, 88, 95] }, { name: '李四', value: [92, 88, 85, 90, 87] }]" />`,

  map1: `<AMapChartCard title="全国销售分布" :map-data="[{ name: '北京', value: 100, adcode: '110000', level: 'province' }, { name: '上海', value: 200, adcode: '310000', level: 'province' }, { name: '广东', value: 300, adcode: '440000', level: 'province' }]" />`,
  map2: `<AMapChartCard title="区域用户分布" :map-data="[{ name: '浙江', value: 250, adcode: '330000', level: 'province' }, { name: '江苏', value: 180, adcode: '320000', level: 'province' }, { name: '四川', value: 150, adcode: '510000', level: 'province' }, { name: '山东', value: 200, adcode: '370000', level: 'province' }]" />`,
  map3: `<AMapChartCard title="订单热力分布" :map-data="[{ name: '河北', value: 120, adcode: '130000', level: 'province' }, { name: '河南', value: 190, adcode: '410000', level: 'province' }, { name: '湖北', value: 160, adcode: '420000', level: 'province' }, { name: '湖南', value: 140, adcode: '430000', level: 'province' }]" />`,
  map4: `<AMapChartCard title="营收区域排名" :map-data="[{ name: '福建', value: 220, adcode: '350000', level: 'province' }, { name: '安徽', value: 135, adcode: '340000', level: 'province' }, { name: '江西', value: 110, adcode: '360000', level: 'province' }, { name: '辽宁', value: 175, adcode: '210000', level: 'province' }]" />`,

  timeline1: `<ATimelineListCard title="项目进度" :list="[{ time: '2024-01-15', content: '项目启动', status: 'success' }, { time: '2024-01-20', content: '需求评审', status: 'info' }, { time: '2024-01-25', content: '开发中', status: 'warning' }]" />`,
  timeline2: `<ATimelineListCard title="订单状态" :list="[{ time: '2024-10-12 10:20', content: '订单已创建', status: 'info' }, { time: '2024-10-12 10:25', content: '支付成功', status: 'success' }, { time: '2024-10-12 11:00', content: '商品出库', status: 'success' }]" />`,
  timeline3: `<ATimelineListCard title="物流跟踪" :list="[{ time: '2024-10-12 14:00', content: '快递已揽收', status: 'success' }, { time: '2024-10-12 18:30', content: '运输中', status: 'info' }, { time: '2024-10-13 09:15', content: '到达本市', status: 'success' }, { time: '2024-10-13 12:00', content: '派送中', status: 'warning' }]" />`,
  timeline4: `<ATimelineListCard title="审批流程" :list="[{ time: '2024-10-10 09:00', content: '提交申请', status: 'success' }, { time: '2024-10-10 14:30', content: '部门审批通过', status: 'success' }, { time: '2024-10-11 10:00', content: '财务审批中', status: 'warning' }]" />`,

  image1: `<AImageCard image-url="https://picsum.photos/400/300" title="精美风景" description="这是一张精美的风景图片" />`,
  image2: `<AImageCard image-url="https://picsum.photos/400/301" title="城市夜景" description="繁华都市的璀璨夜色" />`,
  image3: `<AImageCard image-url="https://picsum.photos/400/302" title="自然景观" description="大自然的鬼斧神工" />`,
  image4: `<AImageCard image-url="https://picsum.photos/400/303" title="海滨风光" description="碧海蓝天,令人心旷神怡" />`,

  user1: `<AUserCard name="张三" avatar="https://i.pravatar.cc/150?img=1" title="产品经理" description="负责产品规划与设计" />`,
  user2: `<AProfileCard :profile="{ name: '李四', avatar: 'https://i.pravatar.cc/150?img=2', title: '技术总监', company: '互联网公司', email: 'lisi@example.com', phone: '138****8888' }" :stats="[{ label: '项目', value: 28 }, { label: '粉丝', value: 1234 }]" show-contact />`,
  user3: `<ASocialCard :content="{ user: '王五', avatar: 'https://i.pravatar.cc/100?img=3', text: '今天天气真好,适合出去走走!', time: '5分钟前' }" />`,
  user4: `<AUserCard name="赵六" avatar="https://i.pravatar.cc/150?img=4" title="前端工程师" description="专注于Vue和React开发" />`,

  table1: `<ATableCard title="最新订单" :columns="[{ prop: 'id', label: '订单号', width: 120 }, { prop: 'name', label: '商品名称' }, { prop: 'price', label: '价格' }, { prop: 'status', label: '状态' }]" :data="[{ id: 'ORD001', name: '商品A', price: '¥199', status: '已完成' }, { id: 'ORD002', name: '商品B', price: '¥299', status: '进行中' }]" />`,

  info1: `<AInfoCard title="成功提示" type="success">操作已成功完成!</AInfoCard>`,
  info2: `<AInfoCard title="警告信息" type="warning">请注意检查数据!</AInfoCard>`,
  info3: `<AInfoCard title="错误提示" type="error">操作失败,请重试!</AInfoCard>`,
  info4: `<AInfoCard title="普通信息" type="info">这是一条普通消息</AInfoCard>`,

  empty1: `<AEmptyCard title="内容" description="暂无数据" />`,
  empty2: `<AEmptyCard description="暂无订单记录" icon="order" />`,
  empty3: `<AEmptyCard title="搜索结果" description="未找到相关内容" icon="search" />`,
  empty4: `<AEmptyCard title="收藏夹" description="还没有收藏任何内容" icon="favorite" />`,

  weather1: `<AWeatherCard :weather="{ city: '北京', temperature: 25, condition: '晴', icon: 'sunny' }" />`,
  weather2: `<AWeatherCard :weather="{ city: '上海', temperature: 28, condition: '多云', icon: 'cloudy', humidity: 65 }" />`,
  weather3: `<AWeatherCard :weather="{ city: '广州', temperature: 22, condition: '小雨', icon: 'rainy', humidity: 78 }" />`,
  weather4: `<AWeatherCard :weather="{ city: '哈尔滨', temperature: -5, condition: '雪', icon: 'snowy', humidity: 45 }" />`,

  pricing1: `<APricingCard plan="基础版" :price="99" period="月" :features="['功能A', '功能B', '功能C']" />`,
  pricing2: `<APricingCard plan="专业版" :price="299" period="月" :features="['所有基础功能', '高级功能A', '高级功能B', '优先支持']" recommended />`,
  pricing3: `<APricingCard plan="免费版" price="免费" :features="['基础功能', '1个项目', '社区支持']" />`,
  pricing4: `<APricingCard plan="企业版" :price="999" period="月" :features="['所有专业功能', '无限项目', '专属客服', '定制开发', 'SLA保障']" />`,

  notification1: `<ANotificationCard title="系统通知" :notifications="[{ id: 1, title: '系统更新', content: '系统将于今晚12点进行维护升级', time: '10分钟前', type: 'info', read: false }, { id: 2, title: '维护完成', content: '系统维护已完成', time: '1小时前', type: 'success', read: true }]" @item-click="(notification) => showMsgSuccess(\`点击了\${notification.title}\`)" />`,
  notification2: `<ANotificationCard title="消息通知" :notifications="[{ id: 1, title: '新订单', content: '您有一个新订单待处理', time: '刚刚', type: 'warning', read: false }, { id: 2, title: '评论回复', content: '有人回复了您的评论', time: '5分钟前', type: 'info', read: false }, { id: 3, title: '系统消息', content: '您的账户余额不足', time: '30分钟前', type: 'error', read: true }]" @item-click="(notification) => showMsgSuccess(\`点击了\${notification.title}\`)" />`,
  notification3: `<ANotificationCard title="待办提醒" :notifications="[{ id: 1, title: '会议提醒', content: '下午3点有项目评审会议', time: '2小时前', type: 'warning', read: false }, { id: 2, title: '报告截止', content: '周报需在今天下班前提交', time: '4小时前', type: 'error', read: false }]" @item-click="(notification) => showMsgSuccess(\`点击了\${notification.title}\`)" />`,
  notification4: `<ANotificationCard title="安全通知" :notifications="[{ id: 1, title: '登录异常', content: '检测到异地登录，请确认是否本人操作', time: '15分钟前', type: 'error', read: false }, { id: 2, title: '密码即将过期', content: '您的密码将在7天后过期，请及时修改', time: '1天前', type: 'warning', read: false }, { id: 3, title: '账号已锁定', content: '多次登录失败，账号已被锁定', time: '2天前', type: 'error', read: true }]" @item-click="(notification) => showMsgSuccess(\`点击了\${notification.title}\`)" />`,

  activity1: `<AActivityCard title="最近活动" :activities="[{ id: 1, user: '张三', action: '创建了', target: '新项目', time: '5分钟前' }, { id: 2, user: '李四', action: '更新了', target: '文档', time: '10分钟前' }]" />`,
  activity2: `<AActivityCard title="系统日志" :activities="[{ id: 1, user: '王五', action: '登录了', target: '系统', time: '1分钟前' }, { id: 2, user: '赵六', action: '删除了', target: '订单数据', time: '3分钟前' }, { id: 3, user: '孙七', action: '导出了', target: '报表', time: '8分钟前' }]" />`,
  activity3: `<AActivityCard title="团队协作" :activities="[{ id: 1, user: '周八', action: '评论了', target: '需求文档', time: '12分钟前' }, { id: 2, user: '吴九', action: '分享了', target: '设计稿', time: '20分钟前' }, { id: 3, user: '郑十', action: '完成了', target: '代码审查', time: '35分钟前' }]" />`,
  activity4: `<AActivityCard title="操作记录" :activities="[{ id: 1, user: '管理员', action: '修改了', target: '系统配置', time: '2分钟前' }, { id: 2, user: '管理员', action: '添加了', target: '新用户', time: '5分钟前' }, { id: 3, user: '管理员', action: '备份了', target: '数据库', time: '15分钟前' }, { id: 4, user: '管理员', action: '清理了', target: '缓存', time: '30分钟前' }]" />`,

  dataCard1: `<ADataCard :colConfig="{ xs: 24, sm: 24, md: 24 }" title="总用户数" icon-code="user" :data-list="[{ label: '当前用户', value: '12,345', percent: 5.2 }]" />`,
  dataCard2: `<ADataCard :colConfig="{ xs: 24, sm: 24, md: 24 }" title="销售数据" icon-code="chart" :data-list="[{ label: '今日销售', value: '¥8,520', percent: 12.5 }, { label: '昨日', value: '¥7,580', percent: -2.1 }]" />`,
  dataCard3: `<ADataCard :colConfig="{ xs: 24, sm: 24, md: 24 }" title="订单数据" icon-code="order" :data-list="[{ label: '待处理', value: '45', percent: 8.3 }, { label: '已完成', value: '328', percent: 15.2 }]" />`,
  dataCard4: `<ADataCard :colConfig="{ xs: 24, sm: 24, md: 24 }" title="网站流量" icon-code="view" :data-list="[{ label: '今日PV', value: '25,680', percent: 22.6 }, { label: '今日UV', value: '8,520', percent: 18.4 }]" />`,

  barStats1: `<ABarStatsCard title="用户增长" description="比上周 +23%" :chart-data="[160, 100, 150, 80, 190, 100, 175]" :x-axis-data="['1', '2', '3', '4', '5', '6', '7']" :stats="[{ label: '今日新增', value: '1.2k' }, { label: '本周新增', value: '8.5k' }]" />`,
  barStats2: `<ABarStatsCard title="订单统计" description="环比增长 +15%" :chart-data="[85, 120, 95, 140, 110, 130, 150]" :x-axis-data="['周一', '周二', '周三', '周四', '周五', '周六', '周日']" :stats="[{ label: '今日订单', value: '150' }, { label: '本周订单', value: '830' }]" />`,
  barStats3: `<ABarStatsCard title="销售额分析" description="同比增长 +35%" :chart-data="[180, 220, 195, 250, 235, 270, 290]" :x-axis-data="['1', '2', '3', '4', '5', '6', '7']" :stats="[{ label: '今日销售', value: '¥29万' }, { label: '本周销售', value: '¥164万' }]" />`,
  barStats4: `<ABarStatsCard title="访问统计" description="较上月 +18%" :chart-data="[320, 280, 350, 310, 390, 360, 410]" :x-axis-data="['周一', '周二', '周三', '周四', '周五', '周六', '周日']" :stats="[{ label: '今日访问', value: '4.1万' }, { label: '本周访问', value: '24.2万' }]" />`,

  lineStats1: `<ALineStatsCard title="访问量趋势" description="较上周 +18.5%" :chart-data="[120, 150, 180, 160, 200, 190, 220]" :x-axis-data="['1', '2', '3', '4', '5', '6', '7']" :stats="[{ label: '今日访问', value: '8.2k' }, { label: '本周访问', value: '52k' }]" />`,
  lineStats2: `<ALineStatsCard title="销售趋势" description="同比增长 +28%" :chart-data="[220, 180, 250, 230, 280, 260, 310]" :x-axis-data="['周一', '周二', '周三', '周四', '周五', '周六', '周日']" :stats="[{ label: '今日销售', value: '¥31k' }, { label: '本周销售', value: '¥173k' }]" />`,
  lineStats3: `<ALineStatsCard title="营收趋势" description="环比增长 +22%" :chart-data="[150, 165, 158, 175, 182, 190, 205]" :x-axis-data="['1', '2', '3', '4', '5', '6', '7']" :stats="[{ label: '今日营收', value: '¥20.5k' }, { label: '本周营收', value: '¥122.5k' }]" />`,
  lineStats4: `<ALineStatsCard title="转化率趋势" description="提升 +5.2%" :chart-data="[12, 15, 14, 18, 17, 20, 22]" :x-axis-data="['周一', '周二', '周三', '周四', '周五', '周六', '周日']" :stats="[{ label: '今日转化', value: '22%' }, { label: '周均转化', value: '16.9%' }]" />`,

  formCard1: `<AFormCard title="基本信息" subtitle="请填写您的基本信息" :model="{ name: '', email: '' }"><el-form-item label="姓名" prop="name"><el-input placeholder="请输入姓名" /></el-form-item><el-form-item label="邮箱" prop="email"><el-input placeholder="请输入邮箱" /></el-form-item></AFormCard>`,
  formCard2: `<AFormCard title="联系方式" subtitle="请填写您的联系方式" :model="{ phone: '', address: '' }"><el-form-item label="电话" prop="phone"><el-input placeholder="请输入电话" /></el-form-item><el-form-item label="地址" prop="address"><el-input placeholder="请输入地址" /></el-form-item></AFormCard>`,
  formCard3: `<AFormCard title="账户设置" subtitle="修改您的账户信息" :model="{ username: '', password: '' }"><el-form-item label="用户名" prop="username"><el-input placeholder="请输入用户名" /></el-form-item><el-form-item label="密码" prop="password"><el-input type="password" placeholder="请输入密码" /></el-form-item></AFormCard>`,
  formCard4: `<AFormCard title="个人资料" subtitle="完善您的个人资料" :model="{ nickname: '', bio: '' }"><el-form-item label="昵称" prop="nickname"><el-input placeholder="请输入昵称" /></el-form-item><el-form-item label="简介" prop="bio"><el-input type="textarea" placeholder="请输入个人简介" :rows="3" /></el-form-item></AFormCard>`
}
</script>

<style lang="scss" scoped>
// 使用 UnoCSS @apply 指令定义样式
.examples-container {
  @apply flex flex-col gap-8 w-full;
}

.section-title {
  @apply text-16px font-600 mb-4 mt-6;
  color: var(--el-text-color-primary);
  border-left: 4px solid var(--el-color-primary);
  padding-left: 12px;

  &:first-child {
    @apply mt-0;
  }
}

.examples-grid {
  @apply grid grid-cols-[repeat(auto-fill,minmax(280px,1fr))] gap-5 w-full;
}

.example-item {
  @apply flex flex-col gap-3;

  .example-header {
    @apply flex items-center justify-between px-3 py-2 rounded-md transition-all duration-300;
    background: var(--el-fill-color-light);

    .example-title {
      @apply text-13px font-500;
      color: var(--el-text-color-regular);
    }

    .copy-btn {
      @apply flex items-center justify-center w-7 h-7 rounded-md cursor-pointer transition-all duration-300;
      color: var(--el-text-color-secondary);
      background: transparent;

      &:hover {
        color: var(--el-color-primary);
        background: var(--el-color-primary-light-9);
        @apply scale-110;
      }

      &:active {
        @apply scale-95;
      }
    }
  }

  /* 确保卡片组件在容器内自适应 */
  :deep(.stats-card),
  :deep(.bar-chart-card),
  :deep(.line-chart-card),
  :deep(.pie-chart-card),
  :deep(.data-list-card) {
    @apply h-full;
  }
}
</style>
