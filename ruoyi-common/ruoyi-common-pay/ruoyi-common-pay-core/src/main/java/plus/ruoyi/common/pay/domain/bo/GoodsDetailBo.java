package plus.ruoyi.common.pay.domain.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品详情信息
 * 用于微信/支付宝支付时传递商品明细
 *
 * @author 抓蛙师
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsDetailBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    private Long goodsId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品数量
     */
    private Long quantity;

    /**
     * 商品单价(单位:元)
     */
    private BigDecimal unitPrice;

    /**
     * 商品图片URL
     */
    private String goodsImg;

    /**
     * 商品详情页路径(小程序路径)
     * 例如: pages/goods/detail?id=123
     */
    private String detailPath;
}
