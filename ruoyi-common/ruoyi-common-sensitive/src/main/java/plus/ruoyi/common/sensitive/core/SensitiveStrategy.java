package plus.ruoyi.common.sensitive.core;

import cn.hutool.core.util.DesensitizedUtil;
import lombok.AllArgsConstructor;
import plus.ruoyi.common.sensitive.utils.DesensitizedUtils;

import java.util.function.Function;

/**
 * 数据脱敏策略枚举
 *
 * <p>定义各种常见敏感数据的脱敏处理策略，基于Hutool的DesensitizedUtil工具类实现。
 *
 * <p>脱敏效果示例：
 * <ul>
 *   <li>手机号：138****8888</li>
 *   <li>身份证：110***********1234</li>
 *   <li>邮箱：t**@example.com</li>
 *   <li>银行卡：6222***********1234</li>
 *   <li>中文名：张*</li>
 *   <li>地址：北京市朝阳区****</li>
 * </ul>
 *
 * <p>扩展说明：可根据业务需要添加自定义脱敏策略，实现Function&lt;String, String&gt;接口即可。
 *
 * @author Yjoioooo
 * @version 3.6.0
 */
@AllArgsConstructor
public enum SensitiveStrategy {

    /**
     * 身份证脱敏 - 保留前3位和后4位
     */
    ID_CARD(s -> DesensitizedUtil.idCardNum(s, 3, 4)),

    /**
     * 手机号脱敏 - 保留前3位和后4位
     */
    PHONE(DesensitizedUtil::mobilePhone),

    /**
     * 地址脱敏 - 保留前8个字符
     */
    ADDRESS(s -> DesensitizedUtil.address(s, 8)),

    /**
     * 邮箱脱敏 - 保留用户名首尾字符和完整域名
     */
    EMAIL(DesensitizedUtil::email),

    /**
     * 银行卡脱敏 - 保留前4位和后4位
     */
    BANK_CARD(DesensitizedUtil::bankCard),

    /**
     * 中文姓名脱敏 - 保留姓氏，名字用*代替
     */
    CHINESE_NAME(DesensitizedUtil::chineseName),

    /**
     * 固定电话脱敏 - 保留区号和后4位
     */
    FIXED_PHONE(DesensitizedUtil::fixedPhone),

    /**
     * 用户ID脱敏 - 生成随机数字替代
     */
    USER_ID(s -> String.valueOf(DesensitizedUtil.userId())),

    /**
     * 密码脱敏 - 全部用*代替
     */
    PASSWORD(DesensitizedUtil::password),

    /**
     * IPv4地址脱敏 - 保留网络段，隐藏主机段
     */
    IPV4(DesensitizedUtil::ipv4),

    /**
     * IPv6地址脱敏 - 保留前缀，隐藏接口标识
     */
    IPV6(DesensitizedUtil::ipv6),

    /**
     * 车牌号脱敏 - 支持普通车辆和新能源车辆
     */
    CAR_LICENSE(DesensitizedUtil::carLicense),

    /**
     * 首字符保留脱敏 - 只显示第一个字符，其余用*代替
     */
    FIRST_MASK(DesensitizedUtil::firstMask),

    /**
     * 通用字符串脱敏 - 可配置前后可见长度和中间掩码长度
     * <p>默认：前4位可见，后4位可见，中间固定4个*
     * <p>示例：1234567890 → 1234**7890
     */
    STRING_MASK(s -> DesensitizedUtils.mask(s, 4, 4, 4)),

    /**
     * 清空脱敏 - 返回空字符串
     */
    CLEAR(s -> DesensitizedUtil.clear()),

    /**
     * 置空脱敏 - 返回null
     */
    CLEAR_TO_NULL(s -> DesensitizedUtil.clearToNull());

    // 可根据业务需要添加其他脱敏策略

    /**
     * 脱敏处理函数
     */
    private final Function<String, String> desensitizer;

    /**
     * 获取脱敏处理函数
     *
     * @return 脱敏处理函数
     */
    public Function<String, String> desensitizer() {
        return desensitizer;
    }
}
