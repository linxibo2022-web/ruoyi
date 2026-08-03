package plus.ruoyi.business.base.domain.vo;

import io.github.linpeilie.annotations.AutoMappers;
import plus.ruoyi.business.base.domain.bo.BindBo;
import plus.ruoyi.common.core.dict.DictPlatformType;
import java.util.Date;
import plus.ruoyi.business.base.domain.Bind;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import plus.ruoyi.common.excel.annotation.ExcelDictFormat;
import plus.ruoyi.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


/**
 * 账号绑定视图对象 b_bind
 *
 * @author 抓蛙师
 * @date 2025-06-22
 */
@Data
@ExcelIgnoreUnannotated
@AutoMappers({
    @AutoMapper(target = Bind.class),
    @AutoMapper(target = BindBo.class)
})
public class BindVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 账号绑定id
     */
    @ExcelProperty(value = "账号绑定id")
    private Long id;

    /**
     * 用户id
     */
    @ExcelProperty(value = "用户id")
    private Long userId;

    /**
     * 平台类型
     */
    @ExcelProperty(value = "平台类型", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = DictPlatformType.DICT_TYPE)
    private String platformType;

    /**
     * appid
     */
    @ExcelProperty(value = "appid")
    private String appid;

    /**
     * unionid
     */
    @ExcelProperty(value = "unionid")
    private String unionid;

    /**
     * openid
     */
    @ExcelProperty(value = "openid")
    private String openid;

    /**
     * 扩展数据
     */
    @ExcelProperty(value = "扩展数据")
    private String extraData;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 更新时间
     */
    @ExcelProperty(value = "更新时间")
    private Date updateTime;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String remark;

}
