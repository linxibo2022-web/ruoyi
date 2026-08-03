package plus.ruoyi.common.satoken.utils;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import plus.ruoyi.common.core.constant.SystemConstants;
import plus.ruoyi.common.core.constant.TenantConstants;
import plus.ruoyi.common.core.domain.model.LoginUser;
import plus.ruoyi.common.core.enums.UserType;
import plus.ruoyi.common.test.base.BaseUnitTest;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * LoginHelper 登录助手工具类测试
 * <p>
 * 测试登录认证和权限管理工具的核心功能:
 * <ul>
 *   <li>常量定义测试</li>
 *   <li>超级管理员判断</li>
 *   <li>租户管理员判断</li>
 *   <li>角色权限判断</li>
 *   <li>用户信息获取</li>
 * </ul>
 *
 * @author 抓蛙师
 */
@DisplayName("LoginHelper 登录助手工具类测试")
class LoginHelperTest extends BaseUnitTest {

    // ==================== 常量测试 ====================

    @Nested
    @DisplayName("常量定义测试")
    class ConstantTests {

        @Test
        @DisplayName("LOGIN_USER 常量值")
        void testLoginUserConstant() {
            assertEquals("loginUser", LoginHelper.LOGIN_USER, "LOGIN_USER 常量应为 loginUser");
        }

        @Test
        @DisplayName("TENANT_ID 常量值")
        void testTenantIdConstant() {
            assertEquals("tenantId", LoginHelper.TENANT_ID, "TENANT_ID 常量应为 tenantId");
        }

        @Test
        @DisplayName("USER_ID 常量值")
        void testUserIdConstant() {
            assertEquals("userId", LoginHelper.USER_ID, "USER_ID 常量应为 userId");
        }

        @Test
        @DisplayName("USER_NAME 常量值")
        void testUserNameConstant() {
            assertEquals("userName", LoginHelper.USER_NAME, "USER_NAME 常量应为 userName");
        }

        @Test
        @DisplayName("DEPT_ID 常量值")
        void testDeptIdConstant() {
            assertEquals("deptId", LoginHelper.DEPT_ID, "DEPT_ID 常量应为 deptId");
        }

        @Test
        @DisplayName("DEPT_NAME 常量值")
        void testDeptNameConstant() {
            assertEquals("deptName", LoginHelper.DEPT_NAME, "DEPT_NAME 常量应为 deptName");
        }

        @Test
        @DisplayName("DEPT_CATEGORY 常量值")
        void testDeptCategoryConstant() {
            assertEquals("deptCategory", LoginHelper.DEPT_CATEGORY, "DEPT_CATEGORY 常量应为 deptCategory");
        }
    }

    // ==================== 超级管理员判断测试 ====================

    @Nested
    @DisplayName("超级管理员判断测试")
    class SuperAdminTests {

        @Test
        @DisplayName("isSuperAdmin(userId) - 超级管理员ID应返回true")
        void testIsSuperAdminWithSuperAdminId() {
            assertTrue(LoginHelper.isSuperAdmin(SystemConstants.SUPER_ADMIN_ID),
                "超级管理员ID应返回true");
        }

        @Test
        @DisplayName("isSuperAdmin(userId) - ID为1应返回true")
        void testIsSuperAdminWithIdOne() {
            assertTrue(LoginHelper.isSuperAdmin(1L), "ID为1应返回true");
        }

        @Test
        @DisplayName("isSuperAdmin(userId) - 普通用户ID应返回false")
        void testIsSuperAdminWithNormalUserId() {
            assertFalse(LoginHelper.isSuperAdmin(2L), "普通用户ID应返回false");
            assertFalse(LoginHelper.isSuperAdmin(100L), "普通用户ID应返回false");
            assertFalse(LoginHelper.isSuperAdmin(999L), "普通用户ID应返回false");
        }

        @Test
        @DisplayName("isSuperAdmin(userId) - null值应返回false")
        void testIsSuperAdminWithNull() {
            assertFalse(LoginHelper.isSuperAdmin(null), "null值应返回false");
        }

        @Test
        @DisplayName("isSuperAdmin(userId) - 0值应返回false")
        void testIsSuperAdminWithZero() {
            assertFalse(LoginHelper.isSuperAdmin(0L), "0值应返回false");
        }

        @Test
        @DisplayName("isSuperAdmin(userId) - 负数应返回false")
        void testIsSuperAdminWithNegative() {
            assertFalse(LoginHelper.isSuperAdmin(-1L), "负数应返回false");
        }
    }

    // ==================== 租户管理员判断测试 ====================

    @Nested
    @DisplayName("租户管理员判断测试")
    class TenantAdminTests {

        @Test
        @DisplayName("isTenantAdmin(rolePermission) - 包含admin角色应返回true")
        void testIsTenantAdminWithAdminRole() {
            Set<String> roles = new HashSet<>();
            roles.add(TenantConstants.TENANT_ADMIN_ROLE_KEY);
            roles.add("user");

            assertTrue(LoginHelper.isTenantAdmin(roles), "包含admin角色应返回true");
        }

        @Test
        @DisplayName("isTenantAdmin(rolePermission) - 仅admin角色应返回true")
        void testIsTenantAdminWithOnlyAdminRole() {
            Set<String> roles = new HashSet<>();
            roles.add(TenantConstants.TENANT_ADMIN_ROLE_KEY);

            assertTrue(LoginHelper.isTenantAdmin(roles), "仅admin角色应返回true");
        }

        @Test
        @DisplayName("isTenantAdmin(rolePermission) - 不包含admin角色应返回false")
        void testIsTenantAdminWithoutAdminRole() {
            Set<String> roles = new HashSet<>();
            roles.add("user");
            roles.add("editor");

            assertFalse(LoginHelper.isTenantAdmin(roles), "不包含admin角色应返回false");
        }

        @Test
        @DisplayName("isTenantAdmin(rolePermission) - 空集合应返回false")
        void testIsTenantAdminWithEmptySet() {
            Set<String> roles = new HashSet<>();

            assertFalse(LoginHelper.isTenantAdmin(roles), "空集合应返回false");
        }

        @Test
        @DisplayName("isTenantAdmin(rolePermission) - null应返回false")
        void testIsTenantAdminWithNull() {
            assertFalse(LoginHelper.isTenantAdmin(null), "null应返回false");
        }
    }

    // ==================== 角色判断测试 ====================

    @Nested
    @DisplayName("角色集合交集判断测试")
    class RoleIntersectionTests {

        @Test
        @DisplayName("hasAnyRole(userRoles, visibleRoles) - 有交集应返回true")
        void testHasAnyRoleWithIntersection() {
            Set<String> userRoles = new HashSet<>();
            userRoles.add("admin");
            userRoles.add("user");

            Set<String> visibleRoles = new HashSet<>();
            visibleRoles.add("admin");
            visibleRoles.add("manager");

            assertTrue(LoginHelper.hasAnyRole(userRoles, visibleRoles), "有交集应返回true");
        }

        @Test
        @DisplayName("hasAnyRole(userRoles, visibleRoles) - 无交集应返回false")
        void testHasAnyRoleWithoutIntersection() {
            Set<String> userRoles = new HashSet<>();
            userRoles.add("user");
            userRoles.add("guest");

            Set<String> visibleRoles = new HashSet<>();
            visibleRoles.add("admin");
            visibleRoles.add("manager");

            assertFalse(LoginHelper.hasAnyRole(userRoles, visibleRoles), "无交集应返回false");
        }

        @Test
        @DisplayName("hasAnyRole(userRoles, visibleRoles) - 用户角色为空应返回false")
        void testHasAnyRoleWithEmptyUserRoles() {
            Set<String> userRoles = new HashSet<>();

            Set<String> visibleRoles = new HashSet<>();
            visibleRoles.add("admin");

            assertFalse(LoginHelper.hasAnyRole(userRoles, visibleRoles), "用户角色为空应返回false");
        }

        @Test
        @DisplayName("hasAnyRole(userRoles, visibleRoles) - 用户角色为null应返回false")
        void testHasAnyRoleWithNullUserRoles() {
            Set<String> visibleRoles = new HashSet<>();
            visibleRoles.add("admin");

            assertFalse(LoginHelper.hasAnyRole(null, visibleRoles), "用户角色为null应返回false");
        }

        @Test
        @DisplayName("hasAnyRole(userRoles, visibleRoles) - 完全相同应返回true")
        void testHasAnyRoleWithSameRoles() {
            Set<String> userRoles = new HashSet<>();
            userRoles.add("admin");

            Set<String> visibleRoles = new HashSet<>();
            visibleRoles.add("admin");

            assertTrue(LoginHelper.hasAnyRole(userRoles, visibleRoles), "完全相同应返回true");
        }
    }

    // ==================== LoginUser 构建辅助方法 ====================

    /**
     * 创建测试用的 LoginUser 对象
     */
    private LoginUser createMockLoginUser(Long userId, String tenantId, String userName) {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(userId);
        loginUser.setTenantId(tenantId);
        loginUser.setUserName(userName);
        loginUser.setUserType(UserType.PC_USER.getUserType());
        loginUser.setDeptId(100L);
        loginUser.setDeptName("测试部门");
        loginUser.setDeptCategory("tech");
        loginUser.setAppid("wx123456");
        loginUser.setUnionid("unionid123");
        loginUser.setOpenid("openid123");
        return loginUser;
    }

    /**
     * 创建带角色权限的 LoginUser 对象
     */
    private LoginUser createMockLoginUserWithRoles(Long userId, Set<String> rolePermissions) {
        LoginUser loginUser = createMockLoginUser(userId, TenantConstants.DEFAULT_TENANT_ID, "testUser");
        loginUser.setRolePermission(rolePermissions);
        return loginUser;
    }

    // ==================== Mock 测试 (需要 Sa-Token 环境) ====================

    @Nested
    @DisplayName("用户信息获取测试 (Mock)")
    class GetLoginUserTests {

        @Test
        @DisplayName("getLoginUser - 从 Storage 获取用户")
        void testGetLoginUserFromStorage() {
            LoginUser mockUser = createMockLoginUser(1L, "000000", "admin");

            try (MockedStatic<SaHolder> saHolderMock = Mockito.mockStatic(SaHolder.class)) {
                var mockStorage = mock(cn.dev33.satoken.context.model.SaStorage.class);
                saHolderMock.when(SaHolder::getStorage).thenReturn(mockStorage);
                when(mockStorage.get(LoginHelper.LOGIN_USER)).thenReturn(mockUser);

                LoginUser result = LoginHelper.getLoginUser();

                assertNotNull(result, "应成功获取用户");
                assertEquals(1L, result.getUserId(), "用户ID应匹配");
                assertEquals("admin", result.getUserName(), "用户名应匹配");
            }
        }

        @Test
        @DisplayName("getLoginUser - Storage 为空时从 Session 获取")
        void testGetLoginUserFromSession() {
            LoginUser mockUser = createMockLoginUser(2L, "000001", "testUser");

            try (MockedStatic<SaHolder> saHolderMock = Mockito.mockStatic(SaHolder.class);
                 MockedStatic<StpUtil> stpUtilMock = Mockito.mockStatic(StpUtil.class)) {

                // Storage 返回 null
                var mockStorage = mock(cn.dev33.satoken.context.model.SaStorage.class);
                saHolderMock.when(SaHolder::getStorage).thenReturn(mockStorage);
                when(mockStorage.get(LoginHelper.LOGIN_USER)).thenReturn(null);

                // Session 返回用户
                SaSession mockSession = mock(SaSession.class);
                stpUtilMock.when(StpUtil::getTokenSession).thenReturn(mockSession);
                when(mockSession.get(LoginHelper.LOGIN_USER)).thenReturn(mockUser);

                LoginUser result = LoginHelper.getLoginUser();

                assertNotNull(result, "应从 Session 获取用户");
                assertEquals(2L, result.getUserId(), "用户ID应匹配");
            }
        }

        @Test
        @DisplayName("getLoginUser - 未登录时返回 null")
        void testGetLoginUserWhenNotLoggedIn() {
            try (MockedStatic<SaHolder> saHolderMock = Mockito.mockStatic(SaHolder.class);
                 MockedStatic<StpUtil> stpUtilMock = Mockito.mockStatic(StpUtil.class)) {

                // Storage 抛出异常
                saHolderMock.when(SaHolder::getStorage).thenThrow(new RuntimeException("Not in web context"));

                // Session 返回 null
                stpUtilMock.when(StpUtil::getTokenSession).thenReturn(null);

                LoginUser result = LoginHelper.getLoginUser();

                assertNull(result, "未登录时应返回 null");
            }
        }
    }

    @Nested
    @DisplayName("根据 Token 获取用户测试")
    class GetLoginUserByTokenTests {

        @Test
        @DisplayName("getLoginUser(token) - 有效 Token 应返回用户")
        void testGetLoginUserByValidToken() {
            LoginUser mockUser = createMockLoginUser(3L, "000002", "tokenUser");
            String testToken = "valid-test-token";

            try (MockedStatic<StpUtil> stpUtilMock = Mockito.mockStatic(StpUtil.class)) {
                SaSession mockSession = mock(SaSession.class);
                stpUtilMock.when(() -> StpUtil.getTokenSessionByToken(testToken)).thenReturn(mockSession);
                when(mockSession.get(LoginHelper.LOGIN_USER)).thenReturn(mockUser);

                LoginUser result = LoginHelper.getLoginUser(testToken);

                assertNotNull(result, "有效 Token 应返回用户");
                assertEquals(3L, result.getUserId(), "用户ID应匹配");
            }
        }

        @Test
        @DisplayName("getLoginUser(token) - 无效 Token 应返回 null")
        void testGetLoginUserByInvalidToken() {
            String invalidToken = "invalid-token";

            try (MockedStatic<StpUtil> stpUtilMock = Mockito.mockStatic(StpUtil.class)) {
                stpUtilMock.when(() -> StpUtil.getTokenSessionByToken(invalidToken)).thenReturn(null);

                LoginUser result = LoginHelper.getLoginUser(invalidToken);

                assertNull(result, "无效 Token 应返回 null");
            }
        }
    }

    @Nested
    @DisplayName("用户属性获取测试 (Mock)")
    class UserAttributeTests {

        @Test
        @DisplayName("getUserId - 获取当前用户ID")
        void testGetUserId() {
            try (MockedStatic<StpUtil> stpUtilMock = Mockito.mockStatic(StpUtil.class)) {
                stpUtilMock.when(() -> StpUtil.getExtra(LoginHelper.USER_ID)).thenReturn(100L);

                Long userId = LoginHelper.getUserId();

                assertEquals(100L, userId, "用户ID应匹配");
            }
        }

        @Test
        @DisplayName("getUserIdStr - 获取当前用户ID字符串")
        void testGetUserIdStr() {
            try (MockedStatic<StpUtil> stpUtilMock = Mockito.mockStatic(StpUtil.class)) {
                stpUtilMock.when(() -> StpUtil.getExtra(LoginHelper.USER_ID)).thenReturn(100L);

                String userIdStr = LoginHelper.getUserIdStr();

                assertEquals("100", userIdStr, "用户ID字符串应匹配");
            }
        }

        @Test
        @DisplayName("getUserName - 获取当前用户名")
        void testGetUserName() {
            try (MockedStatic<StpUtil> stpUtilMock = Mockito.mockStatic(StpUtil.class)) {
                stpUtilMock.when(() -> StpUtil.getExtra(LoginHelper.USER_NAME)).thenReturn("testUser");

                String userName = LoginHelper.getUserName();

                assertEquals("testUser", userName, "用户名应匹配");
            }
        }

        @Test
        @DisplayName("getTenantId - 获取当前租户ID")
        void testGetTenantId() {
            try (MockedStatic<StpUtil> stpUtilMock = Mockito.mockStatic(StpUtil.class)) {
                stpUtilMock.when(() -> StpUtil.getExtra(LoginHelper.TENANT_ID)).thenReturn("000001");

                String tenantId = LoginHelper.getTenantId();

                assertEquals("000001", tenantId, "租户ID应匹配");
            }
        }

        @Test
        @DisplayName("getDeptId - 获取当前部门ID")
        void testGetDeptId() {
            try (MockedStatic<StpUtil> stpUtilMock = Mockito.mockStatic(StpUtil.class)) {
                stpUtilMock.when(() -> StpUtil.getExtra(LoginHelper.DEPT_ID)).thenReturn(200L);

                Long deptId = LoginHelper.getDeptId();

                assertEquals(200L, deptId, "部门ID应匹配");
            }
        }

        @Test
        @DisplayName("getDeptName - 获取当前部门名称")
        void testGetDeptName() {
            try (MockedStatic<StpUtil> stpUtilMock = Mockito.mockStatic(StpUtil.class)) {
                stpUtilMock.when(() -> StpUtil.getExtra(LoginHelper.DEPT_NAME)).thenReturn("技术部");

                String deptName = LoginHelper.getDeptName();

                assertEquals("技术部", deptName, "部门名称应匹配");
            }
        }

        @Test
        @DisplayName("getDeptCategory - 获取当前部门类别")
        void testGetDeptCategory() {
            try (MockedStatic<StpUtil> stpUtilMock = Mockito.mockStatic(StpUtil.class)) {
                stpUtilMock.when(() -> StpUtil.getExtra(LoginHelper.DEPT_CATEGORY)).thenReturn("tech");

                String deptCategory = LoginHelper.getDeptCategory();

                assertEquals("tech", deptCategory, "部门类别应匹配");
            }
        }

        @Test
        @DisplayName("getExtra 异常时返回 null")
        void testGetExtraWithException() {
            try (MockedStatic<StpUtil> stpUtilMock = Mockito.mockStatic(StpUtil.class)) {
                stpUtilMock.when(() -> StpUtil.getExtra(anyString()))
                    .thenThrow(new RuntimeException("Not logged in"));

                assertNull(LoginHelper.getUserId(), "异常时 getUserId 应返回 null");
                assertNull(LoginHelper.getUserName(), "异常时 getUserName 应返回 null");
                assertNull(LoginHelper.getTenantId(), "异常时 getTenantId 应返回 null");
            }
        }
    }

    @Nested
    @DisplayName("微信相关信息获取测试")
    class WechatInfoTests {

        @Test
        @DisplayName("getAppid - 获取当前用户的微信应用ID")
        void testGetAppid() {
            LoginUser mockUser = createMockLoginUser(1L, "000000", "wechatUser");

            try (MockedStatic<SaHolder> saHolderMock = Mockito.mockStatic(SaHolder.class)) {
                var mockStorage = mock(cn.dev33.satoken.context.model.SaStorage.class);
                saHolderMock.when(SaHolder::getStorage).thenReturn(mockStorage);
                when(mockStorage.get(LoginHelper.LOGIN_USER)).thenReturn(mockUser);

                String appid = LoginHelper.getAppid();

                assertEquals("wx123456", appid, "Appid 应匹配");
            }
        }

        @Test
        @DisplayName("getUnionid - 获取当前用户的微信UnionID")
        void testGetUnionid() {
            LoginUser mockUser = createMockLoginUser(1L, "000000", "wechatUser");

            try (MockedStatic<SaHolder> saHolderMock = Mockito.mockStatic(SaHolder.class)) {
                var mockStorage = mock(cn.dev33.satoken.context.model.SaStorage.class);
                saHolderMock.when(SaHolder::getStorage).thenReturn(mockStorage);
                when(mockStorage.get(LoginHelper.LOGIN_USER)).thenReturn(mockUser);

                String unionid = LoginHelper.getUnionid();

                assertEquals("unionid123", unionid, "UnionId 应匹配");
            }
        }

        @Test
        @DisplayName("getOpenid - 获取当前用户的微信OpenID")
        void testGetOpenid() {
            LoginUser mockUser = createMockLoginUser(1L, "000000", "wechatUser");

            try (MockedStatic<SaHolder> saHolderMock = Mockito.mockStatic(SaHolder.class)) {
                var mockStorage = mock(cn.dev33.satoken.context.model.SaStorage.class);
                saHolderMock.when(SaHolder::getStorage).thenReturn(mockStorage);
                when(mockStorage.get(LoginHelper.LOGIN_USER)).thenReturn(mockUser);

                String openid = LoginHelper.getOpenid();

                assertEquals("openid123", openid, "OpenId 应匹配");
            }
        }

        @Test
        @DisplayName("微信信息 - 未登录时返回 null")
        void testWechatInfoWhenNotLoggedIn() {
            try (MockedStatic<SaHolder> saHolderMock = Mockito.mockStatic(SaHolder.class);
                 MockedStatic<StpUtil> stpUtilMock = Mockito.mockStatic(StpUtil.class)) {

                saHolderMock.when(SaHolder::getStorage).thenThrow(new RuntimeException("Not in web context"));
                stpUtilMock.when(StpUtil::getTokenSession).thenReturn(null);

                assertNull(LoginHelper.getAppid(), "未登录时 getAppid 应返回 null");
                assertNull(LoginHelper.getUnionid(), "未登录时 getUnionid 应返回 null");
                assertNull(LoginHelper.getOpenid(), "未登录时 getOpenid 应返回 null");
            }
        }
    }

    @Nested
    @DisplayName("用户类型获取测试")
    class UserTypeTests {

        @Test
        @DisplayName("getUserType - 获取PC用户类型")
        void testGetUserTypePc() {
            try (MockedStatic<StpUtil> stpUtilMock = Mockito.mockStatic(StpUtil.class)) {
                stpUtilMock.when(StpUtil::getLoginIdAsString).thenReturn("pc_user:1");

                UserType userType = LoginHelper.getUserType();

                assertEquals(UserType.PC_USER, userType, "应返回PC用户类型");
            }
        }

        @Test
        @DisplayName("getUserType - 获取APP用户类型")
        void testGetUserTypeApp() {
            try (MockedStatic<StpUtil> stpUtilMock = Mockito.mockStatic(StpUtil.class)) {
                stpUtilMock.when(StpUtil::getLoginIdAsString).thenReturn("app_user:100");

                UserType userType = LoginHelper.getUserType();

                assertEquals(UserType.APP_USER, userType, "应返回APP用户类型");
            }
        }

        @Test
        @DisplayName("getUserType - 获取OpenAPI用户类型")
        void testGetUserTypeOpenApi() {
            try (MockedStatic<StpUtil> stpUtilMock = Mockito.mockStatic(StpUtil.class)) {
                stpUtilMock.when(StpUtil::getLoginIdAsString).thenReturn("openapi_user:500");

                UserType userType = LoginHelper.getUserType();

                assertEquals(UserType.OPENAPI_USER, userType, "应返回OpenAPI用户类型");
            }
        }
    }

    @Nested
    @DisplayName("登录状态检查测试")
    class LoginStatusTests {

        @Test
        @DisplayName("isLogin - 已登录应返回 true")
        void testIsLoginWhenLoggedIn() {
            try (MockedStatic<StpUtil> stpUtilMock = Mockito.mockStatic(StpUtil.class)) {
                // checkLogin 不抛出异常表示已登录
                stpUtilMock.when(StpUtil::checkLogin).then(invocation -> null);

                assertTrue(LoginHelper.isLogin(), "已登录应返回 true");
            }
        }

        @Test
        @DisplayName("isLogin - 未登录应返回 false")
        void testIsLoginWhenNotLoggedIn() {
            try (MockedStatic<StpUtil> stpUtilMock = Mockito.mockStatic(StpUtil.class)) {
                // checkLogin 抛出异常表示未登录
                stpUtilMock.when(StpUtil::checkLogin).thenThrow(new RuntimeException("Not logged in"));

                assertFalse(LoginHelper.isLogin(), "未登录应返回 false");
            }
        }
    }

    @Nested
    @DisplayName("角色权限判断测试 (Mock)")
    class RolePermissionTests {

        @Test
        @DisplayName("getRoleKeys - 已登录返回角色列表")
        void testGetRoleKeysWhenLoggedIn() {
            Set<String> roles = new HashSet<>();
            roles.add("admin");
            roles.add("user");
            LoginUser mockUser = createMockLoginUserWithRoles(1L, roles);

            try (MockedStatic<SaHolder> saHolderMock = Mockito.mockStatic(SaHolder.class)) {
                var mockStorage = mock(cn.dev33.satoken.context.model.SaStorage.class);
                saHolderMock.when(SaHolder::getStorage).thenReturn(mockStorage);
                when(mockStorage.get(LoginHelper.LOGIN_USER)).thenReturn(mockUser);

                Set<String> result = LoginHelper.getRoleKeys();

                assertNotNull(result, "应返回角色列表");
                assertEquals(2, result.size(), "应有2个角色");
                assertTrue(result.contains("admin"), "应包含admin角色");
                assertTrue(result.contains("user"), "应包含user角色");
            }
        }

        @Test
        @DisplayName("getRoleKeys - 未登录返回空集合")
        void testGetRoleKeysWhenNotLoggedIn() {
            try (MockedStatic<SaHolder> saHolderMock = Mockito.mockStatic(SaHolder.class);
                 MockedStatic<StpUtil> stpUtilMock = Mockito.mockStatic(StpUtil.class)) {

                saHolderMock.when(SaHolder::getStorage).thenThrow(new RuntimeException("Not in web context"));
                stpUtilMock.when(StpUtil::getTokenSession).thenReturn(null);

                Set<String> result = LoginHelper.getRoleKeys();

                assertNotNull(result, "应返回空集合而非null");
                assertTrue(result.isEmpty(), "未登录应返回空集合");
            }
        }

        @Test
        @DisplayName("hasRole - 有指定角色应返回 true")
        void testHasRoleWithMatchingRole() {
            Set<String> roles = new HashSet<>();
            roles.add("admin");
            roles.add("user");
            LoginUser mockUser = createMockLoginUserWithRoles(1L, roles);

            try (MockedStatic<SaHolder> saHolderMock = Mockito.mockStatic(SaHolder.class)) {
                var mockStorage = mock(cn.dev33.satoken.context.model.SaStorage.class);
                saHolderMock.when(SaHolder::getStorage).thenReturn(mockStorage);
                when(mockStorage.get(LoginHelper.LOGIN_USER)).thenReturn(mockUser);

                assertTrue(LoginHelper.hasRole("admin"), "有admin角色应返回true");
                assertTrue(LoginHelper.hasRole("user"), "有user角色应返回true");
            }
        }

        @Test
        @DisplayName("hasRole - 无指定角色应返回 false")
        void testHasRoleWithoutMatchingRole() {
            Set<String> roles = new HashSet<>();
            roles.add("user");
            LoginUser mockUser = createMockLoginUserWithRoles(1L, roles);

            try (MockedStatic<SaHolder> saHolderMock = Mockito.mockStatic(SaHolder.class)) {
                var mockStorage = mock(cn.dev33.satoken.context.model.SaStorage.class);
                saHolderMock.when(SaHolder::getStorage).thenReturn(mockStorage);
                when(mockStorage.get(LoginHelper.LOGIN_USER)).thenReturn(mockUser);

                assertFalse(LoginHelper.hasRole("admin"), "无admin角色应返回false");
            }
        }

        @Test
        @DisplayName("hasAnyRole(roleKeys...) - 有任意角色应返回 true")
        void testHasAnyRoleVarargs() {
            Set<String> roles = new HashSet<>();
            roles.add("editor");
            LoginUser mockUser = createMockLoginUserWithRoles(1L, roles);

            try (MockedStatic<SaHolder> saHolderMock = Mockito.mockStatic(SaHolder.class)) {
                var mockStorage = mock(cn.dev33.satoken.context.model.SaStorage.class);
                saHolderMock.when(SaHolder::getStorage).thenReturn(mockStorage);
                when(mockStorage.get(LoginHelper.LOGIN_USER)).thenReturn(mockUser);

                assertTrue(LoginHelper.hasAnyRole("admin", "editor", "user"),
                    "有editor角色应返回true");
            }
        }

        @Test
        @DisplayName("hasAnyRole(roleKeys...) - 无任何角色应返回 false")
        void testHasAnyRoleVarargsNoMatch() {
            Set<String> roles = new HashSet<>();
            roles.add("guest");
            LoginUser mockUser = createMockLoginUserWithRoles(1L, roles);

            try (MockedStatic<SaHolder> saHolderMock = Mockito.mockStatic(SaHolder.class)) {
                var mockStorage = mock(cn.dev33.satoken.context.model.SaStorage.class);
                saHolderMock.when(SaHolder::getStorage).thenReturn(mockStorage);
                when(mockStorage.get(LoginHelper.LOGIN_USER)).thenReturn(mockUser);

                assertFalse(LoginHelper.hasAnyRole("admin", "editor", "user"),
                    "无指定角色应返回false");
            }
        }
    }

    @Nested
    @DisplayName("当前用户超级管理员判断测试 (Mock)")
    class CurrentUserSuperAdminTests {

        @Test
        @DisplayName("isSuperAdmin() - 当前用户是超级管理员")
        void testIsSuperAdminCurrentUser() {
            try (MockedStatic<StpUtil> stpUtilMock = Mockito.mockStatic(StpUtil.class)) {
                stpUtilMock.when(() -> StpUtil.getExtra(LoginHelper.USER_ID)).thenReturn(1L);

                assertTrue(LoginHelper.isSuperAdmin(), "用户ID为1应是超级管理员");
            }
        }

        @Test
        @DisplayName("isSuperAdmin() - 当前用户不是超级管理员")
        void testIsNotSuperAdminCurrentUser() {
            try (MockedStatic<StpUtil> stpUtilMock = Mockito.mockStatic(StpUtil.class)) {
                stpUtilMock.when(() -> StpUtil.getExtra(LoginHelper.USER_ID)).thenReturn(100L);

                assertFalse(LoginHelper.isSuperAdmin(), "普通用户应不是超级管理员");
            }
        }
    }

    @Nested
    @DisplayName("当前用户租户管理员判断测试 (Mock)")
    class CurrentUserTenantAdminTests {

        @Test
        @DisplayName("isTenantAdmin() - 当前用户是租户管理员")
        void testIsTenantAdminCurrentUser() {
            Set<String> roles = new HashSet<>();
            roles.add(TenantConstants.TENANT_ADMIN_ROLE_KEY);
            LoginUser mockUser = createMockLoginUserWithRoles(100L, roles);

            try (MockedStatic<SaHolder> saHolderMock = Mockito.mockStatic(SaHolder.class)) {
                var mockStorage = mock(cn.dev33.satoken.context.model.SaStorage.class);
                saHolderMock.when(SaHolder::getStorage).thenReturn(mockStorage);
                when(mockStorage.get(LoginHelper.LOGIN_USER)).thenReturn(mockUser);

                assertTrue(LoginHelper.isTenantAdmin(), "有admin角色应是租户管理员");
            }
        }

        @Test
        @DisplayName("isTenantAdmin() - 当前用户不是租户管理员")
        void testIsNotTenantAdminCurrentUser() {
            Set<String> roles = new HashSet<>();
            roles.add("user");
            LoginUser mockUser = createMockLoginUserWithRoles(100L, roles);

            try (MockedStatic<SaHolder> saHolderMock = Mockito.mockStatic(SaHolder.class)) {
                var mockStorage = mock(cn.dev33.satoken.context.model.SaStorage.class);
                saHolderMock.when(SaHolder::getStorage).thenReturn(mockStorage);
                when(mockStorage.get(LoginHelper.LOGIN_USER)).thenReturn(mockUser);

                assertFalse(LoginHelper.isTenantAdmin(), "无admin角色应不是租户管理员");
            }
        }

        @Test
        @DisplayName("isTenantAdmin() - 未登录返回 false")
        void testIsTenantAdminWhenNotLoggedIn() {
            try (MockedStatic<SaHolder> saHolderMock = Mockito.mockStatic(SaHolder.class);
                 MockedStatic<StpUtil> stpUtilMock = Mockito.mockStatic(StpUtil.class)) {

                saHolderMock.when(SaHolder::getStorage).thenThrow(new RuntimeException("Not in web context"));
                stpUtilMock.when(StpUtil::getTokenSession).thenReturn(null);

                assertFalse(LoginHelper.isTenantAdmin(), "未登录应返回false");
            }
        }
    }
}
