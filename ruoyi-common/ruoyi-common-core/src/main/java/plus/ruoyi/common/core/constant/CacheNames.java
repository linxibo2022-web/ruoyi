package plus.ruoyi.common.core.constant;

/**
 * 缓存组名称常量
 * <p>
 * key 格式为 cacheNames#ttl#maxIdleTime#maxSize
 * <p>
 * ttl 过期时间 如果设置为0则不过期 默认为0
 * maxIdleTime 最大空闲时间 根据LRU算法清理空闲数据 如果设置为0则不检测 默认为0
 * maxSize 组最大长度 根据LRU算法清理溢出数据 如果设置为0则无限长 默认为0
 * <p>
 * 例子: test#60s、test#0#60s、test#0#1m#1000、test#1h#0#500
 *
 * @author Lion Li
 */
public interface CacheNames {


    /**
     * 字段映射 - 频繁访问的映射关系，适中缓存时间
     * TTL: 30秒, 空闲时间: 2分钟, 最大数量: 1000个
     */
    String FIELD_MAP = "field_map:cache#30s#2m#1000";

    // ==================== 系统基础配置缓存 ====================

    /**
     * 系统配置 - 相对稳定的配置信息，长期缓存
     * TTL: 5天, 空闲时间: 2天, 最大数量: 500个
     */
    String SYS_CONFIG = "sys_config#5d#2d#500";

    /**
     * 数据字典 - 相对稳定的字典数据，长期缓存
     * TTL: 1天, 空闲时间: 12小时, 最大数量: 1000个
     */
    String SYS_DICT = "sys_dict#1d#12h#1000";

    /**
     * 数据字典类型 - 变化较少的字典类型，长期缓存
     * TTL: 1天, 空闲时间: 12小时, 最大数量: 200个
     */
    String SYS_DICT_TYPE = "sys_dict_type#1d#12h#200";

    // ==================== 租户和客户端缓存 ====================

    /**
     * 租户信息 - 全局缓存，相对稳定
     * TTL: 5天, 空闲时间: 2天, 最大数量: 100个
     */
    String SYS_TENANT = GlobalConstants.GLOBAL_REDIS_KEY + "sys_tenant#5d#2d#100";

    /**
     * 用户账户信息 - 用户相关缓存，中等时长
     * TTL: 5天, 空闲时间: 2天, 最大数量: 5000个
     */
    String SYS_USER_NAME = "sys_user_name#5d#2d#5000";

    /**
     * 用户昵称 - 用户相关缓存，中等时长
     * TTL: 5天, 空闲时间: 2天, 最大数量: 5000个
     */
    String SYS_NICKNAME = "sys_nickname#5d#2d#5000";

    /**
     * 用户头像 - 用户相关缓存，中等时长
     * TTL: 5天, 空闲时间: 2天, 最大数量: 5000个
     */
    String SYS_AVATAR = "sys_avatar#5d#2d#5000";

    /**
     * 部门信息 - 组织架构缓存，相对稳定
     * TTL: 5天, 空闲时间: 2天, 最大数量: 1000个
     */
    String SYS_DEPT = "sys_dept#5d#2d#1000";

    // ==================== 权限相关缓存 ====================

    /**
     * 角色自定义权限 - 权限相关缓存，较长时间
     * TTL: 5天, 空闲时间: 2天, 最大数量: 2000个
     */
    String SYS_ROLE_CUSTOM = "sys_role_custom#5d#2d#2000";

    /**
     * 部门及子部门权限 - 层级权限缓存，较长时间
     * TTL: 5天, 空闲时间: 2天, 最大数量: 1000个
     */
    String SYS_DEPT_AND_CHILD = "sys_dept_and_child#5d#2d#1000";

    // ==================== 文件存储缓存 ====================

    /**
     * OSS配置 - 全局配置，长期缓存
     * TTL: 10天, 空闲时间: 5天, 最大数量: 50个
     */
    String SYS_OSS_CONFIG = GlobalConstants.GLOBAL_REDIS_KEY + "sys_oss_config#10d#5d#50";

    /**
     * OSS文件信息 - 文件元数据缓存，长期保存
     * TTL: 3天, 空闲时间: 1天, 最大数量: 10000个
     */
    String SYS_OSS = "sys_oss#3d#1d#10000";

    /**
     * OSS目录缓存 - 目录结构频繁变化，短期缓存
     * TTL: 10秒, 空闲时间: 5秒, 最大数量: 500个
     */
    String SYS_OSS_DIRECTORY = "sys_oss_directory#10s#5s#500";

    // ==================== 统计数据缓存 ====================

    /**
     * 首页统计数据 - 统计数据缓存，短期缓存
     * TTL: 20秒, 空闲时间: 10秒, 最大数量: 100个
     */
    String HOME_STATISTICS = "home_statistics#20s#10s#100";

}
