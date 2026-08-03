<!-- 代码生成编辑 -->
<template>
  <el-card>
    <el-tabs v-model="activeName">
      <el-tab-pane :label="t('Basic Info', '基本信息')" name="basic">
        <BasicInfoForm ref="basicInfoRef" :info="info" />
      </el-tab-pane>
      <el-tab-pane :label="t('Column Info', '字段信息')" name="columnInfo">
        <el-table ref="columnTable" :data="columns" row-key="columnId" :max-height="tableHeight" stripe>
          <el-table-column :label="t('No', '序号')" type="index" min-width="4%" />
          <el-table-column :label="t('Column Name', '字段列名')" prop="columnName" min-width="10%" :show-overflow-tooltip="true" />
          <el-table-column :label="t('Column Label', '字段标签')" min-width="12%">
            <template #default="scope">
              <el-input v-model="scope.row.columnLabel" :placeholder="t('Label for display', '用于显示的标签')">
                <template #suffix>
                  <el-tooltip v-if="scope.row.columnComment" :content="scope.row.columnComment" placement="top">
                    <el-icon><InfoFilled /></el-icon>
                  </el-tooltip>
                </template>
              </el-input>
            </template>
          </el-table-column>
          <el-table-column :label="t('DB Type', '物理类型')" prop="columnType" min-width="9%" :show-overflow-tooltip="true" />
          <el-table-column :label="t('Java Type', 'Java类型')" min-width="11%">
            <template #default="scope">
              <el-select v-model="scope.row.javaType">
                <el-option label="Long" value="Long" />
                <el-option label="String" value="String" />
                <el-option label="Integer" value="Integer" />
                <el-option label="Double" value="Double" />
                <el-option label="BigDecimal" value="BigDecimal" />
                <el-option label="Date" value="Date" />
                <el-option label="Boolean" value="Boolean" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column :label="t('Java Field', 'java属性')" min-width="10%">
            <template #default="scope">
              <el-input v-model="scope.row.javaField"></el-input>
            </template>
          </el-table-column>

          <el-table-column :label="t('Insert', '插入')" min-width="5%">
            <template #default="scope">
              <el-checkbox v-model="scope.row.isInsert" true-value="1" false-value="0"></el-checkbox>
            </template>
          </el-table-column>
          <el-table-column :label="t('Edit', '编辑')" min-width="5%">
            <template #default="scope">
              <el-checkbox v-model="scope.row.isEdit" true-value="1" false-value="0"></el-checkbox>
            </template>
          </el-table-column>
          <el-table-column :label="t('List', '列表')" min-width="5%">
            <template #default="scope">
              <el-checkbox v-model="scope.row.isList" true-value="1" false-value="0"></el-checkbox>
            </template>
          </el-table-column>
          <el-table-column :label="t('Query', '查询')" min-width="5%">
            <template #default="scope">
              <el-checkbox v-model="scope.row.isQuery" true-value="1" false-value="0"></el-checkbox>
            </template>
          </el-table-column>
          <el-table-column :label="t('Query Type', '查询方式')" min-width="10%">
            <template #default="scope">
              <el-select v-model="scope.row.queryType">
                <el-option label="=" value="EQ" />
                <el-option label="!=" value="NE" />
                <el-option label=">" value="GT" />
                <el-option label=">=" value="GE" />
                <el-option label="<" value="LT" />
                <el-option label="<=" value="LE" />
                <el-option label="LIKE" value="LIKE" />
                <el-option label="BETWEEN" value="BETWEEN" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column :label="t('Required', '必填')" min-width="5%">
            <template #default="scope">
              <el-checkbox v-model="scope.row.isRequired" true-value="1" false-value="0"></el-checkbox>
            </template>
          </el-table-column>
          <el-table-column :label="t('Default', '默认值')" min-width="6%" show-overflow-tooltip>
            <template #default="scope">
              <span>{{ scope.row.columnDefault === 'undefined' ? '' : scope.row.columnDefault }}</span>
            </template>
          </el-table-column>
          <el-table-column :label="t('Display Type', '显示类型')" min-width="12%">
            <template #default="scope">
              <el-select v-model="scope.row.htmlType" @change="handleHtmlTypeChange(scope.row)">
                <el-option :label="t('Text Input', '文本框')" value="input" />
                <el-option :label="t('Textarea', '文本域')" value="textarea" />
                <el-option :label="t('Number Input', '数字输入框')" value="numberInput" />
                <el-option :label="t('Select', '下拉框')" value="select" />
                <el-option :label="t('Radio', '单选框')" value="radio" />
                <el-option :label="t('Checkbox', '复选框')" value="checkbox" />
                <el-option :label="t('Datetime', '日期控件')" value="datetime" />
                <el-option :label="t('Image Upload', '图片上传')" value="imageUpload" />
                <el-option :label="t('File Upload', '文件上传')" value="fileUpload" />
                <el-option :label="t('Rich Editor', '富文本控件')" value="editor" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column :label="t('Dict Type', '字典类型')" min-width="12%">
            <template #default="scope">
              <el-select
                v-model="scope.row.dictType"
                clearable
                filterable
                :placeholder="t('Please select', '请选择')"
                value-on-clear=""
                :disabled="isDictTypeDisabled(scope.row)"
                @change="handleDictTypeChange(scope.row)"
              >
                <el-option v-for="dict in dictOptions" :key="dict.dictType" :label="dict.dictName" :value="dict.dictType">
                  <span style="float: left">{{ dict.dictName }}</span>
                  <span style="float: right; color: #8492a6; font-size: 13px">{{ dict.dictType }}</span>
                </el-option>
              </el-select>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane :label="t('Gen Info', '生成信息')" name="genInfo">
        <GenInfoForm ref="genInfoRef" :info="info" :tables="tables" />
      </el-tab-pane>
    </el-tabs>
    <el-form label-width="auto">
      <div style="text-align: center; margin-left: -100px; margin-top: 10px">
        <el-button :loading="buttonLoading" type="primary" @click="submitForm">{{ t('Submit', '提交') }}</el-button>
        <el-button @click="close">{{ t('Back', '返回') }}</el-button>
      </div>
    </el-form>
  </el-card>
</template>

<script setup lang="ts" name="GenEdit">
import BasicInfoForm from './BasicInfoForm.vue'
import GenInfoForm from './GenInfoForm.vue'
import { useRoute } from 'vue-router'
import { getGen, updateGen } from '@/api/tool/gen/genApi'
import { getDictTypeOptions } from '@/api/system/dict/dictType/dictTypeApi'
import type { GenTableColumn, GenTable } from '@/api/tool/gen/genTypes'
import type { SysDictTypeVo } from '@/api/system/dict/dictType/dictTypeTypes'
import { closeOpenPage } from '@/utils/tab'
import { showMsgSuccess, showMsgError } from '@/utils/modal'
const { t } = useI18n()
const route = useRoute()

// 使用表格高度处理钩子
const { tableHeight } = useTableHeight()

// =========== 基础状态 ===========
/**当前激活的标签页*/
const activeName = ref('columnInfo')
/**按钮加载状态*/
const buttonLoading = ref(false)

// =========== 表格数据相关 ===========
/**字段表格实例*/
const columnTable = ref()
/**表列表*/
const tables = ref<GenTable[]>([])
/**字段列表*/
const columns = ref<GenTableColumn[]>([])
/**字典选项*/
const dictOptions = ref<SysDictTypeVo[]>([])
/**表信息*/
const info = ref<Partial<GenTable>>({})

// =========== 表单引用 ===========
/**基本信息表单引用*/
const basicInfoRef = ref<InstanceType<typeof BasicInfoForm>>()
/**生成信息表单引用*/
const genInfoRef = ref<InstanceType<typeof GenInfoForm>>()

// =========== 计算属性 ===========

// =========== 表单验证 ===========
/**获取表单验证Promise*/
const getFormPromise = (form: Ref<any>) => {
  return new Promise((resolve) => {
    form.value?.validate((res: any) => {
      resolve(res)
    })
  })
}

// =========== 提交操作 ===========
/** 提交表单 */
const submitForm = async () => {
  const basicForm = basicInfoRef.value?.$refs.basicInfoForm
  const genForm = genInfoRef.value?.$refs.genInfoForm

  if (!basicForm || !genForm) {
    showMsgError(t('Failed to get form reference', '表单引用获取失败'))
    return
  }

  buttonLoading.value = true
  const validationResults = await Promise.all([basicForm, genForm].map(getFormPromise))
  const validateResult = validationResults.every((item) => !!item)

  if (validateResult) {
    const genTable: any = Object.assign({}, info.value)
    genTable.columns = columns.value
    genTable.params = {
      treeCode: info.value?.treeCode,
      treeName: info.value.treeName,
      treeParentCode: info.value.treeParentCode,
      parentMenuId: info.value.parentMenuId,
      menuIcon: info.value.menuIcon,
      menuOrder: info.value.menuOrder,
      autoImportMenu: info.value.autoImportMenu,
      backendModuleName: info.value.backendModuleName,
      frontendRootDir: info.value.frontendRootDir
    }

    const [err] = await updateGen(genTable)
    if (!err) {
      showMsgSuccess(t('Update Success', '修改成功'))
      close()
    }
  } else {
    showMsgError(t('Form validation failed, please check', '表单校验未通过，请重新检查提交内容'))
  }

  buttonLoading.value = false
}

// =========== 页面操作 ===========
/** 判断字典类型是否应该禁用 */
const isDictTypeDisabled = (row: GenTableColumn) => {
  // 这些显示类型不需要字典，应该禁用字典选择
  const noDictTypes = ['imageUpload', 'fileUpload', 'editor', 'datetime', 'textarea', 'numberInput']
  return noDictTypes.includes(row.htmlType)
}

/** 显示类型变化时的处理 */
const handleHtmlTypeChange = (row: GenTableColumn) => {
  // 如果切换到不支持字典的类型，清空字典类型
  if (isDictTypeDisabled(row)) {
    row.dictType = ''
  }
}

/** 字典类型变化时自动切换显示类型 */
const handleDictTypeChange = (row: GenTableColumn) => {
  if (row.dictType) {
    // 选择了字典类型，自动将显示类型切换为下拉框
    row.htmlType = 'select'
  } else {
    // 清空了字典类型，恢复为文本框
    row.htmlType = 'input'
  }
}

/** 关闭页面 */
const close = () => {
  closeOpenPage({
    path: '/tool/gen',
    query: { t: Date.now().toString(), pageNum: route.query.pageNum }
  })
}

// =========== 数据初始化 ===========
/** 初始化数据 */
const initData = async () => {
  const tableId = route.params && (route.params.tableId as string)
  if (!tableId) return

  // 获取表详细信息
  const [genErr, genData] = await getGen(tableId)
  if (!genErr) {
    columns.value = genData.rows
    info.value = genData.info
    tables.value = genData.tables
  }

  // 查询字典下拉列表
  const [dictErr, dictData] = await getDictTypeOptions()
  if (!dictErr) {
    dictOptions.value = dictData
  }
}

// =========== 生命周期 ===========
onMounted(() => {
  initData()
})
</script>
