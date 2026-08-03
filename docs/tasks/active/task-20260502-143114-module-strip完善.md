# 任务：module-strip 完善 + 实测其他模块裁剪

**状态**: 🔵 已完成（未提交）
**创建时间**: 2026-05-02 14:31:14
**更新时间**: 2026-05-02 14:55
**Git 分支**: master

---

## 📋 需求描述

第一版 module-strip 已可用（mall 实测通过），但只有 1 个真实可用预设。需要：

1. 添加更多模块预设：pay（支付）、ai（AI）、iot（物联网）
2. 逐个实测每个预设：裁剪 + mvn compile 验证
3. 发现新 bug 时修复脚本
4. **测完不提交**，最终通过 sync 还原代码（保证主项目和交付目录干净）

## 🎯 实现步骤

- [ ] 1. 写 pay 预设（删 ruoyi-common-pay 父模块 + 改 ruoyi-business、ruoyi-mall pom）
  - **依赖**：扫描已完成
  - **路径已确认**：
    - 删 `ruoyi-common/ruoyi-common-pay/`（含 5 个子模块）
    - 改 `ruoyi-common/pom.xml`（移除 `<module>ruoyi-common-pay</module>`）
    - 改 `ruoyi-modules/ruoyi-business/pom.xml`（移除 5 个 pay-* 引用）
    - 改 `ruoyi-modules/ruoyi-mall/pom.xml`（移除 5 个 pay-* 引用）
  - **风险**：ruoyi-business 业务代码可能引用 PayService 类，编译会失败 → manual_actions

- [ ] 2. 写 ai 预设（删 ruoyi-common-langchain4j + 改 business pom）
  - 删 `ruoyi-common/ruoyi-common-langchain4j/`
  - 改 `ruoyi-common/pom.xml`（移除 `<module>ruoyi-common-langchain4j</module>`）
  - 改 `ruoyi-modules/ruoyi-business/pom.xml`（移除 `<artifactId>ruoyi-common-langchain4j</artifactId>`）
  - **风险**：业务侧可能引用 ChatModel 等 → manual_actions

- [ ] 3. 写 iot 预设（删 ruoyi-common-mqtt + 改 business pom）
  - 删 `ruoyi-common/ruoyi-common-mqtt/`
  - 改 `ruoyi-common/pom.xml`
  - 改 `ruoyi-modules/ruoyi-business/pom.xml`
  - **风险**：业务侧可能引用 MqttClientTemplate → manual_actions

- [ ] 4. 实测 pay：strip + mvn compile
  - 预期：mvn compile 可能失败，因为 ruoyi-business 的业务代码引用了 PayService
  - 失败处理：记录错误信息到 manual_actions

- [ ] 5. 实测 ai：strip + mvn compile

- [ ] 6. 实测 iot：strip + mvn compile

- [ ] 7. 完善 module-strip 脚本（如发现新 bug）

- [ ] 8. **不提交**，跑 /sync-delivery --force 还原交付目录

- [ ] 9. 测试期间的脚本/预设 commit 不要做（用户后续决定）

## 📝 关键决策

- **裁剪范围**：本期只裁 ruoyi-common-* 公共模块层级，不动业务代码（业务代码混在 base/ 里没法精准切）
- **失败处理**：mvn compile 失败时不修业务代码（manual_actions 提示用户手动处理）
- **测完还原**：用 /sync-delivery --force 把删掉的文件全部拉回，用户后续决定是否真用

## 🐛 问题记录

（实测中记录）

## 🔄 当前进度

**已完成**: 9/9 步骤 (100%)
**当前状态**:
- 完成所有实测，最终 sync 还原后交付目录恢复完整状态
- baseline: 33a0ea856（与之前一致）
- mall + ruoyi-common-pay 都已还原

## 🧪 实测结果矩阵

### 业务模块（第一轮）
| 模块 | 单独裁剪 | mvn compile | 关键发现 |
|-----|---------|------------|---------|
| **mall** | ✅ | ✅ BUILD SUCCESS | 完整可用，前期已测 |
| **iot** | ✅ | ✅ BUILD SUCCESS | 业务代码不依赖 mqtt 类，独立可裁 |
| **ai** | ✅ | ❌ FAILURE | AiServiceImpl 引用 ChatService/ChatResponse |
| **pay** 单独 | ⚠️ pom 修复后 ✅ | ❌ FAILURE | mall 的 OrderPayController/PayNotifyController 引用 pay 类 |
| **mall + pay** 组合 | ✅ | ✅ BUILD SUCCESS | 验证依赖链断开有效 |

### 通用模块（第二轮，本会话新增）
| 模块 | 单独裁剪 | mvn compile | 关键发现 |
|-----|---------|------------|---------|
| **doctemplate** | ✅ | ✅ BUILD SUCCESS | 无业务依赖，最干净 |
| **rocketmq** | ✅ | ✅ BUILD SUCCESS | 当前业务无 RMSendUtil 引用 |
| **social** | ✅ | ❌ FAILURE | AuthController/SysLoginService 直接 import me.zhyd.oauth.* |
| **doctemplate + rocketmq** 组合 | ✅ | ✅ BUILD SUCCESS | 双模块组合验证通过 |

**本会话新增预设**：`pay.json` `ai.json` `iot.json` `doctemplate.json` `rocketmq.json` `social.json`（共 6 个）

### 微信生态模块（第三轮）
| 模块 | 单独裁剪 | mvn compile | 关键发现 |
|-----|---------|------------|---------|
| **media** | ✅ | ❌ FAILURE | pay-core 的 QrCodeUtils 引用 plus.ruoyi.common.media.builder（depends_on: ["pay"]） |
| **miniapp** | ✅ | ❌ FAILURE | PhoneController + MiniappAuthStrategy 直接 import cn.binarywang.wx.miniapp.* |
| **mp** | ✅ | ❌ FAILURE | WxShareController 直接 import plus.ruoyi.common.mp.domain/utils |

**本轮新增预设**：`media.json` `miniapp.json` `mp.json`（共 3 个）

## 🐛 问题记录

### 问题 1: 连续删除多个 dependency 块导致 pom 残缺
- **发现**: 实测 pay 时 mvn 报 `Non-parseable POM` + `Unrecognised tag: 'dependency'`
- **根因**: `_remove_pom_matches` 用 `result.append`/`result.pop()` 维护，连续删除多个块时 `block_start`（lines 索引）与 `len(result)` 错位，回溯 pop 失效
- **修复**: 改为「待删行号集合 to_delete」方式，最后一次性 filter，避免索引错位
- **状态**: 🟢 已解决

### 问题 2: 注释里的 pattern 占位被误判
- **发现**: 原 pom 有 `<!-- <artifactId>ruoyi-common-pay-unionpay</artifactId> -->` 占位行被命中
- **根因**: 脚本不区分注释与代码
- **修复**: 加 `_in_comment` 函数，跨行注释也能识别
- **状态**: 🟢 已解决

### 问题 3: 业务代码反向依赖
- **发现**: ai 单独裁失败、pay 单独裁失败（即使 pom 正确）
- **根因**: 真实业务依赖问题，非脚本 bug
- **应对**: 在预设 manual_actions 注入实测发现的具体类名（AiServiceImpl/OrderPayController/PayNotifyController），用户读 manual_actions 即知后续步骤
- **状态**: 🟢 已通过文档说明解决

## 💬 变更记录

### 2026-05-02 14:31
- 创建任务跟踪、扫描路径

### 2026-05-02 14:35
- 写好 pay/ai/iot 预设

### 2026-05-02 14:40
- 实测 ai → 编译失败（AiServiceImpl 反向依赖）
- 实测 iot → BUILD SUCCESS

### 2026-05-02 14:45
- 实测 pay → 发现脚本 bug：连续删除多块时索引错位，pom 解析失败
- 修复脚本：to_delete 集合方式 + 注释跳过

### 2026-05-02 14:50
- 重测 pay → pom 修复 ✓，但 mall 反向依赖编译失败
- 实测 mall + pay 组合 → BUILD SUCCESS

### 2026-05-02 14:55
- 完善 3 个预设 manual_actions（注入实测信息）
- pay.json 增加 `depends_on: ["mall"]`
- 最终 sync --force 还原交付目录
- **未提交**（按用户要求保留为待 commit 状态）

### 2026-05-03（第二轮：通用模块）
- 扫描可裁的通用模块依赖范围
- 新增 3 个预设：rocketmq.json / social.json / doctemplate.json
- 实测组合 doctemplate+rocketmq+social → social 失败（AuthController 引用 me.zhyd.oauth.*）
- 单独测 doctemplate+rocketmq → BUILD SUCCESS
- 完善 3 个新预设 manual_actions（注入实测发现）
- 最终 sync --force 还原（确认 mall/pay/langchain4j/mqtt/doctemplate/rocketmq/social 全部恢复）

### 实测累计成果
- ✅ 12 次裁剪覆盖 12 个模块（mall/iot/ai/pay/mall+pay/doctemplate/rocketmq/social/doctemplate+rocketmq/media/miniapp/mp）
- ✅ 10 个预设可用（mall/iot/ai/pay/doctemplate/rocketmq/social/media/miniapp/mp，含警告）
- ✅ 修复 2 个真实脚本 bug（块感知 + 注释跳过）
- ✅ 全部预设 manual_actions 含实测验证标记和具体类名引用
- ✅ 主项目代码完全干净，无未提交破坏性改动

### 2026-05-03（第三轮：微信生态模块）
- 实测 media → 失败（pay-core 的 QrCodeUtils 反向依赖）
- 实测 miniapp → 失败（PhoneController/MiniappAuthStrategy 反向依赖）
- 实测 mp → 失败（WxShareController 反向依赖）
- 完善 3 个预设 manual_actions（注入实测发现的具体类名）
- media.json 设置 `depends_on: ["pay"]`
- 最终 sync --force 还原（确认 3 个模块全部恢复）

## 📁 相关文件

- `.claude/skills/module-strip/presets/mall.json` - 已有
- `.claude/skills/module-strip/presets/pay.json` - 待创建
- `.claude/skills/module-strip/presets/ai.json` - 待创建
- `.claude/skills/module-strip/presets/iot.json` - 待创建
- `.claude/skills/module-strip/scripts/module_strip.py` - 视实测情况完善

## ⚠️ 注意事项

- 测试不提交 git，最终用 sync 还原（避免污染交付目录长期状态）
- 每次 strip 后必须 sync 还原才能跑下一个 strip 实测
- 如果 mvn compile 失败，是真实业务依赖问题，不是脚本 bug

## 💬 变更记录

### 2026-05-02 14:31
**变更类型**: 任务创建

**变更内容**:
- 创建任务跟踪
- 完成路径扫描，确认 pay/ai/iot 涉及的 pom 引用位置
- 关键发现：业务侧没有独立 pay/ai/iot 目录，common 模块被 ruoyi-business + ruoyi-mall 引用
