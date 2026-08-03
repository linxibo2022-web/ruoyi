<!-- 广告配置 -->
<template>
  <div>
    <!-- 广告配置搜索栏 -->
    <ASearchForm ref="queryFormRef" v-model="queryParams" :visible="showSearch">
      <AFormInput label="模糊搜索" prop="searchValue" v-model="queryParams.searchValue" @input="handleQuery"></AFormInput>
      <AFormInput label="appid" v-model="queryParams.appid" prop="appid" @input="handleQuery"></AFormInput>
      <AFormSelect label="状态" v-model="queryParams.status" prop="status" :options="sys_enable_status" @change="handleQuery"></AFormSelect>
      <AFormDate v-model="dateRangeCreateTime" prop="createTime" type="daterange" label="创建时间" @change="handleQuery"></AFormDate>
    </ASearchForm>

    <el-card shadow="hover">
      <!-- 广告配置工具栏 -->
      <template #header>
        <el-row :gutter="10" class="mb-2">
          <el-col :span="1.5" v-permi="['base:ad:add']">
            <el-button type="primary" plain icon="Plus" @click="handleAdd">
              {{ t('新增') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['base:ad:update']">
            <el-button type="success" plain icon="Edit" :disabled="selectionItems.length !== 1" @click="handleUpdate()">
              {{ t('修改') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['base:ad:delete']">
            <el-button type="danger" plain icon="Delete" :disabled="selectionItems.length === 0" @click="handleDelete()">
              {{ t('删除') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['base:ad:import']">
            <AImportExcel
              v-slot="{ openImportExcel }"
              :title="t('Ad Data', '广告配置')"
              templateUrl="/base/ad/templateAds"
              importUrl="/base/ad/importAds"
              @import-success="getList"
            >
              <el-button type="info" plain icon="Top" @click="openImportExcel">
                {{ t('导入') }}
              </el-button>
            </AImportExcel>
          </el-col>
          <el-col :span="1.5" v-permi="['base:ad:export']">
            <el-button type="warning" plain icon="Download" @click="handleExport">
              {{ t('导出') }}
            </el-button>
          </el-col>

          <TableToolbar
            v-model:showSearch="showSearch"
            @reset-query="resetQuery"
            @query-table="getList"
            :table-columns="detailFields"
            :table-data="adList"
          ></TableToolbar>
        </el-row>

        <!-- 显示已选项 -->
        <!-- <ASelectionTags :items="selectionItems" :on-clear="selectionClear" @close="selectionRemove" /> -->
      </template>

      <!-- 广告配置表格数据 -->
      <el-table ref="adTableRef" v-loading="isLoading" :data="adList" :height="tableHeight" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column v-if="true" :label="t('id', '主键id')" prop="id" align="center" min-width="105" />
        <el-table-column :label="t('appid', 'appid')" prop="appid" align="center" />
        <el-table-column :label="t('adUnitId', '广告位id')" prop="adUnitId" align="center" />
        <el-table-column :label="t('adName', '广告名称')" prop="adName" align="center" />
        <el-table-column :label="t('adType', '广告类型')" prop="adType" align="center" />
        <el-table-column :label="t('position', '投放位置')" prop="position" align="center" />
        <el-table-column :label="t('img', '广告图片')" prop="img" align="center" width="80">
          <template #default="{ row }">
            <ImagePreview :src="row.img" />
          </template>
        </el-table-column>
        <el-table-column :label="t('description', '描述')" prop="description" align="center" />
        <el-table-column :label="t('jumpAppid', '跳转appid')" prop="jumpAppid" align="center" />
        <el-table-column :label="t('jumpPath', '跳转路径')" prop="jumpPath" align="center" />
        <el-table-column :label="t('styleConfig', '样式配置')" prop="styleConfig" align="center" />
        <el-table-column :label="t('sortOrder', '排序值')" prop="sortOrder" align="center" />
        <el-table-column :label="t('status', '状态')" prop="status" align="center">
          <template #default="{ row }">
            <AFormSwitch v-model="row.status" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column :label="t('createTime', '创建时间')" prop="createTime" align="center" width="105" />
        <el-table-column :label="t('updateTime', '更新时间')" prop="updateTime" align="center" width="105" />
        <el-table-column :label="t('remark', '备注')" prop="remark" align="center" />
        <el-table-column :label="t('操作')" align="center" fixed="right" width="120">
          <template #default="{ row }">
            <el-tooltip :content="t('查看')" placement="top">
              <el-button v-permi="['base:ad:query']" link type="primary" icon="View" @click="handleView(row)"></el-button>
            </el-tooltip>
            <el-tooltip :content="t('修改')" placement="top">
              <el-button v-permi="['base:ad:update']" link type="success" icon="Edit" @click="handleUpdate(row)"></el-button>
            </el-tooltip>
            <el-tooltip :content="t('删除')" placement="top">
              <el-button v-permi="['base:ad:delete']" link type="danger" icon="Delete" @click="handleDelete(row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 添加或修改广告配置对话框 -->
    <AModal v-model="dialog.visible" :title="dialog.title" :loading="buttonLoading" @confirm="submitForm" @cancel="cancel">
      <el-form ref="adFormRef" :model="form" :rules="rules" label-width="auto">
        <el-row :gutter="10">
          <AFormInput label="appid" v-model="form.appid" :maxlength="50" prop="appid" span="auto"></AFormInput>
          <AFormInput label="广告位id" v-model="form.adUnitId" :maxlength="100" prop="adUnitId" span="auto"></AFormInput>
          <AFormInput label="广告名称" v-model="form.adName" :maxlength="100" prop="adName" span="auto"></AFormInput>
          <AFormInput label="广告类型" v-model="form.adType" :maxlength="20" prop="adType" span="auto"></AFormInput>
          <AFormInput label="投放位置" v-model="form.position" :maxlength="50" prop="position" span="auto"></AFormInput>
          <AFormImgUpload label="广告图片" v-model="form.img" prop="img" span="auto"></AFormImgUpload>
          <AFormInput label="描述" v-model="form.description" :maxlength="255" prop="description" span="auto"></AFormInput>
          <AFormInput label="跳转appid" v-model="form.jumpAppid" :maxlength="50" prop="jumpAppid" span="auto"></AFormInput>
          <AFormInput label="跳转路径" v-model="form.jumpPath" :maxlength="255" prop="jumpPath" span="auto"></AFormInput>
          <AFormInput label="样式配置" v-model="form.styleConfig" :maxlength="255" prop="styleConfig" span="auto"></AFormInput>
          <AFormInput label="排序值" v-model="form.sortOrder" type="number" prop="sortOrder" span="auto"></AFormInput>
          <AFormRadio label="状态" v-model="form.status" prop="status" :options="sys_enable_status" span="auto"></AFormRadio>
          <AFormInput label="备注" v-model="form.remark" type="textarea" :maxlength="255" prop="remark" span="auto"></AFormInput>
        </el-row>
      </el-form>
    </AModal>

    <!-- 查看广告配置详情对话框 -->
    <ADetail v-model="viewDialog.visible" :title="viewDialog.title" :data="viewData" :fields="detailFields" />
  </div>
</template>

<script setup lang="ts" name="Ad">
import { pageAds, getAd, addAd, updateAd, deleteAds } from '@/api/business/base/ad/adApi'
import type { AdQuery, AdBo, AdVo } from '@/api/business/base/ad/adTypes'
import { isTrue, toggleStatus } from '@/utils/boolean'
import { addDateRange } from '@/utils/date'
import { toValidate } from '@/utils/to'
import { showMsgSuccess, showConfirm } from '@/utils/modal'

/** 广告配置字典数据 */
const { sys_enable_status } = useDict(DictTypes.sys_enable_status)

const { t } = useI18n()

// 使用表格高度处理钩子
const { tableHeight, queryFormRef, showSearch } = useTableHeight()

// =========== 广告配置查询相关 ===========

/**查询参数对象*/
const queryParams = ref<AdQuery>({
  pageNum: 1,
  pageSize: 10,
  orderByColumn: 'id',
  isAsc: 'desc',
  id: undefined,
  appid: undefined,
  adUnitId: undefined,
  adName: undefined,
  adType: undefined,
  position: undefined,
  img: undefined,
  description: undefined,
  jumpAppid: undefined,
  jumpPath: undefined,
  styleConfig: undefined,
  sortOrder: undefined,
  status: undefined
})

/**日期范围选择器*/
const dateRangeCreateTime = ref<[ElDateModelType, ElDateModelType]>(['', ''])

/** 广告配置搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1
  getList()
}

/** 广告配置重置按钮操作 */
const resetQuery = () => {
  dateRangeCreateTime.value = ['', '']
  queryFormRef.value?.resetFields()
  handleQuery()
}

// =========== 广告配置表格数据相关 ===========
/**表格加载状态*/
const isLoading = ref(true)
/**数据列表*/
const adList = ref<AdVo[]>([])
/**总记录数*/
const total = ref(0)
/**表格实例*/
const adTableRef = ref()
/**选中的数据项*/
const selectionItems = ref<AdVo[]>([])

/** 表格多选事件处理 */
const handleSelectionChange = (selection: AdVo[]) => {
  selectionItems.value = selection
}

/** 查询广告配置列表 */
const getList = async () => {
  isLoading.value = true
  queryParams.value.params = {}
  addDateRange(queryParams.value, dateRangeCreateTime.value, 'createTime')
  const [err, data] = await pageAds(queryParams.value)
  if (!err) {
    adList.value = data.records
    total.value = data.total
  }
  isLoading.value = false
}

/** 导出广告配置数据 */
const handleExport = () => {
  useDownload().exportExcel(t('Ad Data', '广告配置'), '/base/ad/exportAds', queryParams.value)
}

/** 删除广告配置操作 */
const handleDelete = async (row?: AdVo) => {
  const idsToDelete = row ? [row.id] : selectionItems.value.map((item) => item.id)
  if (idsToDelete.length === 0) return
  const itemsToDelete = row ? row.appid || row.id : selectionItems.value.map((item) => item.appid || item.id).join(', ')

  const [confirmErr] = await showConfirm(`${t('是否确认删除')}${itemsToDelete}`)
  if (confirmErr) return

  const [deleteErr] = await deleteAds(idsToDelete)
  if (!deleteErr) {
    showMsgSuccess(t('message.deleteSuccess'))
    await getList()
  }
}

// =========== 广告配置表单相关 ===========
/**初始表单数据*/
const initFormData: AdBo = {
  id: undefined,
  appid: undefined,
  adUnitId: undefined,
  adName: undefined,
  adType: undefined,
  position: undefined,
  img: undefined,
  description: undefined,
  jumpAppid: undefined,
  jumpPath: undefined,
  styleConfig: undefined,
  sortOrder: 999,
  status: '1',
  remark: undefined
}

/**表单引用*/
const adFormRef = ref<ElFormInstance>()
/**表单提交按钮加载状态*/
const buttonLoading = ref(false)
/**对话框配置对象*/
const dialog = ref<DialogState>({
  visible: false,
  title: ''
})
/**表单数据对象*/
const form = ref<AdBo>({ ...initFormData })
/**表单校验规则*/
const rules = ref<ElFormRules>({
  id: [{ required: true, message: t('id cannot be empty', '主键id不能为空'), trigger: 'blur' }],
  adName: [{ required: true, message: t('adName cannot be empty', '广告名称不能为空'), trigger: 'blur' }],
  adType: [{ required: true, message: t('adType cannot be empty', '广告类型不能为空'), trigger: 'blur' }]
})

/** 广告配置表单重置 */
const reset = () => {
  form.value = { ...initFormData }
  adFormRef.value?.resetFields()
}

/** 取消广告配置编辑 */
const cancel = () => {
  reset()
  dialog.value.visible = false
}

/** 新增广告配置操作 */
const handleAdd = () => {
  reset()
  dialog.value.visible = true
  dialog.value.title = `${t('新增')}${t('ad', '广告配置')}`
}

/** 修改广告配置操作 */
const handleUpdate = async (row?: AdVo) => {
  reset()
  const itemToEdit = row || selectionItems.value[0]
  const [err, data] = await getAd(itemToEdit.id)
  if (!err) {
    Object.assign(form.value, data)
    dialog.value.visible = true
    dialog.value.title = `${t('修改')}${t('ad', '广告配置')}`
  }
}

/** 提交广告配置表单 */
const submitForm = async () => {
  const [validateErr] = await toValidate(adFormRef)
  if (validateErr) return

  buttonLoading.value = true
  let err: Error | null, data: any
  if (form.value.id) {
    ;[err, data] = await updateAd(form.value)
  } else {
    ;[err, data] = await addAd(form.value)
  }
  if (!err) {
    showMsgSuccess(form.value.id ? t('message.updateSuccess') : t('message.addSuccess'))

    dialog.value.visible = false
    await getList()
  }
  buttonLoading.value = false
}

/** 广告配置启用禁用状态修改 */
const handleStatusChange = async (row: AdVo) => {
  const text = isTrue(row.status) ? t('启用') : t('停用')
  const [confirmErr] = await showConfirm(`${t('是否确认')}${text}${row.id}?`)
  if (confirmErr) {
    row.status = toggleStatus(row.status)
    return
  }
  const [updateErr] = await updateAd(row)
  if (!updateErr) {
    await getList()
    showMsgSuccess(`${text}${t('成功')}`)
  } else {
    row.status = toggleStatus(row.status)
  }
}

/**查看对话框配置*/
const viewDialog = ref<DialogState>({
  visible: false,
  title: ''
})

/**查看数据*/
const viewData = ref<AdVo>({} as AdVo)

/**详情字段配置 */
const detailFields = computed<FieldConfig[]>(() => [
  { prop: 'id', label: t('id', '主键id') },
  { prop: 'appid', label: t('appid', 'appid') },
  { prop: 'adUnitId', label: t('adUnitId', '广告位id') },
  { prop: 'adName', label: t('adName', '广告名称') },
  { prop: 'adType', label: t('adType', '广告类型') },
  { prop: 'position', label: t('position', '投放位置') },
  { prop: 'img', label: t('img', '广告图片'), type: 'image' },
  { prop: 'description', label: t('description', '描述') },
  { prop: 'jumpAppid', label: t('jumpAppid', '跳转appid') },
  { prop: 'jumpPath', label: t('jumpPath', '跳转路径') },
  { prop: 'styleConfig', label: t('styleConfig', '样式配置') },
  { prop: 'sortOrder', label: t('sortOrder', '排序值') },
  { prop: 'status', label: t('status', '状态'), type: 'dict', dictOptions: sys_enable_status },
  { prop: 'createTime', label: t('createTime', '创建时间'), type: 'datetime' },
  { prop: 'updateTime', label: t('updateTime', '更新时间'), type: 'datetime' },
  { prop: 'remark', label: t('remark', '备注') }
])

/** 查看广告配置详情操作 */
const handleView = async (row: AdVo) => {
  const [err, data] = await getAd(row.id)
  if (!err) {
    viewData.value = data
    viewDialog.value.title = `${t('查看')}${t('ad', '广告配置')}`
    viewDialog.value.visible = true
  }
}

// =========== 生命周期 ===========
/**初始化广告配置数据列表*/
onMounted(() => {
  getList()
})
/**页面激活时刷新广告配置列表*/
onActivated(() => {
  if (isLoading.value) return
  getList()
})
</script>
