package plus.ruoyi.common.mp.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import me.chanjar.weixin.common.redis.BaseWxRedisOps;
import plus.ruoyi.common.redis.utils.RedisUtils;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * 公众号配置缓存实现
 *
 * @author bkywksj
 */
public class PlusWxRedisOps extends BaseWxRedisOps {

    private static final Cache<String, Object> CAFFEINE = Caffeine.newBuilder()
        // 设置最后一次写入或访问后经过固定时间过期
        .expireAfterWrite(5, TimeUnit.SECONDS)
        // 初始的缓存空间大小
        .initialCapacity(100)
        // 缓存的最大条数
        .maximumSize(1000)
        .build();

    @Override
    public String getValue(String key) {
        Object o = CAFFEINE.get(key, k -> RedisUtils.getCacheObject(key));
        return (String) o;
    }

    @Override
    public void setValue(String key, String value, int expire, TimeUnit timeUnit) {
        RedisUtils.setCacheObject(key, value, Duration.ofMillis(timeUnit.toMillis(expire)));
        CAFFEINE.put(key, value);
    }

    @Override
    public Long getExpire(String key) {
        long timeout = RedisUtils.getTimeToLive(key);
        return timeout < 0 ? timeout : timeout / 1000;
    }

    @Override
    public void expire(String key, int expire, TimeUnit timeUnit) {
        RedisUtils.expire(key, Duration.ofMillis(timeUnit.toMillis(expire)));
    }

    @Override
    public Lock getLock(String key) {
        return RedisUtils.getLock(key);
    }
}
