
# 代码规范速查

## 🚫 全栈禁令速查表

> **快速查表**：一眼定位所有禁止写法

| 端 | ❌ 禁止写法 | ✅ 正确写法 | 原因 |
|----|-----------|-----------|------|
| 后端 | `com.ruoyi.*` | `plus.ruoyi.*` | 包名规范 |
| 后端 | `plus.ruoyi.xxx.Xxx` (内联全限定名) | `import` + 短类名 | 代码整洁 |
| 后端 | `Map<String, Object>` 返回业务数据 | 创建 VO 类 | 类型安全 |
| 后端 | `extends ServiceImpl<>` | `implements IXxxService` | 四层架构 |
| 后端 | Service 层 `new LambdaQueryWrapper` | DAO 层 `buildQueryWrapper()` | 职责分离 |
| 后端 | Service 中 `PlusLambdaQuery.of()` | `dao.buildQueryWrapper(bo)` | 四层架构 |
| 后端 | Controller 注入 `IXxxDao` | 只注入 `IXxxService` | 分层依赖 |
| 后端 | Service 注入 `XxxMapper` | 只注入 `IXxxDao` | 分层依赖 |
| 后端 | DAO 中 `MapstructUtils.convert()` | Service 层做转换 | 职责分离 |
| 后端 | 跨 Maven 模块注入对方 DAO | 注入对方 `IXxxService` | 模块边界（同 Maven 模块内可直接注入 DAO） |
| 后端 | `like(Long/Date字段, value)` | `likeCast(Long/Date字段, value)` | PostgreSQL 兼容 |
| 后端 | `/page`, `/{id}`, `/add` | `/pageXxxs`, `/getXxx/{id}`, `/addXxx` | 路径全局唯一 |
| 后端 | `BeanUtil.copyProperties()` | `MapstructUtils.convert()` | 项目统一规范 |
| 后端 | `LocalDateTime.now()` | `DateUtils.getNowDate()` 或 `new Date()` | 框架统一用 Date |
| 后端 | `AUTO_INCREMENT` | 雪花ID（不指定type） | 主键策略 |
| 后端 | `R.ok(stringValue)` 返回字符串到data | `R.ok(null, stringValue)` | 方法重载陷阱 |
| 后端 | `extends BaseEntity` | `extends TenantEntity` | 多租户支持 |
| 后端 | `@Cacheable` 返回 `List.of()`/`Set.of()` | `new ArrayList<>(List.of())` | Redis 反序列化失败 |
| 后端 | `request.getReader()` 读请求体 | `request.getInputStream()` + UTF-8 解码 | 容器按 ISO-8859-1 解码导致中文乱码 |
| 后端 | `new InputStreamReader(stream)` (无字符集) | `new InputStreamReader(stream, UTF_8)` | 不传字符集走 JVM 默认编码（平台依赖） |
| SQL | `phonenumber`（原版RuoYi字段） | `phone`（手机号字段） | 本项目字段名不同 |
| SQL | `del_flag`（原版RuoYi字段） | `is_deleted`（逻辑删除字段） | 本项目字段名不同 |
| 字典 | `sys_normal_disable`（原版RuoYi字典） | `sys_enable_status`（启用状态字典） | 本项目字典名不同；DictTypes 枚举 / SQL 仅定义后者 |
| 字典 | `sys_user_sex`（原版RuoYi字典） | `sys_user_gender`（用户性别字典） | 同上；字段名同样建议从 `sex` → `gender` |
| 前端 | `<el-input>`, `<el-dialog>` | `<AFormInput>`, `<AModal>` | 封装组件 |
| 前端 | `try { await api() } catch` | `const [err, data] = await api()` | 错误处理规范 |
| 前端 | `ElMessage.success()` | 项目封装的消息组件 | 统一风格 |
| 移动端 | `from 'wot-design-uni'` | `from '@/wd'` | 导入路径 |
| 移动端 | `<uni-forms>`, `<uni-field>` | `<wd-form>`, `<wd-input>` | WD UI 规范 |
| 移动端 | CSS `// 注释` | CSS `/* 注释 */` | SCSS 语法 |
| 移动端 | `width: 100px` | `width: 200rpx` | 响应式单位 |
| Bash | `> nul` | `> /dev/null 2>&1` | Windows 会创建 nul 文件 |
| 全栈 | `.vue` 文件无首行注释 | `<!-- 描述 -->` 作为第一行 | Show Comments 插件 |
| 全栈 | `.ts` 文件无首行注释（且无 JSDoc） | `// 描述` 作为第一行 | Show Comments 插件 |

---

## 🚫 后端禁令（20 条）

### 1. 包名必须是 `plus.ruoyi.*`

```java
// ✅ 正确
package plus.ruoyi.business.base.service;

// ❌ 错误
package com.ruoyi.business.base.service;
```

### 2. 禁止使用完整类型引用

```java
// ✅ 正确：先 import 再使用
import plus.ruoyi.common.core.domain.R;
public R<XxxVo> getXxx(Long id) { ... }

// ❌ 错误：直接使用完整包名
public plus.ruoyi.common.core.domain.R<XxxVo> getXxx(Long id) { ... }
```

### 3. 禁止使用 Map 封装业务数据

```java
// ✅ 正确：创建 VO 类
public XxxVo getXxx(Long id) {
    return MapstructUtils.convert(entity, XxxVo.class);
}

// ❌ 错误：使用 Map
public Map<String, Object> getXxx(Long id) {
    Map<String, Object> result = new HashMap<>();
    result.put("id", entity.getId());
    return result;
}
```

### 4. Service 禁止继承基类

```java
// ✅ 正确：不继承任何基类
@Service
public class XxxServiceImpl implements IXxxService {
    private final IXxxDao xxxDao;
}

// ❌ 错误：继承 ServiceImpl
public class XxxServiceImpl extends ServiceImpl<XxxMapper, Xxx> {
}
```

### 5. 查询条件禁止在 Service 层构建

```java
// ✅ 正确：在 DAO 层构建
// XxxDaoImpl.java
public PlusLambdaQuery<Xxx> buildQueryWrapper(XxxBo bo) {
    return PlusLambdaQuery.of()
        .eq(Xxx::getStatus, bo.getStatus())
        .like(StringUtils.isNotBlank(bo.getName()), Xxx::getName, bo.getName());
}

// ❌ 错误：在 Service 层构建
// XxxServiceImpl.java
public List<XxxVo> list(XxxBo bo) {
    LambdaQueryWrapper<Xxx> wrapper = new LambdaQueryWrapper<>();  // 禁止！
    wrapper.eq(Xxx::getStatus, bo.getStatus());
}
```

### 6. 非字符串字段模糊搜索必须用 likeCast（查询和更新）

> ⚠️ **此规范同时适用于 PlusLambdaQuery（查询）和 PlusLambdaUpdate（更新）！**

```java
// ✅ 正确：String 用 like，非 String 用 likeCast（查询场景）
lqw.and(w -> w
    .likeCast(Xxx::getId, searchValue)          // Long → likeCast
    .or().like(Xxx::getName, searchValue)       // String → like
    .or().likeCast(Xxx::getCreateTime, searchValue) // Date → likeCast
);

// ✅ 正确：更新场景同样适用 likeCast
xxxDao.lambdaUpdate()
    .set(Xxx::getStatus, "0")
    .likeCast(Xxx::getId, "100")  // 更新 ID 包含 "100" 的记录（PostgreSQL 兼容）
    .update();

// ❌ 错误：对非字符串类型使用 like（PostgreSQL 会报错）
lqw.and(w -> w
    .like(Xxx::getId, searchValue)              // Long 不能用 like！
);

// ❌ 错误：更新场景同样会报错
xxxDao.lambdaUpdate()
    .set(Xxx::getStatus, "0")
    .like(Xxx::getId, "100")  // Long 类型不能用 like！
    .update();
```

### 7. 接口路径必须包含实体名

```java
// ✅ 正确：路径包含实体名，全局唯一
@GetMapping("/pageAds")
@GetMapping("/getAd/{id}")
@PostMapping("/addAd")
@PutMapping("/updateAd")
@DeleteMapping("/deleteAds/{ids}")

// ❌ 错误：通用路径，不唯一
@GetMapping("/page")
@GetMapping("/{id}")
@PostMapping("/add")
```

### 8. Bash 命令禁止使用 `> nul`

```bash
# ✅ 正确
command > /dev/null 2>&1

# ❌ 错误（会创建名为 nul 的文件）
command > nul
```

### 9. 禁止使用 BeanUtil 进行对象转换

```java
// ✅ 正确：使用 MapstructUtils
XxxVo vo = MapstructUtils.convert(entity, XxxVo.class);
List<XxxVo> voList = MapstructUtils.convert(entityList, XxxVo.class);

// ❌ 错误：使用 BeanUtil
BeanUtil.copyProperties(entity, vo);  // 禁止！
BeanUtils.copyProperties(entity, vo); // 禁止！
```

### 10. 禁止使用 AUTO_INCREMENT

```sql
-- ✅ 正确：不指定自增，使用雪花ID（MyBatis-Plus 全局配置）
id BIGINT(20) NOT NULL COMMENT '主键ID'

-- ❌ 错误：使用自增
id BIGINT(20) AUTO_INCREMENT  -- 禁止！
```

### 11. R.ok() 返回 String 类型的陷阱

```java
// 场景：Controller 返回 R<String>，想把字符串放到 data 中

// ❌ 错误：会匹配 R.ok(String msg)，字符串进入 msg 而非 data
return R.ok(token);  // 结果：{code:200, msg:"xxx", data:null}

// ✅ 正确：明确指定 msg 和 data
return R.ok(null, token);           // data 有值，msg 为 null
return R.ok("获取成功", token);     // msg 和 data 都有值
```

### 12. Entity 必须继承 TenantEntity

```java
// ✅ 正确：支持多租户
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("b_xxx")
public class Xxx extends TenantEntity {  // ✅ TenantEntity
    @TableId(value = "id")
    private Long id;
}

// ❌ 错误：不支持多租户
public class Xxx extends BaseEntity {  // 禁止！
}
```

### 13. @Cacheable 禁止返回不可变集合

```java
// ❌ 错误：List.of()/Set.of()/Map.of() 返回不可变集合
// 会导致 Redis 反序列化失败（Jackson DefaultTyping 无法正确处理）
@Cacheable(value = "xxx")
public List<String> listXxx() {
    return List.of("1", "2");  // 禁止！第二次请求会报错
    return Set.of("a", "b");   // 禁止！
    return Map.of("k", "v");   // 禁止！
}

// ✅ 正确：使用可变集合包装
@Cacheable(value = "xxx")
public List<String> listXxx() {
    return new ArrayList<>(List.of("1", "2"));  // ✅
}

@Cacheable(value = "xxx")
public Set<String> setXxx() {
    return new HashSet<>(Set.of("a", "b"));  // ✅
}

@Cacheable(value = "xxx")
public Map<String, String> mapXxx() {
    return new HashMap<>(Map.of("k", "v"));  // ✅
}
```

**原因**：本项目 Redis 使用 `Jackson DefaultTyping.NON_FINAL`，会为非 final 类添加类型信息。`List.of()` 返回的 `ImmutableCollections$List12` 是非 final 类，序列化后反序列化时会将内层元素误判为类型数组，导致 `ClassNotFoundException`。

### 14. 禁止使用 LocalDateTime.now() 获取当前时间

```java
// ✅ 正确：框架统一使用 java.util.Date
Date now = DateUtils.getNowDate();  // 推荐，语义清晰
Date now = new Date();              // 也可以

// ❌ 错误：框架 Entity 基类用的是 Date，类型不匹配
LocalDateTime now = LocalDateTime.now();  // 禁止！
```

**原因**：`BaseEntity`/`TenantEntity` 的 `createTime`/`updateTime` 均为 `java.util.Date` 类型，使用 `LocalDateTime` 会导致类型不一致，需要额外转换，徒增复杂度。

### 15. 禁止 Controller 注入 DAO 或 Mapper

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

### 16. 禁止 Service 注入 Mapper（必须通过 DAO）

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

### 17. 禁止 Service 中直接创建 PlusLambdaQuery

```java
// ❌ 错误：查询条件应该在 DAO.buildQueryWrapper() 中构建
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

### 18. 禁止 DAO 中做对象转换或写事务注解

```java
// ❌ 错误：对象转换和事务是 Service 的职责
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

### 19. 禁止跨 Maven 模块直接注入 DAO

> **「跨模块」= 跨 Maven 模块**（如 ruoyi-business ↔ ruoyi-system ↔ ruoyi-mall）。
> 同一个 Maven 模块内的子包之间可以直接注入 DAO，如 `SysTenantServiceImpl` 注入 `ISysUserDao`、`ISysDeptDao` 等 11 个同模块 DAO 是合规的。

```java
// ❌ 错误：跨 Maven 模块直接注入对方的 DAO（ruoyi-business → ruoyi-system）
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

### 20. 读取请求体禁止裸用 getReader / 无字符集 Reader

```java
// ❌ 错误：裸用 request.getReader() 读请求体
//    Servlet 容器（Undertow/Tomcat/Jetty）在 Content-Type 不带 charset 时，
//    按 HTTP 规范默认用 ISO-8859-1 解码，中文乱码！
private String readBody(HttpServletRequest request) throws IOException {
    try (BufferedReader reader = request.getReader()) {  // 禁止！
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        return sb.toString();
    }
}

// ❌ 错误：InputStreamReader 不传字符集
//    会使用 JVM 默认编码（Windows GBK / Linux UTF-8，平台依赖，不可靠）
public BufferedReader getReader() throws IOException {
    return new BufferedReader(new InputStreamReader(getInputStream()));  // 禁止！
}

// ✅ 正确：直接读字节流，显式 UTF-8 解码
private String readBody(HttpServletRequest request) throws IOException {
    try (InputStream is = request.getInputStream();
         ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
        is.transferTo(bos);
        return bos.toString(StandardCharsets.UTF_8);
    }
}

// ✅ 正确：必须用 Reader 时显式传 UTF-8
public BufferedReader getReader() throws IOException {
    return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
}
```

**Why**：

1. **`request.getReader()` 字符集判定坑**：HTTP 协议规范规定 Content-Type 无 charset 时按 `ISO-8859-1` 解码。微信支付、支付宝、银联回调发送的 `application/xml` / `text/xml` 通常不带 charset，容器会按 ISO-8859-1 解码，导致 XML 中的中文字段乱码 → 签名校验失败 → 回调处理失败 → 订单状态不同步。
2. **`Spring CharacterEncodingFilter` 不能完全救场**：filter 顺序、profile 配置稍有问题就失效（已实际发生 dev 正常 / prod 乱码的案例）。
3. **`request.setCharacterEncoding(UTF8)` 只影响底层 reader**：自定义 wrapper 里 `new InputStreamReader(stream)` 会绕开这个设置，使用 JVM 默认编码。
4. **直接读 `InputStream` 拿到的就是 HTTP 原始字节**：用 `StandardCharsets.UTF_8` 自己解，绕开容器和 JVM 的字符集判定，100% 可控。

**适用范围**：所有读取 HTTP 请求体的场景——支付/三方回调、日志拦截器、错误日志记录、`HttpServletRequestWrapper` 实现等。

---

## 🚫 前端禁令（3 条）

### 1. 禁止使用原生 Element Plus 组件

```vue
<!-- ❌ 禁止：原生组件 -->
<el-input v-model="form.name" />
<el-select v-model="form.status" />
<el-dialog v-model="visible" />
<el-form inline />
<el-switch v-model="form.enabled" />

<!-- ✅ 必须：项目封装的 A* 组件 -->
<AFormInput v-model="form.name" label="名称" prop="name" />
<AFormSelect v-model="form.status" :options="statusOptions" />
<AModal v-model="visible" title="标题" />
<ASearchForm />
<AFormSwitch v-model="form.enabled" />
```

### 2. 禁止使用 try-catch 处理 API 调用

```typescript
// ❌ 禁止：try-catch 格式
try {
  const data = await pageXxxs(params)
  dataList.value = data.records
} catch (error) {
  console.error(error)
}

// ✅ 必须：[err, data] 格式
const [err, data] = await pageXxxs(params)
if (!err) {
  dataList.value = data.records
}
```

### 3. 禁止直接使用 ElMessage

```typescript
// ❌ 禁止：直接调用 ElMessage
import { ElMessage } from 'element-plus'
ElMessage.success('操作成功')

// ✅ 必须：使用项目封装的消息组件或方法
// 参考 ad.vue 中的实现
```

---

## 🚫 移动端禁令（4 条）

### 1. 禁止从 wot-design-uni 直接导入

```typescript
// ❌ 禁止：从原包导入
import { useToast, useMessage } from 'wot-design-uni'

// ✅ 必须：从项目封装路径导入
import { useToast, useMessage } from '@/wd'

const toast = useToast()
toast.success('操作成功')
```

### 2. 禁止使用 uni-ui 组件

```vue
<!-- ❌ 禁止：uni-ui 组件 -->
<uni-forms>
  <uni-field v-model="form.name" />
</uni-forms>
<uni-popup />
<uni-list />

<!-- ✅ 必须：WD UI 组件 -->
<wd-form>
  <wd-input v-model="form.name" label="名称" />
</wd-form>
<wd-popup />
<wd-cell-group />
```

### 3. CSS 禁止使用 // 注释

```scss
/* ✅ 正确：标准 CSS 注释 */
.container {
  width: 100%;
}

// ❌ 错误：会导致编译错误
// .container {
//   width: 100%;
// }
```

### 4. 禁止使用 px 单位

```scss
/* ✅ 正确：使用 rpx 响应式单位 */
.box {
  width: 200rpx;
  padding: 24rpx;
  font-size: 28rpx;
}

/* ❌ 错误：px 不会自适应 */
.box {
  width: 100px;  /* 禁止！ */
  padding: 12px;
  font-size: 14px;
}
```

---

## 📝 命名规范速查

### 后端命名

| 类型 | 规范 | 示例 |
|------|------|------|
| 包名 | 小写，点分隔 | `plus.ruoyi.business.base` |
| 类名 | 大驼峰 | `AdServiceImpl` |
| 方法名 | 小驼峰 | `getById`, `pageAds` |
| 变量名 | 小驼峰 | `adName`, `createTime` |
| 常量 | 全大写下划线 | `MAX_PAGE_SIZE` |
| 表名 | 小写下划线 | `b_ad`, `m_goods` |
| 字段名 | 小写下划线 | `ad_name`, `create_time` |

### 类命名后缀

| 类型 | 后缀 | 示例 |
|------|------|------|
| 实体类 | 无 | `Ad`, `Goods` |
| 业务对象 | Bo | `AdBo`, `GoodsBo` |
| 视图对象 | Vo | `AdVo`, `GoodsVo` |
| 服务接口 | IXxxService | `IAdService` |
| 服务实现 | XxxServiceImpl | `AdServiceImpl` |
| DAO 接口 | IXxxDao | `IAdDao` |
| DAO 实现 | XxxDaoImpl | `AdDaoImpl` |
| 控制器 | XxxController | `AdController` |
| Mapper | XxxMapper | `AdMapper` |

### 方法命名

| 操作 | 命名 | Controller URL |
|------|------|----------------|
| 分页查询 | `page(bo, pageQuery)` | `GET /pageXxxs` |
| 查询单个 | `get(id)` | `GET /getXxx/{id}` |
| 新增 | `add(bo)` | `POST /addXxx` |
| 修改 | `update(bo)` | `PUT /updateXxx` |
| 删除 | `batchDelete(ids)` | `DELETE /deleteXxxs/{ids}` |
| 导出 | `export(bo)` | `POST /exportXxxs` |

### 前端命名

#### Vue 文件命名（⚠️ 禁止使用 index.vue 作为业务页面文件名）

| 文件类型 | 命名规则 | 示例 | 说明 |
|---------|---------|------|------|
| **业务页面** | 小写业务名.vue | `ad.vue`, `goods.vue`, `feedback.vue` | 与目录名一致 |
| **树表页面** | 业务名Tree.vue | `categoryTree.vue` | 树形结构页面 |
| **子表组件** | 大驼峰Child.vue | `OrderItemChild.vue`, `DictDataChild.vue` | 主子表的子表 |
| **辅助组件** | 大驼峰.vue | `SkuSpecEditor.vue`, `AssignUsers.vue` | 同目录下的辅助组件 |
| **公共组件** | A前缀大驼峰.vue | `AFormInput.vue`, `AModal.vue` | 全局公共组件 |

> **为什么禁止 `index.vue`**：整个 `plus-ui/src/views/` 中无业务页面使用 `index.vue`。使用具体业务名（如 `ad.vue`）可以在编辑器标签页中一眼区分文件，避免打开多个 `index.vue` 无法辨识。

#### TypeScript 文件命名

| 类型 | 规范 | 示例 |
|------|------|------|
| API 文件 | 小驼峰 + Api | `userApi.ts` |
| 类型文件 | 小驼峰 + Types | `userTypes.ts` |
| Store 文件 | 小驼峰 | `user.ts` |

---

## ✅ 避免过度工程

### 不要做的事

1. **不要创建不必要的抽象**
   - 只有一处使用的代码不需要抽取
   - 三处以上相同代码才考虑抽取

2. **不要添加不需要的功能**
   - 只实现当前需求
   - 不要"以防万一"添加功能

3. **不要过早优化**
   - 优先使用简单直接的方案
   - 复杂方案需要有明确理由

4. **不要添加无用注释**
   - 不要给显而易见的代码加注释
   - 只在逻辑复杂处添加注释

5. **不要保留废弃代码**
   - 删除不用的代码，不要注释保留
   - Git 有历史记录

---

## 📦 Git 提交规范

### 格式

```
<type>(<scope>): <description>
```

### 类型

| type | 说明 |
|------|------|
| `feat` | 新功能 |
| `fix` | 修复 Bug |
| `docs` | 文档更新 |
| `style` | 代码格式（不影响逻辑） |
| `refactor` | 重构（不是新功能或修复） |
| `perf` | 性能优化 |
| `test` | 测试 |
| `chore` | 构建/工具 |

### 示例

```bash
feat(business): 新增用户反馈功能
fix(mall): 修复订单状态显示错误
docs(readme): 更新安装说明
refactor(common): 重构分页查询工具类
perf(order): 优化订单列表查询性能
```

---

## 🔗 相关 Skill

| 需要了解 | 激活 Skill |
|---------|-----------|
| 后端 CRUD 开发规范 | `crud-development` |
| PC 端组件使用 | `ui-pc` |
| 移动端组件使用 | `ui-mobile` |
| PC 端状态管理 | `store-pc` |
| 移动端状态管理 | `store-mobile` |
| API 开发规范 | `api-development` |
| 数据库设计规范 | `database-ops` |
