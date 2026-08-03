package plus.ruoyi.system.monitor.service;

import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.system.monitor.domain.bo.SysLoginLogBo;
import plus.ruoyi.system.monitor.domain.vo.SysLoginLogVo;

import java.util.Collection;
import java.util.List;

/**
 * 登录日志服务接口
 *
 * @author Lion Li
 */
public interface ISysLoginLogService {

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return 视图对象
     */
    SysLoginLogVo get(Long id);

    /**
     * 查询列表
     *
     * @param bo 查询参数
     * @return 列表数据
     */
    List<SysLoginLogVo> list(SysLoginLogBo bo);

    /**
     * 分页查询
     *
     * @param bo        查询参数
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    PageResult<SysLoginLogVo> page(SysLoginLogBo bo, PageQuery pageQuery);

    /**
     * 批量删除
     *
     * @param ids ID集合
     * @return 影响行数
     */
    int batchDelete(Collection<Long> ids);

    /**
     * 清空系统登录日志
     */
    void clearLoginLogs();
}
