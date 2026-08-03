<!-- 文件管理 -->
<template>
  <div>
    <!-- 使用可拖拽面板组件 -->
    <AResizablePanels v-model:leftWidth="leftPanelWidth" :min-width="180" :max-width="400" :gutter="20">
      <!-- 左侧面板：文件夹目录树（虚拟滚动 + 搜索防抖，解决大数据量卡死） -->
      <template #left>
        <ATreeFilter
          ref="directoryTreeRef"
          :data="directories"
          :height="directoryTreeHeight"
          :expanded-keys="expandedKeys"
          :placeholder="t('Enter folder name', '请输入文件夹名称')"
          @node-expand="handleNodeExpand"
          @node-collapse="handleNodeCollapse"
          @node-click="handleDirectorySelect"
        >
          <!-- 表头：文件夹标题 + 新建按钮 -->
          <template #header>
            <div class="flex justify-between items-center">
              <span class="font-bold">{{ t('Folder', '文件夹') }}</span>
              <el-button v-permi="['system:ossDirectory:add']" type="primary" link @click="handleAddDirectory">
                <el-icon>
                  <Plus />
                </el-icon>
                {{ t('New', '新建') }}
              </el-button>
            </div>
          </template>
          <!-- 自定义节点：文件夹图标 + 悬浮显示的增/改/删操作按钮 -->
          <template #default="{ node, data }">
            <div class="w-full flex justify-between items-center group">
              <span>
                <el-icon class="mr-1"><Folder /></el-icon>
                {{ node.label }}
              </span>
              <span v-if="data.id !== ALL && data.id !== UNCATEGORIZED" class="opacity-0 transition-opacity duration-300 group-hover:opacity-100">
                <el-button v-permi="['system:ossDirectory:add']" type="primary" link @click.stop="handleAddChildDirectory(data)">
                  <el-icon><Plus /></el-icon>
                </el-button>
                <el-button v-permi="['system:ossDirectory:update']" type="primary" link @click.stop="handleRenameDirectory(data)">
                  <el-icon><Edit /></el-icon>
                </el-button>
                <el-button v-permi="['system:ossDirectory:delete']" type="primary" link @click.stop="handleDeleteDirectory(data)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </span>
            </div>
          </template>
        </ATreeFilter>
      </template>

      <!-- 右侧面板：文件列表区域 -->
      <template #right>
        <!-- 搜索表单 -->
        <ASearchForm ref="queryFormRef" v-model="queryParams" :visible="showSearch">
          <AFormInput label="模糊搜索" prop="searchValue" v-model="queryParams.searchValue" @input="handleQuery"></AFormInput>
          <AFormInput label="文件后缀" v-model="queryParams.fileSuffix" prop="fileSuffix" @input="handleQuery"></AFormInput>
          <AFormDate v-model="dateRangeCreateTime" prop="createTime" type="daterange" label="创建时间" @change="handleQuery"></AFormDate>
        </ASearchForm>

        <!-- 文件列表 -->
        <el-card shadow="hover">
          <template #header>
            <el-row :gutter="10" class="mb-2">
              <el-col :span="1.5">
                <el-button v-permi="['system:oss:upload']" type="primary" plain icon="Upload" @click="handleFile">{{
                  t('Upload File', '上传文件')
                }}</el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button v-permi="['system:oss:upload']" type="primary" plain icon="Upload" @click="handleImage">{{
                  t('Upload Image', '上传图片')
                }}</el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button v-permi="['system:oss:upload']" type="primary" plain icon="Upload" @click="handleMedia">{{
                  t('Upload Media', '上传音视频')
                }}</el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button
                  v-permi="['system:oss:delete']"
                  type="danger"
                  plain
                  icon="Delete"
                  :disabled="selectionItems.length === 0"
                  @click="handleDelete()"
                >
                  {{ t('删除') }}
                </el-button>
              </el-col>
              <el-col :span="1.5" v-if="selectionItems.length > 0">
                <el-button v-permi="['system:ossDirectory:update']" type="primary" plain icon="Position" @click="handleMoveFiles">{{
                  t('Move To', '移动到')
                }}</el-button>
              </el-col>
              <el-col :span="1.5" v-if="selectionItems.length === 1">
                <el-button v-permi="['system:oss:upload']" type="warning" plain icon="RefreshRight" @click="handleReplaceFile">{{
                  t('Replace', '替换')
                }}</el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button
                  v-permi="['system:oss:update']"
                  :type="previewListResource ? 'danger' : 'warning'"
                  plain
                  @click="handlePreviewListResource(!previewListResource)"
                  >{{ t('Preview Toggle', '预览开关') }} : {{ previewListResource ? t('Disable', '禁用') : t('Enable', '启用') }}
                </el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button v-permi="['system:ossConfig:view']" type="info" plain icon="Operation" @click="handleOssConfig">{{
                  t('Config Management', '配置管理')
                }}</el-button>
              </el-col>

              <TableToolbar v-model:showSearch="showSearch" @reset-query="resetQuery" @query-table="getList"></TableToolbar>
            </el-row>

            <!-- 显示当前路径 -->
            <div class="flex items-center mt-2">
              <span class="text-gray-500 mr-2">{{ t('Current Path', '当前路径') }}:</span>
              <span class="font-bold">{{ currentPath }}</span>
            </div>
          </template>

          <!-- 文件表格 -->
          <el-table
            ref="ossTableRef"
            v-loading="isLoading"
            :data="ossList"
            :height="tableHeight"
            stripe
            @selection-change="handleSelectionChange"
            @sort-change="handleSortChange"
          >
            <!-- 表格列配置保持原样... -->
            <el-table-column type="selection" align="center" />
            <el-table-column label="OSS ID" prop="ossId" align="center" v-if="false" />
            <el-table-column :label="t('File Name', '文件名')" prop="fileName" align="center" show-overflow-tooltip>
              <template #default="{ row }">
                <div class="flex items-center">
                  <el-icon v-if="isImageFile(row.fileSuffix)" class="mr-2">
                    <Picture />
                  </el-icon>
                  <el-icon v-else-if="isDocumentFile(row.fileSuffix)" class="mr-2">
                    <Document />
                  </el-icon>
                  <el-icon v-else-if="isVideoFile(row.fileSuffix)" class="mr-2">
                    <VideoPlay />
                  </el-icon>
                  <el-icon v-else-if="isAudioFile(row.fileSuffix)" class="mr-2">
                    <Headset />
                  </el-icon>
                  <el-icon v-else class="mr-2">
                    <Files />
                  </el-icon>
                  <span>{{ row.fileName }}</span>
                </div>
              </template>
            </el-table-column>
            <!-- 其他列保持不变... -->
            <el-table-column :label="t('Original Name', '原名')" prop="originalName" align="center" show-overflow-tooltip />
            <el-table-column :label="t('File Suffix', '文件后缀')" prop="fileSuffix" align="center" />
            <el-table-column :label="t('File Preview', '文件预览')" align="center">
              <template #default="{ row }">
                <ImagePreview v-if="previewListResource && isImageFile(row.fileSuffix)" :src="addCacheBuster(row.url, row.updateTime)" />
                <div v-else-if="isVideoFile(row.fileSuffix)" class="text-center">
                  <el-icon style="font-size: 24px">
                    <VideoPlay />
                  </el-icon>
                  <div class="mt-1">{{ t('Video File', '视频文件') }}</div>
                </div>
                <div v-else-if="isAudioFile(row.fileSuffix)" class="text-center">
                  <el-icon style="font-size: 24px">
                    <Headset />
                  </el-icon>
                  <div class="mt-1">{{ t('Audio File', '音频文件') }}</div>
                </div>
                <div v-else-if="isDocumentFile(row.fileSuffix)" class="text-center">
                  <el-icon style="font-size: 24px">
                    <Document />
                  </el-icon>
                  <div class="mt-1">{{ t('Document File', '文档文件') }}</div>
                </div>
                <div v-else class="text-center">
                  <el-icon style="font-size: 24px">
                    <Files />
                  </el-icon>
                  <div class="mt-1">{{ t('File', '文件') }}</div>
                </div>
              </template>
            </el-table-column>
            <el-table-column :label="t('File Size', '文件大小')" prop="fileSize" align="center" sortable="custom">
              <template #default="{ row }">
                {{ formatFileSize(row.fileSize) }}
              </template>
            </el-table-column>
            <el-table-column :label="t('createTime', '创建时间')" prop="createTime" align="center" min-width="100" sortable="custom">
              <template #default="{ row }">
                <span> {{ formatDate(row.createTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="t('Uploader', '上传人')" prop="createByName" align="center" min-width="88" />
            <el-table-column :label="t('Service Provider', '服务商')" prop="service" align="center" />
            <el-table-column :label="t('操作')" align="center" fixed="right" width="220">
              <template #default="{ row }">
                <el-tooltip :content="t('Preview', '预览')" placement="top">
                  <el-button link type="primary" icon="View" @click="handlePreview(row)"></el-button>
                </el-tooltip>
                <el-tooltip :content="t('Copy', '复制')" placement="top">
                  <el-button link type="primary" icon="CopyDocument" @click="copy(row.url)"></el-button>
                </el-tooltip>
                <el-tooltip :content="t('Download', '下载')" placement="top">
                  <el-button v-permi="['system:oss:download']" link type="primary" icon="Download" @click="handleDownload(row)"></el-button>
                </el-tooltip>
                <el-tooltip :content="t('Move', '移动')" placement="top">
                  <el-button v-permi="['system:ossDirectory:update']" link type="primary" icon="Position" @click="handleFileMove(row)"></el-button>
                </el-tooltip>
                <el-tooltip :content="t('Replace', '替换')" placement="top">
                  <el-button v-permi="['system:oss:upload']" link type="warning" icon="RefreshRight" @click="handleFileReplace(row)"></el-button>
                </el-tooltip>
                <el-tooltip :content="t('删除')" placement="top">
                  <el-button v-permi="['system:oss:delete']" link type="danger" icon="Delete" @click="handleDelete(row)"></el-button>
                </el-tooltip>
              </template>
            </el-table-column>
          </el-table>

          <Pagination v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
        </el-card>
      </template>
    </AResizablePanels>

    <!-- 添加或修改OSS对象存储对话框 -->
    <AModal
      v-model="dialog.visible"
      :title="dialog.title"
      size="medium"
      mode="dialog"
      footer-type="close-only"
      :loading="buttonLoading"
      @cancel="cancel"
    >
      <el-form ref="ossFormRef" :model="form" :rules="rules" label-width="auto">
        <AFormFileUpload
          :limit="isReplaceMode ? 1 : 99999"
          label="文件"
          v-model="form.file"
          drag
          :enable-oss-media-manager="false"
          v-if="type === 0"
          :upload-params="uploadParams"
          @success="handleUploadSuccess"
        />
        <AFormImgUpload
          :limit="isReplaceMode ? 1 : 99999"
          label="图片"
          v-model="form.file"
          :enable-oss-media-manager="false"
          v-if="type === 1"
          :upload-params="uploadParams"
          @success="handleUploadSuccess"
        />
        <AFormFileUpload
          :limit="isReplaceMode ? 1 : 99999"
          label="音视频"
          :file-type="['mp3', 'wav', 'mp4', 'flv']"
          v-model="form.file"
          drag
          :enable-oss-media-manager="false"
          v-if="type === 2"
          :upload-params="uploadParams"
          @success="handleUploadSuccess"
        />
      </el-form>
    </AModal>

    <!-- 目录操作对话框 -->
    <AModal
      v-model="directoryDialogVisible"
      :title="directoryDialog.title"
      size="small"
      mode="dialog"
      :loading="directoryDialog.loading"
      @confirm="submitDirectoryForm"
      @cancel="directoryDialogVisible = false"
    >
      <el-form ref="directoryFormRef" :model="directoryForm" :rules="directoryRules" label-width="auto">
        <el-form-item :label="t('Directory Name', '目录名称')" prop="directoryName">
          <el-input v-model="directoryForm.directoryName" :placeholder="t('Enter directory name', '请输入目录名称')" />
        </el-form-item>
      </el-form>
    </AModal>

    <!-- 移动文件对话框 -->
    <AModal
      v-model="moveDialogVisible"
      :title="t('Move to Folder', '移动到文件夹')"
      size="small"
      mode="dialog"
      @confirm="confirmMove"
      @cancel="moveDialogVisible = false"
    >
      <el-tree
        ref="moveTreeRef"
        :data="directories"
        node-key="id"
        :props="{ label: 'label', children: 'children' }"
        highlight-current
        default-expand-all
        :expand-on-click-node="false"
      />
    </AModal>

    <!-- 文件预览对话框 -->
    <AModal
      v-model="previewDialogVisible"
      :title="t('File Preview', '文件预览')"
      size="large"
      mode="dialog"
      :show-footer="false"
      :mask-closable="true"
      show-fullscreen-toggle
      @fullscreen-change="handleFullscreenChange"
    >
      <AFilePreview
        v-if="previewFile"
        :url="addCacheBuster(previewFile.url, previewFile.updateTime)"
        :file-name="previewFile.originalName || previewFile.fileName"
        :file-suffix="previewFile.fileSuffix"
        :file-size="previewFile.fileSize"
        :is-fullscreen="isPreviewFullscreen"
        @download="downloadFile(previewFile)"
      />
    </AModal>

    <!-- OSS配置管理对话框 -->
    <OssConfig v-model="ossConfigDialog.visible" @success="handleOssConfigSuccess" />
  </div>
</template>

<script setup lang="ts" name="Oss">
import OssConfig from './OssConfig.vue'
import { pageOss, deleteOss } from '@/api/system/oss/oss/ossApi'
import {
  addOssDirectory,
  updateOssDirectory,
  deleteOssDirectorys,
  getOssDirectoryTreeOptions,
  moveOssDirectory
} from '@/api/system/oss/ossDirectory/ossDirectoryApi'
import { getByConfigKey, updateConfigByKey } from '@/api/system/config/config/configApi'
import type { SysOssDirectoryBo, SysOssDirectoryTreeVo } from '@/api/system/oss/ossDirectory/ossDirectoryTypes'
import type { SysOssQuery, SysOssBo, SysOssVo } from '@/api/system/oss/oss/ossTypes'
import { formatFileSize } from '@/utils/format'
import { showConfirm, showMsgSuccess, showMsgWarning, showMsgError, showLoading, hideLoading } from '@/utils/modal'
import { formatDate } from '@/utils/date'
import { copy, addCacheBuster } from '@/utils/function'
import { addDateRange } from '@/utils/date'
import { toValidate } from '@/utils/to'

const { t } = useI18n()

// 使用表格高度处理钩子
const { tableHeight, queryFormRef, showSearch } = useTableHeight(20)

/**左侧面板宽度*/
const leftPanelWidth = ref(230)

// =========== 查询相关 ===========

/**查询参数对象*/
const queryParams = ref<SysOssQuery>({
  pageNum: 1,
  pageSize: 10,
  fileName: '',
  originalName: '',
  fileSuffix: '',
  service: '',
  directoryId: undefined,
  orderByColumn: 'updateTime',
  isAsc: 'desc'
})

const dateRangeCreateTime = ref<[ElDateModelType, ElDateModelType]>(['', ''])

/** 文件搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1
  getList()
}

/** 文件重置按钮操作 */
const resetQuery = () => {
  dateRangeCreateTime.value = ['', '']
  queryFormRef.value?.resetFields()
  handleQuery()
}

// =========== 文件表格数据相关 ===========
/**表格加载状态*/
const isLoading = ref(true)
/**数据列表*/
const ossList = ref<SysOssVo[]>([])
/**总记录数*/
const total = ref(0)
/**表格实例*/
const ossTableRef = ref()
/**选中的数据项*/
const selectionItems = ref<SysOssVo[]>([])

/** 表格多选事件处理 */
const handleSelectionChange = (selection: SysOssVo[]) => {
  selectionItems.value = selection
}

// =========== 状态定义 ===========
const ALL = ref('9999999999999999')
const UNCATEGORIZED = ref('10000000000000000')

// 目录相关
const directories = ref<SysOssDirectoryTreeVo[]>([])
const currentDirectory = ref<SysOssDirectoryTreeVo | null>(null)
const expandedKeys = ref<(string | number)[]>([]) // 存储展开的节点

/** 目录树虚拟滚动高度（px）：跟随搜索栏与表格高度联动，供 el-tree-v2 虚拟滚动使用 */
const directoryTreeHeight = computed(() => (queryFormRef.value?.$el?.offsetHeight || 0) + tableHeight.value + 82)

// 其他状态
const buttonLoading = ref(false)
const previewListResource = ref(true)

// 上传类型
const type = ref(0)

// 预览相关
const previewDialogVisible = ref(false)
const previewFile = ref<SysOssVo | null>(null)
const isPreviewFullscreen = ref(false)

// 文件上传/替换相关
const fileToReplace = ref<SysOssVo | null>(null)
const isReplaceMode = ref(false)

// OSS配置对话框相关
const ossConfigDialog = ref<{ visible: boolean }>({
  visible: false
})

// DOM 引用
const directoryTreeRef = ref()
const moveTreeRef = ref<ElTreeInstance>()
const ossFormRef = ref<ElFormInstance>()
const directoryFormRef = ref<ElFormInstance>()

// 上传参数 - 修复替换模式的参数传递
const uploadParams = computed(() => {
  const params: any = {}

  if (isReplaceMode.value && fileToReplace.value) {
    // 替换模式：不需要传递 directoryId，替换操作在 upload URL 中处理
    // 这里可以传递一些替换相关的标识参数
    params.isReplace = true
    params.replaceOssId = fileToReplace.value.ossId
  } else {
    // 普通上传模式，添加目录ID
    if (currentDirectory.value && currentDirectory.value.id !== UNCATEGORIZED.value && currentDirectory.value.id !== ALL.value) {
      params.directoryId = currentDirectory.value.id
    }
  }

  return params
})

// 对话框配置
const dialog = ref<DialogState>({
  visible: false,
  title: ''
})

// 表单数据
const initFormData: SysOssBo = {
  file: undefined
}
const form = ref<SysOssBo>({ ...initFormData })

// 表单验证规则
const rules = computed<ElFormRules>(() => ({
  file: [{ required: true, message: t('File required', '文件不能为空'), trigger: 'blur' }]
}))

// 目录表单
const directoryForm = ref<SysOssDirectoryBo>({
  directoryId: undefined,
  parentId: undefined,
  directoryName: ''
})

// 目录表单验证规则
const directoryRules = computed<ElFormRules>(() => ({
  directoryName: [
    { required: true, message: t('Directory name required', '请输入目录名称'), trigger: 'blur' },
    { min: 1, max: 50, message: t('Length 1 to 50 characters', '长度在 1 到 50 个字符'), trigger: 'blur' },
    {
      // eslint-disable-next-line @typescript-eslint/no-unsafe-function-type
      validator: (rule: any, value: string, callback: Function) => {
        if (value && /[\\/:*?"<>|]/.test(value)) {
          callback(new Error(t('Directory name cannot contain special characters', '目录名不能包含特殊字符 \\ / : * ? " < > |')))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}))

// 目录对话框配置
const directoryDialog = ref({
  title: '',
  type: '', // add, edit
  loading: false
})

// 目录对话框可见性
const directoryDialogVisible = ref(false)

// 移动文件相关
const moveDialogVisible = ref(false)
const filesToMove = ref<SysOssVo[]>([])

// 当前路径计算属性
const currentPath = computed(() => {
  if (!currentDirectory.value) return '/'
  return currentDirectory.value.directoryPath || '/'
})

// =========== 文件类型相关方法 ===========

/**
 * 判断是否为图片文件
 * @param fileSuffix 文件后缀
 * @returns 是否为图片类型
 */
const isImageFile = (fileSuffix: string): boolean => {
  if (!fileSuffix) return false
  const suffix = fileSuffix.toLowerCase()
  return ['.png', '.jpg', '.jpeg', '.gif', '.webp', '.bmp', '.svg'].includes(suffix)
}

/**
 * 判断是否为文档文件
 * @param fileSuffix 文件后缀
 * @returns 是否为文档类型
 */
const isDocumentFile = (fileSuffix: string): boolean => {
  if (!fileSuffix) return false
  const suffix = fileSuffix.toLowerCase()
  return ['.doc', '.docx', '.xls', '.xlsx', '.ppt', '.pptx', '.pdf', '.txt'].includes(suffix)
}

/**
 * 判断是否为视频文件
 * @param fileSuffix 文件后缀
 * @returns 是否为视频类型
 */
const isVideoFile = (fileSuffix: string): boolean => {
  if (!fileSuffix) return false
  const suffix = fileSuffix.toLowerCase()
  return ['.mp4', '.avi', '.mov', '.wmv', '.flv', '.mkv'].includes(suffix)
}

/**
 * 判断是否为音频文件
 * @param fileSuffix 文件后缀
 * @returns 是否为音频类型
 */
const isAudioFile = (fileSuffix: string): boolean => {
  if (!fileSuffix) return false
  const suffix = fileSuffix.toLowerCase()
  return ['.mp3', '.wav', '.ogg', '.flac', '.aac'].includes(suffix)
}

/**
 * 获取媒体文件MIME类型
 * @param fileSuffix 文件后缀
 * @returns 对应的MIME类型
 */
const getMediaType = (fileSuffix: string): string => {
  if (!fileSuffix) return ''

  const suffix = fileSuffix.toLowerCase()

  // 视频类型
  if (['.mp4'].includes(suffix)) return 'video/mp4'
  if (['.webm'].includes(suffix)) return 'video/webm'
  if (['.avi'].includes(suffix)) return 'video/x-msvideo'
  if (['.mov'].includes(suffix)) return 'video/quicktime'
  if (['.wmv'].includes(suffix)) return 'video/x-ms-wmv'

  // 音频类型
  if (['.mp3'].includes(suffix)) return 'audio/mpeg'
  if (['.wav'].includes(suffix)) return 'audio/wav'
  if (['.ogg'].includes(suffix)) return 'audio/ogg'
  if (['.flac'].includes(suffix)) return 'audio/flac'
  if (['.aac'].includes(suffix)) return 'audio/aac'

  // 默认返回
  return ''
}

/**
 * 根据ID查找目录
 * @param dirs 目录数组
 * @param id 目录ID
 * @returns 找到的目录对象或null
 */
const findDirectoryById = (dirs: SysOssDirectoryTreeVo[], id: string | number): SysOssDirectoryTreeVo | null => {
  for (const dir of dirs) {
    if (dir.id == id) {
      return dir
    }
    if (dir.children && dir.children.length) {
      const found = findDirectoryById(dir.children, id)
      if (found) return found
    }
  }
  return null
}

/**
 * 下载文件
 * @param file 要下载的文件
 */
const downloadFile = (file: SysOssVo) => {
  // 创建一个链接并模拟点击下载
  const a = document.createElement('a')
  a.href = file.url
  a.download = file.fileName
  a.target = '_blank'
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
}

// =========== 方法定义 ===========

/**
 * 节点展开处理方法
 * @param data 展开的节点数据
 */
const handleNodeExpand = (data: any) => {
  if (!expandedKeys.value.includes(data.id)) {
    expandedKeys.value.push(data.id)
  }
}

/**
 * 节点折叠处理方法
 * @param data 折叠的节点数据
 */
const handleNodeCollapse = (data: any) => {
  const index = expandedKeys.value.indexOf(data.id)
  if (index !== -1) {
    expandedKeys.value.splice(index, 1)
  }
}

/**
 * 查询OSS对象存储列表
 */
const getList = async () => {
  isLoading.value = true

  // 查询预览配置
  const [configErr, configData] = await getByConfigKey('system.oss.preview-enabled')
  if (!configErr) {
    previewListResource.value = configData === null ? true : configData === 'true'
  }

  // 处理查询参数
  queryParams.value.params = {}
  addDateRange(queryParams.value, dateRangeCreateTime.value, 'createTime')

  // 查询文件列表
  const [ossErr, ossData] = await pageOss(queryParams.value)
  if (!ossErr) {
    ossList.value = ossData.records
    total.value = ossData.total
  } else if (ossErr) {
    showMsgError(t('Failed to load file list', '加载文件列表失败'))
  }

  isLoading.value = false
}

/**
 * 处理表格排序变化
 * @param {Object} sort - 排序信息对象 { column, prop, order }
 */
const handleSortChange = ({ column, prop, order }) => {
  // 重置分页到第一页
  queryParams.value.pageNum = 1

  // 设置排序参数
  if (order) {
    queryParams.value.orderByColumn = prop
    queryParams.value.isAsc = order
  } else {
    // 取消排序，恢复默认排序
    queryParams.value.orderByColumn = 'updateTime'
    queryParams.value.isAsc = 'desc'
  }

  // 重新查询数据
  getList()
}

/**
 * 加载目录树
 */
const loadDirectories = async () => {
  isLoading.value = true

  // 加载目录结构
  const [dirErr, data] = await getOssDirectoryTreeOptions()
  if (!dirErr) {
    directories.value = data || []

    // 如果没有当前选中目录，则默认选择"全部"目录（ID为'1000100010001000'）
    if (!currentDirectory.value) {
      const allDir = findDirectoryById(directories.value, ALL.value)
      if (allDir) {
        currentDirectory.value = allDir
        await nextTick()
        directoryTreeRef.value?.setCurrentKey(ALL.value)
      }
    } else {
      // 有当前选中目录，恢复选中状态
      await nextTick()
      directoryTreeRef.value?.setCurrentKey(ALL.value)
    }
  } else if (dirErr) {
    showMsgError(t('Failed to load directories', '加载目录失败'))
  }

  isLoading.value = false
}

/**
 * 加载完整数据
 */
const loadData = async () => {
  await loadDirectories()
  await getList()
}

/**
 * 目录树节点点击
 * @param data 选中的目录数据
 */
const handleDirectorySelect = (data: SysOssDirectoryTreeVo) => {
  // 如果选择的是同一个目录，不需要重新加载
  if (currentDirectory.value && currentDirectory.value.id === data.id) {
    return
  }

  currentDirectory.value = data
  queryParams.value.directoryId = data.id

  // 切换目录时清空选择和重置分页
  selectionItems.value = []
  queryParams.value.pageNum = 1
  getList()
}

/** 删除文件操作 */
const handleDelete = async (row?: SysOssVo) => {
  const idsToDelete = row ? [row.ossId] : selectionItems.value.map((item) => item.ossId)
  if (idsToDelete.length === 0) return

  const [confirmErr] = await showConfirm(t('Confirm delete selected files', '是否确认删除选中的文件?'))
  if (confirmErr) return

  showLoading(t('Deleting files', '正在删除文件...'))
  const [deleteErr] = await deleteOss(idsToDelete.join(','))
  hideLoading()

  if (!deleteErr) {
    showMsgSuccess(t('message.deleteSuccess'))
    await getList()
  }
}

// =========== 文件上传相关 ===========

/**
 * 文件上传按钮操作
 */
const handleFile = () => {
  reset()
  type.value = 0
  isReplaceMode.value = false
  fileToReplace.value = null
  dialog.value.visible = true
  dialog.value.title = t('Upload File', '上传文件')
}

/**
 * 图片上传按钮操作
 */
const handleImage = () => {
  reset()
  type.value = 1
  isReplaceMode.value = false
  fileToReplace.value = null
  dialog.value.visible = true
  dialog.value.title = t('Upload Image', '上传图片')
}

/**
 * 音视频上传按钮操作
 */
const handleMedia = () => {
  reset()
  type.value = 2
  isReplaceMode.value = false
  fileToReplace.value = null
  dialog.value.visible = true
  dialog.value.title = t('Upload Media', '上传音视频')
}

/** 文件表单重置 */
const reset = () => {
  form.value.file = undefined
  ossFormRef.value?.resetFields()
}

/** 取消文件上传 */
const cancel = () => {
  dialog.value.visible = false
  isReplaceMode.value = false
  fileToReplace.value = null
  reset()
}

/**
 * 处理上传成功事件
 * @param res 上传响应
 * @param file 文件对象
 * @param newFile 新文件信息
 */
const handleUploadSuccess = (res: any, file: any, newFile: any) => {
  buttonLoading.value = false

  // 重置替换状态
  isReplaceMode.value = false
  fileToReplace.value = null

  // 重新加载文件列表
  getList()
}

// =========== 文件替换相关 ===========

/**
 * 批量替换文件（工具栏按钮）
 */
const handleReplaceFile = () => {
  if (selectionItems.value.length !== 1) {
    showMsgWarning(t('Please select one file to replace', '请选择一个文件进行替换'))
    return
  }

  const file = selectionItems.value[0]
  fileToReplace.value = file
  isReplaceMode.value = true

  // 根据文件类型选择对应的上传组件
  if (isImageFile(file.fileSuffix)) {
    type.value = 1
    dialog.value.title = t('Replace Image', '替换图片')
  } else if (isVideoFile(file.fileSuffix) || isAudioFile(file.fileSuffix)) {
    type.value = 2
    dialog.value.title = t('Replace Media', '替换音视频')
  } else {
    type.value = 0
    dialog.value.title = t('Replace File', '替换文件')
  }

  reset()
  dialog.value.visible = true
}

/**
 * 单个文件替换（列表中的替换按钮）
 * @param file 要替换的文件
 */
const handleFileReplace = (file: SysOssVo) => {
  fileToReplace.value = file
  isReplaceMode.value = true

  // 根据文件类型选择对应的上传组件
  if (isImageFile(file.fileSuffix)) {
    type.value = 1
    dialog.value.title = t('Replace Image', '替换图片')
  } else if (isVideoFile(file.fileSuffix) || isAudioFile(file.fileSuffix)) {
    type.value = 2
    dialog.value.title = t('Replace Media', '替换音视频')
  } else {
    type.value = 0
    dialog.value.title = t('Replace File', '替换文件')
  }

  reset()
  dialog.value.visible = true
}

// =========== 文件操作相关 ===========

/**
 * 预览文件
 * @param file 要预览的文件
 */
const handlePreview = (file: SysOssVo) => {
  previewFile.value = file
  isPreviewFullscreen.value = false // 初始化为非全屏
  previewDialogVisible.value = true
}

/**
 * 处理全屏状态变化
 * @param fullscreen 是否全屏
 */
const handleFullscreenChange = (fullscreen: boolean) => {
  isPreviewFullscreen.value = fullscreen
}

/**
 * 下载文件
 * @param file 要下载的文件
 */
const handleDownload = (file: SysOssVo) => {
  useDownload().downloadOss(file.ossId)
}

/**
 * 预览开关
 * @param preview 是否开启预览
 */
const handlePreviewListResource = async (preview: boolean) => {
  const text = preview ? t('Enable', '启用') : t('Disable', '停用')
  const [confirmErr] = await showConfirm(
    t('Confirm preview config change', '确认要"') + text + t('" preview list images config', '""预览列表图片"配置吗?')
  )
  if (confirmErr) return

  const [updateErr] = await updateConfigByKey('system.oss.preview-enabled', preview)
  if (!updateErr) {
    await getList()
    showMsgSuccess(text + t('Success', '成功'))
  }
}

/**
 * 查看OSS配置
 */
const handleOssConfig = () => {
  ossConfigDialog.value.visible = true
}

/** OSS配置成功回调 */
const handleOssConfigSuccess = () => {
  // 可以在这里处理配置成功后的逻辑
}

// =========== 目录操作相关 ===========

/**
 * 添加顶级目录
 */
const handleAddDirectory = () => {
  resetDirectoryForm()
  directoryDialog.value.title = t('Add Directory', '添加目录')
  directoryDialog.value.type = 'add'
  directoryForm.value.parentId = 0 // 根目录
  directoryDialogVisible.value = true
}

/**
 * 添加子目录
 * @param data 父目录数据
 */
const handleAddChildDirectory = (data: SysOssDirectoryTreeVo) => {
  resetDirectoryForm()
  directoryDialog.value.title = t('Add Sub Directory', '添加子目录')
  directoryDialog.value.type = 'add'
  directoryForm.value.parentId = data.id
  directoryDialogVisible.value = true
}

/**
 * 重命名目录
 * @param data 要重命名的目录数据
 */
const handleRenameDirectory = (data: SysOssDirectoryTreeVo) => {
  resetDirectoryForm()
  directoryDialog.value.title = t('Rename Directory', '重命名目录')
  directoryDialog.value.type = 'edit'
  directoryForm.value.directoryId = data.id
  directoryForm.value.parentId = data.parentId
  directoryForm.value.directoryName = data.label
  directoryDialogVisible.value = true
}

/**
 * 删除目录
 * @param data 要删除的目录
 */
const handleDeleteDirectory = async (data: SysOssDirectoryTreeVo) => {
  // 检查是否有子目录
  if (data.children && data.children.length > 0) {
    showMsgWarning(t('Directory has sub directories', '该目录包含子目录，请先删除子目录'))
    return
  }

  const [confirmErr] = await showConfirm(t('Confirm delete directory', '是否确认删除目录"') + data.label + '"？')
  if (confirmErr) return

  showLoading(t('Deleting directory', '正在删除目录...'))
  const [deleteErr] = await deleteOssDirectorys(data.id)
  hideLoading()

  if (!deleteErr) {
    showMsgSuccess(t('message.deleteSuccess'))

    // 重新加载目录
    await loadDirectories()

    // 如果删除的是当前选中的目录，切换到全部目录
    if (currentDirectory.value && currentDirectory.value.id === data.id) {
      const allDir = findDirectoryById(directories.value, ALL.value)
      if (allDir) {
        currentDirectory.value = allDir
        queryParams.value.directoryId = allDir.id
        await getList()
      }
    }
  }
}

/**
 * 重置目录表单
 */
const resetDirectoryForm = () => {
  directoryForm.value.directoryId = undefined
  directoryForm.value.parentId = undefined
  directoryForm.value.directoryName = ''
  directoryDialog.value.loading = false
}

/**
 * 提交目录表单
 */
const submitDirectoryForm = async () => {
  if (!directoryFormRef.value) return

  const [validateErr] = await toValidate(directoryFormRef)
  if (validateErr) return

  directoryDialog.value.loading = true

  let err: Error | null
  if (directoryDialog.value.type === 'add') {
    ;[err] = await addOssDirectory(directoryForm.value)
  } else {
    ;[err] = await updateOssDirectory(directoryForm.value)
  }

  if (!err) {
    showMsgSuccess(directoryDialog.value.type === 'add' ? t('message.addSuccess') : t('message.updateSuccess'))
    directoryDialogVisible.value = false
    directoryDialog.value.loading = false
    await loadDirectories()
  } else {
    directoryDialog.value.loading = false
  }
}

// =========== 文件移动相关 ===========

/**
 * 移动单个文件
 * @param file 要移动的文件
 */
const handleFileMove = (file: SysOssVo) => {
  filesToMove.value = [file]
  moveDialogVisible.value = true
}

/**
 * 批量移动文件
 */
const handleMoveFiles = () => {
  if (selectionItems.value.length === 0) {
    showMsgWarning(t('Please select files to move', '请选择要移动的文件'))
    return
  }

  filesToMove.value = [...selectionItems.value]
  moveDialogVisible.value = true
}

/**
 * 确认移动文件
 */
const confirmMove = async () => {
  if (!moveTreeRef.value) {
    showMsgWarning(t('Please select target directory', '请选择目标目录'))
    return
  }

  const targetNode = moveTreeRef.value.getCurrentNode() as SysOssDirectoryTreeVo
  if (!targetNode) {
    showMsgWarning(t('Please select target directory', '请选择目标目录'))
    return
  }

  showLoading(t('Moving files', '正在移动文件...'))
  // 批量移动文件
  const ossIds = filesToMove.value.map((file) => file.ossId)
  const [moveErr] = await moveOssDirectory(targetNode.id, ossIds)
  hideLoading()

  if (!moveErr) {
    showMsgSuccess(t('Move success', '移动成功'))
    moveDialogVisible.value = false

    // 清空选择
    selectionItems.value = []

    // 刷新文件列表
    await getList()
  }
}

// =========== 生命周期 ===========

/**初始化文件数据列表*/
onMounted(() => {
  loadData()
})
onActivated(() => {
  if (isLoading.value) return
  loadData()
})
</script>
