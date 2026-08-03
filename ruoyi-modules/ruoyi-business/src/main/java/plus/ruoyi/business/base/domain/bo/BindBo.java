package plus.ruoyi.business.base.domain.bo;

import io.github.linpeilie.annotations.AutoMappers;
import plus.ruoyi.business.base.domain.Bind;
import plus.ruoyi.business.base.domain.vo.BindVo;
import plus.ruoyi.common.mybatis.core.domain.BaseEntity;
import plus.ruoyi.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 账号绑定业务对象 b_bind
 *
 * @author 抓蛙师
 * @date 2025-06-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMappers({
    @AutoMapper(target = Bind.class, reverseConvertGenerate = false),
    @AutoMapper(target = BindVo.class)
})
public class BindBo extends BaseEntity {

    /**
     * 账号绑定id
     */
    @NotNull(message = "账号绑定id不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 平台类型
     */
    private String platformType;

    /**
     * appid
     */
    private String appid;

    /**
     * unionid
     */
    private String unionid;

    /**
     * openid
     */
    private String openid;

    /**
     * 扩展数据
     */
    private String extraData;

    /**
     * 备注
     */
    private String remark;


}
