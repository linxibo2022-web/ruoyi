package plus.ruoyi.business.mall.domain;

import plus.ruoyi.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.io.Serial;

/**
 * 商品SKU对象 m_goods_sku
 *
 * @author 抓蛙师
 * @date 2025-11-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("m_goods_sku")
public class GoodsSku extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * SKU ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 商品ID(关联m_goods.id)
     */
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
    private String specValues;

    /**
     * 原价
     */
    private BigDecimal originalPrice;

    /**
     * 价格
     */
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

    /**
     * 是否删除
     */
    @TableLogic
    private String isDeleted;
}
