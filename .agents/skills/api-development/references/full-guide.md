
# API 开发指南

## RESTful 设计规范

### URL 规范

```
# 格式
/{模块}/{资源}/{操作}

# 示例
/base/ad/pageAds      # 分页查询广告
/base/ad/getAd/{id}   # 获取单个广告
/base/ad/addAd        # 新增广告
/base/ad/updateAd     # 修改广告
/base/ad/deleteAds/{ids}  # 删除广告
```

### HTTP 方法

| 方法 | 用途 | 示例 |
|------|------|------|
| GET | 查询 | 分页列表、单个详情 |
| POST | 新增、复杂查询、导出 | 新增数据、导出Excel |
| PUT | 修改 | 修改数据 |
| DELETE | 删除 | 删除数据 |

### 接口路径唯一性设计 ⭐

**核心原则：接口路径必须包含实体名，确保全局唯一**

| 操作 | HTTP方法 | 路径格式 | 示例 |
|------|---------|---------|------|
| 分页查询 | GET | `/page{实体复数}` | `/pageAds` |
| 列表查询 | GET | `/list{实体复数}` | `/listAds` |
| 获取详情 | GET | `/get{实体}/{id}` | `/getAd/{id}` |
| 新增 | POST | `/add{实体}` | `/addAd` |
| 修改 | PUT | `/update{实体}` | `/updateAd` |
| 删除 | DELETE | `/delete{实体复数}/{ids}` | `/deleteAds/{ids}` |
| 导出 | POST | `/export{实体复数}` | `/exportAds` |
| 导入模板 | POST | `/template{实体复数}` | `/templateAds` |
| 导入 | POST | `/import{实体复数}` | `/importAds` |
| 选项列表 | GET | `/option{实体复数}` | `/optionPayments` |

```java
// ✅ 正确：路径包含实体名，全局唯一
@GetMapping("/pageAds")      // 分页查询广告
@GetMapping("/getAd/{id}")   // 获取广告详情
@PostMapping("/addAd")       // 新增广告
@PutMapping("/updateAd")     // 修改广告
@DeleteMapping("/deleteAds/{ids}")  // 删除广告

// ❌ 错误：通用路径，不唯一
@GetMapping("/page")         // 不知道查什么
@GetMapping("/list")         // 不知道查什么
@GetMapping("/{id}")         // 不知道获取什么
@PostMapping("/add")         // 不知道添加什么
```

---

## 后端 API 实现

### Controller 模板

```java
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/base/xxx")
public class XxxController {

    private final IXxxService xxxService;

    /**
     * 分页查询
     */
    @SaCheckPermission("base:xxx:query")
    @GetMapping("/pageXxxs")
    public R<PageResult<XxxVo>> pageXxxs(XxxBo bo, PageQuery pageQuery) {
        return R.ok(xxxService.page(bo, pageQuery));
    }

    /**
     * 获取详情
     */
    @SaCheckPermission("base:xxx:query")
    @GetMapping("/getXxx/{id}")
    public R<XxxVo> getXxx(@NotNull(message = "ID不能为空") @PathVariable Long id) {
        return R.ok(xxxService.get(id));
    }

    /**
     * 新增
     */
    @SaCheckPermission("base:xxx:add")
    @Log(title = "XXX", operType = DictOperType.INSERT)
    @RepeatSubmit()
    @PostMapping("/addXxx")
    public R<Long> addXxx(@Validated(AddGroup.class) @RequestBody XxxBo bo) {
        return R.ok(xxxService.add(bo));
    }

    /**
     * 修改
     */
    @SaCheckPermission("base:xxx:update")
    @Log(title = "XXX", operType = DictOperType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/updateXxx")
    public R<Void> updateXxx(@Validated(EditGroup.class) @RequestBody XxxBo bo) {
        return R.status(xxxService.update(bo));
    }

    /**
     * 删除
     */
    @SaCheckPermission("base:xxx:delete")
    @Log(title = "XXX", operType = DictOperType.DELETE)
    @DeleteMapping("/deleteXxxs/{ids}")
    public R<Void> deleteXxxs(@NotEmpty(message = "ID不能为空") @PathVariable Long[] ids) {
        return R.status(xxxService.batchDelete(List.of(ids)));
    }
}
```

### 移动端 API (AppController)

移动端 API 同样遵循唯一性原则，路径包含实体名。

```java
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/app/xxx")
public class XxxAppController {

    private final IXxxService xxxService;

    /**
     * 查询列表（移动端通常不需要权限校验，但需要登录）
     */
    @SaCheckLogin
    @GetMapping("/listXxxs")
    public R<List<XxxVo>> listXxxs(XxxBo bo) {
        return R.ok(xxxService.list(bo));
    }

    /**
     * 获取详情
     */
    @SaCheckLogin
    @GetMapping("/getXxx/{id}")
    public R<XxxVo> getXxx(@PathVariable Long id) {
        return R.ok(xxxService.get(id));
    }

    /**
     * 新增
     */
    @SaCheckLogin
    @PostMapping("/addXxx")
    public R<Long> addXxx(@Validated @RequestBody XxxBo bo) {
        return R.ok(xxxService.add(bo));
    }
}
```

**移动端路径示例**（参考实际项目）：

```java
// 手机号相关 - /app/phone
@PostMapping("/bindPhone")     // 绑定手机号
@GetMapping("/getPhone")       // 获取手机号
@DeleteMapping("/unbindPhone") // 解绑手机号

// 微信分享 - /app/wxShare
@GetMapping("/getJsApiSignature")  // 获取JS API签名

// 消息订阅 - /app/subscribe
@GetMapping("/getTemplateConfigs") // 获取模板配置
```

---

## 前端 API 对接

### API 定义 (PC 端)

```typescript
// xxxApi.ts
// ✅ http、Result、PageResult、PageQuery 已自动导入，无需手动 import
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

### API 定义 (移动端)

```typescript
// xxxApi.ts (plus-uniapp)
// ✅ http、Result、PageResult 已自动导入，无需手动 import
import type { XxxVo, XxxBo, XxxQuery } from './xxxTypes'

// ✅ 路径包含实体名，确保唯一性
export const listXxxs = (query?: XxxQuery): Result<XxxVo[]> => {
  return http.get<XxxVo[]>('/app/xxx/listXxxs', query)
}

export const getXxx = (id: string | number): Result<XxxVo> => {
  return http.get<XxxVo>(`/app/xxx/getXxx/${id}`)
}

export const addXxx = (data: XxxBo): Result<number> => {
  return http.post<number>('/app/xxx/addXxx', data)
}

// 分页查询示例
export const pageXxxs = (query?: XxxQuery): Result<PageResult<XxxVo>> => {
  return http.get<PageResult<XxxVo>>('/app/xxx/pageXxxs', query)
}
```

### 类型定义

```typescript
// xxxTypes.ts

/** 查询参数 */
export interface XxxQuery extends PageQuery {
  name?: string
  status?: string
  beginCreateTime?: string
  endCreateTime?: string
}

/** 表单数据 */
export interface XxxBo {
  id?: string | number
  name?: string
  status?: string
  remark?: string
}

/** 视图数据 */
export interface XxxVo {
  id: string | number
  name: string
  status: string
  createTime: string
  remark: string
}
```

---

## 响应格式

### 统一响应结构

```typescript
interface R<T> {
  code: number      // 状态码，200 成功
  msg: string       // 提示信息
  data: T           // 数据
}

interface PageResult<T> {
  records: T[]      // 数据列表
  total: number     // 总数
  pageNum: number   // 当前页
  pageSize: number  // 每页大小
}
```

### 响应示例

```json
// 成功
{
  "code": 200,
  "msg": "操作成功",
  "data": { ... }
}

// 失败
{
  "code": 500,
  "msg": "操作失败：用户不存在"
}

// 分页
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "records": [...],
    "total": 100,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

---

## 参数校验

### 后端校验

```java
public class XxxBo {
    @NotNull(message = "ID不能为空", groups = { EditGroup.class })
    private Long id;

    @NotBlank(message = "名称不能为空", groups = { AddGroup.class, EditGroup.class })
    @Size(max = 100, message = "名称长度不能超过100")
    private String name;
}

// Controller 使用分组校验
@PostMapping("/addXxx")
public R<Long> addXxx(@Validated(AddGroup.class) @RequestBody XxxBo bo) {
    // AddGroup 校验 name
}

@PutMapping("/updateXxx")
public R<Void> updateXxx(@Validated(EditGroup.class) @RequestBody XxxBo bo) {
    // EditGroup 校验 id 和 name
}
```

### 前端校验

```typescript
const rules = {
  name: [
    { required: true, message: '请输入名称', trigger: 'blur' },
    { max: 100, message: '长度不能超过100', trigger: 'blur' }
  ]
}
```

---

## 错误处理

### 后端抛出异常

```java
// 业务异常
throw ServiceException.of("用户不存在");
throw ServiceException.of("用户 {} 不存在", userId);

// 前端收到
{
  "code": 500,
  "msg": "用户不存在"
}
```

### 前端处理错误

```typescript
try {
  await addXxx(data)
  ElMessage.success('添加成功')
} catch (error) {
  // http 封装已处理错误提示
  // 如需特殊处理可在这里
}
```
