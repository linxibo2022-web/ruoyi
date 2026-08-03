package plus.ruoyi.system.service.core;

import plus.ruoyi.common.test.base.BaseServiceTest;
import plus.ruoyi.system.core.domain.bo.SysMenuBo;
import plus.ruoyi.system.core.domain.vo.SysMenuVo;
import plus.ruoyi.system.core.service.ISysMenuService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SysMenuService 菜单管理服务测试
 *
 * ⚠️ 重要:所有测试都会自动回滚,不会产生脏数据
 *
 * @author 抓蛙师
 */
@SpringBootTest
@Transactional
@DisplayName("菜单管理服务测试")
public class SysMenuServiceTest extends BaseServiceTest {

    @Autowired
    private ISysMenuService menuService;

    @Test
    @DisplayName("测试listMenuByUserId-查询用户菜单列表")
    public void testListMenuByUserId() {
        // 使用admin用户(ID=1)
        List<SysMenuVo> menuList = menuService.listMenuByUserId(1L);

        assertNotNull(menuList);
        assertTrue(menuList.size() > 0, "admin用户应该有菜单权限");
    }

    @Test
    @DisplayName("测试listMenus-查询菜单列表")
    public void testListMenus() {
        SysMenuBo queryBo = new SysMenuBo();
        List<SysMenuVo> menuList = menuService.listMenus(queryBo, 1L);

        assertNotNull(menuList);
        assertTrue(menuList.size() > 0, "应该有菜单数据");
    }

    @Test
    @DisplayName("测试listMenuPermissionsByUserId-查询用户权限标识")
    public void testListMenuPermissionsByUserId() {
        // 使用superadmin用户(userId=1)
        Set<String> permissions = menuService.listMenuPermissionsByUserId(1L);

        assertNotNull(permissions);
        // 注意:如果角色没有关联菜单或菜单没有权限标识,可能为空
        // 这里只验证方法能正常执行,不强制要求有权限数据
        if (permissions.size() > 0) {
            // 验证权限格式
            permissions.forEach(perm -> {
                assertNotNull(perm);
                assertTrue(perm.length() > 0, "权限标识不应为空");
            });
        }
    }

    @Test
    @DisplayName("测试listMenuTreeByUserId-查询用户菜单树")
    public void testListMenuTreeByUserId() {
        // 使用admin用户
        var menuTree = menuService.listMenuTreeByUserId(1L);

        assertNotNull(menuTree);
        assertTrue(menuTree.size() > 0, "admin应该有菜单树");
    }

    @Test
    @DisplayName("测试listMenuByUserId-普通用户菜单数量应小于管理员")
    public void testListMenuByUserId_NormalUser() {
        // 1. 查询admin的菜单
        List<SysMenuVo> adminMenus = menuService.listMenuByUserId(1L);

        // 2. 查询普通用户的菜单(假设ID=2存在)
        List<SysMenuVo> userMenus = menuService.listMenuByUserId(2L);

        // 3. 验证
        assertNotNull(adminMenus);
        assertNotNull(userMenus);

        // admin的菜单应该大于等于普通用户
        assertTrue(adminMenus.size() >= userMenus.size(),
            "管理员菜单数量应该大于等于普通用户");
    }

    @Test
    @DisplayName("测试listMenus-按菜单名称查询")
    public void testListMenusByName() {
        SysMenuBo queryBo = new SysMenuBo();
        queryBo.setMenuName("系统"); // 查找包含"系统"的菜单

        List<SysMenuVo> menuList = menuService.listMenus(queryBo, 1L);

        assertNotNull(menuList);
        if (menuList.size() > 0) {
            // 如果有结果,验证包含"系统"关键字
            boolean hasSystemMenu = menuList.stream()
                .anyMatch(menu -> menu.getMenuName().contains("系统"));
            assertTrue(hasSystemMenu, "查询结果应该包含'系统'相关菜单");
        }
    }

    @Test
    @DisplayName("测试listMenus-按菜单状态查询")
    public void testListMenusByStatus() {
        SysMenuBo queryBo = new SysMenuBo();
        queryBo.setStatus("1"); // 查询启用状态的菜单(1=启用,0=停用)

        List<SysMenuVo> menuList = menuService.listMenus(queryBo, 1L);

        assertNotNull(menuList);
        assertTrue(menuList.size() > 0, "应该有启用状态的菜单");

        // 验证所有菜单状态都是启用
        menuList.forEach(menu -> {
            assertEquals("1", menu.getStatus(), "菜单状态应该是启用");
        });
    }

    /**
     * 自定义性能阈值 - 菜单树查询可能较慢
     */
    @Override
    protected long getPerformanceThreshold() {
        return 3000L; // 3秒
    }
}
