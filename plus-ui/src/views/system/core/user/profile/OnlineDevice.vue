<!-- 在线设备 -->
<template>
  <div>
    <!-- 在线设备表格 -->
    <el-table :data="devices" :show-header="true">
      <el-table-column :label="t('deviceType', '设备类型')" prop="deviceType" align="center" />
      <el-table-column :label="t('ipaddr', '主机')" prop="ipaddr" align="center" />
      <el-table-column :label="t('loginLocation', '登录地点')" prop="loginLocation" align="center" />
      <el-table-column :label="t('os', '操作系统')" prop="os" align="center" />
      <el-table-column :label="t('browser', '浏览器')" prop="browser" align="center" />
      <el-table-column :label="t('loginTime', '登录时间')" prop="loginTime" align="center" width="180">
        <template #default="{ row }">
          <span>{{ formatDate(row.loginTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="t('操作')" align="center" width="90" fixed="right">
        <template #default="{ row }">
          <el-tooltip :content="t('删除')" placement="top">
            <el-button link type="danger" icon="Delete" @click="handleDelOnline(row)"></el-button>
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts" name="OnlineDevice">
import { removeCurrentDevice } from '@/api/system/monitor/online/onlineApi'
import { refreshPage } from '@/utils/tab'
import { formatDate } from '@/utils/date'
import { showConfirm, showMsgSuccess } from '@/utils/modal'
const { t } = useI18n()

// =========== Props 定义 ===========

/**
 * 在线设备信息接口定义
 */
interface OnlineDeviceInfo {
  /**
   * 应用类型
   */
  applicationType: string

  /**
   * IP地址
   */
  ipaddr: string

  /**
   * 登录地点
   */
  loginLocation: string

  /**
   * 操作系统
   */
  os: string

  /**
   * 浏览器
   */
  browser: string

  /**
   * 登录时间
   */
  loginTime: number | string

  /**
   * 令牌ID
   */
  tokenId: string
}

/**
 * 组件 Props 接口定义
 */
interface OnlineDeviceProps {
  /**
   * 在线设备列表
   * 必传，不能为空
   */
  devices: OnlineDeviceInfo[]
}

/**定义 props，提供默认值*/
const props = withDefaults(defineProps<OnlineDeviceProps>(), {
  devices: () => []
})

// =========== 计算属性 ===========
/**响应式设备列表*/
const devices = computed(() => props.devices)

// =========== 设备操作 ===========
/**
 * 删除在线设备操作
 * @param row 要删除的设备信息
 */
const handleDelOnline = async (row: OnlineDeviceInfo) => {
  const confirmMsg = t('After removing the device, you will need to re-authenticate on that device', '删除设备后，在该设备登录需要重新进行验证')
  const [confirmErr] = await showConfirm(confirmMsg)
  if (confirmErr) return

  const [err] = await removeCurrentDevice(row.tokenId)
  if (!err) {
    showMsgSuccess(t('message.deleteSuccess'))
    await refreshPage()
  }
}
</script>
