---
name: store-mobile
description: |
  移动端（plus-uniapp）状态管理与 Composables 完整指南。

  触发场景：
  - 在移动端创建/使用 Store（Pinia）
  - 使用 Composables（useAuth、useDict、usePayment、useHttp 等）
  - 跨页面数据共享（移动端）
  - 持久化存储（uni.storage）
  - HTTP 请求与链式调用
  - 认证与权限检查
  - 滚动管理与返回顶部

  触发词：移动端Store、useAuth、useDict、usePayment、useDictStore、移动端状态管理、Composable、useHttp、http请求、链式调用、useScroll、滚动管理、useAppInit、应用初始化、cache、缓存、持久化、useToken

  适用目录：plus-uniapp/**
---
# store-mobile

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
- 核心概念
- 自动导入说明
- 已有 Store 清单
- 已有 Composables 清单
- HTTP 请求（重点）
- 应用初始化（useAppInit）
- 认证与权限
- 字典使用（重点）
- 滚动管理（useScroll）
- 支付功能（usePayment）
- 分享配置
- 创建新 Store
- 创建新 Composable
- 持久化存储
- 常用模式
- 最佳实践
- 参考文件
