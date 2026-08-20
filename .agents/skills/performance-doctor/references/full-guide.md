
# 性能优化指南

## 目录

- [性能问题诊断流程](#性能问题诊断流程)
- [后端性能优化](#后端性能优化)
  - [PlusLambdaQuery/Update 优化](#1-pluslambdaqueryupdate-优化)
  - [SQL 优化](#2-sql-优化)
  - [缓存优化（RedisUtils）](#3-缓存优化redisutils)
  - [多租户优化](#4-多租户优化)
  - [批量操作优化](#5-批量操作优化)
  - [接口优化](#6-接口优化)
- [前端性能优化](#前端性能优化)
- [移动端性能优化](#移动端性能优化)
  - [wd-paging 分页组件优化](#1-wd-paging-分页组件优化)
  - [列表优化](#2-列表优化)
  - [图片优化](#3-图片优化)
  - [页面优化](#4-页面优化)
  - [分包加载](#5-分包加载)
- [性能指标](#性能指标)
- [性能监控工具](#性能监控工具)
- [常见性能问题速查](#常见性能问题速查)

---

## 性能问题诊断流程

```
1. 定位问题
   ├─ 前端问题？→ 检查网络、渲染、内存
   ├─ 后端问题？→ 检查接口、SQL、缓存
   └─ 数据库问题？→ 检查索引、慢查询

2. 分析原因
   ├─ 使用工具测量（Arthas/DevTools/Druid）
   └─ 找出瓶颈点

3. 实施优化
   └─ 针对性优化

4. 验证效果
   └─ 对比优化前后
```

---

## 后端性能优化

### 1. PlusLambdaQuery/Update 优化

本项目封装了 `PlusLambdaQuery`（查询）和 `PlusLambdaUpdate`（更新），位于 `ruoyi-common-mybatis` 模块，提供智能条件处理。

#### like vs likeCast 性能差异

| 方法 | 适用类型 | 数据库行为 | 性能影响 |
|------|---------|-----------|---------|
| `like()` | String | 直接 LIKE | ✅ 可用索引前缀匹配 |
| `likeCast()` | Long/Date | CAST + LIKE | ⚠️ 无法使用索引 |

> ⚠️ **此规范同时适用于 PlusLambdaQuery（查询）和 PlusLambdaUpdate（更新）！**

```java
// ❌ 错误：对 String 字段使用 likeCast（多余的类型转换）
lqw.likeCast(Xxx::getName, searchValue);

// ✅ 正确：String 用 like，非 String 用 likeCast（查询场景）
String searchValue = bo.getSearchValue();
if (StringUtils.isNotBlank(searchValue)) {
    lqw.and(w -> w
        .like(Xxx::getName, searchValue)           // String → like
        .or().likeCast(Xxx::getId, searchValue)    // Long → likeCast
        .or().likeCast(Xxx::getCreateTime, searchValue)  // Date → likeCast
    );
}

// ✅ 正确：更新场景同样适用
xxxDao.lambdaUpdate()
    .set(Xxx::getStatus, "0")
    .likeCast(Xxx::getId, "100")  // Long → likeCast（PostgreSQL 兼容）
    .update();
```

#### 智能条件的性能优势

PlusLambdaQuery 自动忽略无效值，减少不必要的条件判断：

```java
// ❌ 传统写法（每个条件都要判断，代码冗长）
LambdaQueryWrapper<Xxx> wrapper = new LambdaQueryWrapper<>();
if (bo.getId() != null) {
    wrapper.eq(Xxx::getId, bo.getId());
}
if (StringUtils.isNotBlank(bo.getName())) {
    wrapper.like(Xxx::getName, bo.getName());
}
if (StringUtils.isNotBlank(bo.getStatus())) {
    wrapper.eq(Xxx::getStatus, bo.getStatus());
}

// ✅ PlusLambdaQuery（自动处理 null 和空字符串）
PlusLambdaQuery<Xxx> lqw = PlusLambdaQuery.of();
lqw.eq(Xxx::getId, bo.getId())           // null 自动跳过
   .like(Xxx::getName, bo.getName())     // 空字符串自动跳过
   .eq(Xxx::getStatus, bo.getStatus());  // null/空 自动跳过
```

#### BETWEEN 智能降级

```java
// PlusLambdaQuery 的 between 方法会智能处理边界值
lqw.between(Xxx::getCreateTime, beginTime, endTime);
// 当 beginTime 有效、endTime 为 null → 自动转为 >= beginTime
// 当 beginTime 为 null、endTime 有效 → 自动转为 <= endTime
// 当两个都为 null → 不添加任何条件
```

#### 聚合函数支持

```java
// 统计查询
PlusLambdaQuery<Order> lqw = PlusLambdaQuery.of(Order.class);
lqw.select(Order::getUserId)
   .sum(Order::getAmount, Order::getTotalAmount)  // sum(amount) as total_amount
   .count(Order::getId, Order::getOrderCount)     // count(id) as order_count
   .groupBy(Order::getUserId);
```

#### PlusLambdaUpdate 性能优化

| 方法 | 说明 | 性能影响 |
|------|------|---------|
| `set(column, value)` | 设置字段值（不忽略 null） | 每次都生成 SET 子句 |
| `setIfNotNull(column, value)` | 设置字段值（忽略 null/空字符串） | 只在有值时生成 SET |
| `setIncrBy(column, value)` | 字段自增 | 避免先查后改 |
| `setDecrBy(column, value)` | 字段自减 | 避免先查后改 |

```java
// ✅ 推荐：使用 setIfNotNull 减少不必要的更新
xxxDao.lambdaUpdate()
    .setIfNotNull(User::getName, bo.getName())       // name 为 null 时跳过
    .setIfNotNull(User::getPhone, bo.getPhone())     // phone 为 null 时跳过
    .set(User::getUpdateTime, new Date())            // 更新时间始终更新
    .eq(User::getId, bo.getId())
    .update();

// ✅ 推荐：使用 setIncrBy/setDecrBy 避免先查后改（减少数据库访问）
// 比如库存扣减
xxxDao.lambdaUpdate()
    .setDecrBy(Goods::getStock, quantity)   // stock = stock - quantity
    .eq(Goods::getId, goodsId)
    .gt(Goods::getStock, quantity)          // 库存充足才扣减
    .update();

// ❌ 不推荐：先查后改（两次数据库访问，且有并发问题）
Goods goods = goodsDao.getById(goodsId);
goods.setStock(goods.getStock() - quantity);
goodsDao.updateById(goods);
```

### 2. SQL 优化

#### 慢查询分析

```sql
-- 开启慢查询日志
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 1;  -- 超过1秒记录

-- 查看慢查询配置
SHOW VARIABLES LIKE '%slow_query%';

-- 查看 Druid 监控面板（本项目已集成）
-- 访问：http://localhost:8080/druid
```

#### 执行计划分析

```sql
-- 使用 EXPLAIN 分析
EXPLAIN SELECT * FROM b_order WHERE user_id = 123;

-- 关注字段
-- type: ALL(全表扫描) < index < range < ref < const
-- rows: 扫描行数，越少越好
-- Extra: Using filesort(需优化)、Using temporary(需优化)
```

#### 索引优化

```sql
-- ✅ 好的索引设计
CREATE INDEX idx_user_status ON b_order(user_id, status);

-- 索引使用原则
-- 1. 最左前缀原则
-- 2. 避免在索引列上使用函数
-- 3. 避免 != 和 NOT IN
-- 4. 注意索引选择性
```

#### N+1 查询优化

```java
// ❌ 不好：N+1 查询
for (Order order : orders) {
    User user = userDao.getById(order.getUserId());  // 每次循环都查询
}

// ✅ 好的：批量查询 + Map 映射
List<Long> userIds = orders.stream()
    .map(Order::getUserId)
    .distinct()
    .toList();
Map<Long, User> userMap = userDao.listByIds(userIds).stream()
    .collect(Collectors.toMap(User::getId, Function.identity()));

for (Order order : orders) {
    User user = userMap.get(order.getUserId());  // O(1) 查找
}
```

### 3. 缓存优化（RedisUtils）

本项目封装了 `RedisUtils`，位于 `ruoyi-common-redis` 模块，基于 Redisson 实现。

#### 基础缓存操作

```java
import plus.ruoyi.common.redis.utils.RedisUtils;
import java.time.Duration;

// 设置缓存（带过期时间）
RedisUtils.setCacheObject("user:" + id, userVo, Duration.ofMinutes(30));

// 获取缓存
UserVo cached = RedisUtils.getCacheObject("user:" + id);

// 删除缓存
RedisUtils.deleteObject("user:" + id);

// 检查是否存在
boolean exists = RedisUtils.isExistsObject("user:" + id);

// 设置过期时间
RedisUtils.expire("user:" + id, Duration.ofHours(1));
```

#### Spring Cache 注解（推荐）

```java
// 使用 Spring Cache 注解（更简洁）
@Cacheable(value = "user", key = "#id")
public UserVo get(Long id) {
    return MapstructUtils.convert(userDao.getById(id), UserVo.class);
}

@CacheEvict(value = "user", key = "#bo.id")
public int update(UserBo bo) {
    return userDao.updateById(MapstructUtils.convert(bo, User.class));
}

// 缓存穿透防护（缓存 null 值）
@Cacheable(value = "user", key = "#id", unless = "#result == null")
public UserVo get(Long id) {
    // ...
}
```

> ⚠️ **重要警告**：`@Cacheable` 方法**禁止返回不可变集合**（`List.of()`、`Set.of()`、`Map.of()`），会导致 Redis 反序列化失败！必须使用可变集合：
> ```java
> // ❌ 错误
> return List.of("1", "2");
> // ✅ 正确
> return new ArrayList<>(List.of("1", "2"));
> ```

#### 限流控制

```java
import org.redisson.api.RateType;

// 限流：每分钟最多 100 次请求
long remaining = RedisUtils.rateLimiter(
    "api:rate:" + userId,
    RateType.OVERALL,  // 全局限流
    100,               // 允许的请求数
    60                 // 时间窗口（秒）
);

if (remaining < 0) {
    throw ServiceException.of("请求过于频繁，请稍后再试");
}
```

#### 分布式锁

```java
import org.redisson.api.RLock;
import java.util.concurrent.TimeUnit;

// 获取分布式锁
RLock lock = RedisUtils.getLock("order:create:" + orderId);
try {
    // 尝试获取锁，等待5秒，锁定30秒
    if (lock.tryLock(5, 30, TimeUnit.SECONDS)) {
        try {
            // 业务逻辑
            createOrder(orderId);
        } finally {
            lock.unlock();
        }
    } else {
        throw ServiceException.of("操作正在处理中，请勿重复提交");
    }
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
    throw ServiceException.of("获取锁被中断");
}
```

#### 发布订阅

```java
// 发布消息
RedisUtils.publish("order:created", orderVo);

// 订阅消息
RedisUtils.subscribe("order:created", OrderVo.class, order -> {
    // 处理订单创建事件
    sendNotification(order);
});
```

#### 本地缓存（Caffeine）

```java
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

// 对于热点数据，可以使用本地缓存减少 Redis 访问
private final Cache<Long, UserVo> localCache = Caffeine.newBuilder()
    .maximumSize(1000)                      // 最大缓存数量
    .expireAfterWrite(5, TimeUnit.MINUTES)  // 写入后5分钟过期
    .build();

public UserVo getUserWithLocalCache(Long id) {
    return localCache.get(id, key -> {
        // 本地缓存未命中，从 Redis 或数据库获取
        UserVo cached = RedisUtils.getCacheObject("user:" + id);
        if (cached != null) {
            return cached;
        }
        return userDao.getById(id);
    });
}
```

### 4. 多租户优化

本项目使用 `TenantEntity` 实现多租户，需要注意索引设计。

```sql
-- ✅ 必须为 tenant_id 建立索引
CREATE INDEX idx_tenant_id ON b_xxx(tenant_id);

-- ✅ 复合索引：租户 + 常用查询条件
CREATE INDEX idx_tenant_status ON b_xxx(tenant_id, status);
CREATE INDEX idx_tenant_create_time ON b_xxx(tenant_id, create_time);
CREATE INDEX idx_tenant_user ON b_xxx(tenant_id, user_id);

-- ⚠️ 注意：多租户查询会自动添加 tenant_id 条件
-- 确保复合索引的第一列是 tenant_id
```

### 5. 批量操作优化

```java
// ✅ 推荐：分批插入（每批500条）
public void batchInsert(List<Xxx> list) {
    int batchSize = 500;
    for (int i = 0; i < list.size(); i += batchSize) {
        int end = Math.min(i + batchSize, list.size());
        xxxMapper.insertBatch(list.subList(i, end));
    }
}

// ❌ 避免：一次性插入大量数据
xxxMapper.insertBatch(hugeList);  // 可能超时或内存溢出

// ✅ 批量更新优化
@Transactional(rollbackFor = Exception.class)
public void batchUpdate(List<XxxBo> list) {
    int batchSize = 500;
    for (int i = 0; i < list.size(); i += batchSize) {
        int end = Math.min(i + batchSize, list.size());
        List<Xxx> batch = list.subList(i, end).stream()
            .map(bo -> MapstructUtils.convert(bo, Xxx.class))
            .toList();
        xxxMapper.updateBatchById(batch);
    }
}
```

### 6. 接口优化

```java
// ❌ 不好：返回所有字段
public List<Order> listOrders() {
    return orderDao.list();
}

// ✅ 好的：只返回需要的字段（使用 VO）
public List<OrderSimpleVo> listOrders() {
    return orderDao.list().stream()
        .map(o -> MapstructUtils.convert(o, OrderSimpleVo.class))
        .toList();
}

// ✅ 好的：分页查询（本项目标准写法）
public PageResult<OrderVo> pageOrders(OrderBo bo, PageQuery pageQuery) {
    PlusLambdaQuery<Order> wrapper = orderDao.buildQueryWrapper(bo);
    return orderDao.page(wrapper, pageQuery).convert(OrderVo.class);
}
```

---

## 前端性能优化

### 1. 加载优化

#### 路由懒加载

```typescript
// ✅ 好的：懒加载（本项目已默认配置）
const routes = [
  {
    path: '/order',
    component: () => import('@/views/order/index.vue')
  }
]

// ❌ 不好：直接导入
import OrderPage from '@/views/order/index.vue'
```

#### 组件懒加载

```vue
<script setup lang="ts">
import { defineAsyncComponent } from 'vue'

// 懒加载重型组件
const HeavyChart = defineAsyncComponent(() =>
  import('@/components/HeavyChart.vue')
)
</script>
```

#### 图片优化

```vue
<!-- 懒加载图片 -->
<el-image :src="url" lazy />

<!-- 使用合适的图片格式和尺寸 -->
<img :src="getOptimizedUrl(url)" loading="lazy" />
```

### 2. 渲染优化

#### 虚拟列表

```vue
<!-- 大量数据使用虚拟列表 -->
<el-table-v2
  :columns="columns"
  :data="data"
  :width="800"
  :height="600"
/>
```

#### 避免不必要的响应式

```typescript
// ❌ 不好：大对象全部响应式
const bigData = ref(hugeArray)

// ✅ 好的：使用 shallowRef
const bigData = shallowRef(hugeArray)

// ✅ 好的：冻结不需要响应式的数据
const staticData = Object.freeze(hugeArray)
```

#### 计算属性缓存

```typescript
// ✅ 好的：使用 computed 缓存
const filteredList = computed(() =>
  list.value.filter(item => item.status === 'active')
)

// ❌ 不好：在模板中过滤
// <div v-for="item in list.filter(i => i.status === 'active')" />
```

### 3. 网络优化

#### 请求合并

```typescript
// ❌ 不好：串行请求
await getUserInfo()
await getPermissions()
await getMenus()

// ✅ 好的：并行请求
const [userInfo, permissions, menus] = await Promise.all([
  getUserInfo(),
  getPermissions(),
  getMenus()
])
```

#### 防抖节流

```typescript
import { useDebounceFn, useThrottleFn } from '@vueuse/core'

// 搜索防抖
const debouncedSearch = useDebounceFn((keyword) => {
  fetchData(keyword)
}, 300)

// 滚动节流
const throttledScroll = useThrottleFn(() => {
  handleScroll()
}, 100)
```

---

## 移动端性能优化

### 1. wd-paging 分页组件优化

组件位置：`plus-uniapp/src/wd/components/wd-paging/`

#### 基础用法

```vue
<!-- wd-paging 分页列表基础用法 -->
<template>
  <wd-paging
    :fetch="pageUsers"
    :page-size="20"
    @load="handleLoad"
  >
    <template #item="{ item }">
      <UserCard :user="item" />
    </template>
  </wd-paging>
</template>

<script setup lang="ts">
const pageUsers = async (query: PageQuery) => {
  return await userApi.pageUsers(query)
}
</script>
```

#### 性能优化配置

```vue
<wd-paging
  :fetch="pageUsers"
  :page-size="20"                    <!-- 适当增大减少请求次数 -->
  :disabled-auto-load="true"         <!-- 禁用自动加载，改为手动 -->
  :max-records="100"                 <!-- 限制最大记录数，避免内存问题 -->
  :show-manual-load-button="true"    <!-- 显示手动加载按钮 -->
/>
```

#### Tab + Radio 数据缓存

```typescript
// wd-paging 内部维护 tabDataMap，切换 Tab 时复用已加载数据
// 这避免了重复请求，提升用户体验

// 清空缓存的方法（需要时调用）
const pagingRef = ref()

// 清空所有数据
pagingRef.value.clearAllData()

// 清空指定 Tab 的数据
pagingRef.value.clearTabData(0)

// 清空指定 Tab + Radio 组合的数据
pagingRef.value.clearTabRadioData(0, 'active')
```

### 2. 列表优化

```vue
<!-- 使用虚拟列表（大数据量时） -->
<wd-virtual-list
  :data="list"
  :item-height="80"
  @load="loadMore"
>
  <template #default="{ item }">
    <view class="item">{{ item.name }}</view>
  </template>
</wd-virtual-list>
```

### 3. 图片优化

```vue
<!-- 懒加载 + 占位图 -->
<wd-img
  :src="item.image"
  lazy-load
  placeholder="/static/placeholder.png"
  mode="aspectFill"
  width="200rpx"
  height="200rpx"
/>
```

### 4. 页面优化

```typescript
// 避免在 onShow 做重复请求
onShow(() => {
  // ❌ 每次显示都请求
  // fetchData()

  // ✅ 只在需要时刷新
  if (needRefresh.value) {
    fetchData()
    needRefresh.value = false
  }
})

// 使用页面预加载（pages.json）
{
  "preloadRule": {
    "pages/index/index": {
      "network": "all",
      "packages": ["pages-sub/admin"]
    }
  }
}
```

### 5. 分包加载

本项目使用 `pages-sub` 作为分包目录：

```json
// pages.json
{
  "pages": [
    { "path": "pages/index/index" },
    { "path": "pages/auth/login" }
  ],
  "subPackages": [
    {
      "root": "pages-sub/admin",
      "pages": [
        { "path": "user/user" }
      ]
    }
  ]
}
```

---

## 性能指标

### 后端指标

| 指标 | 良好 | 需优化 | 测量工具 |
|------|------|--------|---------|
| 接口响应时间 | < 200ms | > 500ms | Druid/SkyWalking |
| 数据库查询 | < 50ms | > 200ms | Druid 慢查询日志 |
| 内存使用 | < 70% | > 85% | Arthas/JVM监控 |
| CPU 使用 | < 60% | > 80% | 系统监控 |

### 前端指标

| 指标 | 良好 | 需优化 | 测量工具 |
|------|------|--------|---------|
| 首屏加载 | < 2s | > 4s | Lighthouse |
| FCP | < 1.8s | > 3s | Chrome DevTools |
| LCP | < 2.5s | > 4s | Chrome DevTools |
| TTI | < 3.8s | > 7.3s | Lighthouse |

### 移动端指标

| 指标 | 良好 | 需优化 | 测量工具 |
|------|------|--------|---------|
| 首屏渲染 | < 1.5s | > 3s | 微信开发者工具 |
| 页面切换 | < 300ms | > 500ms | 性能面板 |
| 列表滚动 | 60fps | < 30fps | FPS 监控 |
| 包体积 | < 2MB | > 5MB | 构建分析 |

---

## 性能监控工具

### 后端工具

| 工具 | 用途 | 使用方式 |
|------|------|---------|
| **Druid** | SQL 监控、慢查询 | 访问 `/druid`（本项目已集成） |
| **Arthas** | JVM 诊断、火焰图 | `java -jar arthas-boot.jar` |
| **SkyWalking** | 分布式链路追踪 | 需单独部署 |
| **JProfiler** | 内存分析、CPU分析 | IDE 插件 |

### 前端工具

| 工具 | 用途 | 使用方式 |
|------|------|---------|
| **Vue DevTools** | 组件性能分析 | 浏览器扩展 |
| **Lighthouse** | 综合性能评估 | Chrome DevTools |
| **Performance** | 运行时性能分析 | Chrome DevTools |
| **Network** | 网络请求分析 | Chrome DevTools |

### 移动端工具

| 工具 | 用途 | 使用方式 |
|------|------|---------|
| **微信开发者工具** | Audits 性能分析 | 开发工具内置 |
| **uni-app 性能面板** | 页面渲染分析 | HBuilderX |
| **Vconsole** | 移动端调试 | 已集成（开发环境） |

---

## 性能日志分析（开发环境 - 重点！）

### 日志文件位置

| 环境 | 日志文件 | 说明 |
|------|---------|------|
| **开发环境** | `./logs/console.log` | 每次启动清空，包含所有日志（INFO/WARN/ERROR）和 SQL 日志 |
| 生产环境 | `./logs/sys-*.log` | 分级别按天滚动，保留60天 |

> ⚠️ **AI 应优先分析开发环境日志**（`./logs/console.log`），因为：
> - 只包含当前启动的日志，范围小，易分析
> - 包含完整的 SQL 执行时间和性能数据
> - 启动时自动清空，避免历史日志干扰

### AI 自动读取触发条件（5 种情况必读）

当用户描述以下性能问题时，**AI 必须主动 Read ./logs/console.log**：

| 触发场景 | 关键词 | 日志分析重点 |
|---------|--------|-------------|
| 1. 接口响应慢 | "接口慢"、"响应时间长"、"超时" | SQL 执行时间、慢查询（>200ms）、N+1 查询 |
| 2. SQL 性能问题 | "SQL慢"、"查询慢"、"数据库慢" | p6spy 日志中的 Consume Time、SQL 语句 |
| 3. 内存或 CPU 高 | "内存占用"、"CPU高"、"卡顿" | OutOfMemoryError、GC 日志、线程池满 |
| 4. 频繁报错 | "一直报错"、"很多错误" | ERROR 级别日志、异常堆栈、出现频率 |
| 5. 启动慢 | "启动慢"、"启动时间长" | 应用启动时间、Bean 初始化耗时 |

### 日志格式识别规则

```
日期时间 [请求ID] [线程] 级别 日志记录器 - 消息内容

示例1（普通日志）：
2026-01-08 22:12:10 [req-123] [http-nio-8080-exec-1] INFO  plus.ruoyi.business.base.controller.AdController - 查询广告列表，参数：{status=1}

示例2（SQL 日志 - p6spy）：
2026-01-08 22:12:10 [req-123] [http-nio-8080-exec-1] INFO  p6spy - Consume Time：245 ms 2026-01-08 22:12:10
Execute SQL：SELECT * FROM b_ad WHERE tenant_id = '000000' AND is_deleted = '0' AND status = '1'
```

**关键字段**：
- **Consume Time**：SQL 执行耗时（毫秒）
- **Execute SQL**：实际执行的 SQL 语句
- **ERROR**：错误级别日志
- **WARN**：警告级别日志

### 4 种常见性能分析场景

#### 场景 1：分析慢 SQL（最常用）

```bash
# 找出所有执行时间 > 200ms 的 SQL
grep "Consume Time" ./logs/console.log | grep -E "Consume Time：[2-9][0-9]{2,}|[0-9]{4,}" | head -20
```

**分析要点**：
- 执行时间是否合理（一般 < 200ms）
- 是否有索引缺失（WHERE 条件的字段）
- 是否存在 N+1 查询（循环中重复相同 SQL）
- 是否查询了不必要的字段（SELECT *）

#### 场景 2：统计 SQL 执行次数（发现 N+1）

```bash
# 统计每种 SQL 的执行次数
grep "Execute SQL" ./logs/console.log | sed 's/WHERE.*/WHERE .../' | sort | uniq -c | sort -rn | head -20
```

**N+1 特征**：
- 同一个 SELECT 语句重复出现多次
- 通常在循环中执行
- 例如：`SELECT * FROM sys_user WHERE id = ?` 出现 100 次

#### 场景 3：查找异常和错误

```bash
# 查找所有 ERROR 日志
grep "ERROR" ./logs/console.log | tail -50

# 查找超时错误
grep -i "timeout\|timed out" ./logs/console.log

# 查找内存溢出
grep -i "OutOfMemoryError\|heap space" ./logs/console.log
```

#### 场景 4：分析启动性能

```bash
# 查找启动相关日志
grep -E "Started.*in|Tomcat started on port" ./logs/console.log

# 查找 Bean 初始化耗时
grep "Bean.*took" ./logs/console.log
```

### 性能日志分析最佳实践

1. **先看整体，再看细节**
   - 先 `wc -l` 统计总行数（太多说明日志级别过高）
   - 再 `grep ERROR` 看有无错误（有错误先解决错误）
   - 最后分析 SQL 性能

2. **关注高频操作**
   - 用 `uniq -c` 统计重复次数
   - 重复次数多的 SQL 优先优化

3. **使用时间窗口分析**
   - 用 `grep "22:12:[0-9][0-9]"` 只分析某个时间段
   - 方便定位用户操作对应的日志

4. **对比执行计划**
   - 发现慢 SQL 后，复制到数据库执行 `EXPLAIN`
   - 查看是否使用了索引（type 字段）

### 完整分析示例

**用户问题**："接口调用很慢，查询广告列表需要 3 秒"

**AI 分析流程**：

```bash
# 1. 读取日志文件
Read ./logs/console.log

# 2. 搜索广告相关的 SQL
grep "b_ad" ./logs/console.log | grep "Consume Time"

# 3. 找到慢查询
# 输出：Consume Time：2845 ms ... Execute SQL：SELECT * FROM b_ad WHERE status = '1'

# 4. 分析问题
# - 执行时间 2845ms（远超 200ms 阈值）
# - SELECT * 查询所有字段（可能不必要）
# - WHERE status = '1' 可能缺少索引

# 5. 给出优化建议
# - 添加 status 字段索引
# - 只查询需要的字段
# - 考虑添加缓存
```

**AI 输出**：
```
性能分析结果：

1. 慢查询定位
   - SQL: SELECT * FROM b_ad WHERE status = '1'
   - 执行时间: 2845ms（严重超标，应 < 200ms）
   - 扫描行数: 约 50000 行（推测）

2. 问题原因
   - status 字段缺少索引，导致全表扫描
   - 查询所有字段（SELECT *），增加数据传输量
   - 未使用缓存

3. 优化方案（按优先级）
   ✅ 立即执行：
      - 添加索引: CREATE INDEX idx_status ON b_ad(status);
      - 只查询需要的字段: SELECT id, title, image, url FROM b_ad WHERE ...

   ✅ 短期优化：
      - 添加 Redis 缓存（过期时间 5 分钟）
      - 使用 @Cacheable 注解

   ⏳ 长期优化（可选）：
      - 考虑多租户索引: idx_tenant_status(tenant_id, status)
      - 分页查询而非一次性加载所有数据

4. 预期效果
   - 添加索引后: 2845ms → 50ms（预计提升 98%）
   - 使用缓存后: 首次 50ms，后续 5ms
```

### 日志分析工具速查

| 命令 | 用途 | 示例 |
|------|------|------|
| `wc -l` | 统计行数 | `wc -l console.log` |
| `grep` | 搜索关键词 | `grep "ERROR" console.log` |
| `tail -n 100` | 查看最后 100 行 | `tail -n 100 console.log` |
| `head -n 50` | 查看前 50 行 | `head -n 50 console.log` |
| `grep -A 5 -B 5` | 显示匹配行的前后 5 行 | `grep -A 5 -B 5 "ERROR" console.log` |
| `sort \| uniq -c` | 统计重复次数 | `grep "Execute SQL" console.log \| sort \| uniq -c` |
| `awk '{print $4}'` | 提取第 4 列 | `grep "Consume Time" console.log \| awk '{print $6}'` |

### 维护说明

**日志清理时机**：
- 开发环境：每次启动自动清空 `console.log`
- 生产环境：按天滚动，保留 60 天

**日志级别调整**：
- 开发环境默认 INFO 级别（包含 SQL 日志）
- 如需更详细日志，修改 `logging.level.root=DEBUG`（不推荐，日志量大）
- SQL 日志通过 p6spy 单独控制（`spy.properties`）

---

## 常见性能问题速查

| 问题现象 | 可能原因 | 解决方案 |
|----------|----------|----------|
| 接口响应慢 | SQL 无索引 | 添加合适索引，使用 EXPLAIN 分析 |
| 接口响应慢 | N+1 查询 | 改为批量查询 + Map 映射 |
| 接口响应慢 | 未使用缓存 | 使用 RedisUtils 或 @Cacheable |
| 页面加载慢 | 资源太大 | 懒加载、压缩、CDN |
| 列表卡顿 | 数据量大 | 虚拟列表、分页加载 |
| 内存泄漏 | 未清理监听 | 组件卸载时清理 |
| 首屏白屏 | 同步加载 | 路由懒加载 |
| 频繁请求 | 无防抖 | 添加防抖节流 |
| 多租户查询慢 | tenant_id 无索引 | 添加 tenant_id 复合索引 |
| 批量操作超时 | 一次操作太多数据 | 分批处理，每批 500 条 |
