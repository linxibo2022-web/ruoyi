package plus.ruoyi.common.serialmap.core.impl;

import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.utils.MessageUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.serialmap.annotation.SerialMapType;
import plus.ruoyi.common.serialmap.constant.SerialMapConstant;
import plus.ruoyi.common.serialmap.core.SerialMapInterface;
import org.springframework.stereotype.Component;

/**
 * 国际化翻译转换器
 *
 * <p>基于Spring MessageSource实现多语言翻译，支持占位符参数替换
 *
 * <p>使用场景：
 * <ul>
 *   <li>状态码国际化：订单状态、用户状态等</li>
 *   <li>字典翻译：将字典值翻译为对应语言的标签</li>
 *   <li>错误消息：系统错误码翻译</li>
 *   <li>业务术语：专业术语的多语言支持</li>
 * </ul>
 *
 * <p>支持的参数格式：
 * <ul>
 *   <li>简单翻译：直接使用key值查找消息</li>
 *   <li>前缀翻译：prefix.key格式，适用于分组消息</li>
 *   <li>带参数翻译：支持消息模板参数替换</li>
 *   <li>默认值翻译：翻译失败时返回指定默认值</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>
 * // 基础翻译
 * {@code @SerialMap(converter = SerialMapConstant.I18N_TRANSLATE)}
 * private String statusText;
 *
 * // 带前缀翻译
 * {@code @SerialMap(converter = SerialMapConstant.I18N_TRANSLATE, param = "order.status")}
 * private String orderStatus;
 *
 * // 字典国际化翻译
 * {@code @SerialMap(converter = SerialMapConstant.I18N_TRANSLATE, param = "dict.sys_user_gender")}
 * private String sexLabel;
 * </pre>
 *
 * @author Lion Li
 */
@Slf4j
@SerialMapType(type = SerialMapConstant.I18N_TRANSLATE)
public class I18nTranslateImpl implements SerialMapInterface<String> {

    @Override
    public String convert(Object key, String param) {
        if (key == null) {
            return null;
        }

        String keyStr = key.toString();
        if (StringUtils.isBlank(keyStr)) {
            return keyStr;
        }

        try {
            // 构建消息键
            String messageKey = buildMessageKey(keyStr, param);

            // 获取国际化翻译
            String translatedMessage = MessageUtils.message(messageKey);

            // 如果翻译结果等于消息键，说明未找到翻译，尝试其他策略
            if (messageKey.equals(translatedMessage)) {
                return handleMissingTranslation(keyStr, param, messageKey);
            }

            log.debug("国际化翻译成功: {} -> {}", messageKey, translatedMessage);
            return translatedMessage;

        } catch (Exception e) {
            log.error("国际化翻译失败: key={}, param={}, error={}", key, param, e.getMessage());
            return keyStr; // 翻译失败时返回原值
        }
    }

    /**
     * 构建消息键
     *
     * @param key   原始键值
     * @param param 参数配置
     * @return 完整的消息键
     */
    private String buildMessageKey(String key, String param) {
        if (StringUtils.isBlank(param)) {
            // 无参数时，直接使用key作为消息键
            return key;
        }

        // 支持多种参数格式
        if (param.contains("{key}")) {
            // 模板格式：user.status.{key} -> user.status.active
            return param.replace("{key}", key);
        } else if (param.endsWith(".")) {
            // 前缀格式：order.status. -> order.status.pending
            return param + key;
        } else {
            // 简单前缀：order.status -> order.status.pending
            return param + "." + key;
        }
    }

    /**
     * 处理翻译缺失的情况
     *
     * @param originalKey 原始键值
     * @param param       参数配置
     * @param messageKey  消息键
     * @return 处理后的结果
     */
    private String handleMissingTranslation(String originalKey, String param, String messageKey) {
        // 尝试使用原始key进行翻译
        if (StringUtils.isNotBlank(param) && !originalKey.equals(messageKey)) {
            String fallbackTranslation = MessageUtils.message(originalKey);
            if (!originalKey.equals(fallbackTranslation)) {
                log.debug("使用原始key翻译成功: {} -> {}", originalKey, fallbackTranslation);
                return fallbackTranslation;
            }
        }

        // 尝试提取字典翻译
        if (StringUtils.isNotBlank(param) && param.startsWith("dict.")) {
            return handleDictTranslation(originalKey, param);
        }

        log.debug("未找到翻译: messageKey={}, 返回原值: {}", messageKey, originalKey);
        return originalKey;
    }

    /**
     * 处理字典翻译
     *
     * @param key   字典值
     * @param param 参数配置 (格式: dict.dictType)
     * @return 翻译结果
     */
    private String handleDictTranslation(String key, String param) {
        try {
            // 从param中提取字典类型
            String dictType = param.substring(5); // 去掉 "dict." 前缀

            // 构建字典翻译键：dict.sys_user_gender.1 -> "男"
            String dictKey = "dict." + dictType + "." + key;
            String dictTranslation = MessageUtils.message(dictKey);

            if (!dictKey.equals(dictTranslation)) {
                log.debug("字典翻译成功: {} -> {}", dictKey, dictTranslation);
                return dictTranslation;
            }
        } catch (Exception e) {
            log.warn("字典翻译处理失败: key={}, param={}, error={}", key, param, e.getMessage());
        }

        return key;
    }
}
