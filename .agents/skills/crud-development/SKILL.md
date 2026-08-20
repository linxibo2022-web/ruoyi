---
name: crud-development
description: |
  当需要新增或改造标准业务 CRUD 模块时使用。

  触发场景：
  - 需要创建 Entity、Service、DAO、Mapper 或 Controller
  - 需要补充 PC 或移动端 CRUD 页面
  - 需要生成主子表或树表业务模块

  触发词：CRUD、业务模块、Entity、Service、DAO、Controller、主子表、树表
---

# CRUD 开发入口

## 必做步骤

1. 先阅读同领域既有业务模块；涉及建表时加载 `database-ops`。
2. 后端必须采用 Controller → Service → DAO → Mapper；Entity 继承 `TenantEntity`，BO 使用 `@AutoMappers`，Service 不继承任何基类。
3. DAO 负责查询构造并保留 `buildQueryWrapper()`；转换统一使用 `MapstructUtils`。
4. PC 页面按需读取 `references/full-guide.md` 的前端章节；移动端按需读取相应章节，并遵循 A* / wd-* 组件约束。
5. 完成后执行受影响模块的编译或类型检查。

## 按需参考

- 完整的命名、四层模板、树表与主子表规则：`references/full-guide.md`。

## 边界

- 不适用于只读解释、简单文件定位和单行修复。
- 不默认加载完整参考资料；仅在当前任务需要相应模板时读取。
