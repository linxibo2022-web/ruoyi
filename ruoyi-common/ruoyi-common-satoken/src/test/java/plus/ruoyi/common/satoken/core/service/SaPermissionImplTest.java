package plus.ruoyi.common.satoken.core.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import plus.ruoyi.common.core.domain.model.LoginUser;
import plus.ruoyi.common.core.enums.UserType;
import plus.ruoyi.common.satoken.utils.LoginHelper;
import plus.ruoyi.common.test.base.BaseUnitTest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SaPermissionImpl 权限服务实现测试
 * <p>
 * 测试 Sa-Token 权限管理实现类的核心功能:
 * <ul>
 *   <li>获取当前用户菜单权限列表</li>
 *   <li>获取当前用户角色权限列表</li>
 * </ul>
 * <p>
 * 注意: 跨用户查询功能依赖 SpringUtils.getBean() 静态方法，
 * 由于 SpringUtils 继承自 Hutool 的 SpringUtil (final class)，
 * 无法使用 Mockito 进行静态 mock，因此跨用户查询测试需要集成测试环境。
 *
 * @author 抓蛙师
 */
@DisplayName("SaPermissionImpl 权限服务实现测试")
class SaPermissionImplTest extends BaseUnitTest {

    private SaPermissionImpl saPermissionImpl;

    @Override
    protected void setUp() {
        saPermissionImpl = new SaPermissionImpl();
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建测试用的 LoginUser 对象
     * @param userId 用户ID
     * @param userType 用户类型
     * @param menuPermissions 菜单权限
     * @param rolePermissions 角色权限
     */
    private LoginUser createTestLoginUser(Long userId, UserType userType, Set<String> menuPermissions, Set<String> rolePermissions) {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(userId);
        loginUser.setUserType(userType.getUserType());
        loginUser.setUserName("testUser");
        loginUser.setMenuPermission(menuPermissions);
        loginUser.setRolePermission(rolePermissions);
        return loginUser;
    }

    // ==================== 获取权限列表测试 ====================

    @Nested
    @DisplayName("获取菜单权限列表测试")
    class GetPermissionListTests {

        @Test
        @DisplayName("当前用户 - 应返回用户的菜单权限")
        void testGetPermissionListForCurrentUser() {
            Set<String> menuPermissions = new HashSet<>();
            menuPermissions.add("system:user:query");
            menuPermissions.add("system:user:add");
            menuPermissions.add("system:role:query");

            Set<String> rolePermissions = new HashSet<>();
            rolePermissions.add("admin");

            // 创建真实的 LoginUser 对象 (userId=100, userType=PC_USER)
            LoginUser testUser = createTestLoginUser(100L, UserType.PC_USER, menuPermissions, rolePermissions);

            try (MockedStatic<LoginHelper> loginHelperMock = Mockito.mockStatic(LoginHelper.class)) {
                loginHelperMock.when(LoginHelper::getLoginUser).thenReturn(testUser);

                // 使用与 LoginUser.getLoginId() 相同的格式
                String loginId = testUser.getLoginId(); // pc_user:100

                List<String> permissions = saPermissionImpl.getPermissionList(loginId, "pc_user");

                assertNotNull(permissions, "权限列表不应为null");
                assertEquals(3, permissions.size(), "应有3个菜单权限");
                assertTrue(permissions.contains("system:user:query"), "应包含 system:user:query");
                assertTrue(permissions.contains("system:user:add"), "应包含 system:user:add");
                assertTrue(permissions.contains("system:role:query"), "应包含 system:role:query");
            }
        }

        @Test
        @DisplayName("当前用户 - 无权限时应返回空列表")
        void testGetPermissionListForCurrentUserEmpty() {
            Set<String> emptyPermissions = new HashSet<>();
            Set<String> rolePermissions = new HashSet<>();
            rolePermissions.add("guest");

            LoginUser testUser = createTestLoginUser(100L, UserType.PC_USER, emptyPermissions, rolePermissions);

            try (MockedStatic<LoginHelper> loginHelperMock = Mockito.mockStatic(LoginHelper.class)) {
                loginHelperMock.when(LoginHelper::getLoginUser).thenReturn(testUser);

                List<String> permissions = saPermissionImpl.getPermissionList(testUser.getLoginId(), "pc_user");

                assertNotNull(permissions, "权限列表不应为null");
                assertTrue(permissions.isEmpty(), "无权限时应返回空列表");
            }
        }

        @Test
        @DisplayName("当前用户 - 多种权限类型混合")
        void testGetPermissionListMixedPermissions() {
            Set<String> menuPermissions = new HashSet<>();
            menuPermissions.add("system:user:query");
            menuPermissions.add("system:user:add");
            menuPermissions.add("system:user:update");
            menuPermissions.add("system:user:delete");
            menuPermissions.add("system:role:query");
            menuPermissions.add("monitor:log:query");

            Set<String> rolePermissions = new HashSet<>();
            rolePermissions.add("admin");
            rolePermissions.add("user");

            LoginUser testUser = createTestLoginUser(1L, UserType.PC_USER, menuPermissions, rolePermissions);

            try (MockedStatic<LoginHelper> loginHelperMock = Mockito.mockStatic(LoginHelper.class)) {
                loginHelperMock.when(LoginHelper::getLoginUser).thenReturn(testUser);

                List<String> permissions = saPermissionImpl.getPermissionList(testUser.getLoginId(), "pc_user");

                assertNotNull(permissions, "权限列表不应为null");
                assertEquals(6, permissions.size(), "应有6个菜单权限");
                assertTrue(permissions.contains("system:user:query"), "应包含 system:user:query");
                assertTrue(permissions.contains("monitor:log:query"), "应包含 monitor:log:query");
            }
        }

        @Test
        @DisplayName("APP 用户 - 应返回用户的菜单权限")
        void testGetPermissionListForAppUser() {
            Set<String> menuPermissions = new HashSet<>();
            menuPermissions.add("app:profile:view");
            menuPermissions.add("app:order:query");

            Set<String> rolePermissions = new HashSet<>();
            rolePermissions.add("app_user");

            LoginUser testUser = createTestLoginUser(200L, UserType.APP_USER, menuPermissions, rolePermissions);

            try (MockedStatic<LoginHelper> loginHelperMock = Mockito.mockStatic(LoginHelper.class)) {
                loginHelperMock.when(LoginHelper::getLoginUser).thenReturn(testUser);

                List<String> permissions = saPermissionImpl.getPermissionList(testUser.getLoginId(), "app_user");

                assertNotNull(permissions, "权限列表不应为null");
                assertEquals(2, permissions.size(), "应有2个菜单权限");
                assertTrue(permissions.contains("app:profile:view"), "应包含 app:profile:view");
                assertTrue(permissions.contains("app:order:query"), "应包含 app:order:query");
            }
        }
    }

    // ==================== 获取角色列表测试 ====================

    @Nested
    @DisplayName("获取角色权限列表测试")
    class GetRoleListTests {

        @Test
        @DisplayName("当前用户 - 应返回用户的角色权限")
        void testGetRoleListForCurrentUser() {
            Set<String> menuPermissions = new HashSet<>();
            menuPermissions.add("system:user:query");

            Set<String> rolePermissions = new HashSet<>();
            rolePermissions.add("admin");
            rolePermissions.add("user");
            rolePermissions.add("editor");

            LoginUser testUser = createTestLoginUser(100L, UserType.PC_USER, menuPermissions, rolePermissions);

            try (MockedStatic<LoginHelper> loginHelperMock = Mockito.mockStatic(LoginHelper.class)) {
                loginHelperMock.when(LoginHelper::getLoginUser).thenReturn(testUser);

                List<String> roles = saPermissionImpl.getRoleList(testUser.getLoginId(), "pc_user");

                assertNotNull(roles, "角色列表不应为null");
                assertEquals(3, roles.size(), "应有3个角色");
                assertTrue(roles.contains("admin"), "应包含 admin");
                assertTrue(roles.contains("user"), "应包含 user");
                assertTrue(roles.contains("editor"), "应包含 editor");
            }
        }

        @Test
        @DisplayName("当前用户 - 无角色时应返回空列表")
        void testGetRoleListForCurrentUserEmpty() {
            Set<String> menuPermissions = new HashSet<>();
            Set<String> emptyRoles = new HashSet<>();

            LoginUser testUser = createTestLoginUser(100L, UserType.PC_USER, menuPermissions, emptyRoles);

            try (MockedStatic<LoginHelper> loginHelperMock = Mockito.mockStatic(LoginHelper.class)) {
                loginHelperMock.when(LoginHelper::getLoginUser).thenReturn(testUser);

                List<String> roles = saPermissionImpl.getRoleList(testUser.getLoginId(), "pc_user");

                assertNotNull(roles, "角色列表不应为null");
                assertTrue(roles.isEmpty(), "无角色时应返回空列表");
            }
        }

        @Test
        @DisplayName("当前用户 - 单一角色")
        void testGetRoleListSingleRole() {
            Set<String> menuPermissions = new HashSet<>();

            Set<String> rolePermissions = new HashSet<>();
            rolePermissions.add("guest");

            LoginUser testUser = createTestLoginUser(100L, UserType.PC_USER, menuPermissions, rolePermissions);

            try (MockedStatic<LoginHelper> loginHelperMock = Mockito.mockStatic(LoginHelper.class)) {
                loginHelperMock.when(LoginHelper::getLoginUser).thenReturn(testUser);

                List<String> roles = saPermissionImpl.getRoleList(testUser.getLoginId(), "pc_user");

                assertNotNull(roles, "角色列表不应为null");
                assertEquals(1, roles.size(), "应有1个角色");
                assertTrue(roles.contains("guest"), "应包含 guest");
            }
        }

        @Test
        @DisplayName("APP 用户 - 应返回用户的角色权限")
        void testGetRoleListForAppUser() {
            Set<String> menuPermissions = new HashSet<>();

            Set<String> rolePermissions = new HashSet<>();
            rolePermissions.add("app_vip");
            rolePermissions.add("app_member");

            LoginUser testUser = createTestLoginUser(300L, UserType.APP_USER, menuPermissions, rolePermissions);

            try (MockedStatic<LoginHelper> loginHelperMock = Mockito.mockStatic(LoginHelper.class)) {
                loginHelperMock.when(LoginHelper::getLoginUser).thenReturn(testUser);

                List<String> roles = saPermissionImpl.getRoleList(testUser.getLoginId(), "app_user");

                assertNotNull(roles, "角色列表不应为null");
                assertEquals(2, roles.size(), "应有2个角色");
                assertTrue(roles.contains("app_vip"), "应包含 app_vip");
                assertTrue(roles.contains("app_member"), "应包含 app_member");
            }
        }

        @Test
        @DisplayName("OpenAPI 用户 - 应返回用户的角色权限")
        void testGetRoleListForOpenApiUser() {
            Set<String> menuPermissions = new HashSet<>();

            Set<String> rolePermissions = new HashSet<>();
            rolePermissions.add("api_read");
            rolePermissions.add("api_write");

            LoginUser testUser = createTestLoginUser(500L, UserType.OPENAPI_USER, menuPermissions, rolePermissions);

            try (MockedStatic<LoginHelper> loginHelperMock = Mockito.mockStatic(LoginHelper.class)) {
                loginHelperMock.when(LoginHelper::getLoginUser).thenReturn(testUser);

                List<String> roles = saPermissionImpl.getRoleList(testUser.getLoginId(), "openapi_user");

                assertNotNull(roles, "角色列表不应为null");
                assertEquals(2, roles.size(), "应有2个角色");
                assertTrue(roles.contains("api_read"), "应包含 api_read");
                assertTrue(roles.contains("api_write"), "应包含 api_write");
            }
        }
    }

    // ==================== StpInterface 接口实现验证 ====================

    @Nested
    @DisplayName("StpInterface 接口实现验证")
    class StpInterfaceTests {

        @Test
        @DisplayName("SaPermissionImpl 应实现 StpInterface 接口")
        void testImplementsStpInterface() {
            assertTrue(saPermissionImpl instanceof cn.dev33.satoken.stp.StpInterface,
                "SaPermissionImpl 应实现 StpInterface 接口");
        }

        @Test
        @DisplayName("getPermissionList 返回类型为 List<String>")
        void testGetPermissionListReturnType() {
            Set<String> menuPermissions = new HashSet<>();
            menuPermissions.add("test:permission");

            Set<String> rolePermissions = new HashSet<>();

            LoginUser testUser = createTestLoginUser(100L, UserType.PC_USER, menuPermissions, rolePermissions);

            try (MockedStatic<LoginHelper> loginHelperMock = Mockito.mockStatic(LoginHelper.class)) {
                loginHelperMock.when(LoginHelper::getLoginUser).thenReturn(testUser);

                Object result = saPermissionImpl.getPermissionList(testUser.getLoginId(), "pc_user");

                assertTrue(result instanceof List, "返回值应为 List 类型");
            }
        }

        @Test
        @DisplayName("getRoleList 返回类型为 List<String>")
        void testGetRoleListReturnType() {
            Set<String> menuPermissions = new HashSet<>();

            Set<String> rolePermissions = new HashSet<>();
            rolePermissions.add("test:role");

            LoginUser testUser = createTestLoginUser(100L, UserType.PC_USER, menuPermissions, rolePermissions);

            try (MockedStatic<LoginHelper> loginHelperMock = Mockito.mockStatic(LoginHelper.class)) {
                loginHelperMock.when(LoginHelper::getLoginUser).thenReturn(testUser);

                Object result = saPermissionImpl.getRoleList(testUser.getLoginId(), "pc_user");

                assertTrue(result instanceof List, "返回值应为 List 类型");
            }
        }
    }
}
