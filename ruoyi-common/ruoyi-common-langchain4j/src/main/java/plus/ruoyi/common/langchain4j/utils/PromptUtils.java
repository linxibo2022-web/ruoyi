package plus.ruoyi.common.langchain4j.utils;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 提示词工具类
 *
 * @author 抓蛙师
 */
@Slf4j
public class PromptUtils {

    /**
     * 从资源文件加载提示词模板
     */
    public static String loadPromptTemplate(String templateName) {
        String path = "prompts/" + templateName;
        try (InputStream is = PromptUtils.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                log.warn("Prompt template not found: {}", path);
                return "";
            }
            return IoUtil.read(is, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Failed to load prompt template: {}", path, e);
            return "";
        }
    }

    /**
     * 替换提示词变量
     *
     * @param template  模板字符串，使用 {{variableName}} 作为占位符
     * @param variables 变量映射
     * @return 替换后的字符串
     */
    public static String fillTemplate(String template, Map<String, String> variables) {
        if (StrUtil.isBlank(template) || variables == null || variables.isEmpty()) {
            return template;
        }

        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            result = result.replace(placeholder, entry.getValue());
        }

        return result;
    }

    /**
     * 构建系统提示词
     */
    public static String buildSystemPrompt(String role, String context) {
        StringBuilder prompt = new StringBuilder();

        if (StrUtil.isNotBlank(role)) {
            prompt.append("你是").append(role).append("。\n");
        }

        if (StrUtil.isNotBlank(context)) {
            prompt.append(context);
        }

        return prompt.toString();
    }

    /**
     * 构建少样本提示词(Few-shot)
     */
    public static String buildFewShotPrompt(String instruction, List<Example> examples, String query) {
        StringBuilder prompt = new StringBuilder();

        if (StrUtil.isNotBlank(instruction)) {
            prompt.append(instruction).append("\n\n");
        }

        if (examples != null && !examples.isEmpty()) {
            prompt.append("示例：\n");
            for (int i = 0; i < examples.size(); i++) {
                Example example = examples.get(i);
                prompt.append("示例").append(i + 1).append("：\n");
                prompt.append("输入：").append(example.input()).append("\n");
                prompt.append("输出：").append(example.output()).append("\n\n");
            }
        }

        prompt.append("请按照以上示例的格式处理以下输入：\n");
        prompt.append("输入：").append(query);

        return prompt.toString();
    }

    /**
     * 构建思维链提示词(Chain of Thought)
     */
    public static String buildCoTPrompt(String query) {
        return query + "\n\n请一步一步地思考并解释你的推理过程。";
    }

    /**
     * 构建角色扮演提示词
     */
    public static String buildRolePlayPrompt(String role, String personality, String task) {
        return StrUtil.format(
                "你现在扮演{}，性格特点是{}。\n现在请你完成以下任务：{}",
                role, personality, task
        );
    }

    /**
     * 限制输出格式
     */
    public static String addFormatConstraint(String prompt, String format) {
        return prompt + "\n\n请严格按照以下格式输出：\n" + format;
    }

    /**
     * 添加输出长度限制
     */
    public static String addLengthConstraint(String prompt, int maxWords) {
        return prompt + StrUtil.format("\n\n请将回答控制在{}字以内。", maxWords);
    }

    /**
     * 示例记录
     */
    public record Example(String input, String output) {
    }
}
