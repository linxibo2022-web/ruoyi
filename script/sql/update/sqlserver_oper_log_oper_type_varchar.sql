-- ============================================================================
-- sys_oper_log.oper_type 定长 NCHAR 改 NVARCHAR 增量升级脚本 (SQL Server)
-- ============================================================================
-- 变更内容：
--   sys_oper_log.oper_type 由 nchar(3) 改为 nvarchar(8)
-- 触发原因：
--   oper_type 存的是操作类型字典码('1'..'99')，nchar(3) 为定长类型，SQL Server 会用空格补齐
--   并在读回时保留('1  ')，导致：字典标签匹配失败（操作类型列空白）、按类型筛选查不到、
--   Excel 导出操作类型为空。改为变长 nvarchar 即不再填充空格。
-- 引入版本：2026-06 同步批次
-- 升级前请备份 sys_oper_log 表！
-- 说明：oper_type 上存在 idx_sys_oper_log_bt 索引，SQL Server 不允许直接改索引键列的类型，
--       需先删索引、改列、清数据，最后重建索引。
-- ============================================================================

DROP INDEX idx_sys_oper_log_bt ON sys_oper_log
GO

ALTER TABLE sys_oper_log ALTER COLUMN oper_type nvarchar(8) NULL
GO

-- 清理历史填充空格：SQL Server 的 = / <> 比较会忽略尾随空格，故此处不能加 WHERE oper_type <> RTRIM(...)
-- （那样恒为 false，清不掉），直接对全表无条件 RTRIM（已 trim 的行重写为自身，无副作用）。
UPDATE sys_oper_log SET oper_type = RTRIM(oper_type)
GO

CREATE NONCLUSTERED INDEX idx_sys_oper_log_bt ON sys_oper_log (oper_type)
GO

-- ============================================================================
-- 回滚 SQL（如升级后需回退）
-- ============================================================================
-- DROP INDEX idx_sys_oper_log_bt ON sys_oper_log
-- GO
-- ALTER TABLE sys_oper_log ALTER COLUMN oper_type nchar(3) NULL
-- GO
-- CREATE NONCLUSTERED INDEX idx_sys_oper_log_bt ON sys_oper_log (oper_type)
-- GO
