# 项目说明

> 本目录是基于 ruoyi-plus-uniapp 框架的交付副本，自动从主项目同步生成。

## 项目结构

```
.
├── ruoyi-admin/      # 后端启动模块
├── ruoyi-modules/    # 后端业务模块（base / mall / crm / iot 等）
├── ruoyi-common/     # 后端公共模块（35+ 子模块）
├── plus-ui/          # PC 后台前端（Vue 3 + Element Plus）
├── plus-uniapp/      # 移动端（小程序/H5/CLI 构建 APP）
├── plus-app/         # 移动端（HBuilderX 构建原生 APP / 鸿蒙）
├── script/           # 数据库脚本
├── pom.xml           # 后端 Maven 构建
├── package.json      # 前端工作区
├── README.md         # 本文件
└── DEPLOY.md         # 部署文档（含启动说明）
```

## 技术栈

- **后端**：Spring Boot 3.5.8、MyBatis-Plus、Sa-Token、Redis、RocketMQ
- **PC 前端**：Vue 3、Element Plus、Pinia、Vite
- **移动端**：UniApp + Vue 3、WD UI、Pinia

## 快速启动

详见 [DEPLOY.md](./DEPLOY.md)。

## 二次开发

如需基于本目录二次开发，建议：

1. `git init` 建立你自己的仓库
2. 配置 `application-dev.yml` 和 `.env.development` 中的数据库/Redis/上传路径等
3. 启动后端、前端、移动端验证基础功能

## 版本来源

本目录由主项目自动同步生成。具体来源 commit 见 `.delivery-sync-marker` 文件（隐藏，可 `cat` 查看）。
