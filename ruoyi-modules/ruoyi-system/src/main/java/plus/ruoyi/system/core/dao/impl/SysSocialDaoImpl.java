package plus.ruoyi.system.core.dao.impl;

import org.springframework.stereotype.Repository;
import plus.ruoyi.common.mybatis.core.dao.impl.BaseDaoImpl;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.system.core.dao.ISysSocialDao;
import plus.ruoyi.system.core.domain.SysSocial;
import plus.ruoyi.system.core.domain.bo.SysSocialBo;
import plus.ruoyi.system.core.mapper.SysSocialMapper;

import java.util.List;

/**
 * 社会化关系数据访问实现
 *
 * @author thiszhc
 */
@Repository
public class SysSocialDaoImpl extends BaseDaoImpl<SysSocialMapper, SysSocial>
    implements ISysSocialDao {

    /**
     * 构建查询条件
     */
    @Override
    public PlusLambdaQuery<SysSocial> buildQueryWrapper(SysSocialBo bo) {
        PlusLambdaQuery<SysSocial> lqw = PlusLambdaQuery.of(SysSocial.class);
        lqw.eq(SysSocial::getUserId, bo.getUserId());
        lqw.eq(SysSocial::getAuthId, bo.getAuthId());
        lqw.eq(SysSocial::getSource, bo.getSource());
        return lqw;
    }

    /**
     * 根据用户ID查询社会化关系列表
     */
    @Override
    public List<SysSocial> listByUserId(Long userId) {
        PlusLambdaQuery<SysSocial> lqw = PlusLambdaQuery.of(SysSocial.class)
            .eq(SysSocial::getUserId, userId);
        return list(lqw);
    }

    /**
     * 根据认证ID查询社会化关系列表
     */
    @Override
    public List<SysSocial> listByAuthId(String authId) {
        PlusLambdaQuery<SysSocial> lqw = PlusLambdaQuery.of(SysSocial.class)
            .eq(SysSocial::getAuthId, authId);
        return list(lqw);
    }

}
