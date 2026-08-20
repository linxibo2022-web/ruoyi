
# /add-test — 为已有模块补充测试

## 用途

为已存在的业务模块自动生成测试文件。支持三种场景：

1. **全量补充**：模块没有任何测试，一次性生成 ServiceTest + ControllerTest + E2E + Vitest（按需）
2. **增量补充**：模块已有部分测试，补充缺失的（如用户后来写了 composable，现在补 Vitest）
3. **单类型补充**：只补某一种测试（如"只加 E2E 配置"）

## 调用方式

```
/add-test [模块名]              # 自动检测并补充全部缺失测试
/add-test [模块名] --vitest     # 仅检测并补充 Vitest
/add-test [模块名] --e2e        # 仅追加 E2E 配置
/add-test [模块名] --backend    # 仅补充后端测试
```

## 智能检测规则

| 条件 | 检测方式 | 生成文件 |
|:---:|------|------|
| V1 | `composables/use{Module}.ts` 存在 | `use{Module}.test.ts` |
| V2 | `utils/{module}.ts` 存在且有 export | `{module}.test.ts` |
| V3 | Store 有非通用 action | `{module}Store.test.ts` |
| V4 | ServiceImpl 方法数 > 6 | ServiceTest 追加自定义逻辑段 |
| V5 | `.vue` computed ≥ 3 | 提示提取 composable |

标准 CRUD 模块（V1~V4 均不满足）→ 跳过 Vitest。

## 防护规则

1. **@Generated 标记**：所有自动生成的测试文件第一行含标记注释，避免重复覆盖
2. **E2E 去重**：`apiAdd` 字段唯一性校验
3. **不自动覆盖**：文件存在但无 @Generated 标记 → 提示用户确认
4. **不自动 git add**：生成后提示跑通后手动提交
