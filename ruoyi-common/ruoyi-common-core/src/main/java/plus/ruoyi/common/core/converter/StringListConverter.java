package plus.ruoyi.common.core.converter;

import plus.ruoyi.common.core.utils.StreamUtils;
import plus.ruoyi.common.core.utils.StringUtils;

import java.util.List;

/**
 * 字符串列表转换器
 * <p>
 * 如 @AutoMapper(target = Doc.class, uses = StringListConverter.class)
 *
 * @author 抓蛙师
 * @date 2025/8/30
 */
public class StringListConverter {

    /**
     * 将字符串转换为字符串列表
     *
     * @param source 字符串
     * @return 字符串列表
     */
    public List<String> stringToList(String source) {
        return StringUtils.splitToList(source);
    }

    /**
     * 将字符串列表转换为字符串
     *
     * @param source 字符串列表
     * @return 字符串
     */
    public String listToString(List<String> source) {
        return StreamUtils.join(source, s -> s);
    }
}
