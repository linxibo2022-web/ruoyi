package plus.ruoyi.system.service.core;

import plus.ruoyi.common.mybatis.helper.DataPermissionHelper;
import plus.ruoyi.common.test.base.BaseServiceTest;
import plus.ruoyi.common.test.base.TestDataBuilder;
import plus.ruoyi.system.core.domain.bo.SysRoleBo;
import plus.ruoyi.system.core.domain.vo.SysRoleVo;
import plus.ruoyi.system.core.service.ISysRoleService;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SysRoleService 角色管理服务测试
 *
 * ⚠️ 重要:所有测试都会自动回滚,不会产生脏数据
 *
 * @author 抓蛙师
 */
@SpringBootTest
@Transactional
@DisplayName("角色管理服务测试")
public class SysRoleServiceTest extends BaseServiceTest {

    @Autowired
    private ISysRoleService roleService;

    @Test
    @DisplayName("测试get-查询角色详情")
    public void testGet() {
        // 1. 插入测试角色
        SysRoleBo role = createTestRole("测试角色");
        int saveResult = roleService.batchSave(List.of(role));
        assertTrue(saveResult > 0, "保存应该成功");

        // 2. 通过roleKey查询获取roleId（忽略数据权限）
        SysRoleBo queryBo = new SysRoleBo();
        queryBo.setRoleKey(role.getRoleKey());
        List<SysRoleVo> list = DataPermissionHelper.ignore(() -> roleService.list(queryBo));
        assertFalse(list.isEmpty(), "应该能查到刚插入的角色");
        Long roleId = list.get(0).getRoleId();

        // 3. 查询角色详情
        SysRoleVo roleVo = roleService.get(roleId);

        // 4. 验证
        assertNotNull(roleVo);
        assertEquals(role.getRoleName(), roleVo.getRoleName());
        assertEquals(role.getRoleKey(), roleVo.getRoleKey());
    }

    @Test
    @DisplayName("测试list-查询角色列表")
    public void testList() {
        // 1. 插入测试数据
        createAndSaveTestRole("列表测试1");
        createAndSaveTestRole("列表测试2");

        // 2. 查询列表（忽略数据权限）
        SysRoleBo queryBo = new SysRoleBo();
        List<SysRoleVo> list = DataPermissionHelper.ignore(() -> roleService.list(queryBo));

        // 3. 验证
        assertNotNull(list);
        assertTrue(list.size() >= 2, "至少应该有2条测试数据");
    }

    @Test
    @DisplayName("测试page-分页查询角色")
    public void testPage() {
        // 1. 插入测试数据
        for (int i = 0; i < 3; i++) {
            createAndSaveTestRole("分页测试" + i);
        }

        // 2. 分页查询(忽略数据权限)
        SysRoleBo queryBo = new SysRoleBo();
        PageQuery pageQuery = new PageQuery(10, 1);
        PageResult<SysRoleVo> result = DataPermissionHelper.ignore(() ->
            roleService.page(queryBo, pageQuery)
        );

        // 3. 验证
        assertNotNull(result);
        assertTrue(result.getTotal() >= 3, "至少应该有3条数据");
    }

    @Test
    @DisplayName("测试page-按角色名称查询")
    public void testPageByRoleName() {
        // 1. 插入唯一标识的测试数据
        String suffix = String.valueOf(System.currentTimeMillis() % 1000000);
        String uniqueName = "唯一角色_" + suffix;
        SysRoleBo role = createTestRole("唯一角色");
        role.setRoleName(uniqueName);
        role.setRoleKey("unique_" + suffix);
        roleService.batchSave(List.of(role));

        // 2. 按名称查询(忽略数据权限)
        SysRoleBo queryBo = new SysRoleBo();
        queryBo.setRoleName(uniqueName);
        PageQuery pageQuery = new PageQuery(10, 1);
        PageResult<SysRoleVo> result = DataPermissionHelper.ignore(() ->
            roleService.page(queryBo, pageQuery)
        );

        // 3. 验证
        assertNotNull(result);
        assertTrue(result.getTotal() > 0, "应该能查到测试角色");
        boolean found = result.getRecords().stream()
            .anyMatch(r -> uniqueName.equals(r.getRoleName()));
        assertTrue(found, "查询结果应该包含测试角色");
    }

    @Test
    @DisplayName("测试batchDelete-批量删除角色")
    public void testBatchDelete() {
        // 1. 插入测试数据
        String suffix = String.valueOf(System.currentTimeMillis() % 1000000);
        SysRoleBo role1 = createTestRole("待删除1");
        role1.setRoleKey("del1_" + suffix);
        SysRoleBo role2 = createTestRole("待删除2");
        role2.setRoleKey("del2_" + suffix);
        roleService.batchSave(List.of(role1, role2));

        // 2. 查询获取ID（忽略数据权限）
        SysRoleBo queryBo1 = new SysRoleBo();
        queryBo1.setRoleKey(role1.getRoleKey());
        Long id1 = DataPermissionHelper.ignore(() -> roleService.list(queryBo1)).get(0).getRoleId();

        SysRoleBo queryBo2 = new SysRoleBo();
        queryBo2.setRoleKey(role2.getRoleKey());
        Long id2 = DataPermissionHelper.ignore(() -> roleService.list(queryBo2)).get(0).getRoleId();

        // 3. 验证数据存在
        assertNotNull(roleService.get(id1));
        assertNotNull(roleService.get(id2));

        // 4. 批量删除
        int deleteResult = roleService.batchDelete(List.of(id1, id2));
        assertTrue(deleteResult > 0, "删除应该成功");

        // 5. 验证已删除
        assertNull(roleService.get(id1), "id1应该已被删除");
        assertNull(roleService.get(id2), "id2应该已被删除");
    }

    @Test
    @DisplayName("测试listRolesByUserId-查询用户的角色列表")
    public void testListRolesByUserId() {
        // 使用admin用户(ID=1)
        List<SysRoleVo> roles = roleService.listRolesByUserId(1L);

        assertNotNull(roles);
        assertTrue(roles.size() > 0, "admin用户应该至少有一个角色");
    }

    /**
     * 创建测试角色对象
     */
    private SysRoleBo createTestRole(String namePrefix) {
        SysRoleBo role = new SysRoleBo();
        // role_name字段限制30字符,使用短时间戳(后6位)
        String suffix = String.valueOf(System.currentTimeMillis() % 1000000);
        role.setRoleName(namePrefix + "_" + suffix);
        role.setRoleKey("test_role_" + suffix);
        role.setRoleSort(99);
        role.setStatus("0"); // 正常
        role.setRemark("测试角色");
        return role;
    }

    /**
     * 创建并保存测试角色(返回void,因为batchSave不回填ID)
     */
    private void createAndSaveTestRole(String namePrefix) {
        SysRoleBo role = createTestRole(namePrefix);
        roleService.batchSave(List.of(role));
    }

    /**
     * 自定义性能阈值 - 角色操作应该很快
     */
    @Override
    protected long getPerformanceThreshold() {
        return 2000L; // 2秒
    }
}
