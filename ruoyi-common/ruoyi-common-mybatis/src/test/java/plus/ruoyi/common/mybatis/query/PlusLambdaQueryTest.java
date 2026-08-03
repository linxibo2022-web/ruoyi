package plus.ruoyi.common.mybatis.query;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.common.mybatis.mock.MockUser;
import plus.ruoyi.common.test.base.BaseUnitTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PlusLambdaQuery 单元测试
 * <p>
 * 测试自定义Lambda查询构建器的核心功能:
 * <ul>
 *   <li>静态工厂方法</li>
 *   <li>智能条件处理 (自动过滤null和空字符串)</li>
 *   <li>比较操作 (eq, ne, gt, ge, lt, le)</li>
 *   <li>范围操作 (between, notBetween)</li>
 *   <li>集合操作 (in, notIn)</li>
 *   <li>模糊匹配 (like, likeLeft, likeRight)</li>
 *   <li>聚合函数 (sum, min, max, count, avg)</li>
 * </ul>
 *
 * @author 抓蛙师
 */
@DisplayName("PlusLambdaQuery 查询构建器测试")
class PlusLambdaQueryTest extends BaseUnitTest {

    /**
     * 初始化 MyBatis-Plus 实体类表信息
     * Lambda 表达式需要实体类被注册到 TableInfoHelper 中
     */
    @BeforeAll
    static void initTableInfo() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        assistant.setCurrentNamespace("plus.ruoyi.common.mybatis.mock");
        TableInfoHelper.initTableInfo(assistant, MockUser.class);
    }

    // ==================== 静态工厂方法测试 ====================

    @Nested
    @DisplayName("静态工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of() - 创建空查询实例")
        void testOfEmpty() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of();

            assertNotNull(query, "查询实例不应为null");
            assertNull(query.getEntity(), "实体对象应为null");
            assertNull(query.getEntityClass(), "实体类应为null");
        }

        @Test
        @DisplayName("of(Class) - 基于实体类创建查询实例")
        void testOfClass() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class);

            assertNotNull(query, "查询实例不应为null");
            assertEquals(MockUser.class, query.getEntityClass(), "实体类应匹配");
        }

        @Test
        @DisplayName("of(Entity) - 基于实体对象创建查询实例")
        void testOfEntity() {
            MockUser user = new MockUser().setUserName("test");
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(user);

            assertNotNull(query, "查询实例不应为null");
            assertNotNull(query.getEntity(), "实体对象不应为null");
            assertEquals("test", query.getEntity().getUserName(), "用户名应匹配");
        }
    }

    // ==================== 等值条件测试 ====================

    @Nested
    @DisplayName("等值条件测试 (eq/ne)")
    class EqualityConditionTests {

        @Test
        @DisplayName("eq - 有效值应添加条件")
        void testEqWithValidValue() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .eq(MockUser::getStatus, "1");

            String sql = query.getSqlSegment();
            assertNotNull(sql, "SQL片段不应为null");
            assertTrue(sql.contains("status"), "应包含status字段");
        }

        @Test
        @DisplayName("eq - null值应跳过条件")
        void testEqWithNullValue() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .eq(MockUser::getStatus, null);

            String sql = query.getSqlSegment();
            assertTrue(sql == null || sql.isEmpty(), "null值不应生成条件");
        }

        @Test
        @DisplayName("eq - 空字符串应跳过条件")
        void testEqWithEmptyString() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .eq(MockUser::getStatus, "");

            String sql = query.getSqlSegment();
            assertTrue(sql == null || sql.isEmpty(), "空字符串不应生成条件");
        }

        @Test
        @DisplayName("eq - 空白字符串应跳过条件")
        void testEqWithBlankString() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .eq(MockUser::getStatus, "   ");

            String sql = query.getSqlSegment();
            assertTrue(sql == null || sql.isEmpty(), "空白字符串不应生成条件");
        }

        @Test
        @DisplayName("ne - 有效值应添加不等于条件")
        void testNeWithValidValue() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .ne(MockUser::getStatus, "0");

            String sql = query.getSqlSegment();
            assertNotNull(sql, "SQL片段不应为null");
            assertTrue(sql.contains("status"), "应包含status字段");
            assertTrue(sql.contains("<>"), "应包含不等于操作符");
        }

        @Test
        @DisplayName("ne - null值应跳过条件")
        void testNeWithNullValue() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .ne(MockUser::getStatus, null);

            String sql = query.getSqlSegment();
            assertTrue(sql == null || sql.isEmpty(), "null值不应生成条件");
        }

        @Test
        @DisplayName("eq - 数字类型有效值应添加条件")
        void testEqWithNumberValue() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .eq(MockUser::getAge, 18);

            String sql = query.getSqlSegment();
            assertNotNull(sql, "SQL片段不应为null");
            assertTrue(sql.contains("age"), "应包含age字段");
        }

        @Test
        @DisplayName("eq - Long类型有效值应添加条件")
        void testEqWithLongValue() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .eq(MockUser::getDeptId, 100L);

            String sql = query.getSqlSegment();
            assertNotNull(sql, "SQL片段不应为null");
            assertTrue(sql.contains("dept_id"), "应包含dept_id字段");
        }
    }

    // ==================== 比较条件测试 ====================

    @Nested
    @DisplayName("比较条件测试 (gt/ge/lt/le)")
    class ComparisonConditionTests {

        @Test
        @DisplayName("gt - 大于条件有效值测试")
        void testGtWithValidValue() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .gt(MockUser::getAge, 18);

            String sql = query.getSqlSegment();
            assertNotNull(sql, "SQL片段不应为null");
            assertTrue(sql.contains(">"), "应包含大于操作符");
        }

        @Test
        @DisplayName("gt - null值应跳过条件")
        void testGtWithNullValue() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .gt(MockUser::getAge, null);

            String sql = query.getSqlSegment();
            assertTrue(sql == null || sql.isEmpty(), "null值不应生成条件");
        }

        @Test
        @DisplayName("ge - 大于等于条件有效值测试")
        void testGeWithValidValue() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .ge(MockUser::getAge, 18);

            String sql = query.getSqlSegment();
            assertNotNull(sql, "SQL片段不应为null");
            assertTrue(sql.contains(">="), "应包含大于等于操作符");
        }

        @Test
        @DisplayName("ge - null值应跳过条件")
        void testGeWithNullValue() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .ge(MockUser::getAge, null);

            String sql = query.getSqlSegment();
            assertTrue(sql == null || sql.isEmpty(), "null值不应生成条件");
        }

        @Test
        @DisplayName("lt - 小于条件有效值测试")
        void testLtWithValidValue() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .lt(MockUser::getAge, 60);

            String sql = query.getSqlSegment();
            assertNotNull(sql, "SQL片段不应为null");
            assertTrue(sql.contains("<"), "应包含小于操作符");
        }

        @Test
        @DisplayName("lt - null值应跳过条件")
        void testLtWithNullValue() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .lt(MockUser::getAge, null);

            String sql = query.getSqlSegment();
            assertTrue(sql == null || sql.isEmpty(), "null值不应生成条件");
        }

        @Test
        @DisplayName("le - 小于等于条件有效值测试")
        void testLeWithValidValue() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .le(MockUser::getAge, 60);

            String sql = query.getSqlSegment();
            assertNotNull(sql, "SQL片段不应为null");
            assertTrue(sql.contains("<="), "应包含小于等于操作符");
        }

        @Test
        @DisplayName("le - null值应跳过条件")
        void testLeWithNullValue() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .le(MockUser::getAge, null);

            String sql = query.getSqlSegment();
            assertTrue(sql == null || sql.isEmpty(), "null值不应生成条件");
        }
    }

    // ==================== 范围条件测试 ====================

    @Nested
    @DisplayName("范围条件测试 (between/notBetween)")
    class RangeConditionTests {

        @Test
        @DisplayName("between - 两个有效值应生成BETWEEN条件")
        void testBetweenWithBothValid() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .between(MockUser::getAge, 18, 60);

            String sql = query.getSqlSegment();
            assertNotNull(sql, "SQL片段不应为null");
            assertTrue(sql.contains("BETWEEN"), "应包含BETWEEN关键字");
        }

        @Test
        @DisplayName("between - 只有第一个值有效应转为>=条件")
        void testBetweenWithOnlyFirstValid() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .between(MockUser::getAge, 18, null);

            String sql = query.getSqlSegment();
            assertNotNull(sql, "SQL片段不应为null");
            assertTrue(sql.contains(">="), "应转为大于等于条件");
            assertFalse(sql.contains("BETWEEN"), "不应包含BETWEEN关键字");
        }

        @Test
        @DisplayName("between - 只有第二个值有效应转为<=条件")
        void testBetweenWithOnlySecondValid() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .between(MockUser::getAge, null, 60);

            String sql = query.getSqlSegment();
            assertNotNull(sql, "SQL片段不应为null");
            assertTrue(sql.contains("<="), "应转为小于等于条件");
            assertFalse(sql.contains("BETWEEN"), "不应包含BETWEEN关键字");
        }

        @Test
        @DisplayName("between - 两个值都为null应不生成条件")
        void testBetweenWithBothNull() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .between(MockUser::getAge, null, null);

            String sql = query.getSqlSegment();
            assertTrue(sql == null || sql.isEmpty(), "两个null值不应生成条件");
        }

        @Test
        @DisplayName("between - 空字符串应视为无效值")
        void testBetweenWithEmptyStrings() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .between(MockUser::getUserName, "", "");

            String sql = query.getSqlSegment();
            assertTrue(sql == null || sql.isEmpty(), "空字符串不应生成条件");
        }

        @Test
        @DisplayName("notBetween - 两个有效值应生成NOT BETWEEN条件")
        void testNotBetweenWithBothValid() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .notBetween(MockUser::getAge, 18, 60);

            String sql = query.getSqlSegment();
            assertNotNull(sql, "SQL片段不应为null");
            assertTrue(sql.contains("NOT BETWEEN"), "应包含NOT BETWEEN关键字");
        }

        @Test
        @DisplayName("notBetween - 有一个值为null应不生成条件")
        void testNotBetweenWithOneNull() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .notBetween(MockUser::getAge, 18, null);

            String sql = query.getSqlSegment();
            assertTrue(sql == null || sql.isEmpty(), "NOT BETWEEN需要两个有效值");
        }
    }

    // ==================== 集合条件测试 ====================

    @Nested
    @DisplayName("集合条件测试 (in/notIn)")
    class CollectionConditionTests {

        @Test
        @DisplayName("in - 有效集合应生成IN条件")
        void testInWithValidCollection() {
            List<String> statuses = Arrays.asList("0", "1", "2");
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .in(MockUser::getStatus, statuses);

            String sql = query.getSqlSegment();
            assertNotNull(sql, "SQL片段不应为null");
            assertTrue(sql.contains("IN"), "应包含IN关键字");
        }

        @Test
        @DisplayName("in - 空集合应不生成条件")
        void testInWithEmptyCollection() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .in(MockUser::getStatus, Collections.emptyList());

            String sql = query.getSqlSegment();
            assertTrue(sql == null || sql.isEmpty(), "空集合不应生成条件");
        }

        @Test
        @DisplayName("in - null集合应不生成条件")
        void testInWithNullCollection() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .in(MockUser::getStatus, (List<String>) null);

            String sql = query.getSqlSegment();
            assertTrue(sql == null || sql.isEmpty(), "null集合不应生成条件");
        }

        @Test
        @DisplayName("in - 集合中的null和空字符串应被过滤")
        void testInFiltersInvalidElements() {
            List<String> statuses = Arrays.asList("1", null, "", "  ", "2");
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .in(MockUser::getStatus, statuses);

            String sql = query.getSqlSegment();
            assertNotNull(sql, "SQL片段不应为null");
            assertTrue(sql.contains("IN"), "应包含IN关键字");
            // 只有 "1" 和 "2" 是有效的
        }

        @Test
        @DisplayName("in - 集合全部为无效值应不生成条件")
        void testInWithAllInvalidElements() {
            List<String> statuses = Arrays.asList(null, "", "  ");
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .in(MockUser::getStatus, statuses);

            String sql = query.getSqlSegment();
            assertTrue(sql == null || sql.isEmpty(), "全部无效值不应生成条件");
        }

        @Test
        @DisplayName("notIn - 有效集合应生成NOT IN条件")
        void testNotInWithValidCollection() {
            List<String> statuses = Arrays.asList("0", "2");
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .notIn(MockUser::getStatus, statuses);

            String sql = query.getSqlSegment();
            assertNotNull(sql, "SQL片段不应为null");
            assertTrue(sql.contains("NOT IN"), "应包含NOT IN关键字");
        }

        @Test
        @DisplayName("notIn - 空集合应不生成条件")
        void testNotInWithEmptyCollection() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .notIn(MockUser::getStatus, Collections.emptyList());

            String sql = query.getSqlSegment();
            assertTrue(sql == null || sql.isEmpty(), "空集合不应生成条件");
        }

        @Test
        @DisplayName("in - Long类型集合测试")
        void testInWithLongCollection() {
            List<Long> deptIds = Arrays.asList(100L, 101L, 102L);
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .in(MockUser::getDeptId, deptIds);

            String sql = query.getSqlSegment();
            assertNotNull(sql, "SQL片段不应为null");
            assertTrue(sql.contains("IN"), "应包含IN关键字");
        }
    }

    // ==================== 模糊匹配测试 ====================

    @Nested
    @DisplayName("模糊匹配测试 (like/likeLeft/likeRight)")
    class LikeConditionTests {

        @Test
        @DisplayName("like - 有效值应生成LIKE '%value%'条件")
        void testLikeWithValidValue() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .like(MockUser::getUserName, "张");

            String sql = query.getSqlSegment();
            assertNotNull(sql, "SQL片段不应为null");
            assertTrue(sql.contains("LIKE"), "应包含LIKE关键字");
        }

        @Test
        @DisplayName("like - null值应跳过条件")
        void testLikeWithNullValue() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .like(MockUser::getUserName, null);

            String sql = query.getSqlSegment();
            assertTrue(sql == null || sql.isEmpty(), "null值不应生成条件");
        }

        @Test
        @DisplayName("like - 空字符串应跳过条件")
        void testLikeWithEmptyString() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .like(MockUser::getUserName, "");

            String sql = query.getSqlSegment();
            assertTrue(sql == null || sql.isEmpty(), "空字符串不应生成条件");
        }

        @Test
        @DisplayName("likeLeft - 有效值应生成LIKE '%value'条件")
        void testLikeLeftWithValidValue() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .likeLeft(MockUser::getEmail, "@example.com");

            String sql = query.getSqlSegment();
            assertNotNull(sql, "SQL片段不应为null");
            assertTrue(sql.contains("LIKE"), "应包含LIKE关键字");
        }

        @Test
        @DisplayName("likeLeft - null值应跳过条件")
        void testLikeLeftWithNullValue() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .likeLeft(MockUser::getEmail, null);

            String sql = query.getSqlSegment();
            assertTrue(sql == null || sql.isEmpty(), "null值不应生成条件");
        }

        @Test
        @DisplayName("likeRight - 有效值应生成LIKE 'value%'条件")
        void testLikeRightWithValidValue() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .likeRight(MockUser::getPhone, "138");

            String sql = query.getSqlSegment();
            assertNotNull(sql, "SQL片段不应为null");
            assertTrue(sql.contains("LIKE"), "应包含LIKE关键字");
        }

        @Test
        @DisplayName("likeRight - null值应跳过条件")
        void testLikeRightWithNullValue() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .likeRight(MockUser::getPhone, null);

            String sql = query.getSqlSegment();
            assertTrue(sql == null || sql.isEmpty(), "null值不应生成条件");
        }
    }

    // ==================== Select字段测试 ====================

    @Nested
    @DisplayName("Select字段选择测试")
    class SelectFieldTests {

        @Test
        @DisplayName("select - 选择指定字段")
        void testSelectFields() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .select(MockUser::getId, MockUser::getUserName, MockUser::getStatus);

            String sqlSelect = query.getSqlSelect();
            assertNotNull(sqlSelect, "SQL Select不应为null");
            assertTrue(sqlSelect.contains("id"), "应包含id字段");
            assertTrue(sqlSelect.contains("user_name"), "应包含user_name字段");
            assertTrue(sqlSelect.contains("status"), "应包含status字段");
        }

        @Test
        @DisplayName("select - 选择单个字段")
        void testSelectSingleField() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .select(MockUser::getId);

            String sqlSelect = query.getSqlSelect();
            assertNotNull(sqlSelect, "SQL Select不应为null");
            assertTrue(sqlSelect.contains("id"), "应包含id字段");
        }
    }

    // ==================== 聚合函数测试 ====================

    @Nested
    @DisplayName("聚合函数测试 (sum/min/max/count/avg)")
    class AggregateFunctionTests {

        @Test
        @DisplayName("sum - 求和聚合函数")
        void testSum() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .sum(MockUser::getAge);

            String sqlSelect = query.getSqlSelect();
            assertNotNull(sqlSelect, "SQL Select不应为null");
            assertTrue(sqlSelect.toLowerCase().contains("sum"), "应包含sum函数");
        }

        @Test
        @DisplayName("sum - 带别名的求和聚合函数")
        void testSumWithAlias() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .sum(MockUser::getAge, MockUser::getAge);

            String sqlSelect = query.getSqlSelect();
            assertNotNull(sqlSelect, "SQL Select不应为null");
            assertTrue(sqlSelect.toLowerCase().contains("sum"), "应包含sum函数");
            assertTrue(sqlSelect.toLowerCase().contains("as"), "应包含别名");
        }

        @Test
        @DisplayName("min - 最小值聚合函数")
        void testMin() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .min(MockUser::getAge);

            String sqlSelect = query.getSqlSelect();
            assertNotNull(sqlSelect, "SQL Select不应为null");
            assertTrue(sqlSelect.toLowerCase().contains("min"), "应包含min函数");
        }

        @Test
        @DisplayName("max - 最大值聚合函数")
        void testMax() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .max(MockUser::getAge);

            String sqlSelect = query.getSqlSelect();
            assertNotNull(sqlSelect, "SQL Select不应为null");
            assertTrue(sqlSelect.toLowerCase().contains("max"), "应包含max函数");
        }

        @Test
        @DisplayName("count - 计数聚合函数")
        void testCount() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .count(MockUser::getId);

            String sqlSelect = query.getSqlSelect();
            assertNotNull(sqlSelect, "SQL Select不应为null");
            assertTrue(sqlSelect.toLowerCase().contains("count"), "应包含count函数");
        }

        @Test
        @DisplayName("avg - 平均值聚合函数")
        void testAvg() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .avg(MockUser::getAge, MockUser::getAge);

            String sqlSelect = query.getSqlSelect();
            assertNotNull(sqlSelect, "SQL Select不应为null");
            assertTrue(sqlSelect.toLowerCase().contains("avg"), "应包含avg函数");
        }

        @Test
        @DisplayName("aggfunc - 通用聚合函数构建器")
        void testAggfunc() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .aggfunc("count", MockUser::getId, MockUser::getId);

            String sqlSelect = query.getSqlSelect();
            assertNotNull(sqlSelect, "SQL Select不应为null");
            assertTrue(sqlSelect.toLowerCase().contains("count"), "应包含count函数");
        }
    }

    // ==================== 链式调用测试 ====================

    @Nested
    @DisplayName("链式调用测试")
    class ChainCallTests {

        @Test
        @DisplayName("链式调用 - 多条件组合")
        void testChainedConditions() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .eq(MockUser::getStatus, "1")
                .like(MockUser::getUserName, "张")
                .ge(MockUser::getAge, 18)
                .le(MockUser::getAge, 60);

            String sql = query.getSqlSegment();
            assertNotNull(sql, "SQL片段不应为null");
            assertTrue(sql.contains("status"), "应包含status条件");
            assertTrue(sql.contains("user_name"), "应包含user_name条件");
            assertTrue(sql.contains("age"), "应包含age条件");
        }

        @Test
        @DisplayName("链式调用 - 混合有效和无效值")
        void testChainedWithMixedValues() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .eq(MockUser::getStatus, "1")      // 有效
                .eq(MockUser::getUserName, null)   // 无效,应跳过
                .eq(MockUser::getEmail, "")        // 无效,应跳过
                .ge(MockUser::getAge, 18);         // 有效

            String sql = query.getSqlSegment();
            assertNotNull(sql, "SQL片段不应为null");
            assertTrue(sql.contains("status"), "应包含status条件");
            assertTrue(sql.contains("age"), "应包含age条件");
            // user_name 和 email 因为无效值被跳过
        }

        @Test
        @DisplayName("链式调用 - 全部无效值")
        void testChainedWithAllInvalidValues() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .eq(MockUser::getStatus, null)
                .eq(MockUser::getUserName, "")
                .eq(MockUser::getEmail, "   ");

            String sql = query.getSqlSegment();
            assertTrue(sql == null || sql.isEmpty(), "全部无效值不应生成条件");
        }
    }

    // ==================== toQuery转换测试 ====================

    @Nested
    @DisplayName("toQuery转换测试")
    class ToQueryTests {

        @Test
        @DisplayName("toQuery - 转换为PlusQuery")
        void testToQuery() {
            PlusLambdaQuery<MockUser> lambdaQuery = PlusLambdaQuery.of(MockUser.class)
                .eq(MockUser::getStatus, "1");

            var plusQuery = lambdaQuery.toQuery();
            assertNotNull(plusQuery, "转换后的PlusQuery不应为null");
        }
    }

    // ==================== clear方法测试 ====================

    @Nested
    @DisplayName("clear方法测试")
    class ClearTests {

        @Test
        @DisplayName("clear - 清除查询条件")
        void testClear() {
            PlusLambdaQuery<MockUser> query = PlusLambdaQuery.of(MockUser.class)
                .eq(MockUser::getStatus, "1")
                .like(MockUser::getUserName, "张");

            // 验证有条件
            assertNotNull(query.getSqlSegment(), "清除前应有SQL片段");

            // 清除
            query.clear();

            // 验证已清除
            String sqlAfterClear = query.getSqlSegment();
            assertTrue(sqlAfterClear == null || sqlAfterClear.isEmpty(), "清除后不应有SQL片段");
        }
    }
}
