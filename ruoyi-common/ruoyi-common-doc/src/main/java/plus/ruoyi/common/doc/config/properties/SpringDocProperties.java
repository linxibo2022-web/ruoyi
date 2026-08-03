package plus.ruoyi.common.doc.config.properties;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.tags.Tag;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.List;

/**
 * SpringDoc配置属性类
 * 用于绑定application.yml中以springdoc为前缀的配置项
 *
 * @author Lion Li
 */
@Data
@ConfigurationProperties(prefix = "springdoc")
public class SpringDocProperties {

    /**
     * 文档基本信息配置
     */
    @NestedConfigurationProperty
    private InfoProperties info = new InfoProperties();

    /**
     * 外部文档链接配置
     */
    @NestedConfigurationProperty
    private ExternalDocumentation externalDocs;

    /**
     * API标签列表
     */
    private List<Tag> tags = null;

    /**
     * 自定义路径配置
     */
    @NestedConfigurationProperty
    private Paths paths = null;

    /**
     * OpenAPI组件配置（安全方案、响应等）
     */
    @NestedConfigurationProperty
    private Components components = null;

    /**
     * 文档基础信息属性类
     * 对应OpenAPI规范中的Info对象
     *
     * @see io.swagger.v3.oas.models.info.Info
     */
    @Data
    public static class InfoProperties {

        /**
         * API文档标题
         */
        private String title = null;

        /**
         * API文档描述信息
         */
        private String description = null;

        /**
         * 联系人信息（姓名、邮箱、网址等）
         */
        @NestedConfigurationProperty
        private Contact contact = null;

        /**
         * 许可证信息
         */
        @NestedConfigurationProperty
        private License license = null;

        /**
         * API版本号
         */
        private String version = null;
    }
}
