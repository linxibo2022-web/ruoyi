package plus.ruoyi.common.redis.manager;

import plus.ruoyi.common.core.utils.SpringUtils;
import org.springframework.cache.Cache;

import java.util.concurrent.Callable;

/**
 * Caffeine缓存装饰器
 *
 * <p>基于装饰器模式实现二级缓存架构：
 * <ul>
 *   <li>一级缓存：Caffeine本地缓存，提供快速访问</li>
 *   <li>二级缓存：Redis分布式缓存，提供数据持久化和共享</li>
 * </ul>
 *
 * <p>缓存策略：
 * <ul>
 *   <li>读取：先查Caffeine，未命中则查Redis并回写Caffeine</li>
 *   <li>写入：直接写Redis，同时失效Caffeine中的对应数据</li>
 *   <li>删除：同时清理两级缓存</li>
 * </ul>
 *
 * @author LionLi
 */
public class CaffeineCacheDecorator implements Cache {

    /**
     * Caffeine本地缓存实例
     */
    private static final com.github.benmanes.caffeine.cache.Cache<Object, Object>
        CAFFEINE = SpringUtils.getBean("caffeine");

    /**
     * 缓存名称
     */
    private final String name;
    /**
     * 底层缓存实现(通常为Redis缓存)
     */
    private final Cache cache;

    public CaffeineCacheDecorator(String name, Cache cache) {
        this.name = name;
        this.cache = cache;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getNativeCache() {
        return cache.getNativeCache();
    }

    /**
     * 生成唯一缓存key
     *
     * @param key 原始key
     * @return 带缓存名称前缀的唯一key
     */
    public String getUniqueKey(Object key) {
        return name + ":" + key;
    }

    @Override
    public ValueWrapper get(Object key) {
        // 先查Caffeine，未命中则查Redis并缓存到Caffeine
        Object o = CAFFEINE.get(getUniqueKey(key), k -> cache.get(key));
        return (ValueWrapper) o;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Object key, Class<T> type) {
        Object o = CAFFEINE.get(getUniqueKey(key), k -> cache.get(key, type));
        return (T) o;
    }

    @Override
    public void put(Object key, Object value) {
        // 失效Caffeine缓存，写入Redis
        CAFFEINE.invalidate(getUniqueKey(key));
        cache.put(key, value);
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        CAFFEINE.invalidate(getUniqueKey(key));
        return cache.putIfAbsent(key, value);
    }

    @Override
    public void evict(Object key) {
        evictIfPresent(key);
    }

    @Override
    public boolean evictIfPresent(Object key) {
        // 先删除Redis，成功后再删除Caffeine
        boolean b = cache.evictIfPresent(key);
        if (b) {
            CAFFEINE.invalidate(getUniqueKey(key));
        }
        return b;
    }

    @Override
    public void clear() {
        // 清空两级缓存
        CAFFEINE.invalidateAll();
        cache.clear();
    }

    @Override
    public boolean invalidate() {
        return cache.invalidate();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        Object o = CAFFEINE.get(getUniqueKey(key), k -> cache.get(key, valueLoader));
        return (T) o;
    }

}
