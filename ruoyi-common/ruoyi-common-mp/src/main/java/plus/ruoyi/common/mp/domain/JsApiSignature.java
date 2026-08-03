package plus.ruoyi.common.mp.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 微信JS-SDK签名配置
 * <p>
 * 用于前端调用微信JS-SDK接口（如分享、扫一扫等）的签名数据
 * </p>
 *
 * @author bkywksj
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JsApiSignature implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 公众号appId
     */
    private String appId;

    /**
     * 时间戳（秒）
     */
    private Long timestamp;

    /**
     * 随机字符串
     */
    private String nonceStr;

    /**
     * 签名
     */
    private String signature;

    /**
     * 当前网页的URL（不包含#及其后面部分，用于调试）
     */
    private String url;
}
