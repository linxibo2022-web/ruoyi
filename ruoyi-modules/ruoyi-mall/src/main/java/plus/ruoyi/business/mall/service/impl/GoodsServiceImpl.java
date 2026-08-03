package plus.ruoyi.business.mall.service.impl;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plus.ruoyi.business.mall.dao.IGoodsDao;
import plus.ruoyi.business.mall.domain.Goods;
import plus.ruoyi.business.mall.domain.bo.GoodsBo;
import plus.ruoyi.business.mall.domain.vo.GoodsVo;
import plus.ruoyi.business.mall.domain.vo.GoodsSkuVo;
import plus.ruoyi.business.mall.service.IGoodsService;
import plus.ruoyi.business.mall.service.IGoodsSkuService;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.utils.MapstructUtils;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 商品服务实现
 *
 * @author 抓蛙师
 */
@Service
@RequiredArgsConstructor
public class GoodsServiceImpl implements IGoodsService {

    private final IGoodsDao goodsDao;
    private final IGoodsSkuService goodsSkuService;

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return 视图对象
     */
    @Override
    public GoodsVo get(Long id) {
        Goods entity = goodsDao.getById(id);
        GoodsVo vo = MapstructUtils.convert(entity, GoodsVo.class);

        // 查询SKU列表
        if (vo != null) {
            List<GoodsSkuVo> skuList = goodsSkuService.listByGoodsId(id);
            vo.setSkuList(skuList);
        }

        return vo;
    }

    /**
     * 查询列表
     *
     * @param bo 查询参数
     * @return 列表数据
     */
    @Override
    public List<GoodsVo> list(GoodsBo bo) {
        PlusLambdaQuery<Goods> wrapper = goodsDao.buildQueryWrapper(bo);
        List<Goods> entities = goodsDao.list(wrapper);
        return MapstructUtils.convert(entities, GoodsVo.class);
    }

    /**
     * 分页查询
     *
     * @param bo        查询参数
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    @Override
    public PageResult<GoodsVo> page(GoodsBo bo, PageQuery pageQuery) {
        PlusLambdaQuery<Goods> wrapper = goodsDao.buildQueryWrapper(bo);
        PageResult<Goods> entityPage = goodsDao.page(wrapper, pageQuery);
        return entityPage.convert(GoodsVo.class);
    }

    /**
     * 新增
     *
     * @param bo 业务对象
     * @return 主键ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(GoodsBo bo) {
        Goods entity = MapstructUtils.convert(bo, Goods.class);
        beforeSave(entity);
        goodsDao.insert(entity);
        return entity.getId();
    }

    /**
     * 修改
     *
     * @param bo 业务对象
     * @return 影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(GoodsBo bo) {
        if (bo.getId() == null) {
            throw ServiceException.of("商品ID不能为空");
        }
        if (!goodsDao.exists(bo.getId())) {
            throw ServiceException.of("商品不存在");
        }
        Goods entity = MapstructUtils.convert(bo, Goods.class);
        beforeSave(entity);
        return goodsDao.updateById(entity);
    }

    /**
     * 批量删除
     *
     * @param ids ID集合
     * @return 影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            throw ServiceException.of("ID集合不能为空");
        }
        beforeDelete(ids);
        return goodsDao.deleteByIds(ids);
    }

    /**
     * 批量保存
     *
     * @param boList 业务对象集合
     * @return 影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSave(List<GoodsBo> boList) {
        if (CollUtil.isEmpty(boList)) {
            return 0;
        }
        List<Goods> entities = new ArrayList<>(boList.size());
        for (GoodsBo bo : boList) {
            Goods entity = MapstructUtils.convert(bo, Goods.class);
            beforeSave(entity);
            entities.add(entity);
        }
        return goodsDao.batchSave(entities);
    }

    /**
     * 保存前钩子方法
     * 子类可重写此方法实现数据校验、默认值设置等逻辑
     *
     * @param entity 实体对象
     */
    protected void beforeSave(Goods entity) {
        // 默认实现为空,子类按需重写
    }

    /**
     * 删除前钩子方法
     * 级联删除关联的SKU数据,防止产生孤儿记录
     *
     * @param ids 待删除的ID集合
     */
    protected void beforeDelete(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }

        // 级联删除所有关联的SKU
        for (Long goodsId : ids) {
            goodsSkuService.deleteByGoodsId(goodsId);
        }
    }

    /**
     * 同步商品主表数据(根据SKU重新计算价格、库存、销量)
     * 仅对多规格商品(specType=1)进行同步
     *
     * @param goodsId 商品ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncGoodsDataFromSku(Long goodsId) {
        if (goodsId == null) {
            return;
        }

        // 1. 查询商品信息
        Goods goods = goodsDao.getById(goodsId);
        if (goods == null) {
            return;
        }

        // 2. 只同步多规格商品
        if (!"1".equals(goods.getSpecType())) {
            return;
        }

        // 3. 查询该商品所有启用状态的SKU
        List<GoodsSkuVo> skuList = goodsSkuService.listByGoodsId(goodsId);
        if (CollUtil.isEmpty(skuList)) {
            // 如果没有SKU,将主表数据置零
            goods.setPrice(BigDecimal.ZERO);
            goods.setStock(0L);
            goods.setSalesCount(0L);
            goodsDao.updateById(goods);
            return;
        }

        // 4. 计算商品价格(优先使用默认SKU价格,没有则使用最低价)
        BigDecimal goodsPrice = java.math.BigDecimal.ZERO;

        // 查找默认SKU
        GoodsSkuVo defaultSku = skuList.stream()
            .filter(sku -> "1".equals(sku.getIsDefault()))
            .findFirst()
            .orElse(null);

        if (defaultSku != null && defaultSku.getPrice() != null) {
            // 使用默认SKU价格
            goodsPrice = defaultSku.getPrice();
        } else {
            // 没有默认SKU,使用最低价
            goodsPrice = skuList.stream()
                .map(GoodsSkuVo::getPrice)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        }

        // 5. 计算总库存
        Long totalStock = skuList.stream()
            .map(GoodsSkuVo::getStock)
            .filter(Objects::nonNull)
            .reduce(0L, Long::sum);

        // 6. 计算总销量
        Long totalSales = skuList.stream()
            .map(GoodsSkuVo::getSalesCount)
            .filter(Objects::nonNull)
            .reduce(0L, Long::sum);

        // 7. 更新商品主表
        goods.setPrice(goodsPrice);
        goods.setStock(totalStock);
        goods.setSalesCount(totalSales);
        goodsDao.updateById(goods);
    }
}
