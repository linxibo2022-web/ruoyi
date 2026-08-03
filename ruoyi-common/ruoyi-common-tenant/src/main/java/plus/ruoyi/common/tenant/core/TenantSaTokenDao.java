package plus.ruoyi.common.tenant.core;

import plus.ruoyi.common.core.constant.GlobalConstants;
import plus.ruoyi.common.redis.utils.RedisUtils;
import plus.ruoyi.common.satoken.core.dao.PlusSaTokenDao;

import java.time.Duration;
import java.util.List;

/**
 * 多租户SaToken认证数据持久层
 * <p>
 * 继承PlusSaTokenDao并为所有Redis键添加全局前缀，
 * 确保SaToken的认证数据支持多租户隔离
 *
 * @author Lion Li
 */
public class TenantSaTokenDao extends PlusSaTokenDao {

    /**
     * 获取字符串值
     *
     * @param key 键名
     * @return 字符串值
     */
    @Override
    public String get(String key) {
        return super.get(GlobalConstants.GLOBAL_REDIS_KEY + key);
    }

    /**
     * 设置字符串值
     *
     * @param key     键名
     * @param value   值
     * @param timeout 过期时间（秒）
     */
    @Override
    public void set(String key, String value, long timeout) {
        super.set(GlobalConstants.GLOBAL_REDIS_KEY + key, value, timeout);
    }

    /**
     * 更新字符串值（过期时间不变）
     *
     * @param key   键名
     * @param value 新值
     */
    @Override
    public void update(String key, String value) {
        long expire = getTimeout(key);
        // -2表示键不存在
        if (expire == NOT_VALUE_EXPIRE) {
            return;
        }
        this.set(key, value, expire);
    }

    /**
     * 删除键值对
     *
     * @param key 键名
     */
    @Override
    public void delete(String key) {
        super.delete(GlobalConstants.GLOBAL_REDIS_KEY + key);
    }

    /**
     * 获取键的剩余存活时间
     *
     * @param key 键名
     * @return 剩余时间（秒）
     */
    @Override
    public long getTimeout(String key) {
        return super.getTimeout(GlobalConstants.GLOBAL_REDIS_KEY + key);
    }

    /**
     * 修改键的剩余存活时间
     *
     * @param key     键名
     * @param timeout 新的过期时间（秒）
     */
    @Override
    public void updateTimeout(String key, long timeout) {
        // 判断是否要设置为永久
        if (timeout == NEVER_EXPIRE) {
            long expire = getTimeout(key);
            if (expire == NEVER_EXPIRE) {
                // 已经是永久，不做处理
                return;
            } else {
                // 重新设置为永久
                this.set(key, this.get(key), timeout);
            }
            return;
        }
        RedisUtils.expire(GlobalConstants.GLOBAL_REDIS_KEY + key, Duration.ofSeconds(timeout));
    }

    /**
     * 获取对象值
     *
     * @param key 键名
     * @return 对象值
     */
    @Override
    public Object getObject(String key) {
        return super.getObject(GlobalConstants.GLOBAL_REDIS_KEY + key);
    }

    /**
     * 获取指定类型的对象值
     *
     * @param key       键名
     * @param classType 目标类型
     * @return 指定类型的对象
     */
    @Override
    public <T> T getObject(String key, Class<T> classType) {
        return super.getObject(GlobalConstants.GLOBAL_REDIS_KEY + key, classType);
    }

    /**
     * 设置对象值
     *
     * @param key     键名
     * @param object  对象值
     * @param timeout 过期时间（秒）
     */
    @Override
    public void setObject(String key, Object object, long timeout) {
        super.setObject(GlobalConstants.GLOBAL_REDIS_KEY + key, object, timeout);
    }

    /**
     * 更新对象值（过期时间不变）
     *
     * @param key    键名
     * @param object 新对象值
     */
    @Override
    public void updateObject(String key, Object object) {
        long expire = getObjectTimeout(key);
        // -2表示键不存在
        if (expire == NOT_VALUE_EXPIRE) {
            return;
        }
        this.setObject(key, object, expire);
    }

    /**
     * 删除对象
     *
     * @param key 键名
     */
    @Override
    public void deleteObject(String key) {
        super.deleteObject(GlobalConstants.GLOBAL_REDIS_KEY + key);
    }

    /**
     * 获取对象的剩余存活时间
     *
     * @param key 键名
     * @return 剩余时间（秒）
     */
    @Override
    public long getObjectTimeout(String key) {
        return super.getObjectTimeout(GlobalConstants.GLOBAL_REDIS_KEY + key);
    }

    /**
     * 修改对象的剩余存活时间
     *
     * @param key     键名
     * @param timeout 新的过期时间（秒）
     */
    @Override
    public void updateObjectTimeout(String key, long timeout) {
        // 判断是否要设置为永久
        if (timeout == NEVER_EXPIRE) {
            long expire = getObjectTimeout(key);
            if (expire == NEVER_EXPIRE) {
                // 已经是永久，不做处理
                return;
            } else {
                // 重新设置为永久
                this.setObject(key, this.getObject(key), timeout);
            }
            return;
        }
        RedisUtils.expire(GlobalConstants.GLOBAL_REDIS_KEY + key, Duration.ofSeconds(timeout));
    }

    /**
     * 搜索数据
     *
     * @param prefix   键前缀
     * @param keyword  关键词
     * @param start    开始位置
     * @param size     返回数量
     * @param sortType 排序类型
     * @return 搜索结果列表
     */
    @Override
    public List<String> searchData(String prefix, String keyword, int start, int size, boolean sortType) {
        return super.searchData(GlobalConstants.GLOBAL_REDIS_KEY + prefix, keyword, start, size, sortType);
    }
}
