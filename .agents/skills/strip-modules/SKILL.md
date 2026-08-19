---
name: strip-modules
description: |
  当用户用 `/strip-modules` 命令或自然语言要求"在交付目录里删除商城/IoT/支付/AI 等业务模块"时使用此 Skill。是 sync-delivery 的下游补充，处理删目录 + 改 pom.xml 的语义级裁剪。

  触发场景：
  - 用户输入 `/strip-modules`、`/strip-modules mall`、`/strip-modules --list` 等
  - 用户说"裁掉商城/IoT/支付/AI 模块"
  - 用户说"列出可裁剪的模块"
  - 需要在裁剪后跑 mvn compile 验证

  触发词：/strip-modules、strip-modules、模块裁剪、裁掉商城、裁掉支付、裁掉AI、裁掉IoT、删除模块、stripModules
---
# strip-modules

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
- 触发方式
- 执行流程
- 推荐工作流
- 详细规范
- 关联命令
