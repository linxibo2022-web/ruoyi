package plus.ruoyi.common.tenant.handle;

import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.constant.GlobalConstants;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.redis.handler.KeyPrefixHandler;
import plus.ruoyi.common.tenant.helper.TenantHelper;

/**
 * 多租户Redis键前缀处理器
 * <p>
 * 继承KeyPrefixHandler，为Redis键添加租户前缀，
 * 实现Redis数据的租户隔离
 *
 * @author Lion Li
 */
@Slf4j
public class TenantKeyPrefixHandler extends KeyPrefixHandler {

    public TenantKeyPrefixHandler(String keyPrefix) {
        super(keyPrefix);
    }

    /**
     * 为Redis键添加租户前缀
     *
     * @param name 原始键名
     * @return 添加租户前缀后的键名
     */
    @Override
    public String map(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }

        try {
            // 检查是否忽略租户处理
            if (InterceptorIgnoreHelper.willIgnoreTenantLine("")) {
                return super.map(name);
            }
        } catch (NoClassDefFoundError ignore) {
            // 某些服务不需要MyBatis-Plus，忽略类找不到的错误
        }

        // 全局键不需要租户前缀
        if (StringUtils.contains(name, GlobalConstants.GLOBAL_REDIS_KEY)) {
            return super.map(name);
        }

        // 获取租户ID（永远不为null）
        String tenantId = TenantHelper.getTenantId();
        log.debug("Redis键添加租户前缀: {} -> {}:{}", name, tenantId, name);

        // 如果已经包含租户前缀，直接返回
        if (StringUtils.startsWith(name, tenantId + StringUtils.EMPTY)) {
            return super.map(name);
        }

        // 添加租户前缀
        return super.map(tenantId + ":" + name);
    }

    /**
     * 从Redis键中移除租户前缀
     *
     * @param name 包含前缀的键名
     * @return 移除租户前缀后的键名
     */
    @Override
    public String unmap(String name) {
        String unmap = super.unmap(name);
        if (StringUtils.isBlank(unmap)) {
            return null;
        }

        try {
            // 检查是否忽略租户处理
            if (InterceptorIgnoreHelper.willIgnoreTenantLine("")) {
                return unmap;
            }
        } catch (NoClassDefFoundError ignore) {
            // 某些服务不需要MyBatis-Plus，忽略类找不到的错误
        }

        // 全局键不处理租户前缀
        if (StringUtils.contains(name, GlobalConstants.GLOBAL_REDIS_KEY)) {
            return unmap;
        }

        // 获取租户ID（永远不为null）
        String tenantId = TenantHelper.getTenantId();

        // 移除租户前缀
        if (StringUtils.startsWith(unmap, tenantId + StringUtils.EMPTY)) {
            return unmap.substring((tenantId + ":").length());
        }

        return unmap;
    }
}
