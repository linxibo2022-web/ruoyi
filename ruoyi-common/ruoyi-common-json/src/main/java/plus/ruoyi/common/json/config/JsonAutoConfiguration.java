package plus.ruoyi.common.json.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.json.handler.BigNumberSerializer;
import plus.ruoyi.common.json.handler.CustomDateDeserializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

/**
 * Jackson JSON 序列化配置
 * <p>
 * 自定义 Jackson 序列化和反序列化行为，主要包括：
 * <ul>
 * <li>大数值类型处理：避免 JavaScript 精度丢失问题</li>
 * <li>时间类型格式化：统一日期时间格式</li>
 * <li>数值类型转换：BigDecimal 转为字符串避免精度问题</li>
 * <li>时区设置：使用系统默认时区</li>
 * </ul>
 *
 * @author Lion Li
 */
@Slf4j
@AutoConfiguration(before = org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration.class)
public class JsonAutoConfiguration {

    /**
     * 自定义 Jackson ObjectMapper 构建器
     * <p>
     * 配置全局的 JSON 序列化和反序列化规则
     *
     * @return Jackson2ObjectMapperBuilderCustomizer 自定义构建器
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizer() {
        return builder -> {
            // 创建 Java 时间模块，用于处理各种数据类型的序列化
            JavaTimeModule javaTimeModule = new JavaTimeModule();

            // 配置大数值序列化器，避免 JavaScript 数值精度丢失
            javaTimeModule.addSerializer(Long.class, BigNumberSerializer.INSTANCE);
            javaTimeModule.addSerializer(Long.TYPE, BigNumberSerializer.INSTANCE);
            javaTimeModule.addSerializer(BigInteger.class, BigNumberSerializer.INSTANCE);

            // BigDecimal 序列化为字符串，保持精度
            javaTimeModule.addSerializer(BigDecimal.class, ToStringSerializer.instance);

            // 配置 LocalDateTime 的序列化和反序列化格式
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
            javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));

            // 配置 Date 类型的反序列化器，支持多种日期格式
            javaTimeModule.addDeserializer(Date.class, new CustomDateDeserializer());

            // 应用模块配置
            builder.modules(javaTimeModule);
            // 设置默认时区
            builder.timeZone(TimeZone.getDefault());

            log.info("初始化 jackson 配置");
        };
    }

}
