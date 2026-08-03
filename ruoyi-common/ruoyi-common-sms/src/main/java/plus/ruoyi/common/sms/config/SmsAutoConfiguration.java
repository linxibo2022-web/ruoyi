package plus.ruoyi.common.sms.config;

import org.dromara.sms4j.api.dao.SmsDao;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import plus.ruoyi.common.sms.channel.SmsMessageChannel;
import plus.ruoyi.common.sms.core.dao.PlusSmsDao;
import plus.ruoyi.common.sms.handler.SmsExceptionHandler;

/**
 * 短信配置类
 *
 * 自动配置短信相关的Bean，在Redis配置完成后执行
 *
 * @author Feng
 */
@AutoConfiguration(after = {RedisAutoConfiguration.class})
public class SmsAutoConfiguration {

    /**
     * 配置短信数据访问对象
     *
     * @return 短信DAO实例
     */
    @Primary
    @Bean
    public SmsDao smsDao() {
        return new PlusSmsDao();
    }

    /**
     * 配置短信异常处理器
     *
     * @return 短信异常处理器实例
     */
    @Bean
    public SmsExceptionHandler smsExceptionHandler() {
        return new SmsExceptionHandler();
    }

    /**
     * 注册短信消息通道
     * <p>
     * 实现统一消息接口，支持通过 MessagePushService 发送短信
     * </p>
     *
     * @return 短信消息通道实例
     */
    @Bean
    public SmsMessageChannel smsMessageChannel() {
        return new SmsMessageChannel();
    }

}
