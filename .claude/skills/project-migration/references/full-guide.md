
# 项目迁移指南

## 概述

本技能用于将其他 Java 全栈项目（后端 + 前端 + 移动端）迁移到 ruoyi-plus-uniapp 架构下。

**核心挑战**：工程量巨大，Claude Code 上下文有限。

**解决方案**：**文档驱动的分步迁移** — 通过持久化文档串联多个会话，每个会话只处理 1-2 个模块。

**核心理念**：**先跑通，后优化** — 迁移阶段优先保证功能可用，规范统一作为后续可选优化。

---

## 迁移深度策略（最关键的决策）

> **阶段 0 扫描完成后，必须让客户选择迁移深度，再开始实施。**

### 🟢 浅迁移（推荐大型项目）

**原则**：只改不改就跑不起来的东西，数据库完全不动。

| 必须改（不改编译失败/运行崩溃） | 不改（保留原样） |
|---|---|
| 包名 `com.xxx` → `plus.ruoyi.*` | 数据库字段名（通过 @TableField 映射） |
| 三层→四层（添加 DAO 层） | 主键策略（保留 AUTO_INCREMENT） |
| Service 不继承 ServiceImpl | 状态值含义（保留原语义） |
| Spring Security → Sa-Token | 表前缀（通过 @TableName 映射） |
| AjaxResult → R\<T\> | API 路径（保留原路径，前端直接对接） |
| BeanUtils → MapstructUtils | 审计字段名（不改库，Entity 内映射） |
| RuntimeException → ServiceException | 表结构（不 ALTER TABLE） |
| 对象转换使用 @AutoMappers/@AutoMapper | |

**适用场景**：大型项目（20+ 表）、时间紧迫、想快速跑通

### 🟡 标准迁移（推荐中型项目）

在浅迁移基础上追加：
- API 路径统一为 `/pageXxxs` 规范
- 前端组件 el-* → A* 封装组件
- 移动端组件 uni-* → wd-*
- API 调用方式统一为 `[err, data]` 格式

**适用场景**：中型项目（10-20 表）、有一定时间余量

### 🔴 深度迁移（推荐小型项目/新表）

完全对齐本项目规范：
- 数据库字段名统一（del_flag → is_deleted 等）
- 主键迁移到雪花 ID
- 状态值翻转（0正常→1正常）
- 表前缀统一（b_/m_/crm_/iot_）
- 审计字段完全统一
- 添加多租户支持（tenant_id）

**适用场景**：小型项目（< 10 表）、新建表、有充足时间

---

## 迁移总流程（7 个阶段）

```
阶段 0: 源项目全量扫描 → 生成迁移蓝图 + 选择迁移深度（1-2个会话）
   ↓
阶段 1: 数据库迁移     → 表结构适配 + 字典 + 菜单（1个会话）
   ↓
阶段 2: 后端迁移       → 按模块逐个（每模块1个会话）
   ↓
阶段 3: PC前端迁移     → 按页面逐个（每2-3页1个会话）
   ↓
阶段 4: 移动端迁移     → 按页面逐个（每2-3页1个会话）
   ↓
阶段 5: 集成验证       → 全链路测试（1个会话）
   ↓
阶段 6: 优化建议       → 生成优化清单，客户逐条决定（可选）
```

---

## 触发与使用

### 首次迁移（触发词：开始迁移、迁移项目）

用户说"开始迁移 xxx 项目"或"把 xxx 迁移过来"时：

> **严格规则：首次迁移只做扫描和规划，绝对禁止写任何业务代码、建表 SQL 或连接数据库！**

执行步骤：
1. 扫描源项目（后端 + 前端 + 移动端），按 0.1 扫描清单逐项执行
2. **逐个生成**以下所有文件（每生成一个就写入磁盘，不要攒到最后）：
   - ① `docs/migration/source-analysis.md` — 源项目分析报告
   - ② `docs/migration/table-mapping.md` — 表结构映射
   - ③ `docs/migration/api-mapping.md` — API 路径映射
   - ④ `docs/migration/page-mapping.md` — 页面映射（前端+移动端）
   - ⑤ `docs/migration/dict-mapping.md` — 字典数据映射
   - ⑥ `docs/migration/menu-mapping.md` — 菜单权限映射
   - ⑦ `docs/migration/modules/{模块名}.md` — 每个业务模块一个文件
   - ⑧ `docs/migration/migration-blueprint.md` — 总蓝图（**最后生成**，汇总所有信息）
3. **完成校验**：用 Glob 检查 `docs/migration/**/*`，确认以上 8 类文件全部存在。**文件数量不足 8 个则阶段 0 未完成，必须补全！**
4. 向用户展示迁移蓝图摘要（模块清单、技术差异、预计工作量）
5. **询问用户选择迁移深度**（浅/标准/深度），将选择记录到蓝图文件
6. **停下来等用户确认**，不要自行进入下一阶段

> **⛔ 阶段 0 的唯一产出是文档，不是代码。**
> **⛔ 只生成了部分文件（如只有 3 个）就说"阶段 0 完成"是严重错误！**
> **⛔ 阶段 0 结束后必须停止，向用户报告蓝图并等待指令。**
> **⛔ 必须让用户选择迁移深度后才能开始实施！**

### 继续迁移（触发词：继续迁移、迁移进度、恢复迁移）

用户说"继续迁移"或"迁移进度"时：

**第一步（强制）：文档完整性校验**
1. 用 `Glob("docs/migration/**/*")` 检查已有文件列表
2. 对照以下必须文件清单逐个核查：
   - [ ] `migration-blueprint.md`
   - [ ] `source-analysis.md`
   - [ ] `table-mapping.md`
   - [ ] `api-mapping.md`
   - [ ] `page-mapping.md`
   - [ ] `dict-mapping.md`
   - [ ] `menu-mapping.md`
   - [ ] `modules/` 目录下至少 1 个模块文件
3. **如果有任何文件缺失 → 阶段 0 未完成！** 告知用户缺少哪些文件，先补全再继续
4. **只有全部文件都存在时，才能认为阶段 0 已完成**
5. **检查蓝图中是否已选择迁移深度**，如未选择则先询问用户

**第二步：正常继续流程**（仅在文件校验通过后执行）
1. 读取 `docs/migration/migration-blueprint.md`
2. 确认迁移深度（浅/标准/深度）
3. 找到下一个未完成的阶段/模块
4. **向用户确认**即将迁移哪个模块
5. 用户确认后开始实施
6. 完成后更新蓝图状态
7. 向用户报告进度

### 门控规则（必须遵守）

```
阶段 0 完成 → 必须停下来 → 等用户选择迁移深度并确认
                ↓
阶段 1 完成 → 必须停下来 → 等用户说"继续迁移"
                ↓
每个模块完成 → 必须停下来 → 等用户说"继续迁移"或指定下一个
```

**禁止行为**：
- 扫描完蓝图后自动开始建表 SQL
- 一个模块完成后自动开始下一个模块
- 未经用户确认就连接数据库执行操作
- 阶段 0 中生成任何非文档类产物（代码、SQL 等）
- 未确认迁移深度就开始实施

---

## 阶段 0：源项目全量扫描（最关键）

> 这一步是"毫无遗漏"的核心保障。必须自动化扫描，不依赖人工列举。

### 0.1 扫描清单（必须全部覆盖）

#### 后端扫描

| 扫描项 | 扫描方法 | 记录到 |
|--------|---------|--------|
| **所有表** | 读源项目 SQL 文件或连库查 `SHOW TABLES` | `table-mapping.md` |
| **表结构** | `DESC 表名` 或读建表 SQL | `table-mapping.md` |
| **所有 Entity** | `Glob("**/domain/*.java")` 或 `**/entity/*.java` | `source-analysis.md` |
| **所有 Controller** | `Glob("**/controller/*.java")` | `api-mapping.md` |
| **所有 API 路径** | `Grep("@GetMapping\|@PostMapping\|@PutMapping\|@DeleteMapping")` | `api-mapping.md` |
| **所有 Service** | `Glob("**/service/*.java")` | `source-analysis.md` |
| **配置文件** | 读 `application.yml` / `application.properties` | `source-analysis.md` |
| **权限注解** | `Grep("@PreAuthorize\|@SaCheck\|hasPermission")` | `menu-mapping.md` |
| **字典数据** | 查 SQL 中的字典初始化数据 | `dict-mapping.md` |
| **定时任务** | `Grep("@Scheduled\|@XxlJob\|@JobHandler")` | `source-analysis.md` |
| **第三方集成** | 扫描 pom.xml 依赖和配置 | `source-analysis.md` |
| **工具类** | `Glob("**/utils/*.java")` | `source-analysis.md` |
| **审计字段** | 扫描 Entity 基类和表结构中的审计字段名 | `table-mapping.md` |

#### 前端扫描

| 扫描项 | 扫描方法 | 记录到 |
|--------|---------|--------|
| **所有路由/页面** | 读 `router/index.js` 或 `Glob("**/views/**/*.vue")` | `page-mapping.md` |
| **所有 API 文件** | `Glob("**/api/**/*.js")` 或 `**/api/**/*.ts` | `api-mapping.md` |
| **所有 Store** | `Glob("**/store/**/*.js")` | `page-mapping.md` |
| **所有组件** | `Glob("**/components/**/*.vue")` | `page-mapping.md` |
| **UI 库** | 读 `package.json` 确认使用的 UI 框架 | `source-analysis.md` |

#### 移动端扫描

| 扫描项 | 扫描方法 | 记录到 |
|--------|---------|--------|
| **所有页面** | 读 `pages.json` 或 `Glob("**/pages/**/*.vue")` | `page-mapping.md` |
| **所有 API** | `Glob("**/api/**/*.js")` | `api-mapping.md` |
| **UI 库** | 读 `package.json` | `source-analysis.md` |

### 0.2 输出文档结构

> **强制要求：以下所有文件必须全部生成，缺少任何一个都不算完成阶段 0！**

扫描完成后必须生成以下完整文档：

```
docs/migration/
├── migration-blueprint.md   ← 总蓝图（进度追踪主文件）【必须】
├── source-analysis.md       ← 源项目分析报告【必须】
├── table-mapping.md         ← 表结构映射（源表 → 目标表）【必须】
├── api-mapping.md           ← API 映射（源路径 → 目标路径）【必须】
├── page-mapping.md          ← 页面映射（前端+移动端）【必须】
├── dict-mapping.md          ← 字典数据映射【必须】
├── menu-mapping.md          ← 菜单权限映射【必须】
└── modules/                 ← 每个模块的详细迁移计划【必须】
    ├── {module1}.md          ← 按扫描结果生成
    ├── {module2}.md
    └── ...                   ← 每个业务模块一个文件
```

**完成检查**：阶段 0 结束前，必须逐个确认以上文件全部存在且内容完整。如果某个维度源项目不涉及（如无移动端），在对应文件中标注"源项目无此部分，无需迁移"。

### 0.3 蓝图文档模板

#### migration-blueprint.md

```markdown
# 迁移蓝图：{源项目名} → ruoyi-plus-uniapp

**创建时间**: YYYY-MM-DD
**最后更新**: YYYY-MM-DD
**源项目路径**: {路径}
**目标模块归属**: {base/mall/crm/iot}
**迁移深度**: ⬜ 浅迁移 / ⬜ 标准迁移 / ⬜ 深度迁移（待客户确认）

---

## 总进度

- [ ] 阶段 0：源项目扫描
- [ ] 阶段 1：数据库迁移 (0/{总表数} 表)
- [ ] 阶段 2：后端迁移 (0/{总模块数} 模块)
- [ ] 阶段 3：PC前端迁移 (0/{总页面数} 页)
- [ ] 阶段 4：移动端迁移 (0/{总页面数} 页)
- [ ] 阶段 5：集成验证
- [ ] 阶段 6：优化建议（可选）

## 迁移深度确认

> **客户必须在此确认迁移深度后才能开始实施。**

选择的深度：{浅迁移/标准迁移/深度迁移}
确认时间：YYYY-MM-DD

### 本次迁移范围（根据深度自动确定）

| 改动项 | 浅迁移 | 标准迁移 | 深度迁移 | 本次选择 |
|--------|--------|---------|---------|---------|
| 包名转换 | ✅ 必须 | ✅ 必须 | ✅ 必须 | |
| 四层架构 | ✅ 必须 | ✅ 必须 | ✅ 必须 | |
| 安全框架 | ✅ 必须 | ✅ 必须 | ✅ 必须 | |
| 响应封装 | ✅ 必须 | ✅ 必须 | ✅ 必须 | |
| 对象转换 | ✅ 必须 | ✅ 必须 | ✅ 必须 | |
| 异常处理 | ✅ 必须 | ✅ 必须 | ✅ 必须 | |
| API 路径规范 | ❌ 保留 | ✅ 统一 | ✅ 统一 | |
| 前端组件替换 | ❌ 保留 | ✅ 替换 | ✅ 替换 | |
| 移动端组件替换 | ❌ 保留 | ✅ 替换 | ✅ 替换 | |
| 数据库字段名 | ❌ 保留 | ❌ 保留 | ✅ 统一 | |
| 主键策略 | ❌ 保留 | ❌ 保留 | ✅ 雪花ID | |
| 状态值含义 | ❌ 保留 | ❌ 保留 | ✅ 翻转 | |
| 表前缀 | ❌ 保留 | ❌ 保留 | ✅ 统一 | |
| 多租户 | ❌ 不加 | ❌ 不加 | ✅ 添加 | |

## 源项目审计字段分析

| 本项目字段 | 源项目对应字段 | 类型匹配 | 处理方式 |
|-----------|-------------|---------|---------|
| create_by | {扫描结果} | {是/否} | {映射/新增/不处理} |
| create_time | {扫描结果} | {是/否} | {映射/新增/不处理} |
| update_by | {扫描结果} | {是/否} | {映射/新增/不处理} |
| update_time | {扫描结果} | {是/否} | {映射/新增/不处理} |
| is_deleted | {扫描结果} | {是/否} | {映射/新增/不处理} |
| tenant_id | {无/有} | — | {不加/映射/新增} |
| create_dept | {无/有} | — | {不加/映射/新增} |

## 模块清单与状态

| # | 模块名 | 表数 | 数据库 | 后端 | PC前端 | 移动端 | 备注 |
|---|--------|------|--------|------|--------|--------|------|
| 1 | 用户管理 | 2 | ⬜ | ⬜ | ⬜ | ⬜ | |
| 2 | 商品管理 | 3 | ⬜ | ⬜ | ⬜ | ⬜ | |
| 3 | 订单管理 | 4 | ⬜ | ⬜ | ⬜ | ⬜ | 依赖商品 |

## 技术栈差异摘要

| 维度 | 源项目 | 目标（本项目） | 迁移难度 |
|------|--------|-------------|---------|
| 框架 | {Spring Boot x.x} | Spring Boot 3.5.8 | |
| 安全 | {Spring Security/Sa-Token/...} | Sa-Token 1.44.0 | |
| ORM | {MyBatis/MyBatis-Plus/JPA/...} | MyBatis-Plus 3.5.14 | |
| 前端 | {Vue2/Vue3/React/...} | Vue 3 + Element Plus | |
| 移动端 | {有/无} | UniApp + WD UI | |

## 特殊处理项

- [ ] {需要特殊处理的功能1}
- [ ] {需要特殊处理的功能2}
```

#### source-analysis.md

```markdown
# 源项目分析报告

## 基本信息

- **项目名称**: {名称}
- **技术栈**: {Java版本, Spring Boot版本, 数据库, 前端框架}
- **包名前缀**: {com.xxx.yyy}
- **架构模式**: {三层/DDD/微服务/...}

## 后端模块清单

| 模块名 | 包路径 | Entity数 | Controller数 | 说明 |
|--------|--------|---------|-------------|------|
| | | | | |

## 审计字段体系分析

| 维度 | 源项目 | 本项目 | 差异 |
|------|--------|-------|------|
| Entity 基类 | {类名或无} | BaseEntity / TenantEntity | |
| 创建人字段 | {字段名, 类型} | create_by (BIGINT) | |
| 创建时间字段 | {字段名, 类型} | create_time (DATETIME) | |
| 更新人字段 | {字段名, 类型} | update_by (BIGINT) | |
| 更新时间字段 | {字段名, 类型} | update_time (DATETIME) | |
| 逻辑删除字段 | {字段名, 类型} | is_deleted (CHAR) | |
| 多租户字段 | {有/无} | tenant_id (VARCHAR) | |
| 部门审计字段 | {有/无} | create_dept (BIGINT) | |
| 自动填充机制 | {有/无，方式} | MyBatis-Plus MetaObjectHandler | |

## 第三方集成

| 集成项 | 技术/SDK | 本项目对应 | 迁移方案 |
|--------|---------|-----------|---------|
| 支付 | {xxx} | ruoyi-common-pay | |
| 短信 | {xxx} | ruoyi-common-sms | |
| 文件存储 | {xxx} | ruoyi-common-oss | |
| 定时任务 | {xxx} | @Scheduled / SnailJob | |

## 配置项清单

| 配置键 | 源值 | 本项目对应配置 | 备注 |
|--------|------|-------------|------|
| | | | |
```

#### table-mapping.md

```markdown
# 表结构映射

## 迁移深度：{浅迁移/标准迁移/深度迁移}

## 映射策略（根据迁移深度）

### 浅迁移策略（数据库不改，ORM 层映射）
- 保留原表名（通过 @TableName 映射）
- 保留原字段名（通过 @TableField 映射）
- 保留原主键策略
- 不添加 tenant_id / create_dept

### 标准迁移策略
- 同浅迁移（数据库层面不改）

### 深度迁移策略
- 源表前缀 `{xxx_}` → 目标前缀 `{b_/m_/crm_/iot_}`
- `del_flag` → `is_deleted`
- `phonenumber` → `phone`
- 缺少 `tenant_id` → 添加
- `AUTO_INCREMENT` → 雪花ID

## 审计字段映射方案

> 根据源项目审计字段情况选择 Entity 继承策略

| 源项目情况 | Entity 继承策略 | 说明 |
|-----------|---------------|------|
| 审计字段名与本项目相同 | `extends BaseEntity` | 直接继承，自动填充生效 |
| 审计字段名不同（如 creator/gmt_create） | 不继承基类，Entity 自行声明 + @TableField 映射 | 数据库不改，ORM 映射 |
| 没有审计字段 | 不继承基类 | 后续优化可加 |
| 需要多租户 | `extends TenantEntity` | 需确保有 tenant_id 字段 |
| 不需要多租户但审计字段匹配 | `extends BaseEntity` | 无需 tenant_id |

## 表映射清单

### 表1: {源表名}

**浅/标准迁移**：保留原表结构，Entity 通过 @TableName/@TableField 映射

| 源字段 | 源类型 | Entity 属性名 | @TableField 映射 | 备注 |
|--------|--------|-------------|-----------------|------|
| id | INT AUTO_INCREMENT | id | @TableId("id") | 保留自增 |
| del_flag | CHAR(1) | isDeleted | @TableField("del_flag") @TableLogic | ORM 映射 |
| creator | VARCHAR(64) | createBy | @TableField("creator") | ORM 映射 |
| gmt_create | DATETIME | createTime | @TableField("gmt_create") | ORM 映射 |

**深度迁移**（如客户选择）：

| 源字段 | 源类型 | 目标字段 | 目标类型 | 变更说明 |
|--------|--------|---------|---------|---------|
| id | INT AUTO_INCREMENT | id | BIGINT(20) | 雪花ID |
| del_flag | CHAR(1) | is_deleted | CHAR(1) | 字段重命名 |
| — | — | tenant_id | VARCHAR(20) | 新增 |
| — | — | create_dept | BIGINT(20) | 新增 |

**状态**: ⬜ 待迁移 / ✅ 已完成
```

#### modules/{module-name}.md

```markdown
# 模块迁移计划：{模块名}

## 迁移深度：{浅迁移/标准迁移/深度迁移}

## 源项目信息

- **源包路径**: {com.xxx.yyy.module}
- **涉及表**: {表1, 表2}
- **Controller 数**: {N}
- **API 数**: {N}

## 迁移目标

- **目标包路径**: plus.ruoyi.business.{base/mall}.{module}
- **表名处理**: {保留原名 @TableName 映射 / 改为新前缀}（取决于迁移深度）

## Entity 继承策略

> 根据源项目审计字段分析结果选择

- **选择**: {extends BaseEntity / extends TenantEntity / 不继承基类}
- **原因**: {审计字段匹配/字段名不同需映射/无审计字段}

## 迁移检查清单

### 数据库
- [ ] 表结构适配（浅迁移：不改库；深度迁移：建新表 SQL）
- [ ] 字典数据
- [ ] 菜单数据

### 后端（每个实体重复）

#### {实体名1}
- [ ] Entity（继承策略见上方 + @TableName + @TableField 映射）
- [ ] Bo（@AutoMappers，校验注解）
- [ ] Vo（@AutoMapper，Excel 注解）
- [ ] Mapper（继承 BaseMapper）
- [ ] IDao + DaoImpl（buildQueryWrapper）
- [ ] IService + ServiceImpl（不继承基类）
- [ ] Controller（权限注解，API路径根据迁移深度决定）

### PC 前端

#### {页面名1}
- [ ] xxxTypes.ts（类型定义）
- [ ] xxxApi.ts（API 定义）
- [ ] xxx.vue（浅迁移：可保留原组件；标准/深度：使用 A* 组件）

### 移动端（如需）
- [ ] xxxTypes.ts
- [ ] xxxApi.ts
- [ ] xxx.vue（浅迁移：可保留原组件；标准/深度：使用 wd-* 组件）

## 特殊逻辑

{源项目中该模块的非标准业务逻辑，需要特殊处理的部分}

## 依赖关系

- 依赖: {其他模块}
- 被依赖: {其他模块}
```

---

## 阶段 1：数据库适配

> **核心原则**：浅/标准迁移不改数据库，通过 ORM 层映射；深度迁移才改数据库。

### 1.1 浅/标准迁移：数据库不动

**无需建表 SQL**，所有适配在 Entity 层完成：

```java
// 示例：源表 original_order，有 creator/gmt_create/del_flag 字段
@TableName("original_order")  // 保留原表名
public class Order {
    @TableId(value = "id")  // 保留原主键
    private Long id;

    // 业务字段保持原字段名映射
    @TableField("order_no")
    private String orderNo;

    // 审计字段映射（源字段名 → 本项目属性名）
    @TableField(value = "creator", fill = FieldFill.INSERT)
    private String createBy;

    @TableField(value = "gmt_create", fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(value = "modifier", fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(value = "gmt_modified", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @TableLogic
    @TableField("del_flag")
    private String isDeleted;
}
```

**如果审计字段名与本项目完全相同**（create_by, create_time 等），可以直接继承 BaseEntity：

```java
@TableName("original_order")
public class Order extends BaseEntity {
    @TableId(value = "id")
    private Long id;

    // 只需声明业务字段
    private String orderNo;
}
```

**如果需要多租户但源表没有 tenant_id**（浅迁移中少数需要 ALTER TABLE 的情况）：

```sql
-- 仅在需要多租户时执行（需客户确认）
ALTER TABLE original_order ADD COLUMN tenant_id VARCHAR(20) DEFAULT '000000' COMMENT '租户ID';
```

### 1.2 深度迁移：完整建表

只有深度迁移才执行完整的表结构转换：

```sql
CREATE TABLE {前缀}_{表名} (
    id           BIGINT(20)   NOT NULL COMMENT '主键ID',
    tenant_id    VARCHAR(20)  DEFAULT '000000' COMMENT '租户ID',

    -- 业务字段（从源项目映射）
    {字段1}      {类型}       {约束} COMMENT '{注释}',
    status       CHAR(1)      DEFAULT '1' COMMENT '状态(0停用 1正常)',

    -- 审计字段
    create_dept  BIGINT(20)   DEFAULT NULL COMMENT '创建部门',
    create_by    BIGINT(20)   DEFAULT NULL COMMENT '创建人',
    create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by    BIGINT(20)   DEFAULT NULL COMMENT '更新人',
    update_time  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark       VARCHAR(500) DEFAULT NULL COMMENT '备注',
    is_deleted   CHAR(1)      DEFAULT '0' COMMENT '是否删除(0正常 1已删除)',

    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='{表注释}';
```

### 1.3 深度迁移的字段转换规则

> **仅深度迁移适用！浅/标准迁移跳过此节。**

| 源项目写法 | 本项目写法 | 说明 |
|-----------|-----------|------|
| `INT AUTO_INCREMENT` | `BIGINT(20) NOT NULL` | 雪花ID，不用自增 |
| `del_flag CHAR(1) DEFAULT '0'` | `is_deleted CHAR(1) DEFAULT '0'` | 逻辑删除字段名 |
| `phonenumber` | `phone` | 手机号字段名 |
| 无 `tenant_id` | 添加 `tenant_id VARCHAR(20) DEFAULT '000000'` | 多租户支持 |
| 无 `create_dept` | 添加 `create_dept BIGINT(20)` | 部门审计 |
| 无 `create_by` / `update_by` | 添加审计字段 | 完整审计 |
| `status = '0'` 表示正常 | `status = '1'` 表示正常 | 1=积极 0=消极 |
| `is_frame` | `is_external_link` | 菜单表字段名差异 |
| `visible = '0'` 显示 | `visible = '1'` 显示 | 值含义反转 |

### 1.4 字典数据迁移

从源项目的字典表中提取数据，转换为本项目的 `sys_dict_type` + `sys_dict_data` 格式。

### 1.5 菜单数据生成

根据源项目的权限配置，生成本项目的 `sys_menu` INSERT 语句。

**菜单 SQL 模板**：
```sql
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component,
    is_external_link, menu_type, visible, status, perms, icon)
VALUES
-- 一级菜单
({id}, '{模块名}', 0, {序号}, '{path}', NULL,
    '0', 'M', '1', '1', NULL, '{icon}'),
-- 二级菜单（列表页）
({id+1}, '{功能名}', {id}, 1, '{entity}', 'business/{module}/{entity}/{entity}',
    '0', 'C', '1', '1', '{module}:{entity}:query', 'list'),
-- 按钮权限
({id+2}, '新增', {id+1}, 1, '', NULL, '0', 'F', '1', '1', '{module}:{entity}:add', NULL),
({id+3}, '修改', {id+1}, 2, '', NULL, '0', 'F', '1', '1', '{module}:{entity}:update', NULL),
({id+4}, '删除', {id+1}, 3, '', NULL, '0', 'F', '1', '1', '{module}:{entity}:delete', NULL),
({id+5}, '导出', {id+1}, 4, '', NULL, '0', 'F', '1', '1', '{module}:{entity}:export', NULL);
```

---

## 阶段 2：后端迁移

### 2.1 所有迁移深度都必须改的项（🔴 必须）

> **这些不改会编译失败或运行崩溃，无论哪种迁移深度都必须执行。**

#### 包名与架构

| 源项目（常见写法） | 本项目 |
|---|---|
| `com.xxx.yyy` / `com.ruoyi.xxx` | `plus.ruoyi.business.{module}` |
| 三层 Controller → Service → Mapper | 四层 Controller → Service → DAO → Mapper |
| `extends ServiceImpl<M, T>` | `implements IXxxService`（不继承任何基类） |
| `extends IService<T>` | `IXxxService`（自定义接口，不继承） |
| Service 中写 `QueryWrapper` / `LambdaQueryWrapper` | DAO 中 `buildQueryWrapper()` |
| Service 注入 Mapper | Service 只注入 DAO |
| Controller 注入 Service + Mapper | Controller 只注入 Service |
| `extends BaseMapperPlus<M, T, V>` | `extends BaseMapper<T>` |

#### 安全框架

| 源项目 | 本项目 |
|--------|-------|
| `@PreAuthorize("hasPermission('xxx')")` | `@SaCheckPermission("module:entity:op")` |
| `@PreAuthorize("@ss.hasRole('admin')")` | `@SaCheckRole("admin")` |
| `SecurityUtils.getLoginUser()` | `LoginHelper.getLoginUser()` |
| `SecurityUtils.getUserId()` | `LoginHelper.getUserId()` |
| `SecurityUtils.getUsername()` | `LoginHelper.getUsername()` |
| `@EnableGlobalMethodSecurity` | Sa-Token 自动配置，无需注解 |
| `UserDetails` / `Authentication` | `LoginUser`（Sa-Token） |

#### 对象转换

| 源项目 | 本项目 |
|--------|-------|
| `BeanUtils.copyProperties(source, target)` | `MapstructUtils.convert(source, Target.class)` |
| `BeanUtil.copyProperties(source, target)` | `MapstructUtils.convert(source, Target.class)` |
| 手动 `new Vo(); vo.setXxx(entity.getXxx())` | `MapstructUtils.convert(entity, Vo.class)` |
| `@Mapping` (MapStruct原生) | `@AutoMappers` + `@AutoMapper` |

#### Entity 继承（根据审计字段分析结果选择）

| 源项目情况 | 本项目处理 |
|-----------|-----------|
| 审计字段名完全匹配 + 需要多租户 | `extends TenantEntity` |
| 审计字段名完全匹配 + 不需多租户 | `extends BaseEntity` |
| 审计字段名不同 | 不继承基类，自行声明 + `@TableField` 映射 |
| 无审计字段 | 不继承基类 |
| 无基类但浅迁移 | `@TableName("原表名")` + 业务字段 |

#### 响应封装

| 源项目 | 本项目 |
|--------|-------|
| `AjaxResult.success()` | `R.ok()` |
| `AjaxResult.error("msg")` | `R.fail("msg")` 或 `throw ServiceException.of("msg")` |
| `ResponseEntity.ok()` | `R.ok()` |
| `TableDataInfo`（分页） | `R<PageResult<Vo>>` |
| `startPage()` + `getDataTable()` | `dao.page(wrapper, pageQuery)` |

#### 异常处理

| 源项目 | 本项目 |
|--------|-------|
| `throw new RuntimeException("msg")` | `throw ServiceException.of("msg")` |
| `throw new CustomException("msg")` | `throw ServiceException.of("msg")` |
| `throw new BusinessException("msg")` | `throw ServiceException.of("msg")` |

#### 日期时间

| 源项目 | 本项目 |
|--------|-------|
| `LocalDateTime.now()` | `DateUtils.getNowDate()` 或 `new Date()` |
| `LocalDateTime` 字段 | `Date` 字段（框架统一用 Date） |

#### 查询构建

| 源项目 | 本项目 |
|--------|-------|
| `new QueryWrapper<>()` | `PlusLambdaQuery.of()` |
| `new LambdaQueryWrapper<>()` | `PlusLambdaQuery.of()` |
| `.like(Entity::getStringField, val)` | `.like(Entity::getStringField, val)` |
| `.like(Entity::getLongField, val)` | `.likeCast(Entity::getLongField, val)` |
| 时间范围手动拼接 | `.between(Entity::getTime, begin, end)` |

### 2.2 标准/深度迁移额外要改的项（🟡 建议）

#### API 路径（浅迁移保留原路径）

| 源项目 | 本项目（标准/深度） |
|--------|-------|
| `GET /xxx/list` | `GET /xxx/pageXxxs` |
| `GET /xxx/{id}` | `GET /xxx/getXxx/{id}` |
| `POST /xxx` | `POST /xxx/addXxx` |
| `PUT /xxx` | `PUT /xxx/updateXxx` |
| `DELETE /xxx/{ids}` | `DELETE /xxx/deleteXxxs/{ids}` |
| `POST /xxx/export` | `POST /xxx/exportXxxs` |

### 2.3 单模块迁移步骤

每个模块按以下顺序迁移（每步完成后在蓝图中打勾）：

```
1. 数据库适配（浅迁移：无操作；深度迁移：建表 SQL）
   ↓
2. Entity.java（根据审计字段选择继承策略 + @TableName + @TableField 映射）
   ↓
3. Bo.java（@AutoMappers，@Validated 分组校验）
   ↓
4. Vo.java（@AutoMapper，@ExcelProperty）
   ↓
5. Mapper.java（extends BaseMapper<Entity>）
   ↓
6. IDao.java + DaoImpl.java（buildQueryWrapper）
   ↓
7. IService.java + ServiceImpl.java（不继承基类，注入 DAO）
   ↓
8. Controller.java（@SaCheckPermission，API路径根据深度决定）
```

### 2.4 单模块验证清单

每个模块迁移完成后必须检查：

**🔴 所有深度都必须检查**：
- [ ] 包名是 `plus.ruoyi.business.{module}.*`
- [ ] Entity 有 `@TableName`（浅迁移映射原表名，深度迁移用新表名）
- [ ] Entity 继承策略正确（根据审计字段分析结果）
- [ ] Bo 有 `@AutoMappers` 注解
- [ ] Vo 有 `@AutoMapper` 注解
- [ ] Mapper 只继承 `BaseMapper<Entity>`
- [ ] DAO 有 `buildQueryWrapper()` 方法
- [ ] Service 不继承任何基类
- [ ] Service 只注入 DAO，不注入 Mapper
- [ ] Controller 只注入 Service
- [ ] 权限注解使用 `@SaCheckPermission`
- [ ] 对象转换使用 `MapstructUtils.convert()`
- [ ] 异常使用 `ServiceException.of()`
- [ ] 无内联全限定类名（先 import 再使用短类名）
- [ ] 无 `Map<String, Object>` 传递业务数据

**🟡 标准/深度迁移额外检查**：
- [ ] API 路径符合规范（`/pageXxxs`、`/getXxx/{id}` 等）

**🟢 深度迁移额外检查**：
- [ ] Entity 继承 `TenantEntity`
- [ ] Entity 有 `@TableLogic` 标注 `isDeleted`
- [ ] 表名使用本项目前缀

---

## 阶段 3：PC 前端迁移

### 3.1 差异映射规则

#### UI 组件（标准/深度迁移替换，浅迁移可保留原组件）

| 源项目 | 本项目 | 说明 |
|--------|-------|------|
| `<el-dialog>` | `<AModal>` | 弹窗 |
| `<el-form>` (搜索) | `<ASearchForm>` | 搜索表单 |
| `<el-input>` | `<AFormInput>` | 输入框 |
| `<el-select>` | `<AFormSelect>` | 下拉框 |
| `<el-switch>` | `<AFormSwitch>` | 开关 |
| `<el-date-picker>` | `<AFormDate>` | 日期 |
| `ElMessage.success()` | 项目封装的消息组件 | 提示 |
| `ElMessageBox.confirm()` | 项目封装的确认组件 | 确认框 |

#### API 调用（🔴 所有深度都必须改）

| 源项目 | 本项目 |
|--------|-------|
| `try { const res = await api() } catch {}` | `const [err, data] = await api()` |
| `import request from '@/utils/request'` | `http` 已自动导入 |
| `request({ url, method, data })` | `http.get()` / `http.post()` / `http.put()` / `http.del()` |

#### 状态管理（🔴 所有深度都必须改）

| 源项目 | 本项目 |
|--------|-------|
| Vuex (`this.$store`) | Pinia (`defineStore`) |
| `mapState` / `mapActions` | 直接解构 `const store = useXxxStore()` |
| Options API | Composition API (`<script setup>`) |

#### 文件结构（🔴 所有深度都必须改）

| 源项目 | 本项目 |
|--------|-------|
| `views/xxx/index.vue` | `views/business/{module}/{entity}/{entity}.vue` |
| `api/xxx.js` | `api/business/{module}/{entity}/{entity}Api.ts` |
| 无类型文件 | `api/business/{module}/{entity}/{entity}Types.ts` |

### 3.2 前端迁移步骤

每个页面按以下顺序迁移：

```
1. 创建 xxxTypes.ts（类型定义）
   ↓
2. 创建 xxxApi.ts（API 定义，参考 adApi.ts 格式）
   ↓
3. 创建 xxx.vue（页面）
   - 先读参考代码 ad.vue
   - 浅迁移：可保留 el-* 组件，但 API 调用和文件结构必须改
   - 标准/深度：100% 使用 A* 组件
   - API 调用使用 [err, data] 格式
```

### 3.3 前端迁移验证清单

**🔴 所有深度都必须检查**：
- [ ] 文件名是 `{entity}.vue`（不是 `index.vue`）
- [ ] 首行有 `<!-- 中文描述 -->` 注释
- [ ] 使用 `<script setup lang="ts">`
- [ ] API 调用使用 `const [err, data] = await api()` 格式
- [ ] 类型文件和 API 文件已创建

**🟡 标准/深度迁移额外检查**：
- [ ] 0% 使用 `el-*` 原生组件
- [ ] 100% 使用 `A*` 封装组件
- [ ] 不使用 `ElMessage`，使用项目封装

---

## 阶段 4：移动端迁移

### 4.1 差异映射规则

#### UI 组件（标准/深度迁移替换，浅迁移可保留）

| 源项目 | 本项目 |
|--------|-------|
| `<uni-forms>` | `<wd-form>` |
| `<uni-field>` | `<wd-input>` |
| `<van-*>`（Vant） | `<wd-*>`（WD UI） |
| `uni.showToast()` | `useToast().success()` |
| `uni.showModal()` | `useMessage().show()` |

#### 导入方式

| 源项目 | 本项目 |
|--------|-------|
| `from 'wot-design-uni'` | `from '@/wd'` |
| `from 'vant'` | `from '@/wd'`（替换为 WD 组件） |

#### 样式

| 源项目 | 本项目 |
|--------|-------|
| `px` 单位 | `rpx` 单位 |
| `//` CSS 注释 | `/* */` CSS 注释 |

### 4.2 移动端迁移步骤

```
1. 确认页面放在 pages/ 还是 pages-sub/（主页/子页）
   ↓
2. 创建 xxxTypes.ts + xxxApi.ts
   ↓
3. 创建 xxx.vue
   - 先读参考代码 Home.vue 或 login.vue
   - 浅迁移：可保留原组件，但导入方式和样式必须改
   - 标准/深度：使用 wd-* 组件，导入用 from '@/wd'
   - 样式使用 rpx
```

### 4.3 plus-uniapp vs plus-app

| 维度 | plus-uniapp | plus-app |
|------|-------------|----------|
| 代码根目录 | `plus-uniapp/src/` | `plus-app/`（无 src） |
| 构建工具 | CLI (Vite) | HBuilderX |
| 原生插件 | 不支持 | 支持 `nativeplugins/` |
| 适用场景 | 小程序、H5 | 需要原生功能的 APP |

**用户未明确指定时必须询问目标项目！**

---

## 阶段 5：集成验证

### 5.1 全链路验证清单

**🔴 所有深度都必须验证**：
- [ ] **后端编译通过**：`mvn compile` 无错误
- [ ] **API 可调用**：每个 Controller 的 CRUD 接口都能正常响应
- [ ] **权限正确**：菜单、按钮权限配置正确
- [ ] **字典正确**：字典数据能正常加载和显示
- [ ] **PC 页面可用**：列表查询、新增、修改、删除正常
- [ ] **移动端可用**：页面渲染、API 调用正常

**🟡 标准/深度迁移额外验证**：
- [ ] **数据库初始化**：SQL 脚本执行无报错
- [ ] **导出功能**：Excel 导出正常

**🟢 深度迁移额外验证**：
- [ ] **多租户**：数据隔离正常
- [ ] **第三方集成**：支付、短信、OSS 等功能正常

### 5.2 常见问题排查

| 问题 | 可能原因 | 解决方案 |
|------|---------|---------|
| 编译报错 | import 缺失或错误 | 检查包名是否为 `plus.ruoyi.*` |
| API 404 | 路径不匹配 | 检查 `@RequestMapping` 路径和前端 API 路径是否一致 |
| 权限拒绝 | 菜单未配置 | 检查 `sys_menu` INSERT 语句 |
| 数据为空 | 租户过滤（深度迁移） | 检查 `tenant_id` 是否正确 |
| 前端组件报错 | 使用了原生组件（标准/深度迁移） | 替换为 A* / wd-* 组件 |
| API 调用异常 | try-catch 写法 | 改为 `[err, data]` 格式 |
| 字段值为 null | @TableField 映射不正确 | 检查 Entity 的 @TableField(value="源字段名") |
| 审计字段不自动填充 | Entity 未正确继承或映射 | 检查继承策略和 @TableField(fill=) 配置 |

---

## 阶段 6：优化建议（可选）

> **迁移跑通后，生成优化建议清单。每条建议独立，客户可逐条决定是否采纳。**

### 6.1 生成优化建议文档

迁移验证通过后，自动生成 `docs/migration/optimization-suggestions.md`：

```markdown
# 优化建议清单

**生成时间**: YYYY-MM-DD
**迁移深度**: {浅迁移/标准迁移}（深度迁移无需此文档）

---

## 建议 1：统一逻辑删除字段名
- **当前状态**: del_flag（源项目原字段，通过 @TableField 映射）
- **建议改为**: is_deleted（本项目规范）
- **涉及**: ALTER TABLE 重命名字段 + Entity 移除 @TableField 映射
- **影响范围**: {N} 个表、{N} 个 Entity
- **改动量**: 中
- **风险等级**: 低（纯重命名，业务逻辑不变）
- **是否采纳**: ⬜ 待客户确认

## 建议 2：主键迁移到雪花ID
- **当前状态**: AUTO_INCREMENT（源项目原策略）
- **建议改为**: 雪花ID（BIGINT，框架自动生成）
- **涉及**: 修改表主键定义 + 已有数据的 ID 保留不变 + 新数据使用雪花 ID
- **影响范围**: {N} 个表
- **改动量**: 大（已有数据需考虑兼容）
- **风险等级**: 中（需确保外键引用不断裂）
- **是否采纳**: ⬜ 待客户确认

## 建议 3：状态值含义统一
- **当前状态**: status='0' 表示正常（源项目语义）
- **建议改为**: status='1' 表示正常（本项目规范：1=积极 0=消极）
- **涉及**: UPDATE 翻转所有状态值 + 前端/移动端显示逻辑调整
- **影响范围**: {N} 个表、{N} 个页面
- **改动量**: 中
- **风险等级**: 中（容易遗漏某个页面的判断逻辑）
- **是否采纳**: ⬜ 待客户确认

## 建议 4：添加多租户支持
- **当前状态**: 无 tenant_id 字段
- **建议改为**: Entity 继承 TenantEntity + 表添加 tenant_id
- **涉及**: ALTER TABLE 添加 tenant_id + Entity 修改继承 + 配置租户排除表
- **影响范围**: {N} 个表
- **改动量**: 大
- **风险等级**: 中（需全面测试数据隔离）
- **是否采纳**: ⬜ 待客户确认

## 建议 5：统一表前缀
- **当前状态**: {原前缀}（通过 @TableName 映射）
- **建议改为**: {b_/m_/crm_/iot_}（本项目规范）
- **涉及**: RENAME TABLE + Entity @TableName 修改
- **影响范围**: {N} 个表
- **改动量**: 小
- **风险等级**: 低
- **是否采纳**: ⬜ 待客户确认

## 建议 6：API 路径规范化（仅浅迁移需要）
- **当前状态**: 保留源项目 API 路径
- **建议改为**: /pageXxxs、/getXxx/{id} 等本项目规范
- **涉及**: Controller 路径修改 + 前端/移动端 API 文件修改
- **影响范围**: {N} 个 Controller、{N} 个 API 文件
- **改动量**: 中
- **风险等级**: 低
- **是否采纳**: ⬜ 待客户确认

## 建议 7：前端组件替换（仅浅迁移需要）
- **当前状态**: 使用 el-* 原生组件
- **建议改为**: 使用 A* 封装组件
- **涉及**: 逐页替换组件
- **影响范围**: {N} 个 .vue 文件
- **改动量**: 中-大
- **风险等级**: 低
- **是否采纳**: ⬜ 待客户确认

## 建议 8：审计字段名统一
- **当前状态**: {creator/gmt_create 等}（通过 @TableField 映射）
- **建议改为**: create_by/create_time 等（本项目规范）
- **涉及**: ALTER TABLE 重命名字段 + Entity 移除 @TableField 映射 + 可继承 BaseEntity
- **影响范围**: {N} 个表、{N} 个 Entity
- **改动量**: 中
- **风险等级**: 低
- **是否采纳**: ⬜ 待客户确认
```

### 6.2 执行优化

客户确认某条建议后，在后续会话中实施：

```
用户：采纳建议 1（统一逻辑删除字段名）
   ↓
1. 生成 ALTER TABLE SQL
2. 修改 Entity 移除 @TableField("del_flag") 映射
3. 验证功能正常
4. 更新优化建议文档（⬜ → ✅）
```

---

## 跨会话协调机制

### 会话标准流程

```
┌─ 新会话开始 ──────────────────────────────────────────┐
│                                                        │
│  用户说 "继续迁移" 或 "迁移进度"                          │
│       ↓                                                │
│  1. Read docs/migration/migration-blueprint.md          │
│       ↓                                                │
│  2. 确认迁移深度（浅/标准/深度）                           │
│       ↓                                                │
│  3. 找到下一个 ⬜ 未完成的模块                             │
│       ↓                                                │
│  4. Read docs/migration/modules/{module}.md              │
│       ↓                                                │
│  5. 按迁移深度对应的检查清单逐项完成                        │
│       ↓                                                │
│  6. 更新 migration-blueprint.md（⬜ → ✅）                │
│       ↓                                                │
│  7. 更新 modules/{module}.md（打勾）                      │
│       ↓                                                │
│  8. 向用户报告进度                                       │
│                                                        │
└────────────────────────────────────────────────────────┘
```

### 进度报告模板

每个会话结束时向用户报告：

```
迁移深度：{浅迁移/标准迁移/深度迁移}

本次完成：
- [模块名] 后端迁移 ✅
  - Entity/Bo/Vo ✅（继承策略：{BaseEntity/不继承/TenantEntity}）
  - Mapper/DAO ✅
  - Service/Controller ✅

总进度：阶段2 后端迁移 (3/8 模块完成)

下一个模块：{模块名}（预计下次会话完成）
```

---

## 模块粒度控制

### 每个会话的推荐工作量

| 任务类型 | 推荐量 | 上下文消耗 |
|---------|--------|----------|
| 阶段 0 扫描 | 1 个源项目 | 中-高 |
| 数据库适配 | 3-5 个表 | 低 |
| 后端 1 个 CRUD 模块 | 1 个实体（9 个文件） | 中 |
| 后端复杂业务模块 | 1 个模块 | 高 |
| PC 前端页面 | 2-3 个页面 | 中 |
| 移动端页面 | 2-3 个页面 | 中 |

### 模块过大时的拆分策略

如果一个模块包含多个实体（如订单模块有 order、order_item、order_log），按以下策略拆分：

1. **核心实体优先**：先迁移主表（order）
2. **关联实体跟进**：再迁移子表（order_item, order_log）
3. **复杂业务最后**：最后处理跨模块业务逻辑

---

## 常见源框架专项指南

### 从原版 RuoYi 迁移

| 原版 RuoYi | 本项目（🔴 必须改） | 本项目（🟢 可选优化） |
|-----------|---|---|
| `com.ruoyi` 包名 | `plus.ruoyi` | — |
| `BaseEntity` | 根据审计字段选择继承策略 | 统一继承 TenantEntity |
| `AjaxResult` | `R<T>` | — |
| `@PreAuthorize` | `@SaCheckPermission` | — |
| `startPage()` 分页 | `dao.page(wrapper, pageQuery)` | — |
| `getDataTable()` | `entityPage.convert(Vo.class)` | — |
| 无 DAO 层 | 添加 DAO 层 | — |
| `del_flag` | @TableField("del_flag") 映射 | 重命名为 is_deleted |
| `phonenumber` | @TableField("phonenumber") 映射 | 重命名为 phone |

### 从 RuoYi-Vue-Plus 迁移

| RuoYi-Vue-Plus | 本项目（🔴 必须改） | 本项目（🟢 可选优化） |
|---------------|---|---|
| `com.ruoyi` 包名 | `plus.ruoyi` | — |
| `extends ServiceImpl<M,T>` | 不继承 | — |
| `extends IBaseService<T,V>` | 自定义 `IXxxService` | — |
| Service 内构建 QueryWrapper | DAO 层 `buildQueryWrapper()` | — |
| `BaseMapperPlus` | `BaseMapper` | — |
| `BaseEntity` | 根据审计字段选择继承策略 | 统一继承 TenantEntity |
| `BeanUtil.copyProperties()` | `MapstructUtils.convert()` | — |

### 从 SpringBlade 迁移

| SpringBlade | 本项目（🔴 必须改） | 本项目（🟢 可选优化） |
|------------|---|---|
| `org.springblade` 包名 | `plus.ruoyi` | — |
| `@ApiOperation` (Swagger 2) | JavaDoc 注释 | — |
| `extends BladeService` | 不继承 | — |
| Wrapper 在 Service | DAO 层 `buildQueryWrapper()` | — |
| `R.data()` | `R.ok()` | — |
| `Condition.getPage()` | `pageQuery` 参数 | — |

### 从 JPA/Hibernate 项目迁移

| JPA | 本项目（🔴 必须改） | 本项目（🟢 可选优化） |
|----|---|---|
| `@Entity` | `@TableName("原表名")` | 改为本项目表前缀 |
| `@Id @GeneratedValue` | `@TableId(value = "id")` | 迁移到雪花ID |
| `JpaRepository` | `BaseMapper` + DAO | — |
| JPQL / HQL | MyBatis-Plus `PlusLambdaQuery` | — |
| `@ManyToOne` / `@OneToMany` | 手动关联查询 | — |
| `CrudRepository.save()` | `dao.insert()` / `dao.updateById()` | — |
| `findByXxxAndYyy()` | `buildQueryWrapper()` | — |

---

## 常见陷阱

### 陷阱 1：浅迁移时忘记 @TableField 映射

**症状**：字段值全部为 null
**原因**：Entity 属性名与数据库字段名不一致，又没有 @TableField 映射
**解决**：属性名与字段名不同时，必须加 `@TableField("源字段名")`

### 陷阱 2：浅迁移时审计自动填充失效

**症状**：新增/修改记录时 create_by、update_time 等字段为 null
**原因**：未继承 BaseEntity，自定义的审计字段没有配置 `fill` 属性
**解决**：自定义审计字段时需要加 `@TableField(value = "源字段名", fill = FieldFill.INSERT)` 等

### 陷阱 3：状态值含义不一致（浅/标准迁移）

**症状**：启用的记录显示为禁用
**原因**：源项目 `status='0'` 表示正常，本项目前端可能默认 `'1'` 表示正常
**解决**：浅/标准迁移时，前端页面也保留源项目的状态值语义；深度迁移时翻转数据

### 陷阱 4：API 路径不匹配（浅迁移）

**症状**：前端调用 404
**原因**：浅迁移时后端保留了原路径，但前端 API 文件写了本项目规范路径
**解决**：浅迁移时前端 API 路径必须与后端保持一致（都用原路径）

### 陷阱 5：Service 继承了基类

**症状**：编译通过但风格不一致
**原因**：习惯性继承 `ServiceImpl`
**解决**：Service 只 `implements IXxxService`，注入 DAO

### 陷阱 6：忘记跨数据库兼容

**症状**：MySQL 正常但 PostgreSQL 报错
**原因**：Long 类型字段使用了 `like()` 而非 `likeCast()`
**解决**：非 String 字段搜索统一使用 `likeCast()`

### 陷阱 7：对象转换方式错误

**症状**：字段值丢失或类型不匹配
**原因**：使用了 `BeanUtils.copyProperties()` 或手动 setter
**解决**：统一使用 `MapstructUtils.convert()`

### 陷阱 8：上下文溢出

**症状**：会话中途被截断
**原因**：一次性迁移太多模块
**解决**：严格控制每个会话 1-2 个模块，完成后更新蓝图文档

### 陷阱 9：深度迁移中已有数据的主键冲突

**症状**：新增记录时主键冲突
**原因**：原表使用自增主键，已有数据 ID 可能与雪花 ID 冲突
**解决**：深度迁移时，保留已有数据的原 ID，只对新数据使用雪花 ID（设置 AUTO_INCREMENT 起始值远大于已有最大 ID）

---

## 与其他技能的协作

迁移过程中会触发以下技能：

| 迁移阶段 | 关联技能 | 用途 |
|---------|---------|------|
| 阶段 1 | `database-ops` | 建表、字典、菜单 |
| 阶段 2 | `crud-development` | 后端四层架构代码 |
| 阶段 2 | `backend-annotations` | 注解使用规范 |
| 阶段 2 | `security-guard` | 权限注解迁移 |
| 阶段 3 | `ui-pc` | PC 前端组件使用 |
| 阶段 3 | `store-pc` | 状态管理迁移 |
| 阶段 4 | `ui-mobile` | 移动端组件使用 |
| 阶段 4 | `store-mobile` | 移动端状态管理 |
| 阶段 5 | `bug-detective` | 集成问题排查 |
| 进度追踪 | `task-tracker` | 跨会话任务管理 |

---

## 快速参考卡片

### 迁移深度速查

```
浅迁移：   只改架构必须项，数据库不动，@TableField 映射
标准迁移：  浅迁移 + API 路径规范 + 组件替换
深度迁移：  完全对齐本项目规范（改库 + 改字段 + 加多租户）
```

### 后端迁移速查（🔴 所有深度必须）

```
包名:       com.xxx → plus.ruoyi.business.{module}
Entity:     @TableName("原表名") + 根据审计字段选择继承策略
Bo:         @AutoMappers + 校验注解
Vo:         @AutoMapper + @ExcelProperty
Mapper:     extends BaseMapper<Entity>
DAO:        buildQueryWrapper() + extends BaseDaoImpl
Service:    implements IXxxService（不继承）
Controller: @SaCheckPermission + API路径根据深度决定
转换:       MapstructUtils.convert()
异常:       ServiceException.of()
日期:       DateUtils.getNowDate()
```

### Entity 继承策略速查

```
审计字段匹配 + 需多租户  → extends TenantEntity
审计字段匹配 + 不需多租户 → extends BaseEntity
审计字段名不同           → 不继承，自行声明 + @TableField 映射
无审计字段              → 不继承
```

### 前端迁移速查

```
组件:   浅迁移可保留 el-*；标准/深度用 A* 封装组件
API:    [err, data] = await api()（所有深度必须）
消息:   项目封装（不用 ElMessage）
文件名: {entity}.vue（不是 index.vue）
首行:   <!-- 中文描述 -->
类型:   xxxTypes.ts（必须创建）
```

### 移动端迁移速查

```
组件:   浅迁移可保留原组件；标准/深度用 wd-*
导入:   from '@/wd'（不是 'wot-design-uni'）
提示:   useToast().success()（不用 uni.showToast）
样式:   rpx 单位 + /* */ 注释
```
