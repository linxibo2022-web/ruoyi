<!-- 在线用户 -->
<template>
  <div>
    <!--  搜索栏  -->
    <ASearchForm ref="queryFormRef" v-model="queryParams" :visible="showSearch">
      <AFormInput label="用户名称" v-model="queryParams.userName" prop="userName" @input="handleQuery"></AFormInput>
      <AFormInput label="登录地址" v-model="queryParams.ipaddr" prop="ipaddr" @input="handleQuery"></AFormInput>
    </ASearchForm>

    <el-card shadow="hover">
      <!--   工具栏   -->
      <template #header>
        <el-row :gutter="10" class="mb-2">
          <el-col :span="1.5" v-permi="['monitor:online:forceLogout']">
            <el-button type="danger" plain icon="Delete" :disabled="selectionItems.length === 0" @click="handleBatchForceLogout">
              {{ t('button.batchLogout') }}
            </el-button>
          </el-col>
          <TableToolbar v-model:showSearch="showSearch" @reset-query="resetQuery" @query-table="getList"></TableToolbar>
        </el-row>
      </template>

      <!--   表格数据  -->
      <el-table
        ref="onlineTableRef"
        v-loading="isLoading"
        :data="onlineList"
        :max-height="tableHeight"
        row-key="tokenId"
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column :label="t('tokenId', '会话编号')" prop="tokenId" align="center" show-overflow-tooltip />
        <el-table-column :label="t('userName', '登录名称')" prop="userName" align="center" />
        <el-table-column :label="t('deviceType', '设备类型')" prop="deviceType" align="center" />
        <el-table-column :label="t('deptName', '所属部门')" prop="deptName" align="center" />
        <el-table-column :label="t('ipaddr', '主机')" prop="ipaddr" align="center" />
        <el-table-column :label="t('loginLocation', '登录地点')" prop="loginLocation" align="center" />
        <el-table-column :label="t('os', '操作系统')" prop="os" align="center" show-overflow-tooltip />
        <el-table-column :label="t('browser', '浏览器')" prop="browser" align="center" />
        <el-table-column :label="t('loginTime', '登录时间')" prop="loginTime" align="center" width="105">
          <template #default="{ row }">
            <span>{{ formatDate(row.loginTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="t('操作')" align="center" min-width="80" fixed="right">
          <template #default="{ row }">
            <el-tooltip :content="t('button.forceLogout')" placement="top">
              <el-button v-permi="['monitor:online:forceLogout']" link type="danger" icon="Delete" @click="handleForceLogout(row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>
  </div>
</template>

<script setup lang="ts" name="Online">
import { forceLogout, pageOnlineUsers } from '@/api/system/monitor/online/onlineApi'
import type { SysUserOnlineQuery, SysUserOnlineVo } from '@/api/system/monitor/online/onlineTypes'
import { formatDate } from '@/utils/date'
import { showMsgSuccess, showMsgWarning, showMsgError, showConfirm } from '@/utils/modal'
const { t } = useI18n()

// 使用表格高度处理钩子
const { tableHeight, queryFormRef, showSearch } = useTableHeight()

// =========== 查询相关 ===========
/**查询参数对象*/
const queryParams = ref<SysUserOnlineQuery>({
  pageNum: 1,
  pageSize: 10,
  ipaddr: '',
  userName: ''
})

/** 在线用户搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1
  getList()
}

/** 在线用户重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

// =========== 在线用户表格数据相关 ===========
/**表格加载状态*/
const isLoading = ref(true)
/**在线用户数据列表*/
const onlineList = ref<SysUserOnlineVo[]>([])
/**总记录数*/
const total = ref(0)
/**表格实例*/
const onlineTableRef = ref()
/**选中的数据项*/
const selectionItems = ref<SysUserOnlineVo[]>([])

/** 表格多选事件处理 */
const handleSelectionChange = (selection: SysUserOnlineVo[]) => {
  selectionItems.value = selection
}

/** 查询在线用户列表 */
const getList = async () => {
  isLoading.value = true
  const [err, data] = await pageOnlineUsers(queryParams.value)
  if (!err) {
    onlineList.value = data.records
    total.value = data.total
  }
  isLoading.value = false
}

/** 获取在线用户显示名称（优先用户名，其次登录IP） */
const getOnlineUserDisplayName = (user: SysUserOnlineVo) => {
  return user.userName || user.ipaddr || user.tokenId
}

/** 强制退出用户操作 */
const handleForceLogout = async (row: SysUserOnlineVo) => {
  const [confirmErr] = await showConfirm(`${t('message.confirmForceLogout')}${getOnlineUserDisplayName(row)}?`)
  if (confirmErr) return

  const [logoutErr] = await forceLogout(row.tokenId)
  if (!logoutErr) {
    showMsgSuccess('强退成功')
    await getList()
  }
}

/** 批量强制退出用户操作 */
const handleBatchForceLogout = async () => {
  if (selectionItems.value.length === 0) return

  const userNames = selectionItems.value.map((item) => getOnlineUserDisplayName(item)).join(',')
  const [confirmErr] = await showConfirm(`${t('message.confirmBatchForceLogout')}${userNames}?`)
  if (confirmErr) return

  const tokenIds = selectionItems.value.map((item) => item.tokenId)
  let successCount = 0
  let failCount = 0

  // 串行处理，逐个检查错误
  for (const tokenId of tokenIds) {
    const [err] = await forceLogout(tokenId)
    if (!err) {
      successCount++
    } else {
      console.error(`强退用户 ${tokenId} 失败:`, err.message)
      failCount++
    }
  }

  // 显示结果
  if (failCount === 0) {
    showMsgSuccess(t('message.batchLogoutSuccess'))
  } else if (successCount === 0) {
    showMsgError(`批量强退失败，共 ${failCount} 个用户强退失败`)
  } else {
    showMsgWarning(`批量强退完成，成功 ${successCount} 个，失败 ${failCount} 个`)
  }

  await getList()
}

// =========== 生命周期 ===========
/**初始化在线用户数据列表*/
onMounted(() => {
  getList()
})

/**页面激活时刷新在线用户列表*/
onActivated(() => {
  if (isLoading.value) return
  getList()
})
</script>
