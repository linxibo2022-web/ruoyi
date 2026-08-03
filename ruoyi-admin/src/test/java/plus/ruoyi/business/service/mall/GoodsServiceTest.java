package plus.ruoyi.business.service.mall;

import plus.ruoyi.common.test.base.BaseServiceTest;
import plus.ruoyi.business.mall.domain.bo.GoodsBo;
import plus.ruoyi.business.mall.domain.vo.GoodsVo;
import plus.ruoyi.business.mall.service.IGoodsService;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GoodsService 商品服务测试
 *
 * ⚠️ 重要:所有测试都会自动回滚,不会产生脏数据
 *
 * @author 抓蛙师
 */
@SpringBootTest
@Transactional
@DisplayName("商品服务测试")
public class GoodsServiceTest extends BaseServiceTest {

    @Autowired
    private IGoodsService goodsService;

    @Test
    @DisplayName("测试add-新增商品")
    public void testAdd() {
        GoodsBo goods = createTestGoods("测试商品");

        Long goodsId = goodsService.add(goods);

        assertNotNull(goodsId);
        assertTrue(goodsId > 0);

        // 验证可以查询到
        GoodsVo goodsVo = goodsService.get(goodsId);
        assertNotNull(goodsVo);
        assertEquals(goods.getName(), goodsVo.getName());
    }

    @Test
    @DisplayName("测试get-查询商品详情")
    public void testGet() {
        GoodsBo goods = createTestGoods("查询测试商品");
        Long goodsId = goodsService.add(goods);

        GoodsVo goodsVo = goodsService.get(goodsId);

        assertNotNull(goodsVo);
        assertEquals(goodsId, goodsVo.getId());
        assertEquals(goods.getName(), goodsVo.getName());
        assertEquals(goods.getPrice(), goodsVo.getPrice());
    }

    @Test
    @DisplayName("测试list-查询商品列表")
    public void testList() {
        createAndSaveGoods("列表商品1");
        createAndSaveGoods("列表商品2");

        GoodsBo queryBo = new GoodsBo();
        List<GoodsVo> list = goodsService.list(queryBo);

        assertNotNull(list);
        assertTrue(list.size() >= 2);
    }

    @Test
    @DisplayName("测试page-分页查询商品")
    public void testPage() {
        for (int i = 0; i < 3; i++) {
            createAndSaveGoods("分页商品" + i);
        }

        GoodsBo queryBo = new GoodsBo();
        PageQuery pageQuery = new PageQuery(10, 1);
        PageResult<GoodsVo> result = goodsService.page(queryBo, pageQuery);

        assertNotNull(result);
        assertTrue(result.getTotal() >= 3);
    }

    @Test
    @DisplayName("测试update-修改商品")
    public void testUpdate() {
        GoodsBo goods = createTestGoods("修改测试商品");
        Long goodsId = goodsService.add(goods);

        goods.setId(goodsId);
        goods.setName("修改后的商品名称");
        goods.setPrice(new BigDecimal("199.99"));

        int result = goodsService.update(goods);

        assertTrue(result > 0);

        // 验证修改成功
        GoodsVo updated = goodsService.get(goodsId);
        assertEquals("修改后的商品名称", updated.getName());
        assertEquals(new BigDecimal("199.99"), updated.getPrice());
    }

    @Test
    @DisplayName("测试batchDelete-批量删除商品")
    public void testBatchDelete() {
        Long id1 = createAndSaveGoods("删除商品1");
        Long id2 = createAndSaveGoods("删除商品2");

        int result = goodsService.batchDelete(Arrays.asList(id1, id2));

        assertTrue(result > 0);

        // 验证已删除
        GoodsVo goods1 = goodsService.get(id1);
        GoodsVo goods2 = goodsService.get(id2);
        assertNull(goods1);
        assertNull(goods2);
    }

    @Test
    @DisplayName("测试batchSave-批量保存商品")
    public void testBatchSave() {
        GoodsBo goods1 = createTestGoods("批量商品1");
        GoodsBo goods2 = createTestGoods("批量商品2");

        int result = goodsService.batchSave(Arrays.asList(goods1, goods2));

        assertTrue(result > 0);
    }

    @Test
    @DisplayName("测试list-按分类查询")
    public void testListByCategory() {
        GoodsBo goods = createTestGoods("数码产品");
        goods.setCategory("electronics");
        goodsService.add(goods);

        GoodsBo queryBo = new GoodsBo();
        queryBo.setCategory("electronics");
        List<GoodsVo> list = goodsService.list(queryBo);

        assertNotNull(list);
        if (list.size() > 0) {
            list.forEach(g -> assertEquals("electronics", g.getCategory()));
        }
    }

    @Test
    @DisplayName("测试list-按状态查询")
    public void testListByStatus() {
        GoodsBo goods = createTestGoods("上架商品");
        goods.setStatus("1");
        goodsService.add(goods);

        GoodsBo queryBo = new GoodsBo();
        queryBo.setStatus("1");
        List<GoodsVo> list = goodsService.list(queryBo);

        assertNotNull(list);
        assertTrue(list.size() > 0);
        list.forEach(g -> assertEquals("1", g.getStatus()));
    }

    /**
     * 创建测试商品对象
     */
    private GoodsBo createTestGoods(String name) {
        GoodsBo goods = new GoodsBo();
        goods.setName(name + "_" + System.currentTimeMillis());
        goods.setCode("CODE" + System.currentTimeMillis());
        goods.setCategory("test");
        goods.setPrice(new BigDecimal("99.99"));
        goods.setOriginalPrice(new BigDecimal("199.99"));
        goods.setDiscount(new BigDecimal("0.5"));
        goods.setStock(100L);
        goods.setSalesCount(0L);
        goods.setStatus("1");
        goods.setSortOrder(0L);
        goods.setDescription("测试商品描述");
        return goods;
    }

    /**
     * 创建并保存测试商品
     */
    private Long createAndSaveGoods(String name) {
        GoodsBo goods = createTestGoods(name);
        return goodsService.add(goods);
    }

    /**
     * 自定义性能阈值
     */
    @Override
    protected long getPerformanceThreshold() {
        return 3000L; // 3秒
    }
}
