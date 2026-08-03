-- ============================================================================
-- sys_oper_log.oper_type 定长 CHAR 改 VARCHAR2 增量升级脚本 (Oracle)
-- ============================================================================
-- 变更内容：
--   sys_oper_log.oper_type 由 char(3) 改为 varchar2(8)
-- 触发原因：
--   oper_type 存的是操作类型字典码('1'..'99')，char(3) 为定长类型，Oracle 会用空格补齐
--   并在读回时保留('1  ')，导致：字典标签匹配失败（操作类型列空白）、按类型筛选查不到、
--   Excel 导出操作类型为空。改为变长 varchar2 即不再填充空格。
-- 引入版本：2026-06 同步批次
-- 升级前请备份 sys_oper_log 表！
-- ============================================================================

ALTER TABLE sys_oper_log MODIFY (oper_type varchar2(8));

-- Oracle 把 CHAR 转 VARCHAR2 不会自动去空格，必须显式清理历史数据（VARCHAR2 比较空格敏感，WHERE 可命中）
UPDATE sys_oper_log SET oper_type = TRIM(oper_type) WHERE oper_type <> TRIM(oper_type);
COMMIT;

-- ============================================================================
-- 回滚 SQL（如升级后需回退）
-- ============================================================================
-- ALTER TABLE sys_oper_log MODIFY (oper_type char(3));
