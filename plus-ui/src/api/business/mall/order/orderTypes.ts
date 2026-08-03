/** 订单查询类型 */
export interface OrderQuery extends PageQuery {
  /** 订单ID */
  id?: string | number

  /** 订单编号 */
  orderNo?: string

  /** 用户ID */
  userId?: string | number

  /** 商品ID */
  goodsId?: string | number

  /** 商品名称 */
  goodsName?: string

  /** 商品图片 */
  goodsImg?: string

  /** 商品价格 */
  price?: string

  /** 购买数量 */
  quantity?: number

  /** 订单总金额 */
  totalAmount?: string

  /** 实付金额 */
  actualAmount?: string

  /** 订单状态 */
  orderStatus?: string

  /** 支付方式 */
  paymentMethod?: string

  /** 支付时间 */
  paymentTime?: string

  /** 交易流水号 */
  transactionId?: string | number

  /** 买家备注 */
  buyerRemark?: string

  /** 订单扩展信息 */
  orderExtInfo?: string

  /** 收货信息 */
  receiverInfo?: string

  /** 物流信息 */
  shippingInfo?: string

  /** 创建时间 */
  createTime?: string
}

/** 订单表单类型 */
export interface OrderBo {
  /** 订单ID */
  id?: string | number

  /** 订单编号 */
  orderNo?: string

  /** 用户ID */
  userId?: string | number

  /** 商品ID */
  goodsId?: string | number

  /** 商品名称 */
  goodsName?: string

  /** 商品图片 */
  goodsImg?: string

  /** 商品价格 */
  price?: string

  /** 购买数量 */
  quantity?: number

  /** 订单总金额 */
  totalAmount?: string

  /** 实付金额 */
  actualAmount?: string

  /** 订单状态 */
  orderStatus?: string

  /** 支付方式 */
  paymentMethod?: string

  /** 支付时间 */
  paymentTime?: string

  /** 交易流水号 */
  transactionId?: string | number

  /** 买家备注 */
  buyerRemark?: string

  /** 订单扩展信息 */
  orderExtInfo?: string

  /** 收货信息 */
  receiverInfo?: string

  /** 物流信息 */
  shippingInfo?: string

  /** 备注 */
  remark?: string
}

/** 订单视图类型 */
export interface OrderVo {
  /** 订单ID */
  id: string | number

  /** 订单编号 */
  orderNo: string

  /** 用户ID */
  userId: string | number

  /** 商品ID */
  goodsId: string | number

  /** 商品名称 */
  goodsName: string

  /** 商品图片 */
  goodsImg: string

  /** 商品价格 */
  price: string

  /** 购买数量 */
  quantity: number

  /** 订单总金额 */
  totalAmount: string

  /** 实付金额 */
  actualAmount: string

  /** 订单状态 */
  orderStatus: string

  /** 支付方式 */
  paymentMethod: string

  /** 支付时间 */
  paymentTime: string

  /** 交易流水号 */
  transactionId: string | number

  /** 买家备注 */
  buyerRemark: string

  /** 订单扩展信息 */
  orderExtInfo: string

  /** 收货信息 */
  receiverInfo: string

  /** 物流信息 */
  shippingInfo: string

  /** 创建时间 */
  createTime: string

  /** 更新时间 */
  updateTime: string

  /** 备注 */
  remark: string
}

/** 创建订单请求类型 */
export interface CreateOrderBo {
  /** 商品ID */
  goodsId: string | number
  /** 商品名称 */
  goodsName?: string
  /** 商品图片 */
  goodsImg?: string
  /** 商品价格 */
  price: string | number
  /** 购买数量 */
  quantity: number
  /** 买家备注 */
  buyerRemark?: string
  /** 备注 */
  remark?: string
}

/** 创建订单响应类型 */
export interface CreateOrderVo {
  /** 订单ID */
  id: string | number
  /** 订单编号 */
  orderNo: string
  /** 商品ID */
  goodsId: string | number
  /** 商品名称 */
  goodsName: string
  /** 商品图片 */
  goodsImg?: string
  /** 商品价格 */
  price: string | number
  /** 购买数量 */
  quantity: number
  /** 订单总金额 */
  totalAmount: number
  /** 订单状态 */
  orderStatus: string
  /** 订单状态名称 */
  orderStatusName: string
  /** 买家备注 */
  buyerRemark?: string
  /** 创建时间 */
  createTime: string
}

/** 订单状态查询响应 */
export interface OrderStatusVo {
  /** 订单编号 */
  orderNo: string
  /** 订单状态 */
  orderStatus: string
  /** 订单状态名称 */
  orderStatusName: string
  /** 支付时间 */
  paymentTime?: string
  /** 是否已支付 */
  isPaid: boolean
}

/** 支付请求类型 */
export interface PaymentRequest {
  /** 订单编号 */
  orderNo: string
  /** 支付方式 */
  paymentMethod: 'wechat' | 'alipay' | 'balance'
  /** 应用ID */
  appId?: string
  /** 交易类型(支付宝 BARCODE 表示商家扫用户付款码) */
  tradeType?: 'JSAPI' | 'NATIVE' | 'APP' | 'H5' | 'WAP' | 'PAGE' | 'BARCODE'
  /** 微信openId */
  openId?: string
  /** 支付密码 */
  payPassword?: string
  /** 用户付款码(BARCODE 必填,25-30 位数字串) */
  authCode?: string
  /** 返回地址 */
  returnUrl?: string
}

/** 支付响应类型 */
export interface PaymentResponse {
  /** 是否成功 */
  success: boolean
  /** 响应消息 */
  message: string
  /** 错误码 */
  errorCode?: string
  /** 商户订单号 */
  outTradeNo: string
  /** 原始订单号 */
  orderNo: string
  /** 支付方式 */
  paymentMethod: string
  /** 支付金额 */
  totalAmount: number
  /** 第三方交易号 */
  transactionId?: string
  /** 预支付ID */
  prepayId?: string
  /** 支付参数 */
  payInfo?: Record<string, string>
  /** 二维码链接 */
  codeUrl?: string
  /** 二维码Base64 */
  qrCodeBase64?: string
  /** 支付链接 */
  payUrl?: string
  /** 支付表单(支付宝PAGE/WAP返回的HTML表单) */
  payForm?: string
  /** 支付body数据(SDK调起字符串或HTML表单) */
  body?: string
  /** 支付状态 */
  tradeState?: string
  /** 支付时间 */
  payTime?: string
  /** 过期时间 */
  expireTime?: string
}

/** 发货请求类型 */
export interface DeliverOrderBo {
  /** 订单ID */
  id: string | number
  /** 物流信息 */
  shippingInfo: string
}

/** 物流信息类型 */
export interface ShippingInfo {
  /** 快递公司 */
  courierCompany: string
  /** 快递单号 */
  trackingNumber: string
  /** 发货时间 */
  shipTime?: string
  /** 发货备注 */
  shipRemark?: string
}
