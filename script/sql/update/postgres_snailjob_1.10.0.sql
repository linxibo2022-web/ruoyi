-- ============================================================================
-- SnailJob 1.9.0 → 1.10.0 增量升级脚本 (PostgreSQL)
-- ============================================================================
-- 变更内容：
--   1. sj_job        新增 biz_id 字段 + 唯一索引 uk_sj_job_01(namespace_id, biz_id)
--   2. sj_workflow   新增 biz_id 字段 + 唯一索引 uk_sj_workflow_01(namespace_id, biz_id)
-- 注意：PostgreSQL 添加 NOT NULL 列必须先 NULL → 回填 → SET NOT NULL
-- 升级前请备份对应表！
-- ============================================================================

-- --------------------------- sj_job ---------------------------
-- 1. 新增字段（先允许 NULL）
ALTER TABLE sj_job ADD COLUMN biz_id VARCHAR(64) NULL;

-- 2. 历史数据回填
UPDATE sj_job SET biz_id = id::TEXT WHERE biz_id IS NULL;

-- 3. 改为 NOT NULL
ALTER TABLE sj_job ALTER COLUMN biz_id SET NOT NULL;

-- 4. 添加列注释
COMMENT ON COLUMN sj_job.biz_id IS '业务ID';

-- 5. 新增唯一索引
CREATE UNIQUE INDEX uk_sj_job_01 ON sj_job (namespace_id, biz_id);


-- --------------------------- sj_workflow ---------------------------
-- 1. 新增字段（先允许 NULL）
ALTER TABLE sj_workflow ADD COLUMN biz_id VARCHAR(64) NULL;

-- 2. 历史数据回填
UPDATE sj_workflow SET biz_id = id::TEXT WHERE biz_id IS NULL;

-- 3. 改为 NOT NULL
ALTER TABLE sj_workflow ALTER COLUMN biz_id SET NOT NULL;

-- 4. 添加列注释
COMMENT ON COLUMN sj_workflow.biz_id IS '业务ID';

-- 5. 新增唯一索引
CREATE UNIQUE INDEX uk_sj_workflow_01 ON sj_workflow (namespace_id, biz_id);


-- ============================================================================
-- 回滚 SQL（如升级后需回滚到 1.9.0）
-- ============================================================================
-- DROP INDEX IF EXISTS uk_sj_job_01;
-- ALTER TABLE sj_job DROP COLUMN IF EXISTS biz_id;
-- DROP INDEX IF EXISTS uk_sj_workflow_01;
-- ALTER TABLE sj_workflow DROP COLUMN IF EXISTS biz_id;
