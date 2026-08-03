package plus.ruoyi.common.core.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 字典项视图对象
 *
 * @author 抓蛙师
 * @description 用于下拉选择、标签等组件的选项数据
 */
@Data
public class DictItemVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 显示标签文本
     */
    private String label;

    /**
     * 实际存储的值
     */
    private String value;

    /**
     * 状态标识
     */
    private String status;

    /**
     * Element UI Tag 组件的类型
     */
    private String elTagType;

    /**
     * Element UI Tag 组件的自定义类名
     */
    private String elTagClass;

    public static DictItemVo of(String label, String value) {
        DictItemVo item = new DictItemVo();
        item.setLabel(label);
        item.setValue(value);
        return item;
    }

    public static DictItemVo of(String label, String value, String elTagType) {
        DictItemVo item = new DictItemVo();
        item.setLabel(label);
        item.setValue(value);
        item.setElTagType(elTagType);
        return item;
    }
}
