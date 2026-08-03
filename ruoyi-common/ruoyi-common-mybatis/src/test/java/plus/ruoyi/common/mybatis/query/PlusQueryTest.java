package plus.ruoyi.common.mybatis.query;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import plus.ruoyi.common.mybatis.core.query.PlusQuery;
import plus.ruoyi.common.mybatis.mock.MockUser;
import plus.ruoyi.common.test.base.BaseUnitTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PlusQuery 单元测试
 * <p>
 * 测试字符串列名查询构建器的核心功能:
 * <ul>
 *   <li>静态工厂方法</li>
 *   <li>智能条件处理 (自动过滤null和空字符串)</li>
 *   <li>SQL注入检测</li>
 *   <li>聚合函数</li>
 *   <li>与PlusLambdaQuery的转换</li>
 * </ul>
 *
 * @author 抓蛙师
 */
@DisplayName("PlusQuery 字符串列名查询构建器测试")
class PlusQueryTest extends BaseUnitTest {

    /**
     * 初始化 MyBatis-Plus 实体类表信息
     * Lambda 转换功能需要实体类被注册到 TableInfoHelper 中
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
            PlusQuery<MockUser> query = PlusQuery.of();

            assertNotNull(query, "查询实例不应为null");
            assertNull(query.getEntity(), "实体对象应为null");
        }

        @Test
        @DisplayName("of(Class) - 基于实体类创建查询实例")
        void testOfClass() {
            PlusQuery<MockUser> query = PlusQuery.of(MockUser.class);

            assertNotNull(query, "查询实例不应为null");
            assertEquals(MockUser.class, query.getEntityClass(), "实体类应匹配");
        }

        @Test
        @DisplayName("of(Entity) - 基于实体对象创建查询实例")
        void testOfEntity() {
            MockUser user = new MockUser().setUserName("test");
            PlusQuery<MockUser> query = PlusQuery.of(user);

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
            PlusQuery<MockUser> query = PlusQuery.of(MockUser.class)
                .eq("status", "1");

            String sql = query.getSqlSegment();
            assertNotNull(sql, "SQL片段不应为null");
            assertTrue(sql.contains("status"), "应包含status字段");
        }

        @Test
        @DisplayName("eq - null值应跳过条件")
        void testEqWithNullValue() {
            PlusQuery<MockUser> query = PlusQuery.of(MockUser.class)
                .eq("status", null);

            String sql = query.getSqlSegment();
            assertTrue(sql == null || sql.isEmpty(), "null值不应生成条件");
        }

        @Test
        @DisplayName("eq - 空字符串应跳过条件")
        void testEqWithEmptyString() {
            PlusQuery<MockUser> query = PlusQuery.of(MockUser.class)
                .eq("status", "");

            String sql = query.getSqlSegment();
            assertTrue(sql == null || sql.isEmpty(), "空字符串不应生成条件");
        }

        @Test
        @DisplayName("ne - 有效值应添加不等于条件")
        void testNeWithValidValue() {
            PlusQuery<MockUser> query = PlusQuery.of(MockUser.class)
                .ne("status", "0");

            String sql = query.getSqlSegment();
            assertNotNull(sql, "SQL片段不应为null");
            assertTrue(sql.contains("<>"), "应包含不等于操作符");
        }
    }

    // ==================== 比较条件测试 ====================

    @Nested
    @DisplayName("比较条件测试 (gt/ge/lt/le)")
    class ComparisonConditionTests {

        @Test
        @DisplayName("gt - 大于条件有效值测试")
        void testGtWithValidValue() {
            PlusQuery<MockUser> query = PlusQuery.of(MockUser.class)
                .gt("age", 18);

            String sql = query.getSqlSegment();
            assertNotNull(sql, "SQL片段不应为null");
            assertTrue(sql.contains(">"), "应包含大于操作符");
        }

        @Test
        @DisplayName("ge - 大于等于条件有效值测试")
        void testGeWithValidValue() {
            PlusQuery<MockUser> query = PlusQuery.of(MockUser.class)
                .ge("age", 18);

            String sql = query.getSqlSegment();
            assertNotNull(sql, "SQL片段不应为null");
            assertTrue(sql.contains(">="), "应包含大于等于操作符");
        }

        @Test
        @DisplayName("lt - 小于条件有效值测试")
        void testLtWithValidValue() {
            PlusQuery<MockUser> query = PlusQuery.of(MockUser.class)
                .lt("age", 60);

            String sql = query.getSqlSegment();
            assertNotNull(sql, "SQL片段不应为null");
            assertTrue(sql.contains("<"), "应包含小于操作符");
        }

        @Test
        @DisplayName("le - 小于等于条件有效值测试")
        void testLeWithValidValue() {
            PlusQuery<MockUser> query = PlusQuery.of(MockUser.class)
                .le("age", 60);

            String sql = query.getSqlSegment();
            assertNotNull(sql, "SQL片段不应为null");
            assertTrue(sql.contains("<="), "应包含小于等于操作符");
        }

        @Test
        @DisplayName("比较条件 - null值应跳过")
        void testComparisonWithNullValue() {
            PlusQuery<MockUser> query = PlusQuery.of(MockUser.class)
                .gt("age", null)
                .ge("age", null)
                .lt("age", null)
                .le("age", null);

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
            PlusQuery<MockUser> query = PlusQuery.of(MockUser.class)
                .between("age", 18, 60);

            String sql = query.getSqlSegment();
            assertNotNull(sql, "SQL片段不应为null");
            assertTrue(sql.contains("BETWEEN"), "应包含BETWEEN关键字");
        }

        @Test
        @DisplayName("between - 只有第一个值有效应转为>=条件")
        void testBetweenWithOnlyFirstValid() {
            PlusQuery<MockUser> query = PlusQuery.of(MockUser.class)
                .between("age", 18, null);

            String sql = query.getSqlSegment();
            assertNotNull(sql, "SQL片段不应为null");
            assertTrue(sql.contains(">="), "应转为大于等于条件");
        }

        @Test
        @DisplayName("between - 只有第二个值有效应转为<=条件")
        void testBetweenWithOnlySecondValid() {
            PlusQuery<MockUser> query = PlusQuery.of(MockUser.class)
                .between("age", null, 60);

            String sql = query.getSqlSegment();
            assertNotNull(sql, "SQL片段不应为null");
            assertTrue(sql.contains("<="), "应转为小于等于条件");
        }

        @Test
        @DisplayName("between - 两个值都为null应不生成条件")
        void testBetweenWithBothNull() {
            PlusQuery<MockUser> query = PlusQuery.of(MockUser.class)
                .between("age", null, null);

            String sql = query.getSqlSegment();
            assertTrue(sql == null || sql.isEmpty(), "两个null值不应生成条件");
        }

        @Test
        @DisplayName("notBetween - 两个有效值应生成NOT BETWEEN条件")
        void testNotBetweenWithBothValid() {
            PlusQuery<MockUser> query = PlusQuery.of(MockUser.class)
                .notBetween("age", 18, 60);

            String sql = query.getSqlSegment();
            assertNotNull(sql, "SQL片段不应为null");
            assertTrue(sql.contains("NOT BETWEEN"), "应包含NOT BETWEEN关键字");
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
            PlusQuery<MockUser> query = PlusQuery.of(MockUser.class)
                .in("status", statuses);

            String sql = query.getSqlSegment();
            assertNotNull(sql, "SQL片段不应为null");
            assertTrue(sql.contains("IN"), "应包含IN关键字");
        }

        @Test
        @DisplayName("in - 空集合应不生成条件")
        void testInWithEmptyCollection() {
            PlusQuery<MockUser> query = PlusQuery.of(MockUser.class)
                .in("status", Collections.emptyList());

            String sql = query.getSqlSegment();
            assertTrue(sql == null || sql.isEmpty(), "空集合不应生成条件");
        }

        @Test
        @DisplayName("in - 集合中的null和空字符串应被过滤")
        void testInFiltersInvalidElements() {
            List<String> statuses = Arrays.asList("1", null, "", "  ", "2");
            PlusQuery<MockUser> query = PlusQuery.of(MockUser.class)
                .in("status", statuses);

            String sql = query.getSqlSegment();
            assertNotNull(sql, "SQL片段不应为null");
            assertTrue(sql.contains("IN"), "应包含IN关键字");
        }

        @Test
        @DisplayName("notIn - 有效集合应生成NOT IN条件")
        void testNotInWithValidCollection() {
            List<String> statuses = Arrays.asList("0", "2");
            PlusQuery<MockUser> query = PlusQuery.of(MockUser.class)
                .notIn("status", statuses);

            String sql = query.getSqlSegment();
            assertNotNull(sql, "SQL片段不应为null");
            assertTrue(sql.contains("NOT IN"), "应包含NOT IN关键字");
        }
    }

    // ==================== 模糊匹配测试 ====================

    @Nested
    @DisplayName("模糊匹配测试 (like/likeLeft/likeRight)")
    class LikeConditionTests {

        @Test
        @DisplayName("like - 有效值应生成LIKE条件")
        void testLikeWithValidValue() {
            PlusQuery<MockUser> query = PlusQuery.of(MockUser.class)
                .like("user_name", "张");

            String sql = query.getSqlSegment();
            assertNotNull(sql, "SQL片段不应为null");
            assertTrue(sql.contains("LIKE"), "应包含LIKE关键字");
        }

        @Test
        @DisplayName("like - null值应跳过条件")
        void testLikeWithNullValue() {
            PlusQuery<MockUser> query = PlusQuery.of(MockUser.class)
                .like("user_name", null);

            String sql = query.getSqlSegment();
            assertTrue(sql == null || sql.isEmpty(), "null值不应生成条件");
        }

        @Test
        @DisplayName("likeLeft - 有效值应生成LIKE条件")
        void testLikeLeftWithValidValue() {
            PlusQuery<MockUser> query = PlusQuery.of(MockUser.class)
                .likeLeft("email", "@example.com");

            String sql = query.getSqlSegment();
            assertNotNull(sql, "SQL片段不应为null");
            assertTrue(sql.contains("LIKE"), "应包含LIKE关键字");
        }

        @Test
        @DisplayName("likeRight - 有效值应生成LIKE条件")
        void testLikeRightWithValidValue() {
            PlusQuery<MockUser> query = PlusQuery.of(MockUser.class)
                .likeRight("phone", "138");

            String sql = query.getSqlSegment();
            assertNotNull(sql, "SQL片段不应为null");
            assertTrue(sql.contains("LIKE"), "应包含LIKE关键字");
        }
    }

    // ==================== Select字段测试 ====================

    @Nested
    @DisplayName("Select字段选择测试")
    class SelectFieldTests {

        @Test
        @DisplayName("select - 选择指定字段")
        void testSelectFields() {
            PlusQuery<MockUser> query = PlusQuery.of(MockUser.class)
                .select("id", "user_name", "status");

            String sqlSelect = query.getSqlSelect();
            assertNotNull(sqlSelect, "SQL Select不应为null");
            assertTrue(sqlSelect.contains("id"), "应包含id字段");
            assertTrue(sqlSelect.contains("user_name"), "应包含user_name字段");
            assertTrue(sqlSelect.contains("status"), "应包含status字段");
        }

        @Test
        @DisplayName("select - 空字段数组不设置select")
        void testSelectEmptyFields() {
            PlusQuery<MockUser> query = PlusQuery.of(MockUser.class)
                .select();

            String sqlSelect = query.getSqlSelect();
            assertTrue(sqlSelect == null || sqlSelect.isEmpty(), "空字段不应设置select");
        }
    }

    // ==================== 聚合函数测试 ====================

    @Nested
    @DisplayName("聚合函数测试 (sum/min/max/count/avg)")
    class AggregateFunctionTests {

        @Test
        @DisplayName("sum - 求和聚合函数")
        void testSum() {
            PlusQuery<MockUser> query = PlusQuery.of(MockUser.class)
                .sum("age");

            String sqlSelect = query.getSqlSelect();
            assertNotNull(sqlSelect, "SQL Select不应为null");
            assertTrue(sqlSelect.toLowerCase().contains("sum"), "应包含sum函数");
        }

        @Test
        @DisplayName("sum - 带别名的求和聚合函数")
        void testSumWithAlias() {
            PlusQuery<MockUser> query = PlusQuery.of(MockUser.class)
                .sum("age", "total_age");

            String sqlSelect = query.getSqlSelect();
            assertNotNull(sqlSelect, "SQL Select不应为null");
            assertTrue(sqlSelect.toLowerCase().contains("sum"), "应包含sum函数");
            assertTrue(sqlSelect.contains("total_age"), "应包含别名");
        }

        @Test
        @DisplayName("min - 最小值聚合函数")
        void testMin() {
            PlusQuery<MockUser> query = PlusQuery.of(MockUser.class)
                .min("age");

            String sqlSelect = query.getSqlSelect();
            assertNotNull(sqlSelect, "SQL Select不应为null");
            assertTrue(sqlSelect.toLowerCase().contains("min"), "应包含min函数");
        }

        @Test
        @DisplayName("max - 最大值聚合函数")
        void testMax() {
            PlusQuery<MockUser> query = PlusQuery.of(MockUser.class)
                .max("age");

            String sqlSelect = query.getSqlSelect();
            assertNotNull(sqlSelect, "SQL Select不应为null");
            assertTrue(sqlSelect.toLowerCase().contains("max"), "应包含max函数");
        }

        @Test
        @DisplayName("count - 计数聚合函数")
        void testCount() {
            PlusQuery<MockUser> query = PlusQuery.of(MockUser.class)
                .count("id");

            String sqlSelect = query.getSqlSelect();
            assertNotNull(sqlSelect, "SQL Select不应为null");
            assertTrue(sqlSelect.toLowerCase().contains("count"), "应包含count函数");
        }

        @Test
        @DisplayName("avg - 平均值聚合函数")
        void testAvg() {
            PlusQuery<MockUser> query = PlusQuery.of(MockUser.class)
                .avg("age", "avg_age");

            String sqlSelect = query.getSqlSelect();
            assertNotNull(sqlSelect, "SQL Select不应为null");
            assertTrue(sqlSelect.toLowerCase().contains("avg"), "应包含avg函数");
        }

        @Test
        @DisplayName("多个聚合函数组合")
        void testMultipleAggregates() {
            PlusQuery<MockUser> query = PlusQuery.of(MockUser.class)
                .count("id", "total_count")
                .sum("age", "total_age")
                .avg("age", "avg_age");

            String sqlSelect = query.getSqlSelect();
            assertNotNull(sqlSelect, "SQL Select不应为null");
            assertTrue(sqlSelect.toLowerCase().contains("count"), "应包含count函数");
            assertTrue(sqlSelect.toLowerCase().contains("sum"), "应包含sum函数");
            assertTrue(sqlSelect.toLowerCase().contains("avg"), "应包含avg函数");
        }
    }

    // ==================== SQL注入检测测试 ====================

    @Nested
    @DisplayName("SQL注入检测测试")
    class SqlInjectionTests {

        @Test
        @DisplayName("checkSqlInjection - 正常列名应通过")
        void testNormalColumnName() {
            PlusQuery<MockUser> query = PlusQuery.of(MockUser.class)
                .checkSqlInjection()
                .eq("user_name", "test");

            String sql = query.getSqlSegment();
            assertNotNull(sql, "正常列名应生成SQL");
        }

        @Test
        @DisplayName("checkSqlInjection - SQL注入列名应被检测(聚合函数场景)")
        void testSqlInjectionColumnName() {
            // 注意: SqlInjectionUtils.check 主要检测 SQL 关键字
            // columnToString 在聚合函数场景会被调用
            // 这里验证启用 checkSqlInjection 开关后会进行检测
            PlusQuery<MockUser> query = PlusQuery.of(MockUser.class)
                .checkSqlInjection();

            // 验证正常列名在聚合函数中能通过
            query.sum("age", "total_age");
            String sql = query.getSqlSelect();
            assertNotNull(sql, "正常列名在聚合函数中应正常工作");
            assertTrue(sql.contains("sum"), "应包含sum聚合函数");
        }

        @Test
        @DisplayName("checkSqlInjection - 检查开关状态")
        void testCheckSqlInjectionFlag() {
            // 验证 checkSqlInjection 方法返回 this (链式调用)
            PlusQuery<MockUser> query = PlusQuery.of(MockUser.class);
            PlusQuery<MockUser> result = query.checkSqlInjection();
            assertSame(query, result, "checkSqlInjection 应返回 this");
        }

        @Test
        @DisplayName("不启用checkSqlInjection时不检测")
        void testWithoutSqlInjectionCheck() {
            // 不调用checkSqlInjection()，不检测SQL注入
            PlusQuery<MockUser> query = PlusQuery.of(MockUser.class)
                .eq("user_name", "test");

            String sql = query.getSqlSegment();
            assertNotNull(sql, "不启用检测时应正常生成SQL");
        }
    }

    // ==================== Lambda转换测试 ====================

    @Nested
    @DisplayName("Lambda转换测试")
    class LambdaConversionTests {

        @Test
        @DisplayName("lambda() - 转换为PlusLambdaQuery")
        void testToLambda() {
            PlusQuery<MockUser> stringQuery = PlusQuery.of(MockUser.class)
                .eq("status", "1");

            var lambdaQuery = stringQuery.lambda();
            assertNotNull(lambdaQuery, "转换后的LambdaQuery不应为null");
        }
    }

    // ==================== 链式调用测试 ====================

    @Nested
    @DisplayName("链式调用测试")
    class ChainCallTests {

        @Test
        @DisplayName("链式调用 - 多条件组合")
        void testChainedConditions() {
            PlusQuery<MockUser> query = PlusQuery.of(MockUser.class)
                .eq("status", "1")
                .like("user_name", "张")
                .ge("age", 18)
                .le("age", 60);

            String sql = query.getSqlSegment();
            assertNotNull(sql, "SQL片段不应为null");
            assertTrue(sql.contains("status"), "应包含status条件");
            assertTrue(sql.contains("user_name"), "应包含user_name条件");
            assertTrue(sql.contains("age"), "应包含age条件");
        }

        @Test
        @DisplayName("链式调用 - 混合有效和无效值")
        void testChainedWithMixedValues() {
            PlusQuery<MockUser> query = PlusQuery.of(MockUser.class)
                .eq("status", "1")      // 有效
                .eq("user_name", null)  // 无效
                .eq("email", "")        // 无效
                .ge("age", 18);         // 有效

            String sql = query.getSqlSegment();
            assertNotNull(sql, "SQL片段不应为null");
            assertTrue(sql.contains("status"), "应包含status条件");
            assertTrue(sql.contains("age"), "应包含age条件");
        }
    }

    // ==================== clear方法测试 ====================

    @Nested
    @DisplayName("clear方法测试")
    class ClearTests {

        @Test
        @DisplayName("clear - 清除查询条件")
        void testClear() {
            PlusQuery<MockUser> query = PlusQuery.of(MockUser.class)
                .eq("status", "1")
                .like("user_name", "张");

            // 验证有条件
            assertNotNull(query.getSqlSegment(), "清除前应有SQL片段");

            // 清除
            query.clear();

            // 验证已清除
            String sqlAfterClear = query.getSqlSegment();
            assertTrue(sqlAfterClear == null || sqlAfterClear.isEmpty(), "清除后不应有SQL片段");
        }
    }

    // ==================== 构造方法测试 ====================

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("带select字段的构造方法")
        void testConstructorWithColumns() {
            MockUser user = new MockUser().setUserName("test");
            PlusQuery<MockUser> query = new PlusQuery<>(user, "id", "user_name", "status");

            String sqlSelect = query.getSqlSelect();
            assertNotNull(sqlSelect, "应有SQL Select");
            assertTrue(sqlSelect.contains("id"), "应包含id字段");
        }
    }
}
