---
name: app-adapter
description: |
  当需要为 plus-app（原生 APP 项目）开发页面、组件、API 时自动使用此 Skill。提供 plus-app 与 plus-uniapp 的差异适配。

  触发场景：
  - 用户明确提到 plus-app 或 APP 端开发
  - 需要使用原生插件（nativeplugins）
  - 需要 HBuilderX 构建或真机调试
  - 需要鸿蒙 APP 适配
  - 需要 APP 专属配置（地图、客服、分享域名）

  触发词：plus-app、APP端、原生APP、原生插件、HBuilderX、鸿蒙APP、harmony、nativeplugins、APP打包、真机调试、APP专属、APP配置
---
# app-adapter

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
- 核心差异：目录结构映射
- 参考代码位置（plus-app）
- plus-app 独有能力
- 构建与调试
- 条件编译注意
- 依赖差异
- 通用规范（两端完全相同）
- 开发流程（plus-app）
- 常见错误
- 何时使用 plus-app vs plus-uniapp
- 🔗 关联技能边界
