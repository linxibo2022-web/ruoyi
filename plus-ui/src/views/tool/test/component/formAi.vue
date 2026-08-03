<!-- AI表单测试 -->
<template>
  <div class="app-container">
    <el-alert title="AFormInputWithAi 组件示例" type="info" :closable="false" style="margin-bottom: 20px">
      <p>展示带 AI 优化功能的表单输入组件的各种用法</p>
    </el-alert>

    <el-tabs v-model="activeTab" type="border-card">
      <!-- 基础用法 -->
      <el-tab-pane label="基础用法" name="basic">
        <el-card header="基础 AI 优化">
          <el-form :model="basicForm" label-width="120px">
            <AFormInputWithAi
              label="文章标题"
              v-model="basicForm.title"
              prop="title"
              field-type="title"
              :maxlength="100"
              show-word-limit
              placeholder="请输入文章标题，点击右侧 AI 图标可优化"
            />

            <AFormInputWithAi
              label="文章描述"
              v-model="basicForm.description"
              prop="description"
              type="textarea"
              field-type="description"
              :rows="4"
              :maxlength="500"
              show-word-limit
              placeholder="请输入文章描述，点击右侧 AI 图标可优化"
            />

            <AFormInputWithAi
              label="备注信息"
              v-model="basicForm.remark"
              prop="remark"
              type="textarea"
              field-type="remark"
              :rows="3"
              placeholder="请输入备注信息"
            />
          </el-form>

          <el-divider content-position="left">预览数据</el-divider>
          <pre>{{ basicForm }}</pre>
        </el-card>
      </el-tab-pane>

      <!-- 快捷操作 -->
      <el-tab-pane label="快捷操作" name="quick">
        <el-card header="快捷操作按钮组">
          <el-alert title="快捷操作模式" type="success" :closable="false" style="margin-bottom: 16px">
            启用 <code>show-quick-actions</code> 可以显示快捷操作按钮组，提供一键优化功能
          </el-alert>

          <el-form :model="quickForm" label-width="120px">
            <AFormInputWithAi
              label="产品标题"
              v-model="quickForm.productTitle"
              prop="productTitle"
              field-type="title"
              :show-quick-actions="true"
              :quick-actions="['polish', 'expand', 'shorten']"
              placeholder="输入产品标题，使用快捷按钮优化"
            />

            <AFormInputWithAi
              label="产品描述"
              v-model="quickForm.productDesc"
              prop="productDesc"
              type="textarea"
              field-type="description"
              :rows="5"
              :show-quick-actions="true"
              :quick-actions="['polish', 'expand', 'shorten', 'formal', 'casual']"
              placeholder="输入产品描述，使用快捷按钮优化"
            />
          </el-form>

          <el-divider content-position="left">预览数据</el-divider>
          <pre>{{ quickForm }}</pre>
        </el-card>
      </el-tab-pane>

      <!-- 自动应用 -->
      <el-tab-pane label="自动应用" name="auto">
        <el-card header="AI 结果自动应用">
          <el-alert title="自动应用模式" type="warning" :closable="false" style="margin-bottom: 16px">
            设置 <code>:ai-config="{ autoApply: true }"</code> 后，AI 优化结果会自动应用，无需确认
          </el-alert>

          <el-form :model="autoForm" label-width="120px">
            <AFormInputWithAi
              label="快速标题"
              v-model="autoForm.title"
              prop="title"
              field-type="title"
              :ai-config="{ autoApply: true }"
              placeholder="输入后点击 AI 图标，结果会自动应用"
            />

            <AFormInputWithAi
              label="快速描述"
              v-model="autoForm.description"
              prop="description"
              type="textarea"
              field-type="description"
              :rows="4"
              :show-quick-actions="true"
              :ai-config="{ autoApply: true }"
              placeholder="使用快捷按钮，结果会自动应用"
            />
          </el-form>

          <el-divider content-position="left">预览数据</el-divider>
          <pre>{{ autoForm }}</pre>
        </el-card>
      </el-tab-pane>

      <!-- 自定义配置 -->
      <el-tab-pane label="自定义配置" name="custom">
        <el-card header="自定义 AI 配置">
          <el-alert title="自定义提示词" type="info" :closable="false" style="margin-bottom: 16px">
            通过 <code>ai-config</code> 可以自定义系统提示词、温度参数等 AI 配置
          </el-alert>

          <el-form :model="customForm" label-width="120px">
            <AFormInputWithAi
              label="营销文案"
              v-model="customForm.marketing"
              prop="marketing"
              type="textarea"
              :rows="4"
              field-type="custom"
              :ai-config="{
                systemPrompt: '你是专业的营销文案专家，请优化以下营销文案，使其更具吸引力和转化力。直接输出优化结果。',
                temperature: 0.9,
                autoApply: false
              }"
              placeholder="输入营销文案，AI 会以营销专家角度优化"
            />

            <AFormInputWithAi
              label="技术文档"
              v-model="customForm.technical"
              prop="technical"
              type="textarea"
              :rows="4"
              field-type="custom"
              :ai-config="{
                systemPrompt: '你是专业的技术文档撰写专家，请优化以下技术文档，使其更加严谨、准确、易懂。直接输出优化结果。',
                temperature: 0.3,
                autoApply: false
              }"
              placeholder="输入技术文档，AI 会以技术角度优化"
            />
          </el-form>

          <el-divider content-position="left">预览数据</el-divider>
          <pre>{{ customForm }}</pre>
        </el-card>
      </el-tab-pane>

      <!-- 事件监听 -->
      <el-tab-pane label="事件监听" name="events">
        <el-card header="监听 AI 事件">
          <el-alert title="事件说明" type="success" :closable="false" style="margin-bottom: 16px">
            组件提供 <code>@ai-start</code>、<code>@ai-result</code>、<code>@ai-error</code> 事件用于监听 AI 处理过程
          </el-alert>

          <el-form :model="eventForm" label-width="120px">
            <AFormInputWithAi
              label="监听示例"
              v-model="eventForm.content"
              prop="content"
              type="textarea"
              :rows="4"
              field-type="content"
              :show-quick-actions="true"
              @ai-start="handleAiStart"
              @ai-result="handleAiResult"
              @ai-error="handleAiError"
              placeholder="触发 AI 优化，查看下方的事件日志"
            />
          </el-form>

          <el-divider content-position="left">事件日志</el-divider>
          <el-timeline>
            <el-timeline-item v-for="(log, index) in eventLogs" :key="index" :timestamp="log.time" :type="log.type">
              <strong>{{ log.event }}</strong>
              <p v-if="log.data">{{ log.data }}</p>
            </el-timeline-item>
          </el-timeline>

          <el-button @click="eventLogs = []" size="small" style="margin-top: 10px">清空日志</el-button>
        </el-card>
      </el-tab-pane>

      <!-- 响应式布局 -->
      <el-tab-pane label="响应式布局" name="responsive">
        <el-card header="响应式布局 + AI">
          <el-form :model="responsiveForm" label-width="120px">
            <el-row :gutter="20">
              <AFormInputWithAi
                label="标题"
                v-model="responsiveForm.title"
                prop="title"
                :span="{ xs: 24, sm: 24, md: 12, lg: 8 }"
                field-type="title"
              />

              <AFormInputWithAi
                label="副标题"
                v-model="responsiveForm.subtitle"
                prop="subtitle"
                :span="{ xs: 24, sm: 24, md: 12, lg: 8 }"
                field-type="title"
              />

              <AFormInputWithAi
                label="关键词"
                v-model="responsiveForm.keywords"
                prop="keywords"
                :span="{ xs: 24, sm: 24, md: 12, lg: 8 }"
                field-type="custom"
              />
            </el-row>

            <AFormInputWithAi
              label="完整描述"
              v-model="responsiveForm.fullDesc"
              prop="fullDesc"
              type="textarea"
              :rows="4"
              field-type="description"
              :show-quick-actions="true"
            />
          </el-form>

          <el-divider content-position="left">预览数据</el-divider>
          <pre>{{ responsiveForm }}</pre>
        </el-card>
      </el-tab-pane>

      <!-- 手动触发 -->
      <el-tab-pane label="手动触发" name="manual">
        <el-card header="通过 Ref 手动触发 AI">
          <el-alert title="手动触发" type="info" :closable="false" style="margin-bottom: 16px">
            通过组件的 <code>ref</code> 可以手动调用 AI 优化方法
          </el-alert>

          <el-form :model="manualForm" label-width="120px">
            <AFormInputWithAi
              ref="manualInputRef"
              label="内容"
              v-model="manualForm.content"
              prop="content"
              type="textarea"
              :rows="4"
              field-type="content"
              placeholder="输入内容，使用下方按钮触发 AI 优化"
            />
          </el-form>

          <div style="margin-top: 16px">
            <el-button type="primary" @click="triggerAi">触发默认优化</el-button>
            <el-button type="success" @click="triggerQuickAi('polish')">触发润色</el-button>
            <el-button type="success" @click="triggerQuickAi('expand')">触发扩写</el-button>
            <el-button type="success" @click="triggerQuickAi('shorten')">触发精简</el-button>
            <el-button type="warning" @click="openAdvanced">打开高级优化</el-button>
          </div>

          <el-divider content-position="left">预览数据</el-divider>
          <pre>{{ manualForm }}</pre>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts" name="TestFormAi">
import { ref, reactive } from 'vue'
import type { OptimizeType } from '@/components/AForm/AFormInputWithAi'

/** 当前激活的标签页 */
const activeTab = ref('basic')

/** 基础表单 */
const basicForm = reactive({
  title: '如何快速学习Vue3开发',
  description: 'Vue3是一个现代化的前端框架，提供了组合式API、更好的性能和TypeScript支持。',
  remark: '这是一个关于Vue3学习的文章'
})

/** 快捷操作表单 */
const quickForm = reactive({
  productTitle: 'AI智能音箱',
  productDesc: '这款AI智能音箱拥有强大的语音识别功能，可以播放音乐、查询天气、控制智能家居设备。'
})

/** 自动应用表单 */
const autoForm = reactive({
  title: '前端性能优化指南',
  description: '本文介绍了多种前端性能优化技巧，包括代码分割、懒加载、CDN加速等。'
})

/** 自定义配置表单 */
const customForm = reactive({
  marketing: '我们的产品很好用，价格也很便宜，欢迎购买。',
  technical: '这个函数用来处理数据，输入参数是数组，返回处理后的结果。'
})

/** 事件监听表单 */
const eventForm = reactive({
  content: '这是一段需要优化的文本内容。'
})

/** 事件日志 */
const eventLogs = ref<Array<{ time: string; event: string; type: any; data?: string }>>([])

/** 响应式布局表单 */
const responsiveForm = reactive({
  title: 'Vue3响应式布局示例',
  subtitle: '结合AI优化的表单组件',
  keywords: 'Vue3, 响应式, AI, 组件',
  fullDesc: '本示例展示了如何在响应式布局中使用带AI功能的表单组件。'
})

/** 手动触发表单 */
const manualForm = reactive({
  content: '这是一段等待手动触发AI优化的文本内容。'
})

/** 手动触发的组件引用 */
const manualInputRef = ref()

/**
 * 处理 AI 开始事件
 */
const handleAiStart = (type: OptimizeType) => {
  eventLogs.value.unshift({
    time: new Date().toLocaleTimeString(),
    event: `AI 开始处理 - 类型: ${type}`,
    type: 'primary'
  })
}

/**
 * 处理 AI 结果事件
 */
const handleAiResult = (result: string) => {
  eventLogs.value.unshift({
    time: new Date().toLocaleTimeString(),
    event: 'AI 优化完成',
    type: 'success',
    data: `结果: ${result.substring(0, 50)}${result.length > 50 ? '...' : ''}`
  })
}

/**
 * 处理 AI 错误事件
 */
const handleAiError = (error: any) => {
  eventLogs.value.unshift({
    time: new Date().toLocaleTimeString(),
    event: 'AI 优化失败',
    type: 'danger',
    data: error.message || '未知错误'
  })
}

/**
 * 手动触发 AI 优化
 */
const triggerAi = () => {
  if (manualInputRef.value) {
    manualInputRef.value.triggerAi()
  }
}

/**
 * 手动触发快捷 AI 优化
 */
const triggerQuickAi = (type: OptimizeType) => {
  if (manualInputRef.value) {
    manualInputRef.value.triggerQuickAi(type)
  }
}

/**
 * 打开高级优化
 */
const openAdvanced = () => {
  if (manualInputRef.value) {
    manualInputRef.value.openAdvanced()
  }
}
</script>

<style scoped>
pre {
  background-color: var(--el-fill-color-light);
  padding: 12px;
  border-radius: 4px;
  font-size: 13px;
  max-height: 300px;
  overflow: auto;
}

code {
  background-color: var(--el-fill-color);
  padding: 2px 6px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
  color: var(--el-color-primary);
}

:deep(.el-timeline-item__timestamp) {
  font-size: 12px;
}
</style>
