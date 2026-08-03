package plus.ruoyi.business.mall.domain.bo;

import plus.ruoyi.business.mall.domain.Goods;
import plus.ruoyi.common.mybatis.core.domain.BaseEntity;
import plus.ruoyi.common.core.validate.AddGroup;
import plus.ruoyi.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * 商品业务对象 m_goods
 *
 * @author 抓蛙师
 * @date 2025-07-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Goods.class, reverseConvertGenerate = false)
public class GoodsBo extends BaseEntity {

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 商品分类
     */
    private String category;

    /**
     * 商品编码
     */
    private String code;

    /**
     * 商品名称
     */
    @NotBlank(message = "商品名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String name;

    /**
     * 商品主图
     */
    private String img;

    /**
     * 商品图片集(多图,逗号分隔)
     */
    private String imgs;

    /**
     * 规格类型 (0单规格 1多规格)
     */
    private String specType;

    /**
     * 原价(单规格时使用)
     */
    private BigDecimal originalPrice;

    /**
     * 折扣
     */
    private BigDecimal discount;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 库存
     */
    private Long stock;

    /**
     * 销量
     */
    private Long salesCount;

    /**
     * 状态
     */
    private String status;

    /**
     * 排序
     */
    private Long sortOrder;

    /**
     * 备注
     */
    private String remark;


}
