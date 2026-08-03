<!--
充值组件
提供在线充值功能，支持微信支付，生成二维码供用户扫码支付。
基础使用方式示例：

1. 基本用法 - 显示充值对话框
<ARecharge v-model="showRecharge" @success="handleRechargeSuccess" />

2. 完整使用示例（在父组件中）
<template>
  <div>
    <el-button type="primary" @click="openRecharge">充值</el-button>
    <ARecharge
      v-model="rechargeVisible"
      @success="onRechargeSuccess"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import ARecharge from '@/components/ARecharge.vue'

const rechargeVisible = ref(false)

// 打开充值弹窗
const openRecharge = () => {
  rechargeVisible.value = true
}

// 充值成功回调
const onRechargeSuccess = () => {
  console.log('充值成功！')
  // 这里可以刷新用户余额、更新界面等
  refreshUserBalance()
}

// 刷新用户余额
const refreshUserBalance = async () => {
  // 重新获取用户余额数据
}
</script>

3. 监听对话框状态变化
<ARecharge
  v-model="dialogVisible"
  @success="handleSuccess"
  @update:modelValue="handleDialogChange"
/>

<script setup>
// 监听对话框开关状态
const handleDialogChange = (visible: boolean) => {
  if (visible) {
    console.log('充值对话框已打开')
  } else {
    console.log('充值对话框已关闭')
  }
}
</script>

4. 在页面级组件中集成使用
<template>
  <div class="user-center">
    <div class="balance-card">
      <span>当前余额：¥{{ userBalance }}</span>
      <el-button type="primary" @click="showRecharge = true">
        立即充值
      </el-button>
    </div>

    <ARecharge
      v-model="showRecharge"
      @success="handleRechargeComplete"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'

const showRecharge = ref(false)
const userBalance = ref('0.00')

const handleRechargeComplete = () => {
  // 充值成功后刷新余额
  getUserBalance()
  showMessage('充值成功，余额已更新！')
}

const getUserBalance = async () => {
  // 获取最新余额
}
</script>
-->
<template>
  <AModal
    v-model="dialogVisible"
    :title="t('recharge.title')"
    mode="dialog"
    size="large"
    :loading="buttonLoading"
    @confirm="handlePaymentConfirm"
    :confirm-text="t('recharge.paymentCompleted')"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="auto" class="mt-5">
      <el-form-item :label="t('recharge.rechargeAmount')">
        <el-radio-group v-model="goodsId" class="mb-8">
          <el-radio-button :value="item.id" v-for="(item, index) in goodsList" :key="index">
            {{ Number(item.price) }}{{ t('recharge.yuan') }}
          </el-radio-button>
        </el-radio-group>
      </el-form-item>

      <el-form-item :label="t('recharge.paymentMethod')">
        <div class="flex gap-3">
          <div class="payment-card" :class="{ active: paymentMethod === 'wechat' }" @click="selectPaymentMethod('wechat')">
            <Icon code="wxpay" size="40px" color="#00C800"></Icon>
            <span class="text-sm font-medium">{{ t('recharge.wechatPay') }}</span>
          </div>
          <div class="payment-card" :class="{ active: paymentMethod === 'alipay' }" @click="selectPaymentMethod('alipay')">
            <Icon code="alipay" size="40px" color="#1677FF"></Icon>
            <span class="text-sm font-medium">{{ t('recharge.alipay') }}</span>
          </div>
        </div>
      </el-form-item>

      <!-- 支付宝当面付模式切换:用户扫商家二维码 / 商家扫用户付款码 -->
      <el-form-item v-if="paymentMethod === 'alipay'" label="当面付模式">
        <el-radio-group v-model="alipayMode" @change="handleAlipayModeChange">
          <el-radio-button value="NATIVE">用户扫码(展示二维码)</el-radio-button>
          <el-radio-button value="BARCODE">扫付款码(扫枪输入)</el-radio-button>
        </el-radio-group>
      </el-form-item>

      <!-- NATIVE 二维码展示 -->
      <el-row type="flex" justify="center" v-if="qrCodeUrl && !isBarcodeMode">
        <div class="flex flex-col items-center">
          <span class="-mb-2 z-1"> {{ t('recharge.scanToPay', { method: paymentMethodText, amount: Number(selectedGoods.price) }) }} </span>
          <el-image :src="qrCodeUrl" class="w-50 h-50" />
          <span
            class="-mt-2 text-blue-500 cursor-pointer text-sm z-1 hover:text-blue-600 transition-colors"
            @click="handleCreateOrder(selectedGoods)"
          >
            {{ t('recharge.refreshQrCode') }}
          </span>
        </div>
      </el-row>

      <!-- BARCODE 付款码输入(支付宝商家扫用户) -->
      <el-form-item v-if="isBarcodeMode" label="付款码">
        <div class="w-full">
          <el-input
            v-model="authCode"
            placeholder="请扫描或粘贴用户支付宝付款码(25-30 位数字)"
            clearable
            maxlength="32"
            @keyup.enter="handleBarcodePay"
            :disabled="barcodePaying"
          >
            <template #append>
              <el-button type="primary" :loading="barcodePaying" :disabled="!selectedGoods.id" @click="handleBarcodePay">
                扫码扣款
              </el-button>
            </template>
          </el-input>
          <div v-if="barcodeMessage" class="mt-2 text-sm" :class="barcodeSuccess ? 'text-green-600' : 'text-red-500'">
            {{ barcodeMessage }}
          </div>
          <div class="mt-2 text-xs text-gray-400">
            提示:付款码 1 分钟过期。商家扫码后,支付宝可能要求用户在 App 内输入密码(USERPAYING),此时请点击下方"已完成支付"按钮主动查询。
          </div>
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handlePaymentProblem">{{ t('recharge.paymentProblem') }}</el-button>
      <el-button v-superadmin v-if="orderNo" type="danger" :loading="refundLoading" @click="handleRefund">{{ t('recharge.refund') }}</el-button>
      <el-button :loading="buttonLoading" type="primary" @click="handlePaymentConfirm">{{ t('recharge.paymentCompleted') }}</el-button>
    </template>
  </AModal>
</template>

<script setup lang="ts" name="ARecharge">
import type { CreateOrderBo, CreateOrderVo, PaymentResponse } from '@/api/business/mall/order/orderTypes'
import { ref, reactive, watch, onMounted, computed } from 'vue'
import { copy } from '@/utils/function'
import { useI18n } from '@/composables/useI18n'
import { showMsgSuccess, showMsgWarning, showConfirm } from '@/utils/modal'
import { createOrder, createPayment, queryOrderStatus, refundOrder } from '@/api/business/mall/order/orderApi'
import { toWithRetry } from '@/utils/to'

/**商品数据接口*/
export interface GoodsItem {
  /**商品ID*/
  id: number | string
  /**商品价格*/
  price: string
  /**商品名称*/
  name?: string
}

/**组件属性接口*/
interface ARecharge {
  /**对话框显示状态*/
  modelValue: boolean
}

/**定义组件属性*/
const props = withDefaults(defineProps<ARecharge>(), {
  modelValue: false
})

/**定义事件*/
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  success: []
}>()

/**国际化*/
const { t } = useI18n()

/**响应式引用*/
const formRef = ref()
const buttonLoading = ref(false)
const refundLoading = ref(false)
const goodsId = ref<number | string>()
const goodsList = ref<GoodsItem[]>([
  { id: 1, name: '1分钱', price: '0.01' },
  { id: 2, name: '2分钱', price: '0.02' }
])
const orderNo = ref<string>()
const qrCodeUrl = ref<string>()
const paymentMethod = ref<'wechat' | 'alipay'>('wechat')

/**支付宝当面付模式:NATIVE 用户扫商家二维码,BARCODE 商家扫用户付款码*/
const alipayMode = ref<'NATIVE' | 'BARCODE'>('NATIVE')
/**用户付款码(BARCODE 模式)*/
const authCode = ref<string>('')
/**BARCODE 扣款进行中*/
const barcodePaying = ref<boolean>(false)
/**BARCODE 同步响应文案*/
const barcodeMessage = ref<string>('')
/**BARCODE 同步响应是否成功*/
const barcodeSuccess = ref<boolean>(false)

/**计算属性 - 是否当前为 BARCODE 模式*/
const isBarcodeMode = computed(() => paymentMethod.value === 'alipay' && alipayMode.value === 'BARCODE')

/**响应式对象*/
const form = reactive({})
const rules = reactive({})

/**计算属性 - 对话框显示状态*/
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

/**计算属性 - 选中的商品*/
const selectedGoods = computed(() => {
  return goodsList.value.find((item) => item.id === goodsId.value) || ({} as GoodsItem)
})

/**计算属性 - 支付方式文本*/
const paymentMethodText = computed(() => {
  return paymentMethod.value === 'wechat' ? t('recharge.wechat') : t('recharge.alipayShort')
})

/**监听器 - 商品ID变化*/
watch(goodsId, (val) => {
  if (val && selectedGoods.value.id && !isBarcodeMode.value) {
    // BARCODE 模式不预创建订单,等用户输入付款码时再统一创建+扣款
    handleCreateOrder(selectedGoods.value)
  }
})

/**初始化商品列表*/
const initGoodsList = async (): Promise<void> => {
  // const [err, data] = await listGoodsByClassify({ classify: '充值' })
  // goodsList.value = data
}

/**选择支付方式*/
const selectPaymentMethod = (method: 'wechat' | 'alipay') => {
  paymentMethod.value = method
  // 切换支付方式时清除之前的二维码 / 付款码状态
  qrCodeUrl.value = ''
  resetBarcodeState()
  // 微信只有 NATIVE,切到微信时强制重置 alipayMode
  if (method === 'wechat') {
    alipayMode.value = 'NATIVE'
  }
  // 如果已有商品选中且当前是 NATIVE 模式,重新生成二维码
  if (selectedGoods.value.id && !isBarcodeMode.value) {
    handleCreateOrder(selectedGoods.value)
  }
}

/**切换支付宝当面付子模式*/
const handleAlipayModeChange = (mode: 'NATIVE' | 'BARCODE' | string | number | boolean) => {
  alipayMode.value = mode as 'NATIVE' | 'BARCODE'
  qrCodeUrl.value = ''
  resetBarcodeState()
  // 切回 NATIVE 自动刷新二维码;切到 BARCODE 等用户输入付款码
  if (mode === 'NATIVE' && selectedGoods.value.id) {
    handleCreateOrder(selectedGoods.value)
  }
}

/**重置 BARCODE 临时状态*/
const resetBarcodeState = () => {
  authCode.value = ''
  barcodeMessage.value = ''
  barcodeSuccess.value = false
  barcodePaying.value = false
}

/**创建订单*/
const handleCreateOrder = async (goods: GoodsItem): Promise<void> => {
  const createOrderData: CreateOrderBo = {
    goodsId: goods.id,
    goodsName: goods.name || `${t('recharge.rechargePrefix')}${goods.price}${t('recharge.yuan')}`,
    price: goods.price,
    quantity: 1,
    buyerRemark: t('recharge.onlineRecharge')
  }

  const [err, data] = await createOrder(createOrderData)
  if (!err && data) {
    orderNo.value = data.orderNo

    // 微信走 NATIVE 扫码,支付宝走 NATIVE 当面付(alipay.trade.precreate)
    // 两者后端都会返回 qrCodeBase64,前端统一展示二维码
    const paymentData = {
      orderNo: data.orderNo,
      paymentMethod: paymentMethod.value,
      tradeType: 'NATIVE' as const
    }

    const [payErr, payData] = await createPayment(paymentData)
    if (!payErr && payData?.success) {
      qrCodeUrl.value = payData.qrCodeBase64 || ''
    } else {
      showMsgWarning(payData?.message || t('recharge.generateQrCodeFailed'))
    }
  } else {
    showMsgWarning(t('recharge.createOrderFailed'))
  }
}

/**条码支付 — 商家扫用户付款码,调用 alipay.trade.pay 同步扣款*/
const handleBarcodePay = async (): Promise<void> => {
  if (!selectedGoods.value.id) {
    showMsgWarning(t('recharge.selectRechargeAmount'))
    return
  }
  const code = authCode.value?.trim()
  if (!code) {
    showMsgWarning('请先扫描或输入用户付款码')
    return
  }
  // 支付宝付款码为 25-30 位纯数字,做基本校验防止误输入
  if (!/^\d{16,32}$/.test(code)) {
    showMsgWarning('付款码格式异常(应为 16-32 位数字)')
    return
  }

  barcodePaying.value = true
  barcodeMessage.value = ''

  // 1. 创建订单(BARCODE 模式延迟到此处创建,避免轮换商品产生多余 PENDING 订单)
  const createOrderData: CreateOrderBo = {
    goodsId: selectedGoods.value.id,
    goodsName: selectedGoods.value.name || `${t('recharge.rechargePrefix')}${selectedGoods.value.price}${t('recharge.yuan')}`,
    price: selectedGoods.value.price,
    quantity: 1,
    buyerRemark: '支付宝付款码支付'
  }
  const [orderErr, orderData] = await createOrder(createOrderData)
  if (orderErr || !orderData) {
    barcodePaying.value = false
    showMsgWarning(t('recharge.createOrderFailed'))
    return
  }
  orderNo.value = orderData.orderNo

  // 2. 调用条码支付
  const paymentData = {
    orderNo: orderData.orderNo,
    paymentMethod: 'alipay' as const,
    tradeType: 'BARCODE' as const,
    authCode: code
  }
  const [payErr, payData] = await createPayment(paymentData)
  barcodePaying.value = false

  if (payErr || !payData) {
    barcodeSuccess.value = false
    barcodeMessage.value = '请求失败,请重试'
    return
  }

  // 3. 处理三种同步响应
  if (payData.tradeState === 'SUCCESS') {
    barcodeSuccess.value = true
    barcodeMessage.value = `支付成功,交易号:${payData.transactionId || ''}`
    showMsgSuccess('扣款成功')
    emit('success')
  } else if (payData.tradeState === 'USERPAYING') {
    barcodeSuccess.value = true
    barcodeMessage.value = '用户正在输入支付密码,请稍候,然后点击下方"已完成支付"主动查询结果'
  } else {
    barcodeSuccess.value = false
    barcodeMessage.value = payData.message || '扣款失败'
  }
}

/**确认支付*/
const handlePaymentConfirm = async (): Promise<void> => {
  // BARCODE 模式:用户已扫码扣款,只需轮询查询订单状态
  if (isBarcodeMode.value) {
    if (!orderNo.value) {
      showMsgWarning('请先输入付款码并扣款')
      return
    }
    buttonLoading.value = true
    const [err] = await toWithRetry(() => queryOrderStatus(orderNo.value), 3)
    if (!err) {
      showMsgSuccess(t('recharge.paymentSuccess'))
      emit('success')
    } else {
      showMsgWarning('订单未支付或仍在处理中')
    }
    buttonLoading.value = false
    return
  }

  if (!qrCodeUrl.value || !orderNo.value) {
    showMsgWarning(t('recharge.selectRechargeAmount'))
    return
  }

  buttonLoading.value = true

  // 重试0次 需要可以重试多次,这里展示toWithRetry的用法实现轻量轮训功能
  const [err] = await toWithRetry(() => queryOrderStatus(orderNo.value), 0)
  if (!err) {
    showMsgSuccess(t('recharge.paymentSuccess'))
    // 不关闭弹窗
    // dialogVisible.value = false
    emit('success')
  }
  buttonLoading.value = false
}

/**支付遇到问题*/
const handlePaymentProblem = async () => {
  copy('770492966', t('recharge.copiedContact'))
}

/**退款处理*/
const handleRefund = async (): Promise<void> => {
  if (!orderNo.value) {
    showMsgWarning(t('recharge.noRefundableOrder'))
    return
  }

  const [confirmErr] = await showConfirm(t('recharge.confirmRefund'))
  if (confirmErr) return

  refundLoading.value = true
  const [err, data] = await refundOrder(orderNo.value)
  refundLoading.value = false

  if (!err) {
    showMsgSuccess(t('recharge.refundSuccess'))
    // 清空订单号和二维码
    orderNo.value = undefined
    qrCodeUrl.value = ''
    // 不关闭弹窗
    // dialogVisible.value = false
  }
}

/**组件挂载时初始化*/
onMounted(() => {
  initGoodsList()
})
</script>

<style scoped lang="scss">
.payment-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 24px;
  border: 1px solid transparent;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  background-color: var(--el-bg-color);

  span {
    color: var(--el-text-color-regular);
  }

  &:hover {
    background-color: var(--el-fill-color-light);
    box-shadow: 0 2px 4px var(--el-box-shadow-light);
  }

  &.active {
    border-color: var(--el-color-primary);
    background-color: var(--el-color-primary-light-9);
    box-shadow: 0 2px 4px var(--el-box-shadow-light);

    span {
      color: var(--el-color-primary);
    }
  }
}
</style>
