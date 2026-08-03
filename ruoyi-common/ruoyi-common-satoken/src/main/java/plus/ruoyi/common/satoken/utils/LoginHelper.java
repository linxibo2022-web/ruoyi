package plus.ruoyi.common.satoken.utils;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import plus.ruoyi.common.core.constant.SystemConstants;
import plus.ruoyi.common.core.constant.TenantConstants;
import plus.ruoyi.common.core.domain.model.LoginUser;
import plus.ruoyi.common.core.enums.UserType;

import java.util.Collections;
import java.util.Set;

/**
 * 登录鉴权助手工具类
 *
 * <p>基于Sa-Token框架的登录认证和权限管理工具，支持多用户体系和多设备类型：
 * <ul>
 *   <li>多用户体系：同一用户表支持多种用户类型(PC、APP等)</li>
 *   <li>多设备支持：同一用户类型支持多种设备(Web、iOS、Android等)</li>
 *   <li>灵活权限控制：用户类型与设备类型多对多的权限管理</li>
 *   <li>租户隔离：支持多租户环境下的用户管理</li>
 * </ul>
 *
 * <p>架构设计：
 * <pre>
 * 用户类型(UserType) × 设备类型(Device) = 灵活的权限控制矩阵
 * 例如：PC用户可以访问管理功能，APP用户只能访问基础功能
 * </pre>
 *
 * <p>使用示例：
 * <pre>
 * // 用户登录
 * LoginHelper.login(loginUser, loginParameter);
 *
 * // 获取当前用户信息
 * LoginUser user = LoginHelper.getLoginUser();
 *
 * // 获取用户ID和租户信息
 * Long userId = LoginHelper.getUserId();
 * String tenantId = LoginHelper.getTenantId();
 *
 * // 获取微信相关信息
 * String appid = LoginHelper.getAppid();
 * String unionid = LoginHelper.getUnionid();
 * String openid = LoginHelper.getOpenid();
 *
 * // 权限判断
 * boolean isSuperAdmin = LoginHelper.isSuperAdmin();
 * boolean isTenantAdmin = LoginHelper.isTenantAdmin();
 * </pre>
 *
 * @author Lion Li
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginHelper {

    /**
     * Session中存储的登录用户信息key
     */
    public static final String LOGIN_USER = "loginUser";
    /**
     * Token扩展信息中的租户ID key
     */
    public static final String TENANT_ID = "tenantId";
    /**
     * Token扩展信息中的用户ID key
     */
    public static final String USER_ID = "userId";
    /**
     * Token扩展信息中的用户名 key
     */
    public static final String USER_NAME = "userName";
    /**
     * Token扩展信息中的部门ID key
     */
    public static final String DEPT_ID = "deptId";
    /**
     * Token扩展信息中的部门名称 key
     */
    public static final String DEPT_NAME = "deptName";
    /**
     * Token扩展信息中的部门类别 key
     */
    public static final String DEPT_CATEGORY = "deptCategory";

    /**
     * 用户登录系统
     *
     * <p>支持基于设备类型的登录，适用于相同用户体系的不同设备终端
     *
     * @param loginUser      登录用户信息
     * @param loginParameter 登录参数(设备类型、过期时间等)
     */
    public static void login(LoginUser loginUser, SaLoginParameter loginParameter) {
        loginParameter = ObjectUtil.defaultIfNull(loginParameter, new SaLoginParameter());
        // 执行登录并设置扩展信息
        StpUtil.login(loginUser.getLoginId(),
            loginParameter.setExtra(TENANT_ID, loginUser.getTenantId())
                .setExtra(USER_ID, loginUser.getUserId())
                .setExtra(USER_NAME, loginUser.getUserName())
                .setExtra(DEPT_ID, loginUser.getDeptId())
                .setExtra(DEPT_NAME, loginUser.getDeptName())
                .setExtra(DEPT_CATEGORY, loginUser.getDeptCategory())
        );
        // 在Session中存储完整的用户信息
        StpUtil.getTokenSession().set(LOGIN_USER, loginUser);
    }

    /**
     * 获取当前登录用户信息
     *
     * <p>支持两种场景：
     * <ul>
     *   <li>普通登录：从 Token Session 获取</li>
     *   <li>OpenAPI 请求：从请求上下文 Storage 获取</li>
     * </ul>
     *
     * @param <T> 用户类型泛型
     * @return 当前登录用户，未登录返回null
     */
    @SuppressWarnings("unchecked")
    public static <T extends LoginUser> T getLoginUser() {
        // 优先从 Storage 获取（OpenAPI 场景 + 普通请求都会先检查）
        try {
            T user = (T) SaHolder.getStorage().get(LOGIN_USER);
            if (user != null) {
                return user;
            }
        } catch (Exception ignored) {
            // Storage 不可用时忽略
        }

        // 从 Token Session 获取（普通登录场景）
        SaSession session = StpUtil.getTokenSession();
        if (ObjectUtil.isNull(session)) {
            return null;
        }
        return (T) session.get(LOGIN_USER);
    }

    /**
     * 根据Token获取用户信息
     *
     * @param <T>   用户类型泛型
     * @param token 访问令牌
     * @return 对应的登录用户，Token无效返回null
     */
    @SuppressWarnings("unchecked")
    public static <T extends LoginUser> T getLoginUser(String token) {
        SaSession session = StpUtil.getTokenSessionByToken(token);
        if (ObjectUtil.isNull(session)) {
            return null;
        }
        return (T) session.get(LOGIN_USER);
    }

    /**
     * 获取当前用户ID
     *
     * @return 用户ID
     */
    public static Long getUserId() {
        return Convert.toLong(getExtra(USER_ID));
    }

    /**
     * 获取当前用户ID(字符串形式)
     *
     * @return 用户ID字符串
     */
    public static String getUserIdStr() {
        return Convert.toStr(getExtra(USER_ID));
    }

    /**
     * 获取当前用户账户名
     *
     * @return 用户账户名
     */
    public static String getUserName() {
        return Convert.toStr(getExtra(USER_NAME));
    }

    /**
     * 获取当前用户的租户ID
     *
     * @return 租户ID
     */
    public static String getTenantId() {
        return Convert.toStr(getExtra(TENANT_ID));
    }

    /**
     * 获取当前用户的部门ID
     *
     * @return 部门ID
     */
    public static Long getDeptId() {
        return Convert.toLong(getExtra(DEPT_ID));
    }

    /**
     * 获取当前用户的部门名称
     *
     * @return 部门名称
     */
    public static String getDeptName() {
        return Convert.toStr(getExtra(DEPT_NAME));
    }

    /**
     * 获取当前用户的部门类别编码
     *
     * @return 部门类别编码
     */
    public static String getDeptCategory() {
        return Convert.toStr(getExtra(DEPT_CATEGORY));
    }

    /**
     * 获取当前用户的微信应用ID
     *
     * @return 微信应用ID
     */
    public static String getAppid() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getAppid() : null;
    }

    /**
     * 获取当前用户的微信UnionID
     *
     * @return 微信UnionID
     */
    public static String getUnionid() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getUnionid() : null;
    }

    /**
     * 获取当前用户的微信OpenID
     *
     * @return 微信OpenID
     */
    public static String getOpenid() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getOpenid() : null;
    }

    /**
     * 获取Token的扩展信息
     *
     * @param key 扩展信息键名
     * @return 对应的扩展数据，异常时返回null
     */
    private static Object getExtra(String key) {
        try {
            return StpUtil.getExtra(key);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取当前用户类型
     *
     * @return 用户类型枚举
     */
    public static UserType getUserType() {
        String loginId = StpUtil.getLoginIdAsString();
        return UserType.getUserType(loginId);
    }

    /**
     * 判断指定用户是否为超级管理员（超管用户id 角色id都是1）
     *
     * @param userId 用户ID
     * @return true表示是超级管理员
     */
    public static boolean isSuperAdmin(Long userId) {
        return SystemConstants.SUPER_ADMIN_ID.equals(userId);
    }

    /**
     * 判断当前用户是否为超级管理员
     *
     * @return true表示是超级管理员
     */
    public static boolean isSuperAdmin() {
        return isSuperAdmin(getUserId());
    }

    /**
     * 判断是否为租户管理员(基于角色权限)
     *
     * @param rolePermission 角色权限标识集合
     * @return true表示是租户管理员
     */
    public static boolean isTenantAdmin(Set<String> rolePermission) {
        if (CollUtil.isEmpty(rolePermission)) {
            return false;
        }
        return rolePermission.contains(TenantConstants.TENANT_ADMIN_ROLE_KEY);
    }

    /**
     * 判断当前用户是否为租户管理员
     *
     * @return true表示是租户管理员
     */
    public static boolean isTenantAdmin() {
        LoginUser loginUser = getLoginUser();
        if (loginUser == null) {
            return false;
        }
        return Convert.toBool(isTenantAdmin(loginUser.getRolePermission()));
    }

    /**
     * 获取当前用户的角色标识列表
     *
     * @return 角色标识列表，未登录返回空集合
     */
    public static Set<String> getRoleKeys() {
        LoginUser loginUser = getLoginUser();
        if (loginUser == null) {
            return Collections.emptySet();
        }
        return loginUser.getRolePermission();
    }

    /**
     * 判断当前用户是否拥有指定角色
     *
     * @param roleKey 角色标识
     * @return true表示拥有该角色
     */
    public static boolean hasRole(String roleKey) {
        Set<String> roleKeys = getRoleKeys();
        return roleKeys.contains(roleKey);
    }

    /**
     * 判断当前用户是否拥有任意一个指定角色
     *
     * @param roleKeys 角色标识列表
     * @return true表示拥有其中任意一个角色
     */
    public static boolean hasAnyRole(String... roleKeys) {
        Set<String> userRoleKeys = getRoleKeys();
        for (String roleKey : roleKeys) {
            if (userRoleKeys.contains(roleKey)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断用户角色集合与可见角色集合是否有交集
     *
     * @param userRoles    用户角色集合
     * @param visibleRoles 可见角色集合
     * @return true表示有交集
     */
    public static boolean hasAnyRole(Set<String> userRoles, Set<String> visibleRoles) {
        if (userRoles == null || userRoles.isEmpty()) {
            return false;
        }
        // 判断两个集合是否有交集
        return userRoles.stream().anyMatch(visibleRoles::contains);
    }

    /**
     * 检查当前用户是否已登录
     *
     * @return true表示已登录
     */
    public static boolean isLogin() {
        try {
            StpUtil.checkLogin();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
