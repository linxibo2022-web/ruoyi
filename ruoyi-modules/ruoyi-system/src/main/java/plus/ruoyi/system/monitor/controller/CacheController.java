package plus.ruoyi.system.monitor.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.data.redis.core.RedisConnectionUtils;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.system.monitor.domain.vo.CacheMonitorVo;
import lombok.RequiredArgsConstructor;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * 缓存监控控制器
 * <p>
 * 提供Redis缓存监控相关的REST API接口。
 * 主要功能包括获取缓存服务器信息、数据库统计、命令执行统计等监控数据。
 * 用于系统管理员监控缓存服务的运行状态和性能指标。
 * </p>
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/monitor/cache")
public class CacheController {

    /**
     * Redisson连接工厂
     * <p>
     * 用于获取Redis连接，执行监控相关的命令操作。
     * 通过构造函数注入，确保连接的可用性和线程安全。
     * </p>
     */
    private final RedissonConnectionFactory connectionFactory;

    /**
     * 获取缓存监控信息
     * <p>
     * 查询Redis服务器的详细监控数据，包括：
     * <ul>
     *   <li>服务器基本信息（内存、CPU、连接数等）</li>
     *   <li>当前数据库的键数量</li>
     *   <li>各命令的执行统计（调用次数）</li>
     * </ul>
     * </p>
     *
     * @return 包含缓存监控信息的响应对象
     * @throws Exception 当Redis连接异常或命令执行失败时抛出
     * @apiNote 需要 monitor:cache:query 权限
     * @see CacheMonitorVo 返回数据的详细结构
     */
    @SaCheckPermission("monitor:cache:query")
    @GetMapping("/getCacheInfo")
    public R<CacheMonitorVo> getCacheInfo() throws Exception {
        // 获取Redis连接
        RedisConnection connection = connectionFactory.getConnection();
        try {
            // 获取命令统计信息
            Properties commandStats = connection.commands().info("commandstats");

            // 解析命令统计数据，转换为前端所需格式
            List<Map<String, String>> pieList = new ArrayList<>();
            if (commandStats != null) {
                commandStats.stringPropertyNames().forEach(key -> {
                    Map<String, String> data = new HashMap<>(2);
                    String property = commandStats.getProperty(key);
                    // 提取命令名称（移除cmdstat_前缀）
                    data.put("name", StringUtils.removeStart(key, "cmdstat_"));
                    // 提取调用次数（从calls=xxx,usec中解析）
                    data.put("value", StringUtils.substringBetween(property, "calls=", ",usec"));
                    pieList.add(data);
                });
            }

            // 构建监控信息对象
            CacheMonitorVo monitorVo = new CacheMonitorVo();
            monitorVo.setInfo(connection.commands().info());
            monitorVo.setDbSize(connection.commands().dbSize());
            monitorVo.setCommandStats(pieList);

            return R.ok(monitorVo);
        } finally {
            // 归还连接给连接池
            RedisConnectionUtils.releaseConnection(connection, connectionFactory);
        }
    }

}
