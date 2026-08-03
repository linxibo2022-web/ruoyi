package plus.ruoyi.common.serialmap.core.handler;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.core.utils.reflect.ReflectUtils;
import plus.ruoyi.common.serialmap.annotation.SerialMap;
import plus.ruoyi.common.serialmap.core.SerialMapInterface;
import plus.ruoyi.common.serialmap.core.context.SerialMapContext;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 序列化映射处理器
 *
 * <p>核心的JSON序列化处理器，负责在序列化过程中执行字段映射和数据转换。
 *
 * <p>主要功能：
 * <ul>
 *   <li>解析{@link SerialMap}注解配置</li>
 *   <li>查找并执行对应的转换器</li>
 *   <li>处理源字段值获取</li>
 *   <li>管理序列化上下文</li>
 *   <li>提供异常处理和容错机制</li>
 * </ul>
 *
 * <p>工作流程：
 * <ol>
 *   <li>创建序列化上下文，存储字段信息</li>
 *   <li>根据注解配置查找对应的转换器</li>
 *   <li>检测转换器能力，选择合适的转换方法</li>
 *   <li>处理source字段，获取实际转换值</li>
 *   <li>执行转换器的convert方法</li>
 *   <li>输出转换结果，清理上下文</li>
 * </ol>
 *
 * <p>设计特点：
 * <ul>
 *   <li>实现ContextualSerializer接口，支持字段级配置</li>
 *   <li>使用ThreadLocal管理上下文，确保线程安全</li>
 *   <li>提供完善的异常处理，转换失败时保留原值</li>
 *   <li>支持null值处理，与SerialMapBeanSerializerModifier配合</li>
 * </ul>
 *
 * @author 抓蛙师
 */
@Slf4j
@NoArgsConstructor
public class SerialMapHandler extends JsonSerializer<Object> implements ContextualSerializer {

    /**
     * 全局转换器注册表 - 存储所有可用的转换器实例
     */
    public static final Map<String, SerialMapInterface<?>> CONVERTERS = new ConcurrentHashMap<>();

    /**
     * 当前处理的属性名
     */
    private String propertyName;

    /**
     * 当前处理的字段类型
     */
    private Class<?> fieldType;

    /**
     * 当前处理的泛型类型
     */
    private Type genericType;

    /**
     * 当前处理的SerialMap注解配置
     */
    private SerialMap serialMap;

    /**
     * 私有构造函数
     *
     * <p>用于createContextual方法创建配置完整的处理器实例
     *
     * @param propertyName 属性名
     * @param fieldType    字段类型
     * @param genericType  泛型类型
     * @param serialMap    注解配置
     */
    private SerialMapHandler(String propertyName, Class<?> fieldType, Type genericType, SerialMap serialMap) {
        this.propertyName = propertyName;
        this.fieldType = fieldType;
        this.genericType = genericType;
        this.serialMap = serialMap;
    }

    /**
     * 执行序列化处理
     *
     * <p>核心序列化方法，负责值转换和结果输出
     *
     * @param value    待序列化的值
     * @param gen      JSON生成器
     * @param provider 序列化提供者
     * @throws IOException IO异常
     */
    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        // 配置检查
        if (serialMap == null) {
            log.warn("序列化处理器未正确配置，跳过处理");
            gen.writeObject(value);
            return;
        }

        // 创建并设置线程上下文
        SerialMapContext context = SerialMapContext.builder()
            .propertyName(propertyName)
            .fieldType(fieldType)
            .genericType(genericType)
            .annotation(serialMap)
            .build();

        SerialMapContext.setCurrent(context);

        try {
            // 执行值转换
            Object result = convertValue(value, gen.currentValue());
            gen.writeObject(result);
        } catch (Exception e) {
            log.error("字段转换失败: 属性={}, 值={}, 错误={}", propertyName, value, e.getMessage());
            // 容错处理：出错时保留原值
            gen.writeObject(value);
        } finally {
            // 清理线程上下文，避免内存泄漏
            SerialMapContext.clear();
        }
    }

    /**
     * 执行值转换逻辑
     *
     * @param value         原始值
     * @param currentObject 当前序列化对象
     * @return 转换后的值
     */
    private Object convertValue(Object value, Object currentObject) {
        // 1. 查找转换器
        SerialMapInterface<?> converter = CONVERTERS.get(serialMap.converter());
        if (converter == null) {
            log.warn("未找到转换器: {}", serialMap.converter());
            return value;
        }

        // 2. 处理源字段 - 如果指定了source，则从源字段获取值
        if (StringUtils.isNotBlank(serialMap.source())) {
            try {
                value = ReflectUtils.invokeGetter(currentObject, serialMap.source());
                if (value == null) {
                    log.debug("源字段值为空: {}.{}",
                        currentObject.getClass().getSimpleName(), serialMap.source());
                    return null;
                }
            } catch (Exception e) {
                log.error("获取源字段值失败: {}.{}, 错误: {}",
                    currentObject.getClass().getSimpleName(), serialMap.source(), e.getMessage());
                return value;
            }
        }

        // 3. 空值处理
        if (ObjectUtil.isNull(value)) {
            return null;
        }

        // 4. 执行转换
        try {
            // 检查转换器是否支持源对象访问
            if (converter.supportsSourceObjectAccess()) {
                log.debug("使用源对象访问接口进行转换: 转换器={}, 属性={}",
                    serialMap.converter(), propertyName);
                return converter.convert(value, serialMap.param(), currentObject);
            } else {
                log.debug("使用基础接口进行转换: 转换器={}, 属性={}",
                    serialMap.converter(), propertyName);
                return converter.convert(value, serialMap.param());
            }
        } catch (Exception e) {
            log.error("转换执行失败: 转换器={}, 值={}, 错误={}",
                serialMap.converter(), value, e.getMessage());
            return value;
        }
    }

    /**
     * 创建上下文相关的序列化器
     *
     * <p>Jackson框架调用此方法为每个标注了SerialMap注解的字段创建专用的序列化器实例
     *
     * @param provider 序列化提供者
     * @param property Bean属性信息
     * @return 配置好的序列化器实例
     * @throws JsonMappingException JSON映射异常
     */
    @Override
    public JsonSerializer<?> createContextual(SerializerProvider provider, BeanProperty property) throws JsonMappingException {
        SerialMap annotation = property.getAnnotation(SerialMap.class);
        if (annotation == null) {
            return provider.findValueSerializer(property.getType(), property);
        }

        // 获取泛型类型信息
        Type genericType = null;
        try {
            if (property.getType().getContentType() != null) {
                genericType = property.getType().getContentType().getRawClass();
            }
        } catch (Exception e) {
            log.debug("获取泛型类型失败，将使用null代替: {}", e.getMessage());
        }

        // 创建配置完整的处理器实例
        return new SerialMapHandler(
            property.getName(),
            property.getType().getRawClass(),
            genericType,
            annotation
        );
    }
}
