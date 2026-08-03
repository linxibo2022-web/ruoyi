# 增量升级脚本（过渡性）

本目录存放**已部署实例**升级到新版本时需要执行的 `ALTER TABLE` / `CREATE INDEX` 等增量 SQL。
全新部署直接用 `script/sql/` 下的完整建表脚本即可，**无需执行本目录文件**。

## 当前增量脚本

| 文件 | 触发原因 | 影响表 | 引入版本 |
|------|---------|--------|---------|
| `mysql_snailjob_1.10.0.sql` | snailjob 1.9.0 → 1.10.0 | `sj_job` / `sj_workflow` | 2026-05 同步批次 |
| `oracle_snailjob_1.10.0.sql` | 同上 | 同上 | 同上 |
| `postgres_snailjob_1.10.0.sql` | 同上 | 同上 | 同上 |
| `sqlserver_snailjob_1.10.0.sql` | 同上 | 同上 | 同上 |
| `mysql_payment_public_key_id.sql` | 微信支付公钥模式新增公钥ID字段 | `b_payment` | 2026-05 同步批次 |
| `oracle_payment_public_key_id.sql` | 同上 | 同上 | 同上 |
| `postgres_payment_public_key_id.sql` | 同上 | 同上 | 同上 |
| `sqlserver_payment_public_key_id.sql` | 同上 | 同上 | 同上 |
| `mysql_oper_log_oper_type_varchar.sql` | oper_type 定长 CHAR 改 VARCHAR（修复 PG/Oracle/SQLServer 空格填充导致字典标签/筛选/导出失效） | `sys_oper_log` | 2026-06 同步批次 |
| `oracle_oper_log_oper_type_varchar.sql` | 同上 | 同上 | 同上 |
| `postgres_oper_log_oper_type_varchar.sql` | 同上 | 同上 | 同上 |
| `sqlserver_oper_log_oper_type_varchar.sql` | 同上 | 同上 | 同上 |

## 使用方法

1. **生产升级前**：连接对应数据库，执行匹配数据库类型的 `.sql` 文件
2. **执行后立刻重启**应用，避免新旧 schema 混跑触发约束冲突
3. 升级完毕后无需保留本文件，可在**下一个发布版本一并清理**整个 update/ 目录

## 维护约定

- **过渡性目录**：本目录文件只服务「上一版本 → 当前版本」的迁移，下个发布版本应整体清理
- **不要往上游 master 完整脚本里反向回灌 ALTER**：完整脚本永远是最新结构的 `CREATE TABLE`
- 文件命名：`{数据库类型}_{触发原因}_{版本号}.sql`

## 回滚

若升级后出现问题需要回滚，参考各 `.sql` 文件末尾的「回滚 SQL」段落。
