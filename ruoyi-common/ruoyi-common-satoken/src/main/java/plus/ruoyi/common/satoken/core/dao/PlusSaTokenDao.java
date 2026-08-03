package plus.ruoyi.common.satoken.core.dao;

import cn.dev33.satoken.dao.auto.SaTokenDaoBySessionFollowObject;
import cn.dev33.satoken.util.SaFoxUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import plus.ruoyi.common.redis.utils.RedisUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Sa-Token增强存储层实现
 *
 * <p>基于Caffeine + Redis的二级缓存架构，提供高性能的Token存储服务：
 * <ul>
 *   <li>一级缓存：Caffeine本地缓存，5秒过期，提供毫秒级访问</li>
 *   <li>二级缓存：Redis分布式缓存，支持集群间数据共享</li>
 *   <li>读写策略：读取时先查本地缓存，写入时失效本地缓存</li>
 *   <li>兼容性：继承SaTokenDaoBySessionFollowObject，简化session处理</li>
 * </ul>
 *
 * <p>性能优化：
 * <ul>
 *   <li>高频读取操作通过本地缓存显著提升性能</li>
 *   <li>写入操作主动失效本地缓存，保证数据一致性</li>
 *   <li>搜索结果缓存，减少重复查询开销</li>
 * </ul>
 *
 * @author Lion Li
 */
public class PlusSaTokenDao implements SaTokenDaoBySessionFollowObject {

    /**
     * Caffeine本地缓存实例
     * 5秒写入过期，容量1000，优化Token高频查询场景
     */
    private static final Cache<String, Object> CAFFEINE = Caffeine.newBuilder()
        // 设置最后一次写入或访问后经过固定时间过期
        .expireAfterWrite(5, TimeUnit.SECONDS)
        // 初始的缓存空间大小
        .initialCapacity(100)
        // 缓存的最大条数
        .maximumSize(1000)
        .build();

    /**
     * 获取字符串值
     *
     * <p>优先从本地缓存获取，未命中则查询Redis并缓存到本地
     *
     * @param key 缓存key
     * @return 字符串值，不存在返回null
     */
    @Override
    public String get(String key) {
        Object o = CAFFEINE.get(key, k -> RedisUtils.getCacheObject(key));
        return (String) o;
    }

    /**
     * 设置字符串值及过期时间
     *
     * @param key 缓存key
     * @param value 缓存值
     * @param timeout 过期时间(秒)，0或负数表示无效，-1表示永不过期
     */
    @Override
    public void set(String key, String value, long timeout) {
        if (timeout == 0 || timeout <= NOT_VALUE_EXPIRE) {
            return;
        }
        // 永不过期
        if (timeout == NEVER_EXPIRE) {
            RedisUtils.setCacheObject(key, value);
        } else {
            RedisUtils.setCacheObject(key, value, Duration.ofSeconds(timeout));
        }
        // 失效本地缓存保证一致性
        CAFFEINE.invalidate(key);
    }

    /**
     * 更新字符串值(保持原过期时间)
     *
     * @param key 缓存key
     * @param value 新值
     */
    @Override
    public void update(String key, String value) {
        if (RedisUtils.hasKey(key)) {
            RedisUtils.setCacheObject(key, value, true);
            CAFFEINE.invalidate(key);
        }
    }

    /**
     * 删除指定key
     *
     * @param key 缓存key
     */
    @Override
    public void delete(String key) {
        if (RedisUtils.deleteObject(key)) {
            CAFFEINE.invalidate(key);
        }
    }

    /**
     * 获取key的剩余存活时间
     *
     * @param key 缓存key
     * @return 剩余时间(秒)，-1表示永不过期，-2表示key不存在
     */
    @Override
    public long getTimeout(String key) {
        long timeout = RedisUtils.getTimeToLive(key);
        // 加1的目的 解决sa-token使用秒 redis是毫秒导致1秒的精度问题 手动补偿
        return timeout < 0 ? timeout : timeout / 1000 + 1;
    }

    /**
     * 修改key的剩余存活时间
     *
     * @param key 缓存key
     * @param timeout 新的过期时间(秒)
     */
    @Override
    public void updateTimeout(String key, long timeout) {
        RedisUtils.expire(key, Duration.ofSeconds(timeout));
    }

    /**
     * 获取对象值
     *
     * @param key 缓存key
     * @return 对象值，不存在返回null
     */
    @Override
    public Object getObject(String key) {
        Object o = CAFFEINE.get(key, k -> RedisUtils.getCacheObject(key));
        return o;
    }

    /**
     * 获取指定类型的对象值
     *
     * @param key 缓存key
     * @param classType 目标类型
     * @return 指定类型的对象值
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getObject(String key, Class<T> classType) {
        Object o = CAFFEINE.get(key, k -> RedisUtils.getCacheObject(key));
        return (T) o;
    }

    /**
     * 设置对象值及过期时间
     *
     * @param key 缓存key
     * @param object 对象值
     * @param timeout 过期时间(秒)
     */
    @Override
    public void setObject(String key, Object object, long timeout) {
        if (timeout == 0 || timeout <= NOT_VALUE_EXPIRE) {
            return;
        }
        if (timeout == NEVER_EXPIRE) {
            RedisUtils.setCacheObject(key, object);
        } else {
            RedisUtils.setCacheObject(key, object, Duration.ofSeconds(timeout));
        }
        CAFFEINE.invalidate(key);
    }

    /**
     * 更新对象值(保持原过期时间)
     *
     * @param key 缓存key
     * @param object 新对象值
     */
    @Override
    public void updateObject(String key, Object object) {
        if (RedisUtils.hasKey(key)) {
            RedisUtils.setCacheObject(key, object, true);
            CAFFEINE.invalidate(key);
        }
    }

    /**
     * 删除对象
     *
     * @param key 缓存key
     */
    @Override
    public void deleteObject(String key) {
        if (RedisUtils.deleteObject(key)) {
            CAFFEINE.invalidate(key);
        }
    }

    /**
     * 获取对象的剩余存活时间
     *
     * @param key 缓存key
     * @return 剩余时间(秒)
     */
    @Override
    public long getObjectTimeout(String key) {
        long timeout = RedisUtils.getTimeToLive(key);
        // 加1的目的 解决sa-token使用秒 redis是毫秒导致1秒的精度问题 手动补偿
        return timeout < 0 ? timeout : timeout / 1000 + 1;
    }

    /**
     * 修改对象的剩余存活时间
     *
     * @param key 缓存key
     * @param timeout 新的过期时间(秒)
     */
    @Override
    public void updateObjectTimeout(String key, long timeout) {
        RedisUtils.expire(key, Duration.ofSeconds(timeout));
    }

    /**
     * 搜索匹配的key列表
     *
     * <p>支持通配符搜索，结果会缓存到本地以提升重复查询性能
     *
     * @param prefix 前缀
     * @param keyword 关键词
     * @param start 起始位置
     * @param size 返回数量
     * @param sortType 排序类型
     * @return 匹配的key列表
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<String> searchData(String prefix, String keyword, int start, int size, boolean sortType) {
        String keyStr = prefix + "*" + keyword + "*";
        return (List<String>) CAFFEINE.get(keyStr, k -> {
            Collection<String> keys = RedisUtils.keys(keyStr);
            List<String> list = new ArrayList<>(keys);
            return SaFoxUtil.searchList(list, start, size, sortType);
        });
    }
}
