<!--
OSS媒体库管理器组件
* 文件名：AOssMediaManager.vue
* 功能：整合文件目录树、文件列表和各种操作功能，提供完整的OSS文件管理界面
* 使用场景：需要进行OSS文件管理的场景，如内容管理系统、媒体库等
-->
<template>
  <div>
    <el-dialog v-model="dialogVisible" :title="t('ossMediaManager.title')" width="80%" append-to-body destroy-on-close>
      <!-- 添加文件类型提示区域 -->
      <div v-if="props.acceptFileTypes && props.acceptFileTypes.length > 0" class="bg-blue-50 p-2 rounded mb-3 text-sm flex items-center">
        <el-icon class="mr-2">
          <InfoFilled />
        </el-icon>
        <span>{{ t('ossMediaManager.fileTypeHint', { types: props.acceptFileTypes.join(', ') }) }}</span>
      </div>

      <!-- 添加侧边栏切换按钮，在小屏幕下显示 -->
      <el-button v-if="isSmallScreen" class="directory-toggle-btn mb-3" type="primary" size="small" @click="toggleDirectorySidebar">
        <el-icon>
          <Menu />
        </el-icon>
        {{ sidebarVisible ? t('ossMediaManager.hideDirectory') : t('ossMediaManager.showDirectory') }}
      </el-button>

      <el-row :gutter="20" class="media-manager-container">
        <!-- 左侧目录树 - 响应式处理 -->
        <el-col
          :span="isSmallScreen ? (sidebarVisible ? 24 : 0) : 6"
          :class="{ 'directory-sidebar': isSmallScreen, 'is-visible': sidebarVisible }"
          class="directory-container"
        >
          <el-card shadow="hover" class="h-full">
            <template #header>
              <div class="flex justify-between items-center">
                <span class="font-bold">{{ t('ossMediaManager.folder') }}</span>
                <el-button v-permi="['system:ossDirectory:add']" type="primary" link @click="handleAddDirectory">
                  <el-icon>
                    <Plus />
                  </el-icon>
                  {{ t('ossMediaManager.add') }}
                </el-button>
              </div>
            </template>
            <el-input v-model="directoryName" :placeholder="t('ossMediaManager.directoryPlaceholder')" prefix-icon="Search" clearable />
            <el-tree
              ref="directoryTreeRef"
              class="mt-2 directory-tree"
              node-key="id"
              :data="directories"
              :props="{ label: 'label', children: 'children' }"
              :expand-on-click-node="false"
              :filter-node-method="filterNode"
              highlight-current
              :default-expanded-keys="expandedKeys"
              @node-expand="handleNodeExpand"
              @node-collapse="handleNodeCollapse"
              @node-click="handleDirectorySelect"
            >
              <template #default="{ node, data }">
                <div class="w-full directory-node flex justify-between items-center group">
                  <div class="directory-label truncate">
                    <el-icon class="mr-1">
                      <Folder />
                    </el-icon>
                    {{ node.label }}
                  </div>
                  <div
                    v-if="data.id !== ALL && data.id !== UNCATEGORIZED"
                    class="directory-actions opacity-0 transition-opacity duration-300 group-hover:opacity-100"
                  >
                    <!-- 在较宽屏幕上显示完整按钮 -->
                    <div class="actions-expanded hidden md:flex">
                      <el-button v-permi="['system:ossDirectory:add']" type="primary" link @click.stop="handleAddChildDirectory(data)">
                        <el-icon>
                          <Plus />
                        </el-icon>
                      </el-button>
                      <el-button v-permi="['system:ossDirectory:update']" type="primary" link @click.stop="handleRenameDirectory(data)">
                        <el-icon>
                          <Edit />
                        </el-icon>
                      </el-button>
                      <el-button v-permi="['system:ossDirectory:delete']" type="primary" link @click.stop="handleDeleteDirectory(data)">
                        <el-icon>
                          <Delete />
                        </el-icon>
                      </el-button>
                    </div>
                    <!-- 在窄屏幕或目录层级深时使用下拉菜单 -->
                    <div class="actions-collapsed md:hidden">
                      <el-dropdown trigger="click" @command="handleDirectoryCommand($event, data)">
                        <el-button type="primary" link>
                          <el-icon>
                            <MoreFilled />
                          </el-icon>
                        </el-button>
                        <template #dropdown>
                          <el-dropdown-menu>
                            <el-dropdown-item :disabled="!hasPermission('system:ossDirectory:add')" command="add">
                              <el-icon>
                                <Plus />
                              </el-icon>
                              {{ t('ossMediaManager.add') }}
                            </el-dropdown-item>
                            <el-dropdown-item :disabled="!hasPermission('system:ossDirectory:update')" command="rename">
                              <el-icon>
                                <Edit />
                              </el-icon>
                              {{ t('ossMediaManager.rename') }}
                            </el-dropdown-item>
                            <el-dropdown-item :disabled="!hasPermission('system:ossDirectory:delete')" command="delete">
                              <el-icon>
                                <Delete />
                              </el-icon>
                              {{ t('ossMediaManager.delete') }}
                            </el-dropdown-item>
                          </el-dropdown-menu>
                        </template>
                      </el-dropdown>
                    </div>
                  </div>
                </div>
              </template>
            </el-tree>

            <!-- 小屏幕下的关闭按钮 -->
            <div v-if="isSmallScreen && sidebarVisible" class="text-center mt-4">
              <el-button size="small" @click="toggleDirectorySidebar">{{ t('ossMediaManager.closeDirectory') }}</el-button>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧文件区域 - 响应式处理 -->
        <el-col :span="isSmallScreen ? (sidebarVisible ? 0 : 24) : 18" class="file-container">
          <el-card shadow="hover" class="h-full">
            <!-- 固定区域：工具栏和模式选择 -->
            <template #header>
              <div class="mb-3">
                <!-- 路径和操作按钮 -->
                <div class="flex flex-wrap justify-between items-center mb-3">
                  <div class="flex flex-wrap items-center">
                    <span class="font-bold mr-3 truncate max-w-[200px]">{{ currentPath }}</span>
                    <el-button v-permi="['system:oss:upload']" type="primary" @click="handleUpload" class="mr-2">
                      <el-icon>
                        <Upload />
                      </el-icon>
                      <span>{{ t('ossMediaManager.uploadFile') }}</span>
                    </el-button>
                    <el-button v-if="enableReplace && selectionItems.length === 1" type="primary" @click="handleReplaceFile" class="mr-2">
                      <el-icon>
                        <RefreshRight />
                      </el-icon>
                      <span>{{ t('ossMediaManager.replace') }}</span>
                    </el-button>
                    <el-button v-if="showMove && selectionItems.length > 0" type="primary" @click="handleMoveFiles" class="mr-2">
                      <el-icon>
                        <Position />
                      </el-icon>
                      <span>{{ t('ossMediaManager.moveTo') }}</span>
                    </el-button>
                    <el-button v-if="selectionItems.length > 0" type="danger" @click="handleBatchDelete">
                      <el-icon>
                        <Delete />
                      </el-icon>
                      <span>{{ t('ossMediaManager.batchDelete') }}</span>
                    </el-button>
                  </div>
                  <div class="search-container">
                    <el-input v-model="query.originalName" :placeholder="t('ossMediaManager.searchPlaceholder')" clearable class="search-input" @input="handleQuery">
                      <template #prefix>
                        <el-icon>
                          <Search />
                        </el-icon>
                      </template>
                    </el-input>
                  </div>
                </div>

                <!-- 视图模式切换和排序功能 -->
                <div class="flex flex-wrap justify-between items-center mb-2 gap-2">
                  <!-- 左侧：已选择项数量显示 -->
                  <div class="flex flex-wrap items-center gap-2">
                    <div v-if="selectionItems.length > 0" class="text-sm text-primary mr-3 flex items-center gap-2">
                      <el-tag type="info" closable @close="clearSelection"> {{ t('ossMediaManager.selectedCount', { count: selectionItems.length }) }} </el-tag>
                    </div>
                    <div v-else class="text-sm text-gray-400 mr-3">{{ t('ossMediaManager.noSelection') }}</div>

                    <!-- 文件类型筛选 -->
                    <el-select
                      v-model="fileTypeFilter"
                      :placeholder="t('ossMediaManager.fileType')"
                      clearable
                      size="small"
                      @change="handleFileTypeFilterChange"
                      class="mr-3 file-type-filter"
                    >
                      <el-option :label="t('ossMediaManager.allFiles')" value="all" />
                      <!-- 只有当acceptFileTypes为空或包含图片类型时才显示图片选项 -->
                      <el-option v-if="showFileTypeOption('image')" :label="t('ossMediaManager.images')" value="image" />
                      <!-- 只有当acceptFileTypes为空或包含文档类型时才显示文档选项 -->
                      <el-option v-if="showFileTypeOption('document')" :label="t('ossMediaManager.documents')" value="document" />
                      <!-- 只有当acceptFileTypes为空或包含视频类型时才显示视频选项 -->
                      <el-option v-if="showFileTypeOption('video')" :label="t('ossMediaManager.videos')" value="video" />
                      <!-- 只有当acceptFileTypes为空或包含音频类型时才显示音频选项 -->
                      <el-option v-if="showFileTypeOption('audio')" :label="t('ossMediaManager.audios')" value="audio" />
                      <!-- 其他类型选项 -->
                      <el-option v-if="showFileTypeOption('other')" :label="t('ossMediaManager.others')" value="other" />
                    </el-select>
                  </div>

                  <!-- 右侧：排序下拉菜单 -->
                  <div class="flex items-center">
                    <!-- 排序下拉菜单 -->
                    <el-dropdown @command="handleSort" class="mr-3">
                      <el-button type="info" size="small" plain>
                        <span>{{ getSortLabel() }}</span>
                        <el-icon class="ml-1">
                          <ArrowDown />
                        </el-icon>
                      </el-button>
                      <template #dropdown>
                        <el-dropdown-menu>
                          <el-dropdown-item
                            v-for="item in sortOptions"
                            :key="item.value"
                            :command="item.command"
                            :class="{ 'is-active': sortConfig.orderByColumn === item.command.column && sortConfig.isAsc === item.command.order }"
                          >
                            {{ item.label }}
                            <el-icon v-if="sortConfig.orderByColumn === item.command.column" class="ml-1">
                              <Sort v-if="sortConfig.isAsc === 'asc'" />
                              <Sort v-else style="transform: rotate(180deg)" />
                            </el-icon>
                          </el-dropdown-item>
                        </el-dropdown-menu>
                      </template>
                    </el-dropdown>
                  </div>
                </div>
              </div>
            </template>

            <!-- 可滚动区域：文件列表内容 -->
            <el-scrollbar :height="450" ref="fileScrollbarRef">
              <!-- 内容为空时显示 -->
              <el-empty v-if="files.length === 0" :description="t('ossMediaManager.noFiles')" />

              <!-- 网格视图 -->
              <div v-else class="responsive-grid">
                <!-- 网格视图中的文件项 -->
                <div v-for="file in files" :key="file.ossId" class="p-1 relative">
                  <div
                    class="border border-gray-200 rounded overflow-hidden cursor-pointer transition-all duration-300 bg-white h-full flex flex-col hover:shadow-md hover:translate-y-[-2px]"
                    :class="{ 'ring-2 ring-primary translate-y-[-2px] z-10': isFileSelected(file) }"
                    @click="toggleFileSelection(file, $event)"
                    @dblclick="handleFilePreview(file)"
                  >
                    <div class="aspect-square bg-gray-50 relative flex items-center justify-center overflow-hidden">
                      <!-- 图片预览 -->
                      <el-image
                        class="w-full h-full object-cover"
                        v-if="isImageFile(file.fileSuffix)"
                        :src="getImageSrcWithCache(file)"
                        fit="cover"
                        lazy
                      />
                      <!-- 文件类型图标 -->
                      <div v-else class="text-4xl text-gray-400">
                        <div v-if="isDocumentFile(file.fileSuffix)">
                          <Icon code="pdf" class="text-#EF5350" v-if="file.fileSuffix === '.pdf'" />
                          <Icon code="word" class="text-#2C90EF" v-else-if="file.fileSuffix === '.doc' || file.fileSuffix === '.docx'" />
                          <Icon code="excel" class="text-#99CC33" v-else-if="file.fileSuffix === '.xls' || file.fileSuffix === '.xlsx'" />
                          <Icon code="ppt" class="text-#F58966" v-else-if="file.fileSuffix === '.ppt' || file.fileSuffix === '.pptx'" />
                          <Icon code="text" class="text-#99CCFF" v-else-if="file.fileSuffix === '.txt'" />
                          <el-icon v-else>
                            <Document />
                          </el-icon>
                        </div>
                        <el-icon v-else-if="isVideoFile(file.fileSuffix)">
                          <VideoPlay />
                        </el-icon>
                        <el-icon v-else-if="isAudioFile(file.fileSuffix)">
                          <Headset />
                        </el-icon>
                        <el-icon v-else>
                          <Files />
                        </el-icon>
                      </div>
                      <!-- 选中标记 -->
                      <div
                        v-if="isFileSelected(file)"
                        class="absolute right-1 top-1 w-5 h-5 rounded-full bg-green-500 flex items-center justify-center text-white z-20"
                      >
                        <el-icon>
                          <Check />
                        </el-icon>
                      </div>
                    </div>
                    <div class="p-2">
                      <div class="text-sm overflow-hidden text-ellipsis whitespace-nowrap" :title="file.fileName">
                        {{ file.originalName }}
                      </div>
                      <div class="text-xs text-gray-400 mt-1 flex justify-between">
                        <span>{{ formatFileSize(file.fileSize) || t('ossMediaManager.unknownSize') }}</span>
                        <span>{{ file.fileSuffix }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 优化后的无限滚动触发元素 -->
              <div v-if="isLoadingMore" class="text-center py-4">
                <el-icon class="is-loading">
                  <Loading />
                </el-icon>
                <span class="ml-2">{{ t('ossMediaManager.loading') }}</span>
              </div>

              <!-- 仅在有数据且未加载完全时显示加载更多元素 -->
              <div
                v-else-if="files.length > 0 && pagination.total > files.length"
                ref="loadMoreTriggerRef"
                class="text-center py-4 load-more-trigger"
              >
                <div class="text-primary cursor-pointer">{{ t('ossMediaManager.loadingMore') }}</div>
              </div>

              <div v-else-if="files.length > 0" class="text-center py-4 text-gray-400">{{ t('ossMediaManager.allFilesLoaded') }}</div>
            </el-scrollbar>
          </el-card>
        </el-col>
      </el-row>

      <!-- 各种对话框 -->
      <!-- 文件预览对话框 -->
      <el-dialog
        v-model="previewDialogVisible"
        :title="t('ossMediaManager.filePreview')"
        width="80%"
        append-to-body
        destroy-on-close
        close-on-click-modal
        class="preview-dialog"
      >
        <div class="flex flex-col items-center justify-center">
          <!-- 图片预览 - 不变 -->
          <el-image v-if="previewFile && isImageFile(previewFile.fileSuffix)" :src="previewFile?.url" fit="contain" class="max-w-full max-h-[70vh]" />

          <!-- 视频预览 - 新增 -->
          <video v-else-if="previewFile && isVideoFile(previewFile.fileSuffix)" controls class="max-w-full max-h-[70vh]">
            <source :src="previewFile.url" :type="getMediaType(previewFile.fileSuffix)" />
            {{ t('ossMediaManager.videoNotSupported') }}
          </video>

          <!-- 音频预览 - 新增 -->
          <div v-else-if="previewFile && isAudioFile(previewFile.fileSuffix)" class="w-full flex flex-col items-center">
            <div class="text-6xl text-gray-400 mb-4">
              <el-icon>
                <Headset />
              </el-icon>
            </div>
            <div class="mb-4">{{ previewFile.fileName || previewFile.originalName }}</div>
            <audio controls class="w-full max-w-md">
              <source :src="previewFile.url" :type="getMediaType(previewFile.fileSuffix)" />
              {{ t('ossMediaManager.audioNotSupported') }}
            </audio>
          </div>

          <!-- PDF预览 - 新增 -->
          <iframe
            v-else-if="previewFile && previewFile.fileSuffix.toLowerCase() === '.pdf'"
            :src="previewFile.url"
            class="w-full h-[70vh]"
            frameborder="0"
          ></iframe>

          <!-- 文本文件预览 - 新增 -->
          <div v-else-if="previewFile && isTextFile(previewFile.fileSuffix)" class="w-full h-[70vh] overflow-auto">
            <div v-if="textPreviewContent" class="whitespace-pre-wrap p-4 font-mono text-sm">{{ textPreviewContent }}</div>
            <div v-else class="flex justify-center items-center h-full">
              <el-button @click="loadTextPreview(previewFile)" type="primary">{{ t('ossMediaManager.loadTextContent') }}</el-button>
            </div>
          </div>

          <!-- 其他文件预览（无法预览时的默认视图） -->
          <div v-else-if="previewFile" class="text-center">
            <div class="text-6xl text-gray-400 mb-4">
              <div v-if="isDocumentFile(previewFile.fileSuffix)">
                <Icon code="pdf" class="text-#EF5350" v-if="previewFile.fileSuffix === '.pdf'" />
                <Icon code="word" class="text-#2C90EF" v-else-if="previewFile.fileSuffix === '.doc' || previewFile.fileSuffix === '.docx'" />
                <Icon code="excel" class="text-#99CC33" v-else-if="previewFile.fileSuffix === '.xls' || previewFile.fileSuffix === '.xlsx'" />
                <Icon code="ppt" class="text-#F58966" v-else-if="previewFile.fileSuffix === '.ppt' || previewFile.fileSuffix === '.pptx'" />
                <Icon code="text" class="text-#99CCFF" v-else-if="previewFile.fileSuffix === '.txt'" />
                <el-icon v-else>
                  <Document />
                </el-icon>
              </div>
              <el-icon v-else>
                <Files />
              </el-icon>
            </div>
            <div>{{ previewFile?.originalName || previewFile?.fileName }}</div>
            <div class="mt-2 text-gray-500">{{ formatFileSize(previewFile?.fileSize) }}</div>
            <el-button type="primary" class="mt-4" @click="downloadFile(previewFile)">
              <el-icon>
                <Download />
              </el-icon>
              {{ t('ossMediaManager.downloadFile') }}
            </el-button>
          </div>
        </div>
      </el-dialog>

      <!-- 移动文件对话框 -->
      <el-dialog v-model="moveDialogVisible" :title="t('ossMediaManager.moveToFolder')" width="30%" append-to-body>
        <el-tree
          ref="moveTreeRef"
          :data="directories"
          node-key="id"
          :props="{ label: 'label', children: 'children' }"
          highlight-current
          default-expand-all
          :expand-on-click-node="false"
          class="directory-move-tree"
        />
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="moveDialogVisible = false">{{ t('ossMediaManager.cancel') }}</el-button>
            <el-button type="primary" @click="confirmMove">{{ t('ossMediaManager.confirm') }}</el-button>
          </div>
        </template>
      </el-dialog>

      <!-- 目录操作对话框 -->
      <el-dialog v-model="directoryDialogVisible" :title="directoryDialog.title" width="30%" append-to-body>
        <el-form ref="directoryFormRef" :model="directoryForm" :rules="directoryRules" label-width="auto">
          <el-form-item :label="t('ossMediaManager.directoryName')" prop="directoryName">
            <el-input v-model="directoryForm.directoryName" :placeholder="t('ossMediaManager.directoryNamePlaceholder')" />
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="directoryDialogVisible = false">{{ t('ossMediaManager.cancel') }}</el-button>
            <el-button :loading="directoryDialog.loading" type="primary" @click="submitDirectoryForm">{{ t('ossMediaManager.confirm') }}</el-button>
          </div>
        </template>
      </el-dialog>

      <!-- 文件上传对话框 -->
      <el-dialog v-model="uploadDialogVisible" :title="uploadDialog.title" width="500px" append-to-body>
        <el-upload
          ref="uploadRef"
          :action="getUploadUrl"
          :headers="headers"
          :before-upload="beforeFileUpload"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          multiple
          :limit="uploadDialog.type === 'replace' ? 1 : 99999"
          :file-list="uploadFileList"
          :accept="computedAcceptTypes"
        >
          <el-button type="primary">{{ t('ossMediaManager.selectFile') }}</el-button>
          <template #tip>
            <div class="el-upload__tip">
              {{ uploadDialog.type === 'replace' ? t('ossMediaManager.replaceUploadTip') : t('ossMediaManager.uploadTip') }}
            </div>
          </template>
        </el-upload>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="uploadDialogVisible = false">{{ t('ossMediaManager.close') }}</el-button>
          </div>
        </template>
      </el-dialog>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancel">{{ t('ossMediaManager.cancel') }}</el-button>
          <el-button
            type="primary"
            @click="confirmSelection"
            :disabled="props.multiSelect ? selectionItems.length === 0 : selectionItems.length !== 1"
          >
            {{ t('ossMediaManager.confirmSelection') }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="AOssMediaManager">
import { pageOss, deleteOss } from '@/api/system/oss/oss/ossApi'
import type { SysOssVo } from '@/api/system/oss/oss/ossTypes'
import {
  addOssDirectory,
  updateOssDirectory,
  deleteOssDirectorys,
  getOssDirectoryTreeOptions,
  moveOssDirectory
} from '@/api/system/oss/ossDirectory/ossDirectoryApi'
import type { SysOssDirectoryBo, SysOssDirectoryTreeVo } from '@/api/system/oss/ossDirectory/ossDirectoryTypes'
import { formatFileSize } from '@/utils/format'
import { SystemConfig } from '@/systemConfig'
import { toValidate } from '@/utils/to'
import { showMsgWarning, showMsgError, showConfirm, showMsgSuccess } from '@/utils/modal'
import { useI18n } from '@/composables/useI18n'

const { t } = useI18n()
const { hasPermission } = useAuth()

/**
 * 素材库组件属性接口
 */
interface AOssMediaManagerProps {
  /**
   * 控制对话框是否显示
   * @default false
   */
  modelValue: boolean

  /**
   * 是否支持多选
   * @default false
   */
  multiSelect?: boolean

  /**
   * 限制文件大小
   * @default 默认10MB
   */
  fileSize?: number

  /**
   * 是否显示移动文件功能
   * @default true
   */
  showMove?: boolean

  /**
   * 默认选中的目录ID
   * @default null
   */
  defaultDirectoryId?: number | string | null

  /**
   * 接受的文件类型
   * @default [] - 空数组表示接受所有类型
   */
  acceptFileTypes?: string[]

  /**
   * 是否启用替换功能
   * @default true
   */
  enableReplace?: boolean
}

// 组件属性定义
const props = withDefaults(defineProps<AOssMediaManagerProps>(), {
  multiSelect: false,
  showMove: true,
  fileSize: 10,
  defaultDirectoryId: null,
  acceptFileTypes: () => [],
  enableReplace: true
})

// 事件定义
const emit = defineEmits(['update:modelValue', 'select', 'cancel'])

// 对话框可见性
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// DOM引用
const directoryTreeRef = ref<ElTreeInstance>()
const moveTreeRef = ref<ElTreeInstance>()
const directoryFormRef = ref<ElFormInstance>()
const uploadRef = ref<ElUploadInstance>()
const fileScrollbarRef = ref()
// 无限滚动触发元素的引用
const loadMoreTriggerRef = ref(null)

// =========== 响应式状态管理相关变量 ===========
const isSmallScreen = ref(false)
const sidebarVisible = ref(false)

// =========== 状态定义 ===========
const ALL = ref('9999999999999999')
const UNCATEGORIZED = ref('10000000000000000')
// 目录数据
const directories = ref<SysOssDirectoryTreeVo[] | null>(null)
const directoryName = ref('') // 目录搜索关键词
const currentDirectory = ref<SysOssDirectoryTreeVo | null>(null)
const currentPath = computed(() => {
  if (!currentDirectory.value) return '/'
  return currentDirectory.value.directoryPath || '/'
})

const expandedKeys = ref<(string | number)[]>([]) // 存储展开的节点

// 文件类型筛选
const fileTypeFilter = ref('all')
const fileTypeMap = {
  image: ['.png', '.jpg', '.jpeg', '.gif', '.webp', '.bmp', '.svg'],
  document: ['.doc', '.docx', '.xls', '.xlsx', '.ppt', '.pptx', '.pdf', '.txt'],
  video: ['.mp4', '.avi', '.mov', '.wmv', '.flv', '.mkv'],
  audio: ['.mp3', '.wav', '.ogg', '.flac', '.aac'],
  all: [],
  other: []
}

/** 文本文件预览内容 */
const textPreviewContent = ref('')

/**
 * 根据acceptFileTypes自动判断默认应该使用的文件类型筛选
 */
const defaultFileTypeFilter = computed(() => {
  if (!props.acceptFileTypes || props.acceptFileTypes.length === 0) {
    return 'all' // 没有限制时默认显示全部
  }

  // 将接受的类型转换为小写和添加点号（如果需要）
  const acceptTypesNormalized = props.acceptFileTypes.map((type) => (type.startsWith('.') ? type.toLowerCase() : `.${type.toLowerCase()}`))

  // 检查每种类型组的重叠程度
  const typeMatches = {
    image: fileTypeMap.image.filter((type) => acceptTypesNormalized.includes(type)).length,
    document: fileTypeMap.document.filter((type) => acceptTypesNormalized.includes(type)).length,
    video: fileTypeMap.video.filter((type) => acceptTypesNormalized.includes(type)).length,
    audio: fileTypeMap.audio.filter((type) => acceptTypesNormalized.includes(type)).length
  }

  // 找出匹配度最高的类型组
  const maxMatches = Math.max(...Object.values(typeMatches))

  // 如果没有匹配项或匹配项很少，则使用"other"
  if (maxMatches === 0) {
    return 'other'
  }

  // 找出匹配度最高的类型组名称
  for (const [type, matches] of Object.entries(typeMatches)) {
    if (matches === maxMatches) {
      return type
    }
  }

  return 'all'
})

// 查询参数
const query = ref<{
  fileName: string
  originalName: string
  fileSuffix: string
}>({
  fileName: '',
  originalName: '',
  fileSuffix: ''
})

// 排序配置
const sortConfig = ref({
  orderByColumn: 'updateTime',
  isAsc: 'desc'
})

// 排序选项
const sortOptions = computed(() => [
  { label: t('ossMediaManager.sortUpdateTimeDesc'), value: 'updateTime-desc', command: { column: 'updateTime', order: 'desc' } },
  { label: t('ossMediaManager.sortUpdateTimeAsc'), value: 'updateTime-asc', command: { column: 'updateTime', order: 'asc' } },
  { label: t('ossMediaManager.sortFileNameAsc'), value: 'fileName-asc', command: { column: 'originalName', order: 'asc' } },
  { label: t('ossMediaManager.sortFileNameDesc'), value: 'fileName-desc', command: { column: 'originalName', order: 'desc' } },
  { label: t('ossMediaManager.sortFileSizeAsc'), value: 'fileSize-asc', command: { column: 'fileSize', order: 'asc' } },
  { label: t('ossMediaManager.sortFileSizeDesc'), value: 'fileSize-desc', command: { column: 'fileSize', order: 'desc' } },
  { label: t('ossMediaManager.sortFileTypeAsc'), value: 'fileSuffix-asc', command: { column: 'fileSuffix', order: 'asc' } },
  { label: t('ossMediaManager.sortFileTypeDesc'), value: 'fileSuffix-desc', command: { column: 'fileSuffix', order: 'desc' } }
])

// 分页配置
const pagination = ref({
  pageNum: 1,
  pageSize: 50,
  total: 0
})

// 状态标记
const isLoading = ref(false)
const isLoadingMore = ref(false)
const loadingTimer = ref<ReturnType<typeof setTimeout> | null>(null)
const hasLoadedFiles = ref(false) // 标记是否已经加载过文件

// 文件数据
const files = ref<SysOssVo[]>([])

// 文件选择状态管理
const selectionItems = ref<SysOssVo[]>([])

// 文件预览
const previewDialogVisible = ref(false)
const previewFile = ref<SysOssVo | null>(null)

// 文件上传配置
const uploadDialogVisible = ref(false)
const uploadFileList = ref<any[]>([])
const baseUrl = SystemConfig.api.baseUrl
const uploadUrl = computed(() => baseUrl + '/resource/oss/upload')
const headers = ref(useToken().getAuthHeaders())

// 获取上传URL，处理替换和普通上传，并添加目录ID
const getUploadUrl = computed(() => {
  let url
  // 处理替换操作
  if (uploadDialog.value.type === 'replace' && fileToReplace.value) {
    url = `${baseUrl}/resource/oss/replace/${fileToReplace.value.ossId}`
  } else {
    // 普通上传
    url = uploadUrl.value
    if (currentDirectory.value && currentDirectory.value.id !== ALL.value && currentDirectory.value.id !== UNCATEGORIZED.value) {
      url += `?directoryId=${currentDirectory.value.id}`
    }
  }
  return url
})

// 当前要替换的文件
const fileToReplace = ref<SysOssVo | null>(null)
// 上传对话框配置
const uploadDialog = ref({
  title: '',
  type: 'upload' // 'upload' 或 'replace'
})

// 移动文件配置
const moveDialogVisible = ref(false)
const filesToMove = ref<SysOssVo[]>([])

// 目录操作配置
const directoryDialogVisible = ref(false)
const directoryForm = ref<SysOssDirectoryBo>({
  directoryId: undefined,
  parentId: undefined,
  directoryName: ''
})
const directoryRules = computed(() => ({
  directoryName: [
    { required: true, message: t('ossMediaManager.directoryNameRequired'), trigger: 'blur' },
    { min: 1, max: 50, message: t('ossMediaManager.directoryNameLength'), trigger: 'blur' },
    {
      // eslint-disable-next-line @typescript-eslint/no-unsafe-function-type
      validator: (rule: any, value: string, callback: Function) => {
        if (value && /[\\/:*?"<>|]/.test(value)) {
          callback(new Error(t('ossMediaManager.directoryNameInvalid')))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}))
const directoryDialog = ref({
  title: '',
  type: '', // add, edit
  loading: false
})

// 无限滚动观察器引用
const loadMoreObserver = ref<IntersectionObserver | null>(null)

/**
 * 切换侧边栏显示状态
 */
const toggleDirectorySidebar = () => {
  sidebarVisible.value = !sidebarVisible.value
}

/**
 * 更新屏幕尺寸状态
 */
const updateScreenSize = () => {
  isSmallScreen.value = window.innerWidth < 768
  // 大屏幕下总是显示侧边栏
  if (!isSmallScreen.value) {
    sidebarVisible.value = true
  }
}

/**
 * 处理目录命令（从下拉菜单）
 * @param {string} command 命令类型
 * @param {SysOssDirectoryTreeVo} data 目录数据
 */
const handleDirectoryCommand = (command, data) => {
  switch (command) {
    case 'add':
      handleAddChildDirectory(data)
      break
    case 'rename':
      handleRenameDirectory(data)
      break
    case 'delete':
      handleDeleteDirectory(data)
      break
  }
}

// =========== 文件类型相关方法 ===========

/**
 * 处理文件类型筛选变化
 */
const handleFileTypeFilterChange = () => {
  loadFiles(true)
}

/**
 * 判断URL是否已经是预签名URL
 * @param url URL地址
 * @returns 是否为预签名URL
 */
const isPresignedUrl = (url: string): boolean => {
  if (!url) return false

  // 检测各云服务商的预签名URL特征参数
  return (
    url.includes('X-Amz-Algorithm') || // AWS S3/MinIO
    url.includes('X-Amz-Signature') || // AWS S3/MinIO
    url.includes('X-Amz-Credential') || // AWS S3/MinIO
    url.includes('OSSAccessKeyId') || // 阿里云OSS
    url.includes('Signature') || // 阿里云OSS/腾讯云COS
    url.includes('q-sign-algorithm') || // 腾讯云COS
    url.includes('q-signature') || // 腾讯云COS
    (url.includes('e=') && url.includes('token=')) || // 七牛云
    url.includes('AWSAccessKeyId') || // AWS S3 v2
    (url.includes('Expires') && url.includes('Signature')) // 华为云OBS等
  )
}

/**
 * 获取带缓存参数的图片地址
 * @param file 文件对象
 * @returns 处理后的图片URL
 */
const getImageSrcWithCache = (file: SysOssVo): string => {
  if (!file.url) return ''

  // 如果是预签名URL，直接返回不添加缓存参数
  if (isPresignedUrl(file.url)) {
    return file.url
  }

  // 非预签名URL添加缓存参数
  const separator = file.url.includes('?') ? '&' : '?'
  return `${file.url}${separator}t=${file.updateTime || Date.now()}`
}

/**
 * 判断是否为图片文件
 * @param fileSuffix 文件后缀
 * @returns 是否为图片类型
 */
const isImageFile = (fileSuffix: string): boolean => {
  if (!fileSuffix) return false
  const suffix = fileSuffix.toLowerCase()
  return fileTypeMap.image.includes(suffix)
}

/**
 * 判断是否为文档文件
 * @param fileSuffix 文件后缀
 * @returns 是否为文档类型
 */
const isDocumentFile = (fileSuffix: string): boolean => {
  if (!fileSuffix) return false
  const suffix = fileSuffix.toLowerCase()
  return fileTypeMap.document.includes(suffix)
}

/**
 * 判断是否为视频文件
 * @param fileSuffix 文件后缀
 * @returns 是否为视频类型
 */
const isVideoFile = (fileSuffix: string): boolean => {
  if (!fileSuffix) return false
  const suffix = fileSuffix.toLowerCase()
  return fileTypeMap.video.includes(suffix)
}

/**
 * 判断是否为音频文件
 * @param fileSuffix 文件后缀
 * @returns 是否为音频类型
 */
const isAudioFile = (fileSuffix: string): boolean => {
  if (!fileSuffix) return false
  const suffix = fileSuffix.toLowerCase()
  return fileTypeMap.audio.includes(suffix)
}

/**
 * 判断是否显示特定的文件类型选项
 * @param typeGroup 文件类型组名称
 * @returns 是否显示该选项
 */
const showFileTypeOption = (typeGroup: 'image' | 'document' | 'video' | 'audio' | 'other'): boolean => {
  // 如果没有限制或空数组，显示所有选项
  if (!props.acceptFileTypes || props.acceptFileTypes.length === 0) {
    return true
  }

  // 将接受的类型转换为小写和添加点号（如果需要）
  const acceptTypesNormalized = props.acceptFileTypes.map((type) => (type.startsWith('.') ? type.toLowerCase() : `.${type.toLowerCase()}`))

  // 检查特定类型组中是否有任何类型在接受的类型列表中
  const typeList = fileTypeMap[typeGroup]
  if (!typeList || typeList.length === 0) {
    // 对于"other"类型，检查是否有任何接受的类型不在已知类型列表中
    if (typeGroup === 'other') {
      const allKnownTypes = [...fileTypeMap.image, ...fileTypeMap.document, ...fileTypeMap.video, ...fileTypeMap.audio]

      return acceptTypesNormalized.some((type) => !allKnownTypes.includes(type))
    }
    return false
  }

  // 检查是否有任何该组的文件类型在接受的类型列表中
  return typeList.some((type) => acceptTypesNormalized.includes(type))
}

/**
 * 根据接受的文件类型和当前选中的文件类型，计算上传接受的文件类型
 */
const computedAcceptTypes = computed(() => {
  // 如果传入了指定的接受文件类型，优先使用
  if (props.acceptFileTypes && props.acceptFileTypes.length > 0) {
    return props.acceptFileTypes.map((type) => (type.startsWith('.') ? type : `.${type}`)).join(',')
  }

  // 根据当前选中的文件类型筛选器
  if (fileTypeFilter.value === 'all' || fileTypeFilter.value === '') {
    return '' // 接受所有类型
  }

  // 使用对应的文件类型列表
  const fileTypes = fileTypeMap[fileTypeFilter.value as keyof typeof fileTypeMap]
  return fileTypes.join(',')
})

/**
 * 获取接受文件类型的描述文本
 */
const acceptTypesDescription = computed(() => {
  if (props.acceptFileTypes && props.acceptFileTypes.length > 0) {
    return props.acceptFileTypes.join('/')
  }

  if (fileTypeFilter.value === 'all' || fileTypeFilter.value === '') {
    return t('ossMediaManager.anyType')
  }

  const typeNames: Record<string, string> = {
    image: t('ossMediaManager.imageType'),
    document: t('ossMediaManager.documentType'),
    video: t('ossMediaManager.videoType'),
    audio: t('ossMediaManager.audioType'),
    other: t('ossMediaManager.otherType')
  }

  return typeNames[fileTypeFilter.value as keyof typeof typeNames]
})

/**
 * 判断文件是否为文本文件
 * @param fileSuffix 文件后缀
 * @returns 是否为文本文件
 */
const isTextFile = (fileSuffix: string): boolean => {
  if (!fileSuffix) return false
  const suffix = fileSuffix.toLowerCase()
  return ['.txt', '.json', '.xml', '.md', '.csv', '.log', '.html', '.css', '.js'].includes(suffix)
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
 * 加载文本文件预览
 * @param file 要预览的文件
 */
const loadTextPreview = async (file: any) => {
  if (!file || !file.url) return

  try {
    // 显示加载中状态
    textPreviewContent.value = t('ossMediaManager.loadingText')

    // 使用fetch获取文本内容
    const response = await fetch(file.url)
    if (!response.ok) {
      throw new Error(t('ossMediaManager.requestFailed', { status: response.status }))
    }

    // 读取文本内容
    const text = await response.text()
    textPreviewContent.value = text
  } catch (error) {
    console.error('Load text file failed:', error)
    textPreviewContent.value = t('ossMediaManager.loadTextFailed')
  }
}

// =========== 方法定义 ===========

// 节点展开处理方法
const handleNodeExpand = (data: any) => {
  if (!expandedKeys.value.includes(data.id)) {
    expandedKeys.value.push(data.id)
  }
}

// 节点折叠处理方法
const handleNodeCollapse = (data: any) => {
  const index = expandedKeys.value.indexOf(data.id)
  if (index !== -1) {
    expandedKeys.value.splice(index, 1)
  }
}

/**
 * 过滤目录树节点
 * @param value 过滤关键字
 * @param data 目录树节点数据
 * @returns 是否显示该节点
 */
const filterNode = (value: string, data: any) => {
  if (!value) return true
  return (
    data.label.toLowerCase().includes(value.toLowerCase()) || (data.directoryName && data.directoryName.toLowerCase().includes(value.toLowerCase()))
  )
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

/**
 * 清空选择
 */
const clearSelection = () => {
  selectionItems.value = []
}

/**
 * 移除选择的文件
 * @param file 要移除的文件
 */
const removeSelection = (file: SysOssVo) => {
  const index = selectionItems.value.findIndex((item) => item.ossId === file.ossId)
  if (index > -1) {
    selectionItems.value.splice(index, 1)
  }
}

/**
 * 添加选择的文件
 * @param file 要添加的文件
 */
const addSelection = (file: SysOssVo) => {
  const exists = selectionItems.value.some((item) => item.ossId === file.ossId)
  if (!exists) {
    selectionItems.value.push(file)
  }
}

// =========== 无限滚动优化实现 ===========

/**
 * 初始化无限滚动观察器
 * 优化: 只创建一次观察器，减少DOM操作
 */
const setupInfiniteScroll = () => {
  // 只创建一次观察者
  if (!loadMoreObserver.value) {
    loadMoreObserver.value = new IntersectionObserver(
      (entries) => {
        const entry = entries[0]
        if (entry && entry.isIntersecting && !isLoadingMore.value && !isLoading.value) {
          // 满足加载条件：元素可见、当前无加载中状态、还有更多可加载数据
          if (files.value.length < pagination.value.total) {
            loadMoreFiles()
          }
        }
      },
      {
        threshold: 0.1,
        root: fileScrollbarRef.value?.$el,
        // 添加rootMargin，提前触发加载，改善用户体验
        rootMargin: '200px 0px'
      }
    )
  }
}

/**
 * 更新观察的目标元素
 * 优化: 分离观察者初始化和观察元素更新的逻辑
 */
const updateObservedElement = async () => {
  if (loadMoreObserver.value) {
    // 先取消之前的观察
    loadMoreObserver.value.disconnect()

    // 使用相同的配置和回调创建新的观察器
    if (fileScrollbarRef.value?.$el) {
      // 创建新的观察器，使用相同的回调逻辑
      loadMoreObserver.value = new IntersectionObserver(
        (entries) => {
          const entry = entries[0]
          if (entry && entry.isIntersecting && !isLoadingMore.value && !isLoading.value) {
            // 满足加载条件：元素可见、当前无加载中状态、还有更多可加载数据
            if (files.value.length < pagination.value.total) {
              loadMoreFiles()
            }
          }
        },
        {
          threshold: 0.1,
          root: fileScrollbarRef.value?.$el,
          rootMargin: '200px 0px'
        }
      )
    }

    // 等待DOM更新完成后再添加新的观察
    await nextTick()
    // 关键修复：只有在有数据且未加载完全时才继续观察
    if (loadMoreTriggerRef.value && files.value.length > 0 && files.value.length < pagination.value.total) {
      loadMoreObserver.value.observe(loadMoreTriggerRef.value)
    }
  }
}

// =========== 数据加载 ===========

/**
 * 只加载目录树结构
 */
const loadDirectories = async () => {
  isLoading.value = true

  // 加载目录结构
  const [err, data] = await getOssDirectoryTreeOptions()
  if (err) {
    console.error('Load directory failed:', err)
    showMsgError(t('ossMediaManager.loadDirectoryFailed'))
    isLoading.value = false
    return
  }

  directories.value = data || []

  // 如果有默认目录ID，选中该目录
  if (props.defaultDirectoryId) {
    const findDir = findDirectoryById(directories.value, props.defaultDirectoryId)
    if (findDir) {
      currentDirectory.value = findDir
      await nextTick()
      directoryTreeRef.value?.setCurrentKey(props.defaultDirectoryId)
    }
  } else if (!currentDirectory.value) {
    // 没有默认目录ID且没有当前选中目录，则默认选择"全部"目录（ID为'10000000000000000'）
    const allDir = findDirectoryById(directories.value, UNCATEGORIZED.value)
    if (allDir) {
      currentDirectory.value = allDir
      await nextTick()
      directoryTreeRef.value?.setCurrentKey(ALL.value)
    }
  } else {
    // 有当前选中目录，恢复选中状态
    await nextTick()
    directoryTreeRef.value?.setCurrentKey(currentDirectory.value!.id)
  }

  isLoading.value = false
}

/**
 * 加载文件列表（分页）
 * @param {boolean} reset 是否重置分页（切换目录或搜索时需要）
 */
const loadFiles = async (reset = false) => {
  // 如果已经在加载中，防止重复加载
  if (isLoading.value && !reset) return

  // 设置加载状态
  isLoading.value = true

  // 添加延迟，防止频繁加载
  if (loadingTimer.value) {
    clearTimeout(loadingTimer.value)
  }

  loadingTimer.value = setTimeout(async () => {
    // 重置分页
    if (reset) {
      pagination.value.pageNum = 1
      files.value = []
    }

    // 如果非重置模式且已经加载过但没有数据，直接返回不再请求
    if (!reset && pagination.value.pageNum > 1 && files.value.length === 0) {
      isLoading.value = false
      isLoadingMore.value = false
      loadingTimer.value = null
      return
    }

    // 处理文件类型筛选条件
    let fileSuffixFilter = ''
    if (fileTypeFilter.value !== 'all' && fileTypeFilter.value !== '') {
      const suffixList = fileTypeMap[fileTypeFilter.value as keyof typeof fileTypeMap]
      if (suffixList && suffixList.length > 0) {
        fileSuffixFilter = suffixList.join(',')
      }
    }

    // 调用API加载文件列表，应用排序设置和文件类型筛选
    const [err, data] = await pageOss({
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
      fileName: query.value.fileName || '',
      originalName: query.value.originalName || '',
      fileSuffix: fileSuffixFilter || query.value.fileSuffix || '',
      directoryId: currentDirectory?.value?.id,
      orderByColumn: sortConfig.value.orderByColumn,
      isAsc: sortConfig.value.isAsc
    })
    if (!err) {
      // 更新文件列表，追加模式
      if (reset) {
        files.value = data.records || []
      } else {
        files.value = [...files.value, ...(data.records || [])]
      }

      // 如果返回的数据为空，设置total为当前已加载数量，防止继续加载
      if (data.records.length === 0 && pagination.value.pageNum > 1) {
        pagination.value.total = files.value.length
      } else {
        pagination.value.total = data.total
      }

      // 检查是否需要过滤不符合acceptFileTypes的文件
      if (props.acceptFileTypes && props.acceptFileTypes.length > 0) {
        const acceptTypesLower = props.acceptFileTypes.map((type) => (type.startsWith('.') ? type.toLowerCase() : `.${type.toLowerCase()}`))

        files.value = files.value.filter((file) => {
          const suffix = file.fileSuffix.toLowerCase()
          return acceptTypesLower.includes(suffix)
        })
      }

      // 更新无限滚动观察的元素
      await updateObservedElement()
    }
    // 清除加载状态
    isLoading.value = false
    isLoadingMore.value = false
    loadingTimer.value = null
  }, 300)
}

/**
 * 加载更多文件
 */
const loadMoreFiles = () => {
  // 关键修复：检查是否已加载全部及是否有数据
  if (files.value.length >= pagination.value.total || isLoadingMore.value || files.value.length === 0) {
    return
  }

  isLoadingMore.value = true
  pagination.value.pageNum += 1
  loadFiles(false)
}

/**
 * 完整加载（首次打开和需要同时刷新目录和文件时使用）
 */
const loadData = async () => {
  isLoading.value = true

  // 加载目录结构（内部已处理目录选择逻辑）
  await loadDirectories()

  // 加载文件列表
  await loadFiles(true)

  // 标记已加载
  hasLoadedFiles.value = true

  isLoading.value = false
}

/**
 * 查询文件
 */
const handleQuery = () => {
  // 搜索时重置分页，并重新加载文件
  loadFiles(true)
}

// 获取当前排序方式的显示文本
const getSortLabel = () => {
  const option = sortOptions.value.find((opt) => opt.command.column === sortConfig.value.orderByColumn && opt.command.order === sortConfig.value.isAsc)
  return option ? option.label : t('ossMediaManager.sortBy')
}

// 处理排序选择
const handleSort = (command: { column: string; order: string }) => {
  if (sortConfig.value.orderByColumn === command.column && sortConfig.value.isAsc === command.order) {
    return // 如果点击当前激活的排序选项，则不做任何操作
  }

  sortConfig.value.orderByColumn = command.column
  sortConfig.value.isAsc = command.order

  // 重新加载文件列表（重置分页并应用新的排序）
  loadFiles(true)
}

// ---------------- 目录操作方法 ----------------

/**
 * 目录树节点点击
 * @param data 选中的目录数据
 */
const handleDirectorySelect = (data: any) => {
  // 如果选择的是同一个目录，不需要重新加载
  if (currentDirectory.value && currentDirectory.value.id === data.id) {
    return
  }

  currentDirectory.value = data

  // 切换目录时清空选择和搜索条件
  clearSelection()
  query.value.fileName = ''
  query.value.originalName = ''
  query.value.fileSuffix = ''

  // 切换目录时，重置分页并重新加载文件列表
  loadFiles(true)
}

/**
 * 添加顶级目录
 */
const handleAddDirectory = () => {
  resetDirectoryForm()
  directoryDialog.value.title = t('ossMediaManager.addDirectory')
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
  directoryDialog.value.title = t('ossMediaManager.addSubDirectory')
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
  directoryDialog.value.title = t('ossMediaManager.renameDirectory')
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
    showMsgWarning(t('ossMediaManager.hasChildDirectory'))
    return
  }
  const [confirmErr] = await showConfirm(t('ossMediaManager.confirmDeleteDirectory', { name: data.label }), t('ossMediaManager.warning'), {
    confirmButtonText: t('ossMediaManager.confirm'),
    cancelButtonText: t('ossMediaManager.cancel'),
    type: 'warning'
  })
  if (confirmErr) {
    // 用户取消操作
    return
  }

  const [deleteErr] = await deleteOssDirectorys(data.id)
  if (deleteErr) {
    return
  }
  showMsgSuccess(t('ossMediaManager.deleteSuccess'))

  // 重新加载目录
  await loadDirectories()

  // 如果删除的是当前选中的目录，切换到"全部"目录
  if (currentDirectory.value && currentDirectory.value.id === data.id) {
    // 查找"全部"目录节点
    const allDir = findDirectoryById(directories.value, ALL.value)
    if (allDir) {
      currentDirectory.value = allDir
      // 设置树组件的当前选中项
      await nextTick()
      directoryTreeRef.value?.setCurrentKey(ALL.value)
    } else {
      // 如果找不到"全部"目录，清空选择
      currentDirectory.value = null
    }
    // 无论如何都重新加载文件列表
    await loadFiles(true)
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

  const [validErr, isValid] = await toValidate(directoryFormRef)

  if (!isValid) return

  directoryDialog.value.loading = true

  if (directoryDialog.value.type === 'add') {
    // 如果 addOssDirectory 已经返回 Result 格式
    const [addError] = await addOssDirectory(directoryForm.value)
    if (addError) {
      directoryDialog.value.loading = false
      return
    }
    showMsgSuccess(t('ossMediaManager.addDirectorySuccess'))
  } else {
    // 如果 updateOssDirectory 已经返回 Result 格式
    const [updateError] = await updateOssDirectory(directoryForm.value)
    if (updateError) {
      directoryDialog.value.loading = false
      return
    }
    showMsgSuccess(t('ossMediaManager.updateDirectorySuccess'))
  }

  directoryDialogVisible.value = false
  directoryDialog.value.loading = false
  await loadDirectories()
}

// ---------------- 文件预览 ----------------

/**
 * 处理文件预览
 * @param file 要预览的文件
 */
const handleFilePreview = (file: SysOssVo) => {
  previewFile.value = file
  // 清空文本预览内容
  textPreviewContent.value = ''
  previewDialogVisible.value = true

  // 自动加载小型文本文件
  if (isTextFile(file.fileSuffix) && file.fileSize && file.fileSize < 100 * 1024) {
    // 小于100KB的文本文件
    loadTextPreview(file)
  }
}

// ---------------- 文件上传 ----------------

/**
 * 打开上传对话框
 */
const handleUpload = () => {
  uploadFileList.value = []
  uploadDialog.value.title = t('ossMediaManager.uploadFile')
  uploadDialog.value.type = 'upload'
  fileToReplace.value = null
  uploadDialogVisible.value = true
}

/**
 * 处理文件替换
 * 针对工具栏的替换按钮，只有单选时才显示
 */
const handleReplaceFile = () => {
  if (selectionItems.value.length !== 1) {
    showMsgWarning(t('ossMediaManager.selectOneFileToReplace'))
    return
  }

  fileToReplace.value = selectionItems.value[0]
  uploadFileList.value = []
  uploadDialog.value.title = t('ossMediaManager.replaceFile')
  uploadDialog.value.type = 'replace'
  uploadDialogVisible.value = true
}

/**
 * 处理单个文件替换（列表视图中的替换按钮）
 * @param file 要替换的文件
 */
const handleFileReplace = (file: SysOssVo) => {
  fileToReplace.value = file
  uploadFileList.value = []
  uploadDialog.value.title = t('ossMediaManager.replaceFile')
  uploadDialog.value.type = 'replace'
  uploadDialogVisible.value = true
}

/**
 * 上传前检查文件
 * @param file 要上传的文件
 * @returns 是否允许上传
 */
const beforeFileUpload = (file: File) => {
  // 文件大小限制：默认10MB
  const maxSize = props.fileSize * 1024 * 1024
  if (file.size > maxSize) {
    showMsgError(t('ossMediaManager.fileSizeLimit', { size: props.fileSize }))
    return false
  }

  // 如果是替换操作，检查文件类型是否一致
  if (uploadDialog.value.type === 'replace' && fileToReplace.value) {
    const newFileSuffix = file.name.substring(file.name.lastIndexOf('.')).toLowerCase()
    const oldFileSuffix = fileToReplace.value.fileSuffix.toLowerCase()

    if (newFileSuffix !== oldFileSuffix) {
      showMsgError(t('ossMediaManager.fileTypeMustMatch', { oldType: oldFileSuffix, newType: newFileSuffix }))
      return false
    }
  }

  // 文件类型限制（如果有设置）
  if (props.acceptFileTypes && props.acceptFileTypes.length > 0) {
    const fileName = file.name
    const fileSuffix = fileName.substring(fileName.lastIndexOf('.')).toLowerCase()
    const isAcceptType = props.acceptFileTypes.some((type) => {
      const acceptType = type.startsWith('.') ? type.toLowerCase() : '.' + type.toLowerCase()
      return fileSuffix === acceptType
    })

    if (!isAcceptType) {
      showMsgError(t('ossMediaManager.fileTypeMustBe', { types: props.acceptFileTypes.join('/') }))
      return false
    }
  }

  return true
}

/**
 * 上传成功回调
 * @param response 服务器响应
 * @param file 上传的文件对象
 */
const handleUploadSuccess = async (response: any, file: any) => {
  if (response.code === 200) {
    // 判断是否是替换操作
    if (uploadDialog.value.type === 'replace' && fileToReplace.value) {
      showMsgSuccess(t('ossMediaManager.fileReplaceSuccess'))
    } else {
      showMsgSuccess(t('ossMediaManager.uploadSuccess'))
    }

    // 关闭上传对话框
    uploadDialogVisible.value = false

    // 刷新文件列表
    await loadFiles(true)
  } else {
    showMsgError(response.msg || t('ossMediaManager.uploadFailed'))
  }
}

/**
 * 上传失败回调
 * @param error 错误信息
 */
const handleUploadError = (error: any) => {
  console.error('Upload failed:', error)
  showMsgError(t('ossMediaManager.uploadFailed'))
}

// ---------------- 文件选择相关方法 ----------------

/**
 * 选择文件
 * @param file 要选择的文件
 */
const selectFile = (file: SysOssVo) => {
  if (props.multiSelect) {
    // 多选模式 - 切换选择状态
    const isSelected = selectionItems.value.some((item) => item.ossId === file.ossId)

    if (isSelected) {
      removeSelection(file)
    } else {
      addSelection(file)
    }
  } else {
    // 单选模式 - 清空后只选择当前文件
    clearSelection()
    addSelection(file)
    // 如果是单选，可以直接确认
    confirmSelection()
  }
}

/**
 * 判断文件是否被选中
 * @param file 要检查的文件
 * @returns 是否已选中
 */
const isFileSelected = (file: SysOssVo): boolean => {
  return selectionItems.value.some((item) => item.ossId === file.ossId)
}

/**
 * 切换文件选择状态
 * @param file 要切换的文件
 * @param event 鼠标事件，用于检测Ctrl键
 */
const toggleFileSelection = (file: SysOssVo, event: MouseEvent) => {
  const isSelected = isFileSelected(file)

  if (props.multiSelect) {
    // 多选模式下的状态切换
    if (isSelected) {
      removeSelection(file)
    } else {
      addSelection(file)
    }
  } else {
    // 单选模式 - 清空后选择当前文件
    clearSelection()
    addSelection(file)
  }
}

// ---------------- 文件移动 ----------------

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
    showMsgWarning(t('ossMediaManager.selectFilesToMove'))
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
    showMsgWarning(t('ossMediaManager.selectTargetDirectory'))
    return
  }

  const targetNode = moveTreeRef.value.getCurrentNode() as SysOssDirectoryTreeVo
  if (!targetNode) {
    showMsgWarning(t('ossMediaManager.selectTargetDirectory'))
    return
  }

  // 批量移动文件
  const ossIds = filesToMove.value.map((file) => file.ossId)
  const [moveError] = await moveOssDirectory(targetNode.id, ossIds)

  if (moveError) {
    return
  }

  showMsgSuccess(t('ossMediaManager.moveSuccess'))
  moveDialogVisible.value = false

  // 清空选择
  clearSelection()

  // 刷新文件列表
  await loadFiles(true)
}

// ---------------- 文件删除 ----------------

/**
 * 删除单个文件
 * @param file 要删除的文件
 */
const handleDelete = async (file: SysOssVo) => {
  const [confirmErr] = await showConfirm(t('ossMediaManager.confirmDeleteFile'), t('ossMediaManager.warning'), {
    confirmButtonText: t('ossMediaManager.confirm'),
    cancelButtonText: t('ossMediaManager.cancel'),
    type: 'warning'
  })
  if (confirmErr) {
    // 用户取消操作
    return
  }

  const [deleteErr] = await deleteOss(file.ossId)
  if (deleteErr) {
    console.error('Delete file failed:', deleteErr)
    showMsgError(t('ossMediaManager.deleteFileFailed'))
    return
  }

  showMsgSuccess(t('ossMediaManager.deleteSuccess'))

  // 如果被删除的文件在选中列表中，移除它
  if (isFileSelected(file)) {
    removeSelection(file)
  }

  // 刷新文件列表
  await loadFiles(true)
}

/**
 * 批量删除文件
 */
const handleBatchDelete = async () => {
  if (selectionItems.value.length === 0) {
    showMsgWarning(t('ossMediaManager.selectFilesToDelete'))
    return
  }

  const [confirmErr] = await showConfirm(t('ossMediaManager.confirmBatchDelete', { count: selectionItems.value.length }), t('ossMediaManager.warning'), {
    confirmButtonText: t('ossMediaManager.confirm'),
    cancelButtonText: t('ossMediaManager.cancel'),
    type: 'warning'
  })
  if (confirmErr) {
    // 用户取消操作
    return
  }

  const fileIds = selectionItems.value.map((file) => file.ossId).join(',')
  const [deleteErr] = await deleteOss(fileIds)

  if (deleteErr) {
    console.error('Batch delete files failed:', deleteErr)
    showMsgError(t('ossMediaManager.batchDeleteFailed'))
    return
  }

  showMsgSuccess(t('ossMediaManager.batchDeleteSuccess'))

  // 清空选择
  clearSelection()

  // 刷新文件列表
  await loadFiles(true)
}

// ---------------- 确认与取消 ----------------

/**
 * 确认选择文件并关闭对话框
 */
const confirmSelection = () => {
  if (props.multiSelect) {
    if (selectionItems.value.length === 0) {
      showMsgWarning(t('ossMediaManager.pleaseSelectFile'))
      return
    }
    emit('select', selectionItems.value)
  } else {
    if (selectionItems.value.length === 0) {
      showMsgWarning(t('ossMediaManager.pleaseSelectFile'))
      return
    }
    // 单选模式下返回第一个选中项
    emit('select', selectionItems.value[0])
  }
  dialogVisible.value = false
}

/**
 * 取消选择并关闭对话框
 */
const cancel = () => {
  emit('cancel')
  dialogVisible.value = false
}

// =========== 监听器和生命周期钩子 ===========

/**
 * 监听目录名称变化，实时过滤部门树
 */
watchEffect(() => directoryTreeRef.value?.filter(directoryName.value), { flush: 'post' })

/**
 * 判断当前文件列表是否与当前选中目录匹配
 * @returns 是否匹配
 */
const isFileListMatchCurrentDirectory = (): boolean => {
  if (!currentDirectory.value || files.value.length === 0) {
    return false
  }

  // 当前选中的是"全部"目录，任何文件列表都算匹配
  if (currentDirectory.value.id === ALL.value) {
    return true
  }

  // 当前选中的是"未分类"目录，检查第一个文件是否为未分类
  if (currentDirectory.value.id === UNCATEGORIZED.value) {
    return files.value.some((file) => file.directoryId === null)
  }

  // 普通目录，检查第一个文件的目录ID是否匹配
  return files.value.some((file) => file.directoryId === currentDirectory.value?.id)
}

/**
 * 监听对话框变化
 * 打开时加载数据，关闭时清空选择但保留数据缓存
 */
watch(
  () => dialogVisible.value,
  async (val) => {
    if (val) {
      // 打开对话框时
      if (!hasLoadedFiles.value || !directories.value || directories.value.length === 0) {
        // 首次加载或目录为空时，完整加载数据
        await loadData()
        // 初始化无限滚动观察器
        setupInfiniteScroll()
      } else {
        // 已经有数据，只需恢复状态
        await nextTick()
        // 如果有当前选中目录，恢复其选中状态
        if (currentDirectory.value) {
          directoryTreeRef.value?.setCurrentKey(currentDirectory.value.id)
        }

        // 如果当前目录已选中但没有文件数据，或者文件数据与当前目录不匹配，则加载文件数据
        if (currentDirectory.value && (files.value.length === 0 || !isFileListMatchCurrentDirectory())) {
          loadFiles(true)
        }

        // 更新无限滚动观察的元素
        await updateObservedElement()
      }
    } else {
      // 关闭对话框时清空选择，但保留数据缓存和当前目录
      clearSelection()

      // 清理观察器资源，但不销毁观察器
      if (loadMoreObserver.value) {
        loadMoreObserver.value.disconnect()
      }
    }
  }
)

/**
 * 监听文件列表变化，更新无限滚动观察器
 */
watch(
  () => files.value.length,
  () => {
    // 更新无限滚动观察的元素
    updateObservedElement()
  }
)

/**
 * 组件挂载时初始化
 */
onMounted(() => {
  // 设置默认文件类型筛选
  fileTypeFilter.value = defaultFileTypeFilter.value

  // 初始化屏幕尺寸状态
  updateScreenSize()
  window.addEventListener('resize', updateScreenSize)
  window.addEventListener('resize', updateObservedElement)

  if (dialogVisible.value) {
    loadData()
    // 初始化无限滚动观察器
    setupInfiniteScroll()
  }
})

/**
 * 组件卸载时清理
 */
onUnmounted(() => {
  // 清理无限滚动观察器
  if (loadMoreObserver.value) {
    loadMoreObserver.value.disconnect()
    loadMoreObserver.value = null
  }

  // 清理其他定时器等资源
  if (loadingTimer.value) {
    clearTimeout(loadingTimer.value)
    loadingTimer.value = null
  }

  // 移除窗口大小变化的监听
  window.removeEventListener('resize', updateScreenSize)
  window.removeEventListener('resize', updateObservedElement)
})
</script>

<style lang="scss" scoped>
/* 媒体管理器容器 */
.media-manager-container {
  min-height: 400px;
  height: 65vh;
  max-height: 90vh;
  width: 100%;
  position: relative;
}

/* 目录容器 */
.directory-container {
  min-height: 100%;
  transition: all 0.3s ease;
}

/* 文件容器 */
.file-container {
  min-height: 100%;
}

/* 目录树样式 */
.directory-tree {
  overflow-y: auto;
  max-height: calc(65vh - 100px);
}

/* 目录节点样式 */
.directory-node {
  min-width: 0; /* 允许flex子项收缩到比固有内容更小 */
}

.directory-label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.directory-actions {
  min-width: 80px;
  display: flex;
  justify-content: flex-end;
}

/* 搜索输入框响应式 */
.search-input {
  width: 250px;
}

/* 文件类型筛选响应式 */
.file-type-filter {
  width: 120px;
}

/* 响应式网格布局 */
.responsive-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 1rem;
  width: 100%;
}

/* 文件名文本溢出处理 */
.file-name-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 150px;
}

/* 加载更多触发区域 */
.load-more-trigger {
  height: 50px;
  margin-top: 1rem;
  visibility: visible !important;
}

/* 当文件数量较少时，确保固定高度 */
.el-scrollbar {
  min-height: 300px;
  overflow: auto !important;
}

/* 侧边栏切换按钮 */
.directory-toggle-btn {
  margin-bottom: 1rem;
}

/* 小屏幕下的目录侧边栏 */
@media (max-width: 768px) {
  .directory-sidebar {
    position: absolute;
    z-index: 10;
    background: white;
    box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
    width: 100%;
    left: 0;
    height: 100%;
  }

  .search-input {
    width: 100%;
    max-width: 250px;
  }

  .file-type-filter {
    width: 100px;
  }

  .responsive-grid {
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
    gap: 0.5rem;
  }
}

@media (max-width: 480px) {
  .responsive-grid {
    grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
    gap: 0.25rem;
  }

  .preview-dialog {
    width: 95% !important;
  }

  /* 移动端更紧凑的目录树 */
  .directory-move-tree {
    max-height: 60vh;
    overflow: auto;
  }
}

/* 图片项悬停效果 */
.responsive-grid .border-primary {
  border-color: var(--el-color-primary);
  transform: translateY(-2px);
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
</style>
