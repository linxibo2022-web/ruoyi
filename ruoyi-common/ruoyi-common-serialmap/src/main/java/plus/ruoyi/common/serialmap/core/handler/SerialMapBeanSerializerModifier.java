package plus.ruoyi.common.serialmap.core.handler;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import plus.ruoyi.common.serialmap.annotation.SerialMap;
import plus.ruoyi.common.serialmap.config.SerialMapAutoConfiguration;

import java.util.List;

/**
 * Bean序列化修改器
 *
 * <p>自定义Jackson的Bean序列化行为，主要解决null值处理问题。
 *
 * <p>核心功能：
 * <ul>
 *   <li>确保null值也能通过{@link SerialMapHandler}进行处理</li>
 *   <li>统一null值和非null值的序列化路径</li>
 *   <li>避免null值被Jackson默认处理器跳过</li>
 * </ul>
 *
 * <p>技术原理：
 * Jackson默认情况下会将null值交给专门的NullSerializer处理，这会导致标注了
 * {@link SerialMap}注解的字段在值为null时
 * 跳过映射处理。该修改器通过将null值序列化器也设置为SerialMapHandler，
 * 确保所有值（包括null）都经过统一的映射处理流程。
 *
 * <p>该修改器在{@link SerialMapAutoConfiguration}中自动注册。
 *
 * @author Lion Li
 */
public class SerialMapBeanSerializerModifier extends BeanSerializerModifier {

    /**
     * 修改Bean属性的序列化行为
     *
     * <p>遍历所有Bean属性，对使用SerialMapHandler的属性设置相同的null值序列化器，
     * 确保null值也能通过SerialMapHandler进行统一处理。
     *
     * @param config         Bean序列化配置
     * @param beanDesc       Bean描述信息
     * @param beanProperties Bean属性写入器列表
     * @return 修改后的属性写入器列表
     */
    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc,
                                                     List<BeanPropertyWriter> beanProperties) {
        for (BeanPropertyWriter writer : beanProperties) {
            // 检查是否使用SerialMapHandler序列化器
            if (writer.getSerializer() instanceof SerialMapHandler serializer) {
                // 将null值序列化器也设置为同一个SerialMapHandler实例
                writer.assignNullSerializer(serializer);
            }
        }
        return beanProperties;
    }
}
