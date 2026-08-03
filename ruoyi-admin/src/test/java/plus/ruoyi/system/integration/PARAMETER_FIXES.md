# 集成测试参数修复记录

## 修复日期
2025-10-08

## 修复原因
后端 Controller 使用 `@RequestBody` 接收完整对象，但测试代码中的 API 客户端定义使用了 `@Query` 参数或错误的数据类型。

---

## 📋 修复清单

### 1. SystemApiClient 接口定义修复

#### 1.1 resetUserPwd - 重置用户密码

**修改前:**
```java
@Put("/system/user/resetUserPwd")
ForestResponse<R<Void>> resetUserPwd(
    @Header("Authorization") String token,
    @Query("userId") Long userId,
    @Query("password") String password
);
```

**修改后:**
```java
@Put("/system/user/resetUserPwd")
ForestResponse<R<Void>> resetUserPwd(
    @Header("Authorization") String token,
    @Body SysUserBo user
);
```

**原因:** 后端 Controller 方法签名为 `resetUserPwd(@RequestBody SysUserBo user)`

---

#### 1.2 changeUserStatus - 修改用户状态

**修改前:**
```java
@Put("/system/user/changeUserStatus")
ForestResponse<R<Void>> changeUserStatus(
    @Header("Authorization") String token,
    @Query("userId") Long userId,
    @Query("status") String status
);
```

**修改后:**
```java
@Put("/system/user/changeUserStatus")
ForestResponse<R<Void>> changeUserStatus(
    @Header("Authorization") String token,
    @Body SysUserBo user
);
```

**原因:** 后端 Controller 方法签名为 `changeUserStatus(@RequestBody SysUserBo user)`

---

#### 1.3 changeRoleStatus - 修改角色状态

**修改前:**
```java
@Put("/system/role/changeRoleStatus")
ForestResponse<R<Void>> changeRoleStatus(
    @Header("Authorization") String token,
    @Query("roleId") Long roleId,
    @Query("status") String status
);
```

**修改后:**
```java
@Put("/system/role/changeRoleStatus")
ForestResponse<R<Void>> changeRoleStatus(
    @Header("Authorization") String token,
    @Body SysRoleBo role
);
```

**原因:** 后端 Controller 方法签名为 `changeRoleStatus(@RequestBody SysRoleBo role)`

**额外修改:** 添加导入 `import plus.ruoyi.system.core.domain.bo.SysRoleBo;`

---

### 2. 测试代码修复

#### 2.1 SysUserIntegrationTest.java

##### testResetUserPwd

**修改前:**
```java
ForestResponse<R<Void>> response =
    apiClient.resetUserPwd(token, testUserId, "newPassword123");
```

**修改后:**
```java
SysUserBo user = new SysUserBo();
user.setUserId(testUserId);
user.setPassword("newPassword123");

ForestResponse<R<Void>> response = apiClient.resetUserPwd(token, user);
```

---

##### testChangeUserStatus

**修改前:**
```java
ForestResponse<R<Void>> response =
    apiClient.changeUserStatus(token, testUserId, "1");
```

**修改后:**
```java
SysUserBo user = new SysUserBo();
user.setUserId(testUserId);
user.setStatus("1");

ForestResponse<R<Void>> response = apiClient.changeUserStatus(token, user);
```

---

#### 2.2 SysRoleIntegrationTest.java

##### testChangeRoleStatus

**修改前:**
```java
ForestResponse<R<Void>> response =
    apiClient.changeRoleStatus(token, roleId, "1");
```

**修改后:**
```java
SysRoleBo roleBo = new SysRoleBo();
roleBo.setRoleId(roleId);
roleBo.setRoleKey("pc_common"); // 必需字段,避免checkRoleAllowed时NPE
roleBo.setStatus("1");

ForestResponse<R<Void>> response = apiClient.changeRoleStatus(token, roleBo);
```

**特别注意:** 必须提供 `roleKey` 字段，否则在 `SysRoleServiceImpl.checkRoleAllowed()` 方法中会抛出 `NullPointerException`

---

#### 2.3 SysDeptIntegrationTest.java

##### testAddDept

**修改前:**
```java
dept.put("leader", "测试负责人"); // 错误: 字符串
```

**修改后:**
```java
dept.put("leader", 1L); // 正确: 用户ID (Long类型)
```

**原因:** `SysDeptBo.leader` 字段类型为 `Long`（用户ID），不是字符串

---

##### testUpdateDept

**修改前:**
```java
dept.put("leader", "若依"); // 错误: 字符串
```

**修改后:**
```java
dept.put("leader", 1L); // 正确: 用户ID (Long类型)
```

---

## 🔍 根本原因分析

### 问题1: @Query vs @Body
- **错误原因:** 接口定义使用 `@Query` 参数，但后端期望 `@RequestBody`
- **错误现象:** `Required request body is missing`
- **解决方案:** 修改为 `@Body` 并传递完整的 BO 对象

### 问题2: 字段类型不匹配
- **错误原因:** 将字符串值传递给 `Long` 类型字段
- **错误现象:** `Cannot deserialize value of type java.lang.Long from String "xxx"`
- **解决方案:** 使用正确的数据类型（用户ID使用 `Long` 而非姓名字符串）

### 问题3: 必需字段缺失导致NPE
- **错误原因:** `changeRoleStatus` 时未提供 `roleKey` 字段
- **错误现象:** `NullPointerException` 在 `checkRoleAllowed` 方法中
- **解决方案:** 补充必需的 `roleKey` 字段

---

## ✅ 验证要点

运行测试前需要确保：

1. **编译项目:** `mvn clean compile` 确保使用最新代码
2. **检查字段类型:**
   - `SysDeptBo.leader` → `Long` (用户ID)
   - `SysUserBo.userId` → `Long`
   - `SysRoleBo.roleId` → `Long`
3. **检查必需字段:**
   - 修改角色状态时必须提供 `roleKey`
   - 修改用户状态时必须提供 `userId`

---

## 📊 影响范围

| 测试类 | 修改方法数 | 说明 |
|--------|-----------|------|
| SystemApiClient | 3 | 接口定义修改 |
| SysUserIntegrationTest | 2 | 参数改为对象 |
| SysRoleIntegrationTest | 1 | 参数改为对象 + 补充roleKey |
| SysDeptIntegrationTest | 2 | leader字段改为Long类型 |

---

## ⚠️ 重要发现：状态值定义

在修复过程中发现了一个关键的**状态值定义问题**：

### DictEnableStatus 枚举定义

```java
public enum DictEnableStatus {
    ENABLE("1", "启用"),    // "1" = 启用
    DISABLED("0", "禁用")   // "0" = 禁用
}
```

**这与直觉相反！** 通常我们认为 0=禁用，1=启用，但在 RuoYi-Plus 框架中是相反的。

### 影响范围

所有使用 `status` 字段的测试都需要修正：

| 测试类 | 测试方法 | 修改 |
|--------|---------|------|
| SysUserIntegrationTest | testInsertUser | status: "0" → "1" |
| SysUserIntegrationTest | testChangeUserStatus | 先启用("1")，再禁用("0") |
| SysRoleIntegrationTest | testAddRole | status: "0" → "1" |
| SysRoleIntegrationTest | testChangeRoleStatus | 先启用("1")，再禁用("0") |
| SysMenuIntegrationTest | testAddMenu | status: "0" → "1" |
| SysMenuIntegrationTest | testUpdateMenu | status: "0" → "1" |
| SysDeptIntegrationTest | testAddDept | status: "0" → "1" |
| SysDeptIntegrationTest | testUpdateDept | status: "0" → "1" |

### 业务逻辑限制

**已分配的角色不能禁用**:
```java
// SysRoleServiceImpl.updateRoleStatus
if (DictEnableStatus.DISABLED.getValue().equals(status) && this.countUsersByRoleId(roleId) > 0) {
    throw ServiceException.of("角色已分配，不能禁用!");
}
```

**包含未停用子部门的部门不能禁用**:
```sql
SELECT COUNT(*) FROM sys_dept
WHERE status = '1' AND find_in_set('101', ancestors) <> 0
```

因此测试中应该保持实体为**启用状态**（status="1"），避免触发业务限制。

---

*最后更新: 2025-10-08*
*修复人员: Claude Code*
