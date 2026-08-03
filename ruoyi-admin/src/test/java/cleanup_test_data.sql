-- ============================================
-- 集成测试数据清理脚本
-- 使用方法: mysql -u root -p ry_plus_new < cleanup_test_data.sql
-- 或在 MySQL Workbench / Navicat 中直接执行
-- ============================================


SET FOREIGN_KEY_CHECKS = 0;

START TRANSACTION;

-- ============================================
-- 1. 清理关联表数据
-- ============================================

-- 清理测试角色的菜单关联
DELETE FROM sys_role_menu
WHERE role_id IN (
    SELECT role_id FROM sys_role
    WHERE role_name LIKE '测试角色_%'
       OR role_name LIKE '待删除角色_%'
       OR role_key LIKE 'test_role_%'
       OR role_key LIKE 'delete_test_%'
);

-- 清理测试角色的部门关联
DELETE FROM sys_role_dept
WHERE role_id IN (
    SELECT role_id FROM sys_role
    WHERE role_name LIKE '测试角色_%'
       OR role_name LIKE '待删除角色_%'
       OR role_key LIKE 'test_role_%'
       OR role_key LIKE 'delete_test_%'
);

-- 清理测试用户的角色关联
DELETE FROM sys_user_role
WHERE user_id IN (
    SELECT user_id FROM sys_user
    WHERE user_name LIKE 'test_%'
);

-- 清理测试用户的岗位关联
DELETE FROM sys_user_post
WHERE user_id IN (
    SELECT user_id FROM sys_user
    WHERE user_name LIKE 'test_%'
);

-- ============================================
-- 2. 清理主表数据
-- ============================================

-- 清理测试角色
DELETE FROM sys_role
WHERE role_name LIKE '测试角色_%'
   OR role_name LIKE '待删除角色_%'
   OR role_key LIKE 'test_role_%'
   OR role_key LIKE 'delete_test_%';

-- 清理测试部门
DELETE FROM sys_dept
WHERE dept_name LIKE '测试部门_%';

-- 清理测试菜单
DELETE FROM sys_menu
WHERE menu_name LIKE '测试菜单_%';

-- 清理测试用户
DELETE FROM sys_user
WHERE user_name LIKE 'test_%';

-- ============================================
-- 3. 提交事务
-- ============================================

COMMIT;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 4. 查看清理结果
-- ============================================

SELECT '=== 清理完成 ===' AS status;

SELECT
    (SELECT COUNT(*) FROM sys_role
     WHERE role_name LIKE '测试角色_%'
        OR role_name LIKE '待删除角色_%'
        OR role_key LIKE 'test_role_%'
        OR role_key LIKE 'delete_test_%') AS test_roles,
    (SELECT COUNT(*) FROM sys_dept WHERE dept_name LIKE '测试部门_%') AS test_depts,
    (SELECT COUNT(*) FROM sys_menu WHERE menu_name LIKE '测试菜单_%') AS test_menus,
    (SELECT COUNT(*) FROM sys_user WHERE user_name LIKE 'test_%') AS test_users;

SELECT '如果上述数字都是0，说明清理成功' AS result;
