
# 测试开发规范

> **核心原则**：本项目提供四层测试基类，根据测试场景选择合适的基类！

## 测试分层策略

| 层次 | 测试类型 | 基类 | 特点 | 执行速度 |
|------|---------|------|------|---------|
| **单元测试** | 工具类/枚举/POJO | `BaseUnitTest` | 不启动 Spring | < 1s |
| **Spring 测试** | 需要依赖注入 | `BaseSpringTest` | 轻量级 Spring（无 Web） | 2-5s |
| **Service 测试** | Service 层业务 | `BaseServiceTest` | Spring + 事务回滚 | 2-5s |
| **Controller 测试** | HTTP 接口 | `BaseControllerTest` | 完整 Web 容器 | 5-10s |

## 框架对应模块

| 模块路径 | 说明 |
|---------|------|
| `ruoyi-common/ruoyi-common-test` | 测试支持模块 |
| `ruoyi-common/ruoyi-common-test/src/main/java/plus/ruoyi/common/test/base/` | 测试基类 |

### 核心依赖

```xml
<!-- 在业务模块的 pom.xml 中添加 -->
<dependency>
    <groupId>plus.ruoyi</groupId>
    <artifactId>ruoyi-common-test</artifactId>
    <scope>test</scope>
</dependency>
```

---

## 1. 单元测试（BaseUnitTest）

**适用场景：** 工具类、枚举类、POJO、算法逻辑（无需 Spring）

```java
package plus.ruoyi.common.core.utils;

import plus.ruoyi.common.test.base.BaseUnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

@DisplayName("StringUtils 工具类测试")
class StringUtilsTest extends BaseUnitTest {

    @Test
    @DisplayName("测试 isBlank - 空字符串判断")
    void testIsBlank() {
        assertThat(StringUtils.isBlank(null)).isTrue();
        assertThat(StringUtils.isBlank("")).isTrue();
        assertThat(StringUtils.isBlank("   ")).isTrue();
        assertThat(StringUtils.isBlank("hello")).isFalse();
    }
}
```

---

## 2. Spring 测试（BaseSpringTest）

**适用场景：** 需要 @Autowired 注入 Bean，但不需要 Web 环境

```java
package plus.ruoyi.common.mqtt;

import plus.ruoyi.common.test.base.BaseSpringTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.assertj.core.api.Assertions.*;

@Slf4j
@DisplayName("MQTT 客户端测试")
class MqttClientUsageTest extends BaseSpringTest {

    @Autowired(required = false)
    private MqttClientTemplate mqttClient;

    @Test
    @DisplayName("测试 MQTT 发布消息")
    void testPublish() {
        if (mqttClient == null) {
            log.warn("MQTT 未启用,跳过测试");
            return;
        }
        boolean success = mqttClient.publish("test/topic", "Hello MQTT");
        assertThat(success).isTrue();
    }
}
```

---

## 3. Service 测试（BaseServiceTest）

**适用场景：** Service 层业务逻辑，自动事务回滚

```java
package plus.ruoyi.business.base.service;

import plus.ruoyi.common.test.base.BaseServiceTest;
import plus.ruoyi.common.test.base.TestDataBuilder;
import plus.ruoyi.business.base.domain.bo.AdBo;
import plus.ruoyi.business.base.domain.vo.AdVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.assertj.core.api.Assertions.*;

@Slf4j
@DisplayName("广告服务测试")
class AdServiceTest extends BaseServiceTest {

    @Autowired
    private IAdService adService;

    @Test
    @DisplayName("测试添加广告")
    void testAddAd() {
        // Arrange - 准备数据
        AdBo adBo = new AdBo();
        adBo.setAdName(TestDataBuilder.randomString(10));
        adBo.setAdType("1");
        adBo.setAdUrl(TestDataBuilder.randomUrl());
        adBo.setStatus("1");

        // Act - 执行操作
        Long adId = adService.add(adBo);

        // Assert - 验证结果
        assertThat(adId).isNotNull();
        assertThat(adId).isGreaterThan(0);
    }

    @Test
    @DisplayName("测试查询广告详情")
    void testGetAd() {
        // 先添加测试数据
        AdBo adBo = new AdBo();
        adBo.setAdName("测试广告");
        adBo.setAdType("1");
        Long adId = adService.add(adBo);

        // 查询详情
        AdVo adVo = adService.get(adId);

        // 断言
        assertThat(adVo).isNotNull();
        assertThat(adVo.getId()).isEqualTo(adId);
        assertThat(adVo.getAdName()).isEqualTo("测试广告");
    }
}
```

**Mock 外部依赖：**

```java
@DisplayName("订单服务测试")
class OrderServiceTest extends BaseServiceTest {

    @Autowired
    private IOrderService orderService;

    @MockBean  // Mock 支付服务
    private IPaymentService paymentService;

    @Test
    @DisplayName("测试订单支付（Mock 支付服务）")
    void testPayOrder() {
        Long orderId = 123L;

        // Mock 行为
        when(paymentService.pay(orderId)).thenReturn(true);

        // 执行
        Boolean success = orderService.payOrder(orderId);

        // 断言
        assertThat(success).isTrue();
        verify(paymentService, times(1)).pay(orderId);
    }
}
```

---

## 4. Controller 测试（BaseControllerTest）

**适用场景：** HTTP 接口测试，完整请求链路

```java
package plus.ruoyi.business.base.controller;

import plus.ruoyi.common.test.base.BaseControllerTest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

@DisplayName("广告控制器测试")
class AdControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("测试分页查询广告")
    void testPageAds() {
        HttpResponse response = doGet("/base/ad/pageAds?pageNum=1&pageSize=10");

        assertThat(response.getStatus()).isEqualTo(200);
        JSONObject json = JSONUtil.parseObj(response.body());
        assertThat(json.getInt("code")).isEqualTo(200);
    }

    @Test
    @DisplayName("测试添加广告")
    void testAddAd() {
        String requestBody = """
            {
                "adName": "测试广告",
                "adType": "1",
                "adUrl": "https://example.com",
                "status": "1"
            }
            """;

        HttpResponse response = doPost("/base/ad/addAd", requestBody);

        assertThat(response.getStatus()).isEqualTo(200);
        JSONObject json = JSONUtil.parseObj(response.body());
        assertThat(json.getInt("code")).isEqualTo(200);
    }
}
```

---

## 5. 参数化测试

```java
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

@DisplayName("参数化测试示例")
class ParameterizedTestExample extends BaseUnitTest {

    @ParameterizedTest
    @DisplayName("测试多个空字符串")
    @ValueSource(strings = {"", "  ", "\t", "\n"})
    void testBlankStrings(String input) {
        assertThat(StringUtils.isBlank(input)).isTrue();
    }

    @ParameterizedTest
    @DisplayName("测试枚举")
    @EnumSource(AdType.class)
    void testEnum(AdType type) {
        assertThat(type.getDescription()).isNotBlank();
    }

    @ParameterizedTest
    @DisplayName("测试 CSV 数据")
    @CsvSource({
        "1, Banner, 横幅广告",
        "2, Popup, 弹窗广告"
    })
    void testCsvData(String code, String name, String desc) {
        assertThat(code).isNotBlank();
        assertThat(name).isNotBlank();
    }

    @ParameterizedTest
    @DisplayName("测试方法提供数据")
    @MethodSource("provideTestData")
    void testMethodSource(String input, String expected) {
        assertThat(StringUtils.trim(input)).isEqualTo(expected);
    }

    static Stream<Arguments> provideTestData() {
        return Stream.of(
            Arguments.of("  hello  ", "hello"),
            Arguments.of("\tworld\n", "world")
        );
    }
}
```

---

## 6. 测试数据构造器（TestDataBuilder）

```java
import plus.ruoyi.common.test.base.TestDataBuilder;

// 生成随机数据
String userName = TestDataBuilder.randomUserName();
String phone = TestDataBuilder.randomPhone();
String email = TestDataBuilder.randomEmail();
String name = TestDataBuilder.randomChineseName();
String url = TestDataBuilder.randomUrl();
String ip = TestDataBuilder.randomIp();
String str = TestDataBuilder.randomString(10);
int num = TestDataBuilder.randomInt(1, 100);
LocalDateTime date = TestDataBuilder.randomDateTime();
Long id = TestDataBuilder.randomId();
String status = TestDataBuilder.randomStatus();
String adType = TestDataBuilder.randomChoice("1", "2", "3");
List<String> list = TestDataBuilder.randomList(5, TestDataBuilder::randomUserName);
```

---

## 开发检查清单

### 测试文件规范

- [ ] **测试类命名**：`{被测试类名}Test`（如 `StringUtilsTest`）
- [ ] **测试类位置**：`src/test/java` 目录下，包路径与源码一致
- [ ] **测试方法命名**：`test{功能}`（如 `testIsBlank`）
- [ ] **使用 @DisplayName**：为测试类和方法添加中文描述

### 基类选择

- [ ] **工具类/枚举/POJO**：继承 `BaseUnitTest`
- [ ] **需要依赖注入（无 Web）**：继承 `BaseSpringTest`
- [ ] **Service 层**：继承 `BaseServiceTest`
- [ ] **HTTP 接口**：继承 `BaseControllerTest`

### 断言规范

- [ ] **使用 AssertJ**：`assertThat()` 而非 `assertEquals()`
- [ ] **异常断言**：`assertThatThrownBy()` 或 `assertThatExceptionOfType()`

### Mock 规范

- [ ] **Spring 测试**：使用 `@MockBean`
- [ ] **单元测试**：使用 `@Mock/@InjectMocks`
- [ ] **验证调用**：使用 `verify()` 验证 Mock 方法被调用

---

## 常见错误

| 错误写法 | 正确写法 | 原因 |
|---------|---------|------|
| `extends SpringBootTest` | `extends BaseServiceTest` | 应使用框架提供的基类 |
| `Assertions.assertEquals(a, b)` | `assertThat(a).isEqualTo(b)` | AssertJ 更强大 |
| `@Test public void test()` | `@Test @DisplayName("xxx") void testXxx()` | 缺少描述和命名规范 |
| 测试类在 `src/main/java` | 测试类在 `src/test/java` | 测试代码不应打包 |
| Mock 后不验证调用 | `verify(mockObj).method()` | Mock 应验证调用 |
| 测试方法相互依赖 | 每个测试方法独立 | 测试应独立可并行 |
| 硬编码测试数据 | 使用 `TestDataBuilder` | 提高可维护性 |

---

## 异常测试

```java
@Test
@DisplayName("测试添加广告 - 名称为空抛出异常")
void testAddAd_ThrowsException() {
    AdBo adBo = new AdBo();
    // 不设置 adName（必填字段）

    assertThatThrownBy(() -> adService.add(adBo))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining("广告名称不能为空");
}
```

---

## 运行测试

```bash
# Maven 运行所有测试
mvn test

# Maven 运行单个测试类
mvn test -Dtest=AdServiceTest

# Maven 运行单个测试方法
mvn test -Dtest=AdServiceTest#testAddAd

# IDEA 中运行
# 右键测试类/方法 → Run 'XXTest'
```

---

## FAQ

### Q1: 什么时候用 BaseUnitTest vs BaseSpringTest？

**A:**
- **BaseUnitTest**: 工具类、枚举、POJO（不需要 Spring）
- **BaseSpringTest**: 需要 `@Autowired` 注入 Bean

### Q2: Service 测试会污染数据库吗？

**A:** 不会。`BaseServiceTest` 使用 `@Transactional`，测试结束后自动回滚。

### Q3: 如何测试私有方法？

**A:**
- 不要直接测试私有方法
- 通过公共方法间接测试
- 如果私有方法太复杂，考虑提取为独立类

### Q4: Mock 和真实测试如何选择？

**A:**
- **单元测试**: 优先 Mock 外部依赖
- **集成测试**: 使用真实依赖
- **平衡**: 既要有单元测试（快），也要有集成测试（准确）

---

## 自动生成规则（/dev、/crud、/add-test 共用）

> 本段供 `/dev`、`/crud`、`/add-test` 命令参照，定义测试自动生成的标准模板和检测规则。

### 生成策略

| 测试层 | 生成方式 | 触发时机 |
|--------|:---:|------|
| 后端 ServiceTest | 必生成，模板替换类名/包名/字段 | /dev step 7.5, /crud step 6, /add-test |
| 后端 ControllerTest | 必生成，模板替换 API 路径 | 同上 |
| Playwright E2E | 必追加一行 CRUD_PAGES 配置 | 同上 |
| 前端 Vitest | 智能检测，按 V1~V5 规则决定 | 同上 |

### 🔴 防护规则

1. **@Generated 标记**：所有自动生成的测试文件第一行必须包含 `// @Generated by {命令名} — 请勿删除此行，手动修改请在标记行之后进行`
2. **去重检查**：生成前检查文件是否存在，含标记则跳过，无标记则提示用户确认
3. **E2E 去重**：用 `apiAdd` 字段在 `CRUD_PAGES` 数组中唯一性校验
4. **不自动 git add**：生成后提示跑通后手动提交

### 多租户处理

ServiceTest 的 `@BeforeEach` 必须显式设置租户 ID：

```java
@BeforeEach
void setUp() {
    TenantHelper.setTenantId("000000");
}
```

### Vitest 智能检测规则（V1~V5）

| 编号 | 条件 | 检测方式 | 生成文件 |
|:---:|------|------|------|
| V1 | 存在自定义 composable | 文件 `src/composables/use{Module}.ts` 存在 | `use{Module}.test.ts` |
| V2 | 存在模块 utils | 文件 `src/utils/{module}.ts` 存在且有 export function | `{module}.test.ts` |
| V3 | Store 有自定义 action | `src/store/modules/{module}.ts` 中 action 名不在通用列表（select/insert/update/delete） | `{module}Store.test.ts` |
| V4 | ServiceImpl 有自定义方法 | 方法数 > 6（标准 CRUD 共 6 个：add/get/update/delete/page/list） | ServiceTest 追加"自定义业务逻辑"段 |
| V5 | Vue 页面计算属性多 | `.vue` 中 `computed` ≥ 3 | 仅提示提取 composable，不自动生成 |

> **重要**：V1~V3 依赖 composables/utils/store 文件已存在。`/dev` 本身不生成这些文件，标准 CRUD 模块生成时 V1~V4 通常全不满足，正常跳过。后续写了 composable 通过 `/add-test --vitest` 增量补测。

### ServiceTest 模板（Java）

参照 `ruoyi-admin/src/test/java/plus/ruoyi/business/base/service/AdServiceTest.java`。

关键要点：
- 继承 `BaseServiceTest`，`@Tag("dev")`，`@DisplayName("{功能名} Service 测试")`
- 5 个测试方法：`testAdd` / `testPage` / `testGet` / `testUpdate` / `testDelete`
- 使用 `TestDataBuilder` 造随机数据，`AssertJ` 断言
- `@BeforeEach` 中 `TenantHelper.setTenantId("000000")`

### ControllerTest 模板（Java）

关键要点：
- 继承 `BaseControllerTest`，`@Tag("dev")`，`@DisplayName("{功能名} Controller 测试")`
- 6 个测试方法覆盖 API 端点：page/list/get/add/update/delete
- 分页测试断言 `code=200` 即可（允许空数据）
- 使用 `doGet/doPost/doPut/doDelete`，Hutool `JSONUtil` 解析响应
- 登录凭据注释注明仅开发环境使用

### E2E CRUD_PAGES 追加格式

```typescript
{
    route: '/{module}/{class}',
    name: '{功能名}',
    apiPage: '/{module}/{class}/page{Class}s',
    apiAdd: '/{module}/{class}/add{Class}',
    searchKeyword: '{搜索关键词}',
    formValue: 'E2E冒烟{功能名}',
    editValue: 'E2E修改{功能名}'
}
```

---
