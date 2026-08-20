---
name: env-config
description: |
  当需要配置多环境（开发/测试/生产）、修改 application-*.yml 或 .env.* 文件、切换后端 profile、设置前端/移动端环境变量时自动使用此 Skill。

  触发场景：
  - 配置后端 Spring Boot 多 profile（application.yml / application-dev.yml / application-prod.yml）
  - 修改前端 .env.development / .env.production
  - 修改移动端（plus-uniapp / plus-app）.env 配置
  - 切换开发/生产环境的 API 地址、端口、密钥
  - 处理 `${ENV_VAR:default}` 占位符与外部环境变量
  - 排查"配置在哪改"、"为什么生产环境读取到开发值"

  触发词：环境配置、profile、application.yml、application-dev.yml、application-prod.yml、.env、.env.development、.env.production、VITE_APP_、多环境、环境变量、SPRING_PROFILES_ACTIVE、开发环境、生产环境、测试环境、配置切换、env文件
---
# env-config

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
- 后端：Spring Boot Profile
- 前端（plus-ui）
- 移动端（plus-uniapp / plus-app 通用）
- 标识符规划（多项目必读）
- 取值类型规范：环境变量值统一用字符串
- ✅ 正确做法
- ❌ 常见错误
- 常用排查
- 🔗 关联技能边界
