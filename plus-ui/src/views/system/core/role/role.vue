<!-- 角色管理 -->
<template>
  <div>
    <!-- 角色搜索栏 -->
    <ASearchForm ref="queryFormRef" v-model="queryParams" :visible="showSearch">
      <AFormInput label="模糊搜索" prop="searchValue" v-model="queryParams.searchValue" @input="handleQuery"></AFormInput>
      <AFormSelect label="状态" v-model="queryParams.status" prop="status" :options="sys_enable_status" @change="handleQuery"></AFormSelect>
      <AFormDate label="创建时间" v-model="dateRange" prop="createTime" type="daterange" @change="handleQuery"></AFormDate>
    </ASearchForm>

    <el-card shadow="hover">
      <!-- 角色工具栏 -->
      <template #header>
        <el-row :gutter="10" class="mb-2">
          <el-col :span="1.5" v-permi="['system:role:add']">
            <el-button type="primary" plain icon="Plus" @click="handleAdd">
              {{ t('新增') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['system:role:update']">
            <el-button type="success" plain icon="Edit" :disabled="selectionItems.length !== 1" @click="handleUpdate()">
              {{ t('修改') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['system:role:delete']">
            <el-button type="danger" plain icon="Delete" :disabled="selectionItems.length === 0" @click="handleDelete()">
              {{ t('删除') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['system:role:export']">
            <el-button type="warning" plain icon="Download" @click="handleExport">
              {{ t('导出') }}
            </el-button>
          </el-col>

          <TableToolbar v-model:showSearch="showSearch" @reset-query="resetQuery" @query-table="getList"></TableToolbar>
        </el-row>
      </template>

      <!-- 角色表格数据 -->
      <el-table ref="roleTableRef" v-loading="isLoading" :data="roleList" :height="tableHeight" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" :selectable="(row) => !auth.isAnyAdmin(row.roleKey)" />
        <el-table-column v-if="false" :label="t('roleId', '角色id')" prop="roleId" align="center" />
        <el-table-column :label="t('roleName', '角色名称')" prop="roleName" align="center" />
        <el-table-column :label="t('roleKey', '权限字符')" prop="roleKey" align="center" />
        <el-table-column :label="t('roleSort', '显示顺序')" prop="roleSort" align="center" />
        <el-table-column :label="t('status', '状态')" prop="status" align="center">
          <template #default="{ row }">
            <AFormSwitch :disabled="auth.isAnyAdmin(row.roleKey)" v-model="row.status" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column :label="t('createTime', '创建时间')" prop="createTime" align="center" />
        <el-table-column :label="t('操作')" align="center" width="200" fixed="right">
          <template #default="{ row }">
            <template v-if="!auth.isAnyAdmin(row.roleKey)">
              <el-tooltip :content="t('修改')" placement="top">
                <el-button v-permi="['system:role:update']" link type="success" icon="Edit" @click="handleUpdate(row)"></el-button>
              </el-tooltip>
              <el-tooltip :content="t('删除')" placement="top">
                <el-button v-permi="['system:role:delete']" link type="danger" icon="Delete" @click="handleDelete(row)"></el-button>
              </el-tooltip>
              <el-tooltip :content="t('Data Permission', '数据权限')" placement="top">
                <el-button v-permi="['system:role:update']" link type="primary" icon="DataLine" @click="handleDataScope(row)"></el-button>
              </el-tooltip>
              <el-tooltip :content="t('Assign Users', '分配用户')" placement="top">
                <el-button v-permi="['system:role:update']" link type="primary" icon="User" @click="handleAssignUsers(row)"></el-button>
              </el-tooltip>
              <el-tooltip :content="t('Invite', '邀请注册')" placement="top">
                <el-button v-permi="['system:role:update']" link type="warning" icon="Share" @click="handleInvite(row)"></el-button>
              </el-tooltip>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 添加或修改角色对话框 -->
    <AModal v-model="dialog.visible" :title="dialog.title" size="small" :loading="buttonLoading" @confirm="submitForm" @cancel="cancel">
      <el-form ref="roleFormRef" :model="form" :rules="rules" label-width="auto">
        <AFormInput :label="t('Role Name', '角色名称')" v-model="form.roleName" prop="roleName"></AFormInput>
        <el-form-item prop="roleKey">
          <template #label>
            <span>
              <el-tooltip
                :content="t('Permission key defined in controller, e.g.: @SaCheckRole(admin)', '控制器中定义的权限字符，如：@SaCheckRole(admin)')"
                placement="top"
              >
                <el-icon><question-filled /></el-icon>
              </el-tooltip>
              {{ t('Permission Key', '权限字符') }}
            </span>
          </template>
          <el-input v-model="form.roleKey" :placeholder="t('Enter permission key', '请输入权限字符')" />
        </el-form-item>
        <AFormInput type="number" :label="t('Role Order', '角色顺序')" v-model="form.roleSort" prop="roleSort" :min="0"></AFormInput>
        <AFormRadio :label="t('Status', '状态')" v-model="form.status" :options="sys_enable_status"></AFormRadio>
        <AFormSelect
          v-model="form.dataScope"
          :label="t('Data Scope', '权限范围')"
          :options="filteredDataScopeOptions"
          width="100%"
          @change="dataScopeSelectChange"
        ></AFormSelect>
        <el-form-item v-show="form.dataScope === '2'" :label="t('Data Permission', '数据权限')">
          <el-checkbox v-model="deptExpand" @change="(val: boolean) => handleCheckedTreeExpand(val, 'dept')"
            >{{ t('Expand/Collapse', '展开/折叠') }}
          </el-checkbox>
          <el-checkbox v-model="deptNodeAll" @change="(val: boolean) => handleCheckedTreeNodeAll(val, 'dept')">
            {{ t('Select All/None', '全选/全不选') }}
          </el-checkbox>
          <el-checkbox v-model="form.deptCheckStrictly" @change="(val: boolean) => handleCheckedTreeConnect(val, 'dept')"
            >{{ t('Parent-Child Link', '父子联动') }}
          </el-checkbox>
          <el-tree
            ref="deptRef"
            class="tree-border"
            :data="deptOptions"
            show-checkbox
            default-expand-all
            node-key="id"
            :check-strictly="!form.deptCheckStrictly"
            :empty-text="t('Loading...', '加载中，请稍候')"
            :props="{ label: 'label', children: 'children' }"
          ></el-tree>
        </el-form-item>
        <el-form-item :label="t('Menu Permission', '菜单权限')">
          <el-checkbox v-model="menuExpand" @change="(val: boolean) => handleCheckedTreeExpand(val, 'menu')"
            >{{ t('Expand/Collapse', '展开/折叠') }}
          </el-checkbox>
          <el-checkbox v-model="menuNodeAll" @change="(val: boolean) => handleCheckedTreeNodeAll(val, 'menu')">
            {{ t('Select All/None', '全选/全不选') }}
          </el-checkbox>
          <el-checkbox v-model="form.menuCheckStrictly" @change="(val: boolean) => handleCheckedTreeConnect(val, 'menu')"
            >{{ t('Parent-Child Link', '父子联动') }}
          </el-checkbox>
          <el-tree
            ref="menuRef"
            class="tree-border"
            :data="menuOptions"
            show-checkbox
            node-key="id"
            :check-strictly="!form.menuCheckStrictly"
            :empty-text="t('Loading...', '加载中，请稍候')"
            :props="{ label: 'label', children: 'children' }"
          ></el-tree>
        </el-form-item>
        <AFormInput type="textarea" :label="t('Remark', '备注')" v-model="form.remark" prop="remark"></AFormInput>
      </el-form>
    </AModal>

    <!-- 分配角色数据权限对话框 -->
    <AModal
      v-model="openDataScope"
      :title="dialog.title"
      size="small"
      :loading="scopeButtonLoading"
      @confirm="submitDataScope"
      @cancel="cancelDataScope"
    >
      <el-form ref="dataScopeRef" :model="form" label-width="auto">
        <AFormInput :label="t('Role Name', '角色名称')" v-model="form.roleName" disabled></AFormInput>
        <AFormInput :label="t('Permission Key', '权限字符')" v-model="form.roleKey" disabled></AFormInput>
        <AFormSelect
          v-model="form.dataScope"
          :label="t('Data Scope', '权限范围')"
          :options="filteredDataScopeOptions"
          :disabled="isDataScopeExceedsUserPermission"
          :tooltip="
            isDataScopeExceedsUserPermission
              ? t('Data scope exceeds your permission, cannot modify', '该角色的数据权限超出您的权限范围，无法修改')
              : ''
          "
          width="100%"
          @change="dataScopeSelectChange"
        ></AFormSelect>
        <el-form-item v-show="form.dataScope === '2'" :label="t('Data Permission', '数据权限')">
          <el-checkbox v-model="deptExpand" @change="(val: boolean) => handleCheckedTreeExpand(val, 'dept')"
            >{{ t('Expand/Collapse', '展开/折叠') }}
          </el-checkbox>
          <el-checkbox v-model="deptNodeAll" @change="(val: boolean) => handleCheckedTreeNodeAll(val, 'dept')">
            {{ t('Select All/None', '全选/全不选') }}
          </el-checkbox>
          <el-checkbox v-model="form.deptCheckStrictly" @change="(val: boolean) => handleCheckedTreeConnect(val, 'dept')"
            >{{ t('Parent-Child Link', '父子联动') }}
          </el-checkbox>
          <el-tree
            ref="deptRef"
            class="tree-border"
            :data="deptOptions"
            show-checkbox
            default-expand-all
            node-key="id"
            :check-strictly="!form.deptCheckStrictly"
            :empty-text="t('Loading...', '加载中，请稍候')"
            :props="{ label: 'label', children: 'children' }"
          ></el-tree>
        </el-form-item>
      </el-form>
    </AModal>

    <!-- 分配用户对话框 -->
    <AssignUsers v-model="assignUsersDialog.visible" :roleId="assignUsersDialog.roleId" @success="handleAssignUsersSuccess" />

    <!-- 角色邀请组件 -->
    <RoleInvite ref="roleInviteRef" @success="handleInviteSuccess" />
  </div>
</template>

<script setup lang="ts" name="Role">
import {
  addRole,
  changeRoleStatus,
  updateRoleDataScope,
  deleteRoles,
  getRole,
  pageRoles,
  updateRole,
  getRoleDeptTree
} from '@/api/system/core/role/roleApi'
import { getDeptTreeOptions } from '@/api/system/core/dept/deptApi'
import { getRoleMenuTree, getMenuTreeOptions } from '@/api/system/core/menu/menuApi'
import type { SysRoleQuery, SysRoleBo, SysRoleVo, SysDeptTreeOption } from '@/api/system/core/role/roleTypes'
import type { SysMenuTreeOption, SysRoleMenuTree } from '@/api/system/core/menu/menuTypes'
import { isTrue, toggleStatus } from '@/utils/boolean'
import { addDateRange } from '@/utils/date'
import { toValidate } from '@/utils/to'
import { showMsgSuccess, showConfirm } from '@/utils/modal'
import AssignUsers from './AssignUsers.vue'
import RoleInvite from './RoleInvite.vue'
import type { SysDeptTreeVo } from '@/api/system/core/dept/deptTypes'

const { t } = useI18n()

const auth = useAuth()

const userStore = useUserStore()

/**字典数据*/
const { sys_enable_status, sys_data_scope } = useDict(DictTypes.sys_enable_status, DictTypes.sys_data_scope)

/**
 * 数据权限等级映射（用于权限比较）
 * 将数据权限值映射为权限等级，等级越小权限越大
 * - 3（本部门）和 4（本部门及以下）视为同级权限
 * - 5（仅本人）和 6（部门及以下或本人）视为同级权限
 */
const getDataScopeLevel = (dataScope: string): number => {
  const levelMap: Record<string, number> = {
    '1': 1, // 全部数据权限
    '2': 2, // 自定义数据权限
    '3': 3, // 本部门数据权限
    '4': 3, // 本部门及以下数据权限（与本部门同级）
    '5': 4, // 仅本人数据权限
    '6': 4 // 部门及以下或本人数据权限（与仅本人同级）
  }
  return levelMap[dataScope] ?? 99
}

/**
 * 获取当前用户的最高数据权限等级
 */
const userHighestDataScopeLevel = computed(() => {
  const roles = userStore.userInfo?.roles || []
  if (roles.length === 0) return 4 // 默认仅本人级别
  // 找出用户所有角色中最高的数据权限等级（数值最小）
  return (
    Math.min(
      ...roles
        .map((role) => role.dataScope)
        .filter((scope) => scope)
        .map((scope) => getDataScopeLevel(scope))
    ) || 4
  )
})

/**
 * 获取当前用户的最高数据权限值（用于默认值设置）
 */
const userHighestDataScope = computed(() => {
  const roles = userStore.userInfo?.roles || []
  if (roles.length === 0) return '5'
  return (
    roles
      .map((role) => role.dataScope)
      .filter((scope) => scope)
      .sort((a, b) => getDataScopeLevel(a) - getDataScopeLevel(b))
      .at(0) || '5'
  )
})

/**
 * 过滤后的数据权限选项
 * 禁用超出当前用户权限的选项
 */
const filteredDataScopeOptions = computed(() => {
  if (!sys_data_scope.value) return []
  return sys_data_scope.value.map((item: any) => ({
    ...item,
    disabled: getDataScopeLevel(item.value) < userHighestDataScopeLevel.value
  }))
})

/**
 * 判断当前角色的数据权限是否超出用户的最高权限
 * 如果超出，则禁用整个下拉框，不允许修改
 */
const isDataScopeExceedsUserPermission = computed(() => {
  if (!form.value.dataScope) return false
  return getDataScopeLevel(form.value.dataScope) < userHighestDataScopeLevel.value
})

// 使用表格高度处理钩子
const { tableHeight, queryFormRef, showSearch } = useTableHeight()

// =========== 查询相关 ===========

/**查询参数对象*/
const queryParams = ref<SysRoleQuery>({
  pageNum: 1,
  pageSize: 10,
  roleName: '',
  roleKey: '',
  status: ''
})
/**日期范围*/
const dateRange = ref<[ElDateModelType, ElDateModelType]>(['', ''])

/** 角色搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1
  getList()
}

/** 角色重置按钮操作 */
const resetQuery = () => {
  dateRange.value = ['', '']
  queryFormRef.value?.resetFields()
  handleQuery()
}

// =========== 角色表格数据相关 ===========
/**表格加载状态*/
const isLoading = ref(true)
/**数据列表*/
const roleList = ref<SysRoleVo[]>([])
/**总记录数*/
const total = ref(0)
/**表格实例*/
const roleTableRef = ref()
/**选中的数据项*/
const selectionItems = ref<SysRoleVo[]>([])
/**表单加载状态*/
const buttonLoading = ref(false)
/**数据权限表单加载状态*/
const scopeButtonLoading = ref(false)

/** 表格多选事件处理 */
const handleSelectionChange = (selection: SysRoleVo[]) => {
  selectionItems.value = selection
}

/** 查询角色列表 */
const getList = async () => {
  isLoading.value = true
  const [err, data] = await pageRoles(addDateRange(queryParams.value, dateRange.value))
  if (!err) {
    roleList.value = data.records
    total.value = data.total
  }
  isLoading.value = false
}

/** 删除角色操作 */
const handleDelete = async (row?: SysRoleVo) => {
  const idsToDelete = row ? [row.roleId] : selectionItems.value.map((item) => item.roleId)
  if (idsToDelete.length === 0) return
  const itemsToDelete = row ? `${row.roleName}(${row.roleKey})` : selectionItems.value.map((item) => `${item.roleName}(${item.roleKey})`).join(', ')
  const confirmMsg = t(`Are you sure to delete ${itemsToDelete}?`, `是否确认删除 ${itemsToDelete}？`)
  const [confirmErr] = await showConfirm(confirmMsg)
  if (confirmErr) return

  const [deleteErr] = await deleteRoles(idsToDelete)
  if (!deleteErr) {
    showMsgSuccess(t('message.deleteSuccess'))
    await getList()
  }
}

/** 导出角色数据 */
const handleExport = () => {
  useDownload().exportExcel(t('Role Info', '角色信息'), '/system/role/exportRoles', queryParams.value)
}

/** 角色状态修改 */
const handleStatusChange = async (row: SysRoleVo) => {
  const text = isTrue(row.status) ? t('Enable', '启用') : t('Disable', '停用')
  const confirmMsg = t(`Are you sure to "${text}" role "${row.roleName}"?`, `确认要"${text}""${row.roleName}"角色吗？`)
  const [confirmErr] = await showConfirm(confirmMsg)
  if (confirmErr) {
    row.status = toggleStatus(row.status)
    return
  }
  const [changeErr] = await changeRoleStatus(row)
  if (changeErr) {
    row.status = toggleStatus(row.status)
    return
  }
  showMsgSuccess(t(`${text} successfully`, `${text}成功`))
}

// =========== 分配用户相关 ===========
/**分配用户对话框配置*/
const assignUsersDialog = ref<{ visible: boolean; roleId: string | number }>({
  visible: false,
  roleId: ''
})

/** 打开分配用户对话框 */
const handleAssignUsers = (row: SysRoleVo) => {
  assignUsersDialog.value.roleId = row.roleId
  assignUsersDialog.value.visible = true
}

/** 分配用户成功回调 */
const handleAssignUsersSuccess = () => {
  getList()
}

// =========== 邀请功能相关 ===========
/**邀请组件引用*/
const roleInviteRef = ref<InstanceType<typeof RoleInvite>>()

/** 处理邀请操作 */
const handleInvite = (row: SysRoleVo) => {
  roleInviteRef.value?.openInviteDialog(row)
}

/** 邀请成功回调 */
const handleInviteSuccess = () => {
  // 这里可以执行一些成功后的操作，比如刷新数据等
  // getList()
}

// =========== 菜单权限相关 ===========
/**菜单树选项*/
const menuOptions = ref<SysMenuTreeOption[]>([])
/**菜单是否展开*/
const menuExpand = ref(false)
/**菜单是否全选*/
const menuNodeAll = ref(false)
/**菜单树引用*/
const menuRef = ref()

/** 查询菜单树结构 */
const loadMenuTreeOptions = async () => {
  const [err, data] = await getMenuTreeOptions()
  if (!err) {
    menuOptions.value = data
  }
}

/** 根据角色ID查询菜单树结构 */
const getRoleMenuTreeSelect = async (roleId: string | number) => {
  const [err, data] = await getRoleMenuTree(roleId)
  if (!err) {
    menuOptions.value = data.menus
    return data
  }
  return { menus: [], checkedKeys: [] } as SysRoleMenuTree
}

/** 所有菜单节点数据 */
const getMenuAllCheckedKeys = (): any => {
  // 目前被选中的菜单节点
  const checkedKeys = menuRef.value?.getCheckedKeys()
  // 半选中的菜单节点
  const halfCheckedKeys = menuRef.value?.getHalfCheckedKeys()
  if (halfCheckedKeys) {
    // 使用扩展运算符替代 .apply()
    checkedKeys?.push(...halfCheckedKeys)
  }
  return checkedKeys
}

// =========== 部门树相关 ===========
/**部门树选项*/
const deptOptions = ref<SysDeptTreeOption[]>([])
/**部门是否展开*/
const deptExpand = ref(true)
/**部门是否全选*/
const deptNodeAll = ref(false)
/**部门树引用*/
const deptRef = ref()

/** 根据角色ID查询部门树结构 */
const getRoleDeptTreeSelect = async (roleId: string | number) => {
  const [err, data] = await getRoleDeptTree(roleId)
  if (!err) {
    deptOptions.value = data.depts
    return data
  }
  return { depts: [], checkedKeys: [] }
}

/** 加载部门树选项 */
const loadDeptTreeOptions = async () => {
  const [err, data] = await getDeptTreeOptions()
  if (!err) {
    deptOptions.value = data as SysDeptTreeOption[]
  }
}

/** 所有部门节点数据 */
const getDeptAllCheckedKeys = (): any => {
  // 目前被选中的部门节点
  const checkedKeys = deptRef.value?.getCheckedKeys()
  // 半选中的部门节点
  const halfCheckedKeys = deptRef.value?.getHalfCheckedKeys()
  if (halfCheckedKeys) {
    checkedKeys?.push(...halfCheckedKeys)
  }
  return checkedKeys
}

/** 选择角色权限范围触发 */
const dataScopeSelectChange = (value: string) => {
  if (value !== '2') {
    deptRef.value?.setCheckedKeys([])
  }
}

// =========== 树通用操作 ===========
/** 树权限（展开/折叠）*/
const handleCheckedTreeExpand = (value: boolean, type: string) => {
  if (type == 'menu') {
    const treeList = menuOptions.value
    for (let i = 0; i < treeList.length; i++) {
      if (menuRef.value) {
        menuRef.value.store.nodesMap[treeList[i].id].expanded = value
      }
    }
  } else if (type == 'dept') {
    const treeList = deptOptions.value
    for (let i = 0; i < treeList.length; i++) {
      if (deptRef.value) {
        deptRef.value.store.nodesMap[treeList[i].id].expanded = value
      }
    }
  }
}

/** 树权限（全选/全不选） */
const handleCheckedTreeNodeAll = (value: boolean, type: string) => {
  if (type == 'menu') {
    menuRef.value?.setCheckedNodes(value ? (menuOptions.value as any) : [])
  } else if (type == 'dept') {
    deptRef.value?.setCheckedNodes(value ? (deptOptions.value as any) : [])
  }
}

/** 树权限（父子联动） */
const handleCheckedTreeConnect = (value: boolean, type: string) => {
  if (type == 'menu') {
    form.value.menuCheckStrictly = value
  } else if (type == 'dept') {
    form.value.deptCheckStrictly = value
  }
}

// =========== 角色表单相关 ===========
/**初始表单数据*/
const initForm: SysRoleBo = {
  roleId: undefined,
  roleSort: 1,
  status: '1',
  roleName: '',
  roleKey: '',
  menuCheckStrictly: true,
  deptCheckStrictly: true,
  remark: '',
  dataScope: undefined, // 默认不设置，由 getDefaultDataScope 计算
  menuIds: [],
  deptIds: []
}

/**
 * 获取默认数据权限
 * 使用用户的最高数据权限作为默认值，避免越权
 */
const getDefaultDataScope = () => {
  return userHighestDataScope.value
}

/**表单引用*/
const roleFormRef = ref<ElFormInstance>()
/**数据权限表单引用*/
const dataScopeRef = ref<ElFormInstance>()
/**对话框配置对象*/
const dialog = ref<DialogState>({
  visible: false,
  title: ''
})
/**数据权限对话框是否打开*/
const openDataScope = ref(false)

/**表单数据对象*/
const form = ref<SysRoleBo>({ ...initForm })
/**表单校验规则*/
const rules = ref<ElFormRules>({
  roleName: [{ required: true, message: t('roleName cannot be empty', '角色名称不能为空'), trigger: 'blur' }],
  roleKey: [{ required: true, message: t('roleKey cannot be empty', '权限字符不能为空'), trigger: 'blur' }],
  roleSort: [{ required: true, message: t('roleSort cannot be empty', '角色顺序不能为空'), trigger: 'blur' }]
})

/** 角色表单重置 */
const reset = () => {
  menuRef.value?.setCheckedKeys([])
  menuExpand.value = false
  menuNodeAll.value = false
  deptExpand.value = true
  deptNodeAll.value = false
  form.value = { ...initForm }
  roleFormRef.value?.resetFields()
}

/** 取消角色编辑 */
const cancel = () => {
  reset()
  dialog.value.visible = false
}

/** 新增角色操作 */
const handleAdd = async () => {
  reset()
  // 新增时设置默认数据权限为用户的最高权限
  form.value.dataScope = getDefaultDataScope()
  // 并行加载菜单树和部门树
  await Promise.all([loadMenuTreeOptions(), loadDeptTreeOptions()])
  dialog.value.visible = true
  dialog.value.title = t('Add Role', '新增角色')
}

/** 修改角色操作 */
const handleUpdate = async (row?: SysRoleVo) => {
  reset()
  const roleId = row?.roleId || selectionItems.value[0]?.roleId

  // 获取角色数据
  const [roleErr, roleData] = await getRole(roleId)
  if (roleErr) return

  Object.assign(form.value, roleData)
  form.value.roleSort = Number(form.value.roleSort)

  // 并行获取角色菜单数据和部门树数据
  const [menuData, deptData] = await Promise.all([getRoleMenuTreeSelect(roleId), getRoleDeptTreeSelect(roleId)])

  dialog.value.title = t('Edit Role', '修改角色')
  dialog.value.visible = true

  // 设置选中的菜单节点和部门节点
  await nextTick()
  menuRef.value?.setCheckedKeys(menuData.checkedKeys)
  deptRef.value?.setCheckedKeys(deptData.checkedKeys)
}

/** 提交角色表单 */
const submitForm = async () => {
  const [validateErr] = await toValidate(roleFormRef)
  if (validateErr) return

  buttonLoading.value = true

  // 获取选中的菜单ID
  form.value.menuIds = getMenuAllCheckedKeys()
  // 获取选中的部门ID（自定义数据权限时需要）
  if (form.value.dataScope === '2') {
    form.value.deptIds = getDeptAllCheckedKeys()
  }

  let err: Error
  // 添加或更新角色
  if (form.value.roleId) {
    ;[err] = await updateRole(form.value)
  } else {
    ;[err] = await addRole(form.value)
  }
  if (!err) {
    showMsgSuccess(form.value.roleId ? t('message.updateSuccess') : t('message.addSuccess'))
    dialog.value.visible = false
    await getList()
  }
  buttonLoading.value = false
}

/** 分配数据权限操作 */
const handleDataScope = async (row: SysRoleVo) => {
  // 获取角色数据
  const [roleErr, roleData] = await getRole(row.roleId)
  if (roleErr) return

  Object.assign(form.value, roleData)

  // 获取角色部门树数据
  const deptData = await getRoleDeptTreeSelect(row.roleId)

  openDataScope.value = true
  dialog.value.title = t('Assign Data Permission', '分配数据权限')

  // 设置选中的部门节点
  await nextTick()
  deptRef.value?.setCheckedKeys(deptData.checkedKeys)
}

/** 提交数据权限 */
const submitDataScope = async () => {
  if (!form.value.roleId) return

  scopeButtonLoading.value = true

  form.value.deptIds = getDeptAllCheckedKeys()
  const [updateErr] = await updateRoleDataScope(form.value)
  if (!updateErr) {
    showMsgSuccess(t('message.updateSuccess'))
    openDataScope.value = false
    await getList()
  }
  scopeButtonLoading.value = false
}

/** 取消数据权限 */
const cancelDataScope = () => {
  dataScopeRef.value?.resetFields()
  form.value = { ...initForm }
  openDataScope.value = false
}

// =========== 生命周期 ===========
/**初始化角色数据列表*/
onMounted(() => {
  getList()
})

/**页面激活时刷新角色列表*/
onActivated(() => {
  if (isLoading.value) return
  getList()
})
</script>

<style lang="scss" scoped>
/* 树形控件边框样式 */
.tree-border {
  margin-top: 5px;
  border: 1px solid var(--el-border-color-lighter);
  background: var(--el-bg-color) none;
  border-radius: 4px;
  width: 100%;
}
</style>
