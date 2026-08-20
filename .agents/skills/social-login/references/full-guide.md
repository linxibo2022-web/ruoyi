
# 社交登录（OAuth2）开发指南

## 概述

本项目通过 `ruoyi-common-social` 模块封装 JustAuth，支持 **27+ 社交平台**的 OAuth2 登录。采用**策略模式**实现认证分离，支持多租户隔离、自动注册、邀请码绑定等企业级功能。

**核心能力**：
- 27+ 平台一键接入（微信/QQ/GitHub/Gitee/钉钉/企业微信等）
- 4 个自建平台扩展（Gitea/MaxKey/TopIAM/钉钉V2）
- 完整的绑定/解绑/自动注册流程
- Redis 状态缓存防 CSRF
- 分布式锁防并发绑定
- 多租户数据隔离

**与 `wechat-integration` 的区别**：
| 维度 | social-login（本技能） | wechat-integration |
|------|----------------------|-------------------|
| 范围 | 27+ 社交平台的 OAuth 登录 | 微信生态全功能 |
| 核心 | 授权→令牌→用户信息 | 小程序登录/支付/订阅消息/手机号 |
| 场景 | PC 端社交登录、SSO 集成 | 小程序/公众号业务功能 |

---

## 模块结构

```
ruoyi-common/ruoyi-common-social/
└── src/main/java/
    ├── plus/ruoyi/common/social/
    │   ├── config/
    │   │   ├── SocialAutoConfiguration.java       # 自动配置（33行）
    │   │   └── properties/
    │   │       ├── SocialProperties.java          # 配置属性映射（29行）
    │   │       └── SocialLoginConfigProperties.java # 单平台配置（76行）
    │   ├── utils/
    │   │   ├── SocialUtils.java                   # 核心工具类（102行）⭐
    │   │   └── AuthRedisStateCache.java           # Redis 状态缓存（65行）
    │   ├── gitea/                                 # Gitea 扩展
    │   │   ├── AuthGiteaRequest.java              # 认证请求（132行）
    │   │   └── AuthGiteaSource.java               # 接口地址（61行）
    │   ├── maxkey/                                # MaxKey 扩展
    │   │   ├── AuthMaxKeyRequest.java             # 认证请求（114行）
    │   │   └── AuthMaxKeySource.java              # 接口地址（61行）
    │   └── topiam/                                # TopIAM 扩展
    │       ├── AuthTopIamRequest.java             # 认证请求（172行）
    │       └── AuthTopIamSource.java              # 接口地址（62行）
    └── me/zhyd/oauth/request/
        ├── AbstractAuthWeChatEnterpriseRequest.java # 企业微信（181行）
        └── AuthDingTalkV2Request.java              # 钉钉V2（110行）

ruoyi-modules/ruoyi-system/
└── src/main/java/plus/ruoyi/system/
    ├── core/
    │   ├── domain/SysSocial.java                  # 实体（137行）
    │   ├── domain/bo/SysSocialBo.java             # 业务对象（148行）
    │   ├── domain/vo/SysSocialVo.java             # 视图对象（149行）
    │   ├── mapper/SysSocialMapper.java            # Mapper（14行）
    │   ├── dao/ISysSocialDao.java                 # DAO 接口（42行）
    │   ├── dao/impl/SysSocialDaoImpl.java         # DAO 实现（67行）
    │   ├── service/ISysSocialService.java         # Service 接口（92行）
    │   ├── service/impl/SysSocialServiceImpl.java # Service 实现（156行）
    │   └── controller/SysSocialController.java    # 绑定列表控制器（42行）
    └── auth/
        ├── controller/AuthController.java         # 认证控制器（292行）⭐
        ├── service/IAuthStrategy.java             # 策略接口（55行）
        ├── service/impl/SocialAuthStrategy.java   # 社交登录策略（328行）⭐
        └── service/SysLoginService.java           # 登录服务（315行）
```

---

## 配置

### application.yml（justauth 配置）

```yaml
justauth:
  # 前端外网访问地址（回调域名）
  address: ${JUSTAUTH_ADDRESS:http://localhost}
  type:
    # 微信开放平台
    wechat_open:
      client-id: ${WECHAT_OPEN_CLIENT_ID:wxxxxxxxxxxx}
      client-secret: ${WECHAT_OPEN_CLIENT_SECRET:xxxxxxxx}
      redirect-uri: ${justauth.address}/socialCallback?source=wechat_open
    # Gitee
    gitee:
      client-id: ${GITEE_CLIENT_ID:xxxxxxxx}
      client-secret: ${GITEE_CLIENT_SECRET:xxxxxxxx}
      redirect-uri: ${justauth.address}/socialCallback?source=gitee
    # GitHub
    github:
      client-id: ${GITHUB_CLIENT_ID:xxxxxxxx}
      client-secret: ${GITHUB_CLIENT_SECRET:xxxxxxxx}
      redirect-uri: ${justauth.address}/socialCallback?source=github
    # MaxKey（SSO 服务器）
    maxkey:
      server-url: http://sso.maxkey.top
      client-id: 876892492581044224
      client-secret: x1Y5MTMwNzIwMjMxNTM4NDc3Mzche8
      redirect-uri: ${justauth.address}/socialCallback?source=maxkey
    # TopIAM（SSO 服务器）
    topiam:
      server-url: ${TOPIAM_SERVER_URL:http://127.0.0.1:1898/api/v1/authorize/xxx}
      client-id: ${TOPIAM_CLIENT_ID:xxxxxxxx}
      client-secret: ${TOPIAM_CLIENT_SECRET:xxxxxxxx}
      redirect-uri: ${justauth.address}/socialCallback?source=topiam
      scopes: [openid, email, phone, profile]
    # 企业微信
    wechat_enterprise:
      client-id: ${WECHAT_ENTERPRISE_CLIENT_ID:xxxxxxxx}
      client-secret: ${WECHAT_ENTERPRISE_CLIENT_SECRET:xxxxxxxx}
      redirect-uri: ${justauth.address}/socialCallback?source=wechat_enterprise
      agent-id: ${WECHAT_ENTERPRISE_AGENT_ID:1000002}
    # Gitea（自建平台）
    gitea:
      server-url: ${GITEA_SERVER_URL:https://gitea.example.com}
      client-id: ${GITEA_CLIENT_ID:xxxxxxxx}
      client-secret: ${GITEA_CLIENT_SECRET:xxxxxxxx}
      redirect-uri: ${justauth.address}/socialCallback?source=gitea
```

> **关键配置**：
> - `justauth.address`：前端回调域名，必须与第三方平台后台配置一致
> - `redirect-uri` 格式：`{address}/socialCallback?source={平台标识}`
> - 所有敏感信息支持环境变量覆盖

### 系统配置（sys_config）

```sql
-- 社交登录自动注册开关（默认关闭）
INSERT INTO sys_config VALUES
(5, '000000', '社交登录-自动注册开关', 'system.social.auto-register-enabled', 'false', ...);
```

---

## 一、支持的平台（27+）

### JustAuth 内置平台

| 平台 | source 标识 | 说明 |
|------|------------|------|
| 微信开放平台 | `wechat_open` | 扫码登录 |
| 微信公众号 | `wechat_mp` | 网页授权 |
| 企业微信 | `wechat_enterprise` | 企业内部登录 |
| QQ | `qq` | QQ 互联 |
| 微博 | `weibo` | 微博开放平台 |
| 钉钉 | `dingtalk` | 钉钉开放平台（V2） |
| 百度 | `baidu` | 百度开放平台 |
| GitHub | `github` | GitHub OAuth |
| Gitee | `gitee` | Gitee OAuth |
| GitLab | `gitlab` | GitLab OAuth |
| 支付宝 | `alipay_wallet` | 支付宝开放平台 |
| 淘宝 | `taobao` | 淘宝开放平台 |
| 抖音 | `douyin` | 抖音开放平台 |
| LinkedIn | `linkedin` | 领英开放平台 |
| Microsoft | `microsoft` | 微软 Azure AD |
| 华为 | `huawei` | 华为开发者联盟 |
| Coding | `coding` | Coding.net |
| OSChina | `oschina` | 开源中国 |
| 人人网 | `renren` | 人人网 |
| StackOverflow | `stack_overflow` | StackOverflow |
| 阿里云 | `aliyun` | 阿里云 OAuth |

### 自建扩展平台

| 平台 | source 标识 | 核心类 | 说明 |
|------|------------|--------|------|
| Gitea | `gitea` | `AuthGiteaRequest` | 自建 Git 平台 |
| MaxKey | `maxkey` | `AuthMaxKeyRequest` | 开源 SSO 平台 |
| TopIAM | `topiam` | `AuthTopIamRequest` | 企业 IAM 平台 |
| 钉钉V2 | `dingtalk` | `AuthDingTalkV2Request` | 新版钉钉 API |

---

## 二、核心流程

### 流程 1：社交登录（未绑定→自动注册）

```
前端                      后端                          第三方平台
 │                         │                              │
 │ 1. GET /auth/socialBindUrl/github                      │
 │──────────────────────→│                              │
 │                         │ 构建 state（含 tenantId）     │
 │                         │ 调用 SocialUtils              │
 │←──────────────────────│ 返回授权 URL                   │
 │                         │                              │
 │ 2. 跳转到授权 URL       │                              │
 │─────────────────────────────────────────────────────→│
 │                         │                              │
 │ 3. 用户授权             │                              │
 │←─────────────────────────────────────────────────────│
 │    回调 /socialCallback?code=xxx&state=xxx             │
 │                         │                              │
 │ 4. POST /auth/userLogin                                │
 │    { authType:"social", source, socialCode, socialState }
 │──────────────────────→│                              │
 │                         │ SocialAuthStrategy.login()    │
 │                         │ → SocialUtils.loginAuth()     │
 │                         │ → 查询 sys_social 表          │
 │                         │ → 未绑定 → 自动注册           │
 │                         │ → 创建用户 + 绑定 + 登录      │
 │←──────────────────────│ 返回 accessToken               │
```

### 流程 2：已登录用户绑定社交账号

```
前端                      后端                          第三方平台
 │                         │                              │
 │ 1. GET /auth/socialBindUrl/gitee                       │
 │──────────────────────→│ 返回授权 URL                   │
 │                         │                              │
 │ 2. 跳转授权            │                              │
 │─────────────────────────────────────────────────────→│
 │                         │                              │
 │ 3. 回调               │                              │
 │←─────────────────────────────────────────────────────│
 │                         │                              │
 │ 4. POST /auth/socialBind                               │
 │    { source, socialCode, socialState }                  │
 │──────────────────────→│                              │
 │                         │ SocialUtils.loginAuth()       │
 │                         │ SysLoginService.bindSocialAccount()
 │                         │ → 检查是否已被其他用户绑定    │
 │                         │ → 保存到 sys_social           │
 │←──────────────────────│ 绑定成功                       │
```

### 流程 3：解绑社交账号

```
前端                      后端
 │                         │
 │ DELETE /auth/socialUnbind/{socialId}
 │──────────────────────→│
 │                         │ 删除 sys_social 记录
 │←──────────────────────│ 解绑成功
```

---

## 三、后端核心类

### SocialUtils（核心工具类）

```java
import plus.ruoyi.common.social.utils.SocialUtils;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.request.AuthRequest;

// 1. 获取授权请求对象
AuthRequest authRequest = SocialUtils.getAuthRequest(source, socialProperties);

// 2. 生成授权 URL
String authorizeUrl = authRequest.authorize(state);

// 3. 执行登录认证（获取第三方用户信息）
AuthResponse<AuthUser> response = SocialUtils.loginAuth(source, code, state, socialProperties);
if (response.ok()) {
    AuthUser authUser = response.getData();
    // authUser.getUuid()     - 平台唯一ID
    // authUser.getUsername()  - 用户名
    // authUser.getNickname()  - 昵称
    // authUser.getAvatar()    - 头像
    // authUser.getEmail()     - 邮箱
    // authUser.getToken()     - 令牌信息
}
```

### SocialAuthStrategy（登录策略，328行）

```java
// Bean 名称：socialAuthStrategy
// 注册方式：@Service("social" + IAuthStrategy.BASE_NAME)

// 核心流程：
// 1. 解析 SocialLoginBody
// 2. 调用 SocialUtils.loginAuth() 获取 AuthUser
// 3. 构建 authId = source + uuid
// 4. 查询 sys_social 表
// 5. 未绑定 → 检查自动注册开关 → 自动注册
// 6. 已绑定 → 加载用户信息
// 7. 创建 LoginUser → 生成 Token → 返回
```

### SysLoginService.bindSocialAccount()

```java
// 绑定逻辑（带分布式锁）：
// 1. 构建 authId = source + uuid
// 2. 转换 AuthUser → SysSocialBo
// 3. 检查该第三方账号是否已被其他用户绑定
// 4. 查询当前用户是否已绑定同平台
//    - 未绑定 → 新增记录
//    - 已绑定 → 更新记录
// 5. @Lock4j 防并发（锁 source + uuid）
```

### AuthRedisStateCache（CSRF 防护）

```java
import plus.ruoyi.common.social.utils.AuthRedisStateCache;

// Redis Key: {app.id}:social_auth_codes:{state}
// 过期时间：3 分钟
// 用途：存储 OAuth state 参数，防止 CSRF 攻击
```

---

## 四、认证策略模式

项目使用**策略模式**支持多种认证方式：

```java
// IAuthStrategy 接口
public interface IAuthStrategy {
    String BASE_NAME = "AuthStrategy";

    // 根据 authType 动态选择策略
    static AuthTokenVo login(String body, String authType) {
        IAuthStrategy strategy = SpringUtils.getBean(authType + BASE_NAME);
        return strategy.login(body);
    }

    AuthTokenVo login(String body);
}

// 已有策略实现：
// - passwordAuthStrategy   → 密码登录
// - socialAuthStrategy     → 社交登录
// - smsAuthStrategy        → 短信登录
// - wechatAuthStrategy     → 微信小程序登录
```

前端调用统一入口：

```java
// POST /auth/userLogin
// Body: { "authType": "social", "source": "github", "socialCode": "xxx", "socialState": "xxx" }
```

---

## 五、数据库表（sys_social）

```sql
CREATE TABLE sys_social (
    id                 BIGINT       NOT NULL COMMENT '主键',
    user_id            BIGINT       NOT NULL COMMENT '用户ID',
    tenant_id          VARCHAR(20)  DEFAULT '000000' COMMENT '租户ID',
    auth_id            VARCHAR(255) NOT NULL COMMENT '平台+平台唯一ID',
    source             VARCHAR(255) NOT NULL COMMENT '用户来源（如 github）',
    open_id            VARCHAR(255) DEFAULT NULL COMMENT '平台 OpenID',
    user_name          VARCHAR(30)  NOT NULL COMMENT '登录账号',
    nick_name          VARCHAR(30)  DEFAULT '' COMMENT '用户昵称',
    email              VARCHAR(255) DEFAULT '' COMMENT '用户邮箱',
    avatar             VARCHAR(500) DEFAULT '' COMMENT '头像地址',
    access_token       VARCHAR(255) NOT NULL COMMENT '授权令牌',
    expire_in          INT          DEFAULT NULL COMMENT '令牌有效期',
    refresh_token      VARCHAR(255) DEFAULT NULL COMMENT '刷新令牌',
    union_id           VARCHAR(255) DEFAULT NULL COMMENT 'UnionID',
    scope              VARCHAR(255) DEFAULT NULL COMMENT '授权范围',
    -- 审计字段（继承 TenantEntity）
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='社会化关系表';

-- 索引
CREATE INDEX idx_tenant_user ON sys_social (tenant_id, user_id);
CREATE INDEX idx_auth_id ON sys_social (auth_id);
CREATE INDEX idx_source ON sys_social (source);
```

**关键字段说明**：
- `auth_id`：`source + uuid`，如 `github123456`，用于唯一标识一个第三方账号
- `source`：平台标识，对应 `justauth.type` 配置的 key
- `tenant_id`：多租户隔离，同一社交账号可绑定不同租户

---

## 六、前端集成

### socialConfig.ts（平台配置）

```typescript
// plus-ui/src/api/system/auth/socialConfig.ts

export interface SocialConfig {
  type: string       // 平台标识（对应 source）
  name: string       // 平台名称
  icon: string       // 图标
  color?: string     // 品牌色
}

export const SOCIAL_CONFIGS: SocialConfig[] = [
  { type: 'wechat_open', name: '微信开放平台', icon: 'wechat-fill', color: '#07C160' },
  { type: 'wechat_mp', name: '微信公众号', icon: 'wechat-fill', color: '#07C160' },
  { type: 'wechat_enterprise', name: '企业微信', icon: 'wechat-fill', color: '#0089FF' },
  { type: 'dingtalk', name: '钉钉', icon: 'mdi:alpha-d-box', color: '#0089FF' },
  { type: 'maxkey', name: 'MaxKey', icon: 'maxkey' },
  { type: 'topiam', name: 'TopIAM', icon: 'topiam' },
  { type: 'qq', name: 'QQ', icon: 'mdi:qqchat', color: '#12B7F5' },
  { type: 'gitee', name: 'Gitee', icon: 'gitee', color: '#C71D23' },
  { type: 'github', name: 'GitHub', icon: 'github', color: '#181717' },
  // ... 更多平台
]

// 工具函数
export const getSocialConfig = (type: string): SocialConfig | undefined
export const getSocialConfigs = (types: string | string[]): SocialConfig[]
```

### API 调用示例

```typescript
// 获取授权 URL
const [err, url] = await http.get(`/auth/socialBindUrl/${source}`, { domain, inviteCode })

// 社交登录
const [err, token] = await http.post('/auth/userLogin', {
  authType: 'social',
  source: 'github',
  socialCode: code,    // 回调参数
  socialState: state   // 回调参数
})

// 绑定社交账号（已登录）
const [err] = await http.post('/auth/socialBind', { source, socialCode, socialState })

// 解绑社交账号
const [err] = await http.del(`/auth/socialUnbind/${socialId}`)

// 获取绑定列表
const [err, list] = await getSocialBindingList()
```

---

## 七、自动注册机制

### 配置开关

```
sys_config 表:
key = system.social.auto-register-enabled
value = false（默认关闭）
```

### 自动注册流程（SocialAuthStrategy.autoRegisterAndBind）

```java
// 1. 双重检查防并发
List<SysSocialVo> socialList = sysSocialService.listSocialsByAuthId(authId);
if (CollUtil.isNotEmpty(socialList)) {
    return socialList.get(0);  // 已被其他线程注册
}

// 2. 生成用户信息
String userName = source + "_" + RandomUtil.randomString(8);  // 如 github_a8f3k2m1
String password = RandomUtil.randomString(16);                 // 随机密码
String nickName = authUser.getNickname();                      // 第三方昵称
String avatar = authUser.getAvatar();                          // 第三方头像

// 3. 创建 PC 用户（UserType.PC_USER）
SysUserBo userBo = new SysUserBo();
userBo.setUserName(userName);
userBo.setPassword(password);
userBo.setNickName(nickName);
userBo.setAvatar(avatar);
userBo.setUserType(UserType.PC_USER.getUserType());

// 4. 处理邀请码（如果有）
if (StringUtils.isNotBlank(inviteCode)) {
    // 验证邀请码 → 设置部门 → 分配角色 → 更新使用次数
}

// 5. 保存社交绑定
SysSocialBo socialBo = buildSocialBo(authUser, userId);
sysSocialService.add(socialBo);
```

---

## 八、扩展新平台

### 步骤 1：创建 AuthSource（接口地址）

```java
package plus.ruoyi.common.social.xxx;

import me.zhyd.oauth.config.AuthSource;
import plus.ruoyi.common.core.utils.SpringUtils;

public enum AuthXxxSource implements AuthSource {
    XXX_PLATFORM {
        @Override
        public String authorize() {
            return serverUrl() + "/oauth/authorize";
        }
        @Override
        public String accessToken() {
            return serverUrl() + "/oauth/token";
        }
        @Override
        public String userInfo() {
            return serverUrl() + "/api/user";
        }
    };

    private static String serverUrl() {
        return SpringUtils.getProperty("justauth.type.xxx.server-url");
    }
}
```

### 步骤 2：创建 AuthRequest（认证逻辑）

```java
package plus.ruoyi.common.social.xxx;

import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthToken;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthDefaultRequest;

public class AuthXxxRequest extends AuthDefaultRequest {

    public AuthXxxRequest(AuthConfig config, AuthStateCache stateCache) {
        super(config, AuthXxxSource.XXX_PLATFORM, stateCache);
    }

    @Override
    protected AuthToken getAccessToken(AuthCallback authCallback) {
        // POST 请求获取 access_token
        String response = new HttpUtils(config.getHttpConfig())
            .post(source.accessToken())
            .form("grant_type", "authorization_code")
            .form("code", authCallback.getCode())
            .form("client_id", config.getClientId())
            .form("client_secret", config.getClientSecret())
            .form("redirect_uri", config.getRedirectUri())
            .execute();

        // 解析响应
        Dict dict = JsonUtils.parseMap(response);
        return AuthToken.builder()
            .accessToken(dict.getStr("access_token"))
            .tokenType(dict.getStr("token_type"))
            .expireIn(dict.getInt("expires_in"))
            .build();
    }

    @Override
    protected AuthUser getUserInfo(AuthToken authToken) {
        // GET 请求获取用户信息
        String response = new HttpUtils(config.getHttpConfig())
            .get(source.userInfo())
            .header("Authorization", "Bearer " + authToken.getAccessToken())
            .execute();

        Dict dict = JsonUtils.parseMap(response);
        return AuthUser.builder()
            .rawUserInfo(dict)
            .uuid(dict.getStr("id"))
            .username(dict.getStr("login"))
            .nickname(dict.getStr("name"))
            .avatar(dict.getStr("avatar_url"))
            .email(dict.getStr("email"))
            .token(authToken)
            .source(source.toString())
            .build();
    }
}
```

### 步骤 3：注册到 SocialUtils

在 `SocialUtils.getAuthRequest()` 中添加 case：

```java
case "xxx" -> new AuthXxxRequest(
    AuthConfig.builder()
        .clientId(obj.getClientId())
        .clientSecret(obj.getClientSecret())
        .redirectUri(obj.getRedirectUri())
        .build(),
    authStateCache
);
```

### 步骤 4：添加前端配置

在 `socialConfig.ts` 中添加：

```typescript
{ type: 'xxx', name: 'Xxx Platform', icon: 'xxx-icon', color: '#000000' }
```

### 步骤 5：添加 application.yml 配置

```yaml
justauth:
  type:
    xxx:
      server-url: ${XXX_SERVER_URL:https://xxx.example.com}
      client-id: ${XXX_CLIENT_ID:xxxxxxxx}
      client-secret: ${XXX_CLIENT_SECRET:xxxxxxxx}
      redirect-uri: ${justauth.address}/socialCallback?source=xxx
```

---

## 九、常见错误与最佳实践

### ✅ 正确做法

```java
// 1. 使用 SocialUtils 而非直接操作 JustAuth
AuthResponse<AuthUser> response = SocialUtils.loginAuth(source, code, state, socialProperties);

// 2. 检查授权响应状态
if (!response.ok()) {
    throw new ServiceException.of("社交登录授权失败: " + response.getMsg());
}

// 3. 社交绑定使用分布式锁防并发
@Lock4j(keys = {"#authUserData.source", "#authUserData.uuid"}, expire = 10000)
public void bindSocialAccount(AuthUser authUserData) { ... }

// 4. 自动注册使用双重检查
List<SysSocialVo> existing = sysSocialService.listSocialsByAuthId(authId);
if (CollUtil.isNotEmpty(existing)) {
    return existing.get(0);  // 已被其他线程注册
}

// 5. 回调地址必须与第三方平台配置一致
// justauth.address 必须是前端可访问的外网地址
```

### ❌ 常见错误

```java
// 1. 直接使用 JustAuth API（绕过项目封装）
AuthGiteeRequest request = new AuthGiteeRequest(config);  // ❌
SocialUtils.getAuthRequest("gitee", socialProperties);     // ✅

// 2. 忘记检查授权响应
AuthUser authUser = response.getData();  // ❌ 可能 NPE
if (!response.ok()) { throw ... }        // ✅ 先检查

// 3. 回调地址不匹配
// 第三方平台配置：https://example.com/callback
// application.yml：http://localhost/socialCallback   // ❌ 域名不一致
// 两者必须完全一致！

// 4. 硬编码 state 参数
String state = "fixed-state";  // ❌ CSRF 风险
String state = AuthStateUtils.createState();  // ✅ 随机生成

// 5. 社交登录自动注册忘记开启
// sys_config 表中 system.social.auto-register-enabled = 'false'
// 未绑定用户会看到"请先绑定账号"提示
```

---

## 十、多租户支持

### 数据隔离

- `sys_social` 表包含 `tenant_id` 字段
- 同一社交账号可绑定到不同租户
- 查询自动按当前租户过滤

### 登录时租户匹配

```java
// SocialAuthStrategy.login() 中：
if (TenantHelper.isEnable()) {
    // 从 socialState 中解析 tenantId
    String tenantId = parseTenantId(socialState);

    // 查找与当前租户匹配的绑定记录
    List<SysSocialVo> matchList = socialList.stream()
        .filter(s -> tenantId.equals(s.getTenantId()))
        .toList();

    if (matchList.isEmpty()) {
        throw new ServiceException.of("对不起，你没有权限登录当前租户！");
    }
}
```

### State 参数编码

```java
// 构建状态参数（AuthController.socialBindUrl）
Dict stateData = Dict.create()
    .set("tenantId", tenantId)
    .set("domain", domain)
    .set("state", AuthStateUtils.createState())
    .set("inviteCode", inviteCode);

// Base64 编码
String encodedState = Base64.encode(JsonUtils.toJsonString(stateData));
```

---

## 十一、API 接口汇总

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/auth/socialBindUrl/{source}` | 不需要 | 获取授权跳转 URL |
| POST | `/auth/userLogin` | 不需要 | 统一登录（authType=social） |
| POST | `/auth/socialBind` | 需登录 | 绑定第三方账号 |
| DELETE | `/auth/socialUnbind/{socialId}` | 需登录 | 解绑第三方账号 |
| GET | `/system/social/getSocialBindingList` | 需登录 | 查询当前用户绑定列表 |

---

## 十二、与其他技能的关系

| 技能 | 关系 |
|------|------|
| `wechat-integration` | 微信小程序登录走 wechat 策略，非 social 策略 |
| `security-guard` | Sa-Token 令牌生成、登录认证 |
| `redis-cache` | OAuth state 缓存、分布式锁 |
| `multi-tenant` | 社交绑定数据的租户隔离 |
| `crud-development` | SysSocial 实体遵循标准四层架构 |

---

## 十三、参考文件索引

### 后端核心

| 文件 | 行数 | 说明 |
|------|------|------|
| `common/social/utils/SocialUtils.java` | 102 | 核心工具类（27+平台路由） |
| `common/social/utils/AuthRedisStateCache.java` | 65 | Redis 状态缓存 |
| `common/social/config/properties/SocialLoginConfigProperties.java` | 76 | 单平台配置属性 |
| `system/auth/service/impl/SocialAuthStrategy.java` | 328 | 社交登录策略（核心） |
| `system/auth/controller/AuthController.java` | 292 | 认证控制器 |
| `system/auth/service/SysLoginService.java` | 315 | 绑定账号逻辑 |
| `system/core/domain/SysSocial.java` | 137 | 社交绑定实体 |

### 自建平台扩展

| 文件 | 行数 | 说明 |
|------|------|------|
| `common/social/gitea/AuthGiteaRequest.java` | 132 | Gitea 认证 |
| `common/social/maxkey/AuthMaxKeyRequest.java` | 114 | MaxKey 认证 |
| `common/social/topiam/AuthTopIamRequest.java` | 172 | TopIAM 认证 |
| `me/zhyd/oauth/request/AbstractAuthWeChatEnterpriseRequest.java` | 181 | 企业微信 |
| `me/zhyd/oauth/request/AuthDingTalkV2Request.java` | 110 | 钉钉V2 |

### 前端

| 文件 | 说明 |
|------|------|
| `plus-ui/src/api/system/auth/socialConfig.ts` | 平台图标/名称/颜色配置 |
| `plus-ui/src/api/system/core/social/socialApi.ts` | 社交绑定 API |
| `plus-ui/src/api/system/core/social/socialTypes.ts` | 类型定义 |

---

## 十四、FAQ

### Q1: 如何快速接入一个新的社交平台？

**A**: 参考"八、扩展新平台"章节的 5 步流程：创建 AuthSource → 创建 AuthRequest → 注册到 SocialUtils → 添加前端配置 → 添加 yml 配置。如果 JustAuth 已支持该平台，只需在 SocialUtils 中添加 case 和配置即可。

### Q2: 社交登录和密码登录有什么区别？

**A**: 两者通过策略模式区分。统一入口 `POST /auth/userLogin`，前端传不同的 `authType`：
- `authType: "password"` → `PasswordAuthStrategy`
- `authType: "social"` → `SocialAuthStrategy`
- `authType: "sms"` → `SmsAuthStrategy`
- `authType: "wechat"` → `WechatAuthStrategy`

### Q3: 社交登录时未绑定账号怎么办？

**A**: 取决于 `system.social.auto-register-enabled` 配置：
- `false`（默认）：提示用户需要先注册/登录后绑定
- `true`：自动注册新用户并绑定，用户名格式为 `{source}_{随机8位}`

### Q4: 同一个第三方账号能绑定多个租户吗？

**A**: 可以。`sys_social` 表按 `tenant_id` 隔离，同一社交账号在不同租户下独立绑定。登录时根据当前租户 ID 匹配绑定记录。

### Q5: 回调地址报错怎么办？

**A**: 检查以下几点：
1. `justauth.address` 配置的域名与第三方平台后台配置完全一致
2. 包括协议（http/https）、域名、端口号
3. 回调路径格式：`{address}/socialCallback?source={平台标识}`
4. 开发环境可用 ngrok 等工具暴露本地服务

### Q6: State 参数有什么作用？

**A**: 防止 CSRF 攻击。流程：
1. 生成随机 state → 存入 Redis（3 分钟过期）
2. 携带 state 跳转到第三方授权页面
3. 回调时验证 state 与 Redis 中的值匹配
4. 本项目还在 state 中编码了 `tenantId`、`domain`、`inviteCode` 等业务参数
