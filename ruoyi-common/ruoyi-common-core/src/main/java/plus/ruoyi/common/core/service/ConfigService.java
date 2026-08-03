package plus.ruoyi.common.core.service;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.BooleanUtil;
import plus.ruoyi.common.core.utils.StringUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * 通用 参数配置服务
 *
 * @author Lion Li
 */
public interface ConfigService {

    /**
     * 根据参数 key 获取参数值
     *
     * @param configKey 参数 key
     * @return 参数值
     */
    String getConfigValue(String configKey);

    /**
     * 根据参数 key 获取参数值，允许指定默认值
     *
     * @param configKey    参数 key
     * @param defaultValue 默认值
     * @return 参数值
     */
    default String getConfigValue(String configKey, String defaultValue) {
        String value = getConfigValue(configKey);
        return StringUtils.isNotBlank(value) ? value : defaultValue;
    }

    /**
     * 根据参数 key 获取布尔参数值
     *
     * @param configKey 参数 key
     * @return 布尔参数值
     */
    default boolean getBooleanValue(String configKey) {
        return BooleanUtil.toBoolean(getConfigValue(configKey));
    }

    /**
     * 根据参数 key 获取布尔参数值，允许指定默认值
     *
     * @param configKey    参数 key
     * @param defaultValue 默认值
     * @return 布尔参数值
     */
    default boolean getBooleanValue(String configKey, boolean defaultValue) {
        String value = getConfigValue(configKey);
        return StringUtils.isNotBlank(value) ? BooleanUtil.toBoolean(value) : defaultValue;
    }

    /**
     * 根据参数 key 获取整数值
     *
     * @param configKey 参数 key
     * @return Integer 值
     */
    default Integer getIntValue(String configKey) {
        return Convert.toInt(getConfigValue(configKey));
    }

    /**
     * 根据参数 key 获取整数值，允许指定默认值
     *
     * @param configKey    参数 key
     * @param defaultValue 默认值
     * @return Integer 值
     */
    default Integer getIntValue(String configKey, Integer defaultValue) {
        return Convert.toInt(getConfigValue(configKey), defaultValue);
    }

    /**
     * 根据参数 key 获取长整型值
     *
     * @param configKey 参数 key
     * @return Long 值
     */
    default Long getLongValue(String configKey) {
        return Convert.toLong(getConfigValue(configKey));
    }

    /**
     * 根据参数 key 获取长整型值，允许指定默认值
     *
     * @param configKey    参数 key
     * @param defaultValue 默认值
     * @return Long 值
     */
    default Long getLongValue(String configKey, Long defaultValue) {
        return Convert.toLong(getConfigValue(configKey), defaultValue);
    }

    /**
     * 根据参数 key 获取 BigDecimal 值
     *
     * @param configKey 参数 key
     * @return BigDecimal 值
     */
    default BigDecimal getDecimalValue(String configKey) {
        return Convert.toBigDecimal(getConfigValue(configKey));
    }

    /**
     * 根据参数 key 获取 BigDecimal 值，允许指定默认值
     *
     * @param configKey    参数 key
     * @param defaultValue 默认值
     * @return BigDecimal 值
     */
    default BigDecimal getDecimalValue(String configKey, BigDecimal defaultValue) {
        BigDecimal value = Convert.toBigDecimal(getConfigValue(configKey));
        return value != null ? value : defaultValue;
    }

    /**
     * 根据参数 key 获取 Map 类型的配置
     *
     * @param configKey 参数 key
     * @return Dict 对象，如果配置为空或无法解析，返回空 Dict
     */
    Dict getConfigMap(String configKey);

    /**
     * 根据参数 key 获取 Map 类型的配置列表
     *
     * @param configKey 参数 key
     * @return Dict 列表，如果配置为空或无法解析，返回空列表
     */
    List<Dict> getConfigArrayMap(String configKey);

    /**
     * 根据参数 key 获取指定类型的配置对象
     *
     * @param configKey 参数 key
     * @param clazz     目标对象类型
     * @param <T>       目标对象泛型
     * @return 对象实例，如果配置为空或无法解析，返回 null
     */
    <T> T getConfigObject(String configKey, Class<T> clazz);

    /**
     * 根据参数 key 获取指定类型的配置列表
     *
     * @param configKey 参数 key
     * @param clazz     目标元素类型
     * @param <T>       元素类型泛型
     * @return 指定类型列表，如果配置为空或无法解析，返回空列表
     */
    <T> List<T> getConfigArray(String configKey, Class<T> clazz);

}
