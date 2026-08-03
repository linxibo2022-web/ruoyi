package plus.ruoyi.business.mall.service;

import plus.ruoyi.business.mall.domain.bo.GoodsSkuBo;
import plus.ruoyi.business.mall.domain.vo.GoodsSkuVo;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import java.util.Collection;
import java.util.List;

/**
 * 商品SKU服务接口
 *
 * @author 抓蛙师
 * @date 2025-11-01
 */
public interface IGoodsSkuService {

    /**
     * 根据ID查询
     */
    GoodsSkuVo get(Long id);

    /**
     * 根据商品ID查询SKU列表
     */
    List<GoodsSkuVo> listByGoodsId(Long goodsId);

    /**
     * 查询列表
     */
    List<GoodsSkuVo> list(GoodsSkuBo bo);

    /**
     * 分页查询
     */
    PageResult<GoodsSkuVo> page(GoodsSkuBo bo, PageQuery pageQuery);

    /**
     * 新增
     */
    Long add(GoodsSkuBo bo);

    /**
     * 修改
     *
     * @param bo 业务对象
     * @return 影响行数
     */
    int update(GoodsSkuBo bo);

    /**
     * 批量删除
     *
     * @param ids ID集合
     * @return 影响行数
     */
    int batchDelete(Collection<Long> ids);

    /**
     * 批量保存(先删除商品的所有SKU，再批量新增)
     */
    boolean batchSaveByGoodsId(Long goodsId, List<GoodsSkuBo> boList);

    /**
     * 根据商品ID删除所有SKU
     */
    boolean deleteByGoodsId(Long goodsId);
}
