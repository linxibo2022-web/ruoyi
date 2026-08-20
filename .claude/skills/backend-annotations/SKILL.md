---
name: backend-annotations
description: |
  当需要使用后端高级注解时自动使用此 Skill。包含 SerialMap、RateLimiter、RepeatSubmit、Sensitive、DataPermission 等注解。

  触发场景：
  - 数据序列化映射（ID转名称、字典转标签）
  - 接口限流配置
  - 防重复提交
  - 敏感数据脱敏
  - 数据权限控制

  触发词：SerialMap、限流、RateLimiter、防重复、RepeatSubmit、脱敏、Sensitive、数据权限、DataPermission、ID转名称、字典转换、OSS转URL
---
# backend-annotations

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
- 注解一览
- 1. @SerialMap - 序列化映射
- 2. @RateLimiter - 接口限流
- 3. @RepeatSubmit - 防重复提交
- 4. @Sensitive - 数据脱敏
- 5. @DataPermission - 数据权限
- 最佳实践
