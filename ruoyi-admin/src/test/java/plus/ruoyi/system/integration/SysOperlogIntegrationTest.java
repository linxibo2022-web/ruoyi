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
 * 操作日志接口集成测试
 *
 * @author 抓蛙师
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@DisplayName("操作日志接口集成测试")
public class SysOperlogIntegrationTest extends BaseControllerTest {

    @Autowired
    private SystemApiClient apiClient;

    @Autowired
    private TestLoginHelper testLoginHelper;

    private static String token;

    @BeforeAll
    public static void login(@Autowired TestLoginHelper testLoginHelper) {
        log.info("========== 操作日志测试开始,模拟登录 ==========");
        token = testLoginHelper.loginAsSuperAdmin();
    }

    @Test
    @DisplayName("测试分页查询操作日志")
    public void testPageOperlogs() {
        log.info("测试分页查询操作日志");

        // 查询第1页,每页10条
        ForestResponse<R<PageResult<Object>>> response =
            apiClient.pageOperlogs(token, 1, 10);

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
        log.info("========== 操作日志测试结束 ==========");
    }
}
