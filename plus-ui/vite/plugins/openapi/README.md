# OpenAPI 代码生成插件 - 改造说明

## 改造目标

实现智能文件生成策略,避免覆盖用户手动修改的代码,同时提供自动生成的参考文件。

## 核心改造点

### 1. 智能文件生成策略

#### 生成规则

| 场景 | 操作 | 生成文件 |
|------|------|---------|
| 文件不存在 | 直接创建 | `xxxApi.ts` / `xxxTypes.ts` |
| 文件存在且内容相同(MD5相同) | 跳过生成 | 无 |
| 文件存在且内容不同(MD5不同) | 生成参考文件 | `xxxApi.generated.ts` / `xxxTypes.generated.ts` |

#### 判断逻辑

```typescript
// 通过比较原文件和新生成内容的MD5哈希值来判断是否被修改
const existingHash = getFileHash('xxxApi.ts')  // 读取原文件的MD5
const newHash = getContentHash(newContent)     // 计算新内容的MD5

if (existingHash === newHash) {
  // 哈希相同 → 内容未变 → 跳过生成
} else {
  // 哈希不同 → 内容已变 → 生成 .generated.ts
}
```

**优势:**
- ✅ 精确判断内容是否改变(任何字符变化都能检测)
- ✅ 忽略格式差异(自动标准化换行符和空白)
- ✅ 性能优秀(MD5计算速度快)

### 2. 接口自动排序

生成的 API 文件会按照标准 CRUD 顺序自动排序:

#### 排序规则

| 优先级 | 操作类型 | 函数名前缀 | 示例 |
|-------|---------|-----------|------|
| 1 | 分页查询 | `page`, `list`, `query` | `pageAds`, `listUsers` |
| 2 | 单个查询 | `get`, `detail`, `info` | `getAd`, `detailOrder` |
| 3 | 新增 | `add`, `create`, `insert`, `save` | `addAd`, `createUser` |
| 4 | 修改 | `update`, `edit`, `modify` | `updateAd`, `editUser` |
| 5 | 删除 | `delete`, `remove` | `deleteAds`, `removeUser` |
| 6 | 批量操作 | `batch`, `import`, `export` | `batchSave`, `exportData` |
| 7 | 其他 | 其他函数名 | `sendNotify`, `calculate` |

#### 排序示例

**排序前** (OpenAPI 原始顺序):
```typescript
export const deleteAds = (ids: number[]): Result<void> => { }
export const addAd = (data: AdBo): Result<number> => { }
export const getAd = (id: number): Result<AdVo> => { }
export const pageAds = (query?: AdQuery): Result<PageResult<AdVo>> => { }
export const updateAd = (data: AdBo): Result<void> => { }
```

**排序后** (自动排序):
```typescript
export const pageAds = (query?: AdQuery): Result<PageResult<AdVo>> => { }
export const getAd = (id: number): Result<AdVo> => { }
export const addAd = (data: AdBo): Result<number> => { }
export const updateAd = (data: AdBo): Result<void> => { }
export const deleteAds = (ids: number[]): Result<void> => { }
```

**优势**:
- ✅ 统一的代码风格
- ✅ 更好的可读性
- ✅ 符合开发习惯 (查询 → 增 → 改 → 删)
- ✅ 自动处理,无需手动调整

### 3. 忽略配置功能

支持灵活的忽略配置,避免生成不需要的模块/文件/接口:

#### 配置方式

```typescript
createOpenApiPlugin({
  input: 'http://127.0.0.1:5500/v3/api-docs/business',
  output: 'src/api',
  ignore: {
    // 方式1: 忽略指定模块名(精确匹配)
    modules: ['ad', 'order'],

    // 方式2: 忽略指定路径(支持通配符)
    paths: [
      '/base/ad/**',        // 忽略所有 /base/ad 下的接口
      '/common/mall/*',     // 忽略 /common/mall 一级路径下的接口
      '/base/*/detail'      // 忽略所有 detail 接口
    ],

    // 方式3: 忽略指定文件名(支持通配符)
    files: [
      'system*',            // 忽略 systemApi.ts 和 systemTypes.ts
      '*Test*',             // 忽略所有包含 Test 的文件
      'debug*.ts'           // 忽略 debugApi.ts, debugTypes.ts 等
    ],

    // 方式4: 忽略指定接口函数名(支持通配符)
    functions: [
      'template*',          // 忽略 templateAdd, templateUpdate 等
      '*Test',              // 忽略 getUserTest, addOrderTest 等
      'debug*'              // 忽略 debugInfo, debugLog 等
    ],

    // 方式5: 自定义过滤函数
    filter: (moduleKey, apis) => {
      // 忽略测试模块
      if (moduleKey.includes('test')) return true
      // 忽略接口数量少于3的模块
      if (apis.length < 3) return true
      return false
    }
  }
})
```

#### 接口函数名匹配规则

| 模式 | 说明 | 匹配示例 |
|------|------|---------|
| `template*` | 匹配以 template 开头 | `templateAdd`, `templateUpdate`, `templateList` |
| `*Test` | 匹配以 Test 结尾 | `getUserTest`, `addOrderTest` |
| `*Debug*` | 匹配包含 Debug | `getDebugInfo`, `DebugLog`, `setDebugMode` |
| `export*Data` | 组合使用 | `exportOrderData`, `exportUserData` |

**特性**:
- ✅ 忽略大小写 (`Template*` 和 `template*` 效果相同)
- ✅ 支持通配符 `*` 匹配任意字符
- ✅ 精确到函数级别,不影响其他接口

#### 文件名匹配规则

| 模式 | 说明 | 匹配示例 |
|------|------|---------|
| `system*` | 匹配以 system 开头 | `systemApi.ts`, `systemTypes.ts` |
| `*Api.ts` | 匹配以 Api.ts 结尾 | `systemApi.ts`, `orderApi.ts` |
| `*Test*` | 匹配包含 Test | `orderTestApi.ts`, `TestTypes.ts` |
| `debug*.ts` | 组合使用 | `debugApi.ts`, `debugHelper.ts` |

#### 通配符规则

| 模式 | 说明 | 示例 |
|------|------|------|
| `**` | 匹配任意层级 | `/base/ad/**` 匹配 `/base/ad/pageAds`, `/base/ad/sub/getAd` |
| `*` | 匹配单层路径 | `/base/*/detail` 匹配 `/base/ad/detail`, `/base/order/detail` |
| 精确路径 | 完全匹配 | `/base/ad/pageAds` 只匹配该路径 |

#### 日志输出

```bash
📦 开始解析 OpenAPI 文档...
✅ 提取到 50 个类型定义
✅ 提取到 10 个模块
  📂 base/ad: 10 个接口
  ⏭️  忽略接口: templateAdd (匹配函数忽略规则)
  ⏭️  忽略接口: templateUpdate (匹配函数忽略规则)
  📊 business/base/ad: 过滤 2 个接口，保留 8 个
  ✍️  生成 API 文件: business/base/ad/adApi.ts (8 个函数, 文件不存在，创建新文件)
  📂 common/system: 10 个接口
  ⏭️  忽略文件: common/system/systemApi.ts (匹配文件忽略规则)
  ⏭️  忽略文件: common/system/systemTypes.ts (匹配文件忽略规则)
  📂 base/order: 12 个接口
  ✍️  生成 API 文件: business/base/order/orderApi.ts (12 个函数, 文件不存在，创建新文件)
```

### 3. Git/格式化忽略规则

#### 修改前
- 忽略整个目录: `src/api/generated`

#### 修改后
- 忽略所有生成文件: `**/*.generated.ts`

#### 涉及文件
- `.gitignore`: 忽略 Git 版本控制
- `.prettierignore`: 忽略代码格式化

### 4. 工作流程

```
OpenAPI文档
    ↓
解析接口和类型
    ↓
按模块生成代码
    ↓
    ├─ 原文件不存在 → 创建 xxxApi.ts
    ├─ 原文件存在且未修改(行数相同) → 跳过
    └─ 原文件存在且已修改(行数不同) → 创建 xxxApi.generated.ts
```

## 使用场景示例

### 场景 1: 首次生成

```bash
# 执行生成命令
POST http://localhost:5173/__openapi_generate

# 输出:
✍️  生成 API 文件: business/base/ad/adApi.ts (文件不存在，创建新文件)
✍️  生成类型文件: business/base/ad/adTypes.ts (文件不存在，创建新文件)
```

### 场景 2: 文件未修改

```bash
# 再次执行生成命令
POST http://localhost:5173/__openapi_generate

# 输出:
⏭️  跳过生成: business/base/ad/adApi.ts (文件内容相同，跳过生成)
⏭️  跳过生成: business/base/ad/adTypes.ts (文件内容相同，跳过生成)
```

### 场景 3: 文件已修改

```typescript
// 用户手动修改了 adApi.ts，增加了自定义逻辑
export const getAdDetail = (id: number): Result<AdVo> => {
  // 自定义的缓存逻辑
  const cached = getCache(`ad:${id}`)
  if (cached) return Promise.resolve({ code: 200, data: cached })

  return http.get<AdVo>(`/base/ad/getAd/${id}`)
}
```

```bash
# 再次执行生成命令
POST http://localhost:5173/__openapi_generate

# 输出:
✍️  生成 API 文件: business/base/ad/adApi.generated.ts (文件内容已变更，生成 .generated.ts)
```

此时你可以:
- 对比 `adApi.ts` 和 `adApi.generated.ts` 的差异
- 选择性地将新接口合并到 `adApi.ts`
- 保留你的自定义逻辑

### 场景 4: 使用文件级别忽略

```typescript
// vite/plugins/index.ts
createOpenApiPlugin({
  input: `http://127.0.0.1:${apiPort}/v3/api-docs/business`,
  output: 'src/api',
  ignore: {
    // 忽略所有以 system 开头的文件
    files: ['system*']
  }
})
```

```bash
# 执行生成命令
POST http://localhost:5173/__openapi_generate

# 输出:
📦 开始解析 OpenAPI 文档...
✅ 提取到 50 个类型定义
✅ 提取到 5 个模块
  📂 common/system: 10 个接口
  ⏭️  忽略文件: common/system/systemApi.ts (匹配文件忽略规则)
  ⏭️  忽略文件: common/system/systemTypes.ts (匹配文件忽略规则)
  📂 common/dict: 5 个接口
  ✍️  生成 API 文件: common/dict/dictApi.ts
  ✍️  生成类型文件: common/dict/dictTypes.ts
```

**说明**: `system*` 会匹配 `systemApi.ts` 和 `systemTypes.ts`，两个文件都不会生成

### 场景 5: 使用模块+文件组合忽略

```typescript
// vite/plugins/index.ts
createOpenApiPlugin({
  input: `http://127.0.0.1:${apiPort}/v3/api-docs/business`,
  output: 'src/api',
  ignore: {
    // 忽略整个 ad 模块
    modules: ['ad'],
    // 同时忽略其他模块中以 test 开头的文件
    files: ['test*'],
    // 忽略所有测试接口路径
    paths: ['/*/test/**']
  }
})
```

## 优势

### ✅ 精确判断
- 使用 MD5 哈希值比较内容
- 任何字符变化都能检测到
- 自动标准化换行符和空白

### ✅ 保持同步
- 生成 `.generated.ts` 参考文件
- 开发者可对比差异,选择性合并

### ✅ 防止覆盖
- 用户手动修改的代码不会被覆盖
- 开发者可以安全地在生成文件中添加自定义逻辑

### ✅ 灵活控制
- 通过 MD5 哈希精确判断内容变化
- 高效准确,性能优秀

### ✅ 清晰标识
- `.generated.ts` 后缀清晰标识自动生成文件
- Git 和格式化工具自动忽略

## 注意事项

### MD5 哈希比较机制

当前使用 **MD5 哈希值** 精确比较文件内容:

```typescript
// 计算文件的MD5哈希
function getFileHash(filePath: string): string {
  const content = readFileSync(filePath, 'utf-8')
  // 标准化:去除前后空白,统一换行符
  const normalized = content.trim().replace(/\r\n/g, '\n')
  return createHash('md5').update(normalized, 'utf-8').digest('hex')
}

const existingHash = getFileHash(normalPath)
const newHash = getContentHash(newContent)

if (existingHash === newHash) {
  // 内容完全相同 → 跳过生成
}
```

**检测范围:**
- ✅ 任何字符的增加/删除/修改
- ✅ 函数签名变化
- ✅ 类型字段变化
- ✅ 注释内容变化
- ✅ 代码逻辑变化

**忽略差异:**
- ✅ 换行符差异 (CRLF vs LF)
- ✅ 文件首尾空白行

**优势:**
- ✅ 精确判断,避免误判
- ✅ 性能优秀 (MD5计算速度快)
- ✅ 简单可靠

### 手动清理

`.generated.ts` 文件不会自动删除,需要手动管理:

```bash
# 合并完成后删除 .generated.ts
rm src/api/business/base/ad/adApi.generated.ts
```

## 技术实现

### 核心函数: `decideWriteStrategy`

```typescript
/**
 * 计算文件内容的MD5哈希值
 */
function getFileHash(filePath: string): string {
  if (!existsSync(filePath)) return ''
  const content = readFileSync(filePath, 'utf-8')
  // 标准化内容:去除前后空白,统一换行符
  const normalized = content.trim().replace(/\r\n/g, '\n')
  return createHash('md5').update(normalized, 'utf-8').digest('hex')
}

/**
 * 计算字符串内容的MD5哈希值
 */
function getContentHash(content: string): string {
  // 标准化内容:去除前后空白,统一换行符
  const normalized = content.trim().replace(/\r\n/g, '\n')
  return createHash('md5').update(normalized, 'utf-8').digest('hex')
}

/**
 * 决定文件写入策略
 */
function decideWriteStrategy(
  basePath: string,
  fileName: string,
  newContent: string
): { path: string; shouldWrite: boolean; reason: string } {
  const normalPath = `${basePath}/${fileName}`
  const generatedPath = `${basePath}/${fileName.replace(/\.(ts)$/, '.generated.$1')}`

  // 情况1: 原文件不存在
  if (!existsSync(normalPath)) {
    return { path: normalPath, shouldWrite: true, reason: '文件不存在，创建新文件' }
  }

  // 情况2: 原文件存在 → 比较MD5哈希
  const existingHash = getFileHash(normalPath)
  const newHash = getContentHash(newContent)

  if (existingHash === newHash) {
    return { path: generatedPath, shouldWrite: false, reason: '文件内容相同，跳过生成' }
  }

  return { path: generatedPath, shouldWrite: true, reason: '文件内容已变更，生成 .generated.ts' }
}
```

### 应用到生成流程

```typescript
// 生成类型文件
const typeStrategy = decideWriteStrategy(moduleDir, `${subModule}Types.ts`, typesContent)
if (typeStrategy.shouldWrite) {
  writeFile(typeStrategy.path, typesContent)
  console.log(`✍️  生成类型文件: ${typeStrategy.path} (${typeStrategy.reason})`)
} else {
  console.log(`⏭️  跳过生成: ${typeStrategy.reason}`)
}

// 生成 API 文件
const apiStrategy = decideWriteStrategy(moduleDir, `${subModule}Api.ts`, apiContent)
if (apiStrategy.shouldWrite) {
  writeFile(apiStrategy.path, apiContent)
  console.log(`✍️  生成 API 文件: ${apiStrategy.path} (${apiStrategy.reason})`)
} else {
  console.log(`⏭️  跳过生成: ${apiStrategy.reason}`)
}
```

## 后续优化建议

### 1. 三方合并工具集成

```typescript
// 生成后自动调用 diff 工具
if (typeStrategy.shouldWrite && typeStrategy.path.includes('.generated.')) {
  console.log(`💡 检测到修改，请使用以下命令对比差异:`)
  console.log(`   code --diff ${normalPath} ${typeStrategy.path}`)
}
```

### 2. 配置化行为

```typescript
interface OpenApiPluginOptions {
  // 现有配置...

  /** 生成策略: always-总是覆盖, smart-智能判断, backup-备份后覆盖 */
  generateStrategy?: 'always' | 'smart' | 'backup'

  /** 比较方式: md5-内容哈希(当前默认), sha256-更安全的哈希 */
  comparisonMethod?: 'md5' | 'sha256'
}
```

## 版本历史

### v2.5.0 (当前版本)
- ✅ **新增接口函数级别过滤** (`functions`)
  - 支持通配符匹配函数名 (`template*`, `*Test`, `debug*`)
  - 精确到接口级别,不影响同模块其他接口
  - 忽略大小写匹配
- ✅ 更精细的控制粒度: 模块 → 文件 → 接口

### v2.4.0
- ✅ **新增接口自动排序功能**
  - 按照标准 CRUD 顺序排序 (查询 → 增 → 改 → 删)
  - 7 个优先级: 分页查询 > 单个查询 > 新增 > 修改 > 删除 > 批量操作 > 其他
  - 统一代码风格,提升可读性
- ✅ 符合开发习惯,无需手动调整

### v2.3.0
- ✅ **新增文件级别忽略配置** (`files`)
  - 支持通配符匹配文件名 (`system*`, `*Test*`, `debug*.ts`)
  - 精确控制哪些文件生成,哪些不生成
  - 与模块/路径忽略配置配合使用
- ✅ 更灵活的忽略粒度控制

### v2.2.0
- ✅ **新增忽略配置功能**
  - 支持按模块名忽略 (`modules`)
  - 支持按路径忽略 (`paths`,支持通配符 `*` 和 `**`)
  - 支持自定义过滤函数 (`filter`)
- ✅ 灵活控制生成范围,避免生成不需要的模块

### v2.1.0
- ✅ **升级为 MD5 哈希比较** - 精确判断文件内容变化
- ✅ 自动标准化换行符和空白
- ✅ 性能优秀,判断准确

### v2.0.0
- ✅ 实现智能文件生成策略
- ✅ 通过行数判断文件是否修改(已弃用)
- ✅ 修改忽略规则为 `**/*.generated.ts`
- ✅ 优化日志输出,显示生成原因

### v1.0.0
- 基础 OpenAPI 代码生成功能
- 按目录控制覆盖行为
