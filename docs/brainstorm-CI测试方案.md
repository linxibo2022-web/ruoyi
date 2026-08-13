# 头脑风暴：CI 测试方案

**创建时间**: 2026-08-13 01:29
**最后更新**: 2026-08-13 01:29
**状态**: P0 已实施，P1-P3 待定

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

## 风险与注意事项

| 风险 | 应对策略 |
|------|---------|
| 两平台配置再次漂移 | 文件头注释"改一处同步另一处"，本次已发生验证码遗漏 |
| Redisson 类名随库升级变化 | application-test.yml 为字符串配置，升级后跑一次 CI 即发现 |
| JAR 名/端口随源码变更 | ci.yml env 块注释已指向 pom.xml / application.yml |
| GitHub 强制 Node 24 | 观察 deprecation 警告，届时统一升 actions 版本 |

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
