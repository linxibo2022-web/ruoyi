package plus.ruoyi.business.mall.domain.vo;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import io.github.linpeilie.annotations.AutoMappers;
import plus.ruoyi.business.mall.domain.Goods;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import plus.ruoyi.business.mall.domain.bo.GoodsBo;
import plus.ruoyi.common.excel.annotation.ExcelDictFormat;
import plus.ruoyi.common.excel.convert.ExcelDictConvert;
import plus.ruoyi.common.serialmap.annotation.SerialMap;
import plus.ruoyi.common.serialmap.constant.SerialMapConstant;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


/**
 * 商品视图对象 m_goods
 *
 * @author 抓蛙师
 * @date 2025-07-17
 */
@Data
@ExcelIgnoreUnannotated
@AutoMappers({
    @AutoMapper(target = Goods.class),
    @AutoMapper(target = GoodsBo.class)
})
public class GoodsVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    @ExcelProperty(value = "商品ID")
    private Long id;

    /**
     * 商品分类
     */
    @ExcelProperty(value = "商品分类")
    private String category;

    /**
     * 商品编码
     */
    @ExcelProperty(value = "商品编码")
    private String code;

    /**
     * 商品名称
     */
    @ExcelProperty(value = "商品名称")
    private String name;

    /**
     * 商品主图
     */
    @ExcelProperty(value = "商品主图")
    @SerialMap(converter = SerialMapConstant.PRESIGNED_URL)
    private String img;

    /**
     * 商品图片集(多图,逗号分隔)
     */
    @ExcelProperty(value = "商品图片集")
    @SerialMap(converter = SerialMapConstant.PRESIGNED_URL)
    private String imgs;

    /**
     * 规格类型 (0单规格 1多规格)
     */
    @ExcelProperty(value = "规格类型", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "m_goods_spec_type")
    private String specType;

    /**
     * 原价(单规格时使用)
     */
    @ExcelProperty(value = "原价")
    private BigDecimal originalPrice;

    /**
     * 折扣
     */
    @ExcelProperty(value = "折扣")
    private BigDecimal discount;

    /**
     * 价格
     */
    @ExcelProperty(value = "价格")
    private BigDecimal price;

    /**
     * 商品描述
     */
    @ExcelProperty(value = "商品描述")
    private String description;

    /**
     * 库存
     */
    @ExcelProperty(value = "库存")
    private Long stock;

    /**
     * 销量
     */
    @ExcelProperty(value = "销量")
    private Long salesCount;

    /**
     * 状态
     */
    @ExcelProperty(value = "状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_enable_status")
    private String status;

    /**
     * 排序
     */
    @ExcelProperty(value = "排序")
    private Long sortOrder;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 更新时间
     */
    @ExcelProperty(value = "更新时间")
    private Date updateTime;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String remark;

    /**
     * SKU列表（多规格商品时使用）
     */
    private List<GoodsSkuVo> skuList;

}
