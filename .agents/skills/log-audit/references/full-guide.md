
# 操作日志与审计追踪 指南

## 概述

本项目通过 `ruoyi-common-log` 模块提供**基于 AOP 的操作日志自动记录**。只需在 Controller 方法上加 `@Log` 注解，框架会自动切面拦截，异步发布事件，将日志写入 `sys_oper_log` 表。登录 / 登出由 `LoginLogPublisher` 发布事件，写入 `sys_logininfor`。

**核心模块**：`ruoyi-common/ruoyi-common-log`
- `Log.java` — `@Log` 注解
- `LogAspect.java` — AOP 环绕切面
- `OperLogEvent.java` / `LoginLogEvent.java` — Spring 事件
- `LoginLogPublisher.java` — 登录日志发布器

**前端菜单**：`系统监控 → 操作日志` / `登录日志`

---

## 核心注解：@Log

### 注解定义（`plus.ruoyi.common.log.annotation.Log`）

| 属性 | 类型 | 默认值 | 用途 |
|------|------|-------|------|
| `title` | String | `""` | 模块名称（必填），如"广告配置"、"用户管理" |
| `operType` | DictOperType | `OTHER` | 操作类型，对应字典 |
| `isSaveRequestData` | boolean | `true` | 是否保存请求参数 |
| `isSaveResponseData` | boolean | `true` | 是否保存响应结果 |
| `excludeParamNames` | String[] | `{}` | 排除的参数名（不写入日志） |

### DictOperType 操作类型

| 枚举 | 值 | 标签 | 典型场景 |
|------|----|----- |---------|
| `INSERT` | 1 | 新增 | POST /addXxx |
| `UPDATE` | 2 | 修改 | PUT /updateXxx |
| `DELETE` | 3 | 删除 | DELETE /deleteXxx |
| `GRANT` | 4 | 授权 | 角色授权、用户分配菜单 |
| `EXPORT` | 5 | 导出 | Excel 导出 |
| `IMPORT` | 6 | 导入 | Excel 导入 |
| `FORCE` | 7 | 强退 | 管理员强制用户下线 |
| `GENCODE` | 8 | 生成代码 | 代码生成器 |
| `CLEAN` | 9 | 清空数据 | 清空日志表等 |
| `OTHER` | 99 | 其他 | 调用 AI、发送短信等不属于上述的操作 |

---

## 标准用法

### 场景 1：CRUD 接口标准注解（必用）

```java
@RestController
@RequestMapping("/base/ad")
@RequiredArgsConstructor
public class AdController {

    private final IAdService adService;

    @Log(title = "广告配置", operType = DictOperType.INSERT)
    @PostMapping("/addAd")
    public R<Long> addAd(@Validated @RequestBody AdBo bo) {
        return R.ok(adService.addAd(bo));
    }

    @Log(title = "广告配置", operType = DictOperType.UPDATE)
    @PutMapping("/updateAd")
    public R<Void> updateAd(@Validated @RequestBody AdBo bo) {
        adService.updateAd(bo);
        return R.ok();
    }

    @Log(title = "广告配置", operType = DictOperType.DELETE)
    @DeleteMapping("/deleteAds/{ids}")
    public R<Void> deleteAds(@PathVariable Long[] ids) {
        adService.deleteAdsByIds(Arrays.asList(ids));
        return R.ok();
    }

    @Log(title = "广告配置", operType = DictOperType.EXPORT)
    @PostMapping("/exportAds")
    public void exportAds(AdBo bo, HttpServletResponse response) {
        // ...
    }

    @Log(title = "广告配置", operType = DictOperType.IMPORT)
    @PostMapping("/importAds")
    public R<Void> importAds(@RequestPart MultipartFile file) {
        // ...
    }
}
```

### 场景 2：排除敏感参数（密码、Token）

```java
// ❌ 错误：密码会明文写入 sys_oper_log.oper_param
@Log(title = "用户管理", operType = DictOperType.INSERT)
@PostMapping("/addUser")
public R<Void> addUser(@RequestBody UserBo bo) { ... }

// ✅ 正确：排除 password 字段
@Log(title = "用户管理", operType = DictOperType.INSERT,
     excludeParamNames = {"password", "confirmPassword"})
@PostMapping("/addUser")
public R<Void> addUser(@RequestBody UserBo bo) { ... }
```

### 场景 3：不保存响应（返回值过大）

```java
// 导出 Excel 返回流，不需要保存响应内容
@Log(title = "广告配置", operType = DictOperType.EXPORT, isSaveResponseData = false)
@PostMapping("/exportAds")
public void exportAds(...) { ... }

// 大数据量分页查询一般不加 @Log（查询操作不审计）
```

### 场景 4：非 CRUD 业务操作

```java
@Log(title = "AI对话", operType = DictOperType.OTHER)
@PostMapping("/chat")
public R<String> chat(@RequestBody ChatBo bo) { ... }

@Log(title = "在线用户", operType = DictOperType.FORCE)
@DeleteMapping("/kickOut/{tokenId}")
public R<Void> kickOut(@PathVariable String tokenId) { ... }
```

### 场景 5：参数级别的 @Log（少见）

```java
// 在参数上加 @Log，标记该参数为日志标题来源
public R<Void> refund(@Log(title = "订单退款") @RequestBody RefundBo bo) { ... }
```

---

## 登录日志

### 自动记录

登录 / 登出由 Sa-Token 登录流程触发，`LoginLogPublisher` 异步发布 `LoginLogEvent`，写入 `sys_logininfor` 表。开发者**不需要手动调用**。

### 记录字段

- 用户账号 / 租户编码
- 登录 IP / 地址
- 浏览器 / 操作系统
- 登录状态（成功 / 失败）
- 失败消息（如"密码错误"、"验证码失效"）
- 登录时间

### 手动发布登录日志（罕见）

```java
@Resource
private LoginLogPublisher loginLogPublisher;

// 自定义登录成功后调用
loginLogPublisher.publishLoginLog(
    username,
    true,               // 成功
    "登录成功",
    tenantId
);
```

---

## 日志数据表

### sys_oper_log（操作日志）

| 字段 | 说明 |
|------|------|
| `oper_id` | 日志ID（雪花） |
| `tenant_id` | 租户编码 |
| `title` | 模块标题（= @Log.title） |
| `business_type` | 业务类型（= @Log.operType.value） |
| `method` | 方法名称 |
| `request_method` | 请求方式（GET/POST/...） |
| `operator_type` | 操作类别（后台用户 / 手机端用户 / 其他） |
| `oper_name` | 操作人员 |
| `dept_name` | 部门名称 |
| `oper_url` | 请求 URL |
| `oper_ip` | 请求 IP |
| `oper_location` | 操作地点 |
| `oper_param` | 请求参数 JSON（已过滤 `excludeParamNames`） |
| `json_result` | 响应结果 JSON |
| `status` | 操作状态（0 正常 1 异常） |
| `error_msg` | 异常信息 |
| `oper_time` | 操作时间 |
| `cost_time` | 耗时（ms） |

### sys_logininfor（登录日志）

| 字段 | 说明 |
|------|------|
| `info_id` | 日志ID |
| `tenant_id` | 租户编码 |
| `user_name` | 用户账号 |
| `client_key` | 客户端标识 |
| `device_type` | 设备类型 |
| `ipaddr` | 登录 IP |
| `login_location` | 登录地点 |
| `browser` | 浏览器类型 |
| `os` | 操作系统 |
| `status` | 登录状态（0 成功 1 失败） |
| `msg` | 提示消息 |
| `login_time` | 登录时间 |

---

## ✅ 正确做法

| 场景 | 做法 |
|------|------|
| 写操作（POST/PUT/DELETE） | 必须加 `@Log` |
| 查询操作（GET 分页、列表、详情） | 不加 `@Log`（日志量大、价值低） |
| 含密码的接口 | 必用 `excludeParamNames = {"password"}` |
| 导出接口 | `operType = EXPORT`、`isSaveResponseData = false` |
| AI、第三方调用等非标操作 | `operType = OTHER` |
| `title` 命名 | 与菜单模块名一致（如"广告配置"、"用户管理"） |

## ❌ 常见错误

| 错误 | 原因 | 正确做法 |
|------|------|---------|
| 所有 GET 接口都加 `@Log` | 日志表膨胀 | 只审计"改数据"操作 |
| 密码接口未 `excludeParamNames` | 明文密码落库 | 排除敏感字段 |
| `title = "添加广告"`（动词+名词） | 冗余（operType 已表明动作） | `title = "广告配置"`（只写模块名） |
| `operType` 统一用 `OTHER` | 无法按类型统计 | 按 INSERT/UPDATE/DELETE 精确分类 |
| Service 层加 `@Log` | 切面只拦截 Controller 层 | 只在 Controller 加 |
| 大文件上传接口 `isSaveRequestData = true` | MultipartFile 无法序列化，切面已自动跳过 | 默认行为即可，无需特殊处理 |

---

## 查询与清理

### 通过前端菜单

- **系统监控 → 操作日志**：按模块、操作类型、时间范围筛选
- **系统监控 → 登录日志**：按用户、IP、状态筛选

### 清空日志（高危）

后端已提供清理接口：
```java
@Log(title = "操作日志", operType = DictOperType.CLEAN)
@DeleteMapping("/clean")
public R<Void> clean() { ... }
```

**生产环境建议**：通过 `@Scheduled` 或 SnailJob 定期归档超过 N 天的日志，而不是手动清空。

---

## 🔗 关联技能边界

| 场景 | 应使用技能 |
|------|-----------|
| 设计异常处理机制、错误码、try-catch | `error-handler` |
| 排查"为什么日志没记录"这类 Bug | `bug-detective` |
| 敏感字段脱敏（前端显示隐藏） | `security-guard` + `@Sensitive` |
| 数据权限过滤（查询时隔离） | `data-permission` |

**本技能专注**：「接口级别的操作审计」，不涉及业务日志（`log.info/warn/error`）的使用规范。
