package plus.ruoyi.common.sensitive.handler;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.sensitive.annotation.Sensitive;
import plus.ruoyi.common.sensitive.core.SensitiveService;
import plus.ruoyi.common.sensitive.core.SensitiveStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;

import java.io.IOException;
import java.util.Objects;

/**
 * 数据脱敏JSON序列化处理器
 *
 * <p>自定义Jackson序列化器，在JSON序列化过程中自动处理标记了{@link Sensitive}注解的字段。
 *
 * <p>工作流程：
 * <ol>
 *   <li>解析字段上的{@link Sensitive}注解，获取脱敏策略和权限配置</li>
 *   <li>调用{@link SensitiveService}判断当前用户是否需要脱敏</li>
 *   <li>根据判断结果决定输出原始数据或脱敏后的数据</li>
 * </ol>
 *
 * <p>容错机制：
 * <ul>
 *   <li>当SensitiveService实现不存在时，默认不脱敏</li>
 *   <li>当权限判断异常时，记录错误日志并返回原始数据</li>
 * </ul>
 *
 * <p>注意：该处理器仅对String类型字段生效，其他类型字段将使用默认序列化器。
 *
 * @author Yjoioooo
 */
@Slf4j
public class SensitiveHandler extends JsonSerializer<String> implements ContextualSerializer {

    /** 脱敏策略 */
    private final SensitiveStrategy strategy;
    /** 角色标识数组 */
    private final String[] roleKey;
    /** 权限标识数组 */
    private final String[] perms;

    public SensitiveHandler() {
        this.strategy = null;
        this.roleKey = null;
        this.perms = null;
    }

    public SensitiveHandler(SensitiveStrategy strategy, String[] roleKey, String[] perms) {
        this.strategy = strategy;
        this.roleKey = roleKey;
        this.perms = perms;
    }

    /**
     * 执行JSON序列化
     *
     * <p>根据用户权限决定输出原始数据还是脱敏数据
     *
     * @param value 待序列化的字符串值
     * @param gen JSON生成器
     * @param serializers 序列化提供者
     * @throws IOException IO异常
     */
    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        try {
            SensitiveService sensitiveService = SpringUtils.getBean(SensitiveService.class);
            if (ObjectUtil.isNotNull(sensitiveService) && sensitiveService.isSensitive(roleKey, perms)) {
                // 需要脱敏，应用脱敏策略
                gen.writeString(strategy.desensitizer().apply(value));
            } else {
                // 不需要脱敏，输出原始数据
                gen.writeString(value);
            }
        } catch (BeansException e) {
            log.error("脱敏实现不存在, 采用默认处理 => {}", e.getMessage());
            // 容错处理：脱敏服务不存在时输出原始数据
            gen.writeString(value);
        }
    }

    /**
     * 创建上下文相关的序列化器
     *
     * <p>解析字段上的{@link Sensitive}注解，提取脱敏配置信息
     *
     * @param prov 序列化提供者
     * @param property Bean属性信息
     * @return 配置好的序列化器实例
     * @throws JsonMappingException JSON映射异常
     */
    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        Sensitive annotation = property.getAnnotation(Sensitive.class);
        // 检查注解存在且字段类型为String
        if (Objects.nonNull(annotation) && Objects.equals(String.class, property.getType().getRawClass())) {
            return new SensitiveHandler(annotation.strategy(), annotation.roleKey(), annotation.perms());
        }
        // 不符合条件，使用默认序列化器
        return prov.findValueSerializer(property.getType(), property);
    }
}
