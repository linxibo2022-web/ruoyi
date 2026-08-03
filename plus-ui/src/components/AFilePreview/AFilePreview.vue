<!-- 文件预览 -->
<template>
  <div class="a-file-preview">
    <!-- 图片预览 -->
    <div v-if="fileType === 'image'" class="preview-image">
      <el-image
        :src="previewUrl"
        fit="contain"
        :preview-src-list="[previewUrl]"
        :initial-index="0"
        preview-teleported
        class="max-w-full max-h-full"
        :style="{ maxHeight: actualHeight }"
      />
    </div>

    <!-- 视频预览 -->
    <div v-else-if="fileType === 'video'" class="preview-video">
      <video :key="previewUrl" controls class="max-w-full max-h-full" :style="{ maxHeight: actualHeight }">
        <source :src="previewUrl" :type="mimeType" />
        {{ t('filePreview.videoNotSupported') }}
      </video>
    </div>

    <!-- 音频预览 -->
    <div v-else-if="fileType === 'audio'" class="preview-audio">
      <div class="audio-left">
        <el-icon :size="56" class="audio-icon">
          <Headset />
        </el-icon>
      </div>
      <div class="audio-right">
        <div class="audio-info">
          <span class="audio-name">{{ fileName }}</span>
          <span class="audio-meta">
            <span v-if="audioDuration" class="audio-duration">{{ formatDuration(audioDuration) }}</span>
            <span v-if="fileSize" class="audio-size">{{ formatFileSize(fileSize) }}</span>
          </span>
        </div>
        <audio ref="audioRef" :key="previewUrl" controls class="audio-player" @loadedmetadata="onAudioLoaded">
          <source :src="previewUrl" :type="mimeType" />
          {{ t('filePreview.audioNotSupported') }}
        </audio>
      </div>
    </div>

    <!-- PDF 预览 -->
    <div v-else-if="fileType === 'pdf'" class="preview-pdf">
      <iframe :src="previewUrl" class="w-full h-full border-0" :style="{ height: actualHeight }" />
    </div>

    <!-- Office 文档预览 (Word, Excel, PPT) -->
    <div v-else-if="fileType === 'office'" class="preview-office" :style="{ height: actualHeight }">
      <!-- 本地/内网环境提示 -->
      <div v-if="isLocalUrl" class="office-local-tip">
        <div class="tip-icon">
          <el-icon :size="60">
            <Document />
          </el-icon>
        </div>
        <div class="tip-title">{{ fileName || t('filePreview.officeDocument') }}</div>
        <div class="tip-size" v-if="fileSize">{{ formatFileSize(fileSize) }}</div>
        <el-alert type="warning" :closable="false" show-icon class="mt-4 max-w-md">
          <template #title>
            <span>{{ t('filePreview.officePreviewRequiresPublicUrl') }}</span>
          </template>
          <template #default>
            <div class="text-xs mt-1">
              {{ t('filePreview.localNetworkTip') }}
              <br />{{ t('filePreview.downloadToOpen') }}
            </div>
          </template>
        </el-alert>
        <el-button v-if="showDownload" type="primary" class="mt-4" @click="handleDownload">
          <el-icon>
            <Download />
          </el-icon>
          {{ t('filePreview.downloadFile') }}
        </el-button>
      </div>
      <!-- 公网环境使用 Microsoft Office Viewer -->
      <iframe v-else :src="officeViewerUrl" class="w-full h-full border-0" :style="{ height: actualHeight }" />
    </div>

    <!-- 代码/文本预览 -->
    <div v-else-if="fileType === 'text'" class="preview-text">
      <div v-if="textLoading" class="loading-container">
        <el-icon class="is-loading" :size="32">
          <Loading />
        </el-icon>
        <span class="ml-2">{{ t('filePreview.loading') }}</span>
      </div>
      <el-scrollbar v-else :height="actualHeight">
        <pre class="code-content"><code class="hljs" v-html="highlightedCode"></code></pre>
      </el-scrollbar>
    </div>

    <!-- 不支持预览的文件 -->
    <div v-else class="preview-fallback">
      <div class="fallback-icon">
        <el-icon :size="80">
          <FolderOpened v-if="fileType === 'archive'" />
          <Document v-else />
        </el-icon>
      </div>
      <div class="fallback-name">{{ fileName }}</div>
      <div class="fallback-size" v-if="fileSize">{{ formatFileSize(fileSize) }}</div>
      <div class="fallback-tip">{{ t('filePreview.previewNotSupported') }}</div>
      <el-button v-if="showDownload" type="primary" class="mt-4" @click="handleDownload">
        <el-icon>
          <Download />
        </el-icon>
        {{ t('filePreview.downloadFile') }}
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { Headset, Document, Download, Loading, FolderOpened } from '@element-plus/icons-vue'
import { formatFileSize } from '@/utils/format'
import { useI18n } from '@/composables/useI18n'
import hljs from 'highlight.js'
import 'highlight.js/styles/atom-one-light.css'

const { t } = useI18n()

/**
 * AFilePreview - 通用文件预览组件
 *
 * 支持的文件类型:
 * - 图片: png, jpg, jpeg, gif, webp, bmp, svg
 * - 视频: mp4, avi, mov, wmv, flv, mkv, webm
 * - 音频: mp3, wav, ogg, flac, aac
 * - PDF: pdf
 * - Office: doc, docx, xls, xlsx, ppt, pptx (通过 Microsoft Office Online Viewer)
 * - 代码/文本: txt, js, ts, vue, java, py, go, sql, xml, json, yaml, yml, md, css, scss, html
 * - 压缩包: zip, rar, 7z, tar, gz (仅显示下载)
 */

// ============ Props ============
const props = withDefaults(
  defineProps<{
    /** 文件 URL */
    url: string
    /** 文件名（含扩展名） */
    fileName?: string
    /** 文件后缀（如 .pdf，可选，不传则从 fileName/url 自动解析） */
    fileSuffix?: string
    /** 文件大小（字节） */
    fileSize?: number
    /** 预览区域高度 */
    height?: string
    /** 是否显示下载按钮（不支持预览时） */
    showDownload?: boolean
    /** 是否全屏模式（由父组件传入） */
    isFullscreen?: boolean
  }>(),
  {
    fileName: '',
    fileSuffix: '',
    fileSize: 0,
    height: '70vh',
    showDownload: true,
    isFullscreen: false
  }
)

// ============ Emits ============
const emit = defineEmits<{
  (e: 'download'): void
}>()

// ============ 响应式状态 ============
const textContent = ref('')
const textLoading = ref(false)
const audioRef = ref<HTMLAudioElement | null>(null)
const audioDuration = ref(0)

// ============ 文件类型常量 ============
const IMAGE_EXTENSIONS = ['.png', '.jpg', '.jpeg', '.gif', '.webp', '.bmp', '.svg', '.ico']
const VIDEO_EXTENSIONS = ['.mp4', '.avi', '.mov', '.wmv', '.flv', '.mkv', '.webm']
const AUDIO_EXTENSIONS = ['.mp3', '.wav', '.ogg', '.flac', '.aac', '.wma']
const OFFICE_EXTENSIONS = ['.doc', '.docx', '.xls', '.xlsx', '.ppt', '.pptx']
const PDF_EXTENSIONS = ['.pdf']
const TEXT_EXTENSIONS = [
  '.txt',
  '.js',
  '.ts',
  '.tsx',
  '.jsx',
  '.vue',
  '.java',
  '.py',
  '.go',
  '.sql',
  '.xml',
  '.json',
  '.yaml',
  '.yml',
  '.md',
  '.css',
  '.scss',
  '.less',
  '.html',
  '.htm',
  '.sh',
  '.bat',
  '.c',
  '.cpp',
  '.h',
  '.cs',
  '.rb',
  '.php',
  '.swift',
  '.kt',
  '.rs',
  '.lua',
  '.r',
  '.scala',
  '.groovy',
  '.ini',
  '.conf',
  '.properties',
  '.log'
]
const ARCHIVE_EXTENSIONS = ['.zip', '.rar', '.7z', '.tar', '.gz', '.bz2', '.xz']

// ============ 计算属性 ============

/** 实际使用的高度（全屏时使用87vh，否则使用props传入的height） */
const actualHeight = computed(() => {
  return props.isFullscreen ? '87vh' : props.height
})

/** 解析后的文件后缀 */
const suffix = computed(() => {
  if (props.fileSuffix) {
    const s = props.fileSuffix.toLowerCase()
    return s.startsWith('.') ? s : `.${s}`
  }
  // 从 fileName 解析
  if (props.fileName) {
    const match = props.fileName.match(/\.[^.]+$/)
    if (match) return match[0].toLowerCase()
  }
  // 从 URL 解析
  if (props.url) {
    // 去除查询参数
    const urlPath = props.url.split('?')[0]
    const match = urlPath.match(/\.[^.]+$/)
    if (match) return match[0].toLowerCase()
  }
  return ''
})

/** 文件类型 */
const fileType = computed(() => {
  const s = suffix.value
  if (!s) return 'unknown'

  if (IMAGE_EXTENSIONS.includes(s)) return 'image'
  if (VIDEO_EXTENSIONS.includes(s)) return 'video'
  if (AUDIO_EXTENSIONS.includes(s)) return 'audio'
  if (PDF_EXTENSIONS.includes(s)) return 'pdf'
  if (OFFICE_EXTENSIONS.includes(s)) return 'office'
  if (TEXT_EXTENSIONS.includes(s)) return 'text'
  if (ARCHIVE_EXTENSIONS.includes(s)) return 'archive'

  return 'unknown'
})

/** 检测是否为本地/内网 URL（Microsoft Office Viewer 无法访问） */
const isLocalUrl = computed(() => {
  if (!props.url) return true
  try {
    const url = new URL(props.url)
    const hostname = url.hostname.toLowerCase()
    // 本地地址
    if (hostname === 'localhost' || hostname === '127.0.0.1') return true
    // 内网地址段
    if (hostname.startsWith('192.168.')) return true
    if (hostname.startsWith('10.')) return true
    if (hostname.match(/^172\.(1[6-9]|2[0-9]|3[0-1])\./)) return true
    // 本地域名
    if (hostname.endsWith('.local') || hostname.endsWith('.localhost')) return true
    return false
  } catch {
    return true // URL 解析失败也视为本地
  }
})

/** 预览 URL（可能需要添加缓存破坏参数） */
const previewUrl = computed(() => {
  return props.url
})

/** Office 在线预览 URL */
const officeViewerUrl = computed(() => {
  if (fileType.value !== 'office') return ''
  // 使用 Microsoft Office Online Viewer
  const encodedUrl = encodeURIComponent(props.url)
  return `https://view.officeapps.live.com/op/embed.aspx?src=${encodedUrl}`
})

/** MIME 类型 */
const mimeType = computed(() => {
  const s = suffix.value

  // 视频类型
  const videoMimes: Record<string, string> = {
    '.mp4': 'video/mp4',
    '.webm': 'video/webm',
    '.avi': 'video/x-msvideo',
    '.mov': 'video/quicktime',
    '.wmv': 'video/x-ms-wmv',
    '.flv': 'video/x-flv',
    '.mkv': 'video/x-matroska'
  }

  // 音频类型
  const audioMimes: Record<string, string> = {
    '.mp3': 'audio/mpeg',
    '.wav': 'audio/wav',
    '.ogg': 'audio/ogg',
    '.flac': 'audio/flac',
    '.aac': 'audio/aac',
    '.wma': 'audio/x-ms-wma'
  }

  return videoMimes[s] || audioMimes[s] || ''
})

/** 代码高亮后的内容 */
const highlightedCode = computed(() => {
  if (!textContent.value) return ''

  const language = getLanguageFromSuffix(suffix.value)

  try {
    if (language === 'plaintext') {
      return hljs.highlightAuto(textContent.value).value
    }
    return hljs.highlight(textContent.value, { language }).value
  } catch (e) {
    // 高亮失败，返回转义后的原始内容
    return textContent.value.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
  }
})

// ============ 辅助函数 ============

/**
 * 根据后缀获取语言类型
 */
function getLanguageFromSuffix(suffix: string): string {
  const languageMap: Record<string, string> = {
    '.js': 'javascript',
    '.ts': 'typescript',
    '.tsx': 'typescript',
    '.jsx': 'javascript',
    '.vue': 'xml',
    '.java': 'java',
    '.py': 'python',
    '.go': 'go',
    '.sql': 'sql',
    '.xml': 'xml',
    '.json': 'json',
    '.yaml': 'yaml',
    '.yml': 'yaml',
    '.md': 'markdown',
    '.css': 'css',
    '.scss': 'scss',
    '.less': 'less',
    '.html': 'html',
    '.htm': 'html',
    '.sh': 'bash',
    '.bat': 'batch',
    '.c': 'c',
    '.cpp': 'cpp',
    '.h': 'c',
    '.cs': 'csharp',
    '.rb': 'ruby',
    '.php': 'php',
    '.swift': 'swift',
    '.kt': 'kotlin',
    '.rs': 'rust',
    '.lua': 'lua',
    '.r': 'r',
    '.scala': 'scala',
    '.groovy': 'groovy',
    '.ini': 'ini',
    '.conf': 'ini',
    '.properties': 'properties',
    '.log': 'plaintext',
    '.txt': 'plaintext'
  }
  return languageMap[suffix] || 'plaintext'
}

/**
 * 加载文本内容
 */
async function loadTextContent() {
  if (fileType.value !== 'text' || !props.url) return

  textLoading.value = true
  try {
    const response = await fetch(props.url)
    if (response.ok) {
      textContent.value = await response.text()
    } else {
      textContent.value = t('filePreview.loadFailed')
    }
  } catch (e) {
    textContent.value = t('filePreview.loadFailed') + ': ' + (e as Error).message
  } finally {
    textLoading.value = false
  }
}

/**
 * 下载文件
 */
function handleDownload() {
  emit('download')

  // 创建链接并模拟点击下载
  const a = document.createElement('a')
  a.href = props.url
  a.download = props.fileName || 'download'
  a.target = '_blank'
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
}

/**
 * 音频加载完成后获取时长
 */
function onAudioLoaded() {
  if (audioRef.value && audioRef.value.duration) {
    audioDuration.value = audioRef.value.duration
  }
}

/**
 * 格式化时长（秒 -> mm:ss 或 hh:mm:ss）
 */
function formatDuration(seconds: number): string {
  if (!seconds || !isFinite(seconds)) return ''

  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = Math.floor(seconds % 60)

  if (h > 0) {
    return `${h}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
  }
  return `${m}:${s.toString().padStart(2, '0')}`
}

// ============ 生命周期 ============

// 监听文件类型变化，加载文本内容
watch(
  () => [fileType.value, props.url],
  () => {
    if (fileType.value === 'text') {
      loadTextContent()
    }
  },
  { immediate: true }
)
</script>

<style scoped lang="scss">
.a-file-preview {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  .preview-image,
  .preview-video,
  .preview-pdf,
  .preview-office {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .preview-audio {
    display: flex;
    align-items: center;
    gap: 20px;
    padding: 24px;
    background: var(--el-fill-color-lighter);
    border-radius: 12px;
    max-width: 520px;
    width: 100%;

    .audio-left {
      flex-shrink: 0;

      .audio-icon {
        color: var(--el-color-primary);
      }
    }

    .audio-right {
      flex: 1;
      min-width: 0;
      display: flex;
      flex-direction: column;
      gap: 12px;
    }

    .audio-info {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
    }

    .audio-name {
      font-size: 14px;
      font-weight: 500;
      color: var(--el-text-color-primary);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      flex: 1;
      min-width: 0;
    }

    .audio-meta {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-shrink: 0;
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }

    .audio-duration {
      font-family: 'Consolas', 'Monaco', monospace;
    }

    .audio-player {
      width: 100%;
      height: 40px;
    }
  }

  /* Office 本地/内网环境提示样式 */
  .office-local-tip {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
    text-align: center;
    padding: 32px;

    .tip-icon {
      color: var(--el-text-color-secondary);
    }

    .tip-title {
      font-size: 16px;
      font-weight: 500;
      color: var(--el-text-color-primary);
      max-width: 400px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .tip-size {
      font-size: 14px;
      color: var(--el-text-color-secondary);
    }
  }

  .preview-text {
    width: 100%;
    height: 100%;

    .loading-container {
      display: flex;
      align-items: center;
      justify-content: center;
      height: 200px;
      color: var(--el-text-color-secondary);
    }

    .code-content {
      margin: 0;
      padding: 16px;
      font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
      font-size: 13px;
      line-height: 1.6;
      background-color: var(--el-fill-color-lighter);
      border-radius: 4px;

      code.hljs {
        font-family: inherit;
        background-color: transparent !important;
        padding: 0;
        display: block;
        overflow-x: auto;
      }
    }
  }

  .preview-fallback {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
    text-align: center;

    .fallback-icon {
      color: var(--el-text-color-secondary);
    }

    .fallback-name {
      font-size: 16px;
      color: var(--el-text-color-primary);
      max-width: 400px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .fallback-size {
      font-size: 14px;
      color: var(--el-text-color-secondary);
    }

    .fallback-tip {
      font-size: 14px;
      color: var(--el-text-color-placeholder);
    }
  }
}
</style>
