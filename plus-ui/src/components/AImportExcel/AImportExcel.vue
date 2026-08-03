<!-- Excel导入 -->
<template>
  <!--
   Excel导入组件
     1.提供importUrl和templateUrl，显示上传功能和下载模板链接
     2.只提供importUrl，显示上传功能但不显示下载模板链接
     3.只提供templateUrl，点击按钮直接下载模板
     <el-col :span="1.5" v-permi="['system:user:import']">
       <AImportExcel
         v-slot="{ openImportExcel }"
         title="用户数据"
         importUrl="/system/user/importUsers"
         templateUrl="/system/user/templateUsers"
         @import-success="getList">
         <el-button type="info" plain icon="Top" @click="openImportExcel">
           {{ t('导入') }}
         </el-button>
       </AImportExcel>
     </el-col>
-->
  <!-- 使用 div 作为根元素包裹，解决指令无法应用到非元素根节点的问题 -->
  <div class="contents">
    <slot :open-import-excel="openImportExcel"></slot>
    <!-- excel导入对话框 - 只在有importUrl时显示 -->
    <AModal
      v-if="props.importUrl"
      v-model="upload.open"
      :title="upload.title"
      mode="dialog"
      size="small"
      :loading="upload.isUploading"
      @confirm="submitFileForm"
      @cancel="closeDialog"
    >
      <el-upload
        ref="uploadRef"
        :limit="1"
        accept=".xlsx, .xls"
        :headers="upload.headers"
        :action="computedUploadUrl"
        :data="props.importData || {}"
        :disabled="upload.isUploading"
        :on-progress="handleFileUploadProgress"
        :on-success="handleFileSuccess"
        :on-change="handleFileChange"
        :on-remove="handleFileRemove"
        :auto-upload="false"
        drag
      >
        <el-icon class="el-icon--upload">
          <i-ep-upload-filled />
        </el-icon>
        <div class="el-upload__text">{{ t('importExcel.dragHint') }}<em>{{ t('importExcel.clickToUpload') }}</em></div>
        <template #tip>
          <div class="text-center">
            <div class="flex items-center justify-center mb-3 px-3 py-2 bg-blue-50 border border-blue-200 rounded text-blue-600 text-sm">
              <el-icon class="mr-2">
                <i-ep-info-filled />
              </el-icon>
              <span>{{ t('importExcel.autoUpdateHint') }}</span>
            </div>
            <div class="text-xs text-gray-500">
              <div>{{ t('importExcel.formatHint') }}</div>
              <el-link v-if="props.templateUrl" type="primary" :underline="false" class="mt-2 text-xs" @click="downloadTemplate"> {{ t('importExcel.downloadTemplate') }} </el-link>
            </div>
          </div>
        </template>
      </el-upload>
    </AModal>
  </div>
</template>

<script setup lang="ts" name="AImportExcel">
import { objectToQuery } from '@/utils/object'
import { SystemConfig } from '@/systemConfig'
import { getCurrentDateTime } from '@/utils/date'
import { showMsgWarning, showAlert } from '@/utils/modal'

const { t } = useI18n()

/**
 * Excel导入组件的Props接口定义
 * @description 定义了Excel导入组件的属性
 */
interface AExcelImportProps {
  /**
   * 导入标题
   * @required
   */
  title: string

  /**
   * 导入数据的完整URL路径
   * @optional 如果不提供，则不显示上传功能
   */
  importUrl?: string

  /**
   * 导入模板的完整URL路径
   * @optional 如果不提供，则不显示下载模板链接
   */
  templateUrl?: string

  /**
   * 导入URL参数，将拼接到URL后面
   */
  importParams?: Record<string, any>

  /**
   * 导入请求体参数，作为form-data上传
   */
  importData?: Record<string, any>
}

// 定义 props，使用 TS 接口和类型约束
const props = defineProps<AExcelImportProps>()

// 定义 emit 事件
const emit = defineEmits(['importSuccess', 'importError'])

// 上传组件引用
const uploadRef = ref<ElUploadInstance>()

// 本地文件状态管理
const fileList = ref<ElUploadFile[]>([])

interface ImportExcelOption {
  /** 是否显示导入弹出层 */
  open: boolean
  /** 导入弹出层标题 */
  title: string
  /** 是否禁用上传按钮（通常在上传进行中时设置为true） */
  isUploading: boolean
  /** 设置上传的请求头部 */
  headers: { [key: string]: any }
  /** 上传的服务端地址 */
  url: string

  /** 其他扩展参数 */
  [key: string]: any
}

/*** 用户导入参数 */
const upload = ref<ImportExcelOption>({
  // 是否显示弹出层（用户导入）
  open: false,
  // 弹出层标题（用户导入）
  title: '',
  // 是否禁用上传
  isUploading: false,
  // 设置上传的请求头部
  headers: useToken().getAuthHeaders(),
  // 上传的地址，使用完整URL
  url: props.importUrl ? SystemConfig.api.baseUrl + props.importUrl : ''
})

/**
 * 文件变化处理
 * @param file 当前变化的文件
 * @param files 当前所有文件列表
 */
const handleFileChange = (file: ElUploadFile, files: ElUploadFile[]) => {
  console.log('文件变化:', { file, files })

  // 更新本地文件状态
  fileList.value = [...files]

  // 验证文件类型
  if (file.raw) {
    const isExcel =
      file.raw.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' ||
      file.raw.type === 'application/vnd.ms-excel' ||
      file.name.endsWith('.xlsx') ||
      file.name.endsWith('.xls')

    if (!isExcel) {
      showMsgWarning(t('importExcel.excelOnlyWarning'))
      // 移除非Excel文件
      uploadRef.value?.handleRemove(file)
      return false
    }

    console.log('文件验证通过:', file.name)
  }
}

/**
 * 文件移除处理
 * @param file 被移除的文件
 * @param files 移除后的文件列表
 */
const handleFileRemove = (file: ElUploadFile, files: ElUploadFile[]) => {
  console.log('文件移除:', { file, files })
  fileList.value = [...files]
}

/**
 * 打开导入数据弹窗或直接下载模板
 * @description 根据提供的参数执行不同的操作
 */
const openImportExcel = () => {
  // 如果既没有importUrl也没有templateUrl，不执行任何操作
  if (!props.importUrl && !props.templateUrl) {
    showMsgWarning(t('importExcel.noUrlWarning'))
    return
  }

  // 如果只提供了templateUrl，直接下载模板
  if (!props.importUrl && props.templateUrl) {
    downloadTemplate()
    return
  }

  // 如果提供了importUrl，打开导入对话框
  upload.value.title = props.title + t('importExcel.importSuffix')
  upload.value.open = true

  // 重置文件状态
  resetUploadState()
}

/**
 * 重置上传状态
 */
const resetUploadState = () => {
  fileList.value = []
  uploadRef.value?.clearFiles()
}

/**
 * 关闭对话框
 */
const closeDialog = () => {
  if (upload.value.isUploading) return
  upload.value.open = false
  resetUploadState()
}

/**
 * 文件上传中处理
 * @description 设置上传状态为上传中
 */
const handleFileUploadProgress = () => {
  upload.value.isUploading = true
}

/**
 * 文件上传成功处理
 * @param response 上传响应
 * @param file 上传文件
 * @description 关闭上传弹窗，显示导入结果，触发导入成功事件
 */
const handleFileSuccess = (response: any, file: ElUploadFile) => {
  upload.value.open = false
  upload.value.isUploading = false

  // 清理文件
  uploadRef.value?.handleRemove(file)
  resetUploadState()

  // 展示导入结果
  showAlert(
    `<div style='overflow: auto;overflow-x: hidden;max-height: 70vh;padding: 10px 20px 0;'>
      ${response.msg}
    </div>`,
    t('importExcel.importResult'),
    { dangerouslyUseHTMLString: true }
  )

  // 触发导入成功事件
  emit('importSuccess')
}

/**
 * 下载导入模板
 * @description 通过代理下载指定的导入模板，仅在提供templateUrl时可用
 */
const downloadTemplate = () => {
  if (!props.templateUrl) return

  useDownload().download(`${props.title}${t('importExcel.templateSuffix')}_${getCurrentDateTime()}.xlsx`, props.templateUrl)
}

/**
 * 提交上传文件 - 使用本地状态检查
 * @description 检查本地文件状态后提交上传
 */
const submitFileForm = () => {
  console.log('准备提交文件，当前文件列表:', fileList.value)

  // 检查是否有选择文件
  if (fileList.value.length === 0) {
    showMsgWarning(t('importExcel.selectFileFirst'))
    return
  }

  // 检查文件状态
  const readyFiles = fileList.value.filter((file) => file.status === 'ready' || file.status === undefined)

  if (readyFiles.length === 0) {
    showMsgWarning(t('importExcel.noFileAvailable'))
    return
  }

  uploadRef.value?.submit()
}

/**计算导入接口地址*/
const computedUploadUrl = computed(() => {
  const params = props.importParams || {}
  return Object.keys(params).length > 0 ? upload.value.url + '?' + objectToQuery(params) : upload.value.url
})
</script>

<style scoped lang="scss"></style>
