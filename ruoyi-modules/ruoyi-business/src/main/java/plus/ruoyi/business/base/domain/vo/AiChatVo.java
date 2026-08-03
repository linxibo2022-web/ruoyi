package plus.ruoyi.business.base.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * AI对话视图对象
 *
 * @author 抓蛙师
 * @date 2025-01-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * AI回复内容
     */
    private String content;

    /**
     * Token使用情况
     */
    private TokenUsageVo tokenUsage;

    /**
     * 响应时间(毫秒)
     */
    private Long responseTime;

    /**
     * Token使用情况内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenUsageVo implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 输入Token数
         */
        private Integer promptTokens;

        /**
         * 输出Token数
         */
        private Integer completionTokens;

        /**
         * 总Token数
         */
        private Integer totalTokens;
    }
}
