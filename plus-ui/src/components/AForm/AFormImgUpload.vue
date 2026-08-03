<!--用法示例
// 基础图片上传
<AFormImgUpload label="图片" v-model="form.imageUrls" prop="imageUrls" :span="12"></AFormImgUpload>
// 单张图片上传（头像/封面）
<AFormImgUpload label="头像" v-model="form.avatar" prop="avatar" :limit="1" :span="12"></AFormImgUpload>
// 多张图片上传（相册模式）
<AFormImgUpload label="产品相册" v-model="form.gallery" prop="gallery" :limit="9" list-type="picture-card" :span="24"></AFormImgUpload>
// 小尺寸图片上传（适用于表格）
<AFormImgUpload v-model="form.img" :show-form-item="false" :limit="1" size="small"></AFormImgUpload>
// 限制图片类型和大小
<AFormImgUpload label="封面图" v-model="form.cover" prop="cover" :file-type="['jpg', 'png', 'webp']" :file-size="5" :span="12"></AFormImgUpload>
// 拖拽上传模式
<AFormImgUpload label="批量图片" v-model="form.images" prop="images" :drag="true" :limit="20" :span="24"></AFormImgUpload>
// 启用图片压缩
<AFormImgUpload label="高清图片" v-model="form.images" prop="images" :compress-support="true" :compress-target-size="500" :span="12"></AFormImgUpload>
// 启用直传模式（云存储）
<AFormImgUpload label="宣传图" v-model="form.images" prop="images" :enable-direct-upload="true" module-name="promotion" :span="12"></AFormImgUpload>
// 控制存储路径和目录
<AFormImgUpload label="产品图片" v-model="form.images" prop="images" module-name="product" directory-path="/图片/商品" :directory-id="456" :span="12"></AFormImgUpload>
// 启用素材库选择
<AFormImgUpload label="背景图" v-model="form.images" prop="images" :enable-oss-media-manager="true" :span="12"></AFormImgUpload>
// 带提示信息的图片上传
<AFormImgUpload label="身份证" v-model="form.idCards" prop="idCards" tooltip="请上传清晰的身份证照片" :limit="2" :span="12"></AFormImgUpload>
// 不含el-form-item容器的简单图片上传
<AFormImgUpload v-model="form.imageUrls" :show-form-item="false" :limit="1"></AFormImgUpload>
// 替换模式 - 由父组件控制
<AFormImgUpload label="Logo" v-model="form.logo" prop="logo" :upload-params="{isReplace: true, replaceOssId: 123}" :limit="1" :span="12"></AFormImgUpload>
// 返回ossId模式
<AFormImgUpload label="产品图片" v-model="form.productImages" prop="productImages" return-mode="ossId" :limit="5" :span="12"></AFormImgUpload>
// 返回URL模式（默认）- 私有库文件会自动生成预签名URL
<AFormImgUpload label="宣传图" v-model="form.bannerImages" prop="bannerImages" return-mode="url" :limit="3" :span="12"></AFormImgUpload>
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
          ref="imageUploadRef"
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
          :on-preview="handleImagePreview as any"
          :on-progress="handleProgress"
          :key="refreshKey"
        >
          <template v-if="drag">
            <el-icon class="el-icon--upload">
              <UploadFilled />
            </el-icon>
            <div class="el-upload__text">
              {{ t('formUpload.dragHint') }}<em>{{ t('formUpload.clickToUpload') }}</em>
            </div>
          </template>
          <template v-else-if="listType === 'picture-card'">
            <el-icon>
              <Plus />
            </el-icon>
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
            <!-- 添加素材库按钮 - 只在未达到限制时显示 -->
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
            {{ t('formUpload.sizeLimit') }}<b class="text-[#f56c6c]">{{ fileSize }}MB</b>
          </template>
          <template v-if="fileType?.length">
            {{ t('formUpload.formatHint') }}<b class="text-[#f56c6c]">{{ fileType.join('/') }}</b>
          </template>
          {{ t('formUpload.imageSuffix') }}
          <template v-if="limit > 1">
            {{ t('formUpload.countLimit') }}<b class="text-[#f56c6c]">{{ limit }}</b
            >{{ t('formUpload.countUnit') }}
          </template>
          <template v-if="compressSupport">
            {{ t('formUpload.compressHint') }}<b class="text-[#f56c6c]">{{ compressTargetSize }}KB</b>{{ t('formUpload.willCompress') }}
          </template>
          <template v-if="enableDirectUpload">
            <b class="text-[#67c23a]">{{ t('formUpload.directMode') }}</b>
          </template>
        </div>

        <!-- 上传进度 -->
        <div v-if="uploadProgress > 0 && uploadProgress < 100" class="w-full">
          <el-progress :percentage="uploadProgress" :show-text="true">
            <template #default="{ percentage }">
              <span class="text-sm">{{ percentage }}%</span>
            </template>
          </el-progress>
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
        ref="imageUploadRef"
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
        :on-preview="handleImagePreview as any"
        :on-progress="handleProgress"
        :key="refreshKey"
      >
        <template v-if="drag">
          <el-icon class="el-icon--upload">
            <UploadFilled />
          </el-icon>
          <div class="el-upload__text">
            {{ t('formUpload.dragHint') }}<em>{{ t('formUpload.clickToUpload') }}</em>
          </div>
        </template>
        <template v-else-if="listType === 'picture-card'">
          <el-icon>
            <Plus />
          </el-icon>
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
          <!-- 添加素材库按钮 - 只在未达到限制时显示 -->
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
          {{ t('formUpload.sizeLimit') }}<b class="text-[#f56c6c]">{{ fileSize }}MB</b>
        </template>
        <template v-if="fileType?.length">
          {{ t('formUpload.formatHint') }}<b class="text-[#f56c6c]">{{ fileType.join('/') }}</b>
        </template>
        {{ t('formUpload.imageSuffix') }}
        <template v-if="limit > 1">
          {{ t('formUpload.countLimit') }}<b class="text-[#f56c6c]">{{ limit }}</b
          >{{ t('formUpload.countUnit') }}
        </template>
        <template v-if="compressSupport">
          {{ t('formUpload.compressHint') }}<b class="text-[#f56c6c]">{{ compressTargetSize }}KB</b>{{ t('formUpload.willCompress') }}
        </template>
        <template v-if="enableDirectUpload">
          <b class="text-[#67c23a]">{{ t('formUpload.directMode') }}</b>
        </template>
      </div>

      <!-- 上传进度 -->
      <div v-if="uploadProgress > 0 && uploadProgress < 100" class="w-full">
        <el-progress :percentage="uploadProgress" :show-text="true">
          <template #default="{ percentage }">
            <span class="text-sm">{{ percentage }}%</span>
          </template>
        </el-progress>
      </div>
    </div>
  </el-form-item>

  <!--showFormItem为false 只显示图片上传器-->
  <template v-else>
    <div class="flex flex-col items-start gap-10px">
      <el-upload
        ref="imageUploadRef"
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
        :on-preview="handleImagePreview as any"
        :on-progress="handleProgress"
        :key="refreshKey"
      >
        <template v-if="drag">
          <el-icon class="el-icon--upload">
            <UploadFilled />
          </el-icon>
          <div class="el-upload__text">
            {{ t('formUpload.dragHint') }}<em>{{ t('formUpload.clickToUpload') }}</em>
          </div>
        </template>
        <template v-else-if="listType === 'picture-card'">
          <el-icon>
            <Plus />
          </el-icon>
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
          <!-- 添加素材库按钮 - 只在未达到限制时显示 -->
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
          {{ t('formUpload.sizeLimit') }}<b class="text-[#f56c6c]">{{ fileSize }}MB</b>
        </template>
        <template v-if="fileType?.length">
          {{ t('formUpload.formatHint') }}<b class="text-[#f56c6c]">{{ fileType.join('/') }}</b>
        </template>
        {{ t('formUpload.imageSuffix') }}
        <template v-if="limit > 1">
          {{ t('formUpload.countLimit') }}<b class="text-[#f56c6c]">{{ limit }}</b
          >{{ t('formUpload.countUnit') }}
        </template>
        <template v-if="compressSupport">
          {{ t('formUpload.compressHint') }}<b class="text-[#f56c6c]">{{ compressTargetSize }}KB</b>{{ t('formUpload.willCompress') }}
        </template>
        <template v-if="enableDirectUpload">
          <b class="text-[#67c23a]">{{ t('formUpload.directMode') }}</b>
        </template>
      </div>

      <!-- 上传进度 -->
      <div v-if="uploadProgress > 0 && uploadProgress < 100" class="w-full">
        <el-progress :percentage="uploadProgress" :show-text="true">
          <template #default="{ percentage }">
            <span class="text-sm">{{ percentage }}%</span>
          </template>
        </el-progress>
      </div>
    </div>
  </template>

  <!-- 图片预览对话框 -->
  <AModal
    v-model="dialogVisible"
    :title="t('formUpload.previewTitle')"
    mode="dialog"
    size="large"
    footer-type="close-only"
    :cancel-text="t('button.close')"
  >
    <div class="text-center">
      <el-image v-if="previewFile?.url" :src="previewFile.url" fit="contain" class="max-w-full max-h-[70vh]" />
      <div class="mt-4">
        <div class="text-lg font-medium">{{ previewFile?.name }}</div>
        <div class="text-sm text-gray-500 mt-2">{{ previewFile?.url }}</div>
      </div>
      <div class="flex justify-center gap-2 mt-4">
        <el-button type="primary" @click="downloadFile(previewFile)">
          <el-icon>
            <Download />
          </el-icon>
          {{ t('formUpload.downloadImage') }}
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

<script setup lang="ts" name="AFormImgUpload">
import { compressAccurately } from 'image-conversion'
import type { PresignedUrlBo, ConfirmDirectUploadBo } from '@/api/system/oss/oss/ossTypes'
import { useI18n } from '@/composables/useI18n'
import { deleteOss, getPresignedUrl, confirmDirectUpload, listOssByIds } from '@/api/system/oss/oss/ossApi'
import { objectToQuery } from '@/utils/object'
import { useToken } from '@/composables/useToken'
import { SystemConfig } from '@/systemConfig'
import { showMsgError, showLoading, showMsgSuccess, showConfirm, hideLoading } from '@/utils/modal'

const { t, te, isChinese } = useI18n()

/**
 * 图片项接口
 */
interface ImageItem {
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
 * 图片上传组件Props接口
 */
interface AFormImgUploadProps {
  /**
   * 绑定值，可以是字符串、对象或数组
   * @default ''
   */
  modelValue?: string

  /**
   * 标签文本
   * @default '图片'
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
   * @default 1
   */
  limit?: number

  /**
   * 文件大小限制(MB)
   * @default 5
   */
  fileSize?: number

  /**
   * 允许的图片类型
   * @default ['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp']
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
   * 是否支持图片压缩
   * @default false
   */
  compressSupport?: boolean

  /**
   * 压缩目标大小(KB)
   * @default 300
   */
  compressTargetSize?: number

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
   * 上传参数，用于传递额外参数如目录ID或替换模式
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
   * @default 'picture-card'
   */
  listType?: 'text' | 'picture' | 'picture-card'

  /**
   * 是否显示文件列表
   * @default true
   */
  showFileList?: boolean

  /**
   * 上传按钮文字
   * @default '选择图片'
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
   * - 'url': 返回图片URL地址（默认）
   * - 'ossId': 返回OSS文件ID
   * @default 'url'
   */
  returnMode?: 'url' | 'ossId'

  /**
   * 图片卡片尺寸
   * - 'small': 60x60px (适用于表格)
   * - 'medium': 100x100px
   * - 'large': 148x148px (默认)
   * @default 'large'
   */
  size?: 'small' | 'medium' | 'large'
}

// 使用 withDefaults 定义 props，提供默认值
const props = withDefaults(defineProps<AFormImgUploadProps>(), {
  modelValue: '',
  label: '图片',
  labelWidth: undefined,
  prop: '',
  showFormItem: true,
  disabled: false,
  limit: 1,
  fileSize: 5,
  fileType: () => ['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp'],
  isShowTip: true,
  span: undefined,
  compressSupport: false,
  compressTargetSize: 300,
  enableOssMediaManager: false,
  enableClearAll: true,
  uploadParams: () => ({}),
  tooltip: '',
  multiple: true,
  drag: false,
  autoUpload: true,
  listType: 'picture-card',
  showFileList: true,
  uploadButtonText: undefined,
  enableDirectUpload: false,
  moduleName: undefined,
  directoryId: undefined,
  directoryPath: undefined,
  returnMode: 'url',
  size: 'large'
})

// 定义 emit 事件
const emit = defineEmits(['update:modelValue', 'change', 'success', 'error', 'progress', 'exceed'])

// ========== 根据响应式模式选择对应的组合函数 ==========
const { computedSpan, shouldUseCol } = useResponsiveSpan(toRef(props, 'span'), {
  mode: props.responsiveMode,
  modalSize: toRef(props, 'modalSize')
})
// =========== 状态变量 ===========

/** 图片列表 */
const fileList = ref<ImageItem[]>([])
/** 跟踪正在上传的文件数量 */
const pendingUploadCount = ref(0)
/** 临时存储待添加的文件，等所有文件上传完成后统一添加 */
const pendingFiles = ref<ImageItem[]>([])
/** 图片预览对话框可见性 */
const dialogVisible = ref(false)
/** 预览的图片 */
const previewFile = ref<ImageItem | null>(null)
/** 素材库对话框可见性 */
const ossMediaManagerVisible = ref(false)
/** 用于强制刷新上传组件 */
const refreshKey = ref(0)
/** 上传进度 */
const uploadProgress = ref(0)

// =========== DOM引用 ===========

/** 上传组件引用 */
const imageUploadRef = ref<ElUploadInstance>()

// =========== 上传配置 ===========

/** API基础路径 */
const baseUrl = SystemConfig.api.baseUrl
/** 上传URL */
const uploadImageUrl = computed(() => baseUrl + '/resource/oss/upload')
/** 请求头 */
const headers = ref(useToken().getAuthHeaders())

/**
 * 获取上传URL
 * 根据是否为替换操作返回不同的URL
 */
const getUploadUrl = computed(() => {
  let url = uploadImageUrl.value

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
 * 使用计算属性计算国际化上传按钮文本
 */
const computedUploadButtonText = computed(() => {
  return props.uploadButtonText || t('formUpload.selectImage')
})

/**
 * 是否显示上传提示
 */
const showTip = computed(() => props.isShowTip && (props.fileType?.length || props.fileSize))

/**
 * 计算接受的图片类型
 */
const fileAccept = computed(() => props.fileType?.map((type) => `.${type}`).join(',') || 'image/*')

/**
 * 是否为替换模式
 */
const isReplaceMode = computed(() => !!props.uploadParams?.isReplace)

/**
 * 根据 size 属性返回对应的像素尺寸
 */
const uploadCardSize = computed(() => {
  const sizeMap = {
    small: 60,
    medium: 100,
    large: 148
  }
  return sizeMap[props.size] || 148
})

/**
 * 用于显示的文件列表（智能缓存参数处理）
 * 预签名URL不添加缓存参数，避免影响签名；普通URL添加缓存参数解决浏览器缓存问题
 */
const displayFileList = computed(() => {
  return fileList.value.map((item) => {
    // 优先使用带签名的URL用于显示,如果没有则使用原始URL
    const displayUrl = item.signedUrl || item.url

    // 检查是否为各云服务商的预签名URL
    const isPresignedUrl =
      // AWS S3 / MinIO
      displayUrl.includes('X-Amz-Algorithm') ||
      displayUrl.includes('X-Amz-Signature') ||
      displayUrl.includes('X-Amz-Credential') ||
      // 阿里云 OSS
      displayUrl.includes('OSSAccessKeyId') ||
      displayUrl.includes('Signature') ||
      // 腾讯云 COS
      displayUrl.includes('q-sign-algorithm') ||
      displayUrl.includes('q-signature') ||
      // 七牛云
      (displayUrl.includes('e=') && displayUrl.includes('token=')) ||
      // 华为云 OBS
      displayUrl.includes('AWSAccessKeyId') ||
      (displayUrl.includes('Expires') && displayUrl.includes('Signature'))

    if (isPresignedUrl) {
      // 预签名URL不添加缓存参数，避免影响签名
      return { ...item, url: displayUrl }
    } else {
      // 普通URL添加时间戳参数解决浏览器缓存问题
      const separator = displayUrl.includes('?') ? '&' : '?'
      return {
        ...item,
        url: `${displayUrl}${separator}t=${Date.now()}`
      }
    }
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
      console.warn(t('formUpload.mixedFormatWarning'))
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
 * 从外部值初始化图片列表
 */
const initFileList = async () => {
  const val = props.modelValue

  // 如果值为空，清空图片列表
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

      // 检查当前图片列表与模型值是否匹配
      const currentOssIds = fileList.value.filter((item) => item.ossId).map((item) => String(item.ossId))

      // 只有当两者不匹配时才更新图片列表
      if (JSON.stringify(currentOssIds.sort()) !== JSON.stringify(ossIds.sort())) {
        if (ossIds.length > 0) {
          showLoading(t('formUpload.loadingImageInfo'))
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

      // 只有当两者不匹配时才更新图片列表
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

    // 只有当两者不匹配时才更新图片列表
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
 * 检查文件类型是否有效
 * @param file 文件对象
 * @returns 是否为有效类型
 */
const isValidFileType = (file: File): boolean => {
  if (!props.fileType?.length) {
    return file.type.indexOf('image') > -1
  }

  let fileExtension = ''
  if (file.name.lastIndexOf('.') > -1) {
    fileExtension = file.name.slice(file.name.lastIndexOf('.') + 1).toLowerCase()
  }

  return props.fileType.some((type) => {
    const lowerType = type.toLowerCase()
    return file.type.indexOf(lowerType) > -1 || (fileExtension && fileExtension === lowerType)
  })
}

/**
 * 下载图片
 * @param file 要下载的图片
 */
const downloadFile = (file: ImageItem | null) => {
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
      throw new Error(err2?.message || t('formUpload.fileFailed'))
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
      throw new Error(err3?.message || t('formUpload.confirmFailed'))
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
 * 清空全部图片
 * 优化版本：分别处理有ossId和无ossId的图片
 */
const handleClearAll = async () => {
  if (fileList.value.length === 0) {
    showMsgError(t('formUpload.noImageToClear'))
    return
  }

  // 分类文件：有ossId的和没有ossId的
  const filesWithOssId = fileList.value.filter((file) => file.ossId)
  const filesWithoutOssId = fileList.value.filter((file) => !file.ossId)

  // 构建确认消息
  let confirmMessage = t('formUpload.confirmClearAll')
  if (filesWithOssId.length > 0 && filesWithoutOssId.length > 0) {
    confirmMessage = `${t('formUpload.confirmClearAll')}\n• ${t('formUpload.confirmClearCloud', { count: filesWithOssId.length })}\n• ${t('formUpload.confirmClearLocal', { count: filesWithoutOssId.length })}`
  } else if (filesWithOssId.length > 0) {
    confirmMessage = `${t('formUpload.confirmClearAll')}\n${t('formUpload.confirmClearCloud', { count: filesWithOssId.length })}`
  } else {
    confirmMessage = `${t('formUpload.confirmClearAll')}\n${t('formUpload.confirmClearLocal', { count: filesWithoutOssId.length })}`
  }

  const [confirmErr] = await showConfirm(confirmMessage)
  if (confirmErr) return

  // 如果有需要删除的云端文件，显示加载提示
  if (filesWithOssId.length > 0) {
    showLoading(t('formUpload.deletingFiles', { count: filesWithOssId.length }))
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
      showMsgError(t('formUpload.cloudDeleteFailed') + failedFileNames)

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
        successMessage += ', ' + t('formUpload.localFilesCleared', { count: filesWithoutOssId.length })
      } else {
        successMessage = t('formUpload.localFilesCleared', { count: filesWithoutOssId.length })
      }
    } else {
      // 有云端文件删除失败，只清空本地文件（无ossId的）
      fileList.value = fileList.value.filter((file) => file.ossId) // 保留有ossId的（失败的）
      if (successMessage) {
        successMessage += ', ' + t('formUpload.localFilesCleared', { count: filesWithoutOssId.length })
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
    showMsgSuccess(t('formUpload.clearCompleted') + successMessage)
  } else if (successMessage && hasError) {
    showMsgSuccess(t('formUpload.partialClearCompleted') + successMessage)
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
 * @returns 是否允许上传或处理后的文件
 */
const handleBeforeUpload = async (file: File) => {
  // 检查文件类型
  if (!isValidFileType(file)) {
    showMsgError(t('formUpload.formatError', { formats: props.fileType?.join('/') }))
    return false
  }

  // 校验文件名是否包含特殊字符
  if (file.name.includes(',')) {
    showMsgError(t('formUpload.filenameError'))
    return false
  }

  // 校验图片大小
  if (props.fileSize && file.size / 1024 / 1024 > props.fileSize) {
    showMsgError(t('formUpload.sizeError', { size: props.fileSize }))
    return false
  }

  // 增加待上传计数
  pendingUploadCount.value++

  // 只在第一个文件开始上传时显示加载提示
  if (pendingUploadCount.value === 1) {
    showLoading(t('formUpload.uploadingImage'))
  }

  // 如果启用直传模式，需要先获取预签名URL判断存储类型
  if (props.enableDirectUpload) {
    // 获取预签名URL
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
        // 如果获取预签名URL失败，降级到默认上传
        // 处理文件压缩（如果启用）
        if (props.compressSupport && file.size / 1024 > props.compressTargetSize) {
          return compressAccurately(file, props.compressTargetSize).catch((err) => {
            showMsgError(t('formUpload.compressFailed') + (err.message || err))
            // 减少待上传计数
            pendingUploadCount.value--
            // 如果没有待上传的文件，关闭加载提示
            if (pendingUploadCount.value === 0) {
              hideLoading()
            }
            return false
          })
        }
        return true
      }

      // 检查是否是本地存储
      if (presignedData.presignedUrl.includes('/resource/oss/upload')) {
        // 本地存储使用默认上传，处理压缩后返回true继续
        if (props.compressSupport && file.size / 1024 > props.compressTargetSize) {
          return compressAccurately(file, props.compressTargetSize).catch((err) => {
            showMsgError(t('formUpload.compressFailed') + (err.message || err))
            // 减少待上传计数
            pendingUploadCount.value--
            // 如果没有待上传的文件，关闭加载提示
            if (pendingUploadCount.value === 0) {
              hideLoading()
            }
            return false
          })
        }
        return true
      } else {
        // 云存储执行直传，处理压缩后传入已获取的预签名数据
        let processedFile = file
        if (props.compressSupport && file.size / 1024 > props.compressTargetSize) {
          try {
            processedFile = (await compressAccurately(file, props.compressTargetSize)) as File
          } catch (err: any) {
            showMsgError(t('formUpload.compressFailed') + (err.message || err))
            // 减少待上传计数
            pendingUploadCount.value--
            // 如果没有待上传的文件，关闭加载提示
            if (pendingUploadCount.value === 0) {
              hideLoading()
            }
            return false
          }
        }

        handleDirectUpload(processedFile, presignedData)
        return false // 阻止默认上传
      }
    } catch (error: any) {
      // 如果直传预检查失败，降级到默认上传
      // 处理文件压缩（如果启用）
      if (props.compressSupport && file.size / 1024 > props.compressTargetSize) {
        return compressAccurately(file, props.compressTargetSize).catch((err) => {
          showMsgError(t('formUpload.compressFailed') + (err.message || err))
          // 减少待上传计数
          pendingUploadCount.value--
          // 如果没有待上传的文件，关闭加载提示
          if (pendingUploadCount.value === 0) {
            hideLoading()
          }
          return false
        })
      }
      return true
    }
  }

  // 传统上传模式 - 需要压缩图片
  if (props.compressSupport && file.size / 1024 > props.compressTargetSize) {
    return compressAccurately(file, props.compressTargetSize).catch((err) => {
      showMsgError(t('formUpload.compressFailed') + (err.message || err))
      // 减少待上传计数
      pendingUploadCount.value--
      // 如果没有待上传的文件，关闭加载提示
      if (pendingUploadCount.value === 0) {
        hideLoading()
      }
      return false
    })
  }

  return true
}

/**
 * 超出图片数量限制处理
 */
const handleExceed = () => {
  showMsgError(t('formUpload.countError', { limit: props.limit }))
  emit('exceed')
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

    // 创建新图片对象
    const newFile = {
      name: res.data.originalName || res.data.fileName,
      url: cleanUrl, // 存储不带签名的干净URL
      signedUrl: signedUrl, // 存储带签名的完整URL用于显示
      ossId: res.data.ossId
    }

    // 如果是替换模式，立即替换
    if (isReplaceMode.value) {
      fileList.value = [newFile]
      updateModelValue()
      showMsgSuccess(t('formUpload.replaceSuccess'))
      emit('success', res, file, newFile)
    } else {
      // 常规上传：先存到临时列表，等所有文件上传完成后统一处理
      pendingFiles.value.push(newFile)

      // 如果所有文件都上传完成，统一处理
      if (pendingUploadCount.value === 0) {
        // 统一添加所有文件到列表
        fileList.value.push(...pendingFiles.value)

        // 清空临时列表
        pendingFiles.value = []

        // 更新绑定值
        updateModelValue()

        // 刷新组件
        await nextTick()
        await forceRefreshUpload()

        // 显示成功提示
        showMsgSuccess(props.enableDirectUpload ? t('formUpload.directUploadSuccess') : t('formUpload.uploadSuccess'))
      }

      emit('success', res, file, newFile)
    }
  } else {
    showMsgError(res.msg || t('formUpload.uploadFailed'))
    imageUploadRef.value?.handleRemove(file)
    emit('error', res.msg || t('formUpload.uploadFailed'), file)
  }

  // 如果没有待上传的文件，关闭加载提示
  if (pendingUploadCount.value === 0) {
    hideLoading()
  }
}

/**
 * 删除图片
 * @param file 要删除的图片
 * @returns 是否允许删除
 */
const handleBeforeDelete = async (file: ImageItem): Promise<boolean> => {
  // 由于displayFileList使用的是带签名的URL,需要找到原始URL对应的文件
  let fileItem: ImageItem | undefined

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
    const [confirmErr] = await showConfirm(t('formUpload.confirmDeleteCloud'))
    // 用户取消删除操作
    if (confirmErr) return false

    // 调用删除接口
    await deleteOss(fileItem.ossId)
  }

  // 从图片列表中移除该图片（使用找到的fileItem或按原始逻辑）
  if (fileItem) {
    fileList.value = fileList.value.filter((item) => item !== fileItem)
  } else {
    // 如果还是没找到，尝试用文件名删除
    fileList.value = fileList.value.filter((item) => item.name !== file.name)
  }

  updateModelValue()

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

  const errorMsg = err.message || err
  showMsgError(t('formUpload.directUploadFailed') + errorMsg)
  emit('error', errorMsg, file)

  // 如果所有文件都处理完成（成功或失败），需要统一处理待添加的文件
  if (pendingUploadCount.value === 0 && pendingFiles.value.length > 0) {
    // 统一添加成功的文件到列表
    fileList.value.push(...pendingFiles.value)

    // 清空临时列表
    pendingFiles.value = []

    // 更新绑定值并刷新组件
    updateModelValue()
    forceRefreshUpload()
  }

  // 如果没有待上传的文件，关闭加载提示
  if (pendingUploadCount.value === 0) {
    hideLoading()
  }
}

/**
 * 图片预览
 * @param file 预览的图片
 */
const handleImagePreview = (file: ImageItem) => {
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
 * 处理从素材库选择的图片
 * @param selected 选中的图片或图片数组
 */
const handleMediaSelect = async (selected: any) => {
  // 保存一份当前的图片列表
  const currentFileList = [...fileList.value]

  // 检查是否还有剩余空间
  const remainingSpace = props.limit - currentFileList.length
  if (remainingSpace <= 0) {
    showMsgError(t('formUpload.countError', { limit: props.limit }))
    return
  }

  // 创建一个新的图片列表，用于存储最终结果
  const newFileList = [...currentFileList]

  if (Array.isArray(selected)) {
    // 多选模式
    // 只添加限制数量内的图片，并过滤非图片文件
    const imagesToAdd = selected
      .filter((file) => {
        const suffix = file.fileSuffix?.toLowerCase() || ''
        return ['.jpg', '.jpeg', '.png', '.gif', '.webp', '.bmp', '.svg'].includes(suffix)
      })
      .slice(0, remainingSpace)

    // 添加新选择的图片，同时检查是否重复
    for (const file of imagesToAdd) {
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
    // 单选模式 - 检查是否为图片文件
    const suffix = selected.fileSuffix?.toLowerCase() || ''
    const isImage = ['.jpg', '.jpeg', '.png', '.gif', '.webp', '.bmp', '.svg'].includes(suffix)

    if (!isImage) {
      showMsgError(t('formUpload.selectImageFile'))
      return
    }

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

  // 处理完成后，我们需要重置文件列表并使用新的列表
  // 这是为了解决当从素材库选择后不显示图片的问题
  fileList.value = []

  // 在下一个事件循环中更新，确保视图已经刷新
  await nextTick()
  // 更新为新的文件列表
  fileList.value = newFileList
  // 更新绑定值
  updateModelValue()
  // 强制刷新上传组件
  await forceRefreshUpload()
  showMsgSuccess(t('formUpload.selectSuccess'))
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
/* 当图片数达到上限时隐藏上传按钮 */
:deep(.hide .el-upload--picture-card) {
  display: none;
}

/* 优化上传列表样式 */
:deep(.el-upload-list) {
  margin-top: 10px;
}

/* 拖拽上传样式优化 */
:deep(.el-upload-dragger) {
  padding: 40px;
}

/* 进度条样式 */
:deep(.el-progress) {
  margin-top: 10px;
}

/* 图片卡片样式优化 - 使用动态尺寸 */
:deep(.el-upload--picture-card) {
  width: v-bind('uploadCardSize + "px"');
  height: v-bind('uploadCardSize + "px"');
  line-height: v-bind('(uploadCardSize - 2) + "px"');
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: v-bind('uploadCardSize + "px"');
  height: v-bind('uploadCardSize + "px"');
}
</style>
