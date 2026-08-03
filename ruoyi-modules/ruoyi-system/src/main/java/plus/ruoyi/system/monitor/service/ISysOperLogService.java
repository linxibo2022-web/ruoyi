package plus.ruoyi.system.monitor.service;

import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.system.monitor.domain.bo.SysOperLogBo;
import plus.ruoyi.system.monitor.domain.vo.SysOperLogVo;

import java.util.Collection;
import java.util.List;

/**
 * 操作日志服务接口
 *
 * @author Lion Li
 */
public interface ISysOperLogService {

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return 视图对象
     */
    SysOperLogVo get(Long id);

    /**
     * 查询列表
     *
     * @param bo 查询参数
     * @return 列表数据
     */
    List<SysOperLogVo> list(SysOperLogBo bo);

    /**
     * 分页查询
     *
     * @param bo        查询参数
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    PageResult<SysOperLogVo> page(SysOperLogBo bo, PageQuery pageQuery);

    /**
     * 批量删除
     *
     * @param ids ID集合
     * @return 影响行数
     */
    int batchDelete(Collection<Long> ids);

    /**
     * 清空系统操作日志
     */
    void clearOperLogs();
}
