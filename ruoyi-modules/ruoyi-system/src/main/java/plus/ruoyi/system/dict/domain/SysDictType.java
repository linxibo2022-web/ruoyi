package plus.ruoyi.system.dict.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import plus.ruoyi.common.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典类型表 sys_dict_type
 *
 * @author Lion Li
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_type")
public class SysDictType extends TenantEntity {

    /**
     * 字典主键
     */
    @TableId(value = "dict_id")
    private Long dictId;

    /**
     * 字典名称
     */
    private String dictName;

    /**
     * 字典编码
     */
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
