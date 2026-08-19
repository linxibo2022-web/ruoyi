---
name: notification-system
description: |
  当需要发送通知消息、短信验证码、邮件通知、统一消息推送时自动使用此 Skill。

  触发场景：
  - 需要发送短信（验证码、通知、营销）
  - 需要发送邮件（验证码、通知、HTML邮件）
  - 需要使用统一消息推送服务（多通道路由、降级、广播）
  - 需要了解消息通道接口规范和扩展方式
  - 需要为业务模块集成消息推送能力
  - 需要配置短信/邮件服务

  触发词：短信、SMS、邮件、Mail、Email、消息推送、MessagePushService、MessageChannel、通知、验证码、SmsFactory、MailUtils、sendText、sendHtml、统一消息、消息路由、多通道
---
# notification-system

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
- 一、短信模块（ruoyi-common-sms）
- 二、邮件模块（ruoyi-common-mail）
- 三、统一消息推送（ruoyi-common-message）
- 四、SmsMessageChannel 实现参考
- 五、扩展自定义通道
- 六、公告通知系统（SysNotice）
- 七、完整业务集成示例
- 八、常见错误与最佳实践
- 九、与其他技能的关系
- 十、参考文件索引
- 十一、FAQ
