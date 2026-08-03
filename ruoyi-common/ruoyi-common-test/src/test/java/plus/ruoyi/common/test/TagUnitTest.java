package plus.ruoyi.common.test;

import plus.ruoyi.common.test.base.BaseUnitTest;
import org.junit.jupiter.api.*;

/**
 * 标签单元测试案例 - 展示 JUnit 5 @Tag 注解的使用
 * <p>
 * @Tag 用于标记测试类或测试方法,可以根据标签选择性运行测试
 * <p>
 * 运行示例:
 * - 运行特定标签: mvn test -Dgroups="dev"
 * - 排除特定标签: mvn test -DexcludedGroups="exclude"
 *
 * @author Lion Li
 */
@DisplayName("标签单元测试案例")
public class TagUnitTest extends BaseUnitTest {

    @Tag("dev")
    @DisplayName("测试 @Tag dev")
    @Test
    public void testTagDev() {
        System.out.println("dev");
    }

    @Tag("prod")
    @DisplayName("测试 @Tag prod")
    @Test
    public void testTagProd() {
        System.out.println("prod");
    }

    @Tag("local")
    @DisplayName("测试 @Tag local")
    @Test
    public void testTagLocal() {
        System.out.println("local");
    }

    @Tag("exclude")
    @DisplayName("测试 @Tag exclude")
    @Test
    public void testTagExclude() {
        System.out.println("exclude");
    }

    @BeforeEach
    public void testBeforeEach() {
        System.out.println("@BeforeEach ==================");
    }

    @AfterEach
    public void testAfterEach() {
        System.out.println("@AfterEach ==================");
    }


}
