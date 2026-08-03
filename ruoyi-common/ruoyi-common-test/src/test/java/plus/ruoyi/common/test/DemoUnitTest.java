package plus.ruoyi.common.test;

import plus.ruoyi.common.test.base.BaseUnitTest;
import org.junit.jupiter.api.*;

import java.util.concurrent.TimeUnit;

/**
 * 单元测试案例 - 展示 JUnit 5 各种注解的使用
 *
 * @author Lion Li
 */
@DisplayName("单元测试案例")
public class DemoUnitTest extends BaseUnitTest {

    @DisplayName("测试 @Test @DisplayName 注解")
    @Test
    public void testTest() {
        System.out.println("这是一个基础测试方法");
    }

    @Disabled
    @DisplayName("测试 @Disabled 注解")
    @Test
    public void testDisabled() {
        System.out.println("这个测试被禁用,不会执行");
    }

    @Timeout(value = 2L, unit = TimeUnit.SECONDS)
    @DisplayName("测试 @Timeout 注解")
    @Test
    public void testTimeout() throws InterruptedException {
        Thread.sleep(1000);
        System.out.println("测试超时控制(2秒内完成)");
    }


    @DisplayName("测试 @RepeatedTest 注解")
    @RepeatedTest(3)
    public void testRepeatedTest() {
        System.out.println(666);
    }

    @BeforeAll
    public static void testBeforeAll() {
        System.out.println("@BeforeAll ==================");
    }

    @BeforeEach
    public void testBeforeEach() {
        System.out.println("@BeforeEach ==================");
    }

    @AfterEach
    public void testAfterEach() {
        System.out.println("@AfterEach ==================");
    }

    @AfterAll
    public static void testAfterAll() {
        System.out.println("@AfterAll ==================");
    }

}
