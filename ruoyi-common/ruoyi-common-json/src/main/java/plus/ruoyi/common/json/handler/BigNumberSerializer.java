package plus.ruoyi.common.json.handler;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.ser.std.NumberSerializer;

import java.io.IOException;

/**
 * 大数值序列化器
 * <p>
 * 解决 JavaScript 数值精度丢失问题。当数值超出 JavaScript 安全整数范围时，
 * 将其序列化为字符串，避免前端精度丢失
 * <p>
 * JavaScript 安全整数范围：-(2^53-1) 到 (2^53-1)
 *
 * @author Lion Li
 */
@JacksonStdImpl
public class BigNumberSerializer extends NumberSerializer {

    /**
     * JavaScript 最大安全整数
     * <p>
     * 对应 JavaScript 中的 Number.MAX_SAFE_INTEGER
     */
    private static final long MAX_SAFE_INTEGER = 9007199254740991L;

    /**
     * JavaScript 最小安全整数
     * <p>
     * 对应 JavaScript 中的 Number.MIN_SAFE_INTEGER
     */
    private static final long MIN_SAFE_INTEGER = -9007199254740991L;

    /**
     * 序列化器实例
     */
    public static final BigNumberSerializer INSTANCE = new BigNumberSerializer(Number.class);

    /**
     * 构造方法
     *
     * @param rawType 数值类型
     */
    public BigNumberSerializer(Class<? extends Number> rawType) {
        super(rawType);
    }

    /**
     * 数值序列化逻辑
     * <p>
     * 判断数值是否在 JavaScript 安全范围内：
     * - 在安全范围内：按数值类型序列化
     * - 超出安全范围：序列化为字符串
     *
     * @param value 待序列化的数值
     * @param gen JSON 生成器
     * @param provider 序列化提供者
     * @throws IOException 序列化异常
     */
    @Override
    public void serialize(Number value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        // 判断是否在 JavaScript 安全整数范围内
        if (value.longValue() > MIN_SAFE_INTEGER && value.longValue() < MAX_SAFE_INTEGER) {
            // 在安全范围内，使用默认序列化
            super.serialize(value, gen, provider);
        } else {
            // 超出安全范围，序列化为字符串
            gen.writeString(value.toString());
        }
    }
}
