---
name: collaborating-with-codex
description: |
  与 OpenAI Codex CLI 协同开发。优先用官方插件 codex-plugin-cc 的斜杠命令；未安装时回退到框架内置 Python 桥接脚本，并提示用户安装。

  触发场景：
  - 需要算法实现或复杂逻辑分析
  - 需要代码审查和 Bug 分析
  - 需要生成 Unified Diff 补丁
  - 用户明确要求使用 Codex 协作
  - 复杂后端逻辑的原型设计
  - 需要安装、了解或使用 codex-plugin-cc 官方插件

  触发词：Codex、协作、多模型、原型、Diff、算法分析、代码审查、codex协同、codex-plugin-cc、codex插件、官方插件、codex review、codex rescue、adversarial-review、review-gate

  前置要求：
  - 已安装 Codex CLI (npm install -g @openai/codex)
  - 已配置 OpenAI API Key（或 ChatGPT 订阅登录）
---
# collaborating-with-codex

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
- 🔴 启动探测（每次激活时执行，强制）
- 📦 安装提示模板（仅 NOT_INSTALLED 时，且每会话最多一次）
- 🔀 两种调用方式对照
- 安装
- 常用命令
- 快速开始
- 参数说明
- 使用模式
- 返回值结构
- 协作工作流
- 与本项目的集成
- 安装前置
- 故障排除
