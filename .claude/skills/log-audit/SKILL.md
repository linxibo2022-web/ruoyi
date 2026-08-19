---
name: log-audit
description: |
  当需要为业务接口添加操作日志、配置审计追踪、排查日志内容时自动使用此 Skill。涵盖 @Log 注解、操作类型、登录日志、敏感参数脱敏。

  触发场景：
  - 为 Controller 接口添加 @Log 注解记录操作日志
  - 配置 @Log 的操作类型（DictOperType）
  - 排除敏感参数（密码、Token）不写入日志
  - 查询或导出 sys_oper_log 操作日志表
  - 配置登录日志（sys_logininfor）
  - 定位"接口是否有写日志"、"日志里为什么没记录参数"

  触发词：操作日志、登录日志、审计、@Log、sys_oper_log、sys_logininfor、DictOperType、LogAspect、OperLogEvent、LoginLogPublisher、excludeParamNames、isSaveRequestData、日志记录、审计追踪、日志脱敏
---
# log-audit

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
- 核心注解：@Log
- 标准用法
- 登录日志
- 日志数据表
- ✅ 正确做法
- ❌ 常见错误
- 查询与清理
- 🔗 关联技能边界
