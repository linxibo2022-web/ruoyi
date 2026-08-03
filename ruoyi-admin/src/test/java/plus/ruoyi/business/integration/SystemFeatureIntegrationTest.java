package plus.ruoyi.business.integration;

import com.dtflys.forest.http.ForestResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import plus.ruoyi.client.BusinessApiClient;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.test.base.BaseControllerTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 系统功能配置接口集成测试
 *
 * @author 抓蛙师
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@DisplayName("系统功能配置接口集成测试")
public class SystemFeatureIntegrationTest extends BaseControllerTest {

    @Autowired
    private BusinessApiClient apiClient;

    @Test
    @DisplayName("测试获取系统功能开关")
    public void testGetSystemFeatures() {
        log.info("测试获取系统功能开关");

        ForestResponse<R<Object>> response = apiClient.getSystemFeatures();

        // 验证响应
        assertTrue(response.isSuccess());
        R<Object> result = response.getResult();
        assertNotNull(result);
        assertEquals(200, result.getCode());

        // 验证功能配置数据
        Object features = result.getData();
        assertNotNull(features, "系统功能配置不应为null");

        log.info("获取系统功能开关成功: {}", features);
    }

    @AfterAll
    public static void afterAll() {
        log.info("========== 系统功能配置接口测试结束 ==========");
    }
}
