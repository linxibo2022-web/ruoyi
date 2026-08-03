package plus.ruoyi.system.oss.domain.bo;

import io.github.linpeilie.annotations.AutoMappers;
import plus.ruoyi.common.mybatis.core.domain.BaseEntity;
import plus.ruoyi.system.oss.domain.SysOss;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import plus.ruoyi.system.oss.domain.vo.SysOssVo;

/**
 * OSS对象存储分页查询对象 sys_oss
 *
 * @author Lion Li
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMappers({
    @AutoMapper(target = SysOss.class, reverseConvertGenerate = false),
    @AutoMapper(target = SysOssVo.class)
})
public class SysOssBo extends BaseEntity {

    /**
     * ossId
     */
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
