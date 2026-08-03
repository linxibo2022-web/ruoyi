package plus.ruoyi.system.monitor.dao.impl;

import org.springframework.stereotype.Repository;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.mybatis.core.dao.impl.BaseDaoImpl;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaUpdate;
import plus.ruoyi.system.monitor.dao.ISysErrorLogDao;
import plus.ruoyi.system.monitor.domain.SysErrorLog;
import plus.ruoyi.system.monitor.domain.bo.SysErrorLogBo;
import plus.ruoyi.system.monitor.mapper.SysErrorLogMapper;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * 错误日志数据访问实现
 *
 * @author 抓蛙师
 */
@Repository
public class SysErrorLogDaoImpl extends BaseDaoImpl<SysErrorLogMapper, SysErrorLog> implements ISysErrorLogDao {

    /**
     * 构建查询条件
     *
     * @param bo 查询参数
     * @return 查询包装器
     */
    @Override
    public PlusLambdaQuery<SysErrorLog> buildQueryWrapper(SysErrorLogBo bo) {
        Map<String, Object> params = bo.getParams();
        PlusLambdaQuery<SysErrorLog> lqw = PlusLambdaQuery.of();

        // 精确匹配
        lqw.eq(SysErrorLog::getId, bo.getId());
        lqw.eq(SysErrorLog::getTenantId, bo.getTenantId());
        lqw.eq(SysErrorLog::getErrorLevel, bo.getErrorLevel());
        lqw.eq(SysErrorLog::getErrorCode, bo.getErrorCode());
        lqw.eq(SysErrorLog::getTraceId, bo.getTraceId());
        lqw.eq(SysErrorLog::getRequestUri, bo.getRequestUri());
        lqw.eq(SysErrorLog::getRequestPattern, bo.getRequestPattern());
        lqw.eq(SysErrorLog::getRequestMethod, bo.getRequestMethod());
        lqw.eq(SysErrorLog::getRequestIp, bo.getRequestIp());
        lqw.eq(SysErrorLog::getUserId, bo.getUserId());
        lqw.eq(SysErrorLog::getDeptId, bo.getDeptId());
        lqw.eq(SysErrorLog::getModuleName, bo.getModuleName());
        lqw.eq(SysErrorLog::getBusinessType, bo.getBusinessType());
        lqw.eq(SysErrorLog::getClientType, bo.getClientType());
        lqw.eq(SysErrorLog::getHandleStatus, bo.getHandleStatus());

        // 模糊匹配
        lqw.like(StringUtils.isNotBlank(bo.getErrorType()), SysErrorLog::getErrorType, bo.getErrorType());
        lqw.like(StringUtils.isNotBlank(bo.getErrorMessage()), SysErrorLog::getErrorMessage, bo.getErrorMessage());
        lqw.like(StringUtils.isNotBlank(bo.getUserName()), SysErrorLog::getUserName, bo.getUserName());
        lqw.like(StringUtils.isNotBlank(bo.getBusinessKey()), SysErrorLog::getBusinessKey, bo.getBusinessKey());
        lqw.like(StringUtils.isNotBlank(bo.getHandleRemark()), SysErrorLog::getHandleRemark, bo.getHandleRemark());

        // 时间范围查询
        lqw.between(SysErrorLog::getCreateTime, params.get("beginCreateTime"), params.get("endCreateTime"));
        lqw.between(SysErrorLog::getHandleTime, params.get("beginHandleTime"), params.get("endHandleTime"));
        lqw.between(SysErrorLog::getFirstTime, params.get("beginFirstTime"), params.get("endFirstTime"));
        lqw.between(SysErrorLog::getLastTime, params.get("beginLastTime"), params.get("endLastTime"));

        // 全局搜索（跨数据库兼容）
        String searchValue = bo.getSearchValue();
        if (StringUtils.isNotBlank(searchValue)) {
            lqw.and(w -> w
                .likeCast(SysErrorLog::getId, searchValue)                     // Long → likeCast
                .or().like(SysErrorLog::getErrorType, searchValue)             // String → like
                .or().like(SysErrorLog::getErrorMessage, searchValue)          // String → like
                .or().like(SysErrorLog::getTraceId, searchValue)               // String → like
                .or().like(SysErrorLog::getRequestUri, searchValue)            // String → like
                .or().like(SysErrorLog::getRequestPattern, searchValue)        // String → like
                .or().like(SysErrorLog::getUserName, searchValue)              // String → like
                .or().like(SysErrorLog::getBusinessKey, searchValue)           // String → like
                .or().likeCast(SysErrorLog::getCreateTime, searchValue)        // Date → likeCast
            );
        }

        // 默认按创建时间倒序
        lqw.orderByDesc(SysErrorLog::getCreateTime);

        return lqw;
    }

    /**
     * 增加错误发生次数并更新最后发生时间
     *
     * @param id 错误日志ID
     * @return 影响行数
     */
    @Override
    public int incrementOccurrenceCount(Long id) {
        return lambdaUpdate()
            .setSql("occurrence_count = occurrence_count + 1")
            .set(SysErrorLog::getLastTime, new Date())
            .eq(SysErrorLog::getId, id)
            .update();
    }

    /**
     * 根据ID更新处理状态
     *
     * @param id           主键ID
     * @param handleStatus 处理状态
     * @param handleBy     处理人ID
     * @param handleTime   处理时间
     * @param handleRemark 处理备注
     * @return 影响行数
     */
    @Override
    public int updateHandleStatusById(Long id, String handleStatus, Long handleBy, Date handleTime, String handleRemark) {
        return lambdaUpdate()
            .set(SysErrorLog::getHandleStatus, handleStatus)
            .set(SysErrorLog::getHandleBy, handleBy)
            .set(SysErrorLog::getHandleTime, handleTime)
            .setIfNotNull(SysErrorLog::getHandleRemark, handleRemark)
            .eq(SysErrorLog::getId, id)
            .update();
    }

    /**
     * 按ID集合批量更新处理状态
     *
     * @param ids          主键ID集合
     * @param handleStatus 处理状态
     * @param handleBy     处理人ID
     * @param handleTime   处理时间
     * @param handleRemark 处理备注
     * @return 影响行数
     */
    @Override
    public int updateHandleStatusBatch(Collection<Long> ids, String handleStatus, Long handleBy, Date handleTime, String handleRemark) {
        return lambdaUpdate()
            .set(SysErrorLog::getHandleStatus, handleStatus)
            .set(SysErrorLog::getHandleBy, handleBy)
            .set(SysErrorLog::getHandleTime, handleTime)
            .setIfNotNull(SysErrorLog::getHandleRemark, handleRemark)
            .in(SysErrorLog::getId, ids)
            .update();
    }

    /**
     * 按相同错误批量更新处理状态
     *
     * @param tenantId       租户ID
     * @param errorType      异常类名
     * @param errorCode      业务错误码
     * @param errorMessage   错误消息
     * @param requestPattern 请求路径模板
     * @param requestMethod  请求方法
     * @param handleStatus   处理状态
     * @param handleBy       处理人ID
     * @param handleTime     处理时间
     * @param handleRemark   处理备注
     * @return 影响行数
     */
    @Override
    public int updateHandleStatusBySameError(String tenantId, String errorType, String errorCode, String errorMessage,
                                             String requestPattern, String requestMethod, String handleStatus, Long handleBy,
                                             Date handleTime, String handleRemark) {
        PlusLambdaUpdate<SysErrorLog> update = lambdaUpdate()
            .set(SysErrorLog::getHandleStatus, handleStatus)
            .set(SysErrorLog::getHandleBy, handleBy)
            .set(SysErrorLog::getHandleTime, handleTime)
            .setIfNotNull(SysErrorLog::getHandleRemark, handleRemark)
            .eq(SysErrorLog::getTenantId, tenantId)
            .eq(SysErrorLog::getHandleStatus, "0")
            .eq(SysErrorLog::getErrorType, errorType)
            .eq(SysErrorLog::getRequestPattern, requestPattern)
            .eq(SysErrorLog::getRequestMethod, requestMethod);

        if (StringUtils.isNotBlank(errorCode)) {
            update.eq(SysErrorLog::getErrorCode, errorCode);
        } else {
            update.eq(SysErrorLog::getErrorMessage, errorMessage);
        }
        return update.update();
    }

    /**
     * 按相同接口批量更新处理状态
     *
     * @param tenantId       租户ID
     * @param requestPattern 请求路径模板
     * @param requestMethod  请求方法
     * @param handleStatus   处理状态
     * @param handleBy       处理人ID
     * @param handleTime     处理时间
     * @param handleRemark   处理备注
     * @return 影响行数
     */
    @Override
    public int updateHandleStatusBySameRequest(String tenantId, String requestPattern, String requestMethod,
                                               String handleStatus, Long handleBy, Date handleTime, String handleRemark) {
        return lambdaUpdate()
            .set(SysErrorLog::getHandleStatus, handleStatus)
            .set(SysErrorLog::getHandleBy, handleBy)
            .set(SysErrorLog::getHandleTime, handleTime)
            .setIfNotNull(SysErrorLog::getHandleRemark, handleRemark)
            .eq(SysErrorLog::getTenantId, tenantId)
            .eq(SysErrorLog::getHandleStatus, "0")
            .eq(SysErrorLog::getRequestPattern, requestPattern)
            .eq(SysErrorLog::getRequestMethod, requestMethod)
            .update();
    }
}
