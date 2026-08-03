# 开放API平台使用说明

## 📖 功能概述

本系统实现了完整的开放API功能，支持通过 **AppKey + AppSecret** 方式进行API认证，与现有的 Sa-Token JWT 认证并行运行，互不干扰。

## 🚀 快速开始

### 1. 启用开放平台

在 `application.yml` 或环境变量中配置：

```yaml
openapi:
  enabled: true  # 启用开放平台
  timestamp-expire-seconds: 60  # 时间戳过期时间(秒)
```

或使用环境变量：
```bash
OPEN_API_ENABLED=true
```

### 2. 执行数据库脚本

执行 `script/sql/ry_plus_new.sql` 中的建表和菜单SQL。

### 3. 生成API密钥

1. 登录管理后台
2. 进入 `系统管理` → `开放平台` → `API密钥`
3. 点击 `生成密钥` 按钮
4. 填写应用信息：
   - **应用名称**: 必填
   - **关联用户**: 可选，用于权限继承
   - **授权权限**: 选择该密钥可访问的接口权限
   - **过期时间**: 可选
   - **IP白名单**: 可选，多个IP用逗号分隔
5. 提交后系统会显示 **AppKey** 和 **AppSecret**（仅显示一次，请妥善保管）

## 🔐 接口认证

### 标识开放接口

在Controller方法或类上添加 `@OpenApi` 注解：

```java
@OpenApi("用户查询接口")
@SaCheckPermission("system:user:query")
@GetMapping("/list")
public R<List<SysUserVo>> list() {
    // ...
}
```

### 客户端调用

#### 1. 计算签名

签名算法：`MD5(appKey + timestamp + appSecret)`

```java
// Java示例
String appKey = "your_app_key";
String appSecret = "your_app_secret";
long timestamp = System.currentTimeMillis();
String sign = DigestUtil.md5Hex(appKey + timestamp + appSecret);
```

```javascript
// JavaScript示例
import md5 from 'md5'

const appKey = 'your_app_key'
const appSecret = 'your_app_secret'
const timestamp = Date.now()
const sign = md5(appKey + timestamp + appSecret)
```

#### 2. 发送请求

在请求头中添加以下参数：

```http
X-App-Key: your_app_key
X-Timestamp: 1234567890
X-Sign: computed_sign_value
```

完整请求示例：

```bash
curl -X GET "http://localhost:5500/system/user/list" \
  -H "X-App-Key: abc123" \
  -H "X-Timestamp: 1609459200000" \
  -H "X-Sign: e10adc3949ba59abbe56e057f20f883e"
```

## 🛡️ 安全机制

### 1. 签名验证
- 使用 MD5(appKey + timestamp + appSecret) 生成签名
- AppSecret 在数据库中使用 BCrypt 加密存储

### 2. 时间戳防重放
- 默认时间戳有效期60秒
- 超时请求会被拒绝

### 3. 签名防重复
- 已验证的签名会在Redis中缓存60秒
- 重复签名的请求会被拒绝

### 4. 权限控制
- 基于 `@SaCheckPermission` 权限编码
- 仅允许访问已授权的接口

### 5. IP白名单
- 支持配置IP白名单
- 仅白名单内的IP可访问

### 6. 过期控制
- 支持设置密钥过期时间
- 过期密钥自动失效

## 📊 管理功能

### 密钥管理
- ✅ 生成新密钥
- ✅ 修改密钥配置
- ✅ 重置密钥(重新生成AppSecret)
- ✅ 启用/禁用密钥
- ✅ 删除密钥
- ✅ 查看调用统计

### 权限配置
- 树形选择器配置授权权限
- 权限编码与 `@SaCheckPermission` 一致
- 支持精细化的接口级权限控制

## 🔧 高级配置

### 自定义签名算法

如需使用其他签名算法，修改 `OpenApiSignUtils`:

```java
public static String generateSign(String appKey, String timestamp, String appSecret) {
    // 自定义签名逻辑
    return DigestUtil.sha256Hex(appKey + timestamp + appSecret);
}
```

### 自定义拦截器

可以扩展 `OpenApiInterceptor` 添加更多验证逻辑：

```java
@Override
public boolean preHandle(HttpServletRequest request,
                        HttpServletResponse response,
                        Object handler) {
    // 调用父类验证
    boolean result = super.preHandle(request, response, handler);

    // 添加自定义验证
    // ...

    return result;
}
```

## 📝 使用示例

### Python客户端示例

```python
import requests
import hashlib
import time

class OpenApiClient:
    def __init__(self, base_url, app_key, app_secret):
        self.base_url = base_url
        self.app_key = app_key
        self.app_secret = app_secret

    def _generate_sign(self, timestamp):
        content = f"{self.app_key}{timestamp}{self.app_secret}"
        return hashlib.md5(content.encode()).hexdigest()

    def request(self, method, path, **kwargs):
        timestamp = str(int(time.time() * 1000))
        sign = self._generate_sign(timestamp)

        headers = {
            'X-App-Key': self.app_key,
            'X-Timestamp': timestamp,
            'X-Sign': sign
        }

        url = f"{self.base_url}{path}"
        return requests.request(method, url, headers=headers, **kwargs)

# 使用示例
client = OpenApiClient(
    base_url='http://localhost:5500',
    app_key='your_app_key',
    app_secret='your_app_secret'
)

response = client.request('GET', '/system/user/list')
print(response.json())
```

## ⚠️ 注意事项

1. **AppSecret 安全**
   - AppSecret 仅在生成时显示一次
   - 请妥善保管，不要泄露
   - 如需重置，使用"重置密钥"功能

2. **时间戳同步**
   - 客户端与服务器时间差不能超过60秒
   - 建议使用NTP同步时间

3. **签名计算**
   - 签名字符串拼接顺序：appKey + timestamp + appSecret
   - 使用MD5算法
   - 区分大小写

4. **性能优化**
   - 密钥信息会在Redis中缓存1小时
   - 权限验证结果会缓存5分钟
   - 异步记录调用统计

## 🐛 故障排查

### 常见错误

| 错误信息 | 原因 | 解决方案 |
|---------|------|---------|
| 开放平台功能未启用 | `enabled: false` | 修改配置启用 |
| 缺少认证参数 | 缺少请求头 | 添加 X-App-Key, X-Timestamp, X-Sign |
| 请求已过期 | 时间戳超时 | 同步客户端时间 |
| 请求重复 | 签名重复 | 使用新的时间戳 |
| 无效的AppKey | 密钥不存在或已禁用 | 检查密钥状态 |
| 签名验证失败 | 签名计算错误 | 检查签名算法 |
| IP地址不在白名单中 | IP限制 | 添加IP到白名单 |
| 无接口访问权限 | 权限不足 | 配置接口权限 |

## 📚 API文档

所有标记了 `@OpenApi` 的接口都会自动出现在 Swagger 文档中，可通过以下地址访问：

```
http://localhost:5500/doc.html
```

## 🔄 版本更新

### v1.0.0 (2025-10-03)
- ✅ 初始版本
- ✅ 支持 AppKey + AppSecret 认证
- ✅ 签名验证 + 时间戳防重放
- ✅ 权限控制
- ✅ 密钥管理界面
- ✅ 调用统计
