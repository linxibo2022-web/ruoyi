---
name: test-development
description: |
  测试开发技能，编写单元测试、集成测试、Controller测试。包含 JUnit5、Mockito、AssertJ 完整规范。

  触发场景：
  - 编写单元测试（工具类、Service、Controller、DAO）
  - 创建测试数据
  - Mock 外部依赖
  - 集成测试（Spring 容器、数据库）
  - HTTP 接口测试
  - 参数化测试
  - 测试覆盖率提升

  触发词：测试、单元测试、集成测试、@Test、JUnit5、JUnit、Mockito、Mock、断言、test、测试用例、测试覆盖率、测试数据、@SpringBootTest、@Mock、@MockBean、AssertJ、测试类、测试方法、@ParameterizedTest、参数化测试、@BeforeEach、@AfterEach

  注意：本项目使用 ruoyi-common-test 模块提供统一测试基类。
---
# test-development

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
- 测试分层策略
- 框架对应模块
- 1. 单元测试（BaseUnitTest）
- 2. Spring 测试（BaseSpringTest）
- 3. Service 测试（BaseServiceTest）
- 4. Controller 测试（BaseControllerTest）
- 5. 参数化测试
- 6. 测试数据构造器（TestDataBuilder）
- 开发检查清单
- 常见错误
- 异常测试
- 运行测试
- FAQ
- 自动生成规则（/dev、/crud、/add-test 共用）
