<!-- AI对话测试 -->
<template>
  <div class="ai-test-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">AI助手组件</h2>
      <p class="page-description">展示AI助手的各种使用场景和功能</p>
    </div>

    <el-row :gutter="20">
      <el-col :span="24">
        <el-card header="功能概览" class="overview-card">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="支持功能">
              <el-tag type="success">文本优化</el-tag>
              <el-tag type="primary" class="ml-2">数据生成</el-tag>
              <el-tag type="warning" class="ml-2">内容审核</el-tag>
              <el-tag type="info" class="ml-2">文本翻译</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="默认模型">
              <el-tag type="primary">DeepSeek Chat</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="接口类型">
              <el-tag>同步POST请求</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="响应时间">
              <el-tag type="success">1-3秒</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-4">
      <!-- 文本优化示例 -->
      <el-col :lg="12" :md="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <Icon name="edit" />
              <span class="ml-2">文本优化</span>
            </div>
          </template>

          <el-form label-width="100px">
            <el-form-item label="原始文本">
              <el-input v-model="optimizeForm.text" type="textarea" :rows="4" placeholder="输入需要优化的文本..." />
            </el-form-item>

            <el-form-item label="优化类型">
              <el-select v-model="optimizeForm.type" style="width: 100%">
                <el-option label="润色优化" value="polish" />
                <el-option label="扩写补充" value="expand" />
                <el-option label="精简压缩" value="shorten" />
                <el-option label="正式化" value="formal" />
                <el-option label="口语化" value="casual" />
                <el-option label="营销文案" value="marketing" />
              </el-select>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="optimizeLoading" :icon="optimizeLoading ? 'Loading' : 'MagicStick'" @click="handleOptimize">
                {{ optimizeLoading ? '优化中...' : '开始优化' }}
              </el-button>
              <el-button @click="resetOptimize">重置</el-button>
            </el-form-item>

            <el-form-item label="优化结果" v-if="optimizeResult">
              <el-input v-model="optimizeResult" type="textarea" :rows="4" readonly />
              <div class="token-info" v-if="optimizeTokenUsage">
                <el-tag size="small">
                  <Icon name="cpu" />
                  输入: {{ optimizeTokenUsage.promptTokens }} tokens
                </el-tag>
                <el-tag type="success" size="small">
                  <Icon name="cpu" />
                  输出: {{ optimizeTokenUsage.completionTokens }} tokens
                </el-tag>
                <el-tag type="info" size="small">
                  <Icon name="timer" />
                  {{ optimizeResponseTime }}ms
                </el-tag>
              </div>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 数据生成示例 -->
      <el-col :lg="12" :md="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <Icon name="table" />
              <span class="ml-2">数据生成</span>
            </div>
          </template>

          <el-form label-width="100px">
            <el-form-item label="生成数量">
              <el-input-number v-model="generateForm.count" :min="1" :max="20" style="width: 100%" />
            </el-form-item>

            <el-form-item label="数据类型">
              <el-select v-model="generateForm.dataType" style="width: 100%">
                <el-option label="真实数据" value="realistic" />
                <el-option label="测试数据" value="test" />
                <el-option label="演示数据" value="demo" />
              </el-select>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="generateLoading" :icon="generateLoading ? 'Loading' : 'MagicStick'" @click="handleGenerate">
                {{ generateLoading ? '生成中...' : '生成数据' }}
              </el-button>
              <el-button @click="resetGenerate">重置</el-button>
            </el-form-item>

            <el-form-item label="生成结果" v-if="generateResult.length > 0">
              <el-table :data="generateResult" border size="small" max-height="300">
                <el-table-column prop="name" label="姓名" width="100" />
                <el-table-column prop="age" label="年龄" width="80" />
                <el-table-column prop="email" label="邮箱" min-width="180" />
                <el-table-column prop="city" label="城市" width="100" />
              </el-table>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-4">
      <!-- 内容审核示例 -->
      <el-col :lg="12" :md="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <Icon name="view" />
              <span class="ml-2">内容审核</span>
            </div>
          </template>

          <el-form label-width="100px">
            <el-form-item label="标题">
              <el-input v-model="reviewForm.title" placeholder="输入标题..." />
            </el-form-item>

            <el-form-item label="内容">
              <el-input v-model="reviewForm.content" type="textarea" :rows="3" placeholder="输入内容..." />
            </el-form-item>

            <el-form-item label="审核级别">
              <el-radio-group v-model="reviewForm.level">
                <el-radio value="loose">宽松</el-radio>
                <el-radio value="normal">正常</el-radio>
                <el-radio value="strict">严格</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="reviewLoading" :icon="reviewLoading ? 'Loading' : 'View'" @click="handleReview">
                {{ reviewLoading ? '审核中...' : '开始审核' }}
              </el-button>
              <el-button @click="resetReview">重置</el-button>
            </el-form-item>

            <el-form-item label="审核结果" v-if="reviewResult">
              <el-alert
                :title="`审核状态: ${getReviewStatusText(reviewResult.status)}`"
                :type="getReviewStatusType(reviewResult.status)"
                :description="`评分: ${reviewResult.score}/100`"
                show-icon
              />
              <div v-if="reviewResult.issues && reviewResult.issues.length > 0" class="mt-2">
                <el-tag v-for="(issue, index) in reviewResult.issues" :key="index" type="warning" class="mr-2 mb-2">
                  {{ issue.field }}: {{ issue.message }}
                </el-tag>
              </div>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 翻译示例 -->
      <el-col :lg="12" :md="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <Icon name="switch" />
              <span class="ml-2">文本翻译</span>
            </div>
          </template>

          <el-form label-width="100px">
            <el-form-item label="原文">
              <el-input v-model="translateForm.text" type="textarea" :rows="4" placeholder="输入需要翻译的文本..." />
            </el-form-item>

            <el-form-item label="目标语言">
              <el-select v-model="translateForm.targetLang" style="width: 100%">
                <el-option label="英文" value="英文">
                  <span>🇬🇧 英文</span>
                </el-option>
                <el-option label="日文" value="日文">
                  <span>🇯🇵 日文</span>
                </el-option>
                <el-option label="韩文" value="韩文">
                  <span>🇰🇷 韩文</span>
                </el-option>
                <el-option label="法文" value="法文">
                  <span>🇫🇷 法文</span>
                </el-option>
                <el-option label="德文" value="德文">
                  <span>🇩🇪 德文</span>
                </el-option>
                <el-option label="中文" value="中文">
                  <span>🇨🇳 中文</span>
                </el-option>
              </el-select>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="translateLoading" :icon="translateLoading ? 'Loading' : 'Switch'" @click="handleTranslate">
                {{ translateLoading ? '翻译中...' : '开始翻译' }}
              </el-button>
              <el-button @click="resetTranslate">重置</el-button>
            </el-form-item>

            <el-form-item label="翻译结果" v-if="translateResult">
              <el-input v-model="translateResult" type="textarea" :rows="4" readonly />
              <div class="token-info" v-if="translateTokenUsage">
                <el-tag size="small">
                  <Icon name="cpu" />
                  输入: {{ translateTokenUsage.promptTokens }} tokens
                </el-tag>
                <el-tag type="success" size="small">
                  <Icon name="cpu" />
                  输出: {{ translateTokenUsage.completionTokens }} tokens
                </el-tag>
                <el-tag type="info" size="small">
                  <Icon name="timer" />
                  {{ translateResponseTime }}ms
                </el-tag>
              </div>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <!-- 使用说明 -->
    <el-row :gutter="20" class="mt-4">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <Icon name="document" />
              <span class="ml-2">使用说明</span>
            </div>
          </template>

          <el-collapse>
            <el-collapse-item title="📖 快速开始" name="1">
              <el-steps :active="1" finish-status="success">
                <el-step title="配置API Key" description="在application-dev.yml中配置DeepSeek API Key" />
                <el-step title="调用接口" description="使用useAiChat等composables调用AI功能" />
                <el-step title="处理响应" description="获取AI返回的内容和Token使用情况" />
              </el-steps>
            </el-collapse-item>

            <el-collapse-item title="💻 代码示例" name="2">
              <pre class="code-block">
<code>// 文本优化
import { useAiTextOptimize } from '@/composables/useAiChat'

const { optimize, loading } = useAiTextOptimize()
const result = await optimize('原始文本', 'polish')

// 数据生成
import { useAiDataGenerate } from '@/composables/useAiChat'
import type { FieldSchema } from '@/api/business/base/ai/aiTypes'

const { generate } = useAiDataGenerate()
const schema: FieldSchema[] = [
  { name: 'name', type: 'string', label: '姓名' },
  { name: 'age', type: 'number', label: '年龄' }
]
const data = await generate(schema, 10)</code>
              </pre>
            </el-collapse-item>

            <el-collapse-item title="📊 接口说明" name="3">
              <el-table :data="apiList" border>
                <el-table-column prop="name" label="接口名称" width="150" />
                <el-table-column prop="path" label="请求路径" width="200" />
                <el-table-column prop="method" label="请求方法" width="100">
                  <template #default="{ row }">
                    <el-tag type="success">{{ row.method }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="description" label="功能说明" />
              </el-table>
            </el-collapse-item>

            <el-collapse-item title="⚙️ 配置说明" name="4">
              <el-alert title="配置DeepSeek API Key" type="info" :closable="false">
                <template #default>
                  <p>在 <code>application-dev.yml</code> 中配置:</p>
                  <pre class="code-block mt-2">
<code>langchain4j:
  deepseek:
    api-key: sk-your-api-key-here
    base-url: https://api.deepseek.com/v1
    model-name: deepseek-chat</code>
                  </pre>
                </template>
              </el-alert>
            </el-collapse-item>
          </el-collapse>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts" name="TestAi">
import { ref } from 'vue'
import { showMsgWarning, showMsgSuccess, showMsgError } from '@/utils/modal'
import { useAiTextOptimize, useAiDataGenerate, useAiContentReview, useAiTranslate } from '@/composables/useAiChat'
import type { FieldSchema } from '@/api/business/base/ai/aiTypes'

// ========== 文本优化 ==========
const optimizeForm = ref({
  text: '我们的产品很好用,大家都喜欢',
  type: 'polish'
})
const {
  optimize,
  loading: optimizeLoading,
  response: optimizeResult,
  tokenUsage: optimizeTokenUsage,
  responseTime: optimizeResponseTime
} = useAiTextOptimize()

const handleOptimize = async () => {
  if (!optimizeForm.value.text.trim()) {
    showMsgWarning('请输入需要优化的文本')
    return
  }

  try {
    await optimize(optimizeForm.value.text, optimizeForm.value.type as any)
    showMsgSuccess('优化成功')
  } catch (error: any) {
    showMsgError(`优化失败: ${error.message || '未知错误'}`)
  }
}

const resetOptimize = () => {
  optimizeForm.value.text = '我们的产品很好用,大家都喜欢'
  optimizeForm.value.type = 'polish'
  optimizeResult.value = ''
}

// ========== 数据生成 ==========
const generateForm = ref({
  count: 5,
  dataType: 'realistic'
})
const { generate, loading: generateLoading } = useAiDataGenerate()
const generateResult = ref<any[]>([])

const handleGenerate = async () => {
  const schema: FieldSchema[] = [
    { name: 'name', type: 'string', label: '姓名', required: true },
    { name: 'age', type: 'number', label: '年龄' },
    { name: 'email', type: 'string', label: '邮箱', example: 'test@example.com' },
    { name: 'city', type: 'string', label: '城市' }
  ]

  try {
    generateResult.value = await generate(schema, generateForm.value.count, {
      dataType: generateForm.value.dataType as any
    })
    showMsgSuccess(`成功生成 ${generateResult.value.length} 条数据`)
  } catch (error: any) {
    showMsgError(`生成失败: ${error.message || '未知错误'}`)
  }
}

const resetGenerate = () => {
  generateForm.value.count = 5
  generateForm.value.dataType = 'realistic'
  generateResult.value = []
}

// ========== 内容审核 ==========
const reviewForm = ref({
  title: '测试文章标题',
  content: '这是一篇测试文章的内容,用于演示AI内容审核功能。',
  level: 'normal'
})
const { review, loading: reviewLoading } = useAiContentReview()
const reviewResult = ref<any>(null)

const handleReview = async () => {
  if (!reviewForm.value.title.trim() || !reviewForm.value.content.trim()) {
    showMsgWarning('请输入标题和内容')
    return
  }

  try {
    reviewResult.value = await review(
      {
        标题: reviewForm.value.title,
        内容: reviewForm.value.content
      },
      {
        level: reviewForm.value.level as any,
        checkItems: ['compliance', 'sensitive', 'quality'],
        autoFix: true
      }
    )
    showMsgSuccess('审核完成')
  } catch (error: any) {
    showMsgError(`审核失败: ${error.message || '未知错误'}`)
  }
}

const resetReview = () => {
  reviewForm.value.title = '测试文章标题'
  reviewForm.value.content = '这是一篇测试文章的内容,用于演示AI内容审核功能。'
  reviewForm.value.level = 'normal'
  reviewResult.value = null
}

const getReviewStatusText = (status: string) => {
  const map: Record<string, string> = {
    pass: '通过',
    warning: '警告',
    reject: '拒绝'
  }
  return map[status] || status
}

const getReviewStatusType = (status: string): any => {
  const map: Record<string, string> = {
    pass: 'success',
    warning: 'warning',
    reject: 'error'
  }
  return map[status] || 'info'
}

// ========== 翻译 ==========
const translateForm = ref({
  text: '你好,世界!欢迎使用AI助手功能。',
  targetLang: '英文'
})
const {
  translate,
  loading: translateLoading,
  response: translateResult,
  tokenUsage: translateTokenUsage,
  responseTime: translateResponseTime
} = useAiTranslate()

const handleTranslate = async () => {
  if (!translateForm.value.text.trim()) {
    showMsgWarning('请输入需要翻译的文本')
    return
  }

  try {
    await translate(translateForm.value.text, translateForm.value.targetLang, {
      keepFormat: true
    })
    showMsgSuccess('翻译成功')
  } catch (error: any) {
    showMsgError(`翻译失败: ${error.message || '未知错误'}`)
  }
}

const resetTranslate = () => {
  translateForm.value.text = '你好,世界!欢迎使用AI助手功能。'
  translateForm.value.targetLang = '英文'
  translateResult.value = ''
}

// ========== API列表 ==========
const apiList = [
  { name: '统一对话', path: '/base/ai/aiChat', method: 'POST', description: '统一的AI对话接口,支持所有功能' },
  { name: '文本优化', path: '/base/ai/aiOptimize', method: 'POST', description: '优化、润色、扩写、精简文本' },
  { name: '数据生成', path: '/base/ai/aiGenerate', method: 'POST', description: '根据字段定义生成测试数据' },
  { name: '内容审核', path: '/base/ai/aiReview', method: 'POST', description: '审核内容质量、合规性、敏感词' },
  { name: '文本翻译', path: '/base/ai/aiTranslate', method: 'POST', description: '翻译文本到目标语言' }
]
</script>

<style scoped lang="scss">
.ai-test-page {
  padding: 20px;

  .page-header {
    margin-bottom: 24px;
  }

  .page-title {
    font-size: 24px;
    font-weight: 600;
    color: var(--el-text-color-primary);
    margin: 0 0 8px 0;
  }

  .page-description {
    font-size: 14px;
    color: var(--el-text-color-secondary);
    margin: 0;
  }

  .overview-card {
    margin-bottom: 20px;
  }

  .card-header {
    display: flex;
    align-items: center;
    font-weight: 600;
  }

  .token-info {
    margin-top: 10px;
    display: flex;
    gap: 10px;
    flex-wrap: wrap;

    .el-tag {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }

  .code-block {
    background: var(--el-fill-color-light);
    padding: 12px;
    border-radius: 4px;
    overflow-x: auto;
    font-family: 'Courier New', monospace;
    font-size: 13px;
    line-height: 1.6;

    code {
      color: var(--el-text-color-primary);
    }
  }

  .mt-2 {
    margin-top: 8px;
  }

  .mt-4 {
    margin-top: 16px;
  }

  .ml-2 {
    margin-left: 8px;
  }

  .mr-2 {
    margin-right: 8px;
  }

  .mb-2 {
    margin-bottom: 8px;
  }
}
</style>
