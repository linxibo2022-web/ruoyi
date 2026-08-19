---
name: redis-cache
description: |
  当需要使用Redis缓存、分布式锁、限流等功能时自动使用此Skill。包含RedisUtils工具类、CacheUtils工具类、缓存注解使用规范、分布式锁实现、缓存key命名规范等。

  触发场景：
  - 使用Redis缓存数据
  - 配置Spring Cache缓存注解
  - 实现分布式锁
  - 实现接口限流
  - Redis发布订阅
  - 缓存穿透/雪崩/击穿问题
  - 缓存key设计和命名
  - 缓存过期时间设置
  - 缓存清理和刷新

  触发词：Redis、缓存、Cache、@Cacheable、@CacheEvict、@CachePut、RedisUtils、CacheUtils、分布式锁、RLock、限流、RateLimiter、发布订阅、缓存穿透、缓存雪崩、缓存击穿、缓存key、缓存过期、缓存清理

  核心警告：
  - @Cacheable返回值不能使用不可变集合（List.of()、Set.of()、Map.of()）
  - 分布式锁必须在finally中释放
  - keys()和deleteKeys()会忽略租户隔离
---
# redis-cache

## 执行边界

- 本入口只保留触发说明、最小执行原则与资料索引；开始实质实施前，先识别任务涉及的专题，再按需阅读对应完整资料。
- 项目根规则中的中文、编码、安全、架构及并发保护要求始终优先；不得因资料拆分降低既有约束。
- 不需要完整资料的只读解释、状态查询或简单定位，不得默认加载全文。

## 最小步骤

1. 根据当前任务确定所需专题。
2. 按需读取 `references/full-guide.md` 中相关章节，并遵守其中原有硬约束。
3. 仅在任务范围内实施并执行受影响范围的验证。

## 资料索引

- `references/full-guide.md`：原入口的完整规范、模板、案例和边界。主要专题：
- 一、RedisUtils工具类使用指南
- 二、CacheUtils工具类使用指南
- 三、缓存注解最佳实践
- 四、分布式锁使用
- 五、缓存Key命名规范
- 六、常见问题和解决方案
- 七、核心文件位置
- 八、快速参考
- 九、总结
