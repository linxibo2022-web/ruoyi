package plus.ruoyi.system.tenant.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

/**
 * 租户列表项对象
 * 封装租户选择列表中的单个租户信息
 *
 * @author Lion Li
 */
@Data
@AutoMapper(target = SysTenantVo.class)
public class TenantOptionVo {

    /**
     * 租户ID
     * 唯一标识租户的编码，用于多租户数据隔离
     */
    private String tenantId;

    /**
     * 企业名称
     * 租户对应的企业或组织名称，显示在租户选择列表中
     */
    private String companyName;

    /**
     * 域名
     * 租户专属的访问域名，用于通过域名自动识别租户
     */
    private String domain;

}
