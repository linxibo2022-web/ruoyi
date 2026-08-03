package plus.ruoyi.business.mall.domain.vo;

import java.math.BigDecimal;
import java.util.Date;

import io.github.linpeilie.annotations.AutoMappers;
import plus.ruoyi.business.mall.domain.Order;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import plus.ruoyi.business.mall.domain.bo.OrderBo;
import plus.ruoyi.common.excel.annotation.ExcelDictFormat;
import plus.ruoyi.common.excel.convert.ExcelDictConvert;
import plus.ruoyi.common.serialmap.annotation.SerialMap;
import plus.ruoyi.common.serialmap.constant.SerialMapConstant;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


/**
 * 订单视图对象 m_order
 *
 * @author 抓蛙师
 * @date 2025-07-17
 */
@Data
@ExcelIgnoreUnannotated
@AutoMappers({
    @AutoMapper(target = Order.class),
    @AutoMapper(target = OrderBo.class)
})
public class OrderVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @ExcelProperty(value = "订单ID")
    private Long id;

    /**
     * 订单编号
     */
    @ExcelProperty(value = "订单编号")
    private String orderNo;

    /**
     * 用户ID
     */
    @ExcelProperty(value = "用户ID")
    private Long userId;

    /**
     * 商品ID(SPU)
     */
    @ExcelProperty(value = "商品ID")
    private Long goodsId;

    /**
     * SKU ID(规格)
     */
    @ExcelProperty(value = "SKU ID")
    private Long skuId;

    /**
     * 商品名称
     */
    @ExcelProperty(value = "商品名称")
    private String goodsName;

    /**
     * SKU名称
     */
    @ExcelProperty(value = "SKU名称")
    private String skuName;

    /**
     * 规格值
     */
    @ExcelProperty(value = "规格值")
    private String specValues;

    /**
     * 商品图片
     */
    @ExcelProperty(value = "商品图片")
    @SerialMap(converter = SerialMapConstant.PRESIGNED_URL)
    private String goodsImg;

    /**
     * 商品单价
     */
    @ExcelProperty(value = "商品单价")
    private BigDecimal price;

    /**
     * 购买数量
     */
    @ExcelProperty(value = "购买数量")
    private Long quantity;

    /**
     * 订单总金额
     */
    @ExcelProperty(value = "订单总金额")
    private BigDecimal totalAmount;

    /**
     * 实付金额
     */
    @ExcelProperty(value = "实付金额")
    private BigDecimal actualAmount;

    /**
     * 订单状态
     */
    @ExcelProperty(value = "订单状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_order_status")
    private String orderStatus;

    /**
     * 支付方式
     */
    @ExcelProperty(value = "支付方式")
    private String paymentMethod;

    /**
     * 支付时间
     */
    @ExcelProperty(value = "支付时间")
    private Date paymentTime;

    /**
     * 交易流水号
     */
    @ExcelProperty(value = "交易流水号")
    private String transactionId;

    /**
     * 买家备注
     */
    @ExcelProperty(value = "买家备注")
    private String buyerRemark;

    /**
     * 订单扩展信息
     */
    @ExcelProperty(value = "订单扩展信息")
    private String orderExtInfo;

    /**
     * 收货信息
     */
    @ExcelProperty(value = "收货信息")
    private String receiverInfo;

    /**
     * 物流信息
     */
    @ExcelProperty(value = "物流信息")
    private String shippingInfo;

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
