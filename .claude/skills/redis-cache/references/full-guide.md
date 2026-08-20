
## 一、RedisUtils工具类使用指南

### 1.1 基础缓存操作

RedisUtils基于Redisson实现，提供了丰富的Redis操作方法。

#### 存储缓存对象

```java
// 永不过期
RedisUtils.setCacheObject("user:123", userObj);

// 设置过期时间
RedisUtils.setCacheObject("user:123", userObj, Duration.ofMinutes(30));

// 保留原有TTL（需要Redis 6.0+）
RedisUtils.setCacheObject("user:123", userObj, true);

// 仅当key不存在时设置
boolean success = RedisUtils.setObjectIfAbsent("user:123", userObj, Duration.ofMinutes(30));

// 仅当key存在时设置
boolean success = RedisUtils.setObjectIfExists("user:123", userObj, Duration.ofMinutes(30));
```

#### 获取缓存对象

```java
// 获取缓存
User user = RedisUtils.getCacheObject("user:123");

// 获取剩余存活时间（毫秒）
long ttl = RedisUtils.getTimeToLive("user:123");
// 返回值：-1表示永不过期，-2表示key不存在
```

#### 删除和检查缓存

```java
// 删除单个缓存
boolean deleted = RedisUtils.deleteObject("user:123");

// 批量删除
List<String> keys = Arrays.asList("user:123", "user:456");
RedisUtils.deleteObject(keys);

// 检查缓存是否存在
boolean exists = RedisUtils.isExistsObject("user:123");

// 设置过期时间
RedisUtils.expire("user:123", Duration.ofMinutes(30));
```

### 1.2 集合操作

#### List操作

```java
// 缓存List数据
List<String> dataList = new ArrayList<>();
dataList.add("item1");
dataList.add("item2");
RedisUtils.setCacheList("myList", dataList);

// 缓存List并设置过期时间
RedisUtils.setCacheList("myList", dataList, Duration.ofHours(1));

// 向List追加单个数据
RedisUtils.addCacheList("myList", "item3");

// 获取完整List
List<String> result = RedisUtils.getCacheList("myList");

// 获取指定范围数据
List<String> range = RedisUtils.getCacheListRange("myList", 0, 10);
```

#### Set操作

```java
// 缓存Set数据
Set<String> dataSet = new HashSet<>();
dataSet.add("value1");
dataSet.add("value2");
RedisUtils.setCacheSet("mySet", dataSet);

// 向Set追加单个数据
boolean added = RedisUtils.addCacheSet("mySet", "value3");

// 获取Set数据
Set<String> result = RedisUtils.getCacheSet("mySet");
```

#### Map操作

```java
// 缓存Map数据
Map<String, Object> dataMap = new HashMap<>();
dataMap.put("key1", "value1");
dataMap.put("key2", "value2");
RedisUtils.setCacheMap("myMap", dataMap);

// 设置Map中的单个值
RedisUtils.setCacheMapValue("myMap", "key3", "value3");

// 获取Map中的单个值
String value = RedisUtils.getCacheMapValue("myMap", "key1");

// 获取完整Map
Map<String, Object> result = RedisUtils.getCacheMap("myMap");

// 获取Map的所有key
Set<String> keys = RedisUtils.getCacheMapKeySet("myMap");

// 删除Map中的单个值
Object deleted = RedisUtils.delCacheMapValue("myMap", "key1");

// 批量删除Map中的多个值
Set<String> keysToDelete = new HashSet<>(Arrays.asList("key1", "key2"));
RedisUtils.delMultiCacheMapValue("myMap", keysToDelete);

// 获取Map中的多个值
Set<String> keysToGet = new HashSet<>(Arrays.asList("key1", "key2"));
Map<String, Object> values = RedisUtils.getMultiCacheMapValue("myMap", keysToGet);
```

### 1.3 发布订阅

```java
// 发布消息到指定频道
RedisUtils.publish("notification:channel", messageObj);

// 发布消息并自定义处理
RedisUtils.publish("notification:channel", messageObj, msg -> {
    log.info("消息已发布: {}", msg);
});

// 订阅频道接收消息
RedisUtils.subscribe("notification:channel", MessageDTO.class, msg -> {
    log.info("收到消息: {}", msg);
    // 处理消息逻辑
});
```

### 1.4 限流控制

```java
// 限流控制（默认无超时）
// 允许每10秒内最多100个请求
long remaining = RedisUtils.rateLimiter("api:limit:user:123",
    RateType.OVERALL, 100, 10);
if (remaining == -1) {
    throw ServiceException.of("请求过于频繁，请稍后再试");
}

// 限流控制（带超时）
// 允许每10秒内最多100个请求，超时时间5秒
long remaining = RedisUtils.rateLimiter("api:limit:user:123",
    RateType.OVERALL, 100, 10, 5);
```

### 1.5 原子操作

```java
// 设置原子长整型值
RedisUtils.setAtomicValue("counter:view", 0L);

// 获取原子长整型值
long value = RedisUtils.getAtomicValue("counter:view");

// 原子递增
long newValue = RedisUtils.incrAtomicValue("counter:view");

// 原子递减
long newValue = RedisUtils.decrAtomicValue("counter:view");
```

### 1.6 Key操作

```java
// 获取匹配模式的所有key（注意：会忽略租户隔离）
Collection<String> keys = RedisUtils.keys("user:*");

// 通过扫描参数获取key列表
KeysScanOptions options = KeysScanOptions.defaults()
    .pattern("user:*")
    .chunkSize(1000)
    .limit(100);
Collection<String> keys = RedisUtils.keys(options);

// 按模式批量删除key（注意：会忽略租户隔离）
RedisUtils.deleteKeys("temp:*");

// 检查key是否存在
Boolean exists = RedisUtils.hasKey("user:123");
```

### 1.7 监听机制

```java
// 注册对象监听器（需要开启Redis的notify-keyspace-events配置）
RedisUtils.addObjectListener("user:123", new ObjectListener() {
    @Override
    public void onExpired(String name) {
        log.info("缓存已过期: {}", name);
    }

    @Override
    public void onDeleted(String name) {
        log.info("缓存已删除: {}", name);
    }
});

// 注册List监听器
RedisUtils.addListListener("myList", listener);

// 注册Set监听器
RedisUtils.addSetListener("mySet", listener);

// 注册Map监听器
RedisUtils.addMapListener("myMap", listener);
```

---

## 二、CacheUtils工具类使用指南

CacheUtils提供了统一的Spring Cache操作接口，简化缓存使用。

### 2.1 基本操作

```java
// 保存缓存值
CacheUtils.put("userCache", "user:123", userObj);

// 获取缓存值
User user = CacheUtils.get("userCache", "user:123");

// 删除指定缓存项
CacheUtils.evict("userCache", "user:123");

// 清空指定缓存组的所有数据
CacheUtils.clear("userCache");
```

### 2.2 使用场景

```java
// 示例：在Service中手动管理缓存
public class UserService {

    public User getUserById(Long userId) {
        // 先从缓存获取
        User user = CacheUtils.get(CacheNames.SYS_USER, userId);
        if (user != null) {
            return user;
        }

        // 缓存未命中，从数据库查询
        user = userDao.getById(userId);

        // 存入缓存
        if (user != null) {
            CacheUtils.put(CacheNames.SYS_USER, userId, user);
        }

        return user;
    }

    public void updateUser(User user) {
        // 更新数据库
        userDao.updateById(user);

        // 清除缓存
        CacheUtils.evict(CacheNames.SYS_USER, user.getUserId());
    }
}
```

---

## 三、缓存注解最佳实践

### 3.1 @Cacheable - 查询缓存

用于查询方法，如果缓存存在则直接返回，否则执行方法并缓存结果。

```java
/**
 * 根据字典类型查询字典数据
 * 位置：ruoyi-modules/ruoyi-system/src/main/java/plus/ruoyi/system/dict/service/impl/SysDictTypeServiceImpl.java:173
 */
@Cacheable(cacheNames = CacheNames.SYS_DICT, key = "#dictType")
@Override
public List<SysDictDataVo> listDictDataByType(String dictType) {
    if (StringUtils.isBlank(dictType)) {
        return Collections.emptyList();
    }

    List<SysDictData> dictDataList = dictDataDao.listDictDataByType(dictType);
    if (CollUtil.isEmpty(dictDataList)) {
        return Collections.emptyList();
    }
    return MapstructUtils.convert(dictDataList, SysDictDataVo.class);
}
```

**关键点**：
- `cacheNames`：缓存组名称，建议使用CacheNames常量类统一管理
- `key`：缓存key，支持SpEL表达式，如`#dictType`表示方法参数
- 返回值必须可序列化

### 3.2 @CachePut - 更新缓存

用于更新方法，无论缓存是否存在都会执行方法，并将结果更新到缓存。

```java
/**
 * 新增字典类型
 * 位置：ruoyi-modules/ruoyi-system/src/main/java/plus/ruoyi/system/dict/service/impl/SysDictTypeServiceImpl.java:249
 */
@CachePut(cacheNames = CacheNames.SYS_DICT, key = "#bo.dictType")
@Override
public List<SysDictDataVo> insertDictType(SysDictTypeBo bo) {
    SysDictType dictType = MapstructUtils.convert(bo, SysDictType.class);
    boolean success = dictTypeDao.insert(dictType) > 0;
    if (success) {
        bo.setDictId(dictType.getDictId());
        // 新增类型下无数据，返回空列表防止缓存穿透
        return Collections.emptyList();
    }
    throw ServiceException.of("新增字典类型失败");
}
```

**关键点**：
- 方法一定会执行，返回值会更新到缓存
- 适用于新增和修改操作
- 返回空列表可以防止缓存穿透

### 3.3 @CacheEvict - 清除缓存

用于删除方法，清除指定的缓存。

```java
@CacheEvict(cacheNames = CacheNames.SYS_DICT, key = "#dictType")
public void deleteDictType(String dictType) {
    dictTypeDao.deleteByDictType(dictType);
}

// 清除多个缓存
@CacheEvict(cacheNames = CacheNames.SYS_DICT, allEntries = true)
public void clearAllDictCache() {
    // 清空所有字典缓存
}
```

**关键点**：
- `allEntries = true`：清空整个缓存组
- `beforeInvocation = true`：方法执行前清除缓存（默认false，方法执行后清除）

### 3.4 ⚠️ 重要规范：返回值不能使用不可变集合

**错误示例**：

```java
@Cacheable(cacheNames = CacheNames.SYS_DICT, key = "#dictType")
public List<SysDictDataVo> listDictDataByType(String dictType) {
    // ❌ 错误：使用List.of()会导致缓存序列化失败
    return List.of(data1, data2);
}

@Cacheable(cacheNames = CacheNames.SYS_USER, key = "#userId")
public Set<String> getUserRoles(Long userId) {
    // ❌ 错误：使用Set.of()会导致缓存序列化失败
    return Set.of("admin", "user");
}

@Cacheable(cacheNames = CacheNames.SYS_CONFIG, key = "#configKey")
public Map<String, Object> getConfig(String configKey) {
    // ❌ 错误：使用Map.of()会导致缓存序列化失败
    return Map.of("key1", "value1", "key2", "value2");
}
```

**正确示例**：

```java
@Cacheable(cacheNames = CacheNames.SYS_DICT, key = "#dictType")
public List<SysDictDataVo> listDictDataByType(String dictType) {
    // ✅ 正确：使用new ArrayList()
    List<SysDictDataVo> result = new ArrayList<>();
    result.add(data1);
    result.add(data2);
    return result;
}

@Cacheable(cacheNames = CacheNames.SYS_USER, key = "#userId")
public Set<String> getUserRoles(Long userId) {
    // ✅ 正确：使用new HashSet()
    Set<String> roles = new HashSet<>();
    roles.add("admin");
    roles.add("user");
    return roles;
}

@Cacheable(cacheNames = CacheNames.SYS_CONFIG, key = "#configKey")
public Map<String, Object> getConfig(String configKey) {
    // ✅ 正确：使用new HashMap()
    Map<String, Object> config = new HashMap<>();
    config.put("key1", "value1");
    config.put("key2", "value2");
    return config;
}
```

**原因**：`List.of()`、`Set.of()`、`Map.of()`返回的是不可变集合，在Redis序列化时会出现问题。必须使用可变集合（`new ArrayList()`、`new HashSet()`、`new HashMap()`）。

---

## 四、分布式锁使用

### 4.1 获取分布式锁

```java
// 获取锁对象
RLock lock = RedisUtils.getLock("lock:order:" + orderId);

try {
    // 尝试加锁，最多等待10秒，锁定30秒后自动释放
    boolean locked = lock.tryLock(10, 30, TimeUnit.SECONDS);
    if (locked) {
        try {
            // 执行业务逻辑
            processOrder(orderId);
        } finally {
            // 释放锁
            lock.unlock();
        }
    } else {
        throw ServiceException.of("获取锁失败，请稍后重试");
    }
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
    throw ServiceException.of("获取锁被中断");
}
```

### 4.2 使用场景示例

#### 场景1：防止重复提交

```java
public void submitOrder(OrderBo orderBo) {
    String lockKey = "lock:submit:order:" + LoginHelper.getUserId();
    RLock lock = RedisUtils.getLock(lockKey);

    try {
        // 尝试加锁，最多等待3秒，锁定10秒后自动释放
        boolean locked = lock.tryLock(3, 10, TimeUnit.SECONDS);
        if (!locked) {
            throw ServiceException.of("请勿重复提交订单");
        }

        try {
            // 创建订单
            orderDao.insert(orderBo);
        } finally {
            lock.unlock();
        }
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw ServiceException.of("订单提交被中断");
    }
}
```

#### 场景2：库存扣减

```java
public void deductStock(Long goodsId, Integer quantity) {
    String lockKey = "lock:stock:" + goodsId;
    RLock lock = RedisUtils.getLock(lockKey);

    try {
        // 尝试加锁，最多等待5秒，锁定30秒后自动释放
        boolean locked = lock.tryLock(5, 30, TimeUnit.SECONDS);
        if (!locked) {
            throw ServiceException.of("系统繁忙，请稍后重试");
        }

        try {
            // 查询库存
            Goods goods = goodsDao.getById(goodsId);
            if (goods.getStock() < quantity) {
                throw ServiceException.of("库存不足");
            }

            // 扣减库存
            goods.setStock(goods.getStock() - quantity);
            goodsDao.updateById(goods);
        } finally {
            lock.unlock();
        }
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw ServiceException.of("库存扣减被中断");
    }
}
```

### 4.3 分布式锁最佳实践

1. **锁的粒度要细**：锁的范围越小越好，避免锁住不必要的代码
2. **设置合理的超时时间**：根据业务执行时间设置，避免死锁
3. **必须在finally中释放锁**：确保锁一定会被释放
4. **处理InterruptedException**：正确处理中断异常
5. **锁key要有业务含义**：便于排查问题

---

## 五、缓存Key命名规范

### 5.1 命名格式

```
{业务模块}:{功能}:{具体标识}
```

### 5.2 命名示例

```java
// 用户缓存
"user:info:123"              // 用户信息
"user:roles:123"             // 用户角色
"user:permissions:123"       // 用户权限

// 字典缓存
"dict:data:sys_user_gender"     // 字典数据
"dict:type:sys_user_gender"     // 字典类型

// 订单缓存
"order:info:20240101001"     // 订单信息
"order:status:20240101001"   // 订单状态

// 分布式锁
"lock:order:20240101001"     // 订单锁
"lock:stock:123"             // 库存锁
"lock:submit:order:456"      // 提交锁

// 限流
"limit:api:user:123"         // 用户API限流
"limit:sms:18888888888"      // 短信限流
```

### 5.3 使用CacheNames常量类

项目中定义了CacheNames常量类统一管理缓存名称：

```java
// 位置：ruoyi-common/ruoyi-common-core/src/main/java/plus/ruoyi/common/core/constant/CacheNames.java
public interface CacheNames {
    /** 字典数据缓存 */
    String SYS_DICT = "sys_dict";

    /** 字典类型缓存 */
    String SYS_DICT_TYPE = "sys_dict_type";

    /** 用户缓存 */
    String SYS_USER = "sys_user";

    /** 配置缓存 */
    String SYS_CONFIG = "sys_config";
}
```

**使用示例**：

```java
@Cacheable(cacheNames = CacheNames.SYS_DICT, key = "#dictType")
public List<SysDictDataVo> listDictDataByType(String dictType) {
    // ...
}
```

---

## 六、常见问题和解决方案

### 6.1 缓存穿透

**问题**：查询一个不存在的数据，缓存和数据库都没有，导致每次请求都打到数据库。

**解决方案**：

```java
@Cacheable(cacheNames = CacheNames.SYS_DICT, key = "#dictType")
public List<SysDictDataVo> listDictDataByType(String dictType) {
    List<SysDictData> dictDataList = dictDataDao.listDictDataByType(dictType);
    if (CollUtil.isEmpty(dictDataList)) {
        // 返回空列表而不是null，防止缓存穿透
        return Collections.emptyList();
    }
    return MapstructUtils.convert(dictDataList, SysDictDataVo.class);
}
```

### 6.2 缓存雪崩

**问题**：大量缓存同时过期，导致请求全部打到数据库。

**解决方案**：

```java
// 设置随机过期时间，避免同时过期
long baseTime = 30 * 60; // 30分钟
long randomTime = ThreadLocalRandom.current().nextLong(5 * 60); // 随机0-5分钟
Duration duration = Duration.ofSeconds(baseTime + randomTime);
RedisUtils.setCacheObject("user:info:" + userId, userInfo, duration);
```

### 6.3 缓存击穿

**问题**：热点数据过期瞬间，大量请求同时打到数据库。

**解决方案**：使用分布式锁

```java
public User getUserById(Long userId) {
    String cacheKey = "user:info:" + userId;

    // 先从缓存获取
    User user = RedisUtils.getCacheObject(cacheKey);
    if (user != null) {
        return user;
    }

    // 缓存未命中，使用分布式锁
    String lockKey = "lock:user:" + userId;
    RLock lock = RedisUtils.getLock(lockKey);

    try {
        boolean locked = lock.tryLock(3, 10, TimeUnit.SECONDS);
        if (locked) {
            try {
                // 再次检查缓存（双重检查）
                user = RedisUtils.getCacheObject(cacheKey);
                if (user != null) {
                    return user;
                }

                // 从数据库查询
                user = userDao.getById(userId);

                // 存入缓存
                if (user != null) {
                    RedisUtils.setCacheObject(cacheKey, user, Duration.ofMinutes(30));
                }

                return user;
            } finally {
                lock.unlock();
            }
        }
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }

    // 获取锁失败，直接查询数据库
    return userDao.getById(userId);
}
```

### 6.4 缓存与数据库一致性

**问题**：更新数据库后，缓存没有及时更新。

**解决方案**：

```java
// 方案1：先更新数据库，再删除缓存（推荐）
@Transactional(rollbackFor = Exception.class)
public void updateUser(UserBo userBo) {
    // 更新数据库
    User user = MapstructUtils.convert(userBo, User.class);
    userDao.updateById(user);

    // 删除缓存
    RedisUtils.deleteObject("user:info:" + user.getUserId());
}

// 方案2：使用@CachePut更新缓存
@CachePut(cacheNames = CacheNames.SYS_USER, key = "#result.userId")
@Transactional(rollbackFor = Exception.class)
public UserVo updateUser(UserBo userBo) {
    User user = MapstructUtils.convert(userBo, User.class);
    userDao.updateById(user);

    // 返回最新数据，自动更新缓存
    User updatedUser = userDao.getById(user.getUserId());
    return MapstructUtils.convert(updatedUser, UserVo.class);
}
```

### 6.5 租户隔离问题

**问题**：使用`RedisUtils.keys()`或`RedisUtils.deleteKeys()`时会忽略租户隔离。

**解决方案**：

```java
// 手动拼接租户ID
String tenantId = LoginHelper.getTenantId();
String pattern = tenantId + ":user:*";
Collection<String> keys = RedisUtils.keys(pattern);

// 或者使用CacheUtils，它会自动处理租户隔离
CacheUtils.clear(CacheNames.SYS_USER);
```

---

## 七、核心文件位置

| 文件 | 位置 | 说明 |
|------|------|------|
| RedisUtils | `ruoyi-common/ruoyi-common-redis/src/main/java/plus/ruoyi/common/redis/utils/RedisUtils.java` | Redis工具类 |
| CacheUtils | `ruoyi-common/ruoyi-common-redis/src/main/java/plus/ruoyi/common/redis/utils/CacheUtils.java` | 缓存工具类 |
| CacheNames | `ruoyi-common/ruoyi-common-core/src/main/java/plus/ruoyi/common/core/constant/CacheNames.java` | 缓存名称常量 |
| 字典服务示例 | `ruoyi-modules/ruoyi-system/src/main/java/plus/ruoyi/system/dict/service/impl/SysDictTypeServiceImpl.java` | 缓存注解使用示例 |

---

## 八、快速参考

### 8.1 常用操作速查

```java
// 基础缓存
RedisUtils.setCacheObject(key, value);                          // 存储
RedisUtils.setCacheObject(key, value, Duration.ofMinutes(30));  // 存储并设置过期时间
Object value = RedisUtils.getCacheObject(key);                  // 获取
RedisUtils.deleteObject(key);                                   // 删除
boolean exists = RedisUtils.isExistsObject(key);                // 检查是否存在

// 分布式锁
RLock lock = RedisUtils.getLock(lockKey);
boolean locked = lock.tryLock(10, 30, TimeUnit.SECONDS);
lock.unlock();

// 限流
long remaining = RedisUtils.rateLimiter(key, RateType.OVERALL, 100, 10);

// 缓存注解
@Cacheable(cacheNames = CacheNames.SYS_DICT, key = "#dictType")  // 查询缓存
@CachePut(cacheNames = CacheNames.SYS_DICT, key = "#bo.dictType") // 更新缓存
@CacheEvict(cacheNames = CacheNames.SYS_DICT, key = "#dictType")  // 清除缓存
```

### 8.2 注意事项

1. ⚠️ **@Cacheable返回值不能使用不可变集合**（List.of()、Set.of()、Map.of()）
2. ⚠️ **分布式锁必须在finally中释放**
3. ⚠️ **keys()和deleteKeys()会忽略租户隔离**，需要手动拼接租户ID
4. ⚠️ **缓存key要有业务含义**，便于排查问题
5. ⚠️ **设置合理的过期时间**，避免缓存雪崩
6. ⚠️ **返回空列表而不是null**，防止缓存穿透

---

## 九、总结

本技能文档涵盖了Redis缓存开发的核心内容：

1. **RedisUtils工具类**：提供基础缓存、集合操作、发布订阅、限流、原子操作等功能
2. **CacheUtils工具类**：提供Spring Cache统一操作接口
3. **缓存注解**：@Cacheable、@CachePut、@CacheEvict的使用规范
4. **分布式锁**：防止重复提交、库存扣减等场景的实现
5. **缓存Key命名规范**：统一的命名格式和CacheNames常量类
6. **常见问题**：缓存穿透、雪崩、击穿的解决方案

在开发过程中，请严格遵守本文档的规范，特别是：
- @Cacheable返回值不能使用不可变集合
- 分布式锁必须在finally中释放
- 缓存key要有业务含义
- 设置合理的过期时间

