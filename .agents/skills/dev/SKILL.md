---
name: dev
description: |
  /dev - 开发新功能（双模式代码生成）

  触发场景：
  - 需要开发新的业务功能模块
  - 需要直接生成代码（全自动模式）
  - 需要生成代码生成器配置后手动生成（可微调模式）
  - 需要创建新的数据库表和对应代码
  - 需要智能推断模块、表前缀、菜单图标

  触发词：/dev、开发新功能、新功能、代码生成、智能配置、生成配置、新模块、业务开发、直接生成
---
# /dev - 开发新功能（双模式代码生成）

作为新功能开发助手，支持两种代码生成模式：直接生成代码或生成配置后手动生成。

## 🎯 核心优势

- ✅ **双模式选择**：直接生成代码（全自动） / 生成配置后手动生成（可微调）
- ✅ Token 消耗低（配置模式约 2000 tokens）
- ✅ 全自动执行，无需多次确认
- ✅ 智能推断模块、前缀、图标
- ✅ 代码风格由框架保证一致性

## 📋 两种模式对比

| 特性 | 模式A：直接生成代码 | 模式B：生成配置 |
|------|-------------------|----------------|
| **适用场景** | 标准 CRUD，无需微调 | 需要调整字段配置后再生成 |
| **自动化程度** | 全自动（建表+配置+AI直接编写代码+菜单导入） | 半自动（建表+配置，手动去前端生成） |
| **可调整性** | AI 编写后可直接修改代码 | 生成前可在前端界面微调列配置 |
| **执行速度** | 最快（一次性完成） | 需要额外手动操作 |

---

## 🚀 执行流程

### 第一步：询问需求与模式选择

使用 AskUserQuestion 工具同时询问以下问题：

**问题1：功能信息**
```
请告诉我要开发的功能：
1. **功能名称**？（如：优惠券管理、设备监控）
2. **所属模块**？（base/mall/iot/crm/marketing/其他）
```

**问题2：生成模式选择**（使用 AskUserQuestion）

| 选项 | 说明 |
|------|------|
| **直接生成代码（推荐）** | 建表 → 配置 → AI 直接编写全部后端+前端代码文件，全程无需手动操作 |
| **生成配置，手动生成** | 建表 → 配置 → 在前端【代码生成】界面微调后手动点击生成 |

**自动推断配置**（无需确认）：
- base → 表前缀 `b_`，包名 `plus.ruoyi.business.base`
- mall → 表前缀 `m_`，包名 `plus.ruoyi.business.mall`
- iot → 表前缀 `iot_`，包名 `plus.ruoyi.business.iot`
- 其他 → 根据模块名自动生成

### 第一步续：检查活跃任务关联

**自动扫描** `docs/tasks/active/` 是否有与当前功能相关的活跃任务：

```bash
# 扫描活跃任务
ls docs/tasks/active/
```

**处理逻辑**：

| 情况 | 处理方式 |
|------|---------|
| 存在相关活跃任务 | 显示关联信息，开发完成后可更新 |
| 无相关任务或目录不存在 | 静默跳过，不输出 |

**如果存在相关活跃任务**，输出：
```markdown
📋 发现相关活跃任务：
- {任务名} (进度 X/Y, Z%)
- 文件: docs/tasks/active/{文件名}

当前开发可能与此任务相关，开发完成后可更新任务进度。
```

**注意**：此步骤仅为信息展示，不阻塞开发流程。

---

### 第二步：检查功能是否已存在（强制执行）⭐⭐⭐⭐⭐

**自动检查以下内容**：

```bash
# 1. 读取项目资源概览
Read CLAUDE.md

# 2. 检查后端
Grep pattern: "[功能名]Service" path: ruoyi-modules/ output_mode: files_with_matches
Grep pattern: "[功能名]Controller" path: ruoyi-modules/ output_mode: files_with_matches

# 3. 检查前端
Grep pattern: "[功能名]Api" path: plus-ui/src/api/ output_mode: files_with_matches

# 4. 检查数据库表
Read ruoyi-admin/src/main/resources/application-dev.yml
mysql -h[host] -P[port] -u[user] -p[pass] [db] -e "SHOW TABLES LIKE '[表前缀][功能名]';"
```

**检查结果处理**：

**① 如果功能已存在** → 停止执行，输出：
```markdown
⚠️ 功能已存在，避免重复开发！

**已有实现**：
- 后端：[Service/Controller位置]
- 前端：[API位置]
- 数据库表：[表名]

建议：
- 增强功能：在现有代码中添加方法
- 修改功能：使用 Read 工具查看现有代码

是否仍要继续？（通常不建议）
```

**② 如果功能未实现** → 继续执行，输出：
```markdown
✅ 功能未实现，可以开始开发
```

---

### 第三步：连接数据库查看现状（自动执行）

**⚠️ 重要**：数据库连接信息必须从 `application-dev.yml` 动态读取，不要硬编码！

```bash
# 读取数据库配置
Read ruoyi-admin/src/main/resources/application-dev.yml

# 从配置文件中解析数据库连接信息
# 格式：${环境变量:默认值}
# 示例：${DB_HOST:127.0.0.1} → 127.0.0.1
#      ${DB_NAME:ryplus_uni} → ryplus_uni（可能是其他数据库名）

# 连接数据库查询（使用解析出的配置）
mysql -h[host] -P[port] -u[user] -p[pass] [db] <<EOF
-- 查看现有表
SHOW TABLES LIKE 'b_%';
SHOW TABLES LIKE 'm_%';

-- 查询最大菜单ID
SELECT MAX(menu_id) FROM sys_menu;

-- 查询最大表ID
SELECT MAX(table_id) FROM sys_gen_table;

-- 查询最大字典类型ID和字典数据ID
SELECT MAX(dict_id) FROM sys_dict_type;
SELECT MAX(dict_data_id) FROM sys_dict_data;

-- 查询现有顶级菜单（用于判断上级菜单归属）
-- ⚠️ 重要：记录查询结果中的 menu_id，后续步骤需要使用（如 APP配置、商城管理 的 menu_id）
SELECT menu_id, menu_name, order_num
FROM sys_menu
WHERE menu_type = 'M' AND parent_id = 0
ORDER BY order_num;

-- 查询各模块下的子菜单（用于计算菜单顺序）
SELECT parent_id, MAX(order_num) as max_order
FROM sys_menu
WHERE menu_type = 'C'
GROUP BY parent_id;

-- 查询现有字典类型（避免重复创建）
SELECT dict_type, dict_name FROM sys_dict_type;
EOF
```

---

### 第四步：设计表结构（自动执行）

#### 4.1 学习数据库规范

```bash
Read .claude/docs/数据库设计规范.md
Read script/sql/ry_plus_app.sql offset: 1 limit: 50
```

#### 4.2 智能字段命名（触发 GenUtils 推断）

**AI 必须遵循以下命名规则**（完整的 GenUtils 后缀列表）：

| 分类 | 字段后缀 | 自动推断结果 | 示例 |
|------|---------|-------------|------|
| **文本类型** | | | |
| | `xxx_name` | LIKE查询 + input | `coupon_name`, `user_name` |
| | `xxx_title` | LIKE查询 + input | `article_title` |
| | `xxx_content` | editor 富文本 | `news_content`, `description_content` |
| | `remark` | textarea | `remark` |
| **数值类型** | | | |
| | `xxx_num` / `xxx_number` | numberInput | `stock_num`, `order_number` |
| | `xxx_count` / `xxx_quantity` | numberInput | `view_count`, `goods_quantity` |
| | `xxx_amount` / `xxx_price` | numberInput | `discount_amount`, `unit_price` |
| | `xxx_total` / `xxx_rate` | numberInput | `order_total`, `discount_rate` |
| **日期时间** | | | |
| | `xxx_time` | datetime + BETWEEN | `create_time`, `expire_time` |
| | `xxx_date` | datetime + BETWEEN | `birth_date`, `start_date` |
| **状态/类型** | | | |
| | `status` | radio + sys_enable_status | `status` |
| | `xxx_status` | radio/select + 自定义字典 | `order_status`, `device_status` |
| | `xxx_type` | select + 自定义字典 | `coupon_type`, `sensor_type` |
| | `is_xxx` | select + sys_boolean_flag | `is_active`, `is_default` |
| **图片上传** | | | |
| | `xxx_img` / `xxx_image` | imageUpload | `cover_img`, `main_image` |
| | `xxx_pic` / `xxx_picture` | imageUpload | `product_pic` |
| | `xxx_photo` / `xxx_avatar` | imageUpload | `user_photo`, `avatar` |
| | `xxx_icon` / `xxx_logo` | imageUpload | `app_icon`, `brand_logo` |
| | `xxx_cover` | imageUpload | `article_cover` |
| **文件上传** | | | |
| | `xxx_file` / `xxx_attach` | fileUpload | `contract_file`, `order_attach` |
| | `xxx_attachment` | fileUpload | `email_attachment` |
| | `xxx_doc` / `xxx_document` | fileUpload | `report_doc` |
| | `xxx_pdf` | fileUpload | `invoice_pdf` |

**标准表结构模板**：
```sql
CREATE TABLE [前缀]_[功能名] (
    id              BIGINT(20)   NOT NULL COMMENT '主键ID',  -- 雪花ID，不用 AUTO_INCREMENT
    tenant_id       VARCHAR(20)  DEFAULT '000000' COMMENT '租户ID',

    -- 业务字段（遵循智能命名规则）
    xxx_name        VARCHAR(100) NOT NULL COMMENT '名称',
    xxx_type        CHAR(1)      DEFAULT '1' COMMENT '类型',
    xxx_amount      DECIMAL(10,2) DEFAULT 0 COMMENT '金额',
    xxx_img         VARCHAR(500) DEFAULT NULL COMMENT '图片',
    start_time      DATETIME     DEFAULT NULL COMMENT '开始时间',
    end_time        DATETIME     DEFAULT NULL COMMENT '结束时间',
    is_xxx          CHAR(1)      DEFAULT '1' COMMENT '是否xxx(0否 1是)',
    status          CHAR(1)      DEFAULT '1' COMMENT '状态(0停用 1正常)',

    -- 标准审计字段
    create_dept     BIGINT(20)   DEFAULT NULL COMMENT '创建部门',
    create_by       BIGINT(20)   DEFAULT NULL COMMENT '创建人',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by       BIGINT(20)   DEFAULT NULL COMMENT '更新人',
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    is_deleted      CHAR(1)      DEFAULT '0' COMMENT '是否删除(0正常 1已删除)',

    PRIMARY KEY (id),
    KEY idx_tenant_id (tenant_id),
    KEY idx_create_time (create_time)
    -- 业务索引：外键字段、高频查询字段
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='[功能名]表';
```

**⚠️ 重要默认值**：
- `tenant_id`: 必须默认 `'000000'`
- `status`: 必须默认 `'1'` (正常)
- `is_xxx`: 必须默认 `'1'` (是)
- `is_deleted`: 必须默认 `'0'` (未删除)

---

### 第五步：生成方案并确认（仅此一次确认）⭐⭐⭐⭐⭐

**输出完整方案**：
```markdown
## 📋 代码生成方案

### 配置信息
- **功能名称**：优惠券管理
- **模块名称**：mall
- **表名**：m_coupon
- **包名**：plus.ruoyi.business.mall
- **Java类名**：Coupon
- **菜单图标**：ticket（从817个图标智能匹配）

### 菜单配置
- **上级菜单**：商城管理（menu_id: [查询到的上级菜单ID]）
  - 如不存在会自动创建
- **菜单顺序**：20（根据现有子菜单自动递增）

### 字典类型检查
| 字典类型 | 状态 | 说明 | 前端枚举 |
|---------|------|------|---------|
| sys_enable_status | ✅ 已存在 | 系统内置（启用状态） | 已存在 |
| sys_boolean_flag | ✅ 已存在 | 系统内置（逻辑标志） | 已存在 |
| mall_coupon_type | ⚠️ 需创建 | 优惠券类型（满减券、折扣券、兑换券） | 需同步 |

### 表结构设计
\```sql
CREATE TABLE m_coupon (
    id              BIGINT(20)   NOT NULL COMMENT '优惠券ID',  -- 雪花ID
    tenant_id       VARCHAR(20)  DEFAULT '000000' COMMENT '租户ID',
    coupon_name     VARCHAR(100) NOT NULL COMMENT '优惠券名称',
    coupon_type     CHAR(1)      DEFAULT '1' COMMENT '优惠券类型',
    discount_amount DECIMAL(10,2) NOT NULL COMMENT '优惠金额',
    start_time      DATETIME     DEFAULT NULL COMMENT '开始时间',
    end_time        DATETIME     DEFAULT NULL COMMENT '结束时间',
    status          CHAR(1)      DEFAULT '1' COMMENT '状态',
    ...
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券表';
\```

### 智能推断配置
| 字段 | 类型 | 控件 | 查询方式 | 字典类型 | 说明 |
|------|------|------|---------|---------|------|
| id | Long | input | EQ | - | 主键（不插入） |
| tenant_id | String | - | - | - | 框架自动注入（所有权限为0） |
| coupon_name | String | input | LIKE | - | - |
| coupon_type | String | select | EQ | mall_coupon_type | - |
| discount_amount | BigDecimal | numberInput | EQ | - | - |
| start_time | Date | datetime | BETWEEN | - | - |
| status | String | radio | EQ | sys_enable_status | - |

### 执行步骤
1. ✅ （如需要）创建上级菜单
2. ✅ （如需要）创建字典类型和字典数据
3. ✅ （如需要）同步前端字典枚举到 DictTypes
4. ✅ 创建数据库表
5. ✅ 插入代码生成配置（sys_gen_table）
6. ✅ 插入列配置（sys_gen_table_column，共15个字段）

---

**确认开始生成？**（回复"确认"或"开始"）
```

---

### 第六步：自动执行生成（无需确认）

用户确认后，AI 自动执行以下步骤：

#### 6.1 智能判断和创建上级菜单

**判断逻辑**：
```
base 模块 → 归属 "APP配置" (menu_id: 从第三步查询结果获取)
mall 模块 → 归属 "商城管理" (menu_id: 从第三步查询结果获取)
iot 模块  → 归属 "物联网管理" (从查询结果获取，如不存在则创建)
crm 模块  → 归属 "CRM管理" (从查询结果获取，如不存在则创建)
其他模块  → 归属 "[模块名]管理" (从查询结果获取，如不存在则创建)
```

**上级菜单不存在时，自动创建**：
```bash
mysql -h[host] -P[port] -u[user] -p[pass] [db] <<EOF
-- 插入上级菜单（如果不存在）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_external_link, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES (
    [新menu_id], '物联网管理', 0, 91, 'iotManage',
    NULL, '0', '1', 'M', '1', '1',
    '', 'iot', 100, 1, NOW(), NULL, NULL, '物联网管理目录'
);
EOF
```

输出：`✅ 上级菜单已就绪：物联网管理 (menu_id: [新ID])`

#### 6.2 计算菜单顺序

**根据现有子菜单自动计算**：
```
查询上级菜单下最大的 order_num
新菜单顺序 = MAX(order_num) + 10

示例：
- 商城管理下已有：商品(1)、订单(10)
- 新增优惠券：order_num = 20
```

#### 6.3 执行建表SQL

```bash
mysql -h[host] -P[port] -u[user] -p[pass] [db] <<EOF
[建表SQL]
EOF
```

输出：`✅ 表创建成功：m_coupon`

#### 6.4 智能选择菜单图标

```bash
# 读取图标列表
Read plus-ui/src/types/icons.d.ts offset: 1 limit: 200
```

**智能匹配规则**（从817个图标中自动选择）：
```
优惠券 → coupon, ticket, sale
用户   → user, account
订单   → order, shopping-cart
商品   → goods, commodity
设备   → device, hardware
反馈   → feedback, message
积分   → medal, coin
活动   → activity, event
默认   → list, document
```

#### 6.5 检查并生成字典类型（如需要）

**AI 必须检查字段使用的字典是否已存在**：

```bash
# 检查字典类型是否存在
mysql -h[host] -P[port] -u[user] -p[pass] [db] <<EOF
SELECT dict_type FROM sys_dict_type WHERE dict_type IN ('需要的字典类型列表');
EOF
```

**字典检查逻辑**：

1. **提取需要的字典类型**（从字段设计中）：
   ```
   示例：
   - status 字段 → sys_enable_status (✅ 系统内置，无需创建)
   - is_xxx 字段 → sys_boolean_flag (✅ 系统内置，无需创建)
   - coupon_type 字段 → mall_coupon_type (需要检查)
   - device_status 字段 → iot_device_status (需要检查)
   ```

2. **常见系统内置字典**（无需创建）：
   ```
   ✅ sys_enable_status       - 启用状态 (0停用 1正常)
   ✅ sys_boolean_flag        - 逻辑标志 (0否 1是)
   ✅ sys_user_gender         - 用户性别
   ✅ sys_data_scope          - 数据权限
   ✅ sys_audit_status        - 审核状态
   ✅ sys_platform_type       - 平台类型
   ```

3. **需要创建的字典类型**（业务专属）：
   ```
   示例：
   - mall_coupon_type         - 优惠券类型 (满减券、折扣券等)
   - iot_device_status        - 设备状态 (在线、离线、故障)
   - crm_customer_level       - 客户等级 (普通、VIP等)
   ```

**字典不存在时，自动创建（字典值（`dict_value`）必须是**小写英文字母或数字**，禁止使用大写字母！）**：

```bash
mysql -h[host] -P[port] -u[user] -p[pass] [db] <<EOF
-- 插入字典类型
INSERT INTO sys_dict_type (
    dict_id, tenant_id, dict_name, dict_type, status,
    create_dept, create_by, create_time, update_time, remark
) VALUES (
    [新dict_id], '000000', '优惠券类型', 'mall_coupon_type', '0',
    100, 1, NOW(), NOW(), '商城优惠券类型字典'
);

-- 插入字典数据
INSERT INTO sys_dict_data (
    dict_data_id, tenant_id, dict_sort, dict_label, dict_value, dict_type,
    css_class, list_class, is_default, status,
    create_dept, create_by, create_time, update_time, remark
) VALUES
([新dict_data_id], '000000', 1, '满减券', '1', 'mall_coupon_type', '', 'default', 'N', '0', 100, 1, NOW(), NOW(), ''),
([新dict_data_id+1], '000000', 2, '折扣券', '2', 'mall_coupon_type', '', 'default', 'N', '0', 100, 1, NOW(), NOW(), ''),
([新dict_data_id+2], '000000', 3, '兑换券', '3', 'mall_coupon_type', '', 'default', 'N', '0', 100, 1, NOW(), NOW(), '');
EOF
```

输出：
```markdown
✅ 字典类型检查完成：
- sys_enable_status: 已存在（系统内置）
- sys_boolean_flag: 已存在（系统内置）
- mall_coupon_type: 已创建（3个字典项）
```

---

#### 6.6 同步前端字典类型枚举（仅新建字典时执行）⭐⭐⭐⭐⭐

**⚠️ 如果步骤 6.5 中创建了新字典类型，必须同步到前端！**

**操作步骤**：

1. 读取前端枚举：`Read plus-ui/src/composables/useDict.ts offset: 1 limit: 40`

2. 使用 Edit 工具在枚举末尾添加新字典类型：
```typescript
// 示例：添加 mall_coupon_type
old_string: |
  /** 数据权限类型 */
  sys_data_scope = 'sys_data_scope'
}

new_string: |
  /** 数据权限类型 */
  sys_data_scope = 'sys_data_scope',
  /** 优惠券类型 */
  mall_coupon_type = 'mall_coupon_type'
}
```

3. 输出：`✅ 前端字典类型已同步：mall_coupon_type`

**注意**：
- 系统内置字典（sys_enable_status、sys_boolean_flag 等）无需同步
- 注释格式：`/** [字典名称] */`
- 枚举最后一项必须有逗号

---

#### 6.7 生成并执行配置SQL

**AI 模拟 GenUtils 推断规则**：

**Java类型推断**：
```
varchar/char/text → String
datetime/timestamp → Date
int(长度≤10) → Integer
bigint/int(长度>10) → Long
decimal → BigDecimal
```

**HTML控件推断**：
```
根据字段命名规则自动推断（见第四步）
```

**增删改查权限推断**：
```
主键（id）：is_increment=0（雪花ID，非自增）, isRequired=1, isInsert=NULL（主键不插入）, isEdit=1, isList=1, isQuery=1
租户ID（tenant_id）：isRequired=0, isInsert=0（框架自动注入，无需插入）, isEdit=0, isList=0, isQuery=0
业务字段：isRequired（根据NOT NULL判断）, isInsert=1, isEdit=1, isList=1, isQuery=1
审计字段（create_time等）：isInsert=0, isEdit=0, isList（部分显示）, isQuery=0
逻辑删除（is_deleted）：isInsert=0, isEdit=0, isList=0, isQuery=0
```

**⚠️ column_default 字段规则**：
```
- 无默认值：'undefined'（单引号包裹）
- 有默认值：根据类型格式化（见下方详细规则）
```

**⚠️ column_default 默认值格式化规则（重要！）**：

| 数据类型 | column_default 格式 | 示例 |
|---------|-------------------|------|
| VARCHAR/CHAR/TEXT | **三重引号** | `'''NORMAL'''`, `'''1'''` |
| INT/BIGINT/DECIMAL | 单引号 | `'0'`, `'0.00'` |
| DATE/DATETIME | 单引号 | `'undefined'` |
| tenant_id（VARCHAR类型） | **三重引号** | `'''000000'''` |

**核心原理**：字符串类型字段用三重引号是为了在数据库中存储带引号的值（`'NORMAL'`），否则代码生成时会产生 `private String field = NORMAL;` 导致编译错误。

**示例**：
```sql
-- 字典字段
('vip_level', ..., '''NORMAL''', ...),
('status', ..., '''1''', ...),
-- 数值字段
('points', ..., '0', ...),
('total_consume', ..., '0.00', ...),
-- 租户ID（VARCHAR类型，也需要三重引号）
('tenant_id', ..., '''000000''', ...),
-- 无默认值
('gender', ..., 'undefined', ...),
```

**生成并执行SQL**：
```bash
mysql -h[host] -P[port] -u[user] -p[pass] [db] <<EOF
-- 插入表配置
INSERT INTO sys_gen_table (
    table_id, data_name, table_name, table_comment,
    class_name, tpl_category, package_name, module_name, business_name,
    function_name, function_author, gen_type, gen_path, options,
    create_dept, create_by, create_time, update_time
) VALUES (
    [自增ID], 'master', 'm_coupon', '优惠券表',
    'Coupon', 'crud', 'plus.ruoyi.business.mall', 'mall', 'coupon',
    '优惠券', '抓蛙师', '1', '/', '{"parentMenuId":"[查询到的上级菜单ID]","menuIcon":"ticket","menuOrder":"20","autoImportMenu":"1"}',
    100, 1, NOW(), NOW()
);

-- 批量插入列配置（所有字段）
INSERT INTO sys_gen_table_column (
    column_id, table_id, column_name, column_comment, column_label,
    column_type, java_type, java_field, is_pk, is_increment, is_required,
    is_insert, is_edit, is_list, is_query, query_type, html_type, dict_type, column_default,
    sort, create_dept, create_by, create_time, update_time
) VALUES
([自增ID], [table_id], 'id', '优惠券ID', '优惠券ID', 'bigint(20)', 'Long', 'id', '1', '0', '1', NULL, '1', '1', '1', 'EQ', 'input', '', 'undefined',1, 100, 1, NOW(), NOW()),
([自增ID], [table_id], 'tenant_id', '租户ID', '租户ID', 'varchar(20)', 'String', 'tenantId', '0', '0', '0', '0', '0', '0', '0', 'EQ', 'input', '', '''000000''', 2, 100, 1, NOW(), NOW()),
-- ⚠️ tenant_id 配置说明：is_insert=0（由TenantEntity自动注入，无需手动插入），is_edit/is_list/is_query=0（完全由框架自动处理）
([自增ID], [table_id], 'coupon_name', '优惠券名称', '优惠券名称', 'varchar(100)', 'String', 'couponName', '0', '0', '1', '1', '1', '1', '1', 'LIKE', 'input', '', 'undefined', 3, 100, 1, NOW(), NOW()),
([自增ID], [table_id], 'coupon_type', '优惠券类型', '优惠券类型', 'char(1)', 'String', 'couponType', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'select', 'mall_coupon_type', '''1''', 4, 100, 1, NOW(), NOW()),
([自增ID], [table_id], 'discount_amount', '优惠金额', '优惠金额', 'decimal(10,2)', 'BigDecimal', 'discountAmount', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'numberInput', '', 'undefined', 5, 100, 1, NOW(), NOW()),
([自增ID], [table_id], 'start_time', '开始时间', '开始时间', 'datetime', 'Date', 'startTime', '0', '0', '0', '1', '1', '1', '1', 'BETWEEN', 'datetime', '', 'undefined', 6, 100, 1, NOW(), NOW()),
([自增ID], [table_id], 'end_time', '结束时间', '结束时间', 'datetime', 'Date', 'endTime', '0', '0', '0', '1', '1', '1', '1', 'BETWEEN', 'datetime', '', 'undefined', 7, 100, 1, NOW(), NOW()),
([自增ID], [table_id], 'status', '状态', '状态', 'char(1)', 'String', 'status', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'radio', 'sys_enable_status', '''1''', 8, 100, 1, NOW(), NOW()),
([自增ID], [table_id], 'create_dept', '创建部门', '创建部门', 'bigint(20)', 'Long', 'createDept', '0', '0', '0', '0', '0', '0', '0', 'EQ', 'input', '', 'undefined', 9, 100, 1, NOW(), NOW()),
([自增ID], [table_id], 'create_by', '创建人', '创建人', 'bigint(20)', 'Long', 'createBy', '0', '0', '0', '0', '0', '0', '0', 'EQ', 'input', '', 'undefined', 10, 100, 1, NOW(), NOW()),
([自增ID], [table_id], 'create_time', '创建时间', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', '0', '0', '0', '1', '0', 'EQ', 'datetime', '', 'undefined', 11, 100, 1, NOW(), NOW()),
([自增ID], [table_id], 'update_by', '更新人', '更新人', 'bigint(20)', 'Long', 'updateBy', '0', '0', '0', '0', '0', '0', '0', 'EQ', 'input', '', 'undefined', 12, 100, 1, NOW(), NOW()),
([自增ID], [table_id], 'update_time', '更新时间', '更新时间', 'datetime', 'Date', 'updateTime', '0', '0', '0', '0', '0', '0', '0', 'EQ', 'datetime', '', 'undefined', 13, 100, 1, NOW(), NOW()),
([自增ID], [table_id], 'remark', '备注', '备注', 'varchar(500)', 'String', 'remark', '0', '0', '0', '1', '1', '0', '0', 'EQ', 'textarea', '', 'undefined', 14, 100, 1, NOW(), NOW()),
([自增ID], [table_id], 'is_deleted', '是否删除', '是否删除', 'char(1)', 'String', 'isDeleted', '0', '0', '0', '0', '0', '0', '0', 'EQ', 'input', '', '''0''', 15, 100, 1, NOW(), NOW())
;
EOF
```

输出：
```markdown
✅ 配置生成成功！

- sys_gen_table: 1 条
- sys_gen_table_column: 15 条
```

---

### 第七步：根据模式执行（模式分支）⭐⭐⭐⭐⭐

#### 模式A：直接生成代码（用户选择了"直接生成代码"）

**AI 直接编写所有后端和前端代码文件，无需用户手动操作。**

##### 7A.1 读取参考代码（必须！）

> 如果 Ad 模块不存在，则在 `ruoyi-modules/ruoyi-business/` 下找任意一个已有的业务模块作为参考模板。

```bash
# 读取后端参考代码（Ad 模块作为标准模板）
Read ruoyi-modules/ruoyi-business/src/main/java/plus/ruoyi/business/base/domain/Ad.java
Read ruoyi-modules/ruoyi-business/src/main/java/plus/ruoyi/business/base/domain/bo/AdBo.java
Read ruoyi-modules/ruoyi-business/src/main/java/plus/ruoyi/business/base/domain/vo/AdVo.java
Read ruoyi-modules/ruoyi-business/src/main/java/plus/ruoyi/business/base/controller/AdController.java
Read ruoyi-modules/ruoyi-business/src/main/java/plus/ruoyi/business/base/service/IAdService.java
Read ruoyi-modules/ruoyi-business/src/main/java/plus/ruoyi/business/base/service/impl/AdServiceImpl.java
Read ruoyi-modules/ruoyi-business/src/main/java/plus/ruoyi/business/base/dao/IAdDao.java
Read ruoyi-modules/ruoyi-business/src/main/java/plus/ruoyi/business/base/dao/impl/AdDaoImpl.java
Read ruoyi-modules/ruoyi-business/src/main/java/plus/ruoyi/business/base/mapper/AdMapper.java

# 读取前端参考代码
Read plus-ui/src/views/business/base/ad/ad.vue
Read plus-ui/src/api/business/base/ad/adApi.ts
Read plus-ui/src/api/business/base/ad/adTypes.ts

# 读取 Mapper XML 参考
Glob pattern: "**/AdMapper.xml" path: ruoyi-modules/
```

##### 7A.2 直接编写后端代码（10个文件）

按照参考代码风格，AI 使用 Write 工具直接创建以下文件：

**文件创建顺序**（有依赖关系，必须按顺序）：

| 序号 | 文件 | 路径 | 说明 |
|------|------|------|------|
| 1 | Entity | `domain/[Class].java` | extends TenantEntity，@AutoMappers |
| 2 | BO | `domain/bo/[Class]Bo.java` | 业务对象 |
| 3 | VO | `domain/vo/[Class]Vo.java` | 视图对象 |
| 4 | Mapper | `mapper/[Class]Mapper.java` | extends BaseMapperPlus |
| 5 | Mapper XML | `resources/.../[Class]Mapper.xml` | MyBatis 映射 |
| 6 | IDao | `dao/I[Class]Dao.java` | DAO 接口 |
| 7 | DaoImpl | `dao/impl/[Class]DaoImpl.java` | DAO 实现（buildQueryWrapper） |
| 8 | IService | `service/I[Class]Service.java` | Service 接口 |
| 9 | ServiceImpl | `service/impl/[Class]ServiceImpl.java` | Service 实现 |
| 10 | Controller | `controller/[Class]Controller.java` | REST 控制器 |

**基路径**：`ruoyi-modules/ruoyi-business/src/main/java/plus/ruoyi/business/[module]/`

**⚠️ 关键规范**（必须严格遵守 CLAUDE.md 和 crud-development 技能）：
- 包名：`plus.ruoyi.business.[module]`
- Entity 继承 `TenantEntity`（不是 BaseEntity）
- Service 不继承基类，`implements IXxxService`
- 查询条件在 DAO 层 `buildQueryWrapper()` 构建
- 对象转换用 `MapstructUtils.convert()`
- Controller 只注入 Service，Service 只注入 DAO

##### 7A.3 直接编写前端代码（3个文件）

| 序号 | 文件 | 路径 | 说明 |
|------|------|------|------|
| 11 | Types | `[business]Types.ts` | 类型定义（Query/Bo/Vo） |
| 12 | API | `[business]Api.ts` | API 函数（http 已自动导入） |
| 13 | Page | `[business].vue` | 页面组件（使用 ASearchForm/AModal 等封装组件） |

**基路径**：
- 类型/API：`plus-ui/src/api/business/[module]/[business]/`
- 页面：`plus-ui/src/views/business/[module]/[business]/`

**⚠️ 前端关键规范**：
- 页面文件必须以业务名命名（如 `ad.vue`），**禁止使用 `index.vue`**
- 使用项目封装组件（ASearchForm/AFormInput/AModal 等），禁止原生 el- 组件
- API 调用使用 `const [err, data] = await api()` 格式
- http/Result/PageResult/PageQuery 已自动导入，无需 import

##### 7A.4 插入菜单 SQL

```bash
mysql -h[host] -P[port] -u[user] -p[pass] [db] <<EOF
-- 插入 C 类型菜单（业务菜单）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_external_link, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES ([新menu_id], '[功能名]管理', [上级菜单ID], [order_num], '[business]', 'business/[module]/[business]/[business]', '0', '1', 'C', '1', '1', '[module]:[business]:pageList', '[icon]', 100, 1, NOW(), NULL, NULL, '[功能名]管理菜单');

-- 插入 F 类型按钮权限（查询、新增、修改、删除、导出、导入）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_external_link, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES
([新menu_id+1], '[功能名]查询', [C菜单ID], 1, '#', '', '0', '1', 'F', '1', '1', '[module]:[business]:pageList', '#', 100, 1, NOW(), NULL, NULL, ''),
([新menu_id+2], '[功能名]新增', [C菜单ID], 2, '#', '', '0', '1', 'F', '1', '1', '[module]:[business]:add', '#', 100, 1, NOW(), NULL, NULL, ''),
([新menu_id+3], '[功能名]修改', [C菜单ID], 3, '#', '', '0', '1', 'F', '1', '1', '[module]:[business]:update', '#', 100, 1, NOW(), NULL, NULL, ''),
([新menu_id+4], '[功能名]删除', [C菜单ID], 4, '#', '', '0', '1', 'F', '1', '1', '[module]:[business]:remove', '#', 100, 1, NOW(), NULL, NULL, ''),
([新menu_id+5], '[功能名]导出', [C菜单ID], 5, '#', '', '0', '1', 'F', '1', '1', '[module]:[business]:export', '#', 100, 1, NOW(), NULL, NULL, ''),
([新menu_id+6], '[功能名]导入', [C菜单ID], 6, '#', '', '0', '1', 'F', '1', '1', '[module]:[business]:import', '#', 100, 1, NOW(), NULL, NULL, '');
EOF
```

**模式A 完成报告**：
```markdown
## 🎉 代码生成完成！（直接编写模式）

### 已完成
- ✅ 上级菜单已就绪：商城管理 (menu_id: [ID])
- ✅ 字典类型已就绪：mall_coupon_type (新建，含3个字典项)
- ✅ 前端字典枚举已同步：DictTypes.mall_coupon_type
- ✅ 数据库表创建：m_coupon
- ✅ 代码生成配置保存完成（备用）
- ✅ 后端代码已直接编写（10个文件）
- ✅ 前端代码已直接编写（3个文件）
- ✅ 菜单和权限已导入数据库

### 编写的文件
**后端**（10个）：
- Entity: ruoyi-modules/ruoyi-business/.../domain/[Class].java
- BO: .../domain/bo/[Class]Bo.java
- VO: .../domain/vo/[Class]Vo.java
- Controller: .../controller/[Class]Controller.java
- Service: .../service/I[Class]Service.java + impl/[Class]ServiceImpl.java
- DAO: .../dao/I[Class]Dao.java + impl/[Class]DaoImpl.java
- Mapper: .../mapper/[Class]Mapper.java + resources/.../[Class]Mapper.xml

**前端**（3个）：
- 页面: plus-ui/src/views/business/[module]/[business]/[business].vue
- API: plus-ui/src/api/business/[module]/[business]/[business]Api.ts
- 类型: plus-ui/src/api/business/[module]/[business]/[business]Types.ts

### ⚠️ 后续操作
- ⚠️ **重启后端服务**使新代码生效
- ⚠️ 在【菜单管理】中为角色分配权限
- 💡 推荐运行 `/check` 检查代码规范
```

---

#### 模式B：生成配置，手动生成（用户选择了"生成配置"）

**只输出配置完成报告，引导用户前往前端界面手动生成。**

```markdown
## 🎉 配置生成完成！（配置模式）

### 已完成
- ✅ 上级菜单已就绪：商城管理 (menu_id: [查询到的上级菜单ID])
- ✅ 字典类型已就绪：mall_coupon_type (新建，含3个字典项)
- ✅ 前端字典枚举已同步：DictTypes.mall_coupon_type
- ✅ 数据库表创建：m_coupon
- ✅ 代码生成配置保存完成
- ✅ 智能推断字段配置（15个字段）

### 菜单配置详情
- **上级菜单ID**: [查询到的上级菜单ID] (商城管理)
- **菜单顺序**: 20
- **菜单图标**: ticket
- **自动导入菜单**: 已启用

### 字典类型详情
| 字典类型 | 状态 | 字典项 |
|---------|------|--------|
| sys_enable_status | 已存在 | 0-停用, 1-正常 |
| mall_coupon_type | 新建（✅已同步前端枚举） | 1-满减券, 2-折扣券, 3-兑换券 |

### 字段配置详情
| 字段 | 类型 | 控件 | 查询方式 | 字典类型 |
|------|------|------|---------|---------|
| coupon_name | String | input | LIKE | - |
| coupon_type | String | select | EQ | mall_coupon_type |
| discount_amount | BigDecimal | numberInput | EQ | - |
| start_time | Date | datetime | BETWEEN | - |
| status | String | radio | EQ | sys_enable_status |
| ... | ... | ... | ... | ... |

---

## 🚀 下一步：前往前端生成代码

1. 访问：http://localhost
2. 进入：【系统工具】→【代码生成】
3. 找到表：`m_coupon`
4. （可选）点击【编辑】微调配置
5. 点击【生成代码】→ 完成！
   - ✅ 代码自动生成到项目路径
   - ✅ 菜单自动导入到数据库
   - ✅ 权限自动关联

---

## 📝 说明

- ✅ 所有字段已智能推断配置，无需手动调整
- ✅ 上级菜单和顺序已自动配置完成
- ✅ 生成代码时菜单会自动导入（autoImportMenu=1）
- ⚠️ 生成代码后需重启后端服务
- ⚠️ 首次使用记得在【菜单管理】中分配权限
```

---

### 第八步：联动推荐（开发完成后）

#### 8.1 推荐代码规范检查

```markdown
💡 推荐：运行 `/check` 检查生成的代码是否符合项目规范
```

#### 8.2 询问任务跟踪关联（⚠️ 必须询问用户，不能自动执行！）

**用户明确说 "dev 询问用户后再关联"，因此此步骤必须征得用户同意**。

输出提示：
```markdown
📋 是否需要创建/更新任务跟踪文档？

1. **创建新任务** — 在 docs/tasks/active/ 创建此功能的任务跟踪文档
2. **更新现有任务** — 将此功能标记为已完成步骤（需要先有活跃任务）
3. **不需要** — 跳过任务关联
```

**处理逻辑**：

| 用户选择 | 操作 |
|---------|------|
| 创建新任务 | 调用 task-tracker 技能，创建 `docs/tasks/active/` 下的任务文档 |
| 更新现有任务 | 读取活跃任务文档，标记相关步骤完成，更新进度百分比 |
| 不需要 | 结束，不做任何操作 |

**强制规则**：
- ❌ **禁止自动创建任务文档**
- ❌ **禁止未经确认就修改任务文档**
- ✅ **必须明确询问用户意愿**
- ✅ **用户说"不需要"就立即结束**

---

## 📌 组件路径（componentPath）计算规则

> **重要**：组件路径由代码生成器在 `VelocityUtils.getComponentPath()` 中自动计算，dev 指令**不直接创建 C 类型菜单**，但需要理解路径规则以确保配置正确。

### 计算公式

```
componentPath = {package_name 去掉 "plus.ruoyi" 前缀后的路径}/{businessName}/{businessName}
```

**源码逻辑**（`VelocityUtils.java`）：
```java
// 1. 从 package_name 中提取前端目录路径（跳过前两级 plus.ruoyi）
getFrontendPath("plus.ruoyi.business.base") → "business/base"

// 2. 拼接 businessName
getComponentPath() → "business/base" + "/" + "ad" + "/" + "ad" → "business/base/ad/ad"

// 3. 树表模板添加 Tree 后缀
getComponentPath(tplCategory="tree") → "business/base/category/categoryTree"
```

### 各模块路径对照表

| package_name | module_name | business_name | tpl | componentPath | Vue 文件 |
|-------------|-------------|---------------|-----|---------------|---------|
| `plus.ruoyi.business.base` | `base` | `ad` | crud | `business/base/ad/ad` | `views/business/base/ad/ad.vue` |
| `plus.ruoyi.business.mall` | `mall` | `coupon` | crud | `business/mall/coupon/coupon` | `views/business/mall/coupon/coupon.vue` |
| `plus.ruoyi.business.iot` | `iot` | `device` | crud | `business/iot/device/device` | `views/business/iot/device/device.vue` |
| `plus.ruoyi.business.base` | `base` | `category` | tree | `business/base/category/categoryTree` | `views/business/base/category/categoryTree.vue` |

### 关键约束

1. **dev 指令不创建 C 类型菜单 SQL** — 业务菜单由 `autoImportMenu=1` 在代码生成时自动导入
2. **dev 指令只创建 M 类型（目录）菜单** — `component = NULL`，无组件路径
3. **组件路径完全由 `package_name` 和 `business_name` 决定** — 确保 `sys_gen_table` 中这两个字段正确即可

---

## 🚨 AI 强制执行规则

### 流程控制
1. ✅ **第一步必须询问生成模式（直接生成代码 / 生成配置）**
2. ✅ **仅在第五步确认一次，其他步骤自动执行**
3. ✅ **第二步必须检查功能是否存在**
4. ❌ **禁止多次询问用户确认**
5. ❌ **禁止参考其他框架**

### 模式分支规则
6. ✅ **模式A（直接生成代码）：第七步读取参考代码 → 直接编写全部后端+前端文件 → 插入菜单SQL**
7. ✅ **模式B（生成配置）：第七步只输出配置完成报告，引导用户前往前端手动生成**
8. ✅ **模式A 必须先读参考代码（Ad 模块），严格按照相同风格编写**
9. ❌ **禁止在模式A中跳过读取参考代码直接编写（必须先 Read 再 Write）**

### 代码规范
5. ✅ **包名必须是 `plus.ruoyi.*`**
6. ✅ **必须遵循智能字段命名规则**
7. ✅ **必须从 icons.d.ts 智能匹配图标**

### 默认值设置（重要）⭐⭐⭐
8. ✅ **tenant_id 默认值必须是 '000000'**
9. ✅ **status 默认值必须是 '1'（正常）**
10. ✅ **is_xxx 默认值必须是 '1'（是）**
11. ✅ **is_deleted 默认值必须是 '0'（未删除）**

### 菜单配置
12. ✅ **必须从第三步查询结果动态获取上级菜单ID（禁止硬编码），如不存在则自动创建**
13. ✅ **必须自动计算菜单顺序（MAX + 10）**
14. ✅ **不生成业务菜单SQL（框架自动导入）**

### 字典配置
15. ✅ **必须检查字典类型是否已存在**
16. ✅ **系统内置字典无需创建（sys_enable_status、sys_boolean_flag 等）**
17. ✅ **业务字典不存在时自动创建（字典类型+字典数据）**
18. ✅ **字典数据必须包含合理的选项（至少2-3个）**
19. ✅ **新建字典类型后，必须同步到前端 DictTypes 枚举**⭐⭐⭐⭐⭐

### 前端字典枚举同步
20. ✅ **新建字典必须添加到 plus-ui/src/composables/useDict.ts 的 DictTypes 枚举**
21. ✅ **枚举格式：`/** [字典名称] */ [字典类型] = '[字典类型]'`**
22. ✅ **枚举位置：按模块分组（sys_*/base_*/mall_*/iot_*/crm_*）**
23. ✅ **枚举最后一项必须添加逗号（以便后续插入）**

### 代码生成配置
24. ✅ **gen_type 必须设为 '1'（自定义路径）**
25. ✅ **gen_path 必须设为 '/'**
26. ✅ **options 必须包含 parentMenuId、menuIcon、menuOrder、autoImportMenu**

### 列配置（column_default 字段）⭐⭐⭐
27. ✅ **column_default 必须包含在 INSERT 语句中**
28. ✅ **无默认值的字段：column_default = undefined（不是空字符串 ''）**
29. ✅ **tenant_id：column_default = '''000000'''**（VARCHAR类型，需要三重引号！）
30. ✅ **status（CHAR类型）：column_default = '''1'''**（三重引号！）
31. ✅ **is_xxx（CHAR类型）：column_default = '''1'''**（三重引号！）
32. ✅ **is_deleted（CHAR类型）：column_default = '''0'''**（三重引号！）
33. ✅ **字典字段（VARCHAR/CHAR类型）：column_default = '''字典值'''**（三重引号！例如：'''NORMAL'''、'''DOG'''）
34. ✅ **数值字段（INT/BIGINT/DECIMAL）：column_default = '数值'**（单引号，例如：'0'、'0.00'）
35. ⚠️ **重要：字符串类型字段的默认值必须用三重引号，否则代码生成会报错！详见上面的"column_default 默认值格式化规则"**

### 租户字段特殊规则（⭐⭐⭐⭐⭐重要）
36. ✅ **tenant_id 的所有权限必须设为 '0'：is_insert=0, is_edit=0, is_list=0, is_query=0**
37. ✅ **原因：租户ID由TenantEntity自动注入，框架自动处理租户隔离，无需任何手动操作**
38. ✅ **虽然建表时包含 tenant_id 字段，但代码生成配置中完全不参与任何业务逻辑**

### 时间字段
39. ✅ **sys_gen_table 的 create_time 和 update_time 都设为 NOW()**
40. ✅ **sys_gen_table_column 的 create_time 和 update_time 都设为 NOW()**
41. ✅ **sys_dict_type 的 create_time 和 update_time 都设为 NOW()**
42. ✅ **sys_dict_data 的 create_time 和 update_time 都设为 NOW()**

### 数据库连接
43. ✅ **数据库连接信息必须从 application-dev.yml 动态读取**
44. ❌ **禁止硬编码数据库名（如 ryplus_uni）**

### 执行规则
45. ✅ **自动执行所有SQL，无需用户操作**
46. ✅ **新建字典后，自动更新前端 DictTypes 枚举**

### 任务跟踪关联（⚠️ 必须询问用户）
47. ✅ **开发前自动检查 docs/tasks/active/ 中是否有相关活跃任务**
48. ❌ **禁止自动创建/修改任务跟踪文档**
49. ✅ **开发完成后必须询问用户是否需要创建/更新任务文档**
50. ✅ **用户拒绝则立即结束，不再追问**

### 联动推荐
51. ✅ **开发完成后推荐运行 `/check` 检查代码规范**

---

## 📖 字典类型命名规范

### 系统内置字典（无需创建）

| 字典类型 | 字典名称 | 用途 | 说明 |
|---------|---------|------|------|
| `sys_enable_status` | 启用状态 | status 字段 | 0-停用, 1-正常 |
| `sys_boolean_flag` | 逻辑标志 | is_xxx 字段 | 0-否, 1-是 |
| `sys_user_gender` | 用户性别 | gender 字段 | 0-男, 1-女, 2-未知 |
| `sys_data_scope` | 数据权限 | 权限配置 | 1-全部, 2-自定义等 |
| `sys_audit_status` | 审核状态 | 审核功能 | 0-待审核, 1-通过, 2-拒绝 |
| `sys_platform_type` | 平台类型 | 平台配置 | 1-微信, 2-支付宝等 |

### 业务字典命名规则

**格式**: `[模块前缀]_[业务对象]_[字段含义]`

**示例**：

| 模块 | 字段 | 字典类型 | 字典名称 | 选项示例 |
|------|------|---------|---------|---------|
| mall | coupon_type | `mall_coupon_type` | 优惠券类型 | 1-满减券, 2-折扣券, 3-兑换券 |
| mall | order_status | `mall_order_status` | 订单状态 | 1-待付款, 2-已付款, 3-已发货, 4-已完成 |
| iot | device_status | `iot_device_status` | 设备状态 | 1-在线, 2-离线, 3-故障 |
| iot | sensor_type | `iot_sensor_type` | 传感器类型 | 1-温度, 2-湿度, 3-烟雾 |
| crm | customer_level | `crm_customer_level` | 客户等级 | 1-普通, 2-VIP, 3-SVIP |
| crm | contact_status | `crm_contact_status` | 联系状态 | 1-未联系, 2-已联系, 3-已成交 |

### 字典创建模板

```sql
-- 1. 创建字典类型
INSERT INTO sys_dict_type (
    dict_id, tenant_id, dict_name, dict_type, status,
    create_dept, create_by, create_time, update_time, remark
) VALUES (
    [新dict_id], '000000', '[字典名称]', '[字典类型]', '0',
    100, 1, NOW(), NOW(), '[模块名][功能名][字典说明]'
);

-- 2. 批量创建字典数据
INSERT INTO sys_dict_data (
    dict_data_id, tenant_id, dict_sort, dict_label, dict_value, dict_type,
    css_class, list_class, is_default, status,
    create_dept, create_by, create_time, update_time, remark
) VALUES
([新dict_data_id], '000000', 1, '[选项名称]', '1', '[字典类型]', '', 'default', 'N', '0', 100, 1, NOW(), NOW(), ''),
([新dict_data_id+1], '000000', 2, '[选项名称]', '2', '[字典类型]', '', 'default', 'N', '0', 100, 1, NOW(), NOW(), ''),
([新dict_data_id+2], '000000', 3, '[选项名称]', '3', '[字典类型]', '', 'default', 'N', '0', 100, 1, NOW(), NOW(), '');
```

### AI 检查流程

1. **提取字段字典需求**：从表结构设计中识别需要字典的字段
2. **区分系统/业务字典**：判断是否为系统内置字典
3. **检查字典是否存在**：查询 sys_dict_type 表
4. **生成字典数据**：为不存在的业务字典生成合理的选项（2-5个）

---

## 📋 上级菜单映射规则

AI 必须根据模块自动判断归属的上级菜单：

| 模块名 | 上级菜单 | menu_id | 菜单顺序起始 | 说明 |
|-------|---------|---------|-------------|------|
| **base** | APP配置 | 从第三步查询获取 | 从现有最大+10 | 基础配置类功能 |
| **mall** | 商城管理 | 从第三步查询获取 | 从现有最大+10 | 商城业务功能 |
| **iot** | 物联网管理 | 查询获取（如不存在则自动创建） | 91 | 物联网相关功能 |
| **crm** | CRM管理 | 查询获取（如不存在则自动创建） | 92 | 客户关系管理 |
| **marketing** | 营销管理 | 查询获取（如不存在则自动创建） | 93 | 营销活动功能 |
| **其他** | [模块名]管理 | 自动创建 | 94+ | 自定义模块 |

### 上级菜单创建模板

当上级菜单不存在时，使用以下模板创建：

```sql
-- 列顺序与 sql.vm 模板保持一致
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_external_link, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES (
    [新menu_id],           -- 自增ID
    '[模块名]管理',         -- 如：物联网管理
    0,                     -- parent_id = 0 (顶级菜单)
    [order_num],          -- 91、92、93... 递增
    '[module]Manage',      -- 如：iotManage
    NULL,                  -- component为空（M类型目录无组件）
    '0',                   -- is_external_link = '0' (非外链)
    '1',                   -- is_cache = '1' (缓存)
    'M',                   -- menu_type = M (目录)
    '1',                   -- visible = 1 (显示)
    '1',                   -- status = 1 (正常)
    '',                    -- perms为空
    '[icon]',              -- 如：iot（小写模块名）
    100,                   -- create_dept
    1,                     -- create_by
    NOW(),                 -- create_time
    NULL,                  -- update_by
    NULL,                  -- update_time
    '[模块名]管理目录'      -- remark
);
```

### 菜单顺序计算规则

```sql
-- 查询上级菜单下最大的 order_num
SELECT MAX(order_num) as max_order
FROM sys_menu
WHERE parent_id = [上级菜单ID] AND menu_type = 'C';

-- 新菜单顺序 = MAX(order_num) + 10
-- 如果 MAX(order_num) 为 NULL，则从 1 开始
```

---

## 📌 主子表代码生成完整指南

### 什么是主子表？

主子表模式用于处理**一对多**的业务场景，例如：
- **订单与订单明细**：一个订单包含多个商品明细
- **问卷与问题**：一个问卷包含多个问题
- **合同与合同条款**：一个合同包含多个条款

### 主子表的数据库结构

**关键特征**：
- 子表有外键字段关联主表
- 主表和子表都需要单独建表
- 两个表都需要在代码生成器中配置

**示例：订单与订单明细**

```sql
-- 主表：订单表
CREATE TABLE m_order (
    id              BIGINT(20)   NOT NULL COMMENT '订单ID',
    tenant_id       VARCHAR(20)  DEFAULT '000000' COMMENT '租户ID',
    order_no        VARCHAR(50)  NOT NULL COMMENT '订单编号',
    total_amount    DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    status          CHAR(1)      DEFAULT '1' COMMENT '订单状态',
    -- 审计字段...
    create_dept     BIGINT(20)   DEFAULT NULL COMMENT '创建部门',
    create_by       BIGINT(20)   DEFAULT NULL COMMENT '创建人',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by       BIGINT(20)   DEFAULT NULL COMMENT '更新人',
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    is_deleted      CHAR(1)      DEFAULT '0' COMMENT '是否删除',
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='订单表';

-- 子表：订单明细表
CREATE TABLE m_order_item (
    id              BIGINT(20)   NOT NULL COMMENT '明细ID',
    tenant_id       VARCHAR(20)  DEFAULT '000000' COMMENT '租户ID',
    order_id        BIGINT(20)   NOT NULL COMMENT '订单ID（外键）', -- ⚠️ 关联主表
    goods_name      VARCHAR(100) NOT NULL COMMENT '商品名称',
    quantity        INT(11)      NOT NULL COMMENT '商品数量',
    price           DECIMAL(10,2) NOT NULL COMMENT '商品单价',
    -- 审计字段...
    create_dept     BIGINT(20)   DEFAULT NULL COMMENT '创建部门',
    create_by       BIGINT(20)   DEFAULT NULL COMMENT '创建人',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by       BIGINT(20)   DEFAULT NULL COMMENT '更新人',
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    is_deleted      CHAR(1)      DEFAULT '0' COMMENT '是否删除',
    PRIMARY KEY (id),
    KEY idx_order_id (order_id)  -- ⚠️ 外键索引
) ENGINE=InnoDB COMMENT='订单明细表';
```

### 主子表代码生成步骤

#### 步骤1：先生成子表（单表模式）

```bash
# 1. 创建子表
CREATE TABLE m_order_item (...);

# 2. 为子表插入代码生成配置
INSERT INTO sys_gen_table (
    table_id, data_name, table_name, table_comment,
    class_name, tpl_category, package_name, module_name, business_name,
    function_name, function_author, gen_type, gen_path, options,
    create_dept, create_by, create_time, update_time
) VALUES (
    [自增ID], 'master', 'm_order_item', '订单明细表',
    'OrderItem', 'crud',  -- ⚠️ 子表使用 crud 模板，不是 sub
    'plus.ruoyi.business.mall', 'mall', 'orderItem',
    '订单明细', '抓蛙师', '1', '/',
    '{"parentMenuId":"[查询到的上级菜单ID]","menuIcon":"list","menuOrder":"30","autoImportMenu":"1"}',
    100, 1, NOW(), NOW()
);

# 3. 插入子表的列配置（略，按标准字段配置规则）
INSERT INTO sys_gen_table_column (...) VALUES (...);
```

**⚠️ 重要**：子表配置为 **crud 单表模式**，并且**需要配置菜单**！

#### 步骤2：再生成主表（主子表模式）

```bash
# 1. 创建主表
CREATE TABLE m_order (...);

# 2. 为主表插入代码生成配置
INSERT INTO sys_gen_table (
    table_id, data_name, table_name, table_comment,
    sub_table_name, sub_table_fk_name,  -- ⚠️ 关键：关联子表信息
    class_name, tpl_category, package_name, module_name, business_name,
    function_name, function_author, gen_type, gen_path, options,
    create_dept, create_by, create_time, update_time
) VALUES (
    [自增ID], 'master', 'm_order', '订单表',
    'm_order_item', 'order_id',  -- ⚠️ 子表名和外键字段名
    'Order', 'sub',  -- ⚠️ 主表使用 sub 模板
    'plus.ruoyi.business.mall', 'mall', 'order',
    '订单', '抓蛙师', '1', '/',
    '{"parentMenuId":"[查询到的上级菜单ID]","menuIcon":"shopping-cart","menuOrder":"20","autoImportMenu":"1"}',
    100, 1, NOW(), NOW()
);

# 3. 插入主表的列配置（略，按标准字段配置规则）
INSERT INTO sys_gen_table_column (...) VALUES (...);
```

### 主子表的菜单结构

**菜单配置原则**：
1. **子表需要独立菜单**（用于独立管理）
2. **主表菜单**包含主子表的所有操作权限

**生成的菜单结构**：

```
商城管理/
├── 订单管理/                    ← 主表菜单（menuOrder: 20）
│   ├── 订单查询                 ← 主表权限
│   ├── 订单新增                 ← 新增订单时可同时新增明细
│   ├── 订单修改                 ← 修改订单时可同时修改明细
│   ├── 订单删除                 ← 删除订单时级联删除明细
│   ├── 订单导出
│   └── 订单导入
├── 订单明细管理/                ← 子表独立菜单（menuOrder: 30）
│   ├── 明细查询
│   ├── 明细新增
│   ├── 明细修改
│   ├── 明细删除
│   ├── 明细导出
│   └── 明细导入
```

### 主子表的配置关键点

| 配置项 | 主表 | 子表 |
|-------|------|------|
| `tpl_category` | `sub` | `crud` |
| `sub_table_name` | 子表名（如`m_order_item`） | NULL 或不填 |
| `sub_table_fk_name` | 外键字段（如`order_id`） | NULL 或不填 |
| 菜单配置 | ✅ 需要配置 | ✅ 需要配置 |
| 菜单图标 | 主业务图标（如`shopping-cart`） | 子业务图标（如`list`） |
| 菜单顺序 | 较小值（如`20`） | 较大值（如`30`） |

### 主子表的前端交互

生成后的前端页面：

1. **主表页面**（`order.vue`）：
   - 包含主表的列表、新增、编辑、删除功能
   - 在新增/编辑弹窗中**嵌入子表组件**
   - 可以同时操作主表数据和子表数据

2. **子表组件**（`OrderItemChild.vue`）：
   - 作为子组件嵌入到主表页面中
   - 在主表新增/编辑时可以动态添加/删除子表行
   - 绑定到主表的外键字段（`props.parentId`）

3. **子表独立页面**（`orderItem.vue`）：
   - 可以单独管理所有订单明细
   - 可以查询、新增、修改、删除明细记录

### /dev 指令中的主子表生成

当用户要求开发主子表功能时，AI 必须：

1. **识别主子表场景**：
   - 用户提到"订单明细"、"问题列表"、"条款"等关键词
   - 描述中包含"一个xxx包含多个xxx"的表述

2. **询问确认**：
   ```
   您要开发的功能涉及一对多关系吗？
   - 主表：xxx（一条记录）
   - 子表：xxx（多条明细记录）
   - 外键字段：xxx_id
   ```

3. **生成两个表的配置**：
   - **先生成子表**（crud模式 + 独立菜单，menuOrder较大）
   - **再生成主表**（sub模式 + 关联配置，menuOrder较小）

4. **配置关联信息**：
   ```sql
   -- 主表配置中必须包含
   sub_table_name = '子表名',
   sub_table_fk_name = '外键字段名'
   ```

5. **输出完整说明**：
   ```markdown
   ✅ 主子表配置完成！

   **主表**：m_order（订单表）
   - 模板类型：sub（主子表）
   - 关联子表：m_order_item
   - 外键字段：order_id
   - 菜单：订单管理（menuOrder: 20）

   **子表**：m_order_item（订单明细表）
   - 模板类型：crud（单表）
   - 外键字段：order_id → m_order.id
   - 菜单：订单明细管理（menuOrder: 30）

   **前端生成后**：
   - 订单页面可以同时编辑订单和明细
   - 订单明细也可以独立管理
   ```

### 常见错误避免

**❌ 错误1：子表配置为 `sub` 模板**
```sql
-- 错误！子表应该是 crud
tpl_category = 'sub'
```
✅ **正确**：子表配置为 `crud` 模板
```sql
tpl_category = 'crud'
```

---

**❌ 错误2：子表不配置菜单**
```sql
-- 错误！子表也需要独立菜单
options = '{}'  -- 缺少菜单配置
```
✅ **正确**：子表也需要配置菜单
```sql
options = '{"parentMenuId":"xxx","menuIcon":"list","menuOrder":"30","autoImportMenu":"1"}'
```

---

**❌ 错误3：主表未关联子表**
```sql
-- 错误！缺少关联信息
sub_table_name = NULL,
sub_table_fk_name = NULL
```
✅ **正确**：主表必须配置关联信息
```sql
sub_table_name = 'm_order_item',
sub_table_fk_name = 'order_id'
```

---

**❌ 错误4：菜单顺序设置不合理**
```sql
-- 错误！主表 menuOrder 应该小于子表
主表 menuOrder = 30
子表 menuOrder = 20
```
✅ **正确**：主表在前，子表在后
```sql
主表 menuOrder = 20  -- 较小，显示在前
子表 menuOrder = 30  -- 较大，显示在后
```

---

**❌ 错误5：外键字段配置错误**
```sql
-- 错误！外键字段名必须是子表中的字段名，不是主表字段
sub_table_fk_name = 'id'  -- 主表的字段
```
✅ **正确**：外键字段是子表中关联主表的字段
```sql
sub_table_fk_name = 'order_id'  -- 子表中的外键字段
```

---

## 📌 树表代码生成完整指南

### 什么是树表？

树表模式用于处理**层级关系**的业务场景，例如：
- **部门管理**：部门有上下级关系
- **分类管理**：分类有父子关系
- **地区管理**：省市区层级结构
- **菜单管理**：菜单有父子层级

### 树表的数据库结构

**关键特征**：
- 表中有自关联的父ID字段（`parent_id`）
- 通过 `parent_id` 构建树形层级
- 顶级节点的 `parent_id` 通常为 `0`

**示例：分类管理**

```sql
CREATE TABLE b_category (
    id              BIGINT(20)   NOT NULL COMMENT '分类ID',
    tenant_id       VARCHAR(20)  DEFAULT '000000' COMMENT '租户ID',
    parent_id       BIGINT(20)   DEFAULT 0 COMMENT '父分类ID',      -- ⚠️ 树形关键字段
    category_name   VARCHAR(100) NOT NULL COMMENT '分类名称',        -- ⚠️ 树节点显示名称
    sort_order      INT(11)      DEFAULT 0 COMMENT '排序',
    status          CHAR(1)      DEFAULT '1' COMMENT '状态',
    -- 审计字段...
    create_dept     BIGINT(20)   DEFAULT NULL COMMENT '创建部门',
    create_by       BIGINT(20)   DEFAULT NULL COMMENT '创建人',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by       BIGINT(20)   DEFAULT NULL COMMENT '更新人',
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    is_deleted      CHAR(1)      DEFAULT '0' COMMENT '是否删除',
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id)  -- ⚠️ 父ID索引
) ENGINE=InnoDB COMMENT='分类表';
```

### 树表代码生成配置

```sql
INSERT INTO sys_gen_table (
    table_id, data_name, table_name, table_comment,
    class_name, tpl_category,  -- ⚠️ tree 模板类型
    package_name, module_name, business_name,
    function_name, function_author, gen_type, gen_path, options,
    create_dept, create_by, create_time, update_time
) VALUES (
    [自增ID], 'master', 'b_category', '分类表',
    'Category', 'tree',  -- ⚠️ 使用 tree 模板
    'plus.ruoyi.business.base', 'base', 'category',
    '分类', '抓蛙师', '1', '/',
    -- ⚠️ options 必须包含树表三个关键字段
    '{"treeCode":"id","treeParentCode":"parentId","treeName":"categoryName","parentMenuId":"[查询到的上级菜单ID]","menuIcon":"folder","menuOrder":"10","autoImportMenu":"1"}',
    100, 1, NOW(), NOW()
);
```

### 树表 options 必填字段

| 字段 | 说明 | 示例 | 重要提示 |
|------|------|------|---------|
| `treeCode` | 树节点ID字段 | `id` | 必须是 Java 驼峰命名 |
| `treeParentCode` | 父节点ID字段 | `parentId` | 必须是 Java 驼峰命名（不是 `parent_id`） |
| `treeName` | 节点显示名称字段 | `categoryName` | 必须是 Java 驼峰命名 |

### 树表的前端交互

生成后的前端页面（`xxxTree.vue`）：

1. **树形表格展示**：
   - 使用 `el-table` 的 `row-key` 和 `tree-props` 实现树形展示
   - 默认展开所有节点
   - 支持展开/折叠切换按钮

2. **新增节点**：
   - 点击顶部"新增"按钮：创建顶级节点（`parent_id = 0`）
   - 点击行内"新增"按钮：创建当前节点的子节点

3. **父节点选择**：
   - 表单中使用 `AFormTreeSelect` 组件选择父节点
   - 自动构建下拉树结构

### /dev 指令中的树表生成

当用户要求开发树表功能时，AI 必须：

1. **识别树表场景**：
   - 用户提到"分类"、"部门"、"层级"、"树形"等关键词
   - 描述中包含"上下级"、"父子关系"的表述

2. **询问确认**：
   ```
   您要开发的功能是树形层级结构吗？
   - 节点ID字段：id
   - 父节点字段：parent_id
   - 显示名称字段：xxx_name
   ```

3. **配置 options**：
   ```sql
   options = '{"treeCode":"id","treeParentCode":"parentId","treeName":"categoryName",...}'
   ```

4. **输出完整说明**：
   ```markdown
   ✅ 树表配置完成！

   **表名**：b_category（分类表）
   - 模板类型：tree（树表）
   - 树节点ID：id
   - 父节点字段：parentId
   - 显示名称：categoryName

   **前端生成后**：
   - 树形表格展示，支持展开/折叠
   - 新增时可选择父节点
   - 支持在任意节点下新增子节点
   ```

### 常见错误避免

**❌ 错误1：options 中字段名使用下划线**
```sql
-- 错误！应该使用驼峰命名
"treeParentCode":"parent_id"
```
✅ **正确**：使用 Java 驼峰命名
```sql
"treeParentCode":"parentId"
```

---

**❌ 错误2：缺少树表必填字段**
```sql
-- 错误！缺少 treeCode、treeParentCode、treeName
options = '{"parentMenuId":"xxx"}'
```
✅ **正确**：必须包含三个树表字段
```sql
options = '{"treeCode":"id","treeParentCode":"parentId","treeName":"categoryName","parentMenuId":"xxx",...}'
```

---

**❌ 错误3：表中没有父ID字段**
```sql
-- 错误！树表必须有自关联的父ID字段
CREATE TABLE b_category (
    id BIGINT(20) NOT NULL,
    category_name VARCHAR(100) NOT NULL
    -- 缺少 parent_id 字段！
);
```
✅ **正确**：必须有父ID字段
```sql
CREATE TABLE b_category (
    id BIGINT(20) NOT NULL,
    parent_id BIGINT(20) DEFAULT 0 COMMENT '父分类ID',  -- 必须有
    category_name VARCHAR(100) NOT NULL
);
```

---

**❌ 错误4：模板类型选错**
```sql
-- 错误！树表应该用 tree 模板
tpl_category = 'crud'
```
✅ **正确**：使用 tree 模板
```sql
tpl_category = 'tree'
```

---

## 📖 参考文档

- `.claude/docs/数据库设计规范.md`
- `CLAUDE.md`
