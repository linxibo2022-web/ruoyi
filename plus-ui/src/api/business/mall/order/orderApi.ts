// 订单管理 API
import type { OrderQuery, OrderBo, OrderVo, CreateOrderBo, CreateOrderVo, OrderStatusVo, PaymentRequest, PaymentResponse } from './orderTypes'

/**
 * 查询订单列表
 * @param query 查询参数
 * @returns {Result<PageResult<OrderVo>>} 结果
 */
export const pageOrders = (query?: OrderQuery): Result<PageResult<OrderVo>> => {
  return http.get<PageResult<OrderVo>>('/mall/order/pageOrders', query)
}

/**
 * 查询订单详细
 * @param id 订单ID
 * @returns {Result<OrderVo>} 结果
 */
export const getOrder = (id: string | number): Result<OrderVo> => {
  return http.get<OrderVo>(`/mall/order/getOrder/${id}`)
}

/**
 * 新增订单
 * @param data 订单数据
 * @returns {Result<string | number>} 结果
 */
export const addOrder = (data: OrderBo): Result<string | number> => {
  return http.post<string | number>('/mall/order/addOrder', data)
}

/**
 * 修改订单
 * @param data 订单数据
 * @returns {Result<void>} 结果
 */
export const updateOrder = (data: OrderBo): Result<void> => {
  return http.put<void>('/mall/order/updateOrder', data)
}

/**
 * 订单发货
 * @param id 订单ID
 * @param shippingInfo 物流信息
 * @returns {Result<void>} 结果
 */
export const deliverOrder = (id: string | number, shippingInfo: string): Result<void> => {
  return http.put<void>('/mall/order/deliverOrder', { id, shippingInfo })
}

/**
 * 删除订单
 * @param ids 订单ID
 * @returns {Result<void>} 结果
 */
export const deleteOrders = (ids: string | number | Array<string | number>): Result<void> => {
  return http.del<void>(`/mall/order/deleteOrders/${ids}`)
}

/**
 * 创建订单
 * @param data 订单信息
 * @returns {Result<CreateOrderVo>} 结果
 */
export const createOrder = (data: CreateOrderBo): Result<CreateOrderVo> => {
  return http.post<CreateOrderVo>(`/common/mall/order/createOrder`, data)
}

/**
 * 查询订单状态
 * @param orderNo 订单号
 * @returns {Result<OrderStatusVo>} 结果
 */
export const queryOrderStatus = (orderNo: string): Result<OrderStatusVo> => {
  return http.get<OrderStatusVo>(`/common/mall/order/queryOrderStatus`, {
    orderNo
  })
}

/**
 * 根据订单号获取订单信息
 * @param orderNo 订单号
 * @returns {Result<OrderVo>} 结果
 */
export const getOrderByOrderNo = (orderNo: string): Result<OrderVo> => {
  return http.get<OrderVo>(
    `/common/mall/order/getOrderByOrderNo`,
    {},
    {
      params: { orderNo }
    }
  )
}

/**
 * 统一支付接口
 * @param data 支付请求数据
 * @returns {Result<PaymentResponse>} 结果
 */
export const createPayment = (data: PaymentRequest): Result<PaymentResponse> => {
  return http.post<PaymentResponse>(`/common/mall/order/createPayment`, data)
}

/**
 * 查询订单列表（用户端）
 * @param query 查询参数
 * @returns {Result<PageResult<OrderVo>>} 结果
 */
export const getUserOrderList = (query?: OrderQuery): Result<PageResult<OrderVo>> => {
  return http.get<PageResult<OrderVo>>('/common/mall/order/pageOrders', query)
}

/**
 * 订单退款（仅超级管理员）
 * @param orderNo 订单号
 * @returns {Result<void>} 结果
 */
export const refundOrder = (orderNo: string): Result<void> => {
  return http.post<void>('/common/mall/order/refundOrder', { orderNo })
}
