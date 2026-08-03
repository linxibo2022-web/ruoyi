package plus.ruoyi.common.miniapp.utils;

import cn.binarywang.wx.miniapp.api.WxMaQrcodeService;
import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaCodeLineColor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.core.utils.StringUtils;

/**
 * 微信小程序二维码工具类
 * <p>
 * 提供小程序码生成功能，支持：
 * - 不限次数小程序码（createWxaCodeUnlimit）
 * - 普通小程序码（createQrcode）
 * - 临时二维码（createWxaCode）
 * </p>
 *
 * @author bkywksj
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WxMaQrcodeUtils {

    /**
     * 生成小程序码（不限次数）- 推荐使用
     * <p>
     * 适用于需要的码数量极多的业务场景
     * 最多生成10万个（永久有效）
     * </p>
     *
     * @param appid 小程序appid
     * @param scene 场景值（最大32个可见字符，只支持数字、字母、特殊符号 _-）
     * @param page  页面路径（如：pages/index/index），不填默认跳转首页
     * @param width 二维码宽度（默认430，最小280，最大1280）
     * @return 二维码字节数组
     */
    public static byte[] createUnlimitQrcode(String appid, String scene, String page, Integer width) {
        if (StringUtils.isBlank(appid)) {
            throw ServiceException.of("小程序appid不能为空");
        }
        if (StringUtils.isBlank(scene)) {
            throw ServiceException.of("场景值scene不能为空");
        }
        if (scene.length() > 32) {
            throw ServiceException.of("场景值scene长度不能超过32个字符");
        }

        try {
            WxMaService wxMaService = SpringUtils.getBean(WxMaService.class);
            // 切换到指定的小程序配置
            wxMaService.switchover(appid);

            WxMaQrcodeService qrcodeService = wxMaService.getQrcodeService();

            // 根据环境自动切换版本
            String env = SpringUtils.getActiveProfile();
            String envVersion = "dev".equals(env) ? "develop" : "release";
            int qrcodeWidth = width != null ? width : 430;

            log.debug("生成小程序码 - appid:{}, scene:{}, page:{}, width:{}, env:{}",
                appid, scene, page, qrcodeWidth, envVersion);

            return qrcodeService.createWxaCodeUnlimitBytes(
                scene,
                page,
                false,       // checkPath: 是否检查page是否存在（建议关闭，提高效率）
                envVersion,  // envVersion: trial(体验版)/develop(开发版)/release(正式版)
                qrcodeWidth,
                true,        // autoColor: 自动配置线条颜色
                new WxMaCodeLineColor("0", "0", "0"), // lineColor: 黑色
                false        // isHyaline: 不透明
            );
        } catch (WxErrorException e) {
            log.error("生成小程序码失败 - appid:{}, scene:{}, error:{}", appid, scene, e.getMessage(), e);
            throw ServiceException.of("生成小程序码失败: " + e.getError().getErrorMsg());
        }
    }

    /**
     * 生成小程序码（不限次数）- 默认宽度430
     *
     * @param appid 小程序appid
     * @param scene 场景值
     * @param page  页面路径
     * @return 二维码字节数组
     */
    public static byte[] createUnlimitQrcode(String appid, String scene, String page) {
        return createUnlimitQrcode(appid, scene, page, null);
    }

    /**
     * 生成小程序码（不限次数）- 跳转首页
     *
     * @param appid 小程序appid
     * @param scene 场景值
     * @return 二维码字节数组
     */
    public static byte[] createUnlimitQrcode(String appid, String scene) {
        return createUnlimitQrcode(appid, scene, null, null);
    }

    /**
     * 生成普通小程序码（有限次数）
     * <p>
     * 适用于需要的码数量较少的业务场景
     * 最多生成10万个（永久有效）
     * </p>
     *
     * @param appid 小程序appid
     * @param path  页面路径（必填，如：pages/index/index?id=123）
     * @param width 二维码宽度（默认430，最小280，最大1280）
     * @return 二维码字节数组
     */
    public static byte[] createQrcode(String appid, String path, Integer width) {
        if (StringUtils.isBlank(appid)) {
            throw ServiceException.of("小程序appid不能为空");
        }
        if (StringUtils.isBlank(path)) {
            throw ServiceException.of("页面路径path不能为空");
        }

        try {
            WxMaService wxMaService = SpringUtils.getBean(WxMaService.class);
            wxMaService.switchover(appid);

            WxMaQrcodeService qrcodeService = wxMaService.getQrcodeService();

            String env = SpringUtils.getActiveProfile();
            String envVersion = "dev".equals(env) ? "develop" : "release";
            int qrcodeWidth = width != null ? width : 430;

            log.debug("生成小程序码(有限次数) - appid:{}, path:{}, width:{}, env:{}",
                appid, path, qrcodeWidth, envVersion);

            return qrcodeService.createWxaCodeBytes(
                path,
                envVersion,  // envVersion: trial(体验版)/develop(开发版)/release(正式版)
                qrcodeWidth,
                true,        // autoColor: 自动配置线条颜色
                new WxMaCodeLineColor("0", "0", "0"),
                false        // isHyaline: 不透明
            );
        } catch (WxErrorException e) {
            log.error("生成小程序码失败 - appid:{}, path:{}, error:{}", appid, path, e.getMessage(), e);
            throw ServiceException.of("生成小程序码失败: " + e.getError().getErrorMsg());
        }
    }

    /**
     * 生成普通小程序码（有限次数）- 默认宽度430
     *
     * @param appid 小程序appid
     * @param path  页面路径
     * @return 二维码字节数组
     */
    public static byte[] createQrcode(String appid, String path) {
        return createQrcode(appid, path, null);
    }

    /**
     * 生成小程序二维码（有限次数）
     * <p>
     * 生成正方形的普通二维码，而非圆形的小程序码
     * 最多生成10万个（永久有效）
     * </p>
     *
     * @param appid 小程序appid
     * @param path  页面路径（必填，如：pages/index/index?id=123）
     * @param width 二维码宽度（默认430，最小280，最大1280）
     * @return 二维码字节数组
     */
    public static byte[] createMiniQrcode(String appid, String path, Integer width) {
        if (StringUtils.isBlank(appid)) {
            throw ServiceException.of("小程序appid不能为空");
        }
        if (StringUtils.isBlank(path)) {
            throw ServiceException.of("页面路径path不能为空");
        }

        try {
            WxMaService wxMaService = SpringUtils.getBean(WxMaService.class);
            wxMaService.switchover(appid);

            WxMaQrcodeService qrcodeService = wxMaService.getQrcodeService();
            int qrcodeWidth = width != null ? width : 430;

            log.debug("生成小程序二维码(正方形) - appid:{}, path:{}, width:{}", appid, path, qrcodeWidth);

            return qrcodeService.createQrcodeBytes(path, qrcodeWidth);
        } catch (WxErrorException e) {
            log.error("生成小程序二维码失败 - appid:{}, path:{}, error:{}", appid, path, e.getMessage(), e);
            throw ServiceException.of("生成小程序二维码失败: " + e.getError().getErrorMsg());
        }
    }

    /**
     * 生成小程序二维码（有限次数）- 默认宽度430
     *
     * @param appid 小程序appid
     * @param path  页面路径
     * @return 二维码字节数组
     */
    public static byte[] createMiniQrcode(String appid, String path) {
        return createMiniQrcode(appid, path, null);
    }
}
