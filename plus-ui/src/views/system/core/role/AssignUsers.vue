<!-- 分配用户 -->
<template>
  <AModal v-model="visible" :title="t('Assign Users', '分配用户')" size="large" footer-type="close-only" @cancel="handleClose">
    <div>
      <!-- 角色基本信息卡片 -->
      <el-card shadow="hover" class="mb-4">
        <template #header>
          <h5 class="m-0">{{ t('Role Info', '角色信息') }}</h5>
        </template>
        <el-descriptions :column="3" border>
          <el-descriptions-item :label="t('Role ID', '角色ID')">{{ roleInfo.roleId }}</el-descriptions-item>
          <el-descriptions-item :label="t('Role Name', '角色名称')">{{ roleInfo.roleName }}</el-descriptions-item>
          <el-descriptions-item :label="t('Permission Key', '权限字符')">{{ roleInfo.roleKey }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 用户搜索栏 -->
      <ASearchForm ref="queryFormRef" v-model="queryParams" :visible="showSearch">
        <AFormInput :label="t('User Name', '用户名称')" v-model="queryParams.userName" prop="userName" @input="handleQuery"></AFormInput>
        <AFormInput :label="t('Phone', '手机号码')" v-model="queryParams.phone" prop="phone" @input="handleQuery"></AFormInput>
      </ASearchForm>

      <el-card shadow="hover">
        <!-- 工具栏 -->
        <template #header>
          <el-row :gutter="10" class="mb-2">
            <el-col :span="1.5" v-permi="['system:role:add']">
              <el-button type="primary" plain icon="Plus" @click="openSelectUser"> {{ t('Add User', '添加用户') }}</el-button>
            </el-col>
            <el-col :span="1.5" v-permi="['system:role:delete']">
              <el-button type="danger" plain icon="CircleClose" :disabled="selectionItems.length === 0" @click="cancelAuthUserAll">
                {{ t('Batch Cancel Auth', '批量取消授权') }}
              </el-button>
            </el-col>

            <TableToolbar v-model:showSearch="showSearch" @reset-query="resetQuery" @query-table="getList"></TableToolbar>
          </el-row>
        </template>

        <!-- 用户表格数据 -->
        <el-table ref="userTableRef" v-loading="isLoading" :data="userList" height="318" stripe @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column :label="t('userName', '用户名称')" prop="userName" align="center" min-width="100" />
          <el-table-column :label="t('nickName', '用户昵称')" prop="nickName" align="center" min-width="100" />
          <el-table-column :label="t('email', '邮箱')" prop="email" align="center" min-width="120" />
          <el-table-column :label="t('phone', '手机')" prop="phone" align="center" min-width="120" />
          <el-table-column :label="t('status', '状态')" prop="status" align="center" min-width="80">
            <template #default="{ row }">
              <DictTag :value="row.status" :options="sys_enable_status" />
            </template>
          </el-table-column>
          <el-table-column :label="t('Create Time', '创建时间')" prop="createTime" align="center" min-width="160" />
          <el-table-column :label="t('Operation', '操作')" align="center" min-width="80" fixed="right">
            <template #default="{ row }">
              <el-tooltip :content="t('Cancel Auth', '取消授权')" placement="top">
                <el-button v-permi="['system:role:delete']" link type="primary" icon="CircleClose" @click="cancelAuthUser(row)"></el-button>
              </el-tooltip>
            </template>
          </el-table-column>
        </el-table>

        <Pagination v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
      </el-card>
    </div>

    <!-- 选择用户弹窗 -->
    <SelectUser ref="selectRef" :role-id="roleInfo.roleId" @ok="handleQuery" />
  </AModal>
</template>

<script setup lang="ts" name="AssignUsers">
import SelectUser from './SelectUser.vue'
import { pageRoleAuthorizedUsers, revokeUserRole, batchRevokeUserRoles } from '@/api/system/core/userRole/userRoleAuthApi'
import { getRole } from '@/api/system/core/role/roleApi'
import type { SysUserQuery, SysUserVo } from '@/api/system/core/user/userTypes'
import type { SysRoleVo } from '@/api/system/core/role/roleTypes'
import { showMsgSuccess, showConfirm } from '@/utils/modal'

const { t } = useI18n()

/**字典数据*/
const { sys_enable_status } = useDict(DictTypes.sys_enable_status)

interface AssignUsersProps {
  modelValue: boolean
  roleId?: string | number
}

interface AssignUsersEmits {
  (e: 'update:modelValue', value: boolean): void

  (e: 'success'): void
}

const props = withDefaults(defineProps<AssignUsersProps>(), {
  modelValue: false,
  roleId: undefined
})

const emit = defineEmits<AssignUsersEmits>()

// 使用表格高度处理钩子
const { queryFormRef, showSearch } = useTableHeight()

// =========== 对话框控制 ===========
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// =========== 角色信息相关 ===========
/**角色信息*/
const roleInfo = ref<Partial<SysRoleVo>>({
  roleId: undefined,
  roleName: undefined,
  roleKey: undefined
})

/** 查询角色信息 */
const getRoleInfo = async () => {
  if (!props.roleId) return

  const [err, data] = await getRole(props.roleId)
  if (!err) {
    Object.assign(roleInfo.value, data)
  }
}

// =========== 查询相关 ===========
/**查询参数对象*/
const queryParams = ref<SysUserQuery>({
  pageNum: 1,
  pageSize: 10,
  roleId: undefined,
  userName: undefined,
  phone: undefined
})

/** 用户搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1
  getList()
}

/** 用户重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

// =========== 授权用户表格数据相关 ===========
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
/**选择用户组件引用*/
const selectRef = ref<InstanceType<typeof SelectUser>>()

/** 表格多选事件处理 */
const handleSelectionChange = (selection: SysUserVo[]) => {
  selectionItems.value = selection
}

/** 查询授权用户列表 */
const getList = async () => {
  if (!props.roleId) return

  isLoading.value = true
  queryParams.value.roleId = props.roleId

  const [err, data] = await pageRoleAuthorizedUsers(queryParams.value)
  if (!err) {
    userList.value = data.records
    total.value = data.total
  }
  isLoading.value = false
}

/** 打开授权用户表弹窗 */
const openSelectUser = () => {
  selectRef.value?.show()
}

/** 取消授权按钮操作 */
const cancelAuthUser = async (row: SysUserVo) => {
  const [confirmErr] = await showConfirm(
    t('Confirm revoke role from user "{name}"?', `确认要取消该用户"${row.userName}"角色吗？`).replace('{name}', row.userName)
  )
  if (confirmErr) return

  const [err] = await revokeUserRole({ userId: row.userId, roleId: roleInfo.value.roleId as string })
  if (!err) {
    showMsgSuccess(t('message.cancelSuccess', '取消授权成功'))
    await getList()
    emit('success')
  }
}

/** 批量取消授权按钮操作 */
const cancelAuthUserAll = async () => {
  const userIds = selectionItems.value.map((item) => item.userId)
  if (userIds.length === 0) return

  const roleId = roleInfo.value.roleId

  const [confirmErr] = await showConfirm(t('message.confirmCancelSelected', '是否取消选中用户授权数据项?'))
  if (confirmErr) return

  const [err] = await batchRevokeUserRoles({ roleId: roleId as string, userIds })
  if (!err) {
    showMsgSuccess(t('message.cancelSuccess', '取消授权成功'))
    await getList()
    emit('success')
  }
}

/** 关闭对话框操作 */
const handleClose = () => {
  visible.value = false
  // 重置数据
  roleInfo.value = {
    roleId: undefined,
    roleName: undefined,
    roleKey: undefined
  }
  userList.value = []
  selectionItems.value = []
  queryParams.value = {
    pageNum: 1,
    pageSize: 10,
    roleId: undefined,
    userName: undefined,
    phone: undefined
  }
}

// =========== 监听变化 ===========
watch(
  () => props.modelValue,
  (newValue) => {
    if (newValue && props.roleId) {
      getRoleInfo()
      getList()
    }
  }
)
</script>
