package plus.ruoyi.common.social.config.properties;

import lombok.Data;
import java.util.List;

/**
 * 社交登录配置属性类
 *
 * 定义各种第三方登录平台所需的配置参数
 *
 * @author thiszhc
 */
@Data
public class SocialLoginConfigProperties {

    /**
     * 应用ID（客户端ID）
     */
    private String clientId;

    /**
     * 应用密钥（客户端密钥）
     */
    private String clientSecret;

    /**
     * 授权回调地址
     */
    private String redirectUri;

    /**
     * 是否获取微信UnionID
     */
    private boolean unionId;

    /**
     * Coding平台企业名称
     */
    private String codingGroupName;

    /**
     * 支付宝公钥
     */
    private String alipayPublicKey;

    /**
     * 企业微信应用ID
     */
    private String agentId;

    /**
     * StackOverflow平台API密钥
     */
    private String stackOverflowKey;

    /**
     * 设备唯一标识
     */
    private String deviceId;

    /**
     * 客户端操作系统类型
     */
    private String clientOsType;

    /**
     * MaxKey服务器地址
     */
    private String serverUrl;

    /**
     * OAuth授权范围列表
     */
    private List<String> scopes;
}
