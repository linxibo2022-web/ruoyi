package plus.ruoyi.system.monitor.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.mybatis.core.dao.impl.BaseDaoImpl;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.system.monitor.dao.ISysOperLogDao;
import plus.ruoyi.system.monitor.domain.SysOperLog;
import plus.ruoyi.system.monitor.domain.bo.SysOperLogBo;
import plus.ruoyi.system.monitor.mapper.SysOperLogMapper;

import java.util.Map;

/**
 * 操作日志数据访问实现
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Repository
public class SysOperLogDaoImpl extends BaseDaoImpl<SysOperLogMapper, SysOperLog> implements ISysOperLogDao {

    /**
     * 构建查询条件
     */
    @Override
    public PlusLambdaQuery<SysOperLog> buildQueryWrapper(SysOperLogBo bo) {
        Map<String, Object> params = bo.getParams();
        PlusLambdaQuery<SysOperLog> lqw = PlusLambdaQuery.of(SysOperLog.class)
            .like(SysOperLog::getOperIp, bo.getOperIp())
            .like(SysOperLog::getTitle, bo.getTitle())
            //  操作类型使用StringUtils.splitList分割查询
            .in(SysOperLog::getOperType, StringUtils.splitToList(bo.getOperType()))
            .eq(SysOperLog::getStatus, bo.getStatus())
            .like(SysOperLog::getOperName, bo.getOperName())
            .between(SysOperLog::getOperTime, params.get("beginTime"), params.get("endTime"));
        // 模糊搜索（跨数据库兼容：String 类型用 like，非 String 类型用 likeCast）
        String searchValue = bo.getSearchValue();
        if (StringUtils.isNotBlank(searchValue)) {
            lqw.and(w -> w
                .like(SysOperLog::getTitle, searchValue)           // String
                .or().like(SysOperLog::getOperName, searchValue)   // String
                .or().like(SysOperLog::getOperIp, searchValue)     // String
                .or().like(SysOperLog::getOperLocation, searchValue) // String
                .or().likeCast(SysOperLog::getOperId, searchValue)); // Long
        }
        return lqw;
    }

}
