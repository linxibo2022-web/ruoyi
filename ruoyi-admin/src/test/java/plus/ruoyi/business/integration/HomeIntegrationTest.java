package plus.ruoyi.business.integration;

import com.dtflys.forest.http.ForestResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import plus.ruoyi.client.BusinessApiClient;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.test.base.BaseControllerTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 首页接口集成测试
 *
 * @author 抓蛙师
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("首页接口集成测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("integration")
public class HomeIntegrationTest extends BaseControllerTest {

    @Autowired
    private BusinessApiClient apiClient;

    @Test
    @Order(1)
    @DisplayName("测试根据appid获取租户标识")
    public void testGetTenantIdByAppid() {
        log.info("测试根据appid获取租户标识");

        // 使用测试appid (注意: 需要在b_platform表中配置)
        String testAppid = "test_appid_123";
        ForestResponse<R<String>> response = apiClient.getTenantIdByAppid(testAppid);

        // 验证HTTP响应
        assertTrue(response.isSuccess());
        R<String> result = response.getResult();
        assertNotNull(result);

        // 注意: 如果platform表中没有该appid配置,会返回失败
        if (result.getCode() == 200) {
            assertNotNull(result.getData());
            log.info("查询成功: tenantId={}", result.getData());
        } else {
            log.warn("平台配置不存在: {}", result.getMsg());
        }
    }

    @Test
    @Order(2)
    @DisplayName("测试查询广告列表")
    public void testListAds() {
        log.info("测试查询广告列表");

        // 查询所有广告 (不指定position)
        ForestResponse<R<List<Object>>> response = apiClient.listAds(null);

        // 验证响应
        assertTrue(response.isSuccess());
        R<List<Object>> result = response.getResult();
        assertNotNull(result);
        assertEquals(200, result.getCode());

        // 验证数据
        List<Object> ads = result.getData();
        assertNotNull(ads, "广告列表不应为null");

        log.info("查询成功: 广告数量={}", ads.size());
    }

    @Test
    @Order(3)
    @DisplayName("测试按位置查询广告")
    public void testListAdsByPosition() {
        log.info("测试按位置查询广告");

        // 查询首页广告位的广告
        ForestResponse<R<List<Object>>> response = apiClient.listAds("home");

        // 验证响应
        assertTrue(response.isSuccess());
        R<List<Object>> result = response.getResult();
        assertNotNull(result);
        assertEquals(200, result.getCode());

        log.info("查询成功: 首页广告数量={}", result.getData() != null ? result.getData().size() : 0);
    }

    @Test
    @Order(4)
    @DisplayName("测试分页查询商品")
    public void testPageGoods() {
        log.info("测试分页查询商品");

        // 查询第1页,每页10条
        ForestResponse<R<PageResult<Object>>> response = apiClient.pageGoods(1, 10);

        // 验证响应
        assertTrue(response.isSuccess());
        R<PageResult<Object>> result = response.getResult();
        assertNotNull(result);
        assertEquals(200, result.getCode());

        // 验证分页数据
        PageResult<Object> pageResult = result.getData();
        assertNotNull(pageResult);
        assertTrue(pageResult.getTotal() >= 0);

        log.info("查询成功: 总记录数={}, 当前页记录数={}",
            pageResult.getTotal(),
            pageResult.getRecords() != null ? pageResult.getRecords().size() : 0);
    }

    @AfterAll
    public static void afterAll() {
        log.info("========== 首页接口测试结束 ==========");
    }
}
