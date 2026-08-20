# /check - 全栈代码规范检查

作为代码规范检查助手，自动检测项目代码是否符合 ruoyi-plus-uniapp 全栈规范。

## 🎯 检查范围

支持三种检查模式：

1. **全量检查**：`/check` - 检查所有代码
2. **模块检查**：`/check mall` 或 `/check plus-ui` - 检查指定模块/目录
3. **文件检查**：`/check XxxServiceImpl.java` - 检查指定文件

---

## 📋 检查清单总览

### 后端检查（Java）

| 检查项 | 级别 | 说明 |
|--------|------|------|
| 包名规范 | 🔴 严重 | 必须是 `plus.ruoyi.*`，禁止 `com.ruoyi.*` |
| 完整类型引用 | 🔴 严重 | 禁止 `plus.ruoyi.xxx.ClassName`，必须 import |
| Service 继承 | 🔴 严重 | 禁止继承 ServiceImpl/IService |
| DAO 层存在 | 🔴 严重 | 必须有 DAO 层和 buildQueryWrapper |
| 查询条件位置 | 🔴 严重 | 禁止在 Service 层构建 LambdaQueryWrapper |
| Service 中 PlusLambdaQuery | 🔴 严重 | 禁止在 Service 中 `PlusLambdaQuery.of()`，必须调用 `dao.buildQueryWrapper()` |
| Controller 注入 DAO/Mapper | 🔴 严重 | Controller 只能注入 `IXxxService`，禁止注入 DAO 或 Mapper |
| Service 注入 Mapper | 🔴 严重 | Service 只能注入 `IXxxDao`，禁止直接注入 Mapper |
| DAO 中对象转换/事务 | 🔴 严重 | DAO 禁止 `MapstructUtils.convert()` 和 `@Transactional`，这是 Service 的职责 |
| 跨模块 DAO 注入 | 🔴 严重 | 跨 Maven 模块禁止注入对方 DAO，必须注入对方 Service |
| LocalDateTime 使用 | 🔴 严重 | 禁止 `LocalDateTime.now()`，使用 `DateUtils.getNowDate()` 或 `new Date()` |
| **接口路径规范** | 🔴 严重 | 路径必须包含实体名，如 `/pageAds` 而非 `/page` |
| **方法命名规范** | 🔴 严重 | Controller 方法必须包含实体名 |
| Entity 基类 | 🟡 警告 | 业务实体必须继承 TenantEntity |
| BO 映射注解 | 🟡 警告 | 必须使用 @AutoMappers |
| 对象转换 | 🟡 警告 | 必须用 MapstructUtils，禁止 BeanUtil |
| Mapper 继承 | 🟢 建议 | 继承 BaseMapper，不是 BaseMapperPlus |
| Map 传递数据 | 🟢 建议 | 禁止用 Map 封装业务数据 |

### 前端 PC 检查（plus-ui）

| 检查项 | 级别 | 说明 |
|--------|------|------|
| **API 文件目录结构** | 🔴 严重 | API 文件必须放在 `api/business/{模块}/{实体}/` 目录下 |
| **Vue 文件命名** | 🔴 严重 | 业务页面禁止使用 `index.vue`，必须以业务名命名（如 `ad.vue`） |
| 组件使用 | 🔴 严重 | 必须用 AForm*/AModal，禁止直接用 el-dialog/el-input |
| 冗余导入 | 🟡 警告 | 禁止手动导入 http/Result/PageResult/ElMessage |
| 字典使用 | 🟡 警告 | 使用 useDict 获取，DictTag 显示 |
| 样式规范 | 🟢 建议 | 优先 UnoCSS，最小化 :deep() |

### 移动端检查（plus-uniapp / plus-app）

| 检查项 | 级别 | 说明 |
|--------|------|------|
| Toast/Message 导入 | 🔴 严重 | 必须从 `@/wd` 导入，禁止从 `wot-design-uni` |
| **uni.showToast 直接调用** | 🔴 严重 | 业务代码禁止直接调用 `uni.showToast`，必须用 `useToast()`（WD 组件库源码 `wd/components/` 除外） |
| 字典方法使用 | 🔴 严重 | useDict 返回数组，useDictStore.getDictLabel 获取标签 |
| Store 使用 | 🟡 警告 | 优先用已有 Composables |
| 单位使用 | 🟢 建议 | 响应式用 rpx，固定尺寸用 px |

---

## 🔍 后端检查详情

### 1. 包名规范 [🔴 严重]

```bash
# 检查错误包名
Grep pattern: "package com\.ruoyi\." path: ruoyi-modules/ruoyi-business/ output_mode: files_with_matches
Grep pattern: "import com\.ruoyi\." path: ruoyi-modules/ruoyi-business/ output_mode: files_with_matches
```

```java
// ❌ 错误
package com.ruoyi.business.base.service;
import com.ruoyi.common.core.domain.R;

// ✅ 正确
package plus.ruoyi.business.base.service;
import plus.ruoyi.common.core.domain.R;
```

### 2. 完整类型引用 [🔴 严重]

```bash
# 检查完整类型引用
Grep pattern: "plus\.ruoyi\.[a-z]+\.[A-Z][a-zA-Z]+\s" path: ruoyi-modules/ruoyi-business/ glob: "*.java" output_mode: content
```

```java
// ❌ 错误
public plus.ruoyi.common.core.domain.R<XxxVo> getXxx(Long id) { ... }

// ✅ 正确
import plus.ruoyi.common.core.domain.R;
public R<XxxVo> getXxx(Long id) { ... }
```

### 3. Service 继承检查 [🔴 严重]

```bash
Grep pattern: "extends ServiceImpl" path: ruoyi-modules/ruoyi-business/ output_mode: files_with_matches
Grep pattern: "extends IServiceImpl" path: ruoyi-modules/ruoyi-business/ output_mode: files_with_matches
Grep pattern: "implements IService<" path: ruoyi-modules/ruoyi-business/ output_mode: files_with_matches
```

```java
// ❌ 错误
public class AdServiceImpl extends ServiceImpl<AdMapper, Ad> implements IAdService

// ✅ 正确
public class AdServiceImpl implements IAdService {
    private final IAdDao adDao;  // 注入 DAO，不注入 Mapper
}
```

### 4. DAO 层检查 [🔴 严重]

```bash
# 检查是否有 DAO 层
Glob pattern: "ruoyi-modules/ruoyi-business/**/dao/*.java"
Glob pattern: "ruoyi-modules/ruoyi-business/**/dao/impl/*.java"

# 检查是否有 buildQueryWrapper 方法
Grep pattern: "buildQueryWrapper" path: ruoyi-modules/ruoyi-business/ glob: "*DaoImpl.java" output_mode: files_with_matches
```

每个业务模块必须有：
- `dao/IXxxDao.java` - DAO 接口
- `dao/impl/XxxDaoImpl.java` - DAO 实现（含 buildQueryWrapper）

### 5. 查询条件位置 [🔴 严重]

```bash
# 检查 Service 层是否错误构建查询条件
Grep pattern: "new LambdaQueryWrapper" path: ruoyi-modules/ruoyi-business/ glob: "*ServiceImpl.java" output_mode: files_with_matches
Grep pattern: "Wrappers\.lambdaQuery" path: ruoyi-modules/ruoyi-business/ glob: "*ServiceImpl.java" output_mode: files_with_matches
```

```java
// ❌ 错误（在 Service 层构建）
public class XxxServiceImpl {
    public List<XxxVo> list(XxxBo bo) {
        LambdaQueryWrapper<Xxx> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Xxx::getStatus, bo.getStatus());
    }
}

// ✅ 正确（在 DAO 层构建）
public class XxxDaoImpl {
    public PlusLambdaQuery<Xxx> buildQueryWrapper(XxxBo bo) {
        return PlusLambdaQuery.of()
            .eq(Xxx::getStatus, bo.getStatus());
    }
}
```

### 6. 接口路径规范 [🔴 严重]

**核心原则：接口路径必须包含实体名，确保全局唯一**

```bash
# 检查是否使用了通用路径（不含实体名）
Grep pattern: '@GetMapping\("/page"\)' path: ruoyi-modules/ruoyi-business/ glob: "*Controller.java" output_mode: files_with_matches
Grep pattern: '@GetMapping\("/list"\)' path: ruoyi-modules/ruoyi-business/ glob: "*Controller.java" output_mode: files_with_matches
Grep pattern: '@GetMapping\("/\{id\}"\)' path: ruoyi-modules/ruoyi-business/ glob: "*Controller.java" output_mode: files_with_matches
Grep pattern: '@PostMapping\("/add"\)' path: ruoyi-modules/ruoyi-business/ glob: "*Controller.java" output_mode: files_with_matches
Grep pattern: '@PutMapping\("/update"\)' path: ruoyi-modules/ruoyi-business/ glob: "*Controller.java" output_mode: files_with_matches
Grep pattern: '@DeleteMapping\("/\{ids\}"\)' path: ruoyi-modules/ruoyi-business/ glob: "*Controller.java" output_mode: files_with_matches
```

**路径命名规范**：

| 操作 | HTTP方法 | 路径格式 | 示例 |
|------|---------|---------|------|
| 分页查询 | GET | `/page{实体复数}` | `/pageAds` |
| 列表查询 | GET | `/list{实体复数}` | `/listAds` |
| 获取详情 | GET | `/get{实体}/{id}` | `/getAd/{id}` |
| 新增 | POST | `/add{实体}` | `/addAd` |
| 修改 | PUT | `/update{实体}` | `/updateAd` |
| 删除 | DELETE | `/delete{实体复数}/{ids}` | `/deleteAds/{ids}` |
| 导出 | POST | `/export{实体复数}` | `/exportAds` |
| 导入 | POST | `/import{实体复数}` | `/importAds` |
| 选项列表 | GET | `/option{实体复数}` | `/optionPayments` |

```java
// ❌ 错误：通用路径，不唯一
@GetMapping("/page")         // 不知道查什么
@GetMapping("/list")         // 不知道查什么
@GetMapping("/{id}")         // 不知道获取什么
@PostMapping("/add")         // 不知道添加什么
@PutMapping("/update")       // 不知道修改什么
@DeleteMapping("/{ids}")     // 不知道删除什么

// ✅ 正确：路径包含实体名，全局唯一
@GetMapping("/pageAds")      // 分页查询广告
@GetMapping("/listAds")      // 列表查询广告
@GetMapping("/getAd/{id}")   // 获取广告详情
@PostMapping("/addAd")       // 新增广告
@PutMapping("/updateAd")     // 修改广告
@DeleteMapping("/deleteAds/{ids}")  // 删除广告
```

### 7. 方法命名规范 [🔴 严重]

**层级方法命名对照**：

| 层级 | 方法命名 | 说明 |
|------|---------|------|
| **Controller** | `page{实体复数}`, `get{实体}`, `add{实体}`, `update{实体}`, `delete{实体复数}` | 必须包含实体名 |
| **Service** | `page`, `get`, `list`, `add`, `update`, `batchDelete`, `batchSave` | 通用命名 |
| **DAO** | `buildQueryWrapper`, `exists` + 继承 `IBaseDao` 方法 | 查询构建 |

```java
// ❌ 错误：Controller 方法名不含实体名
public R<PageResult<AdVo>> page(AdBo bo, PageQuery pageQuery) { }
public R<AdVo> get(@PathVariable Long id) { }
public R<Long> add(@RequestBody AdBo bo) { }

// ✅ 正确：Controller 方法名包含实体名
public R<PageResult<AdVo>> pageAds(AdBo bo, PageQuery pageQuery) { }
public R<AdVo> getAd(@PathVariable Long id) { }
public R<Long> addAd(@RequestBody AdBo bo) { }

// ✅ Service 层使用通用命名（无需实体名）
public interface IAdService {
    PageResult<AdVo> page(AdBo bo, PageQuery pageQuery);
    AdVo get(Long id);
    Long add(AdBo bo);
    boolean update(AdBo bo);
    boolean batchDelete(Collection<Long> ids);
}
```

### 8. Entity 基类 [🟡 警告]

```bash
# 检查业务实体是否继承 TenantEntity
Grep pattern: "extends TenantEntity" path: ruoyi-modules/ruoyi-business/src/main/java/**/domain/ glob: "*.java" -v "*Bo.java" -v "*Vo.java" output_mode: files_with_matches

# 检查是否错误继承 BaseEntity（主实体不应该继承 BaseEntity）
Grep pattern: "extends BaseEntity" path: ruoyi-modules/ruoyi-business/src/main/java/**/domain/ glob: "*.java" -v "*Bo.java" output_mode: files_with_matches
```

```java
// ❌ 错误（主实体继承 BaseEntity）
public class Ad extends BaseEntity { }

// ✅ 正确（主实体继承 TenantEntity）
public class Ad extends TenantEntity { }

// ✅ 正确（BO 继承 BaseEntity）
public class AdBo extends BaseEntity { }
```

### 9. BO 映射注解 [🟡 警告]

```bash
# 检查 BO 是否使用 @AutoMappers
Grep pattern: "@AutoMappers" path: ruoyi-modules/ruoyi-business/ glob: "*Bo.java" output_mode: files_with_matches
```

```java
// ❌ 错误
public class AdBo extends BaseEntity { }

// ✅ 正确
@AutoMappers({
    @AutoMapper(target = Ad.class, reverseConvertGenerate = false),
    @AutoMapper(target = AdVo.class)
})
public class AdBo extends BaseEntity { }
```

### 10. 对象转换 [🟡 警告]

```bash
# 检查是否使用 BeanUtil
Grep pattern: "BeanUtil\.copy" path: ruoyi-modules/ruoyi-business/ output_mode: files_with_matches
Grep pattern: "BeanUtils\.copy" path: ruoyi-modules/ruoyi-business/ output_mode: files_with_matches
```

```java
// ❌ 错误
BeanUtil.copyProperties(bo, entity);
BeanUtils.copyProperties(source, target);

// ✅ 正确
XxxVo vo = MapstructUtils.convert(entity, XxxVo.class);
List<XxxVo> voList = MapstructUtils.convert(entityList, XxxVo.class);
```

### 11. Mapper 继承 [🟢 建议]

```bash
Grep pattern: "extends BaseMapperPlus" path: ruoyi-modules/ruoyi-business/ output_mode: files_with_matches
```

```java
// ❌ 不推荐
public interface AdMapper extends BaseMapperPlus<Ad> { }

// ✅ 正确
public interface AdMapper extends BaseMapper<Ad> { }
```

### 12. Map 传递数据 [🟢 建议]

```bash
Grep pattern: "Map<String,\s*Object>" path: ruoyi-modules/ruoyi-business/ glob: "*Service*.java" output_mode: files_with_matches
Grep pattern: "HashMap<String,\s*Object>" path: ruoyi-modules/ruoyi-business/ output_mode: files_with_matches
```

```java
// ❌ 错误
public Map<String, Object> getXxx(Long id) {
    Map<String, Object> result = new HashMap<>();
    result.put("id", entity.getId());
    return result;
}

// ✅ 正确（创建 VO 类）
public XxxVo getXxx(Long id) {
    return MapstructUtils.convert(entity, XxxVo.class);
}
```

### 13. Controller 注入 DAO/Mapper [🔴 严重]

**规则**：Controller 只能注入 `IXxxService`，禁止注入 DAO 或 Mapper

```bash
# 检查 Controller 是否注入了 DAO
Grep pattern: "I\w+Dao\s" path: ruoyi-modules/ruoyi-business/ glob: "*Controller.java" output_mode: content

# 检查 Controller 是否注入了 Mapper
Grep pattern: "\w+Mapper\s" path: ruoyi-modules/ruoyi-business/ glob: "*Controller.java" output_mode: content
```

```java
// ❌ 错误：Controller 跳过 Service 层直接访问数据
@RestController
public class OrderController {
    private final IOrderDao orderDao;      // 禁止！
    private final OrderMapper orderMapper; // 禁止！
}

// ✅ 正确：Controller 只注入 Service
@RestController
public class OrderController {
    private final IOrderService orderService;  // ✅
}
```

### 14. Service 注入 Mapper [🔴 严重]

**规则**：Service 只能注入 `IXxxDao` 和其他 `IYyyService`，禁止直接注入 Mapper

```bash
# 检查 Service 是否直接注入了 Mapper
Grep pattern: "\w+Mapper\s" path: ruoyi-modules/ruoyi-business/ glob: "*ServiceImpl.java" output_mode: content
```

```java
// ❌ 错误：Service 绕过 DAO 层直接操作 Mapper
@Service
public class OrderServiceImpl implements IOrderService {
    private final OrderMapper orderMapper;  // 禁止！
}

// ✅ 正确：Service 只注入 DAO 接口
@Service
public class OrderServiceImpl implements IOrderService {
    private final IOrderDao orderDao;  // ✅
}
```

### 15. Service 中 PlusLambdaQuery [🔴 严重]

**规则**：禁止在 Service 中直接创建 `PlusLambdaQuery.of()`，查询条件必须在 DAO 的 `buildQueryWrapper()` 中构建

```bash
# 检查 Service 中是否直接使用 PlusLambdaQuery
Grep pattern: "PlusLambdaQuery" path: ruoyi-modules/ruoyi-business/ glob: "*ServiceImpl.java" output_mode: content
```

```java
// ❌ 错误：Service 中直接构建查询条件
@Service
public class OrderServiceImpl implements IOrderService {
    public List<OrderVo> list(OrderBo bo) {
        PlusLambdaQuery<Order> lqw = PlusLambdaQuery.of();  // 禁止！
        lqw.eq(Order::getStatus, bo.getStatus());
    }
}

// ✅ 正确：调用 DAO 的 buildQueryWrapper
public List<OrderVo> list(OrderBo bo) {
    PlusLambdaQuery<Order> wrapper = orderDao.buildQueryWrapper(bo);  // ✅
    List<Order> list = orderDao.list(wrapper);
    return MapstructUtils.convert(list, OrderVo.class);
}
```

### 16. DAO 中对象转换/事务注解 [🔴 严重]

**规则**：DAO 层禁止使用 `MapstructUtils.convert()` 和 `@Transactional`，这些是 Service 层的职责

```bash
# 检查 DAO 中是否有对象转换
Grep pattern: "MapstructUtils" path: ruoyi-modules/ruoyi-business/ glob: "*DaoImpl.java" output_mode: content

# 检查 DAO 中是否有事务注解
Grep pattern: "@Transactional" path: ruoyi-modules/ruoyi-business/ glob: "*DaoImpl.java" output_mode: content
```

```java
// ❌ 错误：DAO 不应该做对象转换和事务控制
@Repository
public class OrderDaoImpl extends BaseDaoImpl<OrderMapper, Order> {
    @Transactional  // 禁止！DAO 不管事务
    public OrderVo getVo(Long id) {
        return MapstructUtils.convert(getById(id), OrderVo.class);  // 禁止！DAO 不做转换
    }
}

// ✅ 正确：DAO 只返回 Entity，Service 负责转换和事务
// DAO 层
public Order getById(Long id) { ... }  // 返回 Entity

// Service 层
@Transactional(rollbackFor = Exception.class)  // ✅ Service 管事务
public OrderVo get(Long id) {
    Order entity = orderDao.getById(id);
    return MapstructUtils.convert(entity, OrderVo.class);  // ✅ Service 做转换
}
```

### 17. 跨 Maven 模块 DAO 注入 [🔴 严重]

**规则**：跨 Maven 模块（如 ruoyi-business ↔ ruoyi-system ↔ ruoyi-mall）禁止注入对方 DAO，必须注入对方 Service。同一 Maven 模块内可直接注入 DAO。

```bash
# 检查 ruoyi-business 的 Service 中是否注入了 ruoyi-system 的 DAO
Grep pattern: "ISys\w+Dao\s" path: ruoyi-modules/ruoyi-business/ glob: "*ServiceImpl.java" output_mode: content

# 检查 ruoyi-system 的 Service 中是否注入了 ruoyi-business 的 DAO
Grep pattern: "I\w+Dao\s" path: ruoyi-modules/ruoyi-system/ glob: "*ServiceImpl.java" output_mode: content
```

> **注意**：此项需要人工判断是否为跨 Maven 模块。同一 Maven 模块内的子包之间注入 DAO 是合规的（如 `SysTenantServiceImpl` 注入 `ISysUserDao`）。

```java
// ❌ 错误：跨 Maven 模块直接注入对方 DAO（ruoyi-business → ruoyi-system）
@Service
public class OrderServiceImpl implements IOrderService {
    private final IUserDao userDao;  // 禁止！跨 Maven 模块注入 DAO
}

// ✅ 正确：跨 Maven 模块通过 Service 接口访问
@Service
public class OrderServiceImpl implements IOrderService {
    private final IUserService userService;  // ✅ 注入对方 Service 接口
}

// ✅ 正确：同 Maven 模块内可直接注入 DAO（ruoyi-system 内部）
@Service
public class SysTenantServiceImpl implements ISysTenantService {
    private final ISysUserDao userDao;  // ✅ 同属 ruoyi-system，允许
    private final ISysDeptDao deptDao;  // ✅ 同属 ruoyi-system，允许
}
```

### 18. LocalDateTime 使用 [🔴 严重]

**规则**：禁止使用 `LocalDateTime.now()` 获取当前时间，框架统一使用 `java.util.Date`

```bash
# 检查是否使用 LocalDateTime.now()
Grep pattern: "LocalDateTime\.now\(\)" path: ruoyi-modules/ruoyi-business/ output_mode: content
```

```java
// ❌ 错误：框架 Entity 基类用的是 Date，类型不匹配
LocalDateTime now = LocalDateTime.now();  // 禁止！

// ✅ 正确：使用框架统一的 Date 类型
Date now = DateUtils.getNowDate();  // 推荐
Date now = new Date();              // 也可以
```

**原因**：`BaseEntity`/`TenantEntity` 的 `createTime`/`updateTime` 均为 `java.util.Date` 类型，使用 `LocalDateTime` 会导致类型不一致。

---

## 🖥️ 前端 PC 检查详情

### 1. API 文件目录结构 [🔴 严重]

**核心原则：API 文件必须按照后端模块结构组织，禁止随意放置**

```bash
# 检查是否有 API 文件放在 api/business/ 之外的位置
Glob pattern: "plus-ui/src/api/**/*Api.ts"
# 然后检查是否有不在 api/business/ 下的 API 文件
```

**目录结构规范**：

```
plus-ui/src/api/
├── business/                    # 业务 API（必须放这里）
│   └── {模块}/                  # 模块目录（对应后端模块，如 base/license/mall）
│       └── {实体}/              # 实体目录（必须有子目录）
│           ├── {实体}Api.ts     # API 接口
│           └── {实体}Types.ts   # 类型定义
├── system/                      # 系统 API（框架自带）
└── monitor/                     # 监控 API（框架自带）
```

**错误示例**：

```typescript
// ❌ 错误：放在 api/ 根目录下
plus-ui/src/api/xxxApi.ts

// ❌ 错误：放在非 business 的自定义目录下
plus-ui/src/api/public/publicApi.ts

// ❌ 错误：直接放在模块下，没有实体子目录
plus-ui/src/api/business/license/licenseApi.ts

// ✅ 正确：放在 api/business/{模块}/{实体}/ 下
plus-ui/src/api/business/license/product/productApi.ts
```

**公开接口处理方式**：

- **方案1**：在对应实体 API 中添加公开方法（推荐）
- **方案2**：在 `business/{模块}/` 下创建 `public/` 实体目录

### 2. Vue 文件命名规范 [🔴 严重]

**核心原则：业务页面禁止使用 `index.vue`，必须以业务名命名**

```bash
# 检查 views/business/ 下是否有 index.vue（应该用业务名.vue）
Glob pattern: "plus-ui/src/views/business/**/index.vue"
```

```
❌ 错误：views/business/base/ad/index.vue
✅ 正确：views/business/base/ad/ad.vue

❌ 错误：views/business/mall/goods/index.vue
✅ 正确：views/business/mall/goods/goods.vue
```

### 3. 组件使用规范 [🔴 严重]

```bash
# 检查是否直接使用 el-dialog（应该用 AModal）
Grep pattern: "<el-dialog" path: plus-ui/src/views/business/ output_mode: files_with_matches

# 检查是否直接使用 el-input（搜索/表单应该用 AFormInput）
Grep pattern: "<el-input" path: plus-ui/src/views/business/ glob: "*.vue" output_mode: content -C 3
```

```vue
<!-- ❌ 错误 -->
<el-dialog v-model="visible" title="标题">
  <el-form>
    <el-form-item label="名称">
      <el-input v-model="form.name" />
    </el-form-item>
  </el-form>
</el-dialog>

<!-- ✅ 正确 -->
<AModal v-model="dialog.visible" :title="dialog.title" @confirm="submitForm">
  <el-form ref="formRef" :model="form" :rules="rules" label-width="auto">
    <el-row :gutter="10">
      <AFormInput label="名称" v-model="form.name" prop="name" span="auto" />
    </el-row>
  </el-form>
</AModal>
```

**必须使用的组件映射**：

| 场景 | 禁止使用 | 必须使用 |
|------|---------|---------|
| 弹窗 | `el-dialog` | `AModal` |
| 搜索表单 | `el-form + el-input` | `ASearchForm + AFormInput` |
| 表单输入 | `el-input` | `AFormInput` |
| 下拉选择 | `el-select` | `AFormSelect` |
| 日期选择 | `el-date-picker` | `AFormDate` |
| 开关 | `el-switch` | `AFormSwitch` |
| 单选 | `el-radio-group` | `AFormRadio` |
| 图片上传 | `el-upload` | `AFormImgUpload` |
| 详情展示 | 自定义 | `ADetail` |

### 3. 冗余导入检查 [🟡 警告]

```bash
# 检查是否手动导入了自动导入的内容
Grep pattern: "import.*from.*http" path: plus-ui/src/api/business/ glob: "*Api.ts" output_mode: files_with_matches
Grep pattern: "import type \{ Result" path: plus-ui/src/api/business/ glob: "*.ts" output_mode: files_with_matches
Grep pattern: "import type \{ PageResult" path: plus-ui/src/api/business/ glob: "*.ts" output_mode: files_with_matches
Grep pattern: "import type \{ PageQuery" path: plus-ui/src/api/business/ glob: "*.ts" output_mode: files_with_matches
Grep pattern: "import \{ ElMessage" path: plus-ui/src/views/ glob: "*.vue" output_mode: files_with_matches
Grep pattern: "import \{ ref," path: plus-ui/src/views/ glob: "*.vue" output_mode: files_with_matches
```

**已全局自动导入（无需 import）**：
- `http` - HTTP 请求工具
- `Result<T>`, `PageResult<T>`, `PageQuery` - 全局类型
- `ElMessage`, `ElMessageBox`, `ElNotification` - Element Plus
- `ref`, `reactive`, `computed`, `watch` - Vue APIs
- `dayjs` - 日期处理
- `useDict`, `useDictStore`, `useUserStore` - Store

### 4. 字典使用检查 [🟡 警告]

```bash
# 检查字典显示是否使用 DictTag
Grep pattern: "getDictLabel" path: plus-ui/src/views/business/ glob: "*.vue" output_mode: files_with_matches
```

```vue
<!-- ✅ 正确：使用 DictTag 组件显示字典 -->
<DictTag :options="sys_enable_status" :value="row.status" />

<!-- ✅ 正确：表格状态列 + 开关 -->
<el-table-column label="状态" prop="status">
  <template #default="{ row }">
    <AFormSwitch v-model="row.status" @change="handleStatusChange(row)" />
  </template>
</el-table-column>
```

### 5. 样式规范检查 [🟢 建议]

```bash
# 检查是否过度使用 :deep()
Grep pattern: ":deep\(" path: plus-ui/src/views/business/ glob: "*.vue" output_mode: count
```

**样式优先级**：
1. 优先使用 UnoCSS 原子类（`flex`, `gap-2`, `text-sm`）
2. CSS 变量引用主题色（`text-[var(--el-color-primary)]`）
3. 最小化 `<style>` 块
4. `:deep()` 仅用于必须修改第三方组件内部样式

---

## 📱 移动端检查详情

### 1. Toast/Message 导入 [🔴 严重]

```bash
# 检查是否从错误的包导入
Grep pattern: "from 'wot-design-uni'" path: plus-uniapp/src/ glob: "*.vue" output_mode: files_with_matches
Grep pattern: "from 'wot-design-uni'" path: plus-uniapp/src/ glob: "*.ts" output_mode: files_with_matches
Grep pattern: "from 'wot-design-uni'" path: plus-app/ glob: "*.vue" output_mode: files_with_matches
Grep pattern: "from 'wot-design-uni'" path: plus-app/ glob: "*.ts" output_mode: files_with_matches
```

```typescript
// ❌ 错误
import { useToast, useMessage } from 'wot-design-uni'

// ✅ 正确
import { useToast, useMessage } from '@/wd'
```

### 2. uni.showToast 直接调用 [🔴 严重]

**核心原则**：业务代码必须使用项目封装的 `useToast()`，禁止直接调用 `uni.showToast`。原因：`uni.showToast` 绕过统一主题、样式与 i18n，导致提示风格不一致。

```bash
# 扫描两端的所有命中（含 WD 组件库源码）
Grep pattern: "uni\.showToast" path: plus-uniapp/src/ output_mode: content -n
Grep pattern: "uni\.showToast" path: plus-app/ output_mode: content -n
```

**人工筛选**：剔除 `wd/components/**` 路径下的命中（这是 wot-design-uni 第三方组件库源码，由 `/sync-wot` 维护，不属于业务代码）。**剩下的全部都是违规**，必须修复。

```typescript
// ❌ 错误：业务代码直接调用 uni.showToast
uni.showToast({ title: '操作成功', icon: 'success' })
uni.showToast({ title: '微信登录失败', icon: 'none' })

// ✅ 正确：在 setup / composable 顶部实例化，按语义调用
import { useToast } from '@/wd'
const toast = useToast()

toast.success('操作成功')
toast.error('微信登录失败')
toast.info('提示信息')
toast.warning('警告')
toast.loading('加载中...')
```

**特殊情况**：自定义 composable 中需用 toast 时，必须在 composable 函数顶层调用 `const toast = useToast()`，不要在内部回调（如 `catch` 块、`setTimeout`）中重新调用 — 参考 `plus-uniapp/src/composables/useShare.ts` 的 `createShareInstance`。

### 3. 字典方法使用 [🔴 严重]

```bash
# 检查是否错误使用 useDict 的方法
Grep pattern: "useDict\(.*\)\.getDictLabel" path: plus-uniapp/src/ output_mode: files_with_matches
Grep pattern: "const.*getDictLabel.*=.*useDict" path: plus-uniapp/src/ output_mode: files_with_matches
```

```typescript
// ❌ 错误（useDict 没有 getDictLabel 方法）
const { getDictLabel } = useDict(DictTypes.sys_user_gender)

// ✅ 正确：useDict 获取字典数组（用于选择器）
const { sys_user_gender, dictLoading } = useDict(DictTypes.sys_user_gender)

// ✅ 正确：useDictStore 获取字典标签（用于显示）
const dictStore = useDictStore()
const label = dictStore.getDictLabel('sys_user_gender', '0')
```

### 4. Store/Composable 使用 [🟡 警告]

检查是否重复实现已有功能：

**已有 Composables**：
| 功能 | 使用 | 禁止重复实现 |
|------|------|-------------|
| 认证 | `useAuth` | 登录、登出逻辑 |
| Token | `useToken` | Token 存取 |
| 字典 | `useDict` + `useDictStore` | 字典加载和显示 |
| 支付 | `usePayment` | 支付流程 |
| 分享 | `useShare` / `useWxShare` | 分享配置 |
| 订阅消息 | `useSubscribe` | 订阅消息 |
| 上传 | `useUpload` | 文件上传 |
| WebSocket | `useWebSocket` | WS 连接 |
| 国际化 | `useI18n` | 多语言 |
| 主题 | `useTheme` | 主题切换 |

### 5. 单位使用 [🟢 建议]

```bash
# 检查是否混用单位
Grep pattern: ":\s*\d+px" path: plus-uniapp/src/ glob: "*.vue" output_mode: content
```

```scss
// ✅ 正确
.box {
  width: 200rpx;      // 响应式尺寸用 rpx
  padding: 24rpx;
  font-size: 28rpx;
}

.icon {
  width: 44px;        // 固定尺寸用 px
  height: 44px;
}

.footer {
  padding-bottom: env(safe-area-inset-bottom);  // 安全区域
}
```

---

## 🗂️ 字典命名检查（跨端）

### 原版 RuoYi 残留字典名 [🔴 严重]

**核心原则**：本项目数据库与 `DictTypes` 枚举使用 `sys_enable_status`、`sys_user_gender`，**不使用**原版 RuoYi 的 `sys_normal_disable`、`sys_user_sex`。前者会在前端编译报错（DictTypes 无该 key），后端测试与 JavaDoc 也会查不到/示例错误。

```bash
# 全量扫描两个错用字典名
Grep pattern: "sys_normal_disable" path: . output_mode: content -n
Grep pattern: "sys_user_sex" path: . output_mode: content -n
```

**人工筛选**：可以保留的命中只有"对照说明文档"（如本 `/check` 命令、`code-patterns` 禁令表、迁移指南）。其他所有命中都是违规：
- `.vue` / `.ts` 文件中 `DictTypes.sys_normal_disable` / `DictTypes.sys_user_sex` → TS 编译失败
- 测试代码 `setDictType("sys_normal_disable")` → 数据库查不到，断言假成功
- JavaDoc `@DictPattern(dictType = "sys_user_sex")` → 误导用户字段对应

```typescript
// ❌ 错误（原版 RuoYi 残留）
const { sys_normal_disable, sys_user_sex } = useDict(
  DictTypes.sys_normal_disable,
  DictTypes.sys_user_sex
)

// ✅ 正确（本项目字典名）
const { sys_enable_status, sys_user_gender } = useDict(
  DictTypes.sys_enable_status,
  DictTypes.sys_user_gender
)
```

**同时检查字段名**：业务建表/实体如果用了 `sex` 字段，应改为 `gender`，并在 BO/VO 中保持一致。

---

## 📊 输出格式

```markdown
# 🔍 代码规范检查报告

**检查时间**：YYYY-MM-DD HH:mm
**检查范围**：[全量 / 模块名 / 文件名]
**检查模式**：[后端 / 前端PC / 移动端 / 全栈]

---

## 📋 检查结果汇总

| 类别 | 通过 | 警告 | 错误 |
|------|------|------|------|
| 后端 Java | X | X | X |
| 前端 PC | X | X | X |
| 移动端 | X | X | X |

---

## 🔴 严重问题（必须修复）

### 1. [问题类型]

**文件**：`path/to/file.java:42`
**问题**：Service 错误继承 ServiceImpl
**代码**：
\```java
public class AdServiceImpl extends ServiceImpl<AdMapper, Ad>
\```
**修复**：
\```java
public class AdServiceImpl implements IAdService {
    private final IAdDao adDao;
}
\```

---

## 🟡 警告问题（建议修复）

### 1. [问题类型]
...

---

## 🟢 建议优化

### 1. [优化建议]
...

---

## ✅ 检查通过项

- [x] 包名规范
- [x] DAO 层存在
- ...

---

## 📖 相关规范文档

- 后端开发指南：`.claude/skills/crud-development/SKILL.md`
- PC 组件规范：`.claude/skills/ui-pc/SKILL.md`
- 移动端组件规范：`.claude/skills/ui-mobile/SKILL.md`
- 工具类使用：`.claude/skills/utils-toolkit/SKILL.md`
```

---

## 🚨 检查优先级

### 开发完成后必查（阻塞提交）

1. 包名是否是 `plus.ruoyi.*`
2. Service 是否继承了基类
3. 是否有 DAO 层和 buildQueryWrapper
4. 是否在 Service 层构建查询条件（`LambdaQueryWrapper` / `PlusLambdaQuery.of()`）
5. **四层架构分层依赖**：Controller 只注入 Service、Service 只注入 DAO、DAO 不做转换/事务
6. **跨 Maven 模块是否通过 Service 访问**（禁止跨模块注入 DAO）
7. **是否使用了 `LocalDateTime.now()`**（应用 `DateUtils.getNowDate()` 或 `new Date()`）
8. **接口路径是否包含实体名**（如 `/pageAds` 而非 `/page`）
9. **Controller 方法名是否包含实体名**（如 `pageAds()` 而非 `page()`）
10. **前端 API 文件是否放在正确目录**（`api/business/{模块}/{实体}/`）
11. 移动端 Toast/Message 是否从 `@/wd` 导入
12. **移动端是否有 `uni.showToast` 直接调用**（业务代码须用 `useToast()`，排除 `wd/components/` 组件库源码）
13. **是否使用了原版 RuoYi 残留字典名**（`sys_normal_disable` → `sys_enable_status`，`sys_user_sex` → `sys_user_gender`）

### 代码审查建议查

1. 对象转换是否使用 MapstructUtils
2. BO 是否有 @AutoMappers
3. 前端是否使用 AForm* 组件
4. 是否有冗余导入
5. 是否重复实现已有功能

---

## 💡 快速修复指南

### Service 继承错误

```bash
# 查找所有错误继承的 Service
Grep pattern: "extends ServiceImpl" path: ruoyi-modules/ruoyi-business/ output_mode: files_with_matches
```

修复模板：参考 `ruoyi-business/base/service/impl/AdServiceImpl.java`

### 缺少 DAO 层

生成模板：使用 `/crud` 命令自动生成完整的 DAO 层

### 接口路径不规范

```bash
# 查找不规范的接口路径
Grep pattern: '@GetMapping\("/page"\)' path: ruoyi-modules/ruoyi-business/ glob: "*Controller.java"
Grep pattern: '@GetMapping\("/\{id\}"\)' path: ruoyi-modules/ruoyi-business/ glob: "*Controller.java"
```

**修复对照表**：

| 错误路径 | 正确路径 |
|---------|---------|
| `/page` | `/page{实体复数}` 如 `/pageAds` |
| `/list` | `/list{实体复数}` 如 `/listAds` |
| `/{id}` | `/get{实体}/{id}` 如 `/getAd/{id}` |
| `/add` | `/add{实体}` 如 `/addAd` |
| `/update` | `/update{实体}` 如 `/updateAd` |
| `/{ids}` | `/delete{实体复数}/{ids}` 如 `/deleteAds/{ids}` |

### 前端组件替换

| 替换前 | 替换后 |
|--------|--------|
| `<el-dialog>` | `<AModal>` |
| `<el-input>` | `<AFormInput>` |
| `<el-select>` | `<AFormSelect>` |

### 前端 API 文件目录结构修复

```bash
# 查找不规范的 API 文件位置
Glob pattern: "plus-ui/src/api/**/*Api.ts"
# 检查是否有不在 api/business/{模块}/{实体}/ 下的文件
```

**修复方式**：将 API 文件移动到 `api/business/{模块}/{实体}/` 目录下

| 错误位置 | 正确位置 |
|---------|---------|
| `api/xxx/xxxApi.ts` | `api/business/{模块}/{实体}/xxxApi.ts` |
| `api/business/{模块}/xxxApi.ts` | `api/business/{模块}/xxx/xxxApi.ts` |

---

## 📖 参考

- 正确后端代码：`ruoyi-business/base/` 广告模块
- 正确前端代码：`plus-ui/src/views/business/base/ad/ad.vue`
- 正确移动端代码：`plus-uniapp/src/pages/`
- 详细规范：各 Skill 文件
