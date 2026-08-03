---
name: uniapp-platform
description: |
  当需要进行多端开发、条件编译、平台适配时自动使用此 Skill。涵盖小程序、H5、APP 的差异化开发。

  触发场景：
  - 条件编译使用
  - 平台特性判断
  - 跨平台代码适配
  - 原生能力调用

  触发词：条件编译、ifdef、平台判断、isMpWeixin、isH5、isApp、跨平台、小程序、公众号H5、原生能力、多端
---

# 多端平台开发指南

## 平台识别体系

### 支持的平台

| 平台 | 常量标识 | 说明 |
|------|---------|------|
| 微信小程序 | `isMpWeixin` | 微信原生小程序 |
| 支付宝小程序 | `isMpAlipay` | 支付宝原生小程序 |
| 抖音小程序 | `isMpToutiao` | 抖音原生小程序 |
| 微信公众号H5 | `isWechatOfficialH5` | 微信内置浏览器 |
| 支付宝H5 | `isAlipayOfficialH5` | 支付宝内置浏览器 |
| 普通H5 | `isH5` | 普通浏览器 |
| APP | `isApp` | 原生APP |

### 平台能力矩阵

| 功能 | 微信小程序 | 支付宝小程序 | 抖音小程序 | 微信公众号H5 | 普通H5 | APP |
|------|:--------:|:----------:|:--------:|:----------:|:-----:|:---:|
| **支付** |
| 微信支付 | ✅ | ❌ | ❌ | ✅ | ⚠️跳转 | ✅ |
| 支付宝支付 | ❌ | ✅ | ❌ | ❌ | ✅ | ✅ |
| **登录授权** |
| 手机号一键授权 | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ |
| 微信登录 | ✅ | ❌ | ❌ | ✅ | ❌ | ✅ |
| 支付宝登录 | ❌ | ✅ | ❌ | ❌ | ❌ | ✅ |
| **分享** |
| 分享到好友 | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ |
| 分享到朋友圈 | ✅ | ❌ | ❌ | ✅ | ❌ | ✅ |
| **其他能力** |
| 扫码 | ✅ | ✅ | ✅ | ✅ | ⚠️部分 | ✅ |
| 定位 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 蓝牙 | ✅ | ✅ | ⚠️受限 | ❌ | ❌ | ✅ |
| NFC | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ |
| 订阅消息 | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ |

> **图例**：✅ 支持 | ❌ 不支持 | ⚠️ 受限/需特殊处理

---

## 平台判断工具

### 导入与使用

```typescript
// ✅ 推荐：导入具体常量
import {
  platform,
  isApp,
  isMp,
  isMpWeixin,
  isMpAlipay,
  isH5,
  isWechatOfficialH5,
  isAlipayOfficialH5,
  isWechatEnvironment,
  isAlipayEnvironment,
  isInDevTools,
  hasWeixinJSBridge,
  safeGetUrlParams
} from '@/utils/platform'

// ✅ 或导入整体
import * as PLATFORM from '@/utils/platform'
```

### 平台常量

```typescript
// 编译时确定的平台标识
export const platform = __UNI_PLATFORM__     // 'mp-weixin' | 'h5' | 'app' 等
export const isApp = __UNI_PLATFORM__ === 'app'
export const isMp = __UNI_PLATFORM__.startsWith('mp-')
export const isMpWeixin = __UNI_PLATFORM__.startsWith('mp-weixin')
export const isMpAlipay = __UNI_PLATFORM__.startsWith('mp-alipay')
export const isMpToutiao = __UNI_PLATFORM__.startsWith('mp-toutiao')
export const isH5 = __UNI_PLATFORM__ === 'h5'
```

### 运行时环境检测

```typescript
// 微信公众号H5（在微信内打开的H5页面）
export const isWechatOfficialH5 = (() => {
  if (__UNI_PLATFORM__ !== 'h5') return false
  const ua = safeGetUserAgent()
  return ua.includes('micromessenger') && !ua.includes('miniprogram')
})()

// 支付宝H5（在支付宝内打开的H5页面）
export const isAlipayOfficialH5 = (() => {
  if (__UNI_PLATFORM__ !== 'h5') return false
  const ua = safeGetUserAgent()
  return ua.includes('alipayclient')
})()

// 微信环境（小程序或公众号H5）
export const isWechatEnvironment = (): boolean => {
  if (isMpWeixin) return true
  if (__UNI_PLATFORM__ === 'h5') {
    const ua = safeGetUserAgent()
    return ua.includes('micromessenger')
  }
  return false
}

// 支付宝环境
export const isAlipayEnvironment = (): boolean => {
  if (isMpAlipay) return true
  if (__UNI_PLATFORM__ === 'h5') {
    const ua = safeGetUserAgent()
    return ua.includes('alipayclient')
  }
  return false
}

// 开发者工具检测
export const isInDevTools = (): boolean => {
  if (isMp) {
    try {
      // #ifdef MP
      const accountInfo = uni.getAccountInfoSync()
      return accountInfo.miniProgram.envVersion === 'develop'
      // #endif
    } catch (error) {}
    return false
  }
  return false
}

// 微信 JSBridge 检测（公众号H5支付需要）
export const hasWeixinJSBridge = (): boolean => {
  if (__UNI_PLATFORM__ !== 'h5') return false
  try {
    return typeof window !== 'undefined' && typeof window.WeixinJSBridge !== 'undefined'
  } catch (error) {
    return false
  }
}
```

### 安全的浏览器API

```typescript
// 安全的 UserAgent 获取（兼容所有平台）
const safeGetUserAgent = (): string => {
  if (isMp || isApp) return ''
  try {
    return typeof navigator !== 'undefined' && navigator.userAgent
      ? navigator.userAgent.toLowerCase()
      : ''
  } catch (error) {
    return ''
  }
}

// 安全的 URL 参数获取
export const safeGetUrlParams = (key: string): string | null => {
  if (isMp || isApp) return null
  try {
    const location = safeGetLocation()
    if (location.search) {
      const urlParams = new URLSearchParams(location.search)
      return urlParams.get(key)
    }
  } catch (error) {}
  return null
}
```

---

## 条件编译

### 语法说明

```
#ifdef PLATFORM       仅在指定平台编译
#ifndef PLATFORM      除了指定平台外都编译
#endif                结束条件编译块
```

### 支持的平台标识

| 标识 | 说明 |
|------|------|
| `H5` | H5 网页 |
| `APP` | 原生 APP |
| `MP` | 所有小程序 |
| `MP-WEIXIN` | 微信小程序 |
| `MP-ALIPAY` | 支付宝小程序 |
| `MP-BAIDU` | 百度小程序 |
| `MP-TOUTIAO` | 抖音小程序 |
| `MP-QQ` | QQ 小程序 |

### 在 TypeScript 中使用

```typescript
// 模式1: 仅特定平台执行
// #ifdef MP-WEIXIN
uni.requestPayment({
  provider: 'wxpay',
  timeStamp: payInfo.timeStamp,
  nonceStr: payInfo.nonceStr,
  package: payInfo.package,
  signType: payInfo.signType,
  paySign: payInfo.paySign,
  success: () => toast.success('支付成功'),
  fail: () => toast.error('支付失败'),
})
// #endif

// 模式2: 排除特定平台
// #ifndef H5
console.log('非H5平台执行')
// #endif

// 模式3: 多平台分支
const triggerShare = () => {
  // #ifdef MP-WEIXIN
  uni.showShareMenu({
    withShareTicket: true,
    menus: ['shareAppMessage', 'shareTimeline'],
  })
  // #endif

  // #ifdef MP-ALIPAY
  // @ts-expect-error 支付宝特有API
  my.showSharePanel({
    title: shareConfig.value.title,
    content: shareConfig.value.title,
    url: shareConfig.value.path,
  })
  // #endif

  // #ifdef MP-BAIDU
  // @ts-expect-error 百度特有API
  swan.openShare({
    title: shareConfig.value.title,
    imageUrl: shareConfig.value.imageUrl,
  })
  // #endif
}
```

### 在模板中使用

```vue
<template>
  <!-- 仅微信小程序显示 -->
  <!-- #ifdef MP-WEIXIN -->
  <button open-type="getPhoneNumber" @getphonenumber="handleGetPhone">
    获取手机号
  </button>
  <!-- #endif -->

  <!-- 仅H5显示 -->
  <!-- #ifdef H5 -->
  <button @click="handleSmsLogin">
    短信验证码登录
  </button>
  <!-- #endif -->

  <!-- 除了小程序外都显示 -->
  <!-- #ifndef MP -->
  <web-view src="https://example.com"></web-view>
  <!-- #endif -->
</template>
```

### 在样式中使用

```scss
/* #ifdef H5 */
.container {
  padding-top: 44px; /* H5需要顶部安全区 */
}
/* #endif */

/* #ifdef MP-WEIXIN */
.container {
  padding-top: 0; /* 小程序有原生导航栏 */
}
/* #endif */
```

### 全局类型声明

```typescript
// 仅H5环境的全局类型
// #ifdef H5
declare global {
  interface Window {
    WeixinJSBridge?: {
      invoke: (method: string, params: any, callback: (result: any) => void) => void
    }
    wx?: WechatJsSdk
    __wxjs?: WechatJsSdk
  }
}
// #endif
```

---

## nvue 原生渲染（仅 APP 端）

### 是什么 / 何时用

nvue（native vue）使用 **weex 引擎做原生渲染**，绕开 webview。仅 **APP-PLUS** 平台生效，**H5/小程序/其他端不会解析 `.nvue` 文件**。

| 场景 | 是否推荐用 nvue |
|------|----------------|
| 长列表（>100 项）、复杂滚动 | ✅ 推荐 |
| 原生地图、Canvas 动画、视频播放 | ✅ 推荐 |
| 普通业务页面（表单、详情、设置） | ❌ 不推荐，用 `.vue` 即可 |
| 需要 wd-ui / 第三方 vue 组件库 | ❌ 不能用 nvue |

### 如何使用 nvue

**框架的 `plus-app` 已默认支持 nvue**（manifest.json 和 vite 插件都已配置完毕），使用者**无需任何额外配置**：

1. 在 `plus-app/pages/{业务模块}/` 下直接建 `.nvue` 文件
2. 文件命名遵循业务语义（如 `goodsList.nvue`、`scanCode.nvue`），禁用 `list`、`demo` 等占位词
3. `pages.json` 由 `vite-plugin-uni-pages` 自动生成路由，不需要手写

> `plus-uniapp` 项目当前默认不开启 nvue（聚焦小程序/H5）。如要在 plus-uniapp 启用，参见 `app-adapter` 技能。

### nvue 的硬性约束（违反就报错/白屏）

| 限制 | 影响 | 替代方案 |
|------|------|---------|
| **只支持 flex 布局** | 不支持 grid、float、display:block、百分比尺寸 | 用 `flex: 1`、`rpx` 固定尺寸 |
| **文字必须包在 `<text>`** | `<view>` 里直接写文字会丢失，不显示 | 所有文字用 `<text>` 包裹 |
| **不支持 `v-show`** | 写了不生效 | 用 `v-if` 代替 |
| **不支持 background-image** | 背景图样式无效 | 用 `<image>` 组件叠加 |
| **不支持媒体查询 `@media`** | 写了不生效 | 用 JS 判断 + 动态 class |
| **CSS 选择器受限** | 只支持 class 选择器；不支持标签/id/通配符/后代选择器 | 全部用 class 选择器 |
| **不支持 wd-ui / 第三方 vue 组件** | WD UI 组件库基于 vue 渲染，nvue 中调用会白屏或报错 | nvue 页面用原生标签自己写 UI |
| **Vue3 nvue 不支持 recycle-list** | uni-app Vue3 移除该组件 | 用 `<list><cell>` 或 `<scroll-view>` |
| **不支持 Pinia 部分用法** | 早期版本 nvue 使用 store 需特殊处理 | 测试后再决定是否在 nvue 中用 store |

### 混合开发策略（最推荐）

**vue + nvue 同名共存**：同一目录建同名的 `.vue` + `.nvue`，编译器按平台选。

> ⚠️ **文件命名必须用业务语义名**（遵循 CLAUDE.md 命名规范）。禁止用 `list`、`index`、`test`、`demo`、`native` 等通用占位词。

示例（商品列表页同时支持多端 + APP 原生渲染）：

```
pages/goods/
├── goodsList.vue     ← H5 / 小程序使用（可用 wd-ui）
└── goodsList.nvue    ← APP 端优先使用（原生渲染，性能更好）
```

| 平台 | 实际使用 | 原因 |
|------|---------|------|
| H5 / 小程序 | `goodsList.vue` | 可用 wd-ui，无 nvue 限制 |
| APP | `goodsList.nvue` 优先 | 原生渲染，性能更好 |

pages.json 只注册一条 `path: "pages/goods/goodsList"`，编译器自动选。这是兼顾"多端兼容"和"APP 性能"的最优解。

### 排查 nvue 不解析问题（按顺序检查）

1. **运行环境**：是否 `pnpm dev:app` 或 HBuilderX 真机/模拟器？H5/小程序看不到 nvue
2. **manifest.json**：`nvueCompiler` 和 `nvueStyleCompiler` 都是 `"uni-app"` 吗？
3. **vite-plugin-uni-pages**：是否声明 `extensions: ['vue', 'nvue']`？
4. **pages.json**：自动生成的 pages.json 里有没有 `.nvue` 文件对应的路由？没有就是 Step 3 没配
5. **缓存**：删 `node_modules/.vite/`、删 `pages.json` 重新构建
6. **测试环境**：必须真机/APP 模拟器，浏览器/小程序模拟器看不到

---

## 跨平台支付实现

> ⚠️ **安全警告**
>
> 支付签名（`paySign`、`sign`）**必须由服务端生成**！
> 前端只负责调用支付接口，绝不能在前端计算签名。
> 所有敏感参数（如 `appId`、`mchId`、`apiKey`）都应保存在后端。

### 支付结果类型定义

```typescript
// 统一的支付结果类型
interface PayResult {
  success: boolean
  errCode?: 'cancel' | 'fail' | 'timeout' | 'unsupported'
  errMsg?: string
}
```

### 平台支持检测

```typescript
import * as PLATFORM from '@/utils/platform'

// 微信支付支持检测
const isWechatPaySupported = (): boolean => {
  return PLATFORM.isMpWeixin || PLATFORM.isWechatOfficialH5 || PLATFORM.isApp
}

// 支付宝支付支持检测
const isAlipayPaySupported = (): boolean => {
  return PLATFORM.isMpAlipay || PLATFORM.isAlipayOfficialH5 || PLATFORM.isH5 || PLATFORM.isApp
}

// 根据平台自动选择交易类型
const getTradeType = (paymentMethod: PaymentMethod): TradeType => {
  if (paymentMethod === PaymentMethod.WECHAT) {
    if (PLATFORM.isMpWeixin) return TradeType.JSAPI
    else if (PLATFORM.isApp) return TradeType.APP
    else if (PLATFORM.isWechatOfficialH5) return TradeType.JSAPI
    else if (PLATFORM.isH5) return TradeType.H5
    else return TradeType.NATIVE
  } else if (paymentMethod === PaymentMethod.ALIPAY) {
    if (PLATFORM.isMpAlipay || PLATFORM.isApp) return TradeType.APP
    else if (PLATFORM.isH5) return TradeType.WAP
    else return TradeType.PAGE
  }
}
```

### 微信支付多平台实现

```typescript
// 微信小程序支付
const callWechatMpPay = async (payInfo: PayInfo): Promise<boolean> => {
  return new Promise((resolve) => {
    // #ifdef MP-WEIXIN
    uni.requestPayment({
      provider: 'wxpay',
      timeStamp: payInfo.timeStamp,
      nonceStr: payInfo.nonceStr,
      package: payInfo.package,
      signType: payInfo.signType,
      paySign: payInfo.paySign,
      success: () => {
        toast.success('支付成功')
        resolve(true)
      },
      fail: (err) => {
        if (err.errMsg.includes('cancel')) {
          toast.info('取消支付')
        } else {
          toast.error('支付失败')
        }
        resolve(false)
      },
    })
    // #endif

    // #ifndef MP-WEIXIN
    console.error('微信小程序支付仅支持微信小程序环境')
    resolve(false)
    // #endif
  })
}

// 微信公众号H5支付（带超时处理）
const callWechatH5Pay = async (payInfo: PayInfo): Promise<PayResult> => {
  return new Promise((resolve) => {
    // #ifdef H5
    let timeoutId: ReturnType<typeof setTimeout> | null = null
    let resolved = false

    const cleanup = () => {
      if (timeoutId) {
        clearTimeout(timeoutId)
        timeoutId = null
      }
      document.removeEventListener('WeixinJSBridgeReady', onBridgeReady)
    }

    const resolveOnce = (result: PayResult) => {
      if (resolved) return
      resolved = true
      cleanup()
      resolve(result)
    }

    const invokeWechatPay = () => {
      window.WeixinJSBridge.invoke(
        'getBrandWCPayRequest',
        {
          appId: payInfo.appId,
          timeStamp: payInfo.timeStamp,
          nonceStr: payInfo.nonceStr,
          package: payInfo.package,
          signType: payInfo.signType,
          paySign: payInfo.paySign,
        },
        (res: any) => {
          if (res.err_msg === 'get_brand_wcpay_request:ok') {
            toast.success('支付成功')
            resolveOnce({ success: true })
          } else if (res.err_msg === 'get_brand_wcpay_request:cancel') {
            toast.info('取消支付')
            resolveOnce({ success: false, errCode: 'cancel', errMsg: '用户取消支付' })
          } else {
            toast.error('支付失败')
            resolveOnce({ success: false, errCode: 'fail', errMsg: res.err_msg })
          }
        }
      )
    }

    const onBridgeReady = () => {
      invokeWechatPay()
    }

    // 检查 WeixinJSBridge 是否就绪
    if (!PLATFORM.hasWeixinJSBridge()) {
      // 设置超时（5秒）
      timeoutId = setTimeout(() => {
        toast.error('支付初始化超时，请刷新页面重试')
        resolveOnce({ success: false, errCode: 'timeout', errMsg: 'WeixinJSBridge 初始化超时' })
      }, 5000)

      document.addEventListener('WeixinJSBridgeReady', onBridgeReady, false)
    } else {
      invokeWechatPay()
    }
    // #endif

    // #ifndef H5
    resolve({ success: false, errCode: 'unsupported', errMsg: '当前平台不支持公众号H5支付' })
    // #endif
  })
}

// APP微信支付
const callWechatAppPay = async (payInfo: PayInfo): Promise<boolean> => {
  return new Promise((resolve) => {
    // #ifdef APP
    uni.requestPayment({
      provider: 'wxpay',
      orderInfo: {
        appid: payInfo.appId,
        noncestr: payInfo.nonceStr,
        package: 'Sign=WXPay',
        partnerid: payInfo.partnerId,
        prepayid: payInfo.prepayId,
        timestamp: payInfo.timeStamp,
        sign: payInfo.paySign,
      },
      success: () => {
        toast.success('支付成功')
        resolve(true)
      },
      fail: () => {
        toast.error('支付失败')
        resolve(false)
      },
    })
    // #endif

    // #ifndef APP
    resolve({ success: false, errCode: 'unsupported', errMsg: '当前平台不支持APP微信支付' })
    // #endif
  })
}
```

### 支付宝支付多平台实现

```typescript
// 支付宝小程序支付
const callAlipayMpPay = async (payInfo: AlipayPayInfo): Promise<PayResult> => {
  return new Promise((resolve) => {
    // #ifdef MP-ALIPAY
    // @ts-expect-error 支付宝特有API
    my.tradePay({
      tradeNO: payInfo.tradeNo,  // 支付宝交易号
      success: (res: any) => {
        if (res.resultCode === '9000') {
          toast.success('支付成功')
          resolve({ success: true })
        } else if (res.resultCode === '6001') {
          toast.info('取消支付')
          resolve({ success: false, errCode: 'cancel', errMsg: '用户取消支付' })
        } else {
          toast.error('支付失败')
          resolve({ success: false, errCode: 'fail', errMsg: res.memo || '支付失败' })
        }
      },
      fail: (err: any) => {
        toast.error('支付失败')
        resolve({ success: false, errCode: 'fail', errMsg: err.errorMessage })
      },
    })
    // #endif

    // #ifndef MP-ALIPAY
    resolve({ success: false, errCode: 'unsupported', errMsg: '当前平台不支持支付宝小程序支付' })
    // #endif
  })
}

// 支付宝H5支付（WAP支付）
const callAlipayH5Pay = async (payInfo: AlipayPayInfo): Promise<PayResult> => {
  // #ifdef H5
  // H5环境直接跳转支付宝收银台
  if (payInfo.payUrl) {
    window.location.href = payInfo.payUrl
    return { success: true }  // 跳转后由回调页面处理结果
  }
  // 或者使用支付宝JSAPI（需在支付宝客户端内）
  if (PLATFORM.isAlipayOfficialH5 && window.AlipayJSBridge) {
    return new Promise((resolve) => {
      window.AlipayJSBridge.call('tradePay', {
        tradeNO: payInfo.tradeNo,
      }, (res: any) => {
        if (res.resultCode === '9000') {
          toast.success('支付成功')
          resolve({ success: true })
        } else {
          resolve({ success: false, errCode: 'fail', errMsg: res.memo })
        }
      })
    })
  }
  return { success: false, errCode: 'unsupported', errMsg: '缺少支付链接' }
  // #endif

  // #ifndef H5
  return { success: false, errCode: 'unsupported', errMsg: '当前平台不支持支付宝H5支付' }
  // #endif
}

// APP支付宝支付
const callAlipayAppPay = async (payInfo: AlipayPayInfo): Promise<PayResult> => {
  return new Promise((resolve) => {
    // #ifdef APP
    uni.requestPayment({
      provider: 'alipay',
      orderInfo: payInfo.orderString,  // 服务端返回的订单信息字符串
      success: () => {
        toast.success('支付成功')
        resolve({ success: true })
      },
      fail: (err: any) => {
        if (err.errMsg?.includes('cancel')) {
          toast.info('取消支付')
          resolve({ success: false, errCode: 'cancel', errMsg: '用户取消支付' })
        } else {
          toast.error('支付失败')
          resolve({ success: false, errCode: 'fail', errMsg: err.errMsg })
        }
      },
    })
    // #endif

    // #ifndef APP
    resolve({ success: false, errCode: 'unsupported', errMsg: '当前平台不支持APP支付宝支付' })
    // #endif
  })
}

// 支付宝支付参数类型
interface AlipayPayInfo {
  tradeNo?: string      // 支付宝交易号（小程序）
  orderString?: string  // 订单信息字符串（APP）
  payUrl?: string       // 支付跳转链接（H5）
}
```

---

## 跨平台分享实现

### 小程序分享

```typescript
// composables/useShare.ts
export const useShare = () => {
  const shareConfig = ref<ShareConfig>({
    title: '',
    path: '',
    imageUrl: '',
  })

  // 设置分享配置
  const setShareConfig = (config: Partial<ShareConfig>) => {
    Object.assign(shareConfig.value, config)
  }

  // 触发分享（多平台）
  const triggerShare = () => {
    // #ifdef MP-WEIXIN
    uni.showShareMenu({
      withShareTicket: true,
      menus: ['shareAppMessage', 'shareTimeline'],
    })
    // #endif

    // #ifdef MP-ALIPAY
    // @ts-expect-error 支付宝特有API
    my.showSharePanel({
      title: shareConfig.value.title,
      content: shareConfig.value.title,
      url: shareConfig.value.path,
    })
    // #endif

    // #ifdef MP-TOUTIAO
    // @ts-expect-error 抖音特有API
    tt.showShareMenu({
      withShareTicket: true,
      menus: ['shareAppMessage'],
    })
    // #endif

    // #ifdef MP-QQ
    // @ts-expect-error QQ特有API
    qq.showShareMenu({
      showShareItems: ['qq', 'qzone', 'wechatFriends', 'wechatMoment'],
    })
    // #endif

    // #ifdef APP
    // APP端使用系统分享
    uni.share({
      provider: 'weixin',  // 或 'qq', 'sinaweibo' 等
      scene: 'WXSceneSession',  // 'WXSceneSession' 好友 | 'WXSceneTimeline' 朋友圈
      type: 0,  // 0 图文 | 1 纯文字 | 2 纯图片 | 5 小程序
      title: shareConfig.value.title,
      summary: shareConfig.value.title,
      href: shareConfig.value.path,
      imageUrl: shareConfig.value.imageUrl,
      success: () => {
        console.log('分享成功')
      },
      fail: (err) => {
        console.error('分享失败', err)
      },
    })
    // #endif
  }

  // APP端分享到指定平台
  const shareToProvider = (provider: 'weixin' | 'qq' | 'sinaweibo', scene?: string) => {
    // #ifdef APP
    uni.share({
      provider,
      scene: scene || 'WXSceneSession',
      type: 0,
      title: shareConfig.value.title,
      summary: shareConfig.value.title,
      href: shareConfig.value.path,
      imageUrl: shareConfig.value.imageUrl,
    })
    // #endif
  }

  // 小程序生命周期钩子
  onShareAppMessage(() => ({
    title: shareConfig.value.title,
    path: shareConfig.value.path,
    imageUrl: shareConfig.value.imageUrl,
  }))

  onShareTimeline(() => ({
    title: shareConfig.value.title,
    path: shareConfig.value.path,
    imageUrl: shareConfig.value.imageUrl,
  }))

  return {
    shareConfig,
    setShareConfig,
    triggerShare,
    shareToProvider,  // APP端专用
  }
}
```

### APP端分享使用示例

```vue
<template>
  <!-- #ifdef APP -->
  <view class="share-buttons">
    <button @click="shareToProvider('weixin', 'WXSceneSession')">
      分享给微信好友
    </button>
    <button @click="shareToProvider('weixin', 'WXSceneTimeline')">
      分享到朋友圈
    </button>
    <button @click="shareToProvider('qq')">
      分享到QQ
    </button>
  </view>
  <!-- #endif -->
</template>
```

### 微信公众号H5分享

```typescript
// composables/useWxShare.ts
import { isWechatOfficialH5 } from '@/utils/platform'

export const useWxShare = () => {
  const isReady = ref(false)

  // 加载微信JS-SDK
  const loadSdk = (): Promise<void> => {
    // #ifdef H5
    return new Promise((resolve, reject) => {
      if (window.__wxjs?.config) {
        resolve()
        return
      }

      const script = document.createElement('script')
      script.src = 'https://res.wx.qq.com/open/js/jweixin-1.6.0.js'
      script.async = true
      script.onload = () => {
        window.__wxjs = window.wx
        resolve()
      }
      script.onerror = () => reject(new Error('SDK加载失败'))
      document.head.appendChild(script)
    })
    // #endif

    // #ifndef H5
    return Promise.reject(new Error('仅支持H5'))
    // #endif
  }

  // 初始化SDK
  const initSdk = async (): Promise<boolean> => {
    // #ifdef H5
    if (!isWechatOfficialH5) {
      console.warn('请在微信中打开')
      return false
    }

    await loadSdk()

    // 获取签名
    const url = location.href.split('#')[0]
    const [err, config] = await getJsApiSignature(url)
    if (err) return false

    // 配置SDK
    window.__wxjs.config({
      debug: false,
      appId: config.appId,
      timestamp: config.timestamp,
      nonceStr: config.nonceStr,
      signature: config.signature,
      jsApiList: ['updateAppMessageShareData', 'updateTimelineShareData'],
    })

    window.__wxjs.ready(() => {
      isReady.value = true
    })

    return true
    // #endif

    // #ifndef H5
    return false
    // #endif
  }

  // 设置分享
  const setShare = (config: WxShareConfig) => {
    // #ifdef H5
    if (!isReady.value) return

    window.__wxjs.updateAppMessageShareData({
      title: config.title,
      desc: config.desc,
      link: config.link || location.href,
      imgUrl: config.imgUrl,
      success: config.success,
      cancel: config.cancel,
    })

    window.__wxjs.updateTimelineShareData({
      title: config.title,
      link: config.link || location.href,
      imgUrl: config.imgUrl,
    })
    // #endif
  }

  return {
    isReady,
    initSdk,
    setShare,
  }
}
```

---

## 平台特有功能封装

### 微信小程序头像选择

```vue
<!-- 跨平台头像选择组件 -->
<template>
  <!-- 微信小程序专用头像选择 -->
  <!-- #ifdef MP-WEIXIN -->
  <button open-type="chooseAvatar" @chooseavatar="onChooseAvatar">
    选择头像
  </button>
  <!-- #endif -->

  <!-- 其他平台通用选择 -->
  <!-- #ifndef MP-WEIXIN -->
  <button @click="onManualChoose">
    选择头像
  </button>
  <!-- #endif -->
</template>

<script setup lang="ts">
// 微信小程序头像选择
const onChooseAvatar = (e: any) => {
  // #ifdef MP-WEIXIN
  const { avatarUrl } = e.detail
  upload.fastUpload(avatarUrl, {
    onSuccess(res) {
      form.avatar = res.url
    },
    onError(err) {
      toast.error('上传失败')
    },
  })
  // #endif
}

// 通用头像选择
const onManualChoose = async () => {
  // #ifndef MP-WEIXIN
  const res = await upload.chooseFile({ accept: 'image', maxCount: 1 })
  upload.fastUpload(res[0].path, {
    onSuccess(uploadRes) {
      form.avatar = uploadRes.url
    },
  })
  // #endif
}
</script>
```

### 微信小程序手机号授权

```vue
<!-- 跨平台手机号登录组件 -->
<template>
  <!-- 微信小程序手机号授权 -->
  <!-- #ifdef MP-WEIXIN -->
  <button open-type="getPhoneNumber" @getphonenumber="onGetPhone">
    一键登录
  </button>
  <!-- #endif -->

  <!-- 其他平台短信验证码登录 -->
  <!-- #ifndef MP-WEIXIN -->
  <wd-input v-model="phone" placeholder="请输入手机号" />
  <wd-input v-model="code" placeholder="请输入验证码" />
  <button @click="onSmsLogin">登录</button>
  <!-- #endif -->
</template>

<script setup lang="ts">
const onGetPhone = async (e: any) => {
  // #ifdef MP-WEIXIN
  if (e.detail.errMsg !== 'getPhoneNumber:ok') {
    toast.info('取消授权')
    return
  }

  const [err] = await bindPhone({ code: e.detail.code })
  if (!err) {
    toast.success('绑定成功')
  }
  // #endif
}
</script>
```

---

## 最佳实践

### 1. 平台判断优先级

```
条件编译 (#ifdef) > 运行时平台常量 > 功能特性检测 > 降级方案
```

### 2. 何时使用条件编译

```typescript
// ✅ 使用条件编译：平台专有API
// #ifdef MP-WEIXIN
uni.getAccountInfoSync()
// #endif

// ✅ 使用条件编译：平台专有UI
<!-- #ifdef MP-WEIXIN -->
<button open-type="getPhoneNumber">
<!-- #endif -->

// ❌ 不用条件编译：通用逻辑的平台差异
// 使用运行时判断
if (PLATFORM.isMpWeixin) {
  // 微信特殊处理
}
```

### 3. 何时使用运行时判断

```typescript
// ✅ 运行时判断：复杂的平台分支逻辑
const getTradeType = () => {
  if (PLATFORM.isMpWeixin) return 'JSAPI'
  if (PLATFORM.isApp) return 'APP'
  if (PLATFORM.isH5) return 'H5'
}

// ✅ 运行时判断：需要在函数中动态判断
const checkEnvironment = () => {
  return PLATFORM.isWechatEnvironment()
}
```

### 4. 功能降级策略

```typescript
const executePayment = async (method: PaymentMethod) => {
  // 优先使用原生支付
  if (method === PaymentMethod.WECHAT && PLATFORM.isMpWeixin) {
    return await callWechatMpPay()
  }

  // H5环境使用JSBridge
  if (method === PaymentMethod.WECHAT && PLATFORM.isWechatOfficialH5) {
    return await callWechatH5Pay()
  }

  // 降级到余额支付
  if (PLATFORM.isH5 && !PLATFORM.isWechatOfficialH5) {
    toast.info('当前环境不支持微信支付，请使用余额支付')
    return await callBalancePay()
  }
}
```

### 5. 安全的API调用模式

```typescript
// 总是先检查平台再调用特有API
const getWechatCode = async (): Promise<string | null> => {
  if (!PLATFORM.isMpWeixin) {
    console.warn('仅支持微信小程序')
    return null
  }

  return new Promise((resolve) => {
    // #ifdef MP-WEIXIN
    uni.login({
      provider: 'weixin',
      success: (res) => resolve(res.code),
      fail: () => resolve(null),
    })
    // #endif

    // #ifndef MP-WEIXIN
    resolve(null)
    // #endif
  })
}
```

---

## 常见问题

### 1. 条件编译不生效

**注释格式因上下文而异**：

| 上下文 | 正确格式 | 错误格式 |
|--------|---------|---------|
| `<script>` / `.ts` | `// #ifdef MP-WEIXIN` | `/* #ifdef */` |
| `<template>` | `<!-- #ifdef MP-WEIXIN -->` | `// #ifdef` |
| `<style>` / `.scss` | `/* #ifdef MP-WEIXIN */` | `// #ifdef` |

**其他检查项**：
- 检查平台标识是否正确（必须**大写**，如 `MP-WEIXIN` 不是 `mp-weixin`）
- 确保 `#ifdef` 和 `#endif` 成对出现
- 确保 `#endif` 前没有多余空格

### 2. 运行时报错 navigator/window 未定义

```typescript
// ❌ 错误：直接访问浏览器API
const ua = navigator.userAgent

// ✅ 正确：使用安全访问
import { safeGetUserAgent } from '@/utils/platform'
const ua = safeGetUserAgent()
```

### 3. TypeScript 报错平台特有API

```typescript
// 使用 @ts-expect-error 抑制错误
// #ifdef MP-ALIPAY
// @ts-expect-error 支付宝特有API
my.showSharePanel({ ... })
// #endif
```

### 4. H5 环境判断微信公众号

```typescript
// ❌ 错误：只判断 isH5
if (isH5) {
  // 这包含了普通浏览器和微信公众号
}

// ✅ 正确：精确判断
if (isWechatOfficialH5) {
  // 仅微信公众号H5
}

if (isH5 && !isWechatOfficialH5 && !isAlipayOfficialH5) {
  // 普通浏览器H5
}
```
