
# 媒体处理指南

## 模块概览

| 模块 | 位置 | 功能 |
|------|------|------|
| ruoyi-common-media | 媒体处理 | 图片、二维码、GIF、海报 |
| ruoyi-common-excel | Excel处理 | 导入、导出、模板 |
| ruoyi-common-core | 文件工具 | 文件类型判断、下载 |

---

## 图片处理

### ImageUtils 基础操作

```java
import plus.ruoyi.common.media.utils.ImageUtils;
import plus.ruoyi.common.media.enums.ResizeMode;

// 从 URL 加载图片
BufferedImage image = ImageUtils.loadFromUrl("https://example.com/image.jpg");

// 从字节数组加载
BufferedImage image = ImageUtils.loadFromBytes(imageBytes);

// 图片缩放（三种模式）
BufferedImage resized = ImageUtils.resize(image, 300, 300, ResizeMode.FIT);

// 创建圆形头像
BufferedImage circle = ImageUtils.createCircleImage(image, 100);

// 转为字节数组
byte[] bytes = ImageUtils.toBytes(image, "png");
```

### 缩放模式说明

| 模式 | 枚举值 | 说明 |
|------|-------|------|
| 等比缩放 | `FIT` | 保持宽高比，可能有空白 |
| 填充裁剪 | `FILL` | 填满区域，超出部分裁剪 |
| 拉伸变形 | `STRETCH` | 强制拉伸，可能变形 |

### ImageBuilder 链式处理

```java
import plus.ruoyi.common.media.builder.ImageBuilder;
import plus.ruoyi.common.media.options.WatermarkOptions;
import plus.ruoyi.common.media.options.FilterOptions;
import plus.ruoyi.common.media.enums.OutputFormat;
import plus.ruoyi.common.media.enums.ResizeMode;

// 完整示例：缩放 + 水印 + 滤镜
BufferedImage result = ImageBuilder.of("image.jpg")
    .resize(600, 400, ResizeMode.FIT)          // 缩放
    .addWatermark(WatermarkOptions.text("©2024")  // 文字水印
        .position(WatermarkOptions.Position.BOTTOM_RIGHT)
        .opacity(0.6f))
    .applyFilter(new FilterOptions().setGrayscale(true))  // 灰度滤镜
    .format(OutputFormat.PNG)                  // 输出格式
    .build();                                  // 构建

// 从 URL 加载并处理
byte[] bytes = ImageBuilder.of("https://example.com/image.jpg")
    .resize(300, 300)
    .toBytes();

// 输出到文件
ImageBuilder.of(inputStream)
    .rotate(45)                                // 旋转
    .flipHorizontal()                          // 水平翻转
    .save("/path/output.png");

// 输出到流
ImageBuilder.of(image)
    .writeTo(outputStream);
```

### 水印配置

```java
import plus.ruoyi.common.media.options.WatermarkOptions;
import plus.ruoyi.common.media.options.WatermarkOptions.Position;

// 文字水印
WatermarkOptions textWm = WatermarkOptions.text("版权所有")
    .position(Position.BOTTOM_RIGHT)   // 位置
    .offsetX(10).offsetY(10)           // 偏移
    .opacity(0.6f)                     // 透明度
    .font(new Font("微软雅黑", Font.PLAIN, 14))
    .textColor(Color.GRAY);

// 图片水印
WatermarkOptions imageWm = WatermarkOptions.image("/path/logo.png")
    .position(Position.TOP_RIGHT)
    .opacity(0.8f)
    .size(100, 50);                    // 固定尺寸

// 自动调整大小
WatermarkOptions autoWm = WatermarkOptions.image(logoPath)
    .autoSize(true);                   // 根据原图自动调整
```

### 水印位置

| 位置 | 枚举值 |
|------|-------|
| 左上 | `TOP_LEFT` |
| 上中 | `TOP_CENTER` |
| 右上 | `TOP_RIGHT` |
| 左中 | `CENTER_LEFT` |
| 正中 | `CENTER` |
| 右中 | `CENTER_RIGHT` |
| 左下 | `BOTTOM_LEFT` |
| 下中 | `BOTTOM_CENTER` |
| 右下 | `BOTTOM_RIGHT` |

---

## 二维码生成

### QrCodeUtils 快速生成

```java
import plus.ruoyi.common.media.utils.QrCodeUtils;

// 基础二维码
BufferedImage qr = QrCodeUtils.generate("https://example.com");

// 指定尺寸
BufferedImage qr = QrCodeUtils.generate("内容", 256);

// 带 Logo
BufferedImage qr = QrCodeUtils.generateWithLogo(
    "https://example.com",
    256,
    "/path/logo.png"
);

// 彩色二维码
BufferedImage qr = QrCodeUtils.generateColorful(
    "内容",
    256,
    Color.BLUE,     // 前景色
    Color.WHITE     // 背景色
);

// 保存到文件
QrCodeUtils.saveQrCode("内容",
    new QrCodeOptions().setSize(256),
    "/path/qr.png");
```

### QrCodeBuilder 链式构建

```java
import plus.ruoyi.common.media.builder.QrCodeBuilder;

BufferedImage qr = QrCodeBuilder.of("https://example.com")
    .size(300)                          // 尺寸
    .margin(2)                          // 边距
    .colors(Color.BLACK, Color.WHITE)   // 前景/背景色
    .logo("/path/logo.png", 0.25f)      // Logo 及占比
    .build();
```

---

## GIF 动画

### GifUtils 快速创建

```java
import plus.ruoyi.common.media.utils.GifUtils;
import plus.ruoyi.common.media.options.AnimationOptions;

List<BufferedImage> frames = new ArrayList<>();
frames.add(frame1);
frames.add(frame2);
frames.add(frame3);

// 快速创建（每帧 100ms）
byte[] gif = GifUtils.createGif(frames, 100);

// 指定尺寸
byte[] gif = GifUtils.createGif(frames, 800, 600, 100);

// 使用配置
AnimationOptions options = new AnimationOptions()
    .setWidth(800)
    .setHeight(600)
    .setDefaultDelay(100)    // 帧间隔 ms
    .setLoop(true);          // 循环播放

byte[] gif = GifUtils.createGif(frames, options);
```

### GifBuilder 链式构建

```java
import plus.ruoyi.common.media.builder.GifBuilder;

byte[] gif = GifBuilder.of(800, 600, 100)
    .addFrames(frameList)
    .loop(true)
    .backgroundColor(Color.WHITE)
    .build();
```

---

## 海报生成

### PosterUtils 快速创建

```java
import plus.ruoyi.common.media.utils.PosterUtils;

// 纯文字海报
BufferedImage poster = PosterUtils.createTextPoster(
    "海报标题", 800, 600);

// 带背景图
BufferedImage poster = PosterUtils.createTextPoster(
    "海报标题",
    "https://example.com/bg.jpg",
    800, 600);

// 产品海报
BufferedImage poster = PosterUtils.createProductPoster(
    "iPhone 15 Pro",
    "https://example.com/iphone.jpg",
    "¥7999",
    800, 600);
```

### PosterBuilder 链式构建

```java
import plus.ruoyi.common.media.builder.PosterBuilder;

BufferedImage poster = PosterBuilder.of(800, 600)
    .background(Color.WHITE)
    .addImage(productImage, 50, 50, 700, 400)
    .addText("产品名称", "微软雅黑", 32, "#333333", 50, 460)
    .addText("¥299", "Arial", 28, "#FF0000", 50, 500)
    .addWatermark(WatermarkOptions.text("©2024")
        .position(Position.BOTTOM_RIGHT))
    .build();
```

---

## Excel 导入导出

### 导入 Excel

```java
import plus.ruoyi.common.excel.utils.ExcelUtil;
import plus.ruoyi.common.excel.core.ExcelResult;

// 基础导入（返回 Map 列表）
List<Map<Integer, String>> maps = ExcelUtil.importExcel(inputStream);

// 转换为对象列表
List<UserVo> users = ExcelUtil.importExcel(inputStream, UserVo.class);

// 带校验的导入（推荐）
ExcelResult<UserVo> result = ExcelUtil.importExcel(
    inputStream, UserVo.class, true);  // true 启用校验

List<UserVo> successList = result.getList();      // 成功数据
List<String> errors = result.getErrorList();      // 错误列表
String analysis = result.getAnalysis();           // 分析报告

// 处理导入结果
if (!result.getErrorList().isEmpty()) {
    return R.fail(result.getAnalysis());
}
userService.batchSave(MapstructUtils.convert(successList, UserBo.class));
```

### 导出 Excel

```java
import plus.ruoyi.common.excel.utils.ExcelUtil;
import plus.ruoyi.common.excel.core.DropDownOptions;

// 基础导出
ExcelUtil.exportExcel(userList, "用户信息", UserVo.class, response);

// 带下拉选项
List<DropDownOptions> options = Arrays.asList(
    new DropDownOptions(2, Arrays.asList("男", "女")),       // 第2列
    new DropDownOptions(3, Arrays.asList("在职", "离职"))    // 第3列
);
ExcelUtil.exportExcel(userList, "用户信息", UserVo.class, response, options);

// 支持单元格合并
ExcelUtil.exportExcel(users, "用户信息", UserVo.class,
    true,    // 启用合并
    response);

// 输出到流
ExcelUtil.exportExcel(userList, "用户信息", UserVo.class, outputStream);
```

### 模板导出

```java
// 单表数据
ExcelUtil.exportTemplate(userList, "用户报表",
    "templates/user-report.xlsx", response);

// 多表数据
Map<String, Object> data = new HashMap<>();
data.put("userInfo", userObject);
data.put("orders", orderList);
ExcelUtil.exportTemplateMultiList(data, "综合报表",
    "templates/multi-report.xlsx", response);

// 多 Sheet
List<Map<String, Object>> sheets = new ArrayList<>();
sheets.add(Map.of("sheetName", "部门A", "data", deptAList));
sheets.add(Map.of("sheetName", "部门B", "data", deptBList));
ExcelUtil.exportTemplateMultiSheet(sheets, "多部门报表",
    "templates/dept-report.xlsx", response);
```

### ExcelResult 接口

```java
public interface ExcelResult<T> {
    Map<Integer, String> getHead();    // 表头
    List<T> getList();                 // 成功数据
    List<String> getErrorList();       // 错误信息
    String getAnalysis();              // 分析报告
}
```

---

## 文件处理

### FileUtils 文件下载

```java
import plus.ruoyi.common.core.utils.file.FileUtils;

// 设置下载响应头（自动处理浏览器兼容）
FileUtils.setAttachmentResponseHeader(response, "文件名.xlsx");

// URL 编码
String encoded = FileUtils.percentEncode("中文文件名");
```

### FileTypeUtils 类型判断

```java
import plus.ruoyi.common.core.utils.file.FileTypeUtils;

// 获取扩展名
String ext = FileTypeUtils.getExtension("image.jpg");  // "jpg"

// 类型判断
boolean isImage = FileTypeUtils.isImage("jpg");        // true
boolean isVideo = FileTypeUtils.isVideo("mp4");        // true
boolean isDocument = FileTypeUtils.isDocument("pdf");  // true

// 文件对象判断
File file = new File("test.jpg");
boolean isImage = FileTypeUtils.isImage(file);
String type = FileTypeUtils.getFileType(file);         // "图片"
boolean allowed = FileTypeUtils.isAllowed(file);       // true
```

### 预定义文件类型

| 常量 | 包含扩展名 |
|------|-----------|
| `IMAGE_EXTENSION` | bmp, gif, jpg, jpeg, png |
| `VIDEO_EXTENSION` | mp4, avi, rmvb |
| `MEDIA_EXTENSION` | swf, flv, mp3, wav, wma, wmv, mid, avi, mpg, asf, rm, rmvb |
| `DOCUMENT_EXTENSION` | doc, docx, xls, xlsx, ppt, pptx, html, htm, txt, pdf |
| `ARCHIVE_EXTENSION` | rar, zip, gz, bz2 |

---

## 辅助工具

### ColorUtils 颜色处理

```java
import plus.ruoyi.common.media.utils.ColorUtils;

// 解析颜色（支持多种格式）
Color color1 = ColorUtils.parseColor("#FF0000");        // 十六进制
Color color2 = ColorUtils.parseColor("rgb(255,0,0)");  // RGB
Color color3 = ColorUtils.parseColor("red");           // 颜色名

// 带透明度
Color transparent = ColorUtils.withAlpha(Color.RED, 0.5f);
```

### FontUtils 字体处理

```java
import plus.ruoyi.common.media.utils.FontUtils;

// 创建字体
Font font = FontUtils.createFont("微软雅黑", Font.BOLD, 24);

// 默认字体
Font font = FontUtils.createDefaultFont(18);

// 获取文本尺寸
Dimension size = FontUtils.getTextSize("测试文本", font);
int width = size.width;
int height = size.height;
```

---

## 枚举说明

### OutputFormat 输出格式

| 格式 | 枚举值 | MIME |
|------|-------|------|
| JPG | `JPG` | image/jpeg |
| PNG | `PNG` | image/png |
| GIF | `GIF` | image/gif |
| WEBP | `WEBP` | image/webp |
| BMP | `BMP` | image/bmp |

### ImageQuality 图片质量

| 质量 | 枚举值 | 压缩率 |
|------|-------|-------|
| 低 | `LOW` | 0.6 |
| 中 | `MEDIUM` | 0.8 |
| 高 | `HIGH` | 0.95 |

---

## 常见场景示例

### 1. 处理用户头像

```java
BufferedImage avatar = ImageBuilder.of(uploadedFile.getInputStream())
    .resize(300, 300, ResizeMode.FILL)     // 填充裁剪为正方形
    .format(OutputFormat.PNG)
    .build();

// 创建圆形头像
BufferedImage circleAvatar = ImageUtils.createCircleImage(avatar, 300);
```

### 2. 生成产品二维码

```java
BufferedImage qr = QrCodeBuilder.of(productUrl)
    .size(300)
    .logo("/resources/logo.png", 0.2f)
    .build();
```

### 3. 导入用户数据

```java
@PostMapping("/import")
public R<Void> importUsers(MultipartFile file) throws Exception {
    ExcelResult<UserVo> result = ExcelUtil.importExcel(
        file.getInputStream(), UserVo.class, true);

    if (!result.getErrorList().isEmpty()) {
        return R.fail(result.getAnalysis());
    }

    List<UserBo> boList = MapstructUtils.convert(result.getList(), UserBo.class);
    userService.batchSave(boList);
    return R.ok(result.getAnalysis());
}
```

### 4. 导出订单报表

```java
@PostMapping("/export")
public void exportOrders(OrderBo bo, HttpServletResponse response) {
    List<OrderVo> list = orderService.list(bo);

    // 状态列下拉
    List<DropDownOptions> options = Arrays.asList(
        new DropDownOptions(4, Arrays.asList("待支付", "已支付", "已完成"))
    );

    ExcelUtil.exportExcel(list, "订单报表", OrderVo.class, response, options);
}
```

### 5. 生成分享海报

```java
BufferedImage poster = PosterBuilder.of(750, 1334)  // 手机尺寸
    .background(Color.WHITE)
    .addImage(productImage, 50, 100, 650, 650)
    .addText(productName, "微软雅黑", 36, "#333333", 50, 800)
    .addText("¥" + price, "Arial", 48, "#FF0000", 50, 870)
    .addImage(qrCode, 550, 1100, 150, 150)
    .addText("长按识别二维码", "微软雅黑", 20, "#999999", 550, 1260)
    .build();
```

---

## 最佳实践

### 1. 图片处理流程

```java
// ✅ 推荐：链式处理，一次构建
BufferedImage result = ImageBuilder.of(source)
    .resize(800, 600)
    .addWatermark(watermark)
    .format(OutputFormat.PNG)
    .build();

// ❌ 避免：多次中间操作
BufferedImage temp1 = ImageUtils.resize(source, 800, 600, ResizeMode.FIT);
BufferedImage temp2 = addWatermark(temp1, watermark);
// ... 占用更多内存
```

### 2. Excel 导入校验

```java
// ✅ 推荐：启用校验
ExcelResult<UserVo> result = ExcelUtil.importExcel(
    inputStream, UserVo.class, true);

// 统一处理错误
if (!result.getErrorList().isEmpty()) {
    return R.fail(result.getAnalysis());
}

// ❌ 避免：不校验直接导入
List<UserVo> list = ExcelUtil.importExcel(inputStream, UserVo.class);
// 可能导入脏数据
```

### 3. 大文件处理

```java
// ✅ 推荐：流式处理
try (InputStream is = file.getInputStream()) {
    BufferedImage image = ImageUtils.loadFromStream(is);
    // 处理图片
}

// ❌ 避免：加载全部到内存
byte[] all = file.getBytes();
BufferedImage image = ImageUtils.loadFromBytes(all);
```
