import pluginVue from 'eslint-plugin-vue'

// 导入全局变量定义
import globals from 'globals'

// 导入 Prettier ESLint 插件，用于代码格式化
import prettier from 'eslint-plugin-prettier'

// 导入 Vue TypeScript 配置辅助函数和预设配置
import { defineConfigWithVueTs, vueTsConfigs } from '@vue/eslint-config-typescript'

// 导入跳过 Prettier 格式化的配置
import skipFormatting from '@vue/eslint-config-prettier/skip-formatting'

// 使用 Vue TypeScript 配置辅助函数定义 ESLint 配置
export default defineConfigWithVueTs(
  // 指定需要检查的文件类型
  {
    name: 'app/files-to-lint',
    // 检查 JavaScript、TypeScript 和 Vue 文件
    files: ['**/*.{js,cjs,ts,mts,tsx,vue}']
  },

  // 指定需要忽略的文件和目录
  {
    name: 'app/files-to-ignore',
    ignores: [
      // 忽略构建输出目录
      '**/dist/**',
      // 忽略 SSR 构建输出目录
      '**/dist-ssr/**',
      // 忽略测试覆盖率报告目录
      '**/coverage/**',
      // 忽略所有语言文件
      '**/locales/**/*.ts'
    ]
  },

  // 配置语言选项
  {
    languageOptions: {
      // 添加浏览器环境的全局变量（如 window, document 等）
      globals: globals.browser
    }
  },

  // 使用 Vue 插件的基本规则配置 - Vue 的基本规则集，检查常见错误
  pluginVue.configs['flat/essential'],

  // 使用 Vue TypeScript 推荐配置 - TypeScript 的推荐规则集
  vueTsConfigs.recommended,

  // 使用 Prettier 配置但跳过格式化相关规则（避免与 ESLint 规则冲突）
  skipFormatting,

  // 自定义规则配置
  {
    // 启用的插件
    plugins: {
      // 启用 Prettier 插件
      prettier
    },

    // 自定义规则设置
    rules: {
      // ================================
      // 代码风格相关规则
      // ================================

      // 分号规则：不使用分号（与 Prettier 配置保持一致）
      // 'semi': ['error', 'never'],

      // 禁止多余的分号
      // 'no-extra-semi': 'error',

      // ================================
      // TypeScript 相关规则
      // ================================

      // 允许空函数
      '@typescript-eslint/no-empty-function': 'off',

      // 允许使用 any 类型
      '@typescript-eslint/no-explicit-any': 'off',

      // 关闭未使用变量的警告
      '@typescript-eslint/no-unused-vars': 'off',

      // 允许将 this 赋值给变量
      '@typescript-eslint/no-this-alias': 'off',

      // 允许使用空对象类型 {}
      '@typescript-eslint/no-empty-object-type': 'off',

      // 允许未使用的表达式
      '@typescript-eslint/no-unused-expressions': 'off',

      // 允许单词组件名
      'vue/multi-word-component-names': 'off',

      // 关闭 defineProps 验证
      'vue/valid-define-props': 'off',

      // 允许 v-model 带参数
      'vue/no-v-model-argument': 'off',

      // 允许使用 arguments 对象
      'prefer-rest-params': 'off',

      // 强制代码符合 Prettier 格式
      'prettier/prettier': 'error'
    }
  }
)
