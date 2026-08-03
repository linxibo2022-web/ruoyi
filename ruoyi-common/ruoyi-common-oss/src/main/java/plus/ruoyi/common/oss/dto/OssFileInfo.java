package plus.ruoyi.common.oss.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * 对象存储文件信息
 * 用于文件列表中展示基本信息
 *
 * @author 抓蛙师
 */
@Data
@Builder
public class OssFileInfo {

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
     * 是否为目录
     */
    private Boolean isDirectory;

    /**
     * 最后修改时间
     */
    private Date updateTime;

    /**
     * 完整访问URL
     */
    private String url;
}
