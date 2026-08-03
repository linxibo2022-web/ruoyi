package plus.ruoyi.system.service.core;

import plus.ruoyi.common.mybatis.helper.DataPermissionHelper;
import plus.ruoyi.common.test.base.BaseServiceTest;
import plus.ruoyi.common.test.base.TestDataBuilder;
import plus.ruoyi.system.core.domain.bo.SysUserBo;
import plus.ruoyi.system.core.domain.vo.SysUserVo;
import plus.ruoyi.system.core.service.ISysUserService;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SysUserService 用户服务测试
 * <p>
 * ⚠️ 重要:所有测试都会自动回滚,不会产生脏数据
 *
 * @author 抓蛙师
 */
@SpringBootTest
@Transactional // 关键!测试结束自动回滚,不会产生脏数据
@DisplayName("用户服务测试")
public class SysUserServiceTest extends BaseServiceTest {

    @Autowired
    private ISysUserService userService;

    @Test
    @DisplayName("测试insertUser-新增用户成功")
    public void testInsertUser() {
        // 准备测试数据
        SysUserBo user = new SysUserBo();
        user.setUserName("test_" + System.currentTimeMillis()); // 使用时间戳避免重复
        user.setNickName("测试用户");
        user.setPassword("admin123");
        user.setEmail(TestDataBuilder.randomEmail());
        user.setPhone(TestDataBuilder.randomPhone());
        user.setDeptId(103L); // 使用已存在的部门ID
        user.setGender("0"); // 用户性别

        // 执行插入
        Long userId = userService.insertUser(user);

        // 验证
        assertNotNull(userId, "用户ID不应为null");
        assertTrue(userId > 0, "用户ID应该大于0");

        // 验证可以查询到
        SysUserVo userVo = userService.getUserById(userId);
        assertNotNull(userVo, "应该能查询到刚插入的用户");
        assertEquals(user.getUserName(), userVo.getUserName());
        assertEquals(user.getNickName(), userVo.getNickName());

        // ⚠️ 注意:测试结束后,@Transactional会自动回滚
        // 这个用户数据不会真正保存到数据库
    }

    @Test
    @DisplayName("测试getUserById-查询存在的用户")
    public void testGetUserById() {
        // 使用系统已有的superadmin用户(ID通常是1)
        SysUserVo user = userService.getUserById(1L);

        assertNotNull(user, "应该能查到superadmin用户");
        assertEquals("superadmin", user.getUserName());
    }

    @Test
    @DisplayName("测试getUserById-查询不存在的用户应返回null")
    public void testGetUserByIdNotExists() {
        // 使用一个不存在的ID
        SysUserVo user = userService.getUserById(999999L);

        assertNull(user, "不存在的用户应该返回null");
    }

    @Test
    @DisplayName("测试pageUsers-分页查询用户")
    public void testPageUsers() {
        // 准备查询条件
        SysUserBo queryBo = new SysUserBo();
        PageQuery pageQuery = new PageQuery(10, 1); // pageSize, pageNum

        // 执行分页查询(忽略数据权限)
        PageResult<SysUserVo> result = DataPermissionHelper.ignore(() ->
                userService.pageUsers(queryBo, pageQuery)
        );

        // 验证
        assertNotNull(result);
        assertNotNull(result.getRecords());
        assertTrue(result.getTotal() > 0, "应该有用户数据");
    }

    @Test
    @DisplayName("测试pageUsers-按用户名查询")
    public void testPageUsersByUserName() {
        // 查询superadmin用户
        SysUserBo queryBo = new SysUserBo();
        queryBo.setUserName("superadmin");

        PageQuery pageQuery = new PageQuery(10, 1);

        // 执行查询(忽略数据权限)
        PageResult<SysUserVo> result = DataPermissionHelper.ignore(() ->
                userService.pageUsers(queryBo, pageQuery)
        );

        assertNotNull(result);
        assertTrue(result.getTotal() > 0, "应该能查到superadmin用户");

        // 验证查询结果包含superadmin
        boolean hasSuperAdmin = result.getRecords().stream()
                .anyMatch(u -> "superadmin".equals(u.getUserName()));
        assertTrue(hasSuperAdmin, "查询结果应该包含superadmin用户");
    }

    @Test
    @DisplayName("测试updateUser-更新用户信息")
    public void testUpdateUser() {
        // 1. 先插入一个测试用户
        SysUserBo user = new SysUserBo();
        user.setUserName("test_update_" + System.currentTimeMillis());
        user.setNickName("原始昵称");
        user.setPassword("admin123");
        user.setEmail(TestDataBuilder.randomEmail());
        user.setPhone(TestDataBuilder.randomPhone());
        user.setDeptId(103L);
        user.setGender("0");

        Long userId = userService.insertUser(user);
        assertNotNull(userId);

        // 2. 更新用户信息
        user.setUserId(userId);
        user.setNickName("更新后的昵称");
        user.setEmail("new_" + TestDataBuilder.randomEmail());

        boolean updateResult = userService.updateUser(user);
        assertTrue(updateResult, "更新应该成功");

        // 3. 验证更新是否生效
        SysUserVo updated = userService.getUserById(userId);
        assertNotNull(updated);
        assertEquals("更新后的昵称", updated.getNickName());

        // ⚠️ 测试结束自动回滚,数据不会保存
    }

    @Test
    @DisplayName("测试deleteUserById-删除用户")
    public void testDeleteUserById() {
        // 1. 先插入一个测试用户
        SysUserBo user = new SysUserBo();
        user.setUserName("test_delete_" + System.currentTimeMillis());
        user.setNickName("待删除用户");
        user.setPassword("admin123");
        user.setEmail(TestDataBuilder.randomEmail());
        user.setPhone(TestDataBuilder.randomPhone());
        user.setDeptId(103L);
        user.setGender("0");

        Long userId = userService.insertUser(user);
        assertNotNull(userId);

        // 2. 验证用户存在
        SysUserVo before = userService.getUserById(userId);
        assertNotNull(before, "删除前应该能查到用户");

        // 3. 删除用户
        boolean deleteResult = userService.deleteUserById(userId);
        assertTrue(deleteResult, "删除应该成功");

        // 4. 验证用户已被删除
        SysUserVo after = userService.getUserById(userId);
        assertNull(after, "删除后应该查不到用户");

        // ⚠️ 测试结束自动回滚,数据库不会真正删除
    }

    @Test
    @DisplayName("测试isUserNameUnique-检查用户名唯一性")
    public void testIsUserNameUnique() {
        // superadmin用户名已存在,应该返回false(不唯一)
        boolean isUnique = userService.isUserNameUnique("superadmin", null);
        assertFalse(isUnique, "superadmin用户名已存在,应该返回false");

        // 不存在的用户名,应该返回true(唯一)
        isUnique = userService.isUserNameUnique("not_exists_user_" + System.currentTimeMillis(), null);
        assertTrue(isUnique, "不存在的用户名应该返回true");
    }

    @Test
    @DisplayName("测试isPhoneUnique-检查手机号唯一性")
    public void testIsPhoneUnique() {
        // 1. 插入测试用户
        String testPhone = "13800138000";
        SysUserBo user = new SysUserBo();
        user.setUserName("test_phone_" + System.currentTimeMillis());
        user.setNickName("测试手机号");
        user.setPassword("admin123");
        user.setEmail(TestDataBuilder.randomEmail());
        user.setPhone(testPhone);
        user.setDeptId(103L);
        user.setGender("0");

        Long userId = userService.insertUser(user);
        assertNotNull(userId);

        // 2. 检查相同手机号,应该不唯一
        boolean isUnique = userService.isPhoneUnique(testPhone, null);
        assertFalse(isUnique, "已存在的手机号应该返回false");

        // 3. 检查不存在的手机号,应该唯一
        isUnique = userService.isPhoneUnique(TestDataBuilder.randomPhone(), null);
        assertTrue(isUnique, "不存在的手机号应该返回true");

        // ⚠️ 测试结束自动回滚
    }

    @Test
    @DisplayName("测试resetUserPwd-重置用户密码")
    public void testResetUserPwd() {
        // 1. 插入测试用户
        SysUserBo user = new SysUserBo();
        user.setUserName("test_pwd_" + System.currentTimeMillis());
        user.setNickName("测试重置密码");
        user.setPassword("admin123");
        user.setEmail(TestDataBuilder.randomEmail());
        user.setPhone(TestDataBuilder.randomPhone());
        user.setDeptId(103L);
        user.setGender("0");

        Long userId = userService.insertUser(user);
        assertNotNull(userId);

        // 2. 重置密码
        boolean updateResult = userService.resetUserPwd(userId, "newPassword123");
        assertTrue(updateResult, "重置密码应该成功");

        // 3. 验证密码已更新(这里只能验证更新成功,无法直接比对加密后的密码)
        SysUserVo updated = userService.getUserById(userId);
        assertNotNull(updated);

        // ⚠️ 测试结束自动回滚
    }
}
