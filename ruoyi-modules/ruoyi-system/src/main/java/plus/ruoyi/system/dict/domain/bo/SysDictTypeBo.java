package plus.ruoyi.system.dict.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import io.github.linpeilie.annotations.AutoMappers;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import plus.ruoyi.common.core.utils.regex.RegexPatterns;
import plus.ruoyi.common.mybatis.core.domain.BaseEntity;
import plus.ruoyi.system.dict.domain.SysDictType;
import plus.ruoyi.system.dict.domain.vo.SysDictTypeVo;

/**
 * 字典类型业务对象 sys_dict_type
 *
 * @author Michelle.Chung
 */

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMappers({
    @AutoMapper(target = SysDictType.class, reverseConvertGenerate = false),
    @AutoMapper(target = SysDictTypeVo.class)
})
public class SysDictTypeBo extends BaseEntity {

    /**
     * 字典主键
     */
    private Long dictId;

    /**
     * 字典名称
     */
    @NotBlank(message = "字典名称不能为空")
    @Size(min = 0, max = 100, message = "字典名称长度不能超过{max}个字符")
    private String dictName;

    /**
     * 字典编码
     */
    @NotBlank(message = "字典编码不能为空")
    @Size(min = 0, max = 100, message = "字典编码长度不能超过{max}个字符")
    @Pattern(regexp = RegexPatterns.DICT_TYPE, message = "字典编码必须以字母开头，且只能为（小写字母，数字，下滑线）")
    private String dictType;

    /**
     * 状态
     */
    private String status;

    /**
     * 是否系统级字典
     * 0 - 租户自定义字典（允许修改）
     * 1 - 系统级字典（租户不允许修改）
     */
    private String isSystem;

    /**
     * 备注
     */
    private String remark;


}
