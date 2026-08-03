package plus.ruoyi.common.redis.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import plus.ruoyi.common.core.utils.SpringUtils;
import org.redisson.api.*;
import org.redisson.api.options.KeysScanOptions;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Redis工具类
 * <p>
 * 基于Redisson实现的Redis操作工具类，提供多种数据结构操作：
 * - 基础缓存：String类型的存取操作
 * - 集合操作：List、Set、Map等集合类型操作
 * - 发布订阅：消息发布和订阅功能
 * - 限流控制：基于令牌桶的限流机制
 * - 原子操作：原子长整型的递增递减操作
 * - 监听机制：支持各种数据类型的变更监听
 *
 * @author Lion Li
 * @version 3.1.0 新增
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings(value = {"unchecked", "rawtypes"})
public class RedisUtils {

    private static final RedissonClient CLIENT = SpringUtils.getBean(RedissonClient.class);

    // ==================== 客户端和限流 ====================

    /**
     * 获取Redisson客户端实例
     *
     * @return RedissonClient实例
     */
    public static RedissonClient getClient() {
        return CLIENT;
    }

    /**
     * 限流控制（默认无超时）
     *
     * @param key          限流key
     * @param rateType     限流类型（OVERALL/PER_CLIENT）
     * @param rate         允许的速率（每个时间间隔内的请求数）
     * @param rateInterval 速率间隔（秒）
     * @return 剩余令牌数，-1表示获取失败
     */
    public static long rateLimiter(String key, RateType rateType, int rate, int rateInterval) {
        return rateLimiter(key, rateType, rate, rateInterval, 0);
    }

    /**
     * 限流控制
     *
     * @param key          限流key
     * @param rateType     限流类型（OVERALL/PER_CLIENT）
     * @param rate         允许的速率（每个时间间隔内的请求数）
     * @param rateInterval 速率间隔（秒）
     * @param timeout      超时时间（秒）
     * @return 剩余令牌数，-1表示获取失败
     */
    public static long rateLimiter(String key, RateType rateType, int rate, int rateInterval, int timeout) {
        RRateLimiter rateLimiter = CLIENT.getRateLimiter(key);
        rateLimiter.trySetRate(rateType, rate, Duration.ofSeconds(rateInterval), Duration.ofSeconds(timeout));
        if (rateLimiter.tryAcquire()) {
            return rateLimiter.availablePermits();
        } else {
            return -1L;
        }
    }

    // ==================== 发布订阅 ====================

    /**
     * 发布消息到指定频道
     *
     * @param channelKey 频道key
     * @param msg        发送数据
     */
    public static <T> void publish(String channelKey, T msg) {
        RTopic topic = CLIENT.getTopic(channelKey);
        topic.publish(msg);
    }

    /**
     * 发布消息到指定频道并自定义处理
     *
     * @param channelKey 频道key
     * @param msg        发送数据
     * @param consumer   自定义处理函数
     */
    public static <T> void publish(String channelKey, T msg, Consumer<T> consumer) {
        RTopic topic = CLIENT.getTopic(channelKey);
        topic.publish(msg);
        consumer.accept(msg);
    }

    /**
     * 订阅频道接收消息
     *
     * @param channelKey 频道key
     * @param clazz      消息类型
     * @param consumer   消息处理函数
     */
    public static <T> void subscribe(String channelKey, Class<T> clazz, Consumer<T> consumer) {
        RTopic topic = CLIENT.getTopic(channelKey);
        topic.addListener(clazz, (channel, msg) -> consumer.accept(msg));
    }

    // ==================== 基础缓存操作 ====================

    /**
     * 缓存基本对象（永不过期）
     *
     * @param key   缓存的键值
     * @param value 缓存的值
     */
    public static <T> void setCacheObject(final String key, final T value) {
        setCacheObject(key, value, false);
    }

    /**
     * 缓存基本对象，支持保留TTL
     *
     * @param key       缓存的键值
     * @param value     缓存的值
     * @param isSaveTtl 是否保留TTL有效期
     *                  注意：需要Redis 6.0以上版本支持setAndKeepTTL，低版本会降级处理
     */
    public static <T> void setCacheObject(final String key, final T value, final boolean isSaveTtl) {
        RBucket<T> bucket = CLIENT.getBucket(key);
        if (isSaveTtl) {
            try {
                bucket.setAndKeepTTL(value);
            } catch (Exception e) {
                long timeToLive = bucket.remainTimeToLive();
                if (timeToLive == -1) {
                    bucket.set(value);
                } else {
                    bucket.set(value, Duration.ofMillis(timeToLive));
                }
            }
        } else {
            bucket.set(value);
        }
    }

    /**
     * 缓存基本对象并设置过期时间
     *
     * @param key      缓存的键值
     * @param value    缓存的值
     * @param duration 过期时间
     */
    public static <T> void setCacheObject(final String key, final T value, final Duration duration) {
        RBucket<T> bucket = CLIENT.getBucket(key);
        bucket.set(value, duration);
    }

    /**
     * 仅当key不存在时设置值
     *
     * @param key      缓存的键值
     * @param value    缓存的值
     * @param duration 过期时间
     * @return 设置成功返回true，key已存在返回false
     */
    public static <T> boolean setObjectIfAbsent(final String key, final T value, final Duration duration) {
        RBucket<T> bucket = CLIENT.getBucket(key);
        return bucket.setIfAbsent(value, duration);
    }

    /**
     * 仅当key存在时设置值
     *
     * @param key      缓存的键值
     * @param value    缓存的值
     * @param duration 过期时间
     * @return 设置成功返回true，key不存在返回false
     */
    public static <T> boolean setObjectIfExists(final String key, final T value, final Duration duration) {
        RBucket<T> bucket = CLIENT.getBucket(key);
        return bucket.setIfExists(value, duration);
    }

    /**
     * 获取缓存的基本对象
     *
     * @param key 缓存键值
     * @return 缓存键值对应的数据
     */
    public static <T> T getCacheObject(final String key) {
        RBucket<T> rBucket = CLIENT.getBucket(key);
        return rBucket.get();
    }

    /**
     * 获取key的剩余存活时间
     *
     * @param key 缓存键值
     * @return 剩余存活时间（毫秒），-1表示永不过期，-2表示key不存在
     */
    public static <T> long getTimeToLive(final String key) {
        RBucket<T> rBucket = CLIENT.getBucket(key);
        return rBucket.remainTimeToLive();
    }

    /**
     * 设置key的过期时间
     *
     * @param key     Redis键
     * @param timeout 超时时间（秒）
     * @return 设置成功返回true，失败返回false
     */
    public static boolean expire(final String key, final long timeout) {
        return expire(key, Duration.ofSeconds(timeout));
    }

    /**
     * 设置key的过期时间
     *
     * @param key      Redis键
     * @param duration 超时时间
     * @return 设置成功返回true，失败返回false
     */
    public static boolean expire(final String key, final Duration duration) {
        RBucket rBucket = CLIENT.getBucket(key);
        return rBucket.expire(duration);
    }

    /**
     * 删除单个缓存对象
     *
     * @param key 缓存的键值
     * @return 删除成功返回true，失败返回false
     */
    public static boolean deleteObject(final String key) {
        return CLIENT.getBucket(key).delete();
    }

    /**
     * 批量删除缓存对象
     *
     * @param collection 多个key的集合
     */
    public static void deleteObject(final Collection collection) {
        RBatch batch = CLIENT.createBatch();
        collection.forEach(t -> {
            batch.getBucket(t.toString()).deleteAsync();
        });
        batch.execute();
    }

    /**
     * 检查缓存对象是否存在
     *
     * @param key 缓存的键值
     * @return 存在返回true，不存在返回false
     */
    public static boolean isExistsObject(final String key) {
        return CLIENT.getBucket(key).isExists();
    }

    /**
     * 注册对象监听器
     * 注意：需要开启Redis的notify-keyspace-events配置
     *
     * @param key      缓存的键值
     * @param listener 监听器配置
     */
    public static <T> void addObjectListener(final String key, final ObjectListener listener) {
        RBucket<T> result = CLIENT.getBucket(key);
        result.addListener(listener);
    }

    // ==================== List操作 ====================

    /**
     * 缓存List数据
     *
     * @param key      缓存的键值
     * @param dataList 待缓存的List数据
     * @return 添加成功返回true，失败返回false
     */
    public static <T> boolean setCacheList(final String key, final List<T> dataList) {
        RList<T> rList = CLIENT.getList(key);
        return rList.addAll(dataList);
    }

    /**
     * 缓存List数据并设置过期时间
     *
     * @param key      缓存的键值
     * @param dataList 待缓存的List数据
     * @param duration 过期时间
     * @return 添加成功返回true，失败返回false
     */
    public static <T> boolean setCacheList(final String key, final List<T> dataList, final Duration duration) {
        RList<T> rList = CLIENT.getList(key);
        boolean result = rList.addAll(dataList);
        if (result) {
            rList.expire(duration);
        }
        return result;
    }

    /**
     * 向List追加单个数据
     *
     * @param key  缓存的键值
     * @param data 待缓存的数据
     * @return 添加成功返回true，失败返回false
     */
    public static <T> boolean addCacheList(final String key, final T data) {
        RList<T> rList = CLIENT.getList(key);
        return rList.add(data);
    }

    /**
     * 获取完整的缓存List
     *
     * @param key 缓存的键值
     * @return 缓存键值对应的List数据
     */
    public static <T> List<T> getCacheList(final String key) {
        RList<T> rList = CLIENT.getList(key);
        return rList.readAll();
    }

    /**
     * 获取缓存List的指定范围数据
     *
     * @param key  缓存的键值
     * @param from 起始下标（包含）
     * @param to   结束下标（包含）
     * @return 指定范围的List数据
     */
    public static <T> List<T> getCacheListRange(final String key, int from, int to) {
        RList<T> rList = CLIENT.getList(key);
        return rList.range(from, to);
    }

    /**
     * 注册List监听器
     * 注意：需要开启Redis的notify-keyspace-events配置
     *
     * @param key      缓存的键值
     * @param listener 监听器配置
     */
    public static <T> void addListListener(final String key, final ObjectListener listener) {
        RList<T> rList = CLIENT.getList(key);
        rList.addListener(listener);
    }

    // ==================== Set操作 ====================

    /**
     * 缓存Set数据
     *
     * @param key     缓存键值
     * @param dataSet 缓存的数据集合
     * @return 添加成功返回true，失败返回false
     */
    public static <T> boolean setCacheSet(final String key, final Set<T> dataSet) {
        RSet<T> rSet = CLIENT.getSet(key);
        return rSet.addAll(dataSet);
    }

    /**
     * 向Set追加单个数据
     *
     * @param key  缓存的键值
     * @param data 待缓存的数据
     * @return 添加成功返回true，数据已存在返回false
     */
    public static <T> boolean addCacheSet(final String key, final T data) {
        RSet<T> rSet = CLIENT.getSet(key);
        return rSet.add(data);
    }

    /**
     * 获取缓存的Set数据
     *
     * @param key 缓存的key
     * @return Set对象
     */
    public static <T> Set<T> getCacheSet(final String key) {
        RSet<T> rSet = CLIENT.getSet(key);
        return rSet.readAll();
    }

    /**
     * 注册Set监听器
     * 注意：需要开启Redis的notify-keyspace-events配置
     *
     * @param key      缓存的键值
     * @param listener 监听器配置
     */
    public static <T> void addSetListener(final String key, final ObjectListener listener) {
        RSet<T> rSet = CLIENT.getSet(key);
        rSet.addListener(listener);
    }

    // ==================== Map操作 ====================

    /**
     * 缓存Map数据
     *
     * @param key     缓存的键值
     * @param dataMap 缓存的数据映射
     */
    public static <T> void setCacheMap(final String key, final Map<String, T> dataMap) {
        if (dataMap != null) {
            RMap<String, T> rMap = CLIENT.getMap(key);
            rMap.putAll(dataMap);
        }
    }

    /**
     * 获取缓存的Map数据
     *
     * @param key 缓存的键值
     * @return Map对象
     */
    public static <T> Map<String, T> getCacheMap(final String key) {
        RMap<String, T> rMap = CLIENT.getMap(key);
        return rMap.getAll(rMap.keySet());
    }

    /**
     * 获取缓存Map的所有key
     *
     * @param key 缓存的键值
     * @return key集合
     */
    public static <T> Set<String> getCacheMapKeySet(final String key) {
        RMap<String, T> rMap = CLIENT.getMap(key);
        return rMap.keySet();
    }

    /**
     * 设置Map中的单个值
     *
     * @param key   Redis键
     * @param hKey  Hash键
     * @param value 值
     */
    public static <T> void setCacheMapValue(final String key, final String hKey, final T value) {
        RMap<String, T> rMap = CLIENT.getMap(key);
        rMap.put(hKey, value);
    }

    /**
     * 获取Map中的单个值
     *
     * @param key  Redis键
     * @param hKey Hash键
     * @return Hash中的对象
     */
    public static <T> T getCacheMapValue(final String key, final String hKey) {
        RMap<String, T> rMap = CLIENT.getMap(key);
        return rMap.get(hKey);
    }

    /**
     * 删除Map中的单个值
     *
     * @param key  Redis键
     * @param hKey Hash键
     * @return 被删除的对象
     */
    public static <T> T delCacheMapValue(final String key, final String hKey) {
        RMap<String, T> rMap = CLIENT.getMap(key);
        return rMap.remove(hKey);
    }

    /**
     * 批量删除Map中的多个值
     *
     * @param key   Redis键
     * @param hKeys Hash键集合
     */
    public static <T> void delMultiCacheMapValue(final String key, final Set<String> hKeys) {
        RBatch batch = CLIENT.createBatch();
        RMapAsync<String, T> rMap = batch.getMap(key);
        for (String hKey : hKeys) {
            rMap.removeAsync(hKey);
        }
        batch.execute();
    }

    /**
     * 获取Map中的多个值
     *
     * @param key   Redis键
     * @param hKeys Hash键集合
     * @return Hash对象映射
     */
    public static <K, V> Map<K, V> getMultiCacheMapValue(final String key, final Set<K> hKeys) {
        RMap<K, V> rMap = CLIENT.getMap(key);
        return rMap.getAll(hKeys);
    }

    /**
     * 注册Map监听器
     * 注意：需要开启Redis的notify-keyspace-events配置
     *
     * @param key      缓存的键值
     * @param listener 监听器配置
     */
    public static <T> void addMapListener(final String key, final ObjectListener listener) {
        RMap<String, T> rMap = CLIENT.getMap(key);
        rMap.addListener(listener);
    }

    // ==================== 原子操作 ====================

    /**
     * 设置原子长整型值
     *
     * @param key   Redis键
     * @param value 值
     */
    public static void setAtomicValue(String key, long value) {
        RAtomicLong atomic = CLIENT.getAtomicLong(key);
        atomic.set(value);
    }

    /**
     * 获取原子长整型值
     *
     * @param key Redis键
     * @return 当前值
     */
    public static long getAtomicValue(String key) {
        RAtomicLong atomic = CLIENT.getAtomicLong(key);
        return atomic.get();
    }

    /**
     * 原子递增操作
     *
     * @param key Redis键
     * @return 递增后的值
     */
    public static long incrAtomicValue(String key) {
        RAtomicLong atomic = CLIENT.getAtomicLong(key);
        return atomic.incrementAndGet();
    }

    /**
     * 原子递减操作
     *
     * @param key Redis键
     * @return 递减后的值
     */
    public static long decrAtomicValue(String key) {
        RAtomicLong atomic = CLIENT.getAtomicLong(key);
        return atomic.decrementAndGet();
    }

    // ==================== Key操作 ====================

    /**
     * 获取匹配模式的所有key
     * 注意：该操作会忽略租户隔离，需要手动拼接租户ID
     *
     * @param pattern 字符串匹配模式（支持通配符*和?）
     * @return 匹配的key集合
     */
    public static Collection<String> keys(final String pattern) {
        return keys(KeysScanOptions.defaults().pattern(pattern).chunkSize(1000));
    }

    /**
     * 通过扫描参数获取key列表
     *
     * @param keysScanOptions 扫描参数配置
     *                        - limit: 扫描限制数量（默认0，查询全部）
     *                        - pattern: 键匹配模式（默认null）
     *                        - chunkSize: 每次扫描块大小（默认0）
     *                        - type: 键类型（默认null，查询全部类型）
     * @return 匹配的key集合
     */
    public static Collection<String> keys(final KeysScanOptions keysScanOptions) {
        Stream<String> keysStream = CLIENT.getKeys().getKeysStream(keysScanOptions);
        return keysStream.collect(Collectors.toList());
    }

    /**
     * 按模式批量删除key
     * 注意：该操作会忽略租户隔离，需要手动拼接租户ID
     *
     * @param pattern 字符串匹配模式（支持通配符*和?）
     */
    public static void deleteKeys(final String pattern) {
        CLIENT.getKeys().deleteByPattern(pattern);
    }

    /**
     * 检查Redis中是否存在指定key
     *
     * @param key 键
     * @return 存在返回true，不存在返回false
     */
    public static Boolean hasKey(String key) {
        RKeys rKeys = CLIENT.getKeys();
        return rKeys.countExists(key) > 0;
    }

    /**
     * 获取锁
     */
    public static RLock getLock(String key) {
        return CLIENT.getLock(key);
    }
}
