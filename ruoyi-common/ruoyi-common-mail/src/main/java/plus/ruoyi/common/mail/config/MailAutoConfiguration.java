package plus.ruoyi.common.mail.config;

import cn.hutool.extra.mail.MailAccount;
import plus.ruoyi.common.mail.config.properties.MailProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 邮件服务自动配置类
 * <p>
 * 基于 Hutool 的邮件工具，提供简单易用的邮件发送功能
 * <p>
 * 配置启用条件：需要在配置文件中设置 mail.enabled=true
 * <p>
 * 配置示例：
 * <pre>
 * mail:
 *   enabled: true
 *   host: smtp.qq.com
 *   port: 587
 *   from: your-email@qq.com
 *   user: your-email@qq.com
 *   pass: your-password
 *   auth: true
 *   starttlsEnable: true
 * </pre>
 *
 * @author Michelle.Chung
 */
@AutoConfiguration
@EnableConfigurationProperties(MailProperties.class)
public class MailAutoConfiguration {

    /**
     * 创建邮件账户配置 Bean
     * <p>
     * 根据配置属性创建 Hutool 的 MailAccount 对象，
     * 用于后续的邮件发送操作
     *
     * @param mailProperties 邮件配置属性
     * @return 配置完成的邮件账户对象
     */
    @Bean
    @ConditionalOnProperty(value = "mail.enabled", havingValue = "true")
    public MailAccount mailAccount(MailProperties mailProperties) {
        MailAccount account = new MailAccount();

        // 基础连接配置
        account.setHost(mailProperties.getHost());
        account.setPort(mailProperties.getPort());
        account.setAuth(mailProperties.getAuth());

        // 用户认证信息
        account.setFrom(mailProperties.getFrom());
        account.setUser(mailProperties.getUser());
        account.setPass(mailProperties.getPass());

        // SSL/TLS 安全配置
        account.setSocketFactoryPort(mailProperties.getPort());
        account.setStarttlsEnable(mailProperties.getStarttlsEnable());
        account.setSslEnable(mailProperties.getSslEnable());

        // 超时配置
        account.setTimeout(mailProperties.getTimeout());
        account.setConnectionTimeout(mailProperties.getConnectionTimeout());

        return account;
    }

}
