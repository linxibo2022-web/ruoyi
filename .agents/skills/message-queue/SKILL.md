---
name: message-queue
description: |
  当需要使用 RocketMQ 消息队列进行异步通信、系统解耦、削峰填谷时自动使用此 Skill。

  触发场景：
  - 需要发送异步消息（同步/异步/单向/延迟/事务消息）
  - 需要实现消息消费者监听处理
  - 需要管理 Topic（创建/删除/查询/验证路由）
  - 需要延迟消息实现定时业务（订单超时取消等）
  - 需要事务消息保证分布式数据一致性
  - 需要排查 RocketMQ 连接和路由问题

  触发词：RocketMQ、消息队列、MQ、异步消息、延迟消息、事务消息、RMSendUtil、RMTopicUtil、DelayLevel、Topic、消费者、生产者、削峰填谷、系统解耦、sendAsync、sendDelay、sendTransaction、RocketMQMessageListener
---
# message-queue

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
- 模块结构
- 配置
- 一、消息发送（RMSendUtil）
- 二、延迟级别（DelayLevel）
- 三、消费者开发
- 四、Topic 管理（RMTopicUtil）
- 五、故障诊断（RMDiagnosticUtil）
- 六、实战场景
- 七、常见错误与最佳实践
- 八、与 Redis 方案对比
- 九、与其他技能的关系
- 十、参考文件索引
- 十一、FAQ
