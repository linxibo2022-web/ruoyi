package plus.ruoyi.common.redis.handler;

import plus.ruoyi.common.core.utils.StringUtils;
import org.redisson.api.NameMapper;

/**
 * Redis缓存key前缀处理器
 * <p>
 * 实现Redisson的NameMapper接口，为Redis key统一添加/移除前缀
 * 避免不同应用或环境的key冲突，支持空前缀配置
 *
 * @author Lion Li
 * @date 2022/7/14 17:44
 * @since 4.3.0
 */
public class KeyPrefixHandler implements NameMapper {

    /**
     * Redis key前缀，包含分隔符（如：myapp:）
     */
    private final String keyPrefix;

    /**
     * 构造函数
     *
     * @param keyPrefix 前缀字符串，为空时不添加前缀，非空时自动添加":"分隔符
     */
    public KeyPrefixHandler(String keyPrefix) {
        // 前缀为空则返回空前缀，否则添加冒号分隔符
        this.keyPrefix = StringUtils.isBlank(keyPrefix) ? "" : keyPrefix + ":";
    }

    /**
     * 为Redis key添加前缀
     *
     * @param name 原始业务key
     * @return 添加前缀后的完整key，如果key已包含前缀则不重复添加
     */
    @Override
    public String map(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        // 仅在配置了前缀且key未包含前缀时才添加
        if (StringUtils.isNotBlank(keyPrefix) && !name.startsWith(keyPrefix)) {
            return keyPrefix + name;
        }
        return name;
    }

    /**
     * 从Redis key中去除前缀
     *
     * @param name 包含前缀的完整key
     * @return 去除前缀后的业务key，如果key不包含配置前缀则直接返回
     */
    @Override
    public String unmap(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        // 仅在配置了前缀且key包含该前缀时才去除
        if (StringUtils.isNotBlank(keyPrefix) && name.startsWith(keyPrefix)) {
            return name.substring(keyPrefix.length());
        }
        return name;
    }
}
