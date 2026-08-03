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
 * 字典类型管理接口集成测试
 *
 * @author 抓蛙师
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@DisplayName("字典类型管理接口集成测试")
public class SysDictTypeIntegrationTest extends BaseControllerTest {

    @Autowired
    private SystemApiClient apiClient;

    @Autowired
    private TestLoginHelper testLoginHelper;

    private static String token;

    @BeforeAll
    public static void login(@Autowired TestLoginHelper testLoginHelper) {
        log.info("========== 字典类型管理测试开始,模拟登录 ==========");
        token = testLoginHelper.loginAsSuperAdmin();
    }

    @Test
    @DisplayName("测试分页查询字典类型列表")
    public void testPageDictTypes() {
        log.info("测试分页查询字典类型列表");

        // 查询第1页,每页10条
        ForestResponse<R<PageResult<Object>>> response =
            apiClient.pageDictTypes(token, 1, 10);

        // 验证响应
        assertTrue(response.isSuccess());
        R<PageResult<Object>> result = response.getResult();
        assertNotNull(result);
        assertEquals(200, result.getCode());

        // 验证分页数据
        PageResult<Object> pageResult = result.getData();
        assertNotNull(pageResult);
        assertNotNull(pageResult.getRecords());
        assertTrue(pageResult.getTotal() > 0, "应该有字典类型数据");

        log.info("查询成功: 总记录数={}, 当前页记录数={}",
            pageResult.getTotal(), pageResult.getRecords().size());
    }

    @Test
    @DisplayName("测试查询字典类型详情")
    public void testGetDictTypeById() {
        log.info("测试查询字典类型详情");

        // 查询用户性别字典类型(ID=1, dictType=sys_user_gender)
        ForestResponse<R<Object>> response = apiClient.getDictTypeById(token, 1L);

        // 验证响应
        assertTrue(response.isSuccess());
        R<Object> result = response.getResult();
        assertNotNull(result);
        assertEquals(200, result.getCode());

        // 验证数据
        Object dictType = result.getData();
        assertNotNull(dictType, "字典类型数据不应为null");

        log.info("查询成功: dictType={}", dictType);
    }

    @Test
    @DisplayName("测试获取字典类型选项列表")
    public void testGetDictTypeOptions() {
        log.info("测试获取字典类型选项列表");

        ForestResponse<R<Object>> response = apiClient.getDictTypeOptions(token);

        // 验证响应
        assertTrue(response.isSuccess());
        R<Object> result = response.getResult();
        assertNotNull(result);
        assertEquals(200, result.getCode());

        // 验证数据
        Object options = result.getData();
        assertNotNull(options, "字典类型选项不应为null");

        log.info("查询成功: options={}", options);
    }

    @AfterAll
    public static void afterAll() {
        log.info("========== 字典类型管理测试结束 ==========");
    }
}
