package plus.ruoyi.system.openapi.domain.vo;

import java.util.Date;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import plus.ruoyi.common.excel.annotation.ExcelDictFormat;
import plus.ruoyi.common.excel.convert.ExcelDictConvert;
import plus.ruoyi.common.serialmap.annotation.SerialMap;
import plus.ruoyi.common.serialmap.constant.SerialMapConstant;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import plus.ruoyi.system.openapi.domain.SysApiKey;

import java.io.Serial;
import java.io.Serializable;

/**
 * API密钥视图对象 sys_api_key
 *
 * @author 抓蛙师
 * @date 2025-10-03
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = SysApiKey.class)
public class SysApiKeyVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * API密钥ID
     */
    @ExcelProperty(value = "API密钥ID")
    private Long id;

    /**
     * 应用名称
     */
    @ExcelProperty(value = "应用名称")
    private String appName;

    /**
     * AppKey(公开)
     */
    @ExcelProperty(value = "AppKey")
    private String appKey;

    /**
     * 关联用户ID
     */
    @ExcelProperty(value = "关联用户ID")
    private Long userId;

    /**
     * 关联用户名称
     */
    @SerialMap(converter = SerialMapConstant.USER_ID_TO_NICKNAME, source = "userId")
    @ExcelProperty(value = "关联用户")
    private String userName;

    /**
     * 过期时间
     */
    @ExcelProperty(value = "过期时间")
    private Date expireTime;

    /**
     * 状态(0停用 1正常)
     */
    @ExcelProperty(value = "状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_enable_status")
    private String status;

    /**
     * IP白名单,逗号分隔
     */
    @ExcelProperty(value = "IP白名单")
    private String whiteIps;

    /**
     * 调用次数
     */
    @ExcelProperty(value = "调用次数")
    private Long callCount;

    /**
     * 最后调用时间
     */
    @ExcelProperty(value = "最后调用时间")
    private Date lastCallTime;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 更新时间
     */
    @ExcelProperty(value = "更新时间")
    private Date updateTime;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String remark;

}
