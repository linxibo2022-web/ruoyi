package plus.ruoyi.common.redis.utils;

import plus.ruoyi.common.test.base.BaseSpringTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * QueueUtils 分布式队列工具类测试
 *
 * 需要启动 Spring 容器和 Redis 连接
 *
 * @author 抓蛙师
 */
@DisplayName("QueueUtils分布式队列工具类测试")
public class QueueUtilsTest extends BaseSpringTest {

    /**
     * 测试使用的队列名称前缀
     */
    private static final String TEST_QUEUE_PREFIX = "test:queue:";

    /**
     * 测试使用的队列名称列表，用于清理
     */
    private final List<String> testQueues = new ArrayList<>();

    @BeforeEach
    public void setUp() {
        testQueues.clear();
    }

    @AfterEach
    public void tearDown() {
        // 清理测试队列
        for (String queueName : testQueues) {
            try {
                QueueUtils.destroyQueue(queueName);
            } catch (Exception ignored) {
            }
            try {
                QueueUtils.destroyDelayedQueue(queueName);
            } catch (Exception ignored) {
            }
            try {
                QueueUtils.destroyPriorityQueue(queueName);
            } catch (Exception ignored) {
            }
            try {
                QueueUtils.destroyBoundedQueue(queueName);
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 生成测试队列名称并记录
     */
    private String genTestQueue(String suffix) {
        String queueName = TEST_QUEUE_PREFIX + suffix + ":" + System.currentTimeMillis();
        testQueues.add(queueName);
        return queueName;
    }

    // ==================== 普通阻塞队列测试 ====================

    @Test
    @DisplayName("测试addQueueObject/getQueueObject-基本入队出队")
    public void testBasicQueueOperations() {
        String queueName = genTestQueue("basic");

        // 入队
        boolean result1 = QueueUtils.addQueueObject(queueName, "item1");
        boolean result2 = QueueUtils.addQueueObject(queueName, "item2");
        boolean result3 = QueueUtils.addQueueObject(queueName, "item3");

        assertTrue(result1, "第一个入队应该成功");
        assertTrue(result2, "第二个入队应该成功");
        assertTrue(result3, "第三个入队应该成功");

        // 出队（FIFO顺序）
        assertEquals("item1", QueueUtils.getQueueObject(queueName), "第一个出队应该是item1");
        assertEquals("item2", QueueUtils.getQueueObject(queueName), "第二个出队应该是item2");
        assertEquals("item3", QueueUtils.getQueueObject(queueName), "第三个出队应该是item3");
    }

    @Test
    @DisplayName("测试getQueueObject-空队列返回null")
    public void testGetFromEmptyQueue() {
        String queueName = genTestQueue("empty");

        Object result = QueueUtils.getQueueObject(queueName);

        assertNull(result, "空队列出队应该返回null");
    }

    @Test
    @DisplayName("测试removeQueueObject-从队列删除指定元素")
    public void testRemoveQueueObject() {
        String queueName = genTestQueue("remove");

        QueueUtils.addQueueObject(queueName, "a");
        QueueUtils.addQueueObject(queueName, "b");
        QueueUtils.addQueueObject(queueName, "c");

        // 删除中间元素
        boolean removed = QueueUtils.removeQueueObject(queueName, "b");

        assertTrue(removed, "删除存在的元素应该返回true");

        // 验证剩余元素
        assertEquals("a", QueueUtils.getQueueObject(queueName));
        assertEquals("c", QueueUtils.getQueueObject(queueName));
        assertNull(QueueUtils.getQueueObject(queueName));
    }

    @Test
    @DisplayName("测试removeQueueObject-删除不存在的元素")
    public void testRemoveNonExistentFromQueue() {
        String queueName = genTestQueue("removenotexist");

        QueueUtils.addQueueObject(queueName, "a");

        boolean removed = QueueUtils.removeQueueObject(queueName, "notexist");

        assertFalse(removed, "删除不存在的元素应该返回false");
    }

    @Test
    @DisplayName("测试destroyQueue-销毁队列")
    public void testDestroyQueue() {
        String queueName = genTestQueue("destroy");

        QueueUtils.addQueueObject(queueName, "item1");
        QueueUtils.addQueueObject(queueName, "item2");

        boolean destroyed = QueueUtils.destroyQueue(queueName);

        assertTrue(destroyed, "销毁队列应该成功");
        assertNull(QueueUtils.getQueueObject(queueName), "销毁后队列应该为空");
    }

    @Test
    @DisplayName("测试队列-存储对象类型")
    public void testQueueWithObject() {
        String queueName = genTestQueue("object");

        TestQueueItem item1 = new TestQueueItem(1L, "消息1");
        TestQueueItem item2 = new TestQueueItem(2L, "消息2");

        QueueUtils.addQueueObject(queueName, item1);
        QueueUtils.addQueueObject(queueName, item2);

        TestQueueItem result1 = QueueUtils.getQueueObject(queueName);
        TestQueueItem result2 = QueueUtils.getQueueObject(queueName);

        assertNotNull(result1);
        assertEquals(1L, result1.getId());
        assertEquals("消息1", result1.getMessage());

        assertNotNull(result2);
        assertEquals(2L, result2.getId());
        assertEquals("消息2", result2.getMessage());
    }

    // ==================== 延迟队列测试 ====================

    @Test
    @DisplayName("测试addDelayedQueueObject-延迟队列基本功能")
    public void testDelayedQueue() throws InterruptedException {
        String queueName = genTestQueue("delayed");

        // 添加1秒延迟的消息
        QueueUtils.addDelayedQueueObject(queueName, "delayed_item", 1, TimeUnit.SECONDS);

        // 立即获取应该为空（还没到期）
        assertNull(QueueUtils.getQueueObject(queueName), "延迟期间获取应该为空");

        // 等待2秒后获取
        TimeUnit.SECONDS.sleep(2);
        assertEquals("delayed_item", QueueUtils.getQueueObject(queueName), "延迟到期后应该能获取到");
    }

    @Test
    @DisplayName("测试addDelayedQueueObject-毫秒单位延迟")
    public void testDelayedQueueMillis() throws InterruptedException {
        String queueName = genTestQueue("delayedmillis");

        // 添加500毫秒延迟的消息
        QueueUtils.addDelayedQueueObject(queueName, "quick_item", 500);

        // 立即获取应该为空
        assertNull(QueueUtils.getQueueObject(queueName));

        // 等待1秒后获取
        TimeUnit.MILLISECONDS.sleep(800);
        assertEquals("quick_item", QueueUtils.getQueueObject(queueName));
    }

    @Test
    @DisplayName("测试removeDelayedQueueObject-删除延迟队列元素")
    public void testRemoveDelayedQueueObject() {
        String queueName = genTestQueue("delayedremove");

        QueueUtils.addDelayedQueueObject(queueName, "item1", 10, TimeUnit.SECONDS);
        QueueUtils.addDelayedQueueObject(queueName, "item2", 10, TimeUnit.SECONDS);

        boolean removed = QueueUtils.removeDelayedQueueObject(queueName, "item1");

        assertTrue(removed, "删除延迟队列元素应该成功");
    }

    @Test
    @DisplayName("测试destroyDelayedQueue-销毁延迟队列")
    public void testDestroyDelayedQueue() {
        String queueName = genTestQueue("delayeddestroy");

        QueueUtils.addDelayedQueueObject(queueName, "item", 10, TimeUnit.SECONDS);

        // 销毁不会抛出异常
        assertDoesNotThrow(() -> QueueUtils.destroyDelayedQueue(queueName));
    }

    // ==================== 优先队列测试 ====================

    @Test
    @DisplayName("测试优先队列-按优先级出队")
    public void testPriorityQueue() {
        String queueName = genTestQueue("priority");

        // 添加不同优先级的元素（数字越小优先级越高）
        QueueUtils.addPriorityQueueObject(queueName, new PriorityItem(3, "低优先级"));
        QueueUtils.addPriorityQueueObject(queueName, new PriorityItem(1, "高优先级"));
        QueueUtils.addPriorityQueueObject(queueName, new PriorityItem(2, "中优先级"));

        // 出队应该按优先级顺序
        PriorityItem first = QueueUtils.getPriorityQueueObject(queueName);
        PriorityItem second = QueueUtils.getPriorityQueueObject(queueName);
        PriorityItem third = QueueUtils.getPriorityQueueObject(queueName);

        assertEquals(1, first.getPriority(), "第一个出队应该是优先级1");
        assertEquals(2, second.getPriority(), "第二个出队应该是优先级2");
        assertEquals(3, third.getPriority(), "第三个出队应该是优先级3");
    }

    @Test
    @DisplayName("测试removePriorityQueueObject-删除优先队列元素")
    public void testRemovePriorityQueueObject() {
        String queueName = genTestQueue("priorityremove");

        PriorityItem item1 = new PriorityItem(1, "item1");
        PriorityItem item2 = new PriorityItem(2, "item2");

        QueueUtils.addPriorityQueueObject(queueName, item1);
        QueueUtils.addPriorityQueueObject(queueName, item2);

        boolean removed = QueueUtils.removePriorityQueueObject(queueName, item1);

        assertTrue(removed, "删除优先队列元素应该成功");

        // 验证只剩item2
        PriorityItem remaining = QueueUtils.getPriorityQueueObject(queueName);
        assertEquals(2, remaining.getPriority());
    }

    @Test
    @DisplayName("测试destroyPriorityQueue-销毁优先队列")
    public void testDestroyPriorityQueue() {
        String queueName = genTestQueue("prioritydestroy");

        QueueUtils.addPriorityQueueObject(queueName, new PriorityItem(1, "item"));

        boolean destroyed = QueueUtils.destroyPriorityQueue(queueName);

        assertTrue(destroyed, "销毁优先队列应该成功");
    }

    // ==================== 有界队列测试 ====================

    @Test
    @DisplayName("测试有界队列-设置容量")
    public void testBoundedQueueCapacity() {
        String queueName = genTestQueue("bounded");

        // 设置容量为3
        boolean setResult = QueueUtils.trySetBoundedQueueCapacity(queueName, 3);
        assertTrue(setResult, "设置容量应该成功");

        // 添加3个元素应该成功
        assertTrue(QueueUtils.addBoundedQueueObject(queueName, "item1"));
        assertTrue(QueueUtils.addBoundedQueueObject(queueName, "item2"));
        assertTrue(QueueUtils.addBoundedQueueObject(queueName, "item3"));

        // 第4个应该失败（队列已满）
        assertFalse(QueueUtils.addBoundedQueueObject(queueName, "item4"), "队列已满时添加应该失败");
    }

    @Test
    @DisplayName("测试有界队列-出队后可继续入队")
    public void testBoundedQueueDequeueEnqueue() {
        String queueName = genTestQueue("boundedcycle");

        QueueUtils.trySetBoundedQueueCapacity(queueName, 2);
        QueueUtils.addBoundedQueueObject(queueName, "a");
        QueueUtils.addBoundedQueueObject(queueName, "b");

        // 出队一个
        assertEquals("a", QueueUtils.getBoundedQueueObject(queueName));

        // 应该可以再入队一个
        assertTrue(QueueUtils.addBoundedQueueObject(queueName, "c"));

        // 验证顺序
        assertEquals("b", QueueUtils.getBoundedQueueObject(queueName));
        assertEquals("c", QueueUtils.getBoundedQueueObject(queueName));
    }

    @Test
    @DisplayName("测试trySetBoundedQueueCapacity-销毁后重设容量")
    public void testBoundedQueueResetCapacity() {
        String queueName = genTestQueue("boundedreset");

        // 第一次设置
        QueueUtils.trySetBoundedQueueCapacity(queueName, 2);
        QueueUtils.addBoundedQueueObject(queueName, "item");

        // 销毁后重设
        boolean result = QueueUtils.trySetBoundedQueueCapacity(queueName, 5, true);

        assertTrue(result, "销毁后重设容量应该成功");
    }

    @Test
    @DisplayName("测试removeBoundedQueueObject-删除有界队列元素")
    public void testRemoveBoundedQueueObject() {
        String queueName = genTestQueue("boundedremove");

        QueueUtils.trySetBoundedQueueCapacity(queueName, 5);
        QueueUtils.addBoundedQueueObject(queueName, "a");
        QueueUtils.addBoundedQueueObject(queueName, "b");
        QueueUtils.addBoundedQueueObject(queueName, "c");

        boolean removed = QueueUtils.removeBoundedQueueObject(queueName, "b");

        assertTrue(removed, "删除有界队列元素应该成功");

        assertEquals("a", QueueUtils.getBoundedQueueObject(queueName));
        assertEquals("c", QueueUtils.getBoundedQueueObject(queueName));
    }

    @Test
    @DisplayName("测试destroyBoundedQueue-销毁有界队列")
    public void testDestroyBoundedQueue() {
        String queueName = genTestQueue("boundeddestroy");

        QueueUtils.trySetBoundedQueueCapacity(queueName, 5);
        QueueUtils.addBoundedQueueObject(queueName, "item");

        boolean destroyed = QueueUtils.destroyBoundedQueue(queueName);

        assertTrue(destroyed, "销毁有界队列应该成功");
    }

    // ==================== 并发测试 ====================

    @Test
    @DisplayName("测试队列-并发入队出队")
    public void testQueueConcurrency() throws InterruptedException {
        String queueName = genTestQueue("concurrent");
        int producerCount = 5;
        int itemsPerProducer = 20;
        AtomicInteger consumedCount = new AtomicInteger(0);
        CountDownLatch producerLatch = new CountDownLatch(producerCount);
        CountDownLatch consumerLatch = new CountDownLatch(1);

        // 启动生产者线程
        for (int i = 0; i < producerCount; i++) {
            final int producerId = i;
            new Thread(() -> {
                try {
                    for (int j = 0; j < itemsPerProducer; j++) {
                        QueueUtils.addQueueObject(queueName, "producer" + producerId + "_item" + j);
                    }
                } finally {
                    producerLatch.countDown();
                }
            }).start();
        }

        // 等待所有生产者完成
        producerLatch.await(30, TimeUnit.SECONDS);

        // 消费所有消息
        new Thread(() -> {
            try {
                while (true) {
                    Object item = QueueUtils.getQueueObject(queueName);
                    if (item == null) {
                        break;
                    }
                    consumedCount.incrementAndGet();
                }
            } finally {
                consumerLatch.countDown();
            }
        }).start();

        consumerLatch.await(30, TimeUnit.SECONDS);

        assertEquals(producerCount * itemsPerProducer, consumedCount.get(),
            "消费的消息数量应该等于生产的消息数量");
    }

    // ==================== 工具方法测试 ====================

    @Test
    @DisplayName("测试getClient-获取Redisson客户端")
    public void testGetClient() {
        assertNotNull(QueueUtils.getClient(), "应该能获取到Redisson客户端");
    }

    // ==================== 测试辅助类 ====================

    /**
     * 测试用的队列项
     */
    public static class TestQueueItem implements java.io.Serializable {
        private static final long serialVersionUID = 1L;

        private Long id;
        private String message;

        public TestQueueItem() {
        }

        public TestQueueItem(Long id, String message) {
            this.id = id;
            this.message = message;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    /**
     * 优先级队列项（实现Comparable接口）
     */
    public static class PriorityItem implements Comparable<PriorityItem>, java.io.Serializable {
        private static final long serialVersionUID = 1L;

        private int priority;
        private String name;

        public PriorityItem() {
        }

        public PriorityItem(int priority, String name) {
            this.priority = priority;
            this.name = name;
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public int compareTo(PriorityItem other) {
            return Integer.compare(this.priority, other.priority);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PriorityItem that = (PriorityItem) o;
            return priority == that.priority && java.util.Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(priority, name);
        }
    }
}
