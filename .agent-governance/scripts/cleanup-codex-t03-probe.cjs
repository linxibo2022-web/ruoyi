#!/usr/bin/env node
'use strict';

/**
 * 仅清理 T-03 已登记的 Codex Hook 探针快照目录。
 *
 * 绝不递归删除：目录必须位于系统临时根目录、名称符合受控模式，且只包含
 * hooks.json.original 这一个普通文件。这样可在 PowerShell 安全 Hook 拦截
 * 绝对路径 Remove-Item 时，仍保持精确的路径边界和可复核的清理行为。
 */
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');

const TEMP_ROOT = fs.realpathSync.native(path.resolve(os.tmpdir()));
const DIRECTORY_PATTERN = /^codex-t03-hook-probe-[A-Za-z0-9-]+$/;
const SNAPSHOT_FILE = 'hooks.json.original';

function fail(message) {
  throw new Error(message);
}

function validateTarget(targetPath) {
  if (!targetPath) fail('缺少已登记的探针临时目录路径');
  const resolved = fs.realpathSync.native(path.resolve(targetPath));
  if (path.dirname(resolved) !== TEMP_ROOT) fail('临时目录不位于系统临时根目录');
  if (!DIRECTORY_PATTERN.test(path.basename(resolved))) fail('临时目录名称不符合 T-03 受控模式');

  const stat = fs.lstatSync(resolved);
  if (!stat.isDirectory() || stat.isSymbolicLink()) fail('临时目录必须是非链接目录');
  const entries = fs.readdirSync(resolved, { withFileTypes: true });
  if (entries.length !== 1 || entries[0].name !== SNAPSHOT_FILE || !entries[0].isFile() || entries[0].isSymbolicLink()) {
    fail('临时目录内容不符合单快照清理契约');
  }
  return { resolved, snapshotPath: path.join(resolved, SNAPSHOT_FILE) };
}

function cleanupTarget(targetPath) {
  const { resolved, snapshotPath } = validateTarget(targetPath);
  fs.unlinkSync(snapshotPath);
  fs.rmdirSync(resolved);
  if (fs.existsSync(resolved)) fail('清理后临时目录仍存在');
  return resolved;
}

function selfTest() {
  const target = path.join(TEMP_ROOT, `codex-t03-hook-probe-selftest-${process.pid}-${Date.now()}`);
  fs.mkdirSync(target);
  fs.writeFileSync(path.join(target, SNAPSHOT_FILE), 'self-test', 'utf8');
  cleanupTarget(target);

  let rejected = false;
  try {
    validateTarget(path.join(TEMP_ROOT, 'outside-t03-cleanup-contract'));
  } catch {
    rejected = true;
  }
  if (!rejected) fail('负例未被拒绝');
  process.stdout.write('[OK] T-03 受控临时目录清理正反例通过\n');
}

if (require.main === module) {
  try {
    if (process.argv[2] === '--self-test') {
      selfTest();
    } else {
      const cleaned = cleanupTarget(process.argv[2]);
      process.stdout.write(`[OK] 已清理已登记临时目录：${cleaned}\n`);
    }
  } catch (error) {
    process.stderr.write(`cleanup-codex-t03-probe: ${error.message}\n`);
    process.exitCode = 1;
  }
}

module.exports = { cleanupTarget, validateTarget };
