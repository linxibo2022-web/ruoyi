package plus.ruoyi.business.mall.service;

import plus.ruoyi.business.mall.domain.bo.GoodsBo;
import plus.ruoyi.business.mall.domain.vo.GoodsVo;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;

import java.util.Collection;
import java.util.List;

/**
 * 商品服务接口
 *
 * @author 抓蛙师
 */
public interface IGoodsService {

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return 视图对象
     */
    GoodsVo get(Long id);

    /**
     * 查询列表
     *
     * @param bo 查询参数
     * @return 列表数据
     */
    List<GoodsVo> list(GoodsBo bo);

    /**
     * 分页查询
     *
     * @param bo        查询参数
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    PageResult<GoodsVo> page(GoodsBo bo, PageQuery pageQuery);

    /**
     * 新增
     *
     * @param bo 业务对象
     * @return 主键ID
     */
    Long add(GoodsBo bo);

    /**
     * 修改
     *
     * @param bo 业务对象
     * @return 影响行数
     */
    int update(GoodsBo bo);

    /**
     * 批量删除
     *
     * @param ids ID集合
     * @return 影响行数
     */
    int batchDelete(Collection<Long> ids);

    /**
     * 批量保存
     *
     * @param boList 业务对象集合
     * @return 影响行数
     */
    int batchSave(List<GoodsBo> boList);

    /**
     * 同步商品主表数据(根据SKU重新计算价格、库存、销量)
     * 适用场景: SKU变更后需要更新商品主表的统计数据
     *
     * @param goodsId 商品ID
     */
    void syncGoodsDataFromSku(Long goodsId);
}
