package plus.ruoyi.common.mybatis.page;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.mock.MockUser;
import plus.ruoyi.common.test.base.BaseUnitTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PageQuery 分页查询参数测试
 * <p>
 * 测试分页查询参数类的核心功能:
 * <ul>
 *   <li>分页参数构建</li>
 *   <li>排序参数处理</li>
 *   <li>默认值处理</li>
 *   <li>边界条件处理</li>
 * </ul>
 *
 * @author 抓蛙师
 */
@DisplayName("PageQuery 分页参数测试")
class PageQueryTest extends BaseUnitTest {

    // ==================== 构造方法测试 ====================

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("构造方法 - 正常参数")
        void testConstructorWithValidParams() {
            PageQuery pageQuery = new PageQuery(10, 1);

            assertEquals(10, pageQuery.getPageSize(), "每页大小应为10");
            assertEquals(1, pageQuery.getPageNum(), "当前页应为1");
        }

        @Test
        @DisplayName("构造方法 - null参数")
        void testConstructorWithNullParams() {
            PageQuery pageQuery = new PageQuery(null, null);

            assertNull(pageQuery.getPageSize(), "每页大小应为null");
            assertNull(pageQuery.getPageNum(), "当前页应为null");
        }
    }

    // ==================== build方法测试 ====================

    @Nested
    @DisplayName("build方法测试")
    class BuildTests {

        @Test
        @DisplayName("build - 正常分页参数")
        void testBuildWithValidParams() {
            PageQuery pageQuery = new PageQuery(10, 2);
            Page<MockUser> page = pageQuery.build();

            assertNotNull(page, "Page对象不应为null");
            assertEquals(2, page.getCurrent(), "当前页应为2");
            assertEquals(10, page.getSize(), "每页大小应为10");
        }

        @Test
        @DisplayName("build - null参数使用默认值")
        void testBuildWithNullParams() {
            PageQuery pageQuery = new PageQuery(null, null);
            Page<MockUser> page = pageQuery.build();

            assertNotNull(page, "Page对象不应为null");
            assertEquals(PageQuery.DEFAULT_PAGE_NUM, page.getCurrent(), "应使用默认页码");
            assertEquals(PageQuery.DEFAULT_PAGE_SIZE, page.getSize(), "应使用默认每页大小");
        }

        @Test
        @DisplayName("build - 页码为0使用默认值")
        void testBuildWithZeroPageNum() {
            PageQuery pageQuery = new PageQuery(10, 0);
            Page<MockUser> page = pageQuery.build();

            assertEquals(PageQuery.DEFAULT_PAGE_NUM, page.getCurrent(), "页码为0时应使用默认页码1");
        }

        @Test
        @DisplayName("build - 负数页码使用默认值")
        void testBuildWithNegativePageNum() {
            PageQuery pageQuery = new PageQuery(10, -1);
            Page<MockUser> page = pageQuery.build();

            assertEquals(PageQuery.DEFAULT_PAGE_NUM, page.getCurrent(), "负数页码应使用默认页码1");
        }

        @Test
        @DisplayName("build - 大页码测试")
        void testBuildWithLargePageNum() {
            PageQuery pageQuery = new PageQuery(10, 999999);
            Page<MockUser> page = pageQuery.build();

            assertEquals(999999, page.getCurrent(), "大页码应正常处理");
        }
    }

    // ==================== 排序测试 ====================

    @Nested
    @DisplayName("排序测试")
    class OrderTests {

        @Test
        @DisplayName("排序 - 单字段升序")
        void testSingleFieldAsc() {
            PageQuery pageQuery = new PageQuery(10, 1);
            pageQuery.setOrderByColumn("id");
            pageQuery.setIsAsc("asc");

            Page<MockUser> page = pageQuery.build();
            List<OrderItem> orders = page.orders();

            assertNotNull(orders, "排序列表不应为null");
            assertEquals(1, orders.size(), "应有1个排序条件");
            assertEquals("id", orders.get(0).getColumn(), "排序字段应为id");
            assertTrue(orders.get(0).isAsc(), "应为升序");
        }

        @Test
        @DisplayName("排序 - 单字段降序")
        void testSingleFieldDesc() {
            PageQuery pageQuery = new PageQuery(10, 1);
            pageQuery.setOrderByColumn("createTime");
            pageQuery.setIsAsc("desc");

            Page<MockUser> page = pageQuery.build();
            List<OrderItem> orders = page.orders();

            assertNotNull(orders, "排序列表不应为null");
            assertEquals(1, orders.size(), "应有1个排序条件");
            assertEquals("create_time", orders.get(0).getColumn(), "驼峰应转为下划线");
            assertFalse(orders.get(0).isAsc(), "应为降序");
        }

        @Test
        @DisplayName("排序 - 多字段相同排序方向")
        void testMultiFieldSameDirection() {
            PageQuery pageQuery = new PageQuery(10, 1);
            pageQuery.setOrderByColumn("id,createTime");
            pageQuery.setIsAsc("asc");

            Page<MockUser> page = pageQuery.build();
            List<OrderItem> orders = page.orders();

            assertNotNull(orders, "排序列表不应为null");
            assertEquals(2, orders.size(), "应有2个排序条件");
            assertEquals("id", orders.get(0).getColumn(), "第一个排序字段应为id");
            assertEquals("create_time", orders.get(1).getColumn(), "第二个排序字段应为create_time");
            assertTrue(orders.get(0).isAsc(), "第一个应为升序");
            assertTrue(orders.get(1).isAsc(), "第二个应为升序");
        }

        @Test
        @DisplayName("排序 - 多字段不同排序方向")
        void testMultiFieldDifferentDirection() {
            PageQuery pageQuery = new PageQuery(10, 1);
            pageQuery.setOrderByColumn("id,createTime");
            pageQuery.setIsAsc("asc,desc");

            Page<MockUser> page = pageQuery.build();
            List<OrderItem> orders = page.orders();

            assertNotNull(orders, "排序列表不应为null");
            assertEquals(2, orders.size(), "应有2个排序条件");
            assertTrue(orders.get(0).isAsc(), "第一个应为升序");
            assertFalse(orders.get(1).isAsc(), "第二个应为降序");
        }

        @Test
        @DisplayName("排序 - 兼容前端ascending/descending")
        void testFrontendSortCompatibility() {
            PageQuery pageQuery = new PageQuery(10, 1);
            pageQuery.setOrderByColumn("id");
            pageQuery.setIsAsc("ascending");

            Page<MockUser> page = pageQuery.build();
            List<OrderItem> orders = page.orders();

            assertNotNull(orders, "排序列表不应为null");
            assertTrue(orders.get(0).isAsc(), "ascending应转为升序");

            // 测试descending
            pageQuery.setIsAsc("descending");
            page = pageQuery.build();
            orders = page.orders();
            assertFalse(orders.get(0).isAsc(), "descending应转为降序");
        }

        @Test
        @DisplayName("排序 - 空排序字段不生成排序条件")
        void testEmptyOrderByColumn() {
            PageQuery pageQuery = new PageQuery(10, 1);
            pageQuery.setOrderByColumn("");
            pageQuery.setIsAsc("asc");

            Page<MockUser> page = pageQuery.build();
            List<OrderItem> orders = page.orders();

            assertTrue(orders == null || orders.isEmpty(), "空排序字段不应生成排序条件");
        }

        @Test
        @DisplayName("排序 - null排序字段不生成排序条件")
        void testNullOrderByColumn() {
            PageQuery pageQuery = new PageQuery(10, 1);
            pageQuery.setOrderByColumn(null);
            pageQuery.setIsAsc("asc");

            Page<MockUser> page = pageQuery.build();
            List<OrderItem> orders = page.orders();

            assertTrue(orders == null || orders.isEmpty(), "null排序字段不应生成排序条件");
        }

        @Test
        @DisplayName("排序 - 空排序方向不生成排序条件")
        void testEmptyIsAsc() {
            PageQuery pageQuery = new PageQuery(10, 1);
            pageQuery.setOrderByColumn("id");
            pageQuery.setIsAsc("");

            Page<MockUser> page = pageQuery.build();
            List<OrderItem> orders = page.orders();

            assertTrue(orders == null || orders.isEmpty(), "空排序方向不应生成排序条件");
        }

        @Test
        @DisplayName("排序 - 无效排序方向应抛出异常")
        void testInvalidIsAsc() {
            PageQuery pageQuery = new PageQuery(10, 1);
            pageQuery.setOrderByColumn("id");
            pageQuery.setIsAsc("invalid");

            assertThrows(ServiceException.class, pageQuery::build, "无效排序方向应抛出异常");
        }

        @Test
        @DisplayName("排序 - 字段数与方向数不匹配应抛出异常")
        void testMismatchedFieldAndDirection() {
            PageQuery pageQuery = new PageQuery(10, 1);
            // 注意: 不使用 updateTime，因为包含 UPDATE 关键字会被 SqlUtil 拦截
            pageQuery.setOrderByColumn("id,createTime,nickName");
            pageQuery.setIsAsc("asc,desc"); // 3个字段但只有2个方向

            // PageQuery.buildOrderItem 检测到字段数和方向数不匹配，抛出 ServiceException
            assertThrows(ServiceException.class, pageQuery::build, "字段数与方向数不匹配应抛出异常");
        }

        @Test
        @DisplayName("排序 - 驼峰转下划线")
        void testCamelToUnderscore() {
            PageQuery pageQuery = new PageQuery(10, 1);
            // 注意: 不使用 updateTime，因为包含 UPDATE 关键字会被 SqlUtil.checkForDangerousKeywords 拦截
            pageQuery.setOrderByColumn("userName,createTime,nickName");
            pageQuery.setIsAsc("asc,asc,asc"); // 3个字段需要3个方向

            Page<MockUser> page = pageQuery.build();
            List<OrderItem> orders = page.orders();

            assertEquals("user_name", orders.get(0).getColumn(), "userName应转为user_name");
            assertEquals("create_time", orders.get(1).getColumn(), "createTime应转为create_time");
            assertEquals("nick_name", orders.get(2).getColumn(), "nickName应转为nick_name");
        }
    }

    // ==================== getFirstNum方法测试 ====================

    @Nested
    @DisplayName("getFirstNum方法测试")
    class GetFirstNumTests {

        @Test
        @DisplayName("getFirstNum - 第1页")
        void testGetFirstNumPage1() {
            PageQuery pageQuery = new PageQuery(10, 1);

            assertEquals(0, pageQuery.getFirstNum(), "第1页起始索引应为0");
        }

        @Test
        @DisplayName("getFirstNum - 第2页")
        void testGetFirstNumPage2() {
            PageQuery pageQuery = new PageQuery(10, 2);

            assertEquals(10, pageQuery.getFirstNum(), "第2页起始索引应为10");
        }

        @Test
        @DisplayName("getFirstNum - 第5页")
        void testGetFirstNumPage5() {
            PageQuery pageQuery = new PageQuery(20, 5);

            assertEquals(80, pageQuery.getFirstNum(), "第5页(每页20条)起始索引应为80");
        }
    }

    // ==================== 默认值常量测试 ====================

    @Nested
    @DisplayName("默认值常量测试")
    class DefaultValueTests {

        @Test
        @DisplayName("默认页码常量")
        void testDefaultPageNum() {
            assertEquals(1, PageQuery.DEFAULT_PAGE_NUM, "默认页码应为1");
        }

        @Test
        @DisplayName("默认每页大小常量")
        void testDefaultPageSize() {
            assertEquals(Integer.MAX_VALUE, PageQuery.DEFAULT_PAGE_SIZE, "默认每页大小应为Integer.MAX_VALUE");
        }
    }

    // ==================== Setter/Getter测试 ====================

    @Nested
    @DisplayName("Setter/Getter测试")
    class SetterGetterTests {

        @Test
        @DisplayName("setPageSize/getPageSize")
        void testPageSize() {
            PageQuery pageQuery = new PageQuery(10, 1);
            pageQuery.setPageSize(20);

            assertEquals(20, pageQuery.getPageSize(), "每页大小应为20");
        }

        @Test
        @DisplayName("setPageNum/getPageNum")
        void testPageNum() {
            PageQuery pageQuery = new PageQuery(10, 1);
            pageQuery.setPageNum(5);

            assertEquals(5, pageQuery.getPageNum(), "当前页应为5");
        }

        @Test
        @DisplayName("setOrderByColumn/getOrderByColumn")
        void testOrderByColumn() {
            PageQuery pageQuery = new PageQuery(10, 1);
            pageQuery.setOrderByColumn("id,name");

            assertEquals("id,name", pageQuery.getOrderByColumn(), "排序字段应匹配");
        }

        @Test
        @DisplayName("setIsAsc/getIsAsc")
        void testIsAsc() {
            PageQuery pageQuery = new PageQuery(10, 1);
            pageQuery.setIsAsc("asc,desc");

            assertEquals("asc,desc", pageQuery.getIsAsc(), "排序方向应匹配");
        }
    }

    // ==================== 边界条件测试 ====================

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryTests {

        @Test
        @DisplayName("极小每页大小")
        void testMinPageSize() {
            PageQuery pageQuery = new PageQuery(1, 1);
            Page<MockUser> page = pageQuery.build();

            assertEquals(1, page.getSize(), "每页大小应为1");
        }

        @Test
        @DisplayName("极大每页大小")
        void testMaxPageSize() {
            PageQuery pageQuery = new PageQuery(Integer.MAX_VALUE, 1);
            Page<MockUser> page = pageQuery.build();

            assertEquals(Integer.MAX_VALUE, page.getSize(), "每页大小应为Integer.MAX_VALUE");
        }

        @Test
        @DisplayName("极大页码")
        void testMaxPageNum() {
            PageQuery pageQuery = new PageQuery(10, Integer.MAX_VALUE);
            Page<MockUser> page = pageQuery.build();

            assertEquals(Integer.MAX_VALUE, page.getCurrent(), "页码应为Integer.MAX_VALUE");
        }
    }
}
