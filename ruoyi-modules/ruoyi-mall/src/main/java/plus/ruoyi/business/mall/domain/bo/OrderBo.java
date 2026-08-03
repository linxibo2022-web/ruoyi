package plus.ruoyi.business.mall.domain.bo;

import plus.ruoyi.business.mall.domain.Order;
import plus.ruoyi.common.mybatis.core.domain.BaseEntity;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Date;

import plus.ruoyi.common.core.validate.AddGroup;
import plus.ruoyi.common.core.validate.EditGroup;

/**
 * 订单业务对象 m_order
 *
 * @author 抓蛙师
 * @date 2025-07-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Order.class, reverseConvertGenerate = false)
public class OrderBo extends BaseEntity {

    /**
     * 订单ID
     */
    @NotNull(message = "订单ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 订单编号
     */
    @NotBlank(message = "订单编号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String orderNo;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long userId;

    /**
     * 商品ID(SPU)
     */
    @NotNull(message = "商品ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long goodsId;

    /**
     * SKU ID(规格)
     */
    private Long skuId;

    /**
     * 商品名称
     */
    @NotBlank(message = "商品名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String goodsName;

    /**
     * SKU名称(如:红色-S码)
     */
    private String skuName;

    /**
     * 规格值JSON
     */
    private String specValues;

    /**
     * 商品图片
     */
    private String goodsImg;

    /**
     * 商品单价
     */
    @NotNull(message = "商品价格不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal price;

    /**
     * 购买数量
     */
    @NotNull(message = "购买数量不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long quantity;

    /**
     * 订单总金额
     */
    @NotNull(message = "订单总金额不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal totalAmount;

    /**
     * 实付金额
     */
    private BigDecimal actualAmount;

    /**
     * 订单状态
     */
    private String orderStatus;

    /**
     * 支付方式
     */
    private String paymentMethod;

    /**
     * 支付时间
     */
    private Date paymentTime;

    /**
     * 交易流水号
     */
    private String transactionId;

    /**
     * 买家备注
     */
    private String buyerRemark;

    /**
     * 订单扩展信息
     */
    private String orderExtInfo;

    /**
     * 收货信息
     */
    private String receiverInfo;

    /**
     * 物流信息
     */
    private String shippingInfo;

    /**
     * 备注
     */
    private String remark;


}
