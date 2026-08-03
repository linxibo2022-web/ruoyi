<!-- 租户套餐 -->
<template>
  <div>
    <!-- 租户套餐搜索栏 -->
    <ASearchForm ref="queryFormRef" v-model="queryParams" :visible="showSearch">
      <AFormInput :label="t('packageName', '套餐名称')" v-model="queryParams.packageName" prop="packageName" @input="handleQuery"></AFormInput>
    </ASearchForm>

    <el-card shadow="hover">
      <!-- 租户套餐工具栏 -->
      <template #header>
        <el-row :gutter="10" class="mb-2">
          <el-col :span="1.5" v-permi="['system:tenantPackage:add']">
            <el-button type="primary" plain icon="Plus" @click="handleAdd">
              {{ t('新增') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['system:tenantPackage:update']">
            <el-button type="success" plain icon="Edit" :disabled="selectionItems.length !== 1" @click="handleUpdate()">
              {{ t('修改') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['system:tenantPackage:delete']">
            <el-button type="danger" plain icon="Delete" :disabled="selectionItems.length === 0" @click="handleDelete()">
              {{ t('删除') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['system:tenantPackage:export']">
            <el-button type="warning" plain icon="Download" @click="handleExport">
              {{ t('导出') }}
            </el-button>
          </el-col>

          <TableToolbar v-model:showSearch="showSearch" @reset-query="resetQuery" @query-table="getList"></TableToolbar>
        </el-row>
      </template>

      <!-- 租户套餐表格数据 -->
      <el-table
        ref="tenantPackageTableRef"
        v-loading="isLoading"
        :data="tenantPackageList"
        :height="tableHeight"
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column v-if="false" :label="t('packageId', '租户套餐id')" prop="packageId" align="center" />
        <el-table-column :label="t('packageName', '套餐名称')" prop="packageName" align="center" min-width="100" />
        <el-table-column :label="t('remark', '备注')" prop="remark" align="center" min-width="100" />
        <el-table-column :label="t('status', '状态')" prop="status" align="center" min-width="100">
          <template #default="{ row }">
            <AFormSwitch v-model="row.status" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column :label="t('操作')" align="center" min-width="150" fixed="right">
          <template #default="{ row }">
            <el-tooltip :content="t('修改')" placement="top">
              <el-button v-permi="['system:tenantPackage:update']" link type="success" icon="Edit" @click="handleUpdate(row)"></el-button>
            </el-tooltip>
            <el-tooltip :content="t('删除')" placement="top">
              <el-button v-permi="['system:tenantPackage:delete']" link type="danger" icon="Delete" @click="handleDelete(row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 添加或修改租户套餐对话框 -->
    <AModal v-model="dialog.visible" :title="dialog.title" size="small" :loading="buttonLoading" @confirm="submitForm" @cancel="cancel">
      <el-form ref="tenantPackageFormRef" :model="form" :rules="rules" label-width="auto">
        <AFormInput :label="t('Package Name', '套餐名称')" v-model="form.packageName" prop="packageName" span="auto"></AFormInput>
        <el-form-item :label="t('Associated Menus', '关联菜单')">
          <el-checkbox v-model="menuExpand" @change="(val: boolean) => handleCheckedTreeExpand(val, 'menu')">{{
            t('Expand/Collapse', '展开/折叠')
          }}</el-checkbox>
          <el-checkbox v-model="menuNodeAll" @change="(val: boolean) => handleCheckedTreeNodeAll(val, 'menu')">{{
            t('Select All/None', '全选/全不选')
          }}</el-checkbox>
          <el-checkbox v-model="form.menuCheckStrictly" @change="(val: boolean) => handleCheckedTreeConnect(val, 'menu')">{{
            t('Parent-Child Link', '父子联动')
          }}</el-checkbox>
          <el-tree
            ref="menuTreeRef"
            class="tree-border"
            :data="menuOptions"
            show-checkbox
            node-key="id"
            :check-strictly="!form.menuCheckStrictly"
            :empty-text="t('Loading...', '加载中，请稍候')"
            :props="{ label: 'label', children: 'children' } as any"
          ></el-tree>
        </el-form-item>
        <AFormInput :label="t('remark', '备注')" v-model="form.remark" prop="remark" span="auto"></AFormInput>
      </el-form>
    </AModal>
  </div>
</template>

<script setup lang="ts" name="TenantPackage">
import { ref, onMounted, onActivated, nextTick } from 'vue'
import {
  pageTenantPackages,
  getTenantPackage,
  deleteTenantPackages,
  addTenantPackage,
  updateTenantPackage,
  changeTenantPackageStatus
} from '@/api/system/tenant/tenantPackage/tenantPackageApi'
import { getMenuTreeOptions, getTenantPackageMenuTree } from '@/api/system/core/menu/menuApi'
import type { SysTenantPackageQuery, SysTenantPackageBo, SysTenantPackageVo } from '@/api/system/tenant/tenantPackage/tenantPackageTypes'
import type { SysMenuTreeOption, SysRoleMenuTree } from '@/api/system/core/menu/menuTypes'
import { isTrue, toggleStatus } from '@/utils/boolean'
import { toValidate } from '@/utils/to'
import { showMsgSuccess, showConfirm } from '@/utils/modal'

const { t } = useI18n()

// 使用表格高度处理钩子
const { tableHeight, queryFormRef, showSearch } = useTableHeight()

// =========== 查询相关 ===========

/**查询参数对象*/
const queryParams = ref<SysTenantPackageQuery>({
  pageNum: 1,
  pageSize: 10,
  packageName: ''
})

/** 租户套餐搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1
  getList()
}

/** 租户套餐重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

// =========== 租户套餐表格数据相关 ===========
/**表格加载状态*/
const isLoading = ref(true)
/**数据列表*/
const tenantPackageList = ref<SysTenantPackageVo[]>([])
/**总记录数*/
const total = ref(0)
/**表格实例*/
const tenantPackageTableRef = ref()
/**选中的数据项*/
const selectionItems = ref<SysTenantPackageVo[]>([])

/** 表格多选事件处理 */
const handleSelectionChange = (selection: SysTenantPackageVo[]) => {
  selectionItems.value = selection
}

/** 查询租户套餐列表 */
const getList = async () => {
  isLoading.value = true
  const [err, data] = await pageTenantPackages(queryParams.value)
  if (!err) {
    tenantPackageList.value = data.records
    total.value = data.total
  }
  isLoading.value = false
}

/** 导出租户套餐数据 */
const handleExport = () => {
  useDownload().exportExcel(t('Tenant Package', '租户套餐'), '/system/tenant/exportTenantPackages', queryParams.value)
}

/** 删除租户套餐操作 */
const handleDelete = async (row?: SysTenantPackageVo) => {
  const idsToDelete = row ? [row.packageId] : selectionItems.value.map((item) => item.packageId)
  if (idsToDelete.length === 0) return
  const itemsToDelete = row ? row.packageName : selectionItems.value.map((item) => item.packageName).join(', ')
  const [confirmErr] = await showConfirm(`${t('Confirm delete', '是否确认删除')}${itemsToDelete}`)
  if (confirmErr) return

  const [deleteErr] = await deleteTenantPackages(idsToDelete)
  if (!deleteErr) {
    showMsgSuccess(t('message.deleteSuccess'))
    await getList()
  }
}

// =========== 菜单树相关 ===========
/**菜单树选项*/
const menuOptions = ref<SysMenuTreeOption[]>([])
/**菜单树展开状态*/
const menuExpand = ref(false)
/**菜单树全选状态*/
const menuNodeAll = ref(false)
/**菜单树引用*/
const menuTreeRef = ref<ElTreeInstance>()

/** 查询菜单树结构 */
const loadMenuTreeOptions = async () => {
  const [err, data] = await getMenuTreeOptions()
  if (!err) {
    menuOptions.value = data.filter((item) => item.id !== 6)
  }
}

/** 根据租户套餐ID查询菜单树结构 */
const loadTenantPackageMenuTree = async (packageId: string | number) => {
  const [err, data] = await getTenantPackageMenuTree(packageId)
  if (!err) {
    menuOptions.value = data.menus
    return data
  }
  return null
}

/** 获取所有菜单节点数据 */
const getMenuAllCheckedKeys = (): string => {
  // 目前被选中的菜单节点
  const checkedKeys = menuTreeRef.value?.getCheckedKeys() || []
  // 半选中的菜单节点
  const halfCheckedKeys = menuTreeRef.value?.getHalfCheckedKeys() || []

  // 合并所有选中的节点
  const allCheckedKeys = [...checkedKeys, ...halfCheckedKeys]

  // 将数组转换为逗号分隔的字符串
  return allCheckedKeys.join(',')
}

/** 树权限（展开/折叠） */
const handleCheckedTreeExpand = (value: boolean, type: string) => {
  if (type === 'menu') {
    const treeList = menuOptions.value
    for (let i = 0; i < treeList.length; i++) {
      if (menuTreeRef.value) {
        menuTreeRef.value.store.nodesMap[treeList[i].id].expanded = value
      }
    }
  }
}

/** 树权限（全选/全不选） */
const handleCheckedTreeNodeAll = (value: boolean, type: string) => {
  if (type === 'menu') {
    menuTreeRef.value?.setCheckedNodes(value ? (menuOptions.value as any) : [])
  }
}

/** 树权限（父子联动） */
const handleCheckedTreeConnect = (value: boolean, type: string) => {
  if (type === 'menu') {
    form.value.menuCheckStrictly = value
  }
}

/** 租户套餐启用禁用状态修改 */
const handleStatusChange = async (row: SysTenantPackageVo) => {
  const text = isTrue(row.status) ? t('Enable', '启用') : t('Disable', '停用')
  const [confirmErr] = await showConfirm(`${t('Confirm to', '是否确认')}${text}${row.packageName}?`)
  if (confirmErr) {
    row.status = toggleStatus(row.status)
    return
  }
  const [updateErr] = await changeTenantPackageStatus(row.packageId, row.status)
  if (updateErr) {
    row.status = toggleStatus(row.status)
    return
  }
  await getList() // 忽略列表刷新错误
  showMsgSuccess(`${text}${t('Success', '成功')}`)
}

// =========== 租户套餐表单相关 ===========
/**初始表单数据*/
const initFormData: SysTenantPackageBo = {
  packageId: undefined,
  packageName: '',
  menuIds: '',
  remark: '',
  menuCheckStrictly: true
}

/**表单引用*/
const tenantPackageFormRef = ref<ElFormInstance>()
/**表单提交按钮加载状态*/
const buttonLoading = ref(false)
/**对话框配置对象*/
const dialog = ref<DialogState>({
  visible: false,
  title: ''
})
/**表单数据对象*/
const form = ref<SysTenantPackageBo>({ ...initFormData })
/**表单校验规则*/
const rules = computed<ElFormRules>(() => ({
  packageName: [{ required: true, message: t('Package name is required', '套餐名称不能为空'), trigger: 'blur' }]
}))

/** 租户套餐表单重置 */
const reset = () => {
  menuTreeRef.value?.setCheckedKeys([])
  menuExpand.value = false
  menuNodeAll.value = false
  form.value = { ...initFormData }
  tenantPackageFormRef.value?.resetFields()
}

/** 取消租户套餐编辑 */
const cancel = () => {
  reset()
  dialog.value.visible = false
}

/** 新增租户套餐操作 */
const handleAdd = async () => {
  reset()
  await loadMenuTreeOptions()
  dialog.value.visible = true
  dialog.value.title = `${t('新增')}${t('Tenant Package', '租户套餐')}`
}

/** 修改租户套餐操作 */
const handleUpdate = async (row?: SysTenantPackageVo) => {
  reset()
  const itemToEdit = row || selectionItems.value[0]
  const [err, data] = await getTenantPackage(itemToEdit.packageId)
  if (!err) {
    Object.assign(form.value, data)

    const treeData = await loadTenantPackageMenuTree(itemToEdit.packageId)
    if (treeData) {
      dialog.value.visible = true
      dialog.value.title = `${t('修改')}${t('Tenant Package', '租户套餐')}`

      // 处理后端返回的逗号分隔的菜单ID字符串
      if (treeData.checkedKeys) {
        // 统一处理字符串或数组类型的 checkedKeys
        const checkedKeysStr = treeData.checkedKeys as string | string[]
        const checkedKeysArray = Array.isArray(checkedKeysStr)
          ? checkedKeysStr
          : typeof checkedKeysStr === 'string'
            ? checkedKeysStr.split(',').filter(Boolean)
            : []

        // 设置选中状态
        for (const v of checkedKeysArray) {
          await nextTick()
          menuTreeRef.value?.setChecked(v, true, false)
        }
      }
    }
  }
}

/** 提交租户套餐表单 */
const submitForm = async () => {
  const [validateErr] = await toValidate(tenantPackageFormRef)
  if (validateErr) return

  buttonLoading.value = true
  // 获取逗号分隔的菜单ID字符串
  form.value.menuIds = getMenuAllCheckedKeys()

  let err: Error | null
  if (form.value.packageId) {
    ;[err] = await updateTenantPackage(form.value)
  } else {
    ;[err] = await addTenantPackage(form.value)
  }

  if (!err) {
    showMsgSuccess(form.value.packageId ? t('message.updateSuccess') : t('message.addSuccess'))
    dialog.value.visible = false
    await getList()
  }
  buttonLoading.value = false
}

// =========== 生命周期 ===========
/**初始化租户套餐数据列表*/
onMounted(() => {
  getList()
})
/**页面激活时刷新租户套餐列表*/
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
  background: #ffffff none;
  border-radius: 4px;
  width: 100%;
  max-height: 400px;
  overflow-y: auto;
}
</style>
