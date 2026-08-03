package plus.ruoyi.common.langchain4j.core.rag;

import cn.hutool.core.collection.CollUtil;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import plus.ruoyi.common.langchain4j.config.LangChain4jProperties;
import plus.ruoyi.common.langchain4j.core.embedding.EmbeddingService;
import plus.ruoyi.common.langchain4j.domain.dto.DocumentReference;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG检索增强生成服务
 * 提供文档向量化、检索等功能
 *
 * @author 抓蛙师
 */
@Slf4j
public class RagService {

    private final LangChain4jProperties properties;
    private final EmbeddingService embeddingService;

    // 默认使用内存存储，实际使用中应该根据配置选择
    private final EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

    public RagService(LangChain4jProperties properties, EmbeddingService embeddingService) {
        this.properties = properties;
        this.embeddingService = embeddingService;
    }

    /**
     * 添加文档到向量库
     */
    public void addDocument(String documentId, String content) {
        // 分割文档
        List<TextSegment> segments = splitDocument(content, documentId);

        // 批量向量化
        List<Embedding> embeddings = embeddingService.embedSegments(segments);

        // 存储到向量库
        embeddingStore.addAll(embeddings, segments);

        log.info("Added document {} with {} segments", documentId, segments.size());
    }

    /**
     * 批量添加文档
     */
    public void addDocuments(List<Document> documents) {
        for (Document document : documents) {
            String documentId = document.metadata().getString("id");
            addDocument(documentId, document.text());
        }
    }

    /**
     * 检索相关文档
     */
    public List<DocumentReference> retrieve(String query, int maxResults) {
        // 向量化查询
        List<Float> queryEmbedding = embeddingService.embed(query);

        // 转换为float数组
        float[] floatArray = new float[queryEmbedding.size()];
        for (int i = 0; i < queryEmbedding.size(); i++) {
            floatArray[i] = queryEmbedding.get(i);
        }
        Embedding embedding = new Embedding(floatArray);

        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.search(
            EmbeddingSearchRequest.builder()
                .queryEmbedding(embedding)
                .maxResults(maxResults)
                .minScore(properties.getRag().getMinScore())
                .build()
        ).matches();

        // 转换为响应对象
        return matches.stream()
            .map(this::convertToReference)
            .collect(Collectors.toList());
    }

    /**
     * 构建RAG提示词
     */
    public String buildRagPrompt(String query, List<DocumentReference> references) {
        if (CollUtil.isEmpty(references)) {
            return query;
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("参考以下信息回答问题：\n\n");

        for (int i = 0; i < references.size(); i++) {
            DocumentReference ref = references.get(i);
            prompt.append(String.format("[文档%d] %s\n", i + 1, ref.getContent()));
        }

        prompt.append("\n问题：").append(query);
        prompt.append("\n\n请基于以上参考信息，准确、详细地回答问题。如果参考信息不足以回答问题，请明确说明。");

        return prompt.toString();
    }

    /**
     * 分割文档
     */
    private List<TextSegment> splitDocument(String content, String documentId) {
        // 创建元数据
        Metadata metadata = new Metadata();
        metadata.put("id", documentId);

        Document document = Document.from(content, metadata);

        DocumentSplitter splitter = DocumentSplitters.recursive(
            properties.getRag().getChunkSize(),
            properties.getRag().getChunkOverlap()
        );

        return splitter.split(document);
    }

    /**
     * 转换为文档引用
     */
    private DocumentReference convertToReference(EmbeddingMatch<TextSegment> match) {
        TextSegment segment = match.embedded();

        DocumentReference reference = new DocumentReference();
        reference.setContent(segment.text());
        reference.setScore(match.score());

        // 如果有文档ID元数据
        if (segment.metadata() != null && segment.metadata().containsKey("id")) {
            reference.setDocumentId(Long.parseLong(segment.metadata().getString("id")));
        }

        return reference;
    }

    /**
     * 删除文档
     */
    public void removeDocument(String documentId) {
        // TODO: 实现删除逻辑
        log.warn("Remove document not implemented yet");
    }

    /**
     * 清空向量库
     */
    public void clearAll() {
        embeddingStore.removeAll();
        log.info("Cleared all embeddings from store");
    }
}
