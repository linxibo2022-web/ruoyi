<!--用法示例
// 基础文件上传
<AFormFileUpload label="附件" v-model="form.fileUrls" prop="fileUrls" :span="12"></AFormFileUpload>
// 单文件上传
<AFormFileUpload label="合同" v-model="form.contract" prop="contract" :limit="1" :span="12"></AFormFileUpload>
// 限制文件类型和大小
<AFormFileUpload label="文档" v-model="form.documents" prop="documents" :file-type="['pdf', 'doc', 'docx']" :file-size="10" :span="12"></AFormFileUpload>
// 拖拽上传模式
<AFormFileUpload label="批量文档" v-model="form.files" prop="files" :drag="true" :limit="10" :span="24"></AFormFileUpload>
// 启用直传模式（云存储）
<AFormFileUpload label="重要文件" v-model="form.files" prop="files" :enable-direct-upload="true" module-name="documents" :span="12"></AFormFileUpload>
// 控制存储路径和目录
<AFormFileUpload label="项目文档" v-model="form.files" prop="files" module-name="project" directory-path="/文档/办公" :directory-id="123" :span="12"></AFormFileUpload>
// 启用素材库选择
<AFormFileUpload label="模板文件" v-model="form.files" prop="files" :enable-oss-media-manager="true" :span="12"></AFormFileUpload>
// 带提示信息的文件上传
<AFormFileUpload label="证书" v-model="form.files" prop="files" tooltip="请上传相关资质证书" :file-type="['pdf', 'jpg', 'png']" :span="12"></AFormFileUpload>
// 不含el-form-item容器的简单文件上传
<AFormFileUpload v-model="form.fileUrls" :show-form-item="false" :limit="3"></AFormFileUpload>
// 替换模式 - 由父组件控制
<AFormFileUpload label="配置文件" v-model="form.file" :upload-params="{isReplace: true, replaceOssId: 123}" :limit="1" :span="12"></AFormFileUpload>
// 返回ossId模式
<AFormFileUpload label="项目文档" v-model="form.documents" prop="documents" return-mode="ossId" :limit="5" :span="12"></AFormFileUpload>
// 返回URL模式（默认）
<AFormFileUpload label="合同附件" v-model="form.contracts" prop="contracts" return-mode="url" :limit="3" :span="12"></AFormFileUpload>
-->
<template>
  <!--span有值且showFormItem为true 显示ElCol ElFormItem-->
  <el-col :span="computedSpan" v-if="shouldUseCol && showFormItem">
    <el-form-item :label-width="labelWidth" :prop="prop">
      <template #label>
        <span>{{ computedLabel }}</span>
        <el-tooltip v-if="tooltip" :content="tooltip" placement="top" class="ml-1">
          <el-icon>
            <QuestionFilled />
          </el-icon>
        </el-tooltip>
      </template>

      <div class="flex flex-col items-start gap-10px">
        <el-upload
          ref="fileUploadRef"
          :action="getUploadUrl"
          :headers="headers"
          :file-list="displayFileList"
          :accept="fileAccept"
          :limit="limit"
          :disabled="disabled"
          :multiple="multiple"
          :drag="drag"
          :auto-upload="autoUpload"
          :list-type="listType"
          :class="{ 'hide': fileList.length >= limit }"
          :show-file-list="showFileList"
          :before-upload="handleBeforeUpload"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          :on-exceed="handleExceed"
          :before-remove="handleBeforeDelete as any"
          :on-preview="handleFilePreview as any"
          :on-change="handleFileChange"
          :on-progress="handleProgress"
          :key="refreshKey"
        >
          <template v-if="drag">
            <el-icon class="el-icon--upload">
              <UploadFilled />
            </el-icon>
            <div class="el-upload__text">{{ t('formUpload.fileDragHint') }}<em>{{ t('formUpload.clickToUpload') }}</em></div>
          </template>
          <template v-else>
            <el-button type="primary" :disabled="disabled">
              <el-icon>
                <Upload />
              </el-icon>
              {{ computedUploadButtonText }}
            </el-button>
          </template>
        </el-upload>

        <div class="flex items-center gap-10px w-full" v-if="!isReplaceMode">
          <!-- 按钮容器 -->
          <div class="flex gap-10px ml-auto">
            <!-- 添加素材库按钮 -->
            <el-button v-if="!disabled && enableOssMediaManager && fileList.length < limit" type="success" size="small" @click="openOssMediaManager">
              <el-icon>
                <FolderOpened />
              </el-icon>
              {{ t('formUpload.selectFromLibrary') }}
            </el-button>

            <!-- 清空全部按钮 -->
            <el-button v-if="!disabled && enableClearAll && fileList.length > 1" type="danger" size="small" @click="handleClearAll">
              <el-icon>
                <Delete />
              </el-icon>
              {{ t('formUpload.clearAll') }}
            </el-button>
          </div>
        </div>

        <!-- 上传提示 -->
        <div v-if="showTip" class="el-upload__tip text-sm text-gray-500">
          {{ t('formUpload.uploadHintPrefix') }}
          <template v-if="fileSize">
            {{ t('formUpload.sizeLimit') }} <b class="text-[#f56c6c]">{{ fileSize }}MB</b>
          </template>
          <template v-if="fileType?.length">
            {{ t('formUpload.formatHint') }} <b class="text-[#f56c6c]">{{ fileType.join('/') }}</b>
          </template>
          {{ t('formUpload.fileSuffix') }}
          <template v-if="limit > 1">
            {{ t('formUpload.countLimit') }} <b class="text-[#f56c6c]">{{ limit }}</b> {{ t('formUpload.fileCountUnit') }}
          </template>
          <template v-if="enableDirectUpload">
            <b class="text-[#67c23a]">{{ t('formUpload.directMode') }}</b>
          </template>
        </div>

        <!-- 上传进度 -->
        <div v-if="uploadProgress > 0 && uploadProgress < 100" class="w-full">
          <el-progress :percentage="uploadProgress" :show-text="true"></el-progress>
        </div>
      </div>
    </el-form-item>
  </el-col>

  <!--span无值且showFormItem为true 显示ElFormItem-->
  <el-form-item :label-width="labelWidth" :prop="prop" v-else-if="!shouldUseCol && showFormItem">
    <template #label>
      <span>{{ computedLabel }}</span>
      <el-tooltip v-if="tooltip" :content="tooltip" placement="top" class="ml-1">
        <el-icon>
          <QuestionFilled />
        </el-icon>
      </el-tooltip>
    </template>

    <div class="flex flex-col items-start gap-10px">
      <el-upload
        ref="fileUploadRef"
        :action="getUploadUrl"
        :headers="headers"
        :file-list="displayFileList"
        :accept="fileAccept"
        :limit="limit"
        :disabled="disabled"
        :multiple="multiple"
        :drag="drag"
        :auto-upload="autoUpload"
        :list-type="listType"
        :class="{ 'hide': fileList.length >= limit }"
        :show-file-list="showFileList"
        :before-upload="handleBeforeUpload"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        :on-exceed="handleExceed"
        :before-remove="handleBeforeDelete as any"
        :on-preview="handleFilePreview as any"
        :on-change="handleFileChange"
        :on-progress="handleProgress"
        :key="refreshKey"
      >
        <template v-if="drag">
          <el-icon class="el-icon--upload">
            <UploadFilled />
          </el-icon>
          <div class="el-upload__text">{{ t('formUpload.fileDragHint') }}<em>{{ t('formUpload.clickToUpload') }}</em></div>
        </template>
        <template v-else>
          <el-button type="primary" :disabled="disabled">
            <el-icon>
              <Upload />
            </el-icon>
            {{ computedUploadButtonText }}
          </el-button>
        </template>
      </el-upload>

      <div class="flex items-center gap-10px w-full" v-if="!isReplaceMode">
        <!-- 按钮容器 -->
        <div class="flex gap-10px ml-auto">
          <!-- 添加素材库按钮 -->
          <el-button v-if="!disabled && enableOssMediaManager && fileList.length < limit" type="success" size="small" @click="openOssMediaManager">
            <el-icon>
              <FolderOpened />
            </el-icon>
            {{ t('formUpload.selectFromLibrary') }}
          </el-button>

          <!-- 清空全部按钮 -->
          <el-button v-if="!disabled && enableClearAll && fileList.length > 1" type="danger" size="small" @click="handleClearAll">
            <el-icon>
              <Delete />
            </el-icon>
            {{ t('formUpload.clearAll') }}
          </el-button>
        </div>
      </div>

      <!-- 上传提示 -->
      <div v-if="showTip" class="el-upload__tip text-sm text-gray-500">
        {{ t('formUpload.uploadHintPrefix') }}
        <template v-if="fileSize">
          {{ t('formUpload.sizeLimit') }} <b class="text-[#f56c6c]">{{ fileSize }}MB</b>
        </template>
        <template v-if="fileType?.length">
          {{ t('formUpload.formatHint') }} <b class="text-[#f56c6c]">{{ fileType.join('/') }}</b>
        </template>
        {{ t('formUpload.fileSuffix') }}
        <template v-if="limit > 1">
          {{ t('formUpload.countLimit') }} <b class="text-[#f56c6c]">{{ limit }}</b> {{ t('formUpload.fileCountUnit') }}
        </template>
        <template v-if="enableDirectUpload">
          <b class="text-[#67c23a]">{{ t('formUpload.directMode') }}</b>
        </template>
      </div>

      <!-- 上传进度 -->
      <div v-if="uploadProgress > 0 && uploadProgress < 100" class="w-full">
        <el-progress :percentage="uploadProgress" :show-text="true"></el-progress>
      </div>
    </div>
  </el-form-item>

  <!--showFormItem为false 只显示文件上传器-->
  <template v-else>
    <div class="flex flex-col items-start gap-10px">
      <el-upload
        ref="fileUploadRef"
        :action="getUploadUrl"
        :headers="headers"
        :file-list="displayFileList"
        :accept="fileAccept"
        :limit="limit"
        :disabled="disabled"
        :multiple="multiple"
        :drag="drag"
        :auto-upload="autoUpload"
        :list-type="listType"
        :class="{ 'hide': fileList.length >= limit }"
        :show-file-list="showFileList"
        :before-upload="handleBeforeUpload"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        :on-exceed="handleExceed"
        :before-remove="handleBeforeDelete as any"
        :on-preview="handleFilePreview as any"
        :on-change="handleFileChange"
        :on-progress="handleProgress"
        :key="refreshKey"
      >
        <template v-if="drag">
          <el-icon class="el-icon--upload">
            <UploadFilled />
          </el-icon>
          <div class="el-upload__text">{{ t('formUpload.fileDragHint') }}<em>{{ t('formUpload.clickToUpload') }}</em></div>
        </template>
        <template v-else>
          <el-button type="primary" :disabled="disabled">
            <el-icon>
              <Upload />
            </el-icon>
            {{ computedUploadButtonText }}
          </el-button>
        </template>
      </el-upload>

      <div class="flex items-center gap-10px w-full" v-if="!isReplaceMode">
        <!-- 按钮容器 -->
        <div class="flex gap-10px ml-auto">
          <!-- 添加素材库按钮 -->
          <el-button v-if="!disabled && enableOssMediaManager && fileList.length < limit" type="success" size="small" @click="openOssMediaManager">
            <el-icon>
              <FolderOpened />
            </el-icon>
            {{ t('formUpload.selectFromLibrary') }}
          </el-button>

          <!-- 清空全部按钮 -->
          <el-button v-if="!disabled && enableClearAll && fileList.length > 1" type="danger" size="small" @click="handleClearAll">
            <el-icon>
              <Delete />
            </el-icon>
            {{ t('formUpload.clearAll') }}
          </el-button>
        </div>
      </div>

      <!-- 上传提示 -->
      <div v-if="showTip" class="el-upload__tip text-sm text-gray-500">
        {{ t('formUpload.uploadHintPrefix') }}
        <template v-if="fileSize">
          {{ t('formUpload.sizeLimit') }} <b class="text-[#f56c6c]">{{ fileSize }}MB</b>
        </template>
        <template v-if="fileType?.length">
          {{ t('formUpload.formatHint') }} <b class="text-[#f56c6c]">{{ fileType.join('/') }}</b>
        </template>
        {{ t('formUpload.fileSuffix') }}
        <template v-if="limit > 1">
          {{ t('formUpload.countLimit') }} <b class="text-[#f56c6c]">{{ limit }}</b> {{ t('formUpload.fileCountUnit') }}
        </template>
        <template v-if="enableDirectUpload">
          <b class="text-[#67c23a]">{{ t('formUpload.directMode') }}</b>
        </template>
      </div>

      <!-- 上传进度 -->
      <div v-if="uploadProgress > 0 && uploadProgress < 100" class="w-full">
        <el-progress :percentage="uploadProgress" :show-text="true"></el-progress>
      </div>
    </div>
  </template>

  <!-- 文件预览对话框 -->
  <AModal v-model="dialogVisible" :title="t('formUpload.filePreviewTitle')" mode="dialog" size="large" footer-type="close-only" :cancel-text="t('common.close')">
    <div class="text-center">
      <div class="text-6xl text-gray-400 mb-4">
        <el-icon v-if="isDocumentFile(previewFile?.url)">
          <Document />
        </el-icon>
        <el-icon v-else-if="isVideoFile(previewFile?.url)">
          <VideoPlay />
        </el-icon>
        <el-icon v-else-if="isAudioFile(previewFile?.url)">
          <Headset />
        </el-icon>
        <el-icon v-else>
          <Files />
        </el-icon>
      </div>
      <div class="mb-4">
        <div class="text-lg font-medium">{{ previewFile?.name }}</div>
        <div class="text-sm text-gray-500 mt-2">{{ previewFile?.url }}</div>
      </div>
      <div class="flex justify-center gap-2">
        <el-button type="primary" @click="downloadFile(previewFile)">
          <el-icon>
            <Download />
          </el-icon>
          {{ t('formUpload.downloadFile') }}
        </el-button>
      </div>
    </div>
  </AModal>

  <!-- 素材库对话框 -->
  <AOssMediaManager
    v-model="ossMediaManagerVisible"
    :fileSize="fileSize"
    :multi-select="limit > 1"
    :accept-file-types="fileType"
    @select="handleMediaSelect"
  />
</template>

<script setup lang="ts" name="AFormFileUpload">
import type { PresignedUrlBo, ConfirmDirectUploadBo } from '@/api/system/oss/oss/ossTypes'
import { deleteOss, getPresignedUrl, confirmDirectUpload, listOssByIds } from '@/api/system/oss/oss/ossApi'
import { useI18n } from '@/composables/useI18n'
import { objectToQuery } from '@/utils/object'
import { useToken } from '@/composables/useToken'
import { SystemConfig } from '@/systemConfig'
import { showMsgSuccess, showMsgError, showConfirm, showLoading, hideLoading } from '@/utils/modal'

const { t, te, isChinese } = useI18n()

/**
 * 文件项接口
 */
interface FileItem {
  /** 文件名 */
  name: string
  /** 文件URL */
  url: string
  /** OSS文件ID */
  ossId?: string | number
  /** 带签名的完整URL,用于回显 */
  signedUrl?: string
}

/**
 * 上传响应接口
 */
interface UploadResponse {
  /** 响应码 */
  code: number
  /** 响应消息 */
  msg?: string
  /** 响应数据 */
  data?: {
    /** 文件名 */
    fileName: string
    /** 原始文件名 */
    originalName: string
    /** 文件URL（私有桶可能带预签名参数，用于预览） */
    url: string
    /** 原始URL（不带预签名参数，用于存储） */
    originalUrl?: string
    /** OSS文件ID */
    ossId: string
    /** 更新时间 */
    updateTime: string
  }
}

/**
 * 文件上传组件Props接口
 */
interface AFormFileUploadProps {
  /**
   * 绑定值，可以是字符串、对象或数组
   * @default ''
   */
  modelValue?: string

  /**
   * 标签文本
   * @default '文件'
   */
  label?: string

  /**
   * 标签宽度，支持数字或字符串
   * @default undefined
   */
  labelWidth?: string | number

  /**
   * 表单域model数据字段名
   * @default ''
   */
  prop?: string

  /**
   * 是否显示表单项
   * @default true
   */
  showFormItem?: boolean

  /**
   * 是否禁用
   * @default false
   */
  disabled?: boolean

  /**
   * 上传文件数量限制
   * @default 5
   */
  limit?: number

  /**
   * 文件大小限制(MB)
   * @default 5
   */
  fileSize?: number

  /**
   * 允许的文件类型
   * @default ['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'txt', 'pdf']
   */
  fileType?: string[]

  /**
   * 是否显示上传提示
   * @default true
   */
  isShowTip?: boolean

  /**
   * 栅格占据的列数，支持数字、数字字符串、响应式对象或预设字符串
   * - 数字：固定span值，如 12
   * - 数字字符串：如 "12"，会自动转换为数字
   * - 响应式对象：{ xs: 24, sm: 24, md: 12, lg: 8, xl: 6 }
   * - 预设字符串：'auto' - 自动响应式布局
   * @default undefined
   */
  span?: SpanType

  /**
   * 是否启用素材库
   * @default true
   */
  enableOssMediaManager?: boolean

  /**
   * 是否启用清空全部功能
   * @default true
   */
  enableClearAll?: boolean

  /**
   * 上传参数，用于传递额外参数或替换模式
   * @default {}
   */
  uploadParams?: Record<string, any>

  /**
   * 提示信息
   * @default ''
   */
  tooltip?: string

  /**
   * 是否支持多选
   * @default true
   */
  multiple?: boolean

  /**
   * 是否启用拖拽上传
   * @default false
   */
  drag?: boolean

  /**
   * 是否自动上传
   * @default true
   */
  autoUpload?: boolean

  /**
   * 文件列表类型
   * @default 'text'
   */
  listType?: 'text' | 'picture' | 'picture-card'

  /**
   * 是否显示文件列表
   * @default true
   */
  showFileList?: boolean

  /**
   * 上传按钮文字
   * @default '选择文件'
   */
  uploadButtonText?: string

  /**
   * 是否启用直传模式
   * @default false
   */
  enableDirectUpload?: boolean

  /**
   * 模块名称，用于控制物理存储路径
   * @default undefined
   */
  moduleName?: string

  /**
   * 目录Id
   * @default undefined
   */
  directoryId?: string | number

  /**
   * 目录路径，用于控制逻辑分类，如：/文档/办公
   * @default undefined
   */
  directoryPath?: string

  /**
   * 响应式模式
   * - 'screen': 基于屏幕尺寸
   * - 'container': 基于容器尺寸（弹窗场景推荐）
   * - 'modal-size': 基于 AModal 的 size 属性
   * @default 'screen'
   */
  responsiveMode?: 'screen' | 'container' | 'modal-size'

  /**
   * 当 responsiveMode 为 'modal-size' 时使用
   * 应该传入 AModal 的 size 属性值
   */
  modalSize?: 'small' | 'medium' | 'large' | 'xl'

  /**
   * 返回值模式
   * - 'url': 返回文件URL地址（默认）
   * - 'ossId': 返回OSS文件ID
   * @default 'url'
   */
  returnMode?: 'url' | 'ossId'
}

// 使用 withDefaults 定义 props
const props = withDefaults(defineProps<AFormFileUploadProps>(), {
  modelValue: '',
  label: '文件',
  labelWidth: undefined,
  prop: '',
  showFormItem: true,
  disabled: false,
  limit: 1,
  fileSize: 5,
  fileType: () => ['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'txt', 'pdf'],
  isShowTip: true,
  span: undefined,
  enableOssMediaManager: false,
  enableClearAll: true,
  uploadParams: () => ({}),
  tooltip: '',
  multiple: true,
  drag: false,
  autoUpload: true,
  listType: 'text',
  showFileList: true,
  uploadButtonText: '选择文件',
  enableDirectUpload: false,
  moduleName: undefined,
  directoryId: undefined,
  directoryPath: undefined,
  returnMode: 'url'
})

// 定义 emit 事件
const emit = defineEmits(['update:modelValue', 'change', 'success', 'error', 'progress', 'exceed'])

// ========== 根据响应式模式选择对应的组合函数 ==========
const { computedSpan, shouldUseCol } = useResponsiveSpan(toRef(props, 'span'), {
  mode: props.responsiveMode,
  modalSize: toRef(props, 'modalSize')
})
// =========== 状态变量 ===========

/** 文件列表 */
const fileList = ref<FileItem[]>([])
/** 临时存储待添加的文件，等所有文件上传完成后统一添加 */
const pendingFiles = ref<FileItem[]>([])
/** 跟踪正在上传的文件数量 */
const pendingUploadCount = ref(0)
/** 文件预览对话框可见性 */
const dialogVisible = ref(false)
/** 预览的文件 */
const previewFile = ref<FileItem | null>(null)
/** 素材库对话框可见性 */
const ossMediaManagerVisible = ref(false)
/** 用于强制刷新上传组件 */
const refreshKey = ref(0)
/** 上传进度 */
const uploadProgress = ref(0)

// =========== DOM引用 ===========

/** 上传组件引用 */
const fileUploadRef = ref<ElUploadInstance>()

// =========== 上传配置 ===========

/** API基础路径 */
const baseUrl = SystemConfig.api.baseUrl
/** 上传URL */
const uploadFileUrl = computed(() => baseUrl + '/resource/oss/upload')
/** 请求头 */
const headers = ref(useToken().getAuthHeaders())

/**
 * 获取上传URL
 * 根据是否为替换操作返回不同的URL
 */
const getUploadUrl = computed(() => {
  let url = uploadFileUrl.value

  // 如果存在 uploadParams 且包含替换相关参数，使用替换 API
  if (props.uploadParams?.isReplace && props.uploadParams?.replaceOssId) {
    url = `${baseUrl}/resource/oss/replace/${props.uploadParams.replaceOssId}`
  } else {
    // 构建上传参数
    const params: Record<string, any> = { ...props.uploadParams }

    // 添加模块名称和目录路径参数
    if (props.moduleName) {
      params.moduleName = props.moduleName
    }
    if (props.directoryId) {
      params.directoryId = props.directoryId
    }
    if (props.directoryPath) {
      params.directoryPath = props.directoryPath
    }

    const queryString = objectToQuery(params)
    if (queryString) {
      url += `?${queryString}`
    }
  }
  return url
})

// =========== 计算属性 ===========

/**
 * 使用计算属性计算国际化标签文本
 */
const computedLabel = computed(() => {
  // 未显式传 label：用 prop 作为 key 自动从词典国际化（兼容原有行为）
  if (!props.label) return t(props.prop, props.label)
  // 显式传了 label：中文环境直接用 label，保证 label 可控；
  // 其它语言优先查 prop 词典翻译，命中用词典，否则回退 label
  if (isChinese.value) return props.label
  return te(props.prop) ? t(props.prop) : props.label
})

/**
 * 计算上传按钮文本（支持国际化）
 */
const computedUploadButtonText = computed(() => {
  // 如果用户自定义了按钮文本且不是默认值，则使用自定义值
  if (props.uploadButtonText && props.uploadButtonText !== '选择文件') {
    return props.uploadButtonText
  }
  return t('formUpload.selectFile')
})

/**
 * 是否显示上传提示
 */
const showTip = computed(() => props.isShowTip && (props.fileType?.length || props.fileSize))

/**
 * 计算接受的文件类型
 */
const fileAccept = computed(() => props.fileType?.map((type) => `.${type}`).join(',') || '')

/**
 * 是否为替换模式
 */
const isReplaceMode = computed(() => !!props.uploadParams?.isReplace)

/**
 * 用于显示的文件列表（为图片添加缓存参数用于显示）
 */
const displayFileList = computed(() => {
  return fileList.value.map((item) => {
    // 只有图片类型才添加缓存参数，解决浏览器缓存问题
    if (isImageFile(item.url)) {
      const separator = item.url.includes('?') ? '&' : '?'
      return {
        ...item,
        url: `${item.url}${separator}t=${Date.now()}`
      }
    }
    return item
  })
})

/**
 * 使用计算属性实现双向绑定
 */
const modelValue = computed({
  get() {
    return props.modelValue
  },
  set(value) {
    emit('update:modelValue', value)
    emit('change', value)
  }
})

// =========== 文件类型相关方法 ===========

/**
 * 判断文件是否为图片类型
 * @param url 文件URL或路径
 * @returns 是否为图片类型
 */
const isImageFile = (url: string): boolean => {
  if (!url) return false
  const imageTypes = ['.jpg', '.jpeg', '.png', '.gif', '.webp', '.bmp', '.svg']
  return imageTypes.some((type) => url.toLowerCase().endsWith(type))
}

/**
 * 判断文件是否为文档类型
 * @param url 文件URL或路径
 * @returns 是否为文档类型
 */
const isDocumentFile = (url?: string): boolean => {
  if (!url) return false
  const docTypes = ['.doc', '.docx', '.xls', '.xlsx', '.ppt', '.pptx', '.pdf', '.txt']
  return docTypes.some((type) => url.toLowerCase().endsWith(type))
}

/**
 * 判断文件是否为视频类型
 * @param url 文件URL或路径
 * @returns 是否为视频类型
 */
const isVideoFile = (url?: string): boolean => {
  if (!url) return false
  const videoTypes = ['.mp4', '.avi', '.mov', '.wmv', '.flv', '.mkv']
  return videoTypes.some((type) => url.toLowerCase().endsWith(type))
}

/**
 * 判断文件是否为音频类型
 * @param url 文件URL或路径
 * @returns 是否为音频类型
 */
const isAudioFile = (url?: string): boolean => {
  if (!url) return false
  const audioTypes = ['.mp3', '.wav', '.aac', '.flac', '.m4a']
  return audioTypes.some((type) => url.toLowerCase().endsWith(type))
}

// =========== 功能方法 ===========

/**
 * 更新模型值
 * 根据组件使用场景，返回不同格式的值
 */
const updateModelValue = () => {
  // 如果文件列表为空，返回空值
  if (fileList.value.length === 0) {
    modelValue.value = ''
    return
  }

  // 根据返回模式决定返回URL还是ossId
  if (props.returnMode === 'ossId') {
    // ossId模式：优先返回ossId，如果没有ossId则回退到URL（兼容旧数据）
    const filesWithOssId = fileList.value.filter((file) => file.ossId)
    const filesWithoutOssId = fileList.value.filter((file) => !file.ossId && file.url)

    if (filesWithOssId.length > 0 && filesWithoutOssId.length === 0) {
      // 所有文件都有ossId，返回ossId
      modelValue.value = filesWithOssId.map((file) => file.ossId).join(',')
    } else if (filesWithOssId.length === 0 && filesWithoutOssId.length > 0) {
      // 所有文件都没有ossId（旧数据），返回URL
      modelValue.value = filesWithoutOssId.map((file) => file.url).join(',')
    } else if (filesWithOssId.length > 0 && filesWithoutOssId.length > 0) {
      // 混合情况：优先返回ossId，但如果存在没有ossId的文件，则全部返回URL以保持一致性
      console.warn('检测到混合格式的文件（部分有ossId，部分没有），将返回URL格式以保持一致性')
      modelValue.value = fileList.value
        .filter((file) => file.url)
        .map((file) => file.url)
        .join(',')
    } else {
      modelValue.value = ''
    }
  } else {
    // 默认返回URL字符串，以逗号分隔
    modelValue.value = fileList.value
      .filter((file) => file.url)
      .map((file) => file.url)
      .join(',')
  }
}

/**
 * 从外部值初始化文件列表
 */
const initFileList = async () => {
  const val = props.modelValue

  // 如果值为空，清空文件列表
  if (!val) {
    fileList.value = []
    return
  }

  if (props.returnMode === 'ossId') {
    // ossId模式：需要兼容旧数据（可能是URL格式）
    const values = val.split(',').filter((item) => item.trim())

    // 判断数据格式：检查第一个值是否为纯数字（ossId）还是URL
    const isOssIdFormat = values.length > 0 && /^\d+$/.test(values[0].trim())

    if (isOssIdFormat) {
      // 纯ossId格式（保持字符串格式，避免雪花ID精度丢失）
      const ossIds = values.map((id) => id.trim())

      // 检查当前文件列表与模型值是否匹配
      const currentOssIds = fileList.value.filter((item) => item.ossId).map((item) => String(item.ossId))

      // 只有当两者不匹配时才更新文件列表
      if (JSON.stringify(currentOssIds.sort()) !== JSON.stringify(ossIds.sort())) {
        if (ossIds.length > 0) {
          showLoading(t('formUpload.loadingFileInfo'))
          const [err, data] = await listOssByIds(ossIds)
          if (!err && data) {
            fileList.value = data.map((ossItem) => ({
              name: ossItem.originalName || ossItem.fileName,
              url: ossItem.url,
              ossId: ossItem.ossId
            }))
          } else {
            console.error('获取OSS文件信息失败:', err)
            fileList.value = []
          }
          hideLoading()
        } else {
          fileList.value = []
        }
      }
    } else {
      // URL格式（兼容旧数据）：直接作为URL处理，不包含ossId
      const urls = values
      const currentUrls = fileList.value.map((item) => item.url)

      // 只有当两者不匹配时才更新文件列表
      if (JSON.stringify(currentUrls) !== JSON.stringify(urls)) {
        fileList.value = urls.map((url) => {
          const parts = url.split('/')
          return {
            name: parts[parts.length - 1],
            url: url
            // 注意：旧数据没有ossId，所以这里不设置ossId
          }
        })
      }
    }
  } else {
    // URL模式：直接解析URL
    const urls = val.split(',').filter((url) => url.trim())
    const currentUrls = fileList.value.map((item) => item.url)

    // 只有当两者不匹配时才更新文件列表
    if (JSON.stringify(currentUrls) !== JSON.stringify(urls)) {
      fileList.value = urls.map((url) => {
        const cleanUrl = extractCleanUrl(url)
        const parts = url.split('/')
        return {
          name: parts[parts.length - 1].split('?')[0],
          url: cleanUrl,
          signedUrl: url
        }
      })
    }
  }
}

/**
 * 强制刷新上传组件
 */
const forceRefreshUpload = async () => {
  await nextTick()
  refreshKey.value += 1
}

/**
 * 下载文件
 * @param file 要下载的文件
 */
const downloadFile = (file: FileItem | null) => {
  if (!file) return

  // 创建一个链接并模拟点击下载
  const a = document.createElement('a')
  a.href = file.url
  a.download = file.name
  a.target = '_blank'
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
}

/**
 * 从完整URL中提取不带签名的干净URL
 * @param fullUrl 完整URL(可能带签名参数)
 * @returns 不带签名的干净URL
 */
const extractCleanUrl = (fullUrl: string): string => {
  if (!fullUrl) return ''

  try {
    const url = new URL(fullUrl)
    // 移除所有AWS签名相关的查询参数
    url.searchParams.delete('X-Amz-Algorithm')
    url.searchParams.delete('X-Amz-Date')
    url.searchParams.delete('X-Amz-SignedHeaders')
    url.searchParams.delete('X-Amz-Credential')
    url.searchParams.delete('X-Amz-Expires')
    url.searchParams.delete('X-Amz-Signature')
    // 移除时间戳参数(用于公共链接刷新缓存)
    url.searchParams.delete('t')

    // 如果没有其他参数了,返回不带?的URL
    const cleanUrl = url.toString()
    return cleanUrl.endsWith('?') ? cleanUrl.slice(0, -1) : cleanUrl
  } catch (error) {
    // 如果URL解析失败,尝试简单的字符串处理
    return fullUrl.split('?')[0]
  }
}

/**
 * 检查URL是否已存在于文件列表中
 * @param url 要检查的URL
 * @returns 是否存在
 */
const isUrlExist = (url: string): boolean => {
  const cleanUrl = extractCleanUrl(url)
  return fileList.value.some((item) => extractCleanUrl(item.url) === cleanUrl)
}

/**
 * 直传上传方法
 * @param file 要上传的文件
 * @param presignedData 预签名数据
 */
const handleDirectUpload = async (file: File, presignedData: any): Promise<void> => {
  try {
    // S3等云存储的直传逻辑
    const [err2] = await http.put(presignedData.presignedUrl, file, {
      headers: {
        'Content-Type': file.type,
        // 不使用默认的拦截器配置
        auth: false,
        tenant: false,
        repeatSubmit: false
      } as any,
      onUploadProgress: (progressEvent: any) => {
        uploadProgress.value = Math.round((progressEvent.loaded / progressEvent.total) * 100)
      }
    })

    if (err2) {
      throw new Error(err2?.message || t('formUpload.uploadFailed'))
    }

    // 确认上传完成
    const confirmParams: ConfirmDirectUploadBo = {
      fileName: file.name,
      fileKey: presignedData.fileKey,
      fileUrl: presignedData.fileUrl,
      moduleName: props.moduleName || props.uploadParams?.moduleName,
      directoryId: props.directoryId || props.uploadParams?.directoryId,
      directoryPath: props.directoryPath || props.uploadParams?.directoryPath,
      fileSize: file.size
    }

    const [err3, uploadResult] = await confirmDirectUpload(confirmParams)

    if (err3 || !uploadResult) {
      throw new Error(err3?.message || '确认上传失败')
    }

    // 处理上传成功
    const mockResponse = {
      code: 200,
      data: {
        fileName: uploadResult.fileName,
        originalName: uploadResult.fileName,
        url: uploadResult.url,
        ossId: uploadResult.ossId,
        updateTime: uploadResult.updateTime
      }
    }

    // 调用原有的成功处理逻辑
    handleUploadSuccess(mockResponse, { name: file.name } as ElUploadFile)
  } catch (error: any) {
    // 调用原有的错误处理逻辑
    handleUploadError(error, { name: file.name } as ElUploadFile)
  }
}

/**
 * 清空全部文件
 */
const handleClearAll = async () => {
  if (fileList.value.length === 0) {
    showMsgError(t('formUpload.noFileToClear'))
    return
  }

  // 分类文件：有ossId的和没有ossId的
  const filesWithOssId = fileList.value.filter((file) => file.ossId)
  const filesWithoutOssId = fileList.value.filter((file) => !file.ossId)

  // 构建确认消息
  let confirmMessage = t('formUpload.confirmClearAllFiles')
  if (filesWithOssId.length > 0 && filesWithoutOssId.length > 0) {
    confirmMessage = t('formUpload.clearConfirmCloudAndLocal', { cloudCount: filesWithOssId.length, localCount: filesWithoutOssId.length })
  } else if (filesWithOssId.length > 0) {
    confirmMessage = t('formUpload.clearConfirmCloud', { count: filesWithOssId.length })
  } else {
    confirmMessage = t('formUpload.clearConfirmLocal', { count: filesWithoutOssId.length })
  }

  const [confirmErr] = await showConfirm(confirmMessage)
  if (confirmErr) return

  // 如果有需要删除的云端文件，显示加载提示
  if (filesWithOssId.length > 0) {
    showLoading(t('formUpload.deletingCloudFiles', { count: filesWithOssId.length }))
  }

  let successMessage = ''
  let hasError = false

  // 处理有ossId的文件 - 需要调用删除接口
  if (filesWithOssId.length > 0) {
    // 并发删除所有云端文件
    const deletePromises = filesWithOssId.map(async (file) => {
      const [error] = await deleteOss(file.ossId)
      if (error) {
        console.error(`删除云端文件 ${file.name} 失败:`, error)
        return { error: true, fileName: file.name, ossId: file.ossId, message: error.message || error }
      }
      return { error: false, fileName: file.name, ossId: file.ossId }
    })

    const deleteResults = await Promise.all(deletePromises)

    // 分析删除结果
    const failedDeletes = deleteResults.filter((result) => result.error)
    const successfulDeletes = deleteResults.filter((result) => !result.error)

    if (failedDeletes.length > 0) {
      hasError = true
      const failedFileNames = failedDeletes.map((result) => result.fileName).join('、')
      showMsgError(t('formUpload.deleteCloudFileFailed') + failedFileNames)

      // 获取删除失败的ossId列表
      const failedOssIds = failedDeletes.map((result) => result.ossId)

      // 从文件列表中移除删除成功的文件，保留删除失败的文件
      fileList.value = fileList.value.filter(
        (file) => failedOssIds.includes(file.ossId) // 保留删除失败的文件
      )

      successMessage += t('formUpload.cloudFilesDeleted', { count: successfulDeletes.length })
    } else {
      // 所有云端文件删除成功
      successMessage += t('formUpload.cloudFilesDeleted', { count: filesWithOssId.length })
    }
  }

  // 处理没有ossId的文件 - 直接从列表中移除
  if (filesWithoutOssId.length > 0) {
    if (!hasError) {
      // 如果云端文件都删除成功，或者没有云端文件，直接清空所有
      fileList.value = []
      if (successMessage) {
        successMessage += '，' + t('formUpload.localFilesCleared', { count: filesWithoutOssId.length })
      } else {
        successMessage = t('formUpload.localFilesCleared', { count: filesWithoutOssId.length })
      }
    } else {
      // 有云端文件删除失败，只清空本地文件（无ossId的）
      fileList.value = fileList.value.filter((file) => file.ossId) // 保留有ossId的（失败的）
      if (successMessage) {
        successMessage += '，' + t('formUpload.localFilesCleared', { count: filesWithoutOssId.length })
      } else {
        successMessage = t('formUpload.localFilesCleared', { count: filesWithoutOssId.length })
      }
    }
  } else if (!hasError && filesWithOssId.length > 0) {
    // 只有云端文件且都删除成功
    fileList.value = []
  }

  // 更新绑定值
  updateModelValue()

  // 强制刷新上传组件
  forceRefreshUpload()

  // 显示成功消息
  if (successMessage && !hasError) {
    showMsgSuccess(t('formUpload.clearComplete') + successMessage)
  } else if (successMessage && hasError) {
    showMsgSuccess(t('formUpload.partialClearComplete') + successMessage)
  }

  // 关闭加载提示
  if (filesWithOssId.length > 0) {
    hideLoading()
  }
}

// =========== 上传处理方法 ===========

/**
 * 上传前检查
 * @param file 要上传的文件
 * @returns 是否允许上传
 */
const handleBeforeUpload = async (file: File) => {
  // 校验文件类型
  if (props.fileType?.length) {
    const fileName = file.name.split('.')
    const fileExt = fileName[fileName.length - 1].toLowerCase()
    const isTypeOk = props.fileType.some((type) => type.toLowerCase() === fileExt)
    if (!isTypeOk) {
      showMsgError(t('formUpload.fileFormatError', { formats: props.fileType.join('/') }))
      return false
    }
  }

  // 校验文件名是否包含特殊字符
  if (file.name.includes(',')) {
    showMsgError(t('formUpload.filenameHasCommaError'))
    return false
  }

  // 校验文件大小
  if (props.fileSize && file.size / 1024 / 1024 > props.fileSize) {
    showMsgError(t('formUpload.fileSizeError', { size: props.fileSize }))
    return false
  }

  // 增加待上传计数
  pendingUploadCount.value++

  // 只在第一个文件开始上传时显示加载提示
  if (pendingUploadCount.value === 1) {
    showLoading(t('formUpload.uploadingFile'))
  }

  // 如果启用直传模式，需要先获取预签名URL判断存储类型
  if (props.enableDirectUpload) {
    // 先获取预签名URL判断存储类型
    try {
      const presignedParams: PresignedUrlBo = {
        fileName: file.name,
        fileType: file.type,
        moduleName: props.moduleName || props.uploadParams?.moduleName,
        directoryId: props.directoryId || props.uploadParams?.directoryId,
        directoryPath: props.directoryPath || props.uploadParams?.directoryPath
      }

      const [err1, presignedData] = await getPresignedUrl(presignedParams)

      if (err1 || !presignedData) {
        console.error('获取预签名URL失败，降级到默认上传:', err1)
        // 如果获取预签名URL失败，降级到默认上传
        return true
      }

      // 检查是否是本地存储
      if (presignedData.presignedUrl.includes('/resource/oss/upload')) {
        console.log('检测到本地存储，使用自动上传模式')
        // 本地存储使用默认上传，返回true继续
        return true
      } else {
        console.log('检测到云存储，执行直传模式')
        // 云存储执行直传，传入已获取的预签名数据
        handleDirectUpload(file, presignedData)
        return false // 阻止默认上传
      }
    } catch (error: any) {
      console.error('直传预检查失败，降级到默认上传:', error)
      // 如果直传预检查失败，降级到默认上传
      return true
    }
  }

  return true
}

/**
 * 超出文件数量限制处理
 */
const handleExceed = () => {
  showMsgError(t('formUpload.fileCountError', { limit: props.limit }))
  emit('exceed')
}

/**
 * 文件状态改变时的钩子
 * @param file 文件对象
 * @param fileList 文件列表
 */
const handleFileChange = (file: ElUploadFile, fileList: ElUploadFile[]) => {
  // 可以在这里处理文件状态变化
}

/**
 * 文件上传时的钩子
 * @param event 进度事件
 * @param file 文件对象
 * @param fileList 文件列表
 */
const handleProgress = (event: any, file: ElUploadFile, fileList: ElUploadFile[]) => {
  uploadProgress.value = Math.round(event.percent || 0)
  emit('progress', event, file, fileList)
}

/**
 * 上传成功回调
 * @param res 上传响应
 * @param file 上传的文件
 */
const handleUploadSuccess = async (res: UploadResponse, file: ElUploadFile) => {
  // 减少待上传计数
  pendingUploadCount.value--
  uploadProgress.value = 0

  if (res.code === 200 && res.data) {
    // 优先使用后端返回的 originalUrl（干净URL），如果没有则兜底使用 extractCleanUrl
    const signedUrl = res.data.url // 服务器返回的完整URL（私有桶带签名，用于预览）
    const cleanUrl = res.data.originalUrl || extractCleanUrl(signedUrl) // 优先使用 originalUrl，兜底提取

    // 创建新文件对象
    const newFile = {
      name: res.data.originalName || res.data.fileName,
      url: cleanUrl, // 存储不带签名的干净URL
      signedUrl: signedUrl, // 存储带签名的完整URL用于显示
      ossId: res.data.ossId
    }

    // 如果是替换模式
    if (isReplaceMode.value) {
      // 替换当前的文件（应该只有一个）
      fileList.value = [newFile]
      updateModelValue()
      showMsgSuccess(t('formUpload.fileReplaceSuccess'))
      emit('success', res, file, newFile)
    } else {
      // 常规上传：存储到临时列表，等所有文件上传完成后统一添加
      pendingFiles.value.push(newFile)

      // 只有当所有文件都上传完成时才处理
      if (pendingUploadCount.value === 0) {
        // 批量添加所有文件
        fileList.value.push(...pendingFiles.value)
        pendingFiles.value = []
        updateModelValue()
        await nextTick()
        await forceRefreshUpload()
        showMsgSuccess(props.enableDirectUpload ? t('formUpload.fileDirectUploadSuccess') : t('formUpload.fileUploadSuccess'))
      }
      emit('success', res, file, newFile)
    }
  } else {
    showMsgError(res.msg || t('formUpload.uploadFailed'))
    fileUploadRef.value?.handleRemove(file)
    emit('error', res.msg || t('formUpload.uploadFailed'), file)
  }

  // 如果没有待上传的文件，关闭加载提示
  if (pendingUploadCount.value === 0) {
    hideLoading()
  }
}

/**
 * 删除文件
 * @param file 要删除的文件
 * @returns 是否允许删除
 */
const handleBeforeDelete = async (file: FileItem): Promise<boolean> => {
  // 由于displayFileList使用的是带签名的URL,需要找到原始URL对应的文件
  let fileItem: FileItem | undefined

  // 先尝试通过signedUrl匹配
  fileItem = fileList.value.find((item) => item.signedUrl === file.url)

  // 如果没找到,尝试通过清理后的URL匹配
  if (!fileItem) {
    const cleanUrl = extractCleanUrl(file.url)
    fileItem = fileList.value.find((item) => extractCleanUrl(item.url) === cleanUrl)
  }

  // 如果还是找不到，通过文件名匹配
  if (!fileItem) {
    fileItem = fileList.value.find((item) => item.name === file.name)
  }

  if (fileItem?.ossId) {
    const [confirmErr] = await showConfirm(t('formUpload.confirmDeleteCloudFile'))
    // 用户取消删除操作
    if (confirmErr) return false

    // 调用删除接口
    await deleteOss(fileItem.ossId)
  }

  // 从文件列表中移除该文件（使用找到的fileItem或按原始逻辑）
  if (fileItem) {
    fileList.value = fileList.value.filter((item) => item !== fileItem)
  } else {
    // 如果还是没找到，尝试用文件名删除
    fileList.value = fileList.value.filter((item) => item.name !== file.name)
  }

  updateModelValue()

  // 强制刷新上传组件，清除Element Plus内部的文件列表缓存
  await nextTick()
  refreshKey.value += 1

  return true
}

/**
 * 上传失败处理
 * @param err 错误信息
 * @param file 文件对象
 */
const handleUploadError = (err: any, file: ElUploadFile) => {
  // 减少待上传计数
  pendingUploadCount.value--
  uploadProgress.value = 0

  const errorMsg = err.message || err || t('formUpload.unknownError')
  showMsgError((props.enableDirectUpload ? t('formUpload.directUploadFileFailed') : t('formUpload.uploadFileFailed')) + errorMsg)
  emit('error', errorMsg, file)

  // 如果所有文件都处理完毕，处理待添加的文件
  if (pendingUploadCount.value === 0 && pendingFiles.value.length > 0) {
    fileList.value.push(...pendingFiles.value)
    pendingFiles.value = []
    updateModelValue()
    forceRefreshUpload()
  }

  // 如果没有待上传的文件，关闭加载提示
  if (pendingUploadCount.value === 0) {
    hideLoading()
  }
}

/**
 * 文件预览
 * @param file 预览的文件
 */
const handleFilePreview = (file: FileItem) => {
  previewFile.value = file
  dialogVisible.value = true
}

// =========== 素材库相关 ===========

/**
 * 打开素材库
 */
const openOssMediaManager = () => {
  ossMediaManagerVisible.value = true
}

/**
 * 处理从素材库选择的文件
 * @param selected 选中的文件或文件数组
 */
const handleMediaSelect = async (selected: any) => {
  // 保存一份当前的文件列表
  const currentFileList = [...fileList.value]

  // 检查是否还有剩余空间
  const remainingSpace = props.limit - currentFileList.length
  if (remainingSpace <= 0) {
    showMsgError(t('formUpload.fileCountError', { limit: props.limit }))
    return
  }

  // 创建一个新的文件列表，用于存储最终结果
  const newFileList = [...currentFileList]

  if (Array.isArray(selected)) {
    // 多选模式
    // 只添加限制数量内的文件
    const filesToAdd = selected.slice(0, remainingSpace)

    // 添加新选择的文件，同时检查是否重复
    for (const file of filesToAdd) {
      const cleanUrl = extractCleanUrl(file.url)
      if (!isUrlExist(cleanUrl)) {
        newFileList.push({
          name: file.originalName || file.fileName,
          url: cleanUrl,
          signedUrl: file.url,
          ossId: file.ossId
        })
      }
    }
  } else {
    // 单选模式
    const cleanUrl = extractCleanUrl(selected.url)
    if (!isUrlExist(cleanUrl)) {
      newFileList.push({
        name: selected.originalName || selected.fileName,
        url: cleanUrl,
        signedUrl: selected.url,
        ossId: selected.ossId
      })
    }
  }

  // 处理完成后，重置文件列表
  fileList.value = []

  // 在下一个事件循环中更新，确保视图已经刷新
  await nextTick()
  // 更新为新的文件列表
  fileList.value = newFileList

  // 更新绑定值
  updateModelValue()

  // 强制刷新上传组件
  await forceRefreshUpload()

  showMsgSuccess(t('formUpload.selectFileSuccess'))
}

// =========== 生命周期与监听 ===========

// 观察外部modelValue变化
watch(
  () => props.modelValue,
  async (newVal) => {
    if ((!newVal || (typeof newVal === 'string' && newVal === '')) && fileList.value.length > 0) {
      fileList.value = []
    } else if (newVal) {
      await initFileList()
      // 初始化后立即更新v-model,确保清理掉签名参数
      // 这样即使用户没有做任何修改,提交时也是干净的URL
      await nextTick()
      updateModelValue()
    }
  },
  { immediate: true }
)

// 组件挂载时初始化
onMounted(() => {
  initFileList()
})
</script>

<style scoped>
/* 当文件数达到上限时隐藏上传按钮 */
:deep(.hide .el-upload--text) {
  display: none;
}

/* 优化上传列表样式 */
:deep(.el-upload-list) {
  margin-top: 0;
}

/* 拖拽上传样式优化 */
:deep(.el-upload-dragger) {
  padding: 40px;
}

/* 进度条样式 */
:deep(.el-progress) {
  margin-top: 10px;
}

/* 确保长文件名能正确显示省略号 */
:deep(.el-upload-list--text .el-upload-list__item) {
  min-width: 200px !important;
  max-width: calc(100% - 130px) !important;
}
</style>
