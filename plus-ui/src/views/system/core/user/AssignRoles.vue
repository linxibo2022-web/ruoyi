<!-- 分配角色 -->
<template>
  <AModal
    v-model="visible"
    :title="t('Assign Roles', '分配角色')"
    size="large"
    :loading="buttonLoading"
    @confirm="submitForm"
    @close="handleClose"
    @cancel="handleClose"
  >
    <div>
      <!-- 用户基本信息卡片 -->
      <el-card shadow="hover" class="mb-4">
        <template #header>
          <h5 class="m-0">{{ t('User Info', '用户信息') }}</h5>
        </template>
        <el-descriptions :column="3" border>
          <el-descriptions-item :label="t('User ID', '用户ID')">{{ form.userId }}</el-descriptions-item>
          <el-descriptions-item :label="t('Login Account', '登录账号')">{{ form.userName }}</el-descriptions-item>
          <el-descriptions-item :label="t('Nickname', '用户昵称')">{{ form.nickName }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card shadow="hover">
        <template #header>
          <h5 class="m-0">{{ t('Role Info', '角色信息') }}</h5>
        </template>

        <!-- 角色授权表格数据 -->
        <el-table
          ref="roleTableRef"
          v-loading="isLoading"
          :row-key="getRowKey"
          :data="roleList"
          height="400"
          @row-click="clickRow"
          stripe
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="50" align="center" :reserve-selection="true" :selectable="checkSelectable" />
          <el-table-column type="index" :label="t('No.', '序号')" width="60" align="center" />
          <el-table-column :label="t('roleId', '角色id')" prop="roleId" align="center" min-width="100" />
          <el-table-column :label="t('roleName', '角色名称')" prop="roleName" align="center" min-width="100" />
          <el-table-column :label="t('roleKey', '权限字符')" prop="roleKey" align="center" min-width="100" />
          <el-table-column :label="t('createTime', '创建时间')" prop="createTime" align="center" min-width="180" />
        </el-table>
      </el-card>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">{{ t('取消') }}</el-button>
        <el-button :loading="buttonLoading" type="primary" @click="submitForm">{{ t('确定') }}</el-button>
      </div>
    </template>
  </AModal>
</template>

<script setup lang="ts" name="AssignRoles">
import { getUserWithRoles } from '@/api/system/core/user/userApi'
import { assignUserRoles } from '@/api/system/core/userRole/userRoleAuthApi'
import type { SysRoleVo } from '@/api/system/core/role/roleTypes'
import type { SysUserBo } from '@/api/system/core/user/userTypes'
import { isTrue } from '@/utils/boolean'
import { showMsgSuccess } from '@/utils/modal'

const { t } = useI18n()

interface AssignRolesProps {
  modelValue: boolean
  userId?: string | number
}

interface AssignRolesEmits {
  (e: 'update:modelValue', value: boolean): void

  (e: 'success'): void
}

const props = withDefaults(defineProps<AssignRolesProps>(), {
  modelValue: false,
  userId: undefined
})

const emit = defineEmits<AssignRolesEmits>()

// =========== 对话框控制 ===========
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// =========== 用户授权角色表格数据相关 ===========
/**表格加载状态*/
const isLoading = ref(true)
/**角色数据列表*/
const roleList = ref<SysRoleVo[]>([])
/**表格实例*/
const roleTableRef = ref()
/**选中的角色项*/
const selectionItems = ref<SysRoleVo[]>([])

/** 表格多选事件处理 */
const handleSelectionChange = (selection: SysRoleVo[]) => {
  selectionItems.value = selection
}

/** 保存选中的数据编号 */
const getRowKey = (row: SysRoleVo): string => {
  return String(row.roleId)
}
/** 单击选中行数据 */
const clickRow = (row: SysRoleVo) => {
  if (checkSelectable(row)) {
    row.flag = !row.flag
    roleTableRef.value?.toggleRowSelection(row, row.flag)
  }
}
/** 检查角色状态 */
const checkSelectable = (row: SysRoleVo): boolean => {
  return isTrue(row.status)
}

// =========== 用户表单相关 ===========
/**表单数据*/
const form = ref<Partial<SysUserBo>>({
  nickName: undefined,
  userName: undefined,
  userId: undefined
})
/**按钮加载状态*/
const buttonLoading = ref(false)

/** 查询用户授权角色列表 */
const getList = async () => {
  if (!props.userId) return

  isLoading.value = true
  const [err, data] = await getUserWithRoles(props.userId)
  if (!err) {
    Object.assign(form.value, data.user)
    roleList.value = data.roles

    // 设置已授权的角色为选中状态
    await nextTick()
    const selectedRoles = roleList.value.filter((role) => role.flag)
    selectedRoles.forEach((role) => {
      roleTableRef.value?.toggleRowSelection(role, true)
    })
  }
  isLoading.value = false
}

/** 关闭对话框操作 */
const handleClose = () => {
  visible.value = false
  // 重置数据
  form.value = {
    nickName: undefined,
    userName: undefined,
    userId: undefined
  }
  roleList.value = []
  selectionItems.value = []
}

/** 提交用户角色授权表单 */
const submitForm = async () => {
  buttonLoading.value = true
  const userId = form.value.userId
  const roleIds = selectionItems.value.map((role) => role.roleId).join(',')
  const [err] = await assignUserRoles({ userId: userId as string, roleIds })
  if (!err) {
    showMsgSuccess(t('Assign Success', '分配成功'))
    handleClose()
    emit('success')
  }
  buttonLoading.value = false
}

// =========== 监听变化 ===========
watch(
  () => props.modelValue,
  (newValue) => {
    if (newValue && props.userId) {
      getList()
    }
  }
)
</script>
