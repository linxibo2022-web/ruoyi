package plus.ruoyi.common.serialmap.core;

import plus.ruoyi.common.serialmap.annotation.SerialMapType;
import plus.ruoyi.common.serialmap.annotation.SerialMap;

/**
 * 序列化映射转换器接口
 *
 * <p>所有自定义转换器的核心接口，定义了数据转换的标准规范。
 *
 * <p>实现要求：
 * <ul>
 *   <li>实现类必须标注{@link SerialMapType}注解，声明转换器类型</li>
 *   <li>实现类需要注册为Spring Bean（通常使用@Component注解）</li>
 *   <li>泛型T指定转换结果的类型</li>
 *   <li>实现convert方法，处理具体的转换逻辑</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>
 * // 简单转换器
 * {@code @Component}
 * {@code @SerialMapType(type = "user_id_to_name")}
 * public class UserIdToNameConverter implements SerialMapInterface&lt;String&gt; {
 *     {@code @Override}
 *     public String convert(Object key, String param) {
 *         return getUserNameById((Long) key);
 *     }
 * }
 *
 * // 复杂转换器（需要访问多个字段）
 * {@code @Component}
 * {@code @SerialMapType(type = "device_mac_to_product_image")}
 * public class DeviceMacToProductImageConverter implements SerialMapInterface&lt;String&gt; {
 *     {@code @Override}
 *     public String convert(Object key, String param, Object sourceObject) {
 *         DeviceAppVo device = (DeviceAppVo) sourceObject;
 *         // 先用MAC地址查询
 *         String image = getImageByMac(device.getMacAddress());
 *         if (image == null) {
 *             // 再用主板型号查询
 *             image = getImageByBoard(device.getBoardName());
 *         }
 *         return image;
 *     }
 * }
 * </pre>
 *
 * @param <T> 转换结果的类型
 * @author Lion Li
 */
public interface SerialMapInterface<T> {

    /**
     * 执行数据转换
     *
     * <p>将输入的key值根据业务逻辑转换为目标类型的结果。
     *
     * @param key   转换的源值，通常是字段的原始值，保证不为空（框架已做null检查）
     * @param param 转换器额外参数，来源于{@link SerialMap#param()}
     * @return 转换后的结果，可以是任意类型
     */
    T convert(Object key, String param);

    /**
     * 执行值转换（支持访问完整源对象）
     *
     * <p>将输入值转换为目标类型的值。这是增强版本的转换方法，
     * 允许转换器访问完整的源对象，适用于需要多个字段参与转换的复杂场景。
     *
     * <p>默认实现调用原有的两参数方法，确保向后兼容性。
     * 需要访问源对象的转换器应该重写此方法。
     *
     * @param key          转换的源值，通常是字段的原始值
     * @param param        转换器参数，来自注解的param配置
     * @param sourceObject 完整的源对象，转换器可以从中获取任意字段值
     * @return 转换后的目标值，可以是任意类型
     */
    default T convert(Object key, String param, Object sourceObject) {
        return convert(key, param);
    }

    /**
     * 检查转换器是否支持源对象访问
     *
     * <p>转换器可以重写此方法返回true，表示需要访问完整的源对象。
     * 框架会根据此方法的返回值决定调用哪个convert方法。
     *
     * @return true表示支持源对象访问，false表示仅需要基础接口
     */
    default boolean supportsSourceObjectAccess() {
        return false;
    }
}
