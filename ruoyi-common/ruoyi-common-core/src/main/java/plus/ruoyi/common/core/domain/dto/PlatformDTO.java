package plus.ruoyi.common.core.domain.dto;

import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 平台配置DTO
 *
 * @author 抓蛙师
 * @date 2025/6/10
 */
@Data
public class PlatformDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 平台配置id
     */
    private Long id;

    /**
     * 租户id
     */
    private String tenantId;

    /**
     * 平台类型
     */
    private String type;

    /**
     * 名称
     */
    private String name;

    /**
     * appid
     */
    private String appid;

    /**
     * 密钥
     */
    private String secret;

    /**
     * 接口token
     */
    private String token;

    /**
     * 加密密钥
     */
    private String aeskey;

    /**
     * 支持的支付配置ID列表，逗号分隔
     */
    private String paymentIds;

    /**
     * 状态
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 获取支付配置ID列表
     *
     * @return 支付配置ID列表
     */
    public List<Long> getPaymentIdList() {
        if (paymentIds == null || paymentIds.trim().isEmpty()) {
            return Collections.emptyList();
        }

        return Arrays.stream(paymentIds.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(Long::parseLong)
            .collect(Collectors.toList());
    }

    /**
     * 设置支付配置ID列表
     *
     * @param paymentIdList 支付配置ID列表
     */
    public void setPaymentIdList(List<Long> paymentIdList) {
        if (paymentIdList == null || paymentIdList.isEmpty()) {
            this.paymentIds = "";
        } else {
            this.paymentIds = paymentIdList.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        }
    }

    /**
     * 检查是否支持指定的支付配置
     *
     * @param paymentId 支付配置ID
     * @return 是否支持
     */
    public boolean supportsPayment(Long paymentId) {
        return getPaymentIdList().contains(paymentId);
    }
}
