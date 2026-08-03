package plus.ruoyi.system.monitor.service;

import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.system.monitor.domain.bo.SysErrorLogBo;
import plus.ruoyi.system.monitor.domain.vo.SysErrorLogVo;

import java.util.Collection;
import java.util.List;

/**
 * 错误日志服务接口
 *
 * @author 抓蛙师
 */
public interface ISysErrorLogService {

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return 视图对象
     */
    SysErrorLogVo get(Long id);

    /**
     * 查询列表
     *
     * @param bo 查询参数
     * @return 列表数据
     */
    List<SysErrorLogVo> list(SysErrorLogBo bo);

    /**
     * 分页查询
     *
     * @param bo        查询参数
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    PageResult<SysErrorLogVo> page(SysErrorLogBo bo, PageQuery pageQuery);

    /**
     * 更新处理状态
     *
     * @param bo 业务对象（包含处理状态、处理人、处理备注）
     * @return 影响行数
     */
    int updateHandleStatus(SysErrorLogBo bo);

    /**
     * 按ID集合批量更新处理状态
     *
     * @param bo 业务对象（包含处理状态、处理人、处理备注、ID集合）
     * @return 影响行数
     */
    int updateHandleStatusBatch(SysErrorLogBo bo);

    /**
     * 按相同错误批量更新处理状态
     *
     * @param bo 业务对象（包含处理状态、处理人、处理备注、ID）
     * @return 影响行数
     */
    int updateHandleStatusBySameError(SysErrorLogBo bo);

    /**
     * 按相同接口批量更新处理状态
     *
     * @param bo 业务对象（包含处理状态、处理人、处理备注、ID）
     * @return 影响行数
     */
    int updateHandleStatusBySameRequest(SysErrorLogBo bo);

    /**
     * 批量删除
     *
     * @param ids ID集合
     * @return 影响行数
     */
    int batchDelete(Collection<Long> ids);

    /**
     * 清空错误日志
     */
    void clearErrorLogs();
}
