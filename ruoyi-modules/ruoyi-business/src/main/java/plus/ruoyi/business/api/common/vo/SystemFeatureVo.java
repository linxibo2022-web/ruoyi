package plus.ruoyi.business.api.common.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 系统功能配置视图对象
 *
 * @author 抓蛙师
 */
@Data
public class SystemFeatureVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * langchain4j 是否启用
     */
    private Boolean langchain4jEnabled;

    /**
     * langchain4j 深度思考是否可用
     * <p>langchain4j 启用且至少一个模型提供商开启了 enableThinking 时为 true，
     * 前端据此决定是否展示「深度思考」控件
     */
    private Boolean langchain4jThinkingEnabled;

    /**
     * WebSocket 是否启用
     */
    private Boolean websocketEnabled;

    /**
     * SSE (Server-Sent Events) 是否启用
     */
    private Boolean sseEnabled;

    /**
     * 开放API是否启用
     */
    private Boolean openApiEnabled;

    /**
     * 开放API访问控制模式
     * ALL: 所有用户
     * ROLES: 指定角色
     * ADMIN: 仅管理员
     * SUPER_ADMIN: 仅超管
     */
    private String openApiAccessMode;

    /**
     * 开放API允许的角色列表(当mode=ROLES时有效)
     */
    private List<String> openApiAllowedRoles;
}
