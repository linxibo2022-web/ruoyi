---
name: backend-annotations
description: |
  当需要使用后端高级注解时自动使用此 Skill。包含 SerialMap、RateLimiter、RepeatSubmit、Sensitive、DataPermission 等注解。

  触发场景：
  - 数据序列化映射（ID转名称、字典转标签）
  - 接口限流配置
  - 防重复提交
  - 敏感数据脱敏
  - 数据权限控制

  触发词：SerialMap、限流、RateLimiter、防重复、RepeatSubmit、脱敏、Sensitive、数据权限、DataPermission、ID转名称、字典转换、OSS转URL
---

# 后端高级注解指南

## 注解一览

| 注解 | 模块 | 用途 | 常用场景 |
|------|------|------|---------|
| `@SerialMap` | serialmap | 序列化映射 | ID转名称、字典转标签、OSS转URL |
| `@RateLimiter` | ratelimiter | 接口限流 | 防刷、保护接口 |
| `@RepeatSubmit` | idempotent | 防重复提交 | 表单提交、订单创建 |
| `@Sensitive` | sensitive | 数据脱敏 | 手机号、身份证、银行卡 |
| `@DataPermission` | mybatis | 数据权限 | 部门数据隔离 |

---

## 1. @SerialMap - 序列化映射

### 作用
在 JSON 序列化时自动将字段值映射/转换为其他值，无需手动查询。

### 内置转换器

| 转换器常量 | 类型 | 功能 |
|-----------|------|------|
| `USER_ID_TO_NAME` | user_id_to_name | 用户ID → 登录账号 |
| `USER_ID_TO_NICKNAME` | user_id_to_nickname | 用户ID → 昵称 |
| `USER_ID_TO_AVATAR` | user_id_to_avatar | 用户ID → 头像 |
| `DEPT_ID_TO_NAME` | dept_id_to_name | 部门ID → 部门名称 |
| `DICT_TYPE_TO_LABEL` | dict_type_to_label | 字典值 → 字典标签 |
| `OSS_ID_TO_URL` | oss_id_to_url | 文件ID → 访问URL |
| `PRESIGNED_URL` | presigned_url | 私有URL → 预签名URL |
| `FIELD_MAP` | field_map | 通用字段映射 |
| `I18N_TRANSLATE` | i18n_translate | 国际化翻译 |

### 使用示例

```java
import plus.ruoyi.common.serialmap.annotation.SerialMap;
import plus.ruoyi.common.serialmap.constant.SerialMapConstant;

public class OrderVo {

    // ✅ 用户ID转用户名
    @SerialMap(converter = SerialMapConstant.USER_ID_TO_NAME)
    private Long createBy;

    // ✅ 用户ID转昵称（从其他字段取值）
    @SerialMap(converter = SerialMapConstant.USER_ID_TO_NICKNAME, source = "userId")
    private String userNickName;

    // ✅ 部门ID转部门名称
    @SerialMap(converter = SerialMapConstant.DEPT_ID_TO_NAME)
    private Long deptId;

    // ✅ 字典转标签（需指定字典类型）
    @SerialMap(converter = SerialMapConstant.DICT_TYPE_TO_LABEL, param = "sys_order_status")
    private String status;

    // ✅ OSS文件ID转访问URL（支持逗号分隔的多个ID）
    @SerialMap(converter = SerialMapConstant.OSS_ID_TO_URL)
    private String imageIds;

    // ✅ 私有文件URL转预签名URL
    @SerialMap(converter = SerialMapConstant.PRESIGNED_URL)
    private String privateFileUrl;

    // ✅ 通用字段映射 - 单字段
    @SerialMap(converter = SerialMapConstant.FIELD_MAP,
               source = "userId",
               entityClass = SysUser.class,
               targetField = "nickName")
    private String userName;

    // ✅ 通用字段映射 - 对象
    @SerialMap(converter = SerialMapConstant.FIELD_MAP,
               source = "deptId",
               entityClass = SysDept.class)
    private SysDeptVo deptVo;

    // ✅ 通用字段映射 - 集合
    @SerialMap(converter = SerialMapConstant.FIELD_MAP,
               source = "roleIds",
               entityClass = SysRole.class)
    private List<SysRoleVo> roles;
}
```

### 注解参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `converter` | String | **必填**，转换器类型标识 |
| `source` | String | 源字段名，默认当前字段 |
| `param` | String | 额外参数（如字典类型） |
| `entityClass` | Class | 实体类（field_map专用） |
| `targetField` | String | 目标字段名（field_map专用） |

### 自定义转换器

```java
import plus.ruoyi.common.serialmap.annotation.SerialMapType;
import plus.ruoyi.common.serialmap.core.SerialMapInterface;

@SerialMapType(type = "custom_converter")
public class CustomConverterImpl implements SerialMapInterface<String> {

    @Override
    public String convert(Object key, String param) {
        // 自定义转换逻辑
        if (key instanceof Long id) {
            return "转换结果: " + id;
        }
        return null;
    }
}

// 使用
@SerialMap(converter = "custom_converter")
private Long customField;
```

---

## 2. @RateLimiter - 接口限流

### 作用
基于 Redis 和令牌桶算法实现分布式限流，防止接口被恶意刷新。

### 限流类型

| 类型 | 说明 |
|------|------|
| `LimitType.DEFAULT` | 全局限流，所有请求共享配额 |
| `LimitType.IP` | IP限流，每个IP独立计算 |
| `LimitType.CLUSTER` | 集群限流，每个节点独立 |

### 使用示例

```java
import plus.ruoyi.common.ratelimiter.annotation.RateLimiter;
import plus.ruoyi.common.ratelimiter.enums.LimitType;

@RestController
public class ApiController {

    // ✅ 基本用法：60秒内最多100次
    @RateLimiter(time = 60, count = 100)
    @GetMapping("/list")
    public R<List<XxxVo>> list() { }

    // ✅ IP限流：每个IP每分钟最多10次
    @RateLimiter(time = 60, count = 10, limitType = LimitType.IP)
    @PostMapping("/login")
    public R<String> login() { }

    // ✅ 动态key：基于用户ID限流
    @RateLimiter(key = "#userId", time = 60, count = 5)
    @PostMapping("/submit")
    public R<Void> submit(Long userId) { }

    // ✅ SpEL表达式
    @RateLimiter(key = "#{#user.id + ':' + #action}", time = 60, count = 5)
    @PostMapping("/action")
    public R<Void> doAction(User user, String action) { }

    // ✅ 自定义错误消息
    @RateLimiter(time = 60, count = 10, message = "访问过于频繁，请稍后再试")
    @GetMapping("/data")
    public R<DataVo> getData() { }
}
```

### 注解参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `key` | String | "" | 限流key，支持SpEL |
| `time` | int | 60 | 时间窗口（秒） |
| `count` | int | 100 | 最大请求次数 |
| `limitType` | LimitType | DEFAULT | 限流类型 |
| `message` | String | 国际化key | 错误提示 |
| `timeout` | int | 86400 | Redis超时（秒） |

### 推荐配置

| 场景 | time | count | limitType |
|------|------|-------|-----------|
| 登录接口 | 60 | 5-10 | IP |
| 验证码 | 60 | 3 | IP |
| 查询接口 | 60 | 100-1000 | DEFAULT |
| 写入接口 | 60 | 10-50 | DEFAULT |
| 敏感操作 | 60 | 1-5 | IP |

---

## 3. @RepeatSubmit - 防重复提交

### 作用
基于 Redis 分布式锁防止短时间内重复提交表单或请求。

### 使用示例

```java
import plus.ruoyi.common.idempotent.annotation.RepeatSubmit;
import java.util.concurrent.TimeUnit;

@RestController
public class OrderController {

    // ✅ 默认：5秒内不能重复提交
    @RepeatSubmit()
    @PostMapping("/addOrder")
    public R<Long> addOrder(@RequestBody OrderBo bo) { }

    // ✅ 自定义间隔：10秒
    @RepeatSubmit(interval = 10000)
    @PostMapping("/pay")
    public R<Void> pay(@RequestBody PayBo bo) { }

    // ✅ 使用秒作为单位
    @RepeatSubmit(interval = 10, timeUnit = TimeUnit.SECONDS)
    @PostMapping("/submit")
    public R<Void> submit(@RequestBody SubmitBo bo) { }

    // ✅ 自定义提示消息
    @RepeatSubmit(interval = 5000, message = "请勿重复提交订单")
    @PostMapping("/createOrder")
    public R<Long> createOrder(@RequestBody OrderBo bo) { }
}
```

### 注解参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `interval` | int | 5000 | 间隔时间 |
| `timeUnit` | TimeUnit | MILLISECONDS | 时间单位 |
| `message` | String | 国际化key | 错误提示 |

### 常见场景

| 场景 | 推荐间隔 |
|------|---------|
| 普通表单 | 3-5秒 |
| 订单创建 | 10秒 |
| 支付操作 | 30秒 |
| 文件上传 | 10秒 |

---

## 4. @Sensitive - 数据脱敏

### 作用
在 JSON 序列化时自动对敏感字段进行脱敏处理，支持基于角色/权限的访问控制。

### 脱敏策略

| 策略 | 说明 | 效果示例 |
|------|------|---------|
| `ID_CARD` | 身份证 | 110***********1234 |
| `PHONE` | 手机号 | 138****8888 |
| `EMAIL` | 邮箱 | t**@example.com |
| `BANK_CARD` | 银行卡 | 6222***********1234 |
| `CHINESE_NAME` | 中文名 | 张* |
| `ADDRESS` | 地址 | 北京市朝阳区**** |
| `FIXED_PHONE` | 固定电话 | 010****1234 |
| `PASSWORD` | 密码 | ****** |
| `IPV4` | IPv4地址 | 192.168.***.*** |
| `IPV6` | IPv6地址 | 部分隐藏 |
| `CAR_LICENSE` | 车牌号 | 京A***12 |
| `FIRST_MASK` | 首字符保留 | 张*** |
| `STRING_MASK` | 通用掩码 | 1234**7890 |
| `CLEAR` | 清空 | （空字符串） |
| `CLEAR_TO_NULL` | 置空 | null |

### 使用示例

```java
import plus.ruoyi.common.sensitive.annotation.Sensitive;
import plus.ruoyi.common.sensitive.core.SensitiveStrategy;

public class UserVo {

    // ✅ 手机号脱敏
    @Sensitive(strategy = SensitiveStrategy.PHONE)
    private String phone;

    // ✅ 身份证脱敏，admin角色可查看原数据
    @Sensitive(strategy = SensitiveStrategy.ID_CARD, roleKey = {"admin"})
    private String idCard;

    // ✅ 邮箱脱敏，需要用户详情权限才能看原数据
    @Sensitive(strategy = SensitiveStrategy.EMAIL, perms = {"system:user:detail"})
    private String email;

    // ✅ 银行卡脱敏，admin角色或有权限都可查看
    @Sensitive(strategy = SensitiveStrategy.BANK_CARD,
               roleKey = {"admin"},
               perms = {"finance:account:query"})
    private String bankCard;

    // ✅ 地址脱敏
    @Sensitive(strategy = SensitiveStrategy.ADDRESS)
    private String address;

    // ✅ 中文姓名脱敏
    @Sensitive(strategy = SensitiveStrategy.CHINESE_NAME)
    private String realName;
}
```

### 注解参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `strategy` | SensitiveStrategy | 必填 | 脱敏策略 |
| `roleKey` | String[] | {} | 可查看原数据的角色 |
| `perms` | String[] | {} | 可查看原数据的权限 |

### 权限控制逻辑

- `roleKey` 和 `perms` 都为空：所有人都看脱敏数据
- 满足任一 `roleKey` **或** 任一 `perms`：可查看原数据
- 两者是 **OR** 关系

---

## 5. @DataPermission - 数据权限

### 作用
在 SQL 查询时自动拼接数据权限过滤条件，实现部门级数据隔离。

### 使用示例

```java
import plus.ruoyi.common.mybatis.annotation.DataPermission;
import plus.ruoyi.common.mybatis.annotation.DataColumn;

// ✅ 在 Mapper 接口上使用
@DataPermission({
    @DataColumn(key = "deptName", value = "dept_id"),
    @DataColumn(key = "userName", value = "create_by")
})
public interface OrderMapper extends BaseMapper<Order> {
    // 查询时会自动拼接数据权限条件
}

// ✅ 在方法上使用（覆盖类级别配置）
@DataPermission({
    @DataColumn(key = "deptName", value = "create_dept")
})
List<Order> selectOrderList(OrderBo bo);

// ✅ 指定权限标识（拥有此权限的角色不拼接过滤条件）
@DataPermission({
    @DataColumn(key = "deptName", value = "dept_id", permission = "order:all")
})
List<Order> selectAllOrders(OrderBo bo);
```

### 注解参数

**@DataPermission**

| 参数 | 类型 | 说明 |
|------|------|------|
| `value` | DataColumn[] | 数据权限配置数组 |
| `joinStr` | String | SQL连接符（OR/AND） |

**@DataColumn**

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `key` | String[] | "deptName" | 占位符关键字 |
| `value` | String[] | "dept_id" | 替换的字段名 |
| `permission` | String | "" | 权限标识（有此权限不过滤） |

### 数据权限类型

系统支持的数据权限范围：
1. **全部数据** - 无过滤
2. **本部门数据** - 只看本部门
3. **本部门及以下** - 本部门 + 子部门
4. **仅本人数据** - 只看自己创建的
5. **自定义** - 自定义SQL

---

## 最佳实践

### 1. 组合使用

```java
@RestController
@RequestMapping("/order")
public class OrderController {

    // 限流 + 防重复提交 + 日志
    @RateLimiter(time = 60, count = 10, limitType = LimitType.IP)
    @RepeatSubmit(interval = 10, timeUnit = TimeUnit.SECONDS)
    @Log(title = "订单", operType = DictOperType.INSERT)
    @PostMapping("/addOrder")
    public R<Long> addOrder(@Validated @RequestBody OrderBo bo) {
        return R.ok(orderService.add(bo));
    }
}
```

### 2. VO 脱敏 + 映射

```java
public class UserDetailVo {

    private Long id;

    // 脱敏
    @Sensitive(strategy = SensitiveStrategy.PHONE)
    private String phone;

    @Sensitive(strategy = SensitiveStrategy.ID_CARD, roleKey = {"admin"})
    private String idCard;

    // 映射
    @SerialMap(converter = SerialMapConstant.DEPT_ID_TO_NAME)
    private Long deptId;

    @SerialMap(converter = SerialMapConstant.DICT_TYPE_TO_LABEL, param = "sys_user_status")
    private String status;

    @SerialMap(converter = SerialMapConstant.OSS_ID_TO_URL)
    private String avatar;
}
```

### 3. 常见错误

```java
// ❌ 错误：SerialMap 只能用在 VO 类的字段上
@SerialMap(converter = "xxx")
public Long getUserId() { }  // 方法上也可以，但通常放字段上

// ❌ 错误：字典转换没有指定 param
@SerialMap(converter = SerialMapConstant.DICT_TYPE_TO_LABEL)  // 缺少 param
private String status;

// ✅ 正确
@SerialMap(converter = SerialMapConstant.DICT_TYPE_TO_LABEL, param = "sys_status")
private String status;

// ❌ 错误：RateLimiter 用在非 Controller 方法上
@Service
public class XxxService {
    @RateLimiter(...)  // 无效！只能用在 Controller
    public void doSomething() { }
}
```
