package plus.ruoyi.common.mp.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxJsapiSignature;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.mp.domain.JsApiSignature;

/**
 * 微信公众号JS-SDK工具类
 * <p>
 * 提供微信JS-SDK签名生成功能，用于前端调用微信分享、扫一扫等接口
 * </p>
 *
 * <p>使用示例：</p>
 * <pre>
 * // 生成JS-SDK签名配置
 * JsApiSignature signature = WxMpJsApiUtils.createJsApiSignature("appid", "https://example.com/page");
 *
 * // 返回给前端
 * return R.ok(signature);
 * </pre>
 *
 * <p>前端使用：</p>
 * <pre>
 * // 1. 获取签名配置
 * const signature = await api.getJsApiSignature(window.location.href)
 *
 * // 2. 初始化wx.config
 * wx.config({
 *   debug: false,
 *   appId: signature.appId,
 *   timestamp: signature.timestamp,
 *   nonceStr: signature.nonceStr,
 *   signature: signature.signature,
 *   jsApiList: ['updateAppMessageShareData', 'updateTimelineShareData']
 * })
 *
 * // 3. 调用分享接口
 * wx.ready(() => {
 *   wx.updateAppMessageShareData({ ... })
 * })
 * </pre>
 *
 * @author bkywksj
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WxMpJsApiUtils {

    /**
     * 创建JS-SDK签名配置
     * <p>
     * 用于前端调用微信JS-SDK接口（如分享、扫一扫等）
     * </p>
     *
     * @param appid 公众号appid
     * @param url   当前网页的URL（不包含#及其后面部分）
     * @return JS-SDK签名配置
     */
    public static JsApiSignature createJsApiSignature(String appid, String url) {
        // 参数校验
        if (StringUtils.isBlank(appid)) {
            throw ServiceException.of("公众号appid不能为空");
        }
        if (StringUtils.isBlank(url)) {
            throw ServiceException.of("URL不能为空");
        }

        // 移除URL中的#及其后面部分（微信要求）
        String finalUrl = url;
        int hashIndex = url.indexOf('#');
        if (hashIndex > 0) {
            finalUrl = url.substring(0, hashIndex);
        }

        try {
            WxMpService wxMpService = SpringUtils.getBean(WxMpService.class);
            // 切换到指定的公众号配置
            wxMpService.switchover(appid);

            log.debug("生成JS-SDK签名 - appid:{}, url:{}", appid, finalUrl);

            // 创建签名（WeixinJava-MP 自动处理 jsapi_ticket 缓存）
            WxJsapiSignature wxSignature =
                wxMpService.createJsapiSignature(finalUrl);

            // 转换为自定义VO
            JsApiSignature signature = new JsApiSignature(
                appid,
                wxSignature.getTimestamp(),
                wxSignature.getNonceStr(),
                wxSignature.getSignature(),
                finalUrl
            );

            log.info("JS-SDK签名生成成功 - appid:{}, url:{}", appid, finalUrl);
            return signature;

        } catch (WxErrorException e) {
            // 常见错误码说明：
            // 40001: access_token无效或过期
            // 40013: appid无效
            // 40125: secret无效
            log.error("生成JS-SDK签名失败 - appid:{}, url:{}, errCode:{}, errMsg:{}",
                appid, finalUrl, e.getError().getErrorCode(), e.getError().getErrorMsg(), e);
            throw ServiceException.of("生成JS-SDK签名失败: " + e.getError().getErrorMsg());
        } catch (Exception e) {
            log.error("生成JS-SDK签名异常 - appid:{}, url:{}",
                appid, finalUrl, e);
            throw ServiceException.of("生成JS-SDK签名异常: " + e.getMessage());
        }
    }

    /**
     * 创建JS-SDK签名配置（使用默认公众号）
     * <p>
     * 适用于只有一个公众号的场景
     * </p>
     *
     * @param url 当前网页的URL（不包含#及其后面部分）
     * @return JS-SDK签名配置
     */
    public static JsApiSignature createJsApiSignature(String url) {
        try {
            WxMpService wxMpService = SpringUtils.getBean(WxMpService.class);
            // 获取当前配置的appid
            String appid = wxMpService.getWxMpConfigStorage().getAppId();
            return createJsApiSignature(appid, url);
        } catch (Exception e) {
            log.error("获取默认公众号配置失败", e);
            throw ServiceException.of("获取默认公众号配置失败");
        }
    }
}
