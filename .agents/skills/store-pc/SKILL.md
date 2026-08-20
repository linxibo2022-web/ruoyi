---
name: store-pc
description: |
  PC 端（plus-ui）状态管理与 Composables 指南。包含 Pinia Store、Composables 组合式函数的创建和使用规范。

  触发场景：
  - 在 PC 后台创建/使用 Store
  - Pinia 状态管理
  - 跨组件数据共享（PC端）
  - 持久化存储（localCache/sessionCache）
  - HTTP 请求链式调用
  - 权限判断
  - 字典数据加载

  触发词：PC Store、Pinia、defineStore、useUserStore、useDictStore、PC状态管理、useHttp、http、链式调用、useAuth、useToken、useDict、useTableHeight、localCache、sessionCache、缓存、持久化

  适用目录：plus-ui/**
---
# store-pc

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
- 自动导入说明
- 已有 Store 清单
- Composables 清单
- 创建新 Store
- 使用 Store
- 持久化存储
- 常用模式
- 最佳实践
- 与组件配合使用
- 参考文件
