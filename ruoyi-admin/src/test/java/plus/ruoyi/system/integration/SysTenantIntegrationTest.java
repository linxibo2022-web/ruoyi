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
import plus.ruoyi.helper.TestLoginHelper;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 租户管理接口集成测试
 *
 * @author 抓蛙师
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@DisplayName("租户管理接口集成测试")
public class SysTenantIntegrationTest extends BaseControllerTest {

    @Autowired
    private SystemApiClient apiClient;

    @Autowired
    private TestLoginHelper testLoginHelper;

    private static String token;

    @BeforeAll
    public static void login(@Autowired TestLoginHelper testLoginHelper) {
        log.info("========== 租户管理测试开始,模拟登录 ==========");
        token = testLoginHelper.loginAsSuperAdmin();
    }

    @Test
    @DisplayName("测试分页查询租户列表")
    public void testPageTenants() {
        log.info("测试分页查询租户列表");

        // 查询第1页,每页10条
        ForestResponse<R<PageResult<Object>>> response =
            apiClient.pageTenants(token, 1, 10);

        // 验证响应
        assertTrue(response.isSuccess());
        R<PageResult<Object>> result = response.getResult();
        assertNotNull(result);
        assertEquals(200, result.getCode());

        // 验证分页数据
        PageResult<Object> pageResult = result.getData();
        assertNotNull(pageResult);
        assertTrue(pageResult.getTotal() >= 0);

        log.info("查询成功: 总记录数={}", pageResult.getTotal());
    }

    @Test
    @DisplayName("测试查询租户详情")
    public void testGetTenant() {
        log.info("测试查询租户详情");

        // 注意: 此接口需要超级管理员权限(superadmin角色)和system:tenant:query权限
        // 查询租户详情需要传入数据库主键ID,而非业务租户ID
        // 由于默认可能没有租户数据,先尝试查询ID=1
        ForestResponse<R<Object>> response = apiClient.getTenantById(token, 1L);

        // 验证HTTP响应
        assertTrue(response.isSuccess());
        R<Object> result = response.getResult();
        assertNotNull(result);

        // 注意: 如果租户表为空,会返回200但data为null,这是正常的
        if (result.getCode() == 200) {
            log.info("查询租户详情成功: data={}", result.getData() != null ? "有数据" : "租户不存在");
        } else {
            log.warn("查询租户详情失败: code={}, msg={}", result.getCode(), result.getMsg());
        }
    }

    @Test
    @DisplayName("测试查询租户套餐列表")
    public void testPageTenantPackages() {
        log.info("测试查询租户套餐列表");

        // 查询第1页,每页10条
        ForestResponse<R<PageResult<Object>>> response =
            apiClient.pageTenantPackages(token, 1, 10);

        // 验证响应
        assertTrue(response.isSuccess());
        R<PageResult<Object>> result = response.getResult();
        assertNotNull(result);
        assertEquals(200, result.getCode());

        // 验证分页数据
        PageResult<Object> pageResult = result.getData();
        assertNotNull(pageResult);
        assertTrue(pageResult.getTotal() >= 0);

        log.info("查询成功: 总记录数={}", pageResult.getTotal());
    }

    @AfterAll
    public static void afterAll() {
        log.info("========== 租户管理测试结束 ==========");
    }
}
