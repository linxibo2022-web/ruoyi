
# 图标管理指南

> 本项目 PC 端和移动端使用**两套独立的图标体系**，需分别管理。

---

## 1. 两端图标体系总览

| 维度 | PC 端 (plus-ui) | 移动端 (plus-uniapp / plus-app) |
|------|-----------------|-------------------------------|
| **图标总数** | 817 个（iconfont 644 + iconify 173） | 397 个字体图标 |
| **iconfont 项目** | `5022572` | `font_4969054` |
| **字体加载** | 本地文件 | CDN（默认）+ 本地 TTF（APP） |
| **类型定义** | Vite 插件自动生成 `icons.d.ts` | 组件内硬编码 `FontIconName` |
| **扩展能力** | Iconify（`preset.json`） | UnoCSS 图标（`i-` 前缀） |
| **图标选择器** | `IconSelect` 组件（支持搜索） | 无选择器 |

### 关键文件位置

| 文件 | 路径 |
|------|------|
| PC 字体文件 | `plus-ui/src/assets/icons/system/iconfont.*` |
| PC 类型定义 | `plus-ui/src/types/icons.d.ts`（自动生成） |
| PC Iconify 预设 | `plus-ui/src/assets/icons/iconify/preset.json` |
| PC 图标组件 | `plus-ui/src/components/Icon/Icon.vue` |
| PC 图标选择器 | `plus-ui/src/components/Icon/IconSelect.vue` |
| 移动端字体文件 | `plus-uniapp/src/wd/components/wd-icon/wd-icons.ttf` |
| 移动端图标组件 | `plus-uniapp/src/wd/components/wd-icon/wd-icon.vue` |
| plus-app 字体文件 | `plus-app/wd/components/wd-icon/wd-icons.ttf` |

---

## 2. 菜单图标选择（最常用场景）

### 2.1 菜单 SQL 中图标字段说明

```sql
-- sys_menu 表 icon 字段存储 PC 端图标代码字符串
-- 默认值 '#' 表示无图标
INSERT INTO sys_menu VALUES (..., 'icon_code', ...);
```

### 2.2 关键词→图标快速映射表

> 生成菜单 SQL 时，根据功能名称选择合适的图标。

| 功能关键词 | 推荐图标 | 备选图标 |
|-----------|---------|---------|
| **用户/会员/人员** | `user` | `user2`, `user3`, `member`, `my` |
| **角色/权限** | `role` | `permission`, `security` |
| **菜单** | `menu` | `nested`, `list` |
| **部门/组织** | `department` | `company`, `team` |
| **字典/配置** | `dict` | `setting`, `settings` |
| **通知/公告** | `notification` | `announcement`, `reminder` |
| **日志/记录** | `log` | `record`, `history` |
| **监控/运维** | `monitor` | `dashboard`, `data-analysis` |
| **商品/产品** | `goods` | `product`, `shopping` |
| **订单/交易** | `order` | `order-list`, `complete-order` |
| **商城/店铺** | `store` | `shop-window`, `shopping-cart` |
| **优惠券/营销** | `coupon` | `discount`, `red-envelope` |
| **支付/钱包** | `payment` | `wallet`, `money`, `wxpay` |
| **文章/内容** | `article` | `document`, `documentation` |
| **消息/聊天** | `message` | `chat`, `comment`, `sms` |
| **邮件** | `email` | `mail` |
| **文件/附件** | `file` | `folder`, `paperclip-attachment` |
| **图片/相册** | `image` | `photo`, `photo-album`, `picture` |
| **视频/直播** | `video` | `live`, `movie`, `film` |
| **设置/系统** | `setting` | `tool`, `settings` |
| **地图/定位** | `location` | `map`, `position`, `navigation` |
| **日历/时间** | `calendar` | `date`, `time`, `schedule` |
| **统计/报表** | `statistics` | `chart`, `line-chart`, `pie-chart` |
| **代码/开发** | `code` | `terminal`, `bug`, `debug` |
| **API/接口** | `api` | `link`, `server` |
| **数据库** | `database` | `data`, `server` |
| **安全/认证** | `security` | `lock`, `key`, `password` |
| **客服/服务** | `service` | `customer-service`, `consultation` |
| **广告/推广** | `ad` | `megaphone`（iconify） |
| **反馈/评价** | `feedback` | `review`, `rate`, `comment` |
| **物流/配送** | `delivery` | `shipping`, `courier` |
| **礼物/活动** | `gift` | `activity`, `trophy` |
| **工单/任务** | `my-task` | `todo`, `plan-work` |
| **会员/VIP** | `vip` | `crown`, `diamond` |
| **医疗/健康** | `health` | `medical`, `medicine` |
| **教育/学校** | `education` | `school`, `graduation` |
| **物联网/设备** | `device` | `machine`, `robot` |
| **小程序/APP** | `miniapp` | `app`, `mobile` |
| **微信** | `wechat` | `wechat-fill` |
| **二维码** | `qrcode` | `scan`, `scan-code` |

### 2.3 菜单类型与图标规则

| 菜单类型 | menu_type | 图标规则 |
|---------|-----------|---------|
| **一级目录** | `M` | 必须设置图标（如 `'setting'`） |
| **二级菜单** | `C` | 推荐设置图标（如 `'user'`） |
| **按钮权限** | `F` | 图标固定为 `'#'` |

### 2.4 菜单 SQL 图标用法示例

```sql
-- 一级目录：必须设置图标
INSERT INTO sys_menu VALUES (3000, '商城管理', 0, 100, 'mallManage', NULL, NULL,
  '0', '1', 'M', '1', '1', NULL, 'store', 103, 1, sysdate(), NULL, NULL, '商城管理目录');
--                                                   ^^^^^ 图标

-- 二级菜单：推荐设置图标
INSERT INTO sys_menu VALUES (3010, '商品管理', 3000, 10, 'goods',
  'business/mall/goods/goods', NULL, '0', '1', 'C', '1', '1',
  'mall:goods:view', 'goods', 103, 1, sysdate(), NULL, NULL, '商品管理菜单');
--                   ^^^^^ 图标

-- 按钮权限：图标固定 '#'
INSERT INTO sys_menu VALUES (3011, '商品查询', 3010, 1, '#', '', NULL,
  '0', '1', 'F', '1', '1', 'mall:goods:query', '#', 103, 1, sysdate(), NULL, NULL, '');
--                                               ^ 按钮无图标
```

---

## 3. PC 端图标分类索引

> 以下为 PC 端（plus-ui）817 个图标中按业务场景精选的常用图标。

### 3.1 系统管理类

| 图标代码 | 中文名 | 适用场景 |
|---------|--------|---------|
| `setting` | 设置 | 系统设置、配置管理 |
| `tool` | 工具 | 工具箱、系统工具 |
| `monitor` | 监控 | 系统监控、服务监控 |
| `dashboard` | 仪表盘 | 控制台、数据看板 |
| `security` | 安全 | 安全中心、安全设置 |
| `lock` | 锁定 | 权限、密码 |
| `key` | 钥匙 | 密钥管理 |
| `server` | 服务器 | 服务管理 |
| `database` | 数据库 | 数据管理 |
| `code` | 代码 | 代码生成 |
| `terminal` | 终端 | 命令行 |
| `bug` | Bug | 缺陷管理 |
| `log` | 日志 | 操作日志、系统日志 |
| `online` | 在线 | 在线用户 |
| `redis` | Redis | 缓存监控 |

### 3.2 用户与权限类

| 图标代码 | 中文名 | 适用场景 |
|---------|--------|---------|
| `user` | 用户 | 用户管理 |
| `users` | 多用户 | 用户列表 |
| `team` | 团队 | 团队管理 |
| `role` | 角色 | 角色管理 |
| `department` | 部门 | 部门管理 |
| `company` | 公司 | 企业信息 |
| `admin` | 管理员 | 管理员 |
| `permission` | 权限 | 权限管理 |
| `member` | 会员 | 会员管理 |
| `vip` | VIP | VIP 管理 |
| `crown` | 皇冠 | 等级、特权 |

### 3.3 商城与交易类

| 图标代码 | 中文名 | 适用场景 |
|---------|--------|---------|
| `store` | 店铺 | 商城、店铺管理 |
| `goods` | 商品 | 商品管理 |
| `shopping-cart` | 购物车 | 购物车 |
| `order` | 订单 | 订单管理 |
| `payment` | 支付 | 支付管理 |
| `wallet` | 钱包 | 钱包、余额 |
| `money` | 金钱 | 财务、金额 |
| `coupon` | 优惠券 | 优惠券管理 |
| `discount` | 折扣 | 促销、折扣 |
| `gift` | 礼物 | 赠品、礼品 |
| `delivery` | 配送 | 物流、快递 |
| `refund` | 退款 | 退款管理 |
| `invoice` | 发票 | 发票管理 |

### 3.4 内容与信息类

| 图标代码 | 中文名 | 适用场景 |
|---------|--------|---------|
| `article` | 文章 | 文章管理、CMS |
| `document` | 文档 | 文档管理 |
| `notification` | 通知 | 通知公告 |
| `announcement` | 公告 | 系统公告 |
| `message` | 消息 | 站内消息 |
| `comment` | 评论 | 评论管理 |
| `feedback` | 反馈 | 用户反馈 |
| `dict` | 字典 | 数据字典 |
| `form` | 表单 | 表单管理 |
| `table` | 表格 | 数据表 |

### 3.5 文件与媒体类

| 图标代码 | 中文名 | 适用场景 |
|---------|--------|---------|
| `file` | 文件 | 文件管理 |
| `folder` | 文件夹 | 目录管理 |
| `image` | 图片 | 图片管理 |
| `video` | 视频 | 视频管理 |
| `upload` | 上传 | 文件上传 |
| `download` | 下载 | 文件下载 |
| `excel` | Excel | Excel 导入导出 |
| `pdf` | PDF | PDF 文件 |
| `word` | Word | Word 文件 |
| `qrcode` | 二维码 | 二维码管理 |

### 3.6 数据与统计类

| 图标代码 | 中文名 | 适用场景 |
|---------|--------|---------|
| `statistics` | 统计 | 数据统计 |
| `chart` | 图表 | 统计图表 |
| `line-chart` | 折线图 | 趋势分析 |
| `pie-chart` | 饼图 | 占比分析 |
| `data-analysis` | 数据分析 | 数据分析 |
| `trend` | 趋势 | 趋势分析 |
| `report` | 报告 | 报表 |

### 3.7 社交与平台类

| 图标代码 | 中文名 | 适用场景 |
|---------|--------|---------|
| `wechat` | 微信 | 微信相关 |
| `wxpay` | 微信支付 | 微信支付 |
| `qq` | QQ | QQ 相关 |
| `github` | GitHub | GitHub 集成 |
| `gitee` | Gitee | Gitee 集成 |
| `miniapp` | 小程序 | 小程序管理 |

### 3.8 业务场景类

| 图标代码 | 中文名 | 适用场景 |
|---------|--------|---------|
| `calendar` | 日历 | 日程、预约 |
| `location` | 定位 | 地图、位置 |
| `service` | 客服 | 客服中心 |
| `scan` | 扫码 | 扫码功能 |
| `education` | 教育 | 培训、课程 |
| `medical` | 医疗 | 医疗健康 |
| `device` | 设备 | 设备管理 |
| `robot` | 机器人 | AI、智能助手 |
| `workflow` | 工作流 | 流程管理 |

---

## 4. 移动端图标分类索引

> 移动端（plus-uniapp / plus-app）共 397 个图标，分为 10 大类。使用时加 `-fill` 后缀可获取填充版本。

### 4.1 系统操作类（70 个）

常用：`home`, `back`, `close`, `check`, `menu`, `search`, `setting`, `add`, `edit`, `delete`, `copy`, `save`, `upload`, `download`, `share`, `link`, `scan`, `refresh`, `login`, `logout`

### 4.2 用户相关类（27 个）

常用：`user`, `user-add`, `team`, `contact`, `admin`, `vip`, `user-group`, `male`, `female`, `crown`

### 4.3 商业功能类（36 个）

常用：`cart`, `payment`, `order`, `coupon`, `gift`, `wallet`, `card`, `shop`, `goods`, `money`, `discount`, `qrcode`, `bag`, `delivery`, `company`

### 4.4 通讯媒体类（45 个）

常用：`call`, `phone`, `message`, `mail`, `chat`, `video`, `camera`, `image`, `mobile`, `wifi`, `cloud`, `internet`

### 4.5 文件管理类（26 个）

常用：`file`, `folder`, `file-word`, `file-excel`, `file-pdf`, `attach`, `book`

### 4.6 工具功能类（32 个）

常用：`calendar`, `location`, `map`, `tools`, `laptop`, `app`, `history`, `service`, `keyboard`

### 4.7 状态指示类（39 个）

常用：`warn`, `info`, `loading`, `locked`, `visible`, `time`, `star`, `heart`, `notification`, `pin`

### 4.8 数据图表类（16 个）

常用：`chart`, `chart-bar`, `trending-up`, `data`

### 4.9 社交功能类（22 个）

常用：`comment`, `reply`, `emoji`, `moments`, `follow`, `group-chat`

### 4.10 平台品牌类（21 个）

常用：`wechat`, `qq`, `weibo`, `alipay`, `github`, `apple`, `android`, `tiktok`

---

## 5. 两端图标名称对照

> 同一功能在 PC 端和移动端可能使用不同的图标名称。

| 功能 | PC 端图标 | 移动端图标 | 备注 |
|------|----------|----------|------|
| 用户 | `user` | `user` | 相同 |
| 设置 | `setting` | `setting` | 相同 |
| 首页 | `home` | `home` | 相同 |
| 搜索 | `search` | `search` | 相同 |
| 消息 | `message` | `message` | 相同 |
| 订单 | `order` | `order` | 相同 |
| 购物车 | `shopping-cart` | `cart` | **不同** |
| 商品 | `goods` | `goods` | 相同 |
| 店铺 | `store` | `shop` | **不同** |
| 钱包 | `wallet` | `wallet` | 相同 |
| 删除 | `delete` | `delete` | 相同 |
| 编辑 | `edit` | `edit` | 相同 |
| 日历 | `calendar` | `calendar` | 相同 |
| 位置 | `location` | `location` | 相同 |
| 收藏 | `collect` | `favorite` | **不同** |
| 客服 | `customer-service` | `service` | **不同** |

---

## 6. 添加/更换图标操作指南

### 6.1 PC 端添加新图标

#### 方案 A：更新 iconfont 图标库（推荐）

1. **登录 iconfont.cn**
   - 项目地址：`https://www.iconfont.cn/manage/index?manage_type=myprojects&projectId=5022572`

2. **搜索并添加图标**
   - 在 iconfont 图标库搜索需要的图标
   - 添加到项目 `5022572`

3. **下载字体包**
   - 在项目中点击「下载至本地」
   - 获得包含 `iconfont.css`, `.woff2`, `.woff`, `.ttf`, `.json` 的压缩包

4. **替换本地文件**
   ```
   plus-ui/src/assets/icons/system/
   ├── iconfont.css      ← 替换
   ├── iconfont.js       ← 替换
   ├── iconfont.json     ← 替换
   ├── iconfont.ttf      ← 替换
   ├── iconfont.woff     ← 替换
   └── iconfont.woff2    ← 替换
   ```

5. **重新构建**
   - Vite 插件 `iconfont-types` 会自动重新生成 `icons.d.ts`
   - 无需手动更新类型定义

#### 方案 B：添加 Iconify 图标（无需更新字体）

1. 编辑 `plus-ui/src/assets/icons/iconify/preset.json`
2. 添加新图标配置：
   ```json
   {
     "code": "my-icon",
     "name": "我的图标",
     "value": "i-mdi:some-icon"
   }
   ```
3. 重新构建，类型自动生成

### 6.2 移动端添加新图标

#### 方案 A：更新在线字体库（推荐）

1. **登录 iconfont.cn**
   - 项目地址：`https://www.iconfont.cn/manage/index?manage_type=myprojects&projectId=4969054`

2. **搜索并添加图标**
   - 添加到项目 `font_4969054`

3. **更新 CDN 链接**
   - 在 iconfont 项目中获取新的在线链接
   - 修改 `plus-uniapp/src/wd/components/wd-icon/wd-icon.vue`
   - 更新 `@font-face` 中的 URL（约第 547-549 行的 `t=` 时间戳参数）

4. **更新本地 TTF**（APP 环境必须）
   - 从 iconfont 下载 `.ttf` 文件
   - 替换到两个位置：
     ```
     plus-uniapp/src/wd/components/wd-icon/wd-icons.ttf   ← 替换
     plus-app/wd/components/wd-icon/wd-icons.ttf           ← 替换
     ```

5. **更新类型定义**（手动）
   - 在 `wd-icon.vue` 的 `FontIconName` 类型中添加新图标名称（约第 24-420 行）
   - 添加对应 CSS 类：`.wd-icon-{name}:before { content: '\e{xxx}'; }`

#### 方案 B：使用 UnoCSS 图标（无需更新字体）

```vue
<!-- 直接使用 UnoCSS 图标，无需更新字体文件 -->
<wd-icon name="i-carbon:user-avatar" />
<wd-icon name="i-mdi:account-circle" />
```

> UnoCSS 图标在编译时自动处理，但小程序环境**不支持**，仅 H5 和 APP 可用。

#### 方案 C：使用图片图标

```vue
<!-- 适用于所有平台，但增加包体积 -->
<wd-icon name="/static/icons/my-icon.png" />
```

### 6.3 注意事项

| 注意事项 | 说明 |
|---------|------|
| **PC 和移动端不同源** | 添加图标需要分别操作两个 iconfont 项目 |
| **移动端需更新两处** | plus-uniapp 和 plus-app 的 TTF 文件都要替换 |
| **移动端类型需手动更新** | PC 端有 Vite 插件自动生成，移动端需手动维护 |
| **CDN 缓存** | 更新移动端 CDN 链接后，注意浏览器/小程序缓存 |
| **图标命名** | 新图标命名使用 kebab-case（如 `my-new-icon`） |

---

## 7. 与 /dev、/crud 联动

### 7.1 /dev 或 /crud 生成菜单时

生成菜单 SQL 时，按以下步骤选择图标：

1. **根据功能名称**查阅本文档第 2.2 节「关键词→图标映射表」
2. **选择匹配图标**填入菜单 SQL 的 icon 字段
3. **一级目录**必须有图标，**二级菜单**推荐有图标，**按钮权限**固定 `'#'`

### 7.2 移动端页面开发时

使用 `wd-icon` 组件时，参考本文档第 4 节「移动端图标分类索引」选择图标：

```vue
<wd-icon name="order" size="48rpx" color="#1989fa" />
```

### 7.3 PC 端页面开发时

使用 `Icon` 组件时，参考本文档第 3 节「PC 端图标分类索引」：

```vue
<Icon code="order" size="lg" />
```

---

## 8. 常见问题

### Q: 怎么知道某个图标名是否存在？

- **PC 端**：查看 `plus-ui/src/types/icons.d.ts`，搜索图标名
- **移动端**：查看 `plus-uniapp/src/wd/components/wd-icon/wd-icon.vue` 第 24-420 行
- **在线预览**：PC 端启动后访问「系统工具 → 图标」页面

### Q: PC 端图标和移动端图标能通用吗？

不能直接通用。两端使用不同的 iconfont 项目（5022572 vs 4969054），图标集合不同。
但**部分图标名称相同**（如 `user`, `setting`, `home`），参见第 5 节对照表。

### Q: 菜单图标在移动端怎么显示？

菜单图标（sys_menu.icon）主要用于 PC 端后台管理界面。移动端页面有自己的图标体系。
如果移动端需要显示菜单图标，需要做名称映射。

### Q: 添加图标后不生效怎么办？

1. **PC 端**：检查是否重新构建（Vite 插件需要重新扫描）
2. **移动端**：检查 CDN URL 的时间戳是否更新、TTF 文件是否替换、类型定义是否添加
3. **清除缓存**：浏览器缓存、微信开发者工具缓存、APP 缓存
