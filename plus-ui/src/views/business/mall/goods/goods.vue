<!-- 商品管理 -->
<template>
  <div>
    <!-- 商品搜索栏 -->
    <ASearchForm ref="queryFormRef" v-model="queryParams" :visible="showSearch">
      <AFormInput label="模糊搜索" prop="searchValue" v-model="queryParams.searchValue" @input="handleQuery"></AFormInput>
      <AFormInput label="商品名称" v-model="queryParams.name" prop="name" @input="handleQuery"></AFormInput>
      <AFormSelect label="状态" v-model="queryParams.status" prop="status" :options="sys_enable_status" @change="handleQuery"></AFormSelect>
      <AFormDate v-model="dateRangeCreateTime" prop="createTime" type="daterange" label="创建时间" @change="handleQuery"></AFormDate>
    </ASearchForm>

    <el-card shadow="hover">
      <!-- 商品工具栏 -->
      <template #header>
        <el-row :gutter="10" class="mb-2">
          <el-col :span="1.5" v-permi="['mall:goods:add']">
            <el-button type="primary" plain icon="Plus" @click="handleAdd">
              {{ t('新增') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['mall:goods:update']">
            <el-button type="success" plain icon="Edit" :disabled="selectionItems.length !== 1" @click="handleUpdate()">
              {{ t('修改') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['mall:goods:delete']">
            <el-button type="danger" plain icon="Delete" :disabled="selectionItems.length === 0" @click="handleDelete()">
              {{ t('删除') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['mall:goods:import']">
            <AImportExcel
              v-slot="{ openImportExcel }"
              :title="t('Goods Data', '商品')"
              templateUrl="/mall/goods/templateGoods"
              importUrl="/mall/goods/importGoods"
              @import-success="getList"
            >
              <el-button type="info" plain icon="Top" @click="openImportExcel">
                {{ t('导入') }}
              </el-button>
            </AImportExcel>
          </el-col>
          <el-col :span="1.5" v-permi="['mall:goods:export']">
            <el-button type="warning" plain icon="Download" @click="handleExport">
              {{ t('导出') }}
            </el-button>
          </el-col>

          <TableToolbar
            v-model:showSearch="showSearch"
            @reset-query="resetQuery"
            @query-table="getList"
            :table-columns="detailFields"
            :table-data="goodsList"
          ></TableToolbar>
        </el-row>

        <!-- 显示已选项 -->
        <!-- <ASelectionTags :items="selectionItems" :on-clear="selectionClear" @close="selectionRemove" /> -->
      </template>

      <!-- 商品表格数据 -->
      <el-table ref="goodsTableRef" v-loading="isLoading" :data="goodsList" :height="tableHeight" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column v-if="true" :label="t('id', '商品ID')" prop="id" align="center" min-width="105" />
        <el-table-column :label="t('category', '商品分类')" prop="category" align="center" />
        <el-table-column :label="t('code', '商品编码')" prop="code" align="center" />
        <el-table-column :label="t('name', '商品名称')" prop="name" align="center" />
        <el-table-column :label="t('img', '商品主图')" prop="img" align="center" width="80">
          <template #default="{ row }">
            <ImagePreview :src="row.img" />
          </template>
        </el-table-column>
        <el-table-column :label="t('specType', '规格类型')" prop="specType" align="center" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.specType === '0'" type="success">{{ t('Single Spec', '单规格') }}</el-tag>
            <el-tag v-else-if="row.specType === '1'" type="primary">{{ t('Multi Spec', '多规格') }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('price', '价格')" prop="price" align="center" />
        <el-table-column :label="t('stock', '库存')" prop="stock" align="center" />
        <el-table-column :label="t('salesCount', '销量')" prop="salesCount" align="center" />
        <el-table-column :label="t('status', '状态')" prop="status" align="center">
          <template #default="{ row }">
            <AFormSwitch v-model="row.status" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column :label="t('sortOrder', '排序')" prop="sortOrder" align="center" />
        <el-table-column :label="t('createTime', '创建时间')" prop="createTime" align="center" width="105" />
        <el-table-column :label="t('updateTime', '更新时间')" prop="updateTime" align="center" width="105" />
        <el-table-column :label="t('remark', '备注')" prop="remark" align="center" />
        <el-table-column :label="t('操作')" align="center" fixed="right" width="120">
          <template #default="{ row }">
            <el-tooltip :content="t('查看')" placement="top">
              <el-button v-permi="['mall:goods:query']" link type="primary" icon="View" @click="handleView(row)"></el-button>
            </el-tooltip>
            <el-tooltip :content="t('修改')" placement="top">
              <el-button v-permi="['mall:goods:update']" link type="success" icon="Edit" @click="handleUpdate(row)"></el-button>
            </el-tooltip>
            <el-tooltip :content="t('删除')" placement="top">
              <el-button v-permi="['mall:goods:delete']" link type="danger" icon="Delete" @click="handleDelete(row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 添加或修改商品对话框 -->
    <AModal width="72%" v-model="dialog.visible" :title="dialog.title" mode="dialog" :loading="buttonLoading" @confirm="submitForm" @cancel="cancel">
      <el-form ref="goodsFormRef" :model="form" :rules="rules" label-width="auto">
        <el-row :gutter="10">
          <AFormInput label="商品分类" v-model="form.category" :maxlength="30" prop="category" span="auto"></AFormInput>
          <AFormInput label="商品编码" v-model="form.code" :maxlength="30" prop="code" span="auto"></AFormInput>
          <AFormInput label="商品名称" v-model="form.name" :maxlength="200" prop="name" span="auto"></AFormInput>
          <!-- 规格类型选择 -->
          <AFormRadio
            :label="t('Spec Type', '规格类型')"
            v-model="form.specType"
            prop="specType"
            :options="specTypeOptions"
            span="auto"
            @change="handleSpecTypeChange"
          ></AFormRadio>
          <AFormImgUpload label="商品主图" v-model="form.img" prop="img" span="auto" enable-oss-media-manager></AFormImgUpload>
          <AFormImgUpload label="商品图片集" v-model="form.imgs" prop="imgs" span="auto" :limit="9" enable-oss-media-manager></AFormImgUpload>

          <!-- 单规格时显示价格库存字段 -->
          <template v-if="form.specType === '0'">
            <AFormInput
              label="原价"
              v-model="form.originalPrice"
              prop="originalPrice"
              type="text"
              placeholder="请输入原价"
              span="auto"
              @input="calculateFromOriginalPrice"
            >
            </AFormInput>

            <AFormInput
              label="折扣"
              v-model="form.discount"
              prop="discount"
              type="text"
              placeholder="请输入折扣(0-1)"
              span="auto"
              @input="calculateFromDiscount"
              :tooltip="`当前折扣: ${getDiscountPercent()}%`"
            >
            </AFormInput>

            <AFormInput label="价格" v-model="form.price" prop="price" type="text" placeholder="请输入价格" span="auto" @input="calculateFromPrice">
            </AFormInput>

            <AFormInput label="库存" v-model="form.stock" prop="stock" span="auto"></AFormInput>
          </template>

          <AFormInput label="销量" v-model="form.salesCount" prop="salesCount" span="auto" disabled></AFormInput>
          <AFormRadio label="状态" v-model="form.status" prop="status" :options="sys_enable_status" span="auto"></AFormRadio>
          <AFormInput label="排序" v-model="form.sortOrder" prop="sortOrder" type="number" :min="0" :max="9999" span="auto"></AFormInput>
          <AFormInput label="备注" v-model="form.remark" type="textarea" prop="remark" :maxlength="255" span="auto"></AFormInput>
          <AFormEditor label="商品描述" v-model="form.description" prop="description"></AFormEditor>
        </el-row>

        <!-- 多规格时显示SKU管理 -->
        <div v-if="form.specType === '1'" style="margin-top: 20px">
          <GoodsSkuManager v-model="form.skuList" />
        </div>
      </el-form>
    </AModal>

    <!-- 查看商品详情对话框 -->
    <ADetail :column="2" v-model="viewDialog.visible" :title="viewDialog.title" :data="viewData" :fields="detailFields" />
  </div>
</template>

<script setup lang="ts" name="Goods">
import { pageGoods, getGoods, addGoods, updateGoods, deleteGoods, syncGoodsDataFromSku } from '@/api/business/mall/goods/goodsApi'
import type { GoodsQuery, GoodsBo, GoodsVo } from '@/api/business/mall/goods/goodsTypes'
import { listByGoodsId, batchSaveByGoodsId } from '@/api/business/mall/goodsSku/goodsSkuApi'
import type { GoodsSkuBo } from '@/api/business/mall/goodsSku/goodsSkuTypes'
import GoodsSkuManager from './GoodsSkuManager.vue'
import { isTrue, toggleStatus } from '@/utils/boolean'
import { addDateRange } from '@/utils/date'
import { toValidate } from '@/utils/to'
import { showMsgSuccess, showMsgWarning, showMsgError, showConfirm } from '@/utils/modal'

/** 商品字典数据 */
const { sys_enable_status } = useDict(DictTypes.sys_enable_status)

const { t } = useI18n()

// 使用表格高度处理钩子
const { tableHeight, queryFormRef, showSearch } = useTableHeight()

/** 规格类型选项（国际化） */
const specTypeOptions = computed(() => [
  { label: t('Single Spec', '单规格'), value: '0' },
  { label: t('Multi Spec', '多规格'), value: '1' }
])

// =========== 商品查询相关 ===========

/**查询参数对象*/
const queryParams = ref<GoodsQuery>({
  pageNum: 1,
  pageSize: 10,
  orderByColumn: 'id',
  isAsc: 'desc',
  id: undefined,
  category: undefined,
  code: undefined,
  name: undefined,
  img: undefined,
  originalPrice: undefined,
  discount: undefined,
  price: undefined,
  description: undefined,
  stock: undefined,
  salesCount: undefined,
  status: undefined,
  sortOrder: undefined
})

/**日期范围选择器*/
const dateRangeCreateTime = ref<[ElDateModelType, ElDateModelType]>(['', ''])

/** 商品搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1
  getList()
}

/** 商品重置按钮操作 */
const resetQuery = () => {
  dateRangeCreateTime.value = ['', '']
  queryFormRef.value?.resetFields()
  handleQuery()
}

// =========== 商品表格数据相关 ===========
/**表格加载状态*/
const isLoading = ref(true)
/**数据列表*/
const goodsList = ref<GoodsVo[]>([])
/**总记录数*/
const total = ref(0)
/**表格实例*/
const goodsTableRef = ref()
/**选中的数据项*/
const selectionItems = ref<GoodsVo[]>([])

/** 表格多选事件处理 */
const handleSelectionChange = (selection: GoodsVo[]) => {
  selectionItems.value = selection
}

/** 查询商品列表 */
const getList = async () => {
  isLoading.value = true
  queryParams.value.params = {}
  addDateRange(queryParams.value, dateRangeCreateTime.value, 'createTime')
  const [err, data] = await pageGoods(queryParams.value)
  if (!err) {
    goodsList.value = data.records
    total.value = data.total
  }
  isLoading.value = false
}

/** 导出商品数据 */
const handleExport = () => {
  useDownload().exportExcel(t('Goods Data', '商品'), '/mall/goods/exportGoods', queryParams.value)
}

/** 删除商品操作 */
const handleDelete = async (row?: GoodsVo) => {
  const idsToDelete = row ? [row.id] : selectionItems.value.map((item) => item.id)
  if (idsToDelete.length === 0) return
  const itemsToDelete = row ? row.name || row.id : selectionItems.value.map((item) => item.name || item.id).join(', ')

  const [confirmErr] = await showConfirm(`${t('是否确认删除')}${itemsToDelete}`)
  if (confirmErr) return

  const [deleteErr] = await deleteGoods(idsToDelete)
  if (!deleteErr) {
    showMsgSuccess(t('message.deleteSuccess'))
    await getList()
  }
}

// =========== 商品表单相关 ===========
/**初始表单数据*/
const initFormData: GoodsBo & { skuList?: GoodsSkuBo[] } = {
  id: undefined,
  category: undefined,
  code: undefined,
  name: undefined,
  img: undefined,
  imgs: undefined,
  specType: '0',
  originalPrice: undefined,
  discount: '1.00',
  price: undefined,
  description: undefined,
  stock: undefined,
  salesCount: 0,
  status: '1',
  sortOrder: 999,
  remark: undefined,
  skuList: []
}

/**表单引用*/
const goodsFormRef = ref<ElFormInstance>()
/**表单提交按钮加载状态*/
const buttonLoading = ref(false)
/**对话框配置对象*/
const dialog = ref<DialogState>({
  visible: false,
  title: ''
})
/**表单数据对象*/
const form = ref<GoodsBo & { skuList?: GoodsSkuBo[] }>({ ...initFormData })

/** 防止循环计算的标识 */
const isCalculating = ref(false)

/** 根据原价和折扣计算价格 */
const calculateFromOriginalPrice = () => {
  if (isCalculating.value) return

  const originalPrice = parseFloat(form.value.originalPrice || '0')
  const discount = parseFloat(form.value.discount || '1')

  if (originalPrice > 0 && discount > 0) {
    isCalculating.value = true
    form.value.price = (originalPrice * discount).toFixed(2)
    nextTick(() => {
      isCalculating.value = false
    })
  }
}

/** 根据折扣计算价格（当原价存在时） */
const calculateFromDiscount = () => {
  if (isCalculating.value) return

  const originalPrice = parseFloat(form.value.originalPrice || '0')
  const discount = parseFloat(form.value.discount || '1')

  if (originalPrice > 0 && discount >= 0) {
    isCalculating.value = true
    form.value.price = (originalPrice * discount).toFixed(2)
    nextTick(() => {
      isCalculating.value = false
    })
  }
}

/** 根据价格计算折扣（当原价存在时） */
const calculateFromPrice = () => {
  if (isCalculating.value) return

  const originalPrice = parseFloat(form.value.originalPrice || '0')
  const price = parseFloat(form.value.price || '0')

  if (originalPrice > 0 && price >= 0) {
    isCalculating.value = true
    form.value.discount = (price / originalPrice).toFixed(2)
    nextTick(() => {
      isCalculating.value = false
    })
  }
}

/** 获取折扣百分比显示 */
const getDiscountPercent = () => {
  const discount = parseFloat(form.value.discount || '1')
  return (discount * 100).toFixed(0)
}

/** 规格类型切换处理 */
const handleSpecTypeChange = (value: string) => {
  if (value === '0') {
    // 切换到单规格，清空SKU列表
    form.value.skuList = []
  } else {
    // 切换到多规格，清空单规格的价格库存
    form.value.price = undefined
    form.value.stock = undefined
  }
}

/**表单校验规则*/
const rules = ref<ElFormRules>({
  id: [{ required: true, message: t('id cannot be empty', '商品ID不能为空'), trigger: 'blur' }],
  name: [{ required: true, message: t('name cannot be empty', '商品名称不能为空'), trigger: 'blur' }],
  originalPrice: [{ pattern: /^\d+(\.\d{1,2})?$/, message: '原价格式不正确，请输入正数，最多保留2位小数', trigger: 'blur' }],
  discount: [{ pattern: /^(0(\.\d{1,2})?|1(\.0{1,2})?)$/, message: '折扣必须在0-1之间，最多保留2位小数', trigger: 'blur' }],
  price: [
    {
      validator: (rule, value, callback) => {
        // 只在单规格模式下验证价格必填
        if (form.value.specType === '0') {
          if (!value) {
            callback(new Error(t('price cannot be empty', '价格不能为空')))
            return
          }
          if (!/^\d+(\.\d{1,2})?$/.test(value)) {
            callback(new Error('价格格式不正确，请输入正数，最多保留2位小数'))
            return
          }
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
})

/** 商品表单重置 */
const reset = () => {
  form.value = { ...initFormData }
  goodsFormRef.value?.resetFields()
}

/** 取消商品编辑 */
const cancel = () => {
  reset()
  dialog.value.visible = false
}

/** 新增商品操作 */
const handleAdd = () => {
  reset()
  dialog.value.visible = true
  dialog.value.title = `${t('新增')}${t('goods', '商品')}`
}

/** 修改商品操作 */
const handleUpdate = async (row?: GoodsVo) => {
  reset()
  const itemToEdit = row || selectionItems.value[0]
  const [err, data] = await getGoods(itemToEdit.id)
  if (!err) {
    Object.assign(form.value, data)

    // 如果是多规格商品，加载SKU列表
    if (data.specType === '1') {
      const [skuErr, skuData] = await listByGoodsId(itemToEdit.id)
      if (!skuErr) {
        // 转换SKU数据中的字符串价格为数字
        form.value.skuList = (skuData || []).map((sku) => ({
          ...sku,
          originalPrice: sku.originalPrice ? Number(sku.originalPrice) : undefined,
          price: sku.price ? Number(sku.price) : undefined
        }))
      }
    }

    dialog.value.visible = true
    dialog.value.title = `${t('修改')}${t('goods', '商品')}`
  }
}

/** 提交商品表单 */
const submitForm = async () => {
  const [validateErr] = await toValidate(goodsFormRef)
  if (validateErr) return

  // 多规格时校验SKU数据
  if (form.value.specType === '1') {
    if (!form.value.skuList || form.value.skuList.length === 0) {
      showMsgWarning('请至少添加一个SKU')
      return
    }

    // 校验SKU数据完整性
    for (let i = 0; i < form.value.skuList.length; i++) {
      const sku = form.value.skuList[i]
      if (!sku.skuName) {
        showMsgWarning(`第${i + 1}个SKU的名称不能为空`)
        return
      }
      if (!sku.specValues) {
        showMsgWarning(`第${i + 1}个SKU的规格值不能为空`)
        return
      }
      if (!sku.price) {
        showMsgWarning(`第${i + 1}个SKU的价格不能为空`)
        return
      }
    }
  }

  buttonLoading.value = true
  let err: Error | null, data: any

  // 保存商品基本信息
  if (form.value.id) {
    ;[err, data] = await updateGoods(form.value)
  } else {
    ;[err, data] = await addGoods(form.value)
  }

  if (!err) {
    const goodsId = form.value.id || data

    // 如果是多规格，保存SKU数据
    if (form.value.specType === '1' && goodsId) {
      const [skuErr] = await batchSaveByGoodsId(goodsId, form.value.skuList || [])
      if (skuErr) {
        showMsgError('商品保存成功，但SKU保存失败')
        buttonLoading.value = false
        return
      }

      // SKU保存成功后,同步商品主表数据(价格、库存、销量)
      await syncGoodsDataFromSku(goodsId)
    }

    showMsgSuccess(form.value.id ? t('message.updateSuccess') : t('message.addSuccess'))
    dialog.value.visible = false
    await getList()
  }
  buttonLoading.value = false
}

/** 商品启用禁用状态修改 */
const handleStatusChange = async (row: GoodsVo) => {
  const text = isTrue(row.status) ? t('启用') : t('停用')
  const [confirmErr] = await showConfirm(`${t('是否确认')}${text}${row.id}?`)
  if (confirmErr) {
    row.status = toggleStatus(row.status)
    return
  }
  const [updateErr] = await updateGoods(row)
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
const viewData = ref<GoodsVo>({} as GoodsVo)

/**详情字段配置 */
const detailFields = computed<FieldConfig[]>(() => [
  { prop: 'id', label: t('Goods ID', '商品ID') },
  { prop: 'category', label: t('Category', '商品分类') },
  { prop: 'code', label: t('Goods Code', '商品编码') },
  { prop: 'name', label: t('Goods Name', '商品名称') },
  { prop: 'img', label: t('Main Image', '商品主图'), type: 'image' },
  {
    prop: 'specType',
    label: t('Spec Type', '规格类型'),
    type: 'dict',
    dictOptions: specTypeOptions
  },
  { prop: 'originalPrice', label: t('Original Price', '原价') },
  { prop: 'discount', label: t('Discount', '折扣') },
  { prop: 'price', label: t('Price', '价格') },
  { prop: 'stock', label: t('Stock', '库存') },
  { prop: 'salesCount', label: t('Sales Count', '销量') },
  { prop: 'status', label: t('Status', '状态'), type: 'dict', dictOptions: sys_enable_status },
  { prop: 'sortOrder', label: t('Sort Order', '排序') },
  { prop: 'createTime', label: t('Create Time', '创建时间'), type: 'datetime' },
  { prop: 'updateTime', label: t('Update Time', '更新时间'), type: 'datetime' },
  { prop: 'description', label: t('Description', '商品描述'), type: 'html', span: 2 },
  { prop: 'remark', label: t('Remark', '备注'), span: 2 }
])

/** 查看商品详情操作 */
const handleView = async (row: GoodsVo) => {
  const [err, data] = await getGoods(row.id)
  if (!err) {
    viewData.value = data
    viewDialog.value.title = `${t('查看')}${t('goods', '商品')}`
    viewDialog.value.visible = true
  }
}

// =========== 生命周期 ===========
/**初始化商品数据列表*/
onMounted(() => {
  getList()
})
/**页面激活时刷新商品列表*/
onActivated(() => {
  if (isLoading.value) return
  getList()
})
</script>
