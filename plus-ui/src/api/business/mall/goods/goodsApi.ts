// 商品管理 API
import type { GoodsQuery, GoodsBo, GoodsVo } from './goodsTypes'

/**
 * 查询商品列表
 * @param query 查询参数
 * @returns {Result<PageResult<GoodsVo>>} 结果
 */
export const pageGoods = (query?: GoodsQuery): Result<PageResult<GoodsVo>> => {
  return http.get<PageResult<GoodsVo>>('/mall/goods/pageGoods', query)
}

/**
 * 查询商品详细
 * @param id 商品ID
 * @returns {Result<GoodsVo>} 结果
 */
export const getGoods = (id: string | number): Result<GoodsVo> => {
  return http.get<GoodsVo>(`/mall/goods/getGoods/${id}`)
}

/**
 * 新增商品
 * @param data 商品数据
 * @returns {Result<string | number>} 结果
 */
export const addGoods = (data: GoodsBo): Result<string | number> => {
  return http.post<string | number>('/mall/goods/addGoods', data)
}

/**
 * 修改商品
 * @param data 商品数据
 * @returns {Result<void>} 结果
 */
export const updateGoods = (data: GoodsBo): Result<void> => {
  return http.put<void>('/mall/goods/updateGoods', data)
}

/**
 * 删除商品
 * @param ids 商品ID
 * @returns {Result<void>} 结果
 */
export const deleteGoods = (ids: string | number | Array<string | number>): Result<void> => {
  return http.del<void>(`/mall/goods/deleteGoods/${ids}`)
}

/**
 * 同步商品主表数据(根据SKU重新计算价格、库存、销量)
 * @param goodsId 商品ID
 * @returns {Result<void>} 结果
 */
export const syncGoodsDataFromSku = (goodsId: string | number): Result<void> => {
  return http.post<void>(`/mall/goods/syncGoodsDataFromSku/${goodsId}`)
}
