package plus.ruoyi.business.api.app;

import cn.dev33.satoken.annotation.SaIgnore;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import plus.ruoyi.business.base.domain.bo.AdBo;
import plus.ruoyi.business.base.domain.vo.AdVo;
import plus.ruoyi.business.base.service.IAdService;
import plus.ruoyi.business.mall.domain.bo.GoodsBo;
import plus.ruoyi.business.mall.domain.vo.GoodsVo;
import plus.ruoyi.business.mall.service.IGoodsService;
import plus.ruoyi.common.core.constant.Constants;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.core.domain.dto.PlatformDTO;
import plus.ruoyi.common.core.service.PlatformService;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;

import java.util.List;

/**
 * 首页接口
 *
 * @author 抓蛙师
 */
@SaIgnore
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/app/home")
public class HomeController {

    /** 平台配置服务 */
    private final PlatformService platformService;
    /** 广告管理服务 */
    private final IAdService adService;
    /** 商品管理服务 */
    private final IGoodsService goodsService;

    /**
     * 根据 appid 获取租户标识
     *
     * @param appid 小程序或应用的唯一标识
     * @return 租户编号
     */
    @GetMapping("getTenantIdByAppid")
    public R<String> getTenantIdByAppid(@Validated @NotBlank(message = "appid不能为空") String appid) {
        PlatformDTO platformDTO = platformService.getPlatformByAppid(appid, null);
        if (platformDTO == null) {
            return R.fail("平台配置不存在");
        }
        return R.ok("查询成功", platformDTO.getTenantId());
    }

    /**
     * 查询广告列表
     *
     * @param bo 广告筛选条件
     * @return 广告数据
     */
    @GetMapping("listAds")
    public R<List<AdVo>> listAds(AdBo bo) {
        return R.ok(adService.list(bo));
    }

    /**
     * 分页查询商品
     *
     * @param bo 商品筛选条件
     * @param pageQuery 分页参数
     * @return 商品分页数据
     */
    @GetMapping("/pageGoods")
    public R<PageResult<GoodsVo>> pageGoods(GoodsBo bo, PageQuery pageQuery) {
        return R.ok(goodsService.page(bo, pageQuery));
    }
}
