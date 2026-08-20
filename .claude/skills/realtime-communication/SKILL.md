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
# realtime-communication

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
- 概述
- 选型决策
- 配置
- 一、WebSocket 开发指南
- 二、SSE 开发指南
- 三、集群架构
- 四、多租户支持
- 五、常见错误与最佳实践
- 六、重连与心跳机制
- 七、与其他技能的关系
- 八、完整开发流程示例
- 九、参考文件索引
- 十、FAQ
