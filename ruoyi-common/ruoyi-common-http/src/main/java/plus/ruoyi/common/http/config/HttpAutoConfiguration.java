package plus.ruoyi.common.http.config;

import com.dtflys.forest.converter.json.ForestJacksonConverter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import plus.ruoyi.common.core.factory.YmlPropertySourceFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.PropertySource;
import plus.ruoyi.common.http.client.gaode.map.properties.GaodeMapProperties;
import plus.ruoyi.common.http.client.volcengine.tts.properties.VolcengineTtsProperties;

/**
 * Forest HTTP客户端配置
 *
 * @author Feng
 */
@AutoConfiguration
@EnableConfigurationProperties({
    GaodeMapProperties.class,
    VolcengineTtsProperties.class
})
@PropertySource(value = "classpath:http-client-${spring.profiles.active}.yml", factory = YmlPropertySourceFactory.class)
public class HttpAutoConfiguration {

    @Bean
    public ForestJacksonConverter forestJacksonConverter(ObjectMapper objectMapper) {
       // 复制一份，避免影响全局
        ObjectMapper forestMapper = objectMapper.copy();

        // 处理字符串 -> 数组（"内丘县" -> ["内丘县"]）
        forestMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

        // 处理空数组 -> null（[] -> null）
        forestMapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);

        // 忽略未知属性
        forestMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        return new ForestJacksonConverter(forestMapper);
    }
}
