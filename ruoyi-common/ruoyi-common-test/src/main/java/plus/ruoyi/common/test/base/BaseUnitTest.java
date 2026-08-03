package plus.ruoyi.common.test.base;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import plus.ruoyi.common.test.config.TestConfig;

/**
 * 单元测试基类 - 不启动 Spring 容器
 * <p>
 * 适用于无需依赖注入的测试场景:
 * <ul>
 *   <li>工具类测试 (StringUtils, DateUtils 等)</li>
 *   <li>枚举类测试 (ModelProvider, MqttQoS 等)</li>
 *   <li>纯 POJO 测试 (DTO, VO 等)</li>
 *   <li>算法逻辑测试</li>
 * </ul>
 *
 * <p>性能优势:
 * <ul>
 *   <li>启动时间: < 1 秒 (无需启动 Spring)</li>
 *   <li>执行速度: 极快</li>
 *   <li>资源占用: 极低</li>
 * </ul>
 *
 * <p>功能特性:
 * <ul>
 *   <li>自动记录每个测试方法的执行时间</li>
 *   <li>超过性能阈值时输出警告日志</li>
 *   <li>自动管理测试临时目录</li>
 *   <li>提供setUp/tearDown扩展点</li>
 * </ul>
 *
 * <p>使用示例:
 * <pre>
 * {@code @DisplayName("StringUtils工具类测试")}
 * class StringUtilsTest extends BaseUnitTest {
 *
 *     {@code @Test}
 *     {@code @DisplayName("测试 isBlank")}
 *     void testIsBlank() {
 *         assertTrue(StringUtils.isBlank(null));
 *         assertTrue(StringUtils.isBlank(""));
 *     }
 *
 *     // 自定义性能阈值
 *     {@code @Override}
 *     protected long getPerformanceThreshold() {
 *         return 5000L; // 5秒
 *     }
 * }
 * </pre>
 *
 * @author 抓蛙师
 */
@Slf4j
public abstract class BaseUnitTest {

    /**
     * 测试开始时间 (线程隔离)
     */
    private final ThreadLocal<Long> startTimeHolder = new ThreadLocal<>();

    /**
     * 性能警告阈值(毫秒)
     * <p>默认3秒,子类可重写自定义阈值
     *
     * @return 阈值毫秒数
     */
    protected long getPerformanceThreshold() {
        return 3000L;
    }

    /**
     * 是否启用性能监控
     * <p>默认启用,子类可重写关闭监控
     *
     * @return true=启用, false=禁用
     */
    protected boolean isPerformanceMonitorEnabled() {
        return true;
    }

    /**
     * 是否启用测试目录管理
     * <p>默认启用,子类可重写关闭
     *
     * @return true=启用, false=禁用
     */
    protected boolean isTestDirManagementEnabled() {
        return true;
    }

    /**
     * 测试方法执行前
     * <p>自动启动计时器、初始化测试目录并调用setUp()
     *
     * @param testInfo 测试信息
     */
    @BeforeEach
    public final void baseSetUp(TestInfo testInfo) {
        // 初始化测试目录
        if (isTestDirManagementEnabled()) {
            TestConfig.initTestDirs();
        }

        if (isPerformanceMonitorEnabled()) {
            startTimeHolder.set(System.currentTimeMillis());
            log.debug("▶ 开始测试: {}", testInfo.getDisplayName());
        }

        setUp();
    }

    /**
     * 测试方法执行后
     * <p>自动记录执行时间、清理测试文件并调用tearDown()
     *
     * @param testInfo 测试信息
     */
    @AfterEach
    public final void baseTearDown(TestInfo testInfo) {
        tearDown();

        // 清理测试临时文件
        if (isTestDirManagementEnabled()) {
            TestConfig.cleanTestDirs();
        }

        if (isPerformanceMonitorEnabled()) {
            Long startTime = startTimeHolder.get();
            if (startTime != null) {
                long duration = System.currentTimeMillis() - startTime;
                String testName = testInfo.getDisplayName();

                // 性能警告判断
                if (duration > getPerformanceThreshold()) {
                    log.warn("⚠️ 性能警告: {} 执行时间 {}ms 超过阈值 {}ms",
                        testName, duration, getPerformanceThreshold());
                } else {
                    log.info("✓ {} 完成,耗时: {}ms", testName, duration);
                }

                startTimeHolder.remove();
            }
        }
    }

    /**
     * 测试前置处理
     * <p>子类可重写此方法实现自定义初始化逻辑
     */
    protected void setUp() {
        // 子类可重写
    }

    /**
     * 测试后置处理
     * <p>子类可重写此方法实现自定义清理逻辑
     */
    protected void tearDown() {
        // 子类可重写
    }
}
