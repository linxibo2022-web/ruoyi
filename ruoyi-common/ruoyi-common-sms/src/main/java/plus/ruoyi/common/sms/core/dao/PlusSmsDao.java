package plus.ruoyi.common.sms.core.dao;

import plus.ruoyi.common.core.constant.GlobalConstants;
import plus.ruoyi.common.redis.utils.RedisUtils;
import org.dromara.sms4j.api.dao.SmsDao;

import java.time.Duration;

/**
 * 短信缓存数据访问对象实现类
 *
 * 使用框架自带RedisUtils实现，确保协议统一
 * 主要用于短信重试和拦截的缓存管理
 *
 * @author Feng
 */
public class PlusSmsDao implements SmsDao {

    /**
     * 存储键值对，指定过期时间
     *
     * @param key       缓存键
     * @param value     缓存值
     * @param cacheTime 缓存时间（单位：秒）
     */
    @Override
    public void set(String key, Object value, long cacheTime) {
        RedisUtils.setCacheObject(GlobalConstants.GLOBAL_REDIS_KEY + key, value, Duration.ofSeconds(cacheTime));
    }

    /**
     * 存储键值对，永久缓存
     *
     * @param key   缓存键
     * @param value 缓存值
     */
    @Override
    public void set(String key, Object value) {
        RedisUtils.setCacheObject(GlobalConstants.GLOBAL_REDIS_KEY + key, value, true);
    }

    /**
     * 根据键获取缓存值
     *
     * @param key 缓存键
     * @return 缓存值，不存在时返回null
     */
    @Override
    public Object get(String key) {
        return RedisUtils.getCacheObject(GlobalConstants.GLOBAL_REDIS_KEY + key);
    }

    /**
     * 删除指定键的缓存
     *
     * @param key 缓存键
     * @return 删除操作结果
     */
    @Override
    public Object remove(String key) {
        return RedisUtils.deleteObject(GlobalConstants.GLOBAL_REDIS_KEY + key);
    }

    /**
     * 清空所有短信相关缓存
     *
     * 删除所有以"sms:"开头的缓存键
     */
    @Override
    public void clean() {
        RedisUtils.deleteKeys(GlobalConstants.GLOBAL_REDIS_KEY + "sms:*");
    }

}
