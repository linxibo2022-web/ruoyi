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
 * 配置管理接口集成测试
 *
 * @author 抓蛙师
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@DisplayName("配置管理接口集成测试")
public class SysConfigIntegrationTest extends BaseControllerTest {

    @Autowired
    private SystemApiClient apiClient;

    @Autowired
    private TestLoginHelper testLoginHelper;

    private static String token;

    @BeforeAll
    public static void login(@Autowired TestLoginHelper testLoginHelper) {
        log.info("========== 配置管理测试开始,模拟登录 ==========");
        token = testLoginHelper.loginAsSuperAdmin();
    }

    @Test
    @DisplayName("测试分页查询参数配置")
    public void testPageConfigs() {
        log.info("测试分页查询参数配置");

        // 查询第1页,每页10条
        ForestResponse<R<PageResult<Object>>> response =
            apiClient.pageConfigs(token, 1, 10);

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
    @DisplayName("测试查询配置详情")
    public void testGetConfigById() {
        log.info("测试查询配置详情");

        // 查询系统内置配置(ID=1)
        ForestResponse<R<Object>> response = apiClient.getConfigById(token, 1L);

        // 验证响应
        assertTrue(response.isSuccess());
        R<Object> result = response.getResult();
        assertNotNull(result);
        assertEquals(200, result.getCode());

        log.info("查询成功");
    }

    @Test
    @DisplayName("测试根据配置键查询配置值")
    public void testGetConfigByKey() {
        log.info("测试根据配置键查询配置值");

        // 查询系统名称配置
        ForestResponse<R<String>> response =
            apiClient.getConfigByKey(token, "sys.user.initPassword");

        // 验证响应
        assertTrue(response.isSuccess());
        R<String> result = response.getResult();
        assertNotNull(result);
        assertEquals(200, result.getCode());

        log.info("查询成功: 配置值={}", result.getData());
    }

    @AfterAll
    public static void afterAll() {
        log.info("========== 配置管理测试结束 ==========");
    }
}
