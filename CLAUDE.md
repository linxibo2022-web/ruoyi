# CLAUDE.md - ruoyi-plus-uniapp 项目

## 语言设置
**必须使用中文**与用户对话。

## 🔴 文件编码与注释规范（必须遵守）

### 1. 统一编码
- **所有源码与配置文件统一强制使用 UTF-8（无 BOM）**
- 包括但不限于：`.java`、`.vue`、`.ts`、`.js`、`.xml`、`.yml`、`.properties`、`.sql`、`.md`
- **绝对禁止**：UTF-8 with BOM、GBK、GB2312、ANSI、ISO-8859-1 等混用
- 全局编码、项目编码统一强制设置为 UTF-8
- 属性文件（`.properties`）默认编码强制统一为 UTF-8
- 创建 UTF-8 文件时必须强制使用 UTF-8（无 BOM）

### 2. 中文内容规范
- 中文注释、中文日志、中文文档必须可读，不允许乱码（如"鍥藉"）
- 发现乱码优先检查文件实际编码与 IDE 显示编码是否一致

### 3. 注释规范
- 保留已有业务注释，不随意删除历史说明和关键 `//` 注释块
- 新增代码必须补充必要注释，说明"为什么这样做"，避免空泛注释
- 接口方法注释至少包含：用途、参数、返回值、异常/边界行为

### 4. Java 文件保存规则
- 保存 Java 文件时强制 UTF-8 no BOM
- 批量改文件后，必须抽查文件头字节，确认无 `EF BB BF`

### 5. `java: 非法字符: '\ufeff'` 处理规则
- 该报错优先判定为 BOM 问题
- 处理步骤：1) 定位报错文件 → 2) 移除文件头 BOM → 3) 批量扫描同目录 `.java` 文件是否也有 BOM → 4) 重新编译验证

### 6. 工具链与提交前检查
- IDE 默认编码设置为 UTF-8 且关闭 with BOM
- 提交前执行一次编译（如 `mvn -DskipTests compile`）
- 若涉及注释修改，提交前人工检查中文可读性与注释完整性

### 7. 变更原则
- 功能改动与注释改动尽量分开，便于回溯
- 修编码问题时不改业务逻辑，只做最小必要修改

---

## 术语约定
| 术语 | 含义 | 对应目录 |
|------|------|---------|
| **前端** | PC 端 | `plus-ui/` |
| **移动端(CLI)** | 小程序/H5/不需原生插件的APP | `plus-uniapp/` |
| **移动端(原生APP)** | 需要原生插件的APP/鸿蒙APP | `plus-app/` |
| **后端** | Java 服务 | `ruoyi-modules/` |

### 移动端双项目识别（必须遵守）

| 用户关键词 | 目标项目 | 目录结构 |
|-----------|---------|---------|
| "plus-app"、"APP端"、"原生"、"HBuilderX"、"鸿蒙" | **plus-app** | 扁平化（无 `src/`） |
| "plus-uniapp"、"小程序"、"H5"、"公众号" | **plus-uniapp** | `src/` 下组织 |
| "移动端"、"APP"（无明确指定） | **必须询问用户** | - |

**路径差异**：plus-uniapp 代码在 `src/` 下（如 `src/api/`），plus-app 代码在项目根下（如 `api/`），其他规范完全相同。

**绝对禁止**：用户说 "plus-app" 时把文件写到 `plus-uniapp/`，或反之。不确定时**必须先问**。

## MCP 工具触发

| 触发词 | 工具 | 用途 |
|-------|------|------|
| 深度分析、仔细思考、全面评估 | `sequential-thinking` | 链式推理，多步骤分析 |
| 最佳实践、官方文档、标准写法 | `context7` | Vue/Element Plus/UniApp/MyBatis-Plus 等（⚠️ WD UI 禁用，只参考 `plus-uniapp/src/wd/`） |
| 打开浏览器、截图、检查元素 | `chrome-devtools` | 浏览器调试 |
| 用工作站、workstation、ai-workstation | `ai-workstation` | AI 工作站 MCP（route → get_skill → 执行） |

### 端口约定

| 端 | 默认端口 | 配置位置 |
|-----|---------|---------|
| **后端** | `server.port`（yml 配置） | `ruoyi-admin/src/main/resources/application.yml` |
| **前端** | 80（或 81/82 顺延） | `plus-ui/vite.config.ts` |
| **移动端** | 5173 | `plus-uniapp/vite.config.ts` |

> ⚠️ 使用 Chrome DevTools、发起 HTTP 请求或其他需要端口的操作前，如果不确定当前端口，**必须先问用户确认**。

### 时区约定

**所有日期时间必须使用东八区（UTC+8，Asia/Shanghai）**。获取当前时间时使用：
```bash
TZ=Asia/Shanghai date '+%Y-%m-%d %H:%M'
```
> 适用范围：项目状态文档、待办清单、需求文档、任务跟踪、进度报告等所有涉及时间戳的场景。

## 🔴 Skills 强制评估（必须遵守）

> **每次用户提问时，UserPromptSubmit Hook 会注入技能评估提示（以 `## 强制技能激活流程` 开头）。必须严格遵循！**

**流程**：
1. **评估**：根据注入的技能列表，列出匹配的技能及理由（无匹配则写"无匹配技能"）
2. **激活**：对每个匹配的技能调用 `Skill(技能名)`
3. **实现**：激活完成后开始实现

**Skills 位置**：`.claude/skills/[skill-name]/SKILL.md`（共 29 个专业技能）

---

## 🔴 经验沉淀目录加载（会话开始时）

**会话开始（首次响应用户）前**，若 `.claude/docs/experience/` 存在内容，必须执行：

```bash
ls -t .claude/docs/experience/*/*-exp-summary.md 2>/dev/null | head -1
```

若有输出 → 读取这一份最近的摘要文件，作为本次会话的经验上下文（已沉淀的禁令、踩过的坑、待观察事项）。

**Why**：`/exp` 沉淀的经验不会被 hook 自动注入，必须主动读一次最近摘要，否则等于沉淀了又"记不住"。读完即可，不必复述给用户。

**例外**：若用户首条消息是简单问候（"你好"、"在吗"）或与本项目无关的纯通用问题，可跳过加载。

---

## 🔴 多会话并发自动避让协议（L1/L2/L3 三层触发）

> 用户可能同时开多个 Claude Code 会话。本会话必须**自动感知并避让**其他会话的工作，**默认静默执行，不打扰用户**。
> 设计原则：宁可绕路，绝不覆盖；宁可静默放弃，绝不擅自 stash / reset / checkout。

### L1 — 启动时探测（首次响应前，仅执行一次）

```bash
git status -s
git branch --show-current
```

- 把"未提交文件清单"和"当前分支"记入会话上下文，整个会话复用，**不向用户复述**
- 若清单非空且与本次任务无关 → 视为"他者占用区"，本会话**不修改、不 stash、不 checkout、不 reset** 这些文件
- 若清单非空且与本次任务相关（用户接续之前的工作）→ 当作己方未完成工作正常处理

### L2 — 修改文件前（按需触发，单文件粒度）

修改任意已存在文件**之前**，执行：

```bash
git log -1 --format="%ar|%s" <file>
```

判定规则（严格按此执行，不询问）：

| 条件 | 处置 |
|------|------|
| 距今 ≥ 15 分钟 | ✅ 自由修改 |
| 距今 < 15 分钟 + 文件**不在** L1 未提交清单 | ✅ 自由修改（已提交的近期改动不冲突） |
| 距今 < 15 分钟 + 文件**在** L1 未提交清单 + 可绕开（新增功能/换路径） | ⚠️ **静默换路径绕开**，不告知用户 |
| 距今 < 15 分钟 + 文件**在** L1 未提交清单 + 必须改同文件 | 🛑 **此时唯一允许打扰用户一次**："`<file>` 15min 内有未提交改动，疑似其他会话占用，是否继续？" |

### L3 — 提交前（强校验，必做）

`git commit` 前：

```bash
git diff --cached --name-only
```

- 对照本会话明确改过的文件清单（自维护）
- 越界文件 → **静默 `git restore --staged <file>`**，仅提交本会话范围内文件
- 逐个 `git add <具体文件>`，**禁止** `git add -A` / `git add .`
- commit message 末尾可附 `[scope: <模块>]` 标识本次会话范围

### 跨会话操作禁令（不询问、直接禁止）

| 禁令 | 原因 |
|------|------|
| ❌ `git stash` / `git stash pop` | 会污染其他会话的工作区 |
| ❌ `git reset --hard` | 会丢其他会话的未提交改动 |
| ❌ `git checkout <file>`（丢弃改动） | 同上 |
| ❌ `git checkout <branch>`（切分支） | 除非用户明确指示 |
| ❌ `git add -A` / `git add .` | 可能误提交他者文件，必须逐个 add |
| ❌ `git clean -fd` | 会删他者未跟踪文件 |
| ❌ kill 端口 / `taskkill /F` 进程 | 他者 dev server 可能在用 |
| ❌ 删除 `docs/tasks/active/` 下非本会话任务文档 | 同上 |
| ❌ `npx kill-port` 不属于本会话启动的端口 | 同上 |

### 高并发场景升级 → worktree

若用户明确"并行开发"或预计 30+ 分钟同时改**不同模块**，主动建议：

```bash
claude --worktree feature-x
```

官方原生支持（[Common workflows](https://code.claude.com/docs/en/common-workflows)），自动隔离目录 + 分支。**3-5 个并行最佳**，5+ 会撞 API 速率限制。
> 注意：worktree **不能**隔离数据库、Redis、端口（8080/80/5173）。同时跑 dev server 仍需手动错开端口或 profile。

---

## ⚠️ 页面开发强制要求（最高优先级）

**开发前必须：先读参考代码 → 了解封装组件 → 按相同风格编写**

### 参考代码位置

| 开发类型 | plus-uniapp 参考代码 | plus-app 参考代码 |
|---------|---------------------|------------------|
| **前端页面** | `plus-ui/src/views/business/base/ad/ad.vue` | - |
| **前端 API** | `plus-ui/src/api/business/base/ad/adApi.ts` | - |
| **移动端列表** | `plus-uniapp/src/components/tabbar/Home.vue` | `plus-app/components/tabbar/Home.vue` |
| **移动端表单** | `plus-uniapp/src/pages/auth/login.vue` | `plus-app/pages/auth/login.vue` |
| **移动端 API** | `plus-uniapp/src/api/app/home/homeApi.ts` | `plus-app/api/app/home/homeApi.ts` |

### 文件首行注释

`.vue` 文件第一行加 `<!-- 中文描述 -->`，`.ts` 文件第一行加 `// 中文描述`（已有 `/**` JSDoc 的不加）。

### 强制使用封装组件

| 场景 | 前端 (plus-ui) | 移动端 (plus-uniapp) | 禁止使用 |
|------|--------------|-----------------|--------|
| 搜索表单 | `ASearchForm` | `wd-form` | `el-form`, `uni-forms` |
| 输入框 | `AFormInput` | `wd-input` | `el-input`, `uni-field` |
| 下拉框 | `AFormSelect` | `wd-select` | `el-select` |
| 日期选择 | `AFormDate` | `wd-datetimepicker` | `el-date-picker` |
| 弹窗 | `AModal` | `wd-popup` | `el-dialog` |
| 提示消息 | 项目封装 | `useToast()` | `ElMessage`, `uni.showToast()` |

---

## 核心架构（必须牢记）

| 项目 | 规范 |
|------|------|
| **包名** | `plus.ruoyi.*` |
| **四层架构** | Controller → Service（不继承）→ DAO（buildQueryWrapper）→ Mapper |
| **对象转换** | `MapstructUtils.convert()` |
| **Entity基类** | `TenantEntity`（多租户版本） |
| **BO/VO映射** | `@AutoMappers` 注解 |
| **主键策略** | 雪花ID（不用 AUTO_INCREMENT） |
| **⚠️ 类引用** | **先 import 完整类，再使用短类名（绝对禁止内联全限定名）** |

### 模块与表前缀对应

| 模块 | 表前缀 | 包路径 | 示例 |
|------|--------|--------|------|
| base | `b_` | `plus.ruoyi.business.base` | `b_ad`, `b_platform` |
| mall | `m_` | `plus.ruoyi.business.mall` | `m_goods`, `m_order` |
| crm | `crm_` | `plus.ruoyi.business.crm` | `crm_customer` |
| iot | `iot_` | `plus.ruoyi.business.iot` | `iot_device` |
| 系统 | `sys_` | `plus.ruoyi.system` | `sys_user`, `sys_menu` |

---

## 绝对禁止的写法

| 项目 | 错误做法 | 正确做法 |
|------|---------|---------|
| **后端包名** | `com.ruoyi.xxx` | `plus.ruoyi.xxx` |
| **Service 继承** | `extends ServiceImpl<>` | `implements IXxxService` |
| **查询构建位置** | Service 层直接用 `LambdaQueryWrapper` | DAO 层的 `buildQueryWrapper()` |
| **Service 中写查询** | Service 中 `PlusLambdaQuery.of()` | 调用 `dao.buildQueryWrapper(bo)` |
| **Controller 注入 DAO** | Controller 注入 `IXxxDao` | Controller 只注入 `IXxxService` |
| **Service 注入 Mapper** | Service 注入 `XxxMapper` | Service 只注入 `IXxxDao` |
| **跨 Maven 模块注入 DAO** | 跨模块直接注入对方 `IXxxDao` | 注入对方 `IXxxService`（同 Maven 模块内可直接注入 DAO） |
| **DAO 做对象转换** | DAO 中 `MapstructUtils.convert()` | Service 层做对象转换 |
| **DAO 写事务注解** | DAO 中 `@Transactional` | Service 层管理事务 |
| **返回值类型** | `Map<String, Object>` | 具体的 VO 类 |
| **类型引用** | `plus.ruoyi.xxx.Xxx` (内联全限定名) | `import xxx.Xxx` 后用 `Xxx` |
| **对象转换** | `BeanUtil.copyProperties()` | `MapstructUtils.convert()` |
| **API 路径** | `/page` 或 `/{id}` | `/pageXxxs` 或 `/getXxx/{id}` |
| **当前时间** | `LocalDateTime.now()` | `DateUtils.getNowDate()` 或 `new Date()` |
| **主键策略** | `AUTO_INCREMENT` | 雪花ID |
| **普通 CRUD 注解** | `@Schema(description="")` | 使用 JavaDoc 注释 |
| **@Cacheable 返回值** | `List.of()`, `Set.of()`, `Map.of()` | `new ArrayList/HashSet/HashMap()` |
| **组件导入（移动端）** | `from 'wot-design-uni'` | `from '@/wd'` |
| **原生组件（前端）** | `el-input`, `el-dialog`, `el-form` | `AFormInput`, `AModal`, `ASearchForm` |
| **原生组件（移动端）** | `uni-forms`, `uni-field` | `wd-form`, `wd-input` |
| **API 异步调用** | `try { await api() } catch {}` | `const [err, data] = await api()` |
| **消息提示（前端）** | `ElMessage.success()` | 使用项目封装 |
| **消息提示（移动端）** | `uni.showToast()` | `useToast().success()` |
| **菜单字段名** | `is_frame`（原版 RuoYi） | `is_external_link`（本项目） |
| **Vue 页面文件名** | `index.vue`（业务页面） | `业务名.vue`（如 `ad.vue`、`goods.vue`） |
| **菜单默认值** | `visible='0'`, `status='0'` | `visible='1'`, `status='1'`（1=积极 0=消极） |
| **文件首行注释（.vue）** | 无首行注释 | `<!-- 描述 -->` 作为第一行 |
| **文件首行注释（.ts）** | 无首行注释（且无 JSDoc） | `// 描述` 作为第一行 |

### ⚠️ Bash/Shell 禁止项（最常犯错误！）

```bash
# ❌ 禁止：使用 > nul（Windows 会创建名为 nul 的文件！）
command > nul
command 2> nul

# ✅ 正确：不使用任何输出重定向，或使用跨平台方式
command
# 如果必须抑制输出，使用：
command > /dev/null 2>&1
```

**为什么会出错**：Windows 的 `nul` 设备在某些 Shell 环境下不被识别，会被当作普通文件名创建。

---

## API 路径规范

| 操作 | HTTP方法 | 路径格式 | 示例 |
|------|---------|---------|------|
| 分页查询 | GET | `/page{实体复数}` | `/pageAds` |
| 列表查询 | GET | `/list{实体复数}` | `/listAds` |
| 获取详情 | GET | `/get{实体}/{id}` | `/getAd/{id}` |
| 新增 | POST | `/add{实体}` | `/addAd` |
| 修改 | PUT | `/update{实体}` | `/updateAd` |
| 删除 | DELETE | `/delete{实体复数}/{ids}` | `/deleteAds/{ids}` |

---

## 前端核心规范 (plus-ui)

### API 定义示例

```typescript
// xxxApi.ts - http/Result/PageResult/PageQuery 已自动导入，无需 import
import type { XxxQuery, XxxBo, XxxVo } from './xxxTypes'

export const pageXxxs = (query?: XxxQuery): Result<PageResult<XxxVo>> => {
  return http.get('/base/xxx/pageXxxs', query)
}

export const getXxx = (id: string | number): Result<XxxVo> => {
  return http.get(`/base/xxx/getXxx/${id}`)
}

export const addXxx = (data: XxxBo): Result<string | number> => {
  return http.post('/base/xxx/addXxx', data)
}

export const updateXxx = (data: XxxBo): Result<void> => {
  return http.put('/base/xxx/updateXxx', data)
}

export const deleteXxxs = (ids: string | number | Array<string | number>): Result<void> => {
  return http.del(`/base/xxx/deleteXxxs/${ids}`)
}
```

### 类型定义示例

```typescript
export interface XxxQuery extends PageQuery {
  xxxName?: string
  status?: string
}

export interface XxxBo {
  id?: string | number
  xxxName?: string
  status?: string
}

export interface XxxVo {
  id: string | number
  xxxName: string
  status: string
  createTime: string
}
```

---

## 移动端核心规范 (plus-uniapp / plus-app 通用)

### 组件导入

```typescript
import { useToast, useMessage } from '@/wd'  // 必须用 @/wd，不是 'wot-design-uni'
const toast = useToast()
toast.success('操作成功')
```

### 样式规范

```scss
.box {
  width: 200rpx;  // ✅ 使用 rpx
  padding: 24rpx;
}
/* ✅ CSS 注释必须用 /* */ */
/* ❌ 禁止使用 // 注释 */
```

---

## 数据库设计规范

### 建表模板（雪花ID）

```sql
-- 表前缀根据模块：b_(base) / m_(mall) / crm_(crm) / iot_(iot) / sys_(系统)
CREATE TABLE {前缀}_xxx (
    id           BIGINT(20)   NOT NULL COMMENT '主键ID',  -- 雪花ID，不用 AUTO_INCREMENT
    tenant_id    VARCHAR(20)  DEFAULT '000000' COMMENT '租户ID',

    -- 业务字段
    xxx_name     VARCHAR(100) NOT NULL COMMENT '名称',
    status       CHAR(1)      DEFAULT '1' COMMENT '状态(0停用 1正常)',

    -- 审计字段（必须）
    create_dept  BIGINT(20)   DEFAULT NULL COMMENT '创建部门',
    create_by    BIGINT(20)   DEFAULT NULL COMMENT '创建人',
    create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by    BIGINT(20)   DEFAULT NULL COMMENT '更新人',
    update_time  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark       VARCHAR(255) DEFAULT NULL COMMENT '备注',
    is_deleted   CHAR(1)      DEFAULT '0' COMMENT '是否删除(0正常 1已删除)',

    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='xxx表';
```

### SQL 文件位置

- **MySQL**: `script/sql/ry_plus_new.sql`
- **Oracle**: `script/sql/oracle/oracle_ry_plus_new.sql`
- **PostgreSQL**: `script/sql/postgres/postgres_ry_plus_new.sql`
- **SQL Server**: `script/sql/sqlserver/sqlserver_ry_plus_new.sql`

---

## 常见错误速查

### 后端工具类优先级

> 优先使用项目工具类 > Hutool > 自己实现

| 工具类 | 用途 | 常用方法 |
|--------|------|---------|
| `MapstructUtils` | 对象转换 | `convert(source, Target.class)` |
| `StringUtils` | 字符串处理 | `isBlank()`, `isNotBlank()`, `format()` |
| `PlusLambdaQuery` | 查询构建 | `eq()`, `like()`, `likeCast()`, `between()` |
| `TreeBuildUtils` | 树结构构建 | `build(list, rootId, nodeParser)` |
| `ObjectUtils` | 对象判断 | `isNull()`, `isNotNull()`, `isEmpty()` |
| `CollUtil` | 集合操作 | `isEmpty()`, `isNotEmpty()` (Hutool) |
| `ServiceException` | 业务异常 | `ServiceException.of("错误信息")` |

**跨数据库查询**：String 类型用 `like()`，其他类型用 `likeCast()`

### 后端常见错误

| 错误写法 | 正确写法 |
|---------|---------|
| `plus.ruoyi.xxx.Xxx` (内联全限定名) | `import plus.ruoyi.xxx.Xxx;` 然后用 `Xxx` |
| `extends BaseEntity` | `extends TenantEntity`（多租户版） |
| `BeanUtil.copyProperties()` | `MapstructUtils.convert()` |
| `extends ServiceImpl<>` | `implements IXxxService` |
| `new LambdaQueryWrapper` (Service层) | `buildQueryWrapper()` (DAO层) |
| `@GetMapping("/page")` | `@GetMapping("/pageXxxs")` |
| `@GetMapping("/{id}")` | `@GetMapping("/getXxx/{id}")` |
| `like(Xxx::getId, value)` | `likeCast(Xxx::getId, value)` |
| `phonenumber` (原版RuoYi字段) | `phone`（本项目手机号字段） |
| `del_flag` (原版RuoYi字段) | `is_deleted`（本项目逻辑删除字段） |
| `AUTO_INCREMENT` | 不用，使用雪花ID |
| `R.ok(stringValue)` (返回String到data) | `R.ok(null, stringValue)` |
| `@Schema(description = "xxx")` | 普通CRUD不用，仅 @OpenApi 接口需要 |
| `LocalDateTime.now()` | `DateUtils.getNowDate()` 或 `new Date()`（框架统一用 `Date`） |

### 前端/移动端常见错误

| 错误写法 | 正确写法 | 适用端 |
|---------|---------|--------|
| **未读现有代码直接开发** | **先 Read ad.vue 等参考代码** | **前端/移动端** |
| 页面文件命名 `index.vue` | `业务名.vue`（如 `ad.vue`、`goods.vue`） | 前端 |
| `from 'wot-design-uni'` | `from '@/wd'` | 移动端 |
| `<el-dialog>` | `<AModal>` | 前端 |
| `<el-input>` | `<AFormInput>` | 前端 |
| `<el-select>` | `<AFormSelect>` | 前端 |
| `<el-form inline>` | `<ASearchForm>` | 前端 |
| `<el-switch>` | `<AFormSwitch>` | 前端 |
| `<uni-forms>` | `<wd-form>` | 移动端 |
| `<uni-field>` | `<wd-input>` | 移动端 |
| `try { await api() }` | `const [err, data] = await api()` | 前端/移动端 |
| `ElMessage.success()` | 使用项目封装的消息组件 | 前端 |
| `uni.showToast()` | `useToast().success()` | 移动端 |

---

## 后端标准模块位置

```
ruoyi-modules/ruoyi-business/src/main/java/plus/ruoyi/business/base/
├── controller/AdController.java
├── service/IAdService.java
├── service/impl/AdServiceImpl.java
├── dao/IAdDao.java
├── dao/impl/AdDaoImpl.java
├── mapper/AdMapper.java
└── domain/
    ├── Ad.java
    ├── bo/AdBo.java
    └── vo/AdVo.java
```

---

## 移动端目录结构

### plus-uniapp（小程序/H5/CLI构建APP）
```
plus-uniapp/src/          # 注意：有 src/ 层级
├── pages/                # 主页面（自动路由）
├── pages-sub/            # 子页面（分包）
├── components/           # 业务组件
├── api/                  # API 定义
├── composables/          # Composables (useAuth, usePayment 等)
└── wd/                   # WD UI 封装
```

### plus-app（原生APP/HBuilderX构建）
```
plus-app/                 # 注意：无 src/ 层级，扁平化
├── pages/                # 主页面（自动路由）
├── pages-sub/            # 子页面（分包）
├── components/           # 业务组件
├── api/                  # API 定义
├── composables/          # Composables
├── wd/                   # WD UI 封装
├── nativeplugins/        # 原生插件（plus-app 独有）
└── harmony-configs/      # 鸿蒙配置（plus-app 独有）
```

> 路由自动生成：两个项目都使用 `uni-pages` 插件，修改 `pages/` 或 `pages-sub/` 后自动生成 `pages.json`。

---

## 快速命令

| 命令 | 用途 |
|------|------|
| `/dev` | 开发新功能（双模式：直接生成代码 / 生成配置手动生成） |
| `/crud` | 快速生成 CRUD |
| `/check` | 代码规范检查 |
| `/init-docs` | 初始化项目文档（支持空白模板/扫描代码） |
| `/progress` | 查看项目进度 |
| `/add-todo` | 快速添加待办事项 |

---

## 🔴 开发前检查清单

开发任何功能前，必须完成以下检查：

- [ ] **已读参考代码** - 前端/移动端/后端的标准模块示例
- [ ] **已了解项目规范** - 包名、包结构、命名约定
- [ ] **已了解组件库** - 使用的是项目封装组件，不是原生组件
- [ ] **已确认 API 调用方式** - 前端/移动端使用 `[err, data]` 格式
- [ ] **不违反禁止项** - 检查"绝对禁止的写法"表格
- [ ] **代码风格一致** - 完全复制参考代码的风格
