---
name: security-guard
description: |
  安全开发规范。包含 Sa-Token 认证授权、数据脱敏、数据加密、接口安全、漏洞防护。

  触发场景：
  - Sa-Token 权限控制配置
  - 登录认证、Token 管理
  - 数据脱敏处理（@Sensitive）
  - 数据加密处理（@EncryptField、@ApiEncrypt）
  - 接口限流（@RateLimiter）
  - 防重复提交（@RepeatSubmit）
  - XSS/SQL注入防护
  - 前端权限指令（v-permi、v-role）

  触发词：安全、Sa-Token、@SaCheckPermission、@SaCheckLogin、@SaCheckRole、登录认证、Token、数据脱敏、@Sensitive、加密解密、@EncryptField、@ApiEncrypt、限流、@RateLimiter、防重复、@RepeatSubmit、XSS、SQL注入、CSRF、漏洞防护、敏感数据、LoginHelper、v-permi、v-role、useAuth

  注意：
  - 如需行级数据权限（@DataPermission、部门隔离），请使用 data-permission。
  - 如果是设计异常处理机制（try-catch、错误码），请使用 error-handler。
---
# security-guard

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
- 1. Sa-Token 认证授权
- 2. 数据脱敏（@Sensitive）
- 3. 数据加密（@EncryptField / @ApiEncrypt）
- 4. 接口限流（@RateLimiter）
- 5. 防重复提交（@RepeatSubmit）
- 6. 数据权限（@DataPermission）
- 7. 输入校验
- 8. 常见漏洞防护
- 9. 安全检查清单
