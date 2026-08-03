---
name: json-serialization
description: |
  当需要处理 JSON 序列化、反序列化、数据类型转换、日期处理、大数字精度保护时自动使用此 Skill。

  触发场景：
  - JSON 序列化/反序列化操作
  - 大数字精度问题（Long/BigInteger/BigDecimal）
  - 日期时间格式化与转换
  - 复杂泛型类型转换
  - JSON 格式验证
  - 数据类型映射与转换

  触发词：JSON、序列化、反序列化、JsonUtils、日期格式、精度、BigDecimal、Long、类型转换、JSON验证
---

# JSON 序列化与数据转换指南

## 概述

`ruoyi-common-json` 模块是基于 Jackson 的 JSON 处理和数据转换解决方案，提供以下核心功能：

| 功能 | 说明 | 工具类 |
|------|------|--------|
| JSON 序列化 | 对象转 JSON 字符串 | `JsonUtils.toJsonString()` |
| JSON 反序列化 | JSON 字符串转对象 | `JsonUtils.parseObject()` |
| 复杂类型转换 | 支持泛型和集合转换 | `JsonUtils.parseObject(text, TypeReference)` |
| 大数字精度 | 避免 JavaScript 精度丢失 | `BigNumberSerializer` |
| 日期处理 | 统一日期时间格式 | `CustomDateDeserializer` |
| JSON 验证 | 判断字符串是否为有效 JSON | `JsonUtils.isJson()` |

---

## 核心工具类

### JsonUtils - JSON 工具类

提供全局的 ObjectMapper 实例和常用操作方法。

**导入**：
```java
import plus.ruoyi.common.json.utils.JsonUtils;
```

#### 序列化方法

##### 1. 对象转 JSON 字符串
```java
String json = JsonUtils.toJsonString(object);
// 若对象为 null，返回 null
```

**示例**：
```java
User user = new User("张三", 25);
String json = JsonUtils.toJsonString(user);
// 输出: {"name":"张三","age":25}
```

##### 2. 对象序列化为字节数组
```java
// 内部实现
byte[] bytes = JsonUtils.toJsonString(object).getBytes();
```

---

#### 反序列化方法

##### 1. JSON 字符串转简单类型对象
```java
<T> T parseObject(String text, Class<T> clazz)
```

**参数说明**：
- `text`: JSON 字符串，为空时返回 null
- `clazz`: 目标对象的 Class

**示例**：
```java
String json = "{\"name\":\"张三\",\"age\":25}";
User user = JsonUtils.parseObject(json, User.class);
// 若 json 为 null 或空，返回 null
```

##### 2. 字节数组转对象
```java
<T> T parseObject(byte[] bytes, Class<T> clazz)
```

**示例**：
```java
byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
User user = JsonUtils.parseObject(bytes, User.class);
```

##### 3. 复杂泛型类型转换
```java
<T> T parseObject(String text, TypeReference<T> typeReference)
```

**用于转换复杂类型，如 `List<User>`、`Map<String, User>` 等**

**导入 TypeReference**：
```java
import com.fasterxml.jackson.core.type.TypeReference;
```

**示例**：
```java
String json = "[{\"name\":\"张三\",\"age\":25},{\"name\":\"李四\",\"age\":30}]";

// 方式1：使用匿名内部类（推荐）
List<User> users = JsonUtils.parseObject(json, new TypeReference<List<User>>(){});

// 方式2：使用 parseArray 简化
List<User> users = JsonUtils.parseArray(json, User.class);

// Map 转换
String mapJson = "{\"user1\":{\"name\":\"张三\"},\"user2\":{\"name\":\"李四\"}}";
Map<String, User> userMap = JsonUtils.parseObject(mapJson, new TypeReference<Map<String, User>>(){});
```

##### 4. JSON 数组转 List
```java
<T> List<T> parseArray(String text, Class<T> clazz)
```

**示例**：
```java
String json = "[{\"name\":\"张三\"},{\"name\":\"李四\"}]";
List<User> users = JsonUtils.parseArray(json, User.class);
// 若 json 为空，返回空 ArrayList（非 null）
```

##### 5. JSON 转 Dict（增强型 Map）
```java
Dict parseMap(String text)
List<Dict> parseArrayMap(String text)
```

**Dict 特点**：支持链式调用，类型灵活

**示例**：
```java
String json = "{\"name\":\"张三\",\"age\":25}";
Dict dict = JsonUtils.parseMap(json);
String name = dict.getStr("name");        // 获取字符串
int age = dict.getInt("age");             // 获取整数
// 非 JSON 格式时，返回 null

String arrayJson = "[{\"id\":1},{\"id\":2}]";
List<Dict> dicts = JsonUtils.parseArrayMap(arrayJson);
```

---

#### JSON 验证方法

##### 1. 判断是否为合法 JSON
```java
boolean isJson(String str)
```

**返回 true**：有效的 JSON 对象或数组
**返回 false**：无效或为空

**示例**：
```java
JsonUtils.isJson("{\"name\":\"张三\"}");        // true
JsonUtils.isJson("[1,2,3]");                    // true
JsonUtils.isJson("not json");                   // false
JsonUtils.isJson("");                           // false
```

##### 2. 判断是否为 JSON 对象
```java
boolean isJsonObject(String str)
```

**示例**：
```java
JsonUtils.isJsonObject("{\"name\":\"张三\"}");  // true
JsonUtils.isJsonObject("[1,2,3]");              // false
```

##### 3. 判断是否为 JSON 数组
```java
boolean isJsonArray(String str)
```

**示例**：
```java
JsonUtils.isJsonArray("[1,2,3]");               // true
JsonUtils.isJsonArray("{\"name\":\"张三\"}");   // false
```

---

#### 获取 ObjectMapper
```java
ObjectMapper mapper = JsonUtils.getObjectMapper();
// 自定义 Jackson 操作
JsonNode node = mapper.readTree(json);
```

---

## 自动配置详解

### JsonAutoConfiguration 配置

自动配置类 `plus.ruoyi.common.json.config.JsonAutoConfiguration` 在应用启动时进行以下配置：

#### 1. 大数字处理 - 避免 JavaScript 精度丢失

**问题**：
- JavaScript 数字最大安全整数为 `2^53 - 1` (9007199254740991)
- Java 的 `Long` 最大值为 `2^63 - 1`，超出 JavaScript 安全范围时精度丢失

**解决方案**：
```java
// 自动将 Long/BigInteger/BigDecimal 序列化为字符串
javaTimeModule.addSerializer(Long.class, BigNumberSerializer.INSTANCE);
javaTimeModule.addSerializer(BigInteger.class, BigNumberSerializer.INSTANCE);
javaTimeModule.addSerializer(BigDecimal.class, ToStringSerializer.instance);
```

**效果**：
```java
// 序列化前
Long id = 9007199254740992L;
BigInteger big = new BigInteger("9007199254740993");

// JSON 序列化后（自动转为字符串）
{
  "id": "9007199254740992",
  "big": "9007199254740993"
}
```

#### 2. LocalDateTime 格式化

**统一格式**：`yyyy-MM-dd HH:mm:ss`

```java
DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));
```

**示例**：
```java
LocalDateTime now = LocalDateTime.now();

// 序列化
String json = JsonUtils.toJsonString(now);
// 输出: "2025-01-29 14:30:45"

// 反序列化
LocalDateTime parsed = JsonUtils.parseObject("\"2025-01-29 14:30:45\"", LocalDateTime.class);
```

#### 3. Date 自定义反序列化

支持多种日期格式的自动识别和转换（由 `CustomDateDeserializer` 实现）

```java
String[] supportedFormats = {
    "yyyy-MM-dd HH:mm:ss",
    "yyyy-MM-dd",
    "yyyy-MM-dd HH:mm:ss.SSS",
    // 其他自定义格式...
};
```

**示例**：
```java
// 支持多种格式
Date date1 = JsonUtils.parseObject("\"2025-01-29 14:30:45\"", Date.class);
Date date2 = JsonUtils.parseObject("\"2025-01-29\"", Date.class);
Date date3 = JsonUtils.parseObject("\"2025-01-29 14:30:45.123\"", Date.class);
```

---

## 后端使用示例

### API 响应序列化

```java
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    /**
     * 获取用户信息
     */
    @GetMapping("/{id}")
    public Result<UserVo> getUser(@PathVariable Long id) {
        User user = userService.getById(id);
        // 自动通过 JsonUtils 序列化响应
        return Result.ok(MapstructUtils.convert(user, UserVo.class));
    }

    /**
     * 查询用户列表
     */
    @GetMapping("/list")
    public Result<List<UserVo>> listUsers(UserQuery query) {
        List<User> users = userService.list(query);
        // 大数字自动转为字符串，避免前端精度丢失
        return Result.ok(MapstructUtils.convert(users, UserVo.class));
    }
}

// 响应体自动使用配置好的 ObjectMapper 序列化
// 示例输出：
// {
//   "id": "1234567890123456789",  // Long 自动转为字符串
//   "name": "张三",
//   "createTime": "2025-01-29 14:30:45",  // LocalDateTime 格式化
//   "amount": "123.45",  // BigDecimal 转为字符串
//   "code": 0,
//   "msg": "success"
// }
```

### Service 中的 JSON 操作

```java
@Service
@RequiredArgsConstructor
public class DataImportService {

    /**
     * 导入 JSON 格式数据
     */
    public void importUsers(String jsonData) {
        // 验证 JSON 格式
        if (!JsonUtils.isJsonArray(jsonData)) {
            throw ServiceException.of("数据格式不正确，应为 JSON 数组");
        }

        // 解析 JSON 数据
        List<UserImportBo> users = JsonUtils.parseArray(jsonData, UserImportBo.class);

        // 处理数据...
        users.forEach(this::saveUser);
    }

    /**
     * 导出数据为 JSON
     */
    public String exportUsersToJson(List<User> users) {
        return JsonUtils.toJsonString(users);
    }

    /**
     * 处理复杂的嵌套结构
     */
    public void processComplexData(String json) {
        // 使用 TypeReference 处理复杂泛型
        Map<String, List<User>> data = JsonUtils.parseObject(json,
            new TypeReference<Map<String, List<User>>>(){});

        data.forEach((key, users) -> {
            System.out.println(key + ": " + users.size());
        });
    }
}
```

### DAO 层中的 JSON 字段处理

```java
@Data
@TableName("sys_config")
public class SysConfig extends TenantEntity {
    private Long id;
    private String configName;
    private String configValue;  // JSON 字符串字段
}

@Service
public class ConfigService {

    private final IConfigDao configDao;

    /**
     * 获取配置为对象
     */
    public <T> T getConfig(String configName, Class<T> clazz) {
        SysConfig config = configDao.selectOne(new LambdaQueryWrapper<SysConfig>()
            .eq(SysConfig::getConfigName, configName));

        if (config == null) {
            return null;
        }

        // JSON 字符串转对象
        return JsonUtils.parseObject(config.getConfigValue(), clazz);
    }

    /**
     * 保存配置为 JSON
     */
    public void saveConfig(String configName, Object configValue) {
        SysConfig config = new SysConfig();
        config.setConfigName(configName);
        config.setConfigValue(JsonUtils.toJsonString(configValue));

        configDao.insert(config);
    }
}
```

---

## 前端调用 API 注意事项

### 大数字精度问题解决

由于后端自动将大数字序列化为字符串，前端需要正确处理：

#### 前端 API 类型定义

```typescript
// userApi.ts
import type { PageQuery, Result, PageResult } from '@/api/types'

export interface UserVo {
  id: string | number  // 后端返回字符串，不要用 number
  name: string
  createTime: string
  amount: string       // BigDecimal 也返回字符串
}

export interface UserQuery extends PageQuery {
  name?: string
}

// API 调用
export const getUser = (id: string | number): Result<UserVo> => {
  return http.get(`/api/user/${id}`)
}
```

#### 前端处理大数字

```typescript
// 获取用户
const user = await getUser(id)
console.log(user.id)  // 字符串："1234567890123456789"

// 如果需要计算，使用 big.js 或 decimal.js
import Decimal from 'decimal.js'

const amount = new Decimal(user.amount)
const total = amount.plus(100)
console.log(total.toString())
```

---

## 常见错误与最佳实践

### ✅ 正确做法

```java
// 1. 使用 JsonUtils 进行序列化
String json = JsonUtils.toJsonString(user);

// 2. 反序列化时指定正确的类型
User user = JsonUtils.parseObject(json, User.class);

// 3. 复杂类型使用 TypeReference
List<User> users = JsonUtils.parseArray(json, User.class);
Map<String, User> map = JsonUtils.parseObject(json,
    new TypeReference<Map<String, User>>(){});

// 4. JSON 验证
if (JsonUtils.isJsonArray(data)) {
    List<User> users = JsonUtils.parseArray(data, User.class);
}

// 5. 处理 null
String json = JsonUtils.toJsonString(null);  // 返回 null
User user = JsonUtils.parseObject(null, User.class);  // 返回 null

// 6. 大数字自动保护
Long id = 9007199254740992L;
String json = JsonUtils.toJsonString(new User(id));
// 自动序列化为: {"id":"9007199254740992"}
```

### ❌ 常见错误

```java
// 1. 不验证 JSON 格式
List<User> users = JsonUtils.parseArray(data, User.class);  // 若 data 不是数组会报错

// 2. 忘记使用 TypeReference 处理泛型
Map<String, User> map = JsonUtils.parseObject(json, Map.class);  // 错误！

// 3. 对 null 返回值处理不当
String json = JsonUtils.toJsonString(null);
if (json != null) {  // json 已经是 null，不需要检查
    // ...
}

// 4. 直接使用 Long 而不转为字符串存储
BigDecimal amount = new BigDecimal("123.45");
// ❌ 不应该直接存储 amount 的数值
// ✅ 应该存储字符串或转为 BigDecimal

// 5. 前端使用 number 接收大数字
interface User {
    id: number;  // ❌ 错误！会丢失精度
}
interface User {
    id: string;  // ✅ 正确！
}
```

---

## 日期时间处理规范

### LocalDateTime 使用

```java
@Data
class Event {
    private LocalDateTime createdAt;  // ✅ 推荐
    private LocalDateTime updatedAt;
}

// 自动格式化为：2025-01-29 14:30:45
String json = JsonUtils.toJsonString(event);

// 反序列化支持相同格式
Event parsed = JsonUtils.parseObject(json, Event.class);
```

### Date 使用

```java
@Data
class OldData {
    private Date createdAt;  // 旧代码可能使用
}

// 支持多种格式反序列化（由 CustomDateDeserializer 处理）
String json1 = "{\"createdAt\":\"2025-01-29 14:30:45\"}";
String json2 = "{\"createdAt\":\"2025-01-29\"}";
String json3 = "{\"createdAt\":\"2025-01-29 14:30:45.123\"}";

OldData data1 = JsonUtils.parseObject(json1, OldData.class);  // ✅ 所有格式都支持
```

---

## 自定义扩展

### 自定义序列化器

```java
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class CustomSerializer extends JsonSerializer<YourType> {
    @Override
    public void serialize(YourType value, JsonGenerator gen,
                         SerializerProvider serializers) throws IOException {
        // 自定义序列化逻辑
        gen.writeString(value.toString());
    }
}
```

### 集成自定义序列化器

```java
// 在 JsonAutoConfiguration 中添加
javaTimeModule.addSerializer(YourType.class, new CustomSerializer());
```

---

## 性能优化建议

### 1. 避免频繁创建 ObjectMapper

```java
// ❌ 不推荐：每次都创建新实例
ObjectMapper mapper = new ObjectMapper();
String json = mapper.writeValueAsString(user);

// ✅ 推荐：使用 JsonUtils 获取单例
String json = JsonUtils.toJsonString(user);
```

### 2. 大量数据序列化

```java
// ✅ 推荐：使用流式处理
List<User> users = getUserList();
for (User user : users) {
    String json = JsonUtils.toJsonString(user);
    // 处理单条记录
}

// 或使用 Jackson 的流式 API
ObjectMapper mapper = JsonUtils.getObjectMapper();
mapper.writeValue(outputFile, users);
```

### 3. 复杂嵌套结构优化

```java
// 为了避免重复创建 TypeReference，可以缓存
private static final TypeReference<List<User>> USER_LIST_TYPE =
    new TypeReference<List<User>>(){};

// 使用缓存的 TypeReference
List<User> users = JsonUtils.parseObject(json, USER_LIST_TYPE);
```

