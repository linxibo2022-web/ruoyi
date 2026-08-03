---
name: file-oss-management
description: |
  当需要进行文件上传、下载、存储管理时自动使用此 Skill。支持本地存储、阿里云OSS、腾讯云COS、七牛云、MinIO等。

  触发场景：
  - 文件上传下载
  - 云存储配置
  - 预签名URL生成
  - 文件元数据管理
  - 图片处理

  触发词：文件上传、OSS、云存储、MinIO、阿里云、腾讯云、七牛、图片上传、文件下载、预签名、presigned
---

# 文件与云存储指南

## 支持的存储类型

| 类型 | 枚举值 | 说明 |
|------|-------|------|
| 本地存储 | `LOCAL` | 存储到服务器本地目录 |
| 阿里云OSS | `ALIYUN` | 阿里云对象存储 |
| 腾讯云COS | `QCLOUD` | 腾讯云对象存储 |
| 七牛云 | `QINIU` | 七牛云存储 |
| MinIO | `MINIO` | 开源对象存储 |
| 华为云OBS | `OBS` | 华为云对象存储 |

---

## 核心类

| 类 | 说明 |
|----|------|
| `OssFactory` | 获取 OssClient 实例 |
| `OssClient` | 统一操作入口 |
| `OssStrategy` | 存储策略接口 |
| `UploadResult` | 上传结果 |

---

## 基础使用

### 获取 OssClient

```java
import plus.ruoyi.common.oss.factory.OssFactory;
import plus.ruoyi.common.oss.core.OssClient;
import plus.ruoyi.common.oss.enums.OssType;

// 获取默认配置的客户端
OssClient client = OssFactory.instance();

// 获取指定类型的客户端
OssClient client = OssFactory.instance(OssType.ALIYUN);

// 根据配置键获取
OssClient client = OssFactory.instance("aliyun");
```

### 文件上传

```java
import plus.ruoyi.common.oss.dto.UploadResult;

// 1. 上传本地文件
File file = new File("/path/to/file.jpg");
UploadResult result = client.uploadFile(file, "images/avatar.jpg", "image/jpeg");

// 2. 上传文件，自动生成路径（推荐）
UploadResult result = client.uploadSuffix(file, ".jpg");
// 结果：/2024/12/01/uuid.jpg

// 3. 上传到指定模块目录
UploadResult result = client.uploadSuffix(file, ".jpg", "avatar");
// 结果：/avatar/2024/12/01/uuid.jpg

// 4. 上传输入流
InputStream is = multipartFile.getInputStream();
UploadResult result = client.uploadStream(is, "images/photo.jpg", fileSize, "image/jpeg");

// 5. 上传字节数组
byte[] data = multipartFile.getBytes();
UploadResult result = client.uploadSuffix(data, ".png", "image/png");

// 6. 上传到指定模块
UploadResult result = client.uploadSuffix(data, ".png", "product", "image/png");
```

### UploadResult 字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `url` | String | 文件访问URL |
| `fileName` | String | 文件名 |
| `fileSize` | Long | 文件大小（字节） |
| `eTag` | String | 文件校验标记 |

---

## 文件下载

```java
// 下载到临时文件
Path tempFile = client.downloadToTempFile("images/photo.jpg");

// 下载到输出流
OutputStream out = response.getOutputStream();
client.downloadToStream("images/photo.jpg", out, progress -> {
    // 进度回调
    log.info("已下载: {} bytes", progress);
});

// 获取文件流
InputStream is = client.getFileAsStream("images/photo.jpg");
```

---

## 文件操作

```java
// 删除文件
client.deleteFile("images/photo.jpg");

// 复制文件
client.copyFile("images/source.jpg", "images/target.jpg");

// 获取文件元数据
OssFileMetadata metadata = client.getFileMetadata("images/photo.jpg");
log.info("大小: {}, 类型: {}", metadata.getContentLength(), metadata.getContentType());

// 列出文件
List<OssFileInfo> files = client.listFiles("images/", 100);
for (OssFileInfo file : files) {
    log.info("文件: {}, 大小: {}", file.getKey(), file.getSize());
}
```

---

## 预签名URL

### 下载预签名URL

```java
import java.time.Duration;

// 生成60分钟有效的预签名下载URL
String presignedUrl = client.generatePresignedUrl("images/photo.jpg", Duration.ofMinutes(60));

// 生成公共访问URL（需要文件是公开的）
String publicUrl = client.generatePublicUrl("images/photo.jpg");
```

### 上传预签名URL（前端直传）

```java
// 生成预签名上传URL，用于前端直传
String uploadUrl = client.generatePresignedUploadUrl(
    "images/upload.jpg",   // 对象键
    "image/jpeg",          // Content-Type
    3600                   // 有效期（秒）
);

// 前端使用此 URL 直接 PUT 上传
```

---

## Controller 示例

### 通用文件上传

```java
@RestController
@RequestMapping("/system/oss")
public class OssController {

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    public R<UploadResult> upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw ServiceException.of("上传文件不能为空");
        }

        OssClient client = OssFactory.instance();
        String suffix = FileUtils.getSuffix(file.getOriginalFilename());
        UploadResult result = client.uploadSuffix(
            file.getInputStream(),
            suffix,
            file.getSize(),
            file.getContentType()
        );

        return R.ok(result);
    }

    /**
     * 上传到指定模块
     */
    @PostMapping("/upload/{module}")
    public R<UploadResult> uploadToModule(
            @PathVariable String module,
            MultipartFile file) {

        OssClient client = OssFactory.instance();
        String suffix = FileUtils.getSuffix(file.getOriginalFilename());
        UploadResult result = client.uploadSuffix(
            file.getInputStream(),
            suffix,
            module,
            file.getSize(),
            file.getContentType()
        );

        return R.ok(result);
    }

    /**
     * 下载文件
     */
    @GetMapping("/download")
    public void download(@RequestParam String path, HttpServletResponse response) {
        OssClient client = OssFactory.instance();

        // 获取文件元数据
        OssFileMetadata metadata = client.getFileMetadata(path);
        response.setContentType(metadata.getContentType());
        response.setHeader("Content-Disposition",
            "attachment; filename=" + URLEncoder.encode(path, StandardCharsets.UTF_8));

        // 下载到响应流
        client.downloadToStream(path, response.getOutputStream(), null);
    }

    /**
     * 获取预签名URL
     *
     * ⚠️ 注意：R.ok(String) 会把字符串放到 msg 字段而不是 data 字段！
     * 因为 R.ok(String msg) 方法优先于泛型 R.ok(T data) 方法匹配。
     * 所以必须使用 R.ok(null, url) 明确指定 data。
     */
    @GetMapping("/getPresignedUrl")
    public R<String> getPresignedUrl(@RequestParam String path,
                                     @RequestParam(defaultValue = "60") int minutes) {
        OssClient client = OssFactory.instance();
        String url = client.generatePresignedUrl(path, Duration.ofMinutes(minutes));
        // ✅ 正确：使用 R.ok(null, url) 把 url 放到 data 字段
        return R.ok(null, url);
        // ❌ 错误：R.ok(url) 会调用 R.ok(String msg)，url 会被放到 msg 字段
    }
}
```

---

## 移动端文件上传（plus-uniapp / plus-app）

> **核心规范**：项目已封装两套上传机制，**禁止**直接调用 `uni.uploadFile`。

### 🔴 上传方式决策树

```
有 UI（图片墙 / 头像框 / 附件列表 / 表单字段）？
├─ 是 → 使用 <wd-upload> 组件（零配置）
└─ 否（程序化上传：拍照后直传、裁剪后上传、分片等）
      → 使用 http.upload()
```

### 三种方式对比

| 方式 | 位置 | 自动注入 | 使用场景 |
|------|------|---------|---------|
| **`<wd-upload>`** | `@/wd` 组件 | ✅ action 默认 `/resource/oss/upload`、header 默认 Token | **UI 场景首选** |
| **`http.upload()`** | `composables/useHttp.ts` | ✅ baseUrl / Token / Content-Language / 统一错误 | 程序化上传 |
| `uni.uploadFile` | 原生 API | ❌ 全部要手撸 | **禁止直接使用** |

---

### 方式 1：`<wd-upload>` 组件（UI 首选）

```vue
<template>
  <!-- ✅ action 和 header 都有默认值，零配置即可 -->
  <wd-upload v-model:file-list="fileList" :limit="9" />

  <!-- 如需指定模块（如头像、附件），覆盖 action -->
  <wd-upload
    v-model:file-list="avatarList"
    :action="`${apiBase}/resource/oss/upload/avatar`"
    :limit="1"
  />
</template>

<script setup lang="ts">
const fileList = ref([])
const avatarList = ref([])
const apiBase = import.meta.env.VITE_API_BASE_URL
</script>
```

**默认行为**（见 `wd-upload.vue:1295-1296`）：
- `action` = `${baseUrl}/resource/oss/upload`
- `header` = `useToken().getAuthHeaders()`（自动注入 Token）

### 方式 2：`http.upload()`（程序化上传）

```typescript
// 适用于：选图后直传、裁剪后上传、自定义进度、上传队列等
import { useToast } from '@/wd'

const toast = useToast()

const handleUpload = async () => {
  // 1. 选择图片
  const [chooseErr, chooseRes] = await uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera']
  })
  if (chooseErr || !chooseRes?.tempFilePaths?.length) return

  // 2. 上传（✅ http.upload 自动处理 Token / baseUrl / 错误）
  toast.loading('上传中...')
  const [err, result] = await http.upload<UploadResult>({
    url: '/resource/oss/upload/avatar',  // 相对路径，baseUrl 自动拼接
    filePath: chooseRes.tempFilePaths[0],
    name: 'file',
    formData: { /* 可选附加字段 */ }
  })
  toast.close()

  if (!err) {
    imageUrl.value = result.url
    toast.success('上传成功')
  }
}
```

`UploadResult` 字段：`{ url, fileName, fileSize, eTag, ossId? }`

### ❌ 错误示例（禁止）

```typescript
// ❌ 禁止：直接用 uni.uploadFile，要手动处理 Token / URL / 错误 / 加密
uni.uploadFile({
  url: `${baseUrl}/system/oss/upload/avatar`,
  filePath,
  name: 'file',
  header: { Authorization: `Bearer ${token}` }
})

// ❌ 禁止：用 http.post 传 FormData 上传文件
// uniapp 的 request 不支持 FormData，必须用 http.upload
const formData = new FormData()
formData.append('file', file)
http.post('/resource/oss/upload', formData)
```

### API 定义（可选封装）

如需在 `api/` 层封装业务上传接口：

```typescript
// api/system/oss/ossApi.ts

// 预签名 URL（非文件上传，用于前端直传 OSS 场景）
export const getPresignedUrl = (path: string, minutes?: number): Result<string> => {
  return http.get('/system/oss/getPresignedUrl', { path, minutes })
}

// 大多数场景直接在页面调用 http.upload() 即可，无需在 api/ 层封装
```

---

## 配置说明

### 数据库配置表（sys_oss_config）

| 字段 | 说明 |
|------|------|
| `config_key` | 配置标识（如 aliyun、minio） |
| `access_key` | Access Key |
| `secret_key` | Secret Key |
| `bucket_name` | 存储桶名称 |
| `prefix` | 路径前缀 |
| `endpoint` | 服务端点 |
| `domain` | 自定义域名 |
| `is_https` | 是否HTTPS |
| `region` | 区域 |
| `access_policy` | 访问策略（public/private） |
| `status` | 启用状态 |

### 访问策略

| 策略 | 说明 |
|------|------|
| `PUBLIC` | 公开访问，URL 直接可访问 |
| `PRIVATE` | 私有访问，需要预签名URL |

---

## @SerialMap 集成

### OSS ID 转 URL

```java
import plus.ruoyi.common.serialmap.annotation.SerialMap;
import plus.ruoyi.common.serialmap.constant.SerialMapConstant;

public class ProductVo {

    // OSS 文件ID 自动转换为访问URL
    @SerialMap(converter = SerialMapConstant.OSS_ID_TO_URL)
    private String imageId;  // 存储的是文件ID

    // 支持多个ID（逗号分隔）
    @SerialMap(converter = SerialMapConstant.OSS_ID_TO_URL)
    private String imageIds;  // 如："1,2,3" → "url1,url2,url3"
}
```

### 私有文件预签名URL

```java
public class DocumentVo {

    // 私有文件自动生成预签名URL
    @SerialMap(converter = SerialMapConstant.PRESIGNED_URL)
    private String privateFileUrl;  // /private/doc.pdf → https://xxx?sign=xxx
}
```

---

## 最佳实践

### 1. 文件路径规范

```java
// ✅ 推荐：使用 uploadSuffix 自动生成路径
client.uploadSuffix(file, ".jpg", "avatar");
// 结果：/avatar/2024/12/01/550e8400-e29b.jpg

// ❌ 避免：手动拼接路径
client.uploadFile(file, "avatar/" + fileName, contentType);
```

### 2. 大文件处理

```java
// 大文件建议使用流式上传
try (InputStream is = new FileInputStream(largeFile)) {
    client.uploadStream(is, key, fileSize, contentType);
}
```

### 3. 文件类型校验

```java
// 限制允许的文件类型
private static final Set<String> ALLOWED_TYPES = Set.of(
    "image/jpeg", "image/png", "image/gif", "application/pdf"
);

public UploadResult upload(MultipartFile file) {
    if (!ALLOWED_TYPES.contains(file.getContentType())) {
        throw ServiceException.of("不支持的文件类型");
    }
    // 继续上传...
}
```

### 4. 文件大小限制

```yaml
# application.yml
spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 20MB
```

---

## 常见问题

### ⚠️ R.ok(String) 返回值陷阱

```java
// ❌ 错误：字符串会被放到 msg 字段，data 为 null
String url = "https://example.com/file.jpg";
return R.ok(url);
// 结果：{ code: 200, msg: "https://example.com/file.jpg", data: null }

// ✅ 正确：使用 R.ok(null, url) 明确指定 data
return R.ok(null, url);
// 结果：{ code: 200, msg: null, data: "https://example.com/file.jpg" }
```

**原因**：`R.ok(String msg)` 方法优先于泛型 `R.ok(T data)` 匹配，Java 编译器会选择更具体的方法。

### 上传失败：Access Denied

- 检查 Access Key 和 Secret Key
- 检查存储桶权限配置
- 检查 IP 白名单

### 预签名URL无效

- 检查服务器时间是否同步
- 检查 URL 有效期是否过期
- 私有桶必须使用预签名URL

### 跨域问题

配置存储桶 CORS 规则：
- AllowedOrigins: `*` 或具体域名
- AllowedMethods: `GET, PUT, POST, DELETE`
- AllowedHeaders: `*`
