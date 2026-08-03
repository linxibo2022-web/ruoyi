package plus.ruoyi.business.mall.domain.bo;

import lombok.Data;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import plus.ruoyi.business.mall.domain.dto.OrderExtInfoDto;
import plus.ruoyi.business.mall.domain.dto.ReceiverInfoDto;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 创建订单业务对象 CreateOrderBo
 *
 * @author 抓蛙师
 * @date 2025-07-21
 */
@Data
public class CreateOrderBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 订单ID（系统生成）
     */
    private Long id;

    /**
     * 订单编号（系统生成）
     */
    private String orderNo;

    /**
     * 用户ID（从登录信息获取）
     */
    private Long userId;

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    private Long goodsId;

    /**
     * 商品名称
     */
    @NotBlank(message = "商品名称不能为空")
    private String goodsName;

    /**
     * 商品图片
     */
    private String goodsImg;

    /**
     * 商品价格
     */
    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.01", message = "商品价格不能小于0.01元")
    private BigDecimal price;

    /**
     * 购买数量
     */
    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量不能小于1")
    private Long quantity;

    /**
     * 订单总金额（系统计算）
     */
    private BigDecimal totalAmount;

    /**
     * 实付金额
     */
    private BigDecimal actualAmount;

    /**
     * 订单状态（系统设置）
     */
    private String orderStatus;

    /**
     * 买家备注
     */
    private String buyerRemark;

    /**
     * 订单扩展信息（选填）
     */
    private OrderExtInfoDto orderExtInfo;

    /**
     * 收货信息
     */
    // @NotNull(message = "收货信息不能为空")
    private ReceiverInfoDto receiverInfo;

    /**
     * 物流信息（选填，创建订单时通常为空）
     */
    private String shippingInfo;

    /**
     * 备注
     */
    private String remark;
}
