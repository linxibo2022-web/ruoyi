package plus.ruoyi.common.json.utils;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.core.utils.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON 工具类
 * <p>
 * 基于 Jackson 的 JSON 序列化和反序列化工具，提供常用的 JSON 操作方法
 * <p>
 * 主要功能：
 * <ul>
 * <li>对象与 JSON 字符串互转</li>
 * <li>支持复杂类型的泛型转换</li>
 * <li>支持字节数组转换</li>
 * <li>支持集合类型转换</li>
 * <li>异常统一处理</li>
 * </ul>
 *
 * @author 芋道源码
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonUtils {

    /**
     * 全局 ObjectMapper 实例，从 Spring 容器中获取
     */
    private static final ObjectMapper OBJECT_MAPPER = SpringUtils.getBean(ObjectMapper.class);

    /**
     * 获取 ObjectMapper 实例
     *
     * @return ObjectMapper 实例
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    /**
     * 将对象序列化为 JSON 字符串
     *
     * @param object 待序列化的对象
     * @return JSON 字符串，对象为 null 时返回 null
     * @throws RuntimeException 序列化失败时抛出
     */
    public static String toJsonString(Object object) {
        if (ObjectUtil.isNull(object)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将 JSON 字符串反序列化为指定类型对象
     *
     * @param text  JSON 字符串
     * @param clazz 目标对象类型
     * @param <T>   目标对象泛型
     * @return 反序列化后的对象，字符串为空时返回 null
     * @throws RuntimeException 反序列化失败时抛出
     */
    public static <T> T parseObject(String text, Class<T> clazz) {
        if (StringUtils.isEmpty(text)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(text, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将字节数组反序列化为指定类型对象
     *
     * @param bytes 字节数组
     * @param clazz 目标对象类型
     * @param <T>   目标对象泛型
     * @return 反序列化后的对象，字节数组为空时返回 null
     * @throws RuntimeException 反序列化失败时抛出
     */
    public static <T> T parseObject(byte[] bytes, Class<T> clazz) {
        if (ArrayUtil.isEmpty(bytes)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(bytes, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将 JSON 字符串反序列化为复杂类型对象
     * <p>
     * 支持泛型类型的反序列化，如 List&lt;User&gt;、Map&lt;String, Object&gt; 等
     *
     * @param text          JSON 字符串
     * @param typeReference 类型引用，用于指定复杂泛型类型
     * @param <T>           目标对象泛型
     * @return 反序列化后的对象，字符串为空时返回 null
     * @throws RuntimeException 反序列化失败时抛出
     */
    public static <T> T parseObject(String text, TypeReference<T> typeReference) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(text, typeReference);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将 JSON 字符串解析为 Dict 对象
     * <p>
     * Dict 是 Hutool 提供的增强型 Map，支持链式调用和类型转换
     *
     * @param text JSON 字符串
     * @return Dict 对象，字符串为空或非 JSON 格式时返回 null
     * @throws RuntimeException 解析失败时抛出
     */
    public static Dict parseMap(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(text, OBJECT_MAPPER.getTypeFactory().constructType(Dict.class));
        } catch (MismatchedInputException e) {
            // 类型不匹配说明不是 JSON 格式
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将 JSON 数组字符串解析为 Dict 对象列表
     *
     * @param text JSON 数组字符串
     * @return Dict 对象列表，字符串为空时返回 null
     * @throws RuntimeException 解析失败时抛出
     */
    public static List<Dict> parseArrayMap(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(text, OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, Dict.class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将 JSON 数组字符串解析为指定类型对象列表
     *
     * @param text  JSON 数组字符串
     * @param clazz 目标对象类型
     * @param <T>   目标对象泛型
     * @return 对象列表，字符串为空时返回空列表
     * @throws RuntimeException 解析失败时抛出
     */
    public static <T> List<T> parseArray(String text, Class<T> clazz) {
        if (StringUtils.isEmpty(text)) {
            return new ArrayList<>();
        }
        try {
            return OBJECT_MAPPER.readValue(text, OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 判断字符串是否为合法 JSON（对象或数组）
     *
     * @param str 待校验字符串
     * @return true = 合法 JSON，false = 非法或空
     */
    public static boolean isJson(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        try {
            OBJECT_MAPPER.readTree(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断字符串是否为 JSON 对象（{}）
     *
     * @param str 待校验字符串
     * @return true = JSON 对象
     */
    public static boolean isJsonObject(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        try {
            JsonNode node = OBJECT_MAPPER.readTree(str);
            return node.isObject();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断字符串是否为 JSON 数组（[]）
     *
     * @param str 待校验字符串
     * @return true = JSON 数组
     */
    public static boolean isJsonArray(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        try {
            JsonNode node = OBJECT_MAPPER.readTree(str);
            return node.isArray();
        } catch (Exception e) {
            return false;
        }
    }

}
