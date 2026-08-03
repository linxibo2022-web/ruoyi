package plus.ruoyi.system.monitor.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.utils.MapstructUtils;
import plus.ruoyi.common.core.utils.ip.AddressUtils;
import plus.ruoyi.common.log.event.OperLogEvent;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.system.monitor.dao.ISysOperLogDao;
import plus.ruoyi.system.monitor.domain.SysOperLog;
import plus.ruoyi.system.monitor.domain.bo.SysOperLogBo;
import plus.ruoyi.system.monitor.domain.vo.SysOperLogVo;
import plus.ruoyi.system.monitor.service.ISysOperLogService;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * 操作日志服务实现
 *
 * @author Lion Li
 */
@Service
@RequiredArgsConstructor
public class SysOperLogServiceImpl implements ISysOperLogService {

    private final ISysOperLogDao operLogDao;

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return 视图对象
     */
    @Override
    public SysOperLogVo get(Long id) {
        SysOperLog entity = operLogDao.getById(id);
        return MapstructUtils.convert(entity, SysOperLogVo.class);
    }

    /**
     * 查询列表
     *
     * @param bo 查询参数
     * @return 列表数据
     */
    @Override
    public List<SysOperLogVo> list(SysOperLogBo bo) {
        PlusLambdaQuery<SysOperLog> wrapper = operLogDao.buildQueryWrapper(bo);
        List<SysOperLog> entities = operLogDao.list(wrapper);
        return MapstructUtils.convert(entities, SysOperLogVo.class);
    }

    /**
     * 分页查询
     *
     * @param bo        查询参数
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    @Override
    public PageResult<SysOperLogVo> page(SysOperLogBo bo, PageQuery pageQuery) {
        PlusLambdaQuery<SysOperLog> wrapper = operLogDao.buildQueryWrapper(bo);
        PageResult<SysOperLog> entityPage = operLogDao.page(wrapper, pageQuery);
        return entityPage.convert(SysOperLogVo.class);
    }

    /**
     * 批量删除
     *
     * @param ids ID集合
     * @return 影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            throw ServiceException.of("ID集合不能为空");
        }
        return operLogDao.deleteByIds(ids);
    }

    /**
     * 清空系统操作日志
     */
    @Override
    public void clearOperLogs() {
        operLogDao.delete(null);
    }

    /**
     * 操作日志
     *
     * @param operLogEvent 操作日志事件
     */
    @Async
    @EventListener
    public void recordOper(OperLogEvent operLogEvent) {
        SysOperLog operLog = BeanUtil.toBean(operLogEvent, SysOperLog.class);
        // 远程查询操作地点
        operLog.setOperLocation(AddressUtils.getRealAddressByIp(operLog.getOperIp()));
        operLog.setOperTime(new Date());
        operLogDao.insert(operLog);
    }

}
