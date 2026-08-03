package plus.ruoyi.common.core.enums;

import plus.ruoyi.common.core.utils.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户类型
 * 针对不同设备类型的用户体系
 *
 * @author Lion Li
 */
@Getter
@AllArgsConstructor
public enum UserType {

    /**
     * PC端用户
     * - 主要包括台式机、笔记本等传统电脑设备
     * - 活跃超时12小时：适合工作日内的办公场景使用
     * - 固定超时7天：定期要求重新验证身份
     */
    PC_USER("pc", "pc_user", 43200, 604800),

    /**
     * 移动端用户
     * - 活跃超时2天：移动设备使用场景下较长的活跃期
     * - 固定超时30天：较长的固定期提升移动端用户体验
     */
    APP_USER("app", "app_user", 172800, 2592000),

    /**
     * OpenAPI用户
     * - 用于API接口调用的用户类型
     * - 活跃超时12小时：API接口使用场景下较长的活跃期
     * - 固定超时7天：API接口使用场景下更长的固定期
     */
    OPENAPI_USER("openapi", "openapi_user", 43200, 604800);

    /**
     * 设备类型
     * 表示该用户类型使用的设备或应用类型
     * 例如：pc、app等
     * 用于Sa-Token框架的device标识
     */
    private final String deviceType;

    /**
     * 用户类型标识
     * 用于在数据库和请求参数中标识不同类型的用户
     */
    private final String userType;

    /**
     * token活跃超时时间（秒）
     * 从用户最后一次操作开始计时，超过此时间无操作则token失效
     */
    private final int activeTimeout;

    /**
     * token固定超时时间（秒）
     * 从用户登录时开始计时，不论用户是否活跃，达到此时间后token强制失效
     */
    private final int timeout;

    /**
     * 根据字符串获取对应的用户类型枚举
     * <p>
     * 该方法通过检查传入字符串是否包含用户类型标识来匹配枚举值。
     * 注意：此方法使用的是包含关系匹配（而非精确匹配），因此当字符串包含任何用户类型标识时，
     * 将返回第一个匹配的枚举值。例如，"pc_user:1" 会匹配到 PC_USER。
     *
     * @param str 包含用户类型标识的字符串，如"pc_user"、"app_user"等
     * @return 匹配到的用户类型枚举值
     * @throws RuntimeException 当未能找到匹配的用户类型时抛出此异常
     */
    public static UserType getUserType(String str) {
        for (UserType value : values()) {
            if (StringUtils.contains(str, value.getUserType())) {
                return value;
            }
        }
        throw new RuntimeException("'UserType' not found By " + str);
    }
}
