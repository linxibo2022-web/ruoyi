package plus.ruoyi.common.pay.balance.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import plus.ruoyi.common.pay.balance.handler.BalancePayHandler;

/**
 * 余额支付自动配置类
 * <p>
 * 负责注册余额支付相关组件:
 * - BalancePayHandler: 余额支付处理器
 *
 * @author 抓蛙师
 */
@Slf4j
@AutoConfiguration
@ConditionalOnProperty(prefix = "module", name = "pay-enabled", havingValue = "true", matchIfMissing = true)
public class BalancePayAutoConfiguration {

    /**
     * 注册余额支付处理器
     */
    @Bean
    @ConditionalOnMissingBean
    public BalancePayHandler balancePayHandler() {
        log.info("创建余额支付处理器 BalancePayHandler");
        return new BalancePayHandler();
    }
}
