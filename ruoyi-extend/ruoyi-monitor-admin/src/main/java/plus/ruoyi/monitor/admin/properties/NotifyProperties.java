package plus.ruoyi.monitor.admin.properties;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 通知配置属性类
 * 用于从配置文件中读取邮件和WebHook的相关配置
 *
 * @author 抓蛙师
 */
@Data
@ConfigurationProperties(prefix = "notify")
@Configuration
public class NotifyProperties {

    /**
     * 邮件通知配置
     */
    private Mail mail;

    /**
     * WebHook通知配置
     */
    private WebHook webHook;

    /**
     * 邮件通知配置
     */
    @Data
    @NoArgsConstructor
    public static class Mail {

        /**
         * 是否启用邮件通知
         */
        private Boolean enabled = false;

        /**
         * 收件人邮箱地址
         */
        private String to;

        /**
         * 邮件主题
         */
        private String subject;

        /**
         * 邮件内容模板
         * 支持占位符：{0}服务名 {1}实例ID {2}状态名 {3}状态 {4}服务URL {5}时间
         */
        private String template;
    }

    /**
     * WebHook通知配置
     */
    @Data
    @NoArgsConstructor
    public static class WebHook {

        /**
         * 是否启用WebHook通知
         */
        private Boolean enabled = false;

        /**
         * 认证类型
         * 0: 无认证
         * 1: 签名认证（如钉钉）
         * 2: 密码认证
         */
        private String type;

        /**
         * 认证密钥或密码
         */
        private String secret;

        /**
         * 触发关键词（某些webhook需要）
         */
        private String keywords;

        /**
         * WebHook地址
         */
        private String url;

        /**
         * 消息内容模板
         * 支持占位符：{0}标题 {1}服务名 {2}实例ID {3}状态名 {4}状态 {5}服务URL {6}时间
         */
        private String template;
    }
}
