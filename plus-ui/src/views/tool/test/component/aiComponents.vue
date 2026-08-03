<!-- AI组件测试 -->
<template>
  <div class="ai-components-demo">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">AI组件库演示</h2>
      <p class="page-description">展示 AAiTextOptimizer、AAiDataGenerator、AAiContentReviewer 三大核心组件的使用方法</p>
    </div>

    <!-- 组件选择标签页 -->
    <el-tabs v-model="activeTab" type="border-card" class="demo-tabs">
      <!-- 文本优化器组件 -->
      <el-tab-pane label="文本优化器" name="optimizer">
        <div class="component-section">
          <div class="section-header">
            <h3>AAiTextOptimizer 组件</h3>
            <p>提供文本润色、扩写、缩写、风格转换等功能</p>
          </div>

          <el-row :gutter="20">
            <!-- 组件演示 -->
            <el-col :lg="14" :md="24">
              <el-card header="组件演示" shadow="never">
                <AAiTextOptimizer
                  v-model:value="optimizerText"
                  :field-type="optimizerFieldType"
                  :optimize-type="optimizerType"
                  :provider="aiProvider"
                  :temperature="0.7"
                  @result="handleOptimizerResult"
                  @error="handleError"
                />
              </el-card>
            </el-col>

            <!-- 配置和说明 -->
            <el-col :lg="10" :md="24">
              <el-card header="配置选项" shadow="never">
                <el-form label-width="100px" size="small">
                  <el-form-item label="字段类型">
                    <el-select v-model="optimizerFieldType">
                      <el-option label="标题" value="title" />
                      <el-option label="描述" value="description" />
                      <el-option label="备注" value="remark" />
                      <el-option label="自定义" value="custom" />
                    </el-select>
                  </el-form-item>
                  <el-form-item label="默认优化类型">
                    <el-select v-model="optimizerType">
                      <el-option label="润色" value="polish" />
                      <el-option label="扩写" value="expand" />
                      <el-option label="缩写" value="shorten" />
                      <el-option label="正式化" value="formal" />
                      <el-option label="口语化" value="casual" />
                      <el-option label="营销化" value="marketing" />
                      <el-option label="SEO优化" value="seo" />
                    </el-select>
                  </el-form-item>
                  <el-form-item label="AI提供商">
                    <el-select v-model="aiProvider">
                      <el-option label="DeepSeek" value="deepseek" />
                      <el-option label="通义千问" value="qwen" />
                      <el-option label="OpenAI" value="openai" />
                    </el-select>
                  </el-form-item>
                </el-form>

                <el-divider content-position="left">代码示例</el-divider>
                <pre class="code-block"><code>&lt;AAiTextOptimizer
  v-model:value="text"
  field-type="title"
  optimize-type="polish"
  provider="deepseek"
  :temperature="0.7"
  @result="handleResult"
  @error="handleError"
/&gt;</code></pre>
              </el-card>

              <el-card header="Props说明" shadow="never" class="mt-3">
                <el-table :data="optimizerProps" size="small" border>
                  <el-table-column prop="prop" label="属性" width="120" />
                  <el-table-column prop="type" label="类型" width="100" />
                  <el-table-column prop="desc" label="说明" />
                </el-table>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </el-tab-pane>

      <!-- 数据生成器组件 -->
      <el-tab-pane label="数据生成器" name="generator">
        <div class="component-section">
          <div class="section-header">
            <h3>AAiDataGenerator 组件</h3>
            <p>根据字段定义智能生成测试数据、演示数据或真实感数据</p>
          </div>

          <el-row :gutter="20">
            <!-- 组件演示 -->
            <el-col :lg="14" :md="24">
              <el-card header="组件演示" shadow="never">
                <AAiDataGenerator
                  :schema="generatorSchema"
                  :count="generatorCount"
                  :provider="aiProvider"
                  @result="handleGeneratorResult"
                  @error="handleError"
                />
              </el-card>
            </el-col>

            <!-- 配置和说明 -->
            <el-col :lg="10" :md="24">
              <el-card header="字段配置" shadow="never">
                <el-form label-width="100px" size="small">
                  <el-form-item label="生成数量">
                    <el-input-number v-model="generatorCount" :min="1" :max="50" />
                  </el-form-item>
                  <el-form-item label="预设模板">
                    <el-select v-model="generatorTemplate" @change="handleTemplateChange">
                      <el-option label="用户信息" value="user" />
                      <el-option label="商品信息" value="product" />
                      <el-option label="订单信息" value="order" />
                      <el-option label="自定义" value="custom" />
                    </el-select>
                  </el-form-item>
                </el-form>

                <el-divider content-position="left">当前字段Schema</el-divider>
                <pre class="code-block"><code>{{ JSON.stringify(generatorSchema, null, 2) }}</code></pre>

                <el-divider content-position="left">代码示例</el-divider>
                <pre class="code-block"><code>const schema = [
  {
    name: 'name',
    label: '姓名',
    type: 'string',
    required: true
  },
  {
    name: 'age',
    label: '年龄',
    type: 'number'
  }
]

&lt;AAiDataGenerator
  :schema="schema"
  :count="10"
  provider="deepseek"
  @result="handleResult"
/&gt;</code></pre>
              </el-card>

              <el-card header="字段类型说明" shadow="never" class="mt-3">
                <el-table :data="generatorFieldTypes" size="small" border>
                  <el-table-column prop="type" label="类型" width="100" />
                  <el-table-column prop="desc" label="说明" />
                  <el-table-column prop="example" label="示例" />
                </el-table>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </el-tab-pane>

      <!-- 内容审核器组件 -->
      <el-tab-pane label="内容审核器" name="reviewer">
        <div class="component-section">
          <div class="section-header">
            <h3>AAiContentReviewer 组件</h3>
            <p>智能审核内容的合规性、质量、敏感词，并提供修复建议</p>
          </div>

          <el-row :gutter="20">
            <!-- 组件演示 -->
            <el-col :lg="14" :md="24">
              <el-card header="组件演示" shadow="never">
                <AAiContentReviewer
                  :content="reviewerContent"
                  :fields="reviewerFields"
                  :level="reviewerLevel"
                  :provider="aiProvider"
                  @result="handleReviewerResult"
                  @error="handleError"
                />
              </el-card>
            </el-col>

            <!-- 配置和说明 -->
            <el-col :lg="10" :md="24">
              <el-card header="审核配置" shadow="never">
                <el-form label-width="100px" size="small">
                  <el-form-item label="审核级别">
                    <el-radio-group v-model="reviewerLevel">
                      <el-radio value="loose">宽松</el-radio>
                      <el-radio value="normal">标准</el-radio>
                      <el-radio value="strict">严格</el-radio>
                    </el-radio-group>
                  </el-form-item>
                  <el-form-item label="内容模板">
                    <el-select v-model="reviewerTemplate" @change="handleReviewTemplateChange">
                      <el-option label="文章内容" value="article" />
                      <el-option label="商品描述" value="product" />
                      <el-option label="用户评论" value="comment" />
                      <el-option label="自定义" value="custom" />
                    </el-select>
                  </el-form-item>
                </el-form>

                <el-divider content-position="left">字段配置</el-divider>
                <pre class="code-block"><code>{{ JSON.stringify(reviewerFields, null, 2) }}</code></pre>

                <el-divider content-position="left">代码示例</el-divider>
                <pre class="code-block"><code>const fields = [
  { prop: 'title', label: '标题', type: 'text' },
  { prop: 'content', label: '内容', type: 'text' }
]

const content = {
  title: '文章标题',
  content: '文章内容...'
}

&lt;AAiContentReviewer
  :content="content"
  :fields="fields"
  level="normal"
  provider="deepseek"
  @result="handleResult"
/&gt;</code></pre>
              </el-card>

              <el-card header="审核项说明" shadow="never" class="mt-3">
                <el-table :data="reviewerCheckItems" size="small" border>
                  <el-table-column prop="item" label="审核项" width="120" />
                  <el-table-column prop="desc" label="说明" />
                </el-table>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </el-tab-pane>

      <!-- 综合示例 -->
      <el-tab-pane label="综合示例" name="example">
        <div class="component-section">
          <div class="section-header">
            <h3>实际应用场景</h3>
            <p>展示如何在表单中集成AI组件提升用户体验</p>
          </div>

          <el-row :gutter="20">
            <el-col :span="24">
              <el-card header="博客文章发布表单" shadow="never">
                <el-form :model="blogForm" label-width="100px">
                  <el-form-item label="文章标题">
                    <el-input v-model="blogForm.title" placeholder="输入文章标题">
                      <template #append>
                        <el-button @click="showOptimizer('title')">AI优化</el-button>
                      </template>
                    </el-input>
                  </el-form-item>

                  <el-form-item label="文章摘要">
                    <el-input v-model="blogForm.summary" type="textarea" :rows="2" placeholder="输入文章摘要">
                      <template #append>
                        <el-button @click="showOptimizer('summary')">AI优化</el-button>
                      </template>
                    </el-input>
                  </el-form-item>

                  <el-form-item label="文章内容">
                    <el-input v-model="blogForm.content" type="textarea" :rows="6" placeholder="输入文章内容" />
                  </el-form-item>

                  <el-form-item label="标签">
                    <el-tag v-for="tag in blogForm.tags" :key="tag" closable @close="removeTag(tag)" class="mr-2">
                      {{ tag }}
                    </el-tag>
                    <el-input
                      v-if="tagInputVisible"
                      ref="tagInputRef"
                      v-model="tagInputValue"
                      size="small"
                      style="width: 100px"
                      @keyup.enter="handleTagInputConfirm"
                      @blur="handleTagInputConfirm"
                    />
                    <el-button v-else size="small" @click="showTagInput">+ 添加标签</el-button>
                  </el-form-item>

                  <el-form-item>
                    <el-button type="primary" @click="reviewBlogForm">
                      <Icon name="view" />
                      内容审核
                    </el-button>
                    <el-button @click="resetBlogForm">重置</el-button>
                  </el-form-item>
                </el-form>

                <el-divider content-position="left">集成说明</el-divider>
                <el-alert type="info" :closable="false">
                  <p><strong>1. 文本优化集成</strong>：在输入框后添加"AI优化"按钮，点击弹窗显示 AAiTextOptimizer 组件</p>
                  <p class="mt-2"><strong>2. 内容审核集成</strong>：提交前调用 AAiContentReviewer 组件审核表单内容</p>
                  <p class="mt-2"><strong>3. 数据生成集成</strong>：为测试人员提供快速生成测试数据的功能</p>
                </el-alert>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 优化器弹窗 -->
    <AModal v-model="optimizerDialogVisible" title="AI文本优化" size="large" :show-footer="false">
      <AAiTextOptimizer v-model:value="currentOptimizeText" field-type="custom" provider="deepseek" @result="handleOptimizeDialogResult" />
    </AModal>

    <!-- 审核结果弹窗 -->
    <AModal v-model="reviewDialogVisible" title="内容审核结果" size="large" :show-footer="false">
      <AAiContentReviewer :content="blogForm" :fields="blogFormFields" level="normal" provider="deepseek" @review-complete="handleReviewComplete" />
    </AModal>
  </div>
</template>

<script setup lang="ts" name="TestAiComponents">
import { ref, nextTick } from 'vue'
import AAiTextOptimizer from '@/components/AAi/AAiTextOptimizer.vue'
import AAiDataGenerator from '@/components/AAi/AAiDataGenerator.vue'
import AAiContentReviewer from '@/components/AAi/AAiContentReviewer.vue'
import { showMsgSuccess, showMsgError, showMsg } from '@/utils/modal'
import type { FieldSchema } from '@/api/business/base/ai/aiTypes'
import { FieldConfig } from '@/types/global'

// ========== 标签页管理 ==========
const activeTab = ref('optimizer')

// ========== 通用配置 ==========
const aiProvider = ref('deepseek')

// ========== 文本优化器配置 ==========
const optimizerText = ref('我们的产品很好用,功能强大,价格实惠,欢迎购买!')
const optimizerFieldType = ref<'title' | 'description' | 'remark' | 'custom'>('custom')
const optimizerType = ref<'polish' | 'expand' | 'shorten' | 'formal' | 'casual' | 'marketing' | 'translate' | 'seo'>('polish')

const optimizerProps = [
  { prop: 'value', type: 'String', desc: '当前文本内容' },
  { prop: 'field-type', type: 'String', desc: '字段类型：title/description/remark/custom' },
  { prop: 'optimize-type', type: 'String', desc: '优化类型：polish/expand/shorten等' },
  { prop: 'provider', type: 'String', desc: 'AI提供商：deepseek/qwen/openai' },
  { prop: 'temperature', type: 'Number', desc: '温度参数，控制创造性（0-1）' }
]

const handleOptimizerResult = (result: string) => {
  showMsgSuccess('优化完成')
  console.log('优化结果:', result)
}

// ========== 数据生成器配置 ==========
const generatorCount = ref(5)
const generatorTemplate = ref('user')
const generatorSchema = ref<FieldSchema[]>([
  { name: 'name', label: '姓名', type: 'string', required: true },
  { name: 'age', label: '年龄', type: 'number' },
  { name: 'email', label: '邮箱', type: 'string', example: 'test@example.com' },
  { name: 'phone', label: '电话', type: 'string' }
])

const generatorFieldTypes = [
  { type: 'string', desc: '文本类型', example: '张三、产品名称' },
  { type: 'number', desc: '数字类型', example: '18、99.99' },
  { type: 'date', desc: '日期类型', example: '2025-01-26' },
  { type: 'image', desc: '图片类型', example: 'https://...' },
  { type: 'enum', desc: '枚举类型', example: '启用、禁用' }
]

const handleTemplateChange = (template: string) => {
  const templates: Record<string, FieldSchema[]> = {
    user: [
      { name: 'name', label: '姓名', type: 'string', required: true },
      { name: 'age', label: '年龄', type: 'number' },
      { name: 'email', label: '邮箱', type: 'string' },
      { name: 'phone', label: '电话', type: 'string' }
    ],
    product: [
      { name: 'productName', label: '商品名称', type: 'string', required: true },
      { name: 'price', label: '价格', type: 'number' },
      { name: 'category', label: '分类', type: 'string' },
      { name: 'stock', label: '库存', type: 'number' }
    ],
    order: [
      { name: 'orderNo', label: '订单号', type: 'string', required: true },
      { name: 'amount', label: '金额', type: 'number' },
      { name: 'status', label: '状态', type: 'enum', options: ['待支付', '已支付', '已发货', '已完成'] },
      { name: 'createTime', label: '创建时间', type: 'date' }
    ]
  }
  generatorSchema.value = templates[template] || generatorSchema.value
}

const handleGeneratorResult = (data: any[]) => {
  showMsgSuccess(`成功生成 ${data.length} 条数据`)
  console.log('生成的数据:', data)
}

// ========== 内容审核器配置 ==========
const reviewerLevel = ref<'loose' | 'normal' | 'strict'>('normal')
const reviewerTemplate = ref('article')
const reviewerContent = ref({
  title: '如何选择合适的编程语言',
  content: '编程语言的选择对项目成功至关重要。Python适合数据分析,Java适合企业应用...'
})
const reviewerFields = ref<FieldConfig[]>([
  { prop: 'title', label: '标题', type: 'text' },
  { prop: 'content', label: '内容', type: 'text' }
])

const reviewerCheckItems = [
  { item: 'compliance', desc: '合规性检查，确保符合广告法、平台规范' },
  { item: 'sensitive', desc: '敏感词检测，识别违禁词、不当表达' },
  { item: 'quality', desc: '质量评估，评估文案质量和专业性' },
  { item: 'completeness', desc: '完整性检查，检查必填项和信息完整度' },
  { item: 'format', desc: '格式验证，检查格式规范和链接有效性' }
]

const handleReviewTemplateChange = (template: string) => {
  const templates: Record<string, any> = {
    article: {
      content: { title: '如何选择合适的编程语言', content: '编程语言的选择对项目成功至关重要...' },
      fields: [
        { prop: 'title', label: '标题', type: 'text' },
        { prop: 'content', label: '内容', type: 'text' }
      ]
    },
    product: {
      content: { productName: '智能手机', description: '高性能处理器,超长续航,拍照清晰' },
      fields: [
        { prop: 'productName', label: '商品名称', type: 'text' },
        { prop: 'description', label: '商品描述', type: 'text' }
      ]
    },
    comment: {
      content: { userName: '张三', comment: '这个产品真的很好用,强烈推荐!' },
      fields: [
        { prop: 'userName', label: '用户名', type: 'text' },
        { prop: 'comment', label: '评论内容', type: 'text' }
      ]
    }
  }
  const tpl = templates[template]
  if (tpl) {
    reviewerContent.value = tpl.content
    reviewerFields.value = tpl.fields
  }
}

const handleReviewerResult = (result: any) => {
  showMsgSuccess('审核完成')
  console.log('审核结果:', result)
}

// ========== 综合示例：博客表单 ==========
const blogForm = ref({
  title: '',
  summary: '',
  content: '',
  tags: [] as string[]
})

const blogFormFields: FieldConfig[] = [
  { prop: 'title', label: '标题', type: 'text' },
  { prop: 'summary', label: '摘要', type: 'text' },
  { prop: 'content', label: '内容', type: 'text' }
]

const tagInputVisible = ref(false)
const tagInputValue = ref('')
const tagInputRef = ref()

const showTagInput = () => {
  tagInputVisible.value = true
  nextTick(() => {
    tagInputRef.value?.focus()
  })
}

const handleTagInputConfirm = () => {
  if (tagInputValue.value) {
    blogForm.value.tags.push(tagInputValue.value)
  }
  tagInputVisible.value = false
  tagInputValue.value = ''
}

const removeTag = (tag: string) => {
  const index = blogForm.value.tags.indexOf(tag)
  if (index > -1) {
    blogForm.value.tags.splice(index, 1)
  }
}

// 优化器弹窗
const optimizerDialogVisible = ref(false)
const currentOptimizeText = ref('')
const currentOptimizeField = ref('')

const showOptimizer = (field: 'title' | 'summary') => {
  currentOptimizeField.value = field
  currentOptimizeText.value = blogForm.value[field]
  optimizerDialogVisible.value = true
}

const handleOptimizeDialogResult = (result: string) => {
  if (currentOptimizeField.value === 'title' || currentOptimizeField.value === 'summary') {
    blogForm.value[currentOptimizeField.value] = result
    optimizerDialogVisible.value = false
    showMsgSuccess('优化结果已应用')
  }
}

// 审核弹窗
const reviewDialogVisible = ref(false)

const reviewBlogForm = () => {
  if (!blogForm.value.title || !blogForm.value.content) {
    showMsg('请填写标题和内容')
    return
  }
  reviewDialogVisible.value = true
}

const handleReviewComplete = (result: any) => {
  console.log('审核结果:', result)
  if (result.status === 'pass') {
    showMsgSuccess('内容审核通过，可以发布')
  } else {
    showMsg('内容存在问题，请根据建议修改')
  }
}

const resetBlogForm = () => {
  blogForm.value = {
    title: '',
    summary: '',
    content: '',
    tags: []
  }
}

// ========== 通用错误处理 ==========
const handleError = (error: Error) => {
  showMsgError(`操作失败: ${error.message}`)
  console.error('AI组件错误:', error)
}
</script>

<style scoped lang="scss">
.ai-components-demo {
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

  .demo-tabs {
    :deep(.el-tabs__content) {
      padding: 20px;
    }
  }

  .component-section {
    .section-header {
      margin-bottom: 20px;
      padding-bottom: 12px;
      border-bottom: 2px solid var(--el-border-color-lighter);

      h3 {
        font-size: 18px;
        font-weight: 600;
        margin: 0 0 8px 0;
        color: var(--el-text-color-primary);
      }

      p {
        font-size: 14px;
        color: var(--el-text-color-secondary);
        margin: 0;
      }
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
    margin: 0;

    code {
      color: var(--el-text-color-primary);
    }
  }

  .mt-2 {
    margin-top: 8px;
  }

  .mt-3 {
    margin-top: 12px;
  }

  .mr-2 {
    margin-right: 8px;
  }
}
</style>
