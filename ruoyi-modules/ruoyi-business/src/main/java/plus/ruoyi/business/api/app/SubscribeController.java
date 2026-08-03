package plus.ruoyi.business.api.app;

import cn.dev33.satoken.annotation.SaIgnore;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import plus.ruoyi.business.base.domain.vo.TemplateConfig;
import plus.ruoyi.business.base.service.IPlatformService;
import plus.ruoyi.common.core.constant.Constants;
import plus.ruoyi.common.core.domain.R;

import java.util.List;

/**
 * 订阅消息 API
 *
 * @author 抓蛙师
 * @date 2025-10-09
 */
@SaIgnore
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/app/subscribe")
public class SubscribeController {

    private final IPlatformService platformService;

    /**
     * 根据appid获取订阅消息模板配置
     *
     * @param appid 小程序appid
     * @return 订阅消息模板配置列表（只返回启用的）
     */
    @GetMapping("/getTemplateConfigs")
    public R<List<TemplateConfig>> getTemplateConfigs(
        @NotBlank(message = "appid不能为空") @RequestParam String appid
    ) {
        List<TemplateConfig> configs = platformService.getTemplateConfigs(appid);
        return R.ok(configs);
    }
}
