// 商品SKU API
import type { GoodsSkuQuery, GoodsSkuBo, GoodsSkuVo } from './goodsSkuTypes'

/**
 * 查询商品SKU列表
 * @param query 查询参数
 * @returns {Result<PageResult<GoodsSkuVo>>} 结果
 */
export const pageGoodsSkus = (query?: GoodsSkuQuery): Result<PageResult<GoodsSkuVo>> => {
  return http.get<PageResult<GoodsSkuVo>>('/mall/goodsSku/pageGoodsSkus', query)
}

/**
 * 根据商品ID查询SKU列表
 * @param goodsId 商品ID
 * @returns {Result<GoodsSkuVo[]>} 结果
 */
export const listByGoodsId = (goodsId: string | number): Result<GoodsSkuVo[]> => {
  return http.get<GoodsSkuVo[]>(`/mall/goodsSku/listByGoodsId/${goodsId}`)
}

/**
 * 查询商品SKU详细
 * @param id SKU ID
 * @returns {Result<GoodsSkuVo>} 结果
 */
export const getGoodsSku = (id: string | number): Result<GoodsSkuVo> => {
  return http.get<GoodsSkuVo>(`/mall/goodsSku/getGoodsSku/${id}`)
}

/**
 * 新增商品SKU
 * @param data SKU数据
 * @returns {Result<string | number>} 结果
 */
export const addGoodsSku = (data: GoodsSkuBo): Result<string | number> => {
  return http.post<string | number>('/mall/goodsSku/addGoodsSku', data)
}

/**
 * 修改商品SKU
 * @param data SKU数据
 * @returns {Result<void>} 结果
 */
export const updateGoodsSku = (data: GoodsSkuBo): Result<void> => {
  return http.put<void>('/mall/goodsSku/updateGoodsSku', data)
}

/**
 * 删除商品SKU
 * @param ids SKU ID
 * @returns {Result<void>} 结果
 */
export const deleteGoodsSkus = (ids: string | number | Array<string | number>): Result<void> => {
  return http.del<void>(`/mall/goodsSku/deleteGoodsSkus/${ids}`)
}

/**
 * 批量保存商品SKU(先删除商品的所有SKU，再批量新增)
 * @param goodsId 商品ID
 * @param data SKU数据列表
 * @returns {Result<void>} 结果
 */
export const batchSaveByGoodsId = (goodsId: string | number, data: GoodsSkuBo[]): Result<void> => {
  return http.post<void>('/mall/goodsSku/batchSaveByGoodsId', data, {
    params: { goodsId }
  })
}
