package plus.ruoyi.business.base.service;

import plus.ruoyi.business.base.domain.bo.AiChatBo;
import plus.ruoyi.business.base.domain.vo.AiChatVo;

/**
 * AI助手服务接口
 *
 * @author 抓蛙师
 * @date 2025-01-26
 */
public interface IAiService {

    /**
     * AI对话
     *
     * @param bo 对话请求参数
     * @return AI回复
     */
    AiChatVo aiChat(AiChatBo bo);

    /**
     * 文本优化
     *
     * @param bo 对话请求参数
     * @return 优化后的文本
     */
    AiChatVo aiOptimize(AiChatBo bo);

    /**
     * 数据生成
     *
     * @param bo 对话请求参数
     * @return 生成的数据
     */
    AiChatVo aiGenerate(AiChatBo bo);

    /**
     * 内容审核
     *
     * @param bo 对话请求参数
     * @return 审核结果
     */
    AiChatVo aiReview(AiChatBo bo);

    /**
     * 文本翻译
     *
     * @param bo 对话请求参数
     * @return 翻译结果
     */
    AiChatVo aiTranslate(AiChatBo bo);
}
