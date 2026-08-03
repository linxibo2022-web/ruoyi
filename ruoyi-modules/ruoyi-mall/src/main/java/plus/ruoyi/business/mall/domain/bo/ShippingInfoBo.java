package plus.ruoyi.business.mall.domain.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 物流信息业务对象
 * 用于规范化订单物流信息的JSON结构
 *
 * @author 抓蛙师
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingInfoBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 快递公司
     * 例如: 顺丰速运、圆通快递、中通快递
     */
    private String expressCompany;

    /**
     * 快递单号
     */
    private String trackingNumber;

    /**
     * 发货时间
     */
    private Date shipTime;

    /**
     * 收货人姓名
     */
    private String receiverName;

    /**
     * 收货人电话
     */
    private String receiverPhone;

    /**
     * 收货地址
     */
    private String receiverAddress;

    /**
     * 物流状态
     * 0-未发货, 1-已发货, 2-运输中, 3-派送中, 4-已签收, 5-拒收
     */
    private String status;

    /**
     * 备注
     */
    private String remark;
}
