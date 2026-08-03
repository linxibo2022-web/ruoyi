package plus.ruoyi.system.openapi.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 开放API密钥详情视图对象(仅生成时返回,包含明文Secret)
 *
 * @author 抓蛙师
 * @date 2025-10-03
 */
@Data
public class OpenApiSecretVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * API密钥ID
     */
    private Long id;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * AppKey(公开)
     */
    private String appKey;

    /**
     * AppSecret(明文,仅生成时返回一次)
     */
    private String appSecret;

    /**
     * 提示信息
     */
    private String tips = "请妥善保管AppSecret,系统不会再次显示明文密钥";

}
