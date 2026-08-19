---
name: i18n-development
description: |
  国际化开发技能，涵盖后端 MessageSource、前端 Vue i18n、移动端 useI18n 完整实现。

  触发场景：
  - 后端国际化配置（MessageSource、property files）
  - PC 端多语言切换（Vue i18n）
  - 移动端国际化（UniApp i18n）
  - 动态语言切换
  - 语言包管理
  - 多语言字段翻译

  触发词：国际化、多语言、i18n、翻译、t()、语言切换、MessageUtils、content-language、LanguageCode、useI18n、messages.properties、locale、$t、zh_CN、en_US
---
# i18n-development

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
- 框架对应模块
- 1. 后端国际化（Spring Boot MessageSource）
- 2. PC 前端国际化（Vue i18n）
- 3. 移动端国际化（UniApp + useI18n Composable）
- 4. 完整使用示例
- 5. 新增语言支持
- 6. 开发检查清单
- 7. 常见错误
- 8. 最佳实践
- 9. FAQ
- 10. 参考代码位置
