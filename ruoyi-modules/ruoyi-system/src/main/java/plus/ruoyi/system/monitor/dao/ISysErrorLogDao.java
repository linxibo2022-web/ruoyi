package plus.ruoyi.system.monitor.dao;

import plus.ruoyi.common.mybatis.core.dao.IBaseDao;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.system.monitor.domain.SysErrorLog;
import plus.ruoyi.system.monitor.domain.bo.SysErrorLogBo;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

/**
 * 错误日志DAO接口
 *
 * @author 抓蛙师
 */
public interface ISysErrorLogDao extends IBaseDao<SysErrorLog> {

    /**
     * 根据业务对象构建查询条件
     *
     * @param bo 查询参数
     * @return 查询包装器
     */
    PlusLambdaQuery<SysErrorLog> buildQueryWrapper(SysErrorLogBo bo);

    /**
     * 增加错误发生次数并更新最后发生时间
     *
     * @param id 错误日志ID
     * @return 影响行数
     */
    int incrementOccurrenceCount(Long id);

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
    int updateHandleStatusById(Long id, String handleStatus, Long handleBy, Date handleTime, String handleRemark);

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
    int updateHandleStatusBatch(Collection<Long> ids, String handleStatus, Long handleBy, Date handleTime, String handleRemark);

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
    int updateHandleStatusBySameError(String tenantId, String errorType, String errorCode, String errorMessage,
                                      String requestPattern, String requestMethod, String handleStatus, Long handleBy,
                                      Date handleTime, String handleRemark);

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
    int updateHandleStatusBySameRequest(String tenantId, String requestPattern, String requestMethod,
                                        String handleStatus, Long handleBy, Date handleTime, String handleRemark);
}
