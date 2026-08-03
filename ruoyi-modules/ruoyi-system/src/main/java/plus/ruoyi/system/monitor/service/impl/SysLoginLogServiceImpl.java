package plus.ruoyi.system.monitor.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.utils.MapstructUtils;
import plus.ruoyi.common.core.utils.ServletUtils;
import plus.ruoyi.common.core.utils.ip.AddressUtils;
import plus.ruoyi.common.log.event.LoginLogEvent;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.system.monitor.dao.ISysLoginLogDao;
import plus.ruoyi.system.monitor.domain.SysLoginLog;
import plus.ruoyi.system.monitor.domain.bo.SysLoginLogBo;
import plus.ruoyi.system.monitor.domain.vo.SysLoginLogVo;
import plus.ruoyi.system.monitor.service.ISysLoginLogService;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * 登录日志服务实现
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class SysLoginLogServiceImpl implements ISysLoginLogService {

    private final ISysLoginLogDao loginLogDao;

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return 视图对象
     */
    @Override
    public SysLoginLogVo get(Long id) {
        SysLoginLog entity = loginLogDao.getById(id);
        return MapstructUtils.convert(entity, SysLoginLogVo.class);
    }

    /**
     * 查询列表
     *
     * @param bo 查询参数
     * @return 列表数据
     */
    @Override
    public List<SysLoginLogVo> list(SysLoginLogBo bo) {
        PlusLambdaQuery<SysLoginLog> wrapper = loginLogDao.buildQueryWrapper(bo);
        List<SysLoginLog> entities = loginLogDao.list(wrapper);
        return MapstructUtils.convert(entities, SysLoginLogVo.class);
    }

    /**
     * 分页查询
     *
     * @param bo        查询参数
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    @Override
    public PageResult<SysLoginLogVo> page(SysLoginLogBo bo, PageQuery pageQuery) {
        PlusLambdaQuery<SysLoginLog> wrapper = loginLogDao.buildQueryWrapper(bo);
        PageResult<SysLoginLog> entityPage = loginLogDao.page(wrapper, pageQuery);
        return entityPage.convert(SysLoginLogVo.class);
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
        return loginLogDao.deleteByIds(ids);
    }

    /**
     * 清空系统登录日志
     */
    @Override
    public void clearLoginLogs() {
        loginLogDao.delete(null);
    }

    /**
     * 记录登录信息
     *
     * @param loginLogEvent 登录事件
     */
    @Async
    @EventListener
    public void recordLoginLog(LoginLogEvent loginLogEvent) {
        HttpServletRequest request = loginLogEvent.getRequest();
        final UserAgent userAgent = UserAgentUtil.parse(request.getHeader("User-Agent"));
        final String ip = ServletUtils.getClientIP(request);
        String address = AddressUtils.getRealAddressByIp(ip);
        StringBuilder s = new StringBuilder();
        s.append(getBlock(ip));
        s.append(address);
        s.append(getBlock(loginLogEvent.getUserId()));
        s.append(getBlock(loginLogEvent.getUserName()));
        s.append(getBlock(loginLogEvent.getStatus()));
        s.append(getBlock(loginLogEvent.getMessage()));
        // 打印信息到日志
        log.info(s.toString(), loginLogEvent.getArgs());
        // 获取客户端操作系统
        String os = userAgent.getOs().getName();
        // 获取客户端浏览器
        String browser = userAgent.getBrowser().getName();
        // 封装对象
        SysLoginLogBo bo = new SysLoginLogBo();
        bo.setTenantId(loginLogEvent.getTenantId());
        bo.setUserId(loginLogEvent.getUserId());
        bo.setUserName(loginLogEvent.getUserName());
        bo.setDeviceType(loginLogEvent.getDeviceType());
        bo.setIpaddr(ip);
        bo.setLoginLocation(address);
        bo.setBrowser(browser);
        bo.setOs(os);
        bo.setMsg(loginLogEvent.getMessage());
        // 日志状态
        bo.setStatus(loginLogEvent.getStatus());
        // 插入数据
        SysLoginLog loginLog = MapstructUtils.convert(bo, SysLoginLog.class);
        loginLog.setLoginTime(new Date());
        loginLogDao.insert(loginLog);
    }

    private String getBlock(Object msg) {
        if (msg == null) {
            msg = "";
        }
        return "[" + msg.toString() + "]";
    }

}
