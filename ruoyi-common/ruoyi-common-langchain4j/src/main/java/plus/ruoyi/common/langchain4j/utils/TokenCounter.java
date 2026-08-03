package plus.ruoyi.common.langchain4j.utils;

import cn.hutool.core.util.StrUtil;
import dev.langchain4j.model.TokenCountEstimator;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;

/**
 * Token计数工具类（langchain4j 1.x：Tokenizer 已被 TokenCountEstimator 取代）
 *
 * @author 抓蛙师
 */
public class TokenCounter {

    /**
     * 默认估算器：使用 gpt-4o 系列编码（与多数主流模型 BPE 相近）
     */
    private static final TokenCountEstimator DEFAULT_ESTIMATOR = new OpenAiTokenCountEstimator("gpt-4o");

    /**
     * 估算文本的Token数量
     * 使用OpenAI的tokenizer，适用于大多数模型
     */
    public static int estimateTokenCount(String text) {
        if (StrUtil.isBlank(text)) {
            return 0;
        }
        return DEFAULT_ESTIMATOR.estimateTokenCountInText(text);
    }

    /**
     * 估算多个文本的总Token数
     */
    public static int estimateTokenCount(String... texts) {
        int total = 0;
        for (String text : texts) {
            total += estimateTokenCount(text);
        }
        return total;
    }

    /**
     * 检查文本是否超过Token限制
     */
    public static boolean exceedsLimit(String text, int limit) {
        return estimateTokenCount(text) > limit;
    }

    /**
     * 截断文本以适应Token限制
     */
    public static String truncateToTokenLimit(String text, int maxTokens) {
        if (StrUtil.isBlank(text)) {
            return text;
        }

        int currentTokens = estimateTokenCount(text);
        if (currentTokens <= maxTokens) {
            return text;
        }

        // 粗略估算需要保留的字符比例
        double ratio = (double) maxTokens / currentTokens;
        int targetLength = (int) (text.length() * ratio * 0.9); // 留10%余量

        return text.substring(0, Math.min(targetLength, text.length()));
    }

    /**
     * 计算对话历史的总Token数（使用 1.x TokenCountEstimator 的 estimateTokenCountInMessages）
     */
    public static int calculateConversationTokens(dev.langchain4j.data.message.ChatMessage... messages) {
        if (messages == null || messages.length == 0) {
            return 0;
        }
        return DEFAULT_ESTIMATOR.estimateTokenCountInMessages(java.util.Arrays.asList(messages));
    }

    /**
     * 根据中文字符粗略估算Token（1个汉字约等于2个token）
     */
    public static int estimateChineseTokens(String text) {
        if (StrUtil.isBlank(text)) {
            return 0;
        }

        int chineseCount = 0;
        int otherCount = 0;

        for (char c : text.toCharArray()) {
            if (isChinese(c)) {
                chineseCount++;
            } else {
                otherCount++;
            }
        }

        // 中文字符约2个token，其他字符约0.25个token（4个字符1个token）
        return (int) (chineseCount * 2 + otherCount * 0.25);
    }

    /**
     * 判断字符是否为中文
     */
    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B;
    }
}
