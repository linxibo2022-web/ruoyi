package plus.ruoyi.common.core.utils;

import cn.hutool.core.lang.PatternPool;
import cn.hutool.core.net.NetUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.utils.regex.RegexUtils;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 网络工具类
 * <p>基于HuTool的NetUtil进行扩展，提供IPv4/IPv6地址识别、内网地址判断等网络相关功能</p>
 * <p>主要用于IP地址格式验证、网络环境判断等场景</p>
 *
 * @author 秋辞未寒
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NetUtils extends NetUtil {

    /**
     * 判断给定的IP地址字符串是否为IPv4格式
     * <p>使用正则表达式进行IPv4地址格式验证，支持标准的点分十进制格式</p>
     * <p>验证范围：0.0.0.0 - 255.255.255.255</p>
     *
     * @param ipAddress IP地址字符串，如 "192.168.1.1" 或 "10.0.0.1"
     * @return true表示是有效的IPv4地址格式，false表示格式不正确
     */
    public static boolean isIpv4(String ipAddress) {
        return RegexUtils.isMatch(PatternPool.IPV4, ipAddress);
    }

    /**
     * 判断给定的IP地址字符串是否为IPv6格式
     * <p>通过Java原生API进行地址解析和类型判断，支持标准IPv6格式识别</p>
     *
     * @param ipAddress IP地址字符串，如 "2001:db8::1" 或 "192.168.1.1"
     * @return true表示是IPv6地址，false表示不是IPv6地址或格式无效
     */
    public static boolean isIpv6(String ipAddress) {
        try {
            // 通过InetAddress解析并判断是否为IPv6地址类型
            return InetAddress.getByName(ipAddress) instanceof Inet6Address;
        } catch (UnknownHostException e) {
            // 地址格式无效时返回false
            return false;
        }
    }

    /**
     * 判断IPv6地址是否为内网地址（本地地址）
     * <p>根据RFC标准定义，以下IPv6地址类型被视为内网地址：</p>
     * <ul>
     *     <li><strong>通配符地址</strong>：0:0:0:0:0:0:0:0 (::)</li>
     *     <li><strong>链路本地地址</strong>：fe80::/10</li>
     *     <li><strong>唯一本地地址</strong>：fc00::/7 (包含fec0::/10)</li>
     *     <li><strong>环回地址</strong>：::1</li>
     * </ul>
     * <p><b>注意</b>：不同业务场景对内网地址的定义可能有所差异，请根据实际需求调整判断逻辑</p>
     *
     * @param ipAddress IPv6地址字符串
     * @return true表示是内网地址，false表示是公网地址
     * @throws IllegalArgumentException 当IP地址格式无效时抛出
     */
    public static boolean isInnerIpv6(String ipAddress) {
        try {
            // 确保是IPv6地址并进行内网判断
            if (InetAddress.getByName(ipAddress) instanceof Inet6Address inet6Address) {
                // isAnyLocalAddress(): 通配符地址 (::)，表示所有本地地址
                // isLinkLocalAddress(): 链路本地地址 (fe80::/10)，仅在本地链路有效
                // isLoopbackAddress(): 环回地址 (::1)，等同于IPv4的127.0.0.1
                // isSiteLocalAddress(): 站点本地地址 (fec0::/10)，IPv6唯一本地地址
                return inet6Address.isAnyLocalAddress()
                    || inet6Address.isLinkLocalAddress()
                    || inet6Address.isLoopbackAddress()
                    || inet6Address.isSiteLocalAddress();
            }
        } catch (UnknownHostException e) {
            // IPv6地址格式校验失败，抛出明确的异常信息
            throw new IllegalArgumentException("无效的IPv6地址格式: " + ipAddress, e);
        }
        return false;
    }

}
