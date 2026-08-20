---
name: api-development
description: |
  API 接口设计规范、RESTful 设计、前后端对接约定。（与 crud-development 区别：本 Skill 专注于"接口设计规范"，crud-development 用于"完整业务模块开发"）

  触发场景：
  - 设计新的 API 接口路径
  - 定义 RESTful 规范
  - 前后端接口对接约定
  - 接口命名规范
  - R<T> 响应格式设计

  触发词：API设计、接口规范、RESTful、URL设计、接口路径、请求响应、R<T>、统一响应、接口命名、端点设计

  注意：如果是开发完整的 CRUD 业务模块（Entity/Service/DAO/Controller），请使用 crud-development。
---
# api-development

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
- RESTful 设计规范
- 后端 API 实现
- 前端 API 对接
- 响应格式
- 参数校验
- 错误处理
