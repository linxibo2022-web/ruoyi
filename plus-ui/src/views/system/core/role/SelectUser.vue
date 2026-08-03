<!-- 选择用户 -->
<template>
  <AModal v-model="dialog.visible" :title="dialog.title" mode="dialog" size="xl">
    <!-- 使用可拖拽面板组件 -->
    <AResizablePanels v-model:leftWidth="leftPanelWidth" :min-width="200" :max-width="350" :gutter="20">
      <!-- 左侧面板：部门树（虚拟滚动 + 搜索防抖，解决大数据量卡死） -->
      <template #left>
        <ADeptTree ref="deptTreeRef" :data="deptOptions" :height="treeHeight" :title="t('Dept Filter', '部门筛选')" @node-click="handleNodeClick" />
      </template>

      <!-- 右侧面板：用户列表 -->
      <template #right>
        <!-- 用户搜索栏 -->
        <ASearchForm ref="queryFormRef" v-model="queryParams" :visible="showSearch">
          <AFormInput label="用户名称" v-model="queryParams.userName" prop="userName" @input="handleQuery"></AFormInput>
          <AFormInput label="手机号码" v-model="queryParams.phone" prop="phone" @input="handleQuery"></AFormInput>
        </ASearchForm>

        <el-card shadow="hover">
          <!-- 用户工具栏 -->
          <template #header>
            <el-row :gutter="10" class="mb-2">
              <!-- 显示已选用户标签 -->
              <ASelectionTags
                :items="selectedUsers"
                :key-field="'userId'"
                :formatter="(user) => user.userName"
                :on-clear="clearAllSelection"
                @close="handleTagClose"
              >
              </ASelectionTags>
              <TableToolbar v-model:showSearch="showSearch" @reset-query="resetQuery" @query-table="getList"></TableToolbar>
            </el-row>
          </template>

          <!-- 用户表格数据 -->
          <el-table
            ref="userTableRef"
            v-loading="isLoading"
            :data="userList"
            :height="tableHeight"
            stripe
            @selection-change="handleSelectionChange"
            @select="handleSelect"
            @select-all="handleSelectAll"
          >
            <el-table-column type="selection" width="50" align="center" :selectable="isRowSelectable" />
            <el-table-column :label="t('userName', '用户名称')" prop="userName" align="center" />
            <el-table-column :label="t('nickName', '用户昵称')" prop="nickName" align="center" />
            <el-table-column :label="t('deptName', '部门')" prop="deptName" align="center" />
            <el-table-column :label="t('email', '邮箱')" prop="email" align="center" />
            <el-table-column :label="t('phone', '手机')" prop="phone" align="center" />
            <el-table-column :label="t('status', '状态')" prop="status" align="center">
              <template #default="{ row }">
                <DictTag :options="sys_enable_status" :value="row.status" />
              </template>
            </el-table-column>
            <el-table-column :label="t('createTime', '创建时间')" prop="createTime" align="center" />
          </el-table>

          <Pagination v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
        </el-card>
      </template>
    </AResizablePanels>

    <!-- 底部操作按钮 -->
    <template #footer>
      <el-button @click="cancel">{{ t('取消') }}</el-button>
      <el-button :loading="buttonLoading" type="primary" @click="handleSelectUser"> {{ t('确定') }}（{{ selectedUserIds.size }}） </el-button>
    </template>
  </AModal>
</template>

<script setup lang="ts" name="SelectUser">
import { pageRoleUnauthorizedUsers, batchGrantUserRoles } from '@/api/system/core/userRole/userRoleAuthApi'
import { getDeptTreeOptions } from '@/api/system/core/dept/deptApi'
import type { SysUserQuery, SysUserVo } from '@/api/system/core/user/userTypes'
import type { SysDeptTreeVo } from '@/api/system/core/dept/deptTypes'
import { showMsgSuccess, showMsgError } from '@/utils/modal'

const { t } = useI18n()

/**字典数据 */
const { sys_enable_status } = useDict(DictTypes.sys_enable_status)

// 使用表格高度处理钩子
const { tableHeight, queryFormRef, showSearch } = useTableHeight(200)

/**角色ID */
interface SelectUserProps {
  roleId?: string | number
}

const props = withDefaults(defineProps<SelectUserProps>(), {
  // 默认值可以在这里定义
})

// =========== 可拖拽面板相关 ===========
/**左侧面板宽度*/
const leftPanelWidth = ref(220)

/**树形结构高度计算*/
const treeHeight = computed(() => {
  // 弹窗中的高度计算，考虑搜索表单等
  return 420
})

// =========== 部门树相关 ===========
/** 部门树数据 */
const deptOptions = ref<SysDeptTreeVo[]>([])
/** 部门树引用 */
const deptTreeRef = ref()

/**
 * 获取部门树结构数据
 */
const getTreeSelect = async () => {
  const [err, data] = await getDeptTreeOptions()
  if (!err) deptOptions.value = data
}

/**
 * 部门树节点点击事件处理
 */
const handleNodeClick = (data: SysDeptTreeVo) => {
  queryParams.value.deptId = data.id
  handleQuery()
}

// =========== 查询相关 ===========

/**查询参数对象*/
const queryParams = ref<SysUserQuery>({
  pageNum: 1,
  pageSize: 10,
  roleId: undefined,
  userName: undefined,
  phone: undefined,
  deptId: undefined // 新增部门ID过滤条件
})

/** 用户搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1
  getList()
}

/** 用户重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value?.resetFields()
  queryParams.value.deptId = undefined
  deptTreeRef.value?.setCurrentKey(undefined)
  handleQuery()
}

// =========== 跨页选择管理 ===========
/**全局选中的用户ID集合*/
const selectedUserIds = ref<Set<string | number>>(new Set())
/**全局选中的用户信息*/
const selectedUsers = ref<SysUserVo[]>([])

/** 添加选中用户 */
const addSelectedUser = (user: SysUserVo) => {
  selectedUserIds.value.add(user.userId)
  // 避免重复添加
  if (!selectedUsers.value.find((u) => u.userId === user.userId)) {
    selectedUsers.value.push(user)
  }
}

/** 移除选中用户 */
const removeSelectedUser = (userId: string | number) => {
  selectedUserIds.value.delete(userId)
  selectedUsers.value = selectedUsers.value.filter((u) => u.userId !== userId)

  // 同步更新当前页表格的选中状态
  nextTick(() => {
    updateCurrentPageSelection()
  })
}

/** 处理标签关闭事件 */
const handleTagClose = (userId: string | number) => {
  removeSelectedUser(userId)
}

/** 清空所有选择 */
const clearAllSelection = () => {
  selectedUserIds.value.clear()
  selectedUsers.value = []
  userTableRef.value?.clearSelection()
}

/** 判断行是否可选择 */
const isRowSelectable = (row: SysUserVo) => {
  return true // 这里可以加入业务逻辑判断
}

// =========== 用户表格数据相关 ===========
/**表格加载状态*/
const isLoading = ref(true)
/**数据列表*/
const userList = ref<SysUserVo[]>([])
/**总记录数*/
const total = ref(0)
/**表格实例*/
const userTableRef = ref()
/**按钮加载状态*/
const buttonLoading = ref(false)
/**对话框配置对象*/
const dialog = ref<DialogState>({
  visible: false,
  title: ''
})

/** 更新当前页的选择状态 */
const updateCurrentPageSelection = () => {
  if (!userTableRef.value) return

  userList.value.forEach((user) => {
    const isSelected = selectedUserIds.value.has(user.userId)
    userTableRef.value.toggleRowSelection(user, isSelected)
  })
}

/** 表格选择变化处理 */
const handleSelectionChange = (selection: SysUserVo[]) => {
  // 这里主要用于处理全选/取消全选的情况
  // 具体的单行选择在 handleSelect 中处理
}

/** 单行选择处理 */
const handleSelect = (selection: SysUserVo[], row: SysUserVo) => {
  const isSelected = selection.includes(row)

  if (isSelected) {
    addSelectedUser(row)
  } else {
    removeSelectedUser(row.userId)
  }
}

/** 全选处理 */
const handleSelectAll = (selection: SysUserVo[]) => {
  if (selection.length === 0) {
    // 取消当前页所有选择
    userList.value.forEach((user) => {
      removeSelectedUser(user.userId)
    })
  } else {
    // 选中当前页所有用户
    userList.value.forEach((user) => {
      if (isRowSelectable(user)) {
        addSelectedUser(user)
      }
    })
  }
}

/** 查询用户列表 */
const getList = async () => {
  isLoading.value = true
  queryParams.value.roleId = props.roleId

  const [err, data] = await pageRoleUnauthorizedUsers(queryParams.value)
  if (!err) {
    userList.value = data.records
    total.value = data.total

    // 更新当前页的选择状态
    await nextTick()
    updateCurrentPageSelection()
  }
  isLoading.value = false
}

/** 取消按钮 */
const cancel = () => {
  dialog.value.visible = false
  clearAllSelection()
  // 重置部门树选择
  queryParams.value.deptId = undefined
  deptTreeRef.value?.setCurrentKey(undefined)
}

const emit = defineEmits(['ok'])

/** 选择授权用户操作 */
const handleSelectUser = async () => {
  if (selectedUserIds.value.size === 0) {
    showMsgError(t('Please select users to assign', '请选择要分配的用户'))
    return
  }

  buttonLoading.value = true
  const [err] = await batchGrantUserRoles({
    roleId: props.roleId,
    userIds: Array.from(selectedUserIds.value)
  })
  if (!err) {
    showMsgSuccess(t('message.assignSuccess', '分配成功'))
    emit('ok')
    dialog.value.visible = false
    clearAllSelection()
  }
  buttonLoading.value = false
}

/** 显示弹窗 */
const show = async () => {
  dialog.value.visible = true
  dialog.value.title = t('Select Users', '选择用户')
  clearAllSelection() // 每次打开时清空之前的选择

  // 加载部门树数据
  await getTreeSelect()
  // 加载用户列表
  getList()
}

// 暴露方法
defineExpose({
  show
})
</script>
