### 本地静态 SVG 图标

放置在此目录下的 `*.svg` 文件会在构建时被 `vite-plugin-svg-icons-ng` 合并成一张 SVG sprite（雪碧图）。

**使用方式**：

```vue
<!-- 与 iconfont 图标用法完全一致 -->
<Icon code="dingtalk" />
<Icon code="dingtalk" size="lg" color="#0089FF" />
```

**命名规则**：

- 文件名即图标代码（kebab-case），例如 `dingtalk.svg` → `<Icon code="dingtalk" />`
- `Icon` 组件优先级：iconfont > iconify > **svg sprite**（此目录）

**使 `color` 属性生效**：

SVG 源文件内的 `fill` / `stroke` 需改为 `currentColor`，否则颜色会固定为 SVG 原始颜色：

```xml
<svg ...>
  <path fill="currentColor" d="..." />
</svg>
```

**适用场景**：

- iconfont / iconify 找不到的品牌图标（钉钉、MaxKey、TopIAM 等）
- 需要与移动端 `plus-uniapp/src/static/*.svg` 保持"放文件即用"体验的场景
- 业务自定义纯色矢量图

**不适用场景**：

- 彩色多色图标（除非 SVG 里显式写颜色，不需 `color` 属性控制）→ 直接放 `public/` 用 `<img>` 更简单
- 大量通用图标（优先走 iconfont 项目 `5022572`）
