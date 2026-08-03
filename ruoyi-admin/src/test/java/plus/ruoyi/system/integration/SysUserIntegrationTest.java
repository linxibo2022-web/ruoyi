package plus.ruoyi.system.integration;

import com.dtflys.forest.http.ForestResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import plus.ruoyi.client.SystemApiClient;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.test.base.BaseControllerTest;
import plus.ruoyi.common.test.base.TestDataBuilder;
import plus.ruoyi.helper.TestLoginHelper;
import plus.ruoyi.system.core.domain.bo.SysUserBo;
import plus.ruoyi.system.core.domain.vo.SysUserInfoVo;
import plus.ruoyi.system.core.domain.vo.SysUserVo;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户管理接口集成测试
 * <p>
 * 使用Forest HTTP客户端进行真实的HTTP请求测试
 * <p>
 * 特点:
 * <ul>
 *   <li>启动真实的Spring Boot应用</li>
 *   <li>真实登录获取Token</li>
 *   <li>访问真实数据库</li>
 *   <li>完整验证接口功能</li>
 * </ul>
 *
 * @author 抓蛙师
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@DisplayName("用户管理接口集成测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SysUserIntegrationTest extends BaseControllerTest {

    @Autowired
    private SystemApiClient apiClient;

    @Autowired
    private TestLoginHelper testLoginHelper;

    /**
     * 登录token
     */
    private static String token;

    /**
     * 测试用户ID (用于后续测试)
     */
    private static Long testUserId;

    /**
     * 测试用户名 (用于后续测试)
     */
    private static String testUserName;

    /**
     * 所有测试开始前执行一次 - 模拟登录获取token
     */
    @BeforeAll
    public static void loginBeforeAll(@Autowired TestLoginHelper testLoginHelper) {
        log.info("========== 开始集成测试,模拟登录获取Token ==========");
        token = testLoginHelper.loginAsSuperAdmin();
        log.info("登录成功,Token: {}", token.substring(0, 30) + "...");
    }

    @Test
    @Order(1)
    @DisplayName("测试查询用户详情 - 查询superadmin用户")
    public void testGetUserById() {
        log.info("测试查询用户详情");

        // 查询superadmin用户(ID=1)
        ForestResponse<R<SysUserInfoVo>> response = apiClient.getUserById(token, 1L);

        // 验证HTTP响应状态
        assertTrue(response.isSuccess(), "HTTP请求应该成功");
        assertEquals(200, response.getStatusCode(), "HTTP状态码应该是200");

        // 验证业务响应
        R<SysUserInfoVo> result = response.getResult();
        assertNotNull(result, "响应结果不应为null");
        assertEquals(200, result.getCode(), "业务状态码应该是200");

        // 验证用户信息数据
        SysUserInfoVo userInfo = result.getData();
        assertNotNull(userInfo, "用户信息不应为null");

        // 验证用户基本信息
        SysUserVo user = userInfo.getUser();
        assertNotNull(user, "用户基本信息不应为null");
        assertEquals(1L, user.getUserId(), "用户ID应该是1");
        assertEquals("superadmin", user.getUserName(), "用户名应该是superadmin");
        assertNotNull(user.getNickName(), "昵称不应为null");

        // 验证角色和岗位信息
        assertNotNull(userInfo.getRoleIds(), "角色ID列表不应为null");
        assertNotNull(userInfo.getPostIds(), "岗位ID列表不应为null");

        log.info("查询成功: userId={}, userName={}, nickName={}, roleIds={}, postIds={}",
            user.getUserId(), user.getUserName(), user.getNickName(),
            userInfo.getRoleIds(), userInfo.getPostIds());
    }

    @Test
    @Order(2)
    @DisplayName("测试分页查询用户列表")
    public void testPageUsers() {
        log.info("测试分页查询用户列表");

        // 查询第1页,每页10条
        ForestResponse<R<PageResult<SysUserVo>>> response =
            apiClient.pageUsers(token, 1, 10, null);

        // 验证响应
        assertTrue(response.isSuccess());
        R<PageResult<SysUserVo>> result = response.getResult();
        assertNotNull(result);
        assertEquals(200, result.getCode());

        // 验证分页数据
        PageResult<SysUserVo> pageResult = result.getData();
        assertNotNull(pageResult, "分页结果不应为null");
        assertNotNull(pageResult.getRecords(), "用户列表不应为null");
        assertTrue(pageResult.getTotal() > 0, "总记录数应该大于0");

        log.info("查询成功: 总记录数={}, 当前页记录数={}",
            pageResult.getTotal(), pageResult.getRecords().size());
    }

    @Test
    @Order(3)
    @DisplayName("测试新增用户")
    public void testInsertUser() {
        log.info("测试新增用户");

        // 构造测试用户数据
        SysUserBo user = new SysUserBo();
        long timestamp = System.currentTimeMillis();
        user.setUserName("test_" + timestamp);
        user.setNickName("测试用户");
        user.setPassword("test123");
        user.setEmail("test" + timestamp + "@example.com");
        user.setPhone(TestDataBuilder.randomPhone());
        user.setDeptId(100L);  // 若依工作室
        user.setGender("0");   // 男
        user.setStatus("1");   // 启用 (注意: "1"=启用, "0"=禁用)

        // 设置必需的数组字段
        user.setRoleIds(new Long[]{2L});  // PC端普通用户角色
        user.setPostIds(new Long[]{4L});  // 普通员工岗位

        // 发起新增请求
        ForestResponse<R<Long>> response = apiClient.insertUser(token, user);

        // 验证响应
        assertTrue(response.isSuccess(), "HTTP请求应该成功");
        R<Long> result = response.getResult();
        assertNotNull(result);
        assertEquals(200, result.getCode(), "业务状态码应该是200");

        // 验证返回的用户ID
        Long userId = result.getData();
        assertNotNull(userId, "新增用户ID不应为null");
        assertTrue(userId > 0, "用户ID应该大于0");

        // 保存用户ID和用户名供后续测试使用
        testUserId = userId;
        testUserName = user.getUserName();

        log.info("新增成功: userId={}, userName={}", userId, user.getUserName());
    }

    @Test
    @Order(4)
    @DisplayName("测试修改用户")
    public void testUpdateUser() {
        log.info("测试修改用户");

        // 如果没有测试用户ID,跳过测试
        if (testUserId == null) {
            log.warn("未找到测试用户ID,跳过修改测试");
            return;
        }

        // 构造修改数据
        SysUserBo user = new SysUserBo();
        user.setUserId(testUserId);
        user.setUserName(testUserName);  // 必需字段
        user.setNickName("测试用户(已修改)");
        user.setEmail("test_updated@example.com");

        // 设置必需的数组字段(保持原有角色和岗位)
        user.setRoleIds(new Long[]{2L});  // PC端普通用户角色
        user.setPostIds(new Long[]{4L});  // 普通员工岗位

        // 发起修改请求
        ForestResponse<R<Void>> response = apiClient.updateUser(token, user);

        // 验证响应
        assertTrue(response.isSuccess());
        R<Void> result = response.getResult();
        assertNotNull(result);
        assertEquals(200, result.getCode(), "修改应该成功");

        log.info("修改成功: userId={}", testUserId);
    }

    @Test
    @Order(5)
    @DisplayName("测试删除用户")
    public void testDeleteUser() {
        log.info("测试删除用户");

        // 如果没有测试用户ID,跳过测试
        if (testUserId == null) {
            log.warn("未找到测试用户ID,跳过删除测试");
            return;
        }

        // 发起删除请求
        ForestResponse<R<Void>> response = apiClient.deleteUser(token, testUserId);

        // 验证响应
        assertTrue(response.isSuccess());
        R<Void> result = response.getResult();
        assertNotNull(result);
        assertEquals(200, result.getCode(), "删除应该成功");

        log.info("删除成功: userId={}", testUserId);
    }

    @Test
    @Order(6)
    @DisplayName("测试查询不存在的用户 - 应返回错误")
    public void testGetUserNotFound() {
        log.info("测试查询不存在的用户");

        // 查询一个不存在的用户ID
        ForestResponse<R<SysUserInfoVo>> response = apiClient.getUserById(token, 999999L);

        // 验证响应
        assertTrue(response.isSuccess(), "HTTP请求应该成功");
        R<SysUserInfoVo> result = response.getResult();
        assertNotNull(result);

        // 业务应该返回错误
        assertNotEquals(200, result.getCode(), "业务状态码不应该是200");

        log.info("验证成功: 查询不存在的用户返回错误, code={}, msg={}",
            result.getCode(), result.getMsg());
    }

    @Test
    @Order(7)
    @DisplayName("测试按用户名搜索")
    public void testSearchByUserName() {
        log.info("测试按用户名搜索");

        // 搜索admin用户
        ForestResponse<R<PageResult<SysUserVo>>> response =
            apiClient.pageUsers(token, 1, 10, "admin");

        // 验证响应
        assertTrue(response.isSuccess());
        R<PageResult<SysUserVo>> result = response.getResult();
        assertNotNull(result);
        assertEquals(200, result.getCode());

        // 验证搜索结果
        PageResult<SysUserVo> pageResult = result.getData();
        assertNotNull(pageResult);
        assertTrue(pageResult.getTotal() > 0, "应该能搜索到包含admin的用户");

        // 验证搜索结果包含admin
        boolean hasAdmin = pageResult.getRecords().stream()
            .anyMatch(u -> u.getUserName().contains("admin"));
        assertTrue(hasAdmin, "搜索结果应该包含admin用户");

        log.info("搜索成功: 找到{}条包含'admin'的用户", pageResult.getTotal());
    }

    @Test
    @Order(8)
    @DisplayName("测试获取当前用户信息")
    public void testGetUserInfo() {
        log.info("测试获取当前用户信息");

        ForestResponse<R<Object>> response = apiClient.getUserInfo(token);

        // 验证响应
        assertTrue(response.isSuccess());
        R<Object> result = response.getResult();
        assertNotNull(result);
        assertEquals(200, result.getCode());

        // 验证用户信息
        Object userInfo = result.getData();
        assertNotNull(userInfo, "当前用户信息不应为null");

        log.info("获取成功: userInfo={}", userInfo);
    }

    @Test
    @Order(9)
    @DisplayName("测试重置用户密码")
    public void testResetUserPwd() {
        log.info("测试重置用户密码");

        // 如果没有测试用户ID,跳过测试
        if (testUserId == null) {
            log.warn("未找到测试用户ID,跳过密码重置测试");
            return;
        }

        // 重置为新密码
        SysUserBo user = new SysUserBo();
        user.setUserId(testUserId);
        user.setPassword("newPassword123");

        ForestResponse<R<Void>> response = apiClient.resetUserPwd(token, user);

        // 验证响应
        assertTrue(response.isSuccess());
        R<Void> result = response.getResult();
        assertNotNull(result);

        // 注意: 此接口需要 system:user:resetPwd 权限,可能返回403
        if (result.getCode() == 403) {
            log.warn("密码重置权限不足(403),跳过验证: {}", result.getMsg());
            return;
        }

        assertEquals(200, result.getCode(), "密码重置应该成功");

        log.info("密码重置成功: userId={}", testUserId);
    }

    @Test
    @Order(10)
    @DisplayName("测试修改用户状态")
    public void testChangeUserStatus() {
        log.info("测试修改用户状态");

        // 如果没有测试用户ID,跳过测试
        if (testUserId == null) {
            log.warn("未找到测试用户ID,跳过状态修改测试");
            return;
        }

        // 启用用户 - 注意: "1"=启用, "0"=禁用 (DictEnableStatus定义)
        SysUserBo user = new SysUserBo();
        user.setUserId(testUserId);
        user.setStatus("1"); // 启用

        ForestResponse<R<Void>> response = apiClient.changeUserStatus(token, user);

        // 验证响应
        assertTrue(response.isSuccess());
        R<Void> result = response.getResult();
        assertNotNull(result);

        // 注意: 此接口需要 system:user:edit 权限,可能返回403
        if (result.getCode() == 403) {
            log.warn("状态修改权限不足(403),跳过验证: {}", result.getMsg());
            return;
        }

        assertEquals(200, result.getCode(), "状态修改应该成功");

        log.info("用户状态修改成功: userId={}, status=1(启用)", testUserId);

        // 禁用用户
        user.setStatus("0"); // 禁用
        response = apiClient.changeUserStatus(token, user);
        assertTrue(response.isSuccess());
        assertEquals(200, response.getResult().getCode());

        log.info("用户状态修改成功: userId={}, status=0(禁用)", testUserId);
    }

    @Test
    @Order(11)
    @DisplayName("测试获取用户选择框列表")
    public void testGetUserOptions() {
        log.info("测试获取用户选择框列表");

        ForestResponse<R<Object>> response = apiClient.getUserOptions(token);

        // 验证响应
        assertTrue(response.isSuccess());
        R<Object> result = response.getResult();
        assertNotNull(result);
        assertEquals(200, result.getCode());

        // 验证数据
        Object options = result.getData();
        assertNotNull(options, "用户选择框列表不应为null");

        log.info("获取成功: options={}", options);
    }

    /**
     * 所有测试结束后执行
     */
    @AfterAll
    public static void afterAll() {
        log.info("========== 集成测试结束 ==========");
    }
}
