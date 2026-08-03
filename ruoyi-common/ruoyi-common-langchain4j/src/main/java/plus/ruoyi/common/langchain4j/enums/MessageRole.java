package plus.ruoyi.common.langchain4j.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息角色枚举
 */
@Getter
@AllArgsConstructor
public enum MessageRole {
    SYSTEM("system", "系统"),
    USER("user", "用户"),
    ASSISTANT("assistant", "助手"),
    FUNCTION("function", "函数");

    private final String code;
    private final String name;
}
