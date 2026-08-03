<!--
   span有值时显示ElCol ElFormItem组合
   <AFormEditor label="内容" v-model="form.content" prop="content" :span="24"></AFormEditor>
   span无值时仅显示ElFormItem
   <AFormEditor label="内容" v-model="form.content" prop="content"></AFormEditor>
   带提示信息的富文本编辑器
   <AFormEditor label="内容" v-model="form.content" prop="content" tooltip="请输入文章内容" :span="24"></AFormEditor>
-->
<template>
  <!--span有值且showFormItem为true 显示ElCol ElFormItem-->
  <el-col :span="computedSpan" v-if="shouldUseCol && showFormItem">
    <el-form-item class="w-full" :label-width="labelWidth" :prop="prop">
      <template #label>
        <span>{{ computedLabel }}</span>
        <el-tooltip v-if="tooltip" :content="tooltip" placement="top" class="ml-1">
          <el-icon>
            <QuestionFilled />
          </el-icon>
        </el-tooltip>
      </template>
      <div class="editor-wrapper">
        <Toolbar class="editor-toolbar" :editor="editorRef" :mode="mode" :defaultConfig="toolbarConfig" />
        <Editor
          :style="{ height: height, overflowY: 'hidden' }"
          v-model="modelValue"
          :mode="mode"
          :defaultConfig="editorConfig"
          @onCreated="onCreateEditor"
          @customPaste="handleCustomPaste"
        />
      </div>
    </el-form-item>
  </el-col>

  <!--span无值且showFormItem为true 显示ElFormItem-->
  <el-form-item class="w-full" v-else-if="!shouldUseCol && showFormItem" :label-width="labelWidth" :prop="prop">
    <template #label>
      <span>{{ computedLabel }}</span>
      <el-tooltip v-if="tooltip" :content="tooltip" placement="top" class="ml-1">
        <el-icon>
          <QuestionFilled />
        </el-icon>
      </el-tooltip>
    </template>
    <div class="editor-wrapper">
      <Toolbar class="editor-toolbar" :editor="editorRef" :mode="mode" :defaultConfig="toolbarConfig" />
      <Editor
        :style="{ height: height, overflowY: 'hidden' }"
        v-model="modelValue"
        :mode="mode"
        :defaultConfig="editorConfig"
        @onCreated="onCreateEditor"
        @customPaste="handleCustomPaste"
      />
    </div>
  </el-form-item>

  <!--showFormItem为false时只显示编辑器-->
  <template v-else>
    <div class="editor-wrapper">
      <Toolbar class="editor-toolbar" :editor="editorRef" :mode="mode" :defaultConfig="toolbarConfig" />
      <Editor
        :style="{ height: height, overflowY: 'hidden' }"
        v-model="modelValue"
        :mode="mode"
        :defaultConfig="editorConfig"
        @onCreated="onCreateEditor"
        @customPaste="handleCustomPaste"
      />
    </div>
  </template>
</template>

<script setup lang="ts">
import '@wangeditor/editor/dist/css/style.css'
import './icons/iconfont.css'
import { onBeforeUnmount, shallowRef, computed, watch, onMounted } from 'vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import { ElMessage } from 'element-plus'
import { QuestionFilled } from '@element-plus/icons-vue'
import { IDomEditor, IToolbarConfig, IEditorConfig, i18nChangeLanguage } from '@wangeditor/editor'
import { SystemConfig } from '@/systemConfig'
import { generateImageHash } from '@/utils/crypto'
import { saveRemoteImageToOss } from '@/api/system/oss/oss/ossApi'
import { showMsgError, showMsgSuccess } from '@/utils/modal'
import { LanguageCode } from '@/systemConfig'
import { marked } from 'marked'

// ==================== 组件Props ====================
interface AFormEditorProps {
  /** v-model绑定值 */
  modelValue?: string
  /** 表单项标签 */
  label?: string
  /** 标签宽度 */
  labelWidth?: string | number
  /** 表单验证字段名 */
  prop?: string
  /**
   * 栅格占据的列数，支持数字、数字字符串、响应式对象或预设字符串
   * - 数字：固定span值，如 12
   * - 数字字符串：如 "12"，会自动转换为数字
   * - 响应式对象：{ xs: 24, sm: 24, md: 12, lg: 8, xl: 6 }
   * - 预设字符串：'auto' - 自动响应式布局
   * @default undefined
   */
  span?: SpanType
  /** 是否显示表单项包装 */
  showFormItem?: boolean
  /** 编辑器高度 */
  height?: string
  /** 自定义工具栏配置 */
  toolbarKeys?: string[]
  /** 插入新工具到指定位置 */
  insertKeys?: { index: number; keys: string[] }
  /** 排除的工具栏项 */
  excludeKeys?: string[]
  /** 编辑器模式 */
  mode?: 'default' | 'simple'
  /** 占位符文本 */
  placeholder?: string
  /** 提示信息 */
  tooltip?: string
  /** 上传配置 */
  uploadConfig?: {
    maxFileSize?: number
    maxNumberOfFiles?: number
    server?: string
  }
  /** 是否启用 Markdown 粘贴解析 */
  enableMarkdown?: boolean
}

const props = withDefaults(defineProps<AFormEditorProps>(), {
  modelValue: '',
  label: '富文本',
  labelWidth: undefined,
  prop: '',
  span: undefined,
  showFormItem: true,
  height: '500px',
  mode: 'default',
  placeholder: '请输入内容...',
  tooltip: '',
  excludeKeys: () => ['fontFamily'],
  enableMarkdown: true
})

// ==================== 组件事件 ====================
const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

// ==================== 响应式数据 ====================
const editorRef = shallowRef<IDomEditor>()
const uploadedImages = new Map() // 缓存已上传的图片

// ==================== 国际化 ====================
const { t, currentLanguage, te, isChinese } = useI18n()

/**
 * 获取 WangEditor 对应的语言代码
 * WangEditor 支持 'zh-CN' 和 'en' 两种语言
 */
const getWangEditorLang = (lang: LanguageCode): 'zh-CN' | 'en' => {
  return lang === LanguageCode.zh_CN ? 'zh-CN' : 'en'
}

/**
 * 更新 WangEditor 工具栏语言
 */
const updateEditorLanguage = () => {
  const wangLang = getWangEditorLang(currentLanguage.value)
  i18nChangeLanguage(wangLang)
}

// 监听语言变化，动态更新 WangEditor 工具栏语言
watch(currentLanguage, () => {
  updateEditorLanguage()
})

// 组件挂载时设置 WangEditor 语言
onMounted(() => {
  updateEditorLanguage()
})

// ==================== 使用智能响应式逻辑 ====================
const { computedSpan, shouldUseCol } = useResponsiveSpan(toRef(props, 'span'))

// ==================== 计算属性 ====================
const computedLabel = computed(() => {
  // 未显式传 label：用 prop 作为 key 自动从词典国际化（兼容原有行为）
  if (!props.label) return t(props.prop, props.label)
  // 显式传了 label：中文环境直接用 label，保证 label 可控；
  // 其它语言优先查 prop 词典翻译，命中用词典，否则回退 label
  if (isChinese.value) return props.label
  return te(props.prop) ? t(props.prop) : props.label
})

const computedPlaceholder = computed(() => {
  // 如果用户传入了自定义 placeholder，直接使用
  if (props.placeholder && props.placeholder !== '请输入内容...') {
    return props.placeholder
  }
  // 否则使用国际化翻译
  return t('formEditor.placeholder')
})

// 双向绑定
const modelValue = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 常量配置
const DEFAULT_UPLOAD_CONFIG = {
  maxFileSize: 10 * 1024 * 1024, // 10MB
  maxNumberOfFiles: 10,
  fieldName: 'file',
  allowedFileTypes: ['image/*']
} as const

// 图标映射配置
const ICON_MAP = {
  bold: 'icon-bold',
  blockquote: 'icon-double-quotes-left',
  underline: 'icon-underline',
  italic: 'icon-italic',
  'group-more-style': 'icon-more-grid-big',
  color: 'icon-font-color',
  bgColor: 'icon-beijingsetianchong',
  bulletedList: 'icon-list-disorder',
  numberedList: 'icon-list-order',
  todo: 'icon-select-multi',
  'group-justify': 'icon-text-align-left',
  'group-indent': 'icon-list-remove',
  emotion: 'icon-biaoqing1',
  insertLink: 'icon-link-break',
  'group-image': 'icon-image',
  insertTable: 'icon-table',
  codeBlock: 'icon-code-inline',
  divider: 'icon-remove-minus',
  undo: 'icon-undo',
  redo: 'icon-redo',
  fullScreen: 'icon-expand1',
  tableFullWidth: 'icon-table'
} as const

// 计算属性：上传服务器地址
const uploadServer = computed(() => `${SystemConfig.api.baseUrl}${props.uploadConfig?.server || '/resource/oss/upload'}`)

// 合并上传配置
const mergedUploadConfig = computed(() => ({
  ...DEFAULT_UPLOAD_CONFIG,
  ...props.uploadConfig
}))

// 工具栏配置
const toolbarConfig = computed((): Partial<IToolbarConfig> => {
  const config: Partial<IToolbarConfig> = {}

  // 完全自定义工具栏
  if (props.toolbarKeys && props.toolbarKeys.length > 0) {
    config.toolbarKeys = props.toolbarKeys
  }

  // 插入新工具
  if (props.insertKeys) {
    config.insertKeys = props.insertKeys
  }

  // 排除工具
  if (props.excludeKeys && props.excludeKeys.length > 0) {
    config.excludeKeys = props.excludeKeys
  }

  return config
})

// 编辑器配置
const editorConfig = computed<Partial<IEditorConfig>>(() => ({
  placeholder: computedPlaceholder.value,
  MENU_CONF: {
    uploadImage: {
      fieldName: mergedUploadConfig.value.fieldName,
      maxFileSize: mergedUploadConfig.value.maxFileSize,
      maxNumberOfFiles: mergedUploadConfig.value.maxNumberOfFiles,
      allowedFileTypes: mergedUploadConfig.value.allowedFileTypes,
      server: uploadServer.value,
      headers: {
        ...useToken().getAuthHeaders()
      },
      onSuccess(file: File, res: any) {
        showMsgSuccess(t('formEditor.imageUploadSuccess'))
      },
      onError(file: File, err: any, res: any) {
        // 解析错误信息，提供更友好的提示
        let errorMessage = t('formEditor.imageUploadFailed')

        // 检查是否是文件大小超限错误
        if (err && err.message) {
          const errorMsg = err.message.toString()

          // 匹配文件大小超限的错误信息
          const sizeExceedMatch = errorMsg.match(/exceeds maximum allowed size of (\d+(?:\.\d+)?)\s*(MB|KB|GB)/i)
          if (sizeExceedMatch) {
            const maxSize = sizeExceedMatch[1]
            const unit = sizeExceedMatch[2]
            errorMessage = `文件 "${file.name}" 超出大小限制，最大允许 ${maxSize}${unit}`
          }
          // 匹配文件数量超限的错误信息
          else if (errorMsg.includes('maximum number of files')) {
            errorMessage = `上传文件数量超出限制，最多允许 ${mergedUploadConfig.value.maxNumberOfFiles} 个文件`
          }
          // 匹配文件类型不支持的错误信息
          else if (errorMsg.includes('not allowed') || errorMsg.includes('file type')) {
            errorMessage = `文件 "${file.name}" 类型不支持，仅支持图片格式`
          }
        }

        showMsgError(errorMessage)
      },
      // 自定义插入图片
      // eslint-disable-next-line @typescript-eslint/no-unsafe-function-type
      customInsert(res: any, insertFn: Function) {
        // 根据你的服务器响应格式，提取图片URL
        if (res && res.code === 200 && res.data && res.data.url) {
          const { url, fileName } = res.data
          // 插入图片到编辑器
          insertFn(url, fileName, url)
        } else {
          showMsgError(t('formEditor.imageUploadResponseError'))
        }
      }
    },
    uploadVideo: {
      fieldName: mergedUploadConfig.value.fieldName,
      maxFileSize: 100 * 1024 * 1024, // 100MB
      maxNumberOfFiles: 5,
      allowedFileTypes: ['video/*'],
      server: uploadServer.value,
      headers: {
        ...useToken().getAuthHeaders()
      },
      onSuccess(file: File, res: any) {
        showMsgSuccess(t('formEditor.videoUploadSuccess'))
      },
      onError(file: File, err: any, res: any) {
        // 解析错误信息，提供更友好的提示
        let errorMessage = t('formEditor.videoUploadFailed')

        // 检查是否是文件大小超限错误
        if (err && err.message) {
          const errorMsg = err.message.toString()

          // 匹配文件大小超限的错误信息
          const sizeExceedMatch = errorMsg.match(/exceeds maximum allowed size of (\d+(?:\.\d+)?)\s*(MB|KB|GB)/i)
          if (sizeExceedMatch) {
            const maxSize = sizeExceedMatch[1]
            const unit = sizeExceedMatch[2]
            errorMessage = `视频文件 "${file.name}" 超出大小限制，最大允许 ${maxSize}${unit}`
          }
          // 匹配文件数量超限的错误信息
          else if (errorMsg.includes('maximum number of files')) {
            errorMessage = `上传视频数量超出限制，最多允许 5 个文件`
          }
          // 匹配文件类型不支持的错误信息
          else if (errorMsg.includes('not allowed') || errorMsg.includes('file type')) {
            errorMessage = `文件 "${file.name}" 类型不支持，仅支持视频格式`
          }
        }

        showMsgError(errorMessage)
      },
      // 自定义插入视频
      // eslint-disable-next-line @typescript-eslint/no-unsafe-function-type
      customInsert(res: any, insertFn: Function) {
        // 根据你的服务器响应格式，提取视频URL
        if (res && res.code === 200 && res.data && res.data.url) {
          const { url, fileName } = res.data
          // 插入视频到编辑器
          insertFn(url, fileName || 'video')
        } else {
          showMsgError(t('formEditor.videoUploadResponseError'))
        }
      }
    }
  }
}))

// ==================== Markdown 解析配置 ====================

// 配置 marked 选项
marked.setOptions({
  gfm: true, // 启用 GitHub Flavored Markdown
  breaks: true // 将换行符转换为 <br>
})

/**
 * 检测文本是否包含 Markdown 语法
 * 通过检测常见的 Markdown 特征来判断
 */
const isMarkdownContent = (text: string): boolean => {
  if (!text || text.trim().length === 0) return false

  // Markdown 语法正则表达式
  const markdownPatterns = [
    /^#{1,6}\s+.+$/m, // 标题: # ## ### 等
    /^\s*[-*+]\s+.+$/m, // 无序列表: - * +
    /^\s*\d+\.\s+.+$/m, // 有序列表: 1. 2. 3.
    /\[.+?\]\(.+?\)/, // 链接: [text](url)
    /!\[.*?\]\(.+?\)/, // 图片: ![alt](url)
    /\*\*[^*]+\*\*/, // 粗体: **text**
    /__[^_]+__/, // 粗体: __text__
    /(?<!\*)\*[^*]+\*(?!\*)/, // 斜体: *text* (排除粗体)
    /(?<!_)_[^_]+_(?!_)/, // 斜体: _text_ (排除粗体)
    /`[^`]+`/, // 行内代码: `code`
    /^```[\s\S]*?```$/m, // 代码块: ```code```
    /^~~~[\s\S]*?~~~$/m, // 代码块: ~~~code~~~
    /^>\s+.+$/m, // 引用: > text
    /^[-*_]{3,}$/m, // 分割线: --- *** ___
    /\|.+\|.+\|/ // 表格: | col1 | col2 |
  ]

  // 计算匹配的特征数量
  let matchCount = 0
  for (const pattern of markdownPatterns) {
    if (pattern.test(text)) {
      matchCount++
      // 匹配到 2 个及以上特征，认为是 Markdown
      if (matchCount >= 2) return true
    }
  }

  // 如果只有一个特征，需要更严格的判断
  // 检查是否有多行且包含典型 Markdown 结构
  if (matchCount === 1) {
    const lines = text.split('\n')
    // 如果有多行且第一行是标题格式
    if (lines.length > 1 && /^#{1,6}\s+.+$/.test(lines[0].trim())) {
      return true
    }
  }

  return false
}

/**
 * 将 Markdown 文本转换为 HTML
 */
const convertMarkdownToHtml = async (markdown: string): Promise<string> => {
  try {
    const html = await marked.parse(markdown)
    return html
  } catch (error) {
    console.error('Markdown 解析失败:', error)
    return markdown
  }
}

// ==================== 粘贴处理函数 ====================

// 处理自定义粘贴事件
const handleCustomPaste = (editor: IDomEditor, event: ClipboardEvent): boolean => {
  const clipboardData = event.clipboardData
  if (!clipboardData) return false

  // 处理图片文件
  const files = Array.from(clipboardData.files)
  if (files.length > 0) {
    event.preventDefault()
    handlePastedFiles(files, editor)
    return true
  }

  // 处理HTML内容中的图片
  const htmlData = clipboardData.getData('text/html')
  if (htmlData && (htmlData.includes('<img') || htmlData.includes('file:///'))) {
    event.preventDefault()
    handlePastedHTML(htmlData, editor)
    return true
  }

  // 处理 Markdown 内容
  if (props.enableMarkdown) {
    const textData = clipboardData.getData('text/plain')
    if (textData && isMarkdownContent(textData)) {
      event.preventDefault()
      handlePastedMarkdown(textData, editor)
      return true
    }
  }

  return false
}

// 处理粘贴的 Markdown 内容
const handlePastedMarkdown = async (markdown: string, editor: IDomEditor) => {
  try {
    const html = await convertMarkdownToHtml(markdown)
    if (html) {
      editor.dangerouslyInsertHtml(html)
    }
  } catch (error) {
    console.error('处理 Markdown 粘贴失败:', error)
    // 降级：直接插入纯文本
    editor.insertText(markdown)
  }
}

// 处理粘贴的文件
const handlePastedFiles = async (files: File[], editor: IDomEditor) => {
  for (const file of files) {
    if (file.type.startsWith('image/')) {
      try {
        const fileHash = await generateFileHash(file)

        // 检查缓存
        if (uploadedImages.has(fileHash)) {
          const cachedResult = uploadedImages.get(fileHash)
          insertImageToEditor(editor, cachedResult.url, cachedResult.name)
          continue
        }

        // 上传图片
        const uploadResult = await uploadImageFile(file)
        if (uploadResult?.url) {
          // 缓存结果
          uploadedImages.set(fileHash, {
            url: uploadResult.url,
            id: uploadResult.id,
            name: uploadResult.name
          })

          // 插入到编辑器
          insertImageToEditor(editor, uploadResult.url, uploadResult.name)
        }
      } catch (error) {
        ElMessage.error(t('formEditor.imageUploadFailed'))
      }
    }
  }
}

// 处理粘贴的HTML内容
const handlePastedHTML = async (htmlData: string, editor: IDomEditor) => {
  const tempDiv = document.createElement('div')
  tempDiv.innerHTML = htmlData

  const images = tempDiv.querySelectorAll('img')

  // 如果有图片，先处理图片，然后逐个插入
  if (images.length > 0) {
    const processedImages = []

    // 处理所有图片
    for (let i = 0; i < images.length; i++) {
      const img = images[i]
      const src = img.src

      if (!src) continue

      // 过滤掉本地文件路径
      if (src.startsWith('file:///')) {
        img.remove()
        continue
      }

      try {
        let processResult = false

        if (src.startsWith('data:image/')) {
          processResult = await handleBase64Image(img, src)
        } else if (src.startsWith('http')) {
          processResult = await handleRemoteImage(img, src)
        }

        if (processResult) {
          processedImages.push({
            originalSrc: src,
            newSrc: img.src,
            alt: img.alt || '',
            width: img.width || null,
            height: img.height || null
          })
        }
      } catch (error) {
        console.error('处理图片失败:', error)
      }
    }

    // 分离插入：先插入文本内容，再插入图片
    if (processedImages.length > 0) {
      // 先插入文本内容（去掉图片）
      const tempTextDiv = document.createElement('div')
      tempTextDiv.innerHTML = htmlData

      // 移除所有图片标签
      const textImages = tempTextDiv.querySelectorAll('img')
      textImages.forEach((img) => {
        // 用占位符替换图片，保持文档结构
        const placeholder = document.createElement('span')
        placeholder.textContent = `[图片占位符]`
        img.parentNode?.replaceChild(placeholder, img)
      })

      const textOnlyHTML = tempTextDiv.innerHTML.replace(/\[图片占位符\]/g, '\n')

      try {
        // 插入文本内容
        if (textOnlyHTML.trim()) {
          editor.dangerouslyInsertHtml(textOnlyHTML)
        }

        // 延时插入图片
        processedImages.forEach((imgData, index) => {
          setTimeout(
            () => {
              insertImageToEditor(editor, imgData.newSrc, imgData.alt)
            },
            (index + 1) * 200
          )
        })
      } catch (error) {
        // 备用方案：仅插入图片
        processedImages.forEach((imgData, index) => {
          setTimeout(() => {
            insertImageToEditor(editor, imgData.newSrc, imgData.alt)
          }, index * 100)
        })
      }
    } else {
      // 没有成功处理的图片，尝试插入原始HTML
      try {
        const processedHTML = tempDiv.innerHTML
        if (processedHTML.trim()) {
          editor.dangerouslyInsertHtml(processedHTML)
        }
      } catch (error) {
        const textContent = tempDiv.textContent || tempDiv.innerText || ''
        if (textContent.trim()) {
          editor.insertText(textContent)
        }
      }
    }
  } else {
    // 没有图片，直接插入HTML
    try {
      editor.dangerouslyInsertHtml(htmlData)
    } catch (error) {
      const tempDiv2 = document.createElement('div')
      tempDiv2.innerHTML = htmlData
      const textContent = tempDiv2.textContent || tempDiv2.innerText || ''
      if (textContent.trim()) {
        editor.insertText(textContent)
      }
    }
  }
}

// 处理base64图片
const handleBase64Image = async (imgElement: HTMLImageElement, base64Src: string) => {
  const imageHash = generateImageHash(base64Src)

  // 检查缓存
  if (uploadedImages.has(imageHash)) {
    const cachedResult = uploadedImages.get(imageHash)
    imgElement.src = cachedResult.url
    return true
  }

  try {
    // 转换base64为文件并上传
    const file = base64ToFile(base64Src)
    const uploadResult = await uploadImageFile(file)

    if (uploadResult?.url) {
      // 更新图片src
      imgElement.src = uploadResult.url

      // 缓存结果
      uploadedImages.set(imageHash, {
        url: uploadResult.url,
        id: uploadResult.id,
        name: uploadResult.name
      })

      return true
    } else {
      return false
    }
  } catch (error) {
    return false
  }
}

// 处理远程图片
const handleRemoteImage = async (imgElement: HTMLImageElement, remoteUrl: string) => {
  const urlHash = generateImageHash(remoteUrl)

  // 检查缓存
  if (uploadedImages.has(urlHash)) {
    const cachedResult = uploadedImages.get(urlHash)
    imgElement.src = cachedResult.url
    return true
  }

  try {
    // 保存远程图片到OSS
    const [err, data] = await saveRemoteImageToOss(null, remoteUrl)

    if (!err && data?.url) {
      // 更新图片src
      imgElement.src = data.url

      // 缓存结果
      uploadedImages.set(urlHash, {
        url: data.url,
        id: data.ossId,
        name: data.fileName
      })

      return true
    } else {
      return false
    }
  } catch (error) {
    return false
  }
}

// ==================== 工具函数 ====================

// 上传图片文件
const uploadImageFile = async (file: File) => {
  const formData = new FormData()
  formData.append('file', file)

  console.log('🚀 开始上传文件到服务器:', file.name)
  const [err, data] = await http.post('/resource/oss/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
      'repeatSubmit': false,
      ...useToken().getAuthHeaders()
    }
  })

  if (err) {
    throw new Error(t('formEditor.uploadFailed'))
  }

  return {
    id: data.ossId,
    url: data.url,
    name: data.fileName
  }
}

// Base64转文件对象
const base64ToFile = (base64Data: string): File => {
  const mimeMatch = base64Data.match(/^data:([^;]+);base64,(.+)$/)
  if (!mimeMatch) {
    throw new Error(t('formEditor.invalidBase64Data'))
  }

  const mime = mimeMatch[1]
  const base64 = mimeMatch[2]

  const binaryString = atob(base64)
  const bytes = new Uint8Array(binaryString.length)
  for (let i = 0; i < binaryString.length; i++) {
    bytes[i] = binaryString.charCodeAt(i)
  }

  const blob = new Blob([bytes], { type: mime })
  const extension = mime.split('/')[1]
  const fileName = `paste_image_${Date.now()}.${extension}`

  return new File([blob], fileName, { type: mime })
}

// 生成文件哈希
const generateFileHash = async (file: File): Promise<string> => {
  return new Promise((resolve) => {
    const reader = new FileReader()
    reader.onload = () => {
      const base64 = reader.result as string
      const hash = generateImageHash(base64)
      resolve(hash)
    }
    reader.readAsDataURL(file)
  })
}

// 插入图片到编辑器
const insertImageToEditor = (editor: IDomEditor, url: string, alt: string = '') => {
  editor.insertNode({
    type: 'image',
    src: url,
    alt: alt,
    href: '',
    style: {
      width: '',
      height: ''
    },
    children: [{ text: '' }]
  } as any)
}

// ==================== 事件处理 ====================
// 编辑器创建回调
const onCreateEditor = (editor: IDomEditor) => {
  editorRef.value = editor

  // 监听全屏事件
  editor.on('fullScreen', () => {
    console.log('编辑器进入全屏模式')
  })

  applyCustomIcons()
}

// 应用自定义图标（带重试机制）
const applyCustomIcons = () => {
  let retryCount = 0
  const maxRetries = 10
  const retryDelay = 100

  const tryApplyIcons = () => {
    const editor = editorRef.value
    if (!editor) {
      if (retryCount < maxRetries) {
        retryCount++
        setTimeout(tryApplyIcons, retryDelay)
      }
      return
    }

    // 获取当前编辑器的工具栏容器
    const editorContainer = editor.getEditableContainer().closest('.editor-wrapper')
    if (!editorContainer) {
      if (retryCount < maxRetries) {
        retryCount++
        setTimeout(tryApplyIcons, retryDelay)
      }
      return
    }

    const toolbar = editorContainer.querySelector('.w-e-toolbar')
    const toolbarButtons = editorContainer.querySelectorAll('.w-e-bar-item button[data-menu-key]')

    if (toolbar && toolbarButtons.length > 0) {
      overrideIcons(editor)
      return
    }

    // 如果工具栏还没渲染完成，继续重试
    if (retryCount < maxRetries) {
      retryCount++
      setTimeout(tryApplyIcons, retryDelay)
    }
  }

  // 使用 requestAnimationFrame 确保在下一帧执行
  requestAnimationFrame(tryApplyIcons)
}

// 图标替换函数
const overrideIcons = (editorInstance: IDomEditor) => {
  // 获取当前编辑器的工具栏容器
  const editorContainer = editorInstance.getEditableContainer().closest('.editor-wrapper')
  if (!editorContainer) return

  const toolbar = editorContainer.querySelector('.w-e-toolbar')
  if (!toolbar) return

  Object.entries(ICON_MAP).forEach(([menuKey, iconClass]) => {
    const button = toolbar.querySelector(`button[data-menu-key="${menuKey}"]`)
    if (button) {
      button.innerHTML = `<i class='iconfont ${iconClass}'></i>`
    }
  })
}

// ==================== 暴露方法 ====================
defineExpose({
  /** 获取编辑器实例 */
  getEditor: () => editorRef.value,
  /** 设置编辑器内容 */
  setHtml: (html: string) => editorRef.value?.setHtml(html),
  /** 获取编辑器内容 */
  getHtml: () => editorRef.value?.getHtml(),
  /** 清空编辑器 */
  clear: () => editorRef.value?.clear(),
  /** 聚焦编辑器 */
  focus: () => editorRef.value?.focus(),
  /** 设置 Markdown 内容（自动转换为 HTML） */
  setMarkdown: async (markdown: string) => {
    if (editorRef.value) {
      const html = await convertMarkdownToHtml(markdown)
      editorRef.value.setHtml(html)
    }
  },
  /** 插入 Markdown 内容（在光标位置插入） */
  insertMarkdown: async (markdown: string) => {
    if (editorRef.value) {
      const html = await convertMarkdownToHtml(markdown)
      editorRef.value.dangerouslyInsertHtml(html)
    }
  }
})

// ==================== 生命周期 ====================
onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor && !editor.isDestroyed) {
    try {
      editor.destroy()
    } catch (error) {
      console.warn('销毁编辑器失败:', error)
    }
  }
})
</script>

<style lang="scss">
/* ==================== 变量定义 ==================== */
$box-radius: 8px;

/* ==================== CSS变量（主题配置） ==================== */
:root {
  /* 激活颜色 */
  --w-e-toolbar-active-bg-color: #f1f1f4;
  /* toolbar 图标和文字颜色 */
  --w-e-toolbar-color: #071437;
  /* 表格选中时候的边框颜色 */
  --w-e-textarea-selected-border-color: #dbdfe9;
  /* 表格头背景颜色 */
  --w-e-textarea-slight-bg-color: #f1f1f4;
}

/* ==================== 基础容器样式 ==================== */
.editor-wrapper {
  z-index: 1002;
  width: 100%;
  height: 100%;
  border: 1px solid rgba(219, 223, 233, 0.8);
  border-radius: $box-radius !important;

  .iconfont {
    font-size: 20px !important;
  }

  .menu-item {
    display: flex;
    flex-direction: row;
    align-items: center;

    i {
      margin-right: 5px;
    }
  }
}

/* ==================== 工具栏样式 ==================== */
.editor-wrapper {
  /* 工具栏容器 */
  .w-e-bar {
    border-radius: $box-radius $box-radius 0 0 !important;
  }

  .editor-toolbar {
    border-bottom: 1px solid #eaebf1;
  }

  /* 工具栏按钮 */
  .w-e-bar-item button {
    border-radius: $box-radius;
  }

  .w-e-bar-item button:hover {
    background-color: #f1f1f4;
  }

  /* 工具栏分割线 */
  .w-e-bar-divider {
    height: 20px;
    margin-top: 10px;
    background-color: #c4cada;
  }

  /* 工具栏菜单 */
  .w-e-bar-item-group .w-e-bar-item-menus-container {
    min-width: 120px;
    padding: 10px 0;
    border: none;
    border-radius: $box-radius;

    .w-e-bar-item {
      button {
        width: 100%;
        margin: 0 5px;
      }
    }
  }
}

/* ==================== 下拉菜单样式 ==================== */
.editor-wrapper {
  /* 下拉选择框配置 */
  .w-e-select-list {
    min-width: 140px;
    padding: 5px 10px 10px;
    border: none;
    border-radius: $box-radius;
  }

  /* 下拉选择框元素配置 */
  .w-e-select-list ul li {
    margin-top: 5px;
    font-size: 15px !important;
    border-radius: $box-radius;
  }

  /* 下拉选择框 正文文字大小调整 */
  .w-e-select-list ul li:last-of-type {
    font-size: 16px !important;
  }

  /* 下拉选择框 hover 样式调整 */
  .w-e-select-list ul li:hover {
    background-color: #f1f1f4;
  }
}

/* ==================== 弹出组件样式 ==================== */
.editor-wrapper {
  /* 弹出框 */
  .w-e-drop-panel {
    border: 0;
    border-radius: $box-radius;
  }

  /* 输入区域弹出 bar */
  .w-e-hover-bar {
    border-radius: $box-radius;
  }

  /* 超链接弹窗 */
  .w-e-modal {
    border: none;
    border-radius: $box-radius;
  }
}

/* ==================== 编辑器内容区域样式 ==================== */
.editor-wrapper {
  /* 链接样式 */
  a {
    color: #5d87ff;
  }

  /* 文本格式样式 */
  .w-e-text-container [data-slate-editor] strong,
  .w-e-text-container [data-slate-editor] b {
    font-weight: 700 !important;
  }

  .w-e-text-container [data-slate-editor] i,
  .w-e-text-container [data-slate-editor] em {
    font-style: italic !important;
  }

  .w-e-text-container [data-slate-editor] u {
    text-decoration: underline !important;
  }

  /* 代码块 */
  .w-e-text-container [data-slate-editor] pre > code {
    padding: 0.6rem 1rem;
    background-color: #f9f9f9;
    border-radius: $box-radius;
  }

  /* 引用 */
  .w-e-text-container [data-slate-editor] blockquote {
    background-color: rgba(219, 223, 233, 0.25);
    border-left: 4px solid #dbdfe9;
  }
}

/* ==================== 表格样式 ==================== */
.editor-wrapper {
  /* 表格样式优化 */
  .w-e-text-container [data-slate-editor] .table-container th {
    border-right: none;
  }

  .w-e-text-container [data-slate-editor] .table-container th:last-of-type {
    border-right: 1px solid #c4cada !important;
  }
}

/* ==================== 图片样式 ==================== */
.editor-wrapper {
  /* 图片样式调整 */
  .w-e-text-container [data-slate-editor] .w-e-selected-image-container {
    overflow: inherit;

    &:hover {
      border: 0;
    }

    img {
      border: 1px solid transparent;
      transition: border 0.3s;

      &:hover {
        border: 1px solid #5d87ff !important;
      }
    }

    .w-e-image-dragger {
      width: 12px;
      height: 12px;
      background-color: #5d87ff;
      border: 2px solid #ffffff;
      border-radius: $box-radius;
    }

    .left-top {
      top: -6px;
      left: -6px;
    }

    .right-top {
      top: -6px;
      right: -6px;
    }

    .left-bottom {
      bottom: -6px;
      left: -6px;
    }

    .right-bottom {
      right: -6px;
      bottom: -6px;
    }
  }
}

/* ==================== 表单项样式调整 ==================== */
:deep(.el-form-item__content) {
  .editor-wrapper {
    margin-bottom: 0;
  }
}

/* ==================== 暗色主题支持 ==================== */
html.dark .editor-wrapper {
  border: 1px solid rgba(54, 56, 67, 0.8);

  /* CSS变量覆盖（暗色主题） */
  --w-e-toolbar-bg-color: #1b1c22;
  --w-e-toolbar-active-bg-color: #26272f;
  --w-e-toolbar-color: #f5f5f5;
  --w-e-toolbar-border-color: #26272f;
  --w-e-textarea-bg-color: #161618;
  --w-e-textarea-color: #f5f5f5;
  --w-e-textarea-selected-border-color: #363843;
  --w-e-textarea-slight-bg-color: #26272f;

  /* 工具栏暗色主题 */
  .editor-toolbar,
  .w-e-toolbar {
    background-color: #1b1c22;
    border-bottom: 1px solid #26272f;
  }

  /* 工具栏按钮暗色主题 */
  .w-e-bar-item button {
    color: #f5f5f5;
    background-color: transparent;
  }

  .w-e-bar-item button:hover {
    background-color: #26272f;
  }

  /* 激活状态的按钮 */
  .w-e-bar-item button.w-e-bar-item-active {
    background-color: #26272f;
  }

  .w-e-bar-divider {
    background-color: #464852;
  }

  /* 编辑器内容区域暗色背景 */
  .w-e-text-container {
    background-color: #161618 !important;
    color: #f5f5f5 !important;
  }

  /* 编辑器输入区域 */
  .w-e-text-container [data-slate-editor] {
    background-color: #161618 !important;
    color: #f5f5f5 !important;
  }

  /* 下拉菜单暗色主题 */
  .w-e-select-list {
    background-color: #1b1c22;
    color: #f5f5f5;
  }

  .w-e-select-list ul li:hover {
    background-color: #26272f;
  }

  /* 弹出框暗色主题 */
  .w-e-drop-panel {
    background-color: #1b1c22;
    color: #f5f5f5;
  }

  .w-e-modal {
    background-color: #1b1c22;
    color: #f5f5f5;
  }

  /* 代码块暗色主题 */
  .w-e-text-container [data-slate-editor] pre > code {
    background-color: #1b1c22;
    color: #f5f5f5;
  }

  /* 引用块暗色主题 */
  .w-e-text-container [data-slate-editor] blockquote {
    background-color: rgba(54, 56, 67, 0.25);
    border-left: 4px solid #363843;
    color: #f5f5f5;
  }

  /* 表格样式 */
  .w-e-text-container [data-slate-editor] .table-container th:last-of-type {
    border-right: 1px solid #464852 !important;
  }

  /* 图片拖拽控制点 */
  .w-e-image-dragger {
    border: 2px solid #161618 !important;
  }

  /* 链接颜色 */
  a {
    color: #73beff;
  }
}
</style>
