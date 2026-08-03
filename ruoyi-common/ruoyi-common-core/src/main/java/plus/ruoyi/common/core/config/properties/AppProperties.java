package plus.ruoyi.common.core.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 读取项目相关配置
 *
 * @author Lion Li
 */

@Data
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /**
     * 应用id
     */
    private String id;
    /**
     * 应用名称/标题
     */
    private String title;

    /**
     * 授权码
     */
    private String license;

    /**
     * 离线授权码
     * 当网络验证失败时使用,需要管理员根据设备指纹生成
     */
    private String offlineLicense;

    /**
     * 应用版本
     */
    private String version;

    /**
     * 应用版权年份
     */
    private String copyrightYear;

    /**
     * 应用本地文件上传路径
     */
    private String uploadPath;

    /**
     * 应用基础路径(供支付等模块构建回调地址使用)
     */
    private String baseApi;

}
