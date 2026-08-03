// 使用npx prettier --check .如何检测出十几个而不是几百个说明配置就是有效的
// Prettier 配置文件
// 详细文档：https://prettier.io/docs/en/options.html

export default {
  // 单行最大宽度，超过此宽度的行将被换行
  // 设置为150字符，适合宽屏显示器
  printWidth: 150,

  // 缩进宽度，使用多少个空格表示一个缩进级别
  // 设置为标准的2个空格
  tabWidth: 2,

  // 是否使用制表符(tab)缩进
  // false表示使用空格而不是制表符
  useTabs: false,

  // 每行结束是否添加分号
  // false = 不添加分号（依赖 ASI - 自动分号插入）
  // 现代 JavaScript 趋势，代码更简洁
  semi: false,

  // 是否使用单引号
  // true表示优先使用单引号而不是双引号
  singleQuote: true,

  // 对象属性名是否使用引号
  // "preserve"表示保持原样，不强制添加或删除引号
  quoteProps: 'preserve',

  // JSX中是否使用单引号
  // false表示在JSX中使用双引号（即使singleQuote为true）
  jsxSingleQuote: false,

  // 多行结构的闭合括号是否与最后一行在同一行
  // false表示闭合括号另起一行
  bracketSameLine: false,

  // 对象、数组等结构中最后一项后是否添加逗号
  // "none"表示不添加尾随逗号
  trailingComma: 'none',

  // 括号内部是否添加空格
  // true表示在大括号内添加空格，如{ foo: bar }
  bracketSpacing: true,

  // 嵌入的代码块格式化方式
  // "auto"表示自动检测并格式化嵌入的代码
  embeddedLanguageFormatting: 'auto',

  // 箭头函数参数是否总是用括号包裹
  // "always"表示即使只有一个参数也使用括号，如(x) => x
  arrowParens: 'always',

  // 是否仅格式化包含特定注释的文件
  // false表示格式化所有文件，不需要特定的格式化注释
  requirePragma: false,

  // 是否在格式化后的文件顶部插入特定注释
  // false表示不添加特定注释
  insertPragma: false,

  // 如何处理markdown等文本的换行
  // "preserve"表示保持原文本的换行方式不变
  proseWrap: 'preserve',

  // HTML空白符敏感度
  // "css"表示根据CSS display属性决定空白处理方式
  htmlWhitespaceSensitivity: 'css',

  // 是否缩进Vue文件中的<script>和<style>标签
  // false表示不缩进这些标签内部的代码
  vueIndentScriptAndStyle: false,

  // 行尾换行符类型
  // "auto"表示保留文件原有的换行符类型
  endOfLine: 'auto'
}
