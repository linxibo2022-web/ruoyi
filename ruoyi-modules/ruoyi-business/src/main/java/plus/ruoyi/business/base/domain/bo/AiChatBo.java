package plus.ruoyi.business.base.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * AI对话业务对象
 *
 * @author 抓蛙师
 * @date 2025-01-26
 */
@Data
public class AiChatBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户消息
     */
    @NotBlank(message = "消息内容不能为空")
    private String message;

    /**
     * AI功能类型
     * optimize-文本优化, generate-数据生成, review-内容审核, translate-翻译
     */
    private String aiFeature;

    /**
     * 模型提供商 (deepseek, qianwen, openai, claude等)
     */
    private String provider;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 温度参数 (0-2, 默认0.7)
     */
    private Double temperature;

    /**
     * 最大Token数
     */
    private Integer maxTokens;

    /**
     * 上下文信息 (用于传递表单数据等业务上下文)
     */
    private Map<String, Object> context;
}
