/**
 * Copyright (c) 2013-2021 Nikita Koksharov
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package plus.ruoyi.common.redis.manager;

import lombok.Setter;
import plus.ruoyi.common.redis.utils.RedisUtils;
import org.redisson.api.RMap;
import org.redisson.api.RMapCache;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonCache;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheDecorator;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 增强的Spring缓存管理器
 *
 * <p>基于Redisson实现的{@link CacheManager}，相比原生RedissonSpringCacheManager增强了以下功能：
 * <ul>
 *   <li>支持动态缓存名称配置，格式：cacheName#ttl#maxIdleTime#maxSize#local</li>
 *   <li>集成Caffeine本地缓存，实现二级缓存架构</li>
 *   <li>支持事务感知的缓存操作</li>
 *   <li>支持null值存储配置</li>
 * </ul>
 *
 * <p>缓存名称格式说明：
 * <pre>
 * cacheName#30s#10s#1000#1
 * |         |   |   |    └── 是否启用本地缓存(1=启用,0=禁用)
 * |         |   |   └────── 最大缓存条目数
 * |         |   └────────── 最大空闲时间
 * |         └────────────── 存活时间(TTL)
 * └──────────────────────── 缓存名称
 * </pre>
 *
 * @author Nikita Koksharov
 */
@SuppressWarnings("unchecked")
@Setter
public class PlusSpringCacheManager implements CacheManager {

    /** 是否支持动态创建缓存 */
    private boolean dynamic = true;

    /** 是否允许存储null值 */
    private boolean allowNullValues = true;

    /** 是否启用事务感知 */
    private boolean transactionAware = true;

    /** 缓存配置映射表 */
    Map<String, CacheConfig> configMap = new ConcurrentHashMap<>();
    /** 缓存实例映射表 */
    ConcurrentMap<String, Cache> instanceMap = new ConcurrentHashMap<>();

    /**
     * 创建由Redisson实例支持的缓存管理器
     */
    public PlusSpringCacheManager() {
    }

    /**
     * 设置固定的缓存名称集合
     *
     * <p>设置后将禁用动态模式，只能创建预定义名称的缓存
     *
     * @param names 缓存名称集合，null表示启用动态模式
     */
    public void setCacheNames(Collection<String> names) {
        if (names != null) {
            for (String name : names) {
                getCache(name);
            }
            dynamic = false;
        } else {
            dynamic = true;
        }
    }

    /**
     * 设置缓存配置映射
     *
     * @param config 按缓存名称映射的配置对象
     */
    public void setConfig(Map<String, ? extends CacheConfig> config) {
        this.configMap = (Map<String, CacheConfig>) config;
    }

    /**
     * 创建默认缓存配置
     *
     * @return 默认配置实例
     */
    protected CacheConfig createDefaultConfig() {
        return new CacheConfig();
    }

    /**
     * 获取缓存实例
     *
     * <p>支持动态配置格式：cacheName#ttl#maxIdleTime#maxSize#local
     *
     * @param name 缓存名称(可包含配置参数)
     * @return 缓存实例
     */
    @Override
    public Cache getCache(String name) {
        // 解析缓存名称和配置参数
        String[] array = StringUtils.delimitedListToStringArray(name, "#");
        name = array[0];

        Cache cache = instanceMap.get(name);
        if (cache != null) {
            return cache;
        }
        if (!dynamic) {
            return cache;
        }

        // 获取或创建缓存配置
        CacheConfig config = configMap.get(name);
        if (config == null) {
            config = createDefaultConfig();
            configMap.put(name, config);
        }

        // 解析配置参数
        if (array.length > 1) {
            config.setTTL(DurationStyle.detectAndParse(array[1]).toMillis());
        }
        if (array.length > 2) {
            config.setMaxIdleTime(DurationStyle.detectAndParse(array[2]).toMillis());
        }
        if (array.length > 3) {
            config.setMaxSize(Integer.parseInt(array[3]));
        }
        // 默认启用本地缓存
        int local = 1;
        if (array.length > 4) {
            local = Integer.parseInt(array[4]);
        }

        // 根据配置选择缓存类型
        if (config.getMaxIdleTime() == 0 && config.getTTL() == 0 && config.getMaxSize() == 0) {
            return createMap(name, config, local);
        }

        return createMapCache(name, config, local);
    }

    /**
     * 创建普通Map缓存
     *
     * @param name 缓存名称
     * @param config 缓存配置
     * @param local 是否启用本地缓存
     * @return 缓存实例
     */
    private Cache createMap(String name, CacheConfig config, int local) {
        RMap<Object, Object> map = RedisUtils.getClient().getMap(name);

        Cache cache = new RedissonCache(map, allowNullValues);
        // 根据配置决定是否包装本地缓存
        if (local == 1) {
            cache = new CaffeineCacheDecorator(name, cache);
        }
        if (transactionAware) {
            cache = new TransactionAwareCacheDecorator(cache);
        }
        Cache oldCache = instanceMap.putIfAbsent(name, cache);
        if (oldCache != null) {
            cache = oldCache;
        }
        return cache;
    }

    /**
     * 创建带过期策略的MapCache缓存
     *
     * @param name 缓存名称
     * @param config 缓存配置
     * @param local 是否启用本地缓存
     * @return 缓存实例
     */
    private Cache createMapCache(String name, CacheConfig config, int local) {
        RMapCache<Object, Object> map = RedisUtils.getClient().getMapCache(name);

        Cache cache = new RedissonCache(map, config, allowNullValues);
        if (local == 1) {
            cache = new CaffeineCacheDecorator(name, cache);
        }
        if (transactionAware) {
            cache = new TransactionAwareCacheDecorator(cache);
        }
        Cache oldCache = instanceMap.putIfAbsent(name, cache);
        if (oldCache != null) {
            cache = oldCache;
        } else {
            map.setMaxSize(config.getMaxSize());
        }
        return cache;
    }

    @Override
    public Collection<String> getCacheNames() {
        return Collections.unmodifiableSet(configMap.keySet());
    }

}
