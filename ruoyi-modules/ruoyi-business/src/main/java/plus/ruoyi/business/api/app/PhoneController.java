package plus.ruoyi.business.api.app;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import plus.ruoyi.business.base.domain.bo.PhoneBindBo;
import plus.ruoyi.business.base.domain.vo.PhoneBindVo;
import plus.ruoyi.common.core.dict.DictPlatformType;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.core.domain.model.LoginUser;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.satoken.utils.LoginHelper;
import plus.ruoyi.system.core.domain.bo.SysUserBo;
import plus.ruoyi.system.core.service.ISysUserService;

/**
 * 手机号接口
 * <p>
 * 只有当小程序模块启用时才加载此控制器
 *
 * @author 抓蛙师
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/app/phone")
@Validated
@ConditionalOnProperty(prefix = "module", name = "miniapp-enabled", havingValue = "true", matchIfMissing = true)
public class PhoneController {

    /** 微信小程序服务 */
    private final WxMaService wxMaService;
    /** 用户服务 */
    private final ISysUserService sysUserService;

    /**
     * 获取并绑定手机号
     * 通过平台授权码获取用户手机号并自动绑定到当前登录用户
     *
     * @param bo 手机号绑定请求参数
     * @return 绑定结果和手机号信息
     */
    @PostMapping("/bindPhone")
    public R<PhoneBindVo> bindPhone(@Validated @RequestBody PhoneBindBo bo) {
        if (DictPlatformType.MP_WEIXIN.getValue().equals(bo.getPlatform())) {
            LoginUser loginUser = LoginHelper.getLoginUser();
            wxMaService.switchover(loginUser.getAppid());
            try {
                WxMaPhoneNumberInfo phoneInfo = wxMaService.getUserService().getPhoneNumber(bo.getCode());
                String phone = phoneInfo.getPhoneNumber();

                SysUserBo userBo = new SysUserBo(loginUser.getUserId());
                userBo.setPhone(phone);
                if (sysUserService.updateUserProfile(userBo)) {
                    return R.ok("绑定成功", PhoneBindVo.of(phone));
                }
                return R.fail("绑定失败");
            } catch (WxErrorException e) {
                log.error("获取手机号出错: {}", e.getMessage(), e);
                return R.fail("获取手机号出错");
            }
        }
        return R.fail(StringUtils.format("{}待实现", bo.getPlatform()));
    }

    /**
     * 仅获取手机号（不绑定）
     * 用于需要先获取手机号再进行其他操作的场景
     *
     * @param bo 获取请求参数
     * @return 手机号信息
     */
    @GetMapping("/getPhone")
    public R<PhoneBindVo> getPhone(@Validated @RequestBody PhoneBindBo bo) {
        if (DictPlatformType.MP_WEIXIN.getValue().equals(bo.getPlatform())) {
            LoginUser loginUser = LoginHelper.getLoginUser();
            wxMaService.switchover(loginUser.getAppid());
            try {
                WxMaPhoneNumberInfo phoneInfo = wxMaService.getUserService().getPhoneNumber(bo.getCode());
                return R.ok("绑定成功", PhoneBindVo.of(phoneInfo.getPhoneNumber()));
            } catch (WxErrorException e) {
                log.error("获取手机号出错: {}", e.getMessage(), e);
                return R.fail("获取手机号出错");
            }
        }
        return R.fail(StringUtils.format("{}待实现", bo.getPlatform()));
    }

    /**
     * 解绑手机号
     *
     * @return 解绑结果
     */
    @DeleteMapping("/unbindPhone")
    public R<Void> unbindPhone() {
        LoginUser loginUser = LoginHelper.getLoginUser();
        SysUserBo userBo = new SysUserBo(loginUser.getUserId());
        userBo.setPhone(StringUtils.EMPTY);
        return sysUserService.updateUserProfile(userBo) ? R.ok("解绑成功") : R.fail("解绑失败");
    }
}
