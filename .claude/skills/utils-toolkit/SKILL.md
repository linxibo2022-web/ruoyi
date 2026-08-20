---
name: utils-toolkit
description: |
  工具类智能匹配助手 - 根据上下文自动提供后端/前端/移动端对应的工具类和最佳实践。

  触发场景（智能识别端）：
  - 日期时间处理 → 后端 DateUtils / 前端 dayjs / 移动端 dayjs
  - 字符串操作 → 后端 StringUtils / 前端 原生方法
  - 集合操作 → 后端 CollUtil / 前端 Array 方法
  - 对象转换 → 后端 MapstructUtils（必须使用！）
  - 数据校验 → 后端 @Validated / 前端 Form 校验
  - 加密解密 → 后端 SecureUtil / 前端 crypto-js
  - 树结构处理 → 后端 TreeBuildUtils / 前端 @/utils/tree
  - 消息提示 → 后端 ServiceException / 前端 modal.ts / 移动端 useToast
  - 数据格式化 → 后端 自定义 / 前端 @/utils/format
  - HTTP 请求 → 后端 RestTemplate / 前端 http 封装

  触发词：工具类、日期、时间、字符串、集合、数组、转换、校验、加密、格式化、处理、工具、utils、Hutool、dayjs、lodash、树结构、tree、权限、下载、打印、弹窗、消息、toast、modal、websocket、sse、composable、hook

  智能规则：
  - 写 Java 代码 → 提供后端工具类
  - 写 Vue/TypeScript 代码 → 提供前端工具和 Composables
  - 写 UniApp 代码 → 提供移动端工具和已有 Composables
---
# utils-toolkit

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
- 使用规则
- 快速索引
- 1. 日期时间处理
- 2. 字符串操作
- 3. 集合操作
- 4. 对象转换 (重要！)
- 5. 消息提示与弹窗
- 6. 树结构处理
- 7. 数据格式化
- 8. 数据校验
- 9. ID 生成
- 10. JSON 操作
- 11. 加密解密
- 12. 前端 Composables 详解 (plus-ui)
- 13. 移动端 Composables 详解 (plus-uniapp)
- 14. 常用正则
- 自动导入说明
