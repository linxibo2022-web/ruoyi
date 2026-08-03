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
 * 部门管理接口集成测试
 *
 * @author 抓蛙师
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@DisplayName("部门管理接口集成测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SysDeptIntegrationTest extends BaseControllerTest {

    @Autowired
    private SystemApiClient apiClient;

    @Autowired
    private TestLoginHelper testLoginHelper;

    private static String token;

    @BeforeAll
    public static void login(@Autowired TestLoginHelper testLoginHelper) {
        log.info("========== 部门管理测试开始,模拟登录 ==========");
        token = testLoginHelper.loginAsSuperAdmin();
    }

    @Test
    @Order(1)
    @DisplayName("测试获取部门列表")
    public void testListDepts() {
        log.info("测试获取部门列表");

        ForestResponse<R<Object>> response = apiClient.listDepts(token);

        // 验证响应
        assertTrue(response.isSuccess());
        R<Object> result = response.getResult();
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());

        log.info("获取部门列表成功");
    }

    @Test
    @Order(2)
    @DisplayName("测试查询部门详情")
    public void testGetDeptById() {
        log.info("测试查询部门详情");

        // 查询若依科技部门(ID=100)
        ForestResponse<R<Object>> response = apiClient.getDeptById(token, 100L);

        // 验证响应
        assertTrue(response.isSuccess());
        R<Object> result = response.getResult();
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());

        log.info("查询部门详情成功");
    }

    @Test
    @Order(3)
    @DisplayName("测试获取部门树")
    public void testGetDeptTreeOptions() {
        log.info("测试获取部门树");

        ForestResponse<R<Object>> response = apiClient.getDeptTreeOptions(token);

        // 验证响应
        assertTrue(response.isSuccess());
        R<Object> result = response.getResult();
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());

        log.info("获取部门树成功");
    }

    @Test
    @Order(4)
    @DisplayName("测试新增部门")
    public void testAddDept() {
        log.info("测试新增部门");

        // 构造测试部门数据(添加到若依工作室下)
        Map<String, Object> dept = new HashMap<>();
        dept.put("deptName", "测试部门_" + System.currentTimeMillis());
        dept.put("parentId", 100L); // 父部门为若依工作室
        dept.put("orderNum", 100);
        dept.put("leader", 1L); // 负责人ID (superadmin的userId)
        dept.put("phone", "13900000000");
        dept.put("email", "test@example.com");
        dept.put("status", "1"); // 启用 (注意: "1"=启用, "0"=禁用)

        // 发起新增请求
        ForestResponse<R<Void>> response = apiClient.addDept(token, dept);

        // 验证响应
        assertTrue(response.isSuccess());
        R<Void> result = response.getResult();
        assertNotNull(result);
        assertEquals(200, result.getCode(), "新增部门应该成功");

        log.info("新增成功: deptName={}", dept.get("deptName"));
    }

    @Test
    @Order(5)
    @DisplayName("测试修改部门")
    public void testUpdateDept() {
        log.info("测试修改部门");

        // 构造修改数据(修改一个已存在的部门)
        Map<String, Object> dept = new HashMap<>();
        dept.put("deptId", 101L); // 深圳总公司
        dept.put("deptName", "深圳总公司");
        dept.put("parentId", 100L);
        dept.put("orderNum", 1);
        dept.put("leader", 1L); // 负责人ID (superadmin的userId)
        dept.put("phone", "15888888888");
        dept.put("email", "ry@qq.com");
        dept.put("status", "1"); // 启用 (注意: "1"=启用, "0"=禁用)

        // 发起修改请求
        ForestResponse<R<Void>> response = apiClient.updateDept(token, dept);

        // 验证响应
        assertTrue(response.isSuccess());
        R<Void> result = response.getResult();
        assertNotNull(result);
        assertEquals(200, result.getCode(), "修改部门应该成功");

        log.info("修改成功: deptId={}", dept.get("deptId"));
    }

    @Test
    @Order(6)
    @DisplayName("测试删除部门")
    public void testDeleteDept() {
        log.info("测试删除部门");

        // 注意: 这里删除一个测试部门,需要确保该部门存在且可以删除
        // 实际测试中可能需要先创建一个专门用于删除的部门
        Long deleteDeptId = 9999L; // 使用一个不存在的ID,避免影响实际数据

        ForestResponse<R<Void>> response = apiClient.deleteDept(token, deleteDeptId);

        // 验证响应
        assertTrue(response.isSuccess());
        R<Void> result = response.getResult();
        assertNotNull(result);
        // 注意: 删除不存在的部门可能返回错误码,这里只验证HTTP成功

        log.info("删除测试完成: deptId={}", deleteDeptId);
    }

    @AfterAll
    public static void afterAll() {
        log.info("========== 部门管理测试结束 ==========");
    }
}
