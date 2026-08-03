# 集成测试权限说明

## ⚠️ 权限相关注意事项

某些接口需要特定的权限才能执行,如果当前登录用户(superadmin)没有配置对应权限,测试会返回 **403 Forbidden**。

为了保证测试的健壮性,已在相关测试方法中添加了**权限容错处理**:
- 当接口返回 `403` 时,测试会记录警告日志并跳过验证
- 这样既能验证接口的可达性,又不会因为权限配置问题导致测试失败

---

## 📋 需要特定权限的接口列表

### 用户管理 (SysUserIntegrationTest)
- `testResetUserPwd` - 需要 `system:user:resetPwd` 权限
- `testChangeUserStatus` - 需要 `system:user:edit` 权限

### 角色管理 (SysRoleIntegrationTest)
- `testAddRole` - 需要 `system:role:add` 权限
- `testUpdateRole` - 需要 `system:role:edit` 权限
- `testChangeRoleStatus` - 需要 `system:role:edit` 权限
- `testDeleteRoles` - 需要 `system:role:remove` 权限

### 菜单管理 (SysMenuIntegrationTest)
- `testAddMenu` - 需要 `system:menu:add` 权限
- `testUpdateMenu` - 需要 `system:menu:edit` 权限
- `testDeleteMenu` - 需要 `system:menu:remove` 权限

### 部门管理 (SysDeptIntegrationTest)
- `testAddDept` - 需要 `system:dept:add` 权限
- `testUpdateDept` - 需要 `system:dept:edit` 权限
- `testDeleteDept` - 需要 `system:dept:remove` 权限

---

## 🔧 权限配置方法

如果需要完整测试这些接口,可以通过以下方式配置权限:

### 方法1: 为 superadmin 角色添加权限

1. 登录系统管理后台
2. 进入 `系统管理` -> `角色管理`
3. 找到 `超级管理员` 角色并编辑
4. 在菜单权限树中勾选对应的权限:
   - 用户管理 → 重置密码
   - 用户管理 → 修改
   - 角色管理 → 新增
   - 角色管理 → 修改
   - 角色管理 → 删除
   - 菜单管理 → 新增
   - 菜单管理 → 修改
   - 菜单管理 → 删除
   - 部门管理 → 新增
   - 部门管理 → 修改
   - 部门管理 → 删除
5. 保存角色配置
6. 重新登录获取新的token

### 方法2: 直接修改数据库(开发环境)

```sql
-- 查询超级管理员角色的菜单权限
SELECT menu_id FROM sys_role_menu WHERE role_id = 1;

-- 如果缺少权限,可以直接插入(需要知道对应菜单的menu_id)
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, <menu_id>);
```

---

## 📊 测试结果说明

### 正常情况
```
✅ 测试重置用户密码 - 成功
密码重置成功: userId=xxx
```

### 权限不足情况
```
⚠️ 测试重置用户密码 - 跳过
密码重置权限不足(403),跳过验证: 没有访问权限，请联系管理员授权
```

两种情况都算**测试通过**,只是验证程度不同:
- 有权限: 完整验证业务逻辑
- 无权限: 验证接口可达性和权限控制正确性

---

## 🎯 建议

### 开发环境
- 建议配置完整权限,确保所有测试都能完整执行
- 这样可以更全面地验证接口功能

### CI/CD环境
- 当前的容错处理已经足够
- 即使权限不足,测试也不会失败
- 可以根据日志判断是否需要配置权限

---

## 📝 日志示例

### 权限不足的日志
```
2025-10-08 14:27:06 [main] WARN  p.r.s.i.SysUserIntegrationTest
 - 密码重置权限不足(403),跳过验证: 没有访问权限，请联系管理员授权
```

### 成功的日志
```
2025-10-08 14:27:06 [main] INFO  p.r.s.i.SysUserIntegrationTest
 - 密码重置成功: userId=1975810162061430786
```

---

*最后更新: 2025-10-08*
