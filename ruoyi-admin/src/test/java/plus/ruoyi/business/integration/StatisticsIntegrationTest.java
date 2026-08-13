package plus.ruoyi.business.integration;

import com.dtflys.forest.http.ForestResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import plus.ruoyi.client.BusinessApiClient;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.test.base.BaseControllerTest;
import plus.ruoyi.helper.TestLoginHelper;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 统计接口集成测试
 *
 * @author 抓蛙师
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("统计接口集成测试")
@Tag("integration")
public class StatisticsIntegrationTest extends BaseControllerTest {

    @Autowired
    private BusinessApiClient apiClient;

    @Autowired
    private TestLoginHelper testLoginHelper;

    private static String token;

    @BeforeAll
    public static void login(@Autowired TestLoginHelper testLoginHelper) {
        log.info("========== 统计接口测试开始,模拟登录 ==========");
        token = testLoginHelper.loginAsSuperAdmin();
    }

    @Test
    @DisplayName("测试获取首页统计数据")
    public void testGetHomeStatistics() {
        log.info("测试获取首页统计数据");

        ForestResponse<R<Object>> response = apiClient.getHomeStatistics(token);

        // 验证响应
        assertTrue(response.isSuccess());
        R<Object> result = response.getResult();
        assertNotNull(result);
        assertEquals(200, result.getCode());

        // 验证统计数据
        Object statistics = result.getData();
        assertNotNull(statistics, "统计数据不应为null");

        log.info("获取首页统计数据成功: {}", statistics);
    }

    @AfterAll
    public static void afterAll() {
        log.info("========== 统计接口测试结束 ==========");
    }
}
