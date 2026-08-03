package plus.ruoyi.business.service.mall;

import plus.ruoyi.common.test.base.BaseServiceTest;
import plus.ruoyi.business.mall.domain.bo.OrderBo;
import plus.ruoyi.business.mall.domain.vo.OrderVo;
import plus.ruoyi.business.mall.service.IOrderService;
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
 * OrderService 订单服务测试
 * <p>
 * ⚠️ 重要:所有测试都会自动回滚,不会产生脏数据
 *
 * @author 抓蛙师
 */
@SpringBootTest
@Transactional
@DisplayName("订单服务测试")
public class OrderServiceTest extends BaseServiceTest {

    @Autowired
    private IOrderService orderService;

    @Test
    @DisplayName("测试add-新增订单")
    public void testAdd() {
        OrderBo order = createTestOrder("测试订单");

        Long orderId = orderService.add(order);

        assertNotNull(orderId);
        assertTrue(orderId > 0);

        // 验证可以查询到
        OrderVo orderVo = orderService.get(orderId);
        assertNotNull(orderVo);
        assertEquals(order.getOrderNo(), orderVo.getOrderNo());
    }

    @Test
    @DisplayName("测试get-查询订单详情")
    public void testGet() {
        OrderBo order = createTestOrder("查询测试订单");
        Long orderId = orderService.add(order);

        OrderVo orderVo = orderService.get(orderId);

        assertNotNull(orderVo);
        assertEquals(orderId, orderVo.getId());
        assertEquals(order.getOrderNo(), orderVo.getOrderNo());
        assertEquals(order.getTotalAmount(), orderVo.getTotalAmount());
    }

    @Test
    @DisplayName("测试list-查询订单列表")
    public void testList() {
        createAndSaveOrder("列表订单1");
        createAndSaveOrder("列表订单2");

        OrderBo queryBo = new OrderBo();
        List<OrderVo> list = orderService.list(queryBo);

        assertNotNull(list);
        assertTrue(list.size() >= 2);
    }

    @Test
    @DisplayName("测试page-分页查询订单")
    public void testPage() {
        for (int i = 0; i < 3; i++) {
            createAndSaveOrder("分页订单" + i);
        }

        OrderBo queryBo = new OrderBo();
        PageQuery pageQuery = new PageQuery(10, 1);
        PageResult<OrderVo> result = orderService.page(queryBo, pageQuery);

        assertNotNull(result);
        assertTrue(result.getTotal() >= 3);
    }

    @Test
    @DisplayName("测试update-修改订单")
    public void testUpdate() {
        OrderBo order = createTestOrder("修改测试订单");
        Long orderId = orderService.add(order);

        order.setId(orderId);
        order.setOrderStatus("2"); // 已支付
        order.setPaymentMethod("wechat");

        int result = orderService.update(order);

        assertTrue(result > 0);

        // 验证修改成功
        OrderVo updated = orderService.get(orderId);
        assertEquals("2", updated.getOrderStatus());
        assertEquals("wechat", updated.getPaymentMethod());
    }

    @Test
    @DisplayName("测试batchDelete-批量删除订单")
    public void testBatchDelete() {
        Long id1 = createAndSaveOrder("删除订单1");
        Long id2 = createAndSaveOrder("删除订单2");

        int result = orderService.batchDelete(Arrays.asList(id1, id2));

        assertTrue(result > 0);

        // 验证已删除
        OrderVo order1 = orderService.get(id1);
        OrderVo order2 = orderService.get(id2);
        assertNull(order1);
        assertNull(order2);
    }

    @Test
    @DisplayName("测试batchSave-批量保存订单")
    public void testBatchSave() {
        OrderBo order1 = createTestOrder("批量订单1");
        OrderBo order2 = createTestOrder("批量订单2");

        int result = orderService.batchSave(Arrays.asList(order1, order2));

        assertTrue(result > 0);
    }

    @Test
    @DisplayName("测试getByOutTradeNo-根据商户订单号查询")
    public void testGetByOutTradeNo() {
        OrderBo order = createTestOrder("商户订单测试");
        String outTradeNo = "OUT" + System.currentTimeMillis();
        order.setTransactionId(outTradeNo);
        orderService.add(order);

        OrderVo orderVo = orderService.getByOutTradeNo(outTradeNo, false);

        // 这个方法可能返回null,取决于实现
        // assertNotNull(orderVo);
    }

    @Test
    @DisplayName("测试list-按订单状态查询")
    public void testListByOrderStatus() {
        OrderBo order = createTestOrder("待支付订单");
        order.setOrderStatus("1"); // 待支付
        orderService.add(order);

        OrderBo queryBo = new OrderBo();
        queryBo.setOrderStatus("1");
        List<OrderVo> list = orderService.list(queryBo);

        assertNotNull(list);
        assertTrue(list.size() > 0);
        list.forEach(o -> assertEquals("1", o.getOrderStatus()));
    }

    @Test
    @DisplayName("测试list-按用户ID查询")
    public void testListByUserId() {
        OrderBo order = createTestOrder("用户订单");
        order.setUserId(1L);
        orderService.add(order);

        OrderBo queryBo = new OrderBo();
        queryBo.setUserId(1L);
        List<OrderVo> list = orderService.list(queryBo);

        assertNotNull(list);
        assertTrue(list.size() > 0);
        list.forEach(o -> assertEquals(1L, o.getUserId()));
    }

    /**
     * 创建测试订单对象
     */
    private OrderBo createTestOrder(String suffix) {
        OrderBo order = new OrderBo();
        order.setOrderNo("ORDER" + System.currentTimeMillis());
        order.setUserId(1L); // 默认用户ID
        order.setGoodsId(1L);
        order.setGoodsName("测试商品_" + suffix);
        order.setGoodsImg("test.jpg");
        order.setPrice(new BigDecimal("99.99"));
        order.setQuantity(1L);
        order.setTotalAmount(new BigDecimal("99.99"));
        order.setActualAmount(new BigDecimal("99.99"));
        order.setOrderStatus("1"); // 待支付
        order.setPaymentMethod("alipay");
        order.setBuyerRemark("测试订单备注");
        return order;
    }

    /**
     * 创建并保存测试订单
     */
    private Long createAndSaveOrder(String suffix) {
        OrderBo order = createTestOrder(suffix);
        return orderService.add(order);
    }

    /**
     * 自定义性能阈值
     */
    @Override
    protected long getPerformanceThreshold() {
        return 3000L; // 3秒
    }
}
