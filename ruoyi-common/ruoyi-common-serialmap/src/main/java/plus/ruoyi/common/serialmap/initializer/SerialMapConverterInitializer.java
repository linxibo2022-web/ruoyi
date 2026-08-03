package plus.ruoyi.common.serialmap.initializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.serialmap.annotation.SerialMapType;
import plus.ruoyi.common.serialmap.config.SerialMapAutoConfiguration;
import plus.ruoyi.common.serialmap.core.SerialMapInterface;
import plus.ruoyi.common.serialmap.core.handler.SerialMapBeanSerializerModifier;
import plus.ruoyi.common.serialmap.core.handler.SerialMapHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * 序列化映射转换器初始化器
 *
 * <p>功能说明：
 * <ul>
 *   <li>在 Spring 容器完全启动后执行初始化逻辑</li>
 *   <li>扫描并注册所有标注了 @SerialMapType 注解的转换器</li>
 *   <li>建立转换器类型标识与实现类的映射关系</li>
 *   <li>配置 Jackson 序列化器以支持字段映射功能</li>
 * </ul>
 *
 * <p>初始化流程：
 * <ol>
 *   <li>从 Spring 容器中获取所有 SerialMapInterface 类型的 Bean</li>
 *   <li>遍历所有转换器实例，检查是否标注 @SerialMapType 注解</li>
 *   <li>提取注解中的 type 值作为转换器的唯一标识</li>
 *   <li>建立 type → 转换器实例的映射关系</li>
 *   <li>将映射关系注册到全局 SerialMapHandler</li>
 *   <li>配置 Jackson 的 Bean 序列化修改器，启用字段映射功能</li>
 * </ol>
 *
 * <p>使用说明：
 * <ul>
 *   <li>实现 ApplicationRunner 接口，在容器启动完成后自动执行</li>
 *   <li>需要注入 ObjectMapper 实例用于配置 Jackson</li>
 *   <li>转换器实例由 SerialMapAutoConfiguration 创建并注册</li>
 * </ul>
 *
 * @author 抓蛙师
 * @date 2025/10/6
 * @see SerialMapAutoConfiguration 转换器 Bean 注册配置类
 * @see SerialMapType 转换器类型标识注解
 * @see SerialMapHandler 全局转换器处理器
 */
@Slf4j
public class SerialMapConverterInitializer implements ApplicationRunner {

    /**
     * Jackson对象映射器，用于配置序列化行为
     */
    private final ObjectMapper objectMapper;

    public SerialMapConverterInitializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // ==================== 初始化逻辑 ====================

    /**
     * 初始化序列化映射模块
     *
     * <p>在 Spring 容器初始化完成后执行，完成以下工作：
     * <ul>
     *   <li>从容器中获取所有转换器实例</li>
     *   <li>检查并提取 @SerialMapType 注解的 type 值</li>
     *   <li>建立 type → 转换器实例的映射关系</li>
     *   <li>将映射关系注册到全局 SerialMapHandler</li>
     *   <li>配置 Jackson 序列化器以支持字段映射</li>
     * </ul>
     *
     * @param args 应用启动参数
     */
    @Override
    public void run(ApplicationArguments args) {
        // 获取所有转换器Bean
        Map<String, SerialMapInterface> converterBeans =
            SpringUtils.getBeansOfType(SerialMapInterface.class);

        // 构建转换器映射表: type -> converter
        Map<String, SerialMapInterface<?>> converterMap = new HashMap<>(converterBeans.size());

        for (SerialMapInterface<?> converter : converterBeans.values()) {
            // 检查是否标注了 @SerialMapType 注解
            if (converter.getClass().isAnnotationPresent(SerialMapType.class)) {
                SerialMapType annotation = converter.getClass().getAnnotation(SerialMapType.class);
                // 注册到映射表
                converterMap.put(annotation.type(), converter);
            } else {
                log.warn("转换器实现类 {} 未标注 @SerialMapType 注解，将被忽略!",
                    converter.getClass().getName());
            }
        }

        // 将转换器映射表注册到全局处理器
        SerialMapHandler.CONVERTERS.putAll(converterMap);
        log.info("已加载 {} 个序列化映射转换器", converterMap.size());

        // 配置 Jackson Bean 序列化修改器，启用字段映射功能
        objectMapper.setSerializerFactory(
            objectMapper.getSerializerFactory()
                .withSerializerModifier(new SerialMapBeanSerializerModifier()));

        log.info("序列化映射模块初始化完成");
    }
}
