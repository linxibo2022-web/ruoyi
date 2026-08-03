<!-- 用户管理 -->
<template>
  <div>
    <!-- 使用可拖拽面板组件 -->
    <AResizablePanels v-model:leftWidth="leftPanelWidth" :min-width="200" :max-width="500">
      <!-- 左侧面板：部门树（虚拟滚动 + 搜索防抖，解决大数据量卡死） -->
      <template #left>
        <ADeptTree
          ref="deptTreeRef"
          :data="deptOptions"
          :height="deptTreeHeight"
          :placeholder="t('Enter dept name', '请输入部门名称')"
          @node-click="handleNodeClick"
        />
      </template>

      <!-- 右侧面板：用户列表 -->
      <template #right>
        <!-- 用户搜索栏 -->
        <ASearchForm ref="queryFormRef" v-model="queryParams" :visible="showSearch">
          <AFormInput label="模糊搜索" prop="searchValue" v-model="queryParams.searchValue" @input="handleQuery"></AFormInput>
          <!--          <AFormInput label="用户名称" v-model="queryParams.userName" prop="userName" @input="handleQuery"></AFormInput>-->
          <!--          <AFormInput label="手机号码" v-model="queryParams.phone" prop="phone" @input="handleQuery"></AFormInput>-->
          <AFormSelect label="状态" v-model="queryParams.status" prop="status" :options="sys_enable_status" @change="handleQuery"></AFormSelect>
          <AFormDate label="创建时间" v-model="dateRangeCreateTime" prop="createTime" type="daterange" @change="handleQuery"></AFormDate>
        </ASearchForm>

        <el-card shadow="hover">
          <!-- 用户工具栏 -->
          <template #header>
            <el-row :gutter="10" class="mb-2">
              <el-col :span="1.5" v-permi="['system:user:add']">
                <el-button type="primary" plain icon="Plus" @click="handleAdd">
                  {{ t('新增') }}
                </el-button>
              </el-col>
              <el-col :span="1.5" v-permi="['system:user:update']">
                <el-button type="success" plain icon="Edit" :disabled="selectionItems.length !== 1" @click="handleUpdate()">
                  {{ t('修改') }}
                </el-button>
              </el-col>
              <el-col :span="1.5" v-permi="['system:user:delete']">
                <el-button type="danger" plain icon="Delete" :disabled="selectionItems.length === 0" @click="handleDelete()">
                  {{ t('删除') }}
                </el-button>
              </el-col>
              <el-col :span="1.5" v-permi="['system:user:import']">
                <AImportExcel
                  v-slot="{ openImportExcel }"
                  :title="t('User Data', '用户数据')"
                  template-url="/system/user/templateUsers"
                  import-url="/system/user/importUsers"
                  @import-success="getList"
                >
                  <el-button type="info" plain icon="Top" @click="openImportExcel">
                    {{ t('导入') }}
                  </el-button>
                </AImportExcel>
              </el-col>
              <el-col :span="1.5" v-permi="['system:user:export']">
                <el-button type="warning" plain icon="Download" @click="handleExport">
                  {{ t('导出') }}
                </el-button>
              </el-col>

              <TableToolbar v-model:showSearch="showSearch" v-model:columns="columns" @reset-query="resetQuery" @query-table="getList" />
            </el-row>
          </template>

          <!-- 用户表格数据 -->
          <el-table
            ref="userTableRef"
            v-loading="isLoading"
            :data="userList"
            :height="tableHeight"
            border
            stripe
            row-key="userId"
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="50" align="center" />
            <el-table-column
              v-for="col in columns.filter((c) => c.visible)"
              :key="col.field"
              :label="t(col.field, col.label)"
              :prop="col.field"
              align="center"
              :width="col.width"
              :min-width="col.minWidth"
              resizable
            >
              <template #default="{ row }">
                <template v-if="col.field === 'userId'">
                  <span class="cursor-pointer" @click="copy(row.userId)">{{ row.userId }}</span>
                </template>
                <template v-else-if="col.field === 'userName'">
                  <span class="cursor-pointer" @click="copy(row.userName)">{{ row.userName }}</span>
                </template>
                <template v-else-if="col.field === 'nickName'">
                  <span class="cursor-pointer" @click="copy(row.nickName)">{{ row.nickName }}</span>
                </template>
                <template v-else-if="col.field === 'deptName'">
                  {{ row.deptName }}
                </template>
                <template v-else-if="col.field === 'phone'">
                  <span class="cursor-pointer" @click="copy(row.phone)">{{ row.phone }}</span>
                </template>
                <template v-else-if="col.field === 'status'">
                  <AFormSwitch v-model="row.status" :disabled="row.userId === 1" @change="handleStatusChange(row)" />
                </template>
                <template v-else-if="col.field === 'createTime'">
                  {{ row.createTime }}
                </template>
              </template>
            </el-table-column>
            <el-table-column :label="t('操作')" align="center" width="200" fixed="right">
              <template #default="{ row }">
                <template v-if="row.userId !== 1">
                  <el-tooltip :content="t('修改')" placement="top">
                    <el-button v-permi="['system:user:update']" link type="success" icon="Edit" @click="handleUpdate(row)"></el-button>
                  </el-tooltip>
                  <el-tooltip :content="t('删除')" placement="top">
                    <el-button v-permi="['system:user:delete']" link type="danger" icon="Delete" @click="handleDelete(row)"></el-button>
                  </el-tooltip>
                  <el-tooltip :content="t('Reset Password', '重置密码')" placement="top">
                    <el-button v-permi="['system:user:resetPwd']" link type="primary" icon="Key" @click="handleResetPwd(row)"></el-button>
                  </el-tooltip>
                  <el-tooltip :content="t('Assign Roles', '分配角色')" placement="top">
                    <el-button v-permi="['system:user:update']" link type="primary" icon="CircleCheck" @click="handleAssignRoles(row)"></el-button>
                  </el-tooltip>
                  <el-tooltip :content="t('Connection Info', '关联信息')" placement="top">
                    <el-button v-permi="['system:user:query']" link type="primary" icon="Connection" @click="handleConnectionInfo(row)"></el-button>
                  </el-tooltip>
                </template>
              </template>
            </el-table-column>
          </el-table>

          <Pagination v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
        </el-card>
      </template>
    </AResizablePanels>

    <!-- 添加或修改用户对话框 -->
    <AModal v-model="dialog.visible" :title="dialog.title" :loading="buttonLoading" @confirm="submitForm" @cancel="closeDialog">
      <el-form ref="userFormRef" :model="form" :rules="rules" label-width="auto">
        <el-row>
          <AFormInput label="用户昵称" v-model="form.nickName" prop="nickName" span="auto"></AFormInput>
          <AFormTreeSelect
            label="归属部门"
            v-model="form.deptId"
            prop="deptId"
            :data="enabledDeptOptions"
            span="auto"
            @change="handleDeptChange"
          ></AFormTreeSelect>
        </el-row>
        <el-row>
          <AFormInput label="手机号码" v-model="form.phone" prop="phone" :maxlength="11" span="auto"></AFormInput>
          <AFormInput label="邮箱" v-model="form.email" prop="email" :maxlength="50" span="auto"></AFormInput>
        </el-row>
        <el-row>
          <AFormInput
            v-if="form.userId == undefined"
            label="用户名称"
            v-model="form.userName"
            prop="userName"
            :maxlength="20"
            span="auto"
          ></AFormInput>
          <AFormInput
            type="password"
            show-password
            v-if="form.userId == undefined"
            label="用户密码"
            v-model="form.password"
            prop="password"
            :maxlength="20"
            span="auto"
          ></AFormInput>
        </el-row>
        <el-row>
          <AFormSelect label="用户性别" v-model="form.gender" prop="gender" :options="sys_user_gender" span="auto"></AFormSelect>
          <AFormRadio label="状态" v-model="form.status" prop="status" :options="sys_enable_status" span="auto"></AFormRadio>
        </el-row>
        <el-row>
          <AFormSelect
            label="岗位"
            v-model="form.postIds"
            prop="postIds"
            :options="postOptions"
            multiple
            value-field="postId"
            label-field="postName"
            span="auto"
          ></AFormSelect>
          <AFormSelect
            label="角色"
            prop="roleIds"
            v-model="form.roleIds"
            :options="roleOptions"
            multiple
            value-field="roleId"
            label-field="roleName"
            span="auto"
          ></AFormSelect>
        </el-row>
        <AFormInput type="textarea" label="备注" v-model="form.remark" prop="remark" :span="24"></AFormInput>
      </el-form>
    </AModal>

    <!-- 分配角色对话框 -->
    <AssignRoles v-model="assignRolesDialog.visible" :userId="assignRolesDialog.userId" @success="handleAssignRolesSuccess" />

    <!-- 关联信息对话框 -->
    <ConnectionInfo v-model="connectionInfoDialog.visible" :userId="connectionInfoDialog.userId" />
  </div>
</template>

<script setup lang="ts" name="User">
import { pageUsers, getUser, addUser, updateUser, deleteUsers, changeUserStatus, resetUserPwd } from '@/api/system/core/user/userApi'
import { getDeptTreeOptions } from '@/api/system/core/dept/deptApi'
import { getPostOptions } from '@/api/system/core/post/postApi'
import { getByConfigKey } from '@/api/system/config/config/configApi'
import type { SysUserQuery, SysUserBo, SysUserVo } from '@/api/system/core/user/userTypes'
import type { SysDeptTreeVo } from '@/api/system/core/dept/deptTypes'
import type { SysRoleVo } from '@/api/system/core/role/roleTypes'
import type { SysPostVo } from '@/api/system/core/post/postTypes'
import { copy } from '@/utils/function'
import { isTrue, toggleStatus } from '@/utils/boolean'
import { addDateRange, initDateRangeFromQuery } from '@/utils/date'
import { toValidate } from '@/utils/to'
import { showMsgSuccess, showConfirm, showPrompt } from '@/utils/modal'
import AssignRoles from './AssignRoles.vue'
import ConnectionInfo from './ConnectionInfo.vue'

/** 列配置类型 */
interface ColumnConfig {
  field: string
  label: string
  visible: boolean
  width?: number
  minWidth?: number
}

const { t } = useI18n()
const route = useRoute()

/**字典数据 */
const { sys_enable_status, sys_user_gender } = useDict(DictTypes.sys_enable_status, DictTypes.sys_user_gender)

// 使用表格高度处理钩子
const { tableHeight, queryFormRef, showSearch, calculateTableHeight } = useTableHeight()

/**左侧面板宽度*/
const leftPanelWidth = ref(230)

// =========== 查询相关 ===========

/**查询参数对象*/
const queryParams = ref<SysUserQuery>({
  pageNum: 1,
  pageSize: 10,
  userName: undefined,
  phone: undefined,
  status: undefined,
  deptId: undefined,
  roleId: undefined
})

/**日期范围选择器*/
const dateRangeCreateTime = ref<[ElDateModelType, ElDateModelType]>(['', ''])
const dateRangeLoginDate = ref<[ElDateModelType, ElDateModelType]>(['', ''])

/**
 * 根据路由参数初始化查询条件
 */
const initQueryFromRoute = () => {
  // 初始化日期范围
  const { field = 'createTime' } = route.query
  if (field === 'createTime') {
    dateRangeCreateTime.value = initDateRangeFromQuery(route.query)
    dateRangeLoginDate.value = ['', '']
  } else if (field === 'loginDate') {
    dateRangeLoginDate.value = initDateRangeFromQuery(route.query)
    dateRangeCreateTime.value = ['', '']
  }
}

/** 用户搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1
  getList()
}

/** 用户重置按钮操作 */
const resetQuery = () => {
  dateRangeCreateTime.value = ['', '']
  dateRangeLoginDate.value = ['', '']
  queryFormRef.value?.resetFields()
  queryParams.value.pageNum = 1
  queryParams.value.deptId = undefined
  deptTreeRef.value?.setCurrentKey(undefined)
  handleQuery()
}

// =========== 部门树相关 ===========
/**部门树数据*/
const deptOptions = ref<SysDeptTreeVo[]>([])
/**启用的部门树数据*/
const enabledDeptOptions = ref<SysDeptTreeVo[]>([])
/**部门树引用*/
const deptTreeRef = ref()

/**部门树虚拟滚动高度（px）：跟随搜索栏与表格高度联动，供 el-tree-v2 虚拟滚动使用*/
const deptTreeHeight = computed(() => (queryFormRef.value?.$el?.offsetHeight || 0) + tableHeight.value + 82)

/**节点单击事件*/
const handleNodeClick = (data: SysDeptTreeVo) => {
  queryParams.value.deptId = data.id
  handleQuery()
}

/**查询部门下拉树结构*/
const buildDeptTree = async () => {
  const [err, data] = await getDeptTreeOptions()
  if (!err) {
    deptOptions.value = data
    enabledDeptOptions.value = filterDisabledDept(data)
  }
}

/**过滤禁用的部门*/
const filterDisabledDept = (deptList: SysDeptTreeVo[]) => {
  return deptList.filter((dept) => {
    if (dept.disabled) {
      return false
    }
    if (dept.children && dept.children.length) {
      dept.children = filterDisabledDept(dept.children)
    }
    return true
  })
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
/**选中的数据项*/
const selectionItems = ref<SysUserVo[]>([])

/** 列配置（使用 ref 以支持 v-model 双向绑定） */
const columns = ref<ColumnConfig[]>([
  { field: 'userId', label: t('User ID', '用户ID'), visible: true, width: 170 },
  { field: 'userName', label: t('Username', '用户名称'), visible: true, minWidth: 100 },
  { field: 'nickName', label: t('Nickname', '用户昵称'), visible: true, minWidth: 100 },
  { field: 'deptName', label: t('Department', '部门'), visible: true, minWidth: 100 },
  { field: 'phone', label: t('Phone', '手机号码'), visible: true, width: 120 },
  { field: 'status', label: t('Status', '状态'), visible: true, minWidth: 80 },
  { field: 'createTime', label: t('Create Time', '创建时间'), visible: true, width: 160 }
])

/** 表格多选事件处理 */
const handleSelectionChange = (selection: SysUserVo[]) => {
  selectionItems.value = selection
}

/** 查询用户列表 */
const getList = async () => {
  isLoading.value = true
  addDateRange(queryParams.value, dateRangeCreateTime.value)
  addDateRange(queryParams.value, dateRangeLoginDate.value, 'loginDate')

  const [err, data] = await pageUsers(queryParams.value)
  if (!err) {
    userList.value = data.records
    total.value = data.total
  }
  isLoading.value = false
}

/** 导出用户数据 */
const handleExport = () => {
  useDownload().exportExcel(t('User Data', '用户数据'), '/system/user/exportUsers', queryParams.value)
}

/** 获取用户显示名称（优先用户名，其次昵称、手机号、ID） */
const getUserDisplayName = (user: SysUserVo) => {
  const name = user.userName || user.nickName || user.phone || `ID:${user.userId}`
  const nick = user.nickName || ''
  return nick ? `${name}(${nick})` : name
}

/** 删除用户操作 */
const handleDelete = async (row?: SysUserVo) => {
  const idsToDelete = row ? [row.userId] : selectionItems.value.map((item) => item.userId)
  if (idsToDelete.length === 0) return
  const itemsToDelete = row ? getUserDisplayName(row) : selectionItems.value.map((item) => getUserDisplayName(item)).join(', ')
  const [confirmErr] = await showConfirm(`${t('是否确认删除')}${itemsToDelete}`)
  if (confirmErr) return

  const [deleteErr] = await deleteUsers(idsToDelete)
  if (!deleteErr) {
    showMsgSuccess(t('message.deleteSuccess'))
    await getList()
  }
}

// =========== 用户表单相关 ===========
/**初始表单数据*/
const initFormData: SysUserBo = {
  userId: undefined,
  deptId: undefined,
  userName: undefined,
  nickName: undefined,
  password: undefined,
  phone: undefined,
  email: undefined,
  gender: undefined,
  status: '1',
  remark: undefined,
  postIds: [],
  roleIds: []
}

/**表单引用*/
const userFormRef = ref<ElFormInstance>()
/**表单提交按钮加载状态*/
const buttonLoading = ref(false)
/**对话框配置对象*/
const dialog = ref<DialogState>({
  visible: false,
  title: ''
})
/**表单数据对象*/
const form = ref<SysUserBo>({ ...initFormData })
/**初始密码*/
const initPassword = ref('')
/**岗位选项*/
const postOptions = ref<SysPostVo[]>([])
/**角色选项*/
const roleOptions = ref<SysRoleVo[]>([])

// =========== 分配角色相关 ===========
/**分配角色对话框配置*/
const assignRolesDialog = ref<{ visible: boolean; userId: string | number }>({
  visible: false,
  userId: ''
})

// =========== 关联信息相关 ===========
/**关联信息对话框配置*/
const connectionInfoDialog = ref<{ visible: boolean; userId: string | number }>({
  visible: false,
  userId: ''
})

/**表单校验规则*/
const rules = computed<ElFormRules>(() => ({
  userName: [
    { required: true, message: t('Username required', '用户名称不能为空'), trigger: 'blur' },
    { min: 2, max: 20, message: t('Username length', '用户名称长度必须介于 2 和 20 之间'), trigger: 'blur' }
  ],
  nickName: [{ required: true, message: t('Nickname required', '用户昵称不能为空'), trigger: 'blur' }],
  password: [
    { required: true, message: t('Password required', '用户密码不能为空'), trigger: 'blur' },
    { min: 5, max: 20, message: t('Password length', '用户密码长度必须介于 5 和 20 之间'), trigger: 'blur' },
    { pattern: /^[^<>"'|\\]+$/, message: t('Invalid characters', '不能包含非法字符：< > " \' \\ |'), trigger: 'blur' }
  ],
  email: [{ type: 'email', message: t('Invalid email', '请输入正确的邮箱地址'), trigger: ['blur', 'change'] }],
  phone: [{ pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/, message: t('Invalid phone', '请输入正确的手机号码'), trigger: 'blur' }],
  roleIds: [{ required: true, message: t('Role required', '用户角色不能为空'), trigger: 'blur' }]
}))

/** 用户表单重置 */
const reset = () => {
  form.value = { ...initFormData }
  userFormRef.value?.resetFields()
}

/** 取消用户编辑 */
const cancel = () => {
  dialog.value.visible = false
  reset()
}

/** 关闭对话框 */
const closeDialog = () => {
  dialog.value.visible = false
  reset()
}

/** 新增用户操作 */
const handleAdd = async () => {
  reset()
  const [err, data] = await getUser()
  if (!err) {
    dialog.value.visible = true
    dialog.value.title = `${t('新增')}${t('user', '用户')}`
    postOptions.value = data.posts || []
    roleOptions.value = data.roles || []
    form.value.password = initPassword.value.toString()
    // 如果左侧部门树选中了部门，则默认设置为该部门，并加载该部门的岗位
    if (queryParams.value.deptId) {
      form.value.deptId = queryParams.value.deptId
      // 加载该部门对应的岗位列表
      await handleDeptChange(queryParams.value.deptId)
    }
  }
}

/** 修改用户操作 */
const handleUpdate = async (row?: SysUserBo) => {
  reset()
  const itemToEdit = row || selectionItems.value[0]
  const [err, data] = await getUser(itemToEdit.userId)
  if (!err) {
    Object.assign(form.value, data.user)
    dialog.value.visible = true
    dialog.value.title = `${t('修改')}${t('user', '用户')}`
    postOptions.value = data.posts || []
    roleOptions.value = data.roles || []
    form.value.postIds = data.postIds
    form.value.roleIds = data.roleIds
    form.value.password = ''
  }
}

/** 提交用户表单 */
const submitForm = async () => {
  const [validateErr] = await toValidate(userFormRef)
  if (validateErr) return

  buttonLoading.value = true
  let err: Error | null, msg: string | null
  if (form.value.userId) {
    ;[err] = await updateUser(form.value)
  } else {
    ;[err] = await addUser(form.value)
  }

  if (!err) {
    showMsgSuccess(form.value.userId ? t('message.updateSuccess') : t('message.addSuccess'))
    dialog.value.visible = false
    await getList()
  }
  buttonLoading.value = false
}

/** 部门变更处理 */
const handleDeptChange = async (value: number | string) => {
  const [err, data] = await getPostOptions(value)
  if (!err) {
    postOptions.value = data
    form.value.postIds = []
  }
}

// =========== 状态和权限操作 ===========

/** 用户启用禁用状态修改 */
const handleStatusChange = async (row: SysUserVo) => {
  const text = isTrue(row.status) ? t('启用') : t('停用')
  const [confirmErr] = await showConfirm(`${t('是否确认')}${text}${row.userName}?`)
  if (confirmErr) {
    row.status = toggleStatus(row.status)
    return
  }
  const [updateErr] = await changeUserStatus(row.userId, row.status)
  if (updateErr) {
    row.status = toggleStatus(row.status)
    return
  }
  await getList()
  showMsgSuccess(`${text}${t('成功')}`)
}

/** 打开分配角色对话框 */
const handleAssignRoles = (row: SysUserVo) => {
  assignRolesDialog.value.userId = row.userId
  assignRolesDialog.value.visible = true
}

/** 分配角色成功回调 */
const handleAssignRolesSuccess = () => {
  getList()
}

/**查看关联信息*/
const handleConnectionInfo = (row: SysUserVo) => {
  connectionInfoDialog.value.userId = row.userId
  connectionInfoDialog.value.visible = true
}

/** 重置密码按钮操作 */
const handleResetPwd = async (row: SysUserVo) => {
  const [err, res] = await showPrompt({
    message: t('Enter new password for', '请输入新密码') + ` "${row.userName || row.userId}"`,
    title: t('Prompt', '提示'),
    confirmButtonText: t('Confirm', '确定'),
    cancelButtonText: t('Cancel', '取消'),
    closeOnClickModal: false,
    inputPattern: /^.{5,20}$/,
    inputErrorMessage: t('Password length', '用户密码长度必须介于 5 和 20 之间'),
    inputValidator: (value) => {
      if (/<|>|"|'|\||\\/.test(value)) {
        return t('Invalid characters', '不能包含非法字符：< > " \' \\ |')
      }
    }
  })

  if (!err && res) {
    await resetUserPwd(row.userId, res.value)
    showMsgSuccess(t('Password reset success', '修改成功，新密码是：') + res.value)
  }
}

// =========== 生命周期 ===========
/**初始化用户数据列表*/
onMounted(async () => {
  // 根据路由参数初始化查询条件
  initQueryFromRoute()
  buildDeptTree() // 初始化部门数据
  getList() // 初始化列表数据
  const [err, data] = await getByConfigKey('system.user.initial-password')
  if (!err) {
    initPassword.value = data
  }
})
/**页面激活时刷新用户列表*/
onActivated(() => {
  if (isLoading.value) return
  // 重新初始化查询条件
  initQueryFromRoute()
  getList()
})
</script>
