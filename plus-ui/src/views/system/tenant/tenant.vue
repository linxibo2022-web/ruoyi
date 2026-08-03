<!-- 租户管理 -->
<template>
  <div>
    <!-- 租户搜索栏 -->
    <ASearchForm ref="queryFormRef" v-model="queryParams" :visible="showSearch">
      <AFormInput label="模糊搜索" prop="searchValue" v-model="queryParams.searchValue" @input="handleQuery"></AFormInput>
      <AFormSelect label="状态" v-model="queryParams.status" prop="status" :options="sys_enable_status" @change="handleQuery"></AFormSelect>
    </ASearchForm>

    <el-card shadow="hover">
      <!-- 租户工具栏 -->
      <template #header>
        <el-row :gutter="10" class="mb-2">
          <el-col :span="1.5" v-permi="['system:tenant:add']">
            <el-button type="primary" plain icon="Plus" @click="handleAdd">
              {{ t('新增') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['system:tenant:update']">
            <el-button type="success" plain icon="Edit" :disabled="selectionItems.length !== 1" @click="handleUpdate()">
              {{ t('修改') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['system:tenant:delete']">
            <el-button type="danger" plain icon="Delete" :disabled="selectionItems.length === 0" @click="handleDelete()">
              {{ t('删除') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['system:tenant:export']">
            <el-button type="warning" plain icon="Download" @click="handleExport">
              {{ t('导出') }}
            </el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-if="userId === 1" type="success" plain icon="Refresh" @click="handleSyncTenantRoles">
              {{ t('button.syncTenantRoles') }}
            </el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-if="userId === 1" type="success" plain icon="Refresh" @click="handleSyncTenantDicts">
              {{ t('button.syncTenantDicts') }}
            </el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-if="userId === 1" type="success" plain icon="Refresh" @click="handleSyncTenantConfigs">
              {{ t('button.syncTenantConfigs') }}
            </el-button>
          </el-col>

          <TableToolbar v-model:showSearch="showSearch" @reset-query="resetQuery" @query-table="getList"></TableToolbar>
        </el-row>
      </template>

      <!-- 租户表格数据 -->
      <el-table ref="tenantTableRef" v-loading="isLoading" :data="tenantList" :height="tableHeight" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column v-if="false" :label="t('id', 'id')" prop="id" align="center" />
        <el-table-column :label="t('tenantId', '租户ID')" prop="tenantId" align="center" width="100">
          <template #default="{ row }">
            <span class="cursor-pointer" @click="copy(row.tenantId)">{{ row.tenantId }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="t('contactUserName', '联系人')" prop="contactUserName" align="center" min-width="100" />
        <el-table-column :label="t('contactPhone', '联系电话')" prop="contactPhone" align="center" width="110">
          <template #default="{ row }">
            <span class="cursor-pointer" @click="copy(row.contactPhone)">{{ row.contactPhone }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="t('companyName', '企业名称')" prop="companyName" align="center" min-width="100" />
        <el-table-column :label="t('licenseNumber', '社会信用代码')" prop="licenseNumber" align="center" min-width="110" />
        <el-table-column :label="t('expireTime', '过期时间')" prop="expireTime" align="center" width="105" />
        <el-table-column :label="t('tenantStatus', '租户状态')" prop="status" align="center" min-width="100">
          <template #default="{ row }">
            <AFormSwitch v-model="row.status" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column :label="t('createTime', '创建时间')" prop="createTime" align="center" width="105" />
        <el-table-column :label="t('操作')" align="center" min-width="150" fixed="right">
          <template #default="{ row }">
            <el-tooltip :content="t('修改')" placement="top">
              <el-button v-permi="['system:tenant:update']" link type="success" icon="Edit" @click="handleUpdate(row)"></el-button>
            </el-tooltip>
            <el-tooltip :content="t('syncPackage', '同步套餐')" placement="top">
              <el-button v-permi="['system:tenant:update']" link type="primary" icon="Refresh" @click="handleSyncTenantPackage(row)"></el-button>
            </el-tooltip>
            <el-tooltip :content="t('copyTenantUrl', '复制租户网址')" placement="top">
              <el-button v-permi="['system:tenant:query']" link type="success" icon="Connection" @click="copyTenantUrl(row)"></el-button>
            </el-tooltip>
            <el-tooltip :content="t('删除')" placement="top">
              <el-button v-permi="['system:tenant:delete']" link type="danger" icon="Delete" @click="handleDelete(row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 添加或修改租户对话框 -->
    <AModal v-model="dialog.visible" :title="dialog.title" size="small" :loading="buttonLoading" @confirm="submitForm" @cancel="cancel">
      <el-form ref="tenantFormRef" :model="form" :rules="rules" label-width="auto">
        <AFormInput :label="t('companyName', '企业名称')" v-model="form.companyName" prop="companyName" span="auto"></AFormInput>
        <AFormInput :label="t('contactUserName', '联系人')" v-model="form.contactUserName" prop="contactUserName" span="auto"></AFormInput>
        <AFormInput :label="t('contactPhone', '联系电话')" v-model="form.contactPhone" prop="contactPhone" span="auto"></AFormInput>
        <AFormInput v-if="!form.id" :label="t('userName', '用户名')" v-model="form.userName" prop="userName" span="auto"></AFormInput>
        <AFormInput v-if="!form.id" :label="t('password', '密码')" v-model="form.password" prop="password" type="password" span="auto"></AFormInput>
        <el-form-item :label="t('packageId', '租户套餐')" prop="packageId">
          <el-select v-model="form.packageId" :disabled="!!form.tenantId" :placeholder="t('Please select package', '请选择租户套餐')" clearable style="width: 100%">
            <el-option v-for="item in packageList" :key="item.packageId" :label="item.packageName" :value="item.packageId" />
          </el-select>
        </el-form-item>
        <AFormDate width="100%" :label="t('expireTime', '过期时间')" v-model="form.expireTime" type="datetime"></AFormDate>
        <AFormInput
          :label="t('accountCount', '用户数量')"
          :placeholder="t('Enter account limit', '请输入用户数量(-1或者为空代表不限制)')"
          v-model="form.accountCount"
          prop="accountCount"
          span="auto"
        ></AFormInput>
        <AFormInput :label="t('domain', '绑定域名')" :placeholder="t('Enter domain', '请输入绑定域名(如ruoyikj.top)')" v-model="form.domain" prop="domain" span="auto"></AFormInput>
        <AFormInput :label="t('address', '企业地址')" v-model="form.address" prop="address" span="auto"></AFormInput>
        <AFormInput :label="t('licenseNumber', '社会信用代码')" v-model="form.licenseNumber" prop="licenseNumber" span="auto"></AFormInput>
        <AFormInput type="textarea" :label="t('intro', '企业简介')" v-model="form.intro" prop="intro" span="auto"></AFormInput>
        <AFormInput :label="t('remark', '备注')" v-model="form.remark" prop="remark" span="auto"></AFormInput>
      </el-form>
    </AModal>
  </div>
</template>

<script setup lang="ts" name="Tenant">
import {
  pageTenants,
  getTenant,
  deleteTenants,
  addTenant,
  updateTenant,
  changeTenantStatus,
  syncTenantPackage,
  syncTenantDicts,
  syncTenantRoles,
  syncTenantConfigs
} from '@/api/system/tenant/tenant/tenantApi'
import { listTenantPackages } from '@/api/system/tenant/tenantPackage/tenantPackageApi'
import type { SysTenantQuery, SysTenantBo, SysTenantVo } from '@/api/system/tenant/tenant/tenantTypes'
import type { SysTenantPackageVo } from '@/api/system/tenant/tenantPackage/tenantPackageTypes'
import { copy } from '@/utils/function'
import { isTrue, toggleStatus } from '@/utils/boolean'
import { toValidate } from '@/utils/to'
import { showMsgSuccess, showConfirm } from '@/utils/modal'

const { sys_enable_status } = useDict(DictTypes.sys_enable_status)

const { t, isChinese } = useI18n()

// 使用表格高度处理钩子
const { tableHeight, queryFormRef, showSearch } = useTableHeight()

// 获取用户Store
const userStore = useUserStore()
const userId = ref(userStore.userInfo?.userId)

// =========== 查询相关 ===========

/**查询参数对象*/
const queryParams = ref<SysTenantQuery>({
  pageNum: 1,
  pageSize: 10,
  tenantId: '',
  contactUserName: '',
  contactPhone: '',
  companyName: ''
})

/** 租户搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1
  getList()
}

/** 租户重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

// =========== 租户表格数据相关 ===========
/**表格加载状态*/
const isLoading = ref(true)
/**数据列表*/
const tenantList = ref<SysTenantVo[]>([])
/**总记录数*/
const total = ref(0)
/**套餐列表*/
const packageList = ref<SysTenantPackageVo[]>([])
/**表格实例*/
const tenantTableRef = ref()
/**选中的数据项*/
const selectionItems = ref<SysTenantVo[]>([])

/** 表格多选事件处理 */
const handleSelectionChange = (selection: SysTenantVo[]) => {
  selectionItems.value = selection
}

/** 查询租户列表 */
const getList = async () => {
  isLoading.value = true
  const [err, data] = await pageTenants(queryParams.value)
  if (!err) {
    tenantList.value = data.records
    total.value = data.total
  }
  isLoading.value = false
}

/** 查询所有租户套餐 */
const getTenantPackage = async () => {
  const [err, data] = await listTenantPackages()
  if (!err) {
    packageList.value = data
  }
}

/** 租户状态修改 */
const handleStatusChange = async (row: SysTenantVo) => {
  const text = isTrue(row.status) ? t('Enable', '启用') : t('Disable', '停用')
  const confirmMsg = isChinese.value
    ? `确认要"${text}""${row.companyName}"租户吗？`
    : `Are you sure to ${text.toLowerCase()} tenant "${row.companyName}"?`
  const [confirmErr] = await showConfirm(confirmMsg)
  if (confirmErr) {
    row.status = toggleStatus(row.status)
    return
  }

  const [err] = await changeTenantStatus(row.id, row.tenantId, row.status)
  if (!err) {
    showMsgSuccess(text + t(' success', '成功'))
  } else {
    row.status = toggleStatus(row.status)
  }
}

/** 导出租户数据 */
const handleExport = () => {
  useDownload().exportExcel(t('Tenant Management', '租户管理'), '/system/tenant/exportTenants', queryParams.value)
}

/** 同步租户角色 */
const handleSyncTenantRoles = async () => {
  const [confirmErr] = await showConfirm(t('Confirm sync all tenant roles?', '确认要同步所有租户角色吗？'))
  if (confirmErr) return

  const [err] = await syncTenantRoles()
  if (!err) {
    showMsgSuccess(t('Sync tenant roles success', '同步租户角色成功'))
  }
}

/** 同步租户字典 */
const handleSyncTenantDicts = async () => {
  const [confirmErr] = await showConfirm(t('Confirm sync all tenant dicts?', '确认要同步所有租户字典吗？'))
  if (confirmErr) return

  const [err] = await syncTenantDicts()
  if (!err) {
    showMsgSuccess(t('Sync tenant dicts success', '同步租户字典成功'))
  }
}

/** 同步租户配置 */
const handleSyncTenantConfigs = async () => {
  const [confirmErr] = await showConfirm(t('Confirm sync all tenant configs?', '确认要同步所有租户参数配置吗？'))
  if (confirmErr) return

  const [err] = await syncTenantConfigs()
  if (!err) {
    showMsgSuccess(t('Sync tenant configs success', '同步租户参数配置成功'))
  }
}

/** 同步租户套餐按钮操作 */
const handleSyncTenantPackage = async (row: SysTenantVo) => {
  const confirmMsg = isChinese.value
    ? `是否确认同步租户套餐租户id为"${row.tenantId}"的数据项？`
    : `Confirm to sync package for tenant ID "${row.tenantId}"?`
  const [confirmErr] = await showConfirm(confirmMsg)
  if (confirmErr) return

  isLoading.value = true
  const [err] = await syncTenantPackage(row.tenantId, row.packageId)
  if (!err) {
    await getList()
    showMsgSuccess(t('Sync tenant package success', '同步租户套餐成功'))
  }
  isLoading.value = false
}

/** 复制租户访问地址*/
const copyTenantUrl = (row: SysTenantVo) => {
  copy(row.domain ? `http://${row.domain}` : `http://${window.location.host}?tenantId=${row.tenantId}`)
}

/** 删除租户操作 */
const handleDelete = async (row?: SysTenantVo) => {
  const idsToDelete = row ? [row.id] : selectionItems.value.map((item) => item.id)
  if (idsToDelete.length === 0) return
  const itemsToDelete = row ? row.companyName : selectionItems.value.map((item) => item.companyName).join(', ')
  const confirmMsg = isChinese.value ? `是否确认删除下列企业: "${itemsToDelete}"吗？` : `Confirm to delete company: "${itemsToDelete}"?`
  const [confirmErr] = await showConfirm(confirmMsg)
  if (confirmErr) return

  isLoading.value = true
  const [deleteErr] = await deleteTenants(idsToDelete)
  if (!deleteErr) {
    await getList()
    showMsgSuccess(t('message.deleteSuccess'))
  }
  isLoading.value = false
}

// =========== 租户表单相关 ===========
/**初始表单数据*/
const initFormData: SysTenantBo = {
  id: undefined,
  tenantId: undefined,
  contactUserName: '',
  contactPhone: '',
  userName: '',
  password: '',
  companyName: '',
  licenseNumber: '',
  domain: '',
  address: '',
  intro: '',
  remark: '',
  packageId: '',
  expireTime: '',
  accountCount: undefined,
  status: undefined
}

/**表单引用*/
const tenantFormRef = ref<ElFormInstance>()
/**表单提交按钮加载状态*/
const buttonLoading = ref(false)
/**对话框配置对象*/
const dialog = ref<DialogState>({
  visible: false,
  title: ''
})
/**表单数据对象*/
const form = ref<SysTenantBo>({ ...initFormData })
/**表单校验规则*/
const rules = computed<ElFormRules>(() => ({
  companyName: [{ required: true, message: t('Company name required', '企业名称不能为空'), trigger: 'blur' }],
  contactUserName: [{ required: true, message: t('Contact required', '联系人不能为空'), trigger: 'blur' }],
  contactPhone: [{ required: true, message: t('Phone required', '联系电话不能为空'), trigger: 'blur' }],
  userName: [
    { required: true, message: t('Username required', '用户名不能为空'), trigger: 'blur' },
    { min: 2, max: 20, message: t('Username length 2-20', '用户名称长度必须介于 2 和 20 之间'), trigger: 'blur' }
  ],
  password: [
    { required: true, message: t('Password required', '密码不能为空'), trigger: 'blur' },
    { min: 5, max: 20, message: t('Password length 5-20', '用户密码长度必须介于 5 和 20 之间'), trigger: 'blur' }
  ]
}))

/** 租户表单重置 */
const reset = () => {
  form.value = { ...initFormData }
  tenantFormRef.value?.resetFields()
}

/** 取消租户编辑 */
const cancel = () => {
  reset()
  dialog.value.visible = false
}

/** 新增租户操作 */
const handleAdd = async () => {
  reset()
  await getTenantPackage()
  dialog.value.visible = true
  dialog.value.title = `${t('新增')}${t('tenant', '租户')}`
}

/** 修改租户操作 */
const handleUpdate = async (row?: SysTenantVo) => {
  reset()
  await getTenantPackage()

  const itemToEdit = row || selectionItems.value[0]
  const [err, data] = await getTenant(itemToEdit.id)
  if (!err) {
    Object.assign(form.value, data)
    dialog.value.visible = true
    dialog.value.title = `${t('修改')}${t('tenant', '租户')}`
  }
}

/** 提交租户表单 */
const submitForm = async () => {
  const [validateErr] = await toValidate(tenantFormRef)
  if (validateErr) return

  buttonLoading.value = true
  let err: Error | null
  if (form.value.id) {
    ;[err] = await updateTenant(form.value)
  } else {
    ;[err] = await addTenant(form.value)
  }

  if (!err) {
    showMsgSuccess(form.value.id ? t('message.updateSuccess') : t('message.addSuccess'))
    dialog.value.visible = false
    await getList()
  }
  buttonLoading.value = false
}

// =========== 生命周期 ===========
/**初始化租户数据列表*/
onMounted(() => {
  getList()
})
/**页面激活时刷新租户列表*/
onActivated(() => {
  if (isLoading.value) return
  getList()
})
</script>
