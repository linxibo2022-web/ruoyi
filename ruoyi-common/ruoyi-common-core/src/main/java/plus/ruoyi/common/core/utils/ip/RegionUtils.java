package plus.ruoyi.common.core.utils.ip;

import cn.hutool.core.io.resource.ResourceUtil;
import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.service.Config;
import org.lionsoul.ip2region.service.Ip2Region;
import org.lionsoul.ip2region.xdb.Util;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.utils.StringUtils;

import java.io.InputStream;
import java.time.Duration;

/**
 * IP地址行政区域工具类
 *
 * <p>基于 ip2region 3.x 实现离线IP地址到地理位置的转换，支持 IPv4 和 IPv6</p>
 *
 * @see <a href="https://gitee.com/lionsoul/ip2region/tree/master/binding/java">ip2region Java绑定</a>
 * @author 秋辞未寒
 */
@Slf4j
public class RegionUtils {

    /**
     * 默认IPv4地址库文件路径
     * 下载地址：https://gitee.com/lionsoul/ip2region/blob/master/data/ip2region_v4.xdb
     */
    public static final String DEFAULT_IPV4_XDB_PATH = "ip2region_v4.xdb";

    /**
     * 默认IPv6地址库文件路径
     * 下载地址：https://gitee.com/lionsoul/ip2region/blob/master/data/ip2region_v6.xdb
     */
    public static final String DEFAULT_IPV6_XDB_PATH = "ip2region_v6.xdb";

    /**
     * 默认缓存切片大小为15MB（仅针对BufferCache全量读取有效）
     * 注意：设置过大的值可能会因内存不足而导致OOM，请合理设置该值
     */
    public static final int DEFAULT_CACHE_SLICE_BYTES = 1024 * 1024 * 15;

    /**
     * 未知地址标识
     */
    public static final String UNKNOWN_ADDRESS = "未知";

    /**
     * Ip2Region服务实例
     */
    private static Ip2Region ip2Region;

    static {
        try {
            // 注意：Ip2Region 的xdb文件加载策略 CachePolicy 有三种：
            // BufferCache（全量读取xdb到内存中）、VIndexCache（默认策略，按需读取并缓存）、NoCache（实时读取）
            // 本项目使用 BufferCache 策略，会加载整个xdb文件到内存中

            InputStream v4InputStream = ResourceUtil.getStream(DEFAULT_IPV4_XDB_PATH);

            // IPv4配置
            Config v4Config = Config.custom()
                .setCachePolicy(Config.BufferCache)
                .setXdbInputStream(v4InputStream)
                .setCacheSliceBytes(DEFAULT_CACHE_SLICE_BYTES)
                .asV4();

            // IPv6配置（可选）
            Config v6Config = null;
            InputStream v6XdbInputStream = ResourceUtil.getStreamSafe(DEFAULT_IPV6_XDB_PATH);
            if (v6XdbInputStream == null) {
                log.warn("未加载 IPv6 地址库：未在类路径下找到文件 {}。当前仅启用 IPv4 查询。如需启用 IPv6，请将 ip2region_v6.xdb 放置到 resources 目录", DEFAULT_IPV6_XDB_PATH);
            } else {
                v6Config = Config.custom()
                    .setCachePolicy(Config.BufferCache)
                    .setXdbInputStream(v6XdbInputStream)
                    .setCacheSliceBytes(DEFAULT_CACHE_SLICE_BYTES)
                    .asV6();
            }

            // 初始化Ip2Region实例
            RegionUtils.ip2Region = Ip2Region.create(v4Config, v6Config);
            log.debug("IP工具初始化成功，加载IP地址库数据成功！");
        } catch (Exception e) {
            throw ServiceException.of("RegionUtils初始化失败，原因：" + e.getMessage());
        }
    }

    /**
     * 根据IP地址离线获取城市
     *
     * @param ipString ip地址字符串
     * @return 城市信息，异常时返回"未知"
     */
    public static String getRegion(String ipString) {
        try {
            String region = ip2Region.search(ipString);
            if (StringUtils.isBlank(region)) {
                return UNKNOWN_ADDRESS;
            }
            return StringUtils.replace(region, "0", UNKNOWN_ADDRESS);
        } catch (Exception e) {
            log.error("IP地址离线获取城市异常 {}", ipString);
            return UNKNOWN_ADDRESS;
        }
    }

    /**
     * 根据IP地址离线获取城市
     *
     * @param ipBytes ip地址字节数组
     * @return 城市信息，异常时返回"未知"
     */
    public static String getRegion(byte[] ipBytes) {
        try {
            String region = ip2Region.search(ipBytes);
            if (StringUtils.isBlank(region)) {
                return UNKNOWN_ADDRESS;
            }
            return StringUtils.replace(region, "0", UNKNOWN_ADDRESS);
        } catch (Exception e) {
            log.error("IP地址离线获取城市异常 {}", Util.ipToString(ipBytes));
            return UNKNOWN_ADDRESS;
        }
    }

    /**
     * 关闭Ip2Region服务
     */
    public static void close() {
        if (ip2Region == null) {
            return;
        }
        try {
            ip2Region.close(10000);
        } catch (Exception e) {
            log.error("Ip2Region服务关闭异常", e);
        }
    }

    /**
     * 关闭Ip2Region服务
     *
     * @param timeout 关闭超时时间
     */
    public static void close(final Duration timeout) {
        if (ip2Region == null) {
            return;
        }
        if (timeout == null) {
            close();
            return;
        }
        try {
            ip2Region.close(timeout.toMillis());
        } catch (Exception e) {
            log.error("Ip2Region服务关闭异常", e);
        }
    }

}
