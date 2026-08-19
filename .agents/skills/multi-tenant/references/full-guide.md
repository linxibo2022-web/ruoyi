
# 多租户开发指南

## 概述

本项目基于 `ruoyi-common-tenant` 模块实现了**四层租户隔离**：数据库（MyBatis-Plus 拦截器）、Redis 缓存（键前缀）、Spring Cache（缓存名前缀）、SaToken 认证（会话隔离）。

核心设计理念：**底层隔离始终启用**，`tenant.enable` 仅控制业务功能（租户管理界面），不影响数据隔离。业务表只需继承 `TenantEntity` 即可自动获得多租户能力。

---

## 核心类速查

| 类 | 位置 | 用途 |
|----|------|------|
| `TenantEntity` | `ruoyi-common-tenant` | 多租户实体基类（继承它即可） |
| `BaseEntity` | `ruoyi-common-mybatis` | 基础实体（审计字段） |
| `TenantHelper` | `ruoyi-common-tenant` | 租户操作工具类（忽略/切换/获取） |
| `TenantConstants` | `ruoyi-common-core` | 租户常量（DEFAULT_TENANT_ID 等） |
| `TenantProperties` | `ruoyi-common-tenant` | 租户配置属性 |
| `PlusTenantLineHandler` | `ruoyi-common-tenant` | MyBatis SQL 拦截处理器 |
| `TenantKeyPrefixHandler` | `ruoyi-common-tenant` | Redis 键前缀处理器 |
| `TenantSpringCacheManager` | `ruoyi-common-tenant` | Spring Cache 租户管理器 |
| `TenantException` | `ruoyi-common-tenant` | 租户异常类 |

---

## 四层隔离架构

```
┌─────────────────────────────────────────────────────────┐
│                    请求入口                                │
│      租户ID来源：动态租户 > 登录用户 > 请求头/域名          │
└────────────────────────┬────────────────────────────────┘
                         │
    ┌────────────────────┼────────────────────┐
    │                    │                    │
    ▼                    ▼                    ▼
┌──────────┐     ┌──────────────┐     ┌──────────────┐
│ 数据库层  │     │  Redis 层     │     │ SaToken 层   │
│ MyBatis  │     │  键前缀隔离   │     │  会话隔离     │
│ 拦截器    │     │              │     │              │
│          │     │ {tenantId}:  │     │ 全局前缀      │
│ WHERE    │     │  key         │     │              │
│ tenant_id│     │              │     │              │
│ = 'xxx'  │     ├──────────────┤     └──────────────┘
└──────────┘     │ Spring Cache │
                 │ {tenantId}:  │
                 │ cacheName    │
                 └──────────────┘
```

### 租户ID获取优先级

```
TenantHelper.getTenantId() 优先级：
1. 动态租户（线程本地 ThreadLocal / Redis 全局）
2. 登录用户租户（LoginHelper.getTenantId()）
3. 请求租户（域名识别 > 请求头 X-Tenant-Id）
4. 默认租户（"000000"）
```

---

## 使用规范

### 1. 业务实体继承 TenantEntity

所有需要租户隔离的业务表，Entity 类必须继承 `TenantEntity`：

```java
import plus.ruoyi.common.tenant.core.TenantEntity;

/**
 * 广告配置
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("b_ad")
public class Ad extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    /** 广告名称 */
    private String adName;

    /** 状态 */
    private String status;

    // tenantId 字段自动继承，无需声明
    // createBy、createTime、updateBy、updateTime 等审计字段也自动继承
}
```

**继承链**：`Ad → TenantEntity → BaseEntity`

| 来自 | 字段 |
|------|------|
| `BaseEntity` | `createDept`, `createBy`, `createTime`, `updateBy`, `updateTime`, `searchValue`, `params` |
| `TenantEntity` | `tenantId` |
| 业务类自身 | `id`, `adName`, `status` 等业务字段 |

### 2. 建表必须包含 tenant_id 字段

```sql
CREATE TABLE b_ad (
    id           BIGINT(20)   NOT NULL COMMENT '主键ID',
    tenant_id    VARCHAR(20)  DEFAULT '000000' COMMENT '租户ID',  -- 必须

    -- 业务字段
    ad_name      VARCHAR(100) NOT NULL COMMENT '广告名称',
    status       CHAR(1)      DEFAULT '1' COMMENT '状态',

    -- 审计字段
    create_dept  BIGINT(20)   DEFAULT NULL COMMENT '创建部门',
    create_by    BIGINT(20)   DEFAULT NULL COMMENT '创建人',
    create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by    BIGINT(20)   DEFAULT NULL COMMENT '更新人',
    update_time  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark       VARCHAR(255) DEFAULT NULL COMMENT '备注',
    is_deleted   CHAR(1)      DEFAULT '0' COMMENT '是否删除',

    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='广告配置表';

-- 必须创建租户索引
CREATE INDEX idx_tenant_id ON b_ad(tenant_id);
```

### 3. 不需要租户隔离的表

某些全局共享的表不需要租户隔离：

**方式一：配置排除表**（推荐）

```yaml
# application.yml
tenant:
  enable: true
  excludes:
    - b_global_config    # 全局配置表，所有租户共享
```

**方式二：Entity 继承 BaseEntity**（不继承 TenantEntity）

```java
// 全局共享的表，继承 BaseEntity 而非 TenantEntity
@TableName("sys_tenant")
public class SysTenant extends BaseEntity {
    // 租户管理表本身不需要租户隔离
}
```

**系统硬编码排除表**（无需配置，始终排除）：

| 表名 | 说明 |
|------|------|
| `sys_tenant` | 租户表本身 |
| `sys_tenant_package` | 租户套餐表 |
| `sys_gen_table` | 代码生成表 |
| `sys_gen_table_column` | 代码生成列表 |
| `sys_menu` | 菜单表 |
| `sys_role_menu` | 角色菜单关联 |
| `sys_role_dept` | 角色部门关联 |
| `sys_user_role` | 用户角色关联 |
| `sys_user_post` | 用户岗位关联 |
| `sys_oss_config` | OSS 配置表 |

---

## TenantHelper 核心 API

### 忽略租户过滤

当需要查询**所有租户**的数据时（如统计、超管操作）：

```java
// 无返回值
TenantHelper.ignore(() -> {
    // 这里的 SQL 不会添加 tenant_id 条件
    List<SysUser> allUsers = userDao.listAll();
    log.info("全部用户数: {}", allUsers.size());
});

// 有返回值
List<SysUser> allUsers = TenantHelper.ignore(() -> {
    return userDao.listAll();
});
```

### 动态切换租户

当需要临时以**其他租户身份**操作时（如超管代操作、数据同步）：

```java
// 方式一：代码块（推荐，自动清理）
TenantHelper.dynamic("123456", () -> {
    // 这里的所有操作都在租户 123456 下执行
    List<SysUser> users = userDao.listAll();
    orderService.createOrder(orderBo);
});

// 方式二：有返回值
SysUserVo user = TenantHelper.dynamic("123456", () -> {
    return userService.getUserById(userId);
});

// 方式三：手动设置/清除（需自行管理生命周期）
try {
    TenantHelper.setDynamic("123456");
    // 操作...
} finally {
    TenantHelper.clearDynamic();  // 必须清除！
}

// 方式四：全局动态租户（存储到 Redis，跨请求生效）
TenantHelper.setDynamic("123456", true);  // true = 存 Redis
// 后续所有请求都使用租户 123456，直到清除
TenantHelper.clearDynamic();
```

### 获取当前租户

```java
// 获取当前有效租户ID
String tenantId = TenantHelper.getTenantId();

// 检查多租户功能是否启用
boolean enabled = TenantHelper.isEnable();
```

### 常量定义

```java
import plus.ruoyi.common.core.constant.TenantConstants;

// 默认租户ID
String defaultId = TenantConstants.DEFAULT_TENANT_ID;  // "000000"

// 超管角色
String superAdmin = TenantConstants.SUPER_ADMIN_ROLE_KEY;  // "superadmin"

// 租户管理员角色
String tenantAdmin = TenantConstants.TENANT_ADMIN_ROLE_KEY;  // "admin"
```

---

## 配置参考

### application.yml

```yaml
tenant:
  # 是否开启多租户业务功能（底层隔离始终生效）
  enable: true

  # 全局排除表（所有数据源生效）
  excludes:
    # - b_global_config  # 全局共享的业务表

  # 数据源级别排除表（累加模式，在全局基础上追加）
  # datasource-excludes:
  #   master:
  #     - some_table
  #   slave:
  #     - other_table
```

### 配置说明

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `tenant.enable` | Boolean | true | 控制租户管理界面，**不影响数据隔离** |
| `tenant.excludes` | List | 空 | 全局排除表（不添加 tenant_id 条件） |
| `tenant.datasource-excludes` | Map | 空 | 数据源级别排除表（累加模式） |

---

## 租户管理模块

### 数据库表

**sys_tenant**（租户表）：

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT | 主键 |
| `tenant_id` | VARCHAR(20) | 租户编号（6位随机） |
| `contact_user_name` | VARCHAR(20) | 联系人 |
| `contact_phone` | VARCHAR(20) | 联系电话 |
| `company_name` | VARCHAR(30) | 企业名称 |
| `license_number` | VARCHAR(30) | 统一社会信用代码 |
| `address` | VARCHAR(200) | 地址 |
| `domain` | VARCHAR(200) | 域名（用于域名识别租户） |
| `package_id` | BIGINT | 租户套餐ID |
| `expire_time` | DATETIME | 过期时间 |
| `account_count` | INT | 用户数量限制（-1不限制） |
| `status` | CHAR(1) | 状态（0停用 1正常） |

**sys_tenant_package**（租户套餐表）：

| 字段 | 类型 | 说明 |
|------|------|------|
| `package_id` | BIGINT | 套餐ID |
| `package_name` | VARCHAR(20) | 套餐名称 |
| `menu_ids` | VARCHAR(3000) | 关联菜单ID |
| `status` | CHAR(1) | 状态 |

### 创建租户流程

```
新增租户 → 生成6位随机 tenant_id
         → 创建租户记录（sys_tenant）
         → 根据套餐创建管理员角色
         → 同步默认租户的普通业务角色
         → 创建默认部门
         → 创建管理员用户
         → 复制默认租户的字典和配置
```

### 管理 API

| 操作 | 方法 | 路径 |
|------|------|------|
| 分页查询 | GET | `/system/tenant/pageTenants` |
| 获取详情 | GET | `/system/tenant/getTenant/{id}` |
| 新增租户 | POST | `/system/tenant/addTenant` |
| 修改租户 | PUT | `/system/tenant/updateTenant` |
| 删除租户 | DELETE | `/system/tenant/deleteTenants/{ids}` |
| 动态切换 | GET | `/system/tenant/setDynamicTenant/{tenantId}` |
| 清除动态 | GET | `/system/tenant/clearDynamicTenant` |
| 同步套餐 | GET | `/system/tenant/syncTenantPackage` |
| 同步角色 | GET | `/system/tenant/syncTenantRoles` |
| 同步字典 | GET | `/system/tenant/syncTenantDicts` |

---

## 实战示例

### 示例 1：新建租户隔离的业务模块

**步骤**：Entity 继承 `TenantEntity` + 建表包含 `tenant_id` → 自动隔离

```java
// 1. Entity
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("m_order")
public class Order extends TenantEntity {
    @TableId(value = "id")
    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal totalAmount;
    private String status;
}

// 2. DAO 查询 - tenant_id 条件自动添加，无需手动处理
@Component
public class OrderDaoImpl extends BaseDaoImpl<OrderMapper, Order> implements IOrderDao {
    @Override
    public LambdaQueryWrapper<Order> buildQueryWrapper(OrderBo bo) {
        return new PlusLambdaQuery<Order>()
            .like(StringUtils.isNotBlank(bo.getOrderNo()), Order::getOrderNo, bo.getOrderNo())
            .eq(StringUtils.isNotBlank(bo.getStatus()), Order::getStatus, bo.getStatus());
        // 不需要手动添加 .eq(Order::getTenantId, tenantId)
        // MyBatis 拦截器会自动添加 WHERE tenant_id = 'xxx'
    }
}
```

### 示例 2：超管查看所有租户数据

```java
@Service
public class StatisticsServiceImpl implements IStatisticsService {

    @Override
    public DashboardVo getDashboard() {
        // 统计所有租户的用户总数
        long totalUsers = TenantHelper.ignore(() -> {
            return userDao.count();
        });

        // 统计所有租户的订单总额
        BigDecimal totalAmount = TenantHelper.ignore(() -> {
            return orderDao.sumTotalAmount();
        });

        return new DashboardVo(totalUsers, totalAmount);
    }
}
```

### 示例 3：跨租户数据同步

```java
@Service
public class DataSyncServiceImpl implements IDataSyncService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncConfigToAllTenants(SysConfigBo configBo) {
        // 获取所有租户列表
        List<SysTenantVo> tenants = TenantHelper.ignore(() -> {
            return tenantDao.listAll();
        });

        // 逐个租户同步配置
        for (SysTenantVo tenant : tenants) {
            TenantHelper.dynamic(tenant.getTenantId(), () -> {
                configService.saveOrUpdate(configBo);
            });
        }
    }
}
```

### 示例 4：定时任务中处理多租户数据

```java
@Component
public class OrderTimeoutJob {

    @Scheduled(cron = "0 */5 * * * ?")
    public void cancelTimeoutOrders() {
        // 获取所有活跃租户
        List<SysTenantVo> tenants = TenantHelper.ignore(() -> {
            return tenantDao.listActiveTenants();
        });

        for (SysTenantVo tenant : tenants) {
            TenantHelper.dynamic(tenant.getTenantId(), () -> {
                orderService.cancelTimeoutOrders();
            });
        }
    }
}
```

### 示例 5：配置数据源级别排除表

```yaml
# 场景：slave 数据源中的 third_party_data 表不需要租户隔离
tenant:
  enable: true
  excludes:
    - b_global_config        # 全局共享
  datasource-excludes:
    slave:
      - third_party_data     # 仅 slave 数据源排除
```

---

## 常见错误与最佳实践

### 错误 1：业务 Entity 继承 BaseEntity 而非 TenantEntity

```java
// ❌ 错误：继承 BaseEntity，没有租户隔离
@TableName("b_ad")
public class Ad extends BaseEntity {
    private Long id;
    private String adName;
}

// ✅ 正确：继承 TenantEntity，自动租户隔离
@TableName("b_ad")
public class Ad extends TenantEntity {
    private Long id;
    private String adName;
}
```

### 错误 2：手动在查询中添加 tenant_id 条件

```java
// ❌ 错误：手动添加（多余，且可能与拦截器冲突）
LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(Order::getTenantId, TenantHelper.getTenantId());
wrapper.eq(Order::getStatus, "1");

// ✅ 正确：只写业务条件，tenant_id 由拦截器自动添加
LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(Order::getStatus, "1");
```

### 错误 3：忽略租户后不清理

```java
// ❌ 错误：手动 setDynamic 没有 finally 清理
TenantHelper.setDynamic("123456");
userDao.listAll();  // 如果这里抛异常，动态租户永远不会清除！

// ✅ 正确方式一：使用 Lambda（自动清理）
TenantHelper.dynamic("123456", () -> {
    userDao.listAll();
});

// ✅ 正确方式二：手动清理必须在 finally 中
try {
    TenantHelper.setDynamic("123456");
    userDao.listAll();
} finally {
    TenantHelper.clearDynamic();
}
```

### 错误 4：建表忘记 tenant_id 字段

```sql
-- ❌ 错误：缺少 tenant_id
CREATE TABLE b_ad (
    id       BIGINT(20) NOT NULL,
    ad_name  VARCHAR(100) NOT NULL,
    PRIMARY KEY (id)
);

-- ✅ 正确：必须包含 tenant_id 和索引
CREATE TABLE b_ad (
    id           BIGINT(20)   NOT NULL,
    tenant_id    VARCHAR(20)  DEFAULT '000000' COMMENT '租户ID',
    ad_name      VARCHAR(100) NOT NULL,
    PRIMARY KEY (id)
);
CREATE INDEX idx_tenant_id ON b_ad(tenant_id);
```

### 错误 5：认为 tenant.enable=false 就没有隔离

```yaml
# ⚠️ 注意：tenant.enable 仅控制业务功能（管理界面）
# 底层四层隔离始终生效！
tenant:
  enable: false  # 租户管理界面关闭，但 SQL 仍会添加 tenant_id 条件
```

### 错误 6：在全局共享表上使用 TenantEntity

```java
// ❌ 错误：sys_tenant 表不应该有租户隔离
@TableName("sys_tenant")
public class SysTenant extends TenantEntity { }

// ✅ 正确：继承 BaseEntity
@TableName("sys_tenant")
public class SysTenant extends BaseEntity { }
```

---

## 与 data-permission 的关系

| 维度 | multi-tenant（本技能） | data-permission |
|------|----------------------|-----------------|
| **隔离级别** | 租户级（不同企业/组织） | 行级（同租户内部门/个人） |
| **实现方式** | MyBatis TenantLineInnerInterceptor | @DataPermission 注解 |
| **字段** | `tenant_id` | `create_dept`, `create_by` 等 |
| **自动程度** | 完全自动（继承 TenantEntity 即可） | 需要手动添加 @DataPermission 注解 |
| **作用范围** | 所有 SQL（除排除表） | 仅标注注解的方法 |
| **典型场景** | SaaS 多企业隔离 | 企业内部门/个人数据权限 |

两者可以同时使用：先按 `tenant_id` 隔离到租户，再按 `@DataPermission` 在租户内按部门/个人过滤。

---

## 开发检查清单

### 新建业务表

- [ ] Entity 继承 `TenantEntity`（非 BaseEntity）
- [ ] 建表 SQL 包含 `tenant_id VARCHAR(20) DEFAULT '000000'`
- [ ] 建表 SQL 包含 `CREATE INDEX idx_tenant_id ON {table}(tenant_id)`
- [ ] DAO 查询中**不要**手动添加 tenant_id 条件
- [ ] 所有 4 个数据库脚本（MySQL/Oracle/PG/SQLServer）都已同步

### 使用 TenantHelper

- [ ] `ignore()` 和 `dynamic()` 优先使用 Lambda 版本（自动清理）
- [ ] 手动 `setDynamic()` 必须在 `finally` 中 `clearDynamic()`
- [ ] 定时任务需遍历所有租户并用 `dynamic()` 切换
- [ ] 跨租户查询使用 `ignore()` 而非直接修改 SQL

### 配置排除表

- [ ] 全局共享表加入 `tenant.excludes`
- [ ] 特定数据源排除表加入 `tenant.datasource-excludes`
- [ ] Entity 使用 BaseEntity（非 TenantEntity）

---

## 常见问题

### Q1: 查出的数据不包含其他租户的记录？

**A:** 这是正常的！MyBatis 拦截器自动添加了 `WHERE tenant_id = 'xxx'`。如果需要查全量数据，使用 `TenantHelper.ignore()`。

### Q2: 新建租户时初始化了哪些数据？

**A:** `SysTenantServiceImpl.insertTenant()` 会自动：
- 生成 6 位随机 `tenant_id`
- 根据套餐创建管理员角色
- 同步默认租户的普通业务角色
- 创建默认部门
- 创建系统管理员用户
- 复制默认租户的字典和配置

### Q3: 定时任务如何处理多租户？

**A:** 定时任务运行时没有登录用户上下文，需要手动遍历租户：

```java
List<SysTenantVo> tenants = TenantHelper.ignore(() -> tenantDao.listActiveTenants());
for (SysTenantVo tenant : tenants) {
    TenantHelper.dynamic(tenant.getTenantId(), () -> {
        // 在每个租户下执行业务逻辑
    });
}
```

### Q4: Redis 缓存是否自动隔离？

**A:** 是的。`TenantKeyPrefixHandler` 自动为所有 Redis 键添加 `{tenantId}:` 前缀。Spring Cache 的 `@Cacheable` 等注解也自动隔离。

### Q5: 如何判断当前租户是默认租户？

```java
String tenantId = TenantHelper.getTenantId();
boolean isDefault = TenantConstants.DEFAULT_TENANT_ID.equals(tenantId);
```

### Q6: 域名如何映射到租户？

**A:** `SysTenantServiceImpl.getTenantIdByRequest()` 按以下优先级获取：
1. 根据请求域名匹配 `sys_tenant.domain` 字段
2. 读取请求头 `X-Tenant-Id`

---

## 参考代码位置

| 内容 | 路径 |
|------|------|
| 多租户模块 | `ruoyi-common/ruoyi-common-tenant/` |
| TenantEntity | `ruoyi-common-tenant/.../core/TenantEntity.java` |
| TenantHelper | `ruoyi-common-tenant/.../helper/TenantHelper.java` |
| 租户配置 | `ruoyi-admin/src/main/resources/application.yml`（tenant 段） |
| 租户管理 | `ruoyi-modules/ruoyi-system/.../tenant/` |
| 租户表 SQL | `script/sql/ry_plus_sys.sql`（搜索 sys_tenant） |
| 业务参考 | `ruoyi-modules/ruoyi-business/.../base/domain/Ad.java`（继承示例） |
