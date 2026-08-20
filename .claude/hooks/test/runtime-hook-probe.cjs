#!/usr/bin/env node
'use strict';

const crypto = require('node:crypto');
const fs = require('node:fs');

const FIXED_ENUM_FIELDS = new Set(['hook_event_name', 'tool_name', 'agent_type']);

function sha256(value) {
  return crypto.createHash('sha256').update(value, 'utf8').digest('hex');
}

function valueType(value) {
  if (value === null) return 'null';
  if (Array.isArray(value)) return 'array';
  return typeof value;
}

function summarizeField(name, value) {
  const summary = {
    present: true,
    type: valueType(value)
  };

  if (typeof value === 'string') {
    summary.byteLength = Buffer.byteLength(value, 'utf8');
    summary.sha256 = sha256(value);
    if (FIXED_ENUM_FIELDS.has(name)) summary.enumValue = value;
  } else if (Array.isArray(value)) {
    summary.length = value.length;
    summary.itemTypes = [...new Set(value.map(valueType))].sort();
  } else if (value && typeof value === 'object') {
    summary.fields = Object.keys(value).sort().map((fieldName) => ({
      name: fieldName,
      ...summarizeField(fieldName, value[fieldName])
    }));
  }

  return summary;
}

function sanitizeEvent(input) {
  return {
    captureVersion: 1,
    fields: Object.keys(input).sort().map((fieldName) => ({
      name: fieldName,
      ...summarizeField(fieldName, input[fieldName])
    }))
  };
}

function readStdin() {
  return new Promise((resolve, reject) => {
    let input = '';
    process.stdin.setEncoding('utf8');
    process.stdin.on('data', (chunk) => {
      input += chunk;
    });
    process.stdin.on('end', () => resolve(input));
    process.stdin.on('error', reject);
  });
}

async function main() {
  const outputPath = process.argv[2];
  if (!outputPath) throw new Error('缺少脱敏事件输出路径');

  const raw = await readStdin();
  const input = JSON.parse(raw);
  const event = sanitizeEvent(input);
  fs.appendFileSync(outputPath, `${JSON.stringify(event)}\n`, { encoding: 'utf8' });
}

if (require.main === module) {
  main().catch((error) => {
    process.stderr.write(`runtime-hook-probe: ${error.message}\n`);
    process.exitCode = 1;
  });
}

module.exports = { sanitizeEvent };
