package plus.ruoyi.system.oss.domain;

import plus.ruoyi.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * OSS目录对象 sys_oss_directory
 *
 * @author 抓蛙师
 * @date 2025-04-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_oss_directory")
public class SysOssDirectory extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 目录ID
     */
    @TableId(value = "directory_id")
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
