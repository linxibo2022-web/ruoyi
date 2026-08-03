package plus.ruoyi.system.tenant.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 租户配置信息
 *
 * @author Michelle.Chung
 */
@Data
public class TenantConfigVo {

    /**
     * 租户功能是否启用
     * 控制是否显示租户选择界面
     * true表示启用多租户，false表示单租户模式
     */
    private Boolean tenantEnabled;

    /**
     * 可用租户列表
     * 用户可以选择的租户对象集合
     * 仅当tenantEnabled为true时有效
     */
    private List<TenantOptionVo> voList;

}
