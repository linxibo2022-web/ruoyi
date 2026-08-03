# 集成测试数据清理说明

## ⚠️ 重要说明

**集成测试不会自动回滚数据！**

与单元测试不同，集成测试通过真实的HTTP请求访问运行中的应用程序，所有数据操作都会真实写入数据库。

---

## 📋 测试数据特征

所有测试数据都有明确的标识，便于识别和清理：

| 模块 | 数据标识 | 示例 |
|------|---------|------|
| 角色 | `测试角色_时间戳` | 测试角色_1759906789419 |
| 角色Key | `test_role_时间戳` | test_role_1759906789419 |
| 部门 | `测试部门_时间戳` | 测试部门_1759906800131 |
| 菜单 | `测试菜单_时间戳` | 测试菜单_1759906820456 |
| 用户 | `test_时间戳` | test_1759906830789 |

---

## 🧹 手动清理SQL

### 清理所有测试数据

```sql
-- 清理测试角色
DELETE FROM sys_role WHERE role_name LIKE '测试角色_%';
DELETE FROM sys_role WHERE role_key LIKE 'test_role_%';

-- 清理测试部门
DELETE FROM sys_dept WHERE dept_name LIKE '测试部门_%';

-- 清理测试菜单
DELETE FROM sys_menu WHERE menu_name LIKE '测试菜单_%';

-- 清理测试用户
DELETE FROM sys_user WHERE user_name LIKE 'test_%';

-- 清理关联数据
DELETE FROM sys_user_role WHERE user_id NOT IN (SELECT user_id FROM sys_user);
DELETE FROM sys_user_post WHERE user_id NOT IN (SELECT user_id FROM sys_user);
DELETE FROM sys_role_menu WHERE role_id NOT IN (SELECT role_id FROM sys_role);
DELETE FROM sys_role_dept WHERE role_id NOT IN (SELECT role_id FROM sys_role);
```

### 只清理最近的测试数据（按时间戳）

```sql
-- 清理最近1小时的测试数据
-- 时间戳计算: 当前时间 - 1小时 = UNIX_TIMESTAMP(NOW() - INTERVAL 1 HOUR) * 1000

-- 示例: 清理 2025-10-08 14:00 之后的测试数据
-- 时间戳: 1759906000000 左右

DELETE FROM sys_role
WHERE role_name LIKE '测试角色_%'
  AND CAST(SUBSTRING(role_name, 6) AS UNSIGNED) > 1759906000000;

DELETE FROM sys_dept
WHERE dept_name LIKE '测试部门_%'
  AND CAST(SUBSTRING(dept_name, 6) AS UNSIGNED) > 1759906000000;

DELETE FROM sys_menu
WHERE menu_name LIKE '测试菜单_%'
  AND CAST(SUBSTRING(menu_name, 6) AS UNSIGNED) > 1759906000000;

DELETE FROM sys_user
WHERE user_name LIKE 'test_%'
  AND CAST(SUBSTRING(user_name, 6) AS UNSIGNED) > 1759906000000;
```

---

## 🔧 自动清理脚本

创建清理脚本文件 `cleanup_test_data.sql`:

```sql
-- ============================================
-- 集成测试数据清理脚本
-- 使用方法: mysql -u root -p database_name < cleanup_test_data.sql
-- ============================================

USE ry_plus_new;

START TRANSACTION;

-- 1. 备份要删除的数据（可选）
CREATE TABLE IF NOT EXISTS test_data_backup_role AS
SELECT * FROM sys_role WHERE role_name LIKE '测试角色_%' OR role_key LIKE 'test_role_%';

CREATE TABLE IF NOT EXISTS test_data_backup_dept AS
SELECT * FROM sys_dept WHERE dept_name LIKE '测试部门_%';

CREATE TABLE IF NOT EXISTS test_data_backup_menu AS
SELECT * FROM sys_menu WHERE menu_name LIKE '测试菜单_%';

CREATE TABLE IF NOT EXISTS test_data_backup_user AS
SELECT * FROM sys_user WHERE user_name LIKE 'test_%';

-- 2. 清理测试数据
DELETE FROM sys_role_menu WHERE role_id IN (
    SELECT role_id FROM sys_role WHERE role_name LIKE '测试角色_%' OR role_key LIKE 'test_role_%'
);

DELETE FROM sys_role_dept WHERE role_id IN (
    SELECT role_id FROM sys_role WHERE role_name LIKE '测试角色_%' OR role_key LIKE 'test_role_%'
);

DELETE FROM sys_user_role WHERE role_id IN (
    SELECT role_id FROM sys_role WHERE role_name LIKE '测试角色_%' OR role_key LIKE 'test_role_%'
);

DELETE FROM sys_user_role WHERE user_id IN (
    SELECT user_id FROM sys_user WHERE user_name LIKE 'test_%'
);

DELETE FROM sys_user_post WHERE user_id IN (
    SELECT user_id FROM sys_user WHERE user_name LIKE 'test_%'
);

DELETE FROM sys_role WHERE role_name LIKE '测试角色_%' OR role_key LIKE 'test_role_%';
DELETE FROM sys_dept WHERE dept_name LIKE '测试部门_%';
DELETE FROM sys_menu WHERE menu_name LIKE '测试菜单_%';
DELETE FROM sys_user WHERE user_name LIKE 'test_%';

COMMIT;

-- 查看清理结果
SELECT '清理完成' AS status;
SELECT COUNT(*) AS remaining_test_roles FROM sys_role WHERE role_name LIKE '测试角色_%';
SELECT COUNT(*) AS remaining_test_depts FROM sys_dept WHERE dept_name LIKE '测试部门_%';
SELECT COUNT(*) AS remaining_test_menus FROM sys_menu WHERE menu_name LIKE '测试菜单_%';
SELECT COUNT(*) AS remaining_test_users FROM sys_user WHERE user_name LIKE 'test_%';
```

---

## 🎯 建议

### 开发环境
- **定期清理**: 每天或每周运行一次清理脚本
- **保留备份**: 使用带备份的清理脚本

### CI/CD环境
- **测试前清理**: 在测试开始前清理旧数据
- **使用独立数据库**: 为集成测试配置专用数据库
- **测试后清理**: 在测试完成后自动清理

### 生产环境
- **禁止运行集成测试**: 集成测试仅在开发/测试环境运行

---

## 📝 快速清理命令

### MySQL 命令行
```bash
mysql -u root -p ry_plus_new -e "
DELETE FROM sys_role WHERE role_name LIKE '测试角色_%';
DELETE FROM sys_role WHERE role_key LIKE 'test_role_%';
DELETE FROM sys_dept WHERE dept_name LIKE '测试部门_%';
DELETE FROM sys_menu WHERE menu_name LIKE '测试菜单_%';
DELETE FROM sys_user WHERE user_name LIKE 'test_%';
"
```

### Windows 批处理
创建 `cleanup.bat`:
```bat
@echo off
mysql -u root -p ry_plus_new < script/cleanup_test_data.sql
pause
```

---

*最后更新: 2025-10-08*
*说明: 集成测试数据清理指南*
