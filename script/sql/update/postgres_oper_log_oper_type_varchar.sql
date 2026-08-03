-- ============================================================================
-- sys_oper_log.oper_type 定长 CHAR 改 VARCHAR 增量升级脚本 (PostgreSQL)
-- ============================================================================
-- 变更内容：
--   sys_oper_log.oper_type 由 char(3) 改为 varchar(8)
-- 触发原因：
--   oper_type 存的是操作类型字典码('1'..'99')，char(3) 为定长类型，PostgreSQL 会用空格
--   补齐并在读回时保留('1  ')，导致：字典标签匹配失败（操作类型列空白）、按类型筛选查不到、
--   Excel 导出操作类型为空。改为变长 varchar 即不再填充空格。
-- 引入版本：2026-06 同步批次
-- 升级前请备份 sys_oper_log 表！
-- 说明：oper_type 上的 idx_sys_oper_log_bt 索引，PostgreSQL 在 ALTER TYPE 时会自动重建，无需手动处理。
-- ============================================================================

ALTER TABLE sys_oper_log ALTER COLUMN oper_type TYPE varchar(8);

-- char -> varchar 类型转换会自动去掉尾随空格；以下再显式规范一遍历史数据（保险，varchar 比较空格敏感）
UPDATE sys_oper_log SET oper_type = TRIM(oper_type) WHERE oper_type <> TRIM(oper_type);

-- ============================================================================
-- 回滚 SQL（如升级后需回退）
-- ============================================================================
-- ALTER TABLE sys_oper_log ALTER COLUMN oper_type TYPE char(3);
