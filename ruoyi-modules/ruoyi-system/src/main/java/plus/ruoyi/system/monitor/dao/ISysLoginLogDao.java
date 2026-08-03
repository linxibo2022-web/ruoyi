package plus.ruoyi.system.monitor.dao;

import plus.ruoyi.common.mybatis.core.dao.IBaseDao;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.system.monitor.domain.SysLoginLog;
import plus.ruoyi.system.monitor.domain.bo.SysLoginLogBo;

/**
 * 登录日志数据访问层
 *
 * @author Lion Li
 */
public interface ISysLoginLogDao extends IBaseDao<SysLoginLog> {

    /**
     * 构建查询条件
     *
     * @param bo 查询条件
     * @return 查询构造器
     */
    PlusLambdaQuery<SysLoginLog> buildQueryWrapper(SysLoginLogBo bo);
}
