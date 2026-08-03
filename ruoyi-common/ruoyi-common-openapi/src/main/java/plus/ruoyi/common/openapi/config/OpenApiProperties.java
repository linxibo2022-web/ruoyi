package plus.ruoyi.common.openapi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 开放平台配置属性
 *
 * @author 抓蛙师
 */
@Data
@ConfigurationProperties(prefix = "openapi")
public class OpenApiProperties {

    /**
     * 是否启用开放平台
     */
    private Boolean enabled = false;

    /**
     * 时间戳过期时间(秒)
     */
    private Integer timestampExpireSeconds = 60;

    /**
     * 每个用户最大密钥数量
     */
    private Integer maxKeys = 5;

    /**
     * AppSecret加密密钥
     */
    private String secretEncryptKey;

    /**
     * 访问控制配置
     */
    private AccessControl accessControl = new AccessControl();

    /**
     * 访问控制配置类
     */
    @Data
    public static class AccessControl {
        /**
         * 访问模式
         */
        private AccessMode mode = AccessMode.ALL;

        /**
         * 允许的角色列表
         */
        private List<String> allowedRoles = new ArrayList<>();
    }

    /**
     * 访问模式枚举
     */
    public enum AccessMode {
        /**
         * 所有用户
         */
        ALL,
        /**
         * 指定角色
         */
        ROLES,
        /**
         * 仅管理员
         */
        ADMIN,
        /**
         * 仅超管
         */
        SUPER_ADMIN
    }
}
