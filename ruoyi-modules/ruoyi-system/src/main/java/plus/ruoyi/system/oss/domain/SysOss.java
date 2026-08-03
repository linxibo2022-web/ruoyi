package plus.ruoyi.system.oss.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.linpeilie.annotations.AutoMapper;
import plus.ruoyi.common.core.domain.dto.OssDTO;
import plus.ruoyi.common.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * OSS对象存储对象
 *
 * @author Lion Li
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = OssDTO.class)
@TableName("sys_oss")
public class SysOss extends TenantEntity {

    /**
     * 对象存储主键
     */
    @TableId(value = "oss_id")
    private Long ossId;

    /**
     * 目录id
     */
    private Long directoryId;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 原名
     */
    private String originalName;

    /**
     * 文件后缀名
     */
    private String fileSuffix;

    /**
     * 文件大小(字节)
     */
    private Long fileSize;

    /**
     * URL地址
     */
    private String url;

    /**
     * 扩展字段
     */
    private String ext1;

    /**
     * 服务商
     */
    private String service;

}
