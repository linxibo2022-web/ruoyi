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
import plus.ruoyi.helper.TestLoginHelper;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AI聊天接口集成测试
 * <p>
 * 注意: 此测试需要langchain4j配置启用才能运行
 * 配置: langchain4j.enabled=true
 *
 * @author 抓蛙师
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@DisplayName("AI聊天接口集成测试")
@Tag("integration")
public class AiChatIntegrationTest extends BaseControllerTest {

    @Autowired
    private BusinessApiClient apiClient;

    @Autowired
    private TestLoginHelper testLoginHelper;

    private static String token;

    @BeforeAll
    public static void login(@Autowired TestLoginHelper testLoginHelper) {
        log.info("========== AI聊天测试开始,模拟登录 ==========");
        token = testLoginHelper.loginAsSuperAdmin();
    }

    @Test
    @DisplayName("测试快速AI对话")
    public void testAiChat() {
        log.info("测试快速AI对话");

        // 使用test接口快速测试
        ForestResponse<R<Object>> response = apiClient.testChat(token, "你好");

        // 验证HTTP响应
        assertTrue(response.isSuccess());
        R<Object> result = response.getResult();
        assertNotNull(result);

        // 注意: 如果langchain4j未启用,接口会返回404
        if (response.getStatusCode() == 404) {
            log.warn("AI聊天功能未启用(404),跳过测试");
            return;
        }

        // 验证业务响应
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());

        log.info("AI对话成功: {}", result.getData());
    }

    @AfterAll
    public static void afterAll() {
        log.info("========== AI聊天测试结束 ==========");
    }
}
