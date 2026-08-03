package plus.ruoyi.system.oss.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 确认直传上传业务对象
 *
 * @author 抓蛙师
 */
@Data
public class ConfirmDirectUploadBo {

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
     * 文件名
     */
    @NotBlank(message = "文件名不能为空")
    private String fileName;

    /**
     * 文件键
     */
    @NotBlank(message = "文件键不能为空")
    private String fileKey;

    /**
     * 文件URL
     */
    @NotBlank(message = "文件URL不能为空")
    private String fileUrl;

    /**
     * 文件大小（可选）
     */
    private Long fileSize;
}
