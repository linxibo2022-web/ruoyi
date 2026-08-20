
# /strip-modules - 业务模块裁剪助手

在交付目录里删除指定业务模块（删目录 + 改 pom.xml）。是 `/sync-delivery` 的下游补充。

> 本命令在 Claude Code 与 Codex CLI 双端可用。

---

## 触发方式

```
/strip-modules                       # 用 .delivery-sync.json 中 stripModules 配置
/strip-modules mall iot              # 临时指定模块（覆盖配置）
/strip-modules --list                # 列出所有可用预设
/strip-modules --preview             # 仅预览要做的改动
/strip-modules --verify              # 裁剪后跑 mvn compile 验证
```

---

## 执行流程

### 第一步：环境检查

脚本会自动检查：
- `.delivery-sync.json` 存在（来自 /sync-delivery）
- 交付目录路径有效
- 交付目录根有 `.delivery-sync-marker`（确认是受管目录）

### 第二步：调用脚本

```bash
PYTHONIOENCODING=utf-8 python .claude/skills/module-strip/scripts/module_strip.py [选项]
```

### 第三步：根据脚本输出

- **预览模式**：列出每个模块会删什么、改哪些 pom.xml，但不实际执行
- **正式执行**：实际删除 + 修改，输出统计报告
- **末尾**：列出"待手动处理"清单（SQL 中相关表/菜单初始化、前端路由等）

### 第四步（可选）：编译验证

```
/strip-modules --verify
```

会在交付目录跑 `mvn compile`。失败时显示最后 2KB 错误日志便于定位。

---

## 推荐工作流

```
# 完整交付流程
/sync-delivery                          # 1. 同步代码
/strip-modules --preview                # 2. 预览裁剪
/strip-modules --verify                 # 3. 实际裁剪 + 编译验证
```

---

## 详细规范

完整流程、安全检查、添加自定义预设的方法等详见 skill 文档：

`.claude/skills/module-strip/SKILL.md`（Codex 端：`.agents/skills/module-strip/SKILL.md`）

---

## 关联命令

- `/sync-delivery` 主项目 → 交付目录文件级镜像
- `/sync-branches-local` 多分支同步（master → single/workflow）
