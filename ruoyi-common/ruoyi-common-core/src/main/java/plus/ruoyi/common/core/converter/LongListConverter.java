package plus.ruoyi.common.core.converter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import plus.ruoyi.common.core.utils.StreamUtils;
import plus.ruoyi.common.core.utils.StringUtils;

import java.util.List;

/**
 * 长整数列表转换器
 * <p>
 * 如 @AutoMapper(target = Doc.class, uses = LongListConverter.class)
 *
 * @author 抓蛙师
 * @date 2025/8/30
 */
public class LongListConverter {

    /**
     * 将字符串转换为长整数列表
     *
     * @param source 源字符串
     * @return 长整数列表
     */
    public List<Long> stringToLongList(String source) {
        if (StringUtils.isBlank(source)) {
            return List.of();
        }
        List<String> stringList = StringUtils.splitToList(source);
        return StreamUtils.toList(stringList, Convert::toLong);
    }

    /**
     * 将长整数列表转换为逗号分隔的字符串
     *
     * @param source 源长整数列表
     * @return 逗号分隔的字符串
     */
    public String longListToString(List<Long> source) {
        if (CollUtil.isEmpty(source)) {
            return null;
        }
        return StreamUtils.join(source, Convert::toStr);
    }
}
