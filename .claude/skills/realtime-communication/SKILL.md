---
name: realtime-communication
description: |
  当需要实现实时通信功能时自动使用此 Skill。包含 WebSocket 双向通信和 SSE 服务端推送的完整开发指南。

  触发场景：
  - 需要实现 WebSocket 实时双向通信（聊天、在线状态）
  - 需要实现 SSE 服务端推送（AI 流式响应、通知推送）
  - 需要选择 WebSocket 还是 SSE 方案
  - 需要向指定用户或全局推送消息
  - 需要实现心跳检测和断线重连
  - 需要在集群环境下分发实时消息

  触发词：WebSocket、SSE、实时推送、在线聊天、消息推送、双向通信、服务端推送、流式响应、EventSource、心跳、在线状态、ws://、wss://、SseEmitter、WebSocketUtils、SseMessageUtils、publishMessage、useWS、useSSE、useWebSocket
---

# 实时通信开发指南

## 概述

本项目提供两种实时通信方案：**WebSocket**（双向通信）和 **SSE**（服务端单向推送）。两者均支持多租户隔离、集群 Redis 分发、多连接管理。

| 特性 | WebSocket | SSE |
|------|-----------|-----|
| **通信方向** | 双向（客户端 ↔ 服务端） | 单向（服务端 → 客户端） |
| **协议** | ws:// / wss:// | HTTP/HTTPS |
| **对应模块** | `ruoyi-common-websocket` | `ruoyi-common-sse` |
| **适用场景** | 聊天、在线状态、游戏 | AI 流式响应、通知推送、进度条 |
| **多租户** | 三级映射（租户→用户→会话） | 二级映射（用户→Token） |
| **管理接口** | 完整 REST API（12 个端点） | 简单 REST API（4 个端点） |
| **小程序支持** | 需要条件编译适配 | 不支持（仅 H5/APP） |

---

## 选型决策

```
需要客户端发送消息给服务端？
├─ 是 → WebSocket
│   ├─ 聊天功能 → WebSocket
│   ├─ 在线协作 → WebSocket
│   └─ 双向交互 → WebSocket
└─ 否 → 仅服务端推送？
         ├─ 是 → SSE
         │   ├─ AI 流式响应 → SSE
         │   ├─ 系统通知 → SSE
         │   └─ 实时进度 → SSE
         └─ 否 → 不需要实时通信
```

**优先选择 WebSocket 的场景**：
- 需要双向通信（客户端也要发消息）
- 需要小程序支持（SSE 在小程序中不可用）
- 需要完整的连接管理（在线用户列表、强制下线等）

**优先选择 SSE 的场景**：
- 仅需服务端推送（AI 流式响应）
- 不需要小程序支持
- 希望利用 HTTP 协议优势（自动重连、浏览器兼容好）

---

## 配置

### application.yml

```yaml
# WebSocket 配置
websocket:
  # 如果关闭 需要和前端开关一起关闭
  enabled: true                    # 启用 WebSocket
  path: /resource/websocket        # 端点路径
  allowedOrigins: '*'              # 允许跨域

# SSE 配置
sse:
  enabled: false                   # ⚠️ 默认禁用，需显式启用
  path: /resource/sse              # 连接路径
```

> **注意**：SSE 默认禁用（`enabled: false`），使用前需在 `application.yml` 中改为 `true`。

---

## 一、WebSocket 开发指南

### 后端核心类

| 类名 | 包路径 | 用途 |
|------|--------|------|
| `WebSocketUtils` | `plus.ruoyi.common.websocket.utils` | 消息发送工具类（最常用） |
| `WebSocketMessageDto` | `plus.ruoyi.common.websocket.dto` | 消息传输对象 |
| `WebSocketSessionHolder` | `plus.ruoyi.common.websocket.holder` | 会话管理器 |
| `MessageProcessor` | `plus.ruoyi.common.websocket.processor` | 消息处理器接口 |
| `WebSocketAdminController` | `plus.ruoyi.common.websocket.controller` | 管理接口（12 个端点） |

### 后端发送消息

#### 1. 向指定用户发消息

```java
import plus.ruoyi.common.websocket.utils.WebSocketUtils;
import plus.ruoyi.common.websocket.dto.WebSocketMessageDto;

// 方式一：向单个用户发消息
WebSocketUtils.publishMessage(WebSocketMessageDto.of(userId, "你有一条新订单"));

// 方式二：向多个用户发消息
List<Long> userIds = List.of(1001L, 1002L, 1003L);
WebSocketUtils.publishMessage(WebSocketMessageDto.of(userIds, "会议即将开始"));
```

#### 2. 群发消息

```java
// 当前租户内群发
WebSocketUtils.publishAll("系统将于今晚 22:00 维护");

// 全局群发（所有租户，需超级管理员权限）
WebSocketUtils.publishGlobal("平台升级通知");

// 跨租户定向发送（需超级管理员权限）
WebSocketUtils.publishCrossTenant("000001", List.of(userId), "专属通知");
```

#### 3. WebSocketMessageDto 工厂方法

```java
// 单用户消息
WebSocketMessageDto.of(userId, message)

// 多用户消息
WebSocketMessageDto.of(List.of(userId1, userId2), message)

// 当前租户广播
WebSocketMessageDto.broadcast(message)

// 全局广播（所有租户）
WebSocketMessageDto.globalBroadcast(message)

// 跨租户消息
WebSocketMessageDto.crossTenant(tenantId, List.of(userId), message)
```

#### 4. 查询在线状态

```java
import plus.ruoyi.common.websocket.holder.WebSocketSessionHolder;

// 检查用户是否在线
boolean online = WebSocketSessionHolder.isUserOnline(userId);

// 获取当前租户在线用户 ID
Set<Long> onlineUsers = WebSocketSessionHolder.getAllUserIds();

// 获取全局所有在线用户 ID
Set<Long> allOnline = WebSocketSessionHolder.getGlobalAllUserIds();

// 获取连接统计
WebSocketSessionHolder.ConnectionStats stats = WebSocketSessionHolder.getConnectionStats();
int onlineCount = stats.getOnlineUsers();
int totalConnections = stats.getTotalConnections();
```

#### 5. 实际使用示例：公告通知

```java
// 参考：SysNoticeServiceImpl.java
@Override
public void sendNoticeNotification(SysNoticeBo bo) {
    if (StringUtils.isNotBlank(bo.getTargetUserIds())
        && DictEnableStatus.isEnabled(bo.getStatus())) {
        String type = DictNoticeType.getByValue(bo.getNoticeType()).getLabel();
        String message = "[" + type + "] " + bo.getNoticeTitle();
        List<Long> userIds = StringUtils.splitToList(bo.getTargetUserIds(), Convert::toLong);
        WebSocketUtils.publishMessage(WebSocketMessageDto.of(userIds, message));
    }
}
```

#### 6. 实际使用示例：开发日志推送

```java
// 参考：DevLogController.java
@SaIgnore
@PostMapping("/collect")
public R<Void> collect(@Validated @RequestBody DevLogBo bo) {
    DevLogMessageDto messageDto = DevLogMessageDto.of(bo.getLogs());
    WebSocketMessageDto wsMessageDto = WebSocketMessageDto.of(
        List.of(TenantConstants.SUPER_ADMIN_ID),
        JsonUtils.toJsonString(messageDto)
    );
    WebSocketUtils.publishMessage(wsMessageDto);
    return R.ok();
}
```

### 自定义消息处理器

实现 `MessageProcessor` 接口，可以在服务端处理客户端发来的消息。

```java
import plus.ruoyi.common.websocket.processor.MessageProcessor;
import plus.ruoyi.system.domain.vo.LoginUser;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
@Order(10)  // 数字越小优先级越高（ping=1, 业务兜底=MAX_VALUE）
public class AiChatMessageProcessor implements MessageProcessor {

    @Override
    public boolean support(String type) {
        return "ai_chat".equals(type);
    }

    @Override
    public void process(WebSocketSession session, LoginUser loginUser, String payload) {
        log.info("收到 AI 聊天消息: userId={}, payload={}", loginUser.getUserId(), payload);
        // 处理 AI 聊天逻辑...
        // 可通过 WebSocketUtils.sendMessage(session, response) 回复
    }
}
```

**消息路由规则**：
1. 客户端发送纯字符串 `"ping"` → 快速回复 `"pong"`
2. 客户端发送 JSON `{"type": "ai_chat", ...}` → 根据 `type` 路由到对应 Processor
3. 客户端发送其他文本 → 由 `BusinessMessageProcessor`（兜底）处理

### 管理接口（WebSocketAdminController）

> 需要超级管理员或租户管理员角色

| 方法 | 路径 | 用途 |
|------|------|------|
| GET | `/webSocket/getStats` | 获取连接统计 |
| GET | `/webSocket/getOnlineUsers?global=false` | 获取在线用户 ID |
| GET | `/webSocket/getUserSessions/{userId}` | 获取用户会话 ID |
| GET | `/webSocket/checkUserOnline/{userId}` | 检查用户是否在线 |
| GET | `/webSocket/getUserDetails/{userId}` | 获取用户连接详情 |
| POST | `/webSocket/sendUserMessage` | 向用户发消息 |
| POST | `/webSocket/sendSessionMessage` | 向特定会话发消息 |
| POST | `/webSocket/batchSendMessage` | 批量发消息 |
| POST | `/webSocket/broadcastMessage` | 群发消息 |
| POST | `/webSocket/crossTenantMessage` | 跨租户发消息 |
| POST | `/webSocket/disconnectUser/{userId}` | 强制下线用户 |
| POST | `/webSocket/disconnectSession` | 断开特定会话 |

### 前端使用（PC 端 plus-ui）

**参考文件**：`plus-ui/src/composables/useWS.ts`（1075 行）

#### 消息类型枚举

```typescript
export enum WSMessageType {
  SYSTEM_NOTICE = 'system_notice'       // 系统通知
  AI_CHAT_START = 'ai_chat_start'       // AI 聊天开始
  AI_CHAT_STREAM = 'ai_chat_stream'     // AI 流式响应
  AI_CHAT_COMPLETE = 'ai_chat_complete' // AI 生成完成
  AI_CHAT_ERROR = 'ai_chat_error'       // AI 生成错误
  CHAT_MESSAGE = 'chat_message'         // 聊天消息
  DEV_LOG = 'devLog'                    // 开发日志
  HEARTBEAT = 'heartbeat'              // 心跳
}

export interface WSMessage {
  type: WSMessageType
  data: any
  timestamp: number
  id?: string
}
```

#### 基础用法：useWS

```typescript
import { useWS } from '@/composables/useWS'

const { connect, disconnect, reconnect, send, status, isConnected, data } = useWS(wsUrl, {
  maxRetries: 8,              // 最大重试次数
  baseDelay: 3,               // 基础延迟（秒），指数退避：3→6→12→24...
  heartbeatInterval: 30,      // 心跳间隔（秒）
  onConnected: () => {
    console.log('已连接')
  },
  onMessage: (msg) => {
    console.log('收到消息:', msg)
  },
  onDisconnected: (code, reason) => {
    console.log('已断开:', code, reason)
  }
})

// 连接
connect()

// 发送消息
send(JSON.stringify({ type: 'ai_chat', data: { prompt: '你好' } }))

// 断开
disconnect()
```

#### 全局管理器：GlobalWebSocketManager

```typescript
import { webSocket } from '@/composables/useWS'

// 初始化（通常在 App.vue 中）
webSocket.initialize(undefined, {
  onConnected: () => console.log('全局 WS 已连接')
})
webSocket.connect()

// 在任意组件中使用
webSocket.send(JSON.stringify({ type: 'chat_message', data: { text: '你好' } }))

// 添加自定义消息处理器
webSocket.addMessageHandler({
  type: 'order_update',
  handle: async (message) => {
    console.log('订单更新:', message.data)
  }
})

// 销毁（应用卸载时）
webSocket.destroy()
```

#### 内置消息处理器

| 处理器 | 处理类型 | 功能 |
|--------|---------|------|
| `SystemNoticeHandler` | `system_notice` | 显示通知弹窗，存入通知中心 |
| `AiChatStreamHandler` | `ai_chat_*` | 处理 AI 流式响应 |
| `HeartbeatHandler` | `heartbeat` | 心跳响应 |

### 移动端使用（plus-uniapp）

**参考文件**：`plus-uniapp/src/composables/useWebSocket.ts`（980 行）

#### 基础用法

```typescript
import { useWebSocket } from '@/composables/useWebSocket'

const { connect, disconnect, send, isConnected, data } = useWebSocket(wsUrl, {
  maxRetries: 8,
  baseDelay: 3,
  heartbeatInterval: 30,
  onMessage: (msg) => {
    console.log('收到消息:', msg)
  }
})

connect()
```

#### 全局管理器

```typescript
import { webSocket } from '@/composables/useWebSocket'

// 初始化（App.vue）
webSocket.initialize(undefined, {
  onConnected: () => console.log('移动端 WS 已连接')
})
webSocket.connect()

// 发送消息
webSocket.send(JSON.stringify({ type: 'chat_message', data: '你好' }))

// 添加处理器
webSocket.addMessageHandler({
  type: 'order_status',
  handle: async (message) => {
    // 处理订单状态变化
  }
})
```

#### 移动端特殊处理

```typescript
// 移动端使用 uni.connectSocket() 而非浏览器原生 WebSocket
// 自动根据环境选择协议：
// - H5: ws:// 或 wss://
// - 小程序: wss://（强制安全连接）

// 心跳通过 setInterval 实现（非 VueUse 内置）
// 重连策略与 PC 端一致：指数退避
```

---

## 二、SSE 开发指南

### 后端核心类

| 类名 | 包路径 | 用途 |
|------|--------|------|
| `SseMessageUtils` | `plus.ruoyi.common.sse.utils` | SSE 消息发送工具类（最常用） |
| `SseMessageDto` | `plus.ruoyi.common.sse.dto` | SSE 消息传输对象 |
| `SseEmitterManager` | `plus.ruoyi.common.sse.core` | SSE 连接管理器 |
| `SseController` | `plus.ruoyi.common.sse.controller` | SSE HTTP 接口 |

### 后端发送消息

```java
import plus.ruoyi.common.sse.utils.SseMessageUtils;
import plus.ruoyi.common.sse.dto.SseMessageDto;

// 1. 向指定用户推送
SseMessageUtils.publishMessage(SseMessageDto.of(List.of(userId), "你有新消息"));

// 2. 向所有用户广播
SseMessageUtils.publishAll("系统通知");

// 3. 检查 SSE 是否启用
if (SseMessageUtils.isEnable()) {
    // 发送消息
}
```

### SSE 接口（SseController）

> SSE 端点路径通过 `sse.path` 配置，默认需登录（`@SaIgnore` 仅作用于连接检查，实际调用 `StpUtil.checkLogin()`）

| 方法 | 路径 | 用途 |
|------|------|------|
| GET | `${sse.path}` | 建立 SSE 连接（返回 `text/event-stream`） |
| GET | `${sse.path}/close` | 关闭当前连接 |
| GET | `${sse.path}/send?userId=&msg=` | 向指定用户推送 |
| GET | `${sse.path}/sendAll?msg=` | 全局广播 |

### AI 流式响应示例

```java
// 后端：结合 LangChain4j 实现 AI 流式响应
@PostMapping("/ai/chat/stream")
public SseEmitter chatStream(@RequestBody AiChatRequest request) {
    Long userId = LoginHelper.getUserId();
    String token = StpUtil.getTokenValue();

    // 1. 创建 SSE 连接
    SseEmitter emitter = sseEmitterManager.connect(userId, token);

    // 2. 异步调用 AI 模型
    CompletableFuture.runAsync(() -> {
        try {
            aiService.streamChat(request.getPrompt(), chunk -> {
                // 3. 流式推送每个 chunk
                try {
                    emitter.send(SseEmitter.event()
                        .name("message")
                        .data(chunk));
                } catch (IOException e) {
                    log.error("SSE 推送失败", e);
                }
            });
            // 4. 推送完成标记
            emitter.send(SseEmitter.event().name("complete").data("done"));
            emitter.complete();
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    });

    return emitter;
}
```

### 前端使用（PC 端 plus-ui）

**参考文件**：`plus-ui/src/composables/useSSE.ts`（226 行）

```typescript
import { useSSE } from '@/composables/useSSE'

const { close, reconnect, status, unreadCount, eventSource } = useSSE(sseUrl, {
  maxRetries: 8,
  baseDelay: 3
})

// SSE 自动连接、自动重连
// 收到消息自动更新未读数量（unreadCount）
// 关闭连接
close()
```

#### SSE 与 AI 流式响应

```typescript
// 前端使用 EventSource 或 fetch + ReadableStream 接收 AI 流式响应
const eventSource = new EventSource('/api/ai/chat/stream')

eventSource.addEventListener('message', (event) => {
  // 每收到一个 chunk，追加到界面
  chatContent.value += event.data
})

eventSource.addEventListener('complete', () => {
  eventSource.close()
})

eventSource.addEventListener('error', () => {
  eventSource.close()
})
```

---

## 三、集群架构

两种方案都通过 **Redis Pub/Sub** 实现集群消息分发。

### WebSocket 集群流程

```
服务端实例 A                    Redis                    服务端实例 B
     │                          │                          │
     │  publishMessage(dto)     │                          │
     ├─── 本地用户? ───────────→│                          │
     │    ├─ 是 → 直接发送      │                          │
     │    └─ 否 → Redis 发布 ──→│── Pub/Sub ──────────────→│
     │                          │                          │
     │                          │    WebSocketTopicListener │
     │                          │    ├─ 全局消息 → 所有租户 │
     │                          │    ├─ 定向消息 → 指定用户 │
     │                          │    └─ 群发消息 → 租户全员 │
```

### SSE 集群流程

```
服务端实例 A                    Redis                    服务端实例 B
     │                          │                          │
     │  publishMessage(dto)     │                          │
     │── Redis 发布 ──────────→│── Pub/Sub ──────────────→│
     │                          │                          │
     │                          │    SseTopicListener       │
     │                          │    ├─ 有 userIds → 指定用户│
     │                          │    └─ 无 userIds → 全员   │
```

### Redis 主题

| 主题 | 用途 |
|------|------|
| `global:websocket` | WebSocket 消息分发 |
| `global:sse` | SSE 消息分发 |

---

## 四、多租户支持

### WebSocket 三级映射

```
TENANT_SESSION_MAP: Map<String, Map<Long, Map<String, WebSocketSession>>>
  └─ tenantId
     └─ userId
        └─ sessionId → WebSocketSession
```

- 消息发送自动绑定当前租户上下文
- `publishAll()` 仅发送给当前租户用户
- `publishGlobal()` 发送给所有租户（需超管权限）
- `publishCrossTenant()` 跨租户发送（需超管权限）

### SSE 二级映射

```
USER_TOKEN_EMITTERS: Map<Long, Map<String, SseEmitter>>
  └─ userId
     └─ token → SseEmitter
```

- SSE 当前不区分租户（按用户 ID 管理）
- 多设备/多标签页各自独立连接（以 Token 区分）
- SseEmitter 超时时间为 86400000 毫秒（1 天），超时后自动清理

---

## 五、常见错误与最佳实践

### ✅ 正确做法

```java
// 1. 使用 publishMessage 而非 sendMessage（支持集群）
WebSocketUtils.publishMessage(WebSocketMessageDto.of(userId, message));

// 2. 判断 SSE 是否启用再发送
if (SseMessageUtils.isEnable()) {
    SseMessageUtils.publishMessage(SseMessageDto.of(userIds, message));
}

// 3. 自定义处理器使用 @Order 控制优先级
@Component
@Order(10)  // 数字越小越先执行
public class MyProcessor implements MessageProcessor { }
```

### ❌ 常见错误

```java
// 1. 直接使用 sendMessage（仅本机有效，集群环境会丢消息）
WebSocketUtils.sendMessage(userId, message);  // ❌ 不支持集群
WebSocketUtils.publishMessage(dto);           // ✅ 集群 + 本地

// 2. 忘记开启配置
// websocket.enabled 或 sse.enabled 未设置为 true

// 3. 在非超管角色调用全局方法
WebSocketUtils.publishGlobal(message);        // ❌ 非超管会被拒绝
WebSocketUtils.publishAll(message);           // ✅ 当前租户内群发

// 4. SSE 中使用 List.of()（不可变列表，序列化可能出问题）
SseMessageDto.of(List.of(userId), message);   // ⚠️ 建议用 new ArrayList
```

### 前端常见错误

```typescript
// ❌ 直接使用浏览器原生 WebSocket
const ws = new WebSocket(url)

// ✅ 使用项目封装的 useWS（内置重连、心跳、消息管道）
import { useWS } from '@/composables/useWS'
const { connect, send, isConnected } = useWS(url)

// ❌ 移动端导入 PC 端的 composable
import { useWS } from '@/composables/useWS'        // ❌ PC 端

// ✅ 移动端使用专用的 useWebSocket
import { useWebSocket } from '@/composables/useWebSocket'  // ✅ 移动端
```

---

## 六、重连与心跳机制

### 指数退避重连

前端和移动端均使用相同的指数退避策略：

```
重试次数:  1    2    3    4     5     6      7      8
延迟(秒):  3    6    12   24    48    96     192    384（上限）
```

- `maxRetries` 默认 8 次
- `baseDelay` 默认 3 秒
- 公式：`delay = baseDelay * 2^(retryCount - 1)`

### 心跳检测

| 端 | 机制 | 间隔 |
|----|------|------|
| PC 前端 | VueUse `useWebSocket` 内置心跳 | 30 秒 |
| 移动端 | `setInterval` 定时发送 | 30 秒 |
| 后端 | 客户端发 `"ping"`，服务端回 `"pong"` | 客户端驱动 |

---

## 七、与其他技能的关系

| 技能 | 关系 |
|------|------|
| `ai-langchain4j` | AI 流式响应通过 SSE 推送 |
| `redis-cache` | 集群消息通过 Redis Pub/Sub 分发 |
| `multi-tenant` | WebSocket 支持多租户会话隔离 |
| `security-guard` | 连接握手时验证 Sa-Token 登录状态 |
| `notification-system` | WebSocket/SSE 作为 MessageChannel 接入统一消息推送 |

---

## 八、完整开发流程示例

### 场景：为订单模块添加状态实时推送

#### 1. 后端：在订单状态变更时推送

```java
@Service
public class OrderServiceImpl implements IOrderService {

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, String newStatus) {
        // 1. 更新数据库
        orderDao.updateStatus(orderId, newStatus);

        // 2. 查询订单所属用户
        Order order = orderDao.selectById(orderId);

        // 3. 构建消息
        String message = JsonUtils.toJsonString(Map.of(
            "type", "order_status",
            "orderId", orderId,
            "status", newStatus
        ));

        // 4. 推送给订单用户
        WebSocketUtils.publishMessage(
            WebSocketMessageDto.of(order.getUserId(), message)
        );
    }
}
```

#### 2. PC 前端：监听订单状态变化

```typescript
import { webSocket } from '@/composables/useWS'

// 注册订单状态处理器
webSocket.addMessageHandler({
  type: 'order_status',
  handle: async (message) => {
    const { orderId, status } = message.data
    // 更新页面上的订单状态
    updateOrderInTable(orderId, status)
    // 显示通知
    ElNotification.success({ title: '订单更新', message: `订单 ${orderId} 状态已变更` })
  }
})
```

#### 3. 移动端：监听订单状态变化

```typescript
import { webSocket } from '@/composables/useWebSocket'
import { useToast } from '@/wd'

const toast = useToast()

webSocket.addMessageHandler({
  type: 'order_status',
  handle: async (message) => {
    const { orderId, status } = message.data
    toast.success(`订单 ${orderId} 状态已更新`)
    // 刷新订单列表
    refreshOrderList()
  }
})
```

---

## 九、参考文件索引

### 后端

| 文件 | 行数 | 说明 |
|------|------|------|
| `ruoyi-common/ruoyi-common-websocket/` | - | WebSocket 模块根目录 |
| `utils/WebSocketUtils.java` | ~370 | 消息发送工具类 |
| `dto/WebSocketMessageDto.java` | ~90 | 消息 DTO（含工厂方法） |
| `holder/WebSocketSessionHolder.java` | ~200 | 三级映射会话管理 |
| `handler/PlusWebSocketHandler.java` | ~265 | 消息路由处理器 |
| `processor/MessageProcessor.java` | ~15 | 消息处理器接口 |
| `controller/WebSocketAdminController.java` | ~230 | 管理 REST API |
| `ruoyi-common/ruoyi-common-sse/` | - | SSE 模块根目录 |
| `utils/SseMessageUtils.java` | ~99 | SSE 消息工具类 |
| `core/SseEmitterManager.java` | ~196 | SSE 连接管理器 |
| `controller/SseController.java` | ~70 | SSE HTTP 接口 |

### 前端 / 移动端

| 文件 | 行数 | 说明 |
|------|------|------|
| `plus-ui/src/composables/useWS.ts` | 1075 | PC 端 WebSocket Composable |
| `plus-ui/src/composables/useSSE.ts` | 226 | PC 端 SSE Composable |
| `plus-uniapp/src/composables/useWebSocket.ts` | 980 | 移动端 WebSocket Composable |

### 实际使用示例

| 文件 | 说明 |
|------|------|
| `ruoyi-system/config/service/impl/SysNoticeServiceImpl.java` | 公告通知推送 |
| `ruoyi-system/monitor/controller/DevLogController.java` | 开发日志推送 |

---

## 十、FAQ

### Q1: WebSocket 连接需要登录吗？

**A**: 是。`PlusWebSocketInterceptor` 在握手时通过 `LoginHelper.getLoginUser()` 验证登录状态，未登录的连接会被拒绝。

### Q2: 同一个用户可以建立多个连接吗？

**A**: 可以。WebSocket 以 `sessionId` 区分不同连接，SSE 以 `token` 区分。同用户在多个浏览器标签页或设备上各自独立连接。

### Q3: publishMessage 和 sendMessage 有什么区别？

**A**:
- `sendMessage` - 仅发送到**本机**的连接，集群环境下其他实例收不到
- `publishMessage` - 智能分发：本机在线直接发送，不在线通过 **Redis Pub/Sub** 转发到其他实例

生产环境**必须用 `publishMessage`**。

### Q4: SSE 在小程序中能用吗？

**A**: 不能。小程序不支持 `EventSource` API。需要实时通信请使用 WebSocket。

### Q5: 如何在 AI 流式响应中使用 SSE？

**A**: 后端返回 `SseEmitter`，逐块推送数据。前端使用 `EventSource` 或 `fetch + ReadableStream` 接收。具体参考"AI 流式响应示例"章节。

### Q6: 消息格式有什么约定？

**A**: 推荐使用 JSON 格式，包含 `type` 字段用于路由：
```json
{
  "type": "order_status",
  "data": { "orderId": 123, "status": "paid" },
  "timestamp": 1707600000000
}
```
后端 `MessageProcessor` 根据 `type` 字段路由到对应处理器。
