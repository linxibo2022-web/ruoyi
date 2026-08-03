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
 * 通知公告接口集成测试
 *
 * @author 抓蛙师
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@DisplayName("通知公告接口集成测试")
public class SysNoticeIntegrationTest extends BaseControllerTest {

    @Autowired
    private SystemApiClient apiClient;

    @Autowired
    private TestLoginHelper testLoginHelper;

    private static String token;

    @BeforeAll
    public static void login(@Autowired TestLoginHelper testLoginHelper) {
        log.info("========== 通知公告测试开始,模拟登录 ==========");
        token = testLoginHelper.loginAsSuperAdmin();
    }

    @Test
    @DisplayName("测试分页查询通知公告")
    public void testPageNotices() {
        log.info("测试分页查询通知公告");

        // 查询第1页,每页10条
        ForestResponse<R<PageResult<Object>>> response =
            apiClient.pageNotices(token, 1, 10);

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
    @DisplayName("测试查询通知公告详情")
    public void testGetNotice() {
        log.info("测试查询通知公告详情");

        // 查询系统内置公告(ID=1: 温馨提醒：2018-07-01 若依新版本发布啦)
        ForestResponse<R<Object>> response = apiClient.getNoticeById(token, 1L);

        // 验证响应
        assertTrue(response.isSuccess());
        R<Object> result = response.getResult();
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());

        log.info("查询公告详情成功");
    }

    @Test
    @DisplayName("测试标记公告为已读")
    public void testMarkNoticeAsRead() {
        log.info("测试标记公告为已读");

        // 标记ID=1的公告为已读
        ForestResponse<R<Void>> response = apiClient.markNoticeAsRead(token, 1L);

        // 验证响应
        assertTrue(response.isSuccess());
        R<Void> result = response.getResult();
        assertNotNull(result);
        assertEquals(200, result.getCode());

        log.info("标记公告已读成功");
    }

    @Test
    @DisplayName("测试获取用户通知列表")
    public void testPageUserNotices() {
        log.info("测试获取用户通知列表");

        ForestResponse<R<PageResult<Object>>> response =
            apiClient.pageUserNotices(token, 1, 10);

        // 验证响应
        assertTrue(response.isSuccess());
        R<PageResult<Object>> result = response.getResult();
        assertNotNull(result);
        assertEquals(200, result.getCode());

        log.info("获取用户通知列表成功");
    }

    @Test
    @DisplayName("测试获取未读通知数量")
    public void testGetNoticeUnreadCount() {
        log.info("测试获取未读通知数量");

        ForestResponse<R<Long>> response = apiClient.getNoticeUnreadCount(token);

        // 验证响应
        assertTrue(response.isSuccess());
        R<Long> result = response.getResult();
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData() >= 0);

        log.info("未读通知数量: {}", result.getData());
    }

    @AfterAll
    public static void afterAll() {
        log.info("========== 通知公告测试结束 ==========");
    }
}
