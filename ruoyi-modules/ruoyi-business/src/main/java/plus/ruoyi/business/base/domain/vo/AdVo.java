package plus.ruoyi.business.base.domain.vo;

import java.util.Date;

import io.github.linpeilie.annotations.AutoMappers;
import io.swagger.v3.oas.annotations.media.Schema;
import plus.ruoyi.business.base.domain.Ad;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import plus.ruoyi.business.base.domain.bo.AdBo;
import plus.ruoyi.common.excel.annotation.ExcelDictFormat;
import plus.ruoyi.common.excel.convert.ExcelDictConvert;
import plus.ruoyi.common.serialmap.annotation.SerialMap;
import plus.ruoyi.common.serialmap.constant.SerialMapConstant;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


/**
 * 广告配置视图对象 b_ad
 *
 * @author 抓蛙师
 * @date 2025-07-17
 */
@Data
@ExcelIgnoreUnannotated
@AutoMappers({
    @AutoMapper(target = Ad.class),
    @AutoMapper(target = AdBo.class)
})
public class AdVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @Schema(description = "主键id")
    @ExcelProperty(value = "主键id")
    private Long id;

    /**
     * appid
     */
    @Schema(description = "应用ID")
    @ExcelProperty(value = "appid")
    private String appid;

    /**
     * 广告位id
     */
    @Schema(description = "广告位ID")
    @ExcelProperty(value = "广告位id")
    private String adUnitId;

    /**
     * 广告名称
     */
    @Schema(description = "广告名称")
    @ExcelProperty(value = "广告名称")
    private String adName;

    /**
     * 广告类型
     */
    @Schema(description = "广告类型")
    @ExcelProperty(value = "广告类型")
    private String adType;

    /**
     * 投放位置
     */
    @Schema(description = "投放位置")
    @ExcelProperty(value = "投放位置")
    private String position;

    /**
     * 广告图片
     */
    @Schema(description = "广告图片URL")
    @ExcelProperty(value = "广告图片")
    @SerialMap(converter = SerialMapConstant.PRESIGNED_URL)
    private String img;

    /**
     * 描述
     */
    @Schema(description = "广告描述")
    @ExcelProperty(value = "描述")
    private String description;

    /**
     * 跳转appid
     */
    @Schema(description = "跳转应用ID")
    @ExcelProperty(value = "跳转appid")
    private String jumpAppid;

    /**
     * 跳转路径
     */
    @Schema(description = "跳转路径")
    @ExcelProperty(value = "跳转路径")
    private String jumpPath;

    /**
     * 样式配置
     */
    @Schema(description = "样式配置JSON")
    @ExcelProperty(value = "样式配置")
    private String styleConfig;

    /**
     * 排序值
     */
    @Schema(description = "排序值，数值越小越靠前")
    @ExcelProperty(value = "排序值")
    private Long sortOrder;

    /**
     * 状态
     */
    @Schema(description = "状态(0-禁用,1-启用)")
    @ExcelProperty(value = "状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_enable_status")
    private String status;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @ExcelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @ExcelProperty(value = "更新时间")
    private Date updateTime;

    /**
     * 备注
     */
    @Schema(description = "备注说明")
    @ExcelProperty(value = "备注")
    private String remark;


}
