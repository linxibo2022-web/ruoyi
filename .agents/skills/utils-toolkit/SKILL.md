---
name: utils-toolkit
description: |
  工具类智能匹配助手 - 根据上下文自动提供后端/前端/移动端对应的工具类和最佳实践。

  触发场景（智能识别端）：
  - 日期时间处理 → 后端 DateUtils / 前端 dayjs / 移动端 dayjs
  - 字符串操作 → 后端 StringUtils / 前端 原生方法
  - 集合操作 → 后端 CollUtil / 前端 Array 方法
  - 对象转换 → 后端 MapstructUtils（必须使用！）
  - 数据校验 → 后端 @Validated / 前端 Form 校验
  - 加密解密 → 后端 SecureUtil / 前端 crypto-js
  - 树结构处理 → 后端 TreeBuildUtils / 前端 @/utils/tree
  - 消息提示 → 后端 ServiceException / 前端 modal.ts / 移动端 useToast
  - 数据格式化 → 后端 自定义 / 前端 @/utils/format
  - HTTP 请求 → 后端 RestTemplate / 前端 http 封装

  触发词：工具类、日期、时间、字符串、集合、数组、转换、校验、加密、格式化、处理、工具、utils、Hutool、dayjs、lodash、树结构、tree、权限、下载、打印、弹窗、消息、toast、modal、websocket、sse、composable、hook

  智能规则：
  - 写 Java 代码 → 提供后端工具类
  - 写 Vue/TypeScript 代码 → 提供前端工具和 Composables
  - 写 UniApp 代码 → 提供移动端工具和已有 Composables
---

# 工具类大全 (跨端智能匹配)

## 使用规则

| 代码类型 | 使用工具 |
|---------|---------|
| Java 后端 | Hutool、项目自定义工具类 |
| Vue/TypeScript 前端 | @/utils/*、@/composables/*、dayjs |
| UniApp 移动端 | @/composables/*、@/wd（useToast/useMessage）、dayjs |

---

## 快速索引

### 后端工具类索引

| 功能 | 工具类 | 常用方法 |
|------|--------|---------|
| 字符串 | `StringUtils` | `isBlank()`, `format()` |
| 集合 | `CollUtil` | `isEmpty()`, `newArrayList()` |
| 日期 | `DateUtils` / `DateUtil` | `formatDate()`, `getNowDate()` |
| 对象转换 | `MapstructUtils` | `convert()` |
| 树结构 | `TreeBuildUtils` | `build()` |
| JSON | `JSONUtil` / `JSON` | `toJsonStr()`, `toBean()` |
| 加密 | `SecureUtil` | `md5()`, `sha256()` |
| 对象判断 | `ObjectUtils` | `isNull()`, `isEmpty()` |
| ID生成 | `IdUtil` | `getSnowflakeNextId()` |
| 业务异常 | `ServiceException` | `ServiceException.of()` |

### 前端工具索引

| 功能 | 路径 | 常用函数 |
|------|------|---------|
| 树结构 | `@/utils/tree` | `buildTree()`, `findTreeNode()`, `flattenTree()` |
| 格式化 | `@/utils/format` | `formatAmount()`, `formatPhone()`, `formatIDCard()` |
| 消息弹窗 | `@/utils/modal` | `showMsg()`, `showConfirm()`, `showLoading()` |
| 加密 | `@/utils/crypto` | 加解密工具 |
| 缓存 | `@/utils/cache` | 本地缓存操作 |
| 校验 | `@/utils/validators` | 表单验证器 |
| 日期 | `dayjs` | 已全局导入 |

### 前端 Composables 索引

| Composable | 用途 |
|------------|------|
| `useTableHeight` | 表格高度自适应 |
| `useDict` | 字典数据获取 |
| `useDialog` | 弹窗控制 |
| `useSelection` | 表格选择 |
| `useDownload` | 文件下载 |
| `usePrint` | 打印功能 |
| `useAuth` | 权限判断 |
| `useWS` / `useSSE` | WebSocket / SSE |
| `useAiChat` | AI 聊天 |
| `useI18n` | 国际化 |
| `useTheme` | 主题切换 |

### 移动端 Composables 索引

| Composable | 用途 |
|------------|------|
| `useAuth` | 认证逻辑 |
| `useToken` | Token 管理 |
| `useDict` | 字典数据 |
| `usePayment` | 支付功能 |
| `useWxShare` | 微信分享 |
| `useSubscribe` | 订阅消息 |
| `useWebSocket` | WebSocket |
| `useScroll` | 滚动处理 |
| `useI18n` | 国际化 |
| `useTheme` | 主题切换 |
| `useEventBus` | 事件总线 |
| `useAppInit` | 应用初始化 |

---

## 1. 日期时间处理

### 后端 (Java)

> **⚠️ 重要**：本框架 Entity 基类（BaseEntity/TenantEntity）的时间字段统一使用 `java.util.Date`。
> 禁止使用 `LocalDateTime.now()`，获取当前时间必须用 `DateUtils.getNowDate()` 或 `new Date()`。

```java
import plus.ruoyi.common.core.utils.DateUtils;
import plus.ruoyi.common.core.enums.DateTimeFormat;
import cn.hutool.core.date.DateUtil;

// ✅ 获取当前日期（推荐）
Date now = DateUtils.getNowDate();
// ✅ 也可以
Date now2 = new Date();
// ❌ 禁止：框架统一用 Date，不用 LocalDateTime
// LocalDateTime ldt = LocalDateTime.now();

// 获取当前日期字符串
Date now = DateUtils.getNowDate();
String date = DateUtils.getDate();  // yyyy-MM-dd 格式

// 格式化日期
String dateStr = DateUtils.formatDate(new Date());      // yyyy-MM-dd
String timeStr = DateUtils.formatDateTime(new Date());  // yyyy-MM-dd HH:mm:ss
String custom = DateUtils.parseDateToStr(DateTimeFormat.DATE, date);

// Hutool 日期工具（更多功能）
DateUtil.format(date, "yyyy-MM-dd");
DateUtil.parse("2025-01-01");
DateUtil.beginOfDay(date);    // 当天开始
DateUtil.endOfDay(date);      // 当天结束
DateUtil.offsetDay(date, 7);  // 7天后
```

### 前端 (TypeScript)

> **注意**: `dayjs` 已全局自动导入，无需手动 import！

```typescript
// 格式化（dayjs 已自动导入）
dayjs().format('YYYY-MM-DD')
dayjs().format('YYYY-MM-DD HH:mm:ss')

// 解析
dayjs('2025-01-01')

// 计算
dayjs().add(7, 'day')
dayjs().subtract(1, 'month')
dayjs().startOf('day')
dayjs().endOf('day')

// 比较
dayjs('2025-01-01').isBefore('2025-01-02')
dayjs('2025-01-01').isAfter('2024-12-31')
```

### 移动端 (UniApp)

```typescript
// 同前端，使用 dayjs（已自动导入）
dayjs().format('YYYY-MM-DD')
```

---

## 2. 字符串操作

### 后端 (Java)

```java
import plus.ruoyi.common.core.utils.StringUtils;
import cn.hutool.core.util.StrUtil;

// 判空（推荐使用项目自带的 StringUtils）
StringUtils.isBlank(str);      // null/""/空白 都返回 true
StringUtils.isNotBlank(str);
StringUtils.isEmpty(str);      // null/"" 返回 true
StringUtils.isNotEmpty(str);

// 格式化
StringUtils.format("Hello {}", "World");  // Hello World

// Hutool 字符串工具
StrUtil.isBlank(str);
StrUtil.format("Hello {}", "World");
StrUtil.sub(str, 0, 10);   // 截取
StrUtil.split(str, ",");   // 分割
StrUtil.join(",", list);   // 拼接
```

### 前端 (TypeScript)

```typescript
// 判空
const isEmpty = (str: string | null | undefined) => !str || str.trim() === ''
const isNotEmpty = (str: string | null | undefined) => !!str && str.trim() !== ''

// 原生方法
str.trim()
str.split(',')
arr.join(',')
str.substring(0, 10)
str.replace('old', 'new')
str.includes('keyword')
```

---

## 3. 集合操作

### 后端 (Java)

```java
import cn.hutool.core.collection.CollUtil;
import java.util.stream.Collectors;

// 判空
CollUtil.isEmpty(list);
CollUtil.isNotEmpty(list);

// 创建集合
CollUtil.newArrayList(1, 2, 3);
CollUtil.newHashSet(1, 2, 3);

// Stream 操作
list.stream()
    .filter(item -> item.getStatus().equals("1"))
    .map(Item::getName)
    .collect(Collectors.toList());

// 提取 ID 列表
List<Long> ids = list.stream()
    .map(Item::getId)
    .collect(Collectors.toList());
```

### 前端 (TypeScript)

```typescript
// 判空
Array.isArray(arr) && arr.length > 0

// 常用方法
arr.filter(item => item.status === '1')
arr.map(item => item.name)
arr.find(item => item.id === id)
arr.some(item => item.status === '1')
arr.every(item => item.status === '1')
arr.reduce((sum, item) => sum + item.amount, 0)

// 去重
[...new Set(arr)]

// 提取 ID
arr.map(item => item.id)
```

---

## 4. 对象转换 (重要！)

### 后端 (Java) - 必须使用 MapstructUtils！

```java
import plus.ruoyi.common.core.utils.MapstructUtils;

// ✅ 正确：使用 MapstructUtils
XxxVo vo = MapstructUtils.convert(entity, XxxVo.class);
List<XxxVo> voList = MapstructUtils.convert(entityList, XxxVo.class);
Xxx entity = MapstructUtils.convert(bo, Xxx.class);

// ❌ 错误：不要使用 BeanUtils
// BeanUtils.copyProperties(source, target);  // 禁止！
```

### 前端 (TypeScript)

```typescript
// 展开运算符
const newObj = { ...oldObj }
const merged = { ...obj1, ...obj2 }

// Object.assign
const target = Object.assign({}, source)

// 深拷贝
const deep = JSON.parse(JSON.stringify(obj))

// lodash 深拷贝
import { cloneDeep } from 'lodash-es'
const deep = cloneDeep(obj)
```

---

## 5. 消息提示与弹窗

### 后端 (Java)

```java
import plus.ruoyi.common.core.exception.ServiceException;

// 抛出业务异常
throw ServiceException.of("错误信息");
throw ServiceException.of("用户 {} 不存在", userId);

// 条件判断后抛出
if (user == null) {
    throw ServiceException.of("用户不存在");
}
```

### 前端 (TypeScript) - 使用 @/utils/modal

> **重要**: 项目封装了消息工具，推荐使用 `@/utils/modal` 中的函数！

```typescript
import { showMsg, showMsgSuccess, showMsgError, showMsgWarning } from '@/utils/modal'
import { showConfirm, showAlert, showPrompt } from '@/utils/modal'
import { showLoading, hideLoading } from '@/utils/modal'

// 消息提示
showMsgSuccess('操作成功')
showMsgError('操作失败')
showMsgWarning('警告信息')
showMsg('普通提示')

// 确认框（返回 [err, result] 格式）
const [err] = await showConfirm('确定要删除吗?')
if (!err) {
  // 用户确认了
  await deleteRecord()
}

// 带标题的确认框
const [err] = await showConfirm('确定删除?', '删除确认', {
  confirmButtonText: '确定删除',
  cancelButtonText: '取消'
})

// 输入框
const [err, result] = await showPrompt('请输入备注')
if (!err && result.value) {
  saveRemark(result.value)
}

// 加载遮罩
showLoading('数据加载中...')
// ... 操作完成
hideLoading()
```

> **注意**: 虽然 `ElMessage`、`ElMessageBox` 已全局自动导入，但**禁止直接使用**，必须使用封装的 modal 函数！

### 移动端 (UniApp) - 使用 @/wd

> **重要**: 必须从 `@/wd` 导入 useToast/useMessage，不是 `'wot-design-uni'`！

```typescript
// ✅ 正确：从 @/wd 导入
import { useToast, useMessage } from '@/wd'

const toast = useToast()
const message = useMessage()

// Toast 提示
toast.success('操作成功')
toast.error('操作失败')
toast.warning('警告信息')
toast.info('普通提示')
toast.loading('加载中...')

// 确认框
message.confirm({
  title: '提示',
  msg: '确定要删除吗？'
}).then(() => {
  // 确认
}).catch(() => {
  // 取消
})

// ❌ 错误：不要使用 uni.showToast
// uni.showToast({ title: '成功', icon: 'success' })  // 禁止！
// uni.showModal({ ... })  // 禁止！
```

---

## 6. 树结构处理

### 后端 (Java)

```java
import plus.ruoyi.common.core.utils.TreeBuildUtils;

// 构建树结构
List<TreeNode> tree = TreeBuildUtils.build(list, rootId, (node, item) -> {
    node.setId(item.getId());
    node.setParentId(item.getParentId());
    node.setLabel(item.getName());
});
```

### 前端 (TypeScript) - 使用 @/utils/tree

> **重要**: 项目封装了完整的树结构工具！

```typescript
import {
  buildTree,
  findTreeNode,
  findTreeNodePath,
  filterTree,
  flattenTree,
  traverseTree,
  getLeafNodes,
  getTreeDepth,
  insertNode,
  removeNode,
  updateNode
} from '@/utils/tree'

// 构建树（平铺列表 → 嵌套树）
const list = [
  { id: 1, name: '部门1', parentId: 0 },
  { id: 2, name: '部门2', parentId: 1 },
  { id: 3, name: '部门3', parentId: 1 },
]
const tree = buildTree(list)

// 自定义字段名
const tree = buildTree(list, { id: 'itemId', parentId: 'pid' })

// 查找节点
const node = findTreeNode(tree, node => node.id === 3)

// 查找路径（从根到目标）
const path = findTreeNodePath(tree, node => node.id === 3)

// 过滤树
const filtered = filterTree(tree, node => node.status === '1')

// 扁平化（嵌套树 → 平铺数组）
const flatList = flattenTree(tree)

// 遍历树
traverseTree(tree, (node, parent, level) => {
  node.level = level
})

// 获取叶子节点
const leaves = getLeafNodes(tree)

// 获取树深度
const depth = getTreeDepth(tree)
```

---

## 7. 数据格式化

### 前端 (TypeScript) - 使用 @/utils/format

> **重要**: 项目封装了丰富的格式化工具！

```typescript
import {
  formatNumber,
  formatAmount,
  formatPercent,
  formatPhone,
  formatIDCard,
  formatBankCard,
  formatFileSize,
  formatDuration,
  formatPrivacy,
  formatStringLength
} from '@/utils/format'

// 数字格式化
formatNumber(1234.56)                    // "1235"
formatNumber(1234.56, 2)                 // "1234.56"
formatNumber(1234.56, 2, true)           // "1,234.56"（千分位）

// 金额格式化
formatAmount(1234.56)                    // "1,234.56"
formatAmount(1234.56, 2, '.', ',', '¥')  // "¥1,234.56"

// 百分比
formatPercent(0.1234)                    // "12.34%"

// 手机号格式化
formatPhone('13812345678')               // "138-1234-5678"
formatPhone('13812345678', undefined, undefined, true)  // "138****5678"（隐私）

// 身份证脱敏
formatIDCard('110101199001011234')       // "1101**********1234"

// 银行卡格式化
formatBankCard('6225365271562822')       // "6225 3652 7156 2822"
formatBankCard('6225365271562822', ' ', true)  // "**** **** **** 2822"

// 文件大小
formatFileSize(1024)                     // "1.00 KB"
formatFileSize(1234567)                  // "1.18 MB"

// 时长（秒 → 时分秒）
formatDuration(3661)                     // "1小时1分1秒"

// 字符串截断
formatStringLength('这是一个很长的字符串', { maxLength: 10 })  // "这是一个很长..."
```

---

## 8. 数据校验

### 后端 (Java)

```java
import plus.ruoyi.common.core.validate.AddGroup;
import plus.ruoyi.common.core.validate.EditGroup;
import jakarta.validation.constraints.*;

public class XxxBo {
    @NotNull(message = "ID不能为空", groups = { EditGroup.class })
    private Long id;

    @NotBlank(message = "名称不能为空", groups = { AddGroup.class, EditGroup.class })
    @Size(max = 100, message = "名称长度不能超过100")
    private String name;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Min(value = 0, message = "数量不能小于0")
    @Max(value = 9999, message = "数量不能大于9999")
    private Integer count;
}

// Controller 中使用
@PostMapping("/add")
public R<Long> add(@Validated(AddGroup.class) @RequestBody XxxBo bo) {
    // ...
}
```

### 前端 (TypeScript)

```typescript
// Element Plus 表单校验
const rules = {
  name: [
    { required: true, message: '请输入名称', trigger: 'blur' },
    { max: 100, message: '长度不能超过100', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}
```

---

## 9. ID 生成

### 后端 (Java)

```java
import cn.hutool.core.util.IdUtil;

// 雪花算法 ID（推荐）
long id = IdUtil.getSnowflakeNextId();

// UUID
String uuid = IdUtil.simpleUUID();  // 无横线
String uuid = IdUtil.randomUUID();  // 有横线

// 短 ID
String shortId = IdUtil.nanoId();
```

### 前端 (TypeScript)

```typescript
// UUID
const uuid = crypto.randomUUID()

// 简单 ID
const id = Date.now().toString(36) + Math.random().toString(36).substr(2)
```

---

## 10. JSON 操作

### 后端 (Java)

```java
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;

// Hutool
String json = JSONUtil.toJsonStr(obj);
Xxx obj = JSONUtil.toBean(json, Xxx.class);

// FastJSON2
String json = JSON.toJSONString(obj);
Xxx obj = JSON.parseObject(json, Xxx.class);
List<Xxx> list = JSON.parseArray(json, Xxx.class);
```

### 前端 (TypeScript)

```typescript
// 原生
const json = JSON.stringify(obj)
const obj = JSON.parse(json)
```

---

## 11. 加密解密

### 后端 (Java)

```java
import cn.hutool.crypto.SecureUtil;
import cn.hutool.core.codec.Base64;

// MD5
String md5 = SecureUtil.md5("password");

// SHA256
String sha256 = SecureUtil.sha256("password");

// AES 加密
String encrypted = SecureUtil.aes("key".getBytes()).encryptHex("data");
String decrypted = SecureUtil.aes("key".getBytes()).decryptStr(encrypted);

// Base64
String base64 = Base64.encode("data");
String data = Base64.decodeStr(base64);
```

### 前端 (TypeScript)

```typescript
import CryptoJS from 'crypto-js'

// MD5
const md5 = CryptoJS.MD5('password').toString()

// AES
const encrypted = CryptoJS.AES.encrypt('data', 'key').toString()
const decrypted = CryptoJS.AES.decrypt(encrypted, 'key').toString(CryptoJS.enc.Utf8)

// Base64
const base64 = btoa('data')
const data = atob(base64)
```

---

## 12. 前端 Composables 详解 (plus-ui)

> 位于 `plus-ui/src/composables/`

### useTableHeight - 表格高度自适应

```typescript
import { useTableHeight } from '@/composables/useTableHeight'

// 基本用法
const { tableHeight, queryFormRef, showSearch, calculateTableHeight } = useTableHeight()

// 带高度调整（正数减少高度，负数增加高度）
const { tableHeight, queryFormRef } = useTableHeight(50)

// 在模板中使用
// <ASearchForm ref="queryFormRef">...</ASearchForm>
// <el-table :height="tableHeight">...</el-table>
```

### useDict - 字典数据

```typescript
import { useDict, DictTypes } from '@/composables/useDict'

// 获取字典
const { sys_user_gender, sys_enable_status } = useDict(
  DictTypes.sys_user_gender,
  DictTypes.sys_enable_status
)

// 在模板中使用
// <AFormSelect :options="sys_user_gender" v-model="form.gender" />
```

### useDialog - 弹窗控制

```typescript
import { useDialog } from '@/composables/useDialog'

const { visible, open, close, toggle } = useDialog()

// 打开弹窗
open()

// 关闭弹窗
close()
```

### useSelection - 表格选择

```typescript
import { useSelection } from '@/composables/useSelection'

const { selectionItems, selectionIds, handleSelectionChange, clearSelection } = useSelection()

// 绑定到表格
// <el-table @selection-change="handleSelectionChange">
```

### useDownload - 文件下载

```typescript
import { useDownload } from '@/composables/useDownload'

const { download, downloadByUrl, downloadByBlob } = useDownload()

// 下载文件
await download('/api/export', { params })
```

### useAuth - 权限判断

```typescript
import { useAuth } from '@/composables/useAuth'

const { hasPermi, hasRole } = useAuth()

// 判断权限
if (hasPermi('system:user:add')) {
  // 有权限
}
```

### useWS / useSSE - 实时通信

```typescript
// WebSocket
import { useWS } from '@/composables/useWS'
const { connect, send, close, message, status } = useWS()
connect('ws://localhost/ws')

// SSE
import { useSSE } from '@/composables/useSSE'
const { connect, close, message } = useSSE()
connect('/api/sse')
```

---

## 13. 移动端 Composables 详解 (plus-uniapp)

> 位于 `plus-uniapp/src/composables/`

### useAuth - 认证逻辑

```typescript
import { useAuth } from '@/composables/useAuth'

const { checkLogin, requireLogin, logout } = useAuth()

// 检查是否登录
if (!checkLogin()) {
  requireLogin()  // 跳转登录页
}
```

### useDict - 字典数据

```typescript
import { useDict, DictTypes } from '@/composables/useDict'

// 获取字典数据（用于下拉选择）
const { sys_user_gender, dictLoading } = useDict(
  DictTypes.sys_user_gender
)

// 获取字典标签（用于显示）
const dictStore = useDictStore()  // 已自动导入
const label = dictStore.getDictLabel('sys_user_gender', '0')  // 返回 '男'
```

### usePayment - 支付功能

```typescript
import { usePayment } from '@/composables/usePayment'

const { createOrderAndPay, payOrder } = usePayment()

// 创建订单并支付
const result = await createOrderAndPay({
  productId: '123',
  amount: 100
})
```

### useWebSocket - WebSocket

```typescript
import { useWebSocket } from '@/composables/useWebSocket'

const { connect, send, close, message, status } = useWebSocket()

// 连接
connect('wss://example.com/ws')

// 发送消息
send({ type: 'chat', content: 'hello' })
```

### useWxShare - 微信分享

```typescript
import { useWxShare } from '@/composables/useWxShare'

const { initSdk, setShare } = useWxShare()

// 初始化微信SDK
await initSdk()

// 设置分享内容
setShare({
  title: '分享标题',
  desc: '分享描述',
  link: 'https://example.com',
  imgUrl: 'https://example.com/img.png'
})
```

---

## 14. 常用正则

```typescript
// 手机号
const phoneReg = /^1[3-9]\d{9}$/

// 邮箱
const emailReg = /^[\w-]+(\.[\w-]+)*@[\w-]+(\.[\w-]+)+$/

// 身份证
const idCardReg = /^[1-9]\d{5}(19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/

// URL
const urlReg = /^https?:\/\/.+/

// 中文
const chineseReg = /^[\u4e00-\u9fa5]+$/
```

---

## 自动导入说明

### 前端 (plus-ui)

以下内容已全局自动导入，**无需手动 import**：

- `dayjs` - 日期处理
- `ref`, `reactive`, `computed`, `watch` 等 Vue APIs
- Store 函数：`useDictStore`, `useUserStore` 等
- `http` - HTTP 请求工具

**⚠️ 消息/弹窗必须使用封装工具**：

```typescript
// ✅ 正确：使用 @/utils/modal
import { showMsgSuccess, showConfirm } from '@/utils/modal'

// ❌ 错误：直接使用 ElMessage（虽然已自动导入，但禁止使用）
// ElMessage.success('xxx')  // 禁止！
```

### 移动端 (plus-uniapp)

以下内容已全局自动导入：

- `dayjs` - 日期处理
- `ref`, `reactive`, `computed`, `watch` 等 Vue APIs
- Store 函数：`useDictStore`, `useUserStore` 等

**⚠️ 需要手动导入**：

```typescript
// useToast / useMessage 必须手动从 @/wd 导入
import { useToast, useMessage } from '@/wd'
```
