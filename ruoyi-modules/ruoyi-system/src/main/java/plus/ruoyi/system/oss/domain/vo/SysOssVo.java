package plus.ruoyi.system.oss.domain.vo;

import io.github.linpeilie.annotations.AutoMappers;
import plus.ruoyi.common.core.domain.dto.OssDTO;
import plus.ruoyi.common.serialmap.annotation.SerialMap;
import plus.ruoyi.common.serialmap.constant.SerialMapConstant;
import plus.ruoyi.system.oss.domain.SysOss;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * OSS对象存储视图对象 sys_oss
 *
 * @author Lion Li
 */
@Data
@AutoMappers({@AutoMapper(target = SysOss.class), @AutoMapper(target = OssDTO.class)})
public class SysOssVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 对象存储主键
     */
    private Long ossId;

    /**
     * 目录id
     */
    private Long directoryId;

    /**
     * 目录名称
     */
    @SerialMap(converter = SerialMapConstant.DIRECTORY_ID_DIRECTORY_NAME, source = "directoryId")
    private String directoryName;

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
     * URL地址（预览用，私有库会自动生成预签名URL）
     */
    @SerialMap(converter = SerialMapConstant.PRESIGNED_URL)
    private String url;

    /**
     * 原始URL地址（存储用，不含预签名参数）
     * 前端上传组件应使用此字段作为存储值
     */
    private String originalUrl;

    /**
     * 扩展字段
     */
    private String ext1;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 上传人
     */
    private Long createBy;

    /**
     * 上传人名称
     */
    @SerialMap(converter = SerialMapConstant.USER_ID_TO_NAME, source = "createBy")
    private String createByName;

    /**
     * 服务商
     */
    private String service;


}
