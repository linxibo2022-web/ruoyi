package plus.ruoyi.common.pay.exception;

import java.io.Serial;

/**
 * 配置未找到异常
 *
 * @author 抓蛙师
 */
public class ConfigNotFoundException extends PayException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ConfigNotFoundException(String message) {
        super(message);
    }

    public static ConfigNotFoundException of(String tenantId, String appid) {
        return new ConfigNotFoundException(
            String.format("未找到支付配置: tenantId=%s, appid=%s", tenantId, appid)
        );
    }
}
