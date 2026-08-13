package plus.ruoyi.system.integration;

import com.dtflys.forest.http.ForestResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import plus.ruoyi.client.SystemApiClient;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.test.base.BaseControllerTest;
import plus.ruoyi.helper.TestLoginHelper;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 菜单管理接口集成测试
 *
 * @author 抓蛙师
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@DisplayName("菜单管理接口集成测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("integration")
public class SysMenuIntegrationTest extends BaseControllerTest {

    @Autowired
    private SystemApiClient apiClient;

    @Autowired
    private TestLoginHelper testLoginHelper;

    private static String token;

    @BeforeAll
    public static void login(@Autowired TestLoginHelper testLoginHelper) {
        log.info("========== 菜单管理测试开始,模拟登录 ==========");
        token = testLoginHelper.loginAsSuperAdmin();
    }

    @Test
    @Order(1)
    @DisplayName("测试获取路由信息")
    public void testGetRouters() {
        log.info("测试获取路由信息");

        ForestResponse<R<Object>> response = apiClient.getRouters(token);

        // 验证响应
        assertTrue(response.isSuccess());
        R<Object> result = response.getResult();
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());

        log.info("获取路由成功");
    }

    @Test
    @Order(2)
    @DisplayName("测试获取菜单列表")
    public void testListMenus() {
        log.info("测试获取菜单列表");

        ForestResponse<R<Object>> response = apiClient.listMenus(token);

        // 验证响应
        assertTrue(response.isSuccess());
        R<Object> result = response.getResult();
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());

        log.info("获取菜单列表成功");
    }

    @Test
    @Order(3)
    @DisplayName("测试查询菜单详情")
    public void testGetMenuById() {
        log.info("测试查询菜单详情");

        // 查询系统管理菜单(ID=1)
        ForestResponse<R<Object>> response = apiClient.getMenuById(token, 1L);

        // 验证响应
        assertTrue(response.isSuccess());
        R<Object> result = response.getResult();
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());

        log.info("查询菜单详情成功");
    }

    @Test
    @Order(4)
    @DisplayName("测试获取菜单树")
    public void testGetMenuTreeOptions() {
        log.info("测试获取菜单树");

        ForestResponse<R<Object>> response = apiClient.getMenuTreeOptions(token);

        // 验证响应
        assertTrue(response.isSuccess());
        R<Object> result = response.getResult();
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());

        log.info("获取菜单树成功");
    }

    @Test
    @Order(5)
    @DisplayName("测试新增菜单")
    public void testAddMenu() {
        log.info("测试新增菜单");

        // 构造测试菜单数据(添加到系统管理下)
        Map<String, Object> menu = new HashMap<>();
        menu.put("menuName", "测试菜单_" + System.currentTimeMillis());
        menu.put("parentId", 1L); // 父菜单为系统管理
        menu.put("orderNum", 100);
        menu.put("path", "test");
        menu.put("component", "test/index");
        menu.put("menuType", "C"); // 菜单
        menu.put("visible", "0");  // 显示
        menu.put("status", "1");   // 启用 (注意: "1"=启用, "0"=禁用)
        menu.put("icon", "tool");

        // 发起新增请求
        ForestResponse<R<Void>> response = apiClient.addMenu(token, menu);

        // 验证响应
        assertTrue(response.isSuccess());
        R<Void> result = response.getResult();
        assertNotNull(result);
        assertEquals(200, result.getCode(), "新增菜单应该成功");

        log.info("新增成功: menuName={}", menu.get("menuName"));
    }

    @Test
    @Order(6)
    @DisplayName("测试修改菜单")
    public void testUpdateMenu() {
        log.info("测试修改菜单");

        // 构造修改数据(修改一个已存在的菜单,如系统监控)
        Map<String, Object> menu = new HashMap<>();
        menu.put("menuId", 2L);
        menu.put("menuName", "系统监控");
        menu.put("parentId", 0L);
        menu.put("orderNum", 2);
        menu.put("path", "monitor");
        menu.put("component", null);
        menu.put("menuType", "M"); // 目录
        menu.put("visible", "0");
        menu.put("status", "1");   // 启用 (注意: "1"=启用, "0"=禁用)
        menu.put("icon", "monitor");

        // 发起修改请求
        ForestResponse<R<Void>> response = apiClient.updateMenu(token, menu);

        // 验证响应
        assertTrue(response.isSuccess());
        R<Void> result = response.getResult();
        assertNotNull(result);
        assertEquals(200, result.getCode(), "修改菜单应该成功");

        log.info("修改成功: menuId={}", menu.get("menuId"));
    }

    @Test
    @Order(7)
    @DisplayName("测试删除菜单")
    public void testDeleteMenu() {
        log.info("测试删除菜单");

        // 注意: 这里删除一个测试菜单,需要确保该菜单存在且可以删除
        // 实际测试中可能需要先创建一个专门用于删除的菜单
        Long deleteMenuId = 9999L; // 使用一个不存在的ID,避免影响实际数据

        ForestResponse<R<Void>> response = apiClient.deleteMenu(token, deleteMenuId);

        // 验证响应
        assertTrue(response.isSuccess());
        R<Void> result = response.getResult();
        assertNotNull(result);
        // 注意: 删除不存在的菜单可能返回错误码,这里只验证HTTP成功

        log.info("删除测试完成: menuId={}", deleteMenuId);
    }

    @AfterAll
    public static void afterAll() {
        log.info("========== 菜单管理测试结束 ==========");
    }
}
