package plus.ruoyi.business.mall.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 退款业务对象
 * @author 抓蛙师
 * @date 2025/7/21
 */
@Data
public class RefundBo {

    /**
     * 订单号
     */
    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    /**
     * 退款金额，不传则全额退款
     */
    private BigDecimal refundAmount;

    /**
     * 退款原因
     */
    private String reason = "用户申请退款";
}
