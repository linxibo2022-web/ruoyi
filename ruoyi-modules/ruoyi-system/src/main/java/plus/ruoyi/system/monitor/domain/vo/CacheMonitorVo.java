package plus.ruoyi.system.monitor.domain.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 缓存监控信息视图对象
 * <p>
 * 用于封装Redis缓存的监控数据，包括服务器基本信息、数据库大小和命令统计信息。
 * 主要用于系统监控页面展示缓存运行状态和性能指标。
 * </p>
 *
 * @author 抓蛙师
 */
@Data
public class CacheMonitorVo {

    /**
     * Redis服务器信息
     * <p>
     * 包含Redis服务器的详细配置和运行状态信息，如：
     * <ul>
     *   <li>服务器版本信息</li>
     *   <li>内存使用情况</li>
     *   <li>连接数统计</li>
     *   <li>持久化配置</li>
     *   <li>复制信息</li>
     *   <li>CPU使用情况等</li>
     * </ul>
     * </p>
     */
    private Properties info;

    /**
     * 数据库键的数量
     * <p>
     * 当前Redis数据库中存储的键(key)的总数量。
     * 可用于监控缓存数据的规模和变化趋势。
     * </p>
     */
    private Long dbSize;

    /**
     * Redis命令执行统计信息
     * <p>
     * 包含各个Redis命令的执行次数统计，每个Map包含：
     * <ul>
     *   <li>name: 命令名称（如get、set、hget等）</li>
     *   <li>value: 该命令的执行次数</li>
     * </ul>
     * 主要用于分析缓存访问模式和性能瓶颈。
     * </p>
     */
    private List<Map<String, String>> commandStats;

}
