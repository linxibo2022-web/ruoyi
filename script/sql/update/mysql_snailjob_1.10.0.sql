-- ============================================================================
-- SnailJob 1.9.0 → 1.10.0 增量升级脚本 (MySQL)
-- ============================================================================
-- 变更内容：
--   1. sj_job        新增 biz_id 字段 + 唯一索引 uk_sj_job_01(namespace_id, biz_id)
--   2. sj_workflow   新增 biz_id 字段 + 唯一索引 uk_sj_workflow_01(namespace_id, biz_id)
-- 注意：biz_id 上游定义为 NOT NULL，需先 ADD COLUMN(允许 NULL) → 回填 → 改 NOT NULL → 建唯一索引
-- 升级前请备份对应表！
-- ============================================================================

-- --------------------------- sj_job ---------------------------
-- 1. 新增字段（先允许 NULL，便于历史数据回填）
ALTER TABLE `sj_job`
    ADD COLUMN `biz_id` VARCHAR(64) NULL COMMENT '业务ID' AFTER `namespace_id`;

-- 2. 历史数据回填：用 id 字符串作为 biz_id，保证 (namespace_id, biz_id) 不重复
UPDATE `sj_job` SET `biz_id` = CAST(`id` AS CHAR) WHERE `biz_id` IS NULL;

-- 3. 改为 NOT NULL
ALTER TABLE `sj_job`
    MODIFY COLUMN `biz_id` VARCHAR(64) NOT NULL COMMENT '业务ID';

-- 4. 新增唯一索引
ALTER TABLE `sj_job`
    ADD UNIQUE KEY `uk_sj_job_01` (`namespace_id`, `biz_id`);


-- --------------------------- sj_workflow ---------------------------
-- 1. 新增字段（先允许 NULL）
ALTER TABLE `sj_workflow`
    ADD COLUMN `biz_id` VARCHAR(64) NULL COMMENT '业务ID' AFTER `namespace_id`;

-- 2. 历史数据回填
UPDATE `sj_workflow` SET `biz_id` = CAST(`id` AS CHAR) WHERE `biz_id` IS NULL;

-- 3. 改为 NOT NULL
ALTER TABLE `sj_workflow`
    MODIFY COLUMN `biz_id` VARCHAR(64) NOT NULL COMMENT '业务ID';

-- 4. 新增唯一索引
ALTER TABLE `sj_workflow`
    ADD UNIQUE KEY `uk_sj_workflow_01` (`namespace_id`, `biz_id`);


-- ============================================================================
-- 回滚 SQL（如升级后需回滚到 1.9.0）
-- ============================================================================
-- ALTER TABLE `sj_job` DROP INDEX `uk_sj_job_01`;
-- ALTER TABLE `sj_job` DROP COLUMN `biz_id`;
-- ALTER TABLE `sj_workflow` DROP INDEX `uk_sj_workflow_01`;
-- ALTER TABLE `sj_workflow` DROP COLUMN `biz_id`;
