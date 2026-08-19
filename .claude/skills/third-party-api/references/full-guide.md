
# 第三方 API 集成指南

## 概述

本项目通过 `ruoyi-common-http` 模块提供第三方 API 集成能力，基于 **Forest** 声明式 HTTP 客户端框架。该模块**不是通用 HTTP 客户端**，而是针对具体第三方服务的专用集成。

当前已集成的服务：

| 服务 | 客户端接口 | 功能 |
|------|-----------|------|
| **高德地图** | `GaodeMapClient` | IP定位、地理编码、逆地理编码、天气查询、距离计算 |
| **火山引擎 TTS** | `VolcengineTtsClient` | 语音合成（文本转语音）|

待扩展（占位包已存在）：阿里云、百度、微信。

---

## 模块结构

```
ruoyi-common-http/
├── config/
│   └── HttpAutoConfiguration.java              # 自动配置（Forest JSON 转换器）
├── client/
│   ├── gaode/map/                              # 高德地图集成
│   │   ├── GaodeMapClient.java                 # 声明式 API 接口（5 个方法）
│   │   ├── GaodeMapInterceptor.java            # 请求拦截器（注入 API Key）
│   │   ├── properties/
│   │   │   └── GaodeMapProperties.java         # 配置属性
│   │   └── response/                           # 响应 DTO（5 个）
│   │       ├── IPLocationResponse.java         # IP 定位
│   │       ├── GeocodingResponse.java          # 地理编码
│   │       ├── ReverseGeocodingResponse.java   # 逆地理编码
│   │       ├── WeatherResponse.java            # 天气
│   │       └── DistanceResponse.java           # 距离
│   ├── volcengine/tts/                         # 火山引擎 TTS
│   │   ├── VolcengineTtsClient.java            # 声明式 API 接口（3 个方法）
│   │   ├── VolcengineTtsInterceptor.java       # 请求拦截器（注入 Token）
│   │   ├── properties/
│   │   │   └── VolcengineTtsProperties.java    # 配置属性（12 个参数）
│   │   ├── request/
│   │   │   └── VolcengineTtsRequest.java       # 请求 DTO（Builder 模式）
│   │   └── response/
│   │       └── VolcengineTtsResponse.java      # 响应 DTO
│   ├── aliyun/                                 # 阿里云（占位）
│   ├── baidu/                                  # 百度（占位）
│   └── weixin/                                 # 微信（占位）
└── test/
    ├── GaodeMapTest.java                       # 高德地图测试（14 个用例）
    └── VolcengineTtsTest.java                  # TTS 测试（8 个用例）
```

---

## 核心框架：Forest

本模块使用 [Forest](https://forest.kim/) 声明式 HTTP 客户端框架，与 Spring Boot 3 集成。

### Forest 核心特性

| 特性 | 说明 | 示例 |
|------|------|------|
| **声明式接口** | 用接口 + 注解定义 API，无需实现类 | `@Get(url = "/v3/ip")` |
| **拦截器** | `ForestInterceptor` 处理请求前/后逻辑 | 注入 API Key、Token |
| **参数注入** | `@Query`、`@Body`、`@Var` 等注解 | `@Query("ip") String ip` |
| **URL 模板** | `${var}` 占位符支持动态 URL | `${coord.lng},${coord.lat}` |
| **响应包装** | `ForestResponse<T>` 统一封装 | 包含状态码、响应体 |
| **JSON 转换** | 自动序列化/反序列化 | Jackson 集成 |

### HttpAutoConfiguration

```java
@AutoConfiguration
@EnableConfigurationProperties({GaodeMapProperties.class, VolcengineTtsProperties.class})
public class HttpAutoConfiguration {

    @Bean
    public ForestJacksonConverter forestJacksonConverter(ObjectMapper objectMapper) {
        ObjectMapper customMapper = objectMapper.copy();
        // 字符串可转为数组："内丘县" → ["内丘县"]
        customMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        // 空数组转 null：[] → null
        customMapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
        // 忽略未知字段（兼容 API 版本更新）
        customMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return new ForestJacksonConverter(customMapper);
    }
}
```

> **为什么需要自定义 ObjectMapper？** 第三方 API 的 JSON 格式不规范（如高德地图某些字段在有值时返回字符串、无值时返回空数组），需要容错处理。

---

## 一、高德地图集成

### 配置

```yaml
# http-client-dev.yml（按 profile 分文件）
gaode:
  map:
    apiKey: "your_amap_web_service_key"  # 高德 Web 服务 API Key（必填）
    timeout: 5000                         # 超时时间（毫秒，默认 5000）
    enabled: true                         # 是否启用（默认 true）
```

> **获取 API Key**：登录 [高德开放平台](https://lbs.amap.com/)，创建应用并申请 Web 服务类型的 Key。

### GaodeMapClient 接口

```java
@BaseRequest(
    baseURL = "https://restapi.amap.com",
    interceptor = GaodeMapInterceptor.class
)
public interface GaodeMapClient {

    // 1. IP 定位
    @Get(url = "/v3/ip")
    ForestResponse<IPLocationResponse> getLocationByIp(
        @Query("ip") String ip,
        @Query("type") String type    // "4" 代表 IPv4
    );

    // 2. 地理编码（地址 → 坐标）
    @Get(url = "/v3/geocode/geo?output=json")
    ForestResponse<GeocodingResponse> geocoding(
        @Query("address") String address
    );

    // 3. 逆地理编码（坐标 → 地址）
    @Get(url = "/v3/geocode/regeo?output=json&location=${coord.lng},${coord.lat}")
    ForestResponse<ReverseGeocodingResponse> reverseGeocoding(
        @Var("coord") CoordinateUtil.Coordinate coord
    );

    // 4. 天气查询（行政区划代码 → 天气）
    @Get(url = "/v3/weather/weatherInfo")
    ForestResponse<WeatherResponse> getWeatherByAdcode(
        @Query("city") String adcode
    );

    // 5. 距离计算（两点间直线距离）
    @Get(url = "/v3/distance?type=0&origins=${origins.lng},${origins.lat}&destination=${destination.lng},${destination.lat}")
    ForestResponse<DistanceResponse> calculateDistance(
        @Var("origins") CoordinateUtil.Coordinate start,
        @Query("destination") CoordinateUtil.Coordinate end
    );
}
```

### GaodeMapInterceptor 拦截器

```java
@Slf4j
public class GaodeMapInterceptor implements ForestInterceptor {

    @Override
    public boolean beforeExecute(ForestRequest req) {
        GaodeMapProperties properties = SpringUtils.getBean(GaodeMapProperties.class);

        // 1. 检查服务是否启用
        if (!properties.getEnabled()) {
            log.warn("高德地图服务未启用");
            return false;  // 中止请求
        }

        // 2. 检查 API Key
        String apiKey = properties.getApiKey();
        if (StringUtils.isBlank(apiKey)) {
            log.error("高德地图 API Key 未配置");
            return false;
        }

        // 3. 注入 API Key 到查询参数
        req.addQuery("key", apiKey);
        return true;  // 继续执行
    }
}
```

### 使用示例

#### 1. IP 定位

```java
@Resource
private GaodeMapClient gaodeMapClient;

public String getLocationByIp(String ip) {
    ForestResponse<IPLocationResponse> response = gaodeMapClient.getLocationByIp(ip, "4");
    if (response.isSuccess()) {
        IPLocationResponse data = response.getResult();
        return data.getProvince() + data.getCity() + data.getDistrict();
    }
    return "未知位置";
}
```

#### 2. 地理编码（地址转坐标）

```java
public CoordinateUtil.Coordinate getCoordinate(String address) {
    ForestResponse<GeocodingResponse> response = gaodeMapClient.geocoding(address);
    if (response.isSuccess()) {
        GeocodingResponse data = response.getResult();
        if ("1".equals(data.getStatus()) && CollUtil.isNotEmpty(data.getGeocodes())) {
            String location = data.getGeocodes().get(0).getLocation();  // "116.397428,39.90923"
            String[] parts = location.split(",");
            return new CoordinateUtil.Coordinate(
                Double.parseDouble(parts[0]),  // lng
                Double.parseDouble(parts[1])   // lat
            );
        }
    }
    return null;
}
```

#### 3. 逆地理编码（坐标转地址）

```java
public String getAddress(double lng, double lat) {
    CoordinateUtil.Coordinate coord = new CoordinateUtil.Coordinate(lng, lat);
    ForestResponse<ReverseGeocodingResponse> response = gaodeMapClient.reverseGeocoding(coord);
    if (response.isSuccess()) {
        ReverseGeocodingResponse data = response.getResult();
        return data.getRegeocode().getFormattedAddress();
    }
    return null;
}
```

#### 4. 天气查询

```java
public WeatherResponse.WeatherData getWeather(String adcode) {
    ForestResponse<WeatherResponse> response = gaodeMapClient.getWeatherByAdcode(adcode);
    if (response.isSuccess()) {
        WeatherResponse data = response.getResult();
        if ("1".equals(data.getStatus()) && CollUtil.isNotEmpty(data.getLives())) {
            return data.getLives().get(0);
            // weather.getTemperature() → "25"
            // weather.getWeather()     → "晴"
            // weather.getWindpower()   → "≤3"
        }
    }
    return null;
}
```

#### 5. 距离计算

```java
public String calculateDistance(double lng1, double lat1, double lng2, double lat2) {
    CoordinateUtil.Coordinate start = new CoordinateUtil.Coordinate(lng1, lat1);
    CoordinateUtil.Coordinate end = new CoordinateUtil.Coordinate(lng2, lat2);
    ForestResponse<DistanceResponse> response = gaodeMapClient.calculateDistance(start, end);
    if (response.isSuccess()) {
        DistanceResponse data = response.getResult();
        if (CollUtil.isNotEmpty(data.getResults())) {
            return data.getResults().get(0).getDistance();  // 距离（米）
        }
    }
    return null;
}
```

### 响应 DTO 结构

#### IPLocationResponse

```java
@Data
@NoArgsConstructor
public class IPLocationResponse {
    String status;       // "1" 成功
    String info;         // "OK"
    String infocode;     // "10000"
    String country;      // "中国"
    String province;     // "浙江省"
    String city;         // "杭州市"
    String district;     // "西湖区"
    String isp;          // "电信"
    String location;     // "120.12,30.12"
    String ip;           // "223.5.5.5"
}
```

#### GeocodingResponse

```java
@Data
public class GeocodingResponse {
    String status;
    List<GeocodingData> geocodes;

    @Data
    public static class GeocodingData {
        String formattedAddress;   // "北京市朝阳区三里屯街道"
        String country;            // "中国"
        String province;           // "北京市"
        String city;               // "北京市"
        List<String> district;     // ["朝阳区"]
        String adcode;             // "110105"
        String location;           // "116.454871,39.926589"
        String level;              // "门址"
    }
}
```

#### ReverseGeocodingResponse

```java
@Data
public class ReverseGeocodingResponse {
    String status;
    ReverseGeocodingData regeocode;

    @Data
    public static class ReverseGeocodingData {
        String formattedAddress;              // 完整地址
        AddressComponent addressComponent;    // 地址组件
    }

    @Data
    public static class AddressComponent {
        String country, province, city, citycode;
        List<String> district;
        String adcode, township, towncode;
        StreetNumber streetNumber;
        List<BusinessArea> businessAreas;
    }
}
```

#### WeatherResponse

```java
@Data
public class WeatherResponse {
    String status;
    List<WeatherData> lives;

    @Data
    public static class WeatherData {
        String province;        // "浙江"
        String city;            // "杭州市"
        String adcode;          // "330100"
        String weather;         // "晴"
        String temperature;     // "25"
        String winddirection;   // "东北"
        String windpower;       // "≤3"
        String humidity;        // "68"
        String reporttime;      // "2026-02-11 15:30:00"
    }
}
```

---

## 二、火山引擎 TTS 集成

### 配置

```yaml
# http-client-dev.yml
volcengine:
  tts:
    enabled: true                              # 是否启用（默认 false）
    appId: "your_app_id"                       # 应用 ID（必填）
    accessToken: "your_access_token"           # 访问令牌（必填）
    cluster: "volcano_tts"                     # 业务集群
    defaultVoice: "BV001_streaming"            # 默认音色
    encoding: "pcm"                            # 音频编码（pcm/mp3）
    sampleRate: 24000                          # 采样率（16000/24000）
    speedRatio: 1.0                            # 语速（0.5-2.0）
    volumeRatio: 1.0                           # 音量（0.5-2.0）
    pitchRatio: 1.0                            # 音调（0.5-2.0）
    timeout: 30000                             # 超时（毫秒，默认 30000）
```

> **集群类型**：`volcano_tts`=内置音色、`volcano_mega`=复制音色（声音克隆）

### VolcengineTtsClient 接口

```java
@BaseRequest(
    baseURL = "https://openspeech.bytedance.com",
    interceptor = VolcengineTtsInterceptor.class
)
public interface VolcengineTtsClient {

    // 1. 完整版 - 传入完整请求对象
    @Post(url = "/api/v1/tts")
    @Headers("Content-Type: application/json")
    ForestResponse<VolcengineTtsResponse> synthesize(@Body VolcengineTtsRequest request);

    // 2. 简化版 - 指定文本和音色
    default ForestResponse<VolcengineTtsResponse> synthesize(String text, String voiceType) {
        VolcengineTtsProperties props = SpringUtils.getBean(VolcengineTtsProperties.class);
        VolcengineTtsRequest request = VolcengineTtsRequest.builder()
            .app(AppConfig.builder().appid(props.getAppId()).cluster(props.getCluster()).build())
            .audio(AudioConfig.builder()
                .voiceType(voiceType)
                .encoding(props.getEncoding())
                .sampleRate(props.getSampleRate())
                .speedRatio(props.getSpeedRatio())
                .volumeRatio(props.getVolumeRatio())
                .pitchRatio(props.getPitchRatio())
                .build())
            .request(RequestConfig.builder().text(text).build())
            .build();
        return synthesize(request);
    }

    // 3. 最简版 - 仅指定文本（使用默认音色）
    default ForestResponse<VolcengineTtsResponse> synthesize(String text) {
        VolcengineTtsProperties props = SpringUtils.getBean(VolcengineTtsProperties.class);
        return synthesize(text, props.getDefaultVoice());
    }
}
```

### VolcengineTtsRequest（Builder 模式）

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolcengineTtsRequest {

    private AppConfig app;
    private UserConfig user;
    private AudioConfig audio;
    private RequestConfig request;

    @Data @Builder
    public static class AppConfig {
        private String appid;       // 应用 ID
        private String cluster;     // "volcano_tts" 或 "volcano_mega"
    }

    @Data @Builder
    public static class UserConfig {
        @Builder.Default
        private String uid = "default_uid";
    }

    @Data @Builder
    public static class AudioConfig {
        @JsonProperty("voice_type")
        private String voiceType;             // 音色类型
        @Builder.Default
        private String encoding = "mp3";      // 编码格式
        @JsonProperty("sample_rate")
        private Integer sampleRate;           // 采样率
        @Builder.Default @JsonProperty("speed_ratio")
        private Double speedRatio = 1.0;      // 语速
        @Builder.Default @JsonProperty("volume_ratio")
        private Double volumeRatio = 1.0;     // 音量
        @Builder.Default @JsonProperty("pitch_ratio")
        private Double pitchRatio = 1.0;      // 音调
    }

    @Data @Builder
    public static class RequestConfig {
        @Builder.Default
        private String reqid = UUID.randomUUID().toString();
        private String text;                  // 待合成文本
        @Builder.Default @JsonProperty("text_type")
        private String textType = "plain";    // plain=纯文本, ssml=SSML标记
        @Builder.Default
        private String operation = "query";
    }
}
```

### VolcengineTtsResponse

```java
@Data
public class VolcengineTtsResponse {
    public static final int CODE_SUCCESS = 3000;  // 成功响应码

    Integer code;       // 3000=成功
    String message;     // 响应消息
    String reqid;       // 请求 ID
    String data;        // Base64 编码的音频数据
    Addition addition;  // 附加信息

    @Data
    public static class Addition {
        String duration;    // 音频时长（毫秒）
        String firstPkg;   // 首包时间
    }

    public boolean isSuccess() {
        return code != null && code == CODE_SUCCESS;
    }
}
```

### 使用示例

#### 1. 最简调用

```java
@Resource
private VolcengineTtsClient ttsClient;

public byte[] textToSpeech(String text) {
    ForestResponse<VolcengineTtsResponse> response = ttsClient.synthesize(text);
    if (response.isSuccess()) {
        VolcengineTtsResponse data = response.getResult();
        if (data.isSuccess()) {
            return Base64.decode(data.getData());  // 返回音频字节
        }
    }
    return null;
}
```

#### 2. 指定音色

```java
// 使用特定音色
ForestResponse<VolcengineTtsResponse> response = ttsClient.synthesize("你好世界", "BV700_streaming");
```

#### 3. 完整参数控制

```java
VolcengineTtsRequest request = VolcengineTtsRequest.builder()
    .app(AppConfig.builder()
        .appid("your-app-id")
        .cluster("volcano_tts")
        .build())
    .audio(AudioConfig.builder()
        .voiceType("BV001_streaming")
        .encoding("mp3")
        .sampleRate(24000)
        .speedRatio(1.2)     // 1.2 倍速
        .volumeRatio(1.5)    // 1.5 倍音量
        .pitchRatio(0.8)     // 0.8 倍音调（低沉）
        .build())
    .request(RequestConfig.builder()
        .text("这是一段需要转换为语音的文本")
        .textType("plain")
        .build())
    .build();

ForestResponse<VolcengineTtsResponse> response = ttsClient.synthesize(request);
```

#### 4. 保存为 MP3 文件

```java
public void saveToMp3(String text, String filePath) throws IOException {
    ForestResponse<VolcengineTtsResponse> response = ttsClient.synthesize(text);
    if (response.isSuccess() && response.getResult().isSuccess()) {
        byte[] audioData = Base64.decode(response.getResult().getData());
        Files.write(Path.of(filePath), audioData);
    }
}
```

---

## 三、扩展新的第三方服务（5 步）

### 步骤 1：创建配置属性类

```java
package plus.ruoyi.common.http.client.xxx.properties;

@Data
@ConfigurationProperties(prefix = "xxx.service")
public class XxxServiceProperties {
    private Boolean enabled = false;   // 是否启用
    private String apiKey;             // API 密钥
    private Integer timeout = 5000;    // 超时时间
}
```

### 步骤 2：创建响应 DTO

```java
package plus.ruoyi.common.http.client.xxx.response;

@Data
public class XxxResponse {
    private Integer code;
    private String message;
    private Object data;

    public boolean isSuccess() {
        return code != null && code == 0;
    }
}
```

### 步骤 3：创建拦截器

```java
package plus.ruoyi.common.http.client.xxx;

@Slf4j
public class XxxInterceptor implements ForestInterceptor {

    @Override
    public boolean beforeExecute(ForestRequest req) {
        XxxServiceProperties props = SpringUtils.getBean(XxxServiceProperties.class);

        if (!props.getEnabled()) {
            log.warn("Xxx 服务未启用");
            return false;
        }

        if (StringUtils.isBlank(props.getApiKey())) {
            log.error("Xxx API Key 未配置");
            return false;
        }

        // 注入认证信息（根据第三方 API 要求选择方式）
        // 方式 1：Query 参数
        req.addQuery("apiKey", props.getApiKey());
        // 方式 2：Header
        // req.addHeader("Authorization", "Bearer " + props.getApiKey());
        // 方式 3：自定义 Header
        // req.addHeader("X-Api-Key", props.getApiKey());

        return true;
    }
}
```

### 步骤 4：创建声明式客户端接口

```java
package plus.ruoyi.common.http.client.xxx;

@BaseRequest(
    baseURL = "https://api.xxx.com",
    interceptor = XxxInterceptor.class
)
public interface XxxClient {

    @Get(url = "/v1/resource")
    ForestResponse<XxxResponse> getResource(@Query("id") String id);

    @Post(url = "/v1/resource")
    @Headers("Content-Type: application/json")
    ForestResponse<XxxResponse> createResource(@Body XxxRequest request);
}
```

### 步骤 5：注册配置属性

在 `HttpAutoConfiguration` 中添加：

```java
@AutoConfiguration
@EnableConfigurationProperties({
    GaodeMapProperties.class,
    VolcengineTtsProperties.class,
    XxxServiceProperties.class       // 新增
})
public class HttpAutoConfiguration {
    // ...
}
```

### 目录结构

```
client/xxx/
├── XxxClient.java              # Forest 声明式接口
├── XxxInterceptor.java         # 请求拦截器
├── properties/
│   └── XxxServiceProperties.java
├── request/
│   └── XxxRequest.java         # 请求 DTO（可选）
└── response/
    └── XxxResponse.java        # 响应 DTO
```

---

## 四、Forest 框架核心用法

### 注解速查

| 注解 | 用途 | 示例 |
|------|------|------|
| `@BaseRequest` | 接口级配置（baseURL、拦截器） | `@BaseRequest(baseURL = "...", interceptor = X.class)` |
| `@Get` | GET 请求 | `@Get(url = "/v3/ip")` |
| `@Post` | POST 请求 | `@Post(url = "/api/v1/tts")` |
| `@Headers` | 请求头 | `@Headers("Content-Type: application/json")` |
| `@Query` | URL 查询参数 | `@Query("ip") String ip` |
| `@Body` | 请求体 | `@Body XxxRequest request` |
| `@Var` | URL 模板变量 | `@Var("coord") Coordinate coord` |

### URL 模板变量

```java
// URL 中使用 ${} 占位符，通过 @Var 注入对象属性
@Get(url = "/v3/geocode/regeo?location=${coord.lng},${coord.lat}")
ForestResponse<Resp> reverseGeo(@Var("coord") CoordinateUtil.Coordinate coord);

// coord.getLng() 和 coord.getLat() 会自动替换到 URL 中
```

### ForestResponse 使用

```java
ForestResponse<XxxResponse> response = client.someApi(params);

// 检查 HTTP 请求是否成功
if (response.isSuccess()) {
    XxxResponse result = response.getResult();  // 获取反序列化后的对象
    int statusCode = response.getStatusCode();  // HTTP 状态码
}

// 获取原始内容
String rawContent = response.getContent();      // 原始响应字符串
```

### ForestInterceptor 生命周期

```java
public class MyInterceptor implements ForestInterceptor {

    // 1. 请求执行前（注入认证、修改参数）
    @Override
    public boolean beforeExecute(ForestRequest req) {
        req.addQuery("key", "value");
        req.addHeader("Authorization", "Bearer xxx");
        return true;  // true=继续, false=中止
    }

    // 2. 请求成功后（可选）
    @Override
    public void onSuccess(Object data, ForestRequest req, ForestResponse resp) {
        log.info("请求成功: {}", req.getUrl());
    }

    // 3. 请求失败后（可选）
    @Override
    public void onError(ForestRuntimeException ex, ForestRequest req, ForestResponse resp) {
        log.error("请求失败: {} - {}", req.getUrl(), ex.getMessage());
    }
}
```

---

## 五、配置文件规范

### 按环境分文件

配置文件路径：`classpath:http-client-${spring.profiles.active}.yml`

```
resources/
├── http-client-dev.yml      # 开发环境
├── http-client-test.yml     # 测试环境
└── http-client-prod.yml     # 生产环境
```

### 配置模板

```yaml
# http-client-dev.yml
gaode:
  map:
    apiKey: "dev_amap_key"
    timeout: 5000
    enabled: true

volcengine:
  tts:
    enabled: false             # 开发环境默认关闭
    appId: ""
    accessToken: ""
```

### 敏感信息保护

```yaml
# 生产环境通过环境变量注入
gaode:
  map:
    apiKey: ${GAODE_MAP_API_KEY}

volcengine:
  tts:
    appId: ${VOLCENGINE_TTS_APP_ID}
    accessToken: ${VOLCENGINE_TTS_ACCESS_TOKEN}
```

---

## 六、常见错误与最佳实践

### ✅ 正确做法

```java
// 1. 检查 ForestResponse 是否成功
ForestResponse<IPLocationResponse> response = gaodeMapClient.getLocationByIp(ip, "4");
if (response.isSuccess()) {
    IPLocationResponse data = response.getResult();
    if ("1".equals(data.getStatus())) {  // 高德 API 业务状态码
        // 处理数据
    }
}

// 2. 使用 default 方法提供简化接口
default ForestResponse<Resp> simpleMethod(String text) {
    // 自动从 Properties 读取配置，构建完整请求
    XxxProperties props = SpringUtils.getBean(XxxProperties.class);
    return fullMethod(buildRequest(props, text));
}

// 3. 拦截器中使用 SpringUtils 获取 Bean
XxxProperties props = SpringUtils.getBean(XxxProperties.class);

// 4. API Key 通过配置文件/环境变量管理
gaode.map.apiKey: ${GAODE_MAP_API_KEY}
```

### ❌ 常见错误

```java
// 1. 硬编码 API Key
req.addQuery("key", "abc123def456");  // ❌ 安全风险
req.addQuery("key", props.getApiKey());  // ✅ 从配置读取

// 2. 不检查响应状态
IPLocationResponse data = response.getResult();  // ❌ response 可能失败
if (response.isSuccess()) {                       // ✅ 先检查
    IPLocationResponse data = response.getResult();
}

// 3. 混淆 HTTP 状态和业务状态
if (response.isSuccess()) {  // 这只是 HTTP 200
    // 还需要检查业务状态码
    if (!"1".equals(data.getStatus())) {  // 高德 API 的业务错误
        log.error("高德 API 返回错误: {}", data.getInfo());
    }
}

// 4. 忘记在 AutoConfiguration 中注册 Properties
@EnableConfigurationProperties({
    GaodeMapProperties.class,
    // XxxProperties.class  ← ❌ 忘了注册，导致 @Resource 注入失败
})

// 5. 拦截器中直接 @Autowired（Forest 拦截器不由 Spring 管理）
@Autowired  // ❌ 不会生效
private XxxProperties properties;

SpringUtils.getBean(XxxProperties.class);  // ✅ 正确方式

// 6. TTS 超时时间设置过短
volcengine.tts.timeout: 5000   // ❌ TTS 合成可能需要 10-30 秒
volcengine.tts.timeout: 30000  // ✅ 预留足够时间
```

---

## 七、JSON 反序列化容错

高德地图 API 返回的 JSON 格式不统一，需要特殊处理：

| 场景 | 高德返回值 | 处理策略 |
|------|-----------|---------|
| 字段有值 | `"district": "朝阳区"` | 正常解析 |
| 字段无值 | `"district": []` | 空数组转 null |
| 字段单值 | `"district": "朝阳区"` | 字符串转为 `List<String>` |

已通过 `HttpAutoConfiguration` 全局配置：
- `ACCEPT_SINGLE_VALUE_AS_ARRAY` - 字符串可转为数组
- `ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT` - 空数组转为 null
- `FAIL_ON_UNKNOWN_PROPERTIES = false` - 忽略未知字段

---

## 八、与其他技能的关系

| 技能 | 关系 |
|------|------|
| `security-guard` | API Key/Token 安全存储、加密传输 |
| `json-serialization` | Forest 使用 Jackson 做 JSON 转换 |
| `ai-langchain4j` | TTS 可作为 AI 对话的语音输出 |
| `redis-cache` | 第三方 API 结果可缓存（如天气、地理编码） |
| `scheduled-jobs` | 定时刷新 Token 或预热缓存 |

---

## 九、参考文件索引

| 文件 | 行数 | 说明 |
|------|------|------|
| `config/HttpAutoConfiguration.java` | 45 | 自动配置（Forest JSON 转换器） |
| `client/gaode/map/GaodeMapClient.java` | 63 | 高德地图声明式接口 |
| `client/gaode/map/GaodeMapInterceptor.java` | 44 | 高德请求拦截器 |
| `client/gaode/map/properties/GaodeMapProperties.java` | 30 | 高德配置属性 |
| `client/gaode/map/response/IPLocationResponse.java` | 46 | IP 定位响应 |
| `client/gaode/map/response/GeocodingResponse.java` | 100 | 地理编码响应 |
| `client/gaode/map/response/ReverseGeocodingResponse.java` | 137 | 逆地理编码响应 |
| `client/gaode/map/response/WeatherResponse.java` | 65 | 天气响应 |
| `client/gaode/map/response/DistanceResponse.java` | 50 | 距离响应 |
| `client/volcengine/tts/VolcengineTtsClient.java` | 93 | TTS 声明式接口 |
| `client/volcengine/tts/VolcengineTtsInterceptor.java` | 53 | TTS 请求拦截器 |
| `client/volcengine/tts/properties/VolcengineTtsProperties.java` | 81 | TTS 配置属性（12 参数） |
| `client/volcengine/tts/request/VolcengineTtsRequest.java` | 163 | TTS 请求 DTO（Builder） |
| `client/volcengine/tts/response/VolcengineTtsResponse.java` | 66 | TTS 响应 DTO |
| `test/GaodeMapTest.java` | 198 | 高德地图测试（14 用例） |
| `test/VolcengineTtsTest.java` | 298 | TTS 测试（8 用例） |

---

## 十、FAQ

### Q1: Forest 和 RestTemplate/WebClient 有什么区别？

**A**: Forest 是声明式 HTTP 客户端（类似 Feign），用接口注解定义 API，无需编写实现类。RestTemplate/WebClient 是命令式客户端，需要手动构建请求。本项目选择 Forest 是因为其声明式风格更简洁，适合第三方 API 集成。

### Q2: 为什么拦截器中不能用 @Autowired？

**A**: Forest 拦截器的生命周期由 Forest 框架管理，不经过 Spring 容器创建。因此必须通过 `SpringUtils.getBean()` 手动获取 Bean。

### Q3: 如何处理第三方 API 限流？

**A**: 结合 `redis-cache` 技能：
1. 缓存频繁查询的结果（如地理编码、天气）
2. 使用 Redis 计数器做客户端限流
3. 在拦截器的 `onError` 中处理 429 状态码

### Q4: 高德 API 响应中 status="1" 是什么意思？

**A**: 高德地图 API 使用字符串 `"1"` 表示成功、`"0"` 表示失败。注意是**字符串**，不是数字。`infocode` 字段包含详细错误码（如 `"10000"` 为成功）。

### Q5: TTS 的 Base64 音频数据如何使用？

**A**: 火山引擎 TTS 返回 Base64 编码的音频数据，解码后得到原始音频字节流：
```java
byte[] audioBytes = Base64.decode(response.getData());
// PCM 格式可直接播放或转换
// MP3 格式可保存为文件
```

### Q6: 如何切换 TTS 音色？

**A**: 火山引擎提供多种音色，通过 `voiceType` 参数指定：
- `BV001_streaming` - 通用女声
- `BV700_streaming` - 通用男声
- 更多音色参考火山引擎控制台

可在配置文件中设置默认音色（`defaultVoice`），也可在调用时指定：
```java
ttsClient.synthesize("文本", "BV700_streaming");
```
