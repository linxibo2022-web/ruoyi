package plus.ruoyi.common.json.handler;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import plus.ruoyi.common.core.utils.ObjectUtils;

import java.io.IOException;
import java.util.Date;

/**
 * 自定义 Date 类型反序列化器
 * <p>
 * 支持多种日期格式的自动识别和解析，基于 Hutool 的 DateUtil 实现
 * <p>
 * 支持的日期格式包括但不限于：
 * <ul>
 * <li>yyyy-MM-dd HH:mm:ss</li>
 * <li>yyyy-MM-dd</li>
 * <li>yyyy/MM/dd HH:mm:ss</li>
 * <li>时间戳（毫秒）</li>
 * <li>其他常见日期格式</li>
 * </ul>
 *
 * @author AprilWind
 */
public class CustomDateDeserializer extends JsonDeserializer<Date> {

    /**
     * 日期字符串反序列化为 Date 对象
     * <p>
     * 使用 Hutool 的智能日期解析，自动识别多种日期格式
     *
     * @param p    JSON 解析器，用于获取日期字符串
     * @param ctxt 反序列化上下文
     * @return 解析后的 Date 对象
     * @throws IOException 当字符串格式无法解析时抛出
     */
    @Override
    public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        DateTime parse = DateUtil.parse(p.getText());
        if (ObjectUtils.isNull(parse)) {
            return null;
        }
        return parse.toJdkDate();
    }

}
