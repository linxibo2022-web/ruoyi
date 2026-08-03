package plus.ruoyi.business.base.domain.bo;

import io.github.linpeilie.annotations.AutoMappers;
import io.swagger.v3.oas.annotations.media.Schema;
import plus.ruoyi.business.base.domain.Ad;
import plus.ruoyi.business.base.domain.vo.AdVo;
import plus.ruoyi.common.mybatis.core.domain.BaseEntity;
import plus.ruoyi.common.core.validate.AddGroup;
import plus.ruoyi.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 广告配置业务对象 b_ad
 *
 * @author 抓蛙师
 * @date 2025-07-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMappers({
    @AutoMapper(target = Ad.class, reverseConvertGenerate = false),
    @AutoMapper(target = AdVo.class)
})
public class AdBo extends BaseEntity {

    /**
     * 主键id
     */
    @Schema(description = "主键id")
    @NotNull(message = "主键id不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * appid
     */
    @Schema(description = "应用ID")
    private String appid;

    /**
     * 广告位id
     */
    @Schema(description = "广告位ID")
    private String adUnitId;

    /**
     * 广告名称
     */
    @Schema(description = "广告名称")
    @NotBlank(message = "广告名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String adName;

    /**
     * 广告类型
     */
    @Schema(description = "广告类型")
    @NotBlank(message = "广告类型不能为空", groups = { AddGroup.class, EditGroup.class })
    private String adType;

    /**
     * 投放位置
     */
    @Schema(description = "投放位置")
    private String position;

    /**
     * 广告图片
     */
    @Schema(description = "广告图片URL")
    private String img;

    /**
     * 描述
     */
    @Schema(description = "广告描述")
    private String description;

    /**
     * 跳转appid
     */
    @Schema(description = "跳转应用ID")
    private String jumpAppid;

    /**
     * 跳转路径
     */
    @Schema(description = "跳转路径")
    private String jumpPath;

    /**
     * 样式配置
     */
    @Schema(description = "样式配置JSON")
    private String styleConfig;

    /**
     * 排序值
     */
    @Schema(description = "排序值，数值越小越靠前")
    private Long sortOrder;

    /**
     * 状态
     */
    @Schema(description = "状态(0-禁用,1-启用)")
    private String status;

    /**
     * 备注
     */
    @Schema(description = "备注说明")
    private String remark;


}
