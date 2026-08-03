// 支付配置 API
import type { PaymentQuery, PaymentBo, PaymentVo } from '@/api/business/base/payment/paymentTypes'

/**
 * 查询支付配置列表
 * @param query 查询参数
 * @returns {Result<PageResult<PaymentVo>>} 结果
 */
export const pagePayments = (query?: PaymentQuery): Result<PageResult<PaymentVo>> => {
  return http.get<PageResult<PaymentVo>>('/base/payment/pagePayments', query)
}

/**
 * 查询支付配置详细
 * @param id 支付配置ID
 * @returns {Result<PaymentVo>} 结果
 */
export const getPayment = (id: string | number): Result<PaymentVo> => {
  return http.get<PaymentVo>(`/base/payment/getPayment/${id}`)
}

/**
 * 新增支付配置
 * @param data 支付配置数据
 * @returns {Result<string | number>} 结果
 */
export const addPayment = (data: PaymentBo): Result<string | number> => {
  return http.post<string | number>('/base/payment/addPayment', data)
}

/**
 * 修改支付配置
 * @param data 支付配置数据
 * @returns {Result<void>} 结果
 */
export const updatePayment = (data: PaymentBo): Result<void> => {
  return http.put<void>('/base/payment/updatePayment', data)
}

/**
 * 删除支付配置
 * @param ids 支付配置ID
 * @returns {Result<void>} 结果
 */
export const deletePayments = (ids: string | number | Array<string | number>): Result<void> => {
  return http.del<void>(`/base/payment/deletePayments/${ids}`)
}

/**
 * 获取支付配置选项列表
 * 用于下拉选择、关联查询等场景
 * @returns {Result<DictItem[]>} 结果
 */
export const optionPayments = (): Result<DictItem[]> => {
  return http.get<DictItem[]>('/base/payment/optionPayments')
}
