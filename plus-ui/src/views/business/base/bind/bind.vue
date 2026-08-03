<!-- 账号绑定 -->
<template>
  <div>
    <!-- 账号绑定搜索栏 -->
    <ASearchForm ref="queryFormRef" v-model="queryParams" :visible="showSearch">
      <AFormInput label="模糊搜索" v-model="queryParams.searchValue" prop="searchValue" @input="handleQuery"></AFormInput>
      <AFormInput label="用户id" v-model="queryParams.userId" prop="userId" @input="handleQuery"></AFormInput>
      <AFormSelect
        label="平台类型"
        v-model="queryParams.platformType"
        prop="platformType"
        :options="sys_platform_type"
        @change="handleQuery"
      ></AFormSelect>
      <AFormDate label="创建时间" v-model="dateRangeCreateTime" prop="createTime" type="daterange" @change="handleQuery"></AFormDate>
    </ASearchForm>

    <el-card shadow="hover">
      <!-- 账号绑定工具栏 -->
      <template #header>
        <el-row :gutter="10" class="mb-2">
          <el-col :span="1.5" v-permi="['base:bind:add']">
            <el-button type="primary" plain icon="Plus" @click="handleAdd"> {{ t('新增') }}</el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['base:bind:update']">
            <el-button type="success" plain icon="Edit" :disabled="selectionItems.length !== 1" @click="handleUpdate()">
              {{ t('修改') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['base:bind:delete']">
            <el-button type="danger" plain icon="Delete" :disabled="selectionItems.length === 0" @click="handleDelete()">
              {{ t('删除') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['base:bind:import']">
            <AImportExcel
              v-slot="{ openImportExcel }"
              :title="t('Bind Data', '账号绑定')"
              templateUrl="/base/bind/templateBinds"
              importUrl="/base/bind/importBinds"
              @import-success="getList"
            >
              <el-button type="info" plain icon="Top" @click="openImportExcel">
                {{ t('导入') }}
              </el-button>
            </AImportExcel>
          </el-col>
          <el-col :span="1.5" v-permi="['base:bind:export']">
            <el-button type="warning" plain icon="Download" @click="handleExport">
              {{ t('导出') }}
            </el-button>
          </el-col>

          <TableToolbar
            v-model:showSearch="showSearch"
            @reset-query="resetQuery"
            @query-table="getList"
            :table-columns="detailFields"
            :table-data="bindList"
          ></TableToolbar>
        </el-row>

        <!-- 显示已选项 -->
        <!-- <ASelectionTags :items="selectionItems" :on-clear="selectionClear" @close="selectionRemove" /> -->
      </template>

      <!-- 账号绑定表格数据 -->
      <el-table ref="bindTableRef" v-loading="isLoading" :data="bindList" :height="tableHeight" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column v-if="true" :label="t('id', '账号绑定id')" prop="id" align="center" width="105" />
        <el-table-column :label="t('userId', '用户id')" prop="userId" align="center" width="105" />
        <el-table-column :label="t('platformType', '平台类型')" prop="platformType" align="center" width="120">
          <template #default="{ row }">
            <DictTag :options="sys_platform_type" :value="row.platformType" />
          </template>
        </el-table-column>
        <el-table-column :label="t('appid', 'appid')" prop="appid" align="center" />
        <el-table-column :label="t('unionid', 'unionid')" prop="unionid" align="center" min-width="110" />
        <el-table-column :label="t('openid', 'openid')" prop="openid" align="center" min-width="110" />
        <el-table-column :label="t('extraData', '扩展数据')" prop="extraData" align="center" />
        <el-table-column :label="t('createTime', '创建时间')" prop="createTime" align="center" width="105" />
        <el-table-column :label="t('updateTime', '更新时间')" prop="updateTime" align="center" width="105" />
        <el-table-column :label="t('remark', '备注')" prop="remark" align="center" />
        <el-table-column :label="t('操作')" align="center" fixed="right" width="120">
          <template #default="{ row }">
            <el-tooltip :content="t('查看')" placement="top">
              <el-button v-permi="['base:bind:query']" link type="primary" icon="View" @click="handleView(row)"></el-button>
            </el-tooltip>
            <el-tooltip :content="t('修改')" placement="top">
              <el-button v-permi="['base:bind:update']" link type="success" icon="Edit" @click="handleUpdate(row)"></el-button>
            </el-tooltip>
            <el-tooltip :content="t('删除')" placement="top">
              <el-button v-permi="['base:bind:delete']" link type="danger" icon="Delete" @click="handleDelete(row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 添加或修改账号绑定对话框 -->
    <AModal v-model="dialog.visible" :title="dialog.title" :loading="buttonLoading" @confirm="submitForm" @cancel="cancel">
      <el-form ref="bindFormRef" :model="form" :rules="rules" label-width="auto">
        <el-row :gutter="10">
          <AFormInput label="用户id" v-model="form.userId" prop="userId" span="auto"></AFormInput>
          <AFormSelect label="平台类型" v-model="form.platformType" prop="platformType" :options="sys_platform_type" span="auto"></AFormSelect>
          <AFormInput label="appid" v-model="form.appid" :maxlength="30" prop="appid" span="auto"></AFormInput>
          <AFormInput label="unionid" v-model="form.unionid" :maxlength="30" prop="unionid" span="auto"></AFormInput>
          <AFormInput label="openid" v-model="form.openid" :maxlength="30" prop="openid" span="auto"></AFormInput>
          <AFormInput label="扩展数据" v-model="form.extraData" :maxlength="255" prop="extraData" span="auto"></AFormInput>
          <AFormInput label="备注" v-model="form.remark" type="textarea" :maxlength="255" prop="remark" span="auto"></AFormInput>
        </el-row>
      </el-form>
    </AModal>

    <!-- 查看账号绑定详情对话框 -->
    <ADetail v-model="viewDialog.visible" :title="viewDialog.title" :data="viewData" :fields="detailFields" />
  </div>
</template>

<script setup lang="ts" name="Bind">
import { pageBinds, getBind, addBind, updateBind, deleteBinds } from '@/api/business/base/bind/bindApi'
import type { BindQuery, BindBo, BindVo } from '@/api/business/base/bind/bindTypes'
import { addDateRange } from '@/utils/date'
import { toValidate } from '@/utils/to'
import { showMsgSuccess, showConfirm } from '@/utils/modal'

/** 账号绑定字典数据 */
const { sys_platform_type } = useDict(DictTypes.sys_platform_type)

const { t } = useI18n()

// 使用表格高度处理钩子
const { tableHeight, queryFormRef, showSearch } = useTableHeight()

// =========== 账号绑定查询相关 ===========

/**查询参数对象*/
const queryParams = ref<BindQuery>({
  pageNum: 1,
  pageSize: 10,
  orderByColumn: 'id',
  isAsc: 'desc',
  id: undefined,
  userId: undefined,
  platformType: undefined,
  appid: undefined,
  unionid: undefined,
  openid: undefined,
  extraData: undefined
})

/**日期范围选择器*/
const dateRangeCreateTime = ref<[ElDateModelType, ElDateModelType]>(['', ''])

/** 账号绑定搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1
  getList()
}

/** 账号绑定重置按钮操作 */
const resetQuery = () => {
  dateRangeCreateTime.value = ['', '']
  queryFormRef.value?.resetFields()
  handleQuery()
}

// =========== 账号绑定表格数据相关 ===========
/**表格加载状态*/
const isLoading = ref(true)
/**数据列表*/
const bindList = ref<BindVo[]>([])
/**总记录数*/
const total = ref(0)
/**表格实例*/
const bindTableRef = ref()
/**选中的数据项*/
const selectionItems = ref<BindVo[]>([])

/** 表格多选事件处理 */
const handleSelectionChange = (selection: BindVo[]) => {
  selectionItems.value = selection
}

/** 查询账号绑定列表 */
const getList = async () => {
  isLoading.value = true
  queryParams.value.params = {}
  addDateRange(queryParams.value, dateRangeCreateTime.value, 'createTime')
  const [err, data] = await pageBinds(queryParams.value)
  if (!err) {
    bindList.value = data.records
    total.value = data.total
  }
  isLoading.value = false
}

/** 导出账号绑定数据 */
const handleExport = () => {
  useDownload().exportExcel(t('Bind Data', '账号绑定'), '/base/bind/exportBinds', queryParams.value)
}

/** 删除账号绑定操作 */
const handleDelete = async (row?: BindVo) => {
  const idsToDelete = row ? [row.id] : selectionItems.value.map((item) => item.id)
  if (idsToDelete.length === 0) return
  const itemsToDelete = row ? row.userId || row.id : selectionItems.value.map((item) => item.userId || item.id).join(', ')

  const [confirmErr] = await showConfirm(`${t('是否确认删除')}${itemsToDelete}`)
  if (confirmErr) return

  const [deleteErr] = await deleteBinds(idsToDelete)
  if (!deleteErr) {
    showMsgSuccess(t('message.deleteSuccess'))
    await getList()
  }
}

// =========== 账号绑定表单相关 ===========
/**初始表单数据*/
const initFormData: BindBo = {
  id: undefined,
  userId: undefined,
  platformType: undefined,
  appid: undefined,
  unionid: undefined,
  openid: undefined,
  extraData: undefined,
  remark: undefined
}

/**表单引用*/
const bindFormRef = ref<ElFormInstance>()
/**表单提交按钮加载状态*/
const buttonLoading = ref(false)
/**对话框配置对象*/
const dialog = ref<DialogState>({
  visible: false,
  title: ''
})
/**表单数据对象*/
const form = ref<BindBo>({ ...initFormData })
/**表单校验规则*/
const rules = ref<ElFormRules>({
  id: [{ required: true, message: t('id cannot be empty', '账号绑定id不能为空'), trigger: 'blur' }]
})

/** 账号绑定表单重置 */
const reset = () => {
  form.value = { ...initFormData }
  bindFormRef.value?.resetFields()
}

/** 取消账号绑定编辑 */
const cancel = () => {
  reset()
  dialog.value.visible = false
}

/** 新增账号绑定操作 */
const handleAdd = () => {
  reset()
  dialog.value.visible = true
  dialog.value.title = `${t('新增')}${t('bind', '账号绑定')}`
}

/** 修改账号绑定操作 */
const handleUpdate = async (row?: BindVo) => {
  reset()
  const itemToEdit = row || selectionItems.value[0]
  const [err, data] = await getBind(itemToEdit.id)
  if (!err) {
    Object.assign(form.value, data)
    dialog.value.visible = true
    dialog.value.title = `${t('修改')}${t('bind', '账号绑定')}`
  }
}

/** 提交账号绑定表单 */
const submitForm = async () => {
  const [validateErr] = await toValidate(bindFormRef)
  if (validateErr) return

  buttonLoading.value = true
  let err: Error | null
  if (form.value.id) {
    ;[err] = await updateBind(form.value)
  } else {
    ;[err] = await addBind(form.value)
  }
  if (!err) {
    showMsgSuccess(form.value.id ? t('message.updateSuccess') : t('message.addSuccess'))

    dialog.value.visible = false
    await getList()
  }
  buttonLoading.value = false
}

/**查看对话框配置*/
const viewDialog = ref<DialogState>({
  visible: false,
  title: ''
})

/**查看数据*/
const viewData = ref<BindVo>({} as BindVo)

/**详情字段配置 */
const detailFields = computed<FieldConfig[]>(() => [
  { prop: 'id', label: t('Bind ID', '账号绑定id') },
  { prop: 'userId', label: t('User ID', '用户id') },
  { prop: 'platformType', label: t('Platform Type', '平台类型'), type: 'dict', dictOptions: sys_platform_type },
  { prop: 'appid', label: 'appid' },
  { prop: 'unionid', label: 'unionid' },
  { prop: 'openid', label: 'openid' },
  { prop: 'extraData', label: t('Extra Data', '扩展数据') },
  { prop: 'createTime', label: t('Create Time', '创建时间'), type: 'datetime' },
  { prop: 'updateTime', label: t('Update Time', '更新时间'), type: 'datetime' },
  { prop: 'remark', label: t('Remark', '备注') }
])

/** 查看账号绑定详情操作 */
const handleView = async (row: BindVo) => {
  const [err, data] = await getBind(row.id)
  if (!err) {
    viewData.value = data
    viewDialog.value.title = `${t('查看')}${t('bind', '账号绑定')}`
    viewDialog.value.visible = true
  }
}

// =========== 生命周期 ===========
/**初始化账号绑定数据列表*/
onMounted(() => {
  getList()
})
/**页面激活时刷新账号绑定列表*/
onActivated(() => {
  if (isLoading.value) return
  getList()
})
</script>
