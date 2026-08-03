/** 商品SKU查询类型 */
export interface GoodsSkuQuery extends PageQuery {
  /** SKU ID */
  id?: string | number

  /** 商品ID(关联m_goods.id) */
  goodsId?: string | number

  /** SKU编码 */
  skuCode?: string

  /** SKU名称 */
  skuName?: string

  /** 状态 (0停用 1启用) */
  status?: string

  /** 创建时间 */
  createTime?: string
}

/** 商品SKU表单类型 */
export interface GoodsSkuBo {
  /** SKU ID */
  id?: string | number

  /** 商品ID(关联m_goods.id) */
  goodsId?: string | number

  /** SKU编码(自动生成或手动) */
  skuCode?: string

  /** SKU名称(如:红色-S码) */
  skuName?: string

  /** 规格值JSON(如:{"颜色":"红色","尺码":"S"}) */
  specValues?: string

  /** 原价 */
  originalPrice?: number

  /** 价格 */
  price?: number

  /** 库存 */
  stock?: number

  /** 销量 */
  salesCount?: number

  /** SKU图片(可继承商品主图) */
  img?: string

  /** 状态 (0停用 1启用) */
  status?: string

  /** 排序 */
  sortOrder?: number

  /** 是否默认SKU (0否 1是) */
  isDefault?: string

  /** 备注 */
  remark?: string
}

/** 商品SKU视图类型 */
export interface GoodsSkuVo {
  /** SKU ID */
  id: string | number

  /** 商品ID(关联m_goods.id) */
  goodsId: string | number

  /** SKU编码(自动生成或手动) */
  skuCode: string

  /** SKU名称(如:红色-S码) */
  skuName: string

  /** 规格值JSON(如:{"颜色":"红色","尺码":"S"}) */
  specValues: string

  /** 原价 */
  originalPrice: number

  /** 价格 */
  price: number

  /** 库存 */
  stock: number

  /** 销量 */
  salesCount: number

  /** SKU图片 */
  img: string

  /** 状态 (0停用 1启用) */
  status: string

  /** 排序 */
  sortOrder: number

  /** 是否默认SKU (0否 1是) */
  isDefault: string

  /** 创建时间 */
  createTime: string

  /** 更新时间 */
  updateTime: string

  /** 备注 */
  remark: string
}

/** 规格项类型 */
export interface SpecItem {
  /** 规格名称 (如: 颜色、尺码) */
  name: string

  /** 规格值列表 (如: [红色, 蓝色, 黑色]) */
  values: string[]
}

/** SKU编辑行类型（用于表格编辑） */
export interface SkuEditRow extends GoodsSkuBo {
  /** 规格值对象 */
  specValuesObj?: Record<string, string>

  /** 是否新增 */
  isNew?: boolean
}
