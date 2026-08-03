package plus.ruoyi.business.mall.domain;

import plus.ruoyi.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

import java.io.Serial;

/**
 * 商品对象 m_goods
 *
 * @author 抓蛙师
 * @date 2025-07-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("m_goods")
public class Goods extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    @TableId(value = "id")
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
     * 价格(单规格时使用/多规格时为最低价)
     */
    private BigDecimal price;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 库存(单规格时使用/多规格时为总库存)
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

    /**
     * 是否删除
     */
    @TableLogic
    private String isDeleted;


}
