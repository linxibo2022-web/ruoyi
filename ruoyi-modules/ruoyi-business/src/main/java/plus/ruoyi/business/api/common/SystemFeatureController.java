package plus.ruoyi.business.api.common;

import cn.dev33.satoken.annotation.SaIgnore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.business.api.common.vo.SystemFeatureVo;
import plus.ruoyi.common.core.utils.ObjectUtils;
import plus.ruoyi.common.langchain4j.config.LangChain4jProperties;
import plus.ruoyi.common.langchain4j.config.LangChain4jProperties.ModelConfig;
import plus.ruoyi.common.openapi.config.OpenApiProperties;
import plus.ruoyi.common.sse.config.SseProperties;
import plus.ruoyi.common.websocket.config.properties.WebSocketProperties;

/**
 * 系统功能配置接口
 *
 * @author 抓蛙师
 */
@SaIgnore
@RestController
@RequestMapping("/common/system")
public class SystemFeatureController {

    /** langchain4j 配置 */
    @Autowired(required = false)
    private LangChain4jProperties langChain4jProperties;

    /** WebSocket 配置 */
    @Autowired(required = false)
    private WebSocketProperties webSocketProperties;

    /** SSE 配置 */
    @Autowired(required = false)
    private SseProperties sseProperties;

    /** 开放 API 配置 */
    @Autowired(required = false)
    private OpenApiProperties openApiProperties;

    /**
     * 获取系统功能开关
     *
     * @return 功能配置
     */
    @GetMapping("/features")
    public R<SystemFeatureVo> getFeatures() {
        SystemFeatureVo features = new SystemFeatureVo();

        // 检查 langchain4j 是否启用
        Boolean langchain4jEnabled = ObjectUtils.getIfNotNull(langChain4jProperties,
            LangChain4jProperties::getEnabled);
        features.setLangchain4jEnabled(langchain4jEnabled);
        // 深度思考可用 = langchain4j 启用 且 至少一个 provider 开了 enableThinking
        features.setLangchain4jThinkingEnabled(
            Boolean.TRUE.equals(langchain4jEnabled) && anyProviderThinkingEnabled());

        // 检查 WebSocket 是否启用
        features.setWebsocketEnabled(ObjectUtils.getIfNotNull(webSocketProperties,
            WebSocketProperties::getEnabled));

        // 检查 SSE 是否启用
        features.setSseEnabled(ObjectUtils.getIfNotNull(sseProperties,
            SseProperties::getEnabled));

        // 检查开放API是否启用及其访问控制配置
        if (openApiProperties != null) {
            features.setOpenApiEnabled(openApiProperties.getEnabled());
            if (openApiProperties.getEnabled()) {
                OpenApiProperties.AccessControl accessControl = openApiProperties.getAccessControl();
                features.setOpenApiAccessMode(accessControl.getMode().name());
                features.setOpenApiAllowedRoles(accessControl.getAllowedRoles());
            }
        }

        return R.ok(features);
    }

    /**
     * 是否存在任一模型提供商开启了深度思考
     */
    private boolean anyProviderThinkingEnabled() {
        if (langChain4jProperties == null) {
            return false;
        }
        return isThinkingOn(langChain4jProperties.getDeepseek())
            || isThinkingOn(langChain4jProperties.getQianwen())
            || isThinkingOn(langChain4jProperties.getClaude())
            || isThinkingOn(langChain4jProperties.getOpenai())
            || isThinkingOn(langChain4jProperties.getOllama());
    }

    private boolean isThinkingOn(ModelConfig config) {
        return config != null && Boolean.TRUE.equals(config.getEnableThinking());
    }
}
