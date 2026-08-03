---
name: error-handler
description: |
  设计异常处理机制、错误码、日志规范。（与 bug-detective 区别：本 Skill 用于"设计处理机制"，bug-detective 用于"排查已发生的问题"）

  触发场景：
  - 设计 try-catch 异常处理
  - 定义错误码体系
  - 配置日志记录规范
  - 设计全局异常处理器
  - ServiceException 使用方法
  - 错误提示文案优化

  触发词：异常处理、ServiceException、try-catch、全局异常、错误码、日志规范、log.info、@Slf4j、错误提示设计

  注意：如果是排查已发生的 Bug（代码报错、功能异常），请使用 bug-detective。
---

# 错误处理指南

> ⚠️ **本项目规范**：本文档中的示例遵循本项目特定规范，部分与通用框架写法不同。标记 `🔴 本项目规范` 的部分必须严格遵守。

---

## 后端异常处理

### 业务异常（ServiceException）

```java
// ✅ 推荐：使用 ServiceException.of() 静态方法
throw ServiceException.of("用户不存在");
throw ServiceException.of("用户 {} 不存在", userId);  // 支持占位符

// ✅ 带错误码
throw ServiceException.of("用户不存在", 200101);  // businessCode

// ✅ 条件抛出（condition 为 true 时抛出）
ServiceException.throwIf(user == null, "用户不存在");
ServiceException.throwIf(user == null, "用户 {} 不存在", userId);

// ✅ 非空检查
ServiceException.notNull(user, "用户不存在");
ServiceException.notNull(user, "用户 {} 不存在", userId);

// ❌ 错误：throwIfNot 方法不存在！
// ServiceException.throwIfNot(user != null, "用户不存在");  // 不存在这个方法！
```

### ServiceException 完整 API

| 方法 | 说明 | 示例 |
|------|------|------|
| `of(message)` | 快速创建异常 | `ServiceException.of("操作失败")` |
| `of(message, args...)` | 带占位符 | `ServiceException.of("用户{}不存在", userId)` |
| `of(message, businessCode)` | 带错误码 | `ServiceException.of("用户不存在", 200101)` |
| `throwIf(condition, message)` | 条件抛出 | `ServiceException.throwIf(user == null, "用户不存在")` |
| `notNull(object, message)` | 非空检查 | `ServiceException.notNull(user, "用户不存在")` |

### 参数校验异常

```java
// 使用 @Validated 自动校验
@PostMapping("/addUser")
public R<Long> addUser(@Validated(AddGroup.class) @RequestBody UserBo bo) {
    // 参数校验失败会自动抛出 ConstraintViolationException
}

// 手动校验
if (StringUtils.isBlank(bo.getName())) {
    throw ServiceException.of("名称不能为空");
}
```

### 全局异常处理

```java
// 框架已提供全局异常处理器
// 位置: ruoyi-common-core/.../handler/GlobalExceptionHandler.java

// 常见异常处理
// - ServiceException -> 返回业务错误信息
// - ConstraintViolationException -> 返回参数校验错误
// - AccessDeniedException -> 返回权限不足
// - Exception -> 返回系统错误
```

### 异常处理最佳实践

```java
// ✅ 好的：具体异常优先
@Override
@Transactional(rollbackFor = Exception.class)
public Long add(OrderBo bo) {
    // 1. 参数校验
    if (bo.getAmount() <= 0) {
        throw ServiceException.of("订单金额必须大于0");
    }

    // 2. 业务校验（使用 notNull 更简洁）
    User user = userDao.getById(bo.getUserId());
    ServiceException.notNull(user, "用户不存在");

    // 3. 正常逻辑
    Order order = MapstructUtils.convert(bo, Order.class);
    orderDao.insert(order);
    return order.getId();
}

// ❌ 不好：捕获所有异常
public Long add(OrderBo bo) {
    try {
        // ...
    } catch (Exception e) {
        // 吃掉所有异常，难以定位问题
        return null;
    }
}
```

---

## 🔴 前端错误处理（本项目规范）

> **重要**：本项目前端禁止使用 `try-catch`，必须使用 `[err, data]` 格式处理 API 调用。
> **重要**：禁止直接使用 `ElMessage`，必须使用封装的 `showMsgXxx` 工具函数。

### API 错误处理

```typescript
// 🔴 本项目规范：使用 [err, data] 格式
import { showMsgSuccess, showMsgError, showConfirm } from '@/utils/modal'

// ✅ 正确：使用 [err, data] 格式
const handleSubmit = async () => {
  loading.value = true
  const [err, data] = await addUser(form.value)
  loading.value = false

  if (!err) {
    showMsgSuccess('添加成功')
    emit('success')
  }
  // 错误已由全局拦截器处理，无需手动处理
}

// ✅ 正确：删除操作（带确认）
const handleDelete = async (row?: UserVo) => {
  const idsToDelete = row ? [row.id] : selectionItems.value.map(item => item.id)
  if (idsToDelete.length === 0) return

  const [confirmErr] = await showConfirm('确定要删除吗？')
  if (confirmErr) return  // 用户取消

  const [deleteErr] = await deleteUsers(idsToDelete)
  if (!deleteErr) {
    showMsgSuccess('删除成功')
    await getList()
  }
}

// ❌ 禁止：使用 try-catch
const handleSubmit = async () => {
  try {
    await addUser(form.value)  // ❌ 禁止！
  } catch (error) {
    // ...
  }
}

// ❌ 禁止：直接使用 ElMessage
import { ElMessage } from 'element-plus'  // ❌ 禁止！
ElMessage.success('操作成功')  // ❌ 禁止！
```

### 前端消息工具函数（modal.ts）

| 函数 | 用途 | 示例 |
|------|------|------|
| `showMsgSuccess(msg)` | 成功消息 | `showMsgSuccess('保存成功')` |
| `showMsgError(msg)` | 错误消息 | `showMsgError('操作失败')` |
| `showMsgWarning(msg)` | 警告消息 | `showMsgWarning('请检查表单')` |
| `showConfirm(msg)` | 确认弹窗 | `const [err] = await showConfirm('确定删除？')` |
| `showAlert(msg)` | 信息弹窗 | `const [err] = await showAlert('操作说明')` |
| `showPrompt(msg)` | 输入弹窗 | `const [err, result] = await showPrompt('请输入')` |
| `showLoading(msg)` | 加载提示 | `showLoading('加载中...')` |
| `hideLoading()` | 隐藏加载 | `hideLoading()` |

### 全局错误边界

```typescript
// main.ts - 框架已配置
app.config.errorHandler = (err, instance, info) => {
  console.error('全局错误:', err)
  console.error('错误信息:', info)
  // 上报错误监控
}
```

---

## 🔴 移动端错误处理（本项目规范）

> **重要**：禁止使用 `uni.showToast()`，必须使用 `useToast()` from `@/wd`。
> **重要**：禁止直接从 `'wot-design-uni'` 导入，必须从 `@/wd` 导入。

### 请求错误处理

```typescript
// 🔴 本项目规范：从 @/wd 导入
import { useToast, useMessage } from '@/wd'  // ✅ 正确

const toast = useToast()
const message = useMessage()

// ✅ 正确：使用 [err, data] 格式 + useToast
const loadData = async () => {
  loading.value = true
  const [err, data] = await getDetail(id)
  loading.value = false

  if (!err) {
    detail.value = data
  } else {
    toast.error('加载失败，请重试')
  }
}

// ✅ 正确：支付场景
const handlePay = async () => {
  const [payErr, payResult] = await createOrderAndPay(orderData)

  if (!payErr) {
    toast.success('支付成功')
  } else {
    toast.error('支付失败')
  }
}

// ✅ 正确：确认操作（使用 WD UI）
const handleDelete = async () => {
  message.confirm({
    title: '确认删除',
    msg: '确定要删除吗？',
  }).then(async () => {
    const [err] = await deleteItem(id)
    if (!err) {
      toast.success('删除成功')
    }
  })
}

// ❌ 禁止：使用 uni API
uni.showToast({ title: '成功', icon: 'success' })  // ❌ 禁止！
uni.showModal({ title: '提示', content: '确定？' })  // ❌ 禁止！
uni.showLoading({ title: '加载中' })  // ❌ 禁止！

// ❌ 禁止：从 wot-design-uni 直接导入
import { useToast } from 'wot-design-uni'  // ❌ 禁止！
```

### 移动端消息工具（@/wd）

| 函数 | 用途 | 示例 |
|------|------|------|
| `useToast().success(msg)` | 成功提示 | `toast.success('保存成功')` |
| `useToast().error(msg)` | 错误提示 | `toast.error('操作失败')` |
| `useToast().warning(msg)` | 警告提示 | `toast.warning('请注意')` |
| `useToast().info(msg)` | 信息提示 | `toast.info('提示信息')` |
| `useToast().loading(msg)` | 加载提示 | `toast.loading('加载中')` |
| `useMessage().confirm({})` | 确认弹窗 | `message.confirm({ title, msg })` |
| `useMessage().alert({})` | 信息弹窗 | `message.alert({ title, msg })` |

### 页面级错误状态

```vue
<!-- 页面级错误状态处理示例 -->
<script setup lang="ts">
import { useToast } from '@/wd'

const toast = useToast()
const loading = ref(false)
const errorMsg = ref('')
const detail = ref<DetailVo>()

const loadData = async () => {
  loading.value = true
  errorMsg.value = ''

  const [err, data] = await getDetail(id)

  if (!err) {
    detail.value = data
  } else {
    errorMsg.value = '加载失败，请重试'
  }

  loading.value = false
}
</script>

<template>
  <view v-if="loading">
    <wd-loading />
  </view>
  <view v-else-if="errorMsg">
    <wd-status-tip type="error" :tip="errorMsg">
      <wd-button @click="loadData">重试</wd-button>
    </wd-status-tip>
  </view>
  <view v-else>
    <!-- 正常内容 -->
  </view>
</template>
```

---

## 日志规范

### 日志级别

| 级别 | 使用场景 |
|------|----------|
| ERROR | 系统错误、业务异常 |
| WARN | 警告信息、潜在问题 |
| INFO | 重要业务流程、操作记录 |
| DEBUG | 开发调试信息 |
| TRACE | 详细追踪信息 |

### 后端日志

```java
@Slf4j
@Service
public class OrderServiceImpl implements IOrderService {

    @Override
    public Long add(OrderBo bo) {
        log.info("创建订单开始，用户ID: {}", bo.getUserId());

        try {
            // 业务逻辑
            Order order = MapstructUtils.convert(bo, Order.class);
            orderDao.insert(order);

            log.info("创建订单成功，订单ID: {}", order.getId());
            return order.getId();

        } catch (Exception e) {
            log.error("创建订单失败，用户ID: {}，错误: {}", bo.getUserId(), e.getMessage(), e);
            throw e;
        }
    }
}
```

### 日志最佳实践

```java
// ✅ 好的：使用占位符
log.info("处理订单: {}, 状态: {}", orderId, status);

// ❌ 不好：字符串拼接
log.info("处理订单: " + orderId + ", 状态: " + status);

// ✅ 好的：判断日志级别
if (log.isDebugEnabled()) {
    log.debug("详细数据: {}", JSON.toJSONString(data));
}

// ✅ 好的：异常日志带堆栈
log.error("处理失败: {}", e.getMessage(), e);

// ❌ 不好：只记录消息
log.error("处理失败: {}", e.getMessage());

// ✅ 好的：敏感信息脱敏
log.info("用户登录，手机: {}", DesensitizedUtil.mobilePhone(phone));

// ❌ 不好：记录敏感信息
log.info("用户登录，手机: {}", phone);
```

### 前端日志

```typescript
// 开发环境日志
if (import.meta.env.DEV) {
  console.log('调试信息:', data)
}

// 生产环境错误上报
const reportError = (error: Error, context?: object) => {
  // 上报到监控平台
  console.error('错误上报:', error, context)
}
```

---

## 错误码设计

### 错误码规范

```java
// 格式: 模块(2位) + 类型(2位) + 序号(2位)
// 模块: 10-系统, 20-用户, 30-订单, 40-商品
// 类型: 01-参数错误, 02-业务错误, 03-权限错误, 04-系统错误

// 系统错误码
public class ErrorCode {
    // 通用错误
    public static final int SUCCESS = 200;
    public static final int ERROR = 500;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;

    // 用户模块 20xxxx
    public static final int USER_NOT_FOUND = 200101;     // 用户不存在
    public static final int USER_PASSWORD_ERROR = 200102; // 密码错误
    public static final int USER_DISABLED = 200103;       // 用户已禁用

    // 订单模块 30xxxx
    public static final int ORDER_NOT_FOUND = 300101;     // 订单不存在
    public static final int ORDER_STATUS_ERROR = 300201;  // 订单状态错误
}
```

### 错误消息国际化

```java
// 使用 I18nKeys 常量
throw ServiceException.of(I18nKeys.Common.ID_REQUIRED);

// 位置: ruoyi-common-core/.../constant/I18nKeys.java
public interface I18nKeys {
    interface Common {
        String ID_REQUIRED = "common.id.required";      // ID不能为空
        String NOT_FOUND = "common.not.found";          // 数据不存在
        String OPERATION_FAILED = "common.operation.failed";
    }
}
```

---

## 用户友好提示

### 错误提示规范

```java
// ✅ 好的：用户友好提示
throw ServiceException.of("订单已发货，无法取消");
throw ServiceException.of("库存不足，请减少购买数量");
throw ServiceException.of("验证码已过期，请重新获取");

// ❌ 不好：技术术语
throw ServiceException.of("order.status.invalid");
throw ServiceException.of("NullPointerException at line 123");
throw ServiceException.of("数据库连接失败");
```

### 🔴 前端提示（本项目规范）

```typescript
import { showMsgSuccess, showMsgError, showMsgWarning, showConfirm } from '@/utils/modal'

// 操作成功
showMsgSuccess('保存成功')

// 操作失败
showMsgError('保存失败，请重试')

// 确认操作
const [err] = await showConfirm('确定删除这条数据吗？')
if (!err) {
  // 执行删除
}

// 表单校验失败
showMsgWarning('请检查表单填写是否完整')
```

### 🔴 移动端提示（本项目规范）

```typescript
import { useToast, useMessage } from '@/wd'

const toast = useToast()
const message = useMessage()

// 成功提示
toast.success('保存成功')

// 错误提示
toast.error('操作失败')

// 确认对话框
message.confirm({
  title: '提示',
  msg: '确定删除吗？',
}).then(() => {
  // 执行删除
})

// 加载提示
toast.loading('加载中...')
// 操作完成后
toast.close()
```

---

## 错误处理检查清单

### 后端

- [ ] 业务异常使用 `ServiceException.of()`
- [ ] 条件检查使用 `ServiceException.throwIf()` 或 `ServiceException.notNull()`
- [ ] 参数校验使用 `@Validated`
- [ ] 事务方法添加 `rollbackFor = Exception.class`
- [ ] 日志记录异常堆栈
- [ ] 敏感信息脱敏

### 🔴 前端（本项目规范）

- [ ] API 调用使用 `[err, data]` 格式
- [ ] 消息提示使用 `showMsgXxx()` 封装函数
- [ ] 确认弹窗使用 `showConfirm()`
- [ ] ❌ 不使用 `try-catch`
- [ ] ❌ 不直接使用 `ElMessage`
- [ ] 加载状态处理
- [ ] 空数据状态处理

### 🔴 移动端（本项目规范）

- [ ] 从 `@/wd` 导入 `useToast`/`useMessage`
- [ ] API 调用使用 `[err, data]` 格式
- [ ] 消息提示使用 `toast.success()`/`toast.error()`
- [ ] ❌ 不使用 `uni.showToast()`/`uni.showModal()`
- [ ] ❌ 不从 `'wot-design-uni'` 直接导入
- [ ] 加载状态处理
- [ ] 错误状态页面（使用 `wd-status-tip`）
- [ ] 网络错误重试机制

---

## 快速对照表

### 前端（plus-ui）

| ❌ 禁止 | ✅ 正确 |
|--------|--------|
| `try { await api() } catch` | `const [err, data] = await api()` |
| `ElMessage.success('msg')` | `showMsgSuccess('msg')` |
| `ElMessage.error('msg')` | `showMsgError('msg')` |
| `ElMessageBox.confirm()` | `showConfirm('msg')` |

### 移动端（plus-uniapp）

| ❌ 禁止 | ✅ 正确 |
|--------|--------|
| `try { await api() } catch` | `const [err, data] = await api()` |
| `uni.showToast({ title })` | `toast.success('msg')` |
| `uni.showModal({})` | `message.confirm({})` |
| `uni.showLoading({})` | `toast.loading('msg')` |
| `from 'wot-design-uni'` | `from '@/wd'` |

---

## 🔗 关联技能边界

本技能专注于**设计异常处理机制**。遇到以下场景请改用其他技能：

| 场景 | 应使用技能 | 判断关键词 |
|------|-----------|-----------|
| 代码报错了、要定位 Bug | `bug-detective` | "为什么报错"、"怎么不生效"、"找不到原因" |
| 行级数据权限、部门隔离 | `data-permission` | "@DataPermission"、"数据权限不生效" |
| 接口限流、防重复提交、脱敏 | `security-guard` / `backend-annotations` | "@RateLimiter"、"@RepeatSubmit"、"@Sensitive" |

**一句话辨别**：
- "**怎么设计 X 的错误处理**" → error-handler（设计机制）
- "**X 现在报错了怎么办**" → bug-detective（排查问题）
