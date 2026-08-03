/**
 * 缓存监控信息视图对象
 *
 * 用于封装Redis缓存的监控数据，包括服务器基本信息、数据库大小和命令统计信息。
 * 主要用于系统监控页面展示缓存运行状态和性能指标。
 *
 * @interface CacheMonitorVo
 * @author 抓蛙师
 */
export interface CacheMonitorVo {
  /**
   * Redis命令执行统计信息
   *
   * 包含各个Redis命令的执行次数统计，用于分析缓存访问模式和性能瓶颈。
   * 每个对象包含：
   * - name: 命令名称（如get、set、hget等）
   * - value: 该命令的执行次数（字符串格式）
   *
   * @example
   * [
   *   { name: "get", value: "12345" },
   *   { name: "set", value: "8901" },
   *   { name: "hget", value: "2345" }
   * ]
   */
  commandStats: Array<{
    /** 命令名称 */
    name: string
    /** 执行次数 */
    value: string
  }>

  /**
   * 数据库键的数量
   *
   * 当前Redis数据库中存储的键(key)的总数量。
   * 可用于监控缓存数据的规模和变化趋势。
   *
   * @example 15234
   */
  dbSize: number

  /**
   * Redis服务器详细信息
   *
   * 包含Redis服务器的配置和运行状态信息，采用键值对形式存储。
   * 主要包含以下类型的信息：
   * - 服务器版本和编译信息
   * - 内存使用情况和配置
   * - 连接数和网络统计
   * - 持久化配置和状态
   * - 复制和集群信息
   * - CPU使用情况和统计
   *
   * @example
   * {
   *   "redis_version": "6.2.7",
   *   "used_memory": "2048576",
   *   "connected_clients": "10",
   *   "total_connections_received": "1000"
   * }
   */
  info: { [key: string]: string }
}
