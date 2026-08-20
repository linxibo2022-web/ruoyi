---
name: ui-pc
description: |
  当需要开发或改造 plus-ui PC 页面与封装组件时使用。

  触发场景：
  - 需要开发后台列表、表单或详情页面
  - 需要使用 AForm、AModal 等项目封装组件
  - 需要处理 PC 端字典、导出或交互状态

  触发词：PC页面、plus-ui、AForm、AModal、列表页、表单页、后台管理、Element Plus
---

# PC 前端开发入口

1. 先阅读广告模块页面与 API，复制现有布局、类型定义和 `[err, data]` 调用风格。
2. 只能使用项目 A* 封装组件和既有 composables；禁止直接使用 `el-*` 或 `ElMessage`。
3. 按场景读取 `references/full-guide.md`：删除、提交、状态切换、详情、导出和组件清单均在其中。
4. 修改后执行受影响前端的类型检查或构建验证。

详细组件 API、示例和常见误用只在需要时读取 `references/full-guide.md`。
