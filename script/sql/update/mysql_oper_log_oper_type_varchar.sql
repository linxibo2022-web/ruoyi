-- ============================================================================
-- sys_oper_log.oper_type 定长 CHAR 改 VARCHAR 增量升级脚本 (MySQL)
-- ============================================================================
-- 变更内容：
--   sys_oper_log.oper_type 由 char(3) 改为 varchar(8)
-- 触发原因：
--   oper_type 存的是操作类型字典码('1'..'99')，char(3) 为定长类型，值不足时用空格补齐。
--   MySQL 读取 CHAR 时会自动去掉尾随空格，本身无感；但 PostgreSQL / Oracle / SQL Server
--   读回时保留空格('1  ')，导致：字典标签匹配失败（操作类型列空白）、按类型筛选查不到、
--   Excel 导出操作类型为空。统一改为变长类型，四库行为一致。
-- 引入版本：2026-06 同步批次
-- 升级前请备份 sys_oper_log 表！
-- ============================================================================

ALTER TABLE sys_oper_log
    MODIFY COLUMN oper_type varchar(8) DEFAULT '' COMMENT '操作类型';

-- 规范历史数据（MySQL CHAR 读取/转换已自动 trim，此处为保险，通常为空操作）
UPDATE sys_oper_log SET oper_type = TRIM(oper_type) WHERE oper_type <> TRIM(oper_type);

-- ============================================================================
-- 回滚 SQL（如升级后需回退）
-- ============================================================================
-- ALTER TABLE sys_oper_log MODIFY COLUMN oper_type char(3) DEFAULT '' COMMENT '操作类型';
