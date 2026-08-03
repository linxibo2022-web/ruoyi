package plus.ruoyi.business.mall.domain.vo;

import java.math.BigDecimal;
import java.util.Date;
import io.github.linpeilie.annotations.AutoMappers;
import plus.ruoyi.business.mall.domain.GoodsSku;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import plus.ruoyi.business.mall.domain.bo.GoodsSkuBo;
import plus.ruoyi.common.excel.annotation.ExcelDictFormat;
import plus.ruoyi.common.excel.convert.ExcelDictConvert;
import plus.ruoyi.common.serialmap.annotation.SerialMap;
import plus.ruoyi.common.serialmap.constant.SerialMapConstant;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;

/**
 * 商品SKU视图对象 m_goods_sku
 *
 * @author 抓蛙师
 * @date 2025-11-01
 */
@Data
@ExcelIgnoreUnannotated
@AutoMappers({
    @AutoMapper(target = GoodsSku.class),
    @AutoMapper(target = GoodsSkuBo.class)
})
public class GoodsSkuVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * SKU ID
     */
    @ExcelProperty(value = "SKU ID")
    private Long id;

    /**
     * 商品ID(关联m_goods.id)
     */
    @ExcelProperty(value = "商品ID")
    private Long goodsId;

    /**
     * SKU编码(自动生成或手动)
     */
    @ExcelProperty(value = "SKU编码")
    private String skuCode;

    /**
     * SKU名称(如:红色-S码)
     */
    @ExcelProperty(value = "SKU名称")
    private String skuName;

    /**
     * 规格值JSON(如:{"颜色":"红色","尺码":"S"})
     */
    @ExcelProperty(value = "规格值")
    private String specValues;

    /**
     * 原价
     */
    @ExcelProperty(value = "原价")
    private BigDecimal originalPrice;

    /**
     * 价格
     */
    @ExcelProperty(value = "价格")
    private BigDecimal price;

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
     * SKU图片
     */
    @ExcelProperty(value = "SKU图片")
    @SerialMap(converter = SerialMapConstant.PRESIGNED_URL)
    private String img;

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
     * 是否默认SKU (0否 1是)
     */
    @ExcelProperty(value = "是否默认", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_yes_no")
    private String isDefault;

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
}
