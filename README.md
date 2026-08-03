# ruoyi-plus-uniapp 后端框架

## 文档地址 https://ruoyi.plus/backend/

#### Claude Code 跳过权限检查启动
claude --dangerously-skip-permissions

## 运行

```text
1. 开发项目前学会idea操作git管理等技巧 可参考视频: https://www.bilibili.com/video/BV1RM411o7kN

2. 使用idea打开项目根目录 等待安装maven依赖 如未安装右上角刷新依赖

3. 点击菜单file -> Project Structure -> Project Setting -> Project -> SDK设置为 >= jdk21

4. 如果是开发新项目, 则应为新项目制定一个唯一标识符, 最后是不超过8个长度的英文字母下划线组合如mall, 全局搜索erp_sys并全部替换为新的唯一标识符,
标识符与数据库名称,redis前缀,前端缓存前缀,反向代理地址紧密相关,因为标识符需要唯一且符合项目含义(如果只是学习项目则可以跳过此步骤) 
同时全局搜索端口5500 注意勾选W整词搜索 替换为一个唯一的端口口(不同项目使用不同的端口号)

5. 执行根目录/script下sql语句 sys为系统表 job为定时任务表 app为移动端表 根据需求安装 定时任务数据不建议与主数据库同一个库名

6. 右上角启动RuoyiPlus 如果未存在则在ruoyi-admin/src/main/java/plus/ruoyi下打开RuoyiPlus并启动 在ruoyi-extend下可启动定时任务和admin监控

```

### 业务开发推荐的目录层级结构

```text
ruoyi-plus-uniapp (项目根目录)
├── ruoyi-admin                  // 系统入口模块，打包部署的主模块
├── ruoyi-common                 // 通用工具模块，提供各种基础功能支持
│   ├── ruoyi-common-core        // 核心工具模块
│   ├── ruoyi-common-pay         // 支付服务模块
│   └── ...                      // 其他通用功能模块
├── ruoyi-extend                 // 扩展增强模块
│   ├── ruoyi-monitor-admin      // 监控管理模块
│   └── ruoyi-xxl-job-admin      // 任务调度中心模块
├── ruoyi-modules                // 业务功能模块
│   ├── ruoyi-generator          // 代码生成模块
│   ├── ruoyi-system             // 系统管理模块
│   └── ruoyi-business           // 业务功能模块
│       ├── src
│       │   └── main
│       │       ├── java
│       │       │   └── plus.ruoyi.business
│       │       │       ├── api               // API接口层
│       │       │       │   ├── mobile        // 移动端API
│       │       │       │   ├── pay           // 支付相关API
│       │       │       │   └── pc            // PC端API
│       │       │       ├── base              // 基础业务服务
│       │       │       │   ├── authStrategy  // 认证策略
│       │       │       │   ├── controller    // 基础业务控制器
│       │       │       │   ├── domain        // 基础业务领域模型
│       │       │       │   ├── mapper        // 基础业务数据访问
│       │       │       │   └── service       // 基础业务服务
│       │       │       └── job               // 任务调度模块
│       │       │       └── mall              // 商城领域
│       │       │           ├── controller    // 商城控制器
│       │       │           ├── domain        // 商城领域模型
│       │       │           ├── listener      // 商城事件监听器
│       │       │           ├── mapper        // 商城数据访问层
│       │       │           └── service       // 商城服务层
│       │       └── resources
│       │           └── mapper                // MyBatis XML映射文件
│       │               ├── base              // 基础业务映射
│       │               └── mall              // 商城相关映射
├── plus-ui                     // 前端管理框架
│   ├── bin                     // 构建脚本
│   ├── dist                    // 构建输出目录
│   ├── env                     // 环境配置
│   ├── node_modules            // 依赖包
│   ├── public                  // 静态资源
│   ├── src                     // 源代码目录
│   │   ├── api                 // API接口定义
│   │   │   ├── business        // 业务相关API
│   │   │   ├── system          // 系统相关API
│   │   │   └── tool            // 工具相关API
│   │   ├── assets              // 静态资源
│   │   │   ├── images          // 图片资源
│   │   │   ├── logo            // Logo资源
│   │   │   └── styles          // 样式文件
│   │   ├── components          // 通用组件
│   │   ├── composables         // 组合式函数
│   │   ├── directives          // 自定义指令
│   │   ├── layouts             // 布局组件
│   │   ├── locales             // 国际化文件
│   │   ├── plugins             // 插件配置
│   │   ├── router              // 路由配置
│   │   ├── stores              // 状态管理
│   │   ├── types               // TypeScript类型定义
│   │   ├── utils               // 工具类
│   │   ├── views               // 页面视图
│   │   │   ├── business        // 业务页面
│   │   │   ├── common          // 通用页面
│   │   │   ├── system          // 系统页面
│   │   │   └── tool            // 工具页面
│   │   ├── App.vue             // 根组件
│   │   ├── main.ts             // 入口文件
│   │   └── systemConfig.ts     // 系统配置
│   └── vite                    // Vite配置
│       └── plugins             // Vite插件
└── plus-uniapp                 // 小程序前端框架
│   ├── dist                    // 构建输出目录
│   ├── env                     // 环境配置
│   ├── node_modules            // 依赖包
│   ├── src                     // 源代码目录
│   │   ├── api                 // API接口定义
│   │   │   ├── business        // 业务相关API
│   │   │   └── system          // 系统相关API
│   │   ├── components          // 通用组件
│   │   ├── composables         // 组合式函数
│   │   ├── layouts             // 布局组件
│   │   ├── pages               // 页面目录
│   │   │   ├── ...             // 其他业务页面目录
│   │   │   ├── auth            // 认证相关页面
│   │   │   └── tabbar          // 底部导航页面
│   │   ├── static              // 静态资源
│   │   ├── stores              // 状态管理
│   │   │   ├── modules         // 状态模块
│   │   │   └── store.ts        // 状态配置
│   │   ├── subpages            // 子页面
│   │   │   ├── admin           // 管理员页面
│   │   │   └── demo            // 演示页面
│   │   ├── types               // TypeScript类型定义
│   │   ├── uni_modules         // uni-app模块
│   │   ├── utils               // 工具类
│   │   ├── wd                  // 通用组件
│   │   ├── App.vue             // 根组件
│   │   ├── main.ts             // 入口文件
│   │   ├── manifest.json       // 应用配置
│   │   ├── pages.json          // 页面配置
│   │   ├── systemConfig.ts     // 系统配置
│   │   └── uni.scss            // 全局样式
│   ├── vite                    // Vite配置
│   │   └── plugins             // Vite插件
```

## 框架禁止外传 非授权不得使用 授权联系微信/QQ: 770492966(抓蛙师)
