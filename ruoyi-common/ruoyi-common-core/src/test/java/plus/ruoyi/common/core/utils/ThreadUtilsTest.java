package plus.ruoyi.common.core.utils;

import plus.ruoyi.common.test.base.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ThreadUtils 线程工具测试
 *
 * @author 抓蛙师
 */
@DisplayName("ThreadUtils线程工具测试")
public class ThreadUtilsTest extends BaseUnitTest {

    // ==================== shutdownGracefully 测试 ====================

    @Test
    @DisplayName("测试shutdownGracefully-正常关闭线程池")
    public void testShutdownGracefullyNormalCase() {
        // 创建线程池
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // 提交几个简单任务
        executor.submit(() -> ThreadUtils.sleep(100));
        executor.submit(() -> ThreadUtils.sleep(100));

        // 正常关闭
        ThreadUtils.shutdownGracefully(executor);

        // 验证线程池已关闭
        assertTrue(executor.isShutdown(), "线程池应该已关闭");
    }

    @Test
    @DisplayName("测试shutdownGracefully-null线程池不抛异常")
    public void testShutdownGracefullyNullExecutor() {
        // null线程池应该不抛异常
        assertDoesNotThrow(() -> ThreadUtils.shutdownGracefully(null));
    }

    @Test
    @DisplayName("测试shutdownGracefully-已关闭的线程池")
    public void testShutdownGracefullyAlreadyShutdown() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.shutdown();

        // 对已关闭的线程池调用应该正常
        assertDoesNotThrow(() -> ThreadUtils.shutdownGracefully(executor));
    }

    @Test
    @DisplayName("测试shutdownGracefully-自定义超时时间")
    public void testShutdownGracefullyCustomTimeout() {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // 提交任务
        executor.submit(() -> ThreadUtils.sleep(100));

        // 使用1秒超时关闭
        ThreadUtils.shutdownGracefully(executor, 1);

        assertTrue(executor.isShutdown());
    }

    @Test
    @DisplayName("测试shutdownGracefully-快速完成的任务")
    public void testShutdownGracefullyFastTasks() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        AtomicInteger counter = new AtomicInteger(0);

        // 提交10个快速任务
        for (int i = 0; i < 10; i++) {
            executor.submit(() -> counter.incrementAndGet());
        }

        // 关闭线程池
        ThreadUtils.shutdownGracefully(executor, 5);

        // 验证所有任务都完成了
        assertTrue(executor.isTerminated() || executor.isShutdown());
    }

    @Test
    @DisplayName("测试shutdownGracefully-长时间运行的任务")
    public void testShutdownGracefullyLongRunningTasks() {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // 提交一个长时间运行的任务
        executor.submit(() -> {
            try {
                Thread.sleep(10000); // 10秒
            } catch (InterruptedException e) {
                // 被中断是正常的
                Thread.currentThread().interrupt();
            }
        });

        long startTime = System.currentTimeMillis();

        // 使用短超时关闭（应该会触发shutdownNow）
        ThreadUtils.shutdownGracefully(executor, 1);

        long duration = System.currentTimeMillis() - startTime;

        // 验证在合理时间内完成（不会等待10秒）
        assertTrue(duration < 5000, "应该在超时后强制关闭，不会等待太久");
        assertTrue(executor.isShutdown());
    }

    // ==================== logException 测试 ====================

    @Test
    @DisplayName("测试logException-普通Runnable无异常")
    public void testLogExceptionNormalRunnable() {
        Runnable task = () -> System.out.println("测试任务");

        // 不应该抛出异常
        assertDoesNotThrow(() -> ThreadUtils.logException(task, null));
    }

    @Test
    @DisplayName("测试logException-普通Runnable有异常")
    public void testLogExceptionRunnableWithException() {
        Runnable task = () -> System.out.println("测试任务");
        Throwable error = new RuntimeException("测试异常");

        // 不应该抛出异常（只记录日志）
        assertDoesNotThrow(() -> ThreadUtils.logException(task, error));
    }

    @Test
    @DisplayName("测试logException-Future任务成功完成")
    public void testLogExceptionFutureSuccess() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<String> future = executor.submit(() -> "成功");
        future.get(); // 等待完成

        // Future实现了Runnable接口，可以传递
        // 不应该记录任何异常
        if (future instanceof Runnable) {
            assertDoesNotThrow(() -> ThreadUtils.logException((Runnable) future, null));
        }

        executor.shutdown();
    }

    @Test
    @DisplayName("测试logException-Future任务抛出异常")
    public void testLogExceptionFutureWithException() throws InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<?> future = executor.submit(() -> {
            throw new RuntimeException("Future内部异常");
        });

        // 等待任务完成
        ThreadUtils.sleep(200);

        // 应该能够提取并记录异常
        if (future instanceof Runnable) {
            assertDoesNotThrow(() -> ThreadUtils.logException((Runnable) future, null));
        }

        executor.shutdown();
    }

    @Test
    @DisplayName("测试logException-Future任务被取消")
    public void testLogExceptionFutureCancelled() {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<?> future = executor.submit(() -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // 取消任务
        future.cancel(true);

        // 等待取消生效
        ThreadUtils.sleep(100);

        // 应该能够处理CancellationException
        if (future instanceof Runnable) {
            assertDoesNotThrow(() -> ThreadUtils.logException((Runnable) future, null));
        }

        executor.shutdown();
    }

    // ==================== sleep 测试 ====================

    @Test
    @DisplayName("测试sleep-正常睡眠")
    public void testSleepNormalCase() {
        long startTime = System.currentTimeMillis();

        ThreadUtils.sleep(200);

        long duration = System.currentTimeMillis() - startTime;

        // 验证睡眠时间大致正确（允许一定误差）
        assertTrue(duration >= 180 && duration < 300,
            "睡眠时间应该在180-300ms之间，实际: " + duration);
    }

    @Test
    @DisplayName("测试sleep-零毫秒")
    public void testSleepZeroMilliseconds() {
        long startTime = System.currentTimeMillis();

        ThreadUtils.sleep(0);

        long duration = System.currentTimeMillis() - startTime;

        // 0毫秒应该立即返回
        assertTrue(duration < 50, "0毫秒睡眠应该立即返回");
    }

    @Test
    @DisplayName("测试sleep-负数毫秒")
    public void testSleepNegativeMilliseconds() {
        // 负数睡眠会抛出 IllegalArgumentException(Thread.sleep的原生行为)
        // ThreadUtils.sleep() 只捕获 InterruptedException,不捕获 IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> ThreadUtils.sleep(-100));
    }

    @Test
    @DisplayName("测试sleep-被中断")
    public void testSleepInterrupted() throws InterruptedException {
        AtomicBoolean wasInterrupted = new AtomicBoolean(false);

        Thread thread = new Thread(() -> {
            ThreadUtils.sleep(5000); // 睡眠5秒
            // 检查中断状态
            wasInterrupted.set(Thread.currentThread().isInterrupted());
        });

        thread.start();
        ThreadUtils.sleep(100); // 等待线程开始睡眠
        thread.interrupt(); // 中断线程
        thread.join(); // 等待线程结束

        // 验证中断状态被恢复
        assertTrue(wasInterrupted.get(), "中断状态应该被恢复");
    }

    // ==================== sleepSeconds 测试 ====================

    @Test
    @DisplayName("测试sleepSeconds-正常睡眠")
    public void testSleepSecondsNormalCase() {
        long startTime = System.currentTimeMillis();

        ThreadUtils.sleepSeconds(1);

        long duration = System.currentTimeMillis() - startTime;

        // 验证睡眠时间大致为1秒（允许误差）
        assertTrue(duration >= 950 && duration < 1200,
            "睡眠时间应该在950-1200ms之间，实际: " + duration);
    }

    @Test
    @DisplayName("测试sleepSeconds-零秒")
    public void testSleepSecondsZero() {
        long startTime = System.currentTimeMillis();

        ThreadUtils.sleepSeconds(0);

        long duration = System.currentTimeMillis() - startTime;

        assertTrue(duration < 50, "0秒睡眠应该立即返回");
    }

    // ==================== getCurrentThreadInfo 测试 ====================

    @Test
    @DisplayName("测试getCurrentThreadInfo-获取线程信息")
    public void testGetCurrentThreadInfo() {
        String info = ThreadUtils.getCurrentThreadInfo();

        assertNotNull(info);
        assertTrue(info.startsWith("Thread["), "应该以Thread[开头");
        assertTrue(info.contains(","), "应该包含逗号分隔符");
        assertTrue(info.endsWith("]"), "应该以]结尾");
    }

    @Test
    @DisplayName("测试getCurrentThreadInfo-包含线程名称")
    public void testGetCurrentThreadInfoContainsName() {
        String info = ThreadUtils.getCurrentThreadInfo();

        // 应该包含线程名称（通常是main或包含Test）
        assertTrue(info.length() > 10, "线程信息应该包含有效内容");
    }

    @Test
    @DisplayName("测试getCurrentThreadInfo-自定义线程名称")
    public void testGetCurrentThreadInfoCustomName() throws InterruptedException {
        AtomicBoolean success = new AtomicBoolean(false);

        Thread thread = new Thread(() -> {
            String info = ThreadUtils.getCurrentThreadInfo();
            success.set(info.contains("CustomThreadName"));
        }, "CustomThreadName");

        thread.start();
        thread.join();

        assertTrue(success.get(), "应该包含自定义线程名称");
    }

    // ==================== 集成测试 ====================

    @Test
    @DisplayName("测试集成场景-完整的线程池生命周期")
    public void testIntegrationCompleteLifecycle() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        CountDownLatch latch = new CountDownLatch(5);
        AtomicInteger counter = new AtomicInteger(0);

        // 提交多个任务
        for (int i = 0; i < 5; i++) {
            executor.submit(() -> {
                try {
                    counter.incrementAndGet();
                    ThreadUtils.sleep(100);
                } finally {
                    latch.countDown();
                }
            });
        }

        // 等待所有任务提交
        assertTrue(latch.await(5, TimeUnit.SECONDS), "所有任务应该完成");

        // 验证任务执行
        assertEquals(5, counter.get());

        // 优雅关闭
        ThreadUtils.shutdownGracefully(executor, 5);

        // 验证关闭
        assertTrue(executor.isShutdown());
    }

    @Test
    @DisplayName("测试集成场景-线程池异常处理")
    public void testIntegrationExceptionHandling() throws InterruptedException {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>()
        ) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                ThreadUtils.logException(r, t);
            }
        };

        // 提交会抛出异常的任务
        executor.submit(() -> {
            throw new RuntimeException("测试异常");
        });

        // 等待任务执行
        ThreadUtils.sleep(200);

        // 关闭线程池
        ThreadUtils.shutdownGracefully(executor, 1);

        assertTrue(executor.isShutdown());
    }

    @Test
    @DisplayName("测试边界情况-多次关闭同一个线程池")
    public void testBoundaryMultipleShutdown() {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // 多次关闭不应该抛异常
        ThreadUtils.shutdownGracefully(executor);
        ThreadUtils.shutdownGracefully(executor);
        ThreadUtils.shutdownGracefully(executor);

        assertTrue(executor.isShutdown());
    }

    @Test
    @DisplayName("测试边界情况-睡眠极短时间")
    public void testBoundaryVeryShortSleep() {
        long startTime = System.currentTimeMillis();

        ThreadUtils.sleep(1); // 1毫秒

        long duration = System.currentTimeMillis() - startTime;

        // 即使是1毫秒也应该正常执行
        assertTrue(duration < 100, "极短睡眠应该快速返回");
    }

    @Test
    @DisplayName("测试边界情况-并发获取线程信息")
    public void testBoundaryConcurrentGetThreadInfo() throws InterruptedException {
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ConcurrentHashMap<String, String> threadInfos = new ConcurrentHashMap<>();

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            new Thread(() -> {
                String info = ThreadUtils.getCurrentThreadInfo();
                threadInfos.put("thread-" + index, info);
                latch.countDown();
            }, "TestThread-" + i).start();
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals(threadCount, threadInfos.size());
    }
}
