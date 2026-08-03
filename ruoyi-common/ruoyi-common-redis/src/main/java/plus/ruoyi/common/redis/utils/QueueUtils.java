package plus.ruoyi.common.redis.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import plus.ruoyi.common.core.utils.SpringUtils;
import org.redisson.api.*;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 分布式队列工具类
 * <p>
 * 基于Redisson实现的分布式队列操作工具，支持多种队列类型：
 * - 普通阻塞队列：基础的FIFO队列
 * - 延迟队列：支持延迟消费的队列
 * - 优先队列：支持优先级排序的队列
 * - 有界队列：支持容量限制的队列
 * <p>
 * 注意：适用于轻量级队列场景，重量级数据请使用专业MQ
 * 要求Redis版本5.0以上
 *
 * @author Lion Li
 * @version 3.6.0 新增
 */
@SuppressWarnings("deprecation")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QueueUtils {

    private static final RedissonClient CLIENT = SpringUtils.getBean(RedissonClient.class);

    /**
     * 获取Redisson客户端实例
     *
     * @return RedissonClient实例
     */
    public static RedissonClient getClient() {
        return CLIENT;
    }

    // ==================== 普通阻塞队列 ====================

    /**
     * 添加数据到普通队列
     *
     * @param queueName 队列名称
     * @param data      待添加的数据
     * @return 添加成功返回true，失败返回false
     */
    public static <T> boolean addQueueObject(String queueName, T data) {
        RBlockingQueue<T> queue = CLIENT.getBlockingQueue(queueName);
        return queue.offer(data);
    }

    /**
     * 从普通队列获取数据（非阻塞）
     *
     * @param queueName 队列名称
     * @return 队列数据，无数据时返回null
     */
    public static <T> T getQueueObject(String queueName) {
        RBlockingQueue<T> queue = CLIENT.getBlockingQueue(queueName);
        return queue.poll();
    }

    /**
     * 从普通队列删除指定数据
     *
     * @param queueName 队列名称
     * @param data      待删除的数据
     * @return 删除成功返回true，失败返回false
     */
    public static <T> boolean removeQueueObject(String queueName, T data) {
        RBlockingQueue<T> queue = CLIENT.getBlockingQueue(queueName);
        return queue.remove(data);
    }

    /**
     * 销毁普通队列
     * 注意：销毁后所有阻塞监听会抛出异常
     *
     * @param queueName 队列名称
     * @return 销毁成功返回true，失败返回false
     */
    public static <T> boolean destroyQueue(String queueName) {
        RBlockingQueue<T> queue = CLIENT.getBlockingQueue(queueName);
        return queue.delete();
    }

    // ==================== 延迟队列 ====================

    /**
     * 添加数据到延迟队列（默认毫秒单位）
     *
     * @param queueName 队列名称
     * @param data      待添加的数据
     * @param time      延迟时间（毫秒）
     */
    public static <T> void addDelayedQueueObject(String queueName, T data, long time) {
        addDelayedQueueObject(queueName, data, time, TimeUnit.MILLISECONDS);
    }

    /**
     * 添加数据到延迟队列
     *
     * @param queueName 队列名称
     * @param data      待添加的数据
     * @param time      延迟时间
     * @param timeUnit  时间单位
     */
    public static <T> void addDelayedQueueObject(String queueName, T data, long time, TimeUnit timeUnit) {
        RBlockingQueue<T> queue = CLIENT.getBlockingQueue(queueName);
        RDelayedQueue<T> delayedQueue = CLIENT.getDelayedQueue(queue);
        delayedQueue.offer(data, time, timeUnit);
    }

    /**
     * 从延迟队列获取数据（非阻塞）
     *
     * @param queueName 队列名称
     * @return 已到期的队列数据，无数据时返回null
     */
    public static <T> T getDelayedQueueObject(String queueName) {
        RBlockingQueue<T> queue = CLIENT.getBlockingQueue(queueName);
        RDelayedQueue<T> delayedQueue = CLIENT.getDelayedQueue(queue);
        return delayedQueue.poll();
    }

    /**
     * 从延迟队列删除指定数据
     *
     * @param queueName 队列名称
     * @param data      待删除的数据
     * @return 删除成功返回true，失败返回false
     */
    public static <T> boolean removeDelayedQueueObject(String queueName, T data) {
        RBlockingQueue<T> queue = CLIENT.getBlockingQueue(queueName);
        RDelayedQueue<T> delayedQueue = CLIENT.getDelayedQueue(queue);
        return delayedQueue.remove(data);
    }

    /**
     * 销毁延迟队列
     * 注意：销毁后所有阻塞监听会抛出异常
     *
     * @param queueName 队列名称
     */
    public static <T> void destroyDelayedQueue(String queueName) {
        RBlockingQueue<T> queue = CLIENT.getBlockingQueue(queueName);
        RDelayedQueue<T> delayedQueue = CLIENT.getDelayedQueue(queue);
        delayedQueue.destroy();
    }

    // ==================== 优先队列 ====================

    /**
     * 添加数据到优先队列
     * 注意：数据需要实现Comparable接口或提供Comparator
     *
     * @param queueName 队列名称
     * @param data      待添加的数据
     * @return 添加成功返回true，失败返回false
     */
    public static <T> boolean addPriorityQueueObject(String queueName, T data) {
        RPriorityBlockingQueue<T> priorityBlockingQueue = CLIENT.getPriorityBlockingQueue(queueName);
        return priorityBlockingQueue.offer(data);
    }

    /**
     * 从优先队列获取数据（按优先级顺序）
     *
     * @param queueName 队列名称
     * @return 优先级最高的队列数据，无数据时返回null
     */
    public static <T> T getPriorityQueueObject(String queueName) {
        RPriorityBlockingQueue<T> queue = CLIENT.getPriorityBlockingQueue(queueName);
        return queue.poll();
    }

    /**
     * 从优先队列删除指定数据
     *
     * @param queueName 队列名称
     * @param data      待删除的数据
     * @return 删除成功返回true，失败返回false
     */
    public static <T> boolean removePriorityQueueObject(String queueName, T data) {
        RPriorityBlockingQueue<T> queue = CLIENT.getPriorityBlockingQueue(queueName);
        return queue.remove(data);
    }

    /**
     * 销毁优先队列
     * 注意：销毁后所有阻塞监听会抛出异常
     *
     * @param queueName 队列名称
     * @return 销毁成功返回true，失败返回false
     */
    public static <T> boolean destroyPriorityQueue(String queueName) {
        RPriorityBlockingQueue<T> queue = CLIENT.getPriorityBlockingQueue(queueName);
        return queue.delete();
    }

    // ==================== 有界队列 ====================

    /**
     * 设置有界队列容量
     *
     * @param queueName 队列名称
     * @param capacity  队列容量
     * @return 设置成功返回true，失败返回false
     */
    public static <T> boolean trySetBoundedQueueCapacity(String queueName, int capacity) {
        RBoundedBlockingQueue<T> boundedBlockingQueue = CLIENT.getBoundedBlockingQueue(queueName);
        return boundedBlockingQueue.trySetCapacity(capacity);
    }

    /**
     * 设置有界队列容量
     *
     * @param queueName 队列名称
     * @param capacity  队列容量
     * @param destroy   是否先销毁现有队列
     * @return 设置成功返回true，失败返回false
     */
    public static <T> boolean trySetBoundedQueueCapacity(String queueName, int capacity, boolean destroy) {
        RBoundedBlockingQueue<T> boundedBlockingQueue = CLIENT.getBoundedBlockingQueue(queueName);
        if (destroy) {
            boundedBlockingQueue.delete();
        }
        return boundedBlockingQueue.trySetCapacity(capacity);
    }

    /**
     * 添加数据到有界队列
     *
     * @param queueName 队列名称
     * @param data      待添加的数据
     * @return 添加成功返回true，队列已满返回false
     */
    public static <T> boolean addBoundedQueueObject(String queueName, T data) {
        RBoundedBlockingQueue<T> boundedBlockingQueue = CLIENT.getBoundedBlockingQueue(queueName);
        return boundedBlockingQueue.offer(data);
    }

    /**
     * 从有界队列获取数据
     *
     * @param queueName 队列名称
     * @return 队列数据，无数据时返回null
     */
    public static <T> T getBoundedQueueObject(String queueName) {
        RBoundedBlockingQueue<T> queue = CLIENT.getBoundedBlockingQueue(queueName);
        return queue.poll();
    }

    /**
     * 从有界队列删除指定数据
     *
     * @param queueName 队列名称
     * @param data      待删除的数据
     * @return 删除成功返回true，失败返回false
     */
    public static <T> boolean removeBoundedQueueObject(String queueName, T data) {
        RBoundedBlockingQueue<T> queue = CLIENT.getBoundedBlockingQueue(queueName);
        return queue.remove(data);
    }

    /**
     * 销毁有界队列
     * 注意：销毁后所有阻塞监听会抛出异常
     *
     * @param queueName 队列名称
     * @return 销毁成功返回true，失败返回false
     */
    public static <T> boolean destroyBoundedQueue(String queueName) {
        RBoundedBlockingQueue<T> queue = CLIENT.getBoundedBlockingQueue(queueName);
        return queue.delete();
    }

    // ==================== 队列订阅 ====================

    /**
     * 订阅阻塞队列消息
     * 支持所有队列类型：普通队列、延迟队列、优先队列、有界队列等
     *
     * @param queueName 队列名称
     * @param consumer  消息消费函数
     * @param isDelayed 是否为延迟队列
     */
    public static <T> void subscribeBlockingQueue(String queueName, Function<T, CompletionStage<Void>> consumer, boolean isDelayed) {
        RBlockingQueue<T> queue = CLIENT.getBlockingQueue(queueName);
        if (isDelayed) {
            // 订阅延迟队列需要先获取延迟队列实例
            CLIENT.getDelayedQueue(queue);
        }
        queue.subscribeOnElements(consumer);
    }

}
