package plus.ruoyi.common.core.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 模块功能开关配置
 * <p>
 * 用于控制可选模块（支付、小程序、公众号等）的启用状态
 * </p>
 *
 * @author 抓蛙师
 */
@Data
@ConfigurationProperties(prefix = "module")
public class ModuleProperties {

    /**
     * 支付模块是否启用
     * <p>
     * true: 启用支付模块，加载支付相关控制器和监听器
     * false: 禁用支付模块，相关Bean不会被加载
     * </p>
     */
    private Boolean payEnabled = true;

    /**
     * 小程序模块是否启用
     * <p>
     * true: 启用小程序模块，加载小程序相关策略和控制器
     * false: 禁用小程序模块，相关Bean不会被加载
     * </p>
     */
    private Boolean miniappEnabled = true;

    /**
     * 公众号模块是否启用
     * <p>
     * true: 启用公众号模块，加载公众号相关控制器
     * false: 禁用公众号模块，相关Bean不会被加载
     * </p>
     */
    private Boolean mpEnabled = true;
}
