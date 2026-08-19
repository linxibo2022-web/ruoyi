
# 数据库操作指南

## 🔴 核心规则：按配置生成 SQL

> **强制**：生成 SQL 前先读 `application-dev.yml`，看 `spring.datasource.dynamic.datasource` 下哪个 `url:` 未被 `#` 注释，**只生成该数据库类型的 SQL**。除非用户明确要求生成多种数据库的 SQL。

---

## 1. 数据库连接

### 1.1 从配置文件读取连接信息（必须！）

> **重要**: 连接数据库前必须先读取配置文件，获取实际的数据库类型和连接信息！

```bash
# 首先读取配置文件
Read ruoyi-admin/src/main/resources/application-dev.yml
```

### 1.2 支持的数据库类型

本项目支持 **4 种数据库**：

| 数据库 | 配置节点 | 默认端口 | 驱动类 |
|--------|----------|----------|--------|
| **MySQL** | `master` | 3306 | `com.mysql.cj.jdbc.Driver` |
| **Oracle** | `oracle` | 1521 | `oracle.jdbc.OracleDriver` |
| **PostgreSQL** | `postgres` | 5432 | `org.postgresql.Driver` |
| **SQL Server** | `sqlserver` | 1433 | `com.microsoft.sqlserver.jdbc.SQLServerDriver` |

### 1.3 配置格式解析

```yaml
spring:
  datasource:
    dynamic:
      datasource:
        # MySQL 配置
        master:
          url: jdbc:mysql://${DB_HOST:127.0.0.1}:${DB_PORT:3306}/${DB_NAME:数据库名}?...
          username: ${DB_USERNAME:root}
          password: ${DB_PASSWORD:root}

        # Oracle 配置（如启用）
        oracle:
          url: jdbc:oracle:thin:@//${ORACLE_DB_HOST:127.0.0.1}:${ORACLE_DB_PORT:1521}/${ORACLE_DB_NAME:XE}
          username: ${ORACLE_DB_USERNAME:ROOT}
          password: ${ORACLE_DB_PASSWORD:root}

        # PostgreSQL 配置（如启用）
        postgres:
          url: jdbc:postgresql://${POSTGRES_DB_HOST:127.0.0.1}:${POSTGRES_DB_PORT:5432}/${POSTGRES_DB_NAME:postgres}
          username: ${POSTGRES_DB_USERNAME:root}
          password: ${POSTGRES_DB_PASSWORD:root}

        # SQL Server 配置（如启用）
        sqlserver:
          url: jdbc:sqlserver://${SQLSERVER_DB_HOST:127.0.0.1}:${SQLSERVER_DB_PORT:1433};DatabaseName=${SQLSERVER_DB_NAME:master}
          username: ${SQLSERVER_DB_USERNAME:sa}
          password: ${SQLSERVER_DB_PASSWORD:root}
```

**解析规则**：`${环境变量:默认值}` - 优先使用环境变量，否则使用默认值

### 1.4 连接命令（根据实际数据库类型选择）

```bash
# MySQL
mysql -h [主机] -P [端口] -u [用户名] -p[密码] [数据库名]

# PostgreSQL
psql -h [主机] -p [端口] -U [用户名] -d [数据库名]

# SQL Server
sqlcmd -S [主机],[端口] -U [用户名] -P [密码] -d [数据库名]

# Oracle (SQL*Plus)
sqlplus [用户名]/[密码]@[主机]:[端口]/[服务名]
```

---

## 2. SQL 文件位置

> **重要**：项目支持 4 种数据库，SQL 文件按数据库类型分目录存放！

### 2.1 目录结构

```
script/sql/
├── ry_plus_sys.sql              # MySQL 系统表
├── ry_plus_app.sql              # MySQL 业务表
├── ry_plus_new.sql              # MySQL 新业务表
├── ry_plus_job.sql              # MySQL 任务表
│
├── oracle/                       # Oracle 数据库
│   ├── oracle_ry_plus_sys.sql
│   ├── oracle_ry_plus_app.sql
│   ├── oracle_ry_plus_new.sql
│   └── oracle_ry_plus_job.sql
│
├── postgres/                     # PostgreSQL 数据库
│   ├── postgres_ry_plus_sys.sql
│   ├── postgres_ry_plus_app.sql
│   ├── postgres_ry_plus_new.sql
│   └── postgres_ry_plus_job.sql
│
└── sqlserver/                    # SQL Server 数据库
    ├── sqlserver_ry_plus_sys.sql
    ├── sqlserver_ry_plus_app.sql
    ├── sqlserver_ry_plus_new.sql
    └── sqlserver_ry_plus_job.sql
```

### 2.2 SQL 文件说明

| SQL 类型 | MySQL | Oracle | PostgreSQL | SQL Server |
|----------|-------|--------|------------|------------|
| **系统表** | `ry_plus_sys.sql` | `oracle/oracle_ry_plus_sys.sql` | `postgres/postgres_ry_plus_sys.sql` | `sqlserver/sqlserver_ry_plus_sys.sql` |
| **业务表** | `ry_plus_app.sql` | `oracle/oracle_ry_plus_app.sql` | `postgres/postgres_ry_plus_app.sql` | `sqlserver/sqlserver_ry_plus_app.sql` |
| **新业务表** | `ry_plus_new.sql` | `oracle/oracle_ry_plus_new.sql` | `postgres/postgres_ry_plus_new.sql` | `sqlserver/sqlserver_ry_plus_new.sql` |
| **任务调度** | `ry_plus_job.sql` | `oracle/oracle_ry_plus_job.sql` | `postgres/postgres_ry_plus_job.sql` | `sqlserver/sqlserver_ry_plus_job.sql` |

---

## 3. 常用查询（MySQL 语法）

> **注意**: 以下为 MySQL 语法，其他数据库语法有差异

### 3.1 查看表结构

```sql
-- 查看所有表
SHOW TABLES;

-- 按前缀查看表
SHOW TABLES LIKE 'b_%';    -- base 模块
SHOW TABLES LIKE 'm_%';    -- mall 模块
SHOW TABLES LIKE 'sys_%';  -- 系统模块

-- 查看表结构
DESC b_ad;

-- 查看建表语句（推荐，可复制参考）
SHOW CREATE TABLE b_ad;
```

### 3.2 查询最大 ID

```sql
-- 菜单最大 ID
SELECT MAX(menu_id) FROM sys_menu;

-- 字典类型最大 ID
SELECT MAX(dict_id) FROM sys_dict_type;

-- 字典数据最大 ID
SELECT MAX(dict_data_id) FROM sys_dict_data;
```

### 3.3 查询菜单

```sql
-- 查询顶级菜单
SELECT menu_id, menu_name, order_num
FROM sys_menu
WHERE menu_type = 'M' AND parent_id = 0
ORDER BY order_num;

-- 查询某菜单下的子菜单
SELECT menu_id, menu_name, order_num
FROM sys_menu
WHERE parent_id = [父菜单ID]
ORDER BY order_num;

-- 查询上级菜单下最大顺序
SELECT MAX(order_num) FROM sys_menu WHERE parent_id = [上级菜单ID];
-- 新菜单顺序 = MAX + 10
```

### 3.4 查询字典

```sql
-- 查询所有字典类型
SELECT dict_id, dict_name, dict_type FROM sys_dict_type;

-- 查询某字典的数据项
SELECT dict_data_id, dict_label, dict_value
FROM sys_dict_data
WHERE dict_type = '[字典类型]'
ORDER BY dict_sort;
```

---

## 4. 表结构设计规范

### 4.1 表命名规范

| 模块 | 表前缀 | Java 包名 | 示例 |
|------|--------|-----------|------|
| **系统** | `sys_` | `plus.ruoyi.system` | `sys_user`, `sys_menu` |
| **base** | `b_` | `plus.ruoyi.business.base` | `b_ad`, `b_bind`, `b_platform` |
| **mall** | `m_` | `plus.ruoyi.business.mall` | `m_goods`, `m_order` |
| **iot** | `iot_` | `plus.ruoyi.business.iot` | `iot_device` |
| **crm** | `crm_` | `plus.ruoyi.business.crm` | `crm_customer` |

### 4.2 标准建表模板（MySQL，参考 b_ad 表）

> **重要**: 本项目使用 **雪花ID**（ASSIGN_ID），不是自增ID，不要用 `AUTO_INCREMENT`！
> 所有字段和表的 `comment` 必须使用中文。

```sql
-- 参考 script/sql/ry_plus_app.sql 中的 b_ad 表
create table [前缀]_[功能名]
(
    id           bigint(20)   not null comment '主键id',  -- 雪花ID，不用 AUTO_INCREMENT
    tenant_id    varchar(20)  default '000000' comment '租户id',

    -- 业务字段（根据需求添加）
    xxx_name     varchar(100) not null comment '名称',
    xxx_type     varchar(20)  default null comment '类型',
    xxx_img      varchar(500) default null comment '图片',
    xxx_content  text         default null comment '内容',
    xxx_amount   decimal(10,2) default 0 comment '金额',
    sort_order   int(4)       default 999 comment '排序值',
    status       char(1)      default '1' comment '状态(0停用 1正常)',

    -- 审计字段（必须包含）
    create_dept  bigint(20)   default null comment '创建部门',
    create_by    bigint(20)   default null comment '创建人',
    create_time  datetime     default current_timestamp comment '创建时间',
    update_by    bigint(20)   default null comment '更新人',
    update_time  datetime     default current_timestamp on update current_timestamp comment '更新时间',
    remark       varchar(255) default null comment '备注',
    is_deleted   char(1)      default '0' comment '是否删除(0正常 1已删除)',

    primary key (id)
) engine = innodb comment = '[功能名]表';
```

### 4.3 必需字段清单

| 字段 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `id` | `bigint(20)` | 雪花ID | 主键（MyBatis-Plus 自动生成） |
| `tenant_id` | `varchar(20)` | `'000000'` | 租户ID |
| `create_dept` | `bigint(20)` | NULL | 创建部门 |
| `create_by` | `bigint(20)` | NULL | 创建人 |
| `create_time` | `datetime` | CURRENT_TIMESTAMP | 创建时间 |
| `update_by` | `bigint(20)` | NULL | 更新人 |
| `update_time` | `datetime` | 自动更新 | 更新时间 |
| `remark` | `varchar(255)` | NULL | 备注 |
| `is_deleted` | `char(1)` | `'0'` | 逻辑删除 |

> **注意**：本项目字段与原版 RuoYi 不同：手机号用 `phone`（非 `phonenumber`），逻辑删除用 `is_deleted`（非 `del_flag`）

### 4.4 字段命名与类型推断

| 字段后缀 | 数据库类型 | 控件推断 | 查询方式 |
|---------|-----------|---------|---------|
| `xxx_name` | `varchar(100)` | input | LIKE |
| `status` | `char(1)` | radio | EQ |
| `xxx_type` | `varchar(20)` | select | EQ |
| `xxx_time` | `datetime` | datetime | BETWEEN |
| `is_xxx` | `char(1)` | select | EQ |
| `xxx_img` | `varchar(500)` | imageUpload | - |
| `xxx_content` | `text` | editor | - |
| `xxx_amount` | `decimal(10,2)` | numberInput | EQ |
| `sort_order` | `int(4)` | numberInput | - |

### 4.5 默认值规范

| 字段 | 默认值 | 说明 |
|------|--------|------|
| `tenant_id` | `'000000'` | 必须 |
| `status` | `'1'` | 1=正常，0=停用 |
| `is_xxx` | `'1'` | 1=是，0=否 |
| `is_deleted` | `'0'` | 0=正常，1=已删除 |
| `sort_order` | `999` | 默认排序值 |

---

## 5. 字典管理

### 5.1 系统内置字典（无需创建）

| 字典类型 | 字典名称 | 用途 |
|---------|---------|------|
| `sys_enable_status` | 启用状态 | status 字段 (0停用 1正常) |
| `sys_boolean_flag` | 逻辑标志 | is_xxx 字段 (0否 1是) |
| `sys_user_gender` | 用户性别 | gender 字段 |
| `sys_audit_status` | 审核状态 | 审核功能 |
| `sys_platform_type` | 平台类型 | 平台配置 |

### 5.2 创建业务字典

**命名规范**：`[模块前缀]_[业务对象]_[字段含义]`

```sql
-- 1. 先查询最大 ID
SELECT MAX(dict_id) FROM sys_dict_type;
SELECT MAX(dict_data_id) FROM sys_dict_data;

-- 2. 创建字典类型
INSERT INTO sys_dict_type (
    dict_id, tenant_id, dict_name, dict_type, is_system, status,
    create_dept, create_by, create_time, update_time, remark
) VALUES (
    [新dict_id], '000000', '优惠券类型', 'mall_coupon_type', '0', '1',
    100, 1, NOW(), NOW(), '商城优惠券类型字典'
);

-- 3. 创建字典数据
INSERT INTO sys_dict_data (
    dict_data_id, tenant_id, dict_sort, dict_label, dict_value, dict_type,
    css_class, list_class, is_default, status,
    create_dept, create_by, create_time, update_time, remark
) VALUES
([新dict_data_id], '000000', 1, '满减券', '1', 'mall_coupon_type', '', 'primary', 'N', '1', 100, 1, NOW(), NOW(), ''),
([新dict_data_id+1], '000000', 2, '折扣券', '2', 'mall_coupon_type', '', 'success', 'N', '1', 100, 1, NOW(), NOW(), ''),
([新dict_data_id+2], '000000', 3, '兑换券', '3', 'mall_coupon_type', '', 'warning', 'N', '1', 100, 1, NOW(), NOW(), '');
```

---

## 6. 索引设计

### 6.1 索引原则

1. **主键索引**：自动创建
2. **租户索引**：大表建议添加 `idx_tenant_id`
3. **时间索引**：`idx_create_time` 用于排序
4. **外键索引**：关联字段建索引
5. **高频查询**：根据业务添加

### 6.2 索引命名规范

```sql
-- 普通索引
KEY idx_[字段名] ([字段名])

-- 唯一索引
UNIQUE KEY uk_[字段名] ([字段名])

-- 联合索引
KEY idx_[字段1]_[字段2] ([字段1], [字段2])
```

---

## 7. 菜单管理

### 7.1 上级菜单映射

| 模块 | 上级菜单 | 说明 |
|------|---------|------|
| base | APP配置 | 查询: `SELECT menu_id FROM sys_menu WHERE menu_name = 'APP配置'` |
| mall | 商城管理 | 查询: `SELECT menu_id FROM sys_menu WHERE menu_name = '商城管理'` |
| iot | 物联网管理 | 需创建顶级菜单 |
| crm | CRM管理 | 需创建顶级菜单 |

### 7.2 sys_menu 字段速查表

> 🔴 **致命错误警告（必读！）**：
> - 字段名是 **`is_external_link`**，**绝对不是 `is_frame`**！（原版 RuoYi-Vue-Plus 用 `is_frame`，本项目已改名）
> - 框架统一约定：**`'1'` = 积极（正常/显示/缓存）、`'0'` = 消极（停用/隐藏/不缓存/非外链）**
> - 所有布尔/状态字段的默认值都遵循此约定

| 字段 | 类型 | 默认值 | 含义 | ⚠️ 易错点 |
|------|------|--------|------|-----------|
| `menu_id` | bigint | - | 菜单ID | 必须查询 `MAX(menu_id)` 避免冲突 |
| `menu_name` | varchar(50) | - | 菜单名称（中文） | - |
| `parent_id` | bigint | 0 | 父菜单ID | 一级目录=0 |
| `order_num` | int | 0 | 排序 | 同级递增10 |
| `path` | varchar(200) | `''` | 路由地址 | F按钮固定 `'#'` |
| `component` | varchar(255) | NULL | 组件路径 | M目录=NULL，F按钮=`''` |
| `query_param` | varchar(255) | NULL | 路由参数 | 通常为 NULL |
| **`is_external_link`** | char(1) | **`'0'`** | 是否外链 | ❌ **不是 `is_frame`！** 0=非外链 1=外链 |
| **`is_cache`** | char(1) | **`'1'`** | 是否缓存 | 1=缓存 0=不缓存 |
| `menu_type` | char(1) | `''` | 菜单类型 | M=目录 C=菜单 F=按钮 |
| **`visible`** | char(1) | **`'1'`** | 显示状态 | **1=显示 0=隐藏**（不要搞反！） |
| **`status`** | char(1) | **`'1'`** | 启用状态 | **1=正常 0=停用**（不要搞反！） |
| `perms` | varchar(100) | NULL | 权限标识 | 格式: `module:business:action` |
| `icon` | varchar(100) | `'#'` | 图标 | F按钮固定 `'#'` |
| `create_dept` | bigint | NULL | 创建部门 | 固定 `100` |
| `create_by` | bigint | NULL | 创建人 | 固定 `1` |
| `create_time` | datetime | NULL | 创建时间 | `sysdate()` 或 `NOW()` |
| `update_by` | bigint | NULL | 更新人 | NULL |
| `update_time` | datetime | NULL | 更新时间 | NULL |
| `remark` | varchar(500) | NULL | 备注 | - |

### 7.3 创建功能菜单 SQL 模板

> **图标选择**：查阅 `icon-management` 技能中的「关键词→图标映射表」选择合适图标。
> 一级目录必须设置图标，二级菜单推荐设置图标，按钮权限固定 `'#'`。

#### 准备工作

```sql
-- 1. 查询最大 menu_id
SELECT MAX(menu_id) FROM sys_menu;

-- 2. 查询父菜单 ID 和最大 order_num
SELECT menu_id FROM sys_menu WHERE menu_name = '[父菜单名]';
SELECT MAX(order_num) FROM sys_menu WHERE parent_id = [父菜单ID];
-- 新菜单 order_num = MAX + 10
```

#### M 目录菜单（一级目录）

```sql
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component,
    is_external_link, is_cache, menu_type, visible, status,
    perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES ([新ID], '[模块名]管理', 0, [order_num], '[module]Manage', NULL,
    '0', '1', 'M', '1', '1',
    '', '[icon]', 100, 1, NOW(), NULL, NULL, '[模块名]管理目录');
-- is_external_link='0'(非外链) | is_cache='1'(缓存) | visible='1'(显示) | status='1'(正常)
```

#### C 菜单（二级功能菜单）

```sql
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component,
    is_external_link, is_cache, menu_type, visible, status,
    perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES ([新ID], '[功能名]管理', [父菜单ID], [order_num], '[business]', 'business/[module]/[business]/[business]',
    '0', '1', 'C', '1', '1',
    '[module]:[business]:view', '[icon]', 100, 1, NOW(), NULL, NULL, '[功能名]管理菜单');
-- is_external_link='0'(非外链) | is_cache='1'(缓存) | visible='1'(显示) | status='1'(正常)
```

#### F 按钮权限（批量插入）

```sql
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component,
    is_external_link, is_cache, menu_type, visible, status,
    perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES
([新ID+1], '[功能名]查询', [C菜单ID], 1, '#', '', '0', '1', 'F', '1', '1', '[module]:[business]:query',  '#', 100, 1, NOW(), NULL, NULL, ''),
([新ID+2], '[功能名]新增', [C菜单ID], 2, '#', '', '0', '1', 'F', '1', '1', '[module]:[business]:add',    '#', 100, 1, NOW(), NULL, NULL, ''),
([新ID+3], '[功能名]修改', [C菜单ID], 3, '#', '', '0', '1', 'F', '1', '1', '[module]:[business]:update', '#', 100, 1, NOW(), NULL, NULL, ''),
([新ID+4], '[功能名]删除', [C菜单ID], 4, '#', '', '0', '1', 'F', '1', '1', '[module]:[business]:delete', '#', 100, 1, NOW(), NULL, NULL, ''),
([新ID+5], '[功能名]导出', [C菜单ID], 5, '#', '', '0', '1', 'F', '1', '1', '[module]:[business]:export', '#', 100, 1, NOW(), NULL, NULL, ''),
([新ID+6], '[功能名]导入', [C菜单ID], 6, '#', '', '0', '1', 'F', '1', '1', '[module]:[business]:import', '#', 100, 1, NOW(), NULL, NULL, '');
-- 所有 F 按钮：is_external_link='0' | is_cache='1' | visible='1' | status='1' | path='#' | icon='#'
```

---

## 检查清单

**连接数据库前：**
- [ ] 是否先读取了 `application-dev.yml` 配置？
- [ ] 是否确认了数据库类型（MySQL/Oracle/PostgreSQL/SQL Server）？
- [ ] 是否从配置中获取了正确的连接信息？

**表设计检查：**
- [ ] 表名前缀是否正确（b_、m_、sys_ 等）？
- [ ] 是否包含所有审计字段？
- [ ] 是否有 `tenant_id` 字段？
- [ ] 是否有 `is_deleted` 逻辑删除字段？
- [ ] 状态字段默认值是否为 `'1'`？
- [ ] 每个字段是否都有注释？

**菜单 SQL 检查（🔴 最常出错！）：**
- [ ] 字段名是否用了 `is_external_link`（**不是 `is_frame`**）？
- [ ] `is_external_link` 默认值是否为 `'0'`（非外链）？
- [ ] `is_cache` 默认值是否为 `'1'`（缓存）？
- [ ] `visible` 默认值是否为 `'1'`（显示）？
- [ ] `status` 默认值是否为 `'1'`（正常）？
- [ ] 是否遵循 **1=积极 0=消极** 的约定？

**SQL 生成规则（强制）：**
- [ ] 是否已读取 `application-dev.yml` 确认当前启用的数据库类型？
- [ ] 是否**只生成了当前数据库类型的 SQL**（除非用户明确要求多数据库）？
- [ ] SQL 文件是否放到了对应数据库类型的正确目录？
