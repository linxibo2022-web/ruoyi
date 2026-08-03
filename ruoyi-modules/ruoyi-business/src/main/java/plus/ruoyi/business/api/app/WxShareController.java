package plus.ruoyi.business.api.app;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.mp.domain.JsApiSignature;
import plus.ruoyi.common.mp.utils.WxMpJsApiUtils;

/**
 * 微信公众号JS-SDK接口
 * <p>
 * 用于前端调用微信JS-SDK接口（如分享、扫一扫等）
 * <p>
 * 只有当公众号模块启用时才加载此控制器
 *
 * @author 抓蛙师
 */
@RestController
@RequestMapping("/app/wxShare")
@ConditionalOnProperty(prefix = "module", name = "mp-enabled", havingValue = "true", matchIfMissing = true)
public class WxShareController {

    /**
     * 获取微信JS-SDK权限签名
     * <p>
     * 用于前端初始化微信JS-SDK，调用微信分享、扫一扫等功能
     *
     * @param url 当前网页URL（不包含#及其后面部分）
     * @return JS-SDK签名配置
     */
    @GetMapping("/getJsApiSignature")
    public R<JsApiSignature> getJsApiSignature(@RequestParam String url) {
        // 可以从平台配置获取appid，或使用默认公众号
        JsApiSignature signature = WxMpJsApiUtils.createJsApiSignature(url);
        return R.ok(signature);
    }
}
