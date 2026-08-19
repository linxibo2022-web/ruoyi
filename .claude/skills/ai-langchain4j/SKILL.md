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
# ai-langchain4j

## 执行边界

- 本入口只保留触发说明、最小执行原则与资料索引；开始实质实施前，先识别任务涉及的专题，再按需阅读对应完整资料。
- 项目根规则中的中文、编码、安全、架构及并发保护要求始终优先；不得因资料拆分降低既有约束。
- 不需要完整资料的只读解释、状态查询或简单定位，不得默认加载全文。

## 最小步骤

1. 根据当前任务确定所需专题。
2. 按需读取 `references/full-guide.md` 中相关章节，并遵守其中原有硬约束。
3. 仅在任务范围内实施并执行受影响范围的验证。

## 资料索引

- `references/full-guide.md`：原入口的完整规范、模板、案例和边界。主要专题：
- 支持的模型提供商
- 核心架构
- 对话模式
- 后端使用
- WebSocket 集成
- 前端集成 (PC Web)
- 会话管理
- 配置说明
- MCP 工具调用（Model Context Protocol）
- 自定义扩展
- 最佳实践
- 常见问题
