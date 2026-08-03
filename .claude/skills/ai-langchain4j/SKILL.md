---
name: ai-langchain4j
description: |
  当需要集成 AI 大模型、智能对话功能时自动使用此 Skill。支持 DeepSeek、通义千问、OpenAI、Claude、Ollama。

  触发场景：
  - AI 对话功能开发
  - 流式响应处理
  - 多轮对话管理
  - 知识库 RAG 集成
  - 函数调用实现
  - MCP 工具调用集成（接入外部 MCP Server）
  - 对外提供 MCP 服务（把系统能力暴露给外部 Agent）

  触发词：AI、大模型、ChatGPT、DeepSeek、通义千问、Claude、流式、对话、RAG、知识库、Embedding、langchain4j、MCP、Model Context Protocol、MCP Server、对外提供MCP、工具调用、函数调用、McpToolProvider、McpClient、stdio、AiServices、TokenStream、Spring AI
---

# AI 大模型集成指南

## 支持的模型提供商

| 提供商 | 枚举值 | 说明 |
|--------|-------|------|
| DeepSeek | `DEEPSEEK` | 性价比高，推荐默认 |
| 通义千问 | `QIANWEN` | 阿里云，中文优化 |
| OpenAI | `OPENAI` | GPT 系列 |
| Claude | `CLAUDE` | Anthropic |
| Ollama | `OLLAMA` | 本地部署 |

---

## 核心架构

```
用户请求 → ChatService → ModelFactory → 模型提供商
              ↓
         ChatMemoryManager → RedisChatStore（可选）
              ↓
         StreamChatHandler → WebSocket 响应
```

### 核心类职责

| 类 | 职责 |
|----|------|
| `ChatService` | 统一对话入口，支持同步/流式 |
| `ModelFactory` | 模型实例工厂 |
| `ChatMemoryManager` | 会话历史管理 |
| `RedisChatStore` | Redis 持久化存储 |
| `AiChatMessageProcessor` | WebSocket 消息处理 |

---

## 对话模式

| 模式 | 枚举值 | 说明 | 实现状态 |
|------|-------|------|---------|
| 单轮对话 | `SINGLE` | 无上下文，每次独立 | ✅ 已实现 |
| 多轮对话 | `CONTINUOUS` | 保持上下文，连续对话 | ✅ 已实现 |
| 知识库增强 | `RAG` | 结合向量检索 | ⚠️ **空壳**，`ChatService.java:159` 仍是 `// TODO`，实际回落 `CONTINUOUS` |
| 函数调用 | `FUNCTION` | 调用外部工具 | ⚠️ **空壳**，`ChatService.java:170` / `:245` 仍是 `// TODO`，实际回落 `CONTINUOUS` |

> 🔴 **写代码前必读**：`RAG` 与 `FUNCTION` 目前是**声明了但未实现**的模式 —— 传这两个枚举不会报错，但行为等同于 `CONTINUOUS`。
> 不要因为枚举存在就断定功能可用，也不要凭空调用 `properties.getRag().getXxx()` 之外不存在的 API。
> 要让 `FUNCTION` 真正工作，见下方《MCP 工具调用》章节。

---

## 后端使用

### 注入 ChatService

```java
import plus.ruoyi.common.langchain4j.core.chat.ChatService;
import plus.ruoyi.common.langchain4j.domain.request.ChatRequest;
import plus.ruoyi.common.langchain4j.domain.response.ChatResponse;
import plus.ruoyi.common.langchain4j.domain.enums.ChatMode;
import plus.ruoyi.common.langchain4j.domain.enums.ModelProvider;

@Service
@RequiredArgsConstructor
public class AiServiceImpl implements IAiService {

    private final ChatService chatService;

    /**
     * 同步对话
     */
    public ChatResponse chat(String message) {
        ChatRequest request = ChatRequest.builder()
            .sessionId("user-123")
            .message(message)
            .mode(ChatMode.CONTINUOUS)       // 多轮对话
            .provider(ModelProvider.DEEPSEEK)
            .modelName("deepseek-chat")
            .systemPrompt("你是一个专业的客服助手")
            .temperature(0.7)
            .maxTokens(2000)
            .build();

        return chatService.chat(request);
    }

    /**
     * 流式对话
     */
    public void streamChat(String message, Consumer<ChatResponse> onResponse) {
        ChatRequest request = ChatRequest.builder()
            .sessionId("user-123")
            .message(message)
            .mode(ChatMode.CONTINUOUS)
            .provider(ModelProvider.DEEPSEEK)
            .build();

        chatService.streamChat(request, response -> {
            // 每次收到内容都会回调
            onResponse.accept(response);

            if (response.isFinished()) {
                // 对话完成
                log.info("Token 使用: {}", response.getTokenUsage());
            }
        });
    }
}
```

### ChatRequest 参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `sessionId` | String | 是 | 会话ID，用于多轮对话 |
| `message` | String | 是 | 用户消息 |
| `mode` | ChatMode | 否 | 对话模式，默认 SINGLE |
| `provider` | ModelProvider | 否 | 模型提供商，默认配置值 |
| `modelName` | String | 否 | 模型名称，默认配置值 |
| `systemPrompt` | String | 否 | 系统提示词 |
| `temperature` | Double | 否 | 温度 0-1，默认 0.7 |
| `maxTokens` | Integer | 否 | 最大 Token 数 |

### ChatResponse 字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `sessionId` | String | 会话ID |
| `messageId` | String | 消息ID |
| `content` | String | 响应内容 |
| `finished` | boolean | 是否完成 |
| `tokenUsage` | TokenUsage | Token 统计 |
| `references` | List | RAG 引用来源 |
| `error` | String | 错误信息 |
| `responseTime` | Long | 响应时间(ms) |

### TokenUsage 字段

```java
public class TokenUsage {
    private Integer promptTokens;      // 提示词 Token
    private Integer completionTokens;  // 生成 Token
    private Integer totalTokens;       // 总 Token
}
```

---

## WebSocket 集成

### 消息类型

| 类型 | 方向 | 说明 |
|------|------|------|
| `ai_chat` | 客户端→服务端 | 发起对话 |
| `ai_chat_start` | 服务端→客户端 | 开始响应 |
| `ai_chat_stream` | 服务端→客户端 | 流式内容 |
| `ai_chat_complete` | 服务端→客户端 | 完成 |
| `ai_chat_error` | 服务端→客户端 | 错误 |

### 请求格式

```typescript
// 发送消息
{
  type: 'ai_chat',
  sessionId: 'session-xxx',
  message: '你好',
  mode: 'CONTINUOUS',
  provider: 'DEEPSEEK',
  modelName: 'deepseek-chat',
  systemPrompt: '你是一个助手'
}
```

### 响应格式

```typescript
// 开始响应
{
  type: 'ai_chat_start',
  sessionId: 'session-xxx',
  messageId: 'msg-xxx'
}

// 流式内容（多次）
{
  type: 'ai_chat_stream',
  sessionId: 'session-xxx',
  messageId: 'msg-xxx',
  content: '你好'  // 增量内容
}

// 完成
{
  type: 'ai_chat_complete',
  sessionId: 'session-xxx',
  messageId: 'msg-xxx',
  tokenUsage: {
    promptTokens: 10,
    completionTokens: 50,
    totalTokens: 60
  }
}

// 错误
{
  type: 'ai_chat_error',
  sessionId: 'session-xxx',
  error: '错误信息'
}
```

---

## 前端集成 (PC Web)

### useAiChatStore

```typescript
import { useAiChatStore } from '@/stores/modules/aiChat'

const aiChatStore = useAiChatStore()

// 创建会话
const sessionId = aiChatStore.createSession()

// 发送消息
await aiChatStore.sendMessage('你好，请介绍一下自己', {
  provider: 'DEEPSEEK',
  mode: 'CONTINUOUS',
  systemPrompt: '你是一个专业的助手'
})

// 获取当前会话
const session = aiChatStore.currentSession

// 获取消息列表
const messages = session?.messages || []

// 监听流式内容
watch(() => aiChatStore.streamContentBuffer, (content) => {
  // 实时更新 UI
})
```

### Store 状态

```typescript
interface AiChatState {
  sessions: Map<string, AiChatSession>  // 所有会话
  currentSessionId: string | null       // 当前会话ID
  streamingMessageId: string | null     // 正在流式生成的消息ID
  streamContentBuffer: string           // 流式内容缓冲
}

interface AiChatSession {
  id: string
  title: string
  messages: AiChatMessage[]
  createdAt: number
  updatedAt: number
}

interface AiChatMessage {
  id: string
  role: 'user' | 'assistant'
  content: string
  tokenUsage?: TokenUsage
  createdAt: number
}
```

### Store 方法

| 方法 | 说明 |
|------|------|
| `createSession()` | 创建新会话 |
| `switchSession(id)` | 切换会话 |
| `deleteSession(id)` | 删除会话 |
| `sendMessage(content, options)` | 发送消息 |
| `onChatStart(sessionId, messageId)` | WebSocket 回调：开始 |
| `appendStreamContent(sessionId, messageId, content)` | WebSocket 回调：追加内容 |
| `onChatComplete(sessionId, messageId, tokenUsage)` | WebSocket 回调：完成 |
| `onChatError(sessionId, error)` | WebSocket 回调：错误 |

---

## 会话管理

### 会话ID生成规则

```java
// 推荐格式：业务类型-用户ID-时间戳
String sessionId = "chat-" + userId + "-" + System.currentTimeMillis();

// 或使用 UUID
String sessionId = UUID.randomUUID().toString();
```

### 会话历史存储

```yaml
# langchain4j.yml
langchain4j:
  chat:
    history-size: 20           # 保留最近20条消息
    session-timeout: 30        # 会话30分钟过期
    memory-store-type: redis   # 使用 Redis 存储
```

### 手动管理会话

```java
@Service
@RequiredArgsConstructor
public class SessionService {

    private final ChatMemoryManager memoryManager;

    // 清除会话历史
    public void clearSession(String sessionId) {
        memoryManager.clearMemory(sessionId);
    }

    // 获取会话消息
    public List<ChatMessage> getSessionMessages(String sessionId) {
        return memoryManager.getMessages(sessionId);
    }
}
```

---

## 配置说明

### langchain4j.yml

```yaml
langchain4j:
  enabled: true

  # 默认配置
  default-provider: deepseek
  default-model: deepseek-chat

  # 对话配置
  chat:
    stream-enabled: true       # 启用流式
    history-size: 20           # 历史消息数
    session-timeout: 30        # 会话超时(分钟)
    memory-store-type: redis   # memory | redis

  # DeepSeek 配置
  deepseek:
    api-key: ${DEEPSEEK_API_KEY:}
    base-url: https://api.deepseek.com
    models:
      - name: deepseek-chat
        max-tokens: 4096
        temperature: 0.7
      - name: deepseek-coder
        max-tokens: 8192
        temperature: 0.3

  # 通义千问配置
  qianwen:
    api-key: ${QIANWEN_API_KEY:}
    models:
      - name: qwen-turbo
        max-tokens: 6000
      - name: qwen-plus
        max-tokens: 30000

  # OpenAI 配置
  openai:
    api-key: ${OPENAI_API_KEY:}
    base-url: https://api.openai.com
    models:
      - name: gpt-4o
        max-tokens: 4096
      - name: gpt-4o-mini
        max-tokens: 16384

  # Ollama 本地配置
  ollama:
    base-url: http://localhost:11434
    models:
      - name: llama3
        max-tokens: 4096

  # Embedding 配置
  embedding:
    model-name: text-embedding-v2
    dimension: 1536
    batch-size: 100

  # RAG 配置
  rag:
    enabled: false
    max-results: 5
    min-score: 0.7
    chunk-size: 500
    chunk-overlap: 50
```

---

## MCP 工具调用（Model Context Protocol）

MCP 是让大模型调用外部工具的开放协议。接入后，AI 对话可以调用任意 MCP Server 提供的工具（查天气、读文件、查数据库…），
框架侧只做**通用装配**，具体能力由使用者接什么 Server 决定 —— 这比框架自己维护一套 `@Tool` 注册表更符合框架定位。

### ⚠️ 当前框架状态（动手前必看）

| 项目 | 状态 |
|------|------|
| langchain4j 版本 | `1.14.1`（根 `pom.xml` 的 `${langchain4j.version}`）→ **版本已满足，无需升级** |
| `langchain4j-mcp` 依赖 | ❌ **未引入** |
| `LangChain4jProperties` 的 `mcp` 配置节点 | ❌ **不存在** |
| `core/mcp` 包（客户端工厂等） | ❌ **不存在** |
| `ChatMode.FUNCTION` | ⚠️ 空壳，回落 `CONTINUOUS` |
| **MCP 服务端**（对外提供服务） | ❌ 未实现，`ruoyi-common-mcp-server` 模块不存在；且**不能用 langchain4j 做**，见本章末子章节 |

🔴 **这意味着**：现在往 `langchain4j.yml` 写 `mcp:` 配置**不会生效**；代码里引用 `properties.getMcp()`、`McpClientFactory`、`McpToolProvider`
会**直接编译失败**。本章描述的是**接入方案**，不是既有能力 —— 不要照着写完就以为能跑。

> 真要接入时的推进顺序：依赖引入 → 配置属性（默认关闭）→ 客户端工厂（含 `DisposableBean` 回收）→
> 自动配置装配（`ObjectProvider` 注入避免强依赖）→ `FUNCTION` 分支（同步 / 流式）。
> 每一步都必须保证 **MCP 关闭时既有行为零变化**。

### LangChain4j 的 MCP 支持范围

| 方向 | 模块 | 可用性 |
|------|------|--------|
| **MCP 客户端**（本框架去调别人的 MCP Server） | `dev.langchain4j:langchain4j-mcp` | 官方主仓模块，与主版本同步发布 → 1.14.1 可用 |
| **MCP 服务端**（把 RuoYi 能力暴露给外部 Agent） | `dev.langchain4j.community.mcp.server.McpServer` | ⚠️ **仅 stdio 传输，做不了 HTTP 服务** |

**本章主体讲客户端**。服务端方向（对外提供 MCP 服务）langchain4j 走不通，须换技术栈 ——
见下方《反向：把 RuoYi 做成 MCP Server》。

客户端支持的传输方式（包路径 `dev.langchain4j.mcp.client.transport.*`）：

| 传输 | 类 | 适用场景 |
|------|-----|---------|
| Streamable HTTP | `http.StreamableHttpMcpTransport` | **MCP 规范当前推荐**，远程 Server 首选 |
| 旧版 SSE | `http.HttpMcpTransport`（`.sseUrl(...)`） | 对接只实现了老规范的 Server |
| stdio | `stdio.StdioMcpTransport` | 把 MCP Server 拉起为本地子进程 |
| WebSocket | `websocket.*` | 少见 |
| Docker stdio | `docker.*` | Server 跑在容器里 |

### 依赖坐标

```xml
<!-- ruoyi-common/ruoyi-common-langchain4j/pom.xml -->
<!-- 版本复用根 pom 的 ${langchain4j.version}，不新增版本属性 -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-mcp</artifactId>
    <version>${langchain4j.version}</version>
    <exclusions>
        <!-- 与本模块其余 5 个 provider 依赖保持一致：排除 slf4j-simple，用项目统一的 Logback -->
        <exclusion>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-simple</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

### 客户端接入三步

```java
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import dev.langchain4j.service.tool.ToolProvider;

// 1. 传输层
McpTransport transport = StreamableHttpMcpTransport.builder()
    .url("http://localhost:3001/mcp")
    .logRequests(false)   // 排障时打开，生产关闭（会打印完整报文）
    .logResponses(false)
    .build();

// 2. 客户端：key 用于日志定位与多 Server 工具名去重，务必显式设置
McpClient mcpClient = DefaultMcpClient.builder()
    .key("weather")
    .transport(transport)
    .build();

// 3. 工具提供者
ToolProvider toolProvider = McpToolProvider.builder()
    .mcpClients(mcpClient)
    .build();
```

除工具外，MCP 的 resources 可通过 `McpToolProvider.Builder.resourcesAsToolsPresenter(...)` 以「合成工具」形式暴露给模型。

### 🔴 与本框架 ChatService 的衔接（最容易写错的地方）

`toolProvider` 是 **`AiServices` 的构建参数**，而本框架的 `ChatService` **没有用 `AiServices`** —— 它直接调
`chatModel.chat(messages)` / `streamingModel.chat(messages, handler)`。所以：

❌ **不存在**「给 `ChatService` 加一行 `.toolProvider(...)` 就能用」这种改法。

✅ **正确做法**：`FUNCTION` 模式**单开一条 `AiServices` 分支**，不动 `SINGLE` / `CONTINUOUS` / `RAG` 三条既有链路。

**为什么不全量迁移到 AiServices**：现有三条链路的 thinking 注入（`ModelFactory.ThinkingOptions`）和
`StreamChatHandler` 的 THINKING/CONTENT 分相推送，都建立在裸 `chat()` 调用上；全量迁移会牵动前端流式协议，
而 `FUNCTION` 本就是空壳 —— 在它上面新建分支，改动面完全隔离在一个 `case` 里。

```java
// ChatService 中 FUNCTION 分支的正确形态（同步）
private AiChatResponse handleFunctionChat(ChatModel chatModel, ChatRequest request, String sessionId) {
    ToolProvider toolProvider = toolProviderProvider.getIfAvailable();
    if (toolProvider == null) {
        // 🔴 框架必须优雅降级：MCP 未启用/建连全失败时静默回落，不能抛异常
        log.debug("MCP 工具不可用，FUNCTION 模式回落为 CONTINUOUS");
        return handleContinuousChat(chatModel, request, sessionId);
    }
    McpAssistant assistant = AiServices.builder(McpAssistant.class)
        .chatModel(chatModel)
        .toolProvider(toolProvider)
        .chatMemory(memoryManager.getOrCreateMemory(sessionId))   // 复用既有会话记忆
        .build();
    Result<String> result = assistant.chat(request.getMessage());  // Result 包装才能拿到工具执行明细
    return buildFunctionResponse(result);
}
```

`McpAssistant` 是 AI Service 契约接口，**由 `AiServices` 动态代理实现，不需要写实现类**：

```java
public interface McpAssistant {
    Result<String> chat(String message);   // 同步：Result 可取 toolExecutions()
    TokenStream chatStream(String message); // 流式
}
```

### 流式工具调用：TokenStream 回调对照

`TokenStream` 是**独立于 `StreamingChatResponseHandler` 的另一套回调**，现有 `StreamChatHandler` **不能直接复用**，
必须桥接成同样的 `Consumer<AiChatResponse>` 语义，前端协议才不用改：

| TokenStream 回调 | 桥接到框架的 | 说明 |
|-----------------|-------------|------|
| `onPartialThinking(PartialThinking)` | `Phase.THINKING` | 推理内容增量 |
| `onPartialResponse(String)` | `Phase.CONTENT` | 正文增量 |
| `beforeToolExecution(BeforeToolExecution)` | `Phase.TOOL`（需新增） | 工具执行**前**，前端可显示「正在调用 xxx」 |
| `onToolExecuted(ToolExecution)` | 日志 | 工具执行**后**，含执行结果 |
| `onCompleteResponse(ChatResponse)` | `finished=true` + TokenUsage | 全流程结束（TokenUsage 是所有轮次的合计） |
| `onError(Throwable)` | `error` 字段 | 异常 |

```java
tokenStream
    .onPartialThinking(pt -> consumer.accept(thinkingChunk(messageId, pt.text())))
    .onPartialResponse(text -> consumer.accept(contentChunk(messageId, text)))
    .beforeToolExecution(before -> consumer.accept(
        toolChunk(messageId, before.request().name(), before.request().arguments())))
    .onToolExecuted(exec -> log.debug("MCP 工具执行完成: {}", exec.request().name()))
    .onCompleteResponse(resp -> consumer.accept(finishedResponse(messageId, resp)))
    .onError(err -> consumer.accept(errorResponse(messageId, err)))
    .start();   // 🔴 必须调 start()，否则流根本不会开始
```

> 现有 `Phase` 只有 `THINKING` / `CONTENT` 两个值（见 `AiChatResponse.Phase`）。要展示工具调用过程，
> 需同步新增 `TOOL` 值，并在 `AiChatMessageProcessor.java:187` 的分相分发（目前是 `if(THINKING) else` 二分支）里加分支。

### 🔴 安全红线（框架会被子项目照抄，必须守住）

| 红线 | 说明 |
|------|------|
| **stdio 的 `command` 只能来自配置文件** | stdio 传输 = 在服务器上起进程执行命令。`command` 若来自用户输入、请求参数或数据库可编辑字段，等价于**远程命令执行漏洞**。这也是「MCP Server 后台可视化配置」必须禁用 stdio 传输的原因 |
| **工具调用要熔断** | 配 `max-sequential-tool-calls`（建议 10），防止模型陷入「调工具→再调工具」死循环烧 token |
| **MCP 工具不带租户上下文** | 工具在框架层执行，**不经过 MyBatis 租户拦截器**。若 MCP Server 会读写业务库，须在工具侧自行做租户隔离 |
| **凭据不落明文** | MCP Server 的 token 一律走 `${ENV_VAR:}` 占位符，不写死在 yml |
| **stdio 子进程必须回收** | 客户端工厂要实现 `DisposableBean`，容器关闭时 `client.close()`，否则留孤儿进程 |

### 反向：把 RuoYi 做成 MCP Server（对外提供服务）

上面讲的都是**客户端**方向 —— 本框架去调别人的工具。反方向是让 Claude Desktop / Cursor / 外部 Agent
来调 RuoYi 的能力。这是**另一件独立的事**，两个方向可以并存，但**技术选型完全不同**。

#### 🔴 langchain4j 做不到，别往这条路上撞

langchain4j 的四种 MCP 传输实现，包路径全部在 **`client`** 下：

```
dev.langchain4j.mcp.client.transport.http        ← 客户端
dev.langchain4j.mcp.client.transport.stdio       ← 客户端
dev.langchain4j.mcp.client.transport.websocket   ← 客户端
dev.langchain4j.mcp.client.transport.docker      ← 客户端
```

服务端**只有一个**：`dev.langchain4j.community.mcp.server.transport.StdioMcpServerTransport`（官方文档对应页 `mcp-stdio-server`），
**没有任何 HTTP server 传输**。

而 stdio 的语义是"客户端把服务端当子进程拉起来，通过标准输入输出通信"—— RuoYi 是常驻的 Spring Boot HTTP 服务，
**模型对不上**：没法让远程的 Cursor 把你服务器上的 Java 进程拉起来。

**结论：对外提供 MCP 服务必须走 HTTP 传输，必须引入 langchain4j 之外的依赖。**

#### 选型对比

| 方案 | 坐标 | 取舍 |
|------|------|------|
| **官方 MCP Java SDK**（推荐） | `io.modelcontextprotocol.sdk:mcp` | **最轻**，只有协议实现 + Servlet 传输，不引入任何 AI 框架；需自己写 Bean 装配 |
| Spring AI MCP Server | `spring-ai-starter-mcp-server-webmvc` | 最省事（注解式 `@McpTool` / `@McpToolParam` + 自动配置），但把 spring-ai 生态拉进依赖树 |
| 自己实现 | 无 | Streamable HTTP 本质是 JSON-RPC 2.0 over HTTP POST（+SSE），不算难，但要跟着规范演进，长期维护成本高 |

两点澄清：

1. Spring AI 那个 starter **底层就是官方 MCP Java SDK**，直接用 SDK 不会失去任何协议能力。
2. 官方 SDK 与 `langchain4j-mcp` **不冲突** —— groupId 与包名完全不同（`io.modelcontextprotocol` vs `dev.langchain4j.mcp`），
   各管一个方向。代价是项目里会有两套 MCP 协议实现共存，但方向不同，不互相干扰。

**框架推荐官方 SDK**：为一个默认关闭的可选能力把 Spring AI 整套生态拉进 `ruoyi-common`，
会让"这项目到底用哪个 AI 框架"变含糊，将来排障和 `module-strip` 裁剪都更麻烦。

#### 🔴 真正的难点不是协议，是安全上下文

协议接起来是小工作量。对**多租户框架**来说，下面四条才决定成败：

| 问题 | 说明 | 处理方向 |
|------|------|---------|
| **认证怎么进来** | MCP 规范的授权走 OAuth 2.1，但不必真去实现 | 传输层的 `contextExtractor` 可抓 HTTP header（如 `Authorization`）塞进 `McpTransportContext`，交给 Sa-Token 校验即可 |
| 🔴 **租户上下文必须显式注入** | MCP 工具方法**不在**正常 HTTP 请求链路里，`TenantHelper` 上下文是空的 → MyBatis 租户拦截器拿不到 `tenant_id` → **查出全租户数据**，数据泄漏级问题 | 工具执行前从认证信息解析租户并显式设置 `TenantHelper`，执行后清理 |
| 🔴 **`@DataPermission` 会失效** | 行级数据权限依赖 `LoginHelper` 的登录用户，MCP 调用链里没有请求上下文 → 部门/本人权限过滤全部落空 | 同上，手动重建登录上下文 |
| **暴露范围要白名单** | 绝不能把 Controller 全量自动暴露 —— MCP 工具是给 AI **自主调用**的，一个 `deleteXxxs` 被模型误调就是生产事故 | 显式白名单；建议只暴露查询类，写操作不给或加二次确认 |

Spring AI 侧提取 header 的写法（官方 SDK 有等价机制，类名以实际版本为准）：

```java
@Bean
public WebMvcStreamableServerTransportProvider transport() {
    return WebMvcStreamableServerTransportProvider.builder()
        .contextExtractor(serverRequest -> {
            String authorization = serverRequest.headers().firstHeader("Authorization");
            return McpTransportContext.create(Map.of("authorization", authorization));
        })
        .build();
}
```

#### 模块归属建议

做成 **`ruoyi-common-mcp-server` 独立模块**，与客户端能力（在 `ruoyi-common-langchain4j` 内）**分开**，默认关闭。
理由：它引入的是完全不同的依赖栈，混进 langchain4j 模块会让裁剪、排障、版本升级都纠缠不清。

> 📌 本子章节是**选型结论与避坑清单**，框架尚未实现服务端能力，`ruoyi-common-mcp-server` 模块目前**不存在**。

### 常见错误

| ❌ 错误做法 | ✅ 正确做法 |
|-----------|-----------|
| 以为传 `ChatMode.FUNCTION` 就能调工具 | 当前是空壳，回落 `CONTINUOUS`；需先完成 MCP 接入 |
| 想用 langchain4j 对外提供 MCP 服务 | 它服务端只有 stdio，做不了 HTTP 服务；须用官方 MCP Java SDK 或 Spring AI starter |
| MCP Server 的工具方法直接查库 | 不在 HTTP 请求链路里，租户与数据权限上下文都是空的，必须手动注入 |
| 把 Controller 全量暴露成 MCP 工具 | 显式白名单，写操作慎给 —— AI 会自主调用 |
| 给 `ChatService` 现有 `chat()` 加 `toolProvider` | `toolProvider` 是 `AiServices` 的参数，须单开 `AiServices` 分支 |
| 用 `StreamChatHandler` 接 `TokenStream` | 两套回调不兼容，须写桥接 |
| 忘记调 `tokenStream.start()` | 不调则流永不开始，表现为「请求发出去但没有任何响应」 |
| MCP 建连失败就抛异常中断启动 | 框架的可选能力配错不能让应用起不来 → 跳过该 Server + `warn` |
| `mcp.enabled` 默认 `true` | 必须默认 `false`，框架升级不能改变老项目行为 |
| 把 MCP Server 的 `command` 拼接用户输入 | 命令执行漏洞，只能来自 yml |

---

## 自定义扩展

### 自定义模型工厂

```java
@Component
public class CustomModelFactory extends ModelFactory {

    @Override
    public ChatLanguageModel createChatModel(ModelProvider provider, String modelName) {
        // 自定义模型创建逻辑
        if (provider == ModelProvider.CUSTOM) {
            return createCustomModel(modelName);
        }
        return super.createChatModel(provider, modelName);
    }
}
```

### 自定义消息处理器

```java
@Component
public class CustomChatProcessor extends AiChatMessageProcessor {

    @Override
    protected void beforeChat(AiChatWebSocketRequest request) {
        // 对话前处理（如：鉴权、限流）
        log.info("用户 {} 发起对话", request.getUserId());
    }

    @Override
    protected void afterChat(ChatResponse response) {
        // 对话后处理（如：记录日志、统计）
        log.info("消耗 Token: {}", response.getTokenUsage().getTotalTokens());
    }
}
```

---

## 最佳实践

### 1. 会话ID设计

```java
// ✅ 推荐：包含业务含义
String sessionId = "order-consult-" + userId + "-" + orderId;
String sessionId = "customer-service-" + userId;

// ❌ 避免：纯随机ID（难以追踪）
String sessionId = UUID.randomUUID().toString();
```

### 2. 系统提示词优化

```java
// ✅ 推荐：明确角色和约束
String systemPrompt = """
    你是一个专业的电商客服助手。

    你的职责：
    1. 回答用户关于订单、物流、退款的问题
    2. 如果问题超出范围，礼貌引导用户联系人工客服

    注意事项：
    - 回答简洁明了，不要冗余
    - 涉及敏感信息时，要求用户验证身份
    - 不要编造订单信息
    """;

// ❌ 避免：过于简单
String systemPrompt = "你是客服";
```

### 3. 流式响应处理

```typescript
// ✅ 推荐：使用缓冲区合并内容
watch(() => aiChatStore.streamContentBuffer, (content) => {
  currentMessage.value = content
}, { immediate: true })

// ❌ 避免：每个 token 都触发渲染
onStream((token) => {
  document.getElementById('content').innerHTML += token
})
```

### 4. 错误处理

```java
try {
    ChatResponse response = chatService.chat(request);
    if (response.getError() != null) {
        // 业务错误
        throw ServiceException.of(response.getError());
    }
    return response;
} catch (Exception e) {
    // 系统错误
    log.error("AI 对话异常", e);
    throw ServiceException.of("AI 服务暂时不可用，请稍后重试");
}
```

### 5. Token 控制

```java
// 根据场景设置合适的 maxTokens
ChatRequest request = ChatRequest.builder()
    .message(message)
    .maxTokens(getMaxTokensByScene(scene))
    .build();

private int getMaxTokensByScene(String scene) {
    return switch (scene) {
        case "quick_reply" -> 200;    // 快速回复
        case "normal_chat" -> 1000;   // 普通对话
        case "long_content" -> 4000;  // 长文本生成
        default -> 500;
    };
}
```

---

## 常见问题

### 1. 流式响应断开

- 检查 WebSocket 连接状态
- 确认 `stream-enabled: true`
- 检查网络超时设置

### 2. 会话上下文丢失

- 确认 `sessionId` 一致
- 检查 `mode: CONTINUOUS`
- 检查 Redis 连接（如使用 Redis 存储）

### 3. Token 超限

- 减少 `history-size`
- 使用 `SINGLE` 模式
- 选择支持更多 Token 的模型

### 4. 响应速度慢

- 使用流式响应提升体验
- 选择更快的模型（如 deepseek-chat）
- 减少 `maxTokens`

### 5. 传了 `FUNCTION` 模式但 AI 不调用工具

先确认是不是**根本没接**：`ChatService.java:170` / `:245` 若仍是 `// TODO`，说明 MCP 未集成，
`FUNCTION` 会静默回落 `CONTINUOUS` —— 表现就是"AI 正常回话，但从不调工具"。

已接入后仍不调用，按序排查：

1. `langchain4j.mcp.enabled` 是否为 `true`（默认 `false`）
2. 启动日志有没有 `MCP 客户端建连成功`；若是 `建连失败(已跳过)` 则看 `原因=`
3. `servers` 列表是否为空（空列表会 `warn` 一次后降级）
4. MCP Server 是否真的暴露了工具 —— 打开 `logRequests: true` 看 `tools/list` 的返回
5. 模型本身是否支持工具调用（部分小模型 / 本地 Ollama 模型不支持）

### 6. MCP Server 连不上

| 现象 | 排查方向 |
|------|---------|
| HTTP 传输 404 | Streamable HTTP 端点通常是 `/mcp`，旧版 SSE 是 `/sse`，两者**不能混用**（用错传输类就会 404） |
| stdio 起不来 | `command` 要用**绝对路径或确保在 PATH 中**；Windows 下 `npx` 实际是 `npx.cmd` |
| 应用启动被卡住 | 建连不能放在阻塞主线程的位置，且单点失败必须跳过而非抛异常 |
| 关闭应用后残留进程 | stdio 会拉起子进程，客户端工厂必须实现 `DisposableBean` 并 `client.close()` |

> 🔴 排查残留进程时，按 PID 精准结束（`Stop-Process -Id <PID>`），**禁止**按名字批杀 `node.exe`（会误杀其他工具的宿主进程）。
