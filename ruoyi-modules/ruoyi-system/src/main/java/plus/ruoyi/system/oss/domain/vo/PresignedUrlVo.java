package plus.ruoyi.system.oss.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 预签名URL响应Vo
 *
 * @author 抓蛙师
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresignedUrlVo {

    /**
     * 预签名上传URL
     */
    private String presignedUrl;

    /**
     * 模块名称
     */
    private String moduleName;

    /**
     * 目录id
     */
    private Long directoryId;

    /**
     * 文件目录路径
     */
    private String directoryPath;

    /**
     * 文件键
     */
    private String fileKey;

    /**
     * 文件访问URL
     */
    private String fileUrl;

    /**
     * 过期时间（秒）
     */
    private Integer expiration;

    /**
     * 上传提示信息
     */
    private String uploadTip;
}
