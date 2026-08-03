package plus.ruoyi.business.base.domain;

import plus.ruoyi.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 广告配置对象 b_ad
 *
 * @author 抓蛙师
 * @date 2025-07-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("b_ad")
public class Ad extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id")
    private Long id;

    /**
     * appid
     */
    private String appid;

    /**
     * 广告位id
     */
    private String adUnitId;

    /**
     * 广告名称
     */
    private String adName;

    /**
     * 广告类型
     */
    private String adType;

    /**
     * 投放位置
     */
    private String position;

    /**
     * 广告图片
     */
    private String img;

    /**
     * 描述
     */
    private String description;

    /**
     * 跳转appid
     */
    private String jumpAppid;

    /**
     * 跳转路径
     */
    private String jumpPath;

    /**
     * 样式配置
     */
    private String styleConfig;

    /**
     * 排序值
     */
    private Long sortOrder;

    /**
     * 状态
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否删除
     */
    @TableLogic
    private String isDeleted;


}
