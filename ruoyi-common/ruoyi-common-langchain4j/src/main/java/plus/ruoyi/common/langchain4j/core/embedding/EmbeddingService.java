package plus.ruoyi.common.langchain4j.core.embedding;

import cn.hutool.core.collection.CollUtil;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
//import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.output.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.langchain4j.config.LangChain4jProperties;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 向量嵌入服务
 * 将文本转换为向量表示
 *
 * @author 抓蛙师
 */
@Slf4j
public class EmbeddingService {

    private final LangChain4jProperties properties;
    private volatile EmbeddingModel embeddingModel;

    public EmbeddingService(LangChain4jProperties properties) {
        this.properties = properties;
    }

    /**
     * 获取或创建嵌入模型
     */
    private EmbeddingModel getEmbeddingModel() {
        if (embeddingModel == null) {
            synchronized (this) {
                if (embeddingModel == null) {
                    embeddingModel = createEmbeddingModel();
                }
            }
        }
        return embeddingModel;
    }

    /**
     * 创建嵌入模型
     */
    private EmbeddingModel createEmbeddingModel() {
        String provider = properties.getDefaultProvider();

        return switch (provider) {
            case "openai", "deepseek" -> createOpenAiEmbeddingModel();
            default -> createLocalEmbeddingModel();
        };
    }

    /**
     * 创建OpenAI嵌入模型
     */
    private EmbeddingModel createOpenAiEmbeddingModel() {
        var config = properties.getOpenai();

        return OpenAiEmbeddingModel.builder()
            .apiKey(config.getApiKey())
            .baseUrl(config.getBaseUrl())
            .modelName(properties.getEmbedding().getModelName())
            .timeout(properties.getTimeout())
            .maxRetries(properties.getMaxRetries())
            .logRequests(log.isDebugEnabled())
            .logResponses(log.isDebugEnabled())
            .build();
    }

    /**
     * 创建本地嵌入模型
     */
    private EmbeddingModel createLocalEmbeddingModel() {
        log.info("Using local embedding model: all-MiniLM-L6-v2");
        throw ServiceException.of("本地向量模式不支持，需打开向量依赖，注释本句代码，打开下方注释");
//        return new AllMiniLmL6V2EmbeddingModel();
    }

    /**
     * 嵌入单个文本
     */
    public List<Float> embed(String text) {
        Response<Embedding> response = getEmbeddingModel().embed(text);
        return response.content().vectorAsList();
    }

    /**
     * 嵌入多个文本
     */
    public List<List<Float>> embedAll(List<String> texts) {
        if (CollUtil.isEmpty(texts)) {
            return List.of();
        }

        // 将String转换为TextSegment
        List<TextSegment> segments = texts.stream()
            .map(TextSegment::from)
            .collect(Collectors.toList());

        Response<List<Embedding>> response = getEmbeddingModel().embedAll(segments);

        return response.content().stream()
            .map(Embedding::vectorAsList)
            .collect(Collectors.toList());
    }

    /**
     * 嵌入文本段落
     */
    public List<Float> embedSegment(TextSegment segment) {
        Response<Embedding> response = getEmbeddingModel().embed(segment);
        return response.content().vectorAsList();
    }

    /**
     * 批量嵌入文本段落
     */
    public List<Embedding> embedSegments(List<TextSegment> segments) {
        if (CollUtil.isEmpty(segments)) {
            return List.of();
        }

        int batchSize = properties.getEmbedding().getBatchSize();
        List<Embedding> allEmbeddings = CollUtil.newArrayList();

        // 分批处理
        for (int i = 0; i < segments.size(); i += batchSize) {
            int end = Math.min(i + batchSize, segments.size());
            List<TextSegment> batch = segments.subList(i, end);

            Response<List<Embedding>> response = getEmbeddingModel().embedAll(batch);
            allEmbeddings.addAll(response.content());

            log.debug("Embedded batch {}-{} of {}", i, end, segments.size());
        }

        return allEmbeddings;
    }

    /**
     * 计算余弦相似度
     */
    public double cosineSimilarity(List<Float> vector1, List<Float> vector2) {
        if (vector1.size() != vector2.size()) {
            throw new IllegalArgumentException("Vector dimensions must match");
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vector1.size(); i++) {
            dotProduct += vector1.get(i) * vector2.get(i);
            norm1 += vector1.get(i) * vector1.get(i);
            norm2 += vector2.get(i) * vector2.get(i);
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
