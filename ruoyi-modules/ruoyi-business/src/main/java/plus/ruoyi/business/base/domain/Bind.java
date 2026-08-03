package plus.ruoyi.business.base.domain;

import plus.ruoyi.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 账号绑定对象 b_bind
 *
 * @author 抓蛙师
 * @date 2025-06-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("b_bind")
public class Bind extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 账号绑定id
     */
    @TableId(value = "id")
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

    /**
     * 是否删除
     */
    @TableLogic
    private String isDeleted;


}
