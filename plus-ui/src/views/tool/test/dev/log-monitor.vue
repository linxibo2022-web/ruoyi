<!-- 日志监控 -->
<template>
  <div class="log-monitor">
    <!-- 头部 -->
    <div class="header">
      <div class="title">
        <el-icon :size="20">
          <Monitor />
        </el-icon>
        <span>移动端实时日志监控</span>
        <el-tag :type="isConnected ? 'success' : 'danger'" size="small">
          {{ isConnected ? 'WebSocket已连接' : 'WebSocket未连接' }}
        </el-tag>
      </div>
      <div class="actions">
        <el-button size="small" @click="clearLogs">清空日志</el-button>
        <el-button size="small" @click="exportLogs">导出日志</el-button>
      </div>
    </div>

    <!-- 过滤器 -->
    <div class="filters">
      <el-radio-group v-model="filterLevel" size="small">
        <el-radio-button value="all">全部</el-radio-button>
        <el-radio-button value="log">LOG</el-radio-button>
        <el-radio-button value="info">INFO</el-radio-button>
        <el-radio-button value="warn">WARN</el-radio-button>
        <el-radio-button value="error">ERROR</el-radio-button>
      </el-radio-group>
      <el-input v-model="searchText" placeholder="搜索日志内容..." clearable size="small" style="width: 300px">
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <!-- 日志列表 -->
    <div class="log-list" ref="logListRef">
      <div v-for="(log, index) in filteredLogs" :key="index" class="log-item" :class="`log-${log.level}`">
        <div class="log-header">
          <div class="log-meta">
            <el-tag :type="getLevelType(log.level)" size="small">
              {{ log.level.toUpperCase() }}
            </el-tag>
            <span class="log-time">{{ formatTime(log.timestamp) }}</span>
            <span v-if="log.path" class="log-path">{{ log.path }}</span>
          </div>
        </div>
        <div class="log-body">
          <pre>{{ log.message }}</pre>
        </div>
      </div>

      <el-empty v-if="filteredLogs.length === 0" description="暂无日志数据" />
    </div>

    <!-- 底部统计 -->
    <div class="footer">
      <span>总计: {{ logs.length }} 条</span>
      <span>过滤后: {{ filteredLogs.length }} 条</span>
      <span>最后更新: {{ lastUpdateTime }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted, onBeforeUnmount } from 'vue'
import { Monitor, Search, Location } from '@element-plus/icons-vue'
import { formatDate } from '@/utils/date'
import { ElMessage } from 'element-plus'
import { webSocket, MessageHandler, WSMessage } from '@/composables/useWS'

/** 日志项接口 */
interface LogItem {
  level: 'log' | 'info' | 'warn' | 'error'
  message: string
  timestamp: number
  path: string
  userId?: string
}

/** 日志列表 */
const logs = ref<LogItem[]>([])

/** 过滤级别 */
const filterLevel = ref('all')

/** 搜索文本 */
const searchText = ref('')

/** WebSocket连接状态 */
const isConnected = ref(false)

/** 最后更新时间 */
const lastUpdateTime = ref('')

/** 日志列表容器 */
const logListRef = ref<HTMLElement>()

/** 更新连接状态 */
const updateConnectionStatus = () => {
  isConnected.value = webSocket.status === 'OPEN'
}

// 定时检查连接状态
let statusCheckTimer: number | null = null

/** 过滤后的日志 */
const filteredLogs = computed(() => {
  let result = logs.value

  // 按级别过滤
  if (filterLevel.value !== 'all') {
    result = result.filter((log) => log.level === filterLevel.value)
  }

  // 按搜索文本过滤
  if (searchText.value) {
    result = result.filter((log) => log.message.toLowerCase().includes(searchText.value.toLowerCase()))
  }

  return result
})

/** 获取级别对应的标签类型 */
const getLevelType = (level: string) => {
  const typeMap: Record<string, any> = {
    log: 'primary',
    info: 'success',
    warn: 'warning',
    error: 'danger'
  }
  return typeMap[level] || ''
}

/** 格式化时间 */
const formatTime = (timestamp: number) => {
  return formatDate(new Date(timestamp), 'HH:mm:ss.SSS')
}

/** 清空日志 */
const clearLogs = () => {
  logs.value = []
  ElMessage.success('日志已清空')
}

/** 导出日志 */
const exportLogs = () => {
  if (logs.value.length === 0) {
    ElMessage.warning('暂无日志可导出')
    return
  }

  const content = logs.value
    .map((log) => `[${formatTime(log.timestamp)}] [${log.level.toUpperCase()}] ${log.path ? `[${log.path}] ` : ''}${log.message}`)
    .join('\n')

  const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `logs-${formatDate(new Date(), 'YYYY-MM-DD-HHmmss')}.txt`
  a.click()
  URL.revokeObjectURL(url)

  ElMessage.success('日志已导出')
}

/** 滚动到底部 */
const scrollToBottom = () => {
  nextTick(() => {
    if (logListRef.value) {
      logListRef.value.scrollTop = logListRef.value.scrollHeight
    }
  })
}

/** 开发日志消息处理器 */
class DevLogHandler implements MessageHandler {
  handle(message: WSMessage): boolean {
    // 判断是否是开发日志消息
    if (message.type === 'devLog') {
      const data = message.data
      if (data && data.logs) {
        // 批量添加日志
        logs.value.push(...data.logs)

        // 限制日志数量（最多保留5000条）
        if (logs.value.length > 5000) {
          logs.value = logs.value.slice(-5000)
        }

        // 更新时间
        lastUpdateTime.value = formatDate(new Date(), 'HH:mm:ss')

        // 滚动到底部
        scrollToBottom()

        return false // 阻止传播
      }
    }

    return true // 继续传播
  }
}

const devLogHandler = new DevLogHandler()

onMounted(() => {
  // 添加日志消息处理器
  webSocket.addMessageHandler(devLogHandler)

  // 立即更新一次连接状态
  updateConnectionStatus()

  // 每秒检查一次连接状态
  statusCheckTimer = window.setInterval(updateConnectionStatus, 1000)

  console.log('📊 开发日志监控页面已就绪')
})

onBeforeUnmount(() => {
  // 移除日志消息处理器
  webSocket.removeMessageHandler(DevLogHandler)

  // 清除定时器
  if (statusCheckTimer) {
    clearInterval(statusCheckTimer)
    statusCheckTimer = null
  }

  console.log('📊 开发日志监控页面已卸载')
})
</script>

<style lang="scss" scoped>
.log-monitor {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 84px - 32px);
  background: #f5f5f5;
  border-radius: 4px;
  overflow: hidden;
}

.header {
  flex-shrink: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 20px 16px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;

  .title {
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 16px;
    font-weight: 500;
  }

  .actions {
    display: flex;
    gap: 12px;
  }
}

.filters {
  flex-shrink: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
}

.log-list {
  flex: 1;
  padding: 16px 20px;
  overflow-y: auto;
  min-height: 0;

  &::-webkit-scrollbar {
    width: 8px;
  }

  &::-webkit-scrollbar-thumb {
    background: #d9d9d9;
    border-radius: 4px;

    &:hover {
      background: #bfbfbf;
    }
  }
}

.log-item {
  margin-bottom: 8px;
  padding: 8px 12px;
  background: #fff;
  border-left: 3px solid #409eff;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  transition: all 0.2s;

  &:hover {
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  }

  &.log-info {
    border-left-color: #67c23a;
  }

  &.log-warn {
    border-left-color: #e6a23c;
  }

  &.log-error {
    border-left-color: #f56c6c;
    background: #fef0f0;
  }
}

.log-header {
  margin-bottom: 6px;

  .log-meta {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 12px;

    .log-time {
      color: #909399;
    }

    .log-path {
      color: #909399;
      &::before {
        content: '•';
        margin: 0 6px;
      }
    }
  }
}

.log-body {
  pre {
    margin: 0;
    font-size: 13px;
    line-height: 1.5;
    color: #303133;
    white-space: pre-wrap;
    word-break: break-all;
  }
}

.footer {
  flex-shrink: 0;
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 12px 20px;
  background: #fff;
  border-top: 1px solid #e8e8e8;
  font-size: 13px;
  color: #606266;
}
</style>
