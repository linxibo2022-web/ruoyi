package plus.ruoyi.system.core.dao;

import plus.ruoyi.common.mybatis.core.dao.IBaseDao;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.system.core.domain.SysSocial;
import plus.ruoyi.system.core.domain.bo.SysSocialBo;

import java.util.List;

/**
 * 社会化关系DAO接口
 *
 * @author thiszhc
 */
public interface ISysSocialDao extends IBaseDao<SysSocial> {

    /**
     * 根据业务对象构建查询条件
     *
     * @param bo 业务对象
     * @return 查询条件
     */
    PlusLambdaQuery<SysSocial> buildQueryWrapper(SysSocialBo bo);

    /**
     * 根据用户ID查询社会化关系列表
     *
     * @param userId 用户ID
     * @return 社会化关系列表
     */
    List<SysSocial> listByUserId(Long userId);

    /**
     * 根据认证ID查询社会化关系列表
     *
     * @param authId 认证ID
     * @return 社会化关系列表
     */
    List<SysSocial> listByAuthId(String authId);

}
