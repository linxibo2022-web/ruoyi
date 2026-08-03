<!-- 组件渲染器 -->
<template>
  <!-- 非表单组件：图表、卡片、布局组件，不需要 el-form-item 包裹 -->
  <template v-if="isNonFormComponent">
    <!-- ==================== 统计卡片组件 ==================== -->

    <!-- 统计卡片 -->
    <AStatsCard
      v-if="item.type === 'statsCard'"
      :title="item.props?.title || '总用户数'"
      :value="item.props?.value || 8520"
      :unit="item.props?.unit"
      :description="item.props?.description || '较昨日'"
      :icon="item.props?.icon || 'user'"
      :icon-color="item.props?.iconColor"
      :icon-bg-color="item.props?.iconBgColor"
      :trend="item.props?.trend || { value: 12.5, isUp: true }"
      :target="item.props?.target"
      :show-progress="item.props?.showProgress"
      :show-animation="item.props?.showAnimation"
      :col-config="{ xs: 24, sm: 24, md: 24, lg: 24, xl: 24 }"
    />

    <!-- 折线统计卡片 -->
    <ALineStatsCard
      v-else-if="item.type === 'lineStatsCard'"
      :title="item.props?.title || '访问量趋势'"
      :description="item.props?.description || '较上周 +18.5%'"
      :chart-data="item.props?.chartData || [120, 150, 180, 160, 200, 190, 220]"
      :x-axis-data="item.props?.xAxisData || ['1', '2', '3', '4', '5', '6', '7']"
      :stats="
        item.props?.stats || [
          { label: '今日访问', value: '8.2k' },
          { label: '本周访问', value: '52k' }
        ]
      "
      :col-config="{ xs: 24, sm: 24, md: 24, lg: 24, xl: 24 }"
    />

    <!-- 柱状统计卡片 -->
    <ABarStatsCard
      v-else-if="item.type === 'barStatsCard'"
      :title="item.props?.title || '用户增长'"
      :description="item.props?.description || '比上周 +23%'"
      :chart-data="item.props?.chartData || [160, 100, 150, 80, 190, 100, 175]"
      :x-axis-data="item.props?.xAxisData || ['1', '2', '3', '4', '5', '6', '7']"
      :stats="
        item.props?.stats || [
          { label: '今日新增', value: '1.2k' },
          { label: '本月新增', value: '32k' }
        ]
      "
      :col-config="{ xs: 24, sm: 24, md: 24, lg: 24, xl: 24 }"
    />

    <!-- ==================== 图表卡片组件 ==================== -->

    <!-- 饼图卡片 -->
    <APieChartCard
      v-else-if="item.type === 'pieChartCard'"
      :title="item.props?.title || '销售分布'"
      :subtitle="item.props?.subtitle"
      :height="item.props?.height || 280"
      :data="
        item.props?.data || [
          { name: '直接访问', value: 335 },
          { name: '邮件营销', value: 310 },
          { name: '联盟广告', value: 234 }
        ]
      "
      :col-config="{ xs: 24, sm: 24, md: 24, lg: 24, xl: 24 }"
    />

    <!-- 柱图卡片 -->
    <ABarChartCard
      v-else-if="item.type === 'barChartCard'"
      :title="item.props?.title || '月度销售额'"
      :value="item.props?.value"
      :unit="item.props?.unit"
      :trend="item.props?.trend"
      :height="item.props?.height || 280"
      :data="item.props?.data || [120, 200, 150, 180, 220, 190, 240]"
      :col-config="{ xs: 24, sm: 24, md: 24, lg: 24, xl: 24 }"
    />

    <!-- 折线图卡片 -->
    <ALineChartCard
      v-else-if="item.type === 'lineChartCard'"
      :title="item.props?.title || '本月访问量'"
      :value="item.props?.value"
      :unit="item.props?.unit"
      :trend="item.props?.trend"
      :height="item.props?.height || 280"
      :data="item.props?.data || [120, 200, 150, 180, 220, 190, 240]"
      :col-config="{ xs: 24, sm: 24, md: 24, lg: 24, xl: 24 }"
    />

    <!-- 雷达图卡片 -->
    <ARadarChartCard
      v-else-if="item.type === 'radarChartCard'"
      :title="item.props?.title || '能力评估'"
      :height="item.props?.height || 280"
      :indicator="item.props?.indicator || [{ name: '销售' }, { name: '管理' }, { name: '技术' }, { name: '客服' }, { name: '研发' }]"
      :col-config="{ xs: 24, sm: 24, md: 24, lg: 24, xl: 24 }"
    />

    <!-- 地图卡片 -->
    <AMapChartCard
      v-else-if="item.type === 'mapChartCard'"
      :title="item.props?.title || '全国销售分布'"
      :total-value="item.props?.totalValue"
      :unit="item.props?.unit"
      :chart-height="(item.props?.height || 350) + 'px'"
      :map-data="
        item.props?.mapData || [
          { name: '北京', value: 100, adcode: '110000', level: 'province' },
          { name: '上海', value: 200, adcode: '310000', level: 'province' },
          { name: '广东', value: 300, adcode: '440000', level: 'province' }
        ]
      "
      :col-config="{ xs: 24, sm: 24, md: 24, lg: 24, xl: 24 }"
    />

    <!-- ==================== 数据展示卡片组件 ==================== -->

    <!-- 数据对比卡片 -->
    <ADataCard
      v-else-if="item.type === 'dataCard'"
      :title="item.props?.title || '销售数据'"
      :icon-code="item.props?.iconCode || 'chart'"
      :data-list="
        item.props?.dataList || [
          { label: '今日销售', value: '¥8,520', percent: 12.5 },
          { label: '昨日', value: '¥7,580', percent: -2.1 },
          { label: '本周', value: '¥45,600', percent: 8.9 }
        ]
      "
      :col-config="{ xs: 24, sm: 24, md: 24, lg: 24, xl: 24 }"
    />

    <!-- 表格卡片 -->
    <ATableCard
      v-else-if="item.type === 'tableCard'"
      :title="item.props?.title || '最新订单'"
      :data="
        item.props?.data || [
          { id: 'ORD001', name: '商品A', price: '¥199' },
          { id: 'ORD002', name: '商品B', price: '¥299' },
          { id: 'ORD003', name: '商品C', price: '¥399' }
        ]
      "
      :columns="
        item.props?.columns || [
          { prop: 'id', label: '订单号', width: 100 },
          { prop: 'name', label: '商品名称' },
          { prop: 'price', label: '价格', width: 80 }
        ]
      "
      :border="item.props?.border"
      :show-header="item.props?.showHeader !== false"
      :col-config="{ xs: 24, sm: 24, md: 24, lg: 24, xl: 24 }"
    />

    <!-- 数据列表卡片 -->
    <ADataListCard
      v-else-if="item.type === 'dataListCard'"
      :title="item.props?.title || '待办事项'"
      :list="
        item.props?.list || [
          { title: '完成项目报告', status: '进行中', time: '2小时前' },
          { title: '客户会议', status: '待处理', time: '今天 14:00' },
          { title: '代码审查', status: '已完成', time: '昨天' }
        ]
      "
      :show-more-button="item.props?.showMoreButton"
      :col-config="{ xs: 24, sm: 24, md: 24, lg: 24, xl: 24 }"
    />

    <!-- 活动时间轴卡片 -->
    <AActivityCard
      v-else-if="item.type === 'activityCard'"
      :title="item.props?.title || '最近活动'"
      :activities="
        item.props?.activities || [
          { user: '张三', action: '创建了', target: '新项目', time: '5分钟前' },
          { user: '李四', action: '更新了', target: '文档', time: '10分钟前' },
          { user: '王五', action: '删除了', target: '文件', time: '30分钟前' }
        ]
      "
      :show-avatar="item.props?.showAvatar !== false"
      :col-config="{ xs: 24, sm: 24, md: 24, lg: 24, xl: 24 }"
    />

    <!-- 时间轴列表卡片 -->
    <ATimelineListCard
      v-else-if="item.type === 'timelineListCard'"
      :title="item.props?.title || '最近交易'"
      :list="
        item.props?.list || [
          { time: '上午 09:30', content: '收到支付 385.90 元', status: 'success' },
          { time: '上午 10:00', content: '新销售记录', code: 'ML-3467', status: 'info' },
          { time: '下午 02:30', content: '退款处理中', status: 'warning' }
        ]
      "
      :col-config="{ xs: 24, sm: 24, md: 24, lg: 24, xl: 24 }"
    />

    <!-- ==================== 用户信息卡片组件 ==================== -->

    <!-- 用户卡片 -->
    <AUserCard
      v-else-if="item.type === 'userCard'"
      :avatar="item.props?.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"
      :name="item.props?.name || '张三'"
      :role="item.props?.role || '产品经理'"
      :show-status="item.props?.showStatus"
      :stats="item.props?.stats || { posts: 128, followers: '1.2k', following: 567 }"
      :tags="item.props?.tags"
      :col-config="{ xs: 24, sm: 24, md: 24, lg: 24, xl: 24 }"
    />

    <!-- 个人资料卡片 -->
    <AProfileCard
      v-else-if="item.type === 'profileCard'"
      :profile="item.props?.profile || { name: '张三', title: '高级工程师', company: '某某科技公司' }"
      :stats="
        item.props?.stats || [
          { label: '项目', value: 28 },
          { label: '粉丝', value: '1.2k' },
          { label: '关注', value: 567 }
        ]
      "
      :col-config="{ xs: 24, sm: 24, md: 24, lg: 24, xl: 24 }"
    />

    <!-- 社交卡片 -->
    <ASocialCard
      v-else-if="item.type === 'socialCard'"
      :content="item.props?.content || { user: '张三', time: '5分钟前', text: '今天天气真好！出去走走~' }"
      :show-actions="item.props?.showActions"
      :stats="item.props?.stats || { likes: 128, comments: 45, shares: 12 }"
      :col-config="{ xs: 24, sm: 24, md: 24, lg: 24, xl: 24 }"
    />

    <!-- ==================== 特殊功能卡片组件 ==================== -->

    <!-- 表单卡片 -->
    <AFormCard
      v-else-if="item.type === 'formCard'"
      :title="item.props?.title || '基本信息'"
      :model="formData"
      :collapsible="item.props?.collapsible"
      :col-config="{ xs: 24, sm: 24, md: 24, lg: 24, xl: 24 }"
    />

    <!-- 价格卡片 -->
    <APricingCard
      v-else-if="item.type === 'pricingCard'"
      :plan="item.props?.plan || '专业版'"
      :price="item.props?.price || 199"
      :period="item.props?.period || '月'"
      :original-price="item.props?.originalPrice"
      :recommended="item.props?.recommended"
      :features="item.props?.features || ['所有基础功能', '无限项目', '优先支持']"
      :disabled-features="item.props?.disabledFeatures || ['定制开发']"
      :col-config="{ xs: 24, sm: 24, md: 24, lg: 24, xl: 24 }"
    />

    <!-- 图片卡片 -->
    <AImageCard
      v-else-if="item.type === 'imageCard'"
      :image-url="item.props?.imageUrl || 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'"
      :title="item.props?.title || 'Vue 3 深度解析'"
      :description="item.props?.description || '深入探讨Vue 3的响应式原理与组合式API'"
      :category="item.props?.category"
      :author="item.props?.author"
      :date="item.props?.date"
      :layout="item.props?.layout || 'vertical'"
      :col-config="{ xs: 24, sm: 24, md: 24, lg: 24, xl: 24 }"
    />

    <!-- 信息提示卡片 -->
    <AInfoCard
      v-else-if="item.type === 'infoCard'"
      :title="item.props?.title || '系统通知'"
      :type="item.props?.type || 'info'"
      :icon="item.props?.icon"
      :closable="item.props?.closable !== false"
      :col-config="{ xs: 24, sm: 24, md: 24, lg: 24, xl: 24 }"
    />

    <!-- 天气卡片 -->
    <AWeatherCard
      v-else-if="item.type === 'weatherCard'"
      :weather="item.props?.weather || { temperature: 25, condition: '晴', city: '北京', humidity: 60, windSpeed: 12, airQuality: 85 }"
      :show-details="item.props?.showDetails"
      :col-config="{ xs: 24, sm: 24, md: 24, lg: 24, xl: 24 }"
    />

    <!-- 通知卡片 -->
    <ANotificationCard
      v-else-if="item.type === 'notificationCard'"
      :title="item.props?.title || '通知消息'"
      :notifications="
        item.props?.notifications || [
          { title: '新消息', content: '您有一条新消息', read: false, type: 'info' },
          { title: '系统通知', content: '系统将于今晚维护', read: true, type: 'warning' }
        ]
      "
      :show-unread-count="item.props?.showUnreadCount"
      :col-config="{ xs: 24, sm: 24, md: 24, lg: 24, xl: 24 }"
    />

    <!-- 空状态卡片 -->
    <AEmptyCard
      v-else-if="item.type === 'emptyCard'"
      :title="item.props?.title || '暂无数据'"
      :description="item.props?.description || '还没有任何内容，快来创建第一条吧'"
      :icon="item.props?.icon || 'folder'"
      :col-config="{ xs: 24, sm: 24, md: 24, lg: 24, xl: 24 }"
    />

    <!-- ==================== 展示组件 ==================== -->

    <!-- 图标组件 -->
    <Icon
      v-else-if="item.type === 'icon'"
      :code="(item.props?.code || 'star') as IconCode"
      :size="item.props?.size || 'lg'"
      :color="item.props?.color"
      :animate="item.props?.animate"
    />

    <!-- ==================== 独立图表组件 ==================== -->

    <!-- 折线图 -->
    <div v-else-if="item.type === 'lineChart'" class="chart-container">
      <div class="chart-header">
        <Icon code="chart" class="chart-icon" />
        <span class="chart-title">{{ item.props?.title || '折线图' }}</span>
      </div>
      <ALineChart
        :data="item.props?.data || [120, 200, 150, 80, 70, 110, 130]"
        :x-axis-data="item.props?.xAxisData || ['周一', '周二', '周三', '周四', '周五', '周六', '周日']"
        :height="(item.props?.height || 280) + 'px'"
        :smooth="item.props?.smooth !== false"
        :show-area-color="item.props?.showAreaColor"
        :show-legend="item.props?.showLegend"
        :show-tooltip="item.props?.showTooltip !== false"
      />
    </div>

    <!-- 柱状图 -->
    <div v-else-if="item.type === 'barChart'" class="chart-container">
      <div class="chart-header">
        <Icon code="histogram" class="chart-icon" />
        <span class="chart-title">{{ item.props?.title || '柱状图' }}</span>
      </div>
      <ABarChart
        :data="item.props?.data || [120, 200, 150, 80, 70, 110, 130]"
        :x-axis-data="item.props?.xAxisData || ['周一', '周二', '周三', '周四', '周五', '周六', '周日']"
        :height="(item.props?.height || 280) + 'px'"
        :stack="item.props?.stack"
        :show-legend="item.props?.showLegend"
        :show-tooltip="item.props?.showTooltip !== false"
      />
    </div>

    <!-- 饼图 -->
    <div v-else-if="item.type === 'pieChart'" class="chart-container">
      <div class="chart-header">
        <Icon code="pie-chart" class="chart-icon" />
        <span class="chart-title">{{ item.props?.title || '饼图' }}</span>
      </div>
      <APieChart
        :data="item.props?.data || [
          { name: '直接访问', value: 335 },
          { name: '邮件营销', value: 310 },
          { name: '联盟广告', value: 234 },
          { name: '视频广告', value: 135 }
        ]"
        :height="(item.props?.height || 280) + 'px'"
        :radius="item.props?.radius"
        :show-label="item.props?.showLabel"
        :show-legend="item.props?.showLegend"
        :show-tooltip="item.props?.showTooltip !== false"
      />
    </div>

    <!-- 雷达图 -->
    <div v-else-if="item.type === 'radarChart'" class="chart-container">
      <div class="chart-header">
        <Icon code="data-analysis" class="chart-icon" />
        <span class="chart-title">{{ item.props?.title || '雷达图' }}</span>
      </div>
      <ARadarChart
        :indicator="item.props?.indicator || [
          { name: '销售', max: 100 },
          { name: '管理', max: 100 },
          { name: '技术', max: 100 },
          { name: '客服', max: 100 },
          { name: '研发', max: 100 }
        ]"
        :data="item.props?.data || [
          { name: '预算分配', value: [80, 90, 85, 75, 95] }
        ]"
        :height="(item.props?.height || 280) + 'px'"
        :show-legend="item.props?.showLegend"
        :show-tooltip="item.props?.showTooltip !== false"
      />
    </div>

    <!-- 散点图 -->
    <div v-else-if="item.type === 'scatterChart'" class="chart-container">
      <div class="chart-header">
        <Icon code="scatter" class="chart-icon" />
        <span class="chart-title">{{ item.props?.title || '散点图' }}</span>
      </div>
      <AScatterChart
        :data="item.props?.data || [
          { value: [10, 20] },
          { value: [15, 34] },
          { value: [20, 28] },
          { value: [25, 42] },
          { value: [30, 35] },
          { value: [35, 48] },
          { value: [40, 38] }
        ]"
        :height="(item.props?.height || 280) + 'px'"
        :symbol-size="item.props?.symbolSize || 14"
        :show-tooltip="item.props?.showTooltip !== false"
      />
    </div>

    <!-- 地图 (使用 AMapChart) -->
    <div v-else-if="item.type === 'mapChart'" class="chart-container">
      <div class="chart-header">
        <Icon code="location" class="chart-icon" />
        <span class="chart-title">{{ item.props?.title || '地图' }}</span>
      </div>
      <AMapChart
        :height="(item.props?.height || 350) + 'px'"
        :map-data="item.props?.mapData || [
          { name: '北京', value: 100, adcode: '110000', level: 'province' },
          { name: '上海', value: 200, adcode: '310000', level: 'province' },
          { name: '广东', value: 300, adcode: '440000', level: 'province' }
        ]"
      />
    </div>

    <!-- ==================== 布局组件 ==================== -->

    <!-- 行容器 - 预览模式 -->
    <template v-else-if="item.type === 'row' && previewMode">
      <el-row :gutter="item.props?.gutter || 20" :justify="item.props?.justify" :align="item.props?.align">
        <template v-if="item.children && item.children.length > 0">
          <el-col v-for="child in item.children" :key="child.id" :span="child.type === 'col' ? child.props?.span || 12 : 24">
            <ComponentRenderer :item="child" :form-data="formData" :preview-mode="true" />
          </el-col>
        </template>
      </el-row>
    </template>

    <!-- 列容器 - 预览模式（直接渲染子组件） -->
    <template v-else-if="item.type === 'col' && previewMode">
      <template v-if="item.children && item.children.length > 0">
        <ComponentRenderer v-for="child in item.children" :key="child.id" :item="child" :form-data="formData" :preview-mode="true" />
      </template>
    </template>

    <!-- 行容器 - 设计模式 -->
    <div
      v-else-if="item.type === 'row'"
      class="layout-container row-container"
      :class="{ 'is-drag-over': isContainerDragOver }"
      @dragover.prevent.stop="handleContainerDragOver"
      @dragleave="handleContainerDragLeave"
      @drop.stop="handleContainerDrop"
    >
      <div class="container-header">
        <Icon code="rows" class="container-icon" />
        <span class="container-title">行容器 (Row)</span>
        <span class="container-props"> gutter: {{ item.props?.gutter || 20 }} | justify: {{ item.props?.justify || 'start' }} </span>
      </div>
      <div
        class="container-content row-content"
        :style="{
          justifyContent: getFlexJustify(item.props?.justify),
          alignItems: getFlexAlign(item.props?.align)
        }"
      >
        <!-- 子组件渲染 -->
        <template v-if="item.children && item.children.length > 0">
          <div
            v-for="child in item.children"
            :key="child.id"
            class="child-item"
            :class="{ 'is-selected': selectedId === child.id }"
            @click.stop="$emit('select-child', child.id)"
          >
            <ComponentRenderer
              :item="child"
              :form-data="formData"
              :selected-id="selectedId"
              @select-child="(id) => $emit('select-child', id)"
              @remove-child="(id) => $emit('remove-child', id)"
              @add-to-container="(containerId, type) => $emit('add-to-container', containerId, type)"
            />
            <div v-if="selectedId === child.id" class="child-actions">
              <el-button size="small" :icon="Delete" title="删除" @click.stop="$emit('remove-child', child.id)" />
            </div>
          </div>
        </template>
        <!-- 空状态占位 -->
        <div v-else class="container-placeholder">
          <Icon code="plus" />
          <span>拖拽组件到此处</span>
        </div>
      </div>
    </div>

    <!-- 列容器 - 设计模式 -->
    <div
      v-else-if="item.type === 'col'"
      class="layout-container col-container"
      :class="{ 'is-drag-over': isContainerDragOver }"
      @dragover.prevent.stop="handleContainerDragOver"
      @dragleave="handleContainerDragLeave"
      @drop.stop="handleContainerDrop"
    >
      <div class="container-header">
        <Icon code="columns" class="container-icon" />
        <span class="container-title">列容器 (Col)</span>
        <span class="container-props">
          span: {{ item.props?.span || 12 }}/24
          <template v-if="item.props?.offset"> | offset: {{ item.props?.offset }}</template>
        </span>
      </div>
      <div class="container-content col-content">
        <!-- 子组件渲染 -->
        <template v-if="item.children && item.children.length > 0">
          <div
            v-for="child in item.children"
            :key="child.id"
            class="child-item"
            :class="{ 'is-selected': selectedId === child.id }"
            @click.stop="$emit('select-child', child.id)"
          >
            <ComponentRenderer
              :item="child"
              :form-data="formData"
              :selected-id="selectedId"
              @select-child="(id) => $emit('select-child', id)"
              @remove-child="(id) => $emit('remove-child', id)"
              @add-to-container="(containerId, type) => $emit('add-to-container', containerId, type)"
            />
            <div v-if="selectedId === child.id" class="child-actions">
              <el-button size="small" :icon="Delete" title="删除" @click.stop="$emit('remove-child', child.id)" />
            </div>
          </div>
        </template>
        <!-- 空状态占位 -->
        <div v-else class="container-placeholder">
          <Icon code="plus" />
          <span>拖拽组件到此处</span>
        </div>
      </div>
    </div>

    <!-- 分割线 -->
    <el-divider
      v-else-if="item.type === 'divider'"
      :content-position="item.props?.contentPosition || 'center'"
      :direction="item.props?.direction || 'horizontal'"
    >
      {{ item.props?.content }}
    </el-divider>

    <!-- 提示框 -->
    <el-alert
      v-else-if="item.type === 'alert'"
      :title="item.props?.title || '提示信息'"
      :description="item.props?.description"
      :type="item.props?.type || 'info'"
      :closable="item.props?.closable !== false"
      :show-icon="item.props?.showIcon !== false"
    />

    <!-- 折叠面板 -->
    <el-collapse v-else-if="item.type === 'collapse'" :accordion="item.props?.accordion">
      <el-collapse-item :title="item.props?.title || '折叠面板'" name="1">
        <div class="collapse-placeholder">折叠面板内容区域</div>
      </el-collapse-item>
    </el-collapse>

    <!-- 未知非表单组件 -->
    <div v-else class="unknown-type">
      <el-tag type="warning">未知组件类型: {{ item.type }}</el-tag>
    </div>
  </template>

  <!-- 表单组件：需要 el-form-item 包裹 -->
  <!-- eslint-disable vue/no-mutating-props -->
  <el-form-item v-else :label="item.label" :prop="item.prop" :required="item.required">
    <!-- 单行输入 -->
    <AFormInput
      v-if="item.type === 'input'"
      v-model="formData[item.prop]"
      :show-form-item="false"
      :placeholder="item.placeholder || `请输入${item.label}`"
      :disabled="item.disabled"
      v-bind="item.props"
    />

    <!-- 多行文本 -->
    <AFormInput
      v-else-if="item.type === 'textarea'"
      v-model="formData[item.prop]"
      type="textarea"
      :show-form-item="false"
      :placeholder="item.placeholder || `请输入${item.label}`"
      :disabled="item.disabled"
      v-bind="item.props"
    />

    <!-- 密码输入 -->
    <AFormInput
      v-else-if="item.type === 'password'"
      v-model="formData[item.prop]"
      type="password"
      :show-form-item="false"
      :placeholder="item.placeholder || `请输入${item.label}`"
      :disabled="item.disabled"
      v-bind="item.props"
    />

    <!-- 数字输入 -->
    <el-input-number
      v-else-if="item.type === 'number'"
      v-model="formData[item.prop]"
      :placeholder="item.placeholder || `请输入${item.label}`"
      :disabled="item.disabled"
      controls-position=""
      v-bind="item.props"
      class="w-full"
    />

    <!-- 下拉选择 -->
    <AFormSelect
      v-else-if="item.type === 'select'"
      v-model="formData[item.prop]"
      :show-form-item="false"
      :placeholder="item.placeholder || `请选择${item.label}`"
      :disabled="item.disabled"
      :options="componentOptions"
      v-bind="item.props"
    />

    <!-- 单选框 -->
    <AFormRadio
      v-else-if="item.type === 'radio'"
      v-model="formData[item.prop]"
      :show-form-item="false"
      :disabled="item.disabled"
      :options="componentOptions"
      v-bind="item.props"
    />

    <!-- 复选框 -->
    <AFormCheckbox
      v-else-if="item.type === 'checkbox'"
      v-model="formData[item.prop]"
      :show-form-item="false"
      :disabled="item.disabled"
      :options="componentOptions"
      v-bind="item.props"
    />

    <!-- 开关 -->
    <AFormSwitch
      v-else-if="item.type === 'switch'"
      v-model="formData[item.prop]"
      :show-form-item="false"
      :disabled="item.disabled"
      v-bind="item.props"
    />

    <!-- 级联选择 -->
    <AFormCascader
      v-else-if="item.type === 'cascader'"
      v-model="formData[item.prop]"
      :show-form-item="false"
      :placeholder="item.placeholder || `请选择${item.label}`"
      :disabled="item.disabled"
      :options="item.options"
      v-bind="item.props"
    />

    <!-- 树形选择 -->
    <AFormTreeSelect
      v-else-if="item.type === 'treeSelect'"
      v-model="formData[item.prop]"
      :show-form-item="false"
      :placeholder="item.placeholder || `请选择${item.label}`"
      :disabled="item.disabled"
      :data="item.options"
      v-bind="item.props"
    />

    <!-- 日期选择 -->
    <AFormDate
      v-else-if="item.type === 'date'"
      v-model="formData[item.prop]"
      type="date"
      :show-form-item="false"
      :placeholder="item.placeholder || `请选择${item.label}`"
      :disabled="item.disabled"
      v-bind="item.props"
    />

    <!-- 日期时间 -->
    <AFormDate
      v-else-if="item.type === 'datetime'"
      v-model="formData[item.prop]"
      type="datetime"
      :show-form-item="false"
      :placeholder="item.placeholder || `请选择${item.label}`"
      :disabled="item.disabled"
      v-bind="item.props"
    />

    <!-- 日期范围 -->
    <AFormDate
      v-else-if="item.type === 'daterange'"
      v-model="formData[item.prop]"
      type="daterange"
      :show-form-item="false"
      :disabled="item.disabled"
      v-bind="item.props"
    />

    <!-- 日期时间范围 -->
    <AFormDate
      v-else-if="item.type === 'datetimerange'"
      v-model="formData[item.prop]"
      type="datetimerange"
      :show-form-item="false"
      :disabled="item.disabled"
      v-bind="item.props"
    />

    <!-- 时间选择 -->
    <el-time-picker
      v-else-if="item.type === 'time'"
      v-model="formData[item.prop]"
      :placeholder="item.placeholder || `请选择${item.label}`"
      :disabled="item.disabled"
      v-bind="item.props"
      class="w-full"
    />

    <!-- 图片上传 -->
    <AFormImgUpload
      v-else-if="item.type === 'imgUpload'"
      v-model="formData[item.prop]"
      :show-form-item="false"
      :disabled="item.disabled"
      v-bind="item.props"
    />

    <!-- 文件上传 -->
    <AFormFileUpload
      v-else-if="item.type === 'fileUpload'"
      v-model="formData[item.prop]"
      :show-form-item="false"
      :disabled="item.disabled"
      v-bind="item.props"
    />

    <!-- 富文本编辑器 -->
    <AFormEditor
      v-else-if="item.type === 'editor'"
      v-model="formData[item.prop]"
      :show-form-item="false"
      :placeholder="item.placeholder || '请输入内容...'"
      :height="(item.props?.height || 300) + 'px'"
      v-bind="item.props"
    />

    <!-- 未知表单类型 -->
    <div v-else class="unknown-type">
      <el-tag type="warning">未知组件类型: {{ item.type }}</el-tag>
    </div>
  </el-form-item>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { Delete } from '@element-plus/icons-vue'
import type { FormItemSchema, FormItemType } from '../types'
import { NON_FORM_COMPONENT_TYPES, CHART_COMPONENT_TYPES } from '../types'
import { useDict } from '@/composables/useDict'

defineOptions({ name: 'ComponentRenderer' })

const props = withDefaults(
  defineProps<{
    item: FormItemSchema
    formData: Record<string, any>
    selectedId?: string | null
    previewMode?: boolean // 预览模式：不显示容器设计时样式
  }>(),
  {
    selectedId: null,
    previewMode: false
  }
)

const emit = defineEmits<{
  (e: 'select-child', id: string): void
  (e: 'remove-child', id: string): void
  (e: 'add-to-container', containerId: string, type: FormItemType): void
}>()

// 容器拖拽状态
const isContainerDragOver = ref(false)

/** 容器拖拽进入 */
function handleContainerDragOver() {
  if (props.item.type === 'row' || props.item.type === 'col') {
    isContainerDragOver.value = true
  }
}

/** 容器拖拽离开 */
function handleContainerDragLeave() {
  isContainerDragOver.value = false
}

/** 容器拖拽放下 */
function handleContainerDrop(event: DragEvent) {
  isContainerDragOver.value = false
  const type = event.dataTransfer?.getData('componentType') as FormItemType
  if (type) {
    emit('add-to-container', props.item.id, type)
  }
}

/** 是否为非表单组件 */
const isNonFormComponent = computed(() => {
  return NON_FORM_COMPONENT_TYPES.includes(props.item.type)
})

// 字典数据响应式对象
const dictDataMap = ref<Record<string, any[]>>({})

// 加载字典数据
function loadDictData(dictType: string) {
  if (!dictType || dictDataMap.value[dictType]) return

  // 使用 useDict 获取字典数据
  const dictResult = useDict(dictType)

  // 监听加载完成（dictLoading 是 Ref<boolean>）
  watch(
    dictResult.dictLoading,
    (loading) => {
      if (!loading) {
        // 加载完成后获取数据（dictResult[dictType] 是 Ref<DictItem[]>）
        const dataRef = dictResult[dictType] as any
        const data = dataRef?.value ?? dataRef
        if (data && Array.isArray(data)) {
          dictDataMap.value[dictType] = data
        }
      }
    },
    { immediate: true }
  )
}

// 监听字典类型变化，动态加载字典数据
watch(
  () => props.item.props?.dictType,
  (dictType) => {
    if (dictType) {
      loadDictData(dictType)
    }
  },
  { immediate: true }
)

// 获取组件的选项数据（优先使用字典）
const componentOptions = computed(() => {
  const dictType = props.item.props?.dictType
  if (dictType && dictDataMap.value[dictType]?.length > 0) {
    return dictDataMap.value[dictType]
  }
  return props.item.options || []
})

/** 是否为独立图表组件 */
const isChartComponent = computed(() => {
  return CHART_COMPONENT_TYPES.includes(props.item.type)
})

/** 获取图表类型名称 */
function getChartTypeName(type: FormItemType): string {
  const nameMap: Record<string, string> = {
    lineChart: '折线图',
    barChart: '柱状图',
    pieChart: '饼图',
    radarChart: '雷达图',
    scatterChart: '散点图',
    mapChart: '地图'
  }
  return nameMap[type] || '图表'
}

/** 获取图表图标 */
function getChartIcon(type: FormItemType): string {
  const iconMap: Record<string, string> = {
    lineChart: 'chart',
    barChart: 'histogram',
    pieChart: 'pie-chart',
    radarChart: 'data-analysis',
    scatterChart: 'scatter',
    mapChart: 'location'
  }
  return iconMap[type] || 'chart'
}

/** 获取 flex justify-content 值 */
function getFlexJustify(justify?: string): string {
  const map: Record<string, string> = {
    start: 'flex-start',
    center: 'center',
    end: 'flex-end',
    'space-between': 'space-between',
    'space-around': 'space-around',
    'space-evenly': 'space-evenly'
  }
  return map[justify || 'start'] || 'flex-start'
}

/** 获取 flex align-items 值 */
function getFlexAlign(align?: string): string {
  const map: Record<string, string> = {
    top: 'flex-start',
    middle: 'center',
    bottom: 'flex-end'
  }
  return map[align || 'top'] || 'flex-start'
}
</script>

<style scoped lang="scss">
.w-full {
  width: 100%;
}

.unknown-type {
  padding: 20px;
  text-align: center;
}

// ==================== 图表容器 ====================
.chart-container {
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  overflow: hidden;

  .chart-header {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 16px;
    border-bottom: 1px solid var(--el-border-color-lighter);

    .chart-icon {
      font-size: 16px;
      color: var(--el-color-primary);
    }

    .chart-title {
      font-size: 14px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }
  }
}


// ==================== 布局容器 ====================
.layout-container {
  background: var(--el-bg-color);
  border: 2px dashed var(--el-color-primary-light-5);
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.2s;

  &.is-drag-over {
    border-color: var(--el-color-primary);
    background: var(--el-fill-color);
  }

  .container-header {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 12px;
    background: var(--el-fill-color-light);
    border-bottom: 1px solid var(--el-border-color-lighter);

    .container-icon {
      font-size: 16px;
      color: var(--el-color-primary);
    }

    .container-title {
      font-size: 13px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }

    .container-props {
      margin-left: auto;
      font-size: 11px;
      color: var(--el-text-color-secondary);
      font-family: monospace;
    }
  }

  .container-content {
    min-height: 80px;
    padding: 12px;
  }

  .row-content {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  .col-content {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .child-item {
    position: relative;
    padding: 4px;
    border: 1px solid transparent;
    border-radius: 4px;
    transition: all 0.2s;

    &:hover {
      border-color: var(--el-border-color);
    }

    &.is-selected {
      border-color: var(--el-color-primary);
      background: var(--el-fill-color);
    }

    .child-actions {
      position: absolute;
      top: 4px;
      right: 4px;
      z-index: 10;
    }
  }

  .container-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    min-height: 60px;
    color: var(--el-text-color-placeholder);
    font-size: 12px;
    gap: 4px;
  }
}

// ==================== 折叠面板占位 ====================
.collapse-placeholder {
  padding: 20px;
  text-align: center;
  color: var(--el-text-color-secondary);
  background: var(--el-fill-color-lighter);
  border-radius: 4px;
}

</style>
