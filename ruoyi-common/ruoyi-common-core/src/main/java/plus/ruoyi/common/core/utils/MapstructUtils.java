package plus.ruoyi.common.core.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import io.github.linpeilie.Converter;
import io.github.linpeilie.ConvertException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mapstruct 工具类
 * <p>参考文档：<a href="https://mapstruct.plus/introduction/quick-start.html">mapstruct-plus</a></p>
 *
 *
 * @author Michelle.Chung
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MapstructUtils {

    private final static Converter CONVERTER = SpringUtils.getBean(Converter.class);

    /**
     * 将 T 类型对象，转换为 desc 类型的对象并返回
     * <p>优先使用MapStruct转换，如果失败则回退到BeanUtil</p>
     *
     * @param source 数据来源实体
     * @param desc   描述对象 转换后的对象
     * @return desc
     */
    public static <T, V> V convert(T source, Class<V> desc) {
        if (ObjectUtil.isNull(source)) {
            return null;
        }
        if (ObjectUtil.isNull(desc)) {
            return null;
        }
        try {
            return CONVERTER.convert(source, desc);
        } catch (ConvertException e) {
            // MapStruct转换失败，回退到BeanUtil
            return BeanUtil.toBean(source, desc);
        }
    }

    /**
     * 将 T 类型对象，按照配置的映射字段规则，给 desc 类型的对象赋值并返回 desc 对象
     * <p>优先使用MapStruct转换，如果失败则回退到BeanUtil</p>
     *
     * @param source 数据来源实体
     * @param desc   转换后的对象
     * @return desc
     */
    public static <T, V> V convert(T source, V desc) {
        if (ObjectUtil.isNull(source)) {
            return null;
        }
        if (ObjectUtil.isNull(desc)) {
            return null;
        }
        try {
            return CONVERTER.convert(source, desc);
        } catch (ConvertException e) {
            // MapStruct转换失败，回退到BeanUtil
            BeanUtil.copyProperties(source, desc);
            return desc;
        }
    }

    /**
     * 将 T 类型的集合，转换为 desc 类型的集合并返回
     * <p>优先使用MapStruct转换，如果失败则回退到BeanUtil</p>
     *
     * @param sourceList 数据来源实体列表
     * @param desc       描述对象 转换后的对象
     * @return desc
     */
    public static <T, V> List<V> convert(List<T> sourceList, Class<V> desc) {
        if (ObjectUtil.isNull(sourceList)) {
            return null;
        }
        if (CollUtil.isEmpty(sourceList)) {
            return CollUtil.newArrayList();
        }
        try {
            return CONVERTER.convert(sourceList, desc);
        } catch (ConvertException e) {
            // MapStruct转换失败，回退到BeanUtil
            return sourceList.stream()
                .map(source -> BeanUtil.toBean(source, desc))
                .collect(Collectors.toList());
        }
    }

    /**
     * 将 Map 转换为 beanClass 类型的对象并返回
     * <p>注意：MapStruct不支持Map到对象的转换，这里使用Hutool的BeanUtil实现</p>
     *
     * @param map       数据来源
     * @param beanClass bean类
     * @return bean对象
     */
    public static <T> T convert(Map<String, Object> map, Class<T> beanClass) {
        if (MapUtil.isEmpty(map)) {
            return null;
        }
        if (ObjectUtil.isNull(beanClass)) {
            return null;
        }
        // 使用Hutool的BeanUtil进行Map到Bean的转换
        return BeanUtil.toBean(map, beanClass);
    }

}
