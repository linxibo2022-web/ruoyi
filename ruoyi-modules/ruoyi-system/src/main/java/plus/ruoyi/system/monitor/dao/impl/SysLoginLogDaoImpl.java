package plus.ruoyi.system.monitor.dao.impl;

import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import plus.ruoyi.common.mybatis.core.dao.impl.BaseDaoImpl;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.system.monitor.dao.ISysLoginLogDao;
import plus.ruoyi.system.monitor.domain.SysLoginLog;
import plus.ruoyi.system.monitor.domain.bo.SysLoginLogBo;
import plus.ruoyi.system.monitor.mapper.SysLoginLogMapper;

import java.util.Map;

/**
 * 登录日志数据访问实现
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Repository
public class SysLoginLogDaoImpl extends BaseDaoImpl<SysLoginLogMapper, SysLoginLog> implements ISysLoginLogDao {

    /**
     * 构建查询条件
     */
    @Override
    public PlusLambdaQuery<SysLoginLog> buildQueryWrapper(SysLoginLogBo bo) {
        Map<String, Object> params = bo.getParams();
        PlusLambdaQuery<SysLoginLog> lqw = PlusLambdaQuery.of(SysLoginLog.class)
            .like(SysLoginLog::getIpaddr, bo.getIpaddr())
            .eq(SysLoginLog::getStatus, bo.getStatus())
            .like(SysLoginLog::getUserName, bo.getUserName())
            .between(SysLoginLog::getLoginTime, params.get("beginTime"), params.get("endTime"));
        // 模糊搜索（跨数据库兼容：String 类型用 like，非 String 类型用 likeCast）
        String searchValue = bo.getSearchValue();
        if (ObjectUtil.isNotEmpty(searchValue)) {
            lqw.and(w -> w
                .like(SysLoginLog::getUserName, searchValue)          // String
                .or().like(SysLoginLog::getIpaddr, searchValue)       // String
                .or().like(SysLoginLog::getLoginLocation, searchValue) // String
                .or().like(SysLoginLog::getMsg, searchValue)          // String
                .or().like(SysLoginLog::getDeviceType, searchValue)   // String
                .or().like(SysLoginLog::getOs, searchValue)           // String
                .or().like(SysLoginLog::getBrowser, searchValue)      // String
                .or().likeCast(SysLoginLog::getUserId, searchValue)   // Long
                .or().likeCast(SysLoginLog::getInfoId, searchValue)); // Long
        }
        return lqw;
    }

}
