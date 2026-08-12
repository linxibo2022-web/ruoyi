# CI/CD 配置

本项目同时支持 GitLab CI 和 GitHub Actions，同一套测试策略，两份配置。

## 配置文件

| 文件 | 平台 | 说明 |
|------|------|------|
| `../.gitlab-ci.yml` | GitLab CI | 自部署 GitLab 需安装 runner |
| `../.github/workflows/ci.yml` | GitHub Actions | 推送到 GitHub 即自动生效 |

## 测试门禁

| 触发时机 | 跑什么 |
|---------|--------|
| push 到非 main 分支 | backend-test + vitest（3分钟快速反馈） |
| 创建 PR/MR | 全量三层：backend-test + vitest + playwright（12分钟） |

## 本地测试

```bash
mvn test -pl ruoyi-modules/ruoyi-business -am    # 后端
cd plus-ui && pnpm test:unit                       # 前端单元
cd plus-ui && pnpm test:e2e                        # E2E（需前后端启动）
```
