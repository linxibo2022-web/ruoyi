---
name: ui-mobile
description: |
  当需要开发 plus-uniapp 或 plus-app 移动端页面与 WD UI 组件时使用。

  触发场景：
  - 需要开发小程序、H5 或 APP 页面
  - 需要使用 wd-form、wd-paging 等 WD UI 组件
  - 需要处理移动端分页、表单或平台适配

  触发词：移动端、plus-uniapp、plus-app、WD UI、wd-paging、小程序、H5、APP页面
---

# 移动端开发入口

1. 先阅读 Home、登录页和对应 API；使用项目现有的状态与请求模式。
2. 必须使用 `wd-*` 组件，禁止 `uni-*` UI 组件；平台差异按需加载 `uniapp-platform`。
3. 分页、表单、composables、组件 API 与常见误用按需读取 `references/full-guide.md`。
4. 修改后执行受影响端的类型检查或最小构建验证。
