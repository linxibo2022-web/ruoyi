package plus.ruoyi.system.oss.domain.bo;

import io.github.linpeilie.annotations.AutoMappers;
import plus.ruoyi.system.oss.domain.SysOssDirectory;
import plus.ruoyi.common.mybatis.core.domain.BaseEntity;
import plus.ruoyi.common.core.validate.AddGroup;
import plus.ruoyi.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;
import plus.ruoyi.system.oss.domain.vo.SysOssDirectoryVo;

/**
 * OSS目录业务对象 sys_oss_directory
 *
 * @author 抓蛙师
 * @date 2025-04-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMappers({
    @AutoMapper(target = SysOssDirectory.class, reverseConvertGenerate = false),
    @AutoMapper(target = SysOssDirectoryVo.class)
})
public class SysOssDirectoryBo extends BaseEntity {

    /**
     * 目录ID
     */
    @NotNull(message = "目录ID不能为空", groups = { EditGroup.class })
    private Long directoryId;

    /**
     * 父目录ID
     */
    private Long parentId;

    /**
     * 祖级列表
     */
    private String ancestors;

    /**
     * 目录名称
     */
    @NotBlank(message = "目录名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String directoryName;

    /**
     * 目录路径
     */
    private String directoryPath;

    /**
     * 显示顺序
     */
    private Long orderNum;

    /**
     * 目录状态
     */
    private String status;

    /**
     * 是否默认目录
     */
    private String isDefault;

    /**
     * 备注
     */
    private String remark;


}
