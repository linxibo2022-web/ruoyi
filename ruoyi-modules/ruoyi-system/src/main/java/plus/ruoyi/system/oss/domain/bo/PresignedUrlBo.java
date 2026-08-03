package plus.ruoyi.system.oss.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 获取预签名URL业务对象
 *
 * @author 抓蛙师
 */
@Data
public class PresignedUrlBo {

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
     * 文件类型
     */
    @NotBlank(message = "文件类型不能为空")
    private String fileType;
}
