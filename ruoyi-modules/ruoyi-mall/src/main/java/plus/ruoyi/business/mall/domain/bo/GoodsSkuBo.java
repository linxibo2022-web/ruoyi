package plus.ruoyi.business.mall.domain.bo;

import plus.ruoyi.business.mall.domain.GoodsSku;
import plus.ruoyi.common.mybatis.core.domain.BaseEntity;
import plus.ruoyi.common.core.validate.AddGroup;
import plus.ruoyi.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * 商品SKU业务对象 m_goods_sku
 *
 * @author 抓蛙师
 * @date 2025-11-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = GoodsSku.class, reverseConvertGenerate = false)
public class GoodsSkuBo extends BaseEntity {

    /**
     * SKU ID
     */
    @NotNull(message = "SKU ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 商品ID(关联m_goods.id)
     */
    @NotNull(message = "商品ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long goodsId;

    /**
     * SKU编码(自动生成或手动)
     */
    private String skuCode;

    /**
     * SKU名称(如:红色-S码)
     */
    private String skuName;

    /**
     * 规格值JSON(如:{"颜色":"红色","尺码":"S"})
     */
    @NotBlank(message = "规格值不能为空", groups = { AddGroup.class, EditGroup.class })
    private String specValues;

    /**
     * 原价
     */
    private BigDecimal originalPrice;

    /**
     * 价格
     */
    @NotNull(message = "价格不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal price;

    /**
     * 库存
     */
    private Long stock;

    /**
     * 销量
     */
    private Long salesCount;

    /**
     * SKU图片(可继承商品主图)
     */
    private String img;

    /**
     * 状态 (0停用 1启用)
     */
    private String status;

    /**
     * 排序
     */
    private Long sortOrder;

    /**
     * 是否默认SKU (0否 1是)
     */
    private String isDefault;

    /**
     * 备注
     */
    private String remark;
}
