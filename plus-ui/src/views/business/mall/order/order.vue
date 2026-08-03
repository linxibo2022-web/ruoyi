<!-- 订单管理 -->
<template>
  <div>
    <!-- 订单搜索栏 -->
    <ASearchForm ref="queryFormRef" v-model="queryParams" :visible="showSearch">
      <AFormInput label="模糊搜索" prop="searchValue" v-model="queryParams.searchValue" @input="handleQuery"></AFormInput>
      <AFormInput label="用户ID" v-model="queryParams.userId" prop="userId" @input="handleQuery"></AFormInput>
      <AFormSelect
        label="订单状态"
        multiple
        v-model="queryParams.orderStatus"
        prop="orderStatus"
        :options="sys_order_status"
        @change="handleQuery"
      ></AFormSelect>
      <AFormDate v-model="dateRangeCreateTime" prop="createTime" type="daterange" label="创建时间" @change="handleQuery"></AFormDate>
    </ASearchForm>

    <el-card shadow="hover">
      <!-- 订单工具栏 -->
      <template #header>
        <el-row :gutter="10" class="mb-2">
          <el-col :span="1.5" v-permi="['mall:order:add']">
            <el-button type="primary" plain icon="Plus" @click="handleAdd">
              {{ t('新增') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['mall:order:update']">
            <el-button type="success" plain icon="Edit" :disabled="selectionItems.length !== 1" @click="handleUpdate()">
              {{ t('修改') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['mall:order:delete']">
            <el-button type="danger" plain icon="Delete" :disabled="selectionItems.length === 0" @click="handleDelete()">
              {{ t('删除') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['mall:order:import']">
            <AImportExcel
              v-slot="{ openImportExcel }"
              :title="t('Order Data', '订单')"
              templateUrl="/mall/order/templateOrders"
              importUrl="/mall/order/importOrders"
              @import-success="getList"
            >
              <el-button type="info" plain icon="Top" @click="openImportExcel">
                {{ t('导入') }}
              </el-button>
            </AImportExcel>
          </el-col>
          <el-col :span="1.5" v-permi="['mall:order:export']">
            <el-button type="warning" plain icon="Download" @click="handleExport">
              {{ t('导出') }}
            </el-button>
          </el-col>

          <TableToolbar
            v-model:showSearch="showSearch"
            @reset-query="resetQuery"
            @query-table="getList"
            :table-columns="detailFields"
            :table-data="orderList"
          ></TableToolbar>
        </el-row>

        <!-- 显示已选项 -->
        <!-- <ASelectionTags :items="selectionItems" :on-clear="selectionClear" @close="selectionRemove" /> -->
      </template>

      <!-- 订单表格数据 -->
      <el-table ref="orderTableRef" v-loading="isLoading" :data="orderList" :height="tableHeight" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column v-if="true" :label="t('id', '订单ID')" prop="id" align="center" min-width="105" />
        <el-table-column :label="t('orderNo', '订单编号')" prop="orderNo" align="center" />
        <el-table-column :label="t('userId', '用户ID')" prop="userId" align="center" />
        <el-table-column :label="t('goodsId', '商品ID')" prop="goodsId" align="center" />
        <el-table-column :label="t('goodsName', '商品名称')" prop="goodsName" align="center" />
        <el-table-column :label="t('goodsImg', '商品图片')" prop="goodsImg" align="center" width="80">
          <template #default="{ row }">
            <ImagePreview :src="row.goodsImg" />
          </template>
        </el-table-column>
        <el-table-column :label="t('price', '商品价格')" prop="price" align="center" />
        <el-table-column :label="t('quantity', '购买数量')" prop="quantity" align="center" />
        <el-table-column :label="t('totalAmount', '订单总金额')" prop="totalAmount" align="center" />
        <el-table-column :label="t('orderStatus', '订单状态')" prop="orderStatus" align="center">
          <template #default="{ row }">
            <DictTag :options="sys_order_status" :value="row.orderStatus" />
          </template>
        </el-table-column>
        <el-table-column :label="t('paymentMethod', '支付方式')" prop="paymentMethod" align="center" />
        <el-table-column :label="t('paymentTime', '支付时间')" prop="paymentTime" align="center" width="105" />
        <el-table-column :label="t('transactionId', '交易流水号')" prop="transactionId" align="center" />
        <el-table-column :label="t('buyerRemark', '买家备注')" prop="buyerRemark" align="center" />
        <el-table-column :label="t('createTime', '创建时间')" prop="createTime" align="center" width="105" />
        <el-table-column :label="t('updateTime', '更新时间')" prop="updateTime" align="center" width="105" />
        <el-table-column :label="t('remark', '备注')" prop="remark" align="center" />
        <el-table-column :label="t('操作')" align="center" fixed="right" width="160">
          <template #default="{ row }">
            <el-tooltip :content="t('查看')" placement="top">
              <el-button v-permi="['mall:order:query']" link type="primary" icon="View" @click="handleView(row)"></el-button>
            </el-tooltip>
            <el-tooltip :content="t('发货')" placement="top" v-if="row.orderStatus === 'paid'">
              <el-button v-permi="['mall:order:update']" link type="warning" icon="Promotion" @click="handleDeliver(row)"></el-button>
            </el-tooltip>
            <el-tooltip :content="t('修改')" placement="top">
              <el-button v-permi="['mall:order:update']" link type="success" icon="Edit" @click="handleUpdate(row)"></el-button>
            </el-tooltip>
            <el-tooltip :content="t('删除')" placement="top">
              <el-button v-permi="['mall:order:delete']" link type="danger" icon="Delete" @click="handleDelete(row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 添加或修改订单对话框 -->
    <AModal v-model="dialog.visible" size="large" :title="dialog.title" :loading="buttonLoading" @confirm="submitForm" @cancel="cancel">
      <el-form ref="orderFormRef" :model="form" :rules="rules" label-width="auto">
        <el-row :gutter="10">
          <AFormInput label="订单编号" v-model="form.orderNo" :maxlength="32" prop="orderNo" span="auto"></AFormInput>
          <AFormInput label="用户ID" v-model="form.userId" prop="userId" span="auto"></AFormInput>
          <AFormInput label="商品ID" v-model="form.goodsId" prop="goodsId" span="auto"></AFormInput>
          <AFormInput label="商品名称" v-model="form.goodsName" :maxlength="200" prop="goodsName" span="auto"></AFormInput>
          <AFormImgUpload label="商品图片" v-model="form.goodsImg" prop="goodsImg" span="auto"></AFormImgUpload>
          <AFormInput label="商品价格" v-model="form.price" prop="price" span="auto"></AFormInput>
          <AFormInput label="购买数量" v-model="form.quantity" prop="quantity" span="auto"></AFormInput>
          <AFormInput label="订单总金额" v-model="form.totalAmount" prop="totalAmount" span="auto"></AFormInput>
          <AFormInput label="实付金额" v-model="form.actualAmount" prop="actualAmount" span="auto"></AFormInput>
          <AFormSelect label="订单状态" v-model="form.orderStatus" :options="sys_order_status" prop="orderStatus" span="auto"></AFormSelect>
          <AFormInput label="支付方式" v-model="form.paymentMethod" prop="paymentMethod" span="auto"></AFormInput>
          <AFormDate label="支付时间" v-model="form.paymentTime" type="datetime" prop="paymentTime" span="auto"></AFormDate>
          <AFormInput label="交易流水号" v-model="form.transactionId" :maxlength="64" prop="transactionId" span="auto"></AFormInput>
          <AFormInput label="买家备注" v-model="form.buyerRemark" type="textarea" :maxlength="500" prop="buyerRemark" span="auto"></AFormInput>
          <AFormInput label="订单扩展信息" v-model="form.orderExtInfo" type="textarea" :maxlength="500" prop="orderExtInfo" span="auto"></AFormInput>
          <AFormInput label="收货信息" v-model="form.receiverInfo" type="textarea" :maxlength="500" prop="receiverInfo" span="auto"></AFormInput>
          <AFormInput label="物流信息" v-model="form.shippingInfo" type="textarea" :maxlength="1000" prop="shippingInfo" span="auto"></AFormInput>
          <AFormInput label="备注" v-model="form.remark" type="textarea" prop="remark" :maxlength="255" span="auto"></AFormInput>
        </el-row>
      </el-form>
    </AModal>

    <!-- 查看订单详情对话框 -->
    <ADetail :column="2" v-model="viewDialog.visible" :title="viewDialog.title" :data="viewData" :fields="detailFields" />

    <!-- 发货对话框 -->
    <AModal
      size="small"
      v-model="deliverDialog.visible"
      :title="deliverDialog.title"
      :loading="buttonLoading"
      @confirm="submitDeliver"
      @cancel="cancelDeliver"
    >
      <el-form ref="deliverFormRef" :model="deliverForm" :rules="deliverRules" label-width="auto">
        <el-row :gutter="10">
          <AFormInput label="订单编号" disabled v-model="deliverForm.orderNo" span="24" />
          <AFormInput label="商品信息" disabled v-model="deliverForm.goodsName" span="24" />
          <AFormSelect
            disabled
            label="快递公司"
            v-model="deliverForm.courierCompany"
            prop="courierCompany"
            :options="courierOptions"
            placeholder="请选择快递公司"
            span="auto"
          />
          <AFormInput label="快递单号" v-model="deliverForm.trackingNumber" prop="trackingNumber" placeholder="请输入快递单号" span="auto" />
          <AFormInput label="发货备注" v-model="deliverForm.shipRemark" type="textarea" :rows="3" placeholder="请输入发货备注（可选）" span="auto" />
        </el-row>
      </el-form>
    </AModal>
  </div>
</template>

<script setup lang="ts" name="Order">
import { pageOrders, getOrder, addOrder, updateOrder, deleteOrders, deliverOrder } from '@/api/business/mall/order/orderApi'
import type { OrderQuery, OrderBo, OrderVo, ShippingInfo } from '@/api/business/mall/order/orderTypes'
import { addDateRange, initDateRangeFromQuery } from '@/utils/date'
import { toValidate } from '@/utils/to'
import { showMsgSuccess, showConfirm } from '@/utils/modal'

/** 订单字典数据 */
const { sys_order_status } = useDict(DictTypes.sys_order_status)

const { t } = useI18n()
const route = useRoute()

// 使用表格高度处理钩子
const { tableHeight, queryFormRef, showSearch, calculateTableHeight } = useTableHeight()

// =========== 订单查询相关 ===========

/**查询参数对象*/
const queryParams = ref<OrderQuery>({
  pageNum: 1,
  pageSize: 10,
  orderByColumn: 'updateTime',
  isAsc: 'desc',
  id: undefined,
  orderNo: undefined,
  userId: undefined,
  goodsId: undefined,
  goodsName: undefined,
  goodsImg: undefined,
  price: undefined,
  quantity: undefined,
  totalAmount: undefined,
  actualAmount: undefined,
  orderStatus: undefined,
  paymentMethod: undefined,
  transactionId: undefined,
  buyerRemark: undefined,
  orderExtInfo: undefined,
  receiverInfo: undefined,
  shippingInfo: undefined
})

/**日期范围选择器*/
const dateRangePaymentTime = ref<[ElDateModelType, ElDateModelType]>(['', ''])
/**日期范围选择器*/
const dateRangeCreateTime = ref<[ElDateModelType, ElDateModelType]>(['', ''])

/**
 * 根据路由参数初始化查询条件
 */
const initQueryFromRoute = () => {
  const { orderStatus, orderBy } = route.query

  // 订单状态筛选
  if (orderStatus) {
    queryParams.value.orderStatus = orderStatus as string
  }

  // 排序设置
  if (orderBy === 'totalAmount') {
    queryParams.value.orderByColumn = 'totalAmount'
    queryParams.value.isAsc = 'desc'
  }

  // 初始化日期范围
  dateRangeCreateTime.value = initDateRangeFromQuery(route.query)
}

/** 订单搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1
  getList()
}

/** 订单重置按钮操作 */
const resetQuery = () => {
  dateRangePaymentTime.value = ['', '']
  dateRangeCreateTime.value = ['', '']
  queryFormRef.value?.resetFields()
  handleQuery()
}

// =========== 订单表格数据相关 ===========
/**表格加载状态*/
const isLoading = ref(true)
/**数据列表*/
const orderList = ref<OrderVo[]>([])
/**总记录数*/
const total = ref(0)
/**表格实例*/
const orderTableRef = ref()
/**选中的数据项*/
const selectionItems = ref<OrderVo[]>([])

/** 表格多选事件处理 */
const handleSelectionChange = (selection: OrderVo[]) => {
  selectionItems.value = selection
}

/** 查询订单列表 */
const getList = async () => {
  isLoading.value = true
  queryParams.value.params = {}
  addDateRange(queryParams.value, dateRangePaymentTime.value, 'paymentTime')
  addDateRange(queryParams.value, dateRangeCreateTime.value, 'createTime')
  const [err, data] = await pageOrders(queryParams.value)
  if (!err) {
    orderList.value = data.records
    total.value = data.total
  }
  isLoading.value = false
}

/** 导出订单数据 */
const handleExport = () => {
  useDownload().exportExcel(t('Order Data', '订单'), '/mall/order/exportOrders', queryParams.value)
}

/** 删除订单操作 */
const handleDelete = async (row?: OrderVo) => {
  const idsToDelete = row ? [row.id] : selectionItems.value.map((item) => item.id)
  if (idsToDelete.length === 0) return
  const itemsToDelete = row ? row.orderNo || row.id : selectionItems.value.map((item) => item.orderNo || item.id).join(', ')

  const [confirmErr] = await showConfirm(`${t('是否确认删除')}${itemsToDelete}`)
  if (confirmErr) return

  const [deleteErr] = await deleteOrders(idsToDelete)
  if (!deleteErr) {
    showMsgSuccess(t('message.deleteSuccess'))
    await getList()
  }
}

// =========== 订单表单相关 ===========
/**初始表单数据*/
const initFormData: OrderBo = {
  id: undefined,
  orderNo: undefined,
  userId: undefined,
  goodsId: undefined,
  goodsName: undefined,
  goodsImg: undefined,
  price: undefined,
  quantity: undefined,
  totalAmount: undefined,
  actualAmount: undefined,
  orderStatus: 'pending',
  paymentMethod: undefined,
  paymentTime: undefined,
  transactionId: undefined,
  buyerRemark: undefined,
  orderExtInfo: undefined,
  receiverInfo: undefined,
  shippingInfo: undefined,
  remark: undefined
}

/**表单引用*/
const orderFormRef = ref<ElFormInstance>()
/**表单提交按钮加载状态*/
const buttonLoading = ref(false)
/**对话框配置对象*/
const dialog = ref<DialogState>({
  visible: false,
  title: ''
})
/**表单数据对象*/
const form = ref<OrderBo>({ ...initFormData })
/**表单校验规则*/
const rules = ref<ElFormRules>({
  id: [{ required: true, message: t('id cannot be empty', '订单ID不能为空'), trigger: 'blur' }],
  orderNo: [{ required: true, message: t('orderNo cannot be empty', '订单编号不能为空'), trigger: 'blur' }],
  userId: [{ required: true, message: t('userId cannot be empty', '用户ID不能为空'), trigger: 'blur' }],
  goodsId: [{ required: true, message: t('goodsId cannot be empty', '商品ID不能为空'), trigger: 'blur' }],
  goodsName: [{ required: true, message: t('goodsName cannot be empty', '商品名称不能为空'), trigger: 'blur' }],
  price: [{ required: true, message: t('price cannot be empty', '商品价格不能为空'), trigger: 'blur' }],
  quantity: [{ required: true, message: t('quantity cannot be empty', '购买数量不能为空'), trigger: 'blur' }],
  totalAmount: [{ required: true, message: t('totalAmount cannot be empty', '订单总金额不能为空'), trigger: 'blur' }]
})

/** 订单表单重置 */
const reset = () => {
  form.value = { ...initFormData }
  orderFormRef.value?.resetFields()
}

/** 取消订单编辑 */
const cancel = () => {
  reset()
  dialog.value.visible = false
}

/** 新增订单操作 */
const handleAdd = () => {
  reset()
  dialog.value.visible = true
  dialog.value.title = `${t('新增')}${t('order', '订单')}`
}

/** 修改订单操作 */
const handleUpdate = async (row?: OrderVo) => {
  reset()
  const itemToEdit = row || selectionItems.value[0]
  const [err, data] = await getOrder(itemToEdit.id)
  if (!err) {
    Object.assign(form.value, data)
    dialog.value.visible = true
    dialog.value.title = `${t('修改')}${t('order', '订单')}`
  }
}

/** 提交订单表单 */
const submitForm = async () => {
  const [validateErr] = await toValidate(orderFormRef)
  if (validateErr) return

  buttonLoading.value = true
  let err: Error | null, data: any
  if (form.value.id) {
    ;[err, data] = await updateOrder(form.value)
  } else {
    ;[err, data] = await addOrder(form.value)
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
const viewData = ref<OrderVo>({} as OrderVo)

// =========== 发货相关 ===========
/**发货对话框配置*/
const deliverDialog = ref<DialogState>({
  visible: false,
  title: ''
})

/**发货表单引用*/
const deliverFormRef = ref<ElFormInstance>()

/**发货表单数据*/
const deliverForm = ref({
  id: undefined as string | number | undefined,
  orderNo: '',
  goodsName: '',
  courierCompany: '',
  trackingNumber: '',
  shipRemark: ''
})

/**发货表单校验规则*/
const deliverRules = ref<ElFormRules>({
  courierCompany: [{ required: true, message: '请选择快递公司', trigger: 'change' }],
  trackingNumber: [{ required: true, message: '请输入快递单号', trigger: 'blur' }]
})

/**快递公司选项*/
const courierOptions = ref([
  { label: '顺丰快递', value: 'SF' },
  { label: '中通快递', value: 'ZTO' },
  { label: '韵达快递', value: 'YD' },
  { label: '圆通快递', value: 'YTO' },
  { label: '申通快递', value: 'STO' },
  { label: '京东物流', value: 'JD' },
  { label: '邮政EMS', value: 'EMS' },
  { label: '德邦快递', value: 'DBL' },
  { label: '百世快递', value: 'HTKY' }
])

/**详情字段配置 */
const detailFields = ref<FieldConfig[]>([
  { prop: 'id', label: '订单ID' },
  { prop: 'orderNo', label: '订单编号' },
  { prop: 'userId', label: '用户ID' },
  { prop: 'goodsId', label: '商品ID' },
  { prop: 'goodsName', label: '商品名称' },
  { prop: 'goodsImg', label: '商品图片', type: 'image' },
  { prop: 'price', label: '商品价格' },
  { prop: 'quantity', label: '购买数量' },
  { prop: 'totalAmount', label: '订单总金额' },
  { prop: 'actualAmount', label: '实付金额' },
  { prop: 'orderStatus', label: '订单状态' },
  { prop: 'paymentMethod', label: '支付方式' },
  { prop: 'paymentTime', label: '支付时间', type: 'datetime' },
  { prop: 'transactionId', label: '交易流水号' },
  { prop: 'buyerRemark', label: '买家备注' },
  { prop: 'orderExtInfo', label: '订单扩展信息' },
  { prop: 'receiverInfo', label: '收货信息' },
  { prop: 'shippingInfo', label: '物流信息' },
  { prop: 'createTime', label: '创建时间', type: 'datetime' },
  { prop: 'updateTime', label: '更新时间', type: 'datetime' },
  { prop: 'remark', label: '备注', span: 2 }
])

/** 查看订单详情操作 */
const handleView = async (row: OrderVo) => {
  const [err, data] = await getOrder(row.id)
  if (!err) {
    viewData.value = data
    viewDialog.value.title = `${t('查看')}${t('order', '订单')}`
    viewDialog.value.visible = true
  }
}

/** 发货操作 */
const handleDeliver = (row: OrderVo) => {
  resetDeliverForm()
  deliverForm.value.id = row.id
  deliverForm.value.orderNo = row.orderNo
  deliverForm.value.goodsName = row.goodsName
  deliverForm.value.courierCompany = 'SF'
  deliverDialog.value.visible = true
  deliverDialog.value.title = `发货订单：${row.orderNo}`
}

/** 重置发货表单 */
const resetDeliverForm = () => {
  deliverForm.value = {
    id: undefined,
    orderNo: '',
    goodsName: '',
    courierCompany: '',
    trackingNumber: '',
    shipRemark: ''
  }
  deliverFormRef.value?.resetFields()
}

/** 取消发货 */
const cancelDeliver = () => {
  resetDeliverForm()
  deliverDialog.value.visible = false
}

/** 提交发货 */
const submitDeliver = async () => {
  const [validateErr] = await toValidate(deliverFormRef)
  if (validateErr) return

  buttonLoading.value = true

  // 构建物流信息JSON
  const shippingInfo: ShippingInfo = {
    courierCompany: deliverForm.value.courierCompany,
    trackingNumber: deliverForm.value.trackingNumber,
    shipTime: new Date().toISOString(),
    shipRemark: deliverForm.value.shipRemark
  }

  const [err] = await deliverOrder(deliverForm.value.id!, JSON.stringify(shippingInfo))
  if (!err) {
    showMsgSuccess('发货成功')
    deliverDialog.value.visible = false
    await getList()
  }
  buttonLoading.value = false
}

// =========== 生命周期 ===========
/**初始化订单数据列表*/
onMounted(() => {
  // 根据路由参数初始化查询条件
  initQueryFromRoute()
  getList()
})

/**页面激活时刷新订单列表*/
onActivated(() => {
  if (isLoading.value) return
  // 重新检查路由参数
  initQueryFromRoute()
  getList()
})
</script>
