package plus.ruoyi.business.mall.domain.groups;

/**
 * 商城系统验证分组，用于Bean Validation的分组验证，按模块和操作类型组织
 * 示例：@NotBlank(message = "商品名称不能为空", groups = {MallGroups.Goods.Create.class})
 * 示例: createGoods(@Validated(MallGroups.Goods.Create.class) @RequestBody Goods goods)
 *
 * @author yecha
 */
public interface MallGroups {
    /**
     * 通用操作分组
     */
    interface Common {
        /**
         * 创建操作
         */
        interface Add {
        }

        /**
         * 更新操作
         */
        interface Update {
        }

        /**
         * 删除操作
         */
        interface Delete {
        }

        /**
         * 查询操作
         */
        interface Query {
        }
    }

    /**
     * 商品模块验证分组
     */
    interface Goods {
        /**
         * 创建商品
         */
        interface Add {
        }

        /**
         * 更新商品
         */
        interface Update {
        }

        /**
         * 上架商品
         */
        interface OnShelf {
        }

        /**
         * 下架商品
         */
        interface OffShelf {
        }
    }

    /**
     * 订单模块验证分组
     */
    interface Order {
        /**
         * 创建订单
         */
        interface Add {
        }

        /**
         * 更新订单
         */
        interface Update {
        }

        /**
         * 支付订单
         */
        interface Pay {
        }

        /**
         * 取消订单
         */
        interface Cancel {
        }
    }
}
