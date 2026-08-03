<!-- 代码预览 -->
<template>
  <div class="code-preview">
    <!-- 使用可拖拽面板组件 -->
    <AResizablePanels v-model:leftWidth="leftPanelWidth" :min-width="200" :max-width="600" :gutter="16">
      <!-- 左侧面板：文件目录树 -->
      <template #left>
        <el-card shadow="never" class="tree-card">
          <template #header>
            <div class="panel-header">
              <span>{{ t('File Directory', '文件目录') }}</span>
              <el-tooltip :content="t('Expand/Collapse All', '展开/折叠全部')" placement="top">
                <el-button text :icon="isExpandAll ? 'Fold' : 'Expand'" @click="toggleExpandAll" />
              </el-tooltip>
            </div>
          </template>
          <el-scrollbar :height="scrollbarHeight">
            <el-tree
              ref="treeRef"
              :data="treeData"
              :props="{ label: 'label', children: 'children' }"
              :expand-on-click-node="false"
              :default-expand-all="isExpandAll"
              highlight-current
              node-key="fullPath"
              @node-click="handleNodeClick"
            >
              <template #default="{ node, data }">
                <span class="tree-node">
                  <Icon :name="getFileIcon(data)" class="node-icon" />
                  <span class="node-label">{{ node.label }}</span>
                </span>
              </template>
            </el-tree>
          </el-scrollbar>
        </el-card>
      </template>

      <!-- 右侧面板：代码显示 -->
      <template #right>
        <el-card shadow="never" class="code-card">
          <el-tabs v-if="fileList.length > 0" v-model="activeTab" class="code-tabs">
            <el-tab-pane v-for="file in fileList" :key="file.fullPath" :name="file.fullPath">
              <template #label>
                <span class="tab-label">
                  <Icon :name="getFileIcon(file)" class="tab-icon" />
                  {{ file.label }}
                </span>
              </template>
              <div class="code-pane">
                <div class="code-header">
                  <el-link @click="handleCopy(file)" :underline="false" icon="DocumentCopy" type="primary"> {{ t('Copy', '复制') }} </el-link>
                </div>
                <el-scrollbar :height="scrollbarHeight">
                  <pre class="code-content"><code class="hljs" v-html="highlightCode(file)"></code></pre>
                </el-scrollbar>
              </div>
            </el-tab-pane>
          </el-tabs>
          <el-empty v-else :description="t('Please select a file to view', '请选择文件查看')" :image-size="120" />
        </el-card>
      </template>
    </AResizablePanels>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import type { ElTree } from 'element-plus'
import { copy } from '@/utils/function'
import hljs from 'highlight.js'
import 'highlight.js/styles/atom-one-light.css' // 使用 Atom One Light 主题（较为中性）
const { t } = useI18n()

/**
 * 树节点接口
 */
interface TreeNode {
  label: string // 节点显示名称
  fullPath: string // 完整文件路径
  isFile: boolean // 是否是文件节点
  content?: string // 文件内容(仅文件节点有)
  children?: TreeNode[] // 子节点
}

// ============ Props & Emits ============
const props = withDefaults(
  defineProps<{
    /** 文件内容映射表 { "文件路径": "文件内容" } */
    fileData: Record<string, string>
    /** 滚动区域高度 */
    height?: string
  }>(),
  {
    height: 'calc(80vh - 125px)'
  }
)

// ============ 响应式数据 ============
const treeRef = ref<InstanceType<typeof ElTree>>()
const treeData = ref<TreeNode[]>([])
const selectedFile = ref<TreeNode | null>(null)
const isExpandAll = ref(true)
const leftPanelWidth = ref(400) // 左侧面板宽度
const fileList = ref<TreeNode[]>([]) // 所有文件列表（扁平化）
const activeTab = ref<string>('') // 当前激活的 Tab

// 计算滚动条高度
const scrollbarHeight = computed(() => props.height)

// ============ 数据转换函数 ============
/**
 * 将平铺的文件路径转为树形结构
 */
function convertToTree(fileMap: Record<string, string>): TreeNode[] {
  const root: TreeNode = {
    label: 'root',
    fullPath: '',
    isFile: false,
    children: []
  }

  // 遍历所有文件路径
  Object.entries(fileMap).forEach(([path, content]) => {
    // 统一处理路径分隔符 (Windows \ 转为 /)
    const normalizedPath = path.replace(/\\/g, '/')
    const parts = normalizedPath.split('/').filter(Boolean)

    let currentNode = root

    // 逐层构建树形结构
    parts.forEach((part, index) => {
      const isLastPart = index === parts.length - 1
      const fullPath = parts.slice(0, index + 1).join('/')

      // 查找是否已存在该节点
      let existingNode = currentNode.children?.find((child) => child.label === part)

      if (!existingNode) {
        // 创建新节点
        const newNode: TreeNode = {
          label: part,
          fullPath: fullPath,
          isFile: isLastPart,
          children: isLastPart ? undefined : []
        }

        // 如果是文件节点,添加内容
        if (isLastPart) {
          newNode.content = content
        }

        if (!currentNode.children) {
          currentNode.children = []
        }
        currentNode.children.push(newNode)
        existingNode = newNode
      }

      currentNode = existingNode
    })
  })

  return root.children || []
}

/**
 * 递归查找第一个文件节点
 */
function findFirstFile(nodes: TreeNode[]): TreeNode | null {
  for (const node of nodes) {
    if (node.isFile) {
      return node
    }
    if (node.children && node.children.length > 0) {
      const found = findFirstFile(node.children)
      if (found) return found
    }
  }
  return null
}

/**
 * 递归收集所有文件节点（扁平化）
 */
function collectAllFiles(nodes: TreeNode[]): TreeNode[] {
  const files: TreeNode[] = []
  for (const node of nodes) {
    if (node.isFile) {
      files.push(node)
    }
    if (node.children && node.children.length > 0) {
      files.push(...collectAllFiles(node.children))
    }
  }
  return files
}

/**
 * 根据文件类型获取图标
 */
function getFileIcon(node: TreeNode): string {
  if (!node.isFile) {
    return 'Folder'
  }

  const fileName = node.label.toLowerCase()

  // Java 文件
  if (fileName.endsWith('.java')) return 'Document'
  // Vue 文件
  if (fileName.endsWith('.vue')) return 'Document'
  // TypeScript 文件
  if (fileName.endsWith('.ts') || fileName.endsWith('.tsx')) return 'Document'
  // JavaScript 文件
  if (fileName.endsWith('.js') || fileName.endsWith('.jsx')) return 'Document'
  // SQL 文件
  if (fileName.endsWith('.sql')) return 'Document'
  // XML 文件
  if (fileName.endsWith('.xml')) return 'Document'
  // 默认文件图标
  return 'Document'
}

/**
 * 根据文件扩展名获取语言类型
 */
function getLanguage(fileName: string): string {
  const ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase()
  const languageMap: Record<string, string> = {
    java: 'java',
    vue: 'xml',
    ts: 'typescript',
    tsx: 'typescript',
    js: 'javascript',
    jsx: 'javascript',
    sql: 'sql',
    xml: 'xml',
    html: 'html',
    css: 'css',
    scss: 'scss',
    json: 'json',
    yaml: 'yaml',
    yml: 'yaml',
    md: 'markdown'
  }
  return languageMap[ext] || 'plaintext'
}

/**
 * 对代码进行语法高亮
 */
function highlightCode(file: TreeNode): string {
  if (!file.content) return ''

  const language = getLanguage(file.label)

  try {
    if (language === 'plaintext') {
      return hljs.highlightAuto(file.content).value
    }
    return hljs.highlight(file.content, { language }).value
  } catch (error) {
    console.error('Code highlight failed:', error)
    // 如果高亮失败，返回转义后的原始内容
    return file.content.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
  }
}

// ============ 交互事件 ============
/**
 * 树节点点击事件
 */
function handleNodeClick(node: TreeNode) {
  if (node.isFile) {
    selectedFile.value = node
  }
}

/**
 * 切换全部展开/折叠
 */
function toggleExpandAll() {
  isExpandAll.value = !isExpandAll.value
  if (treeRef.value) {
    // 获取所有节点
    const nodes = treeRef.value.store.nodesMap
    Object.values(nodes).forEach((node: any) => {
      node.expanded = isExpandAll.value
    })
  }
}

/**
 * 复制代码
 */
function handleCopy(file: TreeNode) {
  if (file?.content) {
    copy(file.content, t('Copy Success', '复制成功'))
  }
}

// ============ 监听数据变化 ============
watch(
  () => props.fileData,
  (newData) => {
    if (newData && Object.keys(newData).length > 0) {
      treeData.value = convertToTree(newData)
      // 收集所有文件
      fileList.value = collectAllFiles(treeData.value)
      // 默认选中第一个文件
      nextTick(() => {
        const firstFile = findFirstFile(treeData.value)
        if (firstFile) {
          selectedFile.value = firstFile
          activeTab.value = firstFile.fullPath
          // 高亮当前节点
          treeRef.value?.setCurrentKey(firstFile.fullPath)
        }
      })
    } else {
      treeData.value = []
      fileList.value = []
      selectedFile.value = null
      activeTab.value = ''
    }
  },
  { immediate: true, deep: true }
)

// 监听树节点点击，同步更新 activeTab
watch(selectedFile, (newFile) => {
  if (newFile) {
    activeTab.value = newFile.fullPath
  }
})

// 监听 Tab 切换，同步更新选中的文件
watch(activeTab, (newPath) => {
  if (newPath) {
    const file = fileList.value.find((f) => f.fullPath === newPath)
    if (file) {
      selectedFile.value = file
      // 高亮对应的树节点
      treeRef.value?.setCurrentKey(newPath)
    }
  }
})
</script>

<style scoped lang="scss">
.code-preview {
  width: 100%;
  height: 100%;

  .tree-card {
    height: 100%;
    display: flex;
    flex-direction: column;
    border-radius: 8px;

    :deep(.el-card__header) {
      padding: 8px 12px;
      border-bottom: 1px solid var(--el-border-color);
    }

    :deep(.el-card__body) {
      flex: 1;
      padding: 0;
      overflow: hidden;
    }
  }

  .code-card {
    height: 100%;
    border-radius: 8px;

    :deep(.el-card__body) {
      padding: 0;
      height: 100%;
    }
  }

  .panel-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-weight: 600;
    font-size: 14px;
    color: var(--el-text-color-primary);
  }

  :deep(.el-tree) {
    padding: 8px 12px;
    background: transparent;

    .el-tree-node {
      .el-tree-node__content {
        height: 32px;
        padding-right: 8px;

        &:hover {
          background-color: var(--el-fill-color-light);
        }

        .tree-node {
          display: flex;
          align-items: center;
          gap: 6px;
          flex: 1;
          overflow: hidden;

          .node-icon {
            font-size: 16px;
            color: var(--el-color-primary);
            flex-shrink: 0;
          }

          .node-label {
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
        }
      }

      &.is-current > .el-tree-node__content {
        background-color: var(--el-color-primary-light-9);
        color: var(--el-color-primary);
        font-weight: 600;
      }
    }
  }

  // Tab 栏样式 - 底部激活线样式
  .code-tabs {
    height: 100%;

    .tab-label {
      display: flex;
      align-items: center;
      gap: 6px;

      .tab-icon {
        font-size: 14px;
      }
    }

    // 代码面板
    .code-pane {
      height: 100%;
      position: relative;
    }

    // 复制按钮区域
    .code-header {
      position: absolute;
      top: 8px;
      right: 16px;
      z-index: 10;
    }
  }

  // 代码内容样式
  .code-content {
    margin: 0;
    padding: 16px;
    font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
    font-size: 13px;
    line-height: 1.6;
    background-color: var(--el-fill-color-lighter); // 自适应背景色
    border-radius: 4px;

    code.hljs {
      font-family: inherit;
      background-color: transparent !important; // 强制透明，使用父元素的自适应背景色
      padding: 0;
      display: block;
      overflow-x: auto;
    }
  }
}
</style>
