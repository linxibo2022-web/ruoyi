# /crud - 快速生成 CRUD 代码

作为 CRUD 代码生成助手，基于已存在的数据库表快速生成标准 CRUD 代码。

## 🎯 适用场景

### ✅ 适合使用 `/crud` 的情况

- ✅ **数据库表已存在** - 表结构已设计完毕
- ✅ **只需标准 CRUD** - 增删改查、导入导出等标准功能
- ✅ **无复杂业务逻辑** - 没有特殊的业务规则
- ✅ **快速原型开发** - 需要快速搭建基础功能
- ✅ **树形结构数据** - 部门、分类等层级数据（tree 模板）
- ✅ **主子表关联** - 订单-明细等一对多关系（sub 模板）

### ❌ 不适合使用 `/crud` 的情况

- ❌ **表结构尚未设计** → 请使用 `/dev` 命令
- ❌ **需要复杂业务逻辑** → 请使用 `/dev` 命令后手动增强
- ❌ **需要特殊的查询条件** → 请使用 `/dev` 命令后手动修改
- ❌ **需要自定义接口** → 请使用 `/dev` 命令

### 📋 支持的模板类型

| 模板类型 | 适用场景 | 特点 |
|---------|---------|------|
| **crud** | 普通表 | 标准增删改查、分页列表 |
| **tree** | 树形表 | 父子层级、展开折叠、无分页 |
| **sub** | 主子表 | 一对多关联、联动保存删除 |

## 📋 执行流程

### 第一步：连接数据库并查看表结构

#### 1.1 询问用户

```
请提供表名：
（如：b_feedback, m_goods_category）

💡 数据库连接信息将从 application-dev.yml 自动读取
```

#### 1.2 读取数据库配置（强制执行）⭐⭐⭐⭐⭐

```bash
# 读取开发环境配置文件
Read ruoyi-admin/src/main/resources/application-dev.yml
```

从配置文件中提取以下信息（注意环境变量和默认值）：

```yaml
spring:
  datasource:
    dynamic:
      datasource:
        master:
          url: jdbc:mysql://${DB_HOST:127.0.0.1}:${DB_PORT:3306}/${DB_NAME:ryplus_uni}?...
          username: ${DB_USERNAME:root}
          password: ${DB_PASSWORD:root}
```

**解析规则**：
- 格式：`${环境变量:默认值}`
- 主机：`DB_HOST` 默认 `127.0.0.1`
- 端口：`DB_PORT` 默认 `3306`
- 数据库名：`DB_NAME` 默认 `ryplus_uni`
- 用户名：`DB_USERNAME` 默认 `root`
- 密码：`DB_PASSWORD` 默认 `root`

**⚠️ 注意**：不要输出数据库连接信息给用户确认，直接使用读取到的配置连接数据库

#### 1.3 连接数据库并查看表结构

```bash
# 使用解析出的配置连接数据库
mysql -h [主机] -P [端口] -u [用户名] -p[密码] [数据库名]

# 查看表结构
SHOW CREATE TABLE [表名];

# 查看字段详情
DESC [表名];

# 查询最大菜单ID
SELECT MAX(menu_id) FROM sys_menu;
```

#### 1.4 输出表结构分析

```markdown
## 📊 表结构分析

**表名**：[表名]
**注释**：[表注释]

**字段列表**：
| 字段名 | 类型 | 是否必填 | 默认值 | 注释 |
|--------|------|---------|--------|------|
| id | BIGINT(20) | 是 | - | 主键ID |
| tenant_id | VARCHAR(20) | 否 | '000000' | 租户ID |
| name | VARCHAR(100) | 是 | - | 名称 |
| status | CHAR(1) | 否 | '1' | 状态 |
| create_dept | BIGINT(20) | 否 | NULL | 创建部门 |
| create_by | BIGINT(20) | 否 | NULL | 创建人 |
| create_time | DATETIME | 否 | CURRENT_TIMESTAMP | 创建时间 |
| update_by | BIGINT(20) | 否 | NULL | 修改人 |
| update_time | DATETIME | 否 | CURRENT_TIMESTAMP | 更新时间 |
| remark | VARCHAR(255) | 否 | NULL | 备注 |
| is_deleted | CHAR(1) | 否 | '0' | 是否删除 |

**索引设计**：
- PRIMARY KEY: id
- 普通索引: status, create_time

**审计字段**：✅ 完整（包含 create_dept, create_by, create_time, update_by, update_time, remark）
**逻辑删除**：✅ 已配置（is_deleted）
**租户支持**：✅ 已支持（tenant_id）

---

### 提取功能名称

根据表名 `b_feedback` 提取功能名称：
- 中文名：反馈
- 英文名：Feedback
- 类名前缀：Feedback
- 接口路径：/base/feedback

确认功能名称，或自定义修改？
```

---

### 第 1.5 步：选择模板类型（新增）⭐⭐⭐⭐⭐

根据表结构特征，询问用户选择模板类型：

```
## 🎯 请选择模板类型

根据您的表结构，请选择合适的模板类型：

1. **crud** - 普通表（默认）
   适用于：标准增删改查，无层级关系

2. **tree** - 树表
   适用于：有父子层级关系的数据（如部门、分类、菜单）
   特征：表中包含 parent_id 或类似的父级字段

3. **sub** - 主子表
   适用于：一对多关系（如订单-订单明细、文章-评论）
   特征：子表通过外键关联主表

请输入模板类型（crud/tree/sub）：
```

#### 1.5.1 树表自动检测

如果表结构包含以下字段，自动提示可能是树表：
- `parent_id` / `pid` / `parent` - 父级ID
- `ancestors` - 祖级列表
- `order_num` / `sort` - 排序字段

```
💡 检测到您的表可能是树表结构：
- 发现父级字段：parent_id
- 发现排序字段：order_num

是否使用树表模板？(Y/n)
```

#### 1.5.2 主子表自动检测

如果用户提供了多个表名，自动识别为主子表：

```
💡 检测到多表结构，判断为主子表：
- 主表：m_order
- 子表：m_order_item（通过 order_id 关联）

是否使用主子表模板？(Y/n)
```

---

### 第 1.6 步：树表配置（仅 tree 模板）

当用户选择 tree 模板时，询问以下配置：

```
## 🌳 树表配置

请确认或修改以下配置：

1. **树编码字段**（treeCode）
   用于构建树结构的主键字段
   默认检测：id

2. **树父编码字段**（treeParentCode）
   父节点的关联字段
   检测到：parent_id

3. **树名称字段**（treeName）
   在树节点上显示的名称字段
   检测到：name（或 category_name）

确认配置？(Y/n)
```

#### 树表配置示例

```json
{
  "treeCode": "id",
  "treeParentCode": "parentId",
  "treeName": "categoryName"
}
```

> **⚠️ 注意**：`treeParentCode` 和 `treeName` 必须使用 **Java 驼峰命名**（如 `parentId`），不是数据库下划线命名（如 ~~`parent_id`~~）。这些值对应 Entity 类中的 Java 字段名。

#### 树表代码生成差异

| 文件 | 普通表 | 树表 |
|------|--------|------|
| Entity | 标准字段 | + `@TableField(exist = false) List<Entity> children` |
| VO | 标准字段 | + `children` 子节点列表 |
| Service | `page()` 分页 | `listTree()` 树形列表 |
| Controller | `/pageXxxs` | `/listXxxTree` |
| 前端模板 | `index.vue.vm` | `index-tree.vue.vm` |

---

### 第 1.7 步：主子表配置（仅 sub 模板）

当用户选择 sub 模板时，询问以下配置：

```
## 📦 主子表配置

### 主表信息
- 主表名：m_order
- 主表实体：Order

### 子表信息
请确认或修改以下配置：

1. **子表名称**（subTableName）
   检测到：m_order_item

2. **子表外键**（subTableFkName）
   关联主表的外键字段
   检测到：order_id

确认配置？(Y/n)
```

#### 主子表代码生成差异

| 文件 | 普通表 | 主子表 |
|------|--------|--------|
| Entity | 无子表字段 | + `@TableField(exist = false) List<SubEntity> subList` |
| BO | 无子表字段 | + `List<SubBo> subList` |
| VO | 无子表字段 | + `List<SubVo> subList` |
| Service | 单表 CRUD | 主子表联动保存/删除 |
| Controller | 单表操作 | 返回主子表组合数据 |
| 前端模板 | `index.vue.vm` | `index.vue.vm` + `child.vue.vm` |

#### 主子表保存逻辑

```java
@Override
@Transactional(rollbackFor = Exception.class)
public Long add(OrderBo bo) {
    Order entity = MapstructUtils.convert(bo, Order.class);
    orderDao.insert(entity);

    // 保存子表
    if (CollUtil.isNotEmpty(bo.getSubList())) {
        for (OrderItemBo itemBo : bo.getSubList()) {
            OrderItem item = MapstructUtils.convert(itemBo, OrderItem.class);
            item.setOrderId(entity.getId());  // 设置外键
            orderItemDao.insert(item);
        }
    }

    return entity.getId();
}
```

---

### 第二步：生成菜单 SQL

#### 2.1 根据表名前缀确定模块名（强制执行）⭐⭐⭐⭐⭐

**根据表名前缀自动判断模块名**：

| 表名前缀 | 模块名 | 权限标识符格式 | 说明 |
|---------|--------|--------------|------|
| `b_` | `base` | `base:[功能名]:[操作]` | 基础业务模块 |
| `m_` | `mall` | `mall:[功能名]:[操作]` | 商城模块 |
| 其他 | 询问用户 | `[模块名]:[功能名]:[操作]` | 自定义模块 |

**示例**：
- 表名 `b_feedback` → 模块名 `base` → 权限 `base:feedback:view`
- 表名 `m_goods` → 模块名 `mall` → 权限 `mall:goods:view`

**⚠️ 注意**：不要输出给用户确认，直接使用判断结果

#### 2.2 询问菜单信息

```
请提供菜单配置信息：

1. **父菜单ID**：（默认：2000 - APP配置）
   - 2000 - APP配置
   - 2001 - 商城管理
   - 其他自定义

2. **排序值**：（默认：40，在现有菜单之后）

3. **菜单图标**：（默认：'file-text'）
   可选图标：message, form, list, file-text, database

4. **菜单ID起始值**：（默认：3000，避免冲突）
   当前最大菜单ID：[从数据库查询的结果]
   建议使用：[最大ID + 10]
```

#### 2.3 组件路径（componentPath）计算规则 ⭐⭐⭐⭐⭐

C 类型菜单的 `component` 字段值由以下公式计算（与代码生成器 `VelocityUtils.getComponentPath()` 保持一致）：

**公式**：

```
componentPath = {frontendPath}/{businessName}/{businessName}

其中：
  frontendPath = packageName 去掉前两级（plus.ruoyi）后用 "/" 连接
  businessName = 表名去掉前缀后的驼峰形式

树表特殊：componentPath 后加 "Tree" 后缀
```

**各模块对照表**：

| 模块 | packageName | frontendPath | 示例表 | componentPath |
|------|-------------|-------------|--------|---------------|
| base | `plus.ruoyi.business.base` | `business/base` | `b_feedback` | `business/base/feedback/feedback` |
| mall | `plus.ruoyi.business.mall` | `business/mall` | `m_goods` | `business/mall/goods/goods` |
| iot | `plus.ruoyi.business.iot` | `business/iot` | `iot_device` | `business/iot/device/device` |
| 树表 | `plus.ruoyi.business.base` | `business/base` | `b_category`（树） | `business/base/category/categoryTree` |

**对应前端 Vue 文件位置**：`plus-ui/src/views/{componentPath}.vue`

#### 2.4 生成菜单 SQL

根据用户提供的信息和模块名生成完整的菜单 SQL（7个权限）：

**⚠️ 注意**：使用步骤 2.1 判断的模块名替换 `[模块名]`，`create_dept` 固定为 `100`（与 sql.vm 模板一致）

```sql
-- 反馈管理菜单（示例：表名 b_feedback，模块名 base）
INSERT INTO sys_menu
VALUES (3010, '反馈管理', 2000, 40, 'feedback', 'business/base/feedback/feedback', NULL, '0', '1', 'C', '1', '1',
        'base:feedback:view', 'file-text', 100, 1, sysdate(), NULL, NULL, '反馈管理菜单');

INSERT INTO sys_menu
VALUES (3011, '反馈管理查询', 3010, 1, '#', '', NULL, '0', '1', 'F', '1', '1', 'base:feedback:query', '#', 100, 1,
        sysdate(), NULL, NULL, '');

INSERT INTO sys_menu
VALUES (3012, '反馈管理新增', 3010, 2, '#', '', NULL, '0', '1', 'F', '1', '1', 'base:feedback:add', '#', 100, 1,
        sysdate(), NULL, NULL, '');

INSERT INTO sys_menu
VALUES (3013, '反馈管理修改', 3010, 3, '#', '', NULL, '0', '1', 'F', '1', '1', 'base:feedback:update', '#', 100, 1,
        sysdate(), NULL, NULL, '');

INSERT INTO sys_menu
VALUES (3014, '反馈管理删除', 3010, 4, '#', '', NULL, '0', '1', 'F', '1', '1', 'base:feedback:delete', '#', 100, 1,
        sysdate(), NULL, NULL, '');

INSERT INTO sys_menu
VALUES (3015, '反馈管理导出', 3010, 5, '#', '', NULL, '0', '1', 'F', '1', '1', 'base:feedback:export', '#', 100, 1,
        sysdate(), NULL, NULL, '');

INSERT INTO sys_menu
VALUES (3016, '反馈管理导入', 3010, 6, '#', '', NULL, '0', '1', 'F', '1', '1', 'base:feedback:import', '#', 100, 1,
        sysdate(), NULL, NULL, '');
```

**菜单 SQL 将追加到**: `script/sql/ry_plus_app.sql`

---

### 第三步：生成后端代码

#### 3.1 学习现有代码（强制执行）

```bash
# 必须先阅读广告模块代码
Read ruoyi-modules/ruoyi-business/src/main/java/plus/ruoyi/business/base/service/impl/AdServiceImpl.java
Read ruoyi-modules/ruoyi-business/src/main/java/plus/ruoyi/business/base/dao/impl/AdDaoImpl.java
```

#### 3.2 生成代码顺序

按照以下顺序生成（参考 CLAUDE.md 中的标准模板）：

1. **Entity** - 继承 TenantEntity，字段从表结构映射
2. **BO** - 使用 @AutoMappers 注解
3. **VO** - 含 Excel 导出注解
4. **Mapper** - 只继承 BaseMapper
5. **DAO 接口和实现** - 实现 buildQueryWrapper 方法
6. **Service 接口和实现** - 标准 CRUD 方法
7. **Controller** - 标准接口 + 导入导出

#### 3.3 字段类型映射规则

| 数据库类型 | Java类型 | 说明 |
|-----------|---------|------|
| BIGINT(20) | Long | 主键、ID字段 |
| VARCHAR | String | 字符串 |
| CHAR | String | 字符 |
| TEXT | String | 长文本 |
| DATETIME | Date | 日期时间 |
| DECIMAL | BigDecimal | 金额、价格 |
| INT | Integer | 整数 |

#### 3.4 字段命名智能推断规则（基于 GenUtils）⭐⭐⭐⭐⭐

根据字段名后缀自动推断 HTML 控件类型和查询方式：

##### HTML 控件类型推断

| 字段名后缀 | 控件类型 | 说明 |
|-----------|---------|------|
| `name` | input | 名称类输入框 |
| `title` | input | 标题类输入框 |
| `code` | input | 编码类输入框 |
| `status` | select | 状态下拉选择 |
| `type` | select | 类型下拉选择 |
| `sex` | select | 性别下拉选择 |
| `image` / `img` | imageUpload | 图片上传 |
| `images` | imageUpload | 多图上传 |
| `file` | fileUpload | 文件上传 |
| `files` | fileUpload | 多文件上传 |
| `content` | editor | 富文本编辑器 |
| `time` | datetime | 日期时间选择 |
| `date` | datetime | 日期选择 |
| `remark` | textarea | 多行文本 |
| `sort` / `order_num` | input（数字） | 排序字段 |

##### 查询方式推断

| 字段名后缀 | 查询方式 | 生成代码示例 |
|-----------|---------|-------------|
| `name` / `title` | LIKE | `lqw.like(Entity::getName, bo.getName())` |
| `id` / `code` / `status` / `type` | EQ | `lqw.eq(Entity::getStatus, bo.getStatus())` |
| `time` / `date` | BETWEEN | `lqw.between(Entity::getCreateTime, beginTime, endTime)` |
| searchValue 多字段搜索 | LIKE/LIKECAST | String→`like`，非String→`likeCast` |

##### 是否显示在列表/查询/编辑

| 字段类型 | 列表显示 | 查询显示 | 编辑显示 |
|---------|---------|---------|---------|
| 主键 id | ✅ | ❌ | ❌ |
| 审计字段（create_by, update_by 等） | ❌ | ❌ | ❌ |
| 逻辑删除 is_deleted | ❌ | ❌ | ❌ |
| 租户 tenant_id | ❌ | ❌ | ❌ |
| 状态 status | ✅ | ✅ | ✅ |
| 名称 name | ✅ | ✅ | ✅ |
| 内容 content | ❌ | ❌ | ✅ |
| 备注 remark | ❌ | ❌ | ✅ |
| 创建时间 create_time | ✅ | ✅（范围） | ❌ |

##### 字典类型推断

| 字段名后缀 | 推荐字典类型 | 说明 |
|-----------|------------|------|
| `status` | `sys_enable_status` | 状态（正常/停用） |
| `gender` | `sys_user_gender` | 性别 |
| `type` | 根据业务自定义 | 类型字典 |
| `is_xxx` | `sys_yes_no` | 是否选项 |

**注意**：如果需要使用字典，需要在步骤 3.5 中配置字典类型。

#### 3.5 字典类型配置（可选）

如果字段需要使用字典下拉，需要配置字典类型：

```
## 🏷️ 字典类型配置

检测到以下字段可能需要配置字典：

| 字段 | 推荐字典类型 | 是否已存在 |
|------|------------|----------|
| status | sys_enable_status | ✅ 存在 |
| feedback_type | （需自定义） | ❌ 不存在 |

对于不存在的字典，是否需要创建？(Y/n)
```

##### 字典创建流程

如果需要创建新字典（字典值（`dict_value`）必须是**小写英文字母或数字**，禁止使用大写字母！）：

```sql
-- 1. 插入字典类型
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, create_dept, create_by, create_time, remark)
VALUES (200, '反馈类型', 'feedback_type', 100, 1, sysdate(), '反馈类型字典');

-- 2. 插入字典数据
INSERT INTO sys_dict_data (dict_data_id, dict_sort, dict_label, dict_value, dict_type, create_dept, create_by, create_time)
VALUES (2001, 1, '建议', '1', 'feedback_type', 100, 1, sysdate());

INSERT INTO sys_dict_data (dict_data_id, dict_sort, dict_label, dict_value, dict_type, create_dept, create_by, create_time)
VALUES (2002, 2, '投诉', '2', 'feedback_type', 100, 1, sysdate());

INSERT INTO sys_dict_data (dict_data_id, dict_sort, dict_label, dict_value, dict_type, create_dept, create_by, create_time)
VALUES (2003, 3, '其他', '3', 'feedback_type', 100, 1, sysdate());
```

#### 3.6 自动生成查询条件

根据字段类型自动生成 DAO 中的 buildQueryWrapper 方法：

**规则**：
- ID字段 → 精确匹配 `lqw.eq(Entity::getId, bo.getId())`
- 状态字段 → 精确匹配 `lqw.eq(Entity::getStatus, bo.getStatus())`
- 名称字段 → 模糊查询 `lqw.like(Entity::getName, bo.getName())`
- 时间字段 → 范围查询 `lqw.between(Entity::getCreateTime, params.get("beginCreateTime"), params.get("endCreateTime"))`

---

### 第四步：生成前端代码

直接生成完整的前端代码（API + 页面），无需询问。

#### 4.1 学习现有代码（强制执行）

根据模板类型读取对应的参考代码：

```bash
# 通用 - API 定义
Read plus-ui/src/api/business/base/ad/adApi.ts
Read plus-ui/src/api/business/base/ad/adTypes.ts

# 如果生成页面
Read plus-ui/src/views/business/base/ad/ad.vue
```

**树表模板额外参考**：
```bash
# 树表页面参考
Read plus-ui/src/views/system/core/menu/menu.vue
```

**主子表模板额外参考**：
```bash
# 主子表页面参考（主表 + 子表组件）
Read plus-ui/src/views/system/core/user/user.vue
```

#### 4.2 生成文件

##### 普通表（crud）

| 文件 | 说明 |
|------|------|
| `xxxTypes.ts` | 类型定义（Query, Bo, Vo） |
| `xxxApi.ts` | API 接口（page, get, add, update, delete） |
| `xxx.vue` | 页面组件（ASearchForm + 表格 + AModal） |

##### 树表（tree）

| 文件 | 说明 |
|------|------|
| `xxxTypes.ts` | 类型定义（含 children 字段） |
| `xxxApi.ts` | API 接口（listTree 替代 page） |
| `xxx.vue` | 树形表格页面（带展开/折叠） |

**树表 API 差异**：

```typescript
// 普通表
export const pageXxxs = (query?: XxxQuery): Result<PageResult<XxxVo>> => {
  return http.get('/base/xxx/pageXxxs', query)
}

// 树表
export const listXxxTree = (query?: XxxQuery): Result<XxxVo[]> => {
  return http.get('/base/xxx/listXxxTree', query)
}
```

**树表类型差异**：

```typescript
// 树表 VO 需要包含 children
export interface XxxVo {
  id: string | number
  parentId: string | number
  name: string
  orderNum: number
  children?: XxxVo[]  // 子节点列表
}
```

##### 主子表（sub）

| 文件 | 说明 |
|------|------|
| `xxxTypes.ts` | 主表类型 + 子表类型 |
| `xxxApi.ts` | 主表 API（包含子表数据） |
| `xxx.vue` | 主表页面 |
| `xxxChild.vue` | 子表组件（嵌入主表弹窗） |

**主子表类型示例**：

```typescript
// 主表 VO 包含子表列表
export interface OrderVo {
  id: string | number
  orderNo: string
  totalAmount: number
  orderItemList: OrderItemVo[]  // 子表列表
}

// 子表 VO
export interface OrderItemVo {
  id: string | number
  orderId: string | number
  productName: string
  quantity: number
  price: number
}
```

#### 4.3 前端页面组件规范

如果选择生成页面，必须遵循以下规范：

##### 必须使用的组件

| 场景 | 使用组件 | 禁止使用 |
|------|---------|---------|
| 搜索表单 | `<ASearchForm>` | `<el-form inline>` |
| 输入框 | `<AFormInput>` | `<el-input>` |
| 下拉选择 | `<AFormSelect>` | `<el-select>` |
| 日期选择 | `<AFormDate>` | `<el-date-picker>` |
| 弹窗/抽屉 | `<AModal>` | `<el-dialog>` |
| 开关 | `<AFormSwitch>` | `<el-switch>` |
| 图片上传 | `<AFormImgUpload>` | 自定义上传 |
| 文件上传 | `<AFormFileUpload>` | 自定义上传 |
| 富文本 | `<AFormEditor>` | 其他编辑器 |
| 字典标签 | `<DictTag>` | 自定义渲染 |

##### 页面结构模板（必须对标 ad.vue）

> **⚠️ 生成前端页面时，必须先 `Read plus-ui/src/views/business/base/ad/ad.vue`，完全复制其结构和风格！**
> 以下模板仅为结构说明，实际生成时以 ad.vue 为准。

```vue
<!-- XXX管理 -->
<template>
  <div>
    <!-- 搜索栏 -->
    <ASearchForm ref="queryFormRef" v-model="queryParams" :visible="showSearch">
      <AFormInput label="模糊搜索" prop="searchValue" v-model="queryParams.searchValue" @input="handleQuery"></AFormInput>
      <AFormSelect label="状态" v-model="queryParams.status" prop="status" :options="sys_enable_status" @change="handleQuery"></AFormSelect>
      <AFormDate v-model="dateRangeCreateTime" prop="createTime" type="daterange" label="创建时间" @change="handleQuery"></AFormDate>
    </ASearchForm>

    <el-card shadow="hover">
      <!-- 工具栏 -->
      <template #header>
        <el-row :gutter="10" class="mb-2">
          <el-col :span="1.5" v-permi="['[模块]:[功能]:add']">
            <el-button type="primary" plain icon="Plus" @click="handleAdd">
              {{ t('新增') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['[模块]:[功能]:update']">
            <el-button type="success" plain icon="Edit" :disabled="selectionItems.length !== 1" @click="handleUpdate()">
              {{ t('修改') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['[模块]:[功能]:delete']">
            <el-button type="danger" plain icon="Delete" :disabled="selectionItems.length === 0" @click="handleDelete()">
              {{ t('删除') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['[模块]:[功能]:import']">
            <AImportExcel
              v-slot="{ openImportExcel }"
              :title="t('Xxx Data', 'XXX数据')"
              templateUrl="/[模块]/[功能]/templateXxxs"
              importUrl="/[模块]/[功能]/importXxxs"
              @import-success="getList"
            >
              <el-button type="info" plain icon="Top" @click="openImportExcel">
                {{ t('导入') }}
              </el-button>
            </AImportExcel>
          </el-col>
          <el-col :span="1.5" v-permi="['[模块]:[功能]:export']">
            <el-button type="warning" plain icon="Download" @click="handleExport">
              {{ t('导出') }}
            </el-button>
          </el-col>

          <TableToolbar
            v-model:showSearch="showSearch"
            @reset-query="resetQuery"
            @query-table="getList"
            :table-columns="detailFields"
            :table-data="xxxList"
          ></TableToolbar>
        </el-row>
      </template>

      <!-- 表格数据 -->
      <el-table ref="xxxTableRef" v-loading="isLoading" :data="xxxList" :height="tableHeight" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column :label="t('name', '名称')" prop="name" align="center" />
        <el-table-column :label="t('status', '状态')" prop="status" align="center">
          <template #default="{ row }">
            <AFormSwitch v-model="row.status" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column :label="t('createTime', '创建时间')" prop="createTime" align="center" width="105" />
        <el-table-column :label="t('操作')" align="center" fixed="right" width="120">
          <template #default="{ row }">
            <el-tooltip :content="t('查看')" placement="top">
              <el-button v-permi="['[模块]:[功能]:query']" link type="primary" icon="View" @click="handleView(row)"></el-button>
            </el-tooltip>
            <el-tooltip :content="t('修改')" placement="top">
              <el-button v-permi="['[模块]:[功能]:update']" link type="success" icon="Edit" @click="handleUpdate(row)"></el-button>
            </el-tooltip>
            <el-tooltip :content="t('删除')" placement="top">
              <el-button v-permi="['[模块]:[功能]:delete']" link type="danger" icon="Delete" @click="handleDelete(row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 添加或修改对话框 -->
    <AModal v-model="dialog.visible" :title="dialog.title" :loading="buttonLoading" @confirm="submitForm" @cancel="cancel">
      <el-form ref="xxxFormRef" :model="form" :rules="rules" label-width="auto">
        <el-row :gutter="10">
          <AFormInput label="名称" v-model="form.name" prop="name" span="auto"></AFormInput>
          <AFormRadio label="状态" v-model="form.status" prop="status" :options="sys_enable_status" span="auto"></AFormRadio>
          <AFormInput label="备注" v-model="form.remark" type="textarea" :maxlength="255" prop="remark" span="auto"></AFormInput>
        </el-row>
      </el-form>
    </AModal>

    <!-- 查看详情对话框 -->
    <ADetail v-model="viewDialog.visible" :title="viewDialog.title" :data="viewData" :fields="detailFields" />
  </div>
</template>

<script setup lang="ts" name="Xxx">
import { pageXxxs, getXxx, addXxx, updateXxx, deleteXxxs } from '@/api/business/[模块]/[功能]/xxxApi'
import type { XxxQuery, XxxBo, XxxVo } from '@/api/business/[模块]/[功能]/xxxTypes'
import { isTrue, toggleStatus } from '@/utils/boolean'
import { addDateRange } from '@/utils/date'
import { toValidate } from '@/utils/to'
import { showMsgSuccess, showConfirm } from '@/utils/modal'

/** 字典数据 */
const { sys_enable_status } = useDict(DictTypes.sys_enable_status)

const { t } = useI18n()

// 使用表格高度处理钩子
const { tableHeight, queryFormRef, showSearch } = useTableHeight()

// =========== 查询相关 ===========

/**查询参数对象*/
const queryParams = ref<XxxQuery>({
  pageNum: 1,
  pageSize: 10,
  orderByColumn: 'id',
  isAsc: 'desc',
  // 所有查询字段显式初始化为 undefined
  name: undefined,
  status: undefined
})

/**日期范围选择器*/
const dateRangeCreateTime = ref<[ElDateModelType, ElDateModelType]>(['', ''])

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  dateRangeCreateTime.value = ['', '']
  queryFormRef.value?.resetFields()
  handleQuery()
}

// =========== 表格数据相关 ===========
const isLoading = ref(true)
const xxxList = ref<XxxVo[]>([])
const total = ref(0)
const xxxTableRef = ref()
const selectionItems = ref<XxxVo[]>([])

const handleSelectionChange = (selection: XxxVo[]) => {
  selectionItems.value = selection
}

/** 查询列表 */
const getList = async () => {
  isLoading.value = true
  queryParams.value.params = {}
  addDateRange(queryParams.value, dateRangeCreateTime.value, 'createTime')
  const [err, data] = await pageXxxs(queryParams.value)
  if (!err) {
    xxxList.value = data.records
    total.value = data.total
  }
  isLoading.value = false
}

/** 导出数据 */
const handleExport = () => {
  useDownload().exportExcel(t('Xxx Data', 'XXX数据'), '/[模块]/[功能]/exportXxxs', queryParams.value)
}

/** 删除操作 */
const handleDelete = async (row?: XxxVo) => {
  const idsToDelete = row ? [row.id] : selectionItems.value.map((item) => item.id)
  if (idsToDelete.length === 0) return
  const itemsToDelete = row ? row.name || row.id : selectionItems.value.map((item) => item.name || item.id).join(', ')

  const [confirmErr] = await showConfirm(`${t('是否确认删除')}${itemsToDelete}`)
  if (confirmErr) return

  const [deleteErr] = await deleteXxxs(idsToDelete)
  if (!deleteErr) {
    showMsgSuccess(t('message.deleteSuccess'))
    await getList()
  }
}

// =========== 表单相关 ===========
const initFormData: XxxBo = {
  id: undefined,
  name: undefined,
  sortOrder: 999,
  status: '1',
  remark: undefined
}

const xxxFormRef = ref<ElFormInstance>()
const buttonLoading = ref(false)
const dialog = ref<DialogState>({ visible: false, title: '' })
const form = ref<XxxBo>({ ...initFormData })
const rules = ref<ElFormRules>({
  name: [{ required: true, message: t('name cannot be empty', '名称不能为空'), trigger: 'blur' }]
})

const reset = () => {
  form.value = { ...initFormData }
  xxxFormRef.value?.resetFields()
}

const cancel = () => {
  reset()
  dialog.value.visible = false
}

const handleAdd = () => {
  reset()
  dialog.value.visible = true
  dialog.value.title = `${t('新增')}${t('xxx', 'XXX')}`
}

const handleUpdate = async (row?: XxxVo) => {
  reset()
  const itemToEdit = row || selectionItems.value[0]
  const [err, data] = await getXxx(itemToEdit.id)
  if (!err) {
    Object.assign(form.value, data)
    dialog.value.visible = true
    dialog.value.title = `${t('修改')}${t('xxx', 'XXX')}`
  }
}

const submitForm = async () => {
  const [validateErr] = await toValidate(xxxFormRef)
  if (validateErr) return

  buttonLoading.value = true
  let err: Error | null, data: any
  if (form.value.id) {
    ;[err, data] = await updateXxx(form.value)
  } else {
    ;[err, data] = await addXxx(form.value)
  }
  if (!err) {
    showMsgSuccess(form.value.id ? t('message.updateSuccess') : t('message.addSuccess'))
    dialog.value.visible = false
    await getList()
  }
  buttonLoading.value = false
}

/** 状态切换 */
const handleStatusChange = async (row: XxxVo) => {
  const text = isTrue(row.status) ? t('启用') : t('停用')
  const [confirmErr] = await showConfirm(`${t('是否确认')}${text}${row.id}?`)
  if (confirmErr) {
    row.status = toggleStatus(row.status)
    return
  }
  const [updateErr] = await updateXxx(row)
  if (!updateErr) {
    await getList()
    showMsgSuccess(`${text}${t('成功')}`)
  } else {
    row.status = toggleStatus(row.status)
  }
}

/**查看对话框配置*/
const viewDialog = ref<DialogState>({ visible: false, title: '' })
const viewData = ref<XxxVo>({} as XxxVo)

/**详情字段配置 */
const detailFields = computed<FieldConfig[]>(() => [
  { prop: 'id', label: t('id', '主键id') },
  { prop: 'name', label: t('name', '名称') },
  { prop: 'status', label: t('status', '状态'), type: 'dict', dictOptions: sys_enable_status },
  { prop: 'createTime', label: t('createTime', '创建时间'), type: 'datetime' },
  { prop: 'updateTime', label: t('updateTime', '更新时间'), type: 'datetime' },
  { prop: 'remark', label: t('remark', '备注') }
])

const handleView = async (row: XxxVo) => {
  const [err, data] = await getXxx(row.id)
  if (!err) {
    viewData.value = data
    viewDialog.value.title = `${t('查看')}${t('xxx', 'XXX')}`
    viewDialog.value.visible = true
  }
}

// =========== 生命周期 ===========
onMounted(() => {
  getList()
})
onActivated(() => {
  if (isLoading.value) return
  getList()
})
</script>
```

> **关键差异说明**（相比旧模板）：
> - 权限指令用 `v-permi`（不是 `v-hasPermi`）
> - 搜索表单用 `v-model` 绑定（不是 `:model`），搜索项用 `@input`/`@change` 实时触发
> - 工具栏在 `el-card > template #header` 中，与 `<TableToolbar>` 并列
> - 操作列用 icon 按钮 + `<el-tooltip>`（不是文字按钮）
> - 表格高度用 `useTableHeight()` 自适应
> - 国际化用 `useI18n()` 的 `t()` 包裹所有文本
> - 字典用 `useDict(DictTypes.xxx)` 加载
> - 详情查看用 `<ADetail>` 组件 + `detailFields` 配置
> - 导入功能用 `<AImportExcel>` 组件
> - 状态切换用 `<AFormSwitch>` + `handleStatusChange`
> - 消息提示用 `showMsgSuccess`/`showConfirm`（不是 ElMessage）
> - 表单验证用 `toValidate(formRef)`（不是 try-catch）
> - 日期范围用 `dateRangeCreateTime` + `addDateRange()`
> - 导出用 `useDownload().exportExcel()`
> - 加载状态用 `isLoading`（不是 `loading`）
> - 生命周期含 `onMounted` + `onActivated`

#### 4.4 树表页面特殊处理

树表页面需要额外处理：

```vue
<!-- 树表页面示例（含展开/折叠控制） -->
<template>
  <div class="page-container">
    <!-- 工具栏增加展开/折叠按钮 -->
    <TableToolbar>
      <template #left>
        <el-button @click="toggleExpandAll">{{ isExpandAll ? '折叠' : '展开' }}</el-button>
        <el-button type="primary" @click="handleAdd">新增</el-button>
      </template>
    </TableToolbar>

    <!-- 树形表格 -->
    <el-table
      v-loading="loading"
      :data="dataList"
      row-key="id"
      :default-expand-all="isExpandAll"
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
    >
      <!-- 第一列使用 show-overflow-tooltip -->
      <el-table-column label="名称" prop="name" show-overflow-tooltip />
      <!-- 其他列 -->
    </el-table>

    <!-- 树表不需要分页 -->
  </div>
</template>

<script setup lang="ts">
// 展开/折叠控制
const isExpandAll = ref(false)
const toggleExpandAll = () => {
  isExpandAll.value = !isExpandAll.value
  // 刷新表格
  getList()
}
</script>
```

#### 4.5 主子表页面特殊处理

主子表需要生成子表组件：

**主表弹窗中嵌入子表**：

```vue
<!-- 主表弹窗 -->
<AModal v-model="dialogVisible" :title="dialogTitle" width="800px" @confirm="submitForm">
  <el-form ref="formRef" :model="form" :rules="rules">
    <!-- 主表字段 -->
    <AFormInput v-model="form.orderNo" label="订单号" prop="orderNo" />

    <!-- 子表组件 -->
    <el-divider content-position="left">订单明细</el-divider>
    <OrderItemChild v-model="form.orderItemList" />
  </el-form>
</AModal>
```

**子表组件（xxxChild.vue）**：

```vue
<!-- 主子表子组件示例（可编辑明细列表） -->
<template>
  <div class="child-table">
    <el-button type="primary" size="small" @click="handleAdd">添加明细</el-button>
    <el-table :data="modelValue" border>
      <el-table-column label="商品名称" prop="productName">
        <template #default="{ row, $index }">
          <el-input v-model="row.productName" placeholder="请输入" />
        </template>
      </el-table-column>
      <el-table-column label="数量" prop="quantity" width="120">
        <template #default="{ row }">
          <el-input-number v-model="row.quantity" :min="1" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="80">
        <template #default="{ $index }">
          <el-button link type="danger" @click="handleDelete($index)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import type { OrderItemVo } from './orderTypes'

const props = defineProps<{
  modelValue: OrderItemVo[]
}>()

const emit = defineEmits(['update:modelValue'])

const handleAdd = () => {
  const newItem: OrderItemVo = { productName: '', quantity: 1, price: 0 }
  emit('update:modelValue', [...props.modelValue, newItem])
}

const handleDelete = (index: number) => {
  const list = [...props.modelValue]
  list.splice(index, 1)
  emit('update:modelValue', list)
}
</script>
```

---

### 第五步：输出代码清单

```markdown
✅ CRUD 代码生成完成！

## 📦 已生成文件清单

### 菜单 SQL
- ✅ script/sql/ry_plus_app.sql（已追加菜单 SQL）

### 后端代码 (9个文件)
- ✅ domain/Feedback.java (Entity)
- ✅ domain/bo/FeedbackBo.java (BO)
- ✅ domain/vo/FeedbackVo.java (VO)
- ✅ mapper/FeedbackMapper.java (Mapper)
- ✅ dao/IFeedbackDao.java (DAO接口)
- ✅ dao/impl/FeedbackDaoImpl.java (DAO实现)
- ✅ service/IFeedbackService.java (Service接口)
- ✅ service/impl/FeedbackServiceImpl.java (Service实现)
- ✅ controller/FeedbackController.java (Controller)

### 前端代码（3个文件）

- ✅ plus-ui/src/api/business/base/feedback/feedbackTypes.ts
- ✅ plus-ui/src/api/business/base/feedback/feedbackApi.ts
- ✅ plus-ui/src/views/business/base/feedback/feedback.vue

**树表差异**：
- API 使用 `listXxxTree` 替代 `pageXxxs`
- 类型包含 `children` 字段
- 页面包含展开/折叠功能，无分页

**主子表额外文件**：
- ✅ plus-ui/src/views/business/base/order/orderChild.vue（子表组件）

---

## 🚀 下一步操作

### 1. 执行菜单 SQL

\```bash
mysql -h [主机] -P [端口] -u [用户名] -p [数据库]

source script/sql/ry_plus_app.sql;
\```

### 2. 重启后端服务

重新启动 RuoYiAdminApplication

### 3. 测试接口

访问 Swagger: http://localhost:8080/doc.html

测试接口：
- GET /base/feedback/pageFeedbacks - 分页查询
- GET /base/feedback/getFeedback/{id} - 查询详情
- POST /base/feedback/addFeedback - 新增
- PUT /base/feedback/updateFeedback - 修改
- DELETE /base/feedback/deleteFeedbacks/{ids} - 删除
- GET /base/feedback/optionFeedbacks - 选项列表（下拉选择用）

### 4. 配置菜单权限

1. 登录管理后台
2. 系统管理 → 菜单管理
3. 找到"反馈管理"菜单
4. 分配给对应角色

---

## 💡 功能增强建议

### 如需添加业务逻辑

#### 1. 添加保存前校验

在 `FeedbackServiceImpl` 中重写 `beforeSave()` 方法：

\```java
@Override
protected void beforeSave(Feedback entity) {
    // 业务校验逻辑
    if (StringUtils.isBlank(entity.getContent())) {
        throw ServiceException.of("反馈内容不能为空");
    }
    // 默认值设置
    if (entity.getStatus() == null) {
        entity.setStatus("0");  // 默认待处理
    }
}
\```

#### 2. 添加删除前检查

在 `FeedbackServiceImpl` 中重写 `beforeDelete()` 方法：

\```java
@Override
protected void beforeDelete(Collection<Long> ids) {
    // 删除前校验
    List<Feedback> feedbacks = feedbackDao.listByIds(ids);
    boolean hasProcessing = feedbacks.stream()
        .anyMatch(f -> "1".equals(f.getStatus()));
    if (hasProcessing) {
        throw ServiceException.of("存在已处理的反馈，无法删除");
    }
}
\```

#### 3. 添加复杂查询条件

在 `FeedbackDaoImpl` 的 `buildQueryWrapper()` 中添加：

\```java
@Override
public PlusLambdaQuery<Feedback> buildQueryWrapper(FeedbackBo bo) {
    PlusLambdaQuery<Feedback> lqw = PlusLambdaQuery.of();

    // 基础查询
    lqw.eq(Feedback::getId, bo.getId());
    lqw.eq(Feedback::getStatus, bo.getStatus());

    // 复杂查询：按用户ID或处理人ID查询
    if (bo.getUserId() != null) {
        lqw.eq(Feedback::getUserId, bo.getUserId());
    }

    // 模糊搜索：搜索反馈内容
    if (StringUtils.isNotBlank(bo.getSearchValue())) {
        lqw.like(Feedback::getContent, bo.getSearchValue());
    }

    return lqw;
}
\```

#### 4. 添加自定义接口

在 `FeedbackController` 中添加：

\```java
/**
 * 处理反馈
 */
@SaCheckPermission("base:feedback:update")
@Log(title = "反馈管理", operType = DictOperType.UPDATE)
@PostMapping("/handleFeedback/{id}")
public R<Void> handleFeedback(@PathVariable Long id, @RequestBody String result) {
    return R.status(feedbackService.handleFeedback(id, result));
}
\```

在 `FeedbackServiceImpl` 中实现：

\```java
public boolean handleFeedback(Long id, String result) {
    Feedback feedback = feedbackDao.getById(id);
    if (feedback == null) {
        throw ServiceException.of("反馈不存在");
    }

    feedback.setStatus("1");  // 已处理
    feedback.setRemark(result);
    feedback.setHandleTime(new Date());
    feedback.setHandlerId(LoginHelper.getUserId());

    return feedbackDao.updateById(feedback);
}
\```
```

---

## ⚠️ 注意事项

1. **表结构必须包含审计字段**
   - 如果表缺少审计字段，生成的代码可能不完整
   - 建议先修改表结构，添加审计字段

2. **菜单ID避免冲突**
   - 使用数据库查询的最大ID + 10 作为起始ID
   - 确保7个菜单ID连续且不冲突

3. **重启服务**
   - 执行菜单 SQL 后必须重启后端服务
   - 否则新接口无法访问

4. **配置权限**
   - 菜单创建后需要分配给角色
   - 否则普通用户无法看到菜单

---

## 🎯 与 `/dev` 命令的区别

| 对比项 | `/crud` | `/dev` |
|--------|---------|--------|
| **适用场景** | 表已存在 | 从零开始 |
| **表结构设计** | ❌ 跳过 | ✅ 引导设计 |
| **菜单 SQL** | ✅ 生成 | ✅ 生成 |
| **代码生成** | ✅ 自动 | ✅ 自动 |
| **树表支持** | ✅ 支持 | ✅ 支持 |
| **主子表支持** | ✅ 支持 | ✅ 支持 |
| **字典配置** | ✅ 支持 | ✅ 支持 |
| **前端页面** | ✅ 直接生成 | ✅ 直接生成 |
| **业务逻辑** | ❌ 无 | ⏳ 可选 |
| **执行速度** | ⚡ 快速 | 🐢 较慢（更完整） |

**建议**：
- 快速原型：使用 `/crud`
- 正式开发：使用 `/dev`
- 表已存在且无需修改：使用 `/crud`
- 需要完整的开发流程：使用 `/dev`
- 树表/主子表快速生成：使用 `/crud`

---

## 📖 示例对话

### 用户输入

```
/crud
```

### AI 响应

```
好的！我将帮您基于已有表快速生成 CRUD 代码。

## 📋 请提供表名

请提供表名：
（如：b_feedback, m_goods_category）

💡 数据库连接信息将从 application-dev.yml 自动读取
```

---

### 用户输入

```
表名：b_feedback
```

### AI 响应

```
好的！正在读取数据库配置并分析表结构...

[AI 内部：读取 application-dev.yml，解析数据库配置，直接连接数据库]
[执行 SHOW CREATE TABLE b_feedback]

## 📊 表结构分析

**表名**：b_feedback
**注释**：用户反馈表

[显示完整的表结构分析]

---

### 提取功能名称

- 中文名：反馈管理
- 英文名：Feedback
- 接口路径：/base/feedback

确认功能名称，或自定义修改？

回复"确认"继续，或提供自定义名称。
```

---

[后续按流程生成代码...]

---

## 🎉 总结

`/crud` 命令帮助您：
- ⚡ 快速生成标准 CRUD 代码
- 📋 自动分析表结构生成代码
- 🎯 节省开发时间
- ✅ 代码质量有保障

结合 `/dev` 和 `/check` 命令使用，开发效率更高！
