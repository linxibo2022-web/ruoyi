package plus.ruoyi.business.mall.domain;

import plus.ruoyi.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.util.Date;

import java.io.Serial;

/**
 * 订单对象 m_order
 *
 * @author 抓蛙师
 * @date 2025-07-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("m_order")
public class Order extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 商品ID(SPU)
     */
    private Long goodsId;

    /**
     * SKU ID(规格)
     */
    private Long skuId;

    /**
     * 商品名称
     */
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
    private BigDecimal price;

    /**
     * 购买数量
     */
    private Long quantity;

    /**
     * 订单总金额
     */
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

    /**
     * 是否删除
     */
    @TableLogic
    private String isDeleted;


}
