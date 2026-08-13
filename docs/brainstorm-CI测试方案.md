# 头脑风暴：CI 测试方案

**创建时间**: 2026-08-13 01:29
**最后更新**: 2026-08-13 02:25
**状态**: P0 已实施；S1（测试边界重构，方案 C）已实施并本地验证；P1-P3 待定

---

## 背景与目标

PR #1（CI 简化为仅 PR/MR 触发）引入后，三个检查经历了 4 轮修复：
1. `LangChain4jModuleTest` 因 Redisson 自动配置连不上 Redis → 最终方案：`application-test.yml` + `@ActiveProfiles("test")` 统一排除
2. JAR 名错误（`erp_sys.jar` 而非 `ruoyi-admin.jar`）、后端端口错误（5504 而非 5500）
3. CI 无权限绑 80 端口 → 前端改用 5173 + `PLAYWRIGHT_BASE_URL`
4. 测试文件硬编码 `http://localhost:80` 绕过 baseURL → 改相对路径
5. 验证码默认开启导致登录超时 → 后端启动后 SQL 关闭验证码

本次头脑风暴目标：从稳定性/健壮性、执行速度、覆盖度三个维度检查现有 CI 方案。

## 方案讨论

### 现状盘点（两平台）

- GitHub `ci.yml`：backend-test / vitest / playwright 三 job 并行
- GitLab `.gitlab-ci.yml`：test stage（backend-test + vitest）→ e2e stage 串行
- 差异点：GitLab e2e **缺"关验证码"步骤**（已发生一次配置漂移）
- 共性问题：健康检查循环静默失败（超时后不退出，误导后续排查）

### 问题 1：GitLab 缺关验证码步骤

- 方案 A：补 SQL 步骤（同 GitHub）✅ 选定
- 方案 B：Flyway seed 改默认关验证码 ❌（影响生产安全性）
- 方案 C：E2E 注入 token 绕过登录 ❌（测试真实性下降）

### 问题 2：健康检查静默失败

- 方案 A：循环后加最终校验 `curl ... || exit 1` ✅ 选定（3 行改动，失败立即暴露）
- 方案 B：wait-for-it 脚本 ❌（引外部依赖，过度）

### 问题 3：编译重复（速度）

- 方案 A：现状（backend-test 与 playwright 各自编译依赖链）✅ 选定（总时长可接受）
- 方案 B：artifact 跨 job 传 JAR（省 3-5 min，引入编排复杂度）
- 方案 C：合并单 job（单点失败全挂）

### 问题 4：版本漂移

- `pnpm/action-setup` 用 `version: latest` → 建议锁版本
- Node 20 弃用警告 → 观察，GitHub 强制后统一升 v5 actions
- 两平台配置漂移 → 文件头互相注明"改一处，同步改另一处"

### 问题 5：覆盖度缺口

- 移动端零检查 → 暂缓
- E2E 冒烟仅 1 模块（CRUD_PAGES 已参数化，扩模块=加配置对象）
- 前端无 typecheck script → 需先加 script，暂缓

## 最终结论

**选定方案**: 现有架构保持，P0 两处立即修复

**P0 立即修**:
1. GitLab e2e 补"关验证码"步骤（`apt-get install default-mysql-client` + UPDATE sys_config）
2. 两平台健康检查循环加 fail-fast（`curl ... || exit 1`）

**P1 建议做**:
3. `pnpm/action-setup` 锁版本；两文件头注释互相提醒同步

**P2 观察**:
4. 编译时长若 >15 min 再上 artifact 方案

**P3 按需**:
5. E2E 冒烟扩模块（CRUD_PAGES 加配置对象）

## S1 追加结论（评审后新增）

**问题**: 方案评审发现 CI backend-test 实际覆盖与认知严重偏差（business 0 测试、admin 测试全不跑、无 tag 测试被 groups 过滤）。

**选定方案 C（折中）**:
1. CI 测试范围扩到 `-pl ruoyi-admin -am`，backend-test 补 redis service
2. ServiceTest（test profile，需 MySQL+Redis）纳入 CI，补 `@Tag("dev")`
3. integration 测试（dev profile，需完整环境）打 `@Tag("integration")` + surefire 排除，暂留本地/未来独立 job
4. 若后续 integration CI 化：单独开 job 配 Redis，独立决策

> 详见讨论记录「2026-08-13 S1 实施」。

## 风险与注意事项

| 风险 | 应对策略 |
|------|---------|
| 两平台配置再次漂移 | 文件头注释"改一处同步另一处"，本次已发生验证码遗漏 |
| JAR 名/端口随源码变更 | ci.yml env 块注释已指向 pom.xml / application.yml |
| GitHub 强制 Node 24 | 观察 deprecation 警告，届时统一升 actions 版本 |
| admin ServiceTest 含库数据假设（硬编码 ID 等） | 本地验证全绿不代表 CI 全新库全绿，首次 CI 运行需观察，失败按"测试数据假设问题"修测试而非排除 |
| GitLab service 走 hostname（mysql/redis） | backend-test 已配 `DB_HOST`/`REDIS_HOST` 变量，新增中间件时同步检查 |

## 讨论记录

### 2026-08-13 初始讨论
- 用户关注点：稳定性/健壮性、执行速度、覆盖度
- 扩展方向：先优化现有，不新增检查项
- 检查发现 GitLab e2e 缺关验证码步骤（GitHub 已有），两平台配置漂移
- 健康检查循环静默失败问题两平台共存
- 编译重复问题评估后决定保持现状（复杂度 > 收益）

### 2026-08-13 P0 实施
- GitLab e2e：补 default-mysql-client + 关验证码 SQL（`-h mysql` hostname 访问）+ 后端/前端健康检查 fail-fast + 前端失败日志
- GitHub ci.yml：后端/前端健康检查 fail-fast
- 两平台文件头加"改一处同步另一处"提醒
- P1 漂移提醒已完成；pnpm 锁版本用户决定暂缓（保持 version: latest）
- P2/P3 未实施（编译产物复用、移动端检查等）

### 2026-08-13 方案评审（design-review）
对照实际落地代码逐维度审查，发现三大问题：
- **S1 认知偏差**：CI backend-test 实际只跑依赖链上带 `@Tag("dev")` 的测试——business 模块 0 测试、ruoyi-admin 下 27 个 Service/Integration 测试全不在 `-pl` 范围、无 tag 的 100+ 工具类测试被 surefire `groups=dev` 过滤（僵尸化）
- **S2 E2E 假绿**：TC-03 新增失败 `return` 不抛异常测试照样绿；多处软断言；删除/编辑依赖列表首行（首行是 admin 时必挂）
- **M3/M4 健壮性**：验证码 UPDATE 无行数校验；GitLab 无中间件就绪等待

### 2026-08-13 S1 实施（方案 C：折中——纳入 ServiceTest，integration 暂留本地）
三方案对比：A 全量纳入（integration 从未见 CI 环境，dev profile 需 Redis 等完整环境，风险不可控）→ B 维持现状（业务零回归保障）→ **选 C** ✅

实施内容：
- CI backend-test 命令 `-pl ruoyi-modules/ruoyi-business -am` → `-pl ruoyi-admin -am`（覆盖 admin+全依赖链）
- 两平台 backend-test 补 redis service（GitLab 加 `DB_HOST: mysql` / `REDIS_HOST: redis` 变量）
- `application-test.yml`（common-test）：补测试数据源 + Redis 连接配置；**去掉 Redisson/lock4j exclude**（完整上下文含 CacheController 等 Bean 依赖 Redisson，由 CI redis service 提供）
- surefire `excludedGroups` 加 `integration`（本地 `mvn test` 也不误跑需完整环境的集成测试）
- 10 个 ServiceTest 补 `@Tag("dev")`；19 个 integration 补 `@Tag("integration")`（AuthIntegrationTest 原 dev tag 改 integration）
- E2E 假绿重构（S2）：TC-03/05/07 硬断言；新增用户名用运行唯一值闭环搜索/编辑/删除；TC-02/04 接口 200 硬断言；select 选中首选项满足"角色"必填；验证码开启直接报错
- 试跑暴露并修复 2 个测试 bug：`OrderServiceTest` 同毫秒订单号撞唯一索引（加 nanoTime 后缀）；`SysDeptServiceTest` 状态语义写反（项目规范 1=正常，测试查 `"0"`）

验证：本地全量 `mvn test -pl ruoyi-admin -am` BUILD SUCCESS（admin 11 个 ServiceTest 77 方法全绿，integration 确认未执行）；CI 首次运行待观察
