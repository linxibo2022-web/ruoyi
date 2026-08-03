// 商品管理类型
import type { GoodsSkuBo, GoodsSkuVo } from '../goodsSku/goodsSkuTypes'

/** 商品查询类型 */
export interface GoodsQuery extends PageQuery {
  /** 商品ID */
  id?: string | number

  /** 商品分类 */
  category?: string

  /** 商品编码 */
  code?: string

  /** 商品名称 */
  name?: string

  /** 商品主图 */
  img?: string

  /** 商品图片集(多图,逗号分隔) */
  imgs?: string

  /** 规格类型 (0单规格 1多规格) */
  specType?: string

  /** 原价(单规格时使用) */
  originalPrice?: string

  /** 折扣 */
  discount?: string

  /** 价格 */
  price?: string

  /** 商品描述 */
  description?: string

  /** 库存 */
  stock?: number

  /** 销量 */
  salesCount?: number

  /** 状态 */
  status?: string

  /** 排序 */
  sortOrder?: number

  /** 创建时间 */
  createTime?: string

}

/** 商品表单类型 */
export interface GoodsBo {
  /** 商品ID */
  id?: string | number

  /** 商品分类 */
  category?: string

  /** 商品编码 */
  code?: string

  /** 商品名称 */
  name?: string

  /** 商品主图 */
  img?: string

  /** 商品图片集(多图,逗号分隔) */
  imgs?: string

  /** 规格类型 (0单规格 1多规格) */
  specType?: string

  /** 原价(单规格时使用) */
  originalPrice?: string

  /** 折扣 */
  discount?: string

  /** 价格 */
  price?: string

  /** 商品描述 */
  description?: string

  /** 库存 */
  stock?: number

  /** 销量 */
  salesCount?: number

  /** 状态 */
  status?: string

  /** 排序 */
  sortOrder?: number

  /** 备注 */
  remark?: string

  /** SKU列表（多规格商品时使用） */
  skuList?: GoodsSkuBo[]
}

/** 商品视图类型 */
export interface GoodsVo {
  /** 商品ID */
  id: string | number

  /** 商品分类 */
  category: string

  /** 商品编码 */
  code: string

  /** 商品名称 */
  name: string

  /** 商品主图 */
  img: string

  /** 商品图片集(多图,逗号分隔) */
  imgs: string

  /** 规格类型 (0单规格 1多规格) */
  specType: string

  /** 原价(单规格时使用) */
  originalPrice: string

  /** 折扣 */
  discount: string

  /** 价格 */
  price: string

  /** 商品描述 */
  description: string

  /** 库存 */
  stock: number

  /** 销量 */
  salesCount: number

  /** 状态 */
  status: string

  /** 排序 */
  sortOrder: number

  /** 创建时间 */
  createTime: string

  /** 更新时间 */
  updateTime: string

  /** 备注 */
  remark: string

  /** SKU列表（多规格商品时使用） */
  skuList?: GoodsSkuVo[]
}
