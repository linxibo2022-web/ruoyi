<!-- 存储配置 -->
<template>
  <AModal v-model="visible" :title="t('OSS Config Management', 'OSS配置管理')" size="xl" footer-type="close-only" @close="handleClose">
    <div>
      <!-- 对象存储配置搜索栏 -->
      <ASearchForm ref="queryFormRef" v-model="queryParams" :visible="showSearch">
        <AFormInput label="配置key" v-model="queryParams.configKey" prop="configKey" @input="handleQuery"></AFormInput>
        <AFormInput label="桶名称" v-model="queryParams.bucketName" prop="bucketName" @input="handleQuery"></AFormInput>
        <AFormSelect label="是否默认" :options="sys_boolean_flag" v-model="queryParams.status" prop="status" @change="handleQuery"></AFormSelect>
      </ASearchForm>

      <el-card shadow="hover">
        <!-- 对象存储配置工具栏 -->
        <template #header>
          <el-row :gutter="10" class="mb-2">
            <el-col :span="1.5" v-permi="['system:ossConfig:add']">
              <el-button type="primary" plain icon="Plus" @click="handleAdd">
                {{ t('新增') }}
              </el-button>
            </el-col>
            <el-col :span="1.5" v-permi="['system:ossConfig:update']">
              <el-button type="success" plain icon="Edit" :disabled="selectionItems.length !== 1" @click="handleUpdate()">
                {{ t('修改') }}
              </el-button>
            </el-col>
            <el-col :span="1.5" v-permi="['system:ossConfig:delete']">
              <el-button type="danger" plain icon="Delete" :disabled="selectionItems.length === 0" @click="handleDelete()">
                {{ t('删除') }}
              </el-button>
            </el-col>

            <TableToolbar v-model:showSearch="showSearch" :columns="columns" @reset-query="resetQuery" @query-table="getList"></TableToolbar>
          </el-row>
        </template>

        <!-- 对象存储配置表格数据 -->
        <el-table ref="ossConfigTableRef" v-loading="isLoading" :data="ossConfigList" height="400" stripe @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column v-if="columns[0].visible" :label="t('ossConfigId', '主键')" prop="ossConfigId" align="center" />
          <el-table-column v-if="columns[1].visible" :label="t('configKey', '配置key')" prop="configKey" align="center" min-width="120" />
          <el-table-column
            v-if="columns[2].visible"
            :label="t('endpoint', '访问站点')"
            prop="endpoint"
            align="center"
            min-width="150"
            show-overflow-tooltip
          />
          <el-table-column
            v-if="columns[3].visible"
            :label="t('domain', '自定义域名')"
            prop="domain"
            align="center"
            min-width="150"
            show-overflow-tooltip
          />
          <el-table-column v-if="columns[4].visible" :label="t('bucketName', '桶名称')" prop="bucketName" align="center" min-width="100" />
          <el-table-column v-if="columns[5].visible" :label="t('prefix', '前缀')" prop="prefix" align="center" min-width="100" />
          <el-table-column v-if="columns[6].visible" :label="t('region', '域')" prop="region" align="center" min-width="100" />
          <el-table-column v-if="columns[7].visible" :label="t('accessPolicy', '桶权限类型')" prop="accessPolicy" align="center" min-width="120">
            <template #default="{ row }">
              <DictTag :value="row.accessPolicy" :options="accessPolicyOptions" />
            </template>
          </el-table-column>
          <el-table-column v-if="columns[8].visible" :label="t('status', '是否默认')" prop="status" align="center" min-width="100">
            <template #default="{ row }">
              <AFormSwitch v-model="row.status" @change="handleStatusChange(row)" />
            </template>
          </el-table-column>
          <el-table-column :label="t('操作')" align="center" fixed="right" min-width="120">
            <template #default="{ row }">
              <el-tooltip :content="t('修改')" placement="top">
                <el-button v-permi="['system:ossConfig:update']" link type="success" icon="Edit" @click="handleUpdate(row)"></el-button>
              </el-tooltip>
              <el-tooltip :content="t('删除')" placement="top">
                <el-button v-permi="['system:ossConfig:delete']" link type="danger" icon="Delete" @click="handleDelete(row)"></el-button>
              </el-tooltip>
            </template>
          </el-table-column>
        </el-table>

        <Pagination v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
      </el-card>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">{{ t('关闭') }}</el-button>
      </div>
    </template>

    <!-- 添加或修改对象存储配置对话框 -->
    <AModal
      v-model="configDialog.visible"
      :title="configDialog.title"
      mode="dialog"
      :loading="buttonLoading"
      @confirm="submitForm"
      @cancel="cancelConfig"
    >
      <el-form ref="ossConfigFormRef" :model="form" :rules="rules" label-width="auto">
        <el-row>
          <AFormInput label="配置key" v-model="form.configKey" prop="configKey" :span="24"></AFormInput>
          <AFormInput label="访问站点" v-model="form.endpoint" prop="endpoint" :span="24">
            <template #prefix>
              <span style="color: #999">{{ protocol }}</span>
            </template>
          </AFormInput>
          <AFormInput label="自定义域名" v-model="form.domain" prop="domain" :span="24">
            <template #prefix>
              <span style="color: #999">{{ protocol }}</span>
            </template>
          </AFormInput>
          <AFormInput label="accessKey" v-model="form.accessKey" prop="accessKey" :span="24"></AFormInput>
          <AFormInput label="secretKey" v-model="form.secretKey" prop="secretKey" show-password :span="24"></AFormInput>
          <AFormInput label="桶名称" v-model="form.bucketName" prop="bucketName" :span="24"></AFormInput>
          <AFormInput label="前缀" v-model="form.prefix" prop="prefix" :span="24"></AFormInput>
          <AFormInput label="域" v-model="form.region" prop="region" :span="24"></AFormInput>
          <AFormRadio label="是否HTTPS" v-model="form.isHttps" prop="isHttps" :options="sys_boolean_flag" :span="24"></AFormRadio>
          <AFormRadio label="桶权限类型" v-model="form.accessPolicy" prop="accessPolicy" :options="accessPolicyOptions" :span="24"></AFormRadio>
          <AFormInput label="备注" v-model="form.remark" prop="remark" type="textarea" :span="24"></AFormInput>
        </el-row>
      </el-form>
    </AModal>
  </AModal>
</template>

<script setup lang="ts" name="OssConfig">
import {
  pageOssConfigs,
  getOssConfig,
  deleteOssConfigs,
  addOssConfig,
  updateOssConfig,
  changeOssConfigStatus
} from '@/api/system/oss/ossConfig/ossConfigApi'
import type { SysOssConfigQuery, SysOssConfigBo, SysOssConfigVo } from '@/api/system/oss/ossConfig/ossConfigTypes'
import { SystemConfig } from '@/systemConfig'
import { isTrue, toggleStatus } from '@/utils/boolean'
import { toValidate } from '@/utils/to'
import { showMsgSuccess, showConfirm } from '@/utils/modal'

const { t } = useI18n()

/**字典数据 */
const { sys_boolean_flag } = useDict('sys_boolean_flag')

// 桶权限类型选项
const accessPolicyOptions = ref<DictItem[]>([
  { label: 'private', value: '0' },
  { label: 'public', value: '1' },
  { label: 'custom', value: '2' }
])

interface OssConfigProps {
  modelValue: boolean
}

interface OssConfigEmits {
  (e: 'update:modelValue', value: boolean): void

  (e: 'success'): void
}

const props = withDefaults(defineProps<OssConfigProps>(), {
  modelValue: false
})

const emit = defineEmits<OssConfigEmits>()

// 使用表格高度处理钩子
const { queryFormRef, showSearch } = useTableHeight()

// =========== 对话框控制 ===========
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// =========== 查询相关 ===========
/**查询参数对象*/
const queryParams = ref<SysOssConfigQuery>({
  pageNum: 1,
  pageSize: 10,
  configKey: '',
  bucketName: '',
  status: ''
})

/** 对象存储配置搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1
  getList()
}

/** 对象存储配置重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

// =========== 对象存储配置表格数据相关 ===========
/**表格加载状态*/
const isLoading = ref(true)
/**数据列表*/
const ossConfigList = ref<SysOssConfigVo[]>([])
/**总记录数*/
const total = ref(0)
/**表格实例*/
const ossConfigTableRef = ref()
/**选中的数据项*/
const selectionItems = ref<SysOssConfigVo[]>([])

// 列显隐信息
const columns = computed<FieldVisibilityConfig[]>(() => [
  { key: 0, field: 'ossConfigId', label: t('ID', '主键'), visible: false, children: [] },
  { key: 1, field: 'configKey', label: t('Config Key', '配置key'), visible: true, children: [] },
  { key: 2, field: 'endpoint', label: t('Endpoint', '访问站点'), visible: true, children: [] },
  { key: 3, field: 'domain', label: t('Custom Domain', '自定义域名'), visible: true, children: [] },
  { key: 4, field: 'bucketName', label: t('Bucket Name', '桶名称'), visible: true, children: [] },
  { key: 5, field: 'prefix', label: t('Prefix', '前缀'), visible: true, children: [] },
  { key: 6, field: 'region', label: t('Region', '域'), visible: true, children: [] },
  { key: 7, field: 'accessPolicy', label: t('Access Policy', '桶权限类型'), visible: true, children: [] },
  { key: 8, field: 'status', label: t('Status', '状态'), visible: true, children: [] }
])

/** 表格多选事件处理 */
const handleSelectionChange = (selection: SysOssConfigVo[]) => {
  selectionItems.value = selection
}

/** 查询对象存储配置列表 */
const getList = async () => {
  isLoading.value = true
  const [err, data] = await pageOssConfigs(queryParams.value)
  if (!err) {
    ossConfigList.value = data.records
    total.value = data.total
  }
  isLoading.value = false
}

/** 删除对象存储配置操作 */
const handleDelete = async (row?: SysOssConfigVo) => {
  const idsToDelete = row ? [row.ossConfigId] : selectionItems.value.map((item) => item.ossConfigId)
  if (idsToDelete.length === 0) return
  const itemsToDelete = row ? row.configKey : selectionItems.value.map((item) => item.configKey).join(', ')
  const [confirmErr] = await showConfirm(`${t('是否确认删除')}${itemsToDelete}`)
  if (confirmErr) return

  const [deleteErr] = await deleteOssConfigs(idsToDelete)
  if (!deleteErr) {
    showMsgSuccess(t('message.deleteSuccess'))
    await getList()
    emit('success')
  }
}

// =========== 对象存储配置表单相关 ===========
/**初始表单数据*/
const initFormData: SysOssConfigBo = {
  ossConfigId: undefined,
  configKey: '',
  accessKey: '',
  secretKey: '',
  bucketName: '',
  prefix: SystemConfig.app.id,
  endpoint: '',
  domain: '',
  isHttps: '0',
  accessPolicy: '1',
  region: '',
  status: '1',
  remark: ''
}

/**表单引用*/
const ossConfigFormRef = ref<ElFormInstance>()
/**表单提交按钮加载状态*/
const buttonLoading = ref(false)
/**配置对话框配置对象*/
const configDialog = ref<DialogState>({
  visible: false,
  title: ''
})

/**表单数据对象*/
const form = ref<SysOssConfigBo>({ ...initFormData })

/**协议前缀*/
const protocol = computed(() => (form.value.isHttps === '1' ? 'https://' : 'http://'))

/**表单校验规则*/
const rules = computed<ElFormRules>(() => ({
  configKey: [{ required: true, message: t('configKey required', 'configKey不能为空'), trigger: 'blur' }],
  accessKey: [
    { required: true, message: t('accessKey required', 'accessKey不能为空'), trigger: 'blur' },
    {
      min: 2,
      max: 200,
      message: t('accessKey length 2-100', 'accessKey长度必须介于 2 和 100 之间'),
      trigger: 'blur'
    }
  ],
  secretKey: [
    { required: true, message: t('secretKey required', 'secretKey不能为空'), trigger: 'blur' },
    {
      min: 2,
      max: 100,
      message: t('secretKey length 2-100', 'secretKey长度必须介于 2 和 100 之间'),
      trigger: 'blur'
    }
  ],
  bucketName: [
    { required: true, message: t('bucketName required', 'bucketName不能为空'), trigger: 'blur' },
    {
      min: 2,
      max: 100,
      message: t('bucketName length 2-100', 'bucketName长度必须介于 2 和 100 之间'),
      trigger: 'blur'
    }
  ],
  endpoint: [
    { required: true, message: t('endpoint required', 'endpoint不能为空'), trigger: 'blur' },
    {
      min: 2,
      max: 100,
      message: t('endpoint length 2-100', 'endpoint名称长度必须介于 2 和 100 之间'),
      trigger: 'blur'
    }
  ],
  accessPolicy: [{ required: true, message: t('accessPolicy required', 'accessPolicy不能为空'), trigger: 'blur' }]
}))

/** 对象存储配置表单重置 */
const reset = () => {
  form.value = { ...initFormData }
  ossConfigFormRef.value?.resetFields()
}

/** 取消对象存储配置编辑 */
const cancelConfig = () => {
  configDialog.value.visible = false
  reset()
}

/** 新增对象存储配置操作 */
const handleAdd = () => {
  reset()
  configDialog.value.visible = true
  configDialog.value.title = `${t('新增')}${t('ossConfig', '对象存储配置')}`
}

/** 修改对象存储配置操作 */
const handleUpdate = async (row?: SysOssConfigVo) => {
  reset()
  // 如果传入了行，则编辑该行；否则编辑选中的第一行
  const itemToEdit = row || selectionItems.value[0]
  const [err, data] = await getOssConfig(itemToEdit.ossConfigId)
  if (!err) {
    Object.assign(form.value, data)
    configDialog.value.visible = true
    configDialog.value.title = `${t('修改')}${t('ossConfig', '对象存储配置')}`
  }
}

/** 提交对象存储配置表单 */
const submitForm = async () => {
  const [validateErr] = await toValidate(ossConfigFormRef)
  if (validateErr) return

  buttonLoading.value = true
  let err: Error | null
  if (form.value.ossConfigId) {
    ;[err] = await updateOssConfig(form.value)
  } else {
    ;[err] = await addOssConfig(form.value)
  }

  if (!err) {
    showMsgSuccess(form.value.ossConfigId ? t('message.updateSuccess') : t('message.addSuccess'))
    configDialog.value.visible = false
    await getList()
    emit('success')
  }
  buttonLoading.value = false
}

/** 对象存储配置启用禁用状态修改 */
const handleStatusChange = async (row: SysOssConfigVo) => {
  const text = isTrue(row.status) ? t('Enable', '启用') : t('Disable', '停用')
  const [confirmErr] = await showConfirm(`${t('是否确认')}${text}${row.configKey}?`)
  if (confirmErr) {
    row.status = toggleStatus(row.status)
    return
  }
  const [changeErr] = await changeOssConfigStatus(row.ossConfigId, row.status, row.configKey)
  if (changeErr) {
    row.status = toggleStatus(row.status)
    return
  }
  await getList()
  showMsgSuccess(`${text}${t('Success', '成功')}`)
  emit('success')
}

/** 关闭对话框操作 */
const handleClose = () => {
  visible.value = false
  // 重置数据
  ossConfigList.value = []
  selectionItems.value = []
  queryParams.value = {
    pageNum: 1,
    pageSize: 10,
    configKey: '',
    bucketName: '',
    status: ''
  }
}

// =========== 监听变化 ===========
watch(
  () => props.modelValue,
  (newValue) => {
    if (newValue) {
      getList()
    }
  }
)
</script>
