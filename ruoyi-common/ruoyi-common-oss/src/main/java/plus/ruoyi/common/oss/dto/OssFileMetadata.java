package plus.ruoyi.common.oss.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * 对象存储文件元数据
 *
 * @author 抓蛙师
 */
@Data
@Builder
public class OssFileMetadata {

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件类型/Content-Type
     */
    private String contentType;

    /**
     * ETag值，用于文件完整性校验
     */
    private String eTag;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 最后修改时间
     */
    private Date updateTime;

    /**
     * 用户自定义元数据
     */
    private Map<String, String> userMetadata;
}
