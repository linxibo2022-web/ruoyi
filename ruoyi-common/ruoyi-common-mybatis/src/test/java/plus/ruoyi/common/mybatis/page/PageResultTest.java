package plus.ruoyi.common.mybatis.page;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.mybatis.mock.MockUser;
import plus.ruoyi.common.test.base.BaseUnitTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PageResult 分页结果测试
 * <p>
 * 测试分页结果类的核心功能:
 * <ul>
 *   <li>静态工厂方法</li>
 *   <li>构造方法</li>
 *   <li>数据类型转换</li>
 *   <li>分页状态判断</li>
 *   <li>手动分页</li>
 * </ul>
 *
 * @author 抓蛙师
 */
@DisplayName("PageResult 分页结果测试")
class PageResultTest extends BaseUnitTest {

    // ==================== 静态工厂方法测试 ====================

    @Nested
    @DisplayName("静态工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of() - 创建空分页结果")
        void testOfEmpty() {
            PageResult<MockUser> result = PageResult.of();

            assertNotNull(result, "结果不应为null");
            assertTrue(result.getRecords().isEmpty(), "记录列表应为空");
            assertEquals(0, result.getTotal(), "总数应为0");
        }

        @Test
        @DisplayName("of(Page) - 从MyBatis-Plus Page创建")
        void testOfPage() {
            Page<MockUser> page = new Page<>(1, 10);
            page.setTotal(100);
            page.setRecords(createMockUsers(10));

            PageResult<MockUser> result = PageResult.of(page);

            assertNotNull(result, "结果不应为null");
            assertEquals(10, result.getRecords().size(), "记录数应为10");
            assertEquals(100, result.getTotal(), "总数应为100");
            assertEquals(1, result.getCurrent(), "当前页应为1");
            assertEquals(10, result.getSize(), "每页大小应为10");
        }

        @Test
        @DisplayName("of(List) - 从列表创建不分页结果")
        void testOfList() {
            List<MockUser> users = createMockUsers(5);

            PageResult<MockUser> result = PageResult.of(users);

            assertNotNull(result, "结果不应为null");
            assertEquals(5, result.getRecords().size(), "记录数应为5");
            assertEquals(5, result.getTotal(), "总数应为5");
            assertEquals(1, result.getCurrent(), "当前页应为1");
            assertTrue(result.isLast(), "应为最后一页");
        }

        @Test
        @DisplayName("of(List, current, size) - 手动分页")
        void testOfListWithPagination() {
            List<MockUser> allUsers = createMockUsers(25);

            // 第1页，每页10条
            PageResult<MockUser> page1 = PageResult.of(allUsers, 1, 10);
            assertEquals(10, page1.getRecords().size(), "第1页应有10条记录");
            assertEquals(25, page1.getTotal(), "总数应为25");
            assertEquals(1, page1.getCurrent(), "当前页应为1");
            assertFalse(page1.isLast(), "第1页不应是最后一页");

            // 第2页
            PageResult<MockUser> page2 = PageResult.of(allUsers, 2, 10);
            assertEquals(10, page2.getRecords().size(), "第2页应有10条记录");
            assertFalse(page2.isLast(), "第2页不应是最后一页");

            // 第3页（最后一页）
            PageResult<MockUser> page3 = PageResult.of(allUsers, 3, 10);
            assertEquals(5, page3.getRecords().size(), "第3页应有5条记录");
            assertTrue(page3.isLast(), "第3页应是最后一页");
        }

        @Test
        @DisplayName("of(List, current, size) - 空列表")
        void testOfListWithPaginationEmpty() {
            List<MockUser> emptyList = Collections.emptyList();

            PageResult<MockUser> result = PageResult.of(emptyList, 1, 10);

            assertTrue(result.getRecords().isEmpty(), "记录应为空");
            assertEquals(0, result.getTotal(), "总数应为0");
        }

        @Test
        @DisplayName("of(List, current, size) - 页码超出范围")
        void testOfListWithPaginationOutOfRange() {
            List<MockUser> users = createMockUsers(10);

            PageResult<MockUser> result = PageResult.of(users, 100, 10);

            assertTrue(result.getRecords().isEmpty(), "超出范围应返回空记录");
            assertEquals(10, result.getTotal(), "总数应保持为10");
        }
    }

    // ==================== 构造方法测试 ====================

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("无参构造方法")
        void testNoArgConstructor() {
            PageResult<MockUser> result = new PageResult<>();

            assertNull(result.getRecords(), "记录应为null");
            assertEquals(0, result.getTotal(), "总数应为0");
        }

        @Test
        @DisplayName("两参数构造方法 - records和total")
        void testTwoArgConstructor() {
            List<MockUser> users = createMockUsers(5);

            PageResult<MockUser> result = new PageResult<>(users, 100);

            assertEquals(5, result.getRecords().size(), "记录数应为5");
            assertEquals(100, result.getTotal(), "总数应为100");
            assertEquals(1, result.getCurrent(), "当前页默认为1");
            assertTrue(result.isLast(), "默认为最后一页");
        }

        @Test
        @DisplayName("全参数构造方法")
        void testAllArgConstructor() {
            List<MockUser> users = createMockUsers(10);

            PageResult<MockUser> result = new PageResult<>(users, 100, 2, 10, false);

            assertEquals(10, result.getRecords().size(), "记录数应为10");
            assertEquals(100, result.getTotal(), "总数应为100");
            assertEquals(2, result.getCurrent(), "当前页应为2");
            assertEquals(10, result.getSize(), "每页大小应为10");
            assertFalse(result.isLast(), "不应是最后一页");
        }
    }

    // ==================== 分页状态判断测试 ====================

    @Nested
    @DisplayName("分页状态判断测试")
    class PaginationStateTests {

        @Test
        @DisplayName("getPages - 计算总页数")
        void testGetPages() {
            // 100条记录，每页10条 = 10页
            PageResult<MockUser> result = new PageResult<>(createMockUsers(10), 100, 1, 10, false);
            assertEquals(10, result.getPages(), "总页数应为10");

            // 101条记录，每页10条 = 11页
            result = new PageResult<>(createMockUsers(10), 101, 1, 10, false);
            assertEquals(11, result.getPages(), "总页数应为11");

            // 99条记录，每页10条 = 10页
            result = new PageResult<>(createMockUsers(10), 99, 1, 10, false);
            assertEquals(10, result.getPages(), "总页数应为10");
        }

        @Test
        @DisplayName("getPages - 每页大小为0")
        void testGetPagesWithZeroSize() {
            PageResult<MockUser> result = new PageResult<>(createMockUsers(5), 100, 1, 0, false);

            assertEquals(0, result.getPages(), "每页大小为0时总页数应为0");
        }

        @Test
        @DisplayName("hasPrevious - 判断是否有上一页")
        void testHasPrevious() {
            // 第1页没有上一页
            PageResult<MockUser> page1 = new PageResult<>(createMockUsers(10), 100, 1, 10, false);
            assertFalse(page1.hasPrevious(), "第1页不应有上一页");

            // 第2页有上一页
            PageResult<MockUser> page2 = new PageResult<>(createMockUsers(10), 100, 2, 10, false);
            assertTrue(page2.hasPrevious(), "第2页应有上一页");

            // 第10页有上一页
            PageResult<MockUser> page10 = new PageResult<>(createMockUsers(10), 100, 10, 10, true);
            assertTrue(page10.hasPrevious(), "第10页应有上一页");
        }

        @Test
        @DisplayName("hasNext - 判断是否有下一页")
        void testHasNext() {
            // 非最后一页有下一页
            PageResult<MockUser> notLast = new PageResult<>(createMockUsers(10), 100, 1, 10, false);
            assertTrue(notLast.hasNext(), "非最后一页应有下一页");

            // 最后一页没有下一页
            PageResult<MockUser> last = new PageResult<>(createMockUsers(10), 100, 10, 10, true);
            assertFalse(last.hasNext(), "最后一页不应有下一页");
        }

        @Test
        @DisplayName("isLast - 判断是否为最后一页")
        void testIsLast() {
            PageResult<MockUser> notLast = new PageResult<>(createMockUsers(10), 100, 1, 10, false);
            assertFalse(notLast.isLast(), "不应是最后一页");

            PageResult<MockUser> last = new PageResult<>(createMockUsers(10), 100, 10, 10, true);
            assertTrue(last.isLast(), "应是最后一页");
        }
    }

    // ==================== 数据转换测试 ====================

    @Nested
    @DisplayName("数据转换测试")
    class ConversionTests {

        @Test
        @DisplayName("map - 使用函数转换数据类型")
        void testMap() {
            List<MockUser> users = createMockUsers(3);
            PageResult<MockUser> result = new PageResult<>(users, 100, 1, 10, false);

            // 转换为用户名列表
            PageResult<String> stringResult = result.map(MockUser::getUserName);

            assertNotNull(stringResult, "转换结果不应为null");
            assertEquals(3, stringResult.getRecords().size(), "转换后记录数应保持不变");
            assertEquals(100, stringResult.getTotal(), "总数应保持不变");
            assertEquals(1, stringResult.getCurrent(), "当前页应保持不变");

            // 验证转换后的数据
            assertTrue(stringResult.getRecords().get(0).startsWith("user_"), "应包含转换后的用户名");
        }

        @Test
        @DisplayName("map - 空记录转换")
        void testMapEmpty() {
            PageResult<MockUser> result = PageResult.of();

            PageResult<String> stringResult = result.map(MockUser::getUserName);

            assertTrue(stringResult.getRecords().isEmpty(), "转换后记录应为空");
        }

        @Test
        @DisplayName("map - 复杂转换逻辑")
        void testMapComplex() {
            List<MockUser> users = createMockUsers(5);
            PageResult<MockUser> result = new PageResult<>(users, 50, 1, 5, false);

            // 复杂转换：用户名 + 年龄
            PageResult<String> transformed = result.map(user ->
                user.getUserName() + "_age_" + user.getAge()
            );

            assertEquals(5, transformed.getRecords().size(), "转换后记录数应保持不变");
            assertTrue(transformed.getRecords().get(0).contains("_age_"), "应包含转换后的数据");
        }
    }

    // ==================== Setter/Getter链式调用测试 ====================

    @Nested
    @DisplayName("Setter/Getter链式调用测试")
    class ChainedAccessorTests {

        @Test
        @DisplayName("链式设置属性")
        void testChainedSetters() {
            List<MockUser> users = createMockUsers(10);

            PageResult<MockUser> result = new PageResult<MockUser>()
                .setRecords(users)
                .setTotal(100)
                .setCurrent(2)
                .setSize(10)
                .setLast(false);

            assertEquals(10, result.getRecords().size(), "记录数应为10");
            assertEquals(100, result.getTotal(), "总数应为100");
            assertEquals(2, result.getCurrent(), "当前页应为2");
            assertEquals(10, result.getSize(), "每页大小应为10");
            assertFalse(result.isLast(), "不应是最后一页");
        }
    }

    // ==================== 边界条件测试 ====================

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryTests {

        @Test
        @DisplayName("单条记录")
        void testSingleRecord() {
            List<MockUser> users = createMockUsers(1);

            PageResult<MockUser> result = new PageResult<>(users, 1, 1, 10, true);

            assertEquals(1, result.getRecords().size(), "记录数应为1");
            assertEquals(1, result.getTotal(), "总数应为1");
            assertTrue(result.isLast(), "应是最后一页");
            assertFalse(result.hasPrevious(), "不应有上一页");
            assertFalse(result.hasNext(), "不应有下一页");
        }

        @Test
        @DisplayName("大数据量分页")
        void testLargeDataPagination() {
            List<MockUser> users = createMockUsers(1000);

            PageResult<MockUser> result = new PageResult<>(users, 1000000, 500, 1000, false);

            assertEquals(1000, result.getRecords().size(), "记录数应为1000");
            assertEquals(1000000, result.getTotal(), "总数应为1000000");
            assertEquals(1000, result.getPages(), "总页数应为1000");
            assertTrue(result.hasPrevious(), "应有上一页");
            assertTrue(result.hasNext(), "应有下一页");
        }

        @Test
        @DisplayName("null记录列表")
        void testNullRecords() {
            PageResult<MockUser> result = new PageResult<>(null, 0, 1, 10, true);

            assertNull(result.getRecords(), "记录应为null");
        }
    }

    // ==================== IPage转换测试 ====================

    @Nested
    @DisplayName("IPage转换测试")
    class IPageConversionTests {

        @Test
        @DisplayName("of(IPage) - 第一页")
        void testOfIPageFirstPage() {
            Page<MockUser> page = new Page<>(1, 10);
            page.setTotal(100);
            page.setRecords(createMockUsers(10));

            PageResult<MockUser> result = PageResult.of(page);

            assertEquals(1, result.getCurrent(), "当前页应为1");
            assertFalse(result.isLast(), "第一页不应是最后一页");
            assertFalse(result.hasPrevious(), "第一页不应有上一页");
            assertTrue(result.hasNext(), "第一页应有下一页");
        }

        @Test
        @DisplayName("of(IPage) - 中间页")
        void testOfIPageMiddlePage() {
            Page<MockUser> page = new Page<>(5, 10);
            page.setTotal(100);
            page.setRecords(createMockUsers(10));

            PageResult<MockUser> result = PageResult.of(page);

            assertEquals(5, result.getCurrent(), "当前页应为5");
            assertFalse(result.isLast(), "中间页不应是最后一页");
            assertTrue(result.hasPrevious(), "中间页应有上一页");
            assertTrue(result.hasNext(), "中间页应有下一页");
        }

        @Test
        @DisplayName("of(IPage) - 最后一页")
        void testOfIPageLastPage() {
            Page<MockUser> page = new Page<>(10, 10);
            page.setTotal(100);
            page.setRecords(createMockUsers(10));

            PageResult<MockUser> result = PageResult.of(page);

            assertEquals(10, result.getCurrent(), "当前页应为10");
            assertTrue(result.isLast(), "最后一页应是最后一页");
            assertTrue(result.hasPrevious(), "最后一页应有上一页");
            assertFalse(result.hasNext(), "最后一页不应有下一页");
        }

        @Test
        @DisplayName("of(IPage) - 空结果")
        void testOfIPageEmpty() {
            Page<MockUser> page = new Page<>(1, 10);
            page.setTotal(0);
            page.setRecords(Collections.emptyList());

            PageResult<MockUser> result = PageResult.of(page);

            assertTrue(result.getRecords().isEmpty(), "记录应为空");
            assertEquals(0, result.getTotal(), "总数应为0");
            assertTrue(result.isLast(), "空结果应是最后一页");
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建模拟用户列表
     *
     * @param count 用户数量
     * @return 用户列表
     */
    private List<MockUser> createMockUsers(int count) {
        List<MockUser> users = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            MockUser user = new MockUser()
                .setId((long) (i + 1))
                .setUserName("user_" + (i + 1))
                .setNickName("昵称" + (i + 1))
                .setEmail("user" + (i + 1) + "@example.com")
                .setPhone("1380000000" + (i % 10))
                .setAge(20 + (i % 40))
                .setStatus(i % 2 == 0 ? "1" : "0")
                .setDeptId((long) (100 + (i % 5)));
            users.add(user);
        }
        return users;
    }
}
