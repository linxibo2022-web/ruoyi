---
name: collaborating-with-gemini
description: |
  与 Google Gemini CLI 协同开发。将编码任务委托给 Gemini 进行前端原型、UI设计和代码审查。

  触发场景：
  - 需要前端/UI/样式原型设计
  - 需要 CSS/React/Vue 组件设计
  - 需要代码审查和 Bug 分析
  - 用户明确要求使用 Gemini 协作
  - 复杂前端逻辑的原型设计

  触发词：Gemini、协作、多模型、前端原型、UI设计、CSS、样式、gemini协同

  前置要求：
  - 已安装 Gemini CLI (npm install -g @anthropic/gemini-cli)
  - 已配置 Google API Key

  注意：Gemini 对后端逻辑理解有缺陷，后端任务优先使用 Codex。
---
# collaborating-with-gemini

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
- 快速开始
- 参数说明
- 使用模式
- 返回值结构
- 协作工作流
- 与本项目的集成
- 安装前置
- 故障排除
- Gemini vs Codex 选择指南
