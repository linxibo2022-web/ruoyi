-- ============================================================================
-- SnailJob 1.9.0 → 1.10.0 增量升级脚本 (SQL Server)
-- ============================================================================
-- 变更内容：
--   1. sj_job        新增 biz_id 字段 + 唯一索引 uk_sj_job_01(namespace_id, biz_id)
--   2. sj_workflow   新增 biz_id 字段 + 唯一索引 uk_sj_workflow_01(namespace_id, biz_id)
-- 注意：SQL Server 添加 NOT NULL 列必须先 NULL → 回填 → ALTER COLUMN NOT NULL
-- 升级前请备份对应表！
-- ============================================================================

-- --------------------------- sj_job ---------------------------
-- 1. 新增字段（先允许 NULL）
ALTER TABLE sj_job ADD biz_id NVARCHAR(64) NULL;
GO

-- 2. 历史数据回填
UPDATE sj_job SET biz_id = CAST(id AS NVARCHAR(64)) WHERE biz_id IS NULL;
GO

-- 3. 改为 NOT NULL
ALTER TABLE sj_job ALTER COLUMN biz_id NVARCHAR(64) NOT NULL;
GO

-- 4. 添加扩展属性（列注释）
EXEC sp_addextendedproperty
     'MS_Description', N'业务ID',
     'SCHEMA', N'dbo',
     'TABLE', N'sj_job',
     'COLUMN', N'biz_id';
GO

-- 5. 新增唯一索引
CREATE UNIQUE INDEX uk_sj_job_01 ON sj_job (namespace_id, biz_id);
GO


-- --------------------------- sj_workflow ---------------------------
-- 1. 新增字段（先允许 NULL）
ALTER TABLE sj_workflow ADD biz_id NVARCHAR(64) NULL;
GO

-- 2. 历史数据回填
UPDATE sj_workflow SET biz_id = CAST(id AS NVARCHAR(64)) WHERE biz_id IS NULL;
GO

-- 3. 改为 NOT NULL
ALTER TABLE sj_workflow ALTER COLUMN biz_id NVARCHAR(64) NOT NULL;
GO

-- 4. 添加扩展属性（列注释）
EXEC sp_addextendedproperty
     'MS_Description', N'业务ID',
     'SCHEMA', N'dbo',
     'TABLE', N'sj_workflow',
     'COLUMN', N'biz_id';
GO

-- 5. 新增唯一索引
CREATE UNIQUE INDEX uk_sj_workflow_01 ON sj_workflow (namespace_id, biz_id);
GO


-- ============================================================================
-- 回滚 SQL（如升级后需回滚到 1.9.0）
-- ============================================================================
-- DROP INDEX uk_sj_job_01 ON sj_job;
-- ALTER TABLE sj_job DROP COLUMN biz_id;
-- DROP INDEX uk_sj_workflow_01 ON sj_workflow;
-- ALTER TABLE sj_workflow DROP COLUMN biz_id;
