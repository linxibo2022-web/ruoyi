package plus.ruoyi.common.sensitive.handler;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import cn.hutool.extra.spring.SpringUtil;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import plus.ruoyi.common.sensitive.annotation.Sensitive;
import plus.ruoyi.common.sensitive.core.SensitiveService;
import plus.ruoyi.common.sensitive.core.SensitiveStrategy;
import plus.ruoyi.common.test.base.BaseUnitTest;
import org.springframework.beans.BeansException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * SensitiveHandler 数据脱敏序列化处理器测试
 * <p>
 * 测试 Jackson 脱敏序列化处理器的核心功能:
 * <ul>
 *   <li>需要脱敏时应用脱敏策略</li>
 *   <li>不需要脱敏时返回原始数据</li>
 *   <li>SensitiveService不存在时的容错处理</li>
 *   <li>createContextual上下文创建</li>
 * </ul>
 *
 * @author 抓蛙师
 */
@Tag("dev")
@DisplayName("SensitiveHandler 数据脱敏处理器测试")
class SensitiveHandlerTest extends BaseUnitTest {

    private SensitiveHandler sensitiveHandler;

    @Override
    protected void setUp() {
        sensitiveHandler = new SensitiveHandler();
    }

    // ==================== serialize 序列化测试 ====================

    @Nested
    @DisplayName("序列化处理测试")
    class SerializeTests {

        @Test
        @DisplayName("需要脱敏时 - 应用脱敏策略")
        void testSerializeWithDesensitization() throws IOException {
            try (MockedStatic<SpringUtil> springUtilMock = Mockito.mockStatic(SpringUtil.class)) {
                // Mock SensitiveService
                SensitiveService sensitiveService = mock(SensitiveService.class);
                when(sensitiveService.isSensitive(any(), any())).thenReturn(true);
                springUtilMock.when(() -> SpringUtil.getBean(SensitiveService.class))
                    .thenReturn(sensitiveService);

                // Mock JsonGenerator
                JsonGenerator jsonGenerator = mock(JsonGenerator.class);
                SerializerProvider serializerProvider = mock(SerializerProvider.class);

                // 设置处理器状态 - 使用反射设置私有字段
                setHandlerFields(sensitiveHandler, SensitiveStrategy.PHONE, new String[]{"admin"}, new String[]{"user:query"});

                // 执行序列化
                String phone = "13812345678";
                sensitiveHandler.serialize(phone, jsonGenerator, serializerProvider);

                // 验证写入了脱敏后的数据
                verify(jsonGenerator, times(1)).writeString(argThat(
                    (String result) -> result != null && result.contains("****")
                ));
            }
        }

        @Test
        @DisplayName("不需要脱敏时 - 返回原始数据")
        void testSerializeWithoutDesensitization() throws IOException {
            try (MockedStatic<SpringUtil> springUtilMock = Mockito.mockStatic(SpringUtil.class)) {
                // Mock SensitiveService - 返回false表示不需要脱敏
                SensitiveService sensitiveService = mock(SensitiveService.class);
                when(sensitiveService.isSensitive(any(), any())).thenReturn(false);
                springUtilMock.when(() -> SpringUtil.getBean(SensitiveService.class))
                    .thenReturn(sensitiveService);

                // Mock JsonGenerator
                JsonGenerator jsonGenerator = mock(JsonGenerator.class);
                SerializerProvider serializerProvider = mock(SerializerProvider.class);

                // 设置处理器状态
                setHandlerFields(sensitiveHandler, SensitiveStrategy.PHONE, new String[]{"admin"}, new String[]{"user:query"});

                // 执行序列化
                String phone = "13812345678";
                sensitiveHandler.serialize(phone, jsonGenerator, serializerProvider);

                // 验证写入了原始数据
                verify(jsonGenerator, times(1)).writeString(phone);
            }
        }

        @Test
        @DisplayName("SensitiveService为null时 - 返回原始数据")
        void testSerializeWhenServiceIsNull() throws IOException {
            try (MockedStatic<SpringUtil> springUtilMock = Mockito.mockStatic(SpringUtil.class)) {
                // Mock SpringUtil返回null
                springUtilMock.when(() -> SpringUtil.getBean(SensitiveService.class))
                    .thenReturn(null);

                // Mock JsonGenerator
                JsonGenerator jsonGenerator = mock(JsonGenerator.class);
                SerializerProvider serializerProvider = mock(SerializerProvider.class);

                // 设置处理器状态
                setHandlerFields(sensitiveHandler, SensitiveStrategy.EMAIL, new String[]{}, new String[]{});

                // 执行序列化
                String email = "test@example.com";
                sensitiveHandler.serialize(email, jsonGenerator, serializerProvider);

                // 验证写入了原始数据 (因为service为null)
                verify(jsonGenerator, times(1)).writeString(email);
            }
        }

        @Test
        @DisplayName("SensitiveService不存在时 - 容错处理返回原始数据")
        void testSerializeWhenServiceNotFound() throws IOException {
            try (MockedStatic<SpringUtil> springUtilMock = Mockito.mockStatic(SpringUtil.class)) {
                // Mock SpringUtil抛出BeansException
                springUtilMock.when(() -> SpringUtil.getBean(SensitiveService.class))
                    .thenThrow(new BeansException("No qualifying bean of type SensitiveService") {});

                // Mock JsonGenerator
                JsonGenerator jsonGenerator = mock(JsonGenerator.class);
                SerializerProvider serializerProvider = mock(SerializerProvider.class);

                // 设置处理器状态
                setHandlerFields(sensitiveHandler, SensitiveStrategy.CHINESE_NAME, new String[]{}, new String[]{});

                // 执行序列化
                String name = "张三";
                sensitiveHandler.serialize(name, jsonGenerator, serializerProvider);

                // 验证写入了原始数据 (容错处理)
                verify(jsonGenerator, times(1)).writeString(name);
            }
        }
    }

    // ==================== createContextual 上下文创建测试 ====================

    @Nested
    @DisplayName("上下文序列化器创建测试")
    class CreateContextualTests {

        @Test
        @DisplayName("String类型字段有Sensitive注解 - 返回配置好的处理器")
        @SuppressWarnings("unchecked")
        void testCreateContextualWithAnnotatedStringField() throws Exception {
            // Mock BeanProperty
            BeanProperty property = mock(BeanProperty.class);
            SerializerProvider provider = mock(SerializerProvider.class);

            // Mock Sensitive注解
            Sensitive annotation = mock(Sensitive.class);
            when(annotation.strategy()).thenReturn(SensitiveStrategy.PHONE);
            when(annotation.roleKey()).thenReturn(new String[]{"admin"});
            when(annotation.perms()).thenReturn(new String[]{"user:query"});
            when(property.getAnnotation(Sensitive.class)).thenReturn(annotation);

            // Mock JavaType
            JavaType javaType = mock(JavaType.class);
            when(javaType.getRawClass()).thenReturn((Class) String.class);
            when(property.getType()).thenReturn(javaType);

            // 执行
            JsonSerializer<?> result = sensitiveHandler.createContextual(provider, property);

            // 验证返回的是SensitiveHandler实例
            assertTrue(result instanceof SensitiveHandler, "应返回SensitiveHandler实例");
        }

        @Test
        @DisplayName("字段无Sensitive注解 - 使用默认序列化器")
        @SuppressWarnings("unchecked")
        void testCreateContextualWithoutAnnotation() throws Exception {
            // Mock BeanProperty - 无注解
            BeanProperty property = mock(BeanProperty.class);
            SerializerProvider provider = mock(SerializerProvider.class);
            when(property.getAnnotation(Sensitive.class)).thenReturn(null);

            // Mock JavaType
            JavaType javaType = mock(JavaType.class);
            when(property.getType()).thenReturn(javaType);

            // Mock默认序列化器
            JsonSerializer<Object> defaultSerializer = mock(JsonSerializer.class);
            when(provider.findValueSerializer(javaType, property)).thenReturn(defaultSerializer);

            // 执行
            JsonSerializer<?> result = sensitiveHandler.createContextual(provider, property);

            // 验证使用了默认序列化器
            assertEquals(defaultSerializer, result, "无注解时应使用默认序列化器");
        }

        @Test
        @DisplayName("非String类型字段 - 使用默认序列化器")
        @SuppressWarnings("unchecked")
        void testCreateContextualWithNonStringField() throws Exception {
            // Mock BeanProperty
            BeanProperty property = mock(BeanProperty.class);
            SerializerProvider provider = mock(SerializerProvider.class);

            // Mock Sensitive注解 (存在)
            Sensitive annotation = mock(Sensitive.class);
            when(annotation.strategy()).thenReturn(SensitiveStrategy.USER_ID);
            when(property.getAnnotation(Sensitive.class)).thenReturn(annotation);

            // Mock JavaType - 非String类型
            JavaType javaType = mock(JavaType.class);
            when(javaType.getRawClass()).thenReturn((Class) Integer.class);
            when(property.getType()).thenReturn(javaType);

            // Mock默认序列化器
            JsonSerializer<Object> defaultSerializer = mock(JsonSerializer.class);
            when(provider.findValueSerializer(javaType, property)).thenReturn(defaultSerializer);

            // 执行
            JsonSerializer<?> result = sensitiveHandler.createContextual(provider, property);

            // 验证使用了默认序列化器 (因为不是String类型)
            assertEquals(defaultSerializer, result, "非String类型应使用默认序列化器");
        }
    }

    // ==================== ContextualSerializer 接口验证 ====================

    @Nested
    @DisplayName("接口实现验证")
    class InterfaceTests {

        @Test
        @DisplayName("SensitiveHandler应实现ContextualSerializer接口")
        void testImplementsContextualSerializer() {
            assertTrue(sensitiveHandler instanceof ContextualSerializer,
                "SensitiveHandler应实现ContextualSerializer接口");
        }

        @Test
        @DisplayName("SensitiveHandler应继承JsonSerializer<String>")
        void testExtendsJsonSerializer() {
            assertTrue(sensitiveHandler instanceof JsonSerializer,
                "SensitiveHandler应继承JsonSerializer");
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 使用反射设置SensitiveHandler的私有字段
     */
    private void setHandlerFields(SensitiveHandler handler, SensitiveStrategy strategy,
                                  String[] roleKey, String[] perms) {
        try {
            java.lang.reflect.Field strategyField = SensitiveHandler.class.getDeclaredField("strategy");
            strategyField.setAccessible(true);
            strategyField.set(handler, strategy);

            java.lang.reflect.Field roleKeyField = SensitiveHandler.class.getDeclaredField("roleKey");
            roleKeyField.setAccessible(true);
            roleKeyField.set(handler, roleKey);

            java.lang.reflect.Field permsField = SensitiveHandler.class.getDeclaredField("perms");
            permsField.setAccessible(true);
            permsField.set(handler, perms);
        } catch (Exception e) {
            throw new RuntimeException("设置字段失败", e);
        }
    }
}
