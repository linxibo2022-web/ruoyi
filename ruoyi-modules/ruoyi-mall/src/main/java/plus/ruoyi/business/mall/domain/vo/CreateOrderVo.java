package plus.ruoyi.business.mall.domain.vo;

import lombok.Data;
import plus.ruoyi.common.serialmap.annotation.SerialMap;
import plus.ruoyi.common.serialmap.constant.SerialMapConstant;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 创建订单响应对象 CreateOrderVo
 *
 * @author 抓蛙师
 * @date 2025-07-21
 */
@Data
public class CreateOrderVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    private Long id;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 商品ID
     */
    private Long goodsId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品图片
     */
    @SerialMap(converter = SerialMapConstant.PRESIGNED_URL)
    private String goodsImg;

    /**
     * 商品价格
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
     * 订单状态
     */
    private String orderStatus;

    /**
     * 订单状态名称
     */
    private String orderStatusName;

    /**
     * 买家备注
     */
    private String buyerRemark;

    /**
     * 创建时间
     */
    private Date createTime;
}
