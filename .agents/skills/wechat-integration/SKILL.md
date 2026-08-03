---
name: wechat-integration
description: |
  当需要对接微信生态功能时自动使用此 Skill。包含小程序登录、公众号分享、消息订阅、手机号验证等。

  触发场景：
  - 微信小程序登录
  - 微信公众号H5分享
  - 订阅消息推送
  - 获取微信手机号
  - 微信JS-SDK配置

  触发词：微信、小程序、公众号、分享、订阅消息、openid、手机号授权、wx.login、JSSDK、微信登录
---

# 微信生态集成指南

## 功能概览

| 功能 | 平台 | Composable | 说明 |
|------|------|-----------|------|
| 小程序登录 | 微信小程序 | useUserStore | 一键登录、手机号授权 |
| 公众号登录 | 微信H5 | useUserStore | OAuth网页授权 |
| H5分享 | 微信公众号H5 | useWxShare | 自定义分享内容 |
| 小程序分享 | 微信小程序 | onShareAppMessage | 分享给朋友/朋友圈 |
| 订阅消息 | 微信小程序 | useSubscribe | 消息模板订阅 |

---

## wd-button 开放能力速查表

> **重要**：移动端必须使用 `wd-button` 组件，禁止使用原生 `button`！

### 微信小程序 open-type

| open-type | 事件 | 说明 |
|-----------|------|------|
| `getPhoneNumber` | `@getphonenumber` | 获取用户手机号 |
| `chooseAvatar` | `@chooseavatar` | 获取用户头像 |
| `contact` | `@contact` | 打开客服会话 |
| `share` | - | 触发用户转发（需配合 onShareAppMessage） |
| `openSetting` | `@opensetting` | 打开授权设置页 |
| `feedback` | - | 打开意见反馈页面 |
| `launchApp` | `@launchapp` | 打开 APP（需配置） |
| `agreePrivacyAuthorization` | `@agreeprivacyauthorization` | 隐私协议授权 |

### wd-button 示例

```vue
<!-- 获取手机号 -->
<wd-button open-type="getPhoneNumber" @getphonenumber="onGetPhone">
  获取手机号
</wd-button>

<!-- 获取头像 -->
<wd-button open-type="chooseAvatar" @chooseavatar="onChooseAvatar">
  选择头像
</wd-button>

<!-- 客服消息 -->
<wd-button
  open-type="contact"
  @contact="onContact"
  session-from="页面A"
  send-message-title="咨询商品"
>
  联系客服
</wd-button>

<!-- 分享按钮 -->
<wd-button open-type="share">分享给朋友</wd-button>

<!-- 打开设置 -->
<wd-button open-type="openSetting" @opensetting="onOpenSetting">
  授权设置
</wd-button>

<!-- 隐私协议（2023年新增） -->
<wd-button
  open-type="agreePrivacyAuthorization"
  @agreeprivacyauthorization="onAgreePrivacy"
>
  同意并继续
</wd-button>
```

---

## 1. 微信小程序登录

### 使用 useUserStore

```typescript
// ✅ useUserStore 已自动导入，无需手动 import

const userStore = useUserStore()

// 基础登录（只获取 openid）
const handleQuickLogin = async () => {
  const [err] = await userStore.loginWithMiniapp()
  if (!err) {
    // 登录成功
    uni.switchTab({ url: '/pages/index/index' })
  }
}

// ⚠️ getUserInfo 已废弃（2022年），请使用下方的头像昵称组件
```

### 获取用户头像和昵称（新版 API）

微信 2022 年起废弃 `getUserInfo`，改用头像昵称填写组件：

```vue
<!-- 微信小程序获取用户头像和昵称 -->
<template>
  <view class="user-info-form">
    <!-- 头像选择：使用 wd-button 的 chooseAvatar -->
    <view class="avatar-wrapper">
      <wd-button
        open-type="chooseAvatar"
        @chooseavatar="handleChooseAvatar"
        type="text"
        custom-class="avatar-btn"
      >
        <image :src="userInfo.avatarUrl || defaultAvatar" class="avatar" />
      </wd-button>
      <text>点击选择头像</text>
    </view>

    <!-- 昵称输入：使用 type="nickname" 的 input -->
    <wd-input
      v-model="userInfo.nickName"
      type="nickname"
      label="昵称"
      placeholder="请输入昵称"
      @blur="handleNicknameBlur"
    />

    <wd-button block @click="handleSubmit">保存信息</wd-button>
  </view>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { useToast } from '@/wd'

const toast = useToast()
const defaultAvatar = '/static/images/default-avatar.png'

const userInfo = reactive({
  avatarUrl: '',
  nickName: ''
})

// 处理头像选择
const handleChooseAvatar = (detail: any) => {
  // detail.avatarUrl 是临时文件路径，需要上传到服务器
  userInfo.avatarUrl = detail.avatarUrl

  // 建议：上传到 OSS 获取永久地址
  // uploadAvatar(detail.avatarUrl)
}

// 处理昵称输入完成
const handleNicknameBlur = (e: any) => {
  userInfo.nickName = e.detail.value
}

// 提交用户信息
const handleSubmit = async () => {
  if (!userInfo.avatarUrl || !userInfo.nickName) {
    toast.warning('请完善头像和昵称')
    return
  }

  const [err] = await updateUserInfo(userInfo)
  if (!err) {
    toast.success('保存成功')
  }
}
</script>

<style lang="scss">
.avatar-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32rpx;
}
.avatar-btn {
  padding: 0 !important;
  min-width: 0 !important;
}
.avatar {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
}
</style>
```

### 获取微信手机号

```vue
<!-- 微信小程序获取手机号 -->
<template>
  <!-- ✅ 必须使用 wd-button，不要用原生 button -->
  <wd-button
    open-type="getPhoneNumber"
    @getphonenumber="handleGetPhone"
  >
    获取手机号
  </wd-button>
</template>

<script setup lang="ts">
import { useToast } from '@/wd'

const toast = useToast()

const handleGetPhone = async (detail: any) => {
  // wd-button 直接返回 detail，不是 e.detail
  if (detail.errMsg !== 'getPhoneNumber:ok') {
    toast.error('授权失败')
    return
  }

  // 调用后端接口绑定手机号（新版 API 使用 code）
  const [err] = await bindPhone({
    code: detail.code,  // 微信返回的动态令牌
  })

  if (!err) {
    toast.success('绑定成功')
  }
}
</script>
```

> **注意**：`wd-button` 的事件回调直接返回 `detail` 对象，不需要 `e.detail`。

---

## 2. 微信公众号H5登录

### OAuth 授权流程

```typescript
// 1. 引导用户授权
const redirectToAuth = () => {
  const appid = SystemConfig.platforms.wechatOfficialAppId
  const redirectUri = encodeURIComponent(window.location.href)
  const state = Math.random().toString(36).slice(2)

  // 存储 state 用于校验
  sessionStorage.setItem('wx_auth_state', state)

  const url = `https://open.weixin.qq.com/connect/oauth2/authorize?appid=${appid}&redirect_uri=${redirectUri}&response_type=code&scope=snsapi_userinfo&state=${state}#wechat_redirect`

  window.location.href = url
}

// 2. 回调页面处理
onMounted(async () => {
  const url = new URL(window.location.href)
  const code = url.searchParams.get('code')
  const state = url.searchParams.get('state')

  if (code && state) {
    // 校验 state
    if (state !== sessionStorage.getItem('wx_auth_state')) {
      console.error('state 校验失败')
      return
    }

    // 使用 code 登录
    const userStore = useUserStore()
    const [err] = await userStore.loginWithMp({ code, state })

    if (!err) {
      // 登录成功，清理 URL 参数
      history.replaceState(null, '', url.pathname)
    }
  }
})
```

---

## 3. 微信公众号H5分享

### 使用 useWxShare

```typescript
// ✅ useWxShare 已自动导入，无需手动 import

const { share, isReady, error } = useWxShare()

// 配置分享内容
onMounted(async () => {
  const success = await share({
    title: '分享标题',
    desc: '分享描述文字',
    imgUrl: 'https://example.com/share-image.jpg',  // 必须是完整HTTPS地址
    link: window.location.href,  // 可选，默认当前页面
    success: () => {
      console.log('分享成功')
    },
    cancel: () => {
      console.log('取消分享')
    }
  })

  if (!success) {
    console.error('分享配置失败:', error.value)
  }
})
```

### 分步骤使用

```typescript
const { initSdk, setShare, isReady, error } = useWxShare()

// 1. 初始化 SDK（可开启调试模式）
const init = async () => {
  const success = await initSdk({ debug: false })
  if (!success) {
    console.error('SDK初始化失败:', error.value)
  }
}

// 2. 设置分享内容（可多次调用）
const updateShare = (title: string, desc: string) => {
  if (!isReady.value) {
    console.warn('SDK未就绪')
    return
  }

  setShare({
    title,
    desc,
    imgUrl: 'https://example.com/img.jpg'
  })
}
```

### 分享配置参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `title` | string | 是 | 分享标题 |
| `desc` | string | 是 | 分享描述 |
| `imgUrl` | string | 是 | 分享图片URL（完整HTTPS地址） |
| `link` | string | 否 | 分享链接（默认当前页面） |
| `success` | function | 否 | 分享成功回调 |
| `cancel` | function | 否 | 取消分享回调 |

---

## 4. 小程序分享（重要！）

> **与 H5 分享不同**：小程序分享使用 `onShareAppMessage` 和 `onShareTimeline`，不需要 JS-SDK。

### 分享给朋友

```typescript
// 在页面的 <script setup> 中
import { onShareAppMessage, onShareTimeline } from '@dcloudio/uni-app'

// 分享给朋友
onShareAppMessage((res) => {
  // res.from: 'button' 或 'menu'（右上角菜单）
  // res.target: 如果 from='button'，则是触发分享的按钮

  return {
    title: '分享标题',
    path: '/pages/index/index?id=123',  // 分享路径（带参数）
    imageUrl: '/static/share-image.png', // 可选，默认截图
  }
})

// 分享到朋友圈（需要开启）
onShareTimeline(() => {
  return {
    title: '朋友圈分享标题',  // 朋友圈只显示标题
    query: 'id=123',  // 分享参数（不带 path）
    imageUrl: '/static/timeline-image.png',
  }
})
```

### 按钮触发分享

```vue
<!-- 微信小程序按钮触发分享 -->
<template>
  <!-- 方式1：wd-button 触发（推荐） -->
  <wd-button open-type="share">
    分享给朋友
  </wd-button>

  <!-- 方式2：自定义分享按钮 -->
  <view @click="handleShare">
    <wd-icon name="share" />
    <text>分享</text>
  </view>
</template>

<script setup lang="ts">
import { onShareAppMessage } from '@dcloudio/uni-app'

// 必须定义 onShareAppMessage 才能使分享生效
onShareAppMessage((res) => {
  if (res.from === 'button') {
    // 来自按钮点击
    console.log('按钮分享')
  }

  return {
    title: productInfo.value.name,
    path: `/pages-sub/goods/detail?id=${productInfo.value.id}`,
    imageUrl: productInfo.value.coverImage,
  }
})

// 自定义按钮调用
const handleShare = () => {
  // 注意：这种方式需要在 app.json 或页面配置中开启分享
  // 实际上点击后会触发 onShareAppMessage
}
</script>
```

### 动态分享内容

```typescript
import { ref, computed } from 'vue'
import { onShareAppMessage } from '@dcloudio/uni-app'

const productInfo = ref<any>(null)

// 动态返回分享内容
onShareAppMessage(() => {
  if (productInfo.value) {
    return {
      title: `推荐：${productInfo.value.name}`,
      path: `/pages-sub/goods/detail?id=${productInfo.value.id}`,
      imageUrl: productInfo.value.shareImage || productInfo.value.coverImage,
    }
  }

  // 默认分享
  return {
    title: '欢迎使用小程序',
    path: '/pages/index/index',
  }
})
```

### 分享参数接收

```typescript
// 在目标页面接收分享参数
import { onLoad } from '@dcloudio/uni-app'

onLoad((options) => {
  if (options?.id) {
    // 来自分享链接
    loadDetail(options.id)
  }

  // 朋友圈分享参数（query 形式）
  if (options?.shareFrom === 'timeline') {
    // 来自朋友圈
  }
})
```

### 小程序分享 vs H5 分享

| 特性 | 小程序分享 | H5 分享 |
|------|-----------|---------|
| API | `onShareAppMessage` | `useWxShare` |
| 触发方式 | 按钮/右上角菜单 | 右上角菜单 |
| 分享到朋友圈 | `onShareTimeline` | `setShare` 自动支持 |
| 需要签名 | ❌ 不需要 | ✅ 需要 JS-SDK 签名 |
| 路径参数 | `path` 带完整路径 | `link` 带完整 URL |

---

## 5. 订阅消息

### 使用 useSubscribe

```typescript
// ✅ useSubscribe 已自动导入，无需手动 import

const {
  templates,
  loading,
  loadTemplates,
  subscribe,
  subscribeAll,
  isSubscribed
} = useSubscribe()

// 加载订阅模板配置
onLoad(async () => {
  await loadTemplates()
  console.log('可用模板:', templates.value)
})

// 订阅单个模板（支持模板ID或标题关键词）
const handleSubscribe = async () => {
  const result = await subscribe('订单发货通知')  // 或模板ID

  if (result.success) {
    console.log('订阅成功:', result.subscribedIds)
  } else {
    console.log('订阅失败:', result.errMsg)
    console.log('被拒绝的模板:', result.rejectedIds)
  }
}

// 订阅多个模板
const handleMultiSubscribe = async () => {
  const result = await subscribe(['订单发货', '支付成功'])
  // 处理结果...
}

// 订阅所有可用模板
const handleSubscribeAll = async () => {
  const result = await subscribeAll()
  // 处理结果...
}
```

### 订阅结果

```typescript
interface SubscribeResult {
  success: boolean           // 是否有订阅成功
  subscribedIds: string[]    // 订阅成功的模板ID
  failedIds: string[]        // 订阅失败的模板ID
  rejectedIds: string[]      // 用户拒绝的模板ID
  errMsg: string             // 错误信息
}
```

### 检查订阅状态

```typescript
// 检查是否已订阅
const checkStatus = () => {
  if (isSubscribed('订单发货通知')) {
    console.log('已订阅')
  }
}

// 标记消息已发送（用于一次性订阅）
const afterSend = () => {
  markSubscribeSent('template_id')
}
```

---

## 6. 后端接口

### JS-SDK 签名接口

```java
// WxShareController.java
@RestController
@RequestMapping("/app/wxShare")
public class WxShareController {

    @Autowired
    private WxMpService wxMpService;

    /**
     * 获取 JS-SDK 签名
     */
    @GetMapping("/getJsApiSignature")
    public R<JsApiSignatureVo> getJsApiSignature(@RequestParam String url) {
        WxJsapiSignature signature = wxMpService.createJsapiSignature(url);
        return R.ok(JsApiSignatureVo.builder()
            .appId(signature.getAppId())
            .timestamp(String.valueOf(signature.getTimestamp()))
            .nonceStr(signature.getNonceStr())
            .signature(signature.getSignature())
            .build());
    }
}
```

### 订阅模板配置接口

```java
// SubscribeController.java
@RestController
@RequestMapping("/app/subscribe")
public class SubscribeController {

    /**
     * 获取订阅模板配置
     */
    @GetMapping("/getTemplateConfigs")
    public R<List<TemplateConfigVo>> getTemplateConfigs(@RequestParam String appid) {
        // 从数据库获取该 appid 配置的订阅模板
        return R.ok(subscribeService.getTemplates(appid));
    }
}
```

---

## 7. 平台判断工具

```typescript
import {
  isMpWeixin,           // 是否微信小程序
  isWechatOfficialH5,   // 是否微信公众号H5
  isH5,                 // 是否H5
  isMp,                 // 是否小程序（任意平台）
  isApp                 // 是否APP
} from '@/utils/platform'

// 使用示例
if (isMpWeixin) {
  // 微信小程序特有逻辑
  uni.login({ provider: 'weixin' })
}

if (isWechatOfficialH5) {
  // 微信公众号H5特有逻辑
  const { share } = useWxShare()
  await share({ ... })
}
```

---

## 8. 常见问题

### 分享图片不显示

- 图片必须是**完整的 HTTPS 地址**
- 图片大小建议 **300x300** 以上
- 不要使用本地图片或相对路径

### JS-SDK 签名失败

- 确认 URL 不包含 `#` 后面的部分
- 确认 appid 和 secret 正确
- 确认 IP 白名单配置

### 订阅消息收不到

- 确认模板 ID 正确且已审核通过
- 用户必须**主动触发**订阅弹窗（不能自动弹）
- 一次订阅只能发送一条消息

### 手机号获取失败

- 必须在**真机**上测试
- 小程序必须已上线或为**体验版**
- 用户必须**主动点击**按钮授权

---

## 9. 最佳实践

### 登录流程设计

```
用户打开小程序
    ↓
检查是否已登录 (userStore.isLoggedIn)
    ↓ 否
静默登录获取 openid (loginWithMiniapp)
    ↓
需要用户信息？→ 引导授权
    ↓
需要手机号？→ 引导手机号授权
    ↓
完成登录
```

### 分享时机

```typescript
// 推荐：在页面 onShow 时配置分享
onShow(() => {
  share({
    title: pageTitle.value,
    desc: pageDesc.value,
    imgUrl: shareImage.value
  })
})

// 动态更新分享内容
watch(productInfo, (info) => {
  if (info) {
    setShare({
      title: info.name,
      desc: info.description,
      imgUrl: info.coverImage
    })
  }
})
```

### 订阅消息触发时机

```typescript
// 在关键操作前请求订阅
const handleSubmitOrder = async () => {
  // 先请求订阅（用户可能拒绝，不影响下单）
  await subscribe(['订单发货', '物流更新'])

  // 提交订单
  const [err] = await submitOrder(orderData)
  if (!err) {
    // 跳转支付
  }
}
```
