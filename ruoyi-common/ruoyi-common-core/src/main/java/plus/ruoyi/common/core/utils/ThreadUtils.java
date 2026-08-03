package plus.ruoyi.common.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * 线程工具类
 * <p>提供线程池安全关闭、异常处理等线程相关的实用功能</p>
 * <p>主要用于简化线程池管理和异常处理，确保线程资源的正确释放</p>
 *
 * @author 抓蛙师
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ThreadUtils {

    /**
     * 默认等待超时时间（秒）
     */
    private static final int DEFAULT_TIMEOUT_SECONDS = 120;

    /**
     * 安全关闭线程池
     * <p>示例：{@code ThreadUtils.shutdownGracefully(executorService)}</p>
     * <p>关闭步骤：</p>
     * <ol>
     *     <li>调用shutdown()停止接收新任务，等待已存在任务完成</li>
     *     <li>等待120秒，如果超时则调用shutdownNow()强制中断</li>
     *     <li>再等待120秒，如果仍未关闭则记录警告日志</li>
     *     <li>处理线程中断异常，确保当前线程中断状态正确设置</li>
     * </ol>
     *
     * @param executorService 要关闭的线程池，可以为null
     */
    public static void shutdownGracefully(ExecutorService executorService) {
        shutdownGracefully(executorService, DEFAULT_TIMEOUT_SECONDS);
    }

    /**
     * 安全关闭线程池（自定义超时时间）
     * <p>示例：{@code ThreadUtils.shutdownGracefully(executorService, 60)}</p>
     *
     * @param executorService 要关闭的线程池，可以为null
     * @param timeoutSeconds  等待超时时间（秒）
     */
    public static void shutdownGracefully(ExecutorService executorService, int timeoutSeconds) {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                // 等待现有任务终止
                if (!executorService.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)) {
                    // 取消当前执行的任务
                    executorService.shutdownNow();
                    // 等待任务响应被取消
                    if (!executorService.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)) {
                        log.warn("线程池未能在{}秒内完全关闭", timeoutSeconds * 2);
                    }
                }
            } catch (InterruptedException ie) {
                // 当前线程也被中断时，强制关闭线程池
                executorService.shutdownNow();
                // 保持中断状态
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 打印线程执行异常信息
     * <p>示例：{@code ThreadUtils.logException(runnable, throwable)}</p>
     * <p>适用于ThreadPoolExecutor的afterExecute钩子方法中处理异常</p>
     * <p>能够正确提取Future任务中的异常信息并记录日志</p>
     *
     * @param runnable  执行的任务（可能是Future对象）
     * @param throwable 抛出的异常（可能为null）
     */
    public static void logException(Runnable runnable, Throwable throwable) {
        if (throwable == null && runnable instanceof Future<?> future) {
            try {
                // 尝试获取Future的执行结果以提取异常
                if (future.isDone()) {
                    future.get();
                }
            } catch (CancellationException ce) {
                throwable = ce;
            } catch (ExecutionException ee) {
                // 获取真正的异常原因
                throwable = ee.getCause();
            } catch (InterruptedException ie) {
                // 恢复中断状态
                Thread.currentThread().interrupt();
            }
        }

        if (throwable != null) {
            log.error("线程执行异常: {}", throwable.getMessage(), throwable);
        }
    }

    /**
     * 安全睡眠指定毫秒数
     * <p>示例：{@code ThreadUtils.sleep(1000)} // 睡眠1秒</p>
     * <p>自动处理InterruptedException，不会抛出异常</p>
     *
     * @param milliseconds 睡眠时间（毫秒）
     */
    public static void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            // 恢复中断状态
            Thread.currentThread().interrupt();
            log.debug("线程睡眠被中断");
        }
    }

    /**
     * 安全睡眠指定秒数
     * <p>示例：{@code ThreadUtils.sleepSeconds(5)} // 睡眠5秒</p>
     *
     * @param seconds 睡眠时间（秒）
     */
    public static void sleepSeconds(long seconds) {
        sleep(seconds * 1000);
    }

    /**
     * 获取当前线程信息字符串
     * <p>示例：{@code String info = ThreadUtils.getCurrentThreadInfo()}</p>
     * <p>返回格式："Thread[线程名, 优先级, 线程组]"</p>
     *
     * @return 当前线程的详细信息
     */
    public static String getCurrentThreadInfo() {
        Thread currentThread = Thread.currentThread();
        return String.format("Thread[%s, %d, %s]",
            currentThread.getName(),
            currentThread.getPriority(),
            currentThread.getThreadGroup().getName());
    }

}
