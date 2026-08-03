package plus.ruoyi.common.core.utils.ip;

import cn.hutool.http.HtmlUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.utils.NetUtils;
import plus.ruoyi.common.core.utils.StringUtils;

/**
 * 获取地址类
 *
 * @author Lion Li
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AddressUtils {

    /**
     * 未知IP标识
     */
    public static final String UNKNOWN_IP = "XX XX";

    /**
     * 内网地址标识
     */
    public static final String LOCAL_ADDRESS = "内网IP";

    /**
     * 获取IP地址对应的真实地理位置
     *
     * <p>支持IPv4和IPv6地址解析。对于内网地址会直接返回内网标识，
     * 无效地址返回未知标识。输入会自动进行HTML标签清理和空值处理。</p>
     *
     * @param ip IP地址字符串，支持IPv4和IPv6格式
     * @return 地理位置信息，可能的返回值：
     * <ul>
     * <li>具体地理位置（如：北京市 北京市）</li>
     * <li>{@link #LOCAL_ADDRESS} - 内网地址</li>
     * <li>{@link #UNKNOWN_IP} - 无效IP地址</li>
     * </ul>
     */
    public static String getRealAddressByIp(String ip) {
        // 处理空串并过滤HTML标签
        ip = HtmlUtil.cleanHtmlTag(StringUtils.blankToDefault(ip, ""));
        // 判断是否为IPv4
        boolean isIPv4 = NetUtils.isIpv4(ip);
        // 判断是否为IPv6
        boolean isIPv6 = NetUtils.isIpv6(ip);
        // 如果不是IPv4或IPv6，则返回未知IP
        if (!isIPv4 && !isIPv6) {
            return UNKNOWN_IP;
        }
        // 内网不查询
        if ((isIPv4 && NetUtils.isInnerIP(ip)) || (isIPv6 && NetUtils.isInnerIpv6(ip))) {
            return LOCAL_ADDRESS;
        }
        // Tips：Ip2Region 提供了精简的IPv6地址库，精简的IPv6地址库并不能完全支持IPv6地址的查询，且准确度上可能会存在问题，如需要准确的IPv6地址查询，建议自行实现
        return RegionUtils.getRegion(ip);
    }

}
