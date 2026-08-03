package plus.ruoyi.common.redis.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import plus.ruoyi.common.core.utils.SpringUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/**
 * 缓存操作工具类
 *
 * <p>提供统一的缓存操作接口，简化Spring Cache的使用。
 * 支持多种缓存实现(Redis、Caffeine等)的统一操作。</p>
 *
 * <p>使用示例：
 * <pre>
 * // 存储缓存
 * CacheUtils.put("userCache", "user:123", userObj);
 *
 * // 获取缓存
 * User user = CacheUtils.get("userCache", "user:123");
 *
 * // 删除缓存
 * CacheUtils.evict("userCache", "user:123");
 *
 * // 清空缓存组
 * CacheUtils.clear("userCache");
 * </pre>
 *
 * @author Michelle.Chung
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings(value = {"unchecked"})
public class CacheUtils {

    /** Spring缓存管理器实例 */
    private static final CacheManager CACHE_MANAGER = SpringUtils.getBean(CacheManager.class);

    /**
     * 获取缓存值
     *
     * @param <T> 返回值类型
     * @param cacheNames 缓存组名称
     * @param key 缓存key
     * @return 缓存值，不存在时返回null
     */
    public static <T> T get(String cacheNames, Object key) {
        Cache.ValueWrapper wrapper = CACHE_MANAGER.getCache(cacheNames).get(key);
        return wrapper != null ? (T) wrapper.get() : null;
    }

    /**
     * 保存缓存值
     *
     * @param cacheNames 缓存组名称
     * @param key 缓存key
     * @param value 缓存值
     */
    public static void put(String cacheNames, Object key, Object value) {
        CACHE_MANAGER.getCache(cacheNames).put(key, value);
    }

    /**
     * 删除指定缓存项
     *
     * @param cacheNames 缓存组名称
     * @param key 缓存key
     */
    public static void evict(String cacheNames, Object key) {
        CACHE_MANAGER.getCache(cacheNames).evict(key);
    }

    /**
     * 清空指定缓存组的所有数据
     *
     * @param cacheNames 缓存组名称
     */
    public static void clear(String cacheNames) {
        CACHE_MANAGER.getCache(cacheNames).clear();
    }

}
