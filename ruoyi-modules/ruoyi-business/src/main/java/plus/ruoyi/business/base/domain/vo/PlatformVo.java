package plus.ruoyi.business.base.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMappers;
import plus.ruoyi.business.base.domain.Platform;
import plus.ruoyi.business.base.domain.bo.PlatformBo;
import plus.ruoyi.common.core.dict.DictEnableStatus;
import plus.ruoyi.common.core.dict.DictPlatformType;
import plus.ruoyi.common.excel.annotation.ExcelDictFormat;
import plus.ruoyi.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 平台配置视图对象 b_platform
 *
 * @author 抓蛙师
 * @date 2025-05-14
 */
@Data
@ExcelIgnoreUnannotated
@AutoMappers({
    @AutoMapper(target = Platform.class),
    @AutoMapper(target = PlatformBo.class)
})
public class PlatformVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 平台配置id
     */
    @ExcelProperty(value = "平台配置id")
    private Long id;

    /**
     * 平台类型
     */
    @ExcelProperty(value = "平台类型", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = DictPlatformType.DICT_TYPE)
    private String type;

    /**
     * 名称
     */
    @ExcelProperty(value = "名称")
    private String name;

    /**
     * appid
     */
    @ExcelProperty(value = "appid")
    private String appid;

    /**
     * 密钥
     */
    @ExcelProperty(value = "密钥")
    private String secret;

    /**
     * 接口token
     */
    @ExcelProperty(value = "接口token")
    private String token;

    /**
     * 加密密钥
     */
    @ExcelProperty(value = "加密密钥")
    private String aeskey;

    /**
     * 关联支付配置
     */
    @ExcelProperty(value = "关联支付配置")
    private String paymentIds;

    /**
     * 模板配置
     */
    private String templateConfigs;

    /**
     * 状态
     */
    @ExcelProperty(value = "状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = DictEnableStatus.DICT_TYPE)
    private String status;

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
