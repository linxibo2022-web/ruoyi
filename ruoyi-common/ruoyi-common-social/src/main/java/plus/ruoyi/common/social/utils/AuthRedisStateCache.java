package plus.ruoyi.common.social.utils;

import lombok.AllArgsConstructor;
import me.zhyd.oauth.cache.AuthStateCache;
import plus.ruoyi.common.core.constant.GlobalConstants;
import plus.ruoyi.common.redis.utils.RedisUtils;

import java.time.Duration;

/**
 * Redis实现的授权状态缓存
 * <p>
 * 用于存储OAuth认证过程中的状态信息，防止CSRF攻击
 * @author thiszhc
 */
@AllArgsConstructor
public class AuthRedisStateCache implements AuthStateCache {

    /**
     * 存储缓存（使用默认过期时间）
     *
     * @param key   缓存键
     * @param value 缓存值
     */
    @Override
    public void cache(String key, String value) {
        // 授权超时时间默认3分钟
        RedisUtils.setCacheObject(GlobalConstants.SOCIAL_AUTH_CODE_KEY + key, value, Duration.ofMinutes(3));
    }

    /**
     * 存储缓存（指定过期时间）
     *
     * @param key     缓存键
     * @param value   缓存值
     * @param timeout 过期时间（毫秒）
     */
    @Override
    public void cache(String key, String value, long timeout) {
        RedisUtils.setCacheObject(GlobalConstants.SOCIAL_AUTH_CODE_KEY + key, value, Duration.ofMillis(timeout));
    }

    /**
     * 获取缓存内容
     *
     * @param key 缓存键
     * @return 缓存值，不存在时返回null
     */
    @Override
    public String get(String key) {
        return RedisUtils.getCacheObject(GlobalConstants.SOCIAL_AUTH_CODE_KEY + key);
    }

    /**
     * 检查缓存键是否存在且未过期
     *
     * @param key 缓存键
     * @return true：存在且未过期；false：不存在或已过期
     */
    @Override
    public boolean containsKey(String key) {
        return RedisUtils.hasKey(GlobalConstants.SOCIAL_AUTH_CODE_KEY + key);
    }
}
