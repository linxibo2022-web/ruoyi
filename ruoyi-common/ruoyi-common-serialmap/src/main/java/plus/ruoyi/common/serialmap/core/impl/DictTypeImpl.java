package plus.ruoyi.common.serialmap.core.impl;

import plus.ruoyi.common.core.service.DictService;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.serialmap.annotation.SerialMapType;
import plus.ruoyi.common.serialmap.constant.SerialMapConstant;
import plus.ruoyi.common.serialmap.core.SerialMapInterface;
import lombok.AllArgsConstructor;

/**
 * 字典值转标签转换器
 *
 * <p>根据字典类型(param)和字典值(key)获取对应的字典标签
 *
 * @author Lion Li
 */
@SerialMapType(type = SerialMapConstant.DICT_TYPE_TO_LABEL)
public class DictTypeImpl implements SerialMapInterface<String> {

    private final DictService dictService;

    /**
     * 构造方法
     */
    public DictTypeImpl(DictService dictService) {
        this.dictService = dictService;
    }

    @Override
    public String convert(Object key, String param) {
        if (key instanceof String dictValue && StringUtils.isNotBlank(param)) {
            return dictService.getDictLabel(param, dictValue);
        }
        return null;
    }
}
