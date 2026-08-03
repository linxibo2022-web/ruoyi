package plus.ruoyi.common.tenant.core;

import plus.ruoyi.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 多租户实体基类
 * <p>
 * 继承此类的实体会自动支持多租户功能，
 * 在进行数据库操作时会自动添加租户条件
 *
 * @author Michelle.Chung
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TenantEntity extends BaseEntity {

    /**
     * 租户ID
     * <p>
     * 用于标识数据属于哪个租户，实现数据隔离
     */
    private String tenantId;

}
