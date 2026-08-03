package plus.ruoyi.common.tenant.manager;

import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.constant.GlobalConstants;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.redis.manager.PlusSpringCacheManager;
import plus.ruoyi.common.tenant.helper.TenantHelper;
import org.springframework.cache.Cache;

/**
 * 多租户Spring缓存管理器
 * <p>
 * 重写cacheName处理方法，为缓存名称添加租户前缀，
 * 实现Spring Cache的多租户隔离
 *
 * @author Lion Li
 */
@Slf4j
public class TenantSpringCacheManager extends PlusSpringCacheManager {

    public TenantSpringCacheManager() {
    }

    /**
     * 获取缓存实例
     * <p>
     * 自动为缓存名称添加租户前缀，实现缓存的租户隔离
     *
     * @param name 缓存名称
     * @return 缓存实例
     */
    @Override
    public Cache getCache(String name) {
        // 检查是否忽略租户处理
        if (InterceptorIgnoreHelper.willIgnoreTenantLine("")) {
            return super.getCache(name);
        }

        // 全局缓存不添加租户前缀
        if (StringUtils.contains(name, GlobalConstants.GLOBAL_REDIS_KEY)) {
            return super.getCache(name);
        }

        // 获取租户ID（永远不为null）
        String tenantId = TenantHelper.getTenantId();

        // 如果已经包含租户前缀，直接返回
        if (StringUtils.startsWith(name, tenantId)) {
            return super.getCache(name);
        }

        // 添加租户前缀
        return super.getCache(tenantId + ":" + name);
    }
}
