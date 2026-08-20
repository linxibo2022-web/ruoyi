const MARKER = 'SUBAGENT_SKILL_ROUTE';
const SCHEMA_VERSION = 1;

function isStringArray(value) {
  return Array.isArray(value) && value.every(item => typeof item === 'string');
}

function isGeneratedPayload(value) {
  if (!value || typeof value !== 'object' || Array.isArray(value)) return false;
  const keys = Object.keys(value).sort().join(',');
  if (keys !== 'baselineCapabilities,helpers,optionalSkills,primary,schemaVersion,status') return false;
  return value.schemaVersion === SCHEMA_VERSION
    && ['matched', 'no-match', 'router-error', 'invalid-input'].includes(value.status)
    && (value.primary === null || typeof value.primary === 'string')
    && isStringArray(value.helpers)
    && isStringArray(value.baselineCapabilities)
    && isStringArray(value.optionalSkills);
}

function renderEnvelope(payload) {
  if (!isGeneratedPayload(payload)) throw new TypeError('无效的 SUBAGENT_SKILL_ROUTE 信封');
  return `<!-- ${MARKER}\n${JSON.stringify(payload)}\n-->`;
}

/** 只移除消息末尾的完整、可由本库生成的信封；正文和截断标记原样保留。 */
function stripTerminalEnvelopes(message) {
  if (typeof message !== 'string') return { message, removed: 0 };
  let clean = message;
  let removed = 0;
  const suffix = new RegExp(`\\s*<!-- ${MARKER}\\n([^\\n]+)\\n-->\\s*$`);
  for (;;) {
    const match = clean.match(suffix);
    if (!match) break;
    let payload;
    try { payload = JSON.parse(match[1]); } catch { break; }
    if (!isGeneratedPayload(payload)) break;
    clean = clean.slice(0, match.index);
    removed += 1;
  }
  return { message: clean, removed };
}

function replaceTerminalEnvelope(message, payload) {
  if (typeof message !== 'string') throw new TypeError('message 必须为字符串');
  const stripped = stripTerminalEnvelopes(message);
  return { message: `${stripped.message.replace(/\s+$/, '')}\n\n${renderEnvelope(payload)}`, removed: stripped.removed };
}

module.exports = { MARKER, SCHEMA_VERSION, isGeneratedPayload, renderEnvelope, stripTerminalEnvelopes, replaceTerminalEnvelope };
