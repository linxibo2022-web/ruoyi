package plus.ruoyi.system.oss.domain.vo;

import lombok.Data;
import plus.ruoyi.common.serialmap.annotation.SerialMap;
import plus.ruoyi.common.serialmap.constant.SerialMapConstant;

import java.util.Date;

/**
 * 上传对象信息
 *
 * @author Michelle.Chung
 */
@Data
public class SysOssUploadVo {

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
     * 文件名
     */
    private String fileName;

    /**
     * 对象存储主键
     */
    private String ossId;

    /**
     * 更新时间
     */
    private Date updateTime;

}
